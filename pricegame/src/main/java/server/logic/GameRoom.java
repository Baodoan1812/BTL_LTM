package server.logic;

import common.protocol.Message;
import common.protocol.MessageType;
import server.dao.MatchDAO.MatchRecord;
import server.dao.MatchDAO.RoundRecord;
import server.dao.ProductDAO.Product;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Một phòng chơi 2 người. Mọi thao tác trạng thái đều synchronized trên phòng. */
public class GameRoom {
    public static final int ROUNDS = 5;
    private static final int GRACE_SEC = 1; // bù độ trễ mạng cho timeout phía server

    private enum Phase { PLAYING, ASKING, CLOSED }

    public final String id = UUID.randomUUID().toString();
    private final ServerContext ctx;
    private final Player[] ps = new Player[2];

    private Phase phase = Phase.PLAYING;
    private List<Product> products;
    private int round;
    private final double[] score = new double[2];
    private final Double[] guess = new Double[2];
    private boolean roundOpen;
    private ScheduledFuture<?> timer;
    private final boolean[] again = new boolean[2];
    private MatchRecord record;

    public GameRoom(ServerContext ctx, Player p1, Player p2) {
        this.ctx = ctx;
        ps[0] = p1;
        ps[1] = p2;
        p1.room = this;
        p2.room = this;
    }

    private int idx(Player p) { return ps[0] == p ? 0 : (ps[1] == p ? 1 : -1); }

    /** Bắt đầu (hoặc bắt đầu lại) một trận 5 lượt. */
    public synchronized void start() {
        try {
            products = ctx.products.random(ROUNDS);
        } catch (Exception e) {
            products = null;
        }
        if (products == null || products.size() < ROUNDS) {
            for (Player p : ps) p.send(Message.of(MessageType.ROOM_CLOSED).put("reason", "Server không lấy được sản phẩm"));
            close();
            return;
        }
        phase = Phase.PLAYING;
        round = 0;
        score[0] = score[1] = 0;
        again[0] = again[1] = false;
        record = new MatchRecord();
        record.player1Id = ps[0].id;
        record.player2Id = ps[1].id;
        for (int i = 0; i < 2; i++) {
            ps[i].send(Message.of(MessageType.MATCH_START).put("roomId", id)
                    .put("opponent", ps[1 - i].name).put("rounds", ROUNDS));
        }
        startRound();
    }

    private void startRound() {
        round++;
        guess[0] = guess[1] = null;
        roundOpen = true;
        Product p = products.get(round - 1);
        Message m = Message.of(MessageType.ROUND_START).put("round", round).put("total", ROUNDS)
                .put("name", p.name).put("image", p.image).put("desc", p.description)
                .put("timeLimit", ctx.timeLimitSec);
        for (Player pl : ps) pl.send(m);
        final int r = round;
        timer = ctx.scheduler.schedule(() -> endRound(r), ctx.timeLimitSec + GRACE_SEC, TimeUnit.SECONDS);
    }

    public synchronized void guess(Player p, double price) {
        int i = idx(p);
        if (i < 0 || phase != Phase.PLAYING || !roundOpen || guess[i] != null) return;
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0) return;
        guess[i] = price;
        ps[1 - i].send(Message.of(MessageType.OPPONENT_SUBMITTED));
        if (guess[0] != null && guess[1] != null) endRound(round);
    }

    /** Chốt lượt (đủ 2 giá hoặc hết giờ). Cờ roundOpen chống chốt hai lần. */
    private synchronized void endRound(int expectedRound) {
        if (phase != Phase.PLAYING || !roundOpen || round != expectedRound) return;
        roundOpen = false;
        if (timer != null) timer.cancel(false);

        Product p = products.get(round - 1);
        double[] s = ScoreCalculator.scoreRound(p.price, guess[0], guess[1]);
        score[0] += s[0];
        score[1] += s[1];

        RoundRecord rr = new RoundRecord();
        rr.roundNo = round;
        rr.productId = p.id;
        rr.guess1 = guess[0];
        rr.guess2 = guess[1];
        rr.score1 = s[0];
        rr.score2 = s[1];
        record.rounds.add(rr);

        int winner = s[0] > s[1] ? 1 : (s[1] > s[0] ? 2 : 0);
        for (int i = 0; i < 2; i++) {
            Message m = Message.of(MessageType.ROUND_RESULT).put("round", round).put("actual", p.price)
                    .put("winner", winner).put("score1", score[0]).put("score2", score[1]).put("you", i + 1);
            if (guess[0] != null) m.put("g1", guess[0]);
            if (guess[1] != null) m.put("g2", guess[1]);
            ps[i].send(m);
        }

        final int r = round;
        ctx.scheduler.schedule(() -> nextStep(r), ctx.nextRoundDelayMs, TimeUnit.MILLISECONDS);
    }

    private synchronized void nextStep(int finishedRound) {
        if (phase != Phase.PLAYING || round != finishedRound || roundOpen) return;
        if (round < ROUNDS) startRound(); else finishMatch();
    }

    private void finishMatch() {
        phase = Phase.ASKING;
        record.score1 = score[0];
        record.score2 = score[1];
        // Integer.valueOf: tránh NPE do unboxing của toán tử ba ngôi khi hòa (null)
        record.winnerId = score[0] > score[1] ? Integer.valueOf(ps[0].id)
                : (score[1] > score[0] ? Integer.valueOf(ps[1].id) : null);
        record.endReason = "NORMAL";
        persist();
        for (int i = 0; i < 2; i++) {
            String res = score[i] > score[1 - i] ? "WIN" : (score[i] < score[1 - i] ? "LOSE" : "DRAW");
            ps[i].send(Message.of(MessageType.MATCH_END).put("score1", score[0]).put("score2", score[1])
                    .put("you", i + 1).put("result", res));
        }
        for (Player p : ps) p.send(Message.of(MessageType.PLAY_AGAIN_ASK));
    }

    /** Lưu DB và cập nhật điểm trong bộ nhớ để danh sách online hiển thị đúng. */
    private void persist() {
        try {
            ctx.matches.save(record);
        } catch (Exception e) {
            System.err.println("Không lưu được trận đấu: " + e);
        }
        for (int i = 0; i < 2; i++) {
            ps[i].score += score[i];
            if (record.winnerId != null && record.winnerId == ps[i].id) ps[i].wins++;
        }
    }

    public synchronized void playAgain(Player p, boolean yes) {
        int i = idx(p);
        if (i < 0 || phase != Phase.ASKING) return;
        if (!yes) {
            ps[1 - i].send(Message.of(MessageType.ROOM_CLOSED).put("reason", p.name + " không chơi tiếp"));
            close();
            return;
        }
        again[i] = true;
        if (again[0] && again[1]) start();
    }

    /** Một bên bấm Thoát hoặc mất kết nối. */
    public synchronized void leave(Player p, boolean disconnected) {
        int i = idx(p);
        if (i < 0 || phase == Phase.CLOSED) return;
        Player other = ps[1 - i];
        if (phase == Phase.PLAYING) {
            record.score1 = score[0];
            record.score2 = score[1];
            record.winnerId = other.id; // bên còn lại thắng
            record.endReason = disconnected ? "DISCONNECT" : "EXIT";
            persist();
        }
        other.send(Message.of(MessageType.OPPONENT_LEFT).put("name", p.name));
        close();
    }

    private void close() {
        phase = Phase.CLOSED;
        roundOpen = false;
        if (timer != null) timer.cancel(false);
        for (Player p : ps) ctx.players.release(p);
        ctx.players.broadcastOnlineList();
    }
}

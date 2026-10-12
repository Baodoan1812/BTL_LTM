package server.logic;

import common.protocol.Message;
import common.protocol.MessageType;
import org.junit.jupiter.api.Test;
import server.dao.MatchDAO;
import server.dao.ProductDAO.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Chạy GameRoom với người chơi giả, không cần DB/mạng. */
class GameRoomTest {
    private final AtomicReference<MatchDAO.MatchRecord> saved = new AtomicReference<>();
    private ServerContext ctx;

    private static class Fake {
        final Player player;
        final List<Message> inbox = new CopyOnWriteArrayList<>();
        Fake(int id, String name) { player = new Player(id, name, 0, 0, inbox::add); }
        long count(MessageType t) { return inbox.stream().filter(m -> m.type == t).count(); }
        Message last(MessageType t) {
            Message r = null;
            for (Message m : inbox) if (m.type == t) r = m;
            return r;
        }
    }

    private Fake[] setup() {
        ctx = new ServerContext(n -> {
            List<Product> l = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Product p = new Product();
                p.id = i + 1; p.name = "P" + i; p.price = 1000; p.image = "x.jpg";
                l.add(p);
            }
            return l;
        }, saved::set);
        ctx.timeLimitSec = 1;
        ctx.nextRoundDelayMs = 20;
        Fake a = new Fake(1, "a"), b = new Fake(2, "b");
        assertTrue(ctx.players.add(a.player));
        assertTrue(ctx.players.add(b.player));
        assertTrue(ctx.players.tryBusy(a.player, b.player));
        new GameRoom(ctx, a.player, b.player).start();
        return new Fake[]{a, b};
    }

    private static void waitFor(java.util.function.BooleanSupplier c) throws InterruptedException {
        long end = System.currentTimeMillis() + 10_000;
        while (!c.getAsBoolean() && System.currentTimeMillis() < end) Thread.sleep(10);
        assertTrue(c.getAsBoolean(), "timeout chờ điều kiện");
    }

    @Test void fullMatchAWinsAndPersists() throws Exception {
        Fake[] f = setup();
        for (int r = 1; r <= GameRoom.ROUNDS; r++) {
            final int round = r;
            waitFor(() -> f[0].count(MessageType.ROUND_START) == round);
            f[0].player.room.guess(f[0].player, 990);  // a gần hơn
            f[1].player.room.guess(f[1].player, 500);
            f[1].player.room.guess(f[1].player, 999);  // gửi lần 2 bị bỏ qua
        }
        waitFor(() -> f[0].count(MessageType.PLAY_AGAIN_ASK) == 1);
        assertEquals("WIN", f[0].last(MessageType.MATCH_END).str("result"));
        assertEquals("LOSE", f[1].last(MessageType.MATCH_END).str("result"));
        assertEquals(5.0, saved.get().score1);
        assertEquals(0.0, saved.get().score2);
        assertEquals(1, saved.get().winnerId);
        assertEquals(5, saved.get().rounds.size());

        // chơi tiếp: cả hai đồng ý mới có trận mới
        GameRoom room = f[0].player.room;
        room.playAgain(f[0].player, true);
        assertEquals(1, f[0].count(MessageType.MATCH_START));
        room.playAgain(f[1].player, true);
        assertEquals(2, f[0].count(MessageType.MATCH_START));
    }

    @Test void timeoutGivesPointToSubmitter() throws Exception {
        Fake[] f = setup();
        waitFor(() -> f[0].count(MessageType.ROUND_START) == 1);
        f[1].player.room.guess(f[1].player, 1000);   // chỉ b gửi giá
        waitFor(() -> f[0].count(MessageType.ROUND_RESULT) == 1); // hết giờ sau ~2s
        Message res = f[0].last(MessageType.ROUND_RESULT);
        assertEquals(2, res.integer("winner"));
        assertEquals(1.0, res.num("score2"));
    }

    @Test void disconnectMidMatchOtherWins() throws Exception {
        Fake[] f = setup();
        waitFor(() -> f[0].count(MessageType.ROUND_START) == 1);
        GameRoom room = f[0].player.room;
        room.leave(f[0].player, true);
        assertEquals(1, f[1].count(MessageType.OPPONENT_LEFT));
        assertEquals(2, saved.get().winnerId);
        assertEquals("DISCONNECT", saved.get().endReason);
        assertEquals(Player.Status.IDLE, f[1].player.status);
        assertNull(f[1].player.room);
    }

    @Test void declineRematchClosesRoom() throws Exception {
        Fake[] f = setup();
        for (int r = 1; r <= GameRoom.ROUNDS; r++) {
            final int round = r;
            waitFor(() -> f[0].count(MessageType.ROUND_START) == round);
            f[0].player.room.guess(f[0].player, 1000);
            f[1].player.room.guess(f[1].player, 1000);
        }
        waitFor(() -> f[0].count(MessageType.PLAY_AGAIN_ASK) == 1);
        assertEquals("DRAW", f[0].last(MessageType.MATCH_END).str("result"));
        assertNull(saved.get().winnerId);
        GameRoom room = f[0].player.room;
        room.playAgain(f[1].player, false);
        assertEquals(1, f[0].count(MessageType.ROOM_CLOSED));
        assertEquals(Player.Status.IDLE, f[0].player.status);
    }
}

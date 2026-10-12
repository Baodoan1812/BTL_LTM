package server.logic;

import common.protocol.Message;
import common.protocol.MessageType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** Lời mời thách đấu: tạo, chấp nhận/từ chối, hết hạn, hủy khi một bên thoát. */
public class ChallengeManager {
    private static final int EXPIRE_SECONDS = 30;

    private static class Challenge {
        final String id = UUID.randomUUID().toString();
        final Player from, to;
        Challenge(Player from, Player to) { this.from = from; this.to = to; }
    }

    private final ServerContext ctx;
    private final Map<String, Challenge> pending = new ConcurrentHashMap<>();

    public ChallengeManager(ServerContext ctx) { this.ctx = ctx; }

    private boolean involved(Player p) {
        return pending.values().stream().anyMatch(c -> c.from == p || c.to == p);
    }

    public synchronized void request(Player from, int targetId) {
        Player to = ctx.players.get(targetId);
        String err = null;
        if (to == null) err = "Người chơi không còn online";
        else if (to == from) err = "Không thể tự thách đấu";
        else if (from.status != Player.Status.IDLE) err = "Bạn đang bận";
        else if (to.status != Player.Status.IDLE) err = "Đối thủ đang bận";
        else if (involved(from) || involved(to)) err = "Có lời mời đang chờ xử lý";
        if (err != null) {
            from.send(Message.of(MessageType.CHALLENGE_FAIL).put("reason", err));
            return;
        }
        Challenge c = new Challenge(from, to);
        pending.put(c.id, c);
        to.send(Message.of(MessageType.CHALLENGE_INVITE).put("challengeId", c.id).put("fromName", from.name));
        ctx.scheduler.schedule(() -> expire(c.id), EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    private void expire(String id) {
        Challenge c = pending.remove(id);
        if (c != null) {
            c.from.send(Message.of(MessageType.CHALLENGE_FAIL).put("reason", c.to.name + " không phản hồi"));
        }
    }

    public synchronized void respond(Player responder, String challengeId, boolean accept) {
        Challenge c = pending.get(challengeId);
        if (c == null || c.to != responder) {
            responder.send(Message.of(MessageType.CHALLENGE_FAIL).put("reason", "Lời mời đã hết hạn"));
            return;
        }
        pending.remove(challengeId);
        if (!accept) {
            c.from.send(Message.of(MessageType.CHALLENGE_REJECTED).put("byName", responder.name));
            return;
        }
        if (!ctx.players.tryBusy(c.from, c.to)) {
            c.from.send(Message.of(MessageType.CHALLENGE_FAIL).put("reason", "Không thể bắt đầu trận"));
            responder.send(Message.of(MessageType.CHALLENGE_FAIL).put("reason", "Không thể bắt đầu trận"));
            return;
        }
        ctx.players.broadcastOnlineList();
        new GameRoom(ctx, c.from, c.to).start();
    }

    /** Một bên rời hệ thống: hủy các lời mời liên quan. */
    public synchronized void cancelFor(Player p) {
        for (Challenge c : pending.values()) {
            if (c.from == p || c.to == p) {
                pending.remove(c.id);
                Player other = c.from == p ? c.to : c.from;
                other.send(Message.of(MessageType.CHALLENGE_FAIL).put("reason", p.name + " đã rời đi"));
            }
        }
    }
}

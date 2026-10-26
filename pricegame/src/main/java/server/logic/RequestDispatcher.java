package server.logic;

import common.protocol.Message;
import common.protocol.MessageType;
import server.dao.UserDAO;

/** Phân phối message của client tới dịch vụ nghiệp vụ tương ứng. */
public class RequestDispatcher {
    private final ServerContext ctx;

    public RequestDispatcher(ServerContext ctx) { this.ctx = ctx; }

    public ServerContext context() { return ctx; }

    /**
     * @param session phiên kết nối (giữ người chơi sau khi đăng nhập)
     */
    public void dispatch(Session session, Message m) {
        if (m.type == MessageType.REGISTER_REQ) { register(session, m); return; }
        if (m.type == MessageType.LOGIN_REQ) { login(session, m); return; }

        Player p = session.player;
        if (p == null) return; // chưa đăng nhập: bỏ qua mọi message khác

        switch (m.type) {
            case LOGOUT -> session.logout();
            case CHALLENGE_REQ -> ctx.challenges.request(p, m.integer("targetId"));
            case CHALLENGE_RESPONSE -> ctx.challenges.respond(p, m.str("challengeId"), m.bool("accept"));
            case GUESS -> { GameRoom r = p.room; if (r != null) r.guess(p, m.num("price")); }
            case PLAY_AGAIN_RES -> { GameRoom r = p.room; if (r != null) r.playAgain(p, m.bool("yes")); }
            case EXIT_MATCH -> { GameRoom r = p.room; if (r != null) r.leave(p, false); }
            case RANKING_REQ -> {
                try {
                    p.send(Message.of(MessageType.RANKING_RES).put("ranking", ctx.rankingDAO.top(20)));
                } catch (Exception e) {
                    System.err.println("Ranking: " + e);
                }
            }
            case HISTORY_REQ -> {
                try {
                    int uid = m.integer("userId");
                    p.send(Message.of(MessageType.HISTORY_RES).put("userId", uid)
                            .put("name", m.str("name")).put("history", ctx.matchDAO.history(uid, 50)));
                } catch (Exception e) {
                    System.err.println("History: " + e);
                }
            }
            default -> { }
        }
    }

    private void register(Session s, Message m) {
        String u = m.str("username"), pw = m.str("password");
        Message res = Message.of(MessageType.REGISTER_RES);
        if (u == null || pw == null || !u.matches("[A-Za-z0-9_]{3,20}") || pw.length() < 4) {
            s.send(res.put("success", false).put("reason", "Tên 3-20 ký tự chữ/số/_, mật khẩu tối thiểu 4 ký tự"));
            return;
        }
        try {
            boolean ok = ctx.userDAO.create(u, pw);
            s.send(res.put("success", ok).put("reason", ok ? "" : "Tên đăng nhập đã tồn tại"));
        } catch (Exception e) {
            s.send(res.put("success", false).put("reason", "Lỗi cơ sở dữ liệu"));
        }
    }

    private void login(Session s, Message m) {
        Message res = Message.of(MessageType.LOGIN_RES);
        if (s.player != null) { s.send(res.put("success", false).put("reason", "Đã đăng nhập")); return; }
        String u = m.str("username"), pw = m.str("password");
        try {
            UserDAO.UserRow row = u == null || pw == null ? null : ctx.userDAO.findByName(u);
            if (row == null || !row.passwordHash.equals(UserDAO.hash(row.salt, pw))) {
                s.send(res.put("success", false).put("reason", "Sai tên đăng nhập hoặc mật khẩu"));
                return;
            }
            Player p = new Player(row.id, row.username, row.score, row.wins, s::send);
            if (!ctx.players.add(p)) {
                s.send(res.put("success", false).put("reason", "Tài khoản đang đăng nhập ở nơi khác"));
                return;
            }
            s.player = p;
            s.send(res.put("success", true).put("reason", "").put("id", p.id).put("name", p.name).put("score", p.score));
            ctx.players.broadcastOnlineList();
        } catch (Exception e) {
            System.err.println("Login: " + e);
            s.send(res.put("success", false).put("reason", "Lỗi cơ sở dữ liệu"));
        }
    }

    /** Dọn dẹp khi đăng xuất hoặc mất kết nối. */
    public void cleanup(Player p) {
        GameRoom r = p.room;
        if (r != null) r.leave(p, true);
        ctx.challenges.cancelFor(p);
        ctx.players.remove(p);
    }

    /** Phiên của một kết nối. */
    public abstract static class Session {
        public volatile Player player;
        public abstract void send(Message m);
        public abstract void logout();
    }
}

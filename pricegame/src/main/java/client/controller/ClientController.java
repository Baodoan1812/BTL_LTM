package client.controller;

import client.network.ServerConnection;
import client.view.*;
import common.protocol.Message;
import common.protocol.MessageType;

import javax.swing.*;

/** Nhận sự kiện UI → gửi message; nhận message server → cập nhật giao diện (luôn trên EDT). */
public class ClientController {
    private final ServerConnection conn = new ServerConnection();
    private LoginFrame login;
    private LobbyFrame lobby;
    private GameFrame game;
    private RankingFrame ranking;
    private ChallengeDialog invite;
    private ResultDialog result;
    private String myName;
    private String opponent;
    private boolean loggedIn;

    public void start() {
        login = new LoginFrame(this);
        login.setVisible(true);
    }

    // ----- hành động của người dùng -----
    public void connectAndSend(String host, int port, Message first) {
        new Thread(() -> {
            try {
                if (!conn.isConnected()) conn.connect(host, port, m -> SwingUtilities.invokeLater(() -> handle(m)),
                        () -> SwingUtilities.invokeLater(this::onDisconnected));
                conn.send(first);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> login.showError("Không kết nối được server: " + e.getMessage()));
            }
        }, "connect").start();
    }

    public void login(String host, int port, String user, String pass) {
        connectAndSend(host, port, Message.of(MessageType.LOGIN_REQ).put("username", user).put("password", pass));
    }

    public void register(String host, int port, String user, String pass) {
        connectAndSend(host, port, Message.of(MessageType.REGISTER_REQ).put("username", user).put("password", pass));
    }

    public void challenge(int targetId) { conn.send(Message.of(MessageType.CHALLENGE_REQ).put("targetId", targetId)); }

    public void respondChallenge(String id, boolean accept) {
        conn.send(Message.of(MessageType.CHALLENGE_RESPONSE).put("challengeId", id).put("accept", accept));
    }

    public void guess(double price) { conn.send(Message.of(MessageType.GUESS).put("price", price)); }
    public void exitMatch() { conn.send(Message.of(MessageType.EXIT_MATCH)); backToLobby(); }

    public void playAgain(boolean yes) {
        conn.send(Message.of(MessageType.PLAY_AGAIN_RES).put("yes", yes));
        if (!yes) backToLobby();
    }

    public void requestRanking() { conn.send(Message.of(MessageType.RANKING_REQ)); }

    public void requestHistory(int userId, String name) {
        conn.send(Message.of(MessageType.HISTORY_REQ).put("userId", userId).put("name", name));
    }

    public void logout() {
        conn.send(Message.of(MessageType.LOGOUT));
        conn.close();
        Sound.stopMusic();
    }

    // ----- xử lý message từ server -----
    private void handle(Message m) {
        switch (m.type) {
            case REGISTER_RES -> {
                if (m.bool("success")) login.showInfo("Đăng ký thành công, hãy đăng nhập");
                else login.showError(m.str("reason"));
            }
            case LOGIN_RES -> {
                if (m.bool("success")) {
                    myName = m.str("name");
                    loggedIn = true;
                    login.setVisible(false);
                    Sound.startMusic();
                    lobby = new LobbyFrame(this, myName);
                    lobby.setVisible(true);
                } else {
                    login.showError(m.str("reason"));
                }
            }
            case ONLINE_LIST -> { if (lobby != null) lobby.updateList(m.arr("players"), myName); }
            case CHALLENGE_INVITE -> {
                closeInvite();
                Sound.play(Sound.Effect.CHALLENGE);
                invite = new ChallengeDialog(lobby, this, m.str("challengeId"), m.str("fromName"));
                invite.setVisible(true);
            }
            case CHALLENGE_REJECTED -> lobby.info(m.str("byName") + " đã từ chối lời thách đấu");
            case CHALLENGE_FAIL -> { closeInvite(); lobby.info(m.str("reason")); }
            case MATCH_START -> {
                closeInvite();
                closeResult();
                opponent = m.str("opponent");
                Sound.setInGame(true);
                if (game == null) game = new GameFrame(this);
                game.startMatch(myName, opponent, m.integer("rounds"));
                game.setVisible(true);
                lobby.setVisible(false);
            }
            case ROUND_START -> game.startRound(m);
            case OPPONENT_SUBMITTED -> game.opponentSubmitted();
            case ROUND_RESULT -> game.showRoundResult(m);
            case MATCH_END -> game.showMatchEnd(m);
            case PLAY_AGAIN_ASK -> {
                closeResult();
                result = new ResultDialog(game, this, game.lastResultText());
                result.setVisible(true);
            }
            case OPPONENT_LEFT -> {
                closeResult();
                lobby.info(m.str("name") + " đã rời trận. Bạn thắng!");
                backToLobby();
            }
            case ROOM_CLOSED -> {
                closeResult();
                lobby.info(m.str("reason"));
                backToLobby();
            }
            case RANKING_RES -> {
                if (ranking == null) ranking = new RankingFrame(this);
                ranking.show(m.arr("ranking"));
            }
            case HISTORY_RES -> new HistoryDialog(ranking, m.str("name"), m.arr("history")).setVisible(true);
            default -> { }
        }
    }

    private void backToLobby() {
        Sound.setInGame(false);
        closeResult();
        if (game != null) game.setVisible(false);
        if (lobby != null) lobby.setVisible(true);
    }

    private void closeInvite() { if (invite != null) { invite.dispose(); invite = null; } }
    private void closeResult() { if (result != null) { result.dispose(); result = null; } }

    private void onDisconnected() {
        if (!loggedIn && !login.isVisible()) return;
        boolean wasIn = loggedIn;
        loggedIn = false;
        Sound.stopMusic();
        closeInvite();
        closeResult();
        if (game != null) game.setVisible(false);
        if (lobby != null) { lobby.dispose(); lobby = null; }
        login.setVisible(true);
        if (wasIn) login.showError("Mất kết nối tới server");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.useSystemLook();
            new ClientController().start();
        });
    }
}

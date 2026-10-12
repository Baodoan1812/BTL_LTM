package server.logic;

import common.protocol.Message;

/** Người chơi đang online. */
public class Player {
    public enum Status { IDLE, BUSY }

    public final int id;
    public final String name;
    public volatile double score;
    public volatile int wins;
    public volatile Status status = Status.IDLE;
    public volatile GameRoom room;
    private final Sender sender;

    public interface Sender { void send(Message m); }

    public Player(int id, String name, double score, int wins, Sender sender) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.wins = wins;
        this.sender = sender;
    }

    public void send(Message m) { sender.send(m); }
}

package server.logic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.protocol.Message;
import common.protocol.MessageType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Quản lý người chơi online và trạng thái rỗi/bận. */
public class PlayerManager {
    private final Map<Integer, Player> online = new ConcurrentHashMap<>();

    /** @return false nếu tài khoản đã đăng nhập ở nơi khác */
    public boolean add(Player p) {
        return online.putIfAbsent(p.id, p) == null;
    }

    public void remove(Player p) {
        online.remove(p.id, p);
        broadcastOnlineList();
    }

    public Player get(int id) { return online.get(id); }

    /** Đặt cả hai BUSY một cách nguyên tử; false nếu một trong hai không còn rỗi. */
    public synchronized boolean tryBusy(Player a, Player b) {
        if (a.status != Player.Status.IDLE || b.status != Player.Status.IDLE) return false;
        a.status = Player.Status.BUSY;
        b.status = Player.Status.BUSY;
        return true;
    }

    public synchronized void release(Player p) {
        p.status = Player.Status.IDLE;
        p.room = null;
    }

    public void broadcastOnlineList() {
        JsonArray arr = new JsonArray();
        for (Player p : online.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", p.id);
            o.addProperty("name", p.name);
            o.addProperty("score", p.score);
            o.addProperty("status", p.status.name());
            arr.add(o);
        }
        Message m = Message.of(MessageType.ONLINE_LIST).put("players", arr);
        for (Player p : online.values()) p.send(m);
    }
}

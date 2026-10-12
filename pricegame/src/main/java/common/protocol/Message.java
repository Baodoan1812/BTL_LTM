package common.protocol;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Một message: {"type": "...", "data": {...}}. */
public class Message {
    public MessageType type;
    public JsonObject data = new JsonObject();

    public Message() {}

    public static Message of(MessageType type) {
        Message m = new Message();
        m.type = type;
        return m;
    }

    public Message put(String k, String v) { data.addProperty(k, v); return this; }
    public Message put(String k, Number v) { data.addProperty(k, v); return this; }
    public Message put(String k, Boolean v) { data.addProperty(k, v); return this; }
    public Message put(String k, JsonElement v) { data.add(k, v); return this; }

    public boolean has(String k) { return data.has(k) && !data.get(k).isJsonNull(); }
    public String str(String k) { return has(k) ? data.get(k).getAsString() : null; }
    public double num(String k) { return has(k) ? data.get(k).getAsDouble() : 0; }
    public int integer(String k) { return has(k) ? data.get(k).getAsInt() : 0; }
    public boolean bool(String k) { return has(k) && data.get(k).getAsBoolean(); }
    public JsonArray arr(String k) { return has(k) ? data.getAsJsonArray(k) : new JsonArray(); }
}

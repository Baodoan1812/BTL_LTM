package common.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public final class JsonCodec {
    private static final Gson GSON = new Gson();

    private JsonCodec() {}

    public static String encode(Message m) {
        return GSON.toJson(m); // Gson không xuống dòng => 1 message / 1 dòng
    }

    /** @return message hợp lệ hoặc null nếu dòng rỗng/sai định dạng */
    public static Message decode(String line) {
        try {
            Message m = GSON.fromJson(line, Message.class);
            if (m == null || m.type == null) return null;
            if (m.data == null) m.data = new com.google.gson.JsonObject();
            return m;
        } catch (JsonParseException e) {
            return null;
        }
    }
}

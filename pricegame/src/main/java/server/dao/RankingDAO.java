package server.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.*;

public class RankingDAO {
    public JsonArray top(int limit) throws SQLException {
        JsonArray arr = new JsonArray();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT username, total_score, total_wins FROM users ORDER BY total_score DESC, total_wins DESC LIMIT ?")) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    JsonObject o = new JsonObject();
                    o.addProperty("rank", rank++);
                    o.addProperty("name", rs.getString(1));
                    o.addProperty("score", rs.getDouble(2));
                    o.addProperty("wins", rs.getInt(3));
                    arr.add(o);
                }
            }
        }
        return arr;
    }
}

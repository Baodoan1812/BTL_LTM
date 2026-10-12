package server.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.HexFormat;

public class UserDAO {
    public static class UserRow {
        public int id;
        public String username, salt, passwordHash;
        public double score;
        public int wins, played;
    }

    public static String hash(String salt, String password) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public UserRow findByName(String username) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, username, salt, password, total_score, total_wins, total_played FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                UserRow u = new UserRow();
                u.id = rs.getInt(1);
                u.username = rs.getString(2);
                u.salt = rs.getString(3);
                u.passwordHash = rs.getString(4);
                u.score = rs.getDouble(5);
                u.wins = rs.getInt(6);
                u.played = rs.getInt(7);
                return u;
            }
        }
    }

    /** @return false nếu username đã tồn tại */
    public boolean create(String username, String password) throws SQLException {
        byte[] b = new byte[8];
        new SecureRandom().nextBytes(b);
        String salt = HexFormat.of().formatHex(b);
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(username, salt, password) VALUES (?,?,?)")) {
            ps.setString(1, username);
            ps.setString(2, salt);
            ps.setString(3, hash(salt, password));
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException dup) {
            return false;
        }
    }
}

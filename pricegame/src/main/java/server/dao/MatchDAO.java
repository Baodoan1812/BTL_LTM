package server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO {
    public static class RoundRecord {
        public int roundNo, productId;
        public Double guess1, guess2;
        public double score1, score2;
    }

    public static class MatchRecord {
        public int player1Id, player2Id;
        public double score1, score2;
        public Integer winnerId; // null nếu hòa
        public String endReason;
        public List<RoundRecord> rounds = new ArrayList<>();
    }

    /** Lưu trận + các lượt + cập nhật điểm tổng của 2 người chơi trong một transaction. */
    public void save(MatchRecord m) throws SQLException {
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                int matchId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO match_history(player1_id,player2_id,score1,score2,winner_id,end_reason) VALUES (?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, m.player1Id);
                    ps.setInt(2, m.player2Id);
                    ps.setDouble(3, m.score1);
                    ps.setDouble(4, m.score2);
                    if (m.winnerId == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, m.winnerId);
                    ps.setString(6, m.endReason);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        k.next();
                        matchId = k.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO round_history(match_id,round_no,product_id,guess1,guess2,score1,score2) VALUES (?,?,?,?,?,?,?)")) {
                    for (RoundRecord r : m.rounds) {
                        ps.setInt(1, matchId);
                        ps.setInt(2, r.roundNo);
                        ps.setInt(3, r.productId);
                        if (r.guess1 == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, Math.round(r.guess1));
                        if (r.guess2 == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, Math.round(r.guess2));
                        ps.setDouble(6, r.score1);
                        ps.setDouble(7, r.score2);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE users SET total_score=total_score+?, total_wins=total_wins+?, total_played=total_played+1 WHERE id=?")) {
                    ps.setDouble(1, m.score1);
                    ps.setInt(2, Integer.valueOf(m.player1Id).equals(m.winnerId) ? 1 : 0);
                    ps.setInt(3, m.player1Id);
                    ps.executeUpdate();
                    ps.setDouble(1, m.score2);
                    ps.setInt(2, Integer.valueOf(m.player2Id).equals(m.winnerId) ? 1 : 0);
                    ps.setInt(3, m.player2Id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}

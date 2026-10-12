package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Cấu hình qua biến môi trường: DB_URL, DB_USER, DB_PASS. */
public final class DBConnection {
    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/pricegame?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
    private static final String USER = env("DB_USER", "root");
    private static final String PASS = env("DB_PASS", "");

    private DBConnection() {}

    private static String env(String k, String def) {
        String v = System.getenv(k);
        return v == null ? def : v;
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

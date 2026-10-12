package server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public static class Product {
        public int id;
        public String name, description, image;
        public long price;
    }

    public List<Product> random(int n) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, name, description, image, price FROM product ORDER BY RAND() LIMIT ?")) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.id = rs.getInt(1);
                    p.name = rs.getString(2);
                    p.description = rs.getString(3);
                    p.image = rs.getString(4);
                    p.price = rs.getLong(5);
                    list.add(p);
                }
            }
        }
        return list;
    }
}

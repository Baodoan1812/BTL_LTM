package client.view;

import client.controller.ClientController;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RankingFrame extends JFrame {
    private static final String[] MEDALS = {"1", "2", "3"};
    private static final Color[] MEDAL_BG = {new Color(0xFEF3C7), new Color(0xE5E7EB), new Color(0xFFEDD5)};

    private static final int HISTORY_COL = 2;

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Hạng", "Người chơi", "", "Điểm", "Thắng"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final java.util.List<Integer> userIds = new java.util.ArrayList<>();

    public RankingFrame(ClientController ctl) {
        super("Bảng xếp hạng");
        JTable table = new JTable(model);
        Theme.styleTable(table);
        table.setRowHeight(34);
        table.setDefaultRenderer(Object.class, new Theme.StripedRenderer() {
            @Override protected void decorate(JLabel l, Object v, int row, int col) {
                if (row < 3) {
                    l.setBackground(MEDAL_BG[row]);
                    l.setFont(Theme.font(Font.BOLD, 15));
                    if (col == 0) l.setText(MEDALS[row]);
                }
                if (col == HISTORY_COL) {
                    l.setHorizontalAlignment(SwingConstants.CENTER);
                    l.setForeground(Color.BLUE);
                }
            }
        });
        table.getColumnModel().getColumn(HISTORY_COL).setMaxWidth(80);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0 && table.columnAtPoint(e.getPoint()) == HISTORY_COL)
                    ctl.requestHistory(userIds.get(row), (String) model.getValueAt(row, 1));
            }
        });
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                table.setCursor(Cursor.getPredefinedCursor(table.columnAtPoint(e.getPoint()) == HISTORY_COL
                        ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });

        Theme.GradientPanel root = new Theme.GradientPanel();
        root.add(Theme.label("BẢNG XẾP HẠNG", Font.BOLD, 22, Theme.YELLOW), BorderLayout.NORTH);
        ((JComponent) root.getComponent(0)).setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        Theme.Card card = new Theme.Card();
        card.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 14, 20));
        wrap.add(card);
        root.add(wrap, BorderLayout.CENTER);
        setContentPane(root);
        setSize(460, 460);
        setLocationRelativeTo(null);
    }

    public void show(JsonArray ranking) {
        model.setRowCount(0);
        userIds.clear();
        for (JsonElement el : ranking) {
            JsonObject o = el.getAsJsonObject();
            userIds.add(o.get("id").getAsInt());
            model.addRow(new Object[]{o.get("rank").getAsInt(), o.get("name").getAsString(), "Lịch sử",
                    o.get("score").getAsDouble(), o.get("wins").getAsInt()});
        }
        setVisible(true);
        toFront();
    }
}

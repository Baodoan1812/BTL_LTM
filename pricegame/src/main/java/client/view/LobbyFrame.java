package client.view;

import client.controller.ClientController;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LobbyFrame extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"Người chơi", "Điểm", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final List<Integer> ids = new ArrayList<>();
    private final List<Boolean> idle = new ArrayList<>();

    public LobbyFrame(ClientController ctl, String myName) {
        super("Sảnh chờ - " + myName);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { ctl.logout(); System.exit(0); }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(2).setCellRenderer(new Theme.StripedRenderer() {
            @Override protected void decorate(JLabel l, Object v, int row, int col) {
                boolean free = "Rỗi".equals(v);
                l.setText("● " + v);
                l.setForeground(free ? Theme.GREEN : Theme.ORANGE);
                l.setFont(Theme.font(Font.BOLD, 14));
            }
        });

        JButton challenge = new Theme.RoundButton("Thách đấu", Theme.GREEN);
        challenge.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { info("Hãy chọn một người chơi"); return; }
            if (!idle.get(row)) { info("Người này đang bận"); return; }
            ctl.challenge(ids.get(row));
        });
        JButton ranking = new Theme.RoundButton("Xếp hạng", Theme.BLUE);
        ranking.addActionListener(e -> ctl.requestRanking());
        JButton logout = new Theme.RoundButton("Đăng xuất", Theme.RED);
        logout.addActionListener(e -> ctl.logout());

        JPanel head = new JPanel(new GridLayout(2, 1));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        head.add(Theme.label("Xin chào, " + myName + "!", Font.BOLD, 22, Theme.YELLOW));
        head.add(Theme.label("Chọn một người chơi đang rỗi để thách đấu", Font.PLAIN, 13, new Color(0xE9D5FF)));

        Theme.Card card = new Theme.Card();
        card.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        card.add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        south.add(challenge); south.add(ranking); south.add(new MuteButton()); south.add(logout);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(4, 20, 0, 20));
        center.add(card);

        Theme.GradientPanel root = new Theme.GradientPanel();
        root.add(head, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(560, 520);
        setLocationRelativeTo(null);
    }

    public void updateList(JsonArray players, String myName) {
        int selectedId = table.getSelectedRow() >= 0 && table.getSelectedRow() < ids.size() ? ids.get(table.getSelectedRow()) : -1;
        model.setRowCount(0);
        ids.clear();
        idle.clear();
        for (JsonElement el : players) {
            JsonObject p = el.getAsJsonObject();
            String name = p.get("name").getAsString();
            if (name.equals(myName)) continue; // không liệt kê chính mình
            boolean isIdle = "IDLE".equals(p.get("status").getAsString());
            ids.add(p.get("id").getAsInt());
            idle.add(isIdle);
            model.addRow(new Object[]{name, p.get("score").getAsDouble(), isIdle ? "Rỗi" : "Đang chơi"});
        }
        int i = ids.indexOf(selectedId);
        if (i >= 0) table.setRowSelectionInterval(i, i);
    }

    public void info(String msg) { Popup.info(this, "Thông báo", msg); }
}

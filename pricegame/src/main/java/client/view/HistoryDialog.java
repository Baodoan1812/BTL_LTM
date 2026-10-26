package client.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Lịch sử thi đấu của một người chơi. */
public class HistoryDialog extends JDialog {
    public HistoryDialog(Window owner, String name, JsonArray history) {
        super(owner, "Lịch sử thi đấu - " + name, ModalityType.MODELESS);
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Thời gian", "Đối thủ", "Điểm", "Kết quả"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (JsonElement el : history) {
            JsonObject o = el.getAsJsonObject();
            String result = o.get("result").getAsString();
            if (!"NORMAL".equals(o.get("endReason").getAsString())) result += " (rời trận)";
            model.addRow(new Object[]{o.get("time").getAsString(), o.get("opponent").getAsString(),
                    o.get("myScore").getAsDouble() + " - " + o.get("oppScore").getAsDouble(), result});
        }
        JTable table = new JTable(model);
        Theme.styleTable(table);
        table.setRowHeight(30);
        table.setDefaultRenderer(Object.class, new Theme.StripedRenderer() {
            @Override protected void decorate(JLabel l, Object v, int row, int col) { }
        });

        Theme.GradientPanel root = new Theme.GradientPanel();
        JComponent title = Theme.label("LỊCH SỬ: " + name, Font.BOLD, 20, Theme.YELLOW);
        title.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        root.add(title, BorderLayout.NORTH);
        Theme.Card card = new Theme.Card();
        card.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        card.add(history.size() == 0 ? new JLabel("Chưa có trận nào", SwingConstants.CENTER) : sp);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 14, 20));
        wrap.add(card);
        root.add(wrap, BorderLayout.CENTER);
        setContentPane(root);
        setSize(520, 440);
        setLocationRelativeTo(owner);
    }
}

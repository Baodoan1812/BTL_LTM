package client.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/** Bảng màu, font và các thành phần giao diện dùng chung. */
public final class Theme {
    public static final Color PURPLE_DARK = new Color(0x2E1065);
    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color PINK = new Color(0xEC4899);
    public static final Color ORANGE = new Color(0xFB923C);
    public static final Color YELLOW = new Color(0xFACC15);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color RED = new Color(0xEF4444);
    public static final Color BLUE = new Color(0x3B82F6);
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color ROW_ALT = new Color(0xF5F3FF);

    private Theme() {}

    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }

    public static void useSystemLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", font(Font.BOLD, 13));
    }

    /** Nền chuyển màu tím → hồng. */
    public static class GradientPanel extends JPanel {
        public GradientPanel() { setLayout(new BorderLayout()); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, PURPLE_DARK, getWidth(), getHeight(), new Color(0xBE185D)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 20)); // vài vòng tròn trang trí
            g2.fillOval(-60, -60, 220, 220);
            g2.fillOval(getWidth() - 140, getHeight() - 120, 260, 260);
            g2.dispose();
        }
    }

    /** Thẻ trắng bo góc có đổ bóng nhẹ. */
    public static class Card extends JPanel {
        private Color fill;

        public Card() { this(Color.WHITE); }

        public Card(Color fill) {
            this.fill = fill;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 18, 16));
        }

        public void setFill(Color c) { fill = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Float(3, 5, getWidth() - 6, getHeight() - 6, 26, 26));
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 6, getHeight() - 6, 26, 26));
            g2.dispose();
        }
    }

    /** Nút bo tròn, đổi màu khi rê chuột / nhấn / vô hiệu. */
    public static class RoundButton extends JButton {
        private final Color base;
        private boolean hover;

        public RoundButton(String text, Color base) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setFont(font(Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xBDBDBD)
                    : getModel().isPressed() ? base.darker()
                    : hover ? base.brighter() : base;
            g2.setPaint(new GradientPaint(0, 0, c.brighter(), 0, getHeight(), c));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 22, 22));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width, 100), Math.max(d.height, 38));
        }
    }

    public static JLabel label(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font(style, size));
        l.setForeground(color);
        l.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6)); // tránh cắt chữ ở mép
        return l;
    }

    public static void styleField(JTextField f) {
        f.setFont(font(Font.PLAIN, 14));
        Border line = BorderFactory.createLineBorder(new Color(0xD1D5DB), 1, true);
        f.setBorder(BorderFactory.createCompoundBorder(line, BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static void styleTable(JTable t) {
        t.setFont(font(Font.PLAIN, 14));
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(0xDDD6FE));
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tb, v, false, false, r, c);
                l.setBackground(PURPLE);
                l.setForeground(Color.WHITE);
                l.setFont(font(Font.BOLD, 14));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setOpaque(true);
                return l;
            }
        });
        t.setDefaultRenderer(Object.class, new StripedRenderer());
    }

    /** Dòng xen kẽ màu, căn giữa; lớp con có thể tô màu riêng cho từng ô. */
    public static class StripedRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setForeground(TEXT);
            if (!sel) l.setBackground(r % 2 == 0 ? Color.WHITE : ROW_ALT);
            decorate(l, v, r, c);
            return l;
        }

        protected void decorate(JLabel l, Object v, int row, int col) { }
    }
}

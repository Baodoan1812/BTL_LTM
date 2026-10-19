package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Hộp thoại dùng chung thay cho JOptionPane: không viền hệ thống, bo góc, đổ bóng,
 * dải màu + icon vẽ tay theo loại thông báo, có thể kéo di chuyển.
 */
public class Popup extends JDialog {
    public enum Kind {
        INFO(Theme.BLUE), ERROR(Theme.RED), SUCCESS(Theme.GREEN), WARN(Theme.ORANGE),
        CHALLENGE(Theme.PURPLE), WIN(new Color(0xF59E0B)), LOSE(new Color(0x64748B)), DRAW(Theme.ORANGE);

        final Color color;
        Kind(Color c) { color = c; }
    }

    private static final int MARGIN = 12;   // chỗ cho bóng đổ
    private static final int ARC = 26;
    private static final int HEADER_H = 92;

    private final Kind kind;
    private final JLabel titleLabel;
    private final JLabel messageLabel;
    private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
    private boolean escapeCloses = true;

    public Popup(Window owner, boolean modal, Kind kind, String title, String message) {
        super(owner, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        this.kind = kind;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        Shell shell = new Shell();
        shell.setLayout(new BorderLayout());
        shell.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN + 6, MARGIN + 6));
        shell.add(new Header(), BorderLayout.NORTH);

        titleLabel = Theme.label(title, Font.BOLD, 22, kind.color.darker());
        messageLabel = new JLabel();
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(Theme.font(Font.PLAIN, 14));
        messageLabel.setForeground(Theme.MUTED);
        setMessage(message);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(14, 28, 6, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(titleLabel);
        body.add(Box.createVerticalStrut(8));
        body.add(messageLabel);
        shell.add(body, BorderLayout.CENTER);

        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(12, 0, 22, 0));
        shell.add(buttons, BorderLayout.SOUTH);
        setContentPane(shell);

        // kéo cửa sổ bằng chuột
        final Point[] start = {null};
        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { start[0] = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (start[0] == null) return;
                Point p = getLocation();
                setLocation(p.x + e.getX() - start[0].x, p.y + e.getY() - start[0].y);
            }
        };
        shell.addMouseListener(drag);
        shell.addMouseMotionListener(drag);

        getRootPane().registerKeyboardAction(e -> { if (escapeCloses) dispose(); },
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void setMessage(String message) {
        String html = esc(message).replace("\n", "<br>");
        messageLabel.setText("<html><div style='text-align:center;width:290px'>" + html + "</div></html>");
        if (isDisplayable()) { pack(); }
    }

    public void setTitleText(String t) { titleLabel.setText(t); }

    /** Cho phép/không cho phép phím ESC đóng hộp thoại (hộp cần người dùng trả lời thì tắt). */
    public Popup escapeCloses(boolean on) { escapeCloses = on; return this; }

    public JButton addButton(String text, Color color, Runnable action) {
        JButton b = new Theme.RoundButton(text, color);
        b.addActionListener(e -> action.run());
        buttons.add(b);
        return b;
    }

    /** Hiện hộp thoại ở giữa cửa sổ cha. */
    public void open() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---------- hộp thoại dựng sẵn ----------
    private static void notice(Window owner, Kind k, String title, String msg) {
        Popup p = new Popup(owner, true, k, title, msg);
        p.addButton("Đóng", k.color, p::dispose).requestFocusInWindow();
        p.getRootPane().setDefaultButton(null);
        p.open();
    }

    public static void info(Window owner, String title, String msg) { notice(owner, Kind.INFO, title, msg); }
    public static void error(Window owner, String title, String msg) { notice(owner, Kind.ERROR, title, msg); }
    public static void success(Window owner, String title, String msg) { notice(owner, Kind.SUCCESS, title, msg); }
    public static void warn(Window owner, String title, String msg) { notice(owner, Kind.WARN, title, msg); }

    /** @return true nếu người dùng chọn nút xác nhận */
    public static boolean confirm(Window owner, String title, String msg, String yes, String no) {
        boolean[] result = {false};
        Popup p = new Popup(owner, true, Kind.WARN, title, msg);
        p.addButton(no, new Color(0x64748B), p::dispose);
        p.addButton(yes, Theme.RED, () -> { result[0] = true; p.dispose(); });
        p.open();
        return result[0];
    }

    // ---------- vẽ ----------
    /** Thẻ trắng bo góc + bóng. */
    private static class Shell extends JPanel {
        Shell() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - MARGIN * 2 - 6, h = getHeight() - MARGIN * 2 - 6;
            for (int i = 6; i >= 1; i--) { // bóng nhiều lớp mờ dần
                g2.setColor(new Color(0, 0, 0, 9));
                g2.fill(new RoundRectangle2D.Float(MARGIN - i + 3, MARGIN - i + 7, w + i * 2, h + i * 2, ARC + i, ARC + i));
            }
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(MARGIN, MARGIN, w, h, ARC, ARC));
            g2.dispose();
        }
    }

    /** Dải màu phía trên với huy hiệu tròn chứa icon. */
    private class Header extends JComponent {
        Header() { setPreferredSize(new Dimension(400, HEADER_H)); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setClip(0, 0, w, h);
            g2.setPaint(new GradientPaint(0, 0, kind.color.brighter(), w, h, kind.color.darker()));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h + ARC, ARC, ARC)); // chỉ bo 2 góc trên
            g2.setColor(new Color(255, 255, 255, 38));
            g2.fillOval(-30, -50, 120, 120);
            g2.fillOval(w - 90, h - 50, 130, 130);

            int d = 62, cx = w / 2, cy = h / 2;
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillOval(cx - d / 2, cy - d / 2 + 3, d, d);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - d / 2, cy - d / 2, d, d);
            g2.setColor(kind.color);
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawGlyph(g2, cx, cy);
            g2.dispose();
        }

        private void drawGlyph(Graphics2D g2, int cx, int cy) {
            switch (kind) {
                case INFO -> { g2.drawLine(cx, cy - 2, cx, cy + 13); g2.fillOval(cx - 3, cy - 15, 7, 7); }
                case WARN -> { g2.drawLine(cx, cy - 14, cx, cy + 3); g2.fillOval(cx - 3, cy + 8, 7, 7); }
                case ERROR -> { g2.drawLine(cx - 11, cy - 11, cx + 11, cy + 11); g2.drawLine(cx + 11, cy - 11, cx - 11, cy + 11); }
                case SUCCESS -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(cx - 13, cy + 1); p.lineTo(cx - 4, cy + 11); p.lineTo(cx + 14, cy - 10);
                    g2.draw(p);
                }
                case CHALLENGE -> { // tia chớp
                    Path2D p = new Path2D.Float();
                    p.moveTo(cx + 4, cy - 19); p.lineTo(cx - 11, cy + 3); p.lineTo(cx - 1, cy + 3);
                    p.lineTo(cx - 5, cy + 19); p.lineTo(cx + 12, cy - 5); p.lineTo(cx + 2, cy - 5); p.closePath();
                    g2.fill(p);
                }
                case WIN -> { // ngôi sao 5 cánh
                    Path2D p = new Path2D.Float();
                    for (int i = 0; i < 10; i++) {
                        double r = i % 2 == 0 ? 20 : 8.5, a = -Math.PI / 2 + i * Math.PI / 5;
                        double x = cx + r * Math.cos(a), y = cy + 1 + r * Math.sin(a);
                        if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
                    }
                    p.closePath();
                    g2.fill(p);
                }
                case LOSE -> { // mặt buồn
                    g2.fillOval(cx - 11, cy - 11, 6, 6);
                    g2.fillOval(cx + 6, cy - 11, 6, 6);
                    g2.drawArc(cx - 11, cy + 2, 22, 18, 35, 110);
                }
                case DRAW -> { g2.drawLine(cx - 12, cy - 6, cx + 12, cy - 6); g2.drawLine(cx - 12, cy + 7, cx + 12, cy + 7); }
            }
        }
    }
}

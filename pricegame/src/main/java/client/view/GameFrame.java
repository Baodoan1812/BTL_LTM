package client.view;

import client.controller.ClientController;
import common.protocol.Message;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.text.DecimalFormat;

public class GameFrame extends JFrame {
    private static final DecimalFormat MONEY = new DecimalFormat("#,###");
    private static final Color PANEL_GLASS = new Color(255, 255, 255, 34);
    private static final Color SOFT_WHITE = new Color(0xE9D5FF);

    // ---------------- thành phần tự vẽ ----------------

    /** Thẻ người chơi: avatar chữ cái đầu, tên, điểm, ghi chú trạng thái. */
    private static class PlayerCard extends JComponent {
        private final Color accent;
        private final boolean right;
        private String name = "", score = "0", note = "";
        private Color noteColor = SOFT_WHITE;

        PlayerCard(Color accent, boolean right) {
            this.accent = accent;
            this.right = right;
            setPreferredSize(new Dimension(190, 84));
        }

        void set(String name, String score) { this.name = name; this.score = score; repaint(); }
        void setScore(String s) { score = s; repaint(); }
        void setNote(String n, Color c) { note = n; noteColor = c; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(PANEL_GLASS);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 24, 24));
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 24, 24));

            int d = 52, ax = right ? w - d - 14 : 14, ay = (h - d) / 2;
            g2.setPaint(new GradientPaint(ax, ay, accent.brighter(), ax, ay + d, accent.darker()));
            g2.fillOval(ax, ay, d, d);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(Font.BOLD, 24));
            String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, ax + (d - fm.stringWidth(initial)) / 2, ay + (d + fm.getAscent() - fm.getDescent()) / 2);

            int tx = right ? 14 : ax + d + 12, tw = w - d - 40;
            drawText(g2, name, Theme.font(Font.BOLD, 14), Color.WHITE, tx, tw, 27);
            drawText(g2, score, Theme.font(Font.BOLD, 30), Theme.YELLOW, tx, tw, 59);
            if (!note.isEmpty()) drawText(g2, note, Theme.font(Font.PLAIN, 11), noteColor, tx, tw, 76);
            g2.dispose();
        }

        private void drawText(Graphics2D g2, String s, Font f, Color c, int x, int width, int baseline) {
            g2.setFont(f);
            g2.setColor(c);
            FontMetrics fm = g2.getFontMetrics();
            while (s.length() > 1 && fm.stringWidth(s) > width) s = s.substring(0, s.length() - 2) + "…";
            int dx = right ? x + width - fm.stringWidth(s) : x; // đối thủ căn phải
            g2.drawString(s, dx, baseline);
        }
    }

    /** Chấm tiến trình các lượt: chờ / đang chơi / thắng / thua / hòa. */
    private static class RoundDots extends JComponent {
        private int total = 5, current = 0;
        private int[] state = new int[5]; // 0 chờ, 2 thắng, 3 thua, 4 hòa

        RoundDots() { setPreferredSize(new Dimension(200, 26)); }

        void reset(int total) { this.total = total; state = new int[total]; current = 0; repaint(); }
        void setCurrent(int round) { current = round; repaint(); }
        void setResult(int round, int s) { if (round >= 1 && round <= total) state[round - 1] = s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int d = 16, gap = 12, w = total * d + (total - 1) * gap;
            int x = (getWidth() - w) / 2, y = (getHeight() - d) / 2;
            for (int i = 0; i < total; i++) {
                int s = state[i];
                Color c = s == 2 ? Theme.GREEN : s == 3 ? Theme.RED : s == 4 ? Theme.ORANGE : new Color(255, 255, 255, 60);
                g2.setColor(c);
                g2.fillOval(x + i * (d + gap), y, d, d);
                if (s == 0 && i + 1 == current) {
                    g2.setColor(Theme.YELLOW);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawOval(x + i * (d + gap) - 2, y - 2, d + 4, d + 4);
                }
            }
            g2.dispose();
        }
    }

    /** Vòng đếm ngược: cung xanh → cam → đỏ theo thời gian còn lại. */
    private static class TimerRing extends JComponent {
        private int remaining, total = 15;
        private boolean active;

        TimerRing() { setPreferredSize(new Dimension(96, 96)); }

        void set(int remaining, int total, boolean active) {
            this.remaining = remaining; this.total = Math.max(1, total); this.active = active; repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            int size = Math.min(getWidth(), getHeight()) - 14;
            int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;
            g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(0xEDE9FE));
            g2.drawOval(x, y, size, size);
            Color c = remaining > 8 ? Theme.GREEN : remaining > 4 ? Theme.ORANGE : Theme.RED;
            if (active) {
                g2.setColor(c);
                g2.draw(new Arc2D.Double(x, y, size, size, 90, -360.0 * remaining / total, Arc2D.OPEN));
            }
            String s = active ? String.valueOf(remaining) : "--";
            g2.setFont(Theme.font(Font.BOLD, 28));
            g2.setColor(active ? c.darker() : Theme.MUTED);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Khung ảnh sản phẩm bo góc; thiếu ảnh thì hiện dấu "?". */
    private static class ProductImage extends JComponent {
        private Image image;

        ProductImage() { setPreferredSize(new Dimension(300, 170)); }

        void setImage(Image img) { image = img; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight();
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
            g2.setPaint(new GradientPaint(0, 0, new Color(0xEDE9FE), w, h, new Color(0xFBCFE8)));
            g2.fillRect(0, 0, w, h);
            if (image != null) {
                int iw = image.getWidth(null), ih = image.getHeight(null);
                if (iw > 0 && ih > 0) {
                    double s = Math.min((w - 20.0) / iw, (h - 20.0) / ih);
                    int dw = (int) (iw * s), dh = (int) (ih * s);
                    g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
                }
            } else {
                g2.setColor(new Color(124, 58, 237, 70));
                g2.setFont(Theme.font(Font.BOLD, 110));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("?", (w - fm.stringWidth("?")) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    // ---------------- trạng thái ----------------
    private final PlayerCard meCard = new PlayerCard(Theme.BLUE, false);
    private final PlayerCard oppCard = new PlayerCard(Theme.PINK, true);
    private final RoundDots dots = new RoundDots();
    private final JLabel roundLabel = Theme.label("", Font.BOLD, 18, Theme.YELLOW);
    private final ProductImage image = new ProductImage();
    private final JLabel name = Theme.label(" ", Font.BOLD, 20, Theme.TEXT);
    private final JLabel desc = Theme.label(" ", Font.PLAIN, 13, Theme.MUTED);
    private final TimerRing ring = new TimerRing();
    private final JTextField input = new JTextField(12);
    private final JLabel hint = Theme.label(" ", Font.PLAIN, 13, Theme.MUTED);
    private final JButton send = new Theme.RoundButton("Gửi giá", Theme.GREEN);
    private final Theme.Card resultCard = new Theme.Card(Color.WHITE);
    private final JLabel resultTitle = Theme.label(" ", Font.BOLD, 16, Theme.PURPLE);
    private final JLabel resultPrice = Theme.label(" ", Font.BOLD, 22, Theme.TEXT);
    private final JLabel resultDetail = Theme.label(" ", Font.PLAIN, 13, Theme.MUTED);
    private final Timer countdown;

    private String me = "", opponent = "";
    private int totalRounds = 5, timeLimit = 15, remaining, currentRound;
    private String lastResult = "";

    public GameFrame(ClientController ctl) {
        super("Hãy Chọn Giá Đúng");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                if (Popup.confirm(GameFrame.this, "Thoát trận?", "Nếu thoát giữa chừng, bạn sẽ bị xử thua.",
                        "Thoát trận", "Ở lại")) ctl.exitMatch();
            }
        });

        // --- bảng điểm phía trên ---
        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dots.setAlignmentX(Component.CENTER_ALIGNMENT);
        middle.add(Box.createVerticalGlue());
        middle.add(roundLabel);
        middle.add(Box.createVerticalStrut(6));
        middle.add(dots);
        middle.add(Box.createVerticalGlue());
        JPanel scoreboard = new JPanel(new BorderLayout(10, 0));
        scoreboard.setOpaque(false);
        scoreboard.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        scoreboard.add(meCard, BorderLayout.WEST);
        scoreboard.add(middle, BorderLayout.CENTER);
        scoreboard.add(oppCard, BorderLayout.EAST);

        // --- thẻ sản phẩm + ô nhập giá ---
        Theme.styleField(input);
        input.setFont(Theme.font(Font.BOLD, 20));
        input.setHorizontalAlignment(SwingConstants.RIGHT);
        input.getDocument().addDocumentListener(new DocumentListener() {
            void update() {
                String d = input.getText().replaceAll("[^0-9]", "");
                hint.setText(d.isEmpty() || d.length() > 15 ? " " : "= " + MONEY.format(Long.parseLong(d)) + " đ");
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        JLabel inputCaption = new JLabel("Giá dự đoán của bạn (VND)");
        inputCaption.setFont(Theme.font(Font.BOLD, 13));
        inputCaption.setForeground(Theme.TEXT);
        JPanel field = new JPanel(new BorderLayout(8, 0));
        field.setOpaque(false);
        field.add(input, BorderLayout.CENTER);
        field.add(send, BorderLayout.EAST);
        JPanel inputBox = new JPanel();
        inputBox.setOpaque(false);
        inputBox.setLayout(new BoxLayout(inputBox, BoxLayout.Y_AXIS));
        inputCaption.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        inputCaption.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // tránh cắt chữ ở mép
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setHorizontalAlignment(SwingConstants.LEFT);
        inputBox.add(Box.createVerticalGlue());
        inputBox.add(inputCaption);
        inputBox.add(Box.createVerticalStrut(6));
        inputBox.add(field);
        inputBox.add(Box.createVerticalStrut(4));
        inputBox.add(hint);
        inputBox.add(Box.createVerticalGlue());

        JPanel guessRow = new JPanel(new BorderLayout(16, 0));
        guessRow.setOpaque(false);
        guessRow.add(ring, BorderLayout.WEST);
        guessRow.add(inputBox, BorderLayout.CENTER);

        Theme.Card product = new Theme.Card();
        product.setLayout(new BoxLayout(product, BoxLayout.Y_AXIS));
        product.setBorder(BorderFactory.createEmptyBorder(16, 18, 20, 18));
        image.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        guessRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        guessRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        image.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)); // ảnh giãn lấp khoảng trống
        product.add(image);
        product.add(Box.createVerticalStrut(10));
        product.add(name);
        product.add(desc);
        product.add(Box.createVerticalStrut(12));
        product.add(guessRow);

        // --- thẻ kết quả lượt ---
        resultCard.setLayout(new GridLayout(3, 1, 0, 2));
        resultCard.setBorder(BorderFactory.createEmptyBorder(10, 14, 16, 14));
        resultCard.add(resultTitle);
        resultCard.add(resultPrice);
        resultCard.add(resultDetail);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(4, 20, 0, 20));
        center.add(product, BorderLayout.CENTER);
        center.add(resultCard, BorderLayout.SOUTH);

        // --- đáy ---
        JButton exit = new Theme.RoundButton("Thoát trận", new Color(0x64748B));
        exit.addActionListener(e -> dispatchEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
        bottom.add(new MuteButton());
        bottom.add(exit);

        Runnable submit = () -> {
            String digits = input.getText().replaceAll("[^0-9]", "");
            if (digits.isEmpty() || digits.length() > 15 || Long.parseLong(digits) <= 0) {
                showMessage("Giá không hợp lệ", "Hãy nhập một số tiền lớn hơn 0", Theme.RED, new Color(0xFEE2E2));
                return;
            }
            ctl.guess(Double.parseDouble(digits));
            setInputEnabled(false);
            showMessage("Đã gửi " + MONEY.format(Long.parseLong(digits)) + " đ", "Đang chờ đối thủ...", Theme.BLUE, new Color(0xDBEAFE));
        };
        send.addActionListener(e -> submit.run());
        input.addActionListener(e -> submit.run());

        Theme.GradientPanel root = new Theme.GradientPanel();
        root.add(scoreboard, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        countdown = new Timer(1000, e -> {
            if (remaining > 0) remaining--;
            ring.set(remaining, timeLimit, true);
            if (remaining > 0 && remaining <= 5 && input.isEnabled()) Sound.play(Sound.Effect.TICK); // 5 giây cuối
        });
        setMinimumSize(new Dimension(700, 780));
        setSize(720, 800);
        setLocationRelativeTo(null);
        showMessage("Sẵn sàng!", "Trận đấu sắp bắt đầu", Theme.PURPLE, Color.WHITE);
    }

    /** Thẻ thông báo dưới thẻ sản phẩm (tiêu đề, dòng giữa, nền theo trạng thái). */
    private void showMessage(String title, String line, Color color, Color bg) {
        resultCard.setFill(bg);
        resultTitle.setText(title);
        resultTitle.setForeground(color);
        resultPrice.setText(line);
        resultPrice.setForeground(Theme.TEXT);
        resultPrice.setFont(Theme.font(Font.PLAIN, 14));
        resultDetail.setText(" ");
    }

    private void setInputEnabled(boolean on) {
        input.setEnabled(on);
        send.setEnabled(on);
        if (on) input.requestFocusInWindow();
    }

    public void startMatch(String me, String opponent, int rounds) {
        this.me = me;
        this.opponent = opponent;
        this.totalRounds = rounds;
        meCard.set(me, "0");
        oppCard.set(opponent, "0");
        oppCard.setNote("", SOFT_WHITE);
        dots.reset(rounds);
        roundLabel.setText("SẴN SÀNG");
        name.setText(" ");
        desc.setText(" ");
        image.setImage(null);
        input.setText("");
        ring.set(0, timeLimit, false);
        setInputEnabled(false);
        showMessage("Trận đấu bắt đầu!", me + " vs " + opponent, Theme.PURPLE, Color.WHITE);
    }

    public void startRound(Message m) {
        currentRound = m.integer("round");
        roundLabel.setText("LƯỢT " + currentRound + " / " + totalRounds);
        dots.setCurrent(currentRound);
        name.setText(m.str("name"));
        desc.setText(wrap(m.str("desc")));
        setImage(m.str("image"));
        timeLimit = Math.max(1, m.integer("timeLimit"));
        remaining = timeLimit;
        ring.set(remaining, timeLimit, true);
        countdown.restart();
        input.setText("");
        oppCard.setNote("Đang suy nghĩ...", SOFT_WHITE);
        showMessage("Hãy đoán giá sản phẩm!", "Gần giá thật nhất sẽ được +1 điểm", Theme.PURPLE, Color.WHITE);
        setInputEnabled(true);
    }

    /** Mô tả dài: tự xuống dòng, căn giữa, rộng tối đa 560px. */
    private static String wrap(String s) {
        if (s == null || s.isBlank()) return " ";
        String safe = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><table width='560'><tr><td align='center'>" + safe + "</td></tr></table></html>";
    }

    private void setImage(String file) {
        image.setImage(null);
        if (file == null) return;
        URL url = getClass().getResource("/images/" + file);
        if (url != null) image.setImage(new ImageIcon(url).getImage()); // thiếu ảnh: giữ dấu "?"
    }

    public void opponentSubmitted() {
        oppCard.setNote("Đã gửi giá", new Color(0x6EE7B7));
    }

    public void showRoundResult(Message m) {
        countdown.stop();
        ring.set(remaining, timeLimit, false);
        setInputEnabled(false);
        boolean first = m.integer("you") == 1;
        double mine = first ? m.num("score1") : m.num("score2");
        double theirs = first ? m.num("score2") : m.num("score1");
        int w = m.integer("winner");
        String title;
        Color color, bg;
        int dotState;
        if (w == 0) { title = "Hòa lượt này (+0,5 mỗi bên)"; color = new Color(0xB45309); bg = new Color(0xFEF3C7); dotState = 4; }
        else if ((w == 1) == first) { title = "Bạn thắng lượt này! +1 điểm"; color = new Color(0x047857); bg = new Color(0xD1FAE5); dotState = 2; }
        else { title = opponent + " thắng lượt này"; color = new Color(0xB91C1C); bg = new Color(0xFEE2E2); dotState = 3; }
        dots.setResult(m.integer("round"), dotState);
        if (dotState == 2) Sound.play(Sound.Effect.CORRECT);
        else if (dotState == 3) Sound.play(Sound.Effect.WRONG);
        meCard.setScore(fmt(mine));
        oppCard.setScore(fmt(theirs));
        oppCard.setNote("", SOFT_WHITE);

        resultCard.setFill(bg);
        resultTitle.setText(title);
        resultTitle.setForeground(color);
        resultPrice.setText("Giá thật: " + MONEY.format(m.num("actual")) + " đ");
        resultPrice.setFont(Theme.font(Font.BOLD, 22));
        resultPrice.setForeground(Theme.TEXT);
        resultDetail.setText("Bạn: " + guessText(m, first ? "g1" : "g2") + "   |   " + opponent + ": " + guessText(m, first ? "g2" : "g1"));
    }

    private static String guessText(Message m, String key) {
        return m.has(key) ? MONEY.format(m.num(key)) + " đ" : "(bỏ lượt)";
    }

    private static String fmt(double d) { return d == Math.floor(d) ? String.valueOf((int) d) : String.valueOf(d); }

    public void showMatchEnd(Message m) {
        boolean first = m.integer("you") == 1;
        double mine = first ? m.num("score1") : m.num("score2");
        double theirs = first ? m.num("score2") : m.num("score1");
        String res = switch (m.str("result")) {
            case "WIN" -> "BẠN THẮNG!";
            case "LOSE" -> "BẠN THUA!";
            default -> "HÒA!";
        };
        String result = m.str("result");
        Sound.play("WIN".equals(result) ? Sound.Effect.WIN : "LOSE".equals(result) ? Sound.Effect.LOSE : Sound.Effect.CORRECT);
        lastResult = res +"\nTỉ số: " + fmt(mine) + " - " + fmt(theirs);
        roundLabel.setText("KẾT THÚC");
    }

    public String lastResultText() { return lastResult; }
}

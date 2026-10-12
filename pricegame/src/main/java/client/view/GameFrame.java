package client.view;

import client.controller.ClientController;
import common.protocol.Message;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.DecimalFormat;

public class GameFrame extends JFrame {
    private static final DecimalFormat MONEY = new DecimalFormat("#,###");

    /** Ô điểm của một người chơi. */
    private static class Badge extends Theme.Card {
        final JLabel name = Theme.label("", Font.BOLD, 14, Color.WHITE);
        final JLabel score = Theme.label("0", Font.BOLD, 30, Color.WHITE);

        Badge(Color c) {
            super(c);
            setLayout(new GridLayout(2, 1));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 14, 12));
            add(name);
            add(score);
        }
    }

    private final Badge meBadge = new Badge(Theme.BLUE);
    private final Badge oppBadge = new Badge(Theme.PINK);
    private final JLabel roundLabel = Theme.label("", Font.BOLD, 16, Theme.YELLOW);
    private final JLabel image = Theme.label("", Font.BOLD, 18, Theme.PURPLE);
    private final JLabel name = Theme.label("", Font.BOLD, 20, Theme.TEXT);
    private final JLabel desc = Theme.label(" ", Font.PLAIN, 13, Theme.MUTED);
    private final JLabel timeLabel = Theme.label("", Font.BOLD, 28, Theme.GREEN);
    private final JLabel status = Theme.label(" ", Font.PLAIN, 14, Theme.TEXT);
    private final JTextField input = new JTextField(9);
    private final JButton send = new Theme.RoundButton("Gửi", Theme.GREEN);
    private final Timer countdown;

    private String me = "", opponent = "";
    private int totalRounds = 5;
    private int remaining;
    private String lastResult = "";

    public GameFrame(ClientController ctl) {
        super("Hãy Chọn Giá Đúng");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                if (JOptionPane.showConfirmDialog(GameFrame.this, "Thoát trận? Bạn sẽ bị xử thua.", "Thoát",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) ctl.exitMatch();
            }
        });

        // bảng điểm
        JPanel scoreboard = new JPanel(new BorderLayout(10, 0));
        scoreboard.setOpaque(false);
        scoreboard.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));
        meBadge.setPreferredSize(new Dimension(130, 80));
        oppBadge.setPreferredSize(new Dimension(130, 80));
        scoreboard.add(meBadge, BorderLayout.WEST);
        scoreboard.add(roundLabel, BorderLayout.CENTER);
        scoreboard.add(oppBadge, BorderLayout.EAST);

        // thẻ sản phẩm
        image.setPreferredSize(new Dimension(300, 190));
        image.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        image.setOpaque(true);
        image.setBackground(new Color(0xF3E8FF));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);
        Theme.Card product = new Theme.Card();
        product.setLayout(new BoxLayout(product, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{image, name, desc}) c.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        product.add(image);
        product.add(Box.createVerticalStrut(8));
        product.add(name);
        product.add(desc);
        product.add(Box.createVerticalStrut(4));
        product.add(timeLabel);

        Theme.Card statusCard = new Theme.Card(new Color(0xFFFBEB));
        statusCard.setLayout(new BorderLayout());
        statusCard.setBorder(BorderFactory.createEmptyBorder(8, 12, 14, 12));
        statusCard.add(status);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 16));
        center.add(product, BorderLayout.CENTER);
        center.add(statusCard, BorderLayout.SOUTH);

        // nhập giá
        Theme.styleField(input);
        input.setFont(Theme.font(Font.BOLD, 16));
        JButton exit = new Theme.RoundButton("Thoát", Theme.RED);
        exit.addActionListener(e -> dispatchEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        bottom.setOpaque(false);
        bottom.add(Theme.label("Giá dự đoán (VND):", Font.BOLD, 14, Color.WHITE));
        bottom.add(input);
        bottom.add(send);
        bottom.add(exit);

        Runnable submit = () -> {
            String digits = input.getText().replaceAll("[^0-9]", "");
            if (digits.isEmpty() || digits.length() > 15 || Long.parseLong(digits) <= 0) {
                setStatus("Hãy nhập một số tiền hợp lệ (> 0)", Theme.RED);
                return;
            }
            ctl.guess(Double.parseDouble(digits));
            setInputEnabled(false);
            setStatus("Đã gửi " + MONEY.format(Long.parseLong(digits)) + "đ. Chờ đối thủ...", Theme.BLUE);
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
            showTime();
        });
        setSize(640, 660);
        setLocationRelativeTo(null);
    }

    private void setStatus(String text, Color color) {
        status.setForeground(color);
        status.setText("<html><div style='text-align:center'>" + text + "</div></html>");
    }

    private void showTime() {
        timeLabel.setText(remaining + "s");
        timeLabel.setForeground(remaining > 8 ? Theme.GREEN : remaining > 4 ? Theme.ORANGE : Theme.RED);
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
        meBadge.name.setText(me);
        oppBadge.name.setText(opponent);
        meBadge.score.setText("0");
        oppBadge.score.setText("0");
        roundLabel.setText("Sẵn sàng!");
        setStatus("Trận đấu bắt đầu!", Theme.PURPLE);
    }

    public void startRound(Message m) {
        roundLabel.setText("LƯỢT " + m.integer("round") + " / " + totalRounds);
        name.setText(m.str("name"));
        desc.setText(m.str("desc") == null ? " " : m.str("desc") + "  ");
        setImage(m.str("image"), m.str("name"));
        remaining = m.integer("timeLimit");
        showTime();
        countdown.restart();
        input.setText("");
        setStatus("Hãy đoán giá sản phẩm!", Theme.PURPLE);
        setInputEnabled(true);
    }

    private void setImage(String file, String productName) {
        image.setIcon(null);
        image.setText(productName); // ảnh thay thế khi thiếu file
        if (file == null) return;
        URL url = getClass().getResource("/images/" + file);
        if (url == null) return;
        ImageIcon icon = new ImageIcon(url);
        image.setIcon(new ImageIcon(icon.getImage().getScaledInstance(-1, 180, Image.SCALE_SMOOTH)));
        image.setText("");
    }

    public void opponentSubmitted() {
        if (input.isEnabled()) setStatus("Đối thủ đã gửi giá, đến lượt bạn!", Theme.ORANGE);
    }

    public void showRoundResult(Message m) {
        countdown.stop();
        setInputEnabled(false);
        boolean first = m.integer("you") == 1;
        double mine = first ? m.num("score1") : m.num("score2");
        double theirs = first ? m.num("score2") : m.num("score1");
        String myG = guessText(m, first ? "g1" : "g2");
        String opG = guessText(m, first ? "g2" : "g1");
        int w = m.integer("winner");
        Color c;
        String who;
        if (w == 0) { who = "Hòa lượt này (+0,5 mỗi bên)"; c = Theme.ORANGE; }
        else if ((w == 1) == first) { who = "Bạn thắng lượt này (+1)"; c = Theme.GREEN; }
        else { who = opponent + " thắng lượt này"; c = Theme.RED; }
        meBadge.score.setText(fmt(mine));
        oppBadge.score.setText(fmt(theirs));
        timeLabel.setText("");
        setStatus("Giá thật: <b>" + MONEY.format(m.num("actual")) + "đ</b><br>Bạn: " + myG + " — " + opponent + ": " + opG
                + "<br><b>" + who + "</b>", c.darker());
    }

    private static String guessText(Message m, String key) {
        return m.has(key) ? MONEY.format(m.num(key)) + "đ" : "(bỏ lượt)";
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
        lastResult = res + "\nTỉ số: " + fmt(mine) + " - " + fmt(theirs);
        roundLabel.setText("KẾT THÚC");
    }

    public String lastResultText() { return lastResult; }
}

package client.view;

import client.controller.ClientController;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField host = new JTextField("localhost");
    private final JTextField port = new JTextField("9999");
    private final JTextField user = new JTextField();
    private final JPasswordField pass = new JPasswordField();

    public LoginFrame(ClientController ctl) {
        super("Hãy Chọn Giá Đúng - Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Theme.GradientPanel root = new Theme.GradientPanel();
        root.setLayout(new GridBagLayout());

        JLabel logo = Theme.label("$", Font.BOLD, 56, Theme.YELLOW);
        JLabel title = Theme.label("HÃY CHỌN GIÁ ĐÚNG", Font.BOLD, 24, Theme.YELLOW);
        JLabel sub = Theme.label("Đoán giá chuẩn - Thắng đối thủ", Font.PLAIN, 13, new Color(0xE9D5FF));

        Theme.Card card = new Theme.Card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 26, 30));
        for (JTextField f : new JTextField[]{host, port, user, pass}) Theme.styleField(f);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setOpaque(false);
        String[] names = {"Server", "Cổng", "Tên đăng nhập", "Mật khẩu"};
        JComponent[] fields = {host, port, user, pass};
        for (int i = 0; i < 4; i++) {
            JLabel l = new JLabel(names[i]);
            l.setFont(Theme.font(Font.BOLD, 13));
            l.setForeground(Theme.TEXT);
            form.add(l);
            form.add(fields[i]);
        }

        JButton loginBtn = new Theme.RoundButton("Đăng nhập", Theme.PURPLE);
        JButton regBtn = new Theme.RoundButton("Đăng ký", Theme.ORANGE);
        loginBtn.addActionListener(e -> {
            if (valid()) ctl.login(host.getText().trim(), portNum(), user.getText().trim(), new String(pass.getPassword()));
        });
        regBtn.addActionListener(e -> {
            if (valid()) ctl.register(host.getText().trim(), portNum(), user.getText().trim(), new String(pass.getPassword()));
        });
        getRootPane().setDefaultButton(loginBtn);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.setOpaque(false);
        btns.add(loginBtn);
        btns.add(regBtn);

        card.add(form);
        card.add(Box.createVerticalStrut(20));
        card.add(btns);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.insets = new Insets(2, 0, 2, 0);
        root.add(logo, gc);
        root.add(title, gc);
        root.add(sub, gc);
        gc.insets = new Insets(18, 0, 0, 0);
        root.add(card, gc);

        setContentPane(root);
        setSize(440, 560);
        setLocationRelativeTo(null);
    }

    private int portNum() {
        try { return Integer.parseInt(port.getText().trim()); } catch (NumberFormatException e) { return 9999; }
    }

    private boolean valid() {
        if (user.getText().isBlank() || pass.getPassword().length == 0) {
            showError("Nhập tên đăng nhập và mật khẩu");
            return false;
        }
        return true;
    }

    public void showError(String msg) { Popup.error(this, "Có lỗi xảy ra", msg); }
    public void showInfo(String msg) { Popup.success(this, "Thành công", msg); }
}

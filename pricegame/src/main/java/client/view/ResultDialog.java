package client.view;

import client.controller.ClientController;

import javax.swing.*;
import java.awt.*;

/** Kết quả chung cuộc + hỏi chơi tiếp. */
public class ResultDialog extends JDialog {
    public ResultDialog(Frame owner, ClientController ctl, String text) {
        super(owner, "Kết thúc trận", false);
        String[] lines = text.split("\n", 2);
        String head = lines[0];
        Color headColor = head.contains("THẮNG") ? Theme.YELLOW : head.contains("THUA") ? new Color(0xFCA5A5) : Theme.ORANGE;
        String icon = head.contains("THẮNG") ? "CHÚC MỪNG" : head.contains("THUA") ? "CỐ LÊN" : "NGANG TÀI";

        Theme.GradientPanel root = new Theme.GradientPanel();
        root.setLayout(new BorderLayout(0, 6));
        root.setBorder(BorderFactory.createEmptyBorder(18, 30, 16, 30));

        JPanel info = new JPanel(new GridLayout(4, 1, 0, 2));
        info.setOpaque(false);
        info.add(Theme.label(icon, Font.BOLD, 16, Color.WHITE));
        info.add(Theme.label(head, Font.BOLD, 26, headColor));
        info.add(Theme.label(lines.length > 1 ? lines[1] : "", Font.BOLD, 16, Color.WHITE));
        JLabel ask = Theme.label("Bạn có muốn chơi tiếp không?", Font.PLAIN, 14, new Color(0xE9D5FF));
        info.add(ask);

        JButton again = new Theme.RoundButton("Chơi tiếp", Theme.GREEN);
        JButton exit = new Theme.RoundButton("Thoát", Theme.RED);
        again.addActionListener(e -> {
            ctl.playAgain(true);
            again.setEnabled(false);
            exit.setEnabled(false);
            ask.setText("Đang chờ đối thủ...");
        });
        exit.addActionListener(e -> { ctl.playAgain(false); dispose(); });
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        btns.setOpaque(false);
        btns.add(again); btns.add(exit);

        root.add(info, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        setContentPane(root);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(360, 330);
        setLocationRelativeTo(owner);
    }
}

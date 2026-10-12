package client.view;

import client.controller.ClientController;

import javax.swing.*;
import java.awt.*;

/** Hộp thoại lời mời thách đấu (OK / Reject), không chặn luồng giao diện. */
public class ChallengeDialog extends JDialog {
    public ChallengeDialog(Frame owner, ClientController ctl, String challengeId, String fromName) {
        super(owner, "Lời thách đấu", false);
        Theme.GradientPanel root = new Theme.GradientPanel();
        root.setLayout(new BorderLayout(0, 8));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 18, 30));

        JPanel text = new JPanel(new GridLayout(3, 1, 0, 4));
        text.setOpaque(false);
        text.add(Theme.label("THÁCH ĐẤU!", Font.BOLD, 20, Theme.ORANGE));
        text.add(Theme.label(fromName, Font.BOLD, 22, Theme.YELLOW));
        text.add(Theme.label("muốn thách đấu bạn!", Font.PLAIN, 15, Color.WHITE));

        JButton ok = new Theme.RoundButton("OK", Theme.GREEN);
        JButton reject = new Theme.RoundButton("Reject", Theme.RED);
        ok.addActionListener(e -> { ctl.respondChallenge(challengeId, true); dispose(); });
        reject.addActionListener(e -> { ctl.respondChallenge(challengeId, false); dispose(); });
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        btns.setOpaque(false);
        btns.add(ok); btns.add(reject);

        root.add(text, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(330, 270);
        setLocationRelativeTo(owner);
    }
}

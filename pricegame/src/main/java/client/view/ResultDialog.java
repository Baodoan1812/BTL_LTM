package client.view;

import client.controller.ClientController;

import javax.swing.*;
import java.awt.*;

/** Kết quả chung cuộc + hỏi chơi tiếp. */
public class ResultDialog extends Popup {
    public ResultDialog(Window owner, ClientController ctl, String text) {
        super(owner, false, kindOf(text), text.split("\n", 2)[0], buildMessage(text));
        escapeCloses(false);
        JButton[] btn = new JButton[2];
        btn[0] = addButton("Chơi tiếp", Theme.GREEN, () -> {
            ctl.playAgain(true);
            btn[0].setEnabled(false);
            btn[1].setEnabled(false);
            setMessage("Đang chờ đối thủ trả lời...");
        });
        btn[1] = addButton("Thoát", Theme.RED, () -> { ctl.playAgain(false); dispose(); });
        pack();
        setLocationRelativeTo(owner);
    }

    private static Kind kindOf(String text) {
        String head = text.split("\n", 2)[0];
        return head.contains("THẮNG") ? Kind.WIN : head.contains("THUA") ? Kind.LOSE : Kind.DRAW;
    }

    private static String buildMessage(String text) {
        String[] lines = text.split("\n", 2);
        return (lines.length > 1 ? lines[1] + "\n" : "") + "Bạn có muốn chơi tiếp không?";
    }
}

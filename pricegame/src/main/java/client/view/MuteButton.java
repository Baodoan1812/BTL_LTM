package client.view;

import java.awt.*;

/** Nút bật/tắt âm thanh; mọi nút tự đồng bộ trạng thái với nhau. */
public class MuteButton extends Theme.RoundButton {
    public MuteButton() {
        super("", Theme.PURPLE);
        refresh();
        addActionListener(e -> Sound.toggleMute());
        Sound.onMuteChange(() -> EventQueue.invokeLater(this::refresh));
    }

    private void refresh() {
        setText(Sound.isMuted() ? "Âm thanh: Tắt" : "Âm thanh: Bật");
    }
}

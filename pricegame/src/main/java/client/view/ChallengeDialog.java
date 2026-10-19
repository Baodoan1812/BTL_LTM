package client.view;

import client.controller.ClientController;

import java.awt.*;

/** Lời mời thách đấu (chấp nhận / từ chối), không chặn luồng giao diện. */
public class ChallengeDialog extends Popup {
    public ChallengeDialog(Window owner, ClientController ctl, String challengeId, String fromName) {
        super(owner, false, Kind.CHALLENGE, "Lời thách đấu", fromName + " muốn thách đấu bạn!\nBạn có chấp nhận không?");
        escapeCloses(false);
        addButton("Chấp nhận", Theme.GREEN, () -> { ctl.respondChallenge(challengeId, true); dispose(); });
        addButton("Từ chối", Theme.RED, () -> { ctl.respondChallenge(challengeId, false); dispose(); });
        pack();
        setLocationRelativeTo(owner);
    }
}

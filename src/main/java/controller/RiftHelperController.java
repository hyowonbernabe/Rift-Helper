package controller;

import model.LCUGet;
import model.LCUPost;
import view.RiftHelperView;

import javax.swing.*;

public class RiftHelperController {
    private RiftHelperView riftHelperView;
    private volatile boolean autoAccept;

    public RiftHelperController(RiftHelperView riftHelperView) {
        this.riftHelperView = riftHelperView;
        this.autoAccept = false;

        // Trying to implement Websocket
        LCUGet.getFromClient("/lol-champ-select/v1/session");

        this.riftHelperView.addAutoAcceptStartListener(e -> {
            autoAccept = true;

            riftHelperView.buttonAutoAcceptStart.setEnabled(false);
            riftHelperView.buttonAutoAcceptStop.setEnabled(true);

            new Thread(() -> {
                try {
                    while (autoAccept) {
                        int responseCodeAccept = LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");

                        if (responseCodeAccept == 500) {
                            System.out.println("Matchmaking in queue.");
                            Thread.sleep(3000);
                        } else {
                            System.out.println("Matchmaking not in queue.");
                            Thread.sleep(3000);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        this.riftHelperView.addAutoAcceptStopListener(e -> {
            autoAccept = false;

            SwingUtilities.invokeLater(() -> {
                riftHelperView.buttonAutoAcceptStart.setEnabled(true);
                riftHelperView.buttonAutoAcceptStop.setEnabled(false);
            });
        });
    }
}

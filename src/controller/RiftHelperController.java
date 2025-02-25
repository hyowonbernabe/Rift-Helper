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

        this.riftHelperView.addAutoAcceptStartListener(e -> {
            autoAccept = true;

            riftHelperView.buttonAutoAcceptStart.setEnabled(false);
            riftHelperView.buttonAutoAcceptStop.setEnabled(true);

            new Thread(() -> {
                try {
                    int i = 0;
                    while (autoAccept) {
                        int responseCodeAccept = LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");

                        System.out.println(responseCodeAccept + " " + i);

                        i++;
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

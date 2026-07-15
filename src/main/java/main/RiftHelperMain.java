package main;

import com.formdev.flatlaf.FlatDarkLaf;
import controller.RiftHelperMainController;
import model.LCUAuth;
import model.SSLBypass;
import view.RiftHelperMainView;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RiftHelperMain {
    public RiftHelperMain(RiftHelperMainView riftHelperMainView, RiftHelperMainController riftHelperMainController) {
    }

    public static void main(String[] args) {
        setupUiScale();
        FlatDarkLaf.setup();
        SSLBypass.disableSSLVerification();

        if (!LCUAuth.getLCUAuth()) {
            JOptionPane.showMessageDialog(null, "<html><b>League Client not found.</b><br><br>If League Client is running, it may be running with elevated privileges.<br>Try to run Rift Helper with Admin Privilege.</html>", "Client Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Port: " + LCUAuth.port);
        System.out.println("Auth Token: " + LCUAuth.token);

        RiftHelperMainView riftHelperMainView = new RiftHelperMainView();
        RiftHelperMainController riftHelperMainController = new RiftHelperMainController(riftHelperMainView);
        new RiftHelperMain(riftHelperMainView, riftHelperMainController);

        checkDisconnect();
    }

    // The window is a fixed 16:9 box at 35% of the display; scale the whole UI to match so it looks
    // right at that size and grows naturally on bigger screens. FlatLaf's uiScale scales fonts,
    // MigLayout gaps, and component insets globally; the view scales its hand-set fonts/icons/dims
    // by the same factor (UIScale.getUserScaleFactor). Must be set before FlatLaf initializes.
    // Reference: 1080p => 0.75, 1440p => 1.0, 2160p => 1.5.
    private static void setupUiScale() {
        try {
            Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
            double ratio = 16.0 / 9.0;
            double boxH = (res.width / (double) res.height >= ratio) ? res.height : res.width / ratio;
            double scale = Math.max(0.6, Math.min(boxH / 1440.0, 2.0));
            System.setProperty("flatlaf.uiScale", String.format(java.util.Locale.US, "%.3f", scale));
        } catch (Throwable t) {
            // Leave the default scale if the display can't be queried.
        }
    }

    private static void checkDisconnect() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    if (!isClientAlive()) {
                        JOptionPane.showMessageDialog(null, "<html><b>League Client Disconnected.</b></html>", "Client Not Found", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Poll the LCU port with a cheap TCP connect instead of re-running the process query every 5s.
    // When the client closes, the port stops accepting connections. This avoids spawning a
    // PowerShell process (and its console-window flash) on every poll.
    private static boolean isClientAlive() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", Integer.parseInt(LCUAuth.port)), 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

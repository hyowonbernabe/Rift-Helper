package main;

import com.formdev.flatlaf.FlatDarkLaf;
import controller.RiftHelperMainController;
import model.LCUAuth;
import model.PreferenceManager;
import model.SSLBypass;
import view.RiftHelperMainView;

import javax.swing.*;
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

    // Scale the whole UI by the user-configured factor (Settings > UI Scale, persisted). FlatLaf's
    // uiScale scales fonts, MigLayout gaps, and component insets globally; the view scales its
    // hand-set fonts/icons/dims by the same factor (UIScale.getUserScaleFactor). Must be set before
    // FlatLaf initializes, which is why changing the setting needs a restart.
    private static void setupUiScale() {
        try {
            double scale = PreferenceManager.getUiScalePercent() / 100.0;
            System.setProperty("flatlaf.uiScale", String.format(java.util.Locale.US, "%.3f", scale));
        } catch (Throwable t) {
            // Leave the default scale if the preference can't be read.
        }
    }

    /** Relaunch the app in a fresh process so a new UI scale takes effect immediately. The scale is
     *  read once at startup (before FlatLaf initializes) and baked into the components, so applying
     *  a change means starting over. Works both for `java -jar` (dev) and the jpackage exe. */
    public static void restart() {
        try {
            ProcessHandle.Info info = ProcessHandle.current().info();
            String cmd = info.command().orElse(null);
            if (cmd == null) {
                return;
            }
            java.util.List<String> command = new java.util.ArrayList<>();
            command.add(cmd);
            String lc = cmd.toLowerCase();
            if (lc.endsWith("java.exe") || lc.endsWith("javaw.exe") || lc.endsWith("java")) {
                // Dev launch: re-run this jar.
                String jar = new java.io.File(RiftHelperMain.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI()).getPath();
                command.add("-jar");
                command.add(jar);
            } else {
                // Packaged exe launcher: relaunch it with its original arguments.
                info.arguments().ifPresent(args -> java.util.Collections.addAll(command, args));
            }
            new ProcessBuilder(command).start();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
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

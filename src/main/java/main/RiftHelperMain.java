package main;

import com.formdev.flatlaf.FlatDarkLaf;
import controller.RiftHelperMainController;
import model.LCUAuth;
import model.SSLBypass;
import view.RiftHelperMainView;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RiftHelperMain {
    public RiftHelperMain(RiftHelperMainView riftHelperMainView, RiftHelperMainController riftHelperMainController) {
    }

    public static void main(String[] args) {
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

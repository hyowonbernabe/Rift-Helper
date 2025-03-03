package main;

import com.formdev.flatlaf.FlatDarkLaf;
import controller.RiftHelperMainController;
import model.LCUAuth;
import model.SSLBypass;
import view.RiftHelperMainView;

import javax.swing.*;

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
        if (LCUAuth.getLCUAuth()) {
            new Thread(() -> {
                while (true) {
                    try {
                        if (!LCUAuth.getLCUAuth()) {
                            JOptionPane.showMessageDialog(null, "<html><b>League Client Disconnected.</b><html>", "Client Not Found", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        }
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}

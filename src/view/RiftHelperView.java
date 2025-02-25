package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;

public class RiftHelperView extends JFrame {
    public JButton buttonAutoAcceptStart;
    private JPanel panelRiftHelper;
    public JButton buttonAutoAcceptStop;

    public RiftHelperView() {
        setTitle("Rift Helper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panelRiftHelper);
        panelRiftHelper.setBorder(new EmptyBorder(20, 20, 20, 20));
        setSize(300, 100);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void addAutoAcceptStartListener(ActionListener listener) {
        buttonAutoAcceptStart.addActionListener(listener);
    }

    public void addAutoAcceptStopListener(ActionListener listener) {
        buttonAutoAcceptStop.addActionListener(listener);
    }
}

package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;

public class RiftHelperView extends JFrame {
    public JButton buttonAutoAcceptStart;
    private JPanel panelRiftHelper;
    public JButton buttonAutoAcceptStop;
    private JButton buttonBench1;
    private JButton buttonBench2;
    private JButton buttonBench3;
    private JButton buttonBench4;
    private JButton buttonBench5;
    private JButton buttonBench6;
    private JButton buttonBench7;
    private JButton buttonBench8;
    private JButton buttonBench9;
    private JButton buttonBench10;
    public JPanel panelBench2;
    private JPanel panelSomething;
    public JPanel panelBench1;
    private JButton buttonAutoSwap;
    private JLabel labelMatchmaking;
    private JLabel labelAutoAccept;
    private JLabel labelARAM;

    public RiftHelperView() {
        setTitle("Rift Helper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panelRiftHelper);
        panelRiftHelper.setBorder(new EmptyBorder(20, 20, 20, 20));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panelBench2.setVisible(false);
    }

    public void setButtonBench1Text(String text) {
        buttonBench1.setText(text);
    }

    public void setButtonBench2Text(String text) {
        buttonBench2.setText(text);
    }

    public void setButtonBench3Text(String text) {
        buttonBench3.setText(text);
    }

    public void setButtonBench4Text(String text) {
        buttonBench4.setText(text);
    }

    public void setButtonBench5Text(String text) {
        buttonBench5.setText(text);
    }

    public void setButtonBench6Text(String text) {
        buttonBench6.setText(text);
    }

    public void setButtonBench7Text(String text) {
        buttonBench7.setText(text);
    }

    public void setButtonBench8Text(String text) {
        buttonBench8.setText(text);
    }

    public void setButtonBench9Text(String text) {
        buttonBench9.setText(text);
    }

    public void setButtonBench10Text(String text) {
        buttonBench10.setText(text);
    }

    public void addBench1ActionListener(ActionListener actionListener) {
        buttonBench1.addActionListener(actionListener);
    }

    public void addBench2ActionListener(ActionListener actionListener) {
        buttonBench2.addActionListener(actionListener);
    }

    public void addBench3ActionListener(ActionListener actionListener) {
        buttonBench3.addActionListener(actionListener);
    }

    public void addBench4ActionListener(ActionListener actionListener) {
        buttonBench4.addActionListener(actionListener);
    }

    public void addBench5ActionListener(ActionListener actionListener) {
        buttonBench5.addActionListener(actionListener);
    }

    public void addBench6ActionListener(ActionListener actionListener) {
        buttonBench6.addActionListener(actionListener);
    }

    public void addBench7ActionListener(ActionListener actionListener) {
        buttonBench7.addActionListener(actionListener);
    }

    public void addBench8ActionListener(ActionListener actionListener) {
        buttonBench8.addActionListener(actionListener);
    }

    public void addBench9ActionListener(ActionListener actionListener) {
        buttonBench9.addActionListener(actionListener);
    }

    public void addBench10ActionListener(ActionListener actionListener) {
        buttonBench10.addActionListener(actionListener);
    }

    public void addAutoAcceptStartListener(ActionListener listener) {
        buttonAutoAcceptStart.addActionListener(listener);
    }

    public void addAutoAcceptStopListener(ActionListener listener) {
        buttonAutoAcceptStop.addActionListener(listener);
    }

    public void addAutoSwapListener(ActionListener listener) {
        buttonAutoSwap.addActionListener(listener);
    }
}

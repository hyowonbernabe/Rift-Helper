package view;

import model.DDragonParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.util.List;

public class RiftHelperMainView extends JFrame {
    private JTabbedPane tabbedPaneRiftHelper;
    private JPanel panelRiftHelper;
    private JPanel panelLobby;
    private JPanel panelARAM;
    private JPanel panelRift;
    private JPanel panelSettings;
    private JComboBox comboBoxAutoSwapPriority1;
    private JPanel panelAutoSwap;
    private JLabel labelAutoSwap;
    private JLabel labelAutoSwapPriority1;
    private JLabel labelAutoSwapPriority2;
    private JLabel labelPriority3labelAutoSwapPriority3;
    private JLabel labelAutoSwapPriority4;
    private JLabel labelAutoSwapPriority5;
    private JLabel labelAutoSwapPriority6;
    private JLabel labelAutoSwapPriority7;
    private JLabel labelAutoSwapPriority8;
    private JLabel labelAutoSwapPriority9;
    private JLabel labelAutoSwapPriority10;
    private JComboBox comboBoxAutoSwapPriority2;
    private JComboBox comboBoxAutoSwapPriority3;
    private JComboBox comboBoxAutoSwapPriority4;
    private JComboBox comboBoxAutoSwapPriority5;
    private JComboBox comboBoxAutoSwapPriority6;
    private JComboBox comboBoxAutoSwapPriority7;
    private JComboBox comboBoxAutoSwapPriority8;
    private JComboBox comboBoxAutoSwapPriority9;
    private JComboBox comboBoxAutoSwapPriority10;
    private JPanel panelAutoSwapButtons;
    private JButton buttonAutoSwapStart;
    private JButton buttonAutoSwapStop;
    private JButton buttonAutoSwapAdd;
    private JButton buttonAutoSwapSubtract;
    private JLabel labelAutoReroll;
    private JPanel panelAutoAccept;
    private JLabel labelAutoAccept;
    public JButton buttonAutoAcceptStart;
    public JButton buttonAutoAcceptStop;
    private JLabel labelAutoDecline;
    private JButton buttonAutoDeclineStart;
    private JButton buttonAutoDeclineStop;
    private JPanel panelSave;
    private JLabel labelSaveLoad;
    private JButton buttonImport;
    private JButton buttonExport;
    private JLabel labelAutoLock;
    private JComboBox comboBoxAutoLockRankPriority1;
    private JLabel labelAutoLockRankPriority1;
    private JPanel panelAutoLock;
    private JLabel labelQuickSwitch;
    public JPanel panelQuickSwitchBench1;
    public JPanel panelQuickSwitchBench2;
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

    public RiftHelperMainView() {
        setTitle("Rift Helper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panelRiftHelper);
        panelRiftHelper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Initialize Buttons
        buttonAutoAcceptStop.setEnabled(false);
        buttonAutoDeclineStop.setEnabled(false);

        List<String> championNames = DDragonParser.fetchChampionNames();

        // Populate Auto Lock for Summoner's Rift
        populateComboBox(comboBoxAutoLockRankPriority1, championNames);

        // Populate Auto Swap for ARAM/ARURF
        populateComboBox(comboBoxAutoSwapPriority1, championNames);
        populateComboBox(comboBoxAutoSwapPriority2, championNames);
        populateComboBox(comboBoxAutoSwapPriority3, championNames);
        populateComboBox(comboBoxAutoSwapPriority4, championNames);
        populateComboBox(comboBoxAutoSwapPriority5, championNames);
        populateComboBox(comboBoxAutoSwapPriority6, championNames);
        populateComboBox(comboBoxAutoSwapPriority7, championNames);
        populateComboBox(comboBoxAutoSwapPriority8, championNames);
        populateComboBox(comboBoxAutoSwapPriority9, championNames);
        populateComboBox(comboBoxAutoSwapPriority10, championNames);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public String getComboBoxAutoSwapPriority1() {
        return comboBoxAutoSwapPriority1.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority2() {
        return comboBoxAutoSwapPriority2.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority3() {
        return comboBoxAutoSwapPriority3.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority4() {
        return comboBoxAutoSwapPriority4.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority5() {
        return comboBoxAutoSwapPriority5.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority6() {
        return comboBoxAutoSwapPriority6.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority7() {
        return comboBoxAutoSwapPriority7.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority8() {
        return comboBoxAutoSwapPriority8.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority9() {
        return comboBoxAutoSwapPriority9.getSelectedItem().toString();
    }

    public String getComboBoxAutoSwapPriority10() {
        return comboBoxAutoSwapPriority10.getSelectedItem().toString();
    }

    private void populateComboBox(JComboBox<String> comboBox, List<String> items) {
        comboBox.removeAllItems();
        comboBox.addItem(null);
        for (String item : items) {
            comboBox.addItem(item);
        }
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

    public static void main(String[] args) {
        new RiftHelperMainView();
    }
}

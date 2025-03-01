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
    private JPanel panelAutoSwap;
    private JLabel labelAutoSwap;
    public JLabel labelAutoSwapPriority1;
    public JLabel labelAutoSwapPriority2;
    public JLabel labelAutoSwapPriority3;
    public JLabel labelAutoSwapPriority4;
    public JLabel labelAutoSwapPriority5;
    public JLabel labelAutoSwapPriority6;
    public JLabel labelAutoSwapPriority7;
    public JLabel labelAutoSwapPriority8;
    public JLabel labelAutoSwapPriority9;
    public JLabel labelAutoSwapPriority10;
    public JComboBox comboBoxAutoSwapPriority1;
    public JComboBox comboBoxAutoSwapPriority2;
    public JComboBox comboBoxAutoSwapPriority3;
    public JComboBox comboBoxAutoSwapPriority4;
    public JComboBox comboBoxAutoSwapPriority5;
    public JComboBox comboBoxAutoSwapPriority6;
    public JComboBox comboBoxAutoSwapPriority7;
    public JComboBox comboBoxAutoSwapPriority8;
    public JComboBox comboBoxAutoSwapPriority9;
    public JComboBox comboBoxAutoSwapPriority10;
    private JPanel panelAutoSwapButtons;
    public JButton buttonAutoSwapEnable;
    public JButton buttonAutoSwapDisable;
    private JButton buttonAutoSwapAdd;
    private JButton buttonAutoSwapSubtract;
    private JLabel labelAutoReroll;
    private JPanel panelAutoAccept;
    private JLabel labelAutoAccept;
    public JButton buttonAutoAcceptEnable;
    public JButton buttonAutoAcceptDisable;
    private JLabel labelAutoDecline;
    private JButton buttonAutoDeclineEnable;
    private JButton buttonAutoDeclineDisable;
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
    public JButton buttonAlwaysOnTopEnable;
    public JButton buttonAlwaysOnTopDisable;
    private JLabel labelAlwaysOnTop;
    public JButton buttonCenterGUIEnable;
    public JButton buttonCenterGUIDisable;
    private JLabel labelCenterGUI;
    private JPanel panelCenterGUI;
    private JPanel panelAlwaysOnTop;
    public JButton buttonAutoRerollEnable;
    public JButton buttonAutoRerollDisable;

    public RiftHelperMainView() {
        setTitle("Rift Helper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(panelRiftHelper);
        panelRiftHelper.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Hide Elements for ARURF
        buttonAutoAcceptDisable.setEnabled(false);
        buttonAutoDeclineDisable.setEnabled(false);
        buttonAutoSwapDisable.setEnabled(false);
        panelQuickSwitchBench2.setVisible(false);
        comboBoxAutoSwapPriority6.setVisible(false);
        comboBoxAutoSwapPriority7.setVisible(false);
        comboBoxAutoSwapPriority8.setVisible(false);
        comboBoxAutoSwapPriority9.setVisible(false);
        comboBoxAutoSwapPriority10.setVisible(false);
        labelAutoSwapPriority6.setVisible(false);
        labelAutoSwapPriority7.setVisible(false);
        labelAutoSwapPriority8.setVisible(false);
        labelAutoSwapPriority9.setVisible(false);
        labelAutoSwapPriority10.setVisible(false);

        // Hide Elements for Settings
        buttonAlwaysOnTopDisable.setEnabled(false);
        buttonCenterGUIEnable.setEnabled(false);

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
        comboBox.addItem(" ");
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

    public void addAutoAcceptEnableListener(ActionListener listener) {
        buttonAutoAcceptEnable.addActionListener(listener);
    }

    public void addAutoAcceptDisableListener(ActionListener listener) {
        buttonAutoAcceptDisable.addActionListener(listener);
    }

    public void addAutoSwapEnableListener(ActionListener listener) {
        buttonAutoSwapEnable.addActionListener(listener);
    }

    public void addAutoSwapDisableListener(ActionListener listener) {
        buttonAutoSwapDisable.addActionListener(listener);
    }

    public void addAutoSwapAddListener(ActionListener listener) {
        buttonAutoSwapAdd.addActionListener(listener);
    }

    public void addAutoSwapSubtractListener(ActionListener listener) {
        buttonAutoSwapSubtract.addActionListener(listener);
    }

    public void addAlwaysOnTopEnableListener(ActionListener listener) {
        buttonAlwaysOnTopEnable.addActionListener(listener);
    }

    public void addAlwaysOnTopDisableListener(ActionListener listener) {
        buttonAlwaysOnTopDisable.addActionListener(listener);
    }

    public void addCenterGUIEnableListener(ActionListener listener) {
        buttonCenterGUIEnable.addActionListener(listener);
    }

    public void addCenterGUIDisableListener(ActionListener listener) {
        buttonCenterGUIDisable.addActionListener(listener);
    }

    public void addAutoRerollEnableListener(ActionListener listener) {
        buttonAutoRerollEnable.addActionListener(listener);
    }

    public void addAutoRerollDisableListener(ActionListener listener) {
        buttonAutoRerollDisable.addActionListener(listener);
    }

    public static void main(String[] args) {
        new RiftHelperMainView();
    }
}

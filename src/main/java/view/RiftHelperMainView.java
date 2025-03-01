package view;

import model.DDragonParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;

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
    private JButton buttonSave;
    private JButton buttonReset;
    private JButton buttonAutoSwapSave;

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

    public void setComboBoxAutoSwapPriority(String[] priorityChampions) {
        comboBoxAutoSwapPriority1.setSelectedItem(priorityChampions[0]);
        comboBoxAutoSwapPriority2.setSelectedItem(priorityChampions[1]);
        comboBoxAutoSwapPriority3.setSelectedItem(priorityChampions[2]);
        comboBoxAutoSwapPriority4.setSelectedItem(priorityChampions[3]);
        comboBoxAutoSwapPriority5.setSelectedItem(priorityChampions[4]);
        comboBoxAutoSwapPriority6.setSelectedItem(priorityChampions[5]);
        comboBoxAutoSwapPriority7.setSelectedItem(priorityChampions[6]);
        comboBoxAutoSwapPriority8.setSelectedItem(priorityChampions[7]);
        comboBoxAutoSwapPriority9.setSelectedItem(priorityChampions[8]);
        comboBoxAutoSwapPriority10.setSelectedItem(priorityChampions[9]);
    }

    public String getComboBoxAutoSwapPriority1() {
        Object selectedItem = comboBoxAutoSwapPriority1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority2() {
        Object selectedItem = comboBoxAutoSwapPriority2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority3() {
        Object selectedItem = comboBoxAutoSwapPriority3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority4() {
        Object selectedItem = comboBoxAutoSwapPriority4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority5() {
        Object selectedItem = comboBoxAutoSwapPriority5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority6() {
        Object selectedItem = comboBoxAutoSwapPriority6.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority7() {
        Object selectedItem = comboBoxAutoSwapPriority7.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority8() {
        Object selectedItem = comboBoxAutoSwapPriority8.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority9() {
        Object selectedItem = comboBoxAutoSwapPriority9.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoSwapPriority10() {
        Object selectedItem = comboBoxAutoSwapPriority10.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
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

    public void addAutoSwapSaveListener(ActionListener listener) {
        buttonAutoSwapSave.addActionListener(listener);
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

    public void addExportListener(ActionListener listener) {
        buttonExport.addActionListener(listener);
    }
}

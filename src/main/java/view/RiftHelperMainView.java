package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import model.DDragonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
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
    public JButton buttonAutoDeclineEnable;
    public JButton buttonAutoDeclineDisable;
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
    private JButton buttonReset;
    private JButton buttonAutoSwapSave;
    private JPanel panelLoot;
    private JButton buttonDisenchantChampionsSafe;
    private JLabel labelMassChampionDisenchant;
    private JButton buttonDisenchantChampionsHard;
    private JPanel panelAutoDisenchantButtons;
    private JButton buttonDisenchantSkinsSafe;
    private JButton buttonDisenchantSkinsHard;
    private JLabel labelMassSkinDisenchant;
    private JButton buttonSystemTrayEnable;
    private JButton buttonSystemTrayDisable;
    private JLabel labelSystemTray;
    private JPanel panelSystemTray;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private boolean systemTrayEnabled = false;
    private boolean said = false;

    public RiftHelperMainView() {
        setTitle("Rift Helper");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setContentPane(panelRiftHelper);
        panelRiftHelper.setBorder(new EmptyBorder(20, 20, 20, 20));
        setResizable(false);
        setWindowIcon();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                if (systemTrayEnabled) minimizeToTray();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (systemTrayEnabled) minimizeToTray();
                else System.exit(0);
            }
        });

        // Hide Elements for ARURF
        buttonAutoAcceptDisable.setEnabled(false);
        buttonAutoDeclineDisable.setEnabled(false);
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

    public void enableSystemTray() {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(this, "System tray is not supported on this system.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tray = SystemTray.getSystemTray();

        try (InputStream is = getClass().getResourceAsStream("/Kindred.png")) {
            if (is != null) {
                Image image = ImageIO.read(is);
                trayIcon = new TrayIcon(image, "Rift Helper");
                trayIcon.setImageAutoSize(true);

                PopupMenu menu = new PopupMenu();
                MenuItem openItem = new MenuItem("Open");
                MenuItem exitItem = new MenuItem("Exit");

                openItem.addActionListener(e -> restoreFromTray());
                exitItem.addActionListener(e -> System.exit(0));

                menu.add(openItem);
                menu.add(exitItem);
                trayIcon.setPopupMenu(menu);

                trayIcon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            restoreFromTray();
                        }
                    }
                });

                tray.add(trayIcon);
                systemTrayEnabled = true;

                buttonSystemTrayEnable.setEnabled(false);
                buttonSystemTrayDisable.setEnabled(true);
            } else {
                System.err.println("Tray icon not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disableSystemTray() {
        if (tray != null && trayIcon != null) {
            tray.remove(trayIcon);
        }
        systemTrayEnabled = false;

        buttonSystemTrayEnable.setEnabled(true);
        buttonSystemTrayDisable.setEnabled(false);
    }

    private void minimizeToTray() {
        if (systemTrayEnabled) {
            if (!said) {
                trayIcon.displayMessage("Rift Helper", "Minimized to system tray", TrayIcon.MessageType.INFO);
                said = true;
            }
            setVisible(false);
        }
    }

    private void restoreFromTray() {
        setVisible(true);
        setExtendedState(JFrame.NORMAL);
    }

    private void setWindowIcon() {
        try (InputStream is = getClass().getResourceAsStream("/Kindred.png")) {
            if (is != null) {
                Image icon = ImageIO.read(is);
                setIconImage(icon);
            } else {
                System.err.println("Icon not found in resources!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void addAutoDeclineEnableListener(ActionListener listener) {
        buttonAutoDeclineEnable.addActionListener(listener);
    }

    public void addAutoDeclineDisableListener(ActionListener listener) {
        buttonAutoDeclineDisable.addActionListener(listener);
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

    public void addAutoDisenchantChampionsSafeListener(ActionListener listener) {
        buttonDisenchantChampionsSafe.addActionListener(listener);
    }

    public void addAutoDisenchantChampionsHardListener(ActionListener listener) {
        buttonDisenchantChampionsHard.addActionListener(listener);
    }

    public void addAutoDisenchantSkinsSafeListener(ActionListener listener) {
        buttonDisenchantSkinsSafe.addActionListener(listener);
    }

    public void addAutoDisenchantSkinsHardListener(ActionListener listener) {
        buttonDisenchantSkinsHard.addActionListener(listener);
    }

    public void addSystemTrayEnableListener(ActionListener listener) {
        buttonSystemTrayEnable.addActionListener(listener);
    }

    public void addSystemTrayDisableListener(ActionListener listener) {
        buttonSystemTrayDisable.addActionListener(listener);
    }

    public void addExportListener(ActionListener listener) {
        buttonExport.addActionListener(listener);
    }

    public void addImportListener(ActionListener listener) {
        buttonImport.addActionListener(listener);
    }

    public void addResetListener(ActionListener listener) {
        buttonReset.addActionListener(listener);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelRiftHelper = new JPanel();
        panelRiftHelper.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper = new JTabbedPane();
        panelRiftHelper.add(tabbedPaneRiftHelper, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        panelLobby = new JPanel();
        panelLobby.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Lobby", panelLobby);
        panelAutoAccept = new JPanel();
        panelAutoAccept.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelLobby.add(panelAutoAccept, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoAcceptEnable = new JButton();
        buttonAutoAcceptEnable.setText("Enable");
        panelAutoAccept.add(buttonAutoAcceptEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoAcceptDisable = new JButton();
        buttonAutoAcceptDisable.setText("Disable");
        panelAutoAccept.add(buttonAutoAcceptDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panelLobby.add(spacer1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        labelAutoAccept = new JLabel();
        labelAutoAccept.setText("Auto Accept");
        panelLobby.add(labelAutoAccept, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoDecline = new JLabel();
        labelAutoDecline.setText("Auto Decline");
        panelLobby.add(labelAutoDecline, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelLobby.add(panel1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoDeclineEnable = new JButton();
        buttonAutoDeclineEnable.setText("Enable");
        panel1.add(buttonAutoDeclineEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoDeclineDisable = new JButton();
        buttonAutoDeclineDisable.setText("Disable");
        panel1.add(buttonAutoDeclineDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelRift = new JPanel();
        panelRift.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Summoner's Rift", panelRift);
        panelAutoLock = new JPanel();
        panelAutoLock.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelRift.add(panelAutoLock, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelAutoLockRankPriority1 = new JLabel();
        labelAutoLockRankPriority1.setText("1");
        panelAutoLock.add(labelAutoLockRankPriority1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoLockRankPriority1 = new JComboBox();
        panelAutoLock.add(comboBoxAutoLockRankPriority1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelRift.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        labelAutoLock = new JLabel();
        labelAutoLock.setText("Auto Lock");
        panelRift.add(labelAutoLock, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelARAM = new JPanel();
        panelARAM.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("ARAM/ARURF", panelARAM);
        panelAutoSwap = new JPanel();
        panelAutoSwap.setLayout(new GridLayoutManager(12, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelARAM.add(panelAutoSwap, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelAutoSwapPriority1 = new JLabel();
        labelAutoSwapPriority1.setText("1");
        panelAutoSwap.add(labelAutoSwapPriority1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority1 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority2 = new JLabel();
        labelAutoSwapPriority2.setText("2");
        panelAutoSwap.add(labelAutoSwapPriority2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority3 = new JLabel();
        labelAutoSwapPriority3.setText("3");
        panelAutoSwap.add(labelAutoSwapPriority3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority4 = new JLabel();
        labelAutoSwapPriority4.setText("4");
        panelAutoSwap.add(labelAutoSwapPriority4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority5 = new JLabel();
        labelAutoSwapPriority5.setText("5");
        panelAutoSwap.add(labelAutoSwapPriority5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority6 = new JLabel();
        labelAutoSwapPriority6.setText("6");
        panelAutoSwap.add(labelAutoSwapPriority6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority7 = new JLabel();
        labelAutoSwapPriority7.setText("7");
        panelAutoSwap.add(labelAutoSwapPriority7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority8 = new JLabel();
        labelAutoSwapPriority8.setText("8");
        panelAutoSwap.add(labelAutoSwapPriority8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority9 = new JLabel();
        labelAutoSwapPriority9.setText("9");
        panelAutoSwap.add(labelAutoSwapPriority9, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoSwapPriority10 = new JLabel();
        labelAutoSwapPriority10.setText("10");
        panelAutoSwap.add(labelAutoSwapPriority10, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority2 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority3 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority4 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority5 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority5, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority6 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority6, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority7 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority7, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority8 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority8, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority9 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority9, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoSwapPriority10 = new JComboBox();
        panelAutoSwap.add(comboBoxAutoSwapPriority10, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoSwapButtons = new JPanel();
        panelAutoSwapButtons.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelAutoSwap.add(panelAutoSwapButtons, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoSwapEnable = new JButton();
        buttonAutoSwapEnable.setText("Enable");
        panelAutoSwapButtons.add(buttonAutoSwapEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoSwapDisable = new JButton();
        buttonAutoSwapDisable.setText("Disable");
        panelAutoSwapButtons.add(buttonAutoSwapDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoSwapAdd = new JButton();
        buttonAutoSwapAdd.setText("+");
        panelAutoSwapButtons.add(buttonAutoSwapAdd, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoSwapSubtract = new JButton();
        buttonAutoSwapSubtract.setText("-");
        panelAutoSwapButtons.add(buttonAutoSwapSubtract, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoSwapSave = new JButton();
        buttonAutoSwapSave.setText("Save");
        panelAutoSwap.add(buttonAutoSwapSave, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panelARAM.add(spacer3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        labelAutoSwap = new JLabel();
        labelAutoSwap.setText("Auto Swap");
        panelARAM.add(labelAutoSwap, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoReroll = new JLabel();
        labelAutoReroll.setText("Auto Reroll");
        panelARAM.add(labelAutoReroll, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelQuickSwitchBench1 = new JPanel();
        panelQuickSwitchBench1.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panelARAM.add(panelQuickSwitchBench1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonBench1 = new JButton();
        buttonBench1.setText("");
        panelQuickSwitchBench1.add(buttonBench1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench2 = new JButton();
        buttonBench2.setText("");
        panelQuickSwitchBench1.add(buttonBench2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench3 = new JButton();
        buttonBench3.setText("");
        panelQuickSwitchBench1.add(buttonBench3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench4 = new JButton();
        buttonBench4.setText("");
        panelQuickSwitchBench1.add(buttonBench4, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench5 = new JButton();
        buttonBench5.setText("");
        panelQuickSwitchBench1.add(buttonBench5, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        panelQuickSwitchBench2 = new JPanel();
        panelQuickSwitchBench2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panelARAM.add(panelQuickSwitchBench2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonBench6 = new JButton();
        buttonBench6.setText("");
        panelQuickSwitchBench2.add(buttonBench6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench7 = new JButton();
        buttonBench7.setText("");
        panelQuickSwitchBench2.add(buttonBench7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench8 = new JButton();
        buttonBench8.setText("");
        panelQuickSwitchBench2.add(buttonBench8, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench9 = new JButton();
        buttonBench9.setText("");
        panelQuickSwitchBench2.add(buttonBench9, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        buttonBench10 = new JButton();
        buttonBench10.setText("");
        panelQuickSwitchBench2.add(buttonBench10, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 25), new Dimension(100, 25), new Dimension(100, 25), 0, false));
        labelQuickSwitch = new JLabel();
        labelQuickSwitch.setText("Quick Switch");
        panelARAM.add(labelQuickSwitch, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelARAM.add(panel2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoRerollEnable = new JButton();
        buttonAutoRerollEnable.setText("Enable");
        panel2.add(buttonAutoRerollEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoRerollDisable = new JButton();
        buttonAutoRerollDisable.setText("Disable");
        panel2.add(buttonAutoRerollDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelLoot = new JPanel();
        panelLoot.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Loot", panelLoot);
        labelMassChampionDisenchant = new JLabel();
        labelMassChampionDisenchant.setText("Mass Champion Disenchant");
        panelLoot.add(labelMassChampionDisenchant, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panelLoot.add(spacer4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelAutoDisenchantButtons = new JPanel();
        panelAutoDisenchantButtons.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelLoot.add(panelAutoDisenchantButtons, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonDisenchantChampionsSafe = new JButton();
        buttonDisenchantChampionsSafe.setText("Safe Mode (Disenchant Already Owned Champions)");
        panelAutoDisenchantButtons.add(buttonDisenchantChampionsSafe, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonDisenchantChampionsHard = new JButton();
        buttonDisenchantChampionsHard.setText("Hard Mode (Disenchant Everything)");
        panelAutoDisenchantButtons.add(buttonDisenchantChampionsHard, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelMassSkinDisenchant = new JLabel();
        labelMassSkinDisenchant.setText("Mass Skin Disenchant");
        panelAutoDisenchantButtons.add(labelMassSkinDisenchant, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelAutoDisenchantButtons.add(panel3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonDisenchantSkinsSafe = new JButton();
        buttonDisenchantSkinsSafe.setText("Safe Mode (Disenchant Already Owned Skins)");
        panel3.add(buttonDisenchantSkinsSafe, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonDisenchantSkinsHard = new JButton();
        buttonDisenchantSkinsHard.setText("Hard Mode (Disenchant Everything)");
        panel3.add(buttonDisenchantSkinsHard, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelSettings = new JPanel();
        panelSettings.setLayout(new GridLayoutManager(10, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Settings", panelSettings);
        panelSave = new JPanel();
        panelSave.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelSettings.add(panelSave, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonImport = new JButton();
        buttonImport.setText("Import");
        panelSave.add(buttonImport, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonExport = new JButton();
        buttonExport.setText("Export");
        panelSave.add(buttonExport, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panelSettings.add(spacer5, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        labelSaveLoad = new JLabel();
        labelSaveLoad.setText("Save/Load");
        panelSettings.add(labelSaveLoad, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAlwaysOnTop = new JLabel();
        labelAlwaysOnTop.setText("Always On Top");
        panelSettings.add(labelAlwaysOnTop, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAlwaysOnTop = new JPanel();
        panelAlwaysOnTop.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelSettings.add(panelAlwaysOnTop, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAlwaysOnTopEnable = new JButton();
        buttonAlwaysOnTopEnable.setText("Enable");
        panelAlwaysOnTop.add(buttonAlwaysOnTopEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAlwaysOnTopDisable = new JButton();
        buttonAlwaysOnTopDisable.setText("Disable");
        panelAlwaysOnTop.add(buttonAlwaysOnTopDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelCenterGUI = new JLabel();
        labelCenterGUI.setText("Center GUI on Update");
        panelSettings.add(labelCenterGUI, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelCenterGUI = new JPanel();
        panelCenterGUI.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelSettings.add(panelCenterGUI, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonCenterGUIEnable = new JButton();
        buttonCenterGUIEnable.setText("Enable");
        panelCenterGUI.add(buttonCenterGUIEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCenterGUIDisable = new JButton();
        buttonCenterGUIDisable.setText("Disable");
        panelCenterGUI.add(buttonCenterGUIDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonReset = new JButton();
        buttonReset.setText("Reset");
        panelSettings.add(buttonReset, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelSystemTray = new JLabel();
        labelSystemTray.setText("Hide in System Tray");
        panelSettings.add(labelSystemTray, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelSystemTray = new JPanel();
        panelSystemTray.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelSettings.add(panelSystemTray, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonSystemTrayEnable = new JButton();
        buttonSystemTrayEnable.setText("Enable");
        panelSystemTray.add(buttonSystemTrayEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonSystemTrayDisable = new JButton();
        buttonSystemTrayDisable.setText("Disable");
        panelSystemTray.add(buttonSystemTrayDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelRiftHelper;
    }

}

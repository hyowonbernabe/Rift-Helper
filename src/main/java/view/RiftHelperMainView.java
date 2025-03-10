package view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import model.DDragonParser;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    private JButton buttonSupport;
    private JButton buttonTop;
    private JButton buttonBot;
    private JButton buttonJungle;
    private JButton buttonMid;
    private JComboBox comboBoxTop1;
    private JComboBox comboBoxTop2;
    private JComboBox comboBoxTop3;
    private JComboBox comboBoxTop4;
    private JComboBox comboBoxTop5;
    private JComboBox comboBoxJungle1;
    private JComboBox comboBoxJungle2;
    private JComboBox comboBoxJungle3;
    private JComboBox comboBoxJungle4;
    private JComboBox comboBoxJungle5;
    private JComboBox comboBoxMid1;
    private JComboBox comboBoxMid2;
    private JComboBox comboBoxMid3;
    private JComboBox comboBoxMid4;
    private JComboBox comboBoxMid5;
    private JComboBox comboBoxBot1;
    private JComboBox comboBoxBot2;
    private JComboBox comboBoxBot3;
    private JComboBox comboBoxBot4;
    private JComboBox comboBoxBot5;
    private JComboBox comboBoxSupport1;
    private JComboBox comboBoxSupport2;
    private JComboBox comboBoxSupport3;
    private JComboBox comboBoxSupport4;
    private JComboBox comboBoxSupport5;
    private JLabel labelTop1;
    private JLabel labelTop2;
    private JLabel labelTop3;
    private JLabel labelTop4;
    private JLabel labelTop5;
    private JLabel labelJungle1;
    private JLabel labelJungle2;
    private JLabel labelJungle3;
    private JLabel labelJungle4;
    private JLabel labelJungle5;
    private JLabel labelMid1;
    private JLabel labelMid2;
    private JLabel labelMid3;
    private JLabel labelMid4;
    private JLabel labelMid5;
    private JLabel labelBot1;
    private JLabel labelBot2;
    private JLabel labelBot3;
    private JLabel labelBot4;
    private JLabel labelBot5;
    private JLabel labelSupport1;
    private JLabel labelSupport2;
    private JLabel labelSupport3;
    private JLabel labelSupport4;
    private JLabel labelSupport5;
    private JButton buttonTest;
    private JPanel panelAutoLockButtons;
    private JButton buttonAutoLockSave;
    public JButton buttonAutoLockEnable;
    public JButton buttonAutoLockDisable;
    private JComboBox comboBoxAutoBan1;
    private JComboBox comboBoxAutoBan2;
    private JComboBox comboBoxAutoBan3;
    private JComboBox comboBoxAutoBan4;
    private JComboBox comboBoxAutoBan5;
    private JLabel labelAutoBan1;
    private JLabel labelAutoBan2;
    private JLabel labelAutoBan3;
    private JLabel labelAutoBan4;
    private JLabel labelAutoBan5;
    private JLabel labelAutoBan;
    private JButton buttonAutoBanSave;
    public JButton buttonAutoBanEnable;
    public JButton buttonAutoBanDisable;
    private JComboBox comboBoxAutoLockArena1;
    private JComboBox comboBoxAutoLockArena2;
    private JComboBox comboBoxAutoLockArena3;
    private JComboBox comboBoxAutoLockArena4;
    private JComboBox comboBoxAutoLockArena5;
    private JButton buttonAutoLockArenaSave;
    public JButton buttonAutoLockArenaEnable;
    public JButton buttonAutoLockArenaDisable;
    private JComboBox comboBoxAutoBanArena1;
    private JComboBox comboBoxAutoBanArena2;
    private JComboBox comboBoxAutoBanArena3;
    private JComboBox comboBoxAutoBanArena4;
    private JComboBox comboBoxAutoBanArena5;
    public JButton buttonAutoBanArenaEnable;
    public JButton buttonAutoBanArenaDisable;
    private JButton buttonAutoBanArenaSave;
    private JLabel labelAutoLockArena;
    private JLabel labelAutoLockArena1;
    private JLabel labelAutoLockArena2;
    private JLabel labelAutoLockArena3;
    private JLabel labelAutoLockArena4;
    private JLabel labelAutoLockArena5;
    private JPanel panelArena;
    private JPanel panelAutoLockArena;
    private JPanel panelAutoLockArenaButtons;
    private JLabel labelAutoBanArena;
    private JLabel labelAutoBanArena1;
    private JLabel labelAutoBanArena2;
    private JLabel labelAutoBanArena3;
    private JLabel labelAutoBanArena4;
    private JLabel labelAutoBanArena5;
    private JPanel panelAutoBanArenaButtons;
    public JButton buttonAutoBraveryArenaEnable;
    public JButton buttonAutoBraveryArenaDisable;
    private JLabel labelAutoBraveryArena;
    public JButton buttonAutoCheckUpdateEnable;
    public JButton buttonAutoCheckUpdateDisable;
    private JPanel panelTest;
    private JLabel labelAutoCheckUpdate;
    public JButton buttonAutoBanCrowdFavoriteEnable;
    public JButton buttonAutoBanCrowdFavoriteDisable;
    private JLabel labelAutoBanCrowdFavorite;
    private JPanel panelAutoBanArena;
    private JPanel panelAutoBravery;
    private JPanel panelAutoBanCrowdFavorite;
    private JPanel panelInfo;
    private JPanel panelIcon;
    private JPanel panelAutoCheckUpdate;
    private JPanel panelAutoRerollButtons;
    private JPanel panelAutoBan;
    private JPanel panelAutoLock;
    private JPanel panelAutoLockLanes;
    private JPanel panelAutoDecline;
    private JLabel labelIcon;
    private JLabel labelTitle;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private boolean systemTrayEnabled = false;
    private boolean said = false;

    public RiftHelperMainView() {
        $$$setupUI$$$();
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

        try (InputStream is = getClass().getResourceAsStream("/Kindred.png")) {
            if (is != null) {
                Image icon = ImageIO.read(is);
                Image scaledImg = icon.getScaledInstance(384, 384, Image.SCALE_SMOOTH);
                labelIcon.setIcon(new ImageIcon(scaledImg));
            } else {
                System.err.println("Icon not found in resources!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Hide Elements
        panelQuickSwitchBench2.setVisible(false);
        buttonAutoAcceptDisable.setEnabled(false);
        buttonAutoDeclineDisable.setEnabled(false);
        buttonAutoLockDisable.setEnabled(false);
        buttonAutoBanDisable.setEnabled(false);
        buttonAutoSwapDisable.setEnabled(false);
        buttonAutoRerollDisable.setEnabled(false);
        buttonAutoBraveryArenaDisable.setEnabled(false);
        buttonAutoLockArenaDisable.setEnabled(false);
        buttonAutoBanArenaDisable.setEnabled(false);
        buttonAutoBanCrowdFavoriteDisable.setEnabled(false);

        List<String> championNames = DDragonParser.fetchChampionNames();

        // Populate Combo Boxes
        for (JComboBox jComboBox : Arrays.asList(
                comboBoxAutoLockArena1, comboBoxAutoLockArena2, comboBoxAutoLockArena3, comboBoxAutoLockArena4, comboBoxAutoLockArena5,
                comboBoxAutoBanArena1, comboBoxAutoBanArena2, comboBoxAutoBanArena3, comboBoxAutoBanArena4, comboBoxAutoBanArena5,
                comboBoxAutoBan1, comboBoxAutoBan2, comboBoxAutoBan3, comboBoxAutoBan4, comboBoxAutoBan5,
                comboBoxTop1, comboBoxTop2, comboBoxTop3, comboBoxTop4, comboBoxTop5,
                comboBoxJungle1, comboBoxJungle2, comboBoxJungle3, comboBoxJungle4, comboBoxJungle5,
                comboBoxMid1, comboBoxMid2, comboBoxMid3, comboBoxMid4, comboBoxMid5,
                comboBoxBot1, comboBoxBot2, comboBoxBot3, comboBoxBot4, comboBoxBot5,
                comboBoxSupport1, comboBoxSupport2, comboBoxSupport3, comboBoxSupport4, comboBoxSupport5,
                comboBoxAutoSwapPriority1, comboBoxAutoSwapPriority2, comboBoxAutoSwapPriority3, comboBoxAutoSwapPriority4, comboBoxAutoSwapPriority5,
                comboBoxAutoSwapPriority6, comboBoxAutoSwapPriority7, comboBoxAutoSwapPriority8, comboBoxAutoSwapPriority9, comboBoxAutoSwapPriority10)) {
            populateComboBox(jComboBox, championNames);
        }

        pack();
        setLocationRelativeTo(null);
        setVisible(false);
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

    public JLabel[] getAutoSwapPriorityLabels() {
        return new JLabel[]{
                labelAutoSwapPriority1, labelAutoSwapPriority2,
                labelAutoSwapPriority3, labelAutoSwapPriority4,
                labelAutoSwapPriority5, labelAutoSwapPriority6,
                labelAutoSwapPriority7, labelAutoSwapPriority8,
                labelAutoSwapPriority9, labelAutoSwapPriority10
        };
    }

    public JComboBox[] getAutoSwapPriorityComboBoxes() {
        return new JComboBox[]{
                comboBoxAutoSwapPriority1, comboBoxAutoSwapPriority2,
                comboBoxAutoSwapPriority3, comboBoxAutoSwapPriority4,
                comboBoxAutoSwapPriority5, comboBoxAutoSwapPriority6,
                comboBoxAutoSwapPriority7, comboBoxAutoSwapPriority8,
                comboBoxAutoSwapPriority9, comboBoxAutoSwapPriority10
        };
    }
    
    public void setComboBoxAutoLockArenaPriority(String[] priorityChampions) {
        comboBoxAutoLockArena1.setSelectedItem(priorityChampions[0]);
        comboBoxAutoLockArena2.setSelectedItem(priorityChampions[1]);
        comboBoxAutoLockArena3.setSelectedItem(priorityChampions[2]);
        comboBoxAutoLockArena4.setSelectedItem(priorityChampions[3]);
        comboBoxAutoLockArena5.setSelectedItem(priorityChampions[4]);
    }

    public String getComboBoxAutoLockArenaPriority1() {
        Object selectedItem = comboBoxAutoLockArena1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoLockArenaPriority2() {
        Object selectedItem = comboBoxAutoLockArena2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoLockArenaPriority3() {
        Object selectedItem = comboBoxAutoLockArena3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoLockArenaPriority4() {
        Object selectedItem = comboBoxAutoLockArena4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoLockArenaPriority5() {
        Object selectedItem = comboBoxAutoLockArena5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }
    
    public void setComboBoxAutoBanArenaPriority(String[] priorityChampions) {
        comboBoxAutoBanArena1.setSelectedItem(priorityChampions[0]);
        comboBoxAutoBanArena2.setSelectedItem(priorityChampions[1]);
        comboBoxAutoBanArena3.setSelectedItem(priorityChampions[2]);
        comboBoxAutoBanArena4.setSelectedItem(priorityChampions[3]);
        comboBoxAutoBanArena5.setSelectedItem(priorityChampions[4]);
    }

    public String getComboBoxAutoBanArenaPriority1() {
        Object selectedItem = comboBoxAutoBanArena1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBanArenaPriority2() {
        Object selectedItem = comboBoxAutoBanArena2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBanArenaPriority3() {
        Object selectedItem = comboBoxAutoBanArena3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBanArenaPriority4() {
        Object selectedItem = comboBoxAutoBanArena4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBanArenaPriority5() {
        Object selectedItem = comboBoxAutoBanArena5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public void setComboBoxTopPriority(String[] priorityChampions) {
        comboBoxTop1.setSelectedItem(priorityChampions[0]);
        comboBoxTop2.setSelectedItem(priorityChampions[1]);
        comboBoxTop3.setSelectedItem(priorityChampions[2]);
        comboBoxTop4.setSelectedItem(priorityChampions[3]);
        comboBoxTop5.setSelectedItem(priorityChampions[4]);
    }

    public void setComboBoxJunglePriority(String[] priorityChampions) {
        comboBoxJungle1.setSelectedItem(priorityChampions[0]);
        comboBoxJungle2.setSelectedItem(priorityChampions[1]);
        comboBoxJungle3.setSelectedItem(priorityChampions[2]);
        comboBoxJungle4.setSelectedItem(priorityChampions[3]);
        comboBoxJungle5.setSelectedItem(priorityChampions[4]);
    }

    public void setComboBoxMidPriority(String[] priorityChampions) {
        comboBoxMid1.setSelectedItem(priorityChampions[0]);
        comboBoxMid2.setSelectedItem(priorityChampions[1]);
        comboBoxMid3.setSelectedItem(priorityChampions[2]);
        comboBoxMid4.setSelectedItem(priorityChampions[3]);
        comboBoxMid5.setSelectedItem(priorityChampions[4]);
    }

    public void setComboBoxBotPriority(String[] priorityChampions) {
        comboBoxBot1.setSelectedItem(priorityChampions[0]);
        comboBoxBot2.setSelectedItem(priorityChampions[1]);
        comboBoxBot3.setSelectedItem(priorityChampions[2]);
        comboBoxBot4.setSelectedItem(priorityChampions[3]);
        comboBoxBot5.setSelectedItem(priorityChampions[4]);
    }

    public void setComboBoxSupportPriority(String[] priorityChampions) {
        comboBoxSupport1.setSelectedItem(priorityChampions[0]);
        comboBoxSupport2.setSelectedItem(priorityChampions[1]);
        comboBoxSupport3.setSelectedItem(priorityChampions[2]);
        comboBoxSupport4.setSelectedItem(priorityChampions[3]);
        comboBoxSupport5.setSelectedItem(priorityChampions[4]);
    }

    public void setComboBoxAutoBanPriority(String[] priorityChampions) {
        comboBoxAutoBan1.setSelectedItem(priorityChampions[0]);
        comboBoxAutoBan2.setSelectedItem(priorityChampions[1]);
        comboBoxAutoBan3.setSelectedItem(priorityChampions[2]);
        comboBoxAutoBan4.setSelectedItem(priorityChampions[3]);
        comboBoxAutoBan5.setSelectedItem(priorityChampions[4]);
    }

    public String getComboBoxTop1() {
        Object selectedItem = comboBoxTop1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxTop2() {
        Object selectedItem = comboBoxTop2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxTop3() {
        Object selectedItem = comboBoxTop3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxTop4() {
        Object selectedItem = comboBoxTop4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxTop5() {
        Object selectedItem = comboBoxTop5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxJungle1() {
        Object selectedItem = comboBoxJungle1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxJungle2() {
        Object selectedItem = comboBoxJungle2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxJungle3() {
        Object selectedItem = comboBoxJungle3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxJungle4() {
        Object selectedItem = comboBoxJungle4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxJungle5() {
        Object selectedItem = comboBoxJungle5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxMid1() {
        Object selectedItem = comboBoxMid1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxMid2() {
        Object selectedItem = comboBoxMid2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxMid3() {
        Object selectedItem = comboBoxMid3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxMid4() {
        Object selectedItem = comboBoxMid4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxMid5() {
        Object selectedItem = comboBoxMid5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxBot1() {
        Object selectedItem = comboBoxBot1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxBot2() {
        Object selectedItem = comboBoxBot2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxBot3() {
        Object selectedItem = comboBoxBot3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxBot4() {
        Object selectedItem = comboBoxBot4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxBot5() {
        Object selectedItem = comboBoxBot5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxSupport1() {
        Object selectedItem = comboBoxSupport1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxSupport2() {
        Object selectedItem = comboBoxSupport2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxSupport3() {
        Object selectedItem = comboBoxSupport3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxSupport4() {
        Object selectedItem = comboBoxSupport4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxSupport5() {
        Object selectedItem = comboBoxSupport5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBan1() {
        Object selectedItem = comboBoxAutoBan1.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBan2() {
        Object selectedItem = comboBoxAutoBan2.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBan3() {
        Object selectedItem = comboBoxAutoBan3.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBan4() {
        Object selectedItem = comboBoxAutoBan4.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
    }

    public String getComboBoxAutoBan5() {
        Object selectedItem = comboBoxAutoBan5.getSelectedItem();
        return (selectedItem != null) ? selectedItem.toString() : null;
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
        comboBox.addItem("");
        for (String item : items) {
            comboBox.addItem(item);
        }
    }

    public JLabel[] getTopLabels() {
        return new JLabel[]{
                labelTop1, labelTop2, labelTop3, labelTop4, labelTop5
        };
    }

    public JComboBox[] getTopComboBoxes() {
        return new JComboBox[]{
                comboBoxTop1, comboBoxTop2, comboBoxTop3, comboBoxTop4, comboBoxTop5
        };
    }

    public void showTop() {
        JLabel[] labels = getTopLabels();

        JComboBox[] comboBoxes = getTopComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(true);
        }

        for (JLabel label : labels) {
            label.setVisible(true);
        }

        buttonTop.setEnabled(false);
    }

    public void hideTop() {
        JLabel[] labels = getTopLabels();
        JComboBox[] comboBoxes = getTopComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(false);
        }
        for (JLabel label : labels) {
            label.setVisible(false);
        }

        buttonTop.setEnabled(true);
    }

    public JLabel[] getJungleLabels() {
        return new JLabel[]{
                labelJungle1, labelJungle2, labelJungle3, labelJungle4, labelJungle5
        };
    }

    public JComboBox[] getJungleComboBoxes() {
        return new JComboBox[]{
                comboBoxJungle1, comboBoxJungle2, comboBoxJungle3, comboBoxJungle4, comboBoxJungle5
        };
    }

    public void showJungle() {
        JLabel[] labels = getJungleLabels();
        JComboBox[] comboBoxes = getJungleComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(true);
        }
        for (JLabel label : labels) {
            label.setVisible(true);
        }

        buttonJungle.setEnabled(false);
    }

    public void hideJungle() {
        JLabel[] labels = getJungleLabels();
        JComboBox[] comboBoxes = getJungleComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(false);
        }
        for (JLabel label : labels) {
            label.setVisible(false);
        }

        buttonJungle.setEnabled(true);
    }

    public JLabel[] getMidLabels() {
        return new JLabel[]{
                labelMid1, labelMid2, labelMid3, labelMid4, labelMid5
        };
    }

    public JComboBox[] getMidComboBoxes() {
        return new JComboBox[]{
                comboBoxMid1, comboBoxMid2, comboBoxMid3, comboBoxMid4, comboBoxMid5
        };
    }

    public void showMid() {
        JLabel[] labels = getMidLabels();
        JComboBox[] comboBoxes = getMidComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(true);
        }
        for (JLabel label : labels) {
            label.setVisible(true);
        }

        buttonMid.setEnabled(false);
    }

    public void hideMid() {
        JLabel[] labels = getMidLabels();
        JComboBox[] comboBoxes = getMidComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(false);
        }
        for (JLabel label : labels) {
            label.setVisible(false);
        }

        buttonMid.setEnabled(true);
    }

    public JLabel[] getBotLabels() {
        return new JLabel[]{
                labelBot1, labelBot2, labelBot3, labelBot4, labelBot5
        };
    }

    public JComboBox[] getBotComboBoxes() {
        return new JComboBox[]{
                comboBoxBot1, comboBoxBot2, comboBoxBot3, comboBoxBot4, comboBoxBot5
        };
    }

    public void showBot() {
        JLabel[] labels = getBotLabels();
        JComboBox[] comboBoxes = getBotComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(true);
        }
        for (JLabel label : labels) {
            label.setVisible(true);
        }

        buttonBot.setEnabled(false);
    }

    public void hideBot() {
        JLabel[] labels = getBotLabels();
        JComboBox[] comboBoxes = getBotComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(false);
        }
        for (JLabel label : labels) {
            label.setVisible(false);
        }

        buttonBot.setEnabled(true);
    }

    public JLabel[] getSupportLabels() {
        return new JLabel[]{
                labelSupport1, labelSupport2, labelSupport3, labelSupport4, labelSupport5
        };
    }

    public JComboBox[] getSupportComboBoxes() {
        return new JComboBox[]{
                comboBoxSupport1, comboBoxSupport2, comboBoxSupport3, comboBoxSupport4, comboBoxSupport5
        };
    }

    public void showSupport() {
        JLabel[] labels = getSupportLabels();
        JComboBox[] comboBoxes = getSupportComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(true);
        }
        for (JLabel label : labels) {
            label.setVisible(true);
        }

        buttonSupport.setEnabled(false);
    }

    public void hideSupport() {
        JLabel[] labels = getSupportLabels();
        JComboBox[] comboBoxes = getSupportComboBoxes();

        for (JComboBox comboBox : comboBoxes) {
            comboBox.setVisible(false);
        }
        for (JLabel label : labels) {
            label.setVisible(false);
        }

        buttonSupport.setEnabled(true);
    }

    public JButton[] getButtonBench() {
        return new JButton[]{
                buttonBench1, buttonBench2, buttonBench3, buttonBench4, buttonBench5,
                buttonBench6, buttonBench7, buttonBench8, buttonBench9, buttonBench10
        };
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

    public void addTopListener(ActionListener listener) {
        buttonTop.addActionListener(listener);
    }

    public void addJungleListener(ActionListener listener) {
        buttonJungle.addActionListener(listener);
    }

    public void addMidListener(ActionListener listener) {
        buttonMid.addActionListener(listener);
    }

    public void addBotListener(ActionListener listener) {
        buttonBot.addActionListener(listener);
    }

    public void addSupportListener(ActionListener listener) {
        buttonSupport.addActionListener(listener);
    }

    public void addAutoLockSaveListener(ActionListener listener) {
        buttonAutoLockSave.addActionListener(listener);
    }

    public void addAutoLockEnableListener(ActionListener listener) {
        buttonAutoLockEnable.addActionListener(listener);
    }

    public void addAutoLockDisableListener(ActionListener listener) {
        buttonAutoLockDisable.addActionListener(listener);
    }

    public void addAutoBanSaveListener(ActionListener listener) {
        buttonAutoBanSave.addActionListener(listener);
    }

    public void addAutoBanEnableListener(ActionListener listener) {
        buttonAutoBanEnable.addActionListener(listener);
    }

    public void addAutoBanDisableListener(ActionListener listener) {
        buttonAutoBanDisable.addActionListener(listener);
    }

    public void addAutoLockArenaEnableListener(ActionListener listener) {
        buttonAutoLockArenaEnable.addActionListener(listener);
    }

    public void addAutoLockArenaDisableListener(ActionListener listener) {
        buttonAutoLockArenaDisable.addActionListener(listener);
    }

    public void addAutoLockArenaSaveListener(ActionListener listener) {
        buttonAutoLockArenaSave.addActionListener(listener);
    }

    public void addAutoBanArenaEnableListener(ActionListener listener) {
        buttonAutoBanArenaEnable.addActionListener(listener);
    }

    public void addAutoBanArenaDisableListener(ActionListener listener) {
        buttonAutoBanArenaDisable.addActionListener(listener);
    }

    public void addAutoBanArenaSaveListener(ActionListener listener) {
        buttonAutoBanArenaSave.addActionListener(listener);
    }

    public void addAutoBraveryArenaEnableListener(ActionListener listener) {
        buttonAutoBraveryArenaEnable.addActionListener(listener);
    }

    public void addAutoBraveryArenaDisableListener(ActionListener listener) {
        buttonAutoBraveryArenaDisable.addActionListener(listener);
    }

    public void addSystemTrayEnableListener(ActionListener listener) {
        buttonSystemTrayEnable.addActionListener(listener);
    }

    public void addSystemTrayDisableListener(ActionListener listener) {
        buttonSystemTrayDisable.addActionListener(listener);
    }

    public void addAutoCheckUpdateEnableListener(ActionListener listener) {
        buttonAutoCheckUpdateEnable.addActionListener(listener);
    }

    public void addAutoCheckUpdateDisableListener(ActionListener listener) {
        buttonAutoCheckUpdateDisable.addActionListener(listener);
    }

    public void addAutoBanCrowdFavoriteEnableListener(ActionListener listener) {
        buttonAutoBanCrowdFavoriteEnable.addActionListener(listener);
    }

    public void addAutoBanCrowdFavoriteDisableListener(ActionListener listener) {
        buttonAutoBanCrowdFavoriteDisable.addActionListener(listener);
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

    public void addTestListener(ActionListener listener) {
        buttonTest.addActionListener(listener);
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
        panelInfo = new JPanel();
        panelInfo.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Info", panelInfo);
        panelIcon = new JPanel();
        panelIcon.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelInfo.add(panelIcon, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelIcon = new JLabel();
        labelIcon.setText("");
        panelIcon.add(labelIcon, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelTitle = new JLabel();
        Font labelTitleFont = this.$$$getFont$$$(null, Font.BOLD, 72, labelTitle.getFont());
        if (labelTitleFont != null) labelTitle.setFont(labelTitleFont);
        labelTitle.setText("Rift Helper");
        panelIcon.add(labelTitle, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panelInfo.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        final Spacer spacer2 = new Spacer();
        panelLobby.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        labelAutoAccept = new JLabel();
        labelAutoAccept.setText("Auto Accept");
        panelLobby.add(labelAutoAccept, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoDecline = new JLabel();
        labelAutoDecline.setText("Auto Decline");
        panelLobby.add(labelAutoDecline, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoDecline = new JPanel();
        panelAutoDecline.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelLobby.add(panelAutoDecline, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoDeclineEnable = new JButton();
        buttonAutoDeclineEnable.setText("Enable");
        panelAutoDecline.add(buttonAutoDeclineEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoDeclineDisable = new JButton();
        buttonAutoDeclineDisable.setText("Disable");
        panelAutoDecline.add(buttonAutoDeclineDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelRift = new JPanel();
        panelRift.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Summoner's Rift", panelRift);
        final Spacer spacer3 = new Spacer();
        panelRift.add(spacer3, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        labelAutoLock = new JLabel();
        labelAutoLock.setText("Auto Lock");
        panelRift.add(labelAutoLock, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoLockLanes = new JPanel();
        panelAutoLockLanes.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        panelRift.add(panelAutoLockLanes, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonSupport = new JButton();
        buttonSupport.setText("Support");
        panelAutoLockLanes.add(buttonSupport, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonTop = new JButton();
        buttonTop.setText("Top");
        panelAutoLockLanes.add(buttonTop, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonBot = new JButton();
        buttonBot.setText("Bot");
        panelAutoLockLanes.add(buttonBot, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonJungle = new JButton();
        buttonJungle.setText("Jungle");
        panelAutoLockLanes.add(buttonJungle, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonMid = new JButton();
        buttonMid.setText("Mid");
        panelAutoLockLanes.add(buttonMid, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoLock = new JPanel();
        panelAutoLock.setLayout(new GridLayoutManager(27, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelRift.add(panelAutoLock, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelTop1 = new JLabel();
        labelTop1.setText("1");
        panelAutoLock.add(labelTop1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxTop1 = new JComboBox();
        panelAutoLock.add(comboBoxTop1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxTop2 = new JComboBox();
        panelAutoLock.add(comboBoxTop2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxTop3 = new JComboBox();
        panelAutoLock.add(comboBoxTop3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxTop4 = new JComboBox();
        panelAutoLock.add(comboBoxTop4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxTop5 = new JComboBox();
        panelAutoLock.add(comboBoxTop5, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxJungle1 = new JComboBox();
        panelAutoLock.add(comboBoxJungle1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxJungle2 = new JComboBox();
        panelAutoLock.add(comboBoxJungle2, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxJungle3 = new JComboBox();
        panelAutoLock.add(comboBoxJungle3, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxJungle4 = new JComboBox();
        panelAutoLock.add(comboBoxJungle4, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxJungle5 = new JComboBox();
        panelAutoLock.add(comboBoxJungle5, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxMid1 = new JComboBox();
        panelAutoLock.add(comboBoxMid1, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxMid2 = new JComboBox();
        panelAutoLock.add(comboBoxMid2, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxMid3 = new JComboBox();
        panelAutoLock.add(comboBoxMid3, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxMid4 = new JComboBox();
        panelAutoLock.add(comboBoxMid4, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxMid5 = new JComboBox();
        panelAutoLock.add(comboBoxMid5, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxBot1 = new JComboBox();
        panelAutoLock.add(comboBoxBot1, new GridConstraints(15, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxBot2 = new JComboBox();
        panelAutoLock.add(comboBoxBot2, new GridConstraints(16, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxBot3 = new JComboBox();
        panelAutoLock.add(comboBoxBot3, new GridConstraints(17, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxBot4 = new JComboBox();
        panelAutoLock.add(comboBoxBot4, new GridConstraints(18, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxBot5 = new JComboBox();
        panelAutoLock.add(comboBoxBot5, new GridConstraints(19, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxSupport1 = new JComboBox();
        panelAutoLock.add(comboBoxSupport1, new GridConstraints(20, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxSupport2 = new JComboBox();
        panelAutoLock.add(comboBoxSupport2, new GridConstraints(21, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxSupport3 = new JComboBox();
        panelAutoLock.add(comboBoxSupport3, new GridConstraints(22, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxSupport4 = new JComboBox();
        panelAutoLock.add(comboBoxSupport4, new GridConstraints(23, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxSupport5 = new JComboBox();
        panelAutoLock.add(comboBoxSupport5, new GridConstraints(24, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelTop2 = new JLabel();
        labelTop2.setText("2");
        panelAutoLock.add(labelTop2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelTop3 = new JLabel();
        labelTop3.setText("3");
        panelAutoLock.add(labelTop3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelTop4 = new JLabel();
        labelTop4.setText("4");
        panelAutoLock.add(labelTop4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelTop5 = new JLabel();
        labelTop5.setText("5");
        panelAutoLock.add(labelTop5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelJungle1 = new JLabel();
        labelJungle1.setText("1");
        panelAutoLock.add(labelJungle1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelJungle2 = new JLabel();
        labelJungle2.setText("2");
        panelAutoLock.add(labelJungle2, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelJungle3 = new JLabel();
        labelJungle3.setText("3");
        panelAutoLock.add(labelJungle3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelJungle4 = new JLabel();
        labelJungle4.setText("4");
        panelAutoLock.add(labelJungle4, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelJungle5 = new JLabel();
        labelJungle5.setText("5");
        panelAutoLock.add(labelJungle5, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelMid1 = new JLabel();
        labelMid1.setText("1");
        panelAutoLock.add(labelMid1, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelMid2 = new JLabel();
        labelMid2.setText("2");
        panelAutoLock.add(labelMid2, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelMid3 = new JLabel();
        labelMid3.setText("3");
        panelAutoLock.add(labelMid3, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelMid4 = new JLabel();
        labelMid4.setText("4");
        panelAutoLock.add(labelMid4, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelMid5 = new JLabel();
        labelMid5.setText("5");
        panelAutoLock.add(labelMid5, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelBot1 = new JLabel();
        labelBot1.setText("1");
        panelAutoLock.add(labelBot1, new GridConstraints(15, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelBot2 = new JLabel();
        labelBot2.setText("2");
        panelAutoLock.add(labelBot2, new GridConstraints(16, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelBot3 = new JLabel();
        labelBot3.setText("3");
        panelAutoLock.add(labelBot3, new GridConstraints(17, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelBot4 = new JLabel();
        labelBot4.setText("4");
        panelAutoLock.add(labelBot4, new GridConstraints(18, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelBot5 = new JLabel();
        labelBot5.setText("5");
        panelAutoLock.add(labelBot5, new GridConstraints(19, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelSupport1 = new JLabel();
        labelSupport1.setText("1");
        panelAutoLock.add(labelSupport1, new GridConstraints(20, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelSupport2 = new JLabel();
        labelSupport2.setText("2");
        panelAutoLock.add(labelSupport2, new GridConstraints(21, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelSupport3 = new JLabel();
        labelSupport3.setText("3");
        panelAutoLock.add(labelSupport3, new GridConstraints(22, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelSupport4 = new JLabel();
        labelSupport4.setText("4");
        panelAutoLock.add(labelSupport4, new GridConstraints(23, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelSupport5 = new JLabel();
        labelSupport5.setText("5");
        panelAutoLock.add(labelSupport5, new GridConstraints(24, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoLockButtons = new JPanel();
        panelAutoLockButtons.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelAutoLock.add(panelAutoLockButtons, new GridConstraints(25, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoLockEnable = new JButton();
        buttonAutoLockEnable.setText("Enable");
        panelAutoLockButtons.add(buttonAutoLockEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoLockDisable = new JButton();
        buttonAutoLockDisable.setText("Disable");
        panelAutoLockButtons.add(buttonAutoLockDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoLockSave = new JButton();
        buttonAutoLockSave.setText("Save");
        panelAutoLock.add(buttonAutoLockSave, new GridConstraints(26, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBan = new JLabel();
        labelAutoBan.setText("Auto Ban");
        panelRift.add(labelAutoBan, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoBan = new JPanel();
        panelAutoBan.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelRift.add(panelAutoBan, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelAutoBan1 = new JLabel();
        labelAutoBan1.setText("1");
        panelAutoBan.add(labelAutoBan1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBan1 = new JComboBox();
        panelAutoBan.add(comboBoxAutoBan1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBan2 = new JComboBox();
        panelAutoBan.add(comboBoxAutoBan2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBan3 = new JComboBox();
        panelAutoBan.add(comboBoxAutoBan3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBan4 = new JComboBox();
        panelAutoBan.add(comboBoxAutoBan4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBan5 = new JComboBox();
        panelAutoBan.add(comboBoxAutoBan5, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBan2 = new JLabel();
        labelAutoBan2.setText("2");
        panelAutoBan.add(labelAutoBan2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBan3 = new JLabel();
        labelAutoBan3.setText("3");
        panelAutoBan.add(labelAutoBan3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBan4 = new JLabel();
        labelAutoBan4.setText("4");
        panelAutoBan.add(labelAutoBan4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBan5 = new JLabel();
        labelAutoBan5.setText("5");
        panelAutoBan.add(labelAutoBan5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelAutoBan.add(panel1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoBanEnable = new JButton();
        buttonAutoBanEnable.setText("Enable");
        panel1.add(buttonAutoBanEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoBanDisable = new JButton();
        buttonAutoBanDisable.setText("Disable");
        panel1.add(buttonAutoBanDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoBanSave = new JButton();
        buttonAutoBanSave.setText("Save");
        panelAutoBan.add(buttonAutoBanSave, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelARAM = new JPanel();
        panelARAM.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("ARAM", panelARAM);
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
        final Spacer spacer4 = new Spacer();
        panelARAM.add(spacer4, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        panelAutoRerollButtons = new JPanel();
        panelAutoRerollButtons.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelARAM.add(panelAutoRerollButtons, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoRerollEnable = new JButton();
        buttonAutoRerollEnable.setText("Enable");
        panelAutoRerollButtons.add(buttonAutoRerollEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoRerollDisable = new JButton();
        buttonAutoRerollDisable.setText("Disable");
        panelAutoRerollButtons.add(buttonAutoRerollDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelArena = new JPanel();
        panelArena.setLayout(new GridLayoutManager(9, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Arena", panelArena);
        final Spacer spacer5 = new Spacer();
        panelArena.add(spacer5, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelAutoLockArena = new JPanel();
        panelAutoLockArena.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelArena.add(panelAutoLockArena, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelAutoLockArena1 = new JLabel();
        labelAutoLockArena1.setText("1");
        panelAutoLockArena.add(labelAutoLockArena1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoLockArena1 = new JComboBox();
        panelAutoLockArena.add(comboBoxAutoLockArena1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoLockArena2 = new JComboBox();
        panelAutoLockArena.add(comboBoxAutoLockArena2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoLockArena3 = new JComboBox();
        panelAutoLockArena.add(comboBoxAutoLockArena3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoLockArena4 = new JComboBox();
        panelAutoLockArena.add(comboBoxAutoLockArena4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoLockArena5 = new JComboBox();
        panelAutoLockArena.add(comboBoxAutoLockArena5, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoLockArena2 = new JLabel();
        labelAutoLockArena2.setText("2");
        panelAutoLockArena.add(labelAutoLockArena2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoLockArena3 = new JLabel();
        labelAutoLockArena3.setText("3");
        panelAutoLockArena.add(labelAutoLockArena3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoLockArena4 = new JLabel();
        labelAutoLockArena4.setText("4");
        panelAutoLockArena.add(labelAutoLockArena4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoLockArena5 = new JLabel();
        labelAutoLockArena5.setText("5");
        panelAutoLockArena.add(labelAutoLockArena5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoLockArenaButtons = new JPanel();
        panelAutoLockArenaButtons.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelAutoLockArena.add(panelAutoLockArenaButtons, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoLockArenaEnable = new JButton();
        buttonAutoLockArenaEnable.setText("Enable");
        panelAutoLockArenaButtons.add(buttonAutoLockArenaEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoLockArenaDisable = new JButton();
        buttonAutoLockArenaDisable.setText("Disable");
        panelAutoLockArenaButtons.add(buttonAutoLockArenaDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoLockArenaSave = new JButton();
        buttonAutoLockArenaSave.setText("Save");
        panelAutoLockArena.add(buttonAutoLockArenaSave, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoLockArena = new JLabel();
        labelAutoLockArena.setText("Auto Lock");
        panelArena.add(labelAutoLockArena, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBanArena = new JLabel();
        labelAutoBanArena.setText("Auto Ban");
        panelArena.add(labelAutoBanArena, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoBanArena = new JPanel();
        panelAutoBanArena.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelArena.add(panelAutoBanArena, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        labelAutoBanArena1 = new JLabel();
        labelAutoBanArena1.setText("1");
        panelAutoBanArena.add(labelAutoBanArena1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBanArena1 = new JComboBox();
        panelAutoBanArena.add(comboBoxAutoBanArena1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBanArena2 = new JComboBox();
        panelAutoBanArena.add(comboBoxAutoBanArena2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBanArena3 = new JComboBox();
        panelAutoBanArena.add(comboBoxAutoBanArena3, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBanArena4 = new JComboBox();
        panelAutoBanArena.add(comboBoxAutoBanArena4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxAutoBanArena5 = new JComboBox();
        panelAutoBanArena.add(comboBoxAutoBanArena5, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBanArena2 = new JLabel();
        labelAutoBanArena2.setText("2");
        panelAutoBanArena.add(labelAutoBanArena2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBanArena3 = new JLabel();
        labelAutoBanArena3.setText("3");
        panelAutoBanArena.add(labelAutoBanArena3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBanArena4 = new JLabel();
        labelAutoBanArena4.setText("4");
        panelAutoBanArena.add(labelAutoBanArena4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBanArena5 = new JLabel();
        labelAutoBanArena5.setText("5");
        panelAutoBanArena.add(labelAutoBanArena5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoBanArenaButtons = new JPanel();
        panelAutoBanArenaButtons.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelAutoBanArena.add(panelAutoBanArenaButtons, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoBanArenaEnable = new JButton();
        buttonAutoBanArenaEnable.setText("Enable");
        panelAutoBanArenaButtons.add(buttonAutoBanArenaEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoBanArenaDisable = new JButton();
        buttonAutoBanArenaDisable.setText("Disable");
        panelAutoBanArenaButtons.add(buttonAutoBanArenaDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoBanArenaSave = new JButton();
        buttonAutoBanArenaSave.setText("Save");
        panelAutoBanArena.add(buttonAutoBanArenaSave, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoBravery = new JPanel();
        panelAutoBravery.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelArena.add(panelAutoBravery, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoBraveryArenaEnable = new JButton();
        buttonAutoBraveryArenaEnable.setText("Enable");
        panelAutoBravery.add(buttonAutoBraveryArenaEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoBraveryArenaDisable = new JButton();
        buttonAutoBraveryArenaDisable.setText("Disable");
        panelAutoBravery.add(buttonAutoBraveryArenaDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBraveryArena = new JLabel();
        labelAutoBraveryArena.setText("Auto Bravery");
        panelArena.add(labelAutoBraveryArena, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoBanCrowdFavorite = new JLabel();
        labelAutoBanCrowdFavorite.setText("Auto Ban Crowd Favorite");
        panelArena.add(labelAutoBanCrowdFavorite, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoBanCrowdFavorite = new JPanel();
        panelAutoBanCrowdFavorite.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelArena.add(panelAutoBanCrowdFavorite, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoBanCrowdFavoriteEnable = new JButton();
        buttonAutoBanCrowdFavoriteEnable.setText("Enable");
        panelAutoBanCrowdFavorite.add(buttonAutoBanCrowdFavoriteEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoBanCrowdFavoriteDisable = new JButton();
        buttonAutoBanCrowdFavoriteDisable.setText("Disable");
        panelAutoBanCrowdFavorite.add(buttonAutoBanCrowdFavoriteDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelLoot = new JPanel();
        panelLoot.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPaneRiftHelper.addTab("Loot", panelLoot);
        labelMassChampionDisenchant = new JLabel();
        labelMassChampionDisenchant.setText("Mass Champion Disenchant");
        panelLoot.add(labelMassChampionDisenchant, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panelLoot.add(spacer6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelAutoDisenchantButtons.add(panel2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonDisenchantSkinsSafe = new JButton();
        buttonDisenchantSkinsSafe.setText("Safe Mode (Disenchant Already Owned Skins)");
        panel2.add(buttonDisenchantSkinsSafe, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonDisenchantSkinsHard = new JButton();
        buttonDisenchantSkinsHard.setText("Hard Mode (Disenchant Everything)");
        panel2.add(buttonDisenchantSkinsHard, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelSettings = new JPanel();
        panelSettings.setLayout(new GridLayoutManager(13, 1, new Insets(0, 0, 0, 0), -1, -1));
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
        final Spacer spacer7 = new Spacer();
        panelSettings.add(spacer7, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        panelTest = new JPanel();
        panelTest.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panelSettings.add(panelTest, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonTest = new JButton();
        buttonTest.setText("Developer Button");
        panelTest.add(buttonTest, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelAutoCheckUpdate = new JLabel();
        labelAutoCheckUpdate.setText("Check for New Releases");
        panelSettings.add(labelAutoCheckUpdate, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelAutoCheckUpdate = new JPanel();
        panelAutoCheckUpdate.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panelSettings.add(panelAutoCheckUpdate, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonAutoCheckUpdateEnable = new JButton();
        buttonAutoCheckUpdateEnable.setText("Enable");
        panelAutoCheckUpdate.add(buttonAutoCheckUpdateEnable, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonAutoCheckUpdateDisable = new JButton();
        buttonAutoCheckUpdateDisable.setText("Disable");
        panelAutoCheckUpdate.add(buttonAutoCheckUpdateDisable, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelRiftHelper;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

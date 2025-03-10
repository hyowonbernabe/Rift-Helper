package controller;

import model.*;
import no.stelar7.api.r4j.impl.lol.lcu.LCUSocketReader;
import view.FileChooserView;
import view.RiftHelperMainView;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

public class RiftHelperMainController {
    private RiftHelperMainView riftHelperMainView;
    private volatile boolean autoAccept;
    private volatile boolean autoDecline;
    private volatile boolean autoSwap;
    private volatile int autoSwapSlots;
    private volatile int priority;
    private volatile boolean alwaysOnTop;
    private volatile boolean centerGUI;
    private volatile boolean systemTray;
    private volatile boolean autoCheckUpdate;
    private volatile boolean autoReroll;
    private volatile int autoLockLaneChoice;
    private volatile boolean autoLockRank;
    private volatile boolean autoBan;
    private volatile boolean autoLockArena;
    private volatile boolean autoBanArena;
    private volatile boolean autoBanCrowdFavoriteArena;
    private volatile boolean autoBravery;
    private List<BenchChampions> benchChampions;
    private String[] priorityChampions;
    private String[] topChampions;
    private String[] jungleChampions;
    private String[] midChampions;
    private String[] botChampions;
    private String[] supportChampions;
    private String[] banChampions;
    private String[] arenaChampions;
    private String[] banArenaChampions;
    private int rerollsRemaining;
    private int userCellId;
    private final ExecutorService autoSwapExecutorService = Executors.newFixedThreadPool(1);

    public RiftHelperMainController(RiftHelperMainView riftHelperMainView) {
        this.riftHelperMainView = riftHelperMainView;

        startProgram();

        LCUSocketReader socketReader = new LCUSocketReader();
        socketReader.connect();

        System.out.println("Connected to Client: " + socketReader.isConnected());

        socketReader.subscribe("OnJsonApiEvent_lol-champ-select_v1_session", eventData -> {
            Session session = Session.parseFromJson(eventData);

            if (session == null) {
                return;
            }

            List<List<Actions>> actions = session.getActions();
            List<Actions> flattenedActions = actions.stream().flatMap(List::stream).toList();

            List<MyTeam> myTeam = session.getMyTeam();

            userCellId = session.getLocalPlayerCellId();

            System.out.println(session);
            if (!session.isAllowRerolling()) {
                String assignedPosition = "";
                int actionIdPicking = 0;
                int actionIdBanning = 0;
                boolean isInProgressPicking = false;
                boolean isInProgressBanning = false;

                for (Actions userSelection : flattenedActions) {
                    if (userSelection.getActorCellId() == userCellId && userSelection.getType().equals("pick")) {
                        actionIdPicking = userSelection.getId();
                        isInProgressPicking = userSelection.isInProgress();
                    }
                    if (userSelection.getActorCellId() == userCellId && userSelection.getType().equals("ban")) {
                        actionIdBanning = userSelection.getId();
                        isInProgressBanning = userSelection.isInProgress();
                    }
                }

                for (MyTeam myteam : myTeam) {
                    if (userCellId == myteam.getCellId()) {
                        assignedPosition = myteam.getAssignedPosition();
                    }
                }

                String endpointPicking = "/lol-champ-select/v1/session/actions/" + actionIdPicking;
                String endpointBanning = "/lol-champ-select/v1/session/actions/" + actionIdBanning;

                if (isInProgressPicking) {
                    autoLock(endpointPicking, actionIdPicking, assignedPosition);
                    autoBravery(endpointPicking, actionIdPicking);
                    autoLockArena(endpointPicking, actionIdPicking);
                }

                if (isInProgressBanning) {
                    autoBan(endpointBanning, actionIdBanning, assignedPosition);
                    autoBanArena(endpointBanning, actionIdBanning);
                    autoBanCrowdFavoriteArena(endpointBanning, actionIdBanning);
                }
            }

            if (session.isAllowRerolling()) {
                rerollsRemaining = session.getRerollsRemaining();
                benchChampions = session.getBenchChampions();
                JButton[] buttons = this.riftHelperMainView.getButtonBench();

                if (actions == null) {
                    for (JButton button : buttons) {
                        button.setText(null);
                    }
                    priority = 10;
                    this.riftHelperMainView.panelQuickSwitchBench2.setVisible(false);
                    return;
                }

                autoReroll(rerollsRemaining);
                autoSwap();
                nameButtons(buttons);
            }
        });

        this.riftHelperMainView.addTestListener(e -> {
            System.out.println(LCUGet.getFromClient("/lol-lobby-team-builder-champ-select/v1/crowd-favorite-champion-list"));
            System.out.println(LCUGet.getFromClient("/lol-champ-select/v1/crowd-favorite-champion-list"));
        });

        this.riftHelperMainView.addBench1ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 1) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.getFirst().getChampionId());
        });

        this.riftHelperMainView.addBench2ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 2) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(1).getChampionId());
        });

        this.riftHelperMainView.addBench3ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 3) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(2).getChampionId());
        });

        this.riftHelperMainView.addBench4ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 4) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(3).getChampionId());
        });

        this.riftHelperMainView.addBench5ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 5) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(4).getChampionId());
        });

        this.riftHelperMainView.addBench6ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 6) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(5).getChampionId());
        });

        this.riftHelperMainView.addBench7ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 7) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(6).getChampionId());
        });

        this.riftHelperMainView.addBench8ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 8) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(7).getChampionId());
        });

        this.riftHelperMainView.addBench9ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 9) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(8).getChampionId());
        });

        this.riftHelperMainView.addBench10ActionListener(e -> {
            if (benchChampions == null || benchChampions.size() < 10) {
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(9).getChampionId());
        });

        this.riftHelperMainView.addAutoAcceptEnableListener(e -> {
            LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");

            autoAcceptEnable(socketReader);
        });

        this.riftHelperMainView.addAutoAcceptDisableListener(e -> {
            autoAcceptDisable(socketReader);
        });

        this.riftHelperMainView.addAutoDeclineEnableListener(e -> {
            LCUPost.postToClient("/lol-matchmaking/v1/ready-check/decline");

            autoDeclineEnable(socketReader);
        });

        this.riftHelperMainView.addAutoDeclineDisableListener(e -> {
            autoDeclineDisable(socketReader);
        });

        this.riftHelperMainView.addChangeResponseAcceptListener(e -> {
            LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");

            autoAcceptDisable(socketReader);
            autoDeclineDisable(socketReader);
        });

        this.riftHelperMainView.addChangeResponseDeclineListener(e -> {
            LCUPost.postToClient("/lol-matchmaking/v1/ready-check/decline");

            autoAcceptDisable(socketReader);
            autoDeclineDisable(socketReader);
        });

        this.riftHelperMainView.addAutoSwapEnableListener(e -> {
            autoSwap = true;
            System.out.println("Auto Swap Turned On: " + autoSwap);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapEnable.setEnabled(false);
                riftHelperMainView.buttonAutoSwapDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoSwapDisableListener(e -> {
            autoSwap = false;
            priority = 1;
            System.out.println("Auto Swap Turned Off: " + autoSwap);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapEnable.setEnabled(true);
                riftHelperMainView.buttonAutoSwapDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAlwaysOnTopEnableListener(e -> {
            alwaysOnTop = true;
            this.riftHelperMainView.setAlwaysOnTop(alwaysOnTop);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAlwaysOnTopEnable.setEnabled(false);
                riftHelperMainView.buttonAlwaysOnTopDisable.setEnabled(true);
            });

            PreferenceManager.setAlwaysOnTop(alwaysOnTop);
        });

        this.riftHelperMainView.addAlwaysOnTopDisableListener(e -> {
            alwaysOnTop = false;
            this.riftHelperMainView.setAlwaysOnTop(alwaysOnTop);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAlwaysOnTopEnable.setEnabled(true);
                riftHelperMainView.buttonAlwaysOnTopDisable.setEnabled(false);
            });

            PreferenceManager.setAlwaysOnTop(alwaysOnTop);
        });

        this.riftHelperMainView.addAutoSwapAddListener(e -> {
            if (autoSwapSlots >= 10) {
                JOptionPane.showMessageDialog(riftHelperMainView, "You cannot add more slots.\nThe maximum is 10.", "Error Adding", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoSwapSlots++;

            PreferenceManager.setAutoSwapSlots(autoSwapSlots);

            updateAutoSwapSlots();
        });

        this.riftHelperMainView.addAutoSwapSubtractListener(e -> {
            if (autoSwapSlots <= 1) {
                JOptionPane.showMessageDialog(riftHelperMainView, "You cannot subtract more slots.\nThe maximum is 1.", "Error Subtracting", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoSwapSlots--;

            PreferenceManager.setAutoSwapSlots(autoSwapSlots);

            updateAutoSwapSlots();
        });

        this.riftHelperMainView.addAutoSwapSaveListener(e -> {
            saveAutoSwap();
        });

        this.riftHelperMainView.addCenterGUIEnableListener(e -> {
            centerGUI = true;

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonCenterGUIEnable.setEnabled(false);
                riftHelperMainView.buttonCenterGUIDisable.setEnabled(true);
            });

            PreferenceManager.setCenterGUI(centerGUI);
        });

        this.riftHelperMainView.addCenterGUIDisableListener(e -> {
            centerGUI = false;

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonCenterGUIEnable.setEnabled(true);
                riftHelperMainView.buttonCenterGUIDisable.setEnabled(false);
            });

            PreferenceManager.setCenterGUI(centerGUI);
        });

        this.riftHelperMainView.addAutoRerollEnableListener(e -> {
            autoReroll = true;
            System.out.println("Auto Reroll Turned On: " + autoReroll);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoRerollEnable.setEnabled(false);
                riftHelperMainView.buttonAutoRerollDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoRerollDisableListener(e -> {
            autoReroll = false;
            System.out.println("Auto Reroll Turned Off: " + autoReroll);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoRerollEnable.setEnabled(true);
                riftHelperMainView.buttonAutoRerollDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addExportListener(e -> {
            PreferenceManager.exportPreferences();
            String currDir = CurrentDirectory.getCurrentDirectory();
            JOptionPane.showMessageDialog(riftHelperMainView, "Preferences saved on: \n" + currDir, "Export Success", JOptionPane.INFORMATION_MESSAGE);
        });

        this.riftHelperMainView.addImportListener(e -> {
            FileChooserView fileChooserView = new FileChooserView();
            File file = fileChooserView.selectFile("Select Preference File");
            if (file != null) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    Preferences.importPreferences(fis);

                    JOptionPane.showMessageDialog(riftHelperMainView, "Preferences imported!", "Import Success", JOptionPane.INFORMATION_MESSAGE);

                    reset();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(riftHelperMainView, "Preferences not imported.", "Import Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.riftHelperMainView.addResetListener( e -> {
           int result = JOptionPane.showConfirmDialog(riftHelperMainView, "Are you sure you want to reset the preferences?", "Reset Preferences", JOptionPane.YES_NO_OPTION);
           if (result == JOptionPane.YES_OPTION) {
               PreferenceManager.resetPreferences();

               JOptionPane.showMessageDialog(riftHelperMainView, "Preferences Successfully Reset!", "Reset Success", JOptionPane.INFORMATION_MESSAGE);

               reset();
           }
        });

        this.riftHelperMainView.addAutoDisenchantChampionsSafeListener(e -> {
            String eventData = LCUGet.getFromClient("/lol-loot/v1/player-loot");
            List<ShardLoot> shardLoots = ShardLoot.parseFromJson(eventData);

            int totalChampions = 0;
            int totalShards = 0;
            int totalEssenceToDisenchant = 0;
            int totalEssenceBeforeDisenchant = 0;

            for (ShardLoot csi : shardLoots) {
                if (csi.getDisenchantLootName().equals("CURRENCY_champion")) {
                    if (csi.getItemStatus().equals("OWNED")) {
                        totalChampions++;
                        totalShards += csi.getCount();
                        totalEssenceToDisenchant += csi.getDisenchantValue();
                    }
                }

                if (csi.getLootId().equals("CURRENCY_champion")) {
                    totalEssenceBeforeDisenchant = csi.getCount();
                }
            }

            int totalEssenceAfterDisenchant = totalEssenceBeforeDisenchant + totalEssenceToDisenchant;

            if (totalShards == 0 && totalChampions == 0) {
                JOptionPane.showMessageDialog(riftHelperMainView, "There are no champions that are already owned to disenchant!", "Mass Champion Disenchant", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(riftHelperMainView, "Total Champions: " + totalChampions +
                    "\nTotal Shards: " + totalShards +
                    "\nTotal Essence: " + totalEssenceToDisenchant +
                    "\nEssence Before Disenchanting: " + totalEssenceBeforeDisenchant +
                    "\nEssence After Disenchanting: " + totalEssenceAfterDisenchant, "Mass Champion Disenchant", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                String input = JOptionPane.showInputDialog(riftHelperMainView, "<html><center><b>LAST WARNING</b></center><br><br>" +
                        "Continuing will result in " + totalShards + " shards being disenchanted.<br><br><b>You have been warned.</b><br><br>" +
                        "To continue, please enter:<br>" + LCUAuth.port + LCUAuth.token.substring(0, 6)+ "<br></html>", "Mass Champion Disenchant (Safe Mode)", JOptionPane.WARNING_MESSAGE);

                if (Objects.equals(input, LCUAuth.port + LCUAuth.token.substring(0, 6))) {
                    String endpoint = "/lol-loot/v1/recipes/CHAMPION_RENTAL_disenchant/craft?repeat=";

                    for (ShardLoot cs : shardLoots) {
                        if (cs.getDisenchantLootName().equals("CURRENCY_champion")) {
                            if (cs.getItemStatus().equals("OWNED")) {
                                int count = cs.getCount();
                                String champion = cs.getLootId();

                                int responseCode = LCUPost.postToClientWithBody(endpoint + count, "[\"" + champion + "\"]");
                                System.out.println(responseCode);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(riftHelperMainView, "Incorrect", "Mass Champion Disenchant", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.riftHelperMainView.addAutoDisenchantChampionsHardListener(e -> {
            String eventData = LCUGet.getFromClient("/lol-loot/v1/player-loot");
            List<ShardLoot> shardLoots = ShardLoot.parseFromJson(eventData);

            int totalChampions = 0;
            int totalShards = 0;
            int totalEssenceToDisenchant = 0;
            int totalEssenceBeforeDisenchant = 0;

            for (ShardLoot csi : shardLoots) {
                if (csi.getDisenchantLootName().equals("CURRENCY_champion")) {
                    totalChampions++;
                    totalShards += csi.getCount();
                    totalEssenceToDisenchant += csi.getDisenchantValue();
                }

                if (csi.getLootId().equals("CURRENCY_champion")) {
                    totalEssenceBeforeDisenchant = csi.getCount();
                }
            }

            int totalEssenceAfterDisenchant = totalEssenceBeforeDisenchant + totalEssenceToDisenchant;

            if (totalShards == 0 && totalChampions == 0) {
                JOptionPane.showMessageDialog(riftHelperMainView, "There are no champions to disenchant!", "Mass Champion Disenchant", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(riftHelperMainView, "Total Champions: " + totalChampions +
                    "\nTotal Shards: " + totalShards +
                    "\nTotal Essence: " + totalEssenceToDisenchant +
                    "\nEssence Before Disenchanting: " + totalEssenceBeforeDisenchant +
                    "\nEssence After Disenchanting: " + totalEssenceAfterDisenchant, "Mass Champion Disenchant", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                String input = JOptionPane.showInputDialog(riftHelperMainView, "<html><center><b>LAST WARNING</b></center><br><br>" +
                        "Continuing will result in " + totalShards + " shards being disenchanted.<br><br><b>You have been warned.</b><br><br>" +
                        "To continue, please enter:<br>" + LCUAuth.port + LCUAuth.token.substring(0, 6)+ "<br></html>", "Mass Champion Disenchant (Hard Mode)", JOptionPane.WARNING_MESSAGE);

                if (Objects.equals(input, LCUAuth.port + LCUAuth.token.substring(0, 6))) {
                    String endpoint = "/lol-loot/v1/recipes/CHAMPION_RENTAL_disenchant/craft?repeat=";

                    for (ShardLoot cs : shardLoots) {
                        if (cs.getDisenchantLootName().equals("CURRENCY_champion")) {
                            int count = cs.getCount();
                            String champion = cs.getLootId();

                            int responseCode = LCUPost.postToClientWithBody(endpoint + count, "[\"" + champion + "\"]");
                            System.out.println(responseCode);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(riftHelperMainView, "Incorrect", "Mass Champion Disenchant", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.riftHelperMainView.addAutoDisenchantSkinsSafeListener(e -> {
            JOptionPane.showMessageDialog(riftHelperMainView, "Not yet implemented", "Error", JOptionPane.ERROR_MESSAGE);
        });

        this.riftHelperMainView.addAutoDisenchantSkinsHardListener(e -> {
            JOptionPane.showMessageDialog(riftHelperMainView, "Not yet implemented", "Error", JOptionPane.ERROR_MESSAGE);
        });

        this.riftHelperMainView.addSystemTrayEnableListener(e -> {
            systemTray = true;

            this.riftHelperMainView.enableSystemTray();

            PreferenceManager.setSystemTray(systemTray);
        });

        this.riftHelperMainView.addSystemTrayDisableListener(e -> {
            systemTray = false;

            this.riftHelperMainView.disableSystemTray();

            PreferenceManager.setSystemTray(systemTray);
        });

        this.riftHelperMainView.addTopListener(e -> {
            showTop();

            PreferenceManager.setAutoLockLaneChoice(0);
        });

        this.riftHelperMainView.addJungleListener(e -> {
            showJungle();

            PreferenceManager.setAutoLockLaneChoice(1);
        });

        this.riftHelperMainView.addMidListener(e -> {
            showMid();

            PreferenceManager.setAutoLockLaneChoice(2);
        });

        this.riftHelperMainView.addBotListener(e -> {
            showBot();

            PreferenceManager.setAutoLockLaneChoice(3);
        });

        this.riftHelperMainView.addSupportListener(e -> {
            showSupport();

            PreferenceManager.setAutoLockLaneChoice(4);
        });

        this.riftHelperMainView.addAutoLockSaveListener(e -> {
            autoLockSave();
        });

        this.riftHelperMainView.addAutoLockEnableListener(e -> {
            autoLockRank = true;
            System.out.println("Auto Lock Turned On: " + autoLockRank);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoLockEnable.setEnabled(false);
                riftHelperMainView.buttonAutoLockDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoLockDisableListener(e -> {
            autoLockRank = false;
            System.out.println("Auto Lock Turned Off: " + autoLockRank);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoLockEnable.setEnabled(true);
                riftHelperMainView.buttonAutoLockDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoBanEnableListener(e -> {
            autoBan = true;
            System.out.println("Auto Ban Turned On: " + autoBan);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBanDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBanDisableListener(e -> {
            autoBan = false;
            System.out.println("Auto Ban Turned Off: " + autoBan);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanEnable.setEnabled(true);
                riftHelperMainView.buttonAutoBanDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoBanSaveListener(e -> {
            String[] ban = {
                    riftHelperMainView.getComboBoxAutoBan1(), riftHelperMainView.getComboBoxAutoBan2(),
                    riftHelperMainView.getComboBoxAutoBan3(), riftHelperMainView.getComboBoxAutoBan4(),
                    riftHelperMainView.getComboBoxAutoBan5()
            };

            PreferenceManager.setAutoBanPriority(ban);

            JOptionPane.showMessageDialog(riftHelperMainView, "Successfully saved!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
        });

        this.riftHelperMainView.addAutoLockArenaEnableListener(e -> {
            if (autoBravery) {
                JOptionPane.showMessageDialog(riftHelperMainView, "Auto Bravery is enabled.", "Cannot Auto Lock (Arena)", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoLockArena = true;
            System.out.println("Auto Lock (Arena) Turned On: " + autoLockArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoLockArenaEnable.setEnabled(false);
                riftHelperMainView.buttonAutoLockArenaDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoLockArenaDisableListener(e -> {
            autoLockArena = false;
            System.out.println("Auto Lock (Arena) Turned Off: " + autoLockArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoLockArenaEnable.setEnabled(true);
                riftHelperMainView.buttonAutoLockArenaDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoLockArenaSaveListener(e -> {
            String[] champions = {
                    riftHelperMainView.getComboBoxAutoLockArenaPriority1(), riftHelperMainView.getComboBoxAutoLockArenaPriority2(),
                    riftHelperMainView.getComboBoxAutoLockArenaPriority3(), riftHelperMainView.getComboBoxAutoLockArenaPriority4(),
                    riftHelperMainView.getComboBoxAutoLockArenaPriority5()
            };

            PreferenceManager.setAutoLockArenaPriority(champions);

            JOptionPane.showMessageDialog(riftHelperMainView, "Successfully saved!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
        });

        this.riftHelperMainView.addAutoBanArenaEnableListener(e -> {
            if (autoBanCrowdFavoriteArena) {
                JOptionPane.showMessageDialog(riftHelperMainView, "Auto Ban (Arena) is enabled.", "Cannot Auto Ban Crowd Favorite (Arena)", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoBanArena = true;
            System.out.println("Auto Ban (Arena) Turned On: " + autoBanArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanArenaEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBanArenaDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBanArenaDisableListener(e -> {
            autoBanArena = false;
            System.out.println("Auto Ban (Arena) Turned Off: " + autoBanArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanArenaEnable.setEnabled(true);
                riftHelperMainView.buttonAutoBanArenaDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoBanArenaSaveListener(e -> {
            String[] ban = {
                    riftHelperMainView.getComboBoxAutoBanArenaPriority1(), riftHelperMainView.getComboBoxAutoBanArenaPriority2(),
                    riftHelperMainView.getComboBoxAutoBanArenaPriority3(), riftHelperMainView.getComboBoxAutoBanArenaPriority4(),
                    riftHelperMainView.getComboBoxAutoBanArenaPriority5()
            };

            PreferenceManager.setAutoBanArenaPriority(ban);

            JOptionPane.showMessageDialog(riftHelperMainView, "Successfully saved!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
        });

        this.riftHelperMainView.addAutoBanCrowdFavoriteEnableListener(e -> {
            if (autoBanArena) {
                JOptionPane.showMessageDialog(riftHelperMainView, "Auto Ban Crowd Favorite (Arena) is enabled.", "Cannot Auto Ban (Arena)", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoBanCrowdFavoriteArena = true;
            System.out.println("Auto Ban Crowd Favorite (Arena) Turned On: " + autoBanCrowdFavoriteArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanCrowdFavoriteEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBanCrowdFavoriteDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBanCrowdFavoriteDisableListener(e -> {
            autoBanCrowdFavoriteArena = false;
            System.out.println("Auto Ban Crowd Favorite (Arena) Turned Off: " + autoBanCrowdFavoriteArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanCrowdFavoriteEnable.setEnabled(true);
                riftHelperMainView.buttonAutoBanCrowdFavoriteDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoBraveryArenaEnableListener(e -> {
            if (autoLockArena) {
                JOptionPane.showMessageDialog(riftHelperMainView, "Auto Lock (Arena) is enabled.", "Cannot Auto Bravery", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoBravery = true;
            System.out.println("Auto Bravery (Arena) Turned On: " + autoBravery);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBraveryArenaEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBraveryArenaDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBraveryArenaDisableListener(e -> {
            autoBravery = false;
            System.out.println("Auto Bravery (Arena) Turned Off: " + autoBravery);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBraveryArenaEnable.setEnabled(true);
                riftHelperMainView.buttonAutoBraveryArenaDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoCheckUpdateEnableListener(e -> {
            autoCheckUpdate = true;

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoCheckUpdateEnable.setEnabled(false);
                riftHelperMainView.buttonAutoCheckUpdateDisable.setEnabled(true);
            });

            PreferenceManager.setAutoCheckUpdate(autoCheckUpdate);
        });

        this.riftHelperMainView.addAutoCheckUpdateDisableListener(e -> {
            autoCheckUpdate = false;

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoCheckUpdateEnable.setEnabled(true);
                riftHelperMainView.buttonAutoCheckUpdateDisable.setEnabled(false);
            });

            PreferenceManager.setAutoCheckUpdate(autoCheckUpdate);
        });
    }

    private void autoDeclineDisable(LCUSocketReader socketReader) {
        autoDecline = false;

        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoDeclineEnable.setEnabled(true);
            riftHelperMainView.buttonAutoDeclineDisable.setEnabled(false);
        });

        socketReader.unsubscribe("OnJsonApiEvent_lol-matchmaking_v1_search");
    }

    private void autoDeclineEnable(LCUSocketReader socketReader) {
        if (autoAccept) {
            JOptionPane.showMessageDialog(riftHelperMainView, "Auto Accept is enabled.", "Cannot Auto Decline", JOptionPane.WARNING_MESSAGE);
            return;
        }

        autoDecline = true;

        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoDeclineEnable.setEnabled(false);
            riftHelperMainView.buttonAutoDeclineDisable.setEnabled(true);
        });

        socketReader.subscribe("OnJsonApiEvent_lol-matchmaking_v1_search", eventData -> {
            Matchmaking matchmaking = Matchmaking.parseFromJson(eventData);
            ReadyCheck readyCheck = matchmaking.getReadyCheck();

            if (readyCheck.getState().equals("InProgress")) {
                LCUPost.postToClient("/lol-matchmaking/v1/ready-check/decline");
            }
        });
    }

    private void autoAcceptDisable(LCUSocketReader socketReader) {
        autoAccept = false;
        System.out.println("Auto Accept Turned Off: " + autoAccept);

        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoAcceptEnable.setEnabled(true);
            riftHelperMainView.buttonAutoAcceptDisable.setEnabled(false);
        });

        socketReader.unsubscribe("OnJsonApiEvent_lol-matchmaking_v1_search");
    }

    private void autoAcceptEnable(LCUSocketReader socketReader) {
        if (autoDecline) {
            JOptionPane.showMessageDialog(riftHelperMainView, "Auto Decline is enabled.", "Cannot Auto Accept", JOptionPane.WARNING_MESSAGE);
            return;
        }

        autoAccept = true;
        System.out.println("Auto Accept Turned On: " + autoAccept);

        riftHelperMainView.buttonAutoAcceptEnable.setEnabled(false);
        riftHelperMainView.buttonAutoAcceptDisable.setEnabled(true);

        socketReader.subscribe("OnJsonApiEvent_lol-matchmaking_v1_search", eventData -> {
            Matchmaking matchmaking = Matchmaking.parseFromJson(eventData);
            ReadyCheck readyCheck = matchmaking.getReadyCheck();

            if (readyCheck.getState().equals("InProgress")) {
                LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");
            }
        });
    }

    private void autoLock(String endpoint, int actionId, String assignedPosition) {
        if (autoLockRank) {
            switch (assignedPosition) {
                case "top" -> {
                    int[] topPriority = {
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxTop1()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxTop2()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxTop3()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxTop4()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxTop5())
                    };

                    for (int i : topPriority) {
                        if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                            return;
                        }
                    }
                }
                case "jungle" -> {
                    int[] junglePriority = {
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxJungle1()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxJungle2()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxJungle3()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxJungle4()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxJungle5())
                    };

                    for (int i : junglePriority) {
                        if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                            return;
                        }
                    }
                }
                case "middle" -> {
                    int[] midPriority = {
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxMid1()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxMid2()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxMid3()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxMid4()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxMid5())
                    };

                    for (int i : midPriority) {
                        if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                            return;
                        }
                    }
                }
                case "bottom" -> {
                    int[] botPriority = {
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxBot1()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxBot2()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxBot3()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxBot4()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxBot5())
                    };

                    for (int i : botPriority) {
                        if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                            return;
                        }
                    }
                }
                case "support" -> {
                    int[] supportPriorities = {
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxSupport1()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxSupport2()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxSupport3()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxSupport4()),
                            DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxSupport5()),
                    };

                    for (int i : supportPriorities) {
                        if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private void autoBan(String endpoint, int actionId, String assignedPosition) {
        if (autoBan) {
            if (assignedPosition.equals("top") || assignedPosition.equals("jungle") || assignedPosition.equals("middle") || assignedPosition.equals("bottom") || assignedPosition.equals("support")) {
                int[] banPriority = {
                        DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBan1()),
                        DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBan2()),
                        DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBan3()),
                        DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBan4()),
                        DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBan5())
                };

                for (int i : banPriority) {
                    if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                        return;
                    }
                }
            }
        }
    }

    public void autoBravery(String endpoint, int actionId) {
        if (autoBravery) {
            LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, -3));
        }
    }

    public void autoLockArena(String endpoint, int actionId) {
        if (autoLockArena) {
            int[] championPriorities = {
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoLockArenaPriority1()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoLockArenaPriority2()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoLockArenaPriority3()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoLockArenaPriority4()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoLockArenaPriority5())
            };

            for (int i : championPriorities) {
                if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                    return;
                }
            }
        }
    }

    public void autoBanArena(String endpoint, int actionId) {
        if (autoBanArena) {
            int[] banPriority = {
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBanArenaPriority1()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBanArenaPriority2()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBanArenaPriority3()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBanArenaPriority4()),
                    DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoBanArenaPriority5())
            };

            for (int i : banPriority) {
                if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                    return;
                }
            }
        }
    }

    public void autoBanCrowdFavoriteArena(String endpoint, int actionId) {
        if (autoBanCrowdFavoriteArena) {
            int[] banPriority = GsonParser.parseFromJsonIntArray(LCUGet.getFromClient("/lol-lobby-team-builder/champ-select/v1/crowd-favorte-champion-list"));

            for (int i : banPriority) {
                if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                    return;
                }
            }
        }
    }

    private String jsonBodyAutoChoose(int actionId, int championId) {
        return "{"
                + "\"id\": " + actionId + ","
                + "\"actorCellId\": " + userCellId + ","
                + "\"championId\": " + championId + ","
                + "\"type\": \"pick\"" + ","
                + "\"completed\": true" + ","
                + "\"isAllyAction\": true"
                + "}";
    }

    private void autoLockSave() {
        String[] top = {
                riftHelperMainView.getComboBoxTop1(), riftHelperMainView.getComboBoxTop2(), riftHelperMainView.getComboBoxTop3(),
                riftHelperMainView.getComboBoxTop4(), riftHelperMainView.getComboBoxTop5()
        };
        String[] jungle = {
                riftHelperMainView.getComboBoxJungle1(), riftHelperMainView.getComboBoxJungle2(), riftHelperMainView.getComboBoxJungle3(),
                riftHelperMainView.getComboBoxJungle4(), riftHelperMainView.getComboBoxJungle5()
        };
        String[] mid = {
                riftHelperMainView.getComboBoxMid1(), riftHelperMainView.getComboBoxMid2(), riftHelperMainView.getComboBoxMid3(),
                riftHelperMainView.getComboBoxMid4(), riftHelperMainView.getComboBoxMid5()
        };
        String[] bot = {
                riftHelperMainView.getComboBoxBot1(), riftHelperMainView.getComboBoxBot2(), riftHelperMainView.getComboBoxBot3(),
                riftHelperMainView.getComboBoxBot4(), riftHelperMainView.getComboBoxBot5()
        };
        String[] support = {
                riftHelperMainView.getComboBoxSupport1(), riftHelperMainView.getComboBoxSupport2(), riftHelperMainView.getComboBoxSupport3(),
                riftHelperMainView.getComboBoxSupport4(), riftHelperMainView.getComboBoxSupport5()
        };

        PreferenceManager.setAutoLockTopPriority(top);
        PreferenceManager.setAutoLockJunglePriority(jungle);
        PreferenceManager.setAutoLockMidPriority(mid);
        PreferenceManager.setAutoLockBotPriority(bot);
        PreferenceManager.setAutoLockSupportPriority(support);

        JOptionPane.showMessageDialog(riftHelperMainView, "Successfully saved!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startProgram() {
        // Initialize Variables
        this.autoAccept = false;
        this.autoSwap = false;
        this.priority = 10;
        this.autoReroll = false;

        // Store Preferences
        this.priorityChampions = PreferenceManager.getAutoSwapPriority();
        this.topChampions = PreferenceManager.getAutoSwapTopPriority();
        this.jungleChampions = PreferenceManager.getAutoSwapJunglePriority();
        this.midChampions = PreferenceManager.getAutoLockMidPriority();
        this.botChampions = PreferenceManager.getAutoLockBotPriority();
        this.supportChampions = PreferenceManager.getAutoLockSupportPriority();
        this.banChampions = PreferenceManager.getAutoBanPriority();
        this.arenaChampions = PreferenceManager.getAutoLockArenaPriority();
        this.banArenaChampions = PreferenceManager.getAutoBanArenaPriority();
        this.autoSwapSlots = PreferenceManager.getAutoSwapSlots();
        this.alwaysOnTop = PreferenceManager.getAlwaysOnTop();
        this.centerGUI = PreferenceManager.getCenterGUI();
        this.systemTray = PreferenceManager.getSystemTray();
        this.autoCheckUpdate = PreferenceManager.getAutoCheckUpdate();
        this.autoLockLaneChoice = PreferenceManager.getAutoLockLaneChoice();

        loadPreferences();
    }

    private void showTop() {
        this.riftHelperMainView.showTop();
        this.riftHelperMainView.hideJungle();
        this.riftHelperMainView.hideMid();
        this.riftHelperMainView.hideBot();
        this.riftHelperMainView.hideSupport();
    }

    private void showJungle() {
        this.riftHelperMainView.hideTop();
        this.riftHelperMainView.showJungle();
        this.riftHelperMainView.hideMid();
        this.riftHelperMainView.hideBot();
        this.riftHelperMainView.hideSupport();
    }

    private void showMid() {
        this.riftHelperMainView.hideTop();
        this.riftHelperMainView.hideJungle();
        this.riftHelperMainView.showMid();
        this.riftHelperMainView.hideBot();
        this.riftHelperMainView.hideSupport();
    }

    private void showBot() {
        this.riftHelperMainView.hideTop();
        this.riftHelperMainView.hideJungle();
        this.riftHelperMainView.hideMid();
        this.riftHelperMainView.showBot();
        this.riftHelperMainView.hideSupport();
    }

    private void showSupport() {
        this.riftHelperMainView.hideTop();
        this.riftHelperMainView.hideJungle();
        this.riftHelperMainView.hideMid();
        this.riftHelperMainView.hideBot();
        this.riftHelperMainView.showSupport();
    }

    private void loadPreferences() {
        updateAutoSwapSlots();
        this.riftHelperMainView.setAlwaysOnTop(alwaysOnTop);
        if (this.alwaysOnTop) {
            this.riftHelperMainView.buttonAlwaysOnTopEnable.setEnabled(false);
            this.riftHelperMainView.buttonAlwaysOnTopDisable.setEnabled(true);
        } else {
            this.riftHelperMainView.buttonAlwaysOnTopEnable.setEnabled(true);
            this.riftHelperMainView.buttonAlwaysOnTopDisable.setEnabled(false);
        }
        if (this.centerGUI) {
            this.riftHelperMainView.buttonCenterGUIEnable.setEnabled(false);
            this.riftHelperMainView.buttonCenterGUIDisable.setEnabled(true);
        } else {
            this.riftHelperMainView.buttonCenterGUIEnable.setEnabled(true);
            this.riftHelperMainView.buttonCenterGUIDisable.setEnabled(false);
        }
        this.riftHelperMainView.setComboBoxAutoSwapPriority(priorityChampions);
        this.riftHelperMainView.setComboBoxTopPriority(topChampions);
        this.riftHelperMainView.setComboBoxJunglePriority(jungleChampions);
        this.riftHelperMainView.setComboBoxMidPriority(midChampions);
        this.riftHelperMainView.setComboBoxBotPriority(botChampions);
        this.riftHelperMainView.setComboBoxSupportPriority(supportChampions);
        this.riftHelperMainView.setComboBoxAutoBanPriority(banChampions);
        this.riftHelperMainView.setComboBoxAutoLockArenaPriority(arenaChampions);
        this.riftHelperMainView.setComboBoxAutoBanArenaPriority(banArenaChampions);
        if (systemTray) {
            this.riftHelperMainView.enableSystemTray();
        } else {
            this.riftHelperMainView.disableSystemTray();
        }
        if (autoLockLaneChoice == 0) {
            showTop();
        } else if (autoLockLaneChoice == 1) {
            showJungle();
        } else if (autoLockLaneChoice == 2) {
            showMid();
        } else if (autoLockLaneChoice == 3) {
            showBot();
        } else if (autoLockLaneChoice == 4) {
            showSupport();
        }

        reInitialize();
        this.riftHelperMainView.setLocationRelativeTo(null);
        this.riftHelperMainView.setVisible(true);

        if (this.autoCheckUpdate) {
            this.riftHelperMainView.buttonAutoCheckUpdateEnable.setEnabled(false);
            this.riftHelperMainView.buttonAutoCheckUpdateDisable.setEnabled(true);

            System.out.println("Update Checked");
            UpdateChecker.checkForUpdate(this.riftHelperMainView);
        } else {
            this.riftHelperMainView.buttonAutoCheckUpdateEnable.setEnabled(true);
            this.riftHelperMainView.buttonAutoCheckUpdateDisable.setEnabled(false);
        }
    }

    public void autoReroll(int rerollsRemaining) {
        if (autoReroll) {
            if (rerollsRemaining > 0) {
                for (int i = 0; i < rerollsRemaining; i++) {
                    LCUPost.postToClient("/lol-champ-select/v1/session/my-selection/reroll");
                }
            }
        }
    }

    public void nameButtons(JButton[] buttons) {
        // Increase Champion Bench if more than 5
        if (benchChampions.size() > 5) {
            this.riftHelperMainView.panelQuickSwitchBench2.setVisible(true);

            reInitialize();
        }

        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setText(DDragonParser.getChampionName(benchChampions.get(i).getChampionId()));
        }
    }

    public void autoSwap() {
        if (autoSwap) {
            int[] autoSwapChampIdPriority = {
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority1()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority2()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority3()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority4()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority5()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority6()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority7()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority8()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority9()),
                DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority10())
            };

            for (int i = 0; i < autoSwapChampIdPriority.length; i++) {
                for (int j = 0; j < benchChampions.size(); j++) {
                    if ((benchChampions.get(j).getChampionId() == autoSwapChampIdPriority[i]) && priority > i) {
                        if (LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority[j]) == 204) {
                            priority = i;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void saveAutoSwap() {
        String[] comboBoxes = {
                riftHelperMainView.getComboBoxAutoSwapPriority1(), riftHelperMainView.getComboBoxAutoSwapPriority2(),
                riftHelperMainView.getComboBoxAutoSwapPriority3(), riftHelperMainView.getComboBoxAutoSwapPriority4(),
                riftHelperMainView.getComboBoxAutoSwapPriority5(), riftHelperMainView.getComboBoxAutoSwapPriority6(),
                riftHelperMainView.getComboBoxAutoSwapPriority7(), riftHelperMainView.getComboBoxAutoSwapPriority8(),
                riftHelperMainView.getComboBoxAutoSwapPriority9(), riftHelperMainView.getComboBoxAutoSwapPriority10()
        };

        PreferenceManager.setAutoSwapPriority(comboBoxes);

        JOptionPane.showMessageDialog(riftHelperMainView, "Successfully saved!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateAutoSwapSlots() {
        JLabel[] labels = this.riftHelperMainView.getAutoSwapPriorityLabels();

        JComboBox[] comboBoxes = this.riftHelperMainView.getAutoSwapPriorityComboBoxes();

        for (int i = 0; i < 10; i++) {
            if (i < autoSwapSlots) {
                labels[i].setVisible(true);
                comboBoxes[i].setVisible(true);
            } else {
                labels[i].setVisible(false);
                comboBoxes[i].setVisible(false);
                comboBoxes[i].setSelectedIndex(-1);
            }
        }

        reInitialize();
    }

    public void reset() {
        startProgram();

        reInitialize();
    }

    public void reInitialize() {
        riftHelperMainView.revalidate();
        riftHelperMainView.repaint();
        riftHelperMainView.pack();

        if (centerGUI) {
            riftHelperMainView.setLocationRelativeTo(null);
        }
    }

}

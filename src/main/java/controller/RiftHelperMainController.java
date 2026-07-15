package controller;

import model.*;
import no.stelar7.api.r4j.impl.lol.lcu.LCUSocketReader;
import view.ChampionPicker;
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
    private volatile boolean autoSwapPriority;
    private volatile boolean autoSwapSurvey;
    private volatile boolean benchCycling; // Troll Swap running; pauses auto-swap so they don't fight
    private volatile java.util.List<Integer> surveySwapIds = new java.util.ArrayList<>();
    private volatile int autoSwapSlots;
    private volatile int priority;
    private volatile boolean alwaysOnTop;
    private volatile boolean centerGUI;
    private volatile boolean systemTray;
    private volatile boolean autoCheckUpdate;
    private volatile int autoLockLaneChoice;
    private volatile boolean autoLockRank;
    private volatile boolean autoBan;
    private volatile boolean autoLockArena;
    private volatile boolean autoBanArena;
    private volatile boolean autoBanCrowdFavoriteArena;
    private volatile boolean autoBravery;
    private volatile boolean autoHonor;
    private volatile boolean autoSkipScreens;
    private volatile boolean groupAutoQueue;
    private volatile boolean soloAutoQueue;
    private volatile boolean autoMinimize;
    private volatile boolean notifyEnabled;
    private volatile String notifyTopic = "";
    private volatile boolean notifyMatchFound;
    private volatile boolean notifyChampPicked;
    private volatile boolean notifyChampPickedAram;
    private volatile boolean notifyChampSwapAram;
    private volatile boolean notifyChampBanned;
    private volatile boolean notifyOnlyWhenAway;
    private volatile int notifyIdleSeconds = 30;
    private volatile boolean notifyHonor;
    private volatile boolean notifyReturnedToLobby;
    private volatile boolean notifyAutoQueue;
    private volatile boolean notifyGameStarting;
    private volatile String lastGameflowPhase = "";
    private volatile boolean autoQueueArmed = false;
    // Notification dedup: champ-select session events repeat while an action is in progress, so fire
    // the pick/ban notification only once per champ select. Reset on ChampSelect entry.
    private volatile boolean notifiedPick = false;
    private volatile boolean notifiedBan = false;
    // Play Again is called in both PreEndOfGame and EndOfGame; notify "returned to lobby" once per
    // game. Reset when a new game starts (InProgress).
    private volatile boolean notifiedReturnToLobby = false;
    // Ready-check search events repeat for ~12s; notify "match found" once per search. Reset on
    // Matchmaking entry (re-arms for the next queue, and for a re-search after a decline).
    private volatile boolean notifiedMatchFound = false;
    // ARAM assigns a random champion; notify it once per champ select. Reset on ChampSelect entry.
    private volatile boolean notifiedAramPick = false;
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

        // Pass our own CIM-derived token+port so R4J uses them directly instead of its no-arg
        // constructor, which shells out to WMIC (removed on Win11 24H2+) and would fail.
        LCUSocketReader socketReader = new LCUSocketReader(LCUAuth.token, LCUAuth.port);
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

                int pickedId = -1;
                boolean braveryPicked = false;
                if (isInProgressPicking) {
                    pickedId = autoLock(endpointPicking, actionIdPicking, assignedPosition);   // SR/normal
                    braveryPicked = autoBravery(endpointPicking, actionIdPicking);             // Arena random
                    if (pickedId < 0) {
                        pickedId = autoLockArena(endpointPicking, actionIdPicking);            // Arena list
                    }
                }

                int bannedId = -1;
                if (isInProgressBanning) {
                    bannedId = autoBan(endpointBanning, actionIdBanning, assignedPosition);
                    if (bannedId < 0) {
                        bannedId = autoBanArena(endpointBanning, actionIdBanning);
                    }
                    if (bannedId < 0) {
                        bannedId = autoBanCrowdFavoriteArena(endpointBanning, actionIdBanning);
                    }
                }

                // Tuck the client away right after an automated pick/ban (only when that auto action
                // is actually on, so a manual pick is never minimized out from under the user).
                if ((isInProgressPicking && (autoLockRank || autoLockArena || autoBravery))
                        || (isInProgressBanning && (autoBan || autoBanArena || autoBanCrowdFavoriteArena))) {
                    dndMinimize();
                }

                // One notification per champ select for the pick and for the ban (session events
                // repeat while the action is in progress; notifiedPick/notifiedBan dedupe).
                if (isInProgressPicking && !notifiedPick && (autoLockRank || autoLockArena || autoBravery)) {
                    notifiedPick = true;
                    if (pickedId >= 0) {
                        String forLane = (assignedPosition == null || assignedPosition.isBlank())
                                ? "" : " for " + assignedPosition;
                        notify(notifyChampPicked, "Champion Picked",
                                "Locked in " + DDragonParser.getChampionName(pickedId) + forLane + ".", 3, "white_check_mark");
                    } else if (braveryPicked) {
                        notify(notifyChampPicked, "Champion Picked",
                                "Bravery pick locked. Champion is revealed in game.", 3, "game_die");
                    }
                }
                if (isInProgressBanning && !notifiedBan && (autoBan || autoBanArena || autoBanCrowdFavoriteArena)) {
                    notifiedBan = true;
                    String banMsg = bannedId >= 0
                            ? "Banned " + DDragonParser.getChampionName(bannedId) + "."
                            : "Ban locked in.";
                    notify(notifyChampBanned, "Champion Banned", banMsg, 3, "no_entry");
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
                    priority = Integer.MAX_VALUE;
                    this.riftHelperMainView.panelQuickSwitchBench2.setVisible(false);
                    this.riftHelperMainView.setAramCurrentChampion(null);
                    return;
                }

                // Show the champion the user currently has, live.
                int myChampId = localChampionId(myTeam);
                String myChampName = myChampId > 0 ? DDragonParser.getChampionName(myChampId) : null;
                SwingUtilities.invokeLater(() -> this.riftHelperMainView.setAramCurrentChampion(myChampName));

                // Notify the random champion the user was assigned (once per ARAM/Mayhem champ select).
                if (!notifiedAramPick && myChampId > 0) {
                    notifiedAramPick = true;
                    notify(notifyChampPickedAram, "Champion Picked (ARAM)",
                            "Your random champion: " + myChampName + ".", 3, "snowflake");
                }

                int swappedId = autoSwap();
                if (swappedId >= 0) {
                    notify(notifyChampSwapAram, "Champion Swap (ARAM)",
                            "Swapped to " + DDragonParser.getChampionName(swappedId) + ".", 3, "twisted_rightwards_arrows");
                }
                nameButtons(buttons);
            }
        });

        // Auto game-start loop: driven by gameflow-phase transitions (honor -> skip screens) plus
        // lobby-membership changes (re-check auto-queue when friends return). Always subscribed;
        // the toggles gate the actions, not the subscription.
        socketReader.subscribe("OnJsonApiEvent_lol-gameflow_v1_gameflow-phase", eventData -> {
            handleGameflowPhase(parseGameflowPhase(eventData));
        });

        socketReader.subscribe("OnJsonApiEvent_lol-lobby_v2_lobby", eventData -> {
            if ("Lobby".equals(lastGameflowPhase)) {
                evaluateAutoQueue();
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

        // Auto Swap Priority toggle (existing manual 10-slot list).
        this.riftHelperMainView.addAutoSwapEnableListener(e -> {
            autoSwapPriority = true;
            PreferenceManager.setAutoSwapPriorityEnabled(true);
            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapEnable.setEnabled(false);
                riftHelperMainView.buttonAutoSwapDisable.setEnabled(true);
            });
        });
        this.riftHelperMainView.addAutoSwapDisableListener(e -> {
            autoSwapPriority = false;
            PreferenceManager.setAutoSwapPriorityEnabled(false);
            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapEnable.setEnabled(true);
                riftHelperMainView.buttonAutoSwapDisable.setEnabled(false);
            });
        });

        // Auto Swap Survey toggle (survey-generated ranking).
        this.riftHelperMainView.addAutoSwapSurveyEnableListener(e -> {
            autoSwapSurvey = true;
            PreferenceManager.setAutoSwapSurveyEnabled(true);
            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapSurveyEnable.setEnabled(false);
                riftHelperMainView.buttonAutoSwapSurveyDisable.setEnabled(true);
            });
        });
        this.riftHelperMainView.addAutoSwapSurveyDisableListener(e -> {
            autoSwapSurvey = false;
            PreferenceManager.setAutoSwapSurveyEnabled(false);
            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapSurveyEnable.setEnabled(true);
                riftHelperMainView.buttonAutoSwapSurveyDisable.setEnabled(false);
            });
        });

        // Survey lifecycle buttons.
        this.riftHelperMainView.addSurveyStartListener(e -> openSurvey());
        this.riftHelperMainView.addSurveyRefineListener(e -> openSurvey());
        this.riftHelperMainView.addSurveyRedoListener(e -> {
            int r = JOptionPane.showConfirmDialog(riftHelperMainView,
                    "Completely redo your survey? This clears your current ranking.\n"
                            + "Revert to Original will still restore your last completed version.",
                    "Redo Survey", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                AramSurveyStore.deleteForRedo();
                openSurvey();
            }
        });
        this.riftHelperMainView.addSurveyRevertListener(e -> {
            int r = JOptionPane.showConfirmDialog(riftHelperMainView,
                    "Revert your survey to the original version? This discards later changes.",
                    "Revert to Original", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                AramSurveyData current = AramSurveyStore.load();
                AramSurveyStore.pushUndo(current);
                AramSurveyStore.save(AramSurveyStore.originalData());
                refreshSurveySwapIds();
                refreshAramTab();
            }
        });
        this.riftHelperMainView.addSurveyUndoListener(e -> {
            AramSurveyStore.undo(AramSurveyStore.load());
            refreshSurveySwapIds();
            refreshAramTab();
        });
        // Manual edit of the survey list (a picker changed or a swap happened).
        this.riftHelperMainView.addSurveyListChangeListener(() -> onSurveyListEdited());

        this.riftHelperMainView.addTrollSwapListener(e -> trollSwap());
        this.riftHelperMainView.addTrollSwapDelayChangeListener(() ->
                PreferenceManager.setTrollSwapDelayMs(this.riftHelperMainView.getTrollSwapDelayMs()));
        this.riftHelperMainView.addNotifyTutorialListener(e ->
                new view.NotifyTutorialDialog(riftHelperMainView,
                        riftHelperMainView::getNotifyTopic, riftHelperMainView::setNotifyTopic).open());

        // Hide / Show the League client window (backend keeps running). Off the EDT: tearing down or
        // relaunching the client UX can take a moment.
        this.riftHelperMainView.addHideClientListener(e ->
                new Thread(() -> LCUPost.postToClient("/riotclient/kill-ux"), "hide-client").start());
        this.riftHelperMainView.addShowClientListener(e ->
                new Thread(() -> LCUPost.postToClient("/riotclient/launch-ux"), "show-client").start());

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
            PreferenceManager.setAutoLock(true);
            System.out.println("Auto Lock Turned On: " + autoLockRank);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoLockEnable.setEnabled(false);
                riftHelperMainView.buttonAutoLockDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoLockDisableListener(e -> {
            autoLockRank = false;
            PreferenceManager.setAutoLock(false);
            System.out.println("Auto Lock Turned Off: " + autoLockRank);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoLockEnable.setEnabled(true);
                riftHelperMainView.buttonAutoLockDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoBanEnableListener(e -> {
            autoBan = true;
            PreferenceManager.setAutoBan(true);
            System.out.println("Auto Ban Turned On: " + autoBan);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBanDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBanDisableListener(e -> {
            autoBan = false;
            PreferenceManager.setAutoBan(false);
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

            // auto-saved on change; no confirmation dialog
        });

        this.riftHelperMainView.addAutoLockArenaEnableListener(e -> {
            if (autoBravery) {
                JOptionPane.showMessageDialog(riftHelperMainView, "Auto Bravery is enabled.", "Cannot Auto Lock (Arena)", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoLockArena = true;
            PreferenceManager.setAutoLockArena(true);
            System.out.println("Auto Lock (Arena) Turned On: " + autoLockArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoLockArenaEnable.setEnabled(false);
                riftHelperMainView.buttonAutoLockArenaDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoLockArenaDisableListener(e -> {
            autoLockArena = false;
            PreferenceManager.setAutoLockArena(false);
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

            // auto-saved on change; no confirmation dialog
        });

        this.riftHelperMainView.addAutoBanArenaEnableListener(e -> {
            if (autoBanCrowdFavoriteArena) {
                JOptionPane.showMessageDialog(riftHelperMainView, "Auto Ban (Arena) is enabled.", "Cannot Auto Ban Crowd Favorite (Arena)", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoBanArena = true;
            PreferenceManager.setAutoBanArena(true);
            System.out.println("Auto Ban (Arena) Turned On: " + autoBanArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanArenaEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBanArenaDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBanArenaDisableListener(e -> {
            autoBanArena = false;
            PreferenceManager.setAutoBanArena(false);
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

            // auto-saved on change; no confirmation dialog
        });

        this.riftHelperMainView.addAutoBanCrowdFavoriteEnableListener(e -> {
            if (autoBanArena) {
                JOptionPane.showMessageDialog(riftHelperMainView, "Auto Ban Crowd Favorite (Arena) is enabled.", "Cannot Auto Ban (Arena)", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoBanCrowdFavoriteArena = true;
            PreferenceManager.setAutoBanCrowdFavorite(true);
            System.out.println("Auto Ban Crowd Favorite (Arena) Turned On: " + autoBanCrowdFavoriteArena);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBanCrowdFavoriteEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBanCrowdFavoriteDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBanCrowdFavoriteDisableListener(e -> {
            autoBanCrowdFavoriteArena = false;
            PreferenceManager.setAutoBanCrowdFavorite(false);
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
            PreferenceManager.setAutoBravery(true);
            System.out.println("Auto Bravery (Arena) Turned On: " + autoBravery);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoBraveryArenaEnable.setEnabled(false);
                riftHelperMainView.buttonAutoBraveryArenaDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoBraveryArenaDisableListener(e -> {
            autoBravery = false;
            PreferenceManager.setAutoBravery(false);
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

        this.riftHelperMainView.addAutoHonorEnableListener(e -> setAutoHonor(true));
        this.riftHelperMainView.addAutoHonorDisableListener(e -> setAutoHonor(false));
        this.riftHelperMainView.addAutoSkipScreensEnableListener(e -> setAutoSkipScreens(true));
        this.riftHelperMainView.addAutoSkipScreensDisableListener(e -> setAutoSkipScreens(false));
        this.riftHelperMainView.addGroupAutoQueueEnableListener(e -> setGroupAutoQueue(true));
        this.riftHelperMainView.addGroupAutoQueueDisableListener(e -> setGroupAutoQueue(false));
        this.riftHelperMainView.addSoloAutoQueueEnableListener(e -> setSoloAutoQueue(true));
        this.riftHelperMainView.addSoloAutoQueueDisableListener(e -> setSoloAutoQueue(false));
        this.riftHelperMainView.addAutoMinimizeEnableListener(e -> setAutoMinimize(true));
        this.riftHelperMainView.addAutoMinimizeDisableListener(e -> setAutoMinimize(false));

        this.riftHelperMainView.addNotifyEnableListener(e -> setNotifyEnabled(true));
        this.riftHelperMainView.addNotifyDisableListener(e -> setNotifyEnabled(false));
        this.riftHelperMainView.addNotifyMatchFoundEnableListener(e -> setNotifyMatchFound(true));
        this.riftHelperMainView.addNotifyMatchFoundDisableListener(e -> setNotifyMatchFound(false));
        this.riftHelperMainView.addNotifyChampPickedEnableListener(e -> setNotifyChampPicked(true));
        this.riftHelperMainView.addNotifyChampPickedDisableListener(e -> setNotifyChampPicked(false));
        this.riftHelperMainView.addNotifyChampPickedAramEnableListener(e -> setNotifyChampPickedAram(true));
        this.riftHelperMainView.addNotifyChampPickedAramDisableListener(e -> setNotifyChampPickedAram(false));
        this.riftHelperMainView.addNotifyChampSwapAramEnableListener(e -> setNotifyChampSwapAram(true));
        this.riftHelperMainView.addNotifyChampSwapAramDisableListener(e -> setNotifyChampSwapAram(false));
        this.riftHelperMainView.addNotifyChampBannedEnableListener(e -> setNotifyChampBanned(true));
        this.riftHelperMainView.addNotifyChampBannedDisableListener(e -> setNotifyChampBanned(false));
        this.riftHelperMainView.addNotifyOnlyWhenAwayEnableListener(e -> setNotifyOnlyWhenAway(true));
        this.riftHelperMainView.addNotifyOnlyWhenAwayDisableListener(e -> setNotifyOnlyWhenAway(false));
        this.riftHelperMainView.addNotifyIdleSecondsChangeListener(() -> {
            notifyIdleSeconds = this.riftHelperMainView.getNotifyIdleSeconds();
            PreferenceManager.setNotifyIdleSeconds(notifyIdleSeconds);
        });
        this.riftHelperMainView.addUiScaleChangeListener(() ->
                PreferenceManager.setUiScalePercent(this.riftHelperMainView.getUiScalePercent()));
        this.riftHelperMainView.addUiScaleApplyListener(e -> {
            PreferenceManager.setUiScalePercent(this.riftHelperMainView.getUiScalePercent());
            main.RiftHelperMain.restart();
        });
        this.riftHelperMainView.addNotifyHonorEnableListener(e -> setNotifyHonor(true));
        this.riftHelperMainView.addNotifyHonorDisableListener(e -> setNotifyHonor(false));
        this.riftHelperMainView.addNotifyReturnedToLobbyEnableListener(e -> setNotifyReturnedToLobby(true));
        this.riftHelperMainView.addNotifyReturnedToLobbyDisableListener(e -> setNotifyReturnedToLobby(false));
        this.riftHelperMainView.addNotifyAutoQueueEnableListener(e -> setNotifyAutoQueue(true));
        this.riftHelperMainView.addNotifyAutoQueueDisableListener(e -> setNotifyAutoQueue(false));
        this.riftHelperMainView.addNotifyGameStartingEnableListener(e -> setNotifyGameStarting(true));
        this.riftHelperMainView.addNotifyGameStartingDisableListener(e -> setNotifyGameStarting(false));
        this.riftHelperMainView.addNotifyTopicChangeListener(() -> {
            notifyTopic = this.riftHelperMainView.getNotifyTopic();
            PreferenceManager.setNotifyTopic(notifyTopic);
        });

        // Restore persisted matchmaking toggles here (they subscribe to the websocket, which needs
        // socketReader - not available yet in loadPreferences during startProgram()).
        if (autoAccept) {
            autoAcceptEnable(socketReader);
        } else if (autoDecline) {
            autoDeclineEnable(socketReader);
        }
    }

    // ---- Auto game-start loop ----

    private void setAutoHonor(boolean on) {
        autoHonor = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoHonorEnable.setEnabled(!on);
            riftHelperMainView.buttonAutoHonorDisable.setEnabled(on);
        });
        PreferenceManager.setAutoHonor(on);
    }

    private void setAutoSkipScreens(boolean on) {
        autoSkipScreens = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoSkipScreensEnable.setEnabled(!on);
            riftHelperMainView.buttonAutoSkipScreensDisable.setEnabled(on);
        });
        PreferenceManager.setAutoSkipScreens(on);
    }

    private void setGroupAutoQueue(boolean on) {
        groupAutoQueue = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonGroupAutoQueueEnable.setEnabled(!on);
            riftHelperMainView.buttonGroupAutoQueueDisable.setEnabled(on);
        });
        PreferenceManager.setGroupAutoQueue(on);
        if (on && soloAutoQueue) {
            setSoloAutoQueue(false); // Group and Solo are mutually exclusive.
        }
    }

    private void setSoloAutoQueue(boolean on) {
        soloAutoQueue = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonSoloAutoQueueEnable.setEnabled(!on);
            riftHelperMainView.buttonSoloAutoQueueDisable.setEnabled(on);
        });
        PreferenceManager.setSoloAutoQueue(on);
        if (on && groupAutoQueue) {
            setGroupAutoQueue(false); // Group and Solo are mutually exclusive.
        }
    }

    private void setAutoMinimize(boolean on) {
        autoMinimize = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoMinimizeEnable.setEnabled(!on);
            riftHelperMainView.buttonAutoMinimizeDisable.setEnabled(on);
        });
        PreferenceManager.setAutoMinimize(on);
    }

    private void setNotifyEnabled(boolean on) {
        notifyEnabled = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyEnable, riftHelperMainView.buttonNotifyDisable, on);
        PreferenceManager.setNotifyEnabled(on);
    }

    private void setNotifyMatchFound(boolean on) {
        notifyMatchFound = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyMatchFoundEnable, riftHelperMainView.buttonNotifyMatchFoundDisable, on);
        PreferenceManager.setNotifyMatchFound(on);
    }

    private void setNotifyChampPicked(boolean on) {
        notifyChampPicked = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyChampPickedEnable, riftHelperMainView.buttonNotifyChampPickedDisable, on);
        PreferenceManager.setNotifyChampPicked(on);
    }

    private void setNotifyChampPickedAram(boolean on) {
        notifyChampPickedAram = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyChampPickedAramEnable, riftHelperMainView.buttonNotifyChampPickedAramDisable, on);
        PreferenceManager.setNotifyChampPickedAram(on);
    }

    private void setNotifyChampSwapAram(boolean on) {
        notifyChampSwapAram = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyChampSwapAramEnable, riftHelperMainView.buttonNotifyChampSwapAramDisable, on);
        PreferenceManager.setNotifyChampSwapAram(on);
    }

    private void setNotifyChampBanned(boolean on) {
        notifyChampBanned = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyChampBannedEnable, riftHelperMainView.buttonNotifyChampBannedDisable, on);
        PreferenceManager.setNotifyChampBanned(on);
    }

    private void setNotifyOnlyWhenAway(boolean on) {
        notifyOnlyWhenAway = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyOnlyWhenAwayEnable, riftHelperMainView.buttonNotifyOnlyWhenAwayDisable, on);
        PreferenceManager.setNotifyOnlyWhenAway(on);
    }

    private void setNotifyHonor(boolean on) {
        notifyHonor = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyHonorEnable, riftHelperMainView.buttonNotifyHonorDisable, on);
        PreferenceManager.setNotifyHonor(on);
    }

    private void setNotifyReturnedToLobby(boolean on) {
        notifyReturnedToLobby = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyReturnedToLobbyEnable, riftHelperMainView.buttonNotifyReturnedToLobbyDisable, on);
        PreferenceManager.setNotifyReturnedToLobby(on);
    }

    private void setNotifyAutoQueue(boolean on) {
        notifyAutoQueue = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyAutoQueueEnable, riftHelperMainView.buttonNotifyAutoQueueDisable, on);
        PreferenceManager.setNotifyAutoQueue(on);
    }

    private void setNotifyGameStarting(boolean on) {
        notifyGameStarting = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyGameStartingEnable, riftHelperMainView.buttonNotifyGameStartingDisable, on);
        PreferenceManager.setNotifyGameStarting(on);
    }

    private void applyNotifyButtons(JButton enable, JButton disable, boolean on) {
        SwingUtilities.invokeLater(() -> {
            enable.setEnabled(!on);
            disable.setEnabled(on);
        });
    }

    /** Publish one ntfy notification, gated by the master switch, the per-event toggle, and a topic.
     *  Runs off-thread inside {@link Ntfy}, so it never stalls the socket event handlers. */
    private void notify(boolean eventEnabled, String title, String message, int priority, String tags) {
        if (!notifyEnabled || !eventEnabled) {
            return;
        }
        // "Only notify when away": suppress while the user is actively using the PC or watching a
        // fullscreen video. Cheap native check (JNA), fails open. See WindowsPresence.
        if (notifyOnlyWhenAway && !WindowsPresence.shouldNotify(notifyIdleSeconds * 1000L)) {
            return;
        }
        Ntfy.publish(notifyTopic, title, message, priority, tags);
    }

    /** Minimize the League client, but only when Auto Minimize (DND) is on. Called right after an
     *  automated action so the client is tucked away at the exact moment it would pop up. */
    private void dndMinimize() {
        if (autoMinimize) {
            ClientWindow.minimize();
        }
    }

    private static String parseGameflowPhase(String eventData) {
        try {
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(eventData).getAsJsonObject();
            return root.getAsJsonObject("OnJsonApiEvent_lol-gameflow_v1_gameflow-phase").get("data").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private void handleGameflowPhase(String phase) {
        if (phase == null || phase.equals(lastGameflowPhase)) {
            return; // act only on transitions; the event can repeat within a phase
        }
        lastGameflowPhase = phase;
        System.out.println("Gameflow phase: " + phase);

        // Poll for bench swaps only while in champ select; stop the instant we leave it.
        if ("ChampSelect".equals(phase)) {
            startSwapPoll();
        } else {
            stopSwapPoll();
        }

        switch (phase) {
            case "ChampSelect" -> {
                autoQueueArmed = false;
                notifiedPick = false;
                notifiedBan = false;
                notifiedAramPick = false;
            }
            case "Matchmaking" -> {
                autoQueueArmed = false;
                notifiedMatchFound = false;
            }
            case "InProgress" -> {
                autoQueueArmed = false;
                notifiedReturnToLobby = false;
                notify(notifyGameStarting, "Game Starting", "Loading into the game.", 4, "rocket");
            }
            case "PreEndOfGame" -> {
                if (autoHonor) {
                    int honored = Honor.honorFriends();
                    dndMinimize();
                    if (honored > 0) {
                        notify(notifyHonor, "Honor Cast",
                                "Honored " + honored + (honored == 1 ? " friend." : " friends."), 2, "handshake");
                    }
                }
                // Arena (and some modes) never emit EndOfGame; they go PreEndOfGame -> Lobby. So try
                // Play Again here too. It returns the party to the lobby (keeps the group).
                if (autoSkipScreens) {
                    Lobby.playAgain();
                    notifyReturnedToLobbyOnce();
                }
            }
            case "EndOfGame" -> {
                if (autoSkipScreens) {
                    Lobby.playAgain();
                    notifyReturnedToLobbyOnce();
                }
            }
            case "Lobby" -> {
                autoQueueArmed = true;
                dndMinimize(); // minimize the moment a lobby is created (DND)
                evaluateAutoQueue();
            }
            default -> autoQueueArmed = false;
        }
    }

    // Notify "returned to lobby" at most once per game (Play Again runs in both PreEndOfGame and
    // EndOfGame). Reset on the next InProgress.
    private void notifyReturnedToLobbyOnce() {
        if (notifiedReturnToLobby) {
            return;
        }
        notifiedReturnToLobby = true;
        notify(notifyReturnedToLobby, "Back in Lobby", "Returned to the lobby after the game.", 2, "arrows_counterclockwise");
    }

    // Fires at most once per Lobby entry (autoQueueArmed), and only from the Lobby phase.
    private void evaluateAutoQueue() {
        if (!autoQueueArmed || !(soloAutoQueue || groupAutoQueue)) {
            return;
        }
        int members = Lobby.memberCount();
        boolean go = (soloAutoQueue && members == 1) || (groupAutoQueue && members >= 2);
        if (go) {
            autoQueueArmed = false;
            Lobby.startSearch();
            dndMinimize();
            String mode = soloAutoQueue ? "solo" : "group (" + members + " members)";
            notify(notifyAutoQueue, "Queue Started", "Started searching for a match: " + mode + ".", 3, "mag");
            System.out.println("Auto queue started (members=" + members + ")");
        }
    }

    private void autoDeclineDisable(LCUSocketReader socketReader) {
        autoDecline = false;
        PreferenceManager.setAutoDecline(false);

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
        PreferenceManager.setAutoDecline(true);

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
        PreferenceManager.setAutoAccept(false);
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
        PreferenceManager.setAutoAccept(true);
        System.out.println("Auto Accept Turned On: " + autoAccept);

        riftHelperMainView.buttonAutoAcceptEnable.setEnabled(false);
        riftHelperMainView.buttonAutoAcceptDisable.setEnabled(true);

        socketReader.subscribe("OnJsonApiEvent_lol-matchmaking_v1_search", eventData -> {
            Matchmaking matchmaking = Matchmaking.parseFromJson(eventData);
            ReadyCheck readyCheck = matchmaking.getReadyCheck();

            if (readyCheck.getState().equals("InProgress")) {
                LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");
                dndMinimize();
                if (!notifiedMatchFound) {
                    notifiedMatchFound = true;
                    notify(notifyMatchFound, "Match Found", "Ready check accepted.", 4, "video_game");
                }
            }
        });
    }

    /** Returns the champion id locked (via the priority list), or -1 if nothing was locked. */
    private int autoLock(String endpoint, int actionId, String assignedPosition) {
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
                            return i;
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
                            return i;
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
                            return i;
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
                            return i;
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
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /** Returns the champion id banned (via the priority list), or -1 if nothing was banned. */
    private int autoBan(String endpoint, int actionId, String assignedPosition) {
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
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /** Returns true if a Bravery (random) pick was locked. The champion is unknown until in-game. */
    public boolean autoBravery(String endpoint, int actionId) {
        if (autoBravery) {
            LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, -3));
            return true;
        }
        return false;
    }

    /** Returns the Arena champion id locked (via the priority list), or -1 if nothing was locked. */
    public int autoLockArena(String endpoint, int actionId) {
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
                    return i;
                }
            }
        }
        return -1;
    }

    /** Returns the Arena champion id banned (via the priority list), or -1 if nothing was banned. */
    public int autoBanArena(String endpoint, int actionId) {
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
                    return i;
                }
            }
        }
        return -1;
    }

    /** Returns the crowd-favorite champion id banned, or -1 if nothing was banned. */
    public int autoBanCrowdFavoriteArena(String endpoint, int actionId) {
        if (autoBanCrowdFavoriteArena) {
            int[] banPriority = GsonParser.parseFromJsonIntArray(LCUGet.getFromClient("/lol-lobby-team-builder/champ-select/v1/crowd-favorte-champion-list"));

            for (int i : banPriority) {
                if (LCUPatch.patchToClientWithBody(endpoint, jsonBodyAutoChoose(actionId, i)) == 204) {
                    return i;
                }
            }
        }
        return -1;
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

        // auto-saved on change; no confirmation dialog
    }

    private void startProgram() {
        // Initialize Variables (priority = best auto-swap rank reached this champ select; high = none yet)
        this.priority = Integer.MAX_VALUE;

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
        this.autoHonor = PreferenceManager.getAutoHonor();
        this.autoSkipScreens = PreferenceManager.getAutoSkipScreens();
        this.groupAutoQueue = PreferenceManager.getGroupAutoQueue();
        this.soloAutoQueue = PreferenceManager.getSoloAutoQueue();
        this.autoMinimize = PreferenceManager.getAutoMinimize();
        this.notifyEnabled = PreferenceManager.getNotifyEnabled();
        this.notifyTopic = PreferenceManager.getNotifyTopic();
        this.notifyMatchFound = PreferenceManager.getNotifyMatchFound();
        this.notifyChampPicked = PreferenceManager.getNotifyChampPicked();
        this.notifyChampPickedAram = PreferenceManager.getNotifyChampPickedAram();
        this.notifyChampSwapAram = PreferenceManager.getNotifyChampSwapAram();
        this.notifyChampBanned = PreferenceManager.getNotifyChampBanned();
        this.notifyOnlyWhenAway = PreferenceManager.getNotifyOnlyWhenAway();
        this.notifyIdleSeconds = PreferenceManager.getNotifyIdleSeconds();
        this.notifyHonor = PreferenceManager.getNotifyHonor();
        this.notifyReturnedToLobby = PreferenceManager.getNotifyReturnedToLobby();
        this.notifyAutoQueue = PreferenceManager.getNotifyAutoQueue();
        this.notifyGameStarting = PreferenceManager.getNotifyGameStarting();
        this.autoLockLaneChoice = PreferenceManager.getAutoLockLaneChoice();
        this.autoAccept = PreferenceManager.getAutoAccept();
        this.autoDecline = PreferenceManager.getAutoDecline();
        this.autoSwapPriority = PreferenceManager.getAutoSwapPriorityEnabled();
        this.autoSwapSurvey = PreferenceManager.getAutoSwapSurveyEnabled();
        this.autoLockRank = PreferenceManager.getAutoLock();
        this.autoBan = PreferenceManager.getAutoBan();
        this.autoLockArena = PreferenceManager.getAutoLockArena();
        this.autoBanArena = PreferenceManager.getAutoBanArena();
        this.autoBanCrowdFavoriteArena = PreferenceManager.getAutoBanCrowdFavorite();
        this.autoBravery = PreferenceManager.getAutoBravery();

        refreshSurveySwapIds();

        loadPreferences();
        refreshAramTab();
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

        this.riftHelperMainView.buttonAutoHonorEnable.setEnabled(!autoHonor);
        this.riftHelperMainView.buttonAutoHonorDisable.setEnabled(autoHonor);
        this.riftHelperMainView.buttonAutoSkipScreensEnable.setEnabled(!autoSkipScreens);
        this.riftHelperMainView.buttonAutoSkipScreensDisable.setEnabled(autoSkipScreens);
        if (groupAutoQueue && soloAutoQueue) { // safety: the two are mutually exclusive
            soloAutoQueue = false;
            PreferenceManager.setSoloAutoQueue(false);
        }
        this.riftHelperMainView.buttonGroupAutoQueueEnable.setEnabled(!groupAutoQueue);
        this.riftHelperMainView.buttonGroupAutoQueueDisable.setEnabled(groupAutoQueue);
        this.riftHelperMainView.buttonSoloAutoQueueEnable.setEnabled(!soloAutoQueue);
        this.riftHelperMainView.buttonSoloAutoQueueDisable.setEnabled(soloAutoQueue);
        this.riftHelperMainView.buttonAutoMinimizeEnable.setEnabled(!autoMinimize);
        this.riftHelperMainView.buttonAutoMinimizeDisable.setEnabled(autoMinimize);
        this.riftHelperMainView.setNotifyTopic(notifyTopic);
        this.riftHelperMainView.setNotifyIdleSeconds(notifyIdleSeconds);
        this.riftHelperMainView.setUiScalePercent(PreferenceManager.getUiScalePercent());
        this.riftHelperMainView.setTrollSwapDelayMs(PreferenceManager.getTrollSwapDelayMs());
        applyToggleButtons(riftHelperMainView.buttonNotifyEnable, riftHelperMainView.buttonNotifyDisable, notifyEnabled);
        applyToggleButtons(riftHelperMainView.buttonNotifyOnlyWhenAwayEnable, riftHelperMainView.buttonNotifyOnlyWhenAwayDisable, notifyOnlyWhenAway);
        applyToggleButtons(riftHelperMainView.buttonNotifyMatchFoundEnable, riftHelperMainView.buttonNotifyMatchFoundDisable, notifyMatchFound);
        applyToggleButtons(riftHelperMainView.buttonNotifyChampPickedEnable, riftHelperMainView.buttonNotifyChampPickedDisable, notifyChampPicked);
        applyToggleButtons(riftHelperMainView.buttonNotifyChampPickedAramEnable, riftHelperMainView.buttonNotifyChampPickedAramDisable, notifyChampPickedAram);
        applyToggleButtons(riftHelperMainView.buttonNotifyChampSwapAramEnable, riftHelperMainView.buttonNotifyChampSwapAramDisable, notifyChampSwapAram);
        applyToggleButtons(riftHelperMainView.buttonNotifyChampBannedEnable, riftHelperMainView.buttonNotifyChampBannedDisable, notifyChampBanned);
        applyToggleButtons(riftHelperMainView.buttonNotifyHonorEnable, riftHelperMainView.buttonNotifyHonorDisable, notifyHonor);
        applyToggleButtons(riftHelperMainView.buttonNotifyReturnedToLobbyEnable, riftHelperMainView.buttonNotifyReturnedToLobbyDisable, notifyReturnedToLobby);
        applyToggleButtons(riftHelperMainView.buttonNotifyAutoQueueEnable, riftHelperMainView.buttonNotifyAutoQueueDisable, notifyAutoQueue);
        applyToggleButtons(riftHelperMainView.buttonNotifyGameStartingEnable, riftHelperMainView.buttonNotifyGameStartingDisable, notifyGameStarting);
        applyToggleButtons(riftHelperMainView.buttonAutoLockEnable, riftHelperMainView.buttonAutoLockDisable, autoLockRank);
        applyToggleButtons(riftHelperMainView.buttonAutoBanEnable, riftHelperMainView.buttonAutoBanDisable, autoBan);
        applyToggleButtons(riftHelperMainView.buttonAutoLockArenaEnable, riftHelperMainView.buttonAutoLockArenaDisable, autoLockArena);
        applyToggleButtons(riftHelperMainView.buttonAutoBanArenaEnable, riftHelperMainView.buttonAutoBanArenaDisable, autoBanArena);
        applyToggleButtons(riftHelperMainView.buttonAutoBanCrowdFavoriteEnable, riftHelperMainView.buttonAutoBanCrowdFavoriteDisable, autoBanCrowdFavoriteArena);
        applyToggleButtons(riftHelperMainView.buttonAutoBraveryArenaEnable, riftHelperMainView.buttonAutoBraveryArenaDisable, autoBravery);
        applyToggleButtons(riftHelperMainView.buttonAutoSwapEnable, riftHelperMainView.buttonAutoSwapDisable, autoSwapPriority);
        applyToggleButtons(riftHelperMainView.buttonAutoSwapSurveyEnable, riftHelperMainView.buttonAutoSwapSurveyDisable, autoSwapSurvey);
    }

    private void applyToggleButtons(javax.swing.JButton enable, javax.swing.JButton disable, boolean on) {
        enable.setEnabled(!on);
        disable.setEnabled(on);
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

    /** Returns the champion id swapped in from the bench, or -1 if no swap happened. Combines the
     *  manual Priority list (first) and the survey-generated list (second), no dedup, and swaps to
     *  the highest-ranked available bench champion that improves on the best rank reached so far. */
    public int autoSwap() {
        java.util.List<Integer> benchIds = new java.util.ArrayList<>();
        if (benchChampions != null) {
            for (BenchChampions b : benchChampions) {
                benchIds.add(b.getChampionId());
            }
        }
        return tryAutoSwap(benchIds);
    }

    /**
     * Core swap decision, shared by the champ-select event handler and the poll loop. Synchronized
     * so the two threads can never interleave a swap. Builds the combined Priority-then-Survey order
     * (no dedup) and swaps to the best available bench champion that improves on the best rank
     * reached so far ({@code priority}). Returns the swapped champion id, or -1.
     */
    private synchronized int tryAutoSwap(java.util.List<Integer> benchIds) {
        if (benchCycling || (!autoSwapPriority && !autoSwapSurvey) || benchIds == null || benchIds.isEmpty()) {
            return -1;
        }
        int[] priorityIds = {
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
        java.util.List<Integer> order = AutoSwapPlanner.buildOrder(
                autoSwapPriority ? priorityIds : new int[0],
                autoSwapSurvey ? surveySwapIds : java.util.List.of());
        int[] out = {priority};
        int target = AutoSwapPlanner.pickBenchTarget(order, benchIds, priority, out);
        if (target > 0 && LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + target) == 204) {
            priority = out[0];
            return target;
        }
        return -1;
    }

    // ---- Auto-swap poll: during ARAM champ select only, retry grabbing the best bench champ the
    //      moment it frees (contested benches are a race; events can lag/coalesce). Bounded to the
    //      bench phase so nothing polls in the background. ----
    private final java.util.concurrent.ScheduledExecutorService swapPoll =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "aram-swap-poll");
                t.setDaemon(true);
                return t;
            });
    private volatile java.util.concurrent.ScheduledFuture<?> swapPollTask;

    private void startSwapPoll() {
        if (swapPollTask != null && !swapPollTask.isDone()) {
            return; // already polling
        }
        swapPollTask = swapPoll.scheduleWithFixedDelay(this::pollSwapTick, 300, 750,
                java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void stopSwapPoll() {
        java.util.concurrent.ScheduledFuture<?> t = swapPollTask;
        if (t != null) {
            t.cancel(false);
        }
        swapPollTask = null;
    }

    /** One poll: read the live champ-select session, and if it is a bench (rerolling) mode, try a
     *  swap off the freshest bench. Fully exception-tolerant so the loop never dies. */
    private void pollSwapTick() {
        try {
            if (!autoSwapPriority && !autoSwapSurvey) {
                return;
            }
            Session s = Session.parseFromRaw(LCUGet.getFromClient("/lol-champ-select/v1/session"));
            if (s == null || !s.isAllowRerolling()) {
                return;
            }
            userCellId = s.getLocalPlayerCellId();
            java.util.List<Integer> benchIds = new java.util.ArrayList<>();
            if (s.getBenchChampions() != null) {
                for (BenchChampions b : s.getBenchChampions()) {
                    benchIds.add(b.getChampionId());
                }
            }
            tryAutoSwap(benchIds);
        } catch (Exception e) {
            System.out.println("[SwapPoll] " + e.getMessage());
        }
    }

    /** Troll Swap: one-shot cosmetic cycle. Swaps to each bench champion (first to last) then back
     *  to the champion you started on. Runs on a daemon thread; pauses auto-swap while it runs so
     *  they do not fight. Purely visual; the client's swap cooldown may drop steps at low delays. */
    private void trollSwap() {
        if (benchCycling) {
            return; // already running
        }
        final int delayMs = riftHelperMainView.getTrollSwapDelayMs();
        Thread t = new Thread(() -> {
            benchCycling = true;
            try {
                Session s = Session.parseFromRaw(LCUGet.getFromClient("/lol-champ-select/v1/session"));
                if (s == null || !s.isAllowRerolling()) {
                    return;
                }
                userCellId = s.getLocalPlayerCellId();
                int original = localChampionId(s.getMyTeam());
                java.util.List<Integer> bench = new java.util.ArrayList<>();
                if (s.getBenchChampions() != null) {
                    for (BenchChampions b : s.getBenchChampions()) {
                        bench.add(b.getChampionId());
                    }
                }
                if (original <= 0 || bench.isEmpty()) {
                    return;
                }
                for (int id : bench) {
                    LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + id);
                    sleepQuietly(delayMs);
                }
                LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + original); // back to start
            } catch (Exception e) {
                System.out.println("[TrollSwap] " + e.getMessage());
            } finally {
                benchCycling = false;
            }
        }, "troll-swap");
        t.setDaemon(true);
        t.start();
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Load the survey's swap order (flatOrder) into champion ids for auto-swap. */
    private void refreshSurveySwapIds() {
        AramSurveyData d = AramSurveyStore.load();
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        for (String name : surveyListNames(d)) {
            int id = DDragonParser.getChampionId(name);
            if (id > 0) {
                ids.add(id);
            }
        }
        surveySwapIds = ids;
    }

    /** The ordered survey champion names for swapping/display (flatOrder, synced to current tiers). */
    private java.util.List<String> surveyListNames(AramSurveyData d) {
        d.syncFlatOrder();
        return new java.util.ArrayList<>(d.flatOrder);
    }

    /** Open (start / resume / refine) the survey dialog; refresh everything when it closes. */
    private void openSurvey() {
        new view.SurveyDialog(riftHelperMainView, () -> {
            AramSurveyData d = AramSurveyStore.load();
            d.syncFlatOrder();
            AramSurveyStore.save(d);
            refreshSurveySwapIds();
            refreshAramTab();
        }).openResume();
    }

    /** The user reordered/swapped/edited the Survey list in the ARAM tab. */
    private void onSurveyListEdited() {
        AramSurveyData d = AramSurveyStore.load();
        AramSurveyStore.pushUndo(d);
        AramSurveyData edited = d.deepCopy();
        edited.flatOrder = new java.util.ArrayList<>(java.util.Arrays.asList(riftHelperMainView.getSurveyList()));
        AramSurveyStore.save(edited);
        refreshSurveySwapIds();
        refreshAramTab();
    }

    /** Recompute onboarding state, metric, survey list, and revert/undo enablement. */
    private void refreshAramTab() {
        AramSurveyData d = AramSurveyStore.load();
        int total = DDragonParser.championPoolSize();
        int decided = d.decidedCount();
        String state = !AramSurveyStore.exists() ? "none"
                : (total > 0 && decided >= total ? "done" : "partial");
        java.util.List<String> names = surveyListNames(d);
        boolean revert = AramSurveyStore.isModifiedFromOriginal(d);
        boolean undo = AramSurveyStore.canUndo();
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.setSurveyOnboarding(state, decided, total);
            riftHelperMainView.setSurveyMetric(decided, total);
            riftHelperMainView.setSurveyList(names.toArray(new String[0]));
            riftHelperMainView.setSurveyRevertVisible(revert);
            riftHelperMainView.setSurveyUndoEnabled(undo);
        });
    }

    /** The local player's assigned champion id from myTeam (0 if not present yet). */
    private int localChampionId(List<MyTeam> myTeam) {
        if (myTeam == null) {
            return 0;
        }
        for (MyTeam m : myTeam) {
            if (m.getCellId() == userCellId) {
                return m.getChampionId();
            }
        }
        return 0;
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

        // auto-saved on change; no confirmation dialog
    }

    private void updateAutoSwapSlots() {
        JLabel[] labels = this.riftHelperMainView.getAutoSwapPriorityLabels();

        ChampionPicker[] comboBoxes = this.riftHelperMainView.getAutoSwapPriorityComboBoxes();

        for (int i = 0; i < 10; i++) {
            if (i < autoSwapSlots) {
                labels[i].setVisible(true);
                comboBoxes[i].setVisible(true);
            } else {
                labels[i].setVisible(false);
                comboBoxes[i].setVisible(false);
                comboBoxes[i].clearSelection();
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

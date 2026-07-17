package controller;

import model.*;
import no.stelar7.api.r4j.impl.lol.lcu.LCUSocketReader;
import view.AramSwapToolsOverlayPanel;
import view.ChampionPicker;
import view.ClientOverlay;
import view.FileChooserView;
import view.LobbyOverlayPanel;
import view.QuickSwitchBenchOverlayPanel;
import view.RiftHelperMainView;
import view.Top5OverlayPanel;

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
    private volatile boolean autoPickCard; // 2026 ARAM: auto-pick the best offered champion card
    private volatile boolean autoTrade;    // ARAM teammate trades: accept upgrades, decline rest, request best
    private volatile boolean benchCycling; // Troll Swap running; pauses auto-swap so they don't fight
    private volatile java.util.List<Integer> surveySwapIds = new java.util.ArrayList<>();
    private volatile int autoSwapSlots;
    // Troll Swap toggle: infinite bench cycle that bails ~1s before lock, back to the champ you started on.
    private volatile boolean trollToggle;
    private volatile int trollOriginal;
    private volatile int trollBenchIndex;
    private volatile long trollLastSwapAt;
    private volatile java.util.List<Integer> trollRotation; // fixed order (original + bench), captured per run
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
    private volatile boolean autoClaimPasses; // auto-claim all event/battlepass rewards (clears notifications)
    private volatile boolean autoMinimize;
    private volatile boolean lobbyOverlay; // draw the lobby controls on the client during the lobby
    private volatile boolean aramBenchOverlay;
    // (overlay windows are tracked in the `overlays` list below)
    private volatile boolean aramSwapToolsOverlay;
    private volatile boolean aramTop5Overlay;
    private volatile boolean benchModeActive; // in ARAM/reroll champ select right now (bench present)
    private final java.util.List<ClientOverlay> overlays = new java.util.ArrayList<>();
    private QuickSwitchBenchOverlayPanel benchOverlayPanel;
    private Top5OverlayPanel top5OverlayPanel;
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
    private volatile boolean notifyPlayedRecently; // push when someone was in your last 3 games
    private volatile boolean scoutEnabled;         // Players tab: allow background player-intel lookups
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
    // ARAM card auto-pick fires once per champ select. Reset on ChampSelect entry.
    private volatile boolean cardPicked = false;
    // Diagnostic: dump the raw session up to a few times per champ select while no offered cards are
    // found, so a live ARAM reveals the true shape if the primary source ever changes.
    private volatile int cardDumpCount = 0;
    // Champions we've already REQUESTED a swap for this champ select. Keyed by target champion (NOT the
    // swap id) because the LCU re-issues swap ids as champ state changes; keying by id caused the poll
    // to spam a fresh request every tick. Reset on ChampSelect entry.
    private final java.util.Set<Integer> requestedSwapChamps = java.util.concurrent.ConcurrentHashMap.newKeySet();
    // Deduped diagnostics so the 750ms poll logs a line only when the decision/inputs actually change.
    private volatile String lastSwapDiag = "";
    private volatile String lastTradeDiag = "";
    // "Played recently" push fires once per game. Reset on ChampSelect entry.
    private volatile boolean notifiedPlayedRecently = false;
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

                int swappedId = autoSwap(myChampId);
                if (swappedId >= 0) {
                    notify(notifyChampSwapAram, "Champion Swap (ARAM)",
                            "Swapped to " + DDragonParser.getChampionName(swappedId) + ".", 3, "twisted_rightwards_arrows");
                }
                nameButtons(buttons);

                // ARAM overlays: we are in a bench (reroll) champ select. Keep the overlay bench and the
                // Top 5 Ranked view live.
                benchModeActive = true;
                java.util.List<Integer> benchIds = new java.util.ArrayList<>();
                if (benchChampions != null) {
                    for (BenchChampions bc : benchChampions) {
                        benchIds.add(bc.getChampionId());
                    }
                }
                java.util.List<RankedChoice> top5 = computeTop5(myChampId, benchIds);
                SwingUtilities.invokeLater(() -> {
                    riftHelperMainView.setTop5(top5);
                    if (top5OverlayPanel != null) {
                        top5OverlayPanel.setChoices(top5);
                    }
                    if (benchOverlayPanel != null) {
                        benchOverlayPanel.refresh();
                    }
                });
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

        // Auto Pick Card toggle (2026 ARAM champion-card offer).
        this.riftHelperMainView.addAutoPickCardEnableListener(e -> setAutoPickCard(true));
        this.riftHelperMainView.addAutoPickCardDisableListener(e -> setAutoPickCard(false));

        // Auto Trade toggle (ARAM teammate trades).
        this.riftHelperMainView.addAutoTradeEnableListener(e -> setAutoTrade(true));
        this.riftHelperMainView.addAutoTradeDisableListener(e -> setAutoTrade(false));

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
        this.riftHelperMainView.addTrollSwapLoopsChangeListener(() ->
                PreferenceManager.setTrollSwapLoops(this.riftHelperMainView.getTrollSwapLoops()));
        this.riftHelperMainView.addTrollSwapToggleEnableListener(e -> enableTrollToggle());
        this.riftHelperMainView.addTrollSwapToggleDisableListener(e -> disableTrollToggle(true));
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
        this.riftHelperMainView.addAutoClaimPassesEnableListener(e -> setAutoClaimPasses(true));
        this.riftHelperMainView.addAutoClaimPassesDisableListener(e -> setAutoClaimPasses(false));
        this.riftHelperMainView.addAutoMinimizeEnableListener(e -> setAutoMinimize(true));
        this.riftHelperMainView.addAutoMinimizeDisableListener(e -> setAutoMinimize(false));
        this.riftHelperMainView.addLobbyOverlayEnableListener(e -> setLobbyOverlay(true));
        this.riftHelperMainView.addLobbyOverlayDisableListener(e -> setLobbyOverlay(false));
        this.riftHelperMainView.addAramBenchOverlayEnableListener(e -> setAramBenchOverlay(true));
        this.riftHelperMainView.addAramBenchOverlayDisableListener(e -> setAramBenchOverlay(false));
        this.riftHelperMainView.addAramSwapToolsOverlayEnableListener(e -> setAramSwapToolsOverlay(true));
        this.riftHelperMainView.addAramSwapToolsOverlayDisableListener(e -> setAramSwapToolsOverlay(false));
        this.riftHelperMainView.addAramTop5OverlayEnableListener(e -> setAramTop5Overlay(true));
        this.riftHelperMainView.addAramTop5OverlayDisableListener(e -> setAramTop5Overlay(false));
        this.riftHelperMainView.addOverlayOpacityChangeListener(() -> {
            PreferenceManager.setOverlayOpacity(riftHelperMainView.getOverlayOpacity());
            applyOverlayOpacities();
        });
        this.riftHelperMainView.addOverlayHoverOpacityChangeListener(() -> {
            PreferenceManager.setOverlayHoverOpacity(riftHelperMainView.getOverlayHoverOpacity());
            applyOverlayOpacities();
        });
        this.riftHelperMainView.addOverlayKeybindChangeListener(() -> {
            int[] k = riftHelperMainView.getOverlayDragKeys();
            PreferenceManager.setOverlayDragKeys(k);
            ClientOverlay.setDragKeys(k);
        });

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
        this.riftHelperMainView.addNotifyPlayedRecentlyEnableListener(e -> setNotifyPlayedRecently(true));
        this.riftHelperMainView.addNotifyPlayedRecentlyDisableListener(e -> setNotifyPlayedRecently(false));

        // Players (scout) tab: manual refresh + background-lookup toggle.
        this.riftHelperMainView.addScoutRefreshListener(e -> scoutRefresh());
        this.riftHelperMainView.addScoutEnableListener(e -> setScoutEnabled(true));
        this.riftHelperMainView.addScoutDisableListener(e -> setScoutEnabled(false));
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

    private void setAutoClaimPasses(boolean on) {
        autoClaimPasses = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoClaimPassesEnable.setEnabled(!on);
            riftHelperMainView.buttonAutoClaimPassesDisable.setEnabled(on);
        });
        PreferenceManager.setAutoClaimPasses(on);
        if (on) {
            claimPassesAsync(); // sweep immediately when enabled
        }
    }

    /** Claim all event/battlepass rewards off the EDT (LCU HTTP). No-op unless enabled. */
    private void claimPassesAsync() {
        if (!autoClaimPasses) {
            return;
        }
        Thread t = new Thread(() -> {
            int n = Rewards.claimAllPasses();
            if (n > 0) {
                System.out.println("[AutoClaim] claimed rewards on " + n + " pass(es).");
            }
        }, "auto-claim-passes");
        t.setDaemon(true);
        t.start();
    }

    private void setAutoPickCard(boolean on) {
        autoPickCard = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoPickCardEnable.setEnabled(!on);
            riftHelperMainView.buttonAutoPickCardDisable.setEnabled(on);
        });
        PreferenceManager.setAutoPickCard(on);
    }

    private void setAutoTrade(boolean on) {
        autoTrade = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonAutoTradeEnable.setEnabled(!on);
            riftHelperMainView.buttonAutoTradeDisable.setEnabled(on);
        });
        PreferenceManager.setAutoTrade(on);
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

    private void setLobbyOverlay(boolean on) {
        lobbyOverlay = on;
        SwingUtilities.invokeLater(() -> {
            riftHelperMainView.buttonLobbyOverlayEnable.setEnabled(!on);
            riftHelperMainView.buttonLobbyOverlayDisable.setEnabled(on);
        });
        PreferenceManager.setLobbyOverlay(on);
        // The overlay's own tracking timer reads the toggle state each tick; no explicit show/hide.
    }

    private void setAramBenchOverlay(boolean on) {
        aramBenchOverlay = on;
        SwingUtilities.invokeLater(() -> applyToggleButtons(
                riftHelperMainView.buttonAramBenchOverlayEnable, riftHelperMainView.buttonAramBenchOverlayDisable, on));
        PreferenceManager.setAramBenchOverlay(on);
    }

    private void setAramSwapToolsOverlay(boolean on) {
        aramSwapToolsOverlay = on;
        SwingUtilities.invokeLater(() -> applyToggleButtons(
                riftHelperMainView.buttonAramSwapToolsOverlayEnable, riftHelperMainView.buttonAramSwapToolsOverlayDisable, on));
        PreferenceManager.setAramSwapToolsOverlay(on);
    }

    private void setAramTop5Overlay(boolean on) {
        aramTop5Overlay = on;
        SwingUtilities.invokeLater(() -> applyToggleButtons(
                riftHelperMainView.buttonAramTop5OverlayEnable, riftHelperMainView.buttonAramTop5OverlayDisable, on));
        PreferenceManager.setAramTop5Overlay(on);
    }

    private void registerOverlay(ClientOverlay ov, String id) {
        ov.setOpacities(PreferenceManager.getOverlayOpacity() / 100f, PreferenceManager.getOverlayHoverOpacity() / 100f);
        if (PreferenceManager.hasOverlayPosition(id)) {
            ov.setCustomOffset(PreferenceManager.getOverlayPosX(id), PreferenceManager.getOverlayPosY(id));
        }
        ov.setOnMoved((x, y) -> PreferenceManager.setOverlayPosition(id, x, y));
        overlays.add(ov);
        ov.start();
    }

    private void applyOverlayOpacities() {
        float un = riftHelperMainView.getOverlayOpacity() / 100f;
        float hov = riftHelperMainView.getOverlayHoverOpacity() / 100f;
        for (ClientOverlay o : overlays) {
            o.setOpacities(un, hov);
        }
    }

    /** The current effective auto-swap ranking (Priority-then-Survey), top 5, annotated for the live
     *  view: star source, bench availability, and which one is the swap target. Empty when Auto Swap
     *  is off. */
    private java.util.List<RankedChoice> computeTop5(int currentChampId, java.util.List<Integer> benchIds) {
        int[] priorityIds = autoSwapPriority ? readAutoSwapPriorityIds() : new int[0];
        java.util.List<Integer> survey = autoSwapSurvey ? surveySwapIds : java.util.List.of();
        java.util.List<Integer> order = AutoSwapPlanner.buildOrder(priorityIds, survey);
        int priorityCount = 0;
        for (int id : priorityIds) {
            if (id > 0) {
                priorityCount++;
            }
        }
        int floor = order.indexOf(currentChampId);
        if (floor < 0) {
            floor = Integer.MAX_VALUE; // not listed: anything listed is an upgrade
        }
        int target = (benchIds == null || benchIds.isEmpty())
                ? -1 : AutoSwapPlanner.pickBenchTarget(order, benchIds, floor, null);
        java.util.List<RankedChoice> out = new java.util.ArrayList<>();
        for (int i = 0; i < order.size() && out.size() < 5; i++) {
            int id = order.get(i);
            boolean fromPriority = i < priorityCount;
            boolean onBench = benchIds != null && benchIds.contains(id);
            boolean swapTarget = target > 0 && id == target;
            out.add(new RankedChoice(id, i + 1, fromPriority, onBench, swapTarget));
        }
        return out;
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

    private void setNotifyPlayedRecently(boolean on) {
        notifyPlayedRecently = on;
        applyNotifyButtons(riftHelperMainView.buttonNotifyPlayedRecentlyEnable, riftHelperMainView.buttonNotifyPlayedRecentlyDisable, on);
        PreferenceManager.setNotifyPlayedRecently(on);
    }

    private void setScoutEnabled(boolean on) {
        scoutEnabled = on;
        applyNotifyButtons(riftHelperMainView.buttonScoutEnable, riftHelperMainView.buttonScoutDisable, on);
        PreferenceManager.setScoutEnabled(on);
        if (on) {
            scoutRefresh(); // populate the tab immediately when turned on
        }
    }

    /** Refresh the Players tab off the EDT (ScoutReport does its own I/O and delivers on the EDT),
     *  then fire the played-recently push at most once per game. The manual Refresh button always
     *  runs; the auto-refresh on phase changes is gated by {@code scoutEnabled}. */
    private void scoutRefresh() {
        model.ScoutReport.refreshAsync(lastGameflowPhase, report -> {
            riftHelperMainView.setScoutReport(report);
            maybeNotifyPlayedRecently(report);
        });
    }

    private void maybeNotifyPlayedRecently(model.ScoutReport report) {
        if (!notifyPlayedRecently || notifiedPlayedRecently || report == null) {
            return;
        }
        java.util.List<model.ScoutPlayer> recent = report.playedRecentlyPlayers();
        if (recent == null || recent.isEmpty()) {
            return;
        }
        notifiedPlayedRecently = true;
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < recent.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            model.ScoutPlayer p = recent.get(i);
            names.append(p.summonerName == null || p.summonerName.isBlank() ? "(hidden)" : p.summonerName);
        }
        notify(notifyPlayedRecently, "Played Recently", "In your last 3 games: " + names + ".", 4, "eyes");
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

    /** One-time read of the client's current gameflow phase at startup (the WS event only fires on
     *  change). Sets {@link #lastGameflowPhase} directly, without running the transition side effects. */
    private void seedGameflowPhase() {
        try {
            String raw = LCUGet.getFromClient("/lol-gameflow/v1/gameflow-phase");
            if (raw != null) {
                String phase = raw.trim().replace("\"", "");
                if (!phase.isEmpty() && !phase.equals("{}")) {
                    lastGameflowPhase = phase;
                    System.out.println("Seeded gameflow phase: " + phase);
                }
            }
        } catch (Exception ignored) {
            // no client / not ready: the WS event will set it on the next transition
        }
    }

    private void handleGameflowPhase(String phase) {
        if (phase == null || phase.equals(lastGameflowPhase)) {
            return; // act only on transitions; the event can repeat within a phase
        }
        lastGameflowPhase = phase;
        System.out.println("Gameflow phase: " + phase);

        // Poll for bench swaps only while in champ select; stop the instant we leave it. Force a clean
        // restart on every ChampSelect entry (cancel any stale task first) so a dodged-then-requeued
        // champ select always gets a fresh poll - never a wedged one that "doesn't refresh".
        if ("ChampSelect".equals(phase)) {
            stopSwapPoll();
            startSwapPoll();
        } else {
            stopSwapPoll();
            if (trollToggle) {
                disableTrollToggle(false); // champ select over; no bench left to swap back on
            }
            // left champ select: ARAM overlays hide, Top 5 clears
            benchModeActive = false;
            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.setTop5(java.util.List.of());
                if (top5OverlayPanel != null) {
                    top5OverlayPanel.setChoices(java.util.List.of());
                }
            });
        }

        // Players (scout) tab: refresh player intel on the phases where a roster exists; clear it when
        // idle. Gated by the Scout toggle so background lookups can be turned off.
        if (scoutEnabled) {
            switch (phase) {
                case "ChampSelect", "InProgress", "Lobby" -> scoutRefresh();
                case "None" -> SwingUtilities.invokeLater(() -> riftHelperMainView.setScoutReport(null));
                default -> { /* WaitingForStats / EndOfGame etc.: keep the last report shown */ }
            }
        }

        switch (phase) {
            case "ChampSelect" -> {
                autoQueueArmed = false;
                notifiedPick = false;
                notifiedBan = false;
                notifiedAramPick = false;
                cardPicked = false;
                cardDumpCount = 0;
                requestedSwapChamps.clear();
                lastSwapDiag = "";
                lastTradeDiag = "";
                notifiedPlayedRecently = false;
                dndMinimize(); // "match start": champ-select lobby just popped after accept (DND)
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
                claimPassesAsync(); // post-game: new pass rewards may be waiting
                dndMinimize(); // the post-game / honor screen pops the client up; tuck it away (DND)
                if (autoHonor) {
                    int honored = Honor.honorFriends();
                    dndMinimize(); // honoring can re-raise the client; minimize again after
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
                claimPassesAsync(); // post-game: new pass rewards may be waiting
                dndMinimize(); // end-of-game screen pops the client up; tuck it away (DND)
                if (autoSkipScreens) {
                    Lobby.playAgain();
                    notifyReturnedToLobbyOnce();
                }
            }
            case "WaitingForStats", "TerminatedInError" -> dndMinimize(); // stats screen pop-up (DND)
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
        // The gameflow-phase WS event only fires on a phase CHANGE, so a client already sitting in a
        // phase when we launch never reports it. Seed the current phase once so phase-gated features
        // (e.g. the lobby overlay) work immediately instead of only after the next transition.
        seedGameflowPhase();

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
        this.autoClaimPasses = PreferenceManager.getAutoClaimPasses();
        this.autoMinimize = PreferenceManager.getAutoMinimize();
        this.lobbyOverlay = PreferenceManager.getLobbyOverlay();
        this.aramBenchOverlay = PreferenceManager.getAramBenchOverlay();
        this.aramSwapToolsOverlay = PreferenceManager.getAramSwapToolsOverlay();
        this.aramTop5Overlay = PreferenceManager.getAramTop5Overlay();
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
        this.notifyPlayedRecently = PreferenceManager.getNotifyPlayedRecently();
        this.scoutEnabled = PreferenceManager.getScoutEnabled();
        this.autoLockLaneChoice = PreferenceManager.getAutoLockLaneChoice();
        this.autoAccept = PreferenceManager.getAutoAccept();
        this.autoDecline = PreferenceManager.getAutoDecline();
        this.autoSwapPriority = PreferenceManager.getAutoSwapPriorityEnabled();
        this.autoSwapSurvey = PreferenceManager.getAutoSwapSurveyEnabled();
        this.autoPickCard = PreferenceManager.getAutoPickCard();
        this.autoTrade = PreferenceManager.getAutoTrade();
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

        // Lobby overlay: a docked, client-tracking window. Its timer shows it only while the Lobby
        // Overlay toggle is on AND the client is in the Lobby phase (and focused, not minimized).
        // Built after the main toggles are initialized so its shared switches reflect the right state.
        ClientOverlay.setDragKeys(PreferenceManager.getOverlayDragKeys());

        registerOverlay(new ClientOverlay(
                new LobbyOverlayPanel(riftHelperMainView),
                ClientOverlay.Anchor.BOTTOM_LEFT, 16,
                () -> !riftHelperMainView.buttonLobbyOverlayEnable.isEnabled()
                        && "Lobby".equals(lastGameflowPhase)), "lobby");

        this.benchOverlayPanel = new QuickSwitchBenchOverlayPanel(riftHelperMainView);
        registerOverlay(new ClientOverlay(benchOverlayPanel, ClientOverlay.Anchor.TOP_CENTER, 12,
                () -> !riftHelperMainView.buttonAramBenchOverlayEnable.isEnabled() && benchModeActive), "aram-bench");

        registerOverlay(new ClientOverlay(new AramSwapToolsOverlayPanel(riftHelperMainView),
                ClientOverlay.Anchor.BOTTOM_RIGHT, 16,
                () -> !riftHelperMainView.buttonAramSwapToolsOverlayEnable.isEnabled() && benchModeActive), "aram-tools");

        this.top5OverlayPanel = new Top5OverlayPanel();
        registerOverlay(new ClientOverlay(top5OverlayPanel, ClientOverlay.Anchor.TOP_RIGHT, 16,
                () -> !riftHelperMainView.buttonAramTop5OverlayEnable.isEnabled() && benchModeActive), "aram-top5");

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
        applyToggleButtons(riftHelperMainView.buttonAutoClaimPassesEnable, riftHelperMainView.buttonAutoClaimPassesDisable, autoClaimPasses);
        if (autoClaimPasses) {
            claimPassesAsync(); // sweep any pending rewards on startup
        }
        this.riftHelperMainView.buttonAutoMinimizeEnable.setEnabled(!autoMinimize);
        this.riftHelperMainView.buttonAutoMinimizeDisable.setEnabled(autoMinimize);
        applyToggleButtons(riftHelperMainView.buttonLobbyOverlayEnable, riftHelperMainView.buttonLobbyOverlayDisable, lobbyOverlay);
        applyToggleButtons(riftHelperMainView.buttonAramBenchOverlayEnable, riftHelperMainView.buttonAramBenchOverlayDisable, aramBenchOverlay);
        applyToggleButtons(riftHelperMainView.buttonAramSwapToolsOverlayEnable, riftHelperMainView.buttonAramSwapToolsOverlayDisable, aramSwapToolsOverlay);
        applyToggleButtons(riftHelperMainView.buttonAramTop5OverlayEnable, riftHelperMainView.buttonAramTop5OverlayDisable, aramTop5Overlay);
        this.riftHelperMainView.setNotifyTopic(notifyTopic);
        this.riftHelperMainView.setNotifyIdleSeconds(notifyIdleSeconds);
        this.riftHelperMainView.setOverlayOpacity(PreferenceManager.getOverlayOpacity());
        this.riftHelperMainView.setOverlayHoverOpacity(PreferenceManager.getOverlayHoverOpacity());
        this.riftHelperMainView.setOverlayDragKeys(PreferenceManager.getOverlayDragKeys());
        this.riftHelperMainView.setUiScalePercent(PreferenceManager.getUiScalePercent());
        this.riftHelperMainView.setTrollSwapDelayMs(PreferenceManager.getTrollSwapDelayMs());
        this.riftHelperMainView.setTrollSwapLoops(PreferenceManager.getTrollSwapLoops());
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
        applyToggleButtons(riftHelperMainView.buttonNotifyPlayedRecentlyEnable, riftHelperMainView.buttonNotifyPlayedRecentlyDisable, notifyPlayedRecently);
        applyToggleButtons(riftHelperMainView.buttonScoutEnable, riftHelperMainView.buttonScoutDisable, scoutEnabled);
        applyToggleButtons(riftHelperMainView.buttonAutoLockEnable, riftHelperMainView.buttonAutoLockDisable, autoLockRank);
        applyToggleButtons(riftHelperMainView.buttonAutoBanEnable, riftHelperMainView.buttonAutoBanDisable, autoBan);
        applyToggleButtons(riftHelperMainView.buttonAutoLockArenaEnable, riftHelperMainView.buttonAutoLockArenaDisable, autoLockArena);
        applyToggleButtons(riftHelperMainView.buttonAutoBanArenaEnable, riftHelperMainView.buttonAutoBanArenaDisable, autoBanArena);
        applyToggleButtons(riftHelperMainView.buttonAutoBanCrowdFavoriteEnable, riftHelperMainView.buttonAutoBanCrowdFavoriteDisable, autoBanCrowdFavoriteArena);
        applyToggleButtons(riftHelperMainView.buttonAutoBraveryArenaEnable, riftHelperMainView.buttonAutoBraveryArenaDisable, autoBravery);
        applyToggleButtons(riftHelperMainView.buttonAutoSwapEnable, riftHelperMainView.buttonAutoSwapDisable, autoSwapPriority);
        applyToggleButtons(riftHelperMainView.buttonAutoSwapSurveyEnable, riftHelperMainView.buttonAutoSwapSurveyDisable, autoSwapSurvey);
        applyToggleButtons(riftHelperMainView.buttonAutoPickCardEnable, riftHelperMainView.buttonAutoPickCardDisable, autoPickCard);
        applyToggleButtons(riftHelperMainView.buttonAutoTradeEnable, riftHelperMainView.buttonAutoTradeDisable, autoTrade);
        applyToggleButtons(riftHelperMainView.buttonTrollSwapToggleEnable, riftHelperMainView.buttonTrollSwapToggleDisable, false);
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
    public int autoSwap(int currentChampId) {
        java.util.List<Integer> benchIds = new java.util.ArrayList<>();
        if (benchChampions != null) {
            for (BenchChampions b : benchChampions) {
                benchIds.add(b.getChampionId());
            }
        }
        return tryAutoSwap(benchIds, currentChampId);
    }

    /**
     * Core swap decision, shared by the champ-select event handler and the poll loop. Synchronized
     * so the two threads can never interleave a swap. Builds the combined Priority-then-Survey order
     * (no dedup) and swaps to the best available bench champion that improves on the champion you
     * currently hold. Returns the swapped champion id, or -1.
     */
    private synchronized int tryAutoSwap(java.util.List<Integer> benchIds, int currentChampId) {
        if (benchCycling || (!autoSwapPriority && !autoSwapSurvey)) {
            return -1; // feature off or a Troll Swap is cycling; nothing to log
        }
        int[] priorityIds = readAutoSwapPriorityIds();
        java.util.List<Integer> order = AutoSwapPlanner.buildOrder(
                autoSwapPriority ? priorityIds : new int[0],
                autoSwapSurvey ? surveySwapIds : java.util.List.of());
        int floor = order.indexOf(currentChampId);   // rank of the champ you currently hold
        boolean noFloor = floor < 0;
        if (noFloor) {
            floor = Integer.MAX_VALUE;                // not in the list -> anything listed is an upgrade
        }
        java.util.List<Integer> bench = (benchIds == null) ? java.util.List.of() : benchIds;
        int target = bench.isEmpty() ? -1 : AutoSwapPlanner.pickBenchTarget(order, bench, floor, null);
        int code = -1;
        if (target > 0) {
            code = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + target);
        }
        // Deduped diagnostic: prints only when the decision/inputs change, so a dodge-then-requeue
        // repro shows exactly whether this runs, with what bench, and what the swap POST returned.
        String diag = "cur=" + currentChampId + " floor=" + (noFloor ? "none" : floor)
                + " bench=" + bench + " target=" + target + (target > 0 ? " code=" + code : "");
        if (!diag.equals(lastSwapDiag)) {
            lastSwapDiag = diag;
            System.out.println("[AutoSwap] " + diag);
        }
        return (target > 0 && code == 204) ? target : -1;
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

    // ---- Troll Swap toggle poll: fast (100ms) so it reliably catches the ~1s bail-out window;
    //      runs only while the toggle is on. ----
    private static final int TROLL_STOP_MS = 2000;
    private static final int TROLL_POLL_MS = 100;
    private final java.util.concurrent.ScheduledExecutorService trollPoll =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "troll-toggle-poll");
                t.setDaemon(true);
                return t;
            });
    private volatile java.util.concurrent.ScheduledFuture<?> trollPollTask;

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
     *  swap off the freshest bench, then handle ARAM card auto-pick + trades. Fully exception-tolerant
     *  so the loop never dies. Runs every 750ms during ARAM champ select (fast enough for the ~12s
     *  card window and for trades). */
    private void pollSwapTick() {
        try {
            if (!autoSwapPriority && !autoSwapSurvey && !autoPickCard && !autoTrade) {
                return;
            }
            String raw = LCUGet.getFromClient("/lol-champ-select/v1/session");
            Session s = Session.parseFromRaw(raw);
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
            int currentChampId = localChampionId(s.getMyTeam());
            tryAutoSwap(benchIds, currentChampId);       // guarded internally by the swap toggles
            handleAramCardsAndTrades(s, raw);            // #1 card auto-pick + #2 trades
        } catch (Exception e) {
            System.out.println("[SwapPoll] " + e.getMessage());
        }
    }

    /** The 10-slot Auto Swap Priority list read from the view, as champion ids (0 = empty slot).
     *  Shared by auto-swap, card auto-pick, and trades so all three rank identically. */
    private int[] readAutoSwapPriorityIds() {
        return new int[]{
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
    }

    /**
     * #1 ARAM Auto Pick Card + #2 Auto Trade. Synchronized so the poll never interleaves with itself.
     * Both reuse the same Priority+Survey ranking as auto-swap. ARAM-only (caller already gated on
     * {@code isAllowRerolling()}); no-op while a Troll Swap is cycling.
     *
     * <p>Card-offer detection is VERIFY-LIVE: the 2026 card field is undocumented, so {@link
     * #extractOfferedCards} tries the likely field names and logs the raw session once if none match,
     * so a live ARAM reveals the real shape. Everything degrades to a safe no-op until then.
     */
    private synchronized void handleAramCardsAndTrades(Session s, String rawJson) {
        if (s == null || benchCycling) {
            return;
        }
        int[] priorityIds = readAutoSwapPriorityIds();

        // ---- #1 Auto Pick Card: once per champ select, pick the best-ranked offered card. ----
        // Offered cards = the pickable subset; the pick is submitted by COMPLETING the local player's
        // in-progress pick action (2026 ARAM has pick actions + allowSubsetChampionPicks). Confirmed
        // live: the card endpoint family does not exist; picking goes through actions like SR.
        if (autoPickCard && !cardPicked) {
            java.util.List<Integer> offered = extractOfferedCards(s, rawJson);
            if (offered != null && !offered.isEmpty()) {
                int pick = CardPickDecider.decidePick(offered, priorityIds, surveySwapIds, null);
                int pickActionId = inProgressPickActionId(s);
                if (pick > 0 && pickActionId >= 0) {
                    String ep = "/lol-champ-select/v1/session/actions/" + pickActionId;
                    if (LCUPatch.patchToClientWithBody(ep, jsonBodyAutoChoose(pickActionId, pick)) == 204) {
                        cardPicked = true;
                        System.out.println("[AutoCard] picked " + DDragonParser.getChampionName(pick));
                        notify(notifyChampPickedAram, "Card Picked (ARAM)",
                                "Picked " + DDragonParser.getChampionName(pick) + ".", 3, "flower_playing_cards");
                        dndMinimize();
                    }
                }
            }
        }

        // ---- #2 Auto Trade: accept upgrades, decline the rest, request the single best upgrade. ----
        if (autoTrade && s.getTrades() != null && !s.getTrades().isEmpty()) {
            int myChamp = localChampionId(s.getMyTeam());
            java.util.Map<Integer, Integer> cellToChamp = new java.util.HashMap<>();
            if (s.getMyTeam() != null) {
                for (MyTeam m : s.getMyTeam()) {
                    cellToChamp.put(m.getCellId(), m.getChampionId());
                }
            }
            java.util.Set<Integer> benchIds = new java.util.HashSet<>();
            if (s.getBenchChampions() != null) {
                for (BenchChampions b : s.getBenchChampions()) {
                    benchIds.add(b.getChampionId());
                }
            }
            java.util.List<TradeDecider.Trade> trades = new java.util.ArrayList<>();
            java.util.Map<Integer, Integer> tradeIdToChamp = new java.util.HashMap<>();
            for (Trades t : s.getTrades()) {
                int champ = cellToChamp.getOrDefault(t.getCellId(), -1);
                trades.add(new TradeDecider.Trade(t.getId(), t.getState(), champ));
                tradeIdToChamp.put(t.getId(), champ);
            }
            // Deduped diagnostic: the raw swaps (id/state/resolved champ) + my champ, only when changed.
            String diag = "my=" + myChamp + " swaps=" + trades;
            if (!diag.equals(lastTradeDiag)) {
                lastTradeDiag = diag;
                System.out.println("[Trade] " + diag);
            }
            for (TradeDecider.Decision d : TradeDecider.decide(myChamp, priorityIds, surveySwapIds, trades, benchIds)) {
                executeTradeDecision(d, tradeIdToChamp.getOrDefault(d.tradeId(), -1));
            }
        }
    }

    /** POST the REST call for one champion-swap decision. A REQUEST fires at most once per TARGET
     *  CHAMPION per champ select (swap ids churn as champ state changes, so keying by id spammed a
     *  fresh request every 750ms tick). Endpoint is champion-swaps, NOT trades (confirmed live:
     *  /session/trades 404s "Invalid URI format"). Success is 200/204. */
    private void executeTradeDecision(TradeDecider.Decision d, int targetChamp) {
        String verb = switch (d.action()) {
            case REQUEST -> "request";
            case ACCEPT -> "accept";
            case DECLINE -> "decline";
        };
        if (d.action() == TradeDecider.Action.REQUEST) {
            if (targetChamp <= 0 || !requestedSwapChamps.add(targetChamp)) {
                return; // unknown champ, or already requested this champ this champ select
            }
        }
        int code = LCUPost.postToClient("/lol-champ-select/v1/session/champion-swaps/" + d.tradeId() + "/" + verb);
        System.out.println("[AutoTrade] " + verb + " swap " + d.tradeId() + " champ " + targetChamp + " -> " + code);
        if ((code == 200 || code == 204) && d.action() == TradeDecider.Action.ACCEPT) {
            dndMinimize();
        }
    }

    /**
     * Extract the champion ids of the offered ARAM cards from the live session. VERIFY-LIVE: the 2026
     * card-offer field is new/undocumented, so this tries the likely field names and, if none match,
     * logs the raw session once per champ select so a live ARAM reveals the true shape. Returns an
     * empty list (safe no-op) until the real field is confirmed.
     */
    private java.util.List<Integer> extractOfferedCards(Session s, String rawJson) {
        java.util.List<Integer> ids = new java.util.ArrayList<>();

        // Primary (confirmed live): the pickable subset = the champions you're allowed to pick this
        // game (your dealt cards). Returns a bare int array.
        try {
            com.google.gson.JsonElement el = com.google.gson.JsonParser.parseString(
                    LCUGet.getFromClient("/lol-lobby-team-builder/champ-select/v1/subset-champion-list"));
            if (el.isJsonArray()) {
                for (com.google.gson.JsonElement e : el.getAsJsonArray()) {
                    int id = cardChampId(e);
                    if (id > 0) {
                        ids.add(id);
                    }
                }
            }
        } catch (Exception ignore) {
            // "{}" / RPC error when no subset is active yet -> fall through
        }

        // Fallback: a subsetChampionIds field on the raw session (older/alt clients).
        if (ids.isEmpty() && rawJson != null && !rawJson.isBlank()) {
            try {
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(rawJson).getAsJsonObject();
                for (String key : new String[]{"subsetChampionIds", "cards", "championCards", "offeredChampions"}) {
                    if (root.has(key) && root.get(key).isJsonArray()) {
                        for (com.google.gson.JsonElement e : root.getAsJsonArray(key)) {
                            int id = cardChampId(e);
                            if (id > 0) {
                                ids.add(id);
                            }
                        }
                    }
                }
            } catch (Exception ignore) {
                // fall through
            }
        }

        // Diagnostic: if still empty, dump the raw session a few times so a live ARAM reveals any shape
        // change (the cards may simply not be dealt yet this early tick).
        if (ids.isEmpty() && cardDumpCount < 5) {
            cardDumpCount++;
            System.out.println("[AutoCard] no offered cards yet (dump " + cardDumpCount + "/5):\n" + rawJson);
        }
        return ids;
    }

    /** The local player's in-progress pick action id (2026 ARAM has pick actions), or -1 if none. */
    private int inProgressPickActionId(Session s) {
        if (s == null || s.getActions() == null) {
            return -1;
        }
        for (java.util.List<Actions> group : s.getActions()) {
            if (group == null) {
                continue;
            }
            for (Actions a : group) {
                if (a != null && a.getActorCellId() == userCellId
                        && "pick".equals(a.getType()) && a.isInProgress()) {
                    return a.getId();
                }
            }
        }
        return -1;
    }

    /** A card entry may be a bare champion id or an object carrying a championId-like field. */
    private int cardChampId(com.google.gson.JsonElement el) {
        try {
            if (el.isJsonPrimitive()) {
                return el.getAsInt();
            }
            if (el.isJsonObject()) {
                com.google.gson.JsonObject o = el.getAsJsonObject();
                for (String f : new String[]{"championId", "champId", "id"}) {
                    if (o.has(f) && o.get(f).isJsonPrimitive()) {
                        return o.get(f).getAsInt();
                    }
                }
            }
        } catch (Exception ignore) {
            // not a champion id
        }
        return -1;
    }

    /** Troll Swap: cosmetic cycle. Swaps to each bench champion (first to last), repeated `loops`
     *  times, then back to the champion you started on. Runs on a daemon thread; pauses auto-swap
     *  while it runs so they do not fight. Uses the same REST swap the bench buttons use. */
    private void trollSwap() {
        if (benchCycling) {
            return; // already running
        }
        final int delayMs = riftHelperMainView.getTrollSwapDelayMs();
        final int loops = riftHelperMainView.getTrollSwapLoops();
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
                for (int loop = 0; loop < loops; loop++) {
                    for (int id : bench) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + id);
                        sleepQuietly(delayMs);
                    }
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

    /** Turn the infinite Troll Swap toggle on: pause auto-swap and start the fast poll. The champ to
     *  return to is captured on the first tick (off the EDT). No-op if the one-shot Troll Swap is running. */
    private void enableTrollToggle() {
        if (trollToggle) {
            return;
        }
        if (benchCycling) { // one-shot Troll Swap in progress; ignore and revert the switch
            applyToggleButtons(riftHelperMainView.buttonTrollSwapToggleEnable,
                    riftHelperMainView.buttonTrollSwapToggleDisable, false);
            return;
        }
        trollOriginal = 0;    // captured on the first tick
        trollBenchIndex = 0;
        trollLastSwapAt = 0;  // swap on the first eligible tick
        trollRotation = null; // captured on the first eligible tick
        trollToggle = true;
        benchCycling = true;  // pause auto-swap so they don't fight
        applyToggleButtons(riftHelperMainView.buttonTrollSwapToggleEnable,
                riftHelperMainView.buttonTrollSwapToggleDisable, true);
        SwingUtilities.invokeLater(() -> riftHelperMainView.setTrollToggleHintVisible(true));
        startTrollPoll();
    }

    /** Turn the toggle off. When requested, swap back to the champ we started on (off the EDT),
     *  resume auto-swap, and clear the hint. */
    private void disableTrollToggle(boolean returnToOriginal) {
        boolean was = trollToggle;
        trollToggle = false;
        stopTrollPoll();
        final int champ = trollOriginal;
        if (was && returnToOriginal && champ > 0) {
            Thread t = new Thread(() ->
                    LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + champ), "troll-return");
            t.setDaemon(true);
            t.start();
        }
        benchCycling = false; // resume auto-swap
        applyToggleButtons(riftHelperMainView.buttonTrollSwapToggleEnable,
                riftHelperMainView.buttonTrollSwapToggleDisable, false);
        SwingUtilities.invokeLater(() -> riftHelperMainView.setTrollToggleHintVisible(false));
    }

    private void startTrollPoll() {
        if (trollPollTask != null && !trollPollTask.isDone()) {
            return;
        }
        trollPollTask = trollPoll.scheduleWithFixedDelay(this::trollTick, 0, TROLL_POLL_MS,
                java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void stopTrollPoll() {
        java.util.concurrent.ScheduledFuture<?> t = trollPollTask;
        if (t != null) {
            t.cancel(false);
        }
        trollPollTask = null;
    }

    /** One toggle tick: read the live session, capture the return champ once, then bail to it
     *  (~1s before lock), swap to the next bench champ (delay elapsed), or wait. Exception-tolerant. */
    private void trollTick() {
        try {
            if (!trollToggle) {
                return;
            }
            Session s = Session.parseFromRaw(LCUGet.getFromClient("/lol-champ-select/v1/session"));
            boolean inBench = s != null && s.isAllowRerolling();
            java.util.List<Integer> bench = new java.util.ArrayList<>();
            int timeLeft = Integer.MAX_VALUE;
            if (inBench) {
                userCellId = s.getLocalPlayerCellId();
                if (s.getBenchChampions() != null) {
                    for (BenchChampions b : s.getBenchChampions()) {
                        bench.add(b.getChampionId());
                    }
                }
                if (s.getTimer() != null) {
                    timeLeft = s.getTimer().getAdjustedTimeLeftInPhase();
                }
                if (trollOriginal <= 0) {
                    trollOriginal = localChampionId(s.getMyTeam());
                }
            }
            boolean ready = inBench && !bench.isEmpty() && trollOriginal > 0;
            if (ready) {
                if (trollRotation == null) {
                    trollRotation = TrollToggleDecider.buildRotation(trollOriginal, bench);
                } else {
                    for (int id : bench) { // grow-only: capture any late/new bench champ, keep order stable
                        if (id > 0 && !trollRotation.contains(id)) {
                            trollRotation.add(id);
                        }
                    }
                }
            }
            long since = System.currentTimeMillis() - trollLastSwapAt;
            int delay = riftHelperMainView.getTrollSwapDelayMs();
            switch (TrollToggleDecider.decide(ready, timeLeft, TROLL_STOP_MS, since, delay)) {
                case STOP -> {
                    int champ = trollOriginal;
                    disableTrollToggle(true);
                    notify(notifyChampSwapAram, "Troll Swap stopped",
                            "Game starting - returned to " + DDragonParser.getChampionName(champ) + ".",
                            4, "twisted_rightwards_arrows");
                }
                case SWAP_NEXT -> {
                    int held = localChampionId(s.getMyTeam());
                    int[] pick = TrollToggleDecider.nextTarget(trollRotation, held, trollBenchIndex);
                    trollBenchIndex = pick[1];
                    if (pick[0] > 0) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + pick[0]);
                        trollLastSwapAt = System.currentTimeMillis();
                    }
                }
                case WAIT -> { }
            }
        } catch (Exception e) {
            System.out.println("[TrollToggle] " + e.getMessage());
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

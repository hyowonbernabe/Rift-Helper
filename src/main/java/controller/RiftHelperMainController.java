package controller;

import model.BenchChampion;
import model.DDragonParser;
import model.LCUPost;
import model.RerollsRemaining;
import no.stelar7.api.r4j.impl.lol.lcu.LCUSocketReader;
import view.RiftHelperMainView;

import javax.swing.*;
import java.util.List;

public class RiftHelperMainController {
    private RiftHelperMainView riftHelperMainView;
    private volatile boolean autoAccept;
    private volatile boolean autoSwap;
    private volatile int autoSwapSlots;
    private volatile int priority;
    private volatile boolean alwaysOnTop;
    private volatile boolean centerGUI;
    private volatile boolean autoReroll;
    private List<BenchChampion> benchChampions;
    private int rerollsRemaining;

    public RiftHelperMainController(RiftHelperMainView riftHelperMainView) {
        this.riftHelperMainView = riftHelperMainView;
        this.autoAccept = false;
        this.autoSwap = false;
        this.autoSwapSlots = 5;
        this.priority = 1;
        this.centerGUI = true;
        this.autoReroll = false;

        LCUSocketReader socketReader = new LCUSocketReader();
        socketReader.connect();

        System.out.println("Connected to Client: " + socketReader.isConnected());

        socketReader.subscribe("OnJsonApiEvent_lol-champ-select_v1_session", eventData -> {
            System.out.println(eventData);
            benchChampions = BenchChampion.parseFromJson(eventData);
            rerollsRemaining = RerollsRemaining.parseFromJson(eventData);

            autoReroll(rerollsRemaining);
            autoSwap();
            nameButtons();
        });

        this.riftHelperMainView.addBench1ActionListener(e -> {
            if (benchChampions.get(0) == null || benchChampions.isEmpty()) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(0).getChampionId());
        });

        this.riftHelperMainView.addBench2ActionListener(e -> {
            if (benchChampions.get(1) == null || benchChampions.size() < 2) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(1).getChampionId());
        });

        this.riftHelperMainView.addBench3ActionListener(e -> {
            if (benchChampions.get(2) == null || benchChampions.size() < 3) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(2).getChampionId());
        });

        this.riftHelperMainView.addBench4ActionListener(e -> {
            if (benchChampions.get(3) == null || benchChampions.size() < 4) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(3).getChampionId());
        });

        this.riftHelperMainView.addBench5ActionListener(e -> {
            if (benchChampions.get(4) == null || benchChampions.size() < 5) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(4).getChampionId());
        });

        this.riftHelperMainView.addBench6ActionListener(e -> {
            if (benchChampions.get(5) == null || benchChampions.size() < 6) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(5).getChampionId());
        });

        this.riftHelperMainView.addBench7ActionListener(e -> {
            if (benchChampions.get(6) == null || benchChampions.size() < 7) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(6).getChampionId());
        });

        this.riftHelperMainView.addBench8ActionListener(e -> {
            if (benchChampions.get(7) == null || benchChampions.size() < 8) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(7).getChampionId());
        });

        this.riftHelperMainView.addBench9ActionListener(e -> {
            if (benchChampions.get(8) == null || benchChampions.size() < 9) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(8).getChampionId());
        });

        this.riftHelperMainView.addBench10ActionListener(e -> {
            if (benchChampions.get(9) == null || benchChampions.size() < 10) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(9).getChampionId());
        });

        this.riftHelperMainView.addAutoAcceptEnableListener(e -> {
            autoAccept = true;
            System.out.println("Auto Accept Turned On: " + autoAccept);

            riftHelperMainView.buttonAutoAcceptEnable.setEnabled(false);
            riftHelperMainView.buttonAutoAcceptDisable.setEnabled(true);

            new Thread(() -> {
                try {
                    while (autoAccept) {
                        int responseCodeAccept = LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");

                        if (responseCodeAccept == 500) {
                            Thread.sleep(3000);
                        } else {
                            Thread.sleep(3000);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        this.riftHelperMainView.addAutoAcceptDisableListener(e -> {
            autoAccept = false;
            System.out.println("Auto Accept Turned Off: " + autoAccept);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoAcceptEnable.setEnabled(true);
                riftHelperMainView.buttonAutoAcceptDisable.setEnabled(false);
            });
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
        });

        this.riftHelperMainView.addAlwaysOnTopDisableListener(e -> {
            alwaysOnTop = false;
            this.riftHelperMainView.setAlwaysOnTop(alwaysOnTop);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAlwaysOnTopEnable.setEnabled(true);
                riftHelperMainView.buttonAlwaysOnTopDisable.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoSwapAddListener(e -> {
            if (autoSwapSlots >= 10) {
                JOptionPane.showMessageDialog(riftHelperMainView, "You cannot add more slots.\nThe maximum is 10.", "Error Adding", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoSwapSlots++;

            updateAutoSwapSlots();
        });

        this.riftHelperMainView.addAutoSwapSubtractListener(e -> {
            if (autoSwapSlots <= 1) {
                JOptionPane.showMessageDialog(riftHelperMainView, "You cannot subtract more slots.\nThe maximum is 1.", "Error Subtracting", JOptionPane.WARNING_MESSAGE);
                return;
            }

            autoSwapSlots--;

            updateAutoSwapSlots();
        });

        this.riftHelperMainView.addCenterGUIEnableListener(e -> {
            centerGUI = true;

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonCenterGUIEnable.setEnabled(false);
                riftHelperMainView.buttonCenterGUIDisable.setEnabled(true);
            });
        });

        this.riftHelperMainView.addCenterGUIDisableListener(e -> {
            centerGUI = false;

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonCenterGUIEnable.setEnabled(true);
                riftHelperMainView.buttonCenterGUIDisable.setEnabled(false);
            });
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
    }

    public void autoReroll(int rerollsRemaining) {
        if (rerollsRemaining > 0 && autoReroll) {
            for (int i = 0; i < rerollsRemaining; i++) {
                LCUPost.postToClient("/lol-champ-select/v1/session/my-selection/reroll");
            }
        }
    }

    public void nameButtons() {
        // Increase Champion Bench if more than 5
        if (benchChampions.size() > 5) {
            this.riftHelperMainView.panelQuickSwitchBench2.setVisible(true);
            this.riftHelperMainView.pack();

            if (centerGUI) {
                this.riftHelperMainView.setLocationRelativeTo(null);
            }
        }

        if (benchChampions == null) {
            this.riftHelperMainView.setButtonBench1Text(null);
            this.riftHelperMainView.setButtonBench2Text(null);
            this.riftHelperMainView.setButtonBench3Text(null);
            this.riftHelperMainView.setButtonBench4Text(null);
            this.riftHelperMainView.setButtonBench5Text(null);
            this.riftHelperMainView.setButtonBench6Text(null);
            this.riftHelperMainView.setButtonBench7Text(null);
            this.riftHelperMainView.setButtonBench8Text(null);
            this.riftHelperMainView.setButtonBench9Text(null);
            this.riftHelperMainView.setButtonBench10Text(null);
            this.riftHelperMainView.panelQuickSwitchBench2.setVisible(false);
            return;
        }

        // Set Champion ID to Champion Name
        if (benchChampions.get(0) == null) {
            System.out.println("Bench Champion is null 1");
        } else {
            int champId = benchChampions.get(0).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench1Text(champName);
        }
        if (benchChampions.get(1) == null) {
            System.out.println("Bench Champion is null 2");
        } else {
            int champId = benchChampions.get(1).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench2Text(champName);
        }
        if (benchChampions.get(2) == null) {
            System.out.println("Bench Champion is null 3");
        } else {
            int champId = benchChampions.get(2).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench3Text(champName);
        }
        if (benchChampions.get(3) == null) {
            System.out.println("Bench Champion is null 4");
        } else {
            int champId = benchChampions.get(3).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench4Text(champName);
        }
        if (benchChampions.get(4) == null) {
            System.out.println("Bench Champion is null 5");
        } else {
            int champId = benchChampions.get(4).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench5Text(champName);
        }
        if (benchChampions.get(5) == null) {
            System.out.println("Bench Champion is null 6");
        } else {
            int champId = benchChampions.get(5).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench6Text(champName);
        }
        if (benchChampions.get(6) == null) {
            System.out.println("Bench Champion is null 7");
        } else {
            int champId = benchChampions.get(6).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench7Text(champName);
        }
        if (benchChampions.get(7) == null) {
            System.out.println("Bench Champion is null 8");
        } else {
            int champId = benchChampions.get(7).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench8Text(champName);
        }
        if (benchChampions.get(8) == null) {
            System.out.println("Bench Champion is null 9");
        } else {
            int champId = benchChampions.get(8).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench9Text(champName);
        }
        if (benchChampions.get(9) == null) {
            System.out.println("Bench Champion is null 10");
        } else {
            int champId = benchChampions.get(9).getChampionId();
            String champName = DDragonParser.getChampionName(champId);
            this.riftHelperMainView.setButtonBench10Text(champName);
        }
    }

    public void autoSwap() {
        new Thread(() -> {
            while (autoSwap) {

                if (benchChampions == null) {
                    continue;
                }

                int autoSwapChampIdPriority1 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority1());
                int autoSwapChampIdPriority2 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority2());
                int autoSwapChampIdPriority3 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority3());
                int autoSwapChampIdPriority4 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority4());
                int autoSwapChampIdPriority5 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority5());
                int autoSwapChampIdPriority6 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority6());
                int autoSwapChampIdPriority7 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority7());
                int autoSwapChampIdPriority8 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority8());
                int autoSwapChampIdPriority9 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority9());
                int autoSwapChampIdPriority10 = DDragonParser.getChampionId(this.riftHelperMainView.getComboBoxAutoSwapPriority10());

                for (int i = 0; i < benchChampions.size(); i++) {
                    if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority1) && priority <= 10) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority1);
                        priority = 10;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority2) && priority <= 9) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority2);
                        priority = 9;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority3) && priority <= 8) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority3);
                        priority = 8;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority4) && priority <= 7)  {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority4);
                        priority = 7;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority5) && priority <= 6) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority5);
                        priority = 6;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority6) && priority <= 5) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority6);
                        priority = 5;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority7) && priority <= 4) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority7);
                        priority = 4;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority8) && priority <= 3) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority8);
                        priority = 3;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority9) && priority <= 2) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority9);
                        priority = 2;
                    } else if ((benchChampions.get(i).getChampionId() == autoSwapChampIdPriority10) && priority <= 1) {
                        LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + autoSwapChampIdPriority10);
                        priority = 1;
                    }
                }
            }
        }).start();
    }

    private void updateAutoSwapSlots() {
        JLabel[] labels = {
                riftHelperMainView.labelAutoSwapPriority1, riftHelperMainView.labelAutoSwapPriority2,
                riftHelperMainView.labelAutoSwapPriority3, riftHelperMainView.labelAutoSwapPriority4,
                riftHelperMainView.labelAutoSwapPriority5, riftHelperMainView.labelAutoSwapPriority6,
                riftHelperMainView.labelAutoSwapPriority7, riftHelperMainView.labelAutoSwapPriority8,
                riftHelperMainView.labelAutoSwapPriority9, riftHelperMainView.labelAutoSwapPriority10
        };

        JComboBox[] comboBoxes = {
                riftHelperMainView.comboBoxAutoSwapPriority1, riftHelperMainView.comboBoxAutoSwapPriority2,
                riftHelperMainView.comboBoxAutoSwapPriority3, riftHelperMainView.comboBoxAutoSwapPriority4,
                riftHelperMainView.comboBoxAutoSwapPriority5, riftHelperMainView.comboBoxAutoSwapPriority6,
                riftHelperMainView.comboBoxAutoSwapPriority7, riftHelperMainView.comboBoxAutoSwapPriority8,
                riftHelperMainView.comboBoxAutoSwapPriority9, riftHelperMainView.comboBoxAutoSwapPriority10
        };

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

        riftHelperMainView.revalidate();
        riftHelperMainView.repaint();
        riftHelperMainView.pack();

        if (centerGUI) {
            riftHelperMainView.setLocationRelativeTo(null);
        }
    }
}

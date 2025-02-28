package controller;

import model.BenchChampion;
import model.DDragonParser;
import model.LCUPost;
import no.stelar7.api.r4j.impl.lol.lcu.LCUSocketReader;
import view.RiftHelperMainView;

import javax.swing.*;
import java.util.List;

public class RiftHelperMainController {
    private RiftHelperMainView riftHelperMainView;
    private volatile boolean autoAccept;
    private volatile boolean autoSwap;
    private volatile int priority;
    private List<BenchChampion> benchChampions;

    public RiftHelperMainController(RiftHelperMainView riftHelperMainView) {
        this.riftHelperMainView = riftHelperMainView;
        this.autoAccept = false;
        this.autoSwap = false;
        this.priority = 1;

        LCUSocketReader socketReader = new LCUSocketReader();
        socketReader.connect();

        System.out.println("Connected to Client: " + socketReader.isConnected());

        socketReader.subscribe("OnJsonApiEvent_lol-champ-select_v1_session", eventData -> {
            benchChampions = BenchChampion.parseFromJson(eventData);

            autoSwap();
            nameButtons();
        });

        this.riftHelperMainView.addBench1ActionListener(e -> {
            if (benchChampions.get(0) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(0).getChampionId());
        });

        this.riftHelperMainView.addBench2ActionListener(e -> {
            if (benchChampions.get(1) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(1).getChampionId());
        });

        this.riftHelperMainView.addBench3ActionListener(e -> {
            if (benchChampions.get(2) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(2).getChampionId());
        });

        this.riftHelperMainView.addBench4ActionListener(e -> {
            if (benchChampions.get(3) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(3).getChampionId());
        });

        this.riftHelperMainView.addBench5ActionListener(e -> {
            if (benchChampions.get(4) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(4).getChampionId());
        });

        this.riftHelperMainView.addBench6ActionListener(e -> {
            if (benchChampions.get(5) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(5).getChampionId());
        });

        this.riftHelperMainView.addBench7ActionListener(e -> {
            if (benchChampions.get(6) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(6).getChampionId());
        });

        this.riftHelperMainView.addBench8ActionListener(e -> {
            if (benchChampions.get(7) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(7).getChampionId());
        });

        this.riftHelperMainView.addBench9ActionListener(e -> {
            if (benchChampions.get(8) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(8).getChampionId());
        });

        this.riftHelperMainView.addBench10ActionListener(e -> {
            if (benchChampions.get(9) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(9).getChampionId());
        });

        this.riftHelperMainView.addAutoAcceptStartListener(e -> {
            autoAccept = true;
            System.out.println("Auto Accept Turned On: " + autoAccept);

            riftHelperMainView.buttonAutoAcceptStart.setEnabled(false);
            riftHelperMainView.buttonAutoAcceptStop.setEnabled(true);

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

        this.riftHelperMainView.addAutoAcceptStopListener(e -> {
            autoAccept = false;
            System.out.println("Auto Accept Turned Off: " + autoAccept);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoAcceptStart.setEnabled(true);
                riftHelperMainView.buttonAutoAcceptStop.setEnabled(false);
            });
        });

        this.riftHelperMainView.addAutoSwapStartListener(e -> {
            autoSwap = true;
            System.out.println("Auto Swap Turned On: " + autoSwap);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapStart.setEnabled(false);
                riftHelperMainView.buttonAutoSwapStop.setEnabled(true);
            });
        });

        this.riftHelperMainView.addAutoSwapStopListener(e -> {
            autoSwap = false;
            priority = 1;
            System.out.println("Auto Swap Turned Off: " + autoSwap);

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoSwapStart.setEnabled(true);
                riftHelperMainView.buttonAutoSwapStop.setEnabled(false);
            });
        });
    }

    public void nameButtons() {
        // Increase Champion Bench if more than 5
        if (benchChampions.size() > 5) {
            this.riftHelperMainView.panelQuickSwitchBench2.setVisible(true);
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
}

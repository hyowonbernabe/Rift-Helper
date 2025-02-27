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
    private List<BenchChampion> benchChampions;

    public RiftHelperMainController(RiftHelperMainView riftHelperMainView) {
        this.riftHelperMainView = riftHelperMainView;
        this.autoAccept = false;

        LCUSocketReader socketReader = new LCUSocketReader();
        socketReader.connect();

        System.out.println("Connected to Client: " + socketReader.isConnected());

        socketReader.subscribe("OnJsonApiEvent_lol-champ-select_v1_session", eventData -> {
            benchChampions = BenchChampion.parseFromJson(eventData);

            if (benchChampions.size() > 5) {
                this.riftHelperMainView.panelQuickSwitchBench2.setVisible(true);
            }

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
        });

        this.riftHelperMainView.addBench1ActionListener(e -> {
            if (benchChampions.get(0) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(0).getChampionId());
            System.out.println(benchChampions.get(0));
            System.out.println(benchChampions.get(0).getChampionId());
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

            SwingUtilities.invokeLater(() -> {
                riftHelperMainView.buttonAutoAcceptStart.setEnabled(true);
                riftHelperMainView.buttonAutoAcceptStop.setEnabled(false);
            });
        });
    }
}

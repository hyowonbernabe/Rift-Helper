package controller;

import model.BenchChampion;
import model.DDragonParser;
import model.LCUPost;
import no.stelar7.api.r4j.impl.lol.lcu.LCUSocketReader;
import view.RiftHelperView;

import javax.swing.*;
import java.util.List;

public class RiftHelperController {
    private RiftHelperView riftHelperView;
    private volatile boolean autoAccept;
    private List<BenchChampion> benchChampions;

    public RiftHelperController(RiftHelperView riftHelperView) {
        this.riftHelperView = riftHelperView;
        this.autoAccept = false;


        LCUSocketReader socketReader = new LCUSocketReader();
        socketReader.connect();

        System.out.println("Connected to Client: " + socketReader.isConnected());

        socketReader.subscribe("OnJsonApiEvent_lol-champ-select_v1_session", eventData -> {
            benchChampions = BenchChampion.parseFromJson(eventData);

            if (benchChampions.size() > 5) {
                this.riftHelperView.panelBench2.setVisible(true);
            }

            if (benchChampions.get(0) == null) {
                System.out.println("Bench Champion is null 1");
            } else {
                int champId = benchChampions.get(0).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench1Text(champName);
            }

            if (benchChampions.get(1) == null) {
                System.out.println("Bench Champion is null 2");
            } else {
                int champId = benchChampions.get(1).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench2Text(champName);
            }
            if (benchChampions.get(2) == null) {
                System.out.println("Bench Champion is null 3");
            } else {
                int champId = benchChampions.get(2).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench3Text(champName);
            }
            if (benchChampions.get(3) == null) {
                System.out.println("Bench Champion is null 4");
            } else {
                int champId = benchChampions.get(3).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench4Text(champName);
            }
            if (benchChampions.get(4) == null) {
                System.out.println("Bench Champion is null 5");
            } else {
                int champId = benchChampions.get(4).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench5Text(champName);
            }
            if (benchChampions.get(5) == null) {
                System.out.println("Bench Champion is null 6");
            } else {
                int champId = benchChampions.get(5).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench6Text(champName);
            }
            if (benchChampions.get(6) == null) {
                System.out.println("Bench Champion is null 7");
            } else {
                int champId = benchChampions.get(6).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench7Text(champName);
            }
            if (benchChampions.get(7) == null) {
                System.out.println("Bench Champion is null 8");
            } else {
                int champId = benchChampions.get(7).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench8Text(champName);
            }
            if (benchChampions.get(8) == null) {
                System.out.println("Bench Champion is null 9");
            } else {
                int champId = benchChampions.get(8).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench9Text(champName);
            }
            if (benchChampions.get(9) == null) {
                System.out.println("Bench Champion is null 10");
            } else {
                int champId = benchChampions.get(9).getChampionId();
                String champName = DDragonParser.getChampionName(champId);
                this.riftHelperView.setButtonBench10Text(champName);
            }
        });

        this.riftHelperView.addBench1ActionListener(e -> {
            if (benchChampions.get(0) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(0));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench2ActionListener(e -> {
            if (benchChampions.get(1) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(1));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench3ActionListener(e -> {
            if (benchChampions.get(2) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(2));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench4ActionListener(e -> {
            if (benchChampions.get(3) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(3));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench5ActionListener(e -> {
            if (benchChampions.get(4) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(4));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench6ActionListener(e -> {
            if (benchChampions.get(5) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(5));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench7ActionListener(e -> {
            if (benchChampions.get(6) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(6));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench8ActionListener(e -> {
            if (benchChampions.get(7) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(7));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench9ActionListener(e -> {
            if (benchChampions.get(8) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(8));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addBench10ActionListener(e -> {
            if (benchChampions.get(9) == null) {
                System.out.println("Cannot swap. Bench Champion is null");
                return;
            }

            int responseCodeAccept = LCUPost.postToClient("/lol-champ-select/v1/session/bench/swap/" + benchChampions.get(9));
            System.out.println(responseCodeAccept);
        });

        this.riftHelperView.addAutoAcceptStartListener(e -> {
            autoAccept = true;

            riftHelperView.buttonAutoAcceptStart.setEnabled(false);
            riftHelperView.buttonAutoAcceptStop.setEnabled(true);

            new Thread(() -> {
                try {
                    while (autoAccept) {
                        int responseCodeAccept = LCUPost.postToClient("/lol-matchmaking/v1/ready-check/accept");

                        if (responseCodeAccept == 500) {
                            System.out.println("Matchmaking in queue.");
                            Thread.sleep(3000);
                        } else {
                            System.out.println("Matchmaking not in queue.");
                            Thread.sleep(3000);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        this.riftHelperView.addAutoAcceptStopListener(e -> {
            autoAccept = false;

            SwingUtilities.invokeLater(() -> {
                riftHelperView.buttonAutoAcceptStart.setEnabled(true);
                riftHelperView.buttonAutoAcceptStop.setEnabled(false);
            });
        });
    }
}

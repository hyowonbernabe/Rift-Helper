package view;

import com.formdev.flatlaf.util.UIScale;
import model.AramSeeder;
import model.AramSurveyData;
import model.AramSurveyStore;
import model.DDragonParser;
import model.SurveyRanker;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

/**
 * Modal ARAM preference survey. Ports the approved interactive prototype to Swing:
 * <ol>
 *   <li>Phase 1 - tier tap: one champion at a time into Main / Like / Fine / Never.</li>
 *   <li>Phase 2 - staged pairwise ranking (Main, then Like, then Fine) via a merge-sort human
 *       comparator that runs on a worker thread and blocks on the user's duel answers.</li>
 *   <li>Encouragement / offer screens between stages, then a result screen.</li>
 * </ol>
 *
 * Every choice auto-saves to {@code ~/.rift-helper/aram-survey.json} through {@link AramSurveyStore},
 * so the survey resumes exactly where it was left (including part-way through a stage, via the
 * cached comparisons). Nothing here throws out to the caller: risky bits are wrapped, and the store
 * is already failure tolerant.
 */
public class SurveyDialog extends JDialog {

    // Tier palette, matching the approved prototype (gold / green / blue / muted red).
    private static final Color C_MAIN = new Color(0xC9A44C);
    private static final Color C_LIKE = new Color(0x4B9E6A);
    private static final Color C_FINE = new Color(0x5A7BBF);
    private static final Color C_NEVER = new Color(0x8A5A5A);

    private static final String[] STAGES = {"main", "like", "fine"};

    private enum Mode {NONE, TIER, DUEL, OFFER, RESULT}

    private final Runnable onClose;
    private final AramSurveyData data;
    private final List<String> roster;

    // Frozen tier-tap sequence (data.seedOrder reconciled with the live roster) and the play-history
    // context used for per-champion badges. Written by the seeding worker, read on the EDT.
    private volatile List<String> order = new ArrayList<>();
    private volatile Map<String, AramSeeder.ChampInfo> infos;
    private volatile boolean seeding;

    private final Font baseFont;
    private final Font fBody;
    private final Font fTitle;
    private final Font fSub;
    private final Font fEyebrow;

    private final ContentPanel contentPanel = new ContentPanel();
    private final JScrollPane scroll;
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel savedLabel = new JLabel(" ");
    private final JLabel hintLabel = new JLabel(" ", SwingConstants.CENTER);
    private Timer savedTimer;

    // Phase 1 (tier tap) state.
    private int idx;
    private final Deque<Integer> tierHistory = new ArrayDeque<>();

    // Phase 2 (duel) state.
    private Mode mode = Mode.NONE;
    private volatile boolean cancelled;
    private Thread rankerThread;
    private SynchronousQueue<Integer> activeQueue;
    private String activeA;
    private String activeB;
    private int stageDuelCount;
    private int stageEstimate = 1;
    private int stageProgressBase;

    private boolean closed;

    public SurveyDialog(JFrame owner, Runnable onClose) {
        super(owner, "ARAM Preference Survey", true);
        this.onClose = onClose;
        this.data = safeLoad();
        this.roster = safeRoster();
        if ((data.ddragonVersion == null || data.ddragonVersion.isBlank())) {
            try {
                data.ddragonVersion = DDragonParser.getVersion();
            } catch (Exception ignored) {
                // best effort only
            }
        }

        this.baseFont = resolveBaseFont();
        this.fBody = baseFont.deriveFont(Font.PLAIN, fpt(13f));
        this.fTitle = baseFont.deriveFont(Font.BOLD, fpt(16f));
        this.fSub = baseFont.deriveFont(Font.PLAIN, fpt(12f));
        this.fEyebrow = baseFont.deriveFont(Font.BOLD, fpt(11f));

        this.scroll = new JScrollPane(contentPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        buildShell();
        installKeyBindings();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });

        setSize(px(580), px(660));
        setMinimumSize(new Dimension(px(440), px(440)));
        setLocationRelativeTo(owner);
    }

    // ------------------------------------------------------------------ public API

    /** Open the dialog. Always lands on the intro screen first; a single click seeds and resumes. */
    public void openResume() {
        try {
            if (roster.isEmpty()) {
                showRosterError();
                setVisible(true);
                return;
            }
            showIntro();
        } catch (Exception e) {
            System.out.println("[Survey] openResume failed: " + e.getMessage());
            showRosterError();
        }
        setVisible(true);
    }

    /**
     * Leave the intro. Seeding (mastery / match-history fetch, and on a fresh survey the frozen
     * order) runs on a worker so the visible window stays responsive, then routes on the EDT.
     */
    private void beginAfterIntro() {
        if (seeding) {
            return;
        }
        seeding = true;
        hint("Preparing your champions...");
        Thread t = new Thread(() -> {
            try {
                prepareSeeding();
            } catch (Exception e) {
                System.out.println("[Survey] seeding failed: " + e.getMessage());
            } finally {
                SwingUtilities.invokeLater(() -> {
                    seeding = false;
                    routeAfterIntro();
                });
            }
        }, "aram-survey-seed");
        t.setDaemon(true);
        t.start();
    }

    /** Resume at the correct screen for the saved state, using the (now-prepared) seeded order. */
    private void routeAfterIntro() {
        if (closed) {
            return;
        }
        try {
            if (order.isEmpty()) {
                showRosterError();
                return;
            }
            int firstUndecided = nextUndecidedFrom(0);
            if (firstUndecided < order.size()) {
                idx = firstUndecided;
                showTierTap();
            } else {
                maybeSaveCompletionSnapshot();
                String next = nextRankableAfter(null);
                if (next == null) {
                    showResult();
                } else {
                    showOffer(next, previousDoneTier(next));
                }
            }
        } catch (Exception e) {
            System.out.println("[Survey] routeAfterIntro failed: " + e.getMessage());
            showRosterError();
        }
    }

    /**
     * Fetch play-history context and, on a fresh survey, compute the frozen champion order. Safe to
     * run off the EDT: only file / network I/O, no Swing calls. On resume the saved
     * {@code data.seedOrder} is reused as-is; the fetch then only feeds badges.
     */
    private void prepareSeeding() {
        Map<String, AramSeeder.ChampInfo> fetched;
        try {
            fetched = AramSeeder.fetch(); // empty map on failure, never throws
        } catch (Exception e) {
            System.out.println("[Survey] seeder fetch failed: " + e.getMessage());
            fetched = new HashMap<>();
        }
        infos = (fetched == null) ? new HashMap<>() : fetched;

        if (data.seedOrder == null || data.seedOrder.isEmpty()) {
            List<String> seeded;
            try {
                seeded = AramSeeder.seededOrder(infos, roster);
            } catch (Exception e) {
                System.out.println("[Survey] seeded order failed: " + e.getMessage());
                seeded = null;
            }
            data.seedOrder = new ArrayList<>((seeded == null || seeded.isEmpty()) ? roster : seeded);
            persistQuietly();
        }
        order = buildOrder();
    }

    /** Frozen seed order intersected with the live roster, then any brand-new champions appended. */
    private List<String> buildOrder() {
        List<String> built = new ArrayList<>();
        Set<String> rosterSet = new LinkedHashSet<>(roster);
        List<String> seed = (data.seedOrder == null) ? new ArrayList<>() : data.seedOrder;
        for (String n : seed) {
            if (rosterSet.contains(n) && !built.contains(n)) {
                built.add(n);
            }
        }
        for (String n : roster) {
            if (!built.contains(n)) {
                built.add(n);
            }
        }
        return built;
    }

    /** File save with no Swing side effects (safe off the EDT, unlike {@link #safeSave()}). */
    private void persistQuietly() {
        try {
            AramSurveyStore.save(data);
        } catch (Exception e) {
            System.out.println("[Survey] save failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ shell / chrome

    private void buildShell() {
        JPanel root = new JPanel(new MigLayout(
                "insets 16 18 14 18, wrap 1, fill", "[grow,fill]", "[]6[]3[]8[grow,fill]6[]"));
        root.setBackground(Theme.BG);
        setContentPane(root);

        JPanel head = new JPanel(new MigLayout("insets 0, wrap 1, gap 1", "[grow,fill]"));
        head.setOpaque(false);
        head.add(makeLabel("ARAM Preference Survey", fTitle, Theme.TEXT));
        head.add(makeLabel("Auto-saves every choice. Quit and resume anytime.", fSub, Theme.TEXT_FAINT));
        root.add(head, "growx");

        progress.setValue(0);
        progress.setStringPainted(false);
        progress.setBorderPainted(false);
        progress.setForeground(Theme.ACCENT);
        progress.setBackground(Theme.SURFACE_2);
        progress.setPreferredSize(new Dimension(px(100), px(6)));
        root.add(progress, "growx, h " + px(6) + "!");

        savedLabel.setFont(baseFont.deriveFont(Font.PLAIN, fpt(11f)));
        savedLabel.setForeground(Theme.GREEN);
        savedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        root.add(savedLabel, "growx");

        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(Theme.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(px(16));
        root.add(scroll, "grow, push");

        hintLabel.setFont(baseFont.deriveFont(Font.PLAIN, fpt(11f)));
        hintLabel.setForeground(Theme.TEXT_FAINT);
        root.add(hintLabel, "growx");
    }

    private void installKeyBindings() {
        bindKey(KeyEvent.VK_1, () -> tierKey("main"));
        bindKey(KeyEvent.VK_2, () -> tierKey("like"));
        bindKey(KeyEvent.VK_3, () -> tierKey("fine"));
        bindKey(KeyEvent.VK_4, () -> tierKey("never"));
        bindKey(KeyEvent.VK_NUMPAD1, () -> tierKey("main"));
        bindKey(KeyEvent.VK_NUMPAD2, () -> tierKey("like"));
        bindKey(KeyEvent.VK_NUMPAD3, () -> tierKey("fine"));
        bindKey(KeyEvent.VK_NUMPAD4, () -> tierKey("never"));
        bindKey(KeyEvent.VK_BACK_SPACE, this::undoKey);
        bindKey(KeyEvent.VK_LEFT, () -> duelKey(-1));
        bindKey(KeyEvent.VK_RIGHT, () -> duelKey(1));
        bindKey(KeyEvent.VK_EQUALS, () -> duelKey(0));
    }

    private void bindKey(int keyCode, Runnable action) {
        String name = "surveyKey" + keyCode;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0), name);
        getRootPane().getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    action.run();
                } catch (Exception ex) {
                    System.out.println("[Survey] key action failed: " + ex.getMessage());
                }
            }
        });
    }

    private void tierKey(String key) {
        if (mode == Mode.TIER) {
            chooseTier(key);
        }
    }

    private void undoKey() {
        if (mode == Mode.TIER) {
            undoTier();
        }
    }

    private void duelKey(int answer) {
        if (mode == Mode.DUEL && activeQueue != null) {
            answerDuel(activeA, activeB, answer, activeQueue);
        }
    }

    // ------------------------------------------------------------------ intro

    private void showIntro() {
        mode = Mode.NONE;
        hint("");
        setProgress(0);
        boolean resuming = data.decidedCount() > 0;

        JPanel outer = column();

        JPanel card = new JPanel(new MigLayout("insets 24, wrap 1, gap 8", "[grow,center]"));
        card.setOpaque(true);
        card.setBackground(Theme.SURFACE_2);
        card.setBorder(cardBorder(Theme.LINE));

        JLabel emoji = makeLabel("🧭", baseFont.deriveFont(fpt(34f)), Theme.TEXT); // compass
        emoji.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(emoji, "align center");

        JLabel title = makeLabel("How this survey is ordered",
                baseFont.deriveFont(Font.BOLD, fpt(18f)), Theme.TEXT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, "align center");

        JLabel body = makeLabel("<html><div style='text-align:center'>Champions are ordered by how "
                + "much you play them - your mastery and recent games (ARAM, ARAM Mayhem, and "
                + "Summoner's Rift). Most-played first. You choose every tier yourself.</div></html>",
                fBody, Theme.TEXT_DIM);
        body.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(body, "align center, growx, wmax " + px(420));

        JLabel exLabel = makeLabel("Each champion shows quick context, for example:",
                fSub, Theme.TEXT_FAINT);
        exLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(exLabel, "align center, gaptop " + px(6));

        JPanel examples = new JPanel(new MigLayout("insets 0, gap 6, wrap 2", ""));
        examples.setOpaque(false);
        examples.add(badgeChip("Mastery 7 - 452,180"));
        examples.add(badgeChip("Best: A+"));
        examples.add(badgeChip("ARAM 8 - Mayhem 2 - SR 1"));
        examples.add(badgeChip("last played 2d ago"));
        card.add(examples, "align center");

        JButton start = button(resuming ? "Continue" : "Start", Kind.PRIMARY);
        start.addActionListener(e -> beginAfterIntro());
        card.add(start, "align center, gaptop " + px(8));

        outer.add(card, "align center, growx, wmax " + px(520));
        showScreen(outer);
    }

    // ------------------------------------------------------------------ phase 1: tier tap

    private void showTierTap() {
        mode = Mode.TIER;
        hint("Keys:   1 Main    2 Like    3 Fine    4 Never    Backspace Undo");
        if (idx < 0 || idx >= order.size()) {
            beginStages();
            return;
        }
        String name = order.get(idx);
        int total = order.size();
        setProgress((int) Math.round(data.decidedCount() / (double) total * 45));

        JPanel p = column();

        JLabel step = makeLabel(
                "Champion " + (data.decidedCount() + 1) + " of " + total + "   -   tap where it belongs",
                fSub, Theme.TEXT_DIM);
        step.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(step, "align center");

        JLabel champIcon = new JLabel();
        champIcon.setHorizontalAlignment(SwingConstants.CENTER);
        champIcon.setPreferredSize(new Dimension(px(112), px(112)));
        champIcon.setMinimumSize(new Dimension(px(112), px(112)));
        champIcon.setBorder(BorderFactory.createLineBorder(Theme.LINE, px(2)));
        loadIcon(champIcon, name, px(108));
        p.add(champIcon, "align center");

        JPanel nameBlock = new JPanel(new MigLayout("insets 0, wrap 1, gap 4", "[grow,center]"));
        nameBlock.setOpaque(false);
        JLabel champName = makeLabel(name, baseFont.deriveFont(Font.BOLD, fpt(20f)), Theme.TEXT);
        champName.setHorizontalAlignment(SwingConstants.CENTER);
        nameBlock.add(champName, "align center");
        JComponent badges = badgesFor(name);
        if (badges.getComponentCount() > 0) {
            nameBlock.add(badges, "align center, gaptop " + px(2));
        }
        p.add(nameBlock, "align center, gapbottom " + px(4));

        JPanel tiers = new JPanel(new MigLayout("insets 0, gap 10", "[grow,fill]10[grow,fill]10[grow,fill]10[grow,fill]"));
        tiers.setOpaque(false);
        tiers.add(tierCard("main"), "grow, sizegroup t");
        tiers.add(tierCard("like"), "grow, sizegroup t");
        tiers.add(tierCard("fine"), "grow, sizegroup t");
        tiers.add(tierCard("never"), "grow, sizegroup t");
        p.add(tiers, "align center, growx, wmax " + px(560));

        JPanel counts = new JPanel(new MigLayout("insets 0, gap 16", ""));
        counts.setOpaque(false);
        for (String tier : new String[]{"main", "like", "fine", "never"}) {
            counts.add(countChip(tier));
        }
        p.add(counts, "align center");

        JPanel actions = new JPanel(new MigLayout("insets 0, gap 10", ""));
        actions.setOpaque(false);
        JButton undo = button("Undo", Kind.GHOST);
        undo.setEnabled(!tierHistory.isEmpty());
        undo.addActionListener(e -> undoTier());
        JButton finish = button("Finish tiers now", Kind.NORMAL);
        finish.addActionListener(e -> beginStages());
        actions.add(undo);
        actions.add(finish);
        p.add(actions, "align center, gaptop " + px(2));

        showScreen(p);
    }

    private void chooseTier(String key) {
        if (idx < 0 || idx >= order.size()) {
            return;
        }
        String name = order.get(idx);
        data.tiers.put(name, key);
        tierHistory.push(idx);
        safeSave();
        idx = nextUndecidedFrom(idx + 1);
        if (idx >= order.size()) {
            beginStages();
        } else {
            showTierTap();
        }
    }

    private void undoTier() {
        if (tierHistory.isEmpty()) {
            return;
        }
        int prev = tierHistory.pop();
        if (prev >= 0 && prev < order.size()) {
            data.tiers.remove(order.get(prev));
        }
        idx = prev;
        safeSave();
        showTierTap();
    }

    private int nextUndecidedFrom(int from) {
        for (int i = Math.max(0, from); i < order.size(); i++) {
            if (!data.tiers.containsKey(order.get(i))) {
                return i;
            }
        }
        return order.size();
    }

    // ------------------------------------------------------------------ phase 2: staged ranking

    private void beginStages() {
        maybeSaveCompletionSnapshot();
        runStage("main");
    }

    private void runStage(String tier) {
        List<String> items = data.namesInTier(tier);
        if (items.size() <= 1) {
            data.rankedOrder.put(tier, new ArrayList<>(items));
            data.stageDone.put(tier, true);
            safeSave();
            offerNext(tier);
            return;
        }

        stageDuelCount = 0;
        stageEstimate = Math.max(1, (int) Math.ceil(items.size() * (Math.log(items.size()) / Math.log(2))));
        stageProgressBase = stageBase(tier);

        final SynchronousQueue<Integer> queue = new SynchronousQueue<>();
        final SurveyRanker.Asker asker = (a, b) -> {
            if (cancelled) {
                return 0;
            }
            SwingUtilities.invokeLater(() -> showDuel(a, b, queue));
            try {
                return queue.take();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return 0;
            }
        };

        Thread worker = new Thread(() -> {
            final List<String> ranked;
            try {
                ranked = SurveyRanker.rank(items, data.comparisons, asker);
            } catch (Exception ex) {
                System.out.println("[Survey] ranking failed: " + ex.getMessage());
                return;
            }
            SwingUtilities.invokeLater(() -> {
                if (cancelled) {
                    return;
                }
                data.rankedOrder.put(tier, ranked);
                data.stageDone.put(tier, true);
                safeSave();
                offerNext(tier);
            });
        }, "aram-survey-ranker");
        worker.setDaemon(true);
        rankerThread = worker;
        worker.start();
    }

    private void showDuel(String a, String b, SynchronousQueue<Integer> queue) {
        mode = Mode.DUEL;
        activeQueue = queue;
        activeA = a;
        activeB = b;
        stageDuelCount++;
        hint("Keys:   Left = left champ    Right = right champ    = No preference");
        setProgress(stageProgressBase
                + (int) Math.round(Math.min(1.0, stageDuelCount / (double) stageEstimate) * 15));

        JPanel p = column();

        JLabel q = makeLabel("Which would you rather play?   (pick " + stageDuelCount + ")",
                fSub, Theme.TEXT_DIM);
        q.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(q, "align center, gaptop " + px(4));

        JPanel duel = new JPanel(new MigLayout("insets 0, gap 16", "[]16[]16[]"));
        duel.setOpaque(false);
        JButton pa = pickButton(a);
        pa.addActionListener(e -> answerDuel(a, b, -1, queue));
        JLabel vs = makeLabel("VS", baseFont.deriveFont(Font.BOLD, fpt(14f)), Theme.TEXT_FAINT);
        JButton pb = pickButton(b);
        pb.addActionListener(e -> answerDuel(a, b, 1, queue));
        duel.add(pa);
        duel.add(vs, "aligny center");
        duel.add(pb);
        p.add(duel, "align center, wmax " + px(520));

        JButton tie = button("=   No preference", Kind.GHOST);
        tie.addActionListener(e -> answerDuel(a, b, 0, queue));
        p.add(tie, "align center, gaptop " + px(4));

        showScreen(p);
    }

    private void answerDuel(String a, String b, int answer, SynchronousQueue<Integer> queue) {
        if (mode != Mode.DUEL || queue != activeQueue) {
            return; // ignore double answers (key + click, or a stale binding)
        }
        mode = Mode.NONE;
        activeQueue = null;

        String winner = (answer == 0) ? "TIE" : (answer < 0 ? a : b);
        data.comparisons.put(AramSurveyData.pairKey(a, b), winner);
        safeSave();

        try {
            // The worker is already blocked in queue.take(), so this hands off immediately.
            queue.put(answer);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private void offerNext(String doneTier) {
        String next = nextRankableAfter(doneTier);
        if (next == null) {
            showResult();
        } else {
            showOffer(next, doneTier);
        }
    }

    /**
     * First stage after {@code afterTier} (or from the start if null) that has 2+ champions and is
     * not already ranked. Stages with 0-1 champions are auto-ordered, marked done and saved as we
     * pass them (they need no duels).
     */
    private String nextRankableAfter(String afterTier) {
        int start = (afterTier == null) ? 0 : indexOfStage(afterTier) + 1;
        for (int i = Math.max(0, start); i < STAGES.length; i++) {
            String tier = STAGES[i];
            List<String> champs = data.namesInTier(tier);
            if (champs.size() >= 2 && !Boolean.TRUE.equals(data.stageDone.get(tier))) {
                return tier;
            }
            if (champs.size() <= 1) {
                data.rankedOrder.put(tier, new ArrayList<>(champs));
                data.stageDone.put(tier, true);
                safeSave();
            }
        }
        return null;
    }

    private String previousDoneTier(String next) {
        int end = indexOfStage(next);
        for (int i = end - 1; i >= 0; i--) {
            String tier = STAGES[i];
            if (Boolean.TRUE.equals(data.stageDone.get(tier)) && data.namesInTier(tier).size() > 1) {
                return tier;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ offer / encouragement

    private void showOffer(String nextTier, String prevDoneTier) {
        mode = Mode.OFFER;
        hint("");
        setProgress(stageBase(nextTier));
        int n = data.namesInTier(nextTier).size();

        JPanel outer = column();

        JPanel card = new JPanel(new MigLayout("insets 24, wrap 1, gap 6", "[grow,center]"));
        card.setOpaque(true);
        card.setBackground(Theme.SURFACE_2);
        card.setBorder(cardBorder(Theme.LINE));

        JLabel emoji;
        JLabel title;
        JLabel body;
        if (prevDoneTier != null) {
            emoji = makeLabel("🎉", baseFont.deriveFont(fpt(34f)), Theme.TEXT); // party popper
            title = makeLabel("Your " + niceName(prevDoneTier) + " ranking is done!",
                    baseFont.deriveFont(Font.BOLD, fpt(18f)), tierColor(prevDoneTier));
            body = makeLabel("<html><div style='text-align:center'>Keep going and rank your "
                    + niceName(nextTier) + " tier too (" + n + " champs). Every ranked tier makes "
                    + "auto-swap pick sharper when your top champs aren't on the bench.</div></html>",
                    fBody, Theme.TEXT_DIM);
        } else {
            emoji = makeLabel("🎯", baseFont.deriveFont(fpt(34f)), Theme.TEXT); // direct hit
            title = makeLabel("Rank your " + niceName(nextTier) + " tier",
                    baseFont.deriveFont(Font.BOLD, fpt(18f)), tierColor(nextTier));
            body = makeLabel("<html><div style='text-align:center'>Rank your " + niceName(nextTier)
                    + " champs with quick picks (" + n + " champs). Every ranked tier makes auto-swap "
                    + "pick sharper when your top champs aren't on the bench.</div></html>",
                    fBody, Theme.TEXT_DIM);
        }
        emoji.setHorizontalAlignment(SwingConstants.CENTER);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        body.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(emoji, "align center");
        card.add(title, "align center");
        card.add(body, "align center, growx, wmax " + px(420));

        JPanel sell = new JPanel(new MigLayout("insets 6 0 6 0, gap 10", ""));
        sell.setOpaque(false);
        sell.add(pill("One-time setup, never again"));
        sell.add(pill("Sharper auto-swap every game"));
        card.add(sell, "align center");

        JPanel buttons = new JPanel(new MigLayout("insets 0, gap 10", ""));
        buttons.setOpaque(false);
        JButton cont = button("Rank " + niceName(nextTier) + " (" + n + ")", Kind.PRIMARY);
        cont.addActionListener(e -> runStage(nextTier));
        JButton stop = button("Save & finish for now", Kind.GHOST);
        stop.addActionListener(e -> showResult());
        buttons.add(cont);
        buttons.add(stop);
        card.add(buttons, "align center, gaptop " + px(4));

        JLabel foot = makeLabel("You can always continue later - Refine picks up right here.",
                fSub, Theme.TEXT_FAINT);
        foot.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(foot, "align center, gaptop " + px(6));

        outer.add(card, "align center, growx, wmax " + px(520));
        showScreen(outer);
    }

    // ------------------------------------------------------------------ result

    private void showResult() {
        mode = Mode.RESULT;
        hint("");
        setProgress(100);

        JPanel p = new JPanel(new MigLayout("insets 4 6 16 6, wrap 1, gap 2, fillx", "[grow,fill]"));
        p.setOpaque(false);

        JLabel h = makeLabel("Your auto-swap ranking", baseFont.deriveFont(Font.BOLD, fpt(16f)), Theme.TEXT);
        h.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel s = makeLabel("Saved to your PC. Auto-swap uses this order; Never is ignored.",
                fSub, Theme.TEXT_FAINT);
        s.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(h, "growx, gaptop " + px(6));
        p.add(s, "growx, gapbottom " + px(6));

        for (String tier : STAGES) {
            addResultSection(p, tier);
        }

        int neverCount = data.namesInTier("never").size();
        JPanel neverHeader = new JPanel(new MigLayout("insets 14 2 4 2, gap 8", "[]8[]"));
        neverHeader.setOpaque(false);
        neverHeader.add(makeLabel("NEVER", fEyebrow, Theme.TEXT_FAINT));
        neverHeader.add(makeLabel(neverCount + " champs - excluded",
                baseFont.deriveFont(Font.PLAIN, fpt(12f)), Theme.TEXT_FAINT));
        p.add(neverHeader, "growx");

        JButton close = button("Close", Kind.PRIMARY);
        close.addActionListener(e -> closeDialog());
        JPanel foot = new JPanel(new MigLayout("insets 0", "push[]push"));
        foot.setOpaque(false);
        foot.add(close);
        p.add(foot, "growx, gaptop " + px(16));

        showScreen(p);
    }

    private void addResultSection(JPanel parent, String tier) {
        List<String> ranked = data.rankedOrder.get(tier);
        boolean isRanked = Boolean.TRUE.equals(data.stageDone.get(tier)) && ranked != null && ranked.size() > 1;

        List<String> list;
        if (isRanked) {
            list = ranked;
        } else {
            list = new ArrayList<>(data.namesInTier(tier));
            Collections.sort(list);
        }

        parent.add(sectionHeader(tier, isRanked), "growx");

        if (list.isEmpty()) {
            parent.add(makeLabel("none", fSub, Theme.TEXT_FAINT), "gapleft " + px(4));
            return;
        }

        if (isRanked) {
            for (int i = 0; i < list.size(); i++) {
                parent.add(rankedRow(i + 1, list.get(i), tierColor(tier)), "growx");
            }
        } else {
            JPanel grid = new JPanel(new MigLayout("insets 0, wrap 4, gap 6", ""));
            grid.setOpaque(false);
            for (String name : list) {
                grid.add(chip(name));
            }
            parent.add(grid, "growx");
        }
    }

    private JComponent sectionHeader(String tier, boolean isRanked) {
        JPanel h = new JPanel(new MigLayout("insets 14 2 4 2, gap 8", "[]8[]"));
        h.setOpaque(false);
        h.add(makeLabel(niceName(tier).toUpperCase(), fEyebrow, tierColor(tier)));
        String tagText = isRanked ? "✓ ranked in order" : "grouped (rank later to sharpen)";
        h.add(makeLabel(tagText, baseFont.deriveFont(Font.PLAIN, fpt(12f)),
                isRanked ? Theme.GREEN : Theme.TEXT_FAINT));
        return h;
    }

    private JComponent rankedRow(int index, String name, Color color) {
        JPanel r = new JPanel(new MigLayout("insets 5 11 5 11, gap 11", "[26!]11[]11[grow,fill]"));
        r.setOpaque(true);
        r.setBackground(Theme.SURFACE_2);
        r.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        JLabel num = makeLabel(String.valueOf(index), baseFont.deriveFont(Font.BOLD, fpt(13f)), color);
        num.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel ic = new JLabel();
        ic.setPreferredSize(new Dimension(px(34), px(34)));
        ic.setMinimumSize(new Dimension(px(34), px(34)));
        loadIcon(ic, name, px(34));
        r.add(num);
        r.add(ic);
        r.add(makeLabel(name, fBody, Theme.TEXT));
        return r;
    }

    private JComponent chip(String name) {
        JPanel c = new JPanel(new MigLayout("insets 3 5 3 11, gap 7", "[]7[]"));
        c.setOpaque(true);
        c.setBackground(Theme.SURFACE_2);
        c.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        JLabel ic = new JLabel();
        ic.setPreferredSize(new Dimension(px(24), px(24)));
        ic.setMinimumSize(new Dimension(px(24), px(24)));
        loadIcon(ic, name, px(24));
        c.add(ic);
        c.add(makeLabel(name, fBody, Theme.TEXT));
        return c;
    }

    // ------------------------------------------------------------------ error screen

    private void showRosterError() {
        mode = Mode.NONE;
        hint("");
        setProgress(0);
        JPanel p = new JPanel(new MigLayout("insets 24, wrap 1, gap 12", "[grow,center]"));
        p.setOpaque(false);
        JLabel title = makeLabel("Couldn't load champions", baseFont.deriveFont(Font.BOLD, fpt(16f)), Theme.TEXT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(title, "align center");
        JLabel msg = makeLabel("<html><div style='text-align:center'>We could not reach the champion "
                + "list. Check your connection and try again. Any saved progress is kept.</div></html>",
                fSub, Theme.TEXT_DIM);
        msg.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(msg, "align center, growx, wmax " + px(360));
        JButton close = button("Close", Kind.PRIMARY);
        close.addActionListener(e -> closeDialog());
        p.add(close, "align center, gaptop " + px(6));
        showScreen(p);
    }

    // ------------------------------------------------------------------ completion snapshot

    private void maybeSaveCompletionSnapshot() {
        try {
            if (!roster.isEmpty()
                    && data.decidedCount() >= roster.size()
                    && !AramSurveyStore.hasOriginal()) {
                AramSurveyStore.saveOriginalSnapshot(data);
                data.completedAt = System.currentTimeMillis();
                safeSave();
            }
        } catch (Exception e) {
            System.out.println("[Survey] snapshot failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ screen plumbing

    private JPanel column() {
        JPanel p = new JPanel(new MigLayout("insets 8 8 16 8, wrap 1, gap 14", "[grow,center]"));
        p.setOpaque(false);
        return p;
    }

    private void showScreen(JComponent screen) {
        contentPanel.removeAll();
        contentPanel.add(screen, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private void setProgress(int pct) {
        progress.setValue(Math.max(0, Math.min(100, pct)));
    }

    private void hint(String text) {
        hintLabel.setText(text == null || text.isEmpty() ? " " : text);
    }

    private void safeSave() {
        try {
            AramSurveyStore.save(data);
        } catch (Exception e) {
            System.out.println("[Survey] save failed: " + e.getMessage());
        }
        flashSaved();
    }

    private void flashSaved() {
        savedLabel.setText("✓ progress saved");
        if (savedTimer == null) {
            savedTimer = new Timer(900, e -> savedLabel.setText(" "));
            savedTimer.setRepeats(false);
        }
        savedTimer.restart();
    }

    private void closeDialog() {
        if (closed) {
            return;
        }
        closed = true;
        cancelled = true;
        if (rankerThread != null) {
            rankerThread.interrupt();
        }
        dispose();
        try {
            if (onClose != null) {
                onClose.run();
            }
        } catch (Exception e) {
            System.out.println("[Survey] onClose failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ small widgets

    private enum Kind {NORMAL, PRIMARY, DANGER, GHOST}

    private JButton button(String text, Kind kind) {
        JButton b = new JButton(text);
        b.setFont(fBody);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(px(8), px(15), px(8), px(15)));
        Color fg;
        Color bg;
        Color line;
        switch (kind) {
            case PRIMARY -> {
                fg = Theme.ACCENT_TEXT;
                bg = Theme.ACCENT_SOFT;
                line = Theme.ACCENT;
            }
            case DANGER -> {
                fg = Theme.RED;
                bg = Theme.SURFACE_2;
                line = Theme.RED;
            }
            case GHOST -> {
                fg = Theme.TEXT;
                bg = null;
                line = Theme.LINE;
            }
            default -> {
                fg = Theme.TEXT;
                bg = Theme.SURFACE_2;
                line = Theme.LINE;
            }
        }
        b.setForeground(fg);
        if (bg != null) {
            b.setBackground(bg);
            b.setOpaque(true);
            b.setContentAreaFilled(true);
        } else {
            b.setContentAreaFilled(false);
            b.setOpaque(false);
        }
        b.putClientProperty("JComponent.roundRect", true);
        b.putClientProperty("JButton.borderColor", line);
        return b;
    }

    private JButton pickButton(String name) {
        JButton b = new JButton(name);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setIconTextGap(px(10));
        b.setFont(baseFont.deriveFont(Font.BOLD, fpt(16f)));
        b.setForeground(Theme.TEXT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(Theme.SURFACE_2);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createEmptyBorder(px(16), px(12), px(16), px(12)));
        b.setPreferredSize(new Dimension(px(184), px(190)));
        b.putClientProperty("JComponent.roundRect", true);
        b.putClientProperty("JButton.borderColor", Theme.LINE);
        loadButtonIcon(b, name, px(120));
        return b;
    }

    private JComponent tierCard(String key) {
        Color color = tierColor(key);
        JPanel card = new JPanel(new MigLayout("insets 0, wrap 1, gap 3", "[grow,center]"));
        card.setOpaque(true);
        card.setBackground(Theme.SURFACE_2);
        card.setBorder(cardBorder(Theme.LINE));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel t = makeLabel(niceName(key), baseFont.deriveFont(Font.BOLD, fpt(15f)), color);
        t.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel k = makeLabel("press " + keyNumber(key), baseFont.deriveFont(Font.PLAIN, fpt(11f)), Theme.TEXT_FAINT);
        k.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel d = makeLabel(tierDesc(key), baseFont.deriveFont(Font.PLAIN, fpt(11f)), Theme.TEXT_DIM);
        d.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(t, "align center");
        card.add(k, "align center");
        card.add(d, "align center");

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseTier(key);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(cardBorder(Theme.ACCENT));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(cardBorder(Theme.LINE));
            }
        });
        return card;
    }

    private JComponent countChip(String tier) {
        JPanel c = new JPanel(new MigLayout("insets 0, gap 5", ""));
        c.setOpaque(false);
        c.add(makeLabel(niceName(tier), baseFont.deriveFont(Font.PLAIN, fpt(12f)), Theme.TEXT_DIM));
        c.add(makeLabel(String.valueOf(data.namesInTier(tier).size()),
                baseFont.deriveFont(Font.BOLD, fpt(12f)), tierColor(tier)));
        return c;
    }

    private JLabel pill(String text) {
        JLabel l = new JLabel(text);
        l.setFont(baseFont.deriveFont(Font.PLAIN, fpt(11f)));
        l.setForeground(Theme.TEXT_DIM);
        l.setOpaque(true);
        l.setBackground(Theme.SURFACE_1);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(px(5), px(12), px(5), px(12))));
        return l;
    }

    /**
     * Muted context badges for a champion, built from the fetched play history. Returns an empty
     * panel when there is no data for {@code name} (or seeding produced nothing), so callers can
     * skip adding it.
     */
    private JComponent badgesFor(String name) {
        JPanel wrap = new JPanel(new MigLayout("insets 0, gap 6, wrap 3", ""));
        wrap.setOpaque(false);
        Map<String, AramSeeder.ChampInfo> map = infos;
        AramSeeder.ChampInfo info = (map == null) ? null : map.get(name);
        if (info == null) {
            return wrap;
        }
        if (info.masteryLevel > 0 || info.masteryPoints > 0) {
            wrap.add(badgeChip("Mastery " + info.masteryLevel + " - "
                    + String.format("%,d", info.masteryPoints)));
        }
        if (info.highestGrade != null && !info.highestGrade.isBlank()) {
            wrap.add(badgeChip("Best: " + info.highestGrade));
        }
        List<String> games = new ArrayList<>();
        if (info.aramGames > 0) {
            games.add("ARAM " + info.aramGames);
        }
        if (info.mayhemGames > 0) {
            games.add("Mayhem " + info.mayhemGames);
        }
        if (info.srGames > 0) {
            games.add("SR " + info.srGames);
        }
        if (!games.isEmpty()) {
            wrap.add(badgeChip(String.join(" - ", games)));
        }
        if (info.recentWins > 0 || info.recentLosses > 0) {
            wrap.add(badgeChip("recent " + info.recentWins + "W " + info.recentLosses + "L"));
        }
        if (info.lastPlayMillis > 0) {
            wrap.add(badgeChip("last played " + relativeAge(info.lastPlayMillis)));
        }
        return wrap;
    }

    private JLabel badgeChip(String text) {
        JLabel l = new JLabel(text);
        l.setFont(baseFont.deriveFont(Font.PLAIN, fpt(11f)));
        l.setForeground(Theme.TEXT_FAINT);
        l.setOpaque(true);
        l.setBackground(Theme.SURFACE_2);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(px(3), px(9), px(3), px(9))));
        return l;
    }

    private Border cardBorder(Color line) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line),
                BorderFactory.createEmptyBorder(px(11), px(8), px(11), px(8)));
    }

    private JLabel makeLabel(String text, Font font, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(fg);
        return l;
    }

    private void loadIcon(JLabel label, String name, int size) {
        try {
            ChampionIcons.load(name, size, label::setIcon);
        } catch (Exception e) {
            // icons are best effort; a missing icon just shows the name
        }
    }

    private void loadButtonIcon(JButton button, String name, int size) {
        try {
            ChampionIcons.load(name, size, button::setIcon);
        } catch (Exception e) {
            // best effort
        }
    }

    // ------------------------------------------------------------------ tier metadata

    private static String niceName(String tier) {
        return switch (tier) {
            case "main" -> "Main";
            case "like" -> "Like";
            case "fine" -> "Fine";
            case "never" -> "Never";
            default -> tier;
        };
    }

    private static Color tierColor(String tier) {
        return switch (tier) {
            case "main" -> C_MAIN;
            case "like" -> C_LIKE;
            case "fine" -> C_FINE;
            case "never" -> C_NEVER;
            default -> Theme.TEXT;
        };
    }

    private static String tierDesc(String tier) {
        return switch (tier) {
            case "main" -> "Best champs";
            case "like" -> "Happy to play";
            case "fine" -> "If nothing better";
            case "never" -> "Won't swap";
            default -> "";
        };
    }

    private static int keyNumber(String tier) {
        return switch (tier) {
            case "main" -> 1;
            case "like" -> 2;
            case "fine" -> 3;
            case "never" -> 4;
            default -> 0;
        };
    }

    private static int stageBase(String tier) {
        return switch (tier) {
            case "main" -> 45;
            case "like" -> 65;
            case "fine" -> 82;
            default -> 45;
        };
    }

    private static int indexOfStage(String tier) {
        for (int i = 0; i < STAGES.length; i++) {
            if (STAGES[i].equals(tier)) {
                return i;
            }
        }
        return -1;
    }

    /** Compact relative age of a past epoch-millis timestamp, e.g. "3d ago". */
    private static String relativeAge(long millis) {
        long diff = System.currentTimeMillis() - millis;
        if (diff < 0) {
            diff = 0;
        }
        long minutes = diff / 60_000L;
        if (minutes < 60) {
            return Math.max(1, minutes) + "m ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = hours / 24;
        if (days < 30) {
            return days + "d ago";
        }
        long months = days / 30;
        if (months < 12) {
            return months + "mo ago";
        }
        return (days / 365) + "y ago";
    }

    // ------------------------------------------------------------------ loading helpers

    private static AramSurveyData safeLoad() {
        try {
            AramSurveyData d = AramSurveyStore.load();
            return d == null ? AramSurveyData.empty() : d;
        } catch (Exception e) {
            System.out.println("[Survey] load failed: " + e.getMessage());
            return AramSurveyData.empty();
        }
    }

    private static List<String> safeRoster() {
        try {
            List<String> names = DDragonParser.fetchChampionNames();
            return names == null ? new ArrayList<>() : names;
        } catch (Exception e) {
            System.out.println("[Survey] roster load failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Font resolveBaseFont() {
        Font f = UIManager.getFont("defaultFont");
        if (f == null) {
            f = UIManager.getFont("Label.font");
        }
        if (f == null) {
            f = new JLabel().getFont();
        }
        if (f == null) {
            f = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        return f;
    }

    // ------------------------------------------------------------------ scroll body

    private static float fpt(float pt) {
        return Math.round(UIScale.scale(pt));
    }

    private static int px(int v) {
        return UIScale.scale(v);
    }

    /** Content host that fills the scroll viewport width and scrolls only vertically. */
    private static final class ContentPanel extends JPanel implements Scrollable {
        ContentPanel() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}

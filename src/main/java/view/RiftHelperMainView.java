package view;

import model.DDragonParser;
import model.LCUAuth;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

/**
 * Main window, redesigned as a dense rail-nav layout (Swing + FlatLaf + MigLayout).
 *
 * The public API (getters, setters, add*Listener methods, and the enable/disable JButton fields)
 * is preserved exactly so the controller's behavior is unchanged. Each on/off feature is shown as a
 * {@link ToggleSwitch} that drives the hidden Enable/Disable buttons the controller still wires,
 * and champion selectors are {@link ChampionPicker}s behind the old String-based combo API.
 */
public class RiftHelperMainView extends JFrame {

    private Font fTitle;
    private Font fBody;
    private Font fBodyBold;
    private Font fSub;
    private Font fEyebrow;

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final ButtonGroup navGroup = new ButtonGroup();

    // ---- Enable/Disable buttons: never shown, kept so the controller wiring is unchanged. ----
    public final JButton buttonAutoAcceptEnable = new JButton();
    public final JButton buttonAutoAcceptDisable = new JButton();
    public final JButton buttonAutoDeclineEnable = new JButton();
    public final JButton buttonAutoDeclineDisable = new JButton();
    public final JButton buttonAutoLockEnable = new JButton();
    public final JButton buttonAutoLockDisable = new JButton();
    public final JButton buttonAutoBanEnable = new JButton();
    public final JButton buttonAutoBanDisable = new JButton();
    public final JButton buttonAutoSwapEnable = new JButton();
    public final JButton buttonAutoSwapDisable = new JButton();
    public final JButton buttonAlwaysOnTopEnable = new JButton();
    public final JButton buttonAlwaysOnTopDisable = new JButton();
    public final JButton buttonCenterGUIEnable = new JButton();
    public final JButton buttonCenterGUIDisable = new JButton();
    public final JButton buttonAutoCheckUpdateEnable = new JButton();
    public final JButton buttonAutoCheckUpdateDisable = new JButton();
    public final JButton buttonAutoLockArenaEnable = new JButton();
    public final JButton buttonAutoLockArenaDisable = new JButton();
    public final JButton buttonAutoBanArenaEnable = new JButton();
    public final JButton buttonAutoBanArenaDisable = new JButton();
    public final JButton buttonAutoBanCrowdFavoriteEnable = new JButton();
    public final JButton buttonAutoBanCrowdFavoriteDisable = new JButton();
    public final JButton buttonAutoBraveryArenaEnable = new JButton();
    public final JButton buttonAutoBraveryArenaDisable = new JButton();
    public final JButton buttonSystemTrayEnable = new JButton();
    public final JButton buttonSystemTrayDisable = new JButton();
    public final JButton buttonAutoHonorEnable = new JButton();
    public final JButton buttonAutoHonorDisable = new JButton();
    public final JButton buttonAutoSkipScreensEnable = new JButton();
    public final JButton buttonAutoSkipScreensDisable = new JButton();
    public final JButton buttonGroupAutoQueueEnable = new JButton();
    public final JButton buttonGroupAutoQueueDisable = new JButton();
    public final JButton buttonSoloAutoQueueEnable = new JButton();
    public final JButton buttonSoloAutoQueueDisable = new JButton();
    public final JButton buttonAutoMinimizeEnable = new JButton();
    public final JButton buttonAutoMinimizeDisable = new JButton();

    // ---- Action buttons ----
    private final JButton buttonChangeResponseAccept = new JButton();
    private final JButton buttonChangeResponseDecline = new JButton();
    private final JButton buttonAutoSwapSave = new JButton();
    private final JButton buttonAutoSwapAdd = new JButton();
    private final JButton buttonAutoSwapSubtract = new JButton();
    private final JButton buttonAutoLockSave = new JButton();
    private final JButton buttonAutoBanSave = new JButton();
    private final JButton buttonAutoLockArenaSave = new JButton();
    private final JButton buttonAutoBanArenaSave = new JButton();
    private final JButton buttonImport = new JButton();
    private final JButton buttonExport = new JButton();
    private final JButton buttonReset = new JButton();
    private final JButton buttonDisenchantChampionsSafe = new JButton();
    private final JButton buttonDisenchantChampionsHard = new JButton();
    private final JButton buttonDisenchantSkinsSafe = new JButton();
    private final JButton buttonDisenchantSkinsHard = new JButton();
    private final JButton buttonTest = new JButton();

    // Lane selector buttons (Summoner's Rift Auto Lock).
    private final JButton buttonTop = new JButton();
    private final JButton buttonJungle = new JButton();
    private final JButton buttonMid = new JButton();
    private final JButton buttonBot = new JButton();
    private final JButton buttonSupport = new JButton();

    // ---- Champion pickers ----
    private final ChampionPicker[] top = pickers(5);
    private final ChampionPicker[] jungle = pickers(5);
    private final ChampionPicker[] mid = pickers(5);
    private final ChampionPicker[] bot = pickers(5);
    private final ChampionPicker[] support = pickers(5);
    private final ChampionPicker[] autoBan = pickers(5);
    private final ChampionPicker[] arenaLock = pickers(5);
    private final ChampionPicker[] arenaBan = pickers(5);
    private final ChampionPicker[] swap = pickers(10);
    private final JLabel[] swapLabels = new JLabel[10];

    // ---- Bench ----
    private final ChampionButton[] bench = new ChampionButton[10];
    public JPanel panelQuickSwitchBench2;

    // Lane card switching.
    private final CardLayout laneCards = new CardLayout();
    private final JPanel lanePanel = new JPanel(laneCards);

    // System tray.
    private SystemTray tray;
    private TrayIcon trayIcon;
    private boolean systemTrayEnabled = false;
    private boolean said = false;

    public RiftHelperMainView() {
        initFonts();

        setTitle("Rift Helper");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setWindowIcon();

        JPanel rootPanel = new JPanel(new MigLayout("insets 0, gap 0, fill", "[]0[grow,fill]", "[grow,fill]"));
        rootPanel.setBackground(Theme.BG);

        JPanel rail = buildRail();
        JPanel main = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill]", "[]0[grow,fill]"));
        main.setOpaque(false);
        main.add(buildStatusStrip(), "growx, wrap");
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));
        buildPanels();
        main.add(content, "grow");

        rootPanel.add(rail, "growy");
        rootPanel.add(main, "grow");
        setContentPane(rootPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                if (systemTrayEnabled) {
                    minimizeToTray();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (systemTrayEnabled) {
                    minimizeToTray();
                } else {
                    System.exit(0);
                }
            }
        });

        // Initial off-states (mirrors the original constructor).
        buttonAutoAcceptDisable.setEnabled(false);
        buttonAutoDeclineDisable.setEnabled(false);
        buttonAutoLockDisable.setEnabled(false);
        buttonAutoBanDisable.setEnabled(false);
        buttonAutoSwapDisable.setEnabled(false);
        buttonAutoBraveryArenaDisable.setEnabled(false);
        buttonAutoLockArenaDisable.setEnabled(false);
        buttonAutoBanArenaDisable.setEnabled(false);
        buttonAutoBanCrowdFavoriteDisable.setEnabled(false);

        List<String> championNames = DDragonParser.fetchChampionNames();
        for (ChampionPicker[] group : new ChampionPicker[][]{top, jungle, mid, bot, support, autoBan, arenaLock, arenaBan, swap}) {
            for (ChampionPicker picker : group) {
                picker.setItems(championNames);
            }
        }
        // Warm the icon cache so pickers/bench render instantly (sizes used: face 22, list 24, bench 32).
        DDragonParser.prefetchIcons(22, 24, 32);

        // Auto-save: any champion change fires the existing (now silent) save handler, so there are
        // no Save buttons. Programmatic setSelectedName during preference load does not fire this.
        autoSave(buttonAutoLockSave, top, jungle, mid, bot, support);
        autoSave(buttonAutoBanSave, autoBan);
        autoSave(buttonAutoLockArenaSave, arenaLock);
        autoSave(buttonAutoBanArenaSave, arenaBan);
        autoSave(buttonAutoSwapSave, swap);

        selectLane(0);
        pack();
        setMinimumSize(new Dimension(760, Math.min(getHeight(), 640)));
        setLocationRelativeTo(null);
        setVisible(false);
    }

    private void initFonts() {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        fBody = base.deriveFont(Font.PLAIN, 13f);
        fBodyBold = base.deriveFont(Font.BOLD, 13f);
        fTitle = base.deriveFont(Font.BOLD, 15f);
        // Integer point sizes only: fractional sizes make Swing's label renderer open gaps mid-word.
        fSub = base.deriveFont(Font.PLAIN, 12f);
        fEyebrow = base.deriveFont(Font.BOLD, 11f);
    }

    private static ChampionPicker[] pickers(int n) {
        ChampionPicker[] arr = new ChampionPicker[n];
        for (int i = 0; i < n; i++) {
            arr[i] = new ChampionPicker();
        }
        return arr;
    }

    // ---------------------------------------------------------------- rail + status

    private JPanel buildRail() {
        JPanel rail = new JPanel(new MigLayout("insets 10 8 10 8, wrap 1, gap 3, fillx", "[grow,fill]"));
        rail.setBackground(Theme.RAIL);
        rail.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.LINE));

        JPanel brand = new JPanel(new MigLayout("insets 4 6 10 6, gap 8", "[]8[]"));
        brand.setOpaque(false);
        ImageIcon markIcon = kindredMark(22);
        JLabel mark = markIcon != null ? new JLabel(markIcon) : new JLabel();
        JLabel name = new JLabel("Rift Helper");
        name.setFont(fTitle);
        name.setForeground(Theme.TEXT);
        brand.add(mark);
        brand.add(name);
        rail.add(brand, "growx");

        rail.add(navButton("Lobby", Icons.G.LOBBY, "lobby", true), "growx");
        rail.add(navButton("Summoner's Rift", Icons.G.RIFT, "rift", false), "growx");
        rail.add(navButton("ARAM", Icons.G.ARAM, "aram", false), "growx");
        rail.add(navButton("Arena", Icons.G.ARENA, "arena", false), "growx");
        rail.add(navButton("Loot", Icons.G.LOOT, "loot", false), "growx");
        rail.add(Box.createGlue(), "growy, push");
        rail.add(navButton("Settings", Icons.G.SETTINGS, "settings", false), "growx");
        rail.add(navButton("Info", Icons.G.INFO, "info", false), "growx");
        return rail;
    }

    private NavButton navButton(String text, Icons.G glyph, String card, boolean selected) {
        NavButton b = new NavButton(text, glyph);
        b.setSelected(selected);
        navGroup.add(b);
        b.addActionListener(e -> cards.show(content, card));
        return b;
    }

    private JPanel buildStatusStrip() {
        JPanel strip = new JPanel(new MigLayout("insets 8 16 8 16, gap 14", "[]14[]14[]push[]", "[]"));
        strip.setBackground(Theme.SURFACE_1);
        strip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));

        JLabel connected = new JLabel("Connected");
        connected.setIcon(dotIcon(Theme.GREEN, 9));
        connected.setFont(fBodyBold);
        connected.setForeground(Theme.TEXT);
        connected.setIconTextGap(7);

        JLabel client = new JLabel("LeagueClientUx");
        client.setFont(fSub);
        client.setForeground(Theme.TEXT_DIM);

        String portText = LCUAuth.port == null ? "" : ("Port " + LCUAuth.port);
        JLabel port = new JLabel(portText);
        port.setFont(fSub);
        port.setForeground(Theme.TEXT_DIM);

        strip.add(connected);
        strip.add(client);
        strip.add(port);
        return strip;
    }

    // ---------------------------------------------------------------- panels

    private void buildPanels() {
        content.add(buildLobby(), "lobby");
        content.add(buildRift(), "rift");
        content.add(buildAram(), "aram");
        content.add(buildArena(), "arena");
        content.add(buildLoot(), "loot");
        content.add(buildSettings(), "settings");
        content.add(buildInfo(), "info");
    }

    private JPanel section(String title, String sub) {
        JPanel panel = new JPanel(new MigLayout("insets 2, wrap 1, gap 12, fillx", "[grow,fill]"));
        panel.setOpaque(false);
        panel.add(header(title, sub), "growx");
        return panel;
    }

    private JPanel header(String title, String sub) {
        JPanel head = new JPanel(new MigLayout("insets 0 2 0 2, gap 10", "[]10[]push"));
        head.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(fTitle.deriveFont(17f));
        t.setForeground(Theme.TEXT);
        head.add(t);
        if (sub != null) {
            JLabel s = new JLabel(sub);
            s.setFont(fSub);
            s.setForeground(Theme.TEXT_FAINT);
            head.add(s);
        }
        return head;
    }

    private JLabel cardTitle(String text, Icons.G glyph) {
        JLabel l = new JLabel(text);
        l.setFont(fBodyBold.deriveFont(14f));
        l.setForeground(Theme.TEXT);
        if (glyph != null) {
            l.setIcon(Icons.of(glyph, 15, Theme.ACCENT));
            l.setIconTextGap(8);
        }
        return l;
    }

    /** A row: label + optional description on the left, a ToggleSwitch on the right. */
    private JPanel toggleRow(String label, String desc, JButton enable, JButton disable) {
        JPanel row = new JPanel(new MigLayout("insets 6 2 6 2, gap 12, fillx", "[grow,fill][]"));
        row.setOpaque(false);
        JPanel textCol = new JPanel(new MigLayout("insets 0, wrap 1, gap 1", "[grow,fill]"));
        textCol.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(fBody);
        l.setForeground(Theme.TEXT);
        textCol.add(l, "growx");
        if (desc != null) {
            JLabel d = new JLabel(desc);
            d.setFont(fSub);
            d.setForeground(Theme.TEXT_FAINT);
            textCol.add(d, "growx");
        }
        row.add(textCol, "growx");
        row.add(new ToggleSwitch(enable, disable));
        return row;
    }

    private JPanel divider() {
        JPanel line = new JPanel();
        line.setBackground(Theme.LINE_SOFT);
        line.setPreferredSize(new Dimension(10, 1));
        line.setMinimumSize(new Dimension(10, 1));
        return line;
    }

    // ---- Lobby ----

    private JPanel buildLobby() {
        JPanel panel = section("Lobby", "queue and ready-check automation");

        Card toggles = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        toggles.add(toggleRow("Auto Accept", "Instantly accept the ready-check when a match is found.",
                buttonAutoAcceptEnable, buttonAutoAcceptDisable), "growx");
        toggles.add(divider(), "growx, gapy 2 2");
        toggles.add(toggleRow("Auto Decline", "Automatically decline the ready-check.",
                buttonAutoDeclineEnable, buttonAutoDeclineDisable), "growx");
        panel.add(toggles, "growx");

        JLabel note = new JLabel("Auto Accept and Auto Decline are mutually exclusive.");
        note.setFont(fSub);
        note.setForeground(Theme.TEXT_FAINT);
        panel.add(note, "gapx 4");

        Card loop = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        loop.add(cardTitle("Auto Game Loop", Icons.G.UPDATE), "gapbottom 6");
        loop.add(toggleRow("Auto Minimize (DND)", "Minimize the client after each automated action. Bring it up any time.",
                buttonAutoMinimizeEnable, buttonAutoMinimizeDisable), "growx");
        loop.add(divider(), "growx, gapy 2 2");
        loop.add(toggleRow("Auto Honor", "Honor your friends in the post-game lobby (up to 4).",
                buttonAutoHonorEnable, buttonAutoHonorDisable), "growx");
        loop.add(divider(), "growx, gapy 2 2");
        loop.add(toggleRow("Auto Skip Progression and Scoreboard", "Return to the lobby immediately after a game.",
                buttonAutoSkipScreensEnable, buttonAutoSkipScreensDisable), "growx");
        loop.add(divider(), "growx, gapy 2 2");
        loop.add(toggleRow("Group Auto Queue", "Auto Find Match when 2 or more are in the lobby.",
                buttonGroupAutoQueueEnable, buttonGroupAutoQueueDisable), "growx");
        loop.add(divider(), "growx, gapy 2 2");
        loop.add(toggleRow("Solo Auto Queue", "Auto Find Match when you are alone. Warning: loops games unattended.",
                buttonSoloAutoQueueEnable, buttonSoloAutoQueueDisable), "growx");
        panel.add(loop, "growx, gaptop 6");

        JLabel loopNote = new JLabel("Group and Solo Auto Queue are mutually exclusive.");
        loopNote.setFont(fSub);
        loopNote.setForeground(Theme.TEXT_FAINT);
        panel.add(loopNote, "gapx 4");
        return panel;
    }

    // ---- Summoner's Rift ----

    private JPanel buildRift() {
        JPanel panel = section("Summoner's Rift", "ranked and draft pick / ban priorities");

        JPanel cols = new JPanel(new MigLayout("insets 0, gap 12, fillx", "[grow,fill]12[grow,fill]", "[top]"));
        cols.setOpaque(false);

        Card lock = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        lock.add(cardHeaderWithToggle("Auto Lock", Icons.G.LOCK, buttonAutoLockEnable, buttonAutoLockDisable), "growx, gapbottom 10");
        lock.add(buildLaneSelector(), "growx, gapbottom 10");
        lanePanel.setOpaque(false);
        lanePanel.add(priorityColumn(top), "top");
        lanePanel.add(priorityColumn(jungle), "jungle");
        lanePanel.add(priorityColumn(mid), "mid");
        lanePanel.add(priorityColumn(bot), "bot");
        lanePanel.add(priorityColumn(support), "support");
        lock.add(lanePanel, "growx");
        cols.add(lock, "grow");

        Card ban = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        ban.add(cardHeaderWithToggle("Auto Ban", Icons.G.BAN, buttonAutoBanEnable, buttonAutoBanDisable), "growx, gapbottom 10");
        ban.add(priorityColumn(autoBan), "growx");
        cols.add(ban, "grow");

        panel.add(cols, "growx");
        return panel;
    }

    private JPanel buildLaneSelector() {
        JPanel seg = new JPanel(new MigLayout("insets 0, gap 4", ""));
        seg.setOpaque(false);
        styleLaneButton(buttonTop, "Top");
        styleLaneButton(buttonJungle, "Jgl");
        styleLaneButton(buttonMid, "Mid");
        styleLaneButton(buttonBot, "Bot");
        styleLaneButton(buttonSupport, "Sup");
        seg.add(buttonTop);
        seg.add(buttonJungle);
        seg.add(buttonMid);
        seg.add(buttonBot);
        seg.add(buttonSupport);
        return seg;
    }

    // ---- ARAM ----

    private JPanel buildAram() {
        JPanel panel = section("ARAM", "bench swaps");

        JPanel cols = new JPanel(new MigLayout("insets 0, gap 12, fillx", "[grow,fill]12[grow,fill]", "[top]"));
        cols.setOpaque(false);

        Card swapCard = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        swapCard.add(cardHeaderWithToggle("Auto Swap", Icons.G.SWAP, buttonAutoSwapEnable, buttonAutoSwapDisable), "gapbottom 10");
        JPanel swapGrid = new JPanel(new MigLayout("insets 0, wrap 2, gapy 6", "[18!]8[grow,fill]"));
        swapGrid.setOpaque(false);
        for (int i = 0; i < swap.length; i++) {
            swapLabels[i] = numberLabel(i + 1);
            swapGrid.add(swapLabels[i]);
            swapGrid.add(swap[i], "growx");
        }
        swapCard.add(swapGrid, "growx");
        JPanel swapActions = new JPanel(new MigLayout("insets 0, gap 8", "[]4[]"));
        swapActions.setOpaque(false);
        styleButton(buttonAutoSwapSubtract, "", Icons.G.MINUS, ButtonKind.GHOST);
        styleButton(buttonAutoSwapAdd, "", Icons.G.PLUS, ButtonKind.GHOST);
        swapActions.add(buttonAutoSwapSubtract);
        swapActions.add(buttonAutoSwapAdd);
        swapCard.add(swapActions, "gaptop 10");
        cols.add(swapCard, "grow");

        Card right = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        right.add(cardTitle("Quick Switch Bench", Icons.G.SWAP), "growx, gapbottom 12");
        JPanel benchGrid = new JPanel(new MigLayout("insets 0, wrap 5, gap 7", "[grow,fill]", ""));
        benchGrid.setOpaque(false);
        panelQuickSwitchBench2 = new JPanel(new MigLayout("insets 0, wrap 5, gap 7", "[grow,fill]", ""));
        panelQuickSwitchBench2.setOpaque(false);
        for (int i = 0; i < bench.length; i++) {
            bench[i] = new ChampionButton();
            bench[i].setPreferredSize(new Dimension(58, 58));
            if (i < 5) {
                benchGrid.add(bench[i], "grow");
            } else {
                panelQuickSwitchBench2.add(bench[i], "grow");
            }
        }
        right.add(benchGrid, "growx");
        right.add(panelQuickSwitchBench2, "growx, gaptop 7");
        cols.add(right, "grow");

        panel.add(cols, "growx");
        return panel;
    }

    // ---- Arena ----

    private JPanel buildArena() {
        JPanel panel = section("Arena", "2v2v2v2 lock, ban and bravery");

        JPanel cols = new JPanel(new MigLayout("insets 0, gap 12, fillx", "[grow,fill]12[grow,fill]", "[top]"));
        cols.setOpaque(false);

        Card lock = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        lock.add(cardHeaderWithToggle("Auto Lock", Icons.G.LOCK, buttonAutoLockArenaEnable, buttonAutoLockArenaDisable), "growx, gapbottom 10");
        lock.add(priorityColumn(arenaLock), "growx");
        JLabel lockNote = new JLabel("Disabled while Auto Bravery is on.");
        lockNote.setFont(fSub);
        lockNote.setForeground(Theme.TEXT_FAINT);
        lock.add(lockNote, "gaptop 6");
        cols.add(lock, "grow");

        Card ban = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        ban.add(cardHeaderWithToggle("Auto Ban", Icons.G.BAN, buttonAutoBanArenaEnable, buttonAutoBanArenaDisable), "growx, gapbottom 10");
        ban.add(priorityColumn(arenaBan), "growx");
        JLabel banNote = new JLabel("Disabled while Auto Ban Crowd Favorite is on.");
        banNote.setFont(fSub);
        banNote.setForeground(Theme.TEXT_FAINT);
        ban.add(banNote, "gaptop 6");
        cols.add(ban, "grow");

        panel.add(cols, "growx");

        Card extras = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        extras.add(toggleRow("Auto Bravery", "Auto-lock the game's random Bravery pick.",
                buttonAutoBraveryArenaEnable, buttonAutoBraveryArenaDisable), "growx");
        extras.add(divider(), "growx, gapy 2 2");
        extras.add(toggleRow("Auto Ban Crowd Favorite", "Ban from the crowd-favorite champion list.",
                buttonAutoBanCrowdFavoriteEnable, buttonAutoBanCrowdFavoriteDisable), "growx");
        panel.add(extras, "growx");
        return panel;
    }

    // ---- Loot ----

    private JPanel buildLoot() {
        JPanel panel = section("Loot", "mass disenchant");

        Card champs = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        champs.add(cardTitle("Mass Champion Disenchant", Icons.G.LOOT), "gapbottom 10");
        JPanel cbtns = new JPanel(new MigLayout("insets 0, gap 8", ""));
        cbtns.setOpaque(false);
        styleButton(buttonDisenchantChampionsSafe, "Safe  -  owned only", null, ButtonKind.NORMAL);
        styleButton(buttonDisenchantChampionsHard, "Hard  -  everything", null, ButtonKind.DANGER);
        cbtns.add(buttonDisenchantChampionsSafe);
        cbtns.add(buttonDisenchantChampionsHard);
        champs.add(cbtns);
        JLabel note = new JLabel("Both modes ask for confirmation and a typed code. Hard mode cannot be undone.");
        note.setFont(fSub);
        note.setForeground(Theme.AMBER);
        champs.add(note, "gaptop 10");
        panel.add(champs, "growx");

        Card skins = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        skins.add(cardTitle("Mass Skin Disenchant", null), "gapbottom 10");
        JPanel sbtns = new JPanel(new MigLayout("insets 0, gap 8", ""));
        sbtns.setOpaque(false);
        styleButton(buttonDisenchantSkinsSafe, "Safe  -  owned only", null, ButtonKind.NORMAL);
        styleButton(buttonDisenchantSkinsHard, "Hard  -  everything", null, ButtonKind.NORMAL);
        sbtns.add(buttonDisenchantSkinsSafe);
        sbtns.add(buttonDisenchantSkinsHard);
        skins.add(sbtns);
        panel.add(skins, "growx");
        return panel;
    }

    // ---- Settings ----

    private JPanel buildSettings() {
        JPanel panel = section("Settings", "app behavior and preferences");

        JPanel cols = new JPanel(new MigLayout("insets 0, gap 12, fillx", "[grow,fill]12[grow,fill]", "[top]"));
        cols.setOpaque(false);

        Card window = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        window.add(cardTitle("Window and Updates", Icons.G.PIN), "gapbottom 6");
        window.add(toggleRow("Always On Top", null, buttonAlwaysOnTopEnable, buttonAlwaysOnTopDisable), "growx");
        window.add(toggleRow("Center GUI on Update", null, buttonCenterGUIEnable, buttonCenterGUIDisable), "growx");
        window.add(toggleRow("System Tray", null, buttonSystemTrayEnable, buttonSystemTrayDisable), "growx");
        window.add(toggleRow("Auto Check for Updates", null, buttonAutoCheckUpdateEnable, buttonAutoCheckUpdateDisable), "growx");
        cols.add(window, "grow");

        Card prefs = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        prefs.add(cardTitle("Preferences", Icons.G.SAVE), "gapbottom 6");
        JLabel blurb = new JLabel("<html>Save your setup to a file, load it on another PC, or reset to defaults.</html>");
        blurb.setFont(fSub);
        blurb.setForeground(Theme.TEXT_DIM);
        prefs.add(blurb, "growx, gapbottom 12");
        JPanel pbtns = new JPanel(new MigLayout("insets 0, gap 8", "[]8[]push[]"));
        pbtns.setOpaque(false);
        styleButton(buttonExport, "Export", Icons.G.EXPORT, ButtonKind.NORMAL);
        styleButton(buttonImport, "Import", Icons.G.IMPORT, ButtonKind.NORMAL);
        styleButton(buttonReset, "Reset all", Icons.G.RESET, ButtonKind.DANGER);
        pbtns.add(buttonExport);
        pbtns.add(buttonImport);
        pbtns.add(buttonReset);
        prefs.add(pbtns, "growx");
        cols.add(prefs, "grow");

        panel.add(cols, "growx");
        return panel;
    }

    // ---- Info ----

    private JPanel buildInfo() {
        JPanel panel = section("Info", "about");

        Card about = new Card("insets 8 8 12 8, gap 16", "[]16[grow,fill]", "[]");
        ImageIcon big = kindredMark(64);
        about.add(big != null ? new JLabel(big) : new JLabel(), "aligny top");
        JPanel textCol = new JPanel(new MigLayout("insets 0, wrap 1, gap 4", "[grow,fill]"));
        textCol.setOpaque(false);
        JLabel name = new JLabel("Rift Helper");
        name.setFont(fTitle.deriveFont(20f));
        name.setForeground(Theme.TEXT);
        JLabel blurb = new JLabel("<html>Automates queue, champ select, and lobby actions through the League Client (LCU) API. No injection, no memory reads.</html>");
        blurb.setFont(fSub);
        blurb.setForeground(Theme.TEXT_DIM);
        textCol.add(name);
        textCol.add(blurb, "growx");
        about.add(textCol, "growx");
        panel.add(about, "growx");

        Card facts = new Card("insets 8, wrap 2, gap 8 16", "[]16[grow,fill]", "");
        facts.add(kv("Version"));
        facts.add(kvValue("1.3.2"));
        facts.add(kv("Data"));
        facts.add(kvValue("Data Dragon " + DDragonParser.getVersion()));
        facts.add(kv("Client link"));
        facts.add(kvValue("LCU API on 127.0.0.1"));
        facts.add(kv("Discovery"));
        facts.add(kvValue("PowerShell CIM (wmic fallback)"));
        panel.add(facts, "growx");

        styleButton(buttonTest, "Test endpoints", Icons.G.TEST, ButtonKind.GHOST);
        panel.add(buttonTest, "gaptop 4");
        return panel;
    }

    private JLabel kv(String text) {
        JLabel l = new JLabel(text);
        l.setFont(fSub);
        l.setForeground(Theme.TEXT_FAINT);
        return l;
    }

    private JLabel kvValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(fBody);
        l.setForeground(Theme.TEXT);
        return l;
    }

    // ---------------------------------------------------------------- shared builders

    private JPanel cardHeaderWithToggle(String title, Icons.G glyph, JButton enable, JButton disable) {
        JPanel head = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[]push[]"));
        head.setOpaque(false);
        head.add(cardTitle(title, glyph));
        head.add(new ToggleSwitch(enable, disable));
        return head;
    }

    private JPanel priorityColumn(ChampionPicker[] group) {
        JPanel grid = new JPanel(new MigLayout("insets 0, wrap 2, gapy 6", "[18!]8[grow,fill]"));
        grid.setOpaque(false);
        for (int i = 0; i < group.length; i++) {
            grid.add(numberLabel(i + 1));
            grid.add(group[i], "growx");
        }
        return grid;
    }

    private JLabel numberLabel(int n) {
        JLabel l = new JLabel(String.valueOf(n));
        l.setFont(fSub);
        l.setForeground(Theme.TEXT_FAINT);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void autoSave(JButton saveButton, ChampionPicker[]... groups) {
        Runnable r = saveButton::doClick;
        for (ChampionPicker[] group : groups) {
            for (ChampionPicker picker : group) {
                picker.setOnChange(r);
            }
        }
    }

    private enum ButtonKind { NORMAL, PRIMARY, DANGER, GHOST }

    private void styleButton(JButton b, String text, Icons.G glyph, ButtonKind kind) {
        b.setText(text);
        b.setFont(fBody);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 13, 7, 13));
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
        if (glyph != null) {
            b.setIcon(Icons.of(glyph, 15, fg));
            b.setIconTextGap(text == null || text.isEmpty() ? 0 : 7);
        }
    }

    private void styleLaneButton(JButton b, String text) {
        b.setText(text);
        b.setFont(fBody);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(Theme.SURFACE_0);
        b.setForeground(Theme.TEXT_DIM);
        b.setIconTextGap(6);
        b.putClientProperty("JComponent.roundRect", true);
    }

    private void selectLane(int index) {
        JButton[] laneButtons = {buttonTop, buttonJungle, buttonMid, buttonBot, buttonSupport};
        Icons.G[] glyphs = {Icons.G.TOP, Icons.G.JUNGLE, Icons.G.MID, Icons.G.BOT, Icons.G.SUPPORT};
        String[] laneNames = {"top", "jungle", "mid", "bot", "support"};
        for (int i = 0; i < laneButtons.length; i++) {
            boolean on = i == index;
            laneButtons[i].setBackground(on ? Theme.ACCENT_SOFT : Theme.SURFACE_0);
            laneButtons[i].setForeground(on ? Theme.ACCENT_TEXT : Theme.TEXT_DIM);
            laneButtons[i].setIcon(Icons.of(glyphs[i], 14, on ? Theme.ACCENT_TEXT : Theme.TEXT_DIM));
        }
        laneCards.show(lanePanel, laneNames[index]);
    }

    // ---------------------------------------------------------------- icons

    private ImageIcon kindredMark(int size) {
        try (InputStream is = getClass().getResourceAsStream("/Kindred.png")) {
            if (is != null) {
                Image img = ImageIO.read(is).getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignore) {
            // fall through
        }
        return null;
    }

    private ImageIcon dotIcon(Color color, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(0, 0, size, size);
        g2.dispose();
        return new ImageIcon(img);
    }

    private void setWindowIcon() {
        try (InputStream is = getClass().getResourceAsStream("/Kindred.png")) {
            if (is != null) {
                setIconImage(ImageIO.read(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------- system tray

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

    // ---------------------------------------------------------------- champion selection API

    private static String nameOf(ChampionPicker p) {
        return p.getSelectedName();
    }

    private static void setNames(ChampionPicker[] group, String[] values) {
        for (int i = 0; i < group.length && i < values.length; i++) {
            group[i].setSelectedName(values[i]);
        }
    }

    public void setComboBoxAutoSwapPriority(String[] priorityChampions) {
        setNames(swap, priorityChampions);
    }

    public void setComboBoxTopPriority(String[] priorityChampions) {
        setNames(top, priorityChampions);
    }

    public void setComboBoxJunglePriority(String[] priorityChampions) {
        setNames(jungle, priorityChampions);
    }

    public void setComboBoxMidPriority(String[] priorityChampions) {
        setNames(mid, priorityChampions);
    }

    public void setComboBoxBotPriority(String[] priorityChampions) {
        setNames(bot, priorityChampions);
    }

    public void setComboBoxSupportPriority(String[] priorityChampions) {
        setNames(support, priorityChampions);
    }

    public void setComboBoxAutoBanPriority(String[] priorityChampions) {
        setNames(autoBan, priorityChampions);
    }

    public void setComboBoxAutoLockArenaPriority(String[] priorityChampions) {
        setNames(arenaLock, priorityChampions);
    }

    public void setComboBoxAutoBanArenaPriority(String[] priorityChampions) {
        setNames(arenaBan, priorityChampions);
    }

    public String getComboBoxTop1() { return nameOf(top[0]); }
    public String getComboBoxTop2() { return nameOf(top[1]); }
    public String getComboBoxTop3() { return nameOf(top[2]); }
    public String getComboBoxTop4() { return nameOf(top[3]); }
    public String getComboBoxTop5() { return nameOf(top[4]); }

    public String getComboBoxJungle1() { return nameOf(jungle[0]); }
    public String getComboBoxJungle2() { return nameOf(jungle[1]); }
    public String getComboBoxJungle3() { return nameOf(jungle[2]); }
    public String getComboBoxJungle4() { return nameOf(jungle[3]); }
    public String getComboBoxJungle5() { return nameOf(jungle[4]); }

    public String getComboBoxMid1() { return nameOf(mid[0]); }
    public String getComboBoxMid2() { return nameOf(mid[1]); }
    public String getComboBoxMid3() { return nameOf(mid[2]); }
    public String getComboBoxMid4() { return nameOf(mid[3]); }
    public String getComboBoxMid5() { return nameOf(mid[4]); }

    public String getComboBoxBot1() { return nameOf(bot[0]); }
    public String getComboBoxBot2() { return nameOf(bot[1]); }
    public String getComboBoxBot3() { return nameOf(bot[2]); }
    public String getComboBoxBot4() { return nameOf(bot[3]); }
    public String getComboBoxBot5() { return nameOf(bot[4]); }

    public String getComboBoxSupport1() { return nameOf(support[0]); }
    public String getComboBoxSupport2() { return nameOf(support[1]); }
    public String getComboBoxSupport3() { return nameOf(support[2]); }
    public String getComboBoxSupport4() { return nameOf(support[3]); }
    public String getComboBoxSupport5() { return nameOf(support[4]); }

    public String getComboBoxAutoBan1() { return nameOf(autoBan[0]); }
    public String getComboBoxAutoBan2() { return nameOf(autoBan[1]); }
    public String getComboBoxAutoBan3() { return nameOf(autoBan[2]); }
    public String getComboBoxAutoBan4() { return nameOf(autoBan[3]); }
    public String getComboBoxAutoBan5() { return nameOf(autoBan[4]); }

    public String getComboBoxAutoLockArenaPriority1() { return nameOf(arenaLock[0]); }
    public String getComboBoxAutoLockArenaPriority2() { return nameOf(arenaLock[1]); }
    public String getComboBoxAutoLockArenaPriority3() { return nameOf(arenaLock[2]); }
    public String getComboBoxAutoLockArenaPriority4() { return nameOf(arenaLock[3]); }
    public String getComboBoxAutoLockArenaPriority5() { return nameOf(arenaLock[4]); }

    public String getComboBoxAutoBanArenaPriority1() { return nameOf(arenaBan[0]); }
    public String getComboBoxAutoBanArenaPriority2() { return nameOf(arenaBan[1]); }
    public String getComboBoxAutoBanArenaPriority3() { return nameOf(arenaBan[2]); }
    public String getComboBoxAutoBanArenaPriority4() { return nameOf(arenaBan[3]); }
    public String getComboBoxAutoBanArenaPriority5() { return nameOf(arenaBan[4]); }

    public String getComboBoxAutoSwapPriority1() { return nameOf(swap[0]); }
    public String getComboBoxAutoSwapPriority2() { return nameOf(swap[1]); }
    public String getComboBoxAutoSwapPriority3() { return nameOf(swap[2]); }
    public String getComboBoxAutoSwapPriority4() { return nameOf(swap[3]); }
    public String getComboBoxAutoSwapPriority5() { return nameOf(swap[4]); }
    public String getComboBoxAutoSwapPriority6() { return nameOf(swap[5]); }
    public String getComboBoxAutoSwapPriority7() { return nameOf(swap[6]); }
    public String getComboBoxAutoSwapPriority8() { return nameOf(swap[7]); }
    public String getComboBoxAutoSwapPriority9() { return nameOf(swap[8]); }
    public String getComboBoxAutoSwapPriority10() { return nameOf(swap[9]); }

    public JLabel[] getAutoSwapPriorityLabels() {
        return swapLabels;
    }

    public ChampionPicker[] getAutoSwapPriorityComboBoxes() {
        return swap;
    }

    // ---------------------------------------------------------------- lane visibility

    public void showTop() { selectLane(0); }
    public void hideTop() { }
    public void showJungle() { selectLane(1); }
    public void hideJungle() { }
    public void showMid() { selectLane(2); }
    public void hideMid() { }
    public void showBot() { selectLane(3); }
    public void hideBot() { }
    public void showSupport() { selectLane(4); }
    public void hideSupport() { }

    // ---------------------------------------------------------------- bench

    public JButton[] getButtonBench() {
        return bench;
    }

    // ---------------------------------------------------------------- listeners

    public void addBench1ActionListener(ActionListener l) { bench[0].addActionListener(l); }
    public void addBench2ActionListener(ActionListener l) { bench[1].addActionListener(l); }
    public void addBench3ActionListener(ActionListener l) { bench[2].addActionListener(l); }
    public void addBench4ActionListener(ActionListener l) { bench[3].addActionListener(l); }
    public void addBench5ActionListener(ActionListener l) { bench[4].addActionListener(l); }
    public void addBench6ActionListener(ActionListener l) { bench[5].addActionListener(l); }
    public void addBench7ActionListener(ActionListener l) { bench[6].addActionListener(l); }
    public void addBench8ActionListener(ActionListener l) { bench[7].addActionListener(l); }
    public void addBench9ActionListener(ActionListener l) { bench[8].addActionListener(l); }
    public void addBench10ActionListener(ActionListener l) { bench[9].addActionListener(l); }

    public void addAutoAcceptEnableListener(ActionListener l) { buttonAutoAcceptEnable.addActionListener(l); }
    public void addAutoAcceptDisableListener(ActionListener l) { buttonAutoAcceptDisable.addActionListener(l); }
    public void addAutoDeclineEnableListener(ActionListener l) { buttonAutoDeclineEnable.addActionListener(l); }
    public void addAutoDeclineDisableListener(ActionListener l) { buttonAutoDeclineDisable.addActionListener(l); }
    public void addChangeResponseAcceptListener(ActionListener l) { buttonChangeResponseAccept.addActionListener(l); }
    public void addChangeResponseDeclineListener(ActionListener l) { buttonChangeResponseDecline.addActionListener(l); }
    public void addAutoSwapEnableListener(ActionListener l) { buttonAutoSwapEnable.addActionListener(l); }
    public void addAutoSwapDisableListener(ActionListener l) { buttonAutoSwapDisable.addActionListener(l); }
    public void addAutoSwapAddListener(ActionListener l) { buttonAutoSwapAdd.addActionListener(l); }
    public void addAutoSwapSubtractListener(ActionListener l) { buttonAutoSwapSubtract.addActionListener(l); }
    public void addAutoSwapSaveListener(ActionListener l) { buttonAutoSwapSave.addActionListener(l); }
    public void addAlwaysOnTopEnableListener(ActionListener l) { buttonAlwaysOnTopEnable.addActionListener(l); }
    public void addAlwaysOnTopDisableListener(ActionListener l) { buttonAlwaysOnTopDisable.addActionListener(l); }
    public void addCenterGUIEnableListener(ActionListener l) { buttonCenterGUIEnable.addActionListener(l); }
    public void addCenterGUIDisableListener(ActionListener l) { buttonCenterGUIDisable.addActionListener(l); }
    public void addAutoDisenchantChampionsSafeListener(ActionListener l) { buttonDisenchantChampionsSafe.addActionListener(l); }
    public void addAutoDisenchantChampionsHardListener(ActionListener l) { buttonDisenchantChampionsHard.addActionListener(l); }
    public void addAutoDisenchantSkinsSafeListener(ActionListener l) { buttonDisenchantSkinsSafe.addActionListener(l); }
    public void addAutoDisenchantSkinsHardListener(ActionListener l) { buttonDisenchantSkinsHard.addActionListener(l); }
    public void addTopListener(ActionListener l) { buttonTop.addActionListener(l); }
    public void addJungleListener(ActionListener l) { buttonJungle.addActionListener(l); }
    public void addMidListener(ActionListener l) { buttonMid.addActionListener(l); }
    public void addBotListener(ActionListener l) { buttonBot.addActionListener(l); }
    public void addSupportListener(ActionListener l) { buttonSupport.addActionListener(l); }
    public void addAutoLockSaveListener(ActionListener l) { buttonAutoLockSave.addActionListener(l); }
    public void addAutoLockEnableListener(ActionListener l) { buttonAutoLockEnable.addActionListener(l); }
    public void addAutoLockDisableListener(ActionListener l) { buttonAutoLockDisable.addActionListener(l); }
    public void addAutoBanSaveListener(ActionListener l) { buttonAutoBanSave.addActionListener(l); }
    public void addAutoBanEnableListener(ActionListener l) { buttonAutoBanEnable.addActionListener(l); }
    public void addAutoBanDisableListener(ActionListener l) { buttonAutoBanDisable.addActionListener(l); }
    public void addAutoLockArenaEnableListener(ActionListener l) { buttonAutoLockArenaEnable.addActionListener(l); }
    public void addAutoLockArenaDisableListener(ActionListener l) { buttonAutoLockArenaDisable.addActionListener(l); }
    public void addAutoLockArenaSaveListener(ActionListener l) { buttonAutoLockArenaSave.addActionListener(l); }
    public void addAutoBanArenaEnableListener(ActionListener l) { buttonAutoBanArenaEnable.addActionListener(l); }
    public void addAutoBanArenaDisableListener(ActionListener l) { buttonAutoBanArenaDisable.addActionListener(l); }
    public void addAutoBanArenaSaveListener(ActionListener l) { buttonAutoBanArenaSave.addActionListener(l); }
    public void addAutoBraveryArenaEnableListener(ActionListener l) { buttonAutoBraveryArenaEnable.addActionListener(l); }
    public void addAutoBraveryArenaDisableListener(ActionListener l) { buttonAutoBraveryArenaDisable.addActionListener(l); }
    public void addSystemTrayEnableListener(ActionListener l) { buttonSystemTrayEnable.addActionListener(l); }
    public void addSystemTrayDisableListener(ActionListener l) { buttonSystemTrayDisable.addActionListener(l); }
    public void addAutoCheckUpdateEnableListener(ActionListener l) { buttonAutoCheckUpdateEnable.addActionListener(l); }
    public void addAutoCheckUpdateDisableListener(ActionListener l) { buttonAutoCheckUpdateDisable.addActionListener(l); }
    public void addAutoHonorEnableListener(ActionListener l) { buttonAutoHonorEnable.addActionListener(l); }
    public void addAutoHonorDisableListener(ActionListener l) { buttonAutoHonorDisable.addActionListener(l); }
    public void addAutoSkipScreensEnableListener(ActionListener l) { buttonAutoSkipScreensEnable.addActionListener(l); }
    public void addAutoSkipScreensDisableListener(ActionListener l) { buttonAutoSkipScreensDisable.addActionListener(l); }
    public void addGroupAutoQueueEnableListener(ActionListener l) { buttonGroupAutoQueueEnable.addActionListener(l); }
    public void addGroupAutoQueueDisableListener(ActionListener l) { buttonGroupAutoQueueDisable.addActionListener(l); }
    public void addSoloAutoQueueEnableListener(ActionListener l) { buttonSoloAutoQueueEnable.addActionListener(l); }
    public void addSoloAutoQueueDisableListener(ActionListener l) { buttonSoloAutoQueueDisable.addActionListener(l); }
    public void addAutoMinimizeEnableListener(ActionListener l) { buttonAutoMinimizeEnable.addActionListener(l); }
    public void addAutoMinimizeDisableListener(ActionListener l) { buttonAutoMinimizeDisable.addActionListener(l); }
    public void addAutoBanCrowdFavoriteEnableListener(ActionListener l) { buttonAutoBanCrowdFavoriteEnable.addActionListener(l); }
    public void addAutoBanCrowdFavoriteDisableListener(ActionListener l) { buttonAutoBanCrowdFavoriteDisable.addActionListener(l); }
    public void addExportListener(ActionListener l) { buttonExport.addActionListener(l); }
    public void addImportListener(ActionListener l) { buttonImport.addActionListener(l); }
    public void addResetListener(ActionListener l) { buttonReset.addActionListener(l); }
    public void addTestListener(ActionListener l) { buttonTest.addActionListener(l); }

    /** Custom rail navigation button. */
    private class NavButton extends JToggleButton {
        private final Icons.G glyph;

        NavButton(String text, Icons.G glyph) {
            super(text);
            this.glyph = glyph;
            setFont(fBody);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 11));
            setIconTextGap(11);
            setIcon(Icons.of(glyph, 17, Theme.TEXT_DIM));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean sel = isSelected();
            boolean hover = getModel().isRollover();
            if (sel) {
                g2.setColor(Theme.ACCENT_SOFT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(Theme.ACCENT);
                g2.fillRoundRect(0, 5, 3, getHeight() - 10, 3, 3);
            } else if (hover) {
                g2.setColor(Theme.SURFACE_1);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            }
            g2.dispose();

            Color fg = sel ? Theme.ACCENT_TEXT : (hover ? Theme.TEXT : Theme.TEXT_DIM);
            setForeground(fg);
            setIcon(Icons.of(glyph, 17, fg));
            super.paintComponent(g);
        }
    }
}

package view;

import com.formdev.flatlaf.util.UIScale;
import model.DDragonParser;
import model.LCUAuth;
import model.PreferenceManager;
import model.UpdateChecker;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    // Kept for sizing: the window width is fit to the ARAM bench (the widest must-not-wrap element).
    private JPanel railPanel;
    private JPanel statusStrip;
    private JPanel aramSection;
    // Players (scout) tab: scoutBody is rebuilt each refresh from a ScoutReport; the empty label shows
    // when there is no active game.
    private JPanel scoutBody;
    private final JLabel scoutEmptyLabel = new JLabel();
    private Dimension lastProgrammaticSize; // to tell our own setSize apart from a user drag

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
    public final JButton buttonAutoSwapSurveyEnable = new JButton();
    public final JButton buttonAutoSwapSurveyDisable = new JButton();
    public final JButton buttonAutoPickCardEnable = new JButton();
    public final JButton buttonAutoPickCardDisable = new JButton();
    public final JButton buttonAutoTradeEnable = new JButton();
    public final JButton buttonAutoTradeDisable = new JButton();
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
    public final JButton buttonAutoClaimPassesEnable = new JButton();
    public final JButton buttonAutoClaimPassesDisable = new JButton();
    public final JButton buttonAutoMinimizeEnable = new JButton();
    public final JButton buttonAutoMinimizeDisable = new JButton();
    public final JButton buttonLobbyOverlayEnable = new JButton();
    public final JButton buttonLobbyOverlayDisable = new JButton();
    public final JButton buttonAramBenchOverlayEnable = new JButton();
    public final JButton buttonAramBenchOverlayDisable = new JButton();
    public final JButton buttonAramSwapToolsOverlayEnable = new JButton();
    public final JButton buttonAramSwapToolsOverlayDisable = new JButton();
    public final JButton buttonAramTop5OverlayEnable = new JButton();
    public final JButton buttonAramTop5OverlayDisable = new JButton();
    public final JButton buttonPlayersOverlayEnable = new JButton();
    public final JButton buttonPlayersOverlayDisable = new JButton();
    private final RankedChoicesView aramTop5View = new RankedChoicesView();
    private final JSpinner overlayOpacitySpinner = new JSpinner(new SpinnerNumberModel(50, 10, 100, 5));
    private final JSpinner overlayHoverOpacitySpinner = new JSpinner(new SpinnerNumberModel(80, 10, 100, 5));
    private int[] overlayDragKeys = {0x12, 0x14}; // Windows VK: Alt + Caps Lock
    private final JButton overlayKeybindButton = new JButton();
    private Runnable overlayKeybindChanged = () -> { };
    public final JButton buttonNotifyEnable = new JButton();
    public final JButton buttonNotifyDisable = new JButton();
    public final JButton buttonNotifyMatchFoundEnable = new JButton();
    public final JButton buttonNotifyMatchFoundDisable = new JButton();
    public final JButton buttonNotifyChampPickedEnable = new JButton();
    public final JButton buttonNotifyChampPickedDisable = new JButton();
    public final JButton buttonNotifyChampPickedAramEnable = new JButton();
    public final JButton buttonNotifyChampPickedAramDisable = new JButton();
    public final JButton buttonNotifyChampSwapAramEnable = new JButton();
    public final JButton buttonNotifyChampSwapAramDisable = new JButton();
    public final JButton buttonNotifyChampBannedEnable = new JButton();
    public final JButton buttonNotifyChampBannedDisable = new JButton();
    public final JButton buttonNotifyOnlyWhenAwayEnable = new JButton();
    public final JButton buttonNotifyOnlyWhenAwayDisable = new JButton();
    public final JButton buttonNotifyHonorEnable = new JButton();
    public final JButton buttonNotifyHonorDisable = new JButton();
    public final JButton buttonNotifyReturnedToLobbyEnable = new JButton();
    public final JButton buttonNotifyReturnedToLobbyDisable = new JButton();
    public final JButton buttonNotifyAutoQueueEnable = new JButton();
    public final JButton buttonNotifyAutoQueueDisable = new JButton();
    public final JButton buttonNotifyGameStartingEnable = new JButton();
    public final JButton buttonNotifyGameStartingDisable = new JButton();
    public final JButton buttonNotifyPlayedRecentlyEnable = new JButton();
    public final JButton buttonNotifyPlayedRecentlyDisable = new JButton();
    // Players (scout) tab controls.
    public final JButton buttonScoutRefresh = new JButton();
    public final JButton buttonScoutEnable = new JButton();
    public final JButton buttonScoutDisable = new JButton();

    // ntfy topic name (persisted). Live-persisted via a DocumentListener, no Save button.
    private final JTextField notifyTopicField = new JTextField();
    // Idle threshold (seconds) for the "Only notify when away" gate.
    private final JSpinner notifyIdleSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 3600, 5));
    // UI scale (percent). Persisted; applied at startup, so a change needs a restart.
    private final JSpinner uiScaleSpinner = new JSpinner(new SpinnerNumberModel(75, 50, 200, 5));

    // ---- Action buttons ----
    private final JButton buttonChangeResponseAccept = new JButton();
    private final JButton buttonChangeResponseDecline = new JButton();
    private final JButton buttonAutoSwapSave = new JButton();
    private final JButton buttonAutoSwapAdd = new JButton();
    private final JButton buttonAutoSwapSubtract = new JButton();
    // ARAM survey (onboarding + Auto Swap Survey card).
    private final JButton buttonSurveyStart = new JButton();
    private final JButton buttonSurveyRefine = new JButton();
    private final JButton buttonSurveyRedo = new JButton();
    private final JButton buttonSurveyRevert = new JButton();
    private final JButton buttonSurveyUndo = new JButton();
    private final JButton buttonAutoLockSave = new JButton();
    private final JButton buttonAutoBanSave = new JButton();
    private final JButton buttonAutoLockArenaSave = new JButton();
    private final JButton buttonAutoBanArenaSave = new JButton();
    private final JButton buttonUiScaleApply = new JButton();
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
    // ARAM: the champion the user currently has (icon + name), updated live from the session.
    private final JLabel aramCurrentLabel = new JLabel("None");
    // Troll Swap (cosmetic bench cycle) button + its configurable delay.
    public final JButton buttonTrollSwap = new JButton();
    private final JSpinner trollSwapDelaySpinner = new JSpinner(new SpinnerNumberModel(100, 0, 5000, 50));
    private final JSpinner trollSwapLoopsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 20, 1));
    // Troll Swap toggle (infinite loop; delay applies, loops do not; auto-stops ~1s before lock).
    public final JButton buttonTrollSwapToggleEnable = new JButton();
    public final JButton buttonTrollSwapToggleDisable = new JButton();
    private final JLabel trollToggleHint = new JLabel("Troll Toggle ON: Auto Swap paused; returns to your champ ~2s before lock.");
    // Notify setup tutorial button.
    public final JButton buttonNotifyTutorial = new JButton();
    // Hide/Show the League client UX (kill-ux / launch-ux).
    public final JButton buttonHideClient = new JButton();
    public final JButton buttonShowClient = new JButton();

    // ARAM survey onboarding banner + Auto Swap Survey card widgets.
    private JPanel surveyBanner;
    private JLabel surveyBannerText;
    private final JLabel surveyMetric = new JLabel();
    private JPanel surveyListPanel;
    private ChampionPicker[] surveyPickers;
    private JPanel[] surveyRows;
    private Runnable surveyListChangeListener;

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
        content.setBorder(BorderFactory.createEmptyBorder(px(14), px(16), px(16), px(16)));
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
        buttonAutoSwapSurveyDisable.setEnabled(false);
        buttonAutoPickCardDisable.setEnabled(false);
        buttonAutoTradeDisable.setEnabled(false);
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
        DDragonParser.prefetchIcons(px(22), px(24), px(32));

        // Auto-save: any champion change fires the existing (now silent) save handler, so there are
        // no Save buttons. Programmatic setSelectedName during preference load does not fire this.
        autoSave(buttonAutoLockSave, top, jungle, mid, bot, support);
        autoSave(buttonAutoBanSave, autoBan);
        autoSave(buttonAutoLockArenaSave, arenaLock);
        autoSave(buttonAutoBanArenaSave, arenaBan);
        autoSave(buttonAutoSwapSave, swap);

        // Persist the user's manual resize; always reused (and never auto-recomputed) afterwards.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!isShowing()) {
                    return;
                }
                Dimension cur = getSize();
                if (cur.equals(lastProgrammaticSize)) {
                    return; // our own setSize, not a user drag
                }
                lastProgrammaticSize = cur;
                PreferenceManager.setWindowSize(cur.width, cur.height);
            }
        });

        selectLane(0);
        setMinimumSize(new Dimension(px(420), px(300)));
        pack();
        setLocationRelativeTo(null);
        setVisible(false);
    }

    private void initFonts() {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        }
        fBody = base.deriveFont(Font.PLAIN, fpt(13f));
        fBodyBold = base.deriveFont(Font.BOLD, fpt(13f));
        fTitle = base.deriveFont(Font.BOLD, fpt(15f));
        // Integer point sizes only: fractional sizes make Swing's label renderer open gaps mid-word.
        fSub = base.deriveFont(Font.PLAIN, fpt(12f));
        fEyebrow = base.deriveFont(Font.BOLD, fpt(11f));
    }

    // UI scale helpers: fpt = scaled integer font point size (integer keeps Swing from opening gaps
    // mid-word); px = scaled integer pixel size. Factor comes from FlatLaf's uiScale (set in main).
    private static float fpt(float pt) {
        return Math.round(UIScale.scale(pt));
    }

    private static int px(int v) {
        return UIScale.scale(v);
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

        rail.add(navButton("Lobby", Icons.G.LOBBY, "lobby", true), "growx");
        rail.add(navButton("Rift", Icons.G.RIFT, "rift", false), "growx");
        rail.add(navButton("ARAM", Icons.G.ARAM, "aram", false), "growx");
        rail.add(navButton("Arena", Icons.G.ARENA, "arena", false), "growx");
        rail.add(navButton("Loot", Icons.G.LOOT, "loot", false), "growx");
        rail.add(navButton("Players", Icons.G.PLAYERS, "players", false), "growx");
        rail.add(navButton("Notify", Icons.G.BELL, "notifications", false), "growx");
        rail.add(navButton("Overlay", Icons.G.OVERLAY, "overlay", false), "growx");
        rail.add(Box.createGlue(), "growy, push");
        rail.add(navButton("Settings", Icons.G.SETTINGS, "settings", false), "growx");
        rail.add(navButton("Info", Icons.G.INFO, "info", false), "growx");
        this.railPanel = rail;
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
        this.statusStrip = strip;

        JLabel connected = new JLabel("Connected");
        connected.setIcon(dotIcon(Theme.GREEN, px(9)));
        connected.setFont(fBodyBold);
        connected.setForeground(Theme.TEXT);
        connected.setIconTextGap(px(7));

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
        content.add(scrollWrap(buildLobby()), "lobby");
        content.add(scrollWrap(buildRift()), "rift");
        aramSection = buildAram();
        content.add(scrollWrap(aramSection), "aram");
        content.add(scrollWrap(buildArena()), "arena");
        content.add(scrollWrap(buildLoot()), "loot");
        content.add(scrollWrap(buildPlayers()), "players");
        content.add(scrollWrap(buildNotifications()), "notifications");
        content.add(scrollWrap(buildOverlay()), "overlay");
        content.add(scrollWrap(buildSettings()), "settings");
        content.add(scrollWrap(buildInfo()), "info");
    }

    /** Wrap a category panel in its own vertical scroll pane, so each section keeps an independent
     *  scroll position (scrolling one no longer moves the others). */
    private JScrollPane scrollWrap(JPanel view) {
        JScrollPane sp = new JScrollPane(view,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel section(String title, String sub) {
        JPanel panel = new ScrollablePanel(new MigLayout("insets 2, wrap 1, gap 8, fillx", "[grow,fill]"));
        panel.setOpaque(false);
        panel.add(header(title, sub), "growx");
        return panel;
    }

    private JPanel header(String title, String sub) {
        JPanel head = new JPanel(new MigLayout("insets 0 2 0 2, gap 10", "[]10[]push"));
        head.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(fTitle.deriveFont(fpt(17f)));
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
        l.setFont(fBodyBold.deriveFont(fpt(14f)));
        l.setForeground(Theme.TEXT);
        if (glyph != null) {
            l.setIcon(Icons.of(glyph, px(15), Theme.ACCENT));
            l.setIconTextGap(px(8));
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
            JLabel d = new JLabel("<html>" + desc + "</html>");
            d.setFont(fSub);
            d.setForeground(Theme.TEXT_FAINT);
            textCol.add(d, "growx");
        }
        row.add(textCol, "growx");
        row.add(new ToggleSwitch(enable, disable));
        return row;
    }

    /** A row like {@link #toggleRow} but with an arbitrary control (e.g. a spinner) on the right. */
    private JPanel spinnerRow(String label, String desc, javax.swing.JComponent field) {
        JPanel row = new JPanel(new MigLayout("insets 6 2 6 2, gap 12, fillx", "[grow,fill][]"));
        row.setOpaque(false);
        JPanel textCol = new JPanel(new MigLayout("insets 0, wrap 1, gap 1", "[grow,fill]"));
        textCol.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(fBody);
        l.setForeground(Theme.TEXT);
        textCol.add(l, "growx");
        if (desc != null) {
            JLabel d = new JLabel("<html>" + desc + "</html>");
            d.setFont(fSub);
            d.setForeground(Theme.TEXT_FAINT);
            textCol.add(d, "growx");
        }
        row.add(textCol, "growx");
        row.add(field);
        return row;
    }

    private JPanel divider() {
        JPanel line = new JPanel();
        line.setBackground(Theme.LINE_SOFT);
        line.setPreferredSize(new Dimension(px(10), 1));
        line.setMinimumSize(new Dimension(px(10), 1));
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

        JLabel note = new JLabel("<html>Auto Accept and Auto Decline are mutually exclusive.</html>");
        note.setFont(fSub);
        note.setForeground(Theme.TEXT_FAINT);
        panel.add(note, "growx, gapx 4");

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
        loop.add(divider(), "growx, gapy 2 2");
        loop.add(toggleRow("Auto Claim Passes", "Automatically claim all event and battlepass rewards, so unclaimed-reward notifications stay cleared. Covers new passes automatically.",
                buttonAutoClaimPassesEnable, buttonAutoClaimPassesDisable), "growx");
        panel.add(loop, "growx, gaptop 6");

        JLabel loopNote = new JLabel("<html>Group and Solo Auto Queue are mutually exclusive.</html>");
        loopNote.setFont(fSub);
        loopNote.setForeground(Theme.TEXT_FAINT);
        panel.add(loopNote, "growx, gapx 4");
        return panel;
    }

    // ---- Overlay ----

    private JPanel buildOverlay() {
        JPanel panel = section("Overlay", "controls drawn on the League client");

        Card global = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        global.add(cardTitle("Global", Icons.G.PLAYERS), "gapbottom 6");
        global.add(toggleRow("Players Overlay",
                "Scout panel (rank, form, recent games) on the client in every mode, bottom-right.",
                buttonPlayersOverlayEnable, buttonPlayersOverlayDisable), "growx");
        panel.add(global, "growx");

        Card lobby = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        lobby.add(cardTitle("Lobby", Icons.G.LOBBY), "gapbottom 6");
        lobby.add(toggleRow("Lobby Overlay",
                "Show the lobby controls (bottom-left) while you are in the lobby.",
                buttonLobbyOverlayEnable, buttonLobbyOverlayDisable), "growx");
        panel.add(lobby, "growx");

        Card aram = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        aram.add(cardTitle("ARAM", Icons.G.ARAM), "gapbottom 6");
        aram.add(toggleRow("Bench", "Quick Switch Bench on the client (top-center) during ARAM champ select.",
                buttonAramBenchOverlayEnable, buttonAramBenchOverlayDisable), "growx");
        aram.add(divider(), "growx, gapy 2 2");
        aram.add(toggleRow("Swap Tools", "Auto Swap Priority / Survey / Troll toggles and the Troll Swap button (bottom-right).",
                buttonAramSwapToolsOverlayEnable, buttonAramSwapToolsOverlayDisable), "growx");
        aram.add(divider(), "growx, gapy 2 2");
        aram.add(toggleRow("Top 5 Ranked", "Live top-5 auto-swap ranking with reasons (top-right).",
                buttonAramTop5OverlayEnable, buttonAramTop5OverlayDisable), "growx");
        panel.add(aram, "growx, gaptop 6");

        Card appearance = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        appearance.add(cardTitle("Appearance", Icons.G.OVERLAY), "gapbottom 6");
        appearance.add(spinnerRow("Opacity (%)", "How see-through the overlay is when you are not pointing at it.",
                overlayOpacitySpinner), "growx");
        appearance.add(divider(), "growx, gapy 2 2");
        appearance.add(spinnerRow("Opacity when hovered (%)", "Opacity while the mouse is over the overlay.",
                overlayHoverOpacitySpinner), "growx");
        panel.add(appearance, "growx, gaptop 6");

        Card drag = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        drag.add(cardTitle("Move overlays", Icons.G.LANE), "gapbottom 6");
        overlayKeybindButton.setText(keybindText(overlayDragKeys));
        overlayKeybindButton.addActionListener(e -> beginOverlayKeybindCapture());
        drag.add(spinnerRow("Drag keybind", "Hold this to drag any overlay anywhere. Click to change. Positions are saved.",
                overlayKeybindButton), "growx");
        panel.add(drag, "growx, gaptop 6");
        return panel;
    }

    // Human-readable keybind, e.g. "Alt + Caps Lock", from Windows VK codes (which match Java VK_* here).
    private String keybindText(int[] keys) {
        if (keys == null || keys.length == 0) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        for (int k : keys) {
            if (sb.length() > 0) {
                sb.append(" + ");
            }
            sb.append(java.awt.event.KeyEvent.getKeyText(k));
        }
        return sb.toString();
    }

    // Capture the next held chord (modifiers + one main key). Requires a non-modifier key to finalize,
    // so holding Alt then pressing Caps Lock yields Alt + Caps Lock rather than Alt alone.
    private void beginOverlayKeybindCapture() {
        overlayKeybindButton.setText("Press keys...");
        overlayKeybindButton.requestFocusInWindow();
        java.awt.event.KeyAdapter ka = new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int kc = e.getKeyCode();
                boolean isMod = kc == java.awt.event.KeyEvent.VK_CONTROL
                        || kc == java.awt.event.KeyEvent.VK_SHIFT
                        || kc == java.awt.event.KeyEvent.VK_ALT
                        || kc == java.awt.event.KeyEvent.VK_ALT_GRAPH
                        || kc == java.awt.event.KeyEvent.VK_META
                        || kc == java.awt.event.KeyEvent.VK_UNDEFINED;
                if (isMod) {
                    return; // wait for the main key
                }
                java.util.LinkedHashSet<Integer> keys = new java.util.LinkedHashSet<>();
                int mods = e.getModifiersEx();
                if ((mods & java.awt.event.KeyEvent.CTRL_DOWN_MASK) != 0) keys.add(0x11);
                if ((mods & java.awt.event.KeyEvent.SHIFT_DOWN_MASK) != 0) keys.add(0x10);
                if ((mods & java.awt.event.KeyEvent.ALT_DOWN_MASK) != 0) keys.add(0x12);
                keys.add(kc);
                overlayDragKeys = keys.stream().mapToInt(Integer::intValue).toArray();
                overlayKeybindButton.setText(keybindText(overlayDragKeys));
                overlayKeybindButton.removeKeyListener(this);
                e.consume();
                overlayKeybindChanged.run();
            }
        };
        overlayKeybindButton.addKeyListener(ka);
    }

    // ---- Summoner's Rift ----

    private JPanel buildRift() {
        JPanel panel = section("Rift", "ranked and draft pick / ban priorities");

        Card lock = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        lock.add(cardHeaderWithToggle("Auto Lock", Icons.G.LOCK, buttonAutoLockEnable, buttonAutoLockDisable), "growx, gapbottom 8");
        lock.add(buildLaneSelector(), "growx, gapbottom 8");
        lanePanel.setOpaque(false);
        lanePanel.add(listWithSwap(priorityColumn(top), top, buttonAutoLockSave), "top");
        lanePanel.add(listWithSwap(priorityColumn(jungle), jungle, buttonAutoLockSave), "jungle");
        lanePanel.add(listWithSwap(priorityColumn(mid), mid, buttonAutoLockSave), "mid");
        lanePanel.add(listWithSwap(priorityColumn(bot), bot, buttonAutoLockSave), "bot");
        lanePanel.add(listWithSwap(priorityColumn(support), support, buttonAutoLockSave), "support");
        lock.add(lanePanel, "growx");
        panel.add(lock, "growx");

        Card ban = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        ban.add(cardHeaderWithToggle("Auto Ban", Icons.G.BAN, buttonAutoBanEnable, buttonAutoBanDisable), "growx, gapbottom 8");
        ban.add(listWithSwap(priorityColumn(autoBan), autoBan, buttonAutoBanSave), "growx");
        panel.add(ban, "growx");
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

        // Onboarding banner (very top, above Current Champion). Sells one-time setup + sharper swaps.
        // hidemode 3 so the "done" state collapses it entirely instead of leaving an empty gap.
        panel.add(buildSurveyBanner(), "growx, hidemode 3");

        // Current champion (what you have right now).
        Card current = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        current.add(cardTitle("Current Champion", Icons.G.LOCK), "growx, gapbottom 6");
        aramCurrentLabel.setFont(fBodyBold);
        aramCurrentLabel.setForeground(Theme.TEXT);
        aramCurrentLabel.setIconTextGap(px(8));
        current.add(aramCurrentLabel, "growx");
        panel.add(current, "growx");

        // Top 5 Ranked Choices: live view of the auto-swap decision (Priority-then-Survey), with a star
        // for Priority-sourced picks and a status showing what is on the bench / swapped in.
        Card top5 = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        top5.add(cardTitle("Top 5 Ranked Choices", Icons.G.STAR), "growx, gapbottom 6");
        top5.add(aramTop5View, "growx");
        panel.add(top5, "growx");

        // Cards & Trades (2026 ARAM): auto-pick the best offered champion card at champ-select start,
        // and auto-handle teammate trades. Both use your Priority + Survey ranking (same as auto-swap).
        Card cardsTrades = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        cardsTrades.add(cardTitle("Cards & Trades", Icons.G.REROLL), "growx, gapbottom 8");
        cardsTrades.add(toggleRow("Auto Pick Card",
                "At the start of ARAM champ select, pick the highest-ranked of your offered champion cards.",
                buttonAutoPickCardEnable, buttonAutoPickCardDisable), "growx");
        cardsTrades.add(divider(), "growx, gapy 2 2");
        cardsTrades.add(toggleRow("Auto Trade",
                "Accept incoming trades that upgrade you, decline the rest, and request a teammate's champion when it ranks higher than yours.",
                buttonAutoTradeEnable, buttonAutoTradeDisable), "growx");
        panel.add(cardsTrades, "growx");

        // Quick Switch Bench on top.
        Card bench = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        bench.add(cardTitle("Quick Switch Bench", Icons.G.SWAP), "growx, gapbottom 8");
        JPanel benchGrid = new JPanel(new MigLayout("insets 0, wrap 5, gap 6", "[grow,fill]", ""));
        benchGrid.setOpaque(false);
        panelQuickSwitchBench2 = new JPanel(new MigLayout("insets 0, wrap 5, gap 6", "[grow,fill]", ""));
        panelQuickSwitchBench2.setOpaque(false);
        for (int i = 0; i < this.bench.length; i++) {
            this.bench[i] = new ChampionButton();
            this.bench[i].setPreferredSize(new Dimension(px(50), px(50)));
            if (i < 5) {
                benchGrid.add(this.bench[i], "grow");
            } else {
                panelQuickSwitchBench2.add(this.bench[i], "grow");
            }
        }
        bench.add(benchGrid, "growx");
        bench.add(panelQuickSwitchBench2, "growx, gaptop 6");

        // Troll Swap: one-shot cosmetic cycle through the bench and back. Red + caution: ban risk.
        bench.add(divider(), "growx, gapy 8 6");
        JPanel trollRow = new JPanel(new MigLayout("insets 0, gap 6", "[]push[]4[]8[]4[]"));
        trollRow.setOpaque(false);
        styleButton(buttonTrollSwap, "Troll Swap", Icons.G.CAUTION, ButtonKind.DANGER);
        buttonTrollSwap.setForeground(Theme.RED);
        JLabel loopsLbl = new JLabel("Loops");
        loopsLbl.setFont(fSub);
        loopsLbl.setForeground(Theme.TEXT_DIM);
        JLabel delayLbl = new JLabel("Delay (ms)");
        delayLbl.setFont(fSub);
        delayLbl.setForeground(Theme.TEXT_DIM);
        trollSwapLoopsSpinner.setPreferredSize(new Dimension(px(56), px(28)));
        trollSwapDelaySpinner.setPreferredSize(new Dimension(px(70), px(28)));
        trollRow.add(buttonTrollSwap);
        trollRow.add(loopsLbl);
        trollRow.add(trollSwapLoopsSpinner);
        trollRow.add(delayLbl);
        trollRow.add(trollSwapDelaySpinner);
        bench.add(trollRow, "growx");
        // Infinite auto-troll toggle: cycles the bench until you stop it or ~1s before lock, then
        // returns to the champ you started on. Delay applies; loops (button-only) do not.
        JPanel trollToggleRow = new JPanel(new MigLayout("insets 0, gap 6", "[]8[]"));
        trollToggleRow.setOpaque(false);
        JLabel autoTrollLbl = new JLabel("Auto Troll (loop)");
        autoTrollLbl.setFont(fSub);
        autoTrollLbl.setForeground(Theme.TEXT_DIM);
        trollToggleRow.add(autoTrollLbl);
        trollToggleRow.add(new ToggleSwitch(buttonTrollSwapToggleEnable, buttonTrollSwapToggleDisable));
        bench.add(trollToggleRow, "growx, gaptop 4");
        trollToggleHint.setFont(fSub);
        trollToggleHint.setForeground(Theme.RED);
        trollToggleHint.setVisible(false);
        bench.add(trollToggleHint, "growx, gaptop 2");
        JLabel trollWarn = new JLabel("<html>Cosmetic prank: cycles to every bench champion and back. "
                + "Rapid swapping is visible to everyone and can get you banned if used blatantly. Use sparingly.</html>");
        trollWarn.setFont(fSub);
        trollWarn.setForeground(Theme.RED);
        bench.add(trollWarn, "growx, gaptop 4");

        panel.add(bench, "growx");

        // Auto Swap Priority (the manual 10-slot list). Keeps the existing toggle + save/add/subtract.
        Card swapCard = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        JButton swapPriorityBtn = new JButton();
        styleButton(swapPriorityBtn, "Swap", Icons.G.SWAP, ButtonKind.GHOST);
        JPanel pHead = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[]push[][]"));
        pHead.setOpaque(false);
        pHead.add(cardTitle("Auto Swap Priority", Icons.G.SWAP));
        pHead.add(swapPriorityBtn);
        pHead.add(new ToggleSwitch(buttonAutoSwapEnable, buttonAutoSwapDisable));
        swapCard.add(pHead, "growx, gapbottom 6");
        JLabel pFoot = new JLabel("<html>Always beats the survey. For champions you specifically want right now.</html>");
        pFoot.setFont(fSub);
        pFoot.setForeground(Theme.TEXT_FAINT);
        swapCard.add(pFoot, "growx, gapbottom 8");
        JPanel swapGrid = new JPanel(new MigLayout("insets 0, wrap 2, gapy 5", "[18!]8[grow,fill]"));
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
        swapCard.add(swapActions, "gaptop 8");
        panel.add(swapCard, "growx");
        // Swapping two priority slots persists via the existing (silent) priority save button.
        new SwapController(swapPriorityBtn, swap, buttonAutoSwapSave::doClick);

        // Auto Swap Survey (the survey-generated ranked list).
        panel.add(buildSurveyCard(), "growx");

        // Default the banner to the not-started copy; the controller overrides it on refresh.
        setSurveyOnboarding("none", 0, DDragonParser.championPoolSize());
        return panel;
    }

    /** Accent-bordered onboarding banner. Text + button label are set by {@link #setSurveyOnboarding}. */
    private JPanel buildSurveyBanner() {
        JPanel banner = new JPanel(new MigLayout("insets 10 12 10 12, gap 12, fillx", "[grow,fill][]", "[]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT_SOFT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(Theme.ACCENT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        surveyBannerText = new JLabel();
        surveyBannerText.setFont(fSub);
        surveyBannerText.setForeground(Theme.TEXT_DIM);
        styleButton(buttonSurveyStart, "Start Survey", null, ButtonKind.PRIMARY);
        banner.add(surveyBannerText, "growx");
        banner.add(buttonSurveyStart, "aligny center");
        this.surveyBanner = banner;
        return banner;
    }

    /** The Auto Swap Survey card: metric, Refine/Redo/Revert/Undo, a scrollable ranked picker list,
     *  and a Swap button (all over the survey pickers). */
    private JPanel buildSurveyCard() {
        Card card = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");

        // Header: title + metric on the left, Refine / Redo / toggle on the right.
        styleButton(buttonSurveyRefine, "Refine", null, ButtonKind.NORMAL);
        styleButton(buttonSurveyRedo, "Redo", null, ButtonKind.DANGER);
        surveyMetric.setFont(fSub);
        surveyMetric.setForeground(Theme.TEXT_DIM);
        JPanel head = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[][]push[][][]"));
        head.setOpaque(false);
        head.add(cardTitle("Auto Swap Survey", Icons.G.SWAP));
        head.add(surveyMetric);
        head.add(buttonSurveyRefine);
        head.add(buttonSurveyRedo);
        head.add(new ToggleSwitch(buttonAutoSwapSurveyEnable, buttonAutoSwapSurveyDisable));
        card.add(head, "growx, gapbottom 6");

        // Action row: Revert to Original, Undo, and Swap.
        JButton surveySwapBtn = new JButton();
        styleButton(buttonSurveyRevert, "Revert to Original", null, ButtonKind.NORMAL);
        styleButton(buttonSurveyUndo, "Undo", null, ButtonKind.NORMAL);
        styleButton(surveySwapBtn, "Swap", Icons.G.SWAP, ButtonKind.GHOST);
        JPanel actions = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[][]push[]"));
        actions.setOpaque(false);
        // hidemode 3 so the hidden Revert button collapses (Undo slides left) instead of leaving a gap.
        actions.add(buttonSurveyRevert, "hidemode 3");
        actions.add(buttonSurveyUndo);
        actions.add(surveySwapBtn);
        card.add(actions, "growx, gapbottom 6");

        // Scrollable list of numbered survey pickers, sized to the champion pool.
        int total = DDragonParser.championPoolSize();
        List<String> names = DDragonParser.fetchChampionNames();
        surveyPickers = pickers(total);
        surveyRows = new JPanel[total];
        surveyListPanel = new JPanel(new MigLayout("insets 4, wrap 1, gapy 4, fillx", "[grow,fill]"));
        surveyListPanel.setOpaque(false);
        for (int i = 0; i < total; i++) {
            surveyPickers[i].setItems(names);
            final int index = i;
            // A picker edit (via its dropdown) counts as a survey-list change.
            surveyPickers[i].setOnChange(() -> {
                if (surveyListChangeListener != null) {
                    surveyListChangeListener.run();
                }
            });
            JPanel row = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[24!][grow,fill]"));
            row.setOpaque(false);
            row.add(numberLabel(index + 1));
            row.add(surveyPickers[i], "growx");
            row.setVisible(false);
            surveyRows[i] = row;
            // hidemode 3: unused rows take no space, so the list is exactly as tall as the ranking.
            surveyListPanel.add(row, "growx, hidemode 3");
        }
        JScrollPane listScroll = new JScrollPane(surveyListPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        listScroll.getViewport().setOpaque(false);
        listScroll.setOpaque(false);
        listScroll.getVerticalScrollBar().setUnitIncrement(16);
        listScroll.setPreferredSize(new Dimension(px(260), px(230)));
        card.add(listScroll, "growx");

        JLabel foot = new JLabel("<html>Generated by the survey. Scroll the list, and reorder it with Swap. "
                + "Never-tier champions are excluded.</html>");
        foot.setFont(fSub);
        foot.setForeground(Theme.TEXT_FAINT);
        card.add(foot, "growx, gaptop 6");

        // Defaults: Undo disabled, Revert hidden until the survey differs from its original snapshot.
        buttonSurveyUndo.setEnabled(false);
        buttonSurveyRevert.setVisible(false);

        // Swapping two survey rows is a survey-list change (controller pushes undo + persists).
        new SwapController(surveySwapBtn, surveyPickers, () -> {
            if (surveyListChangeListener != null) {
                surveyListChangeListener.run();
            }
        });
        return card;
    }

    /** Show the champion the user currently has in ARAM (icon + name); null/blank clears it. */
    public void setAramCurrentChampion(String name) {
        if (name == null || name.isBlank()) {
            aramCurrentLabel.setText("None");
            aramCurrentLabel.setIcon(null);
            return;
        }
        aramCurrentLabel.setText(name);
        final String requested = name;
        ChampionIcons.load(name, px(32), icon -> {
            if (requested.equals(aramCurrentLabel.getText())) {
                aramCurrentLabel.setIcon(icon);
            }
        });
    }

    // ---- Troll Swap ----
    public void addTrollSwapListener(ActionListener l) { buttonTrollSwap.addActionListener(l); }
    public int getTrollSwapDelayMs() { return (Integer) trollSwapDelaySpinner.getValue(); }
    public void setTrollSwapDelayMs(int ms) { trollSwapDelaySpinner.setValue(Math.max(0, Math.min(ms, 5000))); }
    public void addTrollSwapDelayChangeListener(Runnable r) { trollSwapDelaySpinner.addChangeListener(e -> r.run()); }
    public int getTrollSwapLoops() { return (Integer) trollSwapLoopsSpinner.getValue(); }
    public void setTrollSwapLoops(int n) { trollSwapLoopsSpinner.setValue(Math.max(1, Math.min(n, 20))); }
    public void addTrollSwapLoopsChangeListener(Runnable r) { trollSwapLoopsSpinner.addChangeListener(e -> r.run()); }
    public void addTrollSwapToggleEnableListener(ActionListener l) { buttonTrollSwapToggleEnable.addActionListener(l); }
    public void addTrollSwapToggleDisableListener(ActionListener l) { buttonTrollSwapToggleDisable.addActionListener(l); }

    // ---- Overlay appearance + drag keybind ----
    public int getOverlayOpacity() { return (Integer) overlayOpacitySpinner.getValue(); }
    public void setOverlayOpacity(int v) { overlayOpacitySpinner.setValue(Math.max(10, Math.min(v, 100))); }
    public void addOverlayOpacityChangeListener(Runnable r) { overlayOpacitySpinner.addChangeListener(e -> r.run()); }
    public int getOverlayHoverOpacity() { return (Integer) overlayHoverOpacitySpinner.getValue(); }
    public void setOverlayHoverOpacity(int v) { overlayHoverOpacitySpinner.setValue(Math.max(10, Math.min(v, 100))); }
    public void addOverlayHoverOpacityChangeListener(Runnable r) { overlayHoverOpacitySpinner.addChangeListener(e -> r.run()); }
    public int[] getOverlayDragKeys() { return overlayDragKeys.clone(); }
    public void setOverlayDragKeys(int[] keys) {
        if (keys != null && keys.length > 0) {
            overlayDragKeys = keys.clone();
            overlayKeybindButton.setText(keybindText(overlayDragKeys));
        }
    }
    public void addOverlayKeybindChangeListener(Runnable r) { overlayKeybindChanged = r != null ? r : () -> { }; }
    public void setTrollToggleHintVisible(boolean v) { trollToggleHint.setVisible(v); }
    public void addNotifyTutorialListener(ActionListener l) { buttonNotifyTutorial.addActionListener(l); }
    public void addHideClientListener(ActionListener l) { buttonHideClient.addActionListener(l); }
    public void addShowClientListener(ActionListener l) { buttonShowClient.addActionListener(l); }

    // ---- ARAM survey API ----

    /** Update the onboarding banner. State is "none", "partial", or "done"; done hides the banner.
     *  Copy sells the one-time setup and the sharper auto-swap it unlocks. */
    public void setSurveyOnboarding(String state, int decided, int total) {
        if (surveyBanner == null) {
            return;
        }
        if ("done".equals(state)) {
            surveyBanner.setVisible(false);
        } else if ("partial".equals(state)) {
            surveyBannerText.setText("<html>You are <b>" + decided + "/" + total + "</b> through your survey. "
                    + "Continuing sharpens auto-swap. One-time setup.</html>");
            buttonSurveyStart.setText("Continue Survey");
            surveyBanner.setVisible(true);
        } else {
            surveyBannerText.setText("<html><b>Build your auto-swap ranking.</b> A quick survey learns which "
                    + "champions you like, so auto-swap always grabs your best available pick. "
                    + "One-time setup, and it sharpens every game.</html>");
            buttonSurveyStart.setText("Start Survey");
            surveyBanner.setVisible(true);
        }
        if (aramSection != null) {
            aramSection.revalidate();
            aramSection.repaint();
        }
    }

    /** Survey completion metric shown in the Auto Swap Survey header, e.g. "130 / 171 ranked". */
    public void setSurveyMetric(int decided, int total) {
        surveyMetric.setText(decided + " / " + total + " ranked");
    }

    /** Fill the first {@code names.length} survey pickers (revealing them) and hide the rest. */
    public void setSurveyList(String[] names) {
        if (surveyPickers == null) {
            return;
        }
        int n = names == null ? 0 : names.length;
        for (int i = 0; i < surveyPickers.length; i++) {
            if (i < n) {
                surveyPickers[i].setSelectedName(names[i]);
                surveyRows[i].setVisible(true);
            } else {
                surveyPickers[i].clearSelection();
                surveyRows[i].setVisible(false);
            }
        }
        if (surveyListPanel != null) {
            surveyListPanel.revalidate();
            surveyListPanel.repaint();
        }
    }

    /** The current, non-empty survey picks in order. */
    public String[] getSurveyList() {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (surveyPickers != null) {
            for (ChampionPicker p : surveyPickers) {
                String s = p.getSelectedName();
                if (s != null && !s.isBlank()) {
                    out.add(s);
                }
            }
        }
        return out.toArray(new String[0]);
    }

    public void setSurveyUndoEnabled(boolean enabled) {
        buttonSurveyUndo.setEnabled(enabled);
    }

    public void setSurveyRevertVisible(boolean visible) {
        buttonSurveyRevert.setVisible(visible);
        if (buttonSurveyRevert.getParent() != null) {
            buttonSurveyRevert.getParent().revalidate();
            buttonSurveyRevert.getParent().repaint();
        }
    }

    public void addSurveyStartListener(ActionListener l) { buttonSurveyStart.addActionListener(l); }
    public void addSurveyRefineListener(ActionListener l) { buttonSurveyRefine.addActionListener(l); }
    public void addSurveyRedoListener(ActionListener l) { buttonSurveyRedo.addActionListener(l); }
    public void addSurveyRevertListener(ActionListener l) { buttonSurveyRevert.addActionListener(l); }
    public void addSurveyUndoListener(ActionListener l) { buttonSurveyUndo.addActionListener(l); }
    public void addAutoSwapSurveyEnableListener(ActionListener l) { buttonAutoSwapSurveyEnable.addActionListener(l); }
    public void addAutoSwapSurveyDisableListener(ActionListener l) { buttonAutoSwapSurveyDisable.addActionListener(l); }
    public void addAutoPickCardEnableListener(ActionListener l) { buttonAutoPickCardEnable.addActionListener(l); }
    public void addAutoPickCardDisableListener(ActionListener l) { buttonAutoPickCardDisable.addActionListener(l); }
    public void addAutoTradeEnableListener(ActionListener l) { buttonAutoTradeEnable.addActionListener(l); }
    public void addAutoTradeDisableListener(ActionListener l) { buttonAutoTradeDisable.addActionListener(l); }

    /** Fired whenever the survey list changes: a survey picker edit or a survey swap. */
    public void addSurveyListChangeListener(Runnable r) { this.surveyListChangeListener = r; }

    // ---- Arena ----

    private JPanel buildArena() {
        JPanel panel = section("Arena", "2v2v2v2 lock, ban and bravery");

        Card lock = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        lock.add(cardHeaderWithToggle("Auto Lock", Icons.G.LOCK, buttonAutoLockArenaEnable, buttonAutoLockArenaDisable), "growx, gapbottom 8");
        lock.add(listWithSwap(priorityColumn(arenaLock), arenaLock, buttonAutoLockArenaSave), "growx");
        JLabel lockNote = new JLabel("<html>Disabled while Auto Bravery is on.</html>");
        lockNote.setFont(fSub);
        lockNote.setForeground(Theme.TEXT_FAINT);
        lock.add(lockNote, "growx, gaptop 6");
        panel.add(lock, "growx");

        Card ban = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        ban.add(cardHeaderWithToggle("Auto Ban", Icons.G.BAN, buttonAutoBanArenaEnable, buttonAutoBanArenaDisable), "growx, gapbottom 8");
        ban.add(listWithSwap(priorityColumn(arenaBan), arenaBan, buttonAutoBanArenaSave), "growx");
        JLabel banNote = new JLabel("<html>Disabled while Auto Ban Crowd Favorite is on.</html>");
        banNote.setFont(fSub);
        banNote.setForeground(Theme.TEXT_FAINT);
        ban.add(banNote, "growx, gaptop 6");
        panel.add(ban, "growx");

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
        JLabel note = new JLabel("<html>Both modes ask for confirmation and a typed code. Hard mode cannot be undone.</html>");
        note.setFont(fSub);
        note.setForeground(Theme.AMBER);
        champs.add(note, "growx, gaptop 10");
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

    // ---- Notifications ----

    // ---- Players (scout) tab ----
    private JPanel buildPlayers() {
        JPanel panel = section("Players", "scout the lobby");

        Card head = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        JPanel headRow = new JPanel(new MigLayout("insets 0, gap 8, fillx", "[]push[][]"));
        headRow.setOpaque(false);
        headRow.add(cardTitle("Scout", Icons.G.PLAYERS));
        styleButton(buttonScoutRefresh, "Refresh", Icons.G.UPDATE, ButtonKind.GHOST);
        headRow.add(buttonScoutRefresh);
        headRow.add(new ToggleSwitch(buttonScoutEnable, buttonScoutDisable));
        head.add(headRow, "growx, gapbottom 2");
        JLabel hint = new JLabel("<html>Rank, recent form, and mastery for everyone in your game. Auto-refreshes "
                + "on each phase; toggle off to stop background lookups. Anyone from your last 3 games is flagged.</html>");
        hint.setFont(fSub);
        hint.setForeground(Theme.TEXT_FAINT);
        head.add(hint, "growx");
        panel.add(head, "growx");

        scoutEmptyLabel.setText("<html>No active game. Player intel appears in champ select and in-game.</html>");
        scoutEmptyLabel.setFont(fSub);
        scoutEmptyLabel.setForeground(Theme.TEXT_FAINT);
        panel.add(scoutEmptyLabel, "growx, gapx 4, gaptop 4, hidemode 3");

        scoutBody = new JPanel(new MigLayout("insets 0, wrap 1, gapy 6, fillx", "[grow,fill]"));
        scoutBody.setOpaque(false);
        scoutBody.setVisible(false);
        panel.add(scoutBody, "growx, hidemode 3");
        return panel;
    }

    /** Rebuild the Players tab from a scout report. Safe to call off the EDT? No - call on the EDT
     *  (ScoutReport.refreshAsync already delivers on the EDT). */
    public void setScoutReport(model.ScoutReport r) {
        if (scoutBody == null) {
            return;
        }
        scoutBody.removeAll();
        boolean empty = (r == null || r.isEmpty());
        scoutEmptyLabel.setVisible(empty);
        scoutBody.setVisible(!empty);
        if (!empty) {
            java.util.List<model.ScoutPlayer> recent = r.playedRecentlyPlayers();
            if (recent != null && !recent.isEmpty()) {
                scoutBody.add(scoutAlert(recent), "growx");
            }
            if (r.allies != null && !r.allies.isEmpty()) {
                scoutBody.add(scoutTeamCard("Your Team", r.allies), "growx");
            }
            if (r.enemies != null && !r.enemies.isEmpty()) {
                scoutBody.add(scoutTeamCard("Enemy Team", r.enemies), "growx");
            }
        }
        scoutBody.revalidate();
        scoutBody.repaint();
    }

    private JPanel scoutAlert(java.util.List<model.ScoutPlayer> recent) {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < recent.size(); i++) {
            if (i > 0) {
                names.append(", ");
            }
            model.ScoutPlayer p = recent.get(i);
            names.append(p.summonerName == null || p.summonerName.isBlank() ? "(hidden)" : p.summonerName);
        }
        JLabel l = new JLabel("<html><b>Played recently:</b> " + names + "</html>");
        l.setFont(fSub);
        l.setForeground(Theme.ACCENT);
        JPanel wrap = new JPanel(new MigLayout("insets 6 10 6 10, fillx", "[grow,fill]"));
        wrap.setOpaque(false);
        wrap.add(l, "growx");
        return wrap;
    }

    private JPanel scoutTeamCard(String title, java.util.List<model.ScoutPlayer> players) {
        Card card = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        card.add(cardTitle(title, Icons.G.PLAYERS), "growx, gapbottom 4");
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) {
                card.add(divider(), "growx, gapy 2 2");
            }
            card.add(scoutRow(players.get(i)), "growx");
        }
        return card;
    }

    private JPanel scoutRow(model.ScoutPlayer p) {
        JPanel row = new JPanel(new MigLayout("insets 6 2 6 2, gap 8, fillx", "[grow,fill][]"));
        row.setOpaque(false);
        JPanel col = new JPanel(new MigLayout("insets 0, wrap 1, gap 1", "[grow,fill]"));
        col.setOpaque(false);
        String name = (p.summonerName == null || p.summonerName.isBlank()) ? "(hidden)" : p.summonerName;
        String champ = (p.championName() != null && !p.championName().isBlank()) ? "  •  " + p.championName() : "";
        JLabel nameLbl = new JLabel(name + champ);
        nameLbl.setFont(fBodyBold);
        nameLbl.setForeground(Theme.TEXT);
        col.add(nameLbl, "growx");
        StringBuilder sub = new StringBuilder("<html>");
        sub.append(p.hasAnyRank() ? scoutRankText(p) : "Unranked");
        sub.append(" &nbsp;•&nbsp; ").append(p.recentWins).append("W ").append(p.recentLosses).append("L");
        if (p.topMasteryChampId != null && p.topMasteryChampName() != null && !p.topMasteryChampName().isBlank()) {
            sub.append(" &nbsp;•&nbsp; ").append(p.topMasteryChampName())
               .append(' ').append(scoutPoints(p.topMasteryPoints));
        }
        sub.append("</html>");
        JLabel subLbl = new JLabel(sub.toString());
        subLbl.setFont(fSub);
        subLbl.setForeground(Theme.TEXT_DIM);
        col.add(subLbl, "growx");
        row.add(col, "growx");
        if (p.playedRecently) {
            JLabel badge = new JLabel(p.playedRecentlyCount + "x recent");
            badge.setFont(fSub);
            badge.setForeground(Theme.ACCENT);
            row.add(badge, "aligny top");
        }
        return row;
    }

    private String scoutRankText(model.ScoutPlayer p) {
        if (p.soloRank != null && !p.soloRank.isBlank()) {
            return p.soloRank;
        }
        if (p.flexRank != null && !p.flexRank.isBlank()) {
            return p.flexRank + " (Flex)";
        }
        return "Unranked";
    }

    private String scoutPoints(long pts) {
        if (pts >= 1000) {
            return (pts / 1000) + "k";
        }
        return String.valueOf(pts);
    }

    private JPanel buildNotifications() {
        JPanel panel = section("Notify", "phone alerts via ntfy.sh");

        // Tutorial callout at the top - most users have never used ntfy.
        Card intro = new Card("insets 8 10 8 10, fillx", "[grow,fill][]", "");
        JLabel introTxt = new JLabel("<html><b>New to phone alerts?</b> This walks you through the ntfy setup "
                + "and sends a test to your phone.</html>");
        introTxt.setFont(fSub);
        introTxt.setForeground(Theme.TEXT_DIM);
        styleButton(buttonNotifyTutorial, "How to set up", Icons.G.HELP, ButtonKind.PRIMARY);
        intro.add(introTxt, "growx");
        intro.add(buttonNotifyTutorial);
        panel.add(intro, "growx");

        Card master = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        master.add(toggleRow("Enable Notifications", "Master switch for all phone notifications.",
                buttonNotifyEnable, buttonNotifyDisable), "growx");
        master.add(divider(), "growx, gapy 2 2");
        JLabel topicLabel = new JLabel("ntfy Topic");
        topicLabel.setFont(fBody);
        topicLabel.setForeground(Theme.TEXT);
        master.add(topicLabel, "gaptop 6");
        notifyTopicField.setFont(fBody);
        master.add(notifyTopicField, "growx, gaptop 2");
        JLabel topicHelp = new JLabel("<html>Install the ntfy app on your phone and subscribe to this exact topic. "
                + "Pick something unique and hard to guess (anyone with the name can read your alerts).</html>");
        topicHelp.setFont(fSub);
        topicHelp.setForeground(Theme.TEXT_FAINT);
        master.add(topicHelp, "growx, gaptop 4");
        master.add(divider(), "growx, gapy 6 2");
        master.add(toggleRow("Only Notify When Away", "Hold notifications while you are using the PC or watching fullscreen video.",
                buttonNotifyOnlyWhenAwayEnable, buttonNotifyOnlyWhenAwayDisable), "growx");
        notifyIdleSpinner.setPreferredSize(new Dimension(px(72), px(28)));
        master.add(spinnerRow("Idle Threshold (seconds)",
                "How long with no keyboard or mouse before you count as away.", notifyIdleSpinner), "growx");
        panel.add(master, "growx");

        Card events = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        events.add(cardTitle("Events", Icons.G.BELL), "gapbottom 6");
        events.add(toggleRow("Match Found", "When a ready-check is auto-accepted.",
                buttonNotifyMatchFoundEnable, buttonNotifyMatchFoundDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Champion Picked", "When a champion is auto-locked (normal and Arena games).",
                buttonNotifyChampPickedEnable, buttonNotifyChampPickedDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Champion Picked (ARAM)", "The random champion you are given in ARAM / Mayhem.",
                buttonNotifyChampPickedAramEnable, buttonNotifyChampPickedAramDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Champion Swap (ARAM)", "When an auto-swap takes a champion off the bench.",
                buttonNotifyChampSwapAramEnable, buttonNotifyChampSwapAramDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Champion Banned", "When a ban is auto-locked.",
                buttonNotifyChampBannedEnable, buttonNotifyChampBannedDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Honor Cast", "When friends are auto-honored after a game.",
                buttonNotifyHonorEnable, buttonNotifyHonorDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Returned to Lobby", "When the post-game screens are auto-skipped.",
                buttonNotifyReturnedToLobbyEnable, buttonNotifyReturnedToLobbyDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Queue Started", "When auto-queue starts a match search.",
                buttonNotifyAutoQueueEnable, buttonNotifyAutoQueueDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Game Starting", "When the game loads (In Progress).",
                buttonNotifyGameStartingEnable, buttonNotifyGameStartingDisable), "growx");
        events.add(divider(), "growx, gapy 2 2");
        events.add(toggleRow("Played Recently", "When someone in your game was also in your last 3 games.",
                buttonNotifyPlayedRecentlyEnable, buttonNotifyPlayedRecentlyDisable), "growx");
        panel.add(events, "growx");

        JLabel note = new JLabel("<html>The master switch and a topic must both be set for any notification to send.</html>");
        note.setFont(fSub);
        note.setForeground(Theme.TEXT_FAINT);
        panel.add(note, "growx, gapx 4");
        return panel;
    }

    // ---- Settings ----

    private JPanel buildSettings() {
        JPanel panel = section("Settings", "app behavior and preferences");

        Card window = new Card("insets 4 6 4 6, wrap 1, fillx", "[grow,fill]", "");
        window.add(cardTitle("Window and Updates", Icons.G.PIN), "gapbottom 6");
        window.add(toggleRow("Always On Top", null, buttonAlwaysOnTopEnable, buttonAlwaysOnTopDisable), "growx");
        window.add(toggleRow("Center GUI on Update", null, buttonCenterGUIEnable, buttonCenterGUIDisable), "growx");
        window.add(toggleRow("System Tray", null, buttonSystemTrayEnable, buttonSystemTrayDisable), "growx");
        window.add(toggleRow("Auto Check for Updates", null, buttonAutoCheckUpdateEnable, buttonAutoCheckUpdateDisable), "growx");
        window.add(divider(), "growx, gapy 2 2");
        uiScaleSpinner.setPreferredSize(new Dimension(px(72), px(28)));
        JPanel scaleControls = new JPanel(new MigLayout("insets 0, gap 6", "[]6[]"));
        scaleControls.setOpaque(false);
        scaleControls.add(uiScaleSpinner);
        styleButton(buttonUiScaleApply, "Apply", null, ButtonKind.PRIMARY);
        scaleControls.add(buttonUiScaleApply);
        window.add(spinnerRow("UI Scale (%)", "Size of everything in the app. Apply restarts to take effect.", scaleControls), "growx");
        panel.add(window, "growx");

        Card client = new Card("insets 4 6 8 6, wrap 1, fillx", "[grow,fill]", "");
        client.add(cardTitle("League Client", Icons.G.TRAY), "gapbottom 6");
        JPanel clientBtns = new JPanel(new MigLayout("insets 0, gap 8", "[]8[]"));
        clientBtns.setOpaque(false);
        styleButton(buttonHideClient, "Hide Client", Icons.G.TRAY, ButtonKind.NORMAL);
        styleButton(buttonShowClient, "Show Client", Icons.G.PIN, ButtonKind.NORMAL);
        clientBtns.add(buttonHideClient);
        clientBtns.add(buttonShowClient);
        client.add(clientBtns);
        JLabel clientNote = new JLabel("<html>Hide removes the League window entirely - the client keeps running "
                + "in the background (still logged in and in queue). Show brings it back.</html>");
        clientNote.setFont(fSub);
        clientNote.setForeground(Theme.TEXT_FAINT);
        client.add(clientNote, "growx, gaptop 8");
        panel.add(client, "growx");

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
        panel.add(prefs, "growx");
        return panel;
    }

    // ---- Info ----

    private JPanel buildInfo() {
        JPanel panel = section("Info", "about");

        Card about = new Card("insets 8 8 12 8, gap 16", "[]16[grow,fill]", "[]");
        ImageIcon big = kindredMark(px(64));
        about.add(big != null ? new JLabel(big) : new JLabel(), "aligny top");
        JPanel textCol = new JPanel(new MigLayout("insets 0, wrap 1, gap 4", "[grow,fill]"));
        textCol.setOpaque(false);
        JLabel name = new JLabel("Rift Helper");
        name.setFont(fTitle.deriveFont(fpt(20f)));
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
        facts.add(kvValue(UpdateChecker.CURRENT_VERSION));
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

    /** Wrap a champion list with a trailing "Swap" button that exchanges two of its picks. The swap
     *  persists via the list's existing (silent) save button, matching auto-save on any edit. */
    private JPanel listWithSwap(JPanel list, ChampionPicker[] group, JButton saveButton) {
        JPanel wrap = new JPanel(new MigLayout("insets 0, wrap 1, gap 6, fillx", "[grow,fill]"));
        wrap.setOpaque(false);
        wrap.add(list, "growx");
        JButton swapBtn = new JButton();
        styleButton(swapBtn, "Swap", Icons.G.SWAP, ButtonKind.GHOST);
        JPanel row = new JPanel(new MigLayout("insets 0, gap 0, fillx", "push[]"));
        row.setOpaque(false);
        row.add(swapBtn);
        wrap.add(row, "growx");
        new SwapController(swapBtn, group, saveButton::doClick);
        return wrap;
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
        b.setBorder(BorderFactory.createEmptyBorder(px(7), px(13), px(7), px(13)));
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
            b.setIcon(Icons.of(glyph, px(15), fg));
            b.setIconTextGap(text == null || text.isEmpty() ? 0 : px(7));
        }
    }

    private void styleLaneButton(JButton b, String text) {
        b.setText(text);
        b.setFont(fBody);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(px(6), px(12), px(6), px(12)));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(Theme.SURFACE_0);
        b.setForeground(Theme.TEXT_DIM);
        b.setIconTextGap(px(6));
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
            laneButtons[i].setIcon(Icons.of(glyphs[i], px(14), on ? Theme.ACCENT_TEXT : Theme.TEXT_DIM));
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
    public void addAutoClaimPassesEnableListener(ActionListener l) { buttonAutoClaimPassesEnable.addActionListener(l); }
    public void addAutoClaimPassesDisableListener(ActionListener l) { buttonAutoClaimPassesDisable.addActionListener(l); }
    public void addAutoMinimizeEnableListener(ActionListener l) { buttonAutoMinimizeEnable.addActionListener(l); }
    public void addAutoMinimizeDisableListener(ActionListener l) { buttonAutoMinimizeDisable.addActionListener(l); }
    public void addLobbyOverlayEnableListener(ActionListener l) { buttonLobbyOverlayEnable.addActionListener(l); }
    public void addLobbyOverlayDisableListener(ActionListener l) { buttonLobbyOverlayDisable.addActionListener(l); }
    public void addAramBenchOverlayEnableListener(ActionListener l) { buttonAramBenchOverlayEnable.addActionListener(l); }
    public void addAramBenchOverlayDisableListener(ActionListener l) { buttonAramBenchOverlayDisable.addActionListener(l); }
    public void addAramSwapToolsOverlayEnableListener(ActionListener l) { buttonAramSwapToolsOverlayEnable.addActionListener(l); }
    public void addAramSwapToolsOverlayDisableListener(ActionListener l) { buttonAramSwapToolsOverlayDisable.addActionListener(l); }
    public void addAramTop5OverlayEnableListener(ActionListener l) { buttonAramTop5OverlayEnable.addActionListener(l); }
    public void addAramTop5OverlayDisableListener(ActionListener l) { buttonAramTop5OverlayDisable.addActionListener(l); }
    public void addPlayersOverlayEnableListener(ActionListener l) { buttonPlayersOverlayEnable.addActionListener(l); }
    public void addPlayersOverlayDisableListener(ActionListener l) { buttonPlayersOverlayDisable.addActionListener(l); }
    /** Push the live Top 5 Ranked Choices into the ARAM tab panel. */
    public void setTop5(java.util.List<model.RankedChoice> choices) { aramTop5View.setChoices(choices); }

    // ---- Notifications ----
    public String getNotifyTopic() { return notifyTopicField.getText().trim(); }
    public void setNotifyTopic(String s) { notifyTopicField.setText(s == null ? "" : s); }
    /** Runs r on any edit to the topic field (auto-save, no Save button). */
    public void addNotifyTopicChangeListener(Runnable r) {
        notifyTopicField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { r.run(); }
            public void removeUpdate(DocumentEvent e) { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        });
    }
    public void addNotifyEnableListener(ActionListener l) { buttonNotifyEnable.addActionListener(l); }
    public void addNotifyDisableListener(ActionListener l) { buttonNotifyDisable.addActionListener(l); }
    public void addNotifyMatchFoundEnableListener(ActionListener l) { buttonNotifyMatchFoundEnable.addActionListener(l); }
    public void addNotifyMatchFoundDisableListener(ActionListener l) { buttonNotifyMatchFoundDisable.addActionListener(l); }
    public void addNotifyChampPickedEnableListener(ActionListener l) { buttonNotifyChampPickedEnable.addActionListener(l); }
    public void addNotifyChampPickedDisableListener(ActionListener l) { buttonNotifyChampPickedDisable.addActionListener(l); }
    public void addNotifyChampPickedAramEnableListener(ActionListener l) { buttonNotifyChampPickedAramEnable.addActionListener(l); }
    public void addNotifyChampPickedAramDisableListener(ActionListener l) { buttonNotifyChampPickedAramDisable.addActionListener(l); }
    public void addNotifyChampSwapAramEnableListener(ActionListener l) { buttonNotifyChampSwapAramEnable.addActionListener(l); }
    public void addNotifyChampSwapAramDisableListener(ActionListener l) { buttonNotifyChampSwapAramDisable.addActionListener(l); }
    public void addNotifyChampBannedEnableListener(ActionListener l) { buttonNotifyChampBannedEnable.addActionListener(l); }
    public void addNotifyChampBannedDisableListener(ActionListener l) { buttonNotifyChampBannedDisable.addActionListener(l); }
    public void addNotifyOnlyWhenAwayEnableListener(ActionListener l) { buttonNotifyOnlyWhenAwayEnable.addActionListener(l); }
    public void addNotifyOnlyWhenAwayDisableListener(ActionListener l) { buttonNotifyOnlyWhenAwayDisable.addActionListener(l); }
    public int getNotifyIdleSeconds() { return (Integer) notifyIdleSpinner.getValue(); }
    public void setNotifyIdleSeconds(int s) { notifyIdleSpinner.setValue(Math.max(5, s)); }
    public void addNotifyIdleSecondsChangeListener(Runnable r) { notifyIdleSpinner.addChangeListener(e -> r.run()); }
    public int getUiScalePercent() { return (Integer) uiScaleSpinner.getValue(); }
    public void setUiScalePercent(int p) { uiScaleSpinner.setValue(Math.max(50, Math.min(p, 200))); }
    public void addUiScaleChangeListener(Runnable r) { uiScaleSpinner.addChangeListener(e -> r.run()); }
    public void addUiScaleApplyListener(ActionListener l) { buttonUiScaleApply.addActionListener(l); }
    public void addNotifyHonorEnableListener(ActionListener l) { buttonNotifyHonorEnable.addActionListener(l); }
    public void addNotifyHonorDisableListener(ActionListener l) { buttonNotifyHonorDisable.addActionListener(l); }
    public void addNotifyReturnedToLobbyEnableListener(ActionListener l) { buttonNotifyReturnedToLobbyEnable.addActionListener(l); }
    public void addNotifyReturnedToLobbyDisableListener(ActionListener l) { buttonNotifyReturnedToLobbyDisable.addActionListener(l); }
    public void addNotifyAutoQueueEnableListener(ActionListener l) { buttonNotifyAutoQueueEnable.addActionListener(l); }
    public void addNotifyAutoQueueDisableListener(ActionListener l) { buttonNotifyAutoQueueDisable.addActionListener(l); }
    public void addNotifyGameStartingEnableListener(ActionListener l) { buttonNotifyGameStartingEnable.addActionListener(l); }
    public void addNotifyGameStartingDisableListener(ActionListener l) { buttonNotifyGameStartingDisable.addActionListener(l); }
    public void addNotifyPlayedRecentlyEnableListener(ActionListener l) { buttonNotifyPlayedRecentlyEnable.addActionListener(l); }
    public void addNotifyPlayedRecentlyDisableListener(ActionListener l) { buttonNotifyPlayedRecentlyDisable.addActionListener(l); }
    public void addScoutRefreshListener(ActionListener l) { buttonScoutRefresh.addActionListener(l); }
    public void addScoutEnableListener(ActionListener l) { buttonScoutEnable.addActionListener(l); }
    public void addScoutDisableListener(ActionListener l) { buttonScoutDisable.addActionListener(l); }
    public void addAutoBanCrowdFavoriteEnableListener(ActionListener l) { buttonAutoBanCrowdFavoriteEnable.addActionListener(l); }
    public void addAutoBanCrowdFavoriteDisableListener(ActionListener l) { buttonAutoBanCrowdFavoriteDisable.addActionListener(l); }
    public void addExportListener(ActionListener l) { buttonExport.addActionListener(l); }
    public void addImportListener(ActionListener l) { buttonImport.addActionListener(l); }
    public void addResetListener(ActionListener l) { buttonReset.addActionListener(l); }
    public void addTestListener(ActionListener l) { buttonTest.addActionListener(l); }

    /** Apply the window size: the size the user last dragged to (persisted) if any, else a computed
     *  default (~700 wide at 1080p, scaled up on higher-resolution 16:9 displays). Once the user has
     *  resized, that size is respected and never recomputed. Content scrolls within. */
    @Override
    public void pack() {
        validate();
        applyWindowSize(savedOrDefaultSize());
    }

    private Dimension savedOrDefaultSize() {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int w, h;
        if (PreferenceManager.hasWindowSize()) {
            w = PreferenceManager.getWindowWidth();
            h = PreferenceManager.getWindowHeight();
        } else {
            Dimension d = defaultSize();
            w = d.width;
            h = d.height;
        }
        return new Dimension(Math.min(w, screen.width), Math.min(h, screen.height));
    }

    /** ~700x600 at 1920x1080, scaled by the display's fitted-16:9 width (bigger on higher res). */
    private Dimension defaultSize() {
        Dimension res = Toolkit.getDefaultToolkit().getScreenSize();
        double ratio = 16.0 / 9.0;
        double boxW = (res.width / (double) res.height >= ratio) ? res.height * ratio : res.width;
        double factor = boxW / 1920.0;
        return new Dimension((int) Math.round(700 * factor), (int) Math.round(600 * factor));
    }

    /** Set size programmatically, recording it so the resize listener doesn't mistake it for a drag. */
    private void applyWindowSize(Dimension d) {
        lastProgrammaticSize = d;
        setSize(d);
    }

    /** A panel that fills the scroll pane's width but keeps its natural height, so content scrolls
     *  vertically only. */
    private static final class ScrollablePanel extends JPanel implements Scrollable {
        ScrollablePanel(LayoutManager layout) {
            super(layout);
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
            setBorder(BorderFactory.createEmptyBorder(px(9), px(12), px(9), px(11)));
            setIconTextGap(px(11));
            setIcon(Icons.of(glyph, px(17), Theme.TEXT_DIM));
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

package view;

import com.formdev.flatlaf.util.UIScale;
import model.Ntfy;
import net.miginfocom.swing.MigLayout;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URI;
import java.security.SecureRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Modal, paged wizard that walks a first-time user through setting up phone notifications via
 * ntfy.sh. Most users have never touched ntfy, so this is deliberately plain-language and hand-holds
 * every step: install the app, pick a secret topic, turn notifications on in Rift Helper, and send a
 * test.
 *
 * <p>Five steps, swapped in place, with a Back / Next footer (Next becomes Finish on the last step).
 * The dialog itself changes nothing except the topic: {@code setTopic} is called only when the user
 * taps "Use this topic" on step 3, and it fills / persists the value in the app's Notify tab. Turning
 * notifications on and choosing events stays a manual step the user does in the main window.</p>
 *
 * <p>Nothing here throws out to the caller: the browser launch is wrapped, and {@link Ntfy#publish}
 * is already fire-and-forget.</p>
 */
public class NotifyTutorialDialog extends JDialog {

    private static final int TOTAL = 5;
    private static final char[] TOPIC_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final Supplier<String> getTopic;
    private final Consumer<String> setTopic;

    private final SecureRandom rng = new SecureRandom();

    private final Font baseFont;
    private final Font fBody;
    private final Font fTitle;
    private final Font fSub;

    private final JLabel stepLabel = new JLabel();
    private final JPanel content = new JPanel(new BorderLayout());
    private final JButton back = new JButton("Back");
    private final JButton next = new JButton("Next");

    private int step = 1;

    // The suggested topic for step 3. Generated up front so it survives Back / Next navigation and is
    // only pushed into the app when the user explicitly taps "Use this topic".
    private String topic;

    public NotifyTutorialDialog(JFrame owner, Supplier<String> getTopic, Consumer<String> setTopic) {
        super(owner, "Set up phone notifications", true);
        this.getTopic = getTopic;
        this.setTopic = setTopic;
        this.topic = generateTopic();

        this.baseFont = resolveBaseFont();
        this.fBody = baseFont.deriveFont(Font.PLAIN, fpt(13f));
        this.fTitle = baseFont.deriveFont(Font.BOLD, fpt(16f));
        this.fSub = baseFont.deriveFont(Font.PLAIN, fpt(12f));

        buildShell();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(px(520), px(560));
        setMinimumSize(new Dimension(px(420), px(440)));
        setLocationRelativeTo(owner);
    }

    // ------------------------------------------------------------------ public API

    /** Open the dialog modally, starting on step 1. */
    public void open() {
        step = 1;
        showStep();
        setVisible(true);
    }

    // ------------------------------------------------------------------ shell / chrome

    private void buildShell() {
        JPanel root = new JPanel(new MigLayout(
                "insets 16 18 14 18, wrap 1, fill", "[grow,fill]", "[]4[]10[grow,fill]10[]"));
        root.setBackground(Theme.BG);
        setContentPane(root);

        root.add(makeLabel("Set up phone notifications", fTitle, Theme.TEXT), "growx");

        stepLabel.setFont(baseFont.deriveFont(Font.BOLD, fpt(11f)));
        stepLabel.setForeground(Theme.TEXT_FAINT);
        root.add(stepLabel, "growx");

        content.setBackground(Theme.BG);
        content.setOpaque(true);
        root.add(content, "grow, push");

        JPanel footer = new JPanel(new MigLayout("insets 0", "[]push[]", ""));
        footer.setOpaque(false);
        styleButton(back, Kind.GHOST);
        styleButton(next, Kind.PRIMARY);
        back.addActionListener(e -> {
            if (step > 1) {
                step--;
                showStep();
            }
        });
        next.addActionListener(e -> {
            if (step >= TOTAL) {
                dispose();
            } else {
                step++;
                showStep();
            }
        });
        footer.add(back);
        footer.add(next);
        root.add(footer, "growx");
    }

    private void showStep() {
        stepLabel.setText("Step " + step + " of " + TOTAL);
        content.removeAll();
        JComponent panel = switch (step) {
            case 1 -> buildIntro();
            case 2 -> buildInstall();
            case 3 -> buildTopic();
            case 4 -> buildTurnOn();
            default -> buildTest();
        };
        content.add(panel, BorderLayout.CENTER);
        back.setEnabled(step > 1);
        next.setText(step >= TOTAL ? "Finish" : "Next");
        content.revalidate();
        content.repaint();
    }

    // ------------------------------------------------------------------ steps

    private JComponent buildIntro() {
        JPanel p = stepPanel(FontAwesomeSolid.BELL, "Get alerts on your phone");
        p.add(body("Get a notification on your phone for each automated event - match found, "
                + "champion picked or banned, honor, queue started, game starting. One-time setup, "
                + "about a minute."), bodyCons());
        return p;
    }

    private JComponent buildInstall() {
        JPanel p = stepPanel(FontAwesomeSolid.MOBILE_ALT, "Install the app");
        p.add(body("On your phone, install the free app called ntfy (by Philipp Heckel) from Google "
                + "Play or the App Store. No account, no sign-up."), bodyCons());
        JButton open = button("Open ntfy.sh", Kind.NORMAL);
        open.addActionListener(e -> browse("https://ntfy.sh"));
        p.add(open, "gaptop " + px(6));
        return p;
    }

    private JComponent buildTopic() {
        JPanel p = stepPanel(FontAwesomeSolid.TAG, "Choose a topic");
        p.add(body("A topic is a private name only you know. Anyone who knows it can read your "
                + "alerts, so keep it secret and hard to guess."), bodyCons());

        JTextField field = new JTextField(topic);
        field.setEditable(false);
        field.setFont(baseFont.deriveFont(Font.BOLD, fpt(14f)));
        field.setForeground(Theme.TEXT);
        field.setBackground(Theme.SURFACE_2);
        field.setCaretColor(Theme.TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(px(8), px(10), px(8), px(10))));
        p.add(field, "growx, wmax " + px(460) + ", gaptop " + px(4));

        JLabel confirm = makeLabel(" ", fSub, Theme.GREEN);

        JPanel buttons = new JPanel(new MigLayout("insets 0, gap 10", ""));
        buttons.setOpaque(false);
        JButton suggest = button("Suggest another", Kind.GHOST);
        suggest.addActionListener(e -> {
            topic = generateTopic();
            field.setText(topic);
            confirm.setText(" ");
        });
        JButton use = button("Use this topic", Kind.PRIMARY);
        use.addActionListener(e -> {
            try {
                if (setTopic != null) {
                    setTopic.accept(topic);
                }
            } catch (Exception ex) {
                System.out.println("[NotifyTutorial] setTopic failed: " + ex.getMessage());
            }
            confirm.setForeground(Theme.GREEN);
            confirm.setText("Filled into Rift Helper");
        });
        buttons.add(suggest);
        buttons.add(use);
        p.add(buttons, "gaptop " + px(2));

        p.add(confirm, "gaptop " + px(2));

        p.add(body("In the ntfy app, tap +, choose Subscribe to topic, and enter this exact name."),
                bodyCons() + ", gaptop " + px(6));
        return p;
    }

    private JComponent buildTurnOn() {
        JPanel p = stepPanel(FontAwesomeSolid.TOGGLE_ON, "Turn it on");
        p.add(body("Back in Rift Helper's Notify tab, switch on Enable Notifications, then pick "
                + "which events you want."), bodyCons());
        return p;
    }

    private JComponent buildTest() {
        JPanel p = stepPanel(FontAwesomeSolid.PAPER_PLANE, "Test it");
        p.add(body("Send a test notification to make sure everything is wired up. It goes to the "
                + "topic saved in Rift Helper."), bodyCons());

        JLabel result = makeLabel(" ", fBody, Theme.TEXT_DIM);

        JButton send = button("Send Test Notification", Kind.PRIMARY);
        send.addActionListener(e -> {
            String t = (getTopic == null) ? null : getTopic.get();
            if (t == null || t.isBlank()) {
                result.setForeground(Theme.RED);
                result.setText("Set a topic first (step 3).");
                return;
            }
            Ntfy.publish(t, "Rift Helper", "Test notification - you are all set!", 4, "white_check_mark");
            result.setForeground(Theme.GREEN);
            result.setText("Sent. Check your phone - it should arrive within a few seconds.");
        });
        p.add(send, "gaptop " + px(6));
        p.add(result, "gaptop " + px(4) + ", growx, wmax " + px(460));
        return p;
    }

    // ------------------------------------------------------------------ small widgets

    /** A step surface: a leading accent icon next to a short title, ready for body text below. */
    private JPanel stepPanel(FontAwesomeSolid glyph, String title) {
        JPanel p = new JPanel(new MigLayout("insets 20, wrap 1, gap 10", "[grow,fill]"));
        p.setOpaque(false);

        JPanel head = new JPanel(new MigLayout("insets 0, gap 12", "[]12[grow]", ""));
        head.setOpaque(false);
        head.add(new JLabel(FontIcon.of(glyph, px(26), Theme.ACCENT)), "aligny center");
        head.add(makeLabel(title, baseFont.deriveFont(Font.BOLD, fpt(15f)), Theme.TEXT), "growx");
        p.add(head, "growx");
        return p;
    }

    private JLabel body(String text) {
        return makeLabel("<html><div>" + text + "</div></html>", fBody, Theme.TEXT_DIM);
    }

    private String bodyCons() {
        return "growx, wmax " + px(460);
    }

    private enum Kind {NORMAL, PRIMARY, GHOST}

    private JButton button(String text, Kind kind) {
        JButton b = new JButton(text);
        styleButton(b, kind);
        return b;
    }

    private void styleButton(JButton b, Kind kind) {
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
    }

    private JLabel makeLabel(String text, Font font, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(fg);
        return l;
    }

    // ------------------------------------------------------------------ helpers

    private void browse(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            System.out.println("[NotifyTutorial] browse failed: " + e.getMessage());
        }
    }

    /** {@code "rift-helper-"} plus 8 random chars from [a-z0-9], via SecureRandom. */
    private String generateTopic() {
        StringBuilder sb = new StringBuilder("rift-helper-");
        for (int i = 0; i < 8; i++) {
            sb.append(TOPIC_CHARS[rng.nextInt(TOPIC_CHARS.length)]);
        }
        return sb.toString();
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

    private static float fpt(float pt) {
        return Math.round(UIScale.scale(pt));
    }

    private static int px(int v) {
        return UIScale.scale(v);
    }
}

package view;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Font;

/**
 * ARAM bottom-right overlay: the swap tools grouped together. Auto Swap Priority, Auto Swap Survey,
 * and Auto Troll toggles (each over the SAME Enable/Disable button pair the main window uses, so they
 * stay in sync), plus a Troll Swap button that fires the main window's one-shot Troll Swap. No lists,
 * just the toggles and the button.
 */
public class AramSwapToolsOverlayPanel extends OverlayCard {

    public AramSwapToolsOverlayPanel(RiftHelperMainView v) {
        super(new MigLayout("insets 9 12 10 12, wrap 1, gap 3, fillx", "[grow,fill]"), 14);

        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        Font fRow = base.deriveFont(12f);
        Font fTitle = base.deriveFont(Font.BOLD, 13f);

        JLabel title = new JLabel("Swap Tools");
        title.setFont(fTitle);
        title.setForeground(Theme.TEXT);
        title.setIcon(Icons.of(Icons.G.SWAP, 13, Theme.ACCENT));
        title.setIconTextGap(6);
        add(title, "growx, gapbottom 2");

        add(row("Auto Swap Priority", fRow, v.buttonAutoSwapEnable, v.buttonAutoSwapDisable), "growx");
        add(row("Auto Swap Survey", fRow, v.buttonAutoSwapSurveyEnable, v.buttonAutoSwapSurveyDisable), "growx");
        add(row("Auto Troll (loop)", fRow, v.buttonTrollSwapToggleEnable, v.buttonTrollSwapToggleDisable), "growx");

        JButton troll = new JButton("Troll Swap");
        troll.setFont(fRow);
        troll.setForeground(Theme.RED);
        troll.addActionListener(e -> v.buttonTrollSwap.doClick()); // reuse the main window's action
        add(troll, "growx, gaptop 4");
    }

    private JComponent row(String label, Font font, JButton enable, JButton disable) {
        JPanel row = new JPanel(new MigLayout("insets 2 0 2 0, gap 10, fillx", "[grow,fill][]"));
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(font);
        l.setForeground(Theme.TEXT);
        row.add(l, "growx");
        row.add(new ToggleSwitch(enable, disable));
        return row;
    }
}

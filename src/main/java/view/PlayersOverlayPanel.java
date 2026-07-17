package view;

import model.ScoutPlayer;
import model.ScoutReport;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.Window;
import java.util.List;

/**
 * Players (scout) overlay: a compact, rounded translucent card showing the live scout report for the
 * current game. Bottom-right, all modes. Rendering is purely data-driven off {@link ScoutReport}'s
 * ally/enemy lists, so it adapts to each mode's team size (5s in Rift/ARAM, smaller in Arena).
 */
public class PlayersOverlayPanel extends OverlayCard {
    private final JPanel body;
    private final Font fTitle;
    private final Font fName;
    private final Font fSub;

    public PlayersOverlayPanel() {
        super(new MigLayout("insets 9 12 10 12, wrap 1, gap 4, fillx", "[grow,fill]"), 14);

        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
        fTitle = base.deriveFont(Font.BOLD, 13f);
        fName = base.deriveFont(Font.BOLD, 12f);
        fSub = base.deriveFont(11f);

        JLabel title = new JLabel("Players");
        title.setFont(fTitle);
        title.setForeground(Theme.TEXT);
        title.setIcon(Icons.of(Icons.G.PLAYERS, 13, Theme.ACCENT));
        title.setIconTextGap(6);
        add(title, "growx, gapbottom 2");

        body = new JPanel(new MigLayout("insets 0, wrap 1, gap 2, fillx", "[grow,fill]"));
        body.setOpaque(false);
        add(body, "growx");
        setReport(null);
    }

    public void setReport(ScoutReport r) {
        body.removeAll();
        if (r == null || r.isEmpty()) {
            JLabel none = new JLabel("No active game");
            none.setFont(fSub);
            none.setForeground(Theme.TEXT_FAINT);
            body.add(none, "growx");
        } else {
            addTeam("Your Team", r.allies);
            addTeam("Enemy Team", r.enemies);
        }
        body.revalidate();
        body.repaint();
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) {
            w.pack();
        }
    }

    private void addTeam(String label, List<ScoutPlayer> players) {
        if (players == null || players.isEmpty()) {
            return;
        }
        JLabel head = new JLabel(label);
        head.setFont(fSub);
        head.setForeground(Theme.TEXT_DIM);
        body.add(head, "growx, gaptop 4");
        for (ScoutPlayer p : players) {
            body.add(row(p), "growx");
        }
    }

    private JPanel row(ScoutPlayer p) {
        JPanel row = new JPanel(new MigLayout("insets 1 0 1 0, gap 8, fillx", "[grow,fill][]"));
        row.setOpaque(false);

        JPanel col = new JPanel(new MigLayout("insets 0, wrap 1, gap 0", "[grow,fill]"));
        col.setOpaque(false);
        String name = (p.summonerName == null || p.summonerName.isBlank()) ? "(hidden)" : p.summonerName;
        String champ = (p.championName() != null && !p.championName().isBlank()) ? "  ·  " + p.championName() : "";
        JLabel nameLbl = new JLabel(name + champ);
        nameLbl.setFont(fName);
        nameLbl.setForeground(Theme.TEXT);
        col.add(nameLbl, "growx");
        JLabel subLbl = new JLabel(rankText(p) + "  ·  " + p.recentWins + "W " + p.recentLosses + "L");
        subLbl.setFont(fSub);
        subLbl.setForeground(Theme.TEXT_DIM);
        col.add(subLbl, "growx");
        row.add(col, "growx");

        if (p.playedRecently) {
            JLabel badge = new JLabel(p.playedRecentlyCount + "x");
            badge.setFont(fSub);
            badge.setForeground(Theme.ACCENT);
            row.add(badge, "aligny top");
        }
        return row;
    }

    private String rankText(ScoutPlayer p) {
        if (p.soloRank != null && !p.soloRank.isBlank()) {
            return p.soloRank;
        }
        if (p.flexRank != null && !p.flexRank.isBlank()) {
            return p.flexRank + " (Flex)";
        }
        return "Unranked";
    }
}

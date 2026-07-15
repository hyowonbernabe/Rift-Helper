package view;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.formdev.flatlaf.util.UIScale;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A compact, searchable champion selector. The face shows the selected champion's icon and name;
 * clicking opens a popup with a search field and an icon list filtered as you type.
 *
 * Exposes a String-based API (getSelectedName / setSelectedName) so the view's existing
 * getComboBoxXxx() / setComboBoxXxxPriority(String[]) methods keep working unchanged.
 */
public class ChampionPicker extends JComponent {
    /** First entry in the dropdown; selecting it clears the pick. */
    public static final String NONE = "None";
    private static final int ICON = UIScale.scale(22);
    private static final int ROW_H = UIScale.scale(30);
    private static final int LIST_ICON = UIScale.scale(24);

    private final List<String> items = new ArrayList<>();
    private String selected;
    private ImageIcon selectedIcon;
    private Runnable onChange;

    private JPopupMenu popup;
    private JTextField search;
    private JList<String> list;
    private DefaultListModel<String> model;

    public ChampionPicker() {
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Font base = UIManager.getFont("Label.font");
        if (base != null) {
            setFont(base);
        }
        Dimension pref = new Dimension(UIScale.scale(180), ROW_H);
        setPreferredSize(pref);
        setMinimumSize(new Dimension(UIScale.scale(130), ROW_H));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_H));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup();
            }
        });
    }

    public void setItems(List<String> champions) {
        items.clear();
        items.addAll(champions);
    }

    public String getSelectedName() {
        return selected;
    }

    public void setSelectedName(String name) {
        if (name != null && name.isBlank()) {
            name = null;
        }
        this.selected = name;
        this.selectedIcon = null;
        if (name != null) {
            final String requested = name;
            ChampionIcons.load(name, ICON, icon -> {
                if (requested.equals(selected)) {
                    selectedIcon = icon;
                    repaint();
                }
            });
        }
        repaint();
    }

    public void clearSelection() {
        setSelectedName(null);
    }

    public void setOnChange(Runnable r) {
        this.onChange = r;
    }

    private void showPopup() {
        requestFocusInWindow();
        if (popup == null) {
            buildPopup();
        }
        search.setText("");
        filter("");
        popup.setPopupSize(Math.max(getWidth(), UIScale.scale(230)), UIScale.scale(300));
        popup.show(this, 0, getHeight() + 2);
        SwingUtilities.invokeLater(() -> search.requestFocusInWindow());
    }

    private void buildPopup() {
        model = new DefaultListModel<>();
        list = new JList<>(model);
        list.setCellRenderer(new ChampionRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(ROW_H);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                choose();
            }
        });

        search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Search champion");
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter(search.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                filter(search.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                filter(search.getText());
            }
        });
        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        if (!model.isEmpty()) {
                            list.setSelectedIndex(0);
                            list.requestFocusInWindow();
                        }
                    }
                    case KeyEvent.VK_ENTER -> {
                        if (!model.isEmpty()) {
                            list.setSelectedIndex(Math.max(0, list.getSelectedIndex()));
                            choose();
                        }
                    }
                    case KeyEvent.VK_ESCAPE -> popup.setVisible(false);
                    default -> {
                    }
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    choose();
                }
            }
        });

        JPanel content = new JPanel(new BorderLayout(0, 6));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(search, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        content.add(scroll, BorderLayout.CENTER);

        popup = new JPopupMenu();
        popup.setFocusable(true);
        popup.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        popup.setLayout(new BorderLayout());
        popup.add(content, BorderLayout.CENTER);
    }

    private void filter(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        model.clear();
        model.addElement(NONE); // always first, so you can clear the pick
        for (String name : items) {
            if (q.isEmpty() || name.toLowerCase(Locale.ROOT).contains(q)) {
                model.addElement(name);
            }
        }
    }

    private void choose() {
        String value = list.getSelectedValue();
        if (value != null) {
            setSelectedName(NONE.equals(value) ? null : value);
            if (onChange != null) {
                onChange.run();
            }
        }
        popup.setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        g2.setColor(Theme.SURFACE_2);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 7, 7);
        g2.setColor(isFocusOwner() ? Theme.ACCENT : Theme.LINE);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 7, 7);

        int pad = 4;
        int iconY = (h - ICON) / 2;
        if (selected != null && selectedIcon != null) {
            g2.drawImage(selectedIcon.getImage(), pad, iconY, ICON, ICON, this);
        } else {
            g2.setColor(Theme.SURFACE_0);
            g2.fillRoundRect(pad, iconY, ICON, ICON, 5, 5);
        }

        int textX = pad + ICON + 8;
        if (selected != null) {
            g2.setColor(Theme.TEXT);
            g2.setFont(getFont());
            drawClipped(g2, selected, textX, h, w - textX - 16);
        } else {
            g2.setColor(Theme.TEXT_FAINT);
            g2.setFont(getFont().deriveFont(Font.ITALIC));
            drawClipped(g2, "Add champion", textX, h, w - textX - 16);
        }

        g2.setColor(Theme.TEXT_DIM);
        int cx = w - 15;
        int cy = h / 2 - 1;
        g2.drawLine(cx, cy, cx + 4, cy + 4);
        g2.drawLine(cx + 4, cy + 4, cx + 8, cy);
        g2.dispose();
    }

    private void drawClipped(Graphics2D g2, String text, int x, int h, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String t = text;
        if (fm.stringWidth(t) > maxWidth) {
            while (t.length() > 1 && fm.stringWidth(t + "…") > maxWidth) {
                t = t.substring(0, t.length() - 1);
            }
            t = t + "…";
        }
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(t, x, ty);
    }

    private static class ChampionRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel icon = new JLabel();
        private final JLabel text = new JLabel();

        ChampionRenderer() {
            super(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            icon.setPreferredSize(new Dimension(LIST_ICON, LIST_ICON));
            add(icon, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> jList, String value,
                                                      int index, boolean selected, boolean focus) {
            text.setText(value);
            setBackground(selected ? Theme.ACCENT : Theme.SURFACE_2);
            if (NONE.equals(value)) {
                text.setForeground(selected ? Theme.ON_ACCENT : Theme.TEXT_FAINT);
                icon.setIcon(null);
                return this;
            }
            text.setForeground(selected ? Theme.ON_ACCENT : Theme.TEXT);
            ImageIcon cached = ChampionIcons.cached(value, LIST_ICON);
            icon.setIcon(cached);
            if (cached == null) {
                ChampionIcons.ensure(value, LIST_ICON, jList);
            }
            return this;
        }
    }
}

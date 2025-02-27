package view;

import model.DDragonParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;

public class AutoSwapView extends JFrame {
    private JPanel panelAutoSwap;
    private JLabel labelAutoSwap;
    private JPanel panelAutoSwapOperation;
    private JComboBox comboBoxPriority1;
    private JLabel panelPriority1;
    private JComboBox comboBoxPriority2;
    private JComboBox comboBoxPriority3;
    private JComboBox comboBoxPriority4;
    private JComboBox comboBoxPriority5;
    private JComboBox comboBoxPriority6;
    private JComboBox comboBoxPriority7;
    private JComboBox comboBoxPriority8;
    private JComboBox comboBoxPriority9;
    private JComboBox comboBoxPriority10;
    private JPanel panelButtons;
    private JButton buttonAdd;
    private JButton buttonSubtract;
    private JLabel panelPriority2;
    private JLabel panelPriority3;
    private JLabel panelPriority4;
    private JLabel panelPriority5;
    private JLabel panelPriority6;
    private JLabel panelPriority7;
    private JLabel panelPriority8;
    private JLabel panelPriority9;
    private JLabel panelPriority10;
    private JButton buttonStop;
    private JButton buttonStart;

    public AutoSwapView() {
        setTitle("Rift Helper");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setContentPane(panelAutoSwap);
        panelAutoSwap.setBorder(new EmptyBorder(20, 20, 20, 20));

        List<String> championNames = DDragonParser.fetchChampionNames();

        populateComboBox(comboBoxPriority1, championNames);
        populateComboBox(comboBoxPriority2, championNames);
        populateComboBox(comboBoxPriority3, championNames);
        populateComboBox(comboBoxPriority4, championNames);
        populateComboBox(comboBoxPriority5, championNames);
        populateComboBox(comboBoxPriority6, championNames);
        populateComboBox(comboBoxPriority7, championNames);
        populateComboBox(comboBoxPriority8, championNames);
        populateComboBox(comboBoxPriority9, championNames);
        populateComboBox(comboBoxPriority10, championNames);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void populateComboBox(JComboBox<String> comboBox, List<String> items) {
        comboBox.removeAllItems();
        comboBox.addItem(null);
        for (String item : items) {
            comboBox.addItem(item);
        }
    }
}

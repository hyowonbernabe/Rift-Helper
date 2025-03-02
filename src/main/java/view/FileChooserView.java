package view;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.io.File;

public class FileChooserView {
    public File selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Preferences File");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}
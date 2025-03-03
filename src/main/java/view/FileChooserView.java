package view;

import javax.swing.*;
import java.io.File;

public class FileChooserView {
    public File selectFile(String dialogTitle) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public String selectDirectory(String dialogTitle) {
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setDialogTitle(dialogTitle);
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        directoryChooser.setAcceptAllFileFilterUsed(false);

        int result = directoryChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return directoryChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
}
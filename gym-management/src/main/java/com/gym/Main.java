package com.gym;

import com.gym.storage.DataStore;
import com.gym.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to default look and feel
        }

        DataStore dataStore = new DataStore("data");

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(dataStore);
            frame.setVisible(true);
        });
    }
}

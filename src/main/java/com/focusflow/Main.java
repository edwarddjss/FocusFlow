package com.focusflow;

import com.focusflow.model.settings.SettingsController;
import com.focusflow.model.timer.TimerManager;
import com.focusflow.view.MainFrame;

import javax.swing.*;

/**
 * Main entry point for the FocusFlow application.
 *
 * @author Fareed Uddin
 */
public class Main {

    public static void main(String[] args) {
        // set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore, use default
        }

        SwingUtilities.invokeLater(() -> {
            initializeApplication();
        });
    }

    private static void initializeApplication() {
        SettingsController.getInstance().loadSettings();
        TimerManager.getInstance();

        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        mainFrame.setLocationRelativeTo(null);

        System.out.println("FocusFlow started successfully!");
    }
}

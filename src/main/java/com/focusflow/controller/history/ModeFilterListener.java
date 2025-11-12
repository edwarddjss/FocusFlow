package com.focusflow.controller.history;

import com.focusflow.model.session.SessionLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listener for mode filter changes in history view.
 *
 * @author Frank Watkins
 */
public class ModeFilterListener implements ActionListener {

    private final SessionLogger sessionLogger;
    private String currentMode;

    public ModeFilterListener(SessionLogger sessionLogger) {
        this.sessionLogger = sessionLogger;
        this.currentMode = "All";
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        applyModeFilter(command);
    }

    /**
     * Applies a mode filter.
     */
    public void applyModeFilter(String mode) {
        this.currentMode = mode;

        if ("All".equals(mode)) {
            sessionLogger.clearModeFilter();
        } else {
            sessionLogger.filterByMode(mode);
        }
    }

}

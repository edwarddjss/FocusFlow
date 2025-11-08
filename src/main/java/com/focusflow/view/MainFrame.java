package com.focusflow.view;

import com.focusflow.view.timer.TimerPanel;
import com.focusflow.view.history.HistoryPanel;
import com.focusflow.view.planner.PlannerPanel;
import com.focusflow.view.settings.SettingsPanel;
import com.focusflow.observer.Event;
import com.focusflow.observer.Observer;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame with tabbed navigation.
 *
 * @author Gianluca Binetti
 */
public class MainFrame extends JFrame implements Observer {

    private static final String TITLE = "FocusFlow";
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 700;

    private JTabbedPane tabbedPane;
    private TimerPanel timerPanel;
    private HistoryPanel historyPanel;
    private PlannerPanel plannerPanel;
    private SettingsPanel settingsPanel;

    /**
     * Creates the main application frame.
     */
    public MainFrame() {
        initializeFrame();
        initializePanels();
        initializeTabbedPane();
        com.focusflow.model.timer.TimerManager.getInstance().attach(this);
    }

    /**
     * Configures the frame properties.
     */
    private void initializeFrame() {
        setTitle(TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app_icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }

    /**
     * Creates all view panels.
     */
    private void initializePanels() {
        timerPanel = new TimerPanel();
        historyPanel = new HistoryPanel();
        plannerPanel = new PlannerPanel();
        settingsPanel = new SettingsPanel();
    }

    /**
     * Sets up the tabbed navigation pane.
     */
    private void initializeTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 14));

        tabbedPane.addTab("Timer", createTabIcon("timer"), timerPanel, "Start a focus session");
        tabbedPane.addTab("History", createTabIcon("history"), historyPanel, "View session history");
        tabbedPane.addTab("Planner", createTabIcon("planner"), plannerPanel, "Plan your schedule and get coaching");
        tabbedPane.addTab("Settings", createTabIcon("settings"), settingsPanel, "Configure preferences");

        add(tabbedPane, BorderLayout.CENTER);
    }

    private Icon createTabIcon(String name) {
        try {
            return new ImageIcon(getClass().getResource("/icons/" + name + ".png"));
        } catch (Exception e) {
            return null;
        }
    }

    public TimerPanel getTimerPanel() {
        return timerPanel;
    }

    public HistoryPanel getHistoryPanel() {
        return historyPanel;
    }

    public PlannerPanel getPlannerPanel() {
        return plannerPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    /**
     * Switches to the tab at the given index.
     */
    public void switchToTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * Switches to the Timer tab.
     */
    public void switchToTimerTab() {
        switchToTab(0);
    }

    /**
     * Start study session from planner event.
     */
    public void startStudySession(com.focusflow.model.planner.Planner.PlannerEvent event) {
        switchToTimerTab();

        String modeStr = event.getTimerMode();
        if (modeStr != null) {
            com.focusflow.model.timer.TimerManager manager = com.focusflow.model.timer.TimerManager.getInstance();
            switch (modeStr.toUpperCase()) {
                case "POMODORO":
                    manager.setTimerMode(new com.focusflow.model.timer.PomodoroMode());
                    break;
                case "52/17":
                    manager.setTimerMode(new com.focusflow.model.timer.FiftyTwoSeventeenMode());
                    break;
                case "ULTRADIAN":
                    manager.setTimerMode(new com.focusflow.model.timer.UltradianMode());
                    break;
                case "CUSTOM":
                    manager.setTimerMode(new com.focusflow.model.timer.CustomMode(25, 5));
                    break;
            }
        }
    }

    @Override
    public void update(Event event) {
        if (event.getType() == Event.EventType.TIMER_COMPLETED) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Timer Completed!",
                        "FocusFlow",
                        JOptionPane.INFORMATION_MESSAGE);
                toFront();
                requestFocus();
            });
        }
    }
}

package com.focusflow.view.timer;

import com.focusflow.command.*;
import com.focusflow.model.timer.*;
import com.focusflow.model.session.SessionLogger;
import com.focusflow.model.session.SessionRecord;
import com.focusflow.model.settings.SettingsController;
import com.focusflow.observer.Event;
import com.focusflow.observer.Observer;
import com.focusflow.util.SoundManager;

import javax.swing.*;
import java.awt.*;

/**
 * Panel displaying the timer interface with controls.
 * Handles user interaction with the timer functionality.
 *
 * @author Edward De Jesus
 */
public class TimerPanel extends JPanel implements Observer {

    private JLabel timerLabel;
    private JLabel statusLabel;
    private JComboBox<String> modeSelector;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;
    private JButton skipButton;
    private JLabel cycleCountLabel;
    private JPanel circlePanel;

    private TimerManager timerManager;
    private SessionLogger sessionLogger;
    private int completedCycles; // tracks how many work sessions completed

    // Command pattern - encapsulated timer operations
    private Command startCommand;
    private Command pauseCommand;
    private Command resetCommand;
    private Command skipCommand;

    /**
     * Creates a new TimerPanel.
     */
    public TimerPanel() {
        timerManager = TimerManager.getInstance();
        timerManager.attach(this);
        sessionLogger = SessionLogger.getInstance();
        completedCycles = 0;

        initializeUI();
        initializeListeners();
        updateDisplay();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Timer display panel (center)
        JPanel timerDisplayPanel = createTimerDisplayPanel();
        add(timerDisplayPanel, BorderLayout.CENTER);

        // Control panel (bottom)
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        // Mode selector panel (top)
        JPanel modePanel = createModePanel();
        add(modePanel, BorderLayout.NORTH);
    }

    /**
     * Creates the timer display panel.
     */
    private JPanel createTimerDisplayPanel() {
        JPanel outerPanel = new JPanel(new GridBagLayout());

        // Create circular progress panel
        circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int strokeWidth = 10;
                int size = Math.min(getWidth(), getHeight()) - strokeWidth - 10;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Draw background circle (gray track)
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawOval(x, y, size, size);

                // Calculate progress
                int totalDuration = timerManager.isWorkPhase()
                        ? timerManager.getCurrentMode().getWorkDuration()
                        : timerManager.getCurrentMode().getBreakDuration();
                int remaining = timerManager.getRemainingTime();
                double progress = (double) remaining / totalDuration;

                // Draw progress arc (starts at top, goes clockwise)
                int arcAngle = (int) (360 * progress);
                Color progressColor = timerManager.isWorkPhase()
                        ? new Color(70, 130, 180)  // Steel blue for work
                        : new Color(76, 175, 80);   // Green for break
                g2d.setColor(progressColor);
                g2d.drawArc(x, y, size, size, 90, arcAngle);

                g2d.dispose();
            }
        };
        circlePanel.setLayout(new BoxLayout(circlePanel, BoxLayout.Y_AXIS));
        circlePanel.setPreferredSize(new Dimension(300, 300));
        circlePanel.setOpaque(false);

        timerLabel = new JLabel("25:00");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 72));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Ready to focus");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cycleCountLabel = new JLabel("Completed cycles: 0");
        cycleCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cycleCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        circlePanel.add(Box.createVerticalGlue());
        circlePanel.add(timerLabel);
        circlePanel.add(Box.createVerticalStrut(10));
        circlePanel.add(statusLabel);
        circlePanel.add(Box.createVerticalStrut(15));
        circlePanel.add(cycleCountLabel);
        circlePanel.add(Box.createVerticalGlue());

        outerPanel.add(circlePanel);
        return outerPanel;
    }

    /**
     * Creates the control panel with buttons.
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");
        resetButton = new JButton("Reset");
        skipButton = new JButton("Skip");

        panel.add(startButton);
        panel.add(pauseButton);
        panel.add(resetButton);
        panel.add(skipButton);

        return panel;
    }

    /**
     * Creates the mode selection panel.
     */
    private JPanel createModePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel modeLabel = new JLabel("Timer Mode: ");
        modeSelector = new JComboBox<>(new String[]{
                "Pomodoro (25/5)",
                "52/17",
                "Ultradian (90/20)",
                "Custom"
        });

        panel.add(modeLabel);
        panel.add(modeSelector);

        return panel;
    }

    /**
     * Initializes button listeners using Command pattern.
     */
    private void initializeListeners() {
        // Create command objects
        startCommand = new StartCommand(timerManager);
        pauseCommand = new PauseCommand(timerManager);
        resetCommand = new ResetCommand(timerManager);
        skipCommand = new SkipCommand(timerManager);

        // Attach commands to buttons
        startButton.addActionListener(e -> startCommand.execute());
        pauseButton.addActionListener(e -> pauseCommand.execute());
        resetButton.addActionListener(e -> resetCommand.execute());
        skipButton.addActionListener(e -> skipCommand.execute());
        modeSelector.addActionListener(e -> handleModeSelection());
    }

    /**
     * Handles mode selection from the combo box.
     */
    private void handleModeSelection() {
        String selectedMode = (String) modeSelector.getSelectedItem();
        // System.out.println("Mode changed to: " + selectedMode);
        TimerMode mode = getModeByName(selectedMode);
        if (mode != null) {
            timerManager.setTimerMode(mode);
        }
    }

    /**
     * Gets a timer mode by its display name.
     */
    private TimerMode getModeByName(String name) {
        if (name == null) return null;
        if (name.startsWith("Pomodoro")) {
            return new PomodoroMode();
        } else if (name.equals("52/17")) {
            return new FiftyTwoSeventeenMode();
        } else if (name.startsWith("Ultradian")) {
            return new UltradianMode();
        } else if (name.equals("Custom")) {
            return showCustomModeDialog();
        }
        return null;
    }

    /**
     * Shows dialog for custom timer configuration.
     */
    private TimerMode showCustomModeDialog() {
        SettingsController settings = SettingsController.getInstance();
        int savedWork = getIntSetting(settings, SettingsController.KEY_CUSTOM_WORK_DURATION, 25);
        int savedBreak = getIntSetting(settings, SettingsController.KEY_CUSTOM_BREAK_DURATION, 5);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JSpinner workSpinner = new JSpinner(new SpinnerNumberModel(savedWork, 1, 180, 1));
        JSpinner breakSpinner = new JSpinner(new SpinnerNumberModel(savedBreak, 1, 60, 1));
        panel.add(new JLabel("Work duration (minutes):"));
        panel.add(workSpinner);
        panel.add(new JLabel("Break duration (minutes):"));
        panel.add(breakSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Custom Timer Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int workMinutes = (Integer) workSpinner.getValue();
            int breakMinutes = (Integer) breakSpinner.getValue();
            settings.setSetting(SettingsController.KEY_CUSTOM_WORK_DURATION, workMinutes);
            settings.setSetting(SettingsController.KEY_CUSTOM_BREAK_DURATION, breakMinutes);
            settings.saveSettings();
            return new CustomMode(workMinutes, breakMinutes);
        }
        return null;
    }

    /**
     * Updates the display based on TimerManager state.
     */
    private void updateDisplay() {
        timerLabel.setText(timerManager.getFormattedTime());

        if (timerManager.isRunning()) {
            statusLabel.setText(timerManager.isWorkPhase() ? "Focus time!" : "Break time!");
        } else {
            statusLabel.setText("Ready to focus");
        }

        cycleCountLabel.setText("Completed cycles: " + completedCycles);

        // Repaint the progress circle
        if (circlePanel != null) {
            circlePanel.repaint();
        }
    }

    @Override
    public void update(Event event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()) {
                case TIMER_STARTED:
                case TIMER_TICK:
                case TIMER_PAUSED:
                case TIMER_RESET:
                case MODE_CHANGED:
                    updateDisplay();
                    break;
                case TIMER_COMPLETED:
                    playNotificationSound();
                    checkAutoStart();
                    updateDisplay();
                    break;
                case SESSION_COMPLETED:
                    logCompletedSession();
                    completedCycles++;
                    updateDisplay();
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Safely gets an integer setting, handling Double conversion from JSON storage.
     */
    private int getIntSetting(SettingsController settings, String key, int defaultValue) {
        Object value = settings.getSetting(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Checks if auto-start is enabled and starts the next phase automatically.
     */
    private void checkAutoStart() {
        SettingsController settings = SettingsController.getInstance();
        boolean wasWorkPhase = timerManager.isWorkPhase();

        javax.swing.Timer delayTimer = new javax.swing.Timer(300, e -> {
            if (wasWorkPhase) {
                if (settings.getSetting(SettingsController.KEY_AUTO_START_BREAKS, false)) {
                    timerManager.startSession();
                }
            } else {
                if (settings.getSetting(SettingsController.KEY_AUTO_START_WORK, false)) {
                    timerManager.startSession();
                }
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    /**
     * Plays the notification sound if enabled in settings.
     */
    private void playNotificationSound() {
        SettingsController settings = SettingsController.getInstance();
        if (settings.isSoundEnabled()) {
            SoundManager.getInstance().playSound();
        }
    }

    /**
     * Logs the completed session to the session history.
     */
    private void logCompletedSession() {
        int durationMinutes = timerManager.getCurrentMode().getWorkDuration() / 60;
        String modeName = timerManager.getCurrentMode().getName();
        SessionRecord record = new SessionRecord(modeName, durationMinutes);
        record.complete();
        sessionLogger.logSession(record);
    }
}

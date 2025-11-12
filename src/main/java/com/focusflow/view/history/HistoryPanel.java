package com.focusflow.view.history;

import com.focusflow.controller.history.DateFilterListener;
import com.focusflow.controller.history.ModeFilterListener;
import com.focusflow.model.session.SessionLogger;
import com.focusflow.model.session.SessionRecord;
import com.focusflow.model.session.StatisticsManager;
import com.focusflow.observer.Event;
import com.focusflow.observer.Observer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel displaying session history and statistics.
 *
 * @author Frank Watkins
 */
public class HistoryPanel extends JPanel implements Observer {

    private JTable historyTable;
    private JComboBox<String> dateFilter;
    private JComboBox<String> modeFilter;
    private JButton refreshButton;
    private JLabel totalTimeLabel;
    private JLabel avgSessionLabel;
    private JLabel completionRateLabel;
    private JLabel streakLabel;

    private DefaultTableModel tableModel;
    private SessionLogger sessionLogger;
    private StatisticsManager statisticsManager;

    /**
     * Creates a new HistoryPanel.
     */
    public HistoryPanel() {
        sessionLogger = SessionLogger.getInstance();
        statisticsManager = new StatisticsManager(sessionLogger);

        initializeUI();
        initializeListeners();

        // Attach to SessionLogger for real-time updates when sessions are logged
        sessionLogger.addObserver(this);

        refreshData();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Filter panel (top)
        JPanel filterPanel = createFilterPanel();
        add(filterPanel, BorderLayout.NORTH);

        // Table panel (center)
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Statistics panel (bottom)
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the filter panel.
     */
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filters"));

        panel.add(new JLabel("Date Range:"));
        dateFilter = new JComboBox<>(new String[] {
                "All Time", "Today", "This Week", "This Month", "Last 7 Days", "Last 30 Days"
        });

        panel.add(dateFilter);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Mode:"));
        modeFilter = new JComboBox<>(new String[] {
                "All Modes", "Pomodoro", "52/17", "Ultradian", "Custom"
        });

        panel.add(modeFilter);
        panel.add(Box.createHorizontalStrut(15));
        refreshButton = new JButton("Refresh");
        panel.add(refreshButton);

        return panel;
    }

    /**
     * Creates the table panel.
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Session History"));

        String[] columnNames = { "Date", "Time", "Mode", "Duration", "Type", "Completed" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(22);
        historyTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the statistics panel.
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        totalTimeLabel = new JLabel("Total: 0h 0m", SwingConstants.CENTER);
        avgSessionLabel = new JLabel("Avg: 0m", SwingConstants.CENTER);
        completionRateLabel = new JLabel("Rate: 0%", SwingConstants.CENTER);
        streakLabel = new JLabel("Streak: 0 days", SwingConstants.CENTER);

        panel.add(totalTimeLabel);
        panel.add(avgSessionLabel);
        panel.add(completionRateLabel);
        panel.add(streakLabel);

        return panel;
    }

    /**
     * Initializes event listeners.
     */
    private void initializeListeners() {
        dateFilter.addActionListener(new DateFilterListener(sessionLogger));
        modeFilter.addActionListener(new ModeFilterListener(sessionLogger));
        refreshButton.addActionListener(e -> refreshData());
    }

    /**
     * Refreshes the data display.
     */
    public void refreshData() {
        tableModel.setRowCount(0);
        showSessionHistory();
        statisticsManager.calculateStatistics();
        updateStatistics();
    }

    /**
     * Shows the session history in the table.
     */
    public void showSessionHistory() {
        List<SessionRecord> sessions = sessionLogger.getAllSessions();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        for (SessionRecord session : sessions) {
            String date = session.getStartTime().format(dateFormatter);
            String time = session.getStartTime().format(timeFormatter);
            String mode = session.getModeName();
            String duration = session.getDurationMinutes() + " min";
            String type = session.isCompleted() ? "Work" : "Break";
            String completed = session.isCompleted() ? "Yes" : "No";

            tableModel.addRow(new Object[] { date, time, mode, duration, type, completed });
        }
    }

    /**
     * Updates the statistics display.
     */
    private void updateStatistics() {
        long totalMinutes = statisticsManager.getTotalFocusTime() / 60;
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) (totalMinutes % 60);

        totalTimeLabel.setText("Total: " + hours + "h " + minutes + "m");
        avgSessionLabel.setText("Avg: " + statisticsManager.getAverageSessionLength() + "m");
        completionRateLabel.setText("Rate: " + statisticsManager.getCompletionRate() + "%");
        streakLabel.setText("Streak: " + statisticsManager.getCurrentStreak() + " days");
    }

    @Override
    public void update(Event event) {
        SwingUtilities.invokeLater(() -> {
            if (event.getType() == Event.EventType.SESSION_COMPLETED ||
                    event.getType() == Event.EventType.DATA_LOADED) {
                refreshData();
            }
        });
    }

    public SessionLogger getSessionLogger() {
        return sessionLogger;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
}

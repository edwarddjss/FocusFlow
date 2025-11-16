package com.focusflow.view.planner;

import com.focusflow.controller.planner.ImportFileListener;
import com.focusflow.model.planner.Planner;
import com.focusflow.model.coach.AIPlanner;
import com.focusflow.model.coach.StorageHandler;
import com.focusflow.view.coach.ChatPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Panel for planning and scheduling study sessions.
 * Supports .ics calendar import and AI coaching.
 *
 * @author Gianluca Binetti
 */
public class PlannerPanel extends JPanel {

    private CalendarView calendarView;
    private JButton importButton;
    private JButton exportButton;
    private Planner planner;

    private ChatPanel chatPanel;
    private AIPlanner aiPlanner;
    private StorageHandler storageHandler;

    /**
     * Creates a new PlannerPanel.
     */
    public PlannerPanel() {
        planner = new Planner();
        storageHandler = new StorageHandler();
        aiPlanner = new AIPlanner(storageHandler);
        initializeUI();
        initializeListeners();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel toolbarPanel = createToolbarPanel();
        add(toolbarPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);

        calendarView = new CalendarView();
        splitPane.setLeftComponent(new JScrollPane(calendarView));

        chatPanel = new ChatPanel(aiPlanner, planner);
        chatPanel.setPreferredSize(new Dimension(300, 0));
        splitPane.setRightComponent(chatPanel);

        add(splitPane, BorderLayout.CENTER);

        calendarView.setPlanner(planner);
    }

    /**
     * Creates the toolbar panel.
     */
    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        importButton = new JButton("Import (.ics)");
        exportButton = new JButton("Export");
        JButton weekButton = new JButton("Week");
        JButton monthButton = new JButton("Month");

        panel.add(importButton);
        panel.add(exportButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(weekButton);
        panel.add(monthButton);

        weekButton.addActionListener(e -> calendarView.showWeek(new java.util.Date()));
        monthButton.addActionListener(e -> calendarView.showMonth(new java.util.Date()));

        return panel;
    }

    /**
     * Initializes event listeners.
     */
    private void initializeListeners() {
        importButton.addActionListener(new ImportFileListener(this));
        exportButton.addActionListener(e -> openExportDialog());
    }

    /**
     * Opens the export dialog.
     */
    public void openExportDialog() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        FileDialog fileDialog = new FileDialog(parentFrame, "Export Schedule", FileDialog.SAVE);
        fileDialog.setFile("schedule.ics");
        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();
        if (directory != null && filename != null) {
            File file = new File(directory, filename);
            if (!file.getName().toLowerCase().endsWith(".ics")) {
                file = new File(file.getAbsolutePath() + ".ics");
            }
            exportSchedule(file);
        }
    }

    /**
     * Imports a calendar file.
     */
    private void importCalendarFile(File file) {
        try {
            int countBefore = planner.getAllEvents().size();
            planner.importFromICS(file);
            int imported = planner.getAllEvents().size() - countBefore;

            calendarView.refresh();

            JOptionPane.showMessageDialog(this, "Imported " + imported + " events!",
                    "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to import: " + e.getMessage(),
                    "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Exports the current schedule.
     */
    private void exportSchedule(File file) {
        try {
            planner.exportToICS(file);
            JOptionPane.showMessageDialog(this, "Schedule exported!",
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public CalendarView getCalendarView() {
        return calendarView;
    }

    public Planner getPlanner() {
        return planner;
    }

    public AIPlanner getAIPlanner() {
        return aiPlanner;
    }
}

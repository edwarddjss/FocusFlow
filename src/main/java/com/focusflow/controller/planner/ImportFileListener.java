package com.focusflow.controller.planner;

import com.focusflow.model.planner.Planner;
import com.focusflow.view.planner.PlannerPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener for importing calendar files.
 * Handle .ics file parsing and import events to planner.
 *
 * @author Gianluca Binetti
 */
public class ImportFileListener implements ActionListener {

    private final Planner planner;

    /**
     * Create new ImportFileListener.
     *
     * @param planner planner to import events to
     */
    public ImportFileListener(Planner planner) {
        this.planner = planner;
    }

    /**
     * Create new ImportFileListener from PlannerPanel.
     *
     * @param plannerPanel the planner panel
     */
    public ImportFileListener(PlannerPanel plannerPanel) {
        this.planner = plannerPanel.getPlanner();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        importFile();
    }

    /**
     * Open file chooser and import selected .ics file.
     */
    public void importFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Calendar File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("iCalendar Files (*.ics)", "ics"));

        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            Object[] options = { "Append", "Replace" };
            int choice = JOptionPane.showOptionDialog(null,
                    "Do you want to append to the existing schedule or replace it?",
                    "Import Options",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 1) { // Replace
                planner.clearEvents();
            }

            loadFromFile(file);
        }
    }

    /**
     * Load and parse .ics file.
     *
     * @param file file to load
     */
    private void loadFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Map<String, String>> events = parseICSFile(reader);
            importEvents(events);
            JOptionPane.showMessageDialog(null,
                    "Successfully imported " + events.size() + " events.",
                    "Import Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error reading file: " + e.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Parse ICS file and extract VEVENT components.
     *
     * @param reader file reader
     * @return list of event data maps
     * @throws IOException if reading fails
     */
    private List<Map<String, String>> parseICSFile(BufferedReader reader) throws IOException {
        List<Map<String, String>> events = new ArrayList<>();
        Map<String, String> currentEvent = null;
        String line;
        StringBuilder multiLineValue = new StringBuilder();
        String currentKey = null;

        // Read file line by line and parse ICS format
        while ((line = reader.readLine()) != null) {
            // Handle line continuations (lines starting with space or tab)
            if (line.startsWith(" ") || line.startsWith("\t")) {
                if (currentKey != null) {
                    multiLineValue.append(line.substring(1));
                }
                continue;
            }

            // Save previous multi-line value
            if (currentKey != null && currentEvent != null) {
                currentEvent.put(currentKey, multiLineValue.toString());
            }

            if (line.equals("BEGIN:VEVENT")) {
                currentEvent = new HashMap<>();
            } else if (line.equals("END:VEVENT")) {
                if (currentEvent != null) {
                    events.add(currentEvent);
                    currentEvent = null;
                }
            } else if (currentEvent != null && line.contains(":")) {
                int colonIndex = line.indexOf(":");
                currentKey = line.substring(0, colonIndex);
                // Remove any parameters (e.g., DTSTART;VALUE=DATE:20240101)
                if (currentKey.contains(";")) {
                    currentKey = currentKey.substring(0, currentKey.indexOf(";"));
                }
                multiLineValue = new StringBuilder(line.substring(colonIndex + 1));
            }
        }

        return events;
    }

    /**
     * Import parsed events into planner.
     *
     * @param events events to import
     */
    private void importEvents(List<Map<String, String>> events) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (Map<String, String> event : events) {
            String summary = event.getOrDefault("SUMMARY", "Untitled Event");
            String description = event.getOrDefault("DESCRIPTION", "");
            String dtStart = event.get("DTSTART");
            String dtEnd = event.get("DTEND");

            LocalDateTime startTime = parseDateTime(dtStart, formatter, dateOnlyFormatter);
            LocalDateTime endTime = parseDateTime(dtEnd, formatter, dateOnlyFormatter);

            if (startTime != null) {
                planner.addEvent(summary, description, startTime, endTime);
            }
        }
    }

    /**
     * Parse date/time string from ICS format.
     *
     * @param dateStr        date string
     * @param dateTimeFormat formatter for datetime values
     * @param dateOnlyFormat formatter for date-only values
     * @return parsed LocalDateTime, or null if parsing fails
     */
    private LocalDateTime parseDateTime(String dateStr, DateTimeFormatter dateTimeFormat,
            DateTimeFormatter dateOnlyFormat) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            // Remove timezone suffix if present
            dateStr = dateStr.replace("Z", "");

            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr, dateTimeFormat);
            } else {
                // Date only - assume start of day
                return LocalDateTime.parse(dateStr + "T000000", dateTimeFormat);
            }
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr, dateOnlyFormat);
            } catch (Exception e2) {
                System.err.println("Failed to parse date: " + dateStr);
                return null;
            }
        }
    }
}

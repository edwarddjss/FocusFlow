package com.focusflow.controller.history;

import com.focusflow.model.session.SessionLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Listener for date filter changes in history view.
 *
 * @author Frank Watkins
 */
public class DateFilterListener implements ActionListener {

    private final SessionLogger sessionLogger;
    private LocalDate startDate;
    private LocalDate endDate;

    public DateFilterListener(SessionLogger sessionLogger) {
        this.sessionLogger = sessionLogger;
        this.startDate = LocalDate.now().minusDays(7);
        this.endDate = LocalDate.now();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        applyDateFilter(command);
    }

    /**
     * Applies a date filter based on the filter type.
     */
    public void applyDateFilter(String filterType) {
        LocalDate today = LocalDate.now();

        switch (filterType) {
            case "Today":
                startDate = today;
                endDate = today;
                break;
            case "This Week":
                startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
                endDate = today;
                break;
            case "This Month":
                startDate = today.withDayOfMonth(1);
                endDate = today;
                break;
            case "Last 30 Days":
                startDate = today.minusDays(30);
                endDate = today;
                break;
            case "All Time":
                startDate = LocalDate.of(2000, 1, 1);
                endDate = today;
                break;
            default:
                parseCustomDateRange(filterType);
        }

        sessionLogger.filterByDateRange(startDate, endDate);
    }

    private void parseCustomDateRange(String dateRange) {
        try {
            String[] parts = dateRange.split(" to ");
            if (parts.length == 2) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                startDate = LocalDate.parse(parts[0].trim(), formatter);
                endDate = LocalDate.parse(parts[1].trim(), formatter);
            }
        } catch (DateTimeParseException e) {
            // Keep existing range if parsing fails
        }
    }

}

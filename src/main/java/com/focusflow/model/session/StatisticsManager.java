package com.focusflow.model.session;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages and calculates session statistics for insights and reports.
 *
 * @author Frank Watkins
 */
public class StatisticsManager {

    private final SessionLogger sessionLogger;

    /**
     * Creates manager with given session logger.
     */
    public StatisticsManager(SessionLogger sessionLogger) {
        this.sessionLogger = sessionLogger;
    }

    /**
     * @return total number of sessions
     */
    public int getTotalSessions() {
        return sessionLogger.getSessionCount();
    }

    /**
     * @return total focus time in minutes
     */
    public int getTotalFocusMinutes() {
        return sessionLogger.getTotalFocusMinutes();
    }

    /**
     * @return total focus time in seconds
     */
    public long getTotalFocusTime() {
        return getTotalFocusMinutes() * 60L;
    }

    public void calculateStatistics() {
        // stats calculated on demand
    }

    public int getAverageSessionLength() {
        return (int) Math.round(getAverageSessionDuration());
    }

    /**
     * @return percentage of sessions completed
     */
    public double getCompletionRate() {
        int total = getTotalSessions();
        if (total == 0) return 0;
        return (sessionLogger.getCompletedSessionCount() * 100.0) / total;
    }

    /**
     * @return average session length in minutes
     */
    public double getAverageSessionDuration() {
        List<SessionRecord> sessions = sessionLogger.getAllSessions();
        if (sessions.isEmpty()) return 0;
        
        int total = 0;
        for (SessionRecord session : sessions) {
            total += session.getDurationMinutes();
        }
        return (double) total / sessions.size();
    }

    /**
     * @return current consecutive days with sessions
     */
    public int getCurrentStreak() {
        List<SessionRecord> sessions = sessionLogger.getAllSessions();
        if (sessions.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        while (hasSessionOnDate(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    private boolean hasSessionOnDate(LocalDate date) {
        for (SessionRecord session : sessionLogger.getAllSessions()) {
            if (session.isCompleted()) {
                if (session.getStartTime().toLocalDate().equals(date)) {
                    return true;
                }
            }
        }
        return false;
    }
}

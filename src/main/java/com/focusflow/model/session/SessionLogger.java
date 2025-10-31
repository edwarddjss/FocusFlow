package com.focusflow.model.session;

import com.focusflow.model.coach.StorageHandler;
import com.focusflow.observer.Event;
import com.focusflow.observer.Observer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs and manages session records with filtering and iteration capabilities.
 * Singleton to ensure all panels share the same session data.
 *
 * @author Frank Watkins
 */
public class SessionLogger implements Observer {

    private static SessionLogger instance;

    private final List<SessionRecord> allSessions;
    private List<SessionRecord> filteredSessions;
    private final StorageHandler storageHandler;
    private final List<Observer> observers;

    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    private String filterMode;

    /**
     * Gets the single instance.
     */
    public static synchronized SessionLogger getInstance() {
        if (instance == null) {
            instance = new SessionLogger();
        }
        return instance;
    }

    private SessionLogger() {
        this.allSessions = new ArrayList<>();
        this.filteredSessions = new ArrayList<>();
        this.storageHandler = new StorageHandler();
        this.observers = new ArrayList<>();
        this.filterMode = null;
        loadSessions();
    }

    /**
     * Adds an observer for session events.
     */
    public void addObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Logs a completed session and saves to storage.
     */
    public void logSession(SessionRecord session) {
        allSessions.add(session);
        applyFilters();
        saveSessions();
        notifyObservers();
    }

    private void notifyObservers() {
        Event event = new Event(Event.EventType.SESSION_COMPLETED, null);
        for (Observer observer : observers) {
            observer.update(event);
        }
    }

    /**
     * @return copy of all sessions
     */
    public List<SessionRecord> getAllSessions() {
        return new ArrayList<>(allSessions);
    }

    /**
     * @return copy of filtered sessions
     */
    public List<SessionRecord> getFilteredSessions() {
        return new ArrayList<>(filteredSessions);
    }

    /**
     * Creates iterator for traversing filtered sessions.
     */
    public SessionIterator createIterator() {
        return new SessionIterator(filteredSessions);
    }

    /**
     * Filters sessions by date range.
     */
    public void filterByDateRange(LocalDate startDate, LocalDate endDate) {
        this.filterStartDate = startDate;
        this.filterEndDate = endDate;
        applyFilters();
    }

    /**
     * Filters sessions by timer mode.
     */
    public void filterByMode(String mode) {
        this.filterMode = mode;
        applyFilters();
    }

    /**
     * Clears the mode filter.
     */
    public void clearModeFilter() {
        this.filterMode = null;
        applyFilters();
    }

    /**
     * Clears all active filters.
     */
    public void clearFilters() {
        this.filterStartDate = null;
        this.filterEndDate = null;
        this.filterMode = null;
        this.filteredSessions = new ArrayList<>(allSessions);
    }

    private void applyFilters() {
        filteredSessions = new ArrayList<>();
        for (SessionRecord session : allSessions) {
            if (matchesDateFilter(session) && matchesModeFilter(session)) {
                filteredSessions.add(session);
            }
        }
    }

    private boolean matchesDateFilter(SessionRecord session) {
        if (filterStartDate == null || filterEndDate == null) {
            return true;
        }
        LocalDate sessionDate = session.getStartTime().toLocalDate();
        return !sessionDate.isBefore(filterStartDate) && !sessionDate.isAfter(filterEndDate);
    }

    private boolean matchesModeFilter(SessionRecord session) {
        if (filterMode == null) {
            return true;
        }
        return session.getModeName().equalsIgnoreCase(filterMode);
    }

    public int getSessionCount() {
        return allSessions.size();
    }

    public int getCompletedSessionCount() {
        int count = 0;
        for (SessionRecord session : allSessions) {
            if (session.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalFocusMinutes() {
        int total = 0;
        for (SessionRecord session : allSessions) {
            if (session.isCompleted()) {
                total += session.getDurationMinutes();
            }
        }
        return total;
    }

    private void saveSessions() {
        storageHandler.saveSessions(allSessions);
    }

    private void loadSessions() {
        Type listType = new TypeToken<List<SessionRecord>>(){}.getType();
        List<SessionRecord> loaded = storageHandler.loadSessions(listType);
        if (loaded != null) {
            allSessions.addAll(loaded);
            filteredSessions = new ArrayList<>(allSessions);
        }
    }

    @Override
    public void update(Event event) {
        if (event.getType() == Event.EventType.SESSION_COMPLETED) {
            if (event.getData() instanceof SessionRecord) {
                logSession((SessionRecord) event.getData());
            }
        }
    }
}

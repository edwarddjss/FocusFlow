package com.focusflow.model.planner;

import com.focusflow.model.coach.StorageHandler;
import com.focusflow.observer.Event;
import com.focusflow.observer.Observer;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages calendar events and study planning.
 *
 * @author Gianluca Binetti
 */
public class Planner {

    private final List<PlannerEvent> events;
    private final List<Observer> observers;
    private final StorageHandler storageHandler;

    public Planner() {
        this.events = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.storageHandler = new StorageHandler();
        loadEvents();
    }

    /**
     * Checks if a time slot conflicts with existing events.
     * @return true if there is a conflict
     */
    public boolean hasConflict(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return false;
        for (PlannerEvent existing : events) {
            if (existing.getStartTime() == null || existing.getEndTime() == null) continue;
            boolean overlaps = startTime.isBefore(existing.getEndTime()) && endTime.isAfter(existing.getStartTime());
            if (overlaps) return true;
        }
        return false;
    }

    /**
     * Adds an event to the planner if no conflict exists.
     * @return true if added successfully, false if conflict
     */
    public boolean addEvent(String title, String description, LocalDateTime startTime, LocalDateTime endTime) {
        if (hasConflict(startTime, endTime)) {
            return false;
        }
        PlannerEvent event = new PlannerEvent(title, description, startTime, endTime);
        events.add(event);
        saveEvents();
        notifyObservers(new Event(Event.EventType.DATA_LOADED, event));
        return true;
    }

    /**
     * Adds a study event with timer mode info.
     * @return true if added, false if conflict
     */
    public boolean addStudyEvent(String title, String description, LocalDateTime startTime, LocalDateTime endTime,
            String mode) {
        if (hasConflict(startTime, endTime)) {
            return false;
        }
        PlannerEvent event = new PlannerEvent(title, description, startTime, endTime);
        event.setStudyBlock(true);
        event.setTimerMode(mode);
        events.add(event);
        saveEvents();
        notifyObservers(new Event(Event.EventType.DATA_LOADED, event));
        return true;
    }

    /**
     * Removes an event by ID.
     * @return true if event was found and removed
     */
    public boolean removeEvent(String eventId) {
        PlannerEvent toRemove = null;
        for (PlannerEvent e : events) {
            if (e.getId().equals(eventId)) {
                toRemove = e;
                break;
            }
        }
        if (toRemove != null) {
            events.remove(toRemove);
            saveEvents();
            notifyObservers(new Event(Event.EventType.DATA_LOADED, null));
            return true;
        }
        return false;
    }

    /**
     * Removes all events from the planner.
     */
    public void clearEvents() {
        events.clear();
        saveEvents();
        notifyObservers(new Event(Event.EventType.DATA_LOADED, null));
    }

    /**
     * @return copy of all events
     */
    public List<PlannerEvent> getAllEvents() {
        return new ArrayList<>(events);
    }

    /**
     * Gets events for a specific date.
     */
    public List<PlannerEvent> getEventsForDate(LocalDate date) {
        List<PlannerEvent> result = new ArrayList<>();
        for (PlannerEvent e : events) {
            if (e.getStartTime().toLocalDate().equals(date)) {
                result.add(e);
            }
        }
        return result;
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }

    private void saveEvents() {
        storageHandler.save("planner_events.json", events);
    }

    private void loadEvents() {
        Type listType = new TypeToken<List<PlannerEvent>>() {
        }.getType();
        List<PlannerEvent> loaded = storageHandler.load("planner_events.json", listType);
        if (loaded != null) {
            events.addAll(loaded);
        }
    }

    /**
     * Imports events from an ICS file.
     */
    public void importFromICS(File file) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String title = null;
            String description = null;
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            boolean inEvent = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("BEGIN:VEVENT")) {
                    inEvent = true;
                    title = null;
                    description = null;
                    startTime = null;
                    endTime = null;
                } else if (line.equals("END:VEVENT") && inEvent) {
                    if (title != null && startTime != null) {
                        addEvent(title, description != null ? description : "", startTime, endTime);
                    }
                    inEvent = false;
                } else if (inEvent) {
                    if (line.startsWith("SUMMARY:")) {
                        title = line.substring(8);
                    } else if (line.startsWith("DESCRIPTION:")) {
                        description = line.substring(12);
                    } else if (line.startsWith("DTSTART")) {
                        String dateStr = line.substring(line.indexOf(":") + 1).replace("Z", "");
                        try {
                            startTime = LocalDateTime.parse(dateStr, formatter);
                        } catch (Exception e) {
                            startTime = LocalDate.parse(dateStr.substring(0, 8),
                                    DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
                        }
                    } else if (line.startsWith("DTEND")) {
                        String dateStr = line.substring(line.indexOf(":") + 1).replace("Z", "");
                        try {
                            endTime = LocalDateTime.parse(dateStr, formatter);
                        } catch (Exception e) {
                            endTime = LocalDate.parse(dateStr.substring(0, 8),
                                    DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
                        }
                    }
                }
            }
        }
    }

    /**
     * Exports events to an ICS file.
     */
    public void exportToICS(File file) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("BEGIN:VCALENDAR\n");
            writer.write("VERSION:2.0\n");
            writer.write("PRODID:-//FocusFlow//Study Planner//EN\n");

            for (PlannerEvent event : events) {
                writer.write("BEGIN:VEVENT\n");
                writer.write("UID:" + event.getId() + "\n");
                writer.write("SUMMARY:" + event.getTitle() + "\n");
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    writer.write("DESCRIPTION:" + event.getDescription() + "\n");
                }
                if (event.getStartTime() != null) {
                    writer.write("DTSTART:" + event.getStartTime().format(formatter) + "\n");
                }
                if (event.getEndTime() != null) {
                    writer.write("DTEND:" + event.getEndTime().format(formatter) + "\n");
                }
                writer.write("END:VEVENT\n");
            }

            writer.write("END:VCALENDAR\n");
        }
    }

    /**
     * Inner class for calendar events.
     */
    public static class PlannerEvent {
        private final String id;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String timerMode;
        private boolean isStudyBlock;

        public PlannerEvent(String title, String description, LocalDateTime startTime, LocalDateTime endTime) {
            this.id = UUID.randomUUID().toString();
            this.title = title;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
            this.isStudyBlock = false;
            this.timerMode = "POMODORO";
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public String getTimerMode() { return timerMode; }
        public boolean isStudyBlock() { return isStudyBlock; }

        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public void setTimerMode(String timerMode) { this.timerMode = timerMode; }
        public void setStudyBlock(boolean studyBlock) { isStudyBlock = studyBlock; }
    }
}

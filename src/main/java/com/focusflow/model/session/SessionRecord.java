package com.focusflow.model.session;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single focus session record.
 *
 * @author Frank Watkins
 */
public class SessionRecord {

    private final String id;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private final String modeName;
    private final int plannedDurationMinutes;
    private int actualDurationMinutes;
    private boolean completed;

    /**
     * Creates a new session record.
     */
    public SessionRecord(String modeName, int plannedDurationMinutes) {
        this.id = UUID.randomUUID().toString();
        this.startTime = LocalDateTime.now();
        this.modeName = modeName;
        this.plannedDurationMinutes = plannedDurationMinutes;
        this.actualDurationMinutes = 0;
        this.completed = false;
    }

    /**
     * Marks the session as completed.
     */
    public void complete() {
        this.endTime = LocalDateTime.now();
        this.completed = true;
        calculateActualDuration();
    }

    private void calculateActualDuration() {
        if (endTime != null) {
            long seconds = java.time.Duration.between(startTime, endTime).getSeconds();
            this.actualDurationMinutes = (int) (seconds / 60);
        }
    }

    public String getId() { return id; }
    public LocalDateTime getStartTime() { return startTime; }
    public String getModeName() { return modeName; }
    public boolean isCompleted() { return completed; }

    /**
     * @return actual duration if available, otherwise planned
     */
    public int getDurationMinutes() {
        return actualDurationMinutes > 0 ? actualDurationMinutes : plannedDurationMinutes;
    }

    @Override
    public String toString() {
        return String.format("SessionRecord[id=%s, mode=%s, duration=%dmin, completed=%b]",
                id, modeName, getDurationMinutes(), completed);
    }
}

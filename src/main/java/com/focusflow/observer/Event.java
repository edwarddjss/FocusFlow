package com.focusflow.observer;

/**
 * Event class for the Observer pattern.
 *
 * @author Fareed Uddin
 */
public class Event {

    public enum EventType {
        TIMER_STARTED,
        TIMER_PAUSED,
        TIMER_RESET,
        TIMER_TICK,
        TIMER_COMPLETED,
        SESSION_COMPLETED,
        MODE_CHANGED,
        SETTINGS_CHANGED,
        DATA_LOADED
    }

    private final EventType type;
    private final Object data;
    private final long timestamp;

    public Event(EventType type) {
        this(type, null);
    }

    public Event(EventType type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public EventType getType() { return type; }
    public Object getData() { return data; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Event{type=" + type + ", data=" + data + ", timestamp=" + timestamp + "}";
    }
}

package com.focusflow.model.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SessionRecord.
 *
 * @author Frank Watkins
 */
class SessionRecordTest {

    @Test
    void testCreateRecord() {
        SessionRecord record = new SessionRecord("Pomodoro", 25);
        assertNotNull(record);
        assertEquals("Pomodoro", record.getModeName());
    }

    @Test
    void testRecordNotCompletedByDefault() {
        SessionRecord record = new SessionRecord("Pomodoro", 25);
        assertFalse(record.isCompleted());
    }

    @Test
    void testCompleteRecord() {
        SessionRecord record = new SessionRecord("Pomodoro", 25);
        record.complete();
        assertTrue(record.isCompleted());
    }

    @Test
    void testStartTime() {
        SessionRecord record = new SessionRecord("Pomodoro", 25);
        assertNotNull(record.getStartTime());
    }

    @Test
    void testDifferentModes() {
        SessionRecord pomodoro = new SessionRecord("Pomodoro", 25);
        SessionRecord ultradian = new SessionRecord("Ultradian", 90);

        assertEquals("Pomodoro", pomodoro.getModeName());
        assertEquals("Ultradian", ultradian.getModeName());
    }

    @Test
    void testGetId() {
        SessionRecord record = new SessionRecord("Pomodoro", 25);
        assertNotNull(record.getId());
        assertFalse(record.getId().isEmpty());
    }
}

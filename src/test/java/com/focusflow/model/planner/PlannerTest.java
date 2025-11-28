package com.focusflow.model.planner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Planner.
 *
 * @author Gianluca Binetti
 */
class PlannerTest {

    private Planner planner;

    @BeforeEach
    void setUp() {
        planner = new Planner();
        planner.clearEvents();
    }

    @Test
    void testAddEvent() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);
        boolean added = planner.addEvent("Study Session", "Math homework", start, end);
        assertTrue(added);
    }

    @Test
    void testGetAllEvents() {
        List<Planner.PlannerEvent> events = planner.getAllEvents();
        assertNotNull(events);
    }

    @Test
    void testRemoveEvent() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = start.plusHours(1);
        planner.addEvent("Test Event", "Description", start, end);

        List<Planner.PlannerEvent> events = planner.getAllEvents();
        if (!events.isEmpty()) {
            String eventId = events.get(0).getId();
            boolean removed = planner.removeEvent(eventId);
            assertTrue(removed);
        }
    }

    @Test
    void testClearEvents() {
        LocalDateTime start = LocalDateTime.now().plusHours(3);
        LocalDateTime end = start.plusHours(1);
        planner.addEvent("Event 1", "Desc", start, end);
        planner.clearEvents();
        assertEquals(0, planner.getAllEvents().size());
    }

    @Test
    void testConflictDetection() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        planner.addEvent("First Event", "Desc", start, end);

        // Try to add overlapping event
        boolean conflict = planner.hasConflict(start.plusMinutes(30), end.minusMinutes(30));
        assertTrue(conflict);
    }

    @Test
    void testAddStudyEvent() {
        LocalDateTime start = LocalDateTime.now().plusHours(4);
        LocalDateTime end = start.plusHours(1);
        boolean added = planner.addStudyEvent("Study", "Desc", start, end, "POMODORO");
        assertTrue(added);
    }
}

package com.focusflow.model.timer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TimerMode implementations.
 *
 * @author Edward De Jesus
 */
class TimerModeTest {

    @Test
    void testPomodoroMode() {
        TimerMode mode = new PomodoroMode();
        assertEquals("Pomodoro", mode.getName());
        assertEquals(25 * 60, mode.getWorkDuration());
        assertEquals(5 * 60, mode.getBreakDuration());
    }

    @Test
    void testFiftyTwoSeventeenMode() {
        TimerMode mode = new FiftyTwoSeventeenMode();
        assertEquals("52/17", mode.getName());
        assertEquals(52 * 60, mode.getWorkDuration());
        assertEquals(17 * 60, mode.getBreakDuration());
    }

    @Test
    void testUltradianMode() {
        TimerMode mode = new UltradianMode();
        assertEquals("Ultradian", mode.getName());
        assertEquals(90 * 60, mode.getWorkDuration());
        assertEquals(20 * 60, mode.getBreakDuration());
    }

    @Test
    void testCustomMode() {
        TimerMode mode = new CustomMode(30, 10);
        assertEquals("Custom", mode.getName());
        assertEquals(30 * 60, mode.getWorkDuration());
        assertEquals(10 * 60, mode.getBreakDuration());
    }

    @Test
    void testAllModesHaveDescriptions() {
        TimerMode[] modes = {
            new PomodoroMode(),
            new FiftyTwoSeventeenMode(),
            new UltradianMode(),
            new CustomMode(25, 5)
        };
        for (TimerMode mode : modes) {
            assertNotNull(mode.getDescription());
            assertFalse(mode.getDescription().isEmpty());
        }
    }
}

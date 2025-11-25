package com.focusflow.model.timer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TimerManager.
 *
 * @author Edward De Jesus
 */
class TimerManagerTest {

    private TimerManager timerManager;

    @BeforeEach
    void setUp() {
        TimerManager.resetInstance();
        timerManager = TimerManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        TimerManager.resetInstance();
    }

    @Test
    void testSingletonInstance() {
        TimerManager another = TimerManager.getInstance();
        assertSame(timerManager, another);
    }

    @Test
    void testStartSession() {
        assertFalse(timerManager.isRunning());
        timerManager.startSession();
        assertTrue(timerManager.isRunning());
    }

    @Test
    void testPauseSession() {
        timerManager.startSession();
        assertTrue(timerManager.isRunning());
        timerManager.pauseSession();
        assertFalse(timerManager.isRunning());
    }

    @Test
    void testResetSession() {
        timerManager.startSession();
        timerManager.resetSession();
        assertFalse(timerManager.isRunning());
        assertTrue(timerManager.isWorkPhase());
    }

    @Test
    void testSkipToNext() {
        boolean wasWorkPhase = timerManager.isWorkPhase();
        timerManager.skipToNext();
        assertNotEquals(wasWorkPhase, timerManager.isWorkPhase());
    }

    @Test
    void testDefaultMode() {
        assertNotNull(timerManager.getCurrentMode());
        assertEquals("Pomodoro", timerManager.getCurrentMode().getName());
    }

    @Test
    void testSetTimerMode() {
        TimerMode newMode = new FiftyTwoSeventeenMode();
        timerManager.setTimerMode(newMode);
        assertEquals("52/17", timerManager.getCurrentMode().getName());
    }
}

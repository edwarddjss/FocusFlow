package com.focusflow.command;

import com.focusflow.model.timer.TimerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Command pattern implementation.
 *
 * @author Edward De Jesus
 */
class CommandPatternTest {

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
    void testStartCommand() {
        Command cmd = new StartCommand(timerManager);
        assertEquals("Start", cmd.getName());
        assertFalse(timerManager.isRunning());
        cmd.execute();
        assertTrue(timerManager.isRunning());
    }

    @Test
    void testPauseCommand() {
        Command cmd = new PauseCommand(timerManager);
        assertEquals("Pause", cmd.getName());
        timerManager.startSession();
        assertTrue(timerManager.isRunning());
        cmd.execute();
        assertFalse(timerManager.isRunning());
    }

    @Test
    void testResetCommand() {
        Command cmd = new ResetCommand(timerManager);
        assertEquals("Reset", cmd.getName());
        timerManager.startSession();
        cmd.execute();
        assertFalse(timerManager.isRunning());
    }

    @Test
    void testSkipCommand() {
        Command cmd = new SkipCommand(timerManager);
        assertEquals("Skip", cmd.getName());
        boolean wasWorkPhase = timerManager.isWorkPhase();
        cmd.execute();
        assertNotEquals(wasWorkPhase, timerManager.isWorkPhase());
    }

    @Test
    void testCommandInterface() {
        Command[] commands = {
            new StartCommand(timerManager),
            new PauseCommand(timerManager),
            new ResetCommand(timerManager),
            new SkipCommand(timerManager)
        };

        for (Command cmd : commands) {
            assertNotNull(cmd.getName());
            assertFalse(cmd.getName().isEmpty());
        }
    }
}

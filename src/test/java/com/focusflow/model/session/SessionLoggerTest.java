package com.focusflow.model.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SessionLogger.
 *
 * @author Frank Watkins
 */
class SessionLoggerTest {

    private SessionLogger logger;

    @BeforeEach
    void setUp() {
        logger = SessionLogger.getInstance();
        logger.clearFilters();
    }

    @Test
    void testSingletonInstance() {
        SessionLogger another = SessionLogger.getInstance();
        assertSame(logger, another);
    }

    @Test
    void testLogSession() {
        int initialCount = logger.getSessionCount();
        SessionRecord record = new SessionRecord("Pomodoro", 25);
        record.complete();
        logger.logSession(record);

        assertTrue(logger.getSessionCount() > initialCount);
    }

    @Test
    void testGetAllSessions() {
        List<SessionRecord> sessions = logger.getAllSessions();
        assertNotNull(sessions);
    }

    @Test
    void testCreateIterator() {
        SessionIterator iterator = logger.createIterator();
        assertNotNull(iterator);
    }

    @Test
    void testGetTotalFocusMinutes() {
        int total = logger.getTotalFocusMinutes();
        assertTrue(total >= 0);
    }
}

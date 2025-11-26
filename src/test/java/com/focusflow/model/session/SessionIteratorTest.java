package com.focusflow.model.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SessionIterator (Iterator pattern).
 *
 * @author Frank Watkins
 */
class SessionIteratorTest {

    private List<SessionRecord> sessions;
    private SessionIterator iterator;

    @BeforeEach
    void setUp() {
        sessions = new ArrayList<>();
        sessions.add(new SessionRecord("Pomodoro", 25));
        sessions.add(new SessionRecord("52/17", 52));
        sessions.add(new SessionRecord("Ultradian", 90));
        iterator = new SessionIterator(sessions);
    }

    @Test
    void testHasNextWithElements() {
        assertTrue(iterator.hasNext());
    }

    @Test
    void testHasNextEmptyList() {
        SessionIterator emptyIterator = new SessionIterator(new ArrayList<>());
        assertFalse(emptyIterator.hasNext());
    }

    @Test
    void testNextReturnsElements() {
        SessionRecord first = iterator.next();
        assertEquals("Pomodoro", first.getModeName());

        SessionRecord second = iterator.next();
        assertEquals("52/17", second.getModeName());
    }

    @Test
    void testNextThrowsWhenEmpty() {
        iterator.next();
        iterator.next();
        iterator.next();
        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    void testReset() {
        iterator.next();
        iterator.next();
        iterator.reset();
        assertEquals(0, iterator.getCurrentIndex());
        assertTrue(iterator.hasNext());
    }

    @Test
    void testGetTotalCount() {
        assertEquals(3, iterator.getTotalCount());
    }

    @Test
    void testSkip() {
        iterator.skip(2);
        assertEquals(2, iterator.getCurrentIndex());
        SessionRecord third = iterator.next();
        assertEquals("Ultradian", third.getModeName());
    }
}

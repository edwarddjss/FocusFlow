package com.focusflow.observer;

import com.focusflow.model.timer.TimerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Observer pattern implementation.
 *
 * @author Frank Watkins
 */
class ObserverPatternTest {

    private TimerManager subject;
    private TestObserver observer1;
    private TestObserver observer2;

    @BeforeEach
    void setUp() {
        TimerManager.resetInstance();
        subject = TimerManager.getInstance();
        observer1 = new TestObserver();
        observer2 = new TestObserver();
    }

    @AfterEach
    void tearDown() {
        TimerManager.resetInstance();
    }

    @Test
    void testAttachObserver() {
        subject.attach(observer1);
        subject.startSession();
        assertTrue(observer1.wasNotified());
    }

    @Test
    void testDetachObserver() {
        subject.attach(observer1);
        subject.detach(observer1);
        subject.startSession();
        assertFalse(observer1.wasNotified());
    }

    @Test
    void testMultipleObservers() {
        subject.attach(observer1);
        subject.attach(observer2);
        subject.startSession();
        assertTrue(observer1.wasNotified());
        assertTrue(observer2.wasNotified());
    }

    @Test
    void testEventType() {
        subject.attach(observer1);
        subject.startSession();
        assertEquals(Event.EventType.TIMER_STARTED, observer1.getLastEventType());
    }

    @Test
    void testEventCreation() {
        Event event = new Event(Event.EventType.TIMER_COMPLETED, "test data");
        assertEquals(Event.EventType.TIMER_COMPLETED, event.getType());
        assertEquals("test data", event.getData());
    }

    /**
     * Test observer for verifying notifications.
     */
    private static class TestObserver implements Observer {
        private boolean notified = false;
        private Event.EventType lastEventType = null;

        @Override
        public void update(Event event) {
            notified = true;
            lastEventType = event.getType();
        }

        public boolean wasNotified() {
            return notified;
        }

        public Event.EventType getLastEventType() {
            return lastEventType;
        }
    }
}

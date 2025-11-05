package com.focusflow.model.session;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator for traversing session records.
 *
 * @author Frank Watkins
 */
public class SessionIterator implements Iterator<SessionRecord> {

    private final List<SessionRecord> sessions;
    private int currentIndex;

    /**
     * Creates an iterator for the given sessions.
     */
    public SessionIterator(List<SessionRecord> sessions) {
        this.sessions = sessions;
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < sessions.size();
    }

    @Override
    public SessionRecord next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more sessions");
        }
        return sessions.get(currentIndex++);
    }

    /**
     * Resets the iterator to the beginning.
     */
    public void reset() {
        currentIndex = 0;
    }

    /**
     * @return the current position in the list
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * @return total number of sessions
     */
    public int getTotalCount() {
        return sessions.size();
    }

    /**
     * Skips ahead by the given count.
     */
    public void skip(int count) {
        currentIndex = Math.min(currentIndex + count, sessions.size());
    }
}

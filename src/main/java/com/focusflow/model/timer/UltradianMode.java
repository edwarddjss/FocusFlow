package com.focusflow.model.timer;

/**
 * Ultradian Rhythm timer mode.
 * 90 minutes of focused work followed by a 20 minute break.
 * Based on natural biological cycles that occur throughout the day.
 *
 * @author Edward De Jesus
 */
public class UltradianMode implements TimerMode {

    private static final String NAME = "Ultradian";
    private static final int WORK_DURATION_MINUTES = 90;
    private static final int BREAK_DURATION_MINUTES = 20;
    private static final String DESCRIPTION = "90-minute focus sessions with 20-minute breaks. " +
            "Aligned with natural ultradian rhythms for deep work.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getWorkDuration() {
        return WORK_DURATION_MINUTES * 60;
    }

    @Override
    public int getBreakDuration() {
        return BREAK_DURATION_MINUTES * 60;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String toString() {
        return NAME + " (" + WORK_DURATION_MINUTES + "/" + BREAK_DURATION_MINUTES + ")";
    }
}

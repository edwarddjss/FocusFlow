package com.focusflow.model.timer;

/**
 * Pomodoro Technique timer mode.
 * 25 minutes of focused work followed by a 5 minute break.
 * Developed by Francesco Cirillo in the late 1980s.
 *
 * @author Edward De Jesus
 */
public class PomodoroMode implements TimerMode {

    private static final String NAME = "Pomodoro";
    private static final int WORK_DURATION_MINUTES = 25;
    private static final int BREAK_DURATION_MINUTES = 5;
    private static final String DESCRIPTION = "25-minute focus sessions with 5-minute breaks. " +
            "Classic technique for maintaining concentration.";

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

package com.focusflow.model.timer;

/**
 * Custom timer mode with user-defined durations.
 *
 * @author Edward De Jesus
 */
public class CustomMode implements TimerMode {

    private static final String NAME = "Custom";
    private static final String DESCRIPTION = "Custom focus and break durations defined by the user.";

    private int workDurationMinutes;
    private int breakDurationMinutes;

    public CustomMode() {
        this(25, 5);
    }

    public CustomMode(int workDurationMinutes, int breakDurationMinutes) {
        setWorkDurationMinutes(workDurationMinutes);
        setBreakDurationMinutes(breakDurationMinutes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getWorkDuration() {
        return workDurationMinutes * 60;
    }

    @Override
    public int getBreakDuration() {
        return breakDurationMinutes * 60;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public int getWorkDurationMinutes() {
        return workDurationMinutes;
    }

    public void setWorkDurationMinutes(int workDurationMinutes) {
        if (workDurationMinutes <= 0) {
            throw new IllegalArgumentException("Work duration must be positive");
        }
        this.workDurationMinutes = workDurationMinutes;
    }

    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    public void setBreakDurationMinutes(int breakDurationMinutes) {
        if (breakDurationMinutes <= 0) {
            throw new IllegalArgumentException("Break duration must be positive");
        }
        this.breakDurationMinutes = breakDurationMinutes;
    }

    @Override
    public String toString() {
        return NAME + " (" + workDurationMinutes + "/" + breakDurationMinutes + ")";
    }
}

package com.focusflow.model.timer;

/**
 * 52/17 timer mode based on DeskTime research.
 * 52 minutes of focused work followed by a 17 minute break.
 * Research showed top performers work in 52-minute bursts.
 *
 * @author Edward De Jesus
 */
public class FiftyTwoSeventeenMode implements TimerMode {

    private static final String NAME = "52/17";
    private static final int WORK_DURATION_MINUTES = 52;
    private static final int BREAK_DURATION_MINUTES = 17;
    private static final String DESCRIPTION = "52-minute focus sessions with 17-minute breaks. " +
            "Based on DeskTime research on peak productivity patterns.";

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

package com.focusflow.command;

import com.focusflow.model.timer.TimerManager;

/**
 * Command to skip to the next timer phase.
 *
 * @author Edward De Jesus
 */
public class SkipCommand implements Command {

    private final TimerManager receiver;

    /**
     * Creates a skip command with the given receiver.
     */
    public SkipCommand(TimerManager receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        receiver.skipToNext();
    }

    @Override
    public String getName() {
        return "Skip";
    }
}

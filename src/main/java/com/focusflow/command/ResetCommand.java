package com.focusflow.command;

import com.focusflow.model.timer.TimerManager;

/**
 * Command to reset a timer session.
 *
 * @author Edward De Jesus
 */
public class ResetCommand implements Command {

    private final TimerManager receiver;

    /**
     * Creates a reset command with the given receiver.
     */
    public ResetCommand(TimerManager receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        receiver.resetSession();
    }

    @Override
    public String getName() {
        return "Reset";
    }
}

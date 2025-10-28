package com.focusflow.command;

import com.focusflow.model.timer.TimerManager;

/**
 * Command to pause a timer session.
 *
 * @author Edward De Jesus
 */
public class PauseCommand implements Command {

    private final TimerManager receiver;

    /**
     * Creates a pause command with the given receiver.
     */
    public PauseCommand(TimerManager receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        receiver.pauseSession();
    }

    @Override
    public String getName() {
        return "Pause";
    }
}

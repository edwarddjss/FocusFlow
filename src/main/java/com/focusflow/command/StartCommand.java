package com.focusflow.command;

import com.focusflow.model.timer.TimerManager;

/**
 * Command to start a timer session.
 *
 * @author Edward De Jesus
 */
public class StartCommand implements Command {

    private final TimerManager receiver;

    /**
     * Creates a start command with the given receiver.
     */
    public StartCommand(TimerManager receiver) {
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        // TODO: consider adding validation to check if timer is already running - Edward
        receiver.startSession();
    }

    @Override
    public String getName() {
        return "Start";
    }
}

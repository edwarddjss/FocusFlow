package com.focusflow.command;

/**
 * Command interface for the Command pattern.
 *
 * @author Edward De Jesus
 */
public interface Command {

    void execute();

    String getName();
}

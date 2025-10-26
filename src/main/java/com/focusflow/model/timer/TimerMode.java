package com.focusflow.model.timer;

/**
 * Strategy interface for different timer modes.
 *
 * @author Edward De Jesus
 */
public interface TimerMode {

    String getName();

    int getWorkDuration();

    int getBreakDuration();

    String getDescription();
}

package com.focusflow.model.timer;

import com.focusflow.observer.Event;
import com.focusflow.observer.Event.EventType;
import com.focusflow.observer.Observer;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the timer functionality using the Singleton pattern.
 *
 * @author Edward De Jesus
 */
public class TimerManager {

    private static TimerManager instance;

    private TimerMode currentStrategy;
    private List<Observer> observers;
    private boolean isRunning;
    private int elapsedTime;
    private int remainingTime;
    private boolean isWorkPhase;
    private Timer swingTimer;

    private TimerManager() {
        this.observers = new ArrayList<>();
        this.currentStrategy = new PomodoroMode();
        this.isRunning = false;
        this.elapsedTime = 0;
        this.isWorkPhase = true;
        this.remainingTime = currentStrategy.getWorkDuration();

        this.swingTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
    }

    /**
     * Gets the singleton instance.
     */
    public static synchronized TimerManager getInstance() {
        if (instance == null) {
            instance = new TimerManager();
        }
        return instance;
    }

    /**
     * Resets the singleton (mainly for testing).
     */
    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.swingTimer.stop();
            instance = null;
        }
    }

    /**
     * Attaches an observer to receive timer events.
     * @param observer the observer to attach
     */
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Detaches an observer.
     */
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notifies all attached observers of an event.
     */
    public void notify(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }

    /**
     * Starts the timer session.
     */
    public void startSession() {
        if (!isRunning) {
            isRunning = true;
            swingTimer.start();
            notify(new Event(EventType.TIMER_STARTED, getTimerState()));
        }
    }

    /**
     * Pauses the timer if running.
     */
    public void pauseSession() {
        if (isRunning) {
            isRunning = false;
            swingTimer.stop();
            notify(new Event(EventType.TIMER_PAUSED, getTimerState()));
        }
    }

    /**
     * Resets the timer to initial state.
     */
    public void resetSession() {
        isRunning = false;
        swingTimer.stop();
        elapsedTime = 0;
        isWorkPhase = true;
        remainingTime = currentStrategy.getWorkDuration();
        notify(new Event(EventType.TIMER_RESET, getTimerState()));
    }

    /**
     * Skips to the next phase (work or break).
     */
    public void skipToNext() {
        swingTimer.stop();
        isRunning = false;
        switchPhase();
    }

    /**
     * Sets the timer mode strategy.
     * @param mode the new timer mode
     */
    public void setTimerMode(TimerMode mode) {
        if (mode != null) {
            this.currentStrategy = mode;
            resetSession();
            notify(new Event(EventType.MODE_CHANGED, mode));
        }
    }

    /**
     * @return the current timer mode
     */
    public TimerMode getCurrentMode() {
        return currentStrategy;
    }

    /**
     * @return true if timer is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * @return remaining time in seconds
     */
    public int getRemainingTime() {
        return remainingTime;
    }

    /**
     * @return true if in work phase, false if break
     */
    public boolean isWorkPhase() {
        return isWorkPhase;
    }

    /**
     * @return time formatted as MM:SS
     */
    public String getFormattedTime() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private Object[] getTimerState() {
        return new Object[]{remainingTime, isWorkPhase, currentStrategy.getName()};
    }

    private void tick() {
        if (remainingTime > 0) {
            remainingTime--;
            elapsedTime++;
            notify(new Event(EventType.TIMER_TICK, getTimerState()));
        } else {
            swingTimer.stop();
            isRunning = false;
            notify(new Event(EventType.TIMER_COMPLETED, getTimerState()));

            if (isWorkPhase) {
                notify(new Event(EventType.SESSION_COMPLETED, elapsedTime));
            }

            switchPhase();
        }
    }

    private void switchPhase() {
        isWorkPhase = !isWorkPhase;
        elapsedTime = 0;
        remainingTime = isWorkPhase ? currentStrategy.getWorkDuration() : currentStrategy.getBreakDuration();
        notify(new Event(EventType.TIMER_RESET, getTimerState()));
    }
}

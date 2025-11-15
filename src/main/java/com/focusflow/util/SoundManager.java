package com.focusflow.util;

import java.awt.Toolkit;

/**
 * Manages sound playback for notifications.
 * Uses Singleton pattern to ensure one instance.
 *
 * @author Fareed Uddin
 */
public class SoundManager {

    private static SoundManager instance;

    private SoundManager() {
    }

    /**
     * Returns the singleton instance.
     */
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Plays the notification sound.
     */
    public void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }
}

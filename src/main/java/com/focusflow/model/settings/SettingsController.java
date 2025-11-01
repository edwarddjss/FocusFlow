package com.focusflow.model.settings;

import com.focusflow.model.coach.StorageHandler;
import com.focusflow.observer.Event;
import com.focusflow.observer.Event.EventType;
import com.focusflow.observer.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton controller for managing application settings.
 *
 * @author Gianluca Binetti
 */
public class SettingsController {

    private static SettingsController instance;

    private Map<String, Object> settings;
    private List<Observer> observers;
    private StorageHandler storageHandler;

    // Settings keys
    public static final String KEY_SOUND_ENABLED = "soundEnabled";
    public static final String KEY_CUSTOM_WORK_DURATION = "customWorkDuration";
    public static final String KEY_CUSTOM_BREAK_DURATION = "customBreakDuration";
    public static final String KEY_THEME = "theme";
    public static final String KEY_AUTO_START_BREAKS = "autoStartBreaks";
    public static final String KEY_AUTO_START_WORK = "autoStartWork";
    public static final String KEY_GROQ_API_KEY = "groqApiKey";

    private SettingsController() {
        this.settings = new HashMap<>();
        this.observers = new ArrayList<>();
        this.storageHandler = new StorageHandler();
        loadDefaultSettings();
    }

    /**
     * Returns the singleton instance.
     */
    public static synchronized SettingsController getInstance() {
        if (instance == null) {
            instance = new SettingsController();
        }
        return instance;
    }

    /**
     * Resets singleton for testing purposes.
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    private void loadDefaultSettings() {
        settings.put(KEY_SOUND_ENABLED, true);
        settings.put(KEY_CUSTOM_WORK_DURATION, 25);
        settings.put(KEY_CUSTOM_BREAK_DURATION, 5);
        settings.put(KEY_THEME, "light");
        settings.put(KEY_AUTO_START_BREAKS, false);
        settings.put(KEY_AUTO_START_WORK, false);
        settings.put(KEY_GROQ_API_KEY, "");

        loadSettings();
    }

    /**
     * Gets the Groq API key from settings or environment variable.
     */
    public String getGroqApiKey() {
        String settingsKey = getSetting(KEY_GROQ_API_KEY, "");
        if (settingsKey != null && !settingsKey.isEmpty()) {
            return settingsKey;
        }
        return System.getenv("GROQ_API_KEY");
    }

    /**
     * Gets a setting value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key) {
        return (T) settings.get(key);
    }

    /**
     * Gets a setting with a default fallback.
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String key, T defaultValue) {
        Object value = settings.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Sets a setting value.
     */
    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }

    /**
     * Saves settings to disk and notifies observers.
     */
    public void saveSettings() {
        storageHandler.saveSettings(settings);
        notifyObservers(new Event(EventType.SETTINGS_CHANGED, settings));
    }

    @SuppressWarnings("unchecked")
    public void loadSettings() {
        Map<String, Object> loaded = storageHandler.loadSettings(HashMap.class);
        if (loaded != null) {
            settings.putAll(loaded);
        }
    }

    public boolean isSoundEnabled() {
        return getSetting(KEY_SOUND_ENABLED, true);
    }

    public String getTheme() {
        return getSetting(KEY_THEME, "light");
    }

    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(Observer observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }

    public Map<String, Object> getAllSettings() {
        return new HashMap<>(settings);
    }
}

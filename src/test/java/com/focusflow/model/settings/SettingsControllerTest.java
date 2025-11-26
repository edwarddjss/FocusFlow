package com.focusflow.model.settings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SettingsController.
 *
 * @author Gianluca Binetti
 */
class SettingsControllerTest {

    private SettingsController settings;

    @BeforeEach
    void setUp() {
        SettingsController.resetInstance();
        settings = SettingsController.getInstance();
    }

    @AfterEach
    void tearDown() {
        SettingsController.resetInstance();
    }

    @Test
    void testSingletonInstance() {
        SettingsController another = SettingsController.getInstance();
        assertSame(settings, another);
    }

    @Test
    void testGetSetting() {
        Object value = settings.getSetting(SettingsController.KEY_SOUND_ENABLED);
        assertNotNull(value);
    }

    @Test
    void testSetSetting() {
        settings.setSetting(SettingsController.KEY_SOUND_ENABLED, false);
        assertFalse(settings.isSoundEnabled());
    }

    @Test
    void testDefaultSoundEnabled() {
        assertTrue(settings.isSoundEnabled());
    }

    @Test
    void testDefaultTheme() {
        String theme = settings.getTheme();
        assertNotNull(theme);
    }

    @Test
    void testGetAllSettings() {
        assertNotNull(settings.getAllSettings());
        assertFalse(settings.getAllSettings().isEmpty());
    }
}

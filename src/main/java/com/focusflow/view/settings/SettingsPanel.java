package com.focusflow.view.settings;

import com.focusflow.model.settings.SettingsController;

import javax.swing.*;
import java.awt.*;

/**
 * Settings panel with mute toggle and API key input.
 *
 * @author Gianluca Binetti
 */
public class SettingsPanel extends JPanel {

    private JCheckBox muteCheckbox;
    private JPasswordField apiKeyField;
    private JCheckBox showKeyCheckbox;
    private JLabel apiStatusLabel;
    private JButton saveButton;

    private SettingsController settingsController;

    /**
     * Creates a new SettingsPanel.
     */
    public SettingsPanel() {
        settingsController = SettingsController.getInstance();
        initializeUI();
        initializeListeners();
        loadSettings();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel settingsPanel = createSettingsContent();
        contentPanel.add(settingsPanel);
        contentPanel.add(Box.createVerticalGlue());

        add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the main settings content.
     */
    private JPanel createSettingsContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Mute checkbox
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Mute Sound:"), gbc);
        gbc.gridx = 1;
        muteCheckbox = new JCheckBox();
        panel.add(muteCheckbox, gbc);

        // Separator
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;

        // API Key
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("API Key:"), gbc);
        gbc.gridx = 1;
        apiKeyField = new JPasswordField(20);
        panel.add(apiKeyField, gbc);

        // Show key checkbox
        gbc.gridx = 1; gbc.gridy = 3;
        showKeyCheckbox = new JCheckBox("Show API key");
        showKeyCheckbox.addActionListener(e -> {
            apiKeyField.setEchoChar(showKeyCheckbox.isSelected() ? (char) 0 : '*');
        });
        panel.add(showKeyCheckbox, gbc);

        // Status
        gbc.gridx = 1; gbc.gridy = 4;
        apiStatusLabel = new JLabel(" ");
        panel.add(apiStatusLabel, gbc);

        // Info text
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Get free API key at console.groq.com/keys"), gbc);

        return panel;
    }

    /**
     * Creates the button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveButton = new JButton("Save Settings");
        panel.add(saveButton);
        return panel;
    }

    /**
     * Initializes event listeners.
     */
    private void initializeListeners() {
        saveButton.addActionListener(e -> saveSettings());
    }

    /**
     * Loads settings from the controller.
     */
    public void loadSettings() {
        boolean soundEnabled = settingsController.isSoundEnabled();
        muteCheckbox.setSelected(!soundEnabled);

        String apiKey = settingsController.getSetting(SettingsController.KEY_GROQ_API_KEY, "");
        apiKeyField.setText(apiKey);
        updateApiStatus();
    }

    /**
     * Saves current settings.
     */
    private void saveSettings() {
        settingsController.setSetting(SettingsController.KEY_SOUND_ENABLED, !muteCheckbox.isSelected());

        String apiKey = new String(apiKeyField.getPassword()).trim();
        settingsController.setSetting(SettingsController.KEY_GROQ_API_KEY, apiKey);
        updateApiStatus();

        settingsController.saveSettings();
        JOptionPane.showMessageDialog(this, "Settings saved!", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Updates the API status label.
     */
    private void updateApiStatus() {
        String apiKey = settingsController.getGroqApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            apiStatusLabel.setText("API key configured");
        } else {
            apiStatusLabel.setText("No API key - AI features disabled");
        }
    }

    public SettingsController getSettingsController() {
        return settingsController;
    }
}

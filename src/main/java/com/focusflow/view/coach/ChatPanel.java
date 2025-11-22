package com.focusflow.view.coach;

import com.focusflow.model.coach.AIPlanner;
import com.focusflow.model.planner.Planner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat interface for the AI Study Planner.
 *
 * @author Fareed Uddin
 */
public class ChatPanel extends JPanel {

    private final AIPlanner aiPlanner;
    private final Planner planner;

    private JPanel messagesPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;

    private final List<String> conversationHistory;

    /**
     * Creates chat panel with AI planner and calendar.
     */
    public ChatPanel(AIPlanner aiPlanner, Planner planner) {
        this.aiPlanner = aiPlanner;
        this.planner = planner;
        this.conversationHistory = new ArrayList<>();

        initializeUI();
        addWelcomeMessage();
    }

    /**
     * Sets up the chat UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("AI Study Assistant"));

        // Messages area
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void addWelcomeMessage() {
        addMessage(
                "Hello! I'm your study planning assistant. I can help you organize your tasks, schedule study sessions, and manage your calendar. What are you working on?",
                false);
    }

    /**
     * Sends the current message to the AI.
     */
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty())
            return;

        addMessage(text, true);
        inputField.setText("");
        inputField.setEnabled(false);
        sendButton.setEnabled(false);

        // Process in background
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return aiPlanner.chat(text, planner);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    addMessage(response, false);
                } catch (Exception e) {
                    addMessage("Sorry, I encountered an error: " + e.getMessage(), false);
                } finally {
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocus();
                }
            }
        };
        worker.execute();
    }

    private void addMessage(String text, boolean isUser) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textArea.setBackground(isUser ? new Color(230, 240, 255) : new Color(240, 240, 240));
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Calculate width based on panel size (80% of available width)
        int panelWidth = scrollPane.getViewport().getWidth();
        if (panelWidth <= 0) panelWidth = 300; // fallback
        int bubbleWidth = (int) (panelWidth * 0.80);

        // Set the width and let height be calculated
        textArea.setSize(new Dimension(bubbleWidth, Short.MAX_VALUE));
        Dimension preferred = textArea.getPreferredSize();
        textArea.setPreferredSize(new Dimension(bubbleWidth, preferred.height));
        textArea.setMaximumSize(new Dimension(bubbleWidth, preferred.height));

        // Create a wrapper that aligns left or right
        JPanel messageRow = new JPanel();
        messageRow.setLayout(new BoxLayout(messageRow, BoxLayout.X_AXIS));
        messageRow.setBackground(Color.WHITE);
        messageRow.setBorder(new EmptyBorder(3, 10, 3, 10));

        if (isUser) {
            messageRow.add(Box.createHorizontalGlue());
            messageRow.add(textArea);
            conversationHistory.add("User: " + text);
        } else {
            messageRow.add(textArea);
            messageRow.add(Box.createHorizontalGlue());
            conversationHistory.add("Assistant: " + text);
        }

        // Constrain max height of the row
        messageRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height + 10));

        // Remove glue if exists (always at end), add message, re-add glue
        Component[] components = messagesPanel.getComponents();
        if (components.length > 0 && components[components.length - 1] instanceof Box.Filler) {
            messagesPanel.remove(components.length - 1);
        }
        messagesPanel.add(messageRow);
        messagesPanel.add(Box.createVerticalGlue());
        messagesPanel.revalidate();
        messagesPanel.repaint();

        // Auto-scroll
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * Displays a user message bubble.
     */
    public void addUserMessage(String text) {
        addMessage(text, true);
    }

    /**
     * Displays an AI message bubble.
     */
    public void addAIMessage(String text) {
        addMessage(text, false);
    }
}

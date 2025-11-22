package com.focusflow.model.coach;

import com.focusflow.observer.Event;
import com.focusflow.observer.Observer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI-powered planning assistant using Groq API.
 * Helps schedule study sessions using Llama models.
 *
 * @author Fareed Uddin
 */
public class AIPlanner implements Observer {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    private String model;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final StorageHandler storageHandler;
    private final List<Reflection> reflections;
    private final List<Observer> observers;

    /**
     * Creates planner with default settings.
     */
    public AIPlanner() {
        this.model = DEFAULT_MODEL;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.storageHandler = new StorageHandler();
        this.reflections = new ArrayList<>();
        this.observers = new ArrayList<>();
        loadReflections();
    }

    /**
     * Creates planner with custom storage handler.
     */
    public AIPlanner(StorageHandler storageHandler) {
        this();
    }

    private String getApiKey() {
        return com.focusflow.model.settings.SettingsController.getInstance().getGroqApiKey();
    }

    /**
     * Sets the LLM model to use.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return true if there is data for feedback
     */
    public boolean hasEnoughData() {
        return !reflections.isEmpty();
    }

    /**
     * Sends a chat message and returns the response.
     */
    public String chat(String userMessage, com.focusflow.model.planner.Planner planner) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            return "Please configure your Groq API key in Settings to use the AI assistant.";
        }

        // Build the prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a smart study planning assistant. You can manage the user's calendar.\n");
        prompt.append("Current Date: ").append(java.time.LocalDate.now()).append("\n");
        prompt.append("Existing Events:\n");

        for (com.focusflow.model.planner.Planner.PlannerEvent e : planner.getAllEvents()) {
            prompt.append("- ").append(e.getTitle())
                    .append(" (").append(e.getStartTime()).append(" to ").append(e.getEndTime()).append(")")
                    .append(e.isStudyBlock() ? " [STUDY]" : "")
                    .append("\n");
        }

        prompt.append("\nUser: ").append(userMessage).append("\n\n");
        prompt.append("INSTRUCTIONS:\n");
        prompt.append("1. Analyze the user's request and existing events.\n");
        prompt.append("2. Check for time conflicts. DO NOT schedule events that overlap.\n");
        prompt.append("3. If user asks to plan study sessions, create events with isStudy: true.\n");
        prompt.append("4. Output ONLY a JSON array of actions.\n");
        prompt.append("Example format:\n");
        prompt.append("[{\"action\": \"create_event\", \"title\": \"...\", \"start\": \"YYYY-MM-DDTHH:MM\", \"end\": \"YYYY-MM-DDTHH:MM\", \"isStudy\": true, \"mode\": \"POMODORO\"}]\n");

        String response = callGroqAPI(prompt.toString());

        // Try to parse JSON from response
        String jsonToParse = extractJsonFromResponse(response);
        
        List<String> addedSessions = new ArrayList<>();
        if (jsonToParse != null) {
            addedSessions = executeActions(jsonToParse, planner);
        }

        if (!addedSessions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("I've added the following study session(s) to your calendar:\n\n");
            for (String session : addedSessions) {
                sb.append("- ").append(session).append("\n");
            }
            sb.append("\nCheck the calendar to see your updated schedule!");
            return sb.toString();
        } else if (jsonToParse != null) {
            return "I've reviewed your schedule. No changes were needed at this time.";
        }

        // Return cleaned response if no JSON found
        return response.length() > 500 ? response.substring(0, 500) : response;
    }

    /**
     * Extracts JSON array from AI response.
     */
    private String extractJsonFromResponse(String response) {
        String text = response.trim();
        
        // Look for JSON in code block
        if (text.contains("```json")) {
            int start = text.indexOf("```json") + 7;
            int end = text.lastIndexOf("```");
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }
        
        // Look for raw JSON array
        if (text.contains("[") && text.contains("]")) {
            int start = text.indexOf("[");
            int end = text.lastIndexOf("]") + 1;
            if (end > start) {
                return text.substring(start, end).trim();
            }
        }
        
        return null;
    }

    private List<String> executeActions(String json, com.focusflow.model.planner.Planner planner) {
        List<String> addedSessions = new ArrayList<>();

        try {
            JsonArray actions = gson.fromJson(json, JsonArray.class);
            for (int i = 0; i < actions.size(); i++) {
                JsonObject action = actions.get(i).getAsJsonObject();
                String type = action.get("action").getAsString();

                if ("create_event".equals(type)) {
                    String title = action.get("title").getAsString();
                    String desc = action.has("description") ? action.get("description").getAsString() : "";
                    java.time.LocalDateTime start = java.time.LocalDateTime.parse(action.get("start").getAsString());
                    java.time.LocalDateTime end = java.time.LocalDateTime.parse(action.get("end").getAsString());

                    String mode = action.has("mode") ? action.get("mode").getAsString() : "POMODORO";
                    boolean added = planner.addStudyEvent(title, desc, start, end, mode);
                    if (added) {
                        addedSessions.add(title + " on " + start.toLocalDate());
                    }
                } else if ("delete_event".equals(type)) {
                    String title = action.get("title").getAsString();
                    for (com.focusflow.model.planner.Planner.PlannerEvent e : planner.getAllEvents()) {
                        if (e.getTitle().equals(title)) {
                            planner.removeEvent(e.getId());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to execute AI actions: " + e.getMessage());
        }
        return addedSessions;
    }

    /**
     * Gets general productivity feedback from AI.
     */
    public String generateFeedback() {
        if (getApiKey() == null || getApiKey().isEmpty()) {
            return "Configure your API key in Settings to get AI feedback.";
        }
        if (!hasEnoughData()) {
            return "Not enough data yet. Complete a session to get AI feedback.";
        }
        return callGroqAPI("Give 3 short productivity tips based on user data.");
    }

    private String callGroqAPI(String prompt) {
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);
            JsonArray messages = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("role", "user");
            msg.addProperty("content", prompt);
            messages.add(msg);
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.7);

            Request request = new Request.Builder()
                    .url(GROQ_API_URL)
                    .addHeader("Authorization", "Bearer " + getApiKey())
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "Error: " + response.code();
                }
                String body = response.body().string();
                JsonObject jsonResponse = gson.fromJson(body, JsonObject.class);
                return jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString().trim();
            }
        } catch (Exception e) {
            return "Connection error: " + e.getMessage();
        }
    }

    /**
     * Saves a reflection to storage.
     */
    public void saveReflection(Reflection r) {
        reflections.add(r);
        storageHandler.saveReflections(reflections);
    }

    /**
     * @return all saved reflections
     */
    public List<Reflection> getReflections() {
        return new ArrayList<>(reflections);
    }

    /**
     * Adds an observer.
     */
    public void addObserver(Observer o) {
        observers.add(o);
    }

    public void attach(Observer o) {
        addObserver(o);
    }

    public void detach(Observer o) {
        observers.remove(o);
    }

    private void loadReflections() {
        Type listType = new TypeToken<List<Reflection>>() {}.getType();
        List<Reflection> loaded = storageHandler.loadReflections(listType);
        if (loaded != null) {
            reflections.addAll(loaded);
        }
    }

    @Override
    public void update(Event e) {
        // Handle events if needed
    }
}

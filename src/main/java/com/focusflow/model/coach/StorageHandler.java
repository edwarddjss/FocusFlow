package com.focusflow.model.coach;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Handles JSON file storage for sessions, settings, and reflections.
 *
 * @author Fareed Uddin
 */
public class StorageHandler {

    private static final String DATA_DIRECTORY = "focusflow_data";
    private static final String SESSIONS_FILE = "sessions.json";
    private static final String SETTINGS_FILE = "settings.json";
    private static final String REFLECTIONS_FILE = "reflections.json";

    private final Gson gson;
    private final Path dataPath;

    public StorageHandler() {
        this(System.getProperty("user.home") + File.separator + DATA_DIRECTORY);
    }

    public StorageHandler(String dataDirectory) {
        // Register adapters for LocalDateTime and LocalDate
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new DateTimeSerializer())
                .registerTypeAdapter(LocalDate.class, new DateSerializer())
                .create();
        this.dataPath = Paths.get(dataDirectory);
        ensureDataDirectoryExists();
    }

    // Simple serializer for LocalDateTime - stores as string
    private static class DateTimeSerializer implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString());
        }
    }

    // Simple serializer for LocalDate - stores as string
    private static class DateSerializer implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDate.parse(json.getAsString());
        }
    }

    private void ensureDataDirectoryExists() {
        try {
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    /**
     * Saves data to JSON file.
     */
    public <T> void save(String filename, T data) {
        Path filePath = dataPath.resolve(filename);
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Failed to save data to " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Loads data from JSON file.
     * @return the loaded data, or null if file doesn't exist
     */
    public <T> T load(String filename, Type type) {
        Path filePath = dataPath.resolve(filename);
        if (!Files.exists(filePath)) {
            return null;
        }
        try (Reader reader = new FileReader(filePath.toFile())) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("Failed to load data from " + filename + ": " + e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            System.err.println("Corrupted data file " + filename + ": " + e.getMessage());
            return null;
        }
    }

    public <T> T load(String filename, Class<T> clazz) {
        return load(filename, (Type) clazz);
    }

    public void saveSessions(Object sessions) {
        save(SESSIONS_FILE, sessions);
    }

    public <T> T loadSessions(Type type) {
        return load(SESSIONS_FILE, type);
    }

    public void saveSettings(Object settings) {
        save(SETTINGS_FILE, settings);
    }

    public <T> T loadSettings(Type type) {
        return load(SETTINGS_FILE, type);
    }

    public <T> T loadSettings(Class<T> clazz) {
        return load(SETTINGS_FILE, clazz);
    }

    public void saveReflections(Object reflections) {
        save(REFLECTIONS_FILE, reflections);
    }

    public <T> T loadReflections(Type type) {
        return load(REFLECTIONS_FILE, type);
    }

    public Path getDataPath() {
        return dataPath;
    }
}

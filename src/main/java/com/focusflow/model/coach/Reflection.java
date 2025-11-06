package com.focusflow.model.coach;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user reflection entry after focus sessions.
 *
 * @author Fareed Uddin
 */
public class Reflection {

    private String id;
    private String text;
    private String mood;
    private int productivityRating;
    private LocalDateTime timestamp;

    public Reflection() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.productivityRating = 5;
    }

    public Reflection(String text, String mood, int productivityRating) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.mood = mood;
        this.productivityRating = Math.max(1, Math.min(10, productivityRating));
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public int getProductivityRating() { return productivityRating; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setUserResponse(String userResponse) {
        this.text = userResponse;
    }

    public void setMoodRating(int moodRating) {
        this.productivityRating = Math.max(1, Math.min(5, moodRating));
    }

    public void setCoachFeedback(String coachFeedback) {
        // Store feedback if needed
    }

    public void saveReflection(StorageHandler storageHandler) {
        storageHandler.saveReflections(this);
    }

    @Override
    public String toString() {
        return String.format("Reflection[mood=%s, rating=%d, date=%s]",
                mood, productivityRating, timestamp.toLocalDate());
    }
}

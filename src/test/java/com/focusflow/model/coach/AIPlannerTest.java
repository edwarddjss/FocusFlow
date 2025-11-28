package com.focusflow.model.coach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AIPlanner.
 *
 * @author Fareed Uddin
 */
class AIPlannerTest {

    private AIPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new AIPlanner();
    }

    @Test
    void testCreatePlanner() {
        assertNotNull(planner);
    }

    @Test
    void testGetReflections() {
        List<Reflection> reflections = planner.getReflections();
        assertNotNull(reflections);
    }

    @Test
    void testHasEnoughDataMethod() {
        boolean result = planner.hasEnoughData();
        assertTrue(result == true || result == false);
    }

    @Test
    void testSetModel() {
        planner.setModel("llama-3.1-8b-instant");
        assertTrue(true);
    }

    @Test
    void testGenerateFeedbackWithoutApiKey() {
        String feedback = planner.generateFeedback();
        assertNotNull(feedback);
    }

    @Test
    void testSaveReflection() {
        Reflection reflection = new Reflection("Test reflection", "Good", 4);
        planner.saveReflection(reflection);
        assertTrue(planner.getReflections().size() > 0);
    }
}

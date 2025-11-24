package com.focusflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Main class.
 *
 * @author Fareed Uddin
 */
@DisplayName("Main Tests")
class MainTest {

    @Test
    @DisplayName("Main class exists")
    void testMainClassExists() {
        assertDoesNotThrow(() -> Class.forName("com.focusflow.Main"));
    }

    @Test
    @DisplayName("Main has main method")
    void testMainMethodExists() throws NoSuchMethodException {
        assertNotNull(Main.class.getMethod("main", String[].class));
    }
}

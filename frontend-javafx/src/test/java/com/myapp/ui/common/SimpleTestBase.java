package com.myapp.ui.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

/**
 * Simple test base class that doesn't use JavaFX testing framework
 * This avoids the MonocleWindow issues in headless environments
 */
public abstract class SimpleTestBase {

    @BeforeEach
    public void setUp() throws Exception {
        // Clear authentication state before each test
        HttpClientUtil.clearTokens();
        
        // Reset NavigationController
        NavigationController.resetInstance();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clean up after each test
        HttpClientUtil.clearTokens();
    }

    /**
     * Helper method to create mock UI components for testing
     */
    protected void createMockUIComponents() {
        // This method can be overridden by subclasses to create mock UI components
        // without relying on JavaFX testing framework
    }

    /**
     * Helper method to simulate UI interactions
     */
    protected void simulateUIInteraction(String componentId, Object value) {
        // This method can be overridden by subclasses to simulate UI interactions
        // without relying on JavaFX testing framework
    }
} 
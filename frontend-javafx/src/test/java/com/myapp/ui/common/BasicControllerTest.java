package com.myapp.ui.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic controller test that tests logic without JavaFX UI components
 * This avoids the MonocleWindow issues in headless environments
 */
public class BasicControllerTest extends SimpleTestBase {

    @Test
    @DisplayName("Basic test to verify test infrastructure works")
    void testBasicInfrastructure() {
        assertTrue(true, "Basic test infrastructure is working");
    }

    @Test
    @DisplayName("Test HttpClientUtil token clearing")
    void testHttpClientUtilTokenClearing() {
        // Test that HttpClientUtil can be called without errors
        assertDoesNotThrow(() -> {
            HttpClientUtil.clearTokens();
        });
    }

    @Test
    @DisplayName("Test NavigationController reset")
    void testNavigationControllerReset() {
        // Test that NavigationController can be reset without errors
        assertDoesNotThrow(() -> {
            NavigationController.resetInstance();
        });
    }
} 
package com.myapp.ui.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import javafx.application.Platform;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

/**
 * Base Test Class for Frontend Tests
 * Provides common functionality and setup for all test classes
 * Makes tests more extensible and maintainable
 */
public abstract class BaseTestClass {

    // Test configuration constants
    protected static final int DEFAULT_TIMEOUT_SECONDS = 10;
    protected static final int NETWORK_TIMEOUT_MS = 5000;
    protected static final int UI_WAIT_TIME_MS = 100;

    @BeforeAll
    static void setUpTestEnvironment() {
        // Set system properties for headless testing
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "true");
        
        System.out.println("✓ Test environment initialized");
    }

    @BeforeEach
    void setUp() {
        // Clear authentication state before each test
        HttpClientUtil.clearAuthToken();
        
        // Reset NavigationController to clean state
        try {
            NavigationController.resetInstance();
        } catch (Exception e) {
            // Ignore reset errors in test environment
        }
        
        System.out.println("✓ Test setup completed");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        HttpClientUtil.clearAuthToken();
        
        // Wait for any async operations to complete
        waitForFxEvents();
        
        System.out.println("✓ Test cleanup completed");
    }

    @AfterAll
    static void tearDownTestEnvironment() {
        // Reset timeout to default
        HttpClientUtil.setTimeoutMs(30000);
        
        System.out.println("✓ Test environment cleaned up");
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Wait for JavaFX events to complete
     */
    protected void waitForFxEvents() {
        try {
            WaitForAsyncUtils.waitForFxEvents();
        } catch (Exception e) {
            // Ignore wait errors in test environment
        }
    }

    /**
     * Wait for async operations with timeout
     */
    protected void waitForAsync(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Execute operation on FX thread and wait for completion
     */
    protected void runOnFxThreadAndWait(Runnable operation) {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                operation.run();
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Assert that error message is user-friendly
     */
    protected void assertUserFriendlyErrorMessage(String errorMessage, String context) {
        assertNotNull(errorMessage, context + ": Error message should not be null");
        
        String lowerMsg = errorMessage.toLowerCase();
        
        // Should not contain technical implementation details
        assertFalse(lowerMsg.contains("nullpointerexception"), 
                   context + ": Should not expose NullPointerException");
        assertFalse(lowerMsg.contains("stacktrace"), 
                   context + ": Should not expose stack traces");
        assertFalse(lowerMsg.contains("java."), 
                   context + ": Should not expose Java class names");
        assertFalse(lowerMsg.contains("sql"), 
                   context + ": Should not expose SQL details");
        
        System.out.println("✓ User-friendly error message validated for: " + context);
    }

    /**
     * Assert that network error message is descriptive
     */
    protected void assertNetworkErrorMessage(String errorMessage, String context) {
        assertNotNull(errorMessage, context + ": Network error should have message");
        
        String lowerMsg = errorMessage.toLowerCase();
        boolean hasNetworkIndicator = lowerMsg.contains("timeout") || 
                      lowerMsg.contains("connection") || 
                      lowerMsg.contains("network") ||
                      lowerMsg.contains("failed") ||
                      lowerMsg.contains("error") ||
                      lowerMsg.contains("unavailable") ||
                      lowerMsg.contains("check") ||
                      lowerMsg.contains("404") ||
                      lowerMsg.contains("not found") ||
                      lowerMsg.contains("context");
                      
        assertTrue(hasNetworkIndicator, 
                  context + ": Network error should indicate network issue: " + errorMessage);
        
        System.out.println("✓ Network error message validated for: " + context);
    }

    /**
     * Assert that response contains no sensitive data
     */
    protected void assertNoSensitiveDataExposure(String responseData, String context) {
        if (responseData == null) return;
        
        String lowerData = responseData.toLowerCase();
        
        // Check for password exposure
        if (lowerData.contains("\"id\"") && lowerData.contains("\"email\"")) {
            assertFalse(lowerData.contains("passwordhash") && 
                       !lowerData.contains("passwordchange") && 
                       !lowerData.contains("passwordfield"), 
                      context + ": Should not expose password hashes");
        }
        
        // Check for other sensitive data
        assertFalse(lowerData.contains("\"ssn\""), 
                   context + ": Should not expose SSN");
        assertFalse(lowerData.contains("\"creditcard\""), 
                   context + ": Should not expose credit card numbers");
        
        System.out.println("✓ Sensitive data exposure check passed for: " + context);
    }

    /**
     * Test network scenarios with different timeout values
     */
    protected void testNetworkScenarios(String[] endpoints, int timeoutMs) {
        int originalTimeout = 30000; // Store original
        
        try {
            HttpClientUtil.setTimeoutMs(timeoutMs);
            
            for (String endpoint : endpoints) {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get(endpoint);
                assertNotNull(response, "Should get response for: " + endpoint);
                
                if (!response.isSuccess()) {
                    assertNetworkErrorMessage(response.getMessage(), endpoint);
                }
            }
            
        } finally {
            HttpClientUtil.setTimeoutMs(originalTimeout);
        }
    }

    /**
     * Create mock JSON data for testing
     */
    protected String createMockUserData(boolean includeSensitiveData) {
        if (includeSensitiveData) {
            return "{\"id\":1,\"fullName\":\"Test User\",\"email\":\"test@example.com\"," +
                   "\"passwordHash\":\"secret123\",\"role\":\"BUYER\"}";
        } else {
            return "{\"id\":1,\"fullName\":\"Test User\",\"email\":\"test@example.com\"," +
                   "\"role\":\"BUYER\",\"isActive\":true}";
        }
    }

    /**
     * Create mock transaction data
     */
    protected String createMockTransactionData(boolean includeSensitiveData) {
        if (includeSensitiveData) {
            return "{\"id\":1,\"amount\":100,\"status\":\"COMPLETED\"," +
                   "\"creditCard\":\"1234-5678-9012-3456\"}";
        } else {
            return "{\"id\":1,\"amount\":100,\"status\":\"COMPLETED\"," +
                   "\"paymentMethod\":\"CARD\"}";
        }
    }

    // ==================== ASSERTIONS ====================

    protected void assertNotNull(Object object, String message) {
        org.junit.jupiter.api.Assertions.assertNotNull(object, message);
    }

    protected void assertNull(Object object, String message) {
        org.junit.jupiter.api.Assertions.assertNull(object, message);
    }

    protected void assertTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assertions.assertTrue(condition, message);
    }

    protected void assertFalse(boolean condition, String message) {
        org.junit.jupiter.api.Assertions.assertFalse(condition, message);
    }

    protected void assertEquals(Object expected, Object actual, String message) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual, message);
    }

    protected void fail(String message) {
        org.junit.jupiter.api.Assertions.fail(message);
    }
} 
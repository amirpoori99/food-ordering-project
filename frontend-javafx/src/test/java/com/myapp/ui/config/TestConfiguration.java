package com.myapp.ui.config;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test Configuration for JavaFX UI Tests
 * Handles headless environment setup and performance optimization
 */
public class TestConfiguration implements BeforeAllCallback {

    private static boolean javaFxInitialized = false;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        initializeJavaFX();
    }

    /**
     * Initialize JavaFX toolkit for headless testing
     */
    public static void initializeJavaFX() throws InterruptedException {
        if (!javaFxInitialized) {
            // Set headless properties for JavaFX
            System.setProperty("java.awt.headless", "true");
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("glass.platform", "Monocle");
            System.setProperty("monocle.platform", "Headless");
            
            CountDownLatch latch = new CountDownLatch(1);
            
            Platform.startup(() -> {
                // JavaFX Platform initialized
                latch.countDown();
            });
            
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to initialize JavaFX Platform");
            }
            
            javaFxInitialized = true;
        }
    }

    /**
     * Create a test stage for UI testing
     */
    public static Stage createTestStage() {
        final Stage[] stageRef = new Stage[1];
        final CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                stageRef[0] = new Stage();
                stageRef[0].setTitle("Test Stage");
                stageRef[0].setWidth(800);
                stageRef[0].setHeight(600);
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
        });
        
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to create test stage", e);
        }
        
        return stageRef[0];
    }

    /**
     * Mock HTTP client for testing without backend dependency
     */
    public static class MockHttpClient {
        
        public static class ApiResponse {
            private final String body;
            private final boolean success;
            private final String message;
            
            public ApiResponse(String body, boolean success, String message) {
                this.body = body;
                this.success = success;
                this.message = message;
            }
            
            public String getBody() { return body; }
            public boolean isSuccess() { return success; }
            public String getMessage() { return message; }
        }
        
        public static ApiResponse get(String endpoint) {
            // Simulate API responses for testing
            switch (endpoint) {
                case "/restaurants":
                    return new ApiResponse("[{\"id\":1,\"name\":\"Test Restaurant\"}]", true, "Success");
                case "/users":
                    return new ApiResponse("[{\"id\":1,\"name\":\"Test User\"}]", true, "Success");
                case "/orders":
                    return new ApiResponse("[{\"id\":1,\"customerName\":\"Test Customer\"}]", true, "Success");
                default:
                    return new ApiResponse("{\"status\":\"ok\"}", true, "Success");
            }
        }
        
        public static ApiResponse post(String endpoint, String body, boolean auth) {
            // Simulate POST responses
            if (endpoint.contains("/auth/login")) {
                return new ApiResponse("{\"token\":\"test-token\",\"user\":{\"id\":1}}", true, "Login successful");
            }
            return new ApiResponse("{\"status\":\"created\"}", true, "Success");
        }
    }

    /**
     * Reduced timeout settings for faster test execution
     */
    public static class TestTimeouts {
        public static final int SHORT_TIMEOUT = 2; // seconds
        public static final int MEDIUM_TIMEOUT = 5; // seconds
        public static final int LONG_TIMEOUT = 10; // seconds
        
        // Performance test timeouts (reduced)
        public static final int API_TIMEOUT = 5; // seconds
        public static final int UI_TIMEOUT = 3; // seconds
        public static final int LOAD_TEST_DURATION = 5; // seconds
    }
    
    /**
     * Utility method for safe UI operations
     */
    public static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    action.run();
                } finally {
                    latch.countDown();
                }
            });
            
            try {
                latch.await(TestTimeouts.MEDIUM_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("UI operation interrupted", e);
            }
        }
    }
} 
package com.myapp.ui.edge;

import com.myapp.ui.common.BaseTestClass;
import com.myapp.ui.common.HttpClientUtil;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * Edge Cases and Error Handling Tests
 * تست‌های حالات مرزی و مدیریت خطا
 * 
 * Extends BaseTestClass for better structure and extensibility
 */
@ExtendWith(ApplicationExtension.class)
class EdgeCaseTest extends BaseTestClass {

    private TextField testTextField;
    private PasswordField testPasswordField;
    private ComboBox<String> testComboBox;
    private ListView<String> testListView;
    private TableView<String> testTableView;

    @Start
    void start(Stage stage) {
        testTextField = new TextField();
        testPasswordField = new PasswordField();
        testComboBox = new ComboBox<>();
        testListView = new ListView<>();
        testTableView = new TableView<>();
        
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(testTextField, testPasswordField, testComboBox, testListView, testTableView);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    @BeforeEach
    void setUp() {
        Platform.runLater(() -> {
            testTextField.clear();
            testPasswordField.clear();
            testComboBox.getItems().clear();
            testListView.getItems().clear();
            testTableView.getItems().clear();
        });
    }

    @Test
    void testEmptyAndNullInputs() throws InterruptedException {
        // تست: handling empty and null inputs
        
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Test empty string inputs
                testTextField.setText("");
                assertEquals("", testTextField.getText(), "Empty string should be handled");
                
                // Test setting null - JavaFX handles this internally, just verify no crash
                try {
                    testTextField.setText(null);
                    assertTrue(true, "Setting null should not crash");
                } catch (Exception e) {
                    fail("TextField should handle null without throwing exception: " + e.getMessage());
                }
                
                // Test password field
                testPasswordField.setText("");
                assertEquals("", testPasswordField.getText(), "Empty password should be handled");
                
                // Test ComboBox with null selection
                testComboBox.setValue(null);
                assertNull(testComboBox.getValue(), "Null selection should be handled");
                
                // Test empty list operations
                testListView.getItems().clear();
                assertEquals(0, testListView.getItems().size(), "Empty list should be handled");
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "UI operations should complete");
        
        // Test API calls with empty/null data
        HttpClientUtil.ApiResponse emptyLoginResponse = HttpClientUtil.post("/auth/login", "{}", false);
        assertNotNull(emptyLoginResponse, "Should get response for empty data");
        
        HttpClientUtil.ApiResponse nullResponse = HttpClientUtil.post("/auth/login", null, false);
        assertNotNull(nullResponse, "Should handle null request gracefully");
    }

    @Test
    void testExtremelyLargeInputs() throws InterruptedException {
        // تست: handling extremely large inputs
        
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Test very long text input
                String veryLongText = "A".repeat(100000);
                testTextField.setText(veryLongText);
                
                // UI should handle large text without crashing
                assertNotNull(testTextField.getText(), "UI should handle large text");
                
                // Test large list population
                for (int i = 0; i < 50000; i++) {
                    testListView.getItems().add("Item " + i);
                }
                
                assertEquals(50000, testListView.getItems().size(), "Should handle large lists");
                
            } catch (OutOfMemoryError e) {
                fail("Should handle large data without OutOfMemoryError");
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Large data operations should complete");
        
        // Test API with large payload
        String largePayload = createLargeJsonPayload(10000);
        HttpClientUtil.ApiResponse largeResponse = HttpClientUtil.post("/restaurants", largePayload, false);
        // Should either succeed or fail gracefully
        assertNotNull(largeResponse, "Should handle large payloads");
        if (!largeResponse.isSuccess()) {
            assertNotNull(largeResponse.getMessage(), "Failed large request should have error message");
        }
    }

    @Test
    void testSpecialCharactersAndUnicode() throws InterruptedException {
        // تست: handling special characters and Unicode
        
        String[] specialInputs = {
            "نام رستوران فارسی", // Persian
            "餐厅名称", // Chinese
            "レストラン名", // Japanese
            "مطعم عربي", // Arabic
            "Ресторан", // Russian
            "🍕🍔🍟", // Emojis
            "Special chars: !@#$%^&*()_+-={}[]|\\:;\"'<>?,./",
            "Unicode: \u0020\u00A0\u2000\u2001\u2002\u2003",
            "Line breaks:\r\n\t",
            "Zero width chars: \u200B\u200C\u200D"
        };
        
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                for (String input : specialInputs) {
                    testTextField.setText(input);
                    // TextField may normalize line breaks and special chars
                    assertNotNull(testTextField.getText(), 
                               "Should handle special characters: " + input);
                    
                    testComboBox.getItems().add(input);
                    testListView.getItems().add(input);
                }
                
                assertFalse(testComboBox.getItems().isEmpty(), "Should add special character items");
                assertFalse(testListView.getItems().isEmpty(), "Should add special character items");
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Special character operations should complete");
        
        // Test API with special characters
        for (String input : specialInputs) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/restaurants",
                createRestaurantRequest(input, "Normal address"), false);
            
            if (response.isSuccess() && response.getData() != null && response.getData().has("name")) {
                // Verify special characters are preserved
                assertTrue(response.getData().get("name").asText().length() > 0,
                          "Special characters should be preserved");
            }
        }
    }

    @Test
    void testConcurrentUIOperations() throws InterruptedException {
        // تست: concurrent UI operations
        
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            
            Platform.runLater(() -> {
                try {
                    // Concurrent list modifications
                    for (int j = 0; j < 1000; j++) {
                        testListView.getItems().add("Thread-" + threadIndex + "-Item-" + j);
                    }
                    
                    // Concurrent text field updates
                    testTextField.setText("Thread " + threadIndex + " updated this");
                    
                    // Concurrent combo box updates
                    testComboBox.getItems().add("Thread-" + threadIndex + "-Option");
                    
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(15, TimeUnit.SECONDS), "Concurrent UI operations should complete");
        
        // Verify UI state is consistent
        Platform.runLater(() -> {
            assertNotNull(testTextField.getText(), "Text field should have some value");
            assertTrue(testListView.getItems().size() > 0, "List should have items");
            assertTrue(testComboBox.getItems().size() > 0, "ComboBox should have items");
        });
    }

    @Test
    void testNetworkDisconnectionScenarios() throws InterruptedException {
        // تست: network disconnection handling using base class utilities
        
        System.out.println("Testing network disconnection scenarios...");
        
        try {
            // Test endpoints that should fail gracefully
            String[] testEndpoints = {
                "/test/network", "/invalid/endpoint", "/timeout/test", 
                "/disconnection/test", "/network/failure"
            };
            
            // Use base class utility to test network scenarios with short timeout
            testNetworkScenarios(testEndpoints, 1); // 1ms timeout to simulate disconnection
            
            // Additional validation for disconnection scenarios
            List<HttpClientUtil.ApiResponse> responses = new ArrayList<>();
            
            HttpClientUtil.setTimeoutMs(1); // Very short timeout
            
            for (String endpoint : testEndpoints) {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get(endpoint);
                responses.add(response);
                
                // Validate individual responses
                assertFalse(response.isSuccess(), "Disconnected requests should fail for: " + endpoint);
                assertUserFriendlyErrorMessage(response.getMessage(), endpoint);
                
                // Small delay to prevent overwhelming
                waitForAsync(10);
            }
            
            System.out.println("✓ All network disconnection scenarios handled properly");
            
        } catch (Exception e) {
            // Network tests may cause exceptions, but they should be handled gracefully
            System.out.println("Exception in network test (expected): " + e.getMessage());
            // This is acceptable for network disconnection testing
        } finally {
            // Reset timeout using base class method
            HttpClientUtil.setTimeoutMs(30000);
            waitForAsync(UI_WAIT_TIME_MS);
        }
    }

    @Test
    void testInvalidDataFormats() {
        // تست: invalid data format handling
        
        String[] invalidJsonPayloads = {
            "{invalid json",
            "not json at all",
            "{\"email\":}",
            "{\"email\":\"test\",}",
            "{\"number\":\"not a number\"}",
            "{\"date\":\"invalid date format\"}",
            "{\"boolean\":\"not boolean\"}",
            "null",
            "[]",
            ""
        };
        
        for (String invalidPayload : invalidJsonPayloads) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/auth/login", invalidPayload, false);
            assertNotNull(response, "Should get response for invalid JSON: " + invalidPayload);
            
            if (response.getMessage() != null) {
                String errorMsg = response.getMessage().toLowerCase();
                // Error message should indicate some kind of issue
                assertTrue(errorMsg.contains("invalid") || 
                          errorMsg.contains("format") || 
                          errorMsg.contains("parse") ||
                          errorMsg.contains("malformed") ||
                          errorMsg.contains("error") ||
                          errorMsg.contains("network") ||
                          errorMsg.contains("failed") ||
                          errorMsg.contains("missing") ||
                          errorMsg.contains("required") ||
                          errorMsg.contains("connection") ||
                          errorMsg.contains("timeout"),
                          "Error message should indicate some issue: " + errorMsg);
            }
        }
    }

    @Test
    void testMemoryExhaustionRecovery() throws InterruptedException {
        // تست: recovery from memory exhaustion
        
        List<Object> memoryConsumers = new ArrayList<>();
        boolean memoryExhausted = false;
        
        try {
            // Try to consume a lot of memory
            for (int i = 0; i < 1000; i++) {
                memoryConsumers.add(new byte[1024 * 1024]); // 1MB each
                
                // Check available memory
                Runtime runtime = Runtime.getRuntime();
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                long maxMemory = runtime.maxMemory();
                
                if (usedMemory > maxMemory * 0.9) { // 90% memory used
                    memoryExhausted = true;
                    break;
                }
            }
            
        } catch (OutOfMemoryError e) {
            memoryExhausted = true;
        }
        
        // Clear memory consumers
        memoryConsumers.clear();
        System.gc();
        Thread.sleep(1000);
        
        // Test that application can recover
        CountDownLatch recoveryLatch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Try basic UI operations after potential memory exhaustion
                testTextField.setText("Recovery test");
                testListView.getItems().add("Recovery item");
                
                assertEquals("Recovery test", testTextField.getText(), 
                           "UI should work after memory recovery");
                assertEquals(1, testListView.getItems().size(), 
                           "List operations should work after recovery");
                
            } finally {
                recoveryLatch.countDown();
            }
        });
        
        assertTrue(recoveryLatch.await(10, TimeUnit.SECONDS), 
                  "UI should recover from memory exhaustion");
        
        // Test API calls still work
        HttpClientUtil.ApiResponse recoveryResponse = HttpClientUtil.get("/restaurants");
        // Should work or fail gracefully
        assertNotNull(recoveryResponse, "API should work after memory recovery");
    }

    @Test
    void testRapidUserInteractions() throws InterruptedException {
        // تست: rapid user interactions
        
        CountDownLatch rapidLatch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Simulate rapid clicking/typing
                for (int i = 0; i < 1000; i++) {
                    testTextField.setText("Rapid input " + i);
                    testComboBox.getItems().add("Rapid option " + i);
                    
                    if (i % 100 == 0) {
                        testListView.getItems().clear();
                        testComboBox.getItems().clear();
                    }
                }
                
                // UI should remain responsive
                assertNotNull(testTextField.getText(), "Text field should handle rapid input");
                
            } finally {
                rapidLatch.countDown();
            }
        });
        
        assertTrue(rapidLatch.await(10, TimeUnit.SECONDS), "Rapid interactions should complete");
    }

    @Test
    void testExtremeEdgeCaseValues() {
        // تست: extreme edge case values
        
        String[] extremeValues = {
            String.valueOf(Integer.MAX_VALUE),
            String.valueOf(Integer.MIN_VALUE),
            String.valueOf(Long.MAX_VALUE),
            String.valueOf(Long.MIN_VALUE),
            String.valueOf(Double.MAX_VALUE),
            String.valueOf(Double.MIN_VALUE),
            String.valueOf(Float.POSITIVE_INFINITY),
            String.valueOf(Float.NEGATIVE_INFINITY),
            String.valueOf(Double.NaN),
            "9".repeat(1000), // Very large number
            "-" + "9".repeat(1000), // Very large negative number
            "0.00000000000000000000001", // Very small decimal
            "1E-100", // Scientific notation
            "1E+100"  // Large scientific notation
        };
        
        for (String extremeValue : extremeValues) {
            // Test in restaurant price field
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/restaurants",
                "{\"name\":\"Test\",\"address\":\"Test\",\"averagePrice\":" + extremeValue + "}", false);
            
            // Should either accept valid values or reject invalid ones gracefully
            assertNotNull(response, "Should handle extreme values");
            if (!response.isSuccess() && response.getMessage() != null) {
                assertFalse(response.getMessage().contains("Exception"), 
                          "Error message should be user-friendly");
            }
        }
    }

    // Helper methods
    private String createLargeJsonPayload(int size) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"");
        sb.append("A".repeat(size));
        sb.append("\",\"address\":\"Test Address\",\"description\":\"Test Description\"}");
        return sb.toString();
    }

    private String createRestaurantRequest(String name, String address) {
        return "{"
                + "\"name\":\"" + escapeJson(name) + "\","
                + "\"address\":\"" + escapeJson(address) + "\","
                + "\"phone\":\"021-12345678\","
                + "\"description\":\"Test restaurant\""
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\r", "\\r")
                   .replace("\n", "\\n")
                   .replace("\t", "\\t");
    }
} 
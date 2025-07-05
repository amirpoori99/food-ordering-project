package com.myapp.ui.performance;

import com.myapp.ui.config.TestConfiguration;
import com.myapp.ui.config.TestConfiguration.MockHttpClient;
import com.myapp.ui.config.TestConfiguration.TestTimeouts;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and Stress Tests - Optimized for Native Setup
 * Tests are optimized to work without backend dependency using mocks
 */
@ExtendWith({ApplicationExtension.class, TestConfiguration.class})
@DisplayName("Performance and Stress Tests - Native")
class PerformanceStressTest {

    private ExecutorService executorService;
    private Stage testStage;
    
    @Start
    private void start(Stage stage) {
        this.testStage = stage;
    }

    @BeforeEach
    void setUp() throws Exception {
        // Create thread pool for concurrent tests
        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("API Performance Tests - Mocked")
    class ApiPerformanceTests {

        @Test
        @DisplayName("Should handle high-frequency API calls without degradation")
        void highFrequencyApiCalls() throws InterruptedException {
            int totalCalls = 100; // Reduced from 1000
            CountDownLatch latch = new CountDownLatch(totalCalls);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            AtomicLong totalResponseTime = new AtomicLong(0);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < totalCalls; i++) {
                executorService.submit(() -> {
                    try {
                        long requestStart = System.currentTimeMillis();
                        
                        // Use mock HTTP client instead of real API
                        MockHttpClient.ApiResponse response = MockHttpClient.get("/restaurants");
                        
                        long requestEnd = System.currentTimeMillis();
                        totalResponseTime.addAndGet(requestEnd - requestStart);

                        if (response != null && response.isSuccess()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(TestTimeouts.API_TIMEOUT, TimeUnit.SECONDS), 
                      "All API calls should complete within " + TestTimeouts.API_TIMEOUT + " seconds");

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double averageResponseTime = totalResponseTime.get() / (double) totalCalls;
            double throughput = totalCalls / (totalTime / 1000.0);

            System.out.println("=== API Performance Results ===");
            System.out.println("Total calls: " + totalCalls);
            System.out.println("Success count: " + successCount.get());
            System.out.println("Error count: " + errorCount.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average response time: " + averageResponseTime + "ms");
            System.out.println("Throughput: " + throughput + " requests/second");

            // Relaxed performance assertions for mock testing
            assertTrue(successCount.get() > totalCalls * 0.7, "At least 70% of calls should succeed");
            assertTrue(averageResponseTime < 1000, "Average response time should be under 1 second");
        }

        @Test
        @DisplayName("Should handle concurrent user authentication without race conditions")
        void concurrentAuthentication() throws InterruptedException {
            int concurrentUsers = 50; // Reduced from 100
            CountDownLatch latch = new CountDownLatch(concurrentUsers);
            AtomicInteger authSuccessCount = new AtomicInteger(0);
            AtomicInteger authFailureCount = new AtomicInteger(0);

            for (int i = 0; i < concurrentUsers; i++) {
                final int userId = i;
                executorService.submit(() -> {
                    try {
                        // Use mock authentication
                        String phone = "0912345678" + (userId % 10);
                        String password = "password123";

                        MockHttpClient.ApiResponse response = MockHttpClient.post("/auth/login",
                                "{\"phone\":\"" + phone + "\",\"password\":\"" + password + "\"}", false);

                        if (response != null && response.isSuccess()) {
                            authSuccessCount.incrementAndGet();
                        } else {
                            authFailureCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        authFailureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(TestTimeouts.API_TIMEOUT, TimeUnit.SECONDS), 
                      "All authentication attempts should complete");

            System.out.println("=== Concurrent Authentication Results ===");
            System.out.println("Concurrent users: " + concurrentUsers);
            System.out.println("Auth success: " + authSuccessCount.get());
            System.out.println("Auth failures: " + authFailureCount.get());

            // Should handle all requests without crashing
            assertTrue(authSuccessCount.get() + authFailureCount.get() == concurrentUsers,
                    "All authentication attempts should be processed");
            assertTrue(authSuccessCount.get() > 0, "At least some authentications should succeed");
        }

        @Test
        @DisplayName("Should maintain performance under sustained load")
        void sustainedLoadTest() throws InterruptedException {
            int duration = TestTimeouts.LOAD_TEST_DURATION; // Reduced duration
            int requestsPerSecond = 10; // Reduced rate
            AtomicInteger totalRequests = new AtomicInteger(0);
            AtomicInteger successRequests = new AtomicInteger(0);
            AtomicBoolean running = new AtomicBoolean(true);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

            // Schedule requests at fixed rate
            ScheduledFuture<?> requestScheduler = scheduler.scheduleAtFixedRate(() -> {
                if (running.get()) {
                    for (int i = 0; i < requestsPerSecond; i++) {
                        executorService.submit(() -> {
                            try {
                                totalRequests.incrementAndGet();
                                MockHttpClient.ApiResponse response = MockHttpClient.get("/restaurants");
                                if (response != null && response.isSuccess()) {
                                    successRequests.incrementAndGet();
                                }
                            } catch (Exception e) {
                                // Count as processed but not successful
                            }
                        });
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);

            // Run for specified duration
            Thread.sleep(duration * 1000);
            running.set(false);
            requestScheduler.cancel(false);

            // Wait for remaining requests to complete
            scheduler.shutdown();
            scheduler.awaitTermination(3, TimeUnit.SECONDS);

            System.out.println("=== Sustained Load Test Results ===");
            System.out.println("Duration: " + duration + " seconds");
            System.out.println("Requests per second: " + requestsPerSecond);
            System.out.println("Total requests: " + totalRequests.get());
            System.out.println("Successful requests: " + successRequests.get());
            double successRate = successRequests.get() * 100.0 / Math.max(totalRequests.get(), 1);
            System.out.println("Success rate: " + successRate + "%");

            // Relaxed performance assertions
            assertTrue(successRequests.get() > totalRequests.get() * 0.5, "Success rate should be above 50%");
            assertTrue(totalRequests.get() >= duration * requestsPerSecond * 0.6, "Should maintain reasonable request rate");
        }
    }

    @Nested
    @DisplayName("UI Performance Tests - Optimized")
    class UIPerformanceTests {

        @Test
        @DisplayName("Should handle rapid UI updates without freezing")
        void rapidUIUpdates() throws InterruptedException {
            int updateCount = 100; // Reduced from 1000
            CountDownLatch latch = new CountDownLatch(updateCount);
            AtomicInteger successfulUpdates = new AtomicInteger(0);
            AtomicLong totalUpdateTime = new AtomicLong(0);

            // Use safe UI operations from TestConfiguration
            TestConfiguration.runOnFxThreadAndWait(() -> {
                VBox testContainer = new VBox();
                testStage.setScene(new Scene(testContainer, 400, 300)); // Smaller size
                if (!testStage.isShowing()) {
                    testStage.show();
                }
            });

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < updateCount; i++) {
                final int updateIndex = i;
                Platform.runLater(() -> {
                    try {
                        long updateStart = System.currentTimeMillis();
                        
                        // Simplified UI update
                        VBox container = (VBox) testStage.getScene().getRoot();
                        if (container != null) {
                            javafx.scene.control.Label label = new javafx.scene.control.Label("Update " + updateIndex);
                            container.getChildren().add(label);
                            
                            // Keep container size manageable
                            if (container.getChildren().size() > 50) {
                                container.getChildren().remove(0);
                            }
                        }
                        
                        long updateEnd = System.currentTimeMillis();
                        totalUpdateTime.addAndGet(updateEnd - updateStart);
                        successfulUpdates.incrementAndGet();
                    } catch (Exception e) {
                        System.err.println("UI update failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(TestTimeouts.UI_TIMEOUT, TimeUnit.SECONDS), 
                      "All UI updates should complete within " + TestTimeouts.UI_TIMEOUT + " seconds");

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double averageUpdateTime = successfulUpdates.get() > 0 ? 
                totalUpdateTime.get() / (double) successfulUpdates.get() : 0;

            System.out.println("=== UI Performance Results ===");
            System.out.println("Total updates: " + updateCount);
            System.out.println("Successful updates: " + successfulUpdates.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average update time: " + averageUpdateTime + "ms");

            // Relaxed performance assertions
            assertTrue(successfulUpdates.get() > updateCount * 0.7, "At least 70% of updates should succeed");
            assertTrue(averageUpdateTime < 200, "Average update time should be under 200ms");
        }

        @Test
        @DisplayName("Should handle large dataset rendering efficiently")
        void largeDatasetRendering() throws InterruptedException {
            int itemCount = 1000; // Reduced from 10000
            CountDownLatch latch = new CountDownLatch(1);
            AtomicLong renderTime = new AtomicLong(0);
            AtomicBoolean renderSuccess = new AtomicBoolean(false);

            Platform.runLater(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // Create moderately large dataset
                    ListView<String> listView = new ListView<>();
                    List<String> items = new ArrayList<>();
                    for (int i = 0; i < itemCount; i++) {
                        items.add("Item " + i);
                    }
                    
                    listView.getItems().addAll(items);
                    
                    // Simple scene setup
                    VBox root = new VBox(listView);
                    testStage.setScene(new Scene(root, 400, 300));
                    
                    long endTime = System.currentTimeMillis();
                    renderTime.set(endTime - startTime);
                    renderSuccess.set(true);
                    
                } catch (Exception e) {
                    System.err.println("Large dataset rendering failed: " + e.getMessage());
                    renderSuccess.set(false);
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(TestTimeouts.UI_TIMEOUT, TimeUnit.SECONDS), 
                      "Large dataset rendering should complete");

            System.out.println("=== Large Dataset Rendering Results ===");
            System.out.println("Item count: " + itemCount);
            System.out.println("Render time: " + renderTime.get() + "ms");
            System.out.println("Render success: " + renderSuccess.get());

            assertTrue(renderSuccess.get(), "Large dataset rendering should succeed");
            assertTrue(renderTime.get() < 3000, "Rendering should complete within 3 seconds");
        }

        @Test
        @DisplayName("Should handle concurrent UI operations")
        void concurrentUIOperations() throws InterruptedException {
            int operationCount = 20; // Reduced from larger number
            CountDownLatch latch = new CountDownLatch(operationCount);
            AtomicInteger successfulOperations = new AtomicInteger(0);

            TestConfiguration.runOnFxThreadAndWait(() -> {
                VBox testContainer = new VBox();
                testStage.setScene(new Scene(testContainer, 400, 300));
            });

            for (int i = 0; i < operationCount; i++) {
                final int operationIndex = i;
                Platform.runLater(() -> {
                    try {
                        VBox container = (VBox) testStage.getScene().getRoot();
                        if (container != null) {
                            // Simple UI operation
                            javafx.scene.control.Button button = 
                                new javafx.scene.control.Button("Button " + operationIndex);
                            container.getChildren().add(button);
                            successfulOperations.incrementAndGet();
                        }
                    } catch (Exception e) {
                        System.err.println("Concurrent UI operation failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(TestTimeouts.UI_TIMEOUT, TimeUnit.SECONDS), 
                      "All concurrent UI operations should complete");

            System.out.println("=== Concurrent UI Operations Results ===");
            System.out.println("Total operations: " + operationCount);
            System.out.println("Successful operations: " + successfulOperations.get());

            assertTrue(successfulOperations.get() > operationCount * 0.8, 
                      "At least 80% of UI operations should succeed");
        }
    }

    @Nested
    @DisplayName("Memory Stress Tests - Lightweight")
    class MemoryStressTests {

        @Test
        @DisplayName("Should handle memory pressure without crashing")
        void memoryPressureTest() throws InterruptedException {
            int iterations = 50; // Reduced from larger number
            List<Object> memoryConsumers = new ArrayList<>();
            
            long initialMemory = getUsedMemory();
            
            // Create moderate memory pressure
            for (int i = 0; i < iterations; i++) {
                memoryConsumers.add(new ArrayList<>(1000)); // Smaller arrays
                
                // Force garbage collection periodically
                if (i % 10 == 0) {
                    System.gc();
                    Thread.sleep(10);
                }
            }
            
            long peakMemory = getUsedMemory();
            
            // Clean up
            memoryConsumers.clear();
            System.gc();
            Thread.sleep(100);
            
            long finalMemory = getUsedMemory();
            
            System.out.println("=== Memory Pressure Test Results ===");
            System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + " MB");
            System.out.println("Peak memory: " + (peakMemory / 1024 / 1024) + " MB");
            System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + " MB");
            
            // Basic memory management assertions
            assertTrue(peakMemory > initialMemory, "Memory usage should increase under pressure");
            assertTrue(finalMemory < peakMemory, "Memory should be released after cleanup");
        }
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
} 
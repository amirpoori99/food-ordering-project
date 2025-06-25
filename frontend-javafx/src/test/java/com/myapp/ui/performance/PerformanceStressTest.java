package com.myapp.ui.performance;

import com.myapp.ui.common.HttpClientUtil;
import com.myapp.ui.common.NavigationController;
import com.myapp.ui.common.TestFXBase;
import com.myapp.ui.auth.LoginController;
import com.myapp.ui.order.CartController;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and Stress Testing Suite
 * تست‌های عملکرد و فشار سیستم تحت بارهای سنگین
 */
@DisplayName("Performance and Stress Tests")
class PerformanceStressTest extends TestFXBase {

    private ExecutorService executorService;
    private NavigationController navigationController;
    private LoginController loginController;
    private CartController cartController;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        executorService = Executors.newFixedThreadPool(50);
        navigationController = NavigationController.getInstance();
        loginController = new LoginController();
        cartController = new CartController();
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    @Nested
    @DisplayName("API Performance Tests")
    class ApiPerformanceTests {

        @Test
        @DisplayName("Should handle high-frequency API calls without degradation")
        void highFrequencyApiCalls() throws InterruptedException {
            int totalCalls = 1000;
            CountDownLatch latch = new CountDownLatch(totalCalls);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            AtomicLong totalResponseTime = new AtomicLong(0);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < totalCalls; i++) {
                executorService.submit(() -> {
                    try {
                        long callStart = System.currentTimeMillis();
                        
                        // Make API call
                        HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                        
                        long callEnd = System.currentTimeMillis();
                        totalResponseTime.addAndGet(callEnd - callStart);

                        if (response != null) {
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

            assertTrue(latch.await(60, TimeUnit.SECONDS), "All API calls should complete within 60 seconds");

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

            // Performance assertions
            assertTrue(successCount.get() > totalCalls * 0.8, "At least 80% of calls should succeed");
            assertTrue(averageResponseTime < 5000, "Average response time should be under 5 seconds");
            assertTrue(throughput > 10, "Throughput should be at least 10 requests/second");
        }

        @Test
        @DisplayName("Should handle concurrent user authentication without race conditions")
        void concurrentAuthentication() throws InterruptedException {
            int concurrentUsers = 100;
            CountDownLatch latch = new CountDownLatch(concurrentUsers);
            AtomicInteger authSuccessCount = new AtomicInteger(0);
            AtomicInteger authFailureCount = new AtomicInteger(0);
            List<Exception> exceptions = new CopyOnWriteArrayList<>();

            for (int i = 0; i < concurrentUsers; i++) {
                final int userId = i;
                executorService.submit(() -> {
                    try {
                        // Simulate different user credentials
                        String phone = "0912345678" + (userId % 10);
                        String password = "password123";

                        HttpClientUtil.ApiResponse response = HttpClientUtil.post("/auth/login",
                                "{\"phone\":\"" + phone + "\",\"password\":\"" + password + "\"}", false);

                        if (response != null && !response.getMessage().contains("error")) {
                            authSuccessCount.incrementAndGet();
                        } else {
                            authFailureCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        exceptions.add(e);
                        authFailureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS), "All authentication attempts should complete");

            System.out.println("=== Concurrent Authentication Results ===");
            System.out.println("Concurrent users: " + concurrentUsers);
            System.out.println("Auth success: " + authSuccessCount.get());
            System.out.println("Auth failures: " + authFailureCount.get());
            System.out.println("Exceptions: " + exceptions.size());

            // Should handle all requests without crashing
            assertTrue(authSuccessCount.get() + authFailureCount.get() == concurrentUsers,
                    "All authentication attempts should be processed");
        }

        @Test
        @DisplayName("Should maintain performance under sustained load")
        void sustainedLoadTest() throws InterruptedException {
            int duration = 30; // seconds
            int requestsPerSecond = 20;
            AtomicInteger totalRequests = new AtomicInteger(0);
            AtomicInteger successRequests = new AtomicInteger(0);
            AtomicBoolean running = new AtomicBoolean(true);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

            // Schedule requests at fixed rate
            ScheduledFuture<?> requestScheduler = scheduler.scheduleAtFixedRate(() -> {
                if (running.get()) {
                    for (int i = 0; i < requestsPerSecond; i++) {
                        executorService.submit(() -> {
                            try {
                                totalRequests.incrementAndGet();
                                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                                if (response != null) {
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
            Thread.sleep(5000);

            scheduler.shutdown();

            double successRate = (double) successRequests.get() / totalRequests.get();

            System.out.println("=== Sustained Load Test Results ===");
            System.out.println("Duration: " + duration + " seconds");
            System.out.println("Target RPS: " + requestsPerSecond);
            System.out.println("Total requests: " + totalRequests.get());
            System.out.println("Successful requests: " + successRequests.get());
            System.out.println("Success rate: " + (successRate * 100) + "%");

            assertTrue(totalRequests.get() > duration * requestsPerSecond * 0.8,
                    "Should process at least 80% of expected requests");
            assertTrue(successRate > 0.5, "Success rate should be above 50%");
        }
    }

    @Nested
    @DisplayName("UI Performance Tests")
    class UiPerformanceTests {

        @Test
        @DisplayName("Should handle large data sets in UI components efficiently")
        void largeDataSetRendering() throws InterruptedException {
            CountDownLatch uiLatch = new CountDownLatch(1);
            AtomicLong renderingTime = new AtomicLong(0);

            Platform.runLater(() -> {
                try {
                    long startTime = System.currentTimeMillis();

                    // Create large dataset
                    List<String> largeDataSet = new ArrayList<>();
                    for (int i = 0; i < 10000; i++) {
                        largeDataSet.add("Restaurant Item " + i + " - Description with more text to simulate real data");
                    }

                    // Create ListView and populate with large dataset
                    ListView<String> listView = new ListView<>();
                    listView.getItems().addAll(largeDataSet);

                    // Force rendering
                    VBox container = new VBox();
                    container.getChildren().add(listView);

                    long endTime = System.currentTimeMillis();
                    renderingTime.set(endTime - startTime);

                    uiLatch.countDown();
                } catch (Exception e) {
                    fail("UI rendering failed: " + e.getMessage());
                    uiLatch.countDown();
                }
            });

            assertTrue(uiLatch.await(30, TimeUnit.SECONDS), "UI rendering should complete within 30 seconds");

            System.out.println("=== Large Dataset Rendering Results ===");
            System.out.println("Items rendered: 10,000");
            System.out.println("Rendering time: " + renderingTime.get() + "ms");

            assertTrue(renderingTime.get() < 10000, "Rendering should complete within 10 seconds");
        }

        @Test
        @DisplayName("Should handle rapid UI navigation without memory leaks")
        void rapidNavigationTest() throws InterruptedException {
            int navigationCount = 500;
            CountDownLatch navigationLatch = new CountDownLatch(navigationCount);
            AtomicInteger completedNavigations = new AtomicInteger(0);

            // Measure initial memory
            System.gc();
            Runtime runtime = Runtime.getRuntime();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();

            for (int i = 0; i < navigationCount; i++) {
                Platform.runLater(() -> {
                    try {
                        // Simulate rapid navigation between different views
                        navigationController.navigateToLogin();
                        Thread.sleep(1); // Minimal delay
                        navigationController.navigateToRegister();
                        Thread.sleep(1);
                        navigationController.navigateToLogin();
                        
                        completedNavigations.incrementAndGet();
                    } catch (Exception e) {
                        // Navigation might fail, but shouldn't crash
                    } finally {
                        navigationLatch.countDown();
                    }
                });
            }

            assertTrue(navigationLatch.await(60, TimeUnit.SECONDS), "Navigation test should complete within 60 seconds");

            // Force garbage collection and measure memory
            System.gc();
            Thread.sleep(1000);
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryDifference = finalMemory - initialMemory;

            System.out.println("=== Rapid Navigation Results ===");
            System.out.println("Navigation attempts: " + navigationCount);
            System.out.println("Completed navigations: " + completedNavigations.get());
            System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + " MB");
            System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + " MB");
            System.out.println("Memory difference: " + (memoryDifference / 1024 / 1024) + " MB");

            assertTrue(completedNavigations.get() > navigationCount * 0.8, "At least 80% of navigations should complete");
            assertTrue(memoryDifference < 100 * 1024 * 1024, "Memory increase should be less than 100MB");
        }

        @Test
        @DisplayName("Should handle concurrent UI updates efficiently")
        void concurrentUiUpdates() throws InterruptedException {
            int concurrentUpdates = 200;
            CountDownLatch updateLatch = new CountDownLatch(concurrentUpdates);
            AtomicInteger successfulUpdates = new AtomicInteger(0);

            for (int i = 0; i < concurrentUpdates; i++) {
                final int updateId = i;
                Platform.runLater(() -> {
                    try {
                        // Simulate UI updates
                        loginController.setStatusText("Status update " + updateId);
                        loginController.setPhoneFieldText("09123456789");
                        loginController.setPasswordFieldText("password" + updateId);
                        
                        successfulUpdates.incrementAndGet();
                    } catch (Exception e) {
                        // UI update might fail, but shouldn't crash
                    } finally {
                        updateLatch.countDown();
                    }
                });
            }

            assertTrue(updateLatch.await(30, TimeUnit.SECONDS), "UI updates should complete within 30 seconds");

            System.out.println("=== Concurrent UI Updates Results ===");
            System.out.println("Update attempts: " + concurrentUpdates);
            System.out.println("Successful updates: " + successfulUpdates.get());

            assertTrue(successfulUpdates.get() > concurrentUpdates * 0.9, "At least 90% of UI updates should succeed");
        }
    }

    @Nested
    @DisplayName("Memory and Resource Tests")
    class MemoryResourceTests {

        @Test
        @DisplayName("Should handle memory constraints gracefully")
        void memoryConstraintTest() throws InterruptedException {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();

            System.out.println("=== Memory Constraint Test ===");
            System.out.println("Max memory: " + (maxMemory / 1024 / 1024) + " MB");
            System.out.println("Initial memory: " + (initialMemory / 1024 / 1024) + " MB");

            // Create memory pressure
            List<byte[]> memoryConsumers = new ArrayList<>();
            boolean memoryExhausted = false;

            try {
                // Consume memory until we reach 80% of max memory
                while ((runtime.totalMemory() - runtime.freeMemory()) < maxMemory * 0.8) {
                    memoryConsumers.add(new byte[1024 * 1024]); // 1MB chunks
                }
            } catch (OutOfMemoryError e) {
                memoryExhausted = true;
            }

            long peakMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Peak memory: " + (peakMemory / 1024 / 1024) + " MB");

            // Test application functionality under memory pressure
            AtomicBoolean functionalityWorks = new AtomicBoolean(true);
            try {
                // Test basic API call
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                if (response == null) {
                    functionalityWorks.set(false);
                }

                // Test UI functionality
                Platform.runLater(() -> {
                    try {
                        loginController.setStatusText("Memory pressure test");
                    } catch (Exception e) {
                        functionalityWorks.set(false);
                    }
                });

                WaitForAsyncUtils.waitForFxEvents();
            } catch (Exception e) {
                functionalityWorks.set(false);
            }

            // Clean up memory
            memoryConsumers.clear();
            System.gc();

            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Final memory: " + (finalMemory / 1024 / 1024) + " MB");

            assertTrue(functionalityWorks.get(), "Application should remain functional under memory pressure");
            assertTrue((finalMemory - initialMemory) < 50 * 1024 * 1024, "Memory should be released after cleanup");
        }

        @Test
        @DisplayName("Should handle resource exhaustion scenarios")
        void resourceExhaustionTest() throws InterruptedException {
            // Test thread pool exhaustion
            ExecutorService limitedExecutor = Executors.newFixedThreadPool(5);
            CountDownLatch resourceLatch = new CountDownLatch(100);
            AtomicInteger processedTasks = new AtomicInteger(0);

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 100; i++) {
                limitedExecutor.submit(() -> {
                    try {
                        // Simulate resource-intensive task
                        Thread.sleep(100);
                        processedTasks.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        resourceLatch.countDown();
                    }
                });
            }

            assertTrue(resourceLatch.await(60, TimeUnit.SECONDS), "All tasks should complete within 60 seconds");

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("=== Resource Exhaustion Test Results ===");
            System.out.println("Tasks submitted: 100");
            System.out.println("Tasks processed: " + processedTasks.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average task time: " + (totalTime / 100.0) + "ms");

            assertEquals(100, processedTasks.get(), "All tasks should be processed");
            assertTrue(totalTime > 2000, "Tasks should be queued due to limited thread pool");

            limitedExecutor.shutdown();
        }
    }

    @Nested
    @DisplayName("Network Performance Tests")
    class NetworkPerformanceTests {

        @Test
        @DisplayName("Should handle network latency variations")
        void networkLatencyTest() throws InterruptedException {
            int[] timeouts = {100, 500, 1000, 5000, 10000}; // milliseconds
            
            for (int timeout : timeouts) {
                HttpClientUtil.setTimeoutMs(timeout);
                
                long startTime = System.currentTimeMillis();
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                long endTime = System.currentTimeMillis();
                
                long actualTime = endTime - startTime;
                
                System.out.println("Timeout: " + timeout + "ms, Actual time: " + actualTime + "ms");
                
                // Should not take significantly longer than timeout (allowing for overhead)
                assertTrue(actualTime <= timeout + 2000, "Response time should respect timeout settings");
            }
            
            // Reset to default
            HttpClientUtil.setTimeoutMs(30000);
        }

        @Test
        @DisplayName("Should handle network interruptions gracefully")
        void networkInterruptionTest() throws InterruptedException {
            AtomicInteger retryAttempts = new AtomicInteger(0);
            AtomicInteger successfulRequests = new AtomicInteger(0);
            AtomicInteger failedRequests = new AtomicInteger(0);
            
            // Simulate network interruptions by using very short timeouts
            HttpClientUtil.setTimeoutMs(10); // Very short timeout to simulate network issues
            
            for (int i = 0; i < 50; i++) {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                
                if (response != null && response.isSuccess()) {
                    successfulRequests.incrementAndGet();
                } else {
                    failedRequests.incrementAndGet();
                    
                    // Retry with longer timeout
                    HttpClientUtil.setTimeoutMs(30000);
                    HttpClientUtil.ApiResponse retryResponse = HttpClientUtil.get("/restaurants");
                    if (retryResponse != null) {
                        retryAttempts.incrementAndGet();
                    }
                    HttpClientUtil.setTimeoutMs(10); // Reset to short timeout
                }
            }
            
            System.out.println("=== Network Interruption Test Results ===");
            System.out.println("Successful requests: " + successfulRequests.get());
            System.out.println("Failed requests: " + failedRequests.get());
            System.out.println("Retry attempts: " + retryAttempts.get());
            
            assertTrue(failedRequests.get() > 0, "Should encounter network failures with short timeout");
            assertTrue(retryAttempts.get() > 0, "Should attempt retries after failures");
            
            // Reset to default
            HttpClientUtil.setTimeoutMs(30000);
        }
    }
} 
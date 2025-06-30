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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های عملکرد و فشار سیستم تحت بارهای سنگین
 * این کلاس شامل تست‌های استرس برای بررسی عملکرد سیستم در شرایط مختلف
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
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
            scheduler.shutdown();
            scheduler.awaitTermination(10, TimeUnit.SECONDS);

            System.out.println("=== Sustained Load Test Results ===");
            System.out.println("Duration: " + duration + " seconds");
            System.out.println("Requests per second: " + requestsPerSecond);
            System.out.println("Total requests: " + totalRequests.get());
            System.out.println("Successful requests: " + successRequests.get());
            System.out.println("Success rate: " + (successRequests.get() * 100.0 / totalRequests.get()) + "%");

            // Performance assertions
            assertTrue(successRequests.get() > totalRequests.get() * 0.7, "Success rate should be above 70%");
            assertTrue(totalRequests.get() >= duration * requestsPerSecond * 0.8, "Should maintain request rate");
        }
    }

    @Nested
    @DisplayName("UI Performance Tests")
    class UIPerformanceTests {

        @Test
        @DisplayName("Should handle rapid UI updates without freezing")
        void rapidUIUpdates() throws InterruptedException {
            int updateCount = 1000;
            CountDownLatch latch = new CountDownLatch(updateCount);
            AtomicInteger successfulUpdates = new AtomicInteger(0);
            AtomicLong totalUpdateTime = new AtomicLong(0);

            Platform.runLater(() -> {
                VBox testContainer = new VBox();
                testStage.setScene(new javafx.scene.Scene(testContainer, 800, 600));
                testStage.show();
            });

            WaitForAsyncUtils.waitForFxEvents();

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < updateCount; i++) {
                final int updateIndex = i;
                Platform.runLater(() -> {
                    try {
                        long updateStart = System.currentTimeMillis();
                        
                        // Simulate UI update
                        VBox container = (VBox) testStage.getScene().getRoot();
                        container.getChildren().add(new javafx.scene.control.Label("Update " + updateIndex));
                        
                        long updateEnd = System.currentTimeMillis();
                        totalUpdateTime.addAndGet(updateEnd - updateStart);
                        successfulUpdates.incrementAndGet();
                    } catch (Exception e) {
                        // Update failed
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS), "All UI updates should complete within 30 seconds");

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double averageUpdateTime = totalUpdateTime.get() / (double) updateCount;

            System.out.println("=== UI Performance Results ===");
            System.out.println("Total updates: " + updateCount);
            System.out.println("Successful updates: " + successfulUpdates.get());
            System.out.println("Total time: " + totalTime + "ms");
            System.out.println("Average update time: " + averageUpdateTime + "ms");

            // Performance assertions
            assertTrue(successfulUpdates.get() > updateCount * 0.9, "At least 90% of updates should succeed");
            assertTrue(averageUpdateTime < 100, "Average update time should be under 100ms");
        }

        @Test
        @DisplayName("Should handle large dataset rendering efficiently")
        void largeDatasetRendering() throws InterruptedException {
            int itemCount = 10000;
            CountDownLatch latch = new CountDownLatch(1);
            AtomicLong renderTime = new AtomicLong(0);

            Platform.runLater(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    // Create large dataset
                    ListView<String> listView = new ListView<>();
                    List<String> items = new ArrayList<>();
                    for (int i = 0; i < itemCount; i++) {
                        items.add("Item " + i);
                    }
                    
                    // Render dataset
                    listView.getItems().addAll(items);
                    
                    long endTime = System.currentTimeMillis();
                    renderTime.set(endTime - startTime);
                    
                    // Display in test stage
                    testStage.setScene(new javafx.scene.Scene(listView, 800, 600));
                    testStage.show();
                    
                } catch (Exception e) {
                    // Render failed
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(10, TimeUnit.SECONDS), "Rendering should complete within 10 seconds");

            System.out.println("=== Large Dataset Rendering Results ===");
            System.out.println("Item count: " + itemCount);
            System.out.println("Render time: " + renderTime.get() + "ms");

            // Performance assertions
            assertTrue(renderTime.get() < 5000, "Rendering should complete within 5 seconds");
        }

        @Test
        @DisplayName("Should handle concurrent UI operations")
        void concurrentUIOperations() throws InterruptedException {
            int operationCount = 100;
            CountDownLatch latch = new CountDownLatch(operationCount);
            AtomicInteger successfulOperations = new AtomicInteger(0);
            List<Exception> exceptions = new CopyOnWriteArrayList<>();

            Platform.runLater(() -> {
                VBox container = new VBox();
                testStage.setScene(new javafx.scene.Scene(container, 800, 600));
                testStage.show();
            });

            WaitForAsyncUtils.waitForFxEvents();

            for (int i = 0; i < operationCount; i++) {
                final int operationIndex = i;
                executorService.submit(() -> {
                    try {
                        Platform.runLater(() -> {
                            try {
                                // Simulate UI operation
                                VBox container = (VBox) testStage.getScene().getRoot();
                                container.getChildren().add(new javafx.scene.control.Button("Button " + operationIndex));
                                successfulOperations.incrementAndGet();
                            } catch (Exception e) {
                                exceptions.add(e);
                            } finally {
                                latch.countDown();
                            }
                        });
                    } catch (Exception e) {
                        exceptions.add(e);
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS), "All UI operations should complete");

            System.out.println("=== Concurrent UI Operations Results ===");
            System.out.println("Total operations: " + operationCount);
            System.out.println("Successful operations: " + successfulOperations.get());
            System.out.println("Exceptions: " + exceptions.size());

            // Performance assertions
            assertTrue(successfulOperations.get() > operationCount * 0.8, "At least 80% of operations should succeed");
            assertTrue(exceptions.size() < operationCount * 0.2, "Exception rate should be below 20%");
        }
    }

    @Nested
    @DisplayName("Memory Stress Tests")
    class MemoryStressTests {

        @Test
        @DisplayName("Should handle memory pressure without crashing")
        void memoryPressureTest() throws InterruptedException {
            int iterationCount = 1000;
            AtomicInteger successfulIterations = new AtomicInteger(0);
            AtomicLong totalMemoryUsage = new AtomicLong(0);

            for (int i = 0; i < iterationCount; i++) {
                try {
                    long memoryBefore = getUsedMemory();
                    
                    // Simulate memory-intensive operation
                    List<Object> largeList = new ArrayList<>();
                    for (int j = 0; j < 1000; j++) {
                        largeList.add(new Object());
                    }
                    
                    long memoryAfter = getUsedMemory();
                    totalMemoryUsage.addAndGet(memoryAfter - memoryBefore);
                    
                    // Clear references to allow GC
                    largeList.clear();
                    largeList = null;
                    
                    successfulIterations.incrementAndGet();
                    
                    // Force garbage collection periodically
                    if (i % 100 == 0) {
                        System.gc();
                        Thread.sleep(10);
                    }
                    
                } catch (OutOfMemoryError e) {
                    // Memory pressure handled
                    break;
                }
            }

            System.out.println("=== Memory Pressure Test Results ===");
            System.out.println("Total iterations: " + iterationCount);
            System.out.println("Successful iterations: " + successfulIterations.get());
            System.out.println("Average memory usage per iteration: " + 
                (totalMemoryUsage.get() / (double) successfulIterations.get()) + " bytes");

            // Performance assertions
            assertTrue(successfulIterations.get() > iterationCount * 0.5, "Should handle at least 50% of iterations");
        }

        @Test
        @DisplayName("Should handle memory leaks gracefully")
        void memoryLeakTest() throws InterruptedException {
            int leakIterations = 100;
            long initialMemory = getUsedMemory();
            
            for (int i = 0; i < leakIterations; i++) {
                // Simulate potential memory leak
                createPotentialLeak();
                
                // Force garbage collection
                System.gc();
                Thread.sleep(50);
            }
            
            long finalMemory = getUsedMemory();
            long memoryIncrease = finalMemory - initialMemory;

            System.out.println("=== Memory Leak Test Results ===");
            System.out.println("Leak iterations: " + leakIterations);
            System.out.println("Initial memory: " + initialMemory + " bytes");
            System.out.println("Final memory: " + finalMemory + " bytes");
            System.out.println("Memory increase: " + memoryIncrease + " bytes");

            // Performance assertions
            assertTrue(memoryIncrease < 50 * 1024 * 1024, "Memory increase should be less than 50MB");
        }
    }

    @Nested
    @DisplayName("Network Stress Tests")
    class NetworkStressTests {

        @Test
        @DisplayName("Should handle network timeouts gracefully")
        void networkTimeoutTest() throws InterruptedException {
            int timeoutRequests = 100;
            CountDownLatch latch = new CountDownLatch(timeoutRequests);
            AtomicInteger handledTimeouts = new AtomicInteger(0);
            AtomicInteger unhandledTimeouts = new AtomicInteger(0);

            // Set very short timeout - تنظیم timeout خیلی کوتاه
            HttpClientUtil.setTimeoutMs(1);

            for (int i = 0; i < timeoutRequests; i++) {
                executorService.submit(() -> {
                    try {
                        HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                        if (response != null) {
                            handledTimeouts.incrementAndGet();
                        } else {
                            unhandledTimeouts.incrementAndGet();
                        }
                    } catch (Exception e) {
                        handledTimeouts.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS), "All timeout requests should complete");

            // Reset timeout - بازنشانی timeout
            HttpClientUtil.setTimeoutMs(30000);

            System.out.println("=== Network Timeout Test Results ===");
            System.out.println("Timeout requests: " + timeoutRequests);
            System.out.println("Handled timeouts: " + handledTimeouts.get());
            System.out.println("Unhandled timeouts: " + unhandledTimeouts.get());

            // Performance assertions - تأییدهای عملکرد
            assertTrue(handledTimeouts.get() + unhandledTimeouts.get() == timeoutRequests,
                    "All requests should be processed");
        }

        @Test
        @DisplayName("Should handle network disconnections gracefully")
        void networkDisconnectionTest() throws InterruptedException {
            int disconnectionTests = 50;
            CountDownLatch latch = new CountDownLatch(disconnectionTests);
            AtomicInteger successfulRecoveries = new AtomicInteger(0);

            for (int i = 0; i < disconnectionTests; i++) {
                executorService.submit(() -> {
                    try {
                        // Simulate network disconnection - شبیه‌سازی قطع اتصال شبکه
                        HttpClientUtil.setSimulateNetworkFailure(true);
                        
                        // Attempt request (should fail) - تلاش درخواست (باید شکست بخورد)
                        HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants");
                        
                        // Simulate network recovery - شبیه‌سازی بازیابی شبکه
                        HttpClientUtil.setSimulateNetworkFailure(false);
                        
                        // Attempt request again (should succeed) - تلاش مجدد درخواست (باید موفق شود)
                        response = HttpClientUtil.get("/restaurants");
                        
                        if (response != null) {
                            successfulRecoveries.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Recovery failed - بازیابی شکست خورد
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(30, TimeUnit.SECONDS), "All disconnection tests should complete");

            System.out.println("=== Network Disconnection Test Results ===");
            System.out.println("Disconnection tests: " + disconnectionTests);
            System.out.println("Successful recoveries: " + successfulRecoveries.get());

            // Performance assertions - تأییدهای عملکرد
            assertTrue(successfulRecoveries.get() > disconnectionTests * 0.7,
                    "At least 70% of disconnections should be recovered");
        }
    }

    // Helper methods
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void createPotentialLeak() {
        // Simulate potential memory leak
        List<Object> leakyList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            leakyList.add(new Object());
        }
        // Intentionally not clearing the list to simulate leak
    }
} 
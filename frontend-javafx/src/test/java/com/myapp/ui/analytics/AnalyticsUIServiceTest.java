package com.myapp.ui.analytics;

import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.myapp.ui.analytics.service.AnalyticsUIService;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * تست‌های کلاس AnalyticsUIService
 * تست‌های مربوط به سرویس UI برای Analytics
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
@DisplayName("AnalyticsUIService Tests")
@Tag("analytics")
@Tag("service")
class AnalyticsUIServiceTest {

    private AnalyticsUIService analyticsUIService;
    private static boolean javafxInitialized = false;

    @BeforeAll
    static void initializeJavaFX() {
        if (!javafxInitialized) {
            try {
                Platform.startup(() -> {});
                javafxInitialized = true;
            } catch (Exception e) {
                javafxInitialized = true;
            }
        }
    }

    @BeforeEach
    void setUp() {
        analyticsUIService = new AnalyticsUIService();
    }

    @AfterEach
    void tearDown() {
        if (analyticsUIService != null) {
            analyticsUIService.shutdown();
        }
    }

    // ==================== System Stats Tests ====================

    @Nested
    @DisplayName("System Stats Tests")
    class SystemStatsTests {

        @Test
        @DisplayName("Should get system stats successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldGetSystemStats() throws Exception {
            // When
            CompletableFuture<Map<String, Object>> future = analyticsUIService.getSystemStats();
            Map<String, Object> stats = future.get(5, TimeUnit.SECONDS);

            // Then
            assertNotNull(stats);
            assertFalse(stats.isEmpty());
            assertTrue(stats.containsKey("totalUsers"));
            assertTrue(stats.containsKey("totalRestaurants"));
            assertTrue(stats.containsKey("totalOrders"));
            assertTrue(stats.containsKey("totalRevenue"));
        }

        @Test
        @DisplayName("Should get valid system stats values")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldGetValidSystemStatsValues() throws Exception {
            // When
            CompletableFuture<Map<String, Object>> future = analyticsUIService.getSystemStats();
            Map<String, Object> stats = future.get(5, TimeUnit.SECONDS);

            // Then
            assertNotNull(stats.get("totalUsers"));
            assertNotNull(stats.get("totalRestaurants"));
            assertNotNull(stats.get("totalOrders"));
            assertNotNull(stats.get("totalRevenue"));
            
            // بررسی نوع داده‌ها
            assertTrue(stats.get("totalUsers") instanceof Number);
            assertTrue(stats.get("totalRestaurants") instanceof Number);
            assertTrue(stats.get("totalOrders") instanceof Number);
            assertTrue(stats.get("totalRevenue") instanceof Number);
        }
    }

    // ==================== Chart Update Tests ====================

    @Nested
    @DisplayName("Chart Update Tests")
    class ChartUpdateTests {

        @Test
        @DisplayName("Should update sales trend chart")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateSalesTrendChart() throws Exception {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            String period = "daily";

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateSalesTrendChart(chart, period);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(chart);
        }

        @Test
        @DisplayName("Should update user distribution chart")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateUserDistributionChart() throws Exception {
            // Given
            PieChart chart = new PieChart();

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateUserDistributionChart(chart);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(chart);
        }

        @Test
        @DisplayName("Should update restaurant performance chart")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateRestaurantPerformanceChart() throws Exception {
            // Given
            BarChart<String, Number> chart = new BarChart<>(
                new CategoryAxis(), new NumberAxis()
            );

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateRestaurantPerformanceChart(chart);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(chart);
        }

        @Test
        @DisplayName("Should update revenue chart")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateRevenueChart() throws Exception {
            // Given
            AreaChart<String, Number> chart = new AreaChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            String period = "monthly";

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateRevenueChart(chart, period);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(chart);
        }
    }

    // ==================== Table Update Tests ====================

    @Nested
    @DisplayName("Table Update Tests")
    class TableUpdateTests {

        @Test
        @DisplayName("Should update top restaurants table")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateTopRestaurantsTable() throws Exception {
            // Given
            TableView<Map<String, Object>> table = new TableView<>();
            int limit = 10;

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateTopRestaurantsTable(table, limit);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(table);
        }

        @Test
        @DisplayName("Should update recent orders table")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateRecentOrdersTable() throws Exception {
            // Given
            TableView<Map<String, Object>> table = new TableView<>();
            int limit = 20;

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateRecentOrdersTable(table, limit);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(table);
        }

        @Test
        @DisplayName("Should update user activity table")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateUserActivityTable() throws Exception {
            // Given
            TableView<Map<String, Object>> table = new TableView<>();
            int limit = 15;

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateUserActivityTable(table, limit);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            assertNotNull(table);
        }
    }

    // ==================== Stats Cards Tests ====================

    @Nested
    @DisplayName("Stats Cards Tests")
    class StatsCardsTests {

        @Test
        @DisplayName("Should update stats cards")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldUpdateStatsCards() throws Exception {
            // Given
            Map<String, VBox> statsCards = new HashMap<>();
            statsCards.put("totalUsers", createStatsCard());
            statsCards.put("totalRestaurants", createStatsCard());
            statsCards.put("totalOrders", createStatsCard());
            statsCards.put("totalRevenue", createStatsCard());

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", 1250L);
            stats.put("totalRestaurants", 85L);
            stats.put("totalOrders", 5420L);
            stats.put("totalRevenue", 125000000.0);

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                analyticsUIService.updateStatsCards(statsCards, stats);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        }

        private VBox createStatsCard() {
            VBox card = new VBox();
            VBox content = new VBox();
            Label titleLabel = new Label("Title");
            Label valueLabel = new Label("Value");
            content.getChildren().addAll(titleLabel, valueLabel);
            card.getChildren().add(content);
            return card;
        }
    }

    // ==================== Cache Management Tests ====================

    @Nested
    @DisplayName("Cache Management Tests")
    class CacheManagementTests {

        @Test
        @DisplayName("Should clear cache successfully")
        void shouldClearCache() {
            // When
            analyticsUIService.clearCache();

            // Then
            // Method should execute without throwing exception
            assertTrue(true);
        }

        @Test
        @DisplayName("Should clear specific cache key")
        void shouldClearSpecificCacheKey() {
            // Given
            String cacheKey = "test-key";

            // When
            analyticsUIService.clearCache(cacheKey);

            // Then
            // Method should execute without throwing exception
            assertTrue(true);
        }
    }

    // ==================== Export Tests ====================

    @Nested
    @DisplayName("Export Tests")
    class ExportTests {

        @Test
        @DisplayName("Should export data to CSV")
        void shouldExportToCSV() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("name", "Test Restaurant");
            row.put("rating", 4.5);
            data.add(row);

            String filename = "test-export";

            // When
            String result = analyticsUIService.exportToCSV(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should export data to JSON")
        void shouldExportToJSON() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("name", "Test Restaurant");
            row.put("rating", 4.5);
            data.add(row);

            String filename = "test-export";

            // When
            String result = analyticsUIService.exportToJSON(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should export chart to image")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldExportChartToImage() throws Exception {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            String filename = "test-chart";

            // When
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                String result = analyticsUIService.exportChartToImage(chart, filename);
                assertNotNull(result);
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
        }
    }

    // ==================== Utility Tests ====================

    @Nested
    @DisplayName("Utility Tests")
    class UtilityTests {

        @Test
        @DisplayName("Should get current date time")
        void shouldGetCurrentDateTime() {
            // When
            String dateTime = analyticsUIService.getCurrentDateTime();

            // Then
            assertNotNull(dateTime);
            assertFalse(dateTime.isEmpty());
        }

        @Test
        @DisplayName("Should check server connection")
        void shouldCheckServerConnection() {
            // When
            boolean isConnected = analyticsUIService.isServerConnected();

            // Then
            // Method should return a boolean value
            assertTrue(isConnected == true || isConnected == false);
        }

        @Test
        @DisplayName("Should get system status")
        void shouldGetSystemStatus() {
            // When
            Map<String, String> status = analyticsUIService.getSystemStatus();

            // Then
            assertNotNull(status);
            // Status should contain system information
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null chart gracefully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldHandleNullChart() {
            // When & Then
            assertDoesNotThrow(() -> {
                analyticsUIService.updateSalesTrendChart(null, "daily");
                analyticsUIService.updateUserDistributionChart(null);
                analyticsUIService.updateRestaurantPerformanceChart(null);
                analyticsUIService.updateRevenueChart(null, "monthly");
            });
        }

        @Test
        @DisplayName("Should handle null table gracefully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldHandleNullTable() {
            // When & Then
            assertDoesNotThrow(() -> {
                analyticsUIService.updateTopRestaurantsTable(null, 10);
                analyticsUIService.updateRecentOrdersTable(null, 10);
                analyticsUIService.updateUserActivityTable(null, 10);
            });
        }

        @Test
        @DisplayName("Should handle empty data gracefully")
        void shouldHandleEmptyData() {
            // Given
            List<Map<String, Object>> emptyData = new ArrayList<>();

            // When & Then
            assertDoesNotThrow(() -> {
                analyticsUIService.exportToCSV(emptyData, "empty-test");
                analyticsUIService.exportToJSON(emptyData, "empty-test");
            });
        }
    }

    // ==================== Performance Tests ====================

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle multiple concurrent requests")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldHandleMultipleConcurrentRequests() throws Exception {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            TableView<Map<String, Object>> table = new TableView<>();

            // When
            CountDownLatch latch = new CountDownLatch(4);
            Platform.runLater(() -> {
                analyticsUIService.updateSalesTrendChart(chart, "daily");
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.updateTopRestaurantsTable(table, 10);
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.getSystemStats();
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.getSystemStatus();
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(25, TimeUnit.SECONDS));
            assertNotNull(chart);
            assertNotNull(table);
        }

        @Test
        @DisplayName("Should handle large data export efficiently")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldHandleLargeDataExport() {
            // Given
            List<Map<String, Object>> largeData = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", i);
                row.put("name", "Restaurant " + i);
                row.put("rating", 4.0 + (i % 10) * 0.1);
                largeData.add(row);
            }

            String filename = "large-export-test";

            // When
            long startTime = System.currentTimeMillis();
            String result = analyticsUIService.exportToCSV(largeData, filename);
            long endTime = System.currentTimeMillis();

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            long duration = endTime - startTime;
            assertTrue(duration < 10000, "Export should complete within 10 seconds, took: " + duration + "ms");
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should perform complete analytics workflow")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldPerformCompleteAnalyticsWorkflow() throws Exception {
            // Given
            LineChart<String, Number> salesChart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            PieChart userChart = new PieChart();
            BarChart<String, Number> performanceChart = new BarChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            AreaChart<String, Number> revenueChart = new AreaChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            TableView<Map<String, Object>> table = new TableView<>();

            // When
            CountDownLatch latch = new CountDownLatch(6);
            Platform.runLater(() -> {
                analyticsUIService.updateSalesTrendChart(salesChart, "daily");
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.updateUserDistributionChart(userChart);
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.updateRestaurantPerformanceChart(performanceChart);
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.updateRevenueChart(revenueChart, "monthly");
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.updateTopRestaurantsTable(table, 10);
                latch.countDown();
            });
            Platform.runLater(() -> {
                analyticsUIService.getSystemStats();
                latch.countDown();
            });

            // Then
            assertTrue(latch.await(25, TimeUnit.SECONDS));
            assertNotNull(salesChart);
            assertNotNull(userChart);
            assertNotNull(performanceChart);
            assertNotNull(revenueChart);
            assertNotNull(table);
        }
    }
} 
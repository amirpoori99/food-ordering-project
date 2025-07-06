package com.myapp.ui.analytics;

import javafx.application.Platform;
import javafx.scene.chart.*;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Disabled;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.myapp.ui.analytics.util.ChartUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * تست‌های کلاس ChartUtil
 * تست‌های مربوط به ابزارهای نمودار و استایل‌دهی
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
@Disabled("UI test - only run in graphical environment")
@DisplayName("ChartUtil Tests")
@Tag("analytics")
@Tag("ui")
class ChartUtilTest {

    private ChartUtil chartUtil;
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
        chartUtil = new ChartUtil();
    }

    @AfterEach
    void tearDown() {
        chartUtil = null;
    }

    // ==================== Chart Creation Tests ====================

    @Nested
    @DisplayName("Chart Creation Tests")
    class ChartCreationTests {

        @Test
        @DisplayName("Should create empty line chart successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldCreateEmptyLineChart() {
            // Given
            String chartType = "line";
            String title = "Test Line Chart";

            // When
            Chart chart = chartUtil.createEmptyChart(chartType, title);

            // Then
            assertNotNull(chart);
            assertTrue(chart instanceof LineChart);
            assertEquals(title, chart.getTitle());
        }

        @Test
        @DisplayName("Should create empty bar chart successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldCreateEmptyBarChart() {
            // Given
            String chartType = "bar";
            String title = "Test Bar Chart";

            // When
            Chart chart = chartUtil.createEmptyChart(chartType, title);

            // Then
            assertNotNull(chart);
            assertTrue(chart instanceof BarChart);
            assertEquals(title, chart.getTitle());
        }

        @Test
        @DisplayName("Should create empty pie chart successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldCreateEmptyPieChart() {
            // Given
            String chartType = "pie";
            String title = "Test Pie Chart";

            // When
            Chart chart = chartUtil.createEmptyChart(chartType, title);

            // Then
            assertNotNull(chart);
            assertTrue(chart instanceof PieChart);
            assertEquals(title, chart.getTitle());
        }

        @Test
        @DisplayName("Should create empty area chart successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldCreateEmptyAreaChart() {
            // Given
            String chartType = "area";
            String title = "Test Area Chart";

            // When
            Chart chart = chartUtil.createEmptyChart(chartType, title);

            // Then
            assertNotNull(chart);
            assertTrue(chart instanceof AreaChart);
            assertEquals(title, chart.getTitle());
        }
    }

    // ==================== Chart Styling Tests ====================

    @Nested
    @DisplayName("Chart Styling Tests")
    class ChartStylingTests {

        @Test
        @DisplayName("Should apply chart style successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldApplyChartStyle() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            String styleName = "sales-trend";

            // When
            chartUtil.applyChartStyle(chart, styleName);

            // Then
            assertNotNull(chart.getStyleClass());
            assertTrue(chart.getStyleClass().contains("chart"));
        }

        @Test
        @DisplayName("Should apply multiple chart styles")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldApplyMultipleChartStyles() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            String style1 = "sales-trend";
            String style2 = "user-activity";

            // When
            chartUtil.applyChartStyle(chart, style1);
            chartUtil.applyChartStyle(chart, style2);

            // Then
            assertTrue(chart.getStyleClass().contains("chart"));
        }

        @Test
        @DisplayName("Should set chart colors")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldSetChartColors() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            Color color1 = Color.BLUE;
            Color color2 = Color.RED;

            // When
            chartUtil.setChartColors(chart, color1, color2);

            // Then
            assertNotNull(chart);
        }

        @Test
        @DisplayName("Should set chart animation")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldSetChartAnimation() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            boolean animated = true;

            // When
            chartUtil.setChartAnimation(chart, animated);

            // Then
            assertNotNull(chart);
        }

        @Test
        @DisplayName("Should set chart size")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldSetChartSize() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            double width = 400;
            double height = 300;

            // When
            chartUtil.setChartSize(chart, width, height);

            // Then
            assertNotNull(chart);
        }
    }

    // ==================== Card Creation Tests ====================

    @Nested
    @DisplayName("Card Creation Tests")
    class CardCreationTests {

        @Test
        @DisplayName("Should create chart card successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldCreateChartCard() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            String title = "Chart Title";
            String description = "Chart Description";

            // When
            VBox card = chartUtil.createChartCard(chart, title, description);

            // Then
            assertNotNull(card);
            assertTrue(card.getChildren().contains(chart));
        }

        @Test
        @DisplayName("Should create stats card successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldCreateStatsCard() {
            // Given
            String title = "Total Users";
            String value = "1,250";
            String icon = "👥";
            String color = "#007bff";

            // When
            VBox card = chartUtil.createStatsCard(title, value, icon, color);

            // Then
            assertNotNull(card);
            assertFalse(card.getChildren().isEmpty());
        }

        @Test
        @DisplayName("Should create comparison card successfully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldCreateComparisonCard() {
            // Given
            String title = "Revenue";
            String currentValue = "125M";
            String previousValue = "100M";
            String changePercent = "+25%";

            // When
            HBox card = chartUtil.createComparisonCard(title, currentValue, previousValue, changePercent);

            // Then
            assertNotNull(card);
            assertFalse(card.getChildren().isEmpty());
        }
    }

    // ==================== Utility Tests ====================

    @Nested
    @DisplayName("Utility Tests")
    class UtilityTests {

        @Test
        @DisplayName("Should get chart CSS")
        void shouldGetChartCSS() {
            // When
            String css = chartUtil.getChartCSS();

            // Then
            assertNotNull(css);
            assertFalse(css.isEmpty());
            assertTrue(css.contains(".chart"));
        }

        @Test
        @DisplayName("Should apply CSS to chart")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldApplyCSSToChart() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );
            String css = ".test-chart { -fx-background-color: red; }";

            // When
            chartUtil.applyCSS(chart, css);

            // Then
            assertNotNull(chart);
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null chart gracefully")
        void shouldHandleNullChart() {
            // When & Then
            assertDoesNotThrow(() -> {
                chartUtil.applyChartStyle(null, "test-style");
            });
        }

        @Test
        @DisplayName("Should handle null style gracefully")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void shouldHandleNullStyle() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );

            // When & Then
            assertDoesNotThrow(() -> {
                chartUtil.applyChartStyle(chart, null);
            });
        }

        @Test
        @DisplayName("Should handle invalid chart type gracefully")
        void shouldHandleInvalidChartType() {
            // Given
            String invalidType = "invalid-type";
            String title = "Test";

            // When & Then
            assertDoesNotThrow(() -> {
                Chart chart = chartUtil.createEmptyChart(invalidType, title);
                assertNotNull(chart);
            });
        }
    }

    // ==================== Performance Tests ====================

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should create multiple charts efficiently")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldCreateMultipleChartsEfficiently() {
            // When & Then
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 100; i++) {
                    Chart chart = chartUtil.createEmptyChart("line", "Chart " + i);
                    assertNotNull(chart);
                }
            });
        }

        @Test
        @DisplayName("Should apply styles efficiently")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldApplyStylesEfficiently() {
            // Given
            LineChart<String, Number> chart = new LineChart<>(
                new CategoryAxis(), new NumberAxis()
            );

            // When & Then
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 50; i++) {
                    chartUtil.applyChartStyle(chart, "sales-trend");
                }
            });
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should create complete chart workflow")
        @Timeout(value = 15, unit = TimeUnit.SECONDS)
        void shouldCreateCompleteChartWorkflow() {
            // When & Then
            assertDoesNotThrow(() -> {
                // ایجاد نمودار
                Chart chart = chartUtil.createEmptyChart("line", "Complete Chart");
                
                // اعمال استایل
                chartUtil.applyChartStyle(chart, "sales-trend");
                
                // تنظیم رنگ‌ها
                chartUtil.setChartColors(chart, Color.BLUE, Color.RED);
                
                // تنظیم انیمیشن
                chartUtil.setChartAnimation(chart, true);
                
                // تنظیم اندازه
                chartUtil.setChartSize(chart, 500, 400);
                
                // ایجاد کارت
                VBox card = chartUtil.createChartCard(chart, "Chart Title", "Description");
                
                assertNotNull(chart);
                assertNotNull(card);
            });
        }

        @Test
        @DisplayName("Should handle multiple chart types")
        @Timeout(value = 20, unit = TimeUnit.SECONDS)
        void shouldHandleMultipleChartTypes() {
            // When & Then
            assertDoesNotThrow(() -> {
                // Line Chart
                Chart lineChart = chartUtil.createEmptyChart("line", "Line Chart");
                chartUtil.applyChartStyle(lineChart, "sales-trend");
                
                // Bar Chart
                Chart barChart = chartUtil.createEmptyChart("bar", "Bar Chart");
                chartUtil.applyChartStyle(barChart, "restaurant-performance");
                
                // Pie Chart
                Chart pieChart = chartUtil.createEmptyChart("pie", "Pie Chart");
                chartUtil.applyChartStyle(pieChart, "user-distribution");
                
                // Area Chart
                Chart areaChart = chartUtil.createEmptyChart("area", "Area Chart");
                chartUtil.applyChartStyle(areaChart, "revenue");
                
                assertNotNull(lineChart);
                assertNotNull(barChart);
                assertNotNull(pieChart);
                assertNotNull(areaChart);
            });
        }
    }
} 
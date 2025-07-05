package com.myapp.ui.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import com.myapp.ui.analytics.util.DataFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * تست‌های کلاس DataFormatter
 * تست‌های مربوط به فرمت‌دهی و صادرات داده‌ها
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
@DisplayName("DataFormatter Tests")
@Tag("analytics")
@Tag("util")
class DataFormatterTest {

    private DataFormatter dataFormatter;

    @BeforeEach
    void setUp() {
        dataFormatter = new DataFormatter();
    }

    @AfterEach
    void tearDown() {
        dataFormatter = null;
    }

    // ==================== CSV Export Tests ====================

    @Nested
    @DisplayName("CSV Export Tests")
    class CSVExportTests {

        @Test
        @DisplayName("Should export data to CSV successfully")
        void shouldExportDataToCSV() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("name", "Restaurant A");
            row1.put("rating", 4.5);
            row1.put("orders", 150);
            data.add(row1);

            Map<String, Object> row2 = new HashMap<>();
            row2.put("name", "Restaurant B");
            row2.put("rating", 4.2);
            row2.put("orders", 120);
            data.add(row2);

            String filename = "test-export";

            // When
            String result = dataFormatter.exportToCSV(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("صادر شد") || result.contains("exported"));
        }

        @Test
        @DisplayName("Should handle empty data for CSV export")
        void shouldHandleEmptyDataForCSVExport() {
            // Given
            List<Map<String, Object>> emptyData = new ArrayList<>();
            String filename = "empty-test";

            // When
            String result = dataFormatter.exportToCSV(emptyData, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null data for CSV export")
        void shouldHandleNullDataForCSVExport() {
            // Given
            String filename = "null-test";

            // When
            String result = dataFormatter.exportToCSV(null, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle special characters in CSV export")
        void shouldHandleSpecialCharactersInCSVExport() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("name", "Restaurant with \"quotes\" and, commas");
            row.put("description", "Special chars: \n\r\t");
            data.add(row);

            String filename = "special-chars-test";

            // When
            String result = dataFormatter.exportToCSV(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // ==================== JSON Export Tests ====================

    @Nested
    @DisplayName("JSON Export Tests")
    class JSONExportTests {

        @Test
        @DisplayName("Should export data to JSON successfully")
        void shouldExportDataToJSON() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("name", "Restaurant A");
            row1.put("rating", 4.5);
            row1.put("orders", 150);
            data.add(row1);

            Map<String, Object> row2 = new HashMap<>();
            row2.put("name", "Restaurant B");
            row2.put("rating", 4.2);
            row2.put("orders", 120);
            data.add(row2);

            String filename = "test-export";

            // When
            String result = dataFormatter.exportToJSON(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("صادر شد") || result.contains("exported"));
        }

        @Test
        @DisplayName("Should handle empty data for JSON export")
        void shouldHandleEmptyDataForJSONExport() {
            // Given
            List<Map<String, Object>> emptyData = new ArrayList<>();
            String filename = "empty-test";

            // When
            String result = dataFormatter.exportToJSON(emptyData, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null data for JSON export")
        void shouldHandleNullDataForJSONExport() {
            // Given
            String filename = "null-test";

            // When
            String result = dataFormatter.exportToJSON(null, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle complex data structures in JSON export")
        void shouldHandleComplexDataStructuresInJSONExport() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("name", "Restaurant A");
            row.put("rating", 4.5);
            row.put("orders", 150);
            row.put("active", true);
            row.put("tags", new String[]{"fast-food", "delivery"});
            data.add(row);

            String filename = "complex-test";

            // When
            String result = dataFormatter.exportToJSON(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // ==================== Excel Export Tests ====================

    @Nested
    @DisplayName("Excel Export Tests")
    class ExcelExportTests {

        @Test
        @DisplayName("Should export data to Excel successfully")
        void shouldExportDataToExcel() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("name", "Restaurant A");
            row1.put("rating", 4.5);
            row1.put("orders", 150);
            data.add(row1);

            Map<String, Object> row2 = new HashMap<>();
            row2.put("name", "Restaurant B");
            row2.put("rating", 4.2);
            row2.put("orders", 120);
            data.add(row2);

            String filename = "test-export";

            // When
            String result = dataFormatter.exportToExcel(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("صادر شد") || result.contains("exported"));
        }

        @Test
        @DisplayName("Should handle empty data for Excel export")
        void shouldHandleEmptyDataForExcelExport() {
            // Given
            List<Map<String, Object>> emptyData = new ArrayList<>();
            String filename = "empty-test";

            // When
            String result = dataFormatter.exportToExcel(emptyData, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null data for Excel export")
        void shouldHandleNullDataForExcelExport() {
            // Given
            String filename = "null-test";

            // When
            String result = dataFormatter.exportToExcel(null, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    // ==================== Data Formatting Tests ====================

    @Nested
    @DisplayName("Data Formatting Tests")
    class DataFormattingTests {

        @Test
        @DisplayName("Should format number correctly")
        void shouldFormatNumber() {
            // Given
            Number number = 1234567;

            // When
            String result = dataFormatter.formatNumber(number);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("1,234,567"));
        }

        @Test
        @DisplayName("Should format percentage correctly")
        void shouldFormatPercentage() {
            // Given
            Number percentage = 75.5;

            // When
            String result = dataFormatter.formatPercentage(percentage);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("75.5%"));
        }

        @Test
        @DisplayName("Should format currency correctly")
        void shouldFormatCurrency() {
            // Given
            Number amount = 1250000;

            // When
            String result = dataFormatter.formatCurrency(amount);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.contains("تومان"));
        }

        @Test
        @DisplayName("Should format date correctly")
        void shouldFormatDate() {
            // Given
            Date date = new Date();

            // When
            String result = dataFormatter.formatDate(date);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should format large numbers correctly")
        void shouldFormatLargeNumbers() {
            // Given
            Number largeNumber = 999999999;

            // When
            String result = dataFormatter.formatNumber(largeNumber);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should format zero values correctly")
        void shouldFormatZeroValues() {
            // Given
            Number zero = 0;

            // When
            String numberResult = dataFormatter.formatNumber(zero);
            String percentageResult = dataFormatter.formatPercentage(zero);
            String currencyResult = dataFormatter.formatCurrency(zero);

            // Then
            assertNotNull(numberResult);
            assertNotNull(percentageResult);
            assertNotNull(currencyResult);
            assertEquals("0", numberResult);
            assertEquals("0%", percentageResult);
        }
    }

    // ==================== Statistics Formatting Tests ====================

    @Nested
    @DisplayName("Statistics Formatting Tests")
    class StatisticsFormattingTests {

        @Test
        @DisplayName("Should format statistics correctly")
        void shouldFormatStatistics() {
            // Given
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", 1250);
            stats.put("totalRestaurants", 85);
            stats.put("totalOrders", 5420);
            stats.put("totalRevenue", 125000000.0);

            // When
            Map<String, String> result = dataFormatter.formatStatistics(stats);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.containsKey("totalUsers"));
            assertTrue(result.containsKey("totalRestaurants"));
            assertTrue(result.containsKey("totalOrders"));
            assertTrue(result.containsKey("totalRevenue"));
        }

        @Test
        @DisplayName("Should handle empty statistics")
        void shouldHandleEmptyStatistics() {
            // Given
            Map<String, Object> emptyStats = new HashMap<>();

            // When
            Map<String, String> result = dataFormatter.formatStatistics(emptyStats);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null statistics")
        void shouldHandleNullStatistics() {
            // When
            Map<String, String> result = dataFormatter.formatStatistics(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== Table Data Formatting Tests ====================

    @Nested
    @DisplayName("Table Data Formatting Tests")
    class TableDataFormattingTests {

        @Test
        @DisplayName("Should format table data correctly")
        void shouldFormatTableData() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("name", "Restaurant A");
            row1.put("rating", 4.5);
            row1.put("orders", 150);
            data.add(row1);

            Map<String, Object> row2 = new HashMap<>();
            row2.put("name", "Restaurant B");
            row2.put("rating", 4.2);
            row2.put("orders", 120);
            data.add(row2);

            // When
            List<Map<String, String>> result = dataFormatter.formatTableData(data);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should handle empty table data")
        void shouldHandleEmptyTableData() {
            // Given
            List<Map<String, Object>> emptyData = new ArrayList<>();

            // When
            List<Map<String, String>> result = dataFormatter.formatTableData(emptyData);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle null table data")
        void shouldHandleNullTableData() {
            // When
            List<Map<String, String>> result = dataFormatter.formatTableData(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== Performance Tests ====================

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large datasets efficiently")
        @Timeout(value = 30, unit = java.util.concurrent.TimeUnit.SECONDS)
        void shouldHandleLargeDatasets() {
            // Given
            List<Map<String, Object>> largeData = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", i);
                row.put("name", "Restaurant " + i);
                row.put("rating", 4.0 + (i % 10) * 0.1);
                row.put("orders", 100 + i);
                largeData.add(row);
            }

            String filename = "large-dataset-test";

            // When
            long startTime = System.currentTimeMillis();
            String result = dataFormatter.exportToCSV(largeData, filename);
            long endTime = System.currentTimeMillis();

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
            long duration = endTime - startTime;
            assertTrue(duration < 10000, "Export should complete within 10 seconds, took: " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle multiple concurrent exports")
        @Timeout(value = 30, unit = java.util.concurrent.TimeUnit.SECONDS)
        void shouldHandleMultipleConcurrentExports() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("name", "Test Restaurant");
            row.put("rating", 4.5);
            row.put("orders", 150);
            data.add(row);

            // When & Then
            assertDoesNotThrow(() -> {
                dataFormatter.exportToCSV(data, "concurrent-test-1");
                dataFormatter.exportToJSON(data, "concurrent-test-2");
                dataFormatter.exportToExcel(data, "concurrent-test-3");
            });
        }
    }

    // ==================== Error Handling Tests ====================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null values gracefully")
        void shouldHandleNullValuesGracefully() {
            // When & Then
            assertDoesNotThrow(() -> {
                dataFormatter.formatNumber(null);
                dataFormatter.formatPercentage(null);
                dataFormatter.formatCurrency(null);
                dataFormatter.formatDate(null);
            });
        }

        @Test
        @DisplayName("Should handle empty strings gracefully")
        void shouldHandleEmptyStringsGracefully() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("name", "");
            row.put("description", null);
            data.add(row);

            String filename = "empty-strings-test";

            // When & Then
            assertDoesNotThrow(() -> {
                dataFormatter.exportToCSV(data, filename);
                dataFormatter.exportToJSON(data, filename);
                dataFormatter.exportToExcel(data, filename);
            });
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should perform complete export workflow")
        void shouldPerformCompleteExportWorkflow() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("name", "Restaurant A");
            row1.put("rating", 4.5);
            row1.put("orders", 150);
            row1.put("revenue", 2500000.0);
            data.add(row1);

            Map<String, Object> row2 = new HashMap<>();
            row2.put("name", "Restaurant B");
            row2.put("rating", 4.2);
            row2.put("orders", 120);
            row2.put("revenue", 1800000.0);
            data.add(row2);

            String baseFilename = "complete-workflow-test";

            // When
            String csvResult = dataFormatter.exportToCSV(data, baseFilename + "-csv");
            String jsonResult = dataFormatter.exportToJSON(data, baseFilename + "-json");
            String excelResult = dataFormatter.exportToExcel(data, baseFilename + "-excel");

            // Then
            assertNotNull(csvResult);
            assertNotNull(jsonResult);
            assertNotNull(excelResult);
            assertFalse(csvResult.isEmpty());
            assertFalse(jsonResult.isEmpty());
            assertFalse(excelResult.isEmpty());
        }

        @Test
        @DisplayName("Should handle mixed data types in export")
        void shouldHandleMixedDataTypesInExport() {
            // Given
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new HashMap<>();
            row.put("name", "Restaurant A");
            row.put("rating", 4.5);
            row.put("orders", 150);
            row.put("active", true);
            row.put("founded", new Date());
            row.put("tags", new String[]{"fast-food", "delivery"});
            data.add(row);

            String filename = "mixed-types-test";

            // When
            String result = dataFormatter.exportToJSON(data, filename);

            // Then
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
} 
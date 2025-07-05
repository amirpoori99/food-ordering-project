package com.foodordering.frontend.analytics.ui.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

class DataFormatterTest {

    private DataFormatter dataFormatter;

    @BeforeEach
    void setUp() {
        dataFormatter = new DataFormatter();
    }

    @Test
    void testFormatCurrency() {
        BigDecimal amount = new BigDecimal("1234.56");
        String formatted = dataFormatter.formatCurrency(amount);
        assertEquals("$1,234.56", formatted);
    }

    @Test
    void testFormatCurrencyWithZero() {
        BigDecimal amount = BigDecimal.ZERO;
        String formatted = dataFormatter.formatCurrency(amount);
        assertEquals("$0.00", formatted);
    }

    @Test
    void testFormatCurrencyWithNegative() {
        BigDecimal amount = new BigDecimal("-1234.56");
        String formatted = dataFormatter.formatCurrency(amount);
        assertEquals("-$1,234.56", formatted);
    }

    @Test
    void testFormatPercentage() {
        double value = 0.1234;
        String formatted = dataFormatter.formatPercentage(value);
        assertEquals("12.34%", formatted);
    }

    @Test
    void testFormatPercentageWithZero() {
        double value = 0.0;
        String formatted = dataFormatter.formatPercentage(value);
        assertEquals("0.00%", formatted);
    }

    @Test
    void testFormatPercentageWithHundred() {
        double value = 1.0;
        String formatted = dataFormatter.formatPercentage(value);
        assertEquals("100.00%", formatted);
    }

    @Test
    void testFormatNumber() {
        long number = 1234567;
        String formatted = dataFormatter.formatNumber(number);
        assertEquals("1,234,567", formatted);
    }

    @Test
    void testFormatNumberWithZero() {
        long number = 0;
        String formatted = dataFormatter.formatNumber(number);
        assertEquals("0", formatted);
    }

    @Test
    void testFormatDate() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
        String formatted = dataFormatter.formatDate(dateTime);
        assertEquals("2024-01-15", formatted);
    }

    @Test
    void testFormatDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
        String formatted = dataFormatter.formatDateTime(dateTime);
        assertEquals("2024-01-15 14:30", formatted);
    }

    @Test
    void testFormatTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);
        String formatted = dataFormatter.formatTime(dateTime);
        assertEquals("14:30", formatted);
    }

    @Test
    void testFormatDuration() {
        long minutes = 125;
        String formatted = dataFormatter.formatDuration(minutes);
        assertEquals("2h 5m", formatted);
    }

    @Test
    void testFormatDurationWithZero() {
        long minutes = 0;
        String formatted = dataFormatter.formatDuration(minutes);
        assertEquals("0m", formatted);
    }

    @Test
    void testFormatDurationWithLessThanHour() {
        long minutes = 45;
        String formatted = dataFormatter.formatDuration(minutes);
        assertEquals("45m", formatted);
    }

    @Test
    void testFormatList() {
        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
        String formatted = dataFormatter.formatList(items);
        assertEquals("Item 1, Item 2, Item 3", formatted);
    }

    @Test
    void testFormatListWithEmptyList() {
        List<String> items = Arrays.asList();
        String formatted = dataFormatter.formatList(items);
        assertEquals("", formatted);
    }

    @Test
    void testFormatListWithSingleItem() {
        List<String> items = Arrays.asList("Single Item");
        String formatted = dataFormatter.formatList(items);
        assertEquals("Single Item", formatted);
    }

    @Test
    void testTruncateText() {
        String text = "This is a very long text that needs to be truncated";
        String truncated = dataFormatter.truncateText(text, 20);
        assertEquals("This is a very long...", truncated);
    }

    @Test
    void testTruncateTextShorterThanLimit() {
        String text = "Short text";
        String truncated = dataFormatter.truncateText(text, 20);
        assertEquals("Short text", truncated);
    }

    @Test
    void testTruncateTextWithNull() {
        String truncated = dataFormatter.truncateText(null, 20);
        assertEquals("", truncated);
    }

    @Test
    void testFormatFileSize() {
        long bytes = 1024 * 1024 * 2; // 2MB
        String formatted = dataFormatter.formatFileSize(bytes);
        assertEquals("2.00 MB", formatted);
    }

    @Test
    void testFormatFileSizeInKB() {
        long bytes = 1024 * 5; // 5KB
        String formatted = dataFormatter.formatFileSize(bytes);
        assertEquals("5.00 KB", formatted);
    }

    @Test
    void testFormatFileSizeInBytes() {
        long bytes = 500;
        String formatted = dataFormatter.formatFileSize(bytes);
        assertEquals("500 B", formatted);
    }

    @Test
    void testFormatFileSizeWithZero() {
        long bytes = 0;
        String formatted = dataFormatter.formatFileSize(bytes);
        assertEquals("0 B", formatted);
    }
} 
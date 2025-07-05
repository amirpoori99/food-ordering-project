package com.myapp.ui.analytics.util;

import java.util.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * ابزارهای فرمت‌بندی داده برای Analytics
 * مسئول فرمت‌بندی و صادرات داده‌ها
 * 
 * @author Food Ordering System
 * @version 1.0
 * @since 2024
 */
public class DataFormatter {

    private final DecimalFormat numberFormat;
    private final DecimalFormat currencyFormat;
    private final SimpleDateFormat dateFormat;
    private final ObjectMapper objectMapper;
    
    // تنظیمات فرمت‌بندی
    private static final String CURRENCY_SYMBOL = "تومان";
    private static final String THOUSAND_SEPARATOR = ",";
    private static final String DECIMAL_SEPARATOR = ".";
    private static final int DECIMAL_PLACES = 2;

    public DataFormatter() {
        this.numberFormat = new DecimalFormat("#,##0");
        this.currencyFormat = new DecimalFormat("#,##0.00");
        this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // ==================== Number Formatting ====================

    /**
     * فرمت‌بندی اعداد
     */
    public String formatNumber(Number number) {
        if (number == null) return "0";
        
        if (number instanceof Long || number instanceof Integer) {
            return numberFormat.format(number);
        } else if (number instanceof Double || number instanceof Float) {
            return numberFormat.format(number.doubleValue());
        }
        
        return number.toString();
    }

    /**
     * فرمت‌بندی اعداد با اعشار
     */
    public String formatDecimal(Number number) {
        if (number == null) return "0.00";
        
        return currencyFormat.format(number.doubleValue());
    }

    /**
     * فرمت‌بندی درصد
     */
    public String formatPercentage(Number number) {
        if (number == null) return "0%";
        
        double value = number.doubleValue();
        if (value == 0.0) {
            return "0%";
        }
        return String.format("%.1f%%", value);
    }

    /**
     * فرمت‌بندی درصد با تغییر
     */
    public String formatPercentageChange(Number current, Number previous) {
        if (current == null || previous == null || previous.doubleValue() == 0) {
            return "0%";
        }
        
        double change = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
        String sign = change >= 0 ? "+" : "";
        return sign + String.format("%.1f%%", change);
    }

    // ==================== Currency Formatting ====================

    /**
     * فرمت‌بندی پول
     */
    public String formatCurrency(Number amount) {
        if (amount == null) return "0 " + CURRENCY_SYMBOL;
        
        return formatNumber(amount) + " " + CURRENCY_SYMBOL;
    }

    /**
     * فرمت‌بندی پول با اعشار
     */
    public String formatCurrencyDecimal(Number amount) {
        if (amount == null) return "0.00 " + CURRENCY_SYMBOL;
        
        return formatDecimal(amount) + " " + CURRENCY_SYMBOL;
    }

    /**
     * فرمت‌بندی پول کوتاه (K, M, B)
     */
    public String formatCurrencyShort(Number amount) {
        if (amount == null) return "0 " + CURRENCY_SYMBOL;
        
        double value = amount.doubleValue();
        
        if (value >= 1_000_000_000) {
            return String.format("%.1fB %s", value / 1_000_000_000, CURRENCY_SYMBOL);
        } else if (value >= 1_000_000) {
            return String.format("%.1fM %s", value / 1_000_000, CURRENCY_SYMBOL);
        } else if (value >= 1_000) {
            return String.format("%.1fK %s", value / 1_000, CURRENCY_SYMBOL);
        } else {
            return formatCurrency(amount);
        }
    }

    // ==================== Date/Time Formatting ====================

    /**
     * فرمت‌بندی تاریخ
     */
    public String formatDate(Date date) {
        if (date == null) return "";
        
        return dateFormat.format(date);
    }

    /**
     * فرمت‌بندی تاریخ فارسی
     */
    public String formatDatePersian(Date date) {
        if (date == null) return "";
        
        // تبدیل به تاریخ شمسی
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        return String.format("%d/%02d/%02d", year, month, day);
    }

    /**
     * فرمت‌بندی زمان
     */
    public String formatTime(Date date) {
        if (date == null) return "";
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(date);
    }

    /**
     * فرمت‌بندی تاریخ و زمان
     */
    public String formatDateTime(Date date) {
        if (date == null) return "";
        
        return dateFormat.format(date);
    }

    /**
     * فرمت‌بندی تاریخ و زمان فارسی
     */
    public String formatDateTimePersian(Date date) {
        if (date == null) return "";
        
        return formatDatePersian(date) + " " + formatTime(date);
    }

    /**
     * فرمت‌بندی مدت زمان
     */
    public String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + " میلی‌ثانیه";
        } else if (milliseconds < 60000) {
            return String.format("%.1f ثانیه", milliseconds / 1000.0);
        } else if (milliseconds < 3600000) {
            return String.format("%.1f دقیقه", milliseconds / 60000.0);
        } else {
            return String.format("%.1f ساعت", milliseconds / 3600000.0);
        }
    }

    // ==================== Text Formatting ====================

    /**
     * فرمت‌بندی متن با حروف بزرگ
     */
    public String formatTitleCase(String text) {
        if (text == null || text.isEmpty()) return "";
        
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            
            if (!words[i].isEmpty()) {
                result.append(words[i].substring(0, 1).toUpperCase())
                      .append(words[i].substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }

    /**
     * فرمت‌بندی متن با محدودیت طول
     */
    public String formatTruncatedText(String text, int maxLength) {
        if (text == null) return "";
        
        if (text.length() <= maxLength) {
            return text;
        }
        
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * فرمت‌بندی متن با حروف بزرگ اول
     */
    public String formatCapitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return "";
        
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    // ==================== Status Formatting ====================

    /**
     * فرمت‌بندی وضعیت سفارش
     */
    public String formatOrderStatus(String status) {
        if (status == null) return "نامشخص";
        
        switch (status.toLowerCase()) {
            case "pending":
                return "در انتظار";
            case "confirmed":
                return "تأیید شده";
            case "preparing":
                return "در حال آماده‌سازی";
            case "ready":
                return "آماده";
            case "delivering":
                return "در راه";
            case "delivered":
                return "تحویل شده";
            case "cancelled":
                return "لغو شده";
            default:
                return status;
        }
    }

    /**
     * فرمت‌بندی وضعیت پرداخت
     */
    public String formatPaymentStatus(String status) {
        if (status == null) return "نامشخص";
        
        switch (status.toLowerCase()) {
            case "pending":
                return "در انتظار";
            case "completed":
                return "تکمیل شده";
            case "failed":
                return "ناموفق";
            case "refunded":
                return "بازگشت";
            default:
                return status;
        }
    }

    /**
     * فرمت‌بندی وضعیت کاربر
     */
    public String formatUserStatus(String status) {
        if (status == null) return "نامشخص";
        
        switch (status.toLowerCase()) {
            case "active":
                return "فعال";
            case "inactive":
                return "غیرفعال";
            case "suspended":
                return "معلق";
            case "banned":
                return "مسدود";
            default:
                return status;
        }
    }

    // ==================== Export Functions ====================

    /**
     * صادرات به CSV
     */
    public String exportToCSV(List<Map<String, Object>> data, String filename) {
        if (data == null || data.isEmpty()) {
            return "هیچ داده‌ای برای صادرات وجود ندارد";
        }
        
        try {
            StringBuilder csv = new StringBuilder();
            
            // هدر
            Set<String> headers = data.get(0).keySet();
            csv.append(String.join(",", headers)).append("\n");
            
            // داده‌ها
            for (Map<String, Object> row : data) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    Object value = row.get(header);
                    values.add(formatCSVValue(value));
                }
                csv.append(String.join(",", values)).append("\n");
            }
            
            // ذخیره فایل
            String fullFilename = filename + "_" + getCurrentTimestamp() + ".csv";
            Files.write(Paths.get(fullFilename), csv.toString().getBytes(StandardCharsets.UTF_8));
            
            return "داده‌ها با موفقیت به فایل " + fullFilename + " صادر شدند";
            
        } catch (IOException e) {
            return "خطا در صادرات CSV: " + e.getMessage();
        }
    }

    /**
     * صادرات به JSON
     */
    public String exportToJSON(List<Map<String, Object>> data, String filename) {
        if (data == null || data.isEmpty()) {
            return "هیچ داده‌ای برای صادرات وجود ندارد";
        }
        
        try {
            // تبدیل داده‌ها
            List<Map<String, Object>> formattedData = new ArrayList<>();
            for (Map<String, Object> row : data) {
                Map<String, Object> formattedRow = new HashMap<>();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    formattedRow.put(entry.getKey(), formatExportValue(entry.getValue()));
                }
                formattedData.add(formattedRow);
            }
            
            // تبدیل به JSON
            String json = objectMapper.writeValueAsString(formattedData);
            
            // ذخیره فایل
            String fullFilename = filename + "_" + getCurrentTimestamp() + ".json";
            Files.write(Paths.get(fullFilename), json.getBytes(StandardCharsets.UTF_8));
            
            return "داده‌ها با موفقیت به فایل " + fullFilename + " صادر شدند";
            
        } catch (Exception e) {
            return "خطا در صادرات JSON: " + e.getMessage();
        }
    }

    /**
     * صادرات به Excel (CSV با فرمت بهتر)
     */
    public String exportToExcel(List<Map<String, Object>> data, String filename) {
        if (data == null || data.isEmpty()) {
            return "هیچ داده‌ای برای صادرات وجود ندارد";
        }
        
        try {
            StringBuilder excel = new StringBuilder();
            
            // هدر
            Set<String> headers = data.get(0).keySet();
            excel.append("\"").append(String.join("\",\"", headers)).append("\"\n");
            
            // داده‌ها
            for (Map<String, Object> row : data) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    Object value = row.get(header);
                    values.add("\"" + formatExcelValue(value) + "\"");
                }
                excel.append(String.join(",", values)).append("\n");
            }
            
            // ذخیره فایل
            String fullFilename = filename + "_" + getCurrentTimestamp() + ".csv";
            Files.write(Paths.get(fullFilename), excel.toString().getBytes(StandardCharsets.UTF_8));
            
            return "داده‌ها با موفقیت به فایل " + fullFilename + " صادر شدند";
            
        } catch (IOException e) {
            return "خطا در صادرات Excel: " + e.getMessage();
        }
    }

    // ==================== Utility Methods ====================

    /**
     * فرمت‌بندی مقدار CSV
     */
    private String formatCSVValue(Object value) {
        if (value == null) return "";
        
        String strValue = value.toString();
        
        // اگر شامل کاما یا نقل قول باشد، در نقل قول قرار می‌دهیم
        if (strValue.contains(",") || strValue.contains("\"") || strValue.contains("\n")) {
            return "\"" + strValue.replace("\"", "\"\"") + "\"";
        }
        
        return strValue;
    }

    /**
     * فرمت‌بندی مقدار Excel
     */
    private String formatExcelValue(Object value) {
        if (value == null) return "";
        
        String strValue = value.toString();
        
        // جایگزینی کاراکترهای مشکل‌ساز
        return strValue.replace("\"", "\"\"")
                      .replace("\n", " ")
                      .replace("\r", " ");
    }

    /**
     * فرمت‌بندی مقدار صادرات
     */
    private Object formatExportValue(Object value) {
        if (value == null) return null;
        
        if (value instanceof Number) {
            if (value instanceof Double || value instanceof Float) {
                return formatDecimal((Number) value);
            } else {
                return formatNumber((Number) value);
            }
        } else if (value instanceof Date) {
            return formatDateTime((Date) value);
        }
        
        return value.toString();
    }

    /**
     * دریافت timestamp فعلی
     */
    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return now.format(formatter);
    }

    /**
     * فرمت‌بندی آمار
     */
    public Map<String, String> formatStatistics(Map<String, Object> stats) {
        if (stats == null) {
            return new HashMap<>();
        }
        
        Map<String, String> formattedStats = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Number) {
                if (key.toLowerCase().contains("revenue") || key.toLowerCase().contains("amount")) {
                    formattedStats.put(key, formatCurrency((Number) value));
                } else if (key.toLowerCase().contains("percentage") || key.toLowerCase().contains("rate")) {
                    formattedStats.put(key, formatPercentage((Number) value));
                } else {
                    formattedStats.put(key, formatNumber((Number) value));
                }
            } else if (value instanceof Date) {
                formattedStats.put(key, formatDateTime((Date) value));
            } else {
                formattedStats.put(key, value != null ? value.toString() : "");
            }
        }
        
        return formattedStats;
    }

    /**
     * فرمت‌بندی جدول
     */
    public List<Map<String, String>> formatTableData(List<Map<String, Object>> data) {
        if (data == null) {
            return new ArrayList<>();
        }
        
        List<Map<String, String>> formattedData = new ArrayList<>();
        
        for (Map<String, Object> row : data) {
            Map<String, String> formattedRow = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof Number) {
                    if (key.toLowerCase().contains("revenue") || key.toLowerCase().contains("amount")) {
                        formattedRow.put(key, formatCurrency((Number) value));
                    } else if (key.toLowerCase().contains("percentage") || key.toLowerCase().contains("rate")) {
                        formattedRow.put(key, formatPercentage((Number) value));
                    } else {
                        formattedRow.put(key, formatNumber((Number) value));
                    }
                } else if (value instanceof Date) {
                    formattedRow.put(key, formatDateTime((Date) value));
                } else {
                    formattedRow.put(key, value != null ? value.toString() : "");
                }
            }
            
            formattedData.add(formattedRow);
        }
        
        return formattedData;
    }
} 
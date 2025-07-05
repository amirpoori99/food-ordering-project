package com.myapp.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * کلاس کمکی برای عملیات تاریخ و زمان
 */
public class DateUtil {
    
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * دریافت تاریخ و زمان فعلی
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
    
    /**
     * تبدیل LocalDateTime به String
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : null;
    }
    
    /**
     * تبدیل String به LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER) : null;
    }
    
    /**
     * دریافت تاریخ امروز
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    /**
     * دریافت تاریخ هفته گذشته
     */
    public static LocalDate lastWeek() {
        return LocalDate.now().minusWeeks(1);
    }
    
    /**
     * دریافت تاریخ هفته آینده
     */
    public static LocalDate nextWeek() {
        return LocalDate.now().plusWeeks(1);
    }
    
    /**
     * دریافت تاریخ ماه گذشته
     */
    public static LocalDate lastMonth() {
        return LocalDate.now().minusMonths(1);
    }
    
    /**
     * دریافت تاریخ ماه آینده
     */
    public static LocalDate nextMonth() {
        return LocalDate.now().plusMonths(1);
    }
    
    /**
     * محاسبه تفاوت بین دو تاریخ
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays();
    }
    
    /**
     * محاسبه تفاوت بین دو زمان
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }
    
    /**
     * تبدیل Date به LocalDateTime
     */
    public static LocalDateTime fromDate(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }
    
    /**
     * تبدیل LocalDateTime به Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        return dateTime != null ? Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }
    
    /**
     * بررسی اینکه آیا تاریخ در محدوده است
     */
    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        return date != null && start != null && end != null && 
               !date.isBefore(start) && !date.isAfter(end);
    }
    
    /**
     * دریافت نام روز هفته
     */
    public static String getDayOfWeek(LocalDate date) {
        return date != null ? date.getDayOfWeek().toString() : null;
    }
    
    /**
     * دریافت نام ماه
     */
    public static String getMonthName(LocalDate date) {
        return date != null ? date.getMonth().toString() : null;
    }
} 
package com.myapp.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * کلاس ابزاری برای logging پیشرفته با فرمت‌بندی مناسب و کنترل سطح
 * این کلاس مجموعه‌ای از متدهای کمکی برای log کردن انواع مختلف رویدادها فراهم می‌کند
 * شامل log های تست، عملکرد، امنیت، API و پایگاه داده
 */
public class LoggerUtil {
    
    // فرمت timestamp برای log ها
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * ثبت پیشرفت تست با فرمت‌بندی مناسب
     * 
     * @param logger نمونه Logger
     * @param testName نام تست
     * @param message پیام
     */
    public static void logTestProgress(Logger logger, String testName, String message) {
        logger.info("🧪 [{}] {}", testName, message);
    }
    
    /**
     * ثبت موفقیت تست با emoji و فرمت‌بندی
     * 
     * @param logger نمونه Logger
     * @param testName نام تست
     * @param message پیام موفقیت
     */
    public static void logTestSuccess(Logger logger, String testName, String message) {
        logger.info("✅ [{}] {}", testName, message);
    }
    
    /**
     * ثبت شکست تست با emoji و فرمت‌بندی
     * 
     * @param logger نمونه Logger
     * @param testName نام تست
     * @param message پیام شکست
     */
    public static void logTestFailure(Logger logger, String testName, String message) {
        logger.error("❌ [{}] {}", testName, message);
    }
    
    /**
     * ثبت هشدار تست با emoji و فرمت‌بندی
     * 
     * @param logger نمونه Logger
     * @param testName نام تست
     * @param message پیام هشدار
     */
    public static void logTestWarning(Logger logger, String testName, String message) {
        logger.warn("⚠️ [{}] {}", testName, message);
    }
    
    /**
     * ثبت معیارهای عملکرد
     * 
     * @param logger نمونه Logger
     * @param operation نام عملیات
     * @param durationMs مدت زمان انجام (میلی‌ثانیه)
     */
    public static void logPerformance(Logger logger, String operation, long durationMs) {
        logger.info("⚡ Performance: {} completed in {}ms", operation, durationMs);
    }
    
    /**
     * ثبت معیارهای عملکرد با context اضافی
     * 
     * @param logger نمونه Logger
     * @param operation نام عملیات
     * @param durationMs مدت زمان انجام (میلی‌ثانیه)
     * @param context اطلاعات اضافی
     */
    public static void logPerformance(Logger logger, String operation, long durationMs, String context) {
        logger.info("⚡ Performance: {} completed in {}ms - {}", operation, durationMs, context);
    }
    
    /**
     * ثبت شروع عملیات کسب‌وکار
     * 
     * @param logger نمونه Logger
     * @param operation نام عملیات
     * @param params پارامترهای عملیات
     */
    public static void logOperationStart(Logger logger, String operation, Object... params) {
        logger.info("🔄 Starting: {} with params: {}", operation, java.util.Arrays.toString(params));
    }
    
    /**
     * ثبت تکمیل عملیات کسب‌وکار
     * 
     * @param logger نمونه Logger
     * @param operation نام عملیات
     * @param durationMs مدت زمان انجام (میلی‌ثانیه)
     */
    public static void logOperationComplete(Logger logger, String operation, long durationMs) {
        logger.info("✅ Completed: {} in {}ms", operation, durationMs);
    }
    
    /**
     * ثبت درخواست API
     * 
     * @param logger نمونه Logger
     * @param method HTTP method
     * @param path مسیر درخواست
     * @param userInfo اطلاعات کاربر
     */
    public static void logApiRequest(Logger logger, String method, String path, String userInfo) {
        logger.info("📥 API Request: {} {} - User: {}", method, path, userInfo);
    }
    
    /**
     * ثبت پاسخ API
     * 
     * @param logger نمونه Logger
     * @param method HTTP method
     * @param path مسیر درخواست
     * @param statusCode کد وضعیت HTTP
     * @param durationMs مدت زمان پردازش (میلی‌ثانیه)
     */
    public static void logApiResponse(Logger logger, String method, String path, int statusCode, long durationMs) {
        String emoji = statusCode < 300 ? "✅" : statusCode < 500 ? "⚠️" : "❌"; // انتخاب emoji بر اساس status
        logger.info("{} API Response: {} {} - Status: {} - Duration: {}ms", 
                   emoji, method, path, statusCode, durationMs);
    }
    
    /**
     * ثبت عملیات پایگاه داده
     * 
     * @param logger نمونه Logger
     * @param operation نوع عملیات (CREATE, READ, UPDATE, DELETE)
     * @param entity نام entity
     * @param id شناسه رکورد
     */
    public static void logDatabaseOperation(Logger logger, String operation, String entity, Object id) {
        logger.debug("💾 DB Operation: {} {} with ID: {}", operation, entity, id);
    }
    
    /**
     * ثبت رویداد امنیتی
     * 
     * @param logger نمونه Logger
     * @param event نوع رویداد امنیتی
     * @param userInfo اطلاعات کاربر
     * @param success موفقیت یا شکست عملیات
     */
    public static void logSecurityEvent(Logger logger, String event, String userInfo, boolean success) {
        String emoji = success ? "🔐" : "🚫";   // انتخاب emoji بر اساس نتیجه
        
        if (success) {
            logger.info("{} Security: {} - User: {}", emoji, event, userInfo);
        } else {
            logger.warn("{} Security: {} - User: {} - FAILED", emoji, event, userInfo);
        }
    }
    
    /**
     * ثبت خلاصه تست
     * 
     * @param logger نمونه Logger
     * @param testSuite نام مجموعه تست
     * @param total تعداد کل تست‌ها
     * @param passed تعداد تست‌های موفق
     * @param failed تعداد تست‌های ناموفق
     * @param totalDuration کل مدت زمان اجرا (میلی‌ثانیه)
     */
    public static void logTestSummary(Logger logger, String testSuite, int total, int passed, int failed, long totalDuration) {
        logger.info("📊 Test Summary: {} - Total: {}, Passed: {}, Failed: {}, Duration: {}ms", 
                   testSuite, total, passed, failed, totalDuration);
    }
    
    /**
     * ثبت جداکننده بخش برای خوانایی بهتر
     * 
     * @param logger نمونه Logger
     * @param title عنوان بخش
     */
    public static void logSeparator(Logger logger, String title) {
        String separator = "=".repeat(60);                           // خط جداکننده
        logger.info("\n{}\n{:^60}\n{}", separator, title, separator); // فرمت وسط‌چین
    }
    
    /**
     * ثبت زیربخش برای سازماندهی log ها
     * 
     * @param logger نمونه Logger
     * @param title عنوان زیربخش
     */
    public static void logSubsection(Logger logger, String title) {
        String separator = "-".repeat(40);                     // خط جداکننده کوتاه‌تر
        logger.info("\n{}\n{:^40}", separator, title);         // فرمت وسط‌چین
    }
    
    /**
     * ایجاد timestamp فرمت شده
     * 
     * @return timestamp فرمت شده
     */
    public static String getFormattedTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
    
    /**
     * ثبت log با emoji و سطح سفارشی
     * 
     * @param logger نمونه Logger
     * @param emoji emoji مورد نظر
     * @param message پیام (با پشتیبانی از format)
     * @param params پارامترهای پیام
     */
    public static void logWithEmoji(Logger logger, String emoji, String message, Object... params) {
        logger.info("{} {}", emoji, String.format(message, params));
    }
    
    /**
     * ثبت خطا با context
     * 
     * @param logger نمونه Logger
     * @param operation نام عملیات که خطا در آن رخ داده
     * @param error استثنای رخ داده
     * @param context اطلاعات اضافی
     */
    public static void logError(Logger logger, String operation, Throwable error, Object... context) {
        logger.error("❌ Error in {}: {} - Context: {}", 
                    operation, error.getMessage(), java.util.Arrays.toString(context), error);
    }
    
    /**
     * ثبت اطلاعات تنظیمات
     * 
     * @param logger نمونه Logger
     * @param component نام کامپوننت
     * Log configuration info
     */
    public static void logConfiguration(Logger logger, String component, String key, Object value) {
        logger.info("⚙️ Config: {} - {}={}", component, key, value);
    }
    
    /**
     * Log startup information
     */
    public static void logStartup(Logger logger, String component, String version) {
        logger.info("🚀 Starting {} version {}", component, version);
    }
    
    /**
     * Log shutdown information
     */
    public static void logShutdown(Logger logger, String component) {
        logger.info("🛑 Shutting down {}", component);
    }
} 
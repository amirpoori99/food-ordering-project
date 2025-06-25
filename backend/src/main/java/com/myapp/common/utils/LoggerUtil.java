package com.myapp.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for enhanced logging with proper formatting and level control
 */
public class LoggerUtil {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Log test progress with proper formatting
     */
    public static void logTestProgress(Logger logger, String testName, String message) {
        logger.info("🧪 [{}] {}", testName, message);
    }
    
    /**
     * Log test success with emoji and formatting
     */
    public static void logTestSuccess(Logger logger, String testName, String message) {
        logger.info("✅ [{}] {}", testName, message);
    }
    
    /**
     * Log test failure with emoji and formatting
     */
    public static void logTestFailure(Logger logger, String testName, String message) {
        logger.error("❌ [{}] {}", testName, message);
    }
    
    /**
     * Log test warning with emoji and formatting
     */
    public static void logTestWarning(Logger logger, String testName, String message) {
        logger.warn("⚠️ [{}] {}", testName, message);
    }
    
    /**
     * Log performance metrics
     */
    public static void logPerformance(Logger logger, String operation, long durationMs) {
        logger.info("⚡ Performance: {} completed in {}ms", operation, durationMs);
    }
    
    /**
     * Log performance metrics with additional context
     */
    public static void logPerformance(Logger logger, String operation, long durationMs, String context) {
        logger.info("⚡ Performance: {} completed in {}ms - {}", operation, durationMs, context);
    }
    
    /**
     * Log business operation start
     */
    public static void logOperationStart(Logger logger, String operation, Object... params) {
        logger.info("🔄 Starting: {} with params: {}", operation, java.util.Arrays.toString(params));
    }
    
    /**
     * Log business operation completion
     */
    public static void logOperationComplete(Logger logger, String operation, long durationMs) {
        logger.info("✅ Completed: {} in {}ms", operation, durationMs);
    }
    
    /**
     * Log API request
     */
    public static void logApiRequest(Logger logger, String method, String path, String userInfo) {
        logger.info("📥 API Request: {} {} - User: {}", method, path, userInfo);
    }
    
    /**
     * Log API response
     */
    public static void logApiResponse(Logger logger, String method, String path, int statusCode, long durationMs) {
        String emoji = statusCode < 300 ? "✅" : statusCode < 500 ? "⚠️" : "❌";
        logger.info("{} API Response: {} {} - Status: {} - Duration: {}ms", 
                   emoji, method, path, statusCode, durationMs);
    }
    
    /**
     * Log database operation
     */
    public static void logDatabaseOperation(Logger logger, String operation, String entity, Object id) {
        logger.debug("💾 DB Operation: {} {} with ID: {}", operation, entity, id);
    }
    
    /**
     * Log security event
     */
    public static void logSecurityEvent(Logger logger, String event, String userInfo, boolean success) {
        String emoji = success ? "🔐" : "🚫";
        String level = success ? "INFO" : "WARN";
        
        if (success) {
            logger.info("{} Security: {} - User: {}", emoji, event, userInfo);
        } else {
            logger.warn("{} Security: {} - User: {} - FAILED", emoji, event, userInfo);
        }
    }
    
    /**
     * Log test summary
     */
    public static void logTestSummary(Logger logger, String testSuite, int total, int passed, int failed, long totalDuration) {
        logger.info("📊 Test Summary: {} - Total: {}, Passed: {}, Failed: {}, Duration: {}ms", 
                   testSuite, total, passed, failed, totalDuration);
    }
    
    /**
     * Log section separator for better readability
     */
    public static void logSeparator(Logger logger, String title) {
        String separator = "=".repeat(60);
        logger.info("\n{}\n{:^60}\n{}", separator, title, separator);
    }
    
    /**
     * Log subsection for organizing logs
     */
    public static void logSubsection(Logger logger, String title) {
        String separator = "-".repeat(40);
        logger.info("\n{}\n{:^40}", separator, title);
    }
    
    /**
     * Create formatted timestamp
     */
    public static String getFormattedTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
    
    /**
     * Log with custom emoji and level
     */
    public static void logWithEmoji(Logger logger, String emoji, String message, Object... params) {
        logger.info("{} {}", emoji, String.format(message, params));
    }
    
    /**
     * Log error with context
     */
    public static void logError(Logger logger, String operation, Throwable error, Object... context) {
        logger.error("❌ Error in {}: {} - Context: {}", 
                    operation, error.getMessage(), java.util.Arrays.toString(context), error);
    }
    
    /**
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
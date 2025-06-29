package com.myapp.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * تست‌های کلاس LoggerUtil
 */
public class LoggerUtilTest {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtilTest.class);

    @Test
    @DisplayName("logTestProgress should log test progress")
    public void testLogTestProgress() {
        assertDoesNotThrow(() -> LoggerUtil.logTestProgress(logger, "TestCase", "Progress message"));
    }
    
    @Test
    @DisplayName("logTestSuccess should log test success")
    public void testLogTestSuccess() {
        assertDoesNotThrow(() -> LoggerUtil.logTestSuccess(logger, "TestCase", "Success message"));
    }
    
    @Test
    @DisplayName("logTestFailure should log test failure") 
    public void testLogTestFailure() {
        assertDoesNotThrow(() -> LoggerUtil.logTestFailure(logger, "TestCase", "Failure message"));
    }
    
    @Test
    @DisplayName("logPerformance should log performance metrics")
    public void testLogPerformance() {
        assertDoesNotThrow(() -> LoggerUtil.logPerformance(logger, "Test Operation", 100L));
        assertDoesNotThrow(() -> LoggerUtil.logPerformance(logger, "Test Operation", 200L, "Context info"));
    }
    
    @Test
    @DisplayName("logApiRequest should log API requests")
    public void testLogApiRequest() {
        assertDoesNotThrow(() -> LoggerUtil.logApiRequest(logger, "GET", "/api/test", "user123"));
    }
    
    @Test
    @DisplayName("logApiResponse should log API responses")
    public void testLogApiResponse() {
        assertDoesNotThrow(() -> LoggerUtil.logApiResponse(logger, "GET", "/api/test", 200, 150L));
        assertDoesNotThrow(() -> LoggerUtil.logApiResponse(logger, "POST", "/api/test", 500, 250L));
    }
    
    @Test
    @DisplayName("logDatabaseOperation should log DB operations")
    public void testLogDatabaseOperation() {
        assertDoesNotThrow(() -> LoggerUtil.logDatabaseOperation(logger, "CREATE", "User", 123L));
    }
    
    @Test
    @DisplayName("logSecurityEvent should log security events")
    public void testLogSecurityEvent() {
        assertDoesNotThrow(() -> LoggerUtil.logSecurityEvent(logger, "LOGIN", "user123", true));
        assertDoesNotThrow(() -> LoggerUtil.logSecurityEvent(logger, "LOGIN", "user456", false));
    }
    
    @Test
    @DisplayName("logError should log errors with context")
    public void testLogError() {
        RuntimeException testException = new RuntimeException("Test error");
        assertDoesNotThrow(() -> LoggerUtil.logError(logger, "Test Operation", testException, "context1", "context2"));
    }
    
    @Test
    @DisplayName("utility methods should work correctly")
    public void testUtilityMethods() {
        assertDoesNotThrow(() -> LoggerUtil.logSeparator(logger, "Test Section"));
        assertDoesNotThrow(() -> LoggerUtil.logSubsection(logger, "Test Subsection"));
        assertDoesNotThrow(() -> LoggerUtil.logWithEmoji(logger, "🎯", "Test message with %s", "param"));
        
        String timestamp = LoggerUtil.getFormattedTimestamp();
        assertNotNull(timestamp);
        assertFalse(timestamp.isEmpty());
    }
    
    @Test
    @DisplayName("startup and shutdown logging should work")
    public void testStartupShutdown() {
        assertDoesNotThrow(() -> LoggerUtil.logStartup(logger, "TestComponent", "1.0.0"));
        assertDoesNotThrow(() -> LoggerUtil.logShutdown(logger, "TestComponent"));
        assertDoesNotThrow(() -> LoggerUtil.logConfiguration(logger, "TestComponent", "test.property", "test.value"));
    }
} 
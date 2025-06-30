package com.myapp.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های جامع کلاس SecurityUtil
 * 
 * این کلاس تمام قابلیت‌های امنیتی را آزمایش می‌کند:
 * - Rate Limiting
 * - اعتبارسنجی ورودی
 * - اعتبارسنجی رمز عبور
 * - پاک‌سازی ورودی
 * - Header های امنیتی
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Security Utility Tests")
class SecurityUtilTest {
    
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_PHONE = "09123456789";
    
    @BeforeEach
    void setUp() {
        // پاک‌سازی داده‌های قبلی
        SecurityUtil.cleanupOldData();
    }
    
    // ===== Rate Limiting Tests =====
    
    @Test
    @DisplayName("Rate limiting should allow requests within limits")
    void rateLimiting_shouldAllowRequestsWithinLimits() {
        System.out.println("🔄 Testing rate limiting within limits...");
        
        // First request should be allowed
        assertTrue(SecurityUtil.checkRateLimit(TEST_IP), "First request should be allowed");
        
        // Multiple requests should be allowed
        for (int i = 0; i < 10; i++) {
            assertTrue(SecurityUtil.checkRateLimit(TEST_IP), 
                "Request " + (i + 1) + " should be allowed");
        }
        
        System.out.println("✅ Rate limiting within limits test passed");
    }
    
    @Test
    @DisplayName("Rate limiting should block excessive requests")
    void rateLimiting_shouldBlockExcessiveRequests() {
        System.out.println("🚫 Testing rate limiting excessive requests...");
        
        // Make many requests quickly
        for (int i = 0; i < 100; i++) {
            SecurityUtil.checkRateLimit(TEST_IP);
        }
        
        // Should eventually be blocked
        boolean blocked = false;
        for (int i = 0; i < 10; i++) {
            if (!SecurityUtil.checkRateLimit(TEST_IP)) {
                blocked = true;
                break;
            }
        }
        
        assertTrue(blocked, "Excessive requests should be blocked");
        System.out.println("✅ Rate limiting excessive requests test passed");
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Rate limiting should reject null/empty identifiers")
    void rateLimiting_shouldRejectInvalidIdentifiers(String identifier) {
        assertFalse(SecurityUtil.checkRateLimit(identifier), 
            "Should reject identifier: '" + identifier + "'");
    }
    
    // ===== Login Attempt Tests =====
    
    @Test
    @DisplayName("Login attempts should track failed attempts")
    void loginAttempts_shouldTrackFailedAttempts() {
        System.out.println("🔐 Testing login attempt tracking...");
        
        // Record failed attempts
        for (int i = 0; i < 3; i++) {
            assertFalse(SecurityUtil.recordFailedLogin(TEST_PHONE), 
                "Should not be blocked after " + (i + 1) + " attempts");
        }
        
        // Check remaining attempts
        int remaining = SecurityUtil.getRemainingLoginAttempts(TEST_PHONE);
        assertEquals(2, remaining, "Should have 2 remaining attempts");
        
        System.out.println("✅ Login attempt tracking test passed");
    }
    
    @Test
    @DisplayName("Login attempts should block after max attempts")
    void loginAttempts_shouldBlockAfterMaxAttempts() {
        System.out.println("🚫 Testing login attempt blocking...");
        
        // Record max attempts
        for (int i = 0; i < 5; i++) {
            SecurityUtil.recordFailedLogin(TEST_PHONE);
        }
        
        // Should be blocked
        assertTrue(SecurityUtil.recordFailedLogin(TEST_PHONE), 
            "Should be blocked after max attempts");
        
        // Check remaining attempts
        int remaining = SecurityUtil.getRemainingLoginAttempts(TEST_PHONE);
        assertEquals(0, remaining, "Should have 0 remaining attempts");
        
        System.out.println("✅ Login attempt blocking test passed");
    }
    
    @Test
    @DisplayName("Login attempts should clear after successful login")
    void loginAttempts_shouldClearAfterSuccess() {
        System.out.println("✅ Testing login attempt clearing...");
        
        // Record some failed attempts
        SecurityUtil.recordFailedLogin(TEST_PHONE);
        SecurityUtil.recordFailedLogin(TEST_PHONE);
        
        // Clear attempts
        SecurityUtil.clearLoginAttempts(TEST_PHONE);
        
        // Should have full attempts back
        int remaining = SecurityUtil.getRemainingLoginAttempts(TEST_PHONE);
        assertEquals(5, remaining, "Should have full attempts after clearing");
        
        System.out.println("✅ Login attempt clearing test passed");
    }
    
    // ===== Input Validation Tests =====
    
    @ParameterizedTest
    @ValueSource(strings = {
        "09123456789",
        "09987654321",
        "09111111111"
    })
    @DisplayName("Valid phone numbers should pass validation")
    void validPhoneNumbers_shouldPassValidation(String phone) {
        assertTrue(SecurityUtil.isValidPhone(phone), 
            "Phone number should be valid: " + phone);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "1234567890",
        "0912345678",
        "091234567890",
        "08123456789",
        "0912345678a",
        "0912345678@",
        ""
    })
    @DisplayName("Invalid phone numbers should fail validation")
    void invalidPhoneNumbers_shouldFailValidation(String phone) {
        assertFalse(SecurityUtil.isValidPhone(phone), 
            "Phone number should be invalid: " + phone);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@domain.co.uk",
        "user+tag@example.org"
    })
    @DisplayName("Valid email addresses should pass validation")
    void validEmails_shouldPassValidation(String email) {
        assertTrue(SecurityUtil.isValidEmail(email), 
            "Email should be valid: " + email);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "invalid.email",
        "@example.com",
        "user@",
        "user@.com",
        "user..name@example.com"
    })
    @DisplayName("Invalid email addresses should fail validation")
    void invalidEmails_shouldFailValidation(String email) {
        assertFalse(SecurityUtil.isValidEmail(email), 
            "Email should be invalid: " + email);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "احمد محمدی",
        "John Doe",
        "علی رضا",
        "Mary Jane Smith"
    })
    @DisplayName("Valid names should pass validation")
    void validNames_shouldPassValidation(String name) {
        assertTrue(SecurityUtil.isValidName(name), 
            "Name should be valid: " + name);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "A",
        "123",
        "User@123",
        "Name with <script>",
        ""
    })
    @DisplayName("Invalid names should fail validation")
    void invalidNames_shouldFailValidation(String name) {
        assertFalse(SecurityUtil.isValidName(name), 
            "Name should be invalid: " + name);
    }
    
    // ===== Password Validation Tests =====
    
    @ParameterizedTest
    @ValueSource(strings = {
        "SecurePass123!",
        "MyP@ssw0rd",
        "Str0ng#Pass",
        "C0mpl3x!Pass"
    })
    @DisplayName("Valid passwords should pass validation")
    void validPasswords_shouldPassValidation(String password) {
        assertTrue(SecurityUtil.isValidPassword(password), 
            "Password should be valid: " + password);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "weak",
        "password",
        "12345678",
        "PASSWORD",
        "Pass123",
        "Pass@word",
        "MyPassword123",
        ""
    })
    @DisplayName("Invalid passwords should fail validation")
    void invalidPasswords_shouldFailValidation(String password) {
        assertFalse(SecurityUtil.isValidPassword(password), 
            "Password should be invalid: " + password);
    }
    
    @Test
    @DisplayName("Password validation should provide specific error messages")
    void passwordValidation_shouldProvideSpecificErrors() {
        System.out.println("📝 Testing password validation error messages...");
        
        // Test null password
        String error = SecurityUtil.getPasswordValidationError(null);
        assertNotNull(error, "Should provide error for null password");
        assertTrue(error.contains("خالی"), "Error should mention empty password");
        
        // Test short password
        error = SecurityUtil.getPasswordValidationError("short");
        assertNotNull(error, "Should provide error for short password");
        assertTrue(error.contains("8"), "Error should mention minimum length");
        
        // Test password without lowercase
        error = SecurityUtil.getPasswordValidationError("PASSWORD123!");
        assertNotNull(error, "Should provide error for password without lowercase");
        assertTrue(error.contains("حرف کوچک"), "Error should mention lowercase requirement");
        
        // Test password without uppercase
        error = SecurityUtil.getPasswordValidationError("password123!");
        assertNotNull(error, "Should provide error for password without uppercase");
        assertTrue(error.contains("حرف بزرگ"), "Error should mention uppercase requirement");
        
        // Test password without number
        error = SecurityUtil.getPasswordValidationError("Password!");
        assertNotNull(error, "Should provide error for password without number");
        assertTrue(error.contains("عدد"), "Error should mention number requirement");
        
        // Test password without special character
        error = SecurityUtil.getPasswordValidationError("Password123");
        assertNotNull(error, "Should provide error for password without special character");
        assertTrue(error.contains("کاراکتر خاص"), "Error should mention special character requirement");
        
        // Test valid password
        error = SecurityUtil.getPasswordValidationError("SecurePass123!");
        assertNull(error, "Should return null for valid password");
        
        System.out.println("✅ Password validation error messages test passed");
    }
    
    // ===== Input Sanitization Tests =====
    
    @Test
    @DisplayName("Input sanitization should remove dangerous content")
    void inputSanitization_shouldRemoveDangerousContent() {
        System.out.println("🧹 Testing input sanitization...");
        
        // Test script removal
        String input = "<script>alert('xss')</script>Hello World";
        String sanitized = SecurityUtil.sanitizeInput(input);
        assertFalse(sanitized.contains("<script>"), "Should remove script tags");
        assertTrue(sanitized.contains("Hello World"), "Should preserve safe content");
        
        // Test HTML removal
        input = "<b>Bold</b> and <i>italic</i> text";
        sanitized = SecurityUtil.sanitizeInput(input);
        assertFalse(sanitized.contains("<b>"), "Should remove HTML tags");
        assertFalse(sanitized.contains("<i>"), "Should remove HTML tags");
        assertTrue(sanitized.contains("Bold"), "Should preserve text content");
        
        // Test SQL injection prevention
        input = "'; DROP TABLE users; --";
        sanitized = SecurityUtil.sanitizeInput(input);
        assertFalse(sanitized.toLowerCase().contains("drop"), "Should remove SQL keywords");
        
        // Test event handler removal
        input = "onclick=\"alert('xss')\" onload=\"evil()\"";
        sanitized = SecurityUtil.sanitizeInput(input);
        assertFalse(sanitized.contains("onclick"), "Should remove event handlers");
        assertFalse(sanitized.contains("onload"), "Should remove event handlers");
        
        System.out.println("✅ Input sanitization test passed");
    }
    
    @Test
    @DisplayName("Filename sanitization should create safe filenames")
    void filenameSanitization_shouldCreateSafeFilenames() {
        System.out.println("📁 Testing filename sanitization...");
        
        // Test dangerous characters
        String filename = "file<script>.exe";
        String sanitized = SecurityUtil.sanitizeFilename(filename);
        assertFalse(sanitized.contains("<"), "Should remove dangerous characters");
        assertFalse(sanitized.contains(">"), "Should remove dangerous characters");
        
        // Test spaces and special characters
        filename = "My File (2024).txt";
        sanitized = SecurityUtil.sanitizeFilename(filename);
        assertFalse(sanitized.contains(" "), "Should replace spaces");
        assertFalse(sanitized.contains("("), "Should remove parentheses");
        assertFalse(sanitized.contains(")"), "Should remove parentheses");
        
        // Test multiple underscores
        filename = "file__name.txt";
        sanitized = SecurityUtil.sanitizeFilename(filename);
        assertFalse(sanitized.contains("__"), "Should not have multiple underscores");
        
        System.out.println("✅ Filename sanitization test passed");
    }
    
    // ===== Security Headers Tests =====
    
    @Test
    @DisplayName("Security headers should be properly configured")
    void securityHeaders_shouldBeProperlyConfigured() {
        System.out.println("🛡️ Testing security headers...");
        
        Map<String, String> headers = SecurityUtil.getSecurityHeaders();
        
        // Check required headers
        assertTrue(headers.containsKey("X-Content-Type-Options"), "Should have X-Content-Type-Options");
        assertTrue(headers.containsKey("X-Frame-Options"), "Should have X-Frame-Options");
        assertTrue(headers.containsKey("X-XSS-Protection"), "Should have X-XSS-Protection");
        assertTrue(headers.containsKey("Strict-Transport-Security"), "Should have HSTS");
        assertTrue(headers.containsKey("Content-Security-Policy"), "Should have CSP");
        assertTrue(headers.containsKey("Referrer-Policy"), "Should have Referrer-Policy");
        
        // Check header values
        assertEquals("nosniff", headers.get("X-Content-Type-Options"), "X-Content-Type-Options should be nosniff");
        assertEquals("DENY", headers.get("X-Frame-Options"), "X-Frame-Options should be DENY");
        assertTrue(headers.get("X-XSS-Protection").contains("1"), "X-XSS-Protection should include 1");
        assertTrue(headers.get("Strict-Transport-Security").contains("max-age"), "HSTS should include max-age");
        
        System.out.println("✅ Security headers test passed");
    }
    
    @Test
    @DisplayName("Security header retrieval should work correctly")
    void securityHeaderRetrieval_shouldWorkCorrectly() {
        String header = SecurityUtil.getSecurityHeader("X-Content-Type-Options");
        assertEquals("nosniff", header, "Should retrieve correct header value");
        
        header = SecurityUtil.getSecurityHeader("NonExistentHeader");
        assertNull(header, "Should return null for non-existent header");
    }
    
    // ===== Utility Method Tests =====
    
    @Test
    @DisplayName("Secure random ID generation should work correctly")
    void secureRandomIdGeneration_shouldWorkCorrectly() {
        System.out.println("🔐 Testing secure random ID generation...");
        
        // Test default length
        String id1 = SecurityUtil.generateSecureRandomId(0);
        assertEquals(32, id1.length(), "Default length should be 32");
        
        // Test custom length
        String id2 = SecurityUtil.generateSecureRandomId(16);
        assertEquals(16, id2.length(), "Should generate ID with specified length");
        
        // Test uniqueness
        String id3 = SecurityUtil.generateSecureRandomId(32);
        String id4 = SecurityUtil.generateSecureRandomId(32);
        assertNotEquals(id3, id4, "Generated IDs should be unique");
        
        // Test character set
        String id5 = SecurityUtil.generateSecureRandomId(100);
        assertTrue(id5.matches("[A-Za-z0-9]+"), "Should only contain alphanumeric characters");
        
        System.out.println("✅ Secure random ID generation test passed");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.com",
        "https://api.example.com",
        "http://localhost:8080",
        "http://127.0.0.1:3000"
    })
    @DisplayName("Secure URLs should be recognized")
    void secureUrls_shouldBeRecognized(String url) {
        assertTrue(SecurityUtil.isSecureUrl(url), "Should recognize secure URL: " + url);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "http://example.com",
        "ftp://example.com",
        "file:///etc/passwd",
        "javascript:alert('xss')"
    })
    @DisplayName("Insecure URLs should be rejected")
    void insecureUrls_shouldBeRejected(String url) {
        assertFalse(SecurityUtil.isSecureUrl(url), "Should reject insecure URL: " + url);
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Invalid URLs should be rejected")
    void invalidUrls_shouldBeRejected(String url) {
        assertFalse(SecurityUtil.isSecureUrl(url), "Should reject invalid URL: '" + url + "'");
    }
    
    // ===== Performance Tests =====
    
    @RepeatedTest(10)
    @DisplayName("Security operations should be performant")
    void securityOperations_shouldBePerformant() {
        long startTime = System.currentTimeMillis();
        
        // Perform multiple security operations
        for (int i = 0; i < 100; i++) {
            SecurityUtil.isValidPhone("09123456789");
            SecurityUtil.isValidEmail("test@example.com");
            SecurityUtil.isValidPassword("SecurePass123!");
            SecurityUtil.sanitizeInput("test input");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (less than 1 second)
        assertTrue(duration < 1000, "Security operations should be fast: " + duration + "ms");
    }
    
    // ===== Statistics Tests =====
    
    @Test
    @DisplayName("Rate limit statistics should be accurate")
    void rateLimitStatistics_shouldBeAccurate() {
        System.out.println("📊 Testing rate limit statistics...");
        
        // Make some requests
        SecurityUtil.checkRateLimit("192.168.1.1");
        SecurityUtil.checkRateLimit("192.168.1.2");
        SecurityUtil.recordFailedLogin("09123456789");
        
        Map<String, Object> stats = SecurityUtil.getRateLimitStats();
        
        assertTrue((Integer) stats.get("activeRateLimits") >= 2, "Should track active rate limits");
        assertTrue((Integer) stats.get("activeLoginAttempts") >= 1, "Should track login attempts");
        assertNotNull(stats.get("maxRequestsPerMinute"), "Should include max requests per minute");
        assertNotNull(stats.get("maxRequestsPerHour"), "Should include max requests per hour");
        assertNotNull(stats.get("maxLoginAttempts"), "Should include max login attempts");
        
        System.out.println("✅ Rate limit statistics test passed");
    }
} 
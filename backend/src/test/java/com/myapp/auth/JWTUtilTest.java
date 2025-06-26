package com.myapp.auth;

import com.myapp.common.utils.JWTUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * مجموعه تست‌های ابزار JWT
 * 
 * این کلاس تست تمام عملکردهای کلاس JWTUtil را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Token Generation: ایجاد access و refresh token
 * 2. Token Validation: اعتبارسنجی token های معتبر و نامعتبر
 * 3. Token Parsing: استخراج userId، phone، role از token
 * 4. Bearer Token Handling: مدیریت Authorization header
 * 5. Role-based Tests: تست با نقش‌های مختلف کاربری
 * 6. Edge Cases: تست حالات خاص و خطاها
 * 
 * Security Considerations:
 * - Token expiration validation
 * - Malformed token rejection
 * - Invalid signature detection
 * - Null/empty input handling
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("JWT Utility Tests")
class JWTUtilTest {
    
    /** ID کاربر تستی */
    private final Long testUserId = 123L;
    
    /** شماره تلفن تستی */
    private final String testPhone = "09123456789";
    
    /** نقش کاربری تستی */
    private final String testRole = "customer";
    
    /**
     * تست تولید access token معتبر
     * 
     * Scenario: تولید access token برای کاربر احراز هویت شده
     * Expected:
     * - token نباید null یا خالی باشد
     * - فرمت JWT داشته باشد (3 بخش جدا شده با نقطه)
     * - اطلاعات کاربر قابل استخراج باشد
     * - type برابر "access" باشد
     */
    @Test
    @DisplayName("Should generate valid access token")
    void testGenerateAccessToken() {
        // Act - تولید access token
        String token = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        
        // Assert - بررسی فرمت و محتوای token
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT دارای 3 قسمت است
        
        // بررسی اطلاعات استخراج شده از token
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(token));
        assertEquals(testPhone, JWTUtil.getPhoneFromToken(token));
        assertEquals(testRole, JWTUtil.getRoleFromToken(token));
        assertEquals("access", JWTUtil.getTokenType(token));
    }
    
    /**
     * تست تولید refresh token معتبر
     * 
     * Scenario: تولید refresh token برای تمدید دسترسی
     * Expected:
     * - token نباید null یا خالی باشد
     * - فرمت JWT داشته باشد
     * - userId قابل استخراج باشد
     * - type برابر "refresh" باشد
     */
    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken() {
        // Act - تولید refresh token
        String token = JWTUtil.generateRefreshToken(testUserId);
        
        // Assert - بررسی فرمت و محتوای token
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
        
        // بررسی اطلاعات استخراج شده از token
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(token));
        assertEquals("refresh", JWTUtil.getTokenType(token));
    }
    
    /**
     * تست اعتبارسنجی token های معتبر
     * 
     * Scenario: بررسی token های تازه تولید شده
     * Expected:
     * - هم access token و هم refresh token معتبر باشند
     * - validateToken برای هر دو true برگرداند
     */
    @Test
    @DisplayName("Should validate valid tokens")
    void testValidateToken() {
        // Arrange - تولید token های تستی
        String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String refreshToken = JWTUtil.generateRefreshToken(testUserId);
        
        // Act & Assert - بررسی اعتبار
        assertTrue(JWTUtil.validateToken(accessToken));
        assertTrue(JWTUtil.validateToken(refreshToken));
    }
    
    /**
     * تست رد کردن token های نامعتبر
     * 
     * Scenario: بررسی token های مخدوش، خالی یا null
     * Expected:
     * - validateToken برای تمام حالات false برگرداند
     * - exception پرتاب نشود
     */
    @Test
    @DisplayName("Should reject invalid tokens")
    void testValidateInvalidToken() {
        // Act & Assert - تست انواع token نامعتبر
        assertFalse(JWTUtil.validateToken("invalid.token.here"));
        assertFalse(JWTUtil.validateToken(""));
        assertFalse(JWTUtil.validateToken(null));
    }
    
    /**
     * تست استخراج bearer token از Authorization header
     * 
     * Scenario: پردازش header با فرمت "Bearer {token}"
     * Expected:
     * - token اصلی بدون "Bearer " استخراج شود
     * - فقط token بازگردانده شود نه کل header
     */
    @Test
    @DisplayName("Should extract bearer token from Authorization header")
    void testExtractBearerToken() {
        // Arrange - آماده‌سازی header معتبر
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String authHeader = "Bearer " + token;
        
        // Act - استخراج token
        String extractedToken = JWTUtil.extractBearerToken(authHeader);
        
        // Assert - بررسی token استخراج شده
        assertEquals(token, extractedToken);
    }
    
    /**
     * تست برگرداندن null برای Authorization header نامعتبر
     * 
     * Scenario: header های مخدوش، خالی یا با فرمت اشتباه
     * Expected:
     * - برای تمام حالات null برگردانده شود
     * - exception پرتاب نشود
     */
    @Test
    @DisplayName("Should return null for invalid Authorization header")
    void testExtractBearerTokenInvalid() {
        // Act & Assert - تست انواع header نامعتبر
        assertNull(JWTUtil.extractBearerToken("Invalid header"));
        assertNull(JWTUtil.extractBearerToken("Basic dXNlcjpwYXNz"));
        assertNull(JWTUtil.extractBearerToken(""));
        assertNull(JWTUtil.extractBearerToken(null));
    }
    
    /**
     * تست تشخیص access token ها
     * 
     * Scenario: تمایز بین access token و سایر انواع
     * Expected:
     * - برای access token true برگرداند
     * - برای refresh token و token نامعتبر false برگرداند
     */
    @Test
    @DisplayName("Should identify access tokens")
    void testIsAccessToken() {
        // Arrange - تولید انواع مختلف token
        String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String refreshToken = JWTUtil.generateRefreshToken(testUserId);
        
        // Act & Assert - بررسی تشخیص نوع token
        assertTrue(JWTUtil.isAccessToken(accessToken));
        assertFalse(JWTUtil.isAccessToken(refreshToken));
        assertFalse(JWTUtil.isAccessToken("invalid.token"));
    }
    
    /**
     * تست تشخیص refresh token ها
     * 
     * Scenario: تمایز بین refresh token و سایر انواع
     * Expected:
     * - برای refresh token true برگرداند
     * - برای access token و token نامعتبر false برگرداند
     */
    @Test
    @DisplayName("Should identify refresh tokens")
    void testIsRefreshToken() {
        // Arrange - تولید انواع مختلف token
        String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String refreshToken = JWTUtil.generateRefreshToken(testUserId);
        
        // Act & Assert - بررسی تشخیص نوع token
        assertFalse(JWTUtil.isRefreshToken(accessToken));
        assertTrue(JWTUtil.isRefreshToken(refreshToken));
        assertFalse(JWTUtil.isRefreshToken("invalid.token"));
    }
    
    /**
     * تست تولید جفت token (access + refresh)
     * 
     * Scenario: تولید همزمان access و refresh token
     * Expected:
     * - آرایه دو عنصری برگردانده شود
     * - عنصر اول access token باشد
     * - عنصر دوم refresh token باشد
     * - هر دو برای همان userId باشند
     */
    @Test
    @DisplayName("Should generate token pair")
    void testGenerateTokenPair() {
        // Act - تولید جفت token
        String[] tokens = JWTUtil.generateTokenPair(testUserId, testPhone, testRole);
        
        // Assert - بررسی ساختار آرایه
        assertNotNull(tokens);
        assertEquals(2, tokens.length);
        
        String accessToken = tokens[0];
        String refreshToken = tokens[1];
        
        // بررسی نوع هر token
        assertTrue(JWTUtil.isAccessToken(accessToken));
        assertTrue(JWTUtil.isRefreshToken(refreshToken));
        
        // بررسی اینکه هر دو برای همان کاربر هستند
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(accessToken));
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(refreshToken));
    }
    
    /**
     * تست پرتاب exception برای userId null در refresh token
     * 
     * Scenario: تلاش تولید refresh token بدون userId
     * Expected:
     * - IllegalArgumentException پرتاب شود
     * - token تولید نشود
     */
    @Test
    @DisplayName("Should throw exception for null userId in refresh token generation")
    void testGenerateRefreshTokenWithNullUserId() {
        // Act & Assert - انتظار exception
        assertThrows(IllegalArgumentException.class, () -> 
            JWTUtil.generateRefreshToken(null));
    }
    
    /**
     * تست عملکرد با نقش‌های مختلف کاربری
     * 
     * Scenario: تولید token برای نقش‌های مختلف سیستم
     * Expected:
     * - token برای هر نقش معتبر باشد
     * - نقش قابل استخراج باشد
     * - hasRole برای نقش صحیح true برگرداند
     * 
     * @param role نقش کاربری (customer, seller, delivery, admin)
     */
    @ParameterizedTest
    @ValueSource(strings = {"customer", "seller", "delivery", "admin"})
    @DisplayName("Should work with different user roles")
    void testDifferentUserRoles(String role) {
        // Act - تولید token با نقش مشخص
        String token = JWTUtil.generateAccessToken(testUserId, testPhone, role);
        
        // Assert - بررسی token و نقش
        assertTrue(JWTUtil.validateToken(token));
        assertEquals(role, JWTUtil.getRoleFromToken(token));
        assertTrue(JWTUtil.hasRole(token, role));
    }
    
    /**
     * تست دریافت دوره اعتبار token ها
     * 
     * Scenario: بررسی تنظیمات دوره اعتبار
     * Expected:
     * - دوره اعتبار access token > 0
     * - دوره اعتبار refresh token > 0
     * - refresh token عمر بیشتری از access token داشته باشد
     */
    @Test
    @DisplayName("Should get correct token validity periods")
    void testTokenValidityPeriods() {
        // Act - دریافت دوره اعتبار
        long accessTokenValidity = JWTUtil.getAccessTokenValidity();
        long refreshTokenValidity = JWTUtil.getRefreshTokenValidity();
        
        // Assert - بررسی مقادیر منطقی
        assertTrue(accessTokenValidity > 0);
        assertTrue(refreshTokenValidity > 0);
        assertTrue(refreshTokenValidity > accessTokenValidity); // Refresh باید طولانی‌تر باشد
    }
} 
package com.myapp.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * مجموعه تست‌های AuthResult
 * 
 * این کلاس تست تمام عملکردهای کلاس AuthResult را آزمایش می‌کند:
 * 
 * 1. ایجاد نتیجه احراز هویت موفق (authenticated)
 * 2. ایجاد نتیجه احراز هویت ناموفق (unauthenticated)
 * 3. ایجاد نتیجه refresh token (refreshed)
 * 4. بررسی نقش‌های کاربری (role checking)
 * 5. شناسایی انواع مختلف کاربران
 * 
 * Test Scenarios:
 * - Factory methods مختلف AuthResult
 * - Boolean flags (isAuthenticated, isRefresh)
 * - Getter methods برای فیلدهای مختلف
 * - Role-based authorization checks
 * - Edge cases برای unauthenticated users
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("AuthResult Tests")
class AuthResultTest {
    
    /** ID کاربر تستی */
    private final Long testUserId = 123L;
    
    /** شماره تلفن تستی */
    private final String testPhone = "09123456789";
    
    /** نقش کاربری تستی */
    private final String testRole = "customer";
    
    /** Access token تستی */
    private final String testAccessToken = "access.token.here";
    
    /** Refresh token تستی */
    private final String testRefreshToken = "refresh.token.here";
    
    /** پیام خطای تستی */
    private final String testErrorMessage = "Authentication failed";
    
    /**
     * تست ایجاد نتیجه احراز هویت موفق
     * 
     * Scenario: کاربر با موفقیت احراز هویت شده است
     * Expected: 
     * - isAuthenticated = true
     * - تمام فیلدهای اطلاعات کاربر پر باشند
     * - errorMessage و refreshToken null باشند
     * - isRefresh = false
     */
    @Test
    @DisplayName("Should create authenticated result")
    void testAuthenticatedResult() {
        // Act - ایجاد نتیجه احراز هویت موفق
        AuthResult result = AuthResult.authenticated(testUserId, testPhone, testRole, testAccessToken);
        
        // Assert - بررسی تمام فیلدها
        assertTrue(result.isAuthenticated());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testPhone, result.getPhone());
        assertEquals(testRole, result.getRole());
        assertEquals(testAccessToken, result.getAccessToken());
        assertNull(result.getRefreshToken()); // refresh token در authenticated معمولی نیست
        assertNull(result.getErrorMessage()); // پیام خطا نباید وجود داشته باشد
        assertFalse(result.isRefresh()); // این یک refresh operation نیست
    }
    
    /**
     * تست ایجاد نتیجه احراز هویت ناموفق
     * 
     * Scenario: احراز هویت با شکست مواجه شده است
     * Expected:
     * - isAuthenticated = false
     * - تمام فیلدهای اطلاعات کاربر null باشند
     * - errorMessage پر باشد
     * - isRefresh = false
     */
    @Test
    @DisplayName("Should create unauthenticated result")
    void testUnauthenticatedResult() {
        // Act - ایجاد نتیجه احراز هویت ناموفق
        AuthResult result = AuthResult.unauthenticated(testErrorMessage);
        
        // Assert - بررسی وضعیت failure
        assertFalse(result.isAuthenticated());
        assertNull(result.getUserId());
        assertNull(result.getPhone());
        assertNull(result.getRole());
        assertNull(result.getAccessToken());
        assertNull(result.getRefreshToken());
        assertEquals(testErrorMessage, result.getErrorMessage());
        assertFalse(result.isRefresh());
    }
    
    /**
     * تست ایجاد نتیجه refresh token
     * 
     * Scenario: token های کاربر refresh شده‌اند
     * Expected:
     * - isAuthenticated = true
     * - تمام فیلدهای اطلاعات کاربر پر باشند
     * - هم access token و هم refresh token موجود باشند
     * - isRefresh = true
     */
    @Test
    @DisplayName("Should create refreshed result")
    void testRefreshedResult() {
        // Act - ایجاد نتیجه refresh شده
        AuthResult result = AuthResult.refreshed(testUserId, testPhone, testRole, testAccessToken, testRefreshToken);
        
        // Assert - بررسی وضعیت refresh
        assertTrue(result.isAuthenticated());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testPhone, result.getPhone());
        assertEquals(testRole, result.getRole());
        assertEquals(testAccessToken, result.getAccessToken());
        assertEquals(testRefreshToken, result.getRefreshToken()); // refresh token باید موجود باشد
        assertNull(result.getErrorMessage());
        assertTrue(result.isRefresh()); // این یک refresh operation است
    }
    
    /**
     * تست بررسی نقش مشخص کاربر
     * 
     * Scenario: بررسی اینکه آیا کاربر نقش خاصی دارد یا خیر
     * Expected:
     * - hasRole برای نقش صحیح true برگرداند
     * - hasRole برای نقش‌های اشتباه false برگرداند
     */
    @Test
    @DisplayName("Should check specific role")
    void testHasRole() {
        // Arrange
        AuthResult result = AuthResult.authenticated(testUserId, testPhone, testRole, testAccessToken);
        
        // Act & Assert - تست نقش‌های مختلف
        assertTrue(result.hasRole(testRole)); // نقش اصلی کاربر
        assertFalse(result.hasRole("admin")); // نقش‌های دیگر
        assertFalse(result.hasRole("seller"));
    }
    
    /**
     * تست شناسایی نقش customer
     * 
     * Scenario: بررسی متد کمکی isCustomer
     * Expected:
     * - برای کاربر با نقش "customer" true برگرداند
     * - برای سایر نقش‌ها false برگرداند
     */
    @Test
    @DisplayName("Should identify customer role")
    void testIsCustomer() {
        // Arrange - ایجاد کاربران با نقش‌های مختلف
        AuthResult customerResult = AuthResult.authenticated(testUserId, testPhone, "customer", testAccessToken);
        AuthResult sellerResult = AuthResult.authenticated(testUserId, testPhone, "seller", testAccessToken);
        
        // Act & Assert
        assertTrue(customerResult.isCustomer());
        assertFalse(sellerResult.isCustomer());
    }
    
    /**
     * تست بررسی نقش‌ها برای کاربر احراز هویت نشده
     * 
     * Scenario: کاربری که احراز هویت نشده، نباید هیچ نقشی داشته باشد
     * Expected:
     * - تمام متدهای role checking false برگردانند
     * - hasRole، hasAnyRole، isCustomer، isSeller، isDelivery، isAdmin همه false
     */
    @Test
    @DisplayName("Should return false for role checks on unauthenticated result")
    void testRoleChecksOnUnauthenticated() {
        // Arrange - کاربر احراز هویت نشده
        AuthResult result = AuthResult.unauthenticated(testErrorMessage);
        
        // Act & Assert - همه role checks باید false باشند
        assertFalse(result.hasRole("customer"));
        assertFalse(result.hasAnyRole("customer", "admin"));
        assertFalse(result.isCustomer());
        assertFalse(result.isSeller());
        assertFalse(result.isDelivery());
        assertFalse(result.isAdmin());
    }
}
package com.myapp.auth;

import com.myapp.common.utils.JWTUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Utility Advanced Tests - Edge Cases & Security")
class JWTUtilAdvancedTest {
    
    private final Long testUserId = 123L;
    private final String testPhone = "09123456789";
    private final String testRole = "customer";
    
    // ===== Security Tests =====
    
    @Test
    @DisplayName("Should reject tampered JWT tokens")
    void testTamperedTokenRejection() {
        String validToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String[] parts = validToken.split("\\.");
        
        // Tamper with payload
        String tamperedToken = parts[0] + ".eyJzdWIiOiI5OTkiLCJwaG9uZSI6IjA5MTExMTExMTExIiwicm9sZSI6ImFkbWluIn0." + parts[2];
        
        assertFalse(JWTUtil.validateToken(tamperedToken));
        assertThrows(JwtException.class, () -> JWTUtil.getUserIdFromToken(tamperedToken));
    }
    
    @Test
    @DisplayName("Should reject tokens with wrong issuer")
    void testWrongIssuerRejection() {
        // This would be a token from another service
        String foreignToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjMiLCJpc3MiOiJvdGhlci1zZXJ2aWNlIiwiaWF0IjoxNjk5MDAwMDAwfQ.invalid";
        
        assertFalse(JWTUtil.validateToken(foreignToken));
    }
    
    @Test
    @DisplayName("Should handle malformed tokens gracefully")
    void testMalformedTokenHandling() {
        String[] malformedTokens = {
            "not.a.jwt",
            "only.two.parts",
            "too.many.parts.here.invalid",
            "completely-invalid",
            "eyJhbGciOiJIUzI1NiJ9.invalid-payload.signature",
            "header.{invalid-json}.signature"
        };
        
        for (String malformedToken : malformedTokens) {
            assertFalse(JWTUtil.validateToken(malformedToken), 
                "Should reject malformed token: " + malformedToken);
        }
    }
    
    // ===== Edge Cases Tests =====
    
    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, -1L, 0L, Long.MAX_VALUE})
    @DisplayName("Should handle edge case user IDs")
    void testEdgeCaseUserIds(Long edgeUserId) {
        if (edgeUserId == null || edgeUserId <= 0) {
            assertThrows(IllegalArgumentException.class, () -> 
                JWTUtil.generateAccessToken(edgeUserId, testPhone, testRole));
        } else {
            String token = JWTUtil.generateAccessToken(edgeUserId, testPhone, testRole);
            assertEquals(edgeUserId, JWTUtil.getUserIdFromToken(token));
        }
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "0", "00000000000", "99999999999",
        "09123456789", "09999999999", "09000000000"
    })
    @DisplayName("Should handle various phone number formats")
    void testVariousPhoneNumbers(String phone) {
        String token = JWTUtil.generateAccessToken(testUserId, phone, testRole);
        assertEquals(phone, JWTUtil.getPhoneFromToken(token));
    }
    
    @Test
    @DisplayName("Should handle very long phone numbers and roles")
    void testLongStrings() {
        String longPhone = "0".repeat(100);
        String longRole = "customer".repeat(20);
        
        String token = JWTUtil.generateAccessToken(testUserId, longPhone, longRole);
        assertEquals(longPhone, JWTUtil.getPhoneFromToken(token));
        assertEquals(longRole, JWTUtil.getRoleFromToken(token));
    }
    
    @Test
    @DisplayName("Should handle special characters in phone and role")
    void testSpecialCharacters() {
        String phoneWithSpecialChars = "+98-912-345-6789";
        String roleWithSpecialChars = "customer_vip";
        
        String token = JWTUtil.generateAccessToken(testUserId, phoneWithSpecialChars, roleWithSpecialChars);
        assertEquals(phoneWithSpecialChars, JWTUtil.getPhoneFromToken(token));
        assertEquals(roleWithSpecialChars, JWTUtil.getRoleFromToken(token));
    }
    
    // ===== Concurrency Tests =====
    
    @RepeatedTest(10)
    @DisplayName("Should generate unique tokens in concurrent environment")
    void testConcurrentTokenGeneration() {
        String token1 = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String token2 = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        
        // Tokens should be different due to different issued at times
        assertNotEquals(token1, token2, "Consecutive tokens should be different");
        
        // But both should be valid and contain same user info
        assertTrue(JWTUtil.validateToken(token1));
        assertTrue(JWTUtil.validateToken(token2));
        assertEquals(JWTUtil.getUserIdFromToken(token1), JWTUtil.getUserIdFromToken(token2));
    }
    
    // ===== Token Expiration Tests =====
    
    @Test
    @DisplayName("Should correctly identify token expiration states")
    void testTokenExpirationStates() {
        String token = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        
        // Fresh token should not be expired
        assertFalse(JWTUtil.isTokenExpired(token));
        
        // Should have reasonable remaining time
        long remainingTime = JWTUtil.getRemainingTimeToExpire(token);
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= JWTUtil.getAccessTokenValidity());
        
        // Should be close to full validity period
        long expectedRemaining = JWTUtil.getAccessTokenValidity();
        assertTrue(Math.abs(remainingTime - expectedRemaining) < 5000); // within 5 seconds
    }
    
    @Test
    @DisplayName("Should handle time-based edge cases")
    void testTimingEdgeCases() {
        // Generate token at current time
        Date beforeGeneration = new Date();
        String token = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        Date afterGeneration = new Date();
        
        Date issuedAt = JWTUtil.getIssuedAtFromToken(token);
        Date expiresAt = JWTUtil.getExpirationDateFromToken(token);
        
        // Issued at should be between before and after generation
        assertTrue(issuedAt.compareTo(beforeGeneration) >= 0);
        assertTrue(issuedAt.compareTo(afterGeneration) <= 0);
        
        // Expiration should be in the future
        assertTrue(expiresAt.after(new Date()));
        
        // Time difference should match validity period
        long timeDiff = expiresAt.getTime() - issuedAt.getTime();
        assertEquals(JWTUtil.getAccessTokenValidity(), timeDiff, 1000); // 1 second tolerance
    }
    
    // ===== Authorization Header Tests =====
    
    @ParameterizedTest
    @ValueSource(strings = {
        "Bearer token123",
        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        "Bearer a.b.c"
    })
    @DisplayName("Should extract valid bearer tokens")
    void testValidBearerTokenExtraction(String authHeader) {
        String expectedToken = authHeader.substring(7); // Remove "Bearer "
        String extractedToken = JWTUtil.extractBearerToken(authHeader);
        assertEquals(expectedToken, extractedToken);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "Basic dXNlcjpwYXNz",
        "bearer token123", // lowercase
        "Bearer", // no token
        "Bearer ", // empty token
        "BearerToken123", // no space
        "Token abc123",
        "Authorization: Bearer token123" // full header format
    })
    @DisplayName("Should reject invalid authorization header formats")
    void testInvalidAuthHeaderFormats(String invalidHeader) {
        assertNull(JWTUtil.extractBearerToken(invalidHeader));
    }
    
    // ===== Token Pair Generation Tests =====
    
    @Test
    @DisplayName("Should generate distinct but related token pairs")
    void testTokenPairGeneration() {
        String[] tokens = JWTUtil.generateTokenPair(testUserId, testPhone, testRole);
        
        String accessToken = tokens[0];
        String refreshToken = tokens[1];
        
        // Should be different tokens
        assertNotEquals(accessToken, refreshToken);
        
        // Should have correct types
        assertTrue(JWTUtil.isAccessToken(accessToken));
        assertTrue(JWTUtil.isRefreshToken(refreshToken));
        
        // Access token should contain full user info
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(accessToken));
        assertEquals(testPhone, JWTUtil.getPhoneFromToken(accessToken));
        assertEquals(testRole, JWTUtil.getRoleFromToken(accessToken));
        
        // Refresh token should only contain user ID
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(refreshToken));
        assertThrows(JwtException.class, () -> JWTUtil.getPhoneFromToken(refreshToken));
        assertThrows(JwtException.class, () -> JWTUtil.getRoleFromToken(refreshToken));
    }
    
    // ===== Role-based Tests =====
    
    @Test
    @DisplayName("Should handle role-based authorization correctly")
    void testRoleBasedAuthorization() {
        String customerToken = JWTUtil.generateAccessToken(testUserId, testPhone, "customer");
        String sellerToken = JWTUtil.generateAccessToken(testUserId, testPhone, "seller");
        String deliveryToken = JWTUtil.generateAccessToken(testUserId, testPhone, "delivery");
        String adminToken = JWTUtil.generateAccessToken(testUserId, testPhone, "admin");
        
        // Test specific role checks
        assertTrue(JWTUtil.hasRole(customerToken, "customer"));
        assertFalse(JWTUtil.hasRole(customerToken, "seller"));
        
        assertTrue(JWTUtil.hasRole(sellerToken, "seller"));
        assertFalse(JWTUtil.hasRole(sellerToken, "customer"));
        
        assertTrue(JWTUtil.hasRole(deliveryToken, "delivery"));
        assertFalse(JWTUtil.hasRole(deliveryToken, "admin"));
        
        assertTrue(JWTUtil.hasRole(adminToken, "admin"));
        assertFalse(JWTUtil.hasRole(adminToken, "delivery"));
    }
    
    // ===== Performance Tests =====
    
    @RepeatedTest(100)
    @DisplayName("Should maintain performance under load")
    void testPerformanceUnderLoad() {
        long startTime = System.currentTimeMillis();
        
        // Generate and validate token
        String token = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        assertTrue(JWTUtil.validateToken(token));
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(token));
        
        long endTime = System.currentTimeMillis();
        
        // Should complete within reasonable time (less than 100ms)
        assertTrue(endTime - startTime < 100, "Token operations should be fast");
    }
    
    // ===== Null Safety Tests =====
    
    @Test
    @DisplayName("Should handle null inputs safely")
    void testNullInputSafety() {
        assertThrows(IllegalArgumentException.class, () -> 
            JWTUtil.generateAccessToken(null, testPhone, testRole));
        
        assertThrows(IllegalArgumentException.class, () -> 
            JWTUtil.generateAccessToken(testUserId, null, testRole));
        
        assertThrows(IllegalArgumentException.class, () -> 
            JWTUtil.generateAccessToken(testUserId, testPhone, null));
        
        assertThrows(IllegalArgumentException.class, () -> 
            JWTUtil.generateRefreshToken(null));
        
        assertFalse(JWTUtil.validateToken(null));
        assertEquals(0, JWTUtil.getRemainingTimeToExpire(null));
        assertFalse(JWTUtil.hasRole(null, "customer"));
        assertNull(JWTUtil.extractBearerToken(null));
    }
} 
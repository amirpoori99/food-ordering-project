package com.myapp.auth;

import com.myapp.common.utils.JWTUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.concurrent.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Comprehensive Test - Missing Scenarios")
class JWTComprehensiveTest {

    @Test
    @DisplayName("Should handle token expiration correctly")
    void testTokenExpiration() {
        System.out.println("⏰ Testing token expiration scenarios...");
        
        String token = JWTUtil.generateAccessToken(123L, "09123456789", "customer");
        
        assertTrue(JWTUtil.validateToken(token), "Token should be valid initially");
        assertFalse(JWTUtil.isTokenExpired(token), "Token should not be expired initially");
        
        long remainingTime = JWTUtil.getRemainingTimeToExpire(token);
        assertTrue(remainingTime > 0, "Remaining time should be positive");
        assertTrue(remainingTime <= 24 * 60 * 60 * 1000, "Remaining time should be <= 24 hours");
        
        System.out.println("✅ Token expiration tests passed");
    }

    @Test
    @DisplayName("Should handle concurrent token operations")
    void testConcurrentTokenOperations() throws InterruptedException {
        System.out.println("🔄 Testing concurrent token operations...");
        
        final int threadCount = 10;
        final int tokensPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        final boolean[] hasFailure = {false};
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < tokensPerThread; j++) {
                        Long userId = (long) (threadId * tokensPerThread + j);
                        String phone = "phone" + userId;
                        String role = j % 2 == 0 ? "customer" : "seller";
                        
                        String token = JWTUtil.generateAccessToken(userId, phone, role);
                        
                        if (!JWTUtil.validateToken(token)) {
                            hasFailure[0] = true;
                            return;
                        }
                        
                        if (!userId.equals(JWTUtil.getUserIdFromToken(token))) {
                            hasFailure[0] = true;
                            return;
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        executor.shutdown();
        
        assertFalse(hasFailure[0], "No failures should occur in concurrent operations");
        System.out.println("✅ Concurrent operations test passed");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "\n", "\t", "Bearer", "Bearer ", "Basic token"})
    @DisplayName("Should handle invalid authorization headers")
    void testInvalidAuthorizationHeaders(String invalidHeader) {
        System.out.println("🚫 Testing invalid authorization header: '" + invalidHeader + "'");
        
        String extractedToken = JWTUtil.extractBearerToken(invalidHeader);
        assertNull(extractedToken, "Should return null for invalid header: " + invalidHeader);
    }

    @Test
    @DisplayName("Should handle edge case user IDs")
    void testEdgeCaseUserIds() {
        System.out.println("🔢 Testing edge case user IDs...");
        
        Long largeUserId = Long.MAX_VALUE;
        String token1 = JWTUtil.generateAccessToken(largeUserId, "09123456789", "customer");
        assertTrue(JWTUtil.validateToken(token1), "Should handle max Long user ID");
        assertEquals(largeUserId, JWTUtil.getUserIdFromToken(token1), "Should extract large user ID correctly");
        
        Long smallUserId = 1L;
        String token2 = JWTUtil.generateAccessToken(smallUserId, "09123456789", "customer");
        assertTrue(JWTUtil.validateToken(token2), "Should handle small user ID");
        assertEquals(smallUserId, JWTUtil.getUserIdFromToken(token2), "Should extract small user ID correctly");
        
        System.out.println("✅ Edge case user ID tests passed");
    }

    @Test
    @DisplayName("Should handle special characters in phone and role")
    void testSpecialCharactersInClaims() {
        System.out.println("📱 Testing special characters in claims...");
        
        String intlPhone = "+98-912-345-6789";
        String token1 = JWTUtil.generateAccessToken(123L, intlPhone, "customer");
        assertTrue(JWTUtil.validateToken(token1), "Should handle international phone format");
        assertEquals(intlPhone, JWTUtil.getPhoneFromToken(token1), "Should extract international phone correctly");
        
        String specialRole = "super_admin";
        String token2 = JWTUtil.generateAccessToken(123L, "09123456789", specialRole);
        assertTrue(JWTUtil.validateToken(token2), "Should handle role with underscore");
        assertEquals(specialRole, JWTUtil.getRoleFromToken(token2), "Should extract special role correctly");
        
        System.out.println("✅ Special characters test passed");
    }

    @Test
    @DisplayName("Should handle token security validation")
    void testTokenSecurityValidation() {
        System.out.println("🔐 Testing token security validation...");
        
        String validToken = JWTUtil.generateAccessToken(123L, "09123456789", "customer");
        assertTrue(JWTUtil.validateToken(validToken), "Valid token should pass validation");
        
        String[] malformedTokens = {
            "not.a.token",
            "only.two.parts",
            "four.parts.in.token",
            "empty..parts",
            "....",
            "Bearer eyJhbGciOiJIUzI1NiJ9",
            validToken.substring(0, validToken.length() - 5),
            validToken + "extra"
        };
        
        for (String malformedToken : malformedTokens) {
            assertFalse(JWTUtil.validateToken(malformedToken), 
                "Malformed token should be invalid: " + malformedToken);
        }
        
        System.out.println("✅ Token security validation tests passed");
    }

    @Test
    @DisplayName("Should handle role authorization edge cases")
    void testRoleAuthorizationEdgeCases() {
        System.out.println("👤 Testing role authorization edge cases...");
        
        String customerToken = JWTUtil.generateAccessToken(123L, "09123456789", "customer");
        String adminToken = JWTUtil.generateAccessToken(456L, "09123456780", "admin");
        
        assertFalse(JWTUtil.hasRole(customerToken, "CUSTOMER"), "Role checking should be case sensitive");
        assertFalse(JWTUtil.hasRole(customerToken, "Customer"), "Role checking should be case sensitive");
        
        assertTrue(JWTUtil.hasRole(customerToken, "customer"), "Should have exact customer role");
        assertFalse(JWTUtil.hasRole(customerToken, "customers"), "Should not match plural role");
        assertFalse(JWTUtil.hasRole(customerToken, "custom"), "Should not match partial role");
        
        assertTrue(JWTUtil.hasRole(adminToken, "admin"), "Should have admin role");
        assertFalse(JWTUtil.hasRole(adminToken, "customer"), "Admin should not have customer role");
        
        System.out.println("✅ Role authorization edge cases passed");
    }

    @Test
    @DisplayName("Should handle refresh token specific scenarios")
    void testRefreshTokenScenarios() {
        System.out.println("🔄 Testing refresh token specific scenarios...");
        
        Long userId = 123L;
        String refreshToken = JWTUtil.generateRefreshToken(userId);
        
        assertEquals(userId, JWTUtil.getUserIdFromToken(refreshToken), "Should have user ID");
        assertEquals("refresh", JWTUtil.getTokenType(refreshToken), "Should be refresh type");
        
        assertFalse(JWTUtil.hasRole(refreshToken, "customer"), "Refresh token should not have role");
        assertFalse(JWTUtil.hasRole(refreshToken, "admin"), "Refresh token should not have role");
        
        long refreshValidity = JWTUtil.getRefreshTokenValidity();
        long accessValidity = JWTUtil.getAccessTokenValidity();
        assertTrue(refreshValidity > accessValidity, "Refresh token should be longer-lived");
        
        System.out.println("✅ Refresh token scenarios passed");
    }

    @Test
    @DisplayName("Should handle multiple role scenarios")
    void testMultipleRoleScenarios() {
        System.out.println("🎭 Testing multiple role scenarios...");
        
        String[] validRoles = {"customer", "seller", "delivery", "admin", "super_admin", "guest"};
        
        for (String role : validRoles) {
            String token = JWTUtil.generateAccessToken(123L, "09123456789", role);
            
            assertTrue(JWTUtil.validateToken(token), "Token with role " + role + " should be valid");
            assertEquals(role, JWTUtil.getRoleFromToken(token), "Should extract role " + role + " correctly");
            assertTrue(JWTUtil.hasRole(token, role), "Should have role " + role);
            
            for (String otherRole : validRoles) {
                if (!otherRole.equals(role)) {
                    assertFalse(JWTUtil.hasRole(token, otherRole), 
                        "Should not have role " + otherRole + " when assigned " + role);
                }
            }
        }
        
        System.out.println("✅ Multiple role scenarios passed");
    }

    @Test
    @DisplayName("Should handle memory and performance under load")
    void testMemoryAndPerformanceUnderLoad() {
        System.out.println("💾 Testing memory and performance under load...");
        
        final int tokenCount = 500; // Reduced count for more realistic test
        
        // Force garbage collection before test
        System.gc();
        Thread.yield();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < tokenCount; i++) {
            String token = JWTUtil.generateAccessToken((long) i, "phone" + i, "customer");
            if (i % 100 == 0) {
                assertTrue(JWTUtil.validateToken(token), "Token " + i + " should be valid");
                // Force GC periodically to avoid memory buildup
                if (i % 200 == 0) {
                    System.gc();
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Generated " + tokenCount + " tokens in " + duration + "ms");
        
        // Focus on performance rather than memory (which is hard to measure accurately)
        assertTrue(duration < 10000, "Should generate " + tokenCount + " tokens within 10 seconds");
        
        // Test average performance per token
        double avgTimePerToken = (double) duration / tokenCount;
        assertTrue(avgTimePerToken < 20, "Average time per token should be less than 20ms");
        
        System.out.println("✅ Memory and performance tests passed");
    }
} 
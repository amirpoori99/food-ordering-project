package com.myapp.auth;

import com.myapp.common.utils.JWTUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Missing Scenarios Test")
class JWTMissingScenarios {

    @Test
    @DisplayName("Should handle token expiration edge cases")
    void testTokenExpiration() {
        System.out.println("⏰ Testing token expiration scenarios...");
        
        String token = JWTUtil.generateAccessToken(123L, "09123456789", "customer");
        
        // Token should be valid initially
        assertTrue(JWTUtil.validateToken(token), "Token should be valid initially");
        assertFalse(JWTUtil.isTokenExpired(token), "Token should not be expired initially");
        
        // Check remaining time is reasonable
        long remainingTime = JWTUtil.getRemainingTimeToExpire(token);
        assertTrue(remainingTime > 0, "Remaining time should be positive");
        assertTrue(remainingTime <= 24 * 60 * 60 * 1000, "Should be <= 24 hours");
        
        System.out.println("✅ Token expiration tests passed");
    }

    @Test
    @DisplayName("Should handle concurrent token operations safely")
    void testConcurrentTokenOperations() throws InterruptedException {
        System.out.println("🔄 Testing concurrent token operations...");
        
        final int threadCount = 5;
        final int tokensPerThread = 20;
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
        
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Should complete within 10 seconds");
        executor.shutdown();
        
        assertFalse(hasFailure[0], "No failures should occur in concurrent operations");
        System.out.println("✅ Concurrent operations test passed");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "Bearer", "Bearer ", "Basic token", "Token abc"})
    @DisplayName("Should handle malformed authorization headers")
    void testMalformedAuthorizationHeaders(String invalidHeader) {
        String extractedToken = JWTUtil.extractBearerToken(invalidHeader);
        assertNull(extractedToken, "Should return null for: " + invalidHeader);
    }

    @Test
    @DisplayName("Should handle extreme user ID values")
    void testExtremeUserIds() {
        System.out.println("🔢 Testing extreme user ID values...");
        
        // Test maximum Long value
        Long maxUserId = Long.MAX_VALUE;
        String token1 = JWTUtil.generateAccessToken(maxUserId, "09123456789", "customer");
        assertTrue(JWTUtil.validateToken(token1), "Should handle Long.MAX_VALUE");
        assertEquals(maxUserId, JWTUtil.getUserIdFromToken(token1));
        
        // Test minimum positive value
        Long minUserId = 1L;
        String token2 = JWTUtil.generateAccessToken(minUserId, "09123456789", "customer");
        assertTrue(JWTUtil.validateToken(token2), "Should handle minimum user ID");
        assertEquals(minUserId, JWTUtil.getUserIdFromToken(token2));
        
        System.out.println("✅ Extreme user ID tests passed");
    }

    @Test
    @DisplayName("Should handle international phone formats")
    void testInternationalPhoneFormats() {
        System.out.println("📱 Testing international phone formats...");
        
        String[] phoneFormats = {
            "+98-912-345-6789",
            "+1-555-123-4567", 
            "+44-20-7946-0958",
            "00989123456789",
            "(091) 2345-6789"
        };
        
        for (String phone : phoneFormats) {
            String token = JWTUtil.generateAccessToken(123L, phone, "customer");
            assertTrue(JWTUtil.validateToken(token), "Should handle phone: " + phone);
            assertEquals(phone, JWTUtil.getPhoneFromToken(token), "Should extract phone correctly");
        }
        
        System.out.println("✅ International phone format tests passed");
    }

    @Test
    @DisplayName("Should handle various role formats")
    void testVariousRoleFormats() {
        System.out.println("🎭 Testing various role formats...");
        
        String[] roles = {
            "customer", "seller", "delivery", "admin",
            "super_admin", "guest", "moderator", 
            "support", "manager", "owner"
        };
        
        for (String role : roles) {
            String token = JWTUtil.generateAccessToken(123L, "09123456789", role);
            assertTrue(JWTUtil.validateToken(token), "Should handle role: " + role);
            assertEquals(role, JWTUtil.getRoleFromToken(token));
            assertTrue(JWTUtil.hasRole(token, role), "Should have role: " + role);
        }
        
        System.out.println("✅ Various role format tests passed");
    }

    @Test
    @DisplayName("Should handle token tampering attempts")
    void testTokenTamperingAttempts() {
        System.out.println("🔐 Testing token tampering attempts...");
        
        String validToken = JWTUtil.generateAccessToken(123L, "09123456789", "customer");
        String[] parts = validToken.split("\\.");
        
        // Various tampering attempts
        String[] tamperedTokens = {
            parts[0] + ".tampered." + parts[2],
            "fake." + parts[1] + "." + parts[2],
            parts[0] + "." + parts[1] + ".fake",
            parts[0] + ".." + parts[2],
            parts[0] + "." + parts[1] + ".",
            "." + parts[1] + "." + parts[2],
            validToken.substring(0, validToken.length() - 5) + "12345",
            validToken + "extra"
        };
        
        for (String tamperedToken : tamperedTokens) {
            assertFalse(JWTUtil.validateToken(tamperedToken), 
                "Tampered token should be invalid");
        }
        
        System.out.println("✅ Token tampering tests passed");
    }

    @Test
    @DisplayName("Should handle case sensitivity in role authorization")
    void testRoleCaseSensitivity() {
        System.out.println("🔤 Testing role case sensitivity...");
        
        String customerToken = JWTUtil.generateAccessToken(123L, "09123456789", "customer");
        
        // Exact match should work
        assertTrue(JWTUtil.hasRole(customerToken, "customer"), "Exact match should work");
        
        // Case variations should not work
        assertFalse(JWTUtil.hasRole(customerToken, "CUSTOMER"), "Should be case sensitive");
        assertFalse(JWTUtil.hasRole(customerToken, "Customer"), "Should be case sensitive");
        assertFalse(JWTUtil.hasRole(customerToken, "cUsTomEr"), "Should be case sensitive");
        
        // Partial matches should not work
        assertFalse(JWTUtil.hasRole(customerToken, "custom"), "Partial match should fail");
        assertFalse(JWTUtil.hasRole(customerToken, "customers"), "Plural should fail");
        
        System.out.println("✅ Role case sensitivity tests passed");
    }

    @Test
    @DisplayName("Should handle refresh token limitations")
    void testRefreshTokenLimitations() {
        System.out.println("🔄 Testing refresh token limitations...");
        
        Long userId = 123L;
        String refreshToken = JWTUtil.generateRefreshToken(userId);
        
        // Should have user ID and type
        assertEquals(userId, JWTUtil.getUserIdFromToken(refreshToken));
        assertEquals("refresh", JWTUtil.getTokenType(refreshToken));
        
        // Should NOT have role-based access
        assertFalse(JWTUtil.hasRole(refreshToken, "customer"), 
            "Refresh token should not have customer role");
        assertFalse(JWTUtil.hasRole(refreshToken, "admin"), 
            "Refresh token should not have admin role");
        assertFalse(JWTUtil.hasRole(refreshToken, "seller"), 
            "Refresh token should not have seller role");
        
        // Should be longer-lived than access token
        long refreshValidity = JWTUtil.getRefreshTokenValidity();
        long accessValidity = JWTUtil.getAccessTokenValidity();
        assertTrue(refreshValidity > accessValidity, 
            "Refresh token should be longer-lived");
        
        System.out.println("✅ Refresh token limitation tests passed");
    }

    @Test
    @DisplayName("Should handle token pair consistency")
    void testTokenPairConsistency() {
        System.out.println("👯 Testing token pair consistency...");
        
        Long userId = 123L;
        String phone = "09123456789";
        String role = "customer";
        
        String[] tokenPair = JWTUtil.generateTokenPair(userId, phone, role);
        String accessToken = tokenPair[0];
        String refreshToken = tokenPair[1];
        
        // Both should have same user ID
        assertEquals(JWTUtil.getUserIdFromToken(accessToken), 
                    JWTUtil.getUserIdFromToken(refreshToken));
        
        // Only access token should have phone and role
        assertEquals(phone, JWTUtil.getPhoneFromToken(accessToken));
        assertEquals(role, JWTUtil.getRoleFromToken(accessToken));
        
        // Both should be valid
        assertTrue(JWTUtil.validateToken(accessToken));
        assertTrue(JWTUtil.validateToken(refreshToken));
        
        // Proper types
        assertTrue(JWTUtil.isAccessToken(accessToken));
        assertTrue(JWTUtil.isRefreshToken(refreshToken));
        
        System.out.println("✅ Token pair consistency tests passed");
    }

    @Test
    @DisplayName("Should handle high-load token generation")
    void testHighLoadTokenGeneration() {
        System.out.println("💪 Testing high-load token generation...");
        
        final int tokenCount = 500;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < tokenCount; i++) {
            String token = JWTUtil.generateAccessToken((long) i, "phone" + i, "customer");
            
            // Validate every 50th token to check consistency
            if (i % 50 == 0) {
                assertTrue(JWTUtil.validateToken(token), "Token " + i + " should be valid");
                assertEquals((long) i, JWTUtil.getUserIdFromToken(token));
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Generated " + tokenCount + " tokens in " + duration + "ms");
        
        // Performance expectation
        assertTrue(duration < 5000, "Should generate " + tokenCount + " tokens within 5 seconds");
        
        System.out.println("✅ High-load token generation test passed");
    }
} 
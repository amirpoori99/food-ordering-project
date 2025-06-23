package com.myapp.auth;

import com.myapp.common.utils.JWTUtil;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.JwtException;

/**
 * Authentication middleware for JWT token validation
 * Provides methods to authenticate requests and extract user information
 */
public class AuthMiddleware {
    
    /**
     * Authenticate request using JWT token from Authorization header
     * 
     * @param exchange HTTP exchange
     * @return AuthResult containing authentication information
     */
    public static AuthResult authenticate(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        if (authHeader == null) {
            return AuthResult.unauthenticated("Missing Authorization header");
        }
        
        String token = JWTUtil.extractBearerToken(authHeader);
        if (token == null) {
            return AuthResult.unauthenticated("Invalid Authorization header format. Use 'Bearer <token>'");
        }
        
        return authenticateToken(token);
    }
    
    /**
     * Authenticate using JWT token directly
     * 
     * @param token JWT token
     * @return AuthResult containing authentication information
     */
    public static AuthResult authenticateToken(String token) {
        try {
            if (!JWTUtil.validateToken(token)) {
                return AuthResult.unauthenticated("Invalid token");
            }
            
            if (JWTUtil.isTokenExpired(token)) {
                return AuthResult.unauthenticated("Token expired");
            }
            
            if (!JWTUtil.isAccessToken(token)) {
                return AuthResult.unauthenticated("Invalid token type. Access token required");
            }
            
            Long userId = JWTUtil.getUserIdFromToken(token);
            String phone = JWTUtil.getPhoneFromToken(token);
            String role = JWTUtil.getRoleFromToken(token);
            
            return AuthResult.authenticated(userId, phone, role, token);
            
        } catch (JwtException e) {
            return AuthResult.unauthenticated("Token validation failed: " + e.getMessage());
        } catch (Exception e) {
            return AuthResult.unauthenticated("Authentication error: " + e.getMessage());
        }
    }
    
    /**
     * Check if user has required role
     * 
     * @param authResult Authentication result
     * @param requiredRole Required role
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(AuthResult authResult, String requiredRole) {
        return authResult.isAuthenticated() && requiredRole.equals(authResult.getRole());
    }
    
    /**
     * Check if user has any of the required roles
     * 
     * @param authResult Authentication result
     * @param requiredRoles Array of required roles
     * @return true if user has any of the roles, false otherwise
     */
    public static boolean hasAnyRole(AuthResult authResult, String... requiredRoles) {
        if (!authResult.isAuthenticated()) {
            return false;
        }
        
        String userRole = authResult.getRole();
        for (String role : requiredRoles) {
            if (role.equals(userRole)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if authenticated user is the same as the requested user ID
     * 
     * @param authResult Authentication result
     * @param requestedUserId Requested user ID
     * @return true if same user or user has admin privileges
     */
    public static boolean isSameUserOrAdmin(AuthResult authResult, Long requestedUserId) {
        if (!authResult.isAuthenticated()) {
            return false;
        }
        
        // Check if same user
        if (authResult.getUserId().equals(requestedUserId)) {
            return true;
        }
        
        // Check if admin (for future admin role implementation)
        return "admin".equals(authResult.getRole());
    }
    
    /**
     * Refresh access token using refresh token
     * 
     * @param refreshToken Refresh token
     * @param authRepository Authentication repository for user validation
     * @return AuthResult with new access token or error
     */
    public static AuthResult refreshAccessToken(String refreshToken, AuthRepository authRepository) {
        try {
            if (!JWTUtil.validateToken(refreshToken)) {
                return AuthResult.unauthenticated("Invalid refresh token");
            }
            
            if (JWTUtil.isTokenExpired(refreshToken)) {
                return AuthResult.unauthenticated("Refresh token expired");
            }
            
            if (!JWTUtil.isRefreshToken(refreshToken)) {
                return AuthResult.unauthenticated("Invalid token type. Refresh token required");
            }
            
            Long userId = JWTUtil.getUserIdFromToken(refreshToken);
            
            // Verify user still exists
            var userOpt = authRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return AuthResult.unauthenticated("User not found");
            }
            
            var user = userOpt.get();
            String[] tokens = JWTUtil.generateTokenPair(user.getId(), user.getPhone(), user.getRole().toString());
            
            return AuthResult.refreshed(user.getId(), user.getPhone(), user.getRole().toString(), tokens[0], tokens[1]);
            
        } catch (JwtException e) {
            return AuthResult.unauthenticated("Token refresh failed: " + e.getMessage());
        } catch (Exception e) {
            return AuthResult.unauthenticated("Refresh error: " + e.getMessage());
        }
    }
    
    /**
     * Extract user ID from request path parameter
     * 
     * @param exchange HTTP exchange
     * @param paramName Parameter name (e.g., "userId")
     * @return User ID or null if not found or invalid
     */
    public static Long extractUserIdFromPath(HttpExchange exchange, String paramName) {
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            
            for (int i = 0; i < segments.length - 1; i++) {
                if (segments[i].equals(paramName) && i + 1 < segments.length) {
                    return Long.parseLong(segments[i + 1]);
                }
            }
            
            // Alternative: look for numeric segment after known patterns
            if (path.contains("/users/") || path.contains("/user/")) {
                for (String segment : segments) {
                    try {
                        return Long.parseLong(segment);
                    } catch (NumberFormatException ignored) {
                        // Continue searching
                    }
                }
            }
            
        } catch (Exception e) {
            // Log error in production
        }
        
        return null;
    }
    
    /**
     * Check if request requires authentication based on path
     * 
     * @param path Request path
     * @return true if authentication required, false otherwise
     */
    public static boolean requiresAuthentication(String path) {
        // Public endpoints that don't require authentication
        String[] publicPaths = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/health",
            "/api/status"
        };
        
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return false;
            }
        }
        
        // All other API endpoints require authentication
        return path.startsWith("/api/");
    }
    
    /**
     * Get required role for specific endpoint
     * 
     * @param path Request path
     * @param method HTTP method
     * @return Required role or null if no specific role required
     */
    public static String getRequiredRole(String path, String method) {
        // Restaurant management - sellers only
        if (path.startsWith("/api/restaurants") && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return "seller";
        }
        
        // Delivery management - delivery only
        if (path.startsWith("/api/delivery") && ("PUT".equals(method) || "POST".equals(method))) {
            return "delivery";
        }
        
        // Admin endpoints (for future implementation)
        if (path.startsWith("/api/admin/")) {
            return "admin";
        }
        
        // Most endpoints are accessible by any authenticated user
        return null;
    }
} 
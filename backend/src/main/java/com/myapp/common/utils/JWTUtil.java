package com.myapp.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT Utility class for token generation, validation, and parsing
 * Provides secure JWT authentication functionality
 */
public class JWTUtil {
    
    // Secret key for JWT signing (in production, should be from environment variable)
    private static final String SECRET_KEY = "mySecretKeyForJWTThatShouldBeVeryLongAndSecureInProductionEnvironment123456789";
    
    // Token expiration times
    private static final long ACCESS_TOKEN_VALIDITY = TimeUnit.HOURS.toMillis(24); // 24 hours
    private static final long REFRESH_TOKEN_VALIDITY = TimeUnit.DAYS.toMillis(7); // 7 days
    
    // JWT issuer
    private static final String ISSUER = "food-ordering-app";
    
    // Get signing key
    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    
    /**
     * Generate access token for authenticated user
     * 
     * @param userId User ID
     * @param phone User phone number
     * @param role User role (customer, seller, delivery)
     * @return JWT access token
     */
    public static String generateAccessToken(Long userId, String phone, String role) {
        if (userId == null || phone == null || role == null) {
            throw new IllegalArgumentException("UserId, phone, and role cannot be null");
        }
        
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("phone", phone)
                .claim("role", role)
                .claim("type", "access")
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Generate refresh token for user
     * 
     * @param userId User ID
     * @return JWT refresh token
     */
    public static String generateRefreshToken(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuer(ISSUER)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validate JWT token
     * 
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get user ID from JWT token
     * 
     * @param token JWT token
     * @return User ID
     * @throws JwtException if token is invalid
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.parseLong(claims.getSubject());
    }
    
    /**
     * Get user phone from JWT token
     * 
     * @param token JWT token
     * @return User phone number
     * @throws JwtException if token is invalid or doesn't contain phone
     */
    public static String getPhoneFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String phone = claims.get("phone", String.class);
        if (phone == null) {
            throw new JwtException("Token does not contain phone claim");
        }
        return phone;
    }
    
    /**
     * Get user role from JWT token
     * 
     * @param token JWT token
     * @return User role
     * @throws JwtException if token is invalid or doesn't contain role
     */
    public static String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String role = claims.get("role", String.class);
        if (role == null) {
            throw new JwtException("Token does not contain role claim");
        }
        return role;
    }
    
    /**
     * Get token type (access/refresh) from JWT token
     * 
     * @param token JWT token
     * @return Token type
     * @throws JwtException if token is invalid or doesn't contain type
     */
    public static String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        String type = claims.get("type", String.class);
        if (type == null) {
            throw new JwtException("Token does not contain type claim");
        }
        return type;
    }
    
    /**
     * Check if token is expired
     * 
     * @param token JWT token
     * @return true if token is expired, false otherwise
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true; // Consider invalid tokens as expired
        }
    }
    
    /**
     * Get expiration date from JWT token
     * 
     * @param token JWT token
     * @return Expiration date
     * @throws JwtException if token is invalid
     */
    public static Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * Get issued at date from JWT token
     * 
     * @param token JWT token
     * @return Issued at date
     * @throws JwtException if token is invalid
     */
    public static Date getIssuedAtFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getIssuedAt();
    }
    
    /**
     * Check if token is an access token
     * 
     * @param token JWT token
     * @return true if access token, false otherwise
     */
    public static boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * Check if token is a refresh token
     * 
     * @param token JWT token
     * @return true if refresh token, false otherwise
     */
    public static boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * Extract Bearer token from Authorization header
     * 
     * @param authorizationHeader Authorization header value
     * @return JWT token or null if invalid format
     */
    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
    
    /**
     * Get remaining time until token expires (in milliseconds)
     * 
     * @param token JWT token
     * @return Remaining time in milliseconds, or 0 if expired/invalid
     */
    public static long getRemainingTimeToExpire(String token) {
        if (token == null || token.trim().isEmpty()) {
            return 0;
        }
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (JwtException | IllegalArgumentException e) {
            return 0;
        }
    }
    
    /**
     * Check if user has specific role
     * 
     * @param token JWT token
     * @param requiredRole Required role
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(String token, String requiredRole) {
        try {
            String userRole = getRoleFromToken(token);
            return requiredRole.equals(userRole);
        } catch (JwtException e) {
            return false;
        }
    }
    
    /**
     * Get all claims from JWT token
     * 
     * @param token JWT token
     * @return Claims object
     * @throws JwtException if token is invalid
     */
    private static Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Generate both access and refresh tokens for user
     * 
     * @param userId User ID
     * @param phone User phone number
     * @param role User role
     * @return Array containing [accessToken, refreshToken]
     */
    public static String[] generateTokenPair(Long userId, String phone, String role) {
        String accessToken = generateAccessToken(userId, phone, role);
        String refreshToken = generateRefreshToken(userId);
        return new String[]{accessToken, refreshToken};
    }
    
    /**
     * Get access token validity period in milliseconds
     * 
     * @return Access token validity period
     */
    public static long getAccessTokenValidity() {
        return ACCESS_TOKEN_VALIDITY;
    }
    
    /**
     * Get refresh token validity period in milliseconds
     * 
     * @return Refresh token validity period
     */
    public static long getRefreshTokenValidity() {
        return REFRESH_TOKEN_VALIDITY;
    }
}

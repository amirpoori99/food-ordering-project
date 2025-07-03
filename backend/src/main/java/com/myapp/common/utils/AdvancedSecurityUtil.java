package com.myapp.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Advanced security utilities for threat detection, session management, and input validation
 */
public class AdvancedSecurityUtil {
    private static final Logger logger = Logger.getLogger(AdvancedSecurityUtil.class.getName());
    
    // Threat detection
    private static final ConcurrentHashMap<String, ThreatTracker> threatTrackers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> failedLoginAttempts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> lastFailedLoginTime = new ConcurrentHashMap<>();
    
    // Session management
    private static final ConcurrentHashMap<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes
    
    // Rate limiting for security
    private static final ConcurrentHashMap<String, RateLimiter> securityRateLimiters = new ConcurrentHashMap<>();
    
    // Known attack patterns
    private static final List<Pattern> SQL_INJECTION_PATTERNS = new ArrayList<>();
    private static final List<Pattern> XSS_PATTERNS = new ArrayList<>();
    private static final List<Pattern> PATH_TRAVERSAL_PATTERNS = new ArrayList<>();
    
    static {
        // SQL Injection patterns
        SQL_INJECTION_PATTERNS.add(Pattern.compile("(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION)"));
        SQL_INJECTION_PATTERNS.add(Pattern.compile("(?i)(OR|AND)\\s+\\d+\\s*=\\s*\\d+"));
        SQL_INJECTION_PATTERNS.add(Pattern.compile("(?i)('|;|--|/\\*|\\*/)"));
        
        // XSS patterns
        XSS_PATTERNS.add(Pattern.compile("(?i)<script[^>]*>.*?</script>"));
        XSS_PATTERNS.add(Pattern.compile("(?i)javascript:"));
        XSS_PATTERNS.add(Pattern.compile("(?i)on\\w+\\s*="));
        XSS_PATTERNS.add(Pattern.compile("(?i)<iframe[^>]*>"));
        
        // Path traversal patterns
        PATH_TRAVERSAL_PATTERNS.add(Pattern.compile("(?i)\\.\\./"));
        PATH_TRAVERSAL_PATTERNS.add(Pattern.compile("(?i)\\.\\\\"));
        PATH_TRAVERSAL_PATTERNS.add(Pattern.compile("(?i)%2e%2e%2f"));
    }
    
    /**
     * Threat tracking for IP addresses
     */
    public static class ThreatTracker {
        private final AtomicInteger threatScore = new AtomicInteger(0);
        private final AtomicLong lastThreatTime = new AtomicLong(0);
        private final AtomicInteger requestCount = new AtomicInteger(0);
        
        public void recordThreat(int score) {
            threatScore.addAndGet(score);
            lastThreatTime.set(System.currentTimeMillis());
        }
        
        public boolean isThreat() {
            return threatScore.get() > 50; // Threshold for threat detection
        }
        
        public void recordRequest() {
            requestCount.incrementAndGet();
        }
        
        public int getThreatScore() {
            return threatScore.get();
        }
        
        public void reset() {
            threatScore.set(0);
            lastThreatTime.set(0);
        }
    }
    
    /**
     * Session information
     */
    public static class SessionInfo {
        private final String sessionId;
        private final String userId;
        private final long creationTime;
        private final String userAgent;
        private final String ipAddress;
        
        public SessionInfo(String sessionId, String userId, String userAgent, String ipAddress) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.creationTime = System.currentTimeMillis();
            this.userAgent = userAgent;
            this.ipAddress = ipAddress;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - creationTime > SESSION_TIMEOUT_MS;
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        public long getCreationTime() { return creationTime; }
        public String getUserAgent() { return userAgent; }
        public String getIpAddress() { return ipAddress; }
    }
    
    /**
     * Rate limiter for security
     */
    public static class RateLimiter {
        private final int maxRequests;
        private final long windowMs;
        private final AtomicInteger currentRequests = new AtomicInteger(0);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        
        public RateLimiter(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }
        
        public boolean allowRequest() {
            long now = System.currentTimeMillis();
            long windowStartTime = windowStart.get();
            
            if (now - windowStartTime > windowMs) {
                currentRequests.set(0);
                windowStart.set(now);
            }
            
            return currentRequests.incrementAndGet() <= maxRequests;
        }
    }
    
    /**
     * Detect security threats in input
     */
    public static boolean detectThreats(String input, String ipAddress) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        ThreatTracker tracker = threatTrackers.computeIfAbsent(ipAddress, k -> new ThreatTracker());
        tracker.recordRequest();
        
        int threatScore = 0;
        
        // Check for SQL injection
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                threatScore += 30;
                logger.warning("SQL injection attempt detected from IP: " + ipAddress);
            }
        }
        
        // Check for XSS
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                threatScore += 25;
                logger.warning("XSS attempt detected from IP: " + ipAddress);
            }
        }
        
        // Check for path traversal
        for (Pattern pattern : PATH_TRAVERSAL_PATTERNS) {
            if (pattern.matcher(input).find()) {
                threatScore += 20;
                logger.warning("Path traversal attempt detected from IP: " + ipAddress);
            }
        }
        
        // Check for suspicious patterns
        if (input.length() > 1000) {
            threatScore += 10; // Very long input
        }
        
        if (input.contains("eval(") || input.contains("exec(")) {
            threatScore += 40; // Code execution attempt
        }
        
        if (threatScore > 0) {
            tracker.recordThreat(threatScore);
            return true;
        }
        
        return false;
    }
    
    /**
     * Check rate limiting for security
     */
    public static boolean checkRateLimit(String ipAddress) {
        RateLimiter rateLimiter = securityRateLimiters.computeIfAbsent(
            ipAddress,
            k -> new RateLimiter(100, 60000) // 100 requests per minute
        );
        
        return rateLimiter.allowRequest();
    }
    
    /**
     * Track failed login attempts
     */
    public static void recordFailedLogin(String phone, String ipAddress) {
        failedLoginAttempts.computeIfAbsent(phone, k -> new AtomicInteger(0))
                          .incrementAndGet();
        lastFailedLoginTime.put(phone, new AtomicLong(System.currentTimeMillis()));
        
        // Also track by IP
        failedLoginAttempts.computeIfAbsent(ipAddress, k -> new AtomicInteger(0))
                          .incrementAndGet();
        lastFailedLoginTime.put(ipAddress, new AtomicLong(System.currentTimeMillis()));
        
        logger.warning("Failed login attempt for phone: " + phone + " from IP: " + ipAddress);
    }
    
    /**
     * Check if account is locked due to too many failed attempts
     */
    public static boolean isAccountLocked(String phone, String ipAddress) {
        // Check by phone
        AtomicInteger phoneAttempts = failedLoginAttempts.get(phone);
        if (phoneAttempts != null && phoneAttempts.get() >= 5) {
            AtomicLong lastAttempt = lastFailedLoginTime.get(phone);
            if (lastAttempt != null && System.currentTimeMillis() - lastAttempt.get() < 300000) { // 5 minutes
                return true;
            }
        }
        
        // Check by IP
        AtomicInteger ipAttempts = failedLoginAttempts.get(ipAddress);
        if (ipAttempts != null && ipAttempts.get() >= 10) {
            AtomicLong lastAttempt = lastFailedLoginTime.get(ipAddress);
            if (lastAttempt != null && System.currentTimeMillis() - lastAttempt.get() < 600000) { // 10 minutes
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Reset failed login attempts
     */
    public static void resetFailedLoginAttempts(String phone, String ipAddress) {
        failedLoginAttempts.remove(phone);
        lastFailedLoginTime.remove(phone);
        failedLoginAttempts.remove(ipAddress);
        lastFailedLoginTime.remove(ipAddress);
    }
    
    /**
     * Create new session
     */
    public static String createSession(String userId, String userAgent, String ipAddress) {
        String sessionId = generateSessionId();
        SessionInfo sessionInfo = new SessionInfo(sessionId, userId, userAgent, ipAddress);
        activeSessions.put(sessionId, sessionInfo);
        
        logger.info("New session created for user: " + userId + " from IP: " + ipAddress);
        return sessionId;
    }
    
    /**
     * Validate session
     */
    public static boolean validateSession(String sessionId, String ipAddress) {
        SessionInfo sessionInfo = activeSessions.get(sessionId);
        
        if (sessionInfo == null) {
            return false;
        }
        
        if (sessionInfo.isExpired()) {
            activeSessions.remove(sessionId);
            return false;
        }
        
        // Check if IP address matches (basic security check)
        if (!sessionInfo.getIpAddress().equals(ipAddress)) {
            logger.warning("Session IP mismatch for session: " + sessionId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get user ID from session
     */
    public static String getUserIdFromSession(String sessionId) {
        SessionInfo sessionInfo = activeSessions.get(sessionId);
        return sessionInfo != null ? sessionInfo.getUserId() : null;
    }
    
    /**
     * Invalidate session
     */
    public static void invalidateSession(String sessionId) {
        SessionInfo sessionInfo = activeSessions.remove(sessionId);
        if (sessionInfo != null) {
            logger.info("Session invalidated for user: " + sessionInfo.getUserId());
        }
    }
    
    /**
     * Clean up expired sessions
     */
    public static void cleanupExpiredSessions() {
        activeSessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Sanitize input to prevent XSS
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.replaceAll("(?i)<script[^>]*>.*?</script>", "")
                   .replaceAll("(?i)javascript:", "")
                   .replaceAll("(?i)on\\w+\\s*=", "")
                   .replaceAll("(?i)<iframe[^>]*>", "")
                   .replaceAll("(?i)<object[^>]*>", "")
                   .replaceAll("(?i)<embed[^>]*>", "")
                   .replaceAll("(?i)<link[^>]*>", "")
                   .replaceAll("(?i)<meta[^>]*>", "");
    }
    
    /**
     * Generate secure session ID
     */
    private static String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Get security statistics
     */
    public static String getSecurityStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Security Statistics ===\n");
        
        // Threat statistics
        int totalThreats = 0;
        for (ThreatTracker tracker : threatTrackers.values()) {
            if (tracker.isThreat()) {
                totalThreats++;
            }
        }
        stats.append("Active threats: " + totalThreats + "\n");
        
        // Session statistics
        stats.append("Active sessions: " + activeSessions.size() + "\n");
        
        // Failed login statistics
        int totalFailedLogins = 0;
        for (AtomicInteger attempts : failedLoginAttempts.values()) {
            totalFailedLogins += attempts.get();
        }
        stats.append("Total failed logins: " + totalFailedLogins + "\n");
        
        return stats.toString();
    }
} 
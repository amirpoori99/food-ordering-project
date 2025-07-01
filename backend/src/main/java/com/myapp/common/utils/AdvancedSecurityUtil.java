package com.myapp.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.HashMap;

/**
 * کلاس ابزاری امنیت پیشرفته
 * این کلاس قابلیت‌های امنیتی پیشرفته‌تری را فراهم می‌کند:
 * - رمزنگاری و هش کردن
 * - ثبت وقایع امنیتی
 * - تشخیص تهدیدات پیشرفته
 * - مدیریت session های امن
 * - تولید کلیدهای امن
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class AdvancedSecurityUtil {
    
    // ===== Configuration =====
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SALT_ALGORITHM = "SHA1PRNG";
    private static final int SALT_LENGTH = 32;
    private static final int SESSION_ID_LENGTH = 64;
    private static final int MAX_SESSIONS_PER_USER = 5;
    private static final long SESSION_TIMEOUT_HOURS = 24;
    
    // ===== Threat Detection Patterns =====
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "\\b(union|select|insert|update|delete|drop|create|alter|exec|execute|script)\\b", 
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>|<[^>]*javascript:|on\\w+\\s*=", 
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "\\.\\./|\\.\\\\|%2e%2e%2f|%2e%2e%5c", 
        Pattern.CASE_INSENSITIVE
    );
    
    // ===== Storage =====
    private static final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> userSessionCounts = new ConcurrentHashMap<>();
    private static final List<SecurityEvent> securityEvents = new CopyOnWriteArrayList<>();
    private static final Map<String, ThreatInfo> threatTracker = new ConcurrentHashMap<>();
    
    // ===== Secure Random Generator =====
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * کلاس اطلاعات session
     */
    private static class SessionInfo {
        private final String sessionId;
        private final Long userId;
        private final String userAgent;
        private final String ipAddress;
        private final LocalDateTime createdAt;
        private volatile LocalDateTime lastAccessed;
        
        public SessionInfo(String sessionId, Long userId, String userAgent, String ipAddress) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.userAgent = userAgent;
            this.ipAddress = ipAddress;
            this.createdAt = LocalDateTime.now();
            this.lastAccessed = LocalDateTime.now();
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(lastAccessed.plusHours(SESSION_TIMEOUT_HOURS));
        }
        
        public void updateLastAccessed() {
            this.lastAccessed = LocalDateTime.now();
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public Long getUserId() { return userId; }
        public String getUserAgent() { return userAgent; }
        public String getIpAddress() { return ipAddress; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastAccessed() { return lastAccessed; }
    }
    
    /**
     * کلاس اطلاعات تهدید
     */
    private static class ThreatInfo {
        private final String identifier;
        private final String threatType;
        private final LocalDateTime firstDetected;
        private final AtomicLong occurrenceCount;
        private volatile LocalDateTime lastDetected;
        
        public ThreatInfo(String identifier, String threatType) {
            this.identifier = identifier;
            this.threatType = threatType;
            this.firstDetected = LocalDateTime.now();
            this.lastDetected = LocalDateTime.now();
            this.occurrenceCount = new AtomicLong(1);
        }
        
        public void incrementOccurrence() {
            occurrenceCount.incrementAndGet();
            lastDetected = LocalDateTime.now();
        }
        
        // Getters
        public String getIdentifier() { return identifier; }
        public String getThreatType() { return threatType; }
        public LocalDateTime getFirstDetected() { return firstDetected; }
        public LocalDateTime getLastDetected() { return lastDetected; }
        public long getOccurrenceCount() { return occurrenceCount.get(); }
    }
    
    /**
     * کلاس رویداد امنیتی
     */
    public static class SecurityEvent {
        private final String eventType;
        private final String description;
        private final String identifier;
        private final String severity;
        private final LocalDateTime timestamp;
        private final Map<String, Object> details;
        
        public SecurityEvent(String eventType, String description, String identifier, 
                           String severity, Map<String, Object> details) {
            this.eventType = eventType;
            this.description = description;
            this.identifier = identifier;
            this.severity = severity;
            this.timestamp = LocalDateTime.now();
            this.details = details != null ? new HashMap<>(details) : new HashMap<>();
        }
        
        // Getters
        public String getEventType() { return eventType; }
        public String getDescription() { return description; }
        public String getIdentifier() { return identifier; }
        public String getSeverity() { return severity; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getDetails() { return new HashMap<>(details); }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s (%s) - %s", 
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                severity, eventType, identifier, description);
        }
    }
    
    // ===== Hashing and Encryption Methods =====
    
    /**
     * تولید salt تصادفی
     * 
     * @return salt به صورت Base64
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * هش کردن متن با salt
     * 
     * @param text متن برای هش کردن
     * @param salt salt برای اضافه کردن
     * @return هش SHA-256
     */
    public static String hashWithSalt(String text, String salt) {
        if (text == null || salt == null) {
            throw new IllegalArgumentException("Text and salt cannot be null");
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            String combined = text + salt;
            byte[] hash = digest.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
    
    /**
     * تولید هش امن برای رمز عبور
     * 
     * @param password رمز عبور
     * @return Map شامل هش و salt
     */
    public static Map<String, String> hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        String salt = generateSalt();
        String hash = hashWithSalt(password, salt);
        
        Map<String, String> result = new HashMap<>();
        result.put("hash", hash);
        result.put("salt", salt);
        return result;
    }
    
    /**
     * بررسی رمز عبور
     * 
     * @param password رمز عبور
     * @param storedHash هش ذخیره شده
     * @param storedSalt salt ذخیره شده
     * @return true اگر رمز عبور صحیح باشد
     */
    public static boolean verifyPassword(String password, String storedHash, String storedSalt) {
        if (password == null || storedHash == null || storedSalt == null) {
            return false;
        }
        
        String computedHash = hashWithSalt(password, storedSalt);
        return storedHash.equals(computedHash);
    }
    
    // ===== Session Management Methods =====
    
    /**
     * ایجاد session جدید
     * 
     * @param userId شناسه کاربر
     * @param userAgent User-Agent
     * @param ipAddress آدرس IP
     * @return شناسه session یا null اگر محدودیت تجاوز شود
     */
    public static String createSession(Long userId, String userAgent, String ipAddress) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        // بررسی محدودیت session
        AtomicLong sessionCount = userSessionCounts.computeIfAbsent(userId.toString(), k -> new AtomicLong(0));
        if (sessionCount.get() >= MAX_SESSIONS_PER_USER) {
            logSecurityEvent("SESSION_LIMIT_EXCEEDED", 
                "User exceeded maximum session limit", 
                userId.toString(), "WARNING", 
                Map.of("userId", userId, "sessionCount", sessionCount.get()));
            return null;
        }
        
        // تولید session ID امن
        String sessionId = generateSecureSessionId();
        
        // ایجاد session
        SessionInfo sessionInfo = new SessionInfo(sessionId, userId, userAgent, ipAddress);
        activeSessions.put(sessionId, sessionInfo);
        sessionCount.incrementAndGet();
        
        logSecurityEvent("SESSION_CREATED", 
            "New session created", 
            userId.toString(), "INFO", 
            Map.of("userId", userId, "sessionId", sessionId, "ipAddress", ipAddress));
        
        return sessionId;
    }
    
    /**
     * اعتبارسنجی session
     * 
     * @param sessionId شناسه session
     * @return true اگر session معتبر باشد
     */
    public static boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        
        SessionInfo sessionInfo = activeSessions.get(sessionId);
        if (sessionInfo == null) {
            return false;
        }
        
        if (sessionInfo.isExpired()) {
            removeSession(sessionId);
            return false;
        }
        
        sessionInfo.updateLastAccessed();
        return true;
    }
    
    /**
     * حذف session
     * 
     * @param sessionId شناسه session
     */
    public static void removeSession(String sessionId) {
        if (sessionId == null) return;
        
        SessionInfo sessionInfo = activeSessions.remove(sessionId);
        if (sessionInfo != null) {
            String userId = sessionInfo.getUserId().toString();
            AtomicLong sessionCount = userSessionCounts.get(userId);
            if (sessionCount != null) {
                sessionCount.decrementAndGet();
            }
            
            logSecurityEvent("SESSION_REMOVED", 
                "Session removed", 
                userId, "INFO", 
                Map.of("userId", sessionInfo.getUserId(), "sessionId", sessionId));
        }
    }
    
    /**
     * پاک‌سازی session های منقضی شده
     */
    public static void cleanupExpiredSessions() {
        List<String> expiredSessions = new ArrayList<>();
        
        for (Map.Entry<String, SessionInfo> entry : activeSessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredSessions.add(entry.getKey());
            }
        }
        
        for (String sessionId : expiredSessions) {
            removeSession(sessionId);
        }
        
        if (!expiredSessions.isEmpty()) {
            logSecurityEvent("SESSIONS_CLEANUP", 
                "Cleaned up " + expiredSessions.size() + " expired sessions", 
                "SYSTEM", "INFO", 
                Map.of("expiredCount", expiredSessions.size()));
        }
    }
    
    // ===== Threat Detection Methods =====
    
    /**
     * تشخیص تهدیدات در ورودی
     * 
     * @param input ورودی برای بررسی
     * @param identifier شناسه منبع (IP، userId، etc.)
     * @return true اگر تهدید تشخیص داده شود
     */
    public static boolean detectThreats(String input, String identifier) {
        if (input == null || identifier == null) {
            return false;
        }
        
        boolean threatDetected = false;
        String threatType = null;
        
        // بررسی SQL Injection
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            threatDetected = true;
            threatType = "SQL_INJECTION";
        }
        
        // بررسی XSS
        if (XSS_PATTERN.matcher(input).find()) {
            threatDetected = true;
            threatType = "XSS";
        }
        
        // بررسی Path Traversal
        if (PATH_TRAVERSAL_PATTERN.matcher(input).find()) {
            threatDetected = true;
            threatType = "PATH_TRAVERSAL";
        }
        
        if (threatDetected) {
            recordThreat(identifier, threatType, input);
            logSecurityEvent("THREAT_DETECTED", 
                "Security threat detected: " + threatType, 
                identifier, "HIGH", 
                Map.of("threatType", threatType, "input", input));
        }
        
        return threatDetected;
    }
    
    /**
     * ثبت تهدید
     * 
     * @param identifier شناسه منبع
     * @param threatType نوع تهدید
     * @param details جزئیات تهدید
     */
    private static void recordThreat(String identifier, String threatType, String details) {
        String key = identifier + ":" + threatType;
        ThreatInfo threatInfo = threatTracker.computeIfAbsent(key, k -> new ThreatInfo(identifier, threatType));
        threatInfo.incrementOccurrence();
    }
    
    // ===== Security Event Logging =====
    
    /**
     * ثبت رویداد امنیتی
     * 
     * @param eventType نوع رویداد
     * @param description توضیحات
     * @param identifier شناسه
     * @param severity سطح اهمیت (INFO, WARNING, HIGH, CRITICAL)
     * @param details جزئیات اضافی
     */
    public static void logSecurityEvent(String eventType, String description, String identifier, 
                                      String severity, Map<String, Object> details) {
        SecurityEvent event = new SecurityEvent(eventType, description, identifier, severity, details);
        securityEvents.add(event);
        
        // چاپ در console برای debugging
        System.out.println("🔒 Security Event: " + event.toString());
        
        // اگر رویداد بحرانی است، اقدامات اضافی انجام دهید
        if ("CRITICAL".equals(severity)) {
            handleCriticalEvent(event);
        }
    }
    
    /**
     * مدیریت رویدادهای بحرانی
     * 
     * @param event رویداد بحرانی
     */
    private static void handleCriticalEvent(SecurityEvent event) {
        // در اینجا می‌توانید اقدامات اضافی مانند:
        // - ارسال هشدار به مدیر سیستم
        // - مسدود کردن IP
        // - ثبت در فایل لاگ جداگانه
        // - ارسال ایمیل هشدار
        
        System.err.println("🚨 CRITICAL SECURITY EVENT: " + event.getDescription());
    }
    
    /**
     * دریافت رویدادهای امنیتی
     * 
     * @param limit تعداد رویدادهای بازگشتی
     * @return لیست رویدادها
     */
    public static List<SecurityEvent> getSecurityEvents(int limit) {
        int size = securityEvents.size();
        int startIndex = Math.max(0, size - limit);
        return new ArrayList<>(securityEvents.subList(startIndex, size));
    }
    
    /**
     * دریافت آمار تهدیدات
     * 
     * @return Map شامل آمار تهدیدات
     */
    public static Map<String, Object> getThreatStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalThreats", threatTracker.size());
        stats.put("activeSessions", activeSessions.size());
        stats.put("securityEvents", securityEvents.size());
        
        // آمار تهدیدات بر اساس نوع
        Map<String, Long> threatTypeCounts = new HashMap<>();
        for (ThreatInfo threat : threatTracker.values()) {
            threatTypeCounts.merge(threat.getThreatType(), 1L, Long::sum);
        }
        stats.put("threatTypeCounts", threatTypeCounts);
        
        return stats;
    }
    
    // ===== Utility Methods =====
    
    /**
     * تولید شناسه session امن
     * 
     * @return شناسه session
     */
    private static String generateSecureSessionId() {
        byte[] randomBytes = new byte[SESSION_ID_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * تولید کلید تصادفی امن
     * 
     * @param length طول کلید
     * @return کلید تصادفی
     */
    public static String generateSecureKey(int length) {
        if (length <= 0) length = 32;
        
        byte[] keyBytes = new byte[length];
        secureRandom.nextBytes(keyBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
    }
    
    /**
     * بررسی امنیت فایل
     * 
     * @param filename نام فایل
     * @return true اگر فایل امن باشد
     */
    public static boolean isSecureFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // بررسی پسوندهای خطرناک
        String lowerFilename = filename.toLowerCase();
        String[] dangerousExtensions = {".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js"};
        
        for (String ext : dangerousExtensions) {
            if (lowerFilename.endsWith(ext)) {
                return false;
            }
        }
        
        // بررسی کاراکترهای خطرناک
        return !filename.contains("..") && 
               !filename.contains("\\") && 
               !filename.contains("/") &&
               !filename.contains(":") &&
               !filename.contains("*") &&
               !filename.contains("?") &&
               !filename.contains("\"") &&
               !filename.contains("<") &&
               !filename.contains(">") &&
               !filename.contains("|");
    }
    
    /**
     * پاک‌سازی داده‌های قدیمی
     */
    public static void cleanupOldData() {
        // پاک‌سازی session های منقضی شده
        cleanupExpiredSessions();
        
        // پاک‌سازی رویدادهای قدیمی (بیش از 30 روز)
        LocalDateTime cutoffEvents = LocalDateTime.now().minusDays(30);
        securityEvents.removeIf(event -> event.getTimestamp().isBefore(cutoffEvents));
        
        // پاک‌سازی تهدیدات قدیمی (بیش از 7 روز)
        LocalDateTime cutoffThreats = LocalDateTime.now().minusDays(7);
        threatTracker.entrySet().removeIf(entry -> 
            entry.getValue().getLastDetected().isBefore(cutoffThreats));
    }
} 

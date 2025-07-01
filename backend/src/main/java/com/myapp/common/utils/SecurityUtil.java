package com.myapp.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

/**
 * کلاس ابزاری امنیت پیشرفته
 * این کلاس قابلیت‌های امنیتی پیشرفته را فراهم می‌کند:
 * - Rate Limiting برای جلوگیری از حملات
 * - اعتبارسنجی ورودی و پاک‌سازی
 * - اعتبارسنجی رمز عبور
 * - مدیریت IP های مشکوک
 * - تولید header های امنیتی
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class SecurityUtil {
    
    // ===== Rate Limiting Configuration =====
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_BLOCK_DURATION_MINUTES = 15;
    
    // ===== Password Validation =====
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{" + MIN_PASSWORD_LENGTH + "," + MAX_PASSWORD_LENGTH + "}$"
    );
    
    // ===== Input Validation Patterns =====
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^(?!.*\\.\\.)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\u0600-\\u06FF\\sA-Za-z]{2,50}$");
    
    // ===== Rate Limiting Storage =====
    private static final Map<String, RequestCounter> rateLimitMap = new ConcurrentHashMap<>();
    private static final Map<String, LoginAttempt> loginAttemptsMap = new ConcurrentHashMap<>();
    private static final Set<String> blockedIPs = ConcurrentHashMap.newKeySet();
    
    // ===== Security Headers =====
    private static final Map<String, String> SECURITY_HEADERS = Map.of(
        "X-Content-Type-Options", "nosniff",
        "X-Frame-Options", "DENY",
        "X-XSS-Protection", "1; mode=block",
        "Strict-Transport-Security", "max-age=31536000; includeSubDomains",
        "Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'",
        "Referrer-Policy", "strict-origin-when-cross-origin"
    );
    
    /**
     * کلاس شمارنده درخواست‌ها برای Rate Limiting
     */
    private static class RequestCounter {
        private final AtomicInteger minuteCount = new AtomicInteger(0);
        private final AtomicInteger hourCount = new AtomicInteger(0);
        private volatile long lastMinuteReset = System.currentTimeMillis();
        private volatile long lastHourReset = System.currentTimeMillis();
        
        public boolean canMakeRequest() {
            long now = System.currentTimeMillis();
            
            // Reset minute counter if needed
            if (now - lastMinuteReset > TimeUnit.MINUTES.toMillis(1)) {
                minuteCount.set(0);
                lastMinuteReset = now;
            }
            
            // Reset hour counter if needed
            if (now - lastHourReset > TimeUnit.HOURS.toMillis(1)) {
                hourCount.set(0);
                lastHourReset = now;
            }
            
            // Check limits
            return minuteCount.get() < MAX_REQUESTS_PER_MINUTE && 
                   hourCount.get() < MAX_REQUESTS_PER_HOUR;
        }
        
        public void increment() {
            minuteCount.incrementAndGet();
            hourCount.incrementAndGet();
        }
        
        public int getMinuteCount() {
            return minuteCount.get();
        }
        
        public int getHourCount() {
            return hourCount.get();
        }
    }
    
    /**
     * کلاس مدیریت تلاش‌های ورود
     */
    private static class LoginAttempt {
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        private volatile long firstAttemptTime = System.currentTimeMillis();
        private volatile long lastAttemptTime = System.currentTimeMillis();
        
        public boolean isBlocked() {
            long now = System.currentTimeMillis();
            long timeSinceFirstAttempt = now - firstAttemptTime;
            
            // Reset if block duration has passed
            if (timeSinceFirstAttempt > TimeUnit.MINUTES.toMillis(LOGIN_BLOCK_DURATION_MINUTES)) {
                attemptCount.set(0);
                firstAttemptTime = now;
                return false;
            }
            
            return attemptCount.get() >= MAX_LOGIN_ATTEMPTS;
        }
        
        public void recordAttempt() {
            attemptCount.incrementAndGet();
            lastAttemptTime = System.currentTimeMillis();
        }
        
        public int getAttemptCount() {
            return attemptCount.get();
        }
        
        public long getRemainingBlockTime() {
            if (!isBlocked()) return 0;
            long now = System.currentTimeMillis();
            long timeSinceFirstAttempt = now - firstAttemptTime;
            return Math.max(0, TimeUnit.MINUTES.toMillis(LOGIN_BLOCK_DURATION_MINUTES) - timeSinceFirstAttempt);
        }
    }
    
    // ===== Rate Limiting Methods =====
    
    /**
     * بررسی Rate Limiting برای IP یا شناسه کاربر
     * 
     * @param identifier شناسه (IP یا userId)
     * @return true اگر درخواست مجاز باشد، در غیر اینصورت false
     */
    public static boolean checkRateLimit(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        // Check if IP is blocked
        if (blockedIPs.contains(identifier)) {
            return false;
        }
        
        RequestCounter counter = rateLimitMap.computeIfAbsent(identifier, k -> new RequestCounter());
        
        if (counter.canMakeRequest()) {
            counter.increment();
            return true;
        }
        
        return false;
    }
    
    /**
     * ثبت تلاش ورود ناموفق
     * 
     * @param identifier شناسه (IP یا phone)
     * @return true اگر IP مسدود شده باشد، در غیر اینصورت false
     */
    public static boolean recordFailedLogin(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        LoginAttempt attempt = loginAttemptsMap.computeIfAbsent(identifier, k -> new LoginAttempt());
        attempt.recordAttempt();
        
        if (attempt.isBlocked()) {
            blockedIPs.add(identifier);
            return true;
        }
        
        return false;
    }
    
    /**
     * پاک‌سازی تلاش‌های ورود موفق
     * 
     * @param identifier شناسه (IP یا phone)
     */
    public static void clearLoginAttempts(String identifier) {
        if (identifier != null) {
            loginAttemptsMap.remove(identifier);
            blockedIPs.remove(identifier);
        }
    }
    
    /**
     * دریافت تعداد تلاش‌های ورود باقیمانده
     * 
     * @param identifier شناسه (IP یا phone)
     * @return تعداد تلاش‌های باقیمانده
     */
    public static int getRemainingLoginAttempts(String identifier) {
        if (identifier == null) return 0;
        
        LoginAttempt attempt = loginAttemptsMap.get(identifier);
        if (attempt == null) return MAX_LOGIN_ATTEMPTS;
        
        if (attempt.isBlocked()) return 0;
        return Math.max(0, MAX_LOGIN_ATTEMPTS - attempt.getAttemptCount());
    }
    
    // ===== Input Validation Methods =====
    
    /**
     * اعتبارسنجی شماره تلفن
     * 
     * @param phone شماره تلفن
     * @return true اگر شماره معتبر باشد، در غیر اینصورت false
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * اعتبارسنجی ایمیل
     * 
     * @param email آدرس ایمیل
     * @return true اگر ایمیل معتبر باشد، در غیر اینصورت false
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }
    
    /**
     * اعتبارسنجی نام
     * 
     * @param name نام
     * @return true اگر نام معتبر باشد، در غیر اینصورت false
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * اعتبارسنجی رمز عبور
     * 
     * @param password رمز عبور
     * @return true اگر رمز عبور معتبر باشد، در غیر اینصورت false
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || 
            password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * دریافت پیام خطای اعتبارسنجی رمز عبور
     * 
     * @param password رمز عبور
     * @return پیام خطا یا null اگر رمز عبور معتبر باشد
     */
    public static String getPasswordValidationError(String password) {
        if (password == null) {
            return "رمز عبور نمی‌تواند خالی باشد";
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "رمز عبور باید حداقل " + MIN_PASSWORD_LENGTH + " کاراکتر باشد";
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "رمز عبور نمی‌تواند بیشتر از " + MAX_PASSWORD_LENGTH + " کاراکتر باشد";
        }
        
        if (!password.matches(".*[a-z].*")) {
            return "رمز عبور باید شامل حداقل یک حرف کوچک باشد";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "رمز عبور باید شامل حداقل یک حرف بزرگ باشد";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "رمز عبور باید شامل حداقل یک عدد باشد";
        }
        
        if (!password.matches(".*[@$!%*?&].*")) {
            return "رمز عبور باید شامل حداقل یک کاراکتر خاص (@$!%*?&) باشد";
        }
        
        return null; // رمز عبور معتبر است
    }
    
    // ===== Input Sanitization Methods =====
    
    /**
     * پاک‌سازی ورودی از کاراکترهای خطرناک
     * 
     * @param input ورودی
     * @return ورودی پاک‌سازی شده
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        return input.trim()
                   .replaceAll("<script[^>]*>.*?</script>", "") // Remove script tags
                   .replaceAll("<[^>]*>", "") // Remove HTML tags
                   .replaceAll("javascript:", "") // Remove javascript: protocol
                   .replaceAll("on\\w+\\s*=", "") // Remove event handlers
                   .replaceAll("(?i)\\b(union|select|insert|update|delete|drop|create|alter)\\b", ""); // Remove SQL keywords
    }
    
    /**
     * پاک‌سازی نام فایل
     * 
     * @param filename نام فایل
     * @return نام فایل پاک‌سازی شده
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) return null;
        
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                      .replaceAll("_{2,}", "_")
                      .replaceAll("^_|_$", "");
    }
    
    // ===== Security Headers Methods =====
    
    /**
     * دریافت header های امنیتی
     * 
     * @return Map شامل header های امنیتی
     */
    public static Map<String, String> getSecurityHeaders() {
        return new HashMap<>(SECURITY_HEADERS);
    }
    
    /**
     * دریافت header امنیتی خاص
     * 
     * @param headerName نام header
     * @return مقدار header یا null اگر وجود نداشته باشد
     */
    public static String getSecurityHeader(String headerName) {
        return SECURITY_HEADERS.get(headerName);
    }
    
    // ===== Utility Methods =====
    
    /**
     * تولید شناسه تصادفی امن
     * 
     * @param length طول شناسه
     * @return شناسه تصادفی
     */
    public static String generateSecureRandomId(int length) {
        if (length <= 0) length = 32;
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * بررسی امنیت URL
     * 
     * @param url URL برای بررسی
     * @return true اگر URL امن باشد، در غیر اینصورت false
     */
    public static boolean isSecureUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        String lowerUrl = url.toLowerCase();
        return lowerUrl.startsWith("https://") || 
               lowerUrl.startsWith("http://localhost") ||
               lowerUrl.startsWith("http://127.0.0.1");
    }
    
    /**
     * پاک‌سازی داده‌های Rate Limiting قدیمی
     * این متد باید به صورت دوره‌ای فراخوانی شود
     */
    public static void cleanupOldData() {
        long now = System.currentTimeMillis();
        
        // Cleanup rate limit data older than 1 hour
        rateLimitMap.entrySet().removeIf(entry -> {
            RequestCounter counter = entry.getValue();
            return now - counter.lastMinuteReset > TimeUnit.HOURS.toMillis(1);
        });
        
        // Cleanup login attempts older than 1 hour
        loginAttemptsMap.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            return now - attempt.lastAttemptTime > TimeUnit.HOURS.toMillis(1);
        });
        
        // Cleanup blocked IPs older than 1 hour
        blockedIPs.removeIf(ip -> {
            LoginAttempt attempt = loginAttemptsMap.get(ip);
            return attempt == null || now - attempt.lastAttemptTime > TimeUnit.HOURS.toMillis(1);
        });
    }
    
    /**
     * دریافت آمار Rate Limiting
     * 
     * @return Map شامل آمار
     */
    public static Map<String, Object> getRateLimitStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeRateLimits", rateLimitMap.size());
        stats.put("activeLoginAttempts", loginAttemptsMap.size());
        stats.put("blockedIPs", blockedIPs.size());
        stats.put("maxRequestsPerMinute", MAX_REQUESTS_PER_MINUTE);
        stats.put("maxRequestsPerHour", MAX_REQUESTS_PER_HOUR);
        stats.put("maxLoginAttempts", MAX_LOGIN_ATTEMPTS);
        return stats;
    }
} 

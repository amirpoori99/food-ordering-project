package com.myapp.ui.common;

import java.util.concurrent.atomic.AtomicReference;

/**
 * مدیریت جلسه کاربر در frontend
 * این کلاس مسئول نگهداری اطلاعات جلسه فعلی کاربر است
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class SessionManager {
    
    /** کاربر فعلی */
    private static final AtomicReference<User> currentUser = new AtomicReference<>();
    
    /** زمان timeout جلسه (به ثانیه) */
    private static int sessionTimeoutSeconds = 3600; // 1 ساعت پیش‌فرض
    
    /** زمان آخرین فعالیت کاربر */
    private static long lastActivityTime = System.currentTimeMillis();
    
    /**
     * کلاس User برای نگهداری اطلاعات کاربر
     */
    public static class User {
        private final String id;
        private final String email;
        private final String fullName;
        private final String phone;
        private final String role;
        
        public User(String id, String email, String fullName, String phone, String role) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.phone = phone;
            this.role = role;
        }
        
        public String getId() { return id; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
    }
    
    /**
     * تنظیم کاربر فعلی
     * 
     * @param user کاربر برای تنظیم
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
        lastActivityTime = System.currentTimeMillis();
    }
    
    /**
     * دریافت کاربر فعلی
     * 
     * @return کاربر فعلی یا null اگر جلسه منقضی شده باشد
     */
    public static User getCurrentUser() {
        if (isSessionExpired()) {
            clearSession();
            return null;
        }
        lastActivityTime = System.currentTimeMillis();
        return currentUser.get();
    }
    
    /**
     * پاک کردن جلسه فعلی
     */
    public static void clearSession() {
        currentUser.set(null);
        HttpClientUtil.clearTokens();
    }
    
    /**
     * بررسی انقضای جلسه
     * 
     * @return true اگر جلسه منقضی شده باشد
     */
    public static boolean isSessionExpired() {
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - lastActivityTime) / 1000;
        return elapsedSeconds > sessionTimeoutSeconds;
    }
    
    /**
     * تنظیم timeout جلسه (مخصوص تست)
     * 
     * @param timeoutSeconds زمان timeout به ثانیه
     */
    public static void setSessionTimeout(int timeoutSeconds) {
        sessionTimeoutSeconds = timeoutSeconds;
    }
    
    /**
     * دریافت token احراز هویت فعلی
     * 
     * @return token یا null
     */
    public static String getAuthToken() {
        return HttpClientUtil.getAccessToken();
    }
    
    /**
     * تنظیم token احراز هویت
     * 
     * @param token مقدار token
     */
    public static void setAuthToken(String token) {
        HttpClientUtil.setAuthToken(token);
    }
    
    /**
     * بررسی احراز هویت کاربر
     * 
     * @return true اگر کاربر احراز هویت شده باشد
     */
    public static boolean isAuthenticated() {
        return getCurrentUser() != null && HttpClientUtil.isAuthenticated();
    }
} 
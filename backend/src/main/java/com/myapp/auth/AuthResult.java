package com.myapp.auth;

/**
 * کلاس نتیجه احراز هویت - حاوی نتیجه تلاش برای احراز هویت
 * در صورت موفقیت شامل اطلاعات کاربر، در صورت شکست شامل پیام خطا
 * این کلاس الگوی Result Pattern را پیاده‌سازی می‌کند
 */
public class AuthResult {
    private final boolean authenticated;    // آیا احراز هویت موفق بوده؟
    private final String errorMessage;      // پیام خطا در صورت عدم موفقیت
    private final Long userId;              // شناسه کاربر (در صورت موفقیت)
    private final String phone;             // شماره تلفن کاربر (در صورت موفقیت)
    private final String role;              // نقش کاربر (در صورت موفقیت)
    private final String accessToken;       // Access Token برای دسترسی به API
    private final String refreshToken;      // Refresh Token برای تجدید Access Token
    private final boolean isRefresh;        // آیا این نتیجه از عملیات refresh token است؟
    
    /**
     * سازنده خصوصی - فقط از طریق متدهای static قابل دسترسی
     * این الگو Type Safety و کنترل بهتری فراهم می‌کند
     */
    private AuthResult(boolean authenticated, String errorMessage, Long userId, String phone, String role, 
                      String accessToken, String refreshToken, boolean isRefresh) {
        this.authenticated = authenticated;   // تنظیم وضعیت احراز هویت
        this.errorMessage = errorMessage;     // تنظیم پیام خطا
        this.userId = userId;                 // تنظیم شناسه کاربر
        this.phone = phone;                   // تنظیم شماره تلفن
        this.role = role;                     // تنظیم نقش کاربر
        this.accessToken = accessToken;       // تنظیم Access Token
        this.refreshToken = refreshToken;     // تنظیم Refresh Token
        this.isRefresh = isRefresh;           // تنظیم نوع عملیات
    }
    
    /**
     * ایجاد نتیجه احراز هویت موفق
     * 
     * @param userId شناسه کاربر
     * @param phone شماره تلفن کاربر
     * @param role نقش کاربر
     * @param accessToken Access Token
     * @return نتیجه احراز هویت موفق
     */
    public static AuthResult authenticated(Long userId, String phone, String role, String accessToken) {
        return new AuthResult(true, null, userId, phone, role, accessToken, null, false);
    }
    
    /**
     * ایجاد نتیجه احراز هویت ناموفق
     * 
     * @param errorMessage پیام خطا
     * @return نتیجه احراز هویت ناموفق
     */
    public static AuthResult unauthenticated(String errorMessage) {
        return new AuthResult(false, errorMessage, null, null, null, null, null, false);
    }
    
    /**
     * ایجاد نتیجه تجدید token موفق
     * 
     * @param userId شناسه کاربر
     * @param phone شماره تلفن کاربر
     * @param role نقش کاربر
     * @param newAccessToken Access Token جدید
     * @param newRefreshToken Refresh Token جدید
     * @return نتیجه تجدید token موفق
     */
    public static AuthResult refreshed(Long userId, String phone, String role, String newAccessToken, String newRefreshToken) {
        return new AuthResult(true, null, userId, phone, role, newAccessToken, newRefreshToken, true);
    }
    
    /**
     * بررسی موفقیت احراز هویت
     * 
     * @return true اگر احراز هویت موفق باشد، در غیر اینصورت false
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    /**
     * دریافت پیام خطا در صورت شکست احراز هویت
     * 
     * @return پیام خطا یا null در صورت موفقیت
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * دریافت شناسه کاربر احراز هویت شده
     * 
     * @return شناسه کاربر یا null در صورت عدم احراز هویت
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * دریافت شماره تلفن کاربر احراز هویت شده
     * 
     * @return شماره تلفن کاربر یا null در صورت عدم احراز هویت
     */
    public String getPhone() {
        return phone;
    }
    
    /**
     * دریافت نقش کاربر احراز هویت شده
     * 
     * @return نقش کاربر یا null در صورت عدم احراز هویت
     */
    public String getRole() {
        return role;
    }
    
    /**
     * دریافت Access Token
     * 
     * @return Access Token یا null در صورت عدم احراز هویت
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * دریافت Refresh Token (فقط بعد از عملیات refresh در دسترس است)
     * 
     * @return Refresh Token یا null
     */
    public String getRefreshToken() {
        return refreshToken;
    }
    
    /**
     * بررسی اینکه آیا این نتیجه از عملیات تجدید token است
     * 
     * @return true اگر از refresh باشد، در غیر اینصورت false
     */
    public boolean isRefresh() {
        return isRefresh;
    }
    
    /**
     * بررسی داشتن نقش خاص توسط کاربر
     * 
     * @param requiredRole نقش مورد نیاز
     * @return true اگر کاربر این نقش را داشته باشد، در غیر اینصورت false
     */
    public boolean hasRole(String requiredRole) {
        return authenticated && requiredRole.equals(role);
    }
    
    /**
     * بررسی داشتن هر یک از نقش‌های مشخص شده توسط کاربر
     * 
     * @param roles نقش‌هایی که باید بررسی شوند
     * @return true اگر کاربر حداقل یکی از نقش‌ها را داشته باشد، در غیر اینصورت false
     */
    public boolean hasAnyRole(String... roles) {
        if (!authenticated) {
            return false;
        }
        
        // بررسی هر نقش
        for (String requiredRole : roles) {
            if (requiredRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * بررسی اینکه آیا کاربر مشتری است
     * پشتیبانی از هر دو نام: "BUYER" و "customer"
     * 
     * @return true اگر کاربر مشتری باشد، در غیر اینصورت false
     */
    public boolean isCustomer() {
        return hasRole("BUYER") || hasRole("customer");
    }
    
    /**
     * بررسی اینکه آیا کاربر فروشنده است
     * پشتیبانی از هر دو نام: "SELLER" و "seller"
     * 
     * @return true اگر کاربر فروشنده باشد، در غیر اینصورت false
     */
    public boolean isSeller() {
        return hasRole("SELLER") || hasRole("seller");
    }
    
    /**
     * بررسی اینکه آیا کاربر پیک است
     * پشتیبانی از نام‌های مختلف: "COURIER", "delivery", "courier"
     * 
     * @return true اگر کاربر پیک باشد، در غیر اینصورت false
     */
    public boolean isDelivery() {
        return hasRole("COURIER") || hasRole("delivery") || hasRole("courier");
    }
    
    /**
     * بررسی اینکه آیا کاربر مدیر است
     * پشتیبانی از هر دو نام: "ADMIN" و "admin"
     * 
     * @return true اگر کاربر مدیر باشد، در غیر اینصورت false
     */
    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("admin");
    }
    
    /**
     * نمایش رشته‌ای از نتیجه احراز هویت
     * برای debugging و logging مفید است
     * 
     * @return نمایش رشته‌ای از AuthResult
     */
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
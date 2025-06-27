package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.ProfileResponse;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.models.User;

/**
 * کنترلر احراز هویت - لایه ارائه‌دهنده برای عملیات احراز هویت
 * این کلاس facade رقیقی بر روی AuthService است
 * در مراحل بعدی با annotation های web framework مانند @RestController، @PostMapping تنظیم خواهد شد
 * فعلاً بدون وابستگی به framework نگهداری می‌شود تا تست‌های واحد روی رفتار متمرکز باشند نه HTTP
 * 
 * این کنترلر مسئولیت‌های زیر را دارد:
 * - دریافت درخواست‌های احراز هویت از کلاینت
 * - انتقال درخواست‌ها به لایه Service
 * - بازگرداندن پاسخ‌های مناسب به کلاینت
 * - مدیریت تبدیل DTO ها
 * 
 * از الگوی Controller Pattern استفاده می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class AuthController {

    /** نمونه سرویس احراز هویت برای delegation */
    private final AuthService service;

    /**
     * سازنده کنترلر احراز هویت
     * تزریق وابستگی به AuthService
     * 
     * @param service سرویس احراز هویت
     */
    public AuthController(AuthService service) {
        this.service = service;
    }

    /**
     * ثبت نام کاربر جدید
     * 
     * @param req درخواست ثبت نام حاوی اطلاعات کاربر
     * @return کاربر ثبت شده همراه با ID تولید شده
     * @throws DuplicatePhoneException در صورت تکراری بودن شماره تلفن
     */
    public User register(RegisterRequest req) {
        return service.register(req);
    }

    /**
     * ورود کاربر با شماره تلفن و رمز عبور
     * 
     * @param phone شماره تلفن کاربر
     * @param passwordHash رمز عبور hash شده
     * @return کاربر وارد شده
     * @throws InvalidCredentialsException در صورت نامعتبر بودن اعتبارات
     */
    public User login(String phone, String passwordHash) {
        return service.login(phone, passwordHash);
    }
    
    /**
     * ورود کاربر همراه با تولید JWT token
     * 
     * @param phone شماره تلفن کاربر
     * @param passwordHash رمز عبور hash شده
     * @return AuthResult حاوی JWT tokens (Access + Refresh)
     */
    public AuthResult loginWithTokens(String phone, String passwordHash) {
        return service.loginWithTokens(phone, passwordHash);
    }
    
    /**
     * تجدید Access Token با استفاده از Refresh Token
     * 
     * @param refreshToken Refresh Token برای تجدید
     * @return AuthResult حاوی Access Token جدید
     */
    public AuthResult refreshToken(String refreshToken) {
        return service.refreshToken(refreshToken);
    }
    
    /**
     * اعتبارسنجی Access Token
     * 
     * @param accessToken Access Token برای اعتبارسنجی
     * @return AuthResult حاوی اطلاعات کاربر در صورت معتبر بودن
     */
    public AuthResult validateToken(String accessToken) {
        return service.validateToken(accessToken);
    }
    
    /**
     * خروج کاربر از سیستم
     * در سیستم JWT stateless، خروج معمولاً در سمت کلاینت مدیریت می‌شود
     * 
     * @param userId شناسه کاربر برای خروج
     * @return پیام موفقیت خروج
     */
    public String logout(Long userId) {
        return service.logout(userId);
    }

    /**
     * دریافت پروفایل کاربر بر اساس شناسه
     * تبدیل User entity به ProfileResponse DTO
     * 
     * @param id شناسه کاربر
     * @return ProfileResponse حاوی اطلاعات عمومی کاربر (بدون رمز عبور)
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public ProfileResponse getProfile(long id) {
        return ProfileResponse.from(service.getProfile(id));
    }

    /**
     * به‌روزرسانی پروفایل کاربر
     * امکان به‌روزرسانی جزئی (partial update) فراهم می‌کند
     * 
     * @param id شناسه کاربر
     * @param req درخواست به‌روزرسانی حاوی فیلدهای قابل تغییر
     * @return ProfileResponse حاوی اطلاعات به‌روز شده کاربر
     * @throws NotFoundException در صورت عدم وجود کاربر
     * @throws DuplicatePhoneException در صورت تعارض شماره تلفن (اگر تغییر کند)
     */
    public ProfileResponse updateProfile(long id, UpdateProfileRequest req) {
        return ProfileResponse.from(service.updateProfile(id, req));
    }
}
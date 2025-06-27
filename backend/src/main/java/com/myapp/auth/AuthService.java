package com.myapp.auth;

import com.myapp.auth.dto.RegisterRequest;
import com.myapp.auth.dto.LoginRequest;
import com.myapp.auth.dto.UpdateProfileRequest;
import com.myapp.common.exceptions.InvalidCredentialsException;
import com.myapp.common.models.User;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.exceptions.DuplicatePhoneException;
import com.myapp.common.utils.JWTUtil;

import java.util.Objects;

/**
 * سرویس احراز هویت - لایه منطق کسب‌وکار برای عملیات احراز هویت
 * این کلاس تمام عملیات مربوط به ثبت نام، ورود، تجدید token و مدیریت پروفایل را مدیریت می‌کند
 * از الگوی Service Layer استفاده می‌کند
 */
public class AuthService {

    private final AuthRepository repository;  // مخزن داده‌ها برای دسترسی به کاربران

    /**
     * سازنده سرویس احراز هویت
     * 
     * @param repository مخزن داده‌های احراز هویت
     */
    public AuthService(AuthRepository repository) {
        this.repository = repository;
    }

    /**
     * ثبت نام کاربر جدید و بازگشت کپی ذخیره شده
     * 
     * @param req درخواست ثبت نام حاوی اطلاعات کاربر
     * @return کاربر ثبت شده همراه با ID تولید شده
     * @throws DuplicatePhoneException در صورت تکراری بودن شماره تلفن
     */
    public User register(RegisterRequest req) {
        Objects.requireNonNull(req, "request must not be null");  // اعتبارسنجی ورودی
        
        // ایجاد شیء کاربر از اطلاعات درخواست - بدون ID تا Hibernate خودش تولید کند
        User user = new User(req.getFullName(), req.getPhone(), req.getEmail(),
                req.getPasswordHash(), req.getRole(), req.getAddress());
        
        return repository.saveNew(user);  // ذخیره کاربر در دیتابیس
    }

    /**
     * ورود کاربر با شماره تلفن و رمز عبور
     * در صورت مطابقت، کاربر را برمی‌گرداند، در غیر اینصورت استثنا پرتاب می‌کند
     * 
     * @param phone شماره تلفن کاربر
     * @param passwordHash رمز عبور hash شده
     * @return کاربر وارد شده
     * @throws InvalidCredentialsException در صورت نامعتبر بودن اعتبارات
     */
    public User login(String phone, String passwordHash) {
        return repository.findByPhone(phone)                            // یافتن کاربر با شماره تلفن
                .filter(u -> u.getPasswordHash().equals(passwordHash))  // فیلتر بر اساس رمز عبور
                .orElseThrow(InvalidCredentialsException::new);         // پرتاب استثنا در صورت عدم مطابقت
    }

    /**
     * ورود کاربر با استفاده از درخواست ورود (LoginRequest)
     * این متد wrapper برای متد login اصلی است
     * 
     * @param req درخواست ورود حاوی اطلاعات کاربر
     * @return AuthResult حاوی اطلاعات کاربر یا خطا
     */
    public AuthResult login(LoginRequest req) {
        Objects.requireNonNull(req, "login request must not be null");  // اعتبارسنجی ورودی
        
        try {
            // تلاش برای ورود کاربر
            User user = login(req.getPhone(), req.getPasswordHash());
            
            // تولید جفت token (Access + Refresh)
            String[] tokens = JWTUtil.generateTokenPair(user.getId(), user.getPhone(), user.getRole().toString());
            
            // بازگشت AuthResult با هر دو token
            return AuthResult.refreshed(user.getId(), user.getPhone(), user.getRole().toString(), tokens[0], tokens[1]);
            
        } catch (InvalidCredentialsException e) {
            return AuthResult.unauthenticated("Invalid phone or password");
        } catch (Exception e) {
            return AuthResult.unauthenticated("Login failed: " + e.getMessage());
        }
    }
    
    /**
     * ورود کاربر و تولید JWT token ها
     * این متد ورود کاربر را بررسی کرده و token های دسترسی و تجدید تولید می‌کند
     * 
     * @param phone شماره تلفن کاربر
     * @param passwordHash رمز عبور hash شده
     * @return AuthResult حاوی JWT token ها
     */
    public AuthResult loginWithTokens(String phone, String passwordHash) {
        try {
            // تلاش برای ورود کاربر
            User user = login(phone, passwordHash);
            
            // تولید جفت token (Access + Refresh)
            String[] tokens = JWTUtil.generateTokenPair(user.getId(), user.getPhone(), user.getRole().toString());
            
            // بازگشت AuthResult با هر دو token
            return AuthResult.refreshed(user.getId(), user.getPhone(), user.getRole().toString(), tokens[0], tokens[1]);
            
        } catch (InvalidCredentialsException e) {
            return AuthResult.unauthenticated("Invalid phone or password");
        } catch (Exception e) {
            return AuthResult.unauthenticated("Login failed: " + e.getMessage());
        }
    }
    
    /**
     * تجدید Access Token با استفاده از Refresh Token
     * 
     * @param refreshToken Refresh Token برای تجدید
     * @return AuthResult حاوی token های جدید
     */
    public AuthResult refreshToken(String refreshToken) {
        return AuthMiddleware.refreshAccessToken(refreshToken, repository);
    }
    
    /**
     * اعتبارسنجی Access Token
     * 
     * @param accessToken Access Token برای اعتبارسنجی
     * @return AuthResult حاوی اطلاعات کاربر
     */
    public AuthResult validateToken(String accessToken) {
        return AuthMiddleware.authenticateToken(accessToken);
    }
    
    /**
     * خروج کاربر (invalidate کردن token در سمت کلاینت)
     * توجه: در سیستم JWT stateless، خروج معمولاً در سمت کلاینت مدیریت می‌شود
     * در سیستم‌های production ممکن است نیاز به نگهداری blacklist از token ها باشد
     * 
     * @param userId شناسه کاربر
     * @return پیام موفقیت
     */
    public String logout(Long userId) {
        // در سیستم JWT stateless، خروج معمولاً با حذف token از storage کلاینت انجام می‌شود
        return "Logged out successfully";
    }

    /**
     * به‌روزرسانی پروفایل کاربر (نام کامل، آدرس، ایمیل)
     * در صورت تغییر شماره تلفن، DuplicatePhoneException منتشر خواهد شد
     * 
     * @param updated کاربر با اطلاعات به‌روز شده
     * @return کاربر به‌روز شده
     * @throws DuplicatePhoneException در صورت تعارض شماره تلفن
     */
    public User updateProfile(User updated) throws DuplicatePhoneException {
        return repository.update(updated);
    }

    // ---------------------------------------------------------------------
    // عملیات پروفایل

    /**
     * دریافت پروفایل کاربر بر اساس شناسه
     * 
     * @param id شناسه کاربر
     * @return پروفایل کاربر
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public User getProfile(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    /**
     * به‌روزرسانی پروفایل کاربر با درخواست جزئی
     * فقط فیلدهای ارسال شده در درخواست به‌روز می‌شوند
     * 
     * @param id شناسه کاربر
     * @param req درخواست به‌روزرسانی پروفایل
     * @return کاربر به‌روز شده
     * @throws NotFoundException در صورت عدم وجود کاربر
     * @throws DuplicatePhoneException در صورت تعارض شماره تلفن
     */
    public User updateProfile(long id, com.myapp.auth.dto.UpdateProfileRequest req) {
        // یافتن کاربر موجود
        User existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));

        // به‌روزرسانی فقط فیلدهای ارسال شده (partial update)
        if (req.getFullName() != null) existing.setFullName(req.getFullName());  // به‌روزرسانی نام کامل
        if (req.getEmail() != null) existing.setEmail(req.getEmail());          // به‌روزرسانی ایمیل
        if (req.getAddress() != null) existing.setAddress(req.getAddress());    // به‌روزرسانی آدرس

        return repository.update(existing);  // ذخیره تغییرات در دیتابیس
    }
    
    /**
     * ثبت کاربر مستقیم (برای استفاده در HTTP API)
     * این متد برای ثبت نام مستقیم کاربران از طریق API استفاده می‌شود
     * 
     * @param user کاربر برای ثبت نام
     * @return کاربر ثبت شده
     * @throws DuplicatePhoneException در صورت تکراری بودن شماره تلفن
     */
    public User registerUser(User user) {
        return repository.saveNew(user);
    }
}

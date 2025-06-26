package com.myapp.auth.dto;

// وارد کردن مدل کاربر
import com.myapp.common.models.User;

/**
 * کلاس پاسخ پروفایل - DTO برای ارسال اطلاعات پروفایل کاربر
 * این record شامل اطلاعات عمومی کاربر بدون داده‌های حساس مثل رمز عبور است
 * از Java Records برای immutability و کد تمیزتر استفاده می‌کند
 * 
 * @param id شناسه یکتای کاربر
 * @param fullName نام و نام خانوادگی کاربر
 * @param phone شماره تلفن کاربر
 * @param email آدرس ایمیل کاربر
 * @param role نقش کاربر در سیستم
 * @param address آدرس کاربر
 */
public record ProfileResponse(long id, String fullName, String phone, String email, User.Role role, String address) {
    
    /**
     * متد Factory برای تبدیل User entity به ProfileResponse
     * این متد داده‌های حساس مثل رمز عبور را حذف می‌کند
     * 
     * @param u شیء User برای تبدیل
     * @return ProfileResponse حاوی اطلاعات عمومی کاربر
     */
    public static ProfileResponse from(User u) {
        return new ProfileResponse(u.getId(), u.getFullName(), u.getPhone(), u.getEmail(), u.getRole(), u.getAddress());
    }
} 
package com.myapp.auth.dto;

import com.myapp.common.models.User;

/**
 * کلاس درخواست ثبت نام - DTO برای دریافت اطلاعات ثبت نام کاربر
 * این کلاس داده‌های لازم برای ایجاد حساب کاربری جدید را نگهداری می‌کند
 * از الگوی DTO (Data Transfer Object) استفاده می‌کند
 */
public class RegisterRequest {
    private String fullName;     // نام و نام خانوادگی کاربر
    private String phone;        // شماره تلفن (یکتا و اجباری)
    private String email;        // آدرس ایمیل (اختیاری)
    private String passwordHash; // رمز عبور hash شده
    private User.Role role;      // نقش کاربر در سیستم
    private String address;      // آدرس کاربر (اختیاری)

    /**
     * سازنده کامل - برای ایجاد درخواست ثبت نام با تمام اطلاعات
     * 
     * @param fullName نام و نام خانوادگی کاربر
     * @param phone شماره تلفن کاربر
     * @param email آدرس ایمیل کاربر
     * @param passwordHash رمز عبور hash شده
     * @param role نقش کاربر
     * @param address آدرس کاربر
     */
    public RegisterRequest(String fullName, String phone, String email, String passwordHash, User.Role role, String address) {
        this.fullName = fullName;         // تنظیم نام کامل
        this.phone = phone;               // تنظیم شماره تلفن
        this.email = email;               // تنظیم ایمیل
        this.passwordHash = passwordHash; // تنظیم رمز عبور hash شده
        this.role = role;                 // تنظیم نقش کاربر
        this.address = address;           // تنظیم آدرس
    }

    // متدهای Getter برای دسترسی به داده‌ها
    public String getFullName() { return fullName; }        // دریافت نام کامل
    public String getPhone() { return phone; }              // دریافت شماره تلفن
    public String getEmail() { return email; }              // دریافت ایمیل
    public String getPasswordHash() { return passwordHash; } // دریافت رمز عبور hash شده
    public User.Role getRole() { return role; }             // دریافت نقش کاربر
    public String getAddress() { return address; }          // دریافت آدرس
} 
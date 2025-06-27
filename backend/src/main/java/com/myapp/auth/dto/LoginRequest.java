package com.myapp.auth.dto;

/**
 * کلاس درخواست ورود - DTO برای دریافت اطلاعات ورود کاربر
 * این کلاس داده‌های لازم برای احراز هویت کاربر را نگهداری می‌کند
 * از الگوی DTO (Data Transfer Object) استفاده می‌کند
 */
public class LoginRequest {
    private String phone;        // شماره تلفن کاربر (یکتا و اجباری)
    private String passwordHash; // رمز عبور hash شده

    /**
     * سازنده کامل - برای ایجاد درخواست ورود با اطلاعات ضروری
     * 
     * @param phone شماره تلفن کاربر
     * @param passwordHash رمز عبور hash شده
     */
    public LoginRequest(String phone, String passwordHash) {
        this.phone = phone;               // تنظیم شماره تلفن
        this.passwordHash = passwordHash; // تنظیم رمز عبور hash شده
    }

    /**
     * سازنده پیش‌فرض - برای framework های serialization
     */
    public LoginRequest() {
    }

    // متدهای Getter برای دسترسی به داده‌ها
    /**
     * دریافت شماره تلفن کاربر
     * 
     * @return شماره تلفن کاربر
     */
    public String getPhone() { 
        return phone; 
    }

    /**
     * دریافت رمز عبور hash شده
     * 
     * @return رمز عبور hash شده
     */
    public String getPasswordHash() { 
        return passwordHash; 
    }

    // متدهای Setter برای تنظیم داده‌ها (برای framework های serialization)
    /**
     * تنظیم شماره تلفن کاربر
     * 
     * @param phone شماره تلفن کاربر
     */
    public void setPhone(String phone) { 
        this.phone = phone; 
    }

    /**
     * تنظیم رمز عبور hash شده
     * 
     * @param passwordHash رمز عبور hash شده
     */
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }

    /**
     * نمایش رشته‌ای از درخواست ورود
     * برای debugging مفید است (بدون نمایش رمز عبور)
     * 
     * @return نمایش رشته‌ای از LoginRequest
     */
    @Override
    public String toString() {
        return String.format("LoginRequest{phone='%s', passwordHash='***'}", phone);
    }
} 
package com.myapp.auth.dto;

/**
 * کلاس درخواست به‌روزرسانی پروفایل - DTO برای به‌روزرسانی اطلاعات کاربر
 * این کلاس فقط فیلدهای قابل به‌روزرسانی را شامل می‌شود
 * از الگوی Partial Update استفاده می‌کند (فقط فیلدهای ارسال شده به‌روز می‌شوند)
 */
public class UpdateProfileRequest {
    private String fullName; // نام و نام خانوادگی جدید (اختیاری)
    private String email;    // آدرس ایمیل جدید (اختیاری)
    private String address;  // آدرس جدید (اختیاری)

    /**
     * سازنده پیش‌فرض - برای framework ها و JSON deserialization
     */
    public UpdateProfileRequest() {
    }

    /**
     * سازنده کامل - برای ایجاد درخواست با تمام فیلدها
     * 
     * @param fullName نام و نام خانوادگی جدید
     * @param email آدرس ایمیل جدید
     * @param address آدرس جدید
     */
    public UpdateProfileRequest(String fullName, String email, String address) {
        this.fullName = fullName; // تنظیم نام کامل جدید
        this.email = email;       // تنظیم ایمیل جدید
        this.address = address;   // تنظیم آدرس جدید
    }

    // متدهای Getter برای دسترسی به داده‌ها
    public String getFullName() { return fullName; } // دریافت نام کامل جدید
    public String getEmail() { return email; }       // دریافت ایمیل جدید
    public String getAddress() { return address; }   // دریافت آدرس جدید

    // متدهای Setter برای تنظیم داده‌ها (برای framework های serialization)
    /**
     * تنظیم نام کامل جدید
     * 
     * @param fullName نام و نام خانوادگی جدید
     */
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
    }

    /**
     * تنظیم آدرس ایمیل جدید
     * 
     * @param email آدرس ایمیل جدید
     */
    public void setEmail(String email) { 
        this.email = email; 
    }

    /**
     * تنظیم آدرس جدید
     * 
     * @param address آدرس جدید
     */
    public void setAddress(String address) { 
        this.address = address; 
    }

    /**
     * نمایش رشته‌ای از درخواست به‌روزرسانی پروفایل
     * برای debugging مفید است
     * 
     * @return نمایش رشته‌ای از UpdateProfileRequest
     */
    @Override
    public String toString() {
        return String.format("UpdateProfileRequest{fullName='%s', email='%s', address='%s'}", 
                fullName, email, address);
    }
} 

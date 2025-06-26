package com.myapp.common.models;

import jakarta.persistence.*;

/**
 * مدل کاربر - نماینده یکی از کاربران سیستم
 * این کلاس تمام انواع کاربران (خریدار، فروشنده، پیک، مدیر) را پوشش می‌دهد
 * با استفاده از JPA برای ذخیره‌سازی در دیتابیس تنظیم شده است
 */
@Entity                           // نشان‌دهنده entity در JPA
@Table(name = "users")           // نام جدول در دیتابیس
public class User {

    /* ---------- فیلدهای کلاس ---------- */
    
    @Id                                                    // کلید اصلی
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // Auto-increment برای شناسه
    private Long id;                                       // شناسه یکتای کاربر

    @Column(name = "full_name", nullable = false)  // نام کامل - اجباری
    private String fullName;                       // نام و نام خانوادگی کاربر

    @Column(unique = true, nullable = false)       // شماره تلفن - یکتا و اجباری
    private String phone;                          // شماره تلفن کاربر (برای ورود)

    private String email;                          // آدرس ایمیل (اختیاری)

    @Column(name = "password_hash", nullable = false)  // رمز عبور hash شده - اجباری
    private String passwordHash;                       // رمز عبور رمزگذاری شده

    @Enumerated(EnumType.STRING)                   // ذخیره enum به صورت رشته
    private Role role;                             // نقش کاربر در سیستم

    private String address;                        // آدرس کاربر (اختیاری)

    @Column(name = "is_active", nullable = false)  // وضعیت فعال بودن حساب
    private Boolean isActive = true;               // پیش‌فرض: فعال

    /**
     * enum نقش‌های مختلف کاربران در سیستم
     * BUYER: خریدار (مشتری)
     * SELLER: فروشنده (صاحب رستوران)
     * COURIER: پیک (تحویل‌دهنده)
     * ADMIN: مدیر سیستم
     */
    public enum Role { BUYER, SELLER, COURIER, ADMIN }

    /* ---------- سازنده‌های کلاس ---------- */
    
    /**
     * سازنده اصلی بدون id - برای کد اصلی برنامه
     * Hibernate خودش id را تولید می‌کند
     */
    public User(String fullName,
                String phone,
                String email,
                String passwordHash,
                Role role,
                String address) {
        this.fullName     = fullName;     // تنظیم نام کامل
        this.phone        = phone;        // تنظیم شماره تلفن
        this.email        = email;        // تنظیم ایمیل
        this.passwordHash = passwordHash; // تنظیم رمز عبور hash شده
        this.role         = role;         // تنظیم نقش کاربر
        this.address      = address;      // تنظیم آدرس
        this.isActive     = true;         // کاربران جدید به صورت پیش‌فرض فعال هستند
    }

    /**
     * سازنده با Long id - برای تست‌هایی که id را صراحتاً می‌دهند
     * معمولاً در تست‌های واحد استفاده می‌شود
     */
    public User(Long id,
                String fullName,
                String phone,
                String email,
                String passwordHash,
                Role role,
                String address) {
        this(fullName, phone, email, passwordHash, role, address); // فراخوانی سازنده اصلی
        this.id = id; // تنظیم دستی شناسه
    }

    /**
     * سازنده سازگاری قدیمی (int id) برای حفظ سازگاری با تست‌های قبلی
     * تبدیل int به Long برای id
     */
    public User(int id,
                String fullName,
                String phone,
                String email,
                String passwordHash,
                Role role,
                String address) {
        this((long) id, fullName, phone, email, passwordHash, role, address); // تبدیل int به Long
    }

    /**
     * سازنده پیش‌فرض - برای JPA و فرمورک‌ها
     * JPA نیاز به سازنده بدون پارامتر دارد
     */
    public User() { 
        this.isActive = true; // حتی در سازنده پیش‌فرض، فعال بودن را true قرار می‌دهد
    }

    /* ---------- Getter و Setter ها ---------- */
    
    // دریافت و تنظیم شناسه کاربر
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // دریافت و تنظیم نام کامل کاربر
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    // دریافت و تنظیم شماره تلفن کاربر
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // دریافت و تنظیم ایمیل کاربر
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // دریافت و تنظیم رمز عبور hash شده
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // دریافت و تنظیم نقش کاربر
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // دریافت و تنظیم آدرس کاربر
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    // دریافت و تنظیم وضعیت فعال بودن حساب کاربری
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    /* ---------- متدهای Factory ---------- */
    
    /**
     * متد Factory برای ثبت نام کاربر - ایجاد کاربر با نقش BUYER
     * این متد برای ساده‌سازی فرآیند ثبت نام استفاده می‌شود
     * 
     * @param fullName نام و نام خانوادگی
     * @param phone شماره تلفن
     * @param email آدرس ایمیل
     * @param passwordHash رمز عبور hash شده
     * @param address آدرس کاربر
     * @return شیء User جدید با نقش BUYER
     */
    public static User forRegistration(String fullName, String phone, String email, String passwordHash, String address) {
        return new User(fullName, phone, email, passwordHash, Role.BUYER, address);
    }
}
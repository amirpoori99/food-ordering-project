package com.myapp.common.models;

import jakarta.persistence.*;

/**
 * مدل رستوران - نماینده یکی از رستوران‌های ثبت شده در سیستم
 * این کلاس اطلاعات رستوران‌ها شامل نام، آدرس، وضعیت و صاحب رستوران را نگهداری می‌کند
 * با استفاده از JPA برای ذخیره‌سازی در دیتابیس تنظیم شده است
 */
@Entity                              // نشان‌دهنده entity در JPA
@Table(name = "restaurants")        // نام جدول در دیتابیس
public class Restaurant {

    /* ---------- فیلدهای کلاس ---------- */
    
    @Id                                                    // کلید اصلی
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // Auto-increment برای شناسه
    private Long id;                                       // شناسه یکتای رستوران

    @Column(name = "owner_id", nullable = false)  // شناسه صاحب رستوران - اجباری
    private Long ownerId;                         // FK → users.id (کلید خارجی به جدول کاربران)

    @Column(nullable = false)                     // نام رستوران - اجباری
    private String name;                          // نام رستوران

    private String address;                       // آدرس رستوران (اختیاری)
    private String phone;                         // شماره تلفن رستوران (اختیاری)

    @Enumerated(EnumType.STRING)                  // ذخیره enum به صورت رشته
    private RestaurantStatus status = RestaurantStatus.PENDING; // وضعیت رستوران - پیش‌فرض: در انتظار تأیید

    /* ---------- سازنده‌های کلاس ---------- */
    
    /**
     * سازنده پیش‌فرض - برای JPA و فرمورک‌ها
     * JPA نیاز به سازنده بدون پارامتر دارد
     */
    public Restaurant() { }

    /**
     * سازنده اصلی بدون id - برای ایجاد اشیاء جدید در منطق کسب‌وکار
     * Hibernate خودش id را تولید می‌کند
     * 
     * @param ownerId شناسه صاحب رستوران
     * @param name نام رستوران
     * @param address آدرس رستوران
     * @param phone شماره تلفن رستوران
     */
    public Restaurant(Long ownerId,
                      String name,
                      String address,
                      String phone) {
        this.ownerId = ownerId;   // تنظیم شناسه صاحب رستوران
        this.name    = name;      // تنظیم نام رستوران
        this.address = address;   // تنظیم آدرس رستوران
        this.phone   = phone;     // تنظیم شماره تلفن رستوران
    }

    /**
     * سازنده با Long id - برای تست‌هایی که id اولیه را تنظیم می‌کنند
     * معمولاً در تست‌های واحد استفاده می‌شود
     * 
     * @param id شناسه رستوران
     * @param ownerId شناسه صاحب رستوران
     * @param name نام رستوران
     * @param address آدرس رستوران
     * @param phone شماره تلفن رستوران
     * @param status وضعیت رستوران
     */
    public Restaurant(Long id,
                      Long ownerId,
                      String name,
                      String address,
                      String phone,
                      RestaurantStatus status) {
        this(ownerId, name, address, phone); // فراخوانی سازنده اصلی
        this.id     = id;                     // تنظیم دستی شناسه
        this.status = status;                 // تنظیم وضعیت رستوران
    }

    /**
     * سازنده سازگاری قدیمی (int id / ownerId) برای حفظ سازگاری با تست‌های قبلی
     * تبدیل int به Long برای id و ownerId
     */
    public Restaurant(int id,
                      long ownerId,
                      String name,
                      String address,
                      String phone,
                      RestaurantStatus status) {
        this((long) id, ownerId, name, address, phone, status); // تبدیل int به Long
    }

    /* ---------- متدهای Factory Static ---------- */
    
    /**
     * متد Factory برای ثبت رستوران جدید
     * این متد برای ساده‌سازی فرآیند ثبت رستوران استفاده می‌شود
     * 
     * @param ownerId شناسه صاحب رستوران
     * @param name نام رستوران
     * @param address آدرس رستوران
     * @param phone شماره تلفن رستوران
     * @return شیء Restaurant جدید با وضعیت PENDING
     */
    public static Restaurant forRegistration(Long ownerId,
                                             String name,
                                             String address,
                                             String phone) {
        return new Restaurant(ownerId, name, address, phone);
    }

    /* ---------- Getter و Setter ها ---------- */
    
    // دریافت و تنظیم شناسه رستوران
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // دریافت و تنظیم شناسه صاحب رستوران
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    // دریافت و تنظیم نام رستوران
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // دریافت و تنظیم آدرس رستوران
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // دریافت و تنظیم شماره تلفن رستوران
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // دریافت و تنظیم وضعیت رستوران
    public RestaurantStatus getStatus() { return status; }
    public void setStatus(RestaurantStatus status) { this.status = status; }
}
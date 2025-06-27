package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * مدل کوپن تخفیف - نماینده کوپن‌های تخفیف سیستم سفارش غذا
 * 
 * این کلاس سیستم کامل مدیریت کوپن‌های تخفیف را پیاده‌سازی می‌کند:
 * 
 * === انواع تخفیف ===
 * - تخفیف درصدی (PERCENTAGE): درصد از مبلغ سفارش
 * - تخفیف مبلغ ثابت (FIXED_AMOUNT): مبلغ مشخص تخفیف
 * 
 * === ویژگی‌های کلیدی ===
 * - کد یکتای کوپن برای شناسایی
 * - محدودیت حداقل مبلغ سفارش
 * - محدودیت حداکثر تخفیف برای درصدی‌ها
 * - محدودیت تعداد استفاده کلی
 * - محدودیت تعداد استفاده per-user
 * - بازه زمانی اعتبار
 * - امکان اختصاص به رستوران خاص
 * 
 * === قوانین کسب‌وکار ===
 * - کد کوپن باید یکتا باشد
 * - تاریخ شروع و پایان اعتبار اجباری
 * - تخفیف نمی‌تواند از مبلغ سفارش بیشتر باشد
 * - محدودیت‌های استفاده قابل تنظیم
 * 
 * === Factory Methods ===
 * - createPercentageCoupon(): ایجاد کوپن درصدی
 * - createFixedAmountCoupon(): ایجاد کوپن مبلغ ثابت
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "coupons")
public class Coupon {
    
    /** شناسه یکتای کوپن */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** کد یکتای کوپن برای استفاده توسط کاربر */
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    /** توضیحات کوپن برای نمایش به کاربر */
    @Column(nullable = false, length = 255)
    private String description;
    
    /** نوع تخفیف (درصدی یا مبلغ ثابت) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;
    
    /** مقدار تخفیف - درصد (0-100) یا مبلغ ثابت */
    @Column(nullable = false)
    private Double value;
    
    /** حداقل مبلغ سفارش برای استفاده از کوپن */
    @Column(name = "min_order_amount")
    private Double minOrderAmount;
    
    /** حداکثر مبلغ تخفیف برای کوپن‌های درصدی */
    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;
    
    /** محدودیت تعداد استفاده کلی (null = نامحدود) */
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    /** تعداد استفاده شده از کوپن */
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;
    
    /** محدودیت استفاده per-user (null = نامحدود) */
    @Column(name = "per_user_limit")
    private Integer perUserLimit;
    
    /** زمان شروع اعتبار کوپن */
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    /** زمان پایان اعتبار کوپن */
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    /** وضعیت فعال بودن کوپن */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /** زمان ایجاد کوپن */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /** زمان آخرین به‌روزرسانی */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /** رستوران مختص این کوپن (null = همه رستوران‌ها) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
    
    /** شناسه ایجادکننده کوپن (ادمین یا صاحب رستوران) */
    @Column(name = "created_by")
    private Long createdBy;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض
     * زمان ایجاد را به زمان فعلی تنظیم می‌کند
     */
    public Coupon() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * سازنده اصلی برای ایجاد کوپن
     * 
     * @param code کد یکتای کوپن
     * @param description توضیحات
     * @param type نوع تخفیف
     * @param value مقدار تخفیف
     * @param validFrom زمان شروع اعتبار
     * @param validUntil زمان پایان اعتبار
     */
    public Coupon(String code, String description, CouponType type, Double value, 
                  LocalDateTime validFrom, LocalDateTime validUntil) {
        this();
        this.code = code;
        this.description = description;
        this.type = type;
        this.value = value;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * ایجاد کوپن تخفیف درصدی
     * 
     * @param code کد کوپن
     * @param description توضیحات
     * @param percentage درصد تخفیف (0-100)
     * @param validFrom زمان شروع
     * @param validUntil زمان پایان
     * @return کوپن درصدی ایجاد شده
     * @throws IllegalArgumentException اگر درصد خارج از محدوده 0-100 باشد
     */
    public static Coupon createPercentageCoupon(String code, String description, Double percentage,
                                               LocalDateTime validFrom, LocalDateTime validUntil) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("درصد تخفیف باید بین 0 تا 100 باشد");
        }
        return new Coupon(code, description, CouponType.PERCENTAGE, percentage, validFrom, validUntil);
    }
    
    /**
     * ایجاد کوپن تخفیف مبلغ ثابت
     * 
     * @param code کد کوپن
     * @param description توضیحات
     * @param amount مبلغ تخفیف
     * @param validFrom زمان شروع
     * @param validUntil زمان پایان
     * @return کوپن مبلغ ثابت ایجاد شده
     * @throws IllegalArgumentException اگر مبلغ منفی یا صفر باشد
     */
    public static Coupon createFixedAmountCoupon(String code, String description, Double amount,
                                                LocalDateTime validFrom, LocalDateTime validUntil) {
        if (amount <= 0) {
            throw new IllegalArgumentException("مبلغ تخفیف باید مثبت باشد");
        }
        return new Coupon(code, description, CouponType.FIXED_AMOUNT, amount, validFrom, validUntil);
    }
    
    // ==================== BUSINESS LOGIC METHODS ====================
    
    /**
     * بررسی اعتبار و قابلیت استفاده کوپن
     * 
     * @return true اگر کوپن معتبر و قابل استفاده باشد
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(validFrom) && 
               now.isBefore(validUntil) &&
               (usageLimit == null || usedCount < usageLimit);
    }
    
    /**
     * بررسی قابلیت اعمال کوپن روی مبلغ سفارش
     * 
     * @param orderAmount مبلغ سفارش
     * @return true اگر کوپن قابل اعمال باشد
     */
    public boolean canApplyToOrder(Double orderAmount) {
        if (!isValid()) {
            return false;
        }
        return minOrderAmount == null || orderAmount >= minOrderAmount;
    }
    
    /**
     * محاسبه مبلغ تخفیف برای سفارش
     * 
     * @param orderAmount مبلغ سفارش
     * @return مبلغ تخفیف قابل اعمال
     */
    public Double calculateDiscount(Double orderAmount) {
        if (!canApplyToOrder(orderAmount)) {
            return 0.0;
        }
        
        Double discount;
        if (type == CouponType.PERCENTAGE) {
            discount = orderAmount * (value / 100.0);
            // اعمال محدودیت حداکثر تخفیف
            if (maxDiscountAmount != null && discount > maxDiscountAmount) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = value;
        }
        
        // اطمینان از عدم تجاوز تخفیف از مبلغ سفارش
        return Math.min(discount, orderAmount);
    }
    
    /**
     * استفاده از کوپن (افزایش شمارنده استفاده)
     * 
     * @throws IllegalStateException اگر کوپن غیرقابل استفاده باشد
     */
    public void use() {
        if (!isValid()) {
            throw new IllegalStateException("امکان استفاده از کوپن غیرمعتبر وجود ندارد");
        }
        if (usageLimit != null && usedCount >= usageLimit) {
            throw new IllegalStateException("محدودیت استفاده از کوپن تجاوز شده است");
        }
        
        usedCount++;
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * بازگشت استفاده از کوپن (کاهش شمارنده)
     * در صورت لغو سفارش استفاده می‌شود
     */
    public void revertUsage() {
        if (usedCount > 0) {
            usedCount--;
            updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * بررسی وجود استفاده باقی‌مانده
     * 
     * @return true اگر هنوز قابل استفاده باشد
     */
    public boolean hasRemainingUses() {
        return usageLimit == null || usedCount < usageLimit;
    }
    
    /**
     * محاسبه تعداد استفاده باقی‌مانده
     * 
     * @return تعداد استفاده باقی‌مانده (null = نامحدود)
     */
    public Integer getRemainingUses() {
        if (usageLimit == null) {
            return null; // نامحدود
        }
        return Math.max(0, usageLimit - usedCount);
    }
    
    /**
     * غیرفعال کردن کوپن
     * کوپن قابل استفاده نخواهد بود ولی حذف نمی‌شود
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * فعال کردن کوپن
     * کوپن مجدداً قابل استفاده خواهد شد
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * به‌روزرسانی اطلاعات کوپن
     * 
     * @param description توضیحات جدید
     * @param minOrderAmount حداقل مبلغ سفارش
     * @param maxDiscountAmount حداکثر تخفیف
     * @param usageLimit محدودیت استفاده
     * @param perUserLimit محدودیت per-user
     * @param validUntil تاریخ انقضای جدید
     */
    public void updateInfo(String description, Double minOrderAmount, Double maxDiscountAmount,
                          Integer usageLimit, Integer perUserLimit, LocalDateTime validUntil) {
        if (description != null && !description.trim().isEmpty()) {
            this.description = description.trim();
        }
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.usageLimit = usageLimit;
        this.perUserLimit = perUserLimit;
        if (validUntil != null) {
            this.validUntil = validUntil;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه کوپن */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    /** @return کد کوپن */
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    /** @return توضیحات کوپن */
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    /** @return نوع کوپن */
    public CouponType getType() { return type; }
    public void setType(CouponType type) { this.type = type; }
    
    /** @return مقدار تخفیف */
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    
    /** @return حداقل مبلغ سفارش */
    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    /** @return حداکثر تخفیف */
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    
    /** @return محدودیت استفاده */
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    
    /** @return تعداد استفاده شده */
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    
    /** @return محدودیت per-user */
    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }
    
    /** @return زمان شروع اعتبار */
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    
    /** @return زمان پایان اعتبار */
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    
    /** @return وضعیت فعال بودن */
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    /** @return زمان ایجاد */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    /** @return زمان به‌روزرسانی */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /** @return رستوران مختص */
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    
    /** @return شناسه ایجادکننده */
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    // ==================== OBJECT METHODS ====================
    
    /**
     * بررسی تساوی دو کوپن
     * 
     * @param o شیء مقایسه
     * @return true در صورت برابری
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id) && Objects.equals(code, coupon.code);
    }
    
    /**
     * محاسبه hash code
     * 
     * @return hash code بر اساس شناسه و کد
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }
    
    /**
     * نمایش رشته‌ای کوپن
     * 
     * @return اطلاعات کلیدی برای debugging
     */
    @Override
    public String toString() {
        return "Coupon{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", isActive=" + isActive +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", usedCount=" + usedCount +
                ", usageLimit=" + usageLimit +
                '}';
    }
    
    /**
     * تعیین انواع کوپن تخفیف
     */
    public enum CouponType {
        /** تخفیف درصدی (مثلاً 20% تخفیف) */
        PERCENTAGE,
        
        /** تخفیف مبلغ ثابت (مثلاً 10,000 تومان تخفیف) */
        FIXED_AMOUNT
    }
}

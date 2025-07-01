package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * مدل استفاده از کوپن - نماینده استفاده فردی کاربران از کوپن‌های تخفیف
 * 
 * این کلاس سیستم ردیابی و محدودیت استفاده از کوپن‌ها را پیاده‌سازی می‌کند:
 * 
 * === ویژگی‌های کلیدی ===
 * - ردیابی استفاده تک‌تک کاربران از کوپن‌ها
 * - اعمال محدودیت‌های per-user
 * - ذخیره تاریخچه استفاده برای آمارگیری
 * - پشتیبانی از revert (بازگشت) در صورت لغو سفارش
 * - ثبت دقیق مبالغ تخفیف و سفارش در زمان استفاده
 * 
 * === قوانین کسب‌وکار ===
 * - هر کاربر برای هر کوپن محدودیت استفاده دارد
 * - ترکیب یکتا: کوپن + کاربر + سفارش
 * - امکان بازگشت در صورت لغو سفارش
 * - ذخیره snapshot مبالغ در زمان استفاده
 * 
 * === استفاده در سیستم ===
 * - اعتبارسنجی محدودیت‌های کوپن
 * - گزارش‌گیری از استفاده کوپن‌ها
 * - audit trail برای مالیات و حسابداری
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "coupon_usage", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id", "order_id"}))
public class CouponUsage {
    
    /** شناسه یکتای استفاده از کوپن */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    /** کوپن استفاده شده (رابطه چند‌به‌یک) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    
    /** شناسه کاربر استفاده‌کننده */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /** شناسه سفارش مربوطه */
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    /** مبلغ تخفیف اعمال شده در زمان استفاده */
    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;
    
    /** مبلغ کل سفارش در زمان استفاده (snapshot) */
    @Column(name = "order_amount", nullable = false)
    private Double orderAmount;
    
    /** زمان استفاده از کوپن */
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
    
    /** زمان بازگشت استفاده (در صورت لغو سفارش) */
    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;
    
    /** وضعیت فعال بودن استفاده (false = بازگشت داده شده) */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض
     * زمان استفاده را به زمان فعلی تنظیم می‌کند
     */
    public CouponUsage() {
        this.usedAt = LocalDateTime.now();
    }
    
    /**
     * سازنده کامل برای ایجاد استفاده جدید از کوپن
     * 
     * @param coupon کوپن استفاده شده
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param discountAmount مبلغ تخفیف
     * @param orderAmount مبلغ کل سفارش
     */
    public CouponUsage(Coupon coupon, Long userId, Long orderId, Double discountAmount, Double orderAmount) {
        this();
        this.coupon = coupon;
        this.userId = userId;
        this.orderId = orderId;
        this.discountAmount = discountAmount;
        this.orderAmount = orderAmount;
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * بازگشت استفاده از کوپن
     * 
     * این متد زمانی فراخوانی می‌شود که سفارش لغو شده یا کوپن باید برگردانده شود
     * 
     * @throws IllegalStateException اگر استفاده قبلاً برگردانده شده باشد
     */
    public void revert() {
        if (!isActive) {
            throw new IllegalStateException("استفاده از کوپن قبلاً برگردانده شده است");
        }
        this.isActive = false;
        this.revertedAt = LocalDateTime.now();
    }
    
    /**
     * بررسی برگردانده شدن استفاده
     * 
     * @return true اگر استفاده برگردانده شده باشد
     */
    public boolean isReverted() {
        return !isActive;
    }
    
    /**
     * محاسبه درصد تخفیف اعمال شده
     * 
     * @return درصد تخفیف نسبت به مبلغ سفارش
     */
    public Double getDiscountPercentage() {
        if (orderAmount == null || orderAmount == 0) {
            return 0.0;
        }
        return (discountAmount / orderAmount) * 100.0;
    }
    
    /**
     * بررسی اعتبار استفاده
     * 
     * @return true اگر استفاده معتبر و فعال باشد
     */
    public boolean isValid() {
        return isActive && discountAmount > 0;
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه استفاده */
    public Long getId() { return id; }
    
    /** @param id شناسه استفاده */
    public void setId(Long id) { this.id = id; }
    
    /** @return کوپن استفاده شده */
    public Coupon getCoupon() { return coupon; }
    
    /** @param coupon کوپن استفاده شده */
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    
    /** @return شناسه کاربر */
    public Long getUserId() { return userId; }
    
    /** @param userId شناسه کاربر */
    public void setUserId(Long userId) { this.userId = userId; }
    
    /** @return شناسه سفارش */
    public Long getOrderId() { return orderId; }
    
    /** @param orderId شناسه سفارش */
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    /** @return مبلغ تخفیف */
    public Double getDiscountAmount() { return discountAmount; }
    
    /** @param discountAmount مبلغ تخفیف */
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    
    /** @return مبلغ سفارش */
    public Double getOrderAmount() { return orderAmount; }
    
    /** @param orderAmount مبلغ سفارش */
    public void setOrderAmount(Double orderAmount) { this.orderAmount = orderAmount; }
    
    /** @return زمان استفاده */
    public LocalDateTime getUsedAt() { return usedAt; }
    
    /** @param usedAt زمان استفاده */
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    /** @return زمان بازگشت */
    public LocalDateTime getRevertedAt() { return revertedAt; }
    
    /** @param revertedAt زمان بازگشت */
    public void setRevertedAt(LocalDateTime revertedAt) { this.revertedAt = revertedAt; }
    
    /** @return وضعیت فعال بودن */
    public Boolean getIsActive() { return isActive; }
    
    /** @param isActive وضعیت فعال بودن */
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    // ==================== OBJECT METHODS ====================
    
    /**
     * بررسی تساوی دو استفاده از کوپن
     * 
     * @param o شیء مقایسه
     * @return true در صورت برابری
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponUsage that = (CouponUsage) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * محاسبه hash code
     * 
     * @return hash code بر اساس شناسه
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * نمایش رشته‌ای استفاده از کوپن
     * 
     * @return اطلاعات کلیدی برای debugging
     */
    @Override
    public String toString() {
        return "CouponUsage{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderId=" + orderId +
                ", discountAmount=" + discountAmount +
                ", orderAmount=" + orderAmount +
                ", usedAt=" + usedAt +
                ", isActive=" + isActive +
                '}';
    }
} 

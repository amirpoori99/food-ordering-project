package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * مدل علاقه‌مندی - نماینده رستوران‌های مورد علاقه کاربران
 * 
 * این کلاس سیستم مدیریت لیست رستوران‌های مورد علاقه را پیاده‌سازی می‌کند:
 * 
 * === ویژگی‌های کلیدی ===
 * - یک کاربر می‌تواند هر رستوران را فقط یک بار به علاقه‌مندی‌ها اضافه کند
 * - امکان اضافه کردن یادداشت شخصی برای هر رستوران
 * - ثبت زمان اضافه شدن به علاقه‌مندی‌ها
 * - Index بندی برای جستجوی سریع
 * - پشتیبانی از legacy constructors برای سازگاری
 * 
 * === قوانین کسب‌وکار ===
 * - محدودیت یکتایی: هر کاربر + رستوران فقط یک رکورد
 * - یادداشت‌ها حداکثر 500 کاراکتر
 * - زمان ایجاد به صورت خودکار ثبت می‌شود
 * 
 * === استفاده در سیستم ===
 * - نمایش فهرست رستوران‌های مورد علاقه کاربر
 * - پیشنهاد رستوران بر اساس علاقه‌مندی‌ها
 * - آمارگیری از محبوب‌ترین رستوران‌ها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "restaurant_id"}),
       indexes = {
           @Index(name = "idx_favorite_user", columnList = "user_id"),
           @Index(name = "idx_favorite_restaurant", columnList = "restaurant_id"),
           @Index(name = "idx_favorite_created", columnList = "created_at")
       })
public class Favorite {
    
    /** شناسه یکتای علاقه‌مندی */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    /** کاربر صاحب علاقه‌مندی (رابطه چند‌به‌یک) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /** رستوران مورد علاقه (رابطه چند‌به‌یک) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    /** زمان اضافه شدن به علاقه‌مندی‌ها */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /** یادداشت شخصی کاربر درباره رستوران (اختیاری، حداکثر 500 کاراکتر) */
    @Column(name = "notes", length = 500)
    private String notes;

    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض
     * زمان ایجاد را به زمان فعلی تنظیم می‌کند
     */
    public Favorite() {
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * سازنده ساده با کاربر و رستوران
     * 
     * @param user کاربر صاحب علاقه‌مندی
     * @param restaurant رستوران مورد علاقه
     */
    public Favorite(User user, Restaurant restaurant) {
        this();
        this.user = user;
        this.restaurant = restaurant;
    }
    
    /**
     * سازنده کامل با یادداشت
     * 
     * @param user کاربر صاحب علاقه‌مندی
     * @param restaurant رستوران مورد علاقه
     * @param notes یادداشت شخصی
     */
    public Favorite(User user, Restaurant restaurant, String notes) {
        this(user, restaurant);
        this.notes = notes;
    }
    
    /**
     * سازنده سازگاری قدیمی
     * برای حفظ سازگاری با تست‌های قبلی
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @param createdAt زمان ایجاد
     * @deprecated استفاده از سازنده‌های دیگر توصیه می‌شود
     */
    @Deprecated
    public Favorite(long userId, long restaurantId, java.time.Instant createdAt) {
        this.createdAt = LocalDateTime.ofInstant(createdAt, java.time.ZoneOffset.UTC);
        // توجه: اشیاء User و Restaurant باید جداگانه تنظیم شوند
    }

    // ==================== BUSINESS LOGIC METHODS ====================
    
    /**
     * به‌روزرسانی یادداشت شخصی
     * 
     * یادداشت‌ها به 500 کاراکتر محدود می‌شوند
     * 
     * @param notes یادداشت جدید
     */
    public void updateNotes(String notes) {
        this.notes = notes != null && notes.trim().length() > 500 
            ? notes.trim().substring(0, 500) 
            : (notes != null ? notes.trim() : null);
    }
    
    /**
     * بررسی وجود یادداشت
     * 
     * @return true اگر یادداشت غیر خالی داشته باشد
     */
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
    
    /**
     * بررسی جدید بودن علاقه‌مندی (طی 30 روز گذشته)
     * 
     * @return true اگر علاقه‌مندی جدید باشد
     */
    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * دریافت تاریخ ایجاد در قالب قابل نمایش
     * 
     * @return رشته نمایش تاریخ و زمان
     */
    public String getCreatedAtFormatted() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // ==================== LIFECYCLE CALLBACKS ====================
    
    /**
     * فراخوانی قبل از ذخیره در دیتابیس
     * تنظیم زمان ایجاد در صورت عدم وجود
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه علاقه‌مندی */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** @return کاربر صاحب علاقه‌مندی */
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /** @return رستوران مورد علاقه */
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    /** @return زمان اضافه شدن */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return یادداشت شخصی */
    public String getNotes() { return notes; }
    public void setNotes(String notes) { updateNotes(notes); }

    // ==================== LEGACY GETTERS ====================
    
    /**
     * دریافت شناسه کاربر (برای سازگاری قدیمی)
     * 
     * @return شناسه کاربر یا 0 در صورت عدم وجود
     * @deprecated استفاده از getUser().getId() توصیه می‌شود
     */
    @Deprecated
    public long getUserId() {
        return user != null ? user.getId() : 0L;
    }

    /**
     * دریافت شناسه رستوران (برای سازگاری قدیمی)
     * 
     * @return شناسه رستوران یا 0 در صورت عدم وجود
     * @deprecated استفاده از getRestaurant().getId() توصیه می‌شود
     */
    @Deprecated
    public long getRestaurantId() {
        return restaurant != null ? restaurant.getId() : 0L;
    }

    // ==================== OBJECT METHODS ====================
    
    /**
     * بررسی تساوی دو علاقه‌مندی
     * 
     * دو علاقه‌مندی در صورت داشتن همان ID، کاربر و رستوران برابر هستند
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favorite favorite = (Favorite) o;
        return Objects.equals(id, favorite.id) &&
               Objects.equals(user, favorite.user) &&
               Objects.equals(restaurant, favorite.restaurant);
    }

    /**
     * محاسبه hash code برای علاقه‌مندی
     * 
     * بر اساس ID، شناسه کاربر و شناسه رستوران محاسبه می‌شود
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, user != null ? user.getId() : null, 
                           restaurant != null ? restaurant.getId() : null);
    }
    
    /**
     * نمایش رشته‌ای علاقه‌مندی
     * 
     * شامل اطلاعات کلیدی برای debugging و logging
     */
    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", restaurantId=" + (restaurant != null ? restaurant.getId() : null) +
                ", hasNotes=" + hasNotes() +
                ", createdAt=" + createdAt +
                '}';
    }
} 

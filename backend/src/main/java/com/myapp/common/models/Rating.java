package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * مدل رتبه‌بندی - نماینده نظرات و امتیازات کاربران برای رستوران‌ها
 * 
 * این کلاس سیستم کامل نظرسنجی و رتبه‌بندی رستوران‌ها را مدیریت می‌کند:
 * 
 * === ویژگی‌های کلیدی ===
 * - امتیازدهی از 1 تا 5 ستاره
 * - متن نظر اختیاری تا 1000 کاراکتر
 * - سیستم تأیید نظرات توسط ادمین
 * - شمارنده مفید بودن نظرات
 * - محدودیت یک نظر برای هر کاربر/رستوران
 * - Indexing برای عملکرد بهتر جستجو
 * 
 * === قوانین کسب‌وکار ===
 * - هر کاربر فقط یک نظر برای هر رستوران می‌تواند ثبت کند
 * - امتیاز باید بین 1 تا 5 باشد
 * - متن نظر حداکثر 1000 کاراکتر
 * - تغییر نظر باعث reset شدن تأیید می‌شود
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "ratings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "restaurant_id"}),
       indexes = {
           @Index(name = "idx_rating_restaurant", columnList = "restaurant_id"),
           @Index(name = "idx_rating_user", columnList = "user_id"),
           @Index(name = "idx_rating_score", columnList = "rating_score")
       })
public class Rating {
    
    /** شناسه یکتای رتبه‌بندی */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** کاربر ثبت‌کننده نظر (رابطه چند‌به‌یک) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /** رستوران مورد نظر (رابطه چند‌به‌یک) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    /** امتیاز رتبه‌بندی (1 تا 5 ستاره) */
    @Column(name = "rating_score", nullable = false)
    private Integer ratingScore;
    
    /** متن نظر (اختیاری، حداکثر 1000 کاراکتر) */
    @Column(name = "review_text", length = 1000)
    private String reviewText;
    
    /** زمان ایجاد نظر */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /** زمان آخرین به‌روزرسانی */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /** وضعیت تأیید نظر توسط ادمین */
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;
    
    /** تعداد نفراتی که نظر را مفید دانسته‌اند */
    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;

    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض
     * مقادیر اولیه پیش‌فرض را تنظیم می‌کند
     */
    public Rating() {
        this.createdAt = LocalDateTime.now();
        this.isVerified = false;
        this.helpfulCount = 0;
    }
    
    /**
     * سازنده با امتیاز ساده
     * 
     * @param user کاربر نظردهنده
     * @param restaurant رستوران مورد نظر
     * @param ratingScore امتیاز (1-5)
     */
    public Rating(User user, Restaurant restaurant, Integer ratingScore) {
        this();
        this.user = user;
        this.restaurant = restaurant;
        setRatingScore(ratingScore);
    }
    
    /**
     * سازنده کامل با امتیاز و متن نظر
     * 
     * @param user کاربر نظردهنده
     * @param restaurant رستوران مورد نظر
     * @param ratingScore امتیاز (1-5)
     * @param reviewText متن نظر
     */
    public Rating(User user, Restaurant restaurant, Integer ratingScore, String reviewText) {
        this(user, restaurant, ratingScore);
        this.reviewText = reviewText;
    }
    
    // ==================== BUSINESS LOGIC METHODS ====================
    
    /**
     * به‌روزرسانی امتیاز و متن نظر
     * 
     * @param newScore امتیاز جدید (1-5)
     * @param newReviewText متن نظر جدید
     */
    public void updateRating(Integer newScore, String newReviewText) {
        setRatingScore(newScore);
        this.reviewText = newReviewText;
        this.updatedAt = LocalDateTime.now();
        this.isVerified = false; // ریست تأیید پس از تغییر
    }
    
    /**
     * اعتبارسنجی و تنظیم امتیاز (1-5 ستاره)
     * 
     * @param ratingScore امتیاز مورد نظر
     * @throws IllegalArgumentException در صورت نامعتبر بودن امتیاز
     */
    public void setRatingScore(Integer ratingScore) {
        if (ratingScore == null || ratingScore < 1 || ratingScore > 5) {
            throw new IllegalArgumentException("امتیاز باید بین 1 تا 5 ستاره باشد");
        }
        this.ratingScore = ratingScore;
    }
    
    /**
     * اعتبارسنجی و تنظیم متن نظر
     * 
     * @param reviewText متن نظر
     * @throws IllegalArgumentException در صورت طولانی بودن متن
     */
    public void setReviewText(String reviewText) {
        if (reviewText != null && reviewText.trim().length() > 1000) {
            throw new IllegalArgumentException("متن نظر نمی‌تواند بیشتر از 1000 کاراکتر باشد");
        }
        this.reviewText = reviewText != null ? reviewText.trim() : null;
    }
    
    /**
     * افزایش شمارنده مفید بودن
     */
    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }
    
    /**
     * کاهش شمارنده مفید بودن (حداقل 0)
     */
    public void decrementHelpfulCount() {
        if (this.helpfulCount > 0) {
            this.helpfulCount--;
        }
    }
    
    /**
     * تأیید نظر توسط ادمین
     */
    public void verify() {
        this.isVerified = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * لغو تأیید نظر توسط ادمین
     */
    public void unverify() {
        this.isVerified = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * بررسی وجود متن نظر
     * 
     * @return true اگر نظر متنی داشته باشد
     */
    public boolean hasReview() {
        return reviewText != null && !reviewText.trim().isEmpty();
    }
    
    /**
     * بررسی جدید بودن نظر (طی 30 روز گذشته)
     * 
     * @return true اگر نظر جدید باشد
     */
    public boolean isRecent() {
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * تولید نمایش ستاره‌ای امتیاز
     * 
     * @return رشته نمایش ستاره‌ها (★★★☆☆)
     */
    public String getRatingDisplay() {
        return "★".repeat(ratingScore) + "☆".repeat(5 - ratingScore);
    }

    // ==================== LIFECYCLE CALLBACKS ====================
    
    /**
     * فراخوانی قبل از ذخیره در دیتابیس
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isVerified == null) {
            isVerified = false;
        }
        if (helpfulCount == null) {
            helpfulCount = 0;
        }
    }
    
    /**
     * فراخوانی قبل از به‌روزرسانی در دیتابیس
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه رتبه‌بندی */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** @return کاربر نظردهنده */
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    /** @return رستوران مورد نظر */
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    /** @return امتیاز رتبه‌بندی */
    public Integer getRatingScore() { return ratingScore; }

    /** @return متن نظر */
    public String getReviewText() { return reviewText; }

    /** @return زمان ایجاد */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return زمان به‌روزرسانی */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** @return وضعیت تأیید */
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    /** @return تعداد مفید دانستن */
    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { 
        this.helpfulCount = Math.max(0, helpfulCount != null ? helpfulCount : 0);
    }

    // ==================== OBJECT METHODS ====================
    
    /**
     * بررسی تساوی دو رتبه‌بندی
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Objects.equals(id, rating.id) &&
               Objects.equals(user, rating.user) &&
               Objects.equals(restaurant, rating.restaurant);
    }
    
    /**
     * محاسبه hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, user != null ? user.getId() : null, 
                           restaurant != null ? restaurant.getId() : null);
    }
    
    /**
     * نمایش رشته‌ای رتبه‌بندی
     */
    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", restaurantId=" + (restaurant != null ? restaurant.getId() : null) +
                ", ratingScore=" + ratingScore +
                ", hasReview=" + hasReview() +
                ", isVerified=" + isVerified +
                ", helpfulCount=" + helpfulCount +
                ", createdAt=" + createdAt +
                '}';
    }
}

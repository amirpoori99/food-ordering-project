package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a discount coupon
 * Can be percentage-based or fixed amount discount
 * Can have usage limits and expiration dates
 */
@Entity
@Table(name = "coupons")
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;
    
    @Column(nullable = false)
    private Double value; // Percentage (0-100) or fixed amount
    
    @Column(name = "min_order_amount")
    private Double minOrderAmount; // Minimum order amount to use coupon
    
    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount; // Maximum discount for percentage coupons
    
    @Column(name = "usage_limit")
    private Integer usageLimit; // Total usage limit (null = unlimited)
    
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;
    
    @Column(name = "per_user_limit")
    private Integer perUserLimit; // Usage limit per user (null = unlimited)
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id") // null means applicable to all restaurants
    private Restaurant restaurant;
    
    @Column(name = "created_by")
    private Long createdBy; // Admin or restaurant owner who created the coupon
    
    // Constructors
    public Coupon() {
        this.createdAt = LocalDateTime.now();
    }
    
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
    
    // Factory methods
    /**
     * Creates a percentage-based coupon
     */
    public static Coupon createPercentageCoupon(String code, String description, Double percentage,
                                               LocalDateTime validFrom, LocalDateTime validUntil) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        return new Coupon(code, description, CouponType.PERCENTAGE, percentage, validFrom, validUntil);
    }
    
    /**
     * Creates a fixed amount coupon
     */
    public static Coupon createFixedAmountCoupon(String code, String description, Double amount,
                                                LocalDateTime validFrom, LocalDateTime validUntil) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Fixed amount must be positive");
        }
        return new Coupon(code, description, CouponType.FIXED_AMOUNT, amount, validFrom, validUntil);
    }
    
    // Business logic methods
    /**
     * Checks if the coupon is currently valid and usable
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(validFrom) && 
               now.isBefore(validUntil) &&
               (usageLimit == null || usedCount < usageLimit);
    }
    
    /**
     * Checks if coupon can be applied to the given order amount
     */
    public boolean canApplyToOrder(Double orderAmount) {
        if (!isValid()) {
            return false;
        }
        return minOrderAmount == null || orderAmount >= minOrderAmount;
    }
    
    /**
     * Calculates discount amount for the given order
     */
    public Double calculateDiscount(Double orderAmount) {
        if (!canApplyToOrder(orderAmount)) {
            return 0.0;
        }
        
        Double discount;
        if (type == CouponType.PERCENTAGE) {
            discount = orderAmount * (value / 100.0);
            // Apply maximum discount limit if set
            if (maxDiscountAmount != null && discount > maxDiscountAmount) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = value;
        }
        
        // Ensure discount doesn't exceed order amount
        return Math.min(discount, orderAmount);
    }
    
    /**
     * Uses the coupon (increments usage count)
     */
    public void use() {
        if (!isValid()) {
            throw new IllegalStateException("Cannot use invalid coupon");
        }
        if (usageLimit != null && usedCount >= usageLimit) {
            throw new IllegalStateException("Coupon usage limit exceeded");
        }
        
        usedCount++;
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Reverts coupon usage (decrements usage count)
     */
    public void revertUsage() {
        if (usedCount > 0) {
            usedCount--;
            updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Checks if coupon has remaining uses
     */
    public boolean hasRemainingUses() {
        return usageLimit == null || usedCount < usageLimit;
    }
    
    /**
     * Gets remaining usage count
     */
    public Integer getRemainingUses() {
        if (usageLimit == null) {
            return null; // Unlimited
        }
        return Math.max(0, usageLimit - usedCount);
    }
    
    /**
     * Deactivates the coupon
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Activates the coupon
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Updates coupon information
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
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public CouponType getType() { return type; }
    public void setType(CouponType type) { this.type = type; }
    
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    
    public Double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(Double minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    
    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }
    
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id) && Objects.equals(code, coupon.code);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }
    
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
     * Enum for coupon types
     */
    public enum CouponType {
        PERCENTAGE,     // Percentage discount (e.g., 20% off)
        FIXED_AMOUNT    // Fixed amount discount (e.g., $10 off)
    }
}

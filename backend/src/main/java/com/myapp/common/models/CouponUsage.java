package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing coupon usage by individual users
 * Used to enforce per-user usage limits and track usage history
 */
@Entity
@Table(name = "coupon_usage", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_id", "user_id", "order_id"}))
public class CouponUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;
    
    @Column(name = "order_amount", nullable = false)
    private Double orderAmount;
    
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
    
    @Column(name = "reverted_at")
    private LocalDateTime revertedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public CouponUsage() {
        this.usedAt = LocalDateTime.now();
    }
    
    public CouponUsage(Coupon coupon, Long userId, Long orderId, Double discountAmount, Double orderAmount) {
        this();
        this.coupon = coupon;
        this.userId = userId;
        this.orderId = orderId;
        this.discountAmount = discountAmount;
        this.orderAmount = orderAmount;
    }
    
    // Business methods
    public void revert() {
        if (!isActive) {
            throw new IllegalStateException("Coupon usage is already reverted");
        }
        this.isActive = false;
        this.revertedAt = LocalDateTime.now();
    }
    
    public boolean isReverted() {
        return !isActive;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    
    public Double getOrderAmount() { return orderAmount; }
    public void setOrderAmount(Double orderAmount) { this.orderAmount = orderAmount; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public LocalDateTime getRevertedAt() { return revertedAt; }
    public void setRevertedAt(LocalDateTime revertedAt) { this.revertedAt = revertedAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponUsage that = (CouponUsage) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
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
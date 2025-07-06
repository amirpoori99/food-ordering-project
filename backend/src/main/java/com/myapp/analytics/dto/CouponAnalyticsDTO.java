package com.myapp.analytics.dto;

import java.util.Map;

/**
 * DTO برای آمار کوپن‌ها
 */
public class CouponAnalyticsDTO {
    
    private long totalCoupons;
    private long usedCoupons;
    private double averageDiscount;
    private Map<String, Double> couponRedemption;
    
    // Constructors
    public CouponAnalyticsDTO() {}
    
    // Getters and Setters
    public long getTotalCoupons() {
        return totalCoupons;
    }
    
    public void setTotalCoupons(long totalCoupons) {
        this.totalCoupons = totalCoupons;
    }
    
    public long getUsedCoupons() {
        return usedCoupons;
    }
    
    public void setUsedCoupons(long usedCoupons) {
        this.usedCoupons = usedCoupons;
    }
    
    public double getAverageDiscount() {
        return averageDiscount;
    }
    
    public void setAverageDiscount(double averageDiscount) {
        this.averageDiscount = averageDiscount;
    }
    
    public Map<String, Double> getCouponRedemption() {
        return couponRedemption;
    }
    
    public void setCouponRedemption(Map<String, Double> couponRedemption) {
        this.couponRedemption = couponRedemption;
    }
} 
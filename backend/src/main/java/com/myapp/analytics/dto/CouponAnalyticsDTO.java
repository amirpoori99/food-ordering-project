package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class CouponAnalyticsDTO {
    private long totalCoupons;
    private long usedCoupons;
    private double couponUsageRate;
    private Map<String, Double> couponEffectiveness;
    private List<Map<String, Object>> topCoupons;
    private double totalDiscounts;
    private double averageDiscount;

    public CouponAnalyticsDTO() {}

    public long getTotalCoupons() { return totalCoupons; }
    public void setTotalCoupons(long totalCoupons) { this.totalCoupons = totalCoupons; }

    public long getUsedCoupons() { return usedCoupons; }
    public void setUsedCoupons(long usedCoupons) { this.usedCoupons = usedCoupons; }

    public double getCouponUsageRate() { return couponUsageRate; }
    public void setCouponUsageRate(double couponUsageRate) { this.couponUsageRate = couponUsageRate; }

    public Map<String, Double> getCouponEffectiveness() { return couponEffectiveness; }
    public void setCouponEffectiveness(Map<String, Double> couponEffectiveness) { this.couponEffectiveness = couponEffectiveness; }

    public List<Map<String, Object>> getTopCoupons() { return topCoupons; }
    public void setTopCoupons(List<Map<String, Object>> topCoupons) { this.topCoupons = topCoupons; }

    public double getTotalDiscounts() { return totalDiscounts; }
    public void setTotalDiscounts(double totalDiscounts) { this.totalDiscounts = totalDiscounts; }

    public double getAverageDiscount() { return averageDiscount; }
    public void setAverageDiscount(double averageDiscount) { this.averageDiscount = averageDiscount; }
} 
package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class CouponAnalyticsDTO {
    private int totalCoupons;
    private int usedCoupons;
    private double totalDiscount;
    private Map<String, Double> couponRedemption;
    private List<Map<String, Object>> couponPerformance;
    private List<Map<String, Object>> couponUsage;
    private List<Map<String, Object>> couponEffectiveness;

    public CouponAnalyticsDTO() {}

    public CouponAnalyticsDTO(int totalCoupons, int usedCoupons, double totalDiscount,
                            Map<String, Double> couponRedemption,
                            List<Map<String, Object>> couponPerformance,
                            List<Map<String, Object>> couponUsage,
                            List<Map<String, Object>> couponEffectiveness) {
        this.totalCoupons = totalCoupons;
        this.usedCoupons = usedCoupons;
        this.totalDiscount = totalDiscount;
        this.couponRedemption = couponRedemption;
        this.couponPerformance = couponPerformance;
        this.couponUsage = couponUsage;
        this.couponEffectiveness = couponEffectiveness;
    }

    public int getTotalCoupons() { return totalCoupons; }
    public void setTotalCoupons(int totalCoupons) { this.totalCoupons = totalCoupons; }
    
    public int getUsedCoupons() { return usedCoupons; }
    public void setUsedCoupons(int usedCoupons) { this.usedCoupons = usedCoupons; }
    
    public double getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(double totalDiscount) { this.totalDiscount = totalDiscount; }
    
    public Map<String, Double> getCouponRedemption() { return couponRedemption; }
    public void setCouponRedemption(Map<String, Double> couponRedemption) { this.couponRedemption = couponRedemption; }
    
    public List<Map<String, Object>> getCouponPerformance() { return couponPerformance; }
    public void setCouponPerformance(List<Map<String, Object>> couponPerformance) { this.couponPerformance = couponPerformance; }
    
    public List<Map<String, Object>> getCouponUsage() { return couponUsage; }
    public void setCouponUsage(List<Map<String, Object>> couponUsage) { this.couponUsage = couponUsage; }
    
    public List<Map<String, Object>> getCouponEffectiveness() { return couponEffectiveness; }
    public void setCouponEffectiveness(List<Map<String, Object>> couponEffectiveness) { this.couponEffectiveness = couponEffectiveness; }
} 
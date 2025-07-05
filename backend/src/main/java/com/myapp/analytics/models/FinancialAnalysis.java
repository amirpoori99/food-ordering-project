package com.myapp.analytics.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * مدل تحلیل مالی
 * این کلاس گزارش‌های مالی تفصیلی سیستم را تولید می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class FinancialAnalysis {
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime generatedAt;
    
    // درآمدها
    private Double totalRevenue;
    private Double revenueGrowthRate;
    private Map<String, Double> revenueByRestaurant;
    private Map<String, Double> revenueByCategory;
    private Map<String, Double> dailyRevenue;
    
    // هزینه‌ها
    private Double totalCommissions;
    private Double deliveryFees;
    private Double refunds;
    private Double operationalCosts;
    
    // سودآوری
    private Double grossProfit;
    private Double netProfit;
    private Double profitMargin;
    
    // تحلیل پرداخت
    private Map<String, Integer> paymentMethodsBreakdown;
    private Double successfulPaymentRate;
    private Double failedPaymentRate;
    
    // Constructors
    public FinancialAnalysis() {
        this.generatedAt = LocalDateTime.now();
    }
    
    public FinancialAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.generatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public Double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public Double getRevenueGrowthRate() {
        return revenueGrowthRate;
    }
    
    public void setRevenueGrowthRate(Double revenueGrowthRate) {
        this.revenueGrowthRate = revenueGrowthRate;
    }
    
    public Map<String, Double> getRevenueByRestaurant() {
        return revenueByRestaurant;
    }
    
    public void setRevenueByRestaurant(Map<String, Double> revenueByRestaurant) {
        this.revenueByRestaurant = revenueByRestaurant;
    }
    
    public Map<String, Double> getRevenueByCategory() {
        return revenueByCategory;
    }
    
    public void setRevenueByCategory(Map<String, Double> revenueByCategory) {
        this.revenueByCategory = revenueByCategory;
    }
    
    public Map<String, Double> getDailyRevenue() {
        return dailyRevenue;
    }
    
    public void setDailyRevenue(Map<String, Double> dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }
    
    public Double getTotalCommissions() {
        return totalCommissions;
    }
    
    public void setTotalCommissions(Double totalCommissions) {
        this.totalCommissions = totalCommissions;
    }
    
    public Double getDeliveryFees() {
        return deliveryFees;
    }
    
    public void setDeliveryFees(Double deliveryFees) {
        this.deliveryFees = deliveryFees;
    }
    
    public Double getRefunds() {
        return refunds;
    }
    
    public void setRefunds(Double refunds) {
        this.refunds = refunds;
    }
    
    public Double getOperationalCosts() {
        return operationalCosts;
    }
    
    public void setOperationalCosts(Double operationalCosts) {
        this.operationalCosts = operationalCosts;
    }
    
    public Double getGrossProfit() {
        return grossProfit;
    }
    
    public void setGrossProfit(Double grossProfit) {
        this.grossProfit = grossProfit;
    }
    
    public Double getNetProfit() {
        return netProfit;
    }
    
    public void setNetProfit(Double netProfit) {
        this.netProfit = netProfit;
    }
    
    public Double getProfitMargin() {
        return profitMargin;
    }
    
    public void setProfitMargin(Double profitMargin) {
        this.profitMargin = profitMargin;
    }
    
    public Map<String, Integer> getPaymentMethodsBreakdown() {
        return paymentMethodsBreakdown;
    }
    
    public void setPaymentMethodsBreakdown(Map<String, Integer> paymentMethodsBreakdown) {
        this.paymentMethodsBreakdown = paymentMethodsBreakdown;
    }
    
    public Double getSuccessfulPaymentRate() {
        return successfulPaymentRate;
    }
    
    public void setSuccessfulPaymentRate(Double successfulPaymentRate) {
        this.successfulPaymentRate = successfulPaymentRate;
    }
    
    public Double getFailedPaymentRate() {
        return failedPaymentRate;
    }
    
    public void setFailedPaymentRate(Double failedPaymentRate) {
        this.failedPaymentRate = failedPaymentRate;
    }
    
    /**
     * محاسبه نرخ رشد درآمد
     */
    public void calculateGrowthRate() {
        if (totalRevenue != null && totalRevenue > 0) {
            // محاسبه ساده - باید با داده‌های قبلی مقایسه شود
            this.revenueGrowthRate = 0.0;
        }
    }
    
    /**
     * محاسبه سود خالص
     */
    public void calculateNetProfit() {
        if (totalRevenue != null && totalCommissions != null && 
            deliveryFees != null && refunds != null && operationalCosts != null) {
            
            this.grossProfit = totalRevenue - refunds;
            this.netProfit = grossProfit - totalCommissions - operationalCosts;
            
            if (totalRevenue > 0) {
                this.profitMargin = (netProfit / totalRevenue) * 100;
            }
        }
    }
    
    @Override
    public String toString() {
        return "FinancialAnalysis{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalRevenue=" + totalRevenue +
                ", netProfit=" + netProfit +
                ", profitMargin=" + profitMargin +
                ", generatedAt=" + generatedAt +
                '}';
    }
} 
package com.myapp.analytics.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * مدل تحلیل مالی
 * این کلاس گزارش‌های مالی تفصیلی سیستم را تولید می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
@Entity
@Table(name = "financial_analysis")
public class FinancialAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
    
    // درآمدها
    @Column(name = "total_revenue", precision = 15, scale = 2)
    private Double totalRevenue;
    
    @Column(name = "revenue_growth_rate", precision = 5, scale = 2)
    private Double revenueGrowthRate;
    
    @Column(name = "revenue_by_restaurant", columnDefinition = "TEXT")
    private String revenueByRestaurant; // JSON string
    
    @Column(name = "revenue_by_category", columnDefinition = "TEXT")
    private String revenueByCategory; // JSON string
    
    @Column(name = "daily_revenue", columnDefinition = "TEXT")
    private String dailyRevenue; // JSON string
    
    // هزینه‌ها
    @Column(name = "total_commissions", precision = 15, scale = 2)
    private Double totalCommissions;
    
    @Column(name = "delivery_fees", precision = 15, scale = 2)
    private Double deliveryFees;
    
    @Column(name = "refunds", precision = 15, scale = 2)
    private Double refunds;
    
    @Column(name = "operational_costs", precision = 15, scale = 2)
    private Double operationalCosts;
    
    // سودآوری
    @Column(name = "gross_profit", precision = 15, scale = 2)
    private Double grossProfit;
    
    @Column(name = "net_profit", precision = 15, scale = 2)
    private Double netProfit;
    
    @Column(name = "profit_margin", precision = 5, scale = 2)
    private Double profitMargin;
    
    // تحلیل پرداخت
    @Column(name = "payment_methods_breakdown", columnDefinition = "TEXT")
    private String paymentMethodsBreakdown; // JSON string
    
    @Column(name = "successful_payment_rate", precision = 5, scale = 2)
    private Double successfulPaymentRate;
    
    @Column(name = "failed_payment_rate", precision = 5, scale = 2)
    private Double failedPaymentRate;
    
    // فیلدهای جدید مورد نیاز
    @Column(name = "successful_payments")
    private Integer successfulPayments;
    
    @Column(name = "failed_payments")
    private Integer failedPayments;
    
    @Column(name = "payment_success_rate", precision = 5, scale = 2)
    private Double paymentSuccessRate;
    
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
        return parseJsonToDoubleMap(revenueByRestaurant);
    }
    
    public void setRevenueByRestaurant(Map<String, Double> revenueByRestaurant) {
        this.revenueByRestaurant = mapToJson(revenueByRestaurant);
    }
    
    public Map<String, Double> getRevenueByCategory() {
        return parseJsonToDoubleMap(revenueByCategory);
    }
    
    public void setRevenueByCategory(Map<String, Double> revenueByCategory) {
        this.revenueByCategory = mapToJson(revenueByCategory);
    }
    
    public Map<String, Double> getDailyRevenue() {
        return parseJsonToDoubleMap(dailyRevenue);
    }
    
    public void setDailyRevenue(Map<String, Double> dailyRevenue) {
        this.dailyRevenue = mapToJson(dailyRevenue);
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
        return parseJsonToIntegerMap(paymentMethodsBreakdown);
    }
    
    public void setPaymentMethodsBreakdown(Map<String, Integer> paymentMethodsBreakdown) {
        this.paymentMethodsBreakdown = mapToJson(paymentMethodsBreakdown);
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
    
    // متدهای جدید مورد نیاز
    public Integer getSuccessfulPayments() {
        return successfulPayments;
    }
    
    public void setSuccessfulPayments(Integer successfulPayments) {
        this.successfulPayments = successfulPayments;
    }
    
    public Integer getFailedPayments() {
        return failedPayments;
    }
    
    public void setFailedPayments(Integer failedPayments) {
        this.failedPayments = failedPayments;
    }
    
    public Double getPaymentSuccessRate() {
        return paymentSuccessRate;
    }
    
    public void setPaymentSuccessRate(Double paymentSuccessRate) {
        this.paymentSuccessRate = paymentSuccessRate;
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
    
    // Helper methods for JSON conversion
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private Map<String, Double> parseJsonToDoubleMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    private Map<String, Integer> parseJsonToIntegerMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    private String mapToJson(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
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
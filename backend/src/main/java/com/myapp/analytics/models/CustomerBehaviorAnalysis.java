package com.myapp.analytics.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * مدل تحلیل رفتار مشتریان
 * این کلاس الگوهای رفتاری مشتریان را تحلیل می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class CustomerBehaviorAnalysis {
    
    private Long userId;
    private int analysisPeriod; // روزهای تحلیل
    private LocalDateTime generatedAt;
    
    // الگوهای سفارش‌دهی
    private Double orderFrequency;
    private Double averageOrderValue;
    private List<String> favoriteRestaurants;
    private List<String> favoriteItems;
    
    // الگوهای زمانی
    private Map<String, Integer> orderTimePatterns;
    private List<String> peakOrderDays;
    
    // تجربه کاربری
    private Double averageRating;
    private Double complaintRate;
    
    // پیش‌بینی‌ها
    private Double churnProbability;
    private LocalDateTime nextOrderPrediction;
    private List<String> recommendedItems;
    
    // Constructors
    public CustomerBehaviorAnalysis() {
        this.generatedAt = LocalDateTime.now();
    }
    
    public CustomerBehaviorAnalysis(Long userId, int analysisPeriod) {
        this.userId = userId;
        this.analysisPeriod = analysisPeriod;
        this.generatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public int getAnalysisPeriod() {
        return analysisPeriod;
    }
    
    public void setAnalysisPeriod(int analysisPeriod) {
        this.analysisPeriod = analysisPeriod;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public Double getOrderFrequency() {
        return orderFrequency;
    }
    
    public void setOrderFrequency(Double orderFrequency) {
        this.orderFrequency = orderFrequency;
    }
    
    public Double getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(Double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public List<String> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }
    
    public void setFavoriteRestaurants(List<String> favoriteRestaurants) {
        this.favoriteRestaurants = favoriteRestaurants;
    }
    
    public List<String> getFavoriteItems() {
        return favoriteItems;
    }
    
    public void setFavoriteItems(List<String> favoriteItems) {
        this.favoriteItems = favoriteItems;
    }
    
    public Map<String, Integer> getOrderTimePatterns() {
        return orderTimePatterns;
    }
    
    public void setOrderTimePatterns(Map<String, Integer> orderTimePatterns) {
        this.orderTimePatterns = orderTimePatterns;
    }
    
    public List<String> getPeakOrderDays() {
        return peakOrderDays;
    }
    
    public void setPeakOrderDays(List<String> peakOrderDays) {
        this.peakOrderDays = peakOrderDays;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Double getComplaintRate() {
        return complaintRate;
    }
    
    public void setComplaintRate(Double complaintRate) {
        this.complaintRate = complaintRate;
    }
    
    public Double getChurnProbability() {
        return churnProbability;
    }
    
    public void setChurnProbability(Double churnProbability) {
        this.churnProbability = churnProbability;
    }
    
    public LocalDateTime getNextOrderPrediction() {
        return nextOrderPrediction;
    }
    
    public void setNextOrderPrediction(LocalDateTime nextOrderPrediction) {
        this.nextOrderPrediction = nextOrderPrediction;
    }
    
    public List<String> getRecommendedItems() {
        return recommendedItems;
    }
    
    public void setRecommendedItems(List<String> recommendedItems) {
        this.recommendedItems = recommendedItems;
    }
    
    @Override
    public String toString() {
        return "CustomerBehaviorAnalysis{" +
                "userId=" + userId +
                ", analysisPeriod=" + analysisPeriod +
                ", orderFrequency=" + orderFrequency +
                ", averageOrderValue=" + averageOrderValue +
                ", churnProbability=" + churnProbability +
                ", generatedAt=" + generatedAt +
                '}';
    }
} 
package com.myapp.analytics.models;

import java.time.LocalDateTime;

/**
 * مدل پیش‌بینی سفارش
 * این کلاس پیش‌بینی سفارش بعدی کاربر را نگهداری می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class OrderPrediction {
    
    private Long userId;
    private LocalDateTime predictedOrderTime;
    private Double probability;
    private String predictedRestaurant;
    private Double predictedOrderValue;
    private String confidence; // HIGH, MEDIUM, LOW
    private LocalDateTime generatedAt;
    
    // Constructors
    public OrderPrediction() {
        this.generatedAt = LocalDateTime.now();
    }
    
    public OrderPrediction(Long userId, LocalDateTime predictedOrderTime, Double probability) {
        this.userId = userId;
        this.predictedOrderTime = predictedOrderTime;
        this.probability = probability;
        this.generatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getPredictedOrderTime() {
        return predictedOrderTime;
    }
    
    public void setPredictedOrderTime(LocalDateTime predictedOrderTime) {
        this.predictedOrderTime = predictedOrderTime;
    }
    
    public Double getProbability() {
        return probability;
    }
    
    public void setProbability(Double probability) {
        this.probability = probability;
    }
    
    public String getPredictedRestaurant() {
        return predictedRestaurant;
    }
    
    public void setPredictedRestaurant(String predictedRestaurant) {
        this.predictedRestaurant = predictedRestaurant;
    }
    
    public Double getPredictedOrderValue() {
        return predictedOrderValue;
    }
    
    public void setPredictedOrderValue(Double predictedOrderValue) {
        this.predictedOrderValue = predictedOrderValue;
    }
    
    public String getConfidence() {
        return confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    /**
     * تعیین سطح اعتماد بر اساس احتمال
     */
    public void calculateConfidence() {
        if (probability != null) {
            if (probability >= 0.8) {
                this.confidence = "HIGH";
            } else if (probability >= 0.5) {
                this.confidence = "MEDIUM";
            } else {
                this.confidence = "LOW";
            }
        }
    }
    
    /**
     * تنظیم تاریخ پیش‌بینی شده
     */
    public void setPredictedDate(LocalDateTime predictedDate) {
        this.predictedOrderTime = predictedDate;
    }

    /**
     * تنظیم نمره اعتماد
     */
    public void setConfidenceScore(double confidenceScore) {
        this.probability = confidenceScore;
        calculateConfidence();
    }

    /**
     * تنظیم مبلغ پیش‌بینی شده
     */
    public void setPredictedAmount(Double predictedAmount) {
        this.predictedOrderValue = predictedAmount;
    }

    @Override
    public String toString() {
        return "OrderPrediction{" +
                "userId=" + userId +
                ", predictedOrderTime=" + predictedOrderTime +
                ", probability=" + probability +
                ", confidence='" + confidence + '\'' +
                ", predictedOrderValue=" + predictedOrderValue +
                ", generatedAt=" + generatedAt +
                '}';
    }
} 
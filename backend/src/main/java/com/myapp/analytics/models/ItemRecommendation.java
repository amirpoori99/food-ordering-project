package com.myapp.analytics.models;

import java.time.LocalDateTime;

/**
 * مدل توصیه آیتم
 * این کلاس توصیه‌های شخصی‌سازی شده برای کاربران را نگهداری می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class ItemRecommendation {
    
    private Long userId;
    private Long itemId;
    private String itemName;
    private String restaurantName;
    private Double score; // امتیاز توصیه (0-1)
    private String reason; // دلیل توصیه
    private String category;
    private Double price;
    private Double rating;
    private String confidence; // HIGH, MEDIUM, LOW
    private LocalDateTime generatedAt;
    
    // Constructors
    public ItemRecommendation() {
        this.generatedAt = LocalDateTime.now();
    }
    
    public ItemRecommendation(Long userId, Long itemId, String itemName, Double score) {
        this.userId = userId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.score = score;
        this.generatedAt = LocalDateTime.now();
        this.calculateConfidence();
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getItemId() {
        return itemId;
    }
    
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
        this.calculateConfidence();
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
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
     * محاسبه سطح اعتماد بر اساس امتیاز
     */
    private void calculateConfidence() {
        if (score != null) {
            if (score >= 0.8) {
                this.confidence = "HIGH";
            } else if (score >= 0.6) {
                this.confidence = "MEDIUM";
            } else {
                this.confidence = "LOW";
            }
        }
    }
    
    /**
     * تعیین دلیل توصیه بر اساس امتیاز و کتگوری
     */
    public void generateReason() {
        if (score != null && score >= 0.8) {
            this.reason = "بر اساس سفارشات قبلی شما";
        } else if (category != null) {
            this.reason = "محبوب در دسته " + category;
        } else if (rating != null && rating >= 4.0) {
            this.reason = "امتیاز بالا از کاربران";
        } else {
            this.reason = "توصیه شخصی‌سازی شده";
        }
    }
    
    @Override
    public String toString() {
        return "ItemRecommendation{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", score=" + score +
                ", confidence='" + confidence + '\'' +
                ", reason='" + reason + '\'' +
                ", generatedAt=" + generatedAt +
                '}';
    }
} 
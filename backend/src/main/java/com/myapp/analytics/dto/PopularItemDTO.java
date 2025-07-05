package com.myapp.analytics.dto;

/**
 * DTO برای آیتم‌های محبوب
 */
public class PopularItemDTO {
    
    private long itemId;
    private String itemName;
    private String category;
    private long orderCount;
    private double totalRevenue;
    private double averageRating;
    private double popularityScore;
    
    // Constructors
    public PopularItemDTO() {}
    
    public PopularItemDTO(long itemId, String itemName, String category, long orderCount, double totalRevenue, double averageRating) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.orderCount = orderCount;
        this.totalRevenue = totalRevenue;
        this.averageRating = averageRating;
        this.popularityScore = calculatePopularityScore();
    }
    
    // Getters and Setters
    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    
    public double getPopularityScore() { return popularityScore; }
    public void setPopularityScore(double popularityScore) { this.popularityScore = popularityScore; }
    
    /**
     * محاسبه امتیاز محبوبیت
     */
    private double calculatePopularityScore() {
        // فرمول ساده برای محاسبه امتیاز محبوبیت
        return (orderCount * 0.6) + (totalRevenue * 0.3) + (averageRating * 0.1);
    }
} 
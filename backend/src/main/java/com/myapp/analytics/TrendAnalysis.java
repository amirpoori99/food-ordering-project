package com.myapp.analytics;

import java.time.LocalDateTime;
import java.util.List;

public class TrendAnalysis {
    private String period;
    private LocalDateTime generatedAt;
    private List<Object> salesTrends;
    private List<Object> revenueTrends;
    private List<Object> orderTrends;
    private List<Object> userGrowthTrends;
    private List<Object> userActivityTrends;
    private List<Object> userRetentionTrends;
    private List<Object> productTrends;
    private List<Object> categoryTrends;
    private List<Object> restaurantTrends;
    
    public TrendAnalysis() {}
    
    // Getters and Setters
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public List<Object> getSalesTrends() { return salesTrends; }
    public void setSalesTrends(List<Object> salesTrends) { this.salesTrends = salesTrends; }
    
    public List<Object> getRevenueTrends() { return revenueTrends; }
    public void setRevenueTrends(List<Object> revenueTrends) { this.revenueTrends = revenueTrends; }
    
    public List<Object> getOrderTrends() { return orderTrends; }
    public void setOrderTrends(List<Object> orderTrends) { this.orderTrends = orderTrends; }
    
    public List<Object> getUserGrowthTrends() { return userGrowthTrends; }
    public void setUserGrowthTrends(List<Object> userGrowthTrends) { this.userGrowthTrends = userGrowthTrends; }
    
    public List<Object> getUserActivityTrends() { return userActivityTrends; }
    public void setUserActivityTrends(List<Object> userActivityTrends) { this.userActivityTrends = userActivityTrends; }
    
    public List<Object> getUserRetentionTrends() { return userRetentionTrends; }
    public void setUserRetentionTrends(List<Object> userRetentionTrends) { this.userRetentionTrends = userRetentionTrends; }
    
    public List<Object> getProductTrends() { return productTrends; }
    public void setProductTrends(List<Object> productTrends) { this.productTrends = productTrends; }
    
    public List<Object> getCategoryTrends() { return categoryTrends; }
    public void setCategoryTrends(List<Object> categoryTrends) { this.categoryTrends = categoryTrends; }
    
    public List<Object> getRestaurantTrends() { return restaurantTrends; }
    public void setRestaurantTrends(List<Object> restaurantTrends) { this.restaurantTrends = restaurantTrends; }
} 
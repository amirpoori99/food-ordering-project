package com.myapp.analytics.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public TrendAnalysis() {
        this.salesTrends = new ArrayList<>();
        this.revenueTrends = new ArrayList<>();
        this.orderTrends = new ArrayList<>();
        this.userGrowthTrends = new ArrayList<>();
        this.userActivityTrends = new ArrayList<>();
        this.userRetentionTrends = new ArrayList<>();
        this.productTrends = new ArrayList<>();
        this.categoryTrends = new ArrayList<>();
        this.restaurantTrends = new ArrayList<>();
    }

    public TrendAnalysis(String period, LocalDateTime generatedAt, List<Object> salesTrends, 
                        List<Object> revenueTrends, List<Object> orderTrends, List<Object> userGrowthTrends, 
                        List<Object> userActivityTrends, List<Object> userRetentionTrends, 
                        List<Object> productTrends, List<Object> categoryTrends, List<Object> restaurantTrends) {
        this.period = period;
        this.generatedAt = generatedAt;
        this.salesTrends = salesTrends != null ? salesTrends : new ArrayList<>();
        this.revenueTrends = revenueTrends != null ? revenueTrends : new ArrayList<>();
        this.orderTrends = orderTrends != null ? orderTrends : new ArrayList<>();
        this.userGrowthTrends = userGrowthTrends != null ? userGrowthTrends : new ArrayList<>();
        this.userActivityTrends = userActivityTrends != null ? userActivityTrends : new ArrayList<>();
        this.userRetentionTrends = userRetentionTrends != null ? userRetentionTrends : new ArrayList<>();
        this.productTrends = productTrends != null ? productTrends : new ArrayList<>();
        this.categoryTrends = categoryTrends != null ? categoryTrends : new ArrayList<>();
        this.restaurantTrends = restaurantTrends != null ? restaurantTrends : new ArrayList<>();
    }

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
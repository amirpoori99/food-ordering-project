package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO برای آمار رستوران‌ها
 */
public class RestaurantAnalyticsDTO {
    
    // آمار کلی رستوران‌ها
    private long totalRestaurants;
    private long activeRestaurants;
    private long newRestaurants;
    
    // آمار عملکرد رستوران‌ها
    private List<Map<String, Object>> topPerformingRestaurants;
    private double averageRestaurantRating;
    private double restaurantSatisfaction;
    
    // آمار دسته‌بندی‌ها
    private Map<String, Double> categoryPerformance;
    private Map<String, Double> cuisineDistribution;
    
    // آمار جغرافیایی
    private Map<String, Double> geographicDistribution;
    private Map<String, Double> deliveryZones;
    
    // Constructors
    public RestaurantAnalyticsDTO() {}
    
    // Getters and Setters
    public long getTotalRestaurants() { return totalRestaurants; }
    public void setTotalRestaurants(long totalRestaurants) { this.totalRestaurants = totalRestaurants; }
    
    public long getActiveRestaurants() { return activeRestaurants; }
    public void setActiveRestaurants(long activeRestaurants) { this.activeRestaurants = activeRestaurants; }
    
    public long getNewRestaurants() { return newRestaurants; }
    public void setNewRestaurants(long newRestaurants) { this.newRestaurants = newRestaurants; }
    
    public List<Map<String, Object>> getTopPerformingRestaurants() { return topPerformingRestaurants; }
    public void setTopPerformingRestaurants(List<Map<String, Object>> topPerformingRestaurants) { this.topPerformingRestaurants = topPerformingRestaurants; }
    
    public double getAverageRestaurantRating() { return averageRestaurantRating; }
    public void setAverageRestaurantRating(double averageRestaurantRating) { this.averageRestaurantRating = averageRestaurantRating; }
    
    public double getRestaurantSatisfaction() { return restaurantSatisfaction; }
    public void setRestaurantSatisfaction(double restaurantSatisfaction) { this.restaurantSatisfaction = restaurantSatisfaction; }
    
    public Map<String, Double> getCategoryPerformance() { return categoryPerformance; }
    public void setCategoryPerformance(Map<String, Double> categoryPerformance) { this.categoryPerformance = categoryPerformance; }
    
    public Map<String, Double> getCuisineDistribution() { return cuisineDistribution; }
    public void setCuisineDistribution(Map<String, Double> cuisineDistribution) { this.cuisineDistribution = cuisineDistribution; }
    
    public Map<String, Double> getGeographicDistribution() { return geographicDistribution; }
    public void setGeographicDistribution(Map<String, Double> geographicDistribution) { this.geographicDistribution = geographicDistribution; }
    
    public Map<String, Double> getDeliveryZones() { return deliveryZones; }
    public void setDeliveryZones(Map<String, Double> deliveryZones) { this.deliveryZones = deliveryZones; }
} 
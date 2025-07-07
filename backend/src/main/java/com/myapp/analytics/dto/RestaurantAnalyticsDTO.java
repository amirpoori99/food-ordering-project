package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class RestaurantAnalyticsDTO {
    private int totalRestaurants;
    private int activeRestaurants;
    private double averageRating;
    private List<Map<String, Object>> topPerformingRestaurants;
    private List<Map<String, Object>> restaurantPerformance;
    private List<Map<String, Object>> restaurantCategories;

    public RestaurantAnalyticsDTO() {}

    public RestaurantAnalyticsDTO(int totalRestaurants, int activeRestaurants, double averageRating,
                                List<Map<String, Object>> topPerformingRestaurants,
                                List<Map<String, Object>> restaurantPerformance,
                                List<Map<String, Object>> restaurantCategories) {
        this.totalRestaurants = totalRestaurants;
        this.activeRestaurants = activeRestaurants;
        this.averageRating = averageRating;
        this.topPerformingRestaurants = topPerformingRestaurants;
        this.restaurantPerformance = restaurantPerformance;
        this.restaurantCategories = restaurantCategories;
    }

    public int getTotalRestaurants() { return totalRestaurants; }
    public void setTotalRestaurants(int totalRestaurants) { this.totalRestaurants = totalRestaurants; }
    
    public int getActiveRestaurants() { return activeRestaurants; }
    public void setActiveRestaurants(int activeRestaurants) { this.activeRestaurants = activeRestaurants; }
    
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    
    public List<Map<String, Object>> getTopPerformingRestaurants() { return topPerformingRestaurants; }
    public void setTopPerformingRestaurants(List<Map<String, Object>> topPerformingRestaurants) { this.topPerformingRestaurants = topPerformingRestaurants; }
    
    public List<Map<String, Object>> getRestaurantPerformance() { return restaurantPerformance; }
    public void setRestaurantPerformance(List<Map<String, Object>> restaurantPerformance) { this.restaurantPerformance = restaurantPerformance; }
    
    public List<Map<String, Object>> getRestaurantCategories() { return restaurantCategories; }
    public void setRestaurantCategories(List<Map<String, Object>> restaurantCategories) { this.restaurantCategories = restaurantCategories; }
} 
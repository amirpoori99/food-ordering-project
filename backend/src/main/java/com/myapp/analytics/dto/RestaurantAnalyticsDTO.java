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
    private double averageRating;
    private double averageDeliveryTime;
    private double averageRestaurantRating;
    private double restaurantSatisfaction;
    
    // آمار عملکرد
    private List<Map<String, Object>> topPerformingRestaurants;
    private List<Map<String, Object>> restaurantPerformance;
    private List<Map<String, Object>> restaurantCategories;
    private Map<String, Double> categoryPerformance;
    private Map<String, Double> cuisineDistribution;
    private Map<String, Double> geographicDistribution;
    private Map<String, Double> deliveryZones;
    
    // آمار درآمد
    private double totalRevenue;
    private double averageRevenuePerRestaurant;
    private Map<String, Double> revenueByCategory;
    
    // آمار سفارشات
    private long totalOrders;
    private double averageOrdersPerRestaurant;
    private Map<String, Long> ordersByCategory;
    
    // آمار کیفیت
    private double averageCustomerRating;
    private double averageOrderAccuracy;
    private double averageServiceQuality;
    
    // Constructors
    public RestaurantAnalyticsDTO() {}
    
    // Getters and Setters
    public long getTotalRestaurants() {
        return totalRestaurants;
    }
    
    public void setTotalRestaurants(long totalRestaurants) {
        this.totalRestaurants = totalRestaurants;
    }
    
    public long getActiveRestaurants() {
        return activeRestaurants;
    }
    
    public void setActiveRestaurants(long activeRestaurants) {
        this.activeRestaurants = activeRestaurants;
    }
    
    public long getNewRestaurants() {
        return newRestaurants;
    }
    
    public void setNewRestaurants(long newRestaurants) {
        this.newRestaurants = newRestaurants;
    }
    
    public double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    public double getAverageRestaurantRating() {
        return averageRestaurantRating;
    }
    
    public void setAverageRestaurantRating(double averageRestaurantRating) {
        this.averageRestaurantRating = averageRestaurantRating;
    }
    
    public double getRestaurantSatisfaction() {
        return restaurantSatisfaction;
    }
    
    public void setRestaurantSatisfaction(double restaurantSatisfaction) {
        this.restaurantSatisfaction = restaurantSatisfaction;
    }
    
    public double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }
    
    public void setAverageDeliveryTime(double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }
    
    public List<Map<String, Object>> getTopPerformingRestaurants() {
        return topPerformingRestaurants;
    }
    
    public void setTopPerformingRestaurants(List<Map<String, Object>> topPerformingRestaurants) {
        this.topPerformingRestaurants = topPerformingRestaurants;
    }
    
    public List<Map<String, Object>> getRestaurantPerformance() {
        return restaurantPerformance;
    }
    
    public void setRestaurantPerformance(List<Map<String, Object>> restaurantPerformance) {
        this.restaurantPerformance = restaurantPerformance;
    }
    
    public List<Map<String, Object>> getRestaurantCategories() {
        return restaurantCategories;
    }
    
    public void setRestaurantCategories(List<Map<String, Object>> restaurantCategories) {
        this.restaurantCategories = restaurantCategories;
    }
    
    public Map<String, Double> getCategoryPerformance() {
        return categoryPerformance;
    }
    
    public void setCategoryPerformance(Map<String, Double> categoryPerformance) {
        this.categoryPerformance = categoryPerformance;
    }
    
    public Map<String, Double> getCuisineDistribution() {
        return cuisineDistribution;
    }
    
    public void setCuisineDistribution(Map<String, Double> cuisineDistribution) {
        this.cuisineDistribution = cuisineDistribution;
    }
    
    public Map<String, Double> getGeographicDistribution() {
        return geographicDistribution;
    }
    
    public void setGeographicDistribution(Map<String, Double> geographicDistribution) {
        this.geographicDistribution = geographicDistribution;
    }
    
    public Map<String, Double> getDeliveryZones() {
        return deliveryZones;
    }
    
    public void setDeliveryZones(Map<String, Double> deliveryZones) {
        this.deliveryZones = deliveryZones;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public double getAverageRevenuePerRestaurant() {
        return averageRevenuePerRestaurant;
    }
    
    public void setAverageRevenuePerRestaurant(double averageRevenuePerRestaurant) {
        this.averageRevenuePerRestaurant = averageRevenuePerRestaurant;
    }
    
    public Map<String, Double> getRevenueByCategory() {
        return revenueByCategory;
    }
    
    public void setRevenueByCategory(Map<String, Double> revenueByCategory) {
        this.revenueByCategory = revenueByCategory;
    }
    
    public long getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public double getAverageOrdersPerRestaurant() {
        return averageOrdersPerRestaurant;
    }
    
    public void setAverageOrdersPerRestaurant(double averageOrdersPerRestaurant) {
        this.averageOrdersPerRestaurant = averageOrdersPerRestaurant;
    }
    
    public Map<String, Long> getOrdersByCategory() {
        return ordersByCategory;
    }
    
    public void setOrdersByCategory(Map<String, Long> ordersByCategory) {
        this.ordersByCategory = ordersByCategory;
    }
    
    public double getAverageCustomerRating() {
        return averageCustomerRating;
    }
    
    public void setAverageCustomerRating(double averageCustomerRating) {
        this.averageCustomerRating = averageCustomerRating;
    }
    
    public double getAverageOrderAccuracy() {
        return averageOrderAccuracy;
    }
    
    public void setAverageOrderAccuracy(double averageOrderAccuracy) {
        this.averageOrderAccuracy = averageOrderAccuracy;
    }
    
    public double getAverageServiceQuality() {
        return averageServiceQuality;
    }
    
    public void setAverageServiceQuality(double averageServiceQuality) {
        this.averageServiceQuality = averageServiceQuality;
    }
} 
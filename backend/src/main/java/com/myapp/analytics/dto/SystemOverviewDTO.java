package com.myapp.analytics.dto;

import java.util.Map;

/**
 * DTO برای آمار کلی سیستم
 */
public class SystemOverviewDTO {
    
    // آمار کلی
    private long totalUsers;
    private long totalRestaurants;
    private long totalOrders;
    private double totalRevenue;
    
    // آمار امروز
    private long todayOrders;
    private double todayRevenue;
    private long todayActiveUsers;
    
    // آمار هفته
    private long weeklyOrders;
    private double weeklyRevenue;
    private double weeklyGrowth;
    
    // آمار ماه
    private long monthlyOrders;
    private double monthlyRevenue;
    private double monthlyGrowth;
    
    // آمار عملکرد
    private double averageOrderValue;
    private double orderCompletionRate;
    private double customerSatisfaction;
    
    // آمار اضافی
    private Map<String, Object> additionalMetrics;
    
    // فیلدهای جدید مورد نیاز
    private int activeUsers;
    private int newUsers;
    private int completedOrders;
    private int pendingOrders;
    private int activeRestaurants;
    private double averageDeliveryTime;
    private double deliverySuccessRate;
    
    // Constructors
    public SystemOverviewDTO() {}
    
    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public long getTotalRestaurants() {
        return totalRestaurants;
    }
    
    public void setTotalRestaurants(long totalRestaurants) {
        this.totalRestaurants = totalRestaurants;
    }
    
    public long getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public long getTodayOrders() {
        return todayOrders;
    }
    
    public void setTodayOrders(long todayOrders) {
        this.todayOrders = todayOrders;
    }
    
    public double getTodayRevenue() {
        return todayRevenue;
    }
    
    public void setTodayRevenue(double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }
    
    public long getTodayActiveUsers() {
        return todayActiveUsers;
    }
    
    public void setTodayActiveUsers(long todayActiveUsers) {
        this.todayActiveUsers = todayActiveUsers;
    }
    
    public long getWeeklyOrders() {
        return weeklyOrders;
    }
    
    public void setWeeklyOrders(long weeklyOrders) {
        this.weeklyOrders = weeklyOrders;
    }
    
    public double getWeeklyRevenue() {
        return weeklyRevenue;
    }
    
    public void setWeeklyRevenue(double weeklyRevenue) {
        this.weeklyRevenue = weeklyRevenue;
    }
    
    public double getWeeklyGrowth() {
        return weeklyGrowth;
    }
    
    public void setWeeklyGrowth(double weeklyGrowth) {
        this.weeklyGrowth = weeklyGrowth;
    }
    
    public long getMonthlyOrders() {
        return monthlyOrders;
    }
    
    public void setMonthlyOrders(long monthlyOrders) {
        this.monthlyOrders = monthlyOrders;
    }
    
    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }
    
    public void setMonthlyRevenue(double monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }
    
    public double getMonthlyGrowth() {
        return monthlyGrowth;
    }
    
    public void setMonthlyGrowth(double monthlyGrowth) {
        this.monthlyGrowth = monthlyGrowth;
    }
    
    public double getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public double getOrderCompletionRate() {
        return orderCompletionRate;
    }
    
    public void setOrderCompletionRate(double orderCompletionRate) {
        this.orderCompletionRate = orderCompletionRate;
    }
    
    public double getCustomerSatisfaction() {
        return customerSatisfaction;
    }
    
    public void setCustomerSatisfaction(double customerSatisfaction) {
        this.customerSatisfaction = customerSatisfaction;
    }
    
    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }
    
    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }
    
    // متدهای جدید مورد نیاز
    public int getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public int getNewUsers() {
        return newUsers;
    }
    
    public void setNewUsers(int newUsers) {
        this.newUsers = newUsers;
    }
    
    public int getCompletedOrders() {
        return completedOrders;
    }
    
    public void setCompletedOrders(int completedOrders) {
        this.completedOrders = completedOrders;
    }
    
    public int getPendingOrders() {
        return pendingOrders;
    }
    
    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }
    
    public int getActiveRestaurants() {
        return activeRestaurants;
    }
    
    public void setActiveRestaurants(int activeRestaurants) {
        this.activeRestaurants = activeRestaurants;
    }
    
    public double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }
    
    public void setAverageDeliveryTime(double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }
    
    public double getDeliverySuccessRate() {
        return deliverySuccessRate;
    }
    
    public void setDeliverySuccessRate(double deliverySuccessRate) {
        this.deliverySuccessRate = deliverySuccessRate;
    }
} 
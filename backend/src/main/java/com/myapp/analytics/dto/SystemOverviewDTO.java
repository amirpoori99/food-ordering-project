package com.myapp.analytics.dto;

public class SystemOverviewDTO {
    private int totalUsers;
    private int totalRestaurants;
    private int totalOrders;
    private double totalRevenue;
    private int activeUsers;
    private int newUsers;
    private int completedOrders;
    private int pendingOrders;
    private int activeRestaurants;
    private double averageOrderValue;
    private double averageDeliveryTime;
    private double deliverySuccessRate;

    public SystemOverviewDTO() {}

    public SystemOverviewDTO(int totalUsers, int totalRestaurants, int totalOrders, double totalRevenue,
                           int activeUsers, int newUsers, int completedOrders, int pendingOrders,
                           int activeRestaurants, double averageOrderValue, double averageDeliveryTime,
                           double deliverySuccessRate) {
        this.totalUsers = totalUsers;
        this.totalRestaurants = totalRestaurants;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.activeUsers = activeUsers;
        this.newUsers = newUsers;
        this.completedOrders = completedOrders;
        this.pendingOrders = pendingOrders;
        this.activeRestaurants = activeRestaurants;
        this.averageOrderValue = averageOrderValue;
        this.averageDeliveryTime = averageDeliveryTime;
        this.deliverySuccessRate = deliverySuccessRate;
    }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    
    public int getTotalRestaurants() { return totalRestaurants; }
    public void setTotalRestaurants(int totalRestaurants) { this.totalRestaurants = totalRestaurants; }
    
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
    
    public int getNewUsers() { return newUsers; }
    public void setNewUsers(int newUsers) { this.newUsers = newUsers; }
    
    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    
    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }
    
    public int getActiveRestaurants() { return activeRestaurants; }
    public void setActiveRestaurants(int activeRestaurants) { this.activeRestaurants = activeRestaurants; }
    
    public double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public double getAverageDeliveryTime() { return averageDeliveryTime; }
    public void setAverageDeliveryTime(double averageDeliveryTime) { this.averageDeliveryTime = averageDeliveryTime; }
    
    public double getDeliverySuccessRate() { return deliverySuccessRate; }
    public void setDeliverySuccessRate(double deliverySuccessRate) { this.deliverySuccessRate = deliverySuccessRate; }
} 
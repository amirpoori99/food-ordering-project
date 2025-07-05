package com.myapp.analytics.dto;

import java.util.Map;

/**
 * DTO برای آمار کاربران
 */
public class UserAnalyticsDTO {
    
    // آمار کلی کاربران
    private long totalUsers;
    private long activeUsers;
    private long newUsers;
    private double retentionRate;
    
    // آمار رفتار کاربران
    private double averageSessionDuration;
    private double averageOrdersPerUser;
    private double userEngagement;
    
    // آمار جمعیت‌شناسی
    private Map<String, Double> ageDistribution;
    private Map<String, Double> genderDistribution;
    private Map<String, Double> locationDistribution;
    
    // آمار وفاداری
    private Map<String, Double> loyaltySegments;
    private double churnRate;
    private double lifetimeValue;
    
    // Constructors
    public UserAnalyticsDTO() {}
    
    // Getters and Setters
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    
    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    
    public long getNewUsers() { return newUsers; }
    public void setNewUsers(long newUsers) { this.newUsers = newUsers; }
    
    public double getRetentionRate() { return retentionRate; }
    public void setRetentionRate(double retentionRate) { this.retentionRate = retentionRate; }
    
    public double getAverageSessionDuration() { return averageSessionDuration; }
    public void setAverageSessionDuration(double averageSessionDuration) { this.averageSessionDuration = averageSessionDuration; }
    
    public double getAverageOrdersPerUser() { return averageOrdersPerUser; }
    public void setAverageOrdersPerUser(double averageOrdersPerUser) { this.averageOrdersPerUser = averageOrdersPerUser; }
    
    public double getUserEngagement() { return userEngagement; }
    public void setUserEngagement(double userEngagement) { this.userEngagement = userEngagement; }
    
    public Map<String, Double> getAgeDistribution() { return ageDistribution; }
    public void setAgeDistribution(Map<String, Double> ageDistribution) { this.ageDistribution = ageDistribution; }
    
    public Map<String, Double> getGenderDistribution() { return genderDistribution; }
    public void setGenderDistribution(Map<String, Double> genderDistribution) { this.genderDistribution = genderDistribution; }
    
    public Map<String, Double> getLocationDistribution() { return locationDistribution; }
    public void setLocationDistribution(Map<String, Double> locationDistribution) { this.locationDistribution = locationDistribution; }
    
    public Map<String, Double> getLoyaltySegments() { return loyaltySegments; }
    public void setLoyaltySegments(Map<String, Double> loyaltySegments) { this.loyaltySegments = loyaltySegments; }
    
    public double getChurnRate() { return churnRate; }
    public void setChurnRate(double churnRate) { this.churnRate = churnRate; }
    
    public double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(double lifetimeValue) { this.lifetimeValue = lifetimeValue; }
} 
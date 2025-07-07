package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class UserAnalyticsDTO {
    private int totalUsers;
    private int activeUsers;
    private int newUsers;
    private double userRetention;
    private List<Map<String, Object>> userSegments;
    private List<Map<String, Object>> userBehavior;

    public UserAnalyticsDTO() {}

    public UserAnalyticsDTO(int totalUsers, int activeUsers, int newUsers, double userRetention,
                          List<Map<String, Object>> userSegments, List<Map<String, Object>> userBehavior) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.newUsers = newUsers;
        this.userRetention = userRetention;
        this.userSegments = userSegments;
        this.userBehavior = userBehavior;
    }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    
    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
    
    public int getNewUsers() { return newUsers; }
    public void setNewUsers(int newUsers) { this.newUsers = newUsers; }
    
    public double getUserRetention() { return userRetention; }
    public void setUserRetention(double userRetention) { this.userRetention = userRetention; }
    
    public List<Map<String, Object>> getUserSegments() { return userSegments; }
    public void setUserSegments(List<Map<String, Object>> userSegments) { this.userSegments = userSegments; }
    
    public List<Map<String, Object>> getUserBehavior() { return userBehavior; }
    public void setUserBehavior(List<Map<String, Object>> userBehavior) { this.userBehavior = userBehavior; }
} 
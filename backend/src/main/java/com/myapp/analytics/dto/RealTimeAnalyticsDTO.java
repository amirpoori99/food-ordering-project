package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class RealTimeAnalyticsDTO {
    private long currentUsers;
    private long currentOrders;
    private double currentRevenue;
    private Map<String, Object> systemStatus;
    private Map<String, Object> activeSessions;
    private Map<String, Object> performanceMetrics;
    private int activeUsers;
    private List<Map<String, Object>> performanceMetricsList;

    public RealTimeAnalyticsDTO() {}

    public long getCurrentUsers() { return currentUsers; }
    public void setCurrentUsers(long currentUsers) { this.currentUsers = currentUsers; }

    public long getCurrentOrders() { return currentOrders; }
    public void setCurrentOrders(long currentOrders) { this.currentOrders = currentOrders; }

    public double getCurrentRevenue() { return currentRevenue; }
    public void setCurrentRevenue(double currentRevenue) { this.currentRevenue = currentRevenue; }

    public Map<String, Object> getSystemStatus() { return systemStatus; }
    public void setSystemStatus(Map<String, Object> systemStatus) { this.systemStatus = systemStatus; }

    public Map<String, Object> getActiveSessions() { return activeSessions; }
    public void setActiveSessions(Map<String, Object> activeSessions) { this.activeSessions = activeSessions; }

    public Map<String, Object> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(Map<String, Object> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    
    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
    
    public List<Map<String, Object>> getPerformanceMetricsList() { return performanceMetricsList; }
    public void setPerformanceMetricsList(List<Map<String, Object>> performanceMetrics) { this.performanceMetricsList = performanceMetrics; }
} 
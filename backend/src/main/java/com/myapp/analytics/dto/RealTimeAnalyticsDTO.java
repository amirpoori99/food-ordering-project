package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class RealTimeAnalyticsDTO {
    private int activeUsers;
    private int currentOrders;
    private double currentRevenue;
    private Map<String, Object> systemStatus;
    private Map<Object, Object> performanceMetrics;
    private List<Map<String, Object>> realTimeMetrics;
    private List<Map<String, Object>> liveData;
    private List<Map<String, Object>> instantInsights;
    private List<Map<String, Object>> performanceMetricsList;

    public RealTimeAnalyticsDTO() {}

    public RealTimeAnalyticsDTO(int activeUsers, int currentOrders, double currentRevenue,
                              Map<String, Object> systemStatus, Map<Object, Object> performanceMetrics,
                              List<Map<String, Object>> realTimeMetrics,
                              List<Map<String, Object>> liveData,
                              List<Map<String, Object>> instantInsights,
                              List<Map<String, Object>> performanceMetricsList) {
        this.activeUsers = activeUsers;
        this.currentOrders = currentOrders;
        this.currentRevenue = currentRevenue;
        this.systemStatus = systemStatus;
        this.performanceMetrics = performanceMetrics;
        this.realTimeMetrics = realTimeMetrics;
        this.liveData = liveData;
        this.instantInsights = instantInsights;
        this.performanceMetricsList = performanceMetricsList;
    }

    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
    
    public int getCurrentOrders() { return currentOrders; }
    public void setCurrentOrders(int currentOrders) { this.currentOrders = currentOrders; }
    
    public double getCurrentRevenue() { return currentRevenue; }
    public void setCurrentRevenue(double currentRevenue) { this.currentRevenue = currentRevenue; }
    
    public Map<String, Object> getSystemStatus() { return systemStatus; }
    public void setSystemStatus(Map<String, Object> systemStatus) { this.systemStatus = systemStatus; }
    
    public Map<Object, Object> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(Map<Object, Object> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    
    public List<Map<String, Object>> getRealTimeMetrics() { return realTimeMetrics; }
    public void setRealTimeMetrics(List<Map<String, Object>> realTimeMetrics) { this.realTimeMetrics = realTimeMetrics; }
    
    public List<Map<String, Object>> getLiveData() { return liveData; }
    public void setLiveData(List<Map<String, Object>> liveData) { this.liveData = liveData; }
    
    public List<Map<String, Object>> getInstantInsights() { return instantInsights; }
    public void setInstantInsights(List<Map<String, Object>> instantInsights) { this.instantInsights = instantInsights; }
    
    public List<Map<String, Object>> getPerformanceMetricsList() { return performanceMetricsList; }
    public void setPerformanceMetricsList(List<Map<String, Object>> performanceMetricsList) { this.performanceMetricsList = performanceMetricsList; }
} 
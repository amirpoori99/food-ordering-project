package com.myapp.analytics.dto;

import java.util.Map;

public class RealTimeAnalyticsDTO {
    private long currentUsers;
    private long currentOrders;
    private double currentRevenue;
    private Map<String, Object> systemStatus;
    private Map<String, Object> activeSessions;

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
} 
package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class PerformanceAnalyticsDTO {
    private double systemUptime;
    private double averageResponseTime;
    private double errorRate;
    private Map<String, Object> appPerformance;
    private Map<String, Object> databasePerformance;
    private Map<String, Object> cachePerformance;
    private Map<String, Object> trafficAnalysis;
    private Map<String, Object> peakHours;
    
    // فیلدهای جدید مورد نیاز
    private List<Map<String, Object>> systemPerformance;
    private double responseTime;
    private double uptime;

    public PerformanceAnalyticsDTO() {}

    public double getSystemUptime() { return systemUptime; }
    public void setSystemUptime(double systemUptime) { this.systemUptime = systemUptime; }

    public double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

    public double getErrorRate() { return errorRate; }
    public void setErrorRate(double errorRate) { this.errorRate = errorRate; }

    public Map<String, Object> getAppPerformance() { return appPerformance; }
    public void setAppPerformance(Map<String, Object> appPerformance) { this.appPerformance = appPerformance; }

    public Map<String, Object> getDatabasePerformance() { return databasePerformance; }
    public void setDatabasePerformance(Map<String, Object> databasePerformance) { this.databasePerformance = databasePerformance; }

    public Map<String, Object> getCachePerformance() { return cachePerformance; }
    public void setCachePerformance(Map<String, Object> cachePerformance) { this.cachePerformance = cachePerformance; }

    public Map<String, Object> getTrafficAnalysis() { return trafficAnalysis; }
    public void setTrafficAnalysis(Map<String, Object> trafficAnalysis) { this.trafficAnalysis = trafficAnalysis; }

    public Map<String, Object> getPeakHours() { return peakHours; }
    public void setPeakHours(Map<String, Object> peakHours) { this.peakHours = peakHours; }
    
    // متدهای جدید مورد نیاز
    public List<Map<String, Object>> getSystemPerformance() { return systemPerformance; }
    public void setSystemPerformance(List<Map<String, Object>> systemPerformance) { this.systemPerformance = systemPerformance; }
    
    public double getResponseTime() { return responseTime; }
    public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
    
    public double getUptime() { return uptime; }
    public void setUptime(double uptime) { this.uptime = uptime; }
} 
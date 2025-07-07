package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class PerformanceAnalyticsDTO {
    private double averageResponseTime;
    private double maxResponseTime;
    private double minResponseTime;
    private List<Map<String, Object>> systemPerformance;
    private double responseTime;
    private double errorRate;
    private double uptime;

    public PerformanceAnalyticsDTO() {}

    public PerformanceAnalyticsDTO(double averageResponseTime, double maxResponseTime, double minResponseTime,
                                 List<Map<String, Object>> systemPerformance, double responseTime,
                                 double errorRate, double uptime) {
        this.averageResponseTime = averageResponseTime;
        this.maxResponseTime = maxResponseTime;
        this.minResponseTime = minResponseTime;
        this.systemPerformance = systemPerformance;
        this.responseTime = responseTime;
        this.errorRate = errorRate;
        this.uptime = uptime;
    }

    public double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
    
    public double getMaxResponseTime() { return maxResponseTime; }
    public void setMaxResponseTime(double maxResponseTime) { this.maxResponseTime = maxResponseTime; }
    
    public double getMinResponseTime() { return minResponseTime; }
    public void setMinResponseTime(double minResponseTime) { this.minResponseTime = minResponseTime; }
    
    public List<Map<String, Object>> getSystemPerformance() { return systemPerformance; }
    public void setSystemPerformance(List<Map<String, Object>> systemPerformance) { this.systemPerformance = systemPerformance; }
    
    public double getResponseTime() { return responseTime; }
    public void setResponseTime(double responseTime) { this.responseTime = responseTime; }
    
    public double getErrorRate() { return errorRate; }
    public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
    
    public double getUptime() { return uptime; }
    public void setUptime(double uptime) { this.uptime = uptime; }
} 
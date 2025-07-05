package com.myapp.analytics.dto;

import java.util.Map;

public class OperationalAnalyticsDTO {
    private double systemUptime;
    private double averageResponseTime;
    private double errorRate;
    private Map<String, Double> operationalMetrics;
    private Map<String, Double> operationalTrends;

    public OperationalAnalyticsDTO() {}

    public double getSystemUptime() { return systemUptime; }
    public void setSystemUptime(double systemUptime) { this.systemUptime = systemUptime; }

    public double getAverageResponseTime() { return averageResponseTime; }
    public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

    public double getErrorRate() { return errorRate; }
    public void setErrorRate(double errorRate) { this.errorRate = errorRate; }

    public Map<String, Double> getOperationalMetrics() { return operationalMetrics; }
    public void setOperationalMetrics(Map<String, Double> operationalMetrics) { this.operationalMetrics = operationalMetrics; }

    public Map<String, Double> getOperationalTrends() { return operationalTrends; }
    public void setOperationalTrends(Map<String, Double> operationalTrends) { this.operationalTrends = operationalTrends; }
} 
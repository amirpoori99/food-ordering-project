package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class OperationalAnalyticsDTO {
    private double systemUptime;
    private double averageResponseTime;
    private double errorRate;
    private Map<String, Double> operationalMetrics;
    private Map<String, Double> operationalTrends;
    private Map<String, Double> efficiencyMetrics;
    private Map<String, Double> resourceUtilization;
    private Map<String, Double> processOptimization;
    private List<Map<String, Object>> operationalEfficiency;
    private List<Map<String, Object>> resourceUtilizationList;
    private List<Map<String, Object>> processOptimizationList;

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

    public Map<String, Double> getEfficiencyMetrics() { return efficiencyMetrics; }
    public void setEfficiencyMetrics(Map<String, Double> efficiencyMetrics) { this.efficiencyMetrics = efficiencyMetrics; }

    public Map<String, Double> getResourceUtilization() { return resourceUtilization; }
    public void setResourceUtilization(Map<String, Double> resourceUtilization) { this.resourceUtilization = resourceUtilization; }

    public Map<String, Double> getProcessOptimization() { return processOptimization; }
    public void setProcessOptimization(Map<String, Double> processOptimization) { this.processOptimization = processOptimization; }

    public List<Map<String, Object>> getOperationalEfficiency() { return operationalEfficiency; }
    public void setOperationalEfficiency(List<Map<String, Object>> operationalEfficiency) { this.operationalEfficiency = operationalEfficiency; }

    public List<Map<String, Object>> getResourceUtilizationList() { return resourceUtilizationList; }
    public void setResourceUtilizationList(List<Map<String, Object>> resourceUtilization) { this.resourceUtilizationList = resourceUtilization; }

    public List<Map<String, Object>> getProcessOptimizationList() { return processOptimizationList; }
    public void setProcessOptimizationList(List<Map<String, Object>> processOptimization) { this.processOptimizationList = processOptimization; }
} 
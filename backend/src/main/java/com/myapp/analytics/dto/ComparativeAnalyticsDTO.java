package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class ComparativeAnalyticsDTO {
    private String metric;
    private double currentValue;
    private double previousValue;
    private List<Map<String, Object>> periodComparison;
    private List<Map<String, Object>> benchmarkAnalysis;

    public ComparativeAnalyticsDTO() {}

    public ComparativeAnalyticsDTO(String metric, double currentValue, double previousValue,
                                 List<Map<String, Object>> periodComparison,
                                 List<Map<String, Object>> benchmarkAnalysis) {
        this.metric = metric;
        this.currentValue = currentValue;
        this.previousValue = previousValue;
        this.periodComparison = periodComparison;
        this.benchmarkAnalysis = benchmarkAnalysis;
    }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }
    
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    
    public double getPreviousValue() { return previousValue; }
    public void setPreviousValue(double previousValue) { this.previousValue = previousValue; }
    
    public List<Map<String, Object>> getPeriodComparison() { return periodComparison; }
    public void setPeriodComparison(List<Map<String, Object>> periodComparison) { this.periodComparison = periodComparison; }
    
    public List<Map<String, Object>> getBenchmarkAnalysis() { return benchmarkAnalysis; }
    public void setBenchmarkAnalysis(List<Map<String, Object>> benchmarkAnalysis) { this.benchmarkAnalysis = benchmarkAnalysis; }
} 
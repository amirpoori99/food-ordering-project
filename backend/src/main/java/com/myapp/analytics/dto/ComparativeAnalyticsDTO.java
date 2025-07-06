package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class ComparativeAnalyticsDTO {
    private Map<String, Double> period1Data;
    private Map<String, Double> period2Data;
    private Map<String, Double> differences;
    private Map<String, Double> percentageChanges;
    private Map<String, Double> periodComparison;
    private Map<String, Double> benchmarkAnalysis;
    private List<Map<String, Object>> periodComparisonList;
    private List<Map<String, Object>> benchmarkAnalysisList;

    public ComparativeAnalyticsDTO() {}

    public Map<String, Double> getPeriod1Data() { return period1Data; }
    public void setPeriod1Data(Map<String, Double> period1Data) { this.period1Data = period1Data; }

    public Map<String, Double> getPeriod2Data() { return period2Data; }
    public void setPeriod2Data(Map<String, Double> period2Data) { this.period2Data = period2Data; }

    public Map<String, Double> getDifferences() { return differences; }
    public void setDifferences(Map<String, Double> differences) { this.differences = differences; }

    public Map<String, Double> getPercentageChanges() { return percentageChanges; }
    public void setPercentageChanges(Map<String, Double> percentageChanges) { this.percentageChanges = percentageChanges; }

    public Map<String, Double> getPeriodComparison() { return periodComparison; }
    public void setPeriodComparison(Map<String, Double> periodComparison) { this.periodComparison = periodComparison; }

    public Map<String, Double> getBenchmarkAnalysis() { return benchmarkAnalysis; }
    public void setBenchmarkAnalysis(Map<String, Double> benchmarkAnalysis) { this.benchmarkAnalysis = benchmarkAnalysis; }
    
    public List<Map<String, Object>> getPeriodComparisonList() { return periodComparisonList; }
    public void setPeriodComparison(List<Map<String, Object>> periodComparison) { this.periodComparisonList = periodComparison; }
    
    public List<Map<String, Object>> getBenchmarkAnalysisList() { return benchmarkAnalysisList; }
    public void setBenchmarkAnalysis(List<Map<String, Object>> benchmarkAnalysis) { this.benchmarkAnalysisList = benchmarkAnalysis; }
} 
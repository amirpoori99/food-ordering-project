package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class OperationalAnalyticsDTO {
    private String operation;
    private double value;
    private Map<String, Double> efficiencyMetrics;
    private Map<String, Double> resourceUtilization;
    private Map<String, Double> processOptimization;
    private List<Map<String, Object>> operationalEfficiency;
    private List<Map<String, Object>> resourceUtilizationList;
    private List<Map<String, Object>> processOptimizationList;

    public OperationalAnalyticsDTO() {}

    public OperationalAnalyticsDTO(String operation, double value, Map<String, Double> efficiencyMetrics,
                                 Map<String, Double> resourceUtilization, Map<String, Double> processOptimization,
                                 List<Map<String, Object>> operationalEfficiency,
                                 List<Map<String, Object>> resourceUtilizationList,
                                 List<Map<String, Object>> processOptimizationList) {
        this.operation = operation;
        this.value = value;
        this.efficiencyMetrics = efficiencyMetrics;
        this.resourceUtilization = resourceUtilization;
        this.processOptimization = processOptimization;
        this.operationalEfficiency = operationalEfficiency;
        this.resourceUtilizationList = resourceUtilizationList;
        this.processOptimizationList = processOptimizationList;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public Map<String, Double> getEfficiencyMetrics() { return efficiencyMetrics; }
    public void setEfficiencyMetrics(Map<String, Double> efficiencyMetrics) { this.efficiencyMetrics = efficiencyMetrics; }
    
    public Map<String, Double> getResourceUtilization() { return resourceUtilization; }
    public void setResourceUtilization(Map<String, Double> resourceUtilization) { this.resourceUtilization = resourceUtilization; }
    
    public Map<String, Double> getProcessOptimization() { return processOptimization; }
    public void setProcessOptimization(Map<String, Double> processOptimization) { this.processOptimization = processOptimization; }
    
    public List<Map<String, Object>> getOperationalEfficiency() { return operationalEfficiency; }
    public void setOperationalEfficiency(List<Map<String, Object>> operationalEfficiency) { this.operationalEfficiency = operationalEfficiency; }
    
    public List<Map<String, Object>> getResourceUtilizationList() { return resourceUtilizationList; }
    public void setResourceUtilizationList(List<Map<String, Object>> resourceUtilizationList) { this.resourceUtilizationList = resourceUtilizationList; }
    
    public List<Map<String, Object>> getProcessOptimizationList() { return processOptimizationList; }
    public void setProcessOptimizationList(List<Map<String, Object>> processOptimizationList) { this.processOptimizationList = processOptimizationList; }
} 
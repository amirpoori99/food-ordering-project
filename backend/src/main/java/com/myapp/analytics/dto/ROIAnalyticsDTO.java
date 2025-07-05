package com.myapp.analytics.dto;

import java.util.Map;

public class ROIAnalyticsDTO {
    private double totalInvestment;
    private double totalReturn;
    private double roiPercentage;
    private Map<String, Double> roiByCategory;
    private Map<String, Double> roiByPeriod;

    public ROIAnalyticsDTO() {}

    public double getTotalInvestment() { return totalInvestment; }
    public void setTotalInvestment(double totalInvestment) { this.totalInvestment = totalInvestment; }

    public double getTotalReturn() { return totalReturn; }
    public void setTotalReturn(double totalReturn) { this.totalReturn = totalReturn; }

    public double getRoiPercentage() { return roiPercentage; }
    public void setRoiPercentage(double roiPercentage) { this.roiPercentage = roiPercentage; }

    public Map<String, Double> getRoiByCategory() { return roiByCategory; }
    public void setRoiByCategory(Map<String, Double> roiByCategory) { this.roiByCategory = roiByCategory; }

    public Map<String, Double> getRoiByPeriod() { return roiByPeriod; }
    public void setRoiByPeriod(Map<String, Double> roiByPeriod) { this.roiByPeriod = roiByPeriod; }
} 
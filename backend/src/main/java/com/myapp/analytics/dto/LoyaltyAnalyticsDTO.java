package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class LoyaltyAnalyticsDTO {
    private Map<String, Double> loyaltySegments;
    private double churnRate;
    private double lifetimeValue;
    private Map<String, Double> loyaltyTrends;
    private Map<String, Double> loyaltyMetrics;
    private Map<String, Double> retentionMetrics;
    private Map<String, Double> loyaltyPrograms;
    private double customerLoyalty;
    private double retentionRate;
    private List<Map<String, Object>> loyaltyProgramsList;

    public LoyaltyAnalyticsDTO() {}

    public Map<String, Double> getLoyaltySegments() { return loyaltySegments; }
    public void setLoyaltySegments(Map<String, Double> loyaltySegments) { this.loyaltySegments = loyaltySegments; }

    public double getChurnRate() { return churnRate; }
    public void setChurnRate(double churnRate) { this.churnRate = churnRate; }

    public double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(double lifetimeValue) { this.lifetimeValue = lifetimeValue; }

    public Map<String, Double> getLoyaltyTrends() { return loyaltyTrends; }
    public void setLoyaltyTrends(Map<String, Double> loyaltyTrends) { this.loyaltyTrends = loyaltyTrends; }

    public Map<String, Double> getLoyaltyMetrics() { return loyaltyMetrics; }
    public void setLoyaltyMetrics(Map<String, Double> loyaltyMetrics) { this.loyaltyMetrics = loyaltyMetrics; }

    public Map<String, Double> getRetentionMetrics() { return retentionMetrics; }
    public void setRetentionMetrics(Map<String, Double> retentionMetrics) { this.retentionMetrics = retentionMetrics; }

    public Map<String, Double> getLoyaltyPrograms() { return loyaltyPrograms; }
    public void setLoyaltyPrograms(Map<String, Double> loyaltyPrograms) { this.loyaltyPrograms = loyaltyPrograms; }

    public double getCustomerLoyalty() { return customerLoyalty; }
    public void setCustomerLoyalty(double customerLoyalty) { this.customerLoyalty = customerLoyalty; }

    public double getRetentionRate() { return retentionRate; }
    public void setRetentionRate(double retentionRate) { this.retentionRate = retentionRate; }

    public List<Map<String, Object>> getLoyaltyProgramsList() { return loyaltyProgramsList; }
    public void setLoyaltyProgramsList(List<Map<String, Object>> loyaltyPrograms) { this.loyaltyProgramsList = loyaltyPrograms; }
} 
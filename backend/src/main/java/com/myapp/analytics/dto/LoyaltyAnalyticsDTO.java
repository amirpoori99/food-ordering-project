package com.myapp.analytics.dto;

import java.util.Map;

public class LoyaltyAnalyticsDTO {
    private Map<String, Double> loyaltySegments;
    private double churnRate;
    private double lifetimeValue;
    private Map<String, Double> loyaltyTrends;

    public LoyaltyAnalyticsDTO() {}

    public Map<String, Double> getLoyaltySegments() { return loyaltySegments; }
    public void setLoyaltySegments(Map<String, Double> loyaltySegments) { this.loyaltySegments = loyaltySegments; }

    public double getChurnRate() { return churnRate; }
    public void setChurnRate(double churnRate) { this.churnRate = churnRate; }

    public double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(double lifetimeValue) { this.lifetimeValue = lifetimeValue; }

    public Map<String, Double> getLoyaltyTrends() { return loyaltyTrends; }
    public void setLoyaltyTrends(Map<String, Double> loyaltyTrends) { this.loyaltyTrends = loyaltyTrends; }
} 
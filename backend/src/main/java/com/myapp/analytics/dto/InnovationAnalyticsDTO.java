package com.myapp.analytics.dto;

import java.util.Map;

public class InnovationAnalyticsDTO {
    private Map<String, Double> innovationMetrics;
    private Map<String, Double> innovationTrends;
    private Map<String, Double> rAndDInvestments;

    public InnovationAnalyticsDTO() {}

    public Map<String, Double> getInnovationMetrics() { return innovationMetrics; }
    public void setInnovationMetrics(Map<String, Double> innovationMetrics) { this.innovationMetrics = innovationMetrics; }

    public Map<String, Double> getInnovationTrends() { return innovationTrends; }
    public void setInnovationTrends(Map<String, Double> innovationTrends) { this.innovationTrends = innovationTrends; }

    public Map<String, Double> getRAndDInvestments() { return rAndDInvestments; }
    public void setRAndDInvestments(Map<String, Double> rAndDInvestments) { this.rAndDInvestments = rAndDInvestments; }
} 
package com.myapp.analytics.dto;

import java.util.Map;

public class CompetitiveAnalyticsDTO {
    private Map<String, Double> competitorMetrics;
    private Map<String, Double> marketShare;
    private Map<String, Double> competitiveTrends;

    public CompetitiveAnalyticsDTO() {}

    public Map<String, Double> getCompetitorMetrics() { return competitorMetrics; }
    public void setCompetitorMetrics(Map<String, Double> competitorMetrics) { this.competitorMetrics = competitorMetrics; }

    public Map<String, Double> getMarketShare() { return marketShare; }
    public void setMarketShare(Map<String, Double> marketShare) { this.marketShare = marketShare; }

    public Map<String, Double> getCompetitiveTrends() { return competitiveTrends; }
    public void setCompetitiveTrends(Map<String, Double> competitiveTrends) { this.competitiveTrends = competitiveTrends; }
} 
package com.myapp.analytics.dto;

import java.util.Map;

public class MarketAnalyticsDTO {
    private Map<String, Double> marketSize;
    private Map<String, Double> marketGrowth;
    private Map<String, Double> marketSegments;
    private Map<String, Double> marketTrends;

    public MarketAnalyticsDTO() {}

    public Map<String, Double> getMarketSize() { return marketSize; }
    public void setMarketSize(Map<String, Double> marketSize) { this.marketSize = marketSize; }

    public Map<String, Double> getMarketGrowth() { return marketGrowth; }
    public void setMarketGrowth(Map<String, Double> marketGrowth) { this.marketGrowth = marketGrowth; }

    public Map<String, Double> getMarketSegments() { return marketSegments; }
    public void setMarketSegments(Map<String, Double> marketSegments) { this.marketSegments = marketSegments; }

    public Map<String, Double> getMarketTrends() { return marketTrends; }
    public void setMarketTrends(Map<String, Double> marketTrends) { this.marketTrends = marketTrends; }
} 
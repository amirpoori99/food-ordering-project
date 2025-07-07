package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class MarketAnalyticsDTO {
    private int totalVendors;
    private double marketShare;
    private double growthRate;
    private List<Map<String, Object>> competitiveAnalysis;
    private List<Map<String, Object>> customerSegments;

    public MarketAnalyticsDTO() {}

    public MarketAnalyticsDTO(int totalVendors, double marketShare, double growthRate,
                            List<Map<String, Object>> competitiveAnalysis,
                            List<Map<String, Object>> customerSegments) {
        this.totalVendors = totalVendors;
        this.marketShare = marketShare;
        this.growthRate = growthRate;
        this.competitiveAnalysis = competitiveAnalysis;
        this.customerSegments = customerSegments;
    }

    public int getTotalVendors() { return totalVendors; }
    public void setTotalVendors(int totalVendors) { this.totalVendors = totalVendors; }
    
    public double getMarketShare() { return marketShare; }
    public void setMarketShare(double marketShare) { this.marketShare = marketShare; }
    
    public double getGrowthRate() { return growthRate; }
    public void setGrowthRate(double growthRate) { this.growthRate = growthRate; }
    
    public List<Map<String, Object>> getCompetitiveAnalysis() { return competitiveAnalysis; }
    public void setCompetitiveAnalysis(List<Map<String, Object>> competitiveAnalysis) { this.competitiveAnalysis = competitiveAnalysis; }
    
    public List<Map<String, Object>> getCustomerSegments() { return customerSegments; }
    public void setCustomerSegments(List<Map<String, Object>> customerSegments) { this.customerSegments = customerSegments; }
} 
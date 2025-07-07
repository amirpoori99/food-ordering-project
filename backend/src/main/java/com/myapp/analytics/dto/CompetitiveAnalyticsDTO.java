package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class CompetitiveAnalyticsDTO {
    private String competitor;
    private double marketShare;
    private double performance;
    private List<Map<String, Object>> competitivePosition;
    private List<Map<String, Object>> marketComparison;
    private List<Map<String, Object>> competitiveAdvantages;

    public CompetitiveAnalyticsDTO() {}

    public CompetitiveAnalyticsDTO(String competitor, double marketShare, double performance,
                                 List<Map<String, Object>> competitivePosition,
                                 List<Map<String, Object>> marketComparison,
                                 List<Map<String, Object>> competitiveAdvantages) {
        this.competitor = competitor;
        this.marketShare = marketShare;
        this.performance = performance;
        this.competitivePosition = competitivePosition;
        this.marketComparison = marketComparison;
        this.competitiveAdvantages = competitiveAdvantages;
    }

    public String getCompetitor() { return competitor; }
    public void setCompetitor(String competitor) { this.competitor = competitor; }
    
    public double getMarketShare() { return marketShare; }
    public void setMarketShare(double marketShare) { this.marketShare = marketShare; }
    
    public double getPerformance() { return performance; }
    public void setPerformance(double performance) { this.performance = performance; }
    
    public List<Map<String, Object>> getCompetitivePosition() { return competitivePosition; }
    public void setCompetitivePosition(List<Map<String, Object>> competitivePosition) { this.competitivePosition = competitivePosition; }
    
    public List<Map<String, Object>> getMarketComparison() { return marketComparison; }
    public void setMarketComparison(List<Map<String, Object>> marketComparison) { this.marketComparison = marketComparison; }
    
    public List<Map<String, Object>> getCompetitiveAdvantages() { return competitiveAdvantages; }
    public void setCompetitiveAdvantages(List<Map<String, Object>> competitiveAdvantages) { this.competitiveAdvantages = competitiveAdvantages; }
} 
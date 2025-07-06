package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class CompetitiveAnalyticsDTO {
    private Map<String, Double> competitorMetrics;
    private Map<String, Double> marketShare;
    private Map<String, Double> competitiveTrends;
    private Map<String, Double> competitiveMetrics;
    private Map<String, Double> marketComparison;
    private Map<String, Double> competitiveAdvantages;
    private List<Map<String, Object>> competitivePosition;
    private List<Map<String, Object>> marketComparisonList;
    private List<Map<String, Object>> competitiveAdvantagesList;

    public CompetitiveAnalyticsDTO() {}

    public Map<String, Double> getCompetitorMetrics() { return competitorMetrics; }
    public void setCompetitorMetrics(Map<String, Double> competitorMetrics) { this.competitorMetrics = competitorMetrics; }

    public Map<String, Double> getMarketShare() { return marketShare; }
    public void setMarketShare(Map<String, Double> marketShare) { this.marketShare = marketShare; }

    public Map<String, Double> getCompetitiveTrends() { return competitiveTrends; }
    public void setCompetitiveTrends(Map<String, Double> competitiveTrends) { this.competitiveTrends = competitiveTrends; }

    public Map<String, Double> getCompetitiveMetrics() { return competitiveMetrics; }
    public void setCompetitiveMetrics(Map<String, Double> competitiveMetrics) { this.competitiveMetrics = competitiveMetrics; }

    public Map<String, Double> getMarketComparison() { return marketComparison; }
    public void setMarketComparison(Map<String, Double> marketComparison) { this.marketComparison = marketComparison; }

    public Map<String, Double> getCompetitiveAdvantages() { return competitiveAdvantages; }
    public void setCompetitiveAdvantages(Map<String, Double> competitiveAdvantages) { this.competitiveAdvantages = competitiveAdvantages; }
    
    public List<Map<String, Object>> getCompetitivePosition() { return competitivePosition; }
    public void setCompetitivePosition(List<Map<String, Object>> competitivePosition) { this.competitivePosition = competitivePosition; }
    
    public List<Map<String, Object>> getMarketComparisonList() { return marketComparisonList; }
    public void setMarketComparison(List<Map<String, Object>> marketComparison) { this.marketComparisonList = marketComparison; }
    
    public List<Map<String, Object>> getCompetitiveAdvantagesList() { return competitiveAdvantagesList; }
    public void setCompetitiveAdvantages(List<Map<String, Object>> competitiveAdvantages) { this.competitiveAdvantagesList = competitiveAdvantages; }
} 
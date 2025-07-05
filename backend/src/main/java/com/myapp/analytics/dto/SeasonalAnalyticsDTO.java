package com.myapp.analytics.dto;

import java.util.Map;

public class SeasonalAnalyticsDTO {
    private Map<String, Double> springData;
    private Map<String, Double> summerData;
    private Map<String, Double> autumnData;
    private Map<String, Double> winterData;
    private Map<String, Double> seasonalPatterns;

    public SeasonalAnalyticsDTO() {}

    public Map<String, Double> getSpringData() { return springData; }
    public void setSpringData(Map<String, Double> springData) { this.springData = springData; }

    public Map<String, Double> getSummerData() { return summerData; }
    public void setSummerData(Map<String, Double> summerData) { this.summerData = summerData; }

    public Map<String, Double> getAutumnData() { return autumnData; }
    public void setAutumnData(Map<String, Double> autumnData) { this.autumnData = autumnData; }

    public Map<String, Double> getWinterData() { return winterData; }
    public void setWinterData(Map<String, Double> winterData) { this.winterData = winterData; }

    public Map<String, Double> getSeasonalPatterns() { return seasonalPatterns; }
    public void setSeasonalPatterns(Map<String, Double> seasonalPatterns) { this.seasonalPatterns = seasonalPatterns; }
} 
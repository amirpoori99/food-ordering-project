package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class SeasonalAnalyticsDTO {
    private Map<String, Double> springData;
    private Map<String, Double> summerData;
    private Map<String, Double> autumnData;
    private Map<String, Double> winterData;
    private Map<String, Double> seasonalPatterns;
    private List<Map<String, Object>> holidayImpact;
    private List<Map<String, Object>> weatherImpact;

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
    
    public List<Map<String, Object>> getHolidayImpact() { return holidayImpact; }
    public void setHolidayImpact(List<Map<String, Object>> holidayImpact) { this.holidayImpact = holidayImpact; }
    
    public List<Map<String, Object>> getWeatherImpact() { return weatherImpact; }
    public void setWeatherImpact(List<Map<String, Object>> weatherImpact) { this.weatherImpact = weatherImpact; }
} 
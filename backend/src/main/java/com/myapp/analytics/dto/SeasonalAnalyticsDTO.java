package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class SeasonalAnalyticsDTO {
    private String season;
    private double value;
    private Map<String, Double> seasonalPatterns;
    private List<Map<String, Object>> holidayImpact;
    private List<Map<String, Object>> weatherImpact;

    public SeasonalAnalyticsDTO() {}

    public SeasonalAnalyticsDTO(String season, double value, Map<String, Double> seasonalPatterns,
                              List<Map<String, Object>> holidayImpact, List<Map<String, Object>> weatherImpact) {
        this.season = season;
        this.value = value;
        this.seasonalPatterns = seasonalPatterns;
        this.holidayImpact = holidayImpact;
        this.weatherImpact = weatherImpact;
    }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public Map<String, Double> getSeasonalPatterns() { return seasonalPatterns; }
    public void setSeasonalPatterns(Map<String, Double> seasonalPatterns) { this.seasonalPatterns = seasonalPatterns; }
    
    public List<Map<String, Object>> getHolidayImpact() { return holidayImpact; }
    public void setHolidayImpact(List<Map<String, Object>> holidayImpact) { this.holidayImpact = holidayImpact; }
    
    public List<Map<String, Object>> getWeatherImpact() { return weatherImpact; }
    public void setWeatherImpact(List<Map<String, Object>> weatherImpact) { this.weatherImpact = weatherImpact; }
} 
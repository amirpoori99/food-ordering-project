package com.myapp.analytics.dto;

import java.util.Map;

public class TrendAnalyticsDTO {
    private Map<String, Double> trendData;
    private String trendDirection;
    private double trendStrength;
    private Map<String, Double> forecast;
    private Map<String, Double> seasonality;

    public TrendAnalyticsDTO() {}

    public Map<String, Double> getTrendData() { return trendData; }
    public void setTrendData(Map<String, Double> trendData) { this.trendData = trendData; }

    public String getTrendDirection() { return trendDirection; }
    public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

    public double getTrendStrength() { return trendStrength; }
    public void setTrendStrength(double trendStrength) { this.trendStrength = trendStrength; }

    public Map<String, Double> getForecast() { return forecast; }
    public void setForecast(Map<String, Double> forecast) { this.forecast = forecast; }

    public Map<String, Double> getSeasonality() { return seasonality; }
    public void setSeasonality(Map<String, Double> seasonality) { this.seasonality = seasonality; }
} 
package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class TrendAnalyticsDTO {
    private Map<String, Double> trendData;
    private String trendDirection;
    private double trendStrength;
    private Map<String, Double> forecast;
    private Map<String, Double> seasonality;
    
    // فیلدهای جدید مورد نیاز
    private List<Map<String, Object>> salesTrends;
    private List<Map<String, Object>> userTrends;
    private List<Map<String, Object>> productTrends;
    private List<Map<String, Object>> technologyTrends;

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
    
    // متدهای جدید مورد نیاز
    public List<Map<String, Object>> getSalesTrends() { return salesTrends; }
    public void setSalesTrends(List<Map<String, Object>> salesTrends) { this.salesTrends = salesTrends; }
    
    public List<Map<String, Object>> getUserTrends() { return userTrends; }
    public void setUserTrends(List<Map<String, Object>> userTrends) { this.userTrends = userTrends; }
    
    public List<Map<String, Object>> getProductTrends() { return productTrends; }
    public void setProductTrends(List<Map<String, Object>> productTrends) { this.productTrends = productTrends; }
    
    public List<Map<String, Object>> getTechnologyTrends() { return technologyTrends; }
    public void setTechnologyTrends(List<Map<String, Object>> technologyTrends) { this.technologyTrends = technologyTrends; }
} 
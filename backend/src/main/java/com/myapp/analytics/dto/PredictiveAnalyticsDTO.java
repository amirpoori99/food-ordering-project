package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class PredictiveAnalyticsDTO {
    private Map<String, Double> salesForecast;
    private Map<String, Double> revenueForecast;
    private Map<String, Double> userGrowthForecast;
    private Map<String, Double> churnPrediction;
    private Map<String, Double> demandForecast;
    private Map<String, Double> inventoryForecast;
    private Map<String, Double> demandPrediction;
    private Map<String, Double> trendPrediction;
    private List<Map<String, Object>> demandPredictionList;
    private List<Map<String, Object>> trendPredictionList;

    public PredictiveAnalyticsDTO() {}

    public Map<String, Double> getSalesForecast() { return salesForecast; }
    public void setSalesForecast(Map<String, Double> salesForecast) { this.salesForecast = salesForecast; }

    public Map<String, Double> getRevenueForecast() { return revenueForecast; }
    public void setRevenueForecast(Map<String, Double> revenueForecast) { this.revenueForecast = revenueForecast; }

    public Map<String, Double> getUserGrowthForecast() { return userGrowthForecast; }
    public void setUserGrowthForecast(Map<String, Double> userGrowthForecast) { this.userGrowthForecast = userGrowthForecast; }

    public Map<String, Double> getChurnPrediction() { return churnPrediction; }
    public void setChurnPrediction(Map<String, Double> churnPrediction) { this.churnPrediction = churnPrediction; }

    public Map<String, Double> getDemandForecast() { return demandForecast; }
    public void setDemandForecast(Map<String, Double> demandForecast) { this.demandForecast = demandForecast; }

    public Map<String, Double> getInventoryForecast() { return inventoryForecast; }
    public void setInventoryForecast(Map<String, Double> inventoryForecast) { this.inventoryForecast = inventoryForecast; }

    public Map<String, Double> getDemandPrediction() { return demandPrediction; }
    public void setDemandPrediction(Map<String, Double> demandPrediction) { this.demandPrediction = demandPrediction; }

    public Map<String, Double> getTrendPrediction() { return trendPrediction; }
    public void setTrendPrediction(Map<String, Double> trendPrediction) { this.trendPrediction = trendPrediction; }
    
    public List<Map<String, Object>> getDemandPredictionList() { return demandPredictionList; }
    public void setDemandPrediction(List<Map<String, Object>> demandPrediction) { this.demandPredictionList = demandPrediction; }
    
    public List<Map<String, Object>> getTrendPredictionList() { return trendPredictionList; }
    public void setTrendPrediction(List<Map<String, Object>> trendPrediction) { this.trendPredictionList = trendPrediction; }
} 
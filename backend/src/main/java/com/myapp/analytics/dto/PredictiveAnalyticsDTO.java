package com.myapp.analytics.dto;

import java.util.Map;

public class PredictiveAnalyticsDTO {
    private Map<String, Double> salesForecast;
    private Map<String, Double> revenueForecast;
    private Map<String, Double> userGrowthForecast;
    private Map<String, Double> churnPrediction;
    private Map<String, Double> demandForecast;
    private Map<String, Double> inventoryForecast;

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
} 
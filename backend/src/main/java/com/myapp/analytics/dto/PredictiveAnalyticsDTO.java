package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class PredictiveAnalyticsDTO {
    private String prediction;
    private double confidence;
    private String timeframe;
    private Map<String, Double> salesForecast;
    private Map<String, Double> demandPrediction;
    private Map<String, Double> trendPrediction;

    public PredictiveAnalyticsDTO() {}

    public PredictiveAnalyticsDTO(String prediction, double confidence, String timeframe,
                                Map<String, Double> salesForecast, Map<String, Double> demandPrediction,
                                Map<String, Double> trendPrediction) {
        this.prediction = prediction;
        this.confidence = confidence;
        this.timeframe = timeframe;
        this.salesForecast = salesForecast;
        this.demandPrediction = demandPrediction;
        this.trendPrediction = trendPrediction;
    }

    public String getPrediction() { return prediction; }
    public void setPrediction(String prediction) { this.prediction = prediction; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    
    public Map<String, Double> getSalesForecast() { return salesForecast; }
    public void setSalesForecast(Map<String, Double> salesForecast) { this.salesForecast = salesForecast; }
    
    public Map<String, Double> getDemandPrediction() { return demandPrediction; }
    public void setDemandPrediction(Map<String, Double> demandPrediction) { this.demandPrediction = demandPrediction; }
    
    public Map<String, Double> getTrendPrediction() { return trendPrediction; }
    public void setTrendPrediction(Map<String, Double> trendPrediction) { this.trendPrediction = trendPrediction; }
} 
package com.myapp.analytics.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PredictiveAnalysis {
    private Long userId;
    private LocalDateTime generatedAt;
    private LocalDateTime nextOrderPrediction;
    private double predictedOrderValue;
    private double orderProbability;
    private double churnProbability;
    private double lifetimeValue;
    private double retentionScore;
    private List<Object> recommendedItems;
    private List<Object> itemPreferences;

    public PredictiveAnalysis() {
        this.recommendedItems = new ArrayList<>();
        this.itemPreferences = new ArrayList<>();
    }

    public PredictiveAnalysis(Long userId, LocalDateTime generatedAt, LocalDateTime nextOrderPrediction, 
                             double predictedOrderValue, double orderProbability, double churnProbability, 
                             double lifetimeValue, double retentionScore, List<Object> recommendedItems, 
                             List<Object> itemPreferences) {
        this.userId = userId;
        this.generatedAt = generatedAt;
        this.nextOrderPrediction = nextOrderPrediction;
        this.predictedOrderValue = predictedOrderValue;
        this.orderProbability = orderProbability;
        this.churnProbability = churnProbability;
        this.lifetimeValue = lifetimeValue;
        this.retentionScore = retentionScore;
        this.recommendedItems = recommendedItems != null ? recommendedItems : new ArrayList<>();
        this.itemPreferences = itemPreferences != null ? itemPreferences : new ArrayList<>();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getNextOrderPrediction() { return nextOrderPrediction; }
    public void setNextOrderPrediction(LocalDateTime nextOrderPrediction) { this.nextOrderPrediction = nextOrderPrediction; }
    
    public double getPredictedOrderValue() { return predictedOrderValue; }
    public void setPredictedOrderValue(double predictedOrderValue) { this.predictedOrderValue = predictedOrderValue; }
    
    public double getOrderProbability() { return orderProbability; }
    public void setOrderProbability(double orderProbability) { this.orderProbability = orderProbability; }
    
    public double getChurnProbability() { return churnProbability; }
    public void setChurnProbability(double churnProbability) { this.churnProbability = churnProbability; }
    
    public double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(double lifetimeValue) { this.lifetimeValue = lifetimeValue; }
    
    public double getRetentionScore() { return retentionScore; }
    public void setRetentionScore(double retentionScore) { this.retentionScore = retentionScore; }
    
    public List<Object> getRecommendedItems() { return recommendedItems; }
    public void setRecommendedItems(List<Object> recommendedItems) { this.recommendedItems = recommendedItems; }
    
    public List<Object> getItemPreferences() { return itemPreferences; }
    public void setItemPreferences(List<Object> itemPreferences) { this.itemPreferences = itemPreferences; }
} 
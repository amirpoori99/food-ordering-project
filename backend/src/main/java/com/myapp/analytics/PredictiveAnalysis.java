package com.myapp.analytics;

import java.time.LocalDateTime;
import java.util.List;

public class PredictiveAnalysis {
    private Long userId;
    private LocalDateTime generatedAt;
    private LocalDateTime nextOrderPrediction;
    private Double predictedOrderValue;
    private Double orderProbability;
    private Double churnProbability;
    private Double lifetimeValue;
    private Double retentionScore;
    private List<Object> recommendedItems;
    private List<Object> itemPreferences;
    
    public PredictiveAnalysis() {}
    
    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getNextOrderPrediction() { return nextOrderPrediction; }
    public void setNextOrderPrediction(LocalDateTime nextOrderPrediction) { this.nextOrderPrediction = nextOrderPrediction; }
    
    public Double getPredictedOrderValue() { return predictedOrderValue; }
    public void setPredictedOrderValue(Double predictedOrderValue) { this.predictedOrderValue = predictedOrderValue; }
    
    public Double getOrderProbability() { return orderProbability; }
    public void setOrderProbability(Double orderProbability) { this.orderProbability = orderProbability; }
    
    public Double getChurnProbability() { return churnProbability; }
    public void setChurnProbability(Double churnProbability) { this.churnProbability = churnProbability; }
    
    public Double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(Double lifetimeValue) { this.lifetimeValue = lifetimeValue; }
    
    public Double getRetentionScore() { return retentionScore; }
    public void setRetentionScore(Double retentionScore) { this.retentionScore = retentionScore; }
    
    public List<Object> getRecommendedItems() { return recommendedItems; }
    public void setRecommendedItems(List<Object> recommendedItems) { this.recommendedItems = recommendedItems; }
    
    public List<Object> getItemPreferences() { return itemPreferences; }
    public void setItemPreferences(List<Object> itemPreferences) { this.itemPreferences = itemPreferences; }
} 
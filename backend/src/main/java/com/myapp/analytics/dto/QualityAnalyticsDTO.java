package com.myapp.analytics.dto;

import java.util.Map;

public class QualityAnalyticsDTO {
    private double averageRating;
    private double satisfactionRate;
    private Map<String, Double> qualityByCategory;
    private Map<String, Double> qualityTrends;

    public QualityAnalyticsDTO() {}

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public double getSatisfactionRate() { return satisfactionRate; }
    public void setSatisfactionRate(double satisfactionRate) { this.satisfactionRate = satisfactionRate; }

    public Map<String, Double> getQualityByCategory() { return qualityByCategory; }
    public void setQualityByCategory(Map<String, Double> qualityByCategory) { this.qualityByCategory = qualityByCategory; }

    public Map<String, Double> getQualityTrends() { return qualityTrends; }
    public void setQualityTrends(Map<String, Double> qualityTrends) { this.qualityTrends = qualityTrends; }
} 
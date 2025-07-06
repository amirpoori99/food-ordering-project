package com.myapp.analytics.dto;

import java.util.Map;

public class QualityAnalyticsDTO {
    private double averageRating;
    private double satisfactionRate;
    private Map<String, Double> qualityByCategory;
    private Map<String, Double> qualityTrends;
    
    // فیلدهای جدید مورد نیاز
    private double customerSatisfaction;
    private double orderAccuracy;
    private double foodQuality;
    private double serviceQuality;

    public QualityAnalyticsDTO() {}

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public double getSatisfactionRate() { return satisfactionRate; }
    public void setSatisfactionRate(double satisfactionRate) { this.satisfactionRate = satisfactionRate; }

    public Map<String, Double> getQualityByCategory() { return qualityByCategory; }
    public void setQualityByCategory(Map<String, Double> qualityByCategory) { this.qualityByCategory = qualityByCategory; }

    public Map<String, Double> getQualityTrends() { return qualityTrends; }
    public void setQualityTrends(Map<String, Double> qualityTrends) { this.qualityTrends = qualityTrends; }
    
    // متدهای جدید مورد نیاز
    public double getCustomerSatisfaction() { return customerSatisfaction; }
    public void setCustomerSatisfaction(double customerSatisfaction) { this.customerSatisfaction = customerSatisfaction; }
    
    public double getOrderAccuracy() { return orderAccuracy; }
    public void setOrderAccuracy(double orderAccuracy) { this.orderAccuracy = orderAccuracy; }
    
    public double getFoodQuality() { return foodQuality; }
    public void setFoodQuality(double foodQuality) { this.foodQuality = foodQuality; }
    
    public double getServiceQuality() { return serviceQuality; }
    public void setServiceQuality(double serviceQuality) { this.serviceQuality = serviceQuality; }
} 
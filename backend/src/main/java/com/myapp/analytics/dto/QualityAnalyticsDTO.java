package com.myapp.analytics.dto;

public class QualityAnalyticsDTO {
    private double averageRating;
    private int totalReviews;
    private int positiveReviews;
    private int negativeReviews;
    private double customerSatisfaction;
    private double orderAccuracy;
    private double foodQuality;
    private double serviceQuality;

    public QualityAnalyticsDTO() {}

    public QualityAnalyticsDTO(double averageRating, int totalReviews, int positiveReviews, int negativeReviews,
                             double customerSatisfaction, double orderAccuracy, double foodQuality, double serviceQuality) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.positiveReviews = positiveReviews;
        this.negativeReviews = negativeReviews;
        this.customerSatisfaction = customerSatisfaction;
        this.orderAccuracy = orderAccuracy;
        this.foodQuality = foodQuality;
        this.serviceQuality = serviceQuality;
    }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    
    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
    
    public int getPositiveReviews() { return positiveReviews; }
    public void setPositiveReviews(int positiveReviews) { this.positiveReviews = positiveReviews; }
    
    public int getNegativeReviews() { return negativeReviews; }
    public void setNegativeReviews(int negativeReviews) { this.negativeReviews = negativeReviews; }
    
    public double getCustomerSatisfaction() { return customerSatisfaction; }
    public void setCustomerSatisfaction(double customerSatisfaction) { this.customerSatisfaction = customerSatisfaction; }
    
    public double getOrderAccuracy() { return orderAccuracy; }
    public void setOrderAccuracy(double orderAccuracy) { this.orderAccuracy = orderAccuracy; }
    
    public double getFoodQuality() { return foodQuality; }
    public void setFoodQuality(double foodQuality) { this.foodQuality = foodQuality; }
    
    public double getServiceQuality() { return serviceQuality; }
    public void setServiceQuality(double serviceQuality) { this.serviceQuality = serviceQuality; }
} 
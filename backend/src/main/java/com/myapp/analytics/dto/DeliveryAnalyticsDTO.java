package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class DeliveryAnalyticsDTO {
    private int totalDeliveries;
    private double averageDeliveryTime;
    private int successfulDeliveries;
    private double deliverySuccessRate;
    private List<Map<String, Object>> deliveryByArea;
    private List<Map<String, Object>> courierPerformance;

    public DeliveryAnalyticsDTO() {}

    public DeliveryAnalyticsDTO(int totalDeliveries, double averageDeliveryTime, int successfulDeliveries,
                              double deliverySuccessRate, List<Map<String, Object>> deliveryByArea,
                              List<Map<String, Object>> courierPerformance) {
        this.totalDeliveries = totalDeliveries;
        this.averageDeliveryTime = averageDeliveryTime;
        this.successfulDeliveries = successfulDeliveries;
        this.deliverySuccessRate = deliverySuccessRate;
        this.deliveryByArea = deliveryByArea;
        this.courierPerformance = courierPerformance;
    }

    public int getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(int totalDeliveries) { this.totalDeliveries = totalDeliveries; }
    
    public double getAverageDeliveryTime() { return averageDeliveryTime; }
    public void setAverageDeliveryTime(double averageDeliveryTime) { this.averageDeliveryTime = averageDeliveryTime; }
    
    public int getSuccessfulDeliveries() { return successfulDeliveries; }
    public void setSuccessfulDeliveries(int successfulDeliveries) { this.successfulDeliveries = successfulDeliveries; }
    
    public double getDeliverySuccessRate() { return deliverySuccessRate; }
    public void setDeliverySuccessRate(double deliverySuccessRate) { this.deliverySuccessRate = deliverySuccessRate; }
    
    public List<Map<String, Object>> getDeliveryByArea() { return deliveryByArea; }
    public void setDeliveryByArea(List<Map<String, Object>> deliveryByArea) { this.deliveryByArea = deliveryByArea; }
    
    public List<Map<String, Object>> getCourierPerformance() { return courierPerformance; }
    public void setCourierPerformance(List<Map<String, Object>> courierPerformance) { this.courierPerformance = courierPerformance; }
} 
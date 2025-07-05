package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class DeliveryAnalyticsDTO {
    private long totalDeliveries;
    private long activeCouriers;
    private double averageDeliveryTime;
    private double deliverySuccessRate;
    private double customerSatisfaction;
    private Map<String, Double> deliveryZones;
    private List<Map<String, Object>> topCouriers;

    public DeliveryAnalyticsDTO() {}

    public long getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(long totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public long getActiveCouriers() { return activeCouriers; }
    public void setActiveCouriers(long activeCouriers) { this.activeCouriers = activeCouriers; }

    public double getAverageDeliveryTime() { return averageDeliveryTime; }
    public void setAverageDeliveryTime(double averageDeliveryTime) { this.averageDeliveryTime = averageDeliveryTime; }

    public double getDeliverySuccessRate() { return deliverySuccessRate; }
    public void setDeliverySuccessRate(double deliverySuccessRate) { this.deliverySuccessRate = deliverySuccessRate; }

    public double getCustomerSatisfaction() { return customerSatisfaction; }
    public void setCustomerSatisfaction(double customerSatisfaction) { this.customerSatisfaction = customerSatisfaction; }

    public Map<String, Double> getDeliveryZones() { return deliveryZones; }
    public void setDeliveryZones(Map<String, Double> deliveryZones) { this.deliveryZones = deliveryZones; }

    public List<Map<String, Object>> getTopCouriers() { return topCouriers; }
    public void setTopCouriers(List<Map<String, Object>> topCouriers) { this.topCouriers = topCouriers; }
} 
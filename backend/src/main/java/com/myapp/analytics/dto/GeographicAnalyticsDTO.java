package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class GeographicAnalyticsDTO {
    private String region;
    private double value;
    private Map<String, Double> regionalSales;
    private Map<String, Double> userDistribution;
    private Map<String, Double> restaurantDistribution;
    private List<Map<String, Object>> salesByRegion;
    private List<Map<String, Object>> userDistributionList;
    private List<Map<String, Object>> restaurantDistributionList;

    public GeographicAnalyticsDTO() {}

    public GeographicAnalyticsDTO(String region, double value, Map<String, Double> regionalSales,
                                Map<String, Double> userDistribution, Map<String, Double> restaurantDistribution,
                                List<Map<String, Object>> salesByRegion, List<Map<String, Object>> userDistributionList,
                                List<Map<String, Object>> restaurantDistributionList) {
        this.region = region;
        this.value = value;
        this.regionalSales = regionalSales;
        this.userDistribution = userDistribution;
        this.restaurantDistribution = restaurantDistribution;
        this.salesByRegion = salesByRegion;
        this.userDistributionList = userDistributionList;
        this.restaurantDistributionList = restaurantDistributionList;
    }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public Map<String, Double> getRegionalSales() { return regionalSales; }
    public void setRegionalSales(Map<String, Double> regionalSales) { this.regionalSales = regionalSales; }
    
    public Map<String, Double> getUserDistribution() { return userDistribution; }
    public void setUserDistribution(Map<String, Double> userDistribution) { this.userDistribution = userDistribution; }
    
    public Map<String, Double> getRestaurantDistribution() { return restaurantDistribution; }
    public void setRestaurantDistribution(Map<String, Double> restaurantDistribution) { this.restaurantDistribution = restaurantDistribution; }
    
    public List<Map<String, Object>> getSalesByRegion() { return salesByRegion; }
    public void setSalesByRegion(List<Map<String, Object>> salesByRegion) { this.salesByRegion = salesByRegion; }
    
    public List<Map<String, Object>> getUserDistributionList() { return userDistributionList; }
    public void setUserDistributionList(List<Map<String, Object>> userDistributionList) { this.userDistributionList = userDistributionList; }
    
    public List<Map<String, Object>> getRestaurantDistributionList() { return restaurantDistributionList; }
    public void setRestaurantDistributionList(List<Map<String, Object>> restaurantDistributionList) { this.restaurantDistributionList = restaurantDistributionList; }
} 
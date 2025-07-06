package com.myapp.analytics.dto;

import java.util.Map;
import java.util.List;

public class GeographicAnalyticsDTO {
    private Map<String, Double> orderDistribution;
    private Map<String, Double> revenueDistribution;
    private List<String> topRegions;
    private Map<String, Double> regionGrowth;
    private List<String> topCities;
    private Map<String, Double> cityPerformance;
    private Map<String, Double> regionalSales;
    private Map<String, Double> userDistribution;
    private Map<String, Double> restaurantDistribution;
    private List<Map<String, Object>> salesByRegion;
    private List<Map<String, Object>> userDistributionList;
    private List<Map<String, Object>> restaurantDistributionList;

    public GeographicAnalyticsDTO() {}

    public Map<String, Double> getOrderDistribution() { return orderDistribution; }
    public void setOrderDistribution(Map<String, Double> orderDistribution) { this.orderDistribution = orderDistribution; }

    public Map<String, Double> getRevenueDistribution() { return revenueDistribution; }
    public void setRevenueDistribution(Map<String, Double> revenueDistribution) { this.revenueDistribution = revenueDistribution; }

    public List<String> getTopRegions() { return topRegions; }
    public void setTopRegions(List<String> topRegions) { this.topRegions = topRegions; }

    public Map<String, Double> getRegionGrowth() { return regionGrowth; }
    public void setRegionGrowth(Map<String, Double> regionGrowth) { this.regionGrowth = regionGrowth; }

    public List<String> getTopCities() { return topCities; }
    public void setTopCities(List<String> topCities) { this.topCities = topCities; }

    public Map<String, Double> getCityPerformance() { return cityPerformance; }
    public void setCityPerformance(Map<String, Double> cityPerformance) { this.cityPerformance = cityPerformance; }

    public Map<String, Double> getRegionalSales() { return regionalSales; }
    public void setRegionalSales(Map<String, Double> regionalSales) { this.regionalSales = regionalSales; }

    public Map<String, Double> getUserDistribution() { return userDistribution; }
    public void setUserDistribution(Map<String, Double> userDistribution) { this.userDistribution = userDistribution; }

    public Map<String, Double> getRestaurantDistribution() { return restaurantDistribution; }
    public void setRestaurantDistribution(Map<String, Double> restaurantDistribution) { this.restaurantDistribution = restaurantDistribution; }
    
    public List<Map<String, Object>> getSalesByRegion() { return salesByRegion; }
    public void setSalesByRegion(List<Map<String, Object>> salesByRegion) { this.salesByRegion = salesByRegion; }
    
    public List<Map<String, Object>> getUserDistributionList() { return userDistributionList; }
    public void setUserDistributionList(List<Map<String, Object>> userDistribution) { this.userDistributionList = userDistribution; }
    
    public List<Map<String, Object>> getRestaurantDistributionList() { return restaurantDistributionList; }
    public void setRestaurantDistributionList(List<Map<String, Object>> restaurantDistribution) { this.restaurantDistributionList = restaurantDistribution; }
} 
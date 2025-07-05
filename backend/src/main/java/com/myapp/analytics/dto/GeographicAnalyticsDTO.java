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
} 
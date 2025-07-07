package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class TrendAnalyticsDTO {
    private String trendName;
    private double trendValue;
    private List<Map<String, Object>> salesTrends;
    private List<Map<String, Object>> userTrends;
    private List<Map<String, Object>> productTrends;
    private List<Map<String, Object>> technologyTrends;

    public TrendAnalyticsDTO() {}

    public TrendAnalyticsDTO(String trendName, double trendValue, List<Map<String, Object>> salesTrends,
                           List<Map<String, Object>> userTrends, List<Map<String, Object>> productTrends,
                           List<Map<String, Object>> technologyTrends) {
        this.trendName = trendName;
        this.trendValue = trendValue;
        this.salesTrends = salesTrends;
        this.userTrends = userTrends;
        this.productTrends = productTrends;
        this.technologyTrends = technologyTrends;
    }

    public String getTrendName() { return trendName; }
    public void setTrendName(String trendName) { this.trendName = trendName; }
    
    public double getTrendValue() { return trendValue; }
    public void setTrendValue(double trendValue) { this.trendValue = trendValue; }
    
    public List<Map<String, Object>> getSalesTrends() { return salesTrends; }
    public void setSalesTrends(List<Map<String, Object>> salesTrends) { this.salesTrends = salesTrends; }
    
    public List<Map<String, Object>> getUserTrends() { return userTrends; }
    public void setUserTrends(List<Map<String, Object>> userTrends) { this.userTrends = userTrends; }
    
    public List<Map<String, Object>> getProductTrends() { return productTrends; }
    public void setProductTrends(List<Map<String, Object>> productTrends) { this.productTrends = productTrends; }
    
    public List<Map<String, Object>> getTechnologyTrends() { return technologyTrends; }
    public void setTechnologyTrends(List<Map<String, Object>> technologyTrends) { this.technologyTrends = technologyTrends; }
} 
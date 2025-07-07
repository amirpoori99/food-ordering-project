package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class SalesAnalyticsDTO {
    private double totalSales;
    private int totalOrders;
    private double averageOrderValue;
    private double salesGrowth;
    private List<Map<String, Object>> topSellingItems;
    private List<Map<String, Object>> salesByCategory;
    private List<Map<String, Object>> salesByRestaurant;
    private List<Map<String, Object>> salesByTime;

    public SalesAnalyticsDTO() {}

    public SalesAnalyticsDTO(double totalSales, int totalOrders, double averageOrderValue, double salesGrowth,
                           List<Map<String, Object>> topSellingItems, List<Map<String, Object>> salesByCategory,
                           List<Map<String, Object>> salesByRestaurant, List<Map<String, Object>> salesByTime) {
        this.totalSales = totalSales;
        this.totalOrders = totalOrders;
        this.averageOrderValue = averageOrderValue;
        this.salesGrowth = salesGrowth;
        this.topSellingItems = topSellingItems;
        this.salesByCategory = salesByCategory;
        this.salesByRestaurant = salesByRestaurant;
        this.salesByTime = salesByTime;
    }

    public double getTotalSales() { return totalSales; }
    public void setTotalSales(double totalSales) { this.totalSales = totalSales; }
    
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    
    public double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public double getSalesGrowth() { return salesGrowth; }
    public void setSalesGrowth(double salesGrowth) { this.salesGrowth = salesGrowth; }
    
    public List<Map<String, Object>> getTopSellingItems() { return topSellingItems; }
    public void setTopSellingItems(List<Map<String, Object>> topSellingItems) { this.topSellingItems = topSellingItems; }
    
    public List<Map<String, Object>> getSalesByCategory() { return salesByCategory; }
    public void setSalesByCategory(List<Map<String, Object>> salesByCategory) { this.salesByCategory = salesByCategory; }
    
    public List<Map<String, Object>> getSalesByRestaurant() { return salesByRestaurant; }
    public void setSalesByRestaurant(List<Map<String, Object>> salesByRestaurant) { this.salesByRestaurant = salesByRestaurant; }
    
    public List<Map<String, Object>> getSalesByTime() { return salesByTime; }
    public void setSalesByTime(List<Map<String, Object>> salesByTime) { this.salesByTime = salesByTime; }
} 
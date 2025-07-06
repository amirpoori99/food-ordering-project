package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO برای آمار فروش و درآمد
 */
public class SalesAnalyticsDTO {
    
    // آمار فروش
    private long totalSales;
    private double averageOrderValue;
    private double salesGrowth;
    
    // آمار درآمد
    private double totalRevenue;
    private double revenueGrowth;
    private double profitMargin;
    
    // آمار محصولات
    private List<Map<String, Object>> topSellingItems;
    private List<Map<String, Object>> topSellingCategories;
    
    // آمار زمانی
    private Map<String, Double> hourlySales;
    private Map<String, Double> dailySales;
    private Map<String, Double> weeklySales;
    private Map<String, Double> monthlySales;
    
    // آمار اضافی
    private Map<String, Object> additionalMetrics;
    
    // فیلدهای جدید مورد نیاز
    private List<Map<String, Object>> salesByCategory;
    private List<Map<String, Object>> salesByRestaurant;
    private List<Map<String, Object>> salesByTime;
    
    // Constructors
    public SalesAnalyticsDTO() {}
    
    // Getters and Setters
    public long getTotalSales() {
        return totalSales;
    }
    
    public void setTotalSales(long totalSales) {
        this.totalSales = totalSales;
    }
    
    public double getAverageOrderValue() {
        return averageOrderValue;
    }
    
    public void setAverageOrderValue(double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    
    public double getSalesGrowth() {
        return salesGrowth;
    }
    
    public void setSalesGrowth(double salesGrowth) {
        this.salesGrowth = salesGrowth;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public double getRevenueGrowth() {
        return revenueGrowth;
    }
    
    public void setRevenueGrowth(double revenueGrowth) {
        this.revenueGrowth = revenueGrowth;
    }
    
    public double getProfitMargin() {
        return profitMargin;
    }
    
    public void setProfitMargin(double profitMargin) {
        this.profitMargin = profitMargin;
    }
    
    public List<Map<String, Object>> getTopSellingItems() {
        return topSellingItems;
    }
    
    public void setTopSellingItems(List<Map<String, Object>> topSellingItems) {
        this.topSellingItems = topSellingItems;
    }
    
    public List<Map<String, Object>> getTopSellingCategories() {
        return topSellingCategories;
    }
    
    public void setTopSellingCategories(List<Map<String, Object>> topSellingCategories) {
        this.topSellingCategories = topSellingCategories;
    }
    
    public Map<String, Double> getHourlySales() {
        return hourlySales;
    }
    
    public void setHourlySales(Map<String, Double> hourlySales) {
        this.hourlySales = hourlySales;
    }
    
    public Map<String, Double> getDailySales() {
        return dailySales;
    }
    
    public void setDailySales(Map<String, Double> dailySales) {
        this.dailySales = dailySales;
    }
    
    public Map<String, Double> getWeeklySales() {
        return weeklySales;
    }
    
    public void setWeeklySales(Map<String, Double> weeklySales) {
        this.weeklySales = weeklySales;
    }
    
    public Map<String, Double> getMonthlySales() {
        return monthlySales;
    }
    
    public void setMonthlySales(Map<String, Double> monthlySales) {
        this.monthlySales = monthlySales;
    }
    
    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }
    
    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }
    
    // متدهای جدید مورد نیاز
    public List<Map<String, Object>> getSalesByCategory() {
        return salesByCategory;
    }
    
    public void setSalesByCategory(List<Map<String, Object>> salesByCategory) {
        this.salesByCategory = salesByCategory;
    }
    
    public List<Map<String, Object>> getSalesByRestaurant() {
        return salesByRestaurant;
    }
    
    public void setSalesByRestaurant(List<Map<String, Object>> salesByRestaurant) {
        this.salesByRestaurant = salesByRestaurant;
    }
    
    public List<Map<String, Object>> getSalesByTime() {
        return salesByTime;
    }
    
    public void setSalesByTime(List<Map<String, Object>> salesByTime) {
        this.salesByTime = salesByTime;
    }
} 
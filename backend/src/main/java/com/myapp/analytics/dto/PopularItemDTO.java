package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class PopularItemDTO {
    private String itemName;
    private int orderCount;
    private double totalRevenue;
    private long itemId;
    private int totalQuantity;
    private List<Map<String, Object>> popularItems;
    private List<Map<String, Object>> itemPerformance;
    private List<Map<String, Object>> itemTrends;

    public PopularItemDTO() {}

    public PopularItemDTO(String itemName, int orderCount, double totalRevenue,
                         long itemId, int totalQuantity,
                         List<Map<String, Object>> popularItems,
                         List<Map<String, Object>> itemPerformance,
                         List<Map<String, Object>> itemTrends) {
        this.itemName = itemName;
        this.orderCount = orderCount;
        this.totalRevenue = totalRevenue;
        this.itemId = itemId;
        this.totalQuantity = totalQuantity;
        this.popularItems = popularItems;
        this.itemPerformance = itemPerformance;
        this.itemTrends = itemTrends;
    }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public long getItemId() { return itemId; }
    public void setItemId(long itemId) { this.itemId = itemId; }
    
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    
    public List<Map<String, Object>> getPopularItems() { return popularItems; }
    public void setPopularItems(List<Map<String, Object>> popularItems) { this.popularItems = popularItems; }
    
    public List<Map<String, Object>> getItemPerformance() { return itemPerformance; }
    public void setItemPerformance(List<Map<String, Object>> itemPerformance) { this.itemPerformance = itemPerformance; }
    
    public List<Map<String, Object>> getItemTrends() { return itemTrends; }
    public void setItemTrends(List<Map<String, Object>> itemTrends) { this.itemTrends = itemTrends; }
} 
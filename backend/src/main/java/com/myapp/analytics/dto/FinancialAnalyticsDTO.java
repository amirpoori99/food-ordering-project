package com.myapp.analytics.dto;

import java.util.Map;

public class FinancialAnalyticsDTO {
    private double totalRevenue;
    private double totalCost;
    private double profit;
    private double profitMargin;
    private Map<String, Double> revenueByCategory;
    private Map<String, Double> costByCategory;
    private Map<String, Double> financialTrends;

    public FinancialAnalyticsDTO() {}

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getProfit() { return profit; }
    public void setProfit(double profit) { this.profit = profit; }

    public double getProfitMargin() { return profitMargin; }
    public void setProfitMargin(double profitMargin) { this.profitMargin = profitMargin; }

    public Map<String, Double> getRevenueByCategory() { return revenueByCategory; }
    public void setRevenueByCategory(Map<String, Double> revenueByCategory) { this.revenueByCategory = revenueByCategory; }

    public Map<String, Double> getCostByCategory() { return costByCategory; }
    public void setCostByCategory(Map<String, Double> costByCategory) { this.costByCategory = costByCategory; }

    public Map<String, Double> getFinancialTrends() { return financialTrends; }
    public void setFinancialTrends(Map<String, Double> financialTrends) { this.financialTrends = financialTrends; }
} 
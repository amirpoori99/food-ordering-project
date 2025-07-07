package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class FinancialAnalyticsDTO {
    private double totalRevenue;
    private double totalProfit;
    private double totalExpenses;
    private double revenueGrowth;
    private double profitMargin;
    private List<Map<String, Object>> costAnalysis;
    private List<Map<String, Object>> paymentMethods;

    public FinancialAnalyticsDTO() {}

    public FinancialAnalyticsDTO(double totalRevenue, double totalProfit, double totalExpenses,
                               double revenueGrowth, double profitMargin,
                               List<Map<String, Object>> costAnalysis,
                               List<Map<String, Object>> paymentMethods) {
        this.totalRevenue = totalRevenue;
        this.totalProfit = totalProfit;
        this.totalExpenses = totalExpenses;
        this.revenueGrowth = revenueGrowth;
        this.profitMargin = profitMargin;
        this.costAnalysis = costAnalysis;
        this.paymentMethods = paymentMethods;
    }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public double getTotalProfit() { return totalProfit; }
    public void setTotalProfit(double totalProfit) { this.totalProfit = totalProfit; }
    
    public double getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(double totalExpenses) { this.totalExpenses = totalExpenses; }
    
    public double getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(double revenueGrowth) { this.revenueGrowth = revenueGrowth; }
    
    public double getProfitMargin() { return profitMargin; }
    public void setProfitMargin(double profitMargin) { this.profitMargin = profitMargin; }
    
    public List<Map<String, Object>> getCostAnalysis() { return costAnalysis; }
    public void setCostAnalysis(List<Map<String, Object>> costAnalysis) { this.costAnalysis = costAnalysis; }
    
    public List<Map<String, Object>> getPaymentMethods() { return paymentMethods; }
    public void setPaymentMethods(List<Map<String, Object>> paymentMethods) { this.paymentMethods = paymentMethods; }
} 
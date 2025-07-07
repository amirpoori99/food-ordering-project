package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class LoyaltyAnalyticsDTO {
    private int loyalUsers;
    private double loyaltyRate;
    private double customerLoyalty;
    private double retentionRate;
    private Map<String, Double> loyaltyPrograms;
    private List<Map<String, Object>> loyaltyProgramsList;

    public LoyaltyAnalyticsDTO() {}

    public LoyaltyAnalyticsDTO(int loyalUsers, double loyaltyRate, double customerLoyalty, double retentionRate,
                             Map<String, Double> loyaltyPrograms, List<Map<String, Object>> loyaltyProgramsList) {
        this.loyalUsers = loyalUsers;
        this.loyaltyRate = loyaltyRate;
        this.customerLoyalty = customerLoyalty;
        this.retentionRate = retentionRate;
        this.loyaltyPrograms = loyaltyPrograms;
        this.loyaltyProgramsList = loyaltyProgramsList;
    }

    public int getLoyalUsers() { return loyalUsers; }
    public void setLoyalUsers(int loyalUsers) { this.loyalUsers = loyalUsers; }
    
    public double getLoyaltyRate() { return loyaltyRate; }
    public void setLoyaltyRate(double loyaltyRate) { this.loyaltyRate = loyaltyRate; }
    
    public double getCustomerLoyalty() { return customerLoyalty; }
    public void setCustomerLoyalty(double customerLoyalty) { this.customerLoyalty = customerLoyalty; }
    
    public double getRetentionRate() { return retentionRate; }
    public void setRetentionRate(double retentionRate) { this.retentionRate = retentionRate; }
    
    public Map<String, Double> getLoyaltyPrograms() { return loyaltyPrograms; }
    public void setLoyaltyPrograms(Map<String, Double> loyaltyPrograms) { this.loyaltyPrograms = loyaltyPrograms; }
    
    public List<Map<String, Object>> getLoyaltyProgramsList() { return loyaltyProgramsList; }
    public void setLoyaltyProgramsList(List<Map<String, Object>> loyaltyProgramsList) { this.loyaltyProgramsList = loyaltyProgramsList; }
} 
package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class ROIAnalyticsDTO {
    private double roi;
    private double investment;
    private double returnValue;
    private double investmentROI;
    private double marketingROI;
    private double technologyROI;
    private List<Map<String, Object>> roiByCampaign;
    private List<Map<String, Object>> roiByChannel;
    private List<Map<String, Object>> roiByInvestment;

    public ROIAnalyticsDTO() {}

    public ROIAnalyticsDTO(double roi, double investment, double returnValue,
                          double investmentROI, double marketingROI, double technologyROI,
                          List<Map<String, Object>> roiByCampaign,
                          List<Map<String, Object>> roiByChannel,
                          List<Map<String, Object>> roiByInvestment) {
        this.roi = roi;
        this.investment = investment;
        this.returnValue = returnValue;
        this.investmentROI = investmentROI;
        this.marketingROI = marketingROI;
        this.technologyROI = technologyROI;
        this.roiByCampaign = roiByCampaign;
        this.roiByChannel = roiByChannel;
        this.roiByInvestment = roiByInvestment;
    }

    public double getRoi() { return roi; }
    public void setRoi(double roi) { this.roi = roi; }
    
    public double getInvestment() { return investment; }
    public void setInvestment(double investment) { this.investment = investment; }
    
    public double getReturnValue() { return returnValue; }
    public void setReturnValue(double returnValue) { this.returnValue = returnValue; }
    
    public double getInvestmentROI() { return investmentROI; }
    public void setInvestmentROI(double investmentROI) { this.investmentROI = investmentROI; }
    
    public double getMarketingROI() { return marketingROI; }
    public void setMarketingROI(double marketingROI) { this.marketingROI = marketingROI; }
    
    public double getTechnologyROI() { return technologyROI; }
    public void setTechnologyROI(double technologyROI) { this.technologyROI = technologyROI; }
    
    public List<Map<String, Object>> getRoiByCampaign() { return roiByCampaign; }
    public void setRoiByCampaign(List<Map<String, Object>> roiByCampaign) { this.roiByCampaign = roiByCampaign; }
    
    public List<Map<String, Object>> getRoiByChannel() { return roiByChannel; }
    public void setRoiByChannel(List<Map<String, Object>> roiByChannel) { this.roiByChannel = roiByChannel; }
    
    public List<Map<String, Object>> getRoiByInvestment() { return roiByInvestment; }
    public void setRoiByInvestment(List<Map<String, Object>> roiByInvestment) { this.roiByInvestment = roiByInvestment; }
} 
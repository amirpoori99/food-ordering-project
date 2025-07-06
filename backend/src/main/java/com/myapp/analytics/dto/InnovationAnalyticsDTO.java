package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class InnovationAnalyticsDTO {
    private Map<String, Double> innovationMetrics;
    private Map<String, Double> innovationTrends;
    private Map<String, Double> rAndDInvestments;
    private Map<String, Double> technologyAdoption;
    private Map<String, Double> productInnovation;
    private List<Map<String, Object>> technologyAdoptionList;
    private List<Map<String, Object>> productInnovationList;

    public InnovationAnalyticsDTO() {}

    public Map<String, Double> getInnovationMetrics() { return innovationMetrics; }
    public void setInnovationMetrics(Map<String, Double> innovationMetrics) { this.innovationMetrics = innovationMetrics; }

    public Map<String, Double> getInnovationTrends() { return innovationTrends; }
    public void setInnovationTrends(Map<String, Double> innovationTrends) { this.innovationTrends = innovationTrends; }

    public Map<String, Double> getRAndDInvestments() { return rAndDInvestments; }
    public void setRAndDInvestments(Map<String, Double> rAndDInvestments) { this.rAndDInvestments = rAndDInvestments; }

    public Map<String, Double> getTechnologyAdoption() { return technologyAdoption; }
    public void setTechnologyAdoption(Map<String, Double> technologyAdoption) { this.technologyAdoption = technologyAdoption; }

    public Map<String, Double> getProductInnovation() { return productInnovation; }
    public void setProductInnovation(Map<String, Double> productInnovation) { this.productInnovation = productInnovation; }

    public List<Map<String, Object>> getTechnologyAdoptionList() { return technologyAdoptionList; }
    public void setTechnologyAdoptionList(List<Map<String, Object>> technologyAdoptionList) { this.technologyAdoptionList = technologyAdoptionList; }

    public List<Map<String, Object>> getProductInnovationList() { return productInnovationList; }
    public void setProductInnovationList(List<Map<String, Object>> productInnovationList) { this.productInnovationList = productInnovationList; }
} 
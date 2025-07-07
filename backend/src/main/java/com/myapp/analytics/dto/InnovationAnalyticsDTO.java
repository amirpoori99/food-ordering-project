package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class InnovationAnalyticsDTO {
    private String innovation;
    private double impact;
    private String status;
    private Map<String, Double> productInnovation;
    private List<Map<String, Object>> innovationMetrics;
    private List<Map<String, Object>> technologyAdoption;
    private List<Map<String, Object>> innovationPipeline;

    public InnovationAnalyticsDTO() {}

    public InnovationAnalyticsDTO(String innovation, double impact, String status,
                                Map<String, Double> productInnovation,
                                List<Map<String, Object>> innovationMetrics,
                                List<Map<String, Object>> technologyAdoption,
                                List<Map<String, Object>> innovationPipeline) {
        this.innovation = innovation;
        this.impact = impact;
        this.status = status;
        this.productInnovation = productInnovation;
        this.innovationMetrics = innovationMetrics;
        this.technologyAdoption = technologyAdoption;
        this.innovationPipeline = innovationPipeline;
    }

    public String getInnovation() { return innovation; }
    public void setInnovation(String innovation) { this.innovation = innovation; }
    
    public double getImpact() { return impact; }
    public void setImpact(double impact) { this.impact = impact; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Map<String, Double> getProductInnovation() { return productInnovation; }
    public void setProductInnovation(Map<String, Double> productInnovation) { this.productInnovation = productInnovation; }
    
    public List<Map<String, Object>> getInnovationMetrics() { return innovationMetrics; }
    public void setInnovationMetrics(List<Map<String, Object>> innovationMetrics) { this.innovationMetrics = innovationMetrics; }
    
    public List<Map<String, Object>> getTechnologyAdoption() { return technologyAdoption; }
    public void setTechnologyAdoption(List<Map<String, Object>> technologyAdoption) { this.technologyAdoption = technologyAdoption; }
    
    public List<Map<String, Object>> getInnovationPipeline() { return innovationPipeline; }
    public void setInnovationPipeline(List<Map<String, Object>> innovationPipeline) { this.innovationPipeline = innovationPipeline; }
} 
package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class SecurityAnalyticsDTO {
    private int totalIncidents;
    private int resolvedIncidents;
    private int criticalIncidents;
    private List<Map<String, Object>> securityIncidents;
    private List<Map<String, Object>> fraudDetection;
    private List<Map<String, Object>> complianceMetrics;

    public SecurityAnalyticsDTO() {}

    public SecurityAnalyticsDTO(int totalIncidents, int resolvedIncidents, int criticalIncidents,
                              List<Map<String, Object>> securityIncidents,
                              List<Map<String, Object>> fraudDetection,
                              List<Map<String, Object>> complianceMetrics) {
        this.totalIncidents = totalIncidents;
        this.resolvedIncidents = resolvedIncidents;
        this.criticalIncidents = criticalIncidents;
        this.securityIncidents = securityIncidents;
        this.fraudDetection = fraudDetection;
        this.complianceMetrics = complianceMetrics;
    }

    public int getTotalIncidents() { return totalIncidents; }
    public void setTotalIncidents(int totalIncidents) { this.totalIncidents = totalIncidents; }
    
    public int getResolvedIncidents() { return resolvedIncidents; }
    public void setResolvedIncidents(int resolvedIncidents) { this.resolvedIncidents = resolvedIncidents; }
    
    public int getCriticalIncidents() { return criticalIncidents; }
    public void setCriticalIncidents(int criticalIncidents) { this.criticalIncidents = criticalIncidents; }
    
    public List<Map<String, Object>> getSecurityIncidents() { return securityIncidents; }
    public void setSecurityIncidents(List<Map<String, Object>> securityIncidents) { this.securityIncidents = securityIncidents; }
    
    public List<Map<String, Object>> getFraudDetection() { return fraudDetection; }
    public void setFraudDetection(List<Map<String, Object>> fraudDetection) { this.fraudDetection = fraudDetection; }
    
    public List<Map<String, Object>> getComplianceMetrics() { return complianceMetrics; }
    public void setComplianceMetrics(List<Map<String, Object>> complianceMetrics) { this.complianceMetrics = complianceMetrics; }
} 
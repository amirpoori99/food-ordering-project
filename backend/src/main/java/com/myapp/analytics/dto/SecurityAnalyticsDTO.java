package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class SecurityAnalyticsDTO {
    private double incidentRate;
    private double threatLevel;
    private Map<String, Double> securityEvents;
    private Map<String, Double> securityTrends;
    private Map<String, Double> securityMetrics;
    private Map<String, Double> fraudMetrics;
    private Map<String, Double> complianceMetrics;
    private List<Map<String, Object>> securityIncidents;
    private List<Map<String, Object>> fraudDetection;
    private List<Map<String, Object>> complianceMetricsList;

    public SecurityAnalyticsDTO() {}

    public double getIncidentRate() { return incidentRate; }
    public void setIncidentRate(double incidentRate) { this.incidentRate = incidentRate; }

    public double getThreatLevel() { return threatLevel; }
    public void setThreatLevel(double threatLevel) { this.threatLevel = threatLevel; }

    public Map<String, Double> getSecurityEvents() { return securityEvents; }
    public void setSecurityEvents(Map<String, Double> securityEvents) { this.securityEvents = securityEvents; }

    public Map<String, Double> getSecurityTrends() { return securityTrends; }
    public void setSecurityTrends(Map<String, Double> securityTrends) { this.securityTrends = securityTrends; }

    public Map<String, Double> getSecurityMetrics() { return securityMetrics; }
    public void setSecurityMetrics(Map<String, Double> securityMetrics) { this.securityMetrics = securityMetrics; }

    public Map<String, Double> getFraudMetrics() { return fraudMetrics; }
    public void setFraudMetrics(Map<String, Double> fraudMetrics) { this.fraudMetrics = fraudMetrics; }

    public Map<String, Double> getComplianceMetrics() { return complianceMetrics; }
    public void setComplianceMetrics(Map<String, Double> complianceMetrics) { this.complianceMetrics = complianceMetrics; }

    public List<Map<String, Object>> getSecurityIncidents() { return securityIncidents; }
    public void setSecurityIncidents(List<Map<String, Object>> securityIncidents) { this.securityIncidents = securityIncidents; }

    public List<Map<String, Object>> getFraudDetection() { return fraudDetection; }
    public void setFraudDetection(List<Map<String, Object>> fraudDetection) { this.fraudDetection = fraudDetection; }

    public List<Map<String, Object>> getComplianceMetricsList() { return complianceMetricsList; }
    public void setComplianceMetrics(List<Map<String, Object>> complianceMetrics) { this.complianceMetricsList = complianceMetrics; }
} 
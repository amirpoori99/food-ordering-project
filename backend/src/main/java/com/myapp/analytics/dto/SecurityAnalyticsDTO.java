package com.myapp.analytics.dto;

import java.util.Map;

public class SecurityAnalyticsDTO {
    private double incidentRate;
    private double threatLevel;
    private Map<String, Double> securityEvents;
    private Map<String, Double> securityTrends;

    public SecurityAnalyticsDTO() {}

    public double getIncidentRate() { return incidentRate; }
    public void setIncidentRate(double incidentRate) { this.incidentRate = incidentRate; }

    public double getThreatLevel() { return threatLevel; }
    public void setThreatLevel(double threatLevel) { this.threatLevel = threatLevel; }

    public Map<String, Double> getSecurityEvents() { return securityEvents; }
    public void setSecurityEvents(Map<String, Double> securityEvents) { this.securityEvents = securityEvents; }

    public Map<String, Double> getSecurityTrends() { return securityTrends; }
    public void setSecurityTrends(Map<String, Double> securityTrends) { this.securityTrends = securityTrends; }
} 
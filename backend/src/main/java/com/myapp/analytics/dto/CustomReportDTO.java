package com.myapp.analytics.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CustomReportDTO {
    private String reportName;
    private Map<String, Object> parameters;
    private List<Map<String, Object>> reportData;
    private LocalDateTime generatedAt;
    private long generatedAtTimestamp;
    private String reportType;

    public CustomReportDTO() {}

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public List<Map<String, Object>> getReportData() { return reportData; }
    public void setReportData(List<Map<String, Object>> reportData) { this.reportData = reportData; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public long getGeneratedAtTimestamp() { return generatedAtTimestamp; }
    public void setGeneratedAtTimestamp(long generatedAtTimestamp) { this.generatedAtTimestamp = generatedAtTimestamp; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
} 
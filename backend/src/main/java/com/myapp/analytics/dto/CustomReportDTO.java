package com.myapp.analytics.dto;

import java.util.Map;

public class CustomReportDTO {
    private String reportType;
    private Map<String, Object> data;
    private Map<String, Object> metadata;
    private long generatedAt;

    public CustomReportDTO() {
        this.generatedAt = System.currentTimeMillis();
    }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public long getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(long generatedAt) { this.generatedAt = generatedAt; }
} 
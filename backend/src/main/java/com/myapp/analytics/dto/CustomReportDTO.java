package com.myapp.analytics.dto;

import java.util.List;
import java.util.Map;

public class CustomReportDTO {
    private String reportName;
    private String data;
    private String format;
    private List<Map<String, Object>> customMetrics;
    private List<Map<String, Object>> reportData;
    private List<Map<String, Object>> reportFilters;

    public CustomReportDTO() {}

    public CustomReportDTO(String reportName, String data, String format,
                          List<Map<String, Object>> customMetrics,
                          List<Map<String, Object>> reportData,
                          List<Map<String, Object>> reportFilters) {
        this.reportName = reportName;
        this.data = data;
        this.format = format;
        this.customMetrics = customMetrics;
        this.reportData = reportData;
        this.reportFilters = reportFilters;
    }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public List<Map<String, Object>> getCustomMetrics() { return customMetrics; }
    public void setCustomMetrics(List<Map<String, Object>> customMetrics) { this.customMetrics = customMetrics; }
    
    public List<Map<String, Object>> getReportData() { return reportData; }
    public void setReportData(List<Map<String, Object>> reportData) { this.reportData = reportData; }
    
    public List<Map<String, Object>> getReportFilters() { return reportFilters; }
    public void setReportFilters(List<Map<String, Object>> reportFilters) { this.reportFilters = reportFilters; }
} 
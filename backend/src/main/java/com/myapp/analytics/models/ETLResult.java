package com.myapp.analytics.models;

import java.time.LocalDateTime;
import java.util.List;

/**
 * مدل نتیجه فرآیند ETL
 * این کلاس نتایج و آمار فرآیند Extract-Transform-Load را نگهداری می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class ETLResult {
    
    private String status; // SUCCESS, FAILED, RUNNING
    private LocalDateTime timestamp;
    private Long processingTime; // در میلی‌ثانیه
    private String errorMessage;
    
    // Extract Phase Results
    private Integer extractedOrders;
    private Integer extractedUsers;
    private Integer extractedRestaurants;
    private Integer extractedPayments;
    private Integer extractedReviews;
    
    // Transform Phase Results
    private Integer transformedRecords;
    private Integer cleanedRecords;
    private Integer rejectedRecords;
    private List<String> transformationErrors;
    
    // Load Phase Results
    private Integer loadedOrders;
    private Integer loadedUsers;
    private Integer loadedRestaurants;
    private Integer loadedPayments;
    private Integer loadedReviews;
    
    // Performance Metrics
    private Double extractionRate; // records per second
    private Double transformationRate;
    private Double loadingRate;
    private Long memoryUsage; // در بایت
    private Double cpuUsage; // درصد
    
    // Quality Metrics
    private Double dataQualityScore;
    private Integer duplicateRecords;
    private Integer incompleteRecords;
    private Integer invalidRecords;
    
    public ETLResult() {
        this.timestamp = LocalDateTime.now();
        this.status = "RUNNING";
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getProcessingTime() {
        return processingTime;
    }
    
    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getExtractedOrders() {
        return extractedOrders;
    }
    
    public void setExtractedOrders(Integer extractedOrders) {
        this.extractedOrders = extractedOrders;
    }
    
    public Integer getExtractedUsers() {
        return extractedUsers;
    }
    
    public void setExtractedUsers(Integer extractedUsers) {
        this.extractedUsers = extractedUsers;
    }
    
    public Integer getExtractedRestaurants() {
        return extractedRestaurants;
    }
    
    public void setExtractedRestaurants(Integer extractedRestaurants) {
        this.extractedRestaurants = extractedRestaurants;
    }
    
    public Integer getExtractedPayments() {
        return extractedPayments;
    }
    
    public void setExtractedPayments(Integer extractedPayments) {
        this.extractedPayments = extractedPayments;
    }
    
    public Integer getLoadedOrders() {
        return loadedOrders;
    }
    
    public void setLoadedOrders(Integer loadedOrders) {
        this.loadedOrders = loadedOrders;
    }
    
    public Integer getLoadedUsers() {
        return loadedUsers;
    }
    
    public void setLoadedUsers(Integer loadedUsers) {
        this.loadedUsers = loadedUsers;
    }
    
    public Integer getLoadedRestaurants() {
        return loadedRestaurants;
    }
    
    public void setLoadedRestaurants(Integer loadedRestaurants) {
        this.loadedRestaurants = loadedRestaurants;
    }
    
    public Integer getLoadedPayments() {
        return loadedPayments;
    }
    
    public void setLoadedPayments(Integer loadedPayments) {
        this.loadedPayments = loadedPayments;
    }
    
    // Additional getters and setters
    public Integer getTransformedRecords() { return transformedRecords; }
    public void setTransformedRecords(Integer transformedRecords) { this.transformedRecords = transformedRecords; }
    
    public Integer getCleanedRecords() { return cleanedRecords; }
    public void setCleanedRecords(Integer cleanedRecords) { this.cleanedRecords = cleanedRecords; }
    
    public Integer getRejectedRecords() { return rejectedRecords; }
    public void setRejectedRecords(Integer rejectedRecords) { this.rejectedRecords = rejectedRecords; }
    
    public Double getDataQualityScore() { return dataQualityScore; }
    public void setDataQualityScore(Double dataQualityScore) { this.dataQualityScore = dataQualityScore; }
    
    public Integer getDuplicateRecords() { return duplicateRecords; }
    public void setDuplicateRecords(Integer duplicateRecords) { this.duplicateRecords = duplicateRecords; }
    
    /**
     * محاسبه نرخ موفقیت کلی ETL
     */
    public Double calculateSuccessRate() {
        int totalExtracted = (extractedOrders != null ? extractedOrders : 0) +
                           (extractedUsers != null ? extractedUsers : 0) +
                           (extractedRestaurants != null ? extractedRestaurants : 0) +
                           (extractedPayments != null ? extractedPayments : 0);
                           
        int totalLoaded = (loadedOrders != null ? loadedOrders : 0) +
                         (loadedUsers != null ? loadedUsers : 0) +
                         (loadedRestaurants != null ? loadedRestaurants : 0) +
                         (loadedPayments != null ? loadedPayments : 0);
        
        if (totalExtracted > 0) {
            return ((double) totalLoaded / totalExtracted) * 100;
        }
        return 0.0;
    }
    
    /**
     * محاسبه throughput کلی (records per second)
     */
    public Double calculateOverallThroughput() {
        int totalProcessed = (transformedRecords != null ? transformedRecords : 0);
        if (processingTime != null && processingTime > 0 && totalProcessed > 0) {
            return ((double) totalProcessed) / (processingTime / 1000.0);
        }
        return 0.0;
    }
    
    /**
     * بررسی اینکه آیا ETL موفق بوده یا نه
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }
    
    /**
     * دریافت خلاصه نتایج ETL
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("ETL Process Summary:\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Processing Time: ").append(processingTime).append(" ms\n");
        summary.append("Success Rate: ").append(String.format("%.2f", calculateSuccessRate())).append("%\n");
        summary.append("Overall Throughput: ").append(String.format("%.2f", calculateOverallThroughput())).append(" records/sec\n");
        
        if (errorMessage != null) {
            summary.append("Error: ").append(errorMessage).append("\n");
        }
        
        return summary.toString();
    }
} 
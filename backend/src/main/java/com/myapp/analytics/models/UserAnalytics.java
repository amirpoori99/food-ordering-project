package com.myapp.analytics.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * مدل Analytics برای کاربران در Data Warehouse
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
@Entity
@Table(name = "user_analytics")
public class UserAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_role", length = 50)
    private String userRole;
    
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    // Order Metrics
    @Column(name = "total_orders")
    private Integer totalOrders;
    
    @Column(name = "total_spent", precision = 12, scale = 2)
    private Double totalSpent;
    
    @Column(name = "average_order_value", precision = 10, scale = 2)
    private Double averageOrderValue;
    
    @Column(name = "last_order_date")
    private LocalDateTime lastOrderDate;
    
    // Customer Segmentation
    @Column(name = "customer_segment", length = 20)
    private String customerSegment; // NEW, OCCASIONAL, REGULAR, FREQUENT, VIP
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public UserAnalytics() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    
    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
    
    public Double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    public void setLastOrderDate(LocalDateTime lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    
    public String getCustomerSegment() { return customerSegment; }
    public void setCustomerSegment(String customerSegment) { this.customerSegment = customerSegment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 
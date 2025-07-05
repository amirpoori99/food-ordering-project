package com.myapp.analytics.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * مدل Analytics برای سفارشات در Data Warehouse
 * این کلاس داده‌های تحلیلی سفارشات را برای BI نگهداری می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
@Entity
@Table(name = "order_analytics")
public class OrderAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
    
    @Column(name = "status", length = 50)
    private String status;
    
    // Financial Metrics
    @Column(name = "total_amount", precision = 10, scale = 2)
    private Double totalAmount;
    
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private Double deliveryFee;
    
    @Column(name = "tax", precision = 10, scale = 2)
    private Double tax;
    
    @Column(name = "discount", precision = 10, scale = 2)
    private Double discount;
    
    @Column(name = "net_amount", precision = 10, scale = 2)
    private Double netAmount;
    
    // Time Dimensions
    @Column(name = "hour_of_day")
    private Integer hourOfDay;
    
    @Column(name = "day_of_week")
    private Integer dayOfWeek;
    
    @Column(name = "month")
    private Integer month;
    
    @Column(name = "year")
    private Integer year;
    
    // Performance Metrics
    @Column(name = "delivery_time")
    private Long deliveryTime; // minutes
    
    @Column(name = "item_count")
    private Integer itemCount;
    
    @Column(name = "order_value_category", length = 20)
    private String orderValueCategory; // LOW, MEDIUM, HIGH, PREMIUM
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public OrderAnalytics() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getRestaurantId() {
        return restaurantId;
    }
    
    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Double getDeliveryFee() {
        return deliveryFee;
    }
    
    public void setDeliveryFee(Double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
    
    public Double getTax() {
        return tax;
    }
    
    public void setTax(Double tax) {
        this.tax = tax;
    }
    
    public Double getDiscount() {
        return discount;
    }
    
    public void setDiscount(Double discount) {
        this.discount = discount;
    }
    
    public Double getNetAmount() {
        return netAmount;
    }
    
    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }
    
    public Integer getHourOfDay() {
        return hourOfDay;
    }
    
    public void setHourOfDay(Integer hourOfDay) {
        this.hourOfDay = hourOfDay;
    }
    
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Long getDeliveryTime() {
        return deliveryTime;
    }
    
    public void setDeliveryTime(Long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    
    public Integer getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
    
    public String getOrderValueCategory() {
        return orderValueCategory;
    }
    
    public void setOrderValueCategory(String orderValueCategory) {
        this.orderValueCategory = orderValueCategory;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "OrderAnalytics{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", userId=" + userId +
                ", restaurantId=" + restaurantId +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", orderDate=" + orderDate +
                '}';
    }
} 
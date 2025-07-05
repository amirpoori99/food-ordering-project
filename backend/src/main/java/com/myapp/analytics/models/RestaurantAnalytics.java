package com.myapp.analytics.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * مدل Analytics برای رستوران‌ها
 * 
 * @author Food Ordering System Team
 */
@Entity
@Table(name = "restaurant_analytics")
public class RestaurantAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;
    
    @Column(name = "name", length = 200)
    private String name;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;
    
    @Column(name = "total_orders")
    private Integer totalOrders;
    
    @Column(name = "total_revenue", precision = 12, scale = 2)
    private Double totalRevenue;
    
    @Column(name = "average_rating", precision = 3, scale = 2)
    private Double averageRating;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public RestaurantAnalytics() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 
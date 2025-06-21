 package com.myapp.common.models;

import jakarta.persistence.*;

/**
 * OrderItem Entity - Represents individual items within an order
 * Junction table between Order and FoodItem with quantity and price
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "price", nullable = false)
    private Double price; // Price at time of order (for historical accuracy)
    
    // Constructors
    public OrderItem() {}
    
    public OrderItem(Order order, FoodItem foodItem, Integer quantity, Double price) {
        this.order = order;
        this.foodItem = foodItem;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Business methods
    public Double getSubtotal() {
        return price * quantity;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
} 
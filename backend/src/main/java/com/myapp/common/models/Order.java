package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Entity - Represents a customer order
 * Contains order details, items, status, and relationships with User and Restaurant
 */
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;
    
    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;
    
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
    
    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;
    
    // Constructors
    public Order() {}
    
    private Order(User customer, Restaurant restaurant, String deliveryAddress, String phone) {
        if (customer == null || restaurant == null) {
            throw new IllegalArgumentException("Customer and restaurant are required");
        }
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        this.customer = customer;
        this.restaurant = restaurant;
        this.deliveryAddress = deliveryAddress;
        this.phone = phone;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0.0;
    }
    
    // Factory methods
    public static Order createNew(User customer, Restaurant restaurant, String deliveryAddress, String phone) {
        return new Order(customer, restaurant, deliveryAddress, phone);
    }
    
    // Business methods
    public void addItem(FoodItem foodItem, Integer quantity) {
        if (foodItem == null || quantity <= 0) {
            throw new IllegalArgumentException("Food item and quantity must be valid");
        }
        
        if (!foodItem.isInStock() || foodItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough quantity available");
        }
        
        // Check if item already exists in order
        OrderItem existingItem = findOrderItemByFoodItem(foodItem);
        if (existingItem != null) {
            // Update existing item quantity
            int newQuantity = existingItem.getQuantity() + quantity;
            if (foodItem.getQuantity() < newQuantity) {
                throw new IllegalArgumentException("Not enough quantity available");
            }
            existingItem.setQuantity(newQuantity);
        } else {
            // Add new item
            OrderItem orderItem = new OrderItem(this, foodItem, quantity, foodItem.getPrice());
            orderItems.add(orderItem);
        }
        recalculateTotal();
    }
    
    public void removeItem(OrderItem orderItem) {
        if (orderItems.remove(orderItem)) {
            recalculateTotal();
        }
    }
    
    public void removeItem(FoodItem foodItem) {
        OrderItem orderItem = findOrderItemByFoodItem(foodItem);
        if (orderItem != null) {
            removeItem(orderItem);
        }
    }
    
    private OrderItem findOrderItemByFoodItem(FoodItem foodItem) {
        return orderItems.stream()
                .filter(item -> item.getFoodItem().equals(foodItem))
                .findFirst()
                .orElse(null);
    }
    
    public void recalculateTotal() {
        this.totalAmount = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    public void confirm() {
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm empty order");
        }
        this.status = OrderStatus.CONFIRMED;
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(30);
        
        // Decrease food item quantities
        for (OrderItem item : orderItems) {
            item.getFoodItem().decreaseQuantity(item.getQuantity());
        }
    }
    
    public void cancel() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel order in current status: " + status);
        }
        
        // Restore food item quantities if order was confirmed
        if (status == OrderStatus.CONFIRMED || status == OrderStatus.PREPARING) {
            for (OrderItem item : orderItems) {
                item.getFoodItem().increaseQuantity(item.getQuantity());
            }
        }
        
        this.status = OrderStatus.CANCELLED;
    }
    
    public void markAsDelivered() {
        if (status != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("Order must be out for delivery to mark as delivered");
        }
        this.status = OrderStatus.DELIVERED;
        this.actualDeliveryTime = LocalDateTime.now();
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || 
               status == OrderStatus.CONFIRMED || 
               status == OrderStatus.PREPARING;
    }
    
    public int getTotalItems() {
        return orderItems.stream().mapToInt(OrderItem::getQuantity).sum();
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    
    public Restaurant getRestaurant() { return restaurant; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) { 
        this.estimatedDeliveryTime = estimatedDeliveryTime; 
    }
    
    public LocalDateTime getActualDeliveryTime() { return actualDeliveryTime; }
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) { 
        this.actualDeliveryTime = actualDeliveryTime; 
    }
}
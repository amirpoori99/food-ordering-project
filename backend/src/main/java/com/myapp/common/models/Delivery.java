package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entity representing a delivery in the food ordering system
 */
@Entity
@Table(name = "deliveries")
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private User courier;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;
    
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
    
    @Column(name = "delivery_notes", length = 1000)
    private String deliveryNotes;
    
    @Column(name = "courier_notes", length = 1000)
    private String courierNotes;
    
    @Column(name = "delivery_fee", nullable = false)
    private Double deliveryFee;
    
    @Column(name = "distance_km")
    private Double distanceKm;
    
    // Constructors
    public Delivery() {
    }
    
    public Delivery(Order order, Double deliveryFee) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (deliveryFee == null || deliveryFee < 0) {
            throw new IllegalArgumentException("Delivery fee must be non-negative");
        }
        
        this.order = order;
        this.deliveryFee = deliveryFee;
        this.status = DeliveryStatus.PENDING;
        this.estimatedPickupTime = LocalDateTime.now().plusMinutes(15); // Default 15 minutes
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(45); // Default 45 minutes
    }
    
    // Factory methods
    public static Delivery createNew(Order order, Double deliveryFee) {
        return new Delivery(order, deliveryFee);
    }
    
    public static Delivery createWithDistance(Order order, Double deliveryFee, Double distanceKm) {
        Delivery delivery = new Delivery(order, deliveryFee);
        delivery.setDistanceKm(distanceKm);
        // Adjust estimated times based on distance
        if (distanceKm != null) {
            int extraMinutes = (int) (distanceKm * 3); // 3 minutes per km
            delivery.setEstimatedPickupTime(LocalDateTime.now().plusMinutes(15));
            delivery.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30 + extraMinutes));
        }
        return delivery;
    }
    
    // Business methods
    public void assignToCourier(User courier) {
        if (courier == null) {
            throw new IllegalArgumentException("Courier cannot be null");
        }
        if (!User.Role.COURIER.equals(courier.getRole())) {
            throw new IllegalArgumentException("User must have COURIER role");
        }
        if (!canBeAssigned()) {
            throw new IllegalStateException("Can only assign courier to pending deliveries");
        }
        
        this.courier = courier;
        this.status = DeliveryStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
        this.estimatedPickupTime = LocalDateTime.now().plusMinutes(10); // Update pickup time
    }
    
    public void markAsPickedUp() {
        if (!DeliveryStatus.ASSIGNED.equals(this.status)) {
            throw new IllegalStateException("Delivery must be assigned before pickup");
        }
        if (this.courier == null) {
            throw new IllegalStateException("No courier assigned");
        }
        
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(20); // Update delivery time
        
        // Update order status
        if (this.order != null) {
            this.order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        }
    }
    
    public void markAsDelivered() {
        if (!DeliveryStatus.PICKED_UP.equals(this.status)) {
            throw new IllegalStateException("Delivery must be picked up before marking as delivered");
        }
        
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        
        // Update order status and delivery time
        if (this.order != null) {
            this.order.setStatus(OrderStatus.DELIVERED);
            this.order.setActualDeliveryTime(this.deliveredAt);
        }
    }
    
    public void cancel(String reason) {
        if (DeliveryStatus.DELIVERED.equals(this.status)) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }
        
        this.status = DeliveryStatus.CANCELLED;
        this.deliveryNotes = reason;
        
        // Unassign courier if assigned
        if (!DeliveryStatus.PENDING.equals(this.status)) {
            this.courier = null;
            this.assignedAt = null;
        }
    }
    
    // State checking methods
    public boolean canBeAssigned() {
        return DeliveryStatus.PENDING.equals(this.status);
    }
    
    public boolean canBePickedUp() {
        return DeliveryStatus.ASSIGNED.equals(this.status) && this.courier != null;
    }
    
    public boolean canBeDelivered() {
        return DeliveryStatus.PICKED_UP.equals(this.status);
    }
    
    public boolean isActive() {
        return DeliveryStatus.PENDING.equals(this.status) ||
               DeliveryStatus.ASSIGNED.equals(this.status) ||
               DeliveryStatus.PICKED_UP.equals(this.status);
    }
    
    // Utility methods
    public int getEstimatedPickupMinutes() {
        if (estimatedPickupTime == null) {
            return 0;
        }
        return (int) ChronoUnit.MINUTES.between(LocalDateTime.now(), estimatedPickupTime);
    }
    
    public int getEstimatedDeliveryMinutes() {
        if (estimatedDeliveryTime == null) {
            return 0;
        }
        return (int) ChronoUnit.MINUTES.between(LocalDateTime.now(), estimatedDeliveryTime);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public User getCourier() {
        return courier;
    }
    
    public void setCourier(User courier) {
        this.courier = courier;
    }
    
    public DeliveryStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }
    
    public void setPickedUpAt(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }
    
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    public LocalDateTime getEstimatedPickupTime() {
        return estimatedPickupTime;
    }
    
    public void setEstimatedPickupTime(LocalDateTime estimatedPickupTime) {
        this.estimatedPickupTime = estimatedPickupTime;
    }
    
    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }
    
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
    
    public String getDeliveryNotes() {
        return deliveryNotes;
    }
    
    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
    
    public String getCourierNotes() {
        return courierNotes;
    }
    
    public void setCourierNotes(String courierNotes) {
        this.courierNotes = courierNotes;
    }
    
    public Double getDeliveryFee() {
        return deliveryFee;
    }
    
    public void setDeliveryFee(Double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    @Override
    public String toString() {
        return "Delivery{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", courierId=" + (courier != null ? courier.getId() : null) +
                ", status=" + status +
                ", deliveryFee=" + deliveryFee +
                '}';
    }
}
package com.myapp.common.models;

/**
 * Enumeration representing the status of a delivery
 */
public enum DeliveryStatus {
    /**
     * Delivery request has been created but no courier assigned yet
     */
    PENDING,
    
    /**
     * A courier has been assigned to the delivery
     */
    ASSIGNED,
    
    /**
     * Courier has picked up the order from restaurant
     */
    PICKED_UP,
    
    /**
     * Order has been delivered to customer
     */
    DELIVERED,
    
    /**
     * Delivery has been cancelled
     */
    CANCELLED
}
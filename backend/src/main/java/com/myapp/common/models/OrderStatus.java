 package com.myapp.common.models;

/**
 * Order Status Enum - Represents different states of an order
 */
public enum OrderStatus {
    PENDING,        // Order created but not confirmed
    CONFIRMED,      // Order confirmed by customer
    PREPARING,      // Restaurant is preparing the order
    READY,          // Order is ready for pickup/delivery
    OUT_FOR_DELIVERY, // Order is being delivered
    DELIVERED,      // Order has been delivered
    CANCELLED       // Order was cancelled
} 
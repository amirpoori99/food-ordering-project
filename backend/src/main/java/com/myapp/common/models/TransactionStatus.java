package com.myapp.common.models;

/**
 * Transaction status lifecycle
 */
public enum TransactionStatus {
    PENDING,    // Transaction initiated but not processed
    COMPLETED,  // Transaction successfully completed
    FAILED,     // Transaction failed due to payment issues
    CANCELLED   // Transaction cancelled by user or system
} 

package com.myapp.common.models;

/**
 * Transaction types supported by the system
 */
public enum TransactionType {
    PAYMENT,          // Payment for an order
    REFUND,           // Refund for a cancelled/returned order
    WALLET_CHARGE,    // Adding money to wallet
    WALLET_WITHDRAWAL // Withdrawing money from wallet
} 
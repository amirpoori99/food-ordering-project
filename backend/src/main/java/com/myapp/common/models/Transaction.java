package com.myapp.common.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing payment transactions in the system
 * Supports various transaction types: PAYMENT, REFUND, WALLET_CHARGE, WALLET_WITHDRAWAL
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "order_id")
    private Long orderId; // null for wallet operations
    
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // CARD, WALLET, CASH_ON_DELIVERY
    
    @Column(name = "reference_id", length = 100)
    private String referenceId; // Bank reference or gateway transaction ID
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // Default constructor
    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }
    
    // Constructor for payment transactions
    public Transaction(Long userId, Long orderId, Double amount, TransactionType type, String paymentMethod) {
        this();
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.type = type;
        this.paymentMethod = paymentMethod;
    }
    
    // Constructor for wallet transactions
    public Transaction(Long userId, Double amount, TransactionType type, String description) {
        this();
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.paymentMethod = "WALLET";
    }
    
    // Factory methods
    public static Transaction forPayment(Long userId, Long orderId, Double amount, String paymentMethod) {
        return new Transaction(userId, orderId, amount, TransactionType.PAYMENT, paymentMethod);
    }
    
    public static Transaction forRefund(Long userId, Long orderId, Double amount, String reason) {
        Transaction transaction = new Transaction(userId, orderId, amount, TransactionType.REFUND, "REFUND");
        transaction.setDescription("Refund: " + reason);
        return transaction;
    }
    
    public static Transaction forWalletCharge(Long userId, Double amount, String paymentMethod) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.amount = amount;
        transaction.type = TransactionType.WALLET_CHARGE;
        transaction.paymentMethod = paymentMethod;
        transaction.description = "Wallet charge";
        return transaction;
    }
    
    public static Transaction forWalletWithdrawal(Long userId, Double amount, String description) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.amount = amount;
        transaction.type = TransactionType.WALLET_WITHDRAWAL;
        transaction.paymentMethod = "WALLET";
        transaction.description = description;
        return transaction;
    }
    
    // Business logic methods
    public void markAsCompleted(String referenceId) {
        this.status = TransactionStatus.COMPLETED;
        this.referenceId = referenceId;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.description = (this.description != null ? this.description + ". " : "") + "Failed: " + reason;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markAsCancelled(String reason) {
        this.status = TransactionStatus.CANCELLED;
        this.description = (this.description != null ? this.description + ". " : "") + "Cancelled: " + reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }
    
    public boolean isCompleted() {
        return this.status == TransactionStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return this.status == TransactionStatus.FAILED;
    }
    
    public boolean isCancelled() {
        return this.status == TransactionStatus.CANCELLED;
    }
    
    public boolean isWalletTransaction() {
        return this.type == TransactionType.WALLET_CHARGE || this.type == TransactionType.WALLET_WITHDRAWAL;
    }
    
    public boolean isPaymentTransaction() {
        return this.type == TransactionType.PAYMENT || this.type == TransactionType.REFUND;
    }
    
    public Double getEffectiveAmount() {
        // For withdrawals and refunds, amount is negative from wallet perspective
        if (type == TransactionType.WALLET_WITHDRAWAL || type == TransactionType.REFUND) {
            return -amount;
        }
        return amount;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    /**
     * تنظیم مبلغ با BigDecimal - برای سازگاری با تست‌ها
     * 
     * @param amount مبلغ تراکنش
     */
    public void setAmount(BigDecimal amount) {
        if (amount != null) {
            this.amount = amount.doubleValue();
        } else {
            this.amount = null;
        }
    }
    
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderId=" + orderId +
                ", amount=" + amount +
                ", type=" + type +
                ", status=" + status +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}



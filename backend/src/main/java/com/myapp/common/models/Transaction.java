package com.myapp.common.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * مدل تراکنش مالی - نماینده تراکنش‌های پرداخت در سیستم سفارش غذا
 * 
 * این کلاس سیستم کامل مدیریت تراکنش‌های مالی را پیاده‌سازی می‌کند:
 * 
 * === انواع تراکنش ===
 * - PAYMENT: پرداخت سفارش
 * - REFUND: استرداد وجه
 * - WALLET_CHARGE: شارژ کیف پول
 * - WALLET_WITHDRAWAL: برداشت از کیف پول
 * 
 * === وضعیت‌های تراکنش ===
 * - PENDING: در انتظار پردازش
 * - COMPLETED: تکمیل شده
 * - FAILED: ناموفق
 * - CANCELLED: لغو شده
 * 
 * === ویژگی‌های کلیدی ===
 * - پشتیبانی از روش‌های مختلف پرداخت
 * - ردیابی کامل تراکنش با reference ID
 * - audit trail برای حسابداری
 * - Factory methods برای ایجاد انواع تراکنش
 * - business logic methods برای مدیریت وضعیت
 * 
 * === قوانین کسب‌وکار ===
 * - تراکنش‌های کیف پول orderId ندارند
 * - تراکنش‌های REFUND و WITHDRAWAL از نظر کیف پول منفی هستند
 * - هر تراکنش reference ID یکتا دارد
 * - زمان‌بندی دقیق برای audit
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "transactions")
public class Transaction {
    
    /** شناسه یکتای تراکنش */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    /** شناسه کاربر مرتبط با تراکنش */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /** شناسه سفارش (null برای تراکنش‌های کیف پول) */
    @Column(name = "order_id")
    private Long orderId;
    
    /** مبلغ تراکنش */
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    /** نوع تراکنش */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    /** وضعیت تراکنش */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    /** روش پرداخت (CARD, WALLET, CASH_ON_DELIVERY) */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    /** شناسه مرجع بانک یا درگاه پرداخت */
    @Column(name = "reference_id", length = 100)
    private String referenceId;
    
    /** توضیحات تراکنش */
    @Column(name = "description", length = 500)
    private String description;
    
    /** زمان ایجاد تراکنش */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /** زمان آخرین به‌روزرسانی */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /** زمان پردازش تراکنش */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض
     * زمان ایجاد و وضعیت اولیه را تنظیم می‌کند
     */
    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
    }
    
    /**
     * سازنده برای تراکنش‌های پرداخت سفارش
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param amount مبلغ
     * @param type نوع تراکنش
     * @param paymentMethod روش پرداخت
     */
    public Transaction(Long userId, Long orderId, Double amount, TransactionType type, String paymentMethod) {
        this();
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.type = type;
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * سازنده برای تراکنش‌های کیف پول
     * 
     * @param userId شناسه کاربر
     * @param amount مبلغ
     * @param type نوع تراکنش
     * @param description توضیحات
     */
    public Transaction(Long userId, Double amount, TransactionType type, String description) {
        this();
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.paymentMethod = "WALLET";
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * ایجاد تراکنش پرداخت سفارش
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param amount مبلغ
     * @param paymentMethod روش پرداخت
     * @return تراکنش پرداخت ایجاد شده
     */
    public static Transaction forPayment(Long userId, Long orderId, Double amount, String paymentMethod) {
        return new Transaction(userId, orderId, amount, TransactionType.PAYMENT, paymentMethod);
    }
    
    /**
     * ایجاد تراکنش استرداد
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param amount مبلغ استرداد
     * @param reason دلیل استرداد
     * @return تراکنش استرداد ایجاد شده
     */
    public static Transaction forRefund(Long userId, Long orderId, Double amount, String reason) {
        Transaction transaction = new Transaction(userId, orderId, amount, TransactionType.REFUND, "REFUND");
        transaction.setDescription("استرداد: " + reason);
        return transaction;
    }
    
    /**
     * ایجاد تراکنش شارژ کیف پول
     * 
     * @param userId شناسه کاربر
     * @param amount مبلغ شارژ
     * @param paymentMethod روش پرداخت
     * @return تراکنش شارژ ایجاد شده
     */
    public static Transaction forWalletCharge(Long userId, Double amount, String paymentMethod) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.amount = amount;
        transaction.type = TransactionType.WALLET_CHARGE;
        transaction.paymentMethod = paymentMethod;
        transaction.description = "شارژ کیف پول";
        return transaction;
    }
    
    /**
     * ایجاد تراکنش برداشت از کیف پول
     * 
     * @param userId شناسه کاربر
     * @param amount مبلغ برداشت
     * @param description توضیحات
     * @return تراکنش برداشت ایجاد شده
     */
    public static Transaction forWalletWithdrawal(Long userId, Double amount, String description) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.amount = amount;
        transaction.type = TransactionType.WALLET_WITHDRAWAL;
        transaction.paymentMethod = "WALLET";
        transaction.description = description;
        return transaction;
    }
    
    // ==================== BUSINESS LOGIC METHODS ====================
    
    /**
     * علامت‌گذاری تراکنش به عنوان تکمیل شده
     * 
     * @param referenceId شناسه مرجع تراکنش
     */
    public void markAsCompleted(String referenceId) {
        this.status = TransactionStatus.COMPLETED;
        this.referenceId = referenceId;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * علامت‌گذاری تراکنش به عنوان ناموفق
     * 
     * @param reason دلیل شکست
     */
    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.description = (this.description != null ? this.description + ". " : "") + "ناموفق: " + reason;
        this.processedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * علامت‌گذاری تراکنش به عنوان لغو شده
     * 
     * @param reason دلیل لغو
     */
    public void markAsCancelled(String reason) {
        this.status = TransactionStatus.CANCELLED;
        this.description = (this.description != null ? this.description + ". " : "") + "لغو شده: " + reason;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * بررسی وضعیت در انتظار
     * 
     * @return true اگر تراکنش در انتظار باشد
     */
    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }
    
    /**
     * بررسی وضعیت تکمیل شده
     * 
     * @return true اگر تراکنش تکمیل شده باشد
     */
    public boolean isCompleted() {
        return this.status == TransactionStatus.COMPLETED;
    }
    
    /**
     * بررسی وضعیت ناموفق
     * 
     * @return true اگر تراکنش ناموفق باشد
     */
    public boolean isFailed() {
        return this.status == TransactionStatus.FAILED;
    }
    
    /**
     * بررسی وضعیت لغو شده
     * 
     * @return true اگر تراکنش لغو شده باشد
     */
    public boolean isCancelled() {
        return this.status == TransactionStatus.CANCELLED;
    }
    
    /**
     * بررسی تراکنش کیف پول
     * 
     * @return true اگر مربوط به کیف پول باشد
     */
    public boolean isWalletTransaction() {
        return this.type == TransactionType.WALLET_CHARGE || this.type == TransactionType.WALLET_WITHDRAWAL;
    }
    
    /**
     * بررسی تراکنش پرداخت
     * 
     * @return true اگر مربوط به پرداخت یا استرداد باشد
     */
    public boolean isPaymentTransaction() {
        return this.type == TransactionType.PAYMENT || this.type == TransactionType.REFUND;
    }
    
    /**
     * محاسبه مبلغ مؤثر از نظر کیف پول
     * 
     * @return مبلغ مثبت یا منفی بر اساس نوع تراکنش
     */
    public Double getEffectiveAmount() {
        // برای برداشت و استرداد، مبلغ منفی است
        if (type == TransactionType.WALLET_WITHDRAWAL || type == TransactionType.REFUND) {
            return -amount;
        }
        return amount;
    }
    
    /**
     * callback قبل از به‌روزرسانی entity
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه تراکنش */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    /** @return شناسه کاربر */
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    /** @return شناسه سفارش */
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    /** @return مبلغ تراکنش */
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
    
    /** @return نوع تراکنش */
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    
    /** @return وضعیت تراکنش */
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    
    /** @return روش پرداخت */
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    /** @return شناسه مرجع */
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    
    /** @return توضیحات تراکنش */
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    /** @return زمان ایجاد */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    /** @return زمان به‌روزرسانی */
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    /** @return زمان پردازش */
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    /**
     * نمایش رشته‌ای تراکنش
     * 
     * @return اطلاعات کلیدی برای debugging
     */
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



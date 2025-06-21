package com.myapp.payment;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionStatus;
import com.myapp.common.models.TransactionType;
import com.myapp.common.models.Order;
import com.myapp.common.models.User;
import com.myapp.auth.AuthRepository;
import com.myapp.order.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for payment processing and transaction management
 * Handles payment validation, processing, refunds, and transaction history
 */
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final AuthRepository authRepository;
    private final OrderRepository orderRepository;
    
    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
        this.authRepository = new AuthRepository();
        this.orderRepository = new OrderRepository();
    }
    
    public PaymentService(PaymentRepository paymentRepository, AuthRepository authRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.authRepository = authRepository;
        this.orderRepository = orderRepository;
    }
    
    // ==================== PAYMENT PROCESSING ====================
    
    /**
     * Process payment for an order
     */
    public Transaction processPayment(Long userId, Long orderId, String paymentMethod) {
        // Validate inputs
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be empty");
        }
        
        // Validate user exists
        Optional<User> userOpt = authRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User", userId);
        }
        
        // Validate order exists and belongs to user
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        if (!order.getCustomer().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to the specified user");
        }
        
        // Check if order is in correct status for payment
        if (order.getStatus() != com.myapp.common.models.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order is not in a payable state");
        }
        
        // Check if payment already exists for this order
        List<Transaction> existingPayments = paymentRepository.findByOrderId(orderId);
        Optional<Transaction> completedPayment = existingPayments.stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED && t.getType() == TransactionType.PAYMENT)
            .findFirst();
        
        if (completedPayment.isPresent()) {
            throw new IllegalArgumentException("Payment already completed for this order");
        }
        
        // Create payment transaction
        Transaction payment = Transaction.forPayment(userId, orderId, order.getTotalAmount(), paymentMethod);
        payment = paymentRepository.save(payment);
        
        // Process payment based on method
        try {
            switch (paymentMethod.toUpperCase()) {
                case "WALLET":
                    processWalletPayment(payment);
                    break;
                case "CARD":
                    processCardPayment(payment);
                    break;
                case "CASH_ON_DELIVERY":
                    processCashOnDeliveryPayment(payment);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
            }
        } catch (Exception e) {
            payment.markAsFailed(e.getMessage());
            paymentRepository.update(payment);
            throw e;
        }
        
        return payment;
    }
    
    /**
     * Process wallet payment
     */
    private void processWalletPayment(Transaction payment) {
        // Check wallet balance
        Double walletBalance = paymentRepository.calculateWalletBalance(payment.getUserId());
        if (walletBalance < payment.getAmount()) {
            throw new IllegalArgumentException("Insufficient wallet balance. Balance: " + walletBalance + ", Required: " + payment.getAmount());
        }
        
        // Create wallet withdrawal transaction
        Transaction withdrawal = Transaction.forWalletWithdrawal(
            payment.getUserId(), 
            payment.getAmount(), 
            "Payment for order #" + payment.getOrderId()
        );
        withdrawal.markAsCompleted("WALLET_" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(withdrawal);
        
        // Mark payment as completed
        payment.markAsCompleted("WALLET_PAYMENT_" + payment.getId());
        paymentRepository.update(payment);
    }
    
    /**
     * Process card payment (simulation)
     */
    private void processCardPayment(Transaction payment) {
        // Simulate card payment processing
        String referenceId = "CARD_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Simulate payment gateway response (90% success rate)
        if (Math.random() < 0.9) {
            payment.markAsCompleted(referenceId);
        } else {
            payment.markAsFailed("Card payment declined by bank");
        }
        
        paymentRepository.update(payment);
    }
    
    /**
     * Process cash on delivery payment
     */
    private void processCashOnDeliveryPayment(Transaction payment) {
        // COD payments are marked as pending until delivery
        payment.setDescription("Cash on delivery - payment pending until delivery");
        paymentRepository.update(payment);
    }
    
    // ==================== REFUND PROCESSING ====================
    
    /**
     * Process refund for a payment
     */
    public Transaction processRefund(Long paymentId, String reason) {
        if (paymentId == null || paymentId <= 0) {
            throw new IllegalArgumentException("Payment ID must be positive");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Refund reason cannot be empty");
        }
        
        // Find original payment
        Optional<Transaction> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new NotFoundException("Payment transaction", paymentId);
        }
        
        Transaction originalPayment = paymentOpt.get();
        
        // Validate payment is refundable
        if (originalPayment.getType() != TransactionType.PAYMENT) {
            throw new IllegalArgumentException("Only payment transactions can be refunded");
        }
        if (originalPayment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalArgumentException("Only completed payments can be refunded");
        }
        
        // Check if already refunded
        List<Transaction> existingRefunds = paymentRepository.findByOrderId(originalPayment.getOrderId());
        Optional<Transaction> completedRefund = existingRefunds.stream()
            .filter(t -> t.getStatus() == TransactionStatus.COMPLETED && t.getType() == TransactionType.REFUND)
            .findFirst();
        
        if (completedRefund.isPresent()) {
            throw new IllegalArgumentException("Payment already refunded");
        }
        
        // Create refund transaction
        Transaction refund = Transaction.forRefund(
            originalPayment.getUserId(), 
            originalPayment.getOrderId(), 
            originalPayment.getAmount(), 
            reason
        );
        refund = paymentRepository.save(refund);
        
        // Process refund based on original payment method
        try {
            switch (originalPayment.getPaymentMethod().toUpperCase()) {
                case "WALLET":
                    processWalletRefund(refund);
                    break;
                case "CARD":
                    processCardRefund(refund);
                    break;
                case "CASH_ON_DELIVERY":
                    processCashRefund(refund);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot process refund for payment method: " + originalPayment.getPaymentMethod());
            }
        } catch (Exception e) {
            refund.markAsFailed(e.getMessage());
            paymentRepository.update(refund);
            throw e;
        }
        
        return refund;
    }
    
    /**
     * Process wallet refund
     */
    private void processWalletRefund(Transaction refund) {
        // Create wallet charge transaction for refund
        Transaction walletCharge = Transaction.forWalletCharge(
            refund.getUserId(), 
            refund.getAmount(), 
            "REFUND"
        );
        walletCharge.setDescription("Refund for order #" + refund.getOrderId());
        walletCharge.markAsCompleted("REFUND_" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(walletCharge);
        
        // Mark refund as completed
        refund.markAsCompleted("WALLET_REFUND_" + refund.getId());
        paymentRepository.update(refund);
    }
    
    /**
     * Process card refund (simulation)
     */
    private void processCardRefund(Transaction refund) {
        // Simulate card refund processing
        String referenceId = "REFUND_" + UUID.randomUUID().toString().substring(0, 8);
        refund.markAsCompleted(referenceId);
        paymentRepository.update(refund);
    }
    
    /**
     * Process cash refund
     */
    private void processCashRefund(Transaction refund) {
        // Cash refunds need manual processing
        refund.setDescription(refund.getDescription() + " - Manual cash refund required");
        refund.markAsCompleted("CASH_REFUND_MANUAL");
        paymentRepository.update(refund);
    }
    
    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * Get transaction by ID
     */
    public Transaction getTransaction(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        
        Optional<Transaction> transactionOpt = paymentRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new NotFoundException("Transaction", transactionId);
        }
        
        return transactionOpt.get();
    }
    
    /**
     * Get user transaction history
     */
    public List<Transaction> getUserTransactionHistory(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findByUserId(userId);
    }
    
    /**
     * Get order transaction history
     */
    public List<Transaction> getOrderTransactionHistory(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        
        // Verify order exists
        if (!orderRepository.existsById(orderId)) {
            throw new NotFoundException("Order", orderId);
        }
        
        return paymentRepository.findByOrderId(orderId);
    }
    
    /**
     * Get user wallet transactions
     */
    public List<Transaction> getUserWalletTransactions(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findWalletTransactions(userId);
    }
    
    /**
     * Get user payment transactions
     */
    public List<Transaction> getUserPaymentTransactions(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findPaymentTransactions(userId);
    }
    
    /**
     * Get transactions by status
     */
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }
        
        return paymentRepository.findByStatus(status);
    }
    
    /**
     * Get transactions by type
     */
    public List<Transaction> getTransactionsByType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        
        return paymentRepository.findByType(type);
    }
    
    /**
     * Get transactions within date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return paymentRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Get user transactions within date range
     */
    public List<Transaction> getUserTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    // ==================== TRANSACTION STATISTICS ====================
    
    /**
     * Get user transaction statistics
     */
    public PaymentRepository.TransactionStatistics getUserTransactionStatistics(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.getUserTransactionStatistics(userId);
    }
    
    // ==================== PAYMENT VALIDATION ====================
    
    /**
     * Validate payment method
     */
    public boolean isValidPaymentMethod(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        
        String method = paymentMethod.toUpperCase();
        return method.equals("WALLET") || method.equals("CARD") || method.equals("CASH_ON_DELIVERY");
    }
    
    /**
     * Check if user can make payment
     */
    public boolean canUserMakePayment(Long userId, Double amount, String paymentMethod) {
        if (userId == null || userId <= 0 || amount == null || amount <= 0 || paymentMethod == null) {
            return false;
        }
        
        // Check if user exists
        if (!authRepository.existsById(userId)) {
            return false;
        }
        
        // Check payment method validity
        if (!isValidPaymentMethod(paymentMethod)) {
            return false;
        }
        
        // For wallet payments, check balance
        if ("WALLET".equalsIgnoreCase(paymentMethod)) {
            Double walletBalance = paymentRepository.calculateWalletBalance(userId);
            return walletBalance >= amount;
        }
        
        // For other payment methods, assume they can be processed
        return true;
    }
    
    /**
     * Check if transaction can be refunded
     */
    public boolean canRefundTransaction(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            return false;
        }
        
        Optional<Transaction> transactionOpt = paymentRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            return false;
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Only completed payment transactions can be refunded
        if (transaction.getType() != TransactionType.PAYMENT || transaction.getStatus() != TransactionStatus.COMPLETED) {
            return false;
        }
        
        // Check if already refunded
        if (transaction.getOrderId() != null) {
            List<Transaction> orderTransactions = paymentRepository.findByOrderId(transaction.getOrderId());
            return orderTransactions.stream()
                .noneMatch(t -> t.getType() == TransactionType.REFUND && t.getStatus() == TransactionStatus.COMPLETED);
        }
        
        return true;
    }
    
    /**
     * Update transaction status (for external payment gateway callbacks)
     */
    public Transaction updateTransactionStatus(Long transactionId, TransactionStatus status, String referenceId, String notes) {
        if (transactionId == null || transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }
        
        Optional<Transaction> transactionOpt = paymentRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new NotFoundException("Transaction", transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        
        // Update transaction based on status
        switch (status) {
            case COMPLETED:
                transaction.markAsCompleted(referenceId);
                break;
            case FAILED:
                transaction.markAsFailed(notes != null ? notes : "Payment failed");
                break;
            case CANCELLED:
                transaction.markAsCancelled(notes != null ? notes : "Payment cancelled");
                break;
            default:
                transaction.setStatus(status);
                if (notes != null) {
                    transaction.setDescription(transaction.getDescription() + ". " + notes);
                }
                break;
        }
        
        return paymentRepository.update(transaction);
    }
} 
package com.myapp.payment;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionStatus;
import com.myapp.common.models.TransactionType;
import com.myapp.common.models.User;
import com.myapp.auth.AuthRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for wallet management operations
 * Handles wallet balance, charging, withdrawal, and transaction history
 */
public class WalletService {
    
    private final PaymentRepository paymentRepository;
    private final AuthRepository authRepository;
    
    public WalletService() {
        this.paymentRepository = new PaymentRepository();
        this.authRepository = new AuthRepository();
    }
    
    public WalletService(PaymentRepository paymentRepository, AuthRepository authRepository) {
        this.paymentRepository = paymentRepository;
        this.authRepository = authRepository;
    }
    
    // ==================== WALLET BALANCE MANAGEMENT ====================
    
    /**
     * Get user wallet balance
     */
    public Double getWalletBalance(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.calculateWalletBalance(userId);
    }
    
    /**
     * Check if user has sufficient balance
     */
    public boolean hasSufficientBalance(Long userId, Double amount) {
        if (userId == null || userId <= 0) {
            return false;
        }
        if (amount == null || amount <= 0) {
            return false;
        }
        
        try {
            Double balance = getWalletBalance(userId);
            return balance >= amount;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== WALLET CHARGING ====================
    
    /**
     * Charge wallet using card payment
     */
    public Transaction chargeWalletWithCard(Long userId, Double amount) {
        return chargeWallet(userId, amount, "CARD", "Wallet charge via card");
    }
    
    /**
     * Charge wallet using bank transfer
     */
    public Transaction chargeWalletWithBankTransfer(Long userId, Double amount, String transferReference) {
        String description = "Wallet charge via bank transfer. Reference: " + transferReference;
        return chargeWallet(userId, amount, "BANK_TRANSFER", description);
    }
    
    /**
     * Charge wallet (generic method)
     */
    public Transaction chargeWallet(Long userId, Double amount, String paymentMethod, String description) {
        // Validate inputs
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Charge amount must be positive");
        }
        if (amount > 10000.0) {
            throw new IllegalArgumentException("Maximum charge amount is 10,000 per transaction");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be empty");
        }
        
        // Verify user exists
        Optional<User> userOpt = authRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new NotFoundException("User", userId);
        }
        
        // Check daily charge limit
        validateDailyChargeLimit(userId, amount);
        
        // Create wallet charge transaction
        Transaction chargeTransaction = Transaction.forWalletCharge(userId, amount, paymentMethod);
        if (description != null && !description.trim().isEmpty()) {
            chargeTransaction.setDescription(description.trim());
        }
        
        chargeTransaction = paymentRepository.save(chargeTransaction);
        
        // Process charge based on payment method
        try {
            switch (paymentMethod.toUpperCase()) {
                case "CARD":
                    processCardCharge(chargeTransaction);
                    break;
                case "BANK_TRANSFER":
                    processBankTransferCharge(chargeTransaction);
                    break;
                case "ADMIN_CREDIT":
                    processAdminCredit(chargeTransaction);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported payment method for wallet charge: " + paymentMethod);
            }
        } catch (Exception e) {
            chargeTransaction.markAsFailed(e.getMessage());
            paymentRepository.update(chargeTransaction);
            throw e;
        }
        
        return chargeTransaction;
    }
    
    /**
     * Process card charge (simulation)
     */
    private void processCardCharge(Transaction transaction) {
        // Simulate card payment processing
        String referenceId = "CARD_CHARGE_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Simulate payment gateway response (95% success rate for wallet charges)
        if (Math.random() < 0.95) {
            transaction.markAsCompleted(referenceId);
        } else {
            transaction.markAsFailed("Card charge declined by bank");
        }
        
        paymentRepository.update(transaction);
    }
    
    /**
     * Process bank transfer charge
     */
    private void processBankTransferCharge(Transaction transaction) {
        // Bank transfers need manual verification
        transaction.setDescription(transaction.getDescription() + " - Pending bank verification");
        paymentRepository.update(transaction);
    }
    
    /**
     * Process admin credit (instant)
     */
    private void processAdminCredit(Transaction transaction) {
        String referenceId = "ADMIN_CREDIT_" + UUID.randomUUID().toString().substring(0, 8);
        transaction.markAsCompleted(referenceId);
        paymentRepository.update(transaction);
    }
    
    /**
     * Validate daily charge limit
     */
    private void validateDailyChargeLimit(Long userId, Double amount) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        List<Transaction> todayCharges = paymentRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay)
            .stream()
            .filter(t -> t.getType() == TransactionType.WALLET_CHARGE && t.getStatus() == TransactionStatus.COMPLETED)
            .toList();
        
        Double todayTotal = todayCharges.stream()
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        Double dailyLimit = 50000.0; // Daily limit of 50,000
        if (todayTotal + amount > dailyLimit) {
            throw new IllegalArgumentException("Daily charge limit exceeded. Today: " + todayTotal + ", Limit: " + dailyLimit);
        }
    }
    
    // ==================== WALLET WITHDRAWAL ====================
    
    /**
     * Withdraw from wallet to bank account
     */
    public Transaction withdrawToBank(Long userId, Double amount, String bankAccount, String reason) {
        // Validate inputs
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount < 10.0) {
            throw new IllegalArgumentException("Minimum withdrawal amount is 10");
        }
        if (amount > 5000.0) {
            throw new IllegalArgumentException("Maximum withdrawal amount is 5,000 per transaction");
        }
        if (bankAccount == null || bankAccount.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank account cannot be empty");
        }
        
        // Verify user exists and get balance
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        // Check wallet balance
        Double currentBalance = paymentRepository.calculateWalletBalance(userId);
        if (currentBalance < amount) {
            throw new IllegalArgumentException("Insufficient wallet balance. Balance: " + currentBalance + ", Requested: " + amount);
        }
        
        // Check daily withdrawal limit
        validateDailyWithdrawalLimit(userId, amount);
        
        // Create withdrawal transaction
        String description = "Withdrawal to bank account: " + bankAccount;
        if (reason != null && !reason.trim().isEmpty()) {
            description += ". Reason: " + reason.trim();
        }
        
        Transaction withdrawal = Transaction.forWalletWithdrawal(userId, amount, description);
        withdrawal = paymentRepository.save(withdrawal);
        
        // Process withdrawal (mark as pending for manual processing)
        withdrawal.setDescription(withdrawal.getDescription() + " - Pending bank transfer");
        paymentRepository.update(withdrawal);
        
        return withdrawal;
    }
    
    /**
     * Internal withdrawal for payments (instant)
     */
    public Transaction withdrawForPayment(Long userId, Double amount, String orderReference) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        // Verify user exists and check wallet balance
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        Double currentBalance = paymentRepository.calculateWalletBalance(userId);
        if (currentBalance < amount) {
            throw new IllegalArgumentException("Insufficient wallet balance. Balance: " + currentBalance + ", Required: " + amount);
        }
        
        // Create and complete withdrawal transaction
        String description = "Payment withdrawal for order: " + orderReference;
        Transaction withdrawal = Transaction.forWalletWithdrawal(userId, amount, description);
        
        String referenceId = "PAYMENT_WITHDRAWAL_" + UUID.randomUUID().toString().substring(0, 8);
        withdrawal.markAsCompleted(referenceId);
        
        return paymentRepository.save(withdrawal);
    }
    
    /**
     * Validate daily withdrawal limit
     */
    private void validateDailyWithdrawalLimit(Long userId, Double amount) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        List<Transaction> todayWithdrawals = paymentRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay)
            .stream()
            .filter(t -> t.getType() == TransactionType.WALLET_WITHDRAWAL && t.getStatus() == TransactionStatus.COMPLETED)
            .toList();
        
        Double todayTotal = todayWithdrawals.stream()
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        Double dailyLimit = 20000.0; // Daily withdrawal limit of 20,000
        if (todayTotal + amount > dailyLimit) {
            throw new IllegalArgumentException("Daily withdrawal limit exceeded. Today: " + todayTotal + ", Limit: " + dailyLimit);
        }
    }
    
    // ==================== WALLET TRANSACTION HISTORY ====================
    
    /**
     * Get wallet transaction history
     */
    public List<Transaction> getWalletTransactionHistory(Long userId) {
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
     * Get wallet transaction history within date range
     */
    public List<Transaction> getWalletTransactionHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
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
        
        List<Transaction> allTransactions = paymentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        return allTransactions.stream()
            .filter(t -> t.getType() == TransactionType.WALLET_CHARGE || t.getType() == TransactionType.WALLET_WITHDRAWAL)
            .toList();
    }
    
    /**
     * Get wallet charge history
     */
    public List<Transaction> getWalletChargeHistory(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findByUserIdAndType(userId, TransactionType.WALLET_CHARGE);
    }
    
    /**
     * Get wallet withdrawal history
     */
    public List<Transaction> getWalletWithdrawalHistory(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        return paymentRepository.findByUserIdAndType(userId, TransactionType.WALLET_WITHDRAWAL);
    }
    
    // ==================== WALLET STATISTICS ====================
    
    /**
     * Get wallet statistics for a user
     */
    public WalletStatistics getWalletStatistics(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        List<Transaction> walletTransactions = paymentRepository.findWalletTransactions(userId);
        
        Double totalCharged = walletTransactions.stream()
            .filter(t -> t.getType() == TransactionType.WALLET_CHARGE && t.getStatus() == TransactionStatus.COMPLETED)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        Double totalWithdrawn = walletTransactions.stream()
            .filter(t -> t.getType() == TransactionType.WALLET_WITHDRAWAL && t.getStatus() == TransactionStatus.COMPLETED)
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        Long totalChargeTransactions = walletTransactions.stream()
            .filter(t -> t.getType() == TransactionType.WALLET_CHARGE)
            .count();
        
        Long totalWithdrawalTransactions = walletTransactions.stream()
            .filter(t -> t.getType() == TransactionType.WALLET_WITHDRAWAL)
            .count();
        
        Double currentBalance = getWalletBalance(userId);
        
        return new WalletStatistics(
            currentBalance,
            totalCharged,
            totalWithdrawn,
            totalChargeTransactions,
            totalWithdrawalTransactions
        );
    }
    
    // ==================== ADMIN OPERATIONS ====================
    
    /**
     * Admin credit to user wallet
     */
    public Transaction adminCreditWallet(Long userId, Double amount, String reason, Long adminId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        // Verify admin exists
        if (!authRepository.existsById(adminId)) {
            throw new NotFoundException("Admin", adminId);
        }
        
        String description = "Admin credit by admin #" + adminId;
        if (reason != null && !reason.trim().isEmpty()) {
            description += ". Reason: " + reason.trim();
        }
        
        // Create wallet charge transaction with ADMIN_CREDIT payment method
        Transaction chargeTransaction = Transaction.forWalletCharge(userId, amount, "ADMIN_CREDIT");
        chargeTransaction.setDescription(description);
        
        chargeTransaction = paymentRepository.save(chargeTransaction);
        
        // Admin credits are instant
        String referenceId = "ADMIN_CREDIT_" + UUID.randomUUID().toString().substring(0, 8);
        chargeTransaction.markAsCompleted(referenceId);
        paymentRepository.update(chargeTransaction);
        
        return chargeTransaction;
    }
    
    /**
     * Admin debit from user wallet
     */
    public Transaction adminDebitWallet(Long userId, Double amount, String reason, Long adminId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // Verify user exists
        if (!authRepository.existsById(userId)) {
            throw new NotFoundException("User", userId);
        }
        
        // Verify admin exists
        if (!authRepository.existsById(adminId)) {
            throw new NotFoundException("Admin", adminId);
        }
        
        // Check wallet balance
        Double currentBalance = getWalletBalance(userId);
        if (currentBalance < amount) {
            throw new IllegalArgumentException("Insufficient wallet balance for debit. Balance: " + currentBalance + ", Requested: " + amount);
        }
        
        String description = "Admin debit by admin #" + adminId;
        if (reason != null && !reason.trim().isEmpty()) {
            description += ". Reason: " + reason.trim();
        }
        
        Transaction debit = Transaction.forWalletWithdrawal(userId, amount, description);
        String referenceId = "ADMIN_DEBIT_" + UUID.randomUUID().toString().substring(0, 8);
        debit.markAsCompleted(referenceId);
        
        return paymentRepository.save(debit);
    }
    
    /**
     * Wallet statistics inner class
     */
    public static class WalletStatistics {
        private final Double currentBalance;
        private final Double totalCharged;
        private final Double totalWithdrawn;
        private final Long totalChargeTransactions;
        private final Long totalWithdrawalTransactions;
        
        public WalletStatistics(Double currentBalance, Double totalCharged, Double totalWithdrawn,
                              Long totalChargeTransactions, Long totalWithdrawalTransactions) {
            this.currentBalance = currentBalance;
            this.totalCharged = totalCharged;
            this.totalWithdrawn = totalWithdrawn;
            this.totalChargeTransactions = totalChargeTransactions;
            this.totalWithdrawalTransactions = totalWithdrawalTransactions;
        }
        
        // Getters
        public Double getCurrentBalance() { return currentBalance; }
        public Double getTotalCharged() { return totalCharged; }
        public Double getTotalWithdrawn() { return totalWithdrawn; }
        public Long getTotalChargeTransactions() { return totalChargeTransactions; }
        public Long getTotalWithdrawalTransactions() { return totalWithdrawalTransactions; }
        public Double getNetFlow() { return totalCharged - totalWithdrawn; }
        public Long getTotalTransactions() { return totalChargeTransactions + totalWithdrawalTransactions; }
    }
} 
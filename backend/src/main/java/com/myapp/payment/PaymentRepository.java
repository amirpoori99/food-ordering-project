package com.myapp.payment;

import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionStatus;
import com.myapp.common.models.TransactionType;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity operations
 * Handles all database operations related to payments and wallet transactions
 */
public class PaymentRepository {
    
    private final SessionFactory sessionFactory;
    
    public PaymentRepository() {
        this.sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    public PaymentRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Save a new transaction
     */
    public Transaction save(Transaction transaction) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            session.persist(transaction);
            session.getTransaction().commit();
            return transaction;
        }
    }
    
    /**
     * Update an existing transaction
     */
    public Transaction update(Transaction transaction) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Transaction updated = session.merge(transaction);
            session.getTransaction().commit();
            return updated;
        }
    }
    
    /**
     * Find transaction by ID
     */
    public Optional<Transaction> findById(Long id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Transaction transaction = session.get(Transaction.class, id);
            session.getTransaction().commit();
            return Optional.ofNullable(transaction);
        }
    }
    
    /**
     * Find transaction by reference ID
     */
    public Optional<Transaction> findByReferenceId(String referenceId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.referenceId = :referenceId", Transaction.class);
            query.setParameter("referenceId", referenceId);
            Transaction transaction = query.uniqueResult();
            session.getTransaction().commit();
            return Optional.ofNullable(transaction);
        }
    }
    
    /**
     * Find all transactions for a user
     */
    public List<Transaction> findByUserId(Long userId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.userId = :userId ORDER BY t.createdAt DESC", Transaction.class);
            query.setParameter("userId", userId);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find all transactions for a specific order
     */
    public List<Transaction> findByOrderId(Long orderId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.orderId = :orderId ORDER BY t.createdAt DESC", Transaction.class);
            query.setParameter("orderId", orderId);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find transactions by status
     */
    public List<Transaction> findByStatus(TransactionStatus status) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.status = :status ORDER BY t.createdAt DESC", Transaction.class);
            query.setParameter("status", status);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find transactions by type
     */
    public List<Transaction> findByType(TransactionType type) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.type = :type ORDER BY t.createdAt DESC", Transaction.class);
            query.setParameter("type", type);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find user transactions by status
     */
    public List<Transaction> findByUserIdAndStatus(Long userId, TransactionStatus status) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.userId = :userId AND t.status = :status ORDER BY t.createdAt DESC", 
                Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("status", status);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find user transactions by type
     */
    public List<Transaction> findByUserIdAndType(Long userId, TransactionType type) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.userId = :userId AND t.type = :type ORDER BY t.createdAt DESC", 
                Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find wallet transactions for a user
     */
    public List<Transaction> findWalletTransactions(Long userId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.userId = :userId AND t.type IN (:walletTypes) ORDER BY t.createdAt DESC", 
                Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("walletTypes", List.of(TransactionType.WALLET_CHARGE, TransactionType.WALLET_WITHDRAWAL));
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find payment transactions for a user
     */
    public List<Transaction> findPaymentTransactions(Long userId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.userId = :userId AND t.type IN (:paymentTypes) ORDER BY t.createdAt DESC", 
                Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("paymentTypes", List.of(TransactionType.PAYMENT, TransactionType.REFUND));
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find transactions within date range
     */
    public List<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC", 
                Transaction.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Find user transactions within date range
     */
    public List<Transaction> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Transaction> query = session.createQuery(
                "FROM Transaction t WHERE t.userId = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC", 
                Transaction.class);
            query.setParameter("userId", userId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Transaction> transactions = query.getResultList();
            session.getTransaction().commit();
            return transactions;
        }
    }
    
    /**
     * Calculate total wallet balance for a user
     */
    public Double calculateWalletBalance(Long userId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Double> query = session.createQuery(
                "SELECT COALESCE(SUM(CASE " +
                "WHEN t.type = :charge THEN t.amount " +
                "WHEN t.type = :withdrawal THEN -t.amount " +
                "ELSE 0 END), 0.0) " +
                "FROM Transaction t WHERE t.userId = :userId AND t.status = :status AND t.type IN (:walletTypes)", 
                Double.class);
            query.setParameter("userId", userId);
            query.setParameter("charge", TransactionType.WALLET_CHARGE);
            query.setParameter("withdrawal", TransactionType.WALLET_WITHDRAWAL);
            query.setParameter("status", TransactionStatus.COMPLETED);
            query.setParameter("walletTypes", List.of(TransactionType.WALLET_CHARGE, TransactionType.WALLET_WITHDRAWAL));
            Double balance = query.uniqueResult();
            session.getTransaction().commit();
            return balance != null ? balance : 0.0;
        }
    }
    
    /**
     * Get transaction statistics for a user
     */
    public TransactionStatistics getUserTransactionStatistics(Long userId) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            
            // Total transactions
            Query<Long> totalQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId", Long.class);
            totalQuery.setParameter("userId", userId);
            Long totalTransactions = totalQuery.uniqueResult();
            
            // Completed transactions
            Query<Long> completedQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status", Long.class);
            completedQuery.setParameter("userId", userId);
            completedQuery.setParameter("status", TransactionStatus.COMPLETED);
            Long completedTransactions = completedQuery.uniqueResult();
            
            // Pending transactions
            Query<Long> pendingQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status", Long.class);
            pendingQuery.setParameter("userId", userId);
            pendingQuery.setParameter("status", TransactionStatus.PENDING);
            Long pendingTransactions = pendingQuery.uniqueResult();
            
            // Failed transactions
            Query<Long> failedQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status", Long.class);
            failedQuery.setParameter("userId", userId);
            failedQuery.setParameter("status", TransactionStatus.FAILED);
            Long failedTransactions = failedQuery.uniqueResult();
            
            // Total amount spent (completed payments)
            Query<Double> spentQuery = session.createQuery(
                "SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.status = :status", 
                Double.class);
            spentQuery.setParameter("userId", userId);
            spentQuery.setParameter("type", TransactionType.PAYMENT);
            spentQuery.setParameter("status", TransactionStatus.COMPLETED);
            Double totalSpent = spentQuery.uniqueResult();
            
            // Total amount refunded
            Query<Double> refundQuery = session.createQuery(
                "SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.status = :status", 
                Double.class);
            refundQuery.setParameter("userId", userId);
            refundQuery.setParameter("type", TransactionType.REFUND);
            refundQuery.setParameter("status", TransactionStatus.COMPLETED);
            Double totalRefunded = refundQuery.uniqueResult();
            
            session.getTransaction().commit();
            
            return new TransactionStatistics(
                totalTransactions != null ? totalTransactions : 0L,
                completedTransactions != null ? completedTransactions : 0L,
                pendingTransactions != null ? pendingTransactions : 0L,
                failedTransactions != null ? failedTransactions : 0L,
                totalSpent != null ? totalSpent : 0.0,
                totalRefunded != null ? totalRefunded : 0.0
            );
        }
    }
    
    /**
     * Check if transaction exists
     */
    public boolean existsById(Long id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Query<Long> query = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.id = :id", Long.class);
            query.setParameter("id", id);
            Long count = query.uniqueResult();
            session.getTransaction().commit();
            return count != null && count > 0;
        }
    }
    
    /**
     * Delete transaction (rarely used - for testing purposes)
     */
    public void delete(Long id) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            Transaction transaction = session.get(Transaction.class, id);
            if (transaction != null) {
                session.remove(transaction);
            }
            session.getTransaction().commit();
        }
    }
    
    /**
     * Transaction statistics inner class
     */
    public static class TransactionStatistics {
        private final Long totalTransactions;
        private final Long completedTransactions;
        private final Long pendingTransactions;
        private final Long failedTransactions;
        private final Double totalSpent;
        private final Double totalRefunded;
        
        public TransactionStatistics(Long totalTransactions, Long completedTransactions, 
                                   Long pendingTransactions, Long failedTransactions,
                                   Double totalSpent, Double totalRefunded) {
            this.totalTransactions = totalTransactions;
            this.completedTransactions = completedTransactions;
            this.pendingTransactions = pendingTransactions;
            this.failedTransactions = failedTransactions;
            this.totalSpent = totalSpent;
            this.totalRefunded = totalRefunded;
        }
        
        // Getters
        public Long getTotalTransactions() { return totalTransactions; }
        public Long getCompletedTransactions() { return completedTransactions; }
        public Long getPendingTransactions() { return pendingTransactions; }
        public Long getFailedTransactions() { return failedTransactions; }
        public Double getTotalSpent() { return totalSpent; }
        public Double getTotalRefunded() { return totalRefunded; }
        public Double getNetSpent() { return totalSpent - totalRefunded; }
        public Double getSuccessRate() { 
            return totalTransactions > 0 ? (double) completedTransactions / totalTransactions * 100 : 0.0; 
        }
    }
} 

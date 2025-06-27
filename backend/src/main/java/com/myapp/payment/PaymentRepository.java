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
 * Repository برای مدیریت عملیات Entity تراکنش
 * 
 * این کلاس مسئول تمام عملیات دیتابیس مربوط به:
 * - تراکنش‌های پرداخت و استرداد
 * - تراکنش‌های کیف پول (شارژ و برداشت)
 * - محاسبه موجودی کیف پول
 * - جستجو و فیلتر کردن تراکنش‌ها
 * - تولید آمار و گزارش‌های مالی
 * 
 * Pattern های استفاده شده:
 * - Repository Pattern: انتزاع لایه دسترسی به داده
 * - Session-per-Request: مدیریت session های Hibernate
 * - Query Optimization: استفاده از HQL و Native Query ها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class PaymentRepository {
    
    /** SessionFactory برای مدیریت ارتباط با دیتابیس */
    private final SessionFactory sessionFactory;
    
    /**
     * سازنده پیش‌فرض - دریافت SessionFactory از DatabaseUtil
     */
    public PaymentRepository() {
        this.sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    /**
     * سازنده برای تزریق وابستگی (Dependency Injection)
     * برای تست‌ها و configuration سفارشی استفاده می‌شود
     * 
     * @param sessionFactory SessionFactory سفارشی
     */
    public PaymentRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * ذخیره تراکنش جدید در دیتابیس
     * 
     * @param transaction تراکنش جدید برای ذخیره
     * @return تراکنش ذخیره شده با ID تولید شده
     */
    public Transaction save(Transaction transaction) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(transaction); // Hibernate 6+ syntax
            session.getTransaction().commit();
            return transaction;
        }
    }
    
    /**
     * به‌روزرسانی تراکنش موجود
     * 
     * @param transaction تراکنش برای به‌روزرسانی
     * @return تراکنش به‌روزرسانی شده
     */
    public Transaction update(Transaction transaction) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Transaction updated = session.merge(transaction); // merge برای update
            session.getTransaction().commit();
            return updated;
        }
    }
    
    /**
     * یافتن تراکنش بر اساس ID
     * 
     * @param id شناسه تراکنش
     * @return Optional حاوی تراکنش یا خالی
     */
    public Optional<Transaction> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Transaction transaction = session.get(Transaction.class, id);
            session.getTransaction().commit();
            return Optional.ofNullable(transaction);
        }
    }
    
    /**
     * یافتن تراکنش بر اساس شناسه مرجع
     * 
     * شناسه مرجع توسط payment gateway ها تولید می‌شود
     * برای tracking و reconciliation استفاده می‌شود
     * 
     * @param referenceId شناسه مرجع از payment gateway
     * @return Optional حاوی تراکنش یا خالی
     */
    public Optional<Transaction> findByReferenceId(String referenceId) {
        try (Session session = sessionFactory.openSession()) {
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
     * یافتن تمام تراکنش‌های یک کاربر
     * 
     * نتایج بر اساس تاریخ ایجاد به صورت نزولی مرتب می‌شوند (جدیدترین ابتدا)
     * 
     * @param userId شناسه کاربر
     * @return لیست تراکنش‌های کاربر
     */
    public List<Transaction> findByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
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
     * یافتن تمام تراکنش‌های مربوط به سفارش خاص
     * 
     * شامل تراکنش پرداخت و احتمالی استرداد آن سفارش
     * 
     * @param orderId شناسه سفارش
     * @return لیست تراکنش‌های مربوط به سفارش
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
     * یافتن تراکنش‌ها بر اساس وضعیت
     * 
     * وضعیت‌های موجود: PENDING, COMPLETED, FAILED, CANCELLED
     * 
     * @param status وضعیت تراکنش
     * @return لیست تراکنش‌های با وضعیت مشخص
     */
    public List<Transaction> findByStatus(TransactionStatus status) {
        try (Session session = sessionFactory.openSession()) {
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
     * یافتن تراکنش‌ها بر اساس نوع
     * 
     * انواع تراکنش: PAYMENT, REFUND, WALLET_CHARGE, WALLET_WITHDRAWAL
     * 
     * @param type نوع تراکنش
     * @return لیست تراکنش‌های با نوع مشخص
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
     * یافتن تراکنش‌های کاربر با وضعیت مشخص
     * 
     * ترکیب فیلتر کاربر و وضعیت برای جستجوی دقیق‌تر
     * 
     * @param userId شناسه کاربر
     * @param status وضعیت تراکنش
     * @return لیست تراکنش‌های فیلتر شده
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
     * یافتن تراکنش‌های کاربر با نوع مشخص
     * 
     * @param userId شناسه کاربر
     * @param type نوع تراکنش
     * @return لیست تراکنش‌های فیلتر شده
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
     * یافتن تراکنش‌های کیف پول برای کاربر
     * 
     * شامل تراکنش‌های شارژ (WALLET_CHARGE) و برداشت (WALLET_WITHDRAWAL)
     * 
     * @param userId شناسه کاربر
     * @return لیست تراکنش‌های کیف پول
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
     * یافتن تراکنش‌های پرداخت برای کاربر
     * 
     * شامل تراکنش‌های پرداخت (PAYMENT) و استرداد (REFUND)
     * 
     * @param userId شناسه کاربر
     * @return لیست تراکنش‌های پرداخت
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
     * یافتن تراکنش‌ها در بازه زمانی مشخص
     * 
     * @param startDate تاریخ شروع
     * @param endDate تاریخ پایان
     * @return لیست تراکنش‌ها در بازه زمانی
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
     * یافتن تراکنش‌های کاربر در بازه زمانی مشخص
     * 
     * @param userId شناسه کاربر
     * @param startDate تاریخ شروع
     * @param endDate تاریخ پایان
     * @return لیست تراکنش‌های کاربر در بازه زمانی
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
     * محاسبه موجودی کل کیف پول برای کاربر
     * 
     * این متد با استفاده از aggregate function موجودی را محاسبه می‌کند:
     * - تراکنش‌های شارژ (WALLET_CHARGE) به موجودی اضافه می‌شوند
     * - تراکنش‌های برداشت (WALLET_WITHDRAWAL) از موجودی کم می‌شوند
     * - فقط تراکنش‌های موفق (COMPLETED) محاسبه می‌شوند
     * 
     * @param userId شناسه کاربر
     * @return موجودی فعلی کیف پول
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
     * دریافت آمار کامل تراکنش‌های کاربر
     * 
     * این متد مجموعه‌ای از query های aggregate برای محاسبه آمار اجرا می‌کند:
     * - تعداد کل تراکنش‌ها
     * - تعداد تراکنش‌های موفق/ناموفق/در انتظار
     * - مجموع مبلغ خرج شده (پرداخت‌های موفق)
     * - مجموع مبلغ استرداد شده
     * 
     * @param userId شناسه کاربر
     * @return آمار کامل تراکنش‌های کاربر
     */
    public TransactionStatistics getUserTransactionStatistics(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            
            // تعداد کل تراکنش‌ها
            Query<Long> totalQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId", Long.class);
            totalQuery.setParameter("userId", userId);
            Long totalTransactions = totalQuery.uniqueResult();
            
            // تعداد تراکنش‌های موفق
            Query<Long> completedQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status", Long.class);
            completedQuery.setParameter("userId", userId);
            completedQuery.setParameter("status", TransactionStatus.COMPLETED);
            Long completedTransactions = completedQuery.uniqueResult();
            
            // تعداد تراکنش‌های در انتظار
            Query<Long> pendingQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status", Long.class);
            pendingQuery.setParameter("userId", userId);
            pendingQuery.setParameter("status", TransactionStatus.PENDING);
            Long pendingTransactions = pendingQuery.uniqueResult();
            
            // تعداد تراکنش‌های ناموفق
            Query<Long> failedQuery = session.createQuery(
                "SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status", Long.class);
            failedQuery.setParameter("userId", userId);
            failedQuery.setParameter("status", TransactionStatus.FAILED);
            Long failedTransactions = failedQuery.uniqueResult();
            
            // مجموع مبلغ خرج شده (پرداخت‌های موفق)
            Query<Double> spentQuery = session.createQuery(
                "SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.status = :status", 
                Double.class);
            spentQuery.setParameter("userId", userId);
            spentQuery.setParameter("type", TransactionType.PAYMENT);
            spentQuery.setParameter("status", TransactionStatus.COMPLETED);
            Double totalSpent = spentQuery.uniqueResult();
            
            // مجموع مبلغ استرداد شده
            Query<Double> refundQuery = session.createQuery(
                "SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.status = :status", 
                Double.class);
            refundQuery.setParameter("userId", userId);
            refundQuery.setParameter("type", TransactionType.REFUND);
            refundQuery.setParameter("status", TransactionStatus.COMPLETED);
            Double totalRefunded = refundQuery.uniqueResult();
            
            session.getTransaction().commit();
            
            // ایجاد شیء آمار با تمام اطلاعات محاسبه شده
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
     * بررسی وجود تراکنش با شناسه مشخص
     * 
     * @param id شناسه تراکنش
     * @return true اگر وجود داشته باشد، در غیر این صورت false
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
     * حذف تراکنش (به ندرت استفاده می‌شود - بیشتر برای تست)
     * 
     * توجه: در محیط production، معمولاً تراکنش‌ها حذف نمی‌شوند
     * بلکه وضعیت آنها تغییر می‌کند
     * 
     * @param id شناسه تراکنش برای حذف
     */
    public void delete(Long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Transaction transaction = session.get(Transaction.class, id);
            if (transaction != null) {
                session.remove(transaction);
            }
            session.getTransaction().commit();
        }
    }
    
    /**
     * حذف تمام تراکنش‌ها (فقط برای تست‌ها)
     * 
     * این متد تمام تراکنش‌ها را حذف می‌کند و باید فقط در محیط تست استفاده شود
     * در محیط production، تراکنش‌ها هرگز حذف نمی‌شوند
     */
    public void deleteAll() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Query<?> query = session.createQuery("DELETE FROM Transaction");
            query.executeUpdate();
            session.getTransaction().commit();
        }
    }
    
    /**
     * کلاس داخلی برای نگهداری آمار تراکنش‌ها
     * 
     * این کلاس حاوی تمام اطلاعات آماری مربوط به فعالیت‌های مالی کاربر است
     * و محاسبات اضافی نیز انجام می‌دهد
     */
    public static class TransactionStatistics {
        private final Long totalTransactions;
        private final Long completedTransactions;
        private final Long pendingTransactions;
        private final Long failedTransactions;
        private final Double totalSpent;
        private final Double totalRefunded;
        
        /**
         * سازنده کلاس آمار
         * 
         * @param totalTransactions تعداد کل تراکنش‌ها
         * @param completedTransactions تعداد تراکنش‌های موفق
         * @param pendingTransactions تعداد تراکنش‌های در انتظار
         * @param failedTransactions تعداد تراکنش‌های ناموفق
         * @param totalSpent مجموع مبلغ خرج شده
         * @param totalRefunded مجموع مبلغ استرداد شده
         */
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
        
        // ==================== GETTER METHODS ====================
        
        /** @return تعداد کل تراکنش‌ها */
        public Long getTotalTransactions() { return totalTransactions; }
        
        /** @return تعداد تراکنش‌های موفق */
        public Long getCompletedTransactions() { return completedTransactions; }
        
        /** @return تعداد تراکنش‌های در انتظار */
        public Long getPendingTransactions() { return pendingTransactions; }
        
        /** @return تعداد تراکنش‌های ناموفق */
        public Long getFailedTransactions() { return failedTransactions; }
        
        /** @return مجموع مبلغ خرج شده */
        public Double getTotalSpent() { return totalSpent; }
        
        /** @return مجموع مبلغ استرداد شده */
        public Double getTotalRefunded() { return totalRefunded; }
        
        /** @return مبلغ خالص خرج شده (خرج شده منهای استرداد شده) */
        public Double getNetSpent() { return totalSpent - totalRefunded; }
        
        /** @return درصد موفقیت تراکنش‌ها */
        public Double getSuccessRate() { 
            return totalTransactions > 0 ? (double) completedTransactions / totalTransactions * 100 : 0.0; 
        }
    }
} 

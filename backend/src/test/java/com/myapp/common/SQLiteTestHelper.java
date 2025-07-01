package com.myapp.common;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * کلاس کمکی برای مدیریت مشکلات SQLite در تست‌ها
 * 
 * این کلاس راه‌حل‌هایی برای مشکلات رایج SQLite در محیط تست ارائه می‌دهد:
 * - مدیریت قفل‌های دیتابیس
 * - retry logic برای عملیات ناموفق
 * - مدیریت تراکنش‌ها
 * - بهینه‌سازی برای تست‌های همزمان
 * 
 * @author Food Ordering System Team
 * @version 2.0
 * @since 2024
 */
public class SQLiteTestHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(SQLiteTestHelper.class);
    
    /** حداکثر تعداد تلاش برای retry */
    private static final int MAX_RETRIES = 5;
    
    /** زمان انتظار پایه بین تلاش‌ها (میلی‌ثانیه) */
    private static final long BASE_RETRY_DELAY_MS = 50;
    
    /** ضریب افزایش زمان انتظار */
    private static final double BACKOFF_MULTIPLIER = 1.5;
    
    /**
     * اجرای عملیات با retry logic پیشرفته برای SQLite
     * 
     * @param operation عملیات برای اجرا
     * @param <T> نوع نتیجه
     * @return نتیجه عملیات
     * @throws RuntimeException در صورت عدم موفقیت پس از تمام تلاش‌ها
     */
    public static <T> T executeWithRetry(Supplier<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (isRetryableException(e) && attempt < MAX_RETRIES) {
                    long delay = calculateBackoffDelay(attempt);
                    logger.warn("Attempt {} failed, retrying in {}ms: {}", 
                              attempt, delay, e.getMessage());
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Operation interrupted", ie);
                    }
                } else {
                    // اگر exception قابل retry نیست یا آخرین تلاش است، exception اصلی را برمی‌گردانیم
                    if (lastException instanceof RuntimeException) {
                        throw (RuntimeException) lastException;
                    } else {
                        throw new RuntimeException(lastException);
                    }
                }
            }
        }
        
        throw new RuntimeException("Operation failed after " + MAX_RETRIES + " attempts", lastException);
    }
    
    /**
     * محاسبه زمان انتظار با الگوریتم exponential backoff
     * 
     * @param attempt شماره تلاش
     * @return زمان انتظار (میلی‌ثانیه)
     */
    private static long calculateBackoffDelay(int attempt) {
        return (long) (BASE_RETRY_DELAY_MS * Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
    }
    
    /**
     * اجرای عملیات void با retry logic
     * 
     * @param operation عملیات برای اجرا
     * @throws RuntimeException در صورت عدم موفقیت پس از تمام تلاش‌ها
     */
    public static void executeWithRetry(Runnable operation) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * بررسی اینکه آیا exception قابل retry است
     * 
     * @param e exception برای بررسی
     * @return true اگر قابل retry باشد
     */
    private static boolean isRetryableException(Throwable e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        // SQLite locking errors
        return message.contains("database is locked") ||
               message.contains("SQLITE_BUSY") ||
               message.contains("SQLITE_LOCKED") ||
               message.contains("SQLITE_BUSY_SNAPSHOT") ||
               message.contains("connection is closed") ||
               message.contains("LockAcquisitionException") ||
               message.contains("OptimisticLock") ||
               (e.getCause() != null && isRetryableException(e.getCause()));
    }
    
    /**
     * اجرای عملیات با SessionFactory و retry logic پیشرفته
     * 
     * @param sessionFactory factory برای ایجاد session
     * @param operation عملیات برای اجرا
     * @param <T> نوع نتیجه
     * @return نتیجه عملیات
     */
    public static <T> T executeWithRetry(SessionFactory sessionFactory, Supplier<T> operation) {
        return executeWithRetry(() -> {
            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();
                T result = operation.get();
                transaction.commit();
                return result;
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (Exception rollbackEx) {
                        logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                    }
                }
                throw e;
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        });
    }

    /**
     * اجرای عملیات در تراکنش با retry
     * 
     * @param session session دیتابیس
     * @param operation عملیات برای اجرا
     * @param <T> نوع نتیجه
     * @return نتیجه عملیات
     */
    public static <T> T executeInTransaction(Session session, Supplier<T> operation) {
        return executeWithRetry(() -> {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                T result = operation.get();
                transaction.commit();
                return result;
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (Exception rollbackEx) {
                        logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                    }
                }
                throw e;
            }
        });
    }
    
    /**
     * اجرای عملیات void در تراکنش با retry
     * 
     * @param session session دیتابیس
     * @param operation عملیات برای اجرا
     */
    public static void executeInTransaction(Session session, Runnable operation) {
        executeInTransaction(session, () -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * انتظار برای آزاد شدن قفل دیتابیس
     * 
     * @param timeoutMs زمان انتظار (میلی‌ثانیه)
     */
    public static void waitForDatabaseUnlock(long timeoutMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wait interrupted", e);
        }
    }
    
    /**
     * پاک‌سازی اتصالات دیتابیس
     * 
     * @param session session برای بستن
     */
    public static void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn("Error closing session: {}", e.getMessage());
            }
        }
    }
    
    /**
     * اجرای عملیات با isolation level بالا
     * 
     * @param sessionFactory factory برای ایجاد session
     * @param operation عملیات برای اجرا
     * @param <T> نوع نتیجه
     * @return نتیجه عملیات
     */
    public static <T> T executeWithHighIsolation(SessionFactory sessionFactory, Supplier<T> operation) {
        return executeWithRetry(() -> {
            Session session = null;
            Transaction transaction = null;
            try {
                session = sessionFactory.openSession();
                // تنظیم isolation level بالا برای جلوگیری از conflicts
                transaction = session.beginTransaction();
                T result = operation.get();
                transaction.commit();
                return result;
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (Exception rollbackEx) {
                        logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                    }
                }
                throw e;
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        });
    }
    
    /**
     * اجرای عملیات با timeout
     * 
     * @param operation عملیات برای اجرا
     * @param timeoutMs timeout (میلی‌ثانیه)
     * @param <T> نوع نتیجه
     * @return نتیجه عملیات
     */
    public static <T> T executeWithTimeout(Supplier<T> operation, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        
        return executeWithRetry(() -> {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
            }
            return operation.get();
        });
    }
} 
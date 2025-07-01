package com.myapp.common.utils;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * مدیریت تراکنش‌های SQLite با قابلیت retry و مدیریت قفل
 * 
 * این کلاس برای حل مشکلات همزمانیت SQLite طراحی شده و شامل:
 * - Retry logic برای خطاهای قفل دیتابیس
 * - مدیریت صحیح session و transaction
 * - Timeout مناسب برای عملیات
 */
public class SQLiteTransactionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SQLiteTransactionManager.class);
    
    // تنظیمات retry برای SQLite
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 100;
    private static final long MAX_WAIT_TIME_MS = 5000;
    
    /**
     * اجرای عملیات با retry logic برای SQLite
     * 
     * @param operation عملیات مورد نظر
     * @param operationName نام عملیات برای logging
     * @return نتیجه عملیات
     */
    public static <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        int attempts = 0;
        long startTime = System.currentTimeMillis();
        
        while (attempts < MAX_RETRIES) {
            try {
                attempts++;
                logger.debug("Attempting {} (attempt {}/{})", operationName, attempts, MAX_RETRIES);
                
                T result = operation.get();
                logger.debug("{} completed successfully on attempt {}", operationName, attempts);
                return result;
                
            } catch (Exception e) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                
                // بررسی اینکه آیا خطا مربوط به قفل SQLite است
                if (isSQLiteLockException(e) && attempts < MAX_RETRIES && elapsedTime < MAX_WAIT_TIME_MS) {
                    logger.warn("SQLite lock detected for {} (attempt {}/{}), retrying in {}ms", 
                        operationName, attempts, MAX_RETRIES, RETRY_DELAY_MS);
                    
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Operation interrupted: " + operationName, ie);
                    }
                    
                    continue;
                }
                
                // اگر خطا مربوط به قفل نیست یا تعداد retry تمام شده
                logger.error("{} failed after {} attempts: {}", operationName, attempts, e.getMessage());
                throw new RuntimeException("Operation failed: " + operationName, e);
            }
        }
        
        throw new RuntimeException("Operation failed after " + MAX_RETRIES + " attempts: " + operationName);
    }
    
    /**
     * اجرای عملیات با session و transaction مدیریت شده
     * 
     * @param sessionFunction تابعی که session را دریافت و عملیات را انجام می‌دهد
     * @param operationName نام عملیات
     * @return نتیجه عملیات
     */
    public static <T> T executeInTransaction(Function<Session, T> sessionFunction, String operationName) {
        return executeWithRetry(() -> {
            Session session = null;
            Transaction transaction = null;
            
            try {
                session = DatabaseUtil.getSessionFactory().openSession();
                transaction = session.beginTransaction();
                
                T result = sessionFunction.apply(session);
                
                transaction.commit();
                return result;
                
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    try {
                        transaction.rollback();
                    } catch (Exception rollbackEx) {
                        logger.warn("Error rolling back transaction: {}", rollbackEx.getMessage());
                    }
                }
                throw e;
            } finally {
                if (session != null && session.isOpen()) {
                    try {
                        session.close();
                    } catch (Exception closeEx) {
                        logger.warn("Error closing session: {}", closeEx.getMessage());
                    }
                }
            }
        }, operationName);
    }
    
    /**
     * اجرای عملیات void با session و transaction مدیریت شده
     * 
     * @param sessionConsumer consumer که session را دریافت و عملیات را انجام می‌دهد
     * @param operationName نام عملیات
     */
    public static void executeInTransactionVoid(java.util.function.Consumer<Session> sessionConsumer, String operationName) {
        executeInTransaction(session -> {
            sessionConsumer.accept(session);
            return null;
        }, operationName);
    }
    
    /**
     * بررسی اینکه آیا خطا مربوط به قفل SQLite است
     * 
     * @param e خطای بررسی شده
     * @return true اگر خطا مربوط به قفل SQLite باشد
     */
    private static boolean isSQLiteLockException(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;
        
        return message.contains("database is locked") ||
               message.contains("SQLITE_BUSY") ||
               message.contains("SQLITE_BUSY_SNAPSHOT") ||
               message.contains("LockAcquisitionException") ||
               (e instanceof LockAcquisitionException);
    }
    
    /**
     * انتظار برای آزاد شدن قفل دیتابیس
     * 
     * @param maxWaitTimeMs حداکثر زمان انتظار به میلی‌ثانیه
     */
    public static void waitForDatabaseLock(long maxWaitTimeMs) {
        long startTime = System.currentTimeMillis();
        long checkInterval = 50; // هر 50 میلی‌ثانیه بررسی کن
        
        while (System.currentTimeMillis() - startTime < maxWaitTimeMs) {
            try {
                // تلاش برای یک عملیات ساده برای بررسی آزاد بودن قفل
                executeInTransaction(session -> {
                    session.createQuery("SELECT 1").uniqueResult();
                    return true;
                }, "database lock check");
                
                // اگر موفق بود، قفل آزاد شده
                return;
                
            } catch (Exception e) {
                if (!isSQLiteLockException(e)) {
                    throw e; // اگر خطای دیگری است، آن را throw کن
                }
                
                // انتظار و دوباره تلاش کن
                try {
                    Thread.sleep(checkInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for database lock", ie);
                }
            }
        }
        
        throw new RuntimeException("Database lock timeout after " + maxWaitTimeMs + "ms");
    }
} 
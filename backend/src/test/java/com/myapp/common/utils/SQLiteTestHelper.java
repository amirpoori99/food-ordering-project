package com.myapp.common.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * کلاس کمکی برای مدیریت مشکلات SQLite در تست‌ها
 * این کلاس شامل منطق retry و مدیریت اتصالات برای جلوگیری از خطاهای locking است
 */
public class SQLiteTestHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(SQLiteTestHelper.class);
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 100;
    
    /**
     * اجرای عملیات با retry logic برای جلوگیری از خطاهای SQLite locking
     * @param sessionFactory فکتوری session برای ایجاد اتصال جدید
     * @param operation عملیاتی که باید اجرا شود
     * @return نتیجه عملیات
     */
    public static <T> T executeWithRetry(SessionFactory sessionFactory, Callable<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            Session session = null;
            Transaction transaction = null;
            
            try {
                session = sessionFactory.openSession();
                transaction = session.beginTransaction();
                
                T result = operation.call();
                
                if (transaction.getStatus() == TransactionStatus.ACTIVE) {
                    transaction.commit();
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                
                if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
                    try {
                        transaction.rollback();
                    } catch (Exception rollbackEx) {
                        logger.warn("Error during rollback: {}", rollbackEx.getMessage());
                    }
                }
                
                // اگر خطای locking نیست، بلافاصله throw کن
                if (!isLockingError(e)) {
                    throw new RuntimeException("Non-locking error occurred", e);
                }
                
                logger.warn("SQLite locking error on attempt {}: {}", attempt, e.getMessage());
                
                // اگر آخرین تلاش است، خطا را throw کن
                if (attempt == MAX_RETRIES) {
                    throw new RuntimeException("Max retries exceeded for SQLite operation", e);
                }
                
                // کمی صبر کن قبل از تلاش مجدد
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry", ie);
                }
                
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
        
        throw new RuntimeException("Unexpected error in retry logic", lastException);
    }
    
    /**
     * اجرای عملیات void با retry logic
     * @param sessionFactory فکتوری session
     * @param operation عملیات void
     */
    public static void executeWithRetry(SessionFactory sessionFactory, Runnable operation) {
        executeWithRetry(sessionFactory, () -> {
            operation.run();
            return null;
        });
    }
    
    /**
     * بررسی اینکه آیا خطا مربوط به locking است یا خیر
     * @param e خطای بررسی شده
     * @return true اگر خطای locking باشد
     */
    private static boolean isLockingError(Exception e) {
        String message = e.getMessage();
        if (message == null) return false;
        
        return message.contains("SQLITE_BUSY") ||
               message.contains("database is locked") ||
               message.contains("database file is locked") ||
               message.contains("LockAcquisitionException") ||
               message.contains("OptimisticLock") ||
               message.contains("LogicalConnectionManagedImpl is closed");
    }
    
    /**
     * ایجاد session جدید با بررسی وضعیت اتصال
     * @param sessionFactory فکتوری session
     * @return session جدید
     */
    public static Session createNewSession(SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        
        // بررسی اینکه session معتبر است
        if (!session.isOpen()) {
            throw new IllegalStateException("Failed to create valid session");
        }
        
        return session;
    }
    
    /**
     * بستن امن session
     * @param session session برای بستن
     */
    public static void closeSessionSafely(Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn("Error closing session: {}", e.getMessage());
            }
        }
    }
    
    /**
     * rollback امن transaction
     * @param transaction transaction برای rollback
     */
    public static void rollbackSafely(Transaction transaction) {
        if (transaction != null && transaction.getStatus() == TransactionStatus.ACTIVE) {
            try {
                transaction.rollback();
            } catch (Exception rollbackEx) {
                logger.warn("Error during rollback: {}", rollbackEx.getMessage());
            }
        }
    }
    
    /**
     * پاک کردن کامل دیتابیس با retry logic
     * @param sessionFactory فکتوری session
     * @param cleanupOperation عملیات پاک‌سازی
     */
    public static void cleanupDatabaseWithRetry(SessionFactory sessionFactory, Runnable cleanupOperation) {
        executeWithRetry(sessionFactory, () -> {
            cleanupOperation.run();
            return null;
        });
    }
}

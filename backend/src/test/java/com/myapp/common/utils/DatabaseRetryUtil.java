package com.myapp.common.utils;

import org.hibernate.exception.LockAcquisitionException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * کلاس کمکی برای مدیریت مشکلات SQLite locking با retry logic
 * 
 * این کلاس برای حل مشکلات concurrent access در محیط تست طراحی شده
 * و از SQLite busy/locked exceptions محافظت می‌کند.
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class DatabaseRetryUtil {

    private static final int MAX_RETRIES = 5;
    private static final int BASE_DELAY_MS = 50;
    private static final int MAX_DELAY_MS = 2000;

    /**
     * اجرای عملیات با retry logic برای مقابله با database locks
     * 
     * @param operation عملیات برای اجرا
     * @param description توضیح عملیات برای لاگ
     * @param <T> نوع نتیجه
     * @return نتیجه عملیات
     * @throws RuntimeException اگر پس از تمام تلاش‌ها موفق نشود
     */
    public static <T> T executeWithRetry(Supplier<T> operation, String description) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                // Check if it's a database locking issue
                if (isDatabaseLockException(e)) {
                    lastException = e;
                    if (attempt == MAX_RETRIES) {
                        break;
                    }
                    
                    // Exponential backoff with jitter
                    int delay = Math.min(BASE_DELAY_MS * (1 << (attempt - 1)), MAX_DELAY_MS);
                    delay += ThreadLocalRandom.current().nextInt(delay / 2); // Add jitter
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during database retry: " + description, ie);
                    }
                    
                    System.out.println("⚠️ Database lock detected on " + description + 
                                     ", retrying in " + delay + "ms (attempt " + attempt + "/" + MAX_RETRIES + ")");
                } else {
                    // Non-lock related exceptions should not be retried
                    throw new RuntimeException("Error during " + description + ": " + e.getMessage(), e);
                }
            }
        }
        
        throw new RuntimeException("Failed to complete " + description + " after " + MAX_RETRIES + 
                                 " attempts due to database locking", lastException);
    }

    /**
     * اجرای عملیات void با retry logic
     * 
     * @param operation عملیات برای اجرا
     * @param description توضیح عملیات برای لاگ
     */
    public static void executeWithRetry(Runnable operation, String description) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, description);
    }

    /**
     * تلاش برای اجرای عملیات با graceful handling
     * در صورت شکست، exception پرتاب نمی‌کند
     * 
     * @param operation عملیات برای اجرا
     * @param description توضیح عملیات
     * @return true اگر موفق باشد، false در غیر این صورت
     */
    public static boolean tryExecute(Runnable operation, String description) {
        try {
            executeWithRetry(operation, description);
            return true;
        } catch (Exception e) {
            System.out.println("⚠️ Failed to execute " + description + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * تلاش برای اجرای عملیات با default value در صورت شکست
     * 
     * @param operation عملیات برای اجرا
     * @param defaultValue مقدار پیش‌فرض در صورت شکست
     * @param description توضیح عملیات
     * @param <T> نوع نتیجه
     * @return نتیجه عملیات یا مقدار پیش‌فرض
     */
    public static <T> T executeWithDefault(Supplier<T> operation, T defaultValue, String description) {
        try {
            return executeWithRetry(operation, description);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to execute " + description + ", returning default value: " + e.getMessage());
            return defaultValue;
        }
    }

    /**
     * تشخیص اینکه آیا exception مربوط به database locking است یا نه
     * 
     * @param e exception برای بررسی
     * @return true اگر مربوط به database lock باشد
     */
    private static boolean isDatabaseLockException(Exception e) {
        if (e instanceof LockAcquisitionException) {
            return true;
        }
        
        String message = e.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("database is locked") ||
                   lowerMessage.contains("sqlite_busy") ||
                   lowerMessage.contains("lock acquisition") ||
                   lowerMessage.contains("could not execute batch") ||
                   lowerMessage.contains("connection has already written");
        }
        
        return false;
    }
} 
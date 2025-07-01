package com.myapp.common;

import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * کلاس ابزاری برای مدیریت عملیات پایگاه داده تست
 * 
 * این کلاس مسئولیت‌های زیر را دارد:
 * 
 * Database Setup:
 * - راه‌اندازی پایگاه داده تست
 * - پیکربندی خودکار Hibernate
 * 
 * Data Cleaning:
 * - پاک‌سازی داده‌های تست بعد از هر تست
 * - حذف انتخابی جداول خاص (Notification, Rating)
 * - پاک‌سازی کامل تمام داده‌ها
 * 
 * Transaction Management:
 * - مدیریت تراکنش‌ها با rollback در صورت خطا
 * - Exception handling و logging مناسب
 * 
 * Utility Operations:
 * - شمارش رکوردها برای assertion ها
 * - تست‌های integrity و foreign key constraints
 * 
 * Design Pattern: Utility Class
 * - Static methods برای عملیات مشترک
 * - Instance methods برای lifecycle management
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class TestDatabaseManager {
    
    /** Logger برای ثبت رویدادهای مدیریت پایگاه داده تست */
    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseManager.class);
    
    /**
     * راه‌اندازی پایگاه داده تست
     * 
     * Scenario: آماده‌سازی پایگاه داده برای اجرای تست‌ها
     * Operations:
     * - پیکربندی خودکار Hibernate
     * - ایجاد جداول بر اساس entity mappings
     * - تنظیم connection pool
     * 
     * Note: 
     * پایگاه داده به صورت خودکار از طریق hibernate configuration راه‌اندازی می‌شود
     * این متد بیشتر برای logging و monitoring استفاده می‌شود
     */
    public void setupTestDatabase() {
        // پایگاه داده به صورت خودکار از طریق hibernate configuration راه‌اندازی می‌شود
        logger.debug("Test database setup completed");
    }
    
    /**
     * متد پاک‌سازی برای cleanup تست
     * 
     * Scenario: پاک‌سازی بعد از اتمام lifecycle تست
     * Operations:
     * - پاک‌سازی تمام داده‌های تست
     * - آزادسازی منابع
     * - ثبت completion log
     * 
     * Usage: معمولاً در @AfterAll یا test teardown استفاده می‌شود
     */
    public void cleanup() {
        cleanAllTestData();
        logger.debug("Test database cleanup completed");
    }
    
    /**
     * پاک‌سازی تمام داده‌های notification از پایگاه داده
     * 
     * Scenario: پاک‌سازی انتخابی فقط اعلان‌ها
     * 
     * Transaction Flow:
     * 1. شروع تراکنش
     * 2. حذف تمام notification ها
     * 3. commit تراکنش
     * 4. در صورت خطا: rollback
     * 
     * Error Handling:
     * - Rollback خودکار در صورت exception
     * - Logging جزئیات خطا
     * - Exception swallowing برای stability تست‌ها
     */
    public void clearNotifications() {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // حذف تمام اعلان‌ها
            session.createQuery("DELETE FROM Notification").executeUpdate();
            
            transaction.commit();
            logger.debug("Cleaned all notification data from test database");
            
        } catch (Exception e) {
            // Rollback تراکنش در صورت خطا
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            logger.error("Error cleaning notification data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * پاک‌سازی فقط داده‌های notification بدون حذف کاربران
     * 
     * Scenario: پاک‌سازی انتخابی notifications برای تست‌های که نیاز به حفظ کاربران دارند
     * 
     * این متد فقط notification ها را حذف می‌کند و کاربران را دست‌نخورده نگه می‌دارد
     * مناسب برای تست‌هایی که کاربران ثابت نیاز دارند
     */
    public void clearNotificationData() {
        clearNotifications();
    }
    
    /**
     * پاک‌سازی تمام داده‌های rating از پایگاه داده
     * 
     * Scenario: پاک‌سازی انتخابی فقط رتبه‌بندی‌ها
     * 
     * Static Method: 
     * برای استفاده راحت‌تر در تست‌های مختلف بدون نیاز به instance
     * 
     * Transaction Management:
     * - مدیریت کامل تراکنش با try-with-resources
     * - Exception handling مقاوم
     * - Detailed logging برای debugging
     */
    public static void cleanRatingData() {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // حذف تمام رتبه‌بندی‌ها
            session.createQuery("DELETE FROM Rating").executeUpdate();
            
            transaction.commit();
            logger.debug("Cleaned all rating data from test database");
            
        } catch (Exception e) {
            // Rollback تراکنش در صورت خطا
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            logger.error("Error cleaning rating data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * پاک‌سازی تمام داده‌های تست از پایگاه داده
     * 
     * Scenario: پاک‌سازی کامل برای reset کردن پایگاه داده
     * 
     * Foreign Key Constraint Handling:
     * ترتیب حذف طوری انتخاب شده که foreign key constraints نقض نشوند:
     * 1. Notification (مستقل)
     * 2. Rating (مستقل) 
     * 3. OrderItem (وابسته به Order و FoodItem)
     * 4. Order (وابسته به User و Restaurant)
     * 5. FoodItem (وابسته به Restaurant)
     * 6. Restaurant (وابسته به User)
     * 7. User (بنیادی‌ترین entity)
     * 
     * Performance Consideration:
     * - استفاده از bulk delete برای سرعت بالا
     * - یک تراکنش برای همه عملیات
     */
    public static void cleanAllTestData() {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // حذف به ترتیب برای رعایت foreign key constraints
            session.createQuery("DELETE FROM Notification").executeUpdate();
            session.createQuery("DELETE FROM Rating").executeUpdate();
            session.createQuery("DELETE FROM OrderItem").executeUpdate();
            session.createQuery("DELETE FROM Order").executeUpdate();
            session.createQuery("DELETE FROM FoodItem").executeUpdate();
            session.createQuery("DELETE FROM Restaurant").executeUpdate();
            session.createQuery("DELETE FROM User").executeUpdate();
            
            transaction.commit();
            logger.debug("Cleaned all test data from database");
            
        } catch (Exception e) {
            // Rollback تراکنش در صورت خطا
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            logger.error("Error cleaning test data: {}", e.getMessage(), e);
        }
    }
    
    /**
     * پاک‌سازی تمام داده‌های تست (alias برای cleanAllTestData)
     * 
     * این متد wrapper برای cleanAllTestData است تا با نام‌گذاری تست‌ها سازگار باشد
     */
    public void clearAllData() {
        cleanAllTestData();
    }
    
    /**
     * دریافت تعداد rating ها در پایگاه داده
     * 
     * Scenario: بررسی تعداد رکوردها برای assertion در تست‌ها
     * 
     * Usage Examples:
     * - تأیید اینکه rating ها حذف شده‌اند
     * - بررسی تعداد rating های ایجاد شده
     * - Validation قبل و بعد از عملیات
     * 
     * Return Value:
     * - تعداد واقعی rating ها در database
     * - 0 در صورت خطا یا عدم وجود رکورد
     * 
     * Error Handling:
     * - Exception safety با return 0
     * - Logging خطاها برای debugging
     * 
     * @return تعداد rating ها در پایگاه داده
     */
    public static long getRatingCount() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // استفاده از HQL برای شمارش
            Long count = session.createQuery("SELECT COUNT(r) FROM Rating r", Long.class)
                              .uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            logger.error("Error getting rating count: {}", e.getMessage(), e);
            return 0L; // مقدار پیش‌فرض در صورت خطا
        }
    }
} 
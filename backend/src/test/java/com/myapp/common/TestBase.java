package com.myapp.common;

import com.myapp.common.utils.DatabaseUtil;
import com.myapp.common.utils.TestDataHelper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * کلاس پایه برای تست‌های یکپارچگی
 * این کلاس مدیریت session و transaction را برای تست‌ها فراهم می‌کند
 * 
 * ویژگی‌های کلیدی:
 * - مدیریت خودکار session و transaction
 * - پاک‌سازی داده‌ها بین تست‌ها
 * - تولید داده‌های یکتا برای تست‌ها
 * - عدم بستن sessionFactory برای جلوگیری از خطاهای EntityManagerFactory is closed
 * 
 * @author Food Ordering System Team
 * @version 2.0 - Fixed EntityManagerFactory issues
 */
public abstract class TestBase {
    
    protected SessionFactory sessionFactory;
    protected Session session;
    protected Transaction transaction;
    
    @BeforeEach
    public void setUp() {
        try {
            // Ensure sessionFactory is ready before each test
            TestDatabaseManager.ensureSessionFactoryReady();
            sessionFactory = DatabaseUtil.getSessionFactory();
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            
            // پاک‌سازی داده‌های تست قبل از هر تست
            cleanDatabaseSafely();
            
        } catch (Exception e) {
            System.err.println("❌ Error in TestBase setup: " + e.getMessage());
            throw new RuntimeException("TestBase setup failed", e);
        }
    }
    
    @AfterEach
    public void tearDown() {
        try {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            
            if (session != null && session.isOpen()) {
                session.close();
            }
            
            // پاک‌سازی داده‌های تست بعد از هر تست
            cleanDatabaseSafely();
            
        } catch (Exception e) {
            System.err.println("❌ Error in TestBase teardown: " + e.getMessage());
            // Don't throw exception in teardown to avoid masking test failures
        }
    }
    
    /**
     * پاک‌سازی امن دیتابیس - فقط داده‌ها را پاک می‌کند، sessionFactory را نمی‌بندد
     */
    protected void cleanDatabaseSafely() {
        Session cleanupSession = null;
        Transaction cleanupTransaction = null;
        try {
            // استفاده از sessionFactory موجود
            cleanupSession = sessionFactory.openSession();
            cleanupTransaction = cleanupSession.beginTransaction();
            
            // ترتیب پاک‌سازی جداول بر اساس وابستگی‌ها
            // استفاده از نام‌های واقعی جداول در دیتابیس
            String[] tables = {
                "ratings",           // جدول rating
                "notifications",     // جدول notification  
                "transactions",      // جدول transaction
                "order_items",       // جدول order_item
                "food_items",        // جدول food_item
                "orders",           // جدول order
                "restaurants",      // جدول restaurant
                "users"             // جدول user
            };
            
            for (String table : tables) {
                try {
                    int deletedRows = cleanupSession.createNativeQuery("DELETE FROM " + table).executeUpdate();
                    if (deletedRows > 0) {
                        System.out.println("🧹 Cleaned " + deletedRows + " rows from " + table);
                    }
                } catch (Exception e) {
                    // جدول ممکن است وجود نداشته باشد، نادیده بگیر
                    System.out.println("⚠️ Could not clean table " + table + ": " + e.getMessage());
                }
            }
            
            // Reset SQLite sequence
            try {
                cleanupSession.createNativeQuery("DELETE FROM sqlite_sequence").executeUpdate();
            } catch (Exception e) {
                // نادیده بگیر اگر جدول وجود نداشته باشد
            }
            
            cleanupTransaction.commit();
            
        } catch (Exception e) {
            if (cleanupTransaction != null && cleanupTransaction.isActive()) {
                cleanupTransaction.rollback();
            }
            System.err.println("❌ Error cleaning database: " + e.getMessage());
        } finally {
            if (cleanupSession != null && cleanupSession.isOpen()) {
                cleanupSession.close();
            }
        }
    }
    
    /**
     * تولید داده‌های یکتا برای تست‌ها
     */
    protected String generateUniquePhone() {
        return TestDataHelper.generateUniquePhone();
    }
    
    protected String generateUniqueEmail() {
        return TestDataHelper.generateUniqueEmail();
    }
    
    protected Long generateUniqueUserId() {
        return TestDataHelper.generateUniqueUserId();
    }
} 
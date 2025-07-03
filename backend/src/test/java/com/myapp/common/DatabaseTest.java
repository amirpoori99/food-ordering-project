package com.myapp.common;

import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * تست اتصال دیتابیس و عملیات پایه
 */
public class DatabaseTest {
    
    @BeforeAll
    static void setUp() {
        // بررسی اتصال دیتابیس
        assertNotNull(DatabaseUtil.getSessionFactory());
    }
    
    @Test
    void testDatabaseConnection() {
        Session session = DatabaseUtil.getSessionFactory().openSession();
        assertTrue(session.isOpen());
        session.close();
    }
    
    @Test
    void testUserSaveAndRetrieve() {
        Session session = DatabaseUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        
        try {
            // ایجاد کاربر تست
            User testUser = new User();
            testUser.setFullName("Test User " + System.currentTimeMillis());
            testUser.setPhone("09123456789");
            testUser.setEmail("test@example.com");
            testUser.setPasswordHash("test_password_hash");
            testUser.setRole(User.Role.BUYER);
            
            // ذخیره کاربر
            session.save(testUser);
            transaction.commit();
            
            // بازیابی کاربر
            User retrievedUser = session.get(User.class, testUser.getId());
            assertNotNull(retrievedUser);
            assertEquals(testUser.getFullName(), retrievedUser.getFullName());
            assertEquals(testUser.getPhone(), retrievedUser.getPhone());
            assertEquals(testUser.getEmail(), retrievedUser.getEmail());
            
            // پاک کردن کاربر تست
            transaction = session.beginTransaction();
            session.delete(retrievedUser);
            transaction.commit();
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            fail("خطا در تست دیتابیس: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    @AfterAll
    static void tearDown() {
        // بستن اتصال دیتابیس - حذف شده چون باعث بسته شدن SessionFactory می‌شود
        // و تست‌های بعدی را با خطای EntityManagerFactory is closed مواجه می‌کند
        // DatabaseUtil.shutdown();
    }
} 
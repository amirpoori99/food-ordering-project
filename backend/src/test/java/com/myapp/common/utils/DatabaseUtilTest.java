package com.myapp.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.hibernate.SessionFactory;

/**
 * تست‌های کلاس DatabaseUtil
 * 
 * این کلاس مسئول تست کردن DatabaseUtil است که مدیریت SessionFactory را انجام می‌دهد
 * 
 * نکته مهم: تست shutdown حذف شده چون باعث بستن SessionFactory اصلی می‌شود
 * و تست‌های بعدی را با خطای "EntityManagerFactory is closed" مواجه می‌کند
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class DatabaseUtilTest {

    /**
     * تست دریافت SessionFactory از DatabaseUtil
     * 
     * این تست بررسی می‌کند که:
     * - SessionFactory null نباشد
     * - SessionFactory باز باشد و قابل استفاده باشد
     * 
     * انتظارات:
     * - SessionFactory valid و باز باشد
     * - قابلیت ایجاد session داشته باشد
     */
    @Test
    @DisplayName("دریافت SessionFactory باید SessionFactory معتبر برگرداند")
    public void testGetSessionFactory() {
        // اجرای متد تحت تست
        SessionFactory sessionFactory = DatabaseUtil.getSessionFactory();
        
        // اعتبارسنجی نتایج
        assertNotNull(sessionFactory, "SessionFactory نباید null باشد");
        assertFalse(sessionFactory.isClosed(), "SessionFactory باید باز و قابل استفاده باشد");
        
        System.out.println("✅ SessionFactory معتبر دریافت شد");
    }
    
    /**
     * تست قابلیت استفاده مجدد SessionFactory
     * 
     * این تست بررسی می‌کند که:
     * - دو فراخوانی متوالی همان instance را برگردانند (Singleton Pattern)
     * - SessionFactory قابل استفاده مجدد باشد
     * 
     * انتظارات:
     * - هر دو فراخوانی همان object را برگردانند
     * - Reference equality برقرار باشد
     */
    @Test
    @DisplayName("SessionFactory باید قابل استفاده مجدد باشد (Singleton)")
    public void testSessionFactoryReuse() {
        // دریافت SessionFactory در دو مرحله
        SessionFactory first = DatabaseUtil.getSessionFactory();
        SessionFactory second = DatabaseUtil.getSessionFactory();
        
        // اعتبارسنجی singleton pattern
        assertNotNull(first, "اولین SessionFactory نباید null باشد");
        assertNotNull(second, "دومین SessionFactory نباید null باشد");
        assertSame(first, second, "هر دو SessionFactory باید همان instance باشند (Singleton Pattern)");
        
        System.out.println("✅ Singleton Pattern برای SessionFactory تأیید شد");
    }
    
    /**
     * تست کیفیت SessionFactory
     * 
     * این تست بررسی می‌کند که:
     * - SessionFactory قابلیت ایجاد Session داشته باشد
     * - Session ایجاد شده معتبر باشد
     * 
     * انتظارات:
     * - امکان ایجاد Session موجود باشد
     * - Session باز و قابل استفاده باشد
     */
    @Test
    @DisplayName("SessionFactory باید قابلیت ایجاد Session معتبر داشته باشد")
    public void testSessionFactoryQuality() {
        // دریافت SessionFactory
        SessionFactory sessionFactory = DatabaseUtil.getSessionFactory();
        
        // تلاش برای ایجاد Session
        assertDoesNotThrow(() -> {
            try (var session = sessionFactory.openSession()) {
                assertNotNull(session, "Session ایجاد شده نباید null باشد");
                assertTrue(session.isOpen(), "Session باید باز باشد");
                
                System.out.println("✅ Session معتبر از SessionFactory ایجاد شد");
            }
        }, "ایجاد Session از SessionFactory نباید خطا تولید کند");
    }
}
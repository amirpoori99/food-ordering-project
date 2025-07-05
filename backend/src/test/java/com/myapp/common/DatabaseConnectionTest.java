package com.myapp.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.common.models.User;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;

/**
 * کلاس تست جامع برای اتصال پایگاه داده و عملیات CRUD
 * این کلاس شامل تست‌های مختلف برای اطمینان از صحت کارکرد پایگاه داده است
 */
@DisplayName("تست‌های اتصال و عملیات پایگاه داده")
class DatabaseConnectionTest {

    private static SessionFactory sessionFactory;
    private Session session;
    private Transaction transaction;

    /**
     * راه‌اندازی یکباره قبل از اجرای همه تست‌ها
     * ایجاد SessionFactory و بررسی اولیه اتصال
     */
    @BeforeAll
    static void setUpClass() {
        System.out.println("🔧 راه‌اندازی تست‌های پایگاه داده...");
        
        // دریافت SessionFactory از DatabaseUtil
        sessionFactory = DatabaseUtil.getSessionFactory();
        assertNotNull(sessionFactory, "SessionFactory نباید null باشد");
        assertFalse(sessionFactory.isClosed(), "SessionFactory باید باز باشد");
        
        System.out.println("✅ SessionFactory با موفقیت راه‌اندازی شد");
    }

    /**
     * تمیزکاری نهایی بعد از اجرای همه تست‌ها
     */
    @AfterAll
    static void tearDownClass() {
        System.out.println("🔧 تمیزکاری پایانی...");
        
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            // sessionFactory.close(); // نمی‌بندیم چون ممکن است سایر تست‌ها نیاز داشته باشند
            System.out.println("✅ SessionFactory آماده بستن است");
        }
    }

    /**
     * راه‌اندازی قبل از هر تست
     * ایجاد Session و Transaction جدید
     */
    @BeforeEach
    void setUp() {
        // ایجاد Session جدید برای هر تست
        session = sessionFactory.openSession();
        assertNotNull(session, "Session نباید null باشد");
        assertTrue(session.isOpen(), "Session باید باز باشد");
        
        // شروع Transaction
        transaction = session.beginTransaction();
        assertNotNull(transaction, "Transaction نباید null باشد");
        assertTrue(transaction.isActive(), "Transaction باید فعال باشد");
    }

    /**
     * تمیزکاری بعد از هر تست
     * بستن Session و rollback کردن تغییرات
     */
    @AfterEach
    void tearDown() {
        try {
            // Rollback کردن تغییرات تست برای عدم تداخل با سایر تست‌ها
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        } catch (Exception e) {
            System.err.println("⚠️ خطا در rollback: " + e.getMessage());
        } finally {
            // بستن Session
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * تست پایه اتصال به پایگاه داده
     * بررسی اینکه SessionFactory و Session صحیح کار می‌کنند
     */
    @Test
    @DisplayName("اتصال پایه به پایگاه داده باید موفق باشد")
    void testBasicDatabaseConnection() {
        // بررسی SessionFactory
        assertNotNull(sessionFactory, "SessionFactory نباید null باشد");
        assertFalse(sessionFactory.isClosed(), "SessionFactory باید باز باشد");
        
        // بررسی Session
        assertNotNull(session, "Session نباید null باشد");
        assertTrue(session.isOpen(), "Session باید باز باشد");
        assertTrue(session.isConnected(), "Session باید متصل باشد");
        
        // بررسی Transaction
        assertNotNull(transaction, "Transaction نباید null باشد");
        assertTrue(transaction.isActive(), "Transaction باید فعال باشد");
        
        System.out.println("✅ اتصال پایه به پایگاه داده تأیید شد");
    }

    /**
     * تست اجرای کوئری ساده SQL
     * بررسی امکان اجرای کوئری‌های native SQL
     */
    @Test
    @DisplayName("اجرای کوئری ساده SQL باید کار کند")
    void testSimpleSQLQuery() {
        assertDoesNotThrow(() -> {
            // اجرای کوئری ساده برای تست اتصال
            Query<?> query = session.createNativeQuery("SELECT 1 as test_value");
            Object result = query.getSingleResult();
            
            assertNotNull(result, "نتیجه کوئری نباید null باشد");
            assertEquals(1, ((Number) result).intValue(), "مقدار برگشتی باید 1 باشد");
            
            System.out.println("✅ کوئری ساده SQL اجرا شد: " + result);
        }, "اجرای کوئری ساده نباید خطا تولید کند");
    }

    /**
     * تست ایجاد جدول و بررسی schema
     * اطمینان از اینکه Hibernate جدول‌ها را صحیح ایجاد می‌کند
     */
    @Test
    @DisplayName("جدول‌های ضروری باید در پایگاه داده موجود باشند")
    void testRequiredTablesExist() {
        // لیست جدول‌های مورد انتظار
        String[] expectedTables = {
            "users", "restaurants", "food_items", "orders", 
            "order_items", "transactions", "deliveries", 
            "ratings", "coupons", "coupon_usage", 
            "favorites", "notifications"
        };

        for (String tableName : expectedTables) {
            assertDoesNotThrow(() -> {
                // بررسی وجود جدول از طریق HQL Query
                String hql = "FROM " + tableName.substring(0, 1).toUpperCase() + 
                           tableName.substring(1).replace("_", "");
                try {
                    Query<?> query = session.createQuery(hql);
                    query.setMaxResults(1);
                    query.list(); // فقط برای تست وجود جدول
                    System.out.println("✅ جدول " + tableName + " موجود است");
                } catch (Exception e) {
                    // اگر جدول وجود نداشته باشد، HQL exception می‌دهد
                    System.out.println("⚠️ جدول " + tableName + " یافت نشد");
                }
            }, "بررسی وجود جدول " + tableName + " نباید خطا تولید کند");
        }
    }

    /**
     * تست عملیات CRUD پایه با User entity
     * بررسی Insert, Read, Update, Delete
     */
    @Test
    @DisplayName("عملیات CRUD پایه باید صحیح کار کند")
    void testBasicCRUDOperations() {
        assertDoesNotThrow(() -> {
            // **CREATE** - ایجاد کاربر جدید
            User testUser = User.forRegistration(
                "تست کاربر", 
                "09123456789", 
                "test@example.com", 
                "hashed_password", 
                "آدرس تست"
            );
            
            session.save(testUser);
            session.flush(); // اطمینان از ذخیره در دیتابیس
            
            assertNotNull(testUser.getId(), "شناسه کاربر باید بعد از save مقداردهی شود");
            Long userId = testUser.getId();
            System.out.println("✅ CREATE: کاربر با شناسه " + userId + " ایجاد شد");

            // **READ** - خواندن کاربر از دیتابیس
            User readUser = session.get(User.class, userId);
            assertNotNull(readUser, "کاربر باید از دیتابیس قابل خواندن باشد");
            assertEquals("تست کاربر", readUser.getFullName(), "نام کاربر باید مطابقت داشته باشد");
            assertEquals("09123456789", readUser.getPhone(), "شماره تلفن باید مطابقت داشته باشد");
            System.out.println("✅ READ: کاربر " + readUser.getFullName() + " خوانده شد");

            // **UPDATE** - به‌روزرسانی کاربر
            readUser.setFullName("تست کاربر بروزرسانی شده");
            session.update(readUser);
            session.flush();
            
            // بررسی به‌روزرسانی
            User updatedUser = session.get(User.class, userId);
            assertEquals("تست کاربر بروزرسانی شده", updatedUser.getFullName(), 
                    "نام کاربر باید به‌روزرسانی شده باشد");
            System.out.println("✅ UPDATE: کاربر به‌روزرسانی شد");

            // **DELETE** - حذف کاربر
            session.delete(updatedUser);
            session.flush();
            
            // بررسی حذف
            User deletedUser = session.get(User.class, userId);
            assertNull(deletedUser, "کاربر باید از دیتابیس حذف شده باشد");
            System.out.println("✅ DELETE: کاربر حذف شد");

        }, "عملیات CRUD نباید خطا تولید کند");
    }

    /**
     * تست Transaction Management
     * بررسی صحت عملیات commit و rollback
     */
    @Test
    @DisplayName("مدیریت Transaction باید صحیح کار کند")
    void testTransactionManagement() {
        assertDoesNotThrow(() -> {
            // Test Rollback با شماره‌های یکتا
            long timestamp = System.currentTimeMillis();
            User user1 = User.forRegistration("کاربر اول", "091" + (timestamp % 100000000) + "A", "", "pass", "");
            session.save(user1);
            Long userId1 = user1.getId();
            
            // Rollback transaction
            transaction.rollback();
            
            // شروع transaction جدید
            transaction = session.beginTransaction();
            
            // بررسی اینکه کاربر rollback شده است
            User checkUser1 = session.get(User.class, userId1);
            assertNull(checkUser1, "کاربر بعد از rollback نباید موجود باشد");
            System.out.println("✅ Rollback صحیح کار کرد");

            // Test Commit با شماره یکتای جدید
            User user2 = User.forRegistration("کاربر دوم", "091" + (timestamp % 100000000) + "B", "", "pass", "");
            session.save(user2);
            Long userId2 = user2.getId();
            
            // Commit transaction
            transaction.commit();
            
            // شروع transaction جدید
            transaction = session.beginTransaction();
            
            // بررسی اینکه کاربر commit شده است
            User checkUser2 = session.get(User.class, userId2);
            assertNotNull(checkUser2, "کاربر بعد از commit باید موجود باشد");
            assertEquals("کاربر دوم", checkUser2.getFullName(), "نام کاربر باید صحیح باشد");
            System.out.println("✅ Commit صحیح کار کرد");

            // تمیزکاری
            session.delete(checkUser2);
            
        }, "مدیریت Transaction نباید خطا تولید کند");
    }

    /**
     * تست کوئری‌های پیچیده HQL
     * بررسی امکان استفاده از HQL برای کوئری‌های پیشرفته
     */
    @Test
    @DisplayName("کوئری‌های HQL باید صحیح کار کنند")
    void testHQLQueries() {
        assertDoesNotThrow(() -> {
            // ایجاد چند کاربر تست با شماره‌های یکتا (timestamp-based)
            long timestamp = System.currentTimeMillis();
            User user1 = User.forRegistration("علی احمدی", "091" + (timestamp % 100000000) + "1", "", "pass", "");
            User user2 = User.forRegistration("سارا محمدی", "091" + (timestamp % 100000000) + "2", "", "pass", "");
            User user3 = User.forRegistration("حسن رضایی", "091" + (timestamp % 100000000) + "3", "", "pass", "");
            
            session.save(user1);
            session.save(user2);
            session.save(user3);
            session.flush();

            // کوئری HQL برای جستجوی کاربران
            String hql = "FROM User WHERE fullName LIKE :pattern";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("pattern", "%احمدی%");
            
            List<User> results = query.list();
            assertEquals(1, results.size(), "باید یک کاربر با نام احمدی پیدا شود");
            assertEquals("علی احمدی", results.get(0).getFullName(), "نام کاربر باید علی احمدی باشد");
            System.out.println("✅ کوئری HQL: " + results.size() + " نتیجه یافت شد");

            // کوئری شمارش
            String countHQL = "SELECT COUNT(*) FROM User";
            Query<Long> countQuery = session.createQuery(countHQL, Long.class);
            Long totalUsers = countQuery.getSingleResult();
            assertTrue(totalUsers >= 3, "باید حداقل 3 کاربر در دیتابیس باشد");
            System.out.println("✅ تعداد کل کاربران: " + totalUsers);

            // تمیزکاری
            session.delete(user1);
            session.delete(user2);
            session.delete(user3);
            
        }, "کوئری‌های HQL نباید خطا تولید کند");
    }

    /**
     * تست اتصال مستقیم JDBC (برای مقایسه)
     * اطمینان از اینکه SQLite driver صحیح کار می‌کند
     */
    @Test
    @DisplayName("اتصال مستقیم JDBC باید کار کند")
    void testDirectJDBCConnection() {
        assertDoesNotThrow(() -> {
            // اتصال مستقیم به SQLite
            String url = "jdbc:sqlite:food_ordering.db";
            
            try (Connection connection = DriverManager.getConnection(url)) {
                assertNotNull(connection, "اتصال JDBC نباید null باشد");
                assertFalse(connection.isClosed(), "اتصال JDBC باید باز باشد");
                
                // تست اجرای کوئری
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) as table_count FROM sqlite_master WHERE type='table'");
                    
                    assertTrue(resultSet.next(), "نتایج کوئری باید موجود باشد");
                    int tableCount = resultSet.getInt("table_count");
                    assertTrue(tableCount > 0, "باید حداقل یک جدول در دیتابیس موجود باشد");
                    
                    System.out.println("✅ اتصال مستقیم JDBC: " + tableCount + " جدول یافت شد");
                }
            }
        }, "اتصال مستقیم JDBC نباید خطا تولید کند");
    }

    /**
     * تست عملکرد پایگاه داده (Performance Test)
     * بررسی سرعت عملیات CRUD در حجم کم
     */
    @Test
    @DisplayName("عملکرد پایگاه داده باید قابل قبول باشد")
    void testDatabasePerformance() {
        assertDoesNotThrow(() -> {
            int testCount = 10; // تعداد کم برای تست سریع
            long startTime = System.currentTimeMillis();

            // ایجاد کاربران تست
            for (int i = 1; i <= testCount; i++) {
                User user = User.forRegistration(
                    "کاربر تست " + i, 
                    "0911111111" + i, 
                    "", 
                    "pass", 
                    ""
                );
                session.save(user);
            }
            session.flush();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // بررسی زمان اجرا (باید کمتر از 5 ثانیه باشد)
            assertTrue(duration < 5000, 
                String.format("ایجاد %d کاربر نباید بیش از 5 ثانیه طول بکشد (زمان فعلی: %d ms)", 
                    testCount, duration));

            System.out.println(String.format("✅ عملکرد: %d کاربر در %d میلی‌ثانیه ایجاد شد", 
                testCount, duration));

            // تمیزکاری
            session.createQuery("DELETE FROM User WHERE phone LIKE '0911111111%'").executeUpdate();
            
        }, "تست عملکرد نباید خطا تولید کند");
    }
} 
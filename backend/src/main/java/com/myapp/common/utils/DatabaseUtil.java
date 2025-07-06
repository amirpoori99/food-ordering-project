package com.myapp.common.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Utility Class - Environment-based Database Configuration
 * This class manages the Hibernate SessionFactory for the Food Ordering System
 * 
 * Environment Support:
 * - Development: SQLite (single-file database)
 * - Production: PostgreSQL (with HikariCP connection pooling)
 * - Test: SQLite in-memory database
 * 
 * @author Food Ordering System Team
 * @version 2.0 - Production Ready
 */
public class DatabaseUtil {
    // Lazy initialization - SessionFactory will be created when first accessed
    private static volatile SessionFactory sessionFactory;
    private static final Object lock = new Object();
    
    // Environment variables
    private static final String ENVIRONMENT = System.getProperty("app.environment", "development");
    private static final boolean IS_PRODUCTION = "production".equals(ENVIRONMENT);
    private static final boolean IS_TEST = isTestEnvironment();

    /**
     * ساخت SessionFactory با پیکربندی مناسب برای محیط
     */
    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            
            // تشخیص محیط و انتخاب فایل پیکربندی مناسب
            String configFile = isTestEnvironment() ? "hibernate-test.cfg.xml" : "hibernate.cfg.xml";
            System.out.println("🔧 Using Hibernate config file: " + configFile);
            System.out.println("🔧 Test environment detected: " + isTestEnvironment());
            
            configuration.configure(configFile);
            
            // در محیط تست، از auto-detection استفاده کن
            if (isTestEnvironment()) {
                System.out.println("🔧 Using auto-detection for entities in test environment");
            }
            
            // اضافه کردن package‌های entity به صورت دستی (همیشه)
            configuration.addPackage("com.myapp.common.models");
            configuration.addPackage("com.myapp.analytics.models");
            
            // اضافه کردن entityها به صورت دستی (همیشه)
            configuration.addAnnotatedClass(com.myapp.common.models.User.class);
            configuration.addAnnotatedClass(com.myapp.common.models.Restaurant.class);
            configuration.addAnnotatedClass(com.myapp.common.models.FoodItem.class);
            configuration.addAnnotatedClass(com.myapp.common.models.Order.class);
            configuration.addAnnotatedClass(com.myapp.common.models.OrderItem.class);
            configuration.addAnnotatedClass(com.myapp.common.models.Transaction.class);
            configuration.addAnnotatedClass(com.myapp.common.models.Rating.class);
            configuration.addAnnotatedClass(com.myapp.common.models.Coupon.class);
            configuration.addAnnotatedClass(com.myapp.common.models.CouponUsage.class);
            configuration.addAnnotatedClass(com.myapp.common.models.Favorite.class);
            configuration.addAnnotatedClass(com.myapp.common.models.Notification.class);
            
            // Analytics models
            configuration.addAnnotatedClass(com.myapp.analytics.models.OrderAnalytics.class);
            configuration.addAnnotatedClass(com.myapp.analytics.models.UserAnalytics.class);
            configuration.addAnnotatedClass(com.myapp.analytics.models.RestaurantAnalytics.class);
            configuration.addAnnotatedClass(com.myapp.analytics.models.PaymentAnalytics.class);
            configuration.addAnnotatedClass(com.myapp.analytics.models.ETLResult.class);
            configuration.addAnnotatedClass(com.myapp.analytics.models.DashboardMetrics.class);
            
            System.out.println("🔧 Entity classes added to Hibernate configuration");
            
            // Override with environment variables in production
            if (IS_PRODUCTION) {
                overrideWithEnvironmentVariables(configuration);
            }
            
            // ساخت SessionFactory
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties());
            
            return configuration.buildSessionFactory(builder.build());
        } catch (Exception e) {
            System.err.println("خطا در ساخت SessionFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
     * تشخیص محیط تست
     */
    private static boolean isTestEnvironment() {
        // فقط اگر متغیر سیستم test.environment=true باشد یا کلاس JUnit در استک باشد
        String testEnv = System.getProperty("test.environment");
        if (testEnv != null && testEnv.equals("true")) {
            return true;
        }
        // بررسی وجود کلاس‌های تست JUnit در استک
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Override configuration with environment variables for production
     * 
     * @param configuration Hibernate configuration to override
     */
    private static void overrideWithEnvironmentVariables(Configuration configuration) {
        String dbUrl = System.getenv("DATABASE_URL");
        String dbUsername = System.getenv("DATABASE_USERNAME");
        String dbPassword = System.getenv("DATABASE_PASSWORD");
        String maxPoolSize = System.getenv("DB_MAX_POOL_SIZE");
        String minPoolSize = System.getenv("DB_MIN_POOL_SIZE");
        
        if (dbUrl != null) {
            configuration.setProperty("hibernate.connection.url", dbUrl);
            System.out.println("   📊 Custom database URL configured");
        }
        if (dbUsername != null) {
            configuration.setProperty("hibernate.connection.username", dbUsername);
            System.out.println("   👤 Custom database username configured");
        }
        if (dbPassword != null) {
            configuration.setProperty("hibernate.connection.password", dbPassword);
            System.out.println("   🔒 Custom database password configured");
        }
        if (maxPoolSize != null) {
            configuration.setProperty("hibernate.hikari.maximumPoolSize", maxPoolSize);
            System.out.println("   🏊 Custom max pool size: " + maxPoolSize);
        }
        if (minPoolSize != null) {
            configuration.setProperty("hibernate.hikari.minimumIdle", minPoolSize);
            System.out.println("   🏊 Custom min pool size: " + minPoolSize);
        }
    }
    
    /**
     * Print available API endpoints for reference
     */
    private static void printAvailableEndpoints() {
        System.out.println("\n📡 Available API Endpoints:");
        System.out.println("   🔐 Authentication:");
        System.out.println("      POST /api/auth/login");
        System.out.println("      POST /api/auth/register");
        System.out.println("      POST /api/auth/logout");
        
        System.out.println("   🍽️ Restaurants:");
        System.out.println("      GET  /api/restaurants");
        System.out.println("      POST /api/restaurants");
        System.out.println("      GET  /api/restaurants/{id}");
        
        System.out.println("   🛒 Orders:");
        System.out.println("      GET  /api/orders");
        System.out.println("      POST /api/orders");
        System.out.println("      GET  /api/orders/{id}");
        
        System.out.println("   💳 Payments:");
        System.out.println("      POST /api/payments");
        System.out.println("      GET  /api/payments/{id}");
        System.out.println("      GET  /api/wallet");
        
        System.out.println("   📊 Analytics:");
        System.out.println("      GET  /api/analytics/dashboard");
        System.out.println("      GET  /api/analytics/revenue");
        System.out.println("      GET  /api/analytics/customers");
        System.out.println("      GET  /api/analytics/restaurants");
        
        System.out.println("   🔧 Admin:");
        System.out.println("      GET  /api/admin/dashboard");
        System.out.println("      GET  /api/admin/users");
        System.out.println("      GET  /api/admin/stats");
        
        System.out.println("\n🌐 Web Interface:");
        System.out.println("   Dashboard: http://localhost:8081/web/dashboard.html");
        System.out.println("   Analytics: http://localhost:8081/web/analytics.html");
        System.out.println("   Admin: http://localhost:8081/web/admin.html");
        
        System.out.println("\n📖 Documentation:");
        System.out.println("   API Reference: /docs/api-reference.html");
        System.out.println("   User Guide: /docs/user-guide.html");
        System.out.println("   Technical Docs: /docs/technical-architecture.html");
    }

    /**
     * دریافت نمونه SessionFactory برای استفاده در سایر کلاس‌ها
     * Lazy initialization pattern for thread safety
     * 
     * @return SessionFactory یکتای برنامه
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (lock) {
                if (sessionFactory == null) {
                    sessionFactory = buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }
    
    /**
     * Check if running in production environment
     * 
     * @return true if production, false if development
     */
    public static boolean isProduction() {
        return IS_PRODUCTION;
    }
    
    /**
     * Check if running in test environment
     * 
     * @return true if test, false otherwise
     */
    public static boolean isTest() {
        return IS_TEST;
    }
    
    /**
     * Get current environment name
     * 
     * @return Environment name (development/production/test)
     */
    public static String getEnvironment() {
        if (IS_TEST) return "test";
        return ENVIRONMENT;
    }

    /**
     * بستن SessionFactory در پایان برنامه
     * این متد معمولاً در shutdown hook فراخوانی می‌شود
     */
    public static void shutdown() {
        try {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
                System.out.println("🔒 SessionFactory closed successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Error closing SessionFactory: " + e.getMessage());
        }
    }

    /**
     * دریافت یک Connection JDBC متناسب با محیط جاری
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if (IS_PRODUCTION) {
            // PostgreSQL
            String url = System.getenv("DATABASE_URL");
            String username = System.getenv("DATABASE_USERNAME");
            String password = System.getenv("DATABASE_PASSWORD");
            if (url == null) url = "jdbc:postgresql://localhost:5432/food_ordering";
            if (username == null) username = "postgres";
            if (password == null) password = "postgres";
            return DriverManager.getConnection(url, username, password);
        } else if (IS_TEST) {
            // SQLite In-Memory for tests
            return DriverManager.getConnection("jdbc:sqlite::memory:");
        } else {
            // SQLite for development
            String url = "jdbc:sqlite:backend/food_ordering.db";
            return DriverManager.getConnection(url);
        }
    }
}


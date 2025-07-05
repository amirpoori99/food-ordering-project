package com.myapp.common.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Database Utility Class - Environment-based Database Configuration
 * This class manages the Hibernate SessionFactory for the Food Ordering System
 * 
 * Environment Support:
 * - Development: SQLite (single-file database)
 * - Production: PostgreSQL (with HikariCP connection pooling)
 * 
 * @author Food Ordering System Team
 * @version 2.0 - Production Ready
 */
public class DatabaseUtil {
    // نمونه یکتای SessionFactory برای کل برنامه
    private static final SessionFactory sessionFactory = buildSessionFactory();
    
    // Environment variables
    private static final String ENVIRONMENT = System.getProperty("app.environment", "development");
    private static final boolean IS_PRODUCTION = "production".equals(ENVIRONMENT);

    /**
     * ساخت SessionFactory با استفاده از Environment-based Configuration
     * 
     * Configurations:
     * - Development: SQLite with basic settings
     * - Production: PostgreSQL with HikariCP connection pooling
     * 
     * @return SessionFactory تنظیم شده برای محیط جاری
     * @throws ExceptionInInitializerError در صورت خطا در ایجاد SessionFactory
     */
    private static SessionFactory buildSessionFactory() {
        try {
            System.out.println("🚀 Starting Food Ordering Backend Server...");
            System.out.println("🔧 Database Configuration:");
            
            Configuration configuration;
            
            if (IS_PRODUCTION) {
                // Production Environment - PostgreSQL
                System.out.println("   Environment: Production");
                System.out.println("   Database: PostgreSQL");
                System.out.println("   Connection Pool: HikariCP");
                System.out.println("   Cache: Second-level cache enabled");
                
                // Load production configuration
                configuration = new Configuration().configure("hibernate-production.cfg.xml");
                
                // Override with environment variables if provided
                overrideWithEnvironmentVariables(configuration);
                
            } else {
                // Development Environment - SQLite
                System.out.println("   Environment: Development");
                System.out.println("   Database: SQLite");
                System.out.println("   Connection Pool: Basic");
                System.out.println("   Cache: Disabled for development");
                
                // Load development configuration
                configuration = new Configuration().configure("hibernate.cfg.xml");
            }
            
            // ساخت SessionFactory
            SessionFactory factory = configuration.buildSessionFactory();
            
            System.out.println("✅ Database connection successful!");
            System.out.println("🚀 Server started on http://localhost:8081");
            
            // Print available endpoints
            printAvailableEndpoints();
            
            return factory;
            
        } catch (Exception ex) {
            System.err.println("❌ Failed to create SessionFactory: " + ex.getMessage());
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
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
     * 
     * @return SessionFactory یکتای برنامه
     */
    public static SessionFactory getSessionFactory() {
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
     * Get current environment name
     * 
     * @return Environment name (development/production)
     */
    public static String getEnvironment() {
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
}


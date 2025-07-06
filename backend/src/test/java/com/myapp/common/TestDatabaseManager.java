package com.myapp.common;

import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Database Manager for managing test database operations
 * This class provides utilities for test-specific database operations
 * 
 * @author Food Ordering System Team
 * @version 1.0
 */
public class TestDatabaseManager {
    
    private static SessionFactory sessionFactory;
    
    /**
     * Initialize test database
     */
    public static void initialize() {
        try {
            sessionFactory = DatabaseUtil.getSessionFactory();
            System.out.println("✅ Test database initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize test database: " + e.getMessage());
            throw new RuntimeException("Test database initialization failed", e);
        }
    }
    
    /**
     * Get test session factory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            initialize();
        }
        return sessionFactory;
    }
    
    /**
     * Get a new test session
     */
    public static Session getSession() {
        return getSessionFactory().openSession();
    }
    
    /**
     * Clear all test data without closing the sessionFactory
     * This is safer for test isolation
     */
    public static void clearAllTestData() {
        try {
            clearUsers();
            clearRestaurants();
            cleanRatingData();
            clearNotifications();
            System.out.println("[TestDatabaseManager] All test data cleared");
        } catch (Exception e) {
            System.err.println("[TestDatabaseManager] Error clearing test data: " + e.getMessage());
        }
    }
    
    /**
     * Ensure sessionFactory is properly initialized and not closed
     * This method should be called before any test operations
     */
    public static void ensureSessionFactoryReady() {
        try {
            if (sessionFactory == null || sessionFactory.isClosed()) {
                System.out.println("[TestDatabaseManager] Reinitializing sessionFactory...");
                initialize();
            }
        } catch (Exception e) {
            System.err.println("[TestDatabaseManager] Error ensuring sessionFactory ready: " + e.getMessage());
            throw new RuntimeException("Failed to ensure sessionFactory is ready", e);
        }
    }
    
    /**
     * Clean up test database - only call this at the very end of all tests
     */
    public static void cleanup() {
        try {
            clearAllTestData();
            // Only close sessionFactory if it's not null and not already closed
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
                System.out.println("[TestDatabaseManager] SessionFactory closed successfully");
            }
        } catch (Exception e) {
            System.err.println("[TestDatabaseManager] Error during cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Execute a database operation in a transaction
     */
    public static <T> T executeInTransaction(DatabaseOperation<T> operation) {
        Session session = getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            T result = operation.execute(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Database operation failed", e);
        } finally {
            session.close();
        }
    }
    
    /**
     * Functional interface for database operations
     */
    @FunctionalInterface
    public static interface DatabaseOperation<T> {
        T execute(Session session);
    }

    public static void setupTestDatabase() {
        System.out.println("[TestDatabaseManager] setupTestDatabase called");
    }

    public static void clearUsers() {
        executeInTransaction(session -> {
            session.createQuery("DELETE FROM User").executeUpdate();
            return null;
        });
        System.out.println("[TestDatabaseManager] clearUsers: All users deleted");
    }

    public static void clearRestaurants() {
        executeInTransaction(session -> {
            session.createQuery("DELETE FROM Restaurant").executeUpdate();
            return null;
        });
        System.out.println("[TestDatabaseManager] clearRestaurants: All restaurants deleted");
    }

    public static void clearNotifications() {
        executeInTransaction(session -> {
            session.createQuery("DELETE FROM Notification").executeUpdate();
            return null;
        });
        System.out.println("[TestDatabaseManager] clearNotifications: All notifications deleted");
    }

    public static void clearNotificationData() {
        clearNotifications();
    }

    public static void cleanRatingData() {
        executeInTransaction(session -> {
            session.createQuery("DELETE FROM Rating").executeUpdate();
            return null;
        });
        System.out.println("[TestDatabaseManager] cleanRatingData: All ratings deleted");
    }
} 
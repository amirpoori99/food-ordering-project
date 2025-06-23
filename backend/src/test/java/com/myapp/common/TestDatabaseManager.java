package com.myapp.common;

import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing test database operations
 */
public class TestDatabaseManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseManager.class);
    
    /**
     * Cleans all rating data from the database
     */
    public static void cleanRatingData() {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Delete all ratings
            session.createQuery("DELETE FROM Rating").executeUpdate();
            
            transaction.commit();
            logger.debug("Cleaned all rating data from test database");
            
        } catch (Exception e) {
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
     * Cleans all test data from the database
     */
    public static void cleanAllTestData() {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Delete in order to respect foreign key constraints
            session.createQuery("DELETE FROM Rating").executeUpdate();
            session.createQuery("DELETE FROM OrderItem").executeUpdate();
            session.createQuery("DELETE FROM Order").executeUpdate();
            session.createQuery("DELETE FROM FoodItem").executeUpdate();
            session.createQuery("DELETE FROM Restaurant").executeUpdate();
            session.createQuery("DELETE FROM User").executeUpdate();
            
            transaction.commit();
            logger.debug("Cleaned all test data from database");
            
        } catch (Exception e) {
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
     * Gets the count of ratings in the database
     */
    public static long getRatingCount() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(r) FROM Rating r", Long.class)
                              .uniqueResult();
            return count != null ? count : 0L;
        } catch (Exception e) {
            logger.error("Error getting rating count: {}", e.getMessage(), e);
            return 0L;
        }
    }
} 
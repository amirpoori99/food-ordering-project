package com.myapp.common;

import com.myapp.common.utils.TestDatabaseHelper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

/**
 * Base class for all tests that provides:
 * - Unique database instance per test
 * - Automatic cleanup
 * - Common test utilities
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class BaseTestClass {
    
    protected SessionFactory sessionFactory;
    protected Session session;
    protected Transaction transaction;
    
    @BeforeEach
    void setUp() {
        // Create unique database for this test
        sessionFactory = TestDatabaseHelper.createTestSessionFactory();
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
        
        // Setup test data
        setupTestData();
    }
    
    @AfterEach
    void tearDown() {
        try {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        } catch (Exception e) {
            System.err.println("Error rolling back transaction: " + e.getMessage());
        }
        
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing session: " + e.getMessage());
        }
        
        try {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing session factory: " + e.getMessage());
        }
        
        // Clean up test database files
        TestDatabaseHelper.cleanupTestDatabases();
    }
    
    /**
     * Override this method in subclasses to setup test-specific data
     */
    protected void setupTestData() {
        // Default implementation - override in subclasses
    }
    
    /**
     * Helper method to flush and clear session
     */
    protected void flushAndClear() {
        if (session != null) {
            session.flush();
            session.clear();
        }
    }
    
    /**
     * Helper method to commit current transaction and start new one
     */
    protected void commitAndBeginNewTransaction() {
        if (transaction != null && transaction.isActive()) {
            transaction.commit();
        }
        transaction = session.beginTransaction();
    }
} 
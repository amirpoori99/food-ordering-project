package com.myapp.common.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.io.File;
import java.util.UUID;

/**
 * Helper class for managing test database connections
 * Creates unique database files for each test to prevent SQLite locking issues
 */
public class TestDatabaseHelper {
    
    private static final String TEST_DB_PREFIX = "test_db_";
    private static final String TEST_DB_SUFFIX = ".db";
    
    /**
     * Creates a unique database file name for testing
     */
    public static String createUniqueDatabaseName() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return TEST_DB_PREFIX + timestamp + "_" + uuid + TEST_DB_SUFFIX;
    }
    
    /**
     * Creates a new SessionFactory with a unique database file
     */
    public static SessionFactory createTestSessionFactory() {
        String dbFileName = createUniqueDatabaseName();
        String dbPath = "file:" + dbFileName;
        
        Configuration config = new Configuration();
        config.configure("hibernate-test.cfg.xml");
        
        // Override the database URL with unique file
        String uniqueUrl = "jdbc:sqlite:" + dbPath + "?cache=private&journal_mode=WAL&synchronous=NORMAL&busy_timeout=30000&shared_cache=false";
        config.setProperty("hibernate.connection.url", uniqueUrl);
        
        return config.buildSessionFactory();
    }
    
    /**
     * Cleans up test database files
     */
    public static void cleanupTestDatabases() {
        File currentDir = new File(".");
        File[] files = currentDir.listFiles((dir, name) -> name.startsWith(TEST_DB_PREFIX) && name.endsWith(TEST_DB_SUFFIX));
        
        if (files != null) {
            for (File file : files) {
                try {
                    if (file.delete()) {
                        System.out.println("Cleaned up test database: " + file.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to delete test database: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Gets the current test database name for logging purposes
     */
    public static String getCurrentTestDatabaseName() {
        return "test_db_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".db";
    }
} 
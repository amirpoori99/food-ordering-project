package com.myapp.common.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

/**
 * Helper class for generating unique test data
 * Prevents conflicts between concurrent tests
 */
public class TestDataHelper {
    
    private static final AtomicLong phoneCounter = new AtomicLong(1000000000L);
    private static final AtomicLong emailCounter = new AtomicLong(1L);
    private static final AtomicLong userIdCounter = new AtomicLong(1000L);
    
    /**
     * Generates a unique phone number for testing
     */
    public static String generateUniquePhone() {
        long counter = phoneCounter.incrementAndGet();
        // Ensure we have a 10-digit number starting with 09
        String phoneStr = String.valueOf(counter);
        if (phoneStr.length() > 10) {
            phoneStr = phoneStr.substring(phoneStr.length() - 10);
        } else if (phoneStr.length() < 10) {
            phoneStr = String.format("%010d", counter);
        }
        return "09" + phoneStr.substring(2); // Remove the first two digits and add 09
    }
    
    /**
     * Generates a unique email for testing
     */
    public static String generateUniqueEmail() {
        long counter = emailCounter.incrementAndGet();
        return "test" + counter + "@example.com";
    }
    
    /**
     * Generates a unique user ID for testing
     */
    public static Long generateUniqueUserId() {
        return userIdCounter.incrementAndGet();
    }
    
    /**
     * Generates a unique name for testing
     */
    public static String generateUniqueName() {
        return "Test User " + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generates a unique address for testing
     */
    public static String generateUniqueAddress() {
        return "Test Address " + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generates a unique restaurant name for testing
     */
    public static String generateUniqueRestaurantName() {
        return "Test Restaurant " + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generates a unique item name for testing
     */
    public static String generateUniqueItemName() {
        return "Test Item " + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Resets all counters (useful for test cleanup)
     */
    public static void resetCounters() {
        phoneCounter.set(1000000000L);
        emailCounter.set(1L);
        userIdCounter.set(1000L);
    }
    
    /**
     * Generates a unique phone number with a specific prefix
     */
    public static String generateUniquePhoneWithPrefix(String prefix) {
        long counter = phoneCounter.incrementAndGet();
        String phoneStr = String.valueOf(counter);
        if (phoneStr.length() > 10) {
            phoneStr = phoneStr.substring(phoneStr.length() - 10);
        } else if (phoneStr.length() < 10) {
            phoneStr = String.format("%010d", counter);
        }
        return prefix + phoneStr.substring(prefix.length());
    }
    
    /**
     * Generates a unique email with a specific domain
     */
    public static String generateUniqueEmailWithDomain(String domain) {
        long counter = emailCounter.incrementAndGet();
        return "test" + counter + "@" + domain;
    }
} 
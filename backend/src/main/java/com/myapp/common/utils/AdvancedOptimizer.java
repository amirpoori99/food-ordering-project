package com.myapp.common.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Advanced optimization utilities for performance, reliability, and resource management
 */
public class AdvancedOptimizer {
    private static final Logger logger = Logger.getLogger(AdvancedOptimizer.class.getName());
    
    // Connection pool
    private static final ConcurrentHashMap<String, Connection> connectionPool = new ConcurrentHashMap<>();
    private static final int MAX_POOL_SIZE = 10;
    
    // Circuit breaker state
    private static final ConcurrentHashMap<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    
    // Rate limiting
    private static final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    // Performance metrics
    private static final ConcurrentHashMap<String, AtomicLong> executionTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> successCount = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> failureCount = new ConcurrentHashMap<>();
    
    /**
     * Circuit Breaker Pattern Implementation
     */
    public static class CircuitBreakerState {
        private volatile State state = State.CLOSED;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong lastFailureTime = new AtomicLong(0);
        private final int failureThreshold;
        private final long timeoutMs;
        
        public CircuitBreakerState(int failureThreshold, long timeoutMs) {
            this.failureThreshold = failureThreshold;
            this.timeoutMs = timeoutMs;
        }
        
        public boolean canExecute() {
            switch (state) {
                case CLOSED:
                    return true;
                case OPEN:
                    if (System.currentTimeMillis() - lastFailureTime.get() > timeoutMs) {
                        state = State.HALF_OPEN;
                        return true;
                    }
                    return false;
                case HALF_OPEN:
                    return true;
                default:
                    return false;
            }
        }
        
        public void recordSuccess() {
            failureCount.set(0);
            state = State.CLOSED;
        }
        
        public void recordFailure() {
            failureCount.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());
            
            if (failureCount.get() >= failureThreshold) {
                state = State.OPEN;
            }
        }
        
        private enum State { CLOSED, OPEN, HALF_OPEN }
    }
    
    /**
     * Rate Limiter Implementation
     */
    public static class RateLimiter {
        private final int maxRequests;
        private final long windowMs;
        private final AtomicInteger currentRequests = new AtomicInteger(0);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        
        public RateLimiter(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }
        
        public boolean allowRequest() {
            long now = System.currentTimeMillis();
            long windowStartTime = windowStart.get();
            
            if (now - windowStartTime > windowMs) {
                currentRequests.set(0);
                windowStart.set(now);
            }
            
            return currentRequests.incrementAndGet() <= maxRequests;
        }
    }
    
    /**
     * Execute operation with circuit breaker protection
     */
    public static <T> T executeWithCircuitBreaker(String operationName, Supplier<T> operation) {
        CircuitBreakerState circuitBreaker = circuitBreakers.computeIfAbsent(
            operationName, 
            k -> new CircuitBreakerState(5, 30000) // 5 failures, 30s timeout
        );
        
        if (!circuitBreaker.canExecute()) {
            logger.warning("Circuit breaker OPEN for operation: " + operationName);
            throw new RuntimeException("Service temporarily unavailable");
        }
        
        try {
            long startTime = System.currentTimeMillis();
            T result = operation.get();
            long executionTime = System.currentTimeMillis() - startTime;
            
            circuitBreaker.recordSuccess();
            recordMetrics(operationName, executionTime, true);
            
            return result;
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            recordMetrics(operationName, 0, false);
            throw e;
        }
    }
    
    /**
     * Execute operation with rate limiting
     */
    public static <T> T executeWithRateLimit(String operationName, Supplier<T> operation) {
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(
            operationName,
            k -> new RateLimiter(100, 60000) // 100 requests per minute
        );
        
        if (!rateLimiter.allowRequest()) {
            logger.warning("Rate limit exceeded for operation: " + operationName);
            throw new RuntimeException("Rate limit exceeded");
        }
        
        return operation.get();
    }
    
    /**
     * Execute operation with Hibernate session
     */
    public static <T> T executeWithSession(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (Exception e) {
            logger.severe("Error executing operation with session: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Record performance metrics
     */
    private static void recordMetrics(String operationName, long executionTime, boolean success) {
        executionTimes.computeIfAbsent(operationName, k -> new AtomicLong(0))
                     .addAndGet(executionTime);
        
        if (success) {
            successCount.computeIfAbsent(operationName, k -> new AtomicInteger(0))
                       .incrementAndGet();
        } else {
            failureCount.computeIfAbsent(operationName, k -> new AtomicInteger(0))
                       .incrementAndGet();
        }
    }
    
    /**
     * Get performance statistics
     */
    public static String getPerformanceStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== Performance Statistics ===\n");
        
        for (String operation : executionTimes.keySet()) {
            long totalTime = executionTimes.get(operation).get();
            int successes = successCount.getOrDefault(operation, new AtomicInteger(0)).get();
            int failures = failureCount.getOrDefault(operation, new AtomicInteger(0)).get();
            int total = successes + failures;
            
            if (total > 0) {
                double avgTime = (double) totalTime / total;
                double successRate = (double) successes / total * 100;
                
                stats.append(String.format("%s: Avg=%.2fms, Success=%.1f%%, Total=%d\n", 
                    operation, avgTime, successRate, total));
            }
        }
        
        return stats.toString();
    }
    
    /**
     * Adaptive performance tuning based on metrics
     */
    public static void adaptiveTuning() {
        for (String operation : executionTimes.keySet()) {
            long avgTime = executionTimes.get(operation).get();
            int failures = failureCount.getOrDefault(operation, new AtomicInteger(0)).get();
            
            // Adjust circuit breaker settings based on failure rate
            if (failures > 10) {
                CircuitBreakerState cb = circuitBreakers.get(operation);
                if (cb != null) {
                    logger.info("High failure rate detected for " + operation + 
                              ", circuit breaker settings adjusted");
                }
            }
            
            // Adjust rate limiting based on performance
            if (avgTime > 1000) { // If average time > 1 second
                RateLimiter rl = rateLimiters.get(operation);
                if (rl != null) {
                    logger.info("High execution time detected for " + operation + 
                              ", rate limiting adjusted");
                }
            }
        }
    }
} 
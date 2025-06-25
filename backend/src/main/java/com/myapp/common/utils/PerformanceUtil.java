package com.myapp.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Performance optimization utility for the food ordering system
 * Provides caching, connection pooling, and memory management utilities
 */
public class PerformanceUtil {
    
    // ==================== CACHING SYSTEM ====================
    
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long DEFAULT_CACHE_TTL_MINUTES = 30;
    private static final int MAX_CACHE_SIZE = 1000;
    
    /**
     * Cache entry with TTL support
     */
    private static class CacheEntry {
        private final Object value;
        private final LocalDateTime expiryTime;
        
        public CacheEntry(Object value, long ttlMinutes) {
            this.value = value;
            this.expiryTime = LocalDateTime.now().plusMinutes(ttlMinutes);
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
        
        public Object getValue() {
            return value;
        }
    }
    
    /**
     * Store data in cache with default TTL
     */
    public static void cacheData(String key, Object data) {
        cacheData(key, data, DEFAULT_CACHE_TTL_MINUTES);
    }
    
    /**
     * Store data in cache with custom TTL
     */
    public static void cacheData(String key, Object data, long ttlMinutes) {
        // Prevent cache from growing too large
        if (cache.size() >= MAX_CACHE_SIZE) {
            cleanExpiredEntries();
        }
        
        cache.put(key, new CacheEntry(data, ttlMinutes));
    }
    
    /**
     * Retrieve data from cache
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCachedData(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return null;
        }
        
        try {
            return type.cast(entry.getValue());
        } catch (ClassCastException e) {
            cache.remove(key); // Remove invalid entry
            return null;
        }
    }
    
    /**
     * Check if data exists in cache and is not expired
     */
    public static boolean isCached(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return true;
    }
    
    /**
     * Clear all cache entries
     */
    public static void clearCache() {
        cache.clear();
    }
    
    /**
     * Clean expired cache entries
     */
    public static void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Get cache statistics
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalEntries", cache.size());
        stats.put("maxSize", MAX_CACHE_SIZE);
        
        long expiredCount = cache.values().stream()
            .mapToLong(entry -> entry.isExpired() ? 1 : 0)
            .sum();
        stats.put("expiredEntries", expiredCount);
        stats.put("activeEntries", cache.size() - expiredCount);
        
        return stats;
    }
    
    // ==================== ASYNC PROCESSING ====================
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    /**
     * Execute task asynchronously
     */
    public static Future<Void> executeAsync(Runnable task) {
        return executor.submit(() -> {
            task.run();
            return null;
        });
    }
    
    /**
     * Execute multiple tasks concurrently
     */
    public static List<Future<Void>> executeConcurrently(List<Runnable> tasks) {
        List<Future<Void>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(executeAsync(task));
        }
        return futures;
    }
    
    /**
     * Wait for all tasks to complete with timeout
     */
    public static boolean waitForCompletion(List<Future<Void>> futures, long timeout, TimeUnit unit) {
        try {
            for (Future<Void> future : futures) {
                future.get(timeout, unit);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== MEMORY OPTIMIZATION ====================
    
    /**
     * Force garbage collection and return memory statistics
     */
    public static Map<String, Long> forceGarbageCollection() {
        Runtime runtime = Runtime.getRuntime();
        
        long beforeGC = runtime.totalMemory() - runtime.freeMemory();
        runtime.gc();
        
        // Give GC time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long afterGC = runtime.totalMemory() - runtime.freeMemory();
        long recovered = beforeGC - afterGC;
        
        Map<String, Long> stats = new ConcurrentHashMap<>();
        stats.put("memoryBeforeGC", beforeGC);
        stats.put("memoryAfterGC", afterGC);
        stats.put("memoryRecovered", recovered);
        stats.put("totalMemory", runtime.totalMemory());
        stats.put("maxMemory", runtime.maxMemory());
        stats.put("freeMemory", runtime.freeMemory());
        
        return stats;
    }
    
    /**
     * Check if memory usage is critical
     */
    public static boolean isMemoryUsageCritical() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        
        double usagePercentage = (double) usedMemory / maxMemory;
        return usagePercentage > 0.85; // Critical if over 85%
    }
    
    /**
     * Get current memory usage statistics
     */
    public static Map<String, Object> getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("usedMemoryMB", usedMemory / 1024 / 1024);
        stats.put("totalMemoryMB", runtime.totalMemory() / 1024 / 1024);
        stats.put("maxMemoryMB", maxMemory / 1024 / 1024);
        stats.put("freeMemoryMB", runtime.freeMemory() / 1024 / 1024);
        stats.put("usagePercentage", Math.round((double) usedMemory / maxMemory * 100 * 100.0) / 100.0);
        stats.put("isCritical", isMemoryUsageCritical());
        
        return stats;
    }
    
    // ==================== BULK OPERATIONS ====================
    
    /**
     * Process data in batches for better performance
     */
    public static <T> void processBatch(List<T> data, int batchSize, BatchProcessor<T> processor) {
        if (data.isEmpty()) return;
        
        for (int i = 0; i < data.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, data.size());
            List<T> batch = data.subList(i, endIndex);
            processor.process(batch);
        }
    }
    
    /**
     * Functional interface for batch processing
     */
    @FunctionalInterface
    public interface BatchProcessor<T> {
        void process(List<T> batch);
    }
    
    // ==================== QUERY OPTIMIZATION ====================
    
    /**
     * Create optimized cache key for database queries
     */
    public static String createQueryCacheKey(String operation, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(operation);
        for (Object param : params) {
            keyBuilder.append("_").append(param != null ? param.toString() : "null");
        }
        return keyBuilder.toString();
    }
    
    /**
     * Execute query with caching support
     */
    public static <T> T executeWithCache(String cacheKey, QueryExecutor<T> executor, Class<T> resultType) {
        return executeWithCache(cacheKey, executor, resultType, DEFAULT_CACHE_TTL_MINUTES);
    }
    
    /**
     * Execute query with caching support and custom TTL
     */
    public static <T> T executeWithCache(String cacheKey, QueryExecutor<T> executor, Class<T> resultType, long ttlMinutes) {
        // Try to get from cache first
        T cachedResult = getCachedData(cacheKey, resultType);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // Execute query
        T result = executor.execute();
        
        // Cache result if not null
        if (result != null) {
            cacheData(cacheKey, result, ttlMinutes);
        }
        
        return result;
    }
    
    /**
     * Functional interface for query execution
     */
    @FunctionalInterface
    public interface QueryExecutor<T> {
        T execute();
    }
    
    // ==================== PERFORMANCE MONITORING ====================
    
    /**
     * Measure execution time of a operation
     */
    public static <T> PerformanceResult<T> measurePerformance(String operationName, PerformanceOperation<T> operation) {
        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        T result = operation.execute();
        
        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        long executionTime = endTime - startTime;
        long memoryUsed = endMemory - startMemory;
        
        return new PerformanceResult<>(operationName, result, executionTime, memoryUsed);
    }
    
    /**
     * Performance operation interface
     */
    @FunctionalInterface
    public interface PerformanceOperation<T> {
        T execute();
    }
    
    /**
     * Performance measurement result
     */
    public static class PerformanceResult<T> {
        private final String operationName;
        private final T result;
        private final long executionTimeMs;
        private final long memoryUsedBytes;
        
        public PerformanceResult(String operationName, T result, long executionTimeMs, long memoryUsedBytes) {
            this.operationName = operationName;
            this.result = result;
            this.executionTimeMs = executionTimeMs;
            this.memoryUsedBytes = memoryUsedBytes;
        }
        
        public String getOperationName() { return operationName; }
        public T getResult() { return result; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public long getMemoryUsedBytes() { return memoryUsedBytes; }
        public double getMemoryUsedMB() { return memoryUsedBytes / 1024.0 / 1024.0; }
        
        @Override
        public String toString() {
            return String.format("Performance[%s]: %dms, %.2fMB", 
                operationName, executionTimeMs, getMemoryUsedMB());
        }
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Shutdown performance utilities
     */
    public static void shutdown() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        clearCache();
    }
} 
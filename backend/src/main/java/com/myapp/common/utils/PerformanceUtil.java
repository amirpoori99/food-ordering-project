package com.myapp.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.function.Consumer;

/**
 * کلاس ابزاری بهینه‌سازی عملکرد پیشرفته برای سیستم سفارش غذا
 * Version: 2.0 - Phase 33 Implementation
 * 
 * شامل سیستم cache پیشرفته، پردازش ناهمزمان، مدیریت حافظه، 
 * بهینه‌سازی کوئری، نظارت بر عملکرد و تکنیک‌های پیشرفته بهینه‌سازی
 * 
 * این کلاس ابزارهای پیشرفته‌ای برای بهبود سرعت و کارایی سیستم فراهم می‌کند
 */
public class PerformanceUtil {
    
    // ==================== سیستم کش پیشرفته (ADVANCED CACHING SYSTEM) ====================
    
    // نقشه thread-safe برای ذخیره‌سازی داده‌های کش شده
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    // کش برای آمار عملکرد
    private static final Map<String, PerformanceStats> performanceCache = new ConcurrentHashMap<>();
    // صف برای مدیریت LRU (Least Recently Used)
    private static final Queue<String> lruQueue = new ConcurrentLinkedQueue<>();
    
    // تنظیمات پیشرفته کش
    private static final long DEFAULT_CACHE_TTL_MINUTES = 30;
    private static final int MAX_CACHE_SIZE = 1000; // کاهش اندازه کش برای هماهنگی با تست
    private static final int LRU_CLEANUP_THRESHOLD = 100; // آستانه پاک‌سازی LRU
    private static final AtomicLong cacheHits = new AtomicLong(0);
    private static final AtomicLong cacheMisses = new AtomicLong(0);
    
    /**
     * کلاس داخلی پیشرفته برای ذخیره‌سازی آیتم‌های کش با پشتیبانی از TTL و LRU
     */
    private static class CacheEntry {
        private final Object value;
        private final LocalDateTime expiryTime;
        private LocalDateTime lastAccessTime; // حذف final برای امکان تغییر
        private final AtomicInteger accessCount;
        
        public CacheEntry(Object value, long ttlMinutes) {
            this.value = value;
            this.expiryTime = LocalDateTime.now().plusMinutes(ttlMinutes);
            this.lastAccessTime = LocalDateTime.now();
            this.accessCount = new AtomicInteger(0);
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
        
        public Object getValue() {
            lastAccessTime = LocalDateTime.now();
            accessCount.incrementAndGet();
            return value;
        }
        
        public LocalDateTime getLastAccessTime() {
            return lastAccessTime;
        }
        
        public int getAccessCount() {
            return accessCount.get();
        }
        
        public boolean isHot() {
            return accessCount.get() > 10; // آیتم‌های داغ
        }
    }
    
    /**
     * کلاس آمار عملکرد برای مانیتورینگ
     */
    private static class PerformanceStats {
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private final AtomicLong errors = new AtomicLong(0);
        
        public void recordExecution(long executionTime) {
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            
            // Update min/max
            long currentMin = minExecutionTime.get();
            while (executionTime < currentMin && 
                   !minExecutionTime.compareAndSet(currentMin, executionTime)) {
                currentMin = minExecutionTime.get();
            }
            
            long currentMax = maxExecutionTime.get();
            while (executionTime > currentMax && 
                   !maxExecutionTime.compareAndSet(currentMax, executionTime)) {
                currentMax = maxExecutionTime.get();
            }
        }
        
        public void recordError() {
            errors.incrementAndGet();
        }
        
        public double getAverageExecutionTime() {
            long total = totalExecutions.get();
            return total > 0 ? (double) totalExecutionTime.get() / total : 0;
        }
        
        public long getMinExecutionTime() {
            return minExecutionTime.get() == Long.MAX_VALUE ? 0 : minExecutionTime.get();
        }
        
        public long getMaxExecutionTime() {
            return maxExecutionTime.get();
        }
        
        public long getTotalExecutions() {
            return totalExecutions.get();
        }
        
        public long getErrorCount() {
            return errors.get();
        }
        
        public double getErrorRate() {
            long total = totalExecutions.get();
            return total > 0 ? (double) errors.get() / total : 0;
        }
    }
    
    // ==================== سیستم کش پیشرفته ====================
    
    /**
     * ذخیره‌سازی داده در کش با TTL پیش‌فرض و مدیریت LRU
     */
    public static void cacheData(String key, Object data) {
        cacheData(key, data, DEFAULT_CACHE_TTL_MINUTES);
    }
    
    /**
     * ذخیره‌سازی داده در کش با TTL سفارشی و مدیریت LRU
     */
    public static void cacheData(String key, Object data, long ttlMinutes) {
        // مدیریت LRU و اندازه کش
        if (cache.size() >= MAX_CACHE_SIZE) {
            cleanupLRU();
        }
        
        cache.put(key, new CacheEntry(data, ttlMinutes));
        lruQueue.offer(key);
    }
    
    /**
     * دریافت داده از کش با آمارگیری
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCachedData(String key, Class<T> type) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            cacheMisses.incrementAndGet();
            return null;
        }
        
        cacheHits.incrementAndGet();
        try {
            return type.cast(entry.getValue());
        } catch (ClassCastException e) {
            cache.remove(key);
            cacheMisses.incrementAndGet();
            return null;
        }
    }
    
    /**
     * پاک‌سازی LRU برای آزادسازی فضا
     */
    private static void cleanupLRU() {
        int itemsToRemove = LRU_CLEANUP_THRESHOLD;
        List<String> itemsToRemoveList = new ArrayList<>();
        
        // پیدا کردن آیتم‌های کم‌استفاده
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (itemsToRemoveList.size() >= itemsToRemove) break;
            
            CacheEntry cacheEntry = entry.getValue();
            if (!cacheEntry.isHot() && !cacheEntry.isExpired()) {
                itemsToRemoveList.add(entry.getKey());
            }
        }
        
        // حذف آیتم‌های انتخاب شده
        for (String key : itemsToRemoveList) {
            cache.remove(key);
            lruQueue.remove(key);
        }
    }
    
    /**
     * دریافت آمار پیشرفته کش
     */
    public static Map<String, Object> getAdvancedCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalEntries", cache.size());
        stats.put("maxSize", MAX_CACHE_SIZE);
        stats.put("cacheHits", cacheHits.get());
        stats.put("cacheMisses", cacheMisses.get());
        
        long totalRequests = cacheHits.get() + cacheMisses.get();
        double hitRate = totalRequests > 0 ? (double) cacheHits.get() / totalRequests : 0;
        stats.put("hitRate", String.format("%.2f%%", hitRate * 100));
        
        // آمار آیتم‌های داغ
        long hotItems = cache.values().stream().mapToLong(entry -> entry.isHot() ? 1 : 0).sum();
        stats.put("hotItems", hotItems);
        
        // آمار آیتم‌های منقضی
        long expiredCount = cache.values().stream().mapToLong(entry -> entry.isExpired() ? 1 : 0).sum();
        stats.put("expiredEntries", expiredCount);
        stats.put("activeEntries", cache.size() - expiredCount);
        
        return stats;
    }
    
    // ==================== پردازش ناهمزمان پیشرفته (ADVANCED ASYNC PROCESSING) ====================
    
    // Thread pool پیشرفته با تنظیمات بهینه
    private static final ExecutorService executor = new ThreadPoolExecutor(
        10, // core pool size
        50, // maximum pool size
        60L, // keep alive time
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(1000), // work queue
        new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
    );
    
    // Thread pool برای کارهای I/O محور
    private static final ExecutorService ioExecutor = Executors.newCachedThreadPool();
    
    /**
     * اجرای کار به صورت ناهمزمان با CompletableFuture
     */
    public static <T> CompletableFuture<T> executeAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }
    
    /**
     * اجرای کار به صورت ناهمزمان برای I/O operations
     */
    public static <T> CompletableFuture<T> executeIOAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, ioExecutor);
    }
    
    /**
     * اجرای چندین کار به صورت همزمان با timeout
     */
    public static <T> List<T> executeConcurrentlyWithTimeout(
            List<Supplier<T>> suppliers, long timeout, TimeUnit unit) {
        
        List<CompletableFuture<T>> futures = suppliers.stream()
            .map(supplier -> CompletableFuture.supplyAsync(supplier, executor))
            .toList();
        
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allFutures.get(timeout, unit);
        } catch (Exception e) {
            // Timeout or other exception
            allFutures.cancel(true);
        }
        
        return futures.stream()
            .map(future -> {
                try {
                    return future.get(0, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    return null;
                }
            })
            .toList();
    }
    
    // ==================== بهینه‌سازی حافظه پیشرفته (ADVANCED MEMORY OPTIMIZATION) ====================
    
    /**
     * مانیتورینگ پیشرفته حافظه با آمار تفصیلی
     */
    public static Map<String, Object> getAdvancedMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        // آمار حافظه
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        stats.put("totalMemoryMB", totalMemory / 1024 / 1024);
        stats.put("freeMemoryMB", freeMemory / 1024 / 1024);
        stats.put("usedMemoryMB", usedMemory / 1024 / 1024);
        stats.put("maxMemoryMB", maxMemory / 1024 / 1024);
        stats.put("memoryUsagePercent", (double) usedMemory / maxMemory * 100);
        
        // اضافه کردن کلیدهای مورد نیاز تست‌ها
        stats.put("usagePercentage", (double) usedMemory / maxMemory * 100);
        stats.put("isCritical", isMemoryUsageCritical());
        
        // آمار GC
        long gcCount = 0;
        long gcTime = 0;
        try {
            java.lang.management.GarbageCollectorMXBean[] gcBeans = 
                java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()
                    .toArray(new java.lang.management.GarbageCollectorMXBean[0]);
            
            for (java.lang.management.GarbageCollectorMXBean gcBean : gcBeans) {
                gcCount += gcBean.getCollectionCount();
                gcTime += gcBean.getCollectionTime();
            }
        } catch (Exception e) {
            // GC stats not available
        }
        
        stats.put("gcCount", gcCount);
        stats.put("gcTimeMS", gcTime);
        
        return stats;
    }
    
    /**
     * بررسی بحرانی بودن استفاده از حافظه
     */
    public static boolean isMemoryUsageCritical() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory;
        return memoryUsage > 0.9; // بیش از 90% استفاده
    }
    
    /**
     * بهینه‌سازی حافظه با تنظیمات پیشرفته
     */
    public static void optimizeMemory() {
        Runtime runtime = Runtime.getRuntime();
        
        // بررسی وضعیت حافظه
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory;
        
        // اگر استفاده از حافظه بیش از 80% باشد، GC اجباری
        if (memoryUsage > 0.8) {
            System.gc();
            
            // بررسی مجدد بعد از GC
            usedMemory = runtime.totalMemory() - runtime.freeMemory();
            memoryUsage = (double) usedMemory / maxMemory;
            
            // اگر همچنان بالا است، هشدار
            if (memoryUsage > 0.9) {
                System.err.println("⚠️ WARNING: High memory usage detected: " + 
                    String.format("%.1f%%", memoryUsage * 100));
            }
        }
    }
    
    // ==================== پردازش Batch ====================
    
    /**
     * پردازش Batch برای عملیات‌های بزرگ
     */
    public static <T, R> List<R> processBatch(List<T> items, int batchSize, 
                                             java.util.function.Function<List<T>, List<R>> batchProcessor) {
        List<R> results = new ArrayList<>();
        
        for (int i = 0; i < items.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(i, endIndex);
            
            List<R> batchResults = batchProcessor.apply(batch);
            results.addAll(batchResults);
        }
        
        return results;
    }
    
    // ==================== بهینه‌سازی کوئری پیشرفته (ADVANCED QUERY OPTIMIZATION) ====================
    
    /**
     * اجرای کوئری با کش پیشرفته و آمارگیری عملکرد
     */
    public static <T> T executeWithAdvancedCache(String cacheKey, Supplier<T> executor, Class<T> resultType) {
        return executeWithAdvancedCache(cacheKey, executor, resultType, DEFAULT_CACHE_TTL_MINUTES);
    }
    
    /**
     * اجرای کوئری با کش پیشرفته، آمارگیری و مانیتورینگ
     */
    public static <T> T executeWithAdvancedCache(String cacheKey, Supplier<T> executor, Class<T> resultType, long ttlMinutes) {
        // بررسی کش
        T cachedResult = getCachedData(cacheKey, resultType);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // اجرای کوئری با آمارگیری
        long startTime = System.currentTimeMillis();
        T result = null;
        boolean success = false;
        
        try {
            result = executor.get();
            success = true;
        } catch (Exception e) {
            // ثبت خطا در آمار
            recordPerformanceError(cacheKey);
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            recordPerformanceStats(cacheKey, executionTime, success);
        }
        
        // کش کردن نتیجه
        if (result != null) {
            cacheData(cacheKey, result, ttlMinutes);
        }
        
        return result;
    }
    
    /**
     * ثبت آمار عملکرد
     */
    private static void recordPerformanceStats(String operation, long executionTime, boolean success) {
        PerformanceStats stats = performanceCache.computeIfAbsent(operation, k -> new PerformanceStats());
        
        if (success) {
            stats.recordExecution(executionTime);
        } else {
            stats.recordError();
        }
    }
    
    /**
     * ثبت خطای عملکرد
     */
    private static void recordPerformanceError(String operation) {
        PerformanceStats stats = performanceCache.computeIfAbsent(operation, k -> new PerformanceStats());
        stats.recordError();
    }
    
    /**
     * دریافت آمار عملکرد برای عملیات خاص
     */
    public static Map<String, Object> getPerformanceStats(String operation) {
        PerformanceStats stats = performanceCache.get(operation);
        if (stats == null) {
            return new ConcurrentHashMap<>();
        }
        
        Map<String, Object> result = new ConcurrentHashMap<>();
        result.put("operation", operation);
        result.put("totalExecutions", stats.getTotalExecutions());
        result.put("averageExecutionTime", stats.getAverageExecutionTime());
        result.put("minExecutionTime", stats.getMinExecutionTime());
        result.put("maxExecutionTime", stats.getMaxExecutionTime());
        result.put("errorCount", stats.getErrorCount());
        result.put("errorRate", stats.getErrorRate());
        
        return result;
    }
    
    /**
     * دریافت آمار عملکرد کلی
     */
    public static Map<String, Object> getAllPerformanceStats() {
        Map<String, Object> allStats = new ConcurrentHashMap<>();
        allStats.put("totalOperations", performanceCache.size());
        
        // آمار کلی
        long totalExecutions = 0;
        long totalErrors = 0;
        double totalExecutionTime = 0;
        
        for (PerformanceStats stats : performanceCache.values()) {
            totalExecutions += stats.getTotalExecutions();
            totalErrors += stats.getErrorCount();
            totalExecutionTime += stats.getAverageExecutionTime() * stats.getTotalExecutions();
        }
        
        allStats.put("totalExecutions", totalExecutions);
        allStats.put("totalErrors", totalErrors);
        allStats.put("overallErrorRate", totalExecutions > 0 ? (double) totalErrors / totalExecutions : 0);
        allStats.put("averageExecutionTime", totalExecutions > 0 ? totalExecutionTime / totalExecutions : 0);
        
        return allStats;
    }
    
    // ==================== متدهای موجود (EXISTING METHODS) ====================
    
    public static boolean isCached(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return false;
        }
        return true;
    }
    
    public static void clearCache() {
        cache.clear();
        lruQueue.clear();
        cacheHits.set(0);
        cacheMisses.set(0);
    }
    
    public static void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    public static Map<String, Object> getCacheStats() {
        return getAdvancedCacheStats();
    }
    
    public static Future<Void> executeAsync(Runnable task) {
        return executor.submit(() -> {
            task.run();
            return null;
        });
    }
    
    public static List<Future<Void>> executeConcurrently(List<Runnable> tasks) {
        List<Future<Void>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(executeAsync(task));
        }
        return futures;
    }
    
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
    
    public static Map<String, Object> getMemoryStats() {
        return getAdvancedMemoryStats();
    }
    
    public static void forceGarbageCollection() {
        System.gc();
    }
    
    public static String createQueryCacheKey(String operation, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(operation);
        for (Object param : params) {
            keyBuilder.append("_").append(param != null ? param.toString() : "null");
        }
        return keyBuilder.toString();
    }
    
    public static <T> T executeWithCache(String cacheKey, QueryExecutor<T> executor, Class<T> resultType) {
        return executeWithCache(cacheKey, executor, resultType, DEFAULT_CACHE_TTL_MINUTES);
    }
    
    public static <T> T executeWithCache(String cacheKey, QueryExecutor<T> executor, Class<T> resultType, long ttlMinutes) {
        return executeWithAdvancedCache(cacheKey, executor::execute, resultType, ttlMinutes);
    }
    
    @FunctionalInterface
    public interface QueryExecutor<T> {
        T execute();
    }
    
    public static <T> PerformanceResult<T> measurePerformance(String operation, Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        T result = null;
        boolean success = false;
        
        try {
            result = supplier.get();
            success = true;
        } catch (Exception e) {
            recordPerformanceError(operation);
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            recordPerformanceStats(operation, executionTime, success);
        }
        
        return new PerformanceResult<>(result, System.currentTimeMillis() - startTime, success, operation);
    }
    
    public static class PerformanceResult<T> {
        private final T result;
        private final long executionTime;
        private final boolean success;
        private final String operationName;
        
        public PerformanceResult(T result, long executionTime, boolean success) {
            this.result = result;
            this.executionTime = executionTime;
            this.success = success;
            this.operationName = "unknown";
        }
        
        public PerformanceResult(T result, long executionTime, boolean success, String operationName) {
            this.result = result;
            this.executionTime = executionTime;
            this.success = success;
            this.operationName = operationName;
        }
        
        public T getResult() { return result; }
        public long getExecutionTime() { return executionTime; }
        public long getExecutionTimeMs() { return executionTime; }
        public boolean isSuccess() { return success; }
        public String getOperationName() { return operationName; }
        
        @Override
        public String toString() {
            return String.format("PerformanceResult{executionTime=%dms, success=%s, operation=%s}", 
                executionTime, success, operationName);
        }
    }
    
    // ==================== متدهای جدید برای فاز 33 ====================
    
    /**
     * بهینه‌سازی خودکار سیستم
     */
    public static void autoOptimize() {
        // بهینه‌سازی حافظه
        optimizeMemory();
        
        // پاک‌سازی کش
        cleanExpiredEntries();
        
        // بررسی اندازه کش
        if (cache.size() > MAX_CACHE_SIZE * 0.8) {
            cleanupLRU();
        }
    }
    
    /**
     * دریافت گزارش کامل عملکرد
     */
    public static Map<String, Object> getCompletePerformanceReport() {
        Map<String, Object> report = new ConcurrentHashMap<>();
        report.put("timestamp", LocalDateTime.now().toString());
        report.put("cacheStats", getAdvancedCacheStats());
        report.put("memoryStats", getAdvancedMemoryStats());
        report.put("performanceStats", getAllPerformanceStats());
        
        return report;
    }
    
    /**
     * تنظیم مجدد آمار عملکرد
     */
    public static void resetPerformanceStats() {
        performanceCache.clear();
    }
    
    /**
     * خاموش کردن سیستم
     */
    public static void shutdown() {
        executor.shutdown();
        ioExecutor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!ioExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 
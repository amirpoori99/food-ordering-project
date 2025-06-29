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
 * کلاس ابزاری بهینه‌سازی عملکرد برای سیستم سفارش غذا
 * شامل سیستم cache، پردازش ناهمزمان، مدیریت حافظه و نظارت بر عملکرد
 * این کلاس ابزارهای پیشرفته‌ای برای بهبود سرعت و کارایی سیستم فراهم می‌کند
 */
public class PerformanceUtil {
    
    // ==================== سیستم کش (CACHING SYSTEM) ====================
    
    // نقشه thread-safe برای ذخیره‌سازی داده‌های کش شده
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    // مدت زمان پیش‌فرض انقضای کش (30 دقیقه)
    private static final long DEFAULT_CACHE_TTL_MINUTES = 30;
    // حداکثر تعداد آیتم‌های قابل ذخیره در کش
    private static final int MAX_CACHE_SIZE = 1000;
    
    /**
     * کلاس داخلی برای ذخیره‌سازی آیتم‌های کش با پشتیبانی از TTL
     * هر آیتم شامل مقدار و زمان انقضا می‌باشد
     */
    private static class CacheEntry {
        private final Object value;         // مقدار ذخیره شده
        private final LocalDateTime expiryTime;  // زمان انقضا
        
        /**
         * سازنده آیتم کش
         * 
         * @param value مقدار برای ذخیره‌سازی
         * @param ttlMinutes مدت زمان زنده بودن به دقیقه
         */
        public CacheEntry(Object value, long ttlMinutes) {
            this.value = value;
            this.expiryTime = LocalDateTime.now().plusMinutes(ttlMinutes);
        }
        
        /**
         * بررسی انقضای آیتم کش
         * 
         * @return true اگر آیتم منقضی شده باشد
         */
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
        
        /**
         * دریافت مقدار ذخیره شده
         * 
         * @return مقدار آیتم کش
         */
        public Object getValue() {
            return value;
        }
    }
    
    /**
     * ذخیره‌سازی داده در کش با TTL پیش‌فرض
     * 
     * @param key کلید منحصر به فرد برای داده
     * @param data داده برای ذخیره‌سازی
     */
    public static void cacheData(String key, Object data) {
        cacheData(key, data, DEFAULT_CACHE_TTL_MINUTES);
    }
    
    /**
     * ذخیره‌سازی داده در کش با TTL سفارشی
     * 
     * @param key کلید منحصر به فرد برای داده
     * @param data داده برای ذخیره‌سازی
     * @param ttlMinutes مدت زمان زنده بودن به دقیقه
     */
    public static void cacheData(String key, Object data, long ttlMinutes) {
        // جلوگیری از رشد بیش از حد کش
        if (cache.size() >= MAX_CACHE_SIZE) {
            cleanExpiredEntries();
        }
        
        cache.put(key, new CacheEntry(data, ttlMinutes));
    }
    
    /**
     * دریافت داده از کش
     * 
     * @param key کلید داده
     * @param type نوع کلاس داده
     * @param <T> نوع generic داده
     * @return داده کش شده یا null در صورت عدم وجود یا انقضا
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
            cache.remove(key); // حذف آیتم نامعتبر
            return null;
        }
    }
    
    /**
     * بررسی وجود داده در کش و معتبر بودن آن
     * 
     * @param key کلید داده
     * @return true اگر داده در کش موجود و معتبر باشد
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
     * پاک کردن تمام آیتم‌های کش
     */
    public static void clearCache() {
        cache.clear();
    }
    
    /**
     * پاک کردن آیتم‌های منقضی شده از کش
     */
    public static void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * دریافت آمار کش
     * 
     * @return نقشه شامل آمار کش (تعداد کل، حداکثر اندازه، آیتم‌های فعال و منقضی)
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
    
    // ==================== پردازش ناهمزمان (ASYNC PROCESSING) ====================
    
    // استخر thread ها برای اجرای کارهای ناهمزمان (10 thread)
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    /**
     * اجرای کار به صورت ناهمزمان
     * این متد امکان اجرای کارها در background را فراهم می‌کند
     * 
     * @param task کار برای اجرای ناهمزمان
     * @return Future برای کنترل و انتظار نتیجه
     */
    public static Future<Void> executeAsync(Runnable task) {
        return executor.submit(() -> {
            task.run();
            return null;
        });
    }
    
    /**
     * اجرای چندین کار به صورت همزمان و موازی
     * این متد لیستی از کارها را در threads مختلف اجرا می‌کند
     * 
     * @param tasks لیست کارها برای اجرای موازی
     * @return لیست Future برای کنترل همه کارها
     */
    public static List<Future<Void>> executeConcurrently(List<Runnable> tasks) {
        List<Future<Void>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(executeAsync(task));
        }
        return futures;
    }
    
    /**
     * انتظار برای تکمیل همه کارها با timeout
     * این متد منتظر می‌ماند تا همه کارها تکمیل شوند یا timeout رخ دهد
     * 
     * @param futures لیست Future کارها
     * @param timeout حداکثر زمان انتظار
     * @param unit واحد زمانی timeout
     * @return true اگر همه کارها تکمیل شوند، false در غیر اینصورت
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
    
    // ==================== بهینه‌سازی حافظه (MEMORY OPTIMIZATION) ====================
    
    /**
     * اجبار Garbage Collection و دریافت آمار حافظه
     * این متد GC را اجرا کرده و آمار کامل حافظه قبل و بعد از GC برمی‌گرداند
     * 
     * @return نقشه آمار حافظه شامل مقادیر قبل/بعد GC و حافظه آزاد شده
     */
    public static Map<String, Long> forceGarbageCollection() {
        Runtime runtime = Runtime.getRuntime();
        
        // محاسبه حافظه استفاده شده قبل از GC
        long beforeGC = runtime.totalMemory() - runtime.freeMemory();
        runtime.gc(); // اجرای Garbage Collection
        
        // دادن زمان برای تکمیل GC
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // محاسبه حافظه استفاده شده بعد از GC
        long afterGC = runtime.totalMemory() - runtime.freeMemory();
        long recovered = beforeGC - afterGC; // مقدار حافظه آزاد شده
        
        // تهیه آمار کامل حافظه
        Map<String, Long> stats = new ConcurrentHashMap<>();
        stats.put("memoryBeforeGC", beforeGC);      // حافظه قبل از GC (بایت)
        stats.put("memoryAfterGC", afterGC);        // حافظه بعد از GC (بایت)
        stats.put("memoryRecovered", recovered);     // حافظه آزاد شده (بایت)
        stats.put("totalMemory", runtime.totalMemory()); // کل حافظه تخصیص یافته
        stats.put("maxMemory", runtime.maxMemory());      // حداکثر حافظه در دسترس
        stats.put("freeMemory", runtime.freeMemory());    // حافظه آزاد فعلی
        
        return stats;
    }
    
    /**
     * بررسی وضعیت بحرانی استفاده از حافظه
     * اگر استفاده از حافظه بیش از 85% باشد، وضعیت بحرانی است
     * 
     * @return true اگر استفاده از حافظه بحرانی باشد (بیش از 85%)
     */
    public static boolean isMemoryUsageCritical() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory(); // حافظه استفاده شده
        long maxMemory = runtime.maxMemory();                           // حداکثر حافظه
        
        double usagePercentage = (double) usedMemory / maxMemory;        // درصد استفاده
        return usagePercentage > 0.85; // بحرانی اگر بیش از 85% باشد
    }
    
    /**
     * دریافت آمار فعلی استفاده از حافظه
     * شامل تمام اطلاعات مربوط به حافظه بر حسب مگابایت و درصد
     * 
     * @return نقشه آمار حافظه شامل مقادیر MB و درصد استفاده
     */
    public static Map<String, Object> getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory(); // حافظه استفاده شده
        long maxMemory = runtime.maxMemory();                           // حداکثر حافظه
        
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("usedMemoryMB", usedMemory / 1024 / 1024);           // حافظه استفاده شده (MB)
        stats.put("totalMemoryMB", runtime.totalMemory() / 1024 / 1024); // کل حافظه تخصیص یافته (MB)
        stats.put("maxMemoryMB", maxMemory / 1024 / 1024);             // حداکثر حافظه (MB)
        stats.put("freeMemoryMB", runtime.freeMemory() / 1024 / 1024); // حافظه آزاد (MB)
        stats.put("usagePercentage", Math.round((double) usedMemory / maxMemory * 100 * 100.0) / 100.0); // درصد استفاده
        stats.put("isCritical", isMemoryUsageCritical());              // وضعیت بحرانی یا نه
        
        return stats;
    }
    
    // ==================== عملیات دسته‌ای (BULK OPERATIONS) ====================
    
    /**
     * پردازش داده‌ها در دسته‌های کوچک برای بهبود عملکرد
     * این متد لیست بزرگ داده‌ها را به دسته‌های کوچک تقسیم و پردازش می‌کند
     * برای جلوگیری از مشکلات حافظه و بهبود عملکرد پایگاه داده مفید است
     * 
     * @param data لیست کامل داده‌ها برای پردازش
     * @param batchSize اندازه هر دسته (تعداد آیتم در هر batch)
     * @param processor پردازشگر دسته‌ای که روی هر دسته اعمال می‌شود
     * @param <T> نوع داده‌های درون لیست
     */
    public static <T> void processBatch(List<T> data, int batchSize, BatchProcessor<T> processor) {
        if (data.isEmpty()) return; // اگر داده‌ای نباشد، هیچ کاری نکن
        
        // تقسیم داده‌ها به دسته‌های کوچک و پردازش هر دسته
        for (int i = 0; i < data.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, data.size()); // محاسبه انتهای دسته
            List<T> batch = data.subList(i, endIndex);           // استخراج دسته فعلی
            processor.process(batch);                            // پردازش دسته
        }
    }
    
    /**
     * رابط تابعی برای پردازش دسته‌ای
     * این interface برای تعریف منطق پردازش هر دسته استفاده می‌شود
     */
    @FunctionalInterface
    public interface BatchProcessor<T> {
        /**
         * پردازش یک دسته از داده‌ها
         * 
         * @param batch دسته داده‌ها برای پردازش
         */
        void process(List<T> batch);
    }
    
    // ==================== بهینه‌سازی کوئری (QUERY OPTIMIZATION) ====================
    
    /**
     * ایجاد کلید کش بهینه شده برای کوئری‌های پایگاه داده
     * این متد کلید منحصر به فرد برای کش کردن نتایج کوئری‌ها می‌سازد
     * 
     * @param operation نام عملیات (مثل getUserById، getOrdersByStatus)
     * @param params پارامترهای کوئری که در کلید کش لحاظ می‌شوند
     * @return کلید کش منحصر به فرد
     */
    public static String createQueryCacheKey(String operation, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(operation);
        for (Object param : params) {
            keyBuilder.append("_").append(param != null ? param.toString() : "null");
        }
        return keyBuilder.toString();
    }
    
    /**
     * اجرای کوئری با پشتیبانی از کش (TTL پیش‌فرض)
     * ابتدا از کش بررسی می‌کند، در صورت عدم وجود کوئری را اجرا می‌کند
     * 
     * @param cacheKey کلید کش برای ذخیره‌سازی
     * @param executor اجراکننده کوئری
     * @param resultType نوع نتیجه کوئری
     * @param <T> نوع generic نتیجه
     * @return نتیجه کوئری (از کش یا اجرای مستقیم)
     */
    public static <T> T executeWithCache(String cacheKey, QueryExecutor<T> executor, Class<T> resultType) {
        return executeWithCache(cacheKey, executor, resultType, DEFAULT_CACHE_TTL_MINUTES);
    }
    
    /**
     * اجرای کوئری با پشتیبانی از کش (TTL سفارشی)
     * این متد الگوی cache-aside را پیاده‌سازی می‌کند
     * 
     * @param cacheKey کلید کش برای ذخیره‌سازی
     * @param executor اجراکننده کوئری
     * @param resultType نوع نتیجه کوئری
     * @param ttlMinutes مدت زمان انقضای کش (دقیقه)
     * @param <T> نوع generic نتیجه
     * @return نتیجه کوئری (از کش یا اجرای مستقیم)
     */
    public static <T> T executeWithCache(String cacheKey, QueryExecutor<T> executor, Class<T> resultType, long ttlMinutes) {
        // ابتدا از کش بررسی کن
        T cachedResult = getCachedData(cacheKey, resultType);
        if (cachedResult != null) {
            return cachedResult; // برگرداندن نتیجه از کش
        }
        
        // اجرای کوئری اصلی
        T result = executor.execute();
        
        // کش کردن نتیجه در صورت null نبودن
        if (result != null) {
            cacheData(cacheKey, result, ttlMinutes);
        }
        
        return result;
    }
    
    /**
     * رابط تابعی برای اجرای کوئری
     * این interface برای تعریف منطق اجرای کوئری استفاده می‌شود
     */
    @FunctionalInterface
    public interface QueryExecutor<T> {
        /**
         * اجرای کوئری و برگرداندن نتیجه
         * 
         * @return نتیجه کوئری
         */
        T execute();
    }
    
    // ==================== نظارت بر عملکرد (PERFORMANCE MONITORING) ====================
    
    /**
     * اندازه‌گیری زمان اجرا و استفاده حافظه یک عملیات
     * این متد عملکرد یک تابع را کامل اندازه‌گیری می‌کند
     * 
     * @param operationName نام عملیات برای ثبت در نتیجه
     * @param operation تابع برای اندازه‌گیری عملکرد
     * @param <T> نوع نتیجه عملیات
     * @return نتیجه شامل زمان اجرا، استفاده حافظه و نتیجه عملیات
     */
    public static <T> PerformanceResult<T> measurePerformance(String operationName, PerformanceOperation<T> operation) {
        // ثبت زمان و حافظه قبل از شروع
        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        // اجرای عملیات اصلی
        T result = operation.execute();
        
        // ثبت زمان و حافظه بعد از اتمام
        long endTime = System.currentTimeMillis();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        // محاسبه اختلاف‌ها
        long executionTime = endTime - startTime;       // زمان اجرا (میلی‌ثانیه)
        long memoryUsed = endMemory - startMemory;      // حافظه استفاده شده (بایت)
        
        return new PerformanceResult<>(operationName, result, executionTime, memoryUsed);
    }
    
    /**
     * رابط تابعی برای عملیات قابل اندازه‌گیری
     * این interface برای تعریف عملیاتی که می‌خواهیم عملکردشان را اندازه‌گیری کنیم
     */
    @FunctionalInterface
    public interface PerformanceOperation<T> {
        /**
         * اجرای عملیات و برگرداندن نتیجه
         * 
         * @return نتیجه عملیات
         */
        T execute();
    }
    
    /**
     * کلاس نتیجه اندازه‌گیری عملکرد
     * شامل تمام اطلاعات مربوط به عملکرد یک عملیات
     */
    public static class PerformanceResult<T> {
        private final String operationName;      // نام عملیات
        private final T result;                  // نتیجه عملیات
        private final long executionTimeMs;     // زمان اجرا (میلی‌ثانیه)
        private final long memoryUsedBytes;     // حافظه استفاده شده (بایت)
        
        /**
         * سازنده نتیجه عملکرد
         * 
         * @param operationName نام عملیات
         * @param result نتیجه عملیات
         * @param executionTimeMs زمان اجرا بر حسب میلی‌ثانیه
         * @param memoryUsedBytes حافظه استفاده شده بر حسب بایت
         */
        public PerformanceResult(String operationName, T result, long executionTimeMs, long memoryUsedBytes) {
            this.operationName = operationName;
            this.result = result;
            this.executionTimeMs = executionTimeMs;
            this.memoryUsedBytes = memoryUsedBytes;
        }
        
        // متدهای دسترسی به داده‌ها
        public String getOperationName() { return operationName; }
        public T getResult() { return result; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public long getMemoryUsedBytes() { return memoryUsedBytes; }
        
        /**
         * دریافت حافظه استفاده شده بر حسب مگابایت
         * 
         * @return حافظه استفاده شده (MB)
         */
        public double getMemoryUsedMB() { return memoryUsedBytes / 1024.0 / 1024.0; }
        
        /**
         * نمایش فرمت شده نتایج عملکرد
         * 
         * @return رشته فرمت شده نتایج
         */
        @Override
        public String toString() {
            return String.format("Performance[%s]: %dms, %.2fMB", 
                operationName, executionTimeMs, getMemoryUsedMB());
        }
    }
    
    // ==================== پاکسازی و خاتمه (CLEANUP) ====================
    
    /**
     * خاتمه و پاکسازی ابزارهای عملکرد
     * این متد تمام منابع استفاده شده توسط کلاس را آزاد می‌کند
     * باید در shutdown hook یا پایان برنامه فراخوانی شود
     */
    public static void shutdown() {
        try {
            // خاتمه ExecutorService به صورت graceful
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                // اگر بعد از 5 ثانیه هنوز تکمیل نشده، force shutdown
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // در صورت interrupt، فوری shutdown کن
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // پاکسازی کش
        clearCache();
    }
} 
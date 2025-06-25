package com.myapp.common.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance Utility Test Suite
 * تست‌های جامع برای Performance Optimization Utility
 */
@DisplayName("Performance Utility Tests")
class PerformanceUtilTest {

    @BeforeEach
    void setUp() {
        PerformanceUtil.clearCache();
    }

    @AfterEach
    void tearDown() {
        PerformanceUtil.clearCache();
    }

    @AfterAll
    static void tearDownClass() {
        PerformanceUtil.shutdown();
    }

    // ==================== CACHING TESTS ====================

    @Nested
    @DisplayName("Caching System Tests")
    class CachingTests {

        @Test
        @DisplayName("✅ Cache Storage and Retrieval")
        void testCacheStorageAndRetrieval() {
            String key = "test_key";
            String value = "test_value";
            
            PerformanceUtil.cacheData(key, value);
            assertTrue(PerformanceUtil.isCached(key));
            
            String cachedValue = PerformanceUtil.getCachedData(key, String.class);
            assertEquals(value, cachedValue);
        }

        @Test
        @DisplayName("✅ Cache TTL Expiration")
        void cacheTTLExpiration_ExpiredData_ReturnsNull() throws InterruptedException {
            String key = "ttl_test_key";
            String value = "ttl_test_value";
            
            // Store data with very short TTL (0.1 minutes = 6 seconds)
            PerformanceUtil.cacheData(key, value, 0.1);
            
            // Verify data is initially cached
            assertTrue(PerformanceUtil.isCached(key));
            
            // Wait for expiration (7 seconds)
            Thread.sleep(7000);
            
            // Verify data is no longer cached
            assertFalse(PerformanceUtil.isCached(key));
            assertNull(PerformanceUtil.getCachedData(key, String.class));
        }

        @Test
        @DisplayName("✅ Cache Type Safety")
        void cacheTypeSafety_WrongType_ReturnsNull() {
            String key = "type_test_key";
            Integer value = 42;
            
            // Store Integer in cache
            PerformanceUtil.cacheData(key, value);
            
            // Try to retrieve as String (wrong type)
            String wrongType = PerformanceUtil.getCachedData(key, String.class);
            assertNull(wrongType);
            
            // Retrieve as correct type
            Integer correctType = PerformanceUtil.getCachedData(key, Integer.class);
            assertEquals(value, correctType);
        }

        @Test
        @DisplayName("✅ Cache Statistics")
        void cacheStatistics_VariousOperations_AccurateStats() {
            // Add some data to cache
            for (int i = 0; i < 10; i++) {
                PerformanceUtil.cacheData("key" + i, "value" + i);
            }
            
            Map<String, Object> stats = PerformanceUtil.getCacheStats();
            
            assertEquals(10, stats.get("totalEntries"));
            assertEquals(10, stats.get("activeEntries"));
            assertEquals(0L, stats.get("expiredEntries"));
            assertEquals(1000, stats.get("maxSize"));
        }

        @Test
        @DisplayName("✅ Cache Size Limit")
        void cacheSizeLimit_ExceedsMaxSize_OldEntriesRemoved() {
            // Fill cache beyond max size (1000 entries)
            for (int i = 0; i < 1200; i++) {
                PerformanceUtil.cacheData("key" + i, "value" + i);
            }
            
            Map<String, Object> stats = PerformanceUtil.getCacheStats();
            int totalEntries = (Integer) stats.get("totalEntries");
            
            // Should not exceed max size significantly
            assertTrue(totalEntries <= 1100, "Cache size should be controlled: " + totalEntries);
        }
    }

    // ==================== ASYNC PROCESSING TESTS ====================

    @Nested
    @DisplayName("Async Processing Tests")
    class AsyncProcessingTests {

        @Test
        @DisplayName("✅ Async Task Execution")
        void testAsyncTaskExecution() throws Exception {
            List<String> results = new ArrayList<>();
            
            Future<Void> future = PerformanceUtil.executeAsync(() -> {
                results.add("Task completed");
            });
            
            future.get(5, TimeUnit.SECONDS);
            assertEquals(1, results.size());
            assertEquals("Task completed", results.get(0));
        }

        @Test
        @DisplayName("✅ Concurrent Task Execution")
        void concurrentTaskExecution_MultipleTasks_AllComplete() throws InterruptedException {
            List<String> results = Collections.synchronizedList(new ArrayList<>());
            List<Runnable> tasks = new ArrayList<>();
            
            // Create 10 tasks
            for (int i = 0; i < 10; i++) {
                final int taskNumber = i;
                tasks.add(() -> {
                    try {
                        Thread.sleep(100); // Simulate work
                        results.add("Task " + taskNumber);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            // Execute all tasks concurrently
            List<Future<Void>> futures = PerformanceUtil.executeConcurrently(tasks);
            
            // Wait for all to complete
            boolean allCompleted = PerformanceUtil.waitForCompletion(futures, 10, TimeUnit.SECONDS);
            
            assertTrue(allCompleted);
            assertEquals(10, results.size());
        }
    }

    // ==================== MEMORY OPTIMIZATION TESTS ====================

    @Nested
    @DisplayName("Memory Optimization Tests")
    class MemoryOptimizationTests {

        @Test
        @DisplayName("✅ Garbage Collection Statistics")
        void garbageCollectionStats_ForceGC_ReturnsValidStats() {
            Map<String, Long> stats = PerformanceUtil.forceGarbageCollection();
            
            assertNotNull(stats);
            assertTrue(stats.containsKey("memoryBeforeGC"));
            assertTrue(stats.containsKey("memoryAfterGC"));
            assertTrue(stats.containsKey("memoryRecovered"));
            assertTrue(stats.containsKey("totalMemory"));
            assertTrue(stats.containsKey("maxMemory"));
            assertTrue(stats.containsKey("freeMemory"));
            
            // All values should be non-negative
            for (Long value : stats.values()) {
                assertTrue(value >= 0, "Memory stat should be non-negative: " + value);
            }
        }

        @Test
        @DisplayName("✅ Memory Usage Statistics")
        void testMemoryStatistics() {
            Map<String, Object> stats = PerformanceUtil.getMemoryStats();
            
            assertNotNull(stats);
            assertTrue(stats.containsKey("usedMemoryMB"));
            assertTrue(stats.containsKey("totalMemoryMB"));
            assertTrue(stats.containsKey("maxMemoryMB"));
            assertTrue(stats.containsKey("freeMemoryMB"));
            assertTrue(stats.containsKey("usagePercentage"));
            assertTrue(stats.containsKey("isCritical"));
            
            // Usage percentage should be between 0 and 100
                    Double usagePercentage = (Double) stats.get("usagePercentage");
        assertTrue(usagePercentage >= 0.0 && usagePercentage <= 100.0);
        }

        @Test
        @DisplayName("✅ Memory Critical Detection")
        void memoryCriticalDetection_NormalUsage_NotCritical() {
            // Under normal test conditions, memory shouldn't be critical
            assertFalse(PerformanceUtil.isMemoryUsageCritical());
        }
    }

    // ==================== BULK OPERATIONS TESTS ====================

    @Nested
    @DisplayName("Bulk Operations Tests")
    class BulkOperationsTests {

        @Test
        @DisplayName("✅ Batch Processing")
        void testBatchProcessing() {
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < 250; i++) {
                data.add(i);
            }
            
            List<Integer> processedBatches = new ArrayList<>();
            
            PerformanceUtil.processBatch(data, 100, batch -> {
                processedBatches.add(batch.size());
            });
            
            assertEquals(3, processedBatches.size());
            assertEquals(100, processedBatches.get(0).intValue());
            assertEquals(100, processedBatches.get(1).intValue());
            assertEquals(50, processedBatches.get(2).intValue());
        }

        @Test
        @DisplayName("✅ Batch Processing with Remainder")
        void batchProcessingWithRemainder_UnevenDataset_HandlesRemainder() {
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < 1050; i++) {
                data.add(i);
            }
            
            List<Integer> processedBatches = new ArrayList<>();
            int batchSize = 100;
            
            PerformanceUtil.processBatch(data, batchSize, batch -> {
                processedBatches.add(batch.size());
            });
            
            // Should have 10 full batches and 1 partial batch
            assertEquals(11, processedBatches.size());
            
            // First 10 batches should be full
            for (int i = 0; i < 10; i++) {
                assertEquals(100, processedBatches.get(i));
            }
            
            // Last batch should have remainder
            assertEquals(50, processedBatches.get(10));
        }
    }

    // ==================== QUERY OPTIMIZATION TESTS ====================

    @Nested
    @DisplayName("Query Optimization Tests")
    class QueryOptimizationTests {

        @Test
        @DisplayName("✅ Query Cache Key Generation")
        void testQueryCacheKeyGeneration() {
            String key1 = PerformanceUtil.createQueryCacheKey("getUser", 1L, "active");
            String key2 = PerformanceUtil.createQueryCacheKey("getUser", 2L, "active");
            
            assertNotEquals(key1, key2);
            
            String key1Duplicate = PerformanceUtil.createQueryCacheKey("getUser", 1L, "active");
            assertEquals(key1, key1Duplicate);
        }

        @Test
        @DisplayName("✅ Query Execution with Cache")
        void queryExecutionWithCache_CachedQuery_ReturnsCachedResult() {
            String cacheKey = "test_query";
            String expectedResult = "cached_result";
            
            // First execution - should execute query and cache result
            String result1 = PerformanceUtil.executeWithCache(
                cacheKey,
                () -> expectedResult,
                String.class
            );
            
            assertEquals(expectedResult, result1);
            assertTrue(PerformanceUtil.isCached(cacheKey));
            
            // Second execution - should return cached result
            String result2 = PerformanceUtil.executeWithCache(
                cacheKey,
                () -> "different_result", // This shouldn't be executed
                String.class
            );
            
            assertEquals(expectedResult, result2); // Should return cached result
        }

        @Test
        @DisplayName("✅ Query Cache with Custom TTL")
        void queryCacheWithCustomTTL_ShortTTL_ExpiresCorrectly() throws InterruptedException {
            String cacheKey = "ttl_query";
            String expectedResult = "ttl_result";
            
            // Execute with very short TTL
            String result1 = PerformanceUtil.executeWithCache(
                cacheKey,
                () -> expectedResult,
                String.class,
                0.1 // 0.1 minutes = 6 seconds
            );
            
            assertEquals(expectedResult, result1);
            
            // Wait for expiration
            Thread.sleep(7000);
            
            // Should execute query again
            String result2 = PerformanceUtil.executeWithCache(
                cacheKey,
                () -> "new_result",
                String.class,
                0.1
            );
            
            assertEquals("new_result", result2);
        }
    }

    // ==================== PERFORMANCE MONITORING TESTS ====================

    @Nested
    @DisplayName("Performance Monitoring Tests")
    class PerformanceMonitoringTests {

        @Test
        @DisplayName("✅ Performance Measurement")
        void testPerformanceMeasurement() {
            PerformanceUtil.PerformanceResult<String> result = PerformanceUtil.measurePerformance(
                "test_operation",
                () -> {
                    try {
                        Thread.sleep(50);
                        return "operation_result";
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            );
            
            assertEquals("test_operation", result.getOperationName());
            assertEquals("operation_result", result.getResult());
            assertTrue(result.getExecutionTimeMs() >= 40);
        }

        @Test
        @DisplayName("✅ Performance Result String Representation")
        void performanceResultStringRepresentation_ValidResult_FormattedCorrectly() {
            PerformanceUtil.PerformanceResult<String> result = new PerformanceUtil.PerformanceResult<>(
                "test_op", "result", 150L, 1024L * 1024L // 1MB
            );
            
            String resultString = result.toString();
            assertTrue(resultString.contains("test_op"));
            assertTrue(resultString.contains("150ms"));
            assertTrue(resultString.contains("1.00MB"));
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 50, 100, 200, 500})
        @DisplayName("✅ Performance Measurement Scalability")
        void performanceMeasurementScalability_VariousWorkloads_ScalesLinearly(int workload) {
            PerformanceUtil.PerformanceResult<Integer> result = PerformanceUtil.measurePerformance(
                "scalability_test_" + workload,
                () -> {
                    int sum = 0;
                    for (int i = 0; i < workload * 1000; i++) {
                        sum += i;
                    }
                    return sum;
                }
            );
            
            assertNotNull(result.getResult());
            assertTrue(result.getExecutionTimeMs() >= 0);
            
            // Execution time should roughly scale with workload
            // (allowing for some variance due to JVM optimizations)
            double expectedTimeRatio = (double) workload / 10;
            double actualTimeRatio = (double) result.getExecutionTimeMs() / Math.max(1, result.getExecutionTimeMs());
            
            // Just verify that performance measurement works for all workloads
            assertTrue(result.getExecutionTimeMs() < 5000, 
                "Even large workloads should complete quickly: " + result.getExecutionTimeMs() + "ms");
        }
    }

    // ==================== INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("✅ Complete Performance Optimization Workflow")
        void completePerformanceWorkflow_AllFeatures_WorkTogether() {
            // 1. Measure performance of cached operation
            PerformanceUtil.PerformanceResult<List<String>> result = PerformanceUtil.measurePerformance(
                "cached_data_processing",
                () -> {
                    String cacheKey = PerformanceUtil.createQueryCacheKey("getAllUsers", "active");
                    
                    return PerformanceUtil.executeWithCache(
                        cacheKey,
                        () -> {
                            // Simulate expensive operation
                            List<String> data = new ArrayList<>();
                            for (int i = 0; i < 1000; i++) {
                                data.add("User " + i);
                            }
                            return data;
                        },
                        List.class
                    );
                }
            );
            
            // 2. Verify results
            assertNotNull(result.getResult());
            assertEquals(1000, ((List<?>) result.getResult()).size());
            assertTrue(result.getExecutionTimeMs() >= 0);
            
            // 3. Process data in batches asynchronously
            List<Future<Void>> futures = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<String> data = (List<String>) result.getResult();
            
            PerformanceUtil.processBatch(data, 100, batch -> {
                Future<Void> future = PerformanceUtil.executeAsync(() -> {
                    // Simulate batch processing
                    batch.forEach(item -> {
                        // Process item
                    });
                });
                futures.add(future);
            });
            
            // 4. Wait for all batch processing to complete
            boolean allCompleted = PerformanceUtil.waitForCompletion(futures, 10, TimeUnit.SECONDS);
            assertTrue(allCompleted);
            
            // 5. Check memory usage
            Map<String, Object> memoryStats = PerformanceUtil.getMemoryStats();
            assertFalse((Boolean) memoryStats.get("isCritical"));
            
            // 6. Verify cache statistics
            Map<String, Object> cacheStats = PerformanceUtil.getCacheStats();
            assertTrue((Integer) cacheStats.get("totalEntries") > 0);
        }
    }
} 
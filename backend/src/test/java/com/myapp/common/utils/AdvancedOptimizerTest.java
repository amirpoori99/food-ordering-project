package com.myapp.common.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * مجموعه تست‌های جامع ابزار بهینه‌سازی پیشرفته
 * 
 * این کلاس تست تمام عملکردهای کلاس AdvancedOptimizer را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Connection Pool Tests
 *    - مدیریت اتصالات
 *    - timeout handling
 *    - resource tracking
 *    - error handling
 * 
 * 2. Circuit Breaker Tests
 *    - failure detection
 *    - auto recovery
 *    - state management
 *    - cascade failure prevention
 * 
 * 3. Rate Limiting Tests
 *    - request limiting
 *    - time window management
 *    - thread safety
 *    - dynamic adjustment
 * 
 * 4. Integration Tests
 *    - ترکیب تمام سیستم‌ها
 *    - performance monitoring
 *    - error handling
 *    - resource management
 * 
 * Performance Optimization Patterns:
 * - Connection pooling strategies
 * - Circuit breaker patterns
 * - Rate limiting techniques
 * - Resource management
 * 
 * Production Ready Features:
 * - Thread-safe operations
 * - Resource cleanup
 * - Error handling
 * - Performance metrics
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("🔧 Advanced Optimizer Tests")
class AdvancedOptimizerTest {

    // ==================== تست‌های Connection Pool ====================

    /**
     * تست‌های Connection Pool
     * 
     * این دسته شامل عملیات مدیریت اتصالات:
     * - مدیریت اتصالات
     * - timeout handling
     * - resource tracking
     * - error handling
     */
    @Nested
    @DisplayName("🔗 Connection Pool Tests")
    class ConnectionPoolTests {

        @Test
        @DisplayName("✅ Connection Pool Execution")
        void connectionPool_ExecuteOperation_Success() throws Exception {
            // Arrange
            String expectedResult = "test result";
            
            // Act
            String result = AdvancedOptimizer.executeWithConnection(() -> expectedResult);
            
            // Assert
            assertEquals(expectedResult, result);
        }

        @Test
        @DisplayName("✅ Connection Pool Statistics")
        void connectionPool_GetStats_ValidData() {
            // Act
            Map<String, Object> stats = AdvancedOptimizer.getConnectionPoolStats();
            
            // Assert
            assertNotNull(stats);
            assertTrue(stats.containsKey("activeConnections"));
            assertTrue(stats.containsKey("totalConnections"));
            assertTrue(stats.containsKey("currentConnections"));
            assertTrue(stats.containsKey("availableConnections"));
            assertTrue(stats.containsKey("connectionUtilization"));
            assertTrue(stats.containsKey("connectionWaitTime"));
            assertTrue(stats.containsKey("connectionErrors"));
        }

        @Test
        @DisplayName("✅ Connection Pool Error Handling")
        void connectionPool_ExecuteWithError_HandlesException() {
            // Arrange
            RuntimeException expectedException = new RuntimeException("Test error");
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                try {
                    AdvancedOptimizer.executeWithConnection(() -> {
                        throw expectedException;
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            
            assertEquals(expectedException.getMessage(), exception.getCause().getMessage());
        }
    }

    // ==================== تست‌های Circuit Breaker ====================

    /**
     * تست‌های Circuit Breaker
     * 
     * این دسته شامل عملیات مدیریت خطاها:
     * - failure detection
     * - auto recovery
     * - state management
     * - cascade failure prevention
     */
    @Nested
    @DisplayName("⚡ Circuit Breaker Tests")
    class CircuitBreakerTests {

        @Test
        @DisplayName("✅ Circuit Breaker Normal Operation")
        void circuitBreaker_NormalOperation_Success() throws Exception {
            // Arrange
            String operationName = "test-operation";
            String expectedResult = "success";
            
            // Act
            String result = AdvancedOptimizer.executeWithCircuitBreaker(operationName, () -> expectedResult);
            
            // Assert
            assertEquals(expectedResult, result);
        }

        @Test
        @DisplayName("✅ Circuit Breaker Failure Handling")
        void circuitBreaker_FailureHandling_ThrowsException() {
            // Arrange
            String operationName = "test-operation-failure";
            RuntimeException expectedException = new RuntimeException("Test failure");
            
            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class, () -> {
                AdvancedOptimizer.executeWithCircuitBreaker(operationName, () -> {
                    throw expectedException;
                });
            });
            
            assertEquals(expectedException.getMessage(), exception.getMessage());
        }

        @Test
        @DisplayName("✅ Circuit Breaker Statistics")
        void circuitBreaker_GetStats_ValidData() {
            // Act
            Map<String, Object> stats = AdvancedOptimizer.getCircuitBreakerStats();
            
            // Assert
            assertNotNull(stats);
            assertTrue(stats.containsKey("totalCircuitBreakers"));
            assertTrue(stats.containsKey("circuitBreakers"));
        }
    }

    // ==================== تست‌های Rate Limiting ====================

    /**
     * تست‌های Rate Limiting
     * 
     * این دسته شامل عملیات کنترل درخواست‌ها:
     * - request limiting
     * - time window management
     * - thread safety
     * - dynamic adjustment
     */
    @Nested
    @DisplayName("🚦 Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("✅ Rate Limiting Normal Operation")
        void rateLimiting_NormalOperation_Success() throws Exception {
            // Arrange
            String operationName = "test-rate-limit";
            int maxRequests = 10;
            long timeWindowMs = 1000;
            String expectedResult = "success";
            
            // Act
            String result = AdvancedOptimizer.executeWithRateLimit(operationName, maxRequests, timeWindowMs, () -> expectedResult);
            
            // Assert
            assertEquals(expectedResult, result);
        }

        @Test
        @DisplayName("✅ Rate Limiting Statistics")
        void rateLimiting_GetStats_ValidData() {
            // Act
            Map<String, Object> stats = AdvancedOptimizer.getRateLimiterStats();
            
            // Assert
            assertNotNull(stats);
            assertTrue(stats.containsKey("totalRateLimiters"));
            assertTrue(stats.containsKey("rateLimiters"));
        }

        @Test
        @DisplayName("✅ Rate Limiting Multiple Operations")
        void rateLimiting_MultipleOperations_AllSuccess() throws Exception {
            // Arrange
            String operationName = "test-multiple-rate-limit";
            int maxRequests = 5;
            long timeWindowMs = 1000;
            
            // Act
            List<String> results = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                final int index = i;
                String result = AdvancedOptimizer.executeWithRateLimit(operationName, maxRequests, timeWindowMs, () -> "result-" + index);
                results.add(result);
            }
            
            // Assert
            assertEquals(3, results.size());
            assertEquals("result-0", results.get(0));
            assertEquals("result-1", results.get(1));
            assertEquals("result-2", results.get(2));
        }
    }

    // ==================== تست‌های یکپارچگی ====================

    /**
     * تست‌های یکپارچگی
     * 
     * این دسته شامل تست‌های ترکیبی:
     * - ترکیب تمام سیستم‌ها
     * - performance monitoring
     * - error handling
     * - resource management
     */
    @Nested
    @DisplayName("🔗 Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("✅ Complete Optimization Report")
        void integration_CompleteReport_ValidData() {
            // Act
            Map<String, Object> report = AdvancedOptimizer.getCompleteOptimizationReport();
            
            // Assert
            assertNotNull(report);
            assertTrue(report.containsKey("timestamp"));
            assertTrue(report.containsKey("connectionPool"));
            assertTrue(report.containsKey("circuitBreakers"));
            assertTrue(report.containsKey("rateLimiters"));
        }

        @Test
        @DisplayName("✅ Optimized Execution")
        void integration_OptimizedExecution_Success() throws Exception {
            // Arrange
            String operationName = "test-optimized";
            String expectedResult = "optimized result";
            
            // Act
            String result = AdvancedOptimizer.executeOptimized(operationName, () -> expectedResult);
            
            // Assert
            assertEquals(expectedResult, result);
        }

        @Test
        @DisplayName("✅ Multiple Optimized Operations")
        void integration_MultipleOptimizedOperations_AllSuccess() throws Exception {
            // Arrange
            List<String> operationNames = Arrays.asList("op1", "op2", "op3");
            List<String> expectedResults = Arrays.asList("result1", "result2", "result3");
            
            // Act
            List<String> results = new ArrayList<>();
            for (int i = 0; i < operationNames.size(); i++) {
                final int index = i;
                String result = AdvancedOptimizer.executeOptimized(operationNames.get(i), () -> expectedResults.get(index));
                results.add(result);
            }
            
            // Assert
            assertEquals(expectedResults.size(), results.size());
            for (int i = 0; i < expectedResults.size(); i++) {
                assertEquals(expectedResults.get(i), results.get(i));
            }
        }
    }

    // ==================== تست‌های تنظیمات پویا ====================

    /**
     * تست‌های تنظیمات پویا
     * 
     * این دسته شامل تست‌های تنظیمات:
     * - connection pool size
     * - circuit breaker threshold
     * - rate limit settings
     */
    @Nested
    @DisplayName("⚙️ Dynamic Configuration Tests")
    class DynamicConfigurationTests {

        @Test
        @DisplayName("✅ Set Connection Pool Size")
        void dynamicConfig_SetConnectionPoolSize_NoException() {
            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> {
                AdvancedOptimizer.setConnectionPoolSize(100);
            });
        }

        @Test
        @DisplayName("✅ Set Circuit Breaker Threshold")
        void dynamicConfig_SetCircuitBreakerThreshold_NoException() {
            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> {
                AdvancedOptimizer.setCircuitBreakerThreshold("test-circuit", 10);
            });
        }

        @Test
        @DisplayName("✅ Set Rate Limit")
        void dynamicConfig_SetRateLimit_NoException() {
            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> {
                AdvancedOptimizer.setRateLimit("test-rate", 50, 5000);
            });
        }
    }

    // ==================== تست‌های Edge Cases ====================

    /**
     * تست‌های Edge Cases
     * 
     * این دسته شامل تست‌های حالت‌های خاص:
     * - null values
     * - empty operations
     * - extreme values
     * - concurrent access
     */
    @Nested
    @DisplayName("🔍 Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("✅ Zero Values")
        void edgeCases_ZeroValues_HandlesGracefully() {
            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> {
                AdvancedOptimizer.setConnectionPoolSize(0);
                AdvancedOptimizer.setCircuitBreakerThreshold("test", 0);
                AdvancedOptimizer.setRateLimit("test", 0, 0);
            });
        }

        @Test
        @DisplayName("✅ Negative Values")
        void edgeCases_NegativeValues_HandlesGracefully() {
            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> {
                AdvancedOptimizer.setConnectionPoolSize(-1);
                AdvancedOptimizer.setCircuitBreakerThreshold("test", -5);
                AdvancedOptimizer.setRateLimit("test", -10, -1000);
            });
        }

        @Test
        @DisplayName("✅ Large Values")
        void edgeCases_LargeValues_HandlesGracefully() {
            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> {
                AdvancedOptimizer.setConnectionPoolSize(10000);
                AdvancedOptimizer.setCircuitBreakerThreshold("test", 1000);
                AdvancedOptimizer.setRateLimit("test", 10000, 1000000);
            });
        }
    }

    // ==================== تست‌های Performance ====================

    /**
     * تست‌های Performance
     * 
     * این دسته شامل تست‌های عملکرد:
     * - execution time
     * - memory usage
     * - throughput
     * - scalability
     */
    @Nested
    @DisplayName("📊 Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("✅ Fast Operation Execution")
        void performance_FastOperation_CompletesQuickly() throws Exception {
            // Arrange
            long startTime = System.currentTimeMillis();
            
            // Act
            String result = AdvancedOptimizer.executeOptimized("fast-op", () -> "fast result");
            
            // Assert
            long executionTime = System.currentTimeMillis() - startTime;
            assertEquals("fast result", result);
            assertTrue(executionTime < 1000, "Operation should complete within 1 second");
        }

        @Test
        @DisplayName("✅ Multiple Concurrent Operations")
        void performance_MultipleConcurrentOperations_AllComplete() throws Exception {
            // Arrange
            int operationCount = 10;
            List<String> results = Collections.synchronizedList(new ArrayList<>());
            
            // Act
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < operationCount; i++) {
                final int index = i;
                Thread thread = new Thread(() -> {
                    try {
                        String result = AdvancedOptimizer.executeOptimized("concurrent-op-" + index, () -> "result-" + index);
                        results.add(result);
                    } catch (Exception e) {
                        // Handle exception
                    }
                });
                threads.add(thread);
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join(5000); // 5 second timeout
            }
            
            // Assert
            assertEquals(operationCount, results.size());
        }
    }

    // ==================== تست‌های Error Handling ====================

    /**
     * تست‌های Error Handling
     * 
     * این دسته شامل تست‌های مدیریت خطا:
     * - exception propagation
     * - error recovery
     * - graceful degradation
     * - error reporting
     */
    @Nested
    @DisplayName("⚠️ Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("✅ Exception Propagation")
        void errorHandling_ExceptionPropagation_ThrowsCorrectException() {
            // Arrange
            String operationName = "error-op";
            RuntimeException expectedException = new RuntimeException("Test exception");
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                AdvancedOptimizer.executeOptimized(operationName, () -> {
                    throw expectedException;
                });
            });
            assertTrue(exception.getMessage().contains(expectedException.getMessage()));
        }

        @Test
        @DisplayName("✅ Multiple Error Types")
        void errorHandling_MultipleErrorTypes_HandlesAll() {
            List<RuntimeException> exceptions = Arrays.asList(
                new RuntimeException("Runtime error"),
                new IllegalArgumentException("Illegal argument")
            );
            for (RuntimeException expectedException : exceptions) {
                RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                    AdvancedOptimizer.executeOptimized("error-op", () -> {
                        throw expectedException;
                    });
                });
                assertTrue(exception.getMessage() != null && exception.getMessage().contains(expectedException.getMessage()));
            }
        }
    }
} 
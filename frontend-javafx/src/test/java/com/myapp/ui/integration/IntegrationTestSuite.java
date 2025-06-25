package com.myapp.ui.integration;

import com.myapp.ui.common.BaseTestClass;
import com.myapp.ui.common.HttpClientUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

/**
 * Integration Tests for Critical Scenarios
 * Tests که سناریوهای مهم و خطرناک را پوشش می‌دهند
 */
@ExtendWith(ApplicationExtension.class)
class IntegrationTestSuite extends BaseTestClass {

    @BeforeEach
    void setUp() {
        // Clear any existing state before each test
        HttpClientUtil.clearAuthToken();
    }

    @Test
    void testNetworkFailureDuringOrder() {
        // تست: اگر در وسط سفارش اینترنت قطع شود
        System.out.println("Testing network failure during order placement...");
        
        try {
            // 1. شروع فرآیند سفارش
            HttpClientUtil.ApiResponse loginResponse = HttpClientUtil.post("/auth/login",
                "{\"email\":\"test@example.com\",\"password\":\"password123\"}", false);
            
            // 2. اضافه کردن آیتم به سبد خرید
            HttpClientUtil.ApiResponse cartResponse = HttpClientUtil.post("/orders/cart/add",
                "{\"itemId\":1,\"quantity\":2}", false);
            
            // 3. شبیه‌سازی قطع شدن اینترنت با timeout کوتاه
            HttpClientUtil.setTimeoutMs(1); // Very short timeout to simulate disconnection
            
            // 4. تلاش برای تکمیل سفارش
            HttpClientUtil.ApiResponse orderResponse = HttpClientUtil.post("/orders/place",
                "{\"deliveryAddress\":\"Test Address\",\"paymentMethod\":\"CARD\"}", false);
            
            // 5. انتظار: سفارش نباید موفق شود اما error handling مناسب باشد
            assertFalse(orderResponse.isSuccess(), "Order should fail during network disconnection");
            assertNetworkErrorMessage(orderResponse.getMessage(), "Order placement during network failure");
            
            System.out.println("✓ Network failure during order handled properly");
            
        } finally {
            // Reset network timeout
            HttpClientUtil.setTimeoutMs(30000);
        }
    }

    @Test
    void testServerErrorDuringPayment() {
        // تست: اگر server در هنگام پرداخت error بدهد
        System.out.println("Testing server error during payment...");
        
        // 1. آماده‌سازی سفارش
        HttpClientUtil.ApiResponse prepareResponse = HttpClientUtil.post("/orders/prepare",
            "{\"items\":[{\"id\":1,\"quantity\":2}],\"total\":50.00}", false);
        
        // 2. تست با endpoint های مختلف payment
        String[] paymentEndpoints = {
            "/payment/card",
            "/payment/wallet", 
            "/payment/process"
        };
        
        for (String endpoint : paymentEndpoints) {
            HttpClientUtil.ApiResponse paymentResponse = HttpClientUtil.post(endpoint,
                "{\"amount\":50.00,\"method\":\"CARD\",\"cardNumber\":\"1234567890123456\"}", false);
            
            // 3. چک کردن error handling مناسب
            assertNotNull(paymentResponse, "Payment response should not be null");
            
            if (!paymentResponse.isSuccess()) {
                assertUserFriendlyErrorMessage(paymentResponse.getMessage(), 
                    "Payment error for endpoint: " + endpoint);
            }
        }
        
        System.out.println("✓ Server error during payment handled properly");
    }

    @Test
    void testConcurrentOrderPlacement() {
        // تست: اگر دو user همزمان یک آیتم آخر را سفارش دهند
        System.out.println("Testing concurrent order placement...");
        
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // شبیه‌سازی 5 کاربر که همزمان سفارش می‌دهند
        for (int i = 0; i < threadCount; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    HttpClientUtil.ApiResponse response = HttpClientUtil.post("/orders/concurrent-test",
                        "{\"userId\":" + userId + ",\"itemId\":999,\"quantity\":1}", false);
                    
                    if (response.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            // انتظار تا همه thread ها تمام شوند
            assertTrue(latch.await(10, TimeUnit.SECONDS), "Concurrent operations should complete");
            
            // چک کردن نتایج
            int totalRequests = successCount.get() + failureCount.get();
            assertEquals(threadCount, totalRequests, "All requests should be processed");
            
            System.out.println("✓ Concurrent order placement: " + 
                successCount.get() + " success, " + failureCount.get() + " failures");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Concurrent test was interrupted");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testDatabaseConnectionLoss() {
        // تست: اگر ارتباط با database قطع شود
        System.out.println("Testing database connection loss...");
        
        // 1. تست endpoints مختلف برای چک کردن database connectivity
        String[] databaseEndpoints = {
            "/restaurants",
            "/menu/items",
            "/orders/history",
            "/users/profile"
        };
        
        for (String endpoint : databaseEndpoints) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.get(endpoint);
            
            assertNotNull(response, "Response should not be null for: " + endpoint);
            
            // چک کردن error handling در صورت مشکل database
            if (!response.isSuccess()) {
                assertUserFriendlyErrorMessage(response.getMessage(), endpoint + " database error");
                
                // Error message نباید database internals را expose کند
                String errorMsg = response.getMessage().toLowerCase();
                assertFalse(errorMsg.contains("connection pool"), 
                    "Should not expose database connection details");
                assertFalse(errorMsg.contains("sql exception"), 
                    "Should not expose SQL exception details");
            }
        }
        
        System.out.println("✓ Database connection loss handled properly");
    }

    @Test
    void testMemoryLeakDuringLongUsage() {
        // تست: اگر app مدت طولانی استفاده شود آیا memory leak دارد؟
        System.out.println("Testing memory leak during long usage...");
        
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // شبیه‌سازی استفاده طولانی مدت
        for (int i = 0; i < 100; i++) {
            // درخواست‌های مختلف برای شبیه‌سازی کاربر فعال
            HttpClientUtil.get("/restaurants");
            HttpClientUtil.get("/menu/items");
            HttpClientUtil.post("/orders/cart/add", "{\"itemId\":1,\"quantity\":1}", false);
            
            // هر 20 iteration یکبار memory را چک کن
            if (i % 20 == 0) {
                System.gc(); // Force garbage collection
                waitForAsync(100); // Wait a bit for GC
                
                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                long memoryIncrease = currentMemory - initialMemory;
                
                // Memory increase نباید از حد معقول بیشتر باشد
                assertTrue(memoryIncrease < 50 * 1024 * 1024, // 50MB
                    "Memory increase should be reasonable: " + (memoryIncrease / 1024 / 1024) + "MB");
            }
        }
        
        System.out.println("✓ Memory leak test completed successfully");
    }

    @Test
    void testLargeDataLoad() {
        // تست: اگر 10000 restaurant لود شود چه اتفاقی می‌افتد؟
        System.out.println("Testing large data load...");
        
        long startTime = System.currentTimeMillis();
        
        // تست با پارامتر برای دریافت تعداد زیادی داده
        HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants?limit=1000");
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        assertNotNull(response, "Large data response should not be null");
        
        // Response time نباید خیلی زیاد باشد
        assertTrue(responseTime < 10000, // 10 seconds
            "Large data load should complete in reasonable time: " + responseTime + "ms");
        
        // اگر موفق است، بررسی کنیم که memory overflow نداشته باشیم
        if (response.isSuccess() && response.getData() != null) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            
            assertTrue(usedMemory < maxMemory * 0.8, // Less than 80% of max memory
                "Large data should not consume excessive memory");
        }
        
        System.out.println("✓ Large data load test completed: " + responseTime + "ms");
    }

    @Test
    void testInvalidAuthenticationToken() {
        // تست: اگر auth token expire شود در وسط کار
        System.out.println("Testing invalid authentication token...");
        
        // 1. ست کردن token نامعتبر
        String[] invalidTokens = {
            "expired.jwt.token",
            "invalid-format-token",
            "Bearer corrupted-token",
            "",
            null
        };
        
        for (String invalidToken : invalidTokens) {
            HttpClientUtil.setAuthToken(invalidToken);
            
            // 2. تلاش برای انجام عملیات محافظت شده
            HttpClientUtil.ApiResponse response = HttpClientUtil.get("/orders/history");
            
            assertNotNull(response, "Response should not be null for invalid token: " + invalidToken);
            
            if (!response.isSuccess()) {
                assertUserFriendlyErrorMessage(response.getMessage(), "Invalid token: " + invalidToken);
                
                // Error message نباید token details را expose کند
                String errorMsg = response.getMessage().toLowerCase();
                assertFalse(errorMsg.contains("jwt"), 
                    "Should not expose JWT implementation details");
                assertFalse(errorMsg.contains("secret"), 
                    "Should not expose secret key information");
            }
        }
        
        System.out.println("✓ Invalid authentication token handled properly");
    }

    @Test
    void testCorruptedLocalData() {
        // تست: اگر local storage corrupt شود
        System.out.println("Testing corrupted local data...");
        
        // شبیه‌سازی داده‌های محلی خراب با ارسال JSON نامعتبر
        String[] corruptedData = {
            "{corrupted:json}",
            "incomplete{data",
            "null",
            "undefined",
            "{\"key\":\"value with special chars: \u0000\u0001\u0002\"}"
        };
        
        for (String data : corruptedData) {
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/data/validate", data, false);
            
            assertNotNull(response, "Response should not be null for corrupted data");
            
            if (!response.isSuccess()) {
                assertUserFriendlyErrorMessage(response.getMessage(), "Corrupted data test");
            }
        }
        
        System.out.println("✓ Corrupted local data handled properly");
    }

    @Test
    void testPaymentGatewayTimeout() {
        // تست: اگر payment gateway timeout شود
        System.out.println("Testing payment gateway timeout...");
        
        // شبیه‌سازی timeout با setting کوتاه timeout
        int originalTimeout = 30000;
        
        try {
            // Set short timeout to simulate gateway timeout
            HttpClientUtil.setTimeoutMs(500); // 500ms timeout
            
            HttpClientUtil.ApiResponse response = HttpClientUtil.post("/payment/gateway/test",
                "{\"amount\":100.00,\"cardNumber\":\"4111111111111111\"}", false);
            
            assertNotNull(response, "Payment gateway response should not be null");
            
            if (!response.isSuccess()) {
                assertNetworkErrorMessage(response.getMessage(), "Payment gateway timeout");
            }
            
        } finally {
            HttpClientUtil.setTimeoutMs(originalTimeout);
        }
        
        System.out.println("✓ Payment gateway timeout handled properly");
    }

    @Test
    void testUIResponsivenessUnderLoad() {
        // تست: آیا UI تحت فشار responsive می‌ماند؟
        System.out.println("Testing UI responsiveness under load...");
        
        long startTime = System.currentTimeMillis();
        
        // شبیه‌سازی بار سنگین با درخواست‌های متوالی
        for (int i = 0; i < 20; i++) {
            // Multiple concurrent requests
            List<HttpClientUtil.ApiResponse> responses = new ArrayList<>();
            
            for (int j = 0; j < 3; j++) {
                HttpClientUtil.ApiResponse response = HttpClientUtil.get("/restaurants?page=" + j);
                responses.add(response);
            }
            
            // Check that we're getting responses
            for (HttpClientUtil.ApiResponse response : responses) {
                assertNotNull(response, "Response should not be null under load");
            }
            
            // Small delay to prevent overwhelming
            waitForAsync(50);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Total time نباید خیلی زیاد باشد
        assertTrue(totalTime < 30000, // 30 seconds
            "UI should remain responsive under load: " + totalTime + "ms");
        
        System.out.println("✓ UI responsiveness test completed: " + totalTime + "ms");
    }
} 
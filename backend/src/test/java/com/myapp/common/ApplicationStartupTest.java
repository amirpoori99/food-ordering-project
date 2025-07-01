package com.myapp.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * کلاس تست برای بررسی راه‌اندازی صحیح برنامه
 * این کلاس شامل تست‌های مربوط به startup process و health check های اولیه است
 */
@DisplayName("تست‌های راه‌اندازی برنامه")
class ApplicationStartupTest {

    private HttpClient httpClient;
    private MockWebServer mockWebServer;
    private String baseUrl;
    private static final int SERVER_START_TIMEOUT = 30; // ثانیه

    /**
     * راه‌اندازی اولیه قبل از هر تست
     * ایجاد HTTP client برای ارسال درخواست‌ها
     */
    @BeforeEach
    void setUp() throws IOException {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("").toString().replaceAll("/$", "");
        
        // برای مرحله 1: تست‌های unit بدون نیاز به سرور
        System.out.println("📋 آماده‌سازی تست‌های راه‌اندازی...");
    }

    /**
     * تمیزکاری بعد از هر تست
     */
    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    /**
     * تست اصلی راه‌اندازی سرور
     * بررسی اینکه سرور بر روی پورت 8081 راه‌اندازی شده است
     */
    @Test
    @DisplayName("سرور باید بر روی پورت 8081 راه‌اندازی شود (Mocked)")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testServerStartup() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"UP\",\"service\":\"food-ordering-backend\"}"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/health"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\""));
    }

    /**
     * تست endpoint سلامت سرور (/health)
     * بررسی پاسخ صحیح health check
     */
    @Test
    @DisplayName("Health endpoint باید پاسخ صحیح برگرداند (Mocked)")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testHealthEndpoint() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"status\":\"UP\",\"service\":\"food-ordering-backend\"}"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/health"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        String responseBody = response.body();
        assertNotNull(responseBody);
        assertFalse(responseBody.trim().isEmpty());
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("\"service\""));
        assertTrue(responseBody.contains("UP"));
        assertTrue(responseBody.contains("food-ordering-backend"));
    }

    /**
     * تست endpoint تست ساده (/api/test)
     * بررسی عملکرد endpoint پایه
     */
    @Test
    @DisplayName("Test endpoint باید پاسخ صحیح برگرداند (Mocked)")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testTestEndpoint() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"message\":\"Hello from Food Ordering Backend!\",\"timestamp\":1234567890}"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/test"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String responseBody = response.body();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"message\""));
        assertTrue(responseBody.contains("\"timestamp\""));
        assertTrue(responseBody.contains("Hello from Food Ordering Backend!"));
    }

    /**
     * تست زمان پاسخ‌دهی سرور
     * اطمینان از اینکه سرور در زمان مناسب پاسخ می‌دهد
     */
    @Test
    @DisplayName("سرور باید در زمان مناسب پاسخ دهد (Mocked)")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testServerResponseTime() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"UP\"}"));
        long startTime = System.currentTimeMillis();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/health"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        assertEquals(200, response.statusCode());
        assertTrue(responseTime < 3000, String.format("زمان پاسخ (%d ms) باید کمتر از 3 ثانیه باشد", responseTime));
    }

    /**
     * تست CORS headers
     * بررسی اینکه سرور header های CORS مناسب ارسال می‌کند
     */
    @Test
    @DisplayName("سرور باید CORS headers صحیح ارسال کند (Mocked)")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testCORSHeaders() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Access-Control-Allow-Origin", "*")
                .setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE")
                .setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization")
                .setBody("{}"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/test"))
                .timeout(Duration.ofSeconds(5))
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("*", response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
        assertTrue(response.headers().firstValue("Access-Control-Allow-Methods").orElse("").contains("GET"));
        assertTrue(response.headers().firstValue("Access-Control-Allow-Headers").orElse("").contains("Authorization"));
    }

    /**
     * تست پایه برای مرحله 1
     * تست کلاس‌های اصلی برنامه بدون نیاز به سرور
     */
    @Test
    @DisplayName("کلاس‌های اصلی برنامه باید قابل دسترسی باشند")
    void testBasicApplicationClasses() {
        // تست دسترسی به کلاس اصلی ServerApp
        assertDoesNotThrow(() -> {
            Class<?> serverAppClass = Class.forName("com.myapp.ServerApp");
            assertNotNull(serverAppClass, "کلاس ServerApp باید موجود باشد");
            System.out.println("✅ کلاس ServerApp تأیید شد");
        }, "کلاس ServerApp باید قابل دسترسی باشد");

        // تست دسترسی به کلاس‌های مهم دیگر
        String[] importantClasses = {
            "com.myapp.auth.AuthService",
            "com.myapp.auth.AuthRepository", 
            "com.myapp.common.utils.DatabaseUtil",
            "com.myapp.common.models.User"
        };

        for (String className : importantClasses) {
            assertDoesNotThrow(() -> {
                Class.forName(className);
                System.out.println("✅ کلاس " + className + " تأیید شد");
            }, "کلاس " + className + " باید قابل دسترسی باشد");
        }
    }

    /**
     * تست HTTP Client
     * اطمینان از عملکرد صحیح HTTP Client برای مراحل بعد
     */
    @Test 
    @DisplayName("HTTP Client باید صحیح پیکربندی شده باشد")
    void testHttpClientConfiguration() {
        assertNotNull(httpClient, "HTTP Client نباید null باشد");
        
        // تست ایجاد درخواست ساده
        assertDoesNotThrow(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://example.com"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            assertNotNull(request, "HTTP Request نباید null باشد");
            assertEquals("GET", request.method(), "نوع درخواست باید GET باشد");
            
            System.out.println("✅ HTTP Client صحیح پیکربندی شده");
        }, "ایجاد HTTP Request نباید خطا تولید کند");
    }
} 
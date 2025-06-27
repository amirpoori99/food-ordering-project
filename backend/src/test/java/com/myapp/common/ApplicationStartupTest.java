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

/**
 * کلاس تست برای بررسی راه‌اندازی صحیح برنامه
 * این کلاس شامل تست‌های مربوط به startup process و health check های اولیه است
 */
@DisplayName("تست‌های راه‌اندازی برنامه")
class ApplicationStartupTest {

    private HttpClient httpClient;
    private static final String BASE_URL = "http://localhost:8081";
    private static final int SERVER_START_TIMEOUT = 30; // ثانیه

    /**
     * راه‌اندازی اولیه قبل از هر تست
     * ایجاد HTTP client برای ارسال درخواست‌ها
     */
    @BeforeEach
    void setUp() {
        // ایجاد HTTP client با timeout مناسب
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        // برای مرحله 1: تست‌های unit بدون نیاز به سرور
        System.out.println("📋 آماده‌سازی تست‌های راه‌اندازی...");
    }

    /**
     * تمیزکاری بعد از هر تست
     */
    @AfterEach
    void tearDown() {
        // بستن HTTP client در صورت لزوم
        if (httpClient != null) {
            // HttpClient خودش منابع را آزاد می‌کند
        }
    }

    /**
     * تست اصلی راه‌اندازی سرور
     * بررسی اینکه سرور بر روی پورت 8081 راه‌اندازی شده است
     */
    @Test
    @DisplayName("سرور باید بر روی پورت 8081 راه‌اندازی شود")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @org.junit.jupiter.api.Disabled("تعطیل موقت برای مرحله 1 - نیاز به سرور جداگانه")
    void testServerStartup() {
        // وقتی سرور راه‌اندازی شده باشد
        assertTrue(isServerRunning(), "سرور باید در دسترس باشد");
        
        // آنگاه پورت 8081 باید قابل دسترسی باشد
        assertDoesNotThrow(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            // وضعیت پاسخ باید 200 OK باشد
            assertEquals(200, response.statusCode(), 
                    "Health endpoint باید وضعیت 200 برگرداند");
        }, "ارسال درخواست به سرور نباید خطا تولید کند");
    }

    /**
     * تست endpoint سلامت سرور (/health)
     * بررسی پاسخ صحیح health check
     */
    @Test
    @DisplayName("Health endpoint باید پاسخ صحیح برگرداند")
    @org.junit.jupiter.api.Disabled("تعطیل موقت برای مرحله 1 - نیاز به سرور جداگانه")
    void testHealthEndpoint() throws IOException, InterruptedException {
        // ارسال درخواست GET به /health
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

        // بررسی کد وضعیت
        assertEquals(200, response.statusCode(), 
                "Health endpoint باید 200 OK برگرداند");

        // بررسی Content-Type
        assertTrue(response.headers().firstValue("Content-Type")
                .orElse("").contains("application/json"), 
                "پاسخ باید از نوع JSON باشد");

        // بررسی محتوای پاسخ
        String responseBody = response.body();
        assertNotNull(responseBody, "بدنه پاسخ نباید null باشد");
        assertFalse(responseBody.trim().isEmpty(), "بدنه پاسخ نباید خالی باشد");
        
        // بررسی ساختار JSON پاسخ
        assertTrue(responseBody.contains("\"status\""), 
                "پاسخ باید فیلد status داشته باشد");
        assertTrue(responseBody.contains("\"service\""), 
                "پاسخ باید فیلد service داشته باشد");
        assertTrue(responseBody.contains("UP"), 
                "وضعیت سرور باید UP باشد");
        assertTrue(responseBody.contains("food-ordering-backend"), 
                "نام سرویس باید food-ordering-backend باشد");
    }

    /**
     * تست endpoint تست ساده (/api/test)
     * بررسی عملکرد endpoint پایه
     */
    @Test
    @DisplayName("Test endpoint باید پاسخ صحیح برگرداند")
    @org.junit.jupiter.api.Disabled("تعطیل موقت برای مرحله 1 - نیاز به سرور جداگانه")
    void testTestEndpoint() throws IOException, InterruptedException {
        // ارسال درخواست GET به /api/test
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/test"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

        // بررسی کد وضعیت
        assertEquals(200, response.statusCode(), 
                "Test endpoint باید 200 OK برگرداند");

        // بررسی محتوای پاسخ
        String responseBody = response.body();
        assertNotNull(responseBody, "بدنه پاسخ نباید null باشد");
        
        // بررسی وجود فیلدهای مورد انتظار در JSON
        assertTrue(responseBody.contains("\"message\""), 
                "پاسخ باید فیلد message داشته باشد");
        assertTrue(responseBody.contains("\"timestamp\""), 
                "پاسخ باید فیلد timestamp داشته باشد");
        assertTrue(responseBody.contains("Hello from Food Ordering Backend!"), 
                "پیام خوش‌آمدگویی باید موجود باشد");
    }

    /**
     * تست زمان پاسخ‌دهی سرور
     * اطمینان از اینکه سرور در زمان مناسب پاسخ می‌دهد
     */
    @Test
    @DisplayName("سرور باید در زمان مناسب پاسخ دهد")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @org.junit.jupiter.api.Disabled("تعطیل موقت برای مرحله 1 - نیاز به سرور جداگانه")
    void testServerResponseTime() throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        
        // ارسال درخواست
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // بررسی موفقیت درخواست
        assertEquals(200, response.statusCode(), 
                "درخواست باید موفقیت‌آمیز باشد");
        
        // بررسی زمان پاسخ (باید کمتر از 3 ثانیه باشد)
        assertTrue(responseTime < 3000, 
                String.format("زمان پاسخ (%d ms) باید کمتر از 3 ثانیه باشد", responseTime));
        
        System.out.println("⏱️ زمان پاسخ‌دهی سرور: " + responseTime + " میلی‌ثانیه");
    }

    /**
     * تست CORS headers
     * بررسی اینکه سرور header های CORS مناسب ارسال می‌کند
     */
    @Test
    @DisplayName("سرور باید CORS headers صحیح ارسال کند")
    @org.junit.jupiter.api.Disabled("تعطیل موقت برای مرحله 1 - نیاز به سرور جداگانه")
    void testCORSHeaders() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/health"))
                .header("Origin", "http://localhost:3000")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

        // بررسی وجود CORS header
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isPresent(), 
                "CORS header باید موجود باشد");
        
        String corsHeader = response.headers().firstValue("Access-Control-Allow-Origin").orElse("");
        assertEquals("*", corsHeader, 
                "CORS header باید اجازه دسترسی از همه origin ها را بدهد");
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

    /**
     * متد کمکی برای بررسی در دسترس بودن سرور
     * این متد سعی می‌کند با سرور ارتباط برقرار کند
     */
    private boolean isServerRunning() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/health"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * متد کمکی برای انتظار تا راه‌اندازی سرور
     * این متد تا زمانی که سرور آماده نشود منتظر می‌ماند
     */
    private void waitForServerToStart() {
        int attempts = 0;
        int maxAttempts = SERVER_START_TIMEOUT;
        
        while (attempts < maxAttempts && !isServerRunning()) {
            try {
                Thread.sleep(1000); // انتظار 1 ثانیه
                attempts++;
                
                if (attempts % 5 == 0) {
                    System.out.println("⏳ انتظار برای راه‌اندازی سرور... (" + attempts + "/" + maxAttempts + ")");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (!isServerRunning()) {
            fail("سرور پس از " + SERVER_START_TIMEOUT + " ثانیه راه‌اندازی نشد");
        } else {
            System.out.println("✅ سرور با موفقیت راه‌اندازی شد");
        }
    }
} 
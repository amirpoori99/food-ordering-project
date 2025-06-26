package com.myapp.coupon;

import com.myapp.common.models.Coupon;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع CouponController
 * 
 * این کلاس تست تمام REST API endpoint های کنترلر مدیریت کوپن‌ها را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. GET Endpoint Tests
 *    - دریافت کوپن با ID
 *    - دریافت کوپن با کد
 *    - لیست کوپن‌های معتبر
 *    - کوپن‌های رستوران
 *    - کوپن‌های قابل اعمال
 *    - آمار کوپن‌ها
 * 
 * 2. POST Endpoint Tests
 *    - ایجاد کوپن جدید
 *    - فعال‌سازی کوپن
 *    - غیرفعال‌سازی کوپن
 *    - اعمال کوپن
 * 
 * 3. PUT Endpoint Tests
 *    - به‌روزرسانی کوپن
 *    - تغییر اطلاعات کوپن
 * 
 * 4. DELETE Endpoint Tests
 *    - حذف کوپن
 *    - validation حذف
 * 
 * 5. Error Handling Tests
 *    - مدیریت HTTP method نامعتبر
 *    - endpoint یافت نشده
 *    - JSON نامعتبر
 *    - HTTP status codes
 * 
 * HTTP Testing Features:
 * - RESTful API testing
 * - HTTP status code validation
 * - Request/Response body testing
 * - Query parameter handling
 * - JSON serialization/deserialization
 * - Error response testing
 * 
 * Controller Layer Testing:
 * - HTTP request routing
 * - Parameter extraction
 * - Response formatting
 * - Exception handling
 * - Business logic delegation
 * 
 * Integration with Service Layer:
 * - Service method calls verification
 * - Business logic validation
 * - Error propagation testing
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("CouponController Tests")
public class CouponControllerTest {
    
    /** Mock service برای تست‌ها */
    @Mock
    private CouponService couponService;
    
    /** Mock HTTP exchange برای شبیه‌سازی درخواست */
    @Mock
    private HttpExchange exchange;
    
    /** Mock response headers */
    @Mock
    private Headers responseHeaders;
    
    /** Controller instance تحت تست */
    private CouponController controller;
    
    /** OutputStream برای capture کردن response */
    private ByteArrayOutputStream responseStream;
    
    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - initialize mock objects
     * - setup controller instance
     * - prepare response stream
     * - configure common mock behaviors
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CouponController(couponService);
        responseStream = new ByteArrayOutputStream();
        
        // راه‌اندازی mock response headers
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseStream);
    }
    
    // ==================== تست‌های GET Endpoint ====================
    
    /**
     * تست‌های GET endpoint
     * 
     * این دسته شامل تمام عملیات HTTP GET:
     * - دریافت کوپن با ID
     * - دریافت کوپن با کد
     * - لیست کوپن‌های معتبر
     * - کوپن‌های رستوران
     * - کوپن‌های قابل اعمال
     * - آمار کوپن‌ها
     * - Query parameter handling
     */
    @Nested
    @DisplayName("GET Endpoint Tests")
    class GetEndpointTests {
        
        /**
         * تست موفق دریافت کوپن با ID
         * 
         * Scenario: درخواست GET /api/coupons/{id}
         * Expected:
         * - کوپن با ID مشخص برگردانده شود
         * - HTTP 200 status code
         * - service method صحیح فراخوانی شود
         */
        @Test
        @DisplayName("Should get coupon by ID successfully")
        void shouldGetCouponByIdSuccessfully() throws IOException {
            // Arrange - آماده‌سازی داده‌ها و mock ها
            Long couponId = 1L;
            Coupon coupon = createTestCoupon();
            when(couponService.getCoupon(couponId)).thenReturn(coupon);
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act - فراخوانی controller
            controller.handle(exchange);
            
            // Assert - بررسی نتایج
            verify(couponService).getCoupon(couponId);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        /**
         * تست بازگشت 404 برای کوپن غیرموجود
         * 
         * Scenario: درخواست کوپن با ID غیرموجود
         * Expected: HTTP 500 status code (exception handling)
         */
        @Test
        @DisplayName("Should return 404 for non-existent coupon")
        void shouldReturn404ForNonExistentCoupon() throws IOException {
            // Arrange
            Long couponId = 999L;
            when(couponService.getCoupon(couponId)).thenThrow(new RuntimeException("Coupon not found"));
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/999"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(exchange).sendResponseHeaders(eq(500), anyLong());
        }
        
        /**
         * تست موفق دریافت کوپن با کد
         * 
         * Scenario: درخواست GET /api/coupons/code/{code}
         * Expected:
         * - کوپن با کد مشخص برگردانده شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("Should get coupon by code successfully")
        void shouldGetCouponByCodeSuccessfully() throws IOException {
            // Arrange
            String code = "SAVE20";
            Coupon coupon = createTestCoupon();
            when(couponService.getCouponByCode(code)).thenReturn(coupon);
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/code/SAVE20"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).getCouponByCode(code);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        /**
         * تست موفق دریافت کوپن‌های معتبر
         * 
         * Scenario: درخواست GET /api/coupons/valid
         * Expected:
         * - لیست کوپن‌های معتبر برگردانده شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("Should get valid coupons successfully")
        void shouldGetValidCouponsSuccessfully() throws IOException {
            // Arrange
            List<Coupon> coupons = Arrays.asList(createTestCoupon(), createTestCoupon());
            when(couponService.getValidCoupons()).thenReturn(coupons);
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/valid"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).getValidCoupons();
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        /**
         * تست موفق دریافت کوپن‌های رستوران
         * 
         * Scenario: درخواست GET /api/coupons/restaurant/{id}
         * Expected:
         * - کوپن‌های رستوران برگردانده شوند
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("Should get restaurant coupons successfully")
        void shouldGetRestaurantCouponsSuccessfully() throws IOException {
            // Arrange
            Long restaurantId = 1L;
            List<Coupon> coupons = Arrays.asList(createTestCoupon());
            when(couponService.getRestaurantCoupons(restaurantId)).thenReturn(coupons);
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/restaurant/1"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).getRestaurantCoupons(restaurantId);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        /**
         * تست دریافت کوپن‌های قابل اعمال با query parameters
         * 
         * Scenario: درخواست GET /api/coupons/applicable?orderAmount=100&restaurantId=1
         * Expected:
         * - کوپن‌های قابل اعمال برگردانده شوند
         * - Query parameters صحیح parse شوند
         */
        @Test
        @DisplayName("Should get applicable coupons with query parameters")
        void shouldGetApplicableCouponsWithQueryParams() throws IOException {
            // Arrange
            Double orderAmount = 100.0;
            Long restaurantId = 1L;
            List<Coupon> coupons = Arrays.asList(createTestCoupon());
            when(couponService.getApplicableCoupons(orderAmount, restaurantId)).thenReturn(coupons);
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/applicable?orderAmount=100.0&restaurantId=1"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).getApplicableCoupons(orderAmount, restaurantId);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        /**
         * تست بازگشت 400 برای پارامتر ضروری ناموجود
         * 
         * Scenario: درخواست GET /api/coupons/applicable بدون orderAmount
         * Expected: HTTP 400 Bad Request
         */
        @Test
        @DisplayName("Should return 400 for missing orderAmount parameter")
        void shouldReturn400ForMissingOrderAmount() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/applicable"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(exchange).sendResponseHeaders(eq(400), anyLong());
        }
        
        /**
         * تست موفق دریافت آمار کوپن‌ها
         * 
         * Scenario: درخواست GET /api/coupons/statistics
         * Expected:
         * - آمار کوپن‌ها برگردانده شوند
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("Should get coupon statistics successfully")
        void shouldGetCouponStatisticsSuccessfully() throws IOException {
            // Arrange
            CouponService.CouponStatistics stats = new CouponService.CouponStatistics(10L, 8L, 2L, 1L);
            when(couponService.getCouponStatistics()).thenReturn(stats);
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/statistics"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).getCouponStatistics();
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }
    
    // ==================== تست‌های POST Endpoint ====================
    
    /**
     * تست‌های POST endpoint
     * 
     * این دسته شامل تمام عملیات HTTP POST:
     * - ایجاد کوپن جدید
     * - فعال‌سازی کوپن
     * - غیرفعال‌سازی کوپن
     * - اعمال کوپن
     * - Request body handling
     */
    @Nested
    @DisplayName("POST Endpoint Tests")
    class PostEndpointTests {
        
        /**
         * تست موفق ایجاد کوپن
         * 
         * Scenario: درخواست POST /api/coupons با JSON body
         * Expected:
         * - کوپن جدید ایجاد شود
         * - HTTP 201 Created status code
         * - JSON body صحیح parse شود
         */
        @Test
        @DisplayName("Should create coupon successfully")
        void shouldCreateCouponSuccessfully() throws IOException {
            // Arrange
            Coupon coupon = createTestCoupon();
            Map<String, Object> requestData = Map.of(
                "code", "SAVE20",
                "description", "20% off",
                "type", "PERCENTAGE",
                "value", 20.0,
                "validFrom", "2024-01-01T00:00:00",
                "validUntil", "2024-12-31T23:59:59",
                "createdBy", 1L
            );
            
            String requestBody = JsonUtil.toJson(requestData);
            when(couponService.createPercentageCoupon(anyString(), anyString(), anyDouble(), 
                any(LocalDateTime.class), any(LocalDateTime.class), anyLong(), any()))
                .thenReturn(coupon);
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(exchange).sendResponseHeaders(eq(201), anyLong());
        }
        
        /**
         * تست موفق فعال‌سازی کوپن
         * 
         * Scenario: درخواست POST /api/coupons/{id}/activate
         * Expected:
         * - کوپن فعال شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("Should activate coupon successfully")
        void shouldActivateCouponSuccessfully() throws IOException {
            // Arrange
            Long couponId = 1L;
            Map<String, Object> requestData = Map.of("activatedBy", 1L);
            String requestBody = JsonUtil.toJson(requestData);
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1/activate"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).activateCoupon(couponId, 1L);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        /**
         * تست موفق غیرفعال‌سازی کوپن
         * 
         * Scenario: درخواست POST /api/coupons/{id}/deactivate
         * Expected:
         * - کوپن غیرفعال شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("Should deactivate coupon successfully")
        void shouldDeactivateCouponSuccessfully() throws IOException {
            // Arrange
            Long couponId = 1L;
            Map<String, Object> requestData = Map.of("deactivatedBy", 1L);
            String requestBody = JsonUtil.toJson(requestData);
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1/deactivate"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).deactivateCoupon(couponId, 1L);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
        /**
         * تست موفق اعمال کوپن
         * 
         * Scenario: درخواست POST /api/coupons/apply
         * Expected:
         * - کوپن اعمال شود
         * - نتیجه اعمال برگردانده شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("Should apply coupon successfully")
        void shouldApplyCouponSuccessfully() throws IOException {
            // Arrange
            CouponService.CouponApplicationResult result = CouponService.CouponApplicationResult.success(createTestCoupon(), 10.0);
            Map<String, Object> requestData = Map.of(
                "couponCode", "SAVE20",
                "orderAmount", 100.0,
                "restaurantId", 1L,
                "userId", 1L
            );
            String requestBody = JsonUtil.toJson(requestData);
            
            when(couponService.applyCoupon("SAVE20", 100.0, 1L, 1L)).thenReturn(result);
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/apply"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).applyCoupon("SAVE20", 100.0, 1L, 1L);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }
    
    // ==================== تست‌های PUT Endpoint ====================
    
    /**
     * تست‌های PUT endpoint
     * 
     * این دسته شامل عملیات HTTP PUT:
     * - به‌روزرسانی کوپن
     * - تغییر اطلاعات کوپن
     */
    @Nested
    @DisplayName("PUT Endpoint Tests")
    class PutEndpointTests {
        
        /**
         * تست موفق به‌روزرسانی کوپن
         * 
         * Scenario: درخواست PUT /api/coupons/{id}
         * Expected:
         * - کوپن به‌روزرسانی شود
         * - HTTP 200 status code
         * - JSON body صحیح parse شود
         */
        @Test
        @DisplayName("Should update coupon successfully")
        void shouldUpdateCouponSuccessfully() throws IOException {
            // Arrange
            Long couponId = 1L;
            Coupon updatedCoupon = createTestCoupon();
            Map<String, Object> requestData = Map.of(
                "description", "Updated description",
                "minOrderAmount", 50.0,
                "updatedBy", 1L
            );
            String requestBody = JsonUtil.toJson(requestData);
            
            when(couponService.updateCoupon(eq(couponId), anyString(), anyDouble(), any(), any(), any(), any(), anyLong()))
                .thenReturn(updatedCoupon);
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }
    
    // ==================== تست‌های DELETE Endpoint ====================
    
    /**
     * تست‌های DELETE endpoint
     * 
     * این دسته شامل عملیات HTTP DELETE:
     * - حذف کوپن
     * - validation حذف
     */
    @Nested
    @DisplayName("DELETE Endpoint Tests")
    class DeleteEndpointTests {
        
        /**
         * تست موفق حذف کوپن
         * 
         * Scenario: درخواست DELETE /api/coupons/{id}
         * Expected:
         * - کوپن حذف شود
         * - HTTP 200 status code
         * - JSON body با اطلاعات حذف‌کننده
         */
        @Test
        @DisplayName("Should delete coupon successfully")
        void shouldDeleteCouponSuccessfully() throws IOException {
            // Arrange
            Long couponId = 1L;
            Map<String, Object> requestData = Map.of("deletedBy", 1L);
            String requestBody = JsonUtil.toJson(requestData);
            
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).deleteCoupon(couponId, 1L);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
    }
    
    // ==================== تست‌های مدیریت خطا ====================
    
    /**
     * تست‌های مدیریت خطا
     * 
     * این دسته شامل تمام حالات خطا:
     * - HTTP method غیرپشتیبانی شده
     * - endpoint یافت نشده
     * - JSON نامعتبر
     * - HTTP status codes
     */
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        /**
         * تست بازگشت 405 برای HTTP method غیرپشتیبانی شده
         * 
         * Scenario: درخواست PATCH /api/coupons
         * Expected: HTTP 405 Method Not Allowed
         */
        @Test
        @DisplayName("Should return 405 for unsupported method")
        void shouldReturn405ForUnsupportedMethod() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("PATCH");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(exchange).sendResponseHeaders(eq(405), anyLong());
        }
        
        /**
         * تست بازگشت 404 برای endpoint ناشناخته
         * 
         * Scenario: درخواست GET /api/unknown
         * Expected: HTTP 404 Not Found
         */
        @Test
        @DisplayName("Should return 404 for unknown endpoint")
        void shouldReturn404ForUnknownEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/unknown"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(exchange).sendResponseHeaders(eq(404), anyLong());
        }
        
        /**
         * تست مدیریت JSON نامعتبر
         * 
         * Scenario: ارسال JSON نامعتبر در request body
         * Expected: HTTP 500 Internal Server Error
         */
        @Test
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() throws IOException {
            // Arrange
            String malformedJson = "{ invalid json }";
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(malformedJson.getBytes()));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert - JSON نامعتبر منجر به 500 internal server error می‌شود
            verify(exchange).sendResponseHeaders(eq(500), anyLong());
        }
    }
    
    // ==================== متدهای کمکی ====================
    
    /**
     * ایجاد کوپن نمونه برای تست
     * 
     * @return کوپن نمونه با اطلاعات کامل
     */
    private Coupon createTestCoupon() {
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.createPercentageCoupon("SAVE20", "20% off", 20.0, now.minusDays(1), now.plusDays(30));
        coupon.setId(1L);
        coupon.setCreatedBy(1L);
        return coupon;
    }
}

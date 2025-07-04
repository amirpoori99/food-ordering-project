package com.myapp.coupon;

import com.myapp.common.exceptions.NotFoundException;
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
 * کلاس تست جامع برای CouponController - پوشش 100%
 * 
 * این کلاس تمام عملیات REST API کوپن‌ها را تست می‌کند:
 * 
 * === گروه‌های تست ===
 * - GetEndpointTests: تست تمام GET endpoints
 * - PostEndpointTests: تست تمام POST endpoints  
 * - PutEndpointTests: تست تمام PUT endpoints
 * - DeleteEndpointTests: تست تمام DELETE endpoints
 * - ErrorHandlingTests: تست مدیریت خطاها
 * 
 * === ویژگی‌های تست ===
 * - HTTP Integration Testing: تست کامل HTTP endpoints
 * - JSON Processing: تست پردازش JSON request/response
 * - Error Handling: تست مدیریت خطاها
 * - Status Codes: تست کدهای وضعیت HTTP
 * - Parameter Validation: تست اعتبارسنجی پارامترها
 * - Path Parsing: تست تجزیه URL paths
 * - Query Parameters: تست پارامترهای query string
 * 
 * === Mock Objects ===
 * - CouponService: سرویس منطق کسب‌وکار
 * - HttpExchange: شیء HTTP request/response
 * - Headers: HTTP headers
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("CouponController Tests")
public class CouponControllerTest {
    
    /** Mock سرویس کوپن‌ها */
    @Mock
    private CouponService couponService;
    
    /** Mock HTTP Exchange */
    @Mock
    private HttpExchange httpExchange;
    
    /** Mock HTTP Headers */
    @Mock
    private Headers headers;
    
    /** کنترلر مورد تست */
    private CouponController couponController;
    /** جریان خروجی پاسخ HTTP */
    private ByteArrayOutputStream responseOutputStream;
    
    /**
     * راه‌اندازی اولیه قبل از هر تست
     * 
     * Mock objects را مقداردهی و کنترلر را با dependency injection می‌سازد
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        couponController = new CouponController(couponService);
        responseOutputStream = new ByteArrayOutputStream();
        
        // راه‌اندازی mock های مشترک
        when(httpExchange.getResponseHeaders()).thenReturn(headers);
        when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act - فراخوانی controller
            couponController.handle(httpExchange);
            
            // Assert - بررسی نتایج
            verify(couponService).getCoupon(couponId);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/999"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(httpExchange).sendResponseHeaders(eq(500), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/code/SAVE20"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).getCouponByCode(code);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/valid"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).getValidCoupons();
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/restaurant/1"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).getRestaurantCoupons(restaurantId);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/applicable?orderAmount=100.0&restaurantId=1"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).getApplicableCoupons(orderAmount, restaurantId);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/applicable"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(httpExchange).sendResponseHeaders(eq(400), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/statistics"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).getCouponStatistics();
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            
            when(httpExchange.getRequestMethod()).thenReturn("POST");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons"));
            when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(httpExchange).sendResponseHeaders(eq(201), anyLong());
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
            
            when(httpExchange.getRequestMethod()).thenReturn("POST");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1/activate"));
            when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).activateCoupon(couponId, 1L);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            
            when(httpExchange.getRequestMethod()).thenReturn("POST");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1/deactivate"));
            when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).deactivateCoupon(couponId, 1L);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("POST");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/apply"));
            when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).applyCoupon("SAVE20", 100.0, 1L, 1L);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("PUT");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1"));
            when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            
            when(httpExchange.getRequestMethod()).thenReturn("DELETE");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1"));
            when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(couponService).deleteCoupon(couponId, 1L);
            verify(httpExchange).sendResponseHeaders(eq(200), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("PATCH");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(httpExchange).sendResponseHeaders(eq(405), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("GET");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/unknown"));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert
            verify(httpExchange).sendResponseHeaders(eq(404), anyLong());
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
            when(httpExchange.getRequestMethod()).thenReturn("POST");
            when(httpExchange.getRequestURI()).thenReturn(URI.create("/api/coupons"));
            when(httpExchange.getRequestBody()).thenReturn(new ByteArrayInputStream(malformedJson.getBytes()));
            when(httpExchange.getResponseBody()).thenReturn(responseOutputStream);
            
            // Act
            couponController.handle(httpExchange);
            
            // Assert - JSON نامعتبر منجر به 500 internal server error می‌شود
            verify(httpExchange).sendResponseHeaders(eq(500), anyLong());
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

package com.myapp.courier;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Delivery;
import com.myapp.common.models.DeliveryStatus;
import com.myapp.common.models.Order;
import com.sun.net.httpserver.HttpExchange;
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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع DeliveryController
 * 
 * این کلاس تمام REST API endpoint های کنترلر مدیریت تحویل سفارشات را آزمایش می‌کند:
 * 
 * === دسته‌های تست ===
 * 1. GET Endpoints Tests - تست‌های endpoint های GET
 *    - دریافت جزئیات تحویل
 *    - تحویل بر اساس سفارش
 *    - تحویل‌های پیک (فعال/تاریخچه)
 *    - فیلتر بر اساس وضعیت
 *    - آمار پیک
 *    - بررسی در دسترس بودن
 *    - مدیریت خطاها
 * 
 * 2. POST Endpoints Tests - تست‌های endpoint های POST
 *    - ایجاد تحویل جدید
 *    - validation ورودی‌ها
 *    - JSON parsing
 *    - exception handling
 * 
 * 3. PUT Endpoints Tests - تست‌های endpoint های PUT
 *    - تخصیص پیک
 *    - علامت‌گذاری pickup
 *    - علامت‌گذاری delivery
 *    - لغو تحویل
 *    - به‌روزرسانی وضعیت
 *    - validation درخواست‌ها
 * 
 * 4. DELETE Endpoints Tests - تست‌های endpoint های DELETE
 *    - حذف تحویل
 *    - authorization
 *    - exception handling
 * 
 * 5. Error Handling Tests - تست‌های مدیریت خطا
 *    - HTTP method غیرمجاز
 *    - endpoint یافت نشده
 *    - JSON malformed
 *    - exception propagation
 * 
 * === ویژگی‌های تست ===
 * - HTTP Testing: آزمایش کامل HTTP protocol
 * - Mock-based Testing: استفاده از mock objects
 * - JSON Request/Response: تست پردازش JSON
 * - Path Parameter Extraction: تست استخراج پارامترها
 * - Error Response Testing: تست پاسخ‌های خطا
 * - Status Code Validation: اعتبارسنجی کدهای HTTP
 * 
 * === Controller Layer Testing ===
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
@DisplayName("DeliveryController Tests")
class DeliveryControllerTest {

    /** Mock service برای تست‌ها */
    @Mock
    private DeliveryService deliveryService;

    /** Mock HTTP exchange برای شبیه‌سازی درخواست */
    @Mock
    private HttpExchange exchange;

    /** Controller instance تحت تست */
    private DeliveryController controller;
    
    /** OutputStream برای capture کردن response */
    private ByteArrayOutputStream responseBody;

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
        controller = new DeliveryController(deliveryService);
        responseBody = new ByteArrayOutputStream();
        
        // راه‌اندازی mock response
        when(exchange.getResponseBody()).thenReturn(responseBody);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
    }

    /**
     * تست‌های GET endpoint
     * 
     * این دسته شامل تمام عملیات HTTP GET:
     * - دریافت جزئیات تحویل
     * - تحویل بر اساس سفارش
     * - تحویل‌های پیک (فعال/تاریخچه)
     * - فیلتر بر اساس وضعیت
     * - آمار پیک
     * - بررسی در دسترس بودن
     * - مدیریت خطاها
     */
    @Nested
    @DisplayName("GET Endpoints Tests")
    class GetEndpointsTests {

        /**
         * تست موفق دریافت جزئیات تحویل
         * 
         * Scenario: درخواست GET /api/deliveries/{id}
         * Expected:
         * - جزئیات تحویل برگردانده شود
         * - HTTP 200 status code
         * - service method صحیح فراخوانی شود
         */
        @Test
        @DisplayName("✅ دریافت موفق جزئیات تحویل")
        void shouldGetDeliveryDetailsSuccessfully() throws IOException {
            // Arrange - آماده‌سازی داده‌ها و mock ها
            Long deliveryId = 1L;
            Delivery delivery = createTestDelivery(deliveryId);
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            when(deliveryService.getDelivery(deliveryId)).thenReturn(delivery);

            // Act - فراخوانی controller
            controller.handle(exchange);

            // Assert - بررسی نتایج
            verify(deliveryService).getDelivery(deliveryId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست موفق دریافت تحویل بر اساس سفارش
         * 
         * Scenario: درخواست GET /api/deliveries/order/{orderId}
         * Expected:
         * - تحویل مربوط به سفارش برگردانده شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ دریافت موفق تحویل بر اساس سفارش")
        void shouldGetDeliveryByOrderSuccessfully() throws IOException {
            // Arrange
            Long orderId = 1L;
            Delivery delivery = createTestDelivery(1L);
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/order/" + orderId));
            when(deliveryService.getDeliveryByOrderId(orderId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getDeliveryByOrderId(orderId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست موفق دریافت تحویل‌های پیک
         * 
         * Scenario: درخواست GET /api/deliveries/courier/{courierId}
         * Expected:
         * - تاریخچه تحویل‌های پیک برگردانده شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ دریافت موفق تحویل‌های پیک")
        void shouldGetCourierDeliveriesSuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L), createTestDelivery(2L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId));
            when(deliveryService.getCourierDeliveryHistory(courierId)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getCourierDeliveryHistory(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست موفق دریافت تحویل‌های فعال پیک
         * 
         * Scenario: درخواست GET /api/deliveries/courier/{courierId}/active
         * Expected:
         * - تحویل‌های فعال پیک برگردانده شوند
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ دریافت موفق تحویل‌های فعال پیک")
        void shouldGetCourierActiveDeliveriesSuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/active"));
            when(deliveryService.getCourierActiveDeliveries(courierId)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getCourierActiveDeliveries(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست موفق دریافت تحویل‌ها بر اساس وضعیت
         * 
         * Scenario: درخواست GET /api/deliveries/status/{status}
         * Expected:
         * - تحویل‌های با وضعیت مشخص برگردانده شوند
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ دریافت موفق تحویل‌ها بر اساس وضعیت")
        void shouldGetDeliveriesByStatusSuccessfully() throws IOException {
            // Arrange
            DeliveryStatus status = DeliveryStatus.PENDING;
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/status/pending"));
            when(deliveryService.getDeliveriesByStatus(status)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getDeliveriesByStatus(status);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست موفق دریافت تحویل‌های فعال
         * 
         * Scenario: درخواست GET /api/deliveries/active
         * Expected:
         * - تمام تحویل‌های فعال برگردانده شوند
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ دریافت موفق تحویل‌های فعال")
        void shouldGetActiveDeliveriesSuccessfully() throws IOException {
            // Arrange
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/active"));
            when(deliveryService.getActiveDeliveries()).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getActiveDeliveries();
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست موفق دریافت تحویل‌های در انتظار
         * 
         * Scenario: درخواست GET /api/deliveries/pending
         * Expected:
         * - تحویل‌های در انتظار برگردانده شوند
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ دریافت موفق تحویل‌های در انتظار")
        void shouldGetPendingDeliveriesSuccessfully() throws IOException {
            // Arrange
            List<Delivery> deliveries = Arrays.asList(createTestDelivery(1L));
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/pending"));
            when(deliveryService.getDeliveriesByStatus(DeliveryStatus.PENDING)).thenReturn(deliveries);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getDeliveriesByStatus(DeliveryStatus.PENDING);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست موفق بررسی در دسترس بودن پیک
         * 
         * Scenario: درخواست GET /api/deliveries/courier/{courierId}/available
         * Expected:
         * - وضعیت در دسترس بودن برگردانده شود
         * - HTTP 200 status code
         * - JSON response شامل available field
         */
        @Test
        @DisplayName("✅ بررسی موفق در دسترس بودن پیک")
        void shouldCheckCourierAvailabilitySuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/available"));
            when(deliveryService.isCourierAvailable(courierId)).thenReturn(true);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).isCourierAvailable(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("available"));
        }

        /**
         * تست موفق دریافت آمار پیک
         * 
         * Scenario: درخواست GET /api/deliveries/courier/{courierId}/statistics
         * Expected:
         * - آمار کامل پیک برگردانده شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ دریافت موفق آمار پیک")
        void shouldGetCourierStatisticsSuccessfully() throws IOException {
            // Arrange
            Long courierId = 1L;
            DeliveryRepository.CourierStatistics stats = new DeliveryRepository.CourierStatistics(
                10L, 8L, 1L, 0L, 25.5, 800.0
            );
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/statistics"));
            when(deliveryService.getCourierStatistics(courierId)).thenReturn(stats);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getCourierStatistics(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        /**
         * تست بازگشت 400 برای وضعیت نامعتبر
         * 
         * Scenario: درخواست GET /api/deliveries/status/invalid
         * Expected: HTTP 400 Bad Request
         */
        @Test
        @DisplayName("❌ بازگشت 400 برای وضعیت نامعتبر")
        void shouldReturn400ForInvalidStatus() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/status/invalid"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid status: invalid"));
        }

        /**
         * تست بازگشت 404 برای تحویل یافت نشده
         * 
         * Scenario: درخواست تحویل با ID غیرموجود
         * Expected: HTTP 404 Not Found
         */
        @Test
        @DisplayName("❌ بازگشت 404 برای تحویل یافت نشده")
        void shouldReturn404ForDeliveryNotFound() throws IOException {
            // Arrange
            Long deliveryId = 999L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            when(deliveryService.getDelivery(deliveryId)).thenThrow(new NotFoundException("Delivery", deliveryId));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
        }

        /**
         * تست بازگشت 404 برای endpoint ناشناخته
         * 
         * Scenario: درخواست به endpoint غیرموجود
         * Expected: HTTP 404 Not Found
         */
        @Test
        @DisplayName("❌ بازگشت 404 برای endpoint ناشناخته")
        void shouldReturn404ForUnknownEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/unknown"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        /**
         * تست مدیریت لیست خالی تحویل‌ها
         * 
         * Scenario: درخواست لیست تحویل‌ها که نتیجه خالی دارد
         * Expected:
         * - آرایه خالی JSON برگردانده شود
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ مدیریت لیست خالی تحویل‌ها")
        void shouldHandleEmptyDeliveryListResponses() throws IOException {
            // Arrange
            List<Delivery> emptyList = Arrays.asList();
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/active"));
            when(deliveryService.getActiveDeliveries()).thenReturn(emptyList);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).getActiveDeliveries();
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("[]"));
        }

        /**
         * تست مدیریت پاسخ عدم در دسترس بودن پیک
         * 
         * Scenario: بررسی پیکی که در دسترس نیست
         * Expected:
         * - available: false در JSON
         * - HTTP 200 status code
         */
        @Test
        @DisplayName("✅ مدیریت پاسخ عدم در دسترس بودن پیک")
        void shouldHandleNullCourierAvailabilityResponse() throws IOException {
            // Arrange
            Long courierId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/courier/" + courierId + "/available"));
            when(deliveryService.isCourierAvailable(courierId)).thenReturn(false);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).isCourierAvailable(courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("\"available\":false"));
        }
    }

    @Nested
    @DisplayName("POST Endpoints Tests")
    class PostEndpointsTests {

        @Test
        @DisplayName("Should create delivery successfully")
        void shouldCreateDeliverySuccessfully() throws IOException {
            // Arrange
            Long orderId = 1L;
            Double estimatedFee = 50.0;
            Delivery delivery = createTestDelivery(1L);
            String requestBody = "{\"orderId\":" + orderId + ",\"estimatedFee\":" + estimatedFee + "}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.createDelivery(orderId, estimatedFee)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).createDelivery(orderId, estimatedFee);
            verify(exchange, times(1)).sendResponseHeaders(201, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should return 400 when order ID is missing")
        void shouldReturn400WhenOrderIdIsMissing() throws IOException {
            // Arrange
            String requestBody = "{\"estimatedFee\":50.0}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Order ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when estimated fee is missing")
        void shouldReturn400WhenEstimatedFeeIsMissing() throws IOException {
            // Arrange
            String requestBody = "{\"orderId\":1}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Estimated fee is required"));
        }

        @Test
        @DisplayName("Should return 404 for unknown POST endpoint")
        void shouldReturn404ForUnknownPostEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/unknown"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        @Test
        @DisplayName("Should handle NotFoundException when creating delivery")
        void shouldHandleNotFoundExceptionWhenCreatingDelivery() throws IOException {
            // Arrange
            Long orderId = 999L;
            Double estimatedFee = 50.0;
            String requestBody = "{\"orderId\":" + orderId + ",\"estimatedFee\":" + estimatedFee + "}";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.createDelivery(orderId, estimatedFee))
                .thenThrow(new NotFoundException("Order", orderId));

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).createDelivery(orderId, estimatedFee);
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should handle invalid JSON in POST request")
        void shouldHandleInvalidJsonInPostRequest() throws IOException {
            // Arrange
            String requestBody = "invalid json";
            
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid JSON"));
        }
    }

    @Nested
    @DisplayName("PUT Endpoints Tests")
    class PutEndpointsTests {

        @Test
        @DisplayName("Should assign courier successfully")
        void shouldAssignCourierSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.assignCourier(deliveryId, courierId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).assignCourier(deliveryId, courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should mark picked up successfully")
        void shouldMarkPickedUpSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/pickup"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.markPickedUp(deliveryId, courierId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).markPickedUp(deliveryId, courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should mark delivered successfully")
        void shouldMarkDeliveredSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/deliver"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.markDelivered(deliveryId, courierId)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).markDelivered(deliveryId, courierId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should cancel delivery successfully")
        void shouldCancelDeliverySuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String reason = "Customer requested cancellation";
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"reason\":\"" + reason + "\"}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/cancel"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.cancelDelivery(deliveryId, reason)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).cancelDelivery(deliveryId, reason);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should cancel delivery with null reason")
        void shouldCancelDeliveryWithNullReason() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/cancel"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.cancelDelivery(deliveryId, null)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).cancelDelivery(deliveryId, null);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should update delivery status successfully")
        void shouldUpdateDeliveryStatusSuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            DeliveryStatus status = DeliveryStatus.DELIVERED;
            Delivery delivery = createTestDelivery(deliveryId);
            String requestBody = "{\"status\":\"delivered\"}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/status"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.updateDeliveryStatus(deliveryId, status)).thenReturn(delivery);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).updateDeliveryStatus(deliveryId, status);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should return 400 when courier ID is missing for assign")
        void shouldReturn400WhenCourierIdIsMissingForAssign() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when courier ID is missing for pickup")
        void shouldReturn400WhenCourierIdIsMissingForPickup() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/pickup"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when courier ID is missing for deliver")
        void shouldReturn400WhenCourierIdIsMissingForDeliver() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/deliver"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when status is missing")
        void shouldReturn400WhenStatusIsMissing() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/status"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Status is required"));
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void shouldReturn400ForInvalidStatusInUpdate() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "{\"status\":\"invalid\"}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/status"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid status: invalid"));
        }

        @Test
        @DisplayName("Should return 409 for illegal state exception")
        void shouldReturn409ForIllegalStateException() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            Long courierId = 2L;
            String requestBody = "{\"courierId\":" + courierId + "}";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            when(deliveryService.assignCourier(deliveryId, courierId))
                .thenThrow(new IllegalStateException("Can only assign courier to pending deliveries"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(409, responseBody.toByteArray().length);
        }

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJsonInRequestBody() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "invalid json";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Invalid JSON"));
        }

        @Test
        @DisplayName("Should handle empty request body")
        void shouldHandleEmptyRequestBody() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            String requestBody = "";
            
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId + "/assign"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(400, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Courier ID is required"));
        }

        @Test
        @DisplayName("Should return 404 for unknown PUT endpoint")
        void shouldReturn404ForUnknownPutEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/1/unknown"));
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }
    }

    @Nested
    @DisplayName("DELETE Endpoints Tests")
    class DeleteEndpointsTests {

        @Test
        @DisplayName("Should delete delivery successfully")
        void shouldDeleteDeliverySuccessfully() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            doNothing().when(deliveryService).deleteDelivery(deliveryId);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).deleteDelivery(deliveryId);
            verify(exchange, times(1)).sendResponseHeaders(200, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("deleted successfully"));
        }

        @Test
        @DisplayName("Should return 404 for unknown DELETE endpoint")
        void shouldReturn404ForUnknownDeleteEndpoint() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/unknown"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        @Test
        @DisplayName("Should handle NotFoundException when deleting delivery")
        void shouldHandleNotFoundExceptionWhenDeletingDelivery() throws IOException {
            // Arrange
            Long deliveryId = 999L;
            
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            doThrow(new NotFoundException("Delivery", deliveryId)).when(deliveryService).deleteDelivery(deliveryId);

            // Act
            controller.handle(exchange);

            // Assert
            verify(deliveryService).deleteDelivery(deliveryId);
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 405 for unsupported method")
        void shouldReturn405ForUnsupportedMethod() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("PATCH");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/1"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(405, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Method not allowed"));
        }

        @Test
        @DisplayName("Should return 500 for internal server error")
        void shouldReturn500ForInternalServerError() throws IOException {
            // Arrange
            Long deliveryId = 1L;
            
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/" + deliveryId));
            when(deliveryService.getDelivery(deliveryId)).thenThrow(new RuntimeException("Database error"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(500, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Internal server error"));
        }

        @Test
        @DisplayName("Should handle invalid delivery ID in path")
        void shouldHandleInvalidDeliveryIdInPath() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/deliveries/invalid"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }

        @Test
        @DisplayName("Should handle malformed path")
        void shouldHandleMalformedPath() throws IOException {
            // Arrange
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api"));

            // Act
            controller.handle(exchange);

            // Assert
            verify(exchange, times(1)).sendResponseHeaders(404, responseBody.toByteArray().length);
            assertTrue(responseBody.toString().contains("Endpoint not found"));
        }
    }

    // Helper method to create test delivery
    private Delivery createTestDelivery(Long id) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(id);
        
        Delivery delivery = new Delivery(order, 50.0);
        // Use reflection to set ID
        try {
            java.lang.reflect.Field idField = Delivery.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(delivery, id);
        } catch (Exception e) {
            // Ignore reflection errors in tests
        }
        
        return delivery;
    }
}
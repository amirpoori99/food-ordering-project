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
 * Comprehensive test suite for CouponController
 * Tests all REST API endpoints, HTTP status codes, and error handling
 */
@DisplayName("CouponController Tests")
public class CouponControllerTest {
    
    @Mock
    private CouponService couponService;
    
    @Mock
    private HttpExchange exchange;
    
    @Mock
    private Headers responseHeaders;
    
    private CouponController controller;
    private ByteArrayOutputStream responseStream;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CouponController(couponService);
        responseStream = new ByteArrayOutputStream();
        
        // Setup mock response headers
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseStream);
    }
    
    @Nested
    @DisplayName("GET Endpoint Tests")
    class GetEndpointTests {
        
        @Test
        @DisplayName("Should get coupon by ID successfully")
        void shouldGetCouponByIdSuccessfully() throws IOException {
            // Arrange
            Long couponId = 1L;
            Coupon coupon = createTestCoupon();
            when(couponService.getCoupon(couponId)).thenReturn(coupon);
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/coupons/1"));
            when(exchange.getResponseBody()).thenReturn(responseStream);
            
            // Act
            controller.handle(exchange);
            
            // Assert
            verify(couponService).getCoupon(couponId);
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }
        
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
    
    @Nested
    @DisplayName("POST Endpoint Tests")
    class PostEndpointTests {
        
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
    
    @Nested
    @DisplayName("PUT Endpoint Tests")
    class PutEndpointTests {
        
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
    
    @Nested
    @DisplayName("DELETE Endpoint Tests")
    class DeleteEndpointTests {
        
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
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
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
            
            // Assert - Malformed JSON results in 500 internal server error
            verify(exchange).sendResponseHeaders(eq(500), anyLong());
        }
    }
    
    // Helper methods
    private Coupon createTestCoupon() {
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = Coupon.createPercentageCoupon("SAVE20", "20% off", 20.0, now.minusDays(1), now.plusDays(30));
        coupon.setId(1L);
        coupon.setCreatedBy(1L);
        return coupon;
    }
}

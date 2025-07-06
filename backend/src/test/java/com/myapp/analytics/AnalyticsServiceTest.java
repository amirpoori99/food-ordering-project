package com.myapp.analytics;

import com.myapp.analytics.service.AnalyticsService;
import com.myapp.analytics.repository.AnalyticsRepository;
import com.myapp.analytics.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * تست‌های سرویس تحلیل داده‌ها
 * این کلاس تمام قابلیت‌های Analytics Service را تست می‌کند
 * 
 * @author Food Ordering System Team
 * @version 2.0 - Comprehensive Test Coverage
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Analytics Service Tests")
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        analyticsService = new AnalyticsService(analyticsRepository);
        setupMockRepository();
    }

    /**
     * تنظیم mock repository با داده‌های واقعی
     */
    private void setupMockRepository() {
        System.out.println("[TEST-LOG] setupMockRepository called");
        // Mock sales analytics with proper return values
        SalesAnalyticsDTO salesAnalytics = createSampleSalesAnalytics();
        when(analyticsRepository.getSalesAnalytics(anyString())).thenReturn(salesAnalytics);
        
        // Mock user analytics with proper return values
        UserAnalyticsDTO userAnalytics = createSampleUserAnalytics();
        when(analyticsRepository.getUserAnalytics(anyString())).thenReturn(userAnalytics);
        
        // Mock restaurant analytics with proper return values
        RestaurantAnalyticsDTO restaurantAnalytics = createSampleRestaurantAnalytics();
        when(analyticsRepository.getRestaurantAnalytics(anyString())).thenReturn(restaurantAnalytics);
        
        // Mock system overview with proper return values
        SystemOverviewDTO systemOverview = createSampleSystemOverview();
        when(analyticsRepository.getSystemOverview()).thenReturn(systemOverview);
    }

    @Nested
    @DisplayName("Sales Analytics Tests")
    class SalesAnalyticsTests {

        @Test
        @DisplayName("Should get sales analytics for different periods")
        void testGetSalesAnalyticsForDifferentPeriods() {
            // Given
            String period = "monthly";

            // When
            SalesAnalyticsDTO result = analyticsService.getSalesAnalytics(period);

            // Then
            assertNotNull(result);
            assertEquals(1000L, result.getTotalSales());
            assertNotNull(result.getTopSellingItems());
            assertNotNull(result.getSalesByCategory());
        }

        @ParameterizedTest
        @ValueSource(strings = {"daily", "weekly", "monthly", "yearly"})
        @DisplayName("Should handle different time periods")
        void testDifferentTimePeriods(String period) {
            // When
            SalesAnalyticsDTO result = analyticsService.getSalesAnalytics(period);

            // Then
            assertNotNull(result);
            assertEquals(1000L, result.getTotalSales());
        }
    }

    @Nested
    @DisplayName("User Analytics Tests")
    class UserAnalyticsTests {

        @Test
        @DisplayName("Should get user analytics")
        void testGetUserAnalytics() {
            // When
            UserAnalyticsDTO result = analyticsService.getUserAnalytics("monthly");

            // Then
            assertNotNull(result);
            assertEquals(500, result.getTotalUsers());
            assertEquals(300, result.getActiveUsers());
            assertEquals(50, result.getNewUsers());
        }
    }

    @Nested
    @DisplayName("Restaurant Analytics Tests")
    class RestaurantAnalyticsTests {

        @Test
        @DisplayName("Should get restaurant analytics")
        void testGetRestaurantAnalytics() {
            // When
            RestaurantAnalyticsDTO result = analyticsService.getRestaurantAnalytics("monthly");

            // Then
            assertNotNull(result);
            assertEquals(100, result.getTotalRestaurants());
            assertEquals(80, result.getActiveRestaurants());
            assertNotNull(result.getTopPerformingRestaurants());
        }
    }

    @Nested
    @DisplayName("System Overview Tests")
    class SystemOverviewTests {

        @Test
        @DisplayName("Should get system overview")
        void testGetSystemOverview() {
            // When
            SystemOverviewDTO result = analyticsService.getSystemOverview();

            // Then
            assertNotNull(result);
            assertEquals(1000, result.getTotalOrders());
            assertEquals(50000.0, result.getTotalRevenue());
            assertEquals(500, result.getTotalUsers());
            assertEquals(100, result.getTotalRestaurants());
        }
    }

    // Helper methods to create sample data
    private SalesAnalyticsDTO createSampleSalesAnalytics() {
        SalesAnalyticsDTO dto = new SalesAnalyticsDTO();
        dto.setTotalSales(1000L);
        dto.setSalesGrowth(15.5);
        dto.setTopSellingItems(createSampleTopItems());
        dto.setSalesByCategory(createSampleCategories());
        return dto;
    }

    private UserAnalyticsDTO createSampleUserAnalytics() {
        UserAnalyticsDTO dto = new UserAnalyticsDTO();
        dto.setTotalUsers(500);
        dto.setActiveUsers(300);
        dto.setNewUsers(50);
        dto.setUserRetention(85.5);
        return dto;
    }

    private RestaurantAnalyticsDTO createSampleRestaurantAnalytics() {
        RestaurantAnalyticsDTO dto = new RestaurantAnalyticsDTO();
        dto.setTotalRestaurants(100);
        dto.setActiveRestaurants(80);
        dto.setTopPerformingRestaurants(createSampleTopRestaurants());
        return dto;
    }

    private SystemOverviewDTO createSampleSystemOverview() {
        SystemOverviewDTO dto = new SystemOverviewDTO();
        dto.setTotalUsers(500);
        dto.setTotalRestaurants(100);
        dto.setTotalOrders(1000);
        dto.setTotalRevenue(50000.0);
        dto.setActiveUsers(300);
        dto.setActiveRestaurants(80);
        dto.setCompletedOrders(800);
        dto.setPendingOrders(200);
        dto.setNewUsers(50);
        dto.setAverageOrderValue(50.0);
        dto.setAverageDeliveryTime(30.0);
        dto.setDeliverySuccessRate(95.0);
        return dto;
    }

    private List<Map<String, Object>> createSampleTopItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("itemName", "Sample Item");
        item.put("quantity", 100);
        item.put("revenue", 5000.0);
        items.add(item);
        return items;
    }

    private List<Map<String, Object>> createSampleCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();
        Map<String, Object> category = new HashMap<>();
        category.put("category", "Fast Food");
        category.put("orders", 500L);
        category.put("revenue", 25000.0);
        categories.add(category);
        return categories;
    }

    private List<Map<String, Object>> createSampleTopRestaurants() {
        List<Map<String, Object>> restaurants = new ArrayList<>();
        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("restaurantName", "Sample Restaurant");
        restaurant.put("orders", 200L);
        restaurant.put("revenue", 10000.0);
        restaurants.add(restaurant);
        return restaurants;
    }
} 
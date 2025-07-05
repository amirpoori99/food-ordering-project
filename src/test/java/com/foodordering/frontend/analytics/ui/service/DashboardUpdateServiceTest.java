package com.foodordering.frontend.analytics.ui.service;

import com.foodordering.backend.analytics.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
class DashboardUpdateServiceTest {

    @Mock
    private AnalyticsUIService analyticsUIService;

    private DashboardUpdateService dashboardUpdateService;

    @BeforeEach
    void setUp() {
        dashboardUpdateService = new DashboardUpdateService(analyticsUIService);
    }

    @Test
    void testStartRealTimeUpdates() {
        // Test that the service can start real-time updates
        assertDoesNotThrow(() -> {
            dashboardUpdateService.startRealTimeUpdates();
        });
    }

    @Test
    void testStopRealTimeUpdates() {
        // Test that the service can stop real-time updates
        assertDoesNotThrow(() -> {
            dashboardUpdateService.stopRealTimeUpdates();
        });
    }

    @Test
    void testIsRealTimeEnabled() {
        // Test initial state
        assertFalse(dashboardUpdateService.isRealTimeEnabled());
        
        // Test after starting updates
        dashboardUpdateService.startRealTimeUpdates();
        assertTrue(dashboardUpdateService.isRealTimeEnabled());
        
        // Test after stopping updates
        dashboardUpdateService.stopRealTimeUpdates();
        assertFalse(dashboardUpdateService.isRealTimeEnabled());
    }

    @Test
    void testUpdateDashboardData() {
        // Create sample data
        SalesAnalyticsDTO salesData = new SalesAnalyticsDTO();
        salesData.setTotalSales(1000.0);
        salesData.setTotalOrders(50);
        
        OrderAnalyticsDTO orderData = new OrderAnalyticsDTO();
        orderData.setTotalOrders(50);
        orderData.setAverageOrderValue(20.0);
        
        CustomerAnalyticsDTO customerData = new CustomerAnalyticsDTO();
        customerData.setTotalCustomers(100);
        customerData.setNewCustomers(10);
        
        MenuAnalyticsDTO menuData = new MenuAnalyticsDTO();
        menuData.setTotalItems(25);
        menuData.setPopularItems(Arrays.asList("Pizza", "Burger"));
        
        DeliveryAnalyticsDTO deliveryData = new DeliveryAnalyticsDTO();
        deliveryData.setAverageDeliveryTime(30.0);
        deliveryData.setOnTimeDeliveries(45);
        
        // Test that update method can be called without throwing exceptions
        assertDoesNotThrow(() -> {
            dashboardUpdateService.updateDashboardData(
                salesData, orderData, customerData, menuData, deliveryData
            );
        });
    }

    @Test
    void testRefreshDashboard() {
        // Test that refresh method can be called without throwing exceptions
        assertDoesNotThrow(() -> {
            dashboardUpdateService.refreshDashboard();
        });
    }

    @Test
    void testSetUpdateInterval() {
        // Test setting different update intervals
        assertDoesNotThrow(() -> {
            dashboardUpdateService.setUpdateInterval(5000);
            dashboardUpdateService.setUpdateInterval(10000);
            dashboardUpdateService.setUpdateInterval(30000);
        });
    }

    @Test
    void testGetUpdateInterval() {
        // Test getting the current update interval
        long interval = dashboardUpdateService.getUpdateInterval();
        assertTrue(interval > 0);
    }

    @Test
    void testAddUpdateListener() {
        // Test adding an update listener
        DashboardUpdateService.UpdateListener listener = mock(DashboardUpdateService.UpdateListener.class);
        
        assertDoesNotThrow(() -> {
            dashboardUpdateService.addUpdateListener(listener);
        });
    }

    @Test
    void testRemoveUpdateListener() {
        // Test removing an update listener
        DashboardUpdateService.UpdateListener listener = mock(DashboardUpdateService.UpdateListener.class);
        
        assertDoesNotThrow(() -> {
            dashboardUpdateService.addUpdateListener(listener);
            dashboardUpdateService.removeUpdateListener(listener);
        });
    }

    @Test
    void testGetLastUpdateTime() {
        // Test getting the last update time
        long lastUpdateTime = dashboardUpdateService.getLastUpdateTime();
        assertTrue(lastUpdateTime >= 0);
    }

    @Test
    void testIsUpdating() {
        // Test the updating status
        boolean isUpdating = dashboardUpdateService.isUpdating();
        assertFalse(isUpdating); // Should be false initially
    }

    @Test
    void testGetUpdateCount() {
        // Test getting the update count
        long updateCount = dashboardUpdateService.getUpdateCount();
        assertTrue(updateCount >= 0);
    }

    @Test
    void testResetUpdateCount() {
        // Test resetting the update count
        assertDoesNotThrow(() -> {
            dashboardUpdateService.resetUpdateCount();
        });
    }
} 
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
class AnalyticsUIServiceTest {

    @Mock
    private AnalyticsController analyticsController;

    private AnalyticsUIService analyticsUIService;

    @BeforeEach
    void setUp() {
        analyticsUIService = new AnalyticsUIService(analyticsController);
    }

    @Test
    void testLoadSalesAnalytics() {
        // Test loading sales analytics
        assertDoesNotThrow(() -> {
            CompletableFuture<SalesAnalyticsDTO> future = analyticsUIService.loadSalesAnalytics();
            assertNotNull(future);
        });
    }

    @Test
    void testLoadOrderAnalytics() {
        // Test loading order analytics
        assertDoesNotThrow(() -> {
            CompletableFuture<OrderAnalyticsDTO> future = analyticsUIService.loadOrderAnalytics();
            assertNotNull(future);
        });
    }

    @Test
    void testLoadCustomerAnalytics() {
        // Test loading customer analytics
        assertDoesNotThrow(() -> {
            CompletableFuture<CustomerAnalyticsDTO> future = analyticsUIService.loadCustomerAnalytics();
            assertNotNull(future);
        });
    }

    @Test
    void testLoadMenuAnalytics() {
        // Test loading menu analytics
        assertDoesNotThrow(() -> {
            CompletableFuture<MenuAnalyticsDTO> future = analyticsUIService.loadMenuAnalytics();
            assertNotNull(future);
        });
    }

    @Test
    void testLoadDeliveryAnalytics() {
        // Test loading delivery analytics
        assertDoesNotThrow(() -> {
            CompletableFuture<DeliveryAnalyticsDTO> future = analyticsUIService.loadDeliveryAnalytics();
            assertNotNull(future);
        });
    }

    @Test
    void testLoadAllAnalytics() {
        // Test loading all analytics
        assertDoesNotThrow(() -> {
            CompletableFuture<AnalyticsDataDTO> future = analyticsUIService.loadAllAnalytics();
            assertNotNull(future);
        });
    }

    @Test
    void testExportAnalyticsData() {
        // Test exporting analytics data
        assertDoesNotThrow(() -> {
            CompletableFuture<String> future = analyticsUIService.exportAnalyticsData("test.csv");
            assertNotNull(future);
        });
    }

    @Test
    void testGenerateReport() {
        // Test generating report
        assertDoesNotThrow(() -> {
            CompletableFuture<String> future = analyticsUIService.generateReport("PDF");
            assertNotNull(future);
        });
    }

    @Test
    void testGetAnalyticsController() {
        // Test getting the analytics controller
        AnalyticsController controller = analyticsUIService.getAnalyticsController();
        assertNotNull(controller);
        assertEquals(analyticsController, controller);
    }

    @Test
    void testIsLoading() {
        // Test loading status
        boolean isLoading = analyticsUIService.isLoading();
        assertFalse(isLoading); // Should be false initially
    }

    @Test
    void testGetLastLoadTime() {
        // Test getting last load time
        long lastLoadTime = analyticsUIService.getLastLoadTime();
        assertTrue(lastLoadTime >= 0);
    }

    @Test
    void testGetLoadCount() {
        // Test getting load count
        long loadCount = analyticsUIService.getLoadCount();
        assertTrue(loadCount >= 0);
    }

    @Test
    void testResetLoadCount() {
        // Test resetting load count
        assertDoesNotThrow(() -> {
            analyticsUIService.resetLoadCount();
        });
    }
} 
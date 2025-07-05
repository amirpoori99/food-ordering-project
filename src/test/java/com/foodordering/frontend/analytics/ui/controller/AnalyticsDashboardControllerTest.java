package com.foodordering.frontend.analytics.ui.controller;

import com.foodordering.backend.analytics.dto.*;
import com.foodordering.frontend.analytics.ui.service.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsDashboardControllerTest extends ApplicationTest {

    @Mock
    private AnalyticsUIService analyticsUIService;

    @Mock
    private ChartDataService chartDataService;

    @Mock
    private DashboardUpdateService dashboardUpdateService;

    private AnalyticsDashboardController controller;
    private Stage stage;

    @Start
    private void start(Stage stage) throws IOException {
        this.stage = stage;
        
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/analytics-dashboard.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        
        // Set up scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        // Initialize controller with mocked services
        controller.setAnalyticsUIService(analyticsUIService);
        controller.setChartDataService(chartDataService);
        controller.setDashboardUpdateService(dashboardUpdateService);
    }

    @Test
    void testInitialize() {
        // Test that controller initializes without throwing exceptions
        assertDoesNotThrow(() -> {
            controller.initialize();
        });
    }

    @Test
    void testLoadDashboardData() {
        // Test loading dashboard data
        assertDoesNotThrow(() -> {
            controller.loadDashboardData();
        });
    }

    @Test
    void testRefreshData() {
        // Test refreshing data
        assertDoesNotThrow(() -> {
            controller.refreshData();
        });
    }

    @Test
    void testExportData() {
        // Test exporting data
        assertDoesNotThrow(() -> {
            controller.exportData();
        });
    }

    @Test
    void testGenerateReport() {
        // Test generating report
        assertDoesNotThrow(() -> {
            controller.generateReport();
        });
    }

    @Test
    void testToggleRealTimeUpdates() {
        // Test toggling real-time updates
        assertDoesNotThrow(() -> {
            controller.toggleRealTimeUpdates();
        });
    }

    @Test
    void testUpdateCharts() {
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
        
        // Test updating charts
        assertDoesNotThrow(() -> {
            controller.updateCharts(salesData, orderData, customerData, menuData, deliveryData);
        });
    }

    @Test
    void testUpdateSummaryCards() {
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
        
        // Test updating summary cards
        assertDoesNotThrow(() -> {
            controller.updateSummaryCards(salesData, orderData, customerData, menuData, deliveryData);
        });
    }

    @Test
    void testShowLoadingIndicator() {
        // Test showing loading indicator
        assertDoesNotThrow(() -> {
            controller.showLoadingIndicator();
        });
    }

    @Test
    void testHideLoadingIndicator() {
        // Test hiding loading indicator
        assertDoesNotThrow(() -> {
            controller.hideLoadingIndicator();
        });
    }

    @Test
    void testShowError() {
        // Test showing error
        assertDoesNotThrow(() -> {
            controller.showError("Test error message");
        });
    }

    @Test
    void testShowSuccess() {
        // Test showing success message
        assertDoesNotThrow(() -> {
            controller.showSuccess("Test success message");
        });
    }

    @Test
    void testGetAnalyticsUIService() {
        // Test getting analytics UI service
        AnalyticsUIService service = controller.getAnalyticsUIService();
        assertNotNull(service);
        assertEquals(analyticsUIService, service);
    }

    @Test
    void testGetChartDataService() {
        // Test getting chart data service
        ChartDataService service = controller.getChartDataService();
        assertNotNull(service);
        assertEquals(chartDataService, service);
    }

    @Test
    void testGetDashboardUpdateService() {
        // Test getting dashboard update service
        DashboardUpdateService service = controller.getDashboardUpdateService();
        assertNotNull(service);
        assertEquals(dashboardUpdateService, service);
    }

    @Test
    void testIsDataLoaded() {
        // Test data loaded status
        boolean isLoaded = controller.isDataLoaded();
        assertFalse(isLoaded); // Should be false initially
    }

    @Test
    void testGetLastUpdateTime() {
        // Test getting last update time
        long lastUpdateTime = controller.getLastUpdateTime();
        assertTrue(lastUpdateTime >= 0);
    }
} 
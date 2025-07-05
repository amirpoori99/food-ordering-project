package com.foodordering.frontend.analytics.ui.service;

import com.foodordering.backend.analytics.dto.*;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
class ChartDataServiceTest {

    @Mock
    private AnalyticsUIService analyticsUIService;

    private ChartDataService chartDataService;

    @BeforeEach
    void setUp() {
        chartDataService = new ChartDataService(analyticsUIService);
    }

    @Test
    void testCreateSalesChartData() {
        // Create sample sales data
        SalesAnalyticsDTO salesData = new SalesAnalyticsDTO();
        salesData.setTotalSales(1000.0);
        salesData.setTotalOrders(50);
        
        // Test creating sales chart data
        assertDoesNotThrow(() -> {
            XYChart.Series<String, Number> series = chartDataService.createSalesChartData(salesData);
            assertNotNull(series);
        });
    }

    @Test
    void testCreateOrderChartData() {
        // Create sample order data
        OrderAnalyticsDTO orderData = new OrderAnalyticsDTO();
        orderData.setTotalOrders(50);
        orderData.setAverageOrderValue(20.0);
        
        // Test creating order chart data
        assertDoesNotThrow(() -> {
            XYChart.Series<String, Number> series = chartDataService.createOrderChartData(orderData);
            assertNotNull(series);
        });
    }

    @Test
    void testCreateCustomerChartData() {
        // Create sample customer data
        CustomerAnalyticsDTO customerData = new CustomerAnalyticsDTO();
        customerData.setTotalCustomers(100);
        customerData.setNewCustomers(10);
        
        // Test creating customer chart data
        assertDoesNotThrow(() -> {
            XYChart.Series<String, Number> series = chartDataService.createCustomerChartData(customerData);
            assertNotNull(series);
        });
    }

    @Test
    void testCreateMenuChartData() {
        // Create sample menu data
        MenuAnalyticsDTO menuData = new MenuAnalyticsDTO();
        menuData.setTotalItems(25);
        menuData.setPopularItems(Arrays.asList("Pizza", "Burger"));
        
        // Test creating menu chart data
        assertDoesNotThrow(() -> {
            ObservableList<PieChart.Data> pieData = chartDataService.createMenuChartData(menuData);
            assertNotNull(pieData);
        });
    }

    @Test
    void testCreateDeliveryChartData() {
        // Create sample delivery data
        DeliveryAnalyticsDTO deliveryData = new DeliveryAnalyticsDTO();
        deliveryData.setAverageDeliveryTime(30.0);
        deliveryData.setOnTimeDeliveries(45);
        
        // Test creating delivery chart data
        assertDoesNotThrow(() -> {
            XYChart.Series<String, Number> series = chartDataService.createDeliveryChartData(deliveryData);
            assertNotNull(series);
        });
    }

    @Test
    void testCreateTimeSeriesData() {
        // Test creating time series data
        assertDoesNotThrow(() -> {
            XYChart.Series<String, Number> series = chartDataService.createTimeSeriesData(
                Arrays.asList("Jan", "Feb", "Mar"),
                Arrays.asList(100.0, 150.0, 200.0)
            );
            assertNotNull(series);
        });
    }

    @Test
    void testCreatePieChartData() {
        // Test creating pie chart data
        Map<String, Double> data = Map.of("Category 1", 30.0, "Category 2", 70.0);
        
        assertDoesNotThrow(() -> {
            ObservableList<PieChart.Data> pieData = chartDataService.createPieChartData(data);
            assertNotNull(pieData);
        });
    }

    @Test
    void testCreateBarChartData() {
        // Test creating bar chart data
        Map<String, Number> data = Map.of("Category 1", 100, "Category 2", 200);
        
        assertDoesNotThrow(() -> {
            XYChart.Series<String, Number> series = chartDataService.createBarChartData(data);
            assertNotNull(series);
        });
    }

    @Test
    void testCreateLineChartData() {
        // Test creating line chart data
        List<String> categories = Arrays.asList("Jan", "Feb", "Mar");
        List<Number> values = Arrays.asList(100, 150, 200);
        
        assertDoesNotThrow(() -> {
            XYChart.Series<String, Number> series = chartDataService.createLineChartData(categories, values);
            assertNotNull(series);
        });
    }

    @Test
    void testGetAnalyticsUIService() {
        // Test getting the analytics UI service
        AnalyticsUIService service = chartDataService.getAnalyticsUIService();
        assertNotNull(service);
        assertEquals(analyticsUIService, service);
    }

    @Test
    void testGetChartDataCount() {
        // Test getting chart data count
        long count = chartDataService.getChartDataCount();
        assertTrue(count >= 0);
    }

    @Test
    void testResetChartDataCount() {
        // Test resetting chart data count
        assertDoesNotThrow(() -> {
            chartDataService.resetChartDataCount();
        });
    }
} 
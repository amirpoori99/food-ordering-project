package com.myapp.admin;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AdminController
 */
class AdminControllerTest {

    @Mock
    private AdminService adminService;
    
    @Mock
    private HttpExchange exchange;
    
    private AdminController adminController;
    private ByteArrayOutputStream responseStream;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminController = new AdminController(adminService);
        responseStream = new ByteArrayOutputStream();
    }

    @Test
    @DisplayName("Should return dashboard statistics successfully")
    void testGetDashboardStatistics_Success() throws IOException, URISyntaxException {
        // Arrange
        AdminRepository.SystemStatistics stats = new AdminRepository.SystemStatistics(
            100L, 50L, 200L, 150L, 10000.0, 500.0, 20L, 1500.0, 45L, 10L, 25L
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/dashboard"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getSystemStatistics()).thenReturn(stats);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getSystemStatistics();
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void testGetAllUsers_WithPagination() throws IOException, URISyntaxException {
        // Arrange
        List<User> users = Arrays.asList(
            new User(1L, "John Doe", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address 1"),
            new User(2L, "Jane Smith", "0987654321", "jane@test.com", "hash", User.Role.SELLER, "Address 2")
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/users?page=0&size=10"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getAllUsers(null, null, 0, 10)).thenReturn(users);
        when(adminService.countUsers(null, null)).thenReturn(2L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getAllUsers(null, null, 0, 10);
        verify(adminService, times(1)).countUsers(null, null);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() throws IOException, URISyntaxException {
        // Arrange
        User user = new User(1L, "John Doe", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/users/1"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getUserById(1L)).thenReturn(user);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getUserById(1L);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void testGetUserById_NotFound() throws IOException, URISyntaxException {
        // Arrange
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/users/999"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getUserById(999L)).thenThrow(new NotFoundException("User", 999L));
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(exchange, times(1)).sendResponseHeaders(eq(404), anyLong());
    }

    @Test
    @DisplayName("Should update user status successfully")
    void testUpdateUserStatus_Success() throws IOException, URISyntaxException {
        // Arrange
        String requestBody = "{\"isActive\":false,\"adminId\":1}";
        
        when(exchange.getRequestMethod()).thenReturn("PUT");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/users/2/status"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        doNothing().when(adminService).updateUserStatus(2L, false, 1L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).updateUserStatus(2L, false, 1L);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get all restaurants")
    void testGetAllRestaurants() throws IOException, URISyntaxException {
        // Arrange
        List<Restaurant> restaurants = Arrays.asList(
            new Restaurant(1L, 1L, "Pizza Palace", "123 Main St", "1234567890", RestaurantStatus.APPROVED),
            new Restaurant(2L, 2L, "Burger Joint", "456 Oak Ave", "0987654321", RestaurantStatus.PENDING)
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/restaurants"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getAllRestaurants(null, null, 0, 20)).thenReturn(restaurants);
        when(adminService.countRestaurants(null, null)).thenReturn(2L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getAllRestaurants(null, null, 0, 20);
        verify(adminService, times(1)).countRestaurants(null, null);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should update restaurant status successfully")
    void testUpdateRestaurantStatus_Success() throws IOException, URISyntaxException {
        // Arrange
        String requestBody = "{\"status\":\"APPROVED\",\"adminId\":1}";
        
        when(exchange.getRequestMethod()).thenReturn("PUT");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/restaurants/1/status"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        doNothing().when(adminService).updateRestaurantStatus(1L, RestaurantStatus.APPROVED, 1L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).updateRestaurantStatus(1L, RestaurantStatus.APPROVED, 1L);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get all orders")
    void testGetAllOrders() throws IOException, URISyntaxException {
        // Arrange
        User customer = new User(1L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
        Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
        List<com.myapp.common.models.Order> orders = Arrays.asList(
            com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890")
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/orders"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getAllOrders(null, null, null, null, 0, 20)).thenReturn(orders);
        when(adminService.countOrders(null, null, null, null)).thenReturn(1L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getAllOrders(null, null, null, null, 0, 20);
        verify(adminService, times(1)).countOrders(null, null, null, null);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus_Success() throws IOException, URISyntaxException {
        // Arrange
        String requestBody = "{\"status\":\"CONFIRMED\",\"adminId\":1}";
        
        when(exchange.getRequestMethod()).thenReturn("PUT");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/orders/1/status"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        doNothing().when(adminService).updateOrderStatus(1L, OrderStatus.CONFIRMED, 1L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).updateOrderStatus(1L, OrderStatus.CONFIRMED, 1L);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get all transactions")
    void testGetAllTransactions() throws IOException, URISyntaxException {
        // Arrange
        List<Transaction> transactions = Arrays.asList(
            Transaction.forPayment(1L, 1L, 50.0, "CARD"),
            Transaction.forWalletCharge(2L, 100.0, "CARD")
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/transactions"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getAllTransactions(null, null, null, null, 0, 20)).thenReturn(transactions);
        when(adminService.countTransactions(null, null, null, null)).thenReturn(2L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getAllTransactions(null, null, null, null, 0, 20);
        verify(adminService, times(1)).countTransactions(null, null, null, null);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get transaction by ID successfully")
    void testGetTransactionById_Success() throws IOException, URISyntaxException {
        // Arrange
        Transaction transaction = Transaction.forPayment(1L, 1L, 50.0, "CARD");
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/transactions/1"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getTransactionById(1L)).thenReturn(transaction);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getTransactionById(1L);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get all deliveries")
    void testGetAllDeliveries() throws IOException, URISyntaxException {
        // Arrange
        User customer = new User(1L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
        Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
        com.myapp.common.models.Order order = com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890");
        List<Delivery> deliveries = Arrays.asList(
            new Delivery(order, 10.0)
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/deliveries"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getAllDeliveries(null, null, null, 0, 20)).thenReturn(deliveries);
        when(adminService.countDeliveries(null, null, null)).thenReturn(1L);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getAllDeliveries(null, null, null, 0, 20);
        verify(adminService, times(1)).countDeliveries(null, null, null);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get delivery by ID successfully")
    void testGetDeliveryById_Success() throws IOException, URISyntaxException {
        // Arrange
        User customer = new User(1L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
        Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
        com.myapp.common.models.Order order = com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890");
        Delivery delivery = new Delivery(order, 10.0);
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/deliveries/1"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getDeliveryById(1L)).thenReturn(delivery);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getDeliveryById(1L);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get daily statistics successfully")
    void testGetDailyStatistics_Success() throws IOException, URISyntaxException {
        // Arrange
        List<AdminRepository.DailyStatistics> stats = Arrays.asList(
            new AdminRepository.DailyStatistics(java.sql.Date.valueOf("2023-12-01"), 10L, 500.0),
            new AdminRepository.DailyStatistics(java.sql.Date.valueOf("2023-12-02"), 15L, 750.0)
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/statistics/daily?days=7"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getDailyStatistics(7)).thenReturn(stats);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getDailyStatistics(7);
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get user statistics by role")
    void testGetUserStatistics_Success() throws IOException, URISyntaxException {
        // Arrange
        Map<User.Role, Long> stats = Map.of(
            User.Role.BUYER, 80L,
            User.Role.SELLER, 15L,
            User.Role.COURIER, 10L,
            User.Role.ADMIN, 2L
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/statistics/users"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getUserStatsByRole()).thenReturn(stats);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getUserStatsByRole();
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get restaurant statistics by status")
    void testGetRestaurantStatistics_Success() throws IOException, URISyntaxException {
        // Arrange
        Map<RestaurantStatus, Long> stats = Map.of(
            RestaurantStatus.APPROVED, 45L,
            RestaurantStatus.PENDING, 8L,
            RestaurantStatus.SUSPENDED, 2L
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/statistics/restaurants"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getRestaurantStatsByStatus()).thenReturn(stats);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getRestaurantStatsByStatus();
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should get order statistics by status")
    void testGetOrderStatistics_Success() throws IOException, URISyntaxException {
        // Arrange
        Map<OrderStatus, Long> stats = Map.of(
            OrderStatus.PENDING, 10L,
            OrderStatus.CONFIRMED, 5L,
            OrderStatus.DELIVERED, 150L,
            OrderStatus.CANCELLED, 8L
        );
        
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/statistics/orders"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getOrderStatsByStatus()).thenReturn(stats);
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(adminService, times(1)).getOrderStatsByStatus();
        verify(exchange, times(1)).sendResponseHeaders(eq(200), anyLong());
    }

    @Test
    @DisplayName("Should return 404 for unknown endpoint")
    void testUnknownEndpoint_Returns404() throws IOException, URISyntaxException {
        // Arrange
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/unknown"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(exchange, times(1)).sendResponseHeaders(eq(404), anyLong());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void testIllegalArgumentException_Returns400() throws IOException, URISyntaxException {
        // Arrange
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/users"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getAllUsers(null, null, 0, 20)).thenThrow(new IllegalArgumentException("Invalid parameters"));
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(exchange, times(1)).sendResponseHeaders(eq(400), anyLong());
    }

    @Test
    @DisplayName("Should handle service exception with 500 status")
    void testServiceException_Returns500() throws IOException, URISyntaxException {
        // Arrange
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(new URI("/api/admin/dashboard"));
        when(exchange.getResponseBody()).thenReturn(responseStream);
        when(exchange.getResponseHeaders()).thenReturn(new com.sun.net.httpserver.Headers());
        when(adminService.getSystemStatistics()).thenThrow(new RuntimeException("Database error"));
        
        // Act
        adminController.handle(exchange);
        
        // Assert
        verify(exchange, times(1)).sendResponseHeaders(eq(500), anyLong());
    }
}

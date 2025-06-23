package com.myapp.admin;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Admin Dashboard
 * 
 * Endpoints:
 * GET    /api/admin/dashboard                          - Get dashboard statistics
 * GET    /api/admin/users                              - Get all users with filtering
 * GET    /api/admin/users/{userId}                     - Get user by ID
 * PUT    /api/admin/users/{userId}/status              - Update user status
 * GET    /api/admin/restaurants                        - Get all restaurants with filtering
 * GET    /api/admin/restaurants/{restaurantId}         - Get restaurant by ID
 * PUT    /api/admin/restaurants/{restaurantId}/status  - Update restaurant status
 * GET    /api/admin/orders                             - Get all orders with filtering
 * GET    /api/admin/orders/{orderId}                   - Get order by ID
 * PUT    /api/admin/orders/{orderId}/status            - Update order status
 * GET    /api/admin/transactions                       - Get all transactions with filtering
 * GET    /api/admin/transactions/{transactionId}       - Get transaction by ID
 * GET    /api/admin/deliveries                         - Get all deliveries with filtering
 * GET    /api/admin/deliveries/{deliveryId}            - Get delivery by ID
 * GET    /api/admin/statistics/daily                   - Get daily statistics
 * GET    /api/admin/statistics/users                   - Get user statistics by role
 * GET    /api/admin/statistics/restaurants             - Get restaurant statistics by status
 * GET    /api/admin/statistics/orders                  - Get order statistics by status
 */
public class AdminController implements HttpHandler {
    
    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            if (path.equals("/api/admin/dashboard") && "GET".equals(method)) {
                getDashboardStatistics(exchange);
            } else if (path.equals("/api/admin/users") && "GET".equals(method)) {
                getAllUsers(exchange);
            } else if (path.matches("/api/admin/users/\\d+") && "GET".equals(method)) {
                getUserById(exchange);
            } else if (path.matches("/api/admin/users/\\d+/status") && "PUT".equals(method)) {
                updateUserStatus(exchange);
            } else if (path.equals("/api/admin/restaurants") && "GET".equals(method)) {
                getAllRestaurants(exchange);
            } else if (path.matches("/api/admin/restaurants/\\d+") && "GET".equals(method)) {
                getRestaurantById(exchange);
            } else if (path.matches("/api/admin/restaurants/\\d+/status") && "PUT".equals(method)) {
                updateRestaurantStatus(exchange);
            } else if (path.equals("/api/admin/orders") && "GET".equals(method)) {
                getAllOrders(exchange);
            } else if (path.matches("/api/admin/orders/\\d+") && "GET".equals(method)) {
                getOrderById(exchange);
            } else if (path.matches("/api/admin/orders/\\d+/status") && "PUT".equals(method)) {
                updateOrderStatus(exchange);
            } else if (path.equals("/api/admin/transactions") && "GET".equals(method)) {
                getAllTransactions(exchange);
            } else if (path.matches("/api/admin/transactions/\\d+") && "GET".equals(method)) {
                getTransactionById(exchange);
            } else if (path.equals("/api/admin/deliveries") && "GET".equals(method)) {
                getAllDeliveries(exchange);
            } else if (path.matches("/api/admin/deliveries/\\d+") && "GET".equals(method)) {
                getDeliveryById(exchange);
            } else if (path.equals("/api/admin/statistics/daily") && "GET".equals(method)) {
                getDailyStatistics(exchange);
            } else if (path.equals("/api/admin/statistics/users") && "GET".equals(method)) {
                getUserStatistics(exchange);
            } else if (path.equals("/api/admin/statistics/restaurants") && "GET".equals(method)) {
                getRestaurantStatistics(exchange);
            } else if (path.equals("/api/admin/statistics/orders") && "GET".equals(method)) {
                getOrderStatistics(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            }
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    // ==================== DASHBOARD ====================
    
    /**
     * GET /api/admin/dashboard - Get dashboard statistics
     */
    private void getDashboardStatistics(HttpExchange exchange) throws IOException {
        AdminRepository.SystemStatistics stats = adminService.getSystemStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", stats.getTotalUsers());
        response.put("totalRestaurants", stats.getTotalRestaurants());
        response.put("totalOrders", stats.getTotalOrders());
        response.put("totalDeliveries", stats.getTotalDeliveries());
        response.put("totalRevenue", stats.getTotalRevenue());
        response.put("totalRefunds", stats.getTotalRefunds());
        response.put("todayOrders", stats.getTodayOrders());
        response.put("todayRevenue", stats.getTodayRevenue());
        response.put("activeRestaurants", stats.getActiveRestaurants());
        response.put("pendingOrders", stats.getPendingOrders());
        response.put("activeDeliveries", stats.getActiveDeliveries());
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== USER MANAGEMENT ====================
    
    /**
     * GET /api/admin/users - Get all users with filtering
     */
    private void getAllUsers(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String role = params.get("role");
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<User> users = adminService.getAllUsers(searchTerm, role, page, size);
        Long totalCount = adminService.countUsers(searchTerm, role);
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/users/{userId} - Get user by ID
     */
    private void getUserById(HttpExchange exchange) throws IOException {
        Long userId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/users/");
        User user = adminService.getUserById(userId);
        
        String responseBody = JsonUtil.toJson(user);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * PUT /api/admin/users/{userId}/status - Update user status
     */
    private void updateUserStatus(HttpExchange exchange) throws IOException {
        Long userId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/users/", "/status");
        
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        Boolean isActive = (Boolean) request.get("isActive");
        Long adminId = extractLong(request, "adminId");
        
        if (isActive == null) {
            throw new IllegalArgumentException("isActive field is required");
        }
        
        adminService.updateUserStatus(userId, isActive, adminId);
        
        sendResponse(exchange, 200, "{\"message\":\"User status updated successfully\"}");
    }

    // ==================== RESTAURANT MANAGEMENT ====================
    
    /**
     * GET /api/admin/restaurants - Get all restaurants with filtering
     */
    private void getAllRestaurants(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Restaurant> restaurants = adminService.getAllRestaurants(searchTerm, status, page, size);
        Long totalCount = adminService.countRestaurants(searchTerm, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("restaurants", restaurants);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/restaurants/{restaurantId} - Get restaurant by ID
     */
    private void getRestaurantById(HttpExchange exchange) throws IOException {
        Long restaurantId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/restaurants/");
        Restaurant restaurant = adminService.getRestaurantById(restaurantId);
        
        String responseBody = JsonUtil.toJson(restaurant);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * PUT /api/admin/restaurants/{restaurantId}/status - Update restaurant status
     */
    private void updateRestaurantStatus(HttpExchange exchange) throws IOException {
        Long restaurantId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/restaurants/", "/status");
        
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        String status = (String) request.get("status");
        Long adminId = extractLong(request, "adminId");
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("status field is required");
        }
        
        RestaurantStatus statusEnum;
        try {
            statusEnum = RestaurantStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid restaurant status: " + status);
        }
        
        adminService.updateRestaurantStatus(restaurantId, statusEnum, adminId);
        
        sendResponse(exchange, 200, "{\"message\":\"Restaurant status updated successfully\"}");
    }

    // ==================== ORDER MANAGEMENT ====================
    
    /**
     * GET /api/admin/orders - Get all orders with filtering
     */
    private void getAllOrders(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        Long customerId = parseLongParam(params.get("customerId"));
        Long restaurantId = parseLongParam(params.get("restaurantId"));
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Order> orders = adminService.getAllOrders(searchTerm, status, customerId, restaurantId, page, size);
        Long totalCount = adminService.countOrders(searchTerm, status, customerId, restaurantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/orders/{orderId} - Get order by ID
     */
    private void getOrderById(HttpExchange exchange) throws IOException {
        Long orderId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/orders/");
        Order order = adminService.getOrderById(orderId);
        
        String responseBody = JsonUtil.toJson(order);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * PUT /api/admin/orders/{orderId}/status - Update order status
     */
    private void updateOrderStatus(HttpExchange exchange) throws IOException {
        Long orderId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/orders/", "/status");
        
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        String status = (String) request.get("status");
        Long adminId = extractLong(request, "adminId");
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("status field is required");
        }
        
        OrderStatus statusEnum;
        try {
            statusEnum = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        
        adminService.updateOrderStatus(orderId, statusEnum, adminId);
        
        sendResponse(exchange, 200, "{\"message\":\"Order status updated successfully\"}");
    }

    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * GET /api/admin/transactions - Get all transactions with filtering
     */
    private void getAllTransactions(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        String type = params.get("type");
        Long userId = parseLongParam(params.get("userId"));
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Transaction> transactions = adminService.getAllTransactions(searchTerm, status, type, userId, page, size);
        Long totalCount = adminService.countTransactions(searchTerm, status, type, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/transactions/{transactionId} - Get transaction by ID
     */
    private void getTransactionById(HttpExchange exchange) throws IOException {
        Long transactionId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/transactions/");
        Transaction transaction = adminService.getTransactionById(transactionId);
        
        String responseBody = JsonUtil.toJson(transaction);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== DELIVERY MANAGEMENT ====================
    
    /**
     * GET /api/admin/deliveries - Get all deliveries with filtering
     */
    private void getAllDeliveries(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        Long courierId = parseLongParam(params.get("courierId"));
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Delivery> deliveries = adminService.getAllDeliveries(searchTerm, status, courierId, page, size);
        Long totalCount = adminService.countDeliveries(searchTerm, status, courierId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deliveries", deliveries);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/deliveries/{deliveryId} - Get delivery by ID
     */
    private void getDeliveryById(HttpExchange exchange) throws IOException {
        Long deliveryId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/deliveries/");
        Delivery delivery = adminService.getDeliveryById(deliveryId);
        
        String responseBody = JsonUtil.toJson(delivery);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== STATISTICS ====================
    
    /**
     * GET /api/admin/statistics/daily - Get daily statistics
     */
    private void getDailyStatistics(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        int days = parseIntParam(params.get("days"), 7);
        
        List<AdminRepository.DailyStatistics> stats = adminService.getDailyStatistics(days);
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/statistics/users - Get user statistics by role
     */
    private void getUserStatistics(HttpExchange exchange) throws IOException {
        Map<User.Role, Long> stats = adminService.getUserStatsByRole();
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/statistics/restaurants - Get restaurant statistics by status
     */
    private void getRestaurantStatistics(HttpExchange exchange) throws IOException {
        Map<RestaurantStatus, Long> stats = adminService.getRestaurantStatsByStatus();
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/statistics/orders - Get order statistics by status
     */
    private void getOrderStatistics(HttpExchange exchange) throws IOException {
        Map<OrderStatus, Long> stats = adminService.getOrderStatsByStatus();
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== HELPER METHODS ====================
    
    /**
     * Parse query parameters from query string
     */
    private Map<String, String> parseQueryParameters(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], "");
                }
            }
        }
        return params;
    }
    
    /**
     * Extract ID from path
     */
    private Long extractIdFromPath(String path, String prefix) {
        String idStr = path.substring(prefix.length());
        return Long.parseLong(idStr);
    }
    
    /**
     * Extract ID from path with suffix
     */
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String pathWithoutPrefix = path.substring(prefix.length());
        String idStr = pathWithoutPrefix.substring(0, pathWithoutPrefix.length() - suffix.length());
        return Long.parseLong(idStr);
    }
    
    /**
     * Parse integer parameter with default value
     */
    private int parseIntParam(String param, int defaultValue) {
        if (param == null || param.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Parse Long parameter
     */
    private Long parseLongParam(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(param);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Extract Long value from request map
     */
    private Long extractLong(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid " + key + " format");
            }
        }
        throw new IllegalArgumentException("Invalid " + key + " type");
    }
    
    /**
     * Send HTTP response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
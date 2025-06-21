package com.myapp.order;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Order;
import com.myapp.common.models.OrderStatus;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Order and Shopping Cart Management
 * 
 * Endpoints:
 * POST   /api/orders                           - Create new order (shopping cart)
 * GET    /api/orders/{orderId}                 - Get order details
 * POST   /api/orders/{orderId}/items           - Add item to cart
 * PUT    /api/orders/{orderId}/items/{itemId}  - Update item quantity in cart
 * DELETE /api/orders/{orderId}/items/{itemId}  - Remove item from cart
 * POST   /api/orders/{orderId}/place           - Place order (checkout)
 * PUT    /api/orders/{orderId}/cancel          - Cancel order
 * PUT    /api/orders/{orderId}/status          - Update order status
 * GET    /api/orders/customer/{customerId}     - Get customer orders
 * GET    /api/orders/restaurant/{restaurantId} - Get restaurant orders
 * GET    /api/orders/status/{status}           - Get orders by status
 * GET    /api/orders/active                    - Get active orders
 * GET    /api/orders/pending                   - Get pending orders
 * GET    /api/orders/customer/{customerId}/statistics - Get customer order statistics
 */
public class OrderController implements HttpHandler {
    
    private final OrderService orderService;
    
    public OrderController() {
        this.orderService = new OrderService(
            new OrderRepository(), 
            new ItemRepository(), 
            new RestaurantRepository()
        );
    }
    
    // Constructor for dependency injection (testing)
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange, path);
                    break;
                case "PUT":
                    handlePut(exchange, path);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/orders/\\d+")) {
            // GET /api/orders/{orderId} - Get order details
            Long orderId = extractOrderIdFromPath(path);
            getOrderDetails(exchange, orderId);
        } else if (path.matches("/api/orders/customer/\\d+")) {
            // GET /api/orders/customer/{customerId} - Get customer orders
            Long customerId = extractIdFromPath(path, "/api/orders/customer/");
            getCustomerOrders(exchange, customerId);
        } else if (path.matches("/api/orders/restaurant/\\d+")) {
            // GET /api/orders/restaurant/{restaurantId} - Get restaurant orders
            Long restaurantId = extractIdFromPath(path, "/api/orders/restaurant/");
            getRestaurantOrders(exchange, restaurantId);
        } else if (path.matches("/api/orders/status/\\w+")) {
            // GET /api/orders/status/{status} - Get orders by status
            String statusStr = extractStatusFromPath(path);
            getOrdersByStatus(exchange, statusStr);
        } else if (path.equals("/api/orders/active")) {
            // GET /api/orders/active - Get active orders
            getActiveOrders(exchange);
        } else if (path.equals("/api/orders/pending")) {
            // GET /api/orders/pending - Get pending orders
            getPendingOrders(exchange);
        } else if (path.matches("/api/orders/customer/\\d+/statistics")) {
            // GET /api/orders/customer/{customerId}/statistics - Get customer statistics
            Long customerId = extractIdFromPath(path, "/api/orders/customer/", "/statistics");
            getCustomerStatistics(exchange, customerId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void getOrderDetails(HttpExchange exchange, Long orderId) throws IOException {
        Order order = orderService.getOrder(orderId);
        sendJsonResponse(exchange, 200, order);
    }
    
    private void getCustomerOrders(HttpExchange exchange, Long customerId) throws IOException {
        List<Order> orders = orderService.getCustomerOrders(customerId);
        sendJsonResponse(exchange, 200, orders);
    }
    
    private void getRestaurantOrders(HttpExchange exchange, Long restaurantId) throws IOException {
        List<Order> orders = orderService.getRestaurantOrders(restaurantId);
        sendJsonResponse(exchange, 200, orders);
    }
    
    private void getOrdersByStatus(HttpExchange exchange, String statusStr) throws IOException {
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(status);
            sendJsonResponse(exchange, 200, orders);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    private void getActiveOrders(HttpExchange exchange) throws IOException {
        List<Order> orders = orderService.getActiveOrders();
        sendJsonResponse(exchange, 200, orders);
    }
    
    private void getPendingOrders(HttpExchange exchange) throws IOException {
        List<Order> orders = orderService.getPendingOrders();
        sendJsonResponse(exchange, 200, orders);
    }
    
    private void getCustomerStatistics(HttpExchange exchange, Long customerId) throws IOException {
        OrderService.OrderStatistics stats = orderService.getCustomerOrderStatistics(customerId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/orders")) {
            // POST /api/orders - Create new order (shopping cart)
            createOrder(exchange);
        } else if (path.matches("/api/orders/\\d+/items")) {
            // POST /api/orders/{orderId}/items - Add item to cart
            Long orderId = extractOrderIdFromPath(path, "/items");
            addItemToCart(exchange, orderId);
        } else if (path.matches("/api/orders/\\d+/place")) {
            // POST /api/orders/{orderId}/place - Place order (checkout)
            Long orderId = extractOrderIdFromPath(path, "/place");
            placeOrder(exchange, orderId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void createOrder(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long customerId = getLongFromMap(requestData, "customerId");
        Long restaurantId = getLongFromMap(requestData, "restaurantId");
        String deliveryAddress = getStringFromMap(requestData, "deliveryAddress");
        String phone = getStringFromMap(requestData, "phone");
        
        Order order = orderService.createOrder(customerId, restaurantId, deliveryAddress, phone);
        sendJsonResponse(exchange, 201, order);
    }
    
    private void addItemToCart(HttpExchange exchange, Long orderId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long itemId = getLongFromMap(requestData, "itemId");
        Integer quantity = getIntegerFromMap(requestData, "quantity");
        
        if (quantity == null) {
            sendErrorResponse(exchange, 400, "Quantity is required");
            return;
        }
        
        Order order = orderService.addItemToCart(orderId, itemId, quantity);
        sendJsonResponse(exchange, 200, order);
    }
    
    private void placeOrder(HttpExchange exchange, Long orderId) throws IOException {
        Order order = orderService.placeOrder(orderId);
        sendJsonResponse(exchange, 200, order);
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/orders/\\d+/items/\\d+")) {
            // PUT /api/orders/{orderId}/items/{itemId} - Update item quantity in cart
            Long orderId = extractOrderIdFromPath(path, "/items/\\d+");
            Long itemId = extractItemIdFromPath(path);
            updateItemQuantity(exchange, orderId, itemId);
        } else if (path.matches("/api/orders/\\d+/cancel")) {
            // PUT /api/orders/{orderId}/cancel - Cancel order
            Long orderId = extractOrderIdFromPath(path, "/cancel");
            cancelOrder(exchange, orderId);
        } else if (path.matches("/api/orders/\\d+/status")) {
            // PUT /api/orders/{orderId}/status - Update order status
            Long orderId = extractOrderIdFromPath(path, "/status");
            updateOrderStatus(exchange, orderId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void updateItemQuantity(HttpExchange exchange, Long orderId, Long itemId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        Integer quantity = getIntegerFromMap(requestData, "quantity");
        
        if (quantity == null) {
            sendErrorResponse(exchange, 400, "Quantity is required");
            return;
        }
        
        Order order = orderService.updateItemQuantity(orderId, itemId, quantity);
        sendJsonResponse(exchange, 200, order);
    }
    
    private void cancelOrder(HttpExchange exchange, Long orderId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        String reason = getStringFromMap(requestData, "reason");
        
        Order order = orderService.cancelOrder(orderId, reason);
        sendJsonResponse(exchange, 200, order);
    }
    
    private void updateOrderStatus(HttpExchange exchange, Long orderId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        String statusStr = getStringFromMap(requestData, "status");
        
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            Order order = orderService.updateOrderStatus(orderId, status);
            sendJsonResponse(exchange, 200, order);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/orders/\\d+/items/\\d+")) {
            // DELETE /api/orders/{orderId}/items/{itemId} - Remove item from cart
            Long orderId = extractOrderIdFromPath(path, "/items/\\d+");
            Long itemId = extractItemIdFromPath(path);
            removeItemFromCart(exchange, orderId, itemId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void removeItemFromCart(HttpExchange exchange, Long orderId, Long itemId) throws IOException {
        Order order = orderService.removeItemFromCart(orderId, itemId);
        sendJsonResponse(exchange, 200, order);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private Long extractOrderIdFromPath(String path) {
        // Extract from /api/orders/{orderId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[3]); // /api/orders/{orderId}
    }
    
    private Long extractOrderIdFromPath(String path, String suffix) {
        // Extract from /api/orders/{orderId}/suffix
        String pathWithoutSuffix = path.substring(0, path.lastIndexOf(suffix));
        String[] parts = pathWithoutSuffix.split("/");
        return Long.parseLong(parts[3]);
    }
    
    private Long extractItemIdFromPath(String path) {
        // Extract from /api/orders/{orderId}/items/{itemId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[5]); // /api/orders/{orderId}/items/{itemId}
    }
    
    private Long extractIdFromPath(String path, String prefix) {
        String idStr = path.substring(prefix.length());
        return Long.parseLong(idStr);
    }
    
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String withoutPrefix = path.substring(prefix.length());
        String idStr = withoutPrefix.substring(0, withoutPrefix.indexOf(suffix));
        return Long.parseLong(idStr);
    }
    
    private String extractStatusFromPath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
    
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        // Simple JSON parsing - in production, use Jackson or Gson
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> result = new HashMap<>();
        
        // Basic JSON parsing (simplified for this implementation)
        if (requestBody.trim().isEmpty()) {
            return result;
        }
        
        // Remove curly braces and split by comma
        String content = requestBody.trim().replaceAll("[{}]", "");
        String[] pairs = content.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                
                // Try to parse as number or boolean
                try {
                    if (value.equals("true") || value.equals("false")) {
                        result.put(key, Boolean.parseBoolean(value));
                    } else if (value.contains(".")) {
                        result.put(key, Double.parseDouble(value));
                    } else {
                        result.put(key, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }
    
    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
        }
    }
    
    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = convertToJson(data);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = Map.of(
            "error", true,
            "message", message,
            "status", statusCode
        );
        sendJsonResponse(exchange, statusCode, error);
    }
    
    private String convertToJson(Object data) {
        // Simple JSON serialization - in production, use Jackson or Gson
        if (data == null) {
            return "null";
        }
        
        if (data instanceof String) {
            return "\"" + data + "\"";
        }
        
        if (data instanceof Number || data instanceof Boolean) {
            return data.toString();
        }
        
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(convertToJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                sb.append(convertToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        
        // For complex objects like Order, use reflection-based serialization
        return serializeObject(data);
    }
    
    private String serializeObject(Object obj) {
        // Simple object serialization for Order and other entities
        if (obj instanceof Order) {
            Order order = (Order) obj;
            return String.format(
                "{\"id\":%d,\"customerId\":%d,\"restaurantId\":%d,\"status\":\"%s\",\"totalAmount\":%.2f,\"deliveryAddress\":\"%s\",\"phone\":\"%s\",\"orderDate\":\"%s\",\"itemCount\":%d}",
                order.getId(), order.getCustomer().getId(), order.getRestaurant().getId(),
                order.getStatus(), order.getTotalAmount(), order.getDeliveryAddress(),
                order.getPhone(), order.getOrderDate(), order.getOrderItems().size()
            );
        }
        
        if (obj instanceof OrderService.OrderStatistics) {
            OrderService.OrderStatistics stats = (OrderService.OrderStatistics) obj;
            return String.format(
                "{\"totalOrders\":%d,\"completedOrders\":%d,\"cancelledOrders\":%d,\"totalSpent\":%.2f,\"activeOrders\":%d,\"averageOrderValue\":%.2f}",
                stats.getTotalOrders(), stats.getCompletedOrders(), stats.getCancelledOrders(),
                stats.getTotalSpent(), stats.getActiveOrders(), stats.getAverageOrderValue()
            );
        }
        
        // Fallback to toString
        return "\"" + obj.toString() + "\"";
    }
}

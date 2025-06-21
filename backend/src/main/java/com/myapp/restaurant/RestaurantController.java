package com.myapp.restaurant;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Restaurant Management
 * 
 * Endpoints:
 * POST   /api/restaurants                    - Register new restaurant
 * GET    /api/restaurants                    - Get all approved restaurants (public)
 * GET    /api/restaurants/{id}               - Get restaurant by ID
 * PUT    /api/restaurants/{id}               - Update restaurant information
 * DELETE /api/restaurants/{id}               - Delete restaurant
 * GET    /api/restaurants/owner/{ownerId}    - Get restaurants by owner
 * GET    /api/restaurants/status/{status}    - Get restaurants by status
 * PUT    /api/restaurants/{id}/status        - Update restaurant status (admin)
 * GET    /api/restaurants/statistics         - Get restaurant statistics (admin)
 * POST   /api/restaurants/{id}/approve       - Approve restaurant (admin)
 * POST   /api/restaurants/{id}/reject        - Reject restaurant (admin)
 * POST   /api/restaurants/{id}/suspend       - Suspend restaurant (admin)
 */
public class RestaurantController implements HttpHandler {
    
    private final RestaurantService restaurantService;
    
    public RestaurantController() {
        this.restaurantService = new RestaurantService();
    }
    
    // Constructor for dependency injection (testing)
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
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
        if (path.equals("/api/restaurants")) {
            // GET /api/restaurants - Get all approved restaurants
            getAllApprovedRestaurants(exchange);
        } else if (path.matches("/api/restaurants/\\d+")) {
            // GET /api/restaurants/{id} - Get restaurant by ID
            Long id = extractIdFromPath(path);
            getRestaurantById(exchange, id);
        } else if (path.matches("/api/restaurants/owner/\\d+")) {
            // GET /api/restaurants/owner/{ownerId} - Get restaurants by owner
            Long ownerId = extractIdFromPath(path, "/api/restaurants/owner/");
            getRestaurantsByOwner(exchange, ownerId);
        } else if (path.matches("/api/restaurants/status/\\w+")) {
            // GET /api/restaurants/status/{status} - Get restaurants by status
            String statusStr = extractStatusFromPath(path);
            getRestaurantsByStatus(exchange, statusStr);
        } else if (path.equals("/api/restaurants/statistics")) {
            // GET /api/restaurants/statistics - Get restaurant statistics
            getRestaurantStatistics(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void getAllApprovedRestaurants(HttpExchange exchange) throws IOException {
        List<Restaurant> restaurants = restaurantService.getApprovedRestaurants();
        sendJsonResponse(exchange, 200, restaurants);
    }
    
    private void getRestaurantById(HttpExchange exchange, Long id) throws IOException {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        sendJsonResponse(exchange, 200, restaurant);
    }
    
    private void getRestaurantsByOwner(HttpExchange exchange, Long ownerId) throws IOException {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        sendJsonResponse(exchange, 200, restaurants);
    }
    
    private void getRestaurantsByStatus(HttpExchange exchange, String statusStr) throws IOException {
        try {
            RestaurantStatus status = RestaurantStatus.valueOf(statusStr.toUpperCase());
            List<Restaurant> restaurants = restaurantService.getRestaurantsByStatus(status);
            sendJsonResponse(exchange, 200, restaurants);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    private void getRestaurantStatistics(HttpExchange exchange) throws IOException {
        RestaurantService.RestaurantStatistics stats = restaurantService.getRestaurantStatistics();
        sendJsonResponse(exchange, 200, stats);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/restaurants")) {
            // POST /api/restaurants - Register new restaurant
            registerRestaurant(exchange);
        } else if (path.matches("/api/restaurants/\\d+/approve")) {
            // POST /api/restaurants/{id}/approve - Approve restaurant
            Long id = extractIdFromPath(path, "/api/restaurants/", "/approve");
            approveRestaurant(exchange, id);
        } else if (path.matches("/api/restaurants/\\d+/reject")) {
            // POST /api/restaurants/{id}/reject - Reject restaurant
            Long id = extractIdFromPath(path, "/api/restaurants/", "/reject");
            rejectRestaurant(exchange, id);
        } else if (path.matches("/api/restaurants/\\d+/suspend")) {
            // POST /api/restaurants/{id}/suspend - Suspend restaurant
            Long id = extractIdFromPath(path, "/api/restaurants/", "/suspend");
            suspendRestaurant(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void registerRestaurant(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long ownerId = getLongFromMap(requestData, "ownerId");
        String name = getStringFromMap(requestData, "name");
        String address = getStringFromMap(requestData, "address");
        String phone = getStringFromMap(requestData, "phone");
        
        Restaurant restaurant = restaurantService.registerRestaurant(ownerId, name, address, phone);
        sendJsonResponse(exchange, 201, restaurant);
    }
    
    private void approveRestaurant(HttpExchange exchange, Long id) throws IOException {
        restaurantService.approveRestaurant(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Restaurant approved successfully"));
    }
    
    private void rejectRestaurant(HttpExchange exchange, Long id) throws IOException {
        restaurantService.rejectRestaurant(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Restaurant rejected successfully"));
    }
    
    private void suspendRestaurant(HttpExchange exchange, Long id) throws IOException {
        restaurantService.suspendRestaurant(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Restaurant suspended successfully"));
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/restaurants/\\d+")) {
            // PUT /api/restaurants/{id} - Update restaurant information
            Long id = extractIdFromPath(path);
            updateRestaurant(exchange, id);
        } else if (path.matches("/api/restaurants/\\d+/status")) {
            // PUT /api/restaurants/{id}/status - Update restaurant status
            Long id = extractIdFromPath(path, "/api/restaurants/", "/status");
            updateRestaurantStatus(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void updateRestaurant(HttpExchange exchange, Long id) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String name = getStringFromMap(requestData, "name");
        String address = getStringFromMap(requestData, "address");
        String phone = getStringFromMap(requestData, "phone");
        
        Restaurant restaurant = restaurantService.updateRestaurant(id, name, address, phone);
        sendJsonResponse(exchange, 200, restaurant);
    }
    
    private void updateRestaurantStatus(HttpExchange exchange, Long id) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        String statusStr = getStringFromMap(requestData, "status");
        
        try {
            RestaurantStatus status = RestaurantStatus.valueOf(statusStr.toUpperCase());
            restaurantService.updateRestaurantStatus(id, status);
            sendJsonResponse(exchange, 200, Map.of("message", "Restaurant status updated successfully"));
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/restaurants/\\d+")) {
            // DELETE /api/restaurants/{id} - Delete restaurant
            Long id = extractIdFromPath(path);
            deleteRestaurant(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void deleteRestaurant(HttpExchange exchange, Long id) throws IOException {
        restaurantService.deleteRestaurant(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Restaurant deleted successfully"));
    }
    
    // ==================== UTILITY METHODS ====================
    
    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[parts.length - 1]);
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
                
                // Try to parse as number
                try {
                    if (value.contains(".")) {
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
        
        // For complex objects like Restaurant, use reflection-based serialization
        return serializeObject(data);
    }
    
    private String serializeObject(Object obj) {
        // Simple object serialization for Restaurant and other entities
        if (obj instanceof Restaurant) {
            Restaurant r = (Restaurant) obj;
            return String.format(
                "{\"id\":%d,\"ownerId\":%d,\"name\":\"%s\",\"address\":\"%s\",\"phone\":\"%s\",\"status\":\"%s\"}",
                r.getId(), r.getOwnerId(), r.getName(), r.getAddress(), r.getPhone(), r.getStatus()
            );
        }
        
        if (obj instanceof RestaurantService.RestaurantStatistics) {
            RestaurantService.RestaurantStatistics stats = (RestaurantService.RestaurantStatistics) obj;
            return String.format(
                "{\"totalCount\":%d,\"approvedCount\":%d,\"pendingCount\":%d,\"rejectedCount\":%d,\"suspendedCount\":%d}",
                stats.getTotalCount(), stats.getApprovedCount(), stats.getPendingCount(), 
                stats.getRejectedCount(), stats.getSuspendedCount()
            );
        }
        
        // Fallback to toString
        return "\"" + obj.toString() + "\"";
    }
}

package com.myapp.menu;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Menu Management
 * 
 * Endpoints:
 * GET    /api/menus/restaurant/{restaurantId}                 - Get restaurant menu
 * GET    /api/menus/restaurant/{restaurantId}/available       - Get available menu items
 * POST   /api/menus/restaurant/{restaurantId}/items           - Add item to menu
 * PUT    /api/menus/items/{itemId}                           - Update menu item
 * DELETE /api/menus/items/{itemId}                           - Remove item from menu
 * PUT    /api/menus/items/{itemId}/availability              - Set item availability
 * PUT    /api/menus/items/{itemId}/quantity                  - Update item quantity
 * GET    /api/menus/restaurant/{restaurantId}/categories     - Get menu categories
 * GET    /api/menus/restaurant/{restaurantId}/category/{cat} - Get menu by category
 * GET    /api/menus/restaurant/{restaurantId}/low-stock      - Get low stock items
 * GET    /api/menus/restaurant/{restaurantId}/statistics     - Get menu statistics
 */
public class MenuController implements HttpHandler {
    
    private final MenuService menuService;
    
    public MenuController() {
        this.menuService = new MenuService();
    }
    
    // Constructor for dependency injection (testing)
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
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
        if (path.matches("/api/menus/restaurant/\\d+")) {
            // GET /api/menus/restaurant/{restaurantId} - Get restaurant menu
            Long restaurantId = extractRestaurantIdFromPath(path);
            getRestaurantMenu(exchange, restaurantId);
        } else if (path.matches("/api/menus/restaurant/\\d+/available")) {
            // GET /api/menus/restaurant/{restaurantId}/available - Get available menu
            Long restaurantId = extractRestaurantIdFromPath(path, "/available");
            getAvailableMenu(exchange, restaurantId);
        } else if (path.matches("/api/menus/restaurant/\\d+/categories")) {
            // GET /api/menus/restaurant/{restaurantId}/categories - Get menu categories
            Long restaurantId = extractRestaurantIdFromPath(path, "/categories");
            getMenuCategories(exchange, restaurantId);
        } else if (path.matches("/api/menus/restaurant/\\d+/category/[^/]+")) {
            // GET /api/menus/restaurant/{restaurantId}/category/{category} - Get menu by category
            Long restaurantId = extractRestaurantIdFromPath(path, "/category/[^/]+");
            String category = extractCategoryFromPath(path);
            getMenuByCategory(exchange, restaurantId, category);
        } else if (path.matches("/api/menus/restaurant/\\d+/low-stock")) {
            // GET /api/menus/restaurant/{restaurantId}/low-stock - Get low stock items
            Long restaurantId = extractRestaurantIdFromPath(path, "/low-stock");
            String thresholdParam = getQueryParameter(exchange, "threshold");
            int threshold = thresholdParam != null ? Integer.parseInt(thresholdParam) : 10;
            getLowStockItems(exchange, restaurantId, threshold);
        } else if (path.matches("/api/menus/restaurant/\\d+/statistics")) {
            // GET /api/menus/restaurant/{restaurantId}/statistics - Get menu statistics
            Long restaurantId = extractRestaurantIdFromPath(path, "/statistics");
            getMenuStatistics(exchange, restaurantId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void getRestaurantMenu(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> menu = menuService.getRestaurantMenu(restaurantId);
        sendJsonResponse(exchange, 200, menu);
    }
    
    private void getAvailableMenu(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> menu = menuService.getAvailableMenu(restaurantId);
        sendJsonResponse(exchange, 200, menu);
    }
    
    private void getMenuCategories(HttpExchange exchange, Long restaurantId) throws IOException {
        List<String> categories = menuService.getMenuCategories(restaurantId);
        sendJsonResponse(exchange, 200, categories);
    }
    
    private void getMenuByCategory(HttpExchange exchange, Long restaurantId, String category) throws IOException {
        List<FoodItem> menu = menuService.getMenuByCategory(restaurantId, category);
        sendJsonResponse(exchange, 200, menu);
    }
    
    private void getLowStockItems(HttpExchange exchange, Long restaurantId, int threshold) throws IOException {
        List<FoodItem> items = menuService.getLowStockItems(restaurantId, threshold);
        sendJsonResponse(exchange, 200, items);
    }
    
    private void getMenuStatistics(HttpExchange exchange, Long restaurantId) throws IOException {
        MenuService.MenuStatistics stats = menuService.getMenuStatistics(restaurantId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/menus/restaurant/\\d+/items")) {
            // POST /api/menus/restaurant/{restaurantId}/items - Add item to menu
            Long restaurantId = extractRestaurantIdFromPath(path, "/items");
            addItemToMenu(exchange, restaurantId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void addItemToMenu(HttpExchange exchange, Long restaurantId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String name = getStringFromMap(requestData, "name");
        String description = getStringFromMap(requestData, "description");
        Double price = getDoubleFromMap(requestData, "price");
        String category = getStringFromMap(requestData, "category");
        
        FoodItem item = menuService.addItemToMenu(restaurantId, name, description, price, category);
        sendJsonResponse(exchange, 201, item);
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/menus/items/\\d+")) {
            // PUT /api/menus/items/{itemId} - Update menu item
            Long itemId = extractItemIdFromPath(path);
            updateMenuItem(exchange, itemId);
        } else if (path.matches("/api/menus/items/\\d+/availability")) {
            // PUT /api/menus/items/{itemId}/availability - Set item availability
            Long itemId = extractItemIdFromPath(path, "/availability");
            setItemAvailability(exchange, itemId);
        } else if (path.matches("/api/menus/items/\\d+/quantity")) {
            // PUT /api/menus/items/{itemId}/quantity - Update item quantity
            Long itemId = extractItemIdFromPath(path, "/quantity");
            updateItemQuantity(exchange, itemId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void updateMenuItem(HttpExchange exchange, Long itemId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String name = getStringFromMap(requestData, "name");
        String description = getStringFromMap(requestData, "description");
        Double price = getDoubleFromMap(requestData, "price");
        String category = getStringFromMap(requestData, "category");
        Integer quantity = getIntegerFromMap(requestData, "quantity");
        Boolean available = getBooleanFromMap(requestData, "available");
        
        FoodItem item = menuService.updateMenuItem(itemId, name, description, price, category, quantity, available);
        sendJsonResponse(exchange, 200, item);
    }
    
    private void setItemAvailability(HttpExchange exchange, Long itemId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        Boolean available = getBooleanFromMap(requestData, "available");
        
        if (available == null) {
            sendErrorResponse(exchange, 400, "Available field is required");
            return;
        }
        
        FoodItem item = menuService.setItemAvailability(itemId, available);
        sendJsonResponse(exchange, 200, item);
    }
    
    private void updateItemQuantity(HttpExchange exchange, Long itemId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        Integer quantity = getIntegerFromMap(requestData, "quantity");
        
        if (quantity == null) {
            sendErrorResponse(exchange, 400, "Quantity field is required");
            return;
        }
        
        FoodItem item = menuService.updateItemQuantity(itemId, quantity);
        sendJsonResponse(exchange, 200, item);
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/menus/items/\\d+")) {
            // DELETE /api/menus/items/{itemId} - Remove item from menu
            Long itemId = extractItemIdFromPath(path);
            removeItemFromMenu(exchange, itemId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void removeItemFromMenu(HttpExchange exchange, Long itemId) throws IOException {
        menuService.removeItemFromMenu(itemId);
        sendJsonResponse(exchange, 200, Map.of("message", "Item removed from menu successfully"));
    }
    
    // ==================== UTILITY METHODS ====================
    
    private Long extractRestaurantIdFromPath(String path) {
        // Extract from /api/menus/restaurant/{restaurantId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[4]); // /api/menus/restaurant/{restaurantId}
    }
    
    private Long extractRestaurantIdFromPath(String path, String suffix) {
        // Extract from /api/menus/restaurant/{restaurantId}/suffix
        String pathWithoutSuffix = path.substring(0, path.lastIndexOf(suffix));
        String[] parts = pathWithoutSuffix.split("/");
        return Long.parseLong(parts[4]);
    }
    
    private Long extractItemIdFromPath(String path) {
        // Extract from /api/menus/items/{itemId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[4]); // /api/menus/items/{itemId}
    }
    
    private Long extractItemIdFromPath(String path, String suffix) {
        // Extract from /api/menus/items/{itemId}/suffix
        String pathWithoutSuffix = path.substring(0, path.lastIndexOf(suffix));
        String[] parts = pathWithoutSuffix.split("/");
        return Long.parseLong(parts[4]);
    }
    
    private String extractCategoryFromPath(String path) {
        // Extract from /api/menus/restaurant/{restaurantId}/category/{category}
        String[] parts = path.split("/");
        return parts[6]; // /api/menus/restaurant/{restaurantId}/category/{category}
    }
    
    private String getQueryParameter(HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return null;
        
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
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
    
    private Double getDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
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
    
    private Boolean getBooleanFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
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
        
        // For complex objects like FoodItem, use reflection-based serialization
        return serializeObject(data);
    }
    
    private String serializeObject(Object obj) {
        // Simple object serialization for FoodItem and other entities
        if (obj instanceof FoodItem) {
            FoodItem item = (FoodItem) obj;
            return String.format(
                "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"price\":%.2f,\"category\":\"%s\",\"quantity\":%d,\"available\":%b,\"restaurantId\":%d}",
                item.getId(), item.getName(), item.getDescription(), item.getPrice(), 
                item.getCategory(), item.getQuantity(), item.getAvailable(), 
                item.getRestaurant().getId()
            );
        }
        
        if (obj instanceof MenuService.MenuStatistics) {
            MenuService.MenuStatistics stats = (MenuService.MenuStatistics) obj;
            return String.format(
                "{\"totalItems\":%d,\"availableItems\":%d,\"unavailableItems\":%d,\"outOfStockItems\":%d,\"lowStockItems\":%d,\"inStockItems\":%d,\"availabilityPercentage\":%.2f}",
                stats.getTotalItems(), stats.getAvailableItems(), stats.getUnavailableItems(),
                stats.getOutOfStockItems(), stats.getLowStockItems(), stats.getInStockItems(),
                stats.getAvailabilityPercentage()
            );
        }
        
        // Fallback to toString
        return "\"" + obj.toString() + "\"";
    }
} 
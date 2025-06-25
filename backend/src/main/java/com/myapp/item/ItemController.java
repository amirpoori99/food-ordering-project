package com.myapp.item;

import com.myapp.common.constants.ApplicationConstants;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.utils.JsonUtil;
import com.myapp.common.utils.MapParsingUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Food Item Management
 * 
 * Endpoints:
 * POST   /api/items                          - Add new food item
 * GET    /api/items/{id}                     - Get food item by ID
 * PUT    /api/items/{id}                     - Update food item
 * DELETE /api/items/{id}                     - Delete food item
 * PUT    /api/items/{id}/availability        - Update item availability
 * PUT    /api/items/{id}/quantity           - Update item quantity
 * GET    /api/items/restaurant/{restaurantId} - Get items by restaurant
 * GET    /api/items/restaurant/{restaurantId}/available - Get available items by restaurant
 * GET    /api/items/search                   - Search items by keyword
 * GET    /api/items/category/{category}      - Get items by category
 * GET    /api/items/restaurant/{restaurantId}/categories - Get categories by restaurant
 * GET    /api/items/restaurant/{restaurantId}/low-stock - Get low stock items
 * GET    /api/items/restaurant/{restaurantId}/statistics - Get menu statistics
 */
public class ItemController implements HttpHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;
    
    public ItemController() {
        this.itemService = new ItemService(new ItemRepository(), new com.myapp.restaurant.RestaurantRepository());
    }
    
    // Constructor for dependency injection (testing)
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
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
        if (path.matches("/api/items/\\d+")) {
            // GET /api/items/{id} - Get item by ID
            Long id = extractIdFromPath(path, "/api/items/");
            getItemById(exchange, id);
        } else if (path.matches("/api/items/restaurant/\\d+$")) {
            // GET /api/items/restaurant/{restaurantId} - Get items by restaurant
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/");
            getItemsByRestaurant(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/available")) {
            // GET /api/items/restaurant/{restaurantId}/available - Get available items
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/available");
            getAvailableItemsByRestaurant(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/categories")) {
            // GET /api/items/restaurant/{restaurantId}/categories - Get categories
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/categories");
            getCategoriesByRestaurant(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/low-stock")) {
            // GET /api/items/restaurant/{restaurantId}/low-stock - Get low stock items
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/low-stock");
            getLowStockItems(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/statistics")) {
            // GET /api/items/restaurant/{restaurantId}/statistics - Get menu statistics
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/statistics");
            getMenuStatistics(exchange, restaurantId);
        } else if (path.equals("/api/items/search")) {
            // GET /api/items/search?keyword={keyword} - Search items
            searchItems(exchange);
        } else if (path.matches("/api/items/category/.+")) {
            // GET /api/items/category/{category} - Get items by category
            String category = extractStringFromPath(path, "/api/items/category/");
            getItemsByCategory(exchange, category);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void getItemById(HttpExchange exchange, Long id) throws IOException {
        FoodItem item = itemService.getItem(id);
        sendJsonResponse(exchange, 200, item);
    }
    
    private void getItemsByRestaurant(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> items = itemService.getRestaurantItems(restaurantId);
        sendJsonResponse(exchange, 200, items);
    }
    
    private void getAvailableItemsByRestaurant(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> items = itemService.getAvailableItems(restaurantId);
        sendJsonResponse(exchange, 200, items);
    }
    
    private void getCategoriesByRestaurant(HttpExchange exchange, Long restaurantId) throws IOException {
        List<String> categories = itemService.getRestaurantCategories(restaurantId);
        Map<String, Object> response = Map.of("categories", categories);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getLowStockItems(HttpExchange exchange, Long restaurantId) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        int threshold = 5; // default threshold
        if (query != null && query.contains("threshold=")) {
            try {
                threshold = Integer.parseInt(extractQueryParam(query, "threshold"));
            } catch (NumberFormatException e) {
                // Use default
            }
        }
        
        List<FoodItem> items = itemService.getLowStockItems(restaurantId, threshold);
        Map<String, Object> response = Map.of("items", items, "threshold", threshold);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getMenuStatistics(HttpExchange exchange, Long restaurantId) throws IOException {
        ItemService.MenuStatistics stats = itemService.getMenuStatistics(restaurantId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    private void searchItems(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.contains("keyword=")) {
            sendErrorResponse(exchange, 400, "Missing keyword parameter");
            return;
        }
        
        String keyword = extractQueryParam(query, "keyword");
        List<FoodItem> items = itemService.searchItems(keyword);
        Map<String, Object> response = Map.of("items", items, "keyword", keyword);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getItemsByCategory(HttpExchange exchange, String category) throws IOException {
        List<FoodItem> items = itemService.getItemsByCategory(category);
        Map<String, Object> response = Map.of("items", items, "category", category);
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/items")) {
            // POST /api/items - Add new item
            addItem(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void addItem(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> requestData = parseJsonRequest(exchange);
            
            // Validate required fields
            MapParsingUtil.validateRequiredFields(requestData, "restaurantId", "name", "price", "category");
            
            Long restaurantId = MapParsingUtil.getLongFromMap(requestData, "restaurantId");
            String name = MapParsingUtil.getStringFromMap(requestData, "name");
            String description = MapParsingUtil.getOptionalStringFromMap(requestData, "description", "");
            Double price = MapParsingUtil.getDoubleFromMap(requestData, "price");
            String category = MapParsingUtil.getStringFromMap(requestData, "category");
            String imageUrl = MapParsingUtil.getOptionalStringFromMap(requestData, "imageUrl", null);
            Integer quantity = MapParsingUtil.getOptionalIntegerFromMap(requestData, "quantity", 1);
            
            logger.info("Adding new item: {} for restaurant: {}", name, restaurantId);
            FoodItem item = itemService.addItem(restaurantId, name, description, price, category, imageUrl, quantity);
            
            sendJsonResponse(exchange, ApplicationConstants.HTTP_STATUS.CREATED, item);
        } catch (Exception e) {
            logger.error("Error adding item: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/items/\\d+$")) {
            // PUT /api/items/{id} - Update item
            Long id = extractIdFromPath(path, "/api/items/");
            updateItem(exchange, id);
        } else if (path.matches("/api/items/\\d+/availability")) {
            // PUT /api/items/{id}/availability - Update availability
            Long id = extractIdFromPath(path, "/api/items/", "/availability");
            updateItemAvailability(exchange, id);
        } else if (path.matches("/api/items/\\d+/quantity")) {
            // PUT /api/items/{id}/quantity - Update quantity
            Long id = extractIdFromPath(path, "/api/items/", "/quantity");
            updateItemQuantity(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void updateItem(HttpExchange exchange, Long id) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String name = MapParsingUtil.getOptionalStringFromMap(requestData, "name", null);
        String description = MapParsingUtil.getOptionalStringFromMap(requestData, "description", null);
        Double price = requestData.containsKey("price") ? MapParsingUtil.getDoubleFromMap(requestData, "price") : null;
        String category = MapParsingUtil.getOptionalStringFromMap(requestData, "category", null);
        String imageUrl = MapParsingUtil.getOptionalStringFromMap(requestData, "imageUrl", null);
        Integer quantity = MapParsingUtil.getOptionalIntegerFromMap(requestData, "quantity", null);
        
        FoodItem item = itemService.updateItem(id, name, description, price, category, imageUrl, quantity);
        sendJsonResponse(exchange, 200, item);
    }
    
    private void updateItemAvailability(HttpExchange exchange, Long id) throws IOException {
        try {
            Map<String, Object> requestData = parseJsonRequest(exchange);
            Boolean available = MapParsingUtil.getBooleanFromMap(requestData, "available");
            
            logger.info("Updating availability for item {} to {}", id, available);
            itemService.updateAvailability(id, available);
            
            sendJsonResponse(exchange, ApplicationConstants.HTTP_STATUS.OK, 
                Map.of("message", ApplicationConstants.SUCCESS_MESSAGES.ITEM_UPDATED));
        } catch (Exception e) {
            logger.error("Error updating item availability: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void updateItemQuantity(HttpExchange exchange, Long id) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        Integer quantity = MapParsingUtil.getIntegerFromMap(requestData, "quantity");
        
        itemService.updateQuantity(id, quantity);
        sendJsonResponse(exchange, 200, Map.of("message", "Item quantity updated successfully"));
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/items/\\d+")) {
            // DELETE /api/items/{id} - Delete item
            Long id = extractIdFromPath(path, "/api/items/");
            deleteItem(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void deleteItem(HttpExchange exchange, Long id) throws IOException {
        itemService.deleteItem(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Item deleted successfully"));
    }
    
    // ==================== UTILITY METHODS ====================
    
    private Long extractIdFromPath(String path, String prefix) {
        return Long.parseLong(path.substring(prefix.length()));
    }
    
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        int start = prefix.length();
        int end = path.indexOf(suffix, start);
        return Long.parseLong(path.substring(start, end));
    }
    
    private String extractStringFromPath(String path, String prefix) {
        return path.substring(prefix.length());
    }
    
    private String extractQueryParam(String query, String param) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        return JsonUtil.fromJson(requestBody, Map.class);
    }
    
    // Removed redundant map parsing methods - now using MapParsingUtil
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", ApplicationConstants.CONTENT_TYPE.APPLICATION_JSON);
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = Map.of(
            "error", message,
            "status", statusCode,
            "timestamp", java.time.Instant.now().toString()
        );
        logger.warn("Sending error response: {} - {}", statusCode, message);
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
}

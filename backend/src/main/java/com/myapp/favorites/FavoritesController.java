package com.myapp.favorites;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Favorite;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing user favorites
 * Handles HTTP requests for favorite restaurants operations
 */
public class FavoritesController implements HttpHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoritesController.class);
    private final FavoritesService favoritesService;
    
    public FavoritesController() {
        this.favoritesService = new FavoritesService();
    }
    
    // Constructor for testing
    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            // Remove the base path "/api/favorites"
            String endpoint = path.replace("/api/favorites", "");
            if (endpoint.startsWith("/")) {
                endpoint = endpoint.substring(1);
            }
            
            logger.info("🔗 Favorites API: {} {}", method, endpoint);
            
            switch (method) {
                case "GET":
                    handleGet(exchange, endpoint);
                    break;
                case "POST":
                    handlePost(exchange, endpoint);
                    break;
                case "PUT":
                    handlePut(exchange, endpoint);
                    break;
                case "DELETE":
                    handleDelete(exchange, endpoint);
                    break;
                default:
                    sendErrorResponse(exchange, 405, "Method not allowed: " + method);
            }
            
        } catch (NotFoundException e) {
            logger.warn("Resource not found: {}", e.getMessage());
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            logger.error("Favorites Controller Error: {}", e.getMessage(), e);
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Handle GET requests
     */
    private void handleGet(HttpExchange exchange, String endpoint) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        
        if (endpoint.isEmpty()) {
            // GET /api/favorites?userId=123
            if (query != null && query.contains("userId=")) {
                String userIdStr = extractQueryParam(query, "userId");
                Long userId = parseId(userIdStr, "userId");
                List<Favorite> favorites = favoritesService.getUserFavorites(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("favorites", favorites);
                response.put("count", favorites.size());
                
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, "Missing required parameter: userId");
            }
            
        } else if (endpoint.equals("check")) {
            // GET /api/favorites/check?userId=123&restaurantId=456
            if (query != null && query.contains("userId=") && query.contains("restaurantId=")) {
                String userIdStr = extractQueryParam(query, "userId");
                String restaurantIdStr = extractQueryParam(query, "restaurantId");
                
                Long userId = parseId(userIdStr, "userId");
                Long restaurantId = parseId(restaurantIdStr, "restaurantId");
                boolean isFavorite = favoritesService.isFavorite(userId, restaurantId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("isFavorite", isFavorite);
                
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, "Missing required parameters: userId, restaurantId");
            }
            
        } else if (endpoint.equals("recent")) {
            // GET /api/favorites/recent?days=30
            int days = 30;
            if (query != null && query.contains("days=")) {
                String daysStr = extractQueryParam(query, "days");
                try {
                    days = Integer.parseInt(daysStr);
                    if (days < 1) days = 30;
                } catch (NumberFormatException e) {
                    days = 30;
                }
            }
            
            List<Favorite> recentFavorites = favoritesService.getRecentFavorites(days);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorites", recentFavorites);
            response.put("count", recentFavorites.size());
            response.put("days", days);
            
            sendSuccessResponse(exchange, response);
            
        } else if (endpoint.equals("with-notes")) {
            // GET /api/favorites/with-notes
            List<Favorite> favoritesWithNotes = favoritesService.getFavoritesWithNotes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorites", favoritesWithNotes);
            response.put("count", favoritesWithNotes.size());
            
            sendSuccessResponse(exchange, response);
            
        } else if (endpoint.equals("stats")) {
            // GET /api/favorites/stats?userId=123
            if (query != null && query.contains("userId=")) {
                String userIdStr = extractQueryParam(query, "userId");
                Long userId = parseId(userIdStr, "userId");
                FavoritesService.FavoriteStats stats = favoritesService.getUserFavoriteStats(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("stats", stats);
                
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, "Missing required parameter: userId");
            }
            
        } else if (endpoint.startsWith("restaurant/")) {
            // GET /api/favorites/restaurant/123
            String restaurantIdStr = endpoint.substring("restaurant/".length());
            Long restaurantId = parseId(restaurantIdStr, "restaurantId");
            
            if (query != null && query.contains("count=true")) {
                // GET /api/favorites/restaurant/123?count=true
                Long count = favoritesService.getRestaurantFavoriteCount(restaurantId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", count);
                
                sendSuccessResponse(exchange, response);
            } else {
                // GET /api/favorites/restaurant/123
                List<Favorite> favorites = favoritesService.getRestaurantFavorites(restaurantId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("favorites", favorites);
                response.put("count", favorites.size());
                
                sendSuccessResponse(exchange, response);
            }
            
        } else if (endpoint.startsWith("user/")) {
            // GET /api/favorites/user/123
            String userIdStr = endpoint.substring("user/".length());
            Long userId = parseId(userIdStr, "userId");
            
            if (query != null && query.contains("count=true")) {
                // GET /api/favorites/user/123?count=true
                Long count = favoritesService.getUserFavoriteCount(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", count);
                
                sendSuccessResponse(exchange, response);
            } else {
                // GET /api/favorites/user/123
                List<Favorite> favorites = favoritesService.getUserFavorites(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("favorites", favorites);
                response.put("count", favorites.size());
                
                sendSuccessResponse(exchange, response);
            }
            
        } else if (endpoint.matches("\\d+")) {
            // GET /api/favorites/123 - Get favorite by ID
            Long favoriteId = parseId(endpoint, "favoriteId");
            Favorite favorite = favoritesService.getFavorite(favoriteId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorite", favorite);
            
            sendSuccessResponse(exchange, response);
            
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found: " + endpoint);
        }
    }
    
    /**
     * Handle POST requests
     */
    private void handlePost(HttpExchange exchange, String endpoint) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        
        if (endpoint.isEmpty() || endpoint.equals("add")) {
            // POST /api/favorites/add
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            
            if (!data.containsKey("userId") || !data.containsKey("restaurantId")) {
                sendErrorResponse(exchange, 400, "Missing required fields: userId, restaurantId");
                return;
            }
            
            Long userId = Long.valueOf(data.get("userId").toString());
            Long restaurantId = Long.valueOf(data.get("restaurantId").toString());
            String notes = data.containsKey("notes") ? data.get("notes").toString() : null;
            
            Favorite favorite = favoritesService.addFavorite(userId, restaurantId, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Added to favorites successfully");
            response.put("favorite", favorite);
            
            sendSuccessResponse(exchange, response);
            
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found: " + endpoint);
        }
    }
    
    /**
     * Handle PUT requests
     */
    private void handlePut(HttpExchange exchange, String endpoint) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        
        if (endpoint.matches("\\d+/notes")) {
            // PUT /api/favorites/123/notes
            String favoriteIdStr = endpoint.substring(0, endpoint.indexOf("/notes"));
            Long favoriteId = parseId(favoriteIdStr, "favoriteId");
            
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            String notes = data.containsKey("notes") ? data.get("notes").toString() : null;
            
            Favorite favorite = favoritesService.updateFavoriteNotes(favoriteId, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Favorite notes updated successfully");
            response.put("favorite", favorite);
            
            sendSuccessResponse(exchange, response);
            
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found: " + endpoint);
        }
    }
    
    /**
     * Handle DELETE requests
     */
    private void handleDelete(HttpExchange exchange, String endpoint) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        
        if (endpoint.isEmpty() || endpoint.equals("remove")) {
            // DELETE /api/favorites/remove?userId=123&restaurantId=456
            if (query != null && query.contains("userId=") && query.contains("restaurantId=")) {
                String userIdStr = extractQueryParam(query, "userId");
                String restaurantIdStr = extractQueryParam(query, "restaurantId");
                
                Long userId = parseId(userIdStr, "userId");
                Long restaurantId = parseId(restaurantIdStr, "restaurantId");
                
                boolean removed = favoritesService.removeFavorite(userId, restaurantId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Removed from favorites successfully");
                response.put("removed", removed);
                
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, "Missing required parameters: userId, restaurantId");
            }
            
        } else if (endpoint.matches("\\d+")) {
            // DELETE /api/favorites/123 - Delete favorite by ID
            Long favoriteId = parseId(endpoint, "favoriteId");
            boolean deleted = favoritesService.deleteFavorite(favoriteId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Favorite deleted successfully");
            response.put("deleted", deleted);
            
            sendSuccessResponse(exchange, response);
            
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found: " + endpoint);
        }
    }
    
    /**
     * Parse ID from string with validation
     */
    private Long parseId(String idStr, String fieldName) {
        if (idStr == null || idStr.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        
        try {
            return Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + idStr);
        }
    }
    
    /**
     * Extract query parameter value
     */
    private String extractQueryParam(String query, String param) {
        if (query == null || param == null) {
            return null;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(param)) {
                try {
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                } catch (Exception e) {
                    logger.warn("Failed to decode query parameter {}: {}", param, keyValue[1]);
                    return keyValue[1];
                }
            }
        }
        return null;
    }
    
    /**
     * Send successful JSON response
     */
    private void sendSuccessResponse(HttpExchange exchange, Object data) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        sendResponse(exchange, 200, jsonResponse);
    }
    
    /**
     * Send error JSON response
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        
        String jsonResponse = JsonUtil.toJson(errorResponse);
        sendResponse(exchange, statusCode, jsonResponse);
    }
    
    /**
     * Send HTTP response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
} 
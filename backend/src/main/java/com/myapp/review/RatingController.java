package com.myapp.review;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Rating;
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

public class RatingController implements HttpHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    private final RatingService ratingService;
    
    public RatingController() {
        this.ratingService = new RatingService();
    }
    
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        logger.info("Handling {} request to {}", method, path);
        
        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path);
                case "POST" -> handlePost(exchange, path);
                case "PUT" -> handlePut(exchange, path);
                default -> sendResponse(exchange, 405, Map.of("error", "Method not allowed"));
            }
        } catch (Exception e) {
            logger.error("Error handling request: {}", e.getMessage(), e);
            sendResponse(exchange, 500, Map.of("error", "Internal server error"));
        }
    }
    
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQueryParams(query);
        
        try {
            if (path.equals("/api/ratings")) {
                handleGetAllRatings(exchange, params);
            } else if (path.matches("/api/ratings/\\d+")) {
                Long ratingId = extractIdFromPath(path);
                handleGetRating(exchange, ratingId);
            } else if (path.equals("/api/ratings/restaurant")) {
                String restaurantIdStr = params.get("restaurantId");
                if (restaurantIdStr == null) {
                    sendResponse(exchange, 400, Map.of("error", "Restaurant ID is required"));
                    return;
                }
                Long restaurantId = Long.parseLong(restaurantIdStr);
                handleGetRestaurantRatings(exchange, restaurantId);
            } else if (path.equals("/api/ratings/stats")) {
                String restaurantIdStr = params.get("restaurantId");
                if (restaurantIdStr == null) {
                    sendResponse(exchange, 400, Map.of("error", "Restaurant ID is required"));
                    return;
                }
                Long restaurantId = Long.parseLong(restaurantIdStr);
                handleGetRatingStats(exchange, restaurantId);
            } else {
                sendResponse(exchange, 404, Map.of("error", "Endpoint not found"));
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid number format"));
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, Map.of("error", "Bad request"));
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, Map.of("error", "Not found"));
        }
    }
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/api/ratings")) {
                handleCreateRating(exchange);
            } else {
                sendResponse(exchange, 404, Map.of("error", "Endpoint not found"));
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Bad request"));
        }
    }
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.matches("/api/ratings/\\d+")) {
                Long ratingId = extractIdFromPath(path);
                handleUpdateRating(exchange, ratingId);
            } else {
                sendResponse(exchange, 404, Map.of("error", "Endpoint not found"));
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Bad request"));
        }
    }
    
    @SuppressWarnings("unchecked")
    private void handleCreateRating(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        Long userId = ((Number) request.get("userId")).longValue();
        Long restaurantId = ((Number) request.get("restaurantId")).longValue();
        Integer score = ((Number) request.get("score")).intValue();
        String reviewText = (String) request.get("reviewText");
        
        Rating rating = ratingService.createRating(userId, restaurantId, score, reviewText);
        sendResponse(exchange, 201, rating);
    }
    
    @SuppressWarnings("unchecked")
    private void handleUpdateRating(HttpExchange exchange, Long ratingId) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        Integer newScore = request.get("score") != null ? ((Number) request.get("score")).intValue() : null;
        String newReviewText = (String) request.get("reviewText");
        
        Rating rating = ratingService.updateRating(ratingId, newScore, newReviewText);
        sendResponse(exchange, 200, rating);
    }
    
    private void handleGetRating(HttpExchange exchange, Long ratingId) throws IOException {
        Rating rating = ratingService.getRating(ratingId);
        sendResponse(exchange, 200, rating);
    }
    
    private void handleGetAllRatings(HttpExchange exchange, Map<String, String> params) throws IOException {
        List<Rating> ratings = ratingService.getAllRatings();
        sendResponse(exchange, 200, ratings);
    }
    
    private void handleGetRestaurantRatings(HttpExchange exchange, Long restaurantId) throws IOException {
        List<Rating> ratings = ratingService.getRestaurantRatings(restaurantId);
        sendResponse(exchange, 200, ratings);
    }
    
    private void handleGetRatingStats(HttpExchange exchange, Long restaurantId) throws IOException {
        RatingService.RatingStats stats = ratingService.getRestaurantRatingStats(restaurantId);
        sendResponse(exchange, 200, stats);
    }
    
    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
    
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        params.put(key, value);
                    } catch (Exception e) {
                        logger.warn("Failed to decode query parameter: {}", pair);
                    }
                }
            }
        }
        return params;
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String response = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
} 
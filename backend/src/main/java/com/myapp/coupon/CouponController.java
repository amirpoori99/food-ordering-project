package com.myapp.coupon;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Coupon;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Coupon Management
 * 
 * Endpoints:
 * POST   /api/coupons                      - Create new coupon
 * GET    /api/coupons/{id}                 - Get coupon by ID
 * GET    /api/coupons/code/{code}          - Get coupon by code
 * PUT    /api/coupons/{id}                 - Update coupon
 * DELETE /api/coupons/{id}                 - Delete coupon
 * POST   /api/coupons/{id}/activate        - Activate coupon
 * POST   /api/coupons/{id}/deactivate      - Deactivate coupon
 * POST   /api/coupons/apply                - Apply coupon to order
 * GET    /api/coupons/valid                - Get all valid coupons
 * GET    /api/coupons/restaurant/{id}      - Get restaurant coupons
 * GET    /api/coupons/global               - Get global coupons
 * GET    /api/coupons/applicable           - Get applicable coupons for order
 * GET    /api/coupons/statistics           - Get coupon statistics
 * GET    /api/coupons/expiring             - Get coupons expiring soon
 */
public class CouponController implements HttpHandler {
    
    private final CouponService couponService;
    
    public CouponController() {
        this.couponService = new CouponService();
    }
    
    // Constructor for dependency injection (testing)
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
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
        if (path.matches("/api/coupons/\\d+")) {
            // GET /api/coupons/{id} - Get coupon by ID
            Long id = extractIdFromPath(path);
            getCouponById(exchange, id);
        } else if (path.matches("/api/coupons/code/.+")) {
            // GET /api/coupons/code/{code} - Get coupon by code
            String code = extractCodeFromPath(path);
            getCouponByCode(exchange, code);
        } else if (path.equals("/api/coupons/valid")) {
            // GET /api/coupons/valid - Get all valid coupons
            getValidCoupons(exchange);
        } else if (path.matches("/api/coupons/restaurant/\\d+")) {
            // GET /api/coupons/restaurant/{id} - Get restaurant coupons
            Long restaurantId = extractIdFromPath(path, "/api/coupons/restaurant/");
            getRestaurantCoupons(exchange, restaurantId);
        } else if (path.equals("/api/coupons/global")) {
            // GET /api/coupons/global - Get global coupons
            getGlobalCoupons(exchange);
        } else if (path.equals("/api/coupons/applicable")) {
            // GET /api/coupons/applicable - Get applicable coupons
            getApplicableCoupons(exchange);
        } else if (path.equals("/api/coupons/statistics")) {
            // GET /api/coupons/statistics - Get coupon statistics
            getCouponStatistics(exchange);
        } else if (path.equals("/api/coupons/expiring")) {
            // GET /api/coupons/expiring - Get coupons expiring soon
            getCouponsExpiringSoon(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void getCouponById(HttpExchange exchange, Long id) throws IOException {
        Coupon coupon = couponService.getCoupon(id);
        sendJsonResponse(exchange, 200, coupon);
    }
    
    private void getCouponByCode(HttpExchange exchange, String code) throws IOException {
        Coupon coupon = couponService.getCouponByCode(code);
        sendJsonResponse(exchange, 200, coupon);
    }
    
    private void getValidCoupons(HttpExchange exchange) throws IOException {
        List<Coupon> coupons = couponService.getValidCoupons();
        sendJsonResponse(exchange, 200, coupons);
    }
    
    private void getRestaurantCoupons(HttpExchange exchange, Long restaurantId) throws IOException {
        List<Coupon> coupons = couponService.getRestaurantCoupons(restaurantId);
        sendJsonResponse(exchange, 200, coupons);
    }
    
    private void getGlobalCoupons(HttpExchange exchange) throws IOException {
        List<Coupon> coupons = couponService.getGlobalCoupons();
        sendJsonResponse(exchange, 200, coupons);
    }
    
    private void getApplicableCoupons(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        
        String orderAmountStr = queryParams.get("orderAmount");
        String restaurantIdStr = queryParams.get("restaurantId");
        
        if (orderAmountStr == null) {
            sendErrorResponse(exchange, 400, "orderAmount parameter is required");
            return;
        }
        
        try {
            Double orderAmount = Double.parseDouble(orderAmountStr);
            Long restaurantId = restaurantIdStr != null ? Long.parseLong(restaurantIdStr) : null;
            
            List<Coupon> coupons = couponService.getApplicableCoupons(orderAmount, restaurantId);
            sendJsonResponse(exchange, 200, coupons);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid number format in parameters");
        }
    }
    
    private void getCouponStatistics(HttpExchange exchange) throws IOException {
        CouponService.CouponStatistics stats = couponService.getCouponStatistics();
        sendJsonResponse(exchange, 200, stats);
    }
    
    private void getCouponsExpiringSoon(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String daysStr = queryParams.get("days");
        
        int days = 7; // Default to 7 days
        if (daysStr != null) {
            try {
                days = Integer.parseInt(daysStr);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 400, "Invalid days parameter");
                return;
            }
        }
        
        List<Coupon> coupons = couponService.getCouponsExpiringSoon(days);
        sendJsonResponse(exchange, 200, coupons);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/coupons")) {
            // POST /api/coupons - Create new coupon
            createCoupon(exchange);
        } else if (path.matches("/api/coupons/\\d+/activate")) {
            // POST /api/coupons/{id}/activate - Activate coupon
            Long id = extractIdFromPath(path, "/api/coupons/", "/activate");
            activateCoupon(exchange, id);
        } else if (path.matches("/api/coupons/\\d+/deactivate")) {
            // POST /api/coupons/{id}/deactivate - Deactivate coupon
            Long id = extractIdFromPath(path, "/api/coupons/", "/deactivate");
            deactivateCoupon(exchange, id);
        } else if (path.equals("/api/coupons/apply")) {
            // POST /api/coupons/apply - Apply coupon to order
            applyCoupon(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void createCoupon(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        
        String code = getStringFromMap(requestData, "code");
        String description = getStringFromMap(requestData, "description");
        String typeStr = getStringFromMap(requestData, "type");
        Double value = getDoubleFromMap(requestData, "value");
        String validFromStr = getStringFromMap(requestData, "validFrom");
        String validUntilStr = getStringFromMap(requestData, "validUntil");
        Long createdBy = getLongFromMap(requestData, "createdBy");
        Long restaurantId = getLongFromMap(requestData, "restaurantId"); // Optional
        
        // Optional parameters
        Double minOrderAmount = getDoubleFromMap(requestData, "minOrderAmount");
        Double maxDiscountAmount = getDoubleFromMap(requestData, "maxDiscountAmount");
        Integer usageLimit = getIntegerFromMap(requestData, "usageLimit");
        Integer perUserLimit = getIntegerFromMap(requestData, "perUserLimit");
        
        // Parse dates
        LocalDateTime validFrom = parseDateTime(validFromStr);
        LocalDateTime validUntil = parseDateTime(validUntilStr);
        
        // Parse coupon type
        Coupon.CouponType type;
        try {
            type = Coupon.CouponType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            sendErrorResponse(exchange, 400, "Invalid coupon type. Must be PERCENTAGE or FIXED_AMOUNT");
            return;
        }
        
        // Create coupon
        Coupon coupon;
        if (minOrderAmount != null || maxDiscountAmount != null || usageLimit != null || perUserLimit != null) {
            // Create coupon with additional settings
            coupon = couponService.createCouponWithSettings(
                code, description, type, value, validFrom, validUntil,
                minOrderAmount, maxDiscountAmount, usageLimit, perUserLimit,
                createdBy, restaurantId);
        } else {
            // Create basic coupon
            if (type == Coupon.CouponType.PERCENTAGE) {
                coupon = couponService.createPercentageCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
            } else {
                coupon = couponService.createFixedAmountCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
            }
        }
        
        sendJsonResponse(exchange, 201, coupon);
    }
    
    private void activateCoupon(HttpExchange exchange, Long id) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        Long activatedBy = getLongFromMap(requestData, "activatedBy");
        
        couponService.activateCoupon(id, activatedBy);
        sendJsonResponse(exchange, 200, Map.of("message", "Coupon activated successfully"));
    }
    
    private void deactivateCoupon(HttpExchange exchange, Long id) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        Long deactivatedBy = getLongFromMap(requestData, "deactivatedBy");
        
        couponService.deactivateCoupon(id, deactivatedBy);
        sendJsonResponse(exchange, 200, Map.of("message", "Coupon deactivated successfully"));
    }
    
    private void applyCoupon(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        
        String couponCode = getStringFromMap(requestData, "couponCode");
        Double orderAmount = getDoubleFromMap(requestData, "orderAmount");
        Long restaurantId = getLongFromMap(requestData, "restaurantId");
        Long userId = getLongFromMap(requestData, "userId");
        
        CouponService.CouponApplicationResult result = couponService.applyCoupon(couponCode, orderAmount, restaurantId, userId);
        
        if (result.isSuccess()) {
            Map<String, Object> response = Map.of(
                "success", true,
                "coupon", result.getCoupon(),
                "discountAmount", result.getDiscountAmount()
            );
            sendJsonResponse(exchange, 200, response);
        } else {
            Map<String, Object> response = Map.of(
                "success", false,
                "errorMessage", result.getErrorMessage()
            );
            sendJsonResponse(exchange, 400, response);
        }
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/coupons/\\d+")) {
            // PUT /api/coupons/{id} - Update coupon
            Long id = extractIdFromPath(path);
            updateCoupon(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void updateCoupon(HttpExchange exchange, Long id) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        
        String description = getStringFromMap(requestData, "description");
        Double minOrderAmount = getDoubleFromMap(requestData, "minOrderAmount");
        Double maxDiscountAmount = getDoubleFromMap(requestData, "maxDiscountAmount");
        Integer usageLimit = getIntegerFromMap(requestData, "usageLimit");
        Integer perUserLimit = getIntegerFromMap(requestData, "perUserLimit");
        String validUntilStr = getStringFromMap(requestData, "validUntil");
        Long updatedBy = getLongFromMap(requestData, "updatedBy");
        
        LocalDateTime validUntil = null;
        if (validUntilStr != null) {
            validUntil = parseDateTime(validUntilStr);
        }
        
        Coupon updatedCoupon = couponService.updateCoupon(id, description, minOrderAmount, 
                                                         maxDiscountAmount, usageLimit, perUserLimit, 
                                                         validUntil, updatedBy);
        
        sendJsonResponse(exchange, 200, updatedCoupon);
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/coupons/\\d+")) {
            // DELETE /api/coupons/{id} - Delete coupon
            Long id = extractIdFromPath(path);
            deleteCoupon(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void deleteCoupon(HttpExchange exchange, Long id) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        Long deletedBy = getLongFromMap(requestData, "deletedBy");
        
        couponService.deleteCoupon(id, deletedBy);
        sendJsonResponse(exchange, 200, Map.of("message", "Coupon deleted successfully"));
    }
    
    // ==================== HELPER METHODS ====================
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonRequest(String requestBody) {
        return JsonUtil.fromJson(requestBody, Map.class);
    }
    
    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
    
    private Long extractIdFromPath(String path, String prefix) {
        String idStr = path.substring(prefix.length());
        return Long.parseLong(idStr);
    }
    
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String idStr = path.substring(prefix.length(), path.length() - suffix.length());
        return Long.parseLong(idStr);
    }
    
    private String extractCodeFromPath(String path) {
        return path.substring("/api/coupons/code/".length());
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("DateTime string cannot be null or empty");
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
    }
    
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new java.util.HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
    
    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
            }
        }
        throw new IllegalArgumentException("Invalid type for " + key + ": " + value.getClass().getSimpleName());
    }
    
    private Double getDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
            }
        }
        throw new IllegalArgumentException("Invalid type for " + key + ": " + value.getClass().getSimpleName());
    }
    
    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) {
            Long longValue = (Long) value;
            if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                throw new IllegalArgumentException("Value too large for integer: " + value);
            }
            return longValue.intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
            }
        }
        throw new IllegalArgumentException("Invalid type for " + key + ": " + value.getClass().getSimpleName());
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String jsonResponse = "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
} 
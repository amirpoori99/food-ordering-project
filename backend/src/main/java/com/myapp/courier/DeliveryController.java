package com.myapp.courier;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Delivery;
import com.myapp.common.models.DeliveryStatus;
import com.myapp.common.models.User;
import com.myapp.auth.AuthRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller for Delivery Management
 * 
 * Endpoints:
 * POST   /api/deliveries                               - Create new delivery
 * GET    /api/deliveries/{deliveryId}                  - Get delivery details
 * PUT    /api/deliveries/{deliveryId}/assign           - Assign courier to delivery
 * PUT    /api/deliveries/{deliveryId}/pickup           - Mark delivery as picked up
 * PUT    /api/deliveries/{deliveryId}/deliver          - Mark delivery as delivered
 * PUT    /api/deliveries/{deliveryId}/cancel           - Cancel delivery
 * GET    /api/deliveries/order/{orderId}               - Get delivery by order
 * GET    /api/deliveries/courier/{courierId}           - Get courier deliveries
 * GET    /api/deliveries/courier/{courierId}/active    - Get courier active deliveries
 * GET    /api/deliveries/status/{status}               - Get deliveries by status
 * GET    /api/deliveries/active                        - Get all active deliveries
 * GET    /api/deliveries/pending                       - Get pending deliveries
 * GET    /api/deliveries/courier/{courierId}/available - Check if courier is available
 * GET    /api/deliveries/courier/{courierId}/statistics - Get courier statistics
 * PUT    /api/deliveries/{deliveryId}/status           - Update delivery status (admin)
 * DELETE /api/deliveries/{deliveryId}                  - Delete delivery (admin)
 */
public class DeliveryController implements HttpHandler {
    
    private final DeliveryService deliveryService;
    
    public DeliveryController() {
        this.deliveryService = new DeliveryService(
            new DeliveryRepository(),
            new AuthRepository(),
            new com.myapp.order.OrderRepository()
        );
    }
    
    // Constructor for dependency injection (testing)
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
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
        } catch (IllegalStateException e) {
            sendErrorResponse(exchange, 409, e.getMessage());
        } catch (NotFoundException e) {
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/deliveries/\\d+")) {
            // GET /api/deliveries/{deliveryId} - Get delivery details
            Long deliveryId = extractDeliveryIdFromPath(path);
            getDeliveryDetails(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/order/\\d+")) {
            // GET /api/deliveries/order/{orderId} - Get delivery by order
            Long orderId = extractIdFromPath(path, "/api/deliveries/order/");
            getDeliveryByOrder(exchange, orderId);
        } else if (path.matches("/api/deliveries/courier/\\d+/active")) {
            // GET /api/deliveries/courier/{courierId}/active - Get courier active deliveries
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/", "/active");
            getCourierActiveDeliveries(exchange, courierId);
        } else if (path.matches("/api/deliveries/courier/\\d+/available")) {
            // GET /api/deliveries/courier/{courierId}/available - Check if courier is available
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/", "/available");
            checkCourierAvailability(exchange, courierId);
        } else if (path.matches("/api/deliveries/courier/\\d+/statistics")) {
            // GET /api/deliveries/courier/{courierId}/statistics - Get courier statistics
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/", "/statistics");
            getCourierStatistics(exchange, courierId);
        } else if (path.matches("/api/deliveries/courier/\\d+")) {
            // GET /api/deliveries/courier/{courierId} - Get courier deliveries
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/");
            getCourierDeliveries(exchange, courierId);
        } else if (path.matches("/api/deliveries/status/\\w+")) {
            // GET /api/deliveries/status/{status} - Get deliveries by status
            String statusStr = extractStatusFromPath(path);
            getDeliveriesByStatus(exchange, statusStr);
        } else if (path.equals("/api/deliveries/active")) {
            // GET /api/deliveries/active - Get all active deliveries
            getActiveDeliveries(exchange);
        } else if (path.equals("/api/deliveries/pending")) {
            // GET /api/deliveries/pending - Get pending deliveries
            getPendingDeliveries(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void getDeliveryDetails(HttpExchange exchange, Long deliveryId) throws IOException {
        Delivery delivery = deliveryService.getDelivery(deliveryId);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    private void getDeliveryByOrder(HttpExchange exchange, Long orderId) throws IOException {
        Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    private void getCourierDeliveries(HttpExchange exchange, Long courierId) throws IOException {
        List<Delivery> deliveries = deliveryService.getCourierDeliveryHistory(courierId);
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    private void getCourierActiveDeliveries(HttpExchange exchange, Long courierId) throws IOException {
        List<Delivery> deliveries = deliveryService.getCourierActiveDeliveries(courierId);
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    private void getDeliveriesByStatus(HttpExchange exchange, String statusStr) throws IOException {
        try {
            DeliveryStatus status = DeliveryStatus.valueOf(statusStr.toUpperCase());
            List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status);
            sendJsonResponse(exchange, 200, deliveries);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    private void getActiveDeliveries(HttpExchange exchange) throws IOException {
        List<Delivery> deliveries = deliveryService.getActiveDeliveries();
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    private void getPendingDeliveries(HttpExchange exchange) throws IOException {
        List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(DeliveryStatus.PENDING);
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    private void checkCourierAvailability(HttpExchange exchange, Long courierId) throws IOException {
        boolean isAvailable = deliveryService.isCourierAvailable(courierId);
        Map<String, Object> response = new HashMap<>();
        response.put("courierId", courierId);
        response.put("available", isAvailable);
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getCourierStatistics(HttpExchange exchange, Long courierId) throws IOException {
        DeliveryRepository.CourierStatistics stats = deliveryService.getCourierStatistics(courierId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/deliveries")) {
            // POST /api/deliveries - Create new delivery
            createDelivery(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void createDelivery(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long orderId = getLongFromMap(requestData, "orderId");
        Double estimatedFee = getDoubleFromMap(requestData, "estimatedFee");
        
        if (orderId == null) {
            sendErrorResponse(exchange, 400, "Order ID is required");
            return;
        }
        
        if (estimatedFee == null) {
            sendErrorResponse(exchange, 400, "Estimated fee is required");
            return;
        }
        
        Delivery delivery = deliveryService.createDelivery(orderId, estimatedFee);
        sendJsonResponse(exchange, 201, delivery);
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/deliveries/\\d+/assign")) {
            // PUT /api/deliveries/{deliveryId}/assign - Assign courier to delivery
            Long deliveryId = extractDeliveryIdFromPath(path, "/assign");
            assignCourier(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/pickup")) {
            // PUT /api/deliveries/{deliveryId}/pickup - Mark delivery as picked up
            Long deliveryId = extractDeliveryIdFromPath(path, "/pickup");
            markPickedUp(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/deliver")) {
            // PUT /api/deliveries/{deliveryId}/deliver - Mark delivery as delivered
            Long deliveryId = extractDeliveryIdFromPath(path, "/deliver");
            markDelivered(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/cancel")) {
            // PUT /api/deliveries/{deliveryId}/cancel - Cancel delivery
            Long deliveryId = extractDeliveryIdFromPath(path, "/cancel");
            cancelDelivery(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/status")) {
            // PUT /api/deliveries/{deliveryId}/status - Update delivery status (admin)
            Long deliveryId = extractDeliveryIdFromPath(path, "/status");
            updateDeliveryStatus(exchange, deliveryId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void assignCourier(HttpExchange exchange, Long deliveryId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long courierId = getLongFromMap(requestData, "courierId");
        
        if (courierId == null) {
            sendErrorResponse(exchange, 400, "Courier ID is required");
            return;
        }
        
        Delivery delivery = deliveryService.assignCourier(deliveryId, courierId);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    private void markPickedUp(HttpExchange exchange, Long deliveryId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long courierId = getLongFromMap(requestData, "courierId");
        
        if (courierId == null) {
            sendErrorResponse(exchange, 400, "Courier ID is required");
            return;
        }
        
        Delivery delivery = deliveryService.markPickedUp(deliveryId, courierId);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    private void markDelivered(HttpExchange exchange, Long deliveryId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long courierId = getLongFromMap(requestData, "courierId");
        
        if (courierId == null) {
            sendErrorResponse(exchange, 400, "Courier ID is required");
            return;
        }
        
        Delivery delivery = deliveryService.markDelivered(deliveryId, courierId);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    private void cancelDelivery(HttpExchange exchange, Long deliveryId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String reason = getStringFromMap(requestData, "reason");
        
        Delivery delivery = deliveryService.cancelDelivery(deliveryId, reason);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    private void updateDeliveryStatus(HttpExchange exchange, Long deliveryId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String statusStr = getStringFromMap(requestData, "status");
        
        if (statusStr == null) {
            sendErrorResponse(exchange, 400, "Status is required");
            return;
        }
        
        try {
            DeliveryStatus status = DeliveryStatus.valueOf(statusStr.toUpperCase());
            Delivery delivery = deliveryService.updateDeliveryStatus(deliveryId, status);
            sendJsonResponse(exchange, 200, delivery);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/deliveries/\\d+")) {
            // DELETE /api/deliveries/{deliveryId} - Delete delivery (admin)
            Long deliveryId = extractDeliveryIdFromPath(path);
            deleteDelivery(exchange, deliveryId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void deleteDelivery(HttpExchange exchange, Long deliveryId) throws IOException {
        deliveryService.deleteDelivery(deliveryId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Delivery deleted successfully");
        response.put("deliveryId", deliveryId);
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private Long extractDeliveryIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            throw new IllegalArgumentException("Invalid path format: " + path);
        }
        return Long.parseLong(parts[3]); // /api/deliveries/{id}
    }
    
    private Long extractDeliveryIdFromPath(String path, String suffix) {
        // Remove suffix only from the end of the path
        if (path.endsWith(suffix)) {
            String pathWithoutSuffix = path.substring(0, path.length() - suffix.length());
            return extractDeliveryIdFromPath(pathWithoutSuffix);
        } else {
            throw new IllegalArgumentException("Path does not end with expected suffix: " + suffix);
        }
    }
    
    private Long extractIdFromPath(String path, String prefix) {
        String idPart = path.substring(prefix.length());
        return Long.parseLong(idPart);
    }
    
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String idPart = path.substring(prefix.length());
        idPart = idPart.replace(suffix, "");
        return Long.parseLong(idPart);
    }
    
    private String extractStatusFromPath(String path) {
        String[] parts = path.split("/");
        return parts[4]; // /api/deliveries/status/{status}
    }
    
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }
        
        String json = requestBody.toString().trim();
        if (json.isEmpty()) {
            return new HashMap<>();
        }
        
        return parseJson(json);
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            // Simple JSON parser - in production, use Jackson or Gson
            json = json.trim();
            if (!json.startsWith("{") || !json.endsWith("}")) {
                throw new IllegalArgumentException("Invalid JSON format");
            }
            
            Map<String, Object> result = new HashMap<>();
            json = json.substring(1, json.length() - 1); // Remove { }
            
            if (json.trim().isEmpty()) {
                return result;
            }
            
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) continue;
                
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim();
                
                // Parse different value types
                if (value.equals("null")) {
                    result.put(key, null);
                } else if (value.equals("true")) {
                    result.put(key, true);
                } else if (value.equals("false")) {
                    result.put(key, false);
                } else if (value.startsWith("\"") && value.endsWith("\"")) {
                    result.put(key, value.substring(1, value.length() - 1));
                } else if (value.contains(".")) {
                    result.put(key, Double.parseDouble(value));
                } else {
                    result.put(key, Long.parseLong(value));
                }
            }
            
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage());
        }
    }
    
    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private Double getDoubleFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Float) {
            return ((Float) value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", statusCode);
        
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
    
    private String convertToJson(Object data) {
        if (data == null) {
            return "null";
        }
        
        if (data instanceof String) {
            return "\"" + escapeJson((String) data) + "\"";
        }
        
        if (data instanceof Number || data instanceof Boolean) {
            return data.toString();
        }
        
        if (data instanceof Map) {
            return convertMapToJson((Map<?, ?>) data);
        }
        
        if (data instanceof List) {
            return convertListToJson((List<?>) data);
        }
        
        // For complex objects, use reflection-based serialization
        return serializeObject(data);
    }
    
    private String convertMapToJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(convertToJson(entry.getValue()));
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (Object item : list) {
            if (!first) json.append(",");
            json.append(convertToJson(item));
            first = false;
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String serializeObject(Object obj) {
        // Simple object serialization using reflection
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        try {
            Class<?> clazz = obj.getClass();
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (!first) json.append(",");
                json.append("\"").append(field.getName()).append("\":");
                json.append(convertToJson(value));
                first = false;
            }
        } catch (Exception e) {
            // Fallback to toString
            return "\"" + escapeJson(obj.toString()) + "\"";
        }
        
        json.append("}");
        return json.toString();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
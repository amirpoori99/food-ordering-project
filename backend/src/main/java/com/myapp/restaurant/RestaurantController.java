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
 * کنترلر REST API برای مدیریت رستوران‌ها
 * 
 * این کلاس مسئول مدیریت تمام درخواست‌های HTTP مربوط به:
 * - ثبت رستوران جدید توسط فروشندگان
 * - مشاهده لیست رستوران‌های تأیید شده (عمومی)
 * - مدیریت اطلاعات رستوران‌ها
 * - تغییر وضعیت رستوران‌ها (مدیریتی)
 * - آمار و گزارش‌گیری رستوران‌ها
 * 
 * Endpoints:
 * POST   /api/restaurants                    - ثبت رستوران جدید
 * GET    /api/restaurants                    - دریافت رستوران‌های تأیید شده (عمومی)
 * GET    /api/restaurants/{id}               - دریافت رستوران با شناسه
 * PUT    /api/restaurants/{id}               - به‌روزرسانی اطلاعات رستوران
 * DELETE /api/restaurants/{id}               - حذف رستوران
 * GET    /api/restaurants/owner/{ownerId}    - دریافت رستوران‌های یک مالک
 * GET    /api/restaurants/status/{status}    - دریافت رستوران‌ها بر اساس وضعیت
 * PUT    /api/restaurants/{id}/status        - به‌روزرسانی وضعیت رستوران (مدیر)
 * GET    /api/restaurants/statistics         - دریافت آمار رستوران‌ها (مدیر)
 * POST   /api/restaurants/{id}/approve       - تأیید رستوران (مدیر)
 * POST   /api/restaurants/{id}/reject        - رد رستوران (مدیر)
 * POST   /api/restaurants/{id}/suspend       - تعلیق رستوران (مدیر)
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class RestaurantController implements HttpHandler {
    
    /** سرویس مدیریت رستوران‌ها برای پردازش منطق کسب‌وکار */
    private final RestaurantService restaurantService;
    
    /**
     * سازنده پیش‌فرض - ایجاد instance جدید از RestaurantService
     */
    public RestaurantController() {
        this.restaurantService = new RestaurantService();
    }
    
    /**
     * سازنده برای تزریق وابستگی (Dependency Injection)
     * برای تست‌ها و configuration سفارشی استفاده می‌شود
     * 
     * @param restaurantService سرویس رستوران از بیرون تزریق شده
     */
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }
    
    /**
     * متد اصلی پردازش درخواست‌های HTTP
     * تمام درخواست‌ها را بر اساس method و path مسیریابی می‌کند
     * 
     * @param exchange شیء HttpExchange حاوی اطلاعات درخواست و پاسخ
     * @throws IOException در صورت خطا در پردازش I/O
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // دریافت method (GET, POST, PUT, DELETE) و path درخواست
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            // مسیریابی درخواست بر اساس HTTP method
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
                    // HTTP method پشتیبانی نشده
                    sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (IllegalArgumentException e) {
            // خطای validation ورودی‌ها
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (NotFoundException e) {
            // خطای عدم وجود رستوران
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            // خطاهای غیرمنتظره سرور
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    // ==================== GET ENDPOINTS - پردازش درخواست‌های GET ====================
    
    /**
     * پردازش تمام درخواست‌های GET مربوط به رستوران‌ها
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در پردازش
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/restaurants")) {
            // GET /api/restaurants - دریافت همه رستوران‌های تأیید شده
            getAllApprovedRestaurants(exchange);
        } else if (path.matches("/api/restaurants/\\d+")) {
            // GET /api/restaurants/{id} - دریافت رستوران با شناسه
            Long id = extractIdFromPath(path);
            getRestaurantById(exchange, id);
        } else if (path.matches("/api/restaurants/owner/\\d+")) {
            // GET /api/restaurants/owner/{ownerId} - دریافت رستوران‌های یک مالک
            Long ownerId = extractIdFromPath(path, "/api/restaurants/owner/");
            getRestaurantsByOwner(exchange, ownerId);
        } else if (path.matches("/api/restaurants/status/\\w+")) {
            // GET /api/restaurants/status/{status} - دریافت رستوران‌ها بر اساس وضعیت
            String statusStr = extractStatusFromPath(path);
            getRestaurantsByStatus(exchange, statusStr);
        } else if (path.equals("/api/restaurants/statistics")) {
            // GET /api/restaurants/statistics - دریافت آمار رستوران‌ها
            getRestaurantStatistics(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * دریافت همه رستوران‌های تأیید شده (برای نمایش عمومی)
     * فقط رستوران‌هایی که وضعیت APPROVED دارند نمایش داده می‌شوند
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در پردازش
     */
    private void getAllApprovedRestaurants(HttpExchange exchange) throws IOException {
        List<Restaurant> restaurants = restaurantService.getApprovedRestaurants();
        sendJsonResponse(exchange, 200, restaurants);
    }
    
    /**
     * دریافت اطلاعات رستوران با شناسه مشخص
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه رستوران
     * @throws IOException در صورت خطا در پردازش
     */
    private void getRestaurantById(HttpExchange exchange, Long id) throws IOException {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        sendJsonResponse(exchange, 200, restaurant);
    }
    
    /**
     * دریافت همه رستوران‌های متعلق به یک مالک
     * برای نمایش در پنل فروشندگان استفاده می‌شود
     * 
     * @param exchange شیء HttpExchange
     * @param ownerId شناسه مالک رستوران
     * @throws IOException در صورت خطا در پردازش
     */
    private void getRestaurantsByOwner(HttpExchange exchange, Long ownerId) throws IOException {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        sendJsonResponse(exchange, 200, restaurants);
    }
    
    /**
     * دریافت رستوران‌ها بر اساس وضعیت
     * برای فیلتر کردن رستوران‌ها در پنل مدیریت استفاده می‌شود
     * 
     * @param exchange شیء HttpExchange
     * @param statusStr رشته وضعیت (PENDING, APPROVED, REJECTED, SUSPENDED)
     * @throws IOException در صورت خطا در پردازش
     */
    private void getRestaurantsByStatus(HttpExchange exchange, String statusStr) throws IOException {
        try {
            // تبدیل رشته به enum RestaurantStatus
            RestaurantStatus status = RestaurantStatus.valueOf(statusStr.toUpperCase());
            List<Restaurant> restaurants = restaurantService.getRestaurantsByStatus(status);
            sendJsonResponse(exchange, 200, restaurants);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    /**
     * دریافت آمار کلی رستوران‌ها
     * شامل تعداد کل، تأیید شده، در انتظار، رد شده و تعلیق شده
     * فقط برای مدیران سیستم در دسترس است
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در پردازش
     */
    private void getRestaurantStatistics(HttpExchange exchange) throws IOException {
        RestaurantService.RestaurantStatistics stats = restaurantService.getRestaurantStatistics();
        sendJsonResponse(exchange, 200, stats);
    }
    
    // ==================== POST ENDPOINTS - پردازش درخواست‌های POST ====================
    
    /**
     * پردازش تمام درخواست‌های POST مربوط به رستوران‌ها
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در پردازش
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/restaurants")) {
            // POST /api/restaurants - ثبت رستوران جدید
            registerRestaurant(exchange);
        } else if (path.matches("/api/restaurants/\\d+/approve")) {
            // POST /api/restaurants/{id}/approve - تأیید رستوران
            Long id = extractIdFromPath(path, "/api/restaurants/", "/approve");
            approveRestaurant(exchange, id);
        } else if (path.matches("/api/restaurants/\\d+/reject")) {
            // POST /api/restaurants/{id}/reject - رد رستوران
            Long id = extractIdFromPath(path, "/api/restaurants/", "/reject");
            rejectRestaurant(exchange, id);
        } else if (path.matches("/api/restaurants/\\d+/suspend")) {
            // POST /api/restaurants/{id}/suspend - تعلیق رستوران
            Long id = extractIdFromPath(path, "/api/restaurants/", "/suspend");
            suspendRestaurant(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * ثبت رستوران جدید در سیستم
     * رستوران با وضعیت PENDING ثبت می‌شود و منتظر تأیید مدیر است
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در پردازش
     */
    private void registerRestaurant(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        // استخراج اطلاعات از درخواست
        Long ownerId = getLongFromMap(requestData, "ownerId");
        String name = getStringFromMap(requestData, "name");
        String address = getStringFromMap(requestData, "address");
        String phone = getStringFromMap(requestData, "phone");
        
        // ثبت رستوران جدید
        Restaurant restaurant = restaurantService.registerRestaurant(ownerId, name, address, phone);
        sendJsonResponse(exchange, 201, restaurant);
    }
    
    /**
     * تأیید رستوران توسط مدیر
     * وضعیت رستوران از PENDING به APPROVED تغییر می‌کند
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه رستوران
     * @throws IOException در صورت خطا در پردازش
     */
    private void approveRestaurant(HttpExchange exchange, Long id) throws IOException {
        restaurantService.approveRestaurant(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Restaurant approved successfully"));
    }
    
    /**
     * رد رستوران توسط مدیر
     * وضعیت رستوران از PENDING به REJECTED تغییر می‌کند
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه رستوران
     * @throws IOException در صورت خطا در پردازش
     */
    private void rejectRestaurant(HttpExchange exchange, Long id) throws IOException {
        restaurantService.rejectRestaurant(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Restaurant rejected successfully"));
    }
    
    /**
     * تعلیق رستوران توسط مدیر
     * رستوران از دسترس خارج می‌شود اما حذف نمی‌شود
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه رستوران
     * @throws IOException در صورت خطا در پردازش
     */
    private void suspendRestaurant(HttpExchange exchange, Long id) throws IOException {
        restaurantService.suspendRestaurant(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Restaurant suspended successfully"));
    }
    
    // ==================== PUT ENDPOINTS - پردازش درخواست‌های PUT ====================
    
    /**
     * پردازش تمام درخواست‌های PUT مربوط به رستوران‌ها
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در پردازش
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/restaurants/\\d+")) {
            // PUT /api/restaurants/{id} - به‌روزرسانی اطلاعات رستوران
            Long id = extractIdFromPath(path);
            updateRestaurant(exchange, id);
        } else if (path.matches("/api/restaurants/\\d+/status")) {
            // PUT /api/restaurants/{id}/status - به‌روزرسانی وضعیت رستوران
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
    
    // ==================== UTILITY METHODS - متدهای کمکی ====================
    
    /**
     * استخراج شناسه از انتهای مسیر
     * مثال: /api/restaurants/123 -> 123
     * 
     * @param path مسیر درخواست
     * @return شناسه استخراج شده
     */
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

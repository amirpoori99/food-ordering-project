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
 * کنترلر REST API برای مدیریت تحویل سفارشات
 * 
 * این کلاس تمام endpoint های مربوط به سیستم تحویل را مدیریت می‌کند:
 * 
 * === CRUD عملیات اصلی ===
 * POST   /api/deliveries                               - ایجاد تحویل جدید
 * GET    /api/deliveries/{deliveryId}                  - دریافت جزئیات تحویل
 * DELETE /api/deliveries/{deliveryId}                  - حذف تحویل (مدیر)
 * 
 * === مدیریت فرآیند تحویل ===
 * PUT    /api/deliveries/{deliveryId}/assign           - اختصاص پیک به تحویل
 * PUT    /api/deliveries/{deliveryId}/pickup           - علامت‌گذاری تحویل گرفته شده
 * PUT    /api/deliveries/{deliveryId}/deliver          - علامت‌گذاری تحویل داده شده
 * PUT    /api/deliveries/{deliveryId}/cancel           - لغو تحویل
 * PUT    /api/deliveries/{deliveryId}/status           - به‌روزرسانی وضعیت (مدیر)
 * 
 * === جستجو و فیلتر ===
 * GET    /api/deliveries/order/{orderId}               - تحویل بر اساس سفارش
 * GET    /api/deliveries/courier/{courierId}           - تحویل‌های پیک
 * GET    /api/deliveries/courier/{courierId}/active    - تحویل‌های فعال پیک
 * GET    /api/deliveries/status/{status}               - تحویل‌ها بر اساس وضعیت
 * GET    /api/deliveries/active                        - تمام تحویل‌های فعال
 * GET    /api/deliveries/pending                       - تحویل‌های در انتظار
 * 
 * === مدیریت پیک‌ها ===
 * GET    /api/deliveries/courier/{courierId}/available - بررسی در دسترس بودن پیک
 * GET    /api/deliveries/courier/{courierId}/statistics - آمار عملکرد پیک
 * 
 * ویژگی‌های کلیدی:
 * - RESTful API Design: طراحی مطابق استانداردهای REST
 * - JSON Request/Response: پردازش و تولید JSON
 * - Error Handling: مدیریت خطاها با کدهای HTTP مناسب
 * - Path Parameter Extraction: استخراج پارامترها از URL
 * - Input Validation: اعتبارسنجی داده‌های ورودی
 * - Custom JSON Parser: پارسر JSON سفارشی برای dependency کمتر
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class DeliveryController implements HttpHandler {
    
    /** سرویس منطق کسب‌وکار تحویل */
    private final DeliveryService deliveryService;
    
    /**
     * سازنده پیش‌فرض
     * Dependencies را به صورت خودکار ایجاد می‌کند
     */
    public DeliveryController() {
        this.deliveryService = new DeliveryService(
            new DeliveryRepository(),
            new AuthRepository(),
            new com.myapp.order.OrderRepository()
        );
    }
    
    /**
     * سازنده برای dependency injection (تست)
     * 
     * @param deliveryService سرویس تحویل از خارج تزریق شده
     */
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }
    
    /**
     * هندلر اصلی HTTP requests
     * 
     * تمام درخواست‌ها را بر اساس HTTP method و path مسیریابی می‌کند
     * Error handling کامل برای انواع مختلف exceptions
     * 
     * @param exchange شیء HTTP request/response
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            // مسیریابی بر اساس HTTP method
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
            // خطای پارامتر نامعتبر - 400 Bad Request
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (IllegalStateException e) {
            // خطای وضعیت نامعتبر - 409 Conflict
            sendErrorResponse(exchange, 409, e.getMessage());
        } catch (NotFoundException e) {
            // خطای یافت نشدن - 404 Not Found
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            // خطای سرور - 500 Internal Server Error
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    /**
     * مدیریت تمام GET requests
     * 
     * بر اساس pattern های مختلف URL، درخواست‌ها را مسیریابی می‌کند
     * از regex pattern matching برای انعطاف‌پذیری استفاده می‌کند
     * 
     * @param exchange HTTP exchange object
     * @param path مسیر درخواست
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/deliveries/\\d+")) {
            // GET /api/deliveries/{deliveryId} - دریافت جزئیات تحویل
            Long deliveryId = extractDeliveryIdFromPath(path);
            getDeliveryDetails(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/order/\\d+")) {
            // GET /api/deliveries/order/{orderId} - تحویل بر اساس سفارش
            Long orderId = extractIdFromPath(path, "/api/deliveries/order/");
            getDeliveryByOrder(exchange, orderId);
        } else if (path.matches("/api/deliveries/courier/\\d+/active")) {
            // GET /api/deliveries/courier/{courierId}/active - تحویل‌های فعال پیک
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/", "/active");
            getCourierActiveDeliveries(exchange, courierId);
        } else if (path.matches("/api/deliveries/courier/\\d+/available")) {
            // GET /api/deliveries/courier/{courierId}/available - بررسی در دسترس بودن پیک
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/", "/available");
            checkCourierAvailability(exchange, courierId);
        } else if (path.matches("/api/deliveries/courier/\\d+/statistics")) {
            // GET /api/deliveries/courier/{courierId}/statistics - آمار پیک
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/", "/statistics");
            getCourierStatistics(exchange, courierId);
        } else if (path.matches("/api/deliveries/courier/\\d+")) {
            // GET /api/deliveries/courier/{courierId} - تاریخچه تحویل‌های پیک
            Long courierId = extractIdFromPath(path, "/api/deliveries/courier/");
            getCourierDeliveries(exchange, courierId);
        } else if (path.matches("/api/deliveries/status/\\w+")) {
            // GET /api/deliveries/status/{status} - تحویل‌ها بر اساس وضعیت
            String statusStr = extractStatusFromPath(path);
            getDeliveriesByStatus(exchange, statusStr);
        } else if (path.equals("/api/deliveries/active")) {
            // GET /api/deliveries/active - تمام تحویل‌های فعال
            getActiveDeliveries(exchange);
        } else if (path.equals("/api/deliveries/pending")) {
            // GET /api/deliveries/pending - تحویل‌های در انتظار اختصاص پیک
            getPendingDeliveries(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * دریافت جزئیات کامل یک تحویل
     * 
     * @param exchange HTTP exchange
     * @param deliveryId شناسه تحویل
     */
    private void getDeliveryDetails(HttpExchange exchange, Long deliveryId) throws IOException {
        Delivery delivery = deliveryService.getDelivery(deliveryId);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    /**
     * دریافت تحویل مربوط به یک سفارش خاص
     * 
     * @param exchange HTTP exchange
     * @param orderId شناسه سفارش
     */
    private void getDeliveryByOrder(HttpExchange exchange, Long orderId) throws IOException {
        Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    /**
     * دریافت تاریخچه کامل تحویل‌های یک پیک
     * 
     * @param exchange HTTP exchange
     * @param courierId شناسه پیک
     */
    private void getCourierDeliveries(HttpExchange exchange, Long courierId) throws IOException {
        List<Delivery> deliveries = deliveryService.getCourierDeliveryHistory(courierId);
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    /**
     * دریافت تحویل‌های فعال (در حال انجام) یک پیک
     * 
     * @param exchange HTTP exchange
     * @param courierId شناسه پیک
     */
    private void getCourierActiveDeliveries(HttpExchange exchange, Long courierId) throws IOException {
        List<Delivery> deliveries = deliveryService.getCourierActiveDeliveries(courierId);
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    /**
     * دریافت تحویل‌ها بر اساس وضعیت مشخص
     * 
     * @param exchange HTTP exchange
     * @param statusStr وضعیت تحویل (رشته)
     */
    private void getDeliveriesByStatus(HttpExchange exchange, String statusStr) throws IOException {
        try {
            DeliveryStatus status = DeliveryStatus.valueOf(statusStr.toUpperCase());
            List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status);
            sendJsonResponse(exchange, 200, deliveries);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    /**
     * دریافت تمام تحویل‌های فعال سیستم
     * 
     * @param exchange HTTP exchange
     */
    private void getActiveDeliveries(HttpExchange exchange) throws IOException {
        List<Delivery> deliveries = deliveryService.getActiveDeliveries();
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    /**
     * دریافت تحویل‌های در انتظار اختصاص پیک
     * 
     * @param exchange HTTP exchange
     */
    private void getPendingDeliveries(HttpExchange exchange) throws IOException {
        List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(DeliveryStatus.PENDING);
        sendJsonResponse(exchange, 200, deliveries);
    }
    
    /**
     * بررسی در دسترس بودن پیک برای تحویل جدید
     * 
     * @param exchange HTTP exchange
     * @param courierId شناسه پیک
     */
    private void checkCourierAvailability(HttpExchange exchange, Long courierId) throws IOException {
        boolean isAvailable = deliveryService.isCourierAvailable(courierId);
        Map<String, Object> response = new HashMap<>();
        response.put("courierId", courierId);
        response.put("available", isAvailable);
        sendJsonResponse(exchange, 200, response);
    }
    
    /**
     * دریافت آمار عملکرد پیک
     * 
     * شامل تعداد تحویل‌ها، نرخ موفقیت، درآمد و میانگین زمان تحویل
     * 
     * @param exchange HTTP exchange
     * @param courierId شناسه پیک
     */
    private void getCourierStatistics(HttpExchange exchange, Long courierId) throws IOException {
        DeliveryRepository.CourierStatistics stats = deliveryService.getCourierStatistics(courierId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    /**
     * مدیریت تمام POST requests
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/deliveries")) {
            // POST /api/deliveries - ایجاد تحویل جدید
            createDelivery(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * ایجاد درخواست تحویل جدید برای سفارش
     * 
     * JSON Request Body:
     * {
     *   "orderId": number,
     *   "estimatedFee": number
     * }
     * 
     * @param exchange HTTP exchange
     */
    private void createDelivery(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long orderId = getLongFromMap(requestData, "orderId");
        Double estimatedFee = getDoubleFromMap(requestData, "estimatedFee");
        
        // اعتبارسنجی ورودی‌ها
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
    
    /**
     * مدیریت تمام PUT requests
     * 
     * شامل عملیات‌های به‌روزرسانی وضعیت تحویل
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/deliveries/\\d+/assign")) {
            // PUT /api/deliveries/{deliveryId}/assign - اختصاص پیک
            Long deliveryId = extractDeliveryIdFromPath(path, "/assign");
            assignCourier(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/pickup")) {
            // PUT /api/deliveries/{deliveryId}/pickup - تحویل گرفتن از رستوران
            Long deliveryId = extractDeliveryIdFromPath(path, "/pickup");
            markPickedUp(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/deliver")) {
            // PUT /api/deliveries/{deliveryId}/deliver - تحویل به مشتری
            Long deliveryId = extractDeliveryIdFromPath(path, "/deliver");
            markDelivered(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/cancel")) {
            // PUT /api/deliveries/{deliveryId}/cancel - لغو تحویل
            Long deliveryId = extractDeliveryIdFromPath(path, "/cancel");
            cancelDelivery(exchange, deliveryId);
        } else if (path.matches("/api/deliveries/\\d+/status")) {
            // PUT /api/deliveries/{deliveryId}/status - به‌روزرسانی وضعیت (مدیر)
            Long deliveryId = extractDeliveryIdFromPath(path, "/status");
            updateDeliveryStatus(exchange, deliveryId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * اختصاص پیک به تحویل
     * 
     * JSON Request Body:
     * {
     *   "courierId": number
     * }
     * 
     * @param exchange HTTP exchange
     * @param deliveryId شناسه تحویل
     */
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
    
    /**
     * علامت‌گذاری تحویل به عنوان "تحویل گرفته شده از رستوران"
     * 
     * JSON Request Body:
     * {
     *   "courierId": number
     * }
     * 
     * @param exchange HTTP exchange
     * @param deliveryId شناسه تحویل
     */
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
    
    /**
     * علامت‌گذاری تحویل به عنوان "تحویل داده شده به مشتری"
     * 
     * JSON Request Body:
     * {
     *   "courierId": number
     * }
     * 
     * @param exchange HTTP exchange
     * @param deliveryId شناسه تحویل
     */
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
    
    /**
     * لغو تحویل
     * 
     * JSON Request Body:
     * {
     *   "reason": string (اختیاری)
     * }
     * 
     * @param exchange HTTP exchange
     * @param deliveryId شناسه تحویل
     */
    private void cancelDelivery(HttpExchange exchange, Long deliveryId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String reason = getStringFromMap(requestData, "reason");
        
        Delivery delivery = deliveryService.cancelDelivery(deliveryId, reason);
        sendJsonResponse(exchange, 200, delivery);
    }
    
    /**
     * به‌روزرسانی مستقیم وضعیت تحویل (عملکرد مدیر)
     * 
     * JSON Request Body:
     * {
     *   "status": string
     * }
     * 
     * @param exchange HTTP exchange
     * @param deliveryId شناسه تحویل
     */
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
    
    /**
     * مدیریت تمام DELETE requests
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/deliveries/\\d+")) {
            // DELETE /api/deliveries/{deliveryId} - حذف تحویل (مدیر)
            Long deliveryId = extractDeliveryIdFromPath(path);
            deleteDelivery(exchange, deliveryId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * حذف تحویل (فقط برای مدیر و تحویل‌های لغو شده)
     * 
     * @param exchange HTTP exchange
     * @param deliveryId شناسه تحویل
     */
    private void deleteDelivery(HttpExchange exchange, Long deliveryId) throws IOException {
        deliveryService.deleteDelivery(deliveryId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Delivery deleted successfully");
        response.put("deliveryId", deliveryId);
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * استخراج شناسه تحویل از path ساده
     * 
     * Pattern: /api/deliveries/{id}
     * 
     * @param path مسیر URL
     * @return شناسه تحویل
     */
    private Long extractDeliveryIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            throw new IllegalArgumentException("Invalid path format: " + path);
        }
        return Long.parseLong(parts[3]); // /api/deliveries/{id}
    }
    
    /**
     * استخراج شناسه تحویل از path با suffix
     * 
     * Pattern: /api/deliveries/{id}/action
     * 
     * @param path مسیر URL
     * @param suffix پسوند برای حذف
     * @return شناسه تحویل
     */
    private Long extractDeliveryIdFromPath(String path, String suffix) {
        // حذف suffix فقط از انتهای path
        if (path.endsWith(suffix)) {
            String pathWithoutSuffix = path.substring(0, path.length() - suffix.length());
            return extractDeliveryIdFromPath(pathWithoutSuffix);
        } else {
            throw new IllegalArgumentException("Path does not end with expected suffix: " + suffix);
        }
    }
    
    /**
     * استخراج ID از path با prefix
     * 
     * @param path مسیر URL
     * @param prefix پیشوند برای حذف
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix) {
        String idPart = path.substring(prefix.length());
        return Long.parseLong(idPart);
    }
    
    /**
     * استخراج ID از path با prefix و suffix
     * 
     * @param path مسیر URL
     * @param prefix پیشوند برای حذف
     * @param suffix پسوند برای حذف
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String idPart = path.substring(prefix.length());
        idPart = idPart.replace(suffix, "");
        return Long.parseLong(idPart);
    }
    
    /**
     * استخراج وضعیت از path
     * 
     * Pattern: /api/deliveries/status/{status}
     * 
     * @param path مسیر URL
     * @return رشته وضعیت
     */
    private String extractStatusFromPath(String path) {
        String[] parts = path.split("/");
        return parts[4]; // /api/deliveries/status/{status}
    }
    
    /**
     * پارس درخواست JSON از HTTP request body
     * 
     * @param exchange HTTP exchange
     * @return Map حاوی داده‌های JSON
     */
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
    
    /**
     * پارسر JSON ساده
     * 
     * در محیط production از Jackson یا Gson استفاده کنید
     * این پیاده‌سازی برای کاهش dependencies است
     * 
     * @param json رشته JSON
     * @return Map حاوی داده‌های پارس شده
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String json) {
        try {
            json = json.trim();
            if (!json.startsWith("{") || !json.endsWith("}")) {
                throw new IllegalArgumentException("Invalid JSON format");
            }
            
            Map<String, Object> result = new HashMap<>();
            json = json.substring(1, json.length() - 1); // حذف { }
            
            if (json.trim().isEmpty()) {
                return result;
            }
            
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) continue;
                
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim();
                
                // پارس انواع مختلف مقادیر
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
    
    /**
     * استخراج مقدار String از Map
     * 
     * @param map نقشه داده‌ها
     * @param key کلید
     * @return مقدار رشته یا null
     */
    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * استخراج مقدار Long از Map
     * 
     * انواع مختلف عددی را پشتیبانی می‌کند
     * 
     * @param map نقشه داده‌ها
     * @param key کلید
     * @return مقدار Long یا null
     */
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
    
    /**
     * استخراج مقدار Double از Map
     * 
     * انواع مختلف عددی را پشتیبانی می‌کند
     * 
     * @param map نقشه داده‌ها
     * @param key کلید
     * @return مقدار Double یا null
     */
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
    
    /**
     * ارسال پاسخ JSON
     * 
     * @param exchange HTTP exchange
     * @param statusCode کد وضعیت HTTP
     * @param data داده برای serialize
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = convertToJson(data);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    /**
     * ارسال پاسخ خطا
     * 
     * @param exchange HTTP exchange
     * @param statusCode کد خطای HTTP
     * @param message پیام خطا
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", statusCode);
        
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
    
    /**
     * تبدیل Object به JSON string
     * 
     * Serializer ساده برای انواع مختلف داده‌ها
     * 
     * @param data داده برای تبدیل
     * @return رشته JSON
     */
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
        
        // برای اشیاء پیچیده، از reflection استفاده کن
        return serializeObject(data);
    }
    
    /**
     * تبدیل Map به JSON
     * 
     * @param map نقشه برای تبدیل
     * @return رشته JSON
     */
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
    
    /**
     * تبدیل List به JSON
     * 
     * @param list لیست برای تبدیل
     * @return رشته JSON
     */
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
    
    /**
     * Serialize اشیاء پیچیده با reflection
     * 
     * @param obj شیء برای serialize
     * @return رشته JSON
     */
    private String serializeObject(Object obj) {
        // سریال‌سازی ساده با reflection
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        try {
            Class<?> clazz = obj.getClass();
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                // نادیده گیری فیلدهای static و transient
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
            // Fallback به toString
            return "\"" + escapeJson(obj.toString()) + "\"";
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escape کردن رشته برای JSON
     * 
     * @param str رشته برای escape
     * @return رشته escape شده
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
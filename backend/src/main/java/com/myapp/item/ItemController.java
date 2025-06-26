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
 * کنترلر REST API برای مدیریت آیتم‌های غذایی
 * 
 * این کلاس تمام endpoint های مربوط به آیتم‌های غذایی را مدیریت می‌کند:
 * 
 * === عملیات CRUD آیتم‌ها ===
 * POST   /api/items                          - افزودن آیتم غذایی جدید
 * GET    /api/items/{id}                     - دریافت آیتم بر اساس شناسه
 * PUT    /api/items/{id}                     - به‌روزرسانی آیتم غذایی
 * DELETE /api/items/{id}                     - حذف آیتم غذایی
 * 
 * === مدیریت موجودی ===
 * PUT    /api/items/{id}/availability        - به‌روزرسانی وضعیت در دسترس بودن
 * PUT    /api/items/{id}/quantity           - به‌روزرسانی موجودی آیتم
 * 
 * === جستجو و فیلتر ===
 * GET    /api/items/restaurant/{restaurantId} - دریافت آیتم‌های رستوران
 * GET    /api/items/restaurant/{restaurantId}/available - آیتم‌های در دسترس رستوران
 * GET    /api/items/search                   - جستجو بر اساس کلیدواژه
 * GET    /api/items/category/{category}      - آیتم‌ها بر اساس دسته‌بندی
 * 
 * === گزارش و آمار ===
 * GET    /api/items/restaurant/{restaurantId}/categories - دسته‌بندی‌های رستوران
 * GET    /api/items/restaurant/{restaurantId}/low-stock - آیتم‌های کم موجودی
 * GET    /api/items/restaurant/{restaurantId}/statistics - آمار منوی رستوران
 * 
 * ویژگی‌های کلیدی:
 * - RESTful API Design: طراحی مطابق استانداردهای REST
 * - Advanced Error Handling: مدیریت پیشرفته خطاها
 * - Logging Integration: یکپارچگی با سیستم لاگ
 * - JSON Processing: پردازش JSON با utility classes
 * - Query Parameters: پشتیبانی از پارامترهای جستجو
 * - HTTP Status Codes: کدهای وضعیت مناسب HTTP
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class ItemController implements HttpHandler {
    
    /** Logger برای ثبت رویدادهای کنترلر */
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    
    /** سرویس مدیریت آیتم‌های غذایی */
    private final ItemService itemService;
    
    /**
     * سازنده پیش‌فرض
     * Dependencies را به صورت خودکار ایجاد می‌کند
     */
    public ItemController() {
        this.itemService = new ItemService(new ItemRepository(), new com.myapp.restaurant.RestaurantRepository());
    }
    
    /**
     * سازنده برای dependency injection (تست)
     * 
     * @param itemService سرویس آیتم‌ها تزریق شده
     */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    /**
     * هندلر اصلی HTTP requests
     * 
     * تمام درخواست‌ها را بر اساس HTTP method و path مسیریابی می‌کند
     * Error handling جامع برای انواع مختلف exceptions
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
        if (path.matches("/api/items/\\d+")) {
            // GET /api/items/{id} - دریافت آیتم بر اساس شناسه
            Long id = extractIdFromPath(path, "/api/items/");
            getItemById(exchange, id);
        } else if (path.matches("/api/items/restaurant/\\d+$")) {
            // GET /api/items/restaurant/{restaurantId} - آیتم‌های رستوران
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/");
            getItemsByRestaurant(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/available")) {
            // GET /api/items/restaurant/{restaurantId}/available - آیتم‌های در دسترس
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/available");
            getAvailableItemsByRestaurant(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/categories")) {
            // GET /api/items/restaurant/{restaurantId}/categories - دسته‌بندی‌ها
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/categories");
            getCategoriesByRestaurant(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/low-stock")) {
            // GET /api/items/restaurant/{restaurantId}/low-stock - آیتم‌های کم موجودی
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/low-stock");
            getLowStockItems(exchange, restaurantId);
        } else if (path.matches("/api/items/restaurant/\\d+/statistics")) {
            // GET /api/items/restaurant/{restaurantId}/statistics - آمار منو
            Long restaurantId = extractIdFromPath(path, "/api/items/restaurant/", "/statistics");
            getMenuStatistics(exchange, restaurantId);
        } else if (path.equals("/api/items/search")) {
            // GET /api/items/search?keyword={keyword} - جستجوی آیتم‌ها
            searchItems(exchange);
        } else if (path.matches("/api/items/category/.+")) {
            // GET /api/items/category/{category} - آیتم‌ها بر اساس دسته‌بندی
            String category = extractStringFromPath(path, "/api/items/category/");
            getItemsByCategory(exchange, category);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * دریافت آیتم غذایی بر اساس شناسه
     * 
     * @param exchange HTTP exchange
     * @param id شناسه آیتم
     */
    private void getItemById(HttpExchange exchange, Long id) throws IOException {
        FoodItem item = itemService.getItem(id);
        sendJsonResponse(exchange, 200, item);
    }
    
    /**
     * دریافت تمام آیتم‌های یک رستوران
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getItemsByRestaurant(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> items = itemService.getRestaurantItems(restaurantId);
        sendJsonResponse(exchange, 200, items);
    }
    
    /**
     * دریافت آیتم‌های در دسترس یک رستوران
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getAvailableItemsByRestaurant(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> items = itemService.getAvailableItems(restaurantId);
        sendJsonResponse(exchange, 200, items);
    }
    
    /**
     * دریافت دسته‌بندی‌های رستوران
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getCategoriesByRestaurant(HttpExchange exchange, Long restaurantId) throws IOException {
        List<String> categories = itemService.getRestaurantCategories(restaurantId);
        Map<String, Object> response = Map.of("categories", categories);
        sendJsonResponse(exchange, 200, response);
    }
    
    /**
     * دریافت آیتم‌های کم موجودی رستوران
     * 
     * Query parameter threshold برای تنظیم آستانه کم موجودی
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getLowStockItems(HttpExchange exchange, Long restaurantId) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        int threshold = 5; // آستانه پیش‌فرض
        if (query != null && query.contains("threshold=")) {
            try {
                threshold = Integer.parseInt(extractQueryParam(query, "threshold"));
            } catch (NumberFormatException e) {
                // استفاده از مقدار پیش‌فرض
            }
        }
        
        List<FoodItem> items = itemService.getLowStockItems(restaurantId, threshold);
        Map<String, Object> response = Map.of("items", items, "threshold", threshold);
        sendJsonResponse(exchange, 200, response);
    }
    
    /**
     * دریافت آمار منوی رستوران
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getMenuStatistics(HttpExchange exchange, Long restaurantId) throws IOException {
        ItemService.MenuStatistics stats = itemService.getMenuStatistics(restaurantId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    /**
     * جستجوی آیتم‌ها بر اساس کلیدواژه
     * 
     * Query parameter keyword الزامی است
     * 
     * @param exchange HTTP exchange
     */
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
    
    /**
     * دریافت آیتم‌ها بر اساس دسته‌بندی
     * 
     * @param exchange HTTP exchange
     * @param category نام دسته‌بندی
     */
    private void getItemsByCategory(HttpExchange exchange, String category) throws IOException {
        List<FoodItem> items = itemService.getItemsByCategory(category);
        Map<String, Object> response = Map.of("items", items, "category", category);
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    /**
     * مدیریت تمام POST requests
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/items")) {
            // POST /api/items - افزودن آیتم جدید
            addItem(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * افزودن آیتم غذایی جدید
     * 
     * JSON Request Body:
     * {
     *   "restaurantId": number,
     *   "name": string,
     *   "description": string (اختیاری),
     *   "price": number,
     *   "category": string,
     *   "imageUrl": string (اختیاری),
     *   "quantity": number (اختیاری، پیش‌فرض 1)
     * }
     * 
     * @param exchange HTTP exchange
     */
    private void addItem(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> requestData = parseJsonRequest(exchange);
            
            // اعتبارسنجی فیلدهای الزامی
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
    
    /**
     * مدیریت تمام PUT requests
     * 
     * شامل عملیات‌های به‌روزرسانی آیتم‌ها
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/items/\\d+$")) {
            // PUT /api/items/{id} - به‌روزرسانی آیتم
            Long id = extractIdFromPath(path, "/api/items/");
            updateItem(exchange, id);
        } else if (path.matches("/api/items/\\d+/availability")) {
            // PUT /api/items/{id}/availability - به‌روزرسانی وضعیت در دسترس بودن
            Long id = extractIdFromPath(path, "/api/items/", "/availability");
            updateItemAvailability(exchange, id);
        } else if (path.matches("/api/items/\\d+/quantity")) {
            // PUT /api/items/{id}/quantity - به‌روزرسانی موجودی
            Long id = extractIdFromPath(path, "/api/items/", "/quantity");
            updateItemQuantity(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * به‌روزرسانی آیتم غذایی
     * 
     * فقط فیلدهای ارائه شده در JSON به‌روزرسانی می‌شوند
     * 
     * JSON Request Body:
     * {
     *   "name": string (اختیاری),
     *   "description": string (اختیاری),
     *   "price": number (اختیاری),
     *   "category": string (اختیاری),
     *   "imageUrl": string (اختیاری),
     *   "quantity": number (اختیاری)
     * }
     * 
     * @param exchange HTTP exchange
     * @param id شناسه آیتم
     */
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
    
    /**
     * به‌روزرسانی وضعیت در دسترس بودن آیتم
     * 
     * JSON Request Body:
     * {
     *   "available": boolean
     * }
     * 
     * @param exchange HTTP exchange
     * @param id شناسه آیتم
     */
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
    
    /**
     * به‌روزرسانی موجودی آیتم
     * 
     * JSON Request Body:
     * {
     *   "quantity": number
     * }
     * 
     * @param exchange HTTP exchange
     * @param id شناسه آیتم
     */
    private void updateItemQuantity(HttpExchange exchange, Long id) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        Integer quantity = MapParsingUtil.getIntegerFromMap(requestData, "quantity");
        
        itemService.updateQuantity(id, quantity);
        sendJsonResponse(exchange, 200, Map.of("message", "Item quantity updated successfully"));
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    /**
     * مدیریت تمام DELETE requests
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/items/\\d+")) {
            // DELETE /api/items/{id} - حذف آیتم
            Long id = extractIdFromPath(path, "/api/items/");
            deleteItem(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * حذف آیتم غذایی
     * 
     * @param exchange HTTP exchange
     * @param id شناسه آیتم
     */
    private void deleteItem(HttpExchange exchange, Long id) throws IOException {
        itemService.deleteItem(id);
        sendJsonResponse(exchange, 200, Map.of("message", "Item deleted successfully"));
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * استخراج شناسه از مسیر URL با prefix
     * 
     * @param path مسیر URL
     * @param prefix پیشوند برای حذف
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix) {
        return Long.parseLong(path.substring(prefix.length()));
    }
    
    /**
     * استخراج شناسه از مسیر URL با prefix و suffix
     * 
     * @param path مسیر URL
     * @param prefix پیشوند برای حذف
     * @param suffix پسوند برای حذف
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        int start = prefix.length();
        int end = path.indexOf(suffix, start);
        return Long.parseLong(path.substring(start, end));
    }
    
    /**
     * استخراج رشته از مسیر URL
     * 
     * @param path مسیر URL
     * @param prefix پیشوند برای حذف
     * @return رشته استخراج شده
     */
    private String extractStringFromPath(String path, String prefix) {
        return path.substring(prefix.length());
    }
    
    /**
     * استخراج پارامتر از query string
     * 
     * @param query رشته query
     * @param param نام پارامتر
     * @return مقدار پارامتر یا null
     */
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
    
    /**
     * پارس درخواست JSON از HTTP request body
     * 
     * از JsonUtil استفاده می‌کند برای سریال‌سازی
     * 
     * @param exchange HTTP exchange
     * @return Map حاوی داده‌های JSON
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        return JsonUtil.fromJson(requestBody, Map.class);
    }
    
    /**
     * ارسال پاسخ JSON
     * 
     * از JsonUtil برای تبدیل Object به JSON استفاده می‌کند
     * 
     * @param exchange HTTP exchange
     * @param statusCode کد وضعیت HTTP
     * @param data داده برای serialize
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", ApplicationConstants.CONTENT_TYPE.APPLICATION_JSON);
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    /**
     * ارسال پاسخ خطا
     * 
     * شامل timestamp و جزئیات بیشتر خطا
     * 
     * @param exchange HTTP exchange
     * @param statusCode کد خطای HTTP
     * @param message پیام خطا
     */
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

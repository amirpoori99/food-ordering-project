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
 * کنترلر REST API برای مدیریت منوی رستوران‌ها
 * 
 * این کلاس تمام endpoint های مربوط به مدیریت منو را ارائه می‌دهد:
 * 
 * === دریافت منو ===
 * GET    /api/menus/restaurant/{restaurantId}                 - دریافت منوی کامل رستوران
 * GET    /api/menus/restaurant/{restaurantId}/available       - دریافت آیتم‌های در دسترس
 * 
 * === مدیریت آیتم‌ها ===
 * POST   /api/menus/restaurant/{restaurantId}/items           - افزودن آیتم به منو
 * PUT    /api/menus/items/{itemId}                           - به‌روزرسانی آیتم منو
 * DELETE /api/menus/items/{itemId}                           - حذف آیتم از منو
 * 
 * === مدیریت وضعیت ===
 * PUT    /api/menus/items/{itemId}/availability              - تنظیم وضعیت در دسترس بودن
 * PUT    /api/menus/items/{itemId}/quantity                  - به‌روزرسانی موجودی آیتم
 * 
 * === دسته‌بندی و فیلتر ===
 * GET    /api/menus/restaurant/{restaurantId}/categories     - دریافت دسته‌بندی‌های منو
 * GET    /api/menus/restaurant/{restaurantId}/category/{cat} - آیتم‌های یک دسته خاص
 * 
 * === گزارش و مانیتورینگ ===
 * GET    /api/menus/restaurant/{restaurantId}/low-stock      - آیتم‌های کم موجودی
 * GET    /api/menus/restaurant/{restaurantId}/statistics     - آمار کامل منو
 * 
 * ویژگی‌های کلیدی:
 * - Menu-focused API: رویکرد متمرکز بر منو (در مقابل item-focused)
 * - Restaurant Context: تمام عملیات در بافت رستوران
 * - Simple JSON Processing: پردازش JSON ساده و کارآمد
 * - Category Management: مدیریت دسته‌بندی آیتم‌ها
 * - Inventory Monitoring: نظارت بر موجودی و وضعیت
 * - Query Parameters: پشتیبانی از پارامترهای جستجو
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class MenuController implements HttpHandler {
    
    /** سرویس مدیریت منو */
    private final MenuService menuService;
    
    /**
     * سازنده پیش‌فرض
     * MenuService را به صورت خودکار ایجاد می‌کند
     */
    public MenuController() {
        this.menuService = new MenuService();
    }
    
    /**
     * سازنده برای dependency injection (تست)
     * 
     * @param menuService سرویس منو تزریق شده
     */
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    /**
     * هندلر اصلی HTTP requests
     * 
     * تمام درخواست‌ها را بر اساس HTTP method و path مسیریابی می‌کند
     * Exception handling جامع برای انواع مختلف خطاها
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
     * از regex pattern matching برای تشخیص endpoint ها استفاده می‌کند
     * 
     * @param exchange HTTP exchange object
     * @param path مسیر درخواست
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/menus/restaurant/\\d+")) {
            // GET /api/menus/restaurant/{restaurantId} - دریافت منوی کامل
            Long restaurantId = extractRestaurantIdFromPath(path);
            getRestaurantMenu(exchange, restaurantId);
        } else if (path.matches("/api/menus/restaurant/\\d+/available")) {
            // GET /api/menus/restaurant/{restaurantId}/available - منوی در دسترس
            Long restaurantId = extractRestaurantIdFromPath(path, "/available");
            getAvailableMenu(exchange, restaurantId);
        } else if (path.matches("/api/menus/restaurant/\\d+/categories")) {
            // GET /api/menus/restaurant/{restaurantId}/categories - دسته‌بندی‌ها
            Long restaurantId = extractRestaurantIdFromPath(path, "/categories");
            getMenuCategories(exchange, restaurantId);
        } else if (path.matches("/api/menus/restaurant/\\d+/category/[^/]+")) {
            // GET /api/menus/restaurant/{restaurantId}/category/{category} - فیلتر دسته
            Long restaurantId = extractRestaurantIdFromPath(path, "/category/[^/]+");
            String category = extractCategoryFromPath(path);
            getMenuByCategory(exchange, restaurantId, category);
        } else if (path.matches("/api/menus/restaurant/\\d+/low-stock")) {
            // GET /api/menus/restaurant/{restaurantId}/low-stock - آیتم‌های کم موجودی
            Long restaurantId = extractRestaurantIdFromPath(path, "/low-stock");
            String thresholdParam = getQueryParameter(exchange, "threshold");
            int threshold = thresholdParam != null ? Integer.parseInt(thresholdParam) : 10;
            getLowStockItems(exchange, restaurantId, threshold);
        } else if (path.matches("/api/menus/restaurant/\\d+/statistics")) {
            // GET /api/menus/restaurant/{restaurantId}/statistics - آمار منو
            Long restaurantId = extractRestaurantIdFromPath(path, "/statistics");
            getMenuStatistics(exchange, restaurantId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * دریافت منوی کامل رستوران
     * 
     * شامل تمام آیتم‌ها، در دسترس و غیر قابل دسترس
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getRestaurantMenu(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> menu = menuService.getRestaurantMenu(restaurantId);
        sendJsonResponse(exchange, 200, menu);
    }
    
    /**
     * دریافت آیتم‌های در دسترس منوی رستوران
     * 
     * فقط آیتم‌هایی که قابل سفارش هستند
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getAvailableMenu(HttpExchange exchange, Long restaurantId) throws IOException {
        List<FoodItem> menu = menuService.getAvailableMenu(restaurantId);
        sendJsonResponse(exchange, 200, menu);
    }
    
    /**
     * دریافت دسته‌بندی‌های منوی رستوران
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getMenuCategories(HttpExchange exchange, Long restaurantId) throws IOException {
        List<String> categories = menuService.getMenuCategories(restaurantId);
        sendJsonResponse(exchange, 200, categories);
    }
    
    /**
     * دریافت آیتم‌های منو بر اساس دسته‌بندی
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     * @param category نام دسته‌بندی
     */
    private void getMenuByCategory(HttpExchange exchange, Long restaurantId, String category) throws IOException {
        List<FoodItem> menu = menuService.getMenuByCategory(restaurantId, category);
        sendJsonResponse(exchange, 200, menu);
    }
    
    /**
     * دریافت آیتم‌های کم موجودی رستوران
     * 
     * Query parameter threshold برای تنظیم آستانه کم موجودی
     * پیش‌فرض: 10 عدد
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     * @param threshold آستانه کم موجودی
     */
    private void getLowStockItems(HttpExchange exchange, Long restaurantId, int threshold) throws IOException {
        List<FoodItem> items = menuService.getLowStockItems(restaurantId, threshold);
        sendJsonResponse(exchange, 200, items);
    }
    
    /**
     * دریافت آمار کامل منوی رستوران
     * 
     * شامل تعداد کل آیتم‌ها، در دسترس، تمام شده، کم موجودی و درصد در دسترس بودن
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
    private void getMenuStatistics(HttpExchange exchange, Long restaurantId) throws IOException {
        MenuService.MenuStatistics stats = menuService.getMenuStatistics(restaurantId);
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
        if (path.matches("/api/menus/restaurant/\\d+/items")) {
            // POST /api/menus/restaurant/{restaurantId}/items - افزودن آیتم به منو
            Long restaurantId = extractRestaurantIdFromPath(path, "/items");
            addItemToMenu(exchange, restaurantId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * افزودن آیتم جدید به منوی رستوران
     * 
     * JSON Request Body:
     * {
     *   "name": string,
     *   "description": string,
     *   "price": number,
     *   "category": string
     * }
     * 
     * @param exchange HTTP exchange
     * @param restaurantId شناسه رستوران
     */
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
    
    /**
     * مدیریت تمام PUT requests
     * 
     * شامل عملیات‌های به‌روزرسانی آیتم‌ها و تنظیمات
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/menus/items/\\d+")) {
            // PUT /api/menus/items/{itemId} - به‌روزرسانی آیتم منو
            Long itemId = extractItemIdFromPath(path);
            updateMenuItem(exchange, itemId);
        } else if (path.matches("/api/menus/items/\\d+/availability")) {
            // PUT /api/menus/items/{itemId}/availability - تنظیم وضعیت در دسترس بودن
            Long itemId = extractItemIdFromPath(path, "/availability");
            setItemAvailability(exchange, itemId);
        } else if (path.matches("/api/menus/items/\\d+/quantity")) {
            // PUT /api/menus/items/{itemId}/quantity - به‌روزرسانی موجودی
            Long itemId = extractItemIdFromPath(path, "/quantity");
            updateItemQuantity(exchange, itemId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * به‌روزرسانی آیتم منو
     * 
     * تنها فیلدهای ارائه شده در JSON به‌روزرسانی می‌شوند
     * 
     * JSON Request Body:
     * {
     *   "name": string (اختیاری),
     *   "description": string (اختیاری),
     *   "price": number (اختیاری),
     *   "category": string (اختیاری),
     *   "quantity": number (اختیاری),
     *   "available": boolean (اختیاری)
     * }
     * 
     * @param exchange HTTP exchange
     * @param itemId شناسه آیتم
     */
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
    
    /**
     * تنظیم وضعیت در دسترس بودن آیتم
     * 
     * JSON Request Body:
     * {
     *   "available": boolean
     * }
     * 
     * @param exchange HTTP exchange
     * @param itemId شناسه آیتم
     */
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
    
    /**
     * به‌روزرسانی موجودی آیتم
     * 
     * JSON Request Body:
     * {
     *   "quantity": number
     * }
     * 
     * @param exchange HTTP exchange
     * @param itemId شناسه آیتم
     */
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
    
    /**
     * مدیریت تمام DELETE requests
     * 
     * @param exchange HTTP exchange
     * @param path مسیر درخواست
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/menus/items/\\d+")) {
            // DELETE /api/menus/items/{itemId} - حذف آیتم از منو
            Long itemId = extractItemIdFromPath(path);
            removeItemFromMenu(exchange, itemId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * حذف آیتم از منوی رستوران
     * 
     * @param exchange HTTP exchange
     * @param itemId شناسه آیتم
     */
    private void removeItemFromMenu(HttpExchange exchange, Long itemId) throws IOException {
        menuService.removeItemFromMenu(itemId);
        sendJsonResponse(exchange, 200, Map.of("message", "Item removed from menu successfully"));
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * استخراج شناسه رستوران از مسیر ساده
     * 
     * Pattern: /api/menus/restaurant/{restaurantId}
     * 
     * @param path مسیر URL
     * @return شناسه رستوران
     */
    private Long extractRestaurantIdFromPath(String path) {
        // استخراج از /api/menus/restaurant/{restaurantId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[4]); // /api/menus/restaurant/{restaurantId}
    }
    
    /**
     * استخراج شناسه رستوران از مسیر با suffix
     * 
     * Pattern: /api/menus/restaurant/{restaurantId}/suffix
     * 
     * @param path مسیر URL
     * @param suffix پسوند برای حذف
     * @return شناسه رستوران
     */
    private Long extractRestaurantIdFromPath(String path, String suffix) {
        // استخراج از /api/menus/restaurant/{restaurantId}/suffix
        String pathWithoutSuffix = path.substring(0, path.lastIndexOf(suffix));
        String[] parts = pathWithoutSuffix.split("/");
        return Long.parseLong(parts[4]);
    }
    
    /**
     * استخراج شناسه آیتم از مسیر ساده
     * 
     * Pattern: /api/menus/items/{itemId}
     * 
     * @param path مسیر URL
     * @return شناسه آیتم
     */
    private Long extractItemIdFromPath(String path) {
        // استخراج از /api/menus/items/{itemId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[4]); // /api/menus/items/{itemId}
    }
    
    /**
     * استخراج شناسه آیتم از مسیر با suffix
     * 
     * Pattern: /api/menus/items/{itemId}/suffix
     * 
     * @param path مسیر URL
     * @param suffix پسوند برای حذف
     * @return شناسه آیتم
     */
    private Long extractItemIdFromPath(String path, String suffix) {
        // استخراج از /api/menus/items/{itemId}/suffix
        String pathWithoutSuffix = path.substring(0, path.lastIndexOf(suffix));
        String[] parts = pathWithoutSuffix.split("/");
        return Long.parseLong(parts[4]);
    }
    
    /**
     * استخراج نام دسته‌بندی از مسیر
     * 
     * Pattern: /api/menus/restaurant/{restaurantId}/category/{category}
     * 
     * @param path مسیر URL
     * @return نام دسته‌بندی
     */
    private String extractCategoryFromPath(String path) {
        // استخراج از /api/menus/restaurant/{restaurantId}/category/{category}
        String[] parts = path.split("/");
        return parts[6]; // /api/menus/restaurant/{restaurantId}/category/{category}
    }
    
    /**
     * دریافت پارامتر از query string
     * 
     * @param exchange HTTP exchange
     * @param paramName نام پارامتر
     * @return مقدار پارامتر یا null
     */
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
    
    /**
     * پارس درخواست JSON
     * 
     * پیاده‌سازی ساده JSON parser برای کاهش dependencies
     * در محیط production از Jackson یا Gson استفاده کنید
     * 
     * @param exchange HTTP exchange
     * @return Map حاوی داده‌های JSON پارس شده
     */
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        // پارس ساده JSON - در production از Jackson یا Gson استفاده کنید
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> result = new HashMap<>();
        
        // پارس ابتدایی JSON (ساده‌سازی شده برای این پیاده‌سازی)
        if (requestBody.trim().isEmpty()) {
            return result;
        }
        
        // حذف آکولاد و تقسیم بر اساس کاما
        String content = requestBody.trim().replaceAll("[{}]", "");
        String[] pairs = content.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                
                // تلاش برای پارس به عنوان عدد یا boolean
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
     * استخراج مقدار Double از Map
     * 
     * @param map نقشه داده‌ها
     * @param key کلید
     * @return مقدار Double یا null
     */
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
    
    /**
     * استخراج مقدار Integer از Map
     * 
     * @param map نقشه داده‌ها
     * @param key کلید
     * @return مقدار Integer یا null
     */
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
    
    /**
     * استخراج مقدار Boolean از Map
     * 
     * @param map نقشه داده‌ها
     * @param key کلید
     * @return مقدار Boolean یا null
     */
    private Boolean getBooleanFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
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
        Map<String, Object> error = Map.of(
            "error", true,
            "message", message,
            "status", statusCode
        );
        sendJsonResponse(exchange, statusCode, error);
    }
    
    /**
     * تبدیل Object به JSON string
     * 
     * سریال‌سازی ساده برای انواع مختلف داده‌ها
     * در محیط production از Jackson یا Gson استفاده کنید
     * 
     * @param data داده برای تبدیل
     * @return رشته JSON
     */
    private String convertToJson(Object data) {
        // سریال‌سازی ساده JSON - در production از Jackson یا Gson استفاده کنید
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
        
        // برای اشیاء پیچیده مثل FoodItem، از reflection استفاده کن
        return serializeObject(data);
    }
    
    /**
     * سریال‌سازی اشیاء پیچیده
     * 
     * سریال‌سازی ساده برای FoodItem و دیگر entity ها
     * 
     * @param obj شیء برای سریال‌سازی
     * @return رشته JSON
     */
    private String serializeObject(Object obj) {
        // سریال‌سازی ساده اشیاء برای FoodItem و entity های دیگر
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
        
        // Fallback به toString
        return "\"" + obj.toString() + "\"";
    }
} 
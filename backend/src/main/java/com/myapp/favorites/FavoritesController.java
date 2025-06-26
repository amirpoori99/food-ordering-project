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
 * کنترلر REST API برای مدیریت علاقه‌مندی‌ها
 * 
 * این کلاس تمام endpoint های HTTP مربوط به مدیریت علاقه‌مندی‌های کاربران را ارائه می‌دهد:
 * 
 * === GET Endpoints ===
 * GET    /api/favorites?userId={id}                    - علاقه‌مندی‌های کاربر
 * GET    /api/favorites/check?userId={u}&restaurantId={r} - بررسی وجود علاقه‌مندی
 * GET    /api/favorites/recent?days={d}                - علاقه‌مندی‌های اخیر
 * GET    /api/favorites/with-notes                     - دارای یادداشت
 * GET    /api/favorites/stats?userId={id}              - آمارهای کاربر
 * GET    /api/favorites/restaurant/{id}                - علاقه‌مندان رستوران
 * GET    /api/favorites/restaurant/{id}?count=true     - تعداد علاقه‌مندان
 * GET    /api/favorites/user/{id}                      - علاقه‌مندی‌های کاربر
 * GET    /api/favorites/user/{id}?count=true           - تعداد علاقه‌مندی‌ها
 * GET    /api/favorites/{id}                           - علاقه‌مندی خاص
 * 
 * === POST Endpoints ===
 * POST   /api/favorites/add                            - اضافه کردن علاقه‌مندی
 * 
 * === PUT Endpoints ===
 * PUT    /api/favorites/{id}/notes                     - به‌روزرسانی یادداشت
 * 
 * === DELETE Endpoints ===
 * DELETE /api/favorites/remove?userId={u}&restaurantId={r} - حذف علاقه‌مندی
 * DELETE /api/favorites/{id}                           - حذف بر اساس ID
 * 
 * === JSON Request/Response Format ===
 * Add Favorite Request:
 * {
 *   "userId": number,
 *   "restaurantId": number,
 *   "notes": string (optional)
 * }
 * 
 * Update Notes Request:
 * {
 *   "notes": string
 * }
 * 
 * Standard Response:
 * {
 *   "success": boolean,
 *   "message": string (optional),
 *   "data": object (optional),
 *   "error": string (on error)
 * }
 * 
 * === ویژگی‌های کلیدی ===
 * - RESTful Design: طراحی مطابق استانداردهای REST
 * - Flexible Endpoints: endpoint های متنوع برای نیازهای مختلف
 * - Query Parameter Support: پشتیبانی کامل از پارامترهای URL
 * - Path Parameter Support: پشتیبانی از path variables
 * - JSON Processing: پردازش کامل JSON requests/responses
 * - Error Handling: مدیریت جامع خطاها
 * - Validation: اعتبارسنجی ورودی‌ها
 * - Logging: ثبت تمام عملیات
 * - CORS Support: پشتیبانی از cross-origin requests
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class FavoritesController implements HttpHandler {
    
    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(FavoritesController.class);
    
    /** سرویس منطق کسب‌وکار علاقه‌مندی‌ها */
    private final FavoritesService favoritesService;
    
    /**
     * سازنده پیش‌فرض - Dependency Injection خودکار
     */
    public FavoritesController() {
        this.favoritesService = new FavoritesService();
    }
    
    /**
     * سازنده برای تست - Manual Dependency Injection
     * 
     * @param favoritesService سرویس علاقه‌مندی‌ها برای تست
     */
    public FavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    /**
     * هندلر اصلی HTTP requests
     * 
     * تمام درخواست‌های HTTP را بر اساس method و path مسیریابی می‌کند
     * شامل error handling جامع و logging مناسب
     * 
     * @param exchange شیء HTTP request/response
     * @throws IOException در صورت خطا در پردازش HTTP
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            // حذف prefix پایه "/api/favorites"
            String endpoint = path.replace("/api/favorites", "");
            if (endpoint.startsWith("/")) {
                endpoint = endpoint.substring(1);
            }
            
            logger.info("🔗 Favorites API: {} {}", method, endpoint);
            
            // مسیریابی بر اساس HTTP method
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
     * مدیریت تمام GET requests
     * 
     * این متد پیچیده‌ترین بخش کنترلر است و انواع مختلف GET endpoints را پشتیبانی می‌کند
     * 
     * @param exchange HTTP exchange object
     * @param endpoint endpoint path بعد از حذف prefix
     * @throws IOException در صورت خطا در پردازش
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
            // بررسی اینکه آیا کاربر خاص، رستوران خاص را علاقه‌مندی کرده است
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
            // دریافت علاقه‌مندی‌های اخیر
            int days = 30; // مقدار پیش‌فرض
            if (query != null && query.contains("days=")) {
                String daysStr = extractQueryParam(query, "days");
                try {
                    days = Integer.parseInt(daysStr);
                    if (days < 1) days = 30; // validation
                } catch (NumberFormatException e) {
                    days = 30; // fallback
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
            // دریافت علاقه‌مندی‌های دارای یادداشت
            List<Favorite> favoritesWithNotes = favoritesService.getFavoritesWithNotes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorites", favoritesWithNotes);
            response.put("count", favoritesWithNotes.size());
            
            sendSuccessResponse(exchange, response);
            
        } else if (endpoint.equals("stats")) {
            // GET /api/favorites/stats?userId=123
            // دریافت آمارهای کامل علاقه‌مندی‌های کاربر
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
            // دریافت اطلاعات علاقه‌مندی برای رستوران خاص
            String restaurantIdStr = endpoint.substring("restaurant/".length());
            Long restaurantId = parseId(restaurantIdStr, "restaurantId");
            
            if (query != null && query.contains("count=true")) {
                // GET /api/favorites/restaurant/123?count=true
                // تنها تعداد علاقه‌مندان را برگردان
                Long count = favoritesService.getRestaurantFavoriteCount(restaurantId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", count);
                
                sendSuccessResponse(exchange, response);
            } else {
                // GET /api/favorites/restaurant/123
                // تمام علاقه‌مندی‌های رستوران را برگردان
                List<Favorite> favorites = favoritesService.getRestaurantFavorites(restaurantId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("favorites", favorites);
                response.put("count", favorites.size());
                
                sendSuccessResponse(exchange, response);
            }
            
        } else if (endpoint.startsWith("user/")) {
            // GET /api/favorites/user/123
            // دریافت اطلاعات علاقه‌مندی برای کاربر خاص
            String userIdStr = endpoint.substring("user/".length());
            Long userId = parseId(userIdStr, "userId");
            
            if (query != null && query.contains("count=true")) {
                // GET /api/favorites/user/123?count=true
                // تنها تعداد علاقه‌مندی‌ها را برگردان
                Long count = favoritesService.getUserFavoriteCount(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("count", count);
                
                sendSuccessResponse(exchange, response);
            } else {
                // GET /api/favorites/user/123
                // تمام علاقه‌مندی‌های کاربر را برگردان
                List<Favorite> favorites = favoritesService.getUserFavorites(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("favorites", favorites);
                response.put("count", favorites.size());
                
                sendSuccessResponse(exchange, response);
            }
            
        } else if (endpoint.matches("\\d+")) {
            // GET /api/favorites/123
            // دریافت علاقه‌مندی خاص بر اساس ID
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
     * مدیریت تمام POST requests
     * 
     * شامل ایجاد علاقه‌مندی جدید
     * 
     * @param exchange HTTP exchange object
     * @param endpoint endpoint path
     * @throws IOException در صورت خطا در پردازش
     */
    @SuppressWarnings("unchecked")
    private void handlePost(HttpExchange exchange, String endpoint) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        
        if (endpoint.isEmpty() || endpoint.equals("add")) {
            // POST /api/favorites/add
            // اضافه کردن رستوران به علاقه‌مندی‌های کاربر
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
     * مدیریت تمام PUT requests
     * 
     * شامل به‌روزرسانی یادداشت‌های علاقه‌مندی
     * 
     * @param exchange HTTP exchange object
     * @param endpoint endpoint path
     * @throws IOException در صورت خطا در پردازش
     */
    @SuppressWarnings("unchecked")
    private void handlePut(HttpExchange exchange, String endpoint) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        
        if (endpoint.matches("\\d+/notes")) {
            // PUT /api/favorites/123/notes
            // به‌روزرسانی یادداشت علاقه‌مندی
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
     * مدیریت تمام DELETE requests
     * 
     * شامل حذف علاقه‌مندی به دو روش مختلف
     * 
     * @param exchange HTTP exchange object
     * @param endpoint endpoint path
     * @throws IOException در صورت خطا در پردازش
     */
    private void handleDelete(HttpExchange exchange, String endpoint) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        
        if (endpoint.isEmpty() || endpoint.equals("remove")) {
            // DELETE /api/favorites/remove?userId=123&restaurantId=456
            // حذف علاقه‌مندی بر اساس userId و restaurantId
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
            // DELETE /api/favorites/123
            // حذف علاقه‌مندی بر اساس ID (معمولاً توسط ادمین)
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
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * تبدیل رشته ID به Long با validation
     * 
     * @param idStr رشته حاوی ID
     * @param fieldName نام فیلد برای نمایش در خطا
     * @return ID تبدیل شده
     * @throws IllegalArgumentException اگر ID نامعتبر باشد
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
     * استخراج مقدار پارامتر از query string
     * 
     * شامل URL decoding برای پشتیبانی از کاراکترهای خاص
     * 
     * @param query query string کامل
     * @param param نام پارامتر مورد نظر
     * @return مقدار پارامتر یا null در صورت عدم وجود
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
                    // URL decoding برای پشتیبانی از کاراکترهای خاص
                    return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                } catch (Exception e) {
                    logger.warn("Failed to decode query parameter {}: {}", param, keyValue[1]);
                    return keyValue[1]; // fallback به raw value
                }
            }
        }
        return null;
    }
    
    /**
     * ارسال پاسخ موفق JSON
     * 
     * @param exchange HTTP exchange
     * @param data داده برای تبدیل به JSON
     * @throws IOException در صورت خطا در ارسال
     */
    private void sendSuccessResponse(HttpExchange exchange, Object data) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        sendResponse(exchange, 200, jsonResponse);
    }
    
    /**
     * ارسال پاسخ خطا JSON
     * 
     * @param exchange HTTP exchange
     * @param statusCode HTTP status code
     * @param message پیام خطا
     * @throws IOException در صورت خطا در ارسال
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        
        String jsonResponse = JsonUtil.toJson(errorResponse);
        sendResponse(exchange, statusCode, jsonResponse);
    }
    
    /**
     * ارسال پاسخ HTTP با تنظیمات کامل
     * 
     * شامل Content-Type، CORS headers و response body
     * 
     * @param exchange HTTP exchange
     * @param statusCode HTTP status code
     * @param response محتوای پاسخ
     * @throws IOException در صورت خطا در ارسال
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // تنظیم headers
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // CORS support
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        
        // ارسال response body
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
} 
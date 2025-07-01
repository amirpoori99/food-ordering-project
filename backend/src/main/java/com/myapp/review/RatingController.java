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

/**
 * Controller REST API برای مدیریت نظرات و امتیازدهی
 * 
 * این کلاس تمام endpoints مربوط به عملیات نظرات و امتیازات را ارائه می‌دهد:
 * 
 * === GET Endpoints (دریافت اطلاعات) ===
 * GET    /api/ratings                      - دریافت تمام نظرات
 * GET    /api/ratings/{id}                 - دریافت نظر بر اساس شناسه
 * GET    /api/ratings/restaurant?restaurantId={id} - دریافت نظرات رستوران خاص
 * GET    /api/ratings/stats?restaurantId={id}      - دریافت آمار امتیازات رستوران
 * 
 * === POST Endpoints (ایجاد) ===
 * POST   /api/ratings                      - ایجاد نظر و امتیاز جدید
 * 
 * === PUT Endpoints (به‌روزرسانی) ===
 * PUT    /api/ratings/{id}                 - به‌روزرسانی نظر موجود
 * 
 * === ویژگی‌های کلیدی ===
 * - JSON Request/Response Processing: پردازش JSON برای همه endpoints
 * - Query Parameter Support: پشتیبانی از query parameters
 * - Path Parameter Extraction: استخراج پارامترها از URL path
 * - Error Handling: مدیریت خطاها با HTTP status codes مناسب
 * - URL Decoding: رمزگشایی URL برای query parameters
 * - Request Validation: اعتبارسنجی درخواست‌ها
 * - Business Logic Delegation: تفویض منطق کسب‌وکار به Service
 * - RESTful Design: طراحی مطابق اصول REST
 * 
 * === Request/Response Examples ===
 * 
 * **POST /api/ratings** - ایجاد نظر:
 * ```json
 * {
 *   "userId": 1,
 *   "restaurantId": 5,
 *   "score": 4,
 *   "reviewText": "غذای خوبی بود"
 * }
 * ```
 * 
 * **PUT /api/ratings/123** - به‌روزرسانی نظر:
 * ```json
 * {
 *   "score": 5,
 *   "reviewText": "غذای عالی بود"
 * }
 * ```
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class RatingController implements HttpHandler {
    
    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    /** سرویس منطق کسب‌وکار نظرات */
    private final RatingService ratingService;
    
    /**
     * سازنده پیش‌فرض - RatingService را ایجاد می‌کند
     */
    public RatingController() {
        this.ratingService = new RatingService();
    }
    
    /**
     * سازنده برای تزریق وابستگی (برای تست‌ها)
     * 
     * @param ratingService سرویس نظرات
     */
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }
    
    /**
     * متد اصلی پردازش HTTP requests
     * 
     * این متد requests را بر اساس HTTP method به متدهای مناسب هدایت می‌کند
     * 
     * @param exchange شیء HttpExchange حاوی اطلاعات request و response
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
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
    
    /**
     * پردازش تمام GET requests
     * 
     * این متد path و query parameters را بررسی کرده و درخواست را به handler مناسب ارسال می‌کند
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQueryParams(query);
        
        try {
            if (path.equals("/api/ratings")) {
                // GET /api/ratings - دریافت تمام نظرات
                handleGetAllRatings(exchange, params);
            } else if (path.matches("/api/ratings/\\d+")) {
                // GET /api/ratings/{id} - دریافت نظر بر اساس شناسه
                Long ratingId = extractIdFromPath(path);
                handleGetRating(exchange, ratingId);
            } else if (path.equals("/api/ratings/restaurant")) {
                // GET /api/ratings/restaurant?restaurantId={id} - دریافت نظرات رستوران
                String restaurantIdStr = params.get("restaurantId");
                if (restaurantIdStr == null) {
                    sendResponse(exchange, 400, Map.of("error", "Restaurant ID is required"));
                    return;
                }
                Long restaurantId = Long.parseLong(restaurantIdStr);
                handleGetRestaurantRatings(exchange, restaurantId);
            } else if (path.equals("/api/ratings/stats")) {
                // GET /api/ratings/stats?restaurantId={id} - دریافت آمار امتیازات
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
            // خطای فرمت عددی
            sendResponse(exchange, 400, Map.of("error", "Invalid number format"));
        } catch (IllegalArgumentException e) {
            // خطای ورودی نامعتبر
            sendResponse(exchange, 400, Map.of("error", "Bad request"));
        } catch (NotFoundException e) {
            // خطای عدم وجود resource
            sendResponse(exchange, 404, Map.of("error", "Not found"));
        }
    }
    
    /**
     * پردازش تمام POST requests
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/api/ratings")) {
                // POST /api/ratings - ایجاد نظر جدید
                handleCreateRating(exchange);
            } else {
                sendResponse(exchange, 404, Map.of("error", "Endpoint not found"));
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Bad request"));
        }
    }
    
    /**
     * پردازش تمام PUT requests
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.matches("/api/ratings/\\d+")) {
                // PUT /api/ratings/{id} - به‌روزرسانی نظر
                Long ratingId = extractIdFromPath(path);
                handleUpdateRating(exchange, ratingId);
            } else {
                sendResponse(exchange, 404, Map.of("error", "Endpoint not found"));
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Bad request"));
        }
    }
    
    /**
     * پردازش ایجاد نظر جدید
     * 
     * Request Body JSON Fields:
     * - userId (required): شناسه کاربر نظردهنده
     * - restaurantId (required): شناسه رستوران
     * - score (required): امتیاز (1-5)
     * - reviewText (optional): متن نظر
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
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
    
    /**
     * پردازش به‌روزرسانی نظر موجود
     * 
     * Request Body JSON Fields (همه اختیاری):
     * - score: امتیاز جدید (1-5)
     * - reviewText: متن نظر جدید
     * 
     * @param exchange شیء HttpExchange
     * @param ratingId شناسه نظر
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    @SuppressWarnings("unchecked")
    private void handleUpdateRating(HttpExchange exchange, Long ratingId) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        Integer newScore = request.get("score") != null ? ((Number) request.get("score")).intValue() : null;
        String newReviewText = (String) request.get("reviewText");
        
        Rating rating = ratingService.updateRating(ratingId, newScore, newReviewText);
        sendResponse(exchange, 200, rating);
    }
    
    /**
     * دریافت نظر بر اساس شناسه
     * 
     * @param exchange شیء HttpExchange
     * @param ratingId شناسه نظر
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleGetRating(HttpExchange exchange, Long ratingId) throws IOException {
        Rating rating = ratingService.getRating(ratingId);
        sendResponse(exchange, 200, rating);
    }
    
    /**
     * دریافت تمام نظرات (برای مدیریت)
     * 
     * @param exchange شیء HttpExchange
     * @param params پارامترهای query (آینده: pagination)
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleGetAllRatings(HttpExchange exchange, Map<String, String> params) throws IOException {
        List<Rating> ratings = ratingService.getAllRatings();
        sendResponse(exchange, 200, ratings);
    }
    
    /**
     * دریافت تمام نظرات رستوران خاص
     * 
     * @param exchange شیء HttpExchange
     * @param restaurantId شناسه رستوران
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleGetRestaurantRatings(HttpExchange exchange, Long restaurantId) throws IOException {
        List<Rating> ratings = ratingService.getRestaurantRatings(restaurantId);
        sendResponse(exchange, 200, ratings);
    }
    
    /**
     * دریافت آمار امتیازات رستوران
     * 
     * Response شامل:
     * - averageRating: میانگین امتیاز
     * - totalRatings: تعداد کل نظرات
     * - distribution: توزیع امتیازات (1-5)
     * 
     * @param exchange شیء HttpExchange
     * @param restaurantId شناسه رستوران
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleGetRatingStats(HttpExchange exchange, Long restaurantId) throws IOException {
        RatingService.RatingStats stats = ratingService.getRestaurantRatingStats(restaurantId);
        sendResponse(exchange, 200, stats);
    }
    
    /**
     * استخراج شناسه از انتهای path
     * 
     * مثال: "/api/ratings/123" -> 123
     * 
     * @param path مسیر URL
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
    
    /**
     * پارس کردن query parameters از URL
     * 
     * این متد query string را پارس کرده و پارامترها را URL decode می‌کند
     * 
     * @param query رشته query parameters
     * @return Map حاوی پارامترهای پارس شده
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        // URL decode برای پشتیبانی از کاراکترهای خاص
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
    
    /**
     * ارسال پاسخ JSON با status code مشخص
     * 
     * @param exchange شیء HttpExchange
     * @param statusCode کد وضعیت HTTP
     * @param data داده برای serialize کردن به JSON
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void sendResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String response = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
} 

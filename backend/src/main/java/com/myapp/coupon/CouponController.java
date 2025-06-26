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
 * Controller REST API برای مدیریت کوپن‌های تخفیف
 * 
 * این کلاس تمام endpoints مربوط به عملیات کوپن‌ها را ارائه می‌دهد:
 * 
 * === GET Endpoints (دریافت اطلاعات) ===
 * GET    /api/coupons/{id}                 - دریافت کوپن بر اساس شناسه
 * GET    /api/coupons/code/{code}          - دریافت کوپن بر اساس کد
 * GET    /api/coupons/valid                - دریافت تمام کوپن‌های معتبر
 * GET    /api/coupons/restaurant/{id}      - دریافت کوپن‌های رستوران خاص
 * GET    /api/coupons/global               - دریافت کوپن‌های سراسری
 * GET    /api/coupons/applicable           - دریافت کوپن‌های قابل اعمال (با query params)
 * GET    /api/coupons/statistics           - دریافت آمار کوپن‌ها
 * GET    /api/coupons/expiring             - دریافت کوپن‌های نزدیک به انقضا
 * 
 * === POST Endpoints (ایجاد و عملیات) ===
 * POST   /api/coupons                      - ایجاد کوپن جدید
 * POST   /api/coupons/{id}/activate        - فعال‌سازی کوپن
 * POST   /api/coupons/{id}/deactivate      - غیرفعال‌سازی کوپن
 * POST   /api/coupons/apply                - اعمال کوپن به سفارش
 * 
 * === PUT Endpoints (به‌روزرسانی) ===
 * PUT    /api/coupons/{id}                 - به‌روزرسانی اطلاعات کوپن
 * 
 * === DELETE Endpoints (حذف) ===
 * DELETE /api/coupons/{id}                 - حذف کوپن
 * 
 * === ویژگی‌های کلیدی ===
 * - Request/Response JSON Processing: پردازش JSON برای همه endpoints
 * - Parameter Validation: اعتبارسنجی کامل پارامترها
 * - Error Handling: مدیریت خطاها با HTTP status codes مناسب
 * - Type Conversion: تبدیل انواع داده‌ها (String، Long، Double، Integer)
 * - DateTime Parsing: پارس کردن تاریخ و زمان با ISO format
 * - Query Parameter Support: پشتیبانی از query parameters
 * - Path Parameter Extraction: استخراج پارامترها از URL path
 * - Permission-based Operations: عملیات مبتنی بر مجوزهای کاربر
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class CouponController implements HttpHandler {
    
    /** سرویس منطق کسب‌وکار کوپن‌ها */
    private final CouponService couponService;
    
    /**
     * سازنده پیش‌فرض - CouponService را ایجاد می‌کند
     */
    public CouponController() {
        this.couponService = new CouponService();
    }
    
    /**
     * سازنده برای تزریق وابستگی (برای تست‌ها)
     * 
     * @param couponService سرویس کوپن‌ها
     */
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
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
            // خطاهای validation (400 Bad Request)
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (NotFoundException e) {
            // خطاهای عدم وجود resource (404 Not Found)
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            // خطاهای داخلی سرور (500 Internal Server Error)
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    /**
     * پردازش تمام GET requests
     * 
     * این متد path را بررسی کرده و درخواست را به handler مناسب ارسال می‌کند
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/coupons/\\d+")) {
            // GET /api/coupons/{id} - دریافت کوپن بر اساس شناسه
            Long id = extractIdFromPath(path);
            getCouponById(exchange, id);
        } else if (path.matches("/api/coupons/code/.+")) {
            // GET /api/coupons/code/{code} - دریافت کوپن بر اساس کد
            String code = extractCodeFromPath(path);
            getCouponByCode(exchange, code);
        } else if (path.equals("/api/coupons/valid")) {
            // GET /api/coupons/valid - دریافت تمام کوپن‌های معتبر
            getValidCoupons(exchange);
        } else if (path.matches("/api/coupons/restaurant/\\d+")) {
            // GET /api/coupons/restaurant/{id} - دریافت کوپن‌های رستوران
            Long restaurantId = extractIdFromPath(path, "/api/coupons/restaurant/");
            getRestaurantCoupons(exchange, restaurantId);
        } else if (path.equals("/api/coupons/global")) {
            // GET /api/coupons/global - دریافت کوپن‌های سراسری
            getGlobalCoupons(exchange);
        } else if (path.equals("/api/coupons/applicable")) {
            // GET /api/coupons/applicable - دریافت کوپن‌های قابل اعمال
            getApplicableCoupons(exchange);
        } else if (path.equals("/api/coupons/statistics")) {
            // GET /api/coupons/statistics - دریافت آمار کوپن‌ها
            getCouponStatistics(exchange);
        } else if (path.equals("/api/coupons/expiring")) {
            // GET /api/coupons/expiring - دریافت کوپن‌های نزدیک به انقضا
            getCouponsExpiringSoon(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * دریافت کوپن بر اساس شناسه
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه کوپن
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getCouponById(HttpExchange exchange, Long id) throws IOException {
        Coupon coupon = couponService.getCoupon(id);
        sendJsonResponse(exchange, 200, coupon);
    }
    
    /**
     * دریافت کوپن بر اساس کد
     * 
     * @param exchange شیء HttpExchange
     * @param code کد کوپن
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getCouponByCode(HttpExchange exchange, String code) throws IOException {
        Coupon coupon = couponService.getCouponByCode(code);
        sendJsonResponse(exchange, 200, coupon);
    }
    
    /**
     * دریافت تمام کوپن‌های معتبر
     * 
     * کوپن‌هایی که فعال و در بازه زمانی معتبر هستند
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getValidCoupons(HttpExchange exchange) throws IOException {
        List<Coupon> coupons = couponService.getValidCoupons();
        sendJsonResponse(exchange, 200, coupons);
    }
    
    /**
     * دریافت کوپن‌های رستوران خاص
     * 
     * @param exchange شیء HttpExchange
     * @param restaurantId شناسه رستوران
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getRestaurantCoupons(HttpExchange exchange, Long restaurantId) throws IOException {
        List<Coupon> coupons = couponService.getRestaurantCoupons(restaurantId);
        sendJsonResponse(exchange, 200, coupons);
    }
    
    /**
     * دریافت کوپن‌های سراسری
     * 
     * کوپن‌هایی که برای تمام رستوران‌ها قابل استفاده هستند
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getGlobalCoupons(HttpExchange exchange) throws IOException {
        List<Coupon> coupons = couponService.getGlobalCoupons();
        sendJsonResponse(exchange, 200, coupons);
    }
    
    /**
     * دریافت کوپن‌های قابل اعمال برای سفارش
     * 
     * Query Parameters:
     * - orderAmount (required): مبلغ سفارش
     * - restaurantId (optional): شناسه رستوران
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
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
    
    /**
     * دریافت آمار کوپن‌ها
     * 
     * شامل تعداد کل، فعال، نزدیک به انقضا و منقضی
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getCouponStatistics(HttpExchange exchange) throws IOException {
        CouponService.CouponStatistics stats = couponService.getCouponStatistics();
        sendJsonResponse(exchange, 200, stats);
    }
    
    /**
     * دریافت کوپن‌های نزدیک به انقضا
     * 
     * Query Parameters:
     * - days (optional): تعداد روزهای آینده (پیش‌فرض: 7 روز)
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getCouponsExpiringSoon(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String daysStr = queryParams.get("days");
        
        int days = 7; // پیش‌فرض 7 روز
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
    
    /**
     * پردازش تمام POST requests
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/coupons")) {
            // POST /api/coupons - ایجاد کوپن جدید
            createCoupon(exchange);
        } else if (path.matches("/api/coupons/\\d+/activate")) {
            // POST /api/coupons/{id}/activate - فعال‌سازی کوپن
            Long id = extractIdFromPath(path, "/api/coupons/", "/activate");
            activateCoupon(exchange, id);
        } else if (path.matches("/api/coupons/\\d+/deactivate")) {
            // POST /api/coupons/{id}/deactivate - غیرفعال‌سازی کوپن
            Long id = extractIdFromPath(path, "/api/coupons/", "/deactivate");
            deactivateCoupon(exchange, id);
        } else if (path.equals("/api/coupons/apply")) {
            // POST /api/coupons/apply - اعمال کوپن به سفارش
            applyCoupon(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * ایجاد کوپن جدید
     * 
     * Request Body JSON Fields:
     * - code (required): کد کوپن
     * - description (required): توضیحات
     * - type (required): نوع کوپن (PERCENTAGE | FIXED_AMOUNT)
     * - value (required): مقدار تخفیف
     * - validFrom (required): تاریخ شروع اعتبار
     * - validUntil (required): تاریخ پایان اعتبار
     * - createdBy (required): شناسه ایجادکننده
     * - restaurantId (optional): شناسه رستوران
     * - minOrderAmount (optional): حداقل مبلغ سفارش
     * - maxDiscountAmount (optional): حداکثر مبلغ تخفیف
     * - usageLimit (optional): محدودیت کل استفاده
     * - perUserLimit (optional): محدودیت هر کاربر
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void createCoupon(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        
        // پارامترهای اجباری
        String code = getStringFromMap(requestData, "code");
        String description = getStringFromMap(requestData, "description");
        String typeStr = getStringFromMap(requestData, "type");
        Double value = getDoubleFromMap(requestData, "value");
        String validFromStr = getStringFromMap(requestData, "validFrom");
        String validUntilStr = getStringFromMap(requestData, "validUntil");
        Long createdBy = getLongFromMap(requestData, "createdBy");
        Long restaurantId = getLongFromMap(requestData, "restaurantId"); // اختیاری
        
        // پارامترهای اختیاری
        Double minOrderAmount = getDoubleFromMap(requestData, "minOrderAmount");
        Double maxDiscountAmount = getDoubleFromMap(requestData, "maxDiscountAmount");
        Integer usageLimit = getIntegerFromMap(requestData, "usageLimit");
        Integer perUserLimit = getIntegerFromMap(requestData, "perUserLimit");
        
        // پارس کردن تاریخ‌ها
        LocalDateTime validFrom = parseDateTime(validFromStr);
        LocalDateTime validUntil = parseDateTime(validUntilStr);
        
        // پارس کردن نوع کوپن
        Coupon.CouponType type;
        try {
            type = Coupon.CouponType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            sendErrorResponse(exchange, 400, "Invalid coupon type. Must be PERCENTAGE or FIXED_AMOUNT");
            return;
        }
        
        // ایجاد کوپن
        Coupon coupon;
        if (minOrderAmount != null || maxDiscountAmount != null || usageLimit != null || perUserLimit != null) {
            // ایجاد کوپن با تنظیمات پیشرفته
            coupon = couponService.createCouponWithSettings(
                code, description, type, value, validFrom, validUntil,
                minOrderAmount, maxDiscountAmount, usageLimit, perUserLimit,
                createdBy, restaurantId);
        } else {
            // ایجاد کوپن ساده
            if (type == Coupon.CouponType.PERCENTAGE) {
                coupon = couponService.createPercentageCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
            } else {
                coupon = couponService.createFixedAmountCoupon(code, description, value, validFrom, validUntil, createdBy, restaurantId);
            }
        }
        
        sendJsonResponse(exchange, 201, coupon);
    }
    
    /**
     * فعال‌سازی کوپن
     * 
     * Request Body JSON Fields:
     * - activatedBy (required): شناسه فعال‌کننده
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه کوپن
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void activateCoupon(HttpExchange exchange, Long id) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        Long activatedBy = getLongFromMap(requestData, "activatedBy");
        
        couponService.activateCoupon(id, activatedBy);
        sendJsonResponse(exchange, 200, Map.of("message", "Coupon activated successfully"));
    }
    
    /**
     * غیرفعال‌سازی کوپن
     * 
     * Request Body JSON Fields:
     * - deactivatedBy (required): شناسه غیرفعال‌کننده
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه کوپن
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void deactivateCoupon(HttpExchange exchange, Long id) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        Long deactivatedBy = getLongFromMap(requestData, "deactivatedBy");
        
        couponService.deactivateCoupon(id, deactivatedBy);
        sendJsonResponse(exchange, 200, Map.of("message", "Coupon deactivated successfully"));
    }
    
    /**
     * اعمال کوپن به سفارش
     * 
     * Request Body JSON Fields:
     * - couponCode (required): کد کوپن
     * - orderAmount (required): مبلغ سفارش
     * - restaurantId (required): شناسه رستوران
     * - userId (required): شناسه کاربر
     * 
     * Response:
     * - success: true/false
     * - coupon: شیء کوپن (در صورت موفقیت)
     * - discountAmount: مبلغ تخفیف (در صورت موفقیت)
     * - errorMessage: پیام خطا (در صورت ناموفقی بودن)
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
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
    
    /**
     * پردازش تمام PUT requests
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/coupons/\\d+")) {
            // PUT /api/coupons/{id} - به‌روزرسانی کوپن
            Long id = extractIdFromPath(path);
            updateCoupon(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * به‌روزرسانی اطلاعات کوپن
     * 
     * Request Body JSON Fields (همه اختیاری):
     * - description: توضیحات جدید
     * - minOrderAmount: حداقل مبلغ سفارش
     * - maxDiscountAmount: حداکثر مبلغ تخفیف
     * - usageLimit: محدودیت کل استفاده
     * - perUserLimit: محدودیت هر کاربر
     * - validUntil: تاریخ انقضا
     * - updatedBy (required): شناسه به‌روزرسانی کننده
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه کوپن
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
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
    
    /**
     * پردازش تمام DELETE requests
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/coupons/\\d+")) {
            // DELETE /api/coupons/{id} - حذف کوپن
            Long id = extractIdFromPath(path);
            deleteCoupon(exchange, id);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * حذف کوپن
     * 
     * Request Body JSON Fields:
     * - deletedBy (required): شناسه حذف‌کننده
     * 
     * @param exchange شیء HttpExchange
     * @param id شناسه کوپن
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void deleteCoupon(HttpExchange exchange, Long id) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> requestData = parseJsonRequest(requestBody);
        Long deletedBy = getLongFromMap(requestData, "deletedBy");
        
        couponService.deleteCoupon(id, deletedBy);
        sendJsonResponse(exchange, 200, Map.of("message", "Coupon deleted successfully"));
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * پارس کردن JSON request body
     * 
     * @param requestBody محتوای JSON request
     * @return Map حاوی داده‌های پارس شده
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonRequest(String requestBody) {
        return JsonUtil.fromJson(requestBody, Map.class);
    }
    
    /**
     * استخراج شناسه از انتهای path
     * 
     * مثال: "/api/coupons/123" -> 123
     * 
     * @param path مسیر URL
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
    
    /**
     * استخراج شناسه از path با prefix مشخص
     * 
     * @param path مسیر URL
     * @param prefix پیشوند مسیر
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix) {
        String idStr = path.substring(prefix.length());
        return Long.parseLong(idStr);
    }
    
    /**
     * استخراج شناسه از path با prefix و suffix
     * 
     * @param path مسیر URL
     * @param prefix پیشوند مسیر
     * @param suffix پسوند مسیر
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String idStr = path.substring(prefix.length(), path.length() - suffix.length());
        return Long.parseLong(idStr);
    }
    
    /**
     * استخراج کد کوپن از path
     * 
     * @param path مسیر URL
     * @return کد کوپن استخراج شده
     */
    private String extractCodeFromPath(String path) {
        return path.substring("/api/coupons/code/".length());
    }
    
    /**
     * پارس کردن رشته تاریخ و زمان به LocalDateTime
     * 
     * فرمت مورد انتظار: ISO Local DateTime (yyyy-MM-ddTHH:mm:ss)
     * 
     * @param dateTimeStr رشته تاریخ و زمان
     * @return شیء LocalDateTime
     * @throws IllegalArgumentException در صورت فرمت نامعتبر
     */
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
    
    /**
     * پارس کردن query parameters از URL
     * 
     * @param query رشته query parameters
     * @return Map حاوی پارامترها
     */
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
    
    /**
     * دریافت مقدار String از Map
     * 
     * @param map Map حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار String یا null
     */
    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * دریافت مقدار Long از Map با تبدیل انواع مختلف
     * 
     * @param map Map حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار Long یا null
     * @throws IllegalArgumentException در صورت نوع نامعتبر
     */
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
    
    /**
     * دریافت مقدار Double از Map با تبدیل انواع مختلف
     * 
     * @param map Map حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار Double یا null
     * @throws IllegalArgumentException در صورت نوع نامعتبر
     */
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
    
    /**
     * دریافت مقدار Integer از Map با تبدیل انواع مختلف
     * 
     * @param map Map حاوی داده‌ها
     * @param key کلید مورد نظر
     * @return مقدار Integer یا null
     * @throws IllegalArgumentException در صورت نوع نامعتبر
     */
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
    
    /**
     * ارسال پاسخ JSON با status code مشخص
     * 
     * @param exchange شیء HttpExchange
     * @param statusCode کد وضعیت HTTP
     * @param data داده برای serialize کردن به JSON
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    /**
     * ارسال پاسخ خطا با پیام مشخص
     * 
     * @param exchange شیء HttpExchange
     * @param statusCode کد وضعیت HTTP خطا
     * @param message پیام خطا
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String jsonResponse = "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
} 
package com.myapp.vendor;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API Controller برای عملیات فروشندگان (دیدگاه مشتری)
 * 
 * این کلاس تمام endpoints مربوط به عملیات فروشندگان از دیدگاه مشتری را ارائه می‌دهد:
 * 
 * === GET Endpoints (دریافت اطلاعات) ===
 * GET    /api/vendors                     - دریافت تمام فروشندگان فعال
 * GET    /api/vendors/search              - جستجوی فروشندگان بر اساس کلمه کلیدی
 * GET    /api/vendors/{id}                - دریافت جزئیات فروشنده و منو
 * GET    /api/vendors/{id}/menu           - دریافت منوی فروشنده سازماندهی شده بر اساس دسته‌ها
 * GET    /api/vendors/{id}/stats          - دریافت آمار فروشنده
 * GET    /api/vendors/{id}/available      - بررسی پذیرش سفارش توسط فروشنده
 * GET    /api/vendors/location/{location} - دریافت فروشندگان بر اساس موقعیت
 * GET    /api/vendors/category/{category} - دریافت فروشندگان بر اساس دسته غذایی
 * GET    /api/vendors/featured            - دریافت فروشندگان برجسته/محبوب
 * 
 * === POST Endpoints (ارسال داده) ===
 * POST   /api/vendors/filter              - فیلتر فروشندگان بر اساس معیارهای مختلف
 * 
 * === ویژگی‌های کلیدی ===
 * - Customer-Focused Design: طراحی مختص دیدگاه مشتری
 * - RESTful Architecture: معماری مطابق اصول REST
 * - JSON Processing: پردازش کامل JSON requests/responses
 * - Error Handling: مدیریت جامع خطاها با HTTP status codes مناسب
 * - URL Parameter Support: پشتیبانی از path و query parameters
 * - Input Validation: اعتبارسنجی ورودی‌ها
 * - Unicode Support: پشتیبانی کامل از متن فارسی و Unicode
 * - Security: محافظت از SQL Injection و سایر حملات
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class VendorController implements HttpHandler {
    
    /** سرویس منطق کسب‌وکار فروشندگان */
    private final VendorService vendorService;
    
    /**
     * سازنده پیش‌فرض - VendorService را ایجاد می‌کند
     */
    public VendorController() {
        this.vendorService = new VendorService();
    }
    
    /**
     * سازنده برای تزریق وابستگی (برای تست‌ها)
     * 
     * @param vendorService سرویس فروشندگان برای تزریق
     */
    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    
    /**
     * هندلر اصلی HTTP requests
     * 
     * تمام درخواست‌های HTTP را بر اساس method مسیریابی می‌کند
     * شامل مدیریت خطاها و validation های مناسب
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
    
    /**
     * مدیریت تمام GET requests
     * 
     * این متد path را تجزیه کرده و درخواست را به handler مناسب ارسال می‌کند
     * شامل validation برای ID های عددی و پارامترهای مسیر
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/vendors")) {
            // GET /api/vendors - دریافت تمام فروشندگان فعال
            getAllVendors(exchange);
        } else if (path.equals("/api/vendors/search")) {
            // GET /api/vendors/search?q={keyword} - جستجوی فروشندگان
            searchVendors(exchange);
        } else if (path.matches("/api/vendors/\\d+$")) {
            // GET /api/vendors/{id} - دریافت جزئیات فروشنده و منو
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/");
                getVendorDetails(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/\\d+/menu")) {
            // GET /api/vendors/{id}/menu - دریافت منوی فروشنده
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/", "/menu");
                getVendorMenu(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/\\d+/stats")) {
            // GET /api/vendors/{id}/stats - دریافت آمار فروشنده
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/", "/stats");
                getVendorStats(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/\\d+/available")) {
            // GET /api/vendors/{id}/available - بررسی پذیرش سفارش توسط فروشنده
            try {
                Long vendorId = extractIdFromPath(path, "/api/vendors/", "/available");
                checkVendorAvailability(exchange, vendorId);
            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        } else if (path.matches("/api/vendors/location/.+")) {
            // GET /api/vendors/location/{location} - دریافت فروشندگان بر اساس موقعیت
            String location = extractStringFromPath(path, "/api/vendors/location/");
            getVendorsByLocation(exchange, location);
        } else if (path.matches("/api/vendors/category/.+")) {
            // GET /api/vendors/category/{category} - دریافت فروشندگان بر اساس دسته
            String category = extractStringFromPath(path, "/api/vendors/category/");
            getVendorsByCategory(exchange, category);
        } else if (path.equals("/api/vendors/featured")) {
            // GET /api/vendors/featured - دریافت فروشندگان برجسته
            getFeaturedVendors(exchange);
        } else if (path.matches("/api/vendors/[^/]+$") && !path.matches("/api/vendors/\\d+$")) {
            // مدیریت ID های نامعتبر فروشنده (غیر عددی)
            sendErrorResponse(exchange, 500, "Internal server error");
        } else if (path.matches("/api/vendors/[^/]+/(menu|stats|available)") && !path.matches("/api/vendors/\\d+/(menu|stats|available)")) {
            // مدیریت ID های نامعتبر در sub-endpoints
            sendErrorResponse(exchange, 500, "Internal server error");
        } else if (path.matches("/api/vendors/location/?$")) {
            // مدیریت موقعیت خالی
            sendErrorResponse(exchange, 400, "Location cannot be empty");
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * دریافت تمام فروشندگان فعال
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getAllVendors(HttpExchange exchange) throws IOException {
        List<Restaurant> vendors = vendorService.getAllVendors();
        sendJsonResponse(exchange, 200, vendors);
    }
    
    /**
     * جستجوی فروشندگان بر اساس کلمه کلیدی
     * 
     * پارامتر جستجو از query string استخراج می‌شود (q=keyword)
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void searchVendors(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String searchTerm = "";
        
        if (query != null && query.contains("q=")) {
            searchTerm = extractQueryParam(query, "q");
        }
        
        List<Restaurant> vendors = vendorService.searchVendors(searchTerm);
        Map<String, Object> response = Map.of(
            "vendors", vendors,
            "searchTerm", searchTerm,
            "count", vendors.size()
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    /**
     * دریافت جزئیات کامل فروشنده
     * 
     * شامل validation برای ID مثبت و مدیریت خطاهای مختلف
     * 
     * @param exchange شیء HttpExchange
     * @param vendorId شناسه فروشنده
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getVendorDetails(HttpExchange exchange, Long vendorId) throws IOException {
        try {
            if (vendorId <= 0) {
                sendErrorResponse(exchange, 400, "Vendor ID must be positive");
                return;
            }
            Restaurant vendor = vendorService.getVendor(vendorId);
            sendJsonResponse(exchange, 200, vendor);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals("NotFoundException")) {
                sendErrorResponse(exchange, 404, e.getMessage());
            } else {
                sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
    }
    
    /**
     * دریافت منوی فروشنده سازماندهی شده بر اساس دسته‌ها
     * 
     * @param exchange شیء HttpExchange
     * @param vendorId شناسه فروشنده
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getVendorMenu(HttpExchange exchange, Long vendorId) throws IOException {
        Map<String, Object> menuData = vendorService.getVendorMenu(vendorId);
        sendJsonResponse(exchange, 200, menuData);
    }
    
    /**
     * دریافت آمار فروشنده
     * 
     * شامل تعداد آیتم‌ها، دسته‌ها و سایر اطلاعات آماری
     * 
     * @param exchange شیء HttpExchange
     * @param vendorId شناسه فروشنده
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getVendorStats(HttpExchange exchange, Long vendorId) throws IOException {
        VendorService.VendorStats stats = vendorService.getVendorStats(vendorId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    /**
     * بررسی پذیرش سفارش توسط فروشنده
     * 
     * @param exchange شیء HttpExchange
     * @param vendorId شناسه فروشنده
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void checkVendorAvailability(HttpExchange exchange, Long vendorId) throws IOException {
        boolean isAvailable = vendorService.isVendorAcceptingOrders(vendorId);
        Map<String, Object> response = Map.of(
            "vendorId", vendorId,
            "acceptingOrders", isAvailable
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    /**
     * دریافت فروشندگان بر اساس موقعیت جغرافیایی
     * 
     * @param exchange شیء HttpExchange
     * @param location نام موقعیت/منطقه
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getVendorsByLocation(HttpExchange exchange, String location) throws IOException {
        try {
            List<Restaurant> vendors = vendorService.getVendorsByLocation(location);
            Map<String, Object> response = Map.of(
                "vendors", vendors,
                "location", location,
                "count", vendors.size()
            );
            sendJsonResponse(exchange, 200, response);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * دریافت فروشندگان بر اساس دسته غذایی
     * 
     * @param exchange شیء HttpExchange
     * @param category نام دسته غذایی
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getVendorsByCategory(HttpExchange exchange, String category) throws IOException {
        List<Restaurant> vendors = vendorService.getVendorsByCategory(category);
        Map<String, Object> response = Map.of(
            "vendors", vendors,
            "category", category,
            "count", vendors.size()
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    /**
     * دریافت فروشندگان برجسته/محبوب
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getFeaturedVendors(HttpExchange exchange) throws IOException {
        List<Restaurant> vendors = vendorService.getFeaturedVendors();
        Map<String, Object> response = Map.of(
            "vendors", vendors,
            "count", vendors.size()
        );
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    /**
     * مدیریت تمام POST requests
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/vendors/filter")) {
            // POST /api/vendors/filter - فیلتر فروشندگان بر اساس معیارهای مختلف
            filterVendors(exchange);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * فیلتر فروشندگان بر اساس معیارهای مختلف
     * 
     * این endpoint امکان فیلتر کردن فروشندگان بر اساس ترکیبی از:
     * - موقعیت جغرافیایی
     * - دسته غذایی  
     * - عبارت جستجو در نام
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void filterVendors(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        String location = getOptionalStringFromMap(requestData, "location", null);
        String category = getOptionalStringFromMap(requestData, "category", null);
        String search = getOptionalStringFromMap(requestData, "search", null);
        
        // استفاده از متد filter در VendorRepository
        VendorRepository vendorRepository = new VendorRepository();
        List<Restaurant> vendors = vendorRepository.findByFilters(location, category, search);
        
        Map<String, Object> response = new HashMap<>();
        response.put("vendors", vendors);
        response.put("count", vendors.size());
        response.put("filters", Map.of(
            "location", location != null ? location : "",
            "category", category != null ? category : "",
            "search", search != null ? search : ""
        ));
        
        sendJsonResponse(exchange, 200, response);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * استخراج شناسه از انتهای مسیر URL
     * 
     * @param path مسیر کامل URL
     * @param prefix پیشوند مسیر
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix) {
        return Long.parseLong(path.substring(prefix.length()));
    }
    
    /**
     * استخراج شناسه از وسط مسیر URL (بین prefix و suffix)
     * 
     * @param path مسیر کامل URL
     * @param prefix پیشوند مسیر
     * @param suffix پسوند مسیر
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        int start = prefix.length();
        int end = path.indexOf(suffix, start);
        return Long.parseLong(path.substring(start, end));
    }
    
    /**
     * استخراج رشته از انتهای مسیر URL با URL decoding
     * 
     * @param path مسیر کامل URL
     * @param prefix پیشوند مسیر
     * @return رشته استخراج شده با URL decode
     */
    private String extractStringFromPath(String path, String prefix) {
        return java.net.URLDecoder.decode(path.substring(prefix.length()), java.nio.charset.StandardCharsets.UTF_8);
    }
    
    /**
     * استخراج پارامتر از query string
     * 
     * @param query رشته query parameters
     * @param param نام پارامتر مورد نظر
     * @return مقدار پارامتر با URL decode
     */
    private String extractQueryParam(String query, String param) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2); // تقسیم به حداکثر 2 قسمت برای مدیریت values با "="
            if (kv.length == 2 && kv[0].equals(param)) {
                return java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return "";
    }
    
    /**
     * تبدیل JSON request body به Map
     * 
     * @param exchange شیء HttpExchange
     * @return Map حاوی داده‌های JSON
     * @throws IOException در صورت خطا در خواندن request body
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        return JsonUtil.fromJson(requestBody, Map.class);
    }
    
    /**
     * دریافت رشته اختیاری از Map با مقدار پیش‌فرض
     * 
     * @param map Map حاوی داده‌ها
     * @param key کلید مورد نظر
     * @param defaultValue مقدار پیش‌فرض
     * @return مقدار رشته یا مقدار پیش‌فرض
     */
    private String getOptionalStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            return defaultValue;
        }
        return value.toString().trim();
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
     * ارسال پاسخ خطا با JSON format
     * 
     * @param exchange شیء HttpExchange
     * @param statusCode کد وضعیت HTTP
     * @param message پیام خطا
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = Map.of(
            "error", message,
            "status", statusCode,
            "timestamp", java.time.Instant.now().toString()
        );
        sendJsonResponse(exchange, statusCode, errorResponse);
    }
}

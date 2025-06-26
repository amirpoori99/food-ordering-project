package com.myapp.admin;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
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
 * REST API Controller لپنل مدیریت (Admin Dashboard)
 * 
 * این کلاس تمام endpoints API مربوط به عملیات مدیریتی سیستم سفارش غذا را پیاده‌سازی می‌کند:
 * 
 * === Dashboard و آمار کلی ===
 * GET    /api/admin/dashboard                         - دریافت آمار dashboard
 * GET    /api/admin/statistics/daily                  - آمار روزانه
 * GET    /api/admin/statistics/users                  - آمار کاربران بر اساس نقش
 * GET    /api/admin/statistics/restaurants            - آمار رستوران‌ها بر اساس وضعیت
 * GET    /api/admin/statistics/orders                 - آمار سفارشات بر اساس وضعیت
 * 
 * === مدیریت کاربران (User Management) ===
 * GET    /api/admin/users                             - دریافت تمام کاربران با فیلتر
 * GET    /api/admin/users/{userId}                    - دریافت کاربر با شناسه
 * PUT    /api/admin/users/{userId}/status             - تغییر وضعیت کاربر
 * 
 * === مدیریت رستوران‌ها (Restaurant Management) ===
 * GET    /api/admin/restaurants                       - دریافت تمام رستوران‌ها با فیلتر
 * GET    /api/admin/restaurants/{restaurantId}        - دریافت رستوران با شناسه
 * PUT    /api/admin/restaurants/{restaurantId}/status - تغییر وضعیت رستوران
 * 
 * === مدیریت سفارشات (Order Management) ===
 * GET    /api/admin/orders                            - دریافت تمام سفارشات با فیلتر
 * GET    /api/admin/orders/{orderId}                  - دریافت سفارش با شناسه
 * PUT    /api/admin/orders/{orderId}/status           - تغییر وضعیت سفارش
 * 
 * === مدیریت تراکنش‌ها (Transaction Management) ===
 * GET    /api/admin/transactions                      - دریافت تمام تراکنش‌ها با فیلتر
 * GET    /api/admin/transactions/{transactionId}      - دریافت تراکنش با شناسه
 * 
 * === مدیریت تحویل (Delivery Management) ===
 * GET    /api/admin/deliveries                        - دریافت تمام تحویل‌ها با فیلتر
 * GET    /api/admin/deliveries/{deliveryId}           - دریافت تحویل با شناسه
 * 
 * === ویژگی‌های کلیدی ===
 * - RESTful API Design: طراحی API مبتنی بر استانداردهای REST
 * - Comprehensive Filtering: فیلترهای جامع برای همه endpoints
 * - Pagination Support: پشتیبانی از صفحه‌بندی
 * - Security Filtering: فیلتر کردن فیلدهای حساس
 * - Error Handling: مدیریت جامع خطاها
 * - CORS Support: پشتیبانی از Cross-Origin Resource Sharing
 * - JSON Response Format: فرمت پاسخ JSON استاندارد
 * - Query Parameter Parsing: پردازش پارامترهای query
 * - Path Parameter Extraction: استخراج پارامترهای path
 * - HTTP Status Codes: کدهای وضعیت HTTP مناسب
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class AdminController implements HttpHandler {
    
    /** سرویس لایه منطق کسب‌وکار مدیریت */
    private final AdminService adminService;
    
    /**
     * سازنده با تزریق AdminService
     * 
     * @param adminService سرویس عملیات مدیریتی
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            // مسیریابی درخواست‌ها بر اساس path و method
            if (path.equals("/api/admin/dashboard") && "GET".equals(method)) {
                getDashboardStatistics(exchange);
            } else if (path.equals("/api/admin/users") && "GET".equals(method)) {
                getAllUsers(exchange);
            } else if (path.matches("/api/admin/users/\\d+") && "GET".equals(method)) {
                getUserById(exchange);
            } else if (path.matches("/api/admin/users/\\d+/status") && "PUT".equals(method)) {
                updateUserStatus(exchange);
            } else if (path.equals("/api/admin/restaurants") && "GET".equals(method)) {
                getAllRestaurants(exchange);
            } else if (path.matches("/api/admin/restaurants/\\d+") && "GET".equals(method)) {
                getRestaurantById(exchange);
            } else if (path.matches("/api/admin/restaurants/\\d+/status") && "PUT".equals(method)) {
                updateRestaurantStatus(exchange);
            } else if (path.equals("/api/admin/orders") && "GET".equals(method)) {
                getAllOrders(exchange);
            } else if (path.matches("/api/admin/orders/\\d+") && "GET".equals(method)) {
                getOrderById(exchange);
            } else if (path.matches("/api/admin/orders/\\d+/status") && "PUT".equals(method)) {
                updateOrderStatus(exchange);
            } else if (path.equals("/api/admin/transactions") && "GET".equals(method)) {
                getAllTransactions(exchange);
            } else if (path.matches("/api/admin/transactions/\\d+") && "GET".equals(method)) {
                getTransactionById(exchange);
            } else if (path.equals("/api/admin/deliveries") && "GET".equals(method)) {
                getAllDeliveries(exchange);
            } else if (path.matches("/api/admin/deliveries/\\d+") && "GET".equals(method)) {
                getDeliveryById(exchange);
            } else if (path.equals("/api/admin/statistics/daily") && "GET".equals(method)) {
                getDailyStatistics(exchange);
            } else if (path.equals("/api/admin/statistics/users") && "GET".equals(method)) {
                getUserStatistics(exchange);
            } else if (path.equals("/api/admin/statistics/restaurants") && "GET".equals(method)) {
                getRestaurantStatistics(exchange);
            } else if (path.equals("/api/admin/statistics/orders") && "GET".equals(method)) {
                getOrderStatistics(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            }
        } catch (NotFoundException e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    // ==================== Dashboard و آمار کلی (DASHBOARD) ====================
    
    /**
     * GET /api/admin/dashboard - دریافت آمار dashboard
     * 
     * این endpoint آمار کلی سیستم را برای نمایش در صفحه اصلی پنل مدیریت برمی‌گرداند
     * شامل: تعداد کاربران، رستوران‌ها، سفارشات، درآمد کل، سفارشات امروز و ...
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getDashboardStatistics(HttpExchange exchange) throws IOException {
        AdminRepository.SystemStatistics stats = adminService.getSystemStatistics();
        
        // ساخت پاسخ JSON
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", stats.getTotalUsers());
        response.put("totalRestaurants", stats.getTotalRestaurants());
        response.put("totalOrders", stats.getTotalOrders());
        response.put("totalDeliveries", stats.getTotalDeliveries());
        response.put("totalRevenue", stats.getTotalRevenue());
        response.put("totalRefunds", stats.getTotalRefunds());
        response.put("todayOrders", stats.getTodayOrders());
        response.put("todayRevenue", stats.getTodayRevenue());
        response.put("activeRestaurants", stats.getActiveRestaurants());
        response.put("pendingOrders", stats.getPendingOrders());
        response.put("activeDeliveries", stats.getActiveDeliveries());
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== مدیریت کاربران (USER MANAGEMENT) ====================
    
    /**
     * GET /api/admin/users - دریافت تمام کاربران با فیلتر
     * 
     * پارامترهای query قابل قبول:
     * - search: عبارت جستجو در نام، ایمیل و تلفن
     * - role: فیلتر بر اساس نقش (CUSTOMER, ADMIN, VENDOR, COURIER)
     * - page: شماره صفحه (پیش‌فرض 0)
     * - size: تعداد رکورد در صفحه (پیش‌فرض 20)
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getAllUsers(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        // استخراج پارامترها
        String searchTerm = params.get("search");
        String role = params.get("role");
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        // دریافت کاربران و تعداد کل
        List<User> users = adminService.getAllUsers(searchTerm, role, page, size);
        Long totalCount = adminService.countUsers(searchTerm, role);
        
        // فیلتر کردن فیلدهای حساس برای امنیت
        List<Map<String, Object>> safeUsers = users.stream().map(user -> {
            Map<String, Object> safeUser = new HashMap<>();
            safeUser.put("id", user.getId());
            safeUser.put("fullName", user.getFullName());
            safeUser.put("phone", user.getPhone());
            safeUser.put("email", user.getEmail());
            safeUser.put("role", user.getRole());
            safeUser.put("address", user.getAddress());
            safeUser.put("isActive", user.getIsActive());
            // عمداً رمز عبور هش شده را برای امنیت حذف می‌کنیم
            return safeUser;
        }).toList();
        
        // ساخت پاسخ با pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("users", safeUsers);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/users/{userId} - دریافت کاربر با شناسه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getUserById(HttpExchange exchange) throws IOException {
        Long userId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/users/");
        User user = adminService.getUserById(userId);
        
        // فیلتر کردن فیلدهای حساس برای امنیت
        Map<String, Object> safeUser = new HashMap<>();
        safeUser.put("id", user.getId());
        safeUser.put("fullName", user.getFullName());
        safeUser.put("phone", user.getPhone());
        safeUser.put("email", user.getEmail());
        safeUser.put("role", user.getRole());
        safeUser.put("address", user.getAddress());
        safeUser.put("isActive", user.getIsActive());
        // عمداً رمز عبور هش شده را برای امنیت حذف می‌کنیم
        
        String responseBody = JsonUtil.toJson(safeUser);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * PUT /api/admin/users/{userId}/status - تغییر وضعیت کاربر
     * 
     * Body JSON مورد نیاز:
     * {
     *   "isActive": true/false,
     *   "adminId": شناسه ادمین درخواست‌کننده
     * }
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void updateUserStatus(HttpExchange exchange) throws IOException {
        Long userId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/users/", "/status");
        
        // خواندن و پردازش JSON body
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        @SuppressWarnings("unchecked")
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        Boolean isActive = (Boolean) request.get("isActive");
        Long adminId = extractLong(request, "adminId");
        
        if (isActive == null) {
            throw new IllegalArgumentException("isActive field is required");
        }
        
        adminService.updateUserStatus(userId, isActive, adminId);
        
        sendResponse(exchange, 200, "{\"message\":\"User status updated successfully\"}");
    }

    // ==================== مدیریت رستوران‌ها (RESTAURANT MANAGEMENT) ====================
    
    /**
     * GET /api/admin/restaurants - دریافت تمام رستوران‌ها با فیلتر
     * 
     * پارامترهای query قابل قبول:
     * - search: عبارت جستجو در نام و آدرس رستوران
     * - status: فیلتر بر اساس وضعیت (ACTIVE, INACTIVE, PENDING_APPROVAL)
     * - page: شماره صفحه
     * - size: تعداد رکورد در صفحه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getAllRestaurants(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Restaurant> restaurants = adminService.getAllRestaurants(searchTerm, status, page, size);
        Long totalCount = adminService.countRestaurants(searchTerm, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("restaurants", restaurants);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/restaurants/{restaurantId} - دریافت رستوران با شناسه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getRestaurantById(HttpExchange exchange) throws IOException {
        Long restaurantId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/restaurants/");
        Restaurant restaurant = adminService.getRestaurantById(restaurantId);
        
        String responseBody = JsonUtil.toJson(restaurant);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * PUT /api/admin/restaurants/{restaurantId}/status - تغییر وضعیت رستوران
     * 
     * Body JSON مورد نیاز:
     * {
     *   "status": "ACTIVE" | "INACTIVE" | "PENDING_APPROVAL",
     *   "adminId": شناسه ادمین درخواست‌کننده
     * }
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void updateRestaurantStatus(HttpExchange exchange) throws IOException {
        Long restaurantId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/restaurants/", "/status");
        
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        @SuppressWarnings("unchecked")
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        String status = (String) request.get("status");
        Long adminId = extractLong(request, "adminId");
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("status field is required");
        }
        
        // تبدیل string به enum
        RestaurantStatus statusEnum;
        try {
            statusEnum = RestaurantStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid restaurant status: " + status);
        }
        
        adminService.updateRestaurantStatus(restaurantId, statusEnum, adminId);
        
        sendResponse(exchange, 200, "{\"message\":\"Restaurant status updated successfully\"}");
    }

    // ==================== مدیریت سفارشات (ORDER MANAGEMENT) ====================
    
    /**
     * GET /api/admin/orders - دریافت تمام سفارشات با فیلتر
     * 
     * پارامترهای query قابل قبول:
     * - search: عبارت جستجو در آدرس تحویل و شماره تلفن
     * - status: فیلتر بر اساس وضعیت سفارش
     * - customerId: شناسه مشتری
     * - restaurantId: شناسه رستوران
     * - page: شماره صفحه
     * - size: تعداد رکورد در صفحه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getAllOrders(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        Long customerId = parseLongParam(params.get("customerId"));
        Long restaurantId = parseLongParam(params.get("restaurantId"));
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Order> orders = adminService.getAllOrders(searchTerm, status, customerId, restaurantId, page, size);
        Long totalCount = adminService.countOrders(searchTerm, status, customerId, restaurantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/orders/{orderId} - دریافت سفارش با شناسه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getOrderById(HttpExchange exchange) throws IOException {
        Long orderId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/orders/");
        Order order = adminService.getOrderById(orderId);
        
        String responseBody = JsonUtil.toJson(order);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * PUT /api/admin/orders/{orderId}/status - تغییر وضعیت سفارش
     * 
     * این endpoint به ادمین اجازه تغییر دستی وضعیت سفارش را می‌دهد
     * 
     * Body JSON مورد نیاز:
     * {
     *   "status": "PENDING" | "CONFIRMED" | "PREPARING" | "READY" | "DELIVERED" | "CANCELLED",
     *   "adminId": شناسه ادمین درخواست‌کننده
     * }
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void updateOrderStatus(HttpExchange exchange) throws IOException {
        Long orderId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/orders/", "/status");
        
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        @SuppressWarnings("unchecked")
        Map<String, Object> request = JsonUtil.fromJson(requestBody, Map.class);
        
        String status = (String) request.get("status");
        Long adminId = extractLong(request, "adminId");
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("status field is required");
        }
        
        OrderStatus statusEnum;
        try {
            statusEnum = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
        
        adminService.updateOrderStatus(orderId, statusEnum, adminId);
        
        sendResponse(exchange, 200, "{\"message\":\"Order status updated successfully\"}");
    }

    // ==================== مدیریت تراکنش‌ها (TRANSACTION MANAGEMENT) ====================
    
    /**
     * GET /api/admin/transactions - دریافت تمام تراکنش‌ها با فیلتر
     * 
     * پارامترهای query قابل قبول:
     * - search: عبارت جستجو در شناسه مرجع، توضیحات و روش پرداخت
     * - status: فیلتر بر اساس وضعیت تراکنش
     * - type: فیلتر بر اساس نوع تراکنش (PAYMENT, REFUND, WALLET_CHARGE)
     * - userId: شناسه کاربر
     * - page: شماره صفحه
     * - size: تعداد رکورد در صفحه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getAllTransactions(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        String type = params.get("type");
        Long userId = parseLongParam(params.get("userId"));
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Transaction> transactions = adminService.getAllTransactions(searchTerm, status, type, userId, page, size);
        Long totalCount = adminService.countTransactions(searchTerm, status, type, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/transactions/{transactionId} - دریافت تراکنش با شناسه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getTransactionById(HttpExchange exchange) throws IOException {
        Long transactionId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/transactions/");
        Transaction transaction = adminService.getTransactionById(transactionId);
        
        String responseBody = JsonUtil.toJson(transaction);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== مدیریت تحویل (DELIVERY MANAGEMENT) ====================
    
    /**
     * GET /api/admin/deliveries - دریافت تمام تحویل‌ها با فیلتر
     * 
     * پارامترهای query قابل قبول:
     * - search: عبارت جستجو در یادداشت‌های تحویل
     * - status: فیلتر بر اساس وضعیت تحویل
     * - courierId: شناسه پیک
     * - page: شماره صفحه
     * - size: تعداد رکورد در صفحه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getAllDeliveries(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        String searchTerm = params.get("search");
        String status = params.get("status");
        Long courierId = parseLongParam(params.get("courierId"));
        int page = parseIntParam(params.get("page"), 0);
        int size = parseIntParam(params.get("size"), 20);
        
        List<Delivery> deliveries = adminService.getAllDeliveries(searchTerm, status, courierId, page, size);
        Long totalCount = adminService.countDeliveries(searchTerm, status, courierId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deliveries", deliveries);
        response.put("totalCount", totalCount);
        response.put("page", page);
        response.put("size", size);
        response.put("totalPages", (int) Math.ceil((double) totalCount / size));
        
        String responseBody = JsonUtil.toJson(response);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/deliveries/{deliveryId} - دریافت تحویل با شناسه
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getDeliveryById(HttpExchange exchange) throws IOException {
        Long deliveryId = extractIdFromPath(exchange.getRequestURI().getPath(), "/api/admin/deliveries/");
        Delivery delivery = adminService.getDeliveryById(deliveryId);
        
        String responseBody = JsonUtil.toJson(delivery);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== آمار و گزارشات (STATISTICS) ====================
    
    /**
     * GET /api/admin/statistics/daily - دریافت آمار روزانه
     * 
     * پارامترهای query قابل قبول:
     * - days: تعداد روزهای گذشته (پیش‌فرض 7)
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getDailyStatistics(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, String> params = parseQueryParameters(uri.getQuery());
        
        int days = parseIntParam(params.get("days"), 7);
        
        List<AdminRepository.DailyStatistics> stats = adminService.getDailyStatistics(days);
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/statistics/users - دریافت آمار کاربران بر اساس نقش
     * 
     * برای نمایش نمودار توزیع نقش‌های کاربران
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getUserStatistics(HttpExchange exchange) throws IOException {
        Map<User.Role, Long> stats = adminService.getUserStatsByRole();
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/statistics/restaurants - دریافت آمار رستوران‌ها بر اساس وضعیت
     * 
     * برای نمایش نمودار توزیع وضعیت رستوران‌ها
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getRestaurantStatistics(HttpExchange exchange) throws IOException {
        Map<RestaurantStatus, Long> stats = adminService.getRestaurantStatsByStatus();
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }
    
    /**
     * GET /api/admin/statistics/orders - دریافت آمار سفارشات بر اساس وضعیت
     * 
     * برای نمایش نمودار توزیع وضعیت سفارشات
     * 
     * @param exchange شیء HttpExchange
     * @throws IOException در صورت خطا در I/O
     */
    private void getOrderStatistics(HttpExchange exchange) throws IOException {
        Map<OrderStatus, Long> stats = adminService.getOrderStatsByStatus();
        
        String responseBody = JsonUtil.toJson(stats);
        sendResponse(exchange, 200, responseBody);
    }

    // ==================== متدهای کمکی (HELPER METHODS) ====================
    
    /**
     * پردازش پارامترهای query از رشته query
     * 
     * @param query رشته query از URL
     * @return Map حاوی پارامترها
     */
    private Map<String, String> parseQueryParameters(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], "");
                }
            }
        }
        return params;
    }
    
    /**
     * استخراج شناسه از path
     * 
     * @param path مسیر درخواست
     * @param prefix پیشوند path
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix) {
        String idStr = path.substring(prefix.length());
        return Long.parseLong(idStr);
    }
    
    /**
     * استخراج شناسه از path با پسوند
     * 
     * @param path مسیر درخواست
     * @param prefix پیشوند path
     * @param suffix پسوند path
     * @return شناسه استخراج شده
     */
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String pathWithoutPrefix = path.substring(prefix.length());
        String idStr = pathWithoutPrefix.substring(0, pathWithoutPrefix.length() - suffix.length());
        return Long.parseLong(idStr);
    }
    
    /**
     * پردازش پارامتر integer با مقدار پیش‌فرض
     * 
     * @param param رشته پارامتر
     * @param defaultValue مقدار پیش‌فرض
     * @return مقدار integer پردازش شده
     */
    private int parseIntParam(String param, int defaultValue) {
        if (param == null || param.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * پردازش پارامتر Long اختیاری
     * 
     * @param param رشته پارامتر
     * @return مقدار Long یا null
     */
    private Long parseLongParam(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(param);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * استخراج مقدار Long از Map درخواست
     * 
     * @param request Map حاوی داده‌های درخواست
     * @param key کلید مورد نظر
     * @return مقدار Long استخراج شده
     * @throws IllegalArgumentException در صورت عدم وجود یا نامعتبر بودن مقدار
     */
    private Long extractLong(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid " + key + " format");
            }
        }
        throw new IllegalArgumentException("Invalid " + key + " type");
    }
    
    /**
     * ارسال پاسخ HTTP
     * 
     * @param exchange شیء HttpExchange
     * @param statusCode کد وضعیت HTTP
     * @param response محتوای پاسخ JSON
     * @throws IOException در صورت خطا در I/O
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // تنظیم headers
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        // ارسال پاسخ
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
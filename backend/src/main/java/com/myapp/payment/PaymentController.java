package com.myapp.payment;

import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionStatus;
import com.myapp.common.models.TransactionType;
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
 * کنترلر REST API برای عملیات مالی و پرداخت
 * 
 * این کلاس مسئول مدیریت تمام درخواست‌های HTTP مربوط به:
 * - پردازش پرداخت سفارشات
 * - پردازش استرداد وجه
 * - مشاهده تاریخچه تراکنش‌ها
 * - مدیریت وضعیت تراکنش‌ها
 * - آمار و گزارش‌گیری مالی
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class PaymentController implements HttpHandler {
    
    /** سرویس پرداخت برای پردازش منطق کسب‌وکار */
    private final PaymentService paymentService;
    
    /**
     * سازنده پیش‌فرض - ایجاد instance جدید از PaymentService
     */
    public PaymentController() {
        this.paymentService = new PaymentService();
    }
    
    /**
     * سازنده برای تزریق وابستگی (Dependency Injection)
     * برای تست‌ها و configuration سفارشی استفاده می‌شود
     * 
     * @param paymentService سرویس پرداخت از بیرون تزریق شده
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
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
        // دریافت method (GET, POST, PUT) و path درخواست
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            // مسیریابی درخواست بر اساس HTTP method
            switch (method) {
                case "POST":
                    handlePostRequest(exchange, path);
                    break;
                case "GET":
                    handleGetRequest(exchange, path);
                    break;
                case "PUT":
                    handlePutRequest(exchange, path);
                    break;
                default:
                    // HTTP method پشتیبانی نشده
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    break;
            }
        } catch (Exception e) {
            // مدیریت خطاهای غیرمنتظره
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== POST ENDPOINTS ====================
    
    /**
     * پردازش درخواست‌های POST
     * شامل عملیات ایجاد و پردازش جدید
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException خطا در I/O
     */
    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/payments/process")) {
            // پردازش پرداخت جدید
            processPayment(exchange);
        } else if (path.startsWith("/api/payments/") && path.endsWith("/refund")) {
            // پردازش استرداد وجه
            processRefund(exchange, path);
        } else {
            // endpoint یافت نشد
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * POST /api/payments/process - پردازش پرداخت برای سفارش
     * 
     * این endpoint برای پرداخت سفارشات استفاده می‌شود
     * ورودی: userId, orderId, paymentMethod
     * خروجی: اطلاعات تراکنش ایجاد شده
     */
    private void processPayment(HttpExchange exchange) throws IOException {
        try {
            // خواندن body درخواست
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            // استخراج پارامترهای ضروری
            Long userId = extractLong(request, "userId");
            Long orderId = extractLong(request, "orderId");
            String paymentMethod = extractString(request, "paymentMethod");
            
            // پردازش پرداخت از طریق service layer
            Transaction transaction = paymentService.processPayment(userId, orderId, paymentMethod);
            
            // سریالایز کردن تراکنش و ارسال پاسخ
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            // خطای validation ورودی‌ها
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // خطای داخلی سرور
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * POST /api/payments/{paymentId}/refund - پردازش استرداد وجه
     * 
     * این endpoint برای استرداد وجه پرداخت‌های قبلی استفاده می‌شود
     * ورودی: paymentId در URL و reason در body
     * خروجی: اطلاعات تراکنش استرداد
     */
    private void processRefund(HttpExchange exchange, String path) throws IOException {
        try {
            // استخراج paymentId از URL
            Long paymentId = extractPathParameter(path, "/api/payments/", "/refund");
            
            // خواندن body درخواست
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            // استخراج دلیل استرداد
            String reason = extractString(request, "reason");
            
            // پردازش استرداد از طریق service layer
            Transaction refund = paymentService.processRefund(paymentId, reason);
            
            // سریالایز کردن تراکنش استرداد و ارسال پاسخ
            String response = serializeTransaction(refund);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            // خطای validation ورودی‌ها
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // خطای داخلی سرور
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    /**
     * پردازش درخواست‌های GET
     * شامل عملیات مشاهده و جستجو
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException خطا در I/O
     */
    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api/payments/transaction/")) {
            // دریافت تراکنش بر اساس ID
            getTransaction(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/history")) {
            // تاریخچه تراکنش‌های کاربر
            getUserTransactionHistory(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/wallet-transactions")) {
            // تراکنش‌های کیف پول کاربر
            getUserWalletTransactions(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/payment-transactions")) {
            // تراکنش‌های پرداخت کاربر
            getUserPaymentTransactions(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/statistics")) {
            // آمار تراکنش‌های کاربر
            getUserTransactionStatistics(exchange, path);
        } else if (path.startsWith("/api/payments/order/") && path.endsWith("/history")) {
            // تاریخچه تراکنش‌های سفارش
            getOrderTransactionHistory(exchange, path);
        } else if (path.startsWith("/api/payments/status/")) {
            // جستجو بر اساس وضعیت تراکنش
            getTransactionsByStatus(exchange, path);
        } else if (path.startsWith("/api/payments/type/")) {
            // جستجو بر اساس نوع تراکنش
            getTransactionsByType(exchange, path);
        } else if (path.equals("/api/payments/date-range")) {
            // جستجو بر اساس بازه تاریخ
            getTransactionsByDateRange(exchange);
        } else {
            // endpoint یافت نشد
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * GET /api/payments/transaction/{transactionId} - دریافت تراکنش بر اساس ID
     * 
     * ورودی: transactionId در URL
     * خروجی: اطلاعات کامل تراکنش
     */
    private void getTransaction(HttpExchange exchange, String path) throws IOException {
        try {
            // استخراج transactionId از URL
            Long transactionId = extractPathParameter(path, "/api/payments/transaction/", "");
            
            // دریافت تراکنش از service layer
            Transaction transaction = paymentService.getTransaction(transactionId);
            
            // سریالایز کردن و ارسال پاسخ
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            // خطای validation
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // تراکنش یافت نشد
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/user/{userId}/history - دریافت تاریخچه تراکنش‌های کاربر
     * 
     * تمام تراکنش‌های یک کاربر را بازمی‌گرداند
     * شامل پرداخت‌ها، استردادها و تراکنش‌های کیف پول
     */
    private void getUserTransactionHistory(HttpExchange exchange, String path) throws IOException {
        try {
            // استخراج userId از URL
            Long userId = extractPathParameter(path, "/api/payments/user/", "/history");
            
            // دریافت تاریخچه تراکنش‌ها
            List<Transaction> transactions = paymentService.getUserTransactionHistory(userId);
            
            // سریالایز کردن لیست و ارسال پاسخ
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/user/{userId}/wallet-transactions - دریافت تراکنش‌های کیف پول
     * 
     * فقط تراکنش‌های مربوط به کیف پول (شارژ و برداشت) را بازمی‌گرداند
     */
    private void getUserWalletTransactions(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/payments/user/", "/wallet-transactions");
            
            List<Transaction> transactions = paymentService.getUserWalletTransactions(userId);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/user/{userId}/payment-transactions - دریافت تراکنش‌های پرداخت
     * 
     * فقط تراکنش‌های مربوط به پرداخت و استرداد سفارشات را بازمی‌گرداند
     */
    private void getUserPaymentTransactions(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/payments/user/", "/payment-transactions");
            
            List<Transaction> transactions = paymentService.getUserPaymentTransactions(userId);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/user/{userId}/statistics - دریافت آمار تراکنش‌های کاربر
     * 
     * آمار کاملی از فعالیت‌های مالی کاربر شامل:
     * - تعداد کل تراکنش‌ها
     * - تراکنش‌های موفق/ناموفق/در انتظار
     * - مجموع مبلغ خرج شده و استرداد شده
     * - درصد موفقیت
     */
    private void getUserTransactionStatistics(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/payments/user/", "/statistics");
            
            PaymentRepository.TransactionStatistics stats = paymentService.getUserTransactionStatistics(userId);
            
            String response = serializeTransactionStatistics(stats);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/order/{orderId}/history - دریافت تاریخچه تراکنش‌های سفارش
     * 
     * تمام تراکنش‌های مربوط به یک سفارش خاص شامل پرداخت و استرداد
     */
    private void getOrderTransactionHistory(HttpExchange exchange, String path) throws IOException {
        try {
            Long orderId = extractPathParameter(path, "/api/payments/order/", "/history");
            
            List<Transaction> transactions = paymentService.getOrderTransactionHistory(orderId);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/status/{status} - دریافت تراکنش‌ها بر اساس وضعیت
     * 
     * فیلتر کردن تراکنش‌ها بر اساس وضعیت: PENDING, COMPLETED, FAILED, CANCELLED
     */
    private void getTransactionsByStatus(HttpExchange exchange, String path) throws IOException {
        try {
            String statusStr = extractPathParameter(path, "/api/payments/status/", "").toString();
            TransactionStatus status = TransactionStatus.valueOf(statusStr.toUpperCase());
            
            List<Transaction> transactions = paymentService.getTransactionsByStatus(status);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid status: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/type/{type} - دریافت تراکنش‌ها بر اساس نوع
     * 
     * فیلتر کردن تراکنش‌ها بر اساس نوع: PAYMENT, REFUND, WALLET_CHARGE, WALLET_WITHDRAWAL
     */
    private void getTransactionsByType(HttpExchange exchange, String path) throws IOException {
        try {
            String typeStr = extractPathParameter(path, "/api/payments/type/", "").toString();
            TransactionType type = TransactionType.valueOf(typeStr.toUpperCase());
            
            List<Transaction> transactions = paymentService.getTransactionsByType(type);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid type: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/date-range?startDate=...&endDate=... - دریافت تراکنش‌ها در بازه تاریخ
     * 
     * جستجوی تراکنش‌ها در یک بازه زمانی مشخص
     * فرمت تاریخ: ISO 8601 (yyyy-MM-ddTHH:mm:ss)
     */
    private void getTransactionsByDateRange(HttpExchange exchange) throws IOException {
        try {
            // پارس کردن query parameters
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);
            
            String startDateStr = params.get("startDate");
            String endDateStr = params.get("endDate");
            
            // بررسی وجود پارامترهای ضروری
            if (startDateStr == null || endDateStr == null) {
                sendResponse(exchange, 400, "{\"error\":\"Both startDate and endDate parameters are required\"}");
                return;
            }
            
            // تبدیل رشته به LocalDateTime
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            List<Transaction> transactions = paymentService.getTransactionsByDateRange(startDate, endDate);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (DateTimeParseException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    /**
     * پردازش درخواست‌های PUT
     * شامل عملیات به‌روزرسانی
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست
     * @throws IOException خطا در I/O
     */
    private void handlePutRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api/payments/") && path.endsWith("/status")) {
            // به‌روزرسانی وضعیت تراکنش
            updateTransactionStatus(exchange, path);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * PUT /api/payments/{transactionId}/status - به‌روزرسانی وضعیت تراکنش
     * 
     * این endpoint توسط payment gateway ها برای به‌روزرسانی وضعیت تراکنش استفاده می‌شود
     * مخصوص callback های خارجی و تایید نهایی پرداخت‌ها
     */
    private void updateTransactionStatus(HttpExchange exchange, String path) throws IOException {
        try {
            // استخراج transactionId از URL
            Long transactionId = extractPathParameter(path, "/api/payments/", "/status");
            
            // خواندن body درخواست
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            // استخراج پارامترها
            String statusStr = extractString(request, "status");
            String referenceId = extractOptionalString(request, "referenceId");
            String notes = extractOptionalString(request, "notes");
            
            TransactionStatus status = TransactionStatus.valueOf(statusStr.toUpperCase());
            
            // به‌روزرسانی وضعیت تراکنش
            Transaction transaction = paymentService.updateTransactionStatus(transactionId, status, referenceId, notes);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * ارسال پاسخ HTTP به کلاینت
     * 
     * @param exchange شیء HttpExchange
     * @param statusCode کد وضعیت HTTP
     * @param response محتوای پاسخ به فرمت JSON
     * @throws IOException خطا در I/O
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // تنظیم header های پاسخ
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // CORS support
        
        // ارسال header ها و نوشتن محتوای پاسخ
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    /**
     * استخراج پارامتر از مسیر URL
     * 
     * مثال: از "/api/payments/123/refund" با prefix="/api/payments/" و suffix="/refund"
     * عدد 123 را استخراج می‌کند
     * 
     * @param path مسیر کامل
     * @param prefix پیشوند مسیر
     * @param suffix پسوند مسیر
     * @return ID استخراج شده
     */
    private Long extractPathParameter(String path, String prefix, String suffix) {
        String param = path.substring(prefix.length());
        if (!suffix.isEmpty()) {
            param = param.substring(0, param.length() - suffix.length());
        }
        return Long.parseLong(param);
    }
    
    /**
     * پارس کردن JSON ساده برای درخواست‌های API
     * 
     * توجه: این یک JSON parser ساده است و برای production باید از کتابخانه‌های
     * حرفه‌ای مثل Jackson یا Gson استفاده کرد
     * 
     * @param json رشته JSON ورودی
     * @return Map حاوی key-value های پارس شده
     */
    private Map<String, Object> parseJsonRequest(String json) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        // حذف آکولادهای ابتدا و انتها
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            
            // پردازش هر جفت key:value
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    
                    // تلاش برای تبدیل به عدد
                    try {
                        if (value.contains(".")) {
                            result.put(key, Double.parseDouble(value));
                        } else {
                            result.put(key, Long.parseLong(value));
                        }
                    } catch (NumberFormatException e) {
                        // اگر عدد نبود، به عنوان رشته ذخیره کن
                        result.put(key, value);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * پارس کردن query parameters از URL
     * 
     * مثال: "startDate=2024-01-01&endDate=2024-12-31"
     * 
     * @param query رشته query
     * @return Map حاوی پارامترها
     */
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new java.util.HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
    
    /**
     * استخراج مقدار Long از Map درخواست
     * 
     * @param request Map حاوی داده‌های درخواست
     * @param key کلید مورد نظر
     * @return مقدار Long
     * @throws IllegalArgumentException اگر کلید وجود نداشته باشد یا معتبر نباشد
     */
    private Long extractLong(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        throw new IllegalArgumentException("Missing or invalid " + key);
    }
    
    /**
     * استخراج مقدار String از Map درخواست
     * 
     * @param request Map حاوی داده‌های درخواست
     * @param key کلید مورد نظر
     * @return مقدار String
     * @throws IllegalArgumentException اگر کلید وجود نداشته باشد
     */
    private String extractString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Missing or invalid " + key);
    }
    
    /**
     * استخراج مقدار String اختیاری از Map درخواست
     * 
     * @param request Map حاوی داده‌های درخواست
     * @param key کلید مورد نظر
     * @return مقدار String یا null
     */
    private String extractOptionalString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value instanceof String ? (String) value : null;
    }
    
    /**
     * تبدیل شیء Transaction به JSON string
     * 
     * @param transaction شیء تراکنش
     * @return JSON string
     */
    private String serializeTransaction(Transaction transaction) {
        return "{"
            + "\"id\":" + transaction.getId() + ","
            + "\"userId\":" + transaction.getUserId() + ","
            + "\"orderId\":" + (transaction.getOrderId() != null ? transaction.getOrderId() : "null") + ","
            + "\"amount\":" + transaction.getAmount() + ","
            + "\"type\":\"" + transaction.getType() + "\","
            + "\"status\":\"" + transaction.getStatus() + "\","
            + "\"paymentMethod\":\"" + (transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : "") + "\","
            + "\"referenceId\":\"" + (transaction.getReferenceId() != null ? transaction.getReferenceId() : "") + "\","
            + "\"description\":\"" + (transaction.getDescription() != null ? transaction.getDescription() : "") + "\","
            + "\"createdAt\":\"" + transaction.getCreatedAt() + "\","
            + "\"updatedAt\":\"" + (transaction.getUpdatedAt() != null ? transaction.getUpdatedAt() : "") + "\","
            + "\"processedAt\":\"" + (transaction.getProcessedAt() != null ? transaction.getProcessedAt() : "") + "\""
            + "}";
    }
    
    /**
     * تبدیل لیست Transaction به JSON array
     * 
     * @param transactions لیست تراکنش‌ها
     * @return JSON array string
     */
    private String serializeTransactionList(List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < transactions.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(serializeTransaction(transactions.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * تبدیل آمار تراکنش‌ها به JSON string
     * 
     * @param stats شیء آمار تراکنش‌ها
     * @return JSON string حاوی آمار کامل
     */
    private String serializeTransactionStatistics(PaymentRepository.TransactionStatistics stats) {
        return "{"
            + "\"totalTransactions\":" + stats.getTotalTransactions() + ","
            + "\"completedTransactions\":" + stats.getCompletedTransactions() + ","
            + "\"pendingTransactions\":" + stats.getPendingTransactions() + ","
            + "\"failedTransactions\":" + stats.getFailedTransactions() + ","
            + "\"totalSpent\":" + stats.getTotalSpent() + ","
            + "\"totalRefunded\":" + stats.getTotalRefunded() + ","
            + "\"netSpent\":" + stats.getNetSpent() + ","
            + "\"successRate\":" + stats.getSuccessRate()
            + "}";
    }
}

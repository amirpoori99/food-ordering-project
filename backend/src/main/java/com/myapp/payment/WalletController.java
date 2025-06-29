package com.myapp.payment;

import com.myapp.common.models.Transaction;
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
 * کنترلر REST API برای عملیات کیف پول
 * 
 * این کلاس مسئول مدیریت تمام درخواست‌های HTTP مربوط به:
 * - مشاهده موجودی کیف پول
 * - شارژ کیف پول از طریق روش‌های مختلف
 * - برداشت از کیف پول
 * - تاریخچه تراکنش‌های کیف پول
 * - آمار و گزارش‌گیری کیف پول
 * - عملیات مدیریتی توسط ادمین
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class WalletController implements HttpHandler {
    
    private final WalletService walletService;
    
    public WalletController() {
        this.walletService = new WalletService();
    }
    
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            switch (method) {
                case "GET":
                    handleGetRequest(exchange, path);
                    break;
                case "POST":
                    handlePostRequest(exchange, path);
                    break;
                default:
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    break;
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api/wallet/") && path.endsWith("/balance")) {
            getWalletBalance(exchange, path);
        } else if (path.startsWith("/api/wallet/") && path.endsWith("/transactions")) {
            getWalletTransactionHistory(exchange, path);
        } else if (path.startsWith("/api/wallet/") && path.endsWith("/charges")) {
            getWalletChargeHistory(exchange, path);
        } else if (path.startsWith("/api/wallet/") && path.endsWith("/withdrawals")) {
            getWalletWithdrawalHistory(exchange, path);
        } else if (path.startsWith("/api/wallet/") && path.endsWith("/statistics")) {
            getWalletStatistics(exchange, path);
        } else if (path.startsWith("/api/wallet/") && path.contains("/sufficient-balance/")) {
            checkSufficientBalance(exchange, path);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * GET /api/wallet/{userId}/balance - دریافت موجودی کیف پول کاربر
     * 
     * این endpoint موجودی فعلی کیف پول کاربر را برمی‌گرداند
     * 
     * @param exchange شیء HttpExchange
     * @param path مسیر درخواست حاوی userId
     * @throws IOException در صورت خطا در ورودی/خروجی
     */
    private void getWalletBalance(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/wallet/", "/balance");
            
            Double balance = walletService.getWalletBalance(userId);
            
            String response = "{\"userId\":" + userId + ",\"balance\":" + balance + "}";
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/wallet/{userId}/transactions - دریافت تاریخچه تراکنش‌های کیف پول
     * 
     * این endpoint تاریخچه کامل یا در بازه زمانی مشخص تراکنش‌های کیف پول را برمی‌گرداند
     * پشتیبانی از query parameters برای فیلتر زمانی:
     * - startDate: تاریخ شروع (ISO format)
     * - endDate: تاریخ پایان (ISO format)
     */
    private void getWalletTransactionHistory(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/wallet/", "/transactions");
            
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("startDate") && query.contains("endDate")) {
                // پردازش جستجو در بازه زمانی
                Map<String, String> params = parseQueryParams(query);
                String startDateStr = params.get("startDate");
                String endDateStr = params.get("endDate");
                
                LocalDateTime startDate = LocalDateTime.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                
                List<Transaction> transactions = walletService.getWalletTransactionHistory(userId, startDate, endDate);
                String response = serializeTransactionList(transactions);
                sendResponse(exchange, 200, response);
            } else {
                // دریافت تمام تراکنش‌های کیف پول
                List<Transaction> transactions = walletService.getWalletTransactionHistory(userId);
                String response = serializeTransactionList(transactions);
                sendResponse(exchange, 200, response);
            }
            
        } catch (DateTimeParseException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss\"}");
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/wallet/{userId}/charges - دریافت تاریخچه شارژ کیف پول
     * 
     * فقط تراکنش‌های شارژ کیف پول را برمی‌گرداند
     */
    private void getWalletChargeHistory(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/wallet/", "/charges");
            
            List<Transaction> transactions = walletService.getWalletChargeHistory(userId);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/wallet/{userId}/withdrawals - دریافت تاریخچه برداشت از کیف پول
     * 
     * فقط تراکنش‌های برداشت از کیف پول را برمی‌گرداند
     */
    private void getWalletWithdrawalHistory(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/wallet/", "/withdrawals");
            
            List<Transaction> transactions = walletService.getWalletWithdrawalHistory(userId);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/wallet/{userId}/statistics - دریافت آمار کیف پول
     * 
     * آمار کاملی از فعالیت‌های کیف پول شامل موجودی، مجموع شارژ، برداشت و تعداد تراکنش‌ها
     */
    private void getWalletStatistics(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/wallet/", "/statistics");
            
            WalletService.WalletStatistics stats = walletService.getWalletStatistics(userId);
            
            String response = serializeWalletStatistics(stats);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/wallet/{userId}/sufficient-balance/{amount} - بررسی کفایت موجودی کیف پول
     * 
     * بررسی می‌کند که آیا کاربر موجودی کافی برای مبلغ مورد نظر دارد یا خیر
     * مسیر: /api/wallet/123/sufficient-balance/100.50
     */
    private void checkSufficientBalance(HttpExchange exchange, String path) throws IOException {
        try {
            // استخراج userId و amount از مسیر مانند /api/wallet/123/sufficient-balance/100.50
            String[] parts = path.split("/");
            if (parts.length < 6) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid path format\"}");
                return;
            }
            
            Long userId = Long.parseLong(parts[3]);
            Double amount = Double.parseDouble(parts[5]);
            
            boolean hasSufficient = walletService.hasSufficientBalance(userId, amount);
            Double currentBalance = walletService.getWalletBalance(userId);
            
            String response = "{"
                + "\"userId\":" + userId + ","
                + "\"amount\":" + amount + ","
                + "\"currentBalance\":" + currentBalance + ","
                + "\"hasSufficientBalance\":" + hasSufficient
                + "}";
            
            sendResponse(exchange, 200, response);
            
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid userId or amount format\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/wallet/charge")) {
            chargeWallet(exchange);
        } else if (path.equals("/api/wallet/charge/card")) {
            chargeWalletWithCard(exchange);
        } else if (path.equals("/api/wallet/charge/bank-transfer")) {
            chargeWalletWithBankTransfer(exchange);
        } else if (path.equals("/api/wallet/withdraw")) {
            withdrawFromWallet(exchange);
        } else if (path.equals("/api/wallet/admin/credit")) {
            adminCreditWallet(exchange);
        } else if (path.equals("/api/wallet/admin/debit")) {
            adminDebitWallet(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * POST /api/wallet/charge - شارژ عمومی کیف پول
     * 
     * شارژ کیف پول با روش پرداخت مشخص شده در درخواست
     * پارامترهای ضروری: userId, amount, paymentMethod
     * پارامتر اختیاری: description
     */
    private void chargeWallet(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            Long userId = extractLong(request, "userId");
            Double amount = extractDouble(request, "amount");
            String paymentMethod = extractString(request, "paymentMethod");
            String description = extractOptionalString(request, "description");
            
            Transaction transaction = walletService.chargeWallet(userId, amount, paymentMethod, description);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * POST /api/wallet/charge/card - شارژ کیف پول با کارت
     * 
     * شارژ کیف پول از طریق کارت بانکی
     * پارامترهای ضروری: userId, amount
     */
    private void chargeWalletWithCard(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            Long userId = extractLong(request, "userId");
            Double amount = extractDouble(request, "amount");
            
            Transaction transaction = walletService.chargeWalletWithCard(userId, amount);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * POST /api/wallet/charge/bank-transfer - شارژ کیف پول با انتقال بانکی
     * 
     * شارژ کیف پول از طریق انتقال وجه بانکی
     * پارامترهای ضروری: userId, amount, transferReference
     */
    private void chargeWalletWithBankTransfer(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            Long userId = extractLong(request, "userId");
            Double amount = extractDouble(request, "amount");
            String transferReference = extractString(request, "transferReference");
            
            Transaction transaction = walletService.chargeWalletWithBankTransfer(userId, amount, transferReference);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * POST /api/wallet/withdraw - برداشت از کیف پول
     * 
     * برداشت وجه از کیف پول به حساب بانکی
     * پارامترهای ضروری: userId, amount, bankAccount
     * پارامتر اختیاری: reason
     */
    private void withdrawFromWallet(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            Long userId = extractLong(request, "userId");
            Double amount = extractDouble(request, "amount");
            String bankAccount = extractString(request, "bankAccount");
            String reason = extractOptionalString(request, "reason");
            
            Transaction transaction = walletService.withdrawToBank(userId, amount, bankAccount, reason);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * POST /api/wallet/admin/credit - شارژ کیف پول توسط ادمین
     * 
     * شارژ کیف پول کاربر توسط ادمین (عملیات مدیریتی)
     * پارامترهای ضروری: userId, amount, adminId
     * پارامتر اختیاری: reason
     */
    private void adminCreditWallet(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            Long userId = extractLong(request, "userId");
            Double amount = extractDouble(request, "amount");
            String reason = extractOptionalString(request, "reason");
            Long adminId = extractLong(request, "adminId");
            
            Transaction transaction = walletService.adminCreditWallet(userId, amount, reason, adminId);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * POST /api/wallet/admin/debit - برداشت از کیف پول توسط ادمین
     * 
     * برداشت از کیف پول کاربر توسط ادمین (عملیات مدیریتی)
     * پارامترهای ضروری: userId, amount, adminId
     * پارامتر اختیاری: reason
     */
    private void adminDebitWallet(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            Long userId = extractLong(request, "userId");
            Double amount = extractDouble(request, "amount");
            String reason = extractOptionalString(request, "reason");
            Long adminId = extractLong(request, "adminId");
            
            Transaction transaction = walletService.adminDebitWallet(userId, amount, reason, adminId);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private Long extractPathParameter(String path, String prefix, String suffix) {
        String param = path.substring(prefix.length());
        if (!suffix.isEmpty()) {
            param = param.substring(0, param.length() - suffix.length());
        }
        return Long.parseLong(param);
    }
    
    private Map<String, Object> parseJsonRequest(String json) {
        // پارس ساده JSON برای درخواست‌های پایه
        Map<String, Object> result = new java.util.HashMap<>();
        
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    
                    // تلاش برای پارس به عنوان عدد
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
        }
        
        return result;
    }
    
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
    
    private Double extractDouble(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        throw new IllegalArgumentException("Missing or invalid " + key);
    }
    
    private String extractString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Missing or invalid " + key);
    }
    
    private String extractOptionalString(Map<String, Object> request, String key) {
        Object value = request.get(key);
        return value instanceof String ? (String) value : null;
    }
    
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
    
    private String serializeWalletStatistics(WalletService.WalletStatistics stats) {
        return "{"
            + "\"currentBalance\":" + stats.getCurrentBalance() + ","
            + "\"totalCharged\":" + stats.getTotalCharged() + ","
            + "\"totalWithdrawn\":" + stats.getTotalWithdrawn() + ","
            + "\"totalChargeTransactions\":" + stats.getTotalChargeTransactions() + ","
            + "\"totalWithdrawalTransactions\":" + stats.getTotalWithdrawalTransactions() + ","
            + "\"netFlow\":" + stats.getNetFlow() + ","
            + "\"totalTransactions\":" + stats.getTotalTransactions()
            + "}";
    }
    
    // ==================== WRAPPER METHODS FOR TESTING ====================
    
    /**
     * اضافه کردن متدهای wrapper مستقیم برای تست‌ها و فراخوانی‌های internal
     * این متدها بدون HTTP layer کار می‌کنند
     */
    
    /**
     * دریافت موجودی کیف پول کاربر
     */
    public java.math.BigDecimal getBalance(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return walletService.getBalance(userId);
    }
    
    /**
     * شارژ کیف پول کاربر
     */
    public Transaction creditWallet(Long userId, java.math.BigDecimal amount, String description) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return walletService.creditWallet(userId, amount, description);
    }
    
    /**
     * برداشت از کیف پول کاربر
     */
    public Transaction debitWallet(Long userId, java.math.BigDecimal amount, String description) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return walletService.debitWallet(userId, amount, description);
    }
    
    /**
     * دریافت تاریخچه تراکنش‌های کیف پول
     */
    public List<Transaction> getTransactionHistory(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return walletService.getTransactionHistory(userId);
    }
    
    /**
     * انتقال وجه بین دو کیف پول
     */
    public Transaction transfer(Long fromUserId, Long toUserId, java.math.BigDecimal amount, String description) {
        if (fromUserId == null || toUserId == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }
        return walletService.transfer(fromUserId, toUserId, amount, description);
    }
}

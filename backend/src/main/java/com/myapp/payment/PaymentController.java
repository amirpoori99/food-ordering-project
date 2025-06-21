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
 * REST API Controller for payment operations
 * Handles payment processing, transaction management, and payment history
 */
public class PaymentController implements HttpHandler {
    
    private final PaymentService paymentService;
    
    public PaymentController() {
        this.paymentService = new PaymentService();
    }
    
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
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
                    sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    break;
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== POST ENDPOINTS ====================
    
    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/payments/process")) {
            processPayment(exchange);
        } else if (path.startsWith("/api/payments/") && path.endsWith("/refund")) {
            processRefund(exchange, path);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * POST /api/payments/process - Process payment for an order
     */
    private void processPayment(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            Long userId = extractLong(request, "userId");
            Long orderId = extractLong(request, "orderId");
            String paymentMethod = extractString(request, "paymentMethod");
            
            Transaction transaction = paymentService.processPayment(userId, orderId, paymentMethod);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * POST /api/payments/{paymentId}/refund - Process refund for a payment
     */
    private void processRefund(HttpExchange exchange, String path) throws IOException {
        try {
            Long paymentId = extractPathParameter(path, "/api/payments/", "/refund");
            
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            String reason = extractString(request, "reason");
            
            Transaction refund = paymentService.processRefund(paymentId, reason);
            
            String response = serializeTransaction(refund);
            sendResponse(exchange, 201, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api/payments/transaction/")) {
            getTransaction(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/history")) {
            getUserTransactionHistory(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/wallet-transactions")) {
            getUserWalletTransactions(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/payment-transactions")) {
            getUserPaymentTransactions(exchange, path);
        } else if (path.startsWith("/api/payments/user/") && path.endsWith("/statistics")) {
            getUserTransactionStatistics(exchange, path);
        } else if (path.startsWith("/api/payments/order/") && path.endsWith("/history")) {
            getOrderTransactionHistory(exchange, path);
        } else if (path.startsWith("/api/payments/status/")) {
            getTransactionsByStatus(exchange, path);
        } else if (path.startsWith("/api/payments/type/")) {
            getTransactionsByType(exchange, path);
        } else if (path.equals("/api/payments/date-range")) {
            getTransactionsByDateRange(exchange);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * GET /api/payments/transaction/{transactionId} - Get transaction by ID
     */
    private void getTransaction(HttpExchange exchange, String path) throws IOException {
        try {
            Long transactionId = extractPathParameter(path, "/api/payments/transaction/", "");
            
            Transaction transaction = paymentService.getTransaction(transactionId);
            
            String response = serializeTransaction(transaction);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/user/{userId}/history - Get user transaction history
     */
    private void getUserTransactionHistory(HttpExchange exchange, String path) throws IOException {
        try {
            Long userId = extractPathParameter(path, "/api/payments/user/", "/history");
            
            List<Transaction> transactions = paymentService.getUserTransactionHistory(userId);
            
            String response = serializeTransactionList(transactions);
            sendResponse(exchange, 200, response);
            
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 404, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * GET /api/payments/user/{userId}/wallet-transactions - Get user wallet transactions
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
     * GET /api/payments/user/{userId}/payment-transactions - Get user payment transactions
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
     * GET /api/payments/user/{userId}/statistics - Get user transaction statistics
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
     * GET /api/payments/order/{orderId}/history - Get order transaction history
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
     * GET /api/payments/status/{status} - Get transactions by status
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
     * GET /api/payments/type/{type} - Get transactions by type
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
     * GET /api/payments/date-range?startDate=...&endDate=... - Get transactions by date range
     */
    private void getTransactionsByDateRange(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);
            
            String startDateStr = params.get("startDate");
            String endDateStr = params.get("endDate");
            
            if (startDateStr == null || endDateStr == null) {
                sendResponse(exchange, 400, "{\"error\":\"Both startDate and endDate parameters are required\"}");
                return;
            }
            
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
    
    private void handlePutRequest(HttpExchange exchange, String path) throws IOException {
        if (path.startsWith("/api/payments/") && path.endsWith("/status")) {
            updateTransactionStatus(exchange, path);
        } else {
            sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
        }
    }
    
    /**
     * PUT /api/payments/{transactionId}/status - Update transaction status
     */
    private void updateTransactionStatus(HttpExchange exchange, String path) throws IOException {
        try {
            Long transactionId = extractPathParameter(path, "/api/payments/", "/status");
            
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> request = parseJsonRequest(requestBody);
            
            String statusStr = extractString(request, "status");
            String referenceId = extractOptionalString(request, "referenceId");
            String notes = extractOptionalString(request, "notes");
            
            TransactionStatus status = TransactionStatus.valueOf(statusStr.toUpperCase());
            
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
        // Simple JSON parsing for basic requests
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
                    
                    // Try to parse as number
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

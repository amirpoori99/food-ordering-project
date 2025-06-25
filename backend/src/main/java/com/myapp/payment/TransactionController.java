package com.myapp.payment;

import com.myapp.common.models.Transaction;
import com.myapp.common.models.TransactionType;
import com.myapp.common.utils.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transaction Controller - Handles transaction history and management APIs
 * Provides REST endpoints for transaction queries and reporting
 */
public class TransactionController implements HttpHandler {

    private final WalletService walletService;
    private final PaymentRepository paymentRepository;

    public TransactionController(WalletService walletService, PaymentRepository paymentRepository) {
        this.walletService = walletService;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        try {
            switch (method) {
                case "GET":
                    handleGetRequest(exchange, path, uri.getQuery());
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    // ==================== GET ENDPOINTS ====================

    private void handleGetRequest(HttpExchange exchange, String path, String query) throws IOException {
        // Parse query parameters
        Map<String, String> params = parseQueryString(query);

        if (path.equals("/api/transactions/wallet/history")) {
            handleGetWalletHistory(exchange, params);
        } else if (path.equals("/api/transactions/wallet/charges")) {
            handleGetWalletCharges(exchange, params);
        } else if (path.equals("/api/transactions/wallet/withdrawals")) {
            handleGetWalletWithdrawals(exchange, params);
        } else if (path.equals("/api/transactions/wallet/statistics")) {
            handleGetWalletStatistics(exchange, params);
        } else if (path.startsWith("/api/transactions/")) {
            handleGetTransactionById(exchange, path);
        } else {
            sendNotFoundResponse(exchange);
        }
    }

    /**
     * GET /api/transactions/wallet/history?userId={userId}&startDate={date}&endDate={date}
     * Get wallet transaction history for a user
     */
    private void handleGetWalletHistory(HttpExchange exchange, Map<String, String> params) throws IOException {
        try {
            String userIdStr = params.get("userId");
            if (userIdStr == null) {
                sendErrorResponse(exchange, 400, "User ID is required");
                return;
            }

            Long userId = Long.parseLong(userIdStr);
            List<Transaction> transactions;

            String startDateStr = params.get("startDate");
            String endDateStr = params.get("endDate");

            if (startDateStr != null && endDateStr != null) {
                // Get history within date range
                LocalDateTime startDate = LocalDateTime.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime endDate = LocalDateTime.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                transactions = walletService.getWalletTransactionHistory(userId, startDate, endDate);
            } else {
                // Get all history
                transactions = walletService.getWalletTransactionHistory(userId);
            }

            String jsonResponse = JsonUtil.toJson(transactions);
            sendSuccessResponse(exchange, jsonResponse);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, "Invalid date format or other error: " + e.getMessage());
        }
    }

    /**
     * GET /api/transactions/wallet/charges?userId={userId}
     * Get wallet charge history for a user
     */
    private void handleGetWalletCharges(HttpExchange exchange, Map<String, String> params) throws IOException {
        try {
            String userIdStr = params.get("userId");
            if (userIdStr == null) {
                sendErrorResponse(exchange, 400, "User ID is required");
                return;
            }

            Long userId = Long.parseLong(userIdStr);
            List<Transaction> chargeHistory = walletService.getWalletChargeHistory(userId);

            String jsonResponse = JsonUtil.toJson(chargeHistory);
            sendSuccessResponse(exchange, jsonResponse);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error retrieving charge history: " + e.getMessage());
        }
    }

    /**
     * GET /api/transactions/wallet/withdrawals?userId={userId}
     * Get wallet withdrawal history for a user
     */
    private void handleGetWalletWithdrawals(HttpExchange exchange, Map<String, String> params) throws IOException {
        try {
            String userIdStr = params.get("userId");
            if (userIdStr == null) {
                sendErrorResponse(exchange, 400, "User ID is required");
                return;
            }

            Long userId = Long.parseLong(userIdStr);
            List<Transaction> withdrawalHistory = walletService.getWalletWithdrawalHistory(userId);

            String jsonResponse = JsonUtil.toJson(withdrawalHistory);
            sendSuccessResponse(exchange, jsonResponse);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error retrieving withdrawal history: " + e.getMessage());
        }
    }

    /**
     * GET /api/transactions/wallet/statistics?userId={userId}
     * Get wallet transaction statistics for a user
     */
    private void handleGetWalletStatistics(HttpExchange exchange, Map<String, String> params) throws IOException {
        try {
            String userIdStr = params.get("userId");
            if (userIdStr == null) {
                sendErrorResponse(exchange, 400, "User ID is required");
                return;
            }

            Long userId = Long.parseLong(userIdStr);
            var statistics = walletService.getWalletStatistics(userId);

            String jsonResponse = JsonUtil.toJson(statistics);
            sendSuccessResponse(exchange, jsonResponse);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error retrieving wallet statistics: " + e.getMessage());
        }
    }

    /**
     * GET /api/transactions/{transactionId}
     * Get transaction by ID
     */
    private void handleGetTransactionById(HttpExchange exchange, String path) throws IOException {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 4) {
                sendErrorResponse(exchange, 400, "Transaction ID is required");
                return;
            }

            Long transactionId = Long.parseLong(pathParts[3]);
            Optional<Transaction> transactionOpt = paymentRepository.findById(transactionId);

            if (transactionOpt.isEmpty()) {
                sendNotFoundResponse(exchange, "Transaction not found");
                return;
            }

            Transaction transaction = transactionOpt.get();

            String jsonResponse = JsonUtil.toJson(transaction);
            sendSuccessResponse(exchange, jsonResponse);

        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid transaction ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, "Error retrieving transaction: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    private Map<String, String> parseQueryString(String query) {
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

    private void sendSuccessResponse(HttpExchange exchange, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBody.getBytes().length);
        exchange.getResponseBody().write(responseBody.getBytes());
        exchange.getResponseBody().close();
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String errorResponse = "{\"error\":\"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, errorResponse.getBytes().length);
        exchange.getResponseBody().write(errorResponse.getBytes());
        exchange.getResponseBody().close();
    }

    private void sendNotFoundResponse(HttpExchange exchange) throws IOException {
        sendNotFoundResponse(exchange, "Resource not found");
    }

    private void sendNotFoundResponse(HttpExchange exchange, String message) throws IOException {
        sendErrorResponse(exchange, 404, message);
    }

    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendErrorResponse(exchange, 405, "Method not allowed");
    }
}

/*
 * TRANSACTION CONTROLLER API ENDPOINTS:
 * 
 * ✅ GET /api/transactions/wallet/history?userId={id}&startDate={date}&endDate={date}
 *    - Returns wallet transaction history for user
 *    - Supports date range filtering
 * 
 * ✅ GET /api/transactions/wallet/charges?userId={id}
 *    - Returns wallet charge transaction history
 * 
 * ✅ GET /api/transactions/wallet/withdrawals?userId={id}
 *    - Returns wallet withdrawal transaction history
 * 
 * ✅ GET /api/transactions/wallet/statistics?userId={id}
 *    - Returns wallet transaction statistics and summaries
 * 
 * ✅ GET /api/transactions/{transactionId}
 *    - Returns specific transaction details by ID
 * 
 * FUNCTIONALITY COVERED:
 * - Transaction history queries
 * - Date range filtering
 * - Transaction type filtering
 * - Statistical reporting
 * - Individual transaction lookup
 * - Error handling and validation
 */

package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
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
import java.util.Optional;

public class NotificationController implements HttpHandler {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            switch (method) {
                case "GET" -> handleGet(exchange, path, query);
                case "POST" -> handlePost(exchange, path);
                case "PUT" -> handlePut(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path, String query) throws IOException {
        String[] pathParts = path.split("/");
        Map<String, String> queryParams = parseQueryParams(query);

        // GET /notifications/{userId}
        if (pathParts.length == 3 && "notifications".equals(pathParts[1])) {
            handleGetUserNotifications(exchange, pathParts[2], queryParams);
            return;
        }

        // GET /notification/{id}
        if (pathParts.length == 3 && "notification".equals(pathParts[1])) {
            handleGetNotificationById(exchange, pathParts[2]);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        // POST /notifications
        if (pathParts.length == 2 && "notifications".equals(pathParts[1])) {
            handleCreateNotification(exchange);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        // PUT /notification/{id}/read
        if (pathParts.length == 4 && "notification".equals(pathParts[1]) && "read".equals(pathParts[3])) {
            handleMarkAsRead(exchange, pathParts[2]);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        // DELETE /notification/{id}
        if (pathParts.length == 3 && "notification".equals(pathParts[1])) {
            handleDeleteNotification(exchange, pathParts[2]);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    // Handler implementations
    private void handleGetUserNotifications(HttpExchange exchange, String userIdStr, Map<String, String> queryParams) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetNotificationById(HttpExchange exchange, String idStr) throws IOException {
        try {
            Long id = Long.parseLong(idStr);
            Optional<Notification> notification = notificationService.getNotificationById(id);
            if (notification.isPresent()) {
                sendSuccessResponse(exchange, notification.get());
            } else {
                sendErrorResponse(exchange, 404, "Notification not found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid notification ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleCreateNotification(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            
            Long userId = ((Number) data.get("userId")).longValue();
            String title = (String) data.get("title");
            String message = (String) data.get("message");
            String typeStr = (String) data.get("type");
            
            NotificationType type = NotificationType.valueOf(typeStr.toUpperCase());
            Notification notification = notificationService.createNotification(userId, title, message, type);
            
            sendSuccessResponse(exchange, notification, 201);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        }
    }

    private void handleMarkAsRead(HttpExchange exchange, String idStr) throws IOException {
        try {
            Long id = Long.parseLong(idStr);
            Notification notification = notificationService.markAsRead(id);
            sendSuccessResponse(exchange, notification);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid notification ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleDeleteNotification(HttpExchange exchange, String idStr) throws IOException {
        try {
            Long id = Long.parseLong(idStr);
            notificationService.deleteNotification(id);
            sendSuccessResponse(exchange, Map.of("message", "Notification deleted successfully"));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid notification ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    // Utility methods
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new java.util.HashMap<>();
        if (query != null && !query.isEmpty()) {
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

    private void sendSuccessResponse(HttpExchange exchange, Object data) throws IOException {
        sendSuccessResponse(exchange, data, 200);
    }

    private void sendSuccessResponse(HttpExchange exchange, Object data, int statusCode) throws IOException {
        String response = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> errorResponse = Map.of("error", message);
        String response = JsonUtil.toJson(errorResponse);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
} 
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
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Bad request: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path, String query) throws IOException {
        String[] pathParts = path.split("/");
        Map<String, String> queryParams = parseQueryParams(query);

        // Support both /api/notifications and /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // GET /api/notifications or /notifications
        if (pathParts.length >= baseIndex + 1 && "notifications".equals(pathParts[baseIndex])) {
            // Check for specific endpoints first
            if (pathParts.length >= baseIndex + 2) {
                String param = pathParts[baseIndex + 1];
                if ("unread".equals(param)) {
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleGetUnreadNotifications(exchange, userId);
                        return;
                    }
                } else if ("type".equals(param)) {
                    String userId = queryParams.get("userId");
                    String type = queryParams.get("type");
                    if (userId != null && type != null) {
                        handleGetNotificationsByType(exchange, userId, type);
                        return;
                    }
                } else if ("priority".equals(param)) {
                    String userId = queryParams.get("userId");
                    String priority = queryParams.get("priority");
                    if (userId != null && priority != null) {
                        handleGetNotificationsByPriority(exchange, userId, priority);
                        return;
                    }
                } else if ("high-priority".equals(param)) {
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleGetHighPriorityNotifications(exchange, userId);
                        return;
                    }
                } else if ("recent".equals(param)) {
                    String userId = queryParams.get("userId");
                    String days = queryParams.get("days");
                    if (userId != null && days != null) {
                        handleGetRecentNotifications(exchange, userId, Integer.parseInt(days));
                        return;
                    }
                } else if ("count".equals(param)) {
                    // Check for /api/notifications/count/unread
                    if (pathParts.length >= baseIndex + 3 && "unread".equals(pathParts[baseIndex + 2])) {
                        String userId = queryParams.get("userId");
                        if (userId != null) {
                            handleGetUnreadCount(exchange, userId);
                            return;
                        }
                    }
                    // Check for /api/notifications/count/type
                    if (pathParts.length >= baseIndex + 3 && "type".equals(pathParts[baseIndex + 2])) {
                        String userId = queryParams.get("userId");
                        String type = queryParams.get("type");
                        if (userId != null && type != null) {
                            handleGetNotificationCountByType(exchange, userId, type);
                            return;
                        }
                    }
                    // Check for /api/notifications/count/high-priority-unread
                    if (pathParts.length >= baseIndex + 3 && "high-priority-unread".equals(pathParts[baseIndex + 2])) {
                        String userId = queryParams.get("userId");
                        if (userId != null) {
                            handleGetHighPriorityUnreadCount(exchange, userId);
                            return;
                        }
                    }
                } else if ("orders".equals(param)) {
                    String orderId = queryParams.get("orderId");
                    String userId = queryParams.get("userId");
                    if (orderId != null) {
                        handleGetOrderNotifications(exchange, orderId);
                        return;
                    } else if (userId != null) {
                        handleGetUserOrderNotifications(exchange, userId);
                        return;
                    }
                } else if ("restaurants".equals(param)) {
                    String restaurantId = queryParams.get("restaurantId");
                    if (restaurantId != null) {
                        handleGetRestaurantNotifications(exchange, restaurantId);
                        return;
                    }
                } else if ("deliveries".equals(param)) {
                    String deliveryId = queryParams.get("deliveryId");
                    if (deliveryId != null) {
                        handleGetDeliveryNotifications(exchange, deliveryId);
                        return;
                    }
                } else if ("maintenance".equals(param)) {
                    if ("daily".equals(queryParams.get("action"))) {
                        handlePerformDailyMaintenance(exchange);
                        return;
                    } else if ("cleanup".equals(queryParams.get("action"))) {
                        String days = queryParams.get("days");
                        handleCleanupOldNotifications(exchange, days != null ? Integer.parseInt(days) : 30);
                        return;
                    } else if ("purge".equals(queryParams.get("action"))) {
                        String days = queryParams.get("days");
                        handlePurgeOldNotifications(exchange, days != null ? Integer.parseInt(days) : 90);
                        return;
                    }
                } else if ("latest".equals(param)) {
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleGetLatestNotification(exchange, userId);
                        return;
                    }
                } else if ("stats".equals(param)) {
                    String userId = queryParams.get("userId");
                    String type = queryParams.get("type");
                    if (userId != null && type != null) {
                        handleGetNotificationCountByType(exchange, userId, type);
                        return;
                    } else if (userId != null && queryParams.containsKey("highPriority")) {
                        handleGetHighPriorityUnreadCount(exchange, userId);
                        return;
                    } else if (userId != null && queryParams.containsKey("hasUnreadHighPriority")) {
                        handleCheckIfUserHasUnreadHighPriorityNotifications(exchange, userId);
                        return;
                    }
                } else if ("has-high-priority-unread".equals(param)) {
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleCheckIfUserHasUnreadHighPriorityNotifications(exchange, userId);
                        return;
                    }
                }
            } else {
                // GET /api/notifications with query params
                String userId = queryParams.get("userId");
                String page = queryParams.get("page");
                String size = queryParams.get("size");
                
                if (userId != null) {
                    if (page != null && size != null) {
                        handleGetUserNotificationsPaginated(exchange, userId, Integer.parseInt(page), Integer.parseInt(size));
                        return;
                    } else {
                        handleGetUserNotifications(exchange, userId, queryParams);
                        return;
                    }
                } else {
                    // Handle GET /api/notifications without userId - return 400
                    sendErrorResponse(exchange, 400, "userId parameter is required");
                    return;
                }
            }
        }

        // Support path-based specialized endpoints (e.g., /api/notifications/order/123)
        // Check for /api/notifications/order/{orderId}
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "order".equals(pathParts[baseIndex + 1])) {
            handleGetOrderNotifications(exchange, pathParts[baseIndex + 2]);
            return;
        }
        
        // Check for /api/notifications/user/{userId}/order/{orderId}
        if (pathParts.length == baseIndex + 5 && "notifications".equals(pathParts[baseIndex]) && 
            "user".equals(pathParts[baseIndex + 1]) && "order".equals(pathParts[baseIndex + 3])) {
            handleGetUserOrderNotifications(exchange, pathParts[baseIndex + 2], pathParts[baseIndex + 4]);
            return;
        }
        
        // Check for /api/notifications/restaurant/{restaurantId}
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "restaurant".equals(pathParts[baseIndex + 1])) {
            handleGetRestaurantNotifications(exchange, pathParts[baseIndex + 2]);
            return;
        }
        
        // Check for /api/notifications/delivery/{deliveryId}
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "delivery".equals(pathParts[baseIndex + 1])) {
            handleGetDeliveryNotifications(exchange, pathParts[baseIndex + 2]);
            return;
        }

        // GET /api/notification/{id} or /notification/{id} - singular form
        if (pathParts.length == baseIndex + 2 && "notification".equals(pathParts[baseIndex])) {
            handleGetNotificationById(exchange, pathParts[baseIndex + 1]);
            return;
        }
        
        // GET /api/notifications/{id} or /notifications/{id} - when it's just a notification ID
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex])) {
            // Check if the second part is just a number (notification ID) vs special endpoints
            String param = pathParts[baseIndex + 1];
            try {
                Long.parseLong(param);
                // It's a number, treat as notification ID
                handleGetNotificationById(exchange, param);
                return;
            } catch (NumberFormatException e) {
                // Not a number, treat as other endpoint and continue processing
            }
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        // Support both /api/notifications and /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // POST /api/notifications or /notifications
        if (pathParts.length == baseIndex + 1 && "notifications".equals(pathParts[baseIndex])) {
            handleCreateNotification(exchange);
            return;
        }
        
        // POST /api/notifications/order/created
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "order".equals(pathParts[baseIndex + 1]) && "created".equals(pathParts[baseIndex + 2])) {
            handleCreateOrderNotification(exchange);
            return;
        }
        
        // POST /api/notifications/order/status-changed
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "order".equals(pathParts[baseIndex + 1]) && "status-changed".equals(pathParts[baseIndex + 2])) {
            handleCreateOrderStatusChangedNotification(exchange);
            return;
        }
        
        // POST /api/notifications/delivery/assigned
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "delivery".equals(pathParts[baseIndex + 1]) && "assigned".equals(pathParts[baseIndex + 2])) {
            handleCreateDeliveryAssignedNotification(exchange);
            return;
        }
        
        // POST /api/notifications/maintenance/daily
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "maintenance".equals(pathParts[baseIndex + 1]) && "daily".equals(pathParts[baseIndex + 2])) {
            handlePerformDailyMaintenance(exchange);
            return;
        }
        
        // POST /api/notifications/maintenance/cleanup
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "maintenance".equals(pathParts[baseIndex + 1]) && "cleanup".equals(pathParts[baseIndex + 2])) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);
            String days = queryParams.get("days");
            handleCleanupOldNotifications(exchange, days != null ? Integer.parseInt(days) : 30);
            return;
        }
        
        // POST /api/notifications/maintenance/purge
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "maintenance".equals(pathParts[baseIndex + 1]) && "purge".equals(pathParts[baseIndex + 2])) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);
            String days = queryParams.get("days");
            handlePurgeOldNotifications(exchange, days != null ? Integer.parseInt(days) : 90);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);

        // Support both /api/notifications and /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // PUT /api/notifications/{id}/read or /notifications/{id}/read
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "read".equals(pathParts[baseIndex + 2])) {
            handleMarkAsRead(exchange, pathParts[baseIndex + 1]);
            return;
        }
        
        // PUT /api/notifications/{id}/unread or /notifications/{id}/unread
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "unread".equals(pathParts[baseIndex + 2])) {
            handleMarkAsUnread(exchange, pathParts[baseIndex + 1]);
            return;
        }
        
        // PUT /api/notifications/all/read - mark all as read
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "all".equals(pathParts[baseIndex + 1]) && "read".equals(pathParts[baseIndex + 2])) {
            String userId = queryParams.get("userId");
            if (userId != null) {
                handleMarkAllAsRead(exchange, userId);
                return;
            }
        }
        
        // PUT /api/notifications/type/read - mark by type as read
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "type".equals(pathParts[baseIndex + 1]) && "read".equals(pathParts[baseIndex + 2])) {
            String userId = queryParams.get("userId");
            String type = queryParams.get("type");
            if (userId != null && type != null) {
                handleMarkNotificationsByTypeAsRead(exchange, userId, type);
                return;
            }
        }
        
        // PUT /api/notifications/mark-all-read - alternative format
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex]) && "mark-all-read".equals(pathParts[baseIndex + 1])) {
            String userId = queryParams.get("userId");
            if (userId != null) {
                handleMarkAllAsRead(exchange, userId);
                return;
            }
        }
        
        // PUT /api/notifications/mark-read-by-type - alternative format
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex]) && "mark-read-by-type".equals(pathParts[baseIndex + 1])) {
            String userId = queryParams.get("userId");
            String type = queryParams.get("type");
            if (userId != null && type != null) {
                handleMarkNotificationsByTypeAsRead(exchange, userId, type);
                return;
            }
        }
        
        // PUT /api/notifications/{id}/restore - restore deleted notification
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "restore".equals(pathParts[baseIndex + 2])) {
            handleRestoreNotification(exchange, pathParts[baseIndex + 1]);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);

        // Support both /api/notifications and /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // DELETE /api/notifications/{id} or /notifications/{id}
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex])) {
            handleDeleteNotification(exchange, pathParts[baseIndex + 1]);
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
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, e.getMessage());
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
            String priorityStr = (String) data.get("priority");
            
            NotificationType type = NotificationType.valueOf(typeStr.toUpperCase());
            
            Notification notification;
            if (priorityStr != null) {
                NotificationPriority priority = NotificationPriority.valueOf(priorityStr.toUpperCase());
                notification = notificationService.createNotification(userId, title, message, type, priority);
            } else {
                notification = notificationService.createNotification(userId, title, message, type);
            }
            
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
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                sendErrorResponse(exchange, 404, e.getMessage());
            } else {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
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
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                sendErrorResponse(exchange, 404, e.getMessage());
            } else {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    private void handleRestoreNotification(HttpExchange exchange, String idStr) throws IOException {
        try {
            Long id = Long.parseLong(idStr);
            notificationService.restoreNotification(id);
            sendSuccessResponse(exchange, Map.of("message", "Notification restored successfully"));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid notification ID format");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                sendErrorResponse(exchange, 404, e.getMessage());
            } else {
                sendErrorResponse(exchange, 500, e.getMessage());
            }
        }
    }

    private void handleGetUnreadNotifications(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetNotificationsByType(HttpExchange exchange, String userIdStr, String typeStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            NotificationType type = NotificationType.valueOf(typeStr.toUpperCase());
            List<Notification> notifications = notificationService.getNotificationsByType(userId, type);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid notification type");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetNotificationsByPriority(HttpExchange exchange, String userIdStr, String priorityStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            NotificationPriority priority = NotificationPriority.valueOf(priorityStr.toUpperCase());
            List<Notification> notifications = notificationService.getNotificationsByPriority(userId, priority);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid notification priority");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetHighPriorityNotifications(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            List<Notification> notifications = notificationService.getHighPriorityNotifications(userId);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetRecentNotifications(HttpExchange exchange, String userIdStr, int hours) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            List<Notification> notifications = notificationService.getRecentNotifications(userId, hours);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetUnreadCount(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            Long count = notificationService.getUnreadCount(userId);
            sendSuccessResponse(exchange, Map.of("count", count));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetUserNotificationsPaginated(HttpExchange exchange, String userIdStr, int page, int size) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            List<Notification> notifications = notificationService.getUserNotificationsPaginated(userId, page, size);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetOrderNotifications(HttpExchange exchange, String orderIdStr) throws IOException {
        try {
            Long orderId = Long.parseLong(orderIdStr);
            List<Notification> notifications = notificationService.getOrderNotifications(orderId);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid order ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetUserOrderNotifications(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            // For simplicity, we'll get all order notifications for the user
            // This would need to be implemented properly with order filtering
            List<Notification> notifications = notificationService.getNotificationsByType(userId, NotificationType.ORDER_CREATED);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetUserOrderNotifications(HttpExchange exchange, String userIdStr, String orderIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            Long orderId = Long.parseLong(orderIdStr);
            List<Notification> notifications = notificationService.getUserOrderNotifications(userId, orderId);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetRestaurantNotifications(HttpExchange exchange, String restaurantIdStr) throws IOException {
        try {
            Long restaurantId = Long.parseLong(restaurantIdStr);
            List<Notification> notifications = notificationService.getRestaurantNotifications(restaurantId);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid restaurant ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetDeliveryNotifications(HttpExchange exchange, String deliveryIdStr) throws IOException {
        try {
            Long deliveryId = Long.parseLong(deliveryIdStr);
            List<Notification> notifications = notificationService.getDeliveryNotifications(deliveryId);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid delivery ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetLatestNotification(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            Optional<Notification> notification = notificationService.getLatestNotification(userId);
            if (notification.isPresent()) {
                sendSuccessResponse(exchange, notification.get());
            } else {
                sendErrorResponse(exchange, 404, "No notifications found");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetNotificationCountByType(HttpExchange exchange, String userIdStr, String typeStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            NotificationType type = NotificationType.valueOf(typeStr.toUpperCase());
            Long count = notificationService.getNotificationCountByType(userId, type);
            sendSuccessResponse(exchange, Map.of("count", count));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid notification type");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleGetHighPriorityUnreadCount(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            Long count = notificationService.getHighPriorityUnreadCount(userId);
            sendSuccessResponse(exchange, Map.of("count", count));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleCheckIfUserHasUnreadHighPriorityNotifications(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            boolean hasUnread = notificationService.hasUnreadHighPriorityNotifications(userId);
            sendSuccessResponse(exchange, Map.of("hasHighPriorityUnread", hasUnread));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleMarkAsUnread(HttpExchange exchange, String idStr) throws IOException {
        try {
            Long id = Long.parseLong(idStr);
            Notification notification = notificationService.markAsUnread(id);
            sendSuccessResponse(exchange, notification);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid notification ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleMarkAllAsRead(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            int count = notificationService.markAllAsRead(userId);
            sendSuccessResponse(exchange, Map.of("updated", count));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleMarkNotificationsByTypeAsRead(HttpExchange exchange, String userIdStr, String typeStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            NotificationType type = NotificationType.valueOf(typeStr.toUpperCase());
            int count = notificationService.markAllAsReadByType(userId, type);
            sendSuccessResponse(exchange, Map.of("updated", count));
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid notification type");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handlePerformDailyMaintenance(HttpExchange exchange) throws IOException {
        try {
            notificationService.performDailyMaintenance();
            sendSuccessResponse(exchange, Map.of("message", "Daily maintenance completed successfully"));
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleCleanupOldNotifications(HttpExchange exchange, int days) throws IOException {
        try {
            int count = notificationService.cleanupOldNotifications(days);
            sendSuccessResponse(exchange, Map.of("cleaned", count));
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handlePurgeOldNotifications(HttpExchange exchange, int days) throws IOException {
        try {
            int count = notificationService.purgeOldNotifications(days);
            sendSuccessResponse(exchange, Map.of("purged", count));
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    private void handleCreateOrderNotification(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            
            Long userId = ((Number) data.get("userId")).longValue();
            Long orderId = ((Number) data.get("orderId")).longValue();
            String restaurantName = (String) data.get("restaurantName");
            
            Notification notification = notificationService.notifyOrderCreated(userId, orderId, restaurantName);
            sendSuccessResponse(exchange, notification, 201);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        }
    }

    private void handleCreateOrderStatusChangedNotification(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            
            Long userId = ((Number) data.get("userId")).longValue();
            Long orderId = ((Number) data.get("orderId")).longValue();
            String statusStr = (String) data.get("newStatus");
            
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            Notification notification = notificationService.notifyOrderStatusChanged(userId, orderId, status);
            sendSuccessResponse(exchange, notification, 201);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        }
    }

    private void handleCreateDeliveryAssignedNotification(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            
            Long userId = ((Number) data.get("userId")).longValue();
            Long orderId = ((Number) data.get("orderId")).longValue();
            Long deliveryId = ((Number) data.get("deliveryId")).longValue();
            String courierName = (String) data.get("courierName");
            
            Notification notification = notificationService.notifyDeliveryAssigned(userId, orderId, deliveryId, courierName);
            sendSuccessResponse(exchange, notification, 201);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, e.getMessage());
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
        exchange.sendResponseHeaders(statusCode, -1);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> errorResponse = Map.of("error", message);
        String response = JsonUtil.toJson(errorResponse);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, -1);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
} 
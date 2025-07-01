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

/**
 * کنترلر REST API سیستم اعلان‌ها
 * 
 * این کلاس تمام endpoint های HTTP مربوط به مدیریت اعلان‌ها را ارائه می‌دهد:
 * 
 * === عملیات CRUD پایه ===
 * GET    /api/notifications                              - دریافت اعلان‌های کاربر
 * GET    /api/notification/{id}                          - دریافت اعلان خاص
 * POST   /api/notifications                              - ایجاد اعلان جدید
 * PUT    /api/notifications/{id}/read                    - علامت‌گذاری خوانده شده
 * PUT    /api/notifications/{id}/unread                  - علامت‌گذاری خوانده نشده
 * DELETE /api/notifications/{id}                         - حذف اعلان
 * PUT    /api/notifications/{id}/restore                 - بازیابی اعلان
 * 
 * === دریافت با فیلتر ===
 * GET    /api/notifications/unread?userId={id}           - اعلان‌های خوانده نشده
 * GET    /api/notifications/type?userId={id}&type={type} - فیلتر بر اساس نوع
 * GET    /api/notifications/priority?userId={id}&priority={p} - فیلتر بر اساس اولویت
 * GET    /api/notifications/high-priority?userId={id}    - اعلان‌های فوری
 * GET    /api/notifications/recent?userId={id}&days={d}  - اعلان‌های اخیر
 * 
 * === صفحه‌بندی ===
 * GET    /api/notifications?userId={id}&page={p}&size={s} - اعلان‌ها با pagination
 * 
 * === آمار و شمارش ===
 * GET    /api/notifications/count/unread?userId={id}       - تعداد خوانده نشده
 * GET    /api/notifications/count/type?userId={id}&type={t} - تعداد بر اساس نوع
 * GET    /api/notifications/count/high-priority-unread?userId={id} - تعداد فوری خوانده نشده
 * GET    /api/notifications/latest?userId={id}             - آخرین اعلان
 * GET    /api/notifications/has-high-priority-unread?userId={id} - چک وجود فوری
 * 
 * === entity-specific queries ===
 * GET    /api/notifications/orders?orderId={id}           - اعلان‌های سفارش
 * GET    /api/notifications/orders?userId={uid}&orderId={oid} - اعلان‌های سفارش کاربر
 * GET    /api/notifications/restaurants?restaurantId={id} - اعلان‌های رستوران
 * GET    /api/notifications/deliveries?deliveryId={id}    - اعلان‌های تحویل
 * 
 * === عملیات گروهی ===
 * PUT    /api/notifications/all/read?userId={id}          - خواندن همه
 * PUT    /api/notifications/type/read?userId={id}&type={t} - خواندن بر اساس نوع
 * 
 * === Factory Methods ===
 * POST   /api/notifications/order/created                 - اعلان ثبت سفارش
 * POST   /api/notifications/order/status-changed          - اعلان تغییر وضعیت
 * POST   /api/notifications/delivery/assigned             - اعلان اختصاص پیک
 * 
 * === نگهداری سیستم ===
 * GET    /api/notifications/maintenance?action=daily      - نگهداری روزانه
 * GET    /api/notifications/maintenance?action=cleanup&days={d} - پاکسازی قدیمی‌ها
 * GET    /api/notifications/maintenance?action=purge&days={d}   - حذف کامل
 * 
 * === ویژگی‌های کلیدی ===
 * - Flexible URL Pattern: پشتیبانی از /api/notifications و /notifications
 * - RESTful Design: طراحی مطابق استانداردهای REST
 * - Rich Query Support: پشتیبانی گسترده از query parameters
 * - Error Handling: مدیریت جامع خطاها و HTTP status codes
 * - JSON Processing: پردازش کامل JSON requests/responses
 * - Path-based Routing: مسیریابی پیشرفته بر اساس URL patterns
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class NotificationController implements HttpHandler {
    /** سرویس مدیریت منطق کسب‌وکار اعلان‌ها */
    private final NotificationService notificationService;

    /**
     * سازنده کنترلر با dependency injection
     * 
     * @param notificationService سرویس اعلان‌ها
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * هندلر اصلی HTTP requests
     * 
     * تمام درخواست‌های HTTP را بر اساس method مسیریابی می‌کند
     * شامل error handling جامع برای انواع مختلف خطاها
     * 
     * @param exchange شیء HTTP request/response
     * @throws IOException در صورت خطا در پردازش HTTP
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        try {
            // مسیریابی بر اساس HTTP method
            switch (method) {
                case "GET" -> handleGet(exchange, path, query);
                case "POST" -> handlePost(exchange, path);
                case "PUT" -> handlePut(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default -> sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (IllegalArgumentException e) {
            // خطای پارامتر نامعتبر - 400 Bad Request
            sendErrorResponse(exchange, 400, "Bad request: " + e.getMessage());
        } catch (Exception e) {
            // خطای سرور - 500 Internal Server Error
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * مدیریت تمام GET requests
     * 
     * این متد پیچیده‌ترین بخش کنترلر است و انواع مختلف GET endpoints را پشتیبانی می‌کند:
     * - دریافت اعلان‌ها با فیلترهای مختلف
     * - صفحه‌بندی اعلان‌ها
     * - آمار و شمارش
     * - جستجوهای تخصصی بر اساس entity ها
     * 
     * @param exchange HTTP exchange object
     * @param path مسیر درخواست
     * @param query query string پارامترها
     * @throws IOException در صورت خطا در پردازش
     */
    private void handleGet(HttpExchange exchange, String path, String query) throws IOException {
        String[] pathParts = path.split("/");
        Map<String, String> queryParams = parseQueryParams(query);

        // پشتیبانی از هر دو فرمت /api/notifications و /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // GET /api/notifications یا /notifications
        if (pathParts.length >= baseIndex + 1 && "notifications".equals(pathParts[baseIndex])) {
            // بررسی endpoint های تخصصی ابتدا
            if (pathParts.length >= baseIndex + 2) {
                String param = pathParts[baseIndex + 1];
                
                // مسیریابی به endpoint های مختلف
                if ("unread".equals(param)) {
                    // GET /api/notifications/unread?userId={id}
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleGetUnreadNotifications(exchange, userId);
                        return;
                    }
                } else if ("type".equals(param)) {
                    // GET /api/notifications/type?userId={id}&type={type}
                    String userId = queryParams.get("userId");
                    String type = queryParams.get("type");
                    if (userId != null && type != null) {
                        handleGetNotificationsByType(exchange, userId, type);
                        return;
                    }
                } else if ("priority".equals(param)) {
                    // GET /api/notifications/priority?userId={id}&priority={priority}
                    String userId = queryParams.get("userId");
                    String priority = queryParams.get("priority");
                    if (userId != null && priority != null) {
                        handleGetNotificationsByPriority(exchange, userId, priority);
                        return;
                    }
                } else if ("high-priority".equals(param)) {
                    // GET /api/notifications/high-priority?userId={id}
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleGetHighPriorityNotifications(exchange, userId);
                        return;
                    }
                } else if ("recent".equals(param)) {
                    // GET /api/notifications/recent?userId={id}&days={days}
                    String userId = queryParams.get("userId");
                    String days = queryParams.get("days");
                    if (userId != null && days != null) {
                        handleGetRecentNotifications(exchange, userId, Integer.parseInt(days));
                        return;
                    }
                } else if ("count".equals(param)) {
                    // مسیرهای مختلف count
                    if (pathParts.length >= baseIndex + 3 && "unread".equals(pathParts[baseIndex + 2])) {
                        // GET /api/notifications/count/unread?userId={id}
                        String userId = queryParams.get("userId");
                        if (userId != null) {
                            handleGetUnreadCount(exchange, userId);
                            return;
                        }
                    }
                    if (pathParts.length >= baseIndex + 3 && "type".equals(pathParts[baseIndex + 2])) {
                        // GET /api/notifications/count/type?userId={id}&type={type}
                        String userId = queryParams.get("userId");
                        String type = queryParams.get("type");
                        if (userId != null && type != null) {
                            handleGetNotificationCountByType(exchange, userId, type);
                            return;
                        }
                    }
                    if (pathParts.length >= baseIndex + 3 && "high-priority-unread".equals(pathParts[baseIndex + 2])) {
                        // GET /api/notifications/count/high-priority-unread?userId={id}
                        String userId = queryParams.get("userId");
                        if (userId != null) {
                            handleGetHighPriorityUnreadCount(exchange, userId);
                            return;
                        }
                    }
                } else if ("orders".equals(param)) {
                    // اعلان‌های مربوط به سفارشات
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
                    // اعلان‌های مربوط به رستوران‌ها
                    String restaurantId = queryParams.get("restaurantId");
                    if (restaurantId != null) {
                        handleGetRestaurantNotifications(exchange, restaurantId);
                        return;
                    }
                } else if ("deliveries".equals(param)) {
                    // اعلان‌های مربوط به تحویل‌ها
                    String deliveryId = queryParams.get("deliveryId");
                    if (deliveryId != null) {
                        handleGetDeliveryNotifications(exchange, deliveryId);
                        return;
                    }
                } else if ("maintenance".equals(param)) {
                    // عملیات نگهداری سیستم
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
                    // آخرین اعلان کاربر
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleGetLatestNotification(exchange, userId);
                        return;
                    }
                } else if ("stats".equals(param)) {
                    // آمارهای مختلف
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
                    // بررسی وجود اعلان فوری خوانده نشده
                    String userId = queryParams.get("userId");
                    if (userId != null) {
                        handleCheckIfUserHasUnreadHighPriorityNotifications(exchange, userId);
                        return;
                    }
                }
            } else {
                // GET /api/notifications با query parameters
                String userId = queryParams.get("userId");
                String page = queryParams.get("page");
                String size = queryParams.get("size");
                
                if (userId != null) {
                    if (page != null && size != null) {
                        // صفحه‌بندی
                        handleGetUserNotificationsPaginated(exchange, userId, Integer.parseInt(page), Integer.parseInt(size));
                        return;
                    } else {
                        // تمام اعلان‌های کاربر
                        handleGetUserNotifications(exchange, userId, queryParams);
                        return;
                    }
                } else {
                    // خطا: userId الزامی است
                    sendErrorResponse(exchange, 400, "userId parameter is required");
                    return;
                }
            }
        }

        // پشتیبانی از endpoint های path-based تخصصی
        
        // GET /api/notifications/order/{orderId}
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "order".equals(pathParts[baseIndex + 1])) {
            handleGetOrderNotifications(exchange, pathParts[baseIndex + 2]);
            return;
        }
        
        // GET /api/notifications/user/{userId}/order/{orderId}
        if (pathParts.length == baseIndex + 5 && "notifications".equals(pathParts[baseIndex]) && 
            "user".equals(pathParts[baseIndex + 1]) && "order".equals(pathParts[baseIndex + 3])) {
            handleGetUserOrderNotifications(exchange, pathParts[baseIndex + 2], pathParts[baseIndex + 4]);
            return;
        }
        
        // GET /api/notifications/restaurant/{restaurantId}
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "restaurant".equals(pathParts[baseIndex + 1])) {
            handleGetRestaurantNotifications(exchange, pathParts[baseIndex + 2]);
            return;
        }
        
        // GET /api/notifications/delivery/{deliveryId}
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "delivery".equals(pathParts[baseIndex + 1])) {
            handleGetDeliveryNotifications(exchange, pathParts[baseIndex + 2]);
            return;
        }

        // GET /api/notification/{id} - فرم مفرد
        if (pathParts.length == baseIndex + 2 && "notification".equals(pathParts[baseIndex])) {
            handleGetNotificationById(exchange, pathParts[baseIndex + 1]);
            return;
        }
        
        // GET /api/notifications/{id} - زمانی که فقط شناسه اعلان است
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex])) {
            // بررسی آیا قسمت دوم عدد است (شناسه اعلان) یا endpoint خاص
            String param = pathParts[baseIndex + 1];
            try {
                Long.parseLong(param);
                // عدد است، به عنوان شناسه اعلان در نظر بگیر
                handleGetNotificationById(exchange, param);
                return;
            } catch (NumberFormatException e) {
                // عدد نیست، به عنوان endpoint دیگر ادامه پردازش
            }
        }

        // endpoint یافت نشد
        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    /**
     * مدیریت تمام POST requests
     * 
     * شامل ایجاد اعلان‌های جدید و factory methods برای انواع خاص اعلان‌ها
     * 
     * @param exchange HTTP exchange object
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در پردازش
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");

        // پشتیبانی از هر دو فرمت /api/notifications و /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // POST /api/notifications - ایجاد اعلان عمومی
        if (pathParts.length == baseIndex + 1 && "notifications".equals(pathParts[baseIndex])) {
            handleCreateNotification(exchange);
            return;
        }
        
        // POST /api/notifications/order/created - اعلان ثبت سفارش
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "order".equals(pathParts[baseIndex + 1]) && "created".equals(pathParts[baseIndex + 2])) {
            handleCreateOrderNotification(exchange);
            return;
        }
        
        // POST /api/notifications/order/status-changed - اعلان تغییر وضعیت سفارش
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "order".equals(pathParts[baseIndex + 1]) && "status-changed".equals(pathParts[baseIndex + 2])) {
            handleCreateOrderStatusChangedNotification(exchange);
            return;
        }
        
        // POST /api/notifications/delivery/assigned - اعلان اختصاص پیک
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "delivery".equals(pathParts[baseIndex + 1]) && "assigned".equals(pathParts[baseIndex + 2])) {
            handleCreateDeliveryAssignedNotification(exchange);
            return;
        }
        
        // POST /api/notifications/maintenance/daily - نگهداری روزانه
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "maintenance".equals(pathParts[baseIndex + 1]) && "daily".equals(pathParts[baseIndex + 2])) {
            handlePerformDailyMaintenance(exchange);
            return;
        }
        
        // POST /api/notifications/maintenance/cleanup - پاکسازی اعلان‌های قدیمی
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && 
            "maintenance".equals(pathParts[baseIndex + 1]) && "cleanup".equals(pathParts[baseIndex + 2])) {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);
            String days = queryParams.get("days");
            handleCleanupOldNotifications(exchange, days != null ? Integer.parseInt(days) : 30);
            return;
        }
        
        // POST /api/notifications/maintenance/purge - حذف کامل اعلان‌های منقضی
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

    /**
     * مدیریت تمام PUT requests
     * 
     * شامل عملیات‌های به‌روزرسانی وضعیت اعلان‌ها و عملیات گروهی
     * 
     * @param exchange HTTP exchange object
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در پردازش
     */
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);

        // پشتیبانی از هر دو فرمت /api/notifications و /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // PUT /api/notifications/{id}/read - علامت‌گذاری خوانده شده
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "read".equals(pathParts[baseIndex + 2])) {
            handleMarkAsRead(exchange, pathParts[baseIndex + 1]);
            return;
        }
        
        // PUT /api/notifications/{id}/unread - علامت‌گذاری خوانده نشده
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "unread".equals(pathParts[baseIndex + 2])) {
            handleMarkAsUnread(exchange, pathParts[baseIndex + 1]);
            return;
        }
        
        // PUT /api/notifications/all/read - علامت‌گذاری همه به عنوان خوانده شده
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "all".equals(pathParts[baseIndex + 1]) && "read".equals(pathParts[baseIndex + 2])) {
            String userId = queryParams.get("userId");
            if (userId != null) {
                handleMarkAllAsRead(exchange, userId);
                return;
            }
        }
        
        // PUT /api/notifications/type/read - علامت‌گذاری بر اساس نوع
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "type".equals(pathParts[baseIndex + 1]) && "read".equals(pathParts[baseIndex + 2])) {
            String userId = queryParams.get("userId");
            String type = queryParams.get("type");
            if (userId != null && type != null) {
                handleMarkNotificationsByTypeAsRead(exchange, userId, type);
                return;
            }
        }
        
        // PUT /api/notifications/mark-all-read - فرمت جایگزین
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex]) && "mark-all-read".equals(pathParts[baseIndex + 1])) {
            String userId = queryParams.get("userId");
            if (userId != null) {
                handleMarkAllAsRead(exchange, userId);
                return;
            }
        }
        
        // PUT /api/notifications/mark-read-by-type - فرمت جایگزین
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex]) && "mark-read-by-type".equals(pathParts[baseIndex + 1])) {
            String userId = queryParams.get("userId");
            String type = queryParams.get("type");
            if (userId != null && type != null) {
                handleMarkNotificationsByTypeAsRead(exchange, userId, type);
                return;
            }
        }
        
        // PUT /api/notifications/{id}/restore - بازیابی اعلان حذف شده
        if (pathParts.length == baseIndex + 3 && "notifications".equals(pathParts[baseIndex]) && "restore".equals(pathParts[baseIndex + 2])) {
            handleRestoreNotification(exchange, pathParts[baseIndex + 1]);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    /**
     * مدیریت تمام DELETE requests
     * 
     * شامل حذف منطقی اعلان‌ها
     * 
     * @param exchange HTTP exchange object
     * @param path مسیر درخواست
     * @throws IOException در صورت خطا در پردازش
     */
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);

        // پشتیبانی از هر دو فرمت /api/notifications و /notifications
        boolean hasApiPrefix = pathParts.length > 1 && "api".equals(pathParts[1]);
        int baseIndex = hasApiPrefix ? 2 : 1;

        // DELETE /api/notifications/{id} - حذف اعلان
        if (pathParts.length == baseIndex + 2 && "notifications".equals(pathParts[baseIndex])) {
            handleDeleteNotification(exchange, pathParts[baseIndex + 1]);
            return;
        }

        sendErrorResponse(exchange, 404, "Endpoint not found");
    }

    // ==================== HANDLER IMPLEMENTATIONS ====================
    
    /**
     * دریافت تمام اعلان‌های کاربر
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param queryParams پارامترهای اضافی query
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان خاص بر اساس شناسه
     * 
     * @param exchange HTTP exchange
     * @param idStr شناسه اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * ایجاد اعلان جدید عمومی
     * 
     * JSON Request Body:
     * {
     *   "userId": number,
     *   "title": string,
     *   "message": string,
     *   "type": string,
     *   "priority": string (اختیاری)
     * }
     * 
     * @param exchange HTTP exchange
     * @throws IOException در صورت خطا در پردازش
     */
    @SuppressWarnings("unchecked")
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

    /**
     * علامت‌گذاری اعلان به عنوان خوانده شده
     * 
     * @param exchange HTTP exchange
     * @param idStr شناسه اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * حذف منطقی اعلان
     * 
     * @param exchange HTTP exchange
     * @param idStr شناسه اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * بازیابی اعلان حذف شده
     * 
     * @param exchange HTTP exchange
     * @param idStr شناسه اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های خوانده نشده کاربر
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌ها بر اساس نوع
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param typeStr نوع اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌ها بر اساس اولویت
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param priorityStr اولویت اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های با اولویت بالا
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های اخیر کاربر
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param hours تعداد ساعات گذشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت تعداد اعلان‌های خوانده نشده
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های کاربر با صفحه‌بندی
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param page شماره صفحه (شروع از 0)
     * @param size تعداد رکورد در هر صفحه
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های مربوط به سفارش خاص
     * 
     * @param exchange HTTP exchange
     * @param orderIdStr شناسه سفارش به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های سفارشات کاربر (overload 1)
     * 
     * روش ساده که تمام اعلان‌های نوع ORDER_CREATED کاربر را برمی‌گرداند
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
    private void handleGetUserOrderNotifications(HttpExchange exchange, String userIdStr) throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            // به سادگی، همه اعلان‌های سفارش کاربر را برمی‌گردانیم
            // این باید با فیلتر سفارش مناسب پیاده‌سازی شود
            List<Notification> notifications = notificationService.getNotificationsByType(userId, NotificationType.ORDER_CREATED);
            sendSuccessResponse(exchange, notifications);
        } catch (NumberFormatException e) {
            sendErrorResponse(exchange, 400, "Invalid user ID format");
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    /**
     * دریافت اعلان‌های سفارش خاص کاربر (overload 2)
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param orderIdStr شناسه سفارش به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های مربوط به رستوران
     * 
     * @param exchange HTTP exchange
     * @param restaurantIdStr شناسه رستوران به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت اعلان‌های مربوط به تحویل
     * 
     * @param exchange HTTP exchange
     * @param deliveryIdStr شناسه تحویل به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت آخرین اعلان کاربر
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت تعداد اعلان‌ها بر اساس نوع
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param typeStr نوع اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * دریافت تعداد اعلان‌های فوری خوانده نشده
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * بررسی وجود اعلان فوری خوانده نشده
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * علامت‌گذاری اعلان به عنوان خوانده نشده
     * 
     * @param exchange HTTP exchange
     * @param idStr شناسه اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * علامت‌گذاری همه اعلان‌های کاربر به عنوان خوانده شده
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * علامت‌گذاری اعلان‌ها بر اساس نوع به عنوان خوانده شده
     * 
     * @param exchange HTTP exchange
     * @param userIdStr شناسه کاربر به صورت رشته
     * @param typeStr نوع اعلان به صورت رشته
     * @throws IOException در صورت خطا در پردازش
     */
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

    /**
     * انجام نگهداری روزانه سیستم اعلان‌ها
     * 
     * @param exchange HTTP exchange
     * @throws IOException در صورت خطا در پردازش
     */
    private void handlePerformDailyMaintenance(HttpExchange exchange) throws IOException {
        try {
            notificationService.performDailyMaintenance();
            sendSuccessResponse(exchange, Map.of("message", "Daily maintenance completed successfully"));
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    /**
     * پاکسازی اعلان‌های قدیمی (حذف منطقی)
     * 
     * @param exchange HTTP exchange
     * @param days تعداد روزهای گذشته برای پاکسازی
     * @throws IOException در صورت خطا در پردازش
     */
    private void handleCleanupOldNotifications(HttpExchange exchange, int days) throws IOException {
        try {
            int count = notificationService.cleanupOldNotifications(days);
            sendSuccessResponse(exchange, Map.of("cleaned", count));
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    /**
     * حذف کامل اعلان‌های منقضی از پایگاه داده
     * 
     * @param exchange HTTP exchange
     * @param days تعداد روزهای گذشته برای حذف کامل
     * @throws IOException در صورت خطا در پردازش
     */
    private void handlePurgeOldNotifications(HttpExchange exchange, int days) throws IOException {
        try {
            int count = notificationService.purgeOldNotifications(days);
            sendSuccessResponse(exchange, Map.of("purged", count));
        } catch (Exception e) {
            sendErrorResponse(exchange, 500, e.getMessage());
        }
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Factory Method: ایجاد اعلان ثبت سفارش
     * 
     * JSON Request Body:
     * {
     *   "userId": number,
     *   "orderId": number,
     *   "restaurantName": string
     * }
     * 
     * @param exchange HTTP exchange
     * @throws IOException در صورت خطا در پردازش
     */
    @SuppressWarnings("unchecked")
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

    /**
     * Factory Method: ایجاد اعلان تغییر وضعیت سفارش
     * 
     * JSON Request Body:
     * {
     *   "userId": number,
     *   "orderId": number,
     *   "newStatus": string,
     *   "oldStatus": string (اختیاری)
     * }
     * 
     * @param exchange HTTP exchange
     * @throws IOException در صورت خطا در پردازش
     */
    @SuppressWarnings("unchecked")
    private void handleCreateOrderStatusChangedNotification(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            
            Long userId = ((Number) data.get("userId")).longValue();
            Long orderId = ((Number) data.get("orderId")).longValue();
            String newStatusStr = (String) data.get("newStatus");
            String oldStatusStr = (String) data.get("oldStatus");
            
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
            OrderStatus oldStatus = oldStatusStr != null ? OrderStatus.valueOf(oldStatusStr.toUpperCase()) : null;
            
            // حذف پارامتر oldStatus چون متد NotificationService فقط 3 پارامتر می‌گیرد
            Notification notification = notificationService.notifyOrderStatusChanged(userId, orderId, newStatus);
            sendSuccessResponse(exchange, notification, 201);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        }
    }

    /**
     * Factory Method: ایجاد اعلان اختصاص پیک
     * 
     * JSON Request Body:
     * {
     *   "userId": number,
     *   "orderId": number,
     *   "deliveryId": number,
     *   "courierName": string,
     *   "estimatedDeliveryTime": string (اختیاری)
     * }
     * 
     * @param exchange HTTP exchange
     * @throws IOException در صورت خطا در پردازش
     */
    @SuppressWarnings("unchecked")
    private void handleCreateDeliveryAssignedNotification(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            Map<String, Object> data = JsonUtil.fromJson(requestBody, Map.class);
            
            Long userId = ((Number) data.get("userId")).longValue();
            Long orderId = ((Number) data.get("orderId")).longValue();
            Long deliveryId = ((Number) data.get("deliveryId")).longValue();
            String courierName = (String) data.get("courierName");
            String estimatedDeliveryTimeStr = (String) data.get("estimatedDeliveryTime");
            
            LocalDateTime estimatedDeliveryTime = null;
            if (estimatedDeliveryTimeStr != null) {
                estimatedDeliveryTime = LocalDateTime.parse(estimatedDeliveryTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            
            // استفاده از orderId و deliveryId جداگانه از JSON request
            Notification notification = notificationService.notifyDeliveryAssigned(userId, orderId, deliveryId, courierName);
            sendSuccessResponse(exchange, notification, 201);
        } catch (Exception e) {
            sendErrorResponse(exchange, 400, e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * پارس کردن Query Parameters از URL
     * 
     * @param query رشته query parameters
     * @return Map کلید-مقدار پارامترها
     */
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

    /**
     * ارسال پاسخ موفق با status code 200
     * 
     * @param exchange HTTP exchange
     * @param data داده برای ارسال
     * @throws IOException در صورت خطا در پردازش
     */
    private void sendSuccessResponse(HttpExchange exchange, Object data) throws IOException {
        sendSuccessResponse(exchange, data, 200);
    }

    /**
     * ارسال پاسخ موفق با status code دلخواه
     * 
     * @param exchange HTTP exchange
     * @param data داده برای ارسال
     * @param statusCode HTTP status code
     * @throws IOException در صورت خطا در پردازش
     */
    private void sendSuccessResponse(HttpExchange exchange, Object data, int statusCode) throws IOException {
        String jsonResponse = JsonUtil.toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    /**
     * ارسال پاسخ خطا
     * 
     * @param exchange HTTP exchange
     * @param statusCode HTTP error status code
     * @param message پیام خطا
     * @throws IOException در صورت خطا در پردازش
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String jsonResponse = JsonUtil.toJson(Map.of("error", message, "status", statusCode));
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
} 

package com.myapp.order;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Order;
import com.myapp.common.models.OrderStatus;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * کنترلر REST API مدیریت سفارشات و سبد خرید
 * 
 * این کلاس تمام عملیات HTTP مربوط به مدیریت سفارشات را پیاده‌سازی می‌کند:
 * 
 * === عملیات سبد خرید ===
 * POST   /api/orders                           - ایجاد سفارش جدید (سبد خرید)
 * POST   /api/orders/{orderId}/items           - افزودن آیتم به سبد
 * PUT    /api/orders/{orderId}/items/{itemId}  - به‌روزرسانی مقدار آیتم در سبد
 * DELETE /api/orders/{orderId}/items/{itemId}  - حذف آیتم از سبد
 * 
 * === مدیریت سفارش ===
 * GET    /api/orders/{orderId}                 - دریافت جزئیات سفارش
 * POST   /api/orders/{orderId}/place           - ثبت نهایی سفارش (پرداخت)
 * PUT    /api/orders/{orderId}/cancel          - لغو سفارش
 * PUT    /api/orders/{orderId}/status          - به‌روزرسانی وضعیت سفارش
 * 
 * === جستجو و فیلتر ===
 * GET    /api/orders/customer/{customerId}     - سفارشات مشتری
 * GET    /api/orders/restaurant/{restaurantId} - سفارشات رستوران
 * GET    /api/orders/status/{status}           - سفارشات بر اساس وضعیت
 * GET    /api/orders/active                    - سفارشات فعال
 * GET    /api/orders/pending                   - سفارشات در انتظار
 * 
 * === گزارش و آمار ===
 * GET    /api/orders/customer/{customerId}/statistics - آمار سفارشات مشتری
 * 
 * ویژگی‌های کلیدی:
 * - RESTful Design: طراحی REST استاندارد
 * - Shopping Cart: مدیریت کامل سبد خرید
 * - Order Lifecycle: مدیریت چرخه حیات سفارش
 * - Status Management: مدیریت وضعیت‌های مختلف
 * - Error Handling: مدیریت جامع خطاها
 * - JSON Processing: پردازش کامل JSON
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class OrderController implements HttpHandler {
    
    /** سرویس اصلی مدیریت سفارشات */
    private final OrderService orderService;
    
    /**
     * سازنده پیش‌فرض کنترلر
     * 
     * تمام dependency ها را به صورت خودکار ایجاد می‌کند
     * برای استفاده در محیط production
     */
    public OrderController() {
        this.orderService = new OrderService(
            new OrderRepository(), 
            new ItemRepository(), 
            new RestaurantRepository()
        );
    }
    
    /**
     * سازنده برای تزریق وابستگی
     * 
     * برای تست‌ها و dependency injection استفاده می‌شود
     * 
     * @param orderService سرویس سفارشات از خارج تزریق شده
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * متد اصلی مدیریت درخواست‌های HTTP
     * 
     * تمام درخواست‌های HTTP را دریافت و بر اساس method و path آن‌ها
     * را به متدهای مناسب هدایت می‌کند.
     * 
     * شامل مدیریت جامع خطاها:
     * - 400 Bad Request: برای پارامترهای نامعتبر
     * - 404 Not Found: برای منابع یافت نشده
     * - 405 Method Not Allowed: برای HTTP method های غیرمجاز
     * - 500 Internal Server Error: برای خطاهای سرور
     * 
     * @param exchange شیء HttpExchange شامل request و response
     * @throws IOException در صورت خطا در ارتباط HTTP
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        try {
            // مسیریابی بر اساس HTTP method
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
            // خطای پارامتر نامعتبر - 400 Bad Request
            sendErrorResponse(exchange, 400, e.getMessage());
        } catch (NotFoundException e) {
            // خطای یافت نشدن منبع - 404 Not Found
            sendErrorResponse(exchange, 404, e.getMessage());
        } catch (Exception e) {
            // خطای سرور - 500 Internal Server Error
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    // ==================== GET ENDPOINTS ====================
    
    /**
     * مدیریت تمام درخواست‌های GET
     * 
     * بر اساس pattern های مختلف URL، درخواست‌ها را به متدهای مناسب هدایت می‌کند.
     * از regex pattern matching برای شناسایی مسیرها استفاده می‌کند.
     * 
     * @param exchange HttpExchange شامل request و response
     * @param path مسیر URL درخواست
     * @throws IOException در صورت خطا در ارسال پاسخ
     */
    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/orders/\\d+")) {
            // GET /api/orders/{orderId} - Get order details
            Long orderId = extractOrderIdFromPath(path);
            getOrderDetails(exchange, orderId);
        } else if (path.matches("/api/orders/customer/\\d+")) {
            // GET /api/orders/customer/{customerId} - Get customer orders
            Long customerId = extractIdFromPath(path, "/api/orders/customer/");
            getCustomerOrders(exchange, customerId);
        } else if (path.matches("/api/orders/restaurant/\\d+")) {
            // GET /api/orders/restaurant/{restaurantId} - Get restaurant orders
            Long restaurantId = extractIdFromPath(path, "/api/orders/restaurant/");
            getRestaurantOrders(exchange, restaurantId);
        } else if (path.matches("/api/orders/status/\\w+")) {
            // GET /api/orders/status/{status} - Get orders by status
            String statusStr = extractStatusFromPath(path);
            getOrdersByStatus(exchange, statusStr);
        } else if (path.equals("/api/orders/active")) {
            // GET /api/orders/active - Get active orders
            getActiveOrders(exchange);
        } else if (path.equals("/api/orders/pending")) {
            // GET /api/orders/pending - Get pending orders
            getPendingOrders(exchange);
        } else if (path.matches("/api/orders/customer/\\d+/statistics")) {
            // GET /api/orders/customer/{customerId}/statistics - Get customer statistics
            Long customerId = extractIdFromPath(path, "/api/orders/customer/", "/statistics");
            getCustomerStatistics(exchange, customerId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * دریافت جزئیات کامل یک سفارش
     * 
     * @param exchange HttpExchange object
     * @param orderId شناسه سفارش
     * @throws IOException در صورت خطا در ارسال پاسخ
     */
    private void getOrderDetails(HttpExchange exchange, Long orderId) throws IOException {
        Order order = orderService.getOrder(orderId);
        sendJsonResponse(exchange, 200, order);
    }
    
    /**
     * دریافت تمام سفارشات یک مشتری
     * 
     * @param exchange HttpExchange object
     * @param customerId شناسه مشتری
     * @throws IOException در صورت خطا در ارسال پاسخ
     */
    private void getCustomerOrders(HttpExchange exchange, Long customerId) throws IOException {
        List<Order> orders = orderService.getCustomerOrders(customerId);
        sendJsonResponse(exchange, 200, orders);
    }
    
    /**
     * دریافت تمام سفارشات یک رستوران
     * 
     * @param exchange HttpExchange object
     * @param restaurantId شناسه رستوران
     * @throws IOException در صورت خطا در ارسال پاسخ
     */
    private void getRestaurantOrders(HttpExchange exchange, Long restaurantId) throws IOException {
        List<Order> orders = orderService.getRestaurantOrders(restaurantId);
        sendJsonResponse(exchange, 200, orders);
    }
    
    private void getOrdersByStatus(HttpExchange exchange, String statusStr) throws IOException {
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            List<Order> orders = orderService.getOrdersByStatus(status);
            sendJsonResponse(exchange, 200, orders);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    private void getActiveOrders(HttpExchange exchange) throws IOException {
        List<Order> orders = orderService.getActiveOrders();
        sendJsonResponse(exchange, 200, orders);
    }
    
    private void getPendingOrders(HttpExchange exchange) throws IOException {
        List<Order> orders = orderService.getPendingOrders();
        sendJsonResponse(exchange, 200, orders);
    }
    
    private void getCustomerStatistics(HttpExchange exchange, Long customerId) throws IOException {
        OrderService.OrderStatistics stats = orderService.getCustomerOrderStatistics(customerId);
        sendJsonResponse(exchange, 200, stats);
    }
    
    // ==================== POST ENDPOINTS ====================
    
    /**
     * مدیریت تمام درخواست‌های POST
     * 
     * شامل عملیات ایجاد و افزودن:
     * - ایجاد سفارش جدید (سبد خرید)
     * - افزودن آیتم به سبد خرید
     * - ثبت نهایی سفارش (checkout)
     * 
     * @param exchange HttpExchange object
     * @param path مسیر URL درخواست
     * @throws IOException در صورت خطا در پردازش
     */
    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/api/orders")) {
            // POST /api/orders - Create new order (shopping cart)
            createOrder(exchange);
        } else if (path.matches("/api/orders/\\d+/items")) {
            // POST /api/orders/{orderId}/items - Add item to cart
            Long orderId = extractOrderIdFromPath(path, "/items");
            addItemToCart(exchange, orderId);
        } else if (path.matches("/api/orders/\\d+/place")) {
            // POST /api/orders/{orderId}/place - Place order (checkout)
            Long orderId = extractOrderIdFromPath(path, "/place");
            placeOrder(exchange, orderId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    /**
     * ایجاد سفارش جدید (سبد خرید خالی)
     * 
     * JSON Request Body:
     * {
     *   "customerId": number,
     *   "restaurantId": number,
     *   "deliveryAddress": string,
     *   "phone": string
     * }
     * 
     * @param exchange HttpExchange object
     * @throws IOException در صورت خطا در پردازش
     */
    private void createOrder(HttpExchange exchange) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        // استخراج اطلاعات مورد نیاز از JSON
        Long customerId = getLongFromMap(requestData, "customerId");
        Long restaurantId = getLongFromMap(requestData, "restaurantId");
        String deliveryAddress = getStringFromMap(requestData, "deliveryAddress");
        String phone = getStringFromMap(requestData, "phone");
        
        // ایجاد سفارش جدید و ارسال پاسخ
        Order order = orderService.createOrder(customerId, restaurantId, deliveryAddress, phone);
        sendJsonResponse(exchange, 201, order);
    }
    
    private void addItemToCart(HttpExchange exchange, Long orderId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        
        Long itemId = getLongFromMap(requestData, "itemId");
        Integer quantity = getIntegerFromMap(requestData, "quantity");
        
        if (quantity == null) {
            sendErrorResponse(exchange, 400, "Quantity is required");
            return;
        }
        
        Order order = orderService.addItemToCart(orderId, itemId, quantity);
        sendJsonResponse(exchange, 200, order);
    }
    
    private void placeOrder(HttpExchange exchange, Long orderId) throws IOException {
        Order order = orderService.placeOrder(orderId);
        sendJsonResponse(exchange, 200, order);
    }
    
    // ==================== PUT ENDPOINTS ====================
    
    private void handlePut(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/orders/\\d+/items/\\d+")) {
            // PUT /api/orders/{orderId}/items/{itemId} - Update item quantity in cart
            Long orderId = extractOrderIdFromPath(path, "/items/\\d+");
            Long itemId = extractItemIdFromPath(path);
            updateItemQuantity(exchange, orderId, itemId);
        } else if (path.matches("/api/orders/\\d+/cancel")) {
            // PUT /api/orders/{orderId}/cancel - Cancel order
            Long orderId = extractOrderIdFromPath(path, "/cancel");
            cancelOrder(exchange, orderId);
        } else if (path.matches("/api/orders/\\d+/status")) {
            // PUT /api/orders/{orderId}/status - Update order status
            Long orderId = extractOrderIdFromPath(path, "/status");
            updateOrderStatus(exchange, orderId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void updateItemQuantity(HttpExchange exchange, Long orderId, Long itemId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        Integer quantity = getIntegerFromMap(requestData, "quantity");
        
        if (quantity == null) {
            sendErrorResponse(exchange, 400, "Quantity is required");
            return;
        }
        
        Order order = orderService.updateItemQuantity(orderId, itemId, quantity);
        sendJsonResponse(exchange, 200, order);
    }
    
    private void cancelOrder(HttpExchange exchange, Long orderId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        String reason = getStringFromMap(requestData, "reason");
        
        Order order = orderService.cancelOrder(orderId, reason);
        sendJsonResponse(exchange, 200, order);
    }
    
    private void updateOrderStatus(HttpExchange exchange, Long orderId) throws IOException {
        Map<String, Object> requestData = parseJsonRequest(exchange);
        String statusStr = getStringFromMap(requestData, "status");
        
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            Order order = orderService.updateOrderStatus(orderId, status);
            sendJsonResponse(exchange, 200, order);
        } catch (IllegalArgumentException e) {
            sendErrorResponse(exchange, 400, "Invalid status: " + statusStr);
        }
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/orders/\\d+/items/\\d+")) {
            // DELETE /api/orders/{orderId}/items/{itemId} - Remove item from cart
            Long orderId = extractOrderIdFromPath(path, "/items/\\d+");
            Long itemId = extractItemIdFromPath(path);
            removeItemFromCart(exchange, orderId, itemId);
        } else {
            sendErrorResponse(exchange, 404, "Endpoint not found");
        }
    }
    
    private void removeItemFromCart(HttpExchange exchange, Long orderId, Long itemId) throws IOException {
        Order order = orderService.removeItemFromCart(orderId, itemId);
        sendJsonResponse(exchange, 200, order);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * استخراج شناسه سفارش از مسیر URL
     * 
     * برای مسیرهای با فرمت /api/orders/{orderId}
     * 
     * @param path مسیر URL کامل
     * @return شناسه سفارش
     */
    private Long extractOrderIdFromPath(String path) {
        // استخراج از /api/orders/{orderId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[3]); // /api/orders/{orderId}
    }
    
    private Long extractOrderIdFromPath(String path, String suffix) {
        // Extract from /api/orders/{orderId}/suffix
        String pathWithoutSuffix = path.substring(0, path.lastIndexOf(suffix));
        String[] parts = pathWithoutSuffix.split("/");
        return Long.parseLong(parts[3]);
    }
    
    private Long extractItemIdFromPath(String path) {
        // Extract from /api/orders/{orderId}/items/{itemId}
        String[] parts = path.split("/");
        return Long.parseLong(parts[5]); // /api/orders/{orderId}/items/{itemId}
    }
    
    private Long extractIdFromPath(String path, String prefix) {
        String idStr = path.substring(prefix.length());
        return Long.parseLong(idStr);
    }
    
    private Long extractIdFromPath(String path, String prefix, String suffix) {
        String withoutPrefix = path.substring(prefix.length());
        String idStr = withoutPrefix.substring(0, withoutPrefix.indexOf(suffix));
        return Long.parseLong(idStr);
    }
    
    private String extractStatusFromPath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }
    
    /**
     * پارس کردن درخواست JSON از HTTP request body
     * 
     * این متد ساده JSON parsing انجام می‌دهد. در محیط production
     * بهتر است از کتابخانه‌هایی مثل Jackson یا Gson استفاده کرد.
     * 
     * ویژگی‌ها:
     * - پشتیبانی از String، Number، Boolean
     * - تشخیص خودکار نوع داده
     * - مدیریت فیلدهای خالی
     * 
     * @param exchange HttpExchange حاوی request body
     * @return Map شامل key-value های JSON
     * @throws IOException در صورت خطا در خواندن request body
     */
    private Map<String, Object> parseJsonRequest(HttpExchange exchange) throws IOException {
        // JSON parsing ساده - در production از Jackson یا Gson استفاده کنید
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        Map<String, Object> result = new HashMap<>();
        
        // پارسینگ JSON ساده (پیاده‌سازی ساده شده)
        if (requestBody.trim().isEmpty()) {
            return result;
        }
        
        // حذف آکولاد و تقسیم بر اساس کاما
        String content = requestBody.trim().replaceAll("[{}]", "");
        String[] pairs = content.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                
                // تلاش برای پارس کردن به عنوان عدد یا boolean
                try {
                    if (value.equals("true") || value.equals("false")) {
                        result.put(key, Boolean.parseBoolean(value));
                    } else if (value.contains(".")) {
                        result.put(key, Double.parseDouble(value));
                    } else {
                        result.put(key, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }
    
    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
        }
    }
    
    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + key + ": " + value);
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = convertToJson(data);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = Map.of(
            "error", true,
            "message", message,
            "status", statusCode
        );
        sendJsonResponse(exchange, statusCode, error);
    }
    
    private String convertToJson(Object data) {
        // Simple JSON serialization - in production, use Jackson or Gson
        if (data == null) {
            return "null";
        }
        
        if (data instanceof String) {
            return "\"" + data + "\"";
        }
        
        if (data instanceof Number || data instanceof Boolean) {
            return data.toString();
        }
        
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":");
                sb.append(convertToJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(",");
                sb.append(convertToJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        
        // For complex objects like Order, use reflection-based serialization
        return serializeObject(data);
    }
    
    private String serializeObject(Object obj) {
        // Simple object serialization for Order and other entities
        if (obj instanceof Order) {
            Order order = (Order) obj;
            return String.format(
                "{\"id\":%d,\"customerId\":%d,\"restaurantId\":%d,\"status\":\"%s\",\"totalAmount\":%.2f,\"deliveryAddress\":\"%s\",\"phone\":\"%s\",\"orderDate\":\"%s\",\"itemCount\":%d}",
                order.getId(), order.getCustomer().getId(), order.getRestaurant().getId(),
                order.getStatus(), order.getTotalAmount(), order.getDeliveryAddress(),
                order.getPhone(), order.getOrderDate(), order.getOrderItems().size()
            );
        }
        
        if (obj instanceof OrderService.OrderStatistics) {
            OrderService.OrderStatistics stats = (OrderService.OrderStatistics) obj;
            return String.format(
                "{\"totalOrders\":%d,\"completedOrders\":%d,\"cancelledOrders\":%d,\"totalSpent\":%.2f,\"activeOrders\":%d,\"averageOrderValue\":%.2f}",
                stats.getTotalOrders(), stats.getCompletedOrders(), stats.getCancelledOrders(),
                stats.getTotalSpent(), stats.getActiveOrders(), stats.getAverageOrderValue()
            );
        }
        
        // Fallback to toString
        return "\"" + obj.toString() + "\"";
    }
}

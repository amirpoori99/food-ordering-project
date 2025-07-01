package com.myapp.order;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * سرویس مدیریت سفارشات و عملیات سبد خرید
 * 
 * این کلاس مسئول پردازش تمام منطق کسب‌وکار مربوط به سفارشات است:
 * 
 * === عملیات سبد خرید ===
 * - ایجاد سفارش جدید (سبد خرید خالی)
 * - افزودن/حذف آیتم‌ها از سبد
 * - به‌روزرسانی تعداد آیتم‌ها
 * - محاسبه قیمت کل سفارش
 * 
 * === مدیریت چرخه حیات سفارش ===
 * - ثبت نهایی سفارش (از PENDING به CONFIRMED)
 * - پیگیری مراحل آماده‌سازی (PREPARING → READY)
 * - مدیریت تحویل (OUT_FOR_DELIVERY → DELIVERED)
 * - لغو سفارش در مراحل مجاز
 * 
 * === اعتبارسنجی کسب‌وکار ===
 * - بررسی موجودی آیتم‌ها
 * - اعتبارسنجی تعلق آیتم به رستوران
 * - چک کردن وضعیت رستوران
 * - مدیریت تبدیل وضعیت‌های مجاز
 * 
 * === آمار و گزارش‌گیری ===
 * - آمار سفارش‌های مشتری
 * - محاسبه مبلغ کل خرید
 * - تعداد سفارش‌های موفق/ناموفق
 * 
 * ویژگی‌های کلیدی:
 * - Inventory Management: مدیریت موجودی در زمان ثبت سفارش
 * - Business Rules: اعمال قوانین کسب‌وکار پیچیده
 * - Data Integrity: حفظ یکپارچگی داده‌ها
 * - Error Handling: مدیریت خطاهای مختلف
 * - Status Validation: بررسی صحت تغییر وضعیت‌ها
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class OrderService {
    
    // Repository های مورد نیاز برای دسترسی به داده
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;
    
    /**
     * سازنده سرویس سفارش با تزریق وابستگی‌ها
     * 
     * @param orderRepository repository سفارشات
     * @param itemRepository repository آیتم‌های غذایی
     * @param restaurantRepository repository رستوران‌ها
     */
    public OrderService(OrderRepository orderRepository, ItemRepository itemRepository, RestaurantRepository restaurantRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * ایجاد سفارش جدید برای مشتری و رستوران مشخص
     * این عملیات معادل ایجاد سبد خرید است
     * 
     * @param customerId شناسه مشتری
     * @param restaurantId شناسه رستوران
     * @param deliveryAddress آدرس تحویل
     * @param phone شماره تلفن تماس
     * @return سفارش ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     * @throws NotFoundException در صورت یافت نشدن رستوران
     */
    public Order createOrder(Long customerId, Long restaurantId, String deliveryAddress, String phone) {
        // اعتبارسنجی ورودی‌ها
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address cannot be empty");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        
        // بررسی وجود و در دسترس بودن رستوران
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            throw new NotFoundException("Restaurant not found with ID: " + restaurantId);
        }
        
        Restaurant restaurant = restaurantOpt.get();
        if (restaurant.getStatus() != RestaurantStatus.APPROVED) {
            throw new IllegalArgumentException("Restaurant is not approved for orders");
        }
        
        // ایجاد شیء کاربر (در برنامه واقعی از UserRepository دریافت می‌شود)
        User customer = new User();
        customer.setId(customerId);
        
        // ایجاد سفارش جدید
        Order order = Order.createNew(customer, restaurant, deliveryAddress.trim(), phone.trim());
        return orderRepository.saveNew(order);
    }
    
    /**
     * افزودن آیتم به سبد خرید (سفارش)
     * 
     * @param orderId شناسه سفارش
     * @param itemId شناسه آیتم غذایی
     * @param quantity تعداد مورد نظر
     * @return سفارش به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     * @throws NotFoundException در صورت یافت نشدن سفارش یا آیتم
     */
    public Order addItemToCart(Long orderId, Long itemId, int quantity) {
        // اعتبارسنجی ورودی‌ها
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + quantity);
        }
        
        // یافتن سفارش
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        
        // بررسی اینکه سفارش هنوز قابل تغییر است (وضعیت PENDING)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify order with status: " + order.getStatus());
        }
        
        // یافتن آیتم غذایی
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem item = itemOpt.get();
        
        // بررسی تعلق آیتم به همان رستوران سفارش
        if (!item.getRestaurant().getId().equals(order.getRestaurant().getId())) {
            throw new IllegalArgumentException("Item does not belong to the order's restaurant");
        }
        
        // بررسی در دسترس بودن آیتم
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available: " + item.getName());
        }
        
        // بررسی موجودی کافی
        if (!item.isInStock() || item.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for item: " + item.getName() + 
                                             ". Available: " + item.getQuantity() + ", Requested: " + quantity);
        }
        
        // افزودن آیتم به سفارش
        order.addItem(item, quantity);
        
        return orderRepository.save(order);
    }
    
    /**
     * حذف آیتم از سبد خرید
     * 
     * @param orderId شناسه سفارش
     * @param itemId شناسه آیتم غذایی
     * @return سفارش به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     * @throws NotFoundException در صورت یافت نشدن سفارش یا آیتم
     */
    public Order removeItemFromCart(Long orderId, Long itemId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID cannot be null");
        }
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify order with status: " + order.getStatus());
        }
        
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem item = itemOpt.get();
        order.removeItem(item);
        
        return orderRepository.save(order);
    }
    
    /**
     * به‌روزرسانی تعداد آیتم در سبد خرید
     * 
     * اگر تعداد جدید صفر یا منفی باشد، آیتم از سبد حذف می‌شود
     * 
     * @param orderId شناسه سفارش
     * @param itemId شناسه آیتم غذایی
     * @param newQuantity تعداد جدید
     * @return سفارش به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     * @throws NotFoundException در صورت یافت نشدن سفارش یا آیتم
     */
    public Order updateItemQuantity(Long orderId, Long itemId, int newQuantity) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID cannot be null");
        }
        if (newQuantity <= 0) {
            return removeItemFromCart(orderId, itemId);
        }
        
        // Find the order
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        
        // Validate order is still pending (can be modified)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify order with status: " + order.getStatus());
        }
        
        // Find the item
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem item = itemOpt.get();
        
        // Validate item belongs to the same restaurant
        if (!item.getRestaurant().getId().equals(order.getRestaurant().getId())) {
            throw new IllegalArgumentException("Item does not belong to the order's restaurant");
        }
        
        // Validate item is available
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available: " + item.getName());
        }
        
        // Set the new quantity directly
        order.setItemQuantity(item, newQuantity);
        
        return orderRepository.save(order);
    }
    
    /**
     * ثبت نهایی سفارش (تأیید سبد خرید)
     * 
     * این عملیات سفارش را از وضعیت PENDING به CONFIRMED منتقل می‌کند
     * موجودی آیتم‌ها کاهش یافته و سفارش قابل تغییر نخواهد بود
     * 
     * @param orderId شناسه سفارش
     * @return سفارش تأیید شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن وضعیت یا خالی بودن سبد
     * @throws NotFoundException در صورت یافت نشدن سفارش
     */
    public Order placeOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order cannot be placed. Current status: " + order.getStatus());
        }
        
        if (order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place empty order");
        }
        
        // Validate all items are still available and in stock
        for (OrderItem orderItem : order.getOrderItems()) {
            FoodItem item = orderItem.getFoodItem();
            
            // Refresh item from database to get latest stock info
            Optional<FoodItem> currentItemOpt = itemRepository.findById(item.getId());
            if (currentItemOpt.isEmpty()) {
                throw new IllegalArgumentException("Item no longer exists: " + item.getName());
            }
            
            FoodItem currentItem = currentItemOpt.get();
            if (!currentItem.getAvailable()) {
                throw new IllegalArgumentException("Item is no longer available: " + item.getName());
            }
            
            if (currentItem.getQuantity() < orderItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for item: " + item.getName() + 
                                                 ". Available: " + currentItem.getQuantity() + 
                                                 ", Ordered: " + orderItem.getQuantity());
            }
        }
        
        // Decrease inventory for all items
        for (OrderItem orderItem : order.getOrderItems()) {
            FoodItem item = orderItem.getFoodItem();
            item.decreaseQuantity(orderItem.getQuantity());
            itemRepository.save(item);
        }
        
        // Confirm the order
        order.confirm();
        
        // Set estimated delivery time (30-60 minutes from now)
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusMinutes(30 + (int)(Math.random() * 30));
        order.setEstimatedDeliveryTime(estimatedDelivery);
        
        return orderRepository.save(order);
    }
    
    /**
     * لغو سفارش در صورت امکان‌پذیر بودن
     * 
     * سفارش‌هایی که در وضعیت‌های PENDING، CONFIRMED، یا PREPARING هستند قابل لغو هستند
     * در صورت لغو، موجودی آیتم‌ها (در صورت نیاز) بازگردانده می‌شود
     * 
     * @param orderId شناسه سفارش
     * @param reason دلیل لغو (اختیاری)
     * @return سفارش لغو شده
     * @throws IllegalArgumentException در صورت عدم امکان لغو
     * @throws NotFoundException در صورت یافت نشدن سفارش
     */
    public Order cancelOrder(Long orderId, String reason) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        
        if (!order.canBeCancelled()) {
            throw new IllegalArgumentException("Order cannot be cancelled. Current status: " + order.getStatus());
        }
        
        // If order was confirmed, restore inventory
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PREPARING) {
            for (OrderItem orderItem : order.getOrderItems()) {
                FoodItem item = orderItem.getFoodItem();
                item.increaseQuantity(orderItem.getQuantity());
                itemRepository.save(item);
            }
        }
        
        order.cancel();
        if (reason != null && !reason.trim().isEmpty()) {
            order.setNotes(reason.trim());
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * دریافت جزئیات سفارش بر اساس شناسه
     * 
     * @param orderId شناسه سفارش
     * @return جزئیات کامل سفارش
     * @throws IllegalArgumentException در صورت null بودن شناسه
     * @throws NotFoundException در صورت یافت نشدن سفارش
     */
    public Order getOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        return orderOpt.get();
    }
    
    /**
     * دریافت تمام سفارش‌های یک مشتری
     * 
     * @param customerId شناسه مشتری
     * @return لیست سفارش‌های مشتری مرتب شده بر اساس تاریخ (جدیدترین ابتدا)
     * @throws IllegalArgumentException در صورت null بودن شناسه
     */
    public List<Order> getCustomerOrders(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        
        return orderRepository.findByCustomer(customerId);
    }
    
    /**
     * دریافت تمام سفارش‌های یک رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست سفارش‌های رستوران مرتب شده بر اساس تاریخ
     * @throws IllegalArgumentException در صورت null بودن شناسه
     */
    public List<Order> getRestaurantOrders(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        return orderRepository.findByRestaurant(restaurantId);
    }
    
    /**
     * دریافت سفارش‌ها بر اساس وضعیت
     * 
     * @param status وضعیت سفارش (PENDING, CONFIRMED, PREPARING, ...)
     * @return لیست سفارش‌های با وضعیت مشخص
     * @throws IllegalArgumentException در صورت null بودن وضعیت
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return orderRepository.findByStatus(status);
    }
    
    /**
     * به‌روزرسانی وضعیت سفارش (برای رستوران/ادمین)
     * 
     * تنها تغییرات مجاز بین وضعیت‌ها امکان‌پذیر است
     * 
     * @param orderId شناسه سفارش
     * @param newStatus وضعیت جدید
     * @return سفارش به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت تغییر غیرمجاز وضعیت
     * @throws NotFoundException در صورت یافت نشدن سفارش
     */
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new NotFoundException("Order", orderId);
        }
        
        Order order = orderOpt.get();
        
        // Validate status transition
        OrderStatus currentStatus = order.getStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        // Handle special status changes
        if (newStatus == OrderStatus.DELIVERED) {
            order.markAsDelivered();
        } else {
            orderRepository.updateStatus(orderId, newStatus);
            order.setStatus(newStatus);
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * Validates if a status transition is allowed.
     */
    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case PENDING:
                return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED:
                return to == OrderStatus.PREPARING || to == OrderStatus.CANCELLED;
            case PREPARING:
                return to == OrderStatus.READY || to == OrderStatus.CANCELLED;
            case READY:
                return to == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
    
    /**
     * Gets active orders (not delivered or cancelled).
     */
    public List<Order> getActiveOrders() {
        return orderRepository.findActiveOrders();
    }
    
    /**
     * Gets pending orders waiting for confirmation.
     */
    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrders();
    }
    
    /**
     * Calculates order statistics for a customer.
     */
    public OrderStatistics getCustomerOrderStatistics(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        
        List<Order> orders = getCustomerOrders(customerId);
        
        int totalOrders = orders.size();
        int completedOrders = (int) orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        int cancelledOrders = (int) orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        double totalSpent = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
        
        return new OrderStatistics(totalOrders, completedOrders, cancelledOrders, totalSpent);
    }
    
    /**
     * کلاس آماری برای اطلاعات سفارش‌ها
     * 
     * حاوی آمار کلی فعالیت سفارش‌گیری یک مشتری شامل:
     * - تعداد کل سفارش‌ها
     * - تعداد سفارش‌های تکمیل شده و لغو شده
     * - مبلغ کل خرید و میانگین ارزش سفارش
     */
    public static class OrderStatistics {
        private final int totalOrders;
        private final int completedOrders;
        private final int cancelledOrders;
        private final double totalSpent;
        
        public OrderStatistics(int totalOrders, int completedOrders, int cancelledOrders, double totalSpent) {
            this.totalOrders = totalOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalSpent = totalSpent;
        }
        
        public int getTotalOrders() { return totalOrders; }
        public int getCompletedOrders() { return completedOrders; }
        public int getCancelledOrders() { return cancelledOrders; }
        public double getTotalSpent() { return totalSpent; }
        public int getActiveOrders() { return totalOrders - completedOrders - cancelledOrders; }
        public double getAverageOrderValue() { 
            return completedOrders > 0 ? totalSpent / completedOrders : 0.0; 
        }
    }
}

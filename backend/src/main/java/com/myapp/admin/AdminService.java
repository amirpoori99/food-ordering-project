package com.myapp.admin;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.courier.DeliveryRepository;
import com.myapp.order.OrderRepository;
import com.myapp.payment.PaymentRepository;
import com.myapp.restaurant.RestaurantRepository;

import java.util.List;
import java.util.Map;

/**
 * Service لایه منطق کسب‌وکار برای عملیات پنل مدیریت (Admin Dashboard)
 * 
 * این کلاس تمام منطق کسب‌وکار مربوط به مدیریت سیستم سفارش غذا را پیاده‌سازی می‌کند:
 * 
 * === مدیریت کاربران (User Management) ===
 * - getAllUsers(): دریافت کاربران با pagination و فیلتر
 * - countUsers(): شمارش کاربران با فیلتر
 * - getUserById(): دریافت کاربر بر اساس شناسه
 * - getUserStatsByRole(): آمار کاربران بر اساس نقش
 * - updateUserStatus(): فعال/غیرفعال کردن کاربر
 * 
 * === مدیریت رستوران‌ها (Restaurant Management) ===
 * - getAllRestaurants(): دریافت رستوران‌ها با pagination و فیلتر
 * - countRestaurants(): شمارش رستوران‌ها
 * - getRestaurantById(): دریافت رستوران بر اساس شناسه
 * - getRestaurantStatsByStatus(): آمار رستوران‌ها
 * - updateRestaurantStatus(): تایید/رد/تعلیق رستوران
 * 
 * === مدیریت سفارشات (Order Management) ===
 * - getAllOrders(): دریافت سفارشات با فیلترهای پیشرفته
 * - countOrders(): شمارش سفارشات
 * - getOrderById(): دریافت سفارش بر اساس شناسه
 * - getOrderStatsByStatus(): آمار سفارشات
 * - updateOrderStatus(): تغییر وضعیت سفارش (admin override)
 * 
 * === مدیریت تراکنش‌ها (Transaction Management) ===
 * - getAllTransactions(): دریافت تراکنش‌ها با فیلتر
 * - countTransactions(): شمارش تراکنش‌ها
 * - getTransactionById(): دریافت تراکنش بر اساس شناسه
 * 
 * === مدیریت تحویل (Delivery Management) ===
 * - getAllDeliveries(): دریافت تحویل‌ها با فیلتر
 * - countDeliveries(): شمارش تحویل‌ها
 * - getDeliveryById(): دریافت تحویل بر اساس شناسه
 * 
 * === آمار سیستم (System Statistics) ===
 * - getSystemStatistics(): دریافت آمار کلی سیستم
 * - getDailyStatistics(): دریافت آمار روزانه
 * 
 * === مدیریت مجوزها (Permission Management) ===
 * - verifyAdminPermissions(): تایید مجوزهای مدیریتی
 * 
 * === ویژگی‌های کلیدی ===
 * - Business Logic Validation: اعتبارسنجی منطق کسب‌وکار
 * - Permission Checks: بررسی مجوزهای دسترسی
 * - Data Validation: اعتبارسنجی ورودی‌ها
 * - Error Handling: مدیریت خطاها
 * - Pagination Logic: منطق صفحه‌بندی
 * - Enum Conversion: تبدیل string به enum
 * - Security Enforcement: اجرای امنیت
 * - Admin Privilege Verification: تایید مجوزهای ادمین
 * - Business Rules: اجرای قوانین کسب‌وکار
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class AdminService {
    
    /** Repository لایه دسترسی داده مدیریت */
    private final AdminRepository adminRepository;
    /** Repository لایه دسترسی داده کاربران */
    private final AuthRepository authRepository;
    /** Repository لایه دسترسی داده رستوران‌ها */
    private final RestaurantRepository restaurantRepository;
    /** Repository لایه دسترسی داده سفارشات */
    private final OrderRepository orderRepository;
    /** Repository لایه دسترسی داده پرداخت‌ها */
    private final PaymentRepository paymentRepository;
    /** Repository لایه دسترسی داده تحویل‌ها */
    private final DeliveryRepository deliveryRepository;
    
    /**
     * سازنده با تزریق وابستگی‌ها
     * 
     * @param adminRepository repository مدیریت
     * @param authRepository repository کاربران
     * @param restaurantRepository repository رستوران‌ها
     * @param orderRepository repository سفارشات
     * @param paymentRepository repository پرداخت‌ها
     * @param deliveryRepository repository تحویل‌ها
     */
    public AdminService(AdminRepository adminRepository, AuthRepository authRepository,
                       RestaurantRepository restaurantRepository, OrderRepository orderRepository,
                       PaymentRepository paymentRepository, DeliveryRepository deliveryRepository) {
        this.adminRepository = adminRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.deliveryRepository = deliveryRepository;
    }

    // ==================== مدیریت کاربران (USER MANAGEMENT) ====================
    
    /**
     * دریافت تمام کاربران با pagination و فیلتر
     * 
     * شامل اعتبارسنجی پارامترها و محدودیت‌های امنیتی
     * 
     * @param searchTerm عبارت جستجو (اختیاری)
     * @param role نقش کاربر برای فیلتر (اختیاری)
     * @param page شماره صفحه (شروع از 0)
     * @param size تعداد رکوردها در صفحه
     * @return لیست کاربران با pagination
     * @throws IllegalArgumentException در صورت نقش نامعتبر
     */
    public List<User> getAllUsers(String searchTerm, String role, int page, int size) {
        // اعتبارسنجی و تنظیم پارامترها
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100; // محدودیت حداکثر اندازه صفحه برای بهبود عملکرد
        
        // تبدیل string به enum
        User.Role roleEnum = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllUsers(searchTerm, roleEnum, size, offset);
    }
    
    /**
     * شمارش کاربران با فیلتر
     * 
     * برای محاسبه pagination و نمایش آمار
     * 
     * @param searchTerm عبارت جستجو
     * @param role نقش کاربر
     * @return تعداد کل کاربران
     * @throws IllegalArgumentException در صورت نقش نامعتبر
     */
    public Long countUsers(String searchTerm, String role) {
        User.Role roleEnum = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
        
        return adminRepository.countUsers(searchTerm, roleEnum);
    }
    
    /**
     * دریافت کاربر بر اساس شناسه
     * 
     * @param userId شناسه کاربر
     * @return کاربر یافت شده
     * @throws IllegalArgumentException در صورت شناسه نامعتبر
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public User getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        
        return authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
    }
    
    /**
     * دریافت آمار کاربران بر اساس نقش
     * 
     * برای نمایش نمودار توزیع نقش‌ها در dashboard
     * 
     * @return Map حاوی تعداد کاربران هر نقش
     */
    public Map<User.Role, Long> getUserStatsByRole() {
        return adminRepository.getUserStatsByRole();
    }
    
    /**
     * فعال/غیرفعال کردن کاربر
     * 
     * قوانین کسب‌وکار:
     * - فقط ادمین‌ها می‌توانند وضعیت کاربران را تغییر دهند
     * - نمی‌توان وضعیت ادمین‌های دیگر را تغییر داد
     * 
     * @param userId شناسه کاربر هدف
     * @param isActive وضعیت جدید (فعال/غیرفعال)
     * @param adminId شناسه مدیر درخواست‌کننده
     * @throws IllegalArgumentException در صورت پارامترهای نامعتبر یا عدم مجوز
     * @throws NotFoundException در صورت عدم وجود کاربر یا ادمین
     */
    public void updateUserStatus(Long userId, boolean isActive, Long adminId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // تایید وجود کاربر
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        // تایید وجود ادمین و بررسی نقش
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can update user status");
        }
        
        // نمی‌توان وضعیت ادمین دیگر را تغییر داد
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalArgumentException("Cannot modify admin user status");
        }
        
        adminRepository.updateUserStatus(userId, isActive);
    }

    // ==================== مدیریت رستوران‌ها (RESTAURANT MANAGEMENT) ====================
    
    /**
     * دریافت تمام رستوران‌ها با pagination و فیلتر
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت رستوران
     * @param page شماره صفحه
     * @param size اندازه صفحه
     * @return لیست رستوران‌ها
     * @throws IllegalArgumentException در صورت وضعیت نامعتبر
     */
    public List<Restaurant> getAllRestaurants(String searchTerm, String status, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        RestaurantStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = RestaurantStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid restaurant status: " + status);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllRestaurants(searchTerm, statusEnum, size, offset);
    }
    
    /**
     * شمارش رستوران‌ها با فیلتر
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت رستوران
     * @return تعداد کل رستوران‌ها
     * @throws IllegalArgumentException در صورت وضعیت نامعتبر
     */
    public Long countRestaurants(String searchTerm, String status) {
        RestaurantStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = RestaurantStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid restaurant status: " + status);
            }
        }
        
        return adminRepository.countRestaurants(searchTerm, statusEnum);
    }
    
    /**
     * دریافت رستوران بر اساس شناسه
     * 
     * @param restaurantId شناسه رستوران
     * @return رستوران یافت شده
     * @throws IllegalArgumentException در صورت شناسه نامعتبر
     * @throws NotFoundException در صورت عدم وجود رستوران
     */
    public Restaurant getRestaurantById(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        return restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
    }
    
    /**
     * دریافت آمار رستوران‌ها بر اساس وضعیت
     * 
     * @return Map حاوی تعداد رستوران‌های هر وضعیت
     */
    public Map<RestaurantStatus, Long> getRestaurantStatsByStatus() {
        return adminRepository.getRestaurantStatsByStatus();
    }
    
    /**
     * تغییر وضعیت رستوران (تایید/رد/تعلیق)
     * 
     * فقط ادمین‌ها می‌توانند وضعیت رستوران‌ها را تغییر دهند
     * 
     * @param restaurantId شناسه رستوران
     * @param status وضعیت جدید
     * @param adminId شناسه مدیر
     * @throws IllegalArgumentException در صورت پارامترهای نامعتبر یا عدم مجوز
     * @throws NotFoundException در صورت عدم وجود رستوران یا ادمین
     */
    public void updateRestaurantStatus(Long restaurantId, RestaurantStatus status, Long adminId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Restaurant status cannot be null");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // تایید وجود رستوران
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // تایید وجود ادمین و بررسی نقش
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can update restaurant status");
        }
        
        // تغییر وضعیت رستوران
        restaurant.setStatus(status);
        restaurantRepository.update(restaurant);
    }

    // ==================== مدیریت سفارشات (ORDER MANAGEMENT) ====================
    
    /**
     * دریافت تمام سفارشات با pagination و فیلترهای پیشرفته
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت سفارش
     * @param customerId شناسه مشتری (اختیاری)
     * @param restaurantId شناسه رستوران (اختیاری)
     * @param page شماره صفحه
     * @param size اندازه صفحه
     * @return لیست سفارشات
     * @throws IllegalArgumentException در صورت وضعیت نامعتبر
     */
    public List<Order> getAllOrders(String searchTerm, String status, Long customerId, Long restaurantId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        OrderStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order status: " + status);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllOrders(searchTerm, statusEnum, customerId, restaurantId, size, offset);
    }
    
    /**
     * شمارش سفارشات با فیلتر
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت سفارش
     * @param customerId شناسه مشتری
     * @param restaurantId شناسه رستوران
     * @return تعداد کل سفارشات
     * @throws IllegalArgumentException در صورت وضعیت نامعتبر
     */
    public Long countOrders(String searchTerm, String status, Long customerId, Long restaurantId) {
        OrderStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid order status: " + status);
            }
        }
        
        return adminRepository.countOrders(searchTerm, statusEnum, customerId, restaurantId);
    }
    
    /**
     * دریافت سفارش بر اساس شناسه
     * 
     * @param orderId شناسه سفارش
     * @return سفارش یافت شده
     * @throws IllegalArgumentException در صورت شناسه نامعتبر
     * @throws NotFoundException در صورت عدم وجود سفارش
     */
    public Order getOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order", orderId));
    }
    
    /**
     * دریافت آمار سفارشات بر اساس وضعیت
     * 
     * @return Map حاوی تعداد سفارشات هر وضعیت
     */
    public Map<OrderStatus, Long> getOrderStatsByStatus() {
        return adminRepository.getOrderStatsByStatus();
    }
    
    /**
     * تغییر وضعیت سفارش (admin override)
     * 
     * ادمین‌ها می‌توانند وضعیت سفارشات را در موارد ضروری تغییر دهند
     * 
     * @param orderId شناسه سفارش
     * @param status وضعیت جدید
     * @param adminId شناسه مدیر
     * @throws IllegalArgumentException در صورت پارامترهای نامعتبر یا عدم مجوز
     * @throws NotFoundException در صورت عدم وجود سفارش یا ادمین
     */
    public void updateOrderStatus(Long orderId, OrderStatus status, Long adminId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        // تایید وجود سفارش
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order", orderId));
        
        // تایید وجود ادمین و بررسی نقش
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can override order status");
        }
        
        // تغییر وضعیت سفارش
        order.setStatus(status);
        orderRepository.update(order);
    }

    // ==================== مدیریت تراکنش‌ها (TRANSACTION MANAGEMENT) ====================
    
    /**
     * دریافت تمام تراکنش‌ها با pagination و فیلتر
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت تراکنش
     * @param type نوع تراکنش
     * @param userId شناسه کاربر
     * @param page شماره صفحه
     * @param size اندازه صفحه
     * @return لیست تراکنش‌ها
     * @throws IllegalArgumentException در صورت وضعیت یا نوع نامعتبر
     */
    public List<Transaction> getAllTransactions(String searchTerm, String status, String type, Long userId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        TransactionStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction status: " + status);
            }
        }
        
        TransactionType typeEnum = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                typeEnum = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction type: " + type);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllTransactions(searchTerm, statusEnum, typeEnum, userId, size, offset);
    }
    
    /**
     * شمارش تراکنش‌ها با فیلتر
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت تراکنش
     * @param type نوع تراکنش
     * @param userId شناسه کاربر
     * @return تعداد کل تراکنش‌ها
     * @throws IllegalArgumentException در صورت وضعیت یا نوع نامعتبر
     */
    public Long countTransactions(String searchTerm, String status, String type, Long userId) {
        TransactionStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction status: " + status);
            }
        }
        
        TransactionType typeEnum = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                typeEnum = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid transaction type: " + type);
            }
        }
        
        return adminRepository.countTransactions(searchTerm, statusEnum, typeEnum, userId);
    }
    
    /**
     * دریافت تراکنش بر اساس شناسه
     * 
     * @param transactionId شناسه تراکنش
     * @return تراکنش یافت شده
     * @throws IllegalArgumentException در صورت شناسه نامعتبر
     * @throws NotFoundException در صورت عدم وجود تراکنش
     */
    public Transaction getTransactionById(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive");
        }
        
        return paymentRepository.findById(transactionId)
            .orElseThrow(() -> new NotFoundException("Transaction", transactionId));
    }

    // ==================== مدیریت تحویل (DELIVERY MANAGEMENT) ====================
    
    /**
     * دریافت تمام تحویل‌ها با pagination و فیلتر
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت تحویل
     * @param courierId شناسه پیک
     * @param page شماره صفحه
     * @param size اندازه صفحه
     * @return لیست تحویل‌ها
     * @throws IllegalArgumentException در صورت وضعیت نامعتبر
     */
    public List<Delivery> getAllDeliveries(String searchTerm, String status, Long courierId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        DeliveryStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = DeliveryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid delivery status: " + status);
            }
        }
        
        int offset = page * size;
        return adminRepository.getAllDeliveries(searchTerm, statusEnum, courierId, size, offset);
    }
    
    /**
     * شمارش تحویل‌ها با فیلتر
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت تحویل
     * @param courierId شناسه پیک
     * @return تعداد کل تحویل‌ها
     * @throws IllegalArgumentException در صورت وضعیت نامعتبر
     */
    public Long countDeliveries(String searchTerm, String status, Long courierId) {
        DeliveryStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = DeliveryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid delivery status: " + status);
            }
        }
        
        return adminRepository.countDeliveries(searchTerm, statusEnum, courierId);
    }
    
    /**
     * دریافت تحویل بر اساس شناسه
     * 
     * @param deliveryId شناسه تحویل
     * @return تحویل یافت شده
     * @throws IllegalArgumentException در صورت شناسه نامعتبر
     * @throws NotFoundException در صورت عدم وجود تحویل
     */
    public Delivery getDeliveryById(Long deliveryId) {
        if (deliveryId == null || deliveryId <= 0) {
            throw new IllegalArgumentException("Delivery ID must be positive");
        }
        
        return deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));
    }

    // ==================== آمار سیستم (SYSTEM STATISTICS) ====================
    
    /**
     * دریافت آمار کلی سیستم
     * 
     * شامل تمام معیارهای کلیدی عملکرد سیستم
     * 
     * @return آمار کلی سیستم
     * @throws RuntimeException در صورت خطای دیتابیس
     */
    public AdminRepository.SystemStatistics getSystemStatistics() {
        try {
            return adminRepository.getSystemStatistics();
        } catch (Exception e) {
            // در صورت خطا، RuntimeException برمی‌گردانیم
            throw new RuntimeException("Database error", e);
        }
    }
    
    /**
     * دریافت آمار روزانه برای چند روز گذشته
     * 
     * @param days تعداد روزهای گذشته (حداکثر 90 روز)
     * @return لیست آمار روزانه
     */
    public List<AdminRepository.DailyStatistics> getDailyStatistics(int days) {
        if (days <= 0) days = 7;
        if (days > 90) days = 90; // محدودیت به 90 روز
        
        return adminRepository.getDailyStatistics(days);
    }
    
    /**
     * تایید مجوزهای مدیریتی
     * 
     * بررسی اینکه کاربر دارای نقش ادمین است یا خیر
     * 
     * @param adminId شناسه کاربر
     * @throws IllegalArgumentException در صورت شناسه نامعتبر یا عدم مجوز
     * @throws NotFoundException در صورت عدم وجود کاربر
     */
    public void verifyAdminPermissions(Long adminId) {
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be positive");
        }
        
        User admin = authRepository.findById(adminId)
            .orElseThrow(() -> new NotFoundException("Admin", adminId));
        
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Access denied: Admin privileges required");
        }
    }
}

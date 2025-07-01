package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
import com.myapp.auth.AuthRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * لایه منطق کسب‌وکار سیستم اعلان‌ها (Service Layer Pattern)
 * 
 * این کلاس تمام منطق کسب‌وکار مربوط به اعلان‌ها را مدیریت می‌کند:
 * 
 * === عملیات پایه اعلان‌ها ===
 * - ایجاد اعلان‌های ساده و با اولویت
 * - دریافت اعلان‌ها با انواع فیلتر
 * - مدیریت وضعیت (خوانده/نخوانده)
 * - حذف منطقی و بازیابی
 * 
 * === Factory Methods اعلان‌های خاص ===
 * - اعلان ثبت سفارش جدید
 * - اعلان تغییر وضعیت سفارش
 * - اعلان اختصاص پیک
 * - اعلان پردازش پرداخت
 * - اعلان تایید رستوران
 * - اعلان نگهداری سیستم
 * 
 * === عملیات گروهی ===
 * - علامت‌گذاری دسته‌ای خوانده شده
 * - broadcast اعلان‌های عمومی
 * - تمیزکاری اعلان‌های قدیمی
 * - حذف گروهی منطقی/فیزیکی
 * 
 * === آمار و گزارش ===
 * - تعداد اعلان‌های خوانده نشده
 * - آمار بر اساس نوع و اولویت
 * - گزارش روزانه اعلان‌ها
 * - آخرین اعلان کاربر
 * 
 * === validation و امنیت ===
 * - اعتبارسنجی شناسه کاربران
 * - validation محتوای اعلان‌ها
 * - بررسی پارامترهای ورودی
 * - کنترل سطح دسترسی
 * 
 * === ویژگی‌های کلیدی ===
 * - Business Logic Validation: اعتبارسنجی منطق کسب‌وکار
 * - Factory Pattern: ایجاد اعلان‌های تخصصی
 * - Batch Processing: پردازش گروهی برای کارایی
 * - Error Handling: مدیریت پیشرفته خطاها
 * - Maintenance Operations: عملیات تعمیر و نگهداری
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class NotificationService {
    /** Repository لایه دسترسی به داده‌های اعلان‌ها */
    private final NotificationRepository notificationRepository;
    
    /** Repository لایه دسترسی به داده‌های احراز هویت */
    private final AuthRepository authRepository;

    /**
     * سازنده با dependency injection
     * 
     * @param notificationRepository repository اعلان‌ها
     * @param authRepository repository احراز هویت
     */
    public NotificationService(NotificationRepository notificationRepository, AuthRepository authRepository) {
        this.notificationRepository = notificationRepository;
        this.authRepository = authRepository;
    }

    // ==================== BASIC NOTIFICATION OPERATIONS ====================
    
    /**
     * ایجاد اعلان جدید با اولویت معمولی
     * 
     * validation کامل ورودی‌ها و ذخیره امن اعلان
     * 
     * @param userId شناسه کاربر دریافت‌کننده
     * @param title عنوان اعلان (حداکثر 100 کاراکتر)
     * @param message متن پیام (حداکثر 500 کاراکتر)
     * @param type نوع اعلان
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     * @throws RuntimeException در صورت عدم وجود کاربر
     */
    public Notification createNotification(Long userId, String title, String message, NotificationType type) {
        validateUserId(userId);
        validateNotificationContent(title, message);
        
        Notification notification = new Notification(userId, title, message, type);
        return notificationRepository.save(notification);
    }

    /**
     * ایجاد اعلان جدید با اولویت دلخواه
     * 
     * @param userId شناسه کاربر دریافت‌کننده
     * @param title عنوان اعلان
     * @param message متن پیام
     * @param type نوع اعلان
     * @param priority سطح اولویت اعلان
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     * @throws RuntimeException در صورت عدم وجود کاربر
     */
    public Notification createNotification(Long userId, String title, String message, NotificationType type, NotificationPriority priority) {
        validateUserId(userId);
        validateNotificationContent(title, message);
        
        Notification notification = new Notification(userId, title, message, type, priority);
        return notificationRepository.save(notification);
    }

    /**
     * دریافت اعلان بر اساس شناسه
     * 
     * @param id شناسه اعلان
     * @return Optional حاوی اعلان یا empty
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه
     */
    public Optional<Notification> getNotificationById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Notification ID cannot be null or negative");
        }
        return notificationRepository.findById(id);
    }

    /**
     * دریافت تمام اعلان‌های کاربر
     * 
     * @param userId شناسه کاربر
     * @return لیست اعلان‌های کاربر (مرتب بر اساس تاریخ)
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     * @throws RuntimeException در صورت عدم وجود کاربر
     */
    public List<Notification> getUserNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.findByUserId(userId);
    }

    /**
     * دریافت اعلان‌های کاربر با صفحه‌بندی
     * 
     * برای نمایش بهینه در UI و کاهش بار سرور
     * 
     * @param userId شناسه کاربر
     * @param page شماره صفحه (شروع از 0)
     * @param size تعداد آیتم در هر صفحه
     * @return لیست اعلان‌های صفحه درخواستی
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public List<Notification> getUserNotificationsPaginated(Long userId, int page, int size) {
        validateUserId(userId);
        validatePaginationParams(page, size);
        return notificationRepository.findByUserIdPaginated(userId, page, size);
    }

    /**
     * دریافت اعلان‌های خوانده نشده کاربر
     * 
     * برای نمایش badge تعداد اعلان‌های جدید
     * 
     * @param userId شناسه کاربر
     * @return لیست اعلان‌های خوانده نشده
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public List<Notification> getUnreadNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.findUnreadByUserId(userId);
    }

    /**
     * دریافت اعلان‌های کاربر بر اساس نوع
     * 
     * @param userId شناسه کاربر
     * @param type نوع اعلان (ORDER، PAYMENT، RESTAURANT، ...)
     * @return لیست اعلان‌های نوع مشخص
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public List<Notification> getNotificationsByType(Long userId, NotificationType type) {
        validateUserId(userId);
        validateNotificationType(type);
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    /**
     * دریافت اعلان‌های کاربر بر اساس اولویت
     * 
     * @param userId شناسه کاربر
     * @param priority سطح اولویت (LOW، NORMAL، HIGH، URGENT)
     * @return لیست اعلان‌های با اولویت مشخص
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public List<Notification> getNotificationsByPriority(Long userId, NotificationPriority priority) {
        validateUserId(userId);
        validateNotificationPriority(priority);
        return notificationRepository.findByUserIdAndPriority(userId, priority);
    }

    /**
     * دریافت اعلان‌های فوری کاربر
     * 
     * فقط اعلان‌های با اولویت HIGH برمی‌گرداند
     * 
     * @param userId شناسه کاربر
     * @return لیست اعلان‌های فوری
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public List<Notification> getHighPriorityNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.findHighPriorityByUserId(userId);
    }

    /**
     * دریافت اعلان‌های اخیر کاربر
     * 
     * @param userId شناسه کاربر
     * @param days تعداد روزهای گذشته
     * @return لیست اعلان‌های اخیر
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public List<Notification> getRecentNotifications(Long userId, int days) {
        validateUserId(userId);
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.findRecentByUserId(userId, days);
    }

    // ==================== NOTIFICATION STATE MANAGEMENT ====================
    
    /**
     * علامت‌گذاری اعلان به عنوان خوانده شده
     * 
     * @param notificationId شناسه اعلان
     * @return اعلان به‌روزرسانی شده
     * @throws RuntimeException در صورت عدم وجود اعلان
     */
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.markAsRead();
        return notificationRepository.update(notification);
    }

    /**
     * علامت‌گذاری اعلان به عنوان خوانده نشده
     * 
     * @param notificationId شناسه اعلان
     * @return اعلان به‌روزرسانی شده
     * @throws RuntimeException در صورت عدم وجود اعلان
     */
    public Notification markAsUnread(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.markAsUnread();
        return notificationRepository.update(notification);
    }

    /**
     * حذف منطقی اعلان
     * 
     * اعلان را کاملاً حذف نمی‌کند بلکه فقط علامت حذف می‌زند
     * 
     * @param notificationId شناسه اعلان
     * @throws RuntimeException در صورت عدم وجود اعلان
     */
    public void deleteNotification(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.softDelete();
        notificationRepository.update(notification);
    }

    /**
     * بازیابی اعلان حذف شده
     * 
     * علامت حذف را برمی‌دارد و اعلان را فعال می‌کند
     * 
     * @param notificationId شناسه اعلان
     * @throws RuntimeException در صورت عدم وجود اعلان
     */
    public void restoreNotification(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.restore();
        notificationRepository.update(notification);
    }

    // ==================== BULK OPERATIONS ====================
    
    /**
     * علامت‌گذاری تمام اعلان‌های کاربر به عنوان خوانده شده
     * 
     * عملیات bulk برای بهینه‌سازی performance
     * 
     * @param userId شناسه کاربر
     * @return تعداد اعلان‌های به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public int markAllAsRead(Long userId) {
        validateUserId(userId);
        return notificationRepository.markAllAsReadForUser(userId);
    }

    /**
     * علامت‌گذاری اعلان‌های نوع خاص به عنوان خوانده شده
     * 
     * @param userId شناسه کاربر
     * @param type نوع اعلان
     * @return تعداد اعلان‌های به‌روزرسانی شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public int markAllAsReadByType(Long userId, NotificationType type) {
        validateUserId(userId);
        validateNotificationType(type);
        return notificationRepository.markAsReadByType(userId, type);
    }

    /**
     * تمیزکاری اعلان‌های قدیمی (حذف منطقی)
     * 
     * برای نگهداری performance دیتابیس
     * 
     * @param daysOld تعداد روزهای قدیمی بودن
     * @return تعداد اعلان‌های تمیز شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن تعداد روزها
     */
    public int cleanupOldNotifications(int daysOld) {
        if (daysOld <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.softDeleteOldNotifications(daysOld);
    }

    /**
     * حذف کامل اعلان‌های منقضی (حذف فیزیکی)
     * 
     * برای بهینه‌سازی فضای دیتابیس
     * 
     * @param daysOld تعداد روزهای انقضا
     * @return تعداد اعلان‌های حذف شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن تعداد روزها
     */
    public int purgeOldNotifications(int daysOld) {
        if (daysOld <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.hardDeleteOldNotifications(daysOld);
    }

    // ==================== FACTORY METHODS FOR SPECIFIC NOTIFICATIONS ====================
    
    /**
     * ایجاد اعلان ثبت سفارش جدید
     * 
     * Factory method برای ایجاد آسان اعلان‌های سفارش
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param restaurantName نام رستوران
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public Notification notifyOrderCreated(Long userId, Long orderId, String restaurantName) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (restaurantName == null || restaurantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or empty");
        }

        Notification notification = Notification.orderCreated(userId, orderId, restaurantName);
        return notificationRepository.save(notification);
    }

    /**
     * ایجاد اعلان تغییر وضعیت سفارش
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param newStatus وضعیت جدید سفارش
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public Notification notifyOrderStatusChanged(Long userId, Long orderId, OrderStatus newStatus) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        Notification notification = Notification.orderStatusChanged(userId, orderId, newStatus);
        return notificationRepository.save(notification);
    }

    /**
     * ایجاد اعلان اختصاص پیک
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param deliveryId شناسه تحویل
     * @param courierName نام پیک
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public Notification notifyDeliveryAssigned(Long userId, Long orderId, Long deliveryId, String courierName) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (deliveryId == null || deliveryId <= 0) {
            throw new IllegalArgumentException("Delivery ID cannot be null or negative");
        }
        if (courierName == null || courierName.trim().isEmpty()) {
            throw new IllegalArgumentException("Courier name cannot be null or empty");
        }

        Notification notification = Notification.deliveryAssigned(userId, orderId, deliveryId, courierName);
        return notificationRepository.save(notification);
    }

    /**
     * ایجاد اعلان پردازش پرداخت
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param amount مبلغ پرداخت
     * @param success وضعیت موفقیت پرداخت
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public Notification notifyPaymentProcessed(Long userId, Long orderId, Double amount, boolean success) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Notification notification = Notification.paymentProcessed(userId, orderId, amount, success);
        return notificationRepository.save(notification);
    }

    /**
     * ایجاد اعلان تایید رستوران
     * 
     * @param userId شناسه کاربر (صاحب رستوران)
     * @param restaurantId شناسه رستوران
     * @param restaurantName نام رستوران
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public Notification notifyRestaurantApproved(Long userId, Long restaurantId, String restaurantName) {
        validateUserId(userId);
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID cannot be null or negative");
        }
        if (restaurantName == null || restaurantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or empty");
        }

        Notification notification = Notification.restaurantApproved(userId, restaurantId, restaurantName);
        return notificationRepository.save(notification);
    }

    /**
     * ایجاد اعلان نگهداری سیستم
     * 
     * @param userId شناسه کاربر
     * @param maintenanceTime زمان نگهداری
     * @return اعلان ایجاد شده
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public Notification notifySystemMaintenance(Long userId, LocalDateTime maintenanceTime) {
        validateUserId(userId);
        if (maintenanceTime == null) {
            throw new IllegalArgumentException("Maintenance time cannot be null");
        }

        Notification notification = Notification.systemMaintenance(userId, maintenanceTime);
        return notificationRepository.save(notification);
    }

    // ==================== BROADCAST NOTIFICATIONS ====================
    
    /**
     * پخش اعلان نگهداری سیستم به تمام کاربران فعال
     * 
     * از batch processing برای بهینه‌سازی performance استفاده می‌کند
     * 
     * @param maintenanceTime زمان نگهداری
     * @throws IllegalArgumentException در صورت null بودن زمان
     */
    public void broadcastSystemMaintenance(LocalDateTime maintenanceTime) {
        if (maintenanceTime == null) {
            throw new IllegalArgumentException("Maintenance time cannot be null");
        }

        // دریافت تمام کاربران فعال و ایجاد اعلان‌ها به صورت دسته‌ای
        List<Long> activeUserIds = notificationRepository.getAllActiveUserIds();
        List<Notification> notifications = new java.util.ArrayList<>();
        
        for (Long userId : activeUserIds) {
            try {
                Notification notification = Notification.systemMaintenance(userId, maintenanceTime);
                notifications.add(notification);
            } catch (Exception e) {
                // log خطا ولی ادامه کار با سایر کاربران
                System.err.println("Failed to create maintenance notification for user " + userId + ": " + e.getMessage());
            }
        }
        
        // ذخیره تمام اعلان‌ها به صورت دسته‌ای
        if (!notifications.isEmpty()) {
            notificationRepository.saveBatch(notifications);
        }
    }

    /**
     * پخش پیام تبلیغاتی به تمام کاربران فعال
     * 
     * @param title عنوان پیام تبلیغاتی
     * @param message متن پیام
     * @param priority سطح اولویت (پیش‌فرض: LOW)
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     */
    public void broadcastPromotionalMessage(String title, String message, NotificationPriority priority) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (priority == null) {
            priority = NotificationPriority.LOW;
        }

        // دریافت تمام کاربران فعال و ایجاد اعلان‌ها به صورت دسته‌ای
        List<Long> activeUserIds = notificationRepository.getAllActiveUserIds();
        List<Notification> notifications = new java.util.ArrayList<>();
        
        for (Long userId : activeUserIds) {
            try {
                Notification notification = new Notification(userId, title, message, NotificationType.PROMOTIONAL, priority);
                notifications.add(notification);
            } catch (Exception e) {
                // log خطا ولی ادامه کار با سایر کاربران
                System.err.println("Failed to create promotional notification for user " + userId + ": " + e.getMessage());
            }
        }
        
        // ذخیره تمام اعلان‌ها به صورت دسته‌ای
        if (!notifications.isEmpty()) {
            notificationRepository.saveBatch(notifications);
        }
    }

    // ==================== ANALYTICS AND STATISTICS ====================
    
    /**
     * دریافت تعداد اعلان‌های خوانده نشده کاربر
     * 
     * @param userId شناسه کاربر
     * @return تعداد اعلان‌های خوانده نشده
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public long getUnreadCount(Long userId) {
        validateUserId(userId);
        return notificationRepository.getUnreadCount(userId);
    }

    /**
     * دریافت تعداد اعلان‌های کاربر بر اساس نوع
     * 
     * @param userId شناسه کاربر
     * @param type نوع اعلان
     * @return تعداد اعلان‌های نوع مشخص
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public long getNotificationCountByType(Long userId, NotificationType type) {
        validateUserId(userId);
        validateNotificationType(type);
        return notificationRepository.getNotificationCountByType(userId, type);
    }

    /**
     * دریافت تعداد اعلان‌های فوری خوانده نشده
     * 
     * @param userId شناسه کاربر
     * @return تعداد اعلان‌های فوری خوانده نشده
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public long getHighPriorityUnreadCount(Long userId) {
        validateUserId(userId);
        return notificationRepository.getHighPriorityUnreadCount(userId);
    }

    /**
     * بررسی وجود اعلان‌های فوری خوانده نشده
     * 
     * @param userId شناسه کاربر
     * @return true اگر اعلان فوری خوانده نشده وجود داشته باشد
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public boolean hasUnreadHighPriorityNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.getHighPriorityUnreadCount(userId) > 0;
    }

    /**
     * دریافت آخرین اعلان کاربر
     * 
     * @param userId شناسه کاربر
     * @return Optional حاوی آخرین اعلان یا empty
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public Optional<Notification> getLatestNotification(Long userId) {
        validateUserId(userId);
        return notificationRepository.getLatestNotification(userId);
    }

    /**
     * دریافت آمار اعلان‌ها بر اساس نوع
     * 
     * نتیجه: [نوع، تعداد کل، تعداد خوانده نشده]
     * 
     * @param userId شناسه کاربر
     * @return لیست آرایه‌های آماری
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه کاربر
     */
    public List<Object[]> getNotificationStatsByType(Long userId) {
        validateUserId(userId);
        return notificationRepository.getNotificationStatsByType(userId);
    }

    /**
     * دریافت آمار روزانه اعلان‌ها
     * 
     * نتیجه: [تاریخ، تعداد اعلان‌ها]
     * 
     * @param userId شناسه کاربر
     * @param days تعداد روزهای گذشته
     * @return لیست آمار روزانه
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public List<Object[]> getDailyNotificationCounts(Long userId, int days) {
        validateUserId(userId);
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.getDailyNotificationCounts(userId, days);
    }

    // ==================== ENTITY-SPECIFIC QUERIES ====================
    
    /**
     * دریافت تمام اعلان‌های مربوط به سفارش
     * 
     * @param orderId شناسه سفارش
     * @return لیست اعلان‌های سفارش
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه سفارش
     */
    public List<Notification> getOrderNotifications(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        return notificationRepository.findOrderNotifications(orderId);
    }

    /**
     * دریافت اعلان‌های سفارش برای کاربر خاص
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @return لیست اعلان‌های سفارش کاربر
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    public List<Notification> getUserOrderNotifications(Long userId, Long orderId) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        return notificationRepository.findUserOrderNotifications(userId, orderId);
    }

    /**
     * دریافت اعلان‌های مربوط به رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست اعلان‌های رستوران
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه رستوران
     */
    public List<Notification> getRestaurantNotifications(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID cannot be null or negative");
        }
        return notificationRepository.findRestaurantNotifications(restaurantId);
    }

    /**
     * دریافت اعلان‌های مربوط به تحویل
     * 
     * @param deliveryId شناسه تحویل
     * @return لیست اعلان‌های تحویل
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه تحویل
     */
    public List<Notification> getDeliveryNotifications(Long deliveryId) {
        if (deliveryId == null || deliveryId <= 0) {
            throw new IllegalArgumentException("Delivery ID cannot be null or negative");
        }
        return notificationRepository.findDeliveryNotifications(deliveryId);
    }

    // ==================== SCHEDULED MAINTENANCE OPERATIONS ====================
    
    /**
     * عملیات نگهداری روزانه سیستم اعلان‌ها
     * 
     * شامل:
     * - حذف منطقی اعلان‌های بالای 90 روز
     * - حذف فیزیکی اعلان‌های حذف شده بالای 120 روز
     * 
     * @throws RuntimeException در صورت خطا در عملیات نگهداری
     */
    public void performDailyMaintenance() {
        try {
            // حذف منطقی اعلان‌های قدیمی‌تر از 90 روز
            int softDeleted = cleanupOldNotifications(90);
            System.out.println("Daily maintenance: Soft deleted " + softDeleted + " old notifications");
            
            // حذف فیزیکی اعلان‌های منطقاً حذف شده بیش از 30 روز پیش
            int hardDeleted = purgeOldNotifications(120);
            System.out.println("Daily maintenance: Hard deleted " + hardDeleted + " old notifications");
            
        } catch (Exception e) {
            System.err.println("Error during daily maintenance: " + e.getMessage());
            throw e;
        }
    }

    // ==================== VALIDATION METHODS ====================
    
    /**
     * اعتبارسنجی شناسه کاربر
     * 
     * بررسی معتبر بودن و وجود کاربر در سیستم
     * 
     * @param userId شناسه کاربر
     * @throws IllegalArgumentException در صورت نامعتبر بودن شناسه
     * @throws RuntimeException در صورت عدم وجود کاربر
     */
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
        
        // بررسی وجود کاربر
        if (authRepository.findById(userId).isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    /**
     * اعتبارسنجی محتوای اعلان
     * 
     * بررسی طول و محتوای عنوان و پیام
     * 
     * @param title عنوان اعلان
     * @param message متن پیام
     * @throws IllegalArgumentException در صورت نامعتبر بودن محتوا
     */
    private void validateNotificationContent(String title, String message) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification title cannot be null or empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Notification title cannot exceed 100 characters");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message cannot be null or empty");
        }
        if (message.length() > 500) {
            throw new IllegalArgumentException("Notification message cannot exceed 500 characters");
        }
    }

    /**
     * اعتبارسنجی نوع اعلان
     * 
     * @param type نوع اعلان
     * @throws IllegalArgumentException در صورت null بودن نوع
     */
    private void validateNotificationType(NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
    }

    /**
     * اعتبارسنجی اولویت اعلان
     * 
     * @param priority سطح اولویت
     * @throws IllegalArgumentException در صورت null بودن اولویت
     */
    private void validateNotificationPriority(NotificationPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Notification priority cannot be null");
        }
    }

    /**
     * اعتبارسنجی پارامترهای صفحه‌بندی
     * 
     * @param page شماره صفحه
     * @param size تعداد آیتم در صفحه
     * @throws IllegalArgumentException در صورت نامعتبر بودن پارامترها
     */
    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }
} 

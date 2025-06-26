package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity مدل اعلان‌های سیستم سفارش غذا
 * 
 * این کلاس تمام انواع اعلان‌های سیستم را مدیریت می‌کند:
 * 
 * === انواع اعلان‌ها ===
 * - اعلان‌های سفارش (ثبت، تغییر وضعیت، تحویل)
 * - اعلان‌های پرداخت (موفق، ناموفق، استرداد)
 * - اعلان‌های رستوران (تایید، به‌روزرسانی)
 * - اعلان‌های تبلیغاتی و سیستمی
 * - اعلان‌های نگهداری و یادآوری
 * 
 * === سطوح اولویت ===
 * - LOW: اطلاعات عمومی غیر ضروری
 * - NORMAL: اطلاعات معمولی
 * - MEDIUM: اطلاعات مهم
 * - HIGH: اطلاعات فوری
 * - URGENT: اطلاعات اضطراری
 * 
 * === ویژگی‌های کلیدی ===
 * - Soft Delete Pattern: حذف منطقی بدون از دست دادن داده
 * - Factory Methods: ایجاد آسان انواع مختلف اعلان
 * - Business Logic Methods: مدیریت وضعیت‌های اعلان
 * - Entity Linking: ارتباط با سفارش، رستوران، تحویل
 * - Metadata Support: ذخیره اطلاعات اضافی در JSON
 * - Multi-language Support: پشتیبانی از متن‌های فارسی
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "notifications")
public class Notification {
    /** شناسه یکتای اعلان */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** شناسه کاربر دریافت‌کننده اعلان */
    @Column(nullable = false)
    private Long userId;
    
    /** عنوان اعلان (حداکثر 100 کاراکتر) */
    @Column(nullable = false, length = 100)
    private String title;
    
    /** متن پیام اعلان (حداکثر 500 کاراکتر) */
    @Column(nullable = false, length = 500)
    private String message;
    
    /** نوع اعلان (سفارش، پرداخت، رستوران، سیستم، تبلیغ) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    /** سطح اولویت اعلان */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;
    
    /** وضعیت خوانده شدن اعلان (پیش‌فرض: false) */
    @Column(nullable = false)
    private Boolean isRead = false;
    
    /** وضعیت حذف منطقی اعلان (پیش‌فرض: false) */
    @Column(nullable = false)
    private Boolean isDeleted = false;
    
    /** زمان ایجاد اعلان */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /** زمان خوانده شدن اعلان */
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    // ==================== ENTITY LINKING FIELDS ====================
    
    /** شناسه سفارش مرتبط (برای اعلان‌های سفارش) */
    @Column(name = "order_id")
    private Long orderId;
    
    /** شناسه رستوران مرتبط (برای اعلان‌های رستوران) */
    @Column(name = "restaurant_id") 
    private Long restaurantId;
    
    /** شناسه تحویل مرتبط (برای اعلان‌های تحویل) */
    @Column(name = "delivery_id")
    private Long deliveryId;
    
    /** شناسه کلی entity مرتبط (برای انعطاف‌پذیری) */
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    /** زمان حذف منطقی */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /** داده‌های اضافی در فرمت JSON (حداکثر 1000 کاراکتر) */
    @Column(length = 1000)
    private String metadata;

    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض
     * زمان ایجاد را به زمان فعلی تنظیم می‌کند
     */
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * سازنده اعلان با اولویت معمولی
     * 
     * @param userId شناسه کاربر
     * @param title عنوان اعلان
     * @param message متن پیام
     * @param type نوع اعلان
     */
    public Notification(Long userId, String title, String message, NotificationType type) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = NotificationPriority.NORMAL;
    }

    /**
     * سازنده اعلان با اولویت دلخواه
     * 
     * @param userId شناسه کاربر
     * @param title عنوان اعلان
     * @param message متن پیام
     * @param type نوع اعلان
     * @param priority سطح اولویت
     */
    public Notification(Long userId, String title, String message, NotificationType type, NotificationPriority priority) {
        this(userId, title, message, type);
        this.priority = priority;
    }

    // ==================== BUSINESS LOGIC METHODS ====================
    
    /**
     * علامت‌گذاری اعلان به عنوان خوانده شده
     * 
     * فقط در صورتی که اعلان خوانده نشده باشد، عمل کند
     * زمان خوانده شدن را ثبت می‌کند
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
    
    /**
     * علامت‌گذاری اعلان به عنوان خوانده نشده
     * 
     * زمان خوانده شدن را پاک می‌کند
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }
    
    /**
     * حذف منطقی اعلان
     * 
     * اعلان را حذف نمی‌کند بلکه فقط علامت حذف می‌زند
     * برای نگهداری تاریخچه و امکان بازیابی
     */
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * بازیابی اعلان حذف شده
     * 
     * علامت حذف را برمی‌دارد و زمان حذف را پاک می‌کند
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
    
    /**
     * بررسی انقضای اعلان
     * 
     * @param expirationDays تعداد روزهای انقضا
     * @return true اگر اعلان منقضی شده باشد
     */
    public boolean isExpired(int expirationDays) {
        return this.createdAt.plusDays(expirationDays).isBefore(LocalDateTime.now());
    }
    
    /**
     * بررسی اولویت بالای اعلان
     * 
     * @return true اگر اعلان اولویت HIGH یا URGENT داشته باشد
     */
    public boolean isHighPriority() {
        return this.priority == NotificationPriority.HIGH || this.priority == NotificationPriority.URGENT;
    }
    
    /**
     * بررسی مرتبط بودن با سفارش
     * 
     * @return true اگر اعلان مربوط به سفارش باشد
     */
    public boolean isOrderRelated() {
        return this.orderId != null;
    }
    
    /**
     * بررسی مرتبط بودن با رستوران
     * 
     * @return true اگر اعلان مربوط به رستوران باشد
     */
    public boolean isRestaurantRelated() {
        return this.restaurantId != null;
    }
    
    /**
     * بررسی مرتبط بودن با تحویل
     * 
     * @return true اگر اعلان مربوط به تحویل باشد
     */
    public boolean isDeliveryRelated() {
        return this.deliveryId != null;
    }

    // ==================== FACTORY METHODS ====================
    
    /**
     * ایجاد اعلان ثبت سفارش جدید
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param restaurantName نام رستوران
     * @return اعلان ایجاد شده
     */
    public static Notification orderCreated(Long userId, Long orderId, String restaurantName) {
        Notification notification = new Notification(
            userId,
            "سفارش جدید ثبت شد",
            "سفارش شما از رستوران " + restaurantName + " با موفقیت ثبت شد.",
            NotificationType.ORDER_CREATED,
            NotificationPriority.NORMAL
        );
        notification.setOrderId(orderId);
        notification.setRelatedEntityId(orderId);
        return notification;
    }
    
    /**
     * ایجاد اعلان تغییر وضعیت سفارش
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param newStatus وضعیت جدید سفارش
     * @return اعلان ایجاد شده
     */
    public static Notification orderStatusChanged(Long userId, Long orderId, OrderStatus newStatus) {
        String message = getOrderStatusMessage(newStatus);
        Notification notification = new Notification(
            userId,
            "تغییر وضعیت سفارش",
            message,
            NotificationType.ORDER_STATUS_CHANGED,
            NotificationPriority.HIGH
        );
        notification.setOrderId(orderId);
        notification.setRelatedEntityId(orderId);
        return notification;
    }
    
    /**
     * ایجاد اعلان اختصاص پیک
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param deliveryId شناسه تحویل
     * @param courierName نام پیک
     * @return اعلان ایجاد شده
     */
    public static Notification deliveryAssigned(Long userId, Long orderId, Long deliveryId, String courierName) {
        Notification notification = new Notification(
            userId,
            "پیک انتخاب شد",
            "پیک " + courierName + " برای تحویل سفارش شما انتخاب شد.",
            NotificationType.DELIVERY_ASSIGNED,
            NotificationPriority.HIGH
        );
        notification.setOrderId(orderId);
        notification.setDeliveryId(deliveryId);
        notification.setRelatedEntityId(orderId);
        return notification;
    }
    
    /**
     * ایجاد اعلان پردازش پرداخت
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @param amount مبلغ پرداخت
     * @param success وضعیت موفقیت پرداخت
     * @return اعلان ایجاد شده
     */
    public static Notification paymentProcessed(Long userId, Long orderId, Double amount, boolean success) {
        String title = success ? "پرداخت موفق" : "خطا در پرداخت";
        String message = success ? 
            "پرداخت " + amount + " تومان با موفقیت انجام شد." :
            "پرداخت " + amount + " تومان ناموفق بود. لطفاً دوباره تلاش کنید.";
        NotificationPriority priority = success ? NotificationPriority.NORMAL : NotificationPriority.HIGH;
        
        Notification notification = new Notification(
            userId,
            title,
            message,
            NotificationType.PAYMENT_UPDATE,
            priority
        );
        notification.setOrderId(orderId);
        return notification;
    }
    
    /**
     * ایجاد اعلان تایید رستوران
     * 
     * @param userId شناسه کاربر (صاحب رستوران)
     * @param restaurantId شناسه رستوران
     * @param restaurantName نام رستوران
     * @return اعلان ایجاد شده
     */
    public static Notification restaurantApproved(Long userId, Long restaurantId, String restaurantName) {
        Notification notification = new Notification(
            userId,
            "رستوران تایید شد",
            "رستوران " + restaurantName + " شما توسط ادمین تایید شد.",
            NotificationType.RESTAURANT_APPROVED,
            NotificationPriority.HIGH
        );
        notification.setRestaurantId(restaurantId);
        notification.setRelatedEntityId(restaurantId);
        return notification;
    }
    
    /**
     * ایجاد اعلان نگهداری سیستم
     * 
     * @param userId شناسه کاربر
     * @param maintenanceTime زمان نگهداری
     * @return اعلان ایجاد شده
     */
    public static Notification systemMaintenance(Long userId, LocalDateTime maintenanceTime) {
        Notification notification = new Notification(
            userId,
            "نگهداری سیستم",
            "سیستم در زمان " + maintenanceTime.toString() + " به دلیل نگهداری موقتاً در دسترس نخواهد بود.",
            NotificationType.SYSTEM_MAINTENANCE,
            NotificationPriority.HIGH
        );
        return notification;
    }
    
    /**
     * تولید پیام فارسی بر اساس وضعیت سفارش
     * 
     * @param status وضعیت سفارش
     * @return پیام مناسب به فارسی
     */
    private static String getOrderStatusMessage(OrderStatus status) {
        switch (status) {
            case PENDING:
                return "سفارش شما در انتظار تایید است.";
            case CONFIRMED:
                return "سفارش شما تایید شد و در حال آماده‌سازی است.";
            case PREPARING:
                return "رستوران در حال آماده‌سازی سفارش شما است.";
            case READY:
                return "سفارش شما آماده است و منتظر پیک می‌باشد.";
            case OUT_FOR_DELIVERY:
                return "سفارش شما توسط پیک دریافت شد و در راه است.";
            case DELIVERED:
                return "سفارش شما با موفقیت تحویل داده شد.";
            case CANCELLED:
                return "متأسفانه سفارش شما لغو شد.";
            default:
                return "وضعیت سفارش شما تغییر کرد.";
        }
    }

    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه اعلان */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** @return شناسه کاربر */
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /** @return عنوان اعلان */
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    /** @return متن پیام */
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    /** @return نوع اعلان */
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    /** @return سطح اولویت */
    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    /** @return وضعیت خوانده شدن */
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    /** @return وضعیت حذف منطقی */
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    // ==================== CONVENIENCE METHODS ====================
    
    /** @return true اگر اعلان خوانده شده باشد */
    public boolean isRead() { return Boolean.TRUE.equals(isRead); }
    
    /** @return true اگر اعلان حذف شده باشد */
    public boolean isDeleted() { return Boolean.TRUE.equals(isDeleted); }

    /** @return زمان ایجاد */
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @return زمان خوانده شدن */
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    /** @return شناسه سفارش */
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    /** @return شناسه رستوران */
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    /** @return شناسه تحویل */
    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }

    /** @return داده‌های اضافی JSON */
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    /** @return شناسه entity مرتبط */
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    
    /** @return زمان حذف منطقی */
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // ==================== ENUMS ====================
    
    /**
     * انواع مختلف اعلان‌های سیستم
     * 
     * هر نوع اعلان دارای منطق و نمایش خاص خود است
     */
    public enum NotificationType {
        /** اعلان ثبت سفارش جدید */
        ORDER_CREATED,
        
        /** اعلان تغییر وضعیت سفارش */
        ORDER_STATUS_CHANGED,
        
        /** اعلان عمومی به‌روزرسانی سفارش */
        ORDER_UPDATE,
        
        /** اعلان اختصاص پیک به سفارش */
        DELIVERY_ASSIGNED,
        
        /** اعلان به‌روزرسانی وضعیت تحویل */
        DELIVERY_UPDATE, 
        
        /** اعلان‌های مربوط به پرداخت */
        PAYMENT_UPDATE,
        
        /** اعلان تایید رستوران توسط ادمین */
        RESTAURANT_APPROVED,
        
        /** اعلان‌های عمومی رستوران */
        RESTAURANT_UPDATE,
        
        /** اعلان نگهداری سیستم */
        SYSTEM_MAINTENANCE,
        
        /** اعلان‌های عمومی سیستم */
        SYSTEM_UPDATE,
        
        /** اعلان‌های تبلیغاتی */
        PROMOTIONAL,
        
        /** اعلان‌های تبلیغاتی (مترادف) */
        PROMOTION,
        
        /** اعلان‌های یادآوری */
        REMINDER
    }
    
    /**
     * سطوح اولویت اعلان‌ها
     * 
     * برای تعیین اهمیت و ترتیب نمایش اعلان‌ها
     */
    public enum NotificationPriority {
        /** اولویت پایین - اطلاعات عمومی */
        LOW,
        
        /** اولویت معمولی - اطلاعات استاندارد */
        NORMAL,
        
        /** اولویت متوسط - اطلاعات مهم */
        MEDIUM,
        
        /** اولویت بالا - اطلاعات فوری */
        HIGH,
        
        /** اولویت اضطراری - نیاز به توجه فوری */
        URGENT
    }

    // ==================== STANDARD METHODS ====================
    
    /**
     * مقایسه برابری دو اعلان بر اساس شناسه
     * 
     * @param o شیء برای مقایسه
     * @return true اگر شناسه‌ها برابر باشند
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    /**
     * محاسبه hash code بر اساس شناسه
     * 
     * @return hash code شیء
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * نمایش رشته‌ای اعلان
     * 
     * @return اطلاعات خلاصه اعلان
     */
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", priority=" + priority +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
} 
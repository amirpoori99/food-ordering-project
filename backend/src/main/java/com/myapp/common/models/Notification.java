package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, length = 500)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    @Column(nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    // Optional fields for linking to entities
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "restaurant_id") 
    private Long restaurantId;
    
    @Column(name = "delivery_id")
    private Long deliveryId;
    
    // Generic entity ID for linking to any entity
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // JSON data for extra information
    @Column(length = 1000)
    private String metadata;

    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Long userId, String title, String message, NotificationType type) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = NotificationPriority.NORMAL;
    }

    public Notification(Long userId, String title, String message, NotificationType type, NotificationPriority priority) {
        this(userId, title, message, type);
        this.priority = priority;
    }

    // Business logic methods
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
    
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }
    
    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }
    
    public boolean isExpired(int expirationDays) {
        return this.createdAt.plusDays(expirationDays).isBefore(LocalDateTime.now());
    }
    
    public boolean isHighPriority() {
        return this.priority == NotificationPriority.HIGH || this.priority == NotificationPriority.URGENT;
    }
    
    public boolean isOrderRelated() {
        return this.orderId != null;
    }
    
    public boolean isRestaurantRelated() {
        return this.restaurantId != null;
    }
    
    public boolean isDeliveryRelated() {
        return this.deliveryId != null;
    }

    // Static factory methods for common notification types
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public NotificationPriority getPriority() { return priority; }
    public void setPriority(NotificationPriority priority) { this.priority = priority; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    // Convenience methods
    public boolean isRead() { return Boolean.TRUE.equals(isRead); }
    public boolean isDeleted() { return Boolean.TRUE.equals(isDeleted); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    public Long getDeliveryId() { return deliveryId; }
    public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // Enums
    public enum NotificationType {
        ORDER_CREATED,
        ORDER_STATUS_CHANGED,
        ORDER_UPDATE,
        DELIVERY_ASSIGNED,
        DELIVERY_UPDATE, 
        PAYMENT_UPDATE,
        RESTAURANT_APPROVED,
        RESTAURANT_UPDATE,
        SYSTEM_MAINTENANCE,
        SYSTEM_UPDATE,
        PROMOTIONAL,
        PROMOTION,
        REMINDER
    }
    
    public enum NotificationPriority {
        LOW,
        NORMAL,
        MEDIUM,
        HIGH,
        URGENT
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

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
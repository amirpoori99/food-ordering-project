package com.myapp.common.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entity نمایندگی تحویل سفارش در سیستم سفارش غذا
 * 
 * این کلاس مدل کامل فرآیند تحویل سفارش شامل:
 * - اتصال یک‌به‌یک با Order
 * - اختصاص پیک (Courier)
 * - مدیریت وضعیت‌های مختلف تحویل
 * - ردیابی زمان‌های مختلف فرآیند
 * - محاسبه هزینه و مسافت تحویل
 * - validation قوانین کسب‌وکار
 * 
 * ویژگی‌های کلیدی:
 * - State Machine Pattern برای مدیریت وضعیت‌ها
 * - Business Logic Methods برای انتقال وضعیت
 * - Lazy Loading برای بهینه‌سازی performance
 * - Utility Methods برای محاسبات زمان
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "deliveries")
public class Delivery {
    
    /** شناسه یکتای تحویل */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 
     * سفارش مربوط به این تحویل (رابطه یک‌به‌یک)
     * هر سفارش فقط یک تحویل دارد
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    
    /** 
     * پیک اختصاص داده شده (رابطه چند‌به‌یک)
     * یک پیک می‌تواند چندین تحویل داشته باشد
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private User courier;
    
    /** وضعیت فعلی تحویل */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;
    
    /** زمان اختصاص پیک */
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    /** زمان تحویل گرفتن از رستوران */
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;
    
    /** زمان تحویل به مشتری */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    /** زمان تخمینی تحویل گرفتن از رستوران */
    @Column(name = "estimated_pickup_time")
    private LocalDateTime estimatedPickupTime;
    
    /** زمان تخمینی تحویل به مشتری */
    @Column(name = "estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;
    
    /** یادداشت‌های تحویل (توسط مشتری یا رستوران) */
    @Column(name = "delivery_notes", length = 1000)
    private String deliveryNotes;
    
    /** یادداشت‌های پیک */
    @Column(name = "courier_notes", length = 1000)
    private String courierNotes;
    
    /** هزینه تحویل */
    @Column(name = "delivery_fee", nullable = false)
    private Double deliveryFee;
    
    /** مسافت تحویل به کیلومتر */
    @Column(name = "distance_km")
    private Double distanceKm;
    
    // ==================== CONSTRUCTORS ====================
    
    /**
     * سازنده پیش‌فرض برای JPA
     */
    public Delivery() {
    }
    
    /**
     * سازنده برای ایجاد تحویل جدید
     * 
     * @param order سفارش مربوطه
     * @param deliveryFee هزینه تحویل
     * @throws IllegalArgumentException در صورت نامعتبر بودن ورودی‌ها
     */
    public Delivery(Order order, Double deliveryFee) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (deliveryFee == null || deliveryFee < 0) {
            throw new IllegalArgumentException("Delivery fee must be non-negative");
        }
        
        this.order = order;
        this.deliveryFee = deliveryFee;
        this.status = DeliveryStatus.PENDING;
        
        // تنظیم زمان‌های تخمینی پیش‌فرض
        this.estimatedPickupTime = LocalDateTime.now().plusMinutes(15); // 15 دقیقه برای آماده‌سازی
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(45); // 45 دقیقه کل
    }
    
    // ==================== FACTORY METHODS ====================
    
    /**
     * ایجاد تحویل جدید
     * 
     * @param order سفارش
     * @param deliveryFee هزینه تحویل
     * @return تحویل ایجاد شده
     */
    public static Delivery createNew(Order order, Double deliveryFee) {
        return new Delivery(order, deliveryFee);
    }
    
    /**
     * ایجاد تحویل جدید با مسافت مشخص
     * زمان‌های تخمینی بر اساس مسافت تنظیم می‌شوند
     * 
     * @param order سفارش
     * @param deliveryFee هزینه تحویل
     * @param distanceKm مسافت به کیلومتر
     * @return تحویل ایجاد شده
     */
    public static Delivery createWithDistance(Order order, Double deliveryFee, Double distanceKm) {
        Delivery delivery = new Delivery(order, deliveryFee);
        delivery.setDistanceKm(distanceKm);
        
        // تنظیم زمان‌های تخمینی بر اساس مسافت
        if (distanceKm != null) {
            int extraMinutes = (int) (distanceKm * 3); // 3 دقیقه به ازای هر کیلومتر
            delivery.setEstimatedPickupTime(LocalDateTime.now().plusMinutes(15));
            delivery.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30 + extraMinutes));
        }
        return delivery;
    }
    
    // ==================== BUSINESS METHODS ====================
    
    /**
     * اختصاص پیک به تحویل
     * 
     * این متد state transition از PENDING به ASSIGNED انجام می‌دهد
     * 
     * @param courier پیک مورد نظر
     * @throws IllegalArgumentException اگر پیک null باشد یا نقش COURIER نداشته باشد
     * @throws IllegalStateException اگر تحویل در وضعیت قابل اختصاص نباشد
     */
    public void assignToCourier(User courier) {
        if (courier == null) {
            throw new IllegalArgumentException("Courier cannot be null");
        }
        if (!User.Role.COURIER.equals(courier.getRole())) {
            throw new IllegalArgumentException("User must have COURIER role");
        }
        if (!canBeAssigned()) {
            throw new IllegalStateException("Can only assign courier to pending deliveries");
        }
        
        this.courier = courier;
        this.status = DeliveryStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
        
        // به‌روزرسانی زمان تخمینی تحویل گرفتن
        this.estimatedPickupTime = LocalDateTime.now().plusMinutes(10);
    }
    
    /**
     * علامت‌گذاری تحویل به عنوان "تحویل گرفته شده از رستوران"
     * 
     * State transition از ASSIGNED به PICKED_UP
     * وضعیت سفارش نیز به OUT_FOR_DELIVERY تغییر می‌کند
     * 
     * @throws IllegalStateException اگر شرایط لازم برقرار نباشد
     */
    public void markAsPickedUp() {
        if (!DeliveryStatus.ASSIGNED.equals(this.status)) {
            throw new IllegalStateException("Delivery must be assigned before pickup");
        }
        if (this.courier == null) {
            throw new IllegalStateException("No courier assigned");
        }
        
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
        
        // به‌روزرسانی زمان تخمینی تحویل به مشتری
        this.estimatedDeliveryTime = LocalDateTime.now().plusMinutes(20);
        
        // به‌روزرسانی وضعیت سفارش
        if (this.order != null) {
            this.order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        }
    }
    
    /**
     * علامت‌گذاری تحویل به عنوان "تحویل داده شده"
     * 
     * State transition از PICKED_UP به DELIVERED
     * وضعیت سفارش نیز به DELIVERED تغییر می‌کند
     * 
     * @throws IllegalStateException اگر تحویل از رستوران انجام نشده باشد
     */
    public void markAsDelivered() {
        if (!DeliveryStatus.PICKED_UP.equals(this.status)) {
            throw new IllegalStateException("Delivery must be picked up before marking as delivered");
        }
        
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        
        // به‌روزرسانی وضعیت سفارش و زمان تحویل واقعی
        if (this.order != null) {
            this.order.setStatus(OrderStatus.DELIVERED);
            this.order.setActualDeliveryTime(this.deliveredAt);
        }
    }
    
    /**
     * لغو تحویل
     * 
     * تحویل‌های انجام شده قابل لغو نیستند
     * در صورت لغو، پیک از تحویل حذف می‌شود
     * 
     * @param reason دلیل لغو
     * @throws IllegalStateException اگر تحویل انجام شده باشد
     */
    public void cancel(String reason) {
        if (DeliveryStatus.DELIVERED.equals(this.status)) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }
        
        this.status = DeliveryStatus.CANCELLED;
        this.deliveryNotes = reason;
        
        // حذف اختصاص پیک در صورت وجود
        if (!DeliveryStatus.PENDING.equals(this.status)) {
            this.courier = null;
            this.assignedAt = null;
        }
    }
    
    // ==================== STATE CHECKING METHODS ====================
    
    /**
     * بررسی قابلیت اختصاص پیک
     * 
     * @return true اگر تحویل در وضعیت PENDING باشد
     */
    public boolean canBeAssigned() {
        return DeliveryStatus.PENDING.equals(this.status);
    }
    
    /**
     * بررسی قابلیت تحویل گرفتن از رستوران
     * 
     * @return true اگر پیک اختصاص داده شده و وضعیت ASSIGNED باشد
     */
    public boolean canBePickedUp() {
        return DeliveryStatus.ASSIGNED.equals(this.status) && this.courier != null;
    }
    
    /**
     * بررسی قابلیت تحویل به مشتری
     * 
     * @return true اگر از رستوران تحویل گرفته شده باشد
     */
    public boolean canBeDelivered() {
        return DeliveryStatus.PICKED_UP.equals(this.status);
    }
    
    /**
     * بررسی فعال بودن تحویل
     * 
     * @return true اگر تحویل در یکی از وضعیت‌های فعال باشد
     */
    public boolean isActive() {
        return DeliveryStatus.PENDING.equals(this.status) ||
               DeliveryStatus.ASSIGNED.equals(this.status) ||
               DeliveryStatus.PICKED_UP.equals(this.status);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * محاسبه دقایق باقی‌مانده تا زمان تخمینی تحویل گرفتن
     * 
     * @return تعداد دقایق (منفی اگر زمان گذشته باشد)
     */
    public int getEstimatedPickupMinutes() {
        if (estimatedPickupTime == null) {
            return 0;
        }
        return (int) ChronoUnit.MINUTES.between(LocalDateTime.now(), estimatedPickupTime);
    }
    
    /**
     * محاسبه دقایق باقی‌مانده تا زمان تخمینی تحویل
     * 
     * @return تعداد دقایق (منفی اگر زمان گذشته باشد)
     */
    public int getEstimatedDeliveryMinutes() {
        if (estimatedDeliveryTime == null) {
            return 0;
        }
        return (int) ChronoUnit.MINUTES.between(LocalDateTime.now(), estimatedDeliveryTime);
    }
    
    // ==================== GETTERS AND SETTERS ====================
    
    /** @return شناسه تحویل */
    public Long getId() {
        return id;
    }
    
    /** @param id شناسه تحویل */
    public void setId(Long id) {
        this.id = id;
    }
    
    /** @return سفارش مربوطه */
    public Order getOrder() {
        return order;
    }
    
    /** @param order سفارش مربوطه */
    public void setOrder(Order order) {
        this.order = order;
    }
    
    /** @return پیک اختصاص داده شده */
    public User getCourier() {
        return courier;
    }
    
    /** @param courier پیک */
    public void setCourier(User courier) {
        this.courier = courier;
    }
    
    /** @return وضعیت تحویل */
    public DeliveryStatus getStatus() {
        return status;
    }
    
    /** @param status وضعیت تحویل */
    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }
    
    /** @return زمان اختصاص پیک */
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    /** @param assignedAt زمان اختصاص پیک */
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    /** @return زمان تحویل گرفتن از رستوران */
    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }
    
    /** @param pickedUpAt زمان تحویل گرفتن */
    public void setPickedUpAt(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }
    
    /** @return زمان تحویل به مشتری */
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
    
    /** @param deliveredAt زمان تحویل */
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
    
    /** @return زمان تخمینی تحویل گرفتن */
    public LocalDateTime getEstimatedPickupTime() {
        return estimatedPickupTime;
    }
    
    /** @param estimatedPickupTime زمان تخمینی تحویل گرفتن */
    public void setEstimatedPickupTime(LocalDateTime estimatedPickupTime) {
        this.estimatedPickupTime = estimatedPickupTime;
    }
    
    /** @return زمان تخمینی تحویل */
    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }
    
    /** @param estimatedDeliveryTime زمان تخمینی تحویل */
    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
    
    /** @return یادداشت‌های تحویل */
    public String getDeliveryNotes() {
        return deliveryNotes;
    }
    
    /** @param deliveryNotes یادداشت‌های تحویل */
    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }
    
    /** @return یادداشت‌های پیک */
    public String getCourierNotes() {
        return courierNotes;
    }
    
    /** @param courierNotes یادداشت‌های پیک */
    public void setCourierNotes(String courierNotes) {
        this.courierNotes = courierNotes;
    }
    
    /** @return هزینه تحویل */
    public Double getDeliveryFee() {
        return deliveryFee;
    }
    
    /** @param deliveryFee هزینه تحویل */
    public void setDeliveryFee(Double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
    
    /** @return مسافت تحویل */
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    /** @param distanceKm مسافت تحویل */
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    /**
     * نمایش رشته‌ای از تحویل برای debugging
     * 
     * @return خلاصه اطلاعات تحویل
     */
    @Override
    public String toString() {
        return "Delivery{" +
                "id=" + id +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", courierId=" + (courier != null ? courier.getId() : null) +
                ", status=" + status +
                ", deliveryFee=" + deliveryFee +
                '}';
    }
}
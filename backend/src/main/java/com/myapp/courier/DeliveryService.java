package com.myapp.courier;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.auth.AuthRepository;
import com.myapp.order.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * سرویس مدیریت عملیات تحویل سفارشات
 * 
 * این کلاس مسئول پیاده‌سازی منطق کسب‌وکار مربوط به تحویل شامل:
 * - ایجاد درخواست تحویل برای سفارشات
 * - اختصاص پیک به تحویل‌ها
 * - مدیریت مراحل مختلف تحویل (pickup, delivery)
 * - لغو و به‌روزرسانی تحویل‌ها
 * - محاسبه آمار و گزارش‌های پیک‌ها
 * - validation قوانین کسب‌وکار
 * 
 * ویژگی‌های کلیدی:
 * - State Management: مدیریت انتقال وضعیت‌ها
 * - Courier Availability: بررسی در دسترس بودن پیک‌ها
 * - Business Validation: اعتبارسنجی قوانین کسب‌وکار
 * - Statistics Calculation: محاسبه آمار عملکرد
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final AuthRepository authRepository;
    private final OrderRepository orderRepository;

    public DeliveryService(DeliveryRepository deliveryRepository, 
                          AuthRepository authRepository,
                          OrderRepository orderRepository) {
        this.deliveryRepository = deliveryRepository;
        this.authRepository = authRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new delivery request for an order
     */
    public Delivery createDelivery(Long orderId, Double estimatedFee) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (estimatedFee == null || estimatedFee < 0) {
            throw new IllegalArgumentException("Estimated fee cannot be null or negative");
        }

        // Verify order exists
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order", orderId));

        // Check if delivery already exists for this order
        Optional<Delivery> existingDelivery = deliveryRepository.findByOrderId(orderId);
        if (existingDelivery.isPresent()) {
            throw new IllegalStateException("Delivery already exists for order: " + orderId);
        }

        // Create new delivery
        Delivery delivery = new Delivery(order, estimatedFee);
        return deliveryRepository.save(delivery);
    }

    /**
     * Assigns a courier to a pending delivery
     */
    public Delivery assignCourier(Long deliveryId, Long courierId) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));

        User courier = authRepository.findById(courierId)
            .orElseThrow(() -> new NotFoundException("Courier", courierId));

        // Verify courier role
        if (courier.getRole() != User.Role.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        // Check if courier is available (not assigned to other active deliveries)
        List<Delivery> activeCourierDeliveries = deliveryRepository.findActiveByCourier(courierId);
        if (!activeCourierDeliveries.isEmpty()) {
            throw new IllegalStateException("Courier is already assigned to active deliveries");
        }

        // Assign courier using entity method
        delivery.assignToCourier(courier);
        return deliveryRepository.update(delivery);
    }

    /**
     * Marks delivery as picked up by courier
     */
    public Delivery markPickedUp(Long deliveryId, Long courierId) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));

        // Verify courier is assigned to this delivery
        if (delivery.getCourier() == null || !delivery.getCourier().getId().equals(courierId)) {
            throw new IllegalArgumentException("Courier is not assigned to this delivery");
        }

        delivery.markAsPickedUp();
        return deliveryRepository.update(delivery);
    }

    /**
     * Marks delivery as completed
     */
    public Delivery markDelivered(Long deliveryId, Long courierId) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));

        // Verify courier is assigned to this delivery
        if (delivery.getCourier() == null || !delivery.getCourier().getId().equals(courierId)) {
            throw new IllegalArgumentException("Courier is not assigned to this delivery");
        }

        delivery.markAsDelivered();
        
        // The order status is updated automatically in the entity method
        return deliveryRepository.update(delivery);
    }

    /**
     * Cancels a delivery
     */
    public Delivery cancelDelivery(Long deliveryId, String reason) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));

        delivery.cancel(reason != null ? reason : "No reason provided");
        return deliveryRepository.update(delivery);
    }

    /**
     * Gets delivery by ID
     */
    public Delivery getDelivery(Long deliveryId) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        return deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));
    }

    /**
     * Gets delivery by order ID
     */
    public Delivery getDeliveryByOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        return deliveryRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException("Delivery for order", orderId));
    }

    /**
     * Gets all pending deliveries available for assignment
     */
    public List<Delivery> getPendingDeliveries() {
        return deliveryRepository.findPendingDeliveries();
    }

    /**
     * Gets all active deliveries for a courier
     */
    public List<Delivery> getCourierActiveDeliveries(Long courierId) {
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }

        // Verify courier exists
        User courier = authRepository.findById(courierId)
            .orElseThrow(() -> new NotFoundException("Courier", courierId));

        if (courier.getRole() != User.Role.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        return deliveryRepository.findActiveByCourier(courierId);
    }

    /**
     * Gets delivery history for a courier
     */
    public List<Delivery> getCourierDeliveryHistory(Long courierId) {
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }

        // Verify courier exists
        User courier = authRepository.findById(courierId)
            .orElseThrow(() -> new NotFoundException("Courier", courierId));

        if (courier.getRole() != User.Role.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        return deliveryRepository.findByCourierId(courierId);
    }

    /**
     * Gets deliveries by status
     */
    public List<Delivery> getDeliveriesByStatus(DeliveryStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        return deliveryRepository.findByStatus(status);
    }

    /**
     * Gets courier statistics
     */
    public DeliveryRepository.CourierStatistics getCourierStatistics(Long courierId) {
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }

        // Verify courier exists
        User courier = authRepository.findById(courierId)
            .orElseThrow(() -> new NotFoundException("Courier", courierId));

        if (courier.getRole() != User.Role.COURIER) {
            throw new IllegalArgumentException("User is not a courier");
        }

        return deliveryRepository.getCourierStatistics(courierId);
    }

    /**
     * Checks if a courier is available for assignment
     */
    public boolean isCourierAvailable(Long courierId) {
        if (courierId == null) {
            return false;
        }

        List<Delivery> activeDeliveries = deliveryRepository.findActiveByCourier(courierId);
        return activeDeliveries.isEmpty();
    }

    /**
     * Gets all deliveries (admin function)
     */
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    /**
     * Gets active deliveries
     */
    public List<Delivery> getActiveDeliveries() {
        return deliveryRepository.findActiveDeliveries();
    }

    /**
     * Deletes a delivery (admin function - use with caution)
     */
    public void deleteDelivery(Long deliveryId) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));

        // Only allow deletion of cancelled deliveries
        if (delivery.getStatus() != DeliveryStatus.CANCELLED) {
            throw new IllegalStateException("Can only delete cancelled deliveries");
        }

        deliveryRepository.delete(deliveryId);
    }

    /**
     * Updates delivery status (admin function)
     */
    public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) {
        if (deliveryId == null) {
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new NotFoundException("Delivery", deliveryId));

        // Update status based on business rules
        switch (newStatus) {
            case PENDING:
                delivery.setStatus(DeliveryStatus.PENDING);
                break;
            case ASSIGNED:
                if (delivery.getStatus() != DeliveryStatus.PENDING) {
                    throw new IllegalStateException("Can only assign pending deliveries");
                }
                delivery.setStatus(DeliveryStatus.ASSIGNED);
                break;
            case PICKED_UP:
                if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
                    throw new IllegalStateException("Can only pick up assigned deliveries");
                }
                delivery.markAsPickedUp();
                break;
            case DELIVERED:
                if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
                    throw new IllegalStateException("Can only deliver picked up deliveries");
                }
                delivery.markAsDelivered();
                break;
            case CANCELLED:
                delivery.cancel("Admin cancelled");
                break;
            default:
                throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        return deliveryRepository.update(delivery);
    }

    /**
     * Count deliveries by courier and status
     */
    public Long countDeliveriesByCourierAndStatus(Long courierId, DeliveryStatus status) {
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        return deliveryRepository.countByCourierAndStatus(courierId, status);
    }

    /**
     * Get deliveries by courier and status
     */
    public List<Delivery> getDeliveriesByCourierAndStatus(Long courierId, DeliveryStatus status) {
        if (courierId == null) {
            throw new IllegalArgumentException("Courier ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        return deliveryRepository.findByCourierAndStatus(courierId, status);
    }

    /**
     * Get deliveries by date range
     */
    public List<Delivery> getDeliveriesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        return deliveryRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Check if delivery exists by order ID
     */
    public boolean deliveryExistsForOrder(Long orderId) {
        if (orderId == null) {
            return false;
        }
        return deliveryRepository.existsByOrderId(orderId);
    }
} 

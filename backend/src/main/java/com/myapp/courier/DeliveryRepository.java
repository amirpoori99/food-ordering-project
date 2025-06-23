package com.myapp.courier;

import com.myapp.common.models.Delivery;
import com.myapp.common.models.DeliveryStatus;
import com.myapp.common.models.Order;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DeliveryRepository - Data access layer for Delivery operations
 * Handles all database operations for delivery management
 */
public class DeliveryRepository {

    /**
     * Save a new delivery
     */
    public Delivery save(Delivery delivery) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(delivery);
            session.getTransaction().commit();
            return delivery;
        }
    }

    /**
     * Update an existing delivery
     */
    public Delivery update(Delivery delivery) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Delivery updated = session.merge(delivery);
            session.getTransaction().commit();
            return updated;
        }
    }

    /**
     * Find delivery by ID
     */
    public Optional<Delivery> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Delivery delivery = session.get(Delivery.class, id);
            return Optional.ofNullable(delivery);
        }
    }

    /**
     * Find delivery by order ID
     */
    public Optional<Delivery> findByOrderId(Long orderId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.order.id = :orderId", Delivery.class);
            query.setParameter("orderId", orderId);
            return query.uniqueResultOptional();
        }
    }

    /**
     * Find all deliveries for a specific courier
     */
    public List<Delivery> findByCourierId(Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.courier.id = :courierId ORDER BY d.assignedAt DESC", Delivery.class);
            query.setParameter("courierId", courierId);
            return query.getResultList();
        }
    }

    /**
     * Find deliveries by status
     */
    public List<Delivery> findByStatus(DeliveryStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.status = :status ORDER BY d.id DESC", Delivery.class);
            query.setParameter("status", status);
            return query.getResultList();
        }
    }

    /**
     * Find pending deliveries (waiting for courier assignment)
     */
    public List<Delivery> findPendingDeliveries() {
        return findByStatus(DeliveryStatus.PENDING);
    }

    /**
     * Find active deliveries for a courier (assigned or picked up)
     */
    public List<Delivery> findActiveByCourier(Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.courier.id = :courierId AND d.status IN (:statuses) ORDER BY d.assignedAt", 
                Delivery.class);
            query.setParameter("courierId", courierId);
            query.setParameter("statuses", List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP));
            return query.getResultList();
        }
    }

    /**
     * Find all active deliveries (not delivered or cancelled)
     */
    public List<Delivery> findActiveDeliveries() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.status IN (:statuses) ORDER BY d.id DESC", Delivery.class);
            query.setParameter("statuses", List.of(
                DeliveryStatus.PENDING, 
                DeliveryStatus.ASSIGNED, 
                DeliveryStatus.PICKED_UP
            ));
            return query.getResultList();
        }
    }

    /**
     * Find deliveries by date range
     */
    public List<Delivery> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.assignedAt BETWEEN :startDate AND :endDate ORDER BY d.assignedAt DESC", 
                Delivery.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        }
    }

    /**
     * Find courier deliveries by date range
     */
    public List<Delivery> findByCourierAndDateRange(Long courierId, LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.courier.id = :courierId AND d.assignedAt BETWEEN :startDate AND :endDate ORDER BY d.assignedAt DESC", 
                Delivery.class);
            query.setParameter("courierId", courierId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        }
    }

    /**
     * Find deliveries by courier and status
     */
    public List<Delivery> findByCourierAndStatus(Long courierId, DeliveryStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d WHERE d.courier.id = :courierId AND d.status = :status ORDER BY d.assignedAt DESC", 
                Delivery.class);
            query.setParameter("courierId", courierId);
            query.setParameter("status", status);
            return query.getResultList();
        }
    }

    /**
     * Count total deliveries for a courier
     */
    public Long countByCourier(Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(d) FROM Delivery d WHERE d.courier.id = :courierId", Long.class);
            query.setParameter("courierId", courierId);
            return query.uniqueResult();
        }
    }

    /**
     * Count deliveries by status for a courier
     */
    public Long countByCourierAndStatus(Long courierId, DeliveryStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(d) FROM Delivery d WHERE d.courier.id = :courierId AND d.status = :status", Long.class);
            query.setParameter("courierId", courierId);
            query.setParameter("status", status);
            return query.uniqueResult();
        }
    }

    /**
     * Check if delivery exists by ID
     */
    public boolean existsById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(d) FROM Delivery d WHERE d.id = :id", Long.class);
            query.setParameter("id", id);
            return query.uniqueResult() > 0;
        }
    }

    /**
     * Check if delivery exists for order
     */
    public boolean existsByOrderId(Long orderId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(d) FROM Delivery d WHERE d.order.id = :orderId", Long.class);
            query.setParameter("orderId", orderId);
            return query.uniqueResult() > 0;
        }
    }

    /**
     * Delete delivery by ID
     */
    public void delete(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Delivery delivery = session.get(Delivery.class, id);
            if (delivery != null) {
                session.remove(delivery);
            }
            session.getTransaction().commit();
        }
    }

    /**
     * Find all deliveries (for admin)
     */
    public List<Delivery> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d ORDER BY d.id DESC", Delivery.class);
            return query.getResultList();
        }
    }

    /**
     * Calculate average delivery time for a courier
     */
    public Double getAverageDeliveryTimeMinutes(Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // Calculate average time between pickup and delivery in minutes
            Query<Double> query = session.createQuery(
                "SELECT AVG(EXTRACT(EPOCH FROM (d.deliveredAt - d.pickedUpAt)) / 60) " +
                "FROM Delivery d WHERE d.courier.id = :courierId AND d.status = :delivered AND d.pickedUpAt IS NOT NULL AND d.deliveredAt IS NOT NULL", 
                Double.class);
            query.setParameter("courierId", courierId);
            query.setParameter("delivered", DeliveryStatus.DELIVERED);
            return query.uniqueResult();
        }
    }

    /**
     * Get delivery statistics for courier
     */
    public CourierStatistics getCourierStatistics(Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // Total deliveries
            Long totalDeliveries = countByCourier(courierId);
            
            // Completed deliveries
            Long completedDeliveries = countByCourierAndStatus(courierId, DeliveryStatus.DELIVERED);
            
            // Active deliveries
            Long activeDeliveries = countByCourierAndStatus(courierId, DeliveryStatus.ASSIGNED) +
                                   countByCourierAndStatus(courierId, DeliveryStatus.PICKED_UP);
            
            // Cancelled deliveries
            Long cancelledDeliveries = countByCourierAndStatus(courierId, DeliveryStatus.CANCELLED);
            
            // Average delivery time
            Double avgDeliveryTime = getAverageDeliveryTimeMinutes(courierId);
            
            // Total earnings (sum of delivery fees for completed deliveries)
            Query<Double> earningsQuery = session.createQuery(
                "SELECT COALESCE(SUM(d.deliveryFee), 0.0) FROM Delivery d WHERE d.courier.id = :courierId AND d.status = :delivered", 
                Double.class);
            earningsQuery.setParameter("courierId", courierId);
            earningsQuery.setParameter("delivered", DeliveryStatus.DELIVERED);
            Double totalEarnings = earningsQuery.uniqueResult();
            
            return new CourierStatistics(
                totalDeliveries,
                completedDeliveries,
                activeDeliveries,
                cancelledDeliveries,
                avgDeliveryTime != null ? avgDeliveryTime : 0.0,
                totalEarnings
            );
        }
    }

    /**
     * Inner class for courier statistics
     */
    public static class CourierStatistics {
        private final Long totalDeliveries;
        private final Long completedDeliveries;
        private final Long activeDeliveries;
        private final Long cancelledDeliveries;
        private final Double averageDeliveryTimeMinutes;
        private final Double totalEarnings;

        public CourierStatistics(Long totalDeliveries, Long completedDeliveries, Long activeDeliveries,
                               Long cancelledDeliveries, Double averageDeliveryTimeMinutes, Double totalEarnings) {
            this.totalDeliveries = totalDeliveries;
            this.completedDeliveries = completedDeliveries;
            this.activeDeliveries = activeDeliveries;
            this.cancelledDeliveries = cancelledDeliveries;
            this.averageDeliveryTimeMinutes = averageDeliveryTimeMinutes;
            this.totalEarnings = totalEarnings;
        }

        // Getters
        public Long getTotalDeliveries() { return totalDeliveries; }
        public Long getCompletedDeliveries() { return completedDeliveries; }
        public Long getActiveDeliveries() { return activeDeliveries; }
        public Long getCancelledDeliveries() { return cancelledDeliveries; }
        public Double getAverageDeliveryTimeMinutes() { return averageDeliveryTimeMinutes; }
        public Double getTotalEarnings() { return totalEarnings; }
        
        public Double getSuccessRate() {
            if (totalDeliveries == 0) return 0.0;
            return (completedDeliveries.doubleValue() / totalDeliveries.doubleValue()) * 100.0;
        }
    }
}
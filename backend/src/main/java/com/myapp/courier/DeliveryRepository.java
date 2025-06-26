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
 * Repository برای عملیات Entity تحویل
 * 
 * این کلاس مسئول تمام عملیات دیتابیس مربوط به مدیریت تحویل شامل:
 * - CRUD operations برای Delivery entity
 * - جستجوی پیشرفته بر اساس معیارهای مختلف
 * - محاسبه آمار و گزارش‌های پیک‌ها
 * - Query های بهینه‌سازی شده برای performance
 * - مدیریت روابط با Order و User entities
 * 
 * Pattern های استفاده شده:
 * - Repository Pattern: انتزاع لایه دسترسی به داده
 * - Query Optimization: استفاده از HQL برای کارایی بالا
 * - Aggregate Functions: محاسبه آمار پیچیده
 * - Session Management: مدیریت صحیح session های Hibernate
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class DeliveryRepository {

    /**
     * ذخیره تحویل جدید در دیتابیس
     * 
     * @param delivery تحویل جدید برای ذخیره
     * @return تحویل ذخیره شده با ID تولید شده
     */
    public Delivery save(Delivery delivery) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.persist(delivery); // JPA persist برای entity جدید
            session.getTransaction().commit();
            return delivery;
        }
    }

    /**
     * به‌روزرسانی تحویل موجود
     * 
     * @param delivery تحویل برای به‌روزرسانی
     * @return تحویل به‌روزرسانی شده
     */
    public Delivery update(Delivery delivery) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Delivery updated = session.merge(delivery); // merge برای به‌روزرسانی
            session.getTransaction().commit();
            return updated;
        }
    }

    /**
     * یافتن تحویل بر اساس شناسه
     * 
     * @param id شناسه تحویل
     * @return Optional حاوی تحویل یا خالی
     */
    public Optional<Delivery> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Delivery delivery = session.get(Delivery.class, id);
            return Optional.ofNullable(delivery);
        }
    }

    /**
     * یافتن تحویل بر اساس شناسه سفارش
     * 
     * هر سفارش فقط یک تحویل دارد (رابطه یک‌به‌یک)
     * 
     * @param orderId شناسه سفارش
     * @return Optional حاوی تحویل یا خالی
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
     * یافتن تمام تحویل‌های یک پیک خاص
     * 
     * نتایج بر اساس تاریخ اختصاص به صورت نزولی مرتب می‌شوند
     * 
     * @param courierId شناسه پیک
     * @return لیست تحویل‌های پیک
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
     * یافتن تحویل‌ها بر اساس وضعیت
     * 
     * @param status وضعیت تحویل (PENDING, ASSIGNED, PICKED_UP, DELIVERED, CANCELLED)
     * @return لیست تحویل‌های با وضعیت مشخص
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
     * یافتن تحویل‌های در انتظار (منتظر اختصاص پیک)
     * 
     * @return لیست تحویل‌های PENDING
     */
    public List<Delivery> findPendingDeliveries() {
        return findByStatus(DeliveryStatus.PENDING);
    }

    /**
     * یافتن تحویل‌های فعال یک پیک (اختصاص داده شده یا تحویل گرفته شده)
     * 
     * برای بررسی در دسترس بودن پیک استفاده می‌شود
     * 
     * @param courierId شناسه پیک
     * @return لیست تحویل‌های فعال پیک
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
     * یافتن تمام تحویل‌های فعال (هنوز تکمیل یا لغو نشده)
     * 
     * برای monitoring و مدیریت کلی سیستم استفاده می‌شود
     * 
     * @return لیست تحویل‌های فعال
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
     * یافتن تحویل‌ها در بازه زمانی مشخص
     * 
     * بر اساس زمان اختصاص پیک فیلتر می‌کند
     * 
     * @param startDate تاریخ شروع
     * @param endDate تاریخ پایان
     * @return لیست تحویل‌ها در بازه زمانی
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
     * یافتن تحویل‌های پیک در بازه زمانی مشخص
     * 
     * @param courierId شناسه پیک
     * @param startDate تاریخ شروع
     * @param endDate تاریخ پایان
     * @return لیست تحویل‌های پیک در بازه زمانی
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
     * یافتن تحویل‌های پیک با وضعیت خاص
     * 
     * @param courierId شناسه پیک
     * @param status وضعیت تحویل
     * @return لیست تحویل‌های فیلتر شده
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
     * شمارش کل تحویل‌های یک پیک
     * 
     * @param courierId شناسه پیک
     * @return تعداد کل تحویل‌ها
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
     * شمارش تحویل‌های پیک با وضعیت خاص
     * 
     * @param courierId شناسه پیک
     * @param status وضعیت تحویل
     * @return تعداد تحویل‌ها
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
     * بررسی وجود تحویل با شناسه مشخص
     * 
     * @param id شناسه تحویل
     * @return true اگر وجود داشته باشد
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
     * بررسی وجود تحویل برای سفارش
     * 
     * @param orderId شناسه سفارش
     * @return true اگر تحویل وجود داشته باشد
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
     * حذف تحویل بر اساس شناسه
     * 
     * توجه: عملیات حذف به ندرت استفاده می‌شود
     * 
     * @param id شناسه تحویل
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
     * یافتن تمام تحویل‌ها (برای مدیر سیستم)
     * 
     * @return لیست کامل تحویل‌ها
     */
    public List<Delivery> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Delivery> query = session.createQuery(
                "FROM Delivery d ORDER BY d.id DESC", Delivery.class);
            return query.getResultList();
        }
    }

    /**
     * محاسبه میانگین زمان تحویل برای پیک (بر حسب دقیقه)
     * 
     * زمان بین pickup و delivery محاسبه می‌شود
     * 
     * @param courierId شناسه پیک
     * @return میانگین زمان تحویل یا null
     */
    public Double getAverageDeliveryTimeMinutes(Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // محاسبه میانگین زمان بین pickup و delivery به دقیقه
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
     * دریافت آمار کامل پیک
     * 
     * شامل تعداد تحویل‌ها، درآمد، میانگین زمان و نرخ موفقیت
     * 
     * @param courierId شناسه پیک
     * @return آمار کامل پیک
     */
    public CourierStatistics getCourierStatistics(Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // تعداد کل تحویل‌ها
            Long totalDeliveries = countByCourier(courierId);
            
            // تحویل‌های تکمیل شده
            Long completedDeliveries = countByCourierAndStatus(courierId, DeliveryStatus.DELIVERED);
            
            // تحویل‌های فعال
            Long activeDeliveries = countByCourierAndStatus(courierId, DeliveryStatus.ASSIGNED) +
                                   countByCourierAndStatus(courierId, DeliveryStatus.PICKED_UP);
            
            // تحویل‌های لغو شده
            Long cancelledDeliveries = countByCourierAndStatus(courierId, DeliveryStatus.CANCELLED);
            
            // میانگین زمان تحویل
            Double avgDeliveryTime = getAverageDeliveryTimeMinutes(courierId);
            
            // کل درآمد (مجموع هزینه‌های تحویل موفق)
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
     * کلاس داخلی برای آمار پیک
     * 
     * حاوی تمام اطلاعات آماری مربوط به عملکرد پیک
     */
    public static class CourierStatistics {
        /** تعداد کل تحویل‌ها */
        private final Long totalDeliveries;
        
        /** تعداد تحویل‌های موفق */
        private final Long completedDeliveries;
        
        /** تعداد تحویل‌های فعال */
        private final Long activeDeliveries;
        
        /** تعداد تحویل‌های لغو شده */
        private final Long cancelledDeliveries;
        
        /** میانگین زمان تحویل (دقیقه) */
        private final Double averageDeliveryTimeMinutes;
        
        /** کل درآمد */
        private final Double totalEarnings;

        /**
         * سازنده آمار پیک
         */
        public CourierStatistics(Long totalDeliveries, Long completedDeliveries, Long activeDeliveries,
                               Long cancelledDeliveries, Double averageDeliveryTimeMinutes, Double totalEarnings) {
            this.totalDeliveries = totalDeliveries;
            this.completedDeliveries = completedDeliveries;
            this.activeDeliveries = activeDeliveries;
            this.cancelledDeliveries = cancelledDeliveries;
            this.averageDeliveryTimeMinutes = averageDeliveryTimeMinutes;
            this.totalEarnings = totalEarnings;
        }

        // ==================== GETTERS ====================
        
        /** @return تعداد کل تحویل‌ها */
        public Long getTotalDeliveries() { return totalDeliveries; }
        
        /** @return تعداد تحویل‌های موفق */
        public Long getCompletedDeliveries() { return completedDeliveries; }
        
        /** @return تعداد تحویل‌های فعال */
        public Long getActiveDeliveries() { return activeDeliveries; }
        
        /** @return تعداد تحویل‌های لغو شده */
        public Long getCancelledDeliveries() { return cancelledDeliveries; }
        
        /** @return میانگین زمان تحویل */
        public Double getAverageDeliveryTimeMinutes() { return averageDeliveryTimeMinutes; }
        
        /** @return کل درآمد */
        public Double getTotalEarnings() { return totalEarnings; }
        
        /**
         * محاسبه نرخ موفقیت پیک
         * 
         * @return درصد موفقیت (0-100)
         */
        public Double getSuccessRate() {
            if (totalDeliveries == 0) return 0.0;
            return (completedDeliveries.doubleValue() / totalDeliveries.doubleValue()) * 100.0;
        }
    }
}
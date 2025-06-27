package com.myapp.admin;

import com.myapp.common.models.*;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Repository لایه دسترسی داده برای عملیات پنل مدیریت (Admin Dashboard)
 * 
 * این کلاس تمام عملیات پایگاه داده مربوط به مدیریت سیستم سفارش غذا را ارائه می‌دهد:
 * 
 * === مدیریت کاربران (User Management) ===
 * - getAllUsers(): دریافت تمام کاربران با فیلتر و جستجو
 * - countUsers(): شمارش کاربران با فیلتر
 * - getUserStatsByRole(): آمار کاربران بر اساس نقش
 * - updateUserStatus(): فعال/غیرفعال کردن کاربر
 * 
 * === مدیریت رستوران‌ها (Restaurant Management) ===
 * - getAllRestaurants(): دریافت تمام رستوران‌ها با فیلتر
 * - countRestaurants(): شمارش رستوران‌ها
 * - getRestaurantStatsByStatus(): آمار رستوران‌ها بر اساس وضعیت
 * 
 * === مدیریت سفارشات (Order Management) ===
 * - getAllOrders(): دریافت تمام سفارشات با فیلترهای پیشرفته
 * - countOrders(): شمارش سفارشات
 * - getOrderStatsByStatus(): آمار سفارشات بر اساس وضعیت
 * 
 * === مدیریت تراکنش‌ها (Transaction Management) ===
 * - getAllTransactions(): دریافت تمام تراکنش‌ها با فیلتر
 * - countTransactions(): شمارش تراکنش‌ها
 * 
 * === مدیریت تحویل (Delivery Management) ===
 * - getAllDeliveries(): دریافت تمام تحویل‌ها با فیلتر
 * - countDeliveries(): شمارش تحویل‌ها
 * 
 * === آمار سیستم (System Statistics) ===
 * - getSystemStatistics(): آمار کلی سیستم
 * - getDailyStatistics(): آمار روزانه
 * 
 * === ویژگی‌های کلیدی ===
 * - Advanced Filtering: فیلترهای پیشرفته برای همه entities
 * - Search Functionality: جستجوی متنی در تمام بخش‌ها
 * - Pagination Support: پشتیبانی از صفحه‌بندی
 * - Statistical Queries: queries آماری پیچیده
 * - Dynamic Query Building: ساخت پویای queries
 * - Performance Optimization: بهینه‌سازی کارایی queries
 * - Comprehensive Admin Operations: عملیات جامع مدیریتی
 * - Real-time Statistics: آمار real-time سیستم
 * 
 * === Inner Classes ===
 * - SystemStatistics: کلاس آمار کلی سیستم
 * - DailyStatistics: کلاس آمار روزانه
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class AdminRepository {

    // ==================== مدیریت کاربران (USER MANAGEMENT) ====================
    
    /**
     * دریافت تمام کاربران با فیلتر و جستجوی پیشرفته
     * 
     * این متد امکان جستجو و فیلتر کاربران را بر اساس معیارهای مختلف فراهم می‌کند:
     * - جستجو در نام، ایمیل و شماره تلفن
     * - فیلتر بر اساس نقش کاربر
     * - صفحه‌بندی برای بهبود عملکرد
     * 
     * @param searchTerm عبارت جستجو (در نام، ایمیل، تلفن)
     * @param role نقش کاربر برای فیلتر (اختیاری)
     * @param limit تعداد رکوردها در هر صفحه (0 = نامحدود)
     * @param offset شروع از رکورد (برای pagination)
     * @return لیست کاربران فیلتر شده
     */
    public List<User> getAllUsers(String searchTerm, User.Role role, int limit, int offset) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM User u WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(u.fullName) LIKE :search OR LOWER(u.email) LIKE :search OR u.phone LIKE :search)");
            }
            
            if (role != null) {
                hql.append(" AND u.role = :role");
            }
            
            hql.append(" ORDER BY u.id DESC");
            
            Query<User> query = session.createQuery(hql.toString(), User.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (role != null) {
                query.setParameter("role", role);
            }
            
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            if (offset > 0) {
                query.setFirstResult(offset);
            }
            
            return query.list();
        }
    }
    
    /**
     * شمارش کل کاربران با فیلترهای اعمال شده
     * 
     * برای محاسبه pagination و آمارگیری
     * 
     * @param searchTerm عبارت جستجو
     * @param role نقش کاربر
     * @return تعداد کل کاربران
     */
    public Long countUsers(String searchTerm, User.Role role) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(u) FROM User u WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(u.fullName) LIKE :search OR LOWER(u.email) LIKE :search OR u.phone LIKE :search)");
            }
            
            if (role != null) {
                hql.append(" AND u.role = :role");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (role != null) {
                query.setParameter("role", role);
            }
            
            return query.uniqueResult();
        }
    }
    
    /**
     * دریافت آمار کاربران بر اساس نقش
     * 
     * برای نمایش نمودار توزیع کاربران در پنل مدیریت
     * 
     * @return Map حاوی تعداد کاربران هر نقش
     */
    public Map<User.Role, Long> getUserStatsByRole() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                "SELECT u.role, COUNT(u) FROM User u GROUP BY u.role", Object[].class);
            
            List<Object[]> results = query.list();
            Map<User.Role, Long> stats = new HashMap<>();
            
            for (Object[] result : results) {
                User.Role role = (User.Role) result[0];
                Long count = (Long) result[1];
                stats.put(role, count);
            }
            
            return stats;
        }
    }
    
    /**
     * فعال/غیرفعال کردن کاربر
     * 
     * مدیران می‌توانند کاربران را مسدود یا فعال کنند
     * 
     * @param userId شناسه کاربر
     * @param isActive وضعیت فعال/غیرفعال
     */
    public void updateUserStatus(Long userId, boolean isActive) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setIsActive(isActive);
                session.merge(user);
            }
            
            session.getTransaction().commit();
        }
    }

    // ==================== مدیریت رستوران‌ها (RESTAURANT MANAGEMENT) ====================
    
    /**
     * دریافت تمام رستوران‌ها با فیلتر و جستجو
     * 
     * @param searchTerm عبارت جستجو (در نام و آدرس رستوران)
     * @param status وضعیت رستوران برای فیلتر
     * @param limit تعداد رکوردها در هر صفحه
     * @param offset شروع از رکورد
     * @return لیست رستوران‌های فیلتر شده
     */
    public List<Restaurant> getAllRestaurants(String searchTerm, RestaurantStatus status, int limit, int offset) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Restaurant r WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(r.name) LIKE :search OR LOWER(r.address) LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND r.status = :status");
            }
            
            hql.append(" ORDER BY r.id DESC");
            
            Query<Restaurant> query = session.createQuery(hql.toString(), Restaurant.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            if (offset > 0) {
                query.setFirstResult(offset);
            }
            
            return query.list();
        }
    }
    
    /**
     * شمارش رستوران‌ها با فیلترهای اعمال شده
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت رستوران
     * @return تعداد کل رستوران‌ها
     */
    public Long countRestaurants(String searchTerm, RestaurantStatus status) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(r) FROM Restaurant r WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(r.name) LIKE :search OR LOWER(r.address) LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND r.status = :status");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            return query.uniqueResult();
        }
    }
    
    /**
     * دریافت آمار رستوران‌ها بر اساس وضعیت
     * 
     * برای نمایش نمودار توزیع وضعیت رستوران‌ها
     * 
     * @return Map حاوی تعداد رستوران‌های هر وضعیت
     */
    public Map<RestaurantStatus, Long> getRestaurantStatsByStatus() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                "SELECT r.status, COUNT(r) FROM Restaurant r GROUP BY r.status", Object[].class);
            
            List<Object[]> results = query.list();
            Map<RestaurantStatus, Long> stats = new HashMap<>();
            
            for (Object[] result : results) {
                RestaurantStatus status = (RestaurantStatus) result[0];
                Long count = (Long) result[1];
                stats.put(status, count);
            }
            
            return stats;
        }
    }

    // ==================== مدیریت سفارشات (ORDER MANAGEMENT) ====================
    
    /**
     * دریافت تمام سفارشات با فیلترهای پیشرفته
     * 
     * امکان فیلتر پیچیده سفارشات بر اساس:
     * - جستجو در آدرس تحویل و شماره تلفن
     * - وضعیت سفارش
     * - کاربر مشخص
     * - رستوران مشخص
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت سفارش
     * @param customerId شناسه مشتری (اختیاری)
     * @param restaurantId شناسه رستوران (اختیاری)
     * @param limit تعداد رکوردها
     * @param offset شروع از رکورد
     * @return لیست سفارشات فیلتر شده
     */
    public List<Order> getAllOrders(String searchTerm, OrderStatus status, Long customerId, Long restaurantId, int limit, int offset) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Order o WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(o.deliveryAddress) LIKE :search OR o.phone LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND o.status = :status");
            }
            
            if (customerId != null) {
                hql.append(" AND o.customer.id = :customerId");
            }
            
            if (restaurantId != null) {
                hql.append(" AND o.restaurant.id = :restaurantId");
            }
            
            hql.append(" ORDER BY o.orderDate DESC");
            
            Query<Order> query = session.createQuery(hql.toString(), Order.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (customerId != null) {
                query.setParameter("customerId", customerId);
            }
            
            if (restaurantId != null) {
                query.setParameter("restaurantId", restaurantId);
            }
            
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            if (offset > 0) {
                query.setFirstResult(offset);
            }
            
            return query.list();
        }
    }
    
    /**
     * شمارش سفارشات با فیلترهای اعمال شده
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت سفارش
     * @param customerId شناسه مشتری
     * @param restaurantId شناسه رستوران
     * @return تعداد کل سفارشات
     */
    public Long countOrders(String searchTerm, OrderStatus status, Long customerId, Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(o) FROM Order o WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(o.deliveryAddress) LIKE :search OR o.phone LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND o.status = :status");
            }
            
            if (customerId != null) {
                hql.append(" AND o.customer.id = :customerId");
            }
            
            if (restaurantId != null) {
                hql.append(" AND o.restaurant.id = :restaurantId");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (customerId != null) {
                query.setParameter("customerId", customerId);
            }
            
            if (restaurantId != null) {
                query.setParameter("restaurantId", restaurantId);
            }
            
            return query.uniqueResult();
        }
    }
    
    /**
     * دریافت آمار سفارشات بر اساس وضعیت
     * 
     * برای نمایش نمودار وضعیت سفارشات در dashboard
     * 
     * @return Map حاوی تعداد سفارشات هر وضعیت
     */
    public Map<OrderStatus, Long> getOrderStatsByStatus() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Object[]> query = session.createQuery(
                "SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status", Object[].class);
            
            List<Object[]> results = query.list();
            Map<OrderStatus, Long> stats = new HashMap<>();
            
            for (Object[] result : results) {
                OrderStatus status = (OrderStatus) result[0];
                Long count = (Long) result[1];
                stats.put(status, count);
            }
            
            return stats;
        }
    }

    // ==================== مدیریت تراکنش‌ها (TRANSACTION MANAGEMENT) ====================
    
    /**
     * دریافت تمام تراکنش‌ها با فیلترهای پیشرفته
     * 
     * امکان جستجو و فیلتر تراکنش‌ها بر اساس:
     * - شناسه مرجع، توضیحات و روش پرداخت
     * - وضعیت تراکنش
     * - نوع تراکنش
     * - کاربر مشخص
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت تراکنش
     * @param type نوع تراکنش
     * @param userId شناسه کاربر
     * @param limit تعداد رکوردها
     * @param offset شروع از رکورد
     * @return لیست تراکنش‌های فیلتر شده
     */
    public List<Transaction> getAllTransactions(String searchTerm, TransactionStatus status, TransactionType type, Long userId, int limit, int offset) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Transaction t WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (t.referenceId LIKE :search OR LOWER(t.description) LIKE :search OR t.paymentMethod LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND t.status = :status");
            }
            
            if (type != null) {
                hql.append(" AND t.type = :type");
            }
            
            if (userId != null) {
                hql.append(" AND t.userId = :userId");
            }
            
            hql.append(" ORDER BY t.createdAt DESC");
            
            Query<Transaction> query = session.createQuery(hql.toString(), Transaction.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (type != null) {
                query.setParameter("type", type);
            }
            
            if (userId != null) {
                query.setParameter("userId", userId);
            }
            
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            if (offset > 0) {
                query.setFirstResult(offset);
            }
            
            return query.list();
        }
    }
    
    /**
     * شمارش تراکنش‌ها با فیلترهای اعمال شده
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت تراکنش
     * @param type نوع تراکنش
     * @param userId شناسه کاربر
     * @return تعداد کل تراکنش‌ها
     */
    public Long countTransactions(String searchTerm, TransactionStatus status, TransactionType type, Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(t) FROM Transaction t WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (t.referenceId LIKE :search OR LOWER(t.description) LIKE :search OR t.paymentMethod LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND t.status = :status");
            }
            
            if (type != null) {
                hql.append(" AND t.type = :type");
            }
            
            if (userId != null) {
                hql.append(" AND t.userId = :userId");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (type != null) {
                query.setParameter("type", type);
            }
            
            if (userId != null) {
                query.setParameter("userId", userId);
            }
            
            return query.uniqueResult();
        }
    }

    // ==================== مدیریت تحویل (DELIVERY MANAGEMENT) ====================
    
    /**
     * دریافت تمام تحویل‌ها با فیلتر و جستجو
     * 
     * @param searchTerm عبارت جستجو (در یادداشت‌های تحویل)
     * @param status وضعیت تحویل
     * @param courierId شناسه پیک
     * @param limit تعداد رکوردها
     * @param offset شروع از رکورد
     * @return لیست تحویل‌های فیلتر شده
     */
    public List<Delivery> getAllDeliveries(String searchTerm, DeliveryStatus status, Long courierId, int limit, int offset) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Delivery d WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(d.deliveryNotes) LIKE :search OR LOWER(d.courierNotes) LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND d.status = :status");
            }
            
            if (courierId != null) {
                hql.append(" AND d.courier.id = :courierId");
            }
            
            hql.append(" ORDER BY d.id DESC");
            
            Query<Delivery> query = session.createQuery(hql.toString(), Delivery.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (courierId != null) {
                query.setParameter("courierId", courierId);
            }
            
            if (limit > 0) {
                query.setMaxResults(limit);
            }
            if (offset > 0) {
                query.setFirstResult(offset);
            }
            
            return query.list();
        }
    }
    
    /**
     * شمارش تحویل‌ها با فیلترهای اعمال شده
     * 
     * @param searchTerm عبارت جستجو
     * @param status وضعیت تحویل
     * @param courierId شناسه پیک
     * @return تعداد کل تحویل‌ها
     */
    public Long countDeliveries(String searchTerm, DeliveryStatus status, Long courierId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(d) FROM Delivery d WHERE 1=1");
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hql.append(" AND (LOWER(d.deliveryNotes) LIKE :search OR LOWER(d.courierNotes) LIKE :search)");
            }
            
            if (status != null) {
                hql.append(" AND d.status = :status");
            }
            
            if (courierId != null) {
                hql.append(" AND d.courier.id = :courierId");
            }
            
            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                query.setParameter("search", searchPattern);
            }
            
            if (status != null) {
                query.setParameter("status", status);
            }
            
            if (courierId != null) {
                query.setParameter("courierId", courierId);
            }
            
            return query.uniqueResult();
        }
    }

    // ==================== آمار سیستم (SYSTEM STATISTICS) ====================
    
    /**
     * دریافت آمار کلی سیستم
     * 
     * این متد امکان دریافت آمار کلی سیستم را برای مدیریت پنل مدیریت فراهم می‌کند
     * 
     * @return آمار کلی سیستم
     */
    public SystemStatistics getSystemStatistics() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // Total counts
            Long totalUsers = session.createQuery("SELECT COUNT(u) FROM User u", Long.class).uniqueResult();
            Long totalRestaurants = session.createQuery("SELECT COUNT(r) FROM Restaurant r", Long.class).uniqueResult();
            Long totalOrders = session.createQuery("SELECT COUNT(o) FROM Order o", Long.class).uniqueResult();
            Long totalDeliveries = session.createQuery("SELECT COUNT(d) FROM Delivery d", Long.class).uniqueResult();
            
            // Revenue statistics  
            Double totalRevenue = session.createQuery("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = :paymentType AND t.status = :completedStatus", Double.class)
                .setParameter("paymentType", TransactionType.PAYMENT)
                .setParameter("completedStatus", TransactionStatus.COMPLETED).uniqueResult();
            Double totalRefunds = session.createQuery("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = :refundType AND t.status = :completedStatus", Double.class)
                .setParameter("refundType", TransactionType.REFUND)
                .setParameter("completedStatus", TransactionStatus.COMPLETED).uniqueResult();
            
            // Today's statistics
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            Long todayOrders = session.createQuery("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay", Long.class)
                .setParameter("startOfDay", startOfDay).uniqueResult();
            Double todayRevenue = session.createQuery("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = :paymentType AND t.status = :completedStatus AND t.createdAt >= :startOfDay", Double.class)
                .setParameter("paymentType", TransactionType.PAYMENT)
                .setParameter("completedStatus", TransactionStatus.COMPLETED)
                .setParameter("startOfDay", startOfDay).uniqueResult();
            
            // Active counts
            Long activeRestaurants = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.status = 'APPROVED'", Long.class).uniqueResult();
            Long pendingOrders = session.createQuery("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'", Long.class).uniqueResult();
            Long activeDeliveries = session.createQuery("SELECT COUNT(d) FROM Delivery d WHERE d.status IN ('PENDING', 'ASSIGNED', 'PICKED_UP')", Long.class).uniqueResult();
            
            return new SystemStatistics(
                totalUsers, totalRestaurants, totalOrders, totalDeliveries,
                totalRevenue, totalRefunds, todayOrders, todayRevenue,
                activeRestaurants, pendingOrders, activeDeliveries
            );
        }
    }
    
    /**
     * دریافت آمار روزانه
     * 
     * این متد امکان دریافت آمار روزانه سیستم را برای مدیریت پنل مدیریت فراهم می‌کند
     * 
     * @param days تعداد روزهای گذشته برای دریافت آمار
     * @return آمار روزانه
     */
    public List<DailyStatistics> getDailyStatistics(int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            Query<Object[]> query = session.createQuery(
                "SELECT DATE(o.orderDate), COUNT(o), COALESCE(SUM(o.totalAmount), 0.0) " +
                "FROM Order o WHERE o.orderDate >= :startDate " +
                "GROUP BY DATE(o.orderDate) " +
                "ORDER BY DATE(o.orderDate) DESC", Object[].class);
            query.setParameter("startDate", startDate);
            
            List<Object[]> results = query.list();
            return results.stream()
                .map(result -> new DailyStatistics(
                    (java.sql.Date) result[0],
                    (Long) result[1],
                    (Double) result[2]
                ))
                .toList();
        }
    }

    // ==================== HELPER CLASSES ====================
    
    /**
     * کلاس آمار کلی سیستم
     */
    public static class SystemStatistics {
        private final Long totalUsers;
        private final Long totalRestaurants;
        private final Long totalOrders;
        private final Long totalDeliveries;
        private final Double totalRevenue;
        private final Double totalRefunds;
        private final Long todayOrders;
        private final Double todayRevenue;
        private final Long activeRestaurants;
        private final Long pendingOrders;
        private final Long activeDeliveries;
        
        public SystemStatistics(Long totalUsers, Long totalRestaurants, Long totalOrders, Long totalDeliveries,
                              Double totalRevenue, Double totalRefunds, Long todayOrders, Double todayRevenue,
                              Long activeRestaurants, Long pendingOrders, Long activeDeliveries) {
            this.totalUsers = totalUsers;
            this.totalRestaurants = totalRestaurants;
            this.totalOrders = totalOrders;
            this.totalDeliveries = totalDeliveries;
            this.totalRevenue = totalRevenue;
            this.totalRefunds = totalRefunds;
            this.todayOrders = todayOrders;
            this.todayRevenue = todayRevenue;
            this.activeRestaurants = activeRestaurants;
            this.pendingOrders = pendingOrders;
            this.activeDeliveries = activeDeliveries;
        }
        
        // Getters
        public Long getTotalUsers() { return totalUsers; }
        public Long getTotalRestaurants() { return totalRestaurants; }
        public Long getTotalOrders() { return totalOrders; }
        public Long getTotalDeliveries() { return totalDeliveries; }
        public Double getTotalRevenue() { return totalRevenue; }
        public Double getTotalRefunds() { return totalRefunds; }
        public Long getTodayOrders() { return todayOrders; }
        public Double getTodayRevenue() { return todayRevenue; }
        public Long getActiveRestaurants() { return activeRestaurants; }
        public Long getPendingOrders() { return pendingOrders; }
        public Long getActiveDeliveries() { return activeDeliveries; }
    }
    
    /**
     * کلاس آمار روزانه
     */
    public static class DailyStatistics {
        private final java.sql.Date date;
        private final Long orderCount;
        private final Double revenue;
        
        public DailyStatistics(java.sql.Date date, Long orderCount, Double revenue) {
            this.date = date;
            this.orderCount = orderCount;
            this.revenue = revenue;
        }
        
        // Getters
        public java.sql.Date getDate() { return date; }
        public Long getOrderCount() { return orderCount; }
        public Double getRevenue() { return revenue; }
    }
}
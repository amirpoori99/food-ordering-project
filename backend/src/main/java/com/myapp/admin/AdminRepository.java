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
 * Repository for Admin Dashboard Operations
 * Provides comprehensive admin functionality for managing the food ordering system
 */
public class AdminRepository {

    // ==================== USER MANAGEMENT ====================
    
    /**
     * Get all users with optional filtering
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
     * Count total users with filtering
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
     * Get user statistics by role
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
     * Block/Unblock user
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

    // ==================== RESTAURANT MANAGEMENT ====================
    
    /**
     * Get all restaurants with filtering
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
     * Count restaurants with filtering
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
     * Get restaurant statistics by status
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

    // ==================== ORDER MANAGEMENT ====================
    
    /**
     * Get all orders with filtering
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
     * Count orders with filtering
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
     * Get order statistics by status
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

    // ==================== TRANSACTION MANAGEMENT ====================
    
    /**
     * Get all transactions with filtering
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
     * Count transactions with filtering
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

    // ==================== DELIVERY MANAGEMENT ====================
    
    /**
     * Get all deliveries with filtering
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
     * Count deliveries with filtering
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

    // ==================== SYSTEM STATISTICS ====================
    
    /**
     * Get comprehensive system statistics
     */
    public SystemStatistics getSystemStatistics() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            // Total counts
            Long totalUsers = session.createQuery("SELECT COUNT(u) FROM User u", Long.class).uniqueResult();
            Long totalRestaurants = session.createQuery("SELECT COUNT(r) FROM Restaurant r", Long.class).uniqueResult();
            Long totalOrders = session.createQuery("SELECT COUNT(o) FROM Order o", Long.class).uniqueResult();
            Long totalDeliveries = session.createQuery("SELECT COUNT(d) FROM Delivery d", Long.class).uniqueResult();
            
            // Revenue statistics
            Double totalRevenue = session.createQuery("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = 'PAYMENT' AND t.status = 'COMPLETED'", Double.class).uniqueResult();
            Double totalRefunds = session.createQuery("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = 'REFUND' AND t.status = 'COMPLETED'", Double.class).uniqueResult();
            
            // Today's statistics
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            Long todayOrders = session.createQuery("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startOfDay", Long.class)
                .setParameter("startOfDay", startOfDay).uniqueResult();
            Double todayRevenue = session.createQuery("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = 'PAYMENT' AND t.status = 'COMPLETED' AND t.createdAt >= :startOfDay", Double.class)
                .setParameter("startOfDay", startOfDay).uniqueResult();
            
            // Active counts
            Long activeRestaurants = session.createQuery("SELECT COUNT(r) FROM Restaurant r WHERE r.status = 'ACTIVE'", Long.class).uniqueResult();
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
     * Get daily statistics for the last N days
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
     * System statistics data class
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
     * Daily statistics data class
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
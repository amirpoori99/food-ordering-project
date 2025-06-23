package com.myapp.coupon;

import com.myapp.common.models.Coupon;
import com.myapp.common.models.Restaurant;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for Coupon data access operations
 * Handles all database operations for coupons
 */
public class CouponRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(CouponRepository.class);
    
    // ==================== BASIC CRUD OPERATIONS ====================
    
    /**
     * Saves a new coupon to the database
     */
    public Coupon save(Coupon coupon) {
        Transaction tx = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(coupon);
            tx.commit();
            logger.info("Saved new coupon with ID: {}", coupon.getId());
            return coupon;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error saving coupon: {}", e.getMessage());
            throw new RuntimeException("Failed to save coupon", e);
        }
    }
    
    /**
     * Updates an existing coupon
     */
    public Coupon update(Coupon coupon) {
        Transaction tx = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            coupon.setUpdatedAt(LocalDateTime.now());
            session.update(coupon);
            tx.commit();
            logger.info("Updated coupon with ID: {}", coupon.getId());
            return coupon;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating coupon: {}", e.getMessage());
            throw new RuntimeException("Failed to update coupon", e);
        }
    }
    
    /**
     * Finds a coupon by ID
     */
    public Optional<Coupon> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Coupon coupon = session.get(Coupon.class, id);
            return Optional.ofNullable(coupon);
        } catch (Exception e) {
            logger.error("Error finding coupon by ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to find coupon", e);
        }
    }
    
    /**
     * Finds a coupon by code
     */
    public Optional<Coupon> findByCode(String code) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.code = :code", Coupon.class);
            query.setParameter("code", code);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Error finding coupon by code {}: {}", code, e.getMessage());
            throw new RuntimeException("Failed to find coupon", e);
        }
    }
    
    /**
     * Checks if a coupon code exists
     */
    public boolean existsByCode(String code) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(c) FROM Coupon c WHERE c.code = :code", Long.class);
            query.setParameter("code", code);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            logger.error("Error checking coupon existence by code {}: {}", code, e.getMessage());
            throw new RuntimeException("Failed to check coupon existence", e);
        }
    }
    
    /**
     * Deletes a coupon by ID
     */
    public boolean delete(Long id) {
        Transaction tx = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Coupon coupon = session.get(Coupon.class, id);
            if (coupon != null) {
                session.delete(coupon);
                tx.commit();
                logger.info("Deleted coupon with ID: {}", id);
                return true;
            }
            tx.commit();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error deleting coupon with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete coupon", e);
        }
    }
    
    // ==================== QUERY METHODS ====================
    
    /**
     * Gets all active coupons
     */
    public List<Coupon> findActiveCoupons() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.isActive = true ORDER BY c.createdAt DESC", Coupon.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding active coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to find active coupons", e);
        }
    }
    
    /**
     * Gets all valid coupons (active and within date range)
     */
    public List<Coupon> findValidCoupons() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime now = LocalDateTime.now();
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.isActive = true " +
                "AND c.validFrom <= :now AND c.validUntil > :now " +
                "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit) " +
                "ORDER BY c.createdAt DESC", Coupon.class);
            query.setParameter("now", now);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding valid coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to find valid coupons", e);
        }
    }
    
    /**
     * Gets coupons by restaurant (null restaurant means global coupons)
     */
    public List<Coupon> findByRestaurant(Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query;
            if (restaurant == null) {
                query = session.createQuery(
                    "FROM Coupon c WHERE c.restaurant IS NULL ORDER BY c.createdAt DESC", Coupon.class);
            } else {
                query = session.createQuery(
                    "FROM Coupon c WHERE c.restaurant = :restaurant ORDER BY c.createdAt DESC", Coupon.class);
                query.setParameter("restaurant", restaurant);
            }
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding coupons by restaurant: {}", e.getMessage());
            throw new RuntimeException("Failed to find coupons by restaurant", e);
        }
    }
    
    /**
     * Gets coupons by restaurant ID
     */
    public List<Coupon> findByRestaurantId(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query;
            if (restaurantId == null) {
                query = session.createQuery(
                    "FROM Coupon c WHERE c.restaurant IS NULL ORDER BY c.createdAt DESC", Coupon.class);
            } else {
                query = session.createQuery(
                    "FROM Coupon c WHERE c.restaurant.id = :restaurantId ORDER BY c.createdAt DESC", Coupon.class);
                query.setParameter("restaurantId", restaurantId);
            }
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding coupons by restaurant ID {}: {}", restaurantId, e.getMessage());
            throw new RuntimeException("Failed to find coupons by restaurant", e);
        }
    }
    
    /**
     * Gets global coupons (applicable to all restaurants)
     */
    public List<Coupon> findGlobalCoupons() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.restaurant IS NULL ORDER BY c.createdAt DESC", Coupon.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding global coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to find global coupons", e);
        }
    }
    
    /**
     * Gets expired coupons
     */
    public List<Coupon> findExpiredCoupons() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime now = LocalDateTime.now();
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.validUntil <= :now ORDER BY c.validUntil DESC", Coupon.class);
            query.setParameter("now", now);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding expired coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to find expired coupons", e);
        }
    }
    
    /**
     * Gets coupons expiring soon (within specified days)
     */
    public List<Coupon> findCouponsExpiringSoon(int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime future = now.plusDays(days);
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.isActive = true " +
                "AND c.validUntil > :now AND c.validUntil <= :future " +
                "ORDER BY c.validUntil ASC", Coupon.class);
            query.setParameter("now", now);
            query.setParameter("future", future);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding coupons expiring soon: {}", e.getMessage());
            throw new RuntimeException("Failed to find expiring coupons", e);
        }
    }
    
    /**
     * Gets coupons by type
     */
    public List<Coupon> findByType(Coupon.CouponType type) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.type = :type ORDER BY c.createdAt DESC", Coupon.class);
            query.setParameter("type", type);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding coupons by type {}: {}", type, e.getMessage());
            throw new RuntimeException("Failed to find coupons by type", e);
        }
    }
    
    /**
     * Gets coupons created by specific user
     */
    public List<Coupon> findByCreatedBy(Long createdBy) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.createdBy = :createdBy ORDER BY c.createdAt DESC", Coupon.class);
            query.setParameter("createdBy", createdBy);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding coupons by creator {}: {}", createdBy, e.getMessage());
            throw new RuntimeException("Failed to find coupons by creator", e);
        }
    }
    
    /**
     * Gets coupons with pagination
     */
    public List<Coupon> findWithPagination(int page, int size) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c ORDER BY c.createdAt DESC", Coupon.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding coupons with pagination: {}", e.getMessage());
            throw new RuntimeException("Failed to find coupons with pagination", e);
        }
    }
    
    /**
     * Counts total coupons
     */
    public Long countAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(c) FROM Coupon c", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Error counting all coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to count coupons", e);
        }
    }
    
    /**
     * Counts active coupons
     */
    public Long countActive() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(c) FROM Coupon c WHERE c.isActive = true", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Error counting active coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to count active coupons", e);
        }
    }
    
    /**
     * Gets all coupons
     */
    public List<Coupon> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c ORDER BY c.createdAt DESC", Coupon.class);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding all coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to find all coupons", e);
        }
    }
    
    // ==================== BUSINESS LOGIC QUERIES ====================
    
    /**
     * Finds applicable coupons for a specific order amount and restaurant
     */
    public List<Coupon> findApplicableCoupons(Double orderAmount, Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime now = LocalDateTime.now();
            Query<Coupon> query = session.createQuery(
                "FROM Coupon c WHERE c.isActive = true " +
                "AND c.validFrom <= :now AND c.validUntil > :now " +
                "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit) " +
                "AND (c.minOrderAmount IS NULL OR c.minOrderAmount <= :orderAmount) " +
                "AND (c.restaurant IS NULL OR c.restaurant.id = :restaurantId) " +
                "ORDER BY c.type, c.value DESC", Coupon.class);
            query.setParameter("now", now);
            query.setParameter("orderAmount", orderAmount);
            query.setParameter("restaurantId", restaurantId);
            return query.list();
        } catch (Exception e) {
            logger.error("Error finding applicable coupons: {}", e.getMessage());
            throw new RuntimeException("Failed to find applicable coupons", e);
        }
    }
    
    /**
     * Updates coupon usage count
     */
    public void incrementUsageCount(Long couponId) {
        Transaction tx = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query query = session.createQuery(
                "UPDATE Coupon c SET c.usedCount = c.usedCount + 1, c.updatedAt = :now WHERE c.id = :id");
            query.setParameter("now", LocalDateTime.now());
            query.setParameter("id", couponId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error incrementing usage count for coupon {}: {}", couponId, e.getMessage());
            throw new RuntimeException("Failed to increment usage count", e);
        }
    }
    
    /**
     * Decrements coupon usage count (for refunds)
     */
    public void decrementUsageCount(Long couponId) {
        Transaction tx = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query query = session.createQuery(
                "UPDATE Coupon c SET c.usedCount = CASE WHEN c.usedCount > 0 THEN c.usedCount - 1 ELSE 0 END, " +
                "c.updatedAt = :now WHERE c.id = :id");
            query.setParameter("now", LocalDateTime.now());
            query.setParameter("id", couponId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error decrementing usage count for coupon {}: {}", couponId, e.getMessage());
            throw new RuntimeException("Failed to decrement usage count", e);
        }
    }
}
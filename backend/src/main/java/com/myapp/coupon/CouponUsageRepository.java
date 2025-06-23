package com.myapp.coupon;

import com.myapp.common.models.CouponUsage;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CouponUsage operations
 * Handles per-user coupon usage tracking and validation
 */
public class CouponUsageRepository {
    
    private final SessionFactory sessionFactory;
    
    public CouponUsageRepository() {
        this.sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    // Constructor for dependency injection (testing)
    public CouponUsageRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public CouponUsage save(CouponUsage couponUsage) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(couponUsage);
            session.getTransaction().commit();
            return couponUsage;
        }
    }
    
    public CouponUsage update(CouponUsage couponUsage) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            CouponUsage updated = session.merge(couponUsage);
            session.getTransaction().commit();
            return updated;
        }
    }
    
    public Optional<CouponUsage> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            CouponUsage usage = session.get(CouponUsage.class, id);
            return Optional.ofNullable(usage);
        }
    }
    
    public List<CouponUsage> findByCouponIdAndUserId(Long couponId, Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.userId = :userId ORDER BY cu.usedAt DESC", 
                CouponUsage.class);
            query.setParameter("couponId", couponId);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }
    
    public List<CouponUsage> findActiveByCouponIdAndUserId(Long couponId, Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.userId = :userId AND cu.isActive = true ORDER BY cu.usedAt DESC", 
                CouponUsage.class);
            query.setParameter("couponId", couponId);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }
    
    public Long countActiveByCouponIdAndUserId(Long couponId, Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.userId = :userId AND cu.isActive = true", 
                Long.class);
            query.setParameter("couponId", couponId);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        }
    }
    
    public List<CouponUsage> findByCouponId(Long couponId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.coupon.id = :couponId ORDER BY cu.usedAt DESC", 
                CouponUsage.class);
            query.setParameter("couponId", couponId);
            return query.getResultList();
        }
    }
    
    public List<CouponUsage> findByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.userId = :userId ORDER BY cu.usedAt DESC", 
                CouponUsage.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }
    
    public Optional<CouponUsage> findByOrderId(Long orderId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.orderId = :orderId", 
                CouponUsage.class);
            query.setParameter("orderId", orderId);
            return query.getResultStream().findFirst();
        }
    }
    
    public void delete(Long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            CouponUsage usage = session.get(CouponUsage.class, id);
            if (usage != null) {
                session.remove(usage);
            }
            session.getTransaction().commit();
        }
    }
    
    public void deleteAll() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM CouponUsage").executeUpdate();
            session.getTransaction().commit();
        }
    }
    
    // Statistics methods
    public Long countTotalUsage() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(cu) FROM CouponUsage cu", Long.class);
            return query.getSingleResult();
        }
    }
    
    public Long countActiveUsage() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.isActive = true", Long.class);
            return query.getSingleResult();
        }
    }
    
    public Double getTotalDiscountAmount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT COALESCE(SUM(cu.discountAmount), 0.0) FROM CouponUsage cu WHERE cu.isActive = true", 
                Double.class);
            return query.getSingleResult();
        }
    }
} 
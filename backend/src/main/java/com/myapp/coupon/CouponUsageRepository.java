package com.myapp.coupon;

import com.myapp.common.models.CouponUsage;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository لایه دسترسی داده برای entity های CouponUsage
 * 
 * این کلاس تمام عملیات پایگاه داده مربوط به ردیابی استفاده از کوپن‌ها را ارائه می‌دهد:
 * 
 * === عملیات CRUD پایه ===
 * - save(): ذخیره استفاده جدید از کوپن
 * - update(): به‌روزرسانی استفاده موجود
 * - findById(): جستجو بر اساس شناسه
 * - delete(): حذف رکورد استفاده
 * - deleteAll(): پاکسازی کامل (تست)
 * 
 * === جستجوهای تخصصی ===
 * - findByCouponIdAndUserId(): تمام استفاده‌های کاربر از کوپن خاص
 * - findActiveByCouponIdAndUserId(): استفاده‌های فعال کاربر از کوپن
 * - countActiveByCouponIdAndUserId(): شمارش استفاده‌های فعال
 * - findByCouponId(): تمام استفاده‌ها از کوپن خاص
 * - findByUserId(): تمام استفاده‌های کاربر
 * - findByOrderId(): استفاده کوپن در سفارش خاص
 * 
 * === عملیات آماری ===
 * - countTotalUsage(): تعداد کل استفاده‌ها
 * - countActiveUsage(): تعداد استفاده‌های فعال
 * - getTotalDiscountAmount(): مجموع مبلغ تخفیف‌های اعمال شده
 * 
 * === ویژگی‌های کلیدی ===
 * - Usage Tracking: ردیابی دقیق استفاده از کوپن‌ها
 * - Per-User Limitations: محدودیت‌های per-user
 * - Active Status Management: مدیریت وضعیت فعال/غیرفعال
 * - Order Linking: اتصال با سفارشات
 * - Revert Support: پشتیبانی از بازگشت استفاده
 * - Statistical Queries: queries آماری پیچیده
 * - Transaction Management: مدیریت تراکنش‌ها
 * - SessionFactory Injection: تزریق وابستگی برای تست
 * - HQL Queries: استفاده از Hibernate Query Language
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class CouponUsageRepository {
    
    /** SessionFactory برای مدیریت ارتباط با دیتابیس */
    private final SessionFactory sessionFactory;
    
    /**
     * سازنده پیش‌فرض - دریافت SessionFactory از DatabaseUtil
     */
    public CouponUsageRepository() {
        this.sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    /**
     * سازنده برای تزریق وابستگی (Dependency Injection)
     * برای تست‌ها و configuration سفارشی استفاده می‌شود
     * 
     * @param sessionFactory SessionFactory سفارشی
     */
    public CouponUsageRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    // ==================== BASIC CRUD OPERATIONS ====================
    
    /**
     * ذخیره استفاده جدید از کوپن در دیتابیس
     * 
     * @param couponUsage استفاده از کوپن برای ذخیره
     * @return استفاده ذخیره شده با ID تولید شده
     */
    public CouponUsage save(CouponUsage couponUsage) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(couponUsage);
            session.getTransaction().commit();
            return couponUsage;
        }
    }
    
    /**
     * به‌روزرسانی استفاده موجود از کوپن
     * 
     * @param couponUsage استفاده از کوپن برای به‌روزرسانی
     * @return استفاده به‌روزرسانی شده
     */
    public CouponUsage update(CouponUsage couponUsage) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            CouponUsage updated = session.merge(couponUsage);
            session.getTransaction().commit();
            return updated;
        }
    }
    
    /**
     * جستجوی استفاده از کوپن بر اساس شناسه
     * 
     * @param id شناسه استفاده از کوپن
     * @return Optional حاوی استفاده یا empty در صورت عدم وجود
     */
    public Optional<CouponUsage> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            CouponUsage usage = session.get(CouponUsage.class, id);
            return Optional.ofNullable(usage);
        }
    }
    
    // ==================== SPECIALIZED QUERIES ====================
    
    /**
     * یافتن تمام استفاده‌های کاربر از کوپن خاص
     * 
     * مرتب شده بر اساس تاریخ استفاده (جدیدترین اول)
     * 
     * @param couponId شناسه کوپن
     * @param userId شناسه کاربر
     * @return لیست استفاده‌های کاربر از کوپن
     */
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
    
    /**
     * یافتن استفاده‌های فعال کاربر از کوپن خاص
     * 
     * فقط استفاده‌هایی که هنوز بازگشت داده نشده‌اند (isActive = true)
     * 
     * @param couponId شناسه کوپن
     * @param userId شناسه کاربر
     * @return لیست استفاده‌های فعال کاربر
     */
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
    
    /**
     * شمارش استفاده‌های فعال کاربر از کوپن خاص
     * 
     * برای بررسی محدودیت per-user استفاده می‌شود
     * 
     * @param couponId شناسه کوپن
     * @param userId شناسه کاربر
     * @return تعداد استفاده‌های فعال
     */
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
    
    /**
     * یافتن تمام استفاده‌ها از کوپن خاص
     * 
     * مرتب شده بر اساس تاریخ استفاده (جدیدترین اول)
     * برای آمارگیری و گزارش‌گیری استفاده می‌شود
     * 
     * @param couponId شناسه کوپن
     * @return لیست تمام استفاده‌ها از کوپن
     */
    public List<CouponUsage> findByCouponId(Long couponId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.coupon.id = :couponId ORDER BY cu.usedAt DESC", 
                CouponUsage.class);
            query.setParameter("couponId", couponId);
            return query.getResultList();
        }
    }
    
    /**
     * یافتن تمام استفاده‌های کاربر از کوپن‌ها
     * 
     * مرتب شده بر اساس تاریخ استفاده (جدیدترین اول)
     * برای نمایش تاریخچه استفاده کاربر استفاده می‌شود
     * 
     * @param userId شناسه کاربر
     * @return لیست تمام استفاده‌های کاربر
     */
    public List<CouponUsage> findByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.userId = :userId ORDER BY cu.usedAt DESC", 
                CouponUsage.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        }
    }
    
    /**
     * یافتن استفاده از کوپن در سفارش خاص
     * 
     * هر سفارش حداکثر یک کوپن می‌تواند داشته باشد
     * 
     * @param orderId شناسه سفارش
     * @return Optional حاوی استفاده از کوپن یا empty
     */
    public Optional<CouponUsage> findByOrderId(Long orderId) {
        try (Session session = sessionFactory.openSession()) {
            Query<CouponUsage> query = session.createQuery(
                "FROM CouponUsage cu WHERE cu.orderId = :orderId", 
                CouponUsage.class);
            query.setParameter("orderId", orderId);
            return query.getResultStream().findFirst();
        }
    }
    
    // ==================== DELETE OPERATIONS ====================
    
    /**
     * حذف استفاده از کوپن با شناسه
     * 
     * @param id شناسه استفاده برای حذف
     */
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
    
    /**
     * حذف تمام استفاده‌ها از کوپن‌ها (متد کمکی برای تست‌ها)
     * ⚠️ هشدار: این متد فقط برای محیط تست استفاده شود
     */
    public void deleteAll() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createQuery("DELETE FROM CouponUsage").executeUpdate();
            session.getTransaction().commit();
        }
    }
    
    // ==================== STATISTICAL OPERATIONS ====================
    
    /**
     * شمارش تعداد کل استفاده‌ها از کوپن‌ها
     * 
     * شامل استفاده‌های فعال و غیرفعال
     * 
     * @return تعداد کل استفاده‌ها
     */
    public Long countTotalUsage() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(cu) FROM CouponUsage cu", Long.class);
            return query.getSingleResult();
        }
    }
    
    /**
     * شمارش تعداد استفاده‌های فعال از کوپن‌ها
     * 
     * فقط استفاده‌هایی که بازگشت داده نشده‌اند
     * 
     * @return تعداد استفاده‌های فعال
     */
    public Long countActiveUsage() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.isActive = true", Long.class);
            return query.getSingleResult();
        }
    }
    
    /**
     * محاسبه مجموع مبلغ تخفیف‌های اعمال شده
     * 
     * فقط تخفیف‌های فعال (بازگشت داده نشده) محاسبه می‌شوند
     * 
     * @return مجموع مبلغ تخفیف‌ها
     */
    public Double getTotalDiscountAmount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT COALESCE(SUM(cu.discountAmount), 0.0) FROM CouponUsage cu WHERE cu.isActive = true", 
                Double.class);
            return query.getSingleResult();
        }
    }
} 

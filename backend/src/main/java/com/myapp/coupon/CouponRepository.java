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
 * Repository لایه دسترسی داده برای entity های Coupon
 * 
 * این کلاس تمام عملیات پایگاه داده مربوط به مدیریت کوپن‌های تخفیف را ارائه می‌دهد:
 * 
 * === عملیات CRUD پایه ===
 * - save(): ذخیره کوپن جدید
 * - update(): به‌روزرسانی کوپن موجود
 * - findById(): جستجو بر اساس شناسه
 * - findByCode(): جستجو بر اساس کد کوپن
 * - delete(): حذف کوپن
 * - existsByCode(): بررسی وجود کد کوپن
 * 
 * === جستجوهای وضعیت ===
 * - findActiveCoupons(): کوپن‌های فعال
 * - findValidCoupons(): کوپن‌های معتبر (فعال + در بازه زمانی)
 * - findExpiredCoupons(): کوپن‌های منقضی
 * - findCouponsExpiringSoon(): کوپن‌های نزدیک به انقضا
 * 
 * === جستجوهای تخصصی ===
 * - findByRestaurant(): کوپن‌های رستوران خاص
 * - findGlobalCoupons(): کوپن‌های سراسری
 * - findByType(): کوپن‌ها بر اساس نوع (درصد/مبلغ ثابت)
 * - findByCreatedBy(): کوپن‌های ایجاد شده توسط کاربر خاص
 * - findApplicableCoupons(): کوپن‌های قابل اعمال برای سفارش
 * 
 * === صفحه‌بندی و آمار ===
 * - findWithPagination(): دریافت با صفحه‌بندی
 * - findAll(): تمام کوپن‌ها
 * - countAll(): تعداد کل کوپن‌ها
 * - countActive(): تعداد کوپن‌های فعال
 * 
 * === مدیریت استفاده ===
 * - incrementUsageCount(): افزایش تعداد استفاده
 * - decrementUsageCount(): کاهش تعداد استفاده (برای استرداد)
 * 
 * === ویژگی‌های کلیدی ===
 * - Complex Business Queries: queries پیچیده برای منطق کسب‌وکار
 * - Date Range Validation: اعتبارسنجی بازه زمانی
 * - Usage Tracking: ردیابی تعداد استفاده
 * - Restaurant Scoping: پشتیبانی از کوپن‌های عمومی و اختصاصی
 * - Transaction Management: مدیریت تراکنش‌ها
 * - Error Handling: مدیریت خطاها با try-catch
 * - Logging: ثبت تمام عملیات
 * - HQL Queries: استفاده از Hibernate Query Language
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class CouponRepository {
    
    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(CouponRepository.class);
    
    // ==================== BASIC CRUD OPERATIONS ====================
    
    /**
     * ذخیره کوپن جدید در پایگاه داده
     * 
     * @param coupon شیء کوپن برای ذخیره
     * @return کوپن ذخیره شده با ID تخصیص داده شده
     * @throws RuntimeException در صورت خطا در ذخیره‌سازی
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
     * به‌روزرسانی کوپن موجود
     * 
     * تاریخ updatedAt به صورت خودکار به‌روزرسانی می‌شود
     * 
     * @param coupon شیء کوپن برای به‌روزرسانی
     * @return کوپن به‌روزرسانی شده
     * @throws RuntimeException در صورت خطا در به‌روزرسانی
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
     * جستجوی کوپن بر اساس شناسه
     * 
     * @param id شناسه کوپن
     * @return Optional حاوی کوپن یا empty در صورت عدم وجود
     * @throws RuntimeException در صورت خطا در جستجو
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
     * جستجوی کوپن بر اساس کد کوپن
     * 
     * کد کوپن یکتا است و برای اعتبارسنجی استفاده می‌شود
     * 
     * @param code کد کوپن
     * @return Optional حاوی کوپن یا empty در صورت عدم وجود
     * @throws RuntimeException در صورت خطا در جستجو
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
     * بررسی وجود کد کوپن در سیستم
     * 
     * برای جلوگیری از ایجاد کد تکراری استفاده می‌شود
     * 
     * @param code کد کوپن برای بررسی
     * @return true اگر کد وجود داشته باشد، false در غیر این صورت
     * @throws RuntimeException در صورت خطا در بررسی
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
     * حذف کوپن بر اساس شناسه
     * 
     * @param id شناسه کوپن
     * @return true در صورت حذف موفق، false در صورت عدم وجود
     * @throws RuntimeException در صورت خطا در حذف
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
     * دریافت تمام کوپن‌های فعال
     * 
     * کوپن‌هایی که isActive = true دارند
     * 
     * @return لیست کوپن‌های فعال (مرتب شده بر اساس تاریخ ایجاد)
     * @throws RuntimeException در صورت خطا در جستجو
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
     * دریافت تمام کوپن‌های معتبر
     * 
     * کوپن‌هایی که:
     * - فعال هستند (isActive = true)
     * - در بازه زمانی معتبر قرار دارند
     * - هنوز محدودیت استفاده را نداشته‌اند
     * 
     * @return لیست کوپن‌های معتبر
     * @throws RuntimeException در صورت خطا در جستجو
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
     * دریافت کوپن‌های مربوط به رستوران خاص
     * 
     * @param restaurant شیء رستوران (null برای کوپن‌های سراسری)
     * @return لیست کوپن‌های رستوران یا سراسری
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Coupon> findByRestaurant(Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query;
            if (restaurant == null) {
                // کوپن‌های سراسری
                query = session.createQuery(
                    "FROM Coupon c WHERE c.restaurant IS NULL ORDER BY c.createdAt DESC", Coupon.class);
            } else {
                // کوپن‌های اختصاصی رستوران
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
     * دریافت کوپن‌های مربوط به رستوران خاص (بر اساس ID)
     * 
     * @param restaurantId شناسه رستوران (null برای کوپن‌های سراسری)
     * @return لیست کوپن‌های رستوران یا سراسری
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Coupon> findByRestaurantId(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Query<Coupon> query;
            if (restaurantId == null) {
                // کوپن‌های سراسری
                query = session.createQuery(
                    "FROM Coupon c WHERE c.restaurant IS NULL ORDER BY c.createdAt DESC", Coupon.class);
            } else {
                // کوپن‌های اختصاصی رستوران
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
     * دریافت کوپن‌های سراسری
     * 
     * کوپن‌هایی که برای تمام رستوران‌ها قابل استفاده هستند
     * 
     * @return لیست کوپن‌های سراسری
     * @throws RuntimeException در صورت خطا در جستجو
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
     * دریافت کوپن‌های منقضی شده
     * 
     * کوپن‌هایی که تاریخ انقضایشان گذشته است
     * 
     * @return لیست کوپن‌های منقضی (مرتب شده بر اساس تاریخ انقضا)
     * @throws RuntimeException در صورت خطا در جستجو
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
     * دریافت کوپن‌های نزدیک به انقضا
     * 
     * کوپن‌هایی که در چند روز آینده منقضی می‌شوند
     * برای هشدار و اطلاع‌رسانی به کاربران استفاده می‌شود
     * 
     * @param days تعداد روزهای آینده
     * @return لیست کوپن‌های نزدیک به انقضا (مرتب شده بر اساس تاریخ انقضا)
     * @throws RuntimeException در صورت خطا در جستجو
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
     * دریافت کوپن‌ها بر اساس نوع
     * 
     * @param type نوع کوپن (PERCENTAGE، FIXED_AMOUNT)
     * @return لیست کوپن‌های از نوع مشخص شده
     * @throws RuntimeException در صورت خطا در جستجو
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
     * دریافت کوپن‌های ایجاد شده توسط کاربر خاص
     * 
     * برای مدیریت کوپن‌ها توسط ادمین‌ها یا مالکان رستوران‌ها
     * 
     * @param createdBy شناسه کاربر ایجادکننده
     * @return لیست کوپن‌های ایجاد شده توسط کاربر
     * @throws RuntimeException در صورت خطا در جستجو
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
     * دریافت کوپن‌ها با صفحه‌بندی
     * 
     * برای بهبود عملکرد در صفحات مدیریت با تعداد زیاد کوپن
     * 
     * @param page شماره صفحه (شروع از 0)
     * @param size تعداد رکورد در هر صفحه
     * @return لیست کوپن‌ها با صفحه‌بندی
     * @throws RuntimeException در صورت خطا در جستجو
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
     * شمارش کل تعداد کوپن‌ها
     * 
     * برای آمارهای کلی و محاسبه pagination
     * 
     * @return تعداد کل کوپن‌ها
     * @throws RuntimeException در صورت خطا در شمارش
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
     * شمارش تعداد کوپن‌های فعال
     * 
     * برای آمارهای dashboard
     * 
     * @return تعداد کوپن‌های فعال
     * @throws RuntimeException در صورت خطا در شمارش
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
     * دریافت تمام کوپن‌ها
     * 
     * برای مدیریت کلی سیستم توسط ادمین‌ها
     * 
     * @return لیست تمام کوپن‌ها (مرتب شده بر اساس تاریخ ایجاد)
     * @throws RuntimeException در صورت خطا در جستجو
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
     * یافتن کوپن‌های قابل اعمال برای سفارش خاص
     * 
     * این متد پیچیده‌ترین query است که شامل:
     * - بررسی فعال بودن کوپن
     * - بررسی بازه زمانی معتبر
     * - بررسی محدودیت استفاده
     * - بررسی حداقل مبلغ سفارش
     * - بررسی تطبیق با رستوران (عمومی یا اختصاصی)
     * 
     * @param orderAmount مبلغ سفارش
     * @param restaurantId شناسه رستوران
     * @return لیست کوپن‌های قابل اعمال (مرتب شده بر اساس نوع و مقدار)
     * @throws RuntimeException در صورت خطا در جستجو
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
     * افزایش تعداد استفاده کوپن
     * 
     * زمانی که کوپن در سفارش استفاده می‌شود، باید تعداد استفاده آن افزایش یابد
     * تاریخ updatedAt نیز به‌روزرسانی می‌شود
     * 
     * @param couponId شناسه کوپن
     * @throws RuntimeException در صورت خطا در به‌روزرسانی
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
     * کاهش تعداد استفاده کوپن
     * 
     * برای مواقع استرداد سفارش یا لغو استفاده از کوپن
     * تعداد استفاده نمی‌تواند کمتر از 0 شود
     * 
     * @param couponId شناسه کوپن
     * @throws RuntimeException در صورت خطا در به‌روزرسانی
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

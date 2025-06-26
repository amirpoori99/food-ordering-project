package com.myapp.review;

import com.myapp.common.models.Rating;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository لایه دسترسی داده برای entity های Rating (نظرات و امتیازات)
 * 
 * این کلاس تمام عملیات پایگاه داده مربوط به مدیریت نظرات و امتیازدهی را ارائه می‌دهد:
 * 
 * === عملیات CRUD پایه ===
 * - save(): ذخیره/به‌روزرسانی نظر و امتیاز
 * - findById(): جستجو بر اساس شناسه
 * - delete(): حذف نظر
 * - findAll(): دریافت تمام نظرات
 * 
 * === جستجوهای تخصصی ===
 * - findByUserAndRestaurant(): نظر کاربر برای رستوران خاص (unique constraint)
 * - findByRestaurant(): تمام نظرات رستوران
 * - findByUser(): تمام نظرات کاربر
 * - findByScoreRange(): نظرات در بازه امتیاز مشخص
 * - findVerifiedRatings(): نظرات تایید شده
 * - findRatingsWithReviews(): نظراتی که متن نظر دارند
 * - findRecentRatings(): نظرات اخیر (در چند روز گذشته)
 * 
 * === عملیات آماری ===
 * - getAverageRating(): میانگین امتیاز رستوران
 * - getRatingCount(): تعداد نظرات رستوران
 * - getRatingDistribution(): توزیع امتیازات (تعداد هر امتیاز 1-5)
 * - getTopRatedRestaurants(): رستوران‌های برتر بر اساس امتیاز
 * 
 * === صفحه‌بندی و شمارش ===
 * - findWithPagination(): دریافت با صفحه‌بندی
 * - countAll(): تعداد کل نظرات
 * 
 * === ویژگی‌های کلیدی ===
 * - Unique Constraint: هر کاربر فقط یک نظر برای هر رستوران
 * - Statistical Queries: queries پیچیده برای آمارگیری
 * - Rating Distribution: تحلیل توزیع امتیازات
 * - Verification Support: پشتیبانی از نظرات تایید شده
 * - Temporal Filtering: فیلتر بر اساس زمان
 * - Aggregation Functions: استفاده از توابع آماری SQL
 * - Error Handling: مدیریت خطاها با try-catch
 * - Logging: ثبت تمام عملیات
 * - HQL Queries: استفاده از Hibernate Query Language
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class RatingRepository {
    
    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(RatingRepository.class);
    
    /**
     * ذخیره نظر جدید یا به‌روزرسانی نظر موجود
     * 
     * این متد بر اساس وجود ID تشخیص می‌دهد که آیا باید نظر جدید ایجاد کند یا موجودی را به‌روزرسانی کند
     * 
     * @param rating شیء نظر برای ذخیره
     * @return نظر ذخیره شده یا به‌روزرسانی شده
     * @throws RuntimeException در صورت خطا در ذخیره‌سازی
     */
    public Rating save(Rating rating) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            if (rating.getId() == null) {
                // ایجاد نظر جدید
                session.persist(rating);
                logger.info("Created new rating: {}", rating);
            } else {
                // به‌روزرسانی نظر موجود
                rating = session.merge(rating);
                logger.info("Updated rating: {}", rating);
            }
            
            transaction.commit();
            return rating;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving rating: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save rating", e);
        }
    }
    
    /**
     * جستجوی نظر بر اساس شناسه
     * 
     * @param id شناسه نظر
     * @return Optional حاوی نظر یا empty در صورت عدم وجود
     */
    public Optional<Rating> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Rating rating = session.get(Rating.class, id);
            return Optional.ofNullable(rating);
        } catch (Exception e) {
            logger.error("Error finding rating by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * جستجوی نظر بر اساس کاربر و رستوران (محدودیت یکتایی)
     * 
     * هر کاربر فقط می‌تواند یک نظر برای هر رستوران داشته باشد
     * 
     * @param user کاربر نظردهنده
     * @param restaurant رستوران مورد نظر
     * @return Optional حاوی نظر یا empty در صورت عدم وجود
     */
    public Optional<Rating> findByUserAndRestaurant(User user, Restaurant restaurant) {
        if (user == null || restaurant == null) {
            logger.warn("Cannot find rating with null user or restaurant");
            return Optional.empty();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.user.id = :userId AND r.restaurant.id = :restaurantId";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("userId", user.getId());
            query.setParameter("restaurantId", restaurant.getId());
            
            List<Rating> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error finding rating by user {} and restaurant {}: {}", 
                        user.getId(), restaurant.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * دریافت تمام نظرات مربوط به رستوران خاص
     * 
     * نظرات بر اساس تاریخ ایجاد (جدیدترین ابتدا) مرتب می‌شوند
     * 
     * @param restaurant رستوران مورد نظر
     * @return لیست نظرات رستوران
     */
    public List<Rating> findByRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot find ratings for null restaurant");
            return List.of();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.restaurant.id = :restaurantId ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * دریافت تمام نظرات ثبت شده توسط کاربر خاص
     * 
     * @param user کاربر مورد نظر
     * @return لیست نظرات کاربر (مرتب شده بر اساس تاریخ)
     */
    public List<Rating> findByUser(User user) {
        if (user == null) {
            logger.warn("Cannot find ratings for null user");
            return List.of();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.user.id = :userId ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("userId", user.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings by user {}: {}", user.getId(), e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * جستجوی نظرات در بازه امتیاز مشخص
     * 
     * @param minScore حداقل امتیاز (شامل)
     * @param maxScore حداکثر امتیاز (شامل)
     * @return لیست نظرات در بازه امتیاز
     */
    public List<Rating> findByScoreRange(int minScore, int maxScore) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.ratingScore BETWEEN :minScore AND :maxScore ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("minScore", minScore);
            query.setParameter("maxScore", maxScore);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings by score range {}-{}: {}", 
                        minScore, maxScore, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * دریافت نظرات تایید شده
     * 
     * نظراتی که توسط مدیریت سیستم تایید شده‌اند
     * 
     * @return لیست نظرات تایید شده
     */
    public List<Rating> findVerifiedRatings() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.isVerified = true ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding verified ratings: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * دریافت نظراتی که متن نظر دارند
     * 
     * نظراتی که علاوه بر امتیاز، متن توضیحی نیز ارائه کرده‌اند
     * 
     * @return لیست نظرات دارای متن
     */
    public List<Rating> findRatingsWithReviews() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.reviewText IS NOT NULL AND r.reviewText != '' ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings with reviews: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * دریافت نظرات اخیر (در چند روز گذشته)
     * 
     * @param days تعداد روزهای گذشته
     * @return لیست نظرات اخیر
     */
    public List<Rating> findRecentRatings(int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            String hql = "FROM Rating r WHERE r.createdAt >= :cutoffDate ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("cutoffDate", cutoffDate);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding recent ratings for {} days: {}", days, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * محاسبه میانگین امتیاز رستوران
     * 
     * @param restaurant رستوران مورد نظر
     * @return میانگین امتیاز (0.0 در صورت عدم وجود نظر)
     */
    public Double getAverageRating(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot get average rating for null restaurant");
            return 0.0;
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT AVG(r.ratingScore) FROM Rating r WHERE r.restaurant.id = :restaurantId";
            Query<Double> query = session.createQuery(hql, Double.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
            
        } catch (Exception e) {
            logger.error("Error getting average rating for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return 0.0;
        }
    }
    
    /**
     * شمارش تعداد نظرات رستوران
     * 
     * @param restaurant رستوران مورد نظر
     * @return تعداد نظرات (0 در صورت عدم وجود)
     */
    public Long getRatingCount(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot get rating count for null restaurant");
            return 0L;
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(r) FROM Rating r WHERE r.restaurant.id = :restaurantId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error getting rating count for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * دریافت توزیع امتیازات رستوران (تعداد هر امتیاز از 1 تا 5)
     * 
     * این متد برای نمایش نمودار توزیع امتیازات استفاده می‌شود
     * 
     * @param restaurant رستوران مورد نظر
     * @return Map حاوی تعداد هر امتیاز (1-5)
     */
    public Map<Integer, Long> getRatingDistribution(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot get rating distribution for null restaurant");
            return Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L);
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r.ratingScore, COUNT(r) FROM Rating r WHERE r.restaurant.id = :restaurantId GROUP BY r.ratingScore";
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("restaurantId", restaurant.getId());
            
            List<Object[]> results = query.getResultList();
            Map<Integer, Long> distribution = new java.util.HashMap<>();
            
            // مقداردهی اولیه تمام امتیازها به 0
            for (int i = 1; i <= 5; i++) {
                distribution.put(i, 0L);
            }
            
            // پر کردن تعداد واقعی
            for (Object[] result : results) {
                Integer score = (Integer) result[0];
                Long count = (Long) result[1];
                distribution.put(score, count);
            }
            
            return distribution;
            
        } catch (Exception e) {
            logger.error("Error getting rating distribution for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L);
        }
    }
    
    /**
     * دریافت رستوران‌های برتر بر اساس امتیاز
     * 
     * فقط رستوران‌هایی که حداقل 5 نظر دارند در نظر گرفته می‌شوند
     * ابتدا بر اساس میانگین امتیاز و سپس تعداد نظرات مرتب می‌شوند
     * 
     * @param limit تعداد رستوران‌های برتر
     * @return لیست Object[] حاوی [restaurantId, restaurantName, averageRating, ratingCount]
     */
    public List<Object[]> getTopRatedRestaurants(int limit) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT r.restaurant.id, r.restaurant.name, AVG(r.ratingScore), COUNT(r)
                FROM Rating r 
                GROUP BY r.restaurant.id, r.restaurant.name 
                HAVING COUNT(r) >= 5 
                ORDER BY AVG(r.ratingScore) DESC, COUNT(r) DESC
                """;
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setMaxResults(limit);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error getting top rated restaurants: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * حذف نظر بر اساس شناسه
     * 
     * @param id شناسه نظر
     * @return true در صورت حذف موفق، false در غیر این صورت
     */
    public boolean delete(Long id) {
        if (id == null) {
            logger.warn("Cannot delete rating with null ID");
            return false;
        }
        
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Rating rating = session.get(Rating.class, id);
            if (rating != null) {
                session.remove(rating);
                transaction.commit();
                logger.info("Deleted rating with ID: {}", id);
                return true;
            } else {
                transaction.rollback();
                logger.warn("Rating with ID {} not found for deletion", id);
                return false;
            }
            
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            logger.error("Error deleting rating {}: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * دریافت تمام نظرات (برای مقاصد مدیریتی)
     * 
     * @return لیست تمام نظرات (مرتب شده بر اساس تاریخ ایجاد)
     */
    public List<Rating> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding all ratings: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * دریافت نظرات با صفحه‌بندی
     * 
     * برای بهبود عملکرد در صفحات مدیریت با تعداد زیاد نظرات
     * 
     * @param offset شروع از رکورد (شروع از 0)
     * @param limit تعداد رکوردها در هر صفحه
     * @return لیست نظرات با صفحه‌بندی
     */
    public List<Rating> findWithPagination(int offset, int limit) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings with pagination: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * شمارش کل تعداد نظرات
     * 
     * برای آمارهای کلی و محاسبه pagination
     * 
     * @return تعداد کل نظرات
     */
    public Long countAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(r) FROM Rating r";
            Query<Long> query = session.createQuery(hql, Long.class);
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting all ratings: {}", e.getMessage(), e);
            return 0L;
        }
    }
} 
package com.myapp.favorites;

import com.myapp.common.models.Favorite;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository لایه دسترسی داده برای entity های Favorite
 * 
 * این کلاس تمام عملیات پایگاه داده مربوط به مدیریت علاقه‌مندی‌های کاربران را ارائه می‌دهد:
 * 
 * === عملیات CRUD پایه ===
 * - save(): ذخیره یا به‌روزرسانی علاقه‌مندی
 * - findById(): جستجو بر اساس شناسه
 * - delete(): حذف علاقه‌مندی
 * 
 * === جستجوهای تخصصی ===
 * - findByUser(): علاقه‌مندی‌های کاربر خاص
 * - findByRestaurant(): علاقه‌مندی‌های رستوران خاص
 * - findByUserAndRestaurant(): جستجوی ترکیبی
 * - findRecentFavorites(): علاقه‌مندی‌های اخیر
 * - findFavoritesWithNotes(): علاقه‌مندی‌های دارای یادداشت
 * 
 * === عملیات آماری ===
 * - countByUser(): تعداد علاقه‌مندی‌های کاربر
 * - countByRestaurant(): تعداد علاقه‌مندی‌های رستوران
 * - countAll(): کل تعداد علاقه‌مندی‌ها
 * 
 * === صفحه‌بندی ===
 * - findWithPagination(): دریافت با صفحه‌بندی
 * - findAll(): تمام علاقه‌مندی‌ها (برای ادمین)
 * 
 * === متدهای Legacy ===
 * - save(userId, restaurantId): روش قدیمی ذخیره
 * - find(userId, restaurantId): روش قدیمی جستجو
 * - listByUser(): نام قدیمی findByUser
 * - clear(): پاکسازی کامل (تست)
 * 
 * === ویژگی‌های کلیدی ===
 * - Transaction Management: مدیریت تراکنش‌ها
 * - Error Handling: مدیریت خطاها با try-catch
 * - Logging: ثبت تمام عملیات و خطاها
 * - HQL Queries: استفاده از Hibernate Query Language
 * - Null Safety: بررسی null برای ورودی‌ها
 * - Performance: Sort و Order بهینه
 * - Backward Compatibility: حفظ سازگاری با نسخه قبل
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class FavoritesRepository {

    /** Logger برای ثبت عملیات و خطاها */
    private static final Logger logger = LoggerFactory.getLogger(FavoritesRepository.class);

    /**
     * ذخیره علاقه‌مندی جدید یا به‌روزرسانی موجود
     * 
     * این متد از JPA merge و persist استفاده می‌کند تا بر اساس وجود ID
     * تصمیم بگیرد که آیا باید رکورد جدید ایجاد کند یا موجود را به‌روزرسانی کند
     * 
     * @param favorite شیء علاقه‌مندی برای ذخیره
     * @return علاقه‌مندی ذخیره شده با ID تخصیص داده شده
     * @throws RuntimeException در صورت خطا در ذخیره‌سازی
     */
    public Favorite save(Favorite favorite) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            if (favorite.getId() == null) {
                // رکورد جدید - از persist استفاده می‌کنیم
                session.persist(favorite);
                logger.info("Created new favorite: {}", favorite);
            } else {
                // به‌روزرسانی رکورد موجود - از merge استفاده می‌کنیم
                favorite = session.merge(favorite);
                logger.info("Updated favorite: {}", favorite);
            }
            
            transaction.commit();
            return favorite;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving favorite: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save favorite", e);
        }
    }

    /**
     * متد Legacy برای سازگاری با نسخه قبل
     * 
     * این متد شیوه قدیمی ذخیره علاقه‌مندی با userId و restaurantId است
     * ابتدا User و Restaurant را از دیتابیس load می‌کند سپس Favorite ایجاد می‌کند
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return علاقه‌مندی ایجاد شده
     * @throws IllegalArgumentException اگر کاربر یا رستوران یافت نشود
     * @throws RuntimeException در صورت خطا در ذخیره‌سازی
     */
    public Favorite save(long userId, long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, userId);
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            
            if (user == null || restaurant == null) {
                throw new IllegalArgumentException("User or Restaurant not found");
            }
            
            Favorite favorite = new Favorite(user, restaurant);
            return save(favorite);
            
        } catch (Exception e) {
            logger.error("Error saving favorite with userId {} and restaurantId {}: {}", 
                        userId, restaurantId, e.getMessage(), e);
            throw new RuntimeException("Failed to save favorite", e);
        }
    }

    /**
     * جستجوی علاقه‌مندی بر اساس شناسه
     * 
     * @param id شناسه علاقه‌مندی
     * @return Optional حاوی علاقه‌مندی یا empty در صورت عدم وجود
     */
    public Optional<Favorite> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Favorite favorite = session.get(Favorite.class, id);
            return Optional.ofNullable(favorite);
        } catch (Exception e) {
            logger.error("Error finding favorite by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * جستجوی علاقه‌مندی بر اساس کاربر و رستوران
     * 
     * این متد برای بررسی اینکه آیا کاربر خاص، رستوران خاص را به علاقه‌مندی‌هایش
     * اضافه کرده است یا خیر استفاده می‌شود
     * 
     * @param user شیء کاربر
     * @param restaurant شیء رستوران
     * @return Optional حاوی علاقه‌مندی یا empty در صورت عدم وجود
     */
    public Optional<Favorite> findByUserAndRestaurant(User user, Restaurant restaurant) {
        if (user == null || restaurant == null) {
            logger.warn("Cannot find favorite with null user or restaurant");
            return Optional.empty();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId AND f.restaurant.id = :restaurantId";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", user.getId());
            query.setParameter("restaurantId", restaurant.getId());
            
            List<Favorite> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error finding favorite by user {} and restaurant {}: {}", 
                        user != null ? user.getId() : "null", 
                        restaurant != null ? restaurant.getId() : "null", 
                        e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * متد Legacy برای سازگاری با نسخه قبل
     * 
     * همان کار findByUserAndRestaurant را انجام می‌دهد اما با ID به جای شیء
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     * @return Optional حاوی علاقه‌مندی یا empty در صورت عدم وجود
     */
    public Optional<Favorite> find(long userId, long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId AND f.restaurant.id = :restaurantId";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", userId);
            query.setParameter("restaurantId", restaurantId);
            
            List<Favorite> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error finding favorite by userId {} and restaurantId {}: {}", 
                        userId, restaurantId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * دریافت تمام علاقه‌مندی‌های یک کاربر
     * 
     * نتایج بر اساس تاریخ ایجاد به صورت نزولی مرتب می‌شوند
     * (جدیدترین‌ها اول)
     * 
     * @param user شیء کاربر
     * @return لیست علاقه‌مندی‌های کاربر
     */
    public List<Favorite> findByUser(User user) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", user.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites for user {}: {}", user.getId(), e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * متد Legacy برای سازگاری با نسخه قبل
     * 
     * همان کار findByUser را انجام می‌دهد اما با userId به جای شیء User
     * 
     * @param userId شناسه کاربر
     * @return لیست علاقه‌مندی‌های کاربر
     */
    public List<Favorite> listByUser(long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", userId);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error listing favorites for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * دریافت تمام علاقه‌مندی‌های یک رستوران
     * 
     * برای بررسی محبوبیت رستوران و آمارگیری استفاده می‌شود
     * نتایج بر اساس تاریخ ایجاد مرتب می‌شوند
     * 
     * @param restaurant شیء رستوران
     * @return لیست علاقه‌مندی‌های رستوران
     */
    public List<Favorite> findByRestaurant(Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.restaurant.id = :restaurantId ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * دریافت علاقه‌مندی‌های اخیر (در چند روز گذشته)
     * 
     * برای تحلیل trend ها و الگوهای رفتاری کاربران مفید است
     * 
     * @param days تعداد روزهای گذشته
     * @return لیست علاقه‌مندی‌های اخیر
     */
    public List<Favorite> findRecentFavorites(int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.createdAt >= :cutoffDate ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("cutoffDate", LocalDateTime.now().minusDays(days));
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding recent favorites for {} days: {}", days, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * دریافت علاقه‌مندی‌های دارای یادداشت
     * 
     * برخی کاربران یادداشت‌هایی برای علاقه‌مندی‌هایشان می‌نویسند
     * این متد تنها علاقه‌مندی‌های دارای یادداشت را برمی‌گرداند
     * 
     * @return لیست علاقه‌مندی‌های دارای یادداشت
     */
    public List<Favorite> findFavoritesWithNotes() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.notes IS NOT NULL AND f.notes != '' ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites with notes: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * شمارش تعداد علاقه‌مندی‌های یک رستوران
     * 
     * برای محاسبه امتیاز محبوبیت رستوران استفاده می‌شود
     * 
     * @param restaurant شیء رستوران
     * @return تعداد علاقه‌مندی‌ها
     */
    public Long countByRestaurant(Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f WHERE f.restaurant.id = :restaurantId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting favorites for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * شمارش تعداد علاقه‌مندی‌های یک کاربر
     * 
     * برای پروفایل کاربر و آمارهای شخصی استفاده می‌شود
     * 
     * @param user شیء کاربر
     * @return تعداد علاقه‌مندی‌ها
     */
    public Long countByUser(User user) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", user.getId());
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting favorites for user {}: {}", user.getId(), e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * حذف علاقه‌مندی بر اساس شناسه
     * 
     * @param id شناسه علاقه‌مندی
     * @return true در صورت حذف موفق، false در غیر این صورت
     */
    public boolean delete(Long id) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Favorite favorite = session.get(Favorite.class, id);
            if (favorite != null) {
                session.remove(favorite);
                logger.info("Deleted favorite with ID: {}", id);
                transaction.commit();
                return true;
            } else {
                logger.warn("Favorite with ID {} not found for deletion", id);
                transaction.rollback();
                return false;
            }
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting favorite {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * متد Legacy برای حذف علاقه‌مندی
     * 
     * ابتدا علاقه‌مندی را پیدا می‌کند سپس آن را حذف می‌کند
     * 
     * @param userId شناسه کاربر
     * @param restaurantId شناسه رستوران
     */
    public void delete(long userId, long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Optional<Favorite> favorite = find(userId, restaurantId);
            if (favorite.isPresent()) {
                delete(favorite.get().getId());
            }
        } catch (Exception e) {
            logger.error("Error deleting favorite for user {} and restaurant {}: {}", 
                        userId, restaurantId, e.getMessage(), e);
        }
    }

    /**
     * دریافت تمام علاقه‌مندی‌ها (برای اهداف مدیریتی)
     * 
     * این متد معمولاً توسط ادمین‌ها برای بررسی کل سیستم استفاده می‌شود
     * 
     * @return لیست تمام علاقه‌مندی‌ها
     */
    public List<Favorite> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding all favorites: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * دریافت علاقه‌مندی‌ها با صفحه‌بندی
     * 
     * برای بهبود عملکرد در صفحات با تعداد زیاد علاقه‌مندی
     * 
     * @param offset نقطه شروع (تعداد رکوردهای skip شده)
     * @param limit حداکثر تعداد رکوردهای برگشتی
     * @return لیست علاقه‌مندی‌ها با صفحه‌بندی
     */
    public List<Favorite> findWithPagination(int offset, int limit) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites with pagination: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * شمارش کل علاقه‌مندی‌ها
     * 
     * برای آمارهای کلی سیستم و محاسبه pagination
     * 
     * @return تعداد کل علاقه‌مندی‌ها
     */
    public Long countAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f";
            Query<Long> query = session.createQuery(hql, Long.class);
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting all favorites: {}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * متد Legacy برای پاکسازی کامل علاقه‌مندی‌ها
     * 
     * ⚠️ خطرناک: تمام علاقه‌مندی‌ها را حذف می‌کند
     * معمولاً فقط در تست‌ها استفاده می‌شود
     */
    public void clear() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery("DELETE FROM Favorite").executeUpdate();
            transaction.commit();
            logger.info("Cleared all favorites");
        } catch (Exception e) {
            logger.error("Error clearing favorites: {}", e.getMessage(), e);
        }
    }
} 
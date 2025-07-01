package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * لایه دسترسی به داده‌های اعلان‌ها (Repository Pattern)
 * 
 * این کلاس تمام عملیات دیتابیس مربوط به اعلان‌ها را مدیریت می‌کند:
 * 
 * === عملیات CRUD پایه ===
 * - ایجاد، خواندن، به‌روزرسانی، حذف اعلان‌ها
 * - مدیریت transaction ها و exception handling
 * - Retry mechanism برای عملیات ناموفق
 * 
 * === جستجوی پیشرفته ===
 * - جستجو بر اساس کاربر، نوع، اولویت
 * - فیلتر اعلان‌های خوانده نشده
 * - جستجوی اعلان‌های اخیر
 * - Pagination support
 * 
 * === عملیات گروهی ===
 * - علامت‌گذاری دسته‌ای به عنوان خوانده شده
 * - حذف منطقی اعلان‌های قدیمی
 * - حذف فیزیکی اعلان‌های منقضی
 * - ذخیره دسته‌ای اعلان‌ها
 * 
 * === آمار و گزارش ===
 * - تعداد اعلان‌های خوانده نشده
 * - آمار بر اساس نوع اعلان
 * - گزارش روزانه اعلان‌ها
 * - آخرین اعلان کاربر
 * 
 * === ویژگی‌های کلیدی ===
 * - Transaction Management: مدیریت امن تراکنش‌ها
 * - Error Handling: مدیریت پیشرفته خطاها
 * - Retry Logic: تکرار خودکار عملیات ناموفق
 * - Soft Delete Support: پشتیبانی از حذف منطقی
 * - Performance Optimization: بهینه‌سازی query ها
 * - Batch Operations: عملیات گروهی برای کارایی
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
public class NotificationRepository {

    // ==================== BASIC CRUD OPERATIONS ====================
    
    /**
     * ذخیره اعلان جدید در دیتابیس
     * 
     * از transaction management برای تضمین consistency استفاده می‌کند
     * در صورت خطا، transaction را rollback می‌کند
     * 
     * @param notification اعلان برای ذخیره
     * @return اعلان ذخیره شده با ID
     * @throws RuntimeException در صورت خطا در ذخیره‌سازی
     */
    public Notification save(Notification notification) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(notification);
            transaction.commit();
            return notification;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving notification: " + e.getMessage(), e);
        }
    }

    /**
     * جستجوی اعلان بر اساس شناسه
     * 
     * @param id شناسه اعلان
     * @return Optional حاوی اعلان یا empty
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public Optional<Notification> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Notification notification = session.get(Notification.class, id);
            return Optional.ofNullable(notification);
        } catch (Exception e) {
            throw new RuntimeException("Error finding notification by id: " + e.getMessage(), e);
        }
    }

    /**
     * به‌روزرسانی اعلان موجود
     * 
     * از retry mechanism برای مقابله با lock exception ها استفاده می‌کند
     * 
     * @param notification اعلان برای به‌روزرسانی
     * @return اعلان به‌روزرسانی شده
     * @throws RuntimeException در صورت شکست تمام تلاش‌ها
     */
    public Notification update(Notification notification) {
        return updateWithRetry(notification, 3);
    }

    /**
     * به‌روزرسانی با قابلیت تکرار خودکار
     * 
     * در صورت database lock، عملیات را دوباره تکرار می‌کند
     * از exponential backoff برای جلوگیری از فشار بیش از حد استفاده می‌کند
     * 
     * @param notification اعلان برای به‌روزرسانی
     * @param maxRetries حداکثر تعداد تلاش
     * @return اعلان به‌روزرسانی شده
     */
    private Notification updateWithRetry(Notification notification, int maxRetries) {
        Transaction transaction = null;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(notification);
            transaction.commit();
            return notification;
        } catch (Exception e) {
                lastException = e;
            if (transaction != null) {
                transaction.rollback();
            }
                
                // بررسی امکان تکرار عملیات
                if (isRetryableException(e) && attempt < maxRetries) {
                    try {
                        // Exponential backoff: 50ms, 100ms, 200ms
                        Thread.sleep(50 * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    continue;
                }
                break;
            }
        }
        
        throw new RuntimeException("Error updating notification after " + maxRetries + " attempts: " + lastException.getMessage(), lastException);
        }

    /**
     * بررسی امکان تکرار عملیات بر اساس نوع خطا
     * 
     * @param e exception رخ داده
     * @return true اگر عملیات قابل تکرار باشد
     */
    private boolean isRetryableException(Exception e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("database is locked") || 
               message.contains("sqlite_busy") ||
               message.contains("lockacquisitionexception");
    }

    /**
     * حذف فیزیکی اعلان از دیتابیس
     * 
     * توجه: معمولاً از softDelete استفاده کنید
     * 
     * @param notification اعلان برای حذف
     * @throws RuntimeException در صورت خطا در حذف
     */
    public void delete(Notification notification) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(notification) ? notification : session.merge(notification));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting notification: " + e.getMessage(), e);
        }
    }

    // ==================== FIND OPERATIONS ====================
    
    /**
     * دریافت تمام اعلان‌های یک کاربر
     * 
     * فقط اعلان‌های حذف نشده را برمی‌گرداند
     * نتایج بر اساس تاریخ ایجاد نزولی مرتب شده‌اند
     * 
     * @param userId شناسه کاربر
     * @return لیست اعلان‌های کاربر
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findByUserId(Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications by user id: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های کاربر با صفحه‌بندی
     * 
     * برای نمایش اعلان‌ها در قسمت‌های مختلف UI
     * 
     * @param userId شناسه کاربر
     * @param page شماره صفحه (شروع از 0)
     * @param size تعداد آیتم در هر صفحه
     * @return لیست اعلان‌های صفحه درخواستی
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findByUserIdPaginated(Long userId, int page, int size) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding paginated notifications: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های خوانده نشده کاربر
     * 
     * برای نمایش badge تعداد اعلان‌های جدید
     * 
     * @param userId شناسه کاربر
     * @return لیست اعلان‌های خوانده نشده
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findUnreadByUserId(Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding unread notifications: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های کاربر بر اساس نوع
     * 
     * @param userId شناسه کاربر
     * @param type نوع اعلان (ORDER_CREATED، PAYMENT_UPDATE، ...)
     * @return لیست اعلان‌های از نوع مشخص
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findByUserIdAndType(Long userId, NotificationType type) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications by type: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های کاربر بر اساس اولویت
     * 
     * @param userId شناسه کاربر
     * @param priority سطح اولویت (LOW، NORMAL، HIGH، URGENT)
     * @return لیست اعلان‌های با اولویت مشخص
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findByUserIdAndPriority(Long userId, NotificationPriority priority) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("priority", priority);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications by priority: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های فوری کاربر
     * 
     * فقط اعلان‌های با اولویت HIGH برمی‌گرداند
     * 
     * @param userId شناسه کاربر
     * @return لیست اعلان‌های فوری
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findHighPriorityByUserId(Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("priority", NotificationPriority.HIGH);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding high priority notifications: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های اخیر کاربر
     * 
     * @param userId شناسه کاربر
     * @param days تعداد روزهای گذشته
     * @return لیست اعلان‌های اخیر
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findRecentByUserId(Long userId, int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("since", since);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding recent notifications: " + e.getMessage(), e);
        }
    }

    // ==================== ENTITY-SPECIFIC QUERIES ====================
    
    /**
     * دریافت تمام اعلان‌های مربوط به یک سفارش
     * 
     * شامل اعلان‌های ثبت، تغییر وضعیت، و اختصاص پیک
     * 
     * @param orderId شناسه سفارش
     * @return لیست اعلان‌های مربوط به سفارش
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findOrderNotifications(Long orderId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.relatedEntityId = :orderId AND n.type IN (:orderTypes) AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("orderId", orderId);
            query.setParameter("orderTypes", List.of(NotificationType.ORDER_CREATED, NotificationType.ORDER_STATUS_CHANGED, NotificationType.DELIVERY_ASSIGNED));
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding order notifications: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های سفارش برای کاربر خاص
     * 
     * @param userId شناسه کاربر
     * @param orderId شناسه سفارش
     * @return لیست اعلان‌های سفارش کاربر
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findUserOrderNotifications(Long userId, Long orderId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.relatedEntityId = :orderId AND n.type IN (:orderTypes) AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("orderId", orderId);
            query.setParameter("orderTypes", List.of(NotificationType.ORDER_CREATED, NotificationType.ORDER_STATUS_CHANGED, NotificationType.DELIVERY_ASSIGNED));
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding user order notifications: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های مربوط به رستوران
     * 
     * @param restaurantId شناسه رستوران
     * @return لیست اعلان‌های رستوران
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findRestaurantNotifications(Long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.relatedEntityId = :restaurantId AND n.type = :type AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("restaurantId", restaurantId);
            query.setParameter("type", NotificationType.RESTAURANT_APPROVED);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding restaurant notifications: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت اعلان‌های مربوط به تحویل
     * 
     * @param deliveryId شناسه تحویل
     * @return لیست اعلان‌های تحویل
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Notification> findDeliveryNotifications(Long deliveryId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.relatedEntityId = :deliveryId AND n.type = :type AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("deliveryId", deliveryId);
            query.setParameter("type", NotificationType.DELIVERY_ASSIGNED);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding delivery notifications: " + e.getMessage(), e);
        }
    }

    // ==================== BULK OPERATIONS ====================
    
    /**
     * علامت‌گذاری تمام اعلان‌های کاربر به عنوان خوانده شده
     * 
     * عملیات bulk update برای بهینه‌سازی performance
     * 
     * @param userId شناسه کاربر
     * @return تعداد اعلان‌های به‌روزرسانی شده
     * @throws RuntimeException در صورت خطا در به‌روزرسانی
     */
    public int markAllAsReadForUser(Long userId) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false";
            Query<?> query = session.createQuery(hql);
            query.setParameter("readAt", LocalDateTime.now());
            query.setParameter("userId", userId);
            int result = query.executeUpdate();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error marking all notifications as read: " + e.getMessage(), e);
        }
    }

    /**
     * علامت‌گذاری اعلان‌های نوع خاص به عنوان خوانده شده
     * 
     * @param userId شناسه کاربر
     * @param type نوع اعلان
     * @return تعداد اعلان‌های به‌روزرسانی شده
     * @throws RuntimeException در صورت خطا در به‌روزرسانی
     */
    public int markAsReadByType(Long userId, NotificationType type) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            String hql = "UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.type = :type AND n.isRead = false AND n.isDeleted = false";
            Query<?> query = session.createQuery(hql);
            query.setParameter("readAt", LocalDateTime.now());
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            int result = query.executeUpdate();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error marking notifications as read by type: " + e.getMessage(), e);
        }
    }

    /**
     * حذف منطقی اعلان‌های قدیمی
     * 
     * اعلان‌ها را حذف نمی‌کند بلکه فقط علامت حذف می‌زند
     * برای نگهداری تاریخچه و compliance
     * 
     * @param daysOld تعداد روزهای قدیمی بودن
     * @return تعداد اعلان‌های حذف شده
     * @throws RuntimeException در صورت خطا در حذف
     */
    public int softDeleteOldNotifications(int daysOld) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            String hql = "UPDATE Notification n SET n.isDeleted = true, n.deletedAt = :deletedAt WHERE n.createdAt < :cutoffDate AND n.isDeleted = false";
            Query<?> query = session.createQuery(hql);
            query.setParameter("deletedAt", LocalDateTime.now());
            query.setParameter("cutoffDate", cutoffDate);
            int result = query.executeUpdate();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error soft deleting old notifications: " + e.getMessage(), e);
        }
    }

    /**
     * حذف فیزیکی اعلان‌های منقضی
     * 
     * اعلان‌هایی که قبلاً soft delete شده‌اند را کاملاً حذف می‌کند
     * برای بهینه‌سازی فضای دیتابیس
     * 
     * @param daysOld تعداد روزهای انقضا
     * @return تعداد اعلان‌های حذف شده
     * @throws RuntimeException در صورت خطا در حذف
     */
    public int hardDeleteOldNotifications(int daysOld) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            String hql = "DELETE FROM Notification n WHERE n.deletedAt < :cutoffDate AND n.isDeleted = true";
            Query<?> query = session.createQuery(hql);
            query.setParameter("cutoffDate", cutoffDate);
            int result = query.executeUpdate();
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error hard deleting old notifications: " + e.getMessage(), e);
        }
    }

    // ==================== STATISTICS AND COUNTS ====================
    
    /**
     * دریافت تعداد اعلان‌های خوانده نشده کاربر
     * 
     * @param userId شناسه کاربر
     * @return تعداد اعلان‌های خوانده نشده
     * @throws RuntimeException در صورت خطا در شمارش
     */
    public long getUnreadCount(Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error getting unread count: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت تعداد اعلان‌های کاربر بر اساس نوع
     * 
     * @param userId شناسه کاربر
     * @param type نوع اعلان
     * @return تعداد اعلان‌های نوع مشخص
     * @throws RuntimeException در صورت خطا در شمارش
     */
    public long getNotificationCountByType(Long userId, NotificationType type) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.isDeleted = false";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error getting notification count by type: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت تعداد اعلان‌های فوری خوانده نشده
     * 
     * @param userId شناسه کاربر
     * @return تعداد اعلان‌های فوری خوانده نشده
     * @throws RuntimeException در صورت خطا در شمارش
     */
    public long getHighPriorityUnreadCount(Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isRead = false AND n.isDeleted = false";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", userId);
            query.setParameter("priority", NotificationPriority.HIGH);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error getting high priority unread count: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت آخرین اعلان کاربر
     * 
     * @param userId شناسه کاربر
     * @return Optional حاوی آخرین اعلان یا empty
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public Optional<Notification> getLatestNotification(Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setMaxResults(1);
            List<Notification> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Error getting latest notification: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت آمار اعلان‌ها بر اساس نوع
     * 
     * نتیجه: [نوع، تعداد کل، تعداد خوانده نشده]
     * 
     * @param userId شناسه کاربر
     * @return لیست آرایه‌های آماری
     * @throws RuntimeException در صورت خطا در محاسبه آمار
     */
    public List<Object[]> getNotificationStatsByType(Long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT n.type, COUNT(n), SUM(CASE WHEN n.isRead = false THEN 1 ELSE 0 END) FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false GROUP BY n.type";
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error getting notification stats by type: " + e.getMessage(), e);
        }
    }

    /**
     * دریافت آمار روزانه اعلان‌ها
     * 
     * نتیجه: [تاریخ، تعداد اعلان‌ها]
     * 
     * @param userId شناسه کاربر
     * @param days تعداد روزهای گذشته
     * @return لیست آمار روزانه
     * @throws RuntimeException در صورت خطا در محاسبه آمار
     */
    public List<Object[]> getDailyNotificationCounts(Long userId, int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime since = LocalDateTime.now().minusDays(days);
            String hql = "SELECT DATE(n.createdAt), COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :since AND n.isDeleted = false GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt) DESC";
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("userId", userId);
            query.setParameter("since", since);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error getting daily notification counts: " + e.getMessage(), e);
        }
    }

    // ==================== BROADCAST OPERATIONS ====================
    
    /**
     * دریافت شناسه تمام کاربران فعال
     * 
     * برای ارسال اعلان‌های عمومی (broadcast)
     * 
     * @return لیست شناسه کاربران فعال
     * @throws RuntimeException در صورت خطا در جستجو
     */
    public List<Long> getAllActiveUserIds() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT u.id FROM User u WHERE u.isActive = true";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error getting all active user ids: " + e.getMessage(), e);
        }
    }

    /**
     * ذخیره دسته‌ای اعلان‌ها
     * 
     * برای بهینه‌سازی performance هنگام ارسال اعلان‌های گروهی
     * از batch processing برای کاهش تعداد round-trip ها استفاده می‌کند
     * 
     * @param notifications لیست اعلان‌ها برای ذخیره
     * @throws RuntimeException در صورت خطا در ذخیره‌سازی
     */
    public void saveBatch(List<Notification> notifications) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int batchSize = 50; // بهینه‌سازی برای SQLite
            for (int i = 0; i < notifications.size(); i++) {
                session.persist(notifications.get(i));
                if (i % batchSize == 0 && i > 0) {
                    // flush و clear برای جلوگیری از OutOfMemory
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving batch notifications: " + e.getMessage(), e);
        }
    }
} 

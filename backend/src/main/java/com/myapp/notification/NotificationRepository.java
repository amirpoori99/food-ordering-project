package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class NotificationRepository {
    private final SessionFactory sessionFactory;

    public NotificationRepository() {
        this.sessionFactory = DatabaseUtil.getSessionFactory();
    }

    // Constructor for testing with custom SessionFactory
    public NotificationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // Basic CRUD operations
    public Notification save(Notification notification) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(notification);
            session.getTransaction().commit();
            return notification;
        } catch (Exception e) {
            throw new RuntimeException("Error saving notification: " + e.getMessage(), e);
        }
    }

    public Optional<Notification> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Notification notification = session.get(Notification.class, id);
            return Optional.ofNullable(notification);
        } catch (Exception e) {
            throw new RuntimeException("Error finding notification by id: " + e.getMessage(), e);
        }
    }

    public Notification update(Notification notification) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(notification);
            session.getTransaction().commit();
            return notification;
        } catch (Exception e) {
            throw new RuntimeException("Error updating notification: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Notification notification = session.get(Notification.class, id);
            if (notification != null) {
                session.remove(notification);
            }
            session.getTransaction().commit();
        } catch (Exception e) {
            throw new RuntimeException("Error deleting notification: " + e.getMessage(), e);
        }
    }

    // Advanced queries for user notifications
    public List<Notification> findByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications for user: " + e.getMessage(), e);
        }
    }

    public List<Notification> findByUserIdPaginated(Long userId, int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding paginated notifications: " + e.getMessage(), e);
        }
    }

    public List<Notification> findUnreadByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false ORDER BY n.priority DESC, n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding unread notifications: " + e.getMessage(), e);
        }
    }

    public List<Notification> findByUserIdAndType(Long userId, NotificationType type) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications by type: " + e.getMessage(), e);
        }
    }

    public List<Notification> findByUserIdAndPriority(Long userId, NotificationPriority priority) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("priority", priority);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications by priority: " + e.getMessage(), e);
        }
    }

    public List<Notification> findHighPriorityByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.priority IN ('HIGH', 'URGENT') AND n.isDeleted = false ORDER BY n.priority DESC, n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding high priority notifications: " + e.getMessage(), e);
        }
    }

    // Order-related notifications
    public List<Notification> findByOrderId(Long orderId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.orderId = :orderId AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications for order: " + e.getMessage(), e);
        }
    }

    public List<Notification> findByUserIdAndOrderId(Long userId, Long orderId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.orderId = :orderId AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding user notifications for order: " + e.getMessage(), e);
        }
    }

    // Restaurant-related notifications
    public List<Notification> findByRestaurantId(Long restaurantId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.restaurantId = :restaurantId AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("restaurantId", restaurantId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications for restaurant: " + e.getMessage(), e);
        }
    }

    // Delivery-related notifications
    public List<Notification> findByDeliveryId(Long deliveryId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.deliveryId = :deliveryId AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("deliveryId", deliveryId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications for delivery: " + e.getMessage(), e);
        }
    }

    // Date range queries
    public List<Notification> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.createdAt BETWEEN :startDate AND :endDate AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding notifications by date range: " + e.getMessage(), e);
        }
    }

    public List<Notification> findRecentByUserId(Long userId, int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return findByUserIdAndDateRange(userId, cutoffDate, LocalDateTime.now());
    }

    // Bulk operations
    public int markAllAsReadForUser(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Query<?> query = session.createQuery(
                "UPDATE Notification SET isRead = true, readAt = :readAt WHERE userId = :userId AND isRead = false");
            query.setParameter("readAt", LocalDateTime.now());
            query.setParameter("userId", userId);
            int updatedCount = query.executeUpdate();
            session.getTransaction().commit();
            return updatedCount;
        } catch (Exception e) {
            throw new RuntimeException("Error marking all notifications as read: " + e.getMessage(), e);
        }
    }

    public int markAsReadByType(Long userId, NotificationType type) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Query<?> query = session.createQuery(
                "UPDATE Notification SET isRead = true, readAt = :readAt WHERE userId = :userId AND type = :type AND isRead = false");
            query.setParameter("readAt", LocalDateTime.now());
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            int updatedCount = query.executeUpdate();
            session.getTransaction().commit();
            return updatedCount;
        } catch (Exception e) {
            throw new RuntimeException("Error marking notifications as read by type: " + e.getMessage(), e);
        }
    }

    public int softDeleteOldNotifications(int daysOld) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            Query<?> query = session.createQuery(
                "UPDATE Notification SET isDeleted = true WHERE createdAt < :cutoffDate AND isDeleted = false");
            query.setParameter("cutoffDate", cutoffDate);
            int deletedCount = query.executeUpdate();
            session.getTransaction().commit();
            return deletedCount;
        } catch (Exception e) {
            throw new RuntimeException("Error soft deleting old notifications: " + e.getMessage(), e);
        }
    }

    public int hardDeleteOldNotifications(int daysOld) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
            Query<?> query = session.createQuery(
                "DELETE FROM Notification WHERE createdAt < :cutoffDate AND isDeleted = true");
            query.setParameter("cutoffDate", cutoffDate);
            int deletedCount = query.executeUpdate();
            session.getTransaction().commit();
            return deletedCount;
        } catch (Exception e) {
            throw new RuntimeException("Error hard deleting old notifications: " + e.getMessage(), e);
        }
    }

    // Analytics and statistics
    public long countUnreadByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false",
                Long.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting unread notifications: " + e.getMessage(), e);
        }
    }

    public long countByUserIdAndType(Long userId, NotificationType type) {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.isDeleted = false",
                Long.class);
            query.setParameter("userId", userId);
            query.setParameter("type", type);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting notifications by type: " + e.getMessage(), e);
        }
    }

    public long countHighPriorityUnread(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.priority IN ('HIGH', 'URGENT') AND n.isRead = false AND n.isDeleted = false",
                Long.class);
            query.setParameter("userId", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting high priority unread notifications: " + e.getMessage(), e);
        }
    }

    public List<Object[]> getNotificationStatsByType(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Object[]> query = session.createQuery(
                "SELECT n.type, COUNT(n), SUM(CASE WHEN n.isRead = false THEN 1 ELSE 0 END) " +
                "FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false GROUP BY n.type",
                Object[].class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error getting notification stats by type: " + e.getMessage(), e);
        }
    }

    public List<Object[]> getDailyNotificationCounts(Long userId, int days) {
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);
            Query<Object[]> query = session.createQuery(
                "SELECT DATE(n.createdAt), COUNT(n) " +
                "FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :startDate AND n.isDeleted = false " +
                "GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt) DESC",
                Object[].class);
            query.setParameter("userId", userId);
            query.setParameter("startDate", startDate);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error getting daily notification counts: " + e.getMessage(), e);
        }
    }

    // Utility methods
    public boolean existsUnreadHighPriority(Long userId) {
        return countHighPriorityUnread(userId) > 0;
    }

    public Optional<Notification> findLatestByUserId(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC",
                Notification.class);
            query.setParameter("userId", userId);
            query.setMaxResults(1);
            List<Notification> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Error finding latest notification: " + e.getMessage(), e);
        }
    }

    public List<Notification> findExpiredNotifications(int expirationDays) {
        try (Session session = sessionFactory.openSession()) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(expirationDays);
            Query<Notification> query = session.createQuery(
                "FROM Notification n WHERE n.createdAt < :cutoffDate AND n.isDeleted = false",
                Notification.class);
            query.setParameter("cutoffDate", cutoffDate);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding expired notifications: " + e.getMessage(), e);
        }
    }
} 
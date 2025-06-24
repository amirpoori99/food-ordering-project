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

public class NotificationRepository {

    // Basic CRUD operations
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

    public Optional<Notification> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Notification notification = session.get(Notification.class, id);
            return Optional.ofNullable(notification);
        } catch (Exception e) {
            throw new RuntimeException("Error finding notification by id: " + e.getMessage(), e);
        }
    }

    public Notification update(Notification notification) {
        return updateWithRetry(notification, 3);
    }

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
                
                // Check if it's a lock exception and we can retry
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

    private boolean isRetryableException(Exception e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("database is locked") || 
               message.contains("sqlite_busy") ||
               message.contains("lockacquisitionexception");
    }

    public void delete(Notification notification) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(notification);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting notification: " + e.getMessage(), e);
        }
    }

    // Find operations
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

    public List<Notification> findOrderNotifications(Long orderId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.relatedEntityId = :orderId AND n.type IN (:orderTypes) AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("orderId", orderId);
            query.setParameterList("orderTypes", List.of(NotificationType.ORDER_CREATED, NotificationType.ORDER_STATUS_CHANGED, NotificationType.DELIVERY_ASSIGNED));
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding order notifications: " + e.getMessage(), e);
        }
    }

    public List<Notification> findUserOrderNotifications(Long userId, Long orderId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Notification n WHERE n.userId = :userId AND n.relatedEntityId = :orderId AND n.type IN (:orderTypes) AND n.isDeleted = false ORDER BY n.createdAt DESC";
            Query<Notification> query = session.createQuery(hql, Notification.class);
            query.setParameter("userId", userId);
            query.setParameter("orderId", orderId);
            query.setParameterList("orderTypes", List.of(NotificationType.ORDER_CREATED, NotificationType.ORDER_STATUS_CHANGED, NotificationType.DELIVERY_ASSIGNED));
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error finding user order notifications: " + e.getMessage(), e);
        }
    }

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

    // Bulk operations
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

    // Statistics and counts
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

    // Broadcast operations
    public List<Long> getAllActiveUserIds() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT u.id FROM User u WHERE u.isActive = true";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error getting all active user ids: " + e.getMessage(), e);
        }
    }

    public void saveBatch(List<Notification> notifications) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int batchSize = 50;
            for (int i = 0; i < notifications.size(); i++) {
                session.persist(notifications.get(i));
                if (i % batchSize == 0 && i > 0) {
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
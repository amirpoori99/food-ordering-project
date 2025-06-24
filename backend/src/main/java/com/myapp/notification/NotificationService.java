package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
import com.myapp.auth.AuthRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final AuthRepository authRepository;

    public NotificationService(NotificationRepository notificationRepository, AuthRepository authRepository) {
        this.notificationRepository = notificationRepository;
        this.authRepository = authRepository;
    }

    // Basic notification operations
    public Notification createNotification(Long userId, String title, String message, NotificationType type) {
        validateUserId(userId);
        validateNotificationContent(title, message);
        
        Notification notification = new Notification(userId, title, message, type);
        return notificationRepository.save(notification);
    }

    public Notification createNotification(Long userId, String title, String message, NotificationType type, NotificationPriority priority) {
        validateUserId(userId);
        validateNotificationContent(title, message);
        
        Notification notification = new Notification(userId, title, message, type, priority);
        return notificationRepository.save(notification);
    }

    public Optional<Notification> getNotificationById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Notification ID cannot be null or negative");
        }
        return notificationRepository.findById(id);
    }

    public List<Notification> getUserNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> getUserNotificationsPaginated(Long userId, int page, int size) {
        validateUserId(userId);
        validatePaginationParams(page, size);
        return notificationRepository.findByUserIdPaginated(userId, page, size);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.findUnreadByUserId(userId);
    }

    public List<Notification> getNotificationsByType(Long userId, NotificationType type) {
        validateUserId(userId);
        validateNotificationType(type);
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    public List<Notification> getNotificationsByPriority(Long userId, NotificationPriority priority) {
        validateUserId(userId);
        validateNotificationPriority(priority);
        return notificationRepository.findByUserIdAndPriority(userId, priority);
    }

    public List<Notification> getHighPriorityNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.findHighPriorityByUserId(userId);
    }

    public List<Notification> getRecentNotifications(Long userId, int days) {
        validateUserId(userId);
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.findRecentByUserId(userId, days);
    }

    // Notification state management
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.markAsRead();
        return notificationRepository.update(notification);
    }

    public Notification markAsUnread(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.markAsUnread();
        return notificationRepository.update(notification);
    }

    public void deleteNotification(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.softDelete();
        notificationRepository.update(notification);
    }

    public void restoreNotification(Long notificationId) {
        Optional<Notification> optNotification = notificationRepository.findById(notificationId);
        if (optNotification.isEmpty()) {
            throw new RuntimeException("Notification not found with id: " + notificationId);
        }
        
        Notification notification = optNotification.get();
        notification.restore();
        notificationRepository.update(notification);
    }

    // Bulk operations
    public int markAllAsRead(Long userId) {
        validateUserId(userId);
        return notificationRepository.markAllAsReadForUser(userId);
    }

    public int markAllAsReadByType(Long userId, NotificationType type) {
        validateUserId(userId);
        validateNotificationType(type);
        return notificationRepository.markAsReadByType(userId, type);
    }

    public int cleanupOldNotifications(int daysOld) {
        if (daysOld <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.softDeleteOldNotifications(daysOld);
    }

    public int purgeOldNotifications(int daysOld) {
        if (daysOld <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.hardDeleteOldNotifications(daysOld);
    }

    // Factory methods for creating specific notification types
    public Notification notifyOrderCreated(Long userId, Long orderId, String restaurantName) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (restaurantName == null || restaurantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or empty");
        }

        Notification notification = Notification.orderCreated(userId, orderId, restaurantName);
        return notificationRepository.save(notification);
    }

    public Notification notifyOrderStatusChanged(Long userId, Long orderId, OrderStatus newStatus) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        Notification notification = Notification.orderStatusChanged(userId, orderId, newStatus);
        return notificationRepository.save(notification);
    }

    public Notification notifyDeliveryAssigned(Long userId, Long orderId, Long deliveryId, String courierName) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (deliveryId == null || deliveryId <= 0) {
            throw new IllegalArgumentException("Delivery ID cannot be null or negative");
        }
        if (courierName == null || courierName.trim().isEmpty()) {
            throw new IllegalArgumentException("Courier name cannot be null or empty");
        }

        Notification notification = Notification.deliveryAssigned(userId, orderId, deliveryId, courierName);
        return notificationRepository.save(notification);
    }

    public Notification notifyPaymentProcessed(Long userId, Long orderId, Double amount, boolean success) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Notification notification = Notification.paymentProcessed(userId, orderId, amount, success);
        return notificationRepository.save(notification);
    }

    public Notification notifyRestaurantApproved(Long userId, Long restaurantId, String restaurantName) {
        validateUserId(userId);
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID cannot be null or negative");
        }
        if (restaurantName == null || restaurantName.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be null or empty");
        }

        Notification notification = Notification.restaurantApproved(userId, restaurantId, restaurantName);
        return notificationRepository.save(notification);
    }

    public Notification notifySystemMaintenance(Long userId, LocalDateTime maintenanceTime) {
        validateUserId(userId);
        if (maintenanceTime == null) {
            throw new IllegalArgumentException("Maintenance time cannot be null");
        }

        Notification notification = Notification.systemMaintenance(userId, maintenanceTime);
        return notificationRepository.save(notification);
    }

    // Broadcast notifications
    public void broadcastSystemMaintenance(LocalDateTime maintenanceTime) {
        if (maintenanceTime == null) {
            throw new IllegalArgumentException("Maintenance time cannot be null");
        }

        // Get all active users and create notifications in batch
        List<Long> activeUserIds = notificationRepository.getAllActiveUserIds();
        List<Notification> notifications = new java.util.ArrayList<>();
        
        for (Long userId : activeUserIds) {
            try {
                Notification notification = Notification.systemMaintenance(userId, maintenanceTime);
                notifications.add(notification);
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Failed to create maintenance notification for user " + userId + ": " + e.getMessage());
            }
        }
        
        // Save all notifications in batch
        if (!notifications.isEmpty()) {
            notificationRepository.saveBatch(notifications);
        }
    }

    public void broadcastPromotionalMessage(String title, String message, NotificationPriority priority) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (priority == null) {
            priority = NotificationPriority.LOW;
        }

        // Get all active users and create notifications in batch
        List<Long> activeUserIds = notificationRepository.getAllActiveUserIds();
        List<Notification> notifications = new java.util.ArrayList<>();
        
        for (Long userId : activeUserIds) {
            try {
                Notification notification = new Notification(userId, title, message, NotificationType.PROMOTIONAL, priority);
                notifications.add(notification);
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Failed to create promotional notification for user " + userId + ": " + e.getMessage());
            }
        }
        
        // Save all notifications in batch
        if (!notifications.isEmpty()) {
            notificationRepository.saveBatch(notifications);
        }
    }

    // Analytics and statistics
    public long getUnreadCount(Long userId) {
        validateUserId(userId);
        return notificationRepository.getUnreadCount(userId);
    }

    public long getNotificationCountByType(Long userId, NotificationType type) {
        validateUserId(userId);
        validateNotificationType(type);
        return notificationRepository.getNotificationCountByType(userId, type);
    }

    public long getHighPriorityUnreadCount(Long userId) {
        validateUserId(userId);
        return notificationRepository.getHighPriorityUnreadCount(userId);
    }

    public boolean hasUnreadHighPriorityNotifications(Long userId) {
        validateUserId(userId);
        return notificationRepository.getHighPriorityUnreadCount(userId) > 0;
    }

    public Optional<Notification> getLatestNotification(Long userId) {
        validateUserId(userId);
        return notificationRepository.getLatestNotification(userId);
    }

    public List<Object[]> getNotificationStatsByType(Long userId) {
        validateUserId(userId);
        return notificationRepository.getNotificationStatsByType(userId);
    }

    public List<Object[]> getDailyNotificationCounts(Long userId, int days) {
        validateUserId(userId);
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }
        return notificationRepository.getDailyNotificationCounts(userId, days);
    }

    // Order-related queries
    public List<Notification> getOrderNotifications(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        return notificationRepository.findOrderNotifications(orderId);
    }

    public List<Notification> getUserOrderNotifications(Long userId, Long orderId) {
        validateUserId(userId);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("Order ID cannot be null or negative");
        }
        return notificationRepository.findUserOrderNotifications(userId, orderId);
    }

    // Restaurant-related queries
    public List<Notification> getRestaurantNotifications(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID cannot be null or negative");
        }
        return notificationRepository.findRestaurantNotifications(restaurantId);
    }

    // Delivery-related queries
    public List<Notification> getDeliveryNotifications(Long deliveryId) {
        if (deliveryId == null || deliveryId <= 0) {
            throw new IllegalArgumentException("Delivery ID cannot be null or negative");
        }
        return notificationRepository.findDeliveryNotifications(deliveryId);
    }

    // Scheduled maintenance operations
    public void performDailyMaintenance() {
        try {
            // Soft delete notifications older than 90 days
            int softDeleted = cleanupOldNotifications(90);
            System.out.println("Daily maintenance: Soft deleted " + softDeleted + " old notifications");
            
            // Hard delete notifications that were soft deleted more than 30 days ago
            int hardDeleted = purgeOldNotifications(120);
            System.out.println("Daily maintenance: Hard deleted " + hardDeleted + " old notifications");
            
        } catch (Exception e) {
            System.err.println("Error during daily maintenance: " + e.getMessage());
            throw e;
        }
    }

    // Validation methods
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
        
        // Check if user exists
        if (authRepository.findById(userId).isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
    }

    private void validateNotificationContent(String title, String message) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification title cannot be null or empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Notification title cannot exceed 100 characters");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message cannot be null or empty");
        }
        if (message.length() > 500) {
            throw new IllegalArgumentException("Notification message cannot exceed 500 characters");
        }
    }

    private void validateNotificationType(NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
    }

    private void validateNotificationPriority(NotificationPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Notification priority cannot be null");
        }
    }

    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed 100");
        }
    }
} 
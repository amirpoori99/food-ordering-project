package com.myapp.notification;

import com.myapp.common.TestDatabaseManager;
import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationRepositoryTest {

    private SessionFactory sessionFactory;
    private NotificationRepository notificationRepository;
    @BeforeAll
    void setUpAll() {
        sessionFactory = com.myapp.common.utils.DatabaseUtil.getSessionFactory();
        notificationRepository = new NotificationRepository(sessionFactory);
    }

    @BeforeEach
    void setUp() {
        com.myapp.common.TestDatabaseManager.cleanAllTestData();
    }

    @AfterAll
    void tearDownAll() {
        // DatabaseUtil handles session factory lifecycle
    }

    // ====== Basic CRUD Tests ======
    @Test
    @DisplayName("Save notification - Success")
    void testSaveNotification() {
        // Given
        Notification notification = new Notification(
            1L, "Test Title", "Test Message", NotificationType.ORDER_UPDATE);

        // When
        Notification saved = notificationRepository.save(notification);

        // Then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Title", saved.getTitle());
        assertEquals("Test Message", saved.getMessage());
        assertEquals(NotificationType.ORDER_UPDATE, saved.getType());
        assertEquals(NotificationPriority.NORMAL, saved.getPriority());
        assertFalse(saved.getIsRead());
        assertFalse(saved.getIsDeleted());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Find notification by ID - Success")
    void testFindById() {
        // Given
        Notification notification = new Notification(
            1L, "Test Title", "Test Message", NotificationType.ORDER_UPDATE);
        Notification saved = notificationRepository.save(notification);

        // When
        Optional<Notification> found = notificationRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Test Title", found.get().getTitle());
    }

    @Test
    @DisplayName("Find notification by non-existent ID - Should return empty")
    void testFindByIdNotFound() {
        // When
        Optional<Notification> found = notificationRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Update notification - Success")
    void testUpdateNotification() {
        // Given
        Notification notification = new Notification(
            1L, "Original Title", "Original Message", NotificationType.ORDER_UPDATE);
        Notification saved = notificationRepository.save(notification);

        // When
        saved.setTitle("Updated Title");
        saved.markAsRead();
        Notification updated = notificationRepository.update(saved);

        // Then
        assertEquals("Updated Title", updated.getTitle());
        assertTrue(updated.getIsRead());
        assertNotNull(updated.getReadAt());
    }

    @Test
    @DisplayName("Delete notification - Success")
    void testDeleteNotification() {
        // Given
        Notification notification = new Notification(
            1L, "Test Title", "Test Message", NotificationType.ORDER_UPDATE);
        Notification saved = notificationRepository.save(notification);

        // When
        notificationRepository.delete(saved.getId());

        // Then
        Optional<Notification> found = notificationRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    // ====== User Query Tests ======
    @Test
    @DisplayName("Find notifications by user ID - Success")
    void testFindByUserId() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Title 1", "Message 1", NotificationType.ORDER_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Title 2", "Message 2", NotificationType.PAYMENT_UPDATE));
        notificationRepository.save(new Notification(
            2L, "Other User", "Message 3", NotificationType.ORDER_UPDATE));

        // When
        List<Notification> notifications = notificationRepository.findByUserId(userId);

        // Then
        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> userId.equals(n.getUserId())));
        assertEquals("Title 2", notifications.get(0).getTitle()); // Most recent first
    }

    @Test
    @DisplayName("Find notifications by user ID paginated - Success")
    void testFindByUserIdPaginated() {
        // Given
        Long userId = 1L;
        for (int i = 1; i <= 5; i++) {
            notificationRepository.save(new Notification(
                userId, "Title " + i, "Message " + i, NotificationType.ORDER_UPDATE));
        }

        // When
        List<Notification> page1 = notificationRepository.findByUserIdPaginated(userId, 0, 2);
        List<Notification> page2 = notificationRepository.findByUserIdPaginated(userId, 1, 2);

        // Then
        assertEquals(2, page1.size());
        assertEquals(2, page2.size());
        assertEquals("Title 5", page1.get(0).getTitle()); // Most recent first
        assertEquals("Title 4", page1.get(1).getTitle());
        assertEquals("Title 3", page2.get(0).getTitle());
        assertEquals("Title 2", page2.get(1).getTitle());
    }

    @Test
    @DisplayName("Find unread notifications by user ID - Success")
    void testFindUnreadByUserId() {
        // Given
        Long userId = 1L;
        Notification n1 = notificationRepository.save(new Notification(
            userId, "Unread", "Message 1", NotificationType.ORDER_UPDATE));
        Notification n2 = notificationRepository.save(new Notification(
            userId, "Read", "Message 2", NotificationType.PAYMENT_UPDATE));
        
        n2.markAsRead();
        notificationRepository.update(n2);

        // When
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);

        // Then
        assertEquals(1, unreadNotifications.size());
        assertEquals("Unread", unreadNotifications.get(0).getTitle());
    }

    @Test
    @DisplayName("Find notifications by user ID and type - Success")
    void testFindByUserIdAndType() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Order 1", "Message 1", NotificationType.ORDER_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Payment 1", "Message 2", NotificationType.PAYMENT_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Order 2", "Message 3", NotificationType.ORDER_UPDATE));

        // When
        List<Notification> orderNotifications = notificationRepository.findByUserIdAndType(
            userId, NotificationType.ORDER_UPDATE);

        // Then
        assertEquals(2, orderNotifications.size());
        assertTrue(orderNotifications.stream().allMatch(n -> n.getType() == NotificationType.ORDER_UPDATE));
    }

    @Test
    @DisplayName("Find notifications by user ID and priority - Success")
    void testFindByUserIdAndPriority() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Normal", "Message 1", NotificationType.ORDER_UPDATE, NotificationPriority.NORMAL));
        notificationRepository.save(new Notification(
            userId, "High", "Message 2", NotificationType.PAYMENT_UPDATE, NotificationPriority.HIGH));
        notificationRepository.save(new Notification(
            userId, "High 2", "Message 3", NotificationType.ORDER_UPDATE, NotificationPriority.HIGH));

        // When
        List<Notification> highPriorityNotifications = notificationRepository.findByUserIdAndPriority(
            userId, NotificationPriority.HIGH);

        // Then
        assertEquals(2, highPriorityNotifications.size());
        assertTrue(highPriorityNotifications.stream().allMatch(n -> n.getPriority() == NotificationPriority.HIGH));
    }

    @Test
    @DisplayName("Find high priority notifications by user ID - Success")
    void testFindHighPriorityByUserId() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Normal", "Message 1", NotificationType.ORDER_UPDATE, NotificationPriority.NORMAL));
        notificationRepository.save(new Notification(
            userId, "High", "Message 2", NotificationType.PAYMENT_UPDATE, NotificationPriority.HIGH));
        notificationRepository.save(new Notification(
            userId, "Urgent", "Message 3", NotificationType.SYSTEM_UPDATE, NotificationPriority.URGENT));

        // When
        List<Notification> highPriorityNotifications = notificationRepository.findHighPriorityByUserId(userId);

        // Then
        assertEquals(2, highPriorityNotifications.size());
        assertTrue(highPriorityNotifications.stream().allMatch(n -> 
            n.getPriority() == NotificationPriority.HIGH || n.getPriority() == NotificationPriority.URGENT));
        assertEquals("Urgent", highPriorityNotifications.get(0).getTitle()); // Urgent comes first
    }

    // ====== Entity-specific Query Tests ======
    @Test
    @DisplayName("Find notifications by order ID - Success")
    void testFindByOrderId() {
        // Given
        Long orderId = 123L;
        Notification n1 = new Notification(1L, "Order Created", "Message 1", NotificationType.ORDER_UPDATE);
        n1.setOrderId(orderId);
        notificationRepository.save(n1);

        Notification n2 = new Notification(1L, "Order Confirmed", "Message 2", NotificationType.ORDER_UPDATE);
        n2.setOrderId(orderId);
        notificationRepository.save(n2);

        Notification n3 = new Notification(1L, "Other Order", "Message 3", NotificationType.ORDER_UPDATE);
        n3.setOrderId(456L);
        notificationRepository.save(n3);

        // When
        List<Notification> orderNotifications = notificationRepository.findByOrderId(orderId);

        // Then
        assertEquals(2, orderNotifications.size());
        assertTrue(orderNotifications.stream().allMatch(n -> orderId.equals(n.getOrderId())));
    }

    @Test
    @DisplayName("Find notifications by user ID and order ID - Success")
    void testFindByUserIdAndOrderId() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long orderId = 123L;

        Notification n1 = new Notification(userId1, "User1 Order", "Message 1", NotificationType.ORDER_UPDATE);
        n1.setOrderId(orderId);
        notificationRepository.save(n1);

        Notification n2 = new Notification(userId2, "User2 Order", "Message 2", NotificationType.ORDER_UPDATE);
        n2.setOrderId(orderId);
        notificationRepository.save(n2);

        // When
        List<Notification> user1OrderNotifications = notificationRepository.findByUserIdAndOrderId(userId1, orderId);

        // Then
        assertEquals(1, user1OrderNotifications.size());
        assertEquals(userId1, user1OrderNotifications.get(0).getUserId());
        assertEquals(orderId, user1OrderNotifications.get(0).getOrderId());
    }

    @Test
    @DisplayName("Find notifications by restaurant ID - Success")
    void testFindByRestaurantId() {
        // Given
        Long restaurantId = 789L;
        Notification n1 = new Notification(1L, "Restaurant Approved", "Message 1", NotificationType.RESTAURANT_UPDATE);
        n1.setRestaurantId(restaurantId);
        notificationRepository.save(n1);

        Notification n2 = new Notification(1L, "Other Restaurant", "Message 2", NotificationType.RESTAURANT_UPDATE);
        n2.setRestaurantId(999L);
        notificationRepository.save(n2);

        // When
        List<Notification> restaurantNotifications = notificationRepository.findByRestaurantId(restaurantId);

        // Then
        assertEquals(1, restaurantNotifications.size());
        assertEquals(restaurantId, restaurantNotifications.get(0).getRestaurantId());
    }

    @Test
    @DisplayName("Find notifications by delivery ID - Success")
    void testFindByDeliveryId() {
        // Given
        Long deliveryId = 456L;
        Notification n1 = new Notification(1L, "Delivery Assigned", "Message 1", NotificationType.DELIVERY_UPDATE);
        n1.setDeliveryId(deliveryId);
        notificationRepository.save(n1);

        Notification n2 = new Notification(1L, "Other Delivery", "Message 2", NotificationType.DELIVERY_UPDATE);
        n2.setDeliveryId(789L);
        notificationRepository.save(n2);

        // When
        List<Notification> deliveryNotifications = notificationRepository.findByDeliveryId(deliveryId);

        // Then
        assertEquals(1, deliveryNotifications.size());
        assertEquals(deliveryId, deliveryNotifications.get(0).getDeliveryId());
    }

    // ====== Date Range Query Tests ======
    @Test
    @DisplayName("Find notifications by user ID and date range - Success")
    void testFindByUserIdAndDateRange() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        Notification oldNotification = new Notification(userId, "Old", "Message 1", NotificationType.ORDER_UPDATE);
        oldNotification.setCreatedAt(yesterday.minusHours(1));
        notificationRepository.save(oldNotification);

        Notification recentNotification = new Notification(userId, "Recent", "Message 2", NotificationType.ORDER_UPDATE);
        notificationRepository.save(recentNotification);

        // When
        List<Notification> recentNotifications = notificationRepository.findByUserIdAndDateRange(
            userId, yesterday, tomorrow);

        // Then
        assertEquals(1, recentNotifications.size());
        assertEquals("Recent", recentNotifications.get(0).getTitle());
    }

    @Test
    @DisplayName("Find recent notifications by user ID - Success")
    void testFindRecentByUserId() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        Notification oldNotification = new Notification(userId, "Old", "Message 1", NotificationType.ORDER_UPDATE);
        oldNotification.setCreatedAt(now.minusDays(10));
        notificationRepository.save(oldNotification);

        Notification recentNotification = new Notification(userId, "Recent", "Message 2", NotificationType.ORDER_UPDATE);
        notificationRepository.save(recentNotification);

        // When
        List<Notification> recentNotifications = notificationRepository.findRecentByUserId(userId, 7);

        // Then
        assertEquals(1, recentNotifications.size());
        assertEquals("Recent", recentNotifications.get(0).getTitle());
    }

    // ====== Bulk Operations Tests ======
    @Test
    @DisplayName("Mark all as read for user - Success")
    void testMarkAllAsReadForUser() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Unread 1", "Message 1", NotificationType.ORDER_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Unread 2", "Message 2", NotificationType.PAYMENT_UPDATE));
        notificationRepository.save(new Notification(
            2L, "Other User", "Message 3", NotificationType.ORDER_UPDATE));

        // When
        int updatedCount = notificationRepository.markAllAsReadForUser(userId);

        // Then
        assertEquals(2, updatedCount);
        
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        assertEquals(0, unreadNotifications.size());
        
        List<Notification> otherUserUnread = notificationRepository.findUnreadByUserId(2L);
        assertEquals(1, otherUserUnread.size());
    }

    @Test
    @DisplayName("Mark as read by type - Success")
    void testMarkAsReadByType() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Order 1", "Message 1", NotificationType.ORDER_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Order 2", "Message 2", NotificationType.ORDER_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Payment 1", "Message 3", NotificationType.PAYMENT_UPDATE));

        // When
        int updatedCount = notificationRepository.markAsReadByType(userId, NotificationType.ORDER_UPDATE);

        // Then
        assertEquals(2, updatedCount);
        
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        assertEquals(1, unreadNotifications.size());
        assertEquals(NotificationType.PAYMENT_UPDATE, unreadNotifications.get(0).getType());
    }

    @Test
    @DisplayName("Soft delete old notifications - Success")
    void testSoftDeleteOldNotifications() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Notification oldNotification = new Notification(1L, "Old", "Message 1", NotificationType.ORDER_UPDATE);
        oldNotification.setCreatedAt(now.minusDays(100));
        notificationRepository.save(oldNotification);

        Notification recentNotification = new Notification(1L, "Recent", "Message 2", NotificationType.ORDER_UPDATE);
        notificationRepository.save(recentNotification);

        // When
        int deletedCount = notificationRepository.softDeleteOldNotifications(90);

        // Then
        assertEquals(1, deletedCount);
        
        List<Notification> userNotifications = notificationRepository.findByUserId(1L);
        assertEquals(1, userNotifications.size());
        assertEquals("Recent", userNotifications.get(0).getTitle());
    }

    @Test
    @DisplayName("Hard delete old notifications - Success")
    void testHardDeleteOldNotifications() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Notification oldNotification = new Notification(1L, "Old", "Message 1", NotificationType.ORDER_UPDATE);
        oldNotification.setCreatedAt(now.minusDays(100));
        oldNotification.softDelete();
        notificationRepository.save(oldNotification);

        Notification recentNotification = new Notification(1L, "Recent", "Message 2", NotificationType.ORDER_UPDATE);
        notificationRepository.save(recentNotification);

        // When
        int deletedCount = notificationRepository.hardDeleteOldNotifications(90);

        // Then
        assertEquals(1, deletedCount);
        
        Optional<Notification> found = notificationRepository.findById(oldNotification.getId());
        assertFalse(found.isPresent());
        
        Optional<Notification> recentFound = notificationRepository.findById(recentNotification.getId());
        assertTrue(recentFound.isPresent());
    }

    // ====== Analytics Tests ======
    @Test
    @DisplayName("Count unread notifications by user ID - Success")
    void testCountUnreadByUserId() {
        // Given
        Long userId = 1L;
        Notification n1 = notificationRepository.save(new Notification(
            userId, "Unread 1", "Message 1", NotificationType.ORDER_UPDATE));
        Notification n2 = notificationRepository.save(new Notification(
            userId, "Read", "Message 2", NotificationType.PAYMENT_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Unread 2", "Message 3", NotificationType.SYSTEM_UPDATE));
        
        n2.markAsRead();
        notificationRepository.update(n2);

        // When
        long unreadCount = notificationRepository.countUnreadByUserId(userId);

        // Then
        assertEquals(2, unreadCount);
    }

    @Test
    @DisplayName("Count notifications by user ID and type - Success")
    void testCountByUserIdAndType() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Order 1", "Message 1", NotificationType.ORDER_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Order 2", "Message 2", NotificationType.ORDER_UPDATE));
        notificationRepository.save(new Notification(
            userId, "Payment 1", "Message 3", NotificationType.PAYMENT_UPDATE));

        // When
        long orderCount = notificationRepository.countByUserIdAndType(userId, NotificationType.ORDER_UPDATE);
        long paymentCount = notificationRepository.countByUserIdAndType(userId, NotificationType.PAYMENT_UPDATE);

        // Then
        assertEquals(2, orderCount);
        assertEquals(1, paymentCount);
    }

    @Test
    @DisplayName("Count high priority unread notifications - Success")
    void testCountHighPriorityUnread() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Normal", "Message 1", NotificationType.ORDER_UPDATE, NotificationPriority.NORMAL));
        
        Notification highNotification = notificationRepository.save(new Notification(
            userId, "High", "Message 2", NotificationType.PAYMENT_UPDATE, NotificationPriority.HIGH));
        
        notificationRepository.save(new Notification(
            userId, "Urgent", "Message 3", NotificationType.SYSTEM_UPDATE, NotificationPriority.URGENT));
        
        // Mark high priority as read
        highNotification.markAsRead();
        notificationRepository.update(highNotification);

        // When
        long highPriorityUnreadCount = notificationRepository.countHighPriorityUnread(userId);

        // Then
        assertEquals(1, highPriorityUnreadCount); // Only urgent remains unread
    }

    @Test
    @DisplayName("Check if unread high priority notifications exist - Success")
    void testExistsUnreadHighPriority() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "Normal", "Message 1", NotificationType.ORDER_UPDATE, NotificationPriority.NORMAL));

        // When & Then
        assertFalse(notificationRepository.existsUnreadHighPriority(userId));

        // Given
        notificationRepository.save(new Notification(
            userId, "High", "Message 2", NotificationType.PAYMENT_UPDATE, NotificationPriority.HIGH));

        // When & Then
        assertTrue(notificationRepository.existsUnreadHighPriority(userId));
    }

    @Test
    @DisplayName("Find latest notification by user ID - Success")
    void testFindLatestByUserId() {
        // Given
        Long userId = 1L;
        notificationRepository.save(new Notification(
            userId, "First", "Message 1", NotificationType.ORDER_UPDATE));
        
        // Small delay to ensure different timestamps
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        
        Notification latest = notificationRepository.save(new Notification(
            userId, "Latest", "Message 2", NotificationType.PAYMENT_UPDATE));

        // When
        Optional<Notification> found = notificationRepository.findLatestByUserId(userId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(latest.getId(), found.get().getId());
        assertEquals("Latest", found.get().getTitle());
    }

    @Test
    @DisplayName("Find expired notifications - Success")
    void testFindExpiredNotifications() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Notification expiredNotification = new Notification(1L, "Expired", "Message 1", NotificationType.ORDER_UPDATE);
        expiredNotification.setCreatedAt(now.minusDays(100));
        notificationRepository.save(expiredNotification);

        Notification recentNotification = new Notification(1L, "Recent", "Message 2", NotificationType.ORDER_UPDATE);
        notificationRepository.save(recentNotification);

        // When
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(90);

        // Then
        assertEquals(1, expiredNotifications.size());
        assertEquals("Expired", expiredNotifications.get(0).getTitle());
    }

    // ====== Edge Cases and Error Handling ======
    @Test
    @DisplayName("Handle empty results gracefully")
    void testEmptyResults() {
        // When & Then
        assertTrue(notificationRepository.findByUserId(999L).isEmpty());
        assertTrue(notificationRepository.findUnreadByUserId(999L).isEmpty());
        assertTrue(notificationRepository.findByOrderId(999L).isEmpty());
        assertEquals(0, notificationRepository.countUnreadByUserId(999L));
        assertFalse(notificationRepository.existsUnreadHighPriority(999L));
        assertFalse(notificationRepository.findLatestByUserId(999L).isPresent());
    }

    @Test
    @DisplayName("Handle deleted notifications correctly")
    void testDeletedNotificationsFiltering() {
        // Given
        Long userId = 1L;
        Notification activeNotification = notificationRepository.save(new Notification(
            userId, "Active", "Message 1", NotificationType.ORDER_UPDATE));
        
        Notification deletedNotification = notificationRepository.save(new Notification(
            userId, "Deleted", "Message 2", NotificationType.ORDER_UPDATE));
        deletedNotification.softDelete();
        notificationRepository.update(deletedNotification);

        // When
        List<Notification> userNotifications = notificationRepository.findByUserId(userId);
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        long unreadCount = notificationRepository.countUnreadByUserId(userId);

        // Then
        assertEquals(1, userNotifications.size());
        assertEquals("Active", userNotifications.get(0).getTitle());
        
        assertEquals(1, unreadNotifications.size());
        assertEquals("Active", unreadNotifications.get(0).getTitle());
        
        assertEquals(1, unreadCount);
    }
} 
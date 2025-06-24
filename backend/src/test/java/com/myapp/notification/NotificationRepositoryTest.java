package com.myapp.notification;

import com.myapp.common.TestDatabaseManager;
import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;

import org.junit.jupiter.api.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationRepositoryTest {

    private static NotificationRepository notificationRepository;
    private static TestDatabaseManager testDatabaseManager;
    private static User testUser1, testUser2;

    @BeforeAll
    static void setUpClass() {
        testDatabaseManager = new TestDatabaseManager();
        testDatabaseManager.setupTestDatabase();
        notificationRepository = new NotificationRepository();
        
        // Create test users
        createTestUsers();
    }

    @AfterAll
    static void tearDownClass() {
        if (testDatabaseManager != null) {
            testDatabaseManager.cleanup();
        }
    }

    @BeforeEach
    void setUp() {
        testDatabaseManager.clearNotifications();
    }

    private static void createTestUsers() {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            testUser1 = new User();
            testUser1.setFullName("Test User 1");
            testUser1.setEmail("testuser1@example.com");
            testUser1.setPhone("1234567890");
            testUser1.setPasswordHash("password123");
            testUser1.setIsActive(true);
            session.persist(testUser1);
            
            testUser2 = new User();
            testUser2.setFullName("Test User 2");
            testUser2.setEmail("testuser2@example.com");
            testUser2.setPhone("0987654321");
            testUser2.setPasswordHash("password456");
            testUser2.setIsActive(true);
            session.persist(testUser2);
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperationsTest {

        @Test
        @Order(1)
        @DisplayName("Should save notification successfully")
        void shouldSaveNotificationSuccessfully() {
            // Given
            Notification notification = new Notification(
                testUser1.getId(), 
                "Test Title", 
                "Test Message", 
                NotificationType.ORDER_CREATED
            );

            // When
            Notification savedNotification = notificationRepository.save(notification);

            // Then
            assertNotNull(savedNotification);
            assertNotNull(savedNotification.getId());
            assertEquals("Test Title", savedNotification.getTitle());
            assertEquals("Test Message", savedNotification.getMessage());
            assertEquals(NotificationType.ORDER_CREATED, savedNotification.getType());
            assertEquals(testUser1.getId(), savedNotification.getUserId());
        }

        @Test
        @Order(2)
        @DisplayName("Should find notification by ID successfully")
        void shouldFindNotificationByIdSuccessfully() {
            // Given
            Notification notification = new Notification(
                testUser1.getId(), 
                "Find Test", 
                "Find Message", 
                NotificationType.ORDER_STATUS_CHANGED
            );
            Notification savedNotification = notificationRepository.save(notification);

            // When
            Optional<Notification> foundNotification = notificationRepository.findById(savedNotification.getId());

            // Then
            assertTrue(foundNotification.isPresent());
            assertEquals(savedNotification.getId(), foundNotification.get().getId());
            assertEquals("Find Test", foundNotification.get().getTitle());
        }

        @Test
        @Order(3)
        @DisplayName("Should update notification successfully")
        void shouldUpdateNotificationSuccessfully() {
            // Given
            Notification notification = new Notification(
                testUser1.getId(), 
                "Update Test", 
                "Update Message", 
                NotificationType.DELIVERY_ASSIGNED
            );
            Notification savedNotification = notificationRepository.save(notification);
            
            // When
            savedNotification.markAsRead();
            Notification updatedNotification = notificationRepository.update(savedNotification);

            // Then
            assertNotNull(updatedNotification);
            assertTrue(updatedNotification.isRead());
            assertNotNull(updatedNotification.getReadAt());
        }

        @Test
        @Order(4)
        @DisplayName("Should delete notification successfully")
        void shouldDeleteNotificationSuccessfully() {
            // Given
            Notification notification = new Notification(
                testUser1.getId(), 
                "Delete Test", 
                "Delete Message", 
                NotificationType.PROMOTIONAL
            );
            Notification savedNotification = notificationRepository.save(notification);

            // When
            notificationRepository.delete(savedNotification);

            // Then
            Optional<Notification> deletedNotification = notificationRepository.findById(savedNotification.getId());
            assertFalse(deletedNotification.isPresent());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperationsTest {

        @Test
        @DisplayName("Should find notifications by user ID")
        void shouldFindNotificationsByUserId() {
            // Given
            Notification notification1 = new Notification(
                testUser1.getId(), 
                "User Test 1", 
                "Message 1", 
                NotificationType.ORDER_CREATED
            );
            Notification notification2 = new Notification(
                testUser1.getId(), 
                "User Test 2", 
                "Message 2", 
                NotificationType.ORDER_STATUS_CHANGED
            );
            Notification notification3 = new Notification(
                testUser2.getId(), 
                "Other User", 
                "Message 3", 
                NotificationType.DELIVERY_ASSIGNED
            );

            notificationRepository.save(notification1);
            notificationRepository.save(notification2);
            notificationRepository.save(notification3);

            // When
            List<Notification> userNotifications = notificationRepository.findByUserId(testUser1.getId());

            // Then
            assertEquals(2, userNotifications.size());
            assertTrue(userNotifications.stream().allMatch(n -> n.getUserId().equals(testUser1.getId())));
        }

        @Test
        @DisplayName("Should find notifications by user ID paginated")
        void shouldFindNotificationsByUserIdPaginated() {
            // Given
            for (int i = 0; i < 5; i++) {
                Notification notification = new Notification(
                    testUser1.getId(), 
                    "Page Test " + i, 
                    "Message " + i, 
                    NotificationType.ORDER_CREATED
                );
                notificationRepository.save(notification);
            }

            // When
            List<Notification> page0 = notificationRepository.findByUserIdPaginated(testUser1.getId(), 0, 2);
            List<Notification> page1 = notificationRepository.findByUserIdPaginated(testUser1.getId(), 1, 2);

            // Then
            assertEquals(2, page0.size());
            assertEquals(2, page1.size());
            // Verify no duplicates between pages
            assertNotEquals(page0.get(0).getId(), page1.get(0).getId());
        }

        @Test
        @DisplayName("Should find unread notifications by user ID")
        void shouldFindUnreadNotificationsByUserId() {
            // Given
            Notification readNotification = new Notification(
                testUser1.getId(), 
                "Read Test", 
                "Read Message", 
                NotificationType.ORDER_CREATED
            );
            readNotification.markAsRead();
            
            Notification unreadNotification = new Notification(
                testUser1.getId(), 
                "Unread Test", 
                "Unread Message", 
                NotificationType.ORDER_STATUS_CHANGED
            );

            notificationRepository.save(readNotification);
            notificationRepository.save(unreadNotification);

            // When
            List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(testUser1.getId());

            // Then
            assertEquals(1, unreadNotifications.size());
            assertFalse(unreadNotifications.get(0).isRead());
            assertEquals("Unread Test", unreadNotifications.get(0).getTitle());
        }

        @Test
        @DisplayName("Should find notifications by user ID and type")
        void shouldFindNotificationsByUserIdAndType() {
            // Given
            Notification orderNotification = new Notification(
                testUser1.getId(), 
                "Order Test", 
                "Order Message", 
                NotificationType.ORDER_CREATED
            );
            Notification deliveryNotification = new Notification(
                testUser1.getId(), 
                "Delivery Test", 
                "Delivery Message", 
                NotificationType.DELIVERY_ASSIGNED
            );

            notificationRepository.save(orderNotification);
            notificationRepository.save(deliveryNotification);

            // When
            List<Notification> orderNotifications = notificationRepository.findByUserIdAndType(
                testUser1.getId(), NotificationType.ORDER_CREATED
            );

            // Then
            assertEquals(1, orderNotifications.size());
            assertEquals(NotificationType.ORDER_CREATED, orderNotifications.get(0).getType());
        }

        @Test
        @DisplayName("Should find notifications by user ID and priority")
        void shouldFindNotificationsByUserIdAndPriority() {
            // Given
            Notification highPriorityNotification = new Notification(
                testUser1.getId(), 
                "High Priority", 
                "High Priority Message", 
                NotificationType.SYSTEM_MAINTENANCE,
                NotificationPriority.HIGH
            );
            Notification lowPriorityNotification = new Notification(
                testUser1.getId(), 
                "Low Priority", 
                "Low Priority Message", 
                NotificationType.PROMOTIONAL,
                NotificationPriority.LOW
            );

            notificationRepository.save(highPriorityNotification);
            notificationRepository.save(lowPriorityNotification);

            // When
            List<Notification> highPriorityNotifications = notificationRepository.findByUserIdAndPriority(
                testUser1.getId(), NotificationPriority.HIGH
            );

            // Then
            assertEquals(1, highPriorityNotifications.size());
            assertEquals(NotificationPriority.HIGH, highPriorityNotifications.get(0).getPriority());
        }

        @Test
        @DisplayName("Should find high priority notifications by user ID")
        void shouldFindHighPriorityNotificationsByUserId() {
            // Given
            Notification highPriorityNotification = new Notification(
                testUser1.getId(), 
                "High Priority", 
                "High Priority Message", 
                NotificationType.SYSTEM_MAINTENANCE,
                NotificationPriority.HIGH
            );
            Notification mediumPriorityNotification = new Notification(
                testUser1.getId(), 
                "Medium Priority", 
                "Medium Priority Message", 
                NotificationType.PROMOTIONAL,
                NotificationPriority.MEDIUM
            );

            notificationRepository.save(highPriorityNotification);
            notificationRepository.save(mediumPriorityNotification);

            // When
            List<Notification> highPriorityNotifications = notificationRepository.findHighPriorityByUserId(testUser1.getId());

            // Then
            assertEquals(1, highPriorityNotifications.size());
            assertEquals(NotificationPriority.HIGH, highPriorityNotifications.get(0).getPriority());
        }

        @Test
        @DisplayName("Should find recent notifications by user ID")
        void shouldFindRecentNotificationsByUserId() {
            // Given
            Notification recentNotification = new Notification(
                testUser1.getId(), 
                "Recent Test", 
                "Recent Message", 
                NotificationType.ORDER_CREATED
            );
            notificationRepository.save(recentNotification);

            // Simulate old notification by manually setting creation date
            Notification oldNotification = new Notification(
                testUser1.getId(), 
                "Old Test", 
                "Old Message", 
                NotificationType.ORDER_STATUS_CHANGED
            );
            oldNotification = notificationRepository.save(oldNotification);
            
            // Manually update creation date to be old
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                session.createQuery("UPDATE Notification SET createdAt = :oldDate WHERE id = :id")
                    .setParameter("oldDate", LocalDateTime.now().minusDays(10))
                    .setParameter("id", oldNotification.getId())
                    .executeUpdate();
                transaction.commit();
            }

            // When
            List<Notification> recentNotifications = notificationRepository.findRecentByUserId(testUser1.getId(), 7);

            // Then
            assertEquals(1, recentNotifications.size());
            assertEquals("Recent Test", recentNotifications.get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperationsTest {

        @Test
        @DisplayName("Should mark all notifications as read for user")
        void shouldMarkAllNotificationsAsReadForUser() {
            // Given
            Notification notification1 = new Notification(
                testUser1.getId(), 
                "Bulk Test 1", 
                "Message 1", 
                NotificationType.ORDER_CREATED
            );
            Notification notification2 = new Notification(
                testUser1.getId(), 
                "Bulk Test 2", 
                "Message 2", 
                NotificationType.ORDER_STATUS_CHANGED
            );

            notificationRepository.save(notification1);
            notificationRepository.save(notification2);

            // When
            int updatedCount = notificationRepository.markAllAsReadForUser(testUser1.getId());

            // Then
            assertEquals(2, updatedCount);
            
            // Verify all notifications are now read
            List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(testUser1.getId());
            assertEquals(0, unreadNotifications.size());
        }

        @Test
        @DisplayName("Should mark notifications as read by type")
        void shouldMarkNotificationsAsReadByType() {
            // Given
            Notification orderNotification = new Notification(
                testUser1.getId(), 
                "Order Test", 
                "Order Message", 
                NotificationType.ORDER_CREATED
            );
            Notification deliveryNotification = new Notification(
                testUser1.getId(), 
                "Delivery Test", 
                "Delivery Message", 
                NotificationType.DELIVERY_ASSIGNED
            );

            notificationRepository.save(orderNotification);
            notificationRepository.save(deliveryNotification);

            // When
            int updatedCount = notificationRepository.markAsReadByType(testUser1.getId(), NotificationType.ORDER_CREATED);

            // Then
            assertEquals(1, updatedCount);
            
            // Verify only order notifications are read
            List<Notification> orderNotifications = notificationRepository.findByUserIdAndType(
                testUser1.getId(), NotificationType.ORDER_CREATED
            );
            assertTrue(orderNotifications.get(0).isRead());
            
            List<Notification> deliveryNotifications = notificationRepository.findByUserIdAndType(
                testUser1.getId(), NotificationType.DELIVERY_ASSIGNED
            );
            assertFalse(deliveryNotifications.get(0).isRead());
        }

        @Test
        @DisplayName("Should soft delete old notifications")
        void shouldSoftDeleteOldNotifications() {
            // Given
            Notification oldNotification = new Notification(
                testUser1.getId(), 
                "Old Test", 
                "Old Message", 
                NotificationType.ORDER_CREATED
            );
            oldNotification = notificationRepository.save(oldNotification);
            
            // Manually set old creation date
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                session.createQuery("UPDATE Notification SET createdAt = :oldDate WHERE id = :id")
                    .setParameter("oldDate", LocalDateTime.now().minusDays(35))
                    .setParameter("id", oldNotification.getId())
                    .executeUpdate();
                transaction.commit();
            }

            // When
            int deletedCount = notificationRepository.softDeleteOldNotifications(30);

            // Then
            assertEquals(1, deletedCount);
            
            // Verify notification is soft deleted
            Optional<Notification> deletedNotification = notificationRepository.findById(oldNotification.getId());
            assertTrue(deletedNotification.isPresent());
            assertTrue(deletedNotification.get().isDeleted());
        }
    }

    @Nested
    @DisplayName("Statistics and Counts")
    class StatisticsAndCountsTest {

        @Test
        @DisplayName("Should get unread count")
        void shouldGetUnreadCount() {
            // Given
            Notification readNotification = new Notification(
                testUser1.getId(), 
                "Read Test", 
                "Read Message", 
                NotificationType.ORDER_CREATED
            );
            readNotification.markAsRead();
            
            Notification unreadNotification1 = new Notification(
                testUser1.getId(), 
                "Unread Test 1", 
                "Unread Message 1", 
                NotificationType.ORDER_STATUS_CHANGED
            );
            
            Notification unreadNotification2 = new Notification(
                testUser1.getId(), 
                "Unread Test 2", 
                "Unread Message 2", 
                NotificationType.DELIVERY_ASSIGNED
            );

            notificationRepository.save(readNotification);
            notificationRepository.save(unreadNotification1);
            notificationRepository.save(unreadNotification2);

            // When
            long unreadCount = notificationRepository.getUnreadCount(testUser1.getId());

            // Then
            assertEquals(2, unreadCount);
        }

        @Test
        @DisplayName("Should get notification count by type")
        void shouldGetNotificationCountByType() {
            // Given
            Notification orderNotification1 = new Notification(
                testUser1.getId(), 
                "Order Test 1", 
                "Order Message 1", 
                NotificationType.ORDER_CREATED
            );
            Notification orderNotification2 = new Notification(
                testUser1.getId(), 
                "Order Test 2", 
                "Order Message 2", 
                NotificationType.ORDER_CREATED
            );
            Notification deliveryNotification = new Notification(
                testUser1.getId(), 
                "Delivery Test", 
                "Delivery Message", 
                NotificationType.DELIVERY_ASSIGNED
            );

            notificationRepository.save(orderNotification1);
            notificationRepository.save(orderNotification2);
            notificationRepository.save(deliveryNotification);

            // When
            long orderCount = notificationRepository.getNotificationCountByType(testUser1.getId(), NotificationType.ORDER_CREATED);
            long deliveryCount = notificationRepository.getNotificationCountByType(testUser1.getId(), NotificationType.DELIVERY_ASSIGNED);

            // Then
            assertEquals(2, orderCount);
            assertEquals(1, deliveryCount);
        }

        @Test
        @DisplayName("Should get high priority unread count")
        void shouldGetHighPriorityUnreadCount() {
            // Given
            Notification highPriorityUnread = new Notification(
                testUser1.getId(), 
                "High Priority Unread", 
                "High Priority Message", 
                NotificationType.SYSTEM_MAINTENANCE,
                NotificationPriority.HIGH
            );
            
            Notification highPriorityRead = new Notification(
                testUser1.getId(), 
                "High Priority Read", 
                "High Priority Message", 
                NotificationType.SYSTEM_MAINTENANCE,
                NotificationPriority.HIGH
            );
            highPriorityRead.markAsRead();
            
            Notification mediumPriorityUnread = new Notification(
                testUser1.getId(), 
                "Medium Priority Unread", 
                "Medium Priority Message", 
                NotificationType.PROMOTIONAL,
                NotificationPriority.MEDIUM
            );

            notificationRepository.save(highPriorityUnread);
            notificationRepository.save(highPriorityRead);
            notificationRepository.save(mediumPriorityUnread);

            // When
            long highPriorityUnreadCount = notificationRepository.getHighPriorityUnreadCount(testUser1.getId());

            // Then
            assertEquals(1, highPriorityUnreadCount);
        }

        @Test
        @DisplayName("Should get latest notification")
        void shouldGetLatestNotification() {
            // Given
            Notification oldNotification = new Notification(
                testUser1.getId(), 
                "Old Test", 
                "Old Message", 
                NotificationType.ORDER_CREATED
            );
            notificationRepository.save(oldNotification);
            
            // Wait a moment to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            Notification latestNotification = new Notification(
                testUser1.getId(), 
                "Latest Test", 
                "Latest Message", 
                NotificationType.ORDER_STATUS_CHANGED
            );
            notificationRepository.save(latestNotification);

            // When
            Optional<Notification> latest = notificationRepository.getLatestNotification(testUser1.getId());

            // Then
            assertTrue(latest.isPresent());
            assertEquals("Latest Test", latest.get().getTitle());
        }

        @Test
        @DisplayName("Should get notification stats by type")
        void shouldGetNotificationStatsByType() {
            // Given
            Notification orderNotification1 = new Notification(
                testUser1.getId(), 
                "Order Test 1", 
                "Order Message 1", 
                NotificationType.ORDER_CREATED
            );
            Notification orderNotification2 = new Notification(
                testUser1.getId(), 
                "Order Test 2", 
                "Order Message 2", 
                NotificationType.ORDER_CREATED
            );
            orderNotification2.markAsRead();

            notificationRepository.save(orderNotification1);
            notificationRepository.save(orderNotification2);

            // When
            List<Object[]> stats = notificationRepository.getNotificationStatsByType(testUser1.getId());

            // Then
            assertFalse(stats.isEmpty());
            // Find ORDER_CREATED stats
            Object[] orderStats = stats.stream()
                .filter(stat -> stat[0] == NotificationType.ORDER_CREATED)
                .findFirst()
                .orElse(null);
            
            assertNotNull(orderStats);
            assertEquals(2L, orderStats[1]); // Total count
            assertEquals(1L, orderStats[2]); // Unread count
        }
    }

    @Nested
    @DisplayName("Specialized Find Operations")
    class SpecializedFindOperationsTest {

        @Test
        @DisplayName("Should find order notifications")
        void shouldFindOrderNotifications() {
            // Given
            Long orderId = 123L;
            Notification orderCreated = new Notification(
                testUser1.getId(), 
                "Order Created", 
                "Your order has been created", 
                NotificationType.ORDER_CREATED
            );
            orderCreated.setRelatedEntityId(orderId);
            
            Notification orderStatusChanged = new Notification(
                testUser1.getId(), 
                "Order Status Changed", 
                "Your order status has changed", 
                NotificationType.ORDER_STATUS_CHANGED
            );
            orderStatusChanged.setRelatedEntityId(orderId);
            
            Notification unrelatedNotification = new Notification(
                testUser1.getId(), 
                "Unrelated", 
                "Unrelated notification", 
                NotificationType.PROMOTIONAL
            );

            notificationRepository.save(orderCreated);
            notificationRepository.save(orderStatusChanged);
            notificationRepository.save(unrelatedNotification);

            // When
            List<Notification> orderNotifications = notificationRepository.findOrderNotifications(orderId);

            // Then
            assertEquals(2, orderNotifications.size());
            assertTrue(orderNotifications.stream().allMatch(n -> n.getRelatedEntityId().equals(orderId)));
        }

        @Test
        @DisplayName("Should find user order notifications")
        void shouldFindUserOrderNotifications() {
            // Given
            Long orderId = 456L;
            Notification userOrderNotification = new Notification(
                testUser1.getId(), 
                "User Order", 
                "Your order notification", 
                NotificationType.ORDER_CREATED
            );
            userOrderNotification.setRelatedEntityId(orderId);
            
            Notification otherUserOrderNotification = new Notification(
                testUser2.getId(), 
                "Other User Order", 
                "Other user order notification", 
                NotificationType.ORDER_CREATED
            );
            otherUserOrderNotification.setRelatedEntityId(orderId);

            notificationRepository.save(userOrderNotification);
            notificationRepository.save(otherUserOrderNotification);

            // When
            List<Notification> userOrderNotifications = notificationRepository.findUserOrderNotifications(testUser1.getId(), orderId);

            // Then
            assertEquals(1, userOrderNotifications.size());
            assertEquals(testUser1.getId(), userOrderNotifications.get(0).getUserId());
            assertEquals(orderId, userOrderNotifications.get(0).getRelatedEntityId());
        }
    }

    @Nested
    @DisplayName("Broadcast Operations")
    class BroadcastOperationsTest {

        @Test
        @DisplayName("Should get all active user IDs")
        void shouldGetAllActiveUserIds() {
            // When
            List<Long> activeUserIds = notificationRepository.getAllActiveUserIds();

            // Then
            assertTrue(activeUserIds.size() >= 2); // At least our test users
            assertTrue(activeUserIds.contains(testUser1.getId()));
            assertTrue(activeUserIds.contains(testUser2.getId()));
        }

        @Test
        @DisplayName("Should save batch notifications")
        void shouldSaveBatchNotifications() {
            // Given
            List<Notification> notifications = Arrays.asList(
                new Notification(testUser1.getId(), "Batch 1", "Message 1", NotificationType.PROMOTIONAL),
                new Notification(testUser1.getId(), "Batch 2", "Message 2", NotificationType.PROMOTIONAL),
                new Notification(testUser2.getId(), "Batch 3", "Message 3", NotificationType.PROMOTIONAL)
            );

            // When
            notificationRepository.saveBatch(notifications);

            // Then
            List<Notification> user1Notifications = notificationRepository.findByUserId(testUser1.getId());
            List<Notification> user2Notifications = notificationRepository.findByUserId(testUser2.getId());
            
            assertTrue(user1Notifications.size() >= 2);
            assertTrue(user2Notifications.size() >= 1);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandlingTest {

        @Test
        @DisplayName("Should return empty optional for non-existent notification")
        void shouldReturnEmptyOptionalForNonExistentNotification() {
            // When
            Optional<Notification> result = notificationRepository.findById(999999L);

            // Then
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty list for user with no notifications")
        void shouldReturnEmptyListForUserWithNoNotifications() {
            // Given - using a user ID that doesn't exist
            Long nonExistentUserId = 999999L;

            // When
            List<Notification> notifications = notificationRepository.findByUserId(nonExistentUserId);

            // Then
            assertTrue(notifications.isEmpty());
        }

        @Test
        @DisplayName("Should handle pagination with no results")
        void shouldHandlePaginationWithNoResults() {
            // Given - using a user ID that doesn't exist
            Long nonExistentUserId = 999999L;

            // When
            List<Notification> notifications = notificationRepository.findByUserIdPaginated(nonExistentUserId, 0, 10);

            // Then
            assertTrue(notifications.isEmpty());
        }

        @Test
        @DisplayName("Should return zero for counts when no matching notifications")
        void shouldReturnZeroForCountsWhenNoMatchingNotifications() {
            // Given - using a user ID that doesn't exist
            Long nonExistentUserId = 999999L;

            // When
            long unreadCount = notificationRepository.getUnreadCount(nonExistentUserId);
            long typeCount = notificationRepository.getNotificationCountByType(nonExistentUserId, NotificationType.ORDER_CREATED);
            long highPriorityCount = notificationRepository.getHighPriorityUnreadCount(nonExistentUserId);

            // Then
            assertEquals(0, unreadCount);
            assertEquals(0, typeCount);
            assertEquals(0, highPriorityCount);
        }
    }
} 
package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
import com.myapp.auth.AuthRepository;
import com.myapp.common.models.User;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private AuthRepository authRepository;
    
    private NotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(NotificationRepository.class);
        authRepository = Mockito.mock(AuthRepository.class);
        notificationService = new NotificationService(notificationRepository, authRepository);
    }

    @Nested
    @DisplayName("Basic Notification Operations")
    class BasicNotificationOperationsTest {

        @Test
        @DisplayName("Should create notification successfully")
        void shouldCreateNotificationSuccessfully() {
            // Given
            Long userId = 1L;
            String title = "Test Title";
            String message = "Test Message";
            NotificationType type = NotificationType.ORDER_CREATED;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            Notification savedNotification = new Notification(userId, title, message, type);
            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
            
            // When
            Notification result = notificationService.createNotification(userId, title, message, type);
            
            // Then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(title, result.getTitle());
            assertEquals(message, result.getMessage());
            assertEquals(type, result.getType());
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should create notification with priority successfully")
        void shouldCreateNotificationWithPrioritySuccessfully() {
            // Given
            Long userId = 1L;
            String title = "High Priority Test";
            String message = "Important Message";
            NotificationType type = NotificationType.SYSTEM_MAINTENANCE;
            NotificationPriority priority = NotificationPriority.HIGH;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            Notification savedNotification = new Notification(userId, title, message, type, priority);
            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
            
            // When
            Notification result = notificationService.createNotification(userId, title, message, type, priority);
            
            // Then
            assertNotNull(result);
            assertEquals(priority, result.getPriority());
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid user ID")
        void shouldThrowExceptionForInvalidUserId() {
            // Given
            Long invalidUserId = null;
            String title = "Test";
            String message = "Test";
            NotificationType type = NotificationType.ORDER_CREATED;
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.createNotification(invalidUserId, title, message, type));
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for empty title")
        void shouldThrowExceptionForEmptyTitle() {
            // Given
            Long userId = 1L;
            String emptyTitle = "";
            String message = "Test Message";
            NotificationType type = NotificationType.ORDER_CREATED;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.createNotification(userId, emptyTitle, message, type));
            verify(notificationRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for empty message")
        void shouldThrowExceptionForEmptyMessage() {
            // Given
            Long userId = 1L;
            String title = "Test Title";
            String emptyMessage = "";
            NotificationType type = NotificationType.ORDER_CREATED;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.createNotification(userId, title, emptyMessage, type));
            verify(notificationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Notification Retrieval")
    class NotificationRetrievalTest {

        @Test
        @DisplayName("Should get notification by ID successfully")
        void shouldGetNotificationByIdSuccessfully() {
            // Given
            Long notificationId = 1L;
            Notification notification = new Notification(1L, "Title", "Message", NotificationType.ORDER_CREATED);
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            
            // When
            Optional<Notification> result = notificationService.getNotificationById(notificationId);
            
            // Then
            assertTrue(result.isPresent());
            assertEquals(notification, result.get());
            verify(notificationRepository).findById(notificationId);
        }

        @Test
        @DisplayName("Should return empty optional for non-existent notification")
        void shouldReturnEmptyOptionalForNonExistentNotification() {
            // Given
            Long notificationId = 999L;
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());
            
            // When
            Optional<Notification> result = notificationService.getNotificationById(notificationId);
            
            // Then
            assertFalse(result.isPresent());
            verify(notificationRepository).findById(notificationId);
        }

        @Test
        @DisplayName("Should get user notifications successfully")
        void shouldGetUserNotificationsSuccessfully() {
            // Given
            Long userId = 1L;
            List<Notification> notifications = Arrays.asList(
                new Notification(userId, "Title 1", "Message 1", NotificationType.ORDER_CREATED),
                new Notification(userId, "Title 2", "Message 2", NotificationType.ORDER_STATUS_CHANGED)
            );
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.findByUserId(userId)).thenReturn(notifications);
            
            // When
            List<Notification> result = notificationService.getUserNotifications(userId);
            
            // Then
            assertEquals(2, result.size());
            assertEquals(notifications, result);
            verify(notificationRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("Should get unread notifications successfully")
        void shouldGetUnreadNotificationsSuccessfully() {
            // Given
            Long userId = 1L;
            List<Notification> unreadNotifications = Arrays.asList(
                new Notification(userId, "Unread 1", "Message 1", NotificationType.ORDER_CREATED)
            );
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.findUnreadByUserId(userId)).thenReturn(unreadNotifications);
            
            // When
            List<Notification> result = notificationService.getUnreadNotifications(userId);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(unreadNotifications, result);
            verify(notificationRepository).findUnreadByUserId(userId);
        }

        @Test
        @DisplayName("Should get notifications by type successfully")
        void shouldGetNotificationsByTypeSuccessfully() {
            // Given
            Long userId = 1L;
            NotificationType type = NotificationType.ORDER_CREATED;
            List<Notification> notifications = Arrays.asList(
                new Notification(userId, "Order 1", "Message 1", type),
                new Notification(userId, "Order 2", "Message 2", type)
            );
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.findByUserIdAndType(userId, type)).thenReturn(notifications);
            
            // When
            List<Notification> result = notificationService.getNotificationsByType(userId, type);
            
            // Then
            assertEquals(2, result.size());
            assertEquals(notifications, result);
            verify(notificationRepository).findByUserIdAndType(userId, type);
        }

        @Test
        @DisplayName("Should get notifications by priority successfully")
        void shouldGetNotificationsByPrioritySuccessfully() {
            // Given
            Long userId = 1L;
            NotificationPriority priority = NotificationPriority.HIGH;
            List<Notification> notifications = Arrays.asList(
                new Notification(userId, "High 1", "Message 1", NotificationType.SYSTEM_MAINTENANCE, priority)
            );
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.findByUserIdAndPriority(userId, priority)).thenReturn(notifications);
            
            // When
            List<Notification> result = notificationService.getNotificationsByPriority(userId, priority);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(notifications, result);
            verify(notificationRepository).findByUserIdAndPriority(userId, priority);
        }

        @Test
        @DisplayName("Should get recent notifications successfully")
        void shouldGetRecentNotificationsSuccessfully() {
            // Given
            Long userId = 1L;
            int days = 7;
            List<Notification> recentNotifications = Arrays.asList(
                new Notification(userId, "Recent", "Message", NotificationType.ORDER_CREATED)
            );
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.findRecentByUserId(userId, days)).thenReturn(recentNotifications);
            
            // When
            List<Notification> result = notificationService.getRecentNotifications(userId, days);
            
            // Then
            assertEquals(1, result.size());
            assertEquals(recentNotifications, result);
            verify(notificationRepository).findRecentByUserId(userId, days);
        }
    }

    @Nested
    @DisplayName("Notification State Management")
    class NotificationStateManagementTest {

        @Test
        @DisplayName("Should mark notification as read successfully")
        void shouldMarkNotificationAsReadSuccessfully() {
            // Given
            Long notificationId = 1L;
            Notification notification = new Notification(1L, "Title", "Message", NotificationType.ORDER_CREATED);
            notification.setId(notificationId);
            
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.update(notification)).thenReturn(notification);
            
            // When
            Notification result = notificationService.markAsRead(notificationId);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isRead());
            assertNotNull(result.getReadAt());
            verify(notificationRepository).update(notification);
        }

        @Test
        @DisplayName("Should throw exception when marking non-existent notification as read")
        void shouldThrowExceptionWhenMarkingNonExistentNotificationAsRead() {
            // Given
            Long notificationId = 999L;
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());
            
            // When & Then
            assertThrows(RuntimeException.class, () -> 
                notificationService.markAsRead(notificationId));
            verify(notificationRepository, never()).update(any());
        }

        @Test
        @DisplayName("Should mark notification as unread successfully")
        void shouldMarkNotificationAsUnreadSuccessfully() {
            // Given
            Long notificationId = 1L;
            Notification notification = new Notification(1L, "Title", "Message", NotificationType.ORDER_CREATED);
            notification.setId(notificationId);
            notification.markAsRead(); // First mark as read
            
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.update(notification)).thenReturn(notification);
            
            // When
            Notification result = notificationService.markAsUnread(notificationId);
            
            // Then
            assertNotNull(result);
            assertFalse(result.isRead());
            assertNull(result.getReadAt());
            verify(notificationRepository).update(notification);
        }

        @Test
        @DisplayName("Should delete notification successfully")
        void shouldDeleteNotificationSuccessfully() {
            // Given
            Long notificationId = 1L;
            Notification notification = new Notification(1L, "Title", "Message", NotificationType.ORDER_CREATED);
            notification.setId(notificationId);
            
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.update(notification)).thenReturn(notification);
            
            // When
            notificationService.deleteNotification(notificationId);
            
            // Then
            assertTrue(notification.isDeleted());
            assertNotNull(notification.getDeletedAt());
            verify(notificationRepository).update(notification);
        }

        @Test
        @DisplayName("Should restore notification successfully")
        void shouldRestoreNotificationSuccessfully() {
            // Given
            Long notificationId = 1L;
            Notification notification = new Notification(1L, "Title", "Message", NotificationType.ORDER_CREATED);
            notification.setId(notificationId);
            notification.softDelete(); // First delete
            
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
            when(notificationRepository.update(notification)).thenReturn(notification);
            
            // When
            notificationService.restoreNotification(notificationId);
            
            // Then
            assertFalse(notification.isDeleted());
            assertNull(notification.getDeletedAt());
            verify(notificationRepository).update(notification);
        }
    }

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperationsTest {

        @Test
        @DisplayName("Should mark all notifications as read successfully")
        void shouldMarkAllNotificationsAsReadSuccessfully() {
            // Given
            Long userId = 1L;
            int expectedCount = 5;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.markAllAsReadForUser(userId)).thenReturn(expectedCount);
            
            // When
            int result = notificationService.markAllAsRead(userId);
            
            // Then
            assertEquals(expectedCount, result);
            verify(notificationRepository).markAllAsReadForUser(userId);
        }

        @Test
        @DisplayName("Should mark all notifications by type as read successfully")
        void shouldMarkAllNotificationsByTypeAsReadSuccessfully() {
            // Given
            Long userId = 1L;
            NotificationType type = NotificationType.ORDER_CREATED;
            int expectedCount = 3;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.markAsReadByType(userId, type)).thenReturn(expectedCount);
            
            // When
            int result = notificationService.markAllAsReadByType(userId, type);
            
            // Then
            assertEquals(expectedCount, result);
            verify(notificationRepository).markAsReadByType(userId, type);
        }

        @Test
        @DisplayName("Should cleanup old notifications successfully")
        void shouldCleanupOldNotificationsSuccessfully() {
            // Given
            int daysOld = 30;
            int expectedCount = 10;
            when(notificationRepository.softDeleteOldNotifications(daysOld)).thenReturn(expectedCount);
            
            // When
            int result = notificationService.cleanupOldNotifications(daysOld);
            
            // Then
            assertEquals(expectedCount, result);
            verify(notificationRepository).softDeleteOldNotifications(daysOld);
        }

        @Test
        @DisplayName("Should purge old notifications successfully")
        void shouldPurgeOldNotificationsSuccessfully() {
            // Given
            int daysOld = 90;
            int expectedCount = 5;
            when(notificationRepository.hardDeleteOldNotifications(daysOld)).thenReturn(expectedCount);
            
            // When
            int result = notificationService.purgeOldNotifications(daysOld);
            
            // Then
            assertEquals(expectedCount, result);
            verify(notificationRepository).hardDeleteOldNotifications(daysOld);
        }
    }

    @Nested
    @DisplayName("Factory Methods for Specific Notifications")
    class FactoryMethodsTest {

        @Test
        @DisplayName("Should create order created notification successfully")
        void shouldCreateOrderCreatedNotificationSuccessfully() {
            // Given
            Long userId = 1L;
            Long orderId = 100L;
            String restaurantName = "Test Restaurant";
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            Notification savedNotification = Notification.orderCreated(userId, orderId, restaurantName);
            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
            
            // When
            Notification result = notificationService.notifyOrderCreated(userId, orderId, restaurantName);
            
            // Then
            assertNotNull(result);
            assertEquals(NotificationType.ORDER_CREATED, result.getType());
            assertEquals(orderId, result.getRelatedEntityId());
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should create order status changed notification successfully")
        void shouldCreateOrderStatusChangedNotificationSuccessfully() {
            // Given
            Long userId = 1L;
            Long orderId = 100L;
            OrderStatus newStatus = OrderStatus.PREPARING;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            Notification savedNotification = Notification.orderStatusChanged(userId, orderId, newStatus);
            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
            
            // When
            Notification result = notificationService.notifyOrderStatusChanged(userId, orderId, newStatus);
            
            // Then
            assertNotNull(result);
            assertEquals(NotificationType.ORDER_STATUS_CHANGED, result.getType());
            assertEquals(orderId, result.getRelatedEntityId());
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should create delivery assigned notification successfully")
        void shouldCreateDeliveryAssignedNotificationSuccessfully() {
            // Given
            Long userId = 1L;
            Long orderId = 100L;
            Long deliveryId = 200L;
            String courierName = "John Doe";
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            Notification savedNotification = Notification.deliveryAssigned(userId, orderId, deliveryId, courierName);
            when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
            
            // When
            Notification result = notificationService.notifyDeliveryAssigned(userId, orderId, deliveryId, courierName);
            
            // Then
            assertNotNull(result);
            assertEquals(NotificationType.DELIVERY_ASSIGNED, result.getType());
            assertEquals(orderId, result.getRelatedEntityId());
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid order ID in factory methods")
        void shouldThrowExceptionForInvalidOrderIdInFactoryMethods() {
            // Given
            Long userId = 1L;
            Long invalidOrderId = null;
            String restaurantName = "Test Restaurant";
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.notifyOrderCreated(userId, invalidOrderId, restaurantName));
            verify(notificationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Statistics and Counts")
    class StatisticsAndCountsTest {

        @Test
        @DisplayName("Should get unread count successfully")
        void shouldGetUnreadCountSuccessfully() {
            // Given
            Long userId = 1L;
            long expectedCount = 5L;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.getUnreadCount(userId)).thenReturn(expectedCount);
            
            // When
            long result = notificationService.getUnreadCount(userId);
            
            // Then
            assertEquals(expectedCount, result);
            verify(notificationRepository).getUnreadCount(userId);
        }

        @Test
        @DisplayName("Should get notification count by type successfully")
        void shouldGetNotificationCountByTypeSuccessfully() {
            // Given
            Long userId = 1L;
            NotificationType type = NotificationType.ORDER_CREATED;
            long expectedCount = 3L;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.getNotificationCountByType(userId, type)).thenReturn(expectedCount);
            
            // When
            long result = notificationService.getNotificationCountByType(userId, type);
            
            // Then
            assertEquals(expectedCount, result);
            verify(notificationRepository).getNotificationCountByType(userId, type);
        }

        @Test
        @DisplayName("Should check if user has unread high priority notifications")
        void shouldCheckIfUserHasUnreadHighPriorityNotifications() {
            // Given
            Long userId = 1L;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.getHighPriorityUnreadCount(userId)).thenReturn(2L);
            
            // When
            boolean result = notificationService.hasUnreadHighPriorityNotifications(userId);
            
            // Then
            assertTrue(result);
            verify(notificationRepository).getHighPriorityUnreadCount(userId);
        }

        @Test
        @DisplayName("Should return false when no unread high priority notifications")
        void shouldReturnFalseWhenNoUnreadHighPriorityNotifications() {
            // Given
            Long userId = 1L;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(notificationRepository.getHighPriorityUnreadCount(userId)).thenReturn(0L);
            
            // When
            boolean result = notificationService.hasUnreadHighPriorityNotifications(userId);
            
            // Then
            assertFalse(result);
            verify(notificationRepository).getHighPriorityUnreadCount(userId);
        }
    }

    @Nested
    @DisplayName("Broadcast Operations")
    class BroadcastOperationsTest {

        @Test
        @DisplayName("Should broadcast system maintenance notification successfully")
        void shouldBroadcastSystemMaintenanceNotificationSuccessfully() {
            // Given
            LocalDateTime maintenanceTime = LocalDateTime.now().plusHours(2);
            List<Long> activeUserIds = Arrays.asList(1L, 2L, 3L);
            
            when(notificationRepository.getAllActiveUserIds()).thenReturn(activeUserIds);
            doNothing().when(notificationRepository).saveBatch(any());
            
            // When
            notificationService.broadcastSystemMaintenance(maintenanceTime);
            
            // Then
            verify(notificationRepository).getAllActiveUserIds();
            verify(notificationRepository).saveBatch(any());
        }

        @Test
        @DisplayName("Should broadcast promotional message successfully")
        void shouldBroadcastPromotionalMessageSuccessfully() {
            // Given
            String title = "Special Offer";
            String message = "50% off today!";
            NotificationPriority priority = NotificationPriority.MEDIUM;
            List<Long> activeUserIds = Arrays.asList(1L, 2L, 3L);
            
            when(notificationRepository.getAllActiveUserIds()).thenReturn(activeUserIds);
            doNothing().when(notificationRepository).saveBatch(any());
            
            // When
            notificationService.broadcastPromotionalMessage(title, message, priority);
            
            // Then
            verify(notificationRepository).getAllActiveUserIds();
            verify(notificationRepository).saveBatch(any());
        }
    }

    @Nested
    @DisplayName("Daily Maintenance")
    class DailyMaintenanceTest {

        @Test
        @DisplayName("Should perform daily maintenance successfully")
        void shouldPerformDailyMaintenanceSuccessfully() {
            // Given
            when(notificationRepository.softDeleteOldNotifications(90)).thenReturn(10);
            when(notificationRepository.hardDeleteOldNotifications(120)).thenReturn(5);
            
            // When
            notificationService.performDailyMaintenance();
            
            // Then
            verify(notificationRepository).softDeleteOldNotifications(90);
            verify(notificationRepository).hardDeleteOldNotifications(120);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTest {

        @Test
        @DisplayName("Should throw exception for negative user ID")
        void shouldThrowExceptionForNegativeUserId() {
            // Given
            Long negativeUserId = -1L;
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.createNotification(negativeUserId, "Title", "Message", NotificationType.ORDER_CREATED));
        }

        @Test
        @DisplayName("Should throw exception for null notification type")
        void shouldThrowExceptionForNullNotificationType() {
            // Given
            Long userId = 1L;
            NotificationType nullType = null;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.getNotificationsByType(userId, nullType));
        }

        @Test
        @DisplayName("Should throw exception for null notification priority")
        void shouldThrowExceptionForNullNotificationPriority() {
            // Given
            Long userId = 1L;
            NotificationPriority nullPriority = null;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.getNotificationsByPriority(userId, nullPriority));
        }

        @Test
        @DisplayName("Should throw exception for invalid pagination parameters")
        void shouldThrowExceptionForInvalidPaginationParameters() {
            // Given
            Long userId = 1L;
            int invalidPage = -1;
            int validSize = 10;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.getUserNotificationsPaginated(userId, invalidPage, validSize));
        }

        @Test
        @DisplayName("Should throw exception for invalid days parameter")
        void shouldThrowExceptionForInvalidDaysParameter() {
            // Given
            Long userId = 1L;
            int invalidDays = 0;
            
            User mockUser = new User();
            mockUser.setId(userId);
            when(authRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.getRecentNotifications(userId, invalidDays));
        }
    }
} 
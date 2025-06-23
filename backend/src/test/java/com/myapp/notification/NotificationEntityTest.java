package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEntityTest {

    @Test
    @DisplayName("Create notification with basic constructor - Success")
    void testCreateNotificationBasic() {
        // When
        Notification notification = new Notification(
            1L, "Test Title", "Test Message", NotificationType.ORDER_UPDATE);

        // Then
        assertNotNull(notification);
        assertEquals(1L, notification.getUserId());
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Message", notification.getMessage());
        assertEquals(NotificationType.ORDER_UPDATE, notification.getType());
        assertEquals(NotificationPriority.NORMAL, notification.getPriority());
        assertFalse(notification.getIsRead());
        assertFalse(notification.getIsDeleted());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    @DisplayName("Create notification with priority - Success")
    void testCreateNotificationWithPriority() {
        // When
        Notification notification = new Notification(
            1L, "High Priority", "Important message", 
            NotificationType.SYSTEM_UPDATE, NotificationPriority.HIGH);

        // Then
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
    }

    @Test
    @DisplayName("Mark notification as read - Success")
    void testMarkAsRead() {
        // Given
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        assertFalse(notification.getIsRead());
        assertNull(notification.getReadAt());

        // When
        notification.markAsRead();

        // Then
        assertTrue(notification.getIsRead());
        assertNotNull(notification.getReadAt());
    }

    @Test
    @DisplayName("Mark notification as unread - Success")
    void testMarkAsUnread() {
        // Given
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification.markAsRead();
        assertTrue(notification.getIsRead());

        // When
        notification.markAsUnread();

        // Then
        assertFalse(notification.getIsRead());
        assertNull(notification.getReadAt());
    }

    @Test
    @DisplayName("Soft delete notification - Success")
    void testSoftDelete() {
        // Given
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        assertFalse(notification.getIsDeleted());

        // When
        notification.softDelete();

        // Then
        assertTrue(notification.getIsDeleted());
    }

    @Test
    @DisplayName("Restore notification - Success")
    void testRestore() {
        // Given
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification.softDelete();
        assertTrue(notification.getIsDeleted());

        // When
        notification.restore();

        // Then
        assertFalse(notification.getIsDeleted());
    }

    @Test
    @DisplayName("Check if notification is high priority - Success")
    void testIsHighPriority() {
        // Given
        Notification normalNotification = new Notification(
            1L, "Normal", "Message", NotificationType.ORDER_UPDATE, NotificationPriority.NORMAL);
        Notification highNotification = new Notification(
            1L, "High", "Message", NotificationType.ORDER_UPDATE, NotificationPriority.HIGH);
        Notification urgentNotification = new Notification(
            1L, "Urgent", "Message", NotificationType.ORDER_UPDATE, NotificationPriority.URGENT);

        // When & Then
        assertFalse(normalNotification.isHighPriority());
        assertTrue(highNotification.isHighPriority());
        assertTrue(urgentNotification.isHighPriority());
    }

    @Test
    @DisplayName("Check if notification is expired - Success")
    void testIsExpired() {
        // Given
        Notification oldNotification = new Notification(
            1L, "Old", "Message", NotificationType.ORDER_UPDATE);
        oldNotification.setCreatedAt(LocalDateTime.now().minusDays(10));

        Notification newNotification = new Notification(
            1L, "New", "Message", NotificationType.ORDER_UPDATE);

        // When & Then
        assertTrue(oldNotification.isExpired(5));
        assertFalse(newNotification.isExpired(5));
    }

    @Test
    @DisplayName("Check relationship flags - Success")
    void testRelationshipFlags() {
        // Given
        Notification orderNotification = new Notification(
            1L, "Order", "Message", NotificationType.ORDER_UPDATE);
        orderNotification.setOrderId(123L);

        Notification restaurantNotification = new Notification(
            1L, "Restaurant", "Message", NotificationType.RESTAURANT_UPDATE);
        restaurantNotification.setRestaurantId(456L);

        Notification deliveryNotification = new Notification(
            1L, "Delivery", "Message", NotificationType.DELIVERY_UPDATE);
        deliveryNotification.setDeliveryId(789L);

        // When & Then
        assertTrue(orderNotification.isOrderRelated());
        assertFalse(orderNotification.isRestaurantRelated());
        assertFalse(orderNotification.isDeliveryRelated());

        assertFalse(restaurantNotification.isOrderRelated());
        assertTrue(restaurantNotification.isRestaurantRelated());
        assertFalse(restaurantNotification.isDeliveryRelated());

        assertFalse(deliveryNotification.isOrderRelated());
        assertFalse(deliveryNotification.isRestaurantRelated());
        assertTrue(deliveryNotification.isDeliveryRelated());
    }

    @Test
    @DisplayName("Factory method - Order created - Success")
    void testOrderCreatedFactory() {
        // When
        Notification notification = Notification.orderCreated(1L, 123L, "Test Restaurant");

        // Then
        assertEquals(1L, notification.getUserId());
        assertEquals(NotificationType.ORDER_UPDATE, notification.getType());
        assertEquals(NotificationPriority.NORMAL, notification.getPriority());
        assertEquals(Long.valueOf(123L), notification.getOrderId());
        assertTrue(notification.getTitle().contains("سفارش"));
        assertTrue(notification.getMessage().contains("Test Restaurant"));
    }

    @Test
    @DisplayName("Factory method - Order status changed - Success")
    void testOrderStatusChangedFactory() {
        // When
        Notification notification = Notification.orderStatusChanged(1L, 123L, OrderStatus.CONFIRMED);

        // Then
        assertEquals(NotificationType.ORDER_UPDATE, notification.getType());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertEquals(Long.valueOf(123L), notification.getOrderId());
        assertTrue(notification.getTitle().contains("تغییر وضعیت"));
        assertTrue(notification.getMessage().contains("تایید"));
    }

    @Test
    @DisplayName("Factory method - Delivery assigned - Success")
    void testDeliveryAssignedFactory() {
        // When
        Notification notification = Notification.deliveryAssigned(1L, 123L, 456L, "John Doe");

        // Then
        assertEquals(NotificationType.DELIVERY_UPDATE, notification.getType());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertEquals(Long.valueOf(123L), notification.getOrderId());
        assertEquals(Long.valueOf(456L), notification.getDeliveryId());
        assertTrue(notification.getMessage().contains("John Doe"));
    }

    @Test
    @DisplayName("Factory method - Payment processed success - Success")
    void testPaymentProcessedSuccessFactory() {
        // When
        Notification notification = Notification.paymentProcessed(1L, 123L, 50000.0, true);

        // Then
        assertEquals(NotificationType.PAYMENT_UPDATE, notification.getType());
        assertEquals(NotificationPriority.NORMAL, notification.getPriority());
        assertTrue(notification.getTitle().contains("موفق"));
        assertTrue(notification.getMessage().contains("50000.0"));
    }

    @Test
    @DisplayName("Factory method - Payment processed failure - Success")
    void testPaymentProcessedFailureFactory() {
        // When
        Notification notification = Notification.paymentProcessed(1L, 123L, 50000.0, false);

        // Then
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertTrue(notification.getTitle().contains("خطا"));
        assertTrue(notification.getMessage().contains("ناموفق"));
    }

    @Test
    @DisplayName("Factory method - Restaurant approved - Success")
    void testRestaurantApprovedFactory() {
        // When
        Notification notification = Notification.restaurantApproved(1L, 789L, "My Restaurant");

        // Then
        assertEquals(NotificationType.RESTAURANT_UPDATE, notification.getType());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertEquals(Long.valueOf(789L), notification.getRestaurantId());
        assertTrue(notification.getMessage().contains("My Restaurant"));
        assertTrue(notification.getMessage().contains("تایید"));
    }

    @Test
    @DisplayName("Factory method - System maintenance - Success")
    void testSystemMaintenanceFactory() {
        // Given
        LocalDateTime maintenanceTime = LocalDateTime.now().plusDays(1);

        // When
        Notification notification = Notification.systemMaintenance(1L, maintenanceTime);

        // Then
        assertEquals(NotificationType.SYSTEM_UPDATE, notification.getType());
        assertEquals(NotificationPriority.URGENT, notification.getPriority());
        assertTrue(notification.getTitle().contains("نگهداری"));
        assertTrue(notification.getMessage().contains("نگهداری"));
    }

    @Test
    @DisplayName("Equals and hashCode - Success")
    void testEqualsAndHashCode() {
        // Given
        Notification notification1 = new Notification(1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification1.setId(1L);
        
        Notification notification2 = new Notification(1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification2.setId(1L);
        
        Notification notification3 = new Notification(1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification3.setId(2L);

        // When & Then
        assertEquals(notification1, notification2);
        assertNotEquals(notification1, notification3);
        assertEquals(notification1.hashCode(), notification2.hashCode());
    }

    @Test
    @DisplayName("ToString - Success")
    void testToString() {
        // Given
        Notification notification = new Notification(1L, "Test Title", "Test Message", NotificationType.ORDER_UPDATE);
        notification.setId(1L);

        // When
        String toString = notification.toString();

        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("userId=1"));
        assertTrue(toString.contains("title='Test Title'"));
        assertTrue(toString.contains("type=ORDER_UPDATE"));
        assertTrue(toString.contains("priority=NORMAL"));
        assertTrue(toString.contains("isRead=false"));
    }
} 
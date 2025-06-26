package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * مجموعه تست‌های Entity Notification
 * 
 * این کلاس تست تمام عملکردهای entity class Notification را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Constructors و Factory Methods
 * 2. State Management (read/unread, soft delete/restore)
 * 3. Priority و Expiration Logic
 * 4. Relationship Flags (order, restaurant, delivery)
 * 5. Utility Methods (equals, hashCode, toString)
 * 
 * Factory Methods Test:
 * - orderCreated: ایجاد اعلان برای سفارش جدید
 * - orderStatusChanged: تغییر وضعیت سفارش
 * - deliveryAssigned: تخصیص پیک
 * - paymentProcessed: پردازش پرداخت
 * - restaurantApproved: تایید رستوران
 * - systemMaintenance: اعلان نگهداری سیستم
 * 
 * Business Logic Tested:
 * - State transitions (read ↔ unread, delete ↔ restore)
 * - Priority levels (NORMAL, HIGH, URGENT)
 * - Expiration based on days
 * - Entity relationships
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
class NotificationEntityTest {

    /**
     * تست ایجاد اعلان با سازنده پایه
     * 
     * Scenario: ایجاد اعلان با حداقل اطلاعات ضروری
     * Expected:
     * - تمام فیلدها مقدار صحیح داشته باشند
     * - مقادیر پیش‌فرض (priority=NORMAL, isRead=false) ست شوند
     * - createdAt خودکار ست شود
     */
    @Test
    @DisplayName("Create notification with basic constructor - Success")
    void testCreateNotificationBasic() {
        // When - ایجاد اعلان با سازنده پایه
        Notification notification = new Notification(
            1L, "Test Title", "Test Message", NotificationType.ORDER_CREATED);

        // Then - بررسی تمام فیلدها
        assertNotNull(notification);
        assertEquals(1L, notification.getUserId());
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Message", notification.getMessage());
        assertEquals(NotificationType.ORDER_CREATED, notification.getType());
        assertEquals(NotificationPriority.NORMAL, notification.getPriority());
        assertFalse(notification.getIsRead());
        assertFalse(notification.getIsDeleted());
        assertNotNull(notification.getCreatedAt());
    }

    /**
     * تست ایجاد اعلان با اولویت مشخص
     * 
     * Scenario: ایجاد اعلان با اولویت HIGH
     * Expected: اولویت برابر مقدار ارسال شده باشد
     */
    @Test
    @DisplayName("Create notification with priority - Success")
    void testCreateNotificationWithPriority() {
        // When - ایجاد اعلان با اولویت بالا
        Notification notification = new Notification(
            1L, "High Priority", "Important message", 
            NotificationType.SYSTEM_MAINTENANCE, NotificationPriority.HIGH);

        // Then - بررسی اولویت
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
    }

    /**
     * تست علامت‌گذاری اعلان به عنوان خوانده شده
     * 
     * Scenario: تغییر وضعیت اعلان از unread به read
     * Expected:
     * - isRead = true شود
     * - readAt تنظیم شود
     */
    @Test
    @DisplayName("Mark notification as read - Success")
    void testMarkAsRead() {
        // Given - اعلان خوانده نشده
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_CREATED);
        assertFalse(notification.getIsRead());
        assertNull(notification.getReadAt());

        // When - علامت‌گذاری به عنوان خوانده شده
        notification.markAsRead();

        // Then - بررسی تغییر وضعیت
        assertTrue(notification.getIsRead());
        assertNotNull(notification.getReadAt());
    }

    /**
     * تست علامت‌گذاری اعلان به عنوان خوانده نشده
     * 
     * Scenario: برگرداندن اعلان خوانده شده به حالت unread
     * Expected:
     * - isRead = false شود
     * - readAt پاک شود
     */
    @Test
    @DisplayName("Mark notification as unread - Success")
    void testMarkAsUnread() {
        // Given - اعلان خوانده شده
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_CREATED);
        notification.markAsRead();
        assertTrue(notification.getIsRead());

        // When - علامت‌گذاری به عنوان خوانده نشده
        notification.markAsUnread();

        // Then - بررسی تغییر وضعیت
        assertFalse(notification.getIsRead());
        assertNull(notification.getReadAt());
    }

    /**
     * تست حذف نرم اعلان
     * 
     * Scenario: حذف اعلان بدون پاک کردن از دیتابیس
     * Expected: isDeleted = true شود
     */
    @Test
    @DisplayName("Soft delete notification - Success")
    void testSoftDelete() {
        // Given - اعلان فعال
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_CREATED);
        assertFalse(notification.getIsDeleted());

        // When - حذف نرم
        notification.softDelete();

        // Then - بررسی وضعیت حذف
        assertTrue(notification.getIsDeleted());
    }

    /**
     * تست بازیابی اعلان حذف شده
     * 
     * Scenario: برگرداندن اعلان حذف شده به حالت فعال
     * Expected: isDeleted = false شود
     */
    @Test
    @DisplayName("Restore notification - Success")
    void testRestore() {
        // Given - اعلان حذف شده
        Notification notification = new Notification(
            1L, "Test", "Message", NotificationType.ORDER_CREATED);
        notification.softDelete();
        assertTrue(notification.getIsDeleted());

        // When - بازیابی اعلان
        notification.restore();

        // Then - بررسی بازیابی
        assertFalse(notification.getIsDeleted());
    }

    /**
     * تست بررسی اولویت بالا
     * 
     * Scenario: تشخیص اعلان‌های با اولویت HIGH یا URGENT
     * Expected:
     * - NORMAL = false
     * - HIGH = true  
     * - URGENT = true
     */
    @Test
    @DisplayName("Check if notification is high priority - Success")
    void testIsHighPriority() {
        // Given - اعلان‌های با اولویت‌های مختلف
        Notification normalNotification = new Notification(
            1L, "Normal", "Message", NotificationType.ORDER_CREATED, NotificationPriority.NORMAL);
        Notification highNotification = new Notification(
            1L, "High", "Message", NotificationType.ORDER_CREATED, NotificationPriority.HIGH);
        Notification urgentNotification = new Notification(
            1L, "Urgent", "Message", NotificationType.ORDER_CREATED, NotificationPriority.URGENT);

        // When & Then - بررسی اولویت
        assertFalse(normalNotification.isHighPriority());
        assertTrue(highNotification.isHighPriority());
        assertTrue(urgentNotification.isHighPriority());
    }

    /**
     * تست بررسی انقضای اعلان
     * 
     * Scenario: تشخیص اعلان‌های منقضی شده بر اساس روز
     * Expected:
     * - اعلان قدیمی expired = true
     * - اعلان جدید expired = false
     */
    @Test
    @DisplayName("Check if notification is expired - Success")
    void testIsExpired() {
        // Given - اعلان‌های با تاریخ‌های مختلف
        Notification oldNotification = new Notification(
            1L, "Old", "Message", NotificationType.ORDER_CREATED);
        oldNotification.setCreatedAt(LocalDateTime.now().minusDays(10));

        Notification newNotification = new Notification(
            1L, "New", "Message", NotificationType.ORDER_CREATED);

        // When & Then - بررسی انقضا (5 روز)
        assertTrue(oldNotification.isExpired(5));
        assertFalse(newNotification.isExpired(5));
    }

    /**
     * تست بررسی فلگ‌های ارتباط
     * 
     * Scenario: تشخیص نوع entity مرتبط با اعلان
     * Expected:
     * - اعلان سفارش: isOrderRelated = true
     * - اعلان رستوران: isRestaurantRelated = true
     * - اعلان تحویل: isDeliveryRelated = true
     */
    @Test
    @DisplayName("Check relationship flags - Success")
    void testRelationshipFlags() {
        // Given - اعلان‌های مختلف
        Notification orderNotification = new Notification(
            1L, "Order", "Message", NotificationType.ORDER_CREATED);
        orderNotification.setOrderId(123L);

        Notification restaurantNotification = new Notification(
            1L, "Restaurant", "Message", NotificationType.RESTAURANT_APPROVED);
        restaurantNotification.setRestaurantId(456L);

        Notification deliveryNotification = new Notification(
            1L, "Delivery", "Message", NotificationType.DELIVERY_ASSIGNED);
        deliveryNotification.setDeliveryId(789L);

        // When & Then - بررسی فلگ‌های ارتباط
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

    /**
     * تست Factory method - ایجاد سفارش
     * 
     * Scenario: ایجاد اعلان برای سفارش جدید
     * Expected:
     * - نوع ORDER_CREATED
     * - اولویت NORMAL
     * - orderId تنظیم شود
     * - متن فارسی و شامل نام رستوران
     */
    @Test
    @DisplayName("Factory method - Order created - Success")
    void testOrderCreatedFactory() {
        // When - ایجاد اعلان سفارش جدید
        Notification notification = Notification.orderCreated(1L, 123L, "Test Restaurant");

        // Then - بررسی ویژگی‌ها
        assertEquals(1L, notification.getUserId());
        assertEquals(NotificationType.ORDER_CREATED, notification.getType());
        assertEquals(NotificationPriority.NORMAL, notification.getPriority());
        assertEquals(Long.valueOf(123L), notification.getOrderId());
        assertTrue(notification.getTitle().contains("سفارش"));
        assertTrue(notification.getMessage().contains("Test Restaurant"));
    }

    /**
     * تست Factory method - تغییر وضعیت سفارش
     * 
     * Scenario: اعلان تغییر وضعیت سفارش به CONFIRMED
     * Expected:
     * - نوع ORDER_STATUS_CHANGED
     * - اولویت HIGH
     * - شامل متن تایید
     */
    @Test
    @DisplayName("Factory method - Order status changed - Success")
    void testOrderStatusChangedFactory() {
        // When - ایجاد اعلان تغییر وضعیت
        Notification notification = Notification.orderStatusChanged(1L, 123L, OrderStatus.CONFIRMED);

        // Then - بررسی ویژگی‌ها
        assertEquals(NotificationType.ORDER_STATUS_CHANGED, notification.getType());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertEquals(Long.valueOf(123L), notification.getOrderId());
        assertTrue(notification.getTitle().contains("تغییر وضعیت"));
        assertTrue(notification.getMessage().contains("تایید"));
    }

    /**
     * تست Factory method - تخصیص پیک
     * 
     * Scenario: اعلان تخصیص پیک به سفارش
     * Expected:
     * - نوع DELIVERY_ASSIGNED
     * - اولویت HIGH
     * - شامل نام پیک
     */
    @Test
    @DisplayName("Factory method - Delivery assigned - Success")
    void testDeliveryAssignedFactory() {
        // When - ایجاد اعلان تخصیص پیک
        Notification notification = Notification.deliveryAssigned(1L, 123L, 456L, "John Doe");

        // Then - بررسی ویژگی‌ها
        assertEquals(NotificationType.DELIVERY_ASSIGNED, notification.getType());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertEquals(Long.valueOf(123L), notification.getOrderId());
        assertEquals(Long.valueOf(456L), notification.getDeliveryId());
        assertTrue(notification.getMessage().contains("John Doe"));
    }

    /**
     * تست Factory method - پردازش موفق پرداخت
     * 
     * Scenario: اعلان پردازش موفق پرداخت
     * Expected:
     * - نوع PAYMENT_UPDATE
     * - اولویت NORMAL
     * - شامل متن موفق و مبلغ
     */
    @Test
    @DisplayName("Factory method - Payment processed success - Success")
    void testPaymentProcessedSuccessFactory() {
        // When - ایجاد اعلان پرداخت موفق
        Notification notification = Notification.paymentProcessed(1L, 123L, 50000.0, true);

        // Then - بررسی ویژگی‌ها
        assertEquals(NotificationType.PAYMENT_UPDATE, notification.getType());
        assertEquals(NotificationPriority.NORMAL, notification.getPriority());
        assertTrue(notification.getTitle().contains("موفق"));
        assertTrue(notification.getMessage().contains("50000.0"));
    }

    /**
     * تست Factory method - پردازش ناموفق پرداخت
     * 
     * Scenario: اعلان خطا در پردازش پرداخت
     * Expected:
     * - اولویت HIGH (برای خطا)
     * - شامل متن خطا و ناموفق
     */
    @Test
    @DisplayName("Factory method - Payment processed failure - Success")
    void testPaymentProcessedFailureFactory() {
        // When - ایجاد اعلان پرداخت ناموفق
        Notification notification = Notification.paymentProcessed(1L, 123L, 50000.0, false);

        // Then - بررسی ویژگی‌های خطا
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertTrue(notification.getTitle().contains("خطا"));
        assertTrue(notification.getMessage().contains("ناموفق"));
    }

    /**
     * تست Factory method - تایید رستوران
     * 
     * Scenario: اعلان تایید رستوران برای فروشنده
     * Expected:
     * - نوع RESTAURANT_APPROVED
     * - اولویت HIGH
     * - شامل نام رستوران و متن تایید
     */
    @Test
    @DisplayName("Factory method - Restaurant approved - Success")
    void testRestaurantApprovedFactory() {
        // When - ایجاد اعلان تایید رستوران
        Notification notification = Notification.restaurantApproved(1L, 789L, "My Restaurant");

        // Then - بررسی ویژگی‌ها
        assertEquals(NotificationType.RESTAURANT_APPROVED, notification.getType());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertEquals(Long.valueOf(789L), notification.getRestaurantId());
        assertTrue(notification.getMessage().contains("My Restaurant"));
        assertTrue(notification.getMessage().contains("تایید"));
    }

    /**
     * تست Factory method - نگهداری سیستم
     * 
     * Scenario: اعلان برنامه‌ریزی شده نگهداری سیستم
     * Expected:
     * - نوع SYSTEM_MAINTENANCE
     * - اولویت HIGH
     * - شامل متن نگهداری
     */
    @Test
    @DisplayName("Factory method - System maintenance - Success")
    void testSystemMaintenanceFactory() {
        // Given - زمان نگهداری
        LocalDateTime maintenanceTime = LocalDateTime.now().plusDays(1);

        // When - ایجاد اعلان نگهداری
        Notification notification = Notification.systemMaintenance(1L, maintenanceTime);

        // Then - بررسی ویژگی‌ها
        assertEquals(NotificationType.SYSTEM_MAINTENANCE, notification.getType());
        assertEquals(NotificationPriority.HIGH, notification.getPriority());
        assertTrue(notification.getTitle().contains("نگهداری"));
        assertTrue(notification.getMessage().contains("نگهداری"));
    }

    /**
     * تست equals و hashCode
     * 
     * Scenario: مقایسه و hashing اعلان‌ها
     * Expected:
     * - اعلان‌های با ID یکسان برابر باشند
     * - hashCode یکسان داشته باشند
     */
    @Test
    @DisplayName("Equals and hashCode - Success")
    void testEqualsAndHashCode() {
        // Given - اعلان‌های تستی
        Notification notification1 = new Notification(1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification1.setId(1L);
        
        Notification notification2 = new Notification(1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification2.setId(1L);
        
        Notification notification3 = new Notification(1L, "Test", "Message", NotificationType.ORDER_UPDATE);
        notification3.setId(2L);

        // When & Then - بررسی equals و hashCode
        assertEquals(notification1, notification2);
        assertNotEquals(notification1, notification3);
        assertEquals(notification1.hashCode(), notification2.hashCode());
    }

    /**
     * تست toString
     * 
     * Scenario: تولید نمایش متنی اعلان
     * Expected: شامل تمام فیلدهای مهم باشد
     */
    @Test
    @DisplayName("ToString - Success")
    void testToString() {
        // Given - اعلان تستی
        Notification notification = new Notification(1L, "Test Title", "Test Message", NotificationType.ORDER_CREATED);
        notification.setId(1L);

        // When - تولید toString
        String toString = notification.toString();

        // Then - بررسی محتوای toString
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("userId=1"));
        assertTrue(toString.contains("title='Test Title'"));
        assertTrue(toString.contains("type=ORDER_CREATED"));
        assertTrue(toString.contains("priority=NORMAL"));
        assertTrue(toString.contains("isRead=false"));
    }
} 
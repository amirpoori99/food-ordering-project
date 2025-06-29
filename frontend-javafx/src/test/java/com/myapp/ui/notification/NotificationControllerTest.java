package com.myapp.ui.notification;

import com.myapp.ui.notification.NotificationController.NotificationItem;
import com.myapp.ui.notification.NotificationController.NotificationSettings;
import com.myapp.ui.notification.NotificationController.NotificationType;
import com.myapp.ui.notification.NotificationController.NotificationPriority;
import com.myapp.ui.notification.NotificationController.NotificationFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("تست سیستم اطلاع رسانی و هشدارها")
public class NotificationControllerTest {

    private NotificationController controller;
    private NotificationItem testNotification;
    private NotificationSettings testSettings;
    private java.util.List<NotificationItem> testNotifications;

    @BeforeEach
    void setUp() {
        testNotification = new NotificationItem(
            "تست اطلاع رسانی",
            "این یک پیام تست است",
            NotificationType.ORDER_UPDATE,
            NotificationPriority.MEDIUM,
            LocalDateTime.now(),
            false
        );
        
        testSettings = new NotificationSettings();
        testSettings.setEmailEnabled(true);
        testSettings.setSmsEnabled(true);
        testSettings.setPushEnabled(true);
        testSettings.setSoundEnabled(true);
        testSettings.setVolume(75.0);
        testSettings.setSoundType("صدای پیش‌فرض");
        testSettings.setDoNotDisturb(false);
        
        createTestNotifications();
    }

    private void createTestNotifications() {
        testNotifications = new java.util.ArrayList<>();
        
        testNotifications.add(new NotificationItem(
            "سفارش تحویل داده شد",
            "سفارش شما با موفقیت تحویل داده شده است",
            NotificationType.ORDER_UPDATE,
            NotificationPriority.HIGH,
            LocalDateTime.now().minusMinutes(10),
            false
        ));
        
        testNotifications.add(new NotificationItem(
            "پرداخت موفق",
            "پرداخت شما با موفقیت انجام شد",
            NotificationType.PAYMENT,
            NotificationPriority.MEDIUM,
            LocalDateTime.now().minusHours(1),
            true
        ));
    }

    @Nested
    @DisplayName("تست های مقداردهی اولیه کنترلر")
    class InitializationTests {

        @Test
        @DisplayName("تست مقداردهی اولیه تنظیمات پیش فرض")
        void testDefaultSettings() {
            NotificationSettings defaultSettings = new NotificationSettings();
            
            assertTrue(defaultSettings.isEmailEnabled(), "ایمیل باید پیش فرض فعال باشد");
            assertTrue(defaultSettings.isSmsEnabled(), "پیامک باید پیش فرض فعال باشد");
            assertTrue(defaultSettings.isPushEnabled(), "Push باید پیش فرض فعال باشد");
            assertTrue(defaultSettings.isSoundEnabled(), "صدا باید پیش فرض فعال باشد");
            assertFalse(defaultSettings.isDoNotDisturb(), "مزاحم نشوید باید پیش فرض غیرفعال باشد");
            assertEquals(70.0, defaultSettings.getVolume(), 0.1, "حجم پیش فرض باید 70 باشد");
        }
    }

    @Nested
    @DisplayName("تست های مدل های داده")
    class DataModelTests {

        @Test
        @DisplayName("تست مدل NotificationItem")
        void testNotificationItemModel() {
            assertNotNull(testNotification, "NotificationItem نباید null باشد");
            assertEquals("تست اطلاع رسانی", testNotification.getTitle(), "تست عنوان");
            assertEquals("این یک پیام تست است", testNotification.getMessage(), "تست پیام");
            assertEquals(NotificationType.ORDER_UPDATE, testNotification.getType(), "تست نوع");
            assertEquals(NotificationPriority.MEDIUM, testNotification.getPriority(), "تست اولویت");
            assertFalse(testNotification.isRead(), "وضعیت پیش‌فرض خوانده نشده");
            assertNotNull(testNotification.getTimestamp(), "زمان نباید null باشد");
            
            testNotification.setRead(true);
            assertTrue(testNotification.isRead(), "وضعیت خوانده شده باید تغییر کند");
        }

        @Test
        @DisplayName("تست مدل NotificationSettings")
        void testNotificationSettingsModel() {
            assertTrue(testSettings.isEmailEnabled(), "ایمیل فعال");
            assertTrue(testSettings.isSmsEnabled(), "پیامک فعال");
            assertTrue(testSettings.isPushEnabled(), "Push فعال");
            assertTrue(testSettings.isSoundEnabled(), "صدا فعال");
            assertEquals(75.0, testSettings.getVolume(), "حجم صدا");
            
            testSettings.setEmailEnabled(false);
            assertFalse(testSettings.isEmailEnabled(), "تغییر وضعیت ایمیل");
            
            testSettings.setVolume(50.0);
            assertEquals(50.0, testSettings.getVolume(), "تغییر حجم صدا");
        }
    }

    @Nested
    @DisplayName("تست های Enum ها")
    class EnumTests {

        @Test
        @DisplayName("تست NotificationType enum")
        void testNotificationType() {
            NotificationType[] types = NotificationType.values();
            assertTrue(types.length >= 6, "باید حداقل 6 نوع اطلاع رسانی وجود داشته باشد");
            
            assertEquals("بروزرسانی سفارش", NotificationType.ORDER_UPDATE.getDisplayName());
            assertEquals("پرداخت", NotificationType.PAYMENT.getDisplayName());
            assertEquals("تبلیغات", NotificationType.PROMOTION.getDisplayName());
            assertEquals("سیستم", NotificationType.SYSTEM.getDisplayName());
        }

        @Test
        @DisplayName("تست NotificationPriority enum")
        void testNotificationPriority() {
            NotificationPriority[] priorities = NotificationPriority.values();
            assertEquals(4, priorities.length, "باید 4 سطح اولویت وجود داشته باشد");
            
            assertEquals("کم", NotificationPriority.LOW.getDisplayName());
            assertEquals("متوسط", NotificationPriority.MEDIUM.getDisplayName());
            assertEquals("بالا", NotificationPriority.HIGH.getDisplayName());
            assertEquals("بحرانی", NotificationPriority.CRITICAL.getDisplayName());
        }

        @Test
        @DisplayName("تست NotificationFilter enum")
        void testNotificationFilter() {
            NotificationFilter[] filters = NotificationFilter.values();
            assertEquals(4, filters.length, "باید 4 نوع فیلتر وجود داشته باشد");
            
            assertEquals("همه", NotificationFilter.ALL.toString());
            assertEquals("خوانده نشده", NotificationFilter.UNREAD.toString());
            assertEquals("خوانده شده", NotificationFilter.read.toString());
            assertEquals("اولویت بالا", NotificationFilter.HIGH_PRIORITY.toString());
        }
    }

    @Nested
    @DisplayName("تست های عملکردی")
    class FunctionalTests {

        @Test
        @DisplayName("تست فیلتر اطلاع رسانی ها")
        void testNotificationFiltering() {
            long unreadCount = testNotifications.stream()
                .filter(n -> !n.isRead())
                .count();
            assertEquals(1, unreadCount, "تعداد اطلاع رسانی های خوانده نشده");
            
            long readCount = testNotifications.stream()
                .filter(NotificationItem::isRead)
                .count();
            assertEquals(1, readCount, "تعداد اطلاع رسانی های خوانده شده");
        }

        @Test
        @DisplayName("تست جستجو در اطلاع رسانی ها")
        void testNotificationSearch() {
            long orderNotifications = testNotifications.stream()
                .filter(n -> n.getTitle().toLowerCase().contains("سفارش"))
                .count();
            assertEquals(1, orderNotifications, "تعداد اطلاع رسانی های حاوی کلمه سفارش");
            
            long paymentNotifications = testNotifications.stream()
                .filter(n -> n.getMessage().toLowerCase().contains("پرداخت"))
                .count();
            assertEquals(1, paymentNotifications, "تعداد اطلاع رسانی های حاوی کلمه پرداخت");
        }
    }

    @Nested
    @DisplayName("تست های تنظیمات اطلاع رسانی")
    class SettingsTests {

        @Test
        @DisplayName("تست تنظیمات کانال های اطلاع رسانی")
        void testNotificationChannels() {
            assertTrue(testSettings.isEmailEnabled(), "ایمیل پیش فرض فعال");
            assertTrue(testSettings.isSmsEnabled(), "پیامک پیش فرض فعال");
            assertTrue(testSettings.isPushEnabled(), "Push پیش فرض فعال");
            
            testSettings.setEmailEnabled(false);
            testSettings.setSmsEnabled(false);
            testSettings.setPushEnabled(false);
            
            assertFalse(testSettings.isEmailEnabled(), "ایمیل غیرفعال شد");
            assertFalse(testSettings.isSmsEnabled(), "پیامک غیرفعال شد");
            assertFalse(testSettings.isPushEnabled(), "Push غیرفعال شد");
        }

        @Test
        @DisplayName("تست تنظیمات صدا")
        void testSoundSettings() {
            assertTrue(testSettings.isSoundEnabled(), "صدا پیش فرض فعال");
            assertEquals(75.0, testSettings.getVolume(), "حجم پیش فرض");
            
            testSettings.setVolume(0.0);
            assertEquals(0.0, testSettings.getVolume(), "حجم صفر");
            
            testSettings.setVolume(100.0);
            assertEquals(100.0, testSettings.getVolume(), "حجم حداکثر");
            
            testSettings.setSoundEnabled(false);
            assertFalse(testSettings.isSoundEnabled(), "صدا غیرفعال شد");
        }
    }
} 
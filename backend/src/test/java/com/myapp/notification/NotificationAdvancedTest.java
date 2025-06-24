package com.myapp.notification;

import com.myapp.common.TestDatabaseManager;
import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.auth.AuthRepository;

import org.junit.jupiter.api.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationAdvancedTest {

    private static TestDatabaseManager testDatabaseManager;
    private static NotificationService notificationService;
    private static NotificationRepository notificationRepository;
    private static AuthRepository authRepository;
    private static User testUser1, testUser2, inactiveUser;

    @BeforeAll
    static void setUpClass() {
        testDatabaseManager = new TestDatabaseManager();
        testDatabaseManager.setupTestDatabase();
        
        notificationRepository = new NotificationRepository();
        authRepository = new AuthRepository();
        notificationService = new NotificationService(notificationRepository, authRepository);
        
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
            testUser1.setFullName("Advanced Test User 1");
            testUser1.setEmail("advanced1@example.com");
            testUser1.setPhone("1111111111");
            testUser1.setPasswordHash("password123");
            testUser1.setIsActive(true);
            session.persist(testUser1);
            
            testUser2 = new User();
            testUser2.setFullName("Advanced Test User 2");
            testUser2.setEmail("advanced2@example.com");
            testUser2.setPhone("2222222222");
            testUser2.setPasswordHash("password123");
            testUser2.setIsActive(true);
            session.persist(testUser2);
            
            inactiveUser = new User();
            inactiveUser.setFullName("Inactive User");
            inactiveUser.setEmail("inactive@example.com");
            inactiveUser.setPhone("9999999999");
            inactiveUser.setPasswordHash("password123");
            inactiveUser.setIsActive(false);
            session.persist(inactiveUser);
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should validate all notification types and priorities comprehensively")
    void shouldValidateAllNotificationTypesAndPrioritiesComprehensively() {
        // Test all notification types
        NotificationType[] allTypes = NotificationType.values();
        for (NotificationType type : allTypes) {
            Notification notification = notificationService.createNotification(
                testUser1.getId(), "Test " + type, "Message for " + type, type
            );
            assertNotNull(notification);
            assertEquals(type, notification.getType());
            assertEquals(NotificationPriority.NORMAL, notification.getPriority()); // Default priority
        }
        
        // Test all priorities
        NotificationPriority[] allPriorities = NotificationPriority.values();
        for (NotificationPriority priority : allPriorities) {
            Notification notification = notificationService.createNotification(
                testUser1.getId(), "Test " + priority, "Message for " + priority, 
                NotificationType.PROMOTIONAL, priority
            );
            assertNotNull(notification);
            assertEquals(priority, notification.getPriority());
            assertEquals(NotificationType.PROMOTIONAL, notification.getType());
        }
        
        // Verify all were created
        List<Notification> allNotifications = notificationService.getUserNotifications(testUser1.getId());
        assertEquals(allTypes.length + allPriorities.length, allNotifications.size());
        
        // Test combinations
        for (NotificationType type : allTypes) {
            for (NotificationPriority priority : allPriorities) {
                Notification notification = notificationService.createNotification(
                    testUser1.getId(), "Combo " + type + " " + priority, "Message", type, priority
                );
                assertEquals(type, notification.getType());
                assertEquals(priority, notification.getPriority());
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("Should handle comprehensive validation scenarios")
    void shouldHandleComprehensiveValidationScenarios() {
        // Test boundary values - exactly at limits
        String maxTitle = "A".repeat(100);
        String maxMessage = "B".repeat(500);
        
        Notification maxNotification = notificationService.createNotification(
            testUser1.getId(), maxTitle, maxMessage, NotificationType.ORDER_CREATED
        );
        assertNotNull(maxNotification);
        assertEquals(maxTitle, maxNotification.getTitle());
        assertEquals(maxMessage, maxNotification.getMessage());
        
        // Test minimum valid values
        Notification minNotification = notificationService.createNotification(
            testUser1.getId(), "A", "B", NotificationType.ORDER_CREATED
        );
        assertNotNull(minNotification);
        assertEquals("A", minNotification.getTitle());
        assertEquals("B", minNotification.getMessage());
        
        // Test over-limit values
        String overLimitTitle = "A".repeat(101);
        String overLimitMessage = "B".repeat(501);
        
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), overLimitTitle, "Message", NotificationType.ORDER_CREATED));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), "Title", overLimitMessage, NotificationType.ORDER_CREATED));
        
        // Test null and empty validations
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(null, "Title", "Message", NotificationType.ORDER_CREATED));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), null, "Message", NotificationType.ORDER_CREATED));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), "Title", null, NotificationType.ORDER_CREATED));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), "", "Message", NotificationType.ORDER_CREATED));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), "Title", "", NotificationType.ORDER_CREATED));
        
        // Test whitespace-only strings
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), "   ", "Message", NotificationType.ORDER_CREATED));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(testUser1.getId(), "Title", "   ", NotificationType.ORDER_CREATED));
        
        // Test invalid user IDs
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(-1L, "Title", "Message", NotificationType.ORDER_CREATED));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.createNotification(0L, "Title", "Message", NotificationType.ORDER_CREATED));
        
        // Test non-existent user
        assertThrows(RuntimeException.class, () -> 
            notificationService.createNotification(999999L, "Title", "Message", NotificationType.ORDER_CREATED));
    }

    @Test
    @Order(3)
    @DisplayName("Should test all factory methods comprehensively")
    void shouldTestAllFactoryMethodsComprehensively() {
        Long orderId = 100L;
        Long restaurantId = 200L;
        Long deliveryId = 300L;
        String restaurantName = "پیتزا ایتالیایی";
        String courierName = "علی احمدی";
        Double amount = 125.50;
        LocalDateTime maintenanceTime = LocalDateTime.now().plusHours(2);
        
        // Test order created
        Notification orderCreated = notificationService.notifyOrderCreated(testUser1.getId(), orderId, restaurantName);
        assertNotNull(orderCreated);
        assertEquals(NotificationType.ORDER_CREATED, orderCreated.getType());
        assertEquals(NotificationPriority.NORMAL, orderCreated.getPriority());
        assertEquals(orderId, orderCreated.getOrderId());
        assertEquals(orderId, orderCreated.getRelatedEntityId());
        assertTrue(orderCreated.getMessage().contains(restaurantName));
        
        // Test all order statuses
        OrderStatus[] allStatuses = OrderStatus.values();
        for (OrderStatus status : allStatuses) {
            Notification statusNotification = notificationService.notifyOrderStatusChanged(testUser1.getId(), orderId, status);
            assertNotNull(statusNotification);
            assertEquals(NotificationType.ORDER_STATUS_CHANGED, statusNotification.getType());
            assertEquals(NotificationPriority.HIGH, statusNotification.getPriority());
            assertEquals(orderId, statusNotification.getOrderId());
            assertNotNull(statusNotification.getMessage());
        }
        
        // Test delivery assigned
        Notification deliveryAssigned = notificationService.notifyDeliveryAssigned(testUser1.getId(), orderId, deliveryId, courierName);
        assertNotNull(deliveryAssigned);
        assertEquals(NotificationType.DELIVERY_ASSIGNED, deliveryAssigned.getType());
        assertEquals(NotificationPriority.HIGH, deliveryAssigned.getPriority());
        assertEquals(orderId, deliveryAssigned.getOrderId());
        assertEquals(deliveryId, deliveryAssigned.getDeliveryId());
        assertEquals(orderId, deliveryAssigned.getRelatedEntityId());
        assertTrue(deliveryAssigned.getMessage().contains(courierName));
        
        // Test successful payment
        Notification paymentSuccess = notificationService.notifyPaymentProcessed(testUser1.getId(), orderId, amount, true);
        assertNotNull(paymentSuccess);
        assertEquals(NotificationType.PAYMENT_UPDATE, paymentSuccess.getType());
        assertEquals(NotificationPriority.NORMAL, paymentSuccess.getPriority());
        assertEquals(orderId, paymentSuccess.getOrderId());
        assertTrue(paymentSuccess.getMessage().contains("موفق"));
        assertTrue(paymentSuccess.getMessage().contains(amount.toString()));
        
        // Test failed payment
        Notification paymentFailed = notificationService.notifyPaymentProcessed(testUser1.getId(), orderId, amount, false);
        assertNotNull(paymentFailed);
        assertEquals(NotificationType.PAYMENT_UPDATE, paymentFailed.getType());
        assertEquals(NotificationPriority.HIGH, paymentFailed.getPriority());
        assertTrue(paymentFailed.getMessage().contains("ناموفق"));
        
        // Test restaurant approved
        Notification restaurantApproved = notificationService.notifyRestaurantApproved(testUser1.getId(), restaurantId, restaurantName);
        assertNotNull(restaurantApproved);
        assertEquals(NotificationType.RESTAURANT_APPROVED, restaurantApproved.getType());
        assertEquals(NotificationPriority.HIGH, restaurantApproved.getPriority());
        assertEquals(restaurantId, restaurantApproved.getRestaurantId());
        assertEquals(restaurantId, restaurantApproved.getRelatedEntityId());
        assertTrue(restaurantApproved.getMessage().contains(restaurantName));
        
        // Test system maintenance
        Notification systemMaintenance = notificationService.notifySystemMaintenance(testUser1.getId(), maintenanceTime);
        assertNotNull(systemMaintenance);
        assertEquals(NotificationType.SYSTEM_MAINTENANCE, systemMaintenance.getType());
        assertEquals(NotificationPriority.HIGH, systemMaintenance.getPriority());
        assertTrue(systemMaintenance.getMessage().contains(maintenanceTime.toString()));
        
        // Verify all factory method validations
        testFactoryMethodValidations();
    }

    private void testFactoryMethodValidations() {
        // Order created validations
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyOrderCreated(testUser1.getId(), null, "Restaurant"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyOrderCreated(testUser1.getId(), -1L, "Restaurant"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyOrderCreated(testUser1.getId(), 0L, "Restaurant"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyOrderCreated(testUser1.getId(), 100L, null));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyOrderCreated(testUser1.getId(), 100L, ""));
        
        // Order status changed validations
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyOrderStatusChanged(testUser1.getId(), null, OrderStatus.PENDING));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyOrderStatusChanged(testUser1.getId(), 100L, null));
        
        // Delivery assigned validations
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyDeliveryAssigned(testUser1.getId(), null, 200L, "Courier"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyDeliveryAssigned(testUser1.getId(), 100L, null, "Courier"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyDeliveryAssigned(testUser1.getId(), 100L, -1L, "Courier"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyDeliveryAssigned(testUser1.getId(), 100L, 200L, null));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyDeliveryAssigned(testUser1.getId(), 100L, 200L, ""));
        
        // Payment processed validations
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyPaymentProcessed(testUser1.getId(), null, 50.0, true));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyPaymentProcessed(testUser1.getId(), 100L, null, true));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyPaymentProcessed(testUser1.getId(), 100L, -10.0, true));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyPaymentProcessed(testUser1.getId(), 100L, 0.0, true));
        
        // Restaurant approved validations
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyRestaurantApproved(testUser1.getId(), null, "Restaurant"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyRestaurantApproved(testUser1.getId(), -1L, "Restaurant"));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyRestaurantApproved(testUser1.getId(), 100L, null));
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifyRestaurantApproved(testUser1.getId(), 100L, ""));
        
        // System maintenance validations
        assertThrows(IllegalArgumentException.class, () -> 
            notificationService.notifySystemMaintenance(testUser1.getId(), null));
    }

    @Test
    @Order(4)
    @DisplayName("Should handle performance under significant load")
    void shouldHandlePerformanceUnderSignificantLoad() {
        // Test high-volume creation
        int notificationCount = 500;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < notificationCount; i++) {
            NotificationType type = NotificationType.values()[i % NotificationType.values().length];
            NotificationPriority priority = NotificationPriority.values()[i % NotificationPriority.values().length];
            
            notificationService.createNotification(
                testUser1.getId(), 
                "Performance Test " + i, 
                "Message for performance test number " + i, 
                type, 
                priority
            );
        }
        
        long creationTime = System.currentTimeMillis() - startTime;
        assertTrue(creationTime < 10000, "High-volume creation took too long: " + creationTime + "ms");
        
        // Test comprehensive query performance
        startTime = System.currentTimeMillis();
        
        List<Notification> allNotifications = notificationService.getUserNotifications(testUser1.getId());
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(testUser1.getId());
        long unreadCount = notificationService.getUnreadCount(testUser1.getId());
        long highPriorityUnread = notificationService.getHighPriorityUnreadCount(testUser1.getId());
        boolean hasHighPriorityUnread = notificationService.hasUnreadHighPriorityNotifications(testUser1.getId());
        Optional<Notification> latest = notificationService.getLatestNotification(testUser1.getId());
        
        // Test type-specific queries
        for (NotificationType type : NotificationType.values()) {
            notificationService.getNotificationsByType(testUser1.getId(), type);
            notificationService.getNotificationCountByType(testUser1.getId(), type);
        }
        
        // Test priority-specific queries
        for (NotificationPriority priority : NotificationPriority.values()) {
            notificationService.getNotificationsByPriority(testUser1.getId(), priority);
        }
        
        // Test time-based queries
        notificationService.getRecentNotifications(testUser1.getId(), 1);
        notificationService.getRecentNotifications(testUser1.getId(), 7);
        notificationService.getRecentNotifications(testUser1.getId(), 30);
        
        // Test statistics
        notificationService.getNotificationStatsByType(testUser1.getId());
        notificationService.getDailyNotificationCounts(testUser1.getId(), 7);
        notificationService.getDailyNotificationCounts(testUser1.getId(), 30);
        
        long queryTime = System.currentTimeMillis() - startTime;
        assertTrue(queryTime < 5000, "Comprehensive queries took too long: " + queryTime + "ms");
        
        // Verify results
        assertEquals(notificationCount, allNotifications.size());
        assertEquals(notificationCount, unreadNotifications.size());
        assertEquals(notificationCount, unreadCount);
        assertTrue(highPriorityUnread > 0);
        assertTrue(hasHighPriorityUnread);
        assertTrue(latest.isPresent());
        
        // Test bulk operations performance
        startTime = System.currentTimeMillis();
        int marked = notificationService.markAllAsRead(testUser1.getId());
        long markTime = System.currentTimeMillis() - startTime;
        
        assertEquals(notificationCount, marked);
        assertTrue(markTime < 3000, "Bulk mark as read took too long: " + markTime + "ms");
        
        // Verify final state
        assertEquals(0, notificationService.getUnreadCount(testUser1.getId()));
        assertFalse(notificationService.hasUnreadHighPriorityNotifications(testUser1.getId()));
    }
} 
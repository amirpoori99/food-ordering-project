package com.myapp.notification;

import com.myapp.common.models.Notification;
import com.myapp.common.models.Notification.NotificationType;
import com.myapp.common.models.Notification.NotificationPriority;
import com.myapp.common.models.OrderStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * کلاس تست جامع برای NotificationController
 * 
 * این کلاس تمام endpoint های HTTP مربوط به اعلان‌ها را تست می‌کند:
 * 
 * === دسته‌بندی تست‌ها ===
 * - HandleMethodRoutingTest: تست مسیریابی HTTP methods
 * - GetEndpointsTest: تست تمام GET endpoints
 * - PostEndpointsTest: تست تمام POST endpoints  
 * - PutEndpointsTest: تست تمام PUT endpoints
 * - DeleteEndpointsTest: تست تمام DELETE endpoints
 * - StatisticsEndpointsTest: تست endpoint های آمار
 * - SpecializedEndpointsTest: تست endpoint های تخصصی
 * - MaintenanceEndpointsTest: تست endpoint های نگهداری
 * - ErrorHandlingTest: تست مدیریت خطاها
 * 
 * === ویژگی‌های تست ===
 * - Mock HTTP Exchange: شبیه‌سازی HTTP requests/responses
 * - JSON Processing: تست پردازش JSON
 * - URL Pattern Testing: تست الگوهای URL مختلف
 * - Error Scenario Coverage: پوشش سناریوهای خطا
 * - Response Validation: تایید صحت responses
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
class NotificationControllerTest {

    /** Mock سرویس اعلان‌ها */
    @Mock
    private NotificationService notificationService;
    
    /** Mock HTTP Exchange برای شبیه‌سازی requests */
    @Mock
    private HttpExchange exchange;
    
    /** کنترلر تحت تست */
    private NotificationController notificationController;
    
    /** Stream برای capture کردن response body */
    private ByteArrayOutputStream responseBody;

    /**
     * Helper method برای verification response headers با در نظر گیری actual content length
     * به جای انتظار exact -1L، هر content length valid را قبول می‌کند
     */
    private void verifyResponseHeaders(int expectedStatusCode) throws IOException {
        verify(exchange).sendResponseHeaders(eq(expectedStatusCode), anyLong());
    }

    @BeforeEach
    void setUp() {
        notificationService = Mockito.mock(NotificationService.class);
        exchange = Mockito.mock(HttpExchange.class);
        notificationController = new NotificationController(notificationService);
        responseBody = new ByteArrayOutputStream();
        
        // Mock response headers
        Headers responseHeaders = Mockito.mock(Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseBody);
    }

    @Nested
    @DisplayName("Handle Method Routing")
    class HandleMethodRoutingTest {

        @Test
        @DisplayName("Should route GET requests correctly")
        void shouldRouteGetRequestsCorrectly() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications?userId=1"));
            
            // Mock successful response
            List<Notification> notifications = Arrays.asList(
                new Notification(1L, "Test", "Message", NotificationType.ORDER_CREATED)
            );
            when(notificationService.getUserNotifications(1L)).thenReturn(notifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }

        @Test
        @DisplayName("Should route POST requests correctly")
        void shouldRoutePostRequestsCorrectly() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications"));
            
            String requestBody = "{\"userId\":1,\"title\":\"Test\",\"message\":\"Test Message\",\"type\":\"ORDER_CREATED\"}";
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            Notification notification = new Notification(1L, "Test", "Test Message", NotificationType.ORDER_CREATED);
            when(notificationService.createNotification(anyLong(), anyString(), anyString(), any(NotificationType.class)))
                .thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(201), anyLong());
        }

        @Test
        @DisplayName("Should route PUT requests correctly")
        void shouldRoutePutRequestsCorrectly() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/1/read"));
            
            Notification notification = new Notification(1L, "Test", "Message", NotificationType.ORDER_CREATED);
            notification.setId(1L);
            when(notificationService.markAsRead(1L)).thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }

        @Test
        @DisplayName("Should route DELETE requests correctly")
        void shouldRouteDeleteRequestsCorrectly() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/1"));
            
            doNothing().when(notificationService).deleteNotification(1L);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
        }

        @Test
        @DisplayName("Should return 405 for unsupported methods")
        void shouldReturn405ForUnsupportedMethods() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PATCH");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(405), anyLong());
        }
    }

    @Nested
    @DisplayName("GET Endpoints")
    class GetEndpointsTest {

        @Test
        @DisplayName("Should get user notifications successfully")
        void shouldGetUserNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications?userId=1"));
            
            List<Notification> notifications = Arrays.asList(
                new Notification(1L, "Test 1", "Message 1", NotificationType.ORDER_CREATED),
                new Notification(1L, "Test 2", "Message 2", NotificationType.ORDER_STATUS_CHANGED)
            );
            when(notificationService.getUserNotifications(1L)).thenReturn(notifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getUserNotifications(1L);
            
            String response = responseBody.toString();
            assertTrue(response.contains("Test 1"));
            assertTrue(response.contains("Test 2"));
        }

        @Test
        @DisplayName("Should get user notifications paginated successfully")
        void shouldGetUserNotificationsPaginatedSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications?userId=1&page=0&size=10"));
            
            List<Notification> notifications = Arrays.asList(
                new Notification(1L, "Page Test", "Message", NotificationType.ORDER_CREATED)
            );
            when(notificationService.getUserNotificationsPaginated(1L, 0, 10)).thenReturn(notifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getUserNotificationsPaginated(1L, 0, 10);
        }

        @Test
        @DisplayName("Should get unread notifications successfully")
        void shouldGetUnreadNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/unread?userId=1"));
            
            List<Notification> unreadNotifications = Arrays.asList(
                new Notification(1L, "Unread", "Message", NotificationType.ORDER_CREATED)
            );
            when(notificationService.getUnreadNotifications(1L)).thenReturn(unreadNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getUnreadNotifications(1L);
        }

        @Test
        @DisplayName("Should get notifications by type successfully")
        void shouldGetNotificationsByTypeSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/type?userId=1&type=ORDER_CREATED"));
            
            List<Notification> typeNotifications = Arrays.asList(
                new Notification(1L, "Order", "Message", NotificationType.ORDER_CREATED)
            );
            when(notificationService.getNotificationsByType(1L, NotificationType.ORDER_CREATED)).thenReturn(typeNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getNotificationsByType(1L, NotificationType.ORDER_CREATED);
        }

        @Test
        @DisplayName("Should get notifications by priority successfully")
        void shouldGetNotificationsByPrioritySuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/priority?userId=1&priority=HIGH"));
            
            List<Notification> priorityNotifications = Arrays.asList(
                new Notification(1L, "High Priority", "Message", NotificationType.SYSTEM_MAINTENANCE, NotificationPriority.HIGH)
            );
            when(notificationService.getNotificationsByPriority(1L, NotificationPriority.HIGH)).thenReturn(priorityNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getNotificationsByPriority(1L, NotificationPriority.HIGH);
        }

        @Test
        @DisplayName("Should get high priority notifications successfully")
        void shouldGetHighPriorityNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/high-priority?userId=1"));
            
            List<Notification> highPriorityNotifications = Arrays.asList(
                new Notification(1L, "High Priority", "Message", NotificationType.SYSTEM_MAINTENANCE, NotificationPriority.HIGH)
            );
            when(notificationService.getHighPriorityNotifications(1L)).thenReturn(highPriorityNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getHighPriorityNotifications(1L);
        }

        @Test
        @DisplayName("Should get recent notifications successfully")
        void shouldGetRecentNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/recent?userId=1&days=7"));
            
            List<Notification> recentNotifications = Arrays.asList(
                new Notification(1L, "Recent", "Message", NotificationType.ORDER_CREATED)
            );
            when(notificationService.getRecentNotifications(1L, 7)).thenReturn(recentNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getRecentNotifications(1L, 7);
        }

        @Test
        @DisplayName("Should get notification by ID successfully")
        void shouldGetNotificationByIdSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/1"));
            
            Notification notification = new Notification(1L, "Test", "Message", NotificationType.ORDER_CREATED);
            notification.setId(1L);
            when(notificationService.getNotificationById(1L)).thenReturn(Optional.of(notification));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getNotificationById(1L);
        }

        @Test
        @DisplayName("Should return 404 for non-existent notification")
        void shouldReturn404ForNonExistentNotification() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/999"));
            
            when(notificationService.getNotificationById(999L)).thenReturn(Optional.empty());
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(404), anyLong());
            verify(notificationService).getNotificationById(999L);
        }

        @Test
        @DisplayName("Should get unread count successfully")
        void shouldGetUnreadCountSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/count/unread?userId=1"));
            
            when(notificationService.getUnreadCount(1L)).thenReturn(5L);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getUnreadCount(1L);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"count\":5"));
        }

        @Test
        @DisplayName("Should return 400 for missing userId parameter")
        void shouldReturn400ForMissingUserIdParameter() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(400), anyLong());
        }
    }

    @Nested
    @DisplayName("POST Endpoints")
    class PostEndpointsTest {

        @Test
        @DisplayName("Should create notification successfully")
        void shouldCreateNotificationSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications"));
            
            String requestBody = "{\"userId\":1,\"title\":\"Test Title\",\"message\":\"Test Message\",\"type\":\"ORDER_CREATED\"}";
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            Notification notification = new Notification(1L, "Test Title", "Test Message", NotificationType.ORDER_CREATED);
            notification.setId(1L);
            when(notificationService.createNotification(1L, "Test Title", "Test Message", NotificationType.ORDER_CREATED))
                .thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(201), anyLong());
            verify(notificationService).createNotification(1L, "Test Title", "Test Message", NotificationType.ORDER_CREATED);
        }

        @Test
        @DisplayName("Should create notification with priority successfully")
        void shouldCreateNotificationWithPrioritySuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications"));
            
            String requestBody = "{\"userId\":1,\"title\":\"High Priority\",\"message\":\"Important Message\",\"type\":\"SYSTEM_MAINTENANCE\",\"priority\":\"HIGH\"}";
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            Notification notification = new Notification(1L, "High Priority", "Important Message", NotificationType.SYSTEM_MAINTENANCE, NotificationPriority.HIGH);
            notification.setId(1L);
            when(notificationService.createNotification(1L, "High Priority", "Important Message", NotificationType.SYSTEM_MAINTENANCE, NotificationPriority.HIGH))
                .thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(201), anyLong());
            verify(notificationService).createNotification(1L, "High Priority", "Important Message", NotificationType.SYSTEM_MAINTENANCE, NotificationPriority.HIGH);
        }

        @Test
        @DisplayName("Should create order notification successfully")
        void shouldCreateOrderNotificationSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/order/created"));
            
            String requestBody = "{\"userId\":1,\"orderId\":100,\"restaurantName\":\"Test Restaurant\"}";
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            Notification notification = Notification.orderCreated(1L, 100L, "Test Restaurant");
            notification.setId(1L);
            when(notificationService.notifyOrderCreated(1L, 100L, "Test Restaurant")).thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(201), anyLong());
            verify(notificationService).notifyOrderCreated(1L, 100L, "Test Restaurant");
        }

        @Test
        @DisplayName("Should create order status changed notification successfully")
        void shouldCreateOrderStatusChangedNotificationSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/order/status-changed"));
            
            String requestBody = "{\"userId\":1,\"orderId\":100,\"newStatus\":\"PREPARING\"}";
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            Notification notification = Notification.orderStatusChanged(1L, 100L, OrderStatus.PREPARING);
            notification.setId(1L);
            when(notificationService.notifyOrderStatusChanged(1L, 100L, OrderStatus.PREPARING)).thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(201), anyLong());
            verify(notificationService).notifyOrderStatusChanged(1L, 100L, OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("Should create delivery assigned notification successfully")
        void shouldCreateDeliveryAssignedNotificationSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/delivery/assigned"));
            
            String requestBody = "{\"userId\":1,\"orderId\":100,\"deliveryId\":200,\"courierName\":\"John Doe\"}";
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            Notification notification = Notification.deliveryAssigned(1L, 100L, 200L, "John Doe");
            notification.setId(1L);
            when(notificationService.notifyDeliveryAssigned(1L, 100L, 200L, "John Doe")).thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(201), anyLong());
            verify(notificationService).notifyDeliveryAssigned(1L, 100L, 200L, "John Doe");
        }

        @Test
        @DisplayName("Should return 400 for invalid JSON")
        void shouldReturn400ForInvalidJson() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications"));
            
            String invalidJson = "{invalid json}";
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(invalidJson.getBytes()));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(400), anyLong());
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void shouldReturn400ForMissingRequiredFields() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications"));
            
            String requestBody = "{\"title\":\"Test Title\"}"; // Missing userId, message, type
            when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(400), anyLong());
        }
    }

    @Nested
    @DisplayName("PUT Endpoints")
    class PutEndpointsTest {

        @Test
        @DisplayName("Should mark notification as read successfully")
        void shouldMarkNotificationAsReadSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/1/read"));
            
            Notification notification = new Notification(1L, "Test", "Message", NotificationType.ORDER_CREATED);
            notification.setId(1L);
            notification.markAsRead();
            when(notificationService.markAsRead(1L)).thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).markAsRead(1L);
        }

        @Test
        @DisplayName("Should mark notification as unread successfully")
        void shouldMarkNotificationAsUnreadSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/1/unread"));
            
            Notification notification = new Notification(1L, "Test", "Message", NotificationType.ORDER_CREATED);
            notification.setId(1L);
            when(notificationService.markAsUnread(1L)).thenReturn(notification);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).markAsUnread(1L);
        }

        @Test
        @DisplayName("Should mark all notifications as read successfully")
        void shouldMarkAllNotificationsAsReadSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/mark-all-read?userId=1"));
            
            when(notificationService.markAllAsRead(1L)).thenReturn(5);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).markAllAsRead(1L);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"updated\":5"));
        }

        @Test
        @DisplayName("Should mark notifications by type as read successfully")
        void shouldMarkNotificationsByTypeAsReadSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/mark-read-by-type?userId=1&type=ORDER_CREATED"));
            
            when(notificationService.markAllAsReadByType(1L, NotificationType.ORDER_CREATED)).thenReturn(3);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).markAllAsReadByType(1L, NotificationType.ORDER_CREATED);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"updated\":3"));
        }

        @Test
        @DisplayName("Should return 400 for invalid notification ID in path")
        void shouldReturn400ForInvalidNotificationIdInPath() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/invalid/read"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(400), anyLong());
        }

        @Test
        @DisplayName("Should return 404 when marking non-existent notification as read")
        void shouldReturn404WhenMarkingNonExistentNotificationAsRead() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/999/read"));
            
            when(notificationService.markAsRead(999L)).thenThrow(new RuntimeException("Notification not found"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(404), anyLong());
        }
    }

    @Nested
    @DisplayName("DELETE Endpoints")
    class DeleteEndpointsTest {

        @Test
        @DisplayName("Should delete notification successfully")
        void shouldDeleteNotificationSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/1"));
            
            doNothing().when(notificationService).deleteNotification(1L);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).deleteNotification(1L);
        }

        @Test
        @DisplayName("Should restore notification successfully")
        void shouldRestoreNotificationSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("PUT");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/1/restore"));
            
            doNothing().when(notificationService).restoreNotification(1L);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).restoreNotification(1L);
        }

        @Test
        @DisplayName("Should return 400 for invalid notification ID in delete")
        void shouldReturn400ForInvalidNotificationIdInDelete() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/invalid"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(400), anyLong());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent notification")
        void shouldReturn404WhenDeletingNonExistentNotification() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("DELETE");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/999"));
            
            doThrow(new RuntimeException("Notification not found")).when(notificationService).deleteNotification(999L);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(404), anyLong());
        }
    }

    @Nested
    @DisplayName("Statistics Endpoints")
    class StatisticsEndpointsTest {

        @Test
        @DisplayName("Should get notification count by type successfully")
        void shouldGetNotificationCountByTypeSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/count/type?userId=1&type=ORDER_CREATED"));
            
            when(notificationService.getNotificationCountByType(1L, NotificationType.ORDER_CREATED)).thenReturn(3L);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getNotificationCountByType(1L, NotificationType.ORDER_CREATED);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"count\":3"));
        }

        @Test
        @DisplayName("Should get high priority unread count successfully")
        void shouldGetHighPriorityUnreadCountSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/count/high-priority-unread?userId=1"));
            
            when(notificationService.getHighPriorityUnreadCount(1L)).thenReturn(2L);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getHighPriorityUnreadCount(1L);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"count\":2"));
        }

        @Test
        @DisplayName("Should check if user has unread high priority notifications")
        void shouldCheckIfUserHasUnreadHighPriorityNotifications() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/has-high-priority-unread?userId=1"));
            
            when(notificationService.hasUnreadHighPriorityNotifications(1L)).thenReturn(true);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).hasUnreadHighPriorityNotifications(1L);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"hasHighPriorityUnread\":true"));
        }

        @Test
        @DisplayName("Should get latest notification successfully")
        void shouldGetLatestNotificationSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/latest?userId=1"));
            
            Notification latestNotification = new Notification(1L, "Latest", "Message", NotificationType.ORDER_CREATED);
            latestNotification.setId(1L);
            when(notificationService.getLatestNotification(1L)).thenReturn(Optional.of(latestNotification));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getLatestNotification(1L);
        }

        @Test
        @DisplayName("Should return 404 when no latest notification found")
        void shouldReturn404WhenNoLatestNotificationFound() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/latest?userId=1"));
            
            when(notificationService.getLatestNotification(1L)).thenReturn(Optional.empty());
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(404), anyLong());
        }
    }

    @Nested
    @DisplayName("Specialized Endpoints")
    class SpecializedEndpointsTest {

        @Test
        @DisplayName("Should get order notifications successfully")
        void shouldGetOrderNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/order/123"));
            
            List<Notification> orderNotifications = Arrays.asList(
                new Notification(1L, "Order Created", "Message", NotificationType.ORDER_CREATED),
                new Notification(1L, "Order Status Changed", "Message", NotificationType.ORDER_STATUS_CHANGED)
            );
            when(notificationService.getOrderNotifications(123L)).thenReturn(orderNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getOrderNotifications(123L);
        }

        @Test
        @DisplayName("Should get user order notifications successfully")
        void shouldGetUserOrderNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/user/1/order/123"));
            
            List<Notification> userOrderNotifications = Arrays.asList(
                new Notification(1L, "Your Order", "Message", NotificationType.ORDER_CREATED)
            );
            when(notificationService.getUserOrderNotifications(1L, 123L)).thenReturn(userOrderNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getUserOrderNotifications(1L, 123L);
        }

        @Test
        @DisplayName("Should get restaurant notifications successfully")
        void shouldGetRestaurantNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/restaurant/456"));
            
            List<Notification> restaurantNotifications = Arrays.asList(
                new Notification(1L, "Restaurant Approved", "Message", NotificationType.RESTAURANT_APPROVED)
            );
            when(notificationService.getRestaurantNotifications(456L)).thenReturn(restaurantNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getRestaurantNotifications(456L);
        }

        @Test
        @DisplayName("Should get delivery notifications successfully")
        void shouldGetDeliveryNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/delivery/789"));
            
            List<Notification> deliveryNotifications = Arrays.asList(
                new Notification(1L, "Delivery Assigned", "Message", NotificationType.DELIVERY_ASSIGNED)
            );
            when(notificationService.getDeliveryNotifications(789L)).thenReturn(deliveryNotifications);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).getDeliveryNotifications(789L);
        }
    }

    @Nested
    @DisplayName("Maintenance Endpoints")
    class MaintenanceEndpointsTest {

        @Test
        @DisplayName("Should perform daily maintenance successfully")
        void shouldPerformDailyMaintenanceSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/maintenance/daily"));
            
            doNothing().when(notificationService).performDailyMaintenance();
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).performDailyMaintenance();
        }

        @Test
        @DisplayName("Should cleanup old notifications successfully")
        void shouldCleanupOldNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/maintenance/cleanup?days=30"));
            
            when(notificationService.cleanupOldNotifications(30)).thenReturn(10);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).cleanupOldNotifications(30);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"cleaned\":10"));
        }

        @Test
        @DisplayName("Should purge old notifications successfully")
        void shouldPurgeOldNotificationsSuccessfully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("POST");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/maintenance/purge?days=90"));
            
            when(notificationService.purgeOldNotifications(90)).thenReturn(5);
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(200), anyLong());
            verify(notificationService).purgeOldNotifications(90);
            
            String response = responseBody.toString();
            assertTrue(response.contains("\"purged\":5"));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTest {

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications?userId=1"));
            
            when(notificationService.getUserNotifications(1L)).thenThrow(new RuntimeException("Database error"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(500), anyLong());
        }

        @Test
        @DisplayName("Should handle illegal argument exceptions as 400")
        void shouldHandleIllegalArgumentExceptionsAs400() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications?userId=1"));
            
            when(notificationService.getUserNotifications(1L)).thenThrow(new IllegalArgumentException("Invalid user ID"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(400), anyLong());
        }

        @Test
        @DisplayName("Should handle malformed URLs gracefully")
        void shouldHandleMalformedUrlsGracefully() throws IOException {
            // Given
            when(exchange.getRequestMethod()).thenReturn("GET");
            when(exchange.getRequestURI()).thenReturn(URI.create("/api/notifications/malformed/url/structure"));
            
            // When
            notificationController.handle(exchange);
            
            // Then
            verify(exchange).sendResponseHeaders(eq(404), anyLong());
        }
    }
} 

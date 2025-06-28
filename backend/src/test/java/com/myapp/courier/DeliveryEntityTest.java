package com.myapp.courier;

import com.myapp.common.models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * مجموعه تست‌های جامع Delivery Entity
 * 
 * این کلاس تمام رفتارهای entity تحویل را آزمایش می‌کند:
 * 
 * === دسته‌های تست ===
 * 1. Delivery Creation Tests - تست‌های ایجاد تحویل
 *    - ایجاد با پارامترهای معتبر
 *    - ایجاد با مسافت
 *    - validation ورودی‌ها
 *    - مدیریت خطاها
 * 
 * 2. Courier Assignment Tests - تست‌های تخصیص پیک
 *    - تخصیص موفق پیک
 *    - validation نقش کاربر
 *    - مدیریت وضعیت
 *    - تخصیص مجدد
 * 
 * 3. Pickup Process Tests - تست‌های فرآیند pickup
 *    - علامت‌گذاری pickup موفق
 *    - validation وضعیت
 *    - به‌روزرسانی order status
 * 
 * 4. Delivery Completion Tests - تست‌های تکمیل تحویل
 *    - علامت‌گذاری delivery موفق
 *    - validation workflow
 *    - به‌روزرسانی timestamps
 * 
 * 5. Cancellation Tests - تست‌های لغو تحویل
 *    - لغو در وضعیت‌های مختلف
 *    - مدیریت دلیل لغو
 *    - unassign کردن پیک
 * 
 * 6. State Check Tests - تست‌های بررسی وضعیت
 *    - canBeAssigned
 *    - canBePickedUp
 *    - canBeDelivered
 *    - isActive
 * 
 * 7. Time Estimation Tests - تست‌های محاسبه زمان
 *    - estimated pickup time
 *    - estimated delivery time
 *    - محاسبه دقیقه‌ها
 * 
 * 8. Getters/Setters Tests - تست‌های properties
 *    - encapsulation
 *    - property access
 * 
 * === ویژگی‌های تست ===
 * - Entity Behavior Testing: آزمایش رفتار entity
 * - Business Logic Validation: اعتبارسنجی منطق کسب‌وکار
 * - State Transition Testing: تست انتقال وضعیت‌ها
 * - Parameterized Testing: تست با enum values
 * - Edge Case Coverage: پوشش موارد خاص
 * 
 * === Business Rules Testing ===
 * - Delivery workflow enforcement
 * - Courier role validation
 * - Status transition rules
 * - Time management
 * - Order integration
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Delivery Entity Tests")
class DeliveryEntityTest {

    /** کاربر مشتری نمونه */
    private User customer;
    
    /** کاربر پیک نمونه */
    private User courier;
    
    /** رستوران نمونه */
    private Restaurant restaurant;
    
    /** سفارش نمونه */
    private com.myapp.common.models.Order order; // Fully qualified to avoid ambiguity
    
    /** آیتم غذایی نمونه */
    private FoodItem foodItem;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - ایجاد کاربران تست (مشتری، پیک)
     * - ایجاد رستوران نمونه
     * - ایجاد آیتم غذایی
     * - ایجاد سفارش تست
     */
    @BeforeEach
    void setUp() {
        // ایجاد مشتری تست - Constructor: (id, fullName, phone, email, passwordHash, role, address)
        customer = new User(1L, "John Doe", "1234567890", "john@example.com", 
                           "hashedpass", User.Role.BUYER, "123 Main St");
        
        // ایجاد پیک تست - Constructor: (id, fullName, phone, email, passwordHash, role, address)  
        courier = new User(2L, "Mike Wilson", "0987654321", "mike@example.com", 
                          "hashedpass", User.Role.COURIER, "456 Oak Ave");
        
        // ایجاد رستوران تست - Constructor: (id, ownerId, name, address, phone, status)
        restaurant = new Restaurant(1L, 3L, "Test Restaurant", "789 Food St", "5551234567", RestaurantStatus.APPROVED);
        
        // ایجاد آیتم غذایی تست - Constructor: (id, name, description, price, category, imageUrl, quantity, keywords, restaurant)
        foodItem = new FoodItem(1L, "Test Burger", "Delicious test burger", 
                               15.99, "Main Course", "burger.jpg", 10, "burger delicious", restaurant);
        
        // ایجاد سفارش تست
        order = com.myapp.common.models.Order.createNew(customer, restaurant, "123 Main St", "1234567890");
        order.setId(1L);
        order.addItem(foodItem, 2);
        order.setStatus(OrderStatus.READY);
    }

    /**
     * تست‌های ایجاد تحویل
     * 
     * این دسته شامل تمام عملیات مربوط به ایجاد entity تحویل:
     * - ایجاد با پارامترهای معتبر
     * - ایجاد با مسافت
     * - validation ورودی‌ها
     * - مدیریت خطاها
     */
    @Nested
    @DisplayName("Delivery Creation Tests")
    class DeliveryCreationTests {

        /**
         * تست ایجاد موفق تحویل با پارامترهای معتبر
         * 
         * Scenario: ایجاد تحویل جدید با سفارش و هزینه
         * Expected:
         * - تحویل با موفقیت ایجاد شود
         * - تمام فیلدها صحیح تنظیم شوند
         * - وضعیت اولیه PENDING باشد
         */
        @Test
        @DisplayName("✅ ایجاد موفق تحویل با پارامترهای معتبر")
        void createDelivery_ValidParameters_Success() {
            // Act - فراخوانی متد ایجاد
            Delivery delivery = Delivery.createNew(order, 5.99);

            // Assert - بررسی نتایج
            assertNotNull(delivery, "تحویل ایجاد شده نباید null باشد");
            assertEquals(order, delivery.getOrder(), "سفارش باید صحیح تنظیم شود");
            assertEquals(5.99, delivery.getDeliveryFee(), "هزینه تحویل باید صحیح باشد");
            assertEquals(DeliveryStatus.PENDING, delivery.getStatus(), "وضعیت اولیه باید PENDING باشد");
            assertNull(delivery.getCourier(), "پیک در ابتدا نباید تنظیم شود");
            assertNotNull(delivery.getEstimatedPickupTime(), "زمان تخمینی pickup باید تنظیم شود");
            assertNotNull(delivery.getEstimatedDeliveryTime(), "زمان تخمینی delivery باید تنظیم شود");
        }

        /**
         * تست ایجاد تحویل با مسافت
         * 
         * Scenario: ایجاد تحویل شامل مسافت مشخص
         * Expected:
         * - تحویل با موفقیت ایجاد شود
         * - مسافت صحیح ثبت شود
         */
        @Test
        @DisplayName("✅ ایجاد موفق تحویل با مسافت")
        void createDeliveryWithDistance_ValidParameters_Success() {
            // Act
            Delivery delivery = Delivery.createWithDistance(order, 7.50, 5.2);

            // Assert
            assertNotNull(delivery, "تحویل ایجاد شده نباید null باشد");
            assertEquals(order, delivery.getOrder(), "سفارش باید صحیح تنظیم شود");
            assertEquals(7.50, delivery.getDeliveryFee(), "هزینه تحویل باید صحیح باشد");
            assertEquals(5.2, delivery.getDistanceKm(), "مسافت باید صحیح ثبت شود");
            assertEquals(DeliveryStatus.PENDING, delivery.getStatus(), "وضعیت اولیه باید PENDING باشد");
        }

        /**
         * تست خطا برای سفارش null
         * 
         * Scenario: تلاش ایجاد تحویل بدون سفارش
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای سفارش null")
        void createDelivery_NullOrder_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Delivery.createNew(null, 5.99),
                "باید exception پرتاب شود برای سفارش null"
            );
            assertEquals("Order is required", exception.getMessage());
        }

        /**
         * تست خطا برای هزینه منفی
         * 
         * Scenario: تلاش ایجاد تحویل با هزینه منفی
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای هزینه تحویل منفی")
        void createDelivery_NegativeFee_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Delivery.createNew(order, -1.0),
                "باید exception پرتاب شود برای هزینه منفی"
            );
            assertEquals("Delivery fee must be non-negative", exception.getMessage());
        }

        /**
         * تست خطا برای هزینه null
         * 
         * Scenario: تلاش ایجاد تحویل بدون مشخص کردن هزینه
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای هزینه تحویل null")
        void createDelivery_NullFee_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Delivery.createNew(order, null),
                "باید exception پرتاب شود برای هزینه null"
            );
            assertEquals("Delivery fee must be non-negative", exception.getMessage());
        }
    }

    /**
     * تست‌های تخصیص پیک
     * 
     * این دسته شامل تمام عملیات مربوط به تخصیص پیک:
     * - تخصیص موفق پیک
     * - validation نقش کاربر
     * - مدیریت وضعیت
     * - تخصیص مجدد
     */
    @Nested
    @DisplayName("Courier Assignment Tests")
    class CourierAssignmentTests {

        /** تحویل نمونه برای تست‌ها */
        private Delivery delivery;

        /**
         * راه‌اندازی خاص این دسته تست
         */
        @BeforeEach
        void setUp() {
            delivery = Delivery.createNew(order, 5.99);
        }

        /**
         * تست موفق تخصیص پیک
         * 
         * Scenario: تخصیص پیک معتبر به تحویل PENDING
         * Expected:
         * - پیک با موفقیت تخصیص یابد
         * - وضعیت به ASSIGNED تغییر کند
         * - timestamp های مربوطه تنظیم شوند
         */
        @Test
        @DisplayName("✅ تخصیص موفق پیک")
        void assignToCourier_ValidCourier_Success() {
            // Act - تخصیص پیک
            delivery.assignToCourier(courier);

            // Assert - بررسی نتایج
            assertEquals(courier, delivery.getCourier(), "پیک باید صحیح تخصیص یابد");
            assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus(), "وضعیت باید به ASSIGNED تغییر کند");
            assertNotNull(delivery.getAssignedAt(), "زمان تخصیص باید ثبت شود");
            assertNotNull(delivery.getEstimatedPickupTime(), "زمان تخمینی pickup باید به‌روزرسانی شود");
        }

        /**
         * تست خطا برای پیک null
         * 
         * Scenario: تلاش تخصیص پیک null
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای پیک null")
        void assignToCourier_NullCourier_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> delivery.assignToCourier(null),
                "باید exception پرتاب شود برای پیک null"
            );
            assertEquals("Courier cannot be null", exception.getMessage());
        }

        /**
         * تست خطا برای کاربر غیرپیک
         * 
         * Scenario: تلاش تخصیص کاربری که نقش COURIER ندارد
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای کاربر غیرپیک")
        void assignToCourier_NotCourierRole_ThrowsException() {
            // Arrange - ایجاد کاربر غیرپیک
            User nonCourier = new User(3L, "Not Courier", "1111111111", "not@courier.com", 
                                     "pass", User.Role.BUYER, "Not Courier St");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> delivery.assignToCourier(nonCourier),
                "باید exception پرتاب شود برای کاربر غیرپیک"
            );
            assertEquals("User must have COURIER role", exception.getMessage());
        }

        /**
         * تست خطا برای تخصیص مجدد
         * 
         * Scenario: تلاش تخصیص پیک دوم به تحویل قبلاً تخصیص یافته
         * Expected: IllegalStateException پرتاب شود
         */
        @Test
        @DisplayName("❌ خطا برای تخصیص مجدد پیک")
        void assignToCourier_NotPendingStatus_ThrowsException() {
            // Arrange - تخصیص اول
            delivery.assignToCourier(courier); // Status becomes ASSIGNED

            // ایجاد پیک دوم
            User anotherCourier = new User(4L, "Another Courier", "2222222222", "another@courier.com", 
                                         "pass", User.Role.COURIER, "Another St");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> delivery.assignToCourier(anotherCourier),
                "باید exception پرتاب شود برای تخصیص مجدد"
            );
            assertEquals("Can only assign courier to pending deliveries", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Pickup Process Tests")
    class PickupProcessTests {

        private Delivery delivery;

        @BeforeEach
        void setUp() {
            delivery = Delivery.createNew(order, 5.99);
            delivery.assignToCourier(courier);
        }

        @Test
        @DisplayName("Should mark as picked up successfully")
        void markAsPickedUp_ValidState_Success() {
            // Act
            delivery.markAsPickedUp();

            // Assert
            assertEquals(DeliveryStatus.PICKED_UP, delivery.getStatus());
            assertNotNull(delivery.getPickedUpAt());
            assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
            assertNotNull(delivery.getEstimatedDeliveryTime());
        }

        @Test
        @DisplayName("Should throw exception when not assigned")
        void markAsPickedUp_NotAssigned_ThrowsException() {
            // Arrange
            Delivery pendingDelivery = Delivery.createNew(order, 5.99);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> pendingDelivery.markAsPickedUp()
            );
            assertEquals("Delivery must be assigned before pickup", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when no courier assigned")
        void markAsPickedUp_NoCourier_ThrowsException() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setStatus(DeliveryStatus.ASSIGNED); // Manually set status without courier
            delivery.setCourier(null);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> delivery.markAsPickedUp()
            );
            assertEquals("No courier assigned", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delivery Completion Tests")
    class DeliveryCompletionTests {

        private Delivery delivery;

        @BeforeEach
        void setUp() {
            delivery = Delivery.createNew(order, 5.99);
            delivery.assignToCourier(courier);
            delivery.markAsPickedUp();
        }

        @Test
        @DisplayName("Should mark as delivered successfully")
        void markAsDelivered_ValidState_Success() {
            // Act
            delivery.markAsDelivered();

            // Assert
            assertEquals(DeliveryStatus.DELIVERED, delivery.getStatus());
            assertNotNull(delivery.getDeliveredAt());
            assertEquals(OrderStatus.DELIVERED, order.getStatus());
            assertEquals(delivery.getDeliveredAt(), order.getActualDeliveryTime());
        }

        @Test
        @DisplayName("Should throw exception when not picked up")
        void markAsDelivered_NotPickedUp_ThrowsException() {
            // Arrange
            Delivery assignedDelivery = Delivery.createNew(order, 5.99);
            assignedDelivery.assignToCourier(courier);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> assignedDelivery.markAsDelivered()
            );
            assertEquals("Delivery must be picked up before marking as delivered", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cancellation Tests")
    class CancellationTests {

        @ParameterizedTest
        @EnumSource(value = DeliveryStatus.class, names = {"PENDING", "ASSIGNED", "PICKED_UP"})
        @DisplayName("Should cancel delivery in cancellable states")
        void cancel_CancellableStates_Success(DeliveryStatus status) {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setStatus(status);
            if (status != DeliveryStatus.PENDING) {
                delivery.setCourier(courier);
                delivery.setAssignedAt(LocalDateTime.now());
            }

            // Act
            delivery.cancel("Customer request");

            // Assert
            assertEquals(DeliveryStatus.CANCELLED, delivery.getStatus());
            assertEquals("Customer request", delivery.getDeliveryNotes());
            if (status != DeliveryStatus.PENDING) {
                // Courier should be unassigned
                assertNull(delivery.getCourier());
                assertNull(delivery.getAssignedAt());
            }
        }

        @Test
        @DisplayName("Should throw exception when cancelling delivered order")
        void cancel_DeliveredStatus_ThrowsException() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setStatus(DeliveryStatus.DELIVERED);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> delivery.cancel("Too late")
            );
            assertEquals("Cannot cancel delivered order", exception.getMessage());
        }

        @Test
        @DisplayName("Should cancel without reason")
        void cancel_NoReason_Success() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);

            // Act
            delivery.cancel(null);

            // Assert
            assertEquals(DeliveryStatus.CANCELLED, delivery.getStatus());
            assertNull(delivery.getDeliveryNotes());
        }
    }

    @Nested
    @DisplayName("State Check Tests")
    class StateCheckTests {

        @Test
        @DisplayName("Should check if delivery can be assigned")
        void canBeAssigned_PendingStatus_ReturnsTrue() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);

            // Act & Assert
            assertTrue(delivery.canBeAssigned());
        }

        @Test
        @DisplayName("Should check if delivery cannot be assigned")
        void canBeAssigned_NonPendingStatus_ReturnsFalse() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.assignToCourier(courier);

            // Act & Assert
            assertFalse(delivery.canBeAssigned());
        }

        @Test
        @DisplayName("Should check if delivery can be picked up")
        void canBePickedUp_AssignedWithCourier_ReturnsTrue() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.assignToCourier(courier);

            // Act & Assert
            assertTrue(delivery.canBePickedUp());
        }

        @Test
        @DisplayName("Should check if delivery cannot be picked up")
        void canBePickedUp_NoCourier_ReturnsFalse() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);

            // Act & Assert
            assertFalse(delivery.canBePickedUp());
        }

        @Test
        @DisplayName("Should check if delivery can be delivered")
        void canBeDelivered_PickedUpStatus_ReturnsTrue() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.assignToCourier(courier);
            delivery.markAsPickedUp();

            // Act & Assert
            assertTrue(delivery.canBeDelivered());
        }

        @Test
        @DisplayName("Should check if delivery cannot be delivered")
        void canBeDelivered_NotPickedUp_ReturnsFalse() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.assignToCourier(courier);

            // Act & Assert
            assertFalse(delivery.canBeDelivered());
        }

        @ParameterizedTest
        @EnumSource(value = DeliveryStatus.class, names = {"PENDING", "ASSIGNED", "PICKED_UP"})
        @DisplayName("Should identify active deliveries")
        void isActive_ActiveStates_ReturnsTrue(DeliveryStatus status) {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setStatus(status);

            // Act & Assert
            assertTrue(delivery.isActive());
        }

        @ParameterizedTest
        @EnumSource(value = DeliveryStatus.class, names = {"DELIVERED", "CANCELLED"})
        @DisplayName("Should identify inactive deliveries")
        void isActive_InactiveStates_ReturnsFalse(DeliveryStatus status) {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setStatus(status);

            // Act & Assert
            assertFalse(delivery.isActive());
        }
    }

    @Nested
    @DisplayName("Time Estimation Tests")
    class TimeEstimationTests {

        @Test
        @DisplayName("Should calculate estimated pickup minutes")
        void getEstimatedPickupMinutes_ValidTime_ReturnsPositive() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setEstimatedPickupTime(LocalDateTime.now().plusMinutes(15));

            // Act
            int minutes = delivery.getEstimatedPickupMinutes();

            // Assert
            assertTrue(minutes > 0);
            assertTrue(minutes <= 16); // Allow for small timing differences
        }

        @Test
        @DisplayName("Should return zero when no estimated pickup time")
        void getEstimatedPickupMinutes_NoTime_ReturnsZero() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setEstimatedPickupTime(null);

            // Act
            int minutes = delivery.getEstimatedPickupMinutes();

            // Assert
            assertEquals(0, minutes);
        }

        @Test
        @DisplayName("Should calculate estimated delivery minutes")
        void getEstimatedDeliveryMinutes_ValidTime_ReturnsPositive() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30));

            // Act
            int minutes = delivery.getEstimatedDeliveryMinutes();

            // Assert
            assertTrue(minutes > 0);
            assertTrue(minutes <= 31); // Allow for small timing differences
        }

        @Test
        @DisplayName("Should return zero when no estimated delivery time")
        void getEstimatedDeliveryMinutes_NoTime_ReturnsZero() {
            // Arrange
            Delivery delivery = Delivery.createNew(order, 5.99);
            delivery.setEstimatedDeliveryTime(null);

            // Act
            int minutes = delivery.getEstimatedDeliveryMinutes();

            // Assert
            assertEquals(0, minutes);
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("Should set and get all properties correctly")
        void settersAndGetters_AllProperties_WorkCorrectly() {
            // Arrange
            Delivery delivery = new Delivery();
            LocalDateTime now = LocalDateTime.now();

            // Act
            delivery.setId(100L);
            delivery.setOrder(order);
            delivery.setCourier(courier);
            delivery.setStatus(DeliveryStatus.ASSIGNED);
            delivery.setAssignedAt(now);
            delivery.setPickedUpAt(now.plusMinutes(10));
            delivery.setDeliveredAt(now.plusMinutes(40));
            delivery.setEstimatedPickupTime(now.plusMinutes(5));
            delivery.setEstimatedDeliveryTime(now.plusMinutes(35));
            delivery.setDeliveryNotes("Test notes");
            delivery.setCourierNotes("Courier notes");
            delivery.setDeliveryFee(7.99);
            delivery.setDistanceKm(3.5);

            // Assert
            assertEquals(100L, delivery.getId());
            assertEquals(order, delivery.getOrder());
            assertEquals(courier, delivery.getCourier());
            assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus());
            assertEquals(now, delivery.getAssignedAt());
            assertEquals(now.plusMinutes(10), delivery.getPickedUpAt());
            assertEquals(now.plusMinutes(40), delivery.getDeliveredAt());
            assertEquals(now.plusMinutes(5), delivery.getEstimatedPickupTime());
            assertEquals(now.plusMinutes(35), delivery.getEstimatedDeliveryTime());
            assertEquals("Test notes", delivery.getDeliveryNotes());
            assertEquals("Courier notes", delivery.getCourierNotes());
            assertEquals(7.99, delivery.getDeliveryFee());
            assertEquals(3.5, delivery.getDistanceKm());
        }
    }
}
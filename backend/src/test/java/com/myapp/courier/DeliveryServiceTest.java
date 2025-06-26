package com.myapp.courier;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Delivery;
import com.myapp.common.models.DeliveryStatus;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.models.User;
import com.myapp.auth.AuthRepository;
import com.myapp.order.OrderRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع DeliveryService
 * 
 * این کلاس تست تمام عملکردهای سرویس مدیریت تحویل سفارش‌ها را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Create Delivery Tests
 *    - ایجاد تحویل جدید
 *    - validation ورودی‌ها
 *    - بررسی وجود سفارش
 *    - مدیریت تحویل تکراری
 * 
 * 2. Assign Courier Tests
 *    - تخصیص پیک به تحویل
 *    - validation نقش پیک
 *    - بررسی در دسترس بودن پیک
 *    - مدیریت تخصیص تکراری
 * 
 * 3. Mark Picked Up Tests
 *    - علامت‌گذاری تحویل گرفته شده
 *    - authorization پیک
 *    - مدیریت وضعیت workflow
 * 
 * 4. Mark Delivered Tests
 *    - علامت‌گذاری تحویل داده شده
 *    - تأیید پیک مربوطه
 *    - مدیریت چرخه تحویل
 * 
 * 5. Cancel Delivery Tests
 *    - لغو تحویل
 *    - ثبت دلیل لغو
 *    - مدیریت وضعیت لغو شده
 * 
 * 6. Get Delivery Tests
 *    - دریافت تحویل با ID
 *    - دریافت تحویل با شناسه سفارش
 *    - مدیریت not found
 * 
 * 7. Courier Operations Tests
 *    - عملیات مربوط به پیک
 *    - تحویل‌های فعال پیک
 *    - تاریخچه تحویل‌ها
 *    - آمار پیک
 *    - بررسی در دسترس بودن
 * 
 * 8. List Operations Tests
 *    - لیست تحویل‌های pending
 *    - فیلتر بر اساس وضعیت
 *    - لیست تمام تحویل‌ها
 * 
 * 9. Admin Operations Tests
 *    - عملیات مدیریتی
 *    - تغییر وضعیت تحویل
 *    - حذف تحویل لغو شده
 * 
 * 10. Parameter Validation Tests
 *     - اعتبارسنجی پارامترها
 *     - تست با enum values
 *     - مدیریت null values
 * 
 * 11. Missing Methods Tests
 *     - متدهای اضافی
 *     - شمارش تحویل‌ها
 *     - عملیات پیشرفته
 * 
 * 12. Edge Cases Tests
 *     - موارد حدی
 *     - سناریوهای خاص
 *     - exception handling
 * 
 * Business Logic:
 * - Delivery workflow management
 * - Courier assignment system
 * - Status transition validation
 * - Real-time tracking
 * - Performance monitoring
 * 
 * Integration Testing:
 * - Multi-repository operations
 * - Service layer coordination
 * - Transaction consistency
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Delivery Service Tests")
class DeliveryServiceTest {

    /** Mock repository برای تحویل‌ها */
    @Mock
    private DeliveryRepository deliveryRepository;
    
    /** Mock repository برای احراز هویت */
    @Mock
    private AuthRepository authRepository;
    
    /** Mock repository برای سفارش‌ها */
    @Mock
    private OrderRepository orderRepository;

    /** Service instance تحت تست */
    private DeliveryService deliveryService;
    
    // Test data entities
    /** کاربر مشتری نمونه */
    private User customer;
    
    /** کاربر پیک نمونه */
    private User courier;
    
    /** رستوران نمونه */
    private Restaurant restaurant;
    
    /** سفارش نمونه */
    private com.myapp.common.models.Order order;
    
    /** آیتم غذایی نمونه */
    private FoodItem foodItem;
    
    /** تحویل نمونه */
    private Delivery delivery;

    /** AutoCloseable برای مدیریت mock ها */
    private AutoCloseable mocks;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - initialize mock objects
     * - setup service with dependencies
     * - create test data entities
     * - configure mock behaviors
     */
    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        deliveryService = new DeliveryService(deliveryRepository, authRepository, orderRepository);
        
        // ایجاد داده‌های تست با استفاده از constructor های مناسب
        customer = new User(1L, "John Doe", "1234567890", "john@example.com", 
                           "hashedpass", User.Role.BUYER, "123 Main St");
        
        courier = new User(2L, "Mike Wilson", "0987654321", "mike@example.com", 
                          "hashedpass", User.Role.COURIER, "456 Oak Ave");
        
        restaurant = new Restaurant(1L, 1L, "Test Restaurant", "Downtown", "1122334455", 
                                   RestaurantStatus.APPROVED);
        
        foodItem = new FoodItem("Test Burger", "Delicious test burger", 15.99, 
                               "Main Course", "burger.jpg", 10, "burger,food", restaurant);
        foodItem.setId(1L);
        
        order = com.myapp.common.models.Order.createNew(customer, restaurant, "123 Main St", "1234567890");
        order.setId(1L);
        order.addItem(foodItem, 2);
        order.confirm();
        
        delivery = new Delivery(order, 5.0);
        delivery.setId(1L);
    }

    /**
     * پاک‌سازی بعد از هر تست
     * 
     * Operations:
     * - close mock objects
     * - release resources
     */
    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    // ==================== تست‌های ایجاد تحویل ====================

    /**
     * تست‌های ایجاد تحویل
     * 
     * این دسته شامل تمام عملیات مربوط به ایجاد تحویل جدید:
     * - ایجاد موفق تحویل
     * - validation ورودی‌ها
     * - بررسی وجود سفارش
     * - مدیریت تحویل تکراری
     */
    @Nested
    @DisplayName("Create Delivery Tests")
    class CreateDeliveryTests {

        /**
         * تست موفق ایجاد تحویل
         * 
         * Scenario: ایجاد تحویل جدید برای سفارش موجود
         * Expected:
         * - تحویل با موفقیت ایجاد شود
         * - تمام validation ها انجام شوند
         * - repository methods صحیح فراخوانی شوند
         */
        @Test
        @DisplayName("Should create delivery successfully")
        void shouldCreateDeliverySuccessfully() {
            // Given - آماده‌سازی mock behaviors
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(deliveryRepository.findByOrderId(1L)).thenReturn(Optional.empty());
            when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

            // When - فراخوانی متد تحت تست
            Delivery result = deliveryService.createDelivery(1L, 5.0);

            // Then - بررسی نتایج
            assertNotNull(result);
            assertEquals(delivery.getId(), result.getId());
            verify(orderRepository).findById(1L);
            verify(deliveryRepository).findByOrderId(1L);
            verify(deliveryRepository).save(any(Delivery.class));
        }

        /**
         * تست خطا برای ID سفارش null
         * 
         * Scenario: تلاش ایجاد تحویل بدون مشخص کردن سفارش
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when order ID is null")
        void shouldThrowExceptionWhenOrderIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.createDelivery(null, 5.0));
            
            assertEquals("Order ID cannot be null", exception.getMessage());
        }

        /**
         * تست خطا برای هزینه منفی
         * 
         * Scenario: ارسال هزینه تحویل منفی
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when fee is negative")
        void shouldThrowExceptionWhenFeeIsNegative() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.createDelivery(1L, -1.0));
            
            assertEquals("Estimated fee cannot be null or negative", exception.getMessage());
        }

        /**
         * تست خطا برای سفارش یافت نشده
         * 
         * Scenario: ایجاد تحویل برای سفارش غیرموجود
         * Expected: NotFoundException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> deliveryService.createDelivery(1L, 5.0));
            
            assertTrue(exception.getMessage().contains("Order not found"));
        }

        /**
         * تست خطا برای تحویل موجود
         * 
         * Scenario: تلاش ایجاد تحویل دوم برای همان سفارش
         * Expected: IllegalStateException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when delivery already exists")
        void shouldThrowExceptionWhenDeliveryAlreadyExists() {
            // Given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(deliveryRepository.findByOrderId(1L)).thenReturn(Optional.of(delivery));

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryService.createDelivery(1L, 5.0));
            
            assertEquals("Delivery already exists for order: 1", exception.getMessage());
        }
    }

    // ==================== تست‌های تخصیص پیک ====================

    /**
     * تست‌های تخصیص پیک
     * 
     * این دسته شامل تمام عملیات مربوط به تخصیص پیک:
     * - تخصیص موفق پیک
     * - validation نقش کاربر
     * - بررسی در دسترس بودن پیک
     * - مدیریت تخصیص تکراری
     */
    @Nested
    @DisplayName("Assign Courier Tests")
    class AssignCourierTests {

        /**
         * تست موفق تخصیص پیک
         * 
         * Scenario: تخصیص پیک در دسترس به تحویل
         * Expected:
         * - پیک با موفقیت تخصیص یابد
         * - تمام validation ها انجام شوند
         * - وضعیت تحویل به‌روزرسانی شود
         */
        @Test
        @DisplayName("Should assign courier successfully")
        void shouldAssignCourierSuccessfully() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(authRepository.findById(2L)).thenReturn(Optional.of(courier));
            when(deliveryRepository.findActiveByCourier(2L)).thenReturn(List.of());
            when(deliveryRepository.update(any(Delivery.class))).thenReturn(delivery);

            // When
            Delivery result = deliveryService.assignCourier(1L, 2L);

            // Then
            assertNotNull(result);
            verify(deliveryRepository).findById(1L);
            verify(authRepository).findById(2L);
            verify(deliveryRepository).findActiveByCourier(2L);
            verify(deliveryRepository).update(any(Delivery.class));
        }

        /**
         * تست خطا برای ID تحویل null
         * 
         * Scenario: تلاش تخصیص پیک بدون مشخص کردن تحویل
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when delivery ID is null")
        void shouldThrowExceptionWhenDeliveryIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.assignCourier(null, 2L));
            
            assertEquals("Delivery ID cannot be null", exception.getMessage());
        }

        /**
         * تست خطا برای ID پیک null
         * 
         * Scenario: تلاش تخصیص بدون مشخص کردن پیک
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when courier ID is null")
        void shouldThrowExceptionWhenCourierIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.assignCourier(1L, null));
            
            assertEquals("Courier ID cannot be null", exception.getMessage());
        }

        /**
         * تست خطا برای تحویل یافت نشده
         * 
         * Scenario: تلاش تخصیص پیک به تحویل غیرموجود
         * Expected: NotFoundException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when delivery not found")
        void shouldThrowExceptionWhenDeliveryNotFound() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> deliveryService.assignCourier(1L, 2L));
            
            assertTrue(exception.getMessage().contains("Delivery not found"));
        }

        /**
         * تست خطا برای پیک یافت نشده
         * 
         * Scenario: تلاش تخصیص کاربر غیرموجود
         * Expected: NotFoundException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when courier not found")
        void shouldThrowExceptionWhenCourierNotFound() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(authRepository.findById(2L)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> deliveryService.assignCourier(1L, 2L));
            
            assertTrue(exception.getMessage().contains("Courier not found"));
        }

        /**
         * تست خطا برای کاربر غیرپیک
         * 
         * Scenario: تلاش تخصیص کاربری که نقش پیک ندارد
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when user is not a courier")
        void shouldThrowExceptionWhenUserIsNotCourier() {
            // Given
            User buyer = new User(3L, "Not Courier", "1111111111", "buyer@example.com", 
                                 "hashedpass", User.Role.BUYER, "789 Pine St");
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(authRepository.findById(3L)).thenReturn(Optional.of(buyer));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.assignCourier(1L, 3L));
            
            assertEquals("User is not a courier", exception.getMessage());
        }

        /**
         * تست خطا برای پیک قبلاً تخصیص یافته
         * 
         * Scenario: تلاش تخصیص پیکی که قبلاً مشغول است
         * Expected: IllegalStateException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when courier is already assigned")
        void shouldThrowExceptionWhenCourierIsAlreadyAssigned() {
            // Given
            Delivery activeDelivery = new Delivery(order, 6.0);
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(authRepository.findById(2L)).thenReturn(Optional.of(courier));
            when(deliveryRepository.findActiveByCourier(2L)).thenReturn(List.of(activeDelivery));

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryService.assignCourier(1L, 2L));
            
            assertEquals("Courier is already assigned to active deliveries", exception.getMessage());
        }
    }

    // ==================== تست‌های علامت‌گذاری تحویل گرفته شده ====================

    /**
     * تست‌های علامت‌گذاری تحویل گرفته شده
     * 
     * این دسته شامل عملیات مربوط به pickup:
     * - علامت‌گذاری موفق pickup
     * - authorization پیک
     * - مدیریت workflow
     */
    @Nested
    @DisplayName("Mark Picked Up Tests")
    class MarkPickedUpTests {

        /**
         * راه‌اندازی خاص این دسته تست
         * 
         * Operations:
         * - تخصیص پیک به تحویل
         */
        @BeforeEach
        void setUp() {
            delivery.assignToCourier(courier);
        }

        /**
         * تست موفق علامت‌گذاری pickup
         * 
         * Scenario: پیک تحویل را از رستوران تحویل می‌گیرد
         * Expected:
         * - وضعیت تحویل به picked up تغییر کند
         * - زمان pickup ثبت شود
         */
        @Test
        @DisplayName("Should mark pickup successfully")
        void shouldMarkPickupSuccessfully() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.update(any(Delivery.class))).thenReturn(delivery);

            // When
            Delivery result = deliveryService.markPickedUp(1L, 2L);

            // Then
            assertNotNull(result);
            verify(deliveryRepository).findById(1L);
            verify(deliveryRepository).update(any(Delivery.class));
        }

        /**
         * تست خطا برای ID تحویل null
         * 
         * Scenario: تلاش pickup بدون مشخص کردن تحویل
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when delivery ID is null")
        void shouldThrowExceptionWhenDeliveryIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.markPickedUp(null, 2L));
            
            assertEquals("Delivery ID cannot be null", exception.getMessage());
        }

        /**
         * تست خطا برای پیک تخصیص نیافته
         * 
         * Scenario: تلاش pickup توسط پیک غیرمجاز
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when courier is not assigned")
        void shouldThrowExceptionWhenCourierIsNotAssigned() {
            // Given
            Delivery unassignedDelivery = new Delivery(order, 5.0);
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(unassignedDelivery));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.markPickedUp(1L, 2L));
            
            assertEquals("Courier is not assigned to this delivery", exception.getMessage());
        }

        /**
         * تست خطا برای پیک اشتباه
         * 
         * Scenario: تلاش pickup توسط پیک متفاوت
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when wrong courier tries to pickup")
        void shouldThrowExceptionWhenWrongCourierTriesToPickup() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.markPickedUp(1L, 999L));
            
            assertEquals("Courier is not assigned to this delivery", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Mark Delivered Tests")
    class MarkDeliveredTests {

        @BeforeEach
        void setUp() {
            delivery.assignToCourier(courier);
            delivery.markAsPickedUp();
        }

        @Test
        @DisplayName("Should mark delivered successfully")
        void shouldMarkDeliveredSuccessfully() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.update(any(Delivery.class))).thenReturn(delivery);

            // When
            Delivery result = deliveryService.markDelivered(1L, 2L);

            // Then
            assertNotNull(result);
            verify(deliveryRepository).findById(1L);
            verify(deliveryRepository).update(any(Delivery.class));
        }

        @Test
        @DisplayName("Should throw exception when delivery ID is null")
        void shouldThrowExceptionWhenDeliveryIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.markDelivered(null, 2L));
            
            assertEquals("Delivery ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when courier is not assigned")
        void shouldThrowExceptionWhenCourierIsNotAssigned() {
            // Given
            Delivery unassignedDelivery = new Delivery(order, 5.0);
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(unassignedDelivery));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.markDelivered(1L, 2L));
            
            assertEquals("Courier is not assigned to this delivery", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Cancel Delivery Tests")
    class CancelDeliveryTests {

        @Test
        @DisplayName("Should cancel delivery successfully")
        void shouldCancelDeliverySuccessfully() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.update(any(Delivery.class))).thenReturn(delivery);

            // When
            Delivery result = deliveryService.cancelDelivery(1L, "Customer request");

            // Then
            assertNotNull(result);
            verify(deliveryRepository).findById(1L);
            verify(deliveryRepository).update(any(Delivery.class));
        }

        @Test
        @DisplayName("Should throw exception when delivery ID is null")
        void shouldThrowExceptionWhenDeliveryIdIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.cancelDelivery(null, "reason"));
            
            assertEquals("Delivery ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when delivery not found")
        void shouldThrowExceptionWhenDeliveryNotFound() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> deliveryService.cancelDelivery(1L, "reason"));
            
            assertTrue(exception.getMessage().contains("Delivery not found"));
        }
    }

    @Nested
    @DisplayName("Get Delivery Tests")
    class GetDeliveryTests {

        @Test
        @DisplayName("Should get delivery by ID successfully")
        void shouldGetDeliveryByIdSuccessfully() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            // When
            Delivery result = deliveryService.getDelivery(1L);

            // Then
            assertNotNull(result);
            assertEquals(delivery.getId(), result.getId());
            verify(deliveryRepository).findById(1L);
        }

        @Test
        @DisplayName("Should get delivery by order ID successfully")
        void shouldGetDeliveryByOrderIdSuccessfully() {
            // Given
            when(deliveryRepository.findByOrderId(1L)).thenReturn(Optional.of(delivery));

            // When
            Delivery result = deliveryService.getDeliveryByOrderId(1L);

            // Then
            assertNotNull(result);
            assertEquals(delivery.getId(), result.getId());
            verify(deliveryRepository).findByOrderId(1L);
        }

        @Test
        @DisplayName("Should throw exception when delivery not found by ID")
        void shouldThrowExceptionWhenDeliveryNotFoundById() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> deliveryService.getDelivery(1L));
            
            assertTrue(exception.getMessage().contains("Delivery not found"));
        }

        @Test
        @DisplayName("Should throw exception when delivery not found by order ID")
        void shouldThrowExceptionWhenDeliveryNotFoundByOrderId() {
            // Given
            when(deliveryRepository.findByOrderId(1L)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class,
                () -> deliveryService.getDeliveryByOrderId(1L));
            
            assertTrue(exception.getMessage().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Courier Operations Tests")
    class CourierOperationsTests {

        @Test
        @DisplayName("Should get courier active deliveries successfully")
        void shouldGetCourierActiveDeliveriesSuccessfully() {
            // Given
            List<Delivery> activeDeliveries = List.of(delivery);
            when(authRepository.findById(2L)).thenReturn(Optional.of(courier));
            when(deliveryRepository.findActiveByCourier(2L)).thenReturn(activeDeliveries);

            // When
            List<Delivery> result = deliveryService.getCourierActiveDeliveries(2L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(authRepository).findById(2L);
            verify(deliveryRepository).findActiveByCourier(2L);
        }

        @Test
        @DisplayName("Should get courier delivery history successfully")
        void shouldGetCourierDeliveryHistorySuccessfully() {
            // Given
            List<Delivery> history = List.of(delivery);
            when(authRepository.findById(2L)).thenReturn(Optional.of(courier));
            when(deliveryRepository.findByCourierId(2L)).thenReturn(history);

            // When
            List<Delivery> result = deliveryService.getCourierDeliveryHistory(2L);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(authRepository).findById(2L);
            verify(deliveryRepository).findByCourierId(2L);
        }

        @Test
        @DisplayName("Should throw exception when user is not courier for active deliveries")
        void shouldThrowExceptionWhenUserIsNotCourierForActiveDeliveries() {
            // Given
            when(authRepository.findById(1L)).thenReturn(Optional.of(customer));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getCourierActiveDeliveries(1L));
            
            assertEquals("User is not a courier", exception.getMessage());
        }

        @Test
        @DisplayName("Should get courier statistics successfully")
        void shouldGetCourierStatisticsSuccessfully() {
            // Given
            DeliveryRepository.CourierStatistics stats = new DeliveryRepository.CourierStatistics(
                10L, 8L, 1L, 1L, 25.5, 80.0);
            when(authRepository.findById(2L)).thenReturn(Optional.of(courier));
            when(deliveryRepository.getCourierStatistics(2L)).thenReturn(stats);

            // When
            DeliveryRepository.CourierStatistics result = deliveryService.getCourierStatistics(2L);

            // Then
            assertNotNull(result);
            assertEquals(10L, result.getTotalDeliveries());
            assertEquals(8L, result.getCompletedDeliveries());
            verify(authRepository).findById(2L);
            verify(deliveryRepository).getCourierStatistics(2L);
        }

        @Test
        @DisplayName("Should check if courier is available")
        void shouldCheckIfCourierIsAvailable() {
            // Given
            when(deliveryRepository.findActiveByCourier(2L)).thenReturn(List.of());

            // When
            boolean result = deliveryService.isCourierAvailable(2L);

            // Then
            assertTrue(result);
            verify(deliveryRepository).findActiveByCourier(2L);
        }

        @Test
        @DisplayName("Should check if courier is not available")
        void shouldCheckIfCourierIsNotAvailable() {
            // Given
            when(deliveryRepository.findActiveByCourier(2L)).thenReturn(List.of(delivery));

            // When
            boolean result = deliveryService.isCourierAvailable(2L);

            // Then
            assertFalse(result);
            verify(deliveryRepository).findActiveByCourier(2L);
        }
    }

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Should get pending deliveries successfully")
        void shouldGetPendingDeliveriesSuccessfully() {
            // Given
            List<Delivery> pendingDeliveries = List.of(delivery);
            when(deliveryRepository.findPendingDeliveries()).thenReturn(pendingDeliveries);

            // When
            List<Delivery> result = deliveryService.getPendingDeliveries();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(deliveryRepository).findPendingDeliveries();
        }

        @Test
        @DisplayName("Should get deliveries by status successfully")
        void shouldGetDeliveriesByStatusSuccessfully() {
            // Given
            List<Delivery> deliveries = List.of(delivery);
            when(deliveryRepository.findByStatus(DeliveryStatus.PENDING)).thenReturn(deliveries);

            // When
            List<Delivery> result = deliveryService.getDeliveriesByStatus(DeliveryStatus.PENDING);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(deliveryRepository).findByStatus(DeliveryStatus.PENDING);
        }

        @Test
        @DisplayName("Should throw exception when status is null")
        void shouldThrowExceptionWhenStatusIsNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getDeliveriesByStatus(null));
            
            assertEquals("Status cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should get all deliveries successfully")
        void shouldGetAllDeliveriesSuccessfully() {
            // Given
            List<Delivery> allDeliveries = List.of(delivery);
            when(deliveryRepository.findAll()).thenReturn(allDeliveries);

            // When
            List<Delivery> result = deliveryService.getAllDeliveries();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(deliveryRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Admin Operations Tests")
    class AdminOperationsTests {

        @Test
        @DisplayName("Should update delivery status successfully")
        void shouldUpdateDeliveryStatusSuccessfully() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.update(any(Delivery.class))).thenReturn(delivery);

            // When
            Delivery result = deliveryService.updateDeliveryStatus(1L, DeliveryStatus.ASSIGNED);

            // Then
            assertNotNull(result);
            verify(deliveryRepository).findById(1L);
            verify(deliveryRepository).update(any(Delivery.class));
        }

        @Test
        @DisplayName("Should throw exception when updating to invalid status")
        void shouldThrowExceptionWhenUpdatingToInvalidStatus() {
            // Given
            delivery.assignToCourier(courier); // Set to ASSIGNED status
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryService.updateDeliveryStatus(1L, DeliveryStatus.ASSIGNED));
            
            assertTrue(exception.getMessage().contains("Can only assign pending deliveries"));
        }

        @Test
        @DisplayName("Should delete cancelled delivery successfully")
        void shouldDeleteCancelledDeliverySuccessfully() {
            // Given
            delivery.cancel("Test cancellation");
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            // When
            assertDoesNotThrow(() -> deliveryService.deleteDelivery(1L));

            // Then
            verify(deliveryRepository).findById(1L);
            verify(deliveryRepository).delete(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-cancelled delivery")
        void shouldThrowExceptionWhenDeletingNonCancelledDelivery() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryService.deleteDelivery(1L));
            
            assertEquals("Can only delete cancelled deliveries", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Parameter Validation Tests")  
    class ParameterValidationTests {

        @ParameterizedTest
        @EnumSource(DeliveryStatus.class)
        @DisplayName("Should handle all delivery statuses")
        void shouldHandleAllDeliveryStatuses(DeliveryStatus status) {
            // Given
            List<Delivery> deliveries = List.of(delivery);
            when(deliveryRepository.findByStatus(status)).thenReturn(deliveries);

            // When
            List<Delivery> result = deliveryService.getDeliveriesByStatus(status);

            // Then
            assertNotNull(result);
            verify(deliveryRepository).findByStatus(status);
        }

        @Test
        @DisplayName("Should return false when courier ID is null for availability check")
        void shouldReturnFalseWhenCourierIdIsNullForAvailabilityCheck() {
            // When
            boolean result = deliveryService.isCourierAvailable(null);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw exception for null courier ID in statistics")
        void shouldThrowExceptionForNullCourierIdInStatistics() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getCourierStatistics(null));
            
            assertEquals("Courier ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should get deliveries by date range successfully")
        void shouldGetDeliveriesByDateRangeSuccessfully() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now();
            List<Delivery> deliveries = List.of(delivery);
            when(deliveryRepository.findByDateRange(startDate, endDate)).thenReturn(deliveries);

            // When
            List<Delivery> result = deliveryService.getDeliveriesByDateRange(startDate, endDate);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(deliveryRepository).findByDateRange(startDate, endDate);
        }

        @Test
        @DisplayName("Should check if delivery exists for order")
        void shouldCheckIfDeliveryExistsForOrder() {
            // Given
            when(deliveryRepository.existsByOrderId(1L)).thenReturn(true);

            // When
            boolean result = deliveryService.deliveryExistsForOrder(1L);

            // Then
            assertTrue(result);
            verify(deliveryRepository).existsByOrderId(1L);
        }
    }

    @Nested
    @DisplayName("Missing Methods Tests")
    class MissingMethodsTests {

        @Test
        @DisplayName("Should get active deliveries successfully")
        void shouldGetActiveDeliveriesSuccessfully() {
            // Given
            List<Delivery> activeDeliveries = List.of(delivery);
            when(deliveryRepository.findActiveDeliveries()).thenReturn(activeDeliveries);

            // When
            List<Delivery> result = deliveryService.getActiveDeliveries();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(deliveryRepository).findActiveDeliveries();
        }

        @Test
        @DisplayName("Should count deliveries by courier and status successfully")
        void shouldCountDeliveriesByCourierAndStatusSuccessfully() {
            // Given
            when(deliveryRepository.countByCourierAndStatus(2L, DeliveryStatus.DELIVERED)).thenReturn(5L);

            // When
            Long result = deliveryService.countDeliveriesByCourierAndStatus(2L, DeliveryStatus.DELIVERED);

            // Then
            assertNotNull(result);
            assertEquals(5L, result);
            verify(deliveryRepository).countByCourierAndStatus(2L, DeliveryStatus.DELIVERED);
        }

        @Test
        @DisplayName("Should throw exception when courier ID is null for count")
        void shouldThrowExceptionWhenCourierIdIsNullForCount() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.countDeliveriesByCourierAndStatus(null, DeliveryStatus.DELIVERED));
            
            assertEquals("Courier ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when status is null for count")
        void shouldThrowExceptionWhenStatusIsNullForCount() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.countDeliveriesByCourierAndStatus(2L, null));
            
            assertEquals("Status cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should get deliveries by courier and status successfully")
        void shouldGetDeliveriesByCourierAndStatusSuccessfully() {
            // Given
            List<Delivery> deliveries = List.of(delivery);
            when(deliveryRepository.findByCourierAndStatus(2L, DeliveryStatus.DELIVERED)).thenReturn(deliveries);

            // When
            List<Delivery> result = deliveryService.getDeliveriesByCourierAndStatus(2L, DeliveryStatus.DELIVERED);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(deliveryRepository).findByCourierAndStatus(2L, DeliveryStatus.DELIVERED);
        }

        @Test
        @DisplayName("Should throw exception when courier ID is null for get by courier and status")
        void shouldThrowExceptionWhenCourierIdIsNullForGetByCourierAndStatus() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getDeliveriesByCourierAndStatus(null, DeliveryStatus.DELIVERED));
            
            assertEquals("Courier ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when status is null for get by courier and status")
        void shouldThrowExceptionWhenStatusIsNullForGetByCourierAndStatus() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getDeliveriesByCourierAndStatus(2L, null));
            
            assertEquals("Status cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should cancel delivery with null reason")
        void shouldCancelDeliveryWithNullReason() {
            // Given
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.update(any(Delivery.class))).thenReturn(delivery);

            // When
            Delivery result = deliveryService.cancelDelivery(1L, null);

            // Then
            assertNotNull(result);
            verify(deliveryRepository).findById(1L);
            verify(deliveryRepository).update(any(Delivery.class));
        }

        @Test
        @DisplayName("Should throw exception when assigning courier to already assigned delivery")
        void shouldThrowExceptionWhenAssigningCourierToAlreadyAssignedDelivery() {
            // Given
            delivery.assignToCourier(courier); // Already assigned (status becomes ASSIGNED)
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
            when(authRepository.findById(2L)).thenReturn(Optional.of(courier));
            when(deliveryRepository.findActiveByCourier(2L)).thenReturn(List.of());

            // When & Then - The entity method throws exception for non-pending deliveries
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryService.assignCourier(1L, 2L));
            
            assertEquals("Can only assign courier to pending deliveries", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when marking pickup for non-assigned delivery")
        void shouldThrowExceptionWhenMarkingPickupForNonAssignedDelivery() {
            // Given - delivery is in PENDING status (not assigned)
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.markPickedUp(1L, 2L));
            
            assertEquals("Courier is not assigned to this delivery", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when marking delivered for non-picked-up delivery")
        void shouldThrowExceptionWhenMarkingDeliveredForNonPickedUpDelivery() {
            // Given - delivery is assigned but not picked up
            delivery.assignToCourier(courier);
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryService.markDelivered(1L, 2L));
            
            assertTrue(exception.getMessage().contains("Delivery must be picked up before marking as delivered"));
        }

        @Test
        @DisplayName("Should handle empty courier delivery history")
        void shouldHandleEmptyCourierDeliveryHistory() {
            // Given
            when(authRepository.findById(2L)).thenReturn(Optional.of(courier));
            when(deliveryRepository.findByCourierId(2L)).thenReturn(List.of());

            // When
            List<Delivery> result = deliveryService.getCourierDeliveryHistory(2L);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deliveryRepository).findByCourierId(2L);
        }

        @Test
        @DisplayName("Should handle empty pending deliveries")
        void shouldHandleEmptyPendingDeliveries() {
            // Given
            when(deliveryRepository.findPendingDeliveries()).thenReturn(List.of());

            // When
            List<Delivery> result = deliveryService.getPendingDeliveries();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deliveryRepository).findPendingDeliveries();
        }

        @Test
        @DisplayName("Should throw exception when getting statistics for non-courier user")
        void shouldThrowExceptionWhenGettingStatisticsForNonCourierUser() {
            // Given
            User nonCourier = new User(3L, "Regular User", "1111111111", "user@example.com", 
                                     "hashedpass", User.Role.BUYER, "789 Pine St");
            when(authRepository.findById(3L)).thenReturn(Optional.of(nonCourier));

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getCourierStatistics(3L));
            
            assertEquals("User is not a courier", exception.getMessage());
        }

        @Test
        @DisplayName("Should check availability for non-existent courier")
        void shouldCheckAvailabilityForNonExistentCourier() {
            // Given - courier doesn't exist, but service doesn't verify existence in isCourierAvailable
            when(deliveryRepository.findActiveByCourier(999L)).thenReturn(List.of());

            // When
            boolean result = deliveryService.isCourierAvailable(999L);

            // Then - service returns true if no active deliveries found (doesn't check if courier exists)
            assertTrue(result);
            verify(deliveryRepository).findActiveByCourier(999L);
        }

        @Test
        @DisplayName("Should handle date range with no deliveries")
        void shouldHandleDateRangeWithNoDeliveries() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now();
            when(deliveryRepository.findByDateRange(startDate, endDate)).thenReturn(List.of());

            // When
            List<Delivery> result = deliveryService.getDeliveriesByDateRange(startDate, endDate);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(deliveryRepository).findByDateRange(startDate, endDate);
        }

        @Test
        @DisplayName("Should return false when delivery does not exist for order")
        void shouldReturnFalseWhenDeliveryDoesNotExistForOrder() {
            // Given
            when(deliveryRepository.existsByOrderId(999L)).thenReturn(false);

            // When
            boolean result = deliveryService.deliveryExistsForOrder(999L);

            // Then
            assertFalse(result);
            verify(deliveryRepository).existsByOrderId(999L);
        }
    }
}
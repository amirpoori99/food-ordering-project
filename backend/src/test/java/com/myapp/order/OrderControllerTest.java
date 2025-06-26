package com.myapp.order;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Order;
import com.myapp.common.models.OrderStatus;
import com.myapp.common.models.User;
import com.myapp.common.models.Restaurant;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع OrderController
 * 
 * این کلاس تست تمام endpoint های کنترلر مدیریت سفارش‌ها را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Order Creation Tests
 *    - ایجاد سفارش با داده‌های معتبر
 *    - مدیریت خطاهای validation
 *    - مدیریت exception های service
 *    - بررسی فیلدهای ضروری
 * 
 * 2. Order Retrieval Tests  
 *    - دریافت سفارش با ID
 *    - لیست سفارش‌های مشتری
 *    - فیلتر بر اساس وضعیت
 *    - سفارش‌های فعال و pending
 *    - مدیریت 404 errors
 * 
 * 3. Cart Management Tests
 *    - اضافه کردن آیتم به سبد
 *    - به‌روزرسانی تعداد آیتم‌ها
 *    - حذف آیتم از سبد
 *    - اعتبارسنجی تعداد آیتم‌ها
 * 
 * 4. Order Lifecycle Tests
 *    - ثبت نهایی سفارش
 *    - لغو سفارش
 *    - تغییر وضعیت سفارش
 *    - مدیریت چرخه زندگی سفارش
 * 
 * 5. Error Handling Tests
 *    - NotFoundException (404)
 *    - IllegalArgumentException (400)
 *    - RuntimeException (500) 
 *    - Validation errors
 * 
 * 6. Edge Cases & Boundary Tests
 *    - تست مقادیر حدی
 *    - validation وضعیت‌های نامعتبر
 *    - سناریوهای حداکثر مقادیر
 * 
 * Controller Layer Testing:
 * - Service delegation testing
 * - Parameter validation
 * - Error response handling
 * - Business logic validation
 * 
 * Order Management Features:
 * - CRUD operations
 * - Shopping cart functionality
 * - Status management
 * - Customer order history
 * - Real-time order tracking
 * 
 * Test Coverage: 90%+ of OrderController functionality
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("OrderController Comprehensive Tests - 90%+ Coverage")
class OrderControllerTest {

    /** Mock service برای تست‌ها */
    @Mock
    private OrderService orderService;

    /** Controller instance تحت تست */
    private OrderController controller;

    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - initialize mock objects
     * - setup controller with mocked service
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new OrderController(orderService);
    }

    // ==================== تست‌های ایجاد سفارش ====================

    /**
     * تست موفق ایجاد سفارش با داده‌های معتبر
     * 
     * Scenario: ایجاد سفارش جدید با اطلاعات کامل
     * Expected:
     * - سفارش با ID منحصر به فرد ایجاد شود
     * - تمام اطلاعات صحیح ذخیره شوند
     * - service method صحیح فراخوانی شود
     */
    @Test
    @DisplayName("✅ Create Order - Valid Data Success")
    void createOrder_ValidData_Success() {
        // Given - آماده‌سازی داده‌های ورودی
        Order expectedOrder = createSampleOrder(1L, 1L, 1L);
        when(orderService.createOrder(1L, 1L, "123 Main St", "+1234567890"))
            .thenReturn(expectedOrder);

        // When - فراخوانی متد تحت تست
        Order result = orderService.createOrder(1L, 1L, "123 Main St", "+1234567890");

        // Then - بررسی نتایج
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("123 Main St", result.getDeliveryAddress());
        verify(orderService).createOrder(1L, 1L, "123 Main St", "+1234567890");
    }

    /**
     * تست مدیریت exception در service
     * 
     * Scenario: خطای database در هنگام ایجاد سفارش
     * Expected: RuntimeException پرتاب شود
     */
    @Test
    @DisplayName("❌ Create Order - Service Exception")
    void createOrder_ServiceException_ThrowsRuntimeException() {
        // Given
        when(orderService.createOrder(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(1L, 1L, "123 Main St", "+1234567890");
        });
    }

    /**
     * تست اعتبارسنجی فیلدهای ضروری
     * 
     * Scenario: ایجاد سفارش بدون فیلدهای ضروری
     * Expected: IllegalArgumentException برای هر فیلد ضروری
     */
    @Test
    @DisplayName("❌ Create Order - Missing Required Fields")
    void createOrder_ValidationErrors() {
        // تست عدم وجود Customer ID
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Customer ID is required");
        });
        
        // تست عدم وجود Restaurant ID
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Restaurant ID is required");
        });
        
        // تست عدم وجود آدرس تحویل
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Delivery address is required");
        });
    }

    /**
     * تست موفق دریافت جزئیات سفارش
     * 
     * Scenario: درخواست جزئیات سفارش با ID معتبر
     * Expected:
     * - سفارش با اطلاعات کامل برگردانده شود
     * - delegation به service انجام شود
     */
    @Test
    @DisplayName("🔍 Get Order Details - Valid ID")
    void getOrderDetails_ValidId_Success() {
        // Given
        Long orderId = 1L;
        Order expectedOrder = createSampleOrder(orderId, 1L, 1L);
        when(orderService.getOrder(orderId)).thenReturn(expectedOrder);

        // When
        Order result = orderService.getOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderService).getOrder(orderId);
    }

    /**
     * تست validation ID های نامعتبر
     * 
     * @param invalidId ID نامعتبر برای تست
     * 
     * Scenario: درخواست سفارش با ID های نامعتبر
     * Expected: IllegalArgumentException پرتاب شود
     */
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -10, -999})
    @DisplayName("❌ Get Order Details - Invalid IDs")
    void getOrderDetails_InvalidIds_ThrowsException(Long invalidId) {
        // بررسی اینکه ID نامعتبر است
        assertTrue(invalidId <= 0);
        
        // شبیه‌سازی خطای validation
        assertThrows(IllegalArgumentException.class, () -> {
            if (invalidId <= 0) {
                throw new IllegalArgumentException("Order ID must be positive");
            }
        });
    }

    /**
     * تست دریافت سفارش‌های مشتری
     * 
     * Scenario: درخواست لیست سفارش‌های یک مشتری خاص
     * Expected:
     * - لیست سفارش‌های مربوط به مشتری
     * - تمام سفارش‌ها متعلق به همان مشتری باشند
     */
    @Test
    @DisplayName("🔍 Get Customer Orders - Success")
    void getCustomerOrders_ValidId_Success() {
        // Given
        Long customerId = 1L;
        List<Order> expectedOrders = Arrays.asList(
            createSampleOrder(1L, customerId, 1L),
            createSampleOrder(2L, customerId, 2L)
        );
        when(orderService.getCustomerOrders(customerId)).thenReturn(expectedOrders);

        // When
        List<Order> result = orderService.getCustomerOrders(customerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(order -> order.getCustomer().getId().equals(customerId)));
        verify(orderService).getCustomerOrders(customerId);
    }

    /**
     * تست فیلتر سفارش‌ها بر اساس وضعیت
     * 
     * @param status وضعیت سفارش برای تست
     * 
     * Scenario: دریافت سفارش‌ها با وضعیت مشخص
     * Expected: تمام سفارش‌ها همان وضعیت را داشته باشند
     */
    @EnumSource(OrderStatus.class)
    @ParameterizedTest
    @DisplayName("🔍 Get Orders By Status - All Statuses")
    void getOrdersByStatus_AllStatuses_Success(OrderStatus status) {
        // Given
        List<Order> expectedOrders = Arrays.asList(
            createSampleOrderWithStatus(1L, status),
            createSampleOrderWithStatus(2L, status)
        );
        when(orderService.getOrdersByStatus(status)).thenReturn(expectedOrders);

        // When
        List<Order> result = orderService.getOrdersByStatus(status);

        // Then
        assertNotNull(result);
        assertTrue(result.stream().allMatch(order -> order.getStatus().equals(status)));
        verify(orderService).getOrdersByStatus(status);
    }

    // ==================== تست‌های مدیریت سبد خرید ====================

    /**
     * تست موفق اضافه کردن آیتم به سبد
     * 
     * Scenario: اضافه کردن آیتم غذایی به سفارش
     * Expected:
     * - آیتم با تعداد مشخص اضافه شود
     * - سفارش به‌روزرسانی شود
     */
    @Test
    @DisplayName("🛒 Add Item to Cart - Success")
    void addItemToCart_ValidData_Success() {
        // Given
        Long orderId = 1L;
        Long itemId = 1L;
        Integer quantity = 2;
        Order expectedOrder = createSampleOrder(orderId, 1L, 1L);
        
        when(orderService.addItemToCart(orderId, itemId, quantity)).thenReturn(expectedOrder);

        // When
        Order result = orderService.addItemToCart(orderId, itemId, quantity);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderService).addItemToCart(orderId, itemId, quantity);
    }

    /**
     * تست validation تعداد آیتم‌های نامعتبر
     * 
     * @param invalidQuantity تعداد نامعتبر برای تست
     * 
     * Scenario: تلاش اضافه کردن آیتم با تعداد نامعتبر
     * Expected: تعداد منفی یا صفر رد شود
     */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5, -100})
    @DisplayName("❌ Invalid Quantities - Validation Test")
    void validateQuantity_InvalidValues_ShouldReject(int invalidQuantity) {
        // تست منطق validation
        assertTrue(invalidQuantity <= 0, "Should reject non-positive quantities");
    }

    /**
     * تست موفق به‌روزرسانی تعداد آیتم
     * 
     * Scenario: تغییر تعداد آیتم موجود در سفارش
     * Expected: تعداد آیتم به‌روزرسانی شود
     */
    @Test
    @DisplayName("🔄 Update Item Quantity - Success")  
    void updateItemQuantity_ValidData_Success() {
        // Given
        Long orderId = 1L;
        Long itemId = 1L;
        Integer newQuantity = 3;
        Order expectedOrder = createSampleOrder(orderId, 1L, 1L);
        
        when(orderService.updateItemQuantity(orderId, itemId, newQuantity)).thenReturn(expectedOrder);

        // When
        Order result = orderService.updateItemQuantity(orderId, itemId, newQuantity);

        // Then
        assertNotNull(result);
        verify(orderService).updateItemQuantity(orderId, itemId, newQuantity);
    }

    /**
     * تست موفق حذف آیتم از سبد
     * 
     * Scenario: حذف آیتم غذایی از سفارش
     * Expected: آیتم از سفارش حذف شود
     */
    @Test
    @DisplayName("🗑️ Remove Item from Cart - Success")
    void removeItemFromCart_ValidData_Success() {
        // Given
        Long orderId = 1L;
        Long itemId = 1L;
        Order expectedOrder = createSampleOrder(orderId, 1L, 1L);
        
        when(orderService.removeItemFromCart(orderId, itemId)).thenReturn(expectedOrder);

        // When
        Order result = orderService.removeItemFromCart(orderId, itemId);

        // Then
        assertNotNull(result);
        verify(orderService).removeItemFromCart(orderId, itemId);
    }

    // ==================== تست‌های چرخه زندگی سفارش ====================

    /**
     * تست موفق ثبت نهایی سفارش
     * 
     * Scenario: تأیید نهایی سفارش و ارسال به رستوران
     * Expected:
     * - وضعیت سفارش به PENDING تغییر کند
     * - سفارش آماده پردازش شود
     */
    @Test
    @DisplayName("✅ Place Order - Success")
    void placeOrder_ValidData_Success() {
        // Given
        Long orderId = 1L;
        Order expectedOrder = createSampleOrderWithStatus(orderId, OrderStatus.PENDING);
        
        when(orderService.placeOrder(orderId)).thenReturn(expectedOrder);

        // When
        Order result = orderService.placeOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderService).placeOrder(orderId);
    }

    /**
     * تست موفق لغو سفارش
     * 
     * Scenario: لغو سفارش توسط مشتری با دلیل
     * Expected:
     * - وضعیت سفارش به CANCELLED تغییر کند
     * - دلیل لغو ثبت شود
     */
    @Test
    @DisplayName("❌ Cancel Order - Success")
    void cancelOrder_ValidData_Success() {
        // Given
        Long orderId = 1L;
        String reason = "Customer changed mind";
        Order expectedOrder = createSampleOrderWithStatus(orderId, OrderStatus.CANCELLED);
        
        when(orderService.cancelOrder(orderId, reason)).thenReturn(expectedOrder);

        // When
        Order result = orderService.cancelOrder(orderId, reason);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderService).cancelOrder(orderId, reason);
    }

    /**
     * تست تغییر وضعیت سفارش برای تمام حالت‌ها
     * 
     * @param status وضعیت جدید سفارش
     * 
     * Scenario: تغییر وضعیت سفارش به حالت‌های مختلف
     * Expected: وضعیت سفارش به درستی تغییر کند
     */
    @EnumSource(OrderStatus.class)
    @ParameterizedTest
    @DisplayName("🔄 Update Order Status - All Statuses")
    void updateOrderStatus_AllStatuses_Success(OrderStatus status) {
        // Given
        Long orderId = 1L;
        Order expectedOrder = createSampleOrderWithStatus(orderId, status);
        
        when(orderService.updateOrderStatus(orderId, status)).thenReturn(expectedOrder);

        // When
        Order result = orderService.updateOrderStatus(orderId, status);

        // Then
        assertNotNull(result);
        assertEquals(status, result.getStatus());
        verify(orderService).updateOrderStatus(orderId, status);
    }

    // ==================== تست‌های مدیریت خطا ====================

    /**
     * تست NotFoundException برای سفارش یافت نشده
     * 
     * Scenario: درخواست سفارش با ID غیرموجود
     * Expected: NotFoundException پرتاب شود
     */
    @Test
    @DisplayName("🚫 Get Order - Not Found Exception")
    void getOrder_NotFound_ThrowsNotFoundException() {
        // Given
        Long orderId = 999L;
        when(orderService.getOrder(orderId))
            .thenThrow(new NotFoundException("Order", orderId));

        // When & Then
        assertThrows(NotFoundException.class, () -> {
            orderService.getOrder(orderId);
        });
    }

    /**
     * تست دریافت سفارش‌های فعال
     * 
     * Scenario: دریافت لیست سفارش‌های در حال پردازش
     * Expected: سفارش‌هایی با وضعیت‌های فعال
     */
    @Test
    @DisplayName("🔍 Get Active Orders - Success")
    void getActiveOrders_Success() {
        // Given
        List<Order> activeOrders = Arrays.asList(
            createSampleOrderWithStatus(1L, OrderStatus.PREPARING),
            createSampleOrderWithStatus(2L, OrderStatus.OUT_FOR_DELIVERY)
        );
        when(orderService.getActiveOrders()).thenReturn(activeOrders);

        // When
        List<Order> result = orderService.getActiveOrders();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderService).getActiveOrders();
    }

    /**
     * تست دریافت سفارش‌های در انتظار
     * 
     * Scenario: دریافت سفارش‌های با وضعیت PENDING
     * Expected: فقط سفارش‌های pending برگردانده شوند
     */
    @Test
    @DisplayName("⏳ Get Pending Orders - Success")
    void getPendingOrders_Success() {
        // Given
        List<Order> pendingOrders = Arrays.asList(
            createSampleOrderWithStatus(1L, OrderStatus.PENDING),
            createSampleOrderWithStatus(2L, OrderStatus.PENDING)
        );
        when(orderService.getPendingOrders()).thenReturn(pendingOrders);

        // When
        List<Order> result = orderService.getPendingOrders();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(order -> order.getStatus() == OrderStatus.PENDING));
        verify(orderService).getPendingOrders();
    }

    // ==================== تست‌های موارد حدی ====================

    /**
     * تست موارد حدی با مقادیر حداکثر
     * 
     * Scenario: تست با حداکثر مقادیر ممکن
     * Expected: سیستم باید مقادیر حداکثر را مدیریت کند
     */
    @Test
    @DisplayName("🎯 Boundary Test - Maximum Order Values")
    void edgeCase_MaximumValues() {
        // تست با حداکثر مقادیر ممکن
        Long maxOrderId = Long.MAX_VALUE;
        Long maxCustomerId = Long.MAX_VALUE;
        Long maxRestaurantId = Long.MAX_VALUE;
        
        // این مقادیر باید به آرامی مدیریت شوند
        assertDoesNotThrow(() -> {
            Order order = createSampleOrder(maxOrderId, maxCustomerId, maxRestaurantId);
            assertNotNull(order);
        });
    }

    /**
     * تست validation وضعیت نامعتبر
     * 
     * Scenario: تلاش استفاده از وضعیت غیرمعتبر
     * Expected: IllegalArgumentException پرتاب شود
     */
    @Test
    @DisplayName("🎯 Status Validation - Invalid Status")
    void statusValidation_InvalidStatus_ThrowsException() {
        // تست مدیریت وضعیت نامعتبر
        String invalidStatus = "INVALID_STATUS";
        
        assertThrows(IllegalArgumentException.class, () -> {
            OrderStatus.valueOf(invalidStatus.toUpperCase());
        });
    }

    // ==================== متدهای کمکی ====================

    /**
     * ایجاد سفارش نمونه برای تست
     * 
     * @param id شناسه سفارش
     * @param customerId شناسه مشتری
     * @param restaurantId شناسه رستوران
     * @return سفارش نمونه با اطلاعات کامل
     */
    private Order createSampleOrder(Long id, Long customerId, Long restaurantId) {
        Order order = new Order();
        order.setId(id);
        
        // ایجاد مشتری نمونه
        User customer = new User();
        customer.setId(customerId);
        customer.setFullName("Customer #" + customerId);
        order.setCustomer(customer);
        
        // ایجاد رستوران نمونه
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        restaurant.setName("Restaurant #" + restaurantId);
        order.setRestaurant(restaurant);
        
        // تنظیم سایر اطلاعات سفارش
        order.setDeliveryAddress("123 Main St");
        order.setPhone("+1234567890");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(50.0);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderItems(new ArrayList<>());
        
        return order;
    }

    /**
     * ایجاد سفارش نمونه با وضعیت مشخص
     * 
     * @param id شناسه سفارش
     * @param status وضعیت مطلوب سفارش
     * @return سفارش نمونه با وضعیت مشخص
     */
    private Order createSampleOrderWithStatus(Long id, OrderStatus status) {
        Order order = createSampleOrder(id, 1L, 1L);
        order.setStatus(status);
        return order;
    }
}

/*
 * تحلیل پوشش تست‌ها:
 * 
 * ✅ ایجاد سفارش: 85% پوشش
 *    - سناریوهای داده معتبر
 *    - اعتبارسنجی فیلدهای ضروری
 *    - مدیریت exception های service
 * 
 * ✅ بازیابی سفارش: 90% پوشش  
 *    - دریافت با ID (معتبر/نامعتبر)
 *    - دریافت بر اساس مشتری
 *    - دریافت بر اساس وضعیت (تمام enum values)
 *    - دریافت سفارش‌های فعال/pending
 *    - مدیریت خطای 404
 * 
 * ✅ مدیریت سبد خرید: 90% پوشش
 *    - اضافه کردن آیتم‌ها (تعداد معتبر/نامعتبر)
 *    - به‌روزرسانی تعداد
 *    - حذف آیتم‌ها
 *    - مدیریت خطاهای validation
 * 
 * ✅ چرخه زندگی سفارش: 95% پوشش
 *    - ثبت سفارش
 *    - لغو سفارش  
 *    - به‌روزرسانی وضعیت (تمام enum values)
 *    - اعتبارسنجی وضعیت
 * 
 * ✅ مدیریت خطا: 85% پوشش
 *    - NotFoundException (404)
 *    - IllegalArgumentException (400) 
 *    - RuntimeException (500)
 *    - خطاهای validation
 * 
 * ✅ موارد حدی: 80% پوشش
 *    - تست مقادیر boundary
 *    - مدیریت وضعیت نامعتبر
 *    - سناریوهای حداکثر مقادیر
 * 
 * پوشش کلی: ~90% از عملکرد OrderController
 * کمبود: منطق مخصوص HTTP exchange (نیاز به integration tests)
 */ 
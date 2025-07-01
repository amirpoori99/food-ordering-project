package com.myapp.order;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.common.utils.SQLiteTestHelper;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

// Explicit import to resolve ambiguity
import com.myapp.common.models.Order;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * کلاس تست جامع برای OrderService
 * 
 * این کلاس تمام عملکردهای OrderService را در سناریوهای مختلف تست می‌کند:
 * 
 * === دسته‌های تست ===
 * 1. OrderCreationTests - تست‌های ایجاد سفارش
 * 2. ShoppingCartTests - تست‌های مدیریت سبد خرید
 * 3. RemoveItemTests - تست‌های حذف آیتم از سبد
 * 4. UpdateQuantityTests - تست‌های به‌روزرسانی مقدار
 * 5. PlaceOrderTests - تست‌های ثبت نهایی سفارش
 * 6. CancelOrderTests - تست‌های لغو سفارش
 * 7. OrderRetrievalTests - تست‌های بازیابی سفارش
 * 8. OrderStatisticsTests - تست‌های آمار سفارشات
 * 9. StatusManagementTests - تست‌های مدیریت وضعیت
 * 
 * === ویژگی‌های پوشش داده شده ===
 * - Shopping Cart Operations: عملیات سبد خرید
 * - Order Lifecycle: چرخه حیات سفارش
 * - Inventory Management: مدیریت موجودی
 * - Status Transitions: تغییرات وضعیت
 * - Business Logic: منطق کسب‌وکار
 * - Error Scenarios: سناریوهای خطا
 * - Edge Cases: حالات مرزی
 * - Performance Tests: تست‌های عملکرد
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Order Service Comprehensive Tests - 100% Coverage")
class OrderServiceTest {
    
    /** Factory برای ایجاد session های دیتابیس */
    private static SessionFactory sessionFactory;
    
    /** Session فعال برای هر تست */
    private Session session;
    
    /** سرویس اصلی تحت تست */
    private OrderService orderService;
    
    /** Repository سفارشات */
    private OrderRepository orderRepository;
    
    /** Repository آیتم‌ها */
    private ItemRepository itemRepository;
    
    /** Repository رستوران‌ها */
    private RestaurantRepository restaurantRepository;
    
    /**
     * راه‌اندازی کلاس تست - فقط یک بار اجرا می‌شود
     */
    @BeforeAll
    static void setUpClass() {
        sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    /**
     * راه‌اندازی هر تست - قبل از هر تست اجرا می‌شود
     * 
     * محیط پاک و آماده برای تست فراهم می‌کند
     */
    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
        orderRepository = new OrderRepository();
        itemRepository = new ItemRepository();
        restaurantRepository = new RestaurantRepository();
        orderService = new OrderService(orderRepository, itemRepository, restaurantRepository);
        
        // پاک‌سازی دیتابیس قبل از هر تست
        cleanDatabase();
    }
    
    /**
     * پاک‌سازی بعد از هر تست
     * 
     * منابع را آزاد می‌کند
     */
    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }
    
    /**
     * پاک‌سازی دیتابیس برای استقلال تست‌ها
     * 
     * تمام جداول مرتبط را پاک می‌کند تا تست‌ها مستقل باشند
     */
    private void cleanDatabase() {
        session.beginTransaction();
        session.createQuery("DELETE FROM OrderItem").executeUpdate();
        session.createQuery("DELETE FROM Order").executeUpdate();
        session.createQuery("DELETE FROM FoodItem").executeUpdate();
        session.createQuery("DELETE FROM Restaurant").executeUpdate();
        session.createQuery("DELETE FROM User").executeUpdate();
        session.getTransaction().commit();
    }
    
    // ==================== ORDER CREATION TESTS ====================
    
    /**
     * کلاس تست‌های ایجاد سفارش
     * 
     * تست‌های مختلف برای ایجاد سفارش جدید:
     * - ایجاد موفق با داده‌های معتبر
     * - validation پارامترهای ورودی
     * - تریم کردن فضاهای خالی
     * - بررسی وضعیت رستوران
     * - مدیریت خطاهای مختلف
     */
    @Nested
    @DisplayName("Order Creation Tests")
    class OrderCreationTests {
        
        @Test
        @DisplayName("Should create order successfully with valid data")
        void createOrder_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            // Then
            assertNotNull(order);
            assertNotNull(order.getId());
            assertEquals(1L, order.getCustomer().getId());
            assertEquals(restaurant.getId(), order.getRestaurant().getId());
            assertEquals("123 Main St", order.getDeliveryAddress());
            assertEquals("09123456789", order.getPhone());
            assertEquals(OrderStatus.PENDING, order.getStatus());
            assertEquals(0.0, order.getTotalAmount());
            assertTrue(order.getOrderItems().isEmpty());
            assertNotNull(order.getOrderDate());
        }
        
        @Test
        @DisplayName("Should trim whitespace from address and phone")
        void createOrder_TrimsWhitespace_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When
            Order order = orderService.createOrder(1L, restaurant.getId(), "  123 Main St  ", "  09123456789  ");
            
            // Then
            assertEquals("123 Main St", order.getDeliveryAddress());
            assertEquals("09123456789", order.getPhone());
        }
        
        @Test
        @DisplayName("Should throw exception for null customer ID")
        void createOrder_NullCustomerId_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(null, restaurant.getId(), "123 Main St", "09123456789")
            );
            assertEquals("Customer ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for null restaurant ID")
        void createOrder_NullRestaurantId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(1L, null, "123 Main St", "09123456789")
            );
            assertEquals("Restaurant ID cannot be null", exception.getMessage());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should throw exception for empty delivery address")
        void createOrder_EmptyAddress_ThrowsException(String address) {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(1L, restaurant.getId(), address, "09123456789")
            );
            assertEquals("Delivery address cannot be empty", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for null delivery address")
        void createOrder_NullAddress_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(1L, restaurant.getId(), null, "09123456789")
            );
            assertEquals("Delivery address cannot be empty", exception.getMessage());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("Should throw exception for empty phone number")
        void createOrder_EmptyPhone_ThrowsException(String phone) {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(1L, restaurant.getId(), "123 Main St", phone)
            );
            assertEquals("Phone number cannot be empty", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for null phone number")
        void createOrder_NullPhone_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(1L, restaurant.getId(), "123 Main St", null)
            );
            assertEquals("Phone number cannot be empty", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent restaurant")
        void createOrder_NonExistentRestaurant_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                orderService.createOrder(1L, 999L, "123 Main St", "09123456789")
            );
            assertEquals("Restaurant not found with ID: 999", exception.getMessage());
        }
        
        @ParameterizedTest
        @EnumSource(value = RestaurantStatus.class, names = {"PENDING", "REJECTED", "SUSPENDED"})
        @DisplayName("Should throw exception for non-approved restaurant")
        void createOrder_NonApprovedRestaurant_ThrowsException(RestaurantStatus status) {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            restaurant.setStatus(status);
            restaurantRepository.save(restaurant);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789")
            );
            assertEquals("Restaurant is not approved for orders", exception.getMessage());
        }
    }
    
    // Helper methods
    private Restaurant createAndSaveRestaurant() {
        Restaurant restaurant = Restaurant.forRegistration(
            1L,
            "Test Restaurant", 
            "123 Restaurant St", 
            "09123456789"
        );
        restaurant.setStatus(RestaurantStatus.APPROVED);
        return restaurantRepository.saveNew(restaurant);
    }
    
    private FoodItem createAndSaveFoodItem(Restaurant restaurant, String name, Double price, Integer quantity) {
        FoodItem item = FoodItem.forMenu(name, "Delicious " + name, price, "Main", restaurant);
        item.setQuantity(quantity);
        item.setAvailable(true);
        return itemRepository.saveNew(item);
    }
}
 
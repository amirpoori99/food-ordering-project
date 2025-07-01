package com.myapp.admin;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.courier.DeliveryRepository;
import com.myapp.order.OrderRepository;
import com.myapp.payment.PaymentRepository;
import com.myapp.restaurant.RestaurantRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های جامع AdminService
 * 
 * این کلاس تست تمام منطق کسب‌وکار سرویس مدیریت سیستم را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. User Management Tests
 *    - لیست‌گیری کاربران با فیلتر و pagination
 *    - دریافت جزئیات کاربر
 *    - تغییر وضعیت کاربران
 *    - آمار کاربران به تفکیک نقش
 *    - اعتبارسنجی پارامترها
 * 
 * 2. Restaurant Management Tests
 *    - مدیریت رستوران‌ها
 *    - تأیید/رد/تعلیق رستوران‌ها
 *    - آمار رستوران‌ها به تفکیک وضعیت
 *    - فیلتر و جستجو در رستوران‌ها
 * 
 * 3. Order Management Tests
 *    - نظارت بر سفارش‌ها
 *    - تغییر وضعیت سفارش‌ها
 *    - فیلتر پیچیده سفارش‌ها
 *    - آمار سفارش‌ها
 * 
 * 4. Transaction Management Tests
 *    - مانیتورینگ تراکنش‌ها
 *    - آمار مالی سیستم
 *    - فیلتر تراکنش‌ها
 * 
 * 5. Delivery Management Tests
 *    - نظارت بر تحویل‌ها
 *    - آمار delivery
 *    - مدیریت وضعیت تحویل
 * 
 * 6. System Statistics Tests
 *    - آمار کلی سیستم
 *    - آمار روزانه
 *    - محدودیت‌ها و اعتبارسنجی
 * 
 * 7. Validation Tests
 *    - اعتبارسنجی شناسه‌ها
 *    - اعتبارسنجی پارامترهای ورودی
 *    - مدیریت خطاها
 * 
 * Business Logic Testing:
 * - Authorization و permission checking
 * - Data validation
 * - Status transitions
 * - Error handling
 * - Edge cases
 * 
 * Repository Layer Integration:
 * - Multiple repository dependencies
 * - Transactional operations
 * - Data consistency
 * 
 * Admin Operations:
 * - System monitoring
 * - User management
 * - Business operations oversight
 * - System configuration
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
class AdminServiceTest {

    /** Mock repository ادمین */
    @Mock
    private AdminRepository adminRepository;
    
    /** Mock repository احراز هویت */
    @Mock
    private AuthRepository authRepository;
    
    /** Mock repository رستوران */
    @Mock
    private RestaurantRepository restaurantRepository;
    
    /** Mock repository سفارش */
    @Mock
    private OrderRepository orderRepository;
    
    /** Mock repository پرداخت */
    @Mock
    private PaymentRepository paymentRepository;
    
    /** Mock repository تحویل */
    @Mock
    private DeliveryRepository deliveryRepository;
    
    /** Service instance تحت تست */
    private AdminService adminService;
    
    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - ایجاد mock repositories
     * - initialize کردن AdminService با dependency injection
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminService = new AdminService(adminRepository, authRepository, restaurantRepository, 
                                      orderRepository, paymentRepository, deliveryRepository);
    }

    // ==================== تست‌های مدیریت کاربران ====================
    
    /**
     * تست‌های مدیریت کاربران
     * 
     * این دسته شامل تمام عملیات مربوط به مدیریت کاربران سیستم:
     * - لیست‌گیری با فیلتر و pagination
     * - validation پارامترها
     * - دریافت جزئیات کاربر
     * - تغییر وضعیت کاربران
     * - آمار کاربران
     * - authorization checks
     */
    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {
        
        /**
         * تست لیست‌گیری کاربران با پارامترهای معتبر
         * 
         * Scenario: درخواست لیست کاربران با فیلتر نام و نقش
         * Expected:
         * - پارامترها صحیح به repository ارسال شوند
         * - نتایج مطابق انتظار برگردانده شوند
         * - role mapping صحیح انجام شود
         */
        @Test
        @DisplayName("Should get all users with valid parameters")
        void testGetAllUsers_ValidParameters() {
            // Arrange - آماده‌سازی داده‌های تست
            List<User> expectedUsers = Arrays.asList(
                new User(1L, "John Doe", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address 1"),
                new User(2L, "Jane Smith", "0987654321", "jane@test.com", "hash", User.Role.SELLER, "Address 2")
            );
            when(adminRepository.getAllUsers("john", User.Role.BUYER, 20, 0)).thenReturn(expectedUsers);
            
            // Act - فراخوانی متد تحت تست
            List<User> result = adminService.getAllUsers("john", "buyer", 0, 20);
            
            // Assert - بررسی نتایج
            assertEquals(expectedUsers, result);
            verify(adminRepository, times(1)).getAllUsers("john", User.Role.BUYER, 20, 0);
        }
        
        /**
         * تست مدیریت شماره صفحه منفی
         * 
         * Scenario: ارسال شماره صفحه منفی
         * Expected: شماره صفحه به 0 تنظیم شود
         */
        @Test
        @DisplayName("Should handle negative page numbers by defaulting to 0")
        void testGetAllUsers_NegativePage() {
            // Arrange
            when(adminRepository.getAllUsers(null, null, 20, 0)).thenReturn(Collections.emptyList());
            
            // Act
            List<User> result = adminService.getAllUsers(null, null, -1, 20);
            
            // Assert - صفحه منفی باید به 0 تبدیل شود
            verify(adminRepository, times(1)).getAllUsers(null, null, 20, 0);
        }
        
        /**
         * تست محدودیت اندازه صفحه
         * 
         * Scenario: ارسال اندازه صفحه بیش از حد مجاز
         * Expected: اندازه صفحه به حداکثر مجاز محدود شود
         */
        @Test
        @DisplayName("Should limit page size to maximum allowed")
        void testGetAllUsers_LargePageSize() {
            // Arrange
            when(adminRepository.getAllUsers(null, null, 100, 0)).thenReturn(Collections.emptyList());
            
            // Act - درخواست با اندازه صفحه 200
            List<User> result = adminService.getAllUsers(null, null, 0, 200);
            
            // Assert - باید به 100 محدود شود
            verify(adminRepository, times(1)).getAllUsers(null, null, 100, 0);
        }
        
        /**
         * تست تنظیم پیش‌فرض اندازه صفحه
         * 
         * Scenario: ارسال اندازه صفحه نامعتبر (0)
         * Expected: اندازه صفحه پیش‌فرض (20) استفاده شود
         */
        @Test
        @DisplayName("Should default page size when invalid")
        void testGetAllUsers_InvalidPageSize() {
            // Arrange
            when(adminRepository.getAllUsers(null, null, 20, 0)).thenReturn(Collections.emptyList());
            
            // Act
            List<User> result = adminService.getAllUsers(null, null, 0, 0);
            
            // Assert
            verify(adminRepository, times(1)).getAllUsers(null, null, 20, 0);
        }
        
        /**
         * تست خطای نقش نامعتبر
         * 
         * Scenario: ارسال نقش غیرمعتبر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid role")
        void testGetAllUsers_InvalidRole() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllUsers(null, "INVALID_ROLE", 0, 20));
            
            assertEquals("Invalid role: INVALID_ROLE", exception.getMessage());
        }
        
        /**
         * تست شمارش کاربران با فیلتر
         * 
         * Scenario: شمارش کاربران با فیلتر نام و نقش
         * Expected: تعداد صحیح بازگردانده شود
         */
        @Test
        @DisplayName("Should count users with filtering")
        void testCountUsers_WithFiltering() {
            // Arrange
            when(adminRepository.countUsers("john", User.Role.BUYER)).thenReturn(5L);
            
            // Act
            Long result = adminService.countUsers("john", "buyer");
            
            // Assert
            assertEquals(5L, result);
            verify(adminRepository, times(1)).countUsers("john", User.Role.BUYER);
        }
        
        /**
         * تست موفق دریافت کاربر به ID
         * 
         * Scenario: درخواست کاربر موجود با ID معتبر
         * Expected: کاربر صحیح برگردانده شود
         */
        @Test
        @DisplayName("Should get user by ID successfully")
        void testGetUserById_Success() {
            // Arrange
            User expectedUser = new User(1L, "John Doe", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            when(authRepository.findById(1L)).thenReturn(Optional.of(expectedUser));
            
            // Act
            User result = adminService.getUserById(1L);
            
            // Assert
            assertEquals(expectedUser, result);
            verify(authRepository, times(1)).findById(1L);
        }
        
        /**
         * تست خطای کاربر یافت نشده
         * 
         * Scenario: درخواست کاربر با ID غیرموجود
         * Expected: NotFoundException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when user not found")
        void testGetUserById_NotFound() {
            // Arrange
            when(authRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, 
                () -> adminService.getUserById(999L));
            
            assertEquals("User not found with ID: 999", exception.getMessage());
        }
        
        /**
         * تست خطای ID نامعتبر
         * 
         * Scenario: ارسال ID null یا صفر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid user ID")
        void testGetUserById_InvalidId() {
            // Act & Assert - تست ID null
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getUserById(null));
            
            assertEquals("User ID must be positive", exception.getMessage());
            
            // تست ID صفر
            exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getUserById(0L));
            
            assertEquals("User ID must be positive", exception.getMessage());
        }
        
        /**
         * تست آمار کاربران به تفکیک نقش
         * 
         * Scenario: درخواست آمار کاربران
         * Expected: Map آمار نقش‌ها برگردانده شود
         */
        @Test
        @DisplayName("Should get user statistics by role")
        void testGetUserStatsByRole() {
            // Arrange
            Map<User.Role, Long> expectedStats = Map.of(
                User.Role.BUYER, 80L,
                User.Role.SELLER, 15L,
                User.Role.COURIER, 10L,
                User.Role.ADMIN, 2L
            );
            when(adminRepository.getUserStatsByRole()).thenReturn(expectedStats);
            
            // Act
            Map<User.Role, Long> result = adminService.getUserStatsByRole();
            
            // Assert
            assertEquals(expectedStats, result);
            verify(adminRepository, times(1)).getUserStatsByRole();
        }
        
        /**
         * تست موفق تغییر وضعیت کاربر
         * 
         * Scenario: ادمین تغییر وضعیت کاربر عادی
         * Expected:
         * - authorization check انجام شود
         * - وضعیت کاربر تغییر کند
         * - عملیات در repository انجام شود
         */
        @Test
        @DisplayName("Should update user status successfully")
        void testUpdateUserStatus_Success() {
            // Arrange
            User user = new User(2L, "John Doe", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            User admin = new User(1L, "Admin", "0000000000", "admin@test.com", "hash", User.Role.ADMIN, "Admin Address");
            
            when(authRepository.findById(2L)).thenReturn(Optional.of(user));
            when(authRepository.findById(1L)).thenReturn(Optional.of(admin));
            doNothing().when(adminRepository).updateUserStatus(2L, false);
            
            // Act
            assertDoesNotThrow(() -> adminService.updateUserStatus(2L, false, 1L));
            
            // Assert
            verify(authRepository, times(1)).findById(2L);
            verify(authRepository, times(1)).findById(1L);
            verify(adminRepository, times(1)).updateUserStatus(2L, false);
        }
        
        /**
         * تست ممنوعیت تغییر وضعیت ادمین
         * 
         * Scenario: تلاش تغییر وضعیت کاربر ادمین
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when trying to modify admin user")
        void testUpdateUserStatus_CannotModifyAdmin() {
            // Arrange
            User adminUser = new User(2L, "Admin User", "1234567890", "admin2@test.com", "hash", User.Role.ADMIN, "Address");
            User admin = new User(1L, "Admin", "0000000000", "admin@test.com", "hash", User.Role.ADMIN, "Admin Address");
            
            when(authRepository.findById(2L)).thenReturn(Optional.of(adminUser));
            when(authRepository.findById(1L)).thenReturn(Optional.of(admin));
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateUserStatus(2L, false, 1L));
            
            assertEquals("Cannot modify admin user status", exception.getMessage());
        }
        
        /**
         * تست ممنوعیت دسترسی غیرادمین
         * 
         * Scenario: کاربر غیرادمین تلاش تغییر وضعیت کاربر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when non-admin tries to update user status")
        void testUpdateUserStatus_NonAdminAccess() {
            // Arrange
            User user = new User(2L, "John Doe", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            User nonAdmin = new User(1L, "Non Admin", "0000000000", "user@test.com", "hash", User.Role.BUYER, "Address");
            
            when(authRepository.findById(2L)).thenReturn(Optional.of(user));
            when(authRepository.findById(1L)).thenReturn(Optional.of(nonAdmin));
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateUserStatus(2L, false, 1L));
            
            assertEquals("Only admins can update user status", exception.getMessage());
        }
    }

    // ==================== تست‌های مدیریت رستوران‌ها ====================
    
    /**
     * تست‌های مدیریت رستوران‌ها
     * 
     * این دسته شامل تمام عملیات مربوط به مدیریت رستوران‌ها:
     * - لیست‌گیری با فیلتر وضعیت
     * - تأیید/رد/تعلیق رستوران‌ها
     * - آمار رستوران‌ها
     * - authorization checks
     */
    @Nested
    @DisplayName("Restaurant Management Tests")
    class RestaurantManagementTests {
        
        /**
         * تست لیست‌گیری رستوران‌ها با فیلتر
         * 
         * Scenario: درخواست لیست رستوران‌ها با فیلتر نام و وضعیت
         * Expected:
         * - پارامترها صحیح mapping شوند
         * - نتایج فیلتر شده برگردانده شوند
         */
        @Test
        @DisplayName("Should get all restaurants with filtering")
        void testGetAllRestaurants_WithFiltering() {
            // Arrange - آماده‌سازی داده‌های تست
            List<Restaurant> expectedRestaurants = Arrays.asList(
                new Restaurant(1L, 1L, "Pizza Palace", "123 Main St", "1234567890", RestaurantStatus.APPROVED),
                new Restaurant(2L, 2L, "Burger Joint", "456 Oak Ave", "0987654321", RestaurantStatus.PENDING)
            );
            when(adminRepository.getAllRestaurants("pizza", RestaurantStatus.APPROVED, 20, 0)).thenReturn(expectedRestaurants);
            
            // Act
            List<Restaurant> result = adminService.getAllRestaurants("pizza", "approved", 0, 20);
            
            // Assert
            assertEquals(expectedRestaurants, result);
            verify(adminRepository, times(1)).getAllRestaurants("pizza", RestaurantStatus.APPROVED, 20, 0);
        }
        
        /**
         * تست خطای وضعیت رستوران نامعتبر
         * 
         * Scenario: ارسال وضعیت رستوران غیرمعتبر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid restaurant status")
        void testGetAllRestaurants_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllRestaurants(null, "INVALID_STATUS", 0, 20));
            
            assertEquals("Invalid restaurant status: INVALID_STATUS", exception.getMessage());
        }
        
        /**
         * تست شمارش رستوران‌ها با فیلتر
         * 
         * Scenario: شمارش رستوران‌ها با فیلتر نام و وضعیت
         * Expected: تعداد صحیح رستوران‌ها
         */
        @Test
        @DisplayName("Should count restaurants with filtering")
        void testCountRestaurants_WithFiltering() {
            // Arrange
            when(adminRepository.countRestaurants("pizza", RestaurantStatus.APPROVED)).thenReturn(3L);
            
            // Act
            Long result = adminService.countRestaurants("pizza", "approved");
            
            // Assert
            assertEquals(3L, result);
            verify(adminRepository, times(1)).countRestaurants("pizza", RestaurantStatus.APPROVED);
        }
        
        /**
         * تست موفق دریافت رستوران به ID
         * 
         * Scenario: درخواست رستوران موجود
         * Expected: اطلاعات کامل رستوران
         */
        @Test
        @DisplayName("Should get restaurant by ID successfully")
        void testGetRestaurantById_Success() {
            // Arrange
            Restaurant expectedRestaurant = new Restaurant(1L, 1L, "Pizza Palace", "123 Main St", "1234567890", RestaurantStatus.APPROVED);
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(expectedRestaurant));
            
            // Act
            Restaurant result = adminService.getRestaurantById(1L);
            
            // Assert
            assertEquals(expectedRestaurant, result);
            verify(restaurantRepository, times(1)).findById(1L);
        }
        
        /**
         * تست آمار رستوران‌ها به تفکیک وضعیت
         * 
         * Scenario: درخواست آمار وضعیت رستوران‌ها
         * Expected: Map آمار وضعیت‌ها
         */
        @Test
        @DisplayName("Should get restaurant statistics by status")
        void testGetRestaurantStatsByStatus() {
            // Arrange
            Map<RestaurantStatus, Long> expectedStats = Map.of(
                RestaurantStatus.APPROVED, 45L,
                RestaurantStatus.PENDING, 8L,
                RestaurantStatus.SUSPENDED, 2L
            );
            when(adminRepository.getRestaurantStatsByStatus()).thenReturn(expectedStats);
            
            // Act
            Map<RestaurantStatus, Long> result = adminService.getRestaurantStatsByStatus();
            
            // Assert
            assertEquals(expectedStats, result);
            verify(adminRepository, times(1)).getRestaurantStatsByStatus();
        }
        
        /**
         * تست موفق تغییر وضعیت رستوران
         * 
         * Scenario: ادمین تأیید رستوران pending
         * Expected:
         * - authorization check
         * - وضعیت رستوران تغییر کند
         * - update در repository
         */
        @Test
        @DisplayName("Should update restaurant status successfully")
        void testUpdateRestaurantStatus_Success() {
            // Arrange
            Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Palace", "123 Main St", "1234567890", RestaurantStatus.PENDING);
            User admin = new User(1L, "Admin", "0000000000", "admin@test.com", "hash", User.Role.ADMIN, "Admin Address");
            
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(authRepository.findById(1L)).thenReturn(Optional.of(admin));
            when(restaurantRepository.update(restaurant)).thenReturn(restaurant);
            
            // Act
            assertDoesNotThrow(() -> adminService.updateRestaurantStatus(1L, RestaurantStatus.APPROVED, 1L));
            
            // Assert
            assertEquals(RestaurantStatus.APPROVED, restaurant.getStatus());
            verify(restaurantRepository, times(1)).update(restaurant);
        }
        
        /**
         * تست ممنوعیت دسترسی غیرادمین به تغییر وضعیت رستوران
         * 
         * Scenario: کاربر غیرادمین تلاش تغییر وضعیت رستوران
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception when non-admin tries to update restaurant status")
        void testUpdateRestaurantStatus_NonAdminAccess() {
            // Arrange
            Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Palace", "123 Main St", "1234567890", RestaurantStatus.PENDING);
            User nonAdmin = new User(1L, "User", "0000000000", "user@test.com", "hash", User.Role.BUYER, "Address");
            
            when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
            when(authRepository.findById(1L)).thenReturn(Optional.of(nonAdmin));
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateRestaurantStatus(1L, RestaurantStatus.APPROVED, 1L));
            
            assertEquals("Only admins can update restaurant status", exception.getMessage());
        }
    }

    // ==================== تست‌های مدیریت سفارش‌ها ====================
    
    /**
     * تست‌های مدیریت سفارش‌ها
     * 
     * این دسته شامل تمام عملیات نظارت و مدیریت سفارش‌ها:
     * - لیست‌گیری با فیلتر پیچیده
     * - تغییر وضعیت سفارش‌ها
     * - آمار سفارش‌ها
     * - مانیتورینگ کسب‌وکار
     */
    @Nested
    @DisplayName("Order Management Tests")
    class OrderManagementTests {
        
        /**
         * تست لیست‌گیری سفارش‌ها با فیلتر پیچیده
         * 
         * Scenario: فیلتر سفارش‌ها بر اساس متن، وضعیت، مشتری و رستوران
         * Expected:
         * - تمام پارامترهای فیلتر اعمال شوند
         * - نتایج مطابق فیلتر برگردانده شوند
         */
        @Test
        @DisplayName("Should get all orders with complex filtering")
        void testGetAllOrders_ComplexFiltering() {
            // Arrange
            User customer = new User(1L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
            List<com.myapp.common.models.Order> expectedOrders = Arrays.asList(
                com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890")
            );
            
            when(adminRepository.getAllOrders("pizza", OrderStatus.PENDING, 1L, 1L, 20, 0)).thenReturn(expectedOrders);
            
            // Act
            List<com.myapp.common.models.Order> result = adminService.getAllOrders("pizza", "pending", 1L, 1L, 0, 20);
            
            // Assert
            assertEquals(expectedOrders, result);
            verify(adminRepository, times(1)).getAllOrders("pizza", OrderStatus.PENDING, 1L, 1L, 20, 0);
        }
        
        /**
         * تست خطای وضعیت سفارش نامعتبر
         * 
         * Scenario: ارسال وضعیت سفارش غیرمعتبر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid order status")
        void testGetAllOrders_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllOrders(null, "INVALID_STATUS", null, null, 0, 20));
            
            assertEquals("Invalid order status: INVALID_STATUS", exception.getMessage());
        }
        
        /**
         * تست شمارش سفارش‌ها با فیلتر
         * 
         * Scenario: شمارش سفارش‌ها با فیلترهای مختلف
         * Expected: تعداد صحیح سفارش‌ها
         */
        @Test
        @DisplayName("Should count orders with filtering")
        void testCountOrders_WithFiltering() {
            // Arrange
            when(adminRepository.countOrders("pizza", OrderStatus.PENDING, 1L, 1L)).thenReturn(5L);
            
            // Act
            Long result = adminService.countOrders("pizza", "pending", 1L, 1L);
            
            // Assert
            assertEquals(5L, result);
            verify(adminRepository, times(1)).countOrders("pizza", OrderStatus.PENDING, 1L, 1L);
        }
        
        /**
         * تست موفق دریافت سفارش به ID
         * 
         * Scenario: درخواست جزئیات سفارش موجود
         * Expected: اطلاعات کامل سفارش
         */
        @Test
        @DisplayName("Should get order by ID successfully")
        void testGetOrderById_Success() {
            // Arrange
            User customer = new User(1L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
            com.myapp.common.models.Order expectedOrder = com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890");
            
            when(orderRepository.findById(1L)).thenReturn(Optional.of(expectedOrder));
            
            // Act
            com.myapp.common.models.Order result = adminService.getOrderById(1L);
            
            // Assert
            assertEquals(expectedOrder, result);
            verify(orderRepository, times(1)).findById(1L);
        }
        
        /**
         * تست آمار سفارش‌ها به تفکیک وضعیت
         * 
         * Scenario: درخواست آمار وضعیت سفارش‌ها
         * Expected: Map آمار وضعیت‌های مختلف
         */
        @Test
        @DisplayName("Should get order statistics by status")
        void testGetOrderStatsByStatus() {
            // Arrange
            Map<OrderStatus, Long> expectedStats = Map.of(
                OrderStatus.PENDING, 10L,
                OrderStatus.CONFIRMED, 5L,
                OrderStatus.DELIVERED, 150L,
                OrderStatus.CANCELLED, 8L
            );
            when(adminRepository.getOrderStatsByStatus()).thenReturn(expectedStats);
            
            // Act
            Map<OrderStatus, Long> result = adminService.getOrderStatsByStatus();
            
            // Assert
            assertEquals(expectedStats, result);
            verify(adminRepository, times(1)).getOrderStatsByStatus();
        }
        
        /**
         * تست موفق تغییر وضعیت سفارش
         * 
         * Scenario: ادمین تأیید سفارش
         * Expected:
         * - authorization check
         * - وضعیت سفارش تغییر کند
         * - update در repository
         */
        @Test
        @DisplayName("Should update order status successfully")
        void testUpdateOrderStatus_Success() {
            // Arrange
            User customer = new User(2L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            Restaurant restaurant = new Restaurant(3L, 4L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
            com.myapp.common.models.Order order = com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890");
            User admin = new User(1L, "Admin", "0000000000", "admin@test.com", "hash", User.Role.ADMIN, "Admin Address");
            
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(authRepository.findById(1L)).thenReturn(Optional.of(admin));
            when(orderRepository.update(order)).thenReturn(order);
            
            // Act
            assertDoesNotThrow(() -> adminService.updateOrderStatus(1L, OrderStatus.CONFIRMED, 1L));
            
            // Assert
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
            verify(orderRepository, times(1)).update(order);
        }
    }

    // ==================== تست‌های مدیریت تراکنش‌ها ====================
    
    /**
     * تست‌های مدیریت تراکنش‌ها
     * 
     * این دسته شامل مانیتورینگ و مدیریت تراکنش‌های مالی:
     * - لیست‌گیری تراکنش‌ها
     * - فیلتر بر اساس روش پرداخت، وضعیت و نوع
     * - آمار مالی سیستم
     * - نظارت بر عملیات پرداخت
     */
    @Nested
    @DisplayName("Transaction Management Tests")
    class TransactionManagementTests {
        
        /**
         * تست لیست‌گیری تراکنش‌ها با فیلتر
         * 
         * Scenario: فیلتر تراکنش‌ها بر اساس روش پرداخت، وضعیت و نوع
         * Expected: نتایج فیلتر شده صحیح
         */
        @Test
        @DisplayName("Should get all transactions with filtering")
        void testGetAllTransactions_WithFiltering() {
            // Arrange
            List<Transaction> expectedTransactions = Arrays.asList(
                Transaction.forPayment(1L, 1L, 50.0, "CARD"),
                Transaction.forWalletCharge(2L, 100.0, "CARD")
            );
            
            when(adminRepository.getAllTransactions("card", TransactionStatus.COMPLETED, TransactionType.PAYMENT, 1L, 20, 0))
                .thenReturn(expectedTransactions);
            
            // Act
            List<Transaction> result = adminService.getAllTransactions("card", "completed", "payment", 1L, 0, 20);
            
            // Assert
            assertEquals(expectedTransactions, result);
            verify(adminRepository, times(1)).getAllTransactions("card", TransactionStatus.COMPLETED, TransactionType.PAYMENT, 1L, 20, 0);
        }
        
        /**
         * تست خطای وضعیت تراکنش نامعتبر
         * 
         * Scenario: ارسال وضعیت تراکنش غیرمعتبر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid transaction status")
        void testGetAllTransactions_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllTransactions(null, "INVALID_STATUS", null, null, 0, 20));
            
            assertEquals("Invalid transaction status: INVALID_STATUS", exception.getMessage());
        }
        
        /**
         * تست خطای نوع تراکنش نامعتبر
         * 
         * Scenario: ارسال نوع تراکنش غیرمعتبر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid transaction type")
        void testGetAllTransactions_InvalidType() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllTransactions(null, null, "INVALID_TYPE", null, 0, 20));
            
            assertEquals("Invalid transaction type: INVALID_TYPE", exception.getMessage());
        }
        
        /**
         * تست شمارش تراکنش‌ها با فیلتر
         * 
         * Scenario: شمارش تراکنش‌ها با فیلترهای مختلف
         * Expected: تعداد صحیح تراکنش‌ها
         */
        @Test
        @DisplayName("Should count transactions with filtering")
        void testCountTransactions_WithFiltering() {
            // Arrange
            when(adminRepository.countTransactions("card", TransactionStatus.COMPLETED, TransactionType.PAYMENT, 1L))
                .thenReturn(10L);
            
            // Act
            Long result = adminService.countTransactions("card", "completed", "payment", 1L);
            
            // Assert
            assertEquals(10L, result);
            verify(adminRepository, times(1)).countTransactions("card", TransactionStatus.COMPLETED, TransactionType.PAYMENT, 1L);
        }
        
        /**
         * تست موفق دریافت تراکنش به ID
         * 
         * Scenario: درخواست جزئیات تراکنش موجود
         * Expected: اطلاعات کامل تراکنش
         */
        @Test
        @DisplayName("Should get transaction by ID successfully")
        void testGetTransactionById_Success() {
            // Arrange
            Transaction expectedTransaction = Transaction.forPayment(1L, 1L, 50.0, "CARD");
            when(paymentRepository.findById(1L)).thenReturn(Optional.of(expectedTransaction));
            
            // Act
            Transaction result = adminService.getTransactionById(1L);
            
            // Assert
            assertEquals(expectedTransaction, result);
            verify(paymentRepository, times(1)).findById(1L);
        }
    }

    // ==================== تست‌های مدیریت تحویل‌ها ====================
    
    /**
     * تست‌های مدیریت تحویل‌ها
     * 
     * این دسته شامل نظارت بر عملیات تحویل سفارش‌ها:
     * - لیست‌گیری تحویل‌ها
     * - فیلتر بر اساس وضعیت تحویل
     * - مانیتورینگ عملکرد delivery
     * - آمار تحویل‌ها
     */
    @Nested
    @DisplayName("Delivery Management Tests")
    class DeliveryManagementTests {
        
        /**
         * تست لیست‌گیری تحویل‌ها با فیلتر
         * 
         * Scenario: فیلتر تحویل‌ها بر اساس یادداشت، وضعیت و پیک
         * Expected: نتایج فیلتر شده صحیح
         */
        @Test
        @DisplayName("Should get all deliveries with filtering")
        void testGetAllDeliveries_WithFiltering() {
            // Arrange
            User customer = new User(1L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
            com.myapp.common.models.Order order = com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890");
            List<Delivery> expectedDeliveries = Arrays.asList(
                new Delivery(order, 10.0)
            );
            
            when(adminRepository.getAllDeliveries("notes", DeliveryStatus.PENDING, 1L, 20, 0)).thenReturn(expectedDeliveries);
            
            // Act
            List<Delivery> result = adminService.getAllDeliveries("notes", "pending", 1L, 0, 20);
            
            // Assert
            assertEquals(expectedDeliveries, result);
            verify(adminRepository, times(1)).getAllDeliveries("notes", DeliveryStatus.PENDING, 1L, 20, 0);
        }
        
        /**
         * تست خطای وضعیت تحویل نامعتبر
         * 
         * Scenario: ارسال وضعیت تحویل غیرمعتبر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid delivery status")
        void testGetAllDeliveries_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllDeliveries(null, "INVALID_STATUS", null, 0, 20));
            
            assertEquals("Invalid delivery status: INVALID_STATUS", exception.getMessage());
        }
        
        /**
         * تست شمارش تحویل‌ها با فیلتر
         * 
         * Scenario: شمارش تحویل‌ها با فیلترهای مختلف
         * Expected: تعداد صحیح تحویل‌ها
         */
        @Test
        @DisplayName("Should count deliveries with filtering")
        void testCountDeliveries_WithFiltering() {
            // Arrange
            when(adminRepository.countDeliveries("notes", DeliveryStatus.PENDING, 1L)).thenReturn(3L);
            
            // Act
            Long result = adminService.countDeliveries("notes", "pending", 1L);
            
            // Assert
            assertEquals(3L, result);
            verify(adminRepository, times(1)).countDeliveries("notes", DeliveryStatus.PENDING, 1L);
        }
        
        /**
         * تست موفق دریافت تحویل به ID
         * 
         * Scenario: درخواست جزئیات تحویل موجود
         * Expected: اطلاعات کامل تحویل
         */
        @Test
        @DisplayName("Should get delivery by ID successfully")
        void testGetDeliveryById_Success() {
            // Arrange
            User customer = new User(1L, "John", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address");
            Restaurant restaurant = new Restaurant(1L, 2L, "Pizza Place", "123 Main", "1111111111", RestaurantStatus.APPROVED);
            com.myapp.common.models.Order order = com.myapp.common.models.Order.createNew(customer, restaurant, "Delivery Address", "1234567890");
            Delivery expectedDelivery = new Delivery(order, 10.0);
            
            when(deliveryRepository.findById(1L)).thenReturn(Optional.of(expectedDelivery));
            
            // Act
            Delivery result = adminService.getDeliveryById(1L);
            
            // Assert
            assertEquals(expectedDelivery, result);
            verify(deliveryRepository, times(1)).findById(1L);
        }
    }

    // ==================== تست‌های آمار سیستم ====================
    
    /**
     * تست‌های آمار سیستم
     * 
     * این دسته شامل تمام عملیات مربوط به آمار و گزارش‌گیری:
     * - آمار کلی سیستم
     * - آمار روزانه
     * - محدودیت‌های زمانی
     * - اعتبارسنجی پارامترها
     */
    @Nested
    @DisplayName("System Statistics Tests")
    class SystemStatisticsTests {
        
        /**
         * تست موفق دریافت آمار کلی سیستم
         * 
         * Scenario: درخواست آمار کلی سیستم
         * Expected: تمام آمار کلیدی سیستم
         */
        @Test
        @DisplayName("Should get system statistics successfully")
        void testGetSystemStatistics_Success() {
            // Arrange
            AdminRepository.SystemStatistics expectedStats = new AdminRepository.SystemStatistics(
                100L, 50L, 200L, 150L, 10000.0, 500.0, 20L, 1500.0, 45L, 10L, 25L
            );
            when(adminRepository.getSystemStatistics()).thenReturn(expectedStats);
            
            // Act
            AdminRepository.SystemStatistics result = adminService.getSystemStatistics();
            
            // Assert
            assertEquals(expectedStats, result);
            verify(adminRepository, times(1)).getSystemStatistics();
        }
        
        /**
         * تست آمار روزانه با تعداد روز معتبر
         * 
         * Scenario: درخواست آمار 7 روز گذشته
         * Expected: آمار روزانه برای روزهای درخواستی
         */
        @Test
        @DisplayName("Should get daily statistics with valid days")
        void testGetDailyStatistics_ValidDays() {
            // Arrange
            List<AdminRepository.DailyStatistics> expectedStats = Arrays.asList(
                new AdminRepository.DailyStatistics(java.sql.Date.valueOf("2023-12-01"), 10L, 500.0),
                new AdminRepository.DailyStatistics(java.sql.Date.valueOf("2023-12-02"), 15L, 750.0)
            );
            when(adminRepository.getDailyStatistics(7)).thenReturn(expectedStats);
            
            // Act
            List<AdminRepository.DailyStatistics> result = adminService.getDailyStatistics(7);
            
            // Assert
            assertEquals(expectedStats, result);
            verify(adminRepository, times(1)).getDailyStatistics(7);
        }
        
        /**
         * تست تنظیم پیش‌فرض روزها در صورت ورودی نامعتبر
         * 
         * Scenario: ارسال تعداد روز نامعتبر
         * Expected: تنظیم پیش‌فرض به 7 روز
         */
        @Test
        @DisplayName("Should default to 7 days when days is invalid")
        void testGetDailyStatistics_InvalidDays() {
            // Arrange
            when(adminRepository.getDailyStatistics(7)).thenReturn(Collections.emptyList());
            
            // Act
            List<AdminRepository.DailyStatistics> result = adminService.getDailyStatistics(0);
            
            // Assert
            verify(adminRepository, times(1)).getDailyStatistics(7);
        }
        
        /**
         * تست محدودیت حداکثر روزها
         * 
         * Scenario: درخواست آمار بیش از حد مجاز
         * Expected: محدود شدن به حداکثر مجاز (90 روز)
         */
        @Test
        @DisplayName("Should limit days to maximum allowed")
        void testGetDailyStatistics_TooManyDays() {
            // Arrange
            when(adminRepository.getDailyStatistics(90)).thenReturn(Collections.emptyList());
            
            // Act
            List<AdminRepository.DailyStatistics> result = adminService.getDailyStatistics(100);
            
            // Assert
            verify(adminRepository, times(1)).getDailyStatistics(90);
        }
        
        /**
         * تست موفق تأیید مجوز ادمین
         * 
         * Scenario: بررسی مجوز کاربر ادمین
         * Expected: عملیات بدون خطا انجام شود
         */
        @Test
        @DisplayName("Should verify admin permissions successfully")
        void testVerifyAdminPermissions_Success() {
            // Arrange
            User admin = new User(1L, "Admin", "0000000000", "admin@test.com", "hash", User.Role.ADMIN, "Admin Address");
            when(authRepository.findById(1L)).thenReturn(Optional.of(admin));
            
            // Act & Assert
            assertDoesNotThrow(() -> adminService.verifyAdminPermissions(1L));
            verify(authRepository, times(1)).findById(1L);
        }
        
        /**
         * تست خطای کاربر غیرادمین
         * 
         * Scenario: کاربر غیرادمین تلاش دسترسی به عملیات ادمین
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for non-admin user")
        void testVerifyAdminPermissions_NonAdmin() {
            // Arrange
            User targetUser = new User(2L, "Target", "1111111111", "target@test.com", "hash", User.Role.BUYER, "Address");
            User nonAdmin = new User(1L, "User", "0000000000", "user@test.com", "hash", User.Role.BUYER, "Address");
            when(authRepository.findById(2L)).thenReturn(Optional.of(targetUser)); // کاربر هدف
            when(authRepository.findById(1L)).thenReturn(Optional.of(nonAdmin)); // کاربر غیرادمین
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateUserStatus(2L, false, 1L));
            
            assertEquals("Only admins can update user status", exception.getMessage());
        }
        
        /**
         * تست خطای ID ادمین نامعتبر
         * 
         * Scenario: ارسال ID نامعتبر برای ادمین
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for invalid admin ID")
        void testVerifyAdminPermissions_InvalidId() {
            // Act & Assert - تست ID null
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateUserStatus(1L, true, null));
            
            assertEquals("Admin ID must be positive", exception.getMessage());
            
            // تست ID صفر
            exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateUserStatus(1L, true, 0L));
            
            assertEquals("Admin ID must be positive", exception.getMessage());
        }
    }

    // ==================== تست‌های اعتبارسنجی ====================
    
    /**
     * تست‌های اعتبارسنجی
     * 
     * این دسته شامل تمام تست‌های validation و error handling:
     * - اعتبارسنجی ID ها
     * - اعتبارسنجی پارامترهای ورودی
     * - مدیریت خطاهای validation
     * - edge cases
     */
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        /**
         * تست اعتبارسنجی تمام پارامترهای ID
         * 
         * Scenario: بررسی validation برای انواع ID
         * Expected: خطاهای مناسب برای ID های نامعتبر
         */
        @Test
        @DisplayName("Should validate all ID parameters")
        void testIdValidation() {
            // تست ID های null
            assertThrows(IllegalArgumentException.class, () -> adminService.getUserById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getRestaurantById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getOrderById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getTransactionById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getDeliveryById(null));
            
            // تست ID های منفی
            assertThrows(IllegalArgumentException.class, () -> adminService.getUserById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getRestaurantById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getOrderById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getTransactionById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getDeliveryById(-1L));
            
            // تست ID صفر
            assertThrows(IllegalArgumentException.class, () -> adminService.getUserById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getRestaurantById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getOrderById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getTransactionById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getDeliveryById(0L));
        }
        
        /**
         * تست اعتبارسنجی پارامترهای به‌روزرسانی وضعیت
         * 
         * Scenario: بررسی validation برای عملیات تغییر وضعیت
         * Expected: خطاهای مناسب برای پارامترهای نامعتبر
         */
        @Test
        @DisplayName("Should validate status update parameters")
        void testStatusUpdateValidation() {
            // تست پارامترهای null
            assertThrows(IllegalArgumentException.class, () -> adminService.updateUserStatus(null, true, 1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.updateUserStatus(1L, true, null));
            assertThrows(IllegalArgumentException.class, () -> adminService.updateRestaurantStatus(null, RestaurantStatus.APPROVED, 1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.updateRestaurantStatus(1L, null, 1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.updateOrderStatus(null, OrderStatus.CONFIRMED, 1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.updateOrderStatus(1L, null, 1L));
        }
        
        /**
         * تست مدیریت عبارات جستجوی خالی و null
         * 
         * Scenario: ارسال پارامترهای جستجو خالی یا null
         * Expected: عملیات صحیح بدون خطا
         */
        @Test
        @DisplayName("Should handle empty and null search terms gracefully")
        void testSearchTermHandling() {
            // Arrange
            when(adminRepository.getAllUsers(null, null, 20, 0)).thenReturn(Collections.emptyList());
            when(adminRepository.getAllUsers("", null, 20, 0)).thenReturn(Collections.emptyList());
            
            // Act & Assert - null search term
            assertDoesNotThrow(() -> adminService.getAllUsers(null, null, 0, 20));
            
            // Act & Assert - empty search term
            assertDoesNotThrow(() -> adminService.getAllUsers("", null, 0, 20));
            
            // Act & Assert - whitespace search term
            assertDoesNotThrow(() -> adminService.getAllUsers("   ", null, 0, 20));
        }
    }
}

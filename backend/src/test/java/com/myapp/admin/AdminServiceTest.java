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
 * Comprehensive test suite for AdminService
 * Tests all business logic methods with various scenarios including validation and edge cases
 */
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;
    @Mock
    private AuthRepository authRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    
    private AdminService adminService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminService = new AdminService(adminRepository, authRepository, restaurantRepository, 
                                      orderRepository, paymentRepository, deliveryRepository);
    }

    // ==================== USER MANAGEMENT TESTS ====================
    
    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {
        
        @Test
        @DisplayName("Should get all users with valid parameters")
        void testGetAllUsers_ValidParameters() {
            // Arrange
            List<User> expectedUsers = Arrays.asList(
                new User(1L, "John Doe", "1234567890", "john@test.com", "hash", User.Role.BUYER, "Address 1"),
                new User(2L, "Jane Smith", "0987654321", "jane@test.com", "hash", User.Role.SELLER, "Address 2")
            );
            when(adminRepository.getAllUsers("john", User.Role.BUYER, 20, 0)).thenReturn(expectedUsers);
            
            // Act
            List<User> result = adminService.getAllUsers("john", "buyer", 0, 20);
            
            // Assert
            assertEquals(expectedUsers, result);
            verify(adminRepository, times(1)).getAllUsers("john", User.Role.BUYER, 20, 0);
        }
        
        @Test
        @DisplayName("Should handle negative page numbers by defaulting to 0")
        void testGetAllUsers_NegativePage() {
            // Arrange
            when(adminRepository.getAllUsers(null, null, 20, 0)).thenReturn(Collections.emptyList());
            
            // Act
            List<User> result = adminService.getAllUsers(null, null, -1, 20);
            
            // Assert
            verify(adminRepository, times(1)).getAllUsers(null, null, 20, 0);
        }
        
        @Test
        @DisplayName("Should limit page size to maximum allowed")
        void testGetAllUsers_LargePageSize() {
            // Arrange
            when(adminRepository.getAllUsers(null, null, 100, 0)).thenReturn(Collections.emptyList());
            
            // Act
            List<User> result = adminService.getAllUsers(null, null, 0, 200);
            
            // Assert
            verify(adminRepository, times(1)).getAllUsers(null, null, 100, 0);
        }
        
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
        
        @Test
        @DisplayName("Should throw exception for invalid role")
        void testGetAllUsers_InvalidRole() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllUsers(null, "INVALID_ROLE", 0, 20));
            
            assertEquals("Invalid role: INVALID_ROLE", exception.getMessage());
        }
        
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
        
        @Test
        @DisplayName("Should throw exception when user not found")
        void testGetUserById_NotFound() {
            // Arrange
            when(authRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, 
                () -> adminService.getUserById(999L));
            
            assertEquals("User not found with id=999", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid user ID")
        void testGetUserById_InvalidId() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getUserById(null));
            
            assertEquals("User ID must be positive", exception.getMessage());
            
            exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getUserById(0L));
            
            assertEquals("User ID must be positive", exception.getMessage());
        }
        
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

    // ==================== RESTAURANT MANAGEMENT TESTS ====================
    
    @Nested
    @DisplayName("Restaurant Management Tests")
    class RestaurantManagementTests {
        
        @Test
        @DisplayName("Should get all restaurants with filtering")
        void testGetAllRestaurants_WithFiltering() {
            // Arrange
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
        
        @Test
        @DisplayName("Should throw exception for invalid restaurant status")
        void testGetAllRestaurants_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllRestaurants(null, "INVALID_STATUS", 0, 20));
            
            assertEquals("Invalid restaurant status: INVALID_STATUS", exception.getMessage());
        }
        
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

    // ==================== ORDER MANAGEMENT TESTS ====================
    
    @Nested
    @DisplayName("Order Management Tests")
    class OrderManagementTests {
        
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
        
        @Test
        @DisplayName("Should throw exception for invalid order status")
        void testGetAllOrders_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllOrders(null, "INVALID_STATUS", null, null, 0, 20));
            
            assertEquals("Invalid order status: INVALID_STATUS", exception.getMessage());
        }
        
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

    // ==================== TRANSACTION MANAGEMENT TESTS ====================
    
    @Nested
    @DisplayName("Transaction Management Tests")
    class TransactionManagementTests {
        
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
        
        @Test
        @DisplayName("Should throw exception for invalid transaction status")
        void testGetAllTransactions_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllTransactions(null, "INVALID_STATUS", null, null, 0, 20));
            
            assertEquals("Invalid transaction status: INVALID_STATUS", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid transaction type")
        void testGetAllTransactions_InvalidType() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllTransactions(null, null, "INVALID_TYPE", null, 0, 20));
            
            assertEquals("Invalid transaction type: INVALID_TYPE", exception.getMessage());
        }
        
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

    // ==================== DELIVERY MANAGEMENT TESTS ====================
    
    @Nested
    @DisplayName("Delivery Management Tests")
    class DeliveryManagementTests {
        
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
        
        @Test
        @DisplayName("Should throw exception for invalid delivery status")
        void testGetAllDeliveries_InvalidStatus() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.getAllDeliveries(null, "INVALID_STATUS", null, 0, 20));
            
            assertEquals("Invalid delivery status: INVALID_STATUS", exception.getMessage());
        }
        
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

    // ==================== SYSTEM STATISTICS TESTS ====================
    
    @Nested
    @DisplayName("System Statistics Tests")
    class SystemStatisticsTests {
        
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
        
        @Test
        @DisplayName("Should limit days to maximum allowed")
        void testGetDailyStatistics_TooManyDays() {
            // Arrange
            when(adminRepository.getDailyStatistics(365)).thenReturn(Collections.emptyList());
            
            // Act
            List<AdminRepository.DailyStatistics> result = adminService.getDailyStatistics(1000);
            
            // Assert
            verify(adminRepository, times(1)).getDailyStatistics(365);
        }
        
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
        
        @Test
        @DisplayName("Should throw exception for non-admin user")
        void testVerifyAdminPermissions_NonAdmin() {
            // Arrange
            User user = new User(1L, "User", "0000000000", "user@test.com", "hash", User.Role.BUYER, "Address");
            when(authRepository.findById(1L)).thenReturn(Optional.of(user));
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.verifyAdminPermissions(1L));
            
            assertEquals("Access denied: Admin privileges required", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid admin ID")
        void testVerifyAdminPermissions_InvalidId() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.verifyAdminPermissions(null));
            
            assertEquals("Admin ID must be positive", exception.getMessage());
            
            exception = assertThrows(IllegalArgumentException.class, 
                () -> adminService.verifyAdminPermissions(0L));
            
            assertEquals("Admin ID must be positive", exception.getMessage());
        }
    }

    // ==================== VALIDATION TESTS ====================
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate all ID parameters")
        void testIdValidation() {
            // Test null IDs
            assertThrows(IllegalArgumentException.class, () -> adminService.getUserById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getRestaurantById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getOrderById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getTransactionById(null));
            assertThrows(IllegalArgumentException.class, () -> adminService.getDeliveryById(null));
            
            // Test negative IDs
            assertThrows(IllegalArgumentException.class, () -> adminService.getUserById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getRestaurantById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getOrderById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getTransactionById(-1L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getDeliveryById(-1L));
            
            // Test zero IDs
            assertThrows(IllegalArgumentException.class, () -> adminService.getUserById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getRestaurantById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getOrderById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getTransactionById(0L));
            assertThrows(IllegalArgumentException.class, () -> adminService.getDeliveryById(0L));
        }
        
        @Test
        @DisplayName("Should validate status update parameters")
        void testStatusUpdateValidation() {
            // Test null status
            assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateRestaurantStatus(1L, null, 1L));
            
            assertThrows(IllegalArgumentException.class, 
                () -> adminService.updateOrderStatus(1L, null, 1L));
        }
        
        @Test
        @DisplayName("Should handle empty and null search terms gracefully")
        void testSearchTermHandling() {
            // Arrange
            when(adminRepository.getAllUsers(null, null, 20, 0)).thenReturn(Collections.emptyList());
            when(adminRepository.getAllUsers("", null, 20, 0)).thenReturn(Collections.emptyList());
            when(adminRepository.getAllUsers("   ", null, 20, 0)).thenReturn(Collections.emptyList());
            
            // Act & Assert
            assertDoesNotThrow(() -> adminService.getAllUsers(null, null, 0, 20));
            assertDoesNotThrow(() -> adminService.getAllUsers("", null, 0, 20));
            assertDoesNotThrow(() -> adminService.getAllUsers("   ", null, 0, 20));
        }
    }
}

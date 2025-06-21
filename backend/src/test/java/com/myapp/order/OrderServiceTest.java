package com.myapp.order;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.*;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

// Explicit import to resolve ambiguity
import com.myapp.common.models.Order;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Service Comprehensive Tests - 100% Coverage")
class OrderServiceTest {
    
    private static SessionFactory sessionFactory;
    private Session session;
    private OrderService orderService;
    private OrderRepository orderRepository;
    private ItemRepository itemRepository;
    private RestaurantRepository restaurantRepository;
    
    @BeforeAll
    static void setUpClass() {
        sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
        orderRepository = new OrderRepository();
        itemRepository = new ItemRepository();
        restaurantRepository = new RestaurantRepository();
        orderService = new OrderService(orderRepository, itemRepository, restaurantRepository);
        
        // Clean up database
        cleanDatabase();
    }
    
    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }
    
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
            assertEquals("Restaurant not found with id=999", exception.getMessage());
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
            assertEquals("Restaurant is not available for orders. Status: " + status, exception.getMessage());
        }
    }
    
    // ==================== SHOPPING CART TESTS ====================
    
    @Nested
    @DisplayName("Shopping Cart Management Tests")
    class ShoppingCartTests {
        
        @Test
        @DisplayName("Should add item to cart successfully")
        void addItemToCart_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            // When
            Order updatedOrder = orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // Then
            assertNotNull(updatedOrder);
            assertEquals(1, updatedOrder.getOrderItems().size());
            assertEquals(50000.0, updatedOrder.getTotalAmount());
            assertEquals(2, updatedOrder.getTotalItems());
            
            OrderItem orderItem = updatedOrder.getOrderItems().get(0);
            assertEquals(item.getId(), orderItem.getFoodItem().getId());
            assertEquals(2, orderItem.getQuantity());
            assertEquals(25000.0, orderItem.getPrice());
        }
        
        @Test
        @DisplayName("Should throw exception when adding item from different restaurant")
        void addItemToCart_DifferentRestaurant_ThrowsException() {
            // Given
            Restaurant restaurant1 = createAndSaveRestaurant();
            Restaurant restaurant2 = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant2, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant1.getId(), "123 Main St", "09123456789");
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.addItemToCart(order.getId(), item.getId(), 2)
            );
            assertEquals("Item does not belong to the order's restaurant", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when adding unavailable item")
        void addItemToCart_UnavailableItem_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            item.setAvailable(false);
            itemRepository.save(item);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.addItemToCart(order.getId(), item.getId(), 2)
            );
            assertEquals("Item is not available: Pizza", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void addItemToCart_InsufficientStock_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 3);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.addItemToCart(order.getId(), item.getId(), 5)
            );
            assertEquals("Insufficient stock for item: Pizza. Available: 3, Requested: 5", exception.getMessage());
        }
    }
    
    // ==================== REMOVE ITEM TESTS ====================
    
    @Nested
    @DisplayName("Remove Item from Cart Tests")
    class RemoveItemTests {
        
        @Test
        @DisplayName("Should remove item from cart successfully")
        void removeItemFromCart_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            FoodItem burger = createAndSaveFoodItem(restaurant, "Burger", 15000.0, 5);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            orderService.addItemToCart(order.getId(), pizza.getId(), 2);
            orderService.addItemToCart(order.getId(), burger.getId(), 1);
            
            // When
            Order updatedOrder = orderService.removeItemFromCart(order.getId(), pizza.getId());
            
            // Then
            assertEquals(1, updatedOrder.getOrderItems().size());
            assertEquals(15000.0, updatedOrder.getTotalAmount());
            assertEquals(1, updatedOrder.getTotalItems());
            
            OrderItem remainingItem = updatedOrder.getOrderItems().get(0);
            assertEquals(burger.getId(), remainingItem.getFoodItem().getId());
        }
        
        @Test
        @DisplayName("Should handle removing non-existent item gracefully")
        void removeItemFromCart_NonExistentItem_HandlesGracefully() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // When & Then - Should not throw exception for non-existent item in cart
            assertDoesNotThrow(() -> {
                FoodItem nonExistentItem = createAndSaveFoodItem(restaurant, "Burger", 15000.0, 5);
                orderService.removeItemFromCart(order.getId(), nonExistentItem.getId());
            });
        }
        
        @Test
        @DisplayName("Should throw exception for null order ID")
        void removeItemFromCart_NullOrderId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.removeItemFromCart(null, 1L)
            );
            assertEquals("Order ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for null item ID")
        void removeItemFromCart_NullItemId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.removeItemFromCart(1L, null)
            );
            assertEquals("Item ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent order")
        void removeItemFromCart_NonExistentOrder_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                orderService.removeItemFromCart(999L, 1L)
            );
            assertEquals("Order not found with id=999", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent food item")
        void removeItemFromCart_NonExistentFoodItem_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                orderService.removeItemFromCart(order.getId(), 999L)
            );
            assertEquals("Food item not found with id=999", exception.getMessage());
        }
        
        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"})
        @DisplayName("Should throw exception when modifying non-pending order")
        void removeItemFromCart_NonPendingOrder_ThrowsException(OrderStatus status) {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // Change order status
            order.setStatus(status);
            orderRepository.save(order);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.removeItemFromCart(order.getId(), item.getId())
            );
            assertEquals("Cannot modify order with status: " + status, exception.getMessage());
        }
    }
    
    // ==================== UPDATE QUANTITY TESTS ====================
    
    @Nested
    @DisplayName("Update Item Quantity Tests")
    class UpdateQuantityTests {
        
        @Test
        @DisplayName("Should update item quantity successfully")
        void updateItemQuantity_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // When
            Order updatedOrder = orderService.updateItemQuantity(order.getId(), item.getId(), 5);
            
            // Then
            assertEquals(1, updatedOrder.getOrderItems().size());
            assertEquals(125000.0, updatedOrder.getTotalAmount()); // 5 * 25000
            assertEquals(5, updatedOrder.getTotalItems());
            
            OrderItem orderItem = updatedOrder.getOrderItems().get(0);
            assertEquals(5, orderItem.getQuantity());
        }
        
        @Test
        @DisplayName("Should remove item when quantity is zero")
        void updateItemQuantity_ZeroQuantity_RemovesItem() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            FoodItem burger = createAndSaveFoodItem(restaurant, "Burger", 15000.0, 5);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            orderService.addItemToCart(order.getId(), pizza.getId(), 2);
            orderService.addItemToCart(order.getId(), burger.getId(), 1);
            
            // When
            Order updatedOrder = orderService.updateItemQuantity(order.getId(), pizza.getId(), 0);
            
            // Then
            assertEquals(1, updatedOrder.getOrderItems().size());
            assertEquals(15000.0, updatedOrder.getTotalAmount());
            assertEquals(1, updatedOrder.getTotalItems());
            
            OrderItem remainingItem = updatedOrder.getOrderItems().get(0);
            assertEquals(burger.getId(), remainingItem.getFoodItem().getId());
        }
        
        @Test
        @DisplayName("Should remove item when quantity is negative")
        void updateItemQuantity_NegativeQuantity_RemovesItem() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // When
            Order updatedOrder = orderService.updateItemQuantity(order.getId(), item.getId(), -1);
            
            // Then
            assertTrue(updatedOrder.getOrderItems().isEmpty());
            assertEquals(0.0, updatedOrder.getTotalAmount());
            assertEquals(0, updatedOrder.getTotalItems());
        }
    }
    
    // ==================== PLACE ORDER TESTS ====================
    
    @Nested
    @DisplayName("Place Order Tests")
    class PlaceOrderTests {
        
        @Test
        @DisplayName("Should place order successfully")
        void placeOrder_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // When
            Order placedOrder = orderService.placeOrder(order.getId());
            
            // Then
            assertEquals(OrderStatus.CONFIRMED, placedOrder.getStatus());
            assertNotNull(placedOrder.getEstimatedDeliveryTime());
            assertTrue(placedOrder.getEstimatedDeliveryTime().isAfter(LocalDateTime.now().plusMinutes(25)));
            assertTrue(placedOrder.getEstimatedDeliveryTime().isBefore(LocalDateTime.now().plusMinutes(65)));
            
            // Verify inventory was decreased
            FoodItem updatedItem = itemRepository.findById(item.getId()).orElseThrow();
            assertEquals(8, updatedItem.getQuantity()); // 10 - 2
        }
        
        @Test
        @DisplayName("Should throw exception for null order ID")
        void placeOrder_NullOrderId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(null)
            );
            assertEquals("Order ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent order")
        void placeOrder_NonExistentOrder_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                orderService.placeOrder(999L)
            );
            assertEquals("Order not found with id=999", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for empty order")
        void placeOrder_EmptyOrder_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(order.getId())
            );
            assertEquals("Cannot place empty order", exception.getMessage());
        }
        
        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"CONFIRMED", "PREPARING", "READY", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"})
        @DisplayName("Should throw exception for non-pending order")
        void placeOrder_NonPendingOrder_ThrowsException(OrderStatus status) {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // Change order status
            order.setStatus(status);
            orderRepository.save(order);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(order.getId())
            );
            assertEquals("Order cannot be placed. Current status: " + status, exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when item becomes unavailable before placing order")
        void placeOrder_ItemBecomesUnavailable_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // Make item unavailable
            item.setAvailable(false);
            itemRepository.save(item);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(order.getId())
            );
            assertEquals("Item is no longer available: Pizza", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception when item stock becomes insufficient")
        void placeOrder_InsufficientStock_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 5);
            
            // Reduce stock
            item.setQuantity(3);
            itemRepository.save(item);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(order.getId())
            );
            assertEquals("Insufficient stock for item: Pizza. Available: 3, Ordered: 5", exception.getMessage());
        }
    }
    
    // ==================== CANCEL ORDER TESTS ====================
    
    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {
        
        @Test
        @DisplayName("Should cancel pending order successfully")
        void cancelOrder_PendingOrder_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            
            // When
            Order cancelledOrder = orderService.cancelOrder(order.getId(), "Customer requested cancellation");
            
            // Then
            assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
            assertEquals("Customer requested cancellation", cancelledOrder.getNotes());
        }
        
        @Test
        @DisplayName("Should cancel confirmed order and restore inventory")
        void cancelOrder_ConfirmedOrder_RestoresInventory() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            orderService.placeOrder(order.getId()); // This decreases inventory to 8
            
            // When
            Order cancelledOrder = orderService.cancelOrder(order.getId(), "Restaurant cancelled");
            
            // Then
            assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
            assertEquals("Restaurant cancelled", cancelledOrder.getNotes());
            
            // Verify inventory was restored
            FoodItem updatedItem = itemRepository.findById(item.getId()).orElseThrow();
            assertEquals(10, updatedItem.getQuantity()); // Back to original 10
        }
        
        @Test
        @DisplayName("Should throw exception for null order ID")
        void cancelOrder_NullOrderId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.cancelOrder(null, "reason")
            );
            assertEquals("Order ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent order")
        void cancelOrder_NonExistentOrder_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                orderService.cancelOrder(999L, "reason")
            );
            assertEquals("Order not found with id=999", exception.getMessage());
        }
    }
    
    // ==================== ORDER RETRIEVAL TESTS ====================
    
    @Nested
    @DisplayName("Order Retrieval Tests")
    class OrderRetrievalTests {
        
        @Test
        @DisplayName("Should get order by ID successfully")
        void getOrder_ValidId_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            
            // When
            Order retrievedOrder = orderService.getOrder(order.getId());
            
            // Then
            assertNotNull(retrievedOrder);
            assertEquals(order.getId(), retrievedOrder.getId());
            assertEquals(order.getCustomer().getId(), retrievedOrder.getCustomer().getId());
            assertEquals(order.getRestaurant().getId(), retrievedOrder.getRestaurant().getId());
        }
        
        @Test
        @DisplayName("Should throw exception for null order ID")
        void getOrder_NullId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getOrder(null)
            );
            assertEquals("Order ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent order")
        void getOrder_NonExistentId_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                orderService.getOrder(999L)
            );
            assertEquals("Order not found with id=999", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should get customer orders successfully")
        void getCustomerOrders_ValidCustomerId_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            Order order1 = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            Order order2 = orderService.createOrder(1L, restaurant.getId(), "456 Oak Ave", "09987654321");
            Order order3 = orderService.createOrder(2L, restaurant.getId(), "789 Pine St", "09111222333");
            
            // When
            List<Order> customerOrders = orderService.getCustomerOrders(1L);
            
            // Then
            assertEquals(2, customerOrders.size());
            assertTrue(customerOrders.stream().allMatch(o -> o.getCustomer().getId().equals(1L)));
        }
        
        @Test
        @DisplayName("Should throw exception for null customer ID")
        void getCustomerOrders_NullCustomerId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getCustomerOrders(null)
            );
            assertEquals("Customer ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should get restaurant orders successfully")
        void getRestaurantOrders_ValidRestaurantId_Success() {
            // Given
            Restaurant restaurant1 = createAndSaveRestaurant();
            Restaurant restaurant2 = createAndSaveRestaurant();
            Order order1 = orderService.createOrder(1L, restaurant1.getId(), "123 Main St", "09123456789");
            Order order2 = orderService.createOrder(2L, restaurant1.getId(), "456 Oak Ave", "09987654321");
            Order order3 = orderService.createOrder(3L, restaurant2.getId(), "789 Pine St", "09111222333");
            
            // When
            List<Order> restaurantOrders = orderService.getRestaurantOrders(restaurant1.getId());
            
            // Then
            assertEquals(2, restaurantOrders.size());
            assertTrue(restaurantOrders.stream().allMatch(o -> o.getRestaurant().getId().equals(restaurant1.getId())));
        }
        
        @Test
        @DisplayName("Should throw exception for null restaurant ID")
        void getRestaurantOrders_NullRestaurantId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getRestaurantOrders(null)
            );
            assertEquals("Restaurant ID cannot be null", exception.getMessage());
        }
    }
    
    // ==================== ORDER STATISTICS TESTS ====================
    
    @Nested
    @DisplayName("Order Statistics Tests")
    class OrderStatisticsTests {
        
        @Test
        @DisplayName("Should calculate customer order statistics correctly")
        void getCustomerOrderStatistics_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            
            // Create and place some orders
            Order order1 = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order1.getId(), item.getId(), 2);
            orderService.placeOrder(order1.getId());
            orderService.updateOrderStatus(order1.getId(), OrderStatus.PREPARING);
            orderService.updateOrderStatus(order1.getId(), OrderStatus.READY);
            orderService.updateOrderStatus(order1.getId(), OrderStatus.OUT_FOR_DELIVERY);
            orderService.updateOrderStatus(order1.getId(), OrderStatus.DELIVERED);
            
            Order order2 = orderService.createOrder(1L, restaurant.getId(), "456 Oak Ave", "09987654321");
            orderService.addItemToCart(order2.getId(), item.getId(), 1);
            orderService.placeOrder(order2.getId());
            orderService.cancelOrder(order2.getId(), "Customer cancelled");
            
            Order order3 = orderService.createOrder(1L, restaurant.getId(), "789 Pine St", "09111222333");
            orderService.addItemToCart(order3.getId(), item.getId(), 3);
            orderService.placeOrder(order3.getId());
            orderService.updateOrderStatus(order3.getId(), OrderStatus.PREPARING);
            orderService.updateOrderStatus(order3.getId(), OrderStatus.READY);
            orderService.updateOrderStatus(order3.getId(), OrderStatus.OUT_FOR_DELIVERY);
            orderService.updateOrderStatus(order3.getId(), OrderStatus.DELIVERED);
            
            // When
            OrderService.OrderStatistics stats = orderService.getCustomerOrderStatistics(1L);
            
            // Then
            assertEquals(3, stats.getTotalOrders());
            assertEquals(2, stats.getCompletedOrders());
            assertEquals(1, stats.getCancelledOrders());
            assertEquals(0, stats.getActiveOrders()); // 3 - 2 - 1 = 0
            assertEquals(125000.0, stats.getTotalSpent()); // 50000 + 75000
            assertEquals(62500.0, stats.getAverageOrderValue()); // 125000 / 2
        }
        
        @Test
        @DisplayName("Should handle customer with no orders")
        void getCustomerOrderStatistics_NoOrders_Success() {
            // When
            OrderService.OrderStatistics stats = orderService.getCustomerOrderStatistics(999L);
            
            // Then
            assertEquals(0, stats.getTotalOrders());
            assertEquals(0, stats.getCompletedOrders());
            assertEquals(0, stats.getCancelledOrders());
            assertEquals(0, stats.getActiveOrders());
            assertEquals(0.0, stats.getTotalSpent());
            assertEquals(0.0, stats.getAverageOrderValue());
        }
        
        @Test
        @DisplayName("Should handle customer with only cancelled orders")
        void getCustomerOrderStatistics_OnlyCancelledOrders_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            
            Order order1 = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order1.getId(), item.getId(), 2);
            orderService.cancelOrder(order1.getId(), "Customer cancelled");
            
            Order order2 = orderService.createOrder(1L, restaurant.getId(), "456 Oak Ave", "09987654321");
            orderService.addItemToCart(order2.getId(), item.getId(), 1);
            orderService.placeOrder(order2.getId());
            orderService.cancelOrder(order2.getId(), "Restaurant cancelled");
            
            // When
            OrderService.OrderStatistics stats = orderService.getCustomerOrderStatistics(1L);
            
            // Then
            assertEquals(2, stats.getTotalOrders());
            assertEquals(0, stats.getCompletedOrders());
            assertEquals(2, stats.getCancelledOrders());
            assertEquals(0, stats.getActiveOrders());
            assertEquals(0.0, stats.getTotalSpent());
            assertEquals(0.0, stats.getAverageOrderValue());
        }
        
        @Test
        @DisplayName("Should throw exception for null customer ID")
        void getCustomerOrderStatistics_NullCustomerId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getCustomerOrderStatistics(null)
            );
            assertEquals("Customer ID cannot be null", exception.getMessage());
        }
    }
    
    // ==================== STATUS MANAGEMENT TESTS ====================
    
    @Nested
    @DisplayName("Status Management Tests")
    class StatusManagementTests {
        
        @Test
        @DisplayName("Should update order status successfully")
        void updateOrderStatus_ValidTransition_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            orderService.placeOrder(order.getId());
            
            // When
            Order updatedOrder = orderService.updateOrderStatus(order.getId(), OrderStatus.PREPARING);
            
            // Then
            assertEquals(OrderStatus.PREPARING, updatedOrder.getStatus());
        }
        
        @Test
        @DisplayName("Should handle delivery status update with timestamp")
        void updateOrderStatus_DeliveredStatus_SetsTimestamp() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            orderService.placeOrder(order.getId());
            orderService.updateOrderStatus(order.getId(), OrderStatus.PREPARING);
            orderService.updateOrderStatus(order.getId(), OrderStatus.READY);
            orderService.updateOrderStatus(order.getId(), OrderStatus.OUT_FOR_DELIVERY);
            
            LocalDateTime beforeDelivery = LocalDateTime.now();
            
            // When
            Order deliveredOrder = orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED);
            
            // Then
            assertEquals(OrderStatus.DELIVERED, deliveredOrder.getStatus());
            assertNotNull(deliveredOrder.getActualDeliveryTime());
            assertTrue(deliveredOrder.getActualDeliveryTime().isAfter(beforeDelivery.minusSeconds(1)));
            assertTrue(deliveredOrder.getActualDeliveryTime().isBefore(LocalDateTime.now().plusSeconds(1)));
        }
        
        @Test
        @DisplayName("Should throw exception for null order ID")
        void updateOrderStatus_NullOrderId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrderStatus(null, OrderStatus.PREPARING)
            );
            assertEquals("Order ID cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for null status")
        void updateOrderStatus_NullStatus_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrderStatus(1L, null)
            );
            assertEquals("Status cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for invalid status transition")
        void updateOrderStatus_InvalidTransition_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order.getId(), item.getId(), 2);
            orderService.placeOrder(order.getId());
            
            // When & Then - Try to go from CONFIRMED directly to DELIVERED
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED)
            );
            assertEquals("Invalid status transition from CONFIRMED to DELIVERED", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should get orders by status successfully")
        void getOrdersByStatus_ValidStatus_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            
            Order order1 = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order1.getId(), item.getId(), 2);
            orderService.placeOrder(order1.getId());
            
            Order order2 = orderService.createOrder(2L, restaurant.getId(), "456 Oak Ave", "09987654321");
            orderService.addItemToCart(order2.getId(), item.getId(), 1);
            orderService.placeOrder(order2.getId());
            orderService.updateOrderStatus(order2.getId(), OrderStatus.PREPARING);
            
            Order order3 = orderService.createOrder(3L, restaurant.getId(), "789 Pine St", "09111222333");
            orderService.addItemToCart(order3.getId(), item.getId(), 3);
            orderService.placeOrder(order3.getId());
            
            // When
            List<Order> confirmedOrders = orderService.getOrdersByStatus(OrderStatus.CONFIRMED);
            List<Order> preparingOrders = orderService.getOrdersByStatus(OrderStatus.PREPARING);
            
            // Then
            assertEquals(2, confirmedOrders.size());
            assertEquals(1, preparingOrders.size());
            assertTrue(confirmedOrders.stream().allMatch(o -> o.getStatus() == OrderStatus.CONFIRMED));
            assertTrue(preparingOrders.stream().allMatch(o -> o.getStatus() == OrderStatus.PREPARING));
        }
        
        @Test
        @DisplayName("Should throw exception for null status in getOrdersByStatus")
        void getOrdersByStatus_NullStatus_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderService.getOrdersByStatus(null)
            );
            assertEquals("Status cannot be null", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should get active orders successfully")
        void getActiveOrders_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            
            Order order1 = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order1.getId(), item.getId(), 2);
            orderService.placeOrder(order1.getId());
            
            Order order2 = orderService.createOrder(2L, restaurant.getId(), "456 Oak Ave", "09987654321");
            orderService.addItemToCart(order2.getId(), item.getId(), 1);
            orderService.placeOrder(order2.getId());
            orderService.updateOrderStatus(order2.getId(), OrderStatus.PREPARING);
            orderService.updateOrderStatus(order2.getId(), OrderStatus.READY);
            orderService.updateOrderStatus(order2.getId(), OrderStatus.OUT_FOR_DELIVERY);
            orderService.updateOrderStatus(order2.getId(), OrderStatus.DELIVERED);
            
            Order order3 = orderService.createOrder(3L, restaurant.getId(), "789 Pine St", "09111222333");
            orderService.addItemToCart(order3.getId(), item.getId(), 3);
            orderService.cancelOrder(order3.getId(), "Cancelled");
            
            // When
            List<Order> activeOrders = orderService.getActiveOrders();
            
            // Then
            assertEquals(1, activeOrders.size());
            assertEquals(OrderStatus.CONFIRMED, activeOrders.get(0).getStatus());
        }
        
        @Test
        @DisplayName("Should get pending orders successfully")
        void getPendingOrders_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            
            Order order1 = orderService.createOrder(1L, restaurant.getId(), "123 Main St", "09123456789");
            orderService.addItemToCart(order1.getId(), item.getId(), 2);
            
            Order order2 = orderService.createOrder(2L, restaurant.getId(), "456 Oak Ave", "09987654321");
            orderService.addItemToCart(order2.getId(), item.getId(), 1);
            orderService.placeOrder(order2.getId());
            
            Order order3 = orderService.createOrder(3L, restaurant.getId(), "789 Pine St", "09111222333");
            
            // When
            List<Order> pendingOrders = orderService.getPendingOrders();
            
            // Then
            assertEquals(2, pendingOrders.size());
            assertTrue(pendingOrders.stream().allMatch(o -> o.getStatus() == OrderStatus.PENDING));
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
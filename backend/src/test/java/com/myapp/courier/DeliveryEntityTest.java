package com.myapp.courier;

import com.myapp.common.models.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Delivery Entity Tests")
class DeliveryEntityTest {

    private User customer;
    private User courier;
    private Restaurant restaurant;
    private com.myapp.common.models.Order order; // Fully qualified to avoid ambiguity
    private FoodItem foodItem;

    @BeforeEach
    void setUp() {
        // Create test customer - Constructor: (id, fullName, phone, email, passwordHash, role, address)
        customer = new User(1L, "John Doe", "1234567890", "john@example.com", 
                           "hashedpass", User.Role.BUYER, "123 Main St");
        
        // Create test courier - Constructor: (id, fullName, phone, email, passwordHash, role, address)  
        courier = new User(2L, "Mike Wilson", "0987654321", "mike@example.com", 
                          "hashedpass", User.Role.COURIER, "456 Oak Ave");
        
        // Create test restaurant - Constructor: (id, ownerId, name, address, phone, status)
        restaurant = new Restaurant(1L, 3L, "Test Restaurant", "789 Food St", "5551234567", RestaurantStatus.APPROVED);
        
        // Create test food item - Constructor: (id, name, description, price, category, imageUrl, quantity, keywords, restaurant)
        foodItem = new FoodItem(1L, "Test Burger", "Delicious test burger", 
                               15.99, "Main Course", "burger.jpg", 10, "burger delicious", restaurant);
        
        // Create test order
        order = com.myapp.common.models.Order.createNew(customer, restaurant, "123 Main St", "1234567890");
        order.setId(1L);
        order.addItem(foodItem, 2);
        order.setStatus(OrderStatus.READY);
    }

    @Nested
    @DisplayName("Delivery Creation Tests")
    class DeliveryCreationTests {

        @Test
        @DisplayName("Should create delivery with valid parameters")
        void createDelivery_ValidParameters_Success() {
            // Act
            Delivery delivery = Delivery.createNew(order, 5.99);

            // Assert
            assertNotNull(delivery);
            assertEquals(order, delivery.getOrder());
            assertEquals(5.99, delivery.getDeliveryFee());
            assertEquals(DeliveryStatus.PENDING, delivery.getStatus());
            assertNull(delivery.getCourier());
            assertNotNull(delivery.getEstimatedPickupTime());
            assertNotNull(delivery.getEstimatedDeliveryTime());
        }

        @Test
        @DisplayName("Should create delivery with distance")
        void createDeliveryWithDistance_ValidParameters_Success() {
            // Act
            Delivery delivery = Delivery.createWithDistance(order, 7.50, 5.2);

            // Assert
            assertNotNull(delivery);
            assertEquals(order, delivery.getOrder());
            assertEquals(7.50, delivery.getDeliveryFee());
            assertEquals(5.2, delivery.getDistanceKm());
            assertEquals(DeliveryStatus.PENDING, delivery.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when order is null")
        void createDelivery_NullOrder_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Delivery.createNew(null, 5.99)
            );
            assertEquals("Order is required", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when delivery fee is negative")
        void createDelivery_NegativeFee_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Delivery.createNew(order, -1.0)
            );
            assertEquals("Delivery fee must be non-negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when delivery fee is null")
        void createDelivery_NullFee_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Delivery.createNew(order, null)
            );
            assertEquals("Delivery fee must be non-negative", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Courier Assignment Tests")
    class CourierAssignmentTests {

        private Delivery delivery;

        @BeforeEach
        void setUp() {
            delivery = Delivery.createNew(order, 5.99);
        }

        @Test
        @DisplayName("Should assign courier successfully")
        void assignToCourier_ValidCourier_Success() {
            // Act
            delivery.assignToCourier(courier);

            // Assert
            assertEquals(courier, delivery.getCourier());
            assertEquals(DeliveryStatus.ASSIGNED, delivery.getStatus());
            assertNotNull(delivery.getAssignedAt());
            assertNotNull(delivery.getEstimatedPickupTime());
        }

        @Test
        @DisplayName("Should throw exception when courier is null")
        void assignToCourier_NullCourier_ThrowsException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> delivery.assignToCourier(null)
            );
            assertEquals("Courier cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when user is not courier")
        void assignToCourier_NotCourierRole_ThrowsException() {
            // Arrange - Constructor: (id, fullName, phone, email, passwordHash, role, address)
            User nonCourier = new User(3L, "Not Courier", "1111111111", "not@courier.com", 
                                     "pass", User.Role.BUYER, "Not Courier St");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> delivery.assignToCourier(nonCourier)
            );
            assertEquals("User must have COURIER role", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when delivery is not pending")
        void assignToCourier_NotPendingStatus_ThrowsException() {
            // Arrange
            delivery.assignToCourier(courier); // Status becomes ASSIGNED

            // Constructor: (id, fullName, phone, email, passwordHash, role, address)
            User anotherCourier = new User(4L, "Another Courier", "2222222222", "another@courier.com", 
                                         "pass", User.Role.COURIER, "Another St");

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> delivery.assignToCourier(anotherCourier)
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
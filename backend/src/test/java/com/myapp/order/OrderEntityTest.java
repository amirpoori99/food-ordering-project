package com.myapp.order;

import com.myapp.common.models.*;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Entity Tests")
class OrderEntityTest {

    @BeforeEach
    void setup() {
        // Clean database before each test
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from OrderItem").executeUpdate();
            session.createQuery("delete from com.myapp.common.models.Order").executeUpdate();
            session.createQuery("delete from FoodItem").executeUpdate();
            session.createQuery("delete from Restaurant").executeUpdate();
            session.createQuery("delete from User").executeUpdate();
            tx.commit();
        }
    }

    @Nested
    @DisplayName("Entity Creation Tests")
    class EntityCreationTests {
        
        @Test
        @DisplayName("Order creation with valid data succeeds")
        void order_entityCreation_success() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            
            // Then
            assertThat(order.getCustomer()).isEqualTo(customer);
            assertThat(order.getRestaurant()).isEqualTo(restaurant);
            assertThat(order.getDeliveryAddress()).isEqualTo("123 Main St");
            assertThat(order.getPhone()).isEqualTo("09123456789");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotalAmount()).isEqualTo(0.0);
            assertThat(order.getOrderDate()).isNotNull();
            assertThat(order.getOrderItems()).isEmpty();
        }

        @Test
        @DisplayName("Order creation with null customer throws exception")
        void order_createNew_nullCustomer_throwsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            assertThatThrownBy(() -> Order.createNew(null, restaurant, "123 Main St", "09123456789"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Order creation with null restaurant throws exception")
        void order_createNew_nullRestaurant_throwsException() {
            // Given
            User customer = createAndSaveUser();
            
            // When & Then
            assertThatThrownBy(() -> Order.createNew(customer, null, "123 Main St", "09123456789"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Order creation with blank delivery address throws exception")
        void order_createNew_blankDeliveryAddress_throwsException(String address) {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            assertThatThrownBy(() -> Order.createNew(customer, restaurant, address, "09123456789"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Order creation with blank phone throws exception")
        void order_createNew_blankPhone_throwsException(String phone) {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            assertThatThrownBy(() -> Order.createNew(customer, restaurant, "123 Main St", phone))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Add single item to order succeeds")
        void order_addItem_singleItem_success() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            
            // When
            order.addItem(foodItem, 2);
            
            // Then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getTotalAmount()).isEqualTo(50000.0);
            assertThat(order.getTotalItems()).isEqualTo(2);
            
            OrderItem orderItem = order.getOrderItems().get(0);
            assertThat(orderItem.getFoodItem()).isEqualTo(foodItem);
            assertThat(orderItem.getQuantity()).isEqualTo(2);
            assertThat(orderItem.getPrice()).isEqualTo(25000.0);
        }

        @Test
        @DisplayName("Add multiple different items to order succeeds")
        void order_addItem_multipleItems_success() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            FoodItem burger = createAndSaveFoodItem(restaurant, "Burger", 15000.0, 5);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            
            // When
            order.addItem(pizza, 2);
            order.addItem(burger, 1);
            
            // Then
            assertThat(order.getOrderItems()).hasSize(2);
            assertThat(order.getTotalAmount()).isEqualTo(65000.0);
            assertThat(order.getTotalItems()).isEqualTo(3);
        }

        @Test
        @DisplayName("Add same item multiple times increases quantity")
        void order_addItem_sameItemTwice_increasesQuantity() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            
            // When
            order.addItem(pizza, 2);
            order.addItem(pizza, 1);
            
            // Then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getTotalAmount()).isEqualTo(75000.0);
            assertThat(order.getTotalItems()).isEqualTo(3);
            
            OrderItem orderItem = order.getOrderItems().get(0);
            assertThat(orderItem.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("Remove item from order succeeds")
        void order_removeItem_success() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            FoodItem burger = createAndSaveFoodItem(restaurant, "Burger", 15000.0, 5);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            order.addItem(pizza, 2);
            order.addItem(burger, 1);
            
            // When
            order.removeItem(pizza);
            
            // Then
            assertThat(order.getOrderItems()).hasSize(1);
            assertThat(order.getTotalAmount()).isEqualTo(15000.0);
            assertThat(order.getTotalItems()).isEqualTo(1);
            assertThat(order.getOrderItems().get(0).getFoodItem()).isEqualTo(burger);
        }

        @Test
        @DisplayName("Order confirmation succeeds with valid items")
        void order_confirm_success() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            order.addItem(foodItem, 3);
            
            int initialQuantity = foodItem.getQuantity();
            
            // When
            order.confirm();
            
            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(foodItem.getQuantity()).isEqualTo(initialQuantity - 3);
            assertThat(order.getEstimatedDeliveryTime()).isNotNull();
            assertThat(order.getEstimatedDeliveryTime()).isAfter(LocalDateTime.now().plusMinutes(25));
            assertThat(order.getEstimatedDeliveryTime()).isBefore(LocalDateTime.now().plusMinutes(35));
        }

        @Test
        @DisplayName("Order cancellation succeeds when allowed")
        void order_cancel_success() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            order.addItem(foodItem, 2);
            order.confirm();
            
            int quantityAfterConfirm = foodItem.getQuantity();
            
            // When
            order.cancel();
            
            // Then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(foodItem.getQuantity()).isEqualTo(quantityAfterConfirm + 2); // Quantity restored
        }

        @EnumSource(OrderStatus.class)
        @ParameterizedTest
        @DisplayName("Order state transitions follow business rules")
        void order_stateTransitions_followBusinessRules(OrderStatus status) {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 10);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            order.addItem(foodItem, 1);
            
            // Set order to specific status for testing
            switch (status) {
                case PENDING -> {
                    // Already in PENDING state
                    assertThat(order.canBeCancelled()).isTrue();
                }
                case CONFIRMED -> {
                    order.confirm();
                    assertThat(order.canBeCancelled()).isTrue();
                }
                case PREPARING -> {
                    order.confirm();
                    order.setStatus(OrderStatus.PREPARING);
                    assertThat(order.canBeCancelled()).isTrue();
                }
                case READY -> {
                    order.confirm();
                    order.setStatus(OrderStatus.READY);
                    assertThat(order.canBeCancelled()).isFalse();
                }
                case OUT_FOR_DELIVERY -> {
                    order.confirm();
                    order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
                    assertThat(order.canBeCancelled()).isFalse();
                }
                case DELIVERED -> {
                    order.confirm();
                    order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
                    order.markAsDelivered();
                    assertThat(order.canBeCancelled()).isFalse();
                }
                case CANCELLED -> {
                    order.confirm();
                    order.cancel();
                    assertThat(order.canBeCancelled()).isFalse();
                }
            }
        }
    }

    @Nested
    @DisplayName("Error Conditions Tests")
    class ErrorConditionsTests {
        
        @Test
        @DisplayName("Add item with null food item throws exception")
        void order_addItem_nullFoodItem_throwsException() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            
            // When & Then
            assertThatThrownBy(() -> order.addItem(null, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Food item and quantity must be valid");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, -100})
        @DisplayName("Add item with invalid quantity throws exception")
        void order_addItem_invalidQuantity_throwsException(int quantity) {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = createAndSaveFoodItem(restaurant, "Pizza", 25000.0, 5);
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            
            // When & Then
            assertThatThrownBy(() -> order.addItem(foodItem, quantity))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Food item and quantity must be valid");
        }

        @Test
        @DisplayName("Confirm empty order throws exception")
        void order_confirm_emptyOrder_throwsException() {
            // Given
            User customer = createAndSaveUser();
            Restaurant restaurant = createAndSaveRestaurant();
            Order order = Order.createNew(customer, restaurant, "123 Main St", "09123456789");
            
            // When & Then
            assertThatThrownBy(() -> order.confirm())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Cannot confirm empty order");
        }
    }

    // Helper methods
    private User createAndSaveUser() {
        User user = User.forRegistration("John Doe", "09123456789", "john@example.com", "hashedPassword", "Tehran");
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        }
        return user;
    }

    private Restaurant createAndSaveRestaurant() {
        Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
        restaurant.setStatus(RestaurantStatus.APPROVED);
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(restaurant);
            tx.commit();
        }
        return restaurant;
    }

    private FoodItem createAndSaveFoodItem(Restaurant restaurant, String name, Double price, Integer quantity) {
        FoodItem foodItem = FoodItem.forMenu(name, "Delicious " + name, price, "Fast Food", restaurant);
        foodItem.setQuantity(quantity);
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(foodItem);
            tx.commit();
        }
        return foodItem;
    }
}
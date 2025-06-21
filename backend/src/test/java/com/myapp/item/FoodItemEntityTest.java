package com.myapp.item;

import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FoodItem Entity Tests")
class FoodItemEntityTest {

    @BeforeEach
    void setup() {
        // Clean database before each test
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from FoodItem").executeUpdate();
            session.createQuery("delete from Restaurant").executeUpdate();
            tx.commit();
        }
    }

    @Nested
    @DisplayName("Entity Creation Tests")
    class EntityCreationTests {
        
        @Test
        @DisplayName("FoodItem creation with valid data succeeds")
        void foodItem_entityCreation_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // When
            FoodItem foodItem = FoodItem.forMenu("Pizza Margherita", "Delicious pizza", 25000.0, "Italian", restaurant);
            
            // Then
            assertThat(foodItem.getName()).isEqualTo("Pizza Margherita");
            assertThat(foodItem.getDescription()).isEqualTo("Delicious pizza");
            assertThat(foodItem.getPrice()).isEqualTo(25000.0);
            assertThat(foodItem.getCategory()).isEqualTo("Italian");
            assertThat(foodItem.getRestaurant()).isEqualTo(restaurant);
            assertThat(foodItem.getQuantity()).isEqualTo(1);
            assertThat(foodItem.getAvailable()).isTrue();
            assertThat(foodItem.getImageUrl()).isNull();
            assertThat(foodItem.getKeywords()).isEqualTo("Pizza Margherita");
            assertThat(foodItem.getId()).isNull(); // Not persisted yet
        }

        @Test
        @DisplayName("FoodItem creation with image URL succeeds")
        void foodItem_forMenuWithImage_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            String imageUrl = "https://example.com/pizza.jpg";
            
            // When
            FoodItem foodItem = FoodItem.forMenuWithImage("Pizza Margherita", "Delicious pizza", 
                                                        25000.0, "Italian", imageUrl, restaurant);
            
            // Then
            assertThat(foodItem.getName()).isEqualTo("Pizza Margherita");
            assertThat(foodItem.getDescription()).isEqualTo("Delicious pizza");
            assertThat(foodItem.getPrice()).isEqualTo(25000.0);
            assertThat(foodItem.getCategory()).isEqualTo("Italian");
            assertThat(foodItem.getRestaurant()).isEqualTo(restaurant);
            assertThat(foodItem.getImageUrl()).isEqualTo(imageUrl);
            assertThat(foodItem.getQuantity()).isEqualTo(1);
            assertThat(foodItem.getAvailable()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("FoodItem creation with blank name throws exception")
        void foodItem_forMenu_blankName_throwsException(String name) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // When & Then
            assertThatThrownBy(() -> FoodItem.forMenu(name, "Description", 25000.0, "Italian", restaurant))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name, price, category and restaurant are required");
        }

        @Test
        @DisplayName("FoodItem creation with null restaurant throws exception")
        void foodItem_forMenu_nullRestaurant_throwsException() {
            // When & Then
            assertThatThrownBy(() -> FoodItem.forMenu("Pizza", "Description", 25000.0, "Italian", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name, price, category and restaurant are required");
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0, -100.0, -0.01})
        @DisplayName("FoodItem creation with invalid price throws exception")
        void foodItem_forMenu_invalidPrice_throwsException(double price) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // When & Then
            assertThatThrownBy(() -> FoodItem.forMenu("Pizza", "Description", price, "Italian", restaurant))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name, price, category and restaurant are required");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Stock availability check works correctly")
        void foodItem_isInStock_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            
            // When & Then - Available with quantity
            foodItem.setQuantity(5);
            foodItem.setAvailable(true);
            assertThat(foodItem.isInStock()).isTrue();
            
            // When & Then - Available but no quantity
            foodItem.setQuantity(0);
            foodItem.setAvailable(true);
            assertThat(foodItem.isInStock()).isFalse();
            
            // When & Then - Quantity available but not available
            foodItem.setQuantity(5);
            foodItem.setAvailable(false);
            assertThat(foodItem.isInStock()).isFalse();
            
            // When & Then - Neither available nor quantity
            foodItem.setQuantity(0);
            foodItem.setAvailable(false);
            assertThat(foodItem.isInStock()).isFalse();
        }

        @Test
        @DisplayName("Quantity decrease works correctly")
        void foodItem_decreaseQuantity_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            foodItem.setQuantity(10);
            
            // When
            foodItem.decreaseQuantity(3);
            
            // Then
            assertThat(foodItem.getQuantity()).isEqualTo(7);
            assertThat(foodItem.isInStock()).isTrue();
        }

        @Test
        @DisplayName("Quantity decrease exceeding available throws exception")
        void foodItem_decreaseQuantity_exceedsAvailable_throwsException() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            foodItem.setQuantity(5);
            
            // When & Then
            assertThatThrownBy(() -> foodItem.decreaseQuantity(10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Not enough quantity available");
        }

        @Test
        @DisplayName("Quantity increase works correctly")
        void foodItem_increaseQuantity_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            foodItem.setQuantity(3);
            
            // When
            foodItem.increaseQuantity(7);
            
            // Then
            assertThat(foodItem.getQuantity()).isEqualTo(10);
            assertThat(foodItem.isInStock()).isTrue();
        }
    }

    @Nested
    @DisplayName("Persistence Tests")
    class PersistenceTests {
        
        @Test
        @DisplayName("FoodItem persistence succeeds")
        void foodItem_persistence_success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = FoodItem.forMenu("Burger", "Beef burger with cheese", 
                                               20000.0, "Fast Food", restaurant);
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(foodItem);
                tx.commit();
            }
            
            // Then
            assertThat(foodItem.getId()).isNotNull();
            assertThat(foodItem.getId()).isPositive();
            
            // Verify persistence
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                FoodItem retrieved = session.get(FoodItem.class, foodItem.getId());
                assertThat(retrieved).isNotNull();
                assertThat(retrieved.getName()).isEqualTo("Burger");
                assertThat(retrieved.getPrice()).isEqualTo(20000.0);
                assertThat(retrieved.getRestaurant().getId()).isEqualTo(restaurant.getId());
            }
        }
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
}
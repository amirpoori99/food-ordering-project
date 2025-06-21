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

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("FoodItem creation with blank category throws exception")
        void foodItem_forMenu_blankCategory_throwsException(String category) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // When & Then
            assertThatThrownBy(() -> FoodItem.forMenu("Pizza", "Description", 25000.0, category, restaurant))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name, price, category and restaurant are required");
        }

        @Test
        @DisplayName("FoodItem creation with null description succeeds")
        void foodItem_forMenu_nullDescription_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // When
            FoodItem foodItem = FoodItem.forMenu("Pizza", null, 25000.0, "Italian", restaurant);
            
            // Then
            assertThat(foodItem.getDescription()).isNull();
            assertThat(foodItem.getName()).isEqualTo("Pizza");
        }

        @Test
        @DisplayName("FoodItem creation with Persian characters")
        void foodItem_forMenu_persianCharacters_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "رستوران ایرانی", "تهران", "021-123");
            
            // When
            FoodItem foodItem = FoodItem.forMenu("کباب کوبیده", "کباب کوبیده با برنج", 35000.0, "ایرانی", restaurant);
            
            // Then
            assertThat(foodItem.getName()).isEqualTo("کباب کوبیده");
            assertThat(foodItem.getDescription()).isEqualTo("کباب کوبیده با برنج");
            assertThat(foodItem.getCategory()).isEqualTo("ایرانی");
            assertThat(foodItem.getKeywords()).isEqualTo("کباب کوبیده");
        }

        @Test
        @DisplayName("FoodItem creation with very long data")
        void foodItem_forMenu_veryLongData_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            String longName = "A".repeat(200);
            String longDescription = "Very long description ".repeat(50);
            String longCategory = "Category".repeat(20);
            
            // When
            FoodItem foodItem = FoodItem.forMenu(longName, longDescription, 25000.0, longCategory, restaurant);
            
            // Then
            assertThat(foodItem.getName()).isEqualTo(longName);
            assertThat(foodItem.getDescription()).isEqualTo(longDescription);
            assertThat(foodItem.getCategory()).isEqualTo(longCategory);
        }

        @Test
        @DisplayName("FoodItem creation with special characters")
        void foodItem_forMenu_specialCharacters_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            String nameWithSpecialChars = "Pizza@Margherita#Special$25%";
            String descWithSpecialChars = "Description with symbols: !@#$%^&*()";
            
            // When
            FoodItem foodItem = FoodItem.forMenu(nameWithSpecialChars, descWithSpecialChars, 25000.0, "Italian", restaurant);
            
            // Then
            assertThat(foodItem.getName()).isEqualTo(nameWithSpecialChars);
            assertThat(foodItem.getDescription()).isEqualTo(descWithSpecialChars);
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.01, 1.0, 100.0, 999999.99, Double.MAX_VALUE})
        @DisplayName("FoodItem creation with valid prices")
        void foodItem_forMenu_validPrices_success(double price) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // When
            FoodItem foodItem = FoodItem.forMenu("Test Item", "Description", price, "Category", restaurant);
            
            // Then
            assertThat(foodItem.getPrice()).isEqualTo(price);
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

        @ParameterizedTest
        @ValueSource(ints = {6, 10, 100})
        @DisplayName("Quantity decrease exceeding available throws exception")
        void foodItem_decreaseQuantity_exceedsAvailable_throwsException(int amount) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            foodItem.setQuantity(5);
            
            // When & Then
            assertThatThrownBy(() -> foodItem.decreaseQuantity(amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Not enough quantity available");
        }
        
        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, -100})
        @DisplayName("Quantity decrease with non-positive amounts follows mathematical rules")
        void foodItem_decreaseQuantity_nonPositiveAmount_mathematicalBehavior(int amount) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            int originalQuantity = 5;
            foodItem.setQuantity(originalQuantity);
            
            // When
            foodItem.decreaseQuantity(amount);
            
            // Then - Mathematical behavior: quantity - amount
            // For negative amounts: 5 - (-1) = 5 + 1 = 6
            // For zero: 5 - 0 = 5
            int expectedQuantity = originalQuantity - amount;
            assertThat(foodItem.getQuantity()).isEqualTo(expectedQuantity);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, -100})
        @DisplayName("Quantity increase with non-positive amounts does not change quantity")
        void foodItem_increaseQuantity_nonPositiveAmount_noChange(int amount) {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            int originalQuantity = 5;
            foodItem.setQuantity(originalQuantity);
            
            // When
            // Based on actual implementation: increaseQuantity only increases if amount > 0
            // For non-positive amounts, nothing happens
            foodItem.increaseQuantity(amount);
            
            // Then
            assertThat(foodItem.getQuantity()).isEqualTo(originalQuantity);
        }

        @Test
        @DisplayName("Quantity operations with boundary values")
        void foodItem_quantityOperations_boundaryValues_success() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            
            // Test with maximum quantity
            foodItem.setQuantity(Integer.MAX_VALUE - 1);
            foodItem.increaseQuantity(1);
            assertThat(foodItem.getQuantity()).isEqualTo(Integer.MAX_VALUE);
            
            // Test decreasing to zero
            foodItem.setQuantity(1);
            foodItem.decreaseQuantity(1);
            assertThat(foodItem.getQuantity()).isEqualTo(0);
            assertThat(foodItem.isInStock()).isFalse();
        }

        @Test
        @DisplayName("Availability toggle affects stock status")
        void foodItem_availabilityToggle_affectsStockStatus() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 15000.0, "Italian", restaurant);
            foodItem.setQuantity(5);
            
            // Initially available
            assertThat(foodItem.getAvailable()).isTrue();
            assertThat(foodItem.isInStock()).isTrue();
            
            // Make unavailable
            foodItem.setAvailable(false);
            assertThat(foodItem.isInStock()).isFalse();
            
            // Make available again
            foodItem.setAvailable(true);
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

        @Test
        @DisplayName("FoodItem with image URL persistence")
        void foodItem_withImageUrl_persistence_success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            String imageUrl = "https://example.com/burger.jpg";
            FoodItem foodItem = FoodItem.forMenuWithImage("Burger", "Beef burger", 
                                                        20000.0, "Fast Food", imageUrl, restaurant);
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(foodItem);
                tx.commit();
            }
            
            // Then
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                FoodItem retrieved = session.get(FoodItem.class, foodItem.getId());
                assertThat(retrieved.getImageUrl()).isEqualTo(imageUrl);
            }
        }

        @Test
        @DisplayName("FoodItem with Persian data persistence")
        void foodItem_persianData_persistence_success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = FoodItem.forMenu("کباب کوبیده", "کباب کوبیده با برنج و سالاد", 
                                               35000.0, "ایرانی", restaurant);
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(foodItem);
                tx.commit();
            }
            
            // Then
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                FoodItem retrieved = session.get(FoodItem.class, foodItem.getId());
                assertThat(retrieved.getName()).isEqualTo("کباب کوبیده");
                assertThat(retrieved.getDescription()).isEqualTo("کباب کوبیده با برنج و سالاد");
                assertThat(retrieved.getCategory()).isEqualTo("ایرانی");
            }
        }

        @Test
        @DisplayName("FoodItem quantity and availability persistence")
        void foodItem_quantityAndAvailability_persistence_success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = FoodItem.forMenu("Pizza", "Margherita Pizza", 
                                               25000.0, "Italian", restaurant);
            foodItem.setQuantity(10);
            foodItem.setAvailable(false);
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(foodItem);
                tx.commit();
            }
            
            // Then
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                FoodItem retrieved = session.get(FoodItem.class, foodItem.getId());
                assertThat(retrieved.getQuantity()).isEqualTo(10);
                assertThat(retrieved.getAvailable()).isFalse();
                assertThat(retrieved.isInStock()).isFalse();
            }
        }

        @Test
        @DisplayName("Multiple FoodItems for same restaurant persistence")
        void foodItem_multipleForSameRestaurant_persistence_success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item1 = FoodItem.forMenu("Pizza", "Margherita", 25000.0, "Italian", restaurant);
            FoodItem item2 = FoodItem.forMenu("Pasta", "Carbonara", 20000.0, "Italian", restaurant);
            FoodItem item3 = FoodItem.forMenu("Salad", "Caesar", 15000.0, "Salad", restaurant);
            
            // When
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                session.persist(item1);
                session.persist(item2);
                session.persist(item3);
                tx.commit();
            }
            
            // Then
            assertThat(item1.getId()).isNotNull();
            assertThat(item2.getId()).isNotNull();
            assertThat(item3.getId()).isNotNull();
            assertThat(item1.getId()).isNotEqualTo(item2.getId());
            assertThat(item2.getId()).isNotEqualTo(item3.getId());
            
            // Verify all items belong to same restaurant
            try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
                FoodItem retrieved1 = session.get(FoodItem.class, item1.getId());
                FoodItem retrieved2 = session.get(FoodItem.class, item2.getId());
                FoodItem retrieved3 = session.get(FoodItem.class, item3.getId());
                
                assertThat(retrieved1.getRestaurant().getId()).isEqualTo(restaurant.getId());
                assertThat(retrieved2.getRestaurant().getId()).isEqualTo(restaurant.getId());
                assertThat(retrieved3.getRestaurant().getId()).isEqualTo(restaurant.getId());
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("FoodItem with extreme price values")
        void foodItem_extremePriceValues_handled() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // Test with very small price
            FoodItem cheapItem = FoodItem.forMenu("Cheap Item", "Very cheap", 0.01, "Cheap", restaurant);
            assertThat(cheapItem.getPrice()).isEqualTo(0.01);
            
            // Test with very large price
            FoodItem expensiveItem = FoodItem.forMenu("Expensive Item", "Very expensive", 999999.99, "Luxury", restaurant);
            assertThat(expensiveItem.getPrice()).isEqualTo(999999.99);
        }

        @Test
        @DisplayName("FoodItem with whitespace-only fields")
        void foodItem_whitespaceOnlyFields_handled() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // Name with only spaces should throw exception
            assertThatThrownBy(() -> FoodItem.forMenu("   ", "Description", 25000.0, "Category", restaurant))
                    .isInstanceOf(IllegalArgumentException.class);
            
            // Category with only spaces should throw exception
            assertThatThrownBy(() -> FoodItem.forMenu("Name", "Description", 25000.0, "   ", restaurant))
                    .isInstanceOf(IllegalArgumentException.class);
            
            // Description with only spaces is allowed
            FoodItem item = FoodItem.forMenu("Name", "   ", 25000.0, "Category", restaurant);
            assertThat(item.getDescription()).isEqualTo("   ");
        }

        @Test
        @DisplayName("FoodItem keywords generation")
        void foodItem_keywordsGeneration_correct() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // When
            FoodItem foodItem = FoodItem.forMenu("Pizza Margherita Special", "Description", 25000.0, "Italian", restaurant);
            
            // Then
            assertThat(foodItem.getKeywords()).isEqualTo("Pizza Margherita Special");
        }

        @Test
        @DisplayName("FoodItem with null and empty image URLs")
        void foodItem_nullEmptyImageUrls_handled() {
            // Given
            Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran", "021-123");
            
            // Null image URL
            FoodItem item1 = FoodItem.forMenuWithImage("Pizza", "Description", 25000.0, "Italian", null, restaurant);
            assertThat(item1.getImageUrl()).isNull();
            
            // Empty image URL
            FoodItem item2 = FoodItem.forMenuWithImage("Pizza", "Description", 25000.0, "Italian", "", restaurant);
            assertThat(item2.getImageUrl()).isEqualTo("");
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
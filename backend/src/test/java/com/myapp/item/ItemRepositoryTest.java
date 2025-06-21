package com.myapp.item;

import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ItemRepository Comprehensive Tests")
class ItemRepositoryTest {

    private ItemRepository repository;
    private Restaurant testRestaurant;
    private Restaurant secondRestaurant;

    @BeforeEach
    void setUp() {
        repository = new ItemRepository();
        // Clean database before each test
        repository.deleteAll();
        cleanRestaurants();
        
        // Create and save test restaurants
        testRestaurant = createAndSaveRestaurant("Test Restaurant", "Tehran");
        secondRestaurant = createAndSaveRestaurant("Second Restaurant", "Isfahan");
    }

    @Nested
    @DisplayName("Save Operations Tests")
    class SaveOperationsTests {
        
        @Test
        @DisplayName("Save new food item with all fields succeeds")
        void saveNew_validFoodItemWithAllFields_success() {
            // Given
            FoodItem foodItem = new FoodItem("Pizza Margherita", "Classic Italian pizza with tomato and mozzarella", 
                    25000.0, "Italian", "https://example.com/pizza.jpg", 10, "pizza italian cheese", testRestaurant);
            
            // When
            FoodItem saved = repository.saveNew(foodItem);
            
            // Then
            assertThat(saved.getId()).isNotNull().isPositive();
            assertThat(saved.getName()).isEqualTo("Pizza Margherita");
            assertThat(saved.getDescription()).isEqualTo("Classic Italian pizza with tomato and mozzarella");
            assertThat(saved.getPrice()).isEqualTo(25000.0);
            assertThat(saved.getCategory()).isEqualTo("Italian");
            assertThat(saved.getImageUrl()).isEqualTo("https://example.com/pizza.jpg");
            assertThat(saved.getQuantity()).isEqualTo(10);
            assertThat(saved.getKeywords()).isEqualTo("pizza italian cheese");
            assertThat(saved.getAvailable()).isTrue();
            assertThat(saved.getRestaurant().getId()).isEqualTo(testRestaurant.getId());
        }

        @Test
        @DisplayName("Save new food item using factory method succeeds")
        void saveNew_usingFactoryMethod_success() {
            // Given
            FoodItem foodItem = FoodItem.forMenu("Burger", "Beef burger with cheese", 20000.0, "Fast Food", testRestaurant);
            
            // When
            FoodItem saved = repository.saveNew(foodItem);
            
            // Then
            assertThat(saved.getId()).isNotNull().isPositive();
            assertThat(saved.getName()).isEqualTo("Burger");
            assertThat(saved.getPrice()).isEqualTo(20000.0);
            assertThat(saved.getCategory()).isEqualTo("Fast Food");
            assertThat(saved.getQuantity()).isEqualTo(1); // Default from factory
            assertThat(saved.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("Save existing food item updates correctly")
        void save_existingFoodItem_updatesCorrectly() {
            // Given
            FoodItem foodItem = FoodItem.forMenu("Pasta", "Italian pasta", 18000.0, "Italian", testRestaurant);
            FoodItem saved = repository.saveNew(foodItem);
            
            // When - Update multiple fields
            saved.setPrice(20000.0);
            saved.setDescription("Premium Italian pasta with special sauce");
            saved.setQuantity(15);
            saved.setAvailable(false);
            FoodItem updated = repository.save(saved);
            
            // Then
            assertThat(updated.getId()).isEqualTo(saved.getId());
            assertThat(updated.getPrice()).isEqualTo(20000.0);
            assertThat(updated.getDescription()).isEqualTo("Premium Italian pasta with special sauce");
            assertThat(updated.getQuantity()).isEqualTo(15);
            assertThat(updated.getAvailable()).isFalse();
        }

        @Test
        @DisplayName("Save with null ID creates new item")
        void save_nullId_createsNewItem() {
            // Given
            FoodItem foodItem = FoodItem.forMenu("Salad", "Fresh salad", 15000.0, "Healthy", testRestaurant);
            // Ensure ID is null
            foodItem.setId(null);
            
            // When
            FoodItem saved = repository.save(foodItem);
            
            // Then
            assertThat(saved.getId()).isNotNull().isPositive();
            assertThat(saved.getName()).isEqualTo("Salad");
        }
    }

    @Nested
    @DisplayName("Find Operations Tests")
    class FindOperationsTests {
        
        @Test
        @DisplayName("Find by ID returns correct item")
        void findById_existingItem_returnsCorrectItem() {
            // Given
            FoodItem pizza = FoodItem.forMenu("Pizza", "Cheese pizza", 25000.0, "Italian", testRestaurant);
            FoodItem burger = FoodItem.forMenu("Burger", "Beef burger", 20000.0, "Fast Food", testRestaurant);
            FoodItem savedPizza = repository.saveNew(pizza);
            repository.saveNew(burger);
            
            // When
            Optional<FoodItem> found = repository.findById(savedPizza.getId());
            
            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(savedPizza.getId());
            assertThat(found.get().getName()).isEqualTo("Pizza");
            assertThat(found.get().getPrice()).isEqualTo(25000.0);
        }

        @Test
        @DisplayName("Find by ID with non-existent ID returns empty")
        void findById_nonExistentId_returnsEmpty() {
            // When
            Optional<FoodItem> found = repository.findById(999L);
            
            // Then
            assertThat(found).isNotPresent();
        }

        @Test
        @DisplayName("Find by restaurant returns only restaurant items")
        void findByRestaurant_multipleRestaurants_returnsOnlyRestaurantItems() {
            // Given
            FoodItem pizza1 = FoodItem.forMenu("Pizza", "Restaurant 1 pizza", 25000.0, "Italian", testRestaurant);
            FoodItem burger1 = FoodItem.forMenu("Burger", "Restaurant 1 burger", 20000.0, "Fast Food", testRestaurant);
            FoodItem pizza2 = FoodItem.forMenu("Pizza", "Restaurant 2 pizza", 30000.0, "Italian", secondRestaurant);
            
            repository.saveNew(pizza1);
            repository.saveNew(burger1);
            repository.saveNew(pizza2);
            
            // When
            List<FoodItem> restaurant1Items = repository.findByRestaurant(testRestaurant.getId());
            List<FoodItem> restaurant2Items = repository.findByRestaurant(secondRestaurant.getId());
            
            // Then
            assertThat(restaurant1Items).hasSize(2);
            assertThat(restaurant1Items).extracting(FoodItem::getName)
                    .containsExactlyInAnyOrder("Pizza", "Burger");
            assertThat(restaurant1Items).allMatch(item -> item.getRestaurant().getId().equals(testRestaurant.getId()));
            
            assertThat(restaurant2Items).hasSize(1);
            assertThat(restaurant2Items.get(0).getName()).isEqualTo("Pizza");
            assertThat(restaurant2Items.get(0).getPrice()).isEqualTo(30000.0);
        }

        @Test
        @DisplayName("Find available by restaurant filters correctly")
        void findAvailableByRestaurant_variousStates_filtersCorrectly() {
            // Given
            FoodItem available = FoodItem.forMenu("Available Pizza", "Available pizza", 25000.0, "Italian", testRestaurant);
            available.setQuantity(5);
            available.setAvailable(true);
            
            FoodItem unavailable = FoodItem.forMenu("Unavailable Burger", "Unavailable burger", 20000.0, "Fast Food", testRestaurant);
            unavailable.setAvailable(false);
            unavailable.setQuantity(10);
            
            FoodItem outOfStock = FoodItem.forMenu("Out of Stock Pasta", "Out of stock pasta", 18000.0, "Italian", testRestaurant);
            outOfStock.setQuantity(0);
            outOfStock.setAvailable(true);
            
            FoodItem availableButZeroQuantity = FoodItem.forMenu("Zero Quantity", "Zero quantity item", 15000.0, "Other", testRestaurant);
            availableButZeroQuantity.setQuantity(0);
            availableButZeroQuantity.setAvailable(true);
            
            repository.saveNew(available);
            repository.saveNew(unavailable);
            repository.saveNew(outOfStock);
            repository.saveNew(availableButZeroQuantity);
            
            // When
            List<FoodItem> availableItems = repository.findAvailableByRestaurant(testRestaurant.getId());
            
            // Then
            assertThat(availableItems).hasSize(1);
            assertThat(availableItems.get(0).getName()).isEqualTo("Available Pizza");
            assertThat(availableItems.get(0).getQuantity()).isGreaterThan(0);
            assertThat(availableItems.get(0).getAvailable()).isTrue();
        }

        @Test
        @DisplayName("Find by category returns correct items")
        void findByCategory_multipleCategories_returnsCorrectItems() {
            // Given
            FoodItem pizza = FoodItem.forMenu("Pizza", "Italian pizza", 25000.0, "Italian", testRestaurant);
            FoodItem pasta = FoodItem.forMenu("Pasta", "Italian pasta", 18000.0, "Italian", testRestaurant);
            FoodItem burger = FoodItem.forMenu("Burger", "Fast burger", 20000.0, "Fast Food", testRestaurant);
            FoodItem unavailableItalian = FoodItem.forMenu("Unavailable Risotto", "Risotto", 30000.0, "Italian", testRestaurant);
            unavailableItalian.setAvailable(false);
            
            repository.saveNew(pizza);
            repository.saveNew(pasta);
            repository.saveNew(burger);
            repository.saveNew(unavailableItalian);
            
            // When
            List<FoodItem> italianItems = repository.findByCategory("Italian");
            List<FoodItem> fastFoodItems = repository.findByCategory("Fast Food");
            List<FoodItem> nonExistentItems = repository.findByCategory("Non-existent");
            
            // Then
            assertThat(italianItems).hasSize(2); // Only available items
            assertThat(italianItems).extracting(FoodItem::getName)
                    .containsExactlyInAnyOrder("Pizza", "Pasta");
            assertThat(italianItems).allMatch(item -> item.getAvailable());
            
            assertThat(fastFoodItems).hasSize(1);
            assertThat(fastFoodItems.get(0).getName()).isEqualTo("Burger");
            
            assertThat(nonExistentItems).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"pizza", "PIZZA", "Pizza", "pIzZa"})
        @DisplayName("Search by keyword is case insensitive")
        void searchByKeyword_caseInsensitive_findsItems(String keyword) {
            // Given
            FoodItem pizza = FoodItem.forMenu("Pizza Margherita", "Classic pizza", 25000.0, "Italian", testRestaurant);
            pizza.setKeywords("pizza cheese tomato margherita");
            repository.saveNew(pizza);
            
            // When
            List<FoodItem> results = repository.searchByKeyword(keyword);
            
            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("Pizza Margherita");
        }

        @Test
        @DisplayName("Search by keyword searches name and keywords")
        void searchByKeyword_searchesNameAndKeywords_findsCorrectItems() {
            // Given
            FoodItem pizza = FoodItem.forMenu("Pizza Margherita", "Classic pizza", 25000.0, "Italian", testRestaurant);
            pizza.setKeywords("cheese tomato italian");
            
            FoodItem burger = FoodItem.forMenu("Cheeseburger", "Beef burger", 20000.0, "Fast Food", testRestaurant);
            burger.setKeywords("beef meat fast");
            
            FoodItem pasta = FoodItem.forMenu("Pasta Carbonara", "Creamy pasta", 22000.0, "Italian", testRestaurant);
            pasta.setKeywords("cream egg cheese");
            
            repository.saveNew(pizza);
            repository.saveNew(burger);
            repository.saveNew(pasta);
            
            // When
            List<FoodItem> cheeseResults = repository.searchByKeyword("cheese");
            List<FoodItem> burgerResults = repository.searchByKeyword("burger");
            List<FoodItem> pastaResults = repository.searchByKeyword("pasta");
            
            // Then
            assertThat(cheeseResults).hasSize(3); // Pizza (keywords) + Pasta (keywords) + Cheeseburger (name)
            assertThat(cheeseResults).extracting(FoodItem::getName)
                    .containsExactlyInAnyOrder("Pizza Margherita", "Pasta Carbonara", "Cheeseburger");
            
            assertThat(burgerResults).hasSize(1); // Cheeseburger (name)
            assertThat(burgerResults.get(0).getName()).isEqualTo("Cheeseburger");
            
            assertThat(pastaResults).hasSize(1); // Pasta Carbonara (name)
            assertThat(pastaResults.get(0).getName()).isEqualTo("Pasta Carbonara");
        }

        @Test
        @DisplayName("Find all returns all items")
        void findAll_multipleItems_returnsAllItems() {
            // Given
            FoodItem pizza = FoodItem.forMenu("Pizza", "Pizza", 25000.0, "Italian", testRestaurant);
            FoodItem burger = FoodItem.forMenu("Burger", "Burger", 20000.0, "Fast Food", secondRestaurant);
            repository.saveNew(pizza);
            repository.saveNew(burger);
            
            // When
            List<FoodItem> allItems = repository.findAll();
            
            // Then
            assertThat(allItems).hasSize(2);
            assertThat(allItems).extracting(FoodItem::getName)
                    .containsExactlyInAnyOrder("Pizza", "Burger");
        }
    }

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {
        
        @Test
        @DisplayName("Update availability changes availability status")
        void updateAvailability_validItem_changesStatus() {
            // Given
            FoodItem foodItem = FoodItem.forMenu("Pizza", "Test pizza", 25000.0, "Italian", testRestaurant);
            FoodItem saved = repository.saveNew(foodItem);
            assertThat(saved.getAvailable()).isTrue(); // Initial state
            
            // When
            repository.updateAvailability(saved.getId(), false);
            
            // Then
            Optional<FoodItem> updated = repository.findById(saved.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getAvailable()).isFalse();
            
            // When - Change back to true
            repository.updateAvailability(saved.getId(), true);
            
            // Then
            Optional<FoodItem> updatedAgain = repository.findById(saved.getId());
            assertThat(updatedAgain).isPresent();
            assertThat(updatedAgain.get().getAvailable()).isTrue();
        }

        @Test
        @DisplayName("Update availability with non-existent ID does nothing")
        void updateAvailability_nonExistentId_doesNothing() {
            // When & Then - Should not throw exception
            assertThatNoException().isThrownBy(() -> repository.updateAvailability(999L, false));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 5, 10, 100})
        @DisplayName("Update quantity sets correct quantity")
        void updateQuantity_variousQuantities_setsCorrectly(int quantity) {
            // Given
            FoodItem foodItem = FoodItem.forMenu("Pizza", "Test pizza", 25000.0, "Italian", testRestaurant);
            FoodItem saved = repository.saveNew(foodItem);
            
            // When
            repository.updateQuantity(saved.getId(), quantity);
            
            // Then
            Optional<FoodItem> updated = repository.findById(saved.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("Update quantity with non-existent ID does nothing")
        void updateQuantity_nonExistentId_doesNothing() {
            // When & Then - Should not throw exception
            assertThatNoException().isThrownBy(() -> repository.updateQuantity(999L, 10));
        }
    }

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {
        
        @Test
        @DisplayName("Delete existing item removes from database")
        void delete_existingItem_removesFromDatabase() {
            // Given
            FoodItem foodItem = FoodItem.forMenu("Pizza", "Test pizza", 25000.0, "Italian", testRestaurant);
            FoodItem saved = repository.saveNew(foodItem);
            Long itemId = saved.getId();
            
            // Verify item exists
            assertThat(repository.findById(itemId)).isPresent();
            
            // When
            repository.delete(itemId);
            
            // Then
            assertThat(repository.findById(itemId)).isNotPresent();
        }

        @Test
        @DisplayName("Delete non-existent item does nothing")
        void delete_nonExistentItem_doesNothing() {
            // When & Then - Should not throw exception
            assertThatNoException().isThrownBy(() -> repository.delete(999L));
        }

        @Test
        @DisplayName("Delete all removes all items")
        void deleteAll_multipleItems_removesAllItems() {
            // Given
            FoodItem pizza = FoodItem.forMenu("Pizza", "Pizza", 25000.0, "Italian", testRestaurant);
            FoodItem burger = FoodItem.forMenu("Burger", "Burger", 20000.0, "Fast Food", testRestaurant);
            repository.saveNew(pizza);
            repository.saveNew(burger);
            
            // Verify items exist
            assertThat(repository.findAll()).hasSize(2);
            
            // When
            repository.deleteAll();
            
            // Then
            assertThat(repository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {
        
        @Test
        @DisplayName("Search with empty keyword returns empty list")
        void searchByKeyword_emptyKeyword_returnsEmpty() {
            // Given
            FoodItem pizza = FoodItem.forMenu("Pizza", "Pizza", 25000.0, "Italian", testRestaurant);
            repository.saveNew(pizza);
            
            // When
            List<FoodItem> results = repository.searchByKeyword("");
            
            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Find by restaurant with non-existent restaurant returns empty")
        void findByRestaurant_nonExistentRestaurant_returnsEmpty() {
            // When
            List<FoodItem> results = repository.findByRestaurant(999L);
            
            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Operations work correctly with special characters")
        void operations_specialCharacters_workCorrectly() {
            // Given
            FoodItem specialItem = FoodItem.forMenu("Café Latté", "Special café drink with latté", 35000.0, "Beverages", testRestaurant);
            specialItem.setKeywords("café latté special drink");
            
            // When
            FoodItem saved = repository.saveNew(specialItem);
            List<FoodItem> searchResults = repository.searchByKeyword("café");
            
            // Then
            assertThat(saved.getName()).isEqualTo("Café Latté");
            assertThat(searchResults).hasSize(1);
            assertThat(searchResults.get(0).getName()).isEqualTo("Café Latté");
        }
    }

    // Helper methods
    private Restaurant createAndSaveRestaurant(String name, String address) {
        Restaurant restaurant = Restaurant.forRegistration(1L, name, address, "021-123");
        restaurant.setStatus(RestaurantStatus.APPROVED);
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(restaurant);
            tx.commit();
        }
        return restaurant;
    }

    private void cleanRestaurants() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("delete from Restaurant").executeUpdate();
            tx.commit();
        }
    }
}
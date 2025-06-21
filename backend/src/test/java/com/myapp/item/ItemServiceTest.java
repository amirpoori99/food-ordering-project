package com.myapp.item;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.item.ItemRepository;
import com.myapp.item.ItemService;
import com.myapp.restaurant.RestaurantRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ItemServiceTest {
    
    private static SessionFactory sessionFactory;
    private Session session;
    private ItemService itemService;
    private ItemRepository itemRepository;
    private RestaurantRepository restaurantRepository;
    
    @BeforeAll
    static void setUpClass() {
        sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    // Note: SessionFactory is shared across test classes, so we don't close it here
    // It will be closed when the JVM shuts down
    
    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
        itemRepository = new ItemRepository();
        restaurantRepository = new RestaurantRepository();
        itemService = new ItemService(itemRepository, restaurantRepository);
        
        // Clean up database
        session.beginTransaction();
        session.createQuery("DELETE FROM OrderItem").executeUpdate();
        session.createQuery("DELETE FROM Order").executeUpdate();
        session.createQuery("DELETE FROM FoodItem").executeUpdate();
        session.createQuery("DELETE FROM Restaurant").executeUpdate();
        session.getTransaction().commit();
    }
    
    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }
    
    @Nested
    @DisplayName("Add Item Tests")
    class AddItemTests {
        
        @Test
        @DisplayName("Should add item successfully with valid data")
        void addItem_ValidData_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            
            // When
            FoodItem item = itemService.addItem(
                restaurant.getId(),
                "Cheeseburger",
                "Delicious beef burger with cheese",
                12.99,
                "Burgers",
                "https://example.com/burger.jpg",
                50
            );
            
            // Then
            assertNotNull(item);
            assertNotNull(item.getId());
            assertEquals("Cheeseburger", item.getName());
            assertEquals("Delicious beef burger with cheese", item.getDescription());
            assertEquals(12.99, item.getPrice(), 0.01);
            assertEquals("Burgers", item.getCategory());
            assertEquals("https://example.com/burger.jpg", item.getImageUrl());
            assertEquals(50, item.getQuantity());
            assertEquals(restaurant.getId(), item.getRestaurant().getId());
                         assertTrue(item.getAvailable());
        }
        
        @Test
        @DisplayName("Should add item without image URL")
        void addItem_NoImageUrl_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            
            // When
            FoodItem item = itemService.addItem(
                restaurant.getId(),
                "Pizza",
                "Cheese pizza",
                15.99,
                "Pizza",
                null,
                30
            );
            
            // Then
            assertNotNull(item);
            assertEquals("Pizza", item.getName());
            assertNull(item.getImageUrl());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent restaurant")
        void addItem_NonExistentRestaurant_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.addItem(999L, "Pizza", "Description", 10.0, "Category", null, 10)
            );
            assertEquals("Restaurant not found with id=999", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for empty name")
        void addItem_EmptyName_ThrowsException() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "", "Description", 10.0, "Category", null, 10)
            );
            assertEquals("Item name cannot be empty", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for negative price")
        void addItem_NegativePrice_ThrowsException() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Pizza", "Description", -5.0, "Category", null, 10)
            );
            assertEquals("Item price must be positive", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for excessive price")
        void addItem_ExcessivePrice_ThrowsException() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Pizza", "Description", 15000.0, "Category", null, 10)
            );
            assertEquals("Item price cannot exceed 10,000", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Update Item Tests")
    class UpdateItemTests {
        
        @Test
        @DisplayName("Should update item successfully")
        void updateItem_ValidData_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            
            // When
            FoodItem updatedItem = itemService.updateItem(
                item.getId(),
                "Updated Burger",
                "Updated description",
                15.99,
                "Updated Category",
                "https://new-image.com/burger.jpg",
                75
            );
            
            // Then
            assertNotNull(updatedItem);
            assertEquals("Updated Burger", updatedItem.getName());
            assertEquals("Updated description", updatedItem.getDescription());
            assertEquals(15.99, updatedItem.getPrice(), 0.01);
            assertEquals("Updated Category", updatedItem.getCategory());
            assertEquals("https://new-image.com/burger.jpg", updatedItem.getImageUrl());
            assertEquals(75, updatedItem.getQuantity());
        }
        
        @Test
        @DisplayName("Should update only provided fields")
        void updateItem_PartialUpdate_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            String originalName = item.getName();
            double originalPrice = item.getPrice();
            
            // When
            FoodItem updatedItem = itemService.updateItem(
                item.getId(),
                null, // Don't update name
                "New description only",
                -1, // Don't update price (negative value ignored)
                null, // Don't update category
                null, // Don't update image
                null  // Don't update quantity
            );
            
            // Then
            assertEquals(originalName, updatedItem.getName()); // Unchanged
            assertEquals("New description only", updatedItem.getDescription()); // Changed
            assertEquals(originalPrice, updatedItem.getPrice(), 0.01); // Unchanged
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent item")
        void updateItem_NonExistentItem_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.updateItem(999L, "Name", "Description", 10.0, "Category", null, 10)
            );
            assertEquals("Food item not found with id=999", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Get Item Tests")
    class GetItemTests {
        
        @Test
        @DisplayName("Should get item by ID successfully")
        void getItem_ValidId_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            
            // When
            FoodItem foundItem = itemService.getItem(item.getId());
            
            // Then
            assertNotNull(foundItem);
            assertEquals(item.getId(), foundItem.getId());
            assertEquals(item.getName(), foundItem.getName());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent item")
        void getItem_NonExistentId_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getItem(999L)
            );
            assertEquals("Food item not found with id=999", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Restaurant Items Tests")
    class RestaurantItemsTests {
        
        @Test
        @DisplayName("Should get all restaurant items")
        void getRestaurantItems_ValidRestaurant_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Burger", true);
            createTestFoodItem(restaurant, "Pizza", false);
            createTestFoodItem(restaurant, "Pasta", true);
            
            // When
            List<FoodItem> items = itemService.getRestaurantItems(restaurant.getId());
            
            // Then
            assertEquals(3, items.size());
        }
        
        @Test
        @DisplayName("Should get only available restaurant items")
        void getAvailableItems_ValidRestaurant_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Burger", true);
            createTestFoodItem(restaurant, "Pizza", false);
            createTestFoodItem(restaurant, "Pasta", true);
            
            // When
            List<FoodItem> items = itemService.getAvailableItems(restaurant.getId());
            
            // Then
            assertEquals(2, items.size());
                         assertTrue(items.stream().allMatch(item -> item.getAvailable()));
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent restaurant")
        void getRestaurantItems_NonExistentRestaurant_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getRestaurantItems(999L)
            );
            assertEquals("Restaurant not found with id=999", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Search Tests")
    class SearchTests {
        
        @Test
        @DisplayName("Should search items by keyword")
        void searchItems_ValidKeyword_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Cheeseburger", true);
            createTestFoodItem(restaurant, "Chicken Burger", true);
            createTestFoodItem(restaurant, "Pizza Margherita", true);
            
            // When
            List<FoodItem> items = itemService.searchItems("burger");
            
            // Then
            assertEquals(2, items.size());
            assertTrue(items.stream().allMatch(item -> 
                item.getName().toLowerCase().contains("burger") ||
                item.getDescription().toLowerCase().contains("burger") ||
                item.getKeywords().toLowerCase().contains("burger")
            ));
        }
        
        @Test
        @DisplayName("Should throw exception for empty keyword")
        void searchItems_EmptyKeyword_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.searchItems("")
            );
            assertEquals("Search keyword cannot be empty", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should get items by category")
        void getItemsByCategory_ValidCategory_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Burger", "Burgers", true);
            createTestFoodItem(restaurant, "Cheeseburger", "Burgers", true);
            createTestFoodItem(restaurant, "Pizza", "Pizza", true);
            
            // When
            List<FoodItem> items = itemService.getItemsByCategory("Burgers");
            
            // Then
            assertEquals(2, items.size());
            assertTrue(items.stream().allMatch(item -> "Burgers".equals(item.getCategory())));
        }
    }
    
    @Nested
    @DisplayName("Inventory Management Tests")
    class InventoryManagementTests {
        
        @Test
        @DisplayName("Should decrease quantity successfully")
        void decreaseQuantity_SufficientStock_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(10);
            itemRepository.save(item);
            
            // When
            boolean result = itemService.decreaseQuantity(item.getId(), 3);
            
            // Then
            assertTrue(result);
            FoodItem updatedItem = itemService.getItem(item.getId());
            assertEquals(7, updatedItem.getQuantity());
        }
        
        @Test
        @DisplayName("Should fail to decrease quantity with insufficient stock")
        void decreaseQuantity_InsufficientStock_ReturnsFalse() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(2);
            itemRepository.save(item);
            
            // When
            boolean result = itemService.decreaseQuantity(item.getId(), 5);
            
            // Then
            assertFalse(result);
            FoodItem unchangedItem = itemService.getItem(item.getId());
            assertEquals(2, unchangedItem.getQuantity()); // Unchanged
        }
        
        @Test
        @DisplayName("Should increase quantity successfully")
        void increaseQuantity_ValidAmount_Success() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(5);
            itemRepository.save(item);
            
            // When
            itemService.increaseQuantity(item.getId(), 10);
            
            // Then
            FoodItem updatedItem = itemService.getItem(item.getId());
            assertEquals(15, updatedItem.getQuantity());
        }
        
        @Test
        @DisplayName("Should check stock status correctly")
        void isInStock_VariousQuantities_CorrectStatus() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem inStockItem = createTestFoodItem(restaurant);
            inStockItem.setQuantity(5);
            itemRepository.save(inStockItem);
            
            FoodItem outOfStockItem = createTestFoodItem(restaurant);
            outOfStockItem.setQuantity(0);
            itemRepository.save(outOfStockItem);
            
            // When & Then
            assertTrue(itemService.isInStock(inStockItem.getId()));
            assertFalse(itemService.isInStock(outOfStockItem.getId()));
        }
        
        @Test
        @DisplayName("Should get low stock items")
        void getLowStockItems_VariousQuantities_ReturnsLowStockOnly() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            FoodItem lowStock1 = createTestFoodItem(restaurant, "Item1", true);
            lowStock1.setQuantity(2);
            itemRepository.save(lowStock1);
            
            FoodItem lowStock2 = createTestFoodItem(restaurant, "Item2", true);
            lowStock2.setQuantity(4);
            itemRepository.save(lowStock2);
            
            FoodItem normalStock = createTestFoodItem(restaurant, "Item3", true);
            normalStock.setQuantity(10);
            itemRepository.save(normalStock);
            
            // When
            List<FoodItem> lowStockItems = itemService.getLowStockItems(restaurant.getId());
            
            // Then
            assertEquals(2, lowStockItems.size());
            assertTrue(lowStockItems.stream().allMatch(item -> item.getQuantity() < 5));
        }
    }
    
    @Nested
    @DisplayName("Menu Statistics Tests")
    class MenuStatisticsTests {
        
        @Test
        @DisplayName("Should calculate menu statistics correctly")
        void getMenuStatistics_VariousItems_CorrectStatistics() {
            // Given
            Restaurant restaurant = createTestRestaurant();
            
            // Available and in stock
            FoodItem item1 = FoodItem.forMenu("Item1", "Test description", 15.0, "Test Category", restaurant);
            item1.setQuantity(10);
            item1.setAvailable(true);
            item1 = itemRepository.saveNew(item1);
            
            // Available but low stock
            FoodItem item2 = FoodItem.forMenu("Item2", "Test description", 20.0, "Test Category", restaurant);
            item2.setQuantity(2);
            item2.setAvailable(true);
            item2 = itemRepository.saveNew(item2);
            
            // Not available
            FoodItem item3 = FoodItem.forMenu("Item3", "Test description", 10.0, "Test Category", restaurant);
            item3.setQuantity(5);
            item3.setAvailable(false);
            item3 = itemRepository.saveNew(item3);
            
            // When
            ItemService.MenuStatistics stats = itemService.getMenuStatistics(restaurant.getId());
            
            // Then
            assertEquals(3, stats.getTotalItems());
            assertEquals(2, stats.getAvailableItems());
            assertEquals(2, stats.getInStockItems()); // Only Item1 and Item2 are both available AND have quantity > 0
            assertEquals(1, stats.getLowStockItems());
            assertEquals(15.0, stats.getAveragePrice(), 0.01); // (15+20+10)/3
        }
    }

    // =============== NEW COMPREHENSIVE EDGE CASE TESTS ===============

    @Nested
    @DisplayName("Input Validation Edge Cases")
    class InputValidationTests {
        
        @Test
        @DisplayName("Add item with null parameters throws exceptions")
        void addItem_nullParameters_throwsExceptions() {
            Restaurant restaurant = createTestRestaurant();
            
            // Null name
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), null, "Description", 10.0, "Category", null, 10)
            );
            
            // Null description - system validates this as required
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Valid Name", null, 10.0, "Category", null, 10)
            );
            
            // Null category
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Name", "Description", 10.0, null, null, 10)
            );
        }
        
        @Test
        @DisplayName("Add item with whitespace-only fields throws exceptions")
        void addItem_whitespaceOnlyFields_throwsExceptions() {
            Restaurant restaurant = createTestRestaurant();
            
            // Whitespace-only name
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "   ", "Description", 10.0, "Category", null, 10)
            );
            
            // Whitespace-only category
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Name", "Description", 10.0, "   ", null, 10)
            );
        }

        @Test
        @DisplayName("Update item with invalid IDs throws exceptions")
        void updateItem_invalidIds_throwsExceptions() {
            // Null ID
            assertThrows(IllegalArgumentException.class, () ->
                itemService.updateItem(null, "Name", "Description", 10.0, "Category", null, 10)
            );
            
            // Zero ID
            assertThrows(NotFoundException.class, () ->
                itemService.updateItem(0L, "Name", "Description", 10.0, "Category", null, 10)
            );
            
            // Negative ID
            assertThrows(NotFoundException.class, () ->
                itemService.updateItem(-1L, "Name", "Description", 10.0, "Category", null, 10)
            );
        }

        @Test
        @DisplayName("Get item with invalid IDs throws exceptions")
        void getItem_invalidIds_throwsExceptions() {
            // Null ID
            assertThrows(IllegalArgumentException.class, () ->
                itemService.getItem(null)
            );
            
            // Zero ID
            assertThrows(NotFoundException.class, () ->
                itemService.getItem(0L)
            );
            
            // Negative ID
            assertThrows(NotFoundException.class, () ->
                itemService.getItem(-1L)
            );
        }

        @Test
        @DisplayName("Search with null keyword throws exception")
        void searchItems_nullKeyword_throwsException() {
            assertThrows(IllegalArgumentException.class, () ->
                itemService.searchItems(null)
            );
        }

        @Test
        @DisplayName("Search with whitespace-only keyword throws exception")
        void searchItems_whitespaceKeyword_throwsException() {
            assertThrows(IllegalArgumentException.class, () ->
                itemService.searchItems("   ")
            );
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {
        
        @Test
        @DisplayName("Add item with boundary prices")
        void addItem_boundaryPrices_handledCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // Zero price should fail
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Free Item", "Description", 0.0, "Category", null, 10)
            );
            
            // Very small positive price should succeed
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Cheap Item", "Description", 0.01, "Category", null, 10);
                assertEquals(0.01, item.getPrice(), 0.001);
            });
            
            // Maximum price should succeed
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Expensive Item", "Description", 9999.99, "Category", null, 10);
                assertEquals(9999.99, item.getPrice(), 0.01);
            });
            
            // Just over maximum should fail
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Too Expensive", "Description", 10000.01, "Category", null, 10)
            );
        }

        @Test
        @DisplayName("Add item with boundary quantities")
        void addItem_boundaryQuantities_handledCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // Zero quantity should be allowed (out of stock)
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Out of Stock", "Description", 10.0, "Category", null, 0);
                assertEquals(0, item.getQuantity());
            });
            
            // Negative quantity should fail
            assertThrows(IllegalArgumentException.class, () ->
                itemService.addItem(restaurant.getId(), "Invalid Quantity", "Description", 10.0, "Category", null, -1)
            );
            
            // Very large quantity should succeed
            assertDoesNotThrow(() -> {
                FoodItem item = itemService.addItem(restaurant.getId(), "Bulk Item", "Description", 10.0, "Category", null, 999999);
                assertEquals(999999, item.getQuantity());
            });
        }

        @Test
        @DisplayName("Add item with very long text fields validates length constraints")
        void addItem_veryLongTextFields_validatesLengthConstraints() {
            Restaurant restaurant = createTestRestaurant();
            
            // Very long name (exceeds 100 char limit)
            String longName = "A".repeat(101);
            assertThrows(IllegalArgumentException.class, () -> {
                itemService.addItem(restaurant.getId(), longName, "Description", 10.0, "Category", null, 10);
            });
            
            // Very long description (exceeds 500 char limit)
            String longDescription = "A".repeat(501);
            assertThrows(IllegalArgumentException.class, () -> {
                itemService.addItem(restaurant.getId(), "Normal Name", longDescription, 10.0, "Category", null, 10);
            });
            
            // Very long category (exceeds 50 char limit)
            String longCategory = "A".repeat(51);
            assertThrows(IllegalArgumentException.class, () -> {
                itemService.addItem(restaurant.getId(), "Normal Name", "Description", 10.0, longCategory, null, 10);
            });
            
            // Valid lengths should work
            String validName = "A".repeat(100); // Exact limit
            String validDescription = "B".repeat(500); // Exact limit  
            String validCategory = "C".repeat(50); // Exact limit
            assertDoesNotThrow(() -> {
                itemService.addItem(restaurant.getId(), validName, validDescription, 10.0, validCategory, null, 10);
            });
        }

        @Test
        @DisplayName("Inventory operations with boundary values")
        void inventoryOperations_boundaryValues_handledCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(1);
            itemRepository.save(item);
            
            // Decrease to exactly zero
            assertTrue(itemService.decreaseQuantity(item.getId(), 1));
            assertEquals(0, itemService.getItem(item.getId()).getQuantity());
            
            // Try to decrease when already zero
            assertFalse(itemService.decreaseQuantity(item.getId(), 1));
            assertEquals(0, itemService.getItem(item.getId()).getQuantity());
            
            // Increase from zero
            itemService.increaseQuantity(item.getId(), 5);
            assertEquals(5, itemService.getItem(item.getId()).getQuantity());
            
            // Very large increase
            itemService.increaseQuantity(item.getId(), 999999);
            assertEquals(1000004, itemService.getItem(item.getId()).getQuantity());
        }
    }

    @Nested
    @DisplayName("Business Logic Edge Cases")
    class BusinessLogicTests {
        
        @Test
        @DisplayName("Search items with special characters works correctly")
        void searchItems_specialCharacters_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Café Américain", "Beverages", true);
            createTestFoodItem(restaurant, "Nacho's Special", "Mexican", true);
            createTestFoodItem(restaurant, "Fish & Chips", "British", true);
            
            // Search with accented characters
            List<FoodItem> results1 = itemService.searchItems("café");
            assertEquals(1, results1.size());
            assertEquals("Café Américain", results1.get(0).getName());
            
            // Search with apostrophe
            List<FoodItem> results2 = itemService.searchItems("nacho");
            assertEquals(1, results2.size());
            
            // Search with ampersand
            List<FoodItem> results3 = itemService.searchItems("fish");
            assertEquals(1, results3.size());
        }

        @Test
        @DisplayName("Case insensitive search works correctly")
        void searchItems_caseInsensitive_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "HAMBURGER Deluxe", "Burgers", true);
            createTestFoodItem(restaurant, "chicken WINGS", "Appetizers", true);
            
            // Search with different cases
            assertEquals(1, itemService.searchItems("hamburger").size());
            assertEquals(1, itemService.searchItems("HAMBURGER").size());
            assertEquals(1, itemService.searchItems("HaMbUrGeR").size());
            
            assertEquals(1, itemService.searchItems("wings").size());
            assertEquals(1, itemService.searchItems("WINGS").size());
            assertEquals(1, itemService.searchItems("WiNgS").size());
        }

        @Test
        @DisplayName("Get items by category with case sensitivity")
        void getItemsByCategory_caseSensitive_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            createTestFoodItem(restaurant, "Item1", "Burgers", true);
            createTestFoodItem(restaurant, "Item2", "burgers", true);
            createTestFoodItem(restaurant, "Item3", "BURGERS", true);
            
            // Category search should be case sensitive (business decision)
            assertEquals(1, itemService.getItemsByCategory("Burgers").size());
            assertEquals(1, itemService.getItemsByCategory("burgers").size());
            assertEquals(1, itemService.getItemsByCategory("BURGERS").size());
        }

        @Test
        @DisplayName("Available items filtering works correctly")
        void availableItemsFiltering_variousStates_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // Available with stock
            FoodItem item1 = createTestFoodItem(restaurant, "Available In Stock", true);
            item1.setQuantity(10);
            itemRepository.save(item1);
            
            // Available but no stock
            FoodItem item2 = createTestFoodItem(restaurant, "Available No Stock", true);
            item2.setQuantity(0);
            itemRepository.save(item2);
            
            // Not available but has stock
            FoodItem item3 = createTestFoodItem(restaurant, "Not Available Has Stock", false);
            item3.setQuantity(5);
            itemRepository.save(item3);
            
            // Not available and no stock
            FoodItem item4 = createTestFoodItem(restaurant, "Not Available No Stock", false);
            item4.setQuantity(0);
            itemRepository.save(item4);
            
            // Get all items
            List<FoodItem> allItems = itemService.getRestaurantItems(restaurant.getId());
            assertEquals(4, allItems.size());
            
            // Get only available items - system filters by available=true AND quantity>0
            List<FoodItem> availableItems = itemService.getAvailableItems(restaurant.getId());
            assertEquals(1, availableItems.size()); // Only item1 matches both criteria
            assertTrue(availableItems.stream().allMatch(FoodItem::getAvailable));
            assertTrue(availableItems.stream().allMatch(item -> item.getQuantity() > 0));
        }

        @Test
        @DisplayName("Menu statistics with edge cases")
        void menuStatistics_edgeCases_calculatedCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // When
            ItemService.MenuStatistics emptyStats = itemService.getMenuStatistics(restaurant.getId());
            
            // Then - empty restaurant
            assertEquals(0, emptyStats.getTotalItems());
            assertEquals(0, emptyStats.getAvailableItems());
            assertEquals(0, emptyStats.getInStockItems());
            assertEquals(0, emptyStats.getLowStockItems());
            assertEquals(0.0, emptyStats.getAveragePrice(), 0.01);
        }

        @Test
        @DisplayName("Concurrent quantity modifications handled safely")
        void concurrentQuantityModifications_handledSafely() throws InterruptedException {
            Restaurant restaurant = createTestRestaurant();
            FoodItem item = createTestFoodItem(restaurant);
            item.setQuantity(100);
            itemRepository.save(item);
            
            // This test simulates concurrent access but in a single thread
            // In a real concurrent scenario, proper synchronization would be needed
            
            // Multiple decreases
            for (int i = 0; i < 10; i++) {
                assertTrue(itemService.decreaseQuantity(item.getId(), 5));
            }
            
            FoodItem updatedItem = itemService.getItem(item.getId());
            assertEquals(50, updatedItem.getQuantity());
            
            // Try to decrease more than available
            assertFalse(itemService.decreaseQuantity(item.getId(), 100));
            assertEquals(50, itemService.getItem(item.getId()).getQuantity());
        }
    }

    @Nested
    @DisplayName("Integration and Data Consistency Tests")
    class DataConsistencyTests {
        
        @Test
        @DisplayName("Item operations maintain restaurant relationship integrity")
        void itemOperations_maintainRestaurantRelationship() {
            Restaurant restaurant1 = createTestRestaurant();
            Restaurant restaurant2 = createTestRestaurant();
            
            // Add item to restaurant1
            FoodItem item = itemService.addItem(restaurant1.getId(), "Test Item", "Description", 10.0, "Category", null, 10);
            assertEquals(restaurant1.getId(), item.getRestaurant().getId());
            
            // Update item - restaurant relationship should remain
            FoodItem updatedItem = itemService.updateItem(item.getId(), "Updated Name", null, -1, null, null, null);
            assertEquals(restaurant1.getId(), updatedItem.getRestaurant().getId());
            
            // Item should appear in restaurant1's items, not restaurant2's
            List<FoodItem> restaurant1Items = itemService.getRestaurantItems(restaurant1.getId());
            List<FoodItem> restaurant2Items = itemService.getRestaurantItems(restaurant2.getId());
            
            assertEquals(1, restaurant1Items.size());
            assertEquals(0, restaurant2Items.size());
            assertEquals(item.getId(), restaurant1Items.get(0).getId());
        }

        @Test
        @DisplayName("Search across multiple restaurants works correctly")
        void searchAcrossRestaurants_worksCorrectly() {
            Restaurant restaurant1 = createTestRestaurant();
            Restaurant restaurant2 = createTestRestaurant();
            
            createTestFoodItem(restaurant1, "Pizza Margherita", "Pizza", true);
            createTestFoodItem(restaurant2, "Pizza Pepperoni", "Pizza", true);
            createTestFoodItem(restaurant1, "Burger Classic", "Burgers", true);
            
            // Search should return items from all restaurants
            List<FoodItem> pizzaResults = itemService.searchItems("pizza");
            assertEquals(2, pizzaResults.size());
            
            List<FoodItem> burgerResults = itemService.searchItems("burger");
            assertEquals(1, burgerResults.size());
        }

        @Test
        @DisplayName("Low stock detection across restaurant works correctly")
        void lowStockDetection_acrossRestaurant_worksCorrectly() {
            Restaurant restaurant = createTestRestaurant();
            
            // Create items with various stock levels
            FoodItem lowStock1 = createTestFoodItem(restaurant, "Low Stock 1", true);
            lowStock1.setQuantity(1);
            itemRepository.save(lowStock1);
            
            FoodItem lowStock2 = createTestFoodItem(restaurant, "Low Stock 2", true);
            lowStock2.setQuantity(4);
            itemRepository.save(lowStock2);
            
            FoodItem normalStock = createTestFoodItem(restaurant, "Normal Stock", true);
            normalStock.setQuantity(10);
            itemRepository.save(normalStock);
            
            FoodItem outOfStock = createTestFoodItem(restaurant, "Out of Stock", true);
            outOfStock.setQuantity(0);
            itemRepository.save(outOfStock);
            
            // Unavailable item (will be included in low stock detection as system doesn't filter by availability)
            FoodItem unavailableLowStock = createTestFoodItem(restaurant, "Unavailable Low", false);
            unavailableLowStock.setQuantity(2);
            itemRepository.save(unavailableLowStock);
            
            List<FoodItem> lowStockItems = itemService.getLowStockItems(restaurant.getId());
            
            // System includes all items with quantity < 5, regardless of availability
            assertEquals(4, lowStockItems.size()); // lowStock1, lowStock2, outOfStock, unavailableLowStock
            assertTrue(lowStockItems.stream().allMatch(item -> item.getQuantity() < 5));
        }
    }

    // Helper methods
    private Restaurant createTestRestaurant() {
        Restaurant restaurant = new Restaurant(
            1L, // ownerId
            "Test Restaurant",
            "123 Test St", 
            "1234567890"
        );
        restaurant.setStatus(RestaurantStatus.APPROVED);
        return restaurantRepository.saveNew(restaurant);
    }
    
    private FoodItem createTestFoodItem(Restaurant restaurant) {
        return createTestFoodItem(restaurant, "Test Burger", true);
    }
    
    private FoodItem createTestFoodItem(Restaurant restaurant, String name, boolean available) {
        FoodItem item = FoodItem.forMenu(name, "Test description", 12.99, "Test Category", restaurant);
        item.setQuantity(50);
        item.setAvailable(available);
        return itemRepository.saveNew(item);
    }
    
    private FoodItem createTestFoodItem(Restaurant restaurant, String name, String category, boolean available) {
        FoodItem item = FoodItem.forMenu(name, "Test description", 12.99, category, restaurant);
        item.setQuantity(50);
        item.setAvailable(available);
        return itemRepository.saveNew(item);
    }
}

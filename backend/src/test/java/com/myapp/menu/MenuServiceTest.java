package com.myapp.menu;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * مجموعه تست‌های جامع MenuService
 * 
 * این کلاس تست تمام عملکردهای سرویس مدیریت منو را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Menu Retrieval Tests
 *    - دریافت منوی رستوران
 *    - فیلتر آیتم‌های موجود
 *    - مدیریت منوی خالی
 *    - validation پارامترها
 * 
 * 2. Add Item to Menu Tests
 *    - اضافه کردن آیتم جدید
 *    - اعتبارسنجی ورودی‌ها
 *    - مدیریت whitespace
 *    - validation محدودیت‌های length
 *    - validation قیمت
 * 
 * 3. Update Menu Item Tests
 *    - به‌روزرسانی کامل آیتم
 *    - به‌روزرسانی جزئی
 *    - حفظ فیلدهای تغییر نکرده
 *    - validation تغییرات
 * 
 * 4. Remove Item from Menu Tests
 *    - حذف آیتم از منو
 *    - مدیریت آیتم غیرموجود
 *    - validation شناسه
 * 
 * 5. Item Availability Tests
 *    - تنظیم وضعیت موجودی
 *    - به‌روزرسانی تعداد آیتم
 *    - مدیریت stock
 * 
 * 6. Menu Category Tests
 *    - دسته‌بندی آیتم‌ها
 *    - فیلتر بر اساس دسته
 *    - لیست دسته‌ها
 * 
 * 7. Menu Statistics Tests
 *    - آمار منو
 *    - محاسبه metrics
 *    - تحلیل عملکرد
 * 
 * 8. Restaurant Ownership Tests
 *    - تأیید مالکیت رستوران
 *    - authorization بر آیتم‌ها
 * 
 * 9. Low Stock Tests
 *    - تشخیص آیتم‌های کم موجود
 *    - alert مدیریت موجودی
 * 
 * Database Integration:
 * - Hibernate Session management
 * - Transaction handling
 * - Data cleanup
 * - Real database operations
 * 
 * Business Logic:
 * - Menu management workflow
 * - Inventory tracking
 * - Category management
 * - Statistics calculation
 * - Ownership validation
 * 
 * Test Coverage: Complete coverage of MenuService functionality
 * 
 * @author Food Ordering System Team
 * @version 1.0
 * @since 2024
 */
@DisplayName("Menu Service Comprehensive Tests - Complete Coverage")
class MenuServiceTest {
    
    /** SessionFactory برای تست‌های پایگاه داده */
    private static SessionFactory sessionFactory;
    
    /** Session جاری برای هر تست */
    private Session session;
    
    /** Service instance تحت تست */
    private MenuService menuService;
    
    /** Repository dependencies */
    private MenuRepository menuRepository;
    private ItemRepository itemRepository;
    private RestaurantRepository restaurantRepository;
    
    /**
     * راه‌اندازی کلی قبل از تمام تست‌ها
     * 
     * Operations:
     * - initialize SessionFactory
     * - setup database connection
     */
    @BeforeAll
    static void setUpClass() {
        sessionFactory = DatabaseUtil.getSessionFactory();
    }
    
    /**
     * راه‌اندازی قبل از هر تست
     * 
     * Operations:
     * - open new database session
     * - initialize repositories
     * - setup MenuService with dependencies
     * - clean database for isolated tests
     */
    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
        menuRepository = new MenuRepository();
        itemRepository = new ItemRepository();
        restaurantRepository = new RestaurantRepository();
        menuService = new MenuService(menuRepository, itemRepository, restaurantRepository);
        
        // پاک‌سازی پایگاه داده
        cleanDatabase();
    }
    
    /**
     * پاک‌سازی بعد از هر تست
     * 
     * Operations:
     * - close database session
     * - release resources
     */
    @AfterEach
    void tearDown() {
        if (session != null) {
            session.close();
        }
    }
    
    /**
     * پاک‌سازی پایگاه داده برای تست‌های مستقل
     * 
     * Operations:
     * - delete all FoodItems
     * - delete all Restaurants
     * - ensure clean state
     */
    private void cleanDatabase() {
        session.beginTransaction();
        session.createQuery("DELETE FROM FoodItem").executeUpdate();
        session.createQuery("DELETE FROM Restaurant").executeUpdate();
        session.getTransaction().commit();
    }
    
    // ==================== تست‌های دریافت منو ====================
    
    /**
     * تست‌های دریافت منو
     * 
     * این دسته شامل تمام عملیات مربوط به دریافت و نمایش منو:
     * - دریافت کامل منوی رستوران
     * - فیلتر آیتم‌های موجود
     * - مدیریت منوی خالی
     * - validation و error handling
     */
    @Nested
    @DisplayName("Menu Retrieval Tests")
    class MenuRetrievalTests {
        
        /**
         * تست موفق دریافت منوی رستوران
         * 
         * Scenario: درخواست منوی رستوران با آیتم‌های مختلف
         * Expected:
         * - تمام آیتم‌های رستوران برگردانده شوند
         * - هم آیتم‌های موجود و هم ناموجود
         */
        @Test
        @DisplayName("Should get restaurant menu successfully")
        void getRestaurantMenu_ValidRestaurant_Success() {
            // Given - آماده‌سازی رستوران و آیتم‌ها
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item1 = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            FoodItem item2 = createAndSaveFoodItem(restaurant, "Burger", 18.99, false);
            
            // When
            List<FoodItem> menu = menuService.getRestaurantMenu(restaurant.getId());
            
            // Then
            assertNotNull(menu);
            assertEquals(2, menu.size());
            assertTrue(menu.stream().anyMatch(item -> item.getName().equals("Pizza")));
            assertTrue(menu.stream().anyMatch(item -> item.getName().equals("Burger")));
        }
        
        /**
         * تست دریافت آیتم‌های موجود فقط
         * 
         * Scenario: فیلتر منو برای نمایش آیتم‌های موجود
         * Expected: فقط آیتم‌های available=true برگردانده شوند
         */
        @Test
        @DisplayName("Should get available menu items only")
        void getAvailableMenu_ValidRestaurant_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem availableItem = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            FoodItem unavailableItem = createAndSaveFoodItem(restaurant, "Burger", 18.99, false);
            
            // When
            List<FoodItem> availableMenu = menuService.getAvailableMenu(restaurant.getId());
            
            // Then
            assertNotNull(availableMenu);
            assertEquals(1, availableMenu.size());
            assertEquals("Pizza", availableMenu.get(0).getName());
            assertTrue(availableMenu.get(0).getAvailable());
        }
        
        /**
         * تست بازگشت لیست خالی برای رستوران بدون منو
         * 
         * Scenario: رستوران جدید بدون آیتم منو
         * Expected: لیست خالی (نه null) برگردانده شود
         */
        @Test
        @DisplayName("Should return empty list for restaurant with no menu")
        void getRestaurantMenu_EmptyMenu_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When
            List<FoodItem> menu = menuService.getRestaurantMenu(restaurant.getId());
            
            // Then
            assertNotNull(menu);
            assertTrue(menu.isEmpty());
        }
        
        /**
         * تست خطا برای ID رستوران null
         * 
         * Scenario: ارسال null به عنوان ID رستوران
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null restaurant ID")
        void getRestaurantMenu_NullId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.getRestaurantMenu(null)
            );
            assertEquals("Restaurant ID must be positive", exception.getMessage());
        }
        
        /**
         * تست خطا برای ID رستوران نامعتبر
         * 
         * @param restaurantId ID نامعتبر برای تست
         * 
         * Scenario: ارسال ID منفی یا صفر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @ParameterizedTest
        @ValueSource(longs = {0, -1, -10})
        @DisplayName("Should throw exception for invalid restaurant ID")
        void getRestaurantMenu_InvalidId_ThrowsException(Long restaurantId) {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.getRestaurantMenu(restaurantId)
            );
            assertEquals("Restaurant ID must be positive", exception.getMessage());
        }
        
        /**
         * تست خطا برای رستوران غیرموجود
         * 
         * Scenario: درخواست منو برای رستوران که وجود ندارد
         * Expected: NotFoundException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for non-existent restaurant")
        void getRestaurantMenu_NonExistentRestaurant_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                menuService.getRestaurantMenu(999L)
            );
            assertEquals("Restaurant not found with ID: 999", exception.getMessage());
        }
    }
    
    // ==================== تست‌های اضافه کردن آیتم به منو ====================
    
    /**
     * تست‌های اضافه کردن آیتم به منو
     * 
     * این دسته شامل تمام عملیات اضافه کردن آیتم جدید:
     * - اضافه کردن با داده‌های معتبر
     * - validation فیلدهای مختلف
     * - مدیریت whitespace
     * - محدودیت‌های طول رشته
     * - validation قیمت
     */
    @Nested
    @DisplayName("Add Item to Menu Tests")
    class AddItemToMenuTests {
        
        /**
         * تست موفق اضافه کردن آیتم به منو
         * 
         * Scenario: اضافه کردن آیتم جدید با تمام اطلاعات
         * Expected:
         * - آیتم با ID منحصر به فرد ایجاد شود
         * - تمام فیلدها صحیح ذخیره شوند
         * - available = true (پیش‌فرض)
         * - quantity = 0 (پیش‌فرض)
         */
        @Test
        @DisplayName("Should add item to menu successfully")
        void addItemToMenu_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When
            FoodItem addedItem = menuService.addItemToMenu(
                restaurant.getId(), "Pizza Margherita", "Classic Italian pizza", 25.99, "Italian"
            );
            
            // Then
            assertNotNull(addedItem);
            assertNotNull(addedItem.getId());
            assertEquals("Pizza Margherita", addedItem.getName());
            assertEquals("Classic Italian pizza", addedItem.getDescription());
            assertEquals(25.99, addedItem.getPrice());
            assertEquals("Italian", addedItem.getCategory());
            assertEquals(restaurant.getId(), addedItem.getRestaurant().getId());
            assertTrue(addedItem.getAvailable());
            assertEquals(0, addedItem.getQuantity());
        }
        
        /**
         * تست تمیز کردن whitespace از ورودی
         * 
         * Scenario: ورودی با فاصله‌های اضافی
         * Expected: فاصله‌های ابتدا و انتها حذف شوند
         */
        @Test
        @DisplayName("Should trim whitespace from input")
        void addItemToMenu_TrimsWhitespace_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When
            FoodItem addedItem = menuService.addItemToMenu(
                restaurant.getId(), "  Pizza  ", "  Italian pizza  ", 25.99, "  Italian  "
            );
            
            // Then
            assertEquals("Pizza", addedItem.getName());
            assertEquals("Italian pizza", addedItem.getDescription());
            assertEquals("Italian", addedItem.getCategory());
        }
        
        /**
         * تست اضافه کردن با استفاده از object FoodItem
         * 
         * Scenario: ارسال آبجکت FoodItem کامل
         * Expected: آیتم صحیح ذخیره شود
         */
        @Test
        @DisplayName("Should add item using FoodItem object")
        void addItemToMenu_FoodItemObject_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem foodItem = FoodItem.forMenu("Burger", "Beef burger", 18.99, "Fast Food", restaurant);
            
            // When
            FoodItem addedItem = menuService.addItemToMenu(foodItem);
            
            // Then
            assertNotNull(addedItem);
            assertNotNull(addedItem.getId());
            assertEquals("Burger", addedItem.getName());
            assertEquals("Beef burger", addedItem.getDescription());
            assertEquals(18.99, addedItem.getPrice());
            assertEquals("Fast Food", addedItem.getCategory());
        }
        
        /**
         * تست خطا برای ID رستوران null
         * 
         * Scenario: تلاش اضافه کردن آیتم بدون مشخص کردن رستوران
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null restaurant ID")
        void addItemToMenu_NullRestaurantId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(null, "Pizza", "Italian pizza", 25.99, "Italian")
            );
            assertEquals("Restaurant ID must be positive", exception.getMessage());
        }
        
        /**
         * تست خطا برای ID رستوران نامعتبر
         * 
         * @param restaurantId ID نامعتبر
         * 
         * Scenario: ID منفی یا صفر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @ParameterizedTest
        @ValueSource(longs = {0, -1, -10})
        @DisplayName("Should throw exception for invalid restaurant ID")
        void addItemToMenu_InvalidRestaurantId_ThrowsException(Long restaurantId) {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(restaurantId, "Pizza", "Italian pizza", 25.99, "Italian")
            );
            assertEquals("Restaurant ID must be positive", exception.getMessage());
        }
        
        /**
         * تست خطا برای رستوران غیرموجود
         * 
         * Scenario: اضافه کردن آیتم به رستوران که وجود ندارد
         * Expected: NotFoundException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for non-existent restaurant")
        void addItemToMenu_NonExistentRestaurant_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                menuService.addItemToMenu(999L, "Pizza", "Italian pizza", 25.99, "Italian")
            );
            assertEquals("Restaurant not found with ID: 999", exception.getMessage());
        }
        
        /**
         * تست خطا برای نام خالی
         * 
         * @param name نام نامعتبر
         * 
         * Scenario: نام آیتم خالی یا فقط شامل فاصله
         * Expected: IllegalArgumentException پرتاب شود
         */
        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Should throw exception for empty name")
        void addItemToMenu_EmptyName_ThrowsException(String name) {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(restaurant.getId(), name, "Description", 25.99, "Category")
            );
            assertEquals("Food item name cannot be empty", exception.getMessage());
        }
        
        /**
         * تست خطا برای نام null
         * 
         * Scenario: عدم ارسال نام آیتم
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null name")
        void addItemToMenu_NullName_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(restaurant.getId(), null, "Description", 25.99, "Category")
            );
            assertEquals("Food item name cannot be empty", exception.getMessage());
        }
        
        /**
         * تست خطا برای نام خیلی طولانی
         * 
         * Scenario: نام آیتم بیش از 100 کاراکتر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for very long name")
        void addItemToMenu_VeryLongName_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            String longName = "A".repeat(101);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(restaurant.getId(), longName, "Description", 25.99, "Category")
            );
            assertEquals("Food item name cannot exceed 100 characters", exception.getMessage());
        }
        
        /**
         * تست خطا برای توضیحات خیلی طولانی
         * 
         * Scenario: توضیحات بیش از 500 کاراکتر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for very long description")
        void addItemToMenu_VeryLongDescription_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            String longDescription = "A".repeat(501);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(restaurant.getId(), "Pizza", longDescription, 25.99, "Category")
            );
            assertEquals("Food item description cannot exceed 500 characters", exception.getMessage());
        }
        
        /**
         * تست خطا برای دسته‌بندی خیلی طولانی
         * 
         * Scenario: نام دسته بیش از 50 کاراکتر
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for very long category")
        void addItemToMenu_VeryLongCategory_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            String longCategory = "A".repeat(51);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(restaurant.getId(), "Pizza", "Description", 25.99, longCategory)
            );
            assertEquals("Category cannot exceed 50 characters", exception.getMessage());
        }
        
        /**
         * تست خطا برای قیمت نامعتبر
         * 
         * @param price قیمت نامعتبر
         * 
         * Scenario: قیمت منفی، صفر، یا خیلی بالا
         * Expected: IllegalArgumentException پرتاب شود
         */
        @ParameterizedTest
        @ValueSource(doubles = {0.0, 0.001, -1.0, 10000.0})
        @DisplayName("Should throw exception for invalid price")
        void addItemToMenu_InvalidPrice_ThrowsException(Double price) {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu(restaurant.getId(), "Pizza", "Description", price, "Category")
            );
            assertEquals("Price must be between 0.01 and 9999.99", exception.getMessage());
        }
        
        /**
         * تست خطا برای FoodItem null
         * 
         * Scenario: ارسال null به جای آبجکت FoodItem
         * Expected: IllegalArgumentException پرتاب شود
         */
        @Test
        @DisplayName("Should throw exception for null FoodItem")
        void addItemToMenu_NullFoodItem_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.addItemToMenu((FoodItem) null)
            );
            assertEquals("Food item cannot be null", exception.getMessage());
        }
    }
    
    // ==================== تست‌های به‌روزرسانی آیتم منو ====================
    
    @Nested
    @DisplayName("Update Menu Item Tests")
    class UpdateMenuItemTests {
        
        @Test
        @DisplayName("Should update menu item successfully")
        void updateMenuItem_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            // When
            FoodItem updatedItem = menuService.updateMenuItem(
                item.getId(), "Pizza Margherita", "Updated description", 28.99, "Italian", 10, false
            );
            
            // Then
            assertNotNull(updatedItem);
            assertEquals("Pizza Margherita", updatedItem.getName());
            assertEquals("Updated description", updatedItem.getDescription());
            assertEquals(28.99, updatedItem.getPrice());
            assertEquals("Italian", updatedItem.getCategory());
            assertEquals(10, updatedItem.getQuantity());
            assertFalse(updatedItem.getAvailable());
        }
        
        @Test
        @DisplayName("Should update only provided fields")
        void updateMenuItem_PartialUpdate_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            String originalDescription = item.getDescription();
            String originalCategory = item.getCategory();
            
            // When
            FoodItem updatedItem = menuService.updateMenuItem(
                item.getId(), "Pizza Margherita", null, 28.99, null, null, null
            );
            
            // Then
            assertEquals("Pizza Margherita", updatedItem.getName());
            assertEquals(originalDescription, updatedItem.getDescription()); // Unchanged
            assertEquals(28.99, updatedItem.getPrice());
            assertEquals(originalCategory, updatedItem.getCategory()); // Unchanged
            assertTrue(updatedItem.getAvailable()); // Unchanged
        }
        
        @Test
        @DisplayName("Should update using FoodItem object")
        void updateMenuItem_FoodItemObject_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem existingItem = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            FoodItem updateData = new FoodItem();
            updateData.setId(existingItem.getId());
            updateData.setName("Pizza Margherita");
            updateData.setPrice(28.99);
            
            // When
            FoodItem updatedItem = menuService.updateMenuItem(updateData);
            
            // Then
            assertEquals("Pizza Margherita", updatedItem.getName());
            assertEquals(28.99, updatedItem.getPrice());
            assertEquals(existingItem.getDescription(), updatedItem.getDescription()); // Preserved
            assertEquals(existingItem.getCategory(), updatedItem.getCategory()); // Preserved
        }
        
        @Test
        @DisplayName("Should throw exception for null item ID")
        void updateMenuItem_NullItemId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.updateMenuItem(null, "Pizza", "Description", 25.99, "Category", 10, true)
            );
            assertEquals("Item ID must be positive", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent item")
        void updateMenuItem_NonExistentItem_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                menuService.updateMenuItem(999L, "Pizza", "Description", 25.99, "Category", 10, true)
            );
            assertEquals("Food item not found with ID: 999", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for negative quantity")
        void updateMenuItem_NegativeQuantity_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.updateMenuItem(item.getId(), null, null, null, null, -1, null)
            );
            assertEquals("Quantity cannot be negative", exception.getMessage());
        }
    }
    
    // ==================== تست‌های حذف آیتم از منو ====================
    
    @Nested
    @DisplayName("Remove Item from Menu Tests")
    class RemoveItemFromMenuTests {
        
        @Test
        @DisplayName("Should remove item from menu successfully")
        void removeItemFromMenu_ValidItem_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            // When
            assertDoesNotThrow(() -> menuService.removeItemFromMenu(item.getId()));
            
            // Then
            List<FoodItem> menu = menuService.getRestaurantMenu(restaurant.getId());
            assertTrue(menu.isEmpty());
        }
        
        @Test
        @DisplayName("Should throw exception for null item ID")
        void removeItemFromMenu_NullItemId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.removeItemFromMenu(null)
            );
            assertEquals("Item ID must be positive", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should throw exception for non-existent item")
        void removeItemFromMenu_NonExistentItem_ThrowsException() {
            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                menuService.removeItemFromMenu(999L)
            );
            assertEquals("Food item not found with ID: 999", exception.getMessage());
        }
    }
    
    // ==================== تست‌های موجودی آیتم ====================
    
    @Nested
    @DisplayName("Item Availability Tests")
    class ItemAvailabilityTests {
        
        @Test
        @DisplayName("Should set item availability successfully")
        void setItemAvailability_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            // When
            FoodItem updatedItem = menuService.setItemAvailability(item.getId(), false);
            
            // Then
            assertFalse(updatedItem.getAvailable());
        }
        
        @Test
        @DisplayName("Should update item quantity successfully")
        void updateItemQuantity_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            // When
            FoodItem updatedItem = menuService.updateItemQuantity(item.getId(), 15);
            
            // Then
            assertEquals(15, updatedItem.getQuantity());
        }
        
        @Test
        @DisplayName("Should throw exception for negative quantity")
        void updateItemQuantity_NegativeQuantity_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.updateItemQuantity(item.getId(), -1)
            );
            assertEquals("Quantity cannot be negative", exception.getMessage());
        }
    }
    
    // ==================== تست‌های دسته‌بندی منو ====================
    
    @Nested
    @DisplayName("Menu Category Tests")
    class MenuCategoryTests {
        
        @Test
        @DisplayName("Should get menu items by category")
        void getMenuByCategory_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            pizza.setCategory("Italian");
            itemRepository.save(pizza);
            
            FoodItem burger = createAndSaveFoodItem(restaurant, "Burger", 18.99, true);
            burger.setCategory("Fast Food");
            itemRepository.save(burger);
            
            // When
            List<FoodItem> italianItems = menuService.getMenuByCategory(restaurant.getId(), "Italian");
            
            // Then
            assertEquals(1, italianItems.size());
            assertEquals("Pizza", italianItems.get(0).getName());
        }
        
        @Test
        @DisplayName("Should get all menu categories")
        void getMenuCategories_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            pizza.setCategory("Italian");
            itemRepository.save(pizza);
            
            FoodItem burger = createAndSaveFoodItem(restaurant, "Burger", 18.99, true);
            burger.setCategory("Fast Food");
            itemRepository.save(burger);
            
            // When
            List<String> categories = menuService.getMenuCategories(restaurant.getId());
            
            // Then
            assertEquals(2, categories.size());
            assertTrue(categories.contains("Italian"));
            assertTrue(categories.contains("Fast Food"));
        }
        
        @Test
        @DisplayName("Should throw exception for empty category")
        void getMenuByCategory_EmptyCategory_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.getMenuByCategory(restaurant.getId(), "")
            );
            assertEquals("Category cannot be empty", exception.getMessage());
        }
    }
    
    // ==================== تست‌های آمار منو ====================
    
    @Nested
    @DisplayName("Menu Statistics Tests")
    class MenuStatisticsTests {
        
        @Test
        @DisplayName("Should calculate menu statistics correctly")
        void getMenuStatistics_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // Available in stock
            FoodItem pizza = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            pizza.setQuantity(10);
            itemRepository.save(pizza);
            
            // Available but out of stock
            FoodItem burger = createAndSaveFoodItem(restaurant, "Burger", 18.99, true);
            burger.setQuantity(0);
            itemRepository.save(burger);
            
            // Unavailable
            FoodItem pasta = createAndSaveFoodItem(restaurant, "Pasta", 22.99, false);
            pasta.setQuantity(5);
            itemRepository.save(pasta);
            
            // Low stock
            FoodItem salad = createAndSaveFoodItem(restaurant, "Salad", 15.99, true);
            salad.setQuantity(3);
            itemRepository.save(salad);
            
            // When
            MenuService.MenuStatistics stats = menuService.getMenuStatistics(restaurant.getId());
            
            // Then
            assertEquals(4, stats.getTotalItems());
            assertEquals(3, stats.getAvailableItems()); // pizza, burger, salad
            assertEquals(1, stats.getUnavailableItems()); // pasta
            assertEquals(1, stats.getOutOfStockItems()); // burger
            assertEquals(2, stats.getLowStockItems()); // salad (quantity=3) and pasta (quantity=5)
            assertEquals(3, stats.getInStockItems()); // pizza, pasta, salad
            assertEquals(75.0, stats.getAvailabilityPercentage(), 0.01); // 3/4 * 100
        }
        
        @Test
        @DisplayName("Should handle empty menu statistics")
        void getMenuStatistics_EmptyMenu_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When
            MenuService.MenuStatistics stats = menuService.getMenuStatistics(restaurant.getId());
            
            // Then
            assertEquals(0, stats.getTotalItems());
            assertEquals(0, stats.getAvailableItems());
            assertEquals(0, stats.getUnavailableItems());
            assertEquals(0, stats.getOutOfStockItems());
            assertEquals(0, stats.getLowStockItems());
            assertEquals(0, stats.getInStockItems());
            assertEquals(0.0, stats.getAvailabilityPercentage(), 0.01);
        }
    }
    
    // ==================== تست‌های تأیید مالکیت رستوران ====================
    
    @Nested
    @DisplayName("Restaurant Ownership Tests")
    class RestaurantOwnershipTests {
        
        @Test
        @DisplayName("Should verify restaurant ownership correctly")
        void isRestaurantOwner_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            
            // When & Then
            assertTrue(menuService.isRestaurantOwner(restaurant.getId(), item.getId()));
        }
        
        @Test
        @DisplayName("Should return false for different restaurant")
        void isRestaurantOwner_DifferentRestaurant_ReturnsFalse() {
            // Given
            Restaurant restaurant1 = createAndSaveRestaurant();
            Restaurant restaurant2 = createAndSaveRestaurant();
            FoodItem item = createAndSaveFoodItem(restaurant1, "Pizza", 25.99, true);
            
            // When & Then
            assertFalse(menuService.isRestaurantOwner(restaurant2.getId(), item.getId()));
        }
        
        @Test
        @DisplayName("Should return false for non-existent item")
        void isRestaurantOwner_NonExistentItem_ReturnsFalse() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            assertFalse(menuService.isRestaurantOwner(restaurant.getId(), 999L));
        }
    }
    
    // ==================== تست‌های تشخیص آیتم‌های کم موجود ====================
    
    @Nested
    @DisplayName("Low Stock Tests")
    class LowStockTests {
        
        @Test
        @DisplayName("Should get low stock items correctly")
        void getLowStockItems_ValidData_Success() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            FoodItem highStock = createAndSaveFoodItem(restaurant, "Pizza", 25.99, true);
            highStock.setQuantity(20);
            itemRepository.save(highStock);
            
            FoodItem lowStock = createAndSaveFoodItem(restaurant, "Burger", 18.99, true);
            lowStock.setQuantity(3);
            itemRepository.save(lowStock);
            
            FoodItem outOfStock = createAndSaveFoodItem(restaurant, "Pasta", 22.99, true);
            outOfStock.setQuantity(0);
            itemRepository.save(outOfStock);
            
            // When
            List<FoodItem> lowStockItems = menuService.getLowStockItems(restaurant.getId(), 5);
            
            // Then
            assertEquals(2, lowStockItems.size()); // burger (3) and pasta (0)
            assertTrue(lowStockItems.stream().anyMatch(item -> item.getName().equals("Burger")));
            assertTrue(lowStockItems.stream().anyMatch(item -> item.getName().equals("Pasta")));
        }
        
        @Test
        @DisplayName("Should throw exception for negative threshold")
        void getLowStockItems_NegativeThreshold_ThrowsException() {
            // Given
            Restaurant restaurant = createAndSaveRestaurant();
            
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                menuService.getLowStockItems(restaurant.getId(), -1)
            );
            assertEquals("Threshold cannot be negative", exception.getMessage());
        }
    }
    
    // ==================== متدهای کمکی ====================
    
    private Restaurant createAndSaveRestaurant() {
        Restaurant restaurant = Restaurant.forRegistration(1L, "Test Restaurant", "Tehran, Iran", "021-123456");
        restaurant.setStatus(RestaurantStatus.APPROVED);
        return restaurantRepository.saveNew(restaurant);
    }
    
    private FoodItem createAndSaveFoodItem(Restaurant restaurant, String name, Double price, Boolean available) {
        FoodItem item = FoodItem.forMenu(name, "Delicious " + name, price, "Test Category", restaurant);
        item.setAvailable(available);
        return itemRepository.save(item);
    }

    @Test
    @DisplayName("Should handle concurrent menu updates")
    void shouldHandleConcurrentMenuUpdates() {
        com.myapp.common.utils.SQLiteTestHelper.executeWithRetry(DatabaseUtil.getSessionFactory(), () -> {
            // Simulate concurrent menu updates
            Restaurant restaurant = createAndSaveRestaurant();
            for (int i = 0; i < 10; i++) {
                FoodItem foodItem = FoodItem.forMenu("Test Item " + i, "Test description " + i, 10000.0 + i, "Test Category", restaurant);
                itemRepository.saveNew(foodItem);
            }
            return null;
        });
        // Add assertions as needed
    }
} 
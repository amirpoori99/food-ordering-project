package com.myapp.menu;

import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.item.ItemRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * مجموعه تست‌های کامل MenuRepository
 * 
 * این کلاس تست عملکردهای اصلی repository مدیریت منو را آزمایش می‌کند:
 * 
 * Test Categories:
 * 1. Basic Repository Tests - تست‌های پایه repository
 * 2. Menu CRUD Operations - عملیات CRUD منو  
 * 3. Menu Retrieval Operations - عملیات دریافت منو
 * 4. Inventory Management - مدیریت موجودی
 * 5. Utility Operations - عملیات کمکی
 * 6. Edge Cases - حالات استثنایی
 * 
 * @author Food Ordering System Team
 * @version 2.0 (Enhanced Coverage)
 * @since 2024
 */
@DisplayName("MenuRepository Comprehensive Tests")
class MenuRepositoryTest {

    @Mock
    private ItemRepository mockItemRepository;
    
    private MenuRepository menuRepository;
    private Restaurant testRestaurant;
    private FoodItem testFoodItem1, testFoodItem2, testFoodItem3;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        menuRepository = new MenuRepository(mockItemRepository);
        
        // ایجاد داده‌های تست
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("تست رستوران");
        
        testFoodItem1 = new FoodItem();
        testFoodItem1.setId(1L);
        testFoodItem1.setName("پیتزا مارگاریتا");
        testFoodItem1.setPrice(25.99);
        testFoodItem1.setCategory("پیتزا");
        testFoodItem1.setAvailable(true);
        testFoodItem1.setQuantity(10);
        testFoodItem1.setRestaurant(testRestaurant);
        
        testFoodItem2 = new FoodItem();
        testFoodItem2.setId(2L);
        testFoodItem2.setName("برگر کلاسیک");
        testFoodItem2.setPrice(18.50);
        testFoodItem2.setCategory("برگر");
        testFoodItem2.setAvailable(true);
        testFoodItem2.setQuantity(5);
        testFoodItem2.setRestaurant(testRestaurant);
        
        testFoodItem3 = new FoodItem();
        testFoodItem3.setId(3L);
        testFoodItem3.setName("نوشابه کولا");
        testFoodItem3.setPrice(3.99);
        testFoodItem3.setCategory("نوشیدنی");
        testFoodItem3.setAvailable(false);
        testFoodItem3.setQuantity(0);
        testFoodItem3.setRestaurant(testRestaurant);
    }

    // ==================== BASIC REPOSITORY TESTS ====================
    
    /**
     * تست ایجاد repository منو
     */
    @Test
    @DisplayName("Should create menu repository successfully")
    void createMenuRepository_Success() {
        MenuRepository repository = new MenuRepository();
        assertThat(repository).isNotNull();
    }

    /**
     * تست ایجاد repository با dependency injection
     */
    @Test
    @DisplayName("Should create menu repository with injected dependency")
    void createMenuRepositoryWithDependency_Success() {
        ItemRepository itemRepo = mock(ItemRepository.class);
        MenuRepository repository = new MenuRepository(itemRepo);
        assertThat(repository).isNotNull();
    }

    // ==================== MENU CRUD OPERATIONS ====================

    /**
     * تست افزودن آیتم به منو
     */
    @Test
    @DisplayName("Should add new menu item successfully")
    void addMenuItem_ValidItem_Success() {
        when(mockItemRepository.saveNew(testFoodItem1)).thenReturn(testFoodItem1);
        
        FoodItem result = menuRepository.addMenuItem(testFoodItem1);
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("پیتزا مارگاریتا");
        verify(mockItemRepository).saveNew(testFoodItem1);
    }

    /**
     * تست به‌روزرسانی آیتم منو
     */
    @Test
    @DisplayName("Should update existing menu item successfully")
    void updateMenuItem_ValidItem_Success() {
        testFoodItem1.setPrice(29.99);
        when(mockItemRepository.save(testFoodItem1)).thenReturn(testFoodItem1);
        
        FoodItem result = menuRepository.updateMenuItem(testFoodItem1);
        
        assertThat(result).isNotNull();
        assertThat(result.getPrice()).isEqualTo(29.99);
        verify(mockItemRepository).save(testFoodItem1);
    }

    /**
     * تست دریافت آیتم منو بر اساس ID
     */
    @Test
    @DisplayName("Should get menu item by ID successfully")
    void getMenuItem_ValidId_ReturnsItem() {
        when(mockItemRepository.findById(1L)).thenReturn(Optional.of(testFoodItem1));
        
        Optional<FoodItem> result = menuRepository.getMenuItem(1L);
        
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("پیتزا مارگاریتا");
        verify(mockItemRepository).findById(1L);
    }

    /**
     * تست دریافت آیتم غیرموجود
     */
    @Test
    @DisplayName("Should return empty for non-existent item")
    void getMenuItem_InvalidId_ReturnsEmpty() {
        when(mockItemRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<FoodItem> result = menuRepository.getMenuItem(999L);
        
        assertThat(result).isEmpty();
        verify(mockItemRepository).findById(999L);
    }

    /**
     * تست حذف آیتم از منو
     */
    @Test
    @DisplayName("Should remove menu item successfully")
    void removeMenuItem_ValidId_Success() {
        doNothing().when(mockItemRepository).delete(1L);
        
        menuRepository.removeMenuItem(1L);
        
        verify(mockItemRepository).delete(1L);
    }

    // ==================== MENU RETRIEVAL OPERATIONS ====================

    /**
     * تست دریافت منوی کامل رستوران
     */
    @Test
    @DisplayName("Should get complete restaurant menu")
    void getMenuByRestaurant_ValidId_ReturnsAllItems() {
        List<FoodItem> allItems = Arrays.asList(testFoodItem1, testFoodItem2, testFoodItem3);
        when(mockItemRepository.findByRestaurant(1L)).thenReturn(allItems);
        
        List<FoodItem> result = menuRepository.getMenuByRestaurant(1L);
        
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(testFoodItem1, testFoodItem2, testFoodItem3);
        verify(mockItemRepository).findByRestaurant(1L);
    }

    /**
     * تست دریافت آیتم‌های در دسترس
     */
    @Test
    @DisplayName("Should get only available menu items")
    void getAvailableMenuByRestaurant_ValidId_ReturnsAvailableItems() {
        List<FoodItem> availableItems = Arrays.asList(testFoodItem1, testFoodItem2);
        when(mockItemRepository.findAvailableByRestaurant(1L)).thenReturn(availableItems);
        
        List<FoodItem> result = menuRepository.getAvailableMenuByRestaurant(1L);
        
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testFoodItem1, testFoodItem2);
        assertThat(result).allMatch(FoodItem::getAvailable);
        verify(mockItemRepository).findAvailableByRestaurant(1L);
    }

    /**
     * تست دریافت آیتم‌های بر اساس دسته‌بندی
     */
    @Test
    @DisplayName("Should get menu items by category")
    void getMenuByCategory_ValidCategory_ReturnsFilteredItems() {
        List<FoodItem> pizzaItems = Arrays.asList(testFoodItem1);
        when(mockItemRepository.findByRestaurantAndCategory(1L, "پیتزا")).thenReturn(pizzaItems);
        
        List<FoodItem> result = menuRepository.getMenuByCategory(1L, "پیتزا");
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("پیتزا");
        verify(mockItemRepository).findByRestaurantAndCategory(1L, "پیتزا");
    }

    /**
     * تست جستجوی آیتم‌ها
     */
    @Test
    @DisplayName("Should search menu items by keyword")
    void searchMenu_ValidKeyword_ReturnsMatchingItems() {
        List<FoodItem> searchResults = Arrays.asList(testFoodItem1);
        when(mockItemRepository.searchByKeyword("پیتزا")).thenReturn(searchResults);
        
        List<FoodItem> result = menuRepository.searchMenu("پیتزا");
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("پیتزا");
        verify(mockItemRepository).searchByKeyword("پیتزا");
    }

    // ==================== INVENTORY MANAGEMENT ====================

    /**
     * تست به‌روزرسانی وضعیت در دسترس بودن
     */
    @Test
    @DisplayName("Should update item availability")
    void updateItemAvailability_ValidParams_Success() {
        doNothing().when(mockItemRepository).updateAvailability(1L, false);
        
        menuRepository.updateItemAvailability(1L, false);
        
        verify(mockItemRepository).updateAvailability(1L, false);
    }

    /**
     * تست به‌روزرسانی موجودی آیتم
     */
    @Test
    @DisplayName("Should update item quantity")
    void updateItemQuantity_ValidParams_Success() {
        doNothing().when(mockItemRepository).updateQuantity(1L, 20);
        
        menuRepository.updateItemQuantity(1L, 20);
        
        verify(mockItemRepository).updateQuantity(1L, 20);
    }

    /**
     * تست دریافت آیتم‌های کم موجودی
     */
    @Test
    @DisplayName("Should get low stock items")
    void getLowStockItems_ValidThreshold_ReturnsLowStockItems() {
        List<FoodItem> lowStockItems = Arrays.asList(testFoodItem2);
        when(mockItemRepository.findLowStockByRestaurant(1L, 10)).thenReturn(lowStockItems);
        
        List<FoodItem> result = menuRepository.getLowStockItems(1L, 10);
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isLessThan(10);
        verify(mockItemRepository).findLowStockByRestaurant(1L, 10);
    }

    // ==================== UTILITY OPERATIONS ====================

    /**
     * تست بررسی وجود آیتم در منو
     */
    @Test
    @DisplayName("Should check if restaurant has menu items")
    void hasMenuItems_RestaurantWithItems_ReturnsTrue() {
        List<FoodItem> items = Arrays.asList(testFoodItem1, testFoodItem2);
        when(mockItemRepository.findByRestaurant(1L)).thenReturn(items);
        
        boolean result = menuRepository.hasMenuItems(1L);
        
        assertThat(result).isTrue();
        verify(mockItemRepository).findByRestaurant(1L);
    }

    /**
     * تست بررسی منوی خالی
     */
    @Test
    @DisplayName("Should return false for restaurant with no menu items")
    void hasMenuItems_RestaurantWithoutItems_ReturnsFalse() {
        when(mockItemRepository.findByRestaurant(1L)).thenReturn(Arrays.asList());
        
        boolean result = menuRepository.hasMenuItems(1L);
        
        assertThat(result).isFalse();
        verify(mockItemRepository).findByRestaurant(1L);
    }

    /**
     * تست شمارش آیتم‌های در دسترس
     */
    @Test
    @DisplayName("Should count available items correctly")
    void getAvailableItemCount_ValidRestaurant_ReturnsCorrectCount() {
        List<FoodItem> availableItems = Arrays.asList(testFoodItem1, testFoodItem2);
        when(mockItemRepository.findAvailableByRestaurant(1L)).thenReturn(availableItems);
        
        long result = menuRepository.getAvailableItemCount(1L);
        
        assertThat(result).isEqualTo(2);
        verify(mockItemRepository).findAvailableByRestaurant(1L);
    }

    /**
     * تست دریافت دسته‌بندی‌های منو
     */
    @Test
    @DisplayName("Should get menu categories")
    void getCategories_ValidRestaurant_ReturnsCategories() {
        List<String> categories = Arrays.asList("پیتزا", "برگر", "نوشیدنی");
        when(mockItemRepository.getCategoriesByRestaurant(1L)).thenReturn(categories);
        
        List<String> result = menuRepository.getCategories(1L);
        
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("پیتزا", "برگر", "نوشیدنی");
        verify(mockItemRepository).getCategoriesByRestaurant(1L);
    }

    // ==================== EDGE CASES ====================

    /**
     * تست رفتار با لیست خالی
     */
    @Test
    @DisplayName("Should handle empty menu gracefully")
    void getMenuByRestaurant_EmptyMenu_ReturnsEmptyList() {
        when(mockItemRepository.findByRestaurant(1L)).thenReturn(Arrays.asList());
        
        List<FoodItem> result = menuRepository.getMenuByRestaurant(1L);
        
        assertThat(result).isEmpty();
        verify(mockItemRepository).findByRestaurant(1L);
    }

    /**
     * تست جستجو با کلیدواژه نامطابق
     */
    @Test
    @DisplayName("Should return empty list for no matching search results")
    void searchMenu_NoMatches_ReturnsEmptyList() {
        when(mockItemRepository.searchByKeyword("غذای نامطابق")).thenReturn(Arrays.asList());
        
        List<FoodItem> result = menuRepository.searchMenu("غذای نامطابق");
        
        assertThat(result).isEmpty();
        verify(mockItemRepository).searchByKeyword("غذای نامطابق");
    }
} 
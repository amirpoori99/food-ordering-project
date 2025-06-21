package com.myapp.menu;

import com.myapp.common.models.FoodItem;
import com.myapp.item.ItemRepository;

import java.util.List;
import java.util.Optional;

/**
 * MenuRepository - Wrapper around ItemRepository for menu-specific operations
 * Provides restaurant menu management functionality
 */
public class MenuRepository {
    
    private final ItemRepository itemRepository;
    
    public MenuRepository() {
        this.itemRepository = new ItemRepository();
    }
    
    public MenuRepository(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    /**
     * Add new food item to restaurant menu
     */
    public FoodItem addMenuItem(FoodItem foodItem) {
        return itemRepository.saveNew(foodItem);
    }
    
    /**
     * Update existing menu item
     */
    public FoodItem updateMenuItem(FoodItem foodItem) {
        return itemRepository.save(foodItem);
    }
    
    /**
     * Get full menu for a restaurant
     */
    public List<FoodItem> getMenuByRestaurant(Long restaurantId) {
        return itemRepository.findByRestaurant(restaurantId);
    }
    
    /**
     * Get available menu items for a restaurant (in stock and available)
     */
    public List<FoodItem> getAvailableMenuByRestaurant(Long restaurantId) {
        return itemRepository.findAvailableByRestaurant(restaurantId);
    }
    
    /**
     * Get menu items by category for a restaurant
     */
    public List<FoodItem> getMenuByCategory(Long restaurantId, String category) {
        return itemRepository.findByRestaurantAndCategory(restaurantId, category);
    }
    
    /**
     * Search menu items by keyword
     */
    public List<FoodItem> searchMenu(String keyword) {
        return itemRepository.searchByKeyword(keyword);
    }
    
    /**
     * Get menu item by ID
     */
    public Optional<FoodItem> getMenuItem(Long itemId) {
        return itemRepository.findById(itemId);
    }
    
    /**
     * Remove item from menu
     */
    public void removeMenuItem(Long itemId) {
        itemRepository.delete(itemId);
    }
    
    /**
     * Update item availability
     */
    public void updateItemAvailability(Long itemId, boolean available) {
        itemRepository.updateAvailability(itemId, available);
    }
    
    /**
     * Update item quantity
     */
    public void updateItemQuantity(Long itemId, Integer quantity) {
        itemRepository.updateQuantity(itemId, quantity);
    }
    
    /**
     * Check if restaurant has any menu items
     */
    public boolean hasMenuItems(Long restaurantId) {
        return !getMenuByRestaurant(restaurantId).isEmpty();
    }
    
    /**
     * Get count of available items in restaurant
     */
    public long getAvailableItemCount(Long restaurantId) {
        return getAvailableMenuByRestaurant(restaurantId).size();
    }
    
    /**
     * Get all categories for a restaurant menu
     */
    public List<String> getCategories(Long restaurantId) {
        return itemRepository.getCategoriesByRestaurant(restaurantId);
    }
    
    /**
     * Get low stock items for a restaurant
     */
    public List<FoodItem> getLowStockItems(Long restaurantId, int threshold) {
        return itemRepository.findLowStockByRestaurant(restaurantId, threshold);
    }
}

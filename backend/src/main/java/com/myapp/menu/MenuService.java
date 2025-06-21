package com.myapp.menu;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.item.ItemRepository;
import com.myapp.restaurant.RestaurantRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service class for menu management operations.
 * Handles menu operations for restaurant vendors.
 */
public class MenuService {
    
    private final MenuRepository menuRepository;
    private final ItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;
    
    public MenuService() {
        this.menuRepository = new MenuRepository();
        this.itemRepository = new ItemRepository();
        this.restaurantRepository = new RestaurantRepository();
    }
    
    // Constructor for dependency injection (testing)
    public MenuService(MenuRepository menuRepository, ItemRepository itemRepository, RestaurantRepository restaurantRepository) {
        this.menuRepository = menuRepository;
        this.itemRepository = itemRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * Get complete menu for a restaurant
     */
    public List<FoodItem> getRestaurantMenu(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getMenuByRestaurant(restaurantId);
    }
    
    /**
     * Get only available menu items for a restaurant
     */
    public List<FoodItem> getAvailableMenu(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getAvailableMenuByRestaurant(restaurantId);
    }
    
    /**
     * Add a new item to restaurant menu
     */
    public FoodItem addItemToMenu(Long restaurantId, String name, String description, Double price, String category) {
        validateAddItemInput(restaurantId, name, description, price, category);
        
        // Verify restaurant exists and get it
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        Restaurant restaurant = restaurantOpt.get();
        
        // Create new food item
        FoodItem foodItem = FoodItem.forMenu(name.trim(), description.trim(), price, category.trim(), restaurant);
        // Set initial quantity to 0 - vendor should explicitly set inventory
        foodItem.setQuantity(0);
        
        return itemRepository.save(foodItem);
    }
    
    /**
     * Add item to menu using FoodItem object
     */
    public FoodItem addItemToMenu(FoodItem foodItem) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        if (foodItem.getRestaurant() == null || foodItem.getRestaurant().getId() == null) {
            throw new IllegalArgumentException("Food item must have a valid restaurant");
        }
        
        validateAddItemInput(foodItem.getRestaurant().getId(), foodItem.getName(), 
                           foodItem.getDescription(), foodItem.getPrice(), foodItem.getCategory());
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(foodItem.getRestaurant().getId())) {
            throw new NotFoundException("Restaurant", foodItem.getRestaurant().getId());
        }
        
        return itemRepository.save(foodItem);
    }
    
    /**
     * Update an existing menu item
     */
    public FoodItem updateMenuItem(Long itemId, String name, String description, Double price, String category, Integer quantity, Boolean available) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        // Get existing item
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem existingItem = itemOpt.get();
        
        // Update fields if provided
        if (name != null && !name.trim().isEmpty()) {
            if (name.trim().length() > 100) {
                throw new IllegalArgumentException("Food item name cannot exceed 100 characters");
            }
            existingItem.setName(name.trim());
        }
        
        if (description != null && !description.trim().isEmpty()) {
            if (description.trim().length() > 500) {
                throw new IllegalArgumentException("Food item description cannot exceed 500 characters");
            }
            existingItem.setDescription(description.trim());
        }
        
        if (price != null) {
            if (price < 0.01 || price > 9999.99) {
                throw new IllegalArgumentException("Price must be between 0.01 and 9999.99");
            }
            existingItem.setPrice(price);
        }
        
        if (category != null && !category.trim().isEmpty()) {
            if (category.trim().length() > 50) {
                throw new IllegalArgumentException("Category cannot exceed 50 characters");
            }
            existingItem.setCategory(category.trim());
        }
        
        if (quantity != null) {
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            existingItem.setQuantity(quantity);
        }
        
        if (available != null) {
            existingItem.setAvailable(available);
        }
        
        return itemRepository.save(existingItem);
    }
    
    /**
     * Update menu item using FoodItem object
     */
    public FoodItem updateMenuItem(FoodItem foodItem) {
        if (foodItem == null) {
            throw new IllegalArgumentException("Food item cannot be null");
        }
        if (foodItem.getId() == null || foodItem.getId() <= 0) {
            throw new IllegalArgumentException("Food item ID must be positive");
        }
        
        // Get existing item to preserve all fields
        Optional<FoodItem> existingOpt = itemRepository.findById(foodItem.getId());
        if (existingOpt.isEmpty()) {
            throw new NotFoundException("Food item", foodItem.getId());
        }
        
        FoodItem existingItem = existingOpt.get();
        
        // Update only provided fields
        if (foodItem.getName() != null && !foodItem.getName().trim().isEmpty()) {
            if (foodItem.getName().trim().length() > 100) {
                throw new IllegalArgumentException("Food item name cannot exceed 100 characters");
            }
            existingItem.setName(foodItem.getName().trim());
        }
        
        if (foodItem.getDescription() != null && !foodItem.getDescription().trim().isEmpty()) {
            if (foodItem.getDescription().trim().length() > 500) {
                throw new IllegalArgumentException("Food item description cannot exceed 500 characters");
            }
            existingItem.setDescription(foodItem.getDescription().trim());
        }
        
        if (foodItem.getPrice() != null) {
            if (foodItem.getPrice() < 0.01 || foodItem.getPrice() > 9999.99) {
                throw new IllegalArgumentException("Price must be between 0.01 and 9999.99");
            }
            existingItem.setPrice(foodItem.getPrice());
        }
        
        if (foodItem.getCategory() != null && !foodItem.getCategory().trim().isEmpty()) {
            if (foodItem.getCategory().trim().length() > 50) {
                throw new IllegalArgumentException("Category cannot exceed 50 characters");
            }
            existingItem.setCategory(foodItem.getCategory().trim());
        }
        
        if (foodItem.getQuantity() != null) {
            if (foodItem.getQuantity() < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative");
            }
            existingItem.setQuantity(foodItem.getQuantity());
        }
        
        if (foodItem.getAvailable() != null) {
            existingItem.setAvailable(foodItem.getAvailable());
        }
        
        // Preserve restaurant association
        // (restaurant should not change via update)
        
        return itemRepository.save(existingItem);
    }
    
    /**
     * Remove an item from menu
     */
    public void removeItemFromMenu(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        // Verify item exists
        if (!itemRepository.existsById(itemId)) {
            throw new NotFoundException("Food item", itemId);
        }
        
        itemRepository.delete(itemId);
    }
    
    /**
     * Set item availability status
     */
    public FoodItem setItemAvailability(Long itemId, boolean available) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem item = itemOpt.get();
        item.setAvailable(available);
        
        return itemRepository.save(item);
    }
    
    /**
     * Update item quantity
     */
    public FoodItem updateItemQuantity(Long itemId, int quantity) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new NotFoundException("Food item", itemId);
        }
        
        FoodItem item = itemOpt.get();
        item.setQuantity(quantity);
        
        return itemRepository.save(item);
    }
    
    /**
     * Get menu items by category for a restaurant
     */
    public List<FoodItem> getMenuByCategory(Long restaurantId, String category) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getMenuByCategory(restaurantId, category.trim());
    }
    
    /**
     * Get all categories for a restaurant menu
     */
    public List<String> getMenuCategories(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getCategories(restaurantId);
    }
    
    /**
     * Get low stock items for a restaurant
     */
    public List<FoodItem> getLowStockItems(Long restaurantId, int threshold) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative");
        }
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        return menuRepository.getLowStockItems(restaurantId, threshold);
    }
    
    /**
     * Get menu statistics for a restaurant
     */
    public MenuStatistics getMenuStatistics(Long restaurantId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Verify restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant", restaurantId);
        }
        
        List<FoodItem> allItems = menuRepository.getMenuByRestaurant(restaurantId);
        
        int totalItems = allItems.size();
        int availableItems = (int) allItems.stream().filter(FoodItem::getAvailable).count();
        int unavailableItems = totalItems - availableItems;
        int outOfStockItems = (int) allItems.stream().filter(item -> item.getQuantity() == 0).count();
        int lowStockItems = (int) allItems.stream().filter(item -> item.getQuantity() > 0 && item.getQuantity() <= 5).count();
        
        return new MenuStatistics(totalItems, availableItems, unavailableItems, outOfStockItems, lowStockItems);
    }
    
    /**
     * Check if restaurant owns the menu item
     */
    public boolean isRestaurantOwner(Long restaurantId, Long itemId) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("Item ID must be positive");
        }
        
        Optional<FoodItem> itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            return false;
        }
        
        FoodItem item = itemOpt.get();
        return item.getRestaurant().getId().equals(restaurantId);
    }
    
    /**
     * Validate input for adding new item
     */
    private void validateAddItemInput(Long restaurantId, String name, String description, Double price, String category) {
        if (restaurantId == null || restaurantId <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Food item name cannot be empty");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Food item name cannot exceed 100 characters");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Food item description cannot be empty");
        }
        if (description.trim().length() > 500) {
            throw new IllegalArgumentException("Food item description cannot exceed 500 characters");
        }
        if (price == null || price < 0.01 || price > 9999.99) {
            throw new IllegalArgumentException("Price must be between 0.01 and 9999.99");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        if (category.trim().length() > 50) {
            throw new IllegalArgumentException("Category cannot exceed 50 characters");
        }
    }
    
    /**
     * Menu statistics class
     */
    public static class MenuStatistics {
        private final int totalItems;
        private final int availableItems;
        private final int unavailableItems;
        private final int outOfStockItems;
        private final int lowStockItems;
        
        public MenuStatistics(int totalItems, int availableItems, int unavailableItems, int outOfStockItems, int lowStockItems) {
            this.totalItems = totalItems;
            this.availableItems = availableItems;
            this.unavailableItems = unavailableItems;
            this.outOfStockItems = outOfStockItems;
            this.lowStockItems = lowStockItems;
        }
        
        public int getTotalItems() { return totalItems; }
        public int getAvailableItems() { return availableItems; }
        public int getUnavailableItems() { return unavailableItems; }
        public int getOutOfStockItems() { return outOfStockItems; }
        public int getLowStockItems() { return lowStockItems; }
        public int getInStockItems() { return totalItems - outOfStockItems; }
        public double getAvailabilityPercentage() { 
            return totalItems > 0 ? (double) availableItems / totalItems * 100 : 0.0; 
        }
    }
}

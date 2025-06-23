package com.myapp.item;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.restaurant.RestaurantRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for food item management
 * Handles business logic, validation, and inventory operations
 */
public class ItemService {
    
    private final ItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;
    
    public ItemService(ItemRepository itemRepository, RestaurantRepository restaurantRepository) {
        this.itemRepository = itemRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * Add a new food item to a restaurant's menu
     * Validates restaurant ownership and item data
     */
    public FoodItem addItem(Long restaurantId, String name, String description, 
                           double price, String category, String imageUrl, int quantity) {
        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // Validate input data
        validateItemData(name, description, price, category, quantity);
        
        // Create new food item
        FoodItem item = FoodItem.forMenu(name, description, price, category, restaurant);
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            item = FoodItem.forMenuWithImage(name, description, price, category, imageUrl, restaurant);
        }
        
        // Set quantity after creation
        item.setQuantity(quantity);
        
        return itemRepository.saveNew(item);
    }
    
    /**
     * Update an existing food item
     * Validates ownership and maintains business rules
     */
    public FoodItem updateItem(Long itemId, String name, String description, 
                              double price, String category, String imageUrl, Integer quantity) {
        FoodItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
        
        // Update fields if provided
        if (name != null && !name.trim().isEmpty()) {
            item.setName(name.trim());
        }
        if (description != null && !description.trim().isEmpty()) {
            item.setDescription(description.trim());
        }
        if (price > 0) {
            item.setPrice(price);
        }
        if (category != null && !category.trim().isEmpty()) {
            item.setCategory(category.trim());
        }
        if (imageUrl != null) {
            item.setImageUrl(imageUrl.trim().isEmpty() ? null : imageUrl.trim());
        }
        if (quantity != null && quantity >= 0) {
            item.setQuantity(quantity);
        }
        
        return itemRepository.save(item);
    }
    
    /**
     * Get food item by ID with validation
     */
    public FoodItem getItem(Long itemId) {
        return itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
    }
    
    /**
     * Get all items for a specific restaurant
     */
    public List<FoodItem> getRestaurantItems(Long restaurantId) {
        // Validate restaurant exists
        restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return itemRepository.findByRestaurant(restaurantId);
    }
    
    /**
     * Get only available items for a restaurant (for customer view)
     */
    public List<FoodItem> getAvailableItems(Long restaurantId) {
        // Validate restaurant exists
        restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return itemRepository.findAvailableByRestaurant(restaurantId);
    }
    
    /**
     * Search food items by keyword across all restaurants
     */
    public List<FoodItem> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Search keyword cannot be empty");
        }
        return itemRepository.searchByKeyword(keyword.trim());
    }
    
    /**
     * Search food items by category
     */
    public List<FoodItem> getItemsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        return itemRepository.findByCategory(category.trim());
    }
    
    /**
     * Update item availability status
     */
    public void updateAvailability(Long itemId, boolean available) {
        FoodItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
        
        itemRepository.updateAvailability(itemId, available);
    }
    
    /**
     * Update item quantity (for inventory management)
     */
    public void updateQuantity(Long itemId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        FoodItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
        
        itemRepository.updateQuantity(itemId, newQuantity);
    }
    
    /**
     * Decrease item quantity (when ordered)
     * Returns true if successful, false if insufficient stock
     */
    public boolean decreaseQuantity(Long itemId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        FoodItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
        
        if (item.getQuantity() < amount) {
            return false; // Insufficient stock
        }
        
        item.decreaseQuantity(amount);
        itemRepository.save(item);
        return true;
    }
    
    /**
     * Increase item quantity (when restocking)
     */
    public void increaseQuantity(Long itemId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        FoodItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
        
        item.increaseQuantity(amount);
        itemRepository.save(item);
    }
    
    /**
     * Check if item is in stock
     */
    public boolean isInStock(Long itemId) {
        FoodItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
        
        return item.isInStock();
    }
    
    /**
     * Get low stock items for a restaurant (quantity < 5)
     */
    public List<FoodItem> getLowStockItems(Long restaurantId) {
        // Validate restaurant exists
        restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return itemRepository.findByRestaurant(restaurantId).stream()
            .filter(item -> item.getQuantity() < 5)
            .toList();
    }
    
    /**
     * Delete a food item
     */
    public void deleteItem(Long itemId) {
        FoodItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Food item", itemId));
        
        itemRepository.delete(itemId);
    }
    
    /**
     * Validate item data for creation/update
     */
    private void validateItemData(String name, String description, double price, String category, int quantity) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Item name cannot exceed 100 characters");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Item description cannot be empty");
        }
        if (description.trim().length() > 500) {
            throw new IllegalArgumentException("Item description cannot exceed 500 characters");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Item price must be positive");
        }
        if (price > 10000) {
            throw new IllegalArgumentException("Item price cannot exceed 10,000");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Item category cannot be empty");
        }
        if (category.trim().length() > 50) {
            throw new IllegalArgumentException("Item category cannot exceed 50 characters");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Item quantity cannot be negative");
        }
    }
    
    /**
     * Get restaurant menu statistics
     */
    public MenuStatistics getMenuStatistics(Long restaurantId) {
        // Validate restaurant exists
        restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        List<FoodItem> items = itemRepository.findByRestaurant(restaurantId);
        
        long totalItems = items.size();
        long availableItems = items.stream().filter(item -> item.getAvailable()).count();
        long inStockItems = items.stream().filter(FoodItem::isInStock).count();
        long lowStockItems = items.stream().filter(item -> item.getQuantity() < 5).count();
        double averagePrice = items.stream().mapToDouble(FoodItem::getPrice).average().orElse(0.0);
        
        return new MenuStatistics(totalItems, availableItems, inStockItems, lowStockItems, averagePrice);
    }
    
    /**
     * Menu statistics data class
     */
    public static class MenuStatistics {
        private final long totalItems;
        private final long availableItems;
        private final long inStockItems;
        private final long lowStockItems;
        private final double averagePrice;
        
        public MenuStatistics(long totalItems, long availableItems, long inStockItems, 
                            long lowStockItems, double averagePrice) {
            this.totalItems = totalItems;
            this.availableItems = availableItems;
            this.inStockItems = inStockItems;
            this.lowStockItems = lowStockItems;
            this.averagePrice = averagePrice;
        }
        
        // Getters
        public long getTotalItems() { return totalItems; }
        public long getAvailableItems() { return availableItems; }
        public long getInStockItems() { return inStockItems; }
        public long getLowStockItems() { return lowStockItems; }
        public double getAveragePrice() { return averagePrice; }
        
        @Override
        public String toString() {
            return String.format("MenuStatistics{totalItems=%d, availableItems=%d, inStockItems=%d, lowStockItems=%d, averagePrice=%.2f}", 
                totalItems, availableItems, inStockItems, lowStockItems, averagePrice);
        }
    }
    
    /**
     * Get distinct categories for a restaurant
     */
    public List<String> getRestaurantCategories(Long restaurantId) {
        // Validate restaurant exists
        restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return itemRepository.getCategoriesByRestaurant(restaurantId);
    }

    /**
     * Get low stock items for a restaurant
     */
    public List<FoodItem> getLowStockItems(Long restaurantId, int threshold) {
        // Validate restaurant exists
        restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return itemRepository.findLowStockByRestaurant(restaurantId, threshold);
    }

    /**
     * Get low stock items with default threshold
     */
}

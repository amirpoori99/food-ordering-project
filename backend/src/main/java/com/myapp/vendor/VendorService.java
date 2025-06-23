package com.myapp.vendor;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.FoodItem;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.restaurant.RestaurantRepository;
import com.myapp.item.ItemRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Service class for vendor operations from customer perspective
 * Handles vendor listing, search, and menu browsing
 */
public class VendorService {
    
    private final VendorRepository vendorRepository;
    private final RestaurantRepository restaurantRepository;
    private final ItemRepository itemRepository;
    
    public VendorService() {
        this.vendorRepository = new VendorRepository();
        this.restaurantRepository = new RestaurantRepository();
        this.itemRepository = new ItemRepository();
    }
    
    // Constructor for dependency injection (testing)
    public VendorService(VendorRepository vendorRepository, RestaurantRepository restaurantRepository, ItemRepository itemRepository) {
        this.vendorRepository = vendorRepository;
        this.restaurantRepository = restaurantRepository;
        this.itemRepository = itemRepository;
    }
    
    /**
     * Get all active vendors (approved restaurants)
     */
    public List<Restaurant> getAllVendors() {
        return restaurantRepository.findByStatus(RestaurantStatus.APPROVED);
    }
    
    /**
     * Search vendors by name or location
     */
    public List<Restaurant> searchVendors(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllVendors();
        }
        
        return vendorRepository.searchVendors(searchTerm.trim());
    }
    
    /**
     * Get vendor by ID with full details
     */
    public Restaurant getVendor(Long vendorId) {
        if (vendorId == null || vendorId <= 0) {
            throw new IllegalArgumentException("Vendor ID must be positive");
        }
        
        Restaurant vendor = restaurantRepository.findById(vendorId)
            .orElseThrow(() -> new NotFoundException("Vendor", vendorId));
        
        // Only return approved vendors to customers
        if (vendor.getStatus() != RestaurantStatus.APPROVED) {
            throw new NotFoundException("Vendor", vendorId);
        }
        
        return vendor;
    }
    
    /**
     * Get vendor menu organized by categories
     */
    public Map<String, Object> getVendorMenu(Long vendorId) {
        Restaurant vendor = getVendor(vendorId);
        
        // Get available menu items
        List<FoodItem> menuItems = itemRepository.findAvailableByRestaurant(vendorId);
        
        // Group items by category
        Map<String, List<FoodItem>> itemsByCategory = menuItems.stream()
            .collect(Collectors.groupingBy(FoodItem::getCategory));
        
        // Create response structure matching OpenAPI spec
        Map<String, Object> response = new HashMap<>();
        response.put("vendor", vendor);
        response.put("menu_titles", itemsByCategory.keySet().stream().collect(Collectors.toList()));
        
        // Add each category's items
        for (Map.Entry<String, List<FoodItem>> entry : itemsByCategory.entrySet()) {
            response.put(entry.getKey(), entry.getValue());
        }
        
        return response;
    }
    
    /**
     * Get vendors by location/area
     */
    public List<Restaurant> getVendorsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
        
        return vendorRepository.findByLocation(location.trim());
    }
    
    /**
     * Get featured/popular vendors
     */
    public List<Restaurant> getFeaturedVendors() {
        return vendorRepository.getFeaturedVendors();
    }
    
    /**
     * Get vendors with specific food categories
     */
    public List<Restaurant> getVendorsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        
        return vendorRepository.findByFoodCategory(category.trim());
    }
    
    /**
     * Get vendor statistics for display
     */
    public VendorStats getVendorStats(Long vendorId) {
        Restaurant vendor = getVendor(vendorId);
        
        int totalItems = itemRepository.countByRestaurant(vendorId);
        int availableItems = itemRepository.countAvailableByRestaurant(vendorId);
        List<String> categories = itemRepository.getCategoriesByRestaurant(vendorId);
        
        return new VendorStats(
            vendorId,
            vendor.getName(),
            totalItems,
            availableItems,
            categories.size(),
            categories
        );
    }
    
    /**
     * Check if vendor is currently accepting orders
     */
    public boolean isVendorAcceptingOrders(Long vendorId) {
        if (vendorId == null || vendorId <= 0) {
            throw new IllegalArgumentException("Vendor ID must be positive");
        }

        Restaurant vendor = restaurantRepository.findById(vendorId).orElse(null);
        
        // Return false if vendor doesn't exist or is not approved
        if (vendor == null || vendor.getStatus() != RestaurantStatus.APPROVED) {
            return false;
        }
        
        return itemRepository.countAvailableByRestaurant(vendorId) > 0;
    }
    
    /**
     * Vendor statistics data class
     */
    public static class VendorStats {
        private final Long vendorId;
        private final String vendorName;
        private final int totalItems;
        private final int availableItems;
        private final int totalCategories;
        private final List<String> categories;
        
        public VendorStats(Long vendorId, String vendorName, int totalItems, int availableItems, int totalCategories, List<String> categories) {
            this.vendorId = vendorId;
            this.vendorName = vendorName;
            this.totalItems = totalItems;
            this.availableItems = availableItems;
            this.totalCategories = totalCategories;
            this.categories = categories;
        }
        
        // Getters
        public Long getVendorId() { return vendorId; }
        public String getVendorName() { return vendorName; }
        public int getTotalItems() { return totalItems; }
        public int getAvailableItems() { return availableItems; }
        public int getTotalCategories() { return totalCategories; }
        public List<String> getCategories() { return categories; }
    }
}

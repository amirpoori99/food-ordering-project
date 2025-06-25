package com.myapp.restaurant;

import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.PerformanceUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

public class RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    
    public RestaurantService() {
        this.restaurantRepository = new RestaurantRepository();
    }
    
    // Constructor for dependency injection (testing)
    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * Register a new restaurant
     */
    public Restaurant registerRestaurant(Long ownerId, String name, String address, String phone) {
        validateRegistrationInput(ownerId, name, address, phone);
        
        Restaurant restaurant = new Restaurant(ownerId, name.trim(), address.trim(), phone.trim());
        return restaurantRepository.saveNew(restaurant);
    }
    
    /**
     * Register restaurant with Restaurant object
     */
    public Restaurant registerRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        
        validateRegistrationInput(restaurant.getOwnerId(), restaurant.getName(), 
                                restaurant.getAddress(), restaurant.getPhone());
        
        // Ensure default status for new restaurants
        if (restaurant.getStatus() == null) {
            restaurant.setStatus(RestaurantStatus.PENDING);
        }
        
        return restaurantRepository.saveNew(restaurant);
    }
    
    /**
     * Get restaurant by ID
     */
    public Restaurant getRestaurantById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        Optional<Restaurant> restaurant = restaurantRepository.findById(id);
        if (restaurant.isEmpty()) {
            throw new NotFoundException("Restaurant", id);
        }
        
        return restaurant.get();
    }
    
    /**
     * Get all restaurants owned by a specific user
     */
    public List<Restaurant> getRestaurantsByOwner(Long ownerId) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID must be positive");
        }
        
        return restaurantRepository.listByOwner(ownerId);
    }
    
    /**
     * Get all approved restaurants (public listing) - Optimized with caching
     */
    public List<Restaurant> getApprovedRestaurants() {
        String cacheKey = PerformanceUtil.createQueryCacheKey("approved_restaurants");
        
        return PerformanceUtil.executeWithCache(
            cacheKey,
            () -> {
                PerformanceUtil.PerformanceResult<List<Restaurant>> result = 
                    PerformanceUtil.measurePerformance("getApprovedRestaurants", 
                        () -> restaurantRepository.listApproved());
                
                System.out.println("🏪 " + result.toString());
                return result.getResult();
            },
            List.class,
            15 // Cache for 15 minutes
        );
    }
    
    /**
     * Get all restaurants
     */
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }
    
    /**
     * Get restaurants by status
     */
    public List<Restaurant> getRestaurantsByStatus(RestaurantStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return restaurantRepository.findByStatus(status);
    }
    
    /**
     * Update restaurant status (admin function)
     */
    public void updateRestaurantStatus(Long id, RestaurantStatus status) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        // Verify restaurant exists
        getRestaurantById(id);
        
        restaurantRepository.updateStatus(id, status);
    }
    
    /**
     * Update restaurant information
     */
    public Restaurant updateRestaurant(Long id, String name, String address, String phone) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        Restaurant existingRestaurant = getRestaurantById(id);
        
        // Update fields if provided
        if (name != null && !name.trim().isEmpty()) {
            if (name.trim().length() > 100) {
                throw new IllegalArgumentException("Restaurant name cannot exceed 100 characters");
            }
            existingRestaurant.setName(name.trim());
        }
        
        if (address != null && !address.trim().isEmpty()) {
            if (address.trim().length() > 200) {
                throw new IllegalArgumentException("Restaurant address cannot exceed 200 characters");
            }
            existingRestaurant.setAddress(address.trim());
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            if (phone.trim().length() > 20) {
                throw new IllegalArgumentException("Restaurant phone cannot exceed 20 characters");
            }
            existingRestaurant.setPhone(phone.trim());
        }
        
        return restaurantRepository.save(existingRestaurant);
    }
    
    /**
     * Update restaurant with Restaurant object
     */
    public Restaurant updateRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        if (restaurant.getId() == null || restaurant.getId() <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Get existing restaurant to preserve all fields
        Restaurant existingRestaurant = getRestaurantById(restaurant.getId());
        
        // Update only the provided fields
        if (restaurant.getName() != null && !restaurant.getName().trim().isEmpty()) {
            if (restaurant.getName().trim().length() > 100) {
                throw new IllegalArgumentException("Restaurant name cannot exceed 100 characters");
            }
            existingRestaurant.setName(restaurant.getName().trim());
        }
        
        if (restaurant.getAddress() != null && !restaurant.getAddress().trim().isEmpty()) {
            if (restaurant.getAddress().trim().length() > 200) {
                throw new IllegalArgumentException("Restaurant address cannot exceed 200 characters");
            }
            existingRestaurant.setAddress(restaurant.getAddress().trim());
        }
        
        if (restaurant.getPhone() != null && !restaurant.getPhone().trim().isEmpty()) {
            if (restaurant.getPhone().trim().length() > 20) {
                throw new IllegalArgumentException("Restaurant phone cannot exceed 20 characters");
            }
            existingRestaurant.setPhone(restaurant.getPhone().trim());
        }
        
        // Preserve ownerId and status from existing restaurant
        // (they should not be updated via this method)
        
        return restaurantRepository.save(existingRestaurant);
    }
    
    /**
     * Delete restaurant
     */
    public void deleteRestaurant(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Restaurant ID must be positive");
        }
        
        // Verify restaurant exists
        getRestaurantById(id);
        
        restaurantRepository.delete(id);
    }
    
    /**
     * Check if restaurant exists
     */
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        return restaurantRepository.findById(id).isPresent();
    }
    
    /**
     * Get pending restaurants (for admin approval)
     */
    public List<Restaurant> getPendingRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.PENDING);
    }
    
    /**
     * Approve restaurant
     */
    public void approveRestaurant(Long id) {
        updateRestaurantStatus(id, RestaurantStatus.APPROVED);
    }
    
    /**
     * Reject restaurant
     */
    public void rejectRestaurant(Long id) {
        updateRestaurantStatus(id, RestaurantStatus.REJECTED);
    }
    
    /**
     * Suspend restaurant
     */
    public void suspendRestaurant(Long id) {
        updateRestaurantStatus(id, RestaurantStatus.SUSPENDED);
    }
    
    /**
     * Bulk update restaurant statuses - Optimized with async processing
     */
    public Future<Void> bulkUpdateRestaurantStatus(List<Long> restaurantIds, RestaurantStatus status) {
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            throw new IllegalArgumentException("Restaurant IDs cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        return PerformanceUtil.executeAsync(() -> {
            PerformanceUtil.measurePerformance("bulkUpdateRestaurantStatus", () -> {
                // Process in batches of 50
                PerformanceUtil.processBatch(restaurantIds, 50, batch -> {
                    for (Long id : batch) {
                        try {
                            updateRestaurantStatus(id, status);
                        } catch (Exception e) {
                            System.err.println("Failed to update restaurant " + id + ": " + e.getMessage());
                        }
                    }
                });
                
                // Clear relevant caches after bulk update
                clearRestaurantCaches();
                return null;
            });
        });
    }
    
    /**
     * Bulk approve restaurants - Optimized async operation
     */
    public Future<Void> bulkApproveRestaurants(List<Long> restaurantIds) {
        return bulkUpdateRestaurantStatus(restaurantIds, RestaurantStatus.APPROVED);
    }
    
    /**
     * Clear restaurant-related caches after data modifications
     */
    private void clearRestaurantCaches() {
        // Clear specific cache keys
        String approvedKey = PerformanceUtil.createQueryCacheKey("approved_restaurants");
        String statsKey = PerformanceUtil.createQueryCacheKey("restaurant_statistics");
        
        // Note: PerformanceUtil doesn't have specific key removal, so we'd need to add that
        // For now, we can force a cache cleanup
        PerformanceUtil.cleanExpiredEntries();
    }
    
    /**
     * Get restaurant statistics - Optimized with caching and performance monitoring
     */
    public RestaurantStatistics getRestaurantStatistics() {
        String cacheKey = PerformanceUtil.createQueryCacheKey("restaurant_statistics");
        
        return PerformanceUtil.executeWithCache(
            cacheKey,
            () -> {
                return PerformanceUtil.measurePerformance("calculateRestaurantStatistics", () -> {
                    List<Restaurant> allRestaurants = getAllRestaurants();
                    
                    // Check memory usage before processing large dataset
                    if (PerformanceUtil.isMemoryUsageCritical()) {
                        PerformanceUtil.forceGarbageCollection();
                    }
                    
                    long totalCount = allRestaurants.size();
                    long approvedCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.APPROVED)
                            .count();
                    long pendingCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.PENDING)
                            .count();
                    long rejectedCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.REJECTED)
                            .count();
                    long suspendedCount = allRestaurants.parallelStream()
                            .filter(r -> r.getStatus() == RestaurantStatus.SUSPENDED)
                            .count();
                    
                    return new RestaurantStatistics(totalCount, approvedCount, pendingCount, 
                                                  rejectedCount, suspendedCount);
                }).getResult();
            },
            RestaurantStatistics.class,
            30 // Cache for 30 minutes
        );
    }
    
    // Private validation methods
    private void validateRegistrationInput(Long ownerId, String name, String address, String phone) {
        if (ownerId == null || ownerId <= 0) {
            throw new IllegalArgumentException("Owner ID must be positive");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be empty");
        }
        
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Restaurant name cannot exceed 100 characters");
        }
        
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant address cannot be empty");
        }
        
        if (address.trim().length() > 200) {
            throw new IllegalArgumentException("Restaurant address cannot exceed 200 characters");
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant phone cannot be empty");
        }
        
        if (phone.trim().length() > 20) {
            throw new IllegalArgumentException("Restaurant phone cannot exceed 20 characters");
        }
    }
    
    // Inner class for statistics
    public static class RestaurantStatistics {
        private final long totalCount;
        private final long approvedCount;
        private final long pendingCount;
        private final long rejectedCount;
        private final long suspendedCount;
        
        public RestaurantStatistics(long totalCount, long approvedCount, long pendingCount,
                                  long rejectedCount, long suspendedCount) {
            this.totalCount = totalCount;
            this.approvedCount = approvedCount;
            this.pendingCount = pendingCount;
            this.rejectedCount = rejectedCount;
            this.suspendedCount = suspendedCount;
        }
        
        // Getters
        public long getTotalCount() { return totalCount; }
        public long getApprovedCount() { return approvedCount; }
        public long getPendingCount() { return pendingCount; }
        public long getRejectedCount() { return rejectedCount; }
        public long getSuspendedCount() { return suspendedCount; }
    }
}

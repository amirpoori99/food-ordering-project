package com.myapp.favorites;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Favorite;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.restaurant.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Favorites business logic
 * Handles all favorite operations with validation
 */
public class FavoritesService {
    
    private static final Logger logger = LoggerFactory.getLogger(FavoritesService.class);
    
    private final FavoritesRepository favoritesRepository;
    private final AuthRepository authRepository;
    private final RestaurantRepository restaurantRepository;
    
    public FavoritesService() {
        this.favoritesRepository = new FavoritesRepository();
        this.authRepository = new AuthRepository();
        this.restaurantRepository = new RestaurantRepository();
    }
    
    // Constructor for testing
    public FavoritesService(FavoritesRepository favoritesRepository, 
                           AuthRepository authRepository, 
                           RestaurantRepository restaurantRepository) {
        this.favoritesRepository = favoritesRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * Adds a restaurant to user's favorites
     */
    public Favorite addFavorite(Long userId, Long restaurantId, String notes) {
        logger.info("Adding favorite: userId={}, restaurantId={}", userId, restaurantId);
        
        // Validate inputs
        validateFavoriteInputs(userId, restaurantId);
        
        // Get user and restaurant
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // Check if already exists
        Optional<Favorite> existingFavorite = favoritesRepository.findByUserAndRestaurant(user, restaurant);
        if (existingFavorite.isPresent()) {
            throw new IllegalArgumentException("Restaurant is already in user's favorites");
        }
        
        // Validate that user is not favoriting their own restaurant
        if (restaurant.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Restaurant owners cannot favorite their own restaurants");
        }
        
        // Create and save favorite
        Favorite favorite = new Favorite(user, restaurant, notes);
        Favorite savedFavorite = favoritesRepository.save(favorite);
        
        logger.info("Added favorite with ID: {}", savedFavorite.getId());
        return savedFavorite;
    }
    
    /**
     * Legacy method for backward compatibility
     */
    public Favorite addFavorite(Long userId, Long restaurantId) {
        return addFavorite(userId, restaurantId, null);
    }
    
    /**
     * Removes a restaurant from user's favorites
     */
    public boolean removeFavorite(Long userId, Long restaurantId) {
        logger.info("Removing favorite: userId={}, restaurantId={}", userId, restaurantId);
        
        // Validate inputs
        validateFavoriteInputs(userId, restaurantId);
        
        // Get user and restaurant
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // Find existing favorite
        Optional<Favorite> favorite = favoritesRepository.findByUserAndRestaurant(user, restaurant);
        if (favorite.isEmpty()) {
            throw new NotFoundException("Favorite", "user=" + userId + ", restaurant=" + restaurantId);
        }
        
        // Delete favorite
        boolean deleted = favoritesRepository.delete(favorite.get().getId());
        if (deleted) {
            logger.info("Removed favorite: userId={}, restaurantId={}", userId, restaurantId);
        }
        return deleted;
    }
    
    /**
     * Updates notes for a favorite
     */
    public Favorite updateFavoriteNotes(Long favoriteId, String notes) {
        logger.info("Updating favorite notes: favoriteId={}", favoriteId);
        
        if (favoriteId == null) {
            throw new IllegalArgumentException("Favorite ID cannot be null");
        }
        
        Favorite favorite = favoritesRepository.findById(favoriteId)
            .orElseThrow(() -> new NotFoundException("Favorite", favoriteId));
        
        favorite.updateNotes(notes);
        Favorite updatedFavorite = favoritesRepository.save(favorite);
        
        logger.info("Updated favorite notes for ID: {}", favoriteId);
        return updatedFavorite;
    }
    
    /**
     * Gets a favorite by ID
     */
    public Favorite getFavorite(Long favoriteId) {
        if (favoriteId == null) {
            throw new IllegalArgumentException("Favorite ID cannot be null");
        }
        
        return favoritesRepository.findById(favoriteId)
            .orElseThrow(() -> new NotFoundException("Favorite", favoriteId));
    }
    
    /**
     * Gets all favorites for a user
     */
    public List<Favorite> getUserFavorites(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        return favoritesRepository.findByUser(user);
    }
    
    /**
     * Gets all users who favorited a restaurant
     */
    public List<Favorite> getRestaurantFavorites(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return favoritesRepository.findByRestaurant(restaurant);
    }
    
    /**
     * Checks if a user has favorited a restaurant
     */
    public boolean isFavorite(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return false;
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return false;
            }
            
            return favoritesRepository.findByUserAndRestaurant(user, restaurant).isPresent();
        } catch (Exception e) {
            logger.error("Error checking if user {} favorited restaurant {}: {}", 
                        userId, restaurantId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets user's favorite for a specific restaurant
     */
    public Optional<Favorite> getUserFavoriteForRestaurant(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return Optional.empty();
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return Optional.empty();
            }
            
            return favoritesRepository.findByUserAndRestaurant(user, restaurant);
        } catch (Exception e) {
            logger.error("Error getting user {} favorite for restaurant {}: {}", 
                        userId, restaurantId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Gets recent favorites (within specified days)
     */
    public List<Favorite> getRecentFavorites(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        return favoritesRepository.findRecentFavorites(days);
    }
    
    /**
     * Gets favorites with notes
     */
    public List<Favorite> getFavoritesWithNotes() {
        return favoritesRepository.findFavoritesWithNotes();
    }
    
    /**
     * Gets favorite count for a restaurant
     */
    public Long getRestaurantFavoriteCount(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return favoritesRepository.countByRestaurant(restaurant);
    }
    
    /**
     * Gets favorite count for a user
     */
    public Long getUserFavoriteCount(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        return favoritesRepository.countByUser(user);
    }
    
    /**
     * Gets all favorites (admin only)
     */
    public List<Favorite> getAllFavorites() {
        return favoritesRepository.findAll();
    }
    
    /**
     * Gets favorites with pagination
     */
    public List<Favorite> getFavoritesWithPagination(int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }
        
        int offset = page * size;
        return favoritesRepository.findWithPagination(offset, size);
    }
    
    /**
     * Gets total favorite count
     */
    public Long getTotalFavoriteCount() {
        return favoritesRepository.countAll();
    }
    
    /**
     * Deletes a favorite by ID (admin only)
     */
    public boolean deleteFavorite(Long favoriteId) {
        if (favoriteId == null) {
            throw new IllegalArgumentException("Favorite ID cannot be null");
        }
        
        boolean deleted = favoritesRepository.delete(favoriteId);
        if (deleted) {
            logger.info("Deleted favorite with ID: {}", favoriteId);
        }
        return deleted;
    }
    
    /**
     * Gets favorite statistics for a user
     */
    public FavoriteStats getUserFavoriteStats(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        List<Favorite> favorites = favoritesRepository.findByUser(user);
        Long totalCount = (long) favorites.size();
        Long withNotesCount = favorites.stream()
            .mapToLong(f -> f.hasNotes() ? 1L : 0L)
            .sum();
        Long recentCount = favorites.stream()
            .mapToLong(f -> f.isRecent() ? 1L : 0L)
            .sum();
        
        return new FavoriteStats(totalCount, withNotesCount, recentCount);
    }
    
    // Private helper methods
    
    private void validateFavoriteInputs(Long userId, Long restaurantId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
    }
    
    /**
     * Inner class for favorite statistics
     */
    public static class FavoriteStats {
        private final Long totalFavorites;
        private final Long favoritesWithNotes;
        private final Long recentFavorites;
        
        public FavoriteStats(Long totalFavorites, Long favoritesWithNotes, Long recentFavorites) {
            this.totalFavorites = totalFavorites != null ? totalFavorites : 0L;
            this.favoritesWithNotes = favoritesWithNotes != null ? favoritesWithNotes : 0L;
            this.recentFavorites = recentFavorites != null ? recentFavorites : 0L;
        }
        
        public Long getTotalFavorites() {
            return totalFavorites;
        }
        
        public Long getFavoritesWithNotes() {
            return favoritesWithNotes;
        }
        
        public Long getRecentFavorites() {
            return recentFavorites;
        }
        
        public boolean hasFavorites() {
            return totalFavorites > 0;
        }
        
        public double getNotesPercentage() {
            return totalFavorites > 0 ? (favoritesWithNotes.doubleValue() / totalFavorites.doubleValue()) * 100 : 0.0;
        }
        
        public double getRecentPercentage() {
            return totalFavorites > 0 ? (recentFavorites.doubleValue() / totalFavorites.doubleValue()) * 100 : 0.0;
        }
        
        @Override
        public String toString() {
            return "FavoriteStats{" +
                    "totalFavorites=" + totalFavorites +
                    ", favoritesWithNotes=" + favoritesWithNotes +
                    ", recentFavorites=" + recentFavorites +
                    '}';
        }
    }
} 
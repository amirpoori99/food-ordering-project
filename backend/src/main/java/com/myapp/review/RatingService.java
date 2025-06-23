package com.myapp.review;

import com.myapp.auth.AuthRepository;
import com.myapp.common.exceptions.NotFoundException;
import com.myapp.common.models.Rating;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.restaurant.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for Rating business logic
 * Handles all rating and review operations with validation
 */
public class RatingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    
    private final RatingRepository ratingRepository;
    private final AuthRepository authRepository;
    private final RestaurantRepository restaurantRepository;
    
    public RatingService() {
        this.ratingRepository = new RatingRepository();
        this.authRepository = new AuthRepository();
        this.restaurantRepository = new RestaurantRepository();
    }
    
    // Constructor for testing
    public RatingService(RatingRepository ratingRepository, AuthRepository authRepository, RestaurantRepository restaurantRepository) {
        this.ratingRepository = ratingRepository;
        this.authRepository = authRepository;
        this.restaurantRepository = restaurantRepository;
    }
    
    /**
     * Creates a new rating for a restaurant
     */
    public Rating createRating(Long userId, Long restaurantId, Integer score, String reviewText) {
        logger.info("Creating rating: userId={}, restaurantId={}, score={}", userId, restaurantId, score);
        
        // Validate inputs
        validateRatingInputs(userId, restaurantId, score);
        
        // Get user and restaurant
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        // Check if user already rated this restaurant
        Optional<Rating> existingRating = ratingRepository.findByUserAndRestaurant(user, restaurant);
        if (existingRating.isPresent()) {
            throw new IllegalArgumentException("User has already rated this restaurant. Use update instead.");
        }
        
        // Validate that user is not rating their own restaurant
        if (restaurant.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Restaurant owners cannot rate their own restaurants");
        }
        
        // Create and save rating
        Rating rating = new Rating(user, restaurant, score, reviewText);
        Rating savedRating = ratingRepository.save(rating);
        
        logger.info("Created rating with ID: {}", savedRating.getId());
        return savedRating;
    }
    
    /**
     * Updates an existing rating
     */
    public Rating updateRating(Long ratingId, Integer newScore, String newReviewText) {
        logger.info("Updating rating: ratingId={}, newScore={}", ratingId, newScore);
        
        // Validate inputs
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        if (newScore != null && (newScore < 1 || newScore > 5)) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
        
        // Get existing rating
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        // Update rating
        if (newScore != null) {
            rating.updateRating(newScore, newReviewText);
        } else if (newReviewText != null) {
            rating.setReviewText(newReviewText);
        }
        
        Rating updatedRating = ratingRepository.save(rating);
        logger.info("Updated rating with ID: {}", updatedRating.getId());
        return updatedRating;
    }
    
    /**
     * Gets a rating by ID
     */
    public Rating getRating(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        return ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
    }
    
    /**
     * Gets all ratings for a restaurant
     */
    public List<Rating> getRestaurantRatings(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        return ratingRepository.findByRestaurant(restaurant);
    }
    
    /**
     * Gets all ratings by a user
     */
    public List<Rating> getUserRatings(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = authRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User", userId));
        
        return ratingRepository.findByUser(user);
    }
    
    /**
     * Gets rating statistics for a restaurant
     */
    public RatingStats getRestaurantRatingStats(Long restaurantId) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
        
        Double averageRating = ratingRepository.getAverageRating(restaurant);
        Long totalRatings = ratingRepository.getRatingCount(restaurant);
        Map<Integer, Long> distribution = ratingRepository.getRatingDistribution(restaurant);
        
        return new RatingStats(averageRating, totalRatings, distribution);
    }
    
    /**
     * Checks if a user has rated a restaurant
     */
    public boolean hasUserRatedRestaurant(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return false;
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return false;
            }
            
            return ratingRepository.findByUserAndRestaurant(user, restaurant).isPresent();
        } catch (Exception e) {
            logger.error("Error checking if user {} rated restaurant {}: {}", userId, restaurantId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets user's rating for a specific restaurant
     */
    public Optional<Rating> getUserRatingForRestaurant(Long userId, Long restaurantId) {
        if (userId == null || restaurantId == null) {
            return Optional.empty();
        }
        
        try {
            User user = authRepository.findById(userId).orElse(null);
            Restaurant restaurant = restaurantRepository.findById(restaurantId).orElse(null);
            
            if (user == null || restaurant == null) {
                return Optional.empty();
            }
            
            return ratingRepository.findByUserAndRestaurant(user, restaurant);
        } catch (Exception e) {
            logger.error("Error getting user {} rating for restaurant {}: {}", userId, restaurantId, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Deletes a rating (user can only delete their own rating)
     */
    public boolean deleteRating(Long ratingId, Long userId) {
        logger.info("Deleting rating: ratingId={}, userId={}", ratingId, userId);
        
        if (ratingId == null || userId == null) {
            throw new IllegalArgumentException("Rating ID and User ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        // Check if user owns this rating
        if (!rating.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Users can only delete their own ratings");
        }
        
        boolean deleted = ratingRepository.delete(ratingId);
        if (deleted) {
            logger.info("Deleted rating with ID: {}", ratingId);
        }
        return deleted;
    }
    
    /**
     * Marks a rating as helpful (increment helpful count)
     */
    public Rating markAsHelpful(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.incrementHelpfulCount();
        return ratingRepository.save(rating);
    }
    
    /**
     * Removes helpful mark from a rating (decrement helpful count)
     */
    public Rating removeHelpfulMark(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.decrementHelpfulCount();
        return ratingRepository.save(rating);
    }
    
    /**
     * Verifies a rating (admin only)
     */
    public Rating verifyRating(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.verify();
        Rating verifiedRating = ratingRepository.save(rating);
        logger.info("Verified rating with ID: {}", ratingId);
        return verifiedRating;
    }
    
    /**
     * Unverifies a rating (admin only)
     */
    public Rating unverifyRating(Long ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating ID cannot be null");
        }
        
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new NotFoundException("Rating", ratingId));
        
        rating.unverify();
        Rating unverifiedRating = ratingRepository.save(rating);
        logger.info("Unverified rating with ID: {}", ratingId);
        return unverifiedRating;
    }
    
    /**
     * Gets ratings by score range
     */
    public List<Rating> getRatingsByScore(int minScore, int maxScore) {
        if (minScore < 1 || maxScore > 5 || minScore > maxScore) {
            throw new IllegalArgumentException("Invalid score range. Must be between 1-5 and minScore <= maxScore");
        }
        
        return ratingRepository.findByScoreRange(minScore, maxScore);
    }
    
    /**
     * Gets verified ratings
     */
    public List<Rating> getVerifiedRatings() {
        return ratingRepository.findVerifiedRatings();
    }
    
    /**
     * Gets ratings with review text
     */
    public List<Rating> getRatingsWithReviews() {
        return ratingRepository.findRatingsWithReviews();
    }
    
    /**
     * Gets recent ratings (within specified days)
     */
    public List<Rating> getRecentRatings(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be positive");
        }
        
        return ratingRepository.findRecentRatings(days);
    }
    
    /**
     * Gets all ratings (admin only)
     */
    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }
    
    /**
     * Gets ratings with pagination
     */
    public List<Rating> getRatingsWithPagination(int page, int size) {
        if (page < 0 || size < 1) {
            throw new IllegalArgumentException("Page must be non-negative and size must be positive");
        }
        
        int offset = page * size;
        return ratingRepository.findWithPagination(offset, size);
    }
    
    /**
     * Gets total rating count
     */
    public Long getTotalRatingCount() {
        return ratingRepository.countAll();
    }
    
    // Private helper methods
    
    private void validateRatingInputs(Long userId, Long restaurantId, Integer score) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
        if (score == null || score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
    }
    
    /**
     * Inner class for rating statistics
     */
    public static class RatingStats {
        private final Double averageRating;
        private final Long totalRatings;
        private final Map<Integer, Long> distribution;
        
        public RatingStats(Double averageRating, Long totalRatings, Map<Integer, Long> distribution) {
            this.averageRating = averageRating != null ? averageRating : 0.0;
            this.totalRatings = totalRatings != null ? totalRatings : 0L;
            this.distribution = distribution != null ? distribution : Map.of();
        }
        
        public Double getAverageRating() {
            return averageRating;
        }
        
        public Long getTotalRatings() {
            return totalRatings;
        }
        
        public Map<Integer, Long> getDistribution() {
            return distribution;
        }
        
        public String getAverageRatingFormatted() {
            return String.format("%.1f", averageRating);
        }
        
        public boolean hasRatings() {
            return totalRatings > 0;
        }
        
        @Override
        public String toString() {
            return "RatingStats{" +
                    "averageRating=" + averageRating +
                    ", totalRatings=" + totalRatings +
                    ", distribution=" + distribution +
                    '}';
        }
    }
} 
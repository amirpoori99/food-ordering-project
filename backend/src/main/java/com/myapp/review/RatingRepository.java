package com.myapp.review;

import com.myapp.common.models.Rating;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository class for Rating entity operations
 * Handles database operations for ratings and reviews
 */
public class RatingRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingRepository.class);
    
    /**
     * Saves a new rating or updates existing one
     */
    public Rating save(Rating rating) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            if (rating.getId() == null) {
                session.persist(rating);
                logger.info("Created new rating: {}", rating);
            } else {
                rating = session.merge(rating);
                logger.info("Updated rating: {}", rating);
            }
            
            transaction.commit();
            return rating;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving rating: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save rating", e);
        }
    }
    
    /**
     * Finds rating by ID
     */
    public Optional<Rating> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Rating rating = session.get(Rating.class, id);
            return Optional.ofNullable(rating);
        } catch (Exception e) {
            logger.error("Error finding rating by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Finds rating by user and restaurant (unique constraint)
     */
    public Optional<Rating> findByUserAndRestaurant(User user, Restaurant restaurant) {
        if (user == null || restaurant == null) {
            logger.warn("Cannot find rating with null user or restaurant");
            return Optional.empty();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.user.id = :userId AND r.restaurant.id = :restaurantId";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("userId", user.getId());
            query.setParameter("restaurantId", restaurant.getId());
            
            List<Rating> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error finding rating by user {} and restaurant {}: {}", 
                        user.getId(), restaurant.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Finds all ratings for a restaurant
     */
    public List<Rating> findByRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot find ratings for null restaurant");
            return List.of();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.restaurant.id = :restaurantId ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Finds all ratings by a user
     */
    public List<Rating> findByUser(User user) {
        if (user == null) {
            logger.warn("Cannot find ratings for null user");
            return List.of();
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.user.id = :userId ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("userId", user.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings by user {}: {}", user.getId(), e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Finds ratings by score range
     */
    public List<Rating> findByScoreRange(int minScore, int maxScore) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.ratingScore BETWEEN :minScore AND :maxScore ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("minScore", minScore);
            query.setParameter("maxScore", maxScore);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings by score range {}-{}: {}", 
                        minScore, maxScore, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Finds verified ratings
     */
    public List<Rating> findVerifiedRatings() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.isVerified = true ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding verified ratings: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Finds ratings with reviews (non-empty review text)
     */
    public List<Rating> findRatingsWithReviews() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r WHERE r.reviewText IS NOT NULL AND r.reviewText != '' ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings with reviews: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Finds recent ratings (within specified days)
     */
    public List<Rating> findRecentRatings(int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            String hql = "FROM Rating r WHERE r.createdAt >= :cutoffDate ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setParameter("cutoffDate", cutoffDate);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding recent ratings for {} days: {}", days, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Gets average rating for a restaurant
     */
    public Double getAverageRating(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot get average rating for null restaurant");
            return 0.0;
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT AVG(r.ratingScore) FROM Rating r WHERE r.restaurant.id = :restaurantId";
            Query<Double> query = session.createQuery(hql, Double.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
            
        } catch (Exception e) {
            logger.error("Error getting average rating for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return 0.0;
        }
    }
    
    /**
     * Gets rating count for a restaurant
     */
    public Long getRatingCount(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot get rating count for null restaurant");
            return 0L;
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(r) FROM Rating r WHERE r.restaurant.id = :restaurantId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error getting rating count for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return 0L;
        }
    }
    
    /**
     * Gets rating distribution for a restaurant (count per score)
     */
    public Map<Integer, Long> getRatingDistribution(Restaurant restaurant) {
        if (restaurant == null) {
            logger.warn("Cannot get rating distribution for null restaurant");
            return Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L);
        }
        
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r.ratingScore, COUNT(r) FROM Rating r WHERE r.restaurant.id = :restaurantId GROUP BY r.ratingScore";
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("restaurantId", restaurant.getId());
            
            List<Object[]> results = query.getResultList();
            Map<Integer, Long> distribution = new java.util.HashMap<>();
            
            // Initialize all scores to 0
            for (int i = 1; i <= 5; i++) {
                distribution.put(i, 0L);
            }
            
            // Fill in actual counts
            for (Object[] result : results) {
                Integer score = (Integer) result[0];
                Long count = (Long) result[1];
                distribution.put(score, count);
            }
            
            return distribution;
            
        } catch (Exception e) {
            logger.error("Error getting rating distribution for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L);
        }
    }
    
    /**
     * Gets top rated restaurants
     */
    public List<Object[]> getTopRatedRestaurants(int limit) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = """
                SELECT r.restaurant.id, r.restaurant.name, AVG(r.ratingScore), COUNT(r)
                FROM Rating r 
                GROUP BY r.restaurant.id, r.restaurant.name 
                HAVING COUNT(r) >= 5 
                ORDER BY AVG(r.ratingScore) DESC, COUNT(r) DESC
                """;
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setMaxResults(limit);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error getting top rated restaurants: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Deletes a rating
     */
    public boolean delete(Long id) {
        if (id == null) {
            logger.warn("Cannot delete rating with null ID");
            return false;
        }
        
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Rating rating = session.get(Rating.class, id);
            if (rating != null) {
                session.remove(rating);
                transaction.commit();
                logger.info("Deleted rating with ID: {}", id);
                return true;
            } else {
                transaction.rollback();
                logger.warn("Rating with ID {} not found for deletion", id);
                return false;
            }
            
        } catch (Exception e) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            logger.error("Error deleting rating {}: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Gets all ratings (for admin purposes)
     */
    public List<Rating> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding all ratings: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Gets ratings with pagination
     */
    public List<Rating> findWithPagination(int offset, int limit) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Rating r ORDER BY r.createdAt DESC";
            Query<Rating> query = session.createQuery(hql, Rating.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding ratings with pagination: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Counts total ratings
     */
    public Long countAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(r) FROM Rating r";
            Query<Long> query = session.createQuery(hql, Long.class);
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting all ratings: {}", e.getMessage(), e);
            return 0L;
        }
    }
} 
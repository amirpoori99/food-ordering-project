package com.myapp.favorites;

import com.myapp.common.models.Favorite;
import com.myapp.common.models.Restaurant;
import com.myapp.common.models.User;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Favorite entity operations
 * Handles database operations for user favorites
 */
public class FavoritesRepository {

    private static final Logger logger = LoggerFactory.getLogger(FavoritesRepository.class);

    /**
     * Saves a new favorite or updates existing one
     */
    public Favorite save(Favorite favorite) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            if (favorite.getId() == null) {
                session.persist(favorite);
                logger.info("Created new favorite: {}", favorite);
            } else {
                favorite = session.merge(favorite);
                logger.info("Updated favorite: {}", favorite);
            }
            
            transaction.commit();
            return favorite;
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving favorite: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save favorite", e);
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public Favorite save(long userId, long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, userId);
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            
            if (user == null || restaurant == null) {
                throw new IllegalArgumentException("User or Restaurant not found");
            }
            
            Favorite favorite = new Favorite(user, restaurant);
            return save(favorite);
            
        } catch (Exception e) {
            logger.error("Error saving favorite with userId {} and restaurantId {}: {}", 
                        userId, restaurantId, e.getMessage(), e);
            throw new RuntimeException("Failed to save favorite", e);
        }
    }

    /**
     * Finds favorite by ID
     */
    public Optional<Favorite> findById(Long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Favorite favorite = session.get(Favorite.class, id);
            return Optional.ofNullable(favorite);
        } catch (Exception e) {
            logger.error("Error finding favorite by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Finds favorite by user and restaurant
     */
    public Optional<Favorite> findByUserAndRestaurant(User user, Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId AND f.restaurant.id = :restaurantId";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", user.getId());
            query.setParameter("restaurantId", restaurant.getId());
            
            List<Favorite> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error finding favorite by user {} and restaurant {}: {}", 
                        user.getId(), restaurant.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public Optional<Favorite> find(long userId, long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId AND f.restaurant.id = :restaurantId";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", userId);
            query.setParameter("restaurantId", restaurantId);
            
            List<Favorite> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (Exception e) {
            logger.error("Error finding favorite by userId {} and restaurantId {}: {}", 
                        userId, restaurantId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Gets all favorites for a user
     */
    public List<Favorite> findByUser(User user) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", user.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites for user {}: {}", user.getId(), e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public List<Favorite> listByUser(long userId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("userId", userId);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error listing favorites for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Gets all favorites for a restaurant
     */
    public List<Favorite> findByRestaurant(Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.restaurant.id = :restaurantId ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Gets recent favorites (within specified days)
     */
    public List<Favorite> findRecentFavorites(int days) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.createdAt >= :cutoffDate ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setParameter("cutoffDate", LocalDateTime.now().minusDays(days));
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding recent favorites for {} days: {}", days, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Gets favorites with notes
     */
    public List<Favorite> findFavoritesWithNotes() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f WHERE f.notes IS NOT NULL AND f.notes != '' ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites with notes: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Gets count of favorites for a restaurant
     */
    public Long countByRestaurant(Restaurant restaurant) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f WHERE f.restaurant.id = :restaurantId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("restaurantId", restaurant.getId());
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting favorites for restaurant {}: {}", 
                        restaurant.getId(), e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * Gets count of favorites for a user
     */
    public Long countByUser(User user) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("userId", user.getId());
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting favorites for user {}: {}", user.getId(), e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * Deletes a favorite by ID
     */
    public boolean delete(Long id) {
        Transaction transaction = null;
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Favorite favorite = session.get(Favorite.class, id);
            if (favorite != null) {
                session.remove(favorite);
                logger.info("Deleted favorite with ID: {}", id);
                transaction.commit();
                return true;
            } else {
                logger.warn("Favorite with ID {} not found for deletion", id);
                transaction.rollback();
                return false;
            }
            
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting favorite {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public void delete(long userId, long restaurantId) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Optional<Favorite> favorite = find(userId, restaurantId);
            if (favorite.isPresent()) {
                delete(favorite.get().getId());
            }
        } catch (Exception e) {
            logger.error("Error deleting favorite for user {} and restaurant {}: {}", 
                        userId, restaurantId, e.getMessage(), e);
        }
    }

    /**
     * Gets all favorites (for admin purposes)
     */
    public List<Favorite> findAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding all favorites: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Gets favorites with pagination
     */
    public List<Favorite> findWithPagination(int offset, int limit) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Favorite f ORDER BY f.createdAt DESC";
            Query<Favorite> query = session.createQuery(hql, Favorite.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            return query.getResultList();
            
        } catch (Exception e) {
            logger.error("Error finding favorites with pagination: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Counts total favorites
     */
    public Long countAll() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Favorite f";
            Query<Long> query = session.createQuery(hql, Long.class);
            
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
            
        } catch (Exception e) {
            logger.error("Error counting all favorites: {}", e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    public void clear() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery("DELETE FROM Favorite").executeUpdate();
            transaction.commit();
            logger.info("Cleared all favorites");
        } catch (Exception e) {
            logger.error("Error clearing favorites: {}", e.getMessage(), e);
        }
    }
} 
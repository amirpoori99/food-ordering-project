package com.myapp.vendor;

import com.myapp.common.models.Restaurant;
import com.myapp.common.models.RestaurantStatus;
import com.myapp.common.utils.DatabaseUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Repository class for vendor operations from customer perspective
 * Provides specialized queries for vendor browsing and search
 */
public class VendorRepository {
    
    /**
     * Find vendor by ID
     */
    public Optional<Restaurant> findById(long id) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Restaurant.class, id));
        } catch (Exception e) {
            System.err.println("Error finding vendor by ID: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Search vendors by name or location
     */
    public List<Restaurant> searchVendors(String searchTerm) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant r WHERE r.status = :status AND " +
                        "(LOWER(r.name) LIKE LOWER(:searchTerm) OR " +
                        "LOWER(r.address) LIKE LOWER(:searchTerm))";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error searching vendors: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find vendors by location/area
     */
    public List<Restaurant> findByLocation(String location) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant r WHERE r.status = :status AND " +
                        "LOWER(r.address) LIKE LOWER(:location)";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setParameter("location", "%" + location + "%");
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding vendors by location: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get featured/popular vendors
     * For now, returns approved vendors ordered by creation date (newest first)
     * In future, this could be based on ratings, order count, etc.
     */
    public List<Restaurant> getFeaturedVendors() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant r WHERE r.status = :status " +
                        "ORDER BY r.id DESC";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setMaxResults(10); // Limit to top 10
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting featured vendors: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Find vendors that serve a specific food category
     */
    public List<Restaurant> findByFoodCategory(String category) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT r FROM Restaurant r " +
                        "JOIN FoodItem f ON f.restaurant.id = r.id " +
                        "WHERE r.status = :status AND " +
                        "LOWER(f.category) = LOWER(:category) AND " +
                        "f.available = true";
            
            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            query.setParameter("category", category);
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding vendors by category: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get vendors with their menu item counts
     */
    public List<VendorWithItemCount> getVendorsWithItemCounts() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r.id, r.name, r.address, " +
                        "COUNT(f.id) as itemCount " +
                        "FROM Restaurant r " +
                        "LEFT JOIN FoodItem f ON f.restaurant.id = r.id AND f.available = true " +
                        "WHERE r.status = :status " +
                        "GROUP BY r.id, r.name, r.address " +
                        "ORDER BY itemCount DESC";
            
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            
            List<Object[]> results = query.getResultList();
            List<VendorWithItemCount> vendors = new ArrayList<>();
            
            for (Object[] row : results) {
                Long id = (Long) row[0];
                String name = (String) row[1];
                String address = (String) row[2];
                Long itemCount = (Long) row[3];
                
                vendors.add(new VendorWithItemCount(id, name, address, null, itemCount.intValue()));
            }
            
            return vendors;
        } catch (Exception e) {
            System.err.println("Error getting vendors with item counts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get vendors by multiple filters
     */
    public List<Restaurant> findByFilters(String location, String category, String searchTerm) {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            StringBuilder hqlBuilder = new StringBuilder();
            hqlBuilder.append("SELECT DISTINCT r FROM Restaurant r ");
            
            if (category != null && !category.trim().isEmpty()) {
                hqlBuilder.append("JOIN FoodItem f ON f.restaurant.id = r.id ");
            }
            
            hqlBuilder.append("WHERE r.status = :status ");
            
            if (location != null && !location.trim().isEmpty()) {
                hqlBuilder.append("AND LOWER(r.address) LIKE LOWER(:location) ");
            }
            
            if (category != null && !category.trim().isEmpty()) {
                hqlBuilder.append("AND LOWER(f.category) = LOWER(:category) AND f.available = true ");
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                hqlBuilder.append("AND LOWER(r.name) LIKE LOWER(:searchTerm) ");
            }
            
            Query<Restaurant> query = session.createQuery(hqlBuilder.toString(), Restaurant.class);
            query.setParameter("status", RestaurantStatus.APPROVED);
            
            if (location != null && !location.trim().isEmpty()) {
                query.setParameter("location", "%" + location + "%");
            }
            
            if (category != null && !category.trim().isEmpty()) {
                query.setParameter("category", category);
            }
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                query.setParameter("searchTerm", "%" + searchTerm + "%");
            }
            
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error finding vendors by filters: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Data class for vendor with item count
     */
    public static class VendorWithItemCount {
        private final Long id;
        private final String name;
        private final String address;
        private final String description;
        private final int itemCount;
        
        public VendorWithItemCount(Long id, String name, String address, String description, int itemCount) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.description = description;
            this.itemCount = itemCount;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getDescription() { return description; }
        public int getItemCount() { return itemCount; }
    }
}

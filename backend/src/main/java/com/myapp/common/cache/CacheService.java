package com.myapp.common.cache;

import com.myapp.common.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * High-Level Cache Service for Business Logic
 * 
 * این سرویس cache strategies مختلف برای انواع داده‌ها ارائه می‌دهد
 * 
 * Cache Strategies:
 * - User Data: Medium TTL (30 min)
 * - Restaurant Data: Long TTL (1 hour)
 * - Menu Data: Medium TTL (30 min)
 * - Order Data: Short TTL (5 min)
 * - Analytics Data: Long TTL (1 hour)
 * - Session Data: Short TTL (5 min)
 * 
 * @author Food Ordering System Team
 * @version 1.0 - Production Ready
 */
public class CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    private final RedisCacheManager cacheManager;
    private static CacheService instance;
    
    // Cache Key Prefixes
    private static final String USER_PREFIX = "user:";
    private static final String RESTAURANT_PREFIX = "restaurant:";
    private static final String MENU_PREFIX = "menu:";
    private static final String ITEM_PREFIX = "item:";
    private static final String ORDER_PREFIX = "order:";
    private static final String ANALYTICS_PREFIX = "analytics:";
    private static final String SESSION_PREFIX = "session:";
    private static final String SEARCH_PREFIX = "search:";
    private static final String STATS_PREFIX = "stats:";
    
    /**
     * Private constructor - Singleton pattern
     */
    private CacheService() {
        this.cacheManager = RedisCacheManager.getInstance();
        logger.info("📊 Cache Service initialized with Redis backend");
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized CacheService getInstance() {
        if (instance == null) {
            instance = new CacheService();
        }
        return instance;
    }
    
    // ================================================================
    // USER CACHING
    // ================================================================
    
    /**
     * Cache user data with medium TTL
     */
    public boolean cacheUser(User user) {
        if (user == null || user.getId() == null) return false;
        String key = USER_PREFIX + user.getId();
        return cacheManager.set(key, user, RedisCacheManager.TTL_MEDIUM);
    }
    
    /**
     * Get cached user or execute fallback
     */
    public User getUser(Long userId, Supplier<User> fallback) {
        if (userId == null) return null;
        
        String key = USER_PREFIX + userId;
        User cachedUser = cacheManager.get(key, User.class);
        
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // Cache miss - execute fallback and cache result
        User user = fallback.get();
        if (user != null) {
            cacheUser(user);
        }
        return user;
    }
    
    /**
     * Invalidate user cache
     */
    public boolean invalidateUser(Long userId) {
        if (userId == null) return false;
        String key = USER_PREFIX + userId;
        return cacheManager.delete(key);
    }
    
    // ================================================================
    // RESTAURANT CACHING
    // ================================================================
    
    /**
     * Cache restaurant data with long TTL
     */
    public boolean cacheRestaurant(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) return false;
        String key = RESTAURANT_PREFIX + restaurant.getId();
        return cacheManager.set(key, restaurant, RedisCacheManager.TTL_LONG);
    }
    
    /**
     * Get cached restaurant or execute fallback
     */
    public Restaurant getRestaurant(Long restaurantId, Supplier<Restaurant> fallback) {
        if (restaurantId == null) return null;
        
        String key = RESTAURANT_PREFIX + restaurantId;
        Restaurant cached = cacheManager.get(key, Restaurant.class);
        
        if (cached != null) {
            return cached;
        }
        
        Restaurant restaurant = fallback.get();
        if (restaurant != null) {
            cacheRestaurant(restaurant);
        }
        return restaurant;
    }
    
    /**
     * Cache restaurant list
     */
    public boolean cacheRestaurantList(String cacheKey, List<Restaurant> restaurants) {
        String key = RESTAURANT_PREFIX + "list:" + cacheKey;
        return cacheManager.set(key, restaurants, RedisCacheManager.TTL_MEDIUM);
    }
    
    /**
     * Get cached restaurant list
     */
    @SuppressWarnings("unchecked")
    public List<Restaurant> getRestaurantList(String cacheKey, Supplier<List<Restaurant>> fallback) {
        String key = RESTAURANT_PREFIX + "list:" + cacheKey;
        List<Restaurant> cached = cacheManager.get(key, List.class);
        
        if (cached != null) {
            return cached;
        }
        
        List<Restaurant> restaurants = fallback.get();
        if (restaurants != null) {
            cacheRestaurantList(cacheKey, restaurants);
        }
        return restaurants;
    }
    
    /**
     * Invalidate restaurant cache
     */
    public boolean invalidateRestaurant(Long restaurantId) {
        if (restaurantId == null) return false;
        
        // Delete specific restaurant
        String key = RESTAURANT_PREFIX + restaurantId;
        boolean deleted = cacheManager.delete(key);
        
        // Delete restaurant lists that might contain this restaurant
        cacheManager.deleteByPattern(RESTAURANT_PREFIX + "list:*");
        
        return deleted;
    }
    
    // ================================================================
    // MENU & ITEMS CACHING
    // ================================================================
    
    /**
     * Cache menu items for a restaurant
     */
    public boolean cacheMenuItems(Long restaurantId, List<FoodItem> items) {
        if (restaurantId == null) return false;
        String key = MENU_PREFIX + restaurantId;
        return cacheManager.set(key, items, RedisCacheManager.TTL_MEDIUM);
    }
    
    /**
     * Get cached menu items
     */
    @SuppressWarnings("unchecked")
    public List<FoodItem> getMenuItems(Long restaurantId, Supplier<List<FoodItem>> fallback) {
        if (restaurantId == null) return null;
        
        String key = MENU_PREFIX + restaurantId;
        List<FoodItem> cached = cacheManager.get(key, List.class);
        
        if (cached != null) {
            return cached;
        }
        
        List<FoodItem> items = fallback.get();
        if (items != null) {
            cacheMenuItems(restaurantId, items);
        }
        return items;
    }
    
    /**
     * Cache individual food item
     */
    public boolean cacheItem(FoodItem item) {
        if (item == null || item.getId() == null) return false;
        String key = ITEM_PREFIX + item.getId();
        return cacheManager.set(key, item, RedisCacheManager.TTL_MEDIUM);
    }
    
    /**
     * Get cached food item
     */
    public FoodItem getItem(Long itemId, Supplier<FoodItem> fallback) {
        if (itemId == null) return null;
        
        String key = ITEM_PREFIX + itemId;
        FoodItem cached = cacheManager.get(key, FoodItem.class);
        
        if (cached != null) {
            return cached;
        }
        
        FoodItem item = fallback.get();
        if (item != null) {
            cacheItem(item);
        }
        return item;
    }
    
    /**
     * Invalidate menu cache for restaurant
     */
    public boolean invalidateMenu(Long restaurantId) {
        if (restaurantId == null) return false;
        String key = MENU_PREFIX + restaurantId;
        return cacheManager.delete(key);
    }
    
    // ================================================================
    // ORDER CACHING
    // ================================================================
    
    /**
     * Cache order with short TTL (orders change frequently)
     */
    public boolean cacheOrder(Order order) {
        if (order == null || order.getId() == null) return false;
        String key = ORDER_PREFIX + order.getId();
        return cacheManager.set(key, order, RedisCacheManager.TTL_SHORT);
    }
    
    /**
     * Get cached order
     */
    public Order getOrder(Long orderId, Supplier<Order> fallback) {
        if (orderId == null) return null;
        
        String key = ORDER_PREFIX + orderId;
        Order cached = cacheManager.get(key, Order.class);
        
        if (cached != null) {
            return cached;
        }
        
        Order order = fallback.get();
        if (order != null) {
            cacheOrder(order);
        }
        return order;
    }
    
    /**
     * Cache user's order list
     */
    public boolean cacheUserOrders(Long userId, List<Order> orders) {
        if (userId == null) return false;
        String key = ORDER_PREFIX + "user:" + userId;
        return cacheManager.set(key, orders, RedisCacheManager.TTL_SHORT);
    }
    
    /**
     * Invalidate order cache
     */
    public boolean invalidateOrder(Long orderId) {
        if (orderId == null) return false;
        String key = ORDER_PREFIX + orderId;
        return cacheManager.delete(key);
    }
    
    // ================================================================
    // ANALYTICS CACHING
    // ================================================================
    
    /**
     * Cache analytics data with long TTL
     */
    public boolean cacheAnalytics(String analyticsKey, Object data) {
        String key = ANALYTICS_PREFIX + analyticsKey;
        return cacheManager.set(key, data, RedisCacheManager.TTL_LONG);
    }
    
    /**
     * Get cached analytics data
     */
    public <T> T getAnalytics(String analyticsKey, Class<T> type, Supplier<T> fallback) {
        String key = ANALYTICS_PREFIX + analyticsKey;
        T cached = cacheManager.get(key, type);
        
        if (cached != null) {
            return cached;
        }
        
        T data = fallback.get();
        if (data != null) {
            cacheAnalytics(analyticsKey, data);
        }
        return data;
    }
    
    /**
     * Invalidate analytics cache by pattern
     */
    public boolean invalidateAnalytics(String pattern) {
        return cacheManager.deleteByPattern(ANALYTICS_PREFIX + pattern + "*");
    }
    
    // ================================================================
    // SESSION CACHING
    // ================================================================
    
    /**
     * Cache user session with short TTL
     */
    public boolean cacheSession(String sessionId, Map<String, Object> sessionData) {
        if (sessionId == null) return false;
        String key = SESSION_PREFIX + sessionId;
        return cacheManager.set(key, sessionData, RedisCacheManager.TTL_SHORT);
    }
    
    /**
     * Get cached session
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSession(String sessionId) {
        if (sessionId == null) return null;
        String key = SESSION_PREFIX + sessionId;
        return cacheManager.get(key, Map.class);
    }
    
    /**
     * Invalidate session
     */
    public boolean invalidateSession(String sessionId) {
        if (sessionId == null) return false;
        String key = SESSION_PREFIX + sessionId;
        return cacheManager.delete(key);
    }
    
    // ================================================================
    // SEARCH RESULT CACHING
    // ================================================================
    
    /**
     * Cache search results
     */
    public boolean cacheSearchResults(String searchQuery, Object results) {
        String key = SEARCH_PREFIX + searchQuery.toLowerCase().replaceAll("\\s+", "_");
        return cacheManager.set(key, results, RedisCacheManager.TTL_MEDIUM);
    }
    
    /**
     * Get cached search results
     */
    public <T> T getSearchResults(String searchQuery, Class<T> type) {
        String key = SEARCH_PREFIX + searchQuery.toLowerCase().replaceAll("\\s+", "_");
        return cacheManager.get(key, type);
    }
    
    // ================================================================
    // STATISTICS CACHING
    // ================================================================
    
    /**
     * Cache statistics with daily TTL
     */
    public boolean cacheStats(String statsKey, Object stats) {
        String key = STATS_PREFIX + statsKey;
        return cacheManager.set(key, stats, RedisCacheManager.TTL_DAILY);
    }
    
    /**
     * Get cached statistics
     */
    public <T> T getStats(String statsKey, Class<T> type, Supplier<T> fallback) {
        String key = STATS_PREFIX + statsKey;
        T cached = cacheManager.get(key, type);
        
        if (cached != null) {
            return cached;
        }
        
        T stats = fallback.get();
        if (stats != null) {
            cacheStats(statsKey, stats);
        }
        return stats;
    }
    
    // ================================================================
    // CACHE MANAGEMENT
    // ================================================================
    
    /**
     * Warm up cache with frequently accessed data
     */
    public void warmUpCache() {
        logger.info("🔥 Starting cache warm-up process...");
        
        // Warm-up can be implemented based on business requirements
        // For example: pre-load popular restaurants, frequent searches, etc.
        
        logger.info("✅ Cache warm-up completed");
    }
    
    /**
     * Clear all cache data
     */
    public boolean clearAllCache() {
        logger.warn("🧹 Clearing ALL cache data...");
        return cacheManager.flushAll();
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        return cacheManager.getStatistics();
    }
    
    /**
     * Check cache health
     */
    public boolean isHealthy() {
        return cacheManager.isHealthy();
    }
    
    /**
     * Reset cache statistics
     */
    public void resetStatistics() {
        cacheManager.resetStatistics();
    }
    
    /**
     * Shutdown cache service
     */
    public void shutdown() {
        logger.info("🔒 Shutting down Cache Service...");
        cacheManager.shutdown();
    }
} 
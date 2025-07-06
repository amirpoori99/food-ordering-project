package com.myapp.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis Cache Manager for High-Performance Caching
 * 
 * این کلاس مدیریت cache با Redis برای بهبود عملکرد سیستم
 * 
 * ویژگی‌های کلیدی:
 * - Connection Pooling with Jedis
 * - JSON Serialization/Deserialization
 * - TTL Management
 * - Cache Statistics
 * - Health Monitoring
 * - Pattern-based Cache Invalidation
 * 
 * @author Food Ordering System Team
 * @version 1.0 - Production Ready
 */
public class RedisCacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisCacheManager.class);
    
    // Redis Connection Pool
    private static JedisPool jedisPool;
    private static RedisCacheManager instance;
    
    // JSON Mapper for serialization
    private final ObjectMapper objectMapper;
    
    // Cache Statistics
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long cacheErrors = 0;
    
    // Cache Configuration
    private static final String REDIS_HOST = System.getProperty("redis.host", "localhost");
    private static final int REDIS_PORT = Integer.parseInt(System.getProperty("redis.port", "6379"));
    private static final String REDIS_PASSWORD = System.getProperty("redis.password", null);
    private static final int REDIS_TIMEOUT = Integer.parseInt(System.getProperty("redis.timeout", "2000"));
    private static final int REDIS_DATABASE = Integer.parseInt(System.getProperty("redis.database", "0"));
    
    // TTL Configuration (در ثانیه)
    public static final int TTL_SHORT = 300;      // 5 minutes
    public static final int TTL_MEDIUM = 1800;    // 30 minutes
    public static final int TTL_LONG = 3600;      // 1 hour
    public static final int TTL_DAILY = 86400;    // 24 hours
    
    /**
     * Private constructor - Singleton pattern
     */
    private RedisCacheManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        initializeRedisPool();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized RedisCacheManager getInstance() {
        if (instance == null) {
            instance = new RedisCacheManager();
        }
        return instance;
    }
    
    /**
     * Initialize Redis connection pool
     */
    private void initializeRedisPool() {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            
            // Pool Configuration
            poolConfig.setMaxTotal(50);                // حداکثر تعداد connection
            poolConfig.setMaxIdle(20);                 // حداکثر تعداد connection غیرفعال
            poolConfig.setMinIdle(5);                  // حداقل تعداد connection غیرفعال
            poolConfig.setTestOnBorrow(true);          // تست connection هنگام دریافت
            poolConfig.setTestOnReturn(true);          // تست connection هنگام برگشت
            poolConfig.setTestWhileIdle(true);         // تست connection های غیرفعال
            poolConfig.setNumTestsPerEvictionRun(3);   // تعداد تست در هر دور
            poolConfig.setTimeBetweenEvictionRunsMillis(30000); // 30 seconds
            poolConfig.setBlockWhenExhausted(true);    // انتظار برای connection
            poolConfig.setMaxWaitMillis(2000);         // حداکثر زمان انتظار
            
            // Create Jedis Pool
            if (REDIS_PASSWORD != null && !REDIS_PASSWORD.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT, REDIS_PASSWORD, REDIS_DATABASE);
            } else {
                jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT, REDIS_TIMEOUT);
            }
            
            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                String pong = jedis.ping();
                logger.info("✅ Redis Cache Manager initialized successfully - {}", pong);
                logger.info("📊 Redis Configuration: {}:{} (database: {})", REDIS_HOST, REDIS_PORT, REDIS_DATABASE);
            }
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Redis Cache Manager: {}", e.getMessage());
            // در حالت fallback، cache غیرفعال می‌شود
        }
    }
    
    /**
     * Set value in cache with TTL
     */
    public <T> boolean set(String key, T value, int ttlSeconds) {
        if (jedisPool == null) return false;
        
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = objectMapper.writeValueAsString(value);
            String result = jedis.setex(key, ttlSeconds, jsonValue);
            
            logger.debug("🔄 Cache SET: {} (TTL: {}s)", key, ttlSeconds);
            return "OK".equals(result);
            
        } catch (JsonProcessingException e) {
            logger.error("❌ JSON serialization error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return false;
        } catch (Exception e) {
            logger.error("❌ Redis SET error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return false;
        }
    }
    
    /**
     * Get value from cache
     */
    public <T> T get(String key, Class<T> valueType) {
        if (jedisPool == null) return null;
        
        try (Jedis jedis = jedisPool.getResource()) {
            String jsonValue = jedis.get(key);
            
            if (jsonValue == null) {
                cacheMisses++;
                logger.debug("❌ Cache MISS: {}", key);
                return null;
            }
            
            T value = objectMapper.readValue(jsonValue, valueType);
            cacheHits++;
            logger.debug("✅ Cache HIT: {}", key);
            return value;
            
        } catch (JsonProcessingException e) {
            logger.error("❌ JSON deserialization error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return null;
        } catch (Exception e) {
            logger.error("❌ Redis GET error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return null;
        }
    }
    
    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        if (jedisPool == null) return false;
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("❌ Redis EXISTS error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return false;
        }
    }
    
    /**
     * Delete key from cache
     */
    public boolean delete(String key) {
        if (jedisPool == null) return false;
        
        try (Jedis jedis = jedisPool.getResource()) {
            Long result = jedis.del(key);
            logger.debug("🗑️ Cache DELETE: {} (deleted: {})", key, result > 0);
            return result > 0;
        } catch (Exception e) {
            logger.error("❌ Redis DELETE error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return false;
        }
    }
    
    /**
     * Delete all keys matching pattern
     */
    public boolean deleteByPattern(String pattern) {
        if (jedisPool == null) return false;
        
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys(pattern);
            if (!keys.isEmpty()) {
                Long result = jedis.del(keys.toArray(new String[0]));
                logger.debug("🗑️ Cache DELETE PATTERN: {} (deleted: {} keys)", pattern, result);
                return result > 0;
            }
            return true;
        } catch (Exception e) {
            logger.error("❌ Redis DELETE PATTERN error for pattern {}: {}", pattern, e.getMessage());
            cacheErrors++;
            return false;
        }
    }
    
    /**
     * Set TTL for existing key
     */
    public boolean expire(String key, int ttlSeconds) {
        if (jedisPool == null) return false;
        
        try (Jedis jedis = jedisPool.getResource()) {
            Long result = jedis.expire(key, ttlSeconds);
            return result == 1;
        } catch (Exception e) {
            logger.error("❌ Redis EXPIRE error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return false;
        }
    }
    
    /**
     * Get remaining TTL for key
     */
    public long getTTL(String key) {
        if (jedisPool == null) return -2;
        
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        } catch (Exception e) {
            logger.error("❌ Redis TTL error for key {}: {}", key, e.getMessage());
            cacheErrors++;
            return -2;
        }
    }
    
    /**
     * Flush all cache data
     */
    public boolean flushAll() {
        if (jedisPool == null) return false;
        
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.flushDB();
            logger.warn("🧹 Cache FLUSH ALL executed");
            return "OK".equals(result);
        } catch (Exception e) {
            logger.error("❌ Redis FLUSH ALL error: {}", e.getMessage());
            cacheErrors++;
            return false;
        }
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalRequests = cacheHits + cacheMisses;
        double hitRate = totalRequests > 0 ? (double) cacheHits / totalRequests * 100 : 0;
        
        stats.put("cacheHits", cacheHits);
        stats.put("cacheMisses", cacheMisses);
        stats.put("cacheErrors", cacheErrors);
        stats.put("totalRequests", totalRequests);
        stats.put("hitRate", String.format("%.2f%%", hitRate));
        
        // Redis connection info
        if (jedisPool != null) {
            stats.put("activeConnections", jedisPool.getNumActive());
            stats.put("idleConnections", jedisPool.getNumIdle());
            stats.put("totalConnections", jedisPool.getNumActive() + jedisPool.getNumIdle());
        }
        
        return stats;
    }
    
    /**
     * Health check for Redis
     */
    public boolean isHealthy() {
        if (jedisPool == null) return false;
        
        try (Jedis jedis = jedisPool.getResource()) {
            String pong = jedis.ping();
            return "PONG".equals(pong);
        } catch (Exception e) {
            logger.error("❌ Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset cache statistics
     */
    public void resetStatistics() {
        cacheHits = 0;
        cacheMisses = 0;
        cacheErrors = 0;
        logger.info("📊 Cache statistics reset");
    }
    
    /**
     * Shutdown cache manager
     */
    public void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.info("🔒 Redis Cache Manager shutdown completed");
        }
    }
} 
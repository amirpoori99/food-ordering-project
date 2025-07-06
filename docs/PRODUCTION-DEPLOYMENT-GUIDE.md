# راهنمای استقرار Production برای میلیون‌ها کاربر همزمان

## مشکل SQLite و راه‌حل

### چرا SQLite در Production مناسب نیست؟

```bash
# SQLite محدودیت‌ها:
✗ تنها یک writer در زمان
✗ قفل شدن کل دیتابیس
✗ عدم پشتیبانی از clustering
✗ محدودیت در concurrent connections
```

### راه‌حل‌های Production

## 1. تغییر Database به PostgreSQL/MySQL

### PostgreSQL Setup

```bash
# نصب PostgreSQL
sudo apt-get install postgresql postgresql-contrib

# ایجاد دیتابیس و کاربر
sudo -u postgres psql
CREATE DATABASE food_ordering_prod;
CREATE USER food_ordering_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE food_ordering_prod TO food_ordering_user;
```

### Dependency های مورد نیاز

```xml
<!-- pom.xml -->
<dependencies>
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.1</version>
    </dependency>
    
    <!-- HikariCP Connection Pool -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.0.1</version>
    </dependency>
    
    <!-- Redis for Caching -->
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>5.1.0</version>
    </dependency>
</dependencies>
```

## 2. معماری برای میلیون‌ها کاربر

### Load Balancing

```
                   Internet
                      |
              ┌───────────────┐
              │ Load Balancer │ (Nginx/HAProxy)
              │  (SSL/HTTPS)  │
              └───────────────┘
                      |
      ┌───────────────┼───────────────┐
      │               │               │
┌─────────┐     ┌─────────┐     ┌─────────┐
│App VM 1 │     │App VM 2 │     │App VM 3 │
│Port 8081│     │Port 8081│     │Port 8081│
└─────────┘     └─────────┘     └─────────┘
      │               │               │
      └───────────────┼───────────────┘
                      |
            ┌─────────────────┐
            │ Database Cluster│
            └─────────────────┘
```

### Database Clustering

```
                Master Database
                 (Write Only)
                      |
         ┌────────────┼────────────┐
         │            │            │
    Slave DB 1   Slave DB 2   Slave DB 3
   (Read Only)  (Read Only)  (Read Only)
```

## 3. Configuration های Production

### Application Properties

```properties
# application-production.properties

# Database Master (Writes)
db.master.url=jdbc:postgresql://master-db:5432/food_ordering_prod
db.master.username=food_ordering_user
db.master.password=${DB_PASSWORD}

# Database Slaves (Reads)
db.slave1.url=jdbc:postgresql://slave1-db:5432/food_ordering_prod
db.slave2.url=jdbc:postgresql://slave2-db:5432/food_ordering_prod

# Connection Pool Settings
hikari.maximum-pool-size=50
hikari.minimum-idle=10
hikari.connection-timeout=30000
hikari.idle-timeout=600000
hikari.max-lifetime=1800000

# Redis Cache
redis.host=redis-cluster
redis.port=6379
redis.password=${REDIS_PASSWORD}

# Application Settings
server.port=8081
server.max-connections=10000
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10
```

### Native Production Setup

```yaml
# native-production-setup.yml
version: '3.8'

services:
  # Load Balancer
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/ssl
    depends_on:
      - app1
      - app2
      - app3

  # Application Instances
  app1:
    image: food-ordering:latest
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    depends_on:
      - postgres-master
      - redis

  app2:
    image: food-ordering:latest
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    depends_on:
      - postgres-master
      - redis

  app3:
    image: food-ordering:latest
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    depends_on:
      - postgres-master
      - redis

  # Database Master
  postgres-master:
    image: postgres:15
    environment:
      - POSTGRES_DB=food_ordering_prod
      - POSTGRES_USER=food_ordering_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_master_data:/var/lib/postgresql/data
      - ./postgres-master.conf:/etc/postgresql/postgresql.conf

  # Database Slaves
  postgres-slave1:
    image: postgres:15
    environment:
      - PGUSER=postgres
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_slave1_data:/var/lib/postgresql/data

  # Redis Cache
  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data

volumes:
  postgres_master_data:
  postgres_slave1_data:
  redis_data:
```

## 4. بهینه‌سازی‌های Performance

### Caching Strategy

```java
// مثال استفاده از Redis Cache
@Service
public class RestaurantService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public List<Restaurant> getPopularRestaurants() {
        String cacheKey = "popular_restaurants";
        
        // Check cache first
        List<Restaurant> cached = (List<Restaurant>) redisTemplate.opsForValue()
            .get(cacheKey);
            
        if (cached != null) {
            return cached; // Cache hit
        }
        
        // Cache miss - get from database
        List<Restaurant> restaurants = ProductionDatabaseManager.executeRead(session -> 
            session.createQuery("FROM Restaurant r WHERE r.status = 'ACTIVE' ORDER BY r.rating DESC", Restaurant.class)
                .setMaxResults(50)
                .getResultList()
        );
        
        // Store in cache for 10 minutes
        redisTemplate.opsForValue().set(cacheKey, restaurants, Duration.ofMinutes(10));
        
        return restaurants;
    }
}
```

### Async Processing

```java
// پردازش async برای عملیات سنگین
@Service
public class OrderService {
    
    @Async
    public CompletableFuture<Order> processOrderAsync(Order order) {
        return ProductionDatabaseManager.executeWriteAsync(session -> {
            // پردازش سفارش
            session.save(order);
            
            // ارسال notification async
            notificationService.sendOrderConfirmationAsync(order);
            
            // بروزرسانی inventory async
            inventoryService.updateStockAsync(order.getItems());
            
            return order;
        });
    }
}
```

## 5. Monitoring و Metrics

### Health Checks

```java
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        // Database health
        health.put("database", ProductionDatabaseManager.isHealthy() ? "UP" : "DOWN");
        
        // Redis health
        health.put("cache", checkRedisHealth() ? "UP" : "DOWN");
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        health.put("memory_used_mb", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        health.put("memory_free_mb", runtime.freeMemory() / 1024 / 1024);
        
        // Connection pool stats
        health.put("connection_pool", ProductionDatabaseManager.getConnectionPoolStats());
        
        return ResponseEntity.ok(health);
    }
}
```

### JVM Tuning

```bash
# JVM options برای production
java -server \
     -Xms4g -Xmx8g \
     -XX:+UseG1GC \
     -XX:+UseStringDeduplication \
     -XX:+OptimizeStringConcat \
     -XX:MaxGCPauseMillis=100 \
     -Dspring.profiles.active=production \
     -jar food-ordering-backend.jar
```

## 6. Security در Production

```properties
# Security configurations
# SSL/TLS
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}

# Database encryption
spring.datasource.hikari.connection-init-sql=SET TIME ZONE 'UTC'

# Rate limiting
rate-limit.requests-per-minute=1000
rate-limit.burst-capacity=100
```

## 7. مراحل Migration از SQLite

### مرحله 1: Backup داده‌ها

```bash
# Export data from SQLite
sqlite3 food_ordering.db ".dump" > backup.sql
```

### مرحله 2: تبدیل Schema

```sql
-- تبدیل SQLite schema به PostgreSQL
-- در PostgreSQL:
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(255),
    address VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
```

### مرحله 3: Data Migration

```java
// Script برای migration داده‌ها
public class DataMigrationScript {
    public static void main(String[] args) {
        // Read from SQLite
        Configuration sqliteConfig = new Configuration().configure("hibernate.cfg.xml");
        SessionFactory sqliteFactory = sqliteConfig.buildSessionFactory();
        
        // Write to PostgreSQL
        Configuration pgConfig = new Configuration().configure("hibernate-production.cfg.xml");
        SessionFactory pgFactory = pgConfig.buildSessionFactory();
        
        // Migrate data in batches
        migrateUsers(sqliteFactory, pgFactory);
        migrateRestaurants(sqliteFactory, pgFactory);
        migrateOrders(sqliteFactory, pgFactory);
    }
}
```

## خلاصه

برای پشتیبانی از میلیون‌ها کاربر همزمان:

1. **Database**: PostgreSQL + Master-Slave Replication
2. **Connection Pooling**: HikariCP با تنظیمات بهینه
3. **Caching**: Redis برای کاهش load روی database
4. **Load Balancing**: Multiple application instances
5. **Async Processing**: Non-blocking operations
6. **Monitoring**: Health checks و metrics
7. **Security**: SSL, Rate limiting, Database encryption

این معماری قادر است **10,000+ concurrent users** را پشتیبانی کند و با scaling افقی می‌تواند به میلیون‌ها کاربر برسد. 
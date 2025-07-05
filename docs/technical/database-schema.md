# 🗄️ Database Schema Documentation

## 📋 فهرست مطالب

1. [نمای کلی طراحی](#overview)
2. [Core Tables](#core-tables)
3. [Relationships](#relationships)
4. [Indexes](#indexes)
5. [Performance](#performance)

## 🎯 Overview

### Database Design Principles
- **Normalization**: 3NF normalized design
- **Referential Integrity**: Foreign Key constraints
- **Performance**: Strategic indexing
- **Scalability**: Partition-ready design

### Environment Configuration
```yaml
Production:
  Database: PostgreSQL 15+
  Connection Pool: HikariCP (20-100 connections)
  Backup: Daily + WAL archiving

Development:
  Database: SQLite 3.44+
  Connection Pool: Basic pooling
  Backup: File-based
```

## 🏗️ Core Tables

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Restaurants Table
```sql
CREATE TABLE restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(20),
    rating DECIMAL(2,1) DEFAULT 0.0,
    delivery_fee INTEGER DEFAULT 0,
    minimum_order INTEGER DEFAULT 0,
    is_open BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT REFERENCES users(id),
    restaurant_id BIGINT REFERENCES restaurants(id),
    status VARCHAR(20) DEFAULT 'PENDING',
    subtotal INTEGER NOT NULL,
    delivery_fee INTEGER DEFAULT 0,
    tax INTEGER DEFAULT 0,
    total INTEGER NOT NULL,
    delivery_address TEXT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Food Items Table
```sql
CREATE TABLE food_items (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT REFERENCES restaurants(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    price INTEGER NOT NULL,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔗 Relationships

### Entity Relationships
- Users (1) → Orders (M)
- Restaurants (1) → Orders (M)
- Restaurants (1) → Food Items (M)
- Orders (1) → Order Items (M)
- Orders (1) → Payments (1)
- Users (1) → Wallet (1)

## 📊 Indexes

### Primary Indexes
```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_restaurants_category ON restaurants(category);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_food_items_restaurant_id ON food_items(restaurant_id);
```

### Performance Indexes
```sql
CREATE INDEX idx_orders_status_date ON orders(status, order_date DESC);
CREATE INDEX idx_restaurants_rating ON restaurants(rating DESC);
CREATE INDEX idx_food_items_available ON food_items(restaurant_id) WHERE is_available = true;
```

## ⚡ Performance Considerations

### Query Optimization
- Prepared statements for all queries
- Proper use of indexes
- Avoiding N+1 query problems
- Connection pooling

### Scaling Strategies
- Read replicas for analytical queries
- Partitioning for large tables
- Archiving old data
- Caching frequently accessed data

**نسخه**: 1.0.0
**آخرین به‌روزرسانی**: نوامبر 2024 
# Simple Migration Script: SQLite to PostgreSQL
# اسکریپت ساده migration از SQLite به PostgreSQL

Write-Host "🔄 شروع Migration به PostgreSQL..." -ForegroundColor Green

# تنظیمات
$POSTGRES_DB = "food_ordering_prod"
$POSTGRES_USER = "food_ordering_user"
$POSTGRES_PASSWORD = "FoodOrder2024!Secure"

Write-Host "📋 ایجاد جداول PostgreSQL..." -ForegroundColor Blue

# Schema ساده
$schema = @"
-- ایجاد جداول اصلی
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS restaurants (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    status VARCHAR(50) DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS food_items (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT REFERENCES restaurants(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    available BOOLEAN DEFAULT true,
    quantity INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES users(id),
    restaurant_id BIGINT REFERENCES restaurants(id),
    status VARCHAR(50) DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes برای Performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_restaurants_owner ON restaurants(owner_id);
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);

-- Sample Data
INSERT INTO users (email, password_hash, full_name, role) 
VALUES ('admin@foodorder.com', 'admin123', 'Admin User', 'ADMIN')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (email, password_hash, full_name, role) 
VALUES ('test@test.com', 'test123', 'Test User', 'CUSTOMER')
ON CONFLICT (email) DO NOTHING;

SELECT 'PostgreSQL Schema و Sample Data ایجاد شد!' as result;
"@

# اجرای Schema
try {
    $env:PGPASSWORD = $POSTGRES_PASSWORD
    echo $schema | psql -U $POSTGRES_USER -d $POSTGRES_DB
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Schema PostgreSQL با موفقیت ایجاد شد!" -ForegroundColor Green
    } else {
        Write-Host "❌ خطا در ایجاد Schema" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ خطا: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host "`n🎉 Migration تمام شد!" -ForegroundColor Green
Write-Host "📝 مراحل بعدی:" -ForegroundColor Blue
Write-Host "   1. mvn clean compile" -ForegroundColor Yellow
Write-Host "   2. mvn exec:java" -ForegroundColor Yellow 
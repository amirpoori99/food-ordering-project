-- ================================================================
-- سیستم سفارش غذا - اسکریپت راه‌اندازی دیتابیس production
-- این فایل دیتابیس PostgreSQL را برای محیط production آماده می‌کند
-- شامل: ایجاد دیتابیس، کاربران، جداول مانیتورینگ و تنظیمات امنیتی
-- نسخه: ۲.۰ - پیاده‌سازی فاز ۳۳
-- تاریخ آخرین به‌روزرسانی: تیر ۱۴۰۴
-- ================================================================

-- ================================================================
-- سیستم سفارش غذا - راه‌اندازی دیتابیس PostgreSQL
-- اسکریپت راه‌اندازی اولیه دیتابیس برای محیط production
-- ================================================================

-- ================================================================
-- ۱. ایجاد دیتابیس و کاربر
-- ================================================================
-- اجرا به عنوان PostgreSQL superuser (postgres)  # اجرا به عنوان superuser

-- ایجاد دیتابیس اصلی سیستم  # ایجاد دیتابیس
CREATE DATABASE food_ordering_prod
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- ایجاد کاربر اپلیکیشن با دسترسی‌های محدود  # ایجاد کاربر اپلیکیشن
CREATE USER food_ordering_user WITH ENCRYPTED PASSWORD 'your_secure_password_here';

-- اعطای امتیازات لازم به کاربر اپلیکیشن  # اعطای امتیازات
GRANT CONNECT ON DATABASE food_ordering_prod TO food_ordering_user;
GRANT USAGE ON SCHEMA public TO food_ordering_user;
GRANT CREATE ON SCHEMA public TO food_ordering_user;

-- اتصال به دیتابیس اپلیکیشن  # اتصال به دیتابیس اپلیکیشن
\c food_ordering_prod;

-- اعطای تمام امتیازات روی جداول (فعلی و آینده)  # اعطای تمام امتیازات
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO food_ordering_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO food_ordering_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO food_ordering_user;

-- تنظیم امتیازات پیش‌فرض برای اشیاء آینده  # تنظیم امتیازات پیش‌فرض
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON TABLES TO food_ordering_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON SEQUENCES TO food_ordering_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON FUNCTIONS TO food_ordering_user;

-- ================================================================
-- ۲. ایجاد کاربر پشتیبان (فقط خواندن)
-- ================================================================
-- ایجاد کاربر پشتیبان با دسترسی فقط خواندن  # ایجاد کاربر پشتیبان (فقط خواندن)
CREATE USER food_ordering_backup WITH ENCRYPTED PASSWORD 'backup_password_here';
GRANT CONNECT ON DATABASE food_ordering_prod TO food_ordering_backup;
GRANT USAGE ON SCHEMA public TO food_ordering_backup;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO food_ordering_backup;

-- امتیازات پیش‌فرض کاربر پشتیبان  # امتیازات پیش‌فرض کاربر پشتیبان
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT SELECT ON TABLES TO food_ordering_backup;

-- ================================================================
-- ۳. فعال‌سازی extension های مورد نیاز
-- ================================================================
-- extension UUID برای تولید UUID های منحصر به فرد  # UUID extension برای تولید UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- extension جستجوی متنی کامل  # Full text search extension
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- PostGIS برای ویژگی‌های مکانی (در صورت نیاز)  # PostGIS (if location features needed)
-- CREATE EXTENSION IF NOT EXISTS "postgis";

-- ================================================================
-- ۴. بهینه‌سازی عملکرد دیتابیس
-- ================================================================

-- افزایش work_mem برای عملکرد بهتر  # Increase work_mem for better performance
-- این موارد را به فایل postgresql.conf اضافه کنید:  # Add these to postgresql.conf:
/*
work_mem = 16MB                    -- حافظه کاری برای هر اتصال
shared_buffers = 256MB             -- بافرهای مشترک
effective_cache_size = 1GB         -- اندازه cache موثر
maintenance_work_mem = 256MB       -- حافظه برای نگهداری
max_connections = 200              -- حداکثر اتصالات همزمان
*/

-- ================================================================
-- ۵. ایجاد جدول لاگ audit برای امنیت
-- ================================================================
-- جدول لاگ audit برای ثبت تمام تغییرات در دیتابیس  # ایجاد جدول لاگ audit
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,                    -- شناسه یکتا
    table_name VARCHAR(50) NOT NULL,             -- نام جدول
    operation VARCHAR(10) NOT NULL,              -- نوع عملیات (INSERT, UPDATE, DELETE)
    old_values JSONB,                            -- مقادیر قدیمی
    new_values JSONB,                            -- مقادیر جدید
    user_id BIGINT,                              -- شناسه کاربر
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,  -- زمان عملیات
    ip_address INET,                             -- آدرس IP
    user_agent TEXT                               -- مرورگر کاربر
);

-- ایجاد ایندکس‌های بهینه برای جدول audit  # Index for performance
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_log_table_name ON audit_log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);

-- ================================================================
-- ۶. ایجاد جداول مانیتورینگ سیستم
-- ================================================================

-- جدول مانیتورینگ سلامت سیستم  # System health monitoring
CREATE TABLE IF NOT EXISTS system_health (
    id BIGSERIAL PRIMARY KEY,                    -- شناسه یکتا
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,  -- زمان ثبت
    cpu_usage DECIMAL(5,2),                      -- درصد استفاده CPU
    memory_usage DECIMAL(5,2),                   -- درصد استفاده حافظه
    disk_usage DECIMAL(5,2),                     -- درصد استفاده دیسک
    active_connections INTEGER,                  -- تعداد اتصالات فعال
    response_time_ms INTEGER,                    -- زمان پاسخ‌دهی (میلی‌ثانیه)
    status VARCHAR(20) DEFAULT 'healthy'         -- وضعیت سیستم
);

-- جدول مانیتورینگ endpoint های API  # API endpoint monitoring
CREATE TABLE IF NOT EXISTS api_monitoring (
    id BIGSERIAL PRIMARY KEY,                    -- شناسه یکتا
    endpoint VARCHAR(200) NOT NULL,              -- مسیر API
    method VARCHAR(10) NOT NULL,                 -- روش HTTP
    response_time_ms INTEGER,                    -- زمان پاسخ‌دهی
    status_code INTEGER,                         -- کد وضعیت HTTP
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,  -- زمان درخواست
    error_message TEXT                           -- پیام خطا (در صورت وجود)
);

-- ایجاد ایندکس‌های جداول مانیتورینگ  # Indexes for monitoring tables
CREATE INDEX IF NOT EXISTS idx_system_health_timestamp ON system_health(timestamp);
CREATE INDEX IF NOT EXISTS idx_api_monitoring_timestamp ON api_monitoring(timestamp);
CREATE INDEX IF NOT EXISTS idx_api_monitoring_endpoint ON api_monitoring(endpoint);

-- ================================================================
-- ۷. سیاست‌های نگهداری داده و پاکسازی خودکار
-- ================================================================

-- تابع پاک کردن لاگ‌های audit قدیمی (نگهداری ۹۰ روز)  # Function to clean old audit logs
CREATE OR REPLACE FUNCTION clean_audit_logs()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM audit_log 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '90 days';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- تابع پاک کردن داده‌های سلامت سیستم قدیمی (نگهداری ۳۰ روز)  # Function to clean old system health data
CREATE OR REPLACE FUNCTION clean_system_health()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM system_health 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '30 days';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- تابع پاک کردن داده‌های مانیتورینگ API قدیمی (نگهداری ۷ روز)  # Function to clean old API monitoring data
CREATE OR REPLACE FUNCTION clean_api_monitoring()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM api_monitoring 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '7 days';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- ۸. جدول تأیید پشتیبان‌گیری
-- ================================================================
-- جدول برای ثبت و تأیید پشتیبان‌گیری‌های انجام شده  # جدول تأیید پشتیبان‌گیری
CREATE TABLE IF NOT EXISTS backup_verification (
    id BIGSERIAL PRIMARY KEY,                    -- شناسه یکتا
    backup_date DATE NOT NULL,                   -- تاریخ پشتیبان‌گیری
    backup_size_mb BIGINT,                       -- اندازه پشتیبان (مگابایت)
    tables_count INTEGER,                        -- تعداد جداول
    total_records BIGINT,                        -- تعداد کل رکوردها
    verification_status VARCHAR(20) DEFAULT 'pending',  -- وضعیت تأیید
    verification_timestamp TIMESTAMP WITH TIME ZONE,     -- زمان تأیید
    backup_file_path TEXT,                       -- مسیر فایل پشتیبان
    checksum VARCHAR(64)                         -- checksum فایل
);

-- ================================================================
-- ۹. تنظیمات امنیتی پیشرفته
-- ================================================================

-- تابع hash کردن رمزهای عبور (سازگار با BCrypt)  # Create function to hash passwords
CREATE OR REPLACE FUNCTION hash_password(password TEXT)
RETURNS TEXT AS $$
BEGIN
    -- این یک placeholder است - hash واقعی باید در اپلیکیشن انجام شود
    RETURN crypt(password, gen_salt('bf', 12));
END;
$$ LANGUAGE plpgsql;

-- Row Level Security (در صورت نیاز)
-- ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- ================================================================
-- ۱۰. راه‌اندازی داده‌های اولیه
-- ================================================================

-- درج کاربر مدیر پیش‌فرض (اگر جدول users وجود داشته باشد)
-- این توسط migration اپلیکیشن مدیریت می‌شود، اما به عنوان مرجع نگهداری می‌شود
/*
INSERT INTO users (username, email, password_hash, role, status, created_at)
VALUES (
    'admin',
    'admin@foodordering.com',
    hash_password('admin123'),
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;
*/

-- ================================================================
-- ۱۱. توابع مانیتورینگ عملکرد
-- ================================================================

-- تابع دریافت اندازه دیتابیس  # Function to get database size
CREATE OR REPLACE FUNCTION get_database_size()
RETURNS TABLE(
    database_name TEXT,
    size_mb BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        datname::TEXT,
        pg_database_size(datname) / 1024 / 1024 AS size_mb
    FROM pg_database 
    WHERE datname = current_database();
END;
$$ LANGUAGE plpgsql;

-- تابع دریافت اندازه جداول  # Function to get table sizes
CREATE OR REPLACE FUNCTION get_table_sizes()
RETURNS TABLE(
    schema_name TEXT,
    table_name TEXT,
    size_mb BIGINT,
    row_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        schemaname::TEXT,
        tablename::TEXT,
        pg_total_relation_size(schemaname||'.'||tablename) / 1024 / 1024 AS size_mb,
        COALESCE(
            (SELECT reltuples::BIGINT FROM pg_class WHERE relname = tablename), 
            0
        ) AS row_count
    FROM pg_tables 
    WHERE schemaname = 'public'
    ORDER BY size_mb DESC;
END;
$$ LANGUAGE plpgsql;

-- تابع دریافت اطلاعات اتصالات  # Function to get connection info
CREATE OR REPLACE FUNCTION get_connection_info()
RETURNS TABLE(
    total_connections INTEGER,
    active_connections INTEGER,
    idle_connections INTEGER,
    max_connections INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*)::INTEGER AS total_connections,
        COUNT(CASE WHEN state = 'active' THEN 1 END)::INTEGER AS active_connections,
        COUNT(CASE WHEN state = 'idle' THEN 1 END)::INTEGER AS idle_connections,
        current_setting('max_connections')::INTEGER AS max_connections
    FROM pg_stat_activity;
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- ۱۲. اعطای مجوزهای اجرا
-- ================================================================
-- اعطای مجوز اجرای توابع به کاربران  # Grant execute permissions
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO food_ordering_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO food_ordering_backup;

-- ================================================================
-- راه‌اندازی تکمیل شد
-- ================================================================

-- تأیید راه‌اندازی  # Verify setup
SELECT 'راه‌اندازی دیتابیس با موفقیت تکمیل شد!' AS status;
SELECT version() AS postgresql_version;
SELECT current_database() AS database_name;
SELECT current_user AS connected_as;

-- ================================================================
-- فایل راه‌اندازی پایگاه داده سیستم سفارش غذا
-- این فایل شامل تمام جداول، روابط، ایندکس‌ها و داده‌های اولیه است
-- نویسنده: تیم توسعه
-- تاریخ آخرین ویرایش: تیر ۱۴۰۴
-- نسخه: ۲.۰ - سیستم پایگاه داده پیشرفته
-- ================================================================

-- ================================================================
-- تنظیمات اولیه پایگاه داده
-- ================================================================
PRAGMA foreign_keys = ON;                    -- فعال‌سازی محدودیت‌های کلید خارجی
PRAGMA journal_mode = WAL;                   -- تنظیم حالت journal برای عملکرد بهتر
PRAGMA synchronous = NORMAL;                 -- تنظیم synchronous برای تعادل بین عملکرد و امنیت
PRAGMA cache_size = 10000;                   -- تنظیم اندازه cache برای بهبود عملکرد
PRAGMA temp_store = MEMORY;                  -- ذخیره فایل‌های موقت در حافظه
PRAGMA mmap_size = 268435456;                -- تنظیم اندازه mmap (256MB)

-- ================================================================
-- جدول کاربران (Users)
-- این جدول اطلاعات تمام کاربران سیستم را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا کاربر
    username VARCHAR(50) UNIQUE NOT NULL,    -- نام کاربری (منحصر به فرد)
    password_hash VARCHAR(255) NOT NULL,     -- هش رمز عبور (رمزنگاری شده)
    email VARCHAR(100) UNIQUE NOT NULL,      -- آدرس ایمیل (منحصر به فرد)
    phone VARCHAR(15) UNIQUE,                -- شماره تلفن (منحصر به فرد)
    first_name VARCHAR(50) NOT NULL,         -- نام
    last_name VARCHAR(50) NOT NULL,          -- نام خانوادگی
    address TEXT,                            -- آدرس کامل
    user_type VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- نوع کاربر (CUSTOMER, VENDOR, ADMIN, COURIER)
    is_active BOOLEAN NOT NULL DEFAULT 1,    -- وضعیت فعال بودن حساب
    balance DECIMAL(10,2) DEFAULT 0.00,      -- موجودی حساب (به تومان)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد حساب
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ آخرین به‌روزرسانی
    last_login TIMESTAMP,                    -- تاریخ آخرین ورود
    profile_image VARCHAR(255),              -- مسیر تصویر پروفایل
    verification_code VARCHAR(6),            -- کد تأیید برای احراز هویت
    is_verified BOOLEAN DEFAULT 0,           -- وضعیت تأیید حساب
    reset_token VARCHAR(255),                -- توکن بازنشانی رمز عبور
    reset_token_expires TIMESTAMP            -- تاریخ انقضای توکن بازنشانی
);

-- ================================================================
-- جدول رستوران‌ها (Restaurants)
-- این جدول اطلاعات رستوران‌های موجود در سیستم را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS restaurants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا رستوران
    name VARCHAR(100) NOT NULL,              -- نام رستوران
    description TEXT,                        -- توضیحات رستوران
    address TEXT NOT NULL,                   -- آدرس رستوران
    phone VARCHAR(15) NOT NULL,              -- شماره تلفن رستوران
    email VARCHAR(100),                      -- آدرس ایمیل رستوران
    owner_id INTEGER NOT NULL,               -- شناسه صاحب رستوران (مرتبط با جدول users)
    cuisine_type VARCHAR(50),                -- نوع آشپزی (ایرانی، ایتالیایی، چینی، و...)
    rating DECIMAL(3,2) DEFAULT 0.00,        -- امتیاز متوسط رستوران (۰ تا ۵)
    delivery_fee DECIMAL(8,2) DEFAULT 0.00,  -- هزینه ارسال (به تومان)
    minimum_order DECIMAL(8,2) DEFAULT 0.00, -- حداقل سفارش (به تومان)
    delivery_time_minutes INTEGER DEFAULT 45, -- زمان تحویل تخمینی (به دقیقه)
    is_active BOOLEAN NOT NULL DEFAULT 1,    -- وضعیت فعال بودن رستوران
    is_open BOOLEAN NOT NULL DEFAULT 1,      -- وضعیت باز بودن رستوران
    opening_hours TEXT,                      -- ساعات کاری (JSON format)
    image_url VARCHAR(255),                  -- مسیر تصویر رستوران
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ثبت رستوران
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ آخرین به‌روزرسانی
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE -- محدودیت کلید خارجی
);

-- ================================================================
-- جدول منوها (Menus)
-- این جدول منوهای رستوران‌ها را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS menus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا منو
    restaurant_id INTEGER NOT NULL,          -- شناسه رستوران (مرتبط با جدول restaurants)
    name VARCHAR(100) NOT NULL,              -- نام منو
    description TEXT,                        -- توضیحات منو
    is_active BOOLEAN NOT NULL DEFAULT 1,    -- وضعیت فعال بودن منو
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد منو
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ آخرین به‌روزرسانی
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE -- محدودیت کلید خارجی
);

-- ================================================================
-- جدول دسته‌بندی‌های غذا (Categories)
-- این جدول دسته‌بندی‌های مختلف غذاها را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا دسته‌بندی
    name VARCHAR(50) NOT NULL,               -- نام دسته‌بندی
    description TEXT,                        -- توضیحات دسته‌بندی
    image_url VARCHAR(255),                  -- مسیر تصویر دسته‌بندی
    is_active BOOLEAN NOT NULL DEFAULT 1,    -- وضعیت فعال بودن دسته‌بندی
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد دسته‌بندی
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- تاریخ آخرین به‌روزرسانی
);

-- ================================================================
-- جدول آیتم‌های منو (Menu Items)
-- این جدول آیتم‌های موجود در منوهای رستوران‌ها را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS menu_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا آیتم
    menu_id INTEGER NOT NULL,                -- شناسه منو (مرتبط با جدول menus)
    category_id INTEGER,                     -- شناسه دسته‌بندی (مرتبط با جدول categories)
    name VARCHAR(100) NOT NULL,              -- نام آیتم
    description TEXT,                        -- توضیحات آیتم
    price DECIMAL(8,2) NOT NULL,             -- قیمت (به تومان)
    image_url VARCHAR(255),                  -- مسیر تصویر آیتم
    is_vegetarian BOOLEAN DEFAULT 0,         -- آیا گیاهی است؟
    is_spicy BOOLEAN DEFAULT 0,              -- آیا تند است؟
    is_available BOOLEAN NOT NULL DEFAULT 1, -- آیا موجود است؟
    preparation_time_minutes INTEGER DEFAULT 15, -- زمان آماده‌سازی (به دقیقه)
    calories INTEGER,                        -- کالری (اختیاری)
    allergens TEXT,                          -- مواد حساسیت‌زا (JSON format)
    nutritional_info TEXT,                   -- اطلاعات تغذیه‌ای (JSON format)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد آیتم
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ آخرین به‌روزرسانی
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL -- محدودیت کلید خارجی
);

-- ================================================================
-- جدول سفارش‌ها (Orders)
-- این جدول تمام سفارش‌های انجام شده را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا سفارش
    customer_id INTEGER NOT NULL,            -- شناسه مشتری (مرتبط با جدول users)
    restaurant_id INTEGER NOT NULL,          -- شناسه رستوران (مرتبط با جدول restaurants)
    courier_id INTEGER,                      -- شناسه پیک (مرتبط با جدول users)
    order_number VARCHAR(20) UNIQUE NOT NULL, -- شماره سفارش (منحصر به فرد)
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- وضعیت سفارش (PENDING, CONFIRMED, PREPARING, READY, DELIVERING, DELIVERED, CANCELLED)
    total_amount DECIMAL(10,2) NOT NULL,     -- مبلغ کل سفارش (به تومان)
    delivery_fee DECIMAL(8,2) DEFAULT 0.00,  -- هزینه ارسال (به تومان)
    discount_amount DECIMAL(8,2) DEFAULT 0.00, -- مبلغ تخفیف (به تومان)
    final_amount DECIMAL(10,2) NOT NULL,     -- مبلغ نهایی (به تومان)
    delivery_address TEXT NOT NULL,          -- آدرس تحویل
    delivery_instructions TEXT,              -- دستورالعمل‌های تحویل
    payment_method VARCHAR(20) NOT NULL,     -- روش پرداخت (CASH, CARD, WALLET)
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- وضعیت پرداخت (PENDING, PAID, FAILED)
    estimated_delivery_time TIMESTAMP,       -- زمان تخمینی تحویل
    actual_delivery_time TIMESTAMP,          -- زمان واقعی تحویل
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- زمان ثبت سفارش
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ آخرین به‌روزرسانی
    cancellation_reason TEXT,                -- دلیل لغو سفارش
    rating INTEGER CHECK (rating >= 1 AND rating <= 5), -- امتیاز سفارش (۱ تا ۵)
    review_text TEXT,                        -- متن نظر
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (courier_id) REFERENCES users(id) ON DELETE SET NULL -- محدودیت کلید خارجی
);

-- ================================================================
-- جدول آیتم‌های سفارش (Order Items)
-- این جدول آیتم‌های موجود در هر سفارش را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا آیتم سفارش
    order_id INTEGER NOT NULL,               -- شناسه سفارش (مرتبط با جدول orders)
    menu_item_id INTEGER NOT NULL,           -- شناسه آیتم منو (مرتبط با جدول menu_items)
    quantity INTEGER NOT NULL DEFAULT 1,     -- تعداد
    unit_price DECIMAL(8,2) NOT NULL,        -- قیمت واحد (به تومان)
    total_price DECIMAL(8,2) NOT NULL,       -- قیمت کل (به تومان)
    special_instructions TEXT,               -- دستورالعمل‌های خاص
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد آیتم سفارش
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE -- محدودیت کلید خارجی
);

-- ================================================================
-- جدول کوپن‌ها (Coupons)
-- این جدول کوپن‌های تخفیف موجود در سیستم را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS coupons (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا کوپن
    code VARCHAR(20) UNIQUE NOT NULL,        -- کد کوپن (منحصر به فرد)
    name VARCHAR(100) NOT NULL,              -- نام کوپن
    description TEXT,                        -- توضیحات کوپن
    discount_type VARCHAR(10) NOT NULL,      -- نوع تخفیف (PERCENTAGE, FIXED_AMOUNT)
    discount_value DECIMAL(8,2) NOT NULL,    -- مقدار تخفیف
    minimum_order_amount DECIMAL(8,2) DEFAULT 0.00, -- حداقل مبلغ سفارش
    maximum_discount DECIMAL(8,2),           -- حداکثر تخفیف
    usage_limit INTEGER,                     -- محدودیت تعداد استفاده
    used_count INTEGER DEFAULT 0,            -- تعداد استفاده شده
    valid_from TIMESTAMP NOT NULL,           -- تاریخ شروع اعتبار
    valid_until TIMESTAMP NOT NULL,          -- تاریخ پایان اعتبار
    is_active BOOLEAN NOT NULL DEFAULT 1,    -- وضعیت فعال بودن کوپن
    applicable_restaurants TEXT,             -- رستوران‌های قابل استفاده (JSON array)
    applicable_categories TEXT,              -- دسته‌بندی‌های قابل استفاده (JSON array)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد کوپن
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- تاریخ آخرین به‌روزرسانی
);

-- ================================================================
-- جدول استفاده از کوپن‌ها (Coupon Usage)
-- این جدول استفاده‌های انجام شده از کوپن‌ها را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS coupon_usage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا استفاده
    coupon_id INTEGER NOT NULL,              -- شناسه کوپن (مرتبط با جدول coupons)
    user_id INTEGER NOT NULL,                -- شناسه کاربر (مرتبط با جدول users)
    order_id INTEGER NOT NULL,               -- شناسه سفارش (مرتبط با جدول orders)
    discount_amount DECIMAL(8,2) NOT NULL,   -- مبلغ تخفیف اعمال شده
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ استفاده
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE -- محدودیت کلید خارجی
);

-- ================================================================
-- جدول تراکنش‌های مالی (Transactions)
-- این جدول تمام تراکنش‌های مالی سیستم را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا تراکنش
    user_id INTEGER NOT NULL,                -- شناسه کاربر (مرتبط با جدول users)
    order_id INTEGER,                        -- شناسه سفارش (مرتبط با جدول orders)
    transaction_type VARCHAR(20) NOT NULL,   -- نوع تراکنش (DEPOSIT, WITHDRAWAL, PAYMENT, REFUND)
    amount DECIMAL(10,2) NOT NULL,           -- مبلغ تراکنش (به تومان)
    balance_before DECIMAL(10,2) NOT NULL,   -- موجودی قبل از تراکنش
    balance_after DECIMAL(10,2) NOT NULL,    -- موجودی بعد از تراکنش
    payment_method VARCHAR(20),              -- روش پرداخت
    transaction_id VARCHAR(100),             -- شناسه تراکنش خارجی
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- وضعیت تراکنش (PENDING, COMPLETED, FAILED, CANCELLED)
    description TEXT,                        -- توضیحات تراکنش
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد تراکنش
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ آخرین به‌روزرسانی
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL -- محدودیت کلید خارجی
);

-- ================================================================
-- جدول علاقه‌مندی‌ها (Favorites)
-- این جدول آیتم‌های مورد علاقه کاربران را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا علاقه‌مندی
    user_id INTEGER NOT NULL,                -- شناسه کاربر (مرتبط با جدول users)
    menu_item_id INTEGER NOT NULL,           -- شناسه آیتم منو (مرتبط با جدول menu_items)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ اضافه شدن به علاقه‌مندی‌ها
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    UNIQUE(user_id, menu_item_id)            -- محدودیت یکتا بودن ترکیب کاربر و آیتم
);

-- ================================================================
-- جدول اعلان‌ها (Notifications)
-- این جدول اعلان‌های سیستم را نگهداری می‌کند
-- ================================================================
CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,    -- شناسه یکتا اعلان
    user_id INTEGER NOT NULL,                -- شناسه کاربر (مرتبط با جدول users)
    title VARCHAR(100) NOT NULL,             -- عنوان اعلان
    message TEXT NOT NULL,                   -- متن اعلان
    notification_type VARCHAR(20) NOT NULL,  -- نوع اعلان (ORDER_UPDATE, PROMOTION, SYSTEM)
    is_read BOOLEAN NOT NULL DEFAULT 0,      -- وضعیت خوانده شدن
    related_order_id INTEGER,                -- شناسه سفارش مرتبط (مرتبط با جدول orders)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- تاریخ ایجاد اعلان
    read_at TIMESTAMP,                       -- تاریخ خوانده شدن
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, -- محدودیت کلید خارجی
    FOREIGN KEY (related_order_id) REFERENCES orders(id) ON DELETE SET NULL -- محدودیت کلید خارجی
);

-- ================================================================
-- ایجاد ایندکس‌ها برای بهبود عملکرد
-- ================================================================

-- ایندکس برای جدول users
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_user_type ON users(user_type);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- ایندکس برای جدول restaurants
CREATE INDEX IF NOT EXISTS idx_restaurants_owner_id ON restaurants(owner_id);
CREATE INDEX IF NOT EXISTS idx_restaurants_cuisine_type ON restaurants(cuisine_type);
CREATE INDEX IF NOT EXISTS idx_restaurants_is_active ON restaurants(is_active);
CREATE INDEX IF NOT EXISTS idx_restaurants_is_open ON restaurants(is_open);
CREATE INDEX IF NOT EXISTS idx_restaurants_rating ON restaurants(rating);

-- ایندکس برای جدول menus
CREATE INDEX IF NOT EXISTS idx_menus_restaurant_id ON menus(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_menus_is_active ON menus(is_active);

-- ایندکس برای جدول menu_items
CREATE INDEX IF NOT EXISTS idx_menu_items_menu_id ON menu_items(menu_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_category_id ON menu_items(category_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_is_available ON menu_items(is_available);
CREATE INDEX IF NOT EXISTS idx_menu_items_price ON menu_items(price);

-- ایندکس برای جدول orders
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_orders_courier_id ON orders(courier_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_order_time ON orders(order_time);
CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders(order_number);

-- ایندکس برای جدول order_items
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id ON order_items(menu_item_id);

-- ایندکس برای جدول coupons
CREATE INDEX IF NOT EXISTS idx_coupons_code ON coupons(code);
CREATE INDEX IF NOT EXISTS idx_coupons_is_active ON coupons(is_active);
CREATE INDEX IF NOT EXISTS idx_coupons_valid_from ON coupons(valid_from);
CREATE INDEX IF NOT EXISTS idx_coupons_valid_until ON coupons(valid_until);

-- ایندکس برای جدول coupon_usage
CREATE INDEX IF NOT EXISTS idx_coupon_usage_coupon_id ON coupon_usage(coupon_id);
CREATE INDEX IF NOT EXISTS idx_coupon_usage_user_id ON coupon_usage(user_id);
CREATE INDEX IF NOT EXISTS idx_coupon_usage_order_id ON coupon_usage(order_id);

-- ایندکس برای جدول transactions
CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_order_id ON transactions(order_id);
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_type ON transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);

-- ایندکس برای جدول favorites
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_favorites_menu_item_id ON favorites(menu_item_id);

-- ایندکس برای جدول notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

-- ================================================================
-- درج داده‌های اولیه
-- ================================================================

-- درج دسته‌بندی‌های غذا
INSERT OR IGNORE INTO categories (id, name, description) VALUES
(1, 'پیتزا', 'انواع پیتزاهای مختلف'),
(2, 'برگر', 'انواع برگر و ساندویچ'),
(3, 'سوشی', 'انواع سوشی و غذاهای ژاپنی'),
(4, 'کباب', 'انواع کباب ایرانی'),
(5, 'پاستا', 'انواع پاستا و غذاهای ایتالیایی'),
(6, 'سالاد', 'انواع سالاد و سبزیجات'),
(7, 'نوشیدنی', 'انواع نوشیدنی‌های سرد و گرم'),
(8, 'دسر', 'انواع دسر و شیرینی');

-- درج کوپن‌های نمونه
INSERT OR IGNORE INTO coupons (code, name, description, discount_type, discount_value, minimum_order_amount, valid_from, valid_until) VALUES
('WELCOME10', 'کوپن خوش‌آمدگویی', '۱۰٪ تخفیف برای سفارش‌های بالای ۵۰,۰۰۰ تومان', 'PERCENTAGE', 10.00, 50000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59'),
('FREEDELIVERY', 'ارسال رایگان', 'ارسال رایگان برای سفارش‌های بالای ۱۰۰,۰۰۰ تومان', 'FIXED_AMOUNT', 15000.00, 100000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59'),
('NEWUSER20', 'تخفیف کاربر جدید', '۲۰٪ تخفیف برای کاربران جدید', 'PERCENTAGE', 20.00, 30000.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59');

-- ================================================================
-- ایجاد Trigger برای به‌روزرسانی خودکار updated_at
-- ================================================================

-- Trigger برای جدول users
CREATE TRIGGER IF NOT EXISTS update_users_updated_at
    AFTER UPDATE ON users
    FOR EACH ROW
BEGIN
    UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger برای جدول restaurants
CREATE TRIGGER IF NOT EXISTS update_restaurants_updated_at
    AFTER UPDATE ON restaurants
    FOR EACH ROW
BEGIN
    UPDATE restaurants SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger برای جدول menus
CREATE TRIGGER IF NOT EXISTS update_menus_updated_at
    AFTER UPDATE ON menus
    FOR EACH ROW
BEGIN
    UPDATE menus SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger برای جدول categories
CREATE TRIGGER IF NOT EXISTS update_categories_updated_at
    AFTER UPDATE ON categories
    FOR EACH ROW
BEGIN
    UPDATE categories SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger برای جدول menu_items
CREATE TRIGGER IF NOT EXISTS update_menu_items_updated_at
    AFTER UPDATE ON menu_items
    FOR EACH ROW
BEGIN
    UPDATE menu_items SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger برای جدول orders
CREATE TRIGGER IF NOT EXISTS update_orders_updated_at
    AFTER UPDATE ON orders
    FOR EACH ROW
BEGIN
    UPDATE orders SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger برای جدول coupons
CREATE TRIGGER IF NOT EXISTS update_coupons_updated_at
    AFTER UPDATE ON coupons
    FOR EACH ROW
BEGIN
    UPDATE coupons SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- Trigger برای جدول transactions
CREATE TRIGGER IF NOT EXISTS update_transactions_updated_at
    AFTER UPDATE ON transactions
    FOR EACH ROW
BEGIN
    UPDATE transactions SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- ================================================================
-- ایجاد View های مفید
-- ================================================================

-- View برای نمایش اطلاعات کامل سفارش‌ها
CREATE VIEW IF NOT EXISTS order_details AS
SELECT 
    o.id,
    o.order_number,
    o.status,
    o.total_amount,
    o.final_amount,
    o.order_time,
    c.first_name || ' ' || c.last_name as customer_name,
    c.phone as customer_phone,
    r.name as restaurant_name,
    r.phone as restaurant_phone,
    cou.first_name || ' ' || cou.last_name as courier_name
FROM orders o
JOIN users c ON o.customer_id = c.id
JOIN restaurants r ON o.restaurant_id = r.id
LEFT JOIN users cou ON o.courier_id = cou.id;

-- View برای نمایش آمار رستوران‌ها
CREATE VIEW IF NOT EXISTS restaurant_stats AS
SELECT 
    r.id,
    r.name,
    r.cuisine_type,
    r.rating,
    COUNT(o.id) as total_orders,
    AVG(o.final_amount) as avg_order_amount,
    SUM(o.final_amount) as total_revenue
FROM restaurants r
LEFT JOIN orders o ON r.id = o.restaurant_id
WHERE o.status = 'DELIVERED'
GROUP BY r.id, r.name, r.cuisine_type, r.rating;

-- View برای نمایش کوپن‌های فعال
CREATE VIEW IF NOT EXISTS active_coupons AS
SELECT 
    id,
    code,
    name,
    description,
    discount_type,
    discount_value,
    minimum_order_amount,
    usage_limit,
    used_count,
    valid_from,
    valid_until
FROM coupons
WHERE is_active = 1 
    AND valid_from <= CURRENT_TIMESTAMP 
    AND valid_until >= CURRENT_TIMESTAMP
    AND (usage_limit IS NULL OR used_count < usage_limit);

-- ================================================================
-- پایان فایل راه‌اندازی پایگاه داده
-- ================================================================ 
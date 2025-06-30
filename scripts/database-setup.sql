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
-- ================================================================
-- FOOD ORDERING SYSTEM - DATABASE PRODUCTION SETUP
-- PostgreSQL Database Initialization Script
-- ================================================================

-- ================================================================
-- 1. CREATE DATABASE AND USER
-- ================================================================
-- Run as PostgreSQL superuser (postgres)

-- Create database
CREATE DATABASE food_ordering_prod
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Create application user
CREATE USER food_ordering_user WITH ENCRYPTED PASSWORD 'your_secure_password_here';

-- Grant privileges
GRANT CONNECT ON DATABASE food_ordering_prod TO food_ordering_user;
GRANT USAGE ON SCHEMA public TO food_ordering_user;
GRANT CREATE ON SCHEMA public TO food_ordering_user;

-- Connect to the application database
\c food_ordering_prod;

-- Grant all privileges on all tables (current and future)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO food_ordering_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO food_ordering_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO food_ordering_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON TABLES TO food_ordering_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON SEQUENCES TO food_ordering_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON FUNCTIONS TO food_ordering_user;

-- ================================================================
-- 2. CREATE BACKUP USER (READ-ONLY)
-- ================================================================
CREATE USER food_ordering_backup WITH ENCRYPTED PASSWORD 'backup_password_here';
GRANT CONNECT ON DATABASE food_ordering_prod TO food_ordering_backup;
GRANT USAGE ON SCHEMA public TO food_ordering_backup;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO food_ordering_backup;

-- Default privileges for backup user
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT SELECT ON TABLES TO food_ordering_backup;

-- ================================================================
-- 3. ENABLE EXTENSIONS
-- ================================================================
-- UUID extension for generating UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Full text search extension
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- PostGIS (if location features needed)
-- CREATE EXTENSION IF NOT EXISTS "postgis";

-- ================================================================
-- 4. PERFORMANCE OPTIMIZATIONS
-- ================================================================

-- Increase work_mem for better performance
-- Add these to postgresql.conf:
/*
work_mem = 16MB
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 256MB
max_connections = 200
*/

-- ================================================================
-- 5. CREATE AUDIT LOGGING TABLE
-- ================================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    user_id BIGINT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- Index for performance
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_log_table_name ON audit_log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);

-- ================================================================
-- 6. CREATE SYSTEM MONITORING TABLES
-- ================================================================

-- System health monitoring
CREATE TABLE IF NOT EXISTS system_health (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cpu_usage DECIMAL(5,2),
    memory_usage DECIMAL(5,2),
    disk_usage DECIMAL(5,2),
    active_connections INTEGER,
    response_time_ms INTEGER,
    status VARCHAR(20) DEFAULT 'healthy'
);

-- API endpoint monitoring
CREATE TABLE IF NOT EXISTS api_monitoring (
    id BIGSERIAL PRIMARY KEY,
    endpoint VARCHAR(200) NOT NULL,
    method VARCHAR(10) NOT NULL,
    response_time_ms INTEGER,
    status_code INTEGER,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT
);

-- Indexes for monitoring tables
CREATE INDEX IF NOT EXISTS idx_system_health_timestamp ON system_health(timestamp);
CREATE INDEX IF NOT EXISTS idx_api_monitoring_timestamp ON api_monitoring(timestamp);
CREATE INDEX IF NOT EXISTS idx_api_monitoring_endpoint ON api_monitoring(endpoint);

-- ================================================================
-- 7. DATA RETENTION POLICIES
-- ================================================================

-- Function to clean old audit logs (keep last 90 days)
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

-- Function to clean old system health data (keep last 30 days)
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

-- Function to clean old API monitoring data (keep last 7 days)
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
-- 8. BACKUP VERIFICATION TABLE
-- ================================================================
CREATE TABLE IF NOT EXISTS backup_verification (
    id BIGSERIAL PRIMARY KEY,
    backup_date DATE NOT NULL,
    backup_size_mb BIGINT,
    tables_count INTEGER,
    total_records BIGINT,
    verification_status VARCHAR(20) DEFAULT 'pending',
    verification_timestamp TIMESTAMP WITH TIME ZONE,
    backup_file_path TEXT,
    checksum VARCHAR(64)
);

-- ================================================================
-- 9. SECURITY SETTINGS
-- ================================================================

-- Create function to hash passwords (BCrypt-compatible)
CREATE OR REPLACE FUNCTION hash_password(password TEXT)
RETURNS TEXT AS $$
BEGIN
    -- This is a placeholder - actual hashing should be done in application
    RETURN crypt(password, gen_salt('bf', 12));
END;
$$ LANGUAGE plpgsql;

-- Row Level Security (if needed)
-- ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- ================================================================
-- 10. INITIAL DATA SETUP
-- ================================================================

-- Insert default admin user (if users table exists)
-- This will be handled by application migration, but kept as reference
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
-- 11. PERFORMANCE MONITORING FUNCTIONS
-- ================================================================

-- Function to get database size
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

-- Function to get table sizes
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

-- Function to get connection info
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
-- 12. GRANT EXECUTE PERMISSIONS
-- ================================================================
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO food_ordering_user;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO food_ordering_backup;

-- ================================================================
-- SETUP COMPLETE
-- ================================================================

-- Verify setup
SELECT 'Database setup completed successfully!' AS status;
SELECT version() AS postgresql_version;
SELECT current_database() AS database_name;
SELECT current_user AS connected_as; 
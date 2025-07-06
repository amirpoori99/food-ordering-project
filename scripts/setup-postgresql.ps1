# Setup PostgreSQL for Food Ordering Application
# اسکریپت نصب و راه‌اندازی PostgreSQL برای Production

Write-Host "Starting PostgreSQL setup for Production..." -ForegroundColor Green

# تنظیمات
$POSTGRES_VERSION = "17"
$DB_NAME = "food_ordering_prod"
$DB_USER = "food-ordering-project"
$DB_PASSWORD = "food-ordering-project"
$POSTGRES_PORT = "5432"

# مسیر کامل psql
$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"

# بررسی وجود PostgreSQL
Write-Host "Checking PostgreSQL installation..." -ForegroundColor Blue

if (Test-Path $PSQL_PATH) {
    Write-Host "PostgreSQL is installed" -ForegroundColor Green
} else {
    Write-Host "PostgreSQL not found. Please install it:" -ForegroundColor Red
    Write-Host "   1. Download from https://www.postgresql.org/download/windows/" -ForegroundColor Yellow
    Write-Host "   2. Or use Chocolatey: choco install postgresql" -ForegroundColor Yellow
    Write-Host "   3. Or use Scoop: scoop install postgresql" -ForegroundColor Yellow
    exit 1
}

# ایجاد دیتابیس و کاربر
Write-Host "Creating database and user..." -ForegroundColor Blue

$createDbScript = @"
-- ایجاد کاربر جدید
CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';

-- ایجاد دیتابیس
CREATE DATABASE $DB_NAME OWNER $DB_USER;

-- اعطای مجوزها
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
GRANT CONNECT ON DATABASE $DB_NAME TO $DB_USER;

-- اتصال به دیتابیس جدید و اعطای مجوزهای schema
\c $DB_NAME;
GRANT ALL ON SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;

-- تنظیمات performance
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;

SELECT pg_reload_conf();

\q
"@

# ذخیره script در فایل موقت
$tempScriptPath = "$env:TEMP\create_db.sql"
$createDbScript | Out-File -FilePath $tempScriptPath -Encoding UTF8

try {
    # اجرای script با کاربر postgres
    Write-Host "Executing database creation script..." -ForegroundColor Blue
    & $PSQL_PATH -U postgres -f $tempScriptPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database created successfully!" -ForegroundColor Green
    } else {
        Write-Host "Error creating database" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Error connecting to PostgreSQL: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure:" -ForegroundColor Yellow
    Write-Host "   - PostgreSQL service is running" -ForegroundColor Yellow
    Write-Host "   - postgres user is configured with correct password" -ForegroundColor Yellow
    exit 1
} finally {
    # پاک کردن فایل موقت
    Remove-Item $tempScriptPath -ErrorAction SilentlyContinue
}

# تست اتصال
Write-Host "Testing database connection..." -ForegroundColor Blue

$testConnectionScript = @"
SELECT 
    'Database: ' || current_database() as info
UNION ALL
SELECT 
    'User: ' || current_user as info
UNION ALL
SELECT 
    'Version: ' || version() as info;
"@

$testScriptPath = "$env:TEMP\test_connection.sql"
$testConnectionScript | Out-File -FilePath $testScriptPath -Encoding UTF8

try {
    $env:PGPASSWORD = $DB_PASSWORD
    & $PSQL_PATH -h localhost -p $POSTGRES_PORT -U $DB_USER -d $DB_NAME -f $testScriptPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database connection test successful!" -ForegroundColor Green
    } else {
        Write-Host "Connection test error" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Connection test error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Remove-Item $testScriptPath -ErrorAction SilentlyContinue
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

# ایجاد فایل تنظیمات
Write-Host "Creating configuration file..." -ForegroundColor Blue

$configContent = @"
# PostgreSQL Configuration for Food Ordering App
# تنظیمات اتصال به دیتابیس

DATABASE_URL=jdbc:postgresql://localhost:$POSTGRES_PORT/$DB_NAME
DATABASE_USERNAME=$DB_USER
DATABASE_PASSWORD=$DB_PASSWORD

# HikariCP Settings
HIKARI_MAXIMUM_POOL_SIZE=50
HIKARI_MINIMUM_IDLE=10
HIKARI_CONNECTION_TIMEOUT=30000
HIKARI_IDLE_TIMEOUT=600000
HIKARI_MAX_LIFETIME=1800000

# Application Settings  
SERVER_PORT=8081
"@

$configPath = "scripts\postgresql.env"
$configContent | Out-File -FilePath $configPath -Encoding UTF8

Write-Host "Configuration file saved to: $configPath" -ForegroundColor Green

# راهنمای استفاده
Write-Host "PostgreSQL setup completed successfully!" -ForegroundColor Green
Write-Host "Connection information:" -ForegroundColor Blue
Write-Host "   Database: $DB_NAME" -ForegroundColor White
Write-Host "   User: $DB_USER" -ForegroundColor White
Write-Host "   Password: $DB_PASSWORD" -ForegroundColor White
Write-Host "   Host: localhost" -ForegroundColor White
Write-Host "   Port: $POSTGRES_PORT" -ForegroundColor White

Write-Host "Next steps:" -ForegroundColor Blue
Write-Host "   1. Run migration script: .\scripts\simple-migration.ps1" -ForegroundColor Yellow
Write-Host "   2. Build project: mvn clean compile" -ForegroundColor Yellow
Write-Host "   3. Run application: mvn exec:java" -ForegroundColor Yellow

Write-Host "Manual connection:" -ForegroundColor Blue
Write-Host "   & '$PSQL_PATH' -h localhost -p $POSTGRES_PORT -U $DB_USER -d $DB_NAME" -ForegroundColor White

Write-Host "Ready for millions of concurrent users!" -ForegroundColor Green 
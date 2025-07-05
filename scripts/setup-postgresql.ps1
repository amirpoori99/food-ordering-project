# Setup PostgreSQL for Food Ordering Application
# اسکریپت نصب و راه‌اندازی PostgreSQL برای Production

Write-Host "🚀 شروع نصب PostgreSQL برای Production..." -ForegroundColor Green

# تنظیمات
$POSTGRES_VERSION = "15"
$DB_NAME = "food_ordering_prod"
$DB_USER = "food_ordering_user"
$DB_PASSWORD = "FoodOrder2024!Secure"
$POSTGRES_PORT = "5432"

# بررسی وجود PostgreSQL
Write-Host "📋 بررسی وجود PostgreSQL..." -ForegroundColor Blue

$postgresqlExists = Get-Command psql -ErrorAction SilentlyContinue
if ($postgresqlExists) {
    Write-Host "✅ PostgreSQL قبلاً نصب شده است" -ForegroundColor Green
} else {
    Write-Host "❌ PostgreSQL نصب نشده است. لطفاً آن را نصب کنید:" -ForegroundColor Red
    Write-Host "   1. از https://www.postgresql.org/download/windows/ دانلود کنید" -ForegroundColor Yellow
    Write-Host "   2. یا از Chocolatey استفاده کنید: choco install postgresql" -ForegroundColor Yellow
    Write-Host "   3. یا از Scoop استفاده کنید: scoop install postgresql" -ForegroundColor Yellow
    exit 1
}

# ایجاد دیتابیس و کاربر
Write-Host "🗄️ ایجاد دیتابیس و کاربر..." -ForegroundColor Blue

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
    Write-Host "📝 اجرای script ایجاد دیتابیس..." -ForegroundColor Blue
    psql -U postgres -f $tempScriptPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ دیتابیس با موفقیت ایجاد شد!" -ForegroundColor Green
    } else {
        Write-Host "❌ خطا در ایجاد دیتابیس" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ خطا در اتصال به PostgreSQL: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "💡 اطمینان حاصل کنید که:" -ForegroundColor Yellow
    Write-Host "   - PostgreSQL service در حال اجراست" -ForegroundColor Yellow
    Write-Host "   - کاربر postgres با password صحیح تنظیم شده" -ForegroundColor Yellow
    exit 1
} finally {
    # پاک کردن فایل موقت
    Remove-Item $tempScriptPath -ErrorAction SilentlyContinue
}

# تست اتصال
Write-Host "🔍 تست اتصال به دیتابیس..." -ForegroundColor Blue

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
    psql -h localhost -p $POSTGRES_PORT -U $DB_USER -d $DB_NAME -f $testScriptPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ اتصال به دیتابیس موفق بود!" -ForegroundColor Green
    } else {
        Write-Host "❌ خطا در تست اتصال" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ خطا در تست اتصال: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Remove-Item $testScriptPath -ErrorAction SilentlyContinue
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

# ایجاد فایل تنظیمات
Write-Host "⚙️ ایجاد فایل تنظیمات..." -ForegroundColor Blue

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

Write-Host "📄 فایل تنظیمات در محل زیر ذخیره شد: $configPath" -ForegroundColor Green

# راهنمای استفاده
Write-Host "`n🎉 PostgreSQL با موفقیت راه‌اندازی شد!" -ForegroundColor Green
Write-Host "`n📋 اطلاعات اتصال:" -ForegroundColor Blue
Write-Host "   Database: $DB_NAME" -ForegroundColor White
Write-Host "   User: $DB_USER" -ForegroundColor White
Write-Host "   Password: $DB_PASSWORD" -ForegroundColor White
Write-Host "   Host: localhost" -ForegroundColor White
Write-Host "   Port: $POSTGRES_PORT" -ForegroundColor White

Write-Host "`n📝 مراحل بعدی:" -ForegroundColor Blue
Write-Host "   1. اجرای migration script: .\scripts\migrate-to-postgresql.ps1" -ForegroundColor Yellow
Write-Host "   2. Build کردن project: mvn clean compile" -ForegroundColor Yellow
Write-Host "   3. اجرای application: mvn exec:java" -ForegroundColor Yellow

Write-Host "`n🔗 برای اتصال دستی:" -ForegroundColor Blue
Write-Host "   psql -h localhost -p $POSTGRES_PORT -U $DB_USER -d $DB_NAME" -ForegroundColor White

Write-Host "`n✅ آماده برای پشتیبانی از میلیون‌ها کاربر همزمان!" -ForegroundColor Green 
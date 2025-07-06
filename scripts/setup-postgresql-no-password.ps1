# Setup PostgreSQL for Food Ordering Application (No Password Required)
# اسکریپت نصب و راه‌اندازی PostgreSQL بدون نیاز به پسورد

Write-Host "Starting PostgreSQL setup for Production (No Password)..." -ForegroundColor Green

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
    Write-Host "   2. Install with default settings" -ForegroundColor Yellow
    exit 1
}

# بررسی وضعیت سرویس PostgreSQL
Write-Host "Checking PostgreSQL service status..." -ForegroundColor Blue
$service = Get-Service -Name "postgresql-x64-17" -ErrorAction SilentlyContinue

if ($service -and $service.Status -eq "Running") {
    Write-Host "PostgreSQL service is running" -ForegroundColor Green
} else {
    Write-Host "PostgreSQL service is not running. Starting it..." -ForegroundColor Yellow
    try {
        Start-Service -Name "postgresql-x64-17"
        Start-Sleep -Seconds 5
        Write-Host "PostgreSQL service started successfully" -ForegroundColor Green
    } catch {
        Write-Host "Failed to start PostgreSQL service: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Creating database and user..." -ForegroundColor Blue

# اسکریپت ساخت دیتابیس و کاربر
$createDbScript = @"
-- ایجاد کاربر جدید
DROP USER IF EXISTS $DB_USER;
CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';

-- ایجاد دیتابیس
DROP DATABASE IF EXISTS $DB_NAME;
CREATE DATABASE $DB_NAME OWNER $DB_USER;

-- اعطای مجوزها
GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;
GRANT CONNECT ON DATABASE $DB_NAME TO $DB_USER;

-- اتصال به دیتابیس و اعطای مجوزهای schema
\c $DB_NAME;
GRANT ALL ON SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $DB_USER;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $DB_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO $DB_USER;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO $DB_USER;
"@

# ذخیره اسکریپت در فایل موقت
$tempScript = "temp_create_db.sql"
$createDbScript | Out-File -FilePath $tempScript -Encoding UTF8

Write-Host "Executing database creation script..." -ForegroundColor Blue

try {
    # تنظیم متغیر محیطی برای پسورد
    $env:PGPASSWORD = "postgres"
    
    # اجرای اسکریپت
    $result = & $PSQL_PATH -U postgres -h localhost -f $tempScript
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database and user created successfully!" -ForegroundColor Green
        Write-Host "Database: $DB_NAME" -ForegroundColor White
        Write-Host "User: $DB_USER" -ForegroundColor White
        Write-Host "Password: $DB_PASSWORD" -ForegroundColor White
    } else {
        Write-Host "Error creating database" -ForegroundColor Red
        Write-Host "Result: $result" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # پاک کردن متغیر محیطی
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
    
    # حذف فایل موقت
    if (Test-Path $tempScript) {
        Remove-Item $tempScript -Force
    }
}

Write-Host "PostgreSQL setup completed." -ForegroundColor Green 
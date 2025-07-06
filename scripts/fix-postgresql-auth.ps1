# Fix PostgreSQL Authentication (Administrative Script)
# اسکریپت حل مشکل احراز هویت PostgreSQL

Write-Host "PostgreSQL Authentication Fix Script" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

$PG_DATA_DIR = "C:\Program Files\PostgreSQL\17\data"
$PG_HBA_CONF = "$PG_DATA_DIR\pg_hba.conf"
$PG_HBA_BACKUP = "$PG_DATA_DIR\pg_hba.conf.backup"

# بررسی وجود فایل‌ها
if (-not (Test-Path $PG_HBA_CONF)) {
    Write-Host "pg_hba.conf not found at: $PG_HBA_CONF" -ForegroundColor Red
    exit 1
}

Write-Host "Found pg_hba.conf at: $PG_HBA_CONF" -ForegroundColor Green

# ایجاد backup
Write-Host "Creating backup of pg_hba.conf..." -ForegroundColor Blue
try {
    Copy-Item $PG_HBA_CONF $PG_HBA_BACKUP -Force
    Write-Host "Backup created: $PG_HBA_BACKUP" -ForegroundColor Green
} catch {
    Write-Host "Failed to create backup: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# خواندن محتوای فعلی
Write-Host "Reading current pg_hba.conf..." -ForegroundColor Blue
$currentContent = Get-Content $PG_HBA_CONF

# ایجاد محتوای جدید
Write-Host "Creating new authentication configuration..." -ForegroundColor Blue

$newContent = @"
# PostgreSQL Client Authentication Configuration File
# ===================================================
#
# Refer to the "Client Authentication" section in the PostgreSQL
# documentation for a complete description of this file.

# TYPE  DATABASE        USER            ADDRESS                 METHOD

# "local" is for Unix domain socket connections only
local   all             all                                     trust

# IPv4 local connections:
host    all             all             127.0.0.1/32            trust

# IPv6 local connections:
host    all             all             ::1/128                 trust

# Allow replication connections from localhost, by a user with the
# replication privilege.
local   replication     all                                     trust
host    replication     all             127.0.0.1/32            trust
host    replication     all             ::1/128                 trust
"@

# نوشتن محتوای جدید
Write-Host "Writing new configuration..." -ForegroundColor Blue
try {
    $newContent | Out-File -FilePath $PG_HBA_CONF -Encoding UTF8 -Force
    Write-Host "New configuration written successfully!" -ForegroundColor Green
} catch {
    Write-Host "Failed to write new configuration: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Restart سرویس PostgreSQL
Write-Host "Restarting PostgreSQL service..." -ForegroundColor Blue
try {
    Restart-Service -Name "postgresql-x64-17" -Force
    Start-Sleep -Seconds 10
    Write-Host "PostgreSQL service restarted successfully!" -ForegroundColor Green
} catch {
    Write-Host "Failed to restart PostgreSQL service: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# تست اتصال
Write-Host "Testing connection without password..." -ForegroundColor Blue
$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"

try {
    $testResult = & $PSQL_PATH -U postgres -h localhost -c "SELECT 'Authentication fixed successfully!' as status;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "SUCCESS! Connection works without password!" -ForegroundColor Green
        Write-Host "Result: $testResult" -ForegroundColor White
        
        # حالا می‌توانیم کاربر و دیتابیس را بسازیم
        Write-Host "Creating database and user..." -ForegroundColor Blue
        
        $createScript = @"
-- ایجاد کاربر جدید
DROP USER IF EXISTS food-ordering-project;
CREATE USER "food-ordering-project" WITH PASSWORD 'food-ordering-project';

-- ایجاد دیتابیس
DROP DATABASE IF EXISTS food_ordering_prod;
CREATE DATABASE food_ordering_prod OWNER "food-ordering-project";

-- اعطای مجوزها
GRANT ALL PRIVILEGES ON DATABASE food_ordering_prod TO "food-ordering-project";
GRANT CONNECT ON DATABASE food_ordering_prod TO "food-ordering-project";

-- اتصال به دیتابیس و اعطای مجوزهای schema
\c food_ordering_prod;
GRANT ALL ON SCHEMA public TO "food-ordering-project";
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "food-ordering-project";
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "food-ordering-project";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO "food-ordering-project";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO "food-ordering-project";
"@
        
        $tempFile = "temp_create_db.sql"
        $createScript | Out-File -FilePath $tempFile -Encoding UTF8
        
        $createResult = & $PSQL_PATH -U postgres -h localhost -f $tempFile 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Database and user created successfully!" -ForegroundColor Green
            Write-Host "Database: food_ordering_prod" -ForegroundColor White
            Write-Host "User: food-ordering-project" -ForegroundColor White
            Write-Host "Password: food-ordering-project" -ForegroundColor White
        } else {
            Write-Host "Error creating database: $createResult" -ForegroundColor Red
        }
        
        # حذف فایل موقت
        if (Test-Path $tempFile) {
            Remove-Item $tempFile -Force
        }
        
    } else {
        Write-Host "Connection still failed: $testResult" -ForegroundColor Red
    }
} catch {
    Write-Host "Error during connection test: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Authentication fix process completed." -ForegroundColor Green
Write-Host "Backup of original configuration: $PG_HBA_BACKUP" -ForegroundColor Yellow 
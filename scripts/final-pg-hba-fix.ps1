# Final PostgreSQL pg_hba.conf Fix
# حل نهایی مشکل pg_hba.conf

Write-Host "Final PostgreSQL Authentication Fix" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

$PG_HBA_CONF = "C:\Program Files\PostgreSQL\17\data\pg_hba.conf"

# محتوای صحیح و کامل pg_hba.conf
$correctContent = @"
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

# نوشتن محتوای صحیح
Write-Host "Writing correct pg_hba.conf content..." -ForegroundColor Blue
try {
    $correctContent | Out-File -FilePath $PG_HBA_CONF -Encoding ASCII -Force
    Write-Host "pg_hba.conf written successfully!" -ForegroundColor Green
} catch {
    Write-Host "Failed to write pg_hba.conf: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# بررسی محتوای نوشته شده
Write-Host "Verifying pg_hba.conf content..." -ForegroundColor Blue
$verifyContent = Get-Content $PG_HBA_CONF
Write-Host "File contains $($verifyContent.Count) lines" -ForegroundColor White

# Restart سرویس PostgreSQL
Write-Host "Restarting PostgreSQL service..." -ForegroundColor Blue
try {
    # Stop سرویس
    Write-Host "Stopping PostgreSQL service..." -ForegroundColor Yellow
    Stop-Service -Name "postgresql-x64-17" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 5
    
    # Start سرویس
    Write-Host "Starting PostgreSQL service..." -ForegroundColor Yellow
    Start-Service -Name "postgresql-x64-17"
    Start-Sleep -Seconds 10
    
    Write-Host "PostgreSQL service restarted successfully!" -ForegroundColor Green
} catch {
    Write-Host "Service restart failed: $($_.Exception.Message)" -ForegroundColor Red
}

# بررسی وضعیت سرویس
$service = Get-Service -Name "postgresql-x64-17" -ErrorAction SilentlyContinue
if ($service) {
    Write-Host "Service Status: $($service.Status)" -ForegroundColor Green
} else {
    Write-Host "Service not found!" -ForegroundColor Red
}

# تست اتصال
Write-Host "Testing PostgreSQL connection..." -ForegroundColor Blue
$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"

try {
    $testResult = & $PSQL_PATH -U postgres -h localhost -c "SELECT 'PostgreSQL connection successful!' as status;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "SUCCESS! PostgreSQL connection works!" -ForegroundColor Green
        Write-Host "Result: $testResult" -ForegroundColor White
        
        # حالا می‌توانیم دیتابیس و کاربر را بسازیم
        Write-Host "Creating database and user..." -ForegroundColor Blue
        
        $createScript = @"
-- ایجاد کاربر جدید
DROP USER IF EXISTS "food-ordering-project";
CREATE USER "food-ordering-project" WITH PASSWORD 'food-ordering-project';

-- ایجاد دیتابیس
DROP DATABASE IF EXISTS food_ordering_prod;
CREATE DATABASE food_ordering_prod OWNER "food-ordering-project";

-- اعطای مجوزها
GRANT ALL PRIVILEGES ON DATABASE food_ordering_prod TO "food-ordering-project";
GRANT CONNECT ON DATABASE food_ordering_prod TO "food-ordering-project";
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
        Write-Host "Connection failed: $testResult" -ForegroundColor Red
    }
} catch {
    Write-Host "Error during connection test: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Final PostgreSQL fix completed." -ForegroundColor Green 
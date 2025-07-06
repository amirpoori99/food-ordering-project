# Reset PostgreSQL Password (Administrative Script)
# اسکریپت ریست پسورد کاربر postgres

Write-Host "PostgreSQL Password Reset Script" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"
$NEW_PASSWORD = "postgres123"

# بررسی وجود PostgreSQL
if (-not (Test-Path $PSQL_PATH)) {
    Write-Host "PostgreSQL not found at: $PSQL_PATH" -ForegroundColor Red
    exit 1
}

Write-Host "PostgreSQL found at: $PSQL_PATH" -ForegroundColor Green

# بررسی وضعیت سرویس
$service = Get-Service -Name "postgresql-x64-17" -ErrorAction SilentlyContinue
if (-not $service) {
    Write-Host "PostgreSQL service not found!" -ForegroundColor Red
    exit 1
}

if ($service.Status -ne "Running") {
    Write-Host "Starting PostgreSQL service..." -ForegroundColor Yellow
    Start-Service -Name "postgresql-x64-17"
    Start-Sleep -Seconds 5
}

Write-Host "PostgreSQL service status: $($service.Status)" -ForegroundColor Green

# روش 1: تلاش برای اتصال با پسورد پیش‌فرض
Write-Host "Attempting to connect with default password..." -ForegroundColor Blue

$commonPasswords = @("postgres", "admin", "password", "123456", "")

foreach ($pwd in $commonPasswords) {
    Write-Host "Trying password: '$pwd'" -ForegroundColor Yellow
    
    try {
        if ($pwd -eq "") {
            $result = & $PSQL_PATH -U postgres -h localhost -c "SELECT 'Connection successful' as status;" 2>&1
        } else {
            $env:PGPASSWORD = $pwd
            $result = & $PSQL_PATH -U postgres -h localhost -c "SELECT 'Connection successful' as status;" 2>&1
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "SUCCESS! Connected with password: '$pwd'" -ForegroundColor Green
            $WORKING_PASSWORD = $pwd
            break
        }
    } catch {
        Write-Host "Failed with password: '$pwd'" -ForegroundColor Red
    } finally {
        Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
    }
}

if (-not $WORKING_PASSWORD) {
    Write-Host "Could not connect with any common password." -ForegroundColor Red
    Write-Host "Please provide the current postgres password manually:" -ForegroundColor Yellow
    Write-Host "1. Open pgAdmin or psql" -ForegroundColor White
    Write-Host "2. Connect as postgres user" -ForegroundColor White
    Write-Host "3. Run: ALTER USER postgres PASSWORD '$NEW_PASSWORD';" -ForegroundColor White
    exit 1
}

# ریست پسورد
Write-Host "Resetting postgres password to: $NEW_PASSWORD" -ForegroundColor Blue

try {
    if ($WORKING_PASSWORD -eq "") {
        $resetResult = & $PSQL_PATH -U postgres -h localhost -c "ALTER USER postgres PASSWORD '$NEW_PASSWORD';" 2>&1
    } else {
        $env:PGPASSWORD = $WORKING_PASSWORD
        $resetResult = & $PSQL_PATH -U postgres -h localhost -c "ALTER USER postgres PASSWORD '$NEW_PASSWORD';" 2>&1
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Password reset successful!" -ForegroundColor Green
        Write-Host "New postgres password: $NEW_PASSWORD" -ForegroundColor Green
        
        # تست اتصال با پسورد جدید
        $env:PGPASSWORD = $NEW_PASSWORD
        $testResult = & $PSQL_PATH -U postgres -h localhost -c "SELECT 'Password reset verified' as status;" 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Password reset verified successfully!" -ForegroundColor Green
        } else {
            Write-Host "Warning: Could not verify password reset" -ForegroundColor Yellow
        }
    } else {
        Write-Host "Failed to reset password: $resetResult" -ForegroundColor Red
    }
} catch {
    Write-Host "Error during password reset: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host "Password reset process completed." -ForegroundColor Green 
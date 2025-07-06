# Complete PostgreSQL pg_hba.conf Fix
# تکمیل فایل pg_hba.conf

Write-Host "Completing pg_hba.conf configuration..." -ForegroundColor Green

$PG_HBA_CONF = "C:\Program Files\PostgreSQL\17\data\pg_hba.conf"

# محتوای کامل و صحیح pg_hba.conf
$completeContent = @"
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

# نوشتن محتوای کامل
Write-Host "Writing complete pg_hba.conf..." -ForegroundColor Blue
try {
    $completeContent | Out-File -FilePath $PG_HBA_CONF -Encoding UTF8 -Force
    Write-Host "pg_hba.conf completed successfully!" -ForegroundColor Green
} catch {
    Write-Host "Failed to write pg_hba.conf: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Restart سرویس
Write-Host "Restarting PostgreSQL service..." -ForegroundColor Blue
try {
    Stop-Service -Name "postgresql-x64-17" -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 5
    Start-Service -Name "postgresql-x64-17"
    Start-Sleep -Seconds 10
    Write-Host "PostgreSQL service restarted!" -ForegroundColor Green
} catch {
    Write-Host "Service restart failed: $($_.Exception.Message)" -ForegroundColor Red
}

# تست اتصال
Write-Host "Testing connection..." -ForegroundColor Blue
$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"

try {
    $testResult = & $PSQL_PATH -U postgres -h localhost -c "SELECT 'Connection successful!' as status;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "SUCCESS! PostgreSQL connection works!" -ForegroundColor Green
        Write-Host "Result: $testResult" -ForegroundColor White
    } else {
        Write-Host "Connection failed: $testResult" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "pg_hba.conf fix completed." -ForegroundColor Green 
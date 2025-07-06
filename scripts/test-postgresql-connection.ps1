# Test PostgreSQL Connection
# اسکریپت تست اتصال PostgreSQL

Write-Host "Testing PostgreSQL connection..." -ForegroundColor Blue

$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"

# تست 1: بررسی وضعیت سرویس
Write-Host "1. Checking PostgreSQL service..." -ForegroundColor Yellow
$service = Get-Service -Name "postgresql-x64-17" -ErrorAction SilentlyContinue
if ($service) {
    Write-Host "   Service Status: $($service.Status)" -ForegroundColor Green
} else {
    Write-Host "   Service not found!" -ForegroundColor Red
}

# تست 2: بررسی پورت
Write-Host "2. Checking port 5432..." -ForegroundColor Yellow
$portCheck = netstat -an | findstr :5432
if ($portCheck) {
    Write-Host "   Port 5432 is listening" -ForegroundColor Green
    Write-Host "   $portCheck" -ForegroundColor White
} else {
    Write-Host "   Port 5432 is not listening!" -ForegroundColor Red
}

# تست 3: تست اتصال ساده
Write-Host "3. Testing simple connection..." -ForegroundColor Yellow
try {
    $env:PGPASSWORD = "postgres"
    $result = & $PSQL_PATH -U postgres -h localhost -c "SELECT version();" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   Connection successful!" -ForegroundColor Green
        Write-Host "   Result: $result" -ForegroundColor White
    } else {
        Write-Host "   Connection failed" -ForegroundColor Red
        Write-Host "   Error: $result" -ForegroundColor Red
    }
} catch {
    Write-Host "   Exception: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

# تست 4: بررسی فایل pg_hba.conf
Write-Host "4. Checking pg_hba.conf..." -ForegroundColor Yellow
$pgHbaPath = "C:\Program Files\PostgreSQL\17\data\pg_hba.conf"
if (Test-Path $pgHbaPath) {
    Write-Host "   File exists" -ForegroundColor Green
    $localLines = Get-Content $pgHbaPath | Where-Object { $_ -match "local.*all.*all" }
    if ($localLines) {
        Write-Host "   Local authentication lines:" -ForegroundColor White
        $localLines | ForEach-Object { Write-Host "     $_" -ForegroundColor White }
    }
} else {
    Write-Host "   File not found!" -ForegroundColor Red
}

Write-Host "Connection test completed." -ForegroundColor Blue 
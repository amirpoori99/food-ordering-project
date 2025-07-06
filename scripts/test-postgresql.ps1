# Test PostgreSQL Connection
# اسکریپت تست اتصال PostgreSQL

Write-Host "Testing PostgreSQL connection..." -ForegroundColor Blue

$PSQL_PATH = "C:\Program Files\PostgreSQL\17\bin\psql.exe"

# تست اتصال ساده
try {
    $env:PGPASSWORD = "postgres"  # رمز عبور پیش‌فرض
    $result = & $PSQL_PATH -U postgres -h localhost -c "SELECT 'Connection OK' as status;"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "PostgreSQL connection successful!" -ForegroundColor Green
        Write-Host "Result: $result" -ForegroundColor White
    } else {
        Write-Host "Connection failed" -ForegroundColor Red
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host "Test completed." -ForegroundColor Blue 
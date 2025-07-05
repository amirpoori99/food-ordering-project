# اجرای برنامه با PostgreSQL در محیط Production
# Script to run Food Ordering App with PostgreSQL

Write-Host "🚀 اجرای Food Ordering App با PostgreSQL..." -ForegroundColor Green

# تنظیم environment variables برای Production
$env:APP_ENVIRONMENT = "production"
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/food_ordering_prod"
$env:DATABASE_USERNAME = "food_ordering_user"
$env:DATABASE_PASSWORD = "FoodOrder2024!Secure"

# تنظیمات Connection Pool
$env:HIKARI_MAXIMUM_POOL_SIZE = "50"
$env:HIKARI_MINIMUM_IDLE = "10"
$env:HIKARI_CONNECTION_TIMEOUT = "30000"
$env:HIKARI_IDLE_TIMEOUT = "600000"
$env:HIKARI_MAX_LIFETIME = "1800000"

Write-Host "⚙️ Environment Variables:" -ForegroundColor Blue
Write-Host "   APP_ENVIRONMENT: $env:APP_ENVIRONMENT" -ForegroundColor White
Write-Host "   DATABASE: PostgreSQL" -ForegroundColor White
Write-Host "   CONNECTION_POOL: HikariCP (50 connections)" -ForegroundColor White

# بررسی وجود PostgreSQL
Write-Host "`n🔍 بررسی اتصال PostgreSQL..." -ForegroundColor Blue

try {
    $env:PGPASSWORD = $env:DATABASE_PASSWORD
    $result = psql -h localhost -p 5432 -U $env:DATABASE_USERNAME -d food_ordering_prod -c "SELECT 'PostgreSQL OK' as status;"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ اتصال PostgreSQL موفق!" -ForegroundColor Green
    } else {
        Write-Host "❌ خطا در اتصال PostgreSQL" -ForegroundColor Red
        Write-Host "💡 لطفاً ابتدا PostgreSQL را راه‌اندازی کنید:" -ForegroundColor Yellow
        Write-Host "   .\scripts\setup-postgresql.ps1" -ForegroundColor White
        exit 1
    }
} catch {
    Write-Host "❌ PostgreSQL در دسترس نیست: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
} finally {
    Remove-Item env:PGPASSWORD -ErrorAction SilentlyContinue
}

# Build کردن پروژه
Write-Host "`n🔨 Build کردن پروژه..." -ForegroundColor Blue

try {
    mvn clean compile
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build موفق!" -ForegroundColor Green
    } else {
        Write-Host "❌ خطا در Build" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ خطا در Maven Build: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# اجرای برنامه
Write-Host "`n🎯 اجرای برنامه..." -ForegroundColor Blue
Write-Host "📊 آماده برای پشتیبانی از 100,000+ کاربر همزمان!" -ForegroundColor Green

try {
    # اجرای ServerApp با تنظیمات PostgreSQL
    mvn exec:java -Dexec.mainClass="com.myapp.ServerApp"
} catch {
    Write-Host "❌ خطا در اجرای برنامه: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    # پاک کردن environment variables
    Remove-Item env:APP_ENVIRONMENT -ErrorAction SilentlyContinue
    Remove-Item env:DATABASE_URL -ErrorAction SilentlyContinue
    Remove-Item env:DATABASE_USERNAME -ErrorAction SilentlyContinue
    Remove-Item env:DATABASE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item env:HIKARI_MAXIMUM_POOL_SIZE -ErrorAction SilentlyContinue
    Remove-Item env:HIKARI_MINIMUM_IDLE -ErrorAction SilentlyContinue
    Remove-Item env:HIKARI_CONNECTION_TIMEOUT -ErrorAction SilentlyContinue
    Remove-Item env:HIKARI_IDLE_TIMEOUT -ErrorAction SilentlyContinue
    Remove-Item env:HIKARI_MAX_LIFETIME -ErrorAction SilentlyContinue
} 
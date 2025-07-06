# ================================================================
# اسکریپت اجرای تست‌های فرانت‌اند JavaFX
# این اسکریپت امکان اجرای انواع مختلف تست‌ها را فراهم می‌کند
# ================================================================

param(
    [ValidateSet("unit", "integration", "performance", "all", "clean")]
    [string]$TestType = "unit"
)

Write-Host "TestType: $TestType"

if ($TestType -eq "unit") {
    Write-Host "Running unit tests..."
    mvn test -Punit-tests
} elseif ($TestType -eq "integration") {
    Write-Host "Running integration tests..."
    mvn test -Pintegration-tests
} elseif ($TestType -eq "performance") {
    Write-Host "Running performance tests..."
    mvn test -Pperformance-tests
} elseif ($TestType -eq "all") {
    Write-Host "Running all tests..."
    mvn test -Pall-tests
} elseif ($TestType -eq "clean") {
    Write-Host "Cleaning project..."
    mvn clean
} 
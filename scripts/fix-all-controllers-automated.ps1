# Automated Controller Fix Script
# اصلاح خودکار تمام کنترلرها

Write-Host "Automated Controller Fix" -ForegroundColor Green
Write-Host "=======================" -ForegroundColor Green

# List of controllers to fix
$controllers = @(
    @{Name="DeliveryController"; Path="backend/src/main/java/com/myapp/courier/DeliveryController.java"; Resource="deliveries"},
    @{Name="ItemController"; Path="backend/src/main/java/com/myapp/item/ItemController.java"; Resource="items"},
    @{Name="MenuController"; Path="backend/src/main/java/com/myapp/menu/MenuController.java"; Resource="menu"},
    @{Name="VendorController"; Path="backend/src/main/java/com/myapp/vendor/VendorController.java"; Resource="vendors"},
    @{Name="FavoritesController"; Path="backend/src/main/java/com/myapp/favorites/FavoritesController.java"; Resource="favorites"},
    @{Name="NotificationController"; Path="backend/src/main/java/com/myapp/notification/NotificationController.java"; Resource="notifications"},
    @{Name="AnalyticsController"; Path="backend/src/main/java/com/myapp/analytics/AnalyticsController.java"; Resource="analytics"}
)

foreach ($controller in $controllers) {
    Write-Host "Processing $($controller.Name)..." -ForegroundColor Cyan
    
    if (Test-Path $controller.Path) {
        Write-Host "  ✅ File exists" -ForegroundColor Green
        
        # Read file content
        $content = Get-Content $controller.Path -Raw
        
        # Check if GET endpoint already exists
        if ($content -match "path\.equals\(\"/api/$($controller.Resource)\"\)") {
            Write-Host "  ✅ GET endpoint already exists" -ForegroundColor Green
        } else {
            Write-Host "  ❌ GET endpoint missing - needs manual fix" -ForegroundColor Red
        }
    } else {
        Write-Host "  ❌ File not found: $($controller.Path)" -ForegroundColor Red
    }
}

Write-Host "`nManual fixes needed for controllers without GET endpoints." -ForegroundColor Yellow
Write-Host "Please add the following pattern to each controller:" -ForegroundColor Yellow
Write-Host "1. In handleGetRequest method: if (path.equals(\"/api/{resource}\")) { getAll{Resource}(exchange); }" -ForegroundColor White
Write-Host "2. Add getAll{Resource} method that calls service.getAll{Resource}()" -ForegroundColor White
Write-Host "3. Add getAll{Resource} method to Service class" -ForegroundColor White
Write-Host "4. Add getAll{Resource} method to Repository class" -ForegroundColor White

Write-Host "`nController fix analysis completed!" -ForegroundColor Green 
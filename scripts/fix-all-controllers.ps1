# Fix All Controllers Script
# اصلاح خودکار تمام کنترلرها

Write-Host "Fixing All Controllers" -ForegroundColor Green
Write-Host "=====================" -ForegroundColor Green

# List of controllers that need GET /api/{resource} endpoint
$controllers = @(
    @{Name="OrderController"; Path="backend/src/main/java/com/myapp/order/OrderController.java"; Service="OrderService"; Repository="OrderRepository"; Model="Order"},
    @{Name="PaymentController"; Path="backend/src/main/java/com/myapp/payment/PaymentController.java"; Service="PaymentService"; Repository="PaymentRepository"; Model="Transaction"},
    @{Name="WalletController"; Path="backend/src/main/java/com/myapp/payment/WalletController.java"; Service="WalletService"; Repository="PaymentRepository"; Model="Wallet"},
    @{Name="TransactionController"; Path="backend/src/main/java/com/myapp/payment/TransactionController.java"; Service="TransactionService"; Repository="PaymentRepository"; Model="Transaction"},
    @{Name="DeliveryController"; Path="backend/src/main/java/com/myapp/courier/DeliveryController.java"; Service="DeliveryService"; Repository="DeliveryRepository"; Model="Delivery"},
    @{Name="ItemController"; Path="backend/src/main/java/com/myapp/item/ItemController.java"; Service="ItemService"; Repository="ItemRepository"; Model="Item"},
    @{Name="MenuController"; Path="backend/src/main/java/com/myapp/menu/MenuController.java"; Service="MenuService"; Repository="MenuRepository"; Model="Menu"},
    @{Name="VendorController"; Path="backend/src/main/java/com/myapp/vendor/VendorController.java"; Service="VendorService"; Repository="VendorRepository"; Model="Vendor"},
    @{Name="FavoritesController"; Path="backend/src/main/java/com/myapp/favorites/FavoritesController.java"; Service="FavoritesService"; Repository="FavoritesRepository"; Model="Favorite"},
    @{Name="NotificationController"; Path="backend/src/main/java/com/myapp/notification/NotificationController.java"; Service="NotificationService"; Repository="NotificationRepository"; Model="Notification"},
    @{Name="AnalyticsController"; Path="backend/src/main/java/com/myapp/analytics/AnalyticsController.java"; Service="AnalyticsService"; Repository="AnalyticsRepository"; Model="Analytics"}
)

foreach ($controller in $controllers) {
    Write-Host "Processing $($controller.Name)..." -ForegroundColor Cyan
    
    # Check if controller file exists
    if (Test-Path $controller.Path) {
        Write-Host "  ✅ File exists" -ForegroundColor Green
        
        # Check if GET /api/{resource} endpoint exists
        $content = Get-Content $controller.Path -Raw
        $resourceName = $controller.Name.ToLower().Replace('Controller','')
        $pattern = "path.equals(`"/api/$resourceName`")"
        
        if ($content -match $pattern) {
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
Write-Host "1. In handleGetRequest method: if (path.equals(`"/api/{resource}`")) { getAll{Resource}(exchange); }" -ForegroundColor White
Write-Host "2. Add getAll{Resource} method that calls service.getAll{Resource}()" -ForegroundColor White
Write-Host "3. Add getAll{Resource} method to Service class" -ForegroundColor White
Write-Host "4. Add getAll{Resource} method to Repository class" -ForegroundColor White

Write-Host "`nController fix analysis completed!" -ForegroundColor Green 
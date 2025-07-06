# Comprehensive API Testing Script
# تست جامع تمام API endpoints

Write-Host "Comprehensive API Testing" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green

$baseUrl = "http://localhost:8081"
$testResults = @()

# Function to test API endpoint
function Test-ApiEndpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Description,
        [string]$Body = $null,
        [hashtable]$Headers = @{}
    )
    
    $url = "$baseUrl$Endpoint"
    $result = @{
        Method = $Method
        Endpoint = $Endpoint
        Description = $Description
        Status = "FAILED"
        Response = $null
        Error = $null
    }
    
    try {
        $params = @{
            Uri = $url
            Method = $Method
            ContentType = "application/json"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        if ($Headers.Count -gt 0) {
            $params.Headers = $Headers
        }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        $result.Status = "SUCCESS"
        $result.Response = $response
        Write-Host "✅ $Method $Endpoint - $Description" -ForegroundColor Green
    }
    catch {
        $result.Status = "FAILED"
        $result.Error = $_.Exception.Message
        Write-Host "❌ $Method $Endpoint - $Description" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    
    $testResults += $result
    return $result
}

# 1. Health and Basic Tests
Write-Host "`n1. Health and Basic Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/health" -Description "Health Check"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/test" -Description "API Test"

# 2. Authentication Tests
Write-Host "`n2. Authentication Tests" -ForegroundColor Cyan

# Register a test user
$registerBody = @{
    username = "apitestuser"
    password = "testpass123"
    email = "apitest@example.com"
    fullName = "API Test User"
    phone = "1234567890"
} | ConvertTo-Json

$registerResult = Test-ApiEndpoint -Method "POST" -Endpoint "/api/auth/register" -Description "User Registration" -Body $registerBody

# Login test
$loginBody = @{
    username = "apitestuser"
    password = "testpass123"
    phone = "1234567890"
} | ConvertTo-Json

$loginResult = Test-ApiEndpoint -Method "POST" -Endpoint "/api/auth/login" -Description "User Login" -Body $loginBody

# Extract token if login successful
$authToken = $null
if ($loginResult.Status -eq "SUCCESS" -and $loginResult.Response.accessToken) {
    $authToken = $loginResult.Response.accessToken
    Write-Host "   Token obtained: $($authToken.Substring(0, 20))..." -ForegroundColor Green
}

# 3. Admin API Tests
Write-Host "`n3. Admin API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/dashboard" -Description "Admin Dashboard"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/users" -Description "User Management"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/restaurants" -Description "Restaurant Management"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/orders" -Description "Order Management"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/transactions" -Description "Transaction Monitoring"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/deliveries" -Description "Delivery Tracking"

# 4. Restaurant API Tests
Write-Host "`n4. Restaurant API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/restaurants" -Description "Get All Restaurants"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/restaurants/1" -Description "Get Restaurant by ID"

# 5. Order API Tests
Write-Host "`n5. Order API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/orders/" -Description "Get All Orders"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/orders/1" -Description "Get Order by ID"

# 6. Payment API Tests
Write-Host "`n6. Payment API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/payments/" -Description "Get All Payments"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/wallet/" -Description "Wallet Information"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/transactions/" -Description "Transaction History"

# 7. Delivery API Tests
Write-Host "`n7. Delivery API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/deliveries/" -Description "Get All Deliveries"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/deliveries/1" -Description "Get Delivery by ID"

# 8. Menu and Items API Tests
Write-Host "`n8. Menu and Items API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/items" -Description "Get All Items"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/menu/" -Description "Get Menu"

# 9. Vendor API Tests
Write-Host "`n9. Vendor API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/vendors/" -Description "Get All Vendors"

# 10. Favorites API Tests
Write-Host "`n10. Favorites API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/favorites/" -Description "Get User Favorites"

# 11. Notification API Tests
Write-Host "`n11. Notification API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/notifications/" -Description "Get All Notifications"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/notification/" -Description "Get User Notifications"

# 12. Analytics API Tests
Write-Host "`n12. Analytics API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/analytics/" -Description "Analytics Dashboard"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/analytics/revenue" -Description "Revenue Analytics"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/analytics/orders" -Description "Order Analytics"

# Summary Report
Write-Host "`n" + "="*50 -ForegroundColor Green
Write-Host "API TESTING SUMMARY" -ForegroundColor Green
Write-Host "="*50 -ForegroundColor Green

$successCount = ($testResults | Where-Object { $_.Status -eq "SUCCESS" }).Count
$totalCount = $testResults.Count
$failureCount = $totalCount - $successCount

Write-Host "Total Tests: $totalCount" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $failureCount" -ForegroundColor Red
Write-Host "Success Rate: $([math]::Round(($successCount / $totalCount) * 100, 2))%" -ForegroundColor Cyan

# Detailed Results
Write-Host "`nDetailed Results:" -ForegroundColor Yellow
$testResults | ForEach-Object {
    $statusIcon = if ($_.Status -eq "SUCCESS") { "✅" } else { "❌" }
    Write-Host "$statusIcon $($_.Method) $($_.Endpoint) - $($_.Description)" -ForegroundColor $(if ($_.Status -eq "SUCCESS") { "Green" } else { "Red" })
}

# Save results to file
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resultsFile = "api-test-results-$timestamp.json"
$testResults | ConvertTo-Json -Depth 3 | Out-File -FilePath $resultsFile -Encoding UTF8
Write-Host "`nResults saved to: $resultsFile" -ForegroundColor Cyan

Write-Host "`nAPI Testing completed!" -ForegroundColor Green 
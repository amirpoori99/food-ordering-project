# Complete API Testing Script
# تست جامع تمام API endpoints

Write-Host "Complete API Testing" -ForegroundColor Green
Write-Host "=====================" -ForegroundColor Green

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
        StatusCode = $null
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
        $result.StatusCode = 200
        Write-Host "✅ $Method $Endpoint - $Description" -ForegroundColor Green
    }
    catch {
        $result.Status = "FAILED"
        $result.Error = $_.Exception.Message
        if ($_.Exception.Response) {
            $result.StatusCode = [int]$_.Exception.Response.StatusCode
        }
        Write-Host "❌ $Method $Endpoint - $Description" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
        Write-Host "   Status Code: $($result.StatusCode)" -ForegroundColor Yellow
    }
    
    $testResults += $result
    return $result
}

# 1. Health and Basic Tests
Write-Host "`n1. Health and Basic Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/health" -Description "Health Check"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/test" -Description "API Test"

# 2. Admin API Tests (Working endpoints)
Write-Host "`n2. Admin API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/dashboard" -Description "Admin Dashboard"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/users" -Description "User Management"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/restaurants" -Description "Restaurant Management"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/orders" -Description "Order Management"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/admin/transactions" -Description "Transaction Monitoring"

# 3. Restaurant API Tests
Write-Host "`n3. Restaurant API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/restaurants" -Description "Get All Restaurants"

# 4. Order API Tests
Write-Host "`n4. Order API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/orders" -Description "Get All Orders"

# 5. Payment API Tests
Write-Host "`n5. Payment API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/payments" -Description "Get All Payments"

# 6. Wallet API Tests
Write-Host "`n6. Wallet API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/wallet" -Description "Wallet Information"

# 7. Transaction API Tests
Write-Host "`n7. Transaction API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/transactions" -Description "Transaction History"

# 8. Delivery API Tests
Write-Host "`n8. Delivery API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/deliveries" -Description "Get All Deliveries"

# 9. Menu and Items API Tests
Write-Host "`n9. Menu and Items API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/items" -Description "Get All Items"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/menu" -Description "Get Menu"

# 10. Vendor API Tests
Write-Host "`n10. Vendor API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/vendors" -Description "Get All Vendors"

# 11. Favorites API Tests
Write-Host "`n11. Favorites API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/favorites" -Description "Get User Favorites"

# 12. Notification API Tests
Write-Host "`n12. Notification API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/notifications" -Description "Get All Notifications"
Test-ApiEndpoint -Method "GET" -Endpoint "/api/notification" -Description "Get User Notifications"

# 13. Analytics API Tests
Write-Host "`n13. Analytics API Tests" -ForegroundColor Cyan
Test-ApiEndpoint -Method "GET" -Endpoint "/api/analytics" -Description "Analytics Dashboard"

# Summary Report
Write-Host "`n" + "="*60 -ForegroundColor Green
Write-Host "COMPLETE API TESTING SUMMARY" -ForegroundColor Green
Write-Host "="*60 -ForegroundColor Green

$successCount = ($testResults | Where-Object { $_.Status -eq "SUCCESS" }).Count
$totalCount = $testResults.Count
$failureCount = $totalCount - $successCount

Write-Host "Total Tests: $totalCount" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $failureCount" -ForegroundColor Red

if ($totalCount -gt 0) {
    $successRate = [math]::Round(($successCount / $totalCount) * 100, 2)
    Write-Host "Success Rate: $successRate%" -ForegroundColor Cyan
} else {
    Write-Host "Success Rate: 0%" -ForegroundColor Cyan
}

# Detailed Results by Status Code
Write-Host "`nDetailed Results by Status Code:" -ForegroundColor Yellow
$statusGroups = $testResults | Group-Object StatusCode
$statusGroups | ForEach-Object {
    $statusCode = $_.Name
    $count = $_.Count
    $color = if ($statusCode -eq 200) { "Green" } elseif ($statusCode -eq 404) { "Red" } elseif ($statusCode -eq 401) { "Yellow" } else { "White" }
    Write-Host "Status $statusCode : $count endpoints" -ForegroundColor $color
}

# Failed Endpoints Analysis
$failedTests = $testResults | Where-Object { $_.Status -eq "FAILED" }
if ($failedTests.Count -gt 0) {
    Write-Host "`nFailed Endpoints Analysis:" -ForegroundColor Red
    $failedTests | ForEach-Object {
        Write-Host "❌ $($_.Method) $($_.Endpoint) - Status: $($_.StatusCode)" -ForegroundColor Red
    }
}

# Save results to file
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resultsFile = "complete-api-test-results-$timestamp.json"
$testResults | ConvertTo-Json -Depth 3 | Out-File -FilePath $resultsFile -Encoding UTF8
Write-Host "`nResults saved to: $resultsFile" -ForegroundColor Cyan

Write-Host "`nComplete API Testing finished!" -ForegroundColor Green 
# Quick API Test Script
# تست سریع API endpoints

Write-Host "Quick API Test" -ForegroundColor Green
Write-Host "==============" -ForegroundColor Green

$baseUrl = "http://localhost:8081"
$endpoints = @(
    "/health",
    "/api/test", 
    "/api/admin/dashboard",
    "/api/restaurants",
    "/api/orders",
    "/api/payments",
    "/api/wallet",
    "/api/transactions"
)

foreach ($endpoint in $endpoints) {
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl$endpoint" -Method GET -TimeoutSec 5
        Write-Host "✅ $endpoint - Status: $($response.StatusCode)" -ForegroundColor Green
    }
    catch {
        $statusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { "ERROR" }
        Write-Host "❌ $endpoint - Status: $statusCode" -ForegroundColor Red
    }
}

Write-Host "`nQuick API Test completed!" -ForegroundColor Green 
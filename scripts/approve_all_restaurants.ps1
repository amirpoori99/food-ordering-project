# Script to approve all pending restaurants

Write-Host "Starting to approve all pending restaurants..."

# Login and get token
$loginBody = '{"phone":"09123456789","password":"test123"}'
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $loginBody
$token = ($loginResponse.Content | ConvertFrom-Json).accessToken

if (-not $token) {
    Write-Host "Failed to get JWT token!"
    exit 1
}

Write-Host "Got JWT token successfully."

# Get all pending restaurants
$pendingResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/restaurants/status/PENDING" -Headers @{"Authorization"="Bearer $token"}
$pendingRestaurants = $pendingResponse.Content | ConvertFrom-Json

Write-Host "Found $($pendingRestaurants.Count) pending restaurants."

# Approve each pending restaurant
foreach ($restaurant in $pendingRestaurants) {
    $restaurantId = $restaurant.id
    $restaurantName = $restaurant.name
    
    try {
        $approveResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/restaurants/$restaurantId/approve" -Method POST -Headers @{"Authorization"="Bearer $token"}
        Write-Host "Approved: $restaurantName (ID: $restaurantId)"
    } catch {
        Write-Host "Failed to approve $restaurantName (ID: $restaurantId)"
    }
}

Write-Host "Finished approving restaurants." 
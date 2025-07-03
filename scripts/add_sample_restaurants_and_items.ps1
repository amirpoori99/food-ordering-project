# Script to add 20 restaurants and 1000 menu items to backend server
# First login and get JWT token

$loginBody = '{"phone":"09123456789","password":"test123"}'
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method POST -Headers @{"Content-Type"="application/json"} -Body $loginBody
$token = ($loginResponse.Content | ConvertFrom-Json).accessToken

if (-not $token) {
    Write-Host "JWT token not received!"
    exit 1
}

# List of restaurant types
$types = @("Italian","Iranian","American","Chinese","French","Japanese","Indian","Turkish","Lebanese","Mexican","Vegetarian","Seafood","FastFood","Kebab","Pizza","Fried","European","Traditional","Cafe","Bakery")

for ($i=1; $i -le 20; $i++) {
    $name = "Restaurant $i ($($types[$i-1]))"
    $address = "Test Address $i, Test City"
    $phone = "021-000$i"
    $restaurantBody = @{ name = $name; address = $address; phone = $phone; ownerId = 1 } | ConvertTo-Json -Compress
    $restaurantResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/restaurants" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body $restaurantBody
    $restaurantId = ($restaurantResponse.Content | ConvertFrom-Json).id
    if (-not $restaurantId) {
        Write-Host "Failed to add restaurant $i!"
        continue
    }
    Write-Host "Restaurant $i added. (ID: $restaurantId)"
    # Add 50 menu items for each restaurant
    for ($j=1; $j -le 50; $j++) {
        $itemName = "Food $j of $name"
        $itemBody = @{ name = $itemName; price = (1000 + $j*100); restaurantId = $restaurantId; category = $types[$i-1]; available = $true; quantity = 100; description = "Test description $itemName" } | ConvertTo-Json -Compress
        $itemResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/items" -Method POST -Headers @{"Content-Type"="application/json"; "Authorization"="Bearer $token"} -Body $itemBody
        if ($itemResponse.StatusCode -eq 200 -or $itemResponse.StatusCode -eq 201) {
            Write-Host "  - Item $j added."
        } else {
            Write-Host "  - Failed to add item $j"
        }
    }
} 
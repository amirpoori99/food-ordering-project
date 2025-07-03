# اسکریپت اضافه کردن رستوران‌ها و آیتم‌ها به دیتابیس
# این اسکریپت 20 رستوران و 1000 آیتم اضافه می‌کند

Write-Host "Starting to add restaurants and items..." -ForegroundColor Green

# تنظیمات اولیه
$baseUrl = "http://localhost:8081"
$headers = @{
    "Content-Type" = "application/json"
}

# تابع برای اضافه کردن رستوران
function Add-Restaurant {
    param(
        [string]$name,
        [string]$type,
        [string]$address,
        [string]$phone
    )
    
    $restaurantData = @{
        name = $name
        type = $type
        address = $address
        phone = $phone
        ownerId = 1
    } | ConvertTo-Json
    
    try {
        Write-Host "Sending request for restaurant: $name" -ForegroundColor Yellow
        $response = Invoke-WebRequest -Uri "$baseUrl/api/restaurants" -Method POST -Body $restaurantData -Headers $headers
        Write-Host "Response status: $($response.StatusCode)" -ForegroundColor Cyan
        Write-Host "Response content: $($response.Content)" -ForegroundColor Cyan
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 201) {
            $result = $response.Content | ConvertFrom-Json
            Write-Host "Added restaurant: $name (ID: $($result.id))" -ForegroundColor Green
            return $result.id
        }
    }
    catch {
        Write-Host "Failed to add restaurant $name : $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "Error details: $($_.Exception.Response)" -ForegroundColor Red
        return $null
    }
}

# تابع برای اضافه کردن آیتم
function Add-Item {
    param(
        [int]$restaurantId,
        [string]$name,
        [string]$category,
        [string]$description,
        [double]$price
    )
    
    $itemData = @{
        name = $name
        category = $category
        description = $description
        price = $price
        restaurantId = $restaurantId
        available = $true
        quantity = 100
    } | ConvertTo-Json
    
    try {
        Write-Host "Sending request for item: $name" -ForegroundColor Yellow
        $response = Invoke-WebRequest -Uri "$baseUrl/api/items" -Method POST -Body $itemData -Headers $headers
        Write-Host "Response status: $($response.StatusCode)" -ForegroundColor Cyan
        Write-Host "Response content: $($response.Content)" -ForegroundColor Cyan
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 201) {
            $result = $response.Content | ConvertFrom-Json
            Write-Host "Added item: $name (Price: $price)" -ForegroundColor Green
            return $result.id
        }
    }
    catch {
        Write-Host "Failed to add item $name : $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "Error details: $($_.Exception.Response)" -ForegroundColor Red
        return $null
    }
}

# لیست رستوران‌های ایتالیایی
$italianRestaurants = @(
    @{name="Pizza Palace"; type="Italian"; address="123 Main St"; phone="555-0101"},
    @{name="Pasta Paradise"; type="Italian"; address="456 Oak Ave"; phone="555-0102"},
    @{name="Roma Ristorante"; type="Italian"; address="789 Pine St"; phone="555-0103"},
    @{name="Bella Italia"; type="Italian"; address="321 Elm St"; phone="555-0104"},
    @{name="Mamma Mia"; type="Italian"; address="654 Maple Dr"; phone="555-0105"}
)

# لیست رستوران‌های آمریکایی
$americanRestaurants = @(
    @{name="Burger House"; type="American"; address="111 Center St"; phone="555-0201"},
    @{name="BBQ Grill"; type="American"; address="222 West Ave"; phone="555-0202"},
    @{name="Steak House"; type="American"; address="333 East Blvd"; phone="555-0203"},
    @{name="Diner Deluxe"; type="American"; address="444 North Rd"; phone="555-0204"},
    @{name="Classic Cafe"; type="American"; address="555 South Ln"; phone="555-0205"}
)

# لیست رستوران‌های چینی
$chineseRestaurants = @(
    @{name="Golden Dragon"; type="Chinese"; address="666 Fortune St"; phone="555-0301"},
    @{name="Peking Palace"; type="Chinese"; address="777 Lucky Ave"; phone="555-0302"},
    @{name="Wok & Roll"; type="Chinese"; address="888 Prosperity Rd"; phone="555-0303"},
    @{name="China Town"; type="Chinese"; address="999 Harmony St"; phone="555-0304"},
    @{name="Oriental Express"; type="Chinese"; address="1010 Wisdom Ave"; phone="555-0305"}
)

# لیست رستوران‌های ایرانی
$iranianRestaurants = @(
    @{name="Persian Palace"; type="Iranian"; address="1111 Tehran St"; phone="555-0401"},
    @{name="Shiraz Restaurant"; type="Iranian"; address="2222 Isfahan Ave"; phone="555-0402"},
    @{name="Kebab House"; type="Iranian"; address="3333 Tabriz Rd"; phone="555-0403"},
    @{name="Persian Garden"; type="Iranian"; address="4444 Mashhad St"; phone="555-0404"},
    @{name="Iranian Delight"; type="Iranian"; address="5555 Yazd Ave"; phone="555-0405"}
)

# ترکیب همه رستوران‌ها
$allRestaurants = $italianRestaurants + $americanRestaurants + $chineseRestaurants + $iranianRestaurants

# آیتم‌های ایتالیایی
$italianItems = @(
    @{name="Margherita Pizza"; category="Pizza"; description="Classic tomato and mozzarella"; price=12.99},
    @{name="Pepperoni Pizza"; category="Pizza"; description="Spicy pepperoni with cheese"; price=14.99},
    @{name="Spaghetti Carbonara"; category="Pasta"; description="Creamy pasta with bacon"; price=11.99},
    @{name="Fettuccine Alfredo"; category="Pasta"; description="Creamy Alfredo sauce"; price=10.99},
    @{name="Lasagna"; category="Pasta"; description="Layered pasta with meat sauce"; price=13.99},
    @{name="Chicken Parmesan"; category="Main Course"; description="Breaded chicken with marinara"; price=15.99},
    @{name="Veal Marsala"; category="Main Course"; description="Veal in Marsala wine sauce"; price=18.99},
    @{name="Tiramisu"; category="Dessert"; description="Classic Italian dessert"; price=7.99},
    @{name="Cannoli"; category="Dessert"; description="Sicilian pastry with ricotta"; price=6.99},
    @{name="Garlic Bread"; category="Appetizer"; description="Toasted bread with garlic butter"; price=4.99}
)

# آیتم‌های آمریکایی
$americanItems = @(
    @{name="Classic Burger"; category="Burger"; description="Beef patty with lettuce and tomato"; price=9.99},
    @{name="Cheeseburger"; category="Burger"; description="Burger with melted cheese"; price=10.99},
    @{name="BBQ Ribs"; category="BBQ"; description="Slow-cooked pork ribs"; price=16.99},
    @{name="Pulled Pork Sandwich"; category="Sandwich"; description="Shredded pork with BBQ sauce"; price=11.99},
    @{name="Grilled Chicken"; category="Main Course"; description="Herb-grilled chicken breast"; price=13.99},
    @{name="Steak"; category="Main Course"; description="Grilled beef steak"; price=22.99},
    @{name="French Fries"; category="Side"; description="Crispy golden fries"; price=4.99},
    @{name="Onion Rings"; category="Side"; description="Breaded onion rings"; price=5.99},
    @{name="Apple Pie"; category="Dessert"; description="Classic American pie"; price=6.99},
    @{name="Chocolate Cake"; category="Dessert"; description="Rich chocolate layer cake"; price=7.99}
)

# آیتم‌های چینی
$chineseItems = @(
    @{name="Kung Pao Chicken"; category="Main Course"; description="Spicy chicken with peanuts"; price=12.99},
    @{name="Sweet and Sour Pork"; category="Main Course"; description="Crispy pork in tangy sauce"; price=11.99},
    @{name="Beef and Broccoli"; category="Main Course"; description="Stir-fried beef with vegetables"; price=13.99},
    @{name="Fried Rice"; category="Side"; description="Classic Chinese fried rice"; price=8.99},
    @{name="Chow Mein"; category="Noodles"; description="Stir-fried noodles with vegetables"; price=9.99},
    @{name="Egg Rolls"; category="Appetizer"; description="Crispy vegetable rolls"; price=5.99},
    @{name="Wonton Soup"; category="Soup"; description="Clear soup with dumplings"; price=6.99},
    @{name="General Tso's Chicken"; category="Main Course"; description="Crispy chicken in sweet sauce"; price=12.99},
    @{name="Mapo Tofu"; category="Main Course"; description="Spicy tofu with ground pork"; price=10.99},
    @{name="Fortune Cookie"; category="Dessert"; description="Traditional fortune cookie"; price=1.99}
)

# آیتم‌های ایرانی
$iranianItems = @(
    @{name="Chelo Kebab"; category="Main Course"; description="Grilled meat with rice"; price=14.99},
    @{name="Ghormeh Sabzi"; category="Main Course"; description="Herb stew with meat"; price=13.99},
    @{name="Fesenjan"; category="Main Course"; description="Pomegranate walnut stew"; price=15.99},
    @{name="Tahchin"; category="Rice Dish"; description="Saffron rice with chicken"; price=12.99},
    @{name="Zereshk Polo"; category="Rice Dish"; description="Rice with barberries"; price=11.99},
    @{name="Mirza Ghasemi"; category="Appetizer"; description="Smoked eggplant dip"; price=7.99},
    @{name="Kashk Bademjan"; category="Appetizer"; description="Eggplant with whey"; price=6.99},
    @{name="Shirazi Salad"; category="Salad"; description="Fresh cucumber and tomato"; price=5.99},
    @{name="Baghali Polo"; category="Rice Dish"; description="Rice with fava beans"; price=10.99},
    @{name="Saffron Ice Cream"; category="Dessert"; description="Persian saffron ice cream"; price=6.99}
)

# اضافه کردن رستوران‌ها
Write-Host "Adding restaurants..." -ForegroundColor Yellow
$restaurantIds = @()

foreach ($restaurant in $allRestaurants) {
    $id = Add-Restaurant -name $restaurant.name -type $restaurant.type -address $restaurant.address -phone $restaurant.phone
    if ($id) {
        $restaurantIds += $id
    }
    Start-Sleep -Milliseconds 100
}

Write-Host "Added $($restaurantIds.Count) restaurants" -ForegroundColor Green

# اضافه کردن آیتم‌ها
Write-Host "Adding items..." -ForegroundColor Yellow
$itemCount = 0

foreach ($restaurantId in $restaurantIds) {
    $restaurantIndex = $restaurantIds.IndexOf($restaurantId)
    
    # انتخاب آیتم‌ها بر اساس نوع رستوران
    if ($restaurantIndex -lt 5) {
        # رستوران‌های ایتالیایی
        $items = $italianItems
    } elseif ($restaurantIndex -lt 10) {
        # رستوران‌های آمریکایی
        $items = $americanItems
    } elseif ($restaurantIndex -lt 15) {
        # رستوران‌های چینی
        $items = $chineseItems
    } else {
        # رستوران‌های ایرانی
        $items = $iranianItems
    }
    
    # اضافه کردن 50 آیتم برای هر رستوران
    for ($i = 0; $i -lt 50; $i++) {
        $item = $items[$i % $items.Count]
        $itemName = "$($item.name) #$($i + 1)"
        $itemPrice = $item.price + (Get-Random -Minimum -2 -Maximum 3)
        if ($itemPrice -lt 1) { $itemPrice = 1.99 }
        
        $success = Add-Item -restaurantId $restaurantId -name $itemName -category $item.category -description $item.description -price $itemPrice
        if ($success) {
            $itemCount++
        }
        Start-Sleep -Milliseconds 50
    }
}

Write-Host "Added $itemCount items total" -ForegroundColor Green
Write-Host "Script completed successfully!" -ForegroundColor Green 
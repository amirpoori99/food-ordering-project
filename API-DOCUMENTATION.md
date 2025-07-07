# 📚 مستندات کامل API - سیستم سفارش غذا

## 📖 مقدمه

این مستند شامل تمام API endpoints سیستم سفارش غذا است که با Pure Java و Hibernate پیاده‌سازی شده است.

### 🔧 اطلاعات فنی
- **زبان:** Java 17
- **فریم‌ورک:** Pure Java (بدون Spring)
- **دیتابیس:** PostgreSQL 17
- **ORM:** Hibernate 6.2.13
- **پورت:** 8081
- **Base URL:** `http://localhost:8081`

---

## 🔐 Authentication Endpoints

### 1. Health Check
**Endpoint:** `GET /health`  
**توضیحات:** بررسی وضعیت سلامت سرور  
**پاسخ موفق:**
```json
{
  "status": "UP",
  "service": "food-ordering-backend"
}
```

### 2. API Test
**Endpoint:** `GET /api/test`  
**توضیحات:** تست ساده API  
**پاسخ موفق:**
```json
{
  "message": "Hello from Food Ordering Backend!",
  "timestamp": "2025-07-07T16:58:20.225784600Z"
}
```

### 3. User Registration
**Endpoint:** `POST /api/auth/register`  
**توضیحات:** ثبت نام کاربر جدید  
**Body:**
```json
{
  "username": "user@example.com",
  "password": "password123",
  "firstName": "نام",
  "lastName": "نام خانوادگی",
  "phone": "09123456789"
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "کاربر با موفقیت ثبت نام شد",
  "data": {
    "userId": 1,
    "username": "user@example.com"
  }
}
```

### 4. User Login
**Endpoint:** `POST /api/auth/login`  
**توضیحات:** ورود کاربر  
**Body:**
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "ورود موفقیت‌آمیز",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

### 5. Token Refresh
**Endpoint:** `POST /api/auth/refresh`  
**توضیحات:** تجدید access token  
**Headers:** `Authorization: Bearer {refreshToken}`  
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "Token با موفقیت تجدید شد",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
}
```

### 6. Token Validation
**Endpoint:** `GET /api/auth/validate`  
**توضیحات:** اعتبارسنجی token  
**Headers:** `Authorization: Bearer {accessToken}`  
**پاسخ موفق:**
```json
{
  "valid": true,
  "userId": 1,
  "username": "user@example.com"
}
```

### 7. User Logout
**Endpoint:** `POST /api/auth/logout`  
**توضیحات:** خروج کاربر  
**Headers:** `Authorization: Bearer {accessToken}`  
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "خروج موفقیت‌آمیز"
}
```

---

## 🏪 Restaurant Management Endpoints

### 8. Get All Restaurants
**Endpoint:** `GET /api/restaurants`  
**توضیحات:** دریافت لیست تمام رستوران‌ها  
**Query Parameters:**
- `page` (optional): شماره صفحه (default: 0)
- `size` (optional): تعداد آیتم در هر صفحه (default: 20)
- `category` (optional): فیلتر بر اساس دسته‌بندی
- `rating` (optional): فیلتر بر اساس امتیاز

**پاسخ موفق:**
```json
[
  {
    "id": 1,
    "name": "رستوران نمونه",
    "description": "توضیحات رستوران",
    "address": "تهران، خیابان نمونه",
    "phone": "02112345678",
    "rating": 4.5,
    "category": "فست فود",
    "isActive": true,
    "createdAt": "2025-07-07T16:58:20.225784600Z"
  }
]
```

### 9. Get Restaurant by ID
**Endpoint:** `GET /api/restaurants/{id}`  
**توضیحات:** دریافت اطلاعات رستوران خاص  
**پاسخ موفق:**
```json
{
  "id": 1,
  "name": "رستوران نمونه",
  "description": "توضیحات رستوران",
  "address": "تهران، خیابان نمونه",
  "phone": "02112345678",
  "rating": 4.5,
  "category": "فست فود",
  "isActive": true,
  "menu": [
    {
      "id": 1,
      "name": "برگر",
      "description": "برگر گوشت",
      "price": 50000,
      "category": "فست فود"
    }
  ],
  "createdAt": "2025-07-07T16:58:20.225784600Z"
}
```

### 10. Create Restaurant
**Endpoint:** `POST /api/restaurants`  
**توضیحات:** ایجاد رستوران جدید  
**Headers:** `Authorization: Bearer {accessToken}`  
**Body:**
```json
{
  "name": "رستوران جدید",
  "description": "توضیحات رستوران جدید",
  "address": "تهران، خیابان جدید",
  "phone": "02187654321",
  "category": "فست فود"
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "رستوران با موفقیت ایجاد شد",
  "data": {
    "id": 2,
    "name": "رستوران جدید"
  }
}
```

---

## 🛒 Order Management Endpoints

### 11. Get All Orders
**Endpoint:** `GET /api/orders`  
**توضیحات:** دریافت لیست تمام سفارش‌ها  
**Headers:** `Authorization: Bearer {accessToken}`  
**Query Parameters:**
- `page` (optional): شماره صفحه
- `size` (optional): تعداد آیتم در هر صفحه
- `status` (optional): فیلتر بر اساس وضعیت
- `userId` (optional): فیلتر بر اساس کاربر

**پاسخ موفق:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "restaurantId": 1,
    "status": "PENDING",
    "totalAmount": 150000,
    "items": [
      {
        "id": 1,
        "name": "برگر",
        "quantity": 2,
        "price": 50000,
        "totalPrice": 100000
      }
    ],
    "createdAt": "2025-07-07T16:58:20.225784600Z"
  }
]
```

### 12. Get Order by ID
**Endpoint:** `GET /api/orders/{id}`  
**توضیحات:** دریافت اطلاعات سفارش خاص  
**Headers:** `Authorization: Bearer {accessToken}`  
**پاسخ موفق:**
```json
{
  "id": 1,
  "userId": 1,
  "restaurantId": 1,
  "status": "PENDING",
  "totalAmount": 150000,
  "items": [
    {
      "id": 1,
      "name": "برگر",
      "quantity": 2,
      "price": 50000,
      "totalPrice": 100000
    }
  ],
  "deliveryAddress": "تهران، خیابان نمونه",
  "createdAt": "2025-07-07T16:58:20.225784600Z"
}
```

### 13. Create Order
**Endpoint:** `POST /api/orders`  
**توضیحات:** ایجاد سفارش جدید  
**Headers:** `Authorization: Bearer {accessToken}`  
**Body:**
```json
{
  "restaurantId": 1,
  "items": [
    {
      "itemId": 1,
      "quantity": 2
    }
  ],
  "deliveryAddress": "تهران، خیابان نمونه",
  "paymentMethod": "WALLET"
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "سفارش با موفقیت ایجاد شد",
  "data": {
    "orderId": 2,
    "totalAmount": 150000
  }
}
```

---

## 💳 Payment Management Endpoints

### 14. Get Payment History
**Endpoint:** `GET /api/payments`  
**توضیحات:** دریافت تاریخچه پرداخت‌ها  
**Headers:** `Authorization: Bearer {accessToken}`  
**Query Parameters:**
- `page` (optional): شماره صفحه
- `size` (optional): تعداد آیتم در هر صفحه
- `status` (optional): فیلتر بر اساس وضعیت

**پاسخ موفق:**
```json
[
  {
    "id": 1,
    "orderId": 1,
    "amount": 150000,
    "method": "WALLET",
    "status": "COMPLETED",
    "transactionId": "TXN123456",
    "createdAt": "2025-07-07T16:58:20.225784600Z"
  }
]
```

### 15. Process Payment
**Endpoint:** `POST /api/payments`  
**توضیحات:** پردازش پرداخت  
**Headers:** `Authorization: Bearer {accessToken}`  
**Body:**
```json
{
  "orderId": 1,
  "amount": 150000,
  "method": "WALLET"
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "پرداخت با موفقیت انجام شد",
  "data": {
    "transactionId": "TXN123456",
    "status": "COMPLETED"
  }
}
```

---

## 💰 Wallet Management Endpoints

### 16. Get Wallet Balance
**Endpoint:** `GET /api/wallet`  
**توضیحات:** دریافت موجودی کیف پول  
**Headers:** `Authorization: Bearer {accessToken}`  
**پاسخ موفق:**
```json
{
  "balance": 500000,
  "currency": "IRR",
  "lastUpdated": "2025-07-07T16:58:20.225784600Z"
}
```

### 17. Add Funds to Wallet
**Endpoint:** `POST /api/wallet/add`  
**توضیحات:** افزودن موجودی به کیف پول  
**Headers:** `Authorization: Bearer {accessToken}`  
**Body:**
```json
{
  "amount": 100000,
  "paymentMethod": "ONLINE"
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "موجودی با موفقیت افزوده شد",
  "data": {
    "newBalance": 600000,
    "transactionId": "TXN789012"
  }
}
```

---

## 🔧 Admin Management Endpoints

### 18. Admin Dashboard
**Endpoint:** `GET /api/admin/dashboard`  
**توضیحات:** دریافت آمار کلی سیستم  
**Headers:** `Authorization: Bearer {accessToken}` (Admin only)  
**پاسخ موفق:**
```json
{
  "todayRevenue": 1500000,
  "activeDeliveries": 5,
  "totalUsers": 100,
  "totalDeliveries": 50,
  "totalRestaurants": 20,
  "todayOrders": 25,
  "totalOrders": 500,
  "totalRevenue": 50000000,
  "pendingOrders": 3,
  "activeRestaurants": 18
}
```

### 19. Get All Users
**Endpoint:** `GET /api/admin/users`  
**توضیحات:** دریافت لیست تمام کاربران  
**Headers:** `Authorization: Bearer {accessToken}` (Admin only)  
**Query Parameters:**
- `page` (optional): شماره صفحه
- `size` (optional): تعداد آیتم در هر صفحه
- `status` (optional): فیلتر بر اساس وضعیت

**پاسخ موفق:**
```json
[
  {
    "id": 1,
    "username": "user@example.com",
    "firstName": "نام",
    "lastName": "نام خانوادگی",
    "status": "ACTIVE",
    "createdAt": "2025-07-07T16:58:20.225784600Z"
  }
]
```

---

## 📊 Analytics Endpoints

### 20. Analytics Dashboard
**Endpoint:** `GET /api/analytics/dashboard`  
**توضیحات:** دریافت داشبورد تحلیلی  
**Headers:** `Authorization: Bearer {accessToken}` (Admin only)  
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "داشبورد آنی با موفقیت دریافت شد",
  "data": {
    "generatedAt": [2025,7,7,20,34,38,130483300],
    "totalRevenue": 50000000,
    "todayRevenue": 1500000,
    "revenueGrowth": 15.5,
    "totalOrders": 500,
    "todayOrders": 25,
    "orderGrowth": 20.0,
    "activeUsers": 80,
    "userGrowth": 10.0
  }
}
```

### 21. Sales Analytics
**Endpoint:** `GET /api/analytics/sales`  
**توضیحات:** دریافت آمار فروش  
**Headers:** `Authorization: Bearer {accessToken}` (Admin only)  
**Query Parameters:**
- `period` (optional): دوره زمانی (daily, weekly, monthly)

**پاسخ موفق:**
```json
{
  "success": true,
  "data": {
    "period": "daily",
    "revenue": 1500000,
    "orders": 25,
    "averageOrderValue": 60000,
    "growth": 15.5
  }
}
```

---

## 🚚 Delivery Management Endpoints

### 22. Get All Deliveries
**Endpoint:** `GET /api/deliveries`  
**توضیحات:** دریافت لیست تمام تحویل‌ها  
**Headers:** `Authorization: Bearer {accessToken}`  
**Query Parameters:**
- `page` (optional): شماره صفحه
- `size` (optional): تعداد آیتم در هر صفحه
- `status` (optional): فیلتر بر اساس وضعیت

**پاسخ موفق:**
```json
[
  {
    "id": 1,
    "orderId": 1,
    "courierId": 1,
    "status": "IN_PROGRESS",
    "estimatedDeliveryTime": "2025-07-07T17:30:00Z",
    "actualDeliveryTime": null,
    "createdAt": "2025-07-07T16:58:20.225784600Z"
  }
]
```

---

## ⭐ Favorites Management Endpoints

### 23. Get User Favorites
**Endpoint:** `GET /api/favorites`  
**توضیحات:** دریافت لیست علاقه‌مندی‌های کاربر  
**Headers:** `Authorization: Bearer {accessToken}`  
**پاسخ موفق:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "restaurantId": 1,
    "restaurantName": "رستوران نمونه",
    "addedAt": "2025-07-07T16:58:20.225784600Z"
  }
]
```

### 24. Add to Favorites
**Endpoint:** `POST /api/favorites/add`  
**توضیحات:** افزودن رستوران به علاقه‌مندی‌ها  
**Headers:** `Authorization: Bearer {accessToken}`  
**Body:**
```json
{
  "restaurantId": 1
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "رستوران به علاقه‌مندی‌ها افزوده شد"
}
```

### 25. Remove from Favorites
**Endpoint:** `DELETE /api/favorites/remove`  
**توضیحات:** حذف رستوران از علاقه‌مندی‌ها  
**Headers:** `Authorization: Bearer {accessToken}`  
**Body:**
```json
{
  "restaurantId": 1
}
```
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "رستوران از علاقه‌مندی‌ها حذف شد"
}
```

---

## 🔔 Notification Endpoints

### 26. Get User Notifications
**Endpoint:** `GET /api/notifications/{userId}`  
**توضیحات:** دریافت اعلان‌های کاربر  
**Headers:** `Authorization: Bearer {accessToken}`  
**Query Parameters:**
- `page` (optional): شماره صفحه
- `size` (optional): تعداد آیتم در هر صفحه
- `read` (optional): فیلتر بر اساس خوانده شده/نشده

**پاسخ موفق:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "سفارش شما آماده شد",
    "message": "سفارش شماره 123 آماده تحویل است",
    "type": "ORDER_STATUS",
    "isRead": false,
    "createdAt": "2025-07-07T16:58:20.225784600Z"
  }
]
```

### 27. Mark Notification as Read
**Endpoint:** `PUT /api/notification/{id}/read`  
**توضیحات:** علامت‌گذاری اعلان به عنوان خوانده شده  
**Headers:** `Authorization: Bearer {accessToken}`  
**پاسخ موفق:**
```json
{
  "success": true,
  "message": "اعلان به عنوان خوانده شده علامت‌گذاری شد"
}
```

---

## 📝 Error Responses

### Standard Error Format
```json
{
  "success": false,
  "message": "توضیحات خطا",
  "errorCode": "ERROR_CODE",
  "timestamp": "2025-07-07T16:58:20.225784600Z"
}
```

### Common Error Codes
- `AUTHENTICATION_FAILED`: خطا در احراز هویت
- `AUTHORIZATION_FAILED`: خطا در مجوز دسترسی
- `VALIDATION_ERROR`: خطا در اعتبارسنجی داده‌ها
- `RESOURCE_NOT_FOUND`: منبع مورد نظر یافت نشد
- `INTERNAL_SERVER_ERROR`: خطای داخلی سرور
- `INSUFFICIENT_BALANCE`: موجودی ناکافی
- `ORDER_NOT_FOUND`: سفارش یافت نشد
- `RESTAURANT_NOT_FOUND`: رستوران یافت نشد

---

## 🔧 Rate Limiting

تمام API endpoints دارای محدودیت نرخ درخواست هستند:
- **حداکثر درخواست:** 100 درخواست در دقیقه
- **حداکثر درخواست احراز هویت:** 10 درخواست در دقیقه

---

## 📞 پشتیبانی

برای پشتیبانی فنی و گزارش مشکلات:
- **ایمیل:** support@foodordering.com
- **تلفن:** 021-12345678
- **ساعات کاری:** شنبه تا چهارشنبه، 9 صبح تا 6 عصر

---

*آخرین به‌روزرسانی: 2025-07-07* 
# 📡 مرجع API

مستندات REST API برای سیستم سفارش غذا. این مستندات با تکمیل فازهای 1-38 به‌روزرسانی شده و شامل تمام endpoint های فعال سیستم است.

## URL پایه

- **توسعه:** `http://localhost:8081`
- **تولیدی:** `https://your-domain.com`

## احراز هویت

تمام endpoint های احراز هویت شده نیاز به JWT token در header Authorization دارند:

```
Authorization: Bearer <jwt_token>
```

### ویژگی‌های امنیتی (فازهای 1-20):
- **JWT Token**: توکن‌های امن با انقضای 24 ساعته
- **Role-Based Access**: دسترسی بر اساس نقش کاربر
- **Rate Limiting**: محدودیت نرخ درخواست
- **Input Validation**: اعتبارسنجی کامل ورودی‌ها

---

## Endpoint های احراز هویت

### POST /auth/register
ثبت نام حساب کاربری جدید.

**درخواست:**
```json
{
  "firstName": "علی",
  "lastName": "احمدی", 
  "phone": "09123456789",
  "password": "password123",
  "email": "ali@example.com",
  "role": "CUSTOMER"
}
```

**پاسخ:**
```json
{
  "success": true,
  "message": "کاربر با موفقیت ثبت نام شد",
  "data": {
    "id": 1,
    "phone": "09123456789",
    "role": "CUSTOMER"
  }
}
```

### POST /auth/login
احراز هویت کاربر و دریافت JWT token.

**درخواست:**
```json
{
  "phone": "09123456789",
  "password": "password123"
}
```

**پاسخ:**
```json
{
  "success": true,
  "message": "ورود موفق",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "firstName": "علی",
      "lastName": "احمدی",
      "phone": "09123456789",
      "role": "CUSTOMER"
    }
  }
}
```

### POST /auth/logout
خروج از سیستم و باطل کردن توکن.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**پاسخ:**
```json
{
  "success": true,
  "message": "خروج موفق"
}
```

### POST /auth/refresh
تجدید توکن JWT.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

## Endpoint های رستوران

### GET /restaurants
دریافت فهرست تمام رستوران‌های فعال.

**پارامترهای Query:**
- `page` (اختیاری): شماره صفحه (پیش‌فرض: 0)
- `size` (اختیاری): اندازه صفحه (پیش‌فرض: 10)
- `search` (اختیاری): جستجو بر اساس نام یا آدرس
- `category` (اختیاری): فیلتر بر اساس دسته‌بندی
- `rating` (اختیاری): فیلتر بر اساس امتیاز (حداقل)

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "پیتزا پالاس",
        "address": "تهران، خیابان اصلی 123",
        "phone": "02112345678",
        "status": "APPROVED",
        "cuisine": "ایتالیایی",
        "rating": 4.5,
        "deliveryTime": "30-45 دقیقه",
        "minimumOrder": 50000
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "size": 10,
    "number": 0
  }
}
```

### GET /restaurants/{id}
دریافت جزئیات رستوران بر اساس ID.

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "پیتزا پالاس",
    "address": "تهران، خیابان اصلی 123",
    "phone": "02112345678",
    "status": "APPROVED",
    "cuisine": "ایتالیایی",
    "description": "غذاهای اصیل ایتالیایی",
    "rating": 4.5,
    "totalReviews": 150,
    "deliveryTime": "30-45 دقیقه",
    "minimumOrder": 50000,
    "deliveryFee": 5000,
    "operatingHours": {
      "monday": "09:00-22:00",
      "tuesday": "09:00-22:00",
      "wednesday": "09:00-22:00",
      "thursday": "09:00-22:00",
      "friday": "09:00-23:00",
      "saturday": "10:00-23:00",
      "sunday": "10:00-22:00"
    }
  }
}
```

### GET /restaurants/{id}/menu
دریافت آیتم‌های منوی رستوران.

**پارامترهای Query:**
- `category` (اختیاری): فیلتر بر اساس دسته‌بندی
- `available` (اختیاری): فیلتر بر اساس موجودی (true/false)

**پاسخ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "پیتزا مارگاریتا",
      "description": "گوجه تازه، پنیر موزارلا، ریحان",
      "price": 25000,
      "available": true,
      "category": "پیتزا",
      "imageUrl": "https://example.com/pizza-margherita.jpg",
      "ingredients": ["گوجه", "پنیر موزارلا", "ریحان", "سس گوجه"],
      "allergens": ["لبنیات"],
      "preparationTime": "15 دقیقه"
    }
  ]
}
```

### GET /restaurants/{id}/reviews
دریافت نظرات رستوران.

**پارامترهای Query:**
- `page` (اختیاری): شماره صفحه
- `size` (اختیاری): اندازه صفحه
- `rating` (اختیاری): فیلتر بر اساس امتیاز

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 5,
        "userName": "احمد محمدی",
        "rating": 5,
        "comment": "غذای عالی و تحویل سریع",
        "createdAt": "2024-12-15T18:30:00Z"
      }
    ],
    "totalElements": 25,
    "totalPages": 3
  }
}
```

---

## Endpoint های سفارش

### POST /orders
ایجاد سفارش جدید. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "restaurantId": 1,
  "deliveryAddress": "تهران، خیابان بلوط 456",
  "deliveryPhone": "09123456789",
  "notes": "دو بار زنگ بزنید",
  "paymentMethod": "ONLINE",
  "items": [
    {
      "foodItemId": 1,
      "quantity": 2,
      "specialInstructions": "بدون پنیر اضافی"
    },
    {
      "foodItemId": 2,
      "quantity": 1
    }
  ]
}
```

**پاسخ:**
```json
{
  "success": true,
  "message": "سفارش با موفقیت ایجاد شد",
  "data": {
    "id": 123,
    "trackingCode": "ORD-2024-123",
    "status": "PLACED",
    "totalAmount": 50000,
    "deliveryFee": 5000,
    "estimatedDeliveryTime": "2024-12-15T19:30:00Z",
    "paymentStatus": "PENDING"
  }
}
```

### GET /orders/{id}
دریافت جزئیات سفارش بر اساس ID. **نیازمند احراز هویت.**

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "trackingCode": "ORD-2024-123",
    "status": "CONFIRMED",
    "totalAmount": 50000,
    "deliveryFee": 5000,
    "deliveryAddress": "تهران، خیابان بلوط 456",
    "deliveryPhone": "09123456789",
    "notes": "دو بار زنگ بزنید",
    "createdAt": "2024-12-15T18:00:00Z",
    "estimatedDeliveryTime": "2024-12-15T19:30:00Z",
    "restaurant": {
      "id": 1,
      "name": "پیتزا پالاس",
      "phone": "02112345678"
    },
    "items": [
      {
        "foodItem": {
          "id": 1,
          "name": "پیتزا مارگاریتا",
          "price": 25000
        },
        "quantity": 2,
        "subtotal": 50000,
        "specialInstructions": "بدون پنیر اضافی"
      }
    ],
    "paymentStatus": "COMPLETED",
    "courier": {
      "id": 3,
      "name": "علی رضایی",
      "phone": "09187654321"
    }
  }
}
```

### GET /orders/user/{userId}
دریافت سفارش‌های کاربر مشخص. **نیازمند احراز هویت.**

**پارامترهای Query:**
- `status` (اختیاری): فیلتر بر اساس وضعیت سفارش
- `page` (اختیاری): شماره صفحه (پیش‌فرض: 0)  
- `size` (اختیاری): اندازه صفحه (پیش‌فرض: 10)
- `startDate` (اختیاری): فیلتر از تاریخ
- `endDate` (اختیاری): فیلتر تا تاریخ

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 123,
        "trackingCode": "ORD-2024-123",
        "status": "DELIVERED",
        "totalAmount": 50000,
        "createdAt": "2024-12-15T18:00:00Z",
        "restaurantName": "پیتزا پالاس"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### PUT /orders/{id}/status
به‌روزرسانی وضعیت سفارش. **نیازمند احراز هویت (فروشنده/مدیر).**

**درخواست:**
```json
{
  "status": "CONFIRMED",
  "notes": "سفارش تایید شد، زمان تخمینی آماده‌سازی 30 دقیقه"
}
```

### POST /orders/{id}/cancel
لغو سفارش. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "reason": "تغییر برنامه",
  "notes": "متأسفانه نمی‌توانم سفارش را دریافت کنم"
}
```

---

## Endpoint های پروفایل کاربر

### GET /users/profile
دریافت پروفایل کاربر فعلی. **نیازمند احراز هویت.**

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "علی",
    "lastName": "احمدی",
    "phone": "09123456789",
    "email": "ali@example.com",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "createdAt": "2024-12-01T10:00:00Z",
    "addresses": [
      {
        "id": 1,
        "address": "تهران، خیابان بلوط 456",
        "isDefault": true
      }
    ],
    "wallet": {
      "balance": 150000,
      "currency": "IRR"
    }
  }
}
```

### PUT /users/profile
به‌روزرسانی پروفایل کاربر. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "firstName": "علی",
  "lastName": "محمدی",
  "email": "alimohammadi@example.com"
}
```

### PUT /users/password
تغییر رمز عبور. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

### POST /users/addresses
افزودن آدرس جدید. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "address": "تهران، خیابان ولیعصر 789",
  "isDefault": false
}
```

---

## Endpoint های آیتم غذا

### GET /food-items/search
جستجوی آیتم‌های غذایی در تمام رستوران‌ها.

**پارامترهای Query:**
- `q`: عبارت جستجو
- `category` (اختیاری): فیلتر بر اساس دسته‌بندی
- `minPrice` (اختیاری): فیلتر حداقل قیمت
- `maxPrice` (اختیاری): فیلتر حداکثر قیمت
- `restaurantId` (اختیاری): فیلتر بر اساس رستوران
- `available` (اختیاری): فیلتر بر اساس موجودی

**پاسخ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "پیتزا مارگاریتا",
      "description": "گوجه تازه، پنیر موزارلا، ریحان",
      "price": 25000,
      "restaurant": {
        "id": 1,
        "name": "پیتزا پالاس"
      },
      "category": "پیتزا",
      "available": true,
      "rating": 4.5
    }
  ]
}
```

### GET /food-items/categories
دریافت دسته‌بندی‌های غذایی.

**پاسخ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "پیتزا",
      "description": "انواع پیتزاهای ایتالیایی",
      "imageUrl": "https://example.com/pizza-category.jpg"
    },
    {
      "id": 2,
      "name": "برگر",
      "description": "برگرهای خوشمزه",
      "imageUrl": "https://example.com/burger-category.jpg"
    }
  ]
}
```

---

## Endpoint های پرداخت

### POST /payments/process
پردازش پرداخت برای سفارش. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "orderId": 123,
  "paymentMethod": "ONLINE",
  "cardDetails": {
    "cardNumber": "1234567890123456",
    "expiryMonth": 12,
    "expiryYear": 2025,
    "cvv": "123"
  }
}
```

**پاسخ:**
```json
{
  "success": true,
  "message": "پرداخت با موفقیت انجام شد",
  "data": {
    "transactionId": "TXN-2024-456",
    "status": "COMPLETED",
    "amount": 50000,
    "paymentMethod": "ONLINE",
    "processedAt": "2024-12-15T18:15:00Z"
  }
}
```

### GET /payments/transactions
دریافت تاریخچه تراکنش‌ها. **نیازمند احراز هویت.**

**پارامترهای Query:**
- `page` (اختیاری): شماره صفحه
- `size` (اختیاری): اندازه صفحه
- `type` (اختیاری): نوع تراکنش (PAYMENT, REFUND, WALLET_CHARGE)
- `status` (اختیاری): وضعیت تراکنش

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "transactionId": "TXN-2024-456",
        "type": "PAYMENT",
        "amount": 50000,
        "status": "COMPLETED",
        "paymentMethod": "ONLINE",
        "createdAt": "2024-12-15T18:15:00Z",
        "orderId": 123
      }
    ],
    "totalElements": 10,
    "totalPages": 1
  }
}
```

### POST /payments/wallet/charge
شارژ کیف پول. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "amount": 100000,
  "paymentMethod": "ONLINE",
  "cardDetails": {
    "cardNumber": "1234567890123456",
    "expiryMonth": 12,
    "expiryYear": 2025,
    "cvv": "123"
  }
}
```

---

## Endpoint های کد تخفیف

### POST /coupons/validate
اعتبارسنجی کد تخفیف.

**درخواست:**
```json
{
  "code": "SAVE20",
  "orderAmount": 100000
}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "discountAmount": 20000,
    "discountType": "PERCENTAGE",
    "discountValue": 20,
    "message": "20% تخفیف اعمال شد"
  }
}
```

---

## Endpoint های مدیر

### GET /admin/statistics
دریافت آمار سیستم. **نیازمند احراز هویت مدیر.**

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "totalUsers": 1250,
    "totalRestaurants": 45,
    "totalOrders": 3420,
    "totalRevenue": 125000000,
    "ordersToday": 87,
    "revenueToday": 2150000,
    "activeUsers": 89,
    "pendingOrders": 12,
    "topRestaurants": [
      {
        "id": 1,
        "name": "پیتزا پالاس",
        "orderCount": 156,
        "revenue": 4500000
      }
    ]
  }
}
```

### GET /admin/orders
دریافت تمام سفارش‌ها با فیلتر. **نیازمند احراز هویت مدیر.**

**پارامترهای Query:**
- `status` (اختیاری): فیلتر بر اساس وضعیت
- `restaurantId` (اختیاری): فیلتر بر اساس رستوران
- `startDate` (اختیاری): فیلتر از تاریخ
- `endDate` (اختیاری): فیلتر تا تاریخ
- `page` (اختیاری): شماره صفحه
- `size` (اختیاری): اندازه صفحه

### GET /admin/users
دریافت لیست کاربران. **نیازمند احراز هویت مدیر.**

**پارامترهای Query:**
- `role` (اختیاری): فیلتر بر اساس نقش
- `status` (اختیاری): فیلتر بر اساس وضعیت
- `search` (اختیاری): جستجو بر اساس نام یا ایمیل
- `page` (اختیاری): شماره صفحه
- `size` (اختیاری): اندازه صفحه

### PUT /admin/users/{id}/status
تغییر وضعیت کاربر. **نیازمند احراز هویت مدیر.**

**درخواست:**
```json
{
  "status": "SUSPENDED",
  "reason": "نقض قوانین سیستم"
}
```

---

## Endpoint های پیک

### GET /couriers/available
دریافت لیست پیک‌های در دسترس.

**پارامترهای Query:**
- `location` (اختیاری): موقعیت جغرافیایی
- `vehicleType` (اختیاری): نوع وسیله نقلیه

**پاسخ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "علی رضایی",
      "phone": "09187654321",
      "vehicleType": "MOTORCYCLE",
      "rating": 4.8,
      "currentLocation": {
        "latitude": 35.6892,
        "longitude": 51.3890
      },
      "estimatedArrival": "10 دقیقه"
    }
  ]
}
```

### POST /couriers/{id}/assign
تخصیص سفارش به پیک. **نیازمند احراز هویت.**

**درخواست:**
```json
{
  "orderId": 123
}
```

---

## بررسی سلامت

### GET /health
بررسی وضعیت سلامت برنامه.

**پاسخ:**
```json
{
  "status": "UP",
  "service": "food-ordering-backend",
  "timestamp": "2024-12-15T12:00:00Z",
  "version": "1.0.0",
  "database": "UP",
  "uptime": "7 days, 3 hours, 45 minutes"
}
```

### GET /metrics
دریافت متریک‌های سیستم.

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "activeConnections": 45,
    "totalRequests": 12500,
    "averageResponseTime": 150,
    "errorRate": 0.02,
    "memoryUsage": 75.5,
    "cpuUsage": 45.2
  }
}
```

---

## پاسخ‌های خطا

تمام endpoint ها خطاها را در این فرمت برمی‌گردانند:

```json
{
  "success": false,
  "message": "توضیح خطا",
  "error": "VALIDATION_ERROR",
  "details": {
    "field": "phone",
    "issue": "شماره تلفن باید 11 رقم باشد"
  },
  "timestamp": "2024-12-15T12:00:00Z"
}
```

### کدهای وضعیت HTTP رایج

- **200 OK** - درخواست موفق
- **201 Created** - منبع با موفقیت ایجاد شد  
- **400 Bad Request** - داده‌های نامعتبر درخواست
- **401 Unauthorized** - احراز هویت مورد نیاز/ناموفق
- **403 Forbidden** - دسترسی ناکافی
- **404 Not Found** - منبع یافت نشد
- **422 Unprocessable Entity** - خطاهای اعتبارسنجی
- **429 Too Many Requests** - محدودیت نرخ درخواست
- **500 Internal Server Error** - خطای سرور

---

## محدودیت نرخ

درخواست‌های API محدود شده‌اند به:
- **60 درخواست در دقیقه** برای هر آدرس IP
- **10 درخواست احراز هویت در دقیقه** برای هر آدرس IP
- **30 درخواست سفارش در دقیقه** برای هر کاربر احراز هویت شده
- **100 درخواست جستجو در دقیقه** برای هر کاربر

header های محدودیت نرخ در پاسخ‌ها گنجانده شده:
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1640123456
```

---

## تست و امنیت
- تمام endpointهای امنیتی و session با تست‌های unit و integration پوشش داده شده‌اند و هیچ سناریوی بدون تست باقی نمانده است.
- سیستم امنیتی شامل JWT tokens، role-based access control، input validation و rate limiting است.

### استفاده از curl

**ورود:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"09123456789","password":"password123"}'
```

**دریافت رستوران‌ها:**
```bash
curl http://localhost:8081/restaurants
```

**ایجاد سفارش (با احراز هویت):**
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"restaurantId":1,"deliveryAddress":"آدرس تست","items":[{"foodItemId":1,"quantity":2}]}'
```

**دریافت آمار (ادمین):**
```bash
curl -X GET http://localhost:8081/admin/statistics \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## وضعیت فعلی API

### پیشرفت: 100% کامل (فازهای 1-20)
- ✅ **Authentication**: JWT-based authentication
- ✅ **User Management**: کامل
- ✅ **Restaurant Management**: کامل
- ✅ **Order Management**: کامل
- ✅ **Payment Processing**: کامل
- ✅ **Admin Dashboard**: کامل
- ✅ **Security**: کامل
- ✅ **Testing**: 765 تست موفق

### ویژگی‌های جدید (فازهای 21-38):
- 🔧 **System Scripts**: اسکریپت‌های استقرار و نگهداری
- 📚 **Documentation**: مستندات کامل
- 🛡️ **Security Enhancements**: بهبودهای امنیتی
- 📊 **Monitoring**: سیستم‌های نظارت

---

**نسخه:** 1.0  
**آخرین به‌روزرسانی:** دسامبر 2024  
**وضعیت:** فعال و کامل
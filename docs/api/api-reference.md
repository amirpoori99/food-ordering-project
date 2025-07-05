# 📡 API Reference - Food Ordering System

> **مرجع کامل API های سیستم سفارش غذا**

---

## 📋 فهرست مطالب

1. [مقدمه و اطلاعات کلی](#مقدمه-و-اطلاعات-کلی)
2. [احراز هویت](#احراز-هویت)
3. [کاربران (Users)](#کاربران-users)
4. [رستوران‌ها (Restaurants)](#رستورانها-restaurants)
5. [منوها و غذاها (Menu & Food Items)](#منوها-و-غذاها-menu--food-items)
6. [سفارشات (Orders)](#سفارشات-orders)
7. [پرداخت (Payments)](#پرداخت-payments)
8. [کیف پول (Wallet)](#کیف-پول-wallet)
9. [آنالیتیکس (Analytics)](#آنالیتیکس-analytics)
10. [مدیریت (Admin)](#مدیریت-admin)
11. [کدهای خطا](#کدهای-خطا)
12. [نمونه‌های کاربردی](#نمونههای-کاربردی)

---

## 🌐 مقدمه و اطلاعات کلی

### Base URLs
- **Production**: `https://food-ordering.com/api`
- **Development**: `http://localhost:8081/api`

### فرمت درخواست‌ها
- **Content-Type**: `application/json`
- **Authorization**: `Bearer {token}`
- **Accept**: `application/json`

### فرمت پاسخ‌ها
```json
{
  "success": true,
  "data": {...},
  "message": "عملیات با موفقیت انجام شد",
  "timestamp": "2024-11-20T10:30:00Z"
}
```

### Status Codes
- **200**: OK - درخواست موفق
- **201**: Created - منبع جدید ایجاد شد
- **400**: Bad Request - درخواست نامعتبر
- **401**: Unauthorized - احراز هویت نشده
- **403**: Forbidden - عدم دسترسی
- **404**: Not Found - منبع یافت نشد
- **500**: Internal Server Error - خطای سرور

---

## 🔐 احراز هویت

### POST /auth/register
ثبت‌نام کاربر جدید

**درخواست:**
```json
{
  "username": "ali_rezaei",
  "email": "ali@example.com",
  "password": "SecurePass123",
  "fullName": "علی رضایی",
  "phone": "09123456789",
  "address": "تهران، خیابان ولیعصر",
  "role": "CUSTOMER"
}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "ali_rezaei",
    "email": "ali@example.com",
    "fullName": "علی رضایی",
    "phone": "09123456789",
    "role": "CUSTOMER",
    "createdAt": "2024-11-20T10:30:00Z",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "message": "کاربر با موفقیت ثبت شد"
}
```

### POST /auth/login
ورود کاربر

**درخواست:**
```json
{
  "username": "ali_rezaei",
  "password": "SecurePass123"
}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "ali_rezaei",
      "email": "ali@example.com",
      "fullName": "علی رضایی",
      "role": "CUSTOMER"
    }
  },
  "message": "ورود موفقیت‌آمیز"
}
```

### POST /auth/logout
خروج کاربر

**Headers:**
```
Authorization: Bearer {token}
```

**پاسخ:**
```json
{
  "success": true,
  "message": "خروج موفقیت‌آمیز"
}
```

### POST /auth/refresh
تمدید token

**درخواست:**
```json
{
  "refreshToken": "refresh_token_here"
}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "token": "new_access_token",
    "refreshToken": "new_refresh_token"
  }
}
```

---

## 👥 کاربران (Users)

### GET /users/profile
دریافت اطلاعات پروفایل کاربر

**Headers:**
```
Authorization: Bearer {token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "ali_rezaei",
    "email": "ali@example.com",
    "fullName": "علی رضایی",
    "phone": "09123456789",
    "address": "تهران، خیابان ولیعصر",
    "role": "CUSTOMER",
    "createdAt": "2024-11-20T10:30:00Z",
    "lastLogin": "2024-11-20T15:45:00Z",
    "isActive": true
  }
}
```

### PUT /users/profile
به‌روزرسانی پروفایل کاربر

**Headers:**
```
Authorization: Bearer {token}
```

**درخواست:**
```json
{
  "fullName": "علی رضایی محمدی",
  "phone": "09123456780",
  "address": "تهران، خیابان کریمخان"
}
```

### POST /users/change-password
تغییر رمز عبور

**Headers:**
```
Authorization: Bearer {token}
```

**درخواست:**
```json
{
  "currentPassword": "OldPassword123",
  "newPassword": "NewPassword123",
  "confirmPassword": "NewPassword123"
}
```

---

## 🍽️ رستوران‌ها (Restaurants)

### GET /restaurants
دریافت لیست رستوران‌ها

**Query Parameters:**
- `page`: شماره صفحه (پیش‌فرض: 1)
- `limit`: تعداد آیتم در هر صفحه (پیش‌فرض: 10)
- `search`: جستجو در نام رستوران
- `category`: فیلتر بر اساس دسته‌بندی
- `city`: فیلتر بر اساس شهر
- `isOpen`: فیلتر رستوران‌های باز

**مثال:**
```
GET /restaurants?page=1&limit=10&search=پیتزا&category=ITALIAN&isOpen=true
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "restaurants": [
      {
        "id": 1,
        "name": "پیتزا اسپرسو",
        "description": "بهترین پیتزاهای ایتالیایی",
        "category": "ITALIAN",
        "address": "تهران، خیابان فرشته",
        "phone": "02112345678",
        "rating": 4.5,
        "reviewCount": 150,
        "deliveryTime": "30-45 دقیقه",
        "deliveryFee": 15000,
        "minimumOrder": 50000,
        "isOpen": true,
        "imageUrl": "https://example.com/restaurant1.jpg",
        "openingHours": "10:00-23:00"
      }
    ],
    "totalCount": 25,
    "currentPage": 1,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

### GET /restaurants/{id}
دریافت جزئیات رستوران

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "پیتزا اسپرسو",
    "description": "بهترین پیتزاهای ایتالیایی",
    "category": "ITALIAN",
    "address": "تهران، خیابان فرشته",
    "phone": "02112345678",
    "rating": 4.5,
    "reviewCount": 150,
    "deliveryTime": "30-45 دقیقه",
    "deliveryFee": 15000,
    "minimumOrder": 50000,
    "isOpen": true,
    "imageUrl": "https://example.com/restaurant1.jpg",
    "openingHours": "10:00-23:00",
    "menu": [
      {
        "id": 1,
        "name": "پیتزا مارگاریتا",
        "description": "پیتزا کلاسیک با پنیر موزارلا",
        "price": 85000,
        "category": "PIZZA",
        "imageUrl": "https://example.com/pizza1.jpg",
        "isAvailable": true
      }
    ]
  }
}
```

### POST /restaurants
ایجاد رستوران جدید (فقط مدیر)

**Headers:**
```
Authorization: Bearer {admin_token}
```

**درخواست:**
```json
{
  "name": "رستوران جدید",
  "description": "توضیحات رستوران",
  "category": "PERSIAN",
  "address": "آدرس رستوران",
  "phone": "02187654321",
  "deliveryTime": "45-60 دقیقه",
  "deliveryFee": 20000,
  "minimumOrder": 75000,
  "openingHours": "11:00-24:00"
}
```

---

## 🍕 منوها و غذاها (Menu & Food Items)

### GET /restaurants/{restaurantId}/menu
دریافت منوی رستوران

**Query Parameters:**
- `category`: فیلتر بر اساس دسته‌بندی غذا
- `available`: فیلتر غذاهای موجود

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "restaurantId": 1,
    "restaurantName": "پیتزا اسپرسو",
    "categories": [
      {
        "name": "PIZZA",
        "displayName": "پیتزا",
        "items": [
          {
            "id": 1,
            "name": "پیتزا مارگاریتا",
            "description": "پیتزا کلاسیک با پنیر موزارلا و ریحان",
            "price": 85000,
            "category": "PIZZA",
            "imageUrl": "https://example.com/pizza1.jpg",
            "isAvailable": true,
            "preparationTime": 15,
            "ingredients": ["خمیر پیتزا", "پنیر موزارلا", "سس گوجه", "ریحان"],
            "allergens": ["گلوتن", "لبنیات"],
            "nutrition": {
              "calories": 280,
              "protein": 12,
              "carbs": 35,
              "fat": 8
            }
          }
        ]
      }
    ]
  }
}
```

### GET /items/{id}
دریافت جزئیات آیتم غذا

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "پیتزا مارگاریتا",
    "description": "پیتزا کلاسیک با پنیر موزارلا و ریحان",
    "price": 85000,
    "category": "PIZZA",
    "restaurantId": 1,
    "restaurantName": "پیتزا اسپرسو",
    "imageUrl": "https://example.com/pizza1.jpg",
    "isAvailable": true,
    "preparationTime": 15,
    "ingredients": ["خمیر پیتزا", "پنیر موزارلا", "سس گوجه", "ریحان"],
    "allergens": ["گلوتن", "لبنیات"],
    "nutrition": {
      "calories": 280,
      "protein": 12,
      "carbs": 35,
      "fat": 8
    },
    "reviews": [
      {
        "id": 1,
        "userId": 2,
        "userName": "فاطمه احمدی",
        "rating": 5,
        "comment": "خیلی خوشمزه بود",
        "createdAt": "2024-11-19T18:30:00Z"
      }
    ]
  }
}
```

---

## 🛒 سفارشات (Orders)

### POST /orders
ایجاد سفارش جدید

**Headers:**
```
Authorization: Bearer {token}
```

**درخواست:**
```json
{
  "restaurantId": 1,
  "items": [
    {
      "itemId": 1,
      "quantity": 2,
      "specialInstructions": "بدون پیاز"
    },
    {
      "itemId": 2,
      "quantity": 1
    }
  ],
  "deliveryAddress": "تهران، خیابان ولیعصر، پلاک 123",
  "deliveryTime": "ASAP",
  "paymentMethod": "WALLET",
  "specialInstructions": "زنگ بزنید قبل از تحویل"
}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1001,
    "orderNumber": "FO-2024-001001",
    "status": "PENDING",
    "customerId": 1,
    "restaurantId": 1,
    "restaurantName": "پیتزا اسپرسو",
    "items": [
      {
        "id": 1,
        "name": "پیتزا مارگاریتا",
        "quantity": 2,
        "price": 85000,
        "totalPrice": 170000,
        "specialInstructions": "بدون پیاز"
      }
    ],
    "subtotal": 170000,
    "deliveryFee": 15000,
    "tax": 18500,
    "total": 203500,
    "deliveryAddress": "تهران، خیابان ولیعصر، پلاک 123",
    "estimatedDeliveryTime": "2024-11-20T12:30:00Z",
    "createdAt": "2024-11-20T11:00:00Z"
  }
}
```

### GET /orders
دریافت لیست سفارشات کاربر

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page`: شماره صفحه
- `limit`: تعداد آیتم در هر صفحه
- `status`: فیلتر بر اساس وضعیت سفارش
- `startDate`: فیلتر از تاریخ شروع
- `endDate`: فیلتر تا تاریخ پایان

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "orders": [
      {
        "id": 1001,
        "orderNumber": "FO-2024-001001",
        "status": "DELIVERED",
        "restaurantName": "پیتزا اسپرسو",
        "totalItems": 3,
        "total": 203500,
        "createdAt": "2024-11-20T11:00:00Z",
        "deliveredAt": "2024-11-20T12:25:00Z"
      }
    ],
    "totalCount": 15,
    "currentPage": 1,
    "totalPages": 2
  }
}
```

### GET /orders/{id}
دریافت جزئیات سفارش

**Headers:**
```
Authorization: Bearer {token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "id": 1001,
    "orderNumber": "FO-2024-001001",
    "status": "DELIVERED",
    "customerId": 1,
    "customerName": "علی رضایی",
    "restaurantId": 1,
    "restaurantName": "پیتزا اسپرسو",
    "items": [
      {
        "id": 1,
        "name": "پیتزا مارگاریتا",
        "quantity": 2,
        "price": 85000,
        "totalPrice": 170000,
        "specialInstructions": "بدون پیاز"
      }
    ],
    "subtotal": 170000,
    "deliveryFee": 15000,
    "tax": 18500,
    "total": 203500,
    "deliveryAddress": "تهران، خیابان ولیعصر، پلاک 123",
    "specialInstructions": "زنگ بزنید قبل از تحویل",
    "paymentMethod": "WALLET",
    "createdAt": "2024-11-20T11:00:00Z",
    "estimatedDeliveryTime": "2024-11-20T12:30:00Z",
    "deliveredAt": "2024-11-20T12:25:00Z",
    "statusHistory": [
      {
        "status": "PENDING",
        "timestamp": "2024-11-20T11:00:00Z"
      },
      {
        "status": "CONFIRMED",
        "timestamp": "2024-11-20T11:02:00Z"
      },
      {
        "status": "PREPARING",
        "timestamp": "2024-11-20T11:05:00Z"
      },
      {
        "status": "OUT_FOR_DELIVERY",
        "timestamp": "2024-11-20T12:00:00Z"
      },
      {
        "status": "DELIVERED",
        "timestamp": "2024-11-20T12:25:00Z"
      }
    ]
  }
}
```

### PUT /orders/{id}/cancel
لغو سفارش

**Headers:**
```
Authorization: Bearer {token}
```

**درخواست:**
```json
{
  "reason": "تغییر نظر"
}
```

---

## 💳 پرداخت (Payments)

### POST /payments/process
پردازش پرداخت

**Headers:**
```
Authorization: Bearer {token}
```

**درخواست:**
```json
{
  "orderId": 1001,
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "number": "1234567890123456",
    "expiryMonth": 12,
    "expiryYear": 2025,
    "cvv": "123",
    "holderName": "علی رضایی"
  },
  "amount": 203500
}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "transactionId": "TXN-2024-001001",
    "orderId": 1001,
    "amount": 203500,
    "status": "SUCCESS",
    "paymentMethod": "CREDIT_CARD",
    "gatewayResponse": {
      "referenceNumber": "REF123456789",
      "authCode": "AUTH789"
    },
    "processedAt": "2024-11-20T11:01:00Z"
  }
}
```

### GET /payments/history
تاریخچه پرداخت‌ها

**Headers:**
```
Authorization: Bearer {token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "payments": [
      {
        "id": 1,
        "transactionId": "TXN-2024-001001",
        "orderId": 1001,
        "amount": 203500,
        "status": "SUCCESS",
        "paymentMethod": "CREDIT_CARD",
        "createdAt": "2024-11-20T11:01:00Z"
      }
    ]
  }
}
```

---

## 👛 کیف پول (Wallet)

### GET /wallet/balance
موجودی کیف پول

**Headers:**
```
Authorization: Bearer {token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "balance": 150000,
    "currency": "IRR",
    "lastUpdated": "2024-11-20T10:00:00Z"
  }
}
```

### POST /wallet/charge
شارژ کیف پول

**Headers:**
```
Authorization: Bearer {token}
```

**درخواست:**
```json
{
  "amount": 100000,
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "number": "1234567890123456",
    "expiryMonth": 12,
    "expiryYear": 2025,
    "cvv": "123"
  }
}
```

### GET /wallet/transactions
تراکنش‌های کیف پول

**Headers:**
```
Authorization: Bearer {token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "transactions": [
      {
        "id": 1,
        "type": "CHARGE",
        "amount": 100000,
        "description": "شارژ کیف پول",
        "createdAt": "2024-11-20T09:00:00Z"
      },
      {
        "id": 2,
        "type": "PAYMENT",
        "amount": -50000,
        "description": "پرداخت سفارش FO-2024-001001",
        "createdAt": "2024-11-20T11:01:00Z"
      }
    ]
  }
}
```

---

## 📊 آنالیتیکس (Analytics)

### GET /analytics/dashboard
داشبورد آنالیتیکس (مدیر)

**Headers:**
```
Authorization: Bearer {admin_token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "totalOrders": 1250,
    "totalRevenue": 45000000,
    "activeUsers": 450,
    "averageOrderValue": 180000,
    "topRestaurants": [
      {
        "id": 1,
        "name": "پیتزا اسپرسو",
        "orders": 85,
        "revenue": 2500000
      }
    ],
    "ordersByStatus": {
      "PENDING": 12,
      "PREPARING": 8,
      "OUT_FOR_DELIVERY": 15,
      "DELIVERED": 1215
    },
    "revenueByMonth": {
      "2024-01": 3200000,
      "2024-02": 3800000,
      "2024-03": 4200000
    }
  }
}
```

### GET /analytics/restaurant/{id}
آنالیتیکس رستوران (مدیر رستوران)

**Headers:**
```
Authorization: Bearer {restaurant_admin_token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "restaurantId": 1,
    "totalOrders": 155,
    "totalRevenue": 5200000,
    "averageRating": 4.5,
    "popularItems": [
      {
        "id": 1,
        "name": "پیتزا مارگاریتا",
        "orders": 45,
        "revenue": 850000
      }
    ],
    "ordersByHour": {
      "12": 15,
      "13": 25,
      "19": 35,
      "20": 40
    }
  }
}
```

---

## 🔧 مدیریت (Admin)

### GET /admin/users
لیست کاربران (مدیر)

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Query Parameters:**
- `page`: شماره صفحه
- `limit`: تعداد آیتم در هر صفحه
- `role`: فیلتر بر اساس نقش
- `status`: فیلتر بر اساس وضعیت

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": 1,
        "username": "ali_rezaei",
        "email": "ali@example.com",
        "fullName": "علی رضایی",
        "role": "CUSTOMER",
        "isActive": true,
        "createdAt": "2024-11-20T10:30:00Z",
        "lastLogin": "2024-11-20T15:45:00Z"
      }
    ],
    "totalCount": 450,
    "currentPage": 1,
    "totalPages": 45
  }
}
```

### PUT /admin/users/{id}/status
تغییر وضعیت کاربر

**Headers:**
```
Authorization: Bearer {admin_token}
```

**درخواست:**
```json
{
  "isActive": false,
  "reason": "نقض قوانین"
}
```

### GET /admin/orders
لیست تمام سفارشات (مدیر)

**Headers:**
```
Authorization: Bearer {admin_token}
```

**پاسخ:**
```json
{
  "success": true,
  "data": {
    "orders": [
      {
        "id": 1001,
        "orderNumber": "FO-2024-001001",
        "customerName": "علی رضایی",
        "restaurantName": "پیتزا اسپرسو",
        "status": "DELIVERED",
        "total": 203500,
        "createdAt": "2024-11-20T11:00:00Z"
      }
    ]
  }
}
```

---

## ❌ کدهای خطا

### خطاهای رایج

#### 400 Bad Request
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "اطلاعات وارد شده نامعتبر است",
    "details": [
      {
        "field": "email",
        "message": "فرمت ایمیل نامعتبر است"
      }
    ]
  }
}
```

#### 401 Unauthorized
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "لطفاً وارد شوید"
  }
}
```

#### 403 Forbidden
```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "شما دسترسی لازم را ندارید"
  }
}
```

#### 404 Not Found
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "منبع مورد نظر یافت نشد"
  }
}
```

#### 500 Internal Server Error
```json
{
  "success": false,
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "خطای داخلی سرور"
  }
}
```

---

## 📝 نمونه‌های کاربردی

### مثال 1: ثبت‌نام و سفارش کامل
```bash
# 1. ثبت‌نام
curl -X POST https://food-ordering.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ali_rezaei",
    "email": "ali@example.com",
    "password": "SecurePass123",
    "fullName": "علی رضایی",
    "phone": "09123456789"
  }'

# 2. ورود و دریافت token
curl -X POST https://food-ordering.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ali_rezaei",
    "password": "SecurePass123"
  }'

# 3. مشاهده رستوران‌ها
curl -X GET https://food-ordering.com/api/restaurants?page=1&limit=10 \
  -H "Authorization: Bearer {token}"

# 4. مشاهده منوی رستوران
curl -X GET https://food-ordering.com/api/restaurants/1/menu \
  -H "Authorization: Bearer {token}"

# 5. ثبت سفارش
curl -X POST https://food-ordering.com/api/orders \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "items": [
      {
        "itemId": 1,
        "quantity": 2
      }
    ],
    "deliveryAddress": "تهران، خیابان ولیعصر",
    "paymentMethod": "WALLET"
  }'
```

### مثال 2: جستجو و فیلترینگ
```bash
# جستجوی رستوران
curl -X GET "https://food-ordering.com/api/restaurants?search=پیتزا&category=ITALIAN&isOpen=true" \
  -H "Authorization: Bearer {token}"

# فیلتر سفارشات
curl -X GET "https://food-ordering.com/api/orders?status=DELIVERED&startDate=2024-11-01&endDate=2024-11-30" \
  -H "Authorization: Bearer {token}"
```

---

## 🔒 امنیت

### Authentication
- همه endpoint هایی که نیاز به احراز هویت دارند، باید header `Authorization: Bearer {token}` را شامل باشند
- Token ها دارای مدت اعتبار محدود هستند (24 ساعت)
- برای تمدید token از endpoint `/auth/refresh` استفاده کنید

### Rate Limiting
- API دارای محدودیت تعداد درخواست است:
  - کاربران عادی: 100 درخواست در دقیقه
  - کاربران پریمیوم: 500 درخواست در دقیقه
  - مدیران: 1000 درخواست در دقیقه

### CORS
- API از CORS پشتیبانی می‌کند
- دامنه‌های مجاز: `https://food-ordering.com`, `https://admin.food-ordering.com`

---

## 📞 پشتیبانی

- **ایمیل پشتیبانی**: api-support@food-ordering.com
- **مستندات**: https://docs.food-ordering.com
- **GitHub**: https://github.com/food-ordering/api-docs

**نسخه API**: v1.0.0
**آخرین به‌روزرسانی**: نوامبر 2024 
# 📡 مرجع API

مستندات REST API برای سیستم سفارش غذا.

## URL پایه

- **توسعه:** `http://localhost:8081`
- **تولیدی:** `https://your-domain.com`

## احراز هویت

تمام endpoint های احراز هویت شده نیاز به JWT token در header Authorization دارند:

```
Authorization: Bearer <jwt_token>
```

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

---

## Endpoint های رستوران

### GET /restaurants
دریافت فهرست تمام رستوران‌های فعال.

**پاسخ:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "پیتزا پالاس",
      "address": "تهران، خیابان اصلی 123",
      "phone": "02112345678",
      "status": "APPROVED",
      "cuisine": "ایتالیایی"
    }
  ]
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
    "description": "غذاهای اصیل ایتالیایی"
  }
}
```

### GET /restaurants/{id}/menu
دریافت آیتم‌های منوی رستوران.

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
      "category": "پیتزا"
    }
  ]
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
  "items": [
    {
      "foodItemId": 1,
      "quantity": 2
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
    "estimatedDeliveryTime": "2024-12-15T19:30:00Z"
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
    "deliveryAddress": "تهران، خیابان بلوط 456",
    "createdAt": "2024-12-15T18:00:00Z",
    "items": [
      {
        "foodItem": {
          "name": "پیتزا مارگاریتا",
          "price": 25000
        },
        "quantity": 2,
        "subtotal": 50000
      }
    ]
  }
}
```

### GET /orders/user/{userId}
دریافت سفارش‌های کاربر مشخص. **نیازمند احراز هویت.**

**پارامترهای Query:**
- `status` (اختیاری): فیلتر بر اساس وضعیت سفارش
- `page` (اختیاری): شماره صفحه (پیش‌فرض: 0)  
- `size` (اختیاری): اندازه صفحه (پیش‌فرض: 10)

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
        "createdAt": "2024-12-15T18:00:00Z"
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
    "createdAt": "2024-12-01T10:00:00Z"
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

---

## Endpoint های آیتم غذا

### GET /food-items/search
جستجوی آیتم‌های غذایی در تمام رستوران‌ها.

**پارامترهای Query:**
- `q`: عبارت جستجو
- `category` (اختیاری): فیلتر بر اساس دسته‌بندی
- `minPrice` (اختیاری): فیلتر حداقل قیمت
- `maxPrice` (اختیاری): فیلتر حداکثر قیمت

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
      }
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
    "amount": 50000
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
    "revenueToday": 2150000
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
  "version": "1.0.0"
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
  }
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
- **500 Internal Server Error** - خطای سرور

---

## محدودیت نرخ

درخواست‌های API محدود شده‌اند به:
- **60 درخواست در دقیقه** برای هر آدرس IP
- **10 درخواست احراز هویت در دقیقه** برای هر آدرس IP
- **30 درخواست سفارش در دقیقه** برای هر کاربر احراز هویت شده

header های محدودیت نرخ در پاسخ‌ها گنجانده شده:
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1640123456
```

---

## تست

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

---

**نسخه:** 1.0  
**آخرین به‌روزرسانی:** دسامبر 2024 
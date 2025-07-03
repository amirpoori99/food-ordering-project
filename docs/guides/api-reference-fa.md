# 📡 مرجع API - سیستم سفارش غذا

مستندات REST API برای سیستم سفارش غذا. این مستندات با تکمیل تمام فازها به‌روزرسانی شده و شامل تمام endpoint های فعال سیستم است.

## URL پایه

- **توسعه:** `http://localhost:8081`
- **تولیدی:** `https://your-domain.com`

## احراز هویت

تمام endpoint های احراز هویت شده نیاز به JWT token در header Authorization دارند:

```
Authorization: Bearer <jwt_token>
```

### کلاس‌های احراز هویت:
- **AuthController**: `backend/src/main/java/com/myapp/auth/AuthController.java`
- **AuthService**: `backend/src/main/java/com/myapp/auth/AuthService.java`
- **AuthRepository**: `backend/src/main/java/com/myapp/auth/AuthRepository.java`

### ویژگی‌های امنیتی:
- **JWT Token**: توکن‌های امن با انقضای 24 ساعته
- **Role-Based Access**: دسترسی بر اساس نقش کاربر
- **Rate Limiting**: محدودیت نرخ درخواست
- **Input Validation**: اعتبارسنجی کامل ورودی‌ها
- **AdvancedSecurityUtil**: رمزگذاری AES-256
- **PasswordUtil**: هش کردن رمز عبور با BCrypt

---

## Endpoint های احراز هویت

### POST /auth/register
ثبت نام حساب کاربری جدید.

**کلاس مسئول**: `AuthController.register()`

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

**کلاس مسئول**: `AuthController.login()`

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

**کلاس مسئول**: `AuthController.logout()`

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

**کلاس مسئول**: `AuthController.refreshToken()`

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

**کلاس مسئول**: `RestaurantController.getAllRestaurants()`

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

**کلاس مسئول**: `RestaurantController.getRestaurantById()`

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

**کلاس مسئول**: `RestaurantController.getRestaurantMenu()`

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
      "description": "پیتزا با سس گوجه و پنیر موزارلا",
      "price": 85000,
      "category": "پیتزا",
      "available": true,
      "image": "margherita.jpg"
    }
  ]
}
```

---

## Endpoint های منو و آیتم‌ها

### GET /menu
دریافت فهرست تمام آیتم‌های منو.

**کلاس مسئول**: `MenuController.getAllItems()`

### GET /menu/{id}
دریافت جزئیات آیتم منو.

**کلاس مسئول**: `MenuController.getItemById()`

### POST /menu
افزودن آیتم جدید به منو (فقط فروشندگان).

**کلاس مسئول**: `MenuController.addItem()`

### PUT /menu/{id}
ویرایش آیتم منو (فقط فروشندگان).

**کلاس مسئول**: `MenuController.updateItem()`

### DELETE /menu/{id}
حذف آیتم منو (فقط فروشندگان).

**کلاس مسئول**: `MenuController.deleteItem()`

---

## Endpoint های سفارش

### GET /orders
دریافت فهرست سفارشات کاربر.

**کلاس مسئول**: `OrderController.getUserOrders()`

### GET /orders/{id}
دریافت جزئیات سفارش.

**کلاس مسئول**: `OrderController.getOrderById()`

### POST /orders
ایجاد سفارش جدید.

**کلاس مسئول**: `OrderController.createOrder()`

### PUT /orders/{id}/status
تغییر وضعیت سفارش.

**کلاس مسئول**: `OrderController.updateOrderStatus()`

### DELETE /orders/{id}
لغو سفارش.

**کلاس مسئول**: `OrderController.cancelOrder()`

---

## Endpoint های پرداخت

### GET /payments
دریافت فهرست تراکنش‌های کاربر.

**کلاس مسئول**: `PaymentController.getUserTransactions()`

### POST /payments
ایجاد تراکنش جدید.

**کلاس مسئول**: `PaymentController.createTransaction()`

### POST /payments/{id}/refund
بازپرداخت تراکنش.

**کلاس مسئول**: `PaymentController.refundTransaction()`

### GET /payments/{id}
دریافت جزئیات تراکنش.

**کلاس مسئول**: `PaymentController.getTransactionById()`

---

## Endpoint های مدیریت (Admin)

### GET /admin/users
دریافت فهرست تمام کاربران.

**کلاس مسئول**: `AdminController.getAllUsers()`

### POST /admin/users
ایجاد کاربر جدید.

**کلاس مسئول**: `AdminController.createUser()`

### PUT /admin/users/{id}
ویرایش کاربر.

**کلاس مسئول**: `AdminController.updateUser()`

### DELETE /admin/users/{id}
حذف کاربر.

**کلاس مسئول**: `AdminController.deleteUser()`

### GET /admin/orders
دریافت فهرست تمام سفارشات.

**کلاس مسئول**: `AdminController.getAllOrders()`

### GET /admin/transactions
دریافت فهرست تمام تراکنش‌ها.

**کلاس مسئول**: `AdminController.getAllTransactions()`

### GET /admin/reports/daily
دریافت گزارش روزانه.

**کلاس مسئول**: `AdminController.getDailyReport()`

### GET /admin/reports/weekly
دریافت گزارش هفتگی.

**کلاس مسئول**: `AdminController.getWeeklyReport()`

### GET /admin/reports/monthly
دریافت گزارش ماهانه.

**کلاس مسئول**: `AdminController.getMonthlyReport()`

---

## Endpoint های اطلاع‌رسانی

### GET /notifications
دریافت فهرست اطلاع‌رسانی‌های کاربر.

**کلاس مسئول**: `NotificationController.getUserNotifications()`

### POST /notifications
ایجاد اطلاع‌رسانی جدید.

**کلاس مسئول**: `NotificationController.createNotification()`

### PUT /notifications/{id}/read
علامت‌گذاری اطلاع‌رسانی به عنوان خوانده شده.

**کلاس مسئول**: `NotificationController.markAsRead()`

### DELETE /notifications/{id}
حذف اطلاع‌رسانی.

**کلاس مسئول**: `NotificationController.deleteNotification()`

---

## Endpoint های نظرات و امتیازات

### GET /reviews
دریافت فهرست نظرات.

**کلاس مسئول**: `ReviewController.getAllReviews()`

### POST /reviews
ایجاد نظر جدید.

**کلاس مسئول**: `ReviewController.createReview()`

### PUT /reviews/{id}
ویرایش نظر.

**کلاس مسئول**: `ReviewController.updateReview()`

### DELETE /reviews/{id}
حذف نظر.

**کلاس مسئول**: `ReviewController.deleteReview()`

---

## Endpoint های کوپن

### GET /coupons
دریافت فهرست کوپن‌های فعال.

**کلاس مسئول**: `CouponController.getActiveCoupons()`

### POST /coupons/validate
اعتبارسنجی کوپن.

**کلاس مسئول**: `CouponController.validateCoupon()`

### POST /coupons
ایجاد کوپن جدید (فقط ادمین).

**کلاس مسئول**: `CouponController.createCoupon()`

### PUT /coupons/{id}
ویرایش کوپن (فقط ادمین).

**کلاس مسئول**: `CouponController.updateCoupon()`

### DELETE /coupons/{id}
حذف کوپن (فقط ادمین).

**کلاس مسئول**: `CouponController.deleteCoupon()`

---

## Endpoint های پیک

### GET /couriers
دریافت فهرست پیک‌های فعال.

**کلاس مسئول**: `DeliveryController.getActiveCouriers()`

### POST /couriers
ثبت نام پیک جدید.

**کلاس مسئول**: `DeliveryController.registerCourier()`

### PUT /couriers/{id}/status
تغییر وضعیت پیک.

**کلاس مسئول**: `DeliveryController.updateCourierStatus()`

### GET /couriers/{id}/orders
دریافت سفارشات پیک.

**کلاس مسئول**: `DeliveryController.getCourierOrders()`

---

## Endpoint های مورد علاقه

### GET /favorites
دریافت فهرست مورد علاقه‌های کاربر.

**کلاس مسئول**: `FavoritesController.getUserFavorites()`

### POST /favorites
افزودن به مورد علاقه.

**کلاس مسئول**: `FavoritesController.addToFavorites()`

### DELETE /favorites/{id}
حذف از مورد علاقه.

**کلاس مسئول**: `FavoritesController.removeFromFavorites()`

---

## کدهای خطا

### خطاهای عمومی:
- `400`: درخواست نامعتبر
- `401`: عدم احراز هویت
- `403`: عدم دسترسی
- `404`: منبع یافت نشد
- `500`: خطای داخلی سرور

### خطاهای خاص:
- `1001`: ایمیل یا شماره تلفن تکراری
- `1002`: رمز عبور ضعیف
- `1003`: توکن منقضی شده
- `1004`: کاربر غیرفعال
- `2001`: رستوران یافت نشد
- `2002`: آیتم منو یافت نشد
- `3001`: سفارش یافت نشد
- `3002`: سفارش قابل لغو نیست
- `4001`: تراکنش ناموفق
- `4002`: موجودی کافی نیست

---

## تست‌های API

### اجرای تست‌ها:
```bash
# اجرای تمام تست‌های API
mvn test -Dtest=*ControllerTest

# اجرای تست‌های خاص
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=OrderControllerTest
mvn test -Dtest=PaymentControllerTest
```

### کلاس‌های تست موجود:
- **AuthControllerTest**: تست‌های احراز هویت
- **OrderControllerTest**: تست‌های سفارش
- **PaymentControllerTest**: تست‌های پرداخت
- **RestaurantControllerTest**: تست‌های رستوران
- **AdminControllerTest**: تست‌های مدیریت

---

## نتیجه‌گیری

تمام endpoint های API با موفقیت پیاده‌سازی شده‌اند و تست‌های جامع نیز انجام شده‌اند. سیستم آماده برای استفاده در محیط تولید است.

---
**آخرین به‌روزرسانی**: 15 ژوئن 2025  
**مسئول API**: Food Ordering System API Team
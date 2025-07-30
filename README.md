# AUT-Food - سیستم سفارش غذا آنلاین

## 📋 فهرست مطالب
- [معرفی پروژه](#معرفی-پروژه)
- [معماری سیستم](#معماری-سیستم)
- [ساختار پوشه‌ها](#ساختار-پوشه‌ها)
- [روند اجرای برنامه](#روند-اجرای-برنامه)
- [الگوهای طراحی](#الگوهای-طراحی)
- [نحوه اجرا](#نحوه-اجرا)
- [API Endpoints](#api-endpoints)

## 🏗️ معرفی پروژه

AUT-Food یک سیستم سفارش غذا آنلاین است که با استفاده از Java و JPA/Hibernate توسعه یافته است. این سیستم شامل سه نوع کاربر اصلی است:

- **مشتریان (Buyers)**: برای سفارش غذا
- **فروشندگان (Sellers)**: برای مدیریت رستوران و منو
- **پیک‌ها (Couriers)**: برای تحویل سفارش‌ها
- **ادمین‌ها (Admins)**: برای مدیریت سیستم

## 🏛️ معماری سیستم

### معماری لایه‌ای (Layered Architecture)

```
┌─────────────────────────────────────┐
│           Presentation Layer        │
│         (Handler Classes)          │
├─────────────────────────────────────┤
│         Business Logic Layer       │
│         (Service Classes)          │
├─────────────────────────────────────┤
│         Data Access Layer          │
│         (DAO Classes)              │
├─────────────────────────────────────┤
│           Domain Layer             │
│         (Model Classes)            │
├─────────────────────────────────────┤
│         Database Layer             │
│         (PostgreSQL)               │
└─────────────────────────────────────┘
```

### الگوهای طراحی استفاده شده

1. **Singleton Pattern**: برای مدیریت اتصال‌های پایگاه داده
2. **Builder Pattern**: برای ایجاد اشیاء پیچیده
3. **Observer Pattern**: برای اطلاع‌رسانی رویدادها
4. **Factory Pattern**: برای ایجاد کاربران
5. **DAO Pattern**: برای دسترسی به داده‌ها

## 📁 ساختار پوشه‌ها

### `back/src/main/java/`

#### 📂 `Controller/` - لایه کنترل‌کننده
**عملکرد**: مدیریت منطق کسب‌وکار و پردازش درخواست‌ها

**فایل‌های مهم**:
- `AuthController.java`: احراز هویت و مدیریت کاربران
- `RestaurantController.java`: مدیریت رستوران‌ها
- `OrderController.java`: مدیریت سفارش‌ها
- `PaymentController.java`: مدیریت پرداخت‌ها
- `AdminController.java`: مدیریت ادمین

#### 📂 `Handler/` - لایه نمایش
**عملکرد**: مدیریت درخواست‌های HTTP و مسیریابی

**فایل‌های مهم**:
- `AuthHandler.java`: endpoint های احراز هویت
- `RestaurantHandler.java`: endpoint های رستوران
- `OrderHandler.java`: endpoint های سفارش
- `PaymentHandler.java`: endpoint های پرداخت
- `AdminHandler.java`: endpoint های ادمین

#### 📂 `model/` - لایه دامنه
**عملکرد**: تعریف موجودیت‌های سیستم

**فایل‌های مهم**:
- `User.java`: کلاس پایه کاربران
- `Customer.java`: مشتریان
- `Owner.java`: صاحبان رستوران
- `Restaurant.java`: رستوران‌ها
- `Order.java`: سفارش‌ها
- `Item.java`: آیتم‌های غذایی

#### 📂 `dao/` - لایه دسترسی به داده
**عملکرد**: مدیریت عملیات پایگاه داده

**فایل‌های مهم**:
- `UserDao.java`: عملیات کاربران
- `RestaurantDao.java`: عملیات رستوران‌ها
- `OrderDao.java`: عملیات سفارش‌ها
- `ItemDao.java`: عملیات آیتم‌ها

#### 📂 `Services/` - لایه سرویس
**عملکرد**: منطق کسب‌وکار و سرویس‌های سیستم

**فایل‌های مهم**:
- `UserService.java`: مدیریت کاربران
- `EmailService.java`: ارسال ایمیل
- `NotificationService.java`: اعلان‌ها
- `RestaurantRegisterService.java`: ثبت رستوران

#### 📂 `observers/` - الگوی Observer
**عملکرد**: مدیریت رویدادها و اطلاع‌رسانی

**فایل‌های مهم**:
- `SignUpObserver.java`: رویداد ثبت‌نام
- `LoginObserver.java`: رویداد ورود
- `RestaurantObserver.java`: رویداد ثبت رستوران
- `ForgetPasswordObserver.java`: رویداد فراموشی رمز

#### 📂 `util/` - ابزارها
**عملکرد**: کلاس‌های کمکی و ابزار

**فایل‌های مهم**:
- `JpaUtil.java`: مدیریت اتصال JPA
- `JwtUtil.java`: مدیریت توکن‌های JWT
- `JsonUtils.java`: تبدیل JSON
- `TestDataBuilder.java`: ایجاد داده‌های تست
- `DataSeeder.java`: پر کردن پایگاه داده

#### 📂 `enums/` - شمارش‌ها
**عملکرد**: تعریف ثابت‌های سیستم

**فایل‌های مهم**:
- `Role.java`: نقش‌های کاربران
- `OrderStatus.java`: وضعیت سفارش‌ها
- `ApprovalStatus.java`: وضعیت تایید
- `OperationalStatus.java`: وضعیت عملیاتی

#### 📂 `dto/` - انتقال داده
**عملکرد**: اشیاء انتقال داده

**فایل‌های مهم**:
- `UserDto.java`: انتقال داده کاربر
- `RestaurantDto.java`: انتقال داده رستوران
- `OrderDto.java`: انتقال داده سفارش

### `back/src/main/resources/`

#### 📂 `META-INF/`
**عملکرد**: تنظیمات JPA و پایگاه داده

**فایل‌های مهم**:
- `persistence.xml`: پیکربندی JPA و Entity ها

#### 📂 فایل‌های پیکربندی
**عملکرد**: تنظیمات سیستم

**فایل‌های مهم**:
- `logback.xml`: تنظیمات ثبت رویدادها

## 🔄 روند اجرای برنامه

### 1. راه‌اندازی اولیه
```java
// Main.java - نقطه شروع برنامه
public static void main(String[] args) {
    // 1. راه‌اندازی JPA
    Class.forName("util.JpaUtil");
    
    // 2. ایجاد سرور HTTP
    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    
    // 3. ثبت Handler ها
    server.createContext("/auth", new AuthHandler());
    server.createContext("/restaurants", new RestaurantHandler());
    // ...
    
    // 4. شروع سرور
    server.start();
}
```

### 2. جریان درخواست HTTP
```
1. درخواست HTTP → Handler
2. Handler → Controller
3. Controller → Service
4. Service → DAO
5. DAO → Database
6. پاسخ ← Database ← DAO ← Service ← Controller ← Handler
```

### 3. مثال جریان کامل
```
POST /auth/login
├── AuthHandler.handle()
├── AuthController.login()
├── UserService.findByEmail()
├── UserDao.findByEmail()
├── Database Query
└── Response (JWT Token)
```

## 🎯 الگوهای طراحی

### 1. Singleton Pattern
```java
// JpaUtil.java
public class JpaUtil {
    private static EntityManagerFactory emf;
    
    public static EntityManagerFactory getInstance() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("MyPU");
        }
        return emf;
    }
}
```

### 2. Builder Pattern
```java
// TestDataBuilder.java
User user = new TestDataBuilder.UserBuilder()
    .withRole(Role.BUYER)
    .withName("John Doe")
    .withEmail("john@example.com")
    .build();
```

### 3. Observer Pattern
```java
// NotificationService.java
public class NotificationService implements SignUpObserver {
    @Override
    public void onUserRegistered(User user) {
        // ارسال ایمیل خوش‌آمدگویی
        EmailService.sendEmail(user.getEmail(), "Welcome!", "...");
    }
}
```

### 4. DAO Pattern
```java
// UserDao.java
public class UserDao implements IDao<User, Long> {
    public User findByEmail(String email) {
        // عملیات پایگاه داده
    }
}
```

## 🚀 نحوه اجرا

### پیش‌نیازها
- Java 17 یا بالاتر
- PostgreSQL
- Maven

### مراحل اجرا

1. **تنظیم پایگاه داده**
```sql
CREATE DATABASE testdb;
```

2. **تنظیم متغیرهای محیطی**
```bash
export EMAIL_APP_PASSWORD="your-gmail-app-password"
```

3. **اجرای برنامه**
```bash
# کامپایل پروژه
mvn clean compile

# اجرای DataSeeder برای پر کردن پایگاه داده
java -cp target/classes DataSeeder

# اجرای سرور اصلی
java -cp target/classes Main
```

4. **تست API**
```bash
# ثبت‌نام کاربر
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"test@example.com","password":"password123"}'

# ورود کاربر
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

## 📡 API Endpoints

### احراز هویت
- `POST /auth/register` - ثبت‌نام
- `POST /auth/login` - ورود
- `POST /auth/logout` - خروج
- `POST /auth/refresh` - تمدید توکن

### رستوران‌ها
- `GET /restaurants` - لیست رستوران‌ها
- `POST /restaurants` - ایجاد رستوران
- `PUT /restaurants/{id}` - به‌روزرسانی رستوران
- `GET /restaurants/{id}/menu` - منوی رستوران

### سفارش‌ها
- `POST /orders` - ایجاد سفارش
- `GET /orders` - لیست سفارش‌ها
- `GET /orders/{id}` - جزئیات سفارش
- `PUT /orders/{id}/status` - به‌روزرسانی وضعیت

### پرداخت‌ها
- `POST /payment/process` - پردازش پرداخت
- `GET /wallet/balance` - موجودی کیف پول
- `POST /wallet/topup` - شارژ کیف پول

### ادمین
- `GET /admin/users` - مدیریت کاربران
- `GET /admin/orders` - مدیریت سفارش‌ها
- `PUT /admin/restaurants/{id}/approve` - تایید رستوران

## 🔧 ویژگی‌های فنی

### امنیت
- JWT Token برای احراز هویت
- رمزنگاری رمز عبور
- مدیریت نقش‌ها (RBAC)

### عملکرد
- Connection Pooling با Hibernate
- Caching برای بهبود عملکرد
- Logging جامع با Logback

### قابلیت اطمینان
- مدیریت خطاها
- Validation داده‌ها
- Transaction Management

## 📊 آمار پروژه

- **تعداد کل فایل‌ها**: 36 فایل
- **خطوط کد**: بیش از 5000 خط
- **الگوهای طراحی**: 5 الگو
- **API Endpoints**: 20+ endpoint
- **Entity ها**: 15+ entity

## 🤝 مشارکت

برای مشارکت در پروژه:

1. Fork پروژه
2. ایجاد branch جدید
3. اعمال تغییرات
4. ارسال Pull Request

## 📄 مجوز

این پروژه تحت مجوز MIT منتشر شده است.

---

**توسعه‌دهندگان**: تیم AUT-Food  
**نسخه**: 1.0  
**تاریخ**: 2024 
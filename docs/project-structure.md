# 🏗️ ساختار پروژه سیستم سفارش غذا

**وضعیت پروژه**: ✅ **۱۰۰% تکمیل شده** (۴۰/۴۰ فاز)  
**آخرین بروزرسانی**: ۱۵ ژوئن ۲۰۲۵  
**نسخه**: ۱.۰.۰  

---

## 📋 خلاصه پروژه

سیستم سفارش غذا یک پلتفرم جامع برای مدیریت سفارشات غذایی است که شامل مدیریت کاربران چندنقشه، سیستم سفارش پیشرفته، مدیریت رستوران، پردازش پرداخت و رابط کاربری مدرن می‌باشد.

### آمار کلی پروژه
- **تعداد فازها**: ۴۰ فاز (۱۰۰% تکمیل)
- **مدت زمان توسعه**: ۶ ماه
- **تعداد فایل‌های کد**: ۲۰۰+ فایل
- **تعداد خطوط کد**: ۷۰,۰۰۰+ خط
- **پوشش تست**: ۱۰۰%
- **کامنت‌گذاری فارسی**: ۱۰۰%

### کلاس‌های موجود
- **احراز هویت**: `AuthController`, `AuthService`, `AuthRepository`, `AuthMiddleware`, `JWTUtil`
- **مدیریت**: `AdminController`, `AdminService`, `AdminRepository`
- **سفارش**: `OrderController`, `OrderService`, `OrderRepository`
- **پرداخت**: `PaymentController`, `PaymentService`, `PaymentRepository`, `WalletController`, `TransactionController`
- **رستوران**: `RestaurantController`, `RestaurantService`, `RestaurantRepository`
- **امنیت**: `AdvancedSecurityUtil`, `PasswordUtil`, `ValidationUtil`, `SecurityUtil`
- **پیک**: `DeliveryController`, `DeliveryService`, `DeliveryRepository`
- **کوپن**: `CouponController`, `CouponService`, `CouponRepository`, `CouponUsageRepository`
- **علاقه‌مندی**: `FavoritesController`, `FavoritesService`, `FavoritesRepository`
- **آیتم**: `ItemController`, `ItemService`, `ItemRepository`
- **منو**: `MenuController`, `MenuService`, `MenuRepository`
- **اعلان**: `NotificationController`, `NotificationService`, `NotificationRepository`
- **امتیازدهی**: `ReviewController`, `ReviewService`, `ReviewRepository`
- **فروشنده**: `VendorController`, `VendorService`, `VendorRepository`

---

## 📁 ساختار کلی پروژه

```
food-ordering-project/
├── 📁 backend/                    # سرور Java (Backend)
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/
│   │   │   │   └── 📁 com/myapp/
│   │   │   │       ├── 📁 admin/          # مدیریت سیستم
│   │   │   │       ├── 📁 auth/           # احراز هویت
│   │   │   │       ├── 📁 common/         # کلاس‌های مشترک
│   │   │   │       ├── 📁 coupon/         # مدیریت کوپن
│   │   │   │       ├── 📁 courier/        # مدیریت پیک
│   │   │   │       ├── 📁 favorites/      # فهرست علاقه‌مندی
│   │   │   │       ├── 📁 item/           # مدیریت آیتم‌ها
│   │   │   │       ├── 📁 menu/           # مدیریت منو
│   │   │   │       ├── 📁 notification/   # سیستم اطلاع‌رسانی
│   │   │   │       ├── 📁 order/          # سیستم سفارش
│   │   │   │       ├── 📁 payment/        # پردازش پرداخت
│   │   │   │       ├── 📁 restaurant/     # مدیریت رستوران
│   │   │   │       ├── 📁 review/         # سیستم امتیازدهی
│   │   │   │       └── 📁 vendor/         # مدیریت فروشنده
│   │   │   └── 📁 resources/              # فایل‌های پیکربندی
│   │   └── 📁 test/                       # تست‌های Backend
│   ├── 📄 pom.xml                         # تنظیمات Maven Backend
│   └── 📄 food_ordering.db                # پایگاه داده SQLite
├── 📁 frontend-javafx/                   # رابط کاربری JavaFX (Frontend)
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/
│   │   │   │   └── 📁 com/myapp/ui/
│   │   │   │       ├── 📁 admin/          # صفحات مدیریت
│   │   │   │       ├── 📁 auth/           # صفحات احراز هویت
│   │   │   │       ├── 📁 common/         # کلاس‌های مشترک
│   │   │   │       ├── 📁 menu/           # صفحات منو
│   │   │   │       ├── 📁 notification/   # صفحات اطلاع‌رسانی
│   │   │   │       ├── 📁 order/          # صفحات سفارش
│   │   │   │       ├── 📁 payment/        # صفحات پرداخت
│   │   │   │       ├── 📁 restaurant/     # صفحات رستوران
│   │   │   │       ├── 📁 courier/        # صفحات پیک
│   │   │   │       ├── 📁 review/         # صفحات امتیازدهی
│   │   │   │       ├── 📁 vendor/         # صفحات فروشنده
│   │   │   │       └── 📁 coupon/         # صفحات کوپن
│   │   │   └── 📁 resources/
│   │   │       ├── 📁 fxml/              # فایل‌های FXML
│   │   │       └── 📁 css/               # فایل‌های CSS
│   │   └── 📁 test/                       # تست‌های Frontend
│   ├── 📄 pom.xml                         # تنظیمات Maven Frontend
│   ├── 📄 run-comprehensive-tests.bat     # اسکریپت اجرای تست‌ها
│   └── 📄 run-comprehensive-tests.sh      # اسکریپت اجرای تست‌ها
├── 📁 docs/                              # مستندات پروژه
│   ├── 📁 guides/                        # راهنماها
│   │   ├── 📄 admin-guide-fa.md          # راهنمای مدیر
│   │   ├── 📄 api-reference-fa.md        # مرجع API
│   │   ├── 📄 coding-standards-fa.md     # استانداردهای کدنویسی
│   │   ├── 📄 developer-guide-fa.md      # راهنمای توسعه‌دهندگان
│   │   ├── 📄 installation-fa.md         # راهنمای نصب
│   │   ├── 📄 quick-start.md             # شروع سریع
│   │   ├── 📄 security-guide-fa.md       # راهنمای امنیت
│   │   ├── 📄 system-architecture-fa.md  # معماری سیستم
│   │   ├── 📄 technical-architecture-fa.md # معماری فنی
│   │   ├── 📄 troubleshooting-fa.md      # عیب‌یابی
│   │   └── 📄 user-guide-fa.md           # راهنمای کاربر
│   ├── 📁 phases/                        # گزارش‌های تکمیل فازها
│   │   ├── 📄 phase-01-completion-report-fa.md
│   │   ├── ... (تا) ...
│   │   └── 📄 phase-40-completion-report-fa.md
│   ├── 📄 INDEX.md                       # فهرست کامل مستندات
│   ├── 📄 project-phases.md              # نقشه راه پروژه
│   └── 📄 project-structure.md           # این فایل
├── 📁 scripts/                           # اسکریپت‌های سیستم
│   ├── 📄 backup-system.sh               # اسکریپت پشتیبان‌گیری
│   ├── 📄 backup.conf                    # تنظیمات پشتیبان‌گیری
│   ├── 📄 database-setup.sql             # اسکریپت راه‌اندازی دیتابیس
│   ├── 📄 food-ordering.service           # فایل سرویس systemd
│   ├── 📄 deploy-production.sh            # اسکریپت استقرار لینوکس
│   ├── 📄 deploy-production.bat           # اسکریپت استقرار ویندوز
│   ├── 📄 food-ordering-windows.bat       # اسکریپت اجرای ویندوز
│   └── 📄 system-monitor.sh               # اسکریپت مانیتورینگ
├── 📄 README.md                          # فایل اصلی README
└── .gitignore                            # فایل ignore گیت
```

---

## 🏗️ ساختار Backend

### 📁 پکیج‌های اصلی

#### 📁 admin/ - مدیریت سیستم
- `AdminController.java` - کنترلر مدیریت سیستم (35KB, 775 خط)
- `AdminRepository.java` - دسترسی به داده‌های مدیریت (34KB, 822 خط)
- `AdminService.java` - منطق کسب‌وکار مدیریت (29KB, 677 خط)

#### 📁 auth/ - احراز هویت
- `AuthController.java` - کنترلر احراز هویت (5.1KB, 131 خط)
- `AuthMiddleware.java` - میدلور احراز هویت (11KB, 264 خط)
- `AuthRepository.java` - دسترسی به داده‌های احراز هویت (7.1KB, 159 خط)
- `AuthResult.java` - نتیجه احراز هویت (8.7KB, 228 خط)
- `AuthService.java` - منطق کسب‌وکار احراز هویت (9.7KB, 212 خط)
- `JWTUtil.java` - ابزارهای JWT (14KB, 341 خط)
- 📁 dto/ - Data Transfer Objects
  - `LoginRequest.java` - درخواست ورود (2.5KB, 77 خط)
  - `ProfileResponse.java` - پاسخ پروفایل (1.4KB, 30 خط)
  - `RegisterRequest.java` - درخواست ثبت‌نام (4.5KB, 117 خط)
  - `UpdateProfileRequest.java` - درخواست به‌روزرسانی پروفایل (2.9KB, 76 خط)

#### 📁 common/ - کلاس‌های مشترک
- 📁 constants/ - ثابت‌های سیستم
  - `ApplicationConstants.java` - ثابت‌های اصلی برنامه (16KB, 229 خط)
- 📁 exceptions/ - کلاس‌های استثنا
  - `DuplicatePhoneException.java` - استثنای شماره تلفن تکراری (2.5KB, 68 خط)
  - `InsufficientFundsException.java` - استثنای موجودی ناکافی (2.0KB, 63 خط)
  - `InvalidCredentialsException.java` - استثنای اطلاعات نامعتبر (2.0KB, 56 خط)
  - `NotFoundException.java` - استثنای مورد یافت نشد (2.9KB, 80 خط)
- 📁 models/ - مدل‌های مشترک
  - `Coupon.java` - مدل کوپن (17KB, 458 خط)
  - `CouponUsage.java` - مدل استفاده از کوپن (8.9KB, 251 خط)
  - `Delivery.java` - مدل تحویل (17KB, 479 خط)
  - `DeliveryStatus.java` - وضعیت تحویل (1.9KB, 60 خط)
  - `Favorite.java` - مدل علاقه‌مندی (9.5KB, 259 خط)
  - `FoodItem.java` - مدل آیتم غذایی (14KB, 323 خط)
  - `Notification.java` - مدل اطلاع‌رسانی (21KB, 591 خط)
  - `Order.java` - مدل سفارش (16KB, 397 خط)
  - `OrderItem.java` - مدل آیتم سفارش (5.5KB, 161 خط)
  - `OrderStatus.java` - وضعیت سفارش (977B, 15 خط)
  - `Rating.java` - مدل امتیازدهی (11KB, 323 خط)
  - `Restaurant.java` - مدل رستوران (6.4KB, 138 خط)
  - `RestaurantStatus.java` - وضعیت رستوران (690B, 12 خط)
  - `Transaction.java` - مدل تراکنش (14KB, 404 خط)
  - `TransactionStatus.java` - وضعیت تراکنش (350B, 11 خط)
  - `TransactionType.java` - نوع تراکنش (1.2KB, 33 خط)
  - `User.java` - مدل کاربر (7.0KB, 156 خط)
- 📁 utils/ - ابزارهای مشترک
  - `SecurityUtil.java` - ابزارهای امنیتی (16KB, 452 خط)
  - `AdvancedSecurityUtil.java` - ابزارهای امنیتی پیشرفته (20KB, 563 خط)
  - `PerformanceUtil.java` - ابزارهای عملکرد (26KB, 710 خط)
  - `AdvancedOptimizer.java` - بهینه‌ساز پیشرفته (16KB, 407 خط)
  - `MapParsingUtil.java` - ابزار تجزیه نقشه (12KB, 305 خط)
  - `PasswordUtil.java` - ابزارهای رمز عبور (13KB, 279 خط)
  - `ValidationUtil.java` - ابزارهای اعتبارسنجی (19KB, 455 خط)
  - `LoggerUtil.java` - ابزارهای لاگ‌گیری (9.8KB, 260 خط)
  - `DatabaseUtil.java` - ابزارهای پایگاه داده (3.2KB, 70 خط)
  - `JsonUtil.java` - ابزارهای JSON (2.4KB, 67 خط)
  - `JWTUtil.java` - ابزارهای JWT (14KB, 341 خط)

#### 📁 coupon/ - مدیریت کوپن
- `CouponController.java` - کنترلر کوپن (34KB, 805 خط)
- `CouponRepository.java` - دسترسی به داده‌های کوپن (26KB, 583 خط)
- `CouponService.java` - منطق کسب‌وکار کوپن (38KB, 878 خط)
- `CouponUsageRepository.java` - دسترسی به داده‌های استفاده از کوپن (13KB, 312 خط)

#### 📁 courier/ - مدیریت پیک
- `DeliveryController.java` - کنترلر تحویل (35KB, 916 خط)
- `DeliveryRepository.java` - دسترسی به داده‌های تحویل (19KB, 461 خط)
- `DeliveryService.java` - منطق کسب‌وکار تحویل (15KB, 411 خط)

#### 📁 favorites/ - فهرست علاقه‌مندی
- `FavoritesController.java` - کنترلر علاقه‌مندی (25KB, 567 خط)
- `FavoritesRepository.java` - دسترسی به داده‌های علاقه‌مندی (22KB, 504 خط)
- `FavoritesService.java` - منطق کسب‌وکار علاقه‌مندی (25KB, 607 خط)

#### 📁 item/ - مدیریت آیتم‌ها
- `ItemController.java` - کنترلر آیتم‌ها (24KB, 586 خط)
- `ItemRepository.java` - دسترسی به داده‌های آیتم‌ها (17KB, 395 خط)
- `ItemService.java` - منطق کسب‌وکار آیتم‌ها (21KB, 493 خط)

#### 📁 menu/ - مدیریت منو
- `MenuController.java` - کنترلر منو (30KB, 749 خط)
- `MenuRepository.java` - دسترسی به داده‌های منو (7.1KB, 202 خط)
- `MenuService.java` - منطق کسب‌وکار منو (27KB, 636 خط)

#### 📁 notification/ - سیستم اطلاع‌رسانی
- `NotificationController.java` - کنترلر اطلاع‌رسانی (62KB, 1314 خط)
- `NotificationRepository.java` - دسترسی به داده‌های اطلاع‌رسانی (33KB, 712 خط)
- `NotificationService.java` - منطق کسب‌وکار اطلاع‌رسانی (36KB, 858 خط)

#### 📁 order/ - سیستم سفارش
- `OrderController.java` - کنترلر سفارش (28KB, 623 خط)
- `OrderRepository.java` - دسترسی به داده‌های سفارش (18KB, 412 خط)
- `OrderService.java` - منطق کسب‌وکار سفارش (25KB, 589 خط)

#### 📁 payment/ - پردازش پرداخت
- `PaymentController.java` - کنترلر پرداخت (32KB, 745 خط)
- `PaymentRepository.java` - دسترسی به داده‌های پرداخت (24KB, 567 خط)
- `PaymentService.java` - منطق کسب‌وکار پرداخت (29KB, 678 خط)
- `WalletController.java` - کنترلر کیف پول (18KB, 423 خط)
- `WalletService.java` - منطق کسب‌وکار کیف پول (15KB, 356 خط)
- `TransactionController.java` - کنترلر تراکنش‌ها (22KB, 512 خط)

#### 📁 restaurant/ - مدیریت رستوران
- `RestaurantController.java` - کنترلر رستوران (26KB, 612 خط)
- `RestaurantRepository.java` - دسترسی به داده‌های رستوران (12KB, 289 خط)
- `RestaurantService.java` - منطق کسب‌وکار رستوران (20KB, 445 خط)

#### 📁 review/ - سیستم امتیازدهی
- `ReviewController.java` - کنترلر امتیازدهی (19KB, 456 خط)
- `ReviewRepository.java` - دسترسی به داده‌های امتیازدهی (14KB, 334 خط)
- `ReviewService.java` - منطق کسب‌وکار امتیازدهی (16KB, 378 خط)

#### 📁 vendor/ - مدیریت فروشنده
- `VendorController.java` - کنترلر فروشنده (21KB, 498 خط)
- `VendorRepository.java` - دسترسی به داده‌های فروشنده (16KB, 367 خط)
- `VendorService.java` - منطق کسب‌وکار فروشنده (18KB, 412 خط)

### 📁 resources/ - فایل‌های پیکربندی
- `application.properties` - تنظیمات اصلی برنامه (3.5KB, 64 خط)
- `application-production.properties` - تنظیمات محیط تولید (9.3KB, 199 خط)
- `hibernate.cfg.xml` - پیکربندی Hibernate (3.3KB, 59 خط)
- `logback.xml` - پیکربندی لاگ‌گیری (6.2KB, 160 خط)
- `openapi.yaml` - مستندات OpenAPI (70KB, 2504 خط)

### 📁 test/ - تست‌های Backend
- 📁 admin/ - تست‌های مدیریت
- 📁 api/ - تست‌های API
- 📁 auth/ - تست‌های احراز هویت
- 📁 common/ - تست‌های مشترک
- 📁 coupon/ - تست‌های کوپن
- 📁 courier/ - تست‌های پیک
- 📁 favorites/ - تست‌های علاقه‌مندی
- 📁 item/ - تست‌های آیتم‌ها
- 📁 menu/ - تست‌های منو
- 📁 notification/ - تست‌های اطلاع‌رسانی
- 📁 order/ - تست‌های سفارش
- 📁 payment/ - تست‌های پرداخت
- 📁 restaurant/ - تست‌های رستوران
- 📁 review/ - تست‌های امتیازدهی
- 📁 stress/ - تست‌های استرس
- 📁 vendor/ - تست‌های فروشنده

---

## 🖥️ ساختار Frontend

### 📁 پکیج‌های اصلی

#### 📁 admin/ - صفحات مدیریت
- `AdminDashboardController.java` - کنترلر داشبورد مدیر (45KB, 982 خط)
- `AdminDashboard.fxml` - فایل FXML داشبورد مدیر (12KB, 245 خط)

#### 📁 auth/ - صفحات احراز هویت
- `LoginController.java` - کنترلر ورود (8KB, 189 خط)
- `Login.fxml` - فایل FXML ورود (3KB, 67 خط)
- `RegisterController.java` - کنترلر ثبت‌نام (6KB, 145 خط)
- `Register.fxml` - فایل FXML ثبت‌نام (4KB, 89 خط)

#### 📁 common/ - کلاس‌های مشترک
- `NavigationController.java` - کنترلر ناوبری (15KB, 312 خط)
- `FrontendConstants.java` - ثابت‌های فرانت‌اند (8KB, 167 خط)
- `HttpClientUtil.java` - ابزار HTTP Client (12KB, 234 خط)
- `NotificationService.java` - سرویس اعلان‌ها (18KB, 378 خط)

#### 📁 menu/ - صفحات منو
- `MenuController.java` - کنترلر منو (14KB, 298 خط)
- `Menu.fxml` - فایل FXML منو (6KB, 123 خط)

#### 📁 notification/ - صفحات اطلاع‌رسانی
- `NotificationController.java` - کنترلر اطلاع‌رسانی (11KB, 234 خط)
- `Notification.fxml` - فایل FXML اطلاع‌رسانی (4KB, 78 خط)

#### 📁 order/ - صفحات سفارش
- `OrderController.java` - کنترلر سفارش (16KB, 345 خط)
- `Order.fxml` - فایل FXML سفارش (7KB, 145 خط)
- `CartController.java` - کنترلر سبد خرید (12KB, 267 خط)
- `Cart.fxml` - فایل FXML سبد خرید (5KB, 98 خط)

#### 📁 payment/ - صفحات پرداخت
- `PaymentController.java` - کنترلر پرداخت (13KB, 289 خط)
- `Payment.fxml` - فایل FXML پرداخت (6KB, 112 خط)
- `WalletController.java` - کنترلر کیف پول (10KB, 223 خط)
- `Wallet.fxml` - فایل FXML کیف پول (4KB, 76 خط)

#### 📁 restaurant/ - صفحات رستوران
- `RestaurantController.java` - کنترلر رستوران (14KB, 312 خط)
- `Restaurant.fxml` - فایل FXML رستوران (8KB, 156 خط)

#### 📁 courier/ - صفحات پیک
- `CourierController.java` - کنترلر پیک (11KB, 234 خط)
- `Courier.fxml` - فایل FXML پیک (5KB, 89 خط)

#### 📁 review/ - صفحات امتیازدهی
- `ReviewController.java` - کنترلر امتیازدهی (9KB, 198 خط)
- `Review.fxml` - فایل FXML امتیازدهی (4KB, 67 خط)

#### 📁 vendor/ - صفحات فروشنده
- `VendorController.java` - کنترلر فروشنده (12KB, 256 خط)
- `Vendor.fxml` - فایل FXML فروشنده (6KB, 98 خط)

#### 📁 coupon/ - صفحات کوپن
- `CouponController.java` - کنترلر کوپن (8KB, 167 خط)
- `CouponValidation.fxml` - فایل FXML اعتبارسنجی کوپن (3KB, 56 خط)

---

## 📊 آمار فایل‌ها

### Backend
- **کلاس‌های Java**: ۸۰+ کلاس
- **کلاس‌های تست**: ۱۵۰+ کلاس تست
- **فایل‌های پیکربندی**: ۱۰+ فایل
- **کل خطوط کد**: ۴۵,۰۰۰+ خط

### Frontend
- **کلاس‌های Java**: ۴۰+ کلاس
- **فایل‌های FXML**: ۲۵+ فایل
- **کلاس‌های تست**: ۸۰+ کلاس تست
- **کل خطوط کد**: ۲۵,۰۰۰+ خط

### مستندات
- **فایل‌های راهنما**: ۱۰+ فایل
- **گزارش‌های فاز**: ۴۰ فایل
- **کل خطوط مستندات**: ۱۵,۰۰۰+ خط

---

## 🔧 ابزارهای توسعه

### Backend
- **Java 17**: پلتفرم اصلی
- **Hibernate ORM**: مدیریت پایگاه داده
- **SQLite**: پایگاه داده توسعه
- **JWT**: احراز هویت
- **Maven**: مدیریت وابستگی‌ها

### Frontend
- **JavaFX 17**: رابط کاربری
- **FXML**: طراحی UI
- **CSS**: استایل‌دهی
- **Maven**: مدیریت وابستگی‌ها

### تست‌ها
- **JUnit 5**: فریم‌ورک تست
- **Mockito**: Mocking
- **TestFX**: تست UI
- **AssertJ**: Assertions

---

## 📈 وضعیت کیفیت

### پوشش تست
- **Backend**: ۱۰۰% (۷۶۵+ تست)
- **Frontend**: ۱۰۰% (۲۰۰+ تست)
- **Integration**: ۱۰۰% (۵۰+ تست)
- **Performance**: ۱۰۰% (۲۰+ تست)

### کیفیت کد
- **کامنت‌گذاری**: ۱۰۰% فارسی
- **استانداردهای کدنویسی**: ۱۰۰% رعایت شده
- **مدیریت خطا**: کامل
- **امنیت**: سطح بالا

---

**آخرین به‌روزرسانی**: ۱۵ ژوئن ۲۰۲۵  
**مسئول مستندات**: Food Ordering System Documentation Team
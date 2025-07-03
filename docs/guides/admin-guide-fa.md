# 🛡️ راهنمای مدیر سیستم سفارش غذا (Admin Guide)

## مقدمه
این راهنما ویژه مدیران سیستم است و تمام امکانات داشبورد مدیریتی را با جزئیات کامل پوشش می‌دهد. سیستم در حال حاضر 100% تکمیل شده و تمام بخش‌های اصلی فعال هستند.

---

## ۱. ورود به داشبورد ادمین
- پس از ورود با حساب ادمین، از منوی اصلی گزینه «داشبورد مدیریت» را انتخاب کنید.
- داشبورد شامل ۶ تب اصلی است:
  1. مدیریت کاربران
  2. مدیریت سفارشات
  3. مدیریت تراکنش‌ها
  4. مدیریت رستوران‌ها
  5. گزارشات و آمار
  6. تنظیمات

### کلاس‌های مدیریتی موجود:
- **AdminController**: `backend/src/main/java/com/myapp/admin/AdminController.java`
- **AdminRepository**: `backend/src/main/java/com/myapp/admin/AdminRepository.java`
- **AdminService**: `backend/src/main/java/com/myapp/admin/AdminService.java`

### ویژگی‌های پیشرفته:
- **Real-time Updates**: به‌روزرسانی لحظه‌ای آمار و اطلاعات
- **Advanced Filtering**: فیلترهای پیشرفته برای تمام بخش‌ها
- **Export Capabilities**: خروجی CSV/Excel از تمام داده‌ها
- **Bulk Operations**: عملیات گروهی روی کاربران و رستوران‌ها

---

## ۲. مدیریت کاربران

### مشاهده و جستجو
- مشاهده لیست کامل کاربران با اطلاعات (نام، ایمیل، نقش، وضعیت، تاریخ عضویت)
- جستجو بر اساس نام یا ایمیل
- فیلتر بر اساس نقش (کاربر، ادمین، فروشنده، پیک)
- فیلتر بر اساس وضعیت (فعال/غیرفعال/معلق)

### عملیات مدیریتی
- **افزودن کاربر جدید**: ایجاد حساب کاربری جدید با نقش مشخص
- **ویرایش اطلاعات**: تغییر نام، ایمیل، شماره تلفن
- **تغییر نقش**: ارتقا یا تنزل نقش کاربر
- **تغییر وضعیت**: فعال/غیرفعال کردن حساب
- **حذف کاربر**: حذف دائمی حساب (با تأیید)

### ویژگی‌های پیشرفته
- **Bulk Operations**: انتخاب چندین کاربر و اعمال عملیات گروهی
- **User Analytics**: آمار فعالیت کاربران
- **Login History**: تاریخچه ورود کاربران
- **Account Lockout**: قفل کردن حساب‌های مشکوک

### نکته امنیتی:
- فقط ادمین‌ها مجاز به تغییر وضعیت یا حذف کاربر هستند.
- تمام عملیات در لاگ سیستم ثبت می‌شوند.

---

## ۳. مدیریت سفارشات

### مشاهده و فیلتر
- مشاهده لیست سفارشات با اطلاعات (شناسه، مشتری، رستوران، وضعیت، مبلغ، تاریخ)
- جستجو بر اساس کد پیگیری یا نام مشتری
- فیلتر بر اساس وضعیت سفارش (ثبت شده، تأیید شده، آماده‌سازی، ارسال، تحویل، لغو)
- فیلتر بر اساس تاریخ و مبلغ

### کلاس‌های مرتبط:
- **OrderController**: `backend/src/main/java/com/myapp/order/OrderController.java`
- **OrderRepository**: `backend/src/main/java/com/myapp/order/OrderRepository.java`
- **OrderService**: `backend/src/main/java/com/myapp/order/OrderService.java`

### عملیات مدیریتی
- **تغییر وضعیت سفارش**: انتقال سفارش بین مراحل مختلف
- **مشاهده جزئیات**: اطلاعات کامل سفارش شامل آیتم‌ها و آدرس تحویل
- **مدیریت تحویل**: تخصیص پیک و پیگیری تحویل
- **لغو سفارش**: لغو سفارش با دلیل

### ویژگی‌های پیشرفته
- **Order Analytics**: آمار سفارشات روزانه/ماهانه
- **Revenue Tracking**: پیگیری درآمد
- **Delivery Performance**: عملکرد تحویل
- **Customer Satisfaction**: رضایت مشتریان

---

## ۴. مدیریت تراکنش‌ها

### مشاهده تراکنش‌ها
- مشاهده لیست تراکنش‌ها با اطلاعات (شناسه، کاربر، نوع، مبلغ، وضعیت، تاریخ)
- جستجو بر اساس شناسه تراکنش یا شماره کارت
- فیلتر بر اساس نوع تراکنش (پرداخت، بازپرداخت، شارژ کیف پول)
- فیلتر بر اساس وضعیت (موفق، ناموفق، در انتظار)

### کلاس‌های مرتبط:
- **PaymentController**: `backend/src/main/java/com/myapp/payment/PaymentController.java`
- **PaymentRepository**: `backend/src/main/java/com/myapp/payment/PaymentRepository.java`
- **PaymentService**: `backend/src/main/java/com/myapp/payment/PaymentService.java`

### عملیات مدیریتی
- **مشاهده جزئیات**: اطلاعات کامل تراکنش
- **بازپرداخت**: بازپرداخت تراکنش‌های ناموفق
- **تأیید تراکنش**: تأیید تراکنش‌های مشکوک
- **مسدود کردن**: مسدود کردن تراکنش‌های مشکوک

### ویژگی‌های پیشرفته
- **Transaction Analytics**: آمار تراکنش‌ها
- **Fraud Detection**: تشخیص تراکنش‌های مشکوک
- **Payment Gateway Status**: وضعیت درگاه‌های پرداخت
- **Reconciliation**: تطبیق تراکنش‌ها

---

## ۵. مدیریت رستوران‌ها

### مشاهده و جستجو
- مشاهده لیست رستوران‌ها با اطلاعات (نام، آدرس، وضعیت، امتیاز، تعداد سفارش)
- جستجو بر اساس نام رستوران یا آدرس
- فیلتر بر اساس وضعیت (فعال/غیرفعال/معلق)
- فیلتر بر اساس نوع غذا

### کلاس‌های مرتبط:
- **RestaurantController**: `backend/src/main/java/com/myapp/restaurant/RestaurantController.java`
- **RestaurantRepository**: `backend/src/main/java/com/myapp/restaurant/RestaurantRepository.java`
- **RestaurantService**: `backend/src/main/java/com/myapp/restaurant/RestaurantService.java`

### عملیات مدیریتی
- **افزودن رستوران جدید**: ثبت رستوران جدید با اطلاعات کامل
- **ویرایش اطلاعات**: تغییر نام، آدرس، تلفن، ساعات کاری
- **تغییر وضعیت**: فعال/غیرفعال کردن رستوران
- **مدیریت منو**: مشاهده و تأیید آیتم‌های منو
- **حذف رستوران**: حذف رستوران (با تأیید)

### ویژگی‌های پیشرفته
- **Restaurant Analytics**: آمار عملکرد رستوران‌ها
- **Menu Management**: مدیریت منوها و آیتم‌ها
- **Rating Management**: مدیریت امتیازات و نظرات
- **Commission Tracking**: پیگیری کمیسیون‌ها

---

## ۶. گزارشات و آمار

### نمودارهای تحلیلی
- **نمودار دایره‌ای وضعیت سفارشات**: توزیع سفارشات بر اساس وضعیت
- **نمودار خطی درآمد هفتگی**: روند درآمد در طول هفته
- **نمودار ستونی فروش رستوران‌ها**: مقایسه فروش رستوران‌ها
- **نمودار کاربران جدید**: رشد کاربران در طول زمان

### گزارشات دوره‌ای
- **گزارش روزانه**: آمار روزانه سیستم
- **گزارش هفتگی**: خلاصه عملکرد هفتگی
- **گزارش ماهانه**: گزارش جامع ماهانه
- **گزارش سالانه**: آمار سالانه و روندها

### ویژگی‌های پیشرفته
- **Custom Reports**: گزارشات سفارشی
- **Export Options**: خروجی PDF/Excel/CSV
- **Scheduled Reports**: گزارشات زمان‌بندی شده
- **Email Alerts**: هشدارهای ایمیلی

---

## ۷. تنظیمات سیستم

### تنظیمات امنیتی
- **Password Policy**: سیاست رمز عبور
- **Session Timeout**: زمان انقضای نشست
- **Login Attempts**: محدودیت تلاش‌های ورود
- **IP Whitelist**: لیست سفید IP ها

### کلاس‌های امنیتی:
- **AdvancedSecurityUtil**: `backend/src/main/java/com/myapp/common/utils/AdvancedSecurityUtil.java`
- **PasswordUtil**: `backend/src/main/java/com/myapp/common/utils/PasswordUtil.java`
- **ValidationUtil**: `backend/src/main/java/com/myapp/common/utils/ValidationUtil.java`

### تنظیمات کسب‌وکار
- **Commission Rates**: نرخ کمیسیون
- **Delivery Fees**: هزینه‌های تحویل
- **Minimum Order**: حداقل مبلغ سفارش
- **Operating Hours**: ساعات کاری

### تنظیمات فنی
- **API Settings**: تنظیمات API
- **Database Configuration**: پیکربندی پایگاه داده
- **Backup Settings**: تنظیمات پشتیبان‌گیری
- **Logging Configuration**: تنظیمات لاگ

---

## ۸. ویژگی‌های پیشرفته

### سیستم امنیتی (فاز 34)
- **AdvancedSecurityUtil**: رمزگذاری AES-256 و مدیریت کلیدهای امنیتی
- **PasswordUtil**: هش کردن رمز عبور با BCrypt
- **ValidationUtil**: validation های امنیتی پیشرفته

### سیستم بهینه‌سازی (فاز 33)
- **PerformanceUtil**: بهینه‌سازی عملکرد
- **AdvancedOptimizer**: بهینه‌سازی پیشرفته
- **CachingSystem**: سیستم کش

### سیستم اطلاع‌رسانی (فاز 32)
- **NotificationController**: مدیریت اطلاع‌رسانی
- **NotificationRepository**: ذخیره‌سازی اطلاع‌رسانی
- **NotificationService**: سرویس اطلاع‌رسانی

---

## ۹. نکات تست و نگهداری

### تست‌های جامع
- تمام سناریوهای مدیریتی با تست‌های جامع پوشش داده شده‌اند.
- هرگونه تغییر در کد باید با اجرای تست‌ها و بررسی خروجی انجام شود.
- تمام تست‌های backend و امنیتی به گونه‌ای بازنویسی شده‌اند که در هر محیطی اجرا شوند.

### اجرای تست‌ها:
```bash
# اجرای تمام تست‌های admin
mvn test -Dtest=*Admin*Test

# اجرای تست‌های امنیتی
mvn test -Dtest=*Security*Test

# اجرای تست‌های عملکرد
mvn test -Dtest=*Performance*Test
```

### نگهداری سیستم
- **Daily Monitoring**: نظارت روزانه بر عملکرد سیستم
- **Weekly Backups**: پشتیبان‌گیری هفتگی
- **Monthly Reports**: گزارشات ماهانه
- **Quarterly Updates**: به‌روزرسانی‌های فصلی

### لاگ‌های سیستم:
- **Performance Logs**: `backend/logs/performance.log`
- **Security Logs**: `backend/logs/security.log`
- **Test Logs**: `backend/logs/tests.log`

---

## ۱۰. API Endpoints مدیریتی

### مدیریت کاربران:
- `GET /admin/users` - دریافت لیست کاربران
- `POST /admin/users` - ایجاد کاربر جدید
- `PUT /admin/users/{id}` - ویرایش کاربر
- `DELETE /admin/users/{id}` - حذف کاربر

### مدیریت سفارشات:
- `GET /admin/orders` - دریافت لیست سفارشات
- `PUT /admin/orders/{id}/status` - تغییر وضعیت سفارش
- `GET /admin/orders/{id}` - دریافت جزئیات سفارش

### مدیریت تراکنش‌ها:
- `GET /admin/transactions` - دریافت لیست تراکنش‌ها
- `POST /admin/transactions/{id}/refund` - بازپرداخت
- `PUT /admin/transactions/{id}/status` - تغییر وضعیت

### گزارشات:
- `GET /admin/reports/daily` - گزارش روزانه
- `GET /admin/reports/weekly` - گزارش هفتگی
- `GET /admin/reports/monthly` - گزارش ماهانه

---

## نتیجه‌گیری

سیستم مدیریتی کاملاً آماده و فعال است. تمام ویژگی‌های مورد نیاز برای مدیریت سیستم سفارش غذا پیاده‌سازی شده و تست‌های جامع نیز انجام شده‌اند.

---
**آخرین به‌روزرسانی**: 15 ژوئن 2025  
**مسئول مدیریت**: Food Ordering System Admin Team
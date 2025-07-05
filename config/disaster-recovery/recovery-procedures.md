# رویه‌های بازیابی بحران
# Food Ordering System - Disaster Recovery Procedures

**نسخه**: 1.0  
**تاریخ**: 2025-07-05  
**مسئول**: تیم عملیات IT

---

## 1. مقدمه

این سند شامل رویه‌های تفصیلی برای بازیابی سیستم سفارش غذا در مواقع بحران است. هدف این سند کمک به تیم IT برای بازیابی سریع و موثر سیستم در شرایط اضطراری است.

## 2. طبقه‌بندی بحران‌ها

### 2.1 بحران‌های سطح 1 (بحرانی)
- **خرابی کامل پایگاه داده**
- **از دست رفتن کامل سرور**
- **حملات سایبری با تخریب داده**
- **آتش‌سوزی یا بلایای طبیعی**

### 2.2 بحران‌های سطح 2 (جدی)
- **خرابی جزئی پایگاه داده**
- **از کار افتادن اپلیکیشن**
- **خرابی فایل‌های تنظیمات**
- **مشکلات امنیتی**

### 2.3 بحران‌های سطح 3 (متوسط)
- **کاهش عملکرد سیستم**
- **مشکلات شبکه**
- **خرابی فایل‌های لاگ**
- **مشکلات کاربری**

## 3. رویه‌های بازیابی سطح 1

### 3.1 بازیابی کامل پایگاه داده

#### مرحله 1: ارزیابی اولیه
```bash
# بررسی وضعیت پایگاه داده
sqlite3 backend/food_ordering.db ".tables"

# بررسی یکپارچگی
sqlite3 backend/food_ordering.db "PRAGMA integrity_check;"
```

#### مرحله 2: توقف سرویس‌ها
```powershell
# توقف سرویس‌های مرتبط
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
Stop-Process -Name "javafx" -Force -ErrorAction SilentlyContinue
```

#### مرحله 3: بازیابی از پشتیبان
```powershell
# یافتن آخرین پشتیبان سالم
$latestBackup = Get-ChildItem "backups/database" | Sort-Object LastWriteTime -Descending | Select-Object -First 1

# بازیابی پایگاه داده
Copy-Item $latestBackup.FullName "backend/food_ordering.db" -Force

# تأیید بازیابی
sqlite3 backend/food_ordering.db "PRAGMA integrity_check;"
```

#### مرحله 4: راه‌اندازی مجدد
```powershell
# راه‌اندازی سرویس‌ها
cd backend
mvn spring-boot:run

# تست اتصال
curl http://localhost:8080/api/health
```

### 3.2 بازیابی کامل سرور

#### مرحله 1: ایجاد محیط جدید
```powershell
# ایجاد ساختار دایرکتوری
mkdir food-ordering-recovery
cd food-ordering-recovery

# بازیابی کدهای منبع
git clone <repository-url>
# یا از پشتیبان محلی
Copy-Item "backups/source/latest/*" . -Recurse -Force
```

#### مرحله 2: بازیابی تنظیمات
```powershell
# بازیابی فایل‌های تنظیمات
Copy-Item "backups/config/latest/*" "config/" -Recurse -Force

# بازیابی پایگاه داده
Copy-Item "backups/database/latest/*" "backend/" -Force
```

#### مرحله 3: نصب وابستگی‌ها
```powershell
# نصب وابستگی‌های Java
cd backend
mvn clean install

# نصب وابستگی‌های JavaFX
cd ../frontend-javafx
mvn clean install
```

#### مرحله 4: تست و راه‌اندازی
```powershell
# تست پایگاه داده
sqlite3 backend/food_ordering.db "SELECT COUNT(*) FROM users;"

# راه‌اندازی سرویس‌ها
.\scripts\start-all-services.ps1
```

## 4. رویه‌های بازیابی سطح 2

### 4.1 بازیابی جزئی پایگاه داده

#### شناسایی جداول آسیب‌دیده
```sql
-- بررسی جداول
.tables

-- بررسی یکپارچگی هر جدول
PRAGMA integrity_check(table_name);
```

#### بازیابی جداول خاص
```powershell
# استخراج جدول از پشتیبان
sqlite3 "backups/database/latest/food_ordering.db" ".dump users" > users_recovery.sql

# بازیابی جدول
sqlite3 "backend/food_ordering.db" < users_recovery.sql
```

### 4.2 بازیابی فایل‌های تنظیمات

#### شناسایی فایل‌های مفقود
```powershell
# بررسی فایل‌های تنظیمات
Test-Path "backend/src/main/resources/application.properties"
Test-Path "config/hibernate.cfg.xml"
```

#### بازیابی فایل‌های تنظیمات
```powershell
# بازیابی از پشتیبان
Copy-Item "backups/config/latest/application.properties" "backend/src/main/resources/" -Force
Copy-Item "backups/config/latest/hibernate.cfg.xml" "config/" -Force
```

## 5. رویه‌های بازیابی سطح 3

### 5.1 بازیابی عملکرد سیستم

#### بررسی منابع سیستم
```powershell
# بررسی استفاده از CPU
Get-Process | Sort-Object CPU -Descending | Select-Object -First 10

# بررسی استفاده از حافظه
Get-Process | Sort-Object WorkingSet -Descending | Select-Object -First 10
```

#### بهینه‌سازی عملکرد
```powershell
# راه‌اندازی مجدد سرویس‌ها
Restart-Service -Name "FoodOrderingService"

# پاک‌کردن کش
Remove-Item "cache/*" -Force -Recurse
```

## 6. اسکریپت‌های بازیابی خودکار

### 6.1 اسکریپت بازیابی سریع
```powershell
# emergency-recovery.ps1
param(
    [string]$RecoveryType = "database",
    [string]$BackupDate = "latest"
)

switch ($RecoveryType) {
    "database" {
        Write-Host "Starting database recovery..."
        # کد بازیابی پایگاه داده
    }
    "config" {
        Write-Host "Starting config recovery..."
        # کد بازیابی تنظیمات
    }
    "full" {
        Write-Host "Starting full system recovery..."
        # کد بازیابی کامل
    }
}
```

### 6.2 اسکریپت تأیید بازیابی
```powershell
# verify-recovery.ps1
function Test-DatabaseIntegrity {
    sqlite3 backend/food_ordering.db "PRAGMA integrity_check;"
}

function Test-ApplicationHealth {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/health"
    return $response.status -eq "UP"
}
```

## 7. چک‌لیست بازیابی

### 7.1 چک‌لیست بازیابی کامل
- [ ] توقف سرویس‌های در حال اجرا
- [ ] ایجاد پشتیبان از وضعیت فعلی
- [ ] شناسایی آخرین پشتیبان سالم
- [ ] بازیابی پایگاه داده
- [ ] بازیابی فایل‌های تنظیمات
- [ ] بازیابی کدهای منبع
- [ ] تست یکپارچگی داده‌ها
- [ ] راه‌اندازی سرویس‌ها
- [ ] تست عملکرد سیستم
- [ ] اعلان به تیم و کاربران

### 7.2 چک‌لیست بازیابی جزئی
- [ ] شناسایی بخش آسیب‌دیده
- [ ] ایجاد پشتیبان جزئی
- [ ] بازیابی بخش مورد نظر
- [ ] تست عملکرد بخش بازیابی شده
- [ ] ادغام با سیستم اصلی
- [ ] تست کامل سیستم

## 8. مدیریت بحران

### 8.1 تیم بازیابی بحران
- **مدیر بحران**: مسئول تصمیم‌گیری‌های کلی
- **تکنسین پایگاه داده**: مسئول بازیابی داده‌ها
- **تکنسین سیستم**: مسئول بازیابی سیستم
- **متخصص امنیت**: مسئول بررسی امنیت
- **رابط کسب‌وکار**: مسئول ارتباط با کاربران

### 8.2 کانال‌های ارتباطی
- **تلفن اضطراری**: +98-XXX-XXXX-XXX
- **ایمیل**: disaster-recovery@foodordering.com
- **پیام‌رسان**: کانال تلگرام/واتساپ تیم
- **سیستم بلیط**: جیرا/تریلو

### 8.3 زمان‌بندی بازیابی
- **RTO (Recovery Time Objective)**: 4 ساعت
- **RPO (Recovery Point Objective)**: 1 ساعت
- **زمان اعلان**: 15 دقیقه
- **زمان ارزیابی**: 30 دقیقه

## 9. تست و آموزش

### 9.1 تست‌های دوره‌ای
- **تست ماهانه**: بازیابی پایگاه داده
- **تست فصلی**: بازیابی کامل سیستم
- **تست سالانه**: شبیه‌سازی بحران کامل

### 9.2 آموزش تیم
- **آموزش اولیه**: 8 ساعت
- **آموزش‌های بازآموزی**: 2 ساعت ماهانه
- **مانور عملیاتی**: 4 ساعت فصلی

## 10. مستندسازی و گزارش‌دهی

### 10.1 گزارش بحران
- **شناسایی علت**: شرح دقیق علت بحران
- **اقدامات انجام شده**: مراحل بازیابی
- **زمان بازیابی**: مدت زمان کل فرآیند
- **درس‌های آموخته**: نکات برای بهبود

### 10.2 به‌روزرسانی رویه‌ها
- **بررسی ماهانه**: اصلاح رویه‌ها
- **به‌روزرسانی فصلی**: اضافه کردن رویه‌های جدید
- **مراجعه سالانه**: بازبینی کامل سند

---

**تاریخ آخرین به‌روزرسانی**: 2025-07-05  
**نسخه بعدی**: 1.1  
**مسئول نگهداری**: تیم عملیات IT

*این سند محرمانه بوده و فقط برای کارکنان مجاز در دسترس است.* 
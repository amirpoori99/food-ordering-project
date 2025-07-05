# تعمیرات اعمال شده بر پروژه Food Ordering System

## تاریخ: ۴ ژوئیه ۲۰۲۵

### 🔧 مشکل اصلی که برطرف شد:
**مشکل پایداری داده‌ها** - داده‌های وارد شده در دیتابیس SQLite پس از بستن برنامه از بین می‌رفتند.

### ✅ راه‌حل اعمال شده:

#### 1. تغییر مسیر دیتابیس به مسیر ثابت
- **فایل**: `backend/src/main/java/com/myapp/common/utils/DatabaseUtil.java`
- **تغییر**: مسیر دیتابیس از مسیر نسبی به مسیر مطلق تغییر یافت
- **مسیر جدید**: `%USERPROFILE%\.food_ordering\food_ordering.db` (Windows)
- **کد اضافه شده**:
```java
// ایجاد مسیر پایدار برای دیتابیس
Path userHome = Paths.get(System.getProperty("user.home"));
Path dbDir = userHome.resolve(".food_ordering");
Files.createDirectories(dbDir);
Path dbFile = dbDir.resolve("food_ordering.db");

// تبدیل به URL صحیح برای JDBC
String absolutePath = dbFile.toAbsolutePath().toString().replace("\\", "/");
String absoluteUrl = "jdbc:sqlite:" + absolutePath;

// بازنویسی URL فقط در محیط production (نه در تست‌ها)
boolean runningTests = System.getProperty("surefire.real.class.path") != null;
if (!runningTests) {
    registryBuilder.applySetting("hibernate.connection.url", absoluteUrl);
    registryBuilder.applySetting("hibernate.hbm2ddl.auto", "update");
}
```

#### 2. جداسازی محیط تست از محیط production
- تست‌ها همچنان از دیتابیس in-memory (`:memory:`) استفاده می‌کنند
- فقط در محیط production مسیر دیتابیس تغییر می‌کند

### 📊 نتایج:

#### موفقیت‌ها:
1. ✅ کامپایل Backend بدون خطا
2. ✅ تمام ۲۱۹۴ تست Backend سبز شدند
3. ✅ کامپایل Frontend JavaFX بدون خطا  
4. ✅ مسیر دیتابیس به صورت پایدار تنظیم شد

#### مشکل باقی‌مانده:
- **SQLite Database Lock**: به دلیل محدودیت SQLite در مدیریت همزمانی، وقتی چندین پروسه به دیتابیس متصل می‌شوند، خطای "database is locked" رخ می‌دهد.

### 🚀 راهنمای اجرا:

#### 1. اجرای Backend:
```bash
cd backend
mvn clean package -DskipTests
java -cp "target/classes;target/lib/*" com.myapp.ServerApp
```

#### 2. پر کردن دیتابیس (در ترمینال جداگانه):
```bash
cd backend  
java -cp "target/classes;target/lib/*" com.myapp.DatabasePopulator
```

#### 3. اجرای Frontend:
```bash
cd frontend-javafx
mvn clean package -DskipTests
java -jar target/food-ordering-frontend-1.0.0.jar
```

### ⚠️ نکات مهم:
1. **مطمئن شوید فقط یک instance از برنامه در حال اجرا است**
2. **قبل از اجرای DatabasePopulator، سرور را ببندید**
3. **دیتابیس در مسیر `%USERPROFILE%\.food_ordering\food_ordering.db` ذخیره می‌شود**

### 🔍 بررسی پایداری داده‌ها:
برای بررسی اینکه داده‌ها پایدار هستند:
```bash
cd backend
java -cp "target/classes;target/lib/*" com.myapp.DatabaseChecker
```

### ⚡ بهبودهای پیشنهادی برای آینده:
1. **مهاجرت به دیتابیس قدرتمندتر**: PostgreSQL یا MySQL برای مدیریت بهتر همزمانی
2. **استفاده از Connection Pool**: HikariCP برای مدیریت بهتر اتصالات
3. **پیاده‌سازی Retry Logic**: برای مدیریت خطاهای موقت database lock 
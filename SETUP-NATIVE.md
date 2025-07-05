# راهنمای نصب Native - سیستم سفارش غذا

این راهنما برای نصب و راه‌اندازی پروژه بدون Docker است.

## 📋 پیش‌نیازها

### 1. Java 17+
```bash
java -version
# باید Java 17 یا بالاتر باشد
```

### 2. Maven 3.6+
```bash
mvn --version
# باید Maven 3.6 یا بالاتر باشد
```

## 🐘 نصب PostgreSQL

### Windows:
1. دانلود از [postgresql.org](https://www.postgresql.org/download/windows/)
2. نصب با تنظیمات زیر:
   - Username: `postgres`
   - Password: `123456`
   - Port: `5432`
   - Database: `postgres` (پیش‌فرض)

### macOS:
```bash
# با Homebrew
brew install postgresql@15
brew services start postgresql@15

# تنظیم کاربر
createuser -s postgres
psql postgres -c "ALTER USER postgres PASSWORD '123456';"
```

### Linux (Ubuntu/Debian):
```bash
# نصب PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# تنظیم رمز عبور
sudo -u postgres psql
ALTER USER postgres PASSWORD '123456';
\q

# راه‌اندازی سرویس
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

### تنظیم دیتابیس‌ها:
```sql
-- اتصال به PostgreSQL
psql -U postgres -h localhost

-- ایجاد دیتابیس‌های مورد نیاز
CREATE DATABASE food_ordering;
CREATE DATABASE food_ordering_test;
CREATE DATABASE food_ordering_prod;

-- بررسی دیتابیس‌ها
\l

-- خروج
\q
```

## 🔴 نصب Redis

### Windows:
1. دانلود از [Redis releases](https://github.com/microsoftarchive/redis/releases)
2. نصب و راه‌اندازی:
```bash
# دانلود و اجرا
redis-server.exe

# در terminal دیگر، تست اتصال
redis-cli.exe
ping
# باید PONG برگردد
```

### macOS:
```bash
# با Homebrew
brew install redis
brew services start redis

# تست اتصال
redis-cli ping
# باید PONG برگردد
```

### Linux (Ubuntu/Debian):
```bash
# نصب Redis
sudo apt update
sudo apt install redis-server

# تنظیم سرویس
sudo systemctl enable redis-server
sudo systemctl start redis-server

# تست اتصال
redis-cli ping
# باید PONG برگردد
```

## 🚀 راه‌اندازی پروژه

### 1. کلون پروژه
```bash
git clone <repository-url>
cd food-ordering-project
```

### 2. راه‌اندازی Backend
```bash
cd backend

# کامپایل پروژه
mvn clean compile

# اجرای تست‌ها (اختیاری)
mvn test

# اجرای برنامه
mvn exec:java -Dexec.mainClass="com.myapp.ServerApp"
```

Backend روی `http://localhost:8080/api` در دسترس خواهد بود.

### 3. راه‌اندازی Frontend
```bash
cd ../frontend-javafx

# کامپایل
mvn clean compile

# اجرای برنامه
mvn javafx:run
```

## 🔧 تنظیمات اضافی

### تغییر رمز عبور دیتابیس:
اگر رمز عبور متفاوتی برای PostgreSQL انتخاب کردید:

**فایل:** `backend/src/main/resources/application.properties`
```properties
spring.datasource.password=YOUR_PASSWORD
```

**فایل:** `backend/src/main/resources/hibernate.cfg.xml`
```xml
<property name="hibernate.connection.password">YOUR_PASSWORD</property>
```

### تغییر پورت Redis:
اگر Redis روی پورت متفاوتی اجرا می‌شود:

**فایل:** `backend/src/main/resources/application.properties`
```properties
redis.port=YOUR_REDIS_PORT
```

## 🔍 عیب‌یابی

### مشکلات رایج:

#### 1. خطای اتصال به PostgreSQL:
```
Connection refused. Check that the hostname and port are correct
```
**راه‌حل:**
- اطمینان از اجرای PostgreSQL: `sudo systemctl status postgresql`
- بررسی پورت: `sudo netstat -plnt | grep 5432`
- بررسی فایروال

#### 2. خطای احراز هویت PostgreSQL:
```
password authentication failed for user "postgres"
```
**راه‌حل:**
```bash
# تغییر رمز عبور
sudo -u postgres psql
ALTER USER postgres PASSWORD '123456';
```

#### 3. خطای اتصال به Redis:
```
Could not connect to Redis
```
**راه‌حل:**
```bash
# بررسی وضعیت Redis
redis-cli ping

# اگر پاسخ نداد، راه‌اندازی مجدد
sudo systemctl restart redis-server
```

#### 4. خطای کامپایل Maven:
```
package org.postgresql does not exist
```
**راه‌حل:**
```bash
# پاک کردن cache Maven
mvn clean
rm -rf ~/.m2/repository/org/postgresql

# کامپایل مجدد
mvn clean compile
```

## 📊 تست اتصالات

### تست PostgreSQL:
```bash
psql -U postgres -h localhost -p 5432 -d food_ordering -c "SELECT version();"
```

### تست Redis:
```bash
redis-cli set test "Hello World"
redis-cli get test
# باید "Hello World" برگردد
```

### تست Backend API:
```bash
curl http://localhost:8080/api/health
# باید پاسخ مثبت برگردد
```

## 🎯 مرحله بعد

پس از راه‌اندازی موفق:
1. مطالعه [API Documentation](docs/api-reference-fa.md)
2. راه‌اندازی [Admin Panel](docs/admin-guide-fa.md)
3. تنظیم [Development Environment](docs/development-guide-fa.md)

## 📞 دریافت کمک

اگر با مشکلی مواجه شدید:
1. بررسی [مستندات عیب‌یابی](docs/troubleshooting-fa.md)
2. جستجو در [Issues](../../issues)
3. ایجاد Issue جدید با جزئیات کامل خطا 
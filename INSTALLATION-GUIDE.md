# 📖 راهنمای نصب و راه‌اندازی - سیستم سفارش غذا

## 📋 پیش‌نیازها

### 🔧 نرم‌افزارهای مورد نیاز
- **Java:** JDK 17 یا بالاتر
- **PostgreSQL:** نسخه 17 یا بالاتر
- **Maven:** نسخه 3.9 یا بالاتر
- **Git:** برای دریافت کد منبع

### 💻 مشخصات سیستم
- **سیستم عامل:** Windows 10/11, Linux, macOS
- **حافظه:** حداقل 4GB RAM
- **فضای دیسک:** حداقل 2GB فضای آزاد
- **پورت:** 8081 (برای سرور) و 5432 (برای PostgreSQL)

---

## 🚀 مراحل نصب

### مرحله 1: نصب Java JDK 17

#### Windows:
1. دانلود JDK 17 از [Oracle](https://www.oracle.com/java/technologies/downloads/) یا [Eclipse Adoptium](https://adoptium.net/)
2. اجرای فایل نصب و پیروی از مراحل
3. تنظیم متغیر محیطی `JAVA_HOME`:
   ```cmd
   setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot"
   ```
4. اضافه کردن Java به PATH:
   ```cmd
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install openjdk-17-jdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$PATH:$JAVA_HOME/bin
```

#### macOS:
```bash
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH=$PATH:$JAVA_HOME/bin
```

### مرحله 2: نصب PostgreSQL 17

#### Windows:
1. دانلود PostgreSQL 17 از [postgresql.org](https://www.postgresql.org/download/windows/)
2. اجرای فایل نصب
3. تنظیم پسورد برای کاربر `postgres`
4. نصب pgAdmin (اختیاری)

#### Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install postgresql-17 postgresql-contrib-17
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### macOS:
```bash
brew install postgresql@17
brew services start postgresql@17
```

### مرحله 3: نصب Maven

#### Windows:
1. دانلود Maven از [maven.apache.org](https://maven.apache.org/download.cgi)
2. استخراج در `C:\apache-maven`
3. تنظیم متغیر محیطی:
   ```cmd
   setx MAVEN_HOME "C:\apache-maven\apache-maven-3.9.9"
   setx PATH "%PATH%;%MAVEN_HOME%\bin"
   ```

#### Linux/macOS:
```bash
sudo apt install maven  # Ubuntu/Debian
brew install maven      # macOS
```

### مرحله 4: دریافت کد منبع

```bash
git clone https://github.com/your-repo/food-ordering-project.git
cd food-ordering-project
```

---

## ⚙️ پیکربندی سیستم

### مرحله 1: تنظیم PostgreSQL

#### ایجاد کاربر و دیتابیس:
```sql
-- ورود به PostgreSQL
psql -U postgres

-- ایجاد کاربر
CREATE USER "food-ordering-project" WITH PASSWORD 'food-ordering-project';

-- ایجاد دیتابیس
CREATE DATABASE food_ordering_prod OWNER "food-ordering-project";

-- اعطای مجوزها
GRANT ALL PRIVILEGES ON DATABASE food_ordering_prod TO "food-ordering-project";

-- خروج
\q
```

#### تنظیم pg_hba.conf (برای Windows):
1. باز کردن فایل: `C:\Program Files\PostgreSQL\17\data\pg_hba.conf`
2. اضافه کردن خط زیر:
   ```
   host    all             food-ordering-project    127.0.0.1/32            trust
   ```
3. ریستارت سرویس PostgreSQL

### مرحله 2: پیکربندی پروژه

#### بررسی فایل تنظیمات:
```bash
cd backend
ls src/main/resources/
```

#### فایل‌های تنظیمات موجود:
- `hibernate-production.cfg.xml`: تنظیمات Hibernate برای PostgreSQL
- `application.properties`: تنظیمات عمومی برنامه

#### تنظیمات اتصال دیتابیس:
```xml
<!-- hibernate-production.cfg.xml -->
<property name="hibernate.connection.url">jdbc:postgresql://localhost:5432/food_ordering_prod</property>
<property name="hibernate.connection.username">food-ordering-project</property>
<property name="hibernate.connection.password">food-ordering-project</property>
```

---

## 🏗️ راه‌اندازی پروژه

### مرحله 1: کامپایل پروژه

```bash
cd backend
mvn clean compile
```

### مرحله 2: کپی وابستگی‌ها

```bash
mvn dependency:copy-dependencies
```

### مرحله 3: اجرای سرور

#### روش 1: با Maven Exec Plugin
```bash
mvn exec:java
```

#### روش 2: با Java مستقیم
```bash
java -cp "target/classes;target/dependency/*" com.myapp.ServerApp
```

#### روش 3: با اسکریپت PowerShell (Windows)
```powershell
.\run-with-postgresql.ps1
```

### مرحله 4: تست سرور

```bash
# تست Health Check
curl http://localhost:8081/health

# تست API
curl http://localhost:8081/api/test

# تست رستوران‌ها
curl http://localhost:8081/api/restaurants
```

---

## 🔧 اسکریپت‌های مدیریتی

### اسکریپت‌های موجود:

#### 1. setup-postgresql.ps1
```powershell
# راه‌اندازی کامل PostgreSQL
.\setup-postgresql.ps1
```

#### 2. simple-migration.ps1
```powershell
# مهاجرت داده‌ها
.\simple-migration.ps1
```

#### 3. test-postgresql-connection.ps1
```powershell
# تست اتصال به دیتابیس
.\test-postgresql-connection.ps1
```

#### 4. run-with-postgresql.ps1
```powershell
# اجرای سرور با PostgreSQL
.\run-with-postgresql.ps1
```

---

## 🧪 تست و اعتبارسنجی

### تست‌های اولیه:

#### 1. تست اتصال دیتابیس:
```bash
psql -U food-ordering-project -h localhost -d food_ordering_prod -c "SELECT 1;"
```

#### 2. تست API Endpoints:
```bash
# Health Check
curl http://localhost:8081/health

# API Test
curl http://localhost:8081/api/test

# Restaurants
curl http://localhost:8081/api/restaurants

# Admin Dashboard
curl http://localhost:8081/api/admin/dashboard

# Analytics Dashboard
curl http://localhost:8081/api/analytics/dashboard
```

#### 3. تست عملکرد:
```bash
# تست سرعت پاسخ
time curl http://localhost:8081/health

# تست همزمان
for i in {1..10}; do curl http://localhost:8081/health & done
```

---

## 🔒 تنظیمات امنیتی

### 1. تنظیمات Firewall:
```bash
# Windows
netsh advfirewall firewall add rule name="Food Ordering Server" dir=in action=allow protocol=TCP localport=8081

# Linux
sudo ufw allow 8081
```

### 2. تنظیمات SSL/TLS (اختیاری):
```bash
# تولید کلید SSL
keytool -genkeypair -alias foodordering -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650
```

### 3. تنظیمات امنیتی دیتابیس:
```sql
-- تغییر پسورد کاربر
ALTER USER "food-ordering-project" WITH PASSWORD 'new-secure-password';

-- محدود کردن اتصالات
REVOKE CONNECT ON DATABASE food_ordering_prod FROM PUBLIC;
GRANT CONNECT ON DATABASE food_ordering_prod TO "food-ordering-project";
```

---

## 📊 مانیتورینگ و نگهداری

### 1. لاگ‌های سیستم:
```bash
# مشاهده لاگ‌های سرور
tail -f logs/server.log

# مشاهده لاگ‌های PostgreSQL
tail -f /var/log/postgresql/postgresql-17-main.log
```

### 2. مانیتورینگ عملکرد:
```bash
# بررسی استفاده از CPU و RAM
top
htop

# بررسی پورت‌های فعال
netstat -tulpn | grep :8081
netstat -tulpn | grep :5432
```

### 3. پشتیبان‌گیری دیتابیس:
```bash
# پشتیبان‌گیری
pg_dump -U food-ordering-project -h localhost food_ordering_prod > backup.sql

# بازگردانی
psql -U food-ordering-project -h localhost food_ordering_prod < backup.sql
```

---

## 🚨 عیب‌یابی

### مشکلات رایج:

#### 1. خطای اتصال به دیتابیس:
```bash
# بررسی وضعیت PostgreSQL
sudo systemctl status postgresql

# بررسی پورت
netstat -an | grep :5432

# تست اتصال
psql -U food-ordering-project -h localhost -d food_ordering_prod
```

#### 2. خطای پورت 8081:
```bash
# بررسی پورت
netstat -an | grep :8081

# کشتن پروسه
lsof -ti:8081 | xargs kill -9
```

#### 3. خطای Java:
```bash
# بررسی نسخه Java
java -version

# بررسی JAVA_HOME
echo $JAVA_HOME
```

#### 4. خطای Maven:
```bash
# پاک کردن cache
mvn clean

# به‌روزرسانی dependencies
mvn dependency:resolve
```

---

## 📈 بهینه‌سازی

### 1. تنظیمات JVM:
```bash
# تنظیمات حافظه
java -Xms512m -Xmx2g -cp "target/classes;target/dependency/*" com.myapp.ServerApp
```

### 2. تنظیمات PostgreSQL:
```sql
-- تنظیمات Connection Pool
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';

-- اعمال تغییرات
SELECT pg_reload_conf();
```

### 3. تنظیمات Hibernate:
```xml
<!-- تنظیمات Connection Pool -->
<property name="hibernate.connection.pool_size">20</property>
<property name="hibernate.connection.hikari.maximumPoolSize">20</property>
<property name="hibernate.connection.hikari.minimumIdle">5</property>
```

---

## 📞 پشتیبانی

### اطلاعات تماس:
- **ایمیل:** support@foodordering.com
- **تلفن:** 021-12345678
- **ساعات کاری:** شنبه تا چهارشنبه، 9 صبح تا 6 عصر

### منابع مفید:
- [مستندات API](API-DOCUMENTATION.md)
- [گزارش مهاجرت](FINAL-MIGRATION-REPORT.md)
- [لیست کارها](TODO-LIST.md)

---

*آخرین به‌روزرسانی: 2025-07-07* 
# 🚀 راهنمای نصب

## پیش‌نیازها

### سیستم مورد نیاز
- **جاوا:** نسخه 17 یا بالاتر
- **Maven:** نسخه 3.6 یا بالاتر
- **حافظه:** حداقل 4 گیگابایت RAM
- **فضای دیسک:** حداقل 20 گیگابایت فضای خالی
- **سیستم عامل:** Windows 10+، Linux، macOS

### پایگاه داده
- **توسعه:** SQLite (شامل بسته)
- **تولیدی:** PostgreSQL نسخه 12 یا بالاتر

---

## راه‌اندازی محیط توسعه

### 1. شروع سریع
```bash
# کلون کردن مخزن
git clone <repository-url>
cd food-ordering-project

# ساخت و اجرای بک‌اند
cd backend
mvn clean package
java -jar target/food-ordering-backend.jar

# ساخت و اجرای فرانت‌اند (ترمینال جدید)
cd ../frontend-javafx
mvn clean package
java -jar target/food-ordering-frontend.jar
```

### 2. پیکربندی توسعه
- بک‌اند روی `localhost:8081` اجرا می‌شود
- از پایگاه داده SQLite استفاده می‌کند (خودکار ایجاد می‌شود)
- نیازی به پیکربندی اضافی نیست

---

## استقرار تولیدی

### 1. راه‌اندازی پایگاه داده
```bash
# نصب PostgreSQL
sudo apt install postgresql postgresql-contrib

# ایجاد پایگاه داده
sudo -u postgres psql -f database-setup.sql

# تأیید اتصال
psql -h localhost -U food_ordering_user -d food_ordering_prod -c "SELECT 1"
```

### 2. پیکربندی محیط
```bash
# تنظیم متغیرهای محیط
export DATABASE_URL=jdbc:postgresql://localhost:5432/food_ordering_prod
export DATABASE_USERNAME=food_ordering_user
export DATABASE_PASSWORD=your_secure_password
export JWT_SECRET=your_256_bit_secret_key
export SERVER_PORT=8081
```

### 3. استقرار برنامه

**لینوکس/یونیکس:**
```bash
# کپی فایل‌ها
sudo mkdir -p /opt/food-ordering
sudo cp food-ordering-backend.jar /opt/food-ordering/
sudo cp application-production.properties /opt/food-ordering/

# نصب سرویس
sudo cp food-ordering.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable food-ordering
sudo systemctl start food-ordering
```

**ویندوز:**
```batch
# ایجاد پوشه
mkdir C:\food-ordering

# کپی فایل‌ها
copy food-ordering-backend.jar C:\food-ordering\
copy application-production.properties C:\food-ordering\

# اجرای سرویس
food-ordering-windows.bat start
```

### 4. تأیید نصب
```bash
# بررسی وضعیت سرویس
systemctl status food-ordering

# تست API
curl http://localhost:8081/health

# مشاهده لاگ‌ها
journalctl -u food-ordering -f
```

---

## راه‌اندازی SSL/HTTPS

### 1. تولید گواهینامه
```bash
# خودامضا (توسعه)
keytool -genkeypair -alias food-ordering -keyalg RSA -keysize 2048 \
  -keystore keystore.jks -validity 365

# تولیدی (Let's Encrypt)
certbot certonly --standalone -d yourdomain.com
```

### 2. پیکربندی SSL
```properties
# در application-production.properties
ssl.enabled=true
ssl.keystore.path=/etc/ssl/food-ordering/keystore.jks
ssl.keystore.password=your_password
```

---

## راه‌اندازی پشتیبان‌گیری

### 1. پیکربندی پشتیبان‌گیری
```bash
# ویرایش پیکربندی پشتیبان‌گیری
cp backup.conf /opt/food-ordering/
nano /opt/food-ordering/backup.conf

# تنظیم اطلاعات ورود پایگاه داده
DB_PASSWORD=your_backup_password
BACKUP_LOCATION=/var/backups/food-ordering
```

### 2. زمان‌بندی پشتیبان‌گیری خودکار
```bash
# اضافه کردن به crontab
sudo crontab -e

# پشتیبان‌گیری روزانه در ساعت 2 صبح
0 2 * * * /opt/food-ordering/backup-system.sh backup
```

---

## توزیع فرانت‌اند

### 1. ساخت توزیع
```bash
cd frontend-javafx
mvn clean package

# فایل JAR ایجاد شده در:
# target/food-ordering-frontend.jar
```

### 2. نصب کلاینت
```bash
# اطمینان از نصب Java 17+
java -version

# اجرای برنامه
java -jar food-ordering-frontend.jar

# یا ایجاد میانبر دسکتاپ
chmod +x food-ordering-frontend.jar
```

---

## فایل‌های پیکربندی

### فایل‌های کلیدی پیکربندی
- `application-production.properties` - تنظیمات تولیدی
- `database-setup.sql` - راه‌اندازی اولیه پایگاه داده
- `food-ordering.service` - سرویس لینوکس
- `backup.conf` - پیکربندی پشتیبان‌گیری

### پوشه‌های مهم
- `/opt/food-ordering/` - فایل‌های برنامه (لینوکس)
- `/var/log/food-ordering/` - فایل‌های لاگ
- `/var/backups/food-ordering/` - فایل‌های پشتیبان

---

## عیب‌یابی

### مشکلات رایج

**پورت در حال استفاده:**
```bash
# بررسی اینکه چه چیزی از پورت 8081 استفاده می‌کند
netstat -tulpn | grep 8081
# کشتن پروسه یا تغییر پورت
```

**خطا در اتصال پایگاه داده:**
```bash
# تأیید اینکه PostgreSQL در حال اجرا است
systemctl status postgresql
# بررسی اطلاعات ورود و رشته اتصال
```

**جاوا یافت نشد:**
```bash
# نصب Java 17
sudo apt install openjdk-17-jdk
# تأیید نصب
java -version
```

برای عیب‌یابی بیشتر، [راهنمای عیب‌یابی](troubleshooting.md) را ببینید.

---

## مراحل بعدی

بعد از نصب:
1. [راهنمای کاربر](user-guide.md) را برای مستندات کاربر نهایی بخوانید
2. [راهنمای مدیر](admin-guide.md) را برای مدیریت سیستم بخوانید
3. [مرجع API](api-reference.md) را برای ادغام بررسی کنید 
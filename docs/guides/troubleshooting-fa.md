# 🔧 راهنمای عیب‌یابی - سیستم سفارش غذا

مرجع سریع برای تشخیص و رفع مشکلات رایج.

---

## مشکلات برنامه

### سرویس شروع نمی‌شود

**بررسی نسخه جاوا:**
```bash
java -version  # باید 17+ باشد
```

**بررسی در دسترس بودن پورت:**
```bash
netstat -tulpn | grep 8081
```

**بررسی لاگ‌ها:**
```bash
journalctl -u food-ordering -n 20
```

**راه‌حل‌ها:**
- نصب Java 17: `sudo apt install openjdk-17-jdk`
- کشتن استفاده از پورت: `sudo lsof -ti:8081 | xargs sudo kill -9`
- تصحیح مجوزها: `sudo chown -R food-ordering:food-ordering /opt/food-ordering/`

### کلاس‌های موجود
سیستم شامل کلاس‌های زیر است:
- **احراز هویت**: `AuthController`, `AuthService`, `AuthRepository`
- **مدیریت**: `AdminController`, `AdminService`, `AdminRepository`
- **سفارش**: `OrderController`, `OrderService`, `OrderRepository`
- **پرداخت**: `PaymentController`, `PaymentService`, `PaymentRepository`
- **رستوران**: `RestaurantController`, `RestaurantService`, `RestaurantRepository`
- **امنیت**: `AdvancedSecurityUtil`, `PasswordUtil`, `ValidationUtil`

---

## مشکلات پایگاه داده

### اتصال ناموفق

**تست اتصال:**
```bash
psql -h localhost -U food_ordering_user -d food_ordering_prod -c "SELECT 1"
```

**بررسی سرویس PostgreSQL:**
```bash
systemctl status postgresql
```

**راه‌حل‌ها:**
- شروع PostgreSQL: `sudo systemctl start postgresql`
- تأیید اطلاعات ورود در فایل پیکربندی
- بررسی محدودیت اتصالات در postgresql.conf

### عملکرد کند

**بررسی کوئری‌های کند:**
```sql
SELECT query, mean_time FROM pg_stat_statements 
ORDER BY mean_time DESC LIMIT 10;
```

**راه‌حل‌ها:**
- اجرای `ANALYZE;` برای به‌روزرسانی آمار
- اجرای `VACUUM ANALYZE;` برای نگهداری
- بررسی اندازه پایگاه داده و فضای دیسک موجود

---

## مشکلات شبکه

### API قابل دسترسی نیست

**بررسی گوش دادن سرویس:**
```bash
netstat -tulpn | grep 8081
```

**تست اتصال:**
```bash
curl http://localhost:8081/health
```

**راه‌حل‌ها:**
- باز کردن فایروال: `sudo ufw allow 8081/tcp`
- بررسی server.host در پیکربندی
- راه‌اندازی مجدد سرویس شبکه

---

## مشکلات فرانت‌اند

### برنامه اجرا نمی‌شود

**راه‌حل‌ها:**
- نصب JavaFX: `sudo apt install openjfx`
- تأیید نسخه جاوا: `java -version`
- اجرا با verbose: `java -jar target/food-ordering-frontend.jar -verbose`

### اتصال به بک‌اند ممکن نیست

**راه‌حل‌ها:**
- تأیید اجرای بک‌اند: `systemctl status food-ordering`
- تست سلامت بک‌اند: `curl http://localhost:8081/health`
- بررسی اتصال شبکه

---

## مشکلات عملکرد

### استفاده بالای حافظه

**بررسی حافظه:**
```bash
free -h
ps aux --sort=-%mem | head
```

**راه‌حل‌ها:**
- افزایش حافظه JVM: `export JAVA_OPTS="-Xmx4g -Xms2g"`
- استفاده از G1 garbage collector: `export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"`

---

## مشکلات امنیتی

### خطاهای احراز هویت

**بررسی JWT:**
```bash
# بررسی تنظیمات JWT در application.properties
grep jwt backend/src/main/resources/application.properties
```

**راه‌حل‌ها:**
- تنظیم مجدد JWT_SECRET
- بررسی تاریخ انقضای توکن‌ها
- پاک کردن کش نشست‌ها

### خطاهای رمزگذاری

**بررسی کلاس‌های امنیتی:**
- `AdvancedSecurityUtil`: رمزگذاری پیشرفته
- `PasswordUtil`: مدیریت رمزهای عبور
- `ValidationUtil`: اعتبارسنجی داده‌ها

**راه‌حل‌ها:**
- بررسی تنظیمات رمزگذاری
- به‌روزرسانی کلیدهای رمزگذاری
- بررسی مجوزهای فایل‌ها

---

## مشکلات تست

### تست‌ها اجرا نمی‌شوند

**راه‌حل‌ها:**
```bash
# تست‌های بک‌اند
cd backend
mvn test

# تست‌های فرانت‌اند
cd ../frontend-javafx
mvn test

# تست‌های امنیتی
mvn test -Dtest=*Security*Test

# تست‌های عملکرد
mvn test -Dtest=*Performance*Test
```

### خطاهای تست

**مشکلات رایج:**
- عدم دسترسی به پایگاه داده تست
- تنظیمات نادرست محیط تست
- عدم نصب وابستگی‌های تست

**راه‌حل‌ها:**
- بررسی `application-test.properties`
- اطمینان از نصب تمام وابستگی‌ها
- پاک کردن و بازسازی پروژه: `mvn clean install`

---

## بازیابی اضطراری

### بازیابی کامل سیستم

1. توقف سرویس‌ها: `sudo systemctl stop food-ordering postgresql`
2. بررسی سلامت سیستم: `df -h` و `free -h`
3. بازیابی از پشتیبان: `./backup-system.sh restore-db backup.sql.gz`
4. راه‌اندازی مجدد سرویس‌ها: `sudo systemctl start postgresql food-ordering`
5. تأیید: `curl http://localhost:8081/health`

### بازیابی پایگاه داده

**از پشتیبان:**
```bash
# بازیابی کامل
pg_restore -h localhost -U food_ordering_user -d food_ordering_prod backup.sql

# بازیابی انتخابی
pg_restore -h localhost -U food_ordering_user -d food_ordering_prod --table=users backup.sql
```

---

## لاگ‌ها و نظارت

### جمع‌آوری لاگ‌ها

**لاگ‌های برنامه:**
```bash
# لاگ‌های اخیر
journalctl -u food-ordering --since "1 hour ago" > app.log

# لاگ‌های خطا
journalctl -u food-ordering -p err --since "1 day ago" > errors.log

# بسته‌بندی لاگ‌ها
tar -czf logs-$(date +%Y%m%d).tar.gz *.log
```

**لاگ‌های پایگاه داده:**
```bash
# لاگ‌های PostgreSQL
sudo tail -f /var/log/postgresql/postgresql-*.log

# لاگ‌های امنیتی
sudo tail -f /var/log/auth.log
```

### نظارت عملکرد

**بررسی وضعیت سیستم:**
```bash
# وضعیت سرویس‌ها
systemctl status food-ordering postgresql

# استفاده از منابع
htop
iostat -x 1

# وضعیت شبکه
netstat -i
```

---

## دریافت کمک

### جمع‌آوری اطلاعات

**اطلاعات سیستم:**
```bash
# نسخه‌ها
java -version
mvn -version
psql --version

# وضعیت سرویس‌ها
systemctl status food-ordering postgresql

# لاگ‌های اخیر
journalctl -u food-ordering -n 50 > recent-logs.txt
```

### تماس با پشتیبانی

**اطلاعات تماس:**
- **ایمیل**: support@foodordering.com
- **تلفن**: 021-12345678
- **ساعات کاری**: 24/7

**ارسال گزارش:**
- فایل لاگ‌ها را ضمیمه کنید
- جزئیات مشکل را شرح دهید
- مراحل تکرار مشکل را بنویسید

---

## نتیجه‌گیری

سیستم سفارش غذا با تمام ویژگی‌های امنیتی و بهینه‌سازی آماده استفاده است. در صورت بروز مشکل، ابتدا این راهنما را بررسی کنید و سپس با پشتیبانی تماس بگیرید.

---
**آخرین به‌روزرسانی**: 15 ژوئن 2025  
**مسئول عیب‌یابی**: Food Ordering System Support Team 
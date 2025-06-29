# ⚙️ راهنمای مدیریت سیستم

راهنمای کامل برای مدیران سیستم و تیم‌های عملیاتی.

## مرور کلی

این راهنما شامل مدیریت سیستم، نظارت، نگهداری و عیب‌یابی برای سیستم سفارش غذا است.

---

## مدیریت سیستم

### کنترل سرویس

**لینوکس (systemd):**
```bash
# شروع/توقف/راه‌اندازی مجدد سرویس
sudo systemctl start food-ordering
sudo systemctl stop food-ordering
sudo systemctl restart food-ordering

# بررسی وضعیت
sudo systemctl status food-ordering

# مشاهده لاگ‌ها
sudo journalctl -u food-ordering -f
```

**ویندوز:**
```batch
# مدیریت سرویس
food-ordering-windows.bat start
food-ordering-windows.bat stop
food-ordering-windows.bat restart
food-ordering-windows.bat status
```

### مدیریت پیکربندی

**متغیرهای محیط:**
```bash
# تنظیمات تولیدی
export DATABASE_URL=jdbc:postgresql://localhost:5432/food_ordering_prod
export DATABASE_USERNAME=food_ordering_user
export DATABASE_PASSWORD=secure_password
export JWT_SECRET=256_bit_secret_key
export SERVER_PORT=8081
```

**فایل‌های پیکربندی کلیدی:**
- `application-production.properties` - پیکربندی اصلی
- `food-ordering.service` - سرویس سیستم
- `backup.conf` - تنظیمات پشتیبان‌گیری

---

## نظارت و بررسی سلامت

### سلامت سیستم

**Endpoint های سلامت:**
```bash
# بررسی سلامت پایه
curl http://localhost:8081/health

# اطلاعات جزئی سیستم
curl http://localhost:8081/health/detailed

# اتصال پایگاه داده
curl http://localhost:8081/health/database
```

**پاسخ‌های مورد انتظار:**
- `{"status":"UP","service":"food-ordering-backend"}` - سالم
- کد وضعیت HTTP 200
- زمان پاسخ < 200 میلی‌ثانیه

### نظارت بر لاگ‌ها

**مکان لاگ‌ها:**
- **لینوکس:** `/var/log/food-ordering/`
- **ویندوز:** `C:\food-ordering\logs\`
- **SystemD:** `journalctl -u food-ordering`

**نظارت بر لاگ‌ها:**
```bash
# نظارت لحظه‌ای
tail -f /var/log/food-ordering/food-ordering.log

# جستجوی خطاها
grep -i "error\|exception" /var/log/food-ordering/food-ordering.log

# بررسی آخرین 100 خط
tail -100 /var/log/food-ordering/food-ordering.log
```

### نظارت بر عملکرد

**منابع سیستم:**
```bash
# استفاده از CPU و حافظه
ps aux | grep food-ordering

# جزئیات حافظه
ps -p $(pgrep food-ordering) -o pid,vsz,rss,pmem

# اتصالات شبکه
netstat -tulpn | grep 8081
```

**نظارت بر پایگاه داده:**
```sql
-- اتصالات فعال
SELECT count(*) FROM pg_stat_activity 
WHERE datname = 'food_ordering_prod';

-- اندازه پایگاه داده
SELECT pg_size_pretty(pg_database_size('food_ordering_prod'));

-- کوئری‌های کند
SELECT query, mean_time FROM pg_stat_statements 
ORDER BY mean_time DESC LIMIT 10;
```

---

## مدیریت کاربران

### دسترسی به پنل مدیر
1. ورود با اطلاعات مدیر
2. دسترسی به پنل مدیر در `/admin`
3. عملکردهای مدیریتی در دسترس

### عملیات کاربران

**مشاهده کاربران:**
- فهرست کامل کاربران با وضعیت
- فیلتر بر اساس نقش و وضعیت
- جستجو با نام یا تلفن

**اقدامات کاربری:**
- فعال/غیرفعال کردن حساب‌ها
- تغییر نقش‌های کاربری
- بازنشانی رمز عبور
- مشاهده فعالیت کاربران

### مدیریت رستوران‌ها

**تایید رستوران:**
1. بررسی درخواست‌های رستوران جدید
2. تأیید اطلاعات کسب‌وکار
3. تایید یا رد درخواست‌ها

**نظارت بر رستوران‌ها:**
- نظارت بر عملکرد رستوران‌ها
- رسیدگی به شکایات مشتریان
- تعلیق رستوران‌های متخلف

---

## مدیریت پایگاه داده

### عملیات پشتیبان‌گیری

**پشتیبان‌گیری دستی:**
```bash
# پشتیبان‌گیری کامل سیستم
./backup-system.sh backup

# فقط پایگاه داده
pg_dump -h localhost -U food_ordering_backup food_ordering_prod > backup.sql
```

**پشتیبان‌گیری خودکار:**
```bash
# زمان‌بندی در crontab
0 2 * * * /opt/food-ordering/backup-system.sh backup

# تأیید زمان‌بندی پشتیبان‌گیری
crontab -l
```

**تأیید پشتیبان‌گیری:**
```bash
# فهرست پشتیبان‌های موجود
./backup-system.sh list-backups

# تأیید یکپارچگی پشتیبان
./backup-system.sh verify /path/to/backup
```

### نگهداری پایگاه داده

**وظایف روزانه:**
```sql
-- به‌روزرسانی آمار
ANALYZE;

-- بررسی اندازه جداول
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

**وظایف هفتگی:**
```sql
-- Vacuum و تحلیل
VACUUM ANALYZE;

-- پاک‌سازی لاگ‌های قدیمی audit (نگه‌داری 90 روز)
SELECT clean_audit_logs();
```

### بازیابی داده‌ها

**بازیابی پایگاه داده:**
```bash
# توقف برنامه
sudo systemctl stop food-ordering

# بازیابی از پشتیبان
psql -h localhost -U food_ordering_user -d food_ordering_prod < backup.sql

# شروع برنامه
sudo systemctl start food-ordering

# تأیید بازیابی
curl http://localhost:8081/health
```

---

## مدیریت امنیت

### کنترل دسترسی

**نقش‌های کاربری:**
- **CUSTOMER** - ثبت سفارش، مدیریت پروفایل
- **VENDOR** - مدیریت رستوران و سفارش‌ها
- **COURIER** - مدیریت تحویل‌ها
- **ADMIN** - مدیریت سیستم

**تنظیمات امنیتی:**
- انقضای JWT token: 24 ساعت
- الزامات رمز عبور: حداقل 4 کاراکتر
- زمان‌آوت session: 60 دقیقه
- محدودیت نرخ: 60 درخواست/دقیقه

### نظارت بر امنیت

**نظارت بر ورود ناموفق:**
```bash
# بررسی تلاش‌های احراز هویت ناموفق
grep "Authentication failed" /var/log/food-ordering/food-ordering.log

# نظارت بر فعالیت غیرعادی
grep -c "Authentication failed.*$(date +%Y-%m-%d)" \
    /var/log/food-ordering/food-ordering.log
```

**امنیت پایگاه داده:**
```sql
-- نظارت بر تلاش‌های ورود ناموفق
SELECT * FROM audit_log 
WHERE operation = 'LOGIN_FAILED' 
AND timestamp > NOW() - INTERVAL '24 hours';
```

### مدیریت SSL/HTTPS

**مدیریت گواهینامه:**
```bash
# بررسی انقضای گواهینامه
openssl x509 -in /etc/ssl/food-ordering/cert.pem -text -noout

# تمدید گواهینامه Let's Encrypt
certbot renew --dry-run
```

---

## بهینه‌سازی عملکرد

### تنظیم JVM

**تنظیمات حافظه:**
```bash
# گزینه‌های توصیه شده JVM
export JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
```

### بهینه‌سازی پایگاه داده

**نظارت بر ایندکس‌ها:**
```sql
-- بررسی استفاده از ایندکس
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes 
ORDER BY idx_scan DESC;

-- یافتن ایندکس‌های استفاده نشده
SELECT schemaname, tablename, indexname
FROM pg_stat_user_indexes 
WHERE idx_scan = 0;
```

**بهینه‌سازی کوئری:**
```sql
-- فعال‌سازی زمان‌سنجی کوئری
SET log_min_duration_statement = 1000;

-- نظارت بر کوئری‌های کند
SELECT query, total_time, calls, mean_time
FROM pg_stat_statements 
ORDER BY total_time DESC LIMIT 10;
```

---

## عیب‌یابی

### مشکلات رایج

**سرویس شروع نمی‌شود:**
```bash
# بررسی نسخه جاوا
java -version

# تأیید در دسترس بودن پورت
netstat -tulpn | grep 8081

# بررسی مجوزهای فایل
ls -la /opt/food-ordering/

# مشاهده لاگ‌های جزئی
sudo journalctl -u food-ordering -n 50
```

**مشکلات اتصال پایگاه داده:**
```bash
# تست اتصال پایگاه داده
psql -h localhost -U food_ordering_user -d food_ordering_prod -c "SELECT 1"

# بررسی وضعیت PostgreSQL
sudo systemctl status postgresql

# مشاهده لاگ‌های PostgreSQL
sudo tail -f /var/log/postgresql/postgresql-*.log
```

**استفاده بالای CPU/حافظه:**
```bash
# بررسی منابع سیستم
top -p $(pgrep food-ordering)

# تولید thread dump
jstack $(pgrep food-ordering) > thread-dump.txt

# تحلیل حافظه
jmap -dump:live,format=b,file=heap-dump.hprof $(pgrep food-ordering)
```

### رویه‌های اضطراری

**پاسخ به قطعی سرویس:**
1. بررسی وضعیت سرویس
2. بررسی لاگ‌های اخیر
3. راه‌اندازی مجدد سرویس در صورت نیاز
4. تأیید endpoint های سلامت
5. اطلاع‌رسانی به ذی‌نفعان

**اضطرار پایگاه داده:**
1. توقف فوری برنامه
2. ارزیابی یکپارچگی پایگاه داده
3. بازیابی از آخرین پشتیبان
4. تأیید سازگاری داده‌ها
5. راه‌اندازی مجدد سرویس‌ها

---

## گزارش‌گیری و تحلیل

### گزارش‌های سیستم

**گزارش‌های روزانه:**
```bash
# تولید گزارش روزانه
./daily-report.sh

# مشاهده آمار سیستم
curl http://localhost:8081/metrics
```

**تحلیل کسب‌وکار:**
```sql
-- خلاصه سفارش‌های روزانه
SELECT DATE(created_at) as date, COUNT(*) as orders, 
       SUM(total_amount) as revenue
FROM orders 
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY DATE(created_at);

-- برترین رستوران‌ها
SELECT r.name, COUNT(o.id) as orders, SUM(o.total_amount) as revenue
FROM restaurants r
JOIN orders o ON r.id = o.restaurant_id
WHERE o.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY r.id, r.name
ORDER BY revenue DESC;
```

---

## برنامه نگهداری

### وظایف روزانه
- [ ] بررسی سلامت سرویس
- [ ] بررسی لاگ‌های خطا
- [ ] نظارت بر منابع سیستم
- [ ] تأیید تکمیل پشتیبان‌گیری

### وظایف هفتگی
- [ ] نگهداری پایگاه داده (VACUUM ANALYZE)
- [ ] پاک‌سازی فایل‌های لاگ قدیمی
- [ ] بررسی لاگ‌های امنیتی
- [ ] به‌روزرسانی آمار سیستم

### وظایف ماهانه
- [ ] به‌روزرسانی‌های امنیتی
- [ ] بررسی تمدید گواهینامه
- [ ] تحلیل عملکرد
- [ ] بررسی برنامه‌ریزی ظرفیت

---

## تماس و ارجاع

### پشتیبانی داخلی
- **مدیر سیستم:** admin@company.com
- **مدیر پایگاه داده:** dba@company.com
- **تیم امنیت:** security@company.com

### فروشندگان خارجی
- **پشتیبانی پایگاه داده:** پشتیبانی PostgreSQL
- **گواهینامه‌های SSL:** مرجع صدور گواهینامه
- **ارائه‌دهنده ابری:** در صورت استفاده از زیرساخت ابری

---

**نسخه:** 1.0  
**آخرین به‌روزرسانی:** دسامبر 2024  
**بررسی بعدی:** مارس 2025 
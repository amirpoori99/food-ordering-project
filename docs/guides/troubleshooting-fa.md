# 🔧 راهنمای عیب‌یابی

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
- اجرا با verbose: `java -jar food-ordering-frontend.jar -verbose`

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

## بازیابی اضطراری

### بازیابی کامل سیستم

1. توقف سرویس‌ها: `sudo systemctl stop food-ordering postgresql`
2. بررسی سلامت سیستم: `df -h` و `free -h`
3. بازیابی از پشتیبان: `./backup-system.sh restore-db backup.sql.gz`
4. راه‌اندازی مجدد سرویس‌ها: `sudo systemctl start postgresql food-ordering`
5. تأیید: `curl http://localhost:8081/health`

---

## دریافت کمک

**جمع‌آوری لاگ‌ها:**
```bash
journalctl -u food-ordering --since "1 hour ago" > app.log
tar -czf logs-$(date +%Y%m%d).tar.gz *.log
```

**تماس:** support@foodordering.com

---

**نسخه:** 1.0 
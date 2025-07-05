# 🚀 Production Deployment Guide - Food Ordering System

> **راهنمای کامل استقرار تولید سیستم سفارش غذا**

---

## 📋 فهرست مطالب

1. [پیش‌نیازها](#پیشنیازها)
2. [آماده‌سازی سرور](#آمادهسازی-سرور)
3. [نصب و پیکربندی پایگاه داده](#نصب-و-پیکربندی-پایگاه-داده)
4. [نصب و پیکربندی Redis](#نصب-و-پیکربندی-redis)
5. [نصب و پیکربندی Nginx](#نصب-و-پیکربندی-nginx)
6. [استقرار اپلیکیشن](#استقرار-اپلیکیشن)
7. [نصب سیستم مانیتورینگ](#نصب-سیستم-مانیتورینگ)
8. [تست و راه‌اندازی](#تست-و-راهاندازی)
9. [بهینه‌سازی‌ها](#بهینهسازیها)
10. [عیب‌یابی](#عیبیابی)

---

## 🔧 پیش‌نیازها

### سیستم‌عامل و سخت‌افزار
- **سیستم‌عامل**: Ubuntu 20.04 LTS یا CentOS 8+
- **CPU**: حداقل 4 cores (توصیه: 8 cores)
- **RAM**: حداقل 8GB (توصیه: 16GB)
- **Storage**: حداقل 100GB SSD (توصیه: 200GB SSD)
- **Network**: 1 Gbps bandwidth

### نرم‌افزارها
- **Java**: OpenJDK 17+
- **Maven**: 3.8+
- **PostgreSQL**: 15+
- **Redis**: 7+
- **Nginx**: 1.18+
- **Prometheus**: 2.40+
- **Grafana**: 9.0+

---

## 🖥️ آماده‌سازی سرور

### مرحله 1: به‌روزرسانی سیستم
```bash
# Ubuntu/Debian
sudo apt update && sudo apt upgrade -y

# CentOS/RHEL
sudo yum update -y
```

### مرحله 2: نصب Java 17
```bash
# Ubuntu/Debian
sudo apt install openjdk-17-jdk -y

# CentOS/RHEL
sudo yum install java-17-openjdk-devel -y

# تأیید نصب
java -version
```

### مرحله 3: نصب Maven
```bash
# Ubuntu/Debian
sudo apt install maven -y

# CentOS/RHEL
sudo yum install maven -y

# تأیید نصب
mvn -version
```

### مرحله 4: ایجاد کاربر اپلیکیشن
```bash
# ایجاد کاربر غیر root
sudo adduser foodordering
sudo usermod -aG sudo foodordering

# ایجاد directory های مورد نیاز
sudo mkdir -p /opt/food-ordering
sudo mkdir -p /var/log/food-ordering
sudo mkdir -p /var/lib/food-ordering
sudo chown -R foodordering:foodordering /opt/food-ordering
sudo chown -R foodordering:foodordering /var/log/food-ordering
sudo chown -R foodordering:foodordering /var/lib/food-ordering
```

---

## 🗄️ نصب و پیکربندی پایگاه داده

### مرحله 1: نصب PostgreSQL
```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib -y

# CentOS/RHEL
sudo yum install postgresql-server postgresql-contrib -y
sudo postgresql-setup initdb
```

### مرحله 2: پیکربندی PostgreSQL
```bash
# شروع سرویس
sudo systemctl start postgresql
sudo systemctl enable postgresql

# ایجاد database و user
sudo -u postgres psql
```

```sql
-- در PostgreSQL console
CREATE DATABASE food_ordering_prod;
CREATE USER food_ordering_user WITH ENCRYPTED PASSWORD 'secure_password_2024';
GRANT ALL PRIVILEGES ON DATABASE food_ordering_prod TO food_ordering_user;

-- Performance optimizations
ALTER DATABASE food_ordering_prod SET shared_preload_libraries = 'pg_stat_statements';
ALTER DATABASE food_ordering_prod SET max_connections = 200;
ALTER DATABASE food_ordering_prod SET shared_buffers = '256MB';
ALTER DATABASE food_ordering_prod SET effective_cache_size = '1GB';
ALTER DATABASE food_ordering_prod SET work_mem = '4MB';
ALTER DATABASE food_ordering_prod SET maintenance_work_mem = '64MB';

-- اتصال به database
\c food_ordering_prod;

-- فعال‌سازی extensions
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
CREATE EXTENSION IF NOT EXISTS uuid-ossp;

\q
```

### مرحله 3: پیکربندی أمنیت PostgreSQL
```bash
# ویرایش postgresql.conf
sudo nano /etc/postgresql/15/main/postgresql.conf

# تنظیمات کلیدی:
# listen_addresses = 'localhost'
# port = 5432
# max_connections = 200
# shared_buffers = 256MB
# effective_cache_size = 1GB
# work_mem = 4MB
# maintenance_work_mem = 64MB
# checkpoint_completion_target = 0.7
# wal_buffers = 16MB
# default_statistics_target = 100
# random_page_cost = 1.1
# effective_io_concurrency = 200

# ویرایش pg_hba.conf
sudo nano /etc/postgresql/15/main/pg_hba.conf

# اضافه کردن:
# local   food_ordering_prod   food_ordering_user   md5
# host    food_ordering_prod   food_ordering_user   127.0.0.1/32   md5

# راه‌اندازی مجدد
sudo systemctl restart postgresql
```

---

## 💾 نصب و پیکربندی Redis

### مرحله 1: نصب Redis
```bash
# Ubuntu/Debian
sudo apt install redis-server -y

# CentOS/RHEL
sudo yum install redis -y
```

### مرحله 2: پیکربندی Redis
```bash
# ویرایش redis.conf
sudo nano /etc/redis/redis.conf

# تنظیمات کلیدی:
# bind 127.0.0.1
# port 6379
# timeout 300
# keepalive 300
# maxmemory 2gb
# maxmemory-policy allkeys-lru
# maxmemory-samples 5
# save 900 1
# save 300 10
# save 60 10000
# requirepass redis_secure_password_2024
# tcp-keepalive 300
# databases 16

# راه‌اندازی سرویس
sudo systemctl start redis-server
sudo systemctl enable redis-server

# تست Redis
redis-cli ping
```

---

## 🌐 نصب و پیکربندی Nginx

### مرحله 1: نصب Nginx
```bash
# Ubuntu/Debian
sudo apt install nginx -y

# CentOS/RHEL
sudo yum install nginx -y
```

### مرحله 2: پیکربندی Nginx
```bash
# کپی کردن فایل پیکربندی
sudo cp /opt/food-ordering/config/nginx/nginx-production.conf /etc/nginx/sites-available/food-ordering

# فعال‌سازی سایت
sudo ln -s /etc/nginx/sites-available/food-ordering /etc/nginx/sites-enabled/

# غیرفعال کردن سایت پیش‌فرض
sudo rm /etc/nginx/sites-enabled/default

# تست پیکربندی
sudo nginx -t

# راه‌اندازی سرویس
sudo systemctl start nginx
sudo systemctl enable nginx
```

### مرحله 3: تنظیم SSL Certificate
```bash
# نصب Certbot
sudo apt install certbot python3-certbot-nginx -y

# دریافت SSL certificate
sudo certbot --nginx -d food-ordering.com -d www.food-ordering.com

# تنظیم auto-renewal
sudo crontab -e
# اضافه کردن:
# 0 12 * * * /usr/bin/certbot renew --quiet
```

---

## 🚀 استقرار اپلیکیشن

### مرحله 1: آماده‌سازی کد
```bash
# تبدیل به کاربر foodordering
sudo su - foodordering

# کلون کردن repository
git clone https://github.com/your-org/food-ordering-system.git /opt/food-ordering/app
cd /opt/food-ordering/app

# build اپلیکیشن
cd backend
mvn clean package -DskipTests
```

### مرحله 2: تنظیم Environment Variables
```bash
# ایجاد فایل environment
cat > /opt/food-ordering/app/.env << EOF
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/food_ordering_prod
DATABASE_USERNAME=food_ordering_user
DATABASE_PASSWORD=secure_password_2024
DB_MAX_POOL_SIZE=100
DB_MIN_POOL_SIZE=20

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_secure_password_2024
REDIS_DATABASE=0

# Application Configuration
APP_ENVIRONMENT=production
SERVER_PORT=8081
JWT_SECRET=your-super-secret-jwt-key-2024
ENCRYPTION_KEY=your-encryption-key-2024

# External Services
PAYMENT_GATEWAY_URL=https://payment-gateway.com/api
NOTIFICATION_SERVICE_URL=https://notifications.com/api
EOF

# محافظت از فایل environment
chmod 600 /opt/food-ordering/app/.env
```

### مرحله 3: ایجاد Systemd Service
```bash
# ایجاد سرویس برای instance اول
sudo cat > /etc/systemd/system/food-ordering-app1.service << EOF
[Unit]
Description=Food Ordering System - Instance 1
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=foodordering
Group=foodordering
WorkingDirectory=/opt/food-ordering/app/backend
ExecStart=/usr/bin/java -Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Dserver.port=8081 -Dapp.environment=production -jar target/food-ordering-backend-1.0.0.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=food-ordering-app1
Environment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

[Install]
WantedBy=multi-user.target
EOF

# ایجاد سرویس‌های مشابه برای instance های دیگر
sudo sed 's/app1/app2/g; s/8081/8082/g' /etc/systemd/system/food-ordering-app1.service > /etc/systemd/system/food-ordering-app2.service
sudo sed 's/app1/app3/g; s/8081/8083/g' /etc/systemd/system/food-ordering-app1.service > /etc/systemd/system/food-ordering-app3.service
sudo sed 's/app1/app4/g; s/8081/8084/g' /etc/systemd/system/food-ordering-app1.service > /etc/systemd/system/food-ordering-app4.service

# فعال‌سازی و راه‌اندازی سرویس‌ها
sudo systemctl daemon-reload
sudo systemctl enable food-ordering-app1 food-ordering-app2 food-ordering-app3 food-ordering-app4
sudo systemctl start food-ordering-app1 food-ordering-app2 food-ordering-app3 food-ordering-app4
```

### مرحله 4: راه‌اندازی Database Schema
```bash
# اجرای migration scripts
cd /opt/food-ordering/app/backend
java -Dapp.environment=production -jar target/food-ordering-backend-1.0.0.jar --init-database
```

---

## 📊 نصب سیستم مانیتورینگ

### مرحله 1: نصب Prometheus
```bash
# ایجاد کاربر prometheus
sudo adduser --system --no-create-home --shell /bin/false prometheus

# دانلود Prometheus
wget https://github.com/prometheus/prometheus/releases/download/v2.40.0/prometheus-2.40.0.linux-amd64.tar.gz
tar -xzf prometheus-2.40.0.linux-amd64.tar.gz
sudo mv prometheus-2.40.0.linux-amd64 /opt/prometheus
sudo chown -R prometheus:prometheus /opt/prometheus

# کپی فایل پیکربندی
sudo cp /opt/food-ordering/config/monitoring/prometheus.yml /opt/prometheus/prometheus.yml

# ایجاد systemd service
sudo cat > /etc/systemd/system/prometheus.service << EOF
[Unit]
Description=Prometheus
After=network.target

[Service]
Type=simple
User=prometheus
ExecStart=/opt/prometheus/prometheus --config.file=/opt/prometheus/prometheus.yml --storage.tsdb.path=/opt/prometheus/data
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# راه‌اندازی سرویس
sudo systemctl daemon-reload
sudo systemctl enable prometheus
sudo systemctl start prometheus
```

### مرحله 2: نصب Grafana
```bash
# نصب Grafana
wget -q -O - https://packages.grafana.com/gpg.key | sudo apt-key add -
echo "deb https://packages.grafana.com/oss/deb stable main" | sudo tee -a /etc/apt/sources.list.d/grafana.list
sudo apt update
sudo apt install grafana -y

# راه‌اندازی سرویس
sudo systemctl enable grafana-server
sudo systemctl start grafana-server
```

### مرحله 3: نصب Node Exporter
```bash
# دانلود Node Exporter
wget https://github.com/prometheus/node_exporter/releases/download/v1.4.0/node_exporter-1.4.0.linux-amd64.tar.gz
tar -xzf node_exporter-1.4.0.linux-amd64.tar.gz
sudo mv node_exporter-1.4.0.linux-amd64/node_exporter /usr/local/bin/

# ایجاد systemd service
sudo cat > /etc/systemd/system/node_exporter.service << EOF
[Unit]
Description=Node Exporter
After=network.target

[Service]
Type=simple
ExecStart=/usr/local/bin/node_exporter
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# راه‌اندازی سرویس
sudo systemctl daemon-reload
sudo systemctl enable node_exporter
sudo systemctl start node_exporter
```

---

## 🧪 تست و راه‌اندازی

### مرحله 1: تست سرویس‌ها
```bash
# تست وضعیت سرویس‌ها
sudo systemctl status postgresql
sudo systemctl status redis-server
sudo systemctl status nginx
sudo systemctl status food-ordering-app1
sudo systemctl status food-ordering-app2
sudo systemctl status food-ordering-app3
sudo systemctl status food-ordering-app4
sudo systemctl status prometheus
sudo systemctl status grafana-server
```

### مرحله 2: تست API Endpoints
```bash
# تست health endpoint
curl -f http://localhost:8081/health
curl -f http://localhost:8082/health
curl -f http://localhost:8083/health
curl -f http://localhost:8084/health

# تست API endpoints
curl -f http://localhost:8081/api/restaurants
curl -f http://localhost:8081/api/auth/health

# تست Load Balancer
curl -f https://food-ordering.com/health
curl -f https://food-ordering.com/api/restaurants
```

### مرحله 3: تست Database Connection
```bash
# تست اتصال به database
sudo -u postgres psql -d food_ordering_prod -c "SELECT COUNT(*) FROM users;"

# تست Redis connection
redis-cli -a redis_secure_password_2024 ping
```

---

## ⚡ بهینه‌سازی‌ها

### مرحله 1: تنظیمات سیستم‌عامل
```bash
# تنظیمات kernel parameters
sudo cat >> /etc/sysctl.conf << EOF
# Network optimizations
net.core.rmem_max = 134217728
net.core.wmem_max = 134217728
net.ipv4.tcp_rmem = 4096 87380 134217728
net.ipv4.tcp_wmem = 4096 65536 134217728
net.ipv4.tcp_window_scaling = 1
net.ipv4.tcp_congestion_control = bbr

# File descriptor limits
fs.file-max = 65536
EOF

# اعمال تنظیمات
sudo sysctl -p
```

### مرحله 2: تنظیمات JVM
```bash
# ایجاد فایل JVM options
cat > /opt/food-ordering/app/jvm-options.txt << EOF
-Xms2g
-Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Xloggc:/var/log/food-ordering/gc.log
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=10
-XX:GCLogFileSize=10M
EOF
```

---

## 🔍 عیب‌یابی

### مشکلات رایج و راه‌حل‌ها

#### 1. اپلیکیشن راه‌اندازی نمی‌شود
```bash
# بررسی logs
sudo journalctl -u food-ordering-app1 -f
sudo journalctl -u food-ordering-app2 -f

# بررسی port availability
sudo netstat -tlnp | grep :8081
sudo netstat -tlnp | grep :8082
```

#### 2. مشکل اتصال به database
```bash
# بررسی PostgreSQL status
sudo systemctl status postgresql

# بررسی connectivity
sudo -u postgres psql -c "SELECT version();"

# بررسی logs
sudo tail -f /var/log/postgresql/postgresql-15-main.log
```

#### 3. مشکل Redis cache
```bash
# بررسی Redis status
sudo systemctl status redis-server

# بررسی Redis logs
sudo tail -f /var/log/redis/redis-server.log

# تست Redis
redis-cli -a redis_secure_password_2024 info
```

#### 4. مشکل Nginx
```bash
# بررسی Nginx status
sudo systemctl status nginx

# بررسی Nginx logs
sudo tail -f /var/log/nginx/error.log
sudo tail -f /var/log/nginx/access.log

# تست Nginx config
sudo nginx -t
```

### Performance Monitoring Commands
```bash
# بررسی استفاده از CPU و Memory
htop

# بررسی I/O
iostat -x 1

# بررسی network connections
ss -tulpn

# بررسی disk usage
df -h
du -sh /opt/food-ordering/

# بررسی process های Java
jps -v
```

---

## 📋 Checklist نهایی

### ✅ Pre-deployment Checklist
- [ ] تمام dependencies نصب شده‌اند
- [ ] Database ایجاد و پیکربندی شده
- [ ] Redis راه‌اندازی شده
- [ ] Nginx پیکربندی شده
- [ ] SSL certificates راه‌اندازی شده
- [ ] Environment variables تنظیم شده‌اند
- [ ] Systemd services ایجاد شده‌اند

### ✅ Post-deployment Checklist
- [ ] تمام سرویس‌ها در حال اجرا هستند
- [ ] Health checks پاس می‌شوند
- [ ] API endpoints قابل دسترسی هستند
- [ ] Database connectivity کار می‌کند
- [ ] Redis cache کار می‌کند
- [ ] Load balancer کار می‌کند
- [ ] Monitoring system راه‌اندازی شده
- [ ] Logs در حال ثبت هستند
- [ ] Backups پیکربندی شده‌اند

---

## 🎯 مرحله بعدی

پس از تکمیل موفقیت‌آمیز deployment، به مراحل زیر بروید:

1. **User Acceptance Testing (UAT)**
2. **Performance Testing**
3. **Security Scanning**
4. **Backup and Recovery Testing**
5. **Monitoring Setup Verification**

---

## 📞 پشتیبانی

برای پشتیبانی و سوالات:
- **Email**: support@food-ordering.com
- **Documentation**: https://docs.food-ordering.com
- **GitHub Issues**: https://github.com/your-org/food-ordering-system/issues

---

**آخرین به‌روزرسانی**: نوامبر 2024
**نسخه**: 1.0.0
**وضعیت**: Production Ready 
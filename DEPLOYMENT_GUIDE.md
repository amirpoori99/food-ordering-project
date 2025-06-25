# 🚀 **Food Ordering System - Production Deployment Guide**

## 📋 **Overview**

این راهنما برای استقرار سیستم سفارش غذا در محیط production طراحی شده است. سیستم با استفاده از **Pure Java** (بدون Spring Framework) و **JavaFX** توسعه یافته و بدون نیاز به Docker قابل استقرار است.

## 🏗️ **Architecture Overview**

```
Production Architecture (No Docker/Spring):
├── Java Application Server (Built-in HttpServer)
├── SQLite Database (Development) / PostgreSQL (Production)
├── JavaFX Desktop Client
├── Reverse Proxy (Nginx) - Optional
└── System Service Management (systemd/Windows Service)
```

## 📋 **Prerequisites**

### **Server Requirements**
- **Operating System:** Linux (Ubuntu 20.04+/CentOS 8+) or Windows Server 2019+
- **Java Runtime:** JDK 17 or higher
- **Memory:** Minimum 2GB RAM (Recommended: 4GB RAM)
- **Storage:** 10GB free space (5GB for application + 5GB for database)
- **Network:** Port 8081 available for API server
- **Database:** SQLite (included) or PostgreSQL 12+ for production

### **Client Requirements**
- **Java Runtime:** JDK 17+ with JavaFX support
- **Operating System:** Windows 10+, macOS 10.14+, or Linux with GUI
- **Memory:** Minimum 1GB RAM
- **Network:** Internet connection to reach API server

## 🛠️ **Server Deployment**

### **Step 1: Java Installation**

#### **Ubuntu/Debian:**
```bash
# Update package list
sudo apt update

# Install OpenJDK 17
sudo apt install openjdk-17-jdk

# Verify installation
java -version
javac -version
```

#### **CentOS/RHEL:**
```bash
# Install OpenJDK 17
sudo yum install java-17-openjdk java-17-openjdk-devel

# Verify installation
java -version
```

#### **Windows Server:**
1. Download OpenJDK 17 from [Adoptium](https://adoptium.net/)
2. Install using the MSI installer
3. Add Java to PATH environment variable
4. Verify: `java -version` in Command Prompt

### **Step 2: Application Deployment**

#### **Build Application**
```bash
# Clone repository
git clone <repository-url>
cd food-ordering-project

# Build backend
cd backend
mvn clean package -DskipTests

# Build frontend
cd ../frontend-javafx
mvn clean package -DskipTests
```

#### **Create Application Directory**
```bash
# Linux
sudo mkdir -p /opt/food-ordering
sudo mkdir -p /opt/food-ordering/backend
sudo mkdir -p /opt/food-ordering/frontend
sudo mkdir -p /opt/food-ordering/logs
sudo mkdir -p /opt/food-ordering/data

# Windows (as Administrator)
mkdir C:\food-ordering
mkdir C:\food-ordering\backend
mkdir C:\food-ordering\frontend
mkdir C:\food-ordering\logs
mkdir C:\food-ordering\data
```

#### **Copy Application Files**
```bash
# Linux
sudo cp backend/target/food-ordering-backend-1.0.jar /opt/food-ordering/backend/
sudo cp frontend-javafx/target/food-ordering-frontend-1.0.jar /opt/food-ordering/frontend/
sudo cp food_ordering.db /opt/food-ordering/data/

# Windows
copy backend\target\food-ordering-backend-1.0.jar C:\food-ordering\backend\
copy frontend-javafx\target\food-ordering-frontend-1.0.jar C:\food-ordering\frontend\
copy food_ordering.db C:\food-ordering\data\
```

### **Step 3: Database Setup**

#### **Option A: SQLite (Development/Small Scale)**
```bash
# Database file is automatically created
# No additional setup required
# File location: /opt/food-ordering/data/food_ordering.db
```

#### **Option B: PostgreSQL (Production/Large Scale)**
```bash
# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Create database and user
sudo -u postgres psql
CREATE DATABASE food_ordering;
CREATE USER foodapp WITH ENCRYPTED PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE food_ordering TO foodapp;
\q

# Create tables (run application once to auto-generate schema)
```

### **Step 4: Configuration**

#### **Create Configuration File**
```bash
# Linux
sudo nano /opt/food-ordering/backend/application.properties
```

```properties
# Database Configuration
db.url=jdbc:sqlite:/opt/food-ordering/data/food_ordering.db
# For PostgreSQL:
# db.url=jdbc:postgresql://localhost:5432/food_ordering
# db.username=foodapp
# db.password=secure_password

# Server Configuration
server.port=8081
server.host=0.0.0.0

# JWT Configuration
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=86400
jwt.refresh.expiration=604800

# Logging Configuration
log.level=INFO
log.file=/opt/food-ordering/logs/application.log

# Performance Configuration
cache.ttl=900
cache.maxsize=1000
thread.pool.size=20
```

#### **Create Logging Configuration**
```bash
# Linux
sudo nano /opt/food-ordering/backend/logback.xml
```

```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/opt/food-ordering/logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/opt/food-ordering/logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

### **Step 5: System Service Configuration**

#### **Linux (systemd)**
```bash
# Create service file
sudo nano /etc/systemd/system/food-ordering.service
```

```ini
[Unit]
Description=Food Ordering System Backend
After=network.target

[Service]
Type=simple
User=foodapp
Group=foodapp
WorkingDirectory=/opt/food-ordering/backend
ExecStart=/usr/bin/java -Xmx2g -Xms1g -jar food-ordering-backend-1.0.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=always
RestartSec=5
StandardOutput=journal
StandardError=journal
SyslogIdentifier=food-ordering
KillMode=mixed
KillSignal=SIGTERM

# Environment variables
Environment=JAVA_OPTS="-Xmx2g -Xms1g"
Environment=APP_ENV=production

[Install]
WantedBy=multi-user.target
```

```bash
# Create application user
sudo useradd -r -s /bin/false foodapp
sudo chown -R foodapp:foodapp /opt/food-ordering

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable food-ordering
sudo systemctl start food-ordering

# Check status
sudo systemctl status food-ordering
```

#### **Windows Service**
```batch
REM Install as Windows Service using sc command
sc create "Food Ordering System" binPath= "java -jar C:\food-ordering\backend\food-ordering-backend-1.0.jar" start= auto

REM Start service
sc start "Food Ordering System"

REM Check status
sc query "Food Ordering System"
```

### **Step 6: Nginx Reverse Proxy (Optional)**

#### **Install and Configure Nginx**
```bash
# Install Nginx
sudo apt install nginx

# Create configuration
sudo nano /etc/nginx/sites-available/food-ordering
```

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL Configuration
    ssl_certificate /path/to/your/certificate.pem;
    ssl_certificate_key /path/to/your/private.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    
    # Security Headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";
    
    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    
    location /api/ {
        limit_req zone=api burst=20 nodelay;
        
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
    }
    
    # Health check
    location /health {
        proxy_pass http://localhost:8081/health;
        access_log off;
    }
}
```

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/food-ordering /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## 💻 **Client Deployment**

### **Desktop Application Distribution**

#### **Option A: JAR Distribution**
```bash
# Create distribution package
mkdir food-ordering-client
cp frontend-javafx/target/food-ordering-frontend-1.0.jar food-ordering-client/

# Create startup script (Linux/Mac)
cat > food-ordering-client/start.sh << 'EOF'
#!/bin/bash
java -Xmx1g -jar food-ordering-frontend-1.0.jar
EOF
chmod +x food-ordering-client/start.sh

# Create startup script (Windows)
cat > food-ordering-client/start.bat << 'EOF'
@echo off
java -Xmx1g -jar food-ordering-frontend-1.0.jar
pause
EOF

# Create configuration file
cat > food-ordering-client/client.properties << 'EOF'
api.base.url=https://your-domain.com/api
api.timeout=30000
cache.enabled=true
cache.size=1000
EOF
```

#### **Option B: Native Application (jpackage)**
```bash
# Create native installer (requires JDK 17+)
jpackage \
  --input frontend-javafx/target \
  --name "Food Ordering System" \
  --main-jar food-ordering-frontend-1.0.jar \
  --main-class com.myapp.ui.MainApp \
  --type dmg \
  --app-version 1.0 \
  --vendor "Your Company" \
  --description "Food Ordering System Client"

# For Windows (.exe)
jpackage \
  --input frontend-javafx/target \
  --name "Food Ordering System" \
  --main-jar food-ordering-frontend-1.0.jar \
  --main-class com.myapp.ui.MainApp \
  --type exe \
  --win-dir-chooser \
  --win-menu \
  --win-shortcut
```

## 🔒 **Security Configuration**

### **SSL/HTTPS Setup**

#### **Option A: Self-Signed Certificate (Development)**
```bash
# Generate self-signed certificate
keytool -genkeypair -alias foodordering -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 365

# Update application configuration
echo "server.ssl.enabled=true" >> application.properties
echo "server.ssl.keystore=keystore.p12" >> application.properties
echo "server.ssl.keystore-password=your_password" >> application.properties
```

#### **Option B: Let's Encrypt (Production)**
```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

### **Firewall Configuration**

#### **Linux (UFW)**
```bash
# Enable firewall
sudo ufw enable

# Allow SSH
sudo ufw allow ssh

# Allow HTTP/HTTPS
sudo ufw allow 80
sudo ufw allow 443

# Allow API port (if direct access needed)
sudo ufw allow 8081

# Check status
sudo ufw status
```

#### **Windows Firewall**
```powershell
# Allow API port
New-NetFirewallRule -DisplayName "Food Ordering API" -Direction Inbound -Protocol TCP -LocalPort 8081 -Action Allow

# Allow HTTP/HTTPS
New-NetFirewallRule -DisplayName "HTTP" -Direction Inbound -Protocol TCP -LocalPort 80 -Action Allow
New-NetFirewallRule -DisplayName "HTTPS" -Direction Inbound -Protocol TCP -LocalPort 443 -Action Allow
```

## 📊 **Monitoring & Health Checks**

### **Health Check Endpoints**
```bash
# Basic health check
curl http://localhost:8081/health

# Detailed health check
curl http://localhost:8081/health/detailed

# Database connectivity
curl http://localhost:8081/health/database

# System metrics
curl http://localhost:8081/metrics
```

### **Log Monitoring**

#### **Log Rotation Setup**
```bash
# Create logrotate configuration
sudo nano /etc/logrotate.d/food-ordering
```

```
/opt/food-ordering/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    sharedscripts
    postrotate
        systemctl reload food-ordering
    endscript
}
```

#### **Log Analysis**
```bash
# Monitor real-time logs
tail -f /opt/food-ordering/logs/application.log

# Search for errors
grep ERROR /opt/food-ordering/logs/application.log

# Monitor performance
grep "Performance" /opt/food-ordering/logs/application.log | tail -100
```

## 💾 **Backup & Recovery**

### **Database Backup**

#### **SQLite Backup**
```bash
# Create backup script
cat > /opt/food-ordering/scripts/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/food-ordering/backups"
DB_FILE="/opt/food-ordering/data/food_ordering.db"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR
sqlite3 $DB_FILE ".backup $BACKUP_DIR/food_ordering_$DATE.db"
find $BACKUP_DIR -name "*.db" -mtime +30 -delete
EOF

chmod +x /opt/food-ordering/scripts/backup.sh

# Schedule daily backup
crontab -e
# Add: 0 2 * * * /opt/food-ordering/scripts/backup.sh
```

#### **PostgreSQL Backup**
```bash
# Create PostgreSQL backup script
cat > /opt/food-ordering/scripts/pg_backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/food-ordering/backups"
DB_NAME="food_ordering"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR
pg_dump -h localhost -U foodapp $DB_NAME | gzip > $BACKUP_DIR/food_ordering_$DATE.sql.gz
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
EOF
```

### **Application Backup**
```bash
# Create full system backup
tar -czf food-ordering-backup-$(date +%Y%m%d).tar.gz \
  /opt/food-ordering/backend \
  /opt/food-ordering/frontend \
  /opt/food-ordering/data \
  /opt/food-ordering/logs \
  /etc/systemd/system/food-ordering.service \
  /etc/nginx/sites-available/food-ordering
```

## 🚀 **Performance Optimization**

### **JVM Tuning**
```bash
# Update service file with optimized JVM options
ExecStart=/usr/bin/java \
  -Xmx4g -Xms2g \
  -XX:+UseG1GC \
  -XX:G1HeapRegionSize=16m \
  -XX:+UseStringDeduplication \
  -XX:+OptimizeStringConcat \
  -Djava.awt.headless=true \
  -jar food-ordering-backend-1.0.jar
```

### **Database Optimization**

#### **SQLite Optimization**
```sql
-- Enable WAL mode for better concurrency
PRAGMA journal_mode=WAL;

-- Optimize cache size
PRAGMA cache_size=10000;

-- Enable foreign keys
PRAGMA foreign_keys=ON;
```

#### **PostgreSQL Optimization**
```sql
-- Update postgresql.conf
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
```

## 🔧 **Troubleshooting**

### **Common Issues**

#### **Application Won't Start**
```bash
# Check logs
journalctl -u food-ordering -f

# Check Java version
java -version

# Check port availability
netstat -tulpn | grep 8081

# Check permissions
ls -la /opt/food-ordering/
```

#### **Database Connection Issues**
```bash
# Check database file
ls -la /opt/food-ordering/data/food_ordering.db

# Test database connection
sqlite3 /opt/food-ordering/data/food_ordering.db ".tables"

# Check disk space
df -h /opt/food-ordering/
```

#### **Performance Issues**
```bash
# Monitor memory usage
free -h

# Monitor CPU usage
top -p $(pgrep -f food-ordering-backend)

# Check application metrics
curl http://localhost:8081/metrics | jq .
```

### **Emergency Recovery**
```bash
# Stop service
sudo systemctl stop food-ordering

# Restore from backup
tar -xzf food-ordering-backup-YYYYMMDD.tar.gz -C /

# Restore database
cp /opt/food-ordering/backups/food_ordering_YYYYMMDD_HHMMSS.db /opt/food-ordering/data/food_ordering.db

# Start service
sudo systemctl start food-ordering
```

## ✅ **Deployment Checklist**

### **Pre-Deployment**
- [ ] Server meets minimum requirements
- [ ] Java 17+ installed and configured
- [ ] Application built successfully
- [ ] Configuration files prepared
- [ ] SSL certificates obtained (if using HTTPS)
- [ ] Firewall rules configured
- [ ] Database setup completed

### **Deployment**
- [ ] Application files copied to server
- [ ] System service created and configured
- [ ] Database initialized and tested
- [ ] Nginx/reverse proxy configured (if used)
- [ ] Health checks responding
- [ ] Logs being written correctly
- [ ] Backup system configured and tested

### **Post-Deployment**
- [ ] Application accessible from clients
- [ ] All API endpoints responding
- [ ] Database queries executing correctly
- [ ] Authentication working properly
- [ ] Performance metrics within acceptable ranges
- [ ] Monitoring and alerting active
- [ ] Documentation updated with deployment details

---

## 📞 **Support & Maintenance**

### **Monitoring Commands**
```bash
# Service status
sudo systemctl status food-ordering

# Real-time logs
journalctl -u food-ordering -f

# Memory usage
ps aux | grep food-ordering-backend

# Database size
du -h /opt/food-ordering/data/

# API health check
curl -s http://localhost:8081/health | jq .
```

### **Maintenance Tasks**
- **Daily:** Check service status and logs
- **Weekly:** Review performance metrics and disk usage
- **Monthly:** Update system packages and review security logs
- **Quarterly:** Database backup verification and disaster recovery testing

### **Emergency Contacts**
- **System Administrator:** [Your contact information]
- **Database Administrator:** [DBA contact information]
- **Application Developer:** [Developer contact information]

---

**Deployment Guide Version:** 1.0  
**Last Updated:** December 2025  
**Compatible with:** Food Ordering System v1.0  
**Technology Stack:** Pure Java + JavaFX (No Spring, No Docker) 
# 🚀 Installation Guide

## Prerequisites

### System Requirements
- **Java:** 17 or higher
- **Maven:** 3.6+
- **Memory:** 4GB+ RAM
- **Disk:** 20GB+ free space
- **OS:** Windows 10+, Linux, macOS

### Database
- **Development:** SQLite (included)
- **Production:** PostgreSQL 12+

---

## Development Setup

### 1. Quick Start
```bash
# Clone repository
git clone <repository-url>
cd food-ordering-project

# Build and run backend
cd backend
mvn clean package
java -jar target/food-ordering-backend.jar

# Build and run frontend (new terminal)
cd ../frontend-javafx
mvn clean package
java -jar target/food-ordering-frontend.jar
```

### 2. Development Configuration
- Backend runs on `localhost:8081`
- Uses SQLite database (auto-created)
- No additional configuration needed

---

## Production Deployment

### 1. Database Setup
```bash
# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Create database
sudo -u postgres psql -f database-setup.sql

# Verify connection
psql -h localhost -U food_ordering_user -d food_ordering_prod -c "SELECT 1"
```

### 2. Environment Configuration
```bash
# Set environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/food_ordering_prod
export DATABASE_USERNAME=food_ordering_user
export DATABASE_PASSWORD=your_secure_password
export JWT_SECRET=your_256_bit_secret_key
export SERVER_PORT=8081
```

### 3. Application Deployment

**Linux/Unix:**
```bash
# Copy files
sudo mkdir -p /opt/food-ordering
sudo cp food-ordering-backend.jar /opt/food-ordering/
sudo cp application-production.properties /opt/food-ordering/

# Install service
sudo cp food-ordering.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable food-ordering
sudo systemctl start food-ordering
```

**Windows:**
```batch
# Create directory
mkdir C:\food-ordering

# Copy files
copy food-ordering-backend.jar C:\food-ordering\
copy application-production.properties C:\food-ordering\

# Run service
food-ordering-windows.bat start
```

### 4. Verification
```bash
# Check service status
systemctl status food-ordering

# Test API
curl http://localhost:8081/health

# View logs
journalctl -u food-ordering -f
```

---

## SSL/HTTPS Setup

### 1. Generate Certificate
```bash
# Self-signed (development)
keytool -genkeypair -alias food-ordering -keyalg RSA -keysize 2048 \
  -keystore keystore.jks -validity 365

# Production (Let's Encrypt)
certbot certonly --standalone -d yourdomain.com
```

### 2. Configure SSL
```properties
# In application-production.properties
ssl.enabled=true
ssl.keystore.path=/etc/ssl/food-ordering/keystore.jks
ssl.keystore.password=your_password
```

---

## Backup Setup

### 1. Configure Backup
```bash
# Edit backup configuration
cp backup.conf /opt/food-ordering/
nano /opt/food-ordering/backup.conf

# Set database credentials
DB_PASSWORD=your_backup_password
BACKUP_LOCATION=/var/backups/food-ordering
```

### 2. Schedule Automatic Backup
```bash
# Add to crontab
sudo crontab -e

# Daily backup at 2 AM
0 2 * * * /opt/food-ordering/backup-system.sh backup
```

---

## Frontend Distribution

### 1. Build Distribution
```bash
cd frontend-javafx
mvn clean package

# JAR file created at:
# target/food-ordering-frontend.jar
```

### 2. Client Installation
```bash
# Ensure Java 17+ is installed
java -version

# Run application
java -jar food-ordering-frontend.jar

# Or create desktop shortcut
chmod +x food-ordering-frontend.jar
```

---

## Configuration Files

### Key Configuration Files
- `application-production.properties` - Production settings
- `database-setup.sql` - Database initialization
- `food-ordering.service` - Linux service
- `backup.conf` - Backup configuration

### Important Directories
- `/opt/food-ordering/` - Application files (Linux)
- `/var/log/food-ordering/` - Log files
- `/var/backups/food-ordering/` - Backup files

---

## Troubleshooting

### Common Issues

**Port already in use:**
```bash
# Check what's using port 8081
netstat -tulpn | grep 8081
# Kill process or change port
```

**Database connection failed:**
```bash
# Verify PostgreSQL is running
systemctl status postgresql
# Check credentials and connection string
```

**Java not found:**
```bash
# Install Java 17
sudo apt install openjdk-17-jdk
# Verify installation
java -version
```

For more troubleshooting, see [Troubleshooting Guide](troubleshooting.md).

---

## Next Steps

After installation:
1. Read [User Guide](user-guide.md) for end-user documentation
2. Read [Admin Guide](admin-guide.md) for system administration
3. Review [API Reference](api-reference.md) for integration 
# Food Ordering System - Deployment Guide

## 📋 فهرست

- [پیش‌نیازها](#پیش‌نیازها)
- [محیط‌های مختلف](#محیط‌های-مختلف)
- [مراحل Deployment](#مراحل-deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Rollback Strategy](#rollback-strategy)

## پیش‌نیازها

### نرم‌افزارهای مورد نیاز
- Java JDK 17+
- Apache Maven 3.8+
- Git (اختیاری)

### بررسی نصب
```powershell
java -version    # باید Java 17+ نشان دهد
mvn --version    # باید Maven 3.8+ نشان دهد
git --version    # برای version control
```

## محیط‌های مختلف

### Development Environment
```powershell
# راه‌اندازی محیط
.\scripts\setup-environment.ps1 -Environment development

# deployment
.\scripts\deploy-simple.ps1 -Environment development -DryRun
```

**مشخصات:**
- Backend Port: 8080
- Frontend Port: 8081
- Database: SQLite Development
- Logging: DEBUG level

### Staging Environment
```powershell
# راه‌اندازی محیط
.\scripts\setup-environment.ps1 -Environment staging

# deployment
.\scripts\deploy-simple.ps1 -Environment staging
```

**مشخصات:**
- Backend Port: 8082
- Frontend Port: 8083
- Database: SQLite Staging
- Logging: INFO level

### Production Environment
```powershell
# راه‌اندازی محیط (نیاز به مجوز بالا)
.\scripts\setup-environment.ps1 -Environment production

# deployment (نیاز به تایید)
.\scripts\deploy-simple.ps1 -Environment production -Force
```

**مشخصات:**
- Backend Port: 8080
- Frontend Port: 8080
- Database: SQLite Production (encrypted)
- Logging: WARN level

## مراحل Deployment

### 1. آماده‌سازی
```powershell
# setup محیط
.\scripts\setup-environment.ps1 -Environment development

# بررسی وضعیت
.\scripts\migrate-database.ps1 -Environment development -Action status
```

### 2. Build و Test
```powershell
# build backend
cd backend
mvn clean compile test

# build frontend  
cd ..\frontend-javafx
mvn clean compile test
cd ..
```

### 3. Deployment
```powershell
# deployment ساده
.\scripts\deploy-simple.ps1 -Environment development

# deployment با گزینه‌های پیشرفته
.\scripts\deploy-simple.ps1 -Environment staging -SkipTests -Version "v1.2.3"
```

### 4. تست بعد از Deployment
```powershell
# health check
.\deployments\development\scripts\health-check-development.bat

# بررسی logs
Get-Content logs\deployment-*.log -Tail 20
```

## CI/CD Pipeline

### GitHub Actions
فایل `.github/workflows/ci-cd-pipeline.yml` شامل:

- **Build & Test:** compile کد و اجرای تست‌ها
- **Security Scan:** بررسی امنیتی و OWASP compliance
- **Deploy:** deployment خودکار به محیط‌های مختلف

### Trigger Events
```bash
# Deploy to development
git push origin develop

# Deploy to staging
git push origin staging

# Deploy to production
git push origin main
```

### Jenkins Pipeline
فایل `Jenkinsfile` برای Jenkins CI/CD:

```groovy
pipeline {
    agent any
    stages {
        stage('Build') { ... }
        stage('Test') { ... }
        stage('Security Scan') { ... }
        stage('Deploy') { ... }
    }
}
```

## Rollback Strategy

### لیست نسخه‌های موجود
```powershell
.\scripts\rollback.ps1 -Environment production -ListVersions
```

### Rollback ساده
```powershell
.\scripts\rollback.ps1 -Environment production -TargetVersion "backup-20250105-123456"
```

### Emergency Rollback
```powershell
.\scripts\rollback-system.ps1 -Environment production -RollbackType full -Emergency -Force
```

## نکات مهم

### امنیت
- همیشه از HTTPS استفاده کنید
- environment variables را برای اطلاعات حساس استفاده کنید
- پیش از deployment، security scan اجرا کنید

### Performance
- JVM memory settings را برای هر محیط تنظیم کنید
- connection pooling را فعال کنید
- caching strategies پیاده‌سازی کنید

### Monitoring
- health checks منظم انجام دهید
- log files را بررسی کنید
- metrics و alerts تنظیم کنید

## عیب‌یابی

### مشکلات متداول

#### Port در حال استفاده
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

#### مشکل Database
```powershell
.\scripts\migrate-database.ps1 -Environment development -Action reset
.\scripts\migrate-database.ps1 -Environment development -Action migrate
```

#### Build Error
```powershell
cd backend
mvn clean install -DskipTests
```

## پشتیبانی

برای کمک و پشتیبانی:
- مستندات کامل: `docs/` directory
- Log files: `logs/` directory  
- Configuration: `config/` directory

---

**آخرین بروزرسانی:** 2025-01-05 
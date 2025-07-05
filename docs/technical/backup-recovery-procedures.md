# 💾 Backup & Disaster Recovery Procedures

> **رویه‌های پشتیبان‌گیری و بازیابی بحران سیستم سفارش غذا**

---

## 📋 فهرست مطالب

1. [Overview](#overview)
2. [Backup Strategy](#backup-strategy)
3. [Automated Backup Scripts](#automated-backup-scripts)
4. [Disaster Recovery Plan](#disaster-recovery-plan)
5. [Testing & Verification](#testing-verification)
6. [Monitoring & Alerts](#monitoring-alerts)
7. [Recovery Procedures](#recovery-procedures)
8. [Cloud Backup](#cloud-backup)

---

## 🎯 Overview

### Backup Philosophy
- **3-2-1 Rule**: 3 copies, 2 different media, 1 offsite
- **Automated**: تمام بکاپ‌ها خودکار
- **Tested**: بکاپ‌ها باید آزمایش شوند
- **Monitored**: نظارت بر موفقیت بکاپ‌ها

### Recovery Objectives
```yaml
RTO (Recovery Time Objective):
  - Critical Systems: 30 minutes
  - Non-Critical Systems: 2 hours
  - Full System: 4 hours

RPO (Recovery Point Objective):
  - Database: 1 hour
  - Application: 4 hours
  - Configuration: 24 hours
```

### Backup Types
```yaml
Full Backup:
  - Complete system backup
  - Frequency: Weekly
  - Retention: 4 weeks

Incremental Backup:
  - Changes since last backup
  - Frequency: Daily
  - Retention: 7 days

Differential Backup:
  - Changes since last full backup
  - Frequency: Daily
  - Retention: 7 days
```

---

## 📋 Backup Strategy

### Daily Backup Schedule
```yaml
02:00 AM - Database Backup:
  - PostgreSQL full dump
  - Redis data snapshot
  - Transaction logs

03:00 AM - Application Backup:
  - Configuration files
  - Application logs
  - User uploaded files

04:00 AM - System Backup:
  - System configuration
  - SSL certificates
  - Cron jobs
```

### Weekly Backup Schedule
```yaml
Sunday 01:00 AM - Full System Backup:
  - Complete server image
  - All databases
  - All application files
  - System configuration

Sunday 05:00 AM - Verification:
  - Test restore procedures
  - Backup integrity check
  - Performance testing
```

### Monthly Backup Schedule
```yaml
First Sunday of Month - Archive Backup:
  - Long-term storage
  - Compressed archives
  - Offsite storage
  - Compliance backup
```

---

## 🔧 Automated Backup Scripts

### Daily Database Backup
```bash
#!/bin/bash
# daily-db-backup.sh

# Configuration
BACKUP_DIR="/backup/daily"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7
DB_NAME="food_ordering"

# Create backup directory
mkdir -p $BACKUP_DIR

# PostgreSQL backup
echo "Starting PostgreSQL backup..."
pg_dump -h localhost -U postgres -d $DB_NAME > $BACKUP_DIR/postgresql_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/postgresql_$DATE.sql

# Redis backup
echo "Starting Redis backup..."
redis-cli save
cp /var/lib/redis/dump.rdb $BACKUP_DIR/redis_$DATE.rdb
gzip $BACKUP_DIR/redis_$DATE.rdb

# Transaction log backup
echo "Backing up transaction logs..."
tar -czf $BACKUP_DIR/transaction_logs_$DATE.tar.gz /var/lib/postgresql/*/pg_wal/

# Remove old backups
find $BACKUP_DIR -name "*.gz" -mtime +$RETENTION_DAYS -delete

# Log completion
echo "$(date): Database backup completed successfully" >> /var/log/backup.log
```

### Daily Application Backup
```bash
#!/bin/bash
# daily-app-backup.sh

# Configuration
BACKUP_DIR="/backup/application"
DATE=$(date +%Y%m%d_%H%M%S)
APP_DIR="/opt/food-ordering"

# Create backup directory
mkdir -p $BACKUP_DIR

# Application files backup
echo "Backing up application files..."
tar -czf $BACKUP_DIR/application_$DATE.tar.gz $APP_DIR

# Configuration backup
echo "Backing up configuration..."
tar -czf $BACKUP_DIR/config_$DATE.tar.gz /etc/nginx /etc/ssl

# Log files backup
echo "Backing up logs..."
tar -czf $BACKUP_DIR/logs_$DATE.tar.gz /var/log/

# User uploads backup
echo "Backing up user uploads..."
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz /var/uploads/

# Remove old backups (keep 7 days)
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "$(date): Application backup completed" >> /var/log/backup.log
```

### Weekly Full System Backup
```bash
#!/bin/bash
# weekly-full-backup.sh

# Configuration
BACKUP_DIR="/backup/weekly"
DATE=$(date +%Y%m%d)
RETENTION_WEEKS=4

# Create backup directory
mkdir -p $BACKUP_DIR

# Full system backup
echo "Starting full system backup..."
tar -czf $BACKUP_DIR/full_system_$DATE.tar.gz \
    --exclude=/proc \
    --exclude=/tmp \
    --exclude=/sys \
    --exclude=/dev \
    --exclude=/run \
    --exclude=/backup \
    /

# Database backup with schema
echo "Full database backup with schema..."
pg_dumpall -h localhost -U postgres > $BACKUP_DIR/all_databases_$DATE.sql
gzip $BACKUP_DIR/all_databases_$DATE.sql

# System configuration backup
echo "System configuration backup..."
tar -czf $BACKUP_DIR/system_config_$DATE.tar.gz \
    /etc \
    /var/spool/cron \
    /home/*/.bashrc \
    /home/*/.ssh

# Remove old weekly backups
find $BACKUP_DIR -name "*.tar.gz" -mtime +$((RETENTION_WEEKS*7)) -delete

echo "$(date): Weekly full backup completed" >> /var/log/backup.log
```

---

## 🚨 Disaster Recovery Plan

### Emergency Response Team
```yaml
Primary Contact:
  Name: System Administrator
  Phone: +98-XXX-XXX-XXXX
  Email: admin@company.com

Secondary Contact:
  Name: Database Administrator
  Phone: +98-XXX-XXX-XXXX
  Email: dba@company.com

Escalation:
  Management: management@company.com
  External Support: support@hosting-provider.com
```

### Disaster Scenarios & Response

#### Scenario 1: Database Corruption
```bash
#!/bin/bash
# db-recovery.sh

echo "=== Database Recovery Procedure ==="

# Stop application
systemctl stop food-ordering

# Backup corrupted database
pg_dump -h localhost -U postgres -d food_ordering > /tmp/corrupted_db_$(date +%Y%m%d).sql

# Drop corrupted database
dropdb -h localhost -U postgres food_ordering

# Restore from backup
createdb -h localhost -U postgres food_ordering
gunzip -c /backup/daily/postgresql_$(date +%Y%m%d)_*.sql.gz | psql -h localhost -U postgres -d food_ordering

# Verify restoration
psql -h localhost -U postgres -d food_ordering -c "SELECT count(*) FROM users;"

# Start application
systemctl start food-ordering

echo "Database recovery completed at $(date)"
```

#### Scenario 2: Complete Server Failure
```bash
#!/bin/bash
# server-recovery.sh

echo "=== Complete Server Recovery ==="

# Set up new server
apt update && apt upgrade -y
apt install -y postgresql nginx redis-server openjdk-17-jdk

# Restore system configuration
tar -xzf /backup/weekly/system_config_$(date +%Y%m%d).tar.gz -C /

# Restore databases
gunzip -c /backup/weekly/all_databases_$(date +%Y%m%d).sql.gz | psql -h localhost -U postgres

# Restore application
tar -xzf /backup/weekly/full_system_$(date +%Y%m%d).tar.gz -C /

# Start services
systemctl enable postgresql nginx redis-server
systemctl start postgresql nginx redis-server food-ordering

# Verify services
curl -s http://localhost:8080/health
```

#### Scenario 3: Data Center Outage
```yaml
Primary Site Down:
  1. Activate secondary site
  2. Redirect DNS to backup
  3. Restore from offsite backup
  4. Communicate with users
  5. Monitor recovery progress

Secondary Site Activation:
  1. Start backup servers
  2. Restore latest backup
  3. Update DNS records
  4. Test all services
  5. Monitor performance
```

---

## ✅ Testing & Verification

### Backup Verification Script
```bash
#!/bin/bash
# verify-backup.sh

echo "=== Backup Verification - $(date) ==="

# Test database backup
echo "Testing database backup..."
gunzip -c /backup/daily/postgresql_$(date +%Y%m%d)_*.sql.gz | head -20

# Test application backup
echo "Testing application backup..."
tar -tzf /backup/application/application_$(date +%Y%m%d)_*.tar.gz | head -10

# Check backup sizes
echo "Backup sizes:"
du -sh /backup/daily/* | tail -5
du -sh /backup/weekly/* | tail -3

# Test restore procedure
echo "Testing restore procedure..."
createdb -h localhost -U postgres test_restore
gunzip -c /backup/daily/postgresql_$(date +%Y%m%d)_*.sql.gz | psql -h localhost -U postgres -d test_restore > /tmp/restore_test.log 2>&1

# Verify restore
RESTORE_STATUS=$(psql -h localhost -U postgres -d test_restore -c "SELECT count(*) FROM users;" 2>&1)
if [[ $RESTORE_STATUS =~ [0-9]+ ]]; then
    echo "✅ Restore test: PASSED"
else
    echo "❌ Restore test: FAILED"
fi

# Cleanup
dropdb -h localhost -U postgres test_restore

echo "Backup verification completed"
```

### Monthly Disaster Recovery Test
```bash
#!/bin/bash
# monthly-dr-test.sh

echo "=== Monthly Disaster Recovery Test ==="

# Create test environment
echo "Setting up test environment..."
docker run -d --name test-postgres -e POSTGRES_PASSWORD=password postgres:15
docker run -d --name test-redis redis:7

# Restore from backup
echo "Restoring from backup..."
gunzip -c /backup/weekly/all_databases_$(date +%Y%m%d).sql.gz | docker exec -i test-postgres psql -U postgres

# Test application functionality
echo "Testing application functionality..."
# Add specific tests here

# Performance test
echo "Performance testing..."
# Add performance tests here

# Cleanup
docker stop test-postgres test-redis
docker rm test-postgres test-redis

echo "DR test completed at $(date)"
```

---

## 📊 Monitoring & Alerts

### Backup Monitoring
```bash
#!/bin/bash
# monitor-backups.sh

# Check if backups completed successfully
BACKUP_SUCCESS=$(grep "backup completed successfully" /var/log/backup.log | grep $(date +%Y-%m-%d) | wc -l)

if [ $BACKUP_SUCCESS -lt 3 ]; then
    echo "WARNING: Not all backups completed successfully today"
    echo "Backup failures detected" | mail -s "Backup Alert" admin@company.com
fi

# Check backup sizes
TODAYS_BACKUP_SIZE=$(du -sh /backup/daily/*$(date +%Y%m%d)* | awk '{print $1}')
echo "Today's backup size: $TODAYS_BACKUP_SIZE"

# Check disk space
BACKUP_DISK_USAGE=$(df -h /backup | awk 'NR==2 {print $5}' | sed 's/%//')
if [ $BACKUP_DISK_USAGE -gt 80 ]; then
    echo "WARNING: Backup disk usage is $BACKUP_DISK_USAGE%"
    echo "Backup disk space low" | mail -s "Disk Space Alert" admin@company.com
fi
```

### Alert Configuration
```yaml
Backup Alerts:
  - Backup failure
  - Backup size anomaly
  - Disk space > 80%
  - Restore test failure

Recovery Alerts:
  - Recovery initiated
  - Recovery completed
  - Recovery failed
  - Performance degradation
```

---

## 🔄 Recovery Procedures

### Point-in-Time Recovery
```bash
#!/bin/bash
# point-in-time-recovery.sh

RECOVERY_TIME=$1  # Format: YYYY-MM-DD HH:MM:SS

echo "=== Point-in-Time Recovery to $RECOVERY_TIME ==="

# Stop application
systemctl stop food-ordering

# Backup current state
pg_dump -h localhost -U postgres -d food_ordering > /tmp/pre_recovery_backup.sql

# Restore base backup
dropdb -h localhost -U postgres food_ordering
createdb -h localhost -U postgres food_ordering
gunzip -c /backup/weekly/all_databases_$(date +%Y%m%d).sql.gz | psql -h localhost -U postgres -d food_ordering

# Apply WAL files up to recovery point
pg_waldump /var/lib/postgresql/*/pg_wal/ | grep "$RECOVERY_TIME" | psql -h localhost -U postgres -d food_ordering

# Start application
systemctl start food-ordering

echo "Point-in-time recovery completed"
```

### Partial Recovery
```bash
#!/bin/bash
# partial-recovery.sh

TABLE_NAME=$1

echo "=== Partial Recovery for table: $TABLE_NAME ==="

# Extract table data from backup
pg_restore -h localhost -U postgres -d food_ordering -t $TABLE_NAME /backup/daily/postgresql_$(date +%Y%m%d)_*.sql.gz

# Verify recovery
psql -h localhost -U postgres -d food_ordering -c "SELECT count(*) FROM $TABLE_NAME;"

echo "Partial recovery completed for $TABLE_NAME"
```

---

## ☁️ Cloud Backup

### Cloud Storage Configuration
```yaml
Primary Cloud: AWS S3
  - Bucket: food-ordering-backups-primary
  - Region: us-east-1
  - Storage Class: Standard-IA
  - Encryption: AES-256

Secondary Cloud: Google Cloud Storage
  - Bucket: food-ordering-backups-secondary
  - Region: us-central1
  - Storage Class: Nearline
  - Encryption: Customer-managed keys
```

### Cloud Backup Script
```bash
#!/bin/bash
# cloud-backup.sh

# AWS S3 backup
echo "Uploading to AWS S3..."
aws s3 sync /backup/daily/ s3://food-ordering-backups-primary/daily/
aws s3 sync /backup/weekly/ s3://food-ordering-backups-primary/weekly/

# Google Cloud backup
echo "Uploading to Google Cloud..."
gsutil -m rsync -r /backup/daily/ gs://food-ordering-backups-secondary/daily/
gsutil -m rsync -r /backup/weekly/ gs://food-ordering-backups-secondary/weekly/

echo "Cloud backup completed"
```

---

## 📋 Recovery Checklist

### Pre-Recovery Checklist
- [ ] Identify recovery scope
- [ ] Notify stakeholders
- [ ] Prepare recovery environment
- [ ] Verify backup integrity
- [ ] Plan recovery steps
- [ ] Estimate recovery time

### During Recovery
- [ ] Execute recovery procedures
- [ ] Monitor progress
- [ ] Document actions taken
- [ ] Communicate status updates
- [ ] Verify data integrity
- [ ] Test system functionality

### Post-Recovery Checklist
- [ ] Verify all services running
- [ ] Check data consistency
- [ ] Performance testing
- [ ] User acceptance testing
- [ ] Update documentation
- [ ] Conduct post-mortem

---

**نسخه**: 1.0.0  
**آخرین به‌روزرسانی**: نوامبر 2024  
**وضعیت**: Production Ready 
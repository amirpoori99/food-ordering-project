# 🔧 Maintenance & Troubleshooting Guide

> **راهنمای کامل نگهداری و عیب‌یابی سیستم سفارش غذا**

---

## 📋 فهرست مطالب

1. [نمای کلی نگهداری](#overview)
2. [Daily Maintenance](#daily-maintenance)
3. [Weekly Maintenance](#weekly-maintenance)
4. [Monthly Maintenance](#monthly-maintenance)
5. [Performance Monitoring](#performance-monitoring)
6. [Troubleshooting Guide](#troubleshooting)
7. [Database Maintenance](#database-maintenance)
8. [Security Maintenance](#security-maintenance)
9. [Backup & Recovery](#backup-recovery)
10. [Emergency Procedures](#emergency-procedures)

---

## 🎯 Overview

### Maintenance Philosophy
- **Proactive**: مشکلات قبل از وقوع شناسایی شود
- **Automated**: حداکثر خودکارسازی فرآیندها
- **Monitored**: نظارت مستمر بر عملکرد سیستم
- **Documented**: تمام تغییرات مستندسازی شود

### System Components
```yaml
Application Layer:
  - Java Application (Port 8080)
  - Load Balancer (Nginx)
  - Session Management

Data Layer:
  - PostgreSQL Database
  - Redis Cache
  - File Storage

Infrastructure:
  - Linux Servers
  - Network Configuration
  - SSL Certificates
```

### Maintenance Schedule
```yaml
Daily:
  - System health checks
  - Log monitoring
  - Performance metrics review

Weekly:
  - Database maintenance
  - Security updates
  - Backup verification

Monthly:
  - System optimization
  - Capacity planning
  - Security audit
```

---

## 📅 Daily Maintenance

### Morning Health Check (09:00 AM)
```bash
#!/bin/bash
# daily-health-check.sh

echo "=== Daily Health Check - $(date) ==="

# Check application status
echo "1. Application Status:"
curl -s http://localhost:8080/health | jq '.'

# Check database connection
echo "2. Database Connection:"
psql -h localhost -U postgres -d food_ordering -c "SELECT 1 as status;"

# Check Redis cache
echo "3. Redis Cache:"
redis-cli ping

# Check disk usage
echo "4. Disk Usage:"
df -h | grep -E "(/)|(data)"

# Check memory usage
echo "5. Memory Usage:"
free -h

# Check load average
echo "6. Load Average:"
uptime

# Check recent errors
echo "7. Recent Errors:"
tail -n 50 /var/log/application.log | grep -i error | tail -n 10
```

### Application Logs Review
```bash
# Check application logs
tail -f /var/log/application.log

# Check error patterns
grep -i "error\|exception\|failed" /var/log/application.log | tail -n 20

# Check performance issues
grep -i "slow\|timeout\|high" /var/log/application.log | tail -n 10
```

### Performance Metrics
```bash
# CPU usage
top -bn1 | grep "Cpu(s)"

# Memory usage
cat /proc/meminfo | grep -E "MemTotal|MemFree|MemAvailable"

# Database connections
psql -h localhost -U postgres -d food_ordering -c "SELECT count(*) as active_connections FROM pg_stat_activity;"

# Cache hit rate
redis-cli info stats | grep keyspace_hits
```

---

## 📊 Weekly Maintenance

### Database Maintenance (Sunday 02:00 AM)
```sql
-- Weekly database maintenance script
-- Run as postgres user

-- Update table statistics
ANALYZE;

-- Vacuum tables
VACUUM ANALYZE;

-- Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes 
ORDER BY idx_scan DESC;

-- Check slow queries
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    max_time
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

### Security Updates
```bash
#!/bin/bash
# weekly-security-update.sh

echo "=== Weekly Security Update - $(date) ==="

# Update system packages
apt update && apt upgrade -y

# Check for security advisories
apt list --upgradable | grep -i security

# Update SSL certificates
certbot renew --dry-run

# Check firewall status
ufw status verbose

# Check failed login attempts
grep "Failed password" /var/log/auth.log | tail -n 10
```

### Backup Verification
```bash
#!/bin/bash
# verify-backups.sh

echo "=== Backup Verification - $(date) ==="

# Check backup files
ls -la /backup/daily/ | tail -n 7

# Test backup integrity
pg_dump -h localhost -U postgres -d food_ordering > /tmp/test_backup.sql
if [ $? -eq 0 ]; then
    echo "✅ Database backup integrity: OK"
else
    echo "❌ Database backup integrity: FAILED"
fi

# Check backup size
du -sh /backup/daily/backup_$(date +%Y%m%d).sql

# Test restore procedure (on test database)
createdb -h localhost -U postgres test_restore
psql -h localhost -U postgres -d test_restore < /backup/daily/backup_$(date +%Y%m%d).sql
dropdb -h localhost -U postgres test_restore
```

---

## 🗓️ Monthly Maintenance

### Performance Optimization
```bash
#!/bin/bash
# monthly-optimization.sh

echo "=== Monthly Performance Optimization - $(date) ==="

# Database optimization
psql -h localhost -U postgres -d food_ordering -c "
-- Reindex tables
REINDEX DATABASE food_ordering;

-- Update statistics
ANALYZE;

-- Clean up old data
DELETE FROM wallet_transactions WHERE created_at < NOW() - INTERVAL '2 years';
DELETE FROM order_items WHERE order_id IN (
    SELECT id FROM orders WHERE order_date < NOW() - INTERVAL '1 year'
);
"

# Clear old logs
find /var/log -name "*.log" -mtime +30 -delete

# Clean up cache
redis-cli flushdb

# Update application dependencies
cd /opt/food-ordering
mvn clean install -DskipTests

# Restart services
systemctl restart food-ordering
systemctl restart nginx
systemctl restart redis
```

### Capacity Planning
```bash
#!/bin/bash
# capacity-planning.sh

echo "=== Capacity Planning Report - $(date) ==="

# Database growth
psql -h localhost -U postgres -d food_ordering -c "
SELECT 
    'users' as table_name,
    count(*) as total_records,
    count(*) FILTER (WHERE created_at >= NOW() - INTERVAL '1 month') as monthly_growth
FROM users
UNION ALL
SELECT 
    'orders' as table_name,
    count(*) as total_records,
    count(*) FILTER (WHERE order_date >= NOW() - INTERVAL '1 month') as monthly_growth
FROM orders
UNION ALL
SELECT 
    'restaurants' as table_name,
    count(*) as total_records,
    count(*) FILTER (WHERE created_at >= NOW() - INTERVAL '1 month') as monthly_growth
FROM restaurants;
"

# Disk usage trend
df -h | grep -E "(/)|(data)" > /tmp/disk_usage_$(date +%Y%m%d).log

# Memory usage trend
free -h > /tmp/memory_usage_$(date +%Y%m%d).log

# Traffic analysis
tail -n 10000 /var/log/nginx/access.log | awk '{print $1}' | sort | uniq -c | sort -nr | head -10
```

---

## 📈 Performance Monitoring

### Key Metrics to Monitor
```yaml
Application Metrics:
  - Response time: < 200ms (average)
  - Error rate: < 1%
  - Request throughput: > 100 req/sec
  - Active users: Real-time count

Database Metrics:
  - Connection count: < 80% of max
  - Query execution time: < 100ms (average)
  - Cache hit rate: > 90%
  - Disk usage: < 80%

System Metrics:
  - CPU usage: < 70%
  - Memory usage: < 80%
  - Disk I/O: < 80%
  - Network bandwidth: Monitor trends
```

### Monitoring Commands
```bash
# Real-time application monitoring
watch -n 5 'curl -s http://localhost:8080/health'

# Database performance
watch -n 10 'psql -h localhost -U postgres -d food_ordering -c "SELECT count(*) FROM pg_stat_activity;"'

# System resources
watch -n 5 'free -h && echo "---" && df -h'

# Network connections
watch -n 5 'netstat -tuln | grep -E "(8080|5432|6379)"'
```

### Alert Thresholds
```yaml
Critical Alerts:
  - Application down: > 1 minute
  - Database unavailable: > 30 seconds
  - Memory usage: > 90%
  - Disk space: > 95%
  - Error rate: > 5%

Warning Alerts:
  - Response time: > 500ms
  - CPU usage: > 80%
  - Memory usage: > 85%
  - Database connections: > 90%
  - Cache hit rate: < 70%
```

---

## 🔧 Troubleshooting Guide

### Common Issues & Solutions

#### 1. Application Won't Start
```bash
# Check if port is available
netstat -tuln | grep 8080

# Check Java process
ps aux | grep java

# Check application logs
tail -f /var/log/application.log

# Check configuration
cat /opt/food-ordering/config/application.properties

# Common fixes:
sudo systemctl restart food-ordering
sudo systemctl enable food-ordering
```

#### 2. Database Connection Issues
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check database connectivity
psql -h localhost -U postgres -d food_ordering -c "SELECT 1;"

# Check connection pool
psql -h localhost -U postgres -d food_ordering -c "SELECT count(*) FROM pg_stat_activity;"

# Common fixes:
sudo systemctl restart postgresql
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'newpassword';"
```

#### 3. High CPU Usage
```bash
# Identify high CPU processes
top -o %CPU

# Check for infinite loops
strace -p <process_id>

# Check database queries
psql -h localhost -U postgres -d food_ordering -c "SELECT query, state FROM pg_stat_activity WHERE state = 'active';"

# Common fixes:
# - Optimize slow queries
# - Increase connection pool size
# - Add missing indexes
```

#### 4. Memory Issues
```bash
# Check memory usage
free -h
cat /proc/meminfo

# Check Java heap usage
jstat -gc <java_process_id>

# Check for memory leaks
jmap -histo <java_process_id>

# Common fixes:
# - Increase JVM heap size
# - Fix memory leaks
# - Clear cache
redis-cli flushdb
```

#### 5. Slow Response Times
```bash
# Check active connections
netstat -an | grep :8080 | wc -l

# Check database performance
psql -h localhost -U postgres -d food_ordering -c "SELECT query, query_start, state FROM pg_stat_activity WHERE state = 'active';"

# Check cache hit rate
redis-cli info stats | grep keyspace_hits

# Common fixes:
# - Add database indexes
# - Optimize queries
# - Increase cache TTL
# - Enable query caching
```

---

## 🗄️ Database Maintenance

### Daily Database Tasks
```sql
-- Check for long-running queries
SELECT 
    pid,
    now() - pg_stat_activity.query_start AS duration,
    query 
FROM pg_stat_activity 
WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes'
ORDER BY duration DESC;

-- Check table bloat
SELECT 
    schemaname,
    tablename,
    n_tup_ins,
    n_tup_upd,
    n_tup_del,
    n_dead_tup
FROM pg_stat_user_tables 
WHERE n_dead_tup > 1000
ORDER BY n_dead_tup DESC;

-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read
FROM pg_stat_user_indexes 
WHERE idx_scan = 0
ORDER BY idx_tup_read DESC;
```

### Weekly Database Cleanup
```sql
-- Remove old analytics data
DELETE FROM analytics_data WHERE created_at < NOW() - INTERVAL '3 months';

-- Clean up expired sessions
DELETE FROM user_sessions WHERE expires_at < NOW();

-- Archive old orders
INSERT INTO orders_archive 
SELECT * FROM orders 
WHERE order_date < NOW() - INTERVAL '2 years';

DELETE FROM orders WHERE order_date < NOW() - INTERVAL '2 years';

-- Update statistics
ANALYZE;
```

### Database Health Check
```bash
#!/bin/bash
# db-health-check.sh

echo "=== Database Health Check - $(date) ==="

# Check database size
psql -h localhost -U postgres -d food_ordering -c "
SELECT 
    pg_database.datname,
    pg_size_pretty(pg_database_size(pg_database.datname)) AS size
FROM pg_database
ORDER BY pg_database_size(pg_database.datname) DESC;"

# Check table sizes
psql -h localhost -U postgres -d food_ordering -c "
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) AS index_size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;"

# Check connections
psql -h localhost -U postgres -d food_ordering -c "
SELECT 
    state,
    count(*) as count
FROM pg_stat_activity 
GROUP BY state;"
```

---

## 🔒 Security Maintenance

### Daily Security Checks
```bash
#!/bin/bash
# daily-security-check.sh

echo "=== Daily Security Check - $(date) ==="

# Check failed login attempts
grep "Failed password" /var/log/auth.log | tail -n 10

# Check unusual network activity
netstat -tuln | grep -E "(8080|5432|6379)"

# Check file permissions
ls -la /opt/food-ordering/config/

# Check SSL certificate expiry
echo | openssl s_client -connect localhost:443 2>/dev/null | openssl x509 -noout -dates

# Check for security updates
apt list --upgradable | grep -i security
```

### Weekly Security Updates
```bash
#!/bin/bash
# weekly-security-update.sh

echo "=== Weekly Security Update - $(date) ==="

# Update system packages
apt update && apt list --upgradable

# Check for CVE alerts
apt list --upgradable | grep -i cve

# Update SSL certificates
certbot renew --dry-run

# Check firewall rules
ufw status numbered

# Rotate logs
logrotate -f /etc/logrotate.conf
```

### Security Audit Checklist
```yaml
Application Security:
  - [ ] JWT tokens using secure algorithms
  - [ ] Input validation on all endpoints
  - [ ] SQL injection protection
  - [ ] XSS protection enabled
  - [ ] CSRF tokens implemented

Database Security:
  - [ ] Database users have minimal privileges
  - [ ] Database connections encrypted
  - [ ] Sensitive data encrypted at rest
  - [ ] Regular security patches applied

Infrastructure Security:
  - [ ] Firewall rules configured
  - [ ] SSH key-based authentication
  - [ ] SSL/TLS certificates valid
  - [ ] Regular security updates applied
  - [ ] Intrusion detection enabled
```

---

## 💾 Backup & Recovery

### Backup Strategy
```yaml
Daily Backups:
  - Full database dump
  - Application configuration
  - Log files
  - Retention: 7 days

Weekly Backups:
  - Full system backup
  - Database with indexes
  - User uploaded files
  - Retention: 4 weeks

Monthly Backups:
  - Complete system image
  - Archive old data
  - Retention: 12 months
```

### Backup Scripts
```bash
#!/bin/bash
# daily-backup.sh

BACKUP_DIR="/backup/daily"
DATE=$(date +%Y%m%d)

# Create backup directory
mkdir -p $BACKUP_DIR

# Database backup
pg_dump -h localhost -U postgres -d food_ordering > $BACKUP_DIR/database_$DATE.sql

# Compress backup
gzip $BACKUP_DIR/database_$DATE.sql

# Application config backup
tar -czf $BACKUP_DIR/config_$DATE.tar.gz /opt/food-ordering/config/

# Log backup
tar -czf $BACKUP_DIR/logs_$DATE.tar.gz /var/log/

# Remove old backups (keep 7 days)
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete

echo "Backup completed: $DATE"
```

### Recovery Procedures
```bash
#!/bin/bash
# recovery-procedure.sh

echo "=== Recovery Procedure ==="

# Stop services
systemctl stop food-ordering
systemctl stop nginx

# Restore database
dropdb -h localhost -U postgres food_ordering
createdb -h localhost -U postgres food_ordering
gunzip -c /backup/daily/database_$(date +%Y%m%d).sql.gz | psql -h localhost -U postgres -d food_ordering

# Restore configuration
tar -xzf /backup/daily/config_$(date +%Y%m%d).tar.gz -C /

# Start services
systemctl start food-ordering
systemctl start nginx

# Verify recovery
curl -s http://localhost:8080/health
```

---

## 🚨 Emergency Procedures

### Service Outage Response
```bash
#!/bin/bash
# emergency-response.sh

echo "=== Emergency Response - $(date) ==="

# Check service status
systemctl status food-ordering
systemctl status postgresql
systemctl status nginx
systemctl status redis

# Quick restart
systemctl restart food-ordering
systemctl restart nginx

# Check logs for errors
tail -n 50 /var/log/application.log | grep -i error

# Check database connectivity
psql -h localhost -U postgres -d food_ordering -c "SELECT 1;"

# Check external dependencies
curl -s http://payment-gateway.com/health

# Send alert
echo "Service outage detected at $(date)" | mail -s "URGENT: Service Outage" admin@company.com
```

### Database Corruption Recovery
```bash
#!/bin/bash
# db-corruption-recovery.sh

echo "=== Database Corruption Recovery - $(date) ==="

# Stop application
systemctl stop food-ordering

# Check database integrity
pg_dump -h localhost -U postgres -d food_ordering > /tmp/integrity_check.sql

# If corruption detected, restore from backup
if [ $? -ne 0 ]; then
    echo "Database corruption detected. Restoring from backup..."
    
    # Drop corrupted database
    dropdb -h localhost -U postgres food_ordering
    
    # Create new database
    createdb -h localhost -U postgres food_ordering
    
    # Restore from latest backup
    gunzip -c /backup/daily/database_$(date +%Y%m%d).sql.gz | psql -h localhost -U postgres -d food_ordering
    
    # Verify restore
    psql -h localhost -U postgres -d food_ordering -c "SELECT count(*) FROM users;"
fi

# Start application
systemctl start food-ordering
```

### Contact Information
```yaml
Emergency Contacts:
  Primary Admin:
    Name: System Administrator
    Phone: +98-XXX-XXX-XXXX
    Email: admin@company.com

  Secondary Admin:
    Name: Database Administrator
    Phone: +98-XXX-XXX-XXXX
    Email: dba@company.com

  Infrastructure Team:
    Email: infrastructure@company.com
    Slack: #infrastructure-alerts

External Services:
  Hosting Provider: +98-XXX-XXX-XXXX
  SSL Certificate: support@letsencrypt.org
  Payment Gateway: support@payment-provider.com
```

---

**نسخه**: 1.0.0  
**آخرین به‌روزرسانی**: نوامبر 2024  
**وضعیت**: Production Ready 
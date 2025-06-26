# ⚙️ System Administration Guide

Complete guide for system administrators and operations teams.

## Overview

This guide covers system administration, monitoring, maintenance, and troubleshooting for the Food Ordering System.

---

## System Management

### Service Control

**Linux (systemd):**
```bash
# Start/stop/restart service
sudo systemctl start food-ordering
sudo systemctl stop food-ordering
sudo systemctl restart food-ordering

# Check status
sudo systemctl status food-ordering

# View logs
sudo journalctl -u food-ordering -f
```

**Windows:**
```batch
# Service management
food-ordering-windows.bat start
food-ordering-windows.bat stop
food-ordering-windows.bat restart
food-ordering-windows.bat status
```

### Configuration Management

**Environment Variables:**
```bash
# Production settings
export DATABASE_URL=jdbc:postgresql://localhost:5432/food_ordering_prod
export DATABASE_USERNAME=food_ordering_user
export DATABASE_PASSWORD=secure_password
export JWT_SECRET=256_bit_secret_key
export SERVER_PORT=8081
```

**Key Configuration Files:**
- `application-production.properties` - Main configuration
- `food-ordering.service` - System service
- `backup.conf` - Backup settings

---

## Monitoring & Health Checks

### System Health

**Health Endpoints:**
```bash
# Basic health check
curl http://localhost:8081/health

# Detailed system info
curl http://localhost:8081/health/detailed

# Database connectivity
curl http://localhost:8081/health/database
```

**Expected Responses:**
- `{"status":"UP","service":"food-ordering-backend"}` - Healthy
- HTTP 200 status code
- Response time < 200ms

### Log Monitoring

**Log Locations:**
- **Linux:** `/var/log/food-ordering/`
- **Windows:** `C:\food-ordering\logs\`
- **SystemD:** `journalctl -u food-ordering`

**Monitor Logs:**
```bash
# Real-time monitoring
tail -f /var/log/food-ordering/food-ordering.log

# Search for errors
grep -i "error\|exception" /var/log/food-ordering/food-ordering.log

# Check last 100 lines
tail -100 /var/log/food-ordering/food-ordering.log
```

### Performance Monitoring

**System Resources:**
```bash
# CPU and memory usage
ps aux | grep food-ordering

# Memory details
ps -p $(pgrep food-ordering) -o pid,vsz,rss,pmem

# Network connections
netstat -tulpn | grep 8081
```

**Database Monitoring:**
```sql
-- Active connections
SELECT count(*) FROM pg_stat_activity 
WHERE datname = 'food_ordering_prod';

-- Database size
SELECT pg_size_pretty(pg_database_size('food_ordering_prod'));

-- Slow queries
SELECT query, mean_time FROM pg_stat_statements 
ORDER BY mean_time DESC LIMIT 10;
```

---

## User Management

### Admin Dashboard Access
1. Login with admin credentials
2. Access admin panel at `/admin`
3. Administrative functions available

### User Operations

**View Users:**
- Complete user list with status
- Filter by role and status
- Search by name or phone

**User Actions:**
- Activate/deactivate accounts
- Change user roles
- Reset passwords
- View user activity

### Restaurant Management

**Restaurant Approval:**
1. Review new restaurant applications
2. Verify business information
3. Approve or reject applications

**Restaurant Monitoring:**
- Monitor restaurant performance
- Handle customer complaints
- Suspend non-compliant restaurants

---

## Database Administration

### Backup Operations

**Manual Backup:**
```bash
# Full system backup
./backup-system.sh backup

# Database only
pg_dump -h localhost -U food_ordering_backup food_ordering_prod > backup.sql
```

**Automated Backup:**
```bash
# Schedule in crontab
0 2 * * * /opt/food-ordering/backup-system.sh backup

# Verify backup schedule
crontab -l
```

**Backup Verification:**
```bash
# List available backups
./backup-system.sh list-backups

# Verify backup integrity
./backup-system.sh verify /path/to/backup
```

### Database Maintenance

**Daily Tasks:**
```sql
-- Update statistics
ANALYZE;

-- Check table sizes
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

**Weekly Tasks:**
```sql
-- Vacuum and analyze
VACUUM ANALYZE;

-- Clean old audit logs (keeps 90 days)
SELECT clean_audit_logs();
```

### Data Recovery

**Database Recovery:**
```bash
# Stop application
sudo systemctl stop food-ordering

# Restore from backup
psql -h localhost -U food_ordering_user -d food_ordering_prod < backup.sql

# Start application
sudo systemctl start food-ordering

# Verify recovery
curl http://localhost:8081/health
```

---

## Security Management

### Access Control

**User Roles:**
- **CUSTOMER** - Place orders, manage profile
- **VENDOR** - Manage restaurant and orders
- **COURIER** - Handle deliveries
- **ADMIN** - System administration

**Security Settings:**
- JWT token expiration: 24 hours
- Password requirements: Minimum 4 characters
- Session timeout: 60 minutes
- Rate limiting: 60 requests/minute

### Security Monitoring

**Failed Login Monitoring:**
```bash
# Check failed authentication attempts
grep "Authentication failed" /var/log/food-ordering/food-ordering.log

# Monitor unusual activity
grep -c "Authentication failed.*$(date +%Y-%m-%d)" \
    /var/log/food-ordering/food-ordering.log
```

**Database Security:**
```sql
-- Monitor failed login attempts
SELECT * FROM audit_log 
WHERE operation = 'LOGIN_FAILED' 
AND timestamp > NOW() - INTERVAL '24 hours';
```

### SSL/HTTPS Management

**Certificate Management:**
```bash
# Check certificate expiration
openssl x509 -in /etc/ssl/food-ordering/cert.pem -text -noout

# Renew Let's Encrypt certificate
certbot renew --dry-run
```

---

## Performance Optimization

### JVM Tuning

**Memory Settings:**
```bash
# Recommended JVM options
export JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
```

### Database Optimization

**Index Monitoring:**
```sql
-- Check index usage
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes 
ORDER BY idx_scan DESC;

-- Find unused indexes
SELECT schemaname, tablename, indexname
FROM pg_stat_user_indexes 
WHERE idx_scan = 0;
```

**Query Optimization:**
```sql
-- Enable query timing
SET log_min_duration_statement = 1000;

-- Monitor slow queries
SELECT query, total_time, calls, mean_time
FROM pg_stat_statements 
ORDER BY total_time DESC LIMIT 10;
```

---

## Troubleshooting

### Common Issues

**Service Won't Start:**
```bash
# Check Java version
java -version

# Verify port availability
netstat -tulpn | grep 8081

# Check file permissions
ls -la /opt/food-ordering/

# View detailed logs
sudo journalctl -u food-ordering -n 50
```

**Database Connection Issues:**
```bash
# Test database connection
psql -h localhost -U food_ordering_user -d food_ordering_prod -c "SELECT 1"

# Check PostgreSQL status
sudo systemctl status postgresql

# View PostgreSQL logs
sudo tail -f /var/log/postgresql/postgresql-*.log
```

**High CPU/Memory Usage:**
```bash
# Check system resources
top -p $(pgrep food-ordering)

# Generate thread dump
jstack $(pgrep food-ordering) > thread-dump.txt

# Memory analysis
jmap -dump:live,format=b,file=heap-dump.hprof $(pgrep food-ordering)
```

### Emergency Procedures

**Service Outage Response:**
1. Check service status
2. Review recent logs
3. Restart service if needed
4. Verify health endpoints
5. Notify stakeholders

**Database Emergency:**
1. Stop application immediately
2. Assess database integrity
3. Restore from latest backup
4. Verify data consistency
5. Restart services

---

## Reporting & Analytics

### System Reports

**Daily Reports:**
```bash
# Generate daily report
./daily-report.sh

# View system statistics
curl http://localhost:8081/metrics
```

**Business Analytics:**
```sql
-- Daily order summary
SELECT DATE(created_at) as date, COUNT(*) as orders, 
       SUM(total_amount) as revenue
FROM orders 
WHERE created_at >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY DATE(created_at);

-- Top restaurants
SELECT r.name, COUNT(o.id) as orders, SUM(o.total_amount) as revenue
FROM restaurants r
JOIN orders o ON r.id = o.restaurant_id
WHERE o.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY r.id, r.name
ORDER BY revenue DESC;
```

---

## Maintenance Schedule

### Daily Tasks
- [ ] Check service health
- [ ] Review error logs
- [ ] Monitor system resources
- [ ] Verify backup completion

### Weekly Tasks
- [ ] Database maintenance (VACUUM ANALYZE)
- [ ] Clean old log files
- [ ] Review security logs
- [ ] Update system statistics

### Monthly Tasks
- [ ] Security updates
- [ ] Certificate renewal check
- [ ] Performance analysis
- [ ] Capacity planning review

---

## Contact & Escalation

### Internal Support
- **System Admin:** admin@company.com
- **Database Admin:** dba@company.com
- **Security Team:** security@company.com

### External Vendors
- **Database Support:** PostgreSQL support
- **SSL Certificates:** Certificate authority
- **Cloud Provider:** If using cloud infrastructure

---

**Version:** 1.0  
**Last Updated:** December 2024  
**Next Review:** March 2025 
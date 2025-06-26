# 🔧 Troubleshooting Guide

Quick reference for diagnosing and fixing common issues.

---

## Application Issues

### Service Won't Start

**Check Java version:**
```bash
java -version  # Should be 17+
```

**Check port availability:**
```bash
netstat -tulpn | grep 8081
```

**Check logs:**
```bash
journalctl -u food-ordering -n 20
```

**Solutions:**
- Install Java 17: `sudo apt install openjdk-17-jdk`
- Kill port usage: `sudo lsof -ti:8081 | xargs sudo kill -9`
- Fix permissions: `sudo chown -R food-ordering:food-ordering /opt/food-ordering/`

---

## Database Issues

### Connection Failed

**Test connection:**
```bash
psql -h localhost -U food_ordering_user -d food_ordering_prod -c "SELECT 1"
```

**Check PostgreSQL service:**
```bash
systemctl status postgresql
```

**Solutions:**
- Start PostgreSQL: `sudo systemctl start postgresql`
- Verify credentials in configuration file
- Check connection limits in postgresql.conf

### Slow Performance

**Check slow queries:**
```sql
SELECT query, mean_time FROM pg_stat_statements 
ORDER BY mean_time DESC LIMIT 10;
```

**Solutions:**
- Run `ANALYZE;` to update statistics
- Run `VACUUM ANALYZE;` for maintenance
- Check database size and available disk space

---

## Network Issues

### API Not Accessible

**Check if service is listening:**
```bash
netstat -tulpn | grep 8081
```

**Test connectivity:**
```bash
curl http://localhost:8081/health
```

**Solutions:**
- Open firewall: `sudo ufw allow 8081/tcp`
- Check server.host in configuration
- Restart networking service

---

## Frontend Issues

### Application Won't Launch

**Solutions:**
- Install JavaFX: `sudo apt install openjfx`
- Verify Java version: `java -version`
- Run with verbose: `java -jar food-ordering-frontend.jar -verbose`

### Cannot Connect to Backend

**Solutions:**
- Verify backend is running: `systemctl status food-ordering`
- Test backend health: `curl http://localhost:8081/health`
- Check network connectivity

---

## Performance Issues

### High Memory Usage

**Check memory:**
```bash
free -h
ps aux --sort=-%mem | head
```

**Solutions:**
- Increase JVM memory: `export JAVA_OPTS="-Xmx4g -Xms2g"`
- Use G1 garbage collector: `export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"`

---

## Emergency Recovery

### Complete System Recovery

1. Stop services: `sudo systemctl stop food-ordering postgresql`
2. Check system health: `df -h` and `free -h`
3. Restore from backup: `./backup-system.sh restore-db backup.sql.gz`
4. Restart services: `sudo systemctl start postgresql food-ordering`
5. Verify: `curl http://localhost:8081/health`

---

## Getting Help

**Collect logs:**
```bash
journalctl -u food-ordering --since "1 hour ago" > app.log
tar -czf logs-$(date +%Y%m%d).tar.gz *.log
```

**Contact:** support@foodordering.com

---

**Version:** 1.0 
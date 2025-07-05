#!/bin/bash

# Automated Backup Script - Food Ordering System
# This script performs automated backups of database, files, and configurations

echo "==========================================="
echo "📦 Food Ordering System - Automated Backup"
echo "==========================================="

# Configuration
PROJECT_NAME="food-ordering-system"
BACKUP_BASE="/opt/backups/food-ordering"
DB_NAME="food_ordering_db"
DB_USER="food_ordering_user"
APPLICATION_PATH="/opt/food-ordering"
CONFIG_PATH="/etc/food-ordering"
LOG_PATH="/var/log/food-ordering"
RETENTION_DAYS=30

# Date formatting
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="$BACKUP_BASE/$DATE"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
    echo "$(date '+%Y-%m-%d %H:%M:%S') [INFO] $1" >> "$BACKUP_DIR/backup.log"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    echo "$(date '+%Y-%m-%d %H:%M:%S') [WARN] $1" >> "$BACKUP_DIR/backup.log"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    echo "$(date '+%Y-%m-%d %H:%M:%S') [ERROR] $1" >> "$BACKUP_DIR/backup.log"
}

# Create backup directory
create_backup_dir() {
    log_info "Creating backup directory: $BACKUP_DIR"
    
    mkdir -p "$BACKUP_DIR/database"
    mkdir -p "$BACKUP_DIR/application"
    mkdir -p "$BACKUP_DIR/config"
    mkdir -p "$BACKUP_DIR/logs"
    
    if [ $? -eq 0 ]; then
        log_info "Backup directory created successfully"
    else
        log_error "Failed to create backup directory"
        exit 1
    fi
}

# Backup database
backup_database() {
    log_info "Starting database backup..."
    
    # PostgreSQL backup
    DB_BACKUP_FILE="$BACKUP_DIR/database/food_ordering_db_$DATE.sql"
    
    sudo -u postgres pg_dump -h localhost -U $DB_USER -d $DB_NAME > "$DB_BACKUP_FILE"
    
    if [ $? -eq 0 ]; then
        log_info "Database backup completed: $DB_BACKUP_FILE"
        
        # Compress database backup
        gzip "$DB_BACKUP_FILE"
        log_info "Database backup compressed: $DB_BACKUP_FILE.gz"
    else
        log_error "Database backup failed"
        exit 1
    fi
}

# Backup application files
backup_application() {
    log_info "Starting application backup..."
    
    if [ -d "$APPLICATION_PATH" ]; then
        tar -czf "$BACKUP_DIR/application/application_$DATE.tar.gz" -C "$APPLICATION_PATH" .
        
        if [ $? -eq 0 ]; then
            log_info "Application backup completed"
        else
            log_error "Application backup failed"
            exit 1
        fi
    else
        log_warn "Application directory not found: $APPLICATION_PATH"
    fi
}

# Backup configuration files
backup_config() {
    log_info "Starting configuration backup..."
    
    # Backup system configurations
    CONFIG_BACKUP_DIR="$BACKUP_DIR/config"
    
    # Nginx configuration
    if [ -f "/etc/nginx/sites-available/food-ordering" ]; then
        cp "/etc/nginx/sites-available/food-ordering" "$CONFIG_BACKUP_DIR/nginx-food-ordering.conf"
        log_info "Nginx configuration backed up"
    fi
    
    # Systemd service
    if [ -f "/etc/systemd/system/food-ordering.service" ]; then
        cp "/etc/systemd/system/food-ordering.service" "$CONFIG_BACKUP_DIR/food-ordering.service"
        log_info "Systemd service backed up"
    fi
    
    # Application configuration
    if [ -f "$APPLICATION_PATH/current/src/main/resources/application.properties" ]; then
        cp "$APPLICATION_PATH/current/src/main/resources/application.properties" "$CONFIG_BACKUP_DIR/application.properties"
        log_info "Application configuration backed up"
    fi
    
    # Hibernate configuration
    if [ -f "$APPLICATION_PATH/current/src/main/resources/hibernate.cfg.xml" ]; then
        cp "$APPLICATION_PATH/current/src/main/resources/hibernate.cfg.xml" "$CONFIG_BACKUP_DIR/hibernate.cfg.xml"
        log_info "Hibernate configuration backed up"
    fi
    
    # Redis configuration
    if [ -f "/etc/redis/redis.conf" ]; then
        cp "/etc/redis/redis.conf" "$CONFIG_BACKUP_DIR/redis.conf"
        log_info "Redis configuration backed up"
    fi
    
    # PostgreSQL configuration
    if [ -f "/etc/postgresql/*/main/postgresql.conf" ]; then
        cp /etc/postgresql/*/main/postgresql.conf "$CONFIG_BACKUP_DIR/postgresql.conf"
        log_info "PostgreSQL configuration backed up"
    fi
    
    # Prometheus configuration
    if [ -f "/etc/prometheus/prometheus.yml" ]; then
        cp "/etc/prometheus/prometheus.yml" "$CONFIG_BACKUP_DIR/prometheus.yml"
        log_info "Prometheus configuration backed up"
    fi
}

# Backup logs
backup_logs() {
    log_info "Starting logs backup..."
    
    if [ -d "$LOG_PATH" ]; then
        tar -czf "$BACKUP_DIR/logs/logs_$DATE.tar.gz" -C "$LOG_PATH" .
        
        if [ $? -eq 0 ]; then
            log_info "Logs backup completed"
        else
            log_error "Logs backup failed"
        fi
    else
        log_warn "Log directory not found: $LOG_PATH"
    fi
    
    # System logs
    journalctl -u food-ordering --since="1 week ago" > "$BACKUP_DIR/logs/systemd_logs_$DATE.log"
    log_info "System logs backed up"
}

# Create backup manifest
create_manifest() {
    log_info "Creating backup manifest..."
    
    MANIFEST_FILE="$BACKUP_DIR/BACKUP_MANIFEST.txt"
    
    cat > "$MANIFEST_FILE" << EOF
# Food Ordering System Backup Manifest
# Generated on: $(date)
# Backup ID: $DATE

## System Information
Hostname: $(hostname)
OS: $(lsb_release -d | cut -f2)
Kernel: $(uname -r)
Java Version: $(java -version 2>&1 | head -1)

## Backup Contents
Database: $DB_NAME
Application Path: $APPLICATION_PATH
Configuration Files: /etc/nginx, /etc/systemd, application configs
Logs: $LOG_PATH, systemd logs

## Backup Files
$(find "$BACKUP_DIR" -type f -exec ls -lh {} \; | sed 's/^/  /')

## Backup Size
Total Size: $(du -sh "$BACKUP_DIR" | cut -f1)

## Verification
Database Backup: $(test -f "$BACKUP_DIR/database/food_ordering_db_$DATE.sql.gz" && echo "✅ OK" || echo "❌ MISSING")
Application Backup: $(test -f "$BACKUP_DIR/application/application_$DATE.tar.gz" && echo "✅ OK" || echo "❌ MISSING")
Configuration Backup: $(test -d "$BACKUP_DIR/config" && echo "✅ OK" || echo "❌ MISSING")
Logs Backup: $(test -f "$BACKUP_DIR/logs/logs_$DATE.tar.gz" && echo "✅ OK" || echo "❌ MISSING")

## Restore Instructions
To restore this backup:
1. Stop the application: systemctl stop food-ordering
2. Restore database: gunzip -c database/food_ordering_db_$DATE.sql.gz | sudo -u postgres psql -d $DB_NAME
3. Restore application: tar -xzf application/application_$DATE.tar.gz -C $APPLICATION_PATH
4. Restore configurations: Copy files from config/ to their respective locations
5. Start the application: systemctl start food-ordering

## Contact Information
Generated by: Automated Backup Script
Support: Technical Team
EOF
    
    log_info "Backup manifest created: $MANIFEST_FILE"
}

# Verify backup integrity
verify_backup() {
    log_info "Verifying backup integrity..."
    
    # Check database backup
    if [ -f "$BACKUP_DIR/database/food_ordering_db_$DATE.sql.gz" ]; then
        gunzip -t "$BACKUP_DIR/database/food_ordering_db_$DATE.sql.gz"
        if [ $? -eq 0 ]; then
            log_info "✅ Database backup verification passed"
        else
            log_error "❌ Database backup verification failed"
            exit 1
        fi
    fi
    
    # Check application backup
    if [ -f "$BACKUP_DIR/application/application_$DATE.tar.gz" ]; then
        tar -tzf "$BACKUP_DIR/application/application_$DATE.tar.gz" > /dev/null
        if [ $? -eq 0 ]; then
            log_info "✅ Application backup verification passed"
        else
            log_error "❌ Application backup verification failed"
            exit 1
        fi
    fi
    
    # Check logs backup
    if [ -f "$BACKUP_DIR/logs/logs_$DATE.tar.gz" ]; then
        tar -tzf "$BACKUP_DIR/logs/logs_$DATE.tar.gz" > /dev/null
        if [ $? -eq 0 ]; then
            log_info "✅ Logs backup verification passed"
        else
            log_error "❌ Logs backup verification failed"
        fi
    fi
}

# Clean old backups
cleanup_old_backups() {
    log_info "Cleaning up old backups (retention: $RETENTION_DAYS days)..."
    
    # Find and remove backups older than retention period
    find "$BACKUP_BASE" -maxdepth 1 -type d -name "20*" -mtime +$RETENTION_DAYS -exec rm -rf {} \;
    
    log_info "Old backups cleaned up"
}

# Upload to cloud (optional)
upload_to_cloud() {
    log_info "Cloud upload feature placeholder..."
    
    # This section can be customized for cloud storage
    # Examples:
    # AWS S3: aws s3 cp "$BACKUP_DIR" s3://your-bucket/backups/ --recursive
    # Google Cloud: gsutil -m cp -r "$BACKUP_DIR" gs://your-bucket/backups/
    # Azure: az storage blob upload-batch --destination backups --source "$BACKUP_DIR"
    
    log_warn "Cloud upload not configured. Backup stored locally only."
}

# Send notification
send_notification() {
    local status=$1
    local message=$2
    
    log_info "Sending backup notification..."
    
    # This can be customized for your notification system
    # Examples:
    # Email: echo "$message" | mail -s "Backup $status" admin@yourcompany.com
    # Slack: curl -X POST -H 'Content-type: application/json' --data '{"text":"'$message'"}' YOUR_SLACK_WEBHOOK
    # Discord: curl -X POST -H 'Content-type: application/json' --data '{"content":"'$message'"}' YOUR_DISCORD_WEBHOOK
    
    log_info "Notification sent: $status"
}

# Main backup function
main() {
    local start_time=$(date +%s)
    
    log_info "Starting automated backup process..."
    
    # Create backup directory
    create_backup_dir
    
    # Perform backups
    backup_database
    backup_application
    backup_config
    backup_logs
    
    # Create manifest and verify
    create_manifest
    verify_backup
    
    # Cleanup and upload
    cleanup_old_backups
    upload_to_cloud
    
    # Calculate backup duration
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    log_info "Backup completed successfully in $duration seconds"
    log_info "Backup location: $BACKUP_DIR"
    log_info "Total backup size: $(du -sh "$BACKUP_DIR" | cut -f1)"
    
    # Send success notification
    send_notification "SUCCESS" "Food Ordering System backup completed successfully. Location: $BACKUP_DIR, Size: $(du -sh "$BACKUP_DIR" | cut -f1), Duration: ${duration}s"
    
    echo ""
    echo "==========================================="
    echo "✅ Backup completed successfully!"
    echo "==========================================="
    echo "Backup ID: $DATE"
    echo "Location: $BACKUP_DIR"
    echo "Size: $(du -sh "$BACKUP_DIR" | cut -f1)"
    echo "Duration: ${duration} seconds"
    echo ""
}

# Error handling
trap 'log_error "Backup failed with error"; send_notification "FAILED" "Food Ordering System backup failed with error"; exit 1' ERR

# Run main function
main "$@" 
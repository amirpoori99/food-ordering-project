#!/bin/bash

# ================================================================
# FOOD ORDERING SYSTEM - BACKUP SCRIPT
# ================================================================

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/backup.conf"
LOG_FILE="/var/log/food-ordering/backup.log"
BACKUP_BASE_DIR="/var/backups/food-ordering"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="${BACKUP_BASE_DIR}/${DATE}"

# Default configuration (can be overridden by backup.conf)
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="food_ordering_prod"
DB_USER="food_ordering_backup"
DB_PASSWORD=""
RETENTION_DAYS=30
COMPRESS_BACKUPS=true
VERIFY_BACKUPS=true
EMAIL_NOTIFICATIONS=false
EMAIL_TO=""

# Load configuration if exists
if [[ -f "${CONFIG_FILE}" ]]; then
    source "${CONFIG_FILE}"
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo -e "${timestamp} [${level}] ${message}" | tee -a "${LOG_FILE}"
}

info() { log "INFO" "$*"; }
warn() { log "WARN" "${YELLOW}$*${NC}"; }
error() { log "ERROR" "${RED}$*${NC}"; }
success() { log "SUCCESS" "${GREEN}$*${NC}"; }

# Check dependencies
check_dependencies() {
    local deps=("pg_dump" "gzip" "tar")
    
    for dep in "${deps[@]}"; do
        if ! command -v "$dep" &> /dev/null; then
            error "Required dependency '$dep' is not installed"
            exit 1
        fi
    done
}

# Create backup directory
create_backup_dir() {
    if [[ ! -d "${BACKUP_DIR}" ]]; then
        mkdir -p "${BACKUP_DIR}"
        info "Created backup directory: ${BACKUP_DIR}"
    fi
}

# Database backup
backup_database() {
    info "Starting database backup..."
    
    local db_backup_file="${BACKUP_DIR}/database_${DATE}.sql"
    
    # Set password for pg_dump
    export PGPASSWORD="${DB_PASSWORD}"
    
    # Create database dump
    pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" \
        --verbose --clean --create --if-exists \
        --format=plain \
        > "${db_backup_file}" 2>>"${LOG_FILE}"
    
    if [[ $? -eq 0 ]]; then
        success "Database backup completed: ${db_backup_file}"
        
        # Compress if enabled
        if [[ "${COMPRESS_BACKUPS}" == "true" ]]; then
            gzip "${db_backup_file}"
            success "Database backup compressed: ${db_backup_file}.gz"
        fi
    else
        error "Database backup failed"
        return 1
    fi
    
    unset PGPASSWORD
}

# Application files backup
backup_application_files() {
    info "Starting application files backup..."
    
    local app_dirs=(
        "/opt/food-ordering"
        "/etc/food-ordering"
        "/var/log/food-ordering"
    )
    
    local app_backup_file="${BACKUP_DIR}/application_${DATE}.tar"
    
    # Create tar archive
    tar -cf "${app_backup_file}" \
        --exclude="*.log" \
        --exclude="*.tmp" \
        --exclude="target/*" \
        "${app_dirs[@]}" 2>>"${LOG_FILE}"
    
    if [[ $? -eq 0 ]]; then
        success "Application files backup completed: ${app_backup_file}"
        
        # Compress if enabled
        if [[ "${COMPRESS_BACKUPS}" == "true" ]]; then
            gzip "${app_backup_file}"
            success "Application files backup compressed: ${app_backup_file}.gz"
        fi
    else
        error "Application files backup failed"
        return 1
    fi
}

# Configuration backup
backup_configuration() {
    info "Starting configuration backup..."
    
    local config_dirs=(
        "/etc/food-ordering"
        "/etc/systemd/system/food-ordering.service"
        "/etc/nginx/sites-available/food-ordering"
        "/etc/ssl/food-ordering"
    )
    
    local config_backup_file="${BACKUP_DIR}/configuration_${DATE}.tar"
    
    # Create tar archive (ignore missing files)
    tar -cf "${config_backup_file}" \
        --ignore-failed-read \
        "${config_dirs[@]}" 2>>"${LOG_FILE}"
    
    if [[ $? -eq 0 || $? -eq 1 ]]; then  # 1 means some files missing but ok
        success "Configuration backup completed: ${config_backup_file}"
        
        # Compress if enabled
        if [[ "${COMPRESS_BACKUPS}" == "true" ]]; then
            gzip "${config_backup_file}"
            success "Configuration backup compressed: ${config_backup_file}.gz"
        fi
    else
        error "Configuration backup failed"
        return 1
    fi
}

# Create backup manifest
create_manifest() {
    info "Creating backup manifest..."
    
    local manifest_file="${BACKUP_DIR}/manifest.txt"
    
    cat > "${manifest_file}" << EOF
# Food Ordering System Backup Manifest
# Created: $(date)
# Backup Directory: ${BACKUP_DIR}

BACKUP_DATE=${DATE}
DB_NAME=${DB_NAME}
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
COMPRESSED=${COMPRESS_BACKUPS}
VERIFIED=${VERIFY_BACKUPS}

# Files in this backup:
EOF
    
    ls -la "${BACKUP_DIR}" >> "${manifest_file}"
    
    # Calculate checksums
    if command -v sha256sum &> /dev/null; then
        echo "" >> "${manifest_file}"
        echo "# SHA256 Checksums:" >> "${manifest_file}"
        cd "${BACKUP_DIR}"
        sha256sum *.sql* *.tar* 2>/dev/null >> "${manifest_file}" || true
    fi
    
    success "Backup manifest created: ${manifest_file}"
}

# Verify backup integrity
verify_backup() {
    if [[ "${VERIFY_BACKUPS}" != "true" ]]; then
        return 0
    fi
    
    info "Verifying backup integrity..."
    
    local verification_passed=true
    
    # Check if files exist and are not empty
    for file in "${BACKUP_DIR}"/*.{sql,tar}*; do
        if [[ -f "$file" ]] && [[ -s "$file" ]]; then
            info "✓ File exists and not empty: $(basename "$file")"
        else
            error "✗ File missing or empty: $(basename "$file")"
            verification_passed=false
        fi
    done
    
    # Test compressed files
    for file in "${BACKUP_DIR}"/*.gz; do
        if [[ -f "$file" ]]; then
            if gzip -t "$file" 2>/dev/null; then
                info "✓ Compressed file is valid: $(basename "$file")"
            else
                error "✗ Compressed file is corrupted: $(basename "$file")"
                verification_passed=false
            fi
        fi
    done
    
    if [[ "$verification_passed" == "true" ]]; then
        success "Backup verification passed"
    else
        error "Backup verification failed"
        return 1
    fi
}

# Clean old backups
cleanup_old_backups() {
    info "Cleaning up old backups (keeping last ${RETENTION_DAYS} days)..."
    
    # Find and remove directories older than retention period
    find "${BACKUP_BASE_DIR}" -type d -name "20*" -mtime +${RETENTION_DAYS} -exec rm -rf {} + 2>/dev/null || true
    
    # Count remaining backups
    local remaining_backups=$(find "${BACKUP_BASE_DIR}" -type d -name "20*" | wc -l)
    info "Cleanup completed. ${remaining_backups} backup sets remaining."
}

# Send email notification
send_notification() {
    if [[ "${EMAIL_NOTIFICATIONS}" != "true" ]] || [[ -z "${EMAIL_TO}" ]]; then
        return 0
    fi
    
    local status=$1
    local subject="Food Ordering System Backup - ${status}"
    local message_file="/tmp/backup_notification_${DATE}.txt"
    
    cat > "${message_file}" << EOF
Food Ordering System Backup Report

Status: ${status}
Date: $(date)
Backup Directory: ${BACKUP_DIR}

Backup Details:
$(cat "${BACKUP_DIR}/manifest.txt" 2>/dev/null || echo "Manifest not available")

Log Summary:
$(tail -20 "${LOG_FILE}")
EOF
    
    # Send email using mail command (if available)
    if command -v mail &> /dev/null; then
        mail -s "${subject}" "${EMAIL_TO}" < "${message_file}"
        info "Email notification sent to ${EMAIL_TO}"
    fi
    
    rm -f "${message_file}"
}

# Calculate backup size
calculate_backup_size() {
    local size_bytes=$(du -sb "${BACKUP_DIR}" 2>/dev/null | cut -f1)
    local size_mb=$((size_bytes / 1024 / 1024))
    info "Total backup size: ${size_mb} MB"
}

# Main backup function
run_backup() {
    info "Starting Food Ordering System backup..."
    info "Backup directory: ${BACKUP_DIR}"
    
    local backup_status="SUCCESS"
    
    # Check dependencies
    check_dependencies
    
    # Create backup directory
    create_backup_dir
    
    # Perform backups
    backup_database || backup_status="FAILED"
    backup_application_files || backup_status="FAILED"
    backup_configuration || backup_status="FAILED"
    
    # Create manifest
    create_manifest
    
    # Verify backup
    verify_backup || backup_status="FAILED"
    
    # Calculate size
    calculate_backup_size
    
    # Cleanup old backups
    cleanup_old_backups
    
    # Send notification
    send_notification "${backup_status}"
    
    if [[ "${backup_status}" == "SUCCESS" ]]; then
        success "Backup completed successfully!"
        exit 0
    else
        error "Backup completed with errors!"
        exit 1
    fi
}

# Recovery functions
restore_database() {
    local backup_file=$1
    
    if [[ ! -f "${backup_file}" ]]; then
        error "Backup file not found: ${backup_file}"
        return 1
    fi
    
    info "Restoring database from: ${backup_file}"
    
    # Decompress if needed
    if [[ "${backup_file}" == *.gz ]]; then
        backup_file=$(gunzip -c "${backup_file}")
    fi
    
    export PGPASSWORD="${DB_PASSWORD}"
    
    psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" \
        < "${backup_file}"
    
    unset PGPASSWORD
    
    success "Database restored successfully"
}

# Usage information
usage() {
    cat << EOF
Usage: $0 [COMMAND] [OPTIONS]

Commands:
    backup          Perform full system backup
    restore-db      Restore database from backup file
    list-backups    List available backups
    cleanup         Clean old backups
    verify          Verify backup integrity
    help            Show this help message

Options:
    --config FILE   Use custom configuration file
    --retention N   Keep backups for N days (default: 30)
    --no-compress   Don't compress backup files
    --no-verify     Skip backup verification

Examples:
    $0 backup
    $0 restore-db /var/backups/food-ordering/20231201_120000/database_20231201_120000.sql.gz
    $0 list-backups
    $0 cleanup --retention 14

Configuration:
    Edit backup.conf to customize settings
EOF
}

# Parse command line arguments
case "${1:-}" in
    backup)
        run_backup
        ;;
    restore-db)
        if [[ -z "${2:-}" ]]; then
            error "Please specify backup file path"
            usage
            exit 1
        fi
        restore_database "$2"
        ;;
    list-backups)
        echo "Available backups:"
        ls -la "${BACKUP_BASE_DIR}"/*/manifest.txt 2>/dev/null | sed 's|/manifest.txt||' || echo "No backups found"
        ;;
    cleanup)
        cleanup_old_backups
        ;;
    verify)
        if [[ -z "${2:-}" ]]; then
            error "Please specify backup directory"
            exit 1
        fi
        BACKUP_DIR="$2"
        verify_backup
        ;;
    help|--help|-h)
        usage
        ;;
    *)
        error "Unknown command: ${1:-}"
        usage
        exit 1
        ;;
esac 
#!/bin/bash

# ================================================================
# سیستم سفارش غذا - اسکریپت پیشرفته پشتیبان‌گیری
# نسخه: ۲.۰ - پیاده‌سازی فاز ۳۲
# ================================================================

set -euo pipefail  # تنظیمات امنیتی: خروج در صورت خطا، استفاده از متغیرهای تعریف نشده، و pipe failures

# تنظیمات اولیه
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"  # مسیر دایرکتوری اسکریپت
CONFIG_FILE="${SCRIPT_DIR}/backup.conf"  # فایل پیکربندی
LOG_FILE="/var/log/food-ordering/backup.log"  # فایل لاگ
BACKUP_BASE_DIR="/var/backups/food-ordering"  # مسیر اصلی پشتیبان‌گیری
DATE=$(date +%Y%m%d_%H%M%S)  # تاریخ و زمان فعلی برای نام‌گذاری
BACKUP_DIR="${BACKUP_BASE_DIR}/${DATE}"  # مسیر کامل پشتیبان‌گیری

# تنظیمات پیش‌فرض دیتابیس (می‌تواند توسط backup.conf بازنویسی شود)
DB_HOST="localhost"  # میزبان دیتابیس
DB_PORT="5432"  # پورت دیتابیس
DB_NAME="food_ordering_prod"  # نام دیتابیس
DB_USER="food_ordering_backup"  # کاربر دیتابیس
DB_PASSWORD=""  # رمز عبور دیتابیس
RETENTION_DAYS=30  # تعداد روزهای نگهداری نسخه‌های قدیمی
COMPRESS_BACKUPS=true  # فشرده‌سازی پشتیبان‌گیری
VERIFY_BACKUPS=true  # تأیید صحت پشتیبان‌گیری
EMAIL_NOTIFICATIONS=false  # اعلان‌های ایمیل
EMAIL_TO=""  # آدرس ایمیل دریافت‌کننده
BACKUP_DATABASE=true  # پشتیبان‌گیری از دیتابیس
BACKUP_APPLICATION=true  # پشتیبان‌گیری از فایل‌های اپلیکیشن
BACKUP_CONFIGURATION=true  # پشتیبان‌گیری از تنظیمات
BACKUP_LOGS=true  # پشتیبان‌گیری از لاگ‌ها
ENCRYPT_BACKUPS=false  # رمزنگاری پشتیبان‌گیری
ENCRYPTION_PASSWORD=""  # رمز عبور رمزنگاری
UPLOAD_TO_CLOUD=false  # آپلود به ابر
CLOUD_PROVIDER=""  # ارائه‌دهنده ابر
CLOUD_BUCKET=""  # سطل ابر
CLOUD_CREDENTIALS=""  # اعتبارنامه‌های ابر

# بارگذاری پیکربندی اگر وجود داشته باشد
if [[ -f "${CONFIG_FILE}" ]]; then
    source "${CONFIG_FILE}"  # بارگذاری تنظیمات از فایل پیکربندی
fi

# رنگ‌ها برای خروجی
RED='\033[0;31m'  # قرمز برای خطاها
GREEN='\033[0;32m'  # سبز برای موفقیت
YELLOW='\033[1;33m'  # زرد برای هشدارها
BLUE='\033[0;34m'  # آبی برای اطلاعات
PURPLE='\033[0;35m'  # بنفش برای debug
CYAN='\033[0;36m'  # فیروزه‌ای برای عنوان‌ها
NC='\033[0m'  # بدون رنگ

# تابع پیشرفته لاگ‌گیری با چرخش
log() {
    local level=$1  # سطح لاگ (INFO, WARN, ERROR, SUCCESS, DEBUG)
    shift
    local message="$*"  # پیام لاگ
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')  # برچسب زمانی
    echo -e "${timestamp} [${level}] ${message}" | tee -a "${LOG_FILE}"  # نمایش و ذخیره در فایل
    
    # چرخش فایل لاگ اگر خیلی بزرگ باشد (>100MB)
    if [[ -f "${LOG_FILE}" ]] && [[ $(stat -f%z "${LOG_FILE}" 2>/dev/null || stat -c%s "${LOG_FILE}" 2>/dev/null || echo 0) -gt 104857600 ]]; then
        mv "${LOG_FILE}" "${LOG_FILE}.old"  # انتقال فایل قدیمی
        touch "${LOG_FILE}"  # ایجاد فایل جدید
        log "INFO" "فایل لاگ چرخش شد"  # ثبت لاگ چرخش
    fi
}

# توابع کمکی برای لاگ‌گیری
info() { log "INFO" "$*"; }  # اطلاعات عمومی
warn() { log "WARN" "${YELLOW}$*${NC}"; }  # هشدار
error() { log "ERROR" "${RED}$*${NC}"; }  # خطا
success() { log "SUCCESS" "${GREEN}$*${NC}"; }  # موفقیت
debug() { log "DEBUG" "${BLUE}$*${NC}"; }  # اطلاعات debug

# بررسی پیشرفته وابستگی‌ها
check_dependencies() {
    local deps=("pg_dump" "gzip" "tar")  # وابستگی‌های اجباری
    local optional_deps=("aws" "gcloud" "az" "openssl" "sha256sum")  # وابستگی‌های اختیاری
    
    info "بررسی وابستگی‌های مورد نیاز..."
    for dep in "${deps[@]}"; do
        if ! command -v "$dep" &> /dev/null; then
            error "وابستگی اجباری '$dep' نصب نشده است"
            exit 1  # خروج با کد خطا
        fi
        debug "✓ وابستگی اجباری یافت شد: $dep"
    done
    
    info "بررسی وابستگی‌های اختیاری..."
    for dep in "${optional_deps[@]}"; do
        if command -v "$dep" &> /dev/null; then
            debug "✓ وابستگی اختیاری یافت شد: $dep"
        else
            warn "وابستگی اختیاری '$dep' یافت نشد"
        fi
    done
}

# ایجاد دایرکتوری پشتیبان‌گیری با ساختار پیشرفته
create_backup_dir() {
    if [[ ! -d "${BACKUP_DIR}" ]]; then
        mkdir -p "${BACKUP_DIR}"  # ایجاد دایرکتوری اصلی
        mkdir -p "${BACKUP_DIR}/database"  # دایرکتوری دیتابیس
        mkdir -p "${BACKUP_DIR}/application"  # دایرکتوری اپلیکیشن
        mkdir -p "${BACKUP_DIR}/configuration"  # دایرکتوری تنظیمات
        mkdir -p "${BACKUP_DIR}/logs"  # دایرکتوری لاگ‌ها
        mkdir -p "${BACKUP_DIR}/metadata"  # دایرکتوری متادیتا
        info "ساختار پیشرفته دایرکتوری پشتیبان‌گیری ایجاد شد: ${BACKUP_DIR}"
    fi
}

# پشتیبان‌گیری پیشرفته دیتابیس با تأیید
backup_database() {
    if [[ "${BACKUP_DATABASE}" != "true" ]]; then
        info "پشتیبان‌گیری دیتابیس رد شد (در پیکربندی غیرفعال شده)"
        return 0
    fi
    
    info "شروع پشتیبان‌گیری پیشرفته دیتابیس..."
    
    local db_backup_file="${BACKUP_DIR}/database/database_${DATE}.sql"  # فایل پشتیبان دیتابیس
    local db_checksum_file="${BACKUP_DIR}/database/database_${DATE}.checksum"  # فایل checksum
    
    # تنظیم رمز عبور برای pg_dump
    export PGPASSWORD="${DB_PASSWORD}"
    
    # ایجاد dump دیتابیس با گزینه‌های پیشرفته
    pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" \
        --verbose --clean --create --if-exists \
        --format=plain \
        --no-password \
        --no-owner \
        --no-privileges \
        > "${db_backup_file}" 2>>"${LOG_FILE}"
    
    if [[ $? -eq 0 ]]; then
        success "پشتیبان‌گیری دیتابیس تکمیل شد: ${db_backup_file}"
        
        # ایجاد checksum برای تأیید یکپارچگی
        if command -v sha256sum &> /dev/null; then
            sha256sum "${db_backup_file}" > "${db_checksum_file}"
            debug "Checksum دیتابیس ایجاد شد: ${db_checksum_file}"
        fi
        
        # فشرده‌سازی اگر فعال باشد
        if [[ "${COMPRESS_BACKUPS}" == "true" ]]; then
            gzip "${db_backup_file}"
            success "پشتیبان‌گیری دیتابیس فشرده شد: ${db_backup_file}.gz"
            
            # به‌روزرسانی checksum برای فایل فشرده
            if command -v sha256sum &> /dev/null; then
                sha256sum "${db_backup_file}.gz" > "${db_checksum_file}.gz"
            fi
        fi
        
        # رمزنگاری اگر فعال باشد
        if [[ "${ENCRYPT_BACKUPS}" == "true" ]] && command -v openssl &> /dev/null; then
            if [[ -n "${ENCRYPTION_PASSWORD}" ]]; then
                local encrypted_file="${db_backup_file}.gz.enc"
                echo "${ENCRYPTION_PASSWORD}" | openssl enc -aes-256-cbc -salt \
                    -in "${db_backup_file}.gz" -out "${encrypted_file}" -pass stdin
                success "پشتیبان‌گیری دیتابیس رمزنگاری شد: ${encrypted_file}"
                
                # حذف فایل غیررمزنگاری شده
                rm "${db_backup_file}.gz"
            else
                warn "رمزنگاری فعال است اما رمز عبور ارائه نشده"
            fi
        fi
    else
        error "پشتیبان‌گیری دیتابیس ناموفق بود"
        return 1
    fi
    
    unset PGPASSWORD  # پاک کردن رمز عبور از محیط
}

# پشتیبان‌گیری پیشرفته فایل‌های اپلیکیشن
backup_application_files() {
    if [[ "${BACKUP_APPLICATION}" != "true" ]]; then
        info "پشتیبان‌گیری فایل‌های اپلیکیشن رد شد (در پیکربندی غیرفعال شده)"
        return 0
    fi
    
    info "شروع پشتیبان‌گیری پیشرفته فایل‌های اپلیکیشن..."
    
    local app_dirs=(
        "/opt/food-ordering"  # دایرکتوری اصلی اپلیکیشن
        "/etc/food-ordering"  # دایرکتوری تنظیمات
        "/var/log/food-ordering"  # دایرکتوری لاگ‌ها
    )
    
    local app_backup_file="${BACKUP_DIR}/application/application_${DATE}.tar"  # فایل پشتیبان اپلیکیشن
    local app_checksum_file="${BACKUP_DIR}/application/application_${DATE}.checksum"  # فایل checksum
    
    # ایجاد آرشیو tar با گزینه‌های پیشرفته
    tar -cf "${app_backup_file}" \
        --exclude="*.log" \
        --exclude="*.tmp" \
        --exclude="target/*" \
        --exclude="node_modules/*" \
        --exclude=".git/*" \
        --exclude="*.db" \
        --exclude="*.db-shm" \
        --exclude="*.db-wal" \
        --exclude="*.pid" \
        --exclude="*.lock" \
        "${app_dirs[@]}" 2>>"${LOG_FILE}"
    
    if [[ $? -eq 0 ]]; then
        success "پشتیبان‌گیری فایل‌های اپلیکیشن تکمیل شد: ${app_backup_file}"
        
        # ایجاد checksum
        if command -v sha256sum &> /dev/null; then
            sha256sum "${app_backup_file}" > "${app_checksum_file}"
            debug "Checksum اپلیکیشن ایجاد شد: ${app_checksum_file}"
        fi
        
        # فشرده‌سازی اگر فعال باشد
        if [[ "${COMPRESS_BACKUPS}" == "true" ]]; then
            gzip "${app_backup_file}"
            success "پشتیبان‌گیری فایل‌های اپلیکیشن فشرده شد: ${app_backup_file}.gz"
            
            # به‌روزرسانی checksum برای فایل فشرده
            if command -v sha256sum &> /dev/null; then
                sha256sum "${app_backup_file}.gz" > "${app_checksum_file}.gz"
            fi
        fi
    else
        error "پشتیبان‌گیری فایل‌های اپلیکیشن ناموفق بود"
        return 1
    fi
}

# پشتیبان‌گیری پیشرفته تنظیمات
backup_configuration() {
    if [[ "${BACKUP_CONFIGURATION}" != "true" ]]; then
        info "پشتیبان‌گیری تنظیمات رد شد (در پیکربندی غیرفعال شده)"
        return 0
    fi
    
    info "شروع پشتیبان‌گیری پیشرفته تنظیمات..."
    
    local config_dirs=(
        "/etc/food-ordering"  # تنظیمات اپلیکیشن
        "/etc/systemd/system/food-ordering.service"  # فایل سرویس systemd
        "/etc/nginx/sites-available/food-ordering"  # تنظیمات nginx
        "/etc/ssl/food-ordering"  # گواهینامه‌های SSL
        "/etc/cron.d/food-ordering"  # وظایف cron
    )
    
    local config_backup_file="${BACKUP_DIR}/configuration/config_${DATE}.tar"  # فایل پشتیبان تنظیمات
    local config_checksum_file="${BACKUP_DIR}/configuration/config_${DATE}.checksum"  # فایل checksum
    
    # ایجاد آرشیو tar برای تنظیمات
    tar -cf "${config_backup_file}" \
        --exclude="*.log" \
        --exclude="*.tmp" \
        --exclude="*.pid" \
        --exclude="*.lock" \
        "${config_dirs[@]}" 2>>"${LOG_FILE}"
    
    if [[ $? -eq 0 ]]; then
        success "پشتیبان‌گیری تنظیمات تکمیل شد: ${config_backup_file}"
        
        # ایجاد checksum
        if command -v sha256sum &> /dev/null; then
            sha256sum "${config_backup_file}" > "${config_checksum_file}"
            debug "Checksum تنظیمات ایجاد شد: ${config_checksum_file}"
        fi
        
        # فشرده‌سازی اگر فعال باشد
        if [[ "${COMPRESS_BACKUPS}" == "true" ]]; then
            gzip "${config_backup_file}"
            success "پشتیبان‌گیری تنظیمات فشرده شد: ${config_backup_file}.gz"
            
            # به‌روزرسانی checksum برای فایل فشرده
            if command -v sha256sum &> /dev/null; then
                sha256sum "${config_backup_file}.gz" > "${config_checksum_file}.gz"
            fi
        fi
    else
        error "پشتیبان‌گیری تنظیمات ناموفق بود"
        return 1
    fi
}

# New: Log files backup
backup_log_files() {
    if [[ "${BACKUP_LOGS}" != "true" ]]; then
        info "پشتیبان‌گیری لاگ‌ها رد شد (در پیکربندی غیرفعال شده)"
        return 0
    fi
    
    info "شروع پشتیبان‌گیری لاگ‌ها..."
    
    local log_dirs=(
        "/var/log/food-ordering"
        "/opt/food-ordering/logs"
    )
    
    local log_backup_file="${BACKUP_DIR}/logs/logs_${DATE}.tar"
    local log_checksum_file="${BACKUP_DIR}/logs/logs_${DATE}.checksum"
    
    # ایجاد آرشیو tar از فایل‌های لاگ (فقط آخرین 7 روز را نگهداری کنید)
    find "${log_dirs[@]}" -name "*.log" -mtime -7 -type f 2>/dev/null | \
        tar -cf "${log_backup_file}" -T - 2>>"${LOG_FILE}"
    
    if [[ $? -eq 0 ]]; then
        success "پشتیبان‌گیری لاگ‌ها تکمیل شد: ${log_backup_file}"
        
        # ایجاد checksum
        if command -v sha256sum &> /dev/null; then
            sha256sum "${log_backup_file}" > "${log_checksum_file}"
            debug "Checksum لاگ‌ها ایجاد شد: ${log_checksum_file}"
        fi
        
        # فشرده‌سازی اگر فعال باشد
        if [[ "${COMPRESS_BACKUPS}" == "true" ]]; then
            gzip "${log_backup_file}"
            success "پشتیبان‌گیری لاگ‌ها فشرده شد: ${log_backup_file}.gz"
            
            # به‌روزرسانی checksum برای فایل فشرده
            if command -v sha256sum &> /dev/null; then
                sha256sum "${log_backup_file}.gz" > "${log_checksum_file}.gz"
            fi
        fi
    else
        warn "پشتیبان‌گیری لاگ‌ها با هشدارها تکمیل شد (برخی فایل‌ها ممکن است از دست رفته)"
    fi
}

# Enhanced backup verification
verify_backup() {
    if [[ "${VERIFY_BACKUPS}" != "true" ]]; then
        info "تأیید یکپارچگی پشتیبان‌گیری رد شد (در پیکربندی غیرفعال شده)"
        return 0
    fi
    
    info "تأیید یکپارچگی پشتیبان‌گیری..."
    
    local verification_failed=false
    
    # Verify checksums
    for checksum_file in "${BACKUP_DIR}"/*/*.checksum*; do
        if [[ -f "${checksum_file}" ]]; then
            if ! sha256sum -c "${checksum_file}" &>/dev/null; then
                error "تأیید checksum برای: ${checksum_file} ناموفق بود"
                verification_failed=true
            else
                debug "✓ تأیید checksum برای: ${checksum_file}"
            fi
        fi
    done
    
    # Verify file sizes
    for backup_file in "${BACKUP_DIR}"/*/*.{sql,tar}*; do
        if [[ -f "${backup_file}" ]]; then
            local file_size=$(stat -f%z "${backup_file}" 2>/dev/null || stat -c%s "${backup_file}" 2>/dev/null || echo 0)
            if [[ "${file_size}" -eq 0 ]]; then
                error "فایل پشتیبان خالی است: ${backup_file}"
                verification_failed=true
            else
                debug "✓ تأیید اندازه فایل: ${backup_file} (${file_size} بایت)"
            fi
        fi
    done
    
    if [[ "${verification_failed}" == "true" ]]; then
        error "تأیید یکپارچگی ناموفق بود"
        return 1
    else
        success "تأیید یکپارچگی پشتیبان‌گیری تکمیل شد با موفقیت"
    fi
}

# New: Cloud upload functionality
upload_to_cloud() {
    if [[ "${UPLOAD_TO_CLOUD}" != "true" ]]; then
        info "آپلود به ابر رد شد (در پیکربندی غیرفعال شده)"
        return 0
    fi
    
    info "آپلود پشتیبان به ابر ذخیره‌سازی..."
    
    case "${CLOUD_PROVIDER}" in
        "aws")
            if command -v aws &> /dev/null; then
                aws s3 sync "${BACKUP_DIR}" "s3://${CLOUD_BUCKET}/backups/${DATE}/" \
                    --delete --quiet
                if [[ $? -eq 0 ]]; then
                    success "پشتیبان به AWS S3 آپلود شد: s3://${CLOUD_BUCKET}/backups/${DATE}/"
                else
                    error "آپلود به AWS S3 ناموفق بود"
                    return 1
                fi
            else
                error "CLI AWS یافت نشد"
                return 1
            fi
            ;;
        "gcp")
            if command -v gsutil &> /dev/null; then
                gsutil -m cp -r "${BACKUP_DIR}" "gs://${CLOUD_BUCKET}/backups/${DATE}/"
                if [[ $? -eq 0 ]]; then
                    success "پشتیبان به Google Cloud Storage آپلود شد: gs://${CLOUD_BUCKET}/backups/${DATE}/"
                else
                    error "آپلود به Google Cloud Storage ناموفق بود"
                    return 1
                fi
            else
                error "SDK Google Cloud یافت نشد"
                return 1
            fi
            ;;
        "azure")
            if command -v az &> /dev/null; then
                az storage blob upload-batch \
                    --source "${BACKUP_DIR}" \
                    --destination "${CLOUD_BUCKET}" \
                    --destination-path "backups/${DATE}/"
                if [[ $? -eq 0 ]]; then
                    success "پشتیبان به Azure Blob Storage آپلود شد: ${CLOUD_BUCKET}/backups/${DATE}/"
                else
                    error "آپلود به Azure Blob Storage ناموفق بود"
                    return 1
                fi
            else
                error "CLI Azure یافت نشد"
                return 1
            fi
            ;;
        *)
            warn "Unknown cloud provider: ${CLOUD_PROVIDER}"
            return 1
            ;;
    esac
}

# Enhanced backup cleanup
cleanup_old_backups() {
    info "Cleaning up old backups (retention: ${RETENTION_DAYS} days)..."
    
    local deleted_count=0
    local current_time=$(date +%s)
    local retention_seconds=$((RETENTION_DAYS * 24 * 60 * 60))
    
    for backup_dir in "${BACKUP_BASE_DIR}"/*; do
        if [[ -d "${backup_dir}" ]]; then
            local dir_name=$(basename "${backup_dir}")
            local dir_time=$(date -d "${dir_name}" +%s 2>/dev/null || echo 0)
            
            if [[ "${dir_time}" -gt 0 ]] && [[ $((current_time - dir_time)) -gt "${retention_seconds}" ]]; then
                rm -rf "${backup_dir}"
                deleted_count=$((deleted_count + 1))
                debug "Deleted old backup: ${dir_name}"
            fi
        fi
    done
    
    if [[ "${deleted_count}" -gt 0 ]]; then
        success "Cleaned up ${deleted_count} old backup(s)"
    else
        info "No old backups to clean up"
    fi
}

# Enhanced manifest creation
create_manifest() {
    info "Creating enhanced backup manifest..."
    
    local manifest_file="${BACKUP_DIR}/metadata/manifest.txt"
    local system_info_file="${BACKUP_DIR}/metadata/system_info.txt"
    
    # Create main manifest
    cat > "${manifest_file}" << EOF
# Food Ordering System Enhanced Backup Manifest
# Version: 2.0
# Created: $(date)
# Backup Directory: ${BACKUP_DIR}

BACKUP_DATE=${DATE}
DB_NAME=${DB_NAME}
DB_HOST=${DB_HOST}
DB_PORT=${DB_PORT}
COMPRESSED=${COMPRESS_BACKUPS}
VERIFIED=${VERIFY_BACKUPS}
ENCRYPTED=${ENCRYPT_BACKUPS}
UPLOADED_TO_CLOUD=${UPLOAD_TO_CLOUD}
CLOUD_PROVIDER=${CLOUD_PROVIDER}

# Backup Configuration:
BACKUP_DATABASE=${BACKUP_DATABASE}
BACKUP_APPLICATION=${BACKUP_APPLICATION}
BACKUP_CONFIGURATION=${BACKUP_CONFIGURATION}
BACKUP_LOGS=${BACKUP_LOGS}

# Files in this backup:
EOF
    
    find "${BACKUP_DIR}" -type f -exec ls -la {} \; >> "${manifest_file}"
    
    # Create system information
    cat > "${system_info_file}" << EOF
# System Information
# Generated: $(date)

OS: $(uname -a)
Hostname: $(hostname)
Uptime: $(uptime)
Disk Usage: $(df -h .)
Memory Usage: $(free -h)
Java Version: $(java -version 2>&1 | head -1)
Maven Version: $(mvn -version 2>&1 | head -1)
EOF
    
    # Calculate checksums for all files
    if command -v sha256sum &> /dev/null; then
        echo "" >> "${manifest_file}"
        echo "# SHA256 Checksums:" >> "${manifest_file}"
        cd "${BACKUP_DIR}"
        find . -type f -name "*.sql*" -o -name "*.tar*" -o -name "*.gz*" | \
            xargs sha256sum >> "${manifest_file}" 2>/dev/null || true
    fi
    
    success "Enhanced manifest created: ${manifest_file}"
}

# Enhanced email notification
send_notification() {
    if [[ "${EMAIL_NOTIFICATIONS}" != "true" ]] || [[ -z "${EMAIL_TO}" ]]; then
        return 0
    fi
    
    info "Sending email notification..."
    
    local subject="Food Ordering System Backup - ${DATE}"
    local body="Backup completed successfully at $(date)
    
Backup Directory: ${BACKUP_DIR}
Database: ${DB_NAME}
Compressed: ${COMPRESS_BACKUPS}
Verified: ${VERIFY_BACKUPS}
Encrypted: ${ENCRYPT_BACKUPS}
Cloud Upload: ${UPLOAD_TO_CLOUD}

Total backup size: $(du -sh "${BACKUP_DIR}" | cut -f1)
"
    
    if command -v mail &> /dev/null; then
        echo "${body}" | mail -s "${subject}" "${EMAIL_TO}"
        success "Email notification sent to ${EMAIL_TO}"
    else
        warn "Mail command not available, skipping email notification"
    fi
}

# Main execution function
main() {
    info "Starting enhanced backup process..."
    info "Backup directory: ${BACKUP_DIR}"
    
    # Create log directory
    mkdir -p "$(dirname "${LOG_FILE}")"
    
    # Check dependencies
    check_dependencies
    
    # Create backup directory
    create_backup_dir
    
    # Perform backups
    local backup_failed=false
    
    backup_database || backup_failed=true
    backup_application_files || backup_failed=true
    backup_configuration || backup_failed=true
    backup_log_files || backup_failed=true
    
    # Verify backup if enabled
    if [[ "${backup_failed}" == "false" ]]; then
        verify_backup || backup_failed=true
    fi
    
    # Upload to cloud if enabled
    if [[ "${backup_failed}" == "false" ]]; then
        upload_to_cloud || warn "Cloud upload failed, but backup completed"
    fi
    
    # Create manifest
    create_manifest
    
    # Cleanup old backups
    cleanup_old_backups
    
    # Send notification
    if [[ "${backup_failed}" == "false" ]]; then
        send_notification
        success "Enhanced backup process completed successfully"
        info "Backup location: ${BACKUP_DIR}"
        info "Total size: $(du -sh "${BACKUP_DIR}" | cut -f1)"
    else
        error "Backup process completed with errors"
        exit 1
    fi
}

# Execute main function
main "$@" 
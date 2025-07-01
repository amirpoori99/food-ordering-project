#!/bin/bash

# ================================================================
# اسکریپت پشتیبان‌گیری جامع سیستم سفارش غذا
# این اسکریپت تمام فایل‌ها، پایگاه داده و تنظیمات سیستم را پشتیبان‌گیری می‌کند
# نویسنده: تیم توسعه
# تاریخ آخرین ویرایش: تیر ۱۴۰۴
# نسخه: ۲.۰ - سیستم پشتیبان‌گیری پیشرفته
# ================================================================

# تنظیمات اولیه و متغیرهای محیطی
set -e  # توقف اسکریپت در صورت بروز خطا
set -u  # توقف در صورت استفاده از متغیر تعریف نشده

# ================================================================
# متغیرهای پیکربندی سیستم
# ================================================================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"  # مسیر ریشه پروژه
BACKUP_ROOT="${PROJECT_ROOT}/backups"                           # مسیر اصلی پشتیبان‌ها
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")                             # برچسب زمانی
BACKUP_NAME="food_ordering_backup_${TIMESTAMP}"                # نام فایل پشتیبان
BACKUP_DIR="${BACKUP_ROOT}/${BACKUP_NAME}"                     # مسیر کامل پشتیبان
LOG_FILE="${BACKUP_DIR}/backup.log"                            # فایل گزارش
CONFIG_FILE="${PROJECT_ROOT}/scripts/backup.conf"              # فایل تنظیمات
RETENTION_DAYS=30                                               # مدت نگهداری پشتیبان‌ها (روز)
COMPRESSION_LEVEL=9                                             # سطح فشرده‌سازی (۱-۹)
DATABASE_FILE="food_ordering.db"                                # نام فایل پایگاه داده

# ================================================================
# رنگ‌ها برای نمایش بهتر پیام‌ها
# ================================================================
RED='\033[0;31m'      # قرمز برای خطاها
GREEN='\033[0;32m'    # سبز برای موفقیت
YELLOW='\033[1;33m'   # زرد برای هشدارها
BLUE='\033[0;34m'     # آبی برای اطلاعات
NC='\033[0m'          # بدون رنگ

# ================================================================
# تابع نمایش پیام‌های رنگی
# ================================================================
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

# ================================================================
# تابع بررسی پیش‌نیازها
# ================================================================
check_prerequisites() {
    log_info "بررسی پیش‌نیازهای سیستم..."
    
    # بررسی وجود دستورات ضروری
    local required_commands=("tar" "gzip" "sqlite3" "rsync")
    
    for cmd in "${required_commands[@]}"; do
        if ! command -v "$cmd" &> /dev/null; then
            log_error "دستور '$cmd' یافت نشد. لطفاً آن را نصب کنید."
            exit 1
        fi
    done
    
    # بررسی وجود فایل پایگاه داده
    if [ ! -f "$PROJECT_ROOT/backend/$DATABASE_FILE" ]; then
        log_warning "فایل پایگاه داده یافت نشد: $PROJECT_ROOT/backend/$DATABASE_FILE"
    else
        log_success "فایل پایگاه داده یافت شد"
    fi
    
    log_success "تمام پیش‌نیازها موجود هستند"
}

# ================================================================
# تابع ایجاد ساختار پوشه‌های پشتیبان
# ================================================================
create_backup_structure() {
    log_info "ایجاد ساختار پوشه‌های پشتیبان..."
    
    # ایجاد پوشه اصلی پشتیبان‌ها
    mkdir -p "$BACKUP_ROOT"
    
    # ایجاد پوشه پشتیبان فعلی
    mkdir -p "$BACKUP_DIR"
    
    # ایجاد زیرپوشه‌های مختلف برای دسته‌بندی
    mkdir -p "${BACKUP_DIR}/database"      # پشتیبان پایگاه داده
    mkdir -p "${BACKUP_DIR}/code"          # پشتیبان کدها
    mkdir -p "${BACKUP_DIR}/config"        # پشتیبان تنظیمات
    mkdir -p "${BACKUP_DIR}/logs"          # پشتیبان گزارش‌ها
    mkdir -p "${BACKUP_DIR}/docs"          # پشتیبان مستندات
    
    log_success "ساختار پوشه‌های پشتیبان ایجاد شد"
}

# ================================================================
# تابع پشتیبان‌گیری از پایگاه داده
# ================================================================
backup_database() {
    log_info "شروع پشتیبان‌گیری از پایگاه داده..."
    
    local db_file="${PROJECT_ROOT}/backend/$DATABASE_FILE"
    local backup_db="${BACKUP_DIR}/database/food_ordering_backup.sql"
    
    if [ -f "$db_file" ]; then
        # پشتیبان‌گیری کامل از پایگاه داده SQLite
        sqlite3 "$db_file" ".dump" > "$backup_db"
        
        # فشرده‌سازی فایل پشتیبان
        gzip -$COMPRESSION_LEVEL "$backup_db"
        
        log_success "پشتیبان‌گیری از پایگاه داده با موفقیت انجام شد"
        log_info "حجم فایل پشتیبان: $(du -h "${backup_db}.gz" | cut -f1)"
    else
        log_warning "فایل پایگاه داده یافت نشد: $db_file"
    fi
}

# ================================================================
# تابع پشتیبان‌گیری از کدهای پروژه
# ================================================================
backup_code() {
    log_info "شروع پشتیبان‌گیری از کدهای پروژه..."
    
    # پشتیبان‌گیری از کدهای بک‌اند
    if [ -d "${PROJECT_ROOT}/backend/src" ]; then
        tar -czf "${BACKUP_DIR}/code/backend_src.tar.gz" \
            -C "${PROJECT_ROOT}/backend" src/
        log_success "پشتیبان‌گیری از کدهای بک‌اند انجام شد"
    fi
    
    # پشتیبان‌گیری از کدهای فرانت‌اند
    if [ -d "${PROJECT_ROOT}/frontend-javafx/src" ]; then
        tar -czf "${BACKUP_DIR}/code/frontend_src.tar.gz" \
            -C "${PROJECT_ROOT}/frontend-javafx" src/
        log_success "پشتیبان‌گیری از کدهای فرانت‌اند انجام شد"
    fi
    
    # پشتیبان‌گیری از فایل‌های پیکربندی Maven
    if [ -f "${PROJECT_ROOT}/backend/pom.xml" ]; then
        cp "${PROJECT_ROOT}/backend/pom.xml" "${BACKUP_DIR}/code/backend_pom.xml"
    fi
    
    if [ -f "${PROJECT_ROOT}/frontend-javafx/pom.xml" ]; then
        cp "${PROJECT_ROOT}/frontend-javafx/pom.xml" "${BACKUP_DIR}/code/frontend_pom.xml"
    fi
}

# ================================================================
# تابع پشتیبان‌گیری از تنظیمات
# ================================================================
backup_config() {
    log_info "شروع پشتیبان‌گیری از تنظیمات..."
    
    # پشتیبان‌گیری از فایل‌های تنظیمات
    if [ -f "${PROJECT_ROOT}/backend/src/main/resources/application.properties" ]; then
        cp "${PROJECT_ROOT}/backend/src/main/resources/application.properties" \
           "${BACKUP_DIR}/config/application.properties"
    fi
    
    if [ -f "${PROJECT_ROOT}/backend/src/main/resources/application-production.properties" ]; then
        cp "${PROJECT_ROOT}/backend/src/main/resources/application-production.properties" \
           "${BACKUP_DIR}/config/application-production.properties"
    fi
    
    if [ -f "${PROJECT_ROOT}/backend/src/main/resources/hibernate.cfg.xml" ]; then
        cp "${PROJECT_ROOT}/backend/src/main/resources/hibernate.cfg.xml" \
           "${BACKUP_DIR}/config/hibernate.cfg.xml"
    fi
    
    # پشتیبان‌گیری از اسکریپت‌ها
    if [ -d "${PROJECT_ROOT}/scripts" ]; then
        tar -czf "${BACKUP_DIR}/config/scripts.tar.gz" \
            -C "${PROJECT_ROOT}" scripts/
    fi
    
    log_success "پشتیبان‌گیری از تنظیمات انجام شد"
}

# ================================================================
# تابع پشتیبان‌گیری از مستندات
# ================================================================
backup_documentation() {
    log_info "شروع پشتیبان‌گیری از مستندات..."
    
    # پشتیبان‌گیری از پوشه مستندات
    if [ -d "${PROJECT_ROOT}/docs" ]; then
        tar -czf "${BACKUP_DIR}/docs/documentation.tar.gz" \
            -C "${PROJECT_ROOT}" docs/
        log_success "پشتیبان‌گیری از مستندات انجام شد"
    else
        log_warning "پوشه مستندات یافت نشد"
    fi
    
    # پشتیبان‌گیری از فایل README
    if [ -f "${PROJECT_ROOT}/README.md" ]; then
        cp "${PROJECT_ROOT}/README.md" "${BACKUP_DIR}/docs/README.md"
    fi
}

# ================================================================
# تابع پشتیبان‌گیری از گزارش‌ها
# ================================================================
backup_logs() {
    log_info "شروع پشتیبان‌گیری از گزارش‌ها..."
    
    # پشتیبان‌گیری از گزارش‌های تست
    if [ -d "${PROJECT_ROOT}/backend/target/surefire-reports" ]; then
        tar -czf "${BACKUP_DIR}/logs/backend_test_reports.tar.gz" \
            -C "${PROJECT_ROOT}/backend/target" surefire-reports/
    fi
    
    if [ -d "${PROJECT_ROOT}/frontend-javafx/target/surefire-reports" ]; then
        tar -czf "${BACKUP_DIR}/logs/frontend_test_reports.tar.gz" \
            -C "${PROJECT_ROOT}/frontend-javafx/target" surefire-reports/
    fi
    
    # پشتیبان‌گیری از گزارش‌های پوشش تست
    if [ -d "${PROJECT_ROOT}/backend/target/site/jacoco" ]; then
        tar -czf "${BACKUP_DIR}/logs/backend_coverage_reports.tar.gz" \
            -C "${PROJECT_ROOT}/backend/target/site" jacoco/
    fi
    
    if [ -d "${PROJECT_ROOT}/frontend-javafx/target/site/jacoco" ]; then
        tar -czf "${BACKUP_DIR}/logs/frontend_coverage_reports.tar.gz" \
            -C "${PROJECT_ROOT}/frontend-javafx/target/site" jacoco/
    fi
    
    log_success "پشتیبان‌گیری از گزارش‌ها انجام شد"
}

# ================================================================
# تابع ایجاد فایل اطلاعات پشتیبان
# ================================================================
create_backup_info() {
    log_info "ایجاد فایل اطلاعات پشتیبان..."
    
    local info_file="${BACKUP_DIR}/backup_info.txt"
    
    cat > "$info_file" << EOF
# ================================================================
# اطلاعات پشتیبان سیستم سفارش غذا
# ================================================================
تاریخ پشتیبان‌گیری: $(date)
نام پشتیبان: $BACKUP_NAME
مسیر پشتیبان: $BACKUP_DIR

# ================================================================
# اطلاعات سیستم
# ================================================================
سیستم عامل: $(uname -s)
نسخه هسته: $(uname -r)
معماری: $(uname -m)
کاربر: $(whoami)
مسیر پروژه: $PROJECT_ROOT

# ================================================================
# اطلاعات فایل‌های پشتیبان
# ================================================================
$(find "$BACKUP_DIR" -type f -name "*.gz" -o -name "*.tar" | sort)

# ================================================================
# حجم فایل‌های پشتیبان
# ================================================================
$(du -sh "$BACKUP_DIR"/*)

# ================================================================
# خلاصه پشتیبان‌گیری
# ================================================================
- پایگاه داده: $(if [ -f "${BACKUP_DIR}/database/food_ordering_backup.sql.gz" ]; then echo "موفق"; else echo "ناموفق"; fi)
- کدهای بک‌اند: $(if [ -f "${BACKUP_DIR}/code/backend_src.tar.gz" ]; then echo "موفق"; else echo "ناموفق"; fi)
- کدهای فرانت‌اند: $(if [ -f "${BACKUP_DIR}/code/frontend_src.tar.gz" ]; then echo "موفق"; else echo "ناموفق"; fi)
- تنظیمات: $(if [ -d "${BACKUP_DIR}/config" ]; then echo "موفق"; else echo "ناموفق"; fi)
- مستندات: $(if [ -f "${BACKUP_DIR}/docs/documentation.tar.gz" ]; then echo "موفق"; else echo "ناموفق"; fi)
- گزارش‌ها: $(if [ -d "${BACKUP_DIR}/logs" ]; then echo "موفق"; else echo "ناموفق"; fi)
EOF
    
    log_success "فایل اطلاعات پشتیبان ایجاد شد"
}

# ================================================================
# تابع پاکسازی پشتیبان‌های قدیمی
# ================================================================
cleanup_old_backups() {
    log_info "پاکسازی پشتیبان‌های قدیمی..."
    
    # حذف پشتیبان‌های قدیمی‌تر از RETENTION_DAYS روز
    find "$BACKUP_ROOT" -name "food_ordering_backup_*" -type d -mtime +$RETENTION_DAYS -exec rm -rf {} \;
    
    log_success "پشتیبان‌های قدیمی پاکسازی شدند"
}

# ================================================================
# تابع نمایش خلاصه نهایی
# ================================================================
show_summary() {
    log_info "خلاصه عملیات پشتیبان‌گیری:"
    echo "================================================================"
    echo "نام پشتیبان: $BACKUP_NAME"
    echo "مسیر: $BACKUP_DIR"
    echo "حجم کل: $(du -sh "$BACKUP_DIR" | cut -f1)"
    echo "تعداد فایل‌ها: $(find "$BACKUP_DIR" -type f | wc -l)"
    echo "================================================================"
}

# ================================================================
# تابع اصلی
# ================================================================
main() {
    echo "================================================================"
    echo "🚀 شروع عملیات پشتیبان‌گیری جامع سیستم سفارش غذا"
    echo "📅 تاریخ: $(date)"
    echo "================================================================"
    
    # بررسی پیش‌نیازها
    check_prerequisites
    
    # ایجاد ساختار پوشه‌ها
    create_backup_structure
    
    # اجرای مراحل پشتیبان‌گیری
    backup_database
    backup_code
    backup_config
    backup_documentation
    backup_logs
    
    # ایجاد فایل اطلاعات
    create_backup_info
    
    # پاکسازی پشتیبان‌های قدیمی
    cleanup_old_backups
    
    # نمایش خلاصه
    show_summary
    
    echo "================================================================"
    echo "✅ عملیات پشتیبان‌گیری با موفقیت به پایان رسید"
    echo "📁 مسیر پشتیبان: $BACKUP_DIR"
    echo "📋 گزارش کامل: $LOG_FILE"
    echo "================================================================"
}

# ================================================================
# اجرای تابع اصلی
# ================================================================
main "$@" 
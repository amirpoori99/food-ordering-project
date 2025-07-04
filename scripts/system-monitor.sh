#!/bin/bash

# ================================================================
# اسکریپت مانیتورینگ سیستم سفارش غذا
# این اسکریپت وضعیت سیستم، منابع، سرویس‌ها و عملکرد را بررسی می‌کند
# نویسنده: تیم توسعه
# تاریخ آخرین ویرایش: تیر ۱۴۰۴
# نسخه: ۲.۰ - سیستم مانیتورینگ پیشرفته
# ================================================================

# تنظیمات اولیه و متغیرهای محیطی
set -e  # توقف اسکریپت در صورت بروز خطا

# ================================================================
# متغیرهای پیکربندی سیستم
# ================================================================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"  # مسیر ریشه پروژه
LOG_FILE="/var/log/food-ordering/system-monitor.log"             # فایل گزارش
ALERT_EMAIL="admin@foodordering.ir"                              # ایمیل هشدار
CPU_THRESHOLD=80                                                 # آستانه CPU (درصد)
MEMORY_THRESHOLD=85                                              # آستانه حافظه (درصد)
DISK_THRESHOLD=90                                                # آستانه دیسک (درصد)
SERVICE_NAME="food-ordering"                                     # نام سرویس سیستم

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
# تابع بررسی وضعیت CPU
# ================================================================
check_cpu_usage() {
    log_info "بررسی استفاده از CPU..."
    
    # دریافت درصد استفاده از CPU
    local cpu_usage=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)
    
    log_info "استفاده فعلی CPU: ${cpu_usage}%"
    
    # بررسی آستانه هشدار
    if (( $(echo "$cpu_usage > $CPU_THRESHOLD" | bc -l) )); then
        log_warning "استفاده از CPU بالاتر از آستانه است: ${cpu_usage}% > ${CPU_THRESHOLD}%"
        return 1
    else
        log_success "استفاده از CPU در حد نرمال است"
        return 0
    fi
}

# ================================================================
# تابع بررسی وضعیت حافظه
# ================================================================
check_memory_usage() {
    log_info "بررسی استفاده از حافظه..."
    
    # دریافت اطلاعات حافظه
    local total_mem=$(free -m | awk 'NR==2{printf "%.2f", $2/1024}')
    local used_mem=$(free -m | awk 'NR==2{printf "%.2f", $3/1024}')
    local free_mem=$(free -m | awk 'NR==2{printf "%.2f", $4/1024}')
    local memory_usage=$(free | awk 'NR==2{printf "%.2f", $3*100/$2}')
    
    log_info "حافظه کل: ${total_mem} GB"
    log_info "حافظه استفاده شده: ${used_mem} GB"
    log_info "حافظه آزاد: ${free_mem} GB"
    log_info "درصد استفاده: ${memory_usage}%"
    
    # بررسی آستانه هشدار
    if (( $(echo "$memory_usage > $MEMORY_THRESHOLD" | bc -l) )); then
        log_warning "استفاده از حافظه بالاتر از آستانه است: ${memory_usage}% > ${MEMORY_THRESHOLD}%"
        return 1
    else
        log_success "استفاده از حافظه در حد نرمال است"
        return 0
    fi
}

# ================================================================
# تابع بررسی وضعیت دیسک
# ================================================================
check_disk_usage() {
    log_info "بررسی استفاده از دیسک..."
    
    # بررسی تمام پارتیشن‌ها
    df -h | grep -E '^/dev/' | while read line; do
        local filesystem=$(echo "$line" | awk '{print $1}')
        local size=$(echo "$line" | awk '{print $2}')
        local used=$(echo "$line" | awk '{print $3}')
        local available=$(echo "$line" | awk '{print $4}')
        local usage_percent=$(echo "$line" | awk '{print $5}' | sed 's/%//')
        local mount_point=$(echo "$line" | awk '{print $6}')
        
        log_info "فایل سیستم: $filesystem"
        log_info "اندازه کل: $size"
        log_info "استفاده شده: $used"
        log_info "موجود: $available"
        log_info "درصد استفاده: ${usage_percent}%"
        log_info "نقطه نصب: $mount_point"
        
        # بررسی آستانه هشدار
        if [ "$usage_percent" -gt "$DISK_THRESHOLD" ]; then
            log_warning "استفاده از دیسک بالاتر از آستانه است: ${usage_percent}% > ${DISK_THRESHOLD}% در $mount_point"
        else
            log_success "استفاده از دیسک در حد نرمال است در $mount_point"
        fi
    done
}

# ================================================================
# تابع بررسی وضعیت سرویس سیستم
# ================================================================
check_service_status() {
    log_info "بررسی وضعیت سرویس سیستم..."
    
    # بررسی وضعیت سرویس
    if systemctl is-active --quiet "$SERVICE_NAME"; then
        log_success "سرویس $SERVICE_NAME فعال است"
        
        # بررسی وضعیت دقیق سرویس
        local service_status=$(systemctl is-active "$SERVICE_NAME")
        local service_enabled=$(systemctl is-enabled "$SERVICE_NAME")
        
        log_info "وضعیت سرویس: $service_status"
        log_info "فعال در راه‌اندازی: $service_enabled"
        
        return 0
    else
        log_error "سرویس $SERVICE_NAME غیرفعال است"
        
        # تلاش برای راه‌اندازی مجدد سرویس
        log_info "تلاش برای راه‌اندازی مجدد سرویس..."
        if systemctl restart "$SERVICE_NAME"; then
            log_success "سرویس با موفقیت راه‌اندازی مجدد شد"
            return 0
        else
            log_error "راه‌اندازی مجدد سرویس ناموفق بود"
            return 1
        fi
    fi
}

# ================================================================
# تابع بررسی وضعیت شبکه
# ================================================================
check_network_status() {
    log_info "بررسی وضعیت شبکه..."
    
    # بررسی اتصال اینترنت
    if ping -c 1 8.8.8.8 &> /dev/null; then
        log_success "اتصال اینترنت برقرار است"
    else
        log_warning "اتصال اینترنت قطع است"
    fi
    
    # بررسی پورت‌های باز
    local http_port=$(netstat -tlnp | grep :8080 | wc -l)
    local https_port=$(netstat -tlnp | grep :8443 | wc -l)
    
    if [ "$http_port" -gt 0 ]; then
        log_success "پورت HTTP (8080) باز است"
    else
        log_warning "پورت HTTP (8080) بسته است"
    fi
    
    if [ "$https_port" -gt 0 ]; then
        log_success "پورت HTTPS (8443) باز است"
    else
        log_warning "پورت HTTPS (8443) بسته است"
    fi
}

# ================================================================
# تابع بررسی فایل‌های لاگ
# ================================================================
check_log_files() {
    log_info "بررسی فایل‌های لاگ..."
    
    local log_dir="/var/log/food-ordering"
    
    if [ -d "$log_dir" ]; then
        # بررسی اندازه فایل‌های لاگ
        local total_log_size=$(du -sh "$log_dir" | cut -f1)
        log_info "اندازه کل فایل‌های لاگ: $total_log_size"
        
        # بررسی فایل‌های لاگ بزرگ
        find "$log_dir" -name "*.log" -size +100M 2>/dev/null | while read log_file; do
            log_warning "فایل لاگ بزرگ یافت شد: $log_file"
        done
        
        # بررسی خطاهای اخیر
        local recent_errors=$(grep -i "error\|exception\|failed" "$log_dir"/*.log 2>/dev/null | tail -10 | wc -l)
        if [ "$recent_errors" -gt 0 ]; then
            log_warning "تعداد خطاهای اخیر: $recent_errors"
        else
            log_success "هیچ خطای اخیری یافت نشد"
        fi
    else
        log_warning "پوشه لاگ یافت نشد: $log_dir"
    fi
}

# ================================================================
# تابع بررسی پایگاه داده
# ================================================================
check_database() {
    log_info "بررسی وضعیت پایگاه داده..."
    
    local db_file="$PROJECT_ROOT/backend/food_ordering.db"
    
    if [ -f "$db_file" ]; then
        # بررسی اندازه پایگاه داده
        local db_size=$(du -h "$db_file" | cut -f1)
        log_info "اندازه پایگاه داده: $db_size"
        
        # بررسی دسترسی به پایگاه داده
        if sqlite3 "$db_file" "SELECT COUNT(*) FROM sqlite_master;" &> /dev/null; then
            log_success "دسترسی به پایگاه داده برقرار است"
            
            # بررسی تعداد رکوردها
            local user_count=$(sqlite3 "$db_file" "SELECT COUNT(*) FROM users;" 2>/dev/null || echo "0")
            local order_count=$(sqlite3 "$db_file" "SELECT COUNT(*) FROM orders;" 2>/dev/null || echo "0")
            
            log_info "تعداد کاربران: $user_count"
            log_info "تعداد سفارش‌ها: $order_count"
        else
            log_error "دسترسی به پایگاه داده مشکل دارد"
        fi
    else
        log_warning "فایل پایگاه داده یافت نشد: $db_file"
    fi
}

# ================================================================
# تابع ارسال هشدار
# ================================================================
send_alert() {
    local message="$1"
    local subject="هشدار سیستم سفارش غذا - $(date)"
    
    log_warning "ارسال هشدار: $message"
    
    # ارسال ایمیل هشدار (در صورت نصب mailutils)
    if command -v mail &> /dev/null; then
        echo "$message" | mail -s "$subject" "$ALERT_EMAIL"
        log_info "هشدار به ایمیل ارسال شد: $ALERT_EMAIL"
    else
        log_warning "دستور mail یافت نشد، هشدار ارسال نشد"
    fi
}

# ================================================================
# تابع تولید گزارش
# ================================================================
generate_report() {
    log_info "تولید گزارش مانیتورینگ..."
    
    local report_file="/var/log/food-ordering/monitor-report-$(date +%Y%m%d_%H%M%S).txt"
    
    {
        echo "================================================================"
        echo "گزارش مانیتورینگ سیستم سفارش غذا"
        echo "تاریخ: $(date)"
        echo "================================================================"
        echo ""
        echo "وضعیت سیستم:"
        echo "- CPU: $(top -bn1 | grep 'Cpu(s)' | awk '{print $2}' | cut -d'%' -f1)%"
        echo "- حافظه: $(free | awk 'NR==2{printf "%.2f%%", $3*100/$2}')"
        echo "- دیسک: $(df -h / | awk 'NR==2{print $5}')"
        echo "- سرویس: $(systemctl is-active $SERVICE_NAME)"
        echo ""
        echo "جزئیات کامل در فایل لاگ: $LOG_FILE"
        echo "================================================================"
    } > "$report_file"
    
    log_success "گزارش مانیتورینگ تولید شد: $report_file"
}

# ================================================================
# تابع اصلی
# ================================================================
main() {
    echo "================================================================"
    echo "🔍 شروع مانیتورینگ سیستم سفارش غذا"
    echo "📅 تاریخ: $(date)"
    echo "================================================================"
    
    # ایجاد پوشه لاگ در صورت عدم وجود
    mkdir -p "$(dirname "$LOG_FILE")"
    
    # بررسی‌های مختلف سیستم
    local has_issues=false
    
    check_cpu_usage || has_issues=true
    check_memory_usage || has_issues=true
    check_disk_usage
    check_service_status || has_issues=true
    check_network_status
    check_log_files
    check_database
    
    # تولید گزارش
    generate_report
    
    # ارسال هشدار در صورت وجود مشکل
    if [ "$has_issues" = true ]; then
        send_alert "مشکلاتی در سیستم شناسایی شد. لطفاً گزارش کامل را بررسی کنید."
    fi
    
    echo "================================================================"
    echo "✅ مانیتورینگ سیستم به پایان رسید"
    echo "📋 گزارش کامل: $LOG_FILE"
    echo "================================================================"
}

# ================================================================
# اجرای تابع اصلی
# ================================================================
main "$@"

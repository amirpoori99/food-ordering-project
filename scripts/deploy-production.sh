#!/bin/bash

# ================================================================
# اسکریپت استقرار تولید سیستم سفارش غذا
# این اسکریپت سیستم را در محیط تولید مستقر می‌کند
# نویسنده: تیم توسعه
# تاریخ آخرین ویرایش: تیر ۱۴۰۴
# نسخه: ۲.۰ - سیستم استقرار پیشرفته
# ================================================================

# تنظیمات اولیه و متغیرهای محیطی
set -e  # توقف اسکریپت در صورت بروز خطا
set -u  # توقف در صورت استفاده از متغیر تعریف نشده

# ================================================================
# متغیرهای پیکربندی سیستم
# ================================================================
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"  # مسیر ریشه پروژه
DEPLOY_DIR="/opt/food-ordering"                                # مسیر استقرار در سرور
BACKUP_DIR="/var/backups/food-ordering"                       # مسیر پشتیبان‌ها
LOG_FILE="/var/log/food-ordering/deploy.log"                  # فایل گزارش
SERVICE_NAME="food-ordering"                                   # نام سرویس systemd
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"                      # تنظیمات JVM
PORT=8080                                                      # پورت پیش‌فرض

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
    local required_commands=("java" "mvn" "systemctl" "sudo")
    
    for cmd in "${required_commands[@]}"; do
        if ! command -v "$cmd" &> /dev/null; then
            log_error "دستور '$cmd' یافت نشد. لطفاً آن را نصب کنید."
            exit 1
        fi
    done
    
    # بررسی نسخه Java
    local java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    log_info "نسخه Java: $java_version"
    
    # بررسی نسخه Maven
    local maven_version=$(mvn -version 2>&1 | head -1)
    log_info "نسخه Maven: $maven_version"
    
    log_success "تمام پیش‌نیازها موجود هستند"
}

# ================================================================
# تابع ایجاد ساختار پوشه‌های استقرار
# ================================================================
create_deployment_structure() {
    log_info "ایجاد ساختار پوشه‌های استقرار..."
    
    # ایجاد پوشه‌های اصلی
    sudo mkdir -p "$DEPLOY_DIR"
    sudo mkdir -p "$BACKUP_DIR"
    sudo mkdir -p "$(dirname "$LOG_FILE")"
    
    # ایجاد زیرپوشه‌های مورد نیاز
    sudo mkdir -p "${DEPLOY_DIR}/bin"        # اسکریپت‌های اجرایی
    sudo mkdir -p "${DEPLOY_DIR}/config"     # فایل‌های پیکربندی
    sudo mkdir -p "${DEPLOY_DIR}/logs"       # فایل‌های گزارش
    sudo mkdir -p "${DEPLOY_DIR}/data"       # فایل‌های داده
    sudo mkdir -p "${DEPLOY_DIR}/temp"       # فایل‌های موقت
    
    # تنظیم مجوزهای دسترسی
    sudo chown -R food-ordering:food-ordering "$DEPLOY_DIR"
    sudo chmod -R 755 "$DEPLOY_DIR"
    
    log_success "ساختار پوشه‌های استقرار ایجاد شد"
}

# ================================================================
# تابع پشتیبان‌گیری از نسخه قبلی
# ================================================================
backup_current_version() {
    log_info "پشتیبان‌گیری از نسخه فعلی..."
    
    if [ -d "$DEPLOY_DIR" ]; then
        local backup_name="backup_$(date +%Y%m%d_%H%M%S)"
        local backup_path="$BACKUP_DIR/$backup_name"
        
        sudo cp -r "$DEPLOY_DIR" "$backup_path"
        log_success "پشتیبان‌گیری انجام شد: $backup_path"
        
        # حذف پشتیبان‌های قدیمی (بیش از ۷ روز)
        sudo find "$BACKUP_DIR" -name "backup_*" -type d -mtime +7 -exec rm -rf {} \;
    else
        log_warning "نسخه قبلی یافت نشد، پشتیبان‌گیری رد شد"
    fi
}

# ================================================================
# تابع ساخت پروژه
# ================================================================
build_project() {
    log_info "شروع ساخت پروژه..."
    
    cd "$PROJECT_ROOT"
    
    # پاکسازی و ساخت بک‌اند
    log_info "ساخت بک‌اند..."
    cd backend
    mvn clean package -DskipTests
    if [ $? -eq 0 ]; then
        log_success "ساخت بک‌اند با موفقیت انجام شد"
    else
        log_error "ساخت بک‌اند ناموفق بود"
        exit 1
    fi
    
    # پاکسازی و ساخت فرانت‌اند
    log_info "ساخت فرانت‌اند..."
    cd ../frontend-javafx
    mvn clean package -DskipTests
    if [ $? -eq 0 ]; then
        log_success "ساخت فرانت‌اند با موفقیت انجام شد"
    else
        log_error "ساخت فرانت‌اند ناموفق بود"
        exit 1
    fi
    
    cd "$PROJECT_ROOT"
}

# ================================================================
# تابع کپی فایل‌های پروژه
# ================================================================
copy_project_files() {
    log_info "کپی فایل‌های پروژه..."
    
    # کپی JAR بک‌اند
    sudo cp "$PROJECT_ROOT/backend/target/food-ordering-backend-1.0.0.jar" \
           "$DEPLOY_DIR/bin/food-ordering-backend.jar"
    
    # کپی JAR فرانت‌اند
    sudo cp "$PROJECT_ROOT/frontend-javafx/target/food-ordering-frontend-1.0.0.jar" \
           "$DEPLOY_DIR/bin/food-ordering-frontend.jar"
    
    # کپی فایل‌های پیکربندی
    sudo cp "$PROJECT_ROOT/backend/src/main/resources/application-production.properties" \
           "$DEPLOY_DIR/config/application.properties"
    
    # کپی اسکریپت‌ها
    sudo cp -r "$PROJECT_ROOT/scripts" "$DEPLOY_DIR/"
    
    # تنظیم مجوزهای دسترسی
    sudo chown -R food-ordering:food-ordering "$DEPLOY_DIR"
    sudo chmod +x "$DEPLOY_DIR/bin"/*.jar
    
    log_success "فایل‌های پروژه کپی شدند"
}

# ================================================================
# تابع ایجاد فایل سرویس systemd
# ================================================================
create_systemd_service() {
    log_info "ایجاد فایل سرویس systemd..."
    
    local service_file="/etc/systemd/system/${SERVICE_NAME}.service"
    
    sudo tee "$service_file" > /dev/null << EOF
[Unit]
Description=Food Ordering System Backend
After=network.target
Wants=network.target

[Service]
Type=simple
User=food-ordering
Group=food-ordering
WorkingDirectory=$DEPLOY_DIR
ExecStart=/usr/bin/java $JAVA_OPTS -jar bin/food-ordering-backend.jar
ExecReload=/bin/kill -HUP \$MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=$SERVICE_NAME

# تنظیمات امنیتی
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=$DEPLOY_DIR/logs $DEPLOY_DIR/data

# تنظیمات محدودیت منابع
LimitNOFILE=65536
LimitNPROC=4096

[Install]
WantedBy=multi-user.target
EOF
    
    # بارگذاری مجدد systemd
    sudo systemctl daemon-reload
    
    log_success "فایل سرویس systemd ایجاد شد"
}

# ================================================================
# تابع راه‌اندازی سرویس
# ================================================================
start_service() {
    log_info "راه‌اندازی سرویس..."
    
    # فعال‌سازی سرویس
    sudo systemctl enable "$SERVICE_NAME"
    
    # شروع سرویس
    sudo systemctl start "$SERVICE_NAME"
    
    # بررسی وضعیت سرویس
    sleep 5
    if sudo systemctl is-active --quiet "$SERVICE_NAME"; then
        log_success "سرویس با موفقیت راه‌اندازی شد"
    else
        log_error "راه‌اندازی سرویس ناموفق بود"
        sudo systemctl status "$SERVICE_NAME"
        exit 1
    fi
}

# ================================================================
# تابع بررسی سلامت سیستم
# ================================================================
health_check() {
    log_info "بررسی سلامت سیستم..."
    
    # انتظار برای راه‌اندازی کامل
    sleep 10
    
    # بررسی وضعیت سرویس
    if ! sudo systemctl is-active --quiet "$SERVICE_NAME"; then
        log_error "سرویس فعال نیست"
        return 1
    fi
    
    # بررسی دسترسی به API
    local health_url="http://localhost:$PORT/health"
    local response=$(curl -s -o /dev/null -w "%{http_code}" "$health_url" || echo "000")
    
    if [ "$response" = "200" ]; then
        log_success "بررسی سلامت API موفق بود"
    else
        log_error "بررسی سلامت API ناموفق بود (کد: $response)"
        return 1
    fi
    
    # بررسی استفاده از منابع
    local memory_usage=$(ps -o %mem --no-headers -p $(pgrep -f food-ordering-backend))
    local cpu_usage=$(ps -o %cpu --no-headers -p $(pgrep -f food-ordering-backend))
    
    log_info "استفاده از حافظه: ${memory_usage}%"
    log_info "استفاده از CPU: ${cpu_usage}%"
    
    return 0
}

# ================================================================
# تابع تنظیم فایروال
# ================================================================
configure_firewall() {
    log_info "تنظیم فایروال..."
    
    # بررسی وجود ufw
    if command -v ufw &> /dev/null; then
        sudo ufw allow $PORT/tcp
        log_success "پورت $PORT در فایروال باز شد"
    else
        log_warning "ufw یافت نشد، تنظیم فایروال رد شد"
    fi
    
    # بررسی وجود iptables
    if command -v iptables &> /dev/null; then
        sudo iptables -A INPUT -p tcp --dport $PORT -j ACCEPT
        log_success "قانون iptables برای پورت $PORT اضافه شد"
    fi
}

# ================================================================
# تابع نمایش خلاصه نهایی
# ================================================================
show_summary() {
    log_info "خلاصه عملیات استقرار:"
    echo "================================================================"
    echo "مسیر استقرار: $DEPLOY_DIR"
    echo "نام سرویس: $SERVICE_NAME"
    echo "پورت: $PORT"
    echo "وضعیت سرویس: $(sudo systemctl is-active $SERVICE_NAME)"
    echo "================================================================"
    
    # نمایش دستورات مفید
    echo ""
    echo "دستورات مفید:"
    echo "  وضعیت سرویس: sudo systemctl status $SERVICE_NAME"
    echo "  مشاهده لاگ‌ها: sudo journalctl -u $SERVICE_NAME -f"
    echo "  راه‌اندازی مجدد: sudo systemctl restart $SERVICE_NAME"
    echo "  توقف سرویس: sudo systemctl stop $SERVICE_NAME"
}

# ================================================================
# تابع اصلی
# ================================================================
main() {
    echo "================================================================"
    echo "🚀 شروع عملیات استقرار تولید سیستم سفارش غذا"
    echo "📅 تاریخ: $(date)"
    echo "================================================================"
    
    # بررسی پیش‌نیازها
    check_prerequisites
    
    # ایجاد ساختار پوشه‌ها
    create_deployment_structure
    
    # پشتیبان‌گیری از نسخه قبلی
    backup_current_version
    
    # ساخت پروژه
    build_project
    
    # کپی فایل‌های پروژه
    copy_project_files
    
    # ایجاد فایل سرویس
    create_systemd_service
    
    # راه‌اندازی سرویس
    start_service
    
    # تنظیم فایروال
    configure_firewall
    
    # بررسی سلامت
    if health_check; then
        log_success "بررسی سلامت سیستم موفق بود"
    else
        log_error "بررسی سلامت سیستم ناموفق بود"
        exit 1
    fi
    
    # نمایش خلاصه
    show_summary
    
    echo "================================================================"
    echo "✅ عملیات استقرار با موفقیت به پایان رسید"
    echo "🌐 سیستم در آدرس http://localhost:$PORT در دسترس است"
    echo "📋 گزارش کامل: $LOG_FILE"
    echo "================================================================"
}

# ================================================================
# اجرای تابع اصلی
# ================================================================
main "$@"

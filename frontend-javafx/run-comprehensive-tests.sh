#!/bin/bash

# ================================================================
# اسکریپت اجرای جامع تست‌های سیستم سفارش غذا (Frontend)
# این اسکریپت تمام تست‌های واحد، یکپارچگی، عملکرد و امنیت را اجرا می‌کند
# و گزارش کامل تولید می‌نماید.
# نویسنده: تیم توسعه
# تاریخ آخرین ویرایش: تیر ۱۴۰۴
# نسخه: ۲.۰ - سیستم تست جامع پیشرفته
# ================================================================

# نمایش هدر و اطلاعات اولیه
# ----------------------------------------------------------------
echo "================================================================================"
echo "🎯 FOOD ORDERING SYSTEM - COMPREHENSIVE TEST EXECUTION"
echo "📅 Date: $(date)"
echo "================================================================================"

echo ""
echo "🔍 Checking Prerequisites..."  # بررسی پیش‌نیازها و ابزارهای مورد نیاز

# بررسی نصب بودن Maven - ابزار اصلی ساخت پروژه
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found! Please install Maven first."
    echo "💡 Install with: sudo apt-get install maven (Ubuntu/Debian)"
    echo "💡 Or download from: https://maven.apache.org/download.cgi"
    exit 1
fi
# اگر Maven نصب باشد پیام موفقیت نمایش داده می‌شود
echo "✅ Maven found"

# بررسی نصب بودن Java - محیط اجرایی مورد نیاز
if ! command -v java &> /dev/null; then
    echo "❌ Java not found! Please install Java 11+ first."
    echo "💡 Install with: sudo apt-get install openjdk-17-jdk"
    exit 1
fi
# اگر Java نصب باشد پیام موفقیت نمایش داده می‌شود
echo "✅ Java found"

# بررسی وجود پروژه backend - برای تست‌های یکپارچگی
if [ ! -f "../backend/pom.xml" ]; then
    echo "❌ Backend project not found!"
    echo "💡 Make sure backend folder exists in parent directory"
    exit 1
fi
# اگر پروژه بک‌اند وجود داشته باشد پیام موفقیت نمایش داده می‌شود
echo "✅ Backend project found"

echo ""
echo "🚀 Starting Backend Server..."  # راه‌اندازی سرور بک‌اند برای تست‌های یکپارچگی
cd ../backend  # ورود به پوشه بک‌اند
mvn spring-boot:run &   # اجرای سرور بک‌اند در پس‌زمینه
BACKEND_PID=$!          # ذخیره شناسه فرآیند سرور
cd ../frontend-javafx   # بازگشت به پوشه فرانت‌اند

echo "⏳ Waiting for backend to start (30 seconds)..."  # صبر برای راه‌اندازی کامل سرور
sleep 30

# بررسی اجرای صحیح سرور بک‌اند
if kill -0 $BACKEND_PID 2>/dev/null; then
    echo "✅ Backend server started successfully"
else
    echo "⚠️ Backend server may not have started properly"
fi

echo ""
echo "🧪 Running Comprehensive Test Suite..."  # اجرای تست‌های جامع
echo ""

# تابع اجرای هر دسته تست با مدیریت خطا
# این تابع هر دسته تست را اجرا کرده و نتیجه را گزارش می‌دهد
run_test_category() {
    local test_pattern=$1      # الگوی نام فایل‌های تست
    local category_name=$2     # نام دسته تست برای نمایش
    
    echo "📊 Executing: $category_name"
    
    # اجرای تست‌ها با Maven و مدیریت خطاها
    if mvn test -Dtest="$test_pattern" -Dmaven.test.failure.ignore=true; then
        echo "✅ $category_name completed"
    else
        echo "⚠️ $category_name had some failures (check reports)"
    fi
    echo ""
}

# اجرای تمام دسته‌های تست به ترتیب اولویت
run_test_category "**/*SimpleTest" "Unit Tests"           # تست‌های واحد - بررسی عملکرد جداگانه کلاس‌ها
run_test_category "**/*IntegrationTest" "Integration Tests" # تست‌های یکپارچگی - بررسی تعامل بین بخش‌ها
run_test_category "**/*PerformanceTest" "Performance Tests" # تست‌های عملکرد - بررسی سرعت و کارایی
run_test_category "**/*SecurityTest" "Security Tests"       # تست‌های امنیتی - بررسی آسیب‌پذیری‌ها
run_test_category "**/*EdgeCaseTest" "Edge Case Tests"      # تست‌های شرایط خاص - بررسی حالت‌های غیرعادی
run_test_category "ComprehensiveTestSuite" "Comprehensive Test Suite" # تست جامع - اجرای تمام تست‌ها

echo "📈 Generating Test Reports..."  # تولید گزارش تست‌ها
mvn surefire-report:report-only       # تولید گزارش Surefire
mvn surefire-report:failsafe-report-only  # تولید گزارش Failsafe

echo ""
echo "📋 Test Results Location:"  # نمایش مسیر گزارش‌ها
if [ -f "target/surefire-reports/index.html" ]; then
    echo "✅ HTML Report: $(pwd)/target/surefire-reports/index.html"
    echo "💡 Open in browser: file://$(pwd)/target/surefire-reports/index.html"
else
    echo "⚠️ Test report not found at expected location"
fi

# بررسی وجود گزارش پوشش تست
if [ -f "target/site/jacoco/index.html" ]; then
    echo "✅ Coverage Report: $(pwd)/target/site/jacoco/index.html"
    echo "💡 Open in browser: file://$(pwd)/target/site/jacoco/index.html"
fi

echo ""
echo "================================================================================"
echo "✅ COMPREHENSIVE TEST EXECUTION COMPLETED"
echo "📊 Check the file paths above for detailed results"
echo "📁 Test reports location: target/surefire-reports/"
echo "📊 Coverage reports location: target/site/jacoco/"
echo "================================================================================"

echo ""
echo "🛑 Stopping Backend Server..."  # توقف سرور بک‌اند
if kill -0 $BACKEND_PID 2>/dev/null; then
    kill $BACKEND_PID
    echo "✅ Backend server stopped"
else
    echo "ℹ️ Backend server was not running"
fi

echo ""
echo "🎉 All done! Press Enter to exit..."  # انتظار برای خروج کاربر
read -r 
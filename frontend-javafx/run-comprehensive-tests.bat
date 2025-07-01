@echo off
REM ================================================================
REM اسکریپت اجرای جامع تست‌های سیستم سفارش غذا - ویندوز (Frontend)
REM این اسکریپت تمام تست‌های واحد، یکپارچگی، عملکرد و امنیت را اجرا می‌کند
REM و گزارش کامل تولید می‌نماید.
REM نویسنده: تیم توسعه
REM تاریخ آخرین ویرایش: تیر ۱۴۰۴
REM نسخه: ۲.۰ - سیستم تست جامع پیشرفته ویندوز
REM ================================================================

REM تنظیمات اولیه و متغیرهای محیطی
REM ----------------------------------------------------------------
setlocal enabledelayedexpansion

REM نمایش هدر و اطلاعات اولیه
echo ================================================================
echo 🎯 FOOD ORDERING SYSTEM - COMPREHENSIVE TEST EXECUTION (WINDOWS)
echo 📅 Date: %date% %time%
echo ================================================================

echo.
echo 🔍 Checking Prerequisites...  REM بررسی پیش‌نیازها و ابزارهای مورد نیاز

REM بررسی نصب بودن Maven - ابزار اصلی ساخت پروژه
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found! Please install Maven first.
    echo 💡 Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
REM اگر Maven نصب باشد پیام موفقیت نمایش داده می‌شود
echo ✅ Maven found

REM بررسی نصب بودن Java - محیط اجرایی مورد نیاز
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java 11+ first.
    echo 💡 Download from: https://adoptium.net/
    pause
    exit /b 1
)
REM اگر Java نصب باشد پیام موفقیت نمایش داده می‌شود
echo ✅ Java found

REM بررسی وجود پروژه backend - برای تست‌های یکپارچگی
if not exist "..\backend\pom.xml" (
    echo ❌ Backend project not found!
    echo 💡 Make sure backend folder exists in parent directory
    pause
    exit /b 1
)
REM اگر پروژه بک‌اند وجود داشته باشد پیام موفقیت نمایش داده می‌شود
echo ✅ Backend project found

echo.
echo 🚀 Starting Backend Server...  REM راه‌اندازی سرور بک‌اند برای تست‌های یکپارچگی
cd ..\backend  REM ورود به پوشه بک‌اند
start "Backend Server" cmd /c "mvn spring-boot:run"  REM اجرای سرور بک‌اند در پنجره جدید
cd ..\frontend-javafx  REM بازگشت به پوشه فرانت‌اند

echo ⏳ Waiting for backend to start (30 seconds)...  REM صبر برای راه‌اندازی کامل سرور
timeout /t 30 /nobreak >nul

echo.
echo 🧪 Running Comprehensive Test Suite...  REM اجرای تست‌های جامع
echo.

REM تابع اجرای هر دسته تست با مدیریت خطا
REM این تابع هر دسته تست را اجرا کرده و نتیجه را گزارش می‌دهد
:run_test_category
set test_pattern=%~1      REM الگوی نام فایل‌های تست
set category_name=%~2     REM نام دسته تست برای نمایش

echo 📊 Executing: %category_name%

REM اجرای تست‌ها با Maven و مدیریت خطاها
mvn test -Dtest="%test_pattern%" -Dmaven.test.failure.ignore=true
if %errorlevel% equ 0 (
    echo ✅ %category_name% completed
) else (
    echo ⚠️ %category_name% had some failures (check reports)
)
echo.
goto :eof

REM اجرای تمام دسته‌های تست به ترتیب اولویت
call :run_test_category "**/*SimpleTest" "Unit Tests"           REM تست‌های واحد - بررسی عملکرد جداگانه کلاس‌ها
call :run_test_category "**/*IntegrationTest" "Integration Tests" REM تست‌های یکپارچگی - بررسی تعامل بین بخش‌ها
call :run_test_category "**/*PerformanceTest" "Performance Tests" REM تست‌های عملکرد - بررسی سرعت و کارایی
call :run_test_category "**/*SecurityTest" "Security Tests"       REM تست‌های امنیتی - بررسی آسیب‌پذیری‌ها
call :run_test_category "**/*EdgeCaseTest" "Edge Case Tests"      REM تست‌های شرایط خاص - بررسی حالت‌های غیرعادی
call :run_test_category "ComprehensiveTestSuite" "Comprehensive Test Suite" REM تست جامع - اجرای تمام تست‌ها

echo 📈 Generating Test Reports...  REM تولید گزارش تست‌ها
mvn surefire-report:report-only       REM تولید گزارش Surefire
mvn surefire-report:failsafe-report-only  REM تولید گزارش Failsafe

echo.
echo 📋 Test Results Location:  REM نمایش مسیر گزارش‌ها
if exist "target\surefire-reports\index.html" (
    echo ✅ HTML Report: %cd%\target\surefire-reports\index.html
    echo 💡 Open in browser: file://%cd%\target\surefire-reports\index.html
) else (
    echo ⚠️ Test report not found at expected location
)

REM بررسی وجود گزارش پوشش تست
if exist "target\site\jacoco\index.html" (
    echo ✅ Coverage Report: %cd%\target\site\jacoco\index.html
    echo 💡 Open in browser: file://%cd%\target\site\jacoco\index.html
)

echo.
echo ================================================================
echo ✅ COMPREHENSIVE TEST EXECUTION COMPLETED
echo 📊 Check the file paths above for detailed results
echo 📁 Test reports location: target\surefire-reports\
echo 📊 Coverage reports location: target\site\jacoco\
echo ================================================================

echo.
echo 🛑 Stopping Backend Server...  REM توقف سرور بک‌اند
taskkill /f /im java.exe >nul 2>&1
echo ✅ Backend server stopped

echo.
echo 🎉 All done! Press any key to exit...  REM انتظار برای خروج کاربر
pause >nul 
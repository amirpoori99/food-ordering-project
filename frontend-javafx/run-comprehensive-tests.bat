@echo off
REM ================================================================
REM اسکریپت اجرای جامع تست‌های سیستم سفارش غذا (Frontend) - ویندوز
REM این اسکریپت تمام تست‌های واحد، یکپارچگی، عملکرد و امنیت را اجرا می‌کند
REM و گزارش کامل تولید می‌نماید.
REM نویسنده: تیم توسعه
REM تاریخ آخرین ویرایش: تیر ۱۴۰۴
REM نسخه: ۲.۰ - سیستم تست جامع پیشرفته
REM ================================================================

echo ================================================================================
echo 🎯 FOOD ORDERING SYSTEM - COMPREHENSIVE TEST EXECUTION
echo 📅 Date: %date% %time%
echo ================================================================================

echo.
echo 🔍 Checking Prerequisites...  REM بررسی پیش‌نیازها و ابزارهای مورد نیاز

:: بررسی نصب بودن Maven - ابزار اصلی ساخت پروژه
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found! Please install Maven first.
    echo 💡 Download from: https://maven.apache.org/download.cgi
    goto :error
)
echo ✅ Maven found

:: بررسی نصب بودن Java - محیط اجرایی مورد نیاز
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java 11+ first.
    goto :error
)
echo ✅ Java found

:: بررسی وجود پروژه backend - برای تست‌های یکپارچگی
if not exist "..\backend\pom.xml" (
    echo ❌ Backend project not found!
    echo 💡 Make sure backend folder exists in parent directory
    goto :error
)
echo ✅ Backend project found

echo.
echo 🚀 Starting Backend Server...  REM راه‌اندازی سرور بک‌اند برای تست‌های یکپارچگی
start "Backend Server" cmd /c "cd ..\backend && mvn spring-boot:run"
echo ⏳ Waiting for backend to start (30 seconds)...  REM صبر برای راه‌اندازی کامل سرور
timeout /t 30 /nobreak

echo.
echo 🧪 Running Comprehensive Test Suite...  REM اجرای تست‌های جامع
echo.

:: اجرای تمام دسته‌های تست با گزارش کامل
echo 📊 Executing: Unit Tests  REM تست‌های واحد - بررسی عملکرد جداگانه کلاس‌ها
mvn test -Dtest="**/*SimpleTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Integration Tests  REM تست‌های یکپارچگی - بررسی تعامل بین بخش‌ها
mvn test -Dtest="**/*IntegrationTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Performance Tests  REM تست‌های عملکرد - بررسی سرعت و کارایی
mvn test -Dtest="**/*PerformanceTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Security Tests  REM تست‌های امنیتی - بررسی آسیب‌پذیری‌ها
mvn test -Dtest="**/*SecurityTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Edge Case Tests  REM تست‌های شرایط خاص - بررسی حالت‌های غیرعادی
mvn test -Dtest="**/*EdgeCaseTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Comprehensive Test Suite  REM تست جامع - اجرای تمام تست‌ها
mvn test -Dtest="ComprehensiveTestSuite" -Dmaven.test.failure.ignore=true

echo.
echo 📈 Generating Test Reports...  REM تولید گزارش تست‌ها
mvn surefire-report:report-only       REM تولید گزارش Surefire
mvn surefire-report:failsafe-report-only  REM تولید گزارش Failsafe

echo.
echo 📋 Opening Test Results...  REM نمایش گزارش‌ها در مرورگر
if exist "target\surefire-reports\index.html" (
    start "Test Results" "target\surefire-reports\index.html"
) else (
    echo ⚠️ Test report not found at expected location
)

echo.
echo ================================================================================
echo ✅ COMPREHENSIVE TEST EXECUTION COMPLETED
echo 📊 Check the opened browser window for detailed results
echo 📁 Test reports location: target\surefire-reports\
echo ================================================================================

echo.
echo 🛑 Stopping Backend Server...  REM توقف سرور بک‌اند
taskkill /f /im java.exe /fi "WindowTitle eq Backend Server*" >nul 2>&1

goto :end

:error
echo.
echo ❌ Test execution failed due to missing prerequisites
echo 💡 Please resolve the issues above and try again
exit /b 1

:end
pause 
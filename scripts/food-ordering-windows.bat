@echo off
REM ================================================================
REM اسکریپت راه‌اندازی سیستم سفارش غذا - ویندوز
REM این اسکریپت سیستم را در محیط توسعه ویندوز راه‌اندازی می‌کند
REM نویسنده: تیم توسعه
REM تاریخ آخرین ویرایش: تیر ۱۴۰۴
REM نسخه: ۲.۰ - سیستم راه‌اندازی پیشرفته ویندوز
REM ================================================================

REM تنظیمات اولیه
setlocal enabledelayedexpansion

REM ================================================================
REM متغیرهای پیکربندی سیستم
REM ================================================================
set "PROJECT_ROOT=%~dp0.."                    REM مسیر ریشه پروژه
set "BACKEND_PORT=8080"                       REM پورت بک‌اند
set "FRONTEND_PORT=8081"                      REM پورت فرانت‌اند
set "JAVA_OPTS_BACKEND=-Xms512m -Xmx2g -XX:+UseG1GC" REM تنظیمات JVM بک‌اند
set "JAVA_OPTS_FRONTEND=-Xms256m -Xmx1g"     REM تنظیمات JVM فرانت‌اند
set "LOG_DIR=%PROJECT_ROOT%\logs"             REM مسیر فایل‌های گزارش

REM ================================================================
REM تابع نمایش پیام‌های رنگی
REM ================================================================
:log_info
echo [INFO] %~1
goto :eof

:log_success
echo [SUCCESS] %~1
goto :eof

:log_warning
echo [WARNING] %~1
goto :eof

:log_error
echo [ERROR] %~1
goto :eof

REM ================================================================
REM تابع بررسی پیش‌نیازها
REM ================================================================
:check_prerequisites
call :log_info "بررسی پیش‌نیازهای سیستم..."

REM بررسی وجود Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    call :log_error "Java یافت نشد. لطفاً آن را نصب کنید."
    echo 💡 دانلود از: https://adoptium.net/
    pause
    exit /b 1
)

REM بررسی وجود Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    call :log_error "Maven یافت نشد. لطفاً آن را نصب کنید."
    echo 💡 دانلود از: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM نمایش نسخه‌ها
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do set "JAVA_VERSION=%%i"
call :log_info "نسخه Java: %JAVA_VERSION%"

for /f "tokens=3" %%i in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do set "MAVEN_VERSION=%%i"
call :log_info "نسخه Maven: %MAVEN_VERSION%"

call :log_success "تمام پیش‌نیازها موجود هستند"
goto :eof

REM ================================================================
REM تابع ایجاد ساختار پوشه‌ها
REM ================================================================
:create_directories
call :log_info "ایجاد ساختار پوشه‌ها..."

REM ایجاد پوشه گزارش‌ها
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM ایجاد پوشه‌های موقت
if not exist "%PROJECT_ROOT%\temp" mkdir "%PROJECT_ROOT%\temp"
if not exist "%PROJECT_ROOT%\temp\backend" mkdir "%PROJECT_ROOT%\temp\backend"
if not exist "%PROJECT_ROOT%\temp\frontend" mkdir "%PROJECT_ROOT%\temp\frontend"

call :log_success "ساختار پوشه‌ها ایجاد شد"
goto :eof

REM ================================================================
REM تابع بررسی پورت‌های در حال استفاده
REM ================================================================
:check_ports
call :log_info "بررسی پورت‌های در حال استفاده..."

REM بررسی پورت بک‌اند
netstat -an | findstr ":%BACKEND_PORT%" >nul
if %errorlevel% equ 0 (
    call :log_warning "پورت %BACKEND_PORT% در حال استفاده است"
    echo 🔍 بررسی فرآیندهای استفاده‌کننده از پورت %BACKEND_PORT%:
    netstat -ano | findstr ":%BACKEND_PORT%"
    echo.
    set /p "CONTINUE=آیا می‌خواهید ادامه دهید؟ (y/n): "
    if /i not "!CONTINUE!"=="y" exit /b 1
) else (
    call :log_success "پورت %BACKEND_PORT% آزاد است"
)

REM بررسی پورت فرانت‌اند
netstat -an | findstr ":%FRONTEND_PORT%" >nul
if %errorlevel% equ 0 (
    call :log_warning "پورت %FRONTEND_PORT% در حال استفاده است"
    echo 🔍 بررسی فرآیندهای استفاده‌کننده از پورت %FRONTEND_PORT%:
    netstat -ano | findstr ":%FRONTEND_PORT%"
    echo.
    set /p "CONTINUE=آیا می‌خواهید ادامه دهید؟ (y/n): "
    if /i not "!CONTINUE!"=="y" exit /b 1
) else (
    call :log_success "پورت %FRONTEND_PORT% آزاد است"
)
goto :eof

REM ================================================================
REM تابع ساخت پروژه
REM ================================================================
:build_project
call :log_info "شروع ساخت پروژه..."

cd /d "%PROJECT_ROOT%"

REM پاکسازی و ساخت بک‌اند
call :log_info "ساخت بک‌اند..."
cd backend
mvn clean compile -q
if %errorlevel% equ 0 (
    call :log_success "ساخت بک‌اند با موفقیت انجام شد"
) else (
    call :log_error "ساخت بک‌اند ناموفق بود"
    pause
    exit /b 1
)

REM پاکسازی و ساخت فرانت‌اند
call :log_info "ساخت فرانت‌اند..."
cd ..\frontend-javafx
mvn clean compile -q
if %errorlevel% equ 0 (
    call :log_success "ساخت فرانت‌اند با موفقیت انجام شد"
) else (
    call :log_error "ساخت فرانت‌اند ناموفق بود"
    pause
    exit /b 1
)

cd /d "%PROJECT_ROOT%"
goto :eof

REM ================================================================
REM تابع راه‌اندازی بک‌اند
REM ================================================================
:start_backend
call :log_info "راه‌اندازی سرور بک‌اند..."

REM تغییر به پوشه بک‌اند
cd /d "%PROJECT_ROOT%\backend"

REM راه‌اندازی سرور بک‌اند در پس‌زمینه
start "Food Ordering Backend" cmd /c "echo 🚀 راه‌اندازی سرور بک‌اند... && echo 📍 پورت: %BACKEND_PORT% && echo 📍 مسیر: %CD% && echo. && java %JAVA_OPTS_BACKEND% -cp target/classes com.myapp.ServerApp"

REM انتظار برای راه‌اندازی سرور
call :log_info "انتظار برای راه‌اندازی سرور بک‌اند..."
timeout /t 10 /nobreak >nul

REM بررسی وضعیت سرور
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:%BACKEND_PORT%/health' -UseBasicParsing -TimeoutSec 5; if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 (
    call :log_success "سرور بک‌اند با موفقیت راه‌اندازی شد"
    call :log_info "🌐 آدرس: http://localhost:%BACKEND_PORT%"
) else (
    call :log_warning "سرور بک‌اند ممکن است هنوز در حال راه‌اندازی باشد"
)

cd /d "%PROJECT_ROOT%"
goto :eof

REM ================================================================
REM تابع راه‌اندازی فرانت‌اند
REM ================================================================
:start_frontend
call :log_info "راه‌اندازی فرانت‌اند..."

REM تغییر به پوشه فرانت‌اند
cd /d "%PROJECT_ROOT%\frontend-javafx"

REM راه‌اندازی فرانت‌اند در پس‌زمینه
start "Food Ordering Frontend" cmd /c "echo 🚀 راه‌اندازی فرانت‌اند... && echo 📍 مسیر: %CD% && echo. && java %JAVA_OPTS_FRONTEND% -cp target/classes com.myapp.ui.MainApp"

REM انتظار برای راه‌اندازی فرانت‌اند
call :log_info "انتظار برای راه‌اندازی فرانت‌اند..."
timeout /t 5 /nobreak >nul

call :log_success "فرانت‌اند راه‌اندازی شد"

cd /d "%PROJECT_ROOT%"
goto :eof

REM ================================================================
REM تابع نمایش اطلاعات سیستم
REM ================================================================
:show_system_info
call :log_info "اطلاعات سیستم:"
echo ================================================================
echo 🖥️ سیستم عامل: %OS%
echo 💻 معماری: %PROCESSOR_ARCHITECTURE%
echo 🧠 پردازنده: %NUMBER_OF_PROCESSORS% هسته
echo 💾 حافظه کل: 
for /f "tokens=2" %%i in ('wmic computersystem get TotalPhysicalMemory /value ^| findstr "="') do set "TOTAL_MEMORY=%%i"
set /a "TOTAL_MEMORY_GB=%TOTAL_MEMORY:~0,-1%/1024/1024/1024"
echo %TOTAL_MEMORY_GB% GB
echo ================================================================
goto :eof

REM ================================================================
REM تابع نمایش خلاصه نهایی
REM ================================================================
:show_summary
call :log_info "خلاصه راه‌اندازی:"
echo ================================================================
echo 🚀 سیستم سفارش غذا با موفقیت راه‌اندازی شد
echo.
echo 📍 آدرس‌های دسترسی:
echo    🌐 بک‌اند: http://localhost:%BACKEND_PORT%
echo    🔍 سلامت: http://localhost:%BACKEND_PORT%/health
echo    📊 API: http://localhost:%BACKEND_PORT%/api
echo.
echo 📁 مسیرهای مهم:
echo    📂 پروژه: %PROJECT_ROOT%
echo    📋 گزارش‌ها: %LOG_DIR%
echo    🔧 تنظیمات: %PROJECT_ROOT%\backend\src\main\resources
echo.
echo 🛠️ دستورات مفید:
echo    📊 مشاهده فرآیندها: tasklist | findstr java
echo    🛑 توقف فرآیندها: taskkill /f /im java.exe
echo    📋 مشاهده پورت‌ها: netstat -an | findstr :%BACKEND_PORT%
echo ================================================================
goto :eof

REM ================================================================
REM تابع پاکسازی
REM ================================================================
:cleanup
call :log_info "پاکسازی فایل‌های موقت..."

REM حذف فایل‌های موقت
if exist "%PROJECT_ROOT%\temp" rmdir /s /q "%PROJECT_ROOT%\temp" 2>nul

REM حذف فایل‌های target (اختیاری)
set /p "CLEAN_TARGET=آیا می‌خواهید فایل‌های target پاک شوند؟ (y/n): "
if /i "!CLEAN_TARGET!"=="y" (
    if exist "%PROJECT_ROOT%\backend\target" rmdir /s /q "%PROJECT_ROOT%\backend\target" 2>nul
    if exist "%PROJECT_ROOT%\frontend-javafx\target" rmdir /s /q "%PROJECT_ROOT%\frontend-javafx\target" 2>nul
    call :log_success "فایل‌های target پاک شدند"
)

call :log_success "پاکسازی انجام شد"
goto :eof

REM ================================================================
REM تابع اصلی
REM ================================================================
:main
echo ================================================================
echo 🚀 راه‌اندازی سیستم سفارش غذا - ویندوز
echo 📅 تاریخ: %date% %time%
echo ================================================================

REM بررسی پیش‌نیازها
call :check_prerequisites
if %errorlevel% neq 0 goto :error

REM نمایش اطلاعات سیستم
call :show_system_info

REM ایجاد ساختار پوشه‌ها
call :create_directories
if %errorlevel% neq 0 goto :error

REM بررسی پورت‌ها
call :check_ports
if %errorlevel% neq 0 goto :error

REM ساخت پروژه
call :build_project
if %errorlevel% neq 0 goto :error

REM راه‌اندازی بک‌اند
call :start_backend
if %errorlevel% neq 0 goto :error

REM راه‌اندازی فرانت‌اند
call :start_frontend
if %errorlevel% neq 0 goto :error

REM نمایش خلاصه
call :show_summary

echo.
echo ✅ سیستم با موفقیت راه‌اندازی شد!
echo 💡 برای توقف سیستم، تمام پنجره‌های cmd را ببندید
echo.
goto :end

:error
echo.
echo ❌ راه‌اندازی سیستم با خطا مواجه شد
echo 💡 لطفاً خطاهای بالا را بررسی کنید
echo.
pause
exit /b 1

:end
echo.
echo 🎉 عملیات راه‌اندازی به پایان رسید
echo 📋 برای مشاهده گزارش‌ها به پوشه logs مراجعه کنید
echo.
pause 
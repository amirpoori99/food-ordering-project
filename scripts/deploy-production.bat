@echo off
REM ================================================================
REM اسکریپت استقرار تولید سیستم سفارش غذا - ویندوز
REM این اسکریپت سیستم را در محیط تولید ویندوز مستقر می‌کند
REM نویسنده: تیم توسعه
REM تاریخ آخرین ویرایش: تیر ۱۴۰۴
REM نسخه: ۲.۰ - سیستم استقرار پیشرفته ویندوز
REM ================================================================

REM تنظیمات اولیه
setlocal enabledelayedexpansion

REM ================================================================
REM متغیرهای پیکربندی سیستم
REM ================================================================
set "PROJECT_ROOT=%~dp0.."                    REM مسیر ریشه پروژه
set "DEPLOY_DIR=C:\food-ordering"             REM مسیر استقرار در سرور
set "BACKUP_DIR=C:\backups\food-ordering"     REM مسیر پشتیبان‌ها
set "LOG_FILE=C:\logs\food-ordering\deploy.log" REM فایل گزارش
set "SERVICE_NAME=FoodOrderingService"        REM نام سرویس ویندوز
set "JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC"  REM تنظیمات JVM
set "PORT=8080"                               REM پورت پیش‌فرض

REM ================================================================
REM تابع نمایش پیام‌های رنگی
REM ================================================================
:log_info
echo [INFO] %~1
echo [INFO] %~1 >> "%LOG_FILE%"
goto :eof

:log_success
echo [SUCCESS] %~1
echo [SUCCESS] %~1 >> "%LOG_FILE%"
goto :eof

:log_warning
echo [WARNING] %~1
echo [WARNING] %~1 >> "%LOG_FILE%"
goto :eof

:log_error
echo [ERROR] %~1
echo [ERROR] %~1 >> "%LOG_FILE%"
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
    exit /b 1
)

REM بررسی وجود Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    call :log_error "Maven یافت نشد. لطفاً آن را نصب کنید."
    exit /b 1
)

REM بررسی وجود sc (Service Control Manager)
sc query >nul 2>&1
if %errorlevel% neq 0 (
    call :log_error "Service Control Manager در دسترس نیست."
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
REM تابع ایجاد ساختار پوشه‌های استقرار
REM ================================================================
:create_deployment_structure
call :log_info "ایجاد ساختار پوشه‌های استقرار..."

REM ایجاد پوشه‌های اصلی
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"
if not exist "C:\logs\food-ordering" mkdir "C:\logs\food-ordering"

REM ایجاد زیرپوشه‌های مورد نیاز
if not exist "%DEPLOY_DIR%\bin" mkdir "%DEPLOY_DIR%\bin"        REM اسکریپت‌های اجرایی
if not exist "%DEPLOY_DIR%\config" mkdir "%DEPLOY_DIR%\config"  REM فایل‌های پیکربندی
if not exist "%DEPLOY_DIR%\logs" mkdir "%DEPLOY_DIR%\logs"      REM فایل‌های گزارش
if not exist "%DEPLOY_DIR%\data" mkdir "%DEPLOY_DIR%\data"      REM فایل‌های داده
if not exist "%DEPLOY_DIR%\temp" mkdir "%DEPLOY_DIR%\temp"      REM فایل‌های موقت

call :log_success "ساختار پوشه‌های استقرار ایجاد شد"
goto :eof

REM ================================================================
REM تابع پشتیبان‌گیری از نسخه قبلی
REM ================================================================
:backup_current_version
call :log_info "پشتیبان‌گیری از نسخه فعلی..."

if exist "%DEPLOY_DIR%" (
    set "BACKUP_NAME=backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
    set "BACKUP_NAME=!BACKUP_NAME: =0!"
    set "BACKUP_PATH=%BACKUP_DIR%\!BACKUP_NAME!"
    
    xcopy "%DEPLOY_DIR%" "!BACKUP_PATH!" /E /I /H /Y >nul
    call :log_success "پشتیبان‌گیری انجام شد: !BACKUP_PATH!"
    
    REM حذف پشتیبان‌های قدیمی (بیش از ۷ روز)
    forfiles /p "%BACKUP_DIR%" /s /m backup_* /d -7 /c "cmd /c if @isdir==TRUE rmdir /s /q @path" 2>nul
) else (
    call :log_warning "نسخه قبلی یافت نشد، پشتیبان‌گیری رد شد"
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
mvn clean package -DskipTests
if %errorlevel% equ 0 (
    call :log_success "ساخت بک‌اند با موفقیت انجام شد"
) else (
    call :log_error "ساخت بک‌اند ناموفق بود"
    exit /b 1
)

REM پاکسازی و ساخت فرانت‌اند
call :log_info "ساخت فرانت‌اند..."
cd ..\frontend-javafx
mvn clean package -DskipTests
if %errorlevel% equ 0 (
    call :log_success "ساخت فرانت‌اند با موفقیت انجام شد"
) else (
    call :log_error "ساخت فرانت‌اند ناموفق بود"
    exit /b 1
)

cd /d "%PROJECT_ROOT%"
goto :eof

REM ================================================================
REM تابع کپی فایل‌های پروژه
REM ================================================================
:copy_project_files
call :log_info "کپی فایل‌های پروژه..."

REM کپی JAR بک‌اند
copy "%PROJECT_ROOT%\backend\target\food-ordering-backend-1.0.0.jar" "%DEPLOY_DIR%\bin\food-ordering-backend.jar" >nul

REM کپی JAR فرانت‌اند
copy "%PROJECT_ROOT%\frontend-javafx\target\food-ordering-frontend-1.0.0.jar" "%DEPLOY_DIR%\bin\food-ordering-frontend.jar" >nul

REM کپی فایل‌های پیکربندی
copy "%PROJECT_ROOT%\backend\src\main\resources\application-production.properties" "%DEPLOY_DIR%\config\application.properties" >nul

REM کپی اسکریپت‌ها
xcopy "%PROJECT_ROOT%\scripts" "%DEPLOY_DIR%\scripts\" /E /I /H /Y >nul

call :log_success "فایل‌های پروژه کپی شدند"
goto :eof

REM ================================================================
REM تابع ایجاد فایل سرویس ویندوز
REM ================================================================
:create_windows_service
call :log_info "ایجاد فایل سرویس ویندوز..."

REM ایجاد فایل batch برای اجرای سرویس
set "SERVICE_BAT=%DEPLOY_DIR%\bin\start-service.bat"
(
echo @echo off
echo cd /d "%DEPLOY_DIR%"
echo java %JAVA_OPTS% -jar bin\food-ordering-backend.jar
) > "%SERVICE_BAT%"

REM ایجاد سرویس ویندوز با استفاده از sc
sc create "%SERVICE_NAME%" binPath= "%SERVICE_BAT%" start= auto DisplayName= "Food Ordering System Service" >nul 2>&1

if %errorlevel% equ 0 (
    call :log_success "سرویس ویندوز ایجاد شد"
) else (
    call :log_warning "سرویس ویندوز قبلاً وجود دارد یا ایجاد آن ناموفق بود"
)
goto :eof

REM ================================================================
REM تابع راه‌اندازی سرویس
REM ================================================================
:start_service
call :log_info "راه‌اندازی سرویس..."

REM شروع سرویس
sc start "%SERVICE_NAME%" >nul 2>&1

REM بررسی وضعیت سرویس
timeout /t 5 /nobreak >nul
sc query "%SERVICE_NAME%" | findstr "RUNNING" >nul
if %errorlevel% equ 0 (
    call :log_success "سرویس با موفقیت راه‌اندازی شد"
) else (
    call :log_error "راه‌اندازی سرویس ناموفق بود"
    sc query "%SERVICE_NAME%"
    exit /b 1
)
goto :eof

REM ================================================================
REM تابع بررسی سلامت سیستم
REM ================================================================
:health_check
call :log_info "بررسی سلامت سیستم..."

REM انتظار برای راه‌اندازی کامل
timeout /t 10 /nobreak >nul

REM بررسی وضعیت سرویس
sc query "%SERVICE_NAME%" | findstr "RUNNING" >nul
if %errorlevel% neq 0 (
    call :log_error "سرویس فعال نیست"
    goto :eof
)

REM بررسی دسترسی به API (با استفاده از PowerShell)
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:%PORT%/health' -UseBasicParsing; if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
if %errorlevel% equ 0 (
    call :log_success "بررسی سلامت API موفق بود"
) else (
    call :log_error "بررسی سلامت API ناموفق بود"
    goto :eof
)

REM بررسی استفاده از منابع (با استفاده از PowerShell)
for /f "tokens=5" %%i in ('powershell -Command "Get-Process java | Where-Object {$_.ProcessName -eq 'java'} | Select-Object -ExpandProperty WorkingSet | ForEach-Object {[math]::Round($_/1MB,2)}" 2^>nul') do set "MEMORY_USAGE=%%i"
for /f "tokens=5" %%i in ('powershell -Command "Get-Process java | Where-Object {$_.ProcessName -eq 'java'} | Select-Object -ExpandProperty CPU | ForEach-Object {[math]::Round($_,2)}" 2^>nul') do set "CPU_USAGE=%%i"

call :log_info "استفاده از حافظه: %MEMORY_USAGE% MB"
call :log_info "استفاده از CPU: %CPU_USAGE%%%"

call :log_success "بررسی سلامت سیستم موفق بود"
goto :eof

REM ================================================================
REM تابع تنظیم فایروال ویندوز
REM ================================================================
:configure_firewall
call :log_info "تنظیم فایروال ویندوز..."

REM باز کردن پورت در فایروال ویندوز
netsh advfirewall firewall add rule name="Food Ordering System" dir=in action=allow protocol=TCP localport=%PORT% >nul 2>&1

if %errorlevel% equ 0 (
    call :log_success "پورت %PORT% در فایروال ویندوز باز شد"
) else (
    call :log_warning "تنظیم فایروال ناموفق بود"
)
goto :eof

REM ================================================================
REM تابع نمایش خلاصه نهایی
REM ================================================================
:show_summary
call :log_info "خلاصه عملیات استقرار:"
echo ================================================================
echo مسیر استقرار: %DEPLOY_DIR%
echo نام سرویس: %SERVICE_NAME%
echo پورت: %PORT%
for /f "tokens=3" %%i in ('sc query "%SERVICE_NAME%" ^| findstr "STATE"') do echo وضعیت سرویس: %%i
echo ================================================================

echo.
echo دستورات مفید:
echo   وضعیت سرویس: sc query %SERVICE_NAME%
echo   راه‌اندازی مجدد: sc stop %SERVICE_NAME% ^&^& sc start %SERVICE_NAME%
echo   توقف سرویس: sc stop %SERVICE_NAME%
echo   حذف سرویس: sc delete %SERVICE_NAME%
goto :eof

REM ================================================================
REM تابع اصلی
REM ================================================================
:main
echo ================================================================
echo 🚀 شروع عملیات استقرار تولید سیستم سفارش غذا - ویندوز
echo 📅 تاریخ: %date% %time%
echo ================================================================

REM بررسی پیش‌نیازها
call :check_prerequisites
if %errorlevel% neq 0 goto :error

REM ایجاد ساختار پوشه‌ها
call :create_deployment_structure
if %errorlevel% neq 0 goto :error

REM پشتیبان‌گیری از نسخه قبلی
call :backup_current_version
if %errorlevel% neq 0 goto :error

REM ساخت پروژه
call :build_project
if %errorlevel% neq 0 goto :error

REM کپی فایل‌های پروژه
call :copy_project_files
if %errorlevel% neq 0 goto :error

REM ایجاد فایل سرویس
call :create_windows_service
if %errorlevel% neq 0 goto :error

REM راه‌اندازی سرویس
call :start_service
if %errorlevel% neq 0 goto :error

REM تنظیم فایروال
call :configure_firewall
if %errorlevel% neq 0 goto :error

REM بررسی سلامت
call :health_check
if %errorlevel% neq 0 goto :error

REM نمایش خلاصه
call :show_summary

echo ================================================================
echo ✅ عملیات استقرار با موفقیت به پایان رسید
echo 🌐 سیستم در آدرس http://localhost:%PORT% در دسترس است
echo 📋 گزارش کامل: %LOG_FILE%
echo ================================================================
goto :end

:error
echo.
echo ❌ عملیات استقرار با خطا مواجه شد
echo 💡 لطفاً خطاهای بالا را بررسی کنید
exit /b 1

:end
pause 
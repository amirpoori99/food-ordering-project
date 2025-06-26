@echo off
:: Food Ordering System - Production Deployment Script (Windows)
:: Version: 1.0

echo 🚀 Starting Food Ordering System Production Deployment...
echo.

:: ==================== ENVIRONMENT SETUP ====================
set APP_ENVIRONMENT=production
if "%SERVER_PORT%"=="" set SERVER_PORT=8081
if "%SERVER_HOST%"=="" set SERVER_HOST=0.0.0.0

:: Security Settings Check
if "%JWT_SECRET%"=="" (
    echo ❌ ERROR: JWT_SECRET must be set in production!
    echo    Example: set JWT_SECRET=your-256-bit-secret-key
    pause
    exit /b 1
)

:: Database Settings
if "%DATABASE_URL%"=="" set DATABASE_URL=jdbc:sqlite:food_ordering.db
if "%DATABASE_DRIVER%"=="" set DATABASE_DRIVER=org.sqlite.JDBC

:: SSL Settings
if "%SSL_ENABLED%"=="" set SSL_ENABLED=false
if "%SSL_ENABLED%"=="true" (
    if "%SSL_KEYSTORE_PATH%"=="" (
        echo ❌ ERROR: SSL enabled but SSL_KEYSTORE_PATH not set!
        pause
        exit /b 1
    )
    if "%SSL_KEYSTORE_PASSWORD%"=="" (
        echo ❌ ERROR: SSL enabled but SSL_KEYSTORE_PASSWORD not set!
        pause
        exit /b 1
    )
)

:: Logging Settings
if "%LOG_LEVEL_ROOT%"=="" set LOG_LEVEL_ROOT=INFO
if "%LOG_FILE_PATH%"=="" set LOG_FILE_PATH=./logs

:: ==================== PRE-DEPLOYMENT CHECKS ====================
echo 🔍 Running pre-deployment checks...

:: Check Java
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ ERROR: Java not found. Please install Java 17+
    pause
    exit /b 1
)

:: Check Maven
mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ ERROR: Maven not found. Please install Maven 3.6+
    pause
    exit /b 1
)

echo ✅ Pre-deployment checks passed

:: ==================== BACKUP EXISTING DATA ====================
if exist "food_ordering.db" (
    echo 💾 Creating database backup...
    for /f "tokens=1-3 delims=/ " %%a in ('date /t') do set today=%%c%%a%%b
    for /f "tokens=1-2 delims=: " %%a in ('time /t') do set now=%%a%%b
    set timestamp=%today%_%now%
    set timestamp=%timestamp: =0%
    copy "food_ordering.db" "food_ordering_backup_%timestamp%.db" >nul
    echo ✅ Database backed up
)

:: ==================== BUILD APPLICATION ====================
echo 🔨 Building backend application...
cd backend
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ ERROR: Backend build failed!
    pause
    exit /b 1
)
echo ✅ Backend build successful

echo 🔨 Building frontend application...
cd ..\frontend-javafx
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ ERROR: Frontend build failed!
    pause
    exit /b 1
)
echo ✅ Frontend build successful
cd ..

:: ==================== PREPARE DEPLOYMENT DIRECTORY ====================
set DEPLOY_DIR=deployment
echo 📁 Preparing deployment directory: %DEPLOY_DIR%

if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if not exist "%DEPLOY_DIR%\logs" mkdir "%DEPLOY_DIR%\logs"
if not exist "%DEPLOY_DIR%\config" mkdir "%DEPLOY_DIR%\config"
if not exist "%DEPLOY_DIR%\lib" mkdir "%DEPLOY_DIR%\lib"

:: Copy backend JAR and dependencies
copy "backend\target\food-ordering-backend-*.jar" "%DEPLOY_DIR%\" >nul 2>&1
if exist "backend\target\lib\*.jar" copy "backend\target\lib\*.jar" "%DEPLOY_DIR%\lib\" >nul 2>&1

:: Copy frontend JAR
copy "frontend-javafx\target\food-ordering-frontend-*.jar" "%DEPLOY_DIR%\" >nul 2>&1

:: Copy configuration files
copy "backend\src\main\resources\application.properties" "%DEPLOY_DIR%\config\" >nul 2>&1
copy "backend\src\main\resources\logback.xml" "%DEPLOY_DIR%\config\" >nul 2>&1

:: ==================== CREATE STARTUP SCRIPTS ====================
echo 📜 Creating startup scripts...

:: Backend startup script
echo @echo off > "%DEPLOY_DIR%\start-backend.bat"
echo :: Food Ordering Backend Startup Script >> "%DEPLOY_DIR%\start-backend.bat"
echo. >> "%DEPLOY_DIR%\start-backend.bat"
echo for %%%%f in (food-ordering-backend-*.jar) do set BACKEND_JAR=%%%%f >> "%DEPLOY_DIR%\start-backend.bat"
echo if "%%BACKEND_JAR%%"=="" ( >> "%DEPLOY_DIR%\start-backend.bat"
echo     echo ❌ Backend JAR not found! >> "%DEPLOY_DIR%\start-backend.bat"
echo     pause >> "%DEPLOY_DIR%\start-backend.bat"
echo     exit /b 1 >> "%DEPLOY_DIR%\start-backend.bat"
echo ^) >> "%DEPLOY_DIR%\start-backend.bat"
echo. >> "%DEPLOY_DIR%\start-backend.bat"
echo set CLASSPATH=%%BACKEND_JAR%%;lib\* >> "%DEPLOY_DIR%\start-backend.bat"
echo. >> "%DEPLOY_DIR%\start-backend.bat"
echo echo 🚀 Starting Food Ordering Backend... >> "%DEPLOY_DIR%\start-backend.bat"
echo echo 📍 JAR: %%BACKEND_JAR%% >> "%DEPLOY_DIR%\start-backend.bat"
echo echo 📍 Port: %SERVER_PORT% >> "%DEPLOY_DIR%\start-backend.bat"
echo echo 📍 Environment: %APP_ENVIRONMENT% >> "%DEPLOY_DIR%\start-backend.bat"
echo echo. >> "%DEPLOY_DIR%\start-backend.bat"
echo. >> "%DEPLOY_DIR%\start-backend.bat"
echo java -cp "%%CLASSPATH%%" ^^ >> "%DEPLOY_DIR%\start-backend.bat"
echo      -Dfile.encoding=UTF-8 ^^ >> "%DEPLOY_DIR%\start-backend.bat"
echo      -Djava.awt.headless=true ^^ >> "%DEPLOY_DIR%\start-backend.bat"
echo      -Xms512m -Xmx2g ^^ >> "%DEPLOY_DIR%\start-backend.bat"
echo      -XX:+UseG1GC ^^ >> "%DEPLOY_DIR%\start-backend.bat"
echo      -XX:MaxGCPauseMillis=200 ^^ >> "%DEPLOY_DIR%\start-backend.bat"
echo      -Dlogback.configurationFile=config/logback.xml ^^ >> "%DEPLOY_DIR%\start-backend.bat"
echo      com.myapp.ServerApp >> "%DEPLOY_DIR%\start-backend.bat"

:: Frontend startup script
echo @echo off > "%DEPLOY_DIR%\start-frontend.bat"
echo :: Food Ordering Frontend Startup Script >> "%DEPLOY_DIR%\start-frontend.bat"
echo. >> "%DEPLOY_DIR%\start-frontend.bat"
echo for %%%%f in (food-ordering-frontend-*.jar) do set FRONTEND_JAR=%%%%f >> "%DEPLOY_DIR%\start-frontend.bat"
echo if "%%FRONTEND_JAR%%"=="" ( >> "%DEPLOY_DIR%\start-frontend.bat"
echo     echo ❌ Frontend JAR not found! >> "%DEPLOY_DIR%\start-frontend.bat"
echo     pause >> "%DEPLOY_DIR%\start-frontend.bat"
echo     exit /b 1 >> "%DEPLOY_DIR%\start-frontend.bat"
echo ^) >> "%DEPLOY_DIR%\start-frontend.bat"
echo. >> "%DEPLOY_DIR%\start-frontend.bat"
echo echo 🚀 Starting Food Ordering Frontend... >> "%DEPLOY_DIR%\start-frontend.bat"
echo echo 📍 JAR: %%FRONTEND_JAR%% >> "%DEPLOY_DIR%\start-frontend.bat"
echo echo. >> "%DEPLOY_DIR%\start-frontend.bat"
echo. >> "%DEPLOY_DIR%\start-frontend.bat"
echo java -jar "%%FRONTEND_JAR%%" ^^ >> "%DEPLOY_DIR%\start-frontend.bat"
echo      -Dfile.encoding=UTF-8 ^^ >> "%DEPLOY_DIR%\start-frontend.bat"
echo      -Xms256m -Xmx1g >> "%DEPLOY_DIR%\start-frontend.bat"

:: ==================== DEPLOYMENT SUMMARY ====================
echo.
echo 🎉 Deployment completed successfully!
echo.
echo 📂 Deployment Directory: %DEPLOY_DIR%
echo 🔧 Configuration: %DEPLOY_DIR%\config\
echo 📋 Logs will be written to: %DEPLOY_DIR%\logs\
echo.
echo 🚀 To start the application:
echo    Backend:  cd %DEPLOY_DIR% ^&^& start-backend.bat
echo    Frontend: cd %DEPLOY_DIR% ^&^& start-frontend.bat
echo.
echo 🌐 Backend will be available at: http://%SERVER_HOST%:%SERVER_PORT%
echo 🔍 Health check: http://%SERVER_HOST%:%SERVER_PORT%/api/health
echo.

:: ==================== SECURITY REMINDERS ====================
echo 🔒 SECURITY REMINDERS FOR PRODUCTION:
echo    ✓ Set strong JWT_SECRET environment variable
echo    ✓ Enable SSL (set SSL_ENABLED=true)
echo    ✓ Use PostgreSQL for production database
echo    ✓ Configure Windows Firewall properly
echo    ✓ Set up monitoring and log rotation
echo    ✓ Regularly backup database
echo.
echo 📚 For detailed instructions, see DEPLOYMENT_GUIDE.md
echo.
echo ✅ Deployment script completed successfully!
echo.
pause 
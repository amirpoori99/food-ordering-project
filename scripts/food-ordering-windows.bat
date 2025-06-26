@echo off
REM ================================================================
REM FOOD ORDERING SYSTEM - WINDOWS SERVICE SCRIPT
REM ================================================================

setlocal enabledelayedexpansion

REM Set application directory
set APP_DIR=%~dp0
set JAR_FILE=%APP_DIR%food-ordering-backend.jar
set LOG_DIR=%APP_DIR%logs
set PID_FILE=%APP_DIR%food-ordering.pid

REM Create logs directory if it doesn't exist
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM Set Java options
set JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC -XX:+UseStringDeduplication
set JAVA_OPTS=%JAVA_OPTS% -Dlog.file.path=%LOG_DIR%
set JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=production

REM Set environment variables
set SERVER_HOST=0.0.0.0
set SERVER_PORT=8081

REM Check command line argument
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
goto usage

:start
echo Starting Food Ordering System...
if exist "%PID_FILE%" (
    echo Service is already running. PID file exists: %PID_FILE%
    goto end
)

REM Start the application
start /B java %JAVA_OPTS% -jar "%JAR_FILE%" > "%LOG_DIR%\food-ordering.log" 2>&1
if !errorlevel! equ 0 (
    echo Food Ordering System started successfully
    REM Save process ID (simplified approach for Windows)
    echo %date% %time% > "%PID_FILE%"
) else (
    echo Failed to start Food Ordering System
    exit /b 1
)
goto end

:stop
echo Stopping Food Ordering System...
if not exist "%PID_FILE%" (
    echo Service is not running. PID file not found.
    goto end
)

REM Kill Java processes running the food ordering system
for /f "tokens=2" %%i in ('tasklist /FI "IMAGENAME eq java.exe" /FO CSV ^| findstr "food-ordering"') do (
    taskkill /PID %%i /F >nul 2>&1
)

REM Remove PID file
del "%PID_FILE%" >nul 2>&1
echo Food Ordering System stopped
goto end

:restart
echo Restarting Food Ordering System...
call :stop
timeout /t 5 /nobreak >nul
call :start
goto end

:status
if exist "%PID_FILE%" (
    echo Food Ordering System is running
    echo PID file: %PID_FILE%
    echo Created: 
    type "%PID_FILE%"
) else (
    echo Food Ordering System is not running
)
goto end

:usage
echo Usage: %0 {start^|stop^|restart^|status}
echo.
echo Commands:
echo   start   - Start the Food Ordering System
echo   stop    - Stop the Food Ordering System  
echo   restart - Restart the Food Ordering System
echo   status  - Check if the system is running
echo.
exit /b 1

:end
endlocal 
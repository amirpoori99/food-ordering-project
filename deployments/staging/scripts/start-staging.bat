@echo off
REM Food Ordering System - Start Script for staging
echo Starting Food Ordering System (staging environment)...

set JAVA_OPTS=-Xms1024m -Xmx2048m -Dspring.profiles.active=staging
set SERVER_PORT=8082
set SPRING_PROFILES_ACTIVE=staging

cd /d "%~dp0.."
java %JAVA_OPTS% -jar backend\target\food-ordering-backend.jar

pause

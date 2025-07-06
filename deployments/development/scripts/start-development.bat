@echo off
REM Food Ordering System - Start Script for development
echo Starting Food Ordering System (development environment)...

set JAVA_OPTS=-Xms512m -Xmx1024m -Dspring.profiles.active=dev
set SERVER_PORT=8080
set SPRING_PROFILES_ACTIVE=development

cd /d "%~dp0.."
java %JAVA_OPTS% -jar backend\target\food-ordering-backend.jar

pause

@echo off
REM Food Ordering System - Health Check Script for development
echo Checking health of Food Ordering System (development environment)...

curl -f http://localhost:8080/actuator/health || echo Health check failed

pause

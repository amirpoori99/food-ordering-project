@echo off
REM Food Ordering System - Health Check Script for staging
echo Checking health of Food Ordering System (staging environment)...

curl -f http://localhost:8082/actuator/health || echo Health check failed

pause

@echo off
REM Food Ordering System - Stop Script for staging
echo Stopping Food Ordering System (staging environment)...

for /f "tokens=5" %%a in ('netstat -aon ^| find ":8082" ^| find "LISTENING"') do taskkill /f /pid %%a

echo Service stopped.
pause

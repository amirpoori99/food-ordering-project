@echo off
REM Food Ordering System - Stop Script for development
echo Stopping Food Ordering System (development environment)...

for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do taskkill /f /pid %%a

echo Service stopped.
pause

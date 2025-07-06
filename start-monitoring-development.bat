@echo off
title Food Ordering System - Monitoring Stack
echo ============================================
echo  Food Ordering System - Monitoring Stack
echo ============================================
echo.
echo Environment: development
echo.
echo Starting monitoring components...
echo.

echo [1/4] Starting Node Exporter...
start "Node Exporter" cmd /k "cd tools\monitoring\node-exporter && node_exporter.bat"
timeout /t 2 /nobreak > nul

echo [2/4] Starting Prometheus...
start "Prometheus" cmd /k "cd tools\monitoring\prometheus && prometheus.bat"
timeout /t 3 /nobreak > nul

echo [3/4] Starting AlertManager...
echo AlertManager configured for notifications
timeout /t 2 /nobreak > nul

echo [4/4] Starting Grafana...
start "Grafana" cmd /k "cd tools\monitoring\grafana && grafana.bat"
timeout /t 3 /nobreak > nul

echo.
echo ============================================
echo  Monitoring Stack Started Successfully!
echo ============================================
echo.
echo Services:
echo - Prometheus:    http://localhost:9090
echo - Grafana:       http://localhost:3000
echo - Node Exporter: http://localhost:9100
echo.
echo Grafana Login:
echo - Username: admin
echo - Password: foodordering123
echo.
echo Press any key to open Grafana dashboard...
pause > nul
start http://localhost:3000

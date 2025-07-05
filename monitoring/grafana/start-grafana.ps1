# Grafana Service Script
# Port: 3000
# Default login: admin/admin

$GRAFANA_DIR = "$PSScriptRoot"
$GRAFANA_EXE = "$GRAFANA_DIR\bin\grafana-server.exe"

if (-not (Test-Path $GRAFANA_EXE)) {
    Write-Error "Grafana executable not found at: $GRAFANA_EXE"
    Write-Host "Please download Grafana from: https://grafana.com/grafana/download"
    exit 1
}

Write-Host "Starting Grafana..." -ForegroundColor Green
Write-Host "Web UI: http://localhost:3000" -ForegroundColor Cyan
Write-Host "Default login: admin/admin" -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow

Set-Location $GRAFANA_DIR
& $GRAFANA_EXE

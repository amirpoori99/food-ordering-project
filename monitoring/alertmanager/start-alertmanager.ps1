# Alertmanager Service Script
# Port: 9093

$ALERTMANAGER_DIR = "$PSScriptRoot"
$ALERTMANAGER_EXE = "$ALERTMANAGER_DIR\alertmanager.exe"
$CONFIG_FILE = "$ALERTMANAGER_DIR\alertmanager.yml"

if (-not (Test-Path $ALERTMANAGER_EXE)) {
    Write-Error "Alertmanager executable not found at: $ALERTMANAGER_EXE"
    Write-Host "Please download Alertmanager from: https://prometheus.io/download/"
    exit 1
}

Write-Host "Starting Alertmanager..." -ForegroundColor Green
Write-Host "Web UI: http://localhost:9093" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow

& $ALERTMANAGER_EXE --config.file=$CONFIG_FILE --storage.path="$ALERTMANAGER_DIR\data"

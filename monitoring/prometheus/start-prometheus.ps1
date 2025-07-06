# Prometheus Service Script
# Port: 9090

$PROMETHEUS_DIR = "$PSScriptRoot"
$PROMETHEUS_EXE = "$PROMETHEUS_DIR\prometheus.exe"
$CONFIG_FILE = "$PROMETHEUS_DIR\prometheus.yml"

if (-not (Test-Path $PROMETHEUS_EXE)) {
    Write-Error "Prometheus executable not found at: $PROMETHEUS_EXE"
    Write-Host "Please download Prometheus from: https://prometheus.io/download/"
    exit 1
}

Write-Host "Starting Prometheus..." -ForegroundColor Green
Write-Host "Web UI: http://localhost:9090" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow

& $PROMETHEUS_EXE --config.file=$CONFIG_FILE --storage.tsdb.path="$PROMETHEUS_DIR\data" --web.console.libraries="$PROMETHEUS_DIR\console_libraries" --web.console.templates="$PROMETHEUS_DIR\consoles" --web.enable-lifecycle --storage.tsdb.retention.time=15d

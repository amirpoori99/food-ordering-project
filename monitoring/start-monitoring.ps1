# Food Ordering System - Master Monitoring Script
# Starts all monitoring components

param(
    [string]$Action = "start",
    [switch]$Background = $false
)

$PROJECT_ROOT = Split-Path -Parent $PSScriptRoot
$MONITORING_DIR = "$PROJECT_ROOT\monitoring"

function Write-Info { param([string]$Text) Write-Host "â„¹ $Text" -ForegroundColor Cyan }
function Write-Success { param([string]$Text) Write-Host "âœ“ $Text" -ForegroundColor Green }
function Write-Warning { param([string]$Text) Write-Host "âš  $Text" -ForegroundColor Yellow }

function Start-MonitoringServices {
    Write-Info "Starting Food Ordering System Monitoring..."
    
    # Check if services are already running
    $prometheusRunning = Get-Process -Name "prometheus" -ErrorAction SilentlyContinue
    $grafanaRunning = Get-Process -Name "grafana-server" -ErrorAction SilentlyContinue
    $alertmanagerRunning = Get-Process -Name "alertmanager" -ErrorAction SilentlyContinue
    
    if ($prometheusRunning) {
        Write-Warning "Prometheus is already running (PID: $($prometheusRunning.Id))"
    } else {
        Write-Info "Starting Prometheus..."
        if ($Background) {
            Start-Process -FilePath "powershell.exe" -ArgumentList "-File `"$MONITORING_DIR\prometheus\start-prometheus.ps1`"" -WindowStyle Minimized
        } else {
            Start-Process -FilePath "powershell.exe" -ArgumentList "-File `"$MONITORING_DIR\prometheus\start-prometheus.ps1`""
        }
        Write-Success "Prometheus started"
    }
    
    if ($grafanaRunning) {
        Write-Warning "Grafana is already running (PID: $($grafanaRunning.Id))"
    } else {
        Write-Info "Starting Grafana..."
        if ($Background) {
            Start-Process -FilePath "powershell.exe" -ArgumentList "-File `"$MONITORING_DIR\grafana\start-grafana.ps1`"" -WindowStyle Minimized
        } else {
            Start-Process -FilePath "powershell.exe" -ArgumentList "-File `"$MONITORING_DIR\grafana\start-grafana.ps1`""
        }
        Write-Success "Grafana started"
    }
    
    if ($alertmanagerRunning) {
        Write-Warning "Alertmanager is already running (PID: $($alertmanagerRunning.Id))"
    } else {
        Write-Info "Starting Alertmanager..."
        if ($Background) {
            Start-Process -FilePath "powershell.exe" -ArgumentList "-File `"$MONITORING_DIR\alertmanager\start-alertmanager.ps1`"" -WindowStyle Minimized
        } else {
            Start-Process -FilePath "powershell.exe" -ArgumentList "-File `"$MONITORING_DIR\alertmanager\start-alertmanager.ps1`""
        }
        Write-Success "Alertmanager started"
    }
    
    Write-Success "Monitoring services started successfully!"
    Write-Info "Access URLs:"
    Write-Host "  â€¢ Prometheus: http://localhost:9090" -ForegroundColor Yellow
    Write-Host "  â€¢ Grafana: http://localhost:3000 (admin/admin)" -ForegroundColor Yellow
    Write-Host "  â€¢ Alertmanager: http://localhost:9093" -ForegroundColor Yellow
}

function Stop-MonitoringServices {
    Write-Info "Stopping Food Ordering System Monitoring..."
    
    # Stop Prometheus
    $prometheusProcess = Get-Process -Name "prometheus" -ErrorAction SilentlyContinue
    if ($prometheusProcess) {
        Stop-Process -Name "prometheus" -Force
        Write-Success "Prometheus stopped"
    }
    
    # Stop Grafana
    $grafanaProcess = Get-Process -Name "grafana-server" -ErrorAction SilentlyContinue
    if ($grafanaProcess) {
        Stop-Process -Name "grafana-server" -Force
        Write-Success "Grafana stopped"
    }
    
    # Stop Alertmanager
    $alertmanagerProcess = Get-Process -Name "alertmanager" -ErrorAction SilentlyContinue
    if ($alertmanagerProcess) {
        Stop-Process -Name "alertmanager" -Force
        Write-Success "Alertmanager stopped"
    }
    
    Write-Success "All monitoring services stopped"
}

function Show-MonitoringStatus {
    Write-Info "Food Ordering System Monitoring Status:"
    
    $prometheusRunning = Get-Process -Name "prometheus" -ErrorAction SilentlyContinue
    $grafanaRunning = Get-Process -Name "grafana-server" -ErrorAction SilentlyContinue
    $alertmanagerRunning = Get-Process -Name "alertmanager" -ErrorAction SilentlyContinue
    
    if ($prometheusRunning) {
        Write-Host "  â€¢ Prometheus: " -NoNewline
        Write-Host "RUNNING" -ForegroundColor Green -NoNewline
        Write-Host " (PID: $($prometheusRunning.Id), Port: 9090)"
    } else {
        Write-Host "  â€¢ Prometheus: " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }
    
    if ($grafanaRunning) {
        Write-Host "  â€¢ Grafana: " -NoNewline
        Write-Host "RUNNING" -ForegroundColor Green -NoNewline
        Write-Host " (PID: $($grafanaRunning.Id), Port: 3000)"
    } else {
        Write-Host "  â€¢ Grafana: " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }
    
    if ($alertmanagerRunning) {
        Write-Host "  â€¢ Alertmanager: " -NoNewline
        Write-Host "RUNNING" -ForegroundColor Green -NoNewline
        Write-Host " (PID: $($alertmanagerRunning.Id), Port: 9093)"
    } else {
        Write-Host "  â€¢ Alertmanager: " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }
}

# Main execution
switch ($Action.ToLower()) {
    "start" { Start-MonitoringServices }
    "stop" { Stop-MonitoringServices }
    "restart" { 
        Stop-MonitoringServices
        Start-Sleep -Seconds 3
        Start-MonitoringServices
    }
    "status" { Show-MonitoringStatus }
    default {
        Write-Host "Usage: .\start-monitoring.ps1 [-Action start|stop|restart|status] [-Background]"
        Write-Host "Examples:"
        Write-Host "  .\start-monitoring.ps1 -Action start"
        Write-Host "  .\start-monitoring.ps1 -Action stop"
        Write-Host "  .\start-monitoring.ps1 -Action status"
        Write-Host "  .\start-monitoring.ps1 -Action start -Background"
    }
}

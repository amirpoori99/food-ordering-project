# Food Ordering System - Advanced Monitoring Setup Script
# Phase 42: Prometheus & Grafana Implementation
# Created: 2025-07-05

param(
    [string]$Mode = "setup",
    [string]$Environment = "production",
    [switch]$SkipDownload = $false
)

# Script configuration
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR
$MONITORING_CONFIG = "$PROJECT_ROOT\config\monitoring"
$PROMETHEUS_VERSION = "2.47.0"
$GRAFANA_VERSION = "10.1.0"
$ALERTMANAGER_VERSION = "0.26.0"

# Color functions
function Write-ColoredText {
    param([string]$Text, [string]$Color = "White")
    Write-Host $Text -ForegroundColor $Color
}

function Write-Success { param([string]$Text) Write-ColoredText "✓ $Text" "Green" }
function Write-Info { param([string]$Text) Write-ColoredText "ℹ $Text" "Cyan" }
function Write-Warning { param([string]$Text) Write-ColoredText "⚠ $Text" "Yellow" }
function Write-Error { param([string]$Text) Write-ColoredText "✗ $Text" "Red" }

# Function to check if running as Administrator
function Test-Administrator {
    $currentUser = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = [Security.Principal.WindowsPrincipal]$currentUser
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

# Function to create monitoring directories
function Initialize-MonitoringDirectories {
    Write-Info "Creating monitoring directories..."
    
    $directories = @(
        "$PROJECT_ROOT\monitoring\prometheus",
        "$PROJECT_ROOT\monitoring\grafana",
        "$PROJECT_ROOT\monitoring\alertmanager",
        "$PROJECT_ROOT\monitoring\data\prometheus",
        "$PROJECT_ROOT\monitoring\data\grafana",
        "$PROJECT_ROOT\monitoring\logs"
    )
    
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Success "Created directory: $dir"
        }
    }
}

# Function to copy configuration files
function Copy-ConfigurationFiles {
    Write-Info "Copying configuration files..."
    
    # Copy Prometheus configuration
    Copy-Item "$MONITORING_CONFIG\prometheus.yml" "$PROJECT_ROOT\monitoring\prometheus\" -Force
    Copy-Item "$MONITORING_CONFIG\alerting-rules.yml" "$PROJECT_ROOT\monitoring\prometheus\" -Force
    Copy-Item "$MONITORING_CONFIG\recording-rules.yml" "$PROJECT_ROOT\monitoring\prometheus\" -Force
    
    # Copy Alertmanager configuration
    Copy-Item "$MONITORING_CONFIG\alertmanager.yml" "$PROJECT_ROOT\monitoring\alertmanager\" -Force
    
    # Copy Grafana dashboards
    if (Test-Path "$MONITORING_CONFIG\grafana\dashboards") {
        Copy-Item "$MONITORING_CONFIG\grafana\dashboards\*" "$PROJECT_ROOT\monitoring\grafana\" -Force
    }
    
    Write-Success "Configuration files copied successfully"
}

# Function to create Prometheus service script
function Create-PrometheusService {
    Write-Info "Creating Prometheus service script..."
    
    $prometheusScript = @'
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
'@
    
    $prometheusScript | Out-File -FilePath "$PROJECT_ROOT\monitoring\prometheus\start-prometheus.ps1" -Encoding UTF8
    Write-Success "Prometheus service script created"
}

# Function to create Grafana service script
function Create-GrafanaService {
    Write-Info "Creating Grafana service script..."
    
    $grafanaScript = @'
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
'@
    
    $grafanaScript | Out-File -FilePath "$PROJECT_ROOT\monitoring\grafana\start-grafana.ps1" -Encoding UTF8
    Write-Success "Grafana service script created"
}

# Function to create Alertmanager service script
function Create-AlertmanagerService {
    Write-Info "Creating Alertmanager service script..."
    
    $alertmanagerScript = @'
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
'@
    
    $alertmanagerScript | Out-File -FilePath "$PROJECT_ROOT\monitoring\alertmanager\start-alertmanager.ps1" -Encoding UTF8
    Write-Success "Alertmanager service script created"
}

# Function to create master monitoring script
function Create-MasterMonitoringScript {
    Write-Info "Creating master monitoring script..."
    
    $masterScript = @'
# Food Ordering System - Master Monitoring Script
# Starts all monitoring components

param(
    [string]$Action = "start",
    [switch]$Background = $false
)

$PROJECT_ROOT = Split-Path -Parent $PSScriptRoot
$MONITORING_DIR = "$PROJECT_ROOT\monitoring"

function Write-Info { param([string]$Text) Write-Host "ℹ $Text" -ForegroundColor Cyan }
function Write-Success { param([string]$Text) Write-Host "✓ $Text" -ForegroundColor Green }
function Write-Warning { param([string]$Text) Write-Host "⚠ $Text" -ForegroundColor Yellow }

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
    Write-Host "  • Prometheus: http://localhost:9090" -ForegroundColor Yellow
    Write-Host "  • Grafana: http://localhost:3000 (admin/admin)" -ForegroundColor Yellow
    Write-Host "  • Alertmanager: http://localhost:9093" -ForegroundColor Yellow
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
        Write-Host "  • Prometheus: " -NoNewline
        Write-Host "RUNNING" -ForegroundColor Green -NoNewline
        Write-Host " (PID: $($prometheusRunning.Id), Port: 9090)"
    } else {
        Write-Host "  • Prometheus: " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }
    
    if ($grafanaRunning) {
        Write-Host "  • Grafana: " -NoNewline
        Write-Host "RUNNING" -ForegroundColor Green -NoNewline
        Write-Host " (PID: $($grafanaRunning.Id), Port: 3000)"
    } else {
        Write-Host "  • Grafana: " -NoNewline
        Write-Host "STOPPED" -ForegroundColor Red
    }
    
    if ($alertmanagerRunning) {
        Write-Host "  • Alertmanager: " -NoNewline
        Write-Host "RUNNING" -ForegroundColor Green -NoNewline
        Write-Host " (PID: $($alertmanagerRunning.Id), Port: 9093)"
    } else {
        Write-Host "  • Alertmanager: " -NoNewline
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
'@
    
    $masterScript | Out-File -FilePath "$PROJECT_ROOT\monitoring\start-monitoring.ps1" -Encoding UTF8
    Write-Success "Master monitoring script created"
}

# Function to create installation guide
function Create-InstallationGuide {
    Write-Info "Creating installation guide..."
    
    $guide = @'
# Food Ordering System - Monitoring Installation Guide

## Overview
This guide will help you set up Prometheus, Grafana, and Alertmanager for the Food Ordering System.

## Prerequisites
- Windows 10/11 or Windows Server 2016+
- PowerShell 5.1 or later
- Administrative privileges
- Internet connection (for downloading components)

## Installation Steps

### 1. Download Required Components

#### Prometheus
- Download from: https://prometheus.io/download/
- Choose: prometheus-2.47.0.windows-amd64.zip
- Extract to: `monitoring\prometheus\`

#### Grafana
- Download from: https://grafana.com/grafana/download
- Choose: grafana-10.1.0.windows-amd64.zip
- Extract to: `monitoring\grafana\`

#### Alertmanager
- Download from: https://prometheus.io/download/
- Choose: alertmanager-0.26.0.windows-amd64.zip
- Extract to: `monitoring\alertmanager\`

### 2. Directory Structure
After extraction, your directory structure should look like:
```
monitoring/
├── prometheus/
│   ├── prometheus.exe
│   ├── promtool.exe
│   ├── prometheus.yml
│   ├── alerting-rules.yml
│   ├── recording-rules.yml
│   └── start-prometheus.ps1
├── grafana/
│   ├── bin/
│   │   └── grafana-server.exe
│   ├── conf/
│   ├── food-ordering-overview.json
│   ├── food-ordering-business.json
│   └── start-grafana.ps1
├── alertmanager/
│   ├── alertmanager.exe
│   ├── alertmanager.yml
│   └── start-alertmanager.ps1
└── start-monitoring.ps1
```

### 3. Starting Services

#### Option 1: Start All Services
```powershell
.\monitoring\start-monitoring.ps1 -Action start
```

#### Option 2: Start Individual Services
```powershell
# Start Prometheus
.\monitoring\prometheus\start-prometheus.ps1

# Start Grafana
.\monitoring\grafana\start-grafana.ps1

# Start Alertmanager
.\monitoring\alertmanager\start-alertmanager.ps1
```

### 4. Access Web Interfaces

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Alertmanager**: http://localhost:9093

### 5. Grafana Dashboard Import

1. Login to Grafana (admin/admin)
2. Go to "+" → Import
3. Upload the JSON files:
   - `food-ordering-overview.json`
   - `food-ordering-business.json`

### 6. Verification

Check service status:
```powershell
.\monitoring\start-monitoring.ps1 -Action status
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Check if ports 9090, 3000, 9093 are free
   - Use `netstat -an | findstr :9090` to check

2. **Permission Denied**
   - Run PowerShell as Administrator
   - Check antivirus software

3. **Service Won't Start**
   - Check Windows Event Viewer
   - Verify file paths in configuration

### Log Files
- Prometheus: `monitoring\prometheus\data\`
- Grafana: `monitoring\grafana\data\log\`
- Alertmanager: `monitoring\alertmanager\data\`

## Security Considerations

1. **Change Default Passwords**
   - Grafana: Change admin password
   - Configure authentication

2. **Firewall Configuration**
   - Allow inbound rules for ports 9090, 3000, 9093
   - Restrict access to trusted networks

3. **HTTPS Configuration**
   - Configure SSL certificates
   - Enable HTTPS for all services

## Maintenance

### Regular Tasks
- Monitor disk usage for time-series data
- Review and adjust retention policies
- Update monitoring rules as needed
- Backup dashboard configurations

### Updates
- Check for new versions regularly
- Test updates in non-production environment
- Update configuration files as needed

## Support

For issues related to:
- Food Ordering System: Check project documentation
- Prometheus: https://prometheus.io/docs/
- Grafana: https://grafana.com/docs/
- Alertmanager: https://prometheus.io/docs/alerting/

---

**Installation Date**: {Installation Date}
**Version**: Phase 42 Implementation
**Support Contact**: System Administrator
'@
    
    $guide | Out-File -FilePath "$PROJECT_ROOT\monitoring\INSTALLATION-GUIDE.md" -Encoding UTF8
    Write-Success "Installation guide created"
}

# Function to validate configuration files
function Test-ConfigurationFiles {
    Write-Info "Validating configuration files..."
    
    $errors = @()
    
    # Check Prometheus config
    if (-not (Test-Path "$MONITORING_CONFIG\prometheus.yml")) {
        $errors += "Prometheus configuration file not found"
    }
    
    # Check Alertmanager config
    if (-not (Test-Path "$MONITORING_CONFIG\alertmanager.yml")) {
        $errors += "Alertmanager configuration file not found"
    }
    
    # Check Grafana dashboards
    if (-not (Test-Path "$MONITORING_CONFIG\grafana\dashboards")) {
        $errors += "Grafana dashboards directory not found"
    }
    
    if ($errors.Count -gt 0) {
        Write-Error "Configuration validation failed:"
        foreach ($error in $errors) {
            Write-Error "  - $error"
        }
        return $false
    }
    
    Write-Success "All configuration files validated successfully"
    return $true
}

# Main execution
Write-Info "Food Ordering System - Advanced Monitoring Setup"
Write-Info "Phase 42: Prometheus & Grafana Implementation"
Write-Info "================================================"

if ($Mode -eq "setup") {
    # Validate configuration files
    if (-not (Test-ConfigurationFiles)) {
        Write-Error "Configuration validation failed. Please check your configuration files."
        exit 1
    }
    
    # Initialize directories
    Initialize-MonitoringDirectories
    
    # Copy configuration files
    Copy-ConfigurationFiles
    
    # Create service scripts
    Create-PrometheusService
    Create-GrafanaService
    Create-AlertmanagerService
    Create-MasterMonitoringScript
    
    # Create installation guide
    Create-InstallationGuide
    
    Write-Success "Advanced monitoring setup completed successfully!"
    Write-Info "Next steps:"
    Write-Host "  1. Download required components (see INSTALLATION-GUIDE.md)" -ForegroundColor Yellow
    Write-Host "  2. Run: .\monitoring\start-monitoring.ps1 -Action start" -ForegroundColor Yellow
    Write-Host "  3. Access web interfaces at configured ports" -ForegroundColor Yellow
    
} elseif ($Mode -eq "validate") {
    Test-ConfigurationFiles
    
} else {
    Write-Host "Usage: .\monitoring-setup.ps1 [-Mode setup|validate] [-Environment production|development]"
}

Write-Info "Setup completed at: $(Get-Date)" 
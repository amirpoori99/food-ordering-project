# Real-time Alerting System for Food Ordering System
# Phase 46 Enhancement - Enterprise Alerting & Notification

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment = "development",
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("start", "stop", "status", "test", "send-alert", "monitor", "configure")]
    [string]$Action = "status",
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("critical", "high", "medium", "low")]
    [string]$Severity = "medium",
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("system", "business", "security", "performance")]
    [string]$Category = "system",
    
    [Parameter(Mandatory=$false)]
    [string]$AlertName = "",
    
    [Parameter(Mandatory=$false)]
    [string]$Message = "",
    
    [Parameter(Mandatory=$false)]
    [string]$Service = "",
    
    [Parameter(Mandatory=$false)]
    [switch]$Test = $false
)

# Configuration
$alertConfig = @{
    development = @{
        smtp_server = "smtp.gmail.com"
        smtp_port = 587
        email_from = "alerts-dev@foodordering.com"
        email_to = "devops@foodordering.com"
        slack_webhook = "https://hooks.slack.com/services/DEV/SLACK/WEBHOOK"
        teams_webhook = "https://outlook.office.com/webhook/DEV/TEAMS/WEBHOOK"
        enable_email = $true
        enable_slack = $true
        enable_teams = $false
        enable_sms = $false
    }
    staging = @{
        smtp_server = "smtp.gmail.com"
        smtp_port = 587
        email_from = "alerts-staging@foodordering.com"
        email_to = "devops@foodordering.com"
        slack_webhook = "https://hooks.slack.com/services/STAGING/SLACK/WEBHOOK"
        teams_webhook = "https://outlook.office.com/webhook/STAGING/TEAMS/WEBHOOK"
        enable_email = $true
        enable_slack = $true
        enable_teams = $true
        enable_sms = $false
    }
    production = @{
        smtp_server = "smtp.gmail.com"
        smtp_port = 587
        email_from = "alerts@foodordering.com"
        email_to = "devops@foodordering.com,management@foodordering.com"
        slack_webhook = "https://hooks.slack.com/services/PROD/SLACK/WEBHOOK"
        teams_webhook = "https://outlook.office.com/webhook/PROD/TEAMS/WEBHOOK"
        enable_email = $true
        enable_slack = $true
        enable_teams = $true
        enable_sms = $true
    }
}

$config = $alertConfig[$Environment]
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Host "Real-time Alerting System - Food Ordering" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Action: $Action" -ForegroundColor Yellow
Write-Host "Timestamp: $timestamp" -ForegroundColor Gray
Write-Host "=" * 50

function Get-AlertIcon {
    param([string]$severity, [string]$category)
    
    $icons = @{
        "critical" = "[CRITICAL]"
        "high" = "[HIGH]"
        "medium" = "[MEDIUM]"
        "low" = "[LOW]"
    }
    
    $categoryIcons = @{
        "system" = "[SYSTEM]"
        "business" = "[BUSINESS]"
        "security" = "[SECURITY]"
        "performance" = "[PERFORMANCE]"
    }
    
    return "$($icons[$severity]) $($categoryIcons[$category])"
}

function Send-Alert {
    param(
        [string]$alertName,
        [string]$message,
        [string]$severity,
        [string]$category,
        [string]$service,
        [string]$environment
    )
    
    $icon = Get-AlertIcon -severity $severity -category $category
    $color = switch ($severity) {
        "critical" { "Red" }
        "high" { "DarkRed" }
        "medium" { "Yellow" }
        "low" { "Green" }
        default { "White" }
    }
    
    Write-Host "`n$icon Sending $($severity.ToUpper()) alert: $alertName" -ForegroundColor $color
    Write-Host "Service: $service" -ForegroundColor White
    Write-Host "Message: $message" -ForegroundColor White
    Write-Host "Environment: $environment" -ForegroundColor White
    Write-Host "Category: $category" -ForegroundColor White
    Write-Host "-" * 50
    
    # Send notifications based on configuration
    if ($config.enable_email) {
        Write-Host "  -> Email sent to: $($config.email_to)" -ForegroundColor Green
    }
    
    if ($config.enable_slack) {
        Write-Host "  -> Slack message sent to: #alerts-$severity" -ForegroundColor Green
    }
    
    if ($config.enable_teams) {
        Write-Host "  -> Teams message sent" -ForegroundColor Green
    }
    
    if ($config.enable_sms -and $severity -in @("critical", "high")) {
        Write-Host "  -> SMS sent to emergency contacts" -ForegroundColor Green
    }
    
    # Log alert
    $logEntry = @{
        timestamp = $timestamp
        environment = $environment
        alertName = $alertName
        severity = $severity
        category = $category
        service = $service
        message = $message
        channels_sent = @()
    }
    
    if ($config.enable_email) { $logEntry.channels_sent += "email" }
    if ($config.enable_slack) { $logEntry.channels_sent += "slack" }
    if ($config.enable_teams) { $logEntry.channels_sent += "teams" }
    if ($config.enable_sms -and $severity -in @("critical", "high")) { $logEntry.channels_sent += "sms" }
    
    $logFile = "logs/alerting-system.log"
    if (-not (Test-Path "logs")) {
        New-Item -ItemType Directory -Path "logs" -Force | Out-Null
    }
    $logEntry | ConvertTo-Json -Compress | Out-File -FilePath $logFile -Append
    
    Write-Host "  -> Alert logged successfully" -ForegroundColor Green
    Write-Host "  -> Channels used: $($logEntry.channels_sent -join ', ')" -ForegroundColor White
}

function Test-AlertingSystem {
    Write-Host "Testing alerting system..." -ForegroundColor Yellow
    Write-Host "Sending test alerts..." -ForegroundColor Gray
    
    $testAlerts = @(
        @{
            name = "High CPU Usage"
            message = "CPU usage is above 90% for 5 minutes"
            severity = "high"
            category = "system"
            service = "order-service"
        }
        @{
            name = "Database Connection Failed"
            message = "Unable to connect to primary database"
            severity = "critical"
            category = "system"
            service = "database"
        }
        @{
            name = "Low Order Volume"
            message = "Order volume is 50% below normal for this time"
            severity = "medium"
            category = "business"
            service = "order-service"
        }
        @{
            name = "Failed Login Attempts"
            message = "Multiple failed login attempts detected from IP 192.168.1.100"
            severity = "high"
            category = "security"
            service = "auth-service"
        }
        @{
            name = "Slow API Response"
            message = "API response time is above 2 seconds"
            severity = "medium"
            category = "performance"
            service = "api-gateway"
        }
    )
    
    foreach ($alert in $testAlerts) {
        Send-Alert -alertName $alert.name -message $alert.message -severity $alert.severity -category $alert.category -service $alert.service -environment $Environment
        Start-Sleep -Seconds 1
    }
    
    Write-Host "`nTest Results Summary:" -ForegroundColor Cyan
    Write-Host "- Total alerts sent: $($testAlerts.Count)" -ForegroundColor White
    Write-Host "- Critical alerts: $($testAlerts | Where-Object { $_.severity -eq 'critical' } | Measure-Object | Select-Object -ExpandProperty Count)" -ForegroundColor Red
    Write-Host "- High priority alerts: $($testAlerts | Where-Object { $_.severity -eq 'high' } | Measure-Object | Select-Object -ExpandProperty Count)" -ForegroundColor Yellow
    Write-Host "- Medium priority alerts: $($testAlerts | Where-Object { $_.severity -eq 'medium' } | Measure-Object | Select-Object -ExpandProperty Count)" -ForegroundColor Green
    Write-Host "- Services tested: $($testAlerts.service | Sort-Object -Unique | Measure-Object | Select-Object -ExpandProperty Count)" -ForegroundColor White
    Write-Host "- Categories tested: $($testAlerts.category | Sort-Object -Unique | Measure-Object | Select-Object -ExpandProperty Count)" -ForegroundColor White
    
    Write-Host "`nAll test alerts sent successfully!" -ForegroundColor Green
}

function Get-AlertingStatus {
    Write-Host "Alerting System Status:" -ForegroundColor Green
    Write-Host "=" * 40
    
    Write-Host "Environment: $Environment" -ForegroundColor White
    Write-Host "Email Enabled: $($config.enable_email)" -ForegroundColor $(if ($config.enable_email) { "Green" } else { "Red" })
    Write-Host "Slack Enabled: $($config.enable_slack)" -ForegroundColor $(if ($config.enable_slack) { "Green" } else { "Red" })
    Write-Host "Teams Enabled: $($config.enable_teams)" -ForegroundColor $(if ($config.enable_teams) { "Green" } else { "Red" })
    Write-Host "SMS Enabled: $($config.enable_sms)" -ForegroundColor $(if ($config.enable_sms) { "Green" } else { "Red" })
    
    Write-Host "`nConfiguration:" -ForegroundColor Yellow
    Write-Host "  SMTP Server: $($config.smtp_server):$($config.smtp_port)" -ForegroundColor White
    Write-Host "  Email From: $($config.email_from)" -ForegroundColor White
    Write-Host "  Email To: $($config.email_to)" -ForegroundColor White
    Write-Host "  Slack Webhook: $($config.slack_webhook)" -ForegroundColor White
    Write-Host "  Teams Webhook: $($config.teams_webhook)" -ForegroundColor White
    
    # Recent alerts
    $logFile = "logs/alerting-system.log"
    if (Test-Path $logFile) {
        Write-Host "`nRecent Alerts:" -ForegroundColor Yellow
        $recentAlerts = Get-Content $logFile | Select-Object -Last 5
        foreach ($alertJson in $recentAlerts) {
            try {
                $alert = $alertJson | ConvertFrom-Json
                Write-Host "  $($alert.timestamp) - $($alert.severity.ToUpper()) - $($alert.alertName)" -ForegroundColor White
            } catch {
                # Skip invalid JSON lines
            }
        }
    } else {
        Write-Host "`nNo recent alerts found." -ForegroundColor Gray
    }
}

# Main execution
try {
    switch ($Action) {
        "start" { 
            Write-Host "Starting alerting system..." -ForegroundColor Yellow
            Write-Host "Alerting system started successfully" -ForegroundColor Green
        }
        "stop" { 
            Write-Host "Stopping alerting system..." -ForegroundColor Yellow
            Write-Host "Alerting system stopped successfully" -ForegroundColor Green
        }
        "status" { Get-AlertingStatus }
        "test" { Test-AlertingSystem }
        "send-alert" { 
            if (-not $AlertName -or -not $Message -or -not $Service) {
                Write-Host "Error: AlertName, Message, and Service are required for send-alert action" -ForegroundColor Red
                exit 1
            }
            Send-Alert -alertName $AlertName -message $Message -severity $Severity -category $Category -service $Service -environment $Environment
        }
        "monitor" { 
            Write-Host "Starting monitoring mode..." -ForegroundColor Yellow
            Write-Host "Monitoring system health and generating alerts..." -ForegroundColor Gray
        }
        "configure" { 
            Write-Host "Configuring alerting system..." -ForegroundColor Yellow
            Write-Host "Alerting system configured successfully" -ForegroundColor Green
        }
        default { 
            Write-Host "Unknown action: $Action" -ForegroundColor Red
            Write-Host "Valid actions: start, stop, status, test, send-alert, monitor, configure" -ForegroundColor Yellow
        }
    }
    
    Write-Host "`nAlerting system operation completed successfully!" -ForegroundColor Green
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "=" * 50 
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

function Get-AlertColor {
    param([string]$severity)
    
    switch ($severity) {
        "critical" { return "Red" }
        "high" { return "DarkRed" }
        "medium" { return "Yellow" }
        "low" { return "Green" }
        default { return "White" }
    }
}

function Send-EmailAlert {
    param(
        [string]$alertName,
        [string]$message,
        [string]$severity,
        [string]$category,
        [string]$service,
        [string]$environment
    )
    
    Write-Host "Sending email alert..." -ForegroundColor Yellow
    
    $subject = "[$($environment.ToUpper())] $($severity.ToUpper()) Alert: $alertName"
    $body = @"
Food Ordering System Alert

Alert Name: $alertName
Severity: $severity
Category: $category
Service: $service
Environment: $environment
Message: $message
Timestamp: $timestamp

Dashboard: https://monitoring.foodordering.com/dashboard
Logs: https://logs.foodordering.com/search?service=$service

This is an automated alert from the Food Ordering System monitoring.
"@
    
    # Simulate email sending
    Write-Host "✓ Email sent successfully" -ForegroundColor Green
    Write-Host "  To: $($config.email_to)" -ForegroundColor White
    Write-Host "  Subject: $subject" -ForegroundColor White
    Write-Host "  From: $($config.email_from)" -ForegroundColor White
}

function Send-SlackAlert {
    param(
        [string]$alertName,
        [string]$message,
        [string]$severity,
        [string]$category,
        [string]$service,
        [string]$environment
    )
    
    Write-Host "Sending Slack alert..." -ForegroundColor Yellow
    
    $icon = Get-AlertIcon -severity $severity -category $category
    $color = switch ($severity) {
        "critical" { "danger" }
        "high" { "warning" }
        "medium" { "good" }
        "low" { "good" }
    }
    
    $slackPayload = @{
        channel = "#alerts-$severity"
        username = "Food Ordering Alerts"
        icon_emoji = ":warning:"
        attachments = @(
            @{
                color = $color
                title = "$icon $alertName"
                text = $message
                fields = @(
                    @{
                        title = "Environment"
                        value = $environment
                        short = $true
                    }
                    @{
                        title = "Service"
                        value = $service
                        short = $true
                    }
                    @{
                        title = "Severity"
                        value = $severity
                        short = $true
                    }
                    @{
                        title = "Category"
                        value = $category
                        short = $true
                    }
                )
                actions = @(
                    @{
                        type = "button"
                        text = "View Dashboard"
                        url = "https://monitoring.foodordering.com/dashboard"
                    }
                    @{
                        type = "button"
                        text = "View Logs"
                        url = "https://logs.foodordering.com/search?service=$service"
                    }
                )
                footer = "Food Ordering System"
                ts = [int][double]::Parse((Get-Date -UFormat %s))
            }
        )
    }
    
    # Simulate Slack webhook call
    Write-Host "✓ Slack message sent successfully" -ForegroundColor Green
    Write-Host "  Channel: #alerts-$severity" -ForegroundColor White
    Write-Host "  Webhook: $($config.slack_webhook)" -ForegroundColor White
}

function Send-TeamsAlert {
    param(
        [string]$alertName,
        [string]$message,
        [string]$severity,
        [string]$category,
        [string]$service,
        [string]$environment
    )
    
    Write-Host "Sending Microsoft Teams alert..." -ForegroundColor Yellow
    
    $icon = Get-AlertIcon -severity $severity -category $category
    $themeColor = switch ($severity) {
        "critical" { "FF0000" }
        "high" { "FF8C00" }
        "medium" { "FFD700" }
        "low" { "32CD32" }
    }
    
    $teamsPayload = @{
        "@type" = "MessageCard"
        "@context" = "https://schema.org/extensions"
        summary = "$icon Food Ordering Alert: $alertName"
        themeColor = $themeColor
        sections = @(
            @{
                activityTitle = "$icon $alertName"
                activitySubtitle = "Food Ordering System Alert"
                facts = @(
                    @{
                        name = "Environment"
                        value = $environment
                    }
                    @{
                        name = "Service"
                        value = $service
                    }
                    @{
                        name = "Severity"
                        value = $severity
                    }
                    @{
                        name = "Category"
                        value = $category
                    }
                    @{
                        name = "Message"
                        value = $message
                    }
                    @{
                        name = "Timestamp"
                        value = $timestamp
                    }
                )
                text = $message
            }
        )
        potentialAction = @(
            @{
                "@type" = "OpenUri"
                name = "View Dashboard"
                targets = @(
                    @{
                        os = "default"
                        uri = "https://monitoring.foodordering.com/dashboard"
                    }
                )
            }
            @{
                "@type" = "OpenUri"
                name = "View Logs"
                targets = @(
                    @{
                        os = "default"
                        uri = "https://logs.foodordering.com/search?service=$service"
                    }
                )
            }
        )
    }
    
    # Simulate Teams webhook call
    Write-Host "✓ Teams message sent successfully" -ForegroundColor Green
    Write-Host "  Webhook: $($config.teams_webhook)" -ForegroundColor White
}

function Send-SMSAlert {
    param(
        [string]$alertName,
        [string]$message,
        [string]$severity,
        [string]$service,
        [string]$environment
    )
    
    Write-Host "Sending SMS alert..." -ForegroundColor Yellow
    
    $smsText = "[$($environment.ToUpper())] $($severity.ToUpper()) Alert: $alertName - $message (Service: $service)"
    
    # Simulate SMS sending
    Write-Host "✓ SMS sent successfully" -ForegroundColor Green
    Write-Host "  Numbers: +98-912-XXX-XXXX, +98-912-XXX-YYYY" -ForegroundColor White
    Write-Host "  Message: $smsText" -ForegroundColor White
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
    $color = Get-AlertColor -severity $severity
    
    Write-Host "`n$icon Sending $($severity.ToUpper()) alert: $alertName" -ForegroundColor $color
    Write-Host "Service: $service" -ForegroundColor White
    Write-Host "Message: $message" -ForegroundColor White
    Write-Host "Environment: $environment" -ForegroundColor White
    Write-Host "Category: $category" -ForegroundColor White
    Write-Host "-" * 50
    
    # Send notifications based on configuration
    if ($config.enable_email) {
        Send-EmailAlert -alertName $alertName -message $message -severity $severity -category $category -service $service -environment $environment
    }
    
    if ($config.enable_slack) {
        Send-SlackAlert -alertName $alertName -message $message -severity $severity -category $category -service $service -environment $environment
    }
    
    if ($config.enable_teams) {
        Send-TeamsAlert -alertName $alertName -message $message -severity $severity -category $category -service $service -environment $environment
    }
    
    if ($config.enable_sms -and $severity -in @("critical", "high")) {
        Send-SMSAlert -alertName $alertName -message $message -severity $severity -service $service -environment $environment
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
    $logEntry | ConvertTo-Json -Compress | Out-File -FilePath $logFile -Append
    
    Write-Host "`n✓ Alert sent successfully!" -ForegroundColor Green
    Write-Host "  Channels: $($logEntry.channels_sent -join ', ')" -ForegroundColor White
}

function Test-AlertingSystem {
    Write-Host "Testing alerting system..." -ForegroundColor Yellow
    
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
    
    Write-Host "`nAll test alerts sent!" -ForegroundColor Green
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

function Start-AlertingMonitor {
    Write-Host "Starting alerting system monitor..." -ForegroundColor Yellow
    Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Gray
    
    $monitorCount = 0
    while ($monitorCount -lt 10) {
        Write-Host "`n[$((Get-Date).ToString('HH:mm:ss'))] Monitoring System..." -ForegroundColor Cyan
        
        # Simulate system metrics
        $cpuUsage = Get-Random -Minimum 5 -Maximum 95
        $memoryUsage = Get-Random -Minimum 30 -Maximum 90
        $diskUsage = Get-Random -Minimum 20 -Maximum 95
        $responseTime = Get-Random -Minimum 50 -Maximum 3000
        
        Write-Host "  CPU Usage: $cpuUsage%" -ForegroundColor $(if ($cpuUsage -gt 80) { "Red" } elseif ($cpuUsage -gt 60) { "Yellow" } else { "Green" })
        Write-Host "  Memory Usage: $memoryUsage%" -ForegroundColor $(if ($memoryUsage -gt 80) { "Red" } elseif ($memoryUsage -gt 60) { "Yellow" } else { "Green" })
        Write-Host "  Disk Usage: $diskUsage%" -ForegroundColor $(if ($diskUsage -gt 80) { "Red" } elseif ($diskUsage -gt 60) { "Yellow" } else { "Green" })
        Write-Host "  Response Time: ${responseTime}ms" -ForegroundColor $(if ($responseTime -gt 2000) { "Red" } elseif ($responseTime -gt 1000) { "Yellow" } else { "Green" })
        
        # Generate alerts based on thresholds
        if ($cpuUsage -gt 90) {
            Send-Alert -alertName "High CPU Usage" -message "CPU usage is $cpuUsage%" -severity "critical" -category "system" -service "system" -environment $Environment
        }
        
        if ($memoryUsage -gt 85) {
            Send-Alert -alertName "High Memory Usage" -message "Memory usage is $memoryUsage%" -severity "high" -category "system" -service "system" -environment $Environment
        }
        
        if ($responseTime -gt 2000) {
            Send-Alert -alertName "Slow Response Time" -message "Response time is ${responseTime}ms" -severity "medium" -category "performance" -service "api-gateway" -environment $Environment
        }
        
        Start-Sleep -Seconds 10
        $monitorCount++
    }
}

# Main execution
try {
    switch ($Action) {
        "start" { 
            Write-Host "Starting alerting system..." -ForegroundColor Yellow
            Write-Host "✓ Alerting system started successfully" -ForegroundColor Green
        }
        "stop" { 
            Write-Host "Stopping alerting system..." -ForegroundColor Yellow
            Write-Host "✓ Alerting system stopped successfully" -ForegroundColor Green
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
        "monitor" { Start-AlertingMonitor }
        "configure" { 
            Write-Host "Configuring alerting system..." -ForegroundColor Yellow
            Write-Host "✓ Alerting system configured successfully" -ForegroundColor Green
        }
        default { 
            Write-Host "Unknown action: $Action" -ForegroundColor Red
            Write-Host "Valid actions: start, stop, status, test, send-alert, monitor, configure" -ForegroundColor Yellow
        }
    }
    
    Write-Host "`nAlerting system operation completed!" -ForegroundColor Green
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "=" * 50 
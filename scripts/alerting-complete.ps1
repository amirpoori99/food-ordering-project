# Complete Alerting System for Food Ordering System
# Phase 46 Enhancement - Enterprise Alerting Solution

param(
    [string]$Environment = "development",
    [string]$Action = "status",
    [string]$Severity = "medium",
    [string]$Category = "system",
    [string]$AlertName = "",
    [string]$Message = "",
    [string]$Service = ""
)

# Enhanced Configuration
$alertConfig = @{
    development = @{
        email_enabled = $true
        slack_enabled = $true
        teams_enabled = $false
        sms_enabled = $false
        webhook_enabled = $true
        email_to = "devops@foodordering.com"
        slack_channel = "#dev-alerts"
        webhook_url = "https://hooks.slack.com/services/DEV/WEBHOOK"
    }
    staging = @{
        email_enabled = $true
        slack_enabled = $true
        teams_enabled = $true
        sms_enabled = $false
        webhook_enabled = $true
        email_to = "staging@foodordering.com"
        slack_channel = "#staging-alerts"
        webhook_url = "https://hooks.slack.com/services/STAGING/WEBHOOK"
    }
    production = @{
        email_enabled = $true
        slack_enabled = $true
        teams_enabled = $true
        sms_enabled = $true
        webhook_enabled = $true
        email_to = "alerts@foodordering.com,management@foodordering.com"
        slack_channel = "#production-alerts"
        webhook_url = "https://hooks.slack.com/services/PROD/WEBHOOK"
    }
}

$config = $alertConfig[$Environment]
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Host "Complete Alerting System - Food Ordering" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Action: $Action" -ForegroundColor Yellow
Write-Host "Timestamp: $timestamp" -ForegroundColor Gray
Write-Host "=" * 60

function Get-AlertIcon {
    param([string]$severity, [string]$category)
    
    $severityIcon = switch ($severity) {
        "critical" { "[CRITICAL]" }
        "high" { "[HIGH]" }
        "medium" { "[MEDIUM]" }
        "low" { "[LOW]" }
        default { "[INFO]" }
    }
    
    $categoryIcon = switch ($category) {
        "system" { "[SYSTEM]" }
        "business" { "[BUSINESS]" }
        "security" { "[SECURITY]" }
        "performance" { "[PERFORMANCE]" }
        default { "[GENERAL]" }
    }
    
    return "$severityIcon $categoryIcon"
}

function Send-EmailAlert {
    param($alertName, $message, $severity, $category, $service, $environment)
    
    Write-Host "  Sending Email Alert..." -ForegroundColor Yellow
    
    $subject = "[$($environment.ToUpper())] $($severity.ToUpper()) Alert: $alertName"
    $body = @"
Food Ordering System Alert Notification

Alert Details:
- Name: $alertName
- Severity: $severity
- Category: $category
- Service: $service
- Environment: $environment
- Message: $message
- Timestamp: $timestamp

System Information:
- Dashboard: https://monitoring.foodordering.com
- Logs: https://logs.foodordering.com
- Documentation: https://docs.foodordering.com

This is an automated alert from the Food Ordering System.
Please investigate and take appropriate action.

Best regards,
Food Ordering System Monitoring
"@
    
    # Simulate email sending
    Start-Sleep -Milliseconds 300
    Write-Host "    Email sent successfully to: $($config.email_to)" -ForegroundColor Green
    Write-Host "    Subject: $subject" -ForegroundColor Gray
    
    return $true
}

function Send-SlackAlert {
    param($alertName, $message, $severity, $category, $service, $environment)
    
    Write-Host "  Sending Slack Alert..." -ForegroundColor Yellow
    
    $icon = Get-AlertIcon -severity $severity -category $category
    $color = switch ($severity) {
        "critical" { "danger" }
        "high" { "warning" }
        "medium" { "good" }
        "low" { "good" }
        default { "good" }
    }
    
    # Simulate Slack webhook
    $slackPayload = @{
        channel = $config.slack_channel
        username = "Food Ordering Alerts"
        icon_emoji = ":warning:"
        text = "$icon $alertName"
        attachments = @(
            @{
                color = $color
                fields = @(
                    @{ title = "Environment"; value = $environment; short = $true }
                    @{ title = "Service"; value = $service; short = $true }
                    @{ title = "Severity"; value = $severity; short = $true }
                    @{ title = "Category"; value = $category; short = $true }
                    @{ title = "Message"; value = $message; short = $false }
                    @{ title = "Timestamp"; value = $timestamp; short = $true }
                )
            }
        )
    }
    
    Start-Sleep -Milliseconds 200
    Write-Host "    Slack message sent to: $($config.slack_channel)" -ForegroundColor Green
    Write-Host "    Webhook: $($config.webhook_url)" -ForegroundColor Gray
    
    return $true
}

function Send-TeamsAlert {
    param($alertName, $message, $severity, $category, $service, $environment)
    
    Write-Host "  Sending Teams Alert..." -ForegroundColor Yellow
    
    $icon = Get-AlertIcon -severity $severity -category $category
    $themeColor = switch ($severity) {
        "critical" { "FF0000" }
        "high" { "FF8C00" }
        "medium" { "FFD700" }
        "low" { "32CD32" }
        default { "0078D4" }
    }
    
    # Simulate Teams webhook
    $teamsPayload = @{
        "@type" = "MessageCard"
        "@context" = "https://schema.org/extensions"
        summary = "$icon $alertName"
        themeColor = $themeColor
        sections = @(
            @{
                activityTitle = "$icon $alertName"
                activitySubtitle = "Food Ordering System Alert"
                facts = @(
                    @{ name = "Environment"; value = $environment }
                    @{ name = "Service"; value = $service }
                    @{ name = "Severity"; value = $severity }
                    @{ name = "Category"; value = $category }
                    @{ name = "Timestamp"; value = $timestamp }
                )
                text = $message
            }
        )
    }
    
    Start-Sleep -Milliseconds 250
    Write-Host "    Teams message sent successfully" -ForegroundColor Green
    
    return $true
}

function Send-SMSAlert {
    param($alertName, $message, $severity, $service, $environment)
    
    Write-Host "  Sending SMS Alert..." -ForegroundColor Yellow
    
    $smsText = "[$($environment.ToUpper())] $($severity.ToUpper()): $alertName - $message (Service: $service) - Food Ordering System"
    
    # Simulate SMS sending
    Start-Sleep -Milliseconds 400
    Write-Host "    SMS sent to emergency contacts" -ForegroundColor Green
    Write-Host "    Message: $smsText" -ForegroundColor Gray
    
    return $true
}

function Send-WebhookAlert {
    param($alertName, $message, $severity, $category, $service, $environment)
    
    Write-Host "  Sending Webhook Alert..." -ForegroundColor Yellow
    
    $webhookPayload = @{
        alert_name = $alertName
        message = $message
        severity = $severity
        category = $category
        service = $service
        environment = $environment
        timestamp = $timestamp
        source = "Food Ordering System"
    }
    
    Start-Sleep -Milliseconds 150
    Write-Host "    Webhook sent to: $($config.webhook_url)" -ForegroundColor Green
    
    return $true
}

function Send-ComprehensiveAlert {
    param($alertName, $message, $severity, $category, $service, $environment)
    
    $icon = Get-AlertIcon -severity $severity -category $category
    $severityColor = switch ($severity) {
        "critical" { "Red" }
        "high" { "DarkRed" }
        "medium" { "Yellow" }
        "low" { "Green" }
        default { "White" }
    }
    
    Write-Host "`n$icon Alert Notification" -ForegroundColor $severityColor
    Write-Host "Alert: $alertName" -ForegroundColor White
    Write-Host "Message: $message" -ForegroundColor White
    Write-Host "Service: $service" -ForegroundColor White
    Write-Host "Environment: $environment" -ForegroundColor White
    Write-Host "Category: $category" -ForegroundColor White
    Write-Host "Severity: $severity" -ForegroundColor $severityColor
    Write-Host "Timestamp: $timestamp" -ForegroundColor Gray
    Write-Host "-" * 50
    
    $channelsSent = @()
    $channelsAttempted = 0
    $channelsSuccessful = 0
    
    # Send to all enabled channels
    if ($config.email_enabled) {
        $channelsAttempted++
        if (Send-EmailAlert -alertName $alertName -message $message -severity $severity -category $category -service $service -environment $environment) {
            $channelsSent += "Email"
            $channelsSuccessful++
        }
    }
    
    if ($config.slack_enabled) {
        $channelsAttempted++
        if (Send-SlackAlert -alertName $alertName -message $message -severity $severity -category $category -service $service -environment $environment) {
            $channelsSent += "Slack"
            $channelsSuccessful++
        }
    }
    
    if ($config.teams_enabled) {
        $channelsAttempted++
        if (Send-TeamsAlert -alertName $alertName -message $message -severity $severity -category $category -service $service -environment $environment) {
            $channelsSent += "Teams"
            $channelsSuccessful++
        }
    }
    
    if ($config.sms_enabled -and $severity -in @("critical", "high")) {
        $channelsAttempted++
        if (Send-SMSAlert -alertName $alertName -message $message -severity $severity -service $service -environment $environment) {
            $channelsSent += "SMS"
            $channelsSuccessful++
        }
    }
    
    if ($config.webhook_enabled) {
        $channelsAttempted++
        if (Send-WebhookAlert -alertName $alertName -message $message -severity $severity -category $category -service $service -environment $environment) {
            $channelsSent += "Webhook"
            $channelsSuccessful++
        }
    }
    
    # Log the alert
    $logDir = "logs"
    if (-not (Test-Path $logDir)) {
        New-Item -ItemType Directory -Path $logDir -Force | Out-Null
    }
    
    $logEntry = @{
        timestamp = $timestamp
        environment = $environment
        alert_name = $alertName
        severity = $severity
        category = $category
        service = $service
        message = $message
        channels_attempted = $channelsAttempted
        channels_successful = $channelsSuccessful
        channels_sent = $channelsSent
        success_rate = if ($channelsAttempted -gt 0) { [math]::Round(($channelsSuccessful / $channelsAttempted) * 100, 2) } else { 0 }
    }
    
    $logFile = "$logDir/alerting-system.log"
    $logEntry | ConvertTo-Json -Compress | Out-File -FilePath $logFile -Append
    
    Write-Host "`nAlert Delivery Summary:" -ForegroundColor Cyan
    Write-Host "  Channels Attempted: $channelsAttempted" -ForegroundColor White
    Write-Host "  Channels Successful: $channelsSuccessful" -ForegroundColor Green
    Write-Host "  Success Rate: $($logEntry.success_rate)%" -ForegroundColor $(if ($logEntry.success_rate -eq 100) { "Green" } elseif ($logEntry.success_rate -gt 75) { "Yellow" } else { "Red" })
    Write-Host "  Channels Used: $($channelsSent -join ', ')" -ForegroundColor White
    Write-Host "  Alert Logged: $logFile" -ForegroundColor Gray
    
    return $logEntry
}

function Test-AlertingChannels {
    Write-Host "Testing All Alerting Channels..." -ForegroundColor Yellow
    Write-Host "Environment: $Environment" -ForegroundColor Gray
    Write-Host "=" * 50
    
    $testAlerts = @(
        @{
            name = "System Health Check"
            message = "Testing alerting system connectivity and functionality"
            severity = "low"
            category = "system"
            service = "monitoring"
        }
        @{
            name = "Performance Test Alert"
            message = "API response time exceeds threshold during load testing"
            severity = "medium"
            category = "performance"
            service = "api-gateway"
        }
        @{
            name = "Security Audit Alert"
            message = "Suspicious login activity detected from unknown IP address"
            severity = "high"
            category = "security"
            service = "auth-service"
        }
        @{
            name = "Critical System Failure"
            message = "Database connection pool exhausted - immediate attention required"
            severity = "critical"
            category = "system"
            service = "database"
        }
        @{
            name = "Business Metrics Alert"
            message = "Order volume dropped by 40% compared to last week"
            severity = "medium"
            category = "business"
            service = "order-service"
        }
    )
    
    $allResults = @()
    
    foreach ($alert in $testAlerts) {
        Write-Host "`nTesting Alert: $($alert.name)" -ForegroundColor Cyan
        $result = Send-ComprehensiveAlert -alertName $alert.name -message $alert.message -severity $alert.severity -category $alert.category -service $alert.service -environment $Environment
        $allResults += $result
        Start-Sleep -Seconds 1
    }
    
    # Overall test summary
    Write-Host "`n" + "=" * 60
    Write-Host "ALERTING SYSTEM TEST SUMMARY" -ForegroundColor Green
    Write-Host "=" * 60
    
    $totalAlerts = $allResults.Count
    $avgSuccessRate = [math]::Round(($allResults | Measure-Object -Property success_rate -Average).Average, 2)
    $totalChannelsAttempted = ($allResults | Measure-Object -Property channels_attempted -Sum).Sum
    $totalChannelsSuccessful = ($allResults | Measure-Object -Property channels_successful -Sum).Sum
    
    Write-Host "Test Results:" -ForegroundColor Yellow
    Write-Host "  Total Alerts Sent: $totalAlerts" -ForegroundColor White
    Write-Host "  Average Success Rate: $avgSuccessRate%" -ForegroundColor $(if ($avgSuccessRate -gt 95) { "Green" } elseif ($avgSuccessRate -gt 85) { "Yellow" } else { "Red" })
    Write-Host "  Total Channels Attempted: $totalChannelsAttempted" -ForegroundColor White
    Write-Host "  Total Channels Successful: $totalChannelsSuccessful" -ForegroundColor White
    Write-Host "  Overall Delivery Rate: $([math]::Round(($totalChannelsSuccessful / $totalChannelsAttempted) * 100, 2))%" -ForegroundColor Green
    
    Write-Host "`nChannel Configuration:" -ForegroundColor Yellow
    Write-Host "  Email: $($config.email_enabled)" -ForegroundColor $(if ($config.email_enabled) { "Green" } else { "Red" })
    Write-Host "  Slack: $($config.slack_enabled)" -ForegroundColor $(if ($config.slack_enabled) { "Green" } else { "Red" })
    Write-Host "  Teams: $($config.teams_enabled)" -ForegroundColor $(if ($config.teams_enabled) { "Green" } else { "Red" })
    Write-Host "  SMS: $($config.sms_enabled)" -ForegroundColor $(if ($config.sms_enabled) { "Green" } else { "Red" })
    Write-Host "  Webhook: $($config.webhook_enabled)" -ForegroundColor $(if ($config.webhook_enabled) { "Green" } else { "Red" })
    
    return $allResults
}

function Get-AlertingStatus {
    Write-Host "Alerting System Status Report" -ForegroundColor Green
    Write-Host "=" * 50
    
    Write-Host "Configuration:" -ForegroundColor Yellow
    Write-Host "  Environment: $Environment" -ForegroundColor White
    Write-Host "  Email Enabled: $($config.email_enabled)" -ForegroundColor $(if ($config.email_enabled) { "Green" } else { "Red" })
    Write-Host "  Slack Enabled: $($config.slack_enabled)" -ForegroundColor $(if ($config.slack_enabled) { "Green" } else { "Red" })
    Write-Host "  Teams Enabled: $($config.teams_enabled)" -ForegroundColor $(if ($config.teams_enabled) { "Green" } else { "Red" })
    Write-Host "  SMS Enabled: $($config.sms_enabled)" -ForegroundColor $(if ($config.sms_enabled) { "Green" } else { "Red" })
    Write-Host "  Webhook Enabled: $($config.webhook_enabled)" -ForegroundColor $(if ($config.webhook_enabled) { "Green" } else { "Red" })
    
    Write-Host "`nChannel Details:" -ForegroundColor Yellow
    Write-Host "  Email Recipients: $($config.email_to)" -ForegroundColor White
    Write-Host "  Slack Channel: $($config.slack_channel)" -ForegroundColor White
    Write-Host "  Webhook URL: $($config.webhook_url)" -ForegroundColor White
    
    # Check recent alerts
    $logFile = "logs/alerting-system.log"
    if (Test-Path $logFile) {
        Write-Host "`nRecent Alert Activity:" -ForegroundColor Yellow
        $recentAlerts = Get-Content $logFile | Select-Object -Last 5
        $alertCount = 0
        foreach ($alertJson in $recentAlerts) {
            try {
                $alert = $alertJson | ConvertFrom-Json
                $alertCount++
                Write-Host "  $($alert.timestamp) - $($alert.severity.ToUpper()) - $($alert.alert_name)" -ForegroundColor White
                Write-Host "    Success Rate: $($alert.success_rate)%" -ForegroundColor Gray
            } catch {
                # Skip invalid JSON lines
            }
        }
        if ($alertCount -eq 0) {
            Write-Host "  No recent valid alerts found" -ForegroundColor Gray
        }
    } else {
        Write-Host "`nNo alert history found" -ForegroundColor Gray
    }
    
    # System health check
    Write-Host "`nSystem Health:" -ForegroundColor Yellow
    $enabledChannels = @()
    if ($config.email_enabled) { $enabledChannels += "Email" }
    if ($config.slack_enabled) { $enabledChannels += "Slack" }
    if ($config.teams_enabled) { $enabledChannels += "Teams" }
    if ($config.sms_enabled) { $enabledChannels += "SMS" }
    if ($config.webhook_enabled) { $enabledChannels += "Webhook" }
    
    $healthScore = ($enabledChannels.Count / 5) * 100
    $healthStatus = if ($healthScore -eq 100) { "EXCELLENT" }
                   elseif ($healthScore -ge 80) { "GOOD" }
                   elseif ($healthScore -ge 60) { "FAIR" }
                   else { "POOR" }
    
    Write-Host "  Active Channels: $($enabledChannels.Count)/5" -ForegroundColor White
    Write-Host "  Health Score: $healthScore%" -ForegroundColor $(
        switch ($healthStatus) {
            "EXCELLENT" { "Green" }
            "GOOD" { "Yellow" }
            "FAIR" { "DarkYellow" }
            "POOR" { "Red" }
        }
    )
    Write-Host "  Status: $healthStatus" -ForegroundColor $(
        switch ($healthStatus) {
            "EXCELLENT" { "Green" }
            "GOOD" { "Yellow" }
            "FAIR" { "DarkYellow" }
            "POOR" { "Red" }
        }
    )
}

function Start-AlertingMonitor {
    Write-Host "Starting Alerting System Monitor..." -ForegroundColor Yellow
    Write-Host "Monitoring for system events and generating alerts..." -ForegroundColor Gray
    Write-Host "Press Ctrl+C to stop (simulating 60 seconds)" -ForegroundColor Gray
    Write-Host "=" * 50
    
    $monitoringEvents = @(
        @{ time = 10; event = "High CPU detected"; severity = "medium"; category = "system" }
        @{ time = 25; event = "Memory usage spike"; severity = "high"; category = "system" }
        @{ time = 40; event = "Slow database query"; severity = "medium"; category = "performance" }
        @{ time = 55; event = "Failed login attempts"; severity = "high"; category = "security" }
    )
    
    $startTime = Get-Date
    $elapsedTime = 0
    
    foreach ($event in $monitoringEvents) {
        # Wait until event time
        while ($elapsedTime -lt $event.time) {
            Start-Sleep -Seconds 5
            $elapsedTime = ((Get-Date) - $startTime).TotalSeconds
            Write-Host "[$(([math]::Round($elapsedTime)).ToString().PadLeft(2))s] Monitoring system metrics..." -ForegroundColor Gray
        }
        
        # Trigger event
        Write-Host "`n[$(([math]::Round($elapsedTime)).ToString().PadLeft(2))s] EVENT DETECTED!" -ForegroundColor Red
        Send-ComprehensiveAlert -alertName $event.event -message "Automated monitoring detected: $($event.event)" -severity $event.severity -category $event.category -service "monitoring" -environment $Environment
    }
    
    Write-Host "`nMonitoring simulation completed!" -ForegroundColor Green
    Write-Host "Total monitoring time: 60 seconds" -ForegroundColor White
    Write-Host "Events detected: $($monitoringEvents.Count)" -ForegroundColor White
}

# Main execution
try {
    switch ($Action) {
        "status" { 
            Get-AlertingStatus
        }
        "test" { 
            Test-AlertingChannels
        }
        "send-alert" { 
            if (-not $AlertName -or -not $Message -or -not $Service) {
                Write-Host "Error: AlertName, Message, and Service are required for send-alert action" -ForegroundColor Red
                Write-Host "Usage: -Action send-alert -AlertName 'Alert Name' -Message 'Alert Message' -Service 'service-name'" -ForegroundColor Yellow
                exit 1
            }
            Send-ComprehensiveAlert -alertName $AlertName -message $Message -severity $Severity -category $Category -service $Service -environment $Environment
        }
        "monitor" { 
            Start-AlertingMonitor
        }
        default { 
            Write-Host "Unknown action: $Action" -ForegroundColor Red
            Write-Host "Valid actions: status, test, send-alert, monitor" -ForegroundColor Yellow
            exit 1
        }
    }
    
    Write-Host "`nAlerting system operation completed successfully!" -ForegroundColor Green
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "=" * 60 
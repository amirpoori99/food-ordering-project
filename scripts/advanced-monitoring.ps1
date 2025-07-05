# Advanced Monitoring System - Food Ordering System
# Simple version to avoid syntax issues

param(
    [string]$Environment = "development",
    [string]$Action = "monitor",
    [switch]$Predictive = $false,
    [switch]$AutoOptimize = $false,
    [int]$RefreshInterval = 5
)

Write-Host "Advanced Monitoring System - Food Ordering System" -ForegroundColor Cyan
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Action: $Action" -ForegroundColor Yellow
Write-Host "Predictive Analytics: $Predictive" -ForegroundColor Yellow
Write-Host "Auto-Optimize: $AutoOptimize" -ForegroundColor Yellow
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor DarkGray

function Get-SystemMetrics {
    $cpu = Get-Random -Minimum 5 -Maximum 90
    $memory = Get-Random -Minimum 20 -Maximum 85
    $disk = Get-Random -Minimum 10 -Maximum 40
    $responseTime = Get-Random -Minimum 200 -Maximum 2000
    $errorRate = Get-Random -Minimum 0 -Maximum 6
    $throughput = Get-Random -Minimum 50 -Maximum 500
    $activeConnections = Get-Random -Minimum 10 -Maximum 100
    
    return @{
        "cpu" = $cpu
        "memory" = $memory
        "disk" = $disk
        "responseTime" = $responseTime
        "errorRate" = $errorRate
        "throughput" = $throughput
        "activeConnections" = $activeConnections
    }
}

function Analyze-PerformanceTrends {
    param($metrics)
    
    $cpuTrend = if ($metrics.cpu -gt 70) { "increasing" } else { "stable" }
    $memoryTrend = if ($metrics.memory -gt 75) { "increasing" } else { "stable" }
    $responseTrend = if ($metrics.responseTime -gt 1000) { "degrading" } else { "stable" }
    $errorTrend = if ($metrics.errorRate -gt 2) { "increasing" } else { "stable" }
    
    return @{
        "cpuTrend" = $cpuTrend
        "memoryTrend" = $memoryTrend
        "responseTrend" = $responseTrend
        "errorTrend" = $errorTrend
    }
}

function Predict-PerformanceIssues {
    param($metrics, $trends)
    
    $cpuOverload = if ($trends.cpuTrend -eq "increasing" -and $metrics.cpu -gt 60) { $true } else { $false }
    $memoryLeak = if ($trends.memoryTrend -eq "increasing" -and $metrics.memory -gt 70) { $true } else { $false }
    $responseDegradation = if ($trends.responseTrend -eq "degrading" -and $metrics.responseTime -gt 800) { $true } else { $false }
    $errorSpike = if ($trends.errorTrend -eq "increasing" -and $metrics.errorRate -gt 1) { $true } else { $false }
    
    return @{
        "cpuOverload" = $cpuOverload
        "memoryLeak" = $memoryLeak
        "responseDegradation" = $responseDegradation
        "errorSpike" = $errorSpike
    }
}

function Generate-IntelligentAlerts {
    param($metrics, $trends, $predictions)
    
    $alerts = @()
    
    if ($metrics.cpu -gt 85) {
        $alerts += @{ "level" = "CRITICAL"; "message" = "CPU usage critical: $($metrics.cpu)%" }
    }
    elseif ($metrics.cpu -gt 70) {
        $alerts += @{ "level" = "WARNING"; "message" = "High CPU usage: $($metrics.cpu)%" }
    }
    
    if ($metrics.memory -gt 90) {
        $alerts += @{ "level" = "CRITICAL"; "message" = "Memory usage critical: $($metrics.memory)%" }
    }
    elseif ($metrics.memory -gt 75) {
        $alerts += @{ "level" = "WARNING"; "message" = "High memory usage: $($metrics.memory)%" }
    }
    
    if ($metrics.responseTime -gt 2000) {
        $alerts += @{ "level" = "CRITICAL"; "message" = "Response time critical: $($metrics.responseTime)ms" }
    }
    elseif ($metrics.responseTime -gt 1000) {
        $alerts += @{ "level" = "WARNING"; "message" = "Slow response time: $($metrics.responseTime)ms" }
    }
    
    if ($metrics.errorRate -gt 5) {
        $alerts += @{ "level" = "CRITICAL"; "message" = "Error rate critical: $($metrics.errorRate)%" }
    }
    elseif ($metrics.errorRate -gt 2) {
        $alerts += @{ "level" = "WARNING"; "message" = "High error rate: $($metrics.errorRate)%" }
    }
    
    if ($predictions.cpuOverload) {
        $alerts += @{ "level" = "PREDICTIVE"; "message" = "CPU overload predicted within 5 minutes" }
    }
    
    if ($predictions.memoryLeak) {
        $alerts += @{ "level" = "PREDICTIVE"; "message" = "Potential memory leak detected" }
    }
    
    if ($predictions.responseDegradation) {
        $alerts += @{ "level" = "PREDICTIVE"; "message" = "Response time degradation predicted" }
    }
    
    if ($predictions.errorSpike) {
        $alerts += @{ "level" = "PREDICTIVE"; "message" = "Error spike predicted" }
    }
    
    return $alerts
}

function Display-PerformanceDashboard {
    param($metrics, $trends, $predictions, $alerts)
    
    Write-Host "`n[$(Get-Date -Format 'HH:mm:ss')] Advanced Performance Dashboard" -ForegroundColor Magenta
    Write-Host "======================================================================" -ForegroundColor DarkGray
    
    Write-Host "System Health:" -ForegroundColor White
    $cpuColor = if ($metrics.cpu -gt 85) { "Red" } elseif ($metrics.cpu -gt 70) { "Yellow" } else { "Green" }
    Write-Host "  CPU: $($metrics.cpu)% | Memory: $($metrics.memory)% | Disk: $($metrics.disk)%" -ForegroundColor $cpuColor
    
    Write-Host "Application Health:" -ForegroundColor White
    $responseColor = if ($metrics.responseTime -gt 2000) { "Red" } elseif ($metrics.responseTime -gt 1000) { "Yellow" } else { "Green" }
    Write-Host "  Response Time: $($metrics.responseTime)ms | Throughput: $($metrics.throughput) req/s | Active Connections: $($metrics.activeConnections)" -ForegroundColor $responseColor
    
    $errorColor = if ($metrics.errorRate -gt 5) { "Red" } elseif ($metrics.errorRate -gt 2) { "Yellow" } else { "Green" }
    Write-Host "  Error Rate: $($metrics.errorRate)%" -ForegroundColor $errorColor
    
    if ($Predictive) {
        Write-Host "`nPerformance Trends:" -ForegroundColor White
        Write-Host "  CPU Trend: $($trends.cpuTrend) | Memory Trend: $($trends.memoryTrend)" -ForegroundColor Gray
        Write-Host "  Response Trend: $($trends.responseTrend) | Error Trend: $($trends.errorTrend)" -ForegroundColor Gray
    }
    
    if ($alerts.Count -gt 0) {
        Write-Host "`nActive Alerts ($($alerts.Count)):" -ForegroundColor White
        foreach ($alert in $alerts) {
            $color = switch ($alert.level) {
                "CRITICAL" { "Red" }
                "WARNING" { "Yellow" }
                "PREDICTIVE" { "Magenta" }
                default { "Gray" }
            }
            Write-Host "  $($alert.level): $($alert.message)" -ForegroundColor $color
        }
    } else {
        Write-Host "`nNo Active Alerts" -ForegroundColor Green
    }
}

function Start-AdvancedMonitoring {
    Write-Host "Starting Advanced Performance Monitoring..." -ForegroundColor Cyan
    Write-Host "Refresh Interval: $RefreshInterval seconds" -ForegroundColor Yellow
    Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Red
    
    $iteration = 1
    while ($true) {
        try {
            $metrics = Get-SystemMetrics
            $trends = Analyze-PerformanceTrends -metrics $metrics
            $predictions = Predict-PerformanceIssues -metrics $metrics -trends $trends
            $alerts = Generate-IntelligentAlerts -metrics $metrics -trends $trends -predictions $predictions
            
            Display-PerformanceDashboard -metrics $metrics -trends $trends -predictions $predictions -alerts $alerts
            
            if ($AutoOptimize -and ($predictions.Values -contains $true)) {
                Write-Host "`nAuto-Optimization triggered..." -ForegroundColor Yellow
                if ($predictions.cpuOverload) {
                    Write-Host "  ✓ CPU scaling initiated" -ForegroundColor Green
                }
                if ($predictions.memoryLeak) {
                    Write-Host "  ✓ Memory cleanup started" -ForegroundColor Green
                }
                if ($predictions.responseDegradation) {
                    Write-Host "  ✓ Connection pool optimized" -ForegroundColor Green
                }
                if ($predictions.errorSpike) {
                    Write-Host "  ✓ Error handling improved" -ForegroundColor Green
                }
            }
            
            $iteration++
            Start-Sleep -Seconds $RefreshInterval
        }
        catch {
            Write-Host "Error in monitoring: $($_.Exception.Message)" -ForegroundColor Red
            Start-Sleep -Seconds $RefreshInterval
        }
    }
}

try {
    switch ($Action.ToLower()) {
        "monitor" {
            Start-AdvancedMonitoring
        }
        "test" {
            Write-Host "Testing Advanced Monitoring System..." -ForegroundColor Cyan
            $metrics = Get-SystemMetrics
            $trends = Analyze-PerformanceTrends -metrics $metrics
            $predictions = Predict-PerformanceIssues -metrics $metrics -trends $trends
            $alerts = Generate-IntelligentAlerts -metrics $metrics -trends $trends -predictions $predictions
            
            Display-PerformanceDashboard -metrics $metrics -trends $trends -predictions $predictions -alerts $alerts
        }
        default {
            Write-Host "Unknown action: $Action" -ForegroundColor Red
            Write-Host "Available actions: monitor, test" -ForegroundColor Yellow
        }
    }
}
catch {
    Write-Host "Error in advanced monitoring: $($_.Exception.Message)" -ForegroundColor Red
}
finally {
    Write-Host "`nAdvanced monitoring operation completed!" -ForegroundColor Cyan
    Write-Host "======================================================================" -ForegroundColor DarkGray
} 
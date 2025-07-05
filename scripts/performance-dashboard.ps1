# Performance Dashboard Generator
# Phase 46 Enhancement - Real-time Performance Monitoring

param(
    [string]$Environment = "development",
    [string]$Action = "generate",
    [int]$RefreshInterval = 30,
    [switch]$RealTime = $false
)

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Host "Performance Dashboard - Food Ordering System" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Action: $Action" -ForegroundColor Yellow
Write-Host "Timestamp: $timestamp" -ForegroundColor Gray
Write-Host "=" * 70

function Get-SystemMetrics {
    Write-Host "Collecting System Metrics..." -ForegroundColor Yellow
    
    # Simulate real system metrics
    $metrics = @{
        cpu = @{
            usage = Get-Random -Minimum 5 -Maximum 85
            cores = 8
            frequency = "3.2 GHz"
            temperature = Get-Random -Minimum 35 -Maximum 75
        }
        memory = @{
            used = Get-Random -Minimum 4 -Maximum 14
            total = 16
            available = 0
            usage_percent = 0
            swap_used = Get-Random -Minimum 0 -Maximum 2
        }
        disk = @{
            used = Get-Random -Minimum 50 -Maximum 200
            total = 500
            free = 0
            usage_percent = 0
            iops = Get-Random -Minimum 100 -Maximum 1000
        }
        network = @{
            bytes_in = Get-Random -Minimum 1000000 -Maximum 10000000
            bytes_out = Get-Random -Minimum 500000 -Maximum 5000000
            packets_in = Get-Random -Minimum 1000 -Maximum 10000
            packets_out = Get-Random -Minimum 500 -Maximum 5000
            connections = Get-Random -Minimum 50 -Maximum 200
        }
    }
    
    # Calculate derived metrics
    $metrics.memory.available = $metrics.memory.total - $metrics.memory.used
    $metrics.memory.usage_percent = [math]::Round(($metrics.memory.used / $metrics.memory.total) * 100, 2)
    $metrics.disk.free = $metrics.disk.total - $metrics.disk.used
    $metrics.disk.usage_percent = [math]::Round(($metrics.disk.used / $metrics.disk.total) * 100, 2)
    
    return $metrics
}

function Get-ApplicationMetrics {
    Write-Host "Collecting Application Metrics..." -ForegroundColor Yellow
    
    $appMetrics = @{
        java = @{
            heap_used = Get-Random -Minimum 200 -Maximum 800
            heap_max = 1024
            heap_usage_percent = 0
            gc_count = Get-Random -Minimum 10 -Maximum 100
            gc_time = Get-Random -Minimum 50 -Maximum 500
            threads_active = Get-Random -Minimum 20 -Maximum 80
            threads_peak = Get-Random -Minimum 50 -Maximum 120
        }
        database = @{
            connections_active = Get-Random -Minimum 5 -Maximum 25
            connections_max = 50
            connections_usage_percent = 0
            queries_per_second = Get-Random -Minimum 10 -Maximum 100
            slow_queries = Get-Random -Minimum 0 -Maximum 5
            cache_hit_ratio = Get-Random -Minimum 85 -Maximum 99
            lock_waits = Get-Random -Minimum 0 -Maximum 10
        }
        cache = @{
            redis_memory_used = Get-Random -Minimum 50 -Maximum 400
            redis_memory_max = 512
            redis_usage_percent = 0
            redis_hit_ratio = Get-Random -Minimum 90 -Maximum 99
            redis_ops_per_sec = Get-Random -Minimum 50 -Maximum 300
            redis_connected_clients = Get-Random -Minimum 10 -Maximum 50
        }
        web_server = @{
            active_requests = Get-Random -Minimum 5 -Maximum 50
            requests_per_second = Get-Random -Minimum 20 -Maximum 150
            avg_response_time = Get-Random -Minimum 100 -Maximum 2000
            error_rate = Get-Random -Minimum 0 -Maximum 5
            status_2xx = Get-Random -Minimum 90 -Maximum 99
            status_4xx = Get-Random -Minimum 1 -Maximum 5
            status_5xx = Get-Random -Minimum 0 -Maximum 3
        }
    }
    
    # Calculate derived metrics
    $appMetrics.java.heap_usage_percent = [math]::Round(($appMetrics.java.heap_used / $appMetrics.java.heap_max) * 100, 2)
    $appMetrics.database.connections_usage_percent = [math]::Round(($appMetrics.database.connections_active / $appMetrics.database.connections_max) * 100, 2)
    $appMetrics.cache.redis_usage_percent = [math]::Round(($appMetrics.cache.redis_memory_used / $appMetrics.cache.redis_memory_max) * 100, 2)
    
    return $appMetrics
}

function Get-BusinessMetrics {
    Write-Host "Collecting Business Metrics..." -ForegroundColor Yellow
    
    $businessMetrics = @{
        orders = @{
            total_today = Get-Random -Minimum 50 -Maximum 500
            completed_today = Get-Random -Minimum 45 -Maximum 450
            cancelled_today = Get-Random -Minimum 2 -Maximum 20
            pending_today = Get-Random -Minimum 3 -Maximum 30
            completion_rate = 0
            avg_order_value = Get-Random -Minimum 15 -Maximum 75
        }
        users = @{
            active_sessions = Get-Random -Minimum 20 -Maximum 200
            new_registrations_today = Get-Random -Minimum 5 -Maximum 50
            login_success_rate = Get-Random -Minimum 95 -Maximum 99
            session_duration_avg = Get-Random -Minimum 300 -Maximum 1800
        }
        restaurants = @{
            active_restaurants = Get-Random -Minimum 15 -Maximum 50
            avg_preparation_time = Get-Random -Minimum 15 -Maximum 45
            restaurant_rating_avg = Get-Random -Minimum 4.0 -Maximum 4.9
        }
        revenue = @{
            revenue_today = Get-Random -Minimum 1000 -Maximum 10000
            revenue_this_month = Get-Random -Minimum 15000 -Maximum 150000
            commission_earned = Get-Random -Minimum 150 -Maximum 1500
        }
    }
    
    # Calculate derived metrics
    $businessMetrics.orders.completion_rate = [math]::Round(($businessMetrics.orders.completed_today / $businessMetrics.orders.total_today) * 100, 2)
    
    return $businessMetrics
}

function Get-PerformanceAlerts {
    param($systemMetrics, $appMetrics, $businessMetrics)
    
    $alerts = @()
    
    # System alerts
    if ($systemMetrics.cpu.usage -gt 80) {
        $alerts += @{ level = "HIGH"; type = "SYSTEM"; message = "High CPU usage: $($systemMetrics.cpu.usage)%" }
    }
    if ($systemMetrics.memory.usage_percent -gt 85) {
        $alerts += @{ level = "HIGH"; type = "SYSTEM"; message = "High memory usage: $($systemMetrics.memory.usage_percent)%" }
    }
    if ($systemMetrics.disk.usage_percent -gt 90) {
        $alerts += @{ level = "CRITICAL"; type = "SYSTEM"; message = "Disk space critical: $($systemMetrics.disk.usage_percent)%" }
    }
    
    # Application alerts
    if ($appMetrics.java.heap_usage_percent -gt 85) {
        $alerts += @{ level = "HIGH"; type = "APPLICATION"; message = "High JVM heap usage: $($appMetrics.java.heap_usage_percent)%" }
    }
    if ($appMetrics.database.connections_usage_percent -gt 80) {
        $alerts += @{ level = "MEDIUM"; type = "DATABASE"; message = "High DB connection usage: $($appMetrics.database.connections_usage_percent)%" }
    }
    if ($appMetrics.web_server.avg_response_time -gt 1500) {
        $alerts += @{ level = "MEDIUM"; type = "PERFORMANCE"; message = "Slow response time: $($appMetrics.web_server.avg_response_time)ms" }
    }
    if ($appMetrics.web_server.error_rate -gt 3) {
        $alerts += @{ level = "HIGH"; type = "APPLICATION"; message = "High error rate: $($appMetrics.web_server.error_rate)%" }
    }
    
    # Business alerts
    if ($businessMetrics.orders.completion_rate -lt 90) {
        $alerts += @{ level = "MEDIUM"; type = "BUSINESS"; message = "Low order completion rate: $($businessMetrics.orders.completion_rate)%" }
    }
    if ($businessMetrics.users.login_success_rate -lt 95) {
        $alerts += @{ level = "MEDIUM"; type = "BUSINESS"; message = "Low login success rate: $($businessMetrics.users.login_success_rate)%" }
    }
    
    return $alerts
}

function Generate-DashboardHTML {
    param($systemMetrics, $appMetrics, $businessMetrics, $alerts)
    
    Write-Host "Generating Performance Dashboard HTML..." -ForegroundColor Yellow
    
    $dashboardDir = "reports/dashboard"
    if (-not (Test-Path $dashboardDir)) {
        New-Item -ItemType Directory -Path $dashboardDir -Force | Out-Null
    }
    
    $dashboardFile = "$dashboardDir/performance-dashboard-$Environment-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"
    
    $htmlContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>Food Ordering System - Performance Dashboard</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .dashboard { 
            max-width: 1400px; 
            margin: 0 auto; 
            background: white; 
            border-radius: 15px; 
            box-shadow: 0 10px 30px rgba(0,0,0,0.3);
            overflow: hidden;
        }
        .header { 
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%); 
            color: white; 
            padding: 30px; 
            text-align: center; 
        }
        .header h1 { font-size: 2.5em; margin-bottom: 10px; }
        .header .subtitle { font-size: 1.2em; opacity: 0.9; }
        .header .timestamp { font-size: 0.9em; opacity: 0.7; margin-top: 10px; }
        
        .content { padding: 30px; }
        .section { margin-bottom: 40px; }
        .section-title { 
            font-size: 1.8em; 
            color: #2c3e50; 
            margin-bottom: 20px; 
            border-bottom: 3px solid #3498db; 
            padding-bottom: 10px; 
        }
        
        .metrics-grid { 
            display: grid; 
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); 
            gap: 20px; 
            margin-bottom: 30px; 
        }
        .metric-card { 
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); 
            border-radius: 10px; 
            padding: 25px; 
            border-left: 5px solid #3498db; 
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            transition: transform 0.3s ease;
        }
        .metric-card:hover { transform: translateY(-5px); }
        .metric-title { 
            font-size: 1.1em; 
            color: #495057; 
            margin-bottom: 15px; 
            font-weight: 600; 
        }
        .metric-value { 
            font-size: 2.2em; 
            font-weight: bold; 
            margin-bottom: 5px; 
        }
        .metric-unit { 
            font-size: 0.9em; 
            color: #6c757d; 
            margin-left: 5px; 
        }
        .metric-status { 
            font-size: 0.9em; 
            font-weight: 600; 
            margin-top: 8px; 
        }
        
        .good { color: #27ae60; }
        .warning { color: #f39c12; }
        .critical { color: #e74c3c; }
        
        .progress-bar { 
            width: 100%; 
            height: 10px; 
            background-color: #ecf0f1; 
            border-radius: 5px; 
            overflow: hidden; 
            margin-top: 10px; 
        }
        .progress-fill { 
            height: 100%; 
            border-radius: 5px; 
            transition: width 0.3s ease; 
        }
        
        .alerts-section { 
            background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%); 
            border-radius: 10px; 
            padding: 25px; 
            margin-bottom: 30px; 
            border-left: 5px solid #e53e3e; 
        }
        .alert-item { 
            background: white; 
            padding: 15px; 
            margin: 10px 0; 
            border-radius: 8px; 
            border-left: 4px solid #e53e3e; 
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .alert-level { 
            font-weight: bold; 
            margin-right: 10px; 
        }
        .alert-type { 
            background: #f1f3f4; 
            padding: 2px 8px; 
            border-radius: 4px; 
            font-size: 0.8em; 
            margin-right: 10px; 
        }
        
        .charts-section { 
            display: grid; 
            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); 
            gap: 30px; 
        }
        .chart-container { 
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); 
            border-radius: 10px; 
            padding: 25px; 
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }
        .chart-title { 
            font-size: 1.3em; 
            color: #2c3e50; 
            margin-bottom: 20px; 
            text-align: center; 
        }
        
        .summary-stats { 
            display: grid; 
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); 
            gap: 15px; 
            margin-top: 30px; 
        }
        .summary-card { 
            background: linear-gradient(135deg, #3498db 0%, #2980b9 100%); 
            color: white; 
            padding: 20px; 
            border-radius: 10px; 
            text-align: center; 
            box-shadow: 0 5px 15px rgba(52, 152, 219, 0.3);
        }
        .summary-number { 
            font-size: 2em; 
            font-weight: bold; 
            margin-bottom: 5px; 
        }
        .summary-label { 
            font-size: 0.9em; 
            opacity: 0.9; 
        }
        
        .refresh-info { 
            text-align: center; 
            padding: 20px; 
            background: #f8f9fa; 
            color: #6c757d; 
            border-radius: 0 0 15px 15px; 
        }
        
        @media (max-width: 768px) {
            .metrics-grid, .charts-section, .summary-stats { 
                grid-template-columns: 1fr; 
            }
            .header h1 { font-size: 2em; }
            .content { padding: 20px; }
        }
    </style>
</head>
<body>
    <div class="dashboard">
        <div class="header">
            <h1>🚀 Food Ordering System</h1>
            <div class="subtitle">Performance Dashboard - $Environment Environment</div>
            <div class="timestamp">Last Updated: $timestamp</div>
        </div>
        
        <div class="content">
"@

    # Add alerts section if there are any alerts
    if ($alerts.Count -gt 0) {
        $htmlContent += @"
            <div class="section">
                <div class="section-title">🚨 Active Alerts ($($alerts.Count))</div>
                <div class="alerts-section">
"@
        foreach ($alert in $alerts) {
            $levelColor = switch ($alert.level) {
                "CRITICAL" { "#e53e3e" }
                "HIGH" { "#dd6b20" }
                "MEDIUM" { "#d69e2e" }
                "LOW" { "#38a169" }
            }
            $htmlContent += @"
                    <div class="alert-item">
                        <span class="alert-level" style="color: $levelColor;">$($alert.level)</span>
                        <span class="alert-type">$($alert.type)</span>
                        $($alert.message)
                    </div>
"@
        }
        $htmlContent += @"
                </div>
            </div>
"@
    }

    # System metrics section
    $htmlContent += @"
            <div class="section">
                <div class="section-title">💻 System Performance</div>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-title">CPU Usage</div>
                        <div class="metric-value $(if ($systemMetrics.cpu.usage -gt 80) { 'critical' } elseif ($systemMetrics.cpu.usage -gt 60) { 'warning' } else { 'good' })">$($systemMetrics.cpu.usage)<span class="metric-unit">%</span></div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: $($systemMetrics.cpu.usage)%; background-color: $(if ($systemMetrics.cpu.usage -gt 80) { '#e74c3c' } elseif ($systemMetrics.cpu.usage -gt 60) { '#f39c12' } else { '#27ae60' });"></div>
                        </div>
                        <div class="metric-status $(if ($systemMetrics.cpu.usage -gt 80) { 'critical' } elseif ($systemMetrics.cpu.usage -gt 60) { 'warning' } else { 'good' })">$($systemMetrics.cpu.cores) cores @ $($systemMetrics.cpu.frequency)</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Memory Usage</div>
                        <div class="metric-value $(if ($systemMetrics.memory.usage_percent -gt 85) { 'critical' } elseif ($systemMetrics.memory.usage_percent -gt 70) { 'warning' } else { 'good' })">$($systemMetrics.memory.usage_percent)<span class="metric-unit">%</span></div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: $($systemMetrics.memory.usage_percent)%; background-color: $(if ($systemMetrics.memory.usage_percent -gt 85) { '#e74c3c' } elseif ($systemMetrics.memory.usage_percent -gt 70) { '#f39c12' } else { '#27ae60' });"></div>
                        </div>
                        <div class="metric-status $(if ($systemMetrics.memory.usage_percent -gt 85) { 'critical' } elseif ($systemMetrics.memory.usage_percent -gt 70) { 'warning' } else { 'good' })">$($systemMetrics.memory.used)GB / $($systemMetrics.memory.total)GB</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Disk Usage</div>
                        <div class="metric-value $(if ($systemMetrics.disk.usage_percent -gt 90) { 'critical' } elseif ($systemMetrics.disk.usage_percent -gt 75) { 'warning' } else { 'good' })">$($systemMetrics.disk.usage_percent)<span class="metric-unit">%</span></div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: $($systemMetrics.disk.usage_percent)%; background-color: $(if ($systemMetrics.disk.usage_percent -gt 90) { '#e74c3c' } elseif ($systemMetrics.disk.usage_percent -gt 75) { '#f39c12' } else { '#27ae60' });"></div>
                        </div>
                        <div class="metric-status $(if ($systemMetrics.disk.usage_percent -gt 90) { 'critical' } elseif ($systemMetrics.disk.usage_percent -gt 75) { 'warning' } else { 'good' })">$($systemMetrics.disk.used)GB / $($systemMetrics.disk.total)GB</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Network Activity</div>
                        <div class="metric-value good">$($systemMetrics.network.connections)<span class="metric-unit">connections</span></div>
                        <div class="metric-status good">In: $([math]::Round($systemMetrics.network.bytes_in / 1024 / 1024, 2))MB | Out: $([math]::Round($systemMetrics.network.bytes_out / 1024 / 1024, 2))MB</div>
                    </div>
                </div>
            </div>
            
            <div class="section">
                <div class="section-title">☕ Application Performance</div>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-title">JVM Heap Usage</div>
                        <div class="metric-value $(if ($appMetrics.java.heap_usage_percent -gt 85) { 'critical' } elseif ($appMetrics.java.heap_usage_percent -gt 70) { 'warning' } else { 'good' })">$($appMetrics.java.heap_usage_percent)<span class="metric-unit">%</span></div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: $($appMetrics.java.heap_usage_percent)%; background-color: $(if ($appMetrics.java.heap_usage_percent -gt 85) { '#e74c3c' } elseif ($appMetrics.java.heap_usage_percent -gt 70) { '#f39c12' } else { '#27ae60' });"></div>
                        </div>
                        <div class="metric-status $(if ($appMetrics.java.heap_usage_percent -gt 85) { 'critical' } elseif ($appMetrics.java.heap_usage_percent -gt 70) { 'warning' } else { 'good' })">$($appMetrics.java.heap_used)MB / $($appMetrics.java.heap_max)MB</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Database Connections</div>
                        <div class="metric-value $(if ($appMetrics.database.connections_usage_percent -gt 80) { 'critical' } elseif ($appMetrics.database.connections_usage_percent -gt 60) { 'warning' } else { 'good' })">$($appMetrics.database.connections_usage_percent)<span class="metric-unit">%</span></div>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: $($appMetrics.database.connections_usage_percent)%; background-color: $(if ($appMetrics.database.connections_usage_percent -gt 80) { '#e74c3c' } elseif ($appMetrics.database.connections_usage_percent -gt 60) { '#f39c12' } else { '#27ae60' });"></div>
                        </div>
                        <div class="metric-status $(if ($appMetrics.database.connections_usage_percent -gt 80) { 'critical' } elseif ($appMetrics.database.connections_usage_percent -gt 60) { 'warning' } else { 'good' })">$($appMetrics.database.connections_active) / $($appMetrics.database.connections_max) active</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Redis Cache</div>
                        <div class="metric-value $(if ($appMetrics.cache.redis_hit_ratio -lt 90) { 'warning' } else { 'good' })">$($appMetrics.cache.redis_hit_ratio)<span class="metric-unit">%</span></div>
                        <div class="metric-status $(if ($appMetrics.cache.redis_hit_ratio -lt 90) { 'warning' } else { 'good' })">Hit Ratio | $($appMetrics.cache.redis_ops_per_sec) ops/sec</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Web Server</div>
                        <div class="metric-value $(if ($appMetrics.web_server.avg_response_time -gt 1500) { 'critical' } elseif ($appMetrics.web_server.avg_response_time -gt 1000) { 'warning' } else { 'good' })">$($appMetrics.web_server.avg_response_time)<span class="metric-unit">ms</span></div>
                        <div class="metric-status $(if ($appMetrics.web_server.avg_response_time -gt 1500) { 'critical' } elseif ($appMetrics.web_server.avg_response_time -gt 1000) { 'warning' } else { 'good' })">Avg Response Time | $($appMetrics.web_server.requests_per_second) req/sec</div>
                    </div>
                </div>
            </div>
            
            <div class="section">
                <div class="section-title">📊 Business Metrics</div>
                <div class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-title">Orders Today</div>
                        <div class="metric-value good">$($businessMetrics.orders.total_today)</div>
                        <div class="metric-status $(if ($businessMetrics.orders.completion_rate -lt 90) { 'warning' } else { 'good' })">Completion Rate: $($businessMetrics.orders.completion_rate)%</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Active Users</div>
                        <div class="metric-value good">$($businessMetrics.users.active_sessions)</div>
                        <div class="metric-status good">Sessions | $($businessMetrics.users.new_registrations_today) new registrations</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Revenue Today</div>
                        <div class="metric-value good">$($businessMetrics.revenue.revenue_today)<span class="metric-unit">USD</span></div>
                        <div class="metric-status good">Avg Order: $($businessMetrics.orders.avg_order_value) USD</div>
                    </div>
                    
                    <div class="metric-card">
                        <div class="metric-title">Restaurant Performance</div>
                        <div class="metric-value good">$($businessMetrics.restaurants.restaurant_rating_avg)<span class="metric-unit">/5</span></div>
                        <div class="metric-status good">Avg Rating | $($businessMetrics.restaurants.avg_preparation_time)min prep time</div>
                    </div>
                </div>
            </div>
            
            <div class="section">
                <div class="section-title">📈 Summary Statistics</div>
                <div class="summary-stats">
                    <div class="summary-card">
                        <div class="summary-number">$(if ($systemMetrics.cpu.usage -lt 70 -and $systemMetrics.memory.usage_percent -lt 80) { 'EXCELLENT' } elseif ($systemMetrics.cpu.usage -lt 80 -and $systemMetrics.memory.usage_percent -lt 85) { 'GOOD' } else { 'NEEDS ATTENTION' })</div>
                        <div class="summary-label">System Health</div>
                    </div>
                    <div class="summary-card">
                        <div class="summary-number">$(if ($appMetrics.web_server.avg_response_time -lt 1000) { 'FAST' } elseif ($appMetrics.web_server.avg_response_time -lt 2000) { 'NORMAL' } else { 'SLOW' })</div>
                        <div class="summary-label">Response Speed</div>
                    </div>
                    <div class="summary-card">
                        <div class="summary-number">$($businessMetrics.orders.completion_rate)</div>
                        <div class="summary-label">Order Success %</div>
                    </div>
                    <div class="summary-card">
                        <div class="summary-number">$($alerts.Count)</div>
                        <div class="summary-label">Active Alerts</div>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="refresh-info">
            Dashboard refreshes every $RefreshInterval seconds | Generated at $timestamp
        </div>
    </div>
</body>
</html>
"@
    
    $htmlContent | Out-File -FilePath $dashboardFile -Encoding UTF8
    
    Write-Host "Performance dashboard generated: $dashboardFile" -ForegroundColor Green
    
    return $dashboardFile
}

function Start-RealTimeMonitoring {
    param($refreshInterval)
    
    Write-Host "Starting Real-time Performance Monitoring..." -ForegroundColor Yellow
    Write-Host "Refresh Interval: $refreshInterval seconds" -ForegroundColor Gray
    Write-Host "Press Ctrl+C to stop monitoring" -ForegroundColor Gray
    Write-Host "=" * 50
    
    $monitoringCount = 0
    while ($monitoringCount -lt 10) {
        $currentTime = Get-Date -Format "HH:mm:ss"
        Write-Host "`n[$currentTime] Collecting Performance Data..." -ForegroundColor Cyan
        
        $systemMetrics = Get-SystemMetrics
        $appMetrics = Get-ApplicationMetrics
        $businessMetrics = Get-BusinessMetrics
        $alerts = Get-PerformanceAlerts -systemMetrics $systemMetrics -appMetrics $appMetrics -businessMetrics $businessMetrics
        
        # Display key metrics
        Write-Host "System Health:" -ForegroundColor Yellow
        Write-Host "  CPU: $($systemMetrics.cpu.usage)% | Memory: $($systemMetrics.memory.usage_percent)% | Disk: $($systemMetrics.disk.usage_percent)%" -ForegroundColor White
        
        Write-Host "Application Health:" -ForegroundColor Yellow
        Write-Host "  JVM Heap: $($appMetrics.java.heap_usage_percent)% | DB Connections: $($appMetrics.database.connections_usage_percent)% | Response Time: $($appMetrics.web_server.avg_response_time)ms" -ForegroundColor White
        
        Write-Host "Business Metrics:" -ForegroundColor Yellow
        Write-Host "  Orders Today: $($businessMetrics.orders.total_today) | Active Users: $($businessMetrics.users.active_sessions) | Revenue: $($businessMetrics.revenue.revenue_today) USD" -ForegroundColor White
        
        if ($alerts.Count -gt 0) {
            Write-Host "Active Alerts: $($alerts.Count)" -ForegroundColor Red
            foreach ($alert in $alerts) {
                Write-Host "  $($alert.level): $($alert.message)" -ForegroundColor Red
            }
        } else {
            Write-Host "No Active Alerts" -ForegroundColor Green
        }
        
        Start-Sleep -Seconds $refreshInterval
        $monitoringCount++
    }
    
    Write-Host "`nReal-time monitoring completed!" -ForegroundColor Green
}

# Main execution
try {
    switch ($Action) {
        "generate" {
            $systemMetrics = Get-SystemMetrics
            $appMetrics = Get-ApplicationMetrics
            $businessMetrics = Get-BusinessMetrics
            $alerts = Get-PerformanceAlerts -systemMetrics $systemMetrics -appMetrics $appMetrics -businessMetrics $businessMetrics
            
            $dashboardFile = Generate-DashboardHTML -systemMetrics $systemMetrics -appMetrics $appMetrics -businessMetrics $businessMetrics -alerts $alerts
            
            Write-Host "`nDashboard Summary:" -ForegroundColor Yellow
            Write-Host "  System Health: $(if ($systemMetrics.cpu.usage -lt 70 -and $systemMetrics.memory.usage_percent -lt 80) { 'EXCELLENT' } else { 'NEEDS ATTENTION' })" -ForegroundColor $(if ($systemMetrics.cpu.usage -lt 70 -and $systemMetrics.memory.usage_percent -lt 80) { 'Green' } else { 'Yellow' })
            Write-Host "  Application Performance: $(if ($appMetrics.web_server.avg_response_time -lt 1000) { 'EXCELLENT' } else { 'GOOD' })" -ForegroundColor Green
            Write-Host "  Active Alerts: $($alerts.Count)" -ForegroundColor $(if ($alerts.Count -eq 0) { 'Green' } else { 'Red' })
            Write-Host "  Dashboard File: $dashboardFile" -ForegroundColor White
        }
        "monitor" {
            if ($RealTime) {
                Start-RealTimeMonitoring -refreshInterval $RefreshInterval
            } else {
                Write-Host "Use -RealTime switch for continuous monitoring" -ForegroundColor Yellow
            }
        }
        default {
            Write-Host "Unknown action: $Action" -ForegroundColor Red
            Write-Host "Valid actions: generate, monitor" -ForegroundColor Yellow
            exit 1
        }
    }
    
    Write-Host "`nPerformance dashboard operation completed!" -ForegroundColor Green
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "=" * 70 
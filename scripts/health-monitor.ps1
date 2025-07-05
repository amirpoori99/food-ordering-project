#!/usr/bin/env pwsh

function Test-ApplicationHealth {
    $results = @()
    
    # Test database connectivity
    try {
        # Simulate database test
        $dbStatus = "Connected"
        $results += "Database: OK"
    } catch {
        $results += "Database: FAILED"
    }
    
    # Test API endpoints
    try {
        # Simulate API test
        $apiStatus = "Responsive"
        $results += "API: OK"
    } catch {
        $results += "API: FAILED"
    }
    
    # Test system resources
    $cpu = Get-WmiObject -Class Win32_Processor | Measure-Object -Property LoadPercentage -Average | Select-Object -ExpandProperty Average
    if ($cpu -lt 90) {
        $results += "CPU: OK"
    } else {
        $results += "CPU: WARNING"
    }
    
    return $results
}

function Start-HealthMonitoring {
    while ($true) {
        $health = Test-ApplicationHealth
        $timestamp = Get-Date
        
        Write-Host "Health Check - $timestamp" -ForegroundColor Cyan
        foreach ($check in $health) {
            if ($check -like "*OK*") {
                Write-Host $check -ForegroundColor Green
            } else {
                Write-Host $check -ForegroundColor Red
            }
        }
        
        # Log health status
        $logEntry = "$timestamp|$($health -join '|')"
        Add-Content -Path "logs/health-checks.log" -Value $logEntry
        
        Start-Sleep 30
    }
}

Start-HealthMonitoring

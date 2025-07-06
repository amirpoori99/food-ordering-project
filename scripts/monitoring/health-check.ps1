# Health Check Script for Monitoring Stack
param([string]$Service = "all")

function Test-ServiceHealth {
    param([string]$ServiceName, [string]$Url, [int]$Port)
    
    try {
        $response = Invoke-WebRequest -Uri $Url -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        Write-Host "$ServiceName: ✅ Healthy (Status: $($response.StatusCode))" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "$ServiceName: ❌ Unhealthy (Error: $($_.Exception.Message))" -ForegroundColor Red
        return $false
    }
}

Write-Host "Food Ordering System - Monitoring Health Check" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

$services = @{
    "prometheus" = "http://localhost:9090"
    "grafana" = "http://localhost:3000"
    "nodeexporter" = "http://localhost:9100"
}

$allHealthy = $true

if ($Service -eq "all") {
    foreach ($svc in $services.GetEnumerator()) {
        $healthy = Test-ServiceHealth -ServiceName $svc.Key -Url $svc.Value
        if (-not $healthy) { $allHealthy = $false }
    }
} else {
    if ($services.ContainsKey($Service)) {
        $allHealthy = Test-ServiceHealth -ServiceName $Service -Url $services[$Service]
    } else {
        Write-Host "Unknown service: $Service" -ForegroundColor Red
        $allHealthy = $false
    }
}

Write-Host ""
if ($allHealthy) {
    Write-Host "Overall Status: ✅ All services healthy" -ForegroundColor Green
} else {
    Write-Host "Overall Status: ❌ Some services are unhealthy" -ForegroundColor Red
}

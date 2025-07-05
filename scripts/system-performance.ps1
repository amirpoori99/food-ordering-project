param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment
)

Write-Host "Food Ordering System - Performance Analysis" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor Yellow

# System Performance Check
Write-Host "`nSystem Performance:" -ForegroundColor Cyan

# CPU Usage
try {
    $cpu = Get-Counter "\Processor(_Total)\% Processor Time" -SampleInterval 1 -MaxSamples 1
    $cpuValue = [math]::Round($cpu.CounterSamples[0].CookedValue, 2)
    Write-Host "CPU Usage: $cpuValue%" -ForegroundColor White
} catch {
    Write-Host "CPU Usage: Unable to measure" -ForegroundColor Yellow
}

# Memory Usage
try {
    $memory = Get-CimInstance -ClassName Win32_OperatingSystem
    $memoryUsed = [math]::Round(($memory.TotalVisibleMemorySize - $memory.FreePhysicalMemory) / 1MB, 2)
    $memoryTotal = [math]::Round($memory.TotalVisibleMemorySize / 1MB, 2)
    $memoryPercent = [math]::Round(($memoryUsed / $memoryTotal) * 100, 2)
    Write-Host "Memory Usage: $memoryUsed GB / $memoryTotal GB ($memoryPercent%)" -ForegroundColor White
} catch {
    Write-Host "Memory Usage: Unable to measure" -ForegroundColor Yellow
}

# Disk Usage
try {
    $disk = Get-CimInstance -ClassName Win32_LogicalDisk | Where-Object { $_.DeviceID -eq "C:" }
    if ($disk) {
        $diskUsed = [math]::Round(($disk.Size - $disk.FreeSpace) / 1GB, 2)
        $diskTotal = [math]::Round($disk.Size / 1GB, 2)
        $diskPercent = [math]::Round(($diskUsed / $diskTotal) * 100, 2)
        Write-Host "Disk Usage: $diskUsed GB / $diskTotal GB ($diskPercent%)" -ForegroundColor White
    }
} catch {
    Write-Host "Disk Usage: Unable to measure" -ForegroundColor Yellow
}

# Java Processes
Write-Host "`nJava Processes:" -ForegroundColor Cyan
try {
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        foreach ($process in $javaProcesses) {
            $memoryMB = [math]::Round($process.WorkingSet64 / 1MB, 2)
            Write-Host "Process $($process.Id): $memoryMB MB" -ForegroundColor White
        }
    } else {
        Write-Host "No Java processes running" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Unable to check Java processes" -ForegroundColor Yellow
}

# Database Check
Write-Host "`nDatabase Status:" -ForegroundColor Cyan
$dbFile = "backend/food_ordering.db"
if (Test-Path $dbFile) {
    $dbSize = [math]::Round((Get-Item $dbFile).Length / 1MB, 2)
    Write-Host "Database Size: $dbSize MB" -ForegroundColor White
} else {
    Write-Host "Database not found" -ForegroundColor Yellow
}

# Performance Recommendations
Write-Host "`nRecommendations:" -ForegroundColor Green

if ($cpuValue -gt 80) {
    Write-Host "- High CPU usage detected. Consider optimizing CPU-intensive operations." -ForegroundColor Yellow
}

if ($memoryPercent -gt 85) {
    Write-Host "- High memory usage detected. Consider memory optimization." -ForegroundColor Yellow
}

if ($diskPercent -gt 90) {
    Write-Host "- High disk usage detected. Consider cleaning up disk space." -ForegroundColor Red
}

if (-not $javaProcesses) {
    Write-Host "- No Java processes running. Start the application for accurate analysis." -ForegroundColor Cyan
}

Write-Host "`nPerformance check completed." -ForegroundColor Green 
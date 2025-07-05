# Performance Check Script for Food Ordering System

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [int]$SampleCount = 10
)

Write-Host "Food Ordering System - Performance Check" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Collecting $SampleCount samples..." -ForegroundColor Cyan

# Initialize metrics
$metrics = @{
    CPU = @()
    Memory = @()
    Disk = @()
}

# Collect performance samples
for ($i = 1; $i -le $SampleCount; $i++) {
    Write-Host "Sample $i/$SampleCount" -ForegroundColor Gray
    
    # CPU Usage
    try {
        $cpu = Get-Counter "\Processor(_Total)\% Processor Time" -SampleInterval 1 -MaxSamples 1
        $cpuValue = [math]::Round($cpu.CounterSamples[0].CookedValue, 2)
        $metrics.CPU += $cpuValue
        Write-Host "  CPU: $cpuValue%" -ForegroundColor White
    } catch {
        Write-Host "  CPU: Unable to measure" -ForegroundColor Yellow
    }
    
    # Memory Usage
    try {
        $memory = Get-CimInstance -ClassName Win32_OperatingSystem
        $memoryUsed = [math]::Round(($memory.TotalVisibleMemorySize - $memory.FreePhysicalMemory) / 1MB, 2)
        $memoryTotal = [math]::Round($memory.TotalVisibleMemorySize / 1MB, 2)
        $memoryPercent = [math]::Round(($memoryUsed / $memoryTotal) * 100, 2)
        $metrics.Memory += $memoryPercent
        Write-Host "  Memory: $memoryUsed GB / $memoryTotal GB ($memoryPercent%)" -ForegroundColor White
    } catch {
        Write-Host "  Memory: Unable to measure" -ForegroundColor Yellow
    }
    
    # Disk Usage (C: drive)
    try {
        $disk = Get-CimInstance -ClassName Win32_LogicalDisk | Where-Object { $_.DeviceID -eq "C:" }
        if ($disk) {
            $diskUsed = [math]::Round(($disk.Size - $disk.FreeSpace) / 1GB, 2)
            $diskTotal = [math]::Round($disk.Size / 1GB, 2)
            $diskPercent = [math]::Round(($diskUsed / $diskTotal) * 100, 2)
            $metrics.Disk += $diskPercent
            Write-Host "  Disk C:: $diskUsed GB / $diskTotal GB ($diskPercent%)" -ForegroundColor White
        }
    } catch {
        Write-Host "  Disk: Unable to measure" -ForegroundColor Yellow
    }
    
    # Java Processes
    try {
        $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
        if ($javaProcesses) {
            foreach ($process in $javaProcesses) {
                $memoryMB = [math]::Round($process.WorkingSet64 / 1MB, 2)
                Write-Host "  Java Process $($process.Id): $memoryMB MB" -ForegroundColor Cyan
            }
        } else {
            Write-Host "  Java: No processes running" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  Java: Unable to check processes" -ForegroundColor Yellow
    }
    
    if ($i -lt $SampleCount) {
        Start-Sleep -Seconds 2
    }
}

# Calculate averages and display results
Write-Host "`nPerformance Summary:" -ForegroundColor Green
Write-Host "===================" -ForegroundColor Green

if ($metrics.CPU.Count -gt 0) {
    $avgCPU = [math]::Round(($metrics.CPU | Measure-Object -Average).Average, 2)
    $maxCPU = [math]::Round(($metrics.CPU | Measure-Object -Maximum).Maximum, 2)
    Write-Host "CPU Usage - Average: $avgCPU%, Peak: $maxCPU%" -ForegroundColor $(if ($avgCPU -gt 80) { "Red" } elseif ($avgCPU -gt 60) { "Yellow" } else { "Green" })
}

if ($metrics.Memory.Count -gt 0) {
    $avgMemory = [math]::Round(($metrics.Memory | Measure-Object -Average).Average, 2)
    $maxMemory = [math]::Round(($metrics.Memory | Measure-Object -Maximum).Maximum, 2)
    Write-Host "Memory Usage - Average: $avgMemory%, Peak: $maxMemory%" -ForegroundColor $(if ($avgMemory -gt 85) { "Red" } elseif ($avgMemory -gt 70) { "Yellow" } else { "Green" })
}

if ($metrics.Disk.Count -gt 0) {
    $avgDisk = [math]::Round(($metrics.Disk | Measure-Object -Average).Average, 2)
    Write-Host "Disk Usage - Average: $avgDisk%" -ForegroundColor $(if ($avgDisk -gt 90) { "Red" } elseif ($avgDisk -gt 80) { "Yellow" } else { "Green" })
}

# Performance recommendations
Write-Host "`nRecommendations:" -ForegroundColor Cyan

if ($metrics.CPU.Count -gt 0 -and ($metrics.CPU | Measure-Object -Average).Average -gt 80) {
    Write-Host "⚠️  High CPU usage detected. Consider optimizing CPU-intensive operations." -ForegroundColor Yellow
}

if ($metrics.Memory.Count -gt 0 -and ($metrics.Memory | Measure-Object -Average).Average -gt 85) {
    Write-Host "⚠️  High memory usage detected. Consider increasing system memory or optimizing memory usage." -ForegroundColor Yellow
}

if ($metrics.Disk.Count -gt 0 -and ($metrics.Disk | Measure-Object -Average).Average -gt 90) {
    Write-Host "⚠️  High disk usage detected. Consider cleaning up disk space." -ForegroundColor Red
}

$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if (-not $javaProcesses) {
    Write-Host "ℹ️  No Java processes running. Consider starting the application for more accurate analysis." -ForegroundColor Cyan
}

Write-Host "`nPerformance check completed!" -ForegroundColor Green
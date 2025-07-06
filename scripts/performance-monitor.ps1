#!/usr/bin/env pwsh

# Real-time Performance Monitoring
function Get-SystemMetrics {
    $cpu = Get-WmiObject -Class Win32_Processor | Measure-Object -Property LoadPercentage -Average | Select-Object -ExpandProperty Average
    $memory = Get-WmiObject -Class Win32_OperatingSystem
    $memoryUsed = [math]::Round(($memory.TotalVisibleMemorySize - $memory.FreePhysicalMemory) / $memory.TotalVisibleMemorySize * 100, 2)
    
    $disk = Get-WmiObject -Class Win32_LogicalDisk -Filter "DeviceID='C:'"
    $diskUsed = [math]::Round(($disk.Size - $disk.FreeSpace) / $disk.Size * 100, 2)
    
    return @{
        CPU_Percent = $cpu
        Memory_Percent = $memoryUsed
        Disk_Percent = $diskUsed
        Timestamp = Get-Date
    }
}

function Monitor-System {
    while ($true) {
        $metrics = Get-SystemMetrics
        
        Write-Host "System Metrics - $(Get-Date)" -ForegroundColor Cyan
        Write-Host "CPU: $($metrics.CPU_Percent)% | Memory: $($metrics.Memory_Percent)% | Disk: $($metrics.Disk_Percent)%" -ForegroundColor White
        
        # Alert thresholds
        if ($metrics.CPU_Percent -gt 80) {
            Write-Host "ALERT: High CPU usage detected: $($metrics.CPU_Percent)%" -ForegroundColor Red
        }
        
        if ($metrics.Memory_Percent -gt 85) {
            Write-Host "ALERT: High memory usage detected: $($metrics.Memory_Percent)%" -ForegroundColor Red
        }
        
        if ($metrics.Disk_Percent -gt 85) {
            Write-Host "ALERT: High disk usage detected: $($metrics.Disk_Percent)%" -ForegroundColor Red
        }
        
        # Log metrics
        $logEntry = "$(Get-Date)|CPU:$($metrics.CPU_Percent)|Memory:$($metrics.Memory_Percent)|Disk:$($metrics.Disk_Percent)"
        Add-Content -Path "logs/performance-metrics.log" -Value $logEntry
        
        Start-Sleep 10
    }
}

Monitor-System

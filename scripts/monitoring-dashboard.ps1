#!/usr/bin/env pwsh

function Show-Dashboard {
    Clear-Host
    Write-Host "=================================" -ForegroundColor Blue
    Write-Host "   FOOD ORDERING SYSTEM DASHBOARD" -ForegroundColor Blue
    Write-Host "=================================" -ForegroundColor Blue
    Write-Host ""
    
    # System Status
    $metrics = Get-SystemMetrics
    Write-Host "SYSTEM METRICS:" -ForegroundColor Yellow
    Write-Host "CPU Usage: $($metrics.CPU_Percent)%" -ForegroundColor $(if($metrics.CPU_Percent -gt 80) {"Red"} else {"Green"})
    Write-Host "Memory Usage: $($metrics.Memory_Percent)%" -ForegroundColor $(if($metrics.Memory_Percent -gt 85) {"Red"} else {"Green"})
    Write-Host "Disk Usage: $($metrics.Disk_Percent)%" -ForegroundColor $(if($metrics.Disk_Percent -gt 85) {"Red"} else {"Green"})
    Write-Host ""
    
    # Recent logs
    Write-Host "RECENT ACTIVITY:" -ForegroundColor Yellow
    if (Test-Path "logs/performance-metrics.log") {
        Get-Content "logs/performance-metrics.log" -Tail 3 | ForEach-Object {
            Write-Host $_ -ForegroundColor Gray
        }
    }
    
    Write-Host ""
    Write-Host "Last Updated: $(Get-Date)" -ForegroundColor Cyan
    Write-Host "Press Ctrl+C to exit" -ForegroundColor Gray
}

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
    }
}

while ($true) {
    Show-Dashboard
    Start-Sleep 30
}

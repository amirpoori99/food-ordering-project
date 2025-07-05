# Food Ordering System - Performance Analyzer
# Phase 46: Performance Optimization & Load Testing

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [int]$DurationMinutes = 5,
    
    [Parameter(Mandatory=$false)]
    [int]$SampleInterval = 10,
    
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport,
    
    [Parameter(Mandatory=$false)]
    [string]$OutputPath = "reports\performance"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile = "logs\performance-analysis-$Environment-$TimeStamp.log"

# Performance metrics storage
$PerformanceMetrics = @{
    System = @()
    JVM = @()
    Database = @()
    Application = @()
    Network = @()
}

# Create required directories
$directories = @("logs", "reports", "reports\performance")
foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -Path $dir -ItemType Directory -Force | Out-Null
    }
}

# Logging Function
function Write-Log {
    param($Message, $Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [$Level] $Message"
    
    Write-Host $logEntry -ForegroundColor $(
        switch ($Level) {
            "ERROR" { "Red" }
            "WARNING" { "Yellow" }
            "SUCCESS" { "Green" }
            "INFO" { "Cyan" }
            "METRIC" { "Magenta" }
            default { "White" }
        }
    )
    
    Add-Content -Path $LogFile -Value $logEntry
}

# Get Environment Configuration
function Get-EnvironmentConfig {
    param($Environment)
    
    $configFile = "config\deployment\$Environment.json"
    
    if (Test-Path $configFile) {
        $config = Get-Content $configFile | ConvertFrom-Json
        return $config
    } else {
        Write-Log "Configuration file not found: $configFile" "ERROR"
        throw "Configuration file not found"
    }
}

# Get System Performance Metrics
function Get-SystemMetrics {
    try {
        $cpu = Get-Counter "\Processor(_Total)\% Processor Time" -SampleInterval 1 -MaxSamples 3
        $memory = Get-CimInstance -ClassName Win32_OperatingSystem
        $disk = Get-CimInstance -ClassName Win32_LogicalDisk | Where-Object { $_.DriveType -eq 3 }
        
        $cpuAvg = ($cpu.CounterSamples | Measure-Object CookedValue -Average).Average
        $memoryUsedGB = [math]::Round(($memory.TotalVisibleMemorySize - $memory.FreePhysicalMemory) / 1MB, 2)
        $memoryTotalGB = [math]::Round($memory.TotalVisibleMemorySize / 1MB, 2)
        $memoryUsagePercent = [math]::Round(($memoryUsedGB / $memoryTotalGB) * 100, 2)
        
        $systemMetric = @{
            Timestamp = Get-Date
            CPU_Percentage = [math]::Round($cpuAvg, 2)
            Memory_Used_GB = $memoryUsedGB
            Memory_Total_GB = $memoryTotalGB
            Memory_Usage_Percentage = $memoryUsagePercent
            Disk_Usage = @()
        }
        
        foreach ($drive in $disk) {
            $diskUsed = [math]::Round(($drive.Size - $drive.FreeSpace) / 1GB, 2)
            $diskTotal = [math]::Round($drive.Size / 1GB, 2)
            $diskUsagePercent = if ($diskTotal -gt 0) { [math]::Round(($diskUsed / $diskTotal) * 100, 2) } else { 0 }
            
            $systemMetric.Disk_Usage += @{
                Drive = $drive.DeviceID
                Used_GB = $diskUsed
                Total_GB = $diskTotal
                Usage_Percentage = $diskUsagePercent
            }
        }
        
        return $systemMetric
        
    } catch {
        Write-Log "Error collecting system metrics: $($_.Exception.Message)" "ERROR"
        return $null
    }
}

# Get JVM Performance Metrics
function Get-JVMMetrics {
    param($Config)
    
    try {
        # Try to find Java processes
        $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
        
        if (-not $javaProcesses) {
            Write-Log "No Java processes found" "WARNING"
            return $null
        }
        
        $jvmMetrics = @()
        
        foreach ($process in $javaProcesses) {
            try {
                $processMetric = @{
                    Timestamp = Get-Date
                    PID = $process.Id
                    Memory_MB = [math]::Round($process.WorkingSet64 / 1MB, 2)
                    CPU_Time = $process.TotalProcessorTime.TotalMilliseconds
                    Thread_Count = $process.Threads.Count
                    Handle_Count = $process.HandleCount
                }
                
                # Try to get additional JVM info if jcmd is available
                try {
                    $jcmdOutput = & jcmd $process.Id VM.info 2>$null
                    if ($LASTEXITCODE -eq 0) {
                        $processMetric.JVM_Info = "Available"
                    }
                } catch {
                    $processMetric.JVM_Info = "Not available"
                }
                
                $jvmMetrics += $processMetric
                
            } catch {
                Write-Log "Error collecting metrics for Java process $($process.Id): $($_.Exception.Message)" "WARNING"
            }
        }
        
        return $jvmMetrics
        
    } catch {
        Write-Log "Error collecting JVM metrics: $($_.Exception.Message)" "ERROR"
        return $null
    }
}

# Get Database Performance Metrics
function Get-DatabaseMetrics {
    param($Config)
    
    try {
        $dbPath = "database\$Environment\food_ordering_$Environment.db"
        
        if (-not (Test-Path $dbPath)) {
            Write-Log "Database file not found: $dbPath" "WARNING"
            return $null
        }
        
        $dbFileInfo = Get-Item $dbPath
        $dbSizeMB = [math]::Round($dbFileInfo.Length / 1MB, 2)
        
        $dbMetric = @{
            Timestamp = Get-Date
            Database_Path = $dbPath
            Size_MB = $dbSizeMB
            Last_Modified = $dbFileInfo.LastWriteTime
            Connection_Test = "Unknown"
        }
        
        # Test database connection (simplified)
        try {
            if (Test-Path "backend\target\classes") {
                $dbMetric.Connection_Test = "Backend classes available"
            } else {
                $dbMetric.Connection_Test = "Backend not compiled"
            }
        } catch {
            $dbMetric.Connection_Test = "Connection test failed"
        }
        
        return $dbMetric
        
    } catch {
        Write-Log "Error collecting database metrics: $($_.Exception.Message)" "ERROR"
        return $null
    }
}

# Get Application Performance Metrics
function Get-ApplicationMetrics {
    param($Config)
    
    try {
        $backendPort = $Config.backend.port
        $frontendPort = $Config.frontend.port
        
        $appMetric = @{
            Timestamp = Get-Date
            Backend_Port = $backendPort
            Frontend_Port = $frontendPort
            Backend_Status = "Unknown"
            Frontend_Status = "Unknown"
            Response_Times = @()
        }
        
        # Test backend endpoint
        try {
            $backendUrl = "http://localhost:$backendPort"
            $response = Invoke-WebRequest -Uri $backendUrl -TimeoutSec 5 -ErrorAction SilentlyContinue
            $appMetric.Backend_Status = "Reachable (Status: $($response.StatusCode))"
        } catch {
            $appMetric.Backend_Status = "Not reachable"
        }
        
        # Test if frontend port is listening
        try {
            $frontendConnection = Test-NetConnection -ComputerName localhost -Port $frontendPort -InformationLevel Quiet -WarningAction SilentlyContinue
            $appMetric.Frontend_Status = if ($frontendConnection) { "Port listening" } else { "Port not listening" }
        } catch {
            $appMetric.Frontend_Status = "Connection test failed"
        }
        
        return $appMetric
        
    } catch {
        Write-Log "Error collecting application metrics: $($_.Exception.Message)" "ERROR"
        return $null
    }
}

# Get Network Performance Metrics
function Get-NetworkMetrics {
    try {
        $networkAdapters = Get-CimInstance -ClassName Win32_NetworkAdapter | Where-Object { $_.NetEnabled -eq $true }
        
        $networkMetric = @{
            Timestamp = Get-Date
            Active_Adapters = $networkAdapters.Count
            Adapter_Details = @()
        }
        
        foreach ($adapter in $networkAdapters) {
            $networkMetric.Adapter_Details += @{
                Name = $adapter.Name
                MAC_Address = $adapter.MACAddress
                Speed = $adapter.Speed
            }
        }
        
        # Test localhost connectivity
        try {
            $pingResult = Test-Connection -ComputerName localhost -Count 1 -Quiet
            $networkMetric.Localhost_Ping = if ($pingResult) { "Success" } else { "Failed" }
        } catch {
            $networkMetric.Localhost_Ping = "Failed"
        }
        
        return $networkMetric
        
    } catch {
        Write-Log "Error collecting network metrics: $($_.Exception.Message)" "ERROR"
        return $null
    }
}

# Analyze Performance Data
function Invoke-PerformanceAnalysis {
    Write-Log "Analyzing performance data..." "INFO"
    
    $analysis = @{
        Overall_Health = "Good"
        Bottlenecks = @()
        Recommendations = @()
        Critical_Issues = @()
        Warnings = @()
    }
    
    # Analyze system metrics
    if ($PerformanceMetrics.System.Count -gt 0) {
        $avgCPU = ($PerformanceMetrics.System | Measure-Object CPU_Percentage -Average).Average
        $avgMemory = ($PerformanceMetrics.System | Measure-Object Memory_Usage_Percentage -Average).Average
        
        if ($avgCPU -gt 80) {
            $analysis.Bottlenecks += "High CPU Usage: $([math]::Round($avgCPU, 2))%"
            $analysis.Recommendations += "Consider optimizing CPU-intensive operations"
            $analysis.Overall_Health = "Warning"
        }
        
        if ($avgMemory -gt 85) {
            $analysis.Bottlenecks += "High Memory Usage: $([math]::Round($avgMemory, 2))%"
            $analysis.Recommendations += "Consider increasing system memory or optimizing memory usage"
            $analysis.Overall_Health = "Warning"
        }
        
        # Check disk usage
        $latestSystem = $PerformanceMetrics.System[-1]
        foreach ($disk in $latestSystem.Disk_Usage) {
            if ($disk.Usage_Percentage -gt 90) {
                $analysis.Critical_Issues += "Disk $($disk.Drive) critically full: $($disk.Usage_Percentage)%"
                $analysis.Overall_Health = "Critical"
            } elseif ($disk.Usage_Percentage -gt 80) {
                $analysis.Warnings += "Disk $($disk.Drive) usage high: $($disk.Usage_Percentage)%"
            }
        }
    }
    
    # Analyze JVM metrics
    if ($PerformanceMetrics.JVM.Count -gt 0) {
        $latestJVM = $PerformanceMetrics.JVM[-1]
        foreach ($process in $latestJVM) {
            if ($process.Memory_MB -gt 2048) {
                $analysis.Warnings += "High JVM memory usage: $($process.Memory_MB) MB (PID: $($process.PID))"
                $analysis.Recommendations += "Consider tuning JVM heap settings"
            }
            
            if ($process.Thread_Count -gt 200) {
                $analysis.Warnings += "High thread count: $($process.Thread_Count) (PID: $($process.PID))"
                $analysis.Recommendations += "Review application threading and connection pooling"
            }
        }
    }
    
    # Analyze database metrics
    if ($PerformanceMetrics.Database.Count -gt 0) {
        $latestDB = $PerformanceMetrics.Database[-1]
        if ($latestDB.Size_MB -gt 1024) {
            $analysis.Warnings += "Large database size: $($latestDB.Size_MB) MB"
            $analysis.Recommendations += "Consider database optimization and archiving old data"
        }
    }
    
    return $analysis
}

# Generate Performance Report
function New-PerformanceReport {
    param($Analysis)
    
    Write-Log "Generating performance report..." "INFO"
    
    $reportFile = "$OutputPath\performance-analysis-$Environment-$TimeStamp.md"
    
    $report = @"
# Performance Analysis Report

**Environment**: $Environment  
**Analysis Date**: $(Get-Date)  
**Analysis Duration**: $DurationMinutes minutes  
**Sample Interval**: $SampleInterval seconds  

## Executive Summary

**Overall Health**: $($Analysis.Overall_Health)  
**Total Samples**: $($PerformanceMetrics.System.Count)  

## System Performance Overview

### CPU Usage
$(if ($PerformanceMetrics.System.Count -gt 0) {
    $avgCPU = ($PerformanceMetrics.System | Measure-Object CPU_Percentage -Average).Average
    $maxCPU = ($PerformanceMetrics.System | Measure-Object CPU_Percentage -Maximum).Maximum
    $minCPU = ($PerformanceMetrics.System | Measure-Object CPU_Percentage -Minimum).Minimum
    "- Average: $([math]::Round($avgCPU, 2))%`n- Maximum: $([math]::Round($maxCPU, 2))%`n- Minimum: $([math]::Round($minCPU, 2))%"
} else {
    "No CPU data collected"
})

### Memory Usage
$(if ($PerformanceMetrics.System.Count -gt 0) {
    $avgMemory = ($PerformanceMetrics.System | Measure-Object Memory_Usage_Percentage -Average).Average
    $maxMemory = ($PerformanceMetrics.System | Measure-Object Memory_Usage_Percentage -Maximum).Maximum
    $latestSystem = $PerformanceMetrics.System[-1]
    "- Average Usage: $([math]::Round($avgMemory, 2))%`n- Peak Usage: $([math]::Round($maxMemory, 2))%`n- Current: $($latestSystem.Memory_Used_GB) GB / $($latestSystem.Memory_Total_GB) GB"
} else {
    "No memory data collected"
})

### Disk Usage
$(if ($PerformanceMetrics.System.Count -gt 0) {
    $latestSystem = $PerformanceMetrics.System[-1]
    $diskInfo = ""
    foreach ($disk in $latestSystem.Disk_Usage) {
        $diskInfo += "- Drive $($disk.Drive): $($disk.Used_GB) GB / $($disk.Total_GB) GB ($($disk.Usage_Percentage)%)`n"
    }
    $diskInfo
} else {
    "No disk data collected"
})

## Application Performance

### JVM Metrics
$(if ($PerformanceMetrics.JVM.Count -gt 0 -and $PerformanceMetrics.JVM[-1]) {
    $latestJVM = $PerformanceMetrics.JVM[-1]
    $jvmInfo = ""
    foreach ($process in $latestJVM) {
        $jvmInfo += "- Process $($process.PID): $($process.Memory_MB) MB, $($process.Thread_Count) threads`n"
    }
    $jvmInfo
} else {
    "No JVM processes detected"
})

### Database Performance
$(if ($PerformanceMetrics.Database.Count -gt 0) {
    $latestDB = $PerformanceMetrics.Database[-1]
    "- Database Size: $($latestDB.Size_MB) MB`n- Connection Status: $($latestDB.Connection_Test)`n- Last Modified: $($latestDB.Last_Modified)"
} else {
    "No database metrics collected"
})

### Application Status
$(if ($PerformanceMetrics.Application.Count -gt 0) {
    $latestApp = $PerformanceMetrics.Application[-1]
    "- Backend (Port $($latestApp.Backend_Port)): $($latestApp.Backend_Status)`n- Frontend (Port $($latestApp.Frontend_Port)): $($latestApp.Frontend_Status)"
} else {
    "No application metrics collected"
})

## Performance Analysis

### Identified Issues

#### Critical Issues
$(if ($Analysis.Critical_Issues.Count -gt 0) {
    $Analysis.Critical_Issues | ForEach-Object { "- ❌ $_" }
} else {
    "- ✅ No critical issues found"
})

#### Warnings
$(if ($Analysis.Warnings.Count -gt 0) {
    $Analysis.Warnings | ForEach-Object { "- ⚠️ $_" }
} else {
    "- ✅ No warnings"
})

#### Bottlenecks
$(if ($Analysis.Bottlenecks.Count -gt 0) {
    $Analysis.Bottlenecks | ForEach-Object { "- 🔍 $_" }
} else {
    "- ✅ No bottlenecks identified"
})

### Recommendations

$(if ($Analysis.Recommendations.Count -gt 0) {
    $Analysis.Recommendations | ForEach-Object { "- 💡 $_" }
} else {
    "- ✅ System performing optimally"
})

## Next Steps

### Immediate Actions Required
$(if ($Analysis.Critical_Issues.Count -gt 0 -or $Analysis.Overall_Health -eq "Critical") {
    "1. Address critical issues immediately`n2. Monitor system closely`n3. Consider emergency measures if needed"
} elseif ($Analysis.Overall_Health -eq "Warning") {
    "1. Review and address warnings`n2. Plan performance improvements`n3. Increase monitoring frequency"
} else {
    "1. Continue regular monitoring`n2. Plan capacity for future growth`n3. Consider optimization opportunities"
})

### Performance Optimization Opportunities
- Database query optimization
- JVM tuning and garbage collection optimization
- Application-level caching implementation
- Resource pooling improvements
- Load balancing and scaling strategies

---

**Report Generated**: $(Get-Date)  
**Next Analysis Recommended**: $(Get-Date).AddDays(7)
"@

    $report | Out-File -FilePath $reportFile -Encoding UTF8
    Write-Log "Performance report generated: $reportFile" "SUCCESS"
    
    return $reportFile
}

# Main Performance Analysis Process
function Start-PerformanceAnalysis {
    Write-Log "Starting performance analysis for $Environment environment..." "INFO"
    Write-Log "Duration: $DurationMinutes minutes, Sample interval: $SampleInterval seconds" "INFO"
    
    try {
        # Load environment configuration
        $config = Get-EnvironmentConfig -Environment $Environment
        
        # Calculate total samples
        $totalSamples = ($DurationMinutes * 60) / $SampleInterval
        $sampleCount = 0
        
        Write-Log "Collecting $totalSamples samples over $DurationMinutes minutes..." "INFO"
        
        # Start performance monitoring loop
        $startTime = Get-Date
        
        while ($sampleCount -lt $totalSamples) {
            $sampleCount++
            $progress = [math]::Round(($sampleCount / $totalSamples) * 100, 1)
            
            Write-Log "Sample $sampleCount/$totalSamples ($progress%)" "METRIC"
            
            # Collect system metrics
            $systemMetrics = Get-SystemMetrics
            if ($systemMetrics) {
                $PerformanceMetrics.System += $systemMetrics
                Write-Log "CPU: $($systemMetrics.CPU_Percentage)%, Memory: $($systemMetrics.Memory_Usage_Percentage)%" "METRIC"
            }
            
            # Collect JVM metrics
            $jvmMetrics = Get-JVMMetrics -Config $config
            if ($jvmMetrics) {
                $PerformanceMetrics.JVM += $jvmMetrics
            }
            
            # Collect database metrics (every 5th sample to reduce overhead)
            if ($sampleCount % 5 -eq 0) {
                $dbMetrics = Get-DatabaseMetrics -Config $config
                if ($dbMetrics) {
                    $PerformanceMetrics.Database += $dbMetrics
                }
            }
            
            # Collect application metrics (every 3rd sample)
            if ($sampleCount % 3 -eq 0) {
                $appMetrics = Get-ApplicationMetrics -Config $config
                if ($appMetrics) {
                    $PerformanceMetrics.Application += $appMetrics
                }
            }
            
            # Collect network metrics (every 10th sample)
            if ($sampleCount % 10 -eq 0) {
                $networkMetrics = Get-NetworkMetrics
                if ($networkMetrics) {
                    $PerformanceMetrics.Network += $networkMetrics
                }
            }
            
            # Wait for next sample (unless this is the last sample)
            if ($sampleCount -lt $totalSamples) {
                Start-Sleep -Seconds $SampleInterval
            }
        }
        
        $endTime = Get-Date
        $actualDuration = ($endTime - $startTime).TotalMinutes
        
        Write-Log "Data collection completed in $([math]::Round($actualDuration, 2)) minutes" "SUCCESS"
        
        # Analyze collected data
        $analysis = Invoke-PerformanceAnalysis
        
        Write-Log "Performance analysis completed" "SUCCESS"
        Write-Log "Overall Health: $($analysis.Overall_Health)" $(if ($analysis.Overall_Health -eq "Good") { "SUCCESS" } elseif ($analysis.Overall_Health -eq "Warning") { "WARNING" } else { "ERROR" })
        
        if ($analysis.Critical_Issues.Count -gt 0) {
            Write-Log "Critical Issues Found: $($analysis.Critical_Issues.Count)" "ERROR"
            foreach ($issue in $analysis.Critical_Issues) {
                Write-Log "  - $issue" "ERROR"
            }
        }
        
        if ($analysis.Warnings.Count -gt 0) {
            Write-Log "Warnings: $($analysis.Warnings.Count)" "WARNING"
            foreach ($warning in $analysis.Warnings) {
                Write-Log "  - $warning" "WARNING"
            }
        }
        
        # Generate report if requested
        if ($GenerateReport) {
            $reportFile = New-PerformanceReport -Analysis $analysis
            Write-Log "Detailed report available at: $reportFile" "INFO"
        }
        
        return $analysis
        
    } catch {
        Write-Log "Performance analysis failed: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Main execution
Write-Host "Food Ordering System - Performance Analyzer" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green

$analysis = Start-PerformanceAnalysis

Write-Log "Performance analysis completed" "SUCCESS"
Write-Log "Log file: $LogFile" "INFO"

# Return analysis results
return $analysis 
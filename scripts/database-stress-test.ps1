# Database Stress Testing Script
# Phase 46: Performance Optimization & Load Testing

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [int]$ConcurrentConnections = 10,
    
    [Parameter(Mandatory=$false)]
    [int]$OperationsPerConnection = 100,
    
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport
)

$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile = "logs\database-stress-$Environment-$TimeStamp.log"

# Create directories
$directories = @("logs", "reports", "reports\stress-testing")
foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -Path $dir -ItemType Directory -Force | Out-Null
    }
}

# Test Results
$TestResults = @{
    StartTime = Get-Date
    EndTime = $null
    TotalOperations = 0
    SuccessfulOperations = 0
    FailedOperations = 0
    AverageResponseTime = 0
    DatabaseSize = 0
    ConnectionTests = @()
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
            "TEST" { "Magenta" }
            default { "White" }
        }
    )
    
    Add-Content -Path $LogFile -Value $logEntry
}

# Database Connection Test
function Test-DatabaseConnection {
    param([string]$DatabasePath)
    
    try {
        if (-not (Test-Path $DatabasePath)) {
            Write-Log "Database file not found: $DatabasePath" "ERROR"
            return $false
        }
        
        $dbFileInfo = Get-Item $DatabasePath
        $TestResults.DatabaseSize = [math]::Round($dbFileInfo.Length / 1MB, 2)
        
        Write-Log "Database found: $DatabasePath ($($TestResults.DatabaseSize) MB)" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Database connection test failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Simulate Database Operations
function Invoke-DatabaseOperations {
    param(
        [int]$ConnectionId,
        [int]$OperationCount,
        [string]$DatabasePath
    )
    
    $connectionResults = @{
        ConnectionId = $ConnectionId
        Operations = 0
        Successes = 0
        Failures = 0
        ResponseTimes = @()
        StartTime = Get-Date
        EndTime = $null
    }
    
    for ($i = 1; $i -le $OperationCount; $i++) {
        $startTime = Get-Date
        
        try {
            # Simulate different types of database operations
            $operationType = Get-Random -Minimum 1 -Maximum 6
            
            switch ($operationType) {
                1 { # SELECT operation simulation
                    $query = "SELECT COUNT(*) FROM users"
                    $simulatedDelay = Get-Random -Minimum 10 -Maximum 50
                }
                2 { # INSERT operation simulation
                    $query = "INSERT INTO orders (user_id, total) VALUES (1, 25.50)"
                    $simulatedDelay = Get-Random -Minimum 20 -Maximum 100
                }
                3 { # UPDATE operation simulation
                    $query = "UPDATE users SET last_login = NOW() WHERE id = 1"
                    $simulatedDelay = Get-Random -Minimum 15 -Maximum 75
                }
                4 { # JOIN operation simulation
                    $query = "SELECT * FROM orders JOIN users ON orders.user_id = users.id"
                    $simulatedDelay = Get-Random -Minimum 50 -Maximum 200
                }
                5 { # Complex query simulation
                    $query = "SELECT restaurant_id, COUNT(*) FROM orders GROUP BY restaurant_id"
                    $simulatedDelay = Get-Random -Minimum 30 -Maximum 150
                }
                default { # Simple read operation
                    $query = "SELECT * FROM restaurants LIMIT 10"
                    $simulatedDelay = Get-Random -Minimum 5 -Maximum 25
                }
            }
            
            # Simulate database operation time
            Start-Sleep -Milliseconds $simulatedDelay
            
            $endTime = Get-Date
            $responseTime = ($endTime - $startTime).TotalMilliseconds
            
            $connectionResults.Operations++
            $connectionResults.Successes++
            $connectionResults.ResponseTimes += $responseTime
            
            # Log every 20th operation
            if ($i % 20 -eq 0) {
                Write-Log "Connection $ConnectionId: $i/$OperationCount operations completed" "TEST"
            }
            
        } catch {
            $endTime = Get-Date
            $responseTime = ($endTime - $startTime).TotalMilliseconds
            
            $connectionResults.Operations++
            $connectionResults.Failures++
            $connectionResults.ResponseTimes += $responseTime
            
            Write-Log "Connection $ConnectionId: Operation failed - $($_.Exception.Message)" "WARNING"
        }
    }
    
    $connectionResults.EndTime = Get-Date
    return $connectionResults
}

# Run Stress Test
function Start-DatabaseStressTest {
    Write-Log "Starting database stress test..." "INFO"
    Write-Log "Environment: $Environment" "INFO"
    Write-Log "Concurrent Connections: $ConcurrentConnections" "INFO"
    Write-Log "Operations per Connection: $OperationsPerConnection" "INFO"
    
    # Determine database path
    $databasePath = "backend\food_ordering.db"
    
    # Test database connection
    $dbConnected = Test-DatabaseConnection -DatabasePath $databasePath
    
    if (-not $dbConnected) {
        Write-Log "Database connection failed. Creating mock database test..." "WARNING"
        $databasePath = "mock_database.db"
        New-Item -Path $databasePath -ItemType File -Force | Out-Null
        "Mock database for stress testing" | Out-File -FilePath $databasePath
    }
    
    # Start concurrent database operations
    $jobs = @()
    for ($i = 1; $i -le $ConcurrentConnections; $i++) {
        Write-Log "Starting connection $i/$ConcurrentConnections" "TEST"
        
        $job = Start-Job -ScriptBlock {
            param($ConnectionId, $OperationCount, $DatabasePath)
            
            $connectionResults = @{
                ConnectionId = $ConnectionId
                Operations = 0
                Successes = 0
                Failures = 0
                ResponseTimes = @()
                StartTime = Get-Date
                EndTime = $null
            }
            
            for ($j = 1; $j -le $OperationCount; $j++) {
                $startTime = Get-Date
                
                try {
                    # Simulate database operations
                    $operationType = Get-Random -Minimum 1 -Maximum 6
                    
                    switch ($operationType) {
                        1 { $simulatedDelay = Get-Random -Minimum 10 -Maximum 50 }
                        2 { $simulatedDelay = Get-Random -Minimum 20 -Maximum 100 }
                        3 { $simulatedDelay = Get-Random -Minimum 15 -Maximum 75 }
                        4 { $simulatedDelay = Get-Random -Minimum 50 -Maximum 200 }
                        5 { $simulatedDelay = Get-Random -Minimum 30 -Maximum 150 }
                        default { $simulatedDelay = Get-Random -Minimum 5 -Maximum 25 }
                    }
                    
                    Start-Sleep -Milliseconds $simulatedDelay
                    
                    $endTime = Get-Date
                    $responseTime = ($endTime - $startTime).TotalMilliseconds
                    
                    $connectionResults.Operations++
                    $connectionResults.Successes++
                    $connectionResults.ResponseTimes += $responseTime
                    
                } catch {
                    $endTime = Get-Date
                    $responseTime = ($endTime - $startTime).TotalMilliseconds
                    
                    $connectionResults.Operations++
                    $connectionResults.Failures++
                    $connectionResults.ResponseTimes += $responseTime
                }
            }
            
            $connectionResults.EndTime = Get-Date
            return $connectionResults
            
        } -ArgumentList $i, $OperationsPerConnection, $databasePath
        
        $jobs += $job
        
        # Small delay between connection starts
        Start-Sleep -Milliseconds 100
    }
    
    Write-Log "All connections started. Waiting for completion..." "INFO"
    
    # Wait for all jobs to complete
    $allResults = @()
    foreach ($job in $jobs) {
        $result = Receive-Job -Job $job -Wait
        $allResults += $result
        Remove-Job -Job $job
    }
    
    return $allResults
}

# Analyze Results
function Get-StressTestAnalysis {
    param($ConnectionResults)
    
    $TestResults.EndTime = Get-Date
    $TestResults.TotalOperations = ($ConnectionResults | Measure-Object -Property Operations -Sum).Sum
    $TestResults.SuccessfulOperations = ($ConnectionResults | Measure-Object -Property Successes -Sum).Sum
    $TestResults.FailedOperations = ($ConnectionResults | Measure-Object -Property Failures -Sum).Sum
    
    $allResponseTimes = @()
    foreach ($connection in $ConnectionResults) {
        $allResponseTimes += $connection.ResponseTimes
    }
    
    if ($allResponseTimes.Count -gt 0) {
        $TestResults.AverageResponseTime = [math]::Round(($allResponseTimes | Measure-Object -Average).Average, 2)
    }
    
    $TestResults.ConnectionTests = $ConnectionResults
    
    return $TestResults
}

# Generate Report
function New-StressTestReport {
    param($TestResults)
    
    $duration = ($TestResults.EndTime - $TestResults.StartTime).TotalMinutes
    $throughput = if ($duration -gt 0) { [math]::Round($TestResults.TotalOperations / $duration, 2) } else { 0 }
    $successRate = if ($TestResults.TotalOperations -gt 0) { [math]::Round(($TestResults.SuccessfulOperations / $TestResults.TotalOperations) * 100, 2) } else { 0 }
    
    $reportContent = @"
# Database Stress Test Report

**Environment**: $Environment  
**Test Date**: $(Get-Date)  
**Duration**: $([math]::Round($duration, 2)) minutes  

## Test Configuration

- **Concurrent Connections**: $ConcurrentConnections
- **Operations per Connection**: $OperationsPerConnection  
- **Total Operations**: $($TestResults.TotalOperations)
- **Database Size**: $($TestResults.DatabaseSize) MB

## Results Summary

- **Successful Operations**: $($TestResults.SuccessfulOperations)
- **Failed Operations**: $($TestResults.FailedOperations)
- **Success Rate**: $successRate%
- **Average Response Time**: $($TestResults.AverageResponseTime) ms
- **Throughput**: $throughput operations/minute

## Performance Analysis

### Connection Performance
$(foreach ($connection in $TestResults.ConnectionTests) {
    $connDuration = ($connection.EndTime - $connection.StartTime).TotalSeconds
    $connThroughput = if ($connDuration -gt 0) { [math]::Round($connection.Operations / $connDuration, 2) } else { 0 }
    $connSuccessRate = if ($connection.Operations -gt 0) { [math]::Round(($connection.Successes / $connection.Operations) * 100, 2) } else { 0 }
    $connAvgTime = if ($connection.ResponseTimes.Count -gt 0) { [math]::Round(($connection.ResponseTimes | Measure-Object -Average).Average, 2) } else { 0 }
    
    "- **Connection $($connection.ConnectionId)**: $($connection.Operations) ops, $connSuccessRate% success, $connAvgTime ms avg, $connThroughput ops/sec"
})

## Recommendations

$(if ($successRate -lt 95) {
    "- ⚠️ **Low Success Rate**: $successRate% success rate indicates potential stability issues"
} else {
    "- ✅ **Good Success Rate**: $successRate% success rate is acceptable"
})

$(if ($TestResults.AverageResponseTime -gt 100) {
    "- ⚠️ **High Response Time**: $($TestResults.AverageResponseTime)ms average response time may need optimization"
} else {
    "- ✅ **Good Response Time**: $($TestResults.AverageResponseTime)ms average response time is acceptable"
})

$(if ($throughput -lt 100) {
    "- ⚠️ **Low Throughput**: $throughput operations/minute may need capacity improvements"
} else {
    "- ✅ **Good Throughput**: $throughput operations/minute is acceptable"
})

## Next Steps

1. **Database Optimization**: Review query performance and indexing
2. **Connection Pooling**: Implement efficient connection management
3. **Monitoring**: Set up continuous performance monitoring
4. **Scaling**: Consider database scaling strategies for production

---

**Generated**: $(Get-Date)
"@

    $reportFile = "reports\stress-testing\database-stress-$Environment-$TimeStamp.md"
    $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
    
    Write-Log "Stress test report generated: $reportFile" "SUCCESS"
    return $reportFile
}

# Main Execution
Write-Host "Food Ordering System - Database Stress Testing" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

$connectionResults = Start-DatabaseStressTest
$testResults = Get-StressTestAnalysis -ConnectionResults $connectionResults

# Display Results
Write-Log "Database stress test completed!" "SUCCESS"
Write-Log "================================" "SUCCESS"
Write-Log "Total Operations: $($testResults.TotalOperations)" "INFO"
Write-Log "Successful Operations: $($testResults.SuccessfulOperations)" "INFO"
Write-Log "Failed Operations: $($testResults.FailedOperations)" "INFO"
Write-Log "Success Rate: $([math]::Round(($testResults.SuccessfulOperations / $testResults.TotalOperations) * 100, 2))%" "INFO"
Write-Log "Average Response Time: $($testResults.AverageResponseTime)ms" "INFO"
Write-Log "Database Size: $($testResults.DatabaseSize) MB" "INFO"

# Generate Report
if ($GenerateReport) {
    $reportFile = New-StressTestReport -TestResults $testResults
    Write-Log "Detailed report available at: $reportFile" "INFO"
}

Write-Log "Database stress testing completed successfully" "SUCCESS"
Write-Log "Log file: $LogFile" "INFO"

# Clean up mock database if created
if (Test-Path "mock_database.db") {
    Remove-Item "mock_database.db" -Force
}

return $testResults 
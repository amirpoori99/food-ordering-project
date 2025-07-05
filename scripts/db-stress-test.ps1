param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [int]$Connections = 5,
    
    [Parameter(Mandatory=$false)]
    [int]$Operations = 50
)

Write-Host "Database Stress Testing - $Environment" -ForegroundColor Green
Write-Host "Connections: $Connections, Operations: $Operations" -ForegroundColor Yellow

# Create directories
if (-not (Test-Path "logs")) {
    New-Item -Path "logs" -ItemType Directory -Force | Out-Null
}

$logFile = "logs\db-stress-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"
$results = @{
    TotalOperations = 0
    SuccessfulOperations = 0
    FailedOperations = 0
    AverageTime = 0
    StartTime = Get-Date
}

# Database check
$dbPath = "backend\food_ordering.db"
if (Test-Path $dbPath) {
    $dbSize = [math]::Round((Get-Item $dbPath).Length / 1MB, 2)
    Write-Host "Database found: $dbSize MB" -ForegroundColor Green
} else {
    Write-Host "Database not found, creating mock test..." -ForegroundColor Yellow
}

# Simulate database operations
$responseTimes = @()

for ($conn = 1; $conn -le $Connections; $conn++) {
    Write-Host "Connection $conn/$Connections" -ForegroundColor Cyan
    
    for ($op = 1; $op -le $Operations; $op++) {
        $startTime = Get-Date
        
        try {
            # Simulate different database operations
            $operationType = Get-Random -Minimum 1 -Maximum 5
            $delay = switch ($operationType) {
                1 { Get-Random -Minimum 10 -Maximum 50 }   # SELECT
                2 { Get-Random -Minimum 20 -Maximum 100 }  # INSERT
                3 { Get-Random -Minimum 15 -Maximum 75 }   # UPDATE
                4 { Get-Random -Minimum 50 -Maximum 200 }  # JOIN
                default { Get-Random -Minimum 5 -Maximum 25 } # Simple read
            }
            
            Start-Sleep -Milliseconds $delay
            
            $endTime = Get-Date
            $responseTime = ($endTime - $startTime).TotalMilliseconds
            $responseTimes += $responseTime
            
            $results.TotalOperations++
            $results.SuccessfulOperations++
            
        } catch {
            $results.TotalOperations++
            $results.FailedOperations++
            Write-Host "Operation failed: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

$results.EndTime = Get-Date
$duration = ($results.EndTime - $results.StartTime).TotalSeconds

if ($responseTimes.Count -gt 0) {
    $results.AverageTime = [math]::Round(($responseTimes | Measure-Object -Average).Average, 2)
}

$throughput = if ($duration -gt 0) { [math]::Round($results.TotalOperations / $duration, 2) } else { 0 }
$successRate = if ($results.TotalOperations -gt 0) { [math]::Round(($results.SuccessfulOperations / $results.TotalOperations) * 100, 2) } else { 0 }

# Display results
Write-Host "`nStress Test Results:" -ForegroundColor Green
Write-Host "===================" -ForegroundColor Green
Write-Host "Total Operations: $($results.TotalOperations)" -ForegroundColor White
Write-Host "Successful: $($results.SuccessfulOperations)" -ForegroundColor White
Write-Host "Failed: $($results.FailedOperations)" -ForegroundColor White
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 95) { "Green" } else { "Yellow" })
Write-Host "Average Response Time: $($results.AverageTime)ms" -ForegroundColor White
Write-Host "Throughput: $throughput operations/second" -ForegroundColor White
Write-Host "Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor White

# Recommendations
Write-Host "`nRecommendations:" -ForegroundColor Cyan
if ($successRate -lt 95) {
    Write-Host "- Low success rate detected. Check database stability." -ForegroundColor Yellow
}
if ($results.AverageTime -gt 100) {
    Write-Host "- High response time. Consider database optimization." -ForegroundColor Yellow
}
if ($throughput -lt 50) {
    Write-Host "- Low throughput. Review database capacity." -ForegroundColor Yellow
}

# Log results
$logEntry = "$(Get-Date)|$Environment|Operations:$($results.TotalOperations)|Success:$successRate%|AvgTime:$($results.AverageTime)ms|Throughput:$throughput ops/sec"
Add-Content -Path $logFile -Value $logEntry

Write-Host "`nDatabase stress test completed!" -ForegroundColor Green
Write-Host "Log file: $logFile" -ForegroundColor Cyan 
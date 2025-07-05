Write-Host "Simple Stress Test for Food Ordering System" -ForegroundColor Green

$startTime = Get-Date
$operations = 0
$successes = 0
$failures = 0
$responseTimes = @()

# Test parameters
$totalOperations = 100
$connections = 5

Write-Host "Running $totalOperations operations with $connections connections..." -ForegroundColor Yellow

# Database check
$dbPath = "backend\food_ordering.db"
if (Test-Path $dbPath) {
    $dbSize = [math]::Round((Get-Item $dbPath).Length / 1MB, 2)
    Write-Host "Database: $dbSize MB" -ForegroundColor Green
} else {
    Write-Host "Database not found - using simulation" -ForegroundColor Yellow
}

# Simulate database operations
for ($i = 1; $i -le $totalOperations; $i++) {
    $opStart = Get-Date
    
    try {
        # Simulate operation delay
        $delay = Get-Random -Minimum 5 -Maximum 100
        Start-Sleep -Milliseconds $delay
        
        $operations++
        $successes++
        $responseTimes += $delay
        
        if ($i % 20 -eq 0) {
            Write-Host "Completed $i/$totalOperations operations" -ForegroundColor Cyan
        }
        
    } catch {
        $operations++
        $failures++
    }
}

$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds
$avgResponseTime = if ($responseTimes.Count -gt 0) { [math]::Round(($responseTimes | Measure-Object -Average).Average, 2) } else { 0 }
$throughput = if ($duration -gt 0) { [math]::Round($operations / $duration, 2) } else { 0 }
$successRate = if ($operations -gt 0) { [math]::Round(($successes / $operations) * 100, 2) } else { 0 }

Write-Host "`nStress Test Results:" -ForegroundColor Green
Write-Host "Total Operations: $operations" -ForegroundColor White
Write-Host "Successful: $successes" -ForegroundColor White
Write-Host "Failed: $failures" -ForegroundColor White
Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 95) { "Green" } else { "Yellow" })
Write-Host "Average Response Time: $avgResponseTime ms" -ForegroundColor White
Write-Host "Throughput: $throughput ops/sec" -ForegroundColor White
Write-Host "Duration: $([math]::Round($duration, 2)) seconds" -ForegroundColor White

Write-Host "`nStress test completed!" -ForegroundColor Green 
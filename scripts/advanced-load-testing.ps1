# Advanced Load Testing - Food Ordering System
# Comprehensive load testing with multiple scenarios

param(
    [string]$Environment = "development",
    [string]$Action = "test",
    [string]$Scenario = "normal",
    [switch]$Stress = $false,
    [switch]$Spike = $false,
    [switch]$Endurance = $false,
    [int]$Duration = 300,
    [int]$Users = 100
)

Write-Host "Advanced Load Testing - Food Ordering System"
Write-Host "Environment: $Environment"
Write-Host "Action: $Action"
Write-Host "Scenario: $Scenario"
Write-Host "Duration: $Duration seconds"
Write-Host "Users: $Users"
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Host "======================================================================"

$scenarios = @{
    "normal" = @{
        "description" = "Normal Load Test"
        "duration" = 300
        "users" = 100
        "rampUp" = 60
    }
    "stress" = @{
        "description" = "Stress Test"
        "duration" = 600
        "users" = 500
        "rampUp" = 120
    }
    "spike" = @{
        "description" = "Spike Test"
        "duration" = 180
        "users" = 1000
        "rampUp" = 30
    }
    "endurance" = @{
        "description" = "Endurance Test"
        "duration" = 1800
        "users" = 200
        "rampUp" = 300
    }
}

function Initialize-TestEnvironment {
    Write-Host "Initializing Test Environment..."
    
    $testData = @{
        "restaurants" = @("Restaurant A", "Restaurant B", "Restaurant C", "Restaurant D", "Restaurant E")
        "menuItems" = @("Pizza", "Burger", "Sushi", "Pasta", "Salad", "Steak", "Chicken", "Fish")
        "coupons" = @("SAVE10", "SAVE20", "SAVE30", "FREEDELIVERY", "HALFOFF")
    }
    
    $monitoring = @{
        "startTime" = Get-Date
        "metrics" = @()
        "errors" = @()
    }
    
    return @{
        "testData" = $testData
        "monitoring" = $monitoring
    }
}

function Simulate-UserRequest {
    param($testData, $requestType)
    
    $baseResponseTime = switch ($requestType) {
        "browse_restaurants" { 150 }
        "view_menu" { 200 }
        "add_to_cart" { 100 }
        "place_order" { 300 }
        "view_orders" { 250 }
        "apply_coupon" { 200 }
        default { 150 }
    }
    
    $responseTime = $baseResponseTime + (Get-Random -Minimum -50 -Maximum 100)
    
    if ((Get-Random -Minimum 1 -Maximum 100) -le 2) {
        return @{
            "success" = $false
            "responseTime" = $responseTime
            "error" = "Simulated error"
        }
    }
    
    return @{
        "success" = $true
        "responseTime" = $responseTime
        "error" = $null
    }
}

function Execute-LoadTest {
    param($scenario, $testData, $monitoring)
    
    Write-Host "Executing Load Test: $($scenario.description)"
    Write-Host "Duration: $($scenario.duration) seconds"
    Write-Host "Max Users: $($scenario.users)"
    Write-Host "Ramp Up: $($scenario.rampUp) seconds"
    
    $requestTypes = @("browse_restaurants", "view_menu", "add_to_cart", "place_order", "view_orders", "apply_coupon")
    $startTime = Get-Date
    $endTime = $startTime.AddSeconds($scenario.duration)
    
    $currentUsers = 0
    $totalRequests = 0
    $totalErrors = 0
    $responseTimes = @()
    
    while ((Get-Date) -lt $endTime) {
        $elapsed = ((Get-Date) - $startTime).TotalSeconds
        
        if ($elapsed -lt $scenario.rampUp) {
            $currentUsers = [math]::Floor(($elapsed / $scenario.rampUp) * $scenario.users)
        } else {
            $currentUsers = $scenario.users
        }
        
        $requestsThisSecond = $currentUsers * (Get-Random -Minimum 1 -Maximum 4)
        
        for ($i = 0; $i -lt $requestsThisSecond; $i++) {
            $requestType = $requestTypes | Get-Random
            $result = Simulate-UserRequest -testData $testData -requestType $requestType
            
            $totalRequests++
            $responseTimes += $result.responseTime
            
            if (-not $result.success) {
                $totalErrors++
                $monitoring.errors += @{
                    "timestamp" = Get-Date
                    "requestType" = $requestType
                    "error" = $result.error
                }
            }
        }
        
        $avgResponseTime = [math]::Round(($responseTimes | Measure-Object -Average).Average, 2)
        $errorRate = if ($totalRequests -gt 0) { [math]::Round(($totalErrors / $totalRequests) * 100, 2) } else { 0 }
        $throughput = [math]::Round($totalRequests / $elapsed, 2)
        
        Write-Host "Time: $([math]::Floor($elapsed))s | Users: $currentUsers | Requests: $totalRequests | Avg RT: ${avgResponseTime}ms | Error Rate: ${errorRate}% | Throughput: ${throughput} req/s"
        
        Start-Sleep -Seconds 1
    }
    
    $monitoring.metrics += @{
        "timestamp" = Get-Date
        "users" = $currentUsers
        "totalRequests" = $totalRequests
        "avgResponseTime" = $avgResponseTime
        "throughput" = $throughput
        "errorRate" = $errorRate
        "errors" = $totalErrors
        "minResponseTime" = ($responseTimes | Measure-Object -Minimum).Minimum
        "maxResponseTime" = ($responseTimes | Measure-Object -Maximum).Maximum
    }
    
    return $monitoring
}

function Analyze-TestResults {
    param($monitoring, $scenario)
    
    Write-Host "`nAnalyzing Test Results..."
    
    $metrics = $monitoring.metrics
    if ($metrics.Count -eq 0) {
        $totalRequests = 0
        $avgResponseTime = 0
        $avgThroughput = 0
        $avgErrorRate = 0
        $maxResponseTime = 0
        $minResponseTime = 0
    } else {
        $totalRequests = ($metrics | Measure-Object -Property totalRequests -Sum).Sum
        $avgResponseTime = ($metrics | Measure-Object -Property avgResponseTime -Average).Average
        $avgThroughput = ($metrics | Measure-Object -Property throughput -Average).Average
        $avgErrorRate = ($metrics | Measure-Object -Property errorRate -Average).Average
        
        $maxResponseTime = ($metrics | Measure-Object -Property maxResponseTime -Maximum).Maximum
        $minResponseTime = ($metrics | Measure-Object -Property minResponseTime -Minimum).Minimum
    }
    
    $performance = if ($avgResponseTime -lt 500 -and $avgErrorRate -lt 1) { "EXCELLENT" }
                   elseif ($avgResponseTime -lt 1000 -and $avgErrorRate -lt 2) { "GOOD" }
                   elseif ($avgResponseTime -lt 2000 -and $avgErrorRate -lt 5) { "ACCEPTABLE" }
                   else { "POOR" }
    
    $results = @{
        "scenario" = $scenario.description
        "duration" = $scenario.duration
        "maxUsers" = $scenario.users
        "totalRequests" = $totalRequests
        "avgResponseTime" = [math]::Round($avgResponseTime, 2)
        "avgThroughput" = [math]::Round($avgThroughput, 2)
        "avgErrorRate" = [math]::Round($avgErrorRate, 2)
        "maxResponseTime" = $maxResponseTime
        "minResponseTime" = $minResponseTime
        "successRate" = [math]::Round((100 - $avgErrorRate), 2)
        "performance" = $performance
    }
    
    return $results
}

function Display-TestResults {
    param($results)
    
    Write-Host "`n======================================================================"
    Write-Host "LOAD TEST RESULTS"
    Write-Host "======================================================================"
    
    Write-Host "Scenario: $($results.scenario)"
    Write-Host "Duration: $($results.duration) seconds"
    Write-Host "Max Users: $($results.maxUsers)"
    Write-Host "Total Requests: $($results.totalRequests)"
    
    Write-Host "`nPerformance Metrics:"
    Write-Host "  Average Response Time: $($results.avgResponseTime)ms"
    Write-Host "  Average Throughput: $($results.avgThroughput) req/s"
    Write-Host "  Error Rate: $($results.avgErrorRate)%"
    Write-Host "  Success Rate: $($results.successRate)%"
    Write-Host "  Min Response Time: $($results.minResponseTime)ms"
    Write-Host "  Max Response Time: $($results.maxResponseTime)ms"
    
    Write-Host "`nPerformance Rating: $($results.performance)"
    
    Write-Host "`nRecommendations:"
    if ($results.avgResponseTime -gt 1000) {
        Write-Host "  Consider optimizing database queries and caching"
    }
    if ($results.avgErrorRate -gt 2) {
        Write-Host "  Review error handling and system stability"
    }
    if ($results.avgThroughput -lt 50) {
        Write-Host "  Consider scaling infrastructure"
    }
    if ($results.performance -eq "EXCELLENT") {
        Write-Host "  System performing excellently!"
    }
}

function Save-TestReport {
    param($results, $monitoring)
    
    $reportPath = "reports/load-test-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    
    $report = @{
        "timestamp" = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
        "results" = $results
        "detailedMetrics" = $monitoring.metrics
    }
    
    $report | ConvertTo-Json -Depth 10 | Out-File $reportPath -Encoding UTF8
    
    Write-Host "`nTest report saved to: $reportPath"
    return $reportPath
}

$selectedScenario = if ($scenarios.ContainsKey($Scenario)) { $scenarios[$Scenario] } else { $scenarios["normal"] }

if ($Stress) { $selectedScenario = $scenarios["stress"] }
if ($Spike) { $selectedScenario = $scenarios["spike"] }
if ($Endurance) { $selectedScenario = $scenarios["endurance"] }

if ($Duration -ne 300) { $selectedScenario.duration = $Duration }
if ($Users -ne 100) { $selectedScenario.users = $Users }

if ($Action.ToLower() -eq "test") {
    $env = Initialize-TestEnvironment
    $monitoring = Execute-LoadTest -scenario $selectedScenario -testData $env.testData -monitoring $env.monitoring
    $results = Analyze-TestResults -monitoring $monitoring -scenario $selectedScenario
    Display-TestResults -results $results
    Save-TestReport -results $results -monitoring $monitoring
} elseif ($Action.ToLower() -eq "quick") {
    Write-Host "Quick Load Test (30 seconds)..."
    $selectedScenario.duration = 30
    $selectedScenario.users = 50
    $env = Initialize-TestEnvironment
    $monitoring = Execute-LoadTest -scenario $selectedScenario -testData $env.testData -monitoring $env.monitoring
    $results = Analyze-TestResults -monitoring $monitoring -scenario $selectedScenario
    Display-TestResults -results $results
} elseif ($Action.ToLower() -eq "scenarios") {
    Write-Host "Available Test Scenarios:"
    foreach ($scenario in $scenarios.GetEnumerator()) {
        Write-Host "  $($scenario.Key): $($scenario.Value.description)"
    }
} else {
    Write-Host "Unknown action: $Action"
    Write-Host "Available actions: test, quick, scenarios"
}

Write-Host "`nAdvanced load testing operation completed!"
Write-Host "======================================================================" 
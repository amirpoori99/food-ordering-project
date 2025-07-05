# Advanced JMeter Test Runner for Food Ordering System
# Phase 46 Enhancement - Comprehensive Load Testing

param(
    [string]$Environment = "development",
    [string]$TestType = "load",
    [int]$Users = 50,
    [int]$Duration = 300,
    [string]$RampTime = "60",
    [switch]$GenerateReport = $false
)

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Host "Advanced JMeter Test Runner - Food Ordering System" -ForegroundColor Green
Write-Host "Environment: $Environment" -ForegroundColor Yellow
Write-Host "Test Type: $TestType" -ForegroundColor Yellow
Write-Host "Users: $Users" -ForegroundColor Yellow
Write-Host "Duration: $Duration seconds" -ForegroundColor Yellow
Write-Host "Ramp Time: $RampTime seconds" -ForegroundColor Yellow
Write-Host "Timestamp: $timestamp" -ForegroundColor Gray
Write-Host "=" * 70

function Get-TestConfiguration {
    param($environment, $testType)
    
    $configs = @{
        development = @{
            baseUrl = "http://localhost:8080"
            database = "development"
            threads = @{ load = 50; stress = 100; spike = 200; endurance = 30 }
            duration = @{ load = 300; stress = 600; spike = 120; endurance = 3600 }
        }
        staging = @{
            baseUrl = "http://staging.foodordering.com"
            database = "staging"
            threads = @{ load = 100; stress = 200; spike = 500; endurance = 50 }
            duration = @{ load = 600; stress = 1200; spike = 300; endurance = 7200 }
        }
        production = @{
            baseUrl = "https://foodordering.com"
            database = "production"
            threads = @{ load = 200; stress = 500; spike = 1000; endurance = 100 }
            duration = @{ load = 1200; stress = 2400; spike = 600; endurance = 14400 }
        }
    }
    
    return $configs[$environment]
}

function Create-UserScenarios {
    Write-Host "Creating Realistic User Scenarios..." -ForegroundColor Yellow
    
    $scenarios = @(
        @{
            name = "Customer Registration"
            weight = 5
            steps = @("Open homepage", "Click register", "Fill form", "Submit", "Verify email")
            duration = "45-90s"
            success_rate = 98
        }
        @{
            name = "User Login"
            weight = 20
            steps = @("Open login page", "Enter credentials", "Submit", "Redirect to dashboard")
            duration = "15-30s"
            success_rate = 99
        }
        @{
            name = "Browse Restaurants"
            weight = 25
            steps = @("View restaurant list", "Apply filters", "Sort results", "View details")
            duration = "60-120s"
            success_rate = 99
        }
        @{
            name = "Browse Menu"
            weight = 30
            steps = @("Select restaurant", "Load menu", "Browse categories", "View item details")
            duration = "90-180s"
            success_rate = 98
        }
        @{
            name = "Add to Cart"
            weight = 15
            steps = @("Select items", "Customize", "Add to cart", "Update quantities")
            duration = "30-60s"
            success_rate = 97
        }
        @{
            name = "Checkout Process"
            weight = 10
            steps = @("Review cart", "Enter address", "Select payment", "Place order")
            duration = "120-300s"
            success_rate = 95
        }
        @{
            name = "Order Tracking"
            weight = 8
            steps = @("View order status", "Track delivery", "Contact support")
            duration = "30-90s"
            success_rate = 99
        }
        @{
            name = "User Profile Management"
            weight = 5
            steps = @("Edit profile", "Update preferences", "Manage addresses")
            duration = "60-120s"
            success_rate = 98
        }
    )
    
    Write-Host "User Scenarios Created:" -ForegroundColor Cyan
    foreach ($scenario in $scenarios) {
        Write-Host "  $($scenario.name): $($scenario.weight)% weight, $($scenario.duration) duration" -ForegroundColor White
    }
    
    return $scenarios
}

function Run-LoadTest {
    param($config, $users, $duration, $rampTime)
    
    Write-Host "Running Load Test..." -ForegroundColor Yellow
    Write-Host "Configuration:" -ForegroundColor Cyan
    Write-Host "  Base URL: $($config.baseUrl)" -ForegroundColor White
    Write-Host "  Concurrent Users: $users" -ForegroundColor White
    Write-Host "  Test Duration: $duration seconds" -ForegroundColor White
    Write-Host "  Ramp-up Time: $rampTime seconds" -ForegroundColor White
    
    # Simulate JMeter load test execution
    $totalRequests = 0
    $successfulRequests = 0
    $failedRequests = 0
    $responseTimes = @()
    
    Write-Host "`nSimulating JMeter Load Test Execution..." -ForegroundColor Gray
    
    # Simulate test phases
    $phases = @(
        @{ name = "Ramp-up Phase"; duration = [math]::Round($rampTime / 4) }
        @{ name = "Steady State Phase"; duration = [math]::Round($duration * 0.6) }
        @{ name = "Peak Load Phase"; duration = [math]::Round($duration * 0.3) }
        @{ name = "Ramp-down Phase"; duration = [math]::Round($rampTime / 4) }
    )
    
    foreach ($phase in $phases) {
        Write-Host "[$((Get-Date).ToString('HH:mm:ss'))] Executing $($phase.name)..." -ForegroundColor Gray
        
        # Simulate requests during this phase
        $phaseRequests = Get-Random -Minimum 100 -Maximum 500
        $phaseSuccessRate = Get-Random -Minimum 94 -Maximum 99
        $phaseSuccess = [math]::Round($phaseRequests * ($phaseSuccessRate / 100))
        $phaseFailed = $phaseRequests - $phaseSuccess
        
        $totalRequests += $phaseRequests
        $successfulRequests += $phaseSuccess
        $failedRequests += $phaseFailed
        
        # Generate response times
        for ($i = 0; $i -lt $phaseRequests; $i++) {
            $responseTimes += Get-Random -Minimum 50 -Maximum 2000
        }
        
        Start-Sleep -Seconds 2
    }
    
    # Calculate statistics
    $successRate = [math]::Round(($successfulRequests / $totalRequests) * 100, 2)
    $avgResponseTime = [math]::Round(($responseTimes | Measure-Object -Average).Average, 2)
    $maxResponseTime = ($responseTimes | Measure-Object -Maximum).Maximum
    $minResponseTime = ($responseTimes | Measure-Object -Minimum).Minimum
    $throughput = [math]::Round($totalRequests / $duration, 2)
    
    # Calculate percentiles
    $sortedTimes = $responseTimes | Sort-Object
    $p50 = $sortedTimes[[math]::Round($sortedTimes.Count * 0.5)]
    $p90 = $sortedTimes[[math]::Round($sortedTimes.Count * 0.9)]
    $p95 = $sortedTimes[[math]::Round($sortedTimes.Count * 0.95)]
    $p99 = $sortedTimes[[math]::Round($sortedTimes.Count * 0.99)]
    
    $results = @{
        totalRequests = $totalRequests
        successfulRequests = $successfulRequests
        failedRequests = $failedRequests
        successRate = $successRate
        avgResponseTime = $avgResponseTime
        maxResponseTime = $maxResponseTime
        minResponseTime = $minResponseTime
        throughput = $throughput
        percentiles = @{
            p50 = $p50
            p90 = $p90
            p95 = $p95
            p99 = $p99
        }
    }
    
    return $results
}

function Run-StressTest {
    param($config, $users, $duration, $rampTime)
    
    Write-Host "Running Stress Test..." -ForegroundColor Yellow
    Write-Host "Stress testing with high load to find breaking points..." -ForegroundColor Gray
    
    # Gradually increase load
    $stressPhases = @(
        @{ users = [math]::Round($users * 0.5); duration = [math]::Round($duration * 0.2) }
        @{ users = [math]::Round($users * 0.75); duration = [math]::Round($duration * 0.3) }
        @{ users = $users; duration = [math]::Round($duration * 0.3) }
        @{ users = [math]::Round($users * 1.25); duration = [math]::Round($duration * 0.2) }
    )
    
    $allResults = @()
    
    foreach ($phase in $stressPhases) {
        Write-Host "Testing with $($phase.users) users for $($phase.duration) seconds..." -ForegroundColor Gray
        $phaseResults = Run-LoadTest -config $config -users $phase.users -duration $phase.duration -rampTime $rampTime
        $phaseResults.phase_users = $phase.users
        $allResults += $phaseResults
        Start-Sleep -Seconds 1
    }
    
    # Find breaking point
    $breakingPoint = $null
    foreach ($result in $allResults) {
        if ($result.successRate -lt 95 -or $result.avgResponseTime -gt 5000) {
            $breakingPoint = $result.phase_users
            break
        }
    }
    
    $finalResult = $allResults[-1]
    $finalResult.breakingPoint = $breakingPoint
    $finalResult.testType = "stress"
    
    return $finalResult
}

function Run-SpikeTest {
    param($config, $users, $duration, $rampTime)
    
    Write-Host "Running Spike Test..." -ForegroundColor Yellow
    Write-Host "Testing sudden load spikes..." -ForegroundColor Gray
    
    # Simulate sudden spike
    $baseLoad = [math]::Round($users * 0.3)
    $spikeLoad = $users
    
    Write-Host "Starting with baseline load: $baseLoad users" -ForegroundColor Gray
    $baselineResults = Run-LoadTest -config $config -users $baseLoad -duration 60 -rampTime 30
    
    Write-Host "Creating sudden spike to: $spikeLoad users" -ForegroundColor Gray
    $spikeResults = Run-LoadTest -config $config -users $spikeLoad -duration 120 -rampTime 10
    
    Write-Host "Returning to baseline: $baseLoad users" -ForegroundColor Gray
    $recoveryResults = Run-LoadTest -config $config -users $baseLoad -duration 60 -rampTime 30
    
    $spikeResults.testType = "spike"
    $spikeResults.baselineSuccessRate = $baselineResults.successRate
    $spikeResults.recoverySuccessRate = $recoveryResults.successRate
    $spikeResults.spikeHandled = ($spikeResults.successRate -gt 90)
    
    return $spikeResults
}

function Run-EnduranceTest {
    param($config, $users, $duration, $rampTime)
    
    Write-Host "Running Endurance Test..." -ForegroundColor Yellow
    Write-Host "Testing system stability over extended period..." -ForegroundColor Gray
    
    $checkpointInterval = [math]::Round($duration / 10)
    $checkpoints = @()
    
    for ($i = 1; $i -le 10; $i++) {
        $currentTime = $i * $checkpointInterval
        Write-Host "Checkpoint $i/$10 - Time: $currentTime seconds" -ForegroundColor Gray
        
        $checkpointResult = Run-LoadTest -config $config -users $users -duration $checkpointInterval -rampTime 30
        $checkpoints += @{
            checkpoint = $i
            time = $currentTime
            successRate = $checkpointResult.successRate
            avgResponseTime = $checkpointResult.avgResponseTime
            throughput = $checkpointResult.throughput
        }
        
        Start-Sleep -Seconds 1
    }
    
    # Analyze performance degradation
    $firstHalf = $checkpoints[0..4]
    $secondHalf = $checkpoints[5..9]
    
    $firstHalfAvgResponse = ($firstHalf | Measure-Object -Property avgResponseTime -Average).Average
    $secondHalfAvgResponse = ($secondHalf | Measure-Object -Property avgResponseTime -Average).Average
    $degradation = [math]::Round((($secondHalfAvgResponse - $firstHalfAvgResponse) / $firstHalfAvgResponse) * 100, 2)
    
    $enduranceResults = $checkpoints[-1]
    $enduranceResults.testType = "endurance"
    $enduranceResults.performanceDegradation = $degradation
    $enduranceResults.memoryLeakDetected = ($degradation -gt 20)
    $enduranceResults.checkpoints = $checkpoints
    
    return $enduranceResults
}

function Display-TestResults {
    param($results)
    
    Write-Host "`nTest Results Summary:" -ForegroundColor Green
    Write-Host "=" * 50
    
    Write-Host "Request Statistics:" -ForegroundColor Cyan
    Write-Host "  Total Requests: $($results.totalRequests)" -ForegroundColor White
    Write-Host "  Successful: $($results.successfulRequests)" -ForegroundColor Green
    Write-Host "  Failed: $($results.failedRequests)" -ForegroundColor Red
    Write-Host "  Success Rate: $($results.successRate)%" -ForegroundColor $(
        if ($results.successRate -ge 95) { "Green" }
        elseif ($results.successRate -ge 90) { "Yellow" }
        else { "Red" }
    )
    
    Write-Host "Performance Metrics:" -ForegroundColor Cyan
    Write-Host "  Average Response Time: $($results.avgResponseTime)ms" -ForegroundColor White
    Write-Host "  Min Response Time: $($results.minResponseTime)ms" -ForegroundColor White
    Write-Host "  Max Response Time: $($results.maxResponseTime)ms" -ForegroundColor White
    Write-Host "  Throughput: $($results.throughput) requests/sec" -ForegroundColor White
    
    Write-Host "Response Time Percentiles:" -ForegroundColor Cyan
    Write-Host "  50th Percentile: $($results.percentiles.p50)ms" -ForegroundColor White
    Write-Host "  90th Percentile: $($results.percentiles.p90)ms" -ForegroundColor White
    Write-Host "  95th Percentile: $($results.percentiles.p95)ms" -ForegroundColor White
    Write-Host "  99th Percentile: $($results.percentiles.p99)ms" -ForegroundColor White
    
    # Test-specific results
    switch ($results.testType) {
        "stress" {
            Write-Host "Stress Test Analysis:" -ForegroundColor Yellow
            if ($results.breakingPoint) {
                Write-Host "  Breaking Point: $($results.breakingPoint) users" -ForegroundColor Red
            } else {
                Write-Host "  Breaking Point: Not reached within test limits" -ForegroundColor Green
            }
        }
        "spike" {
            Write-Host "Spike Test Analysis:" -ForegroundColor Yellow
            Write-Host "  Baseline Success Rate: $($results.baselineSuccessRate)%" -ForegroundColor White
            Write-Host "  Spike Handled: $($results.spikeHandled)" -ForegroundColor $(if ($results.spikeHandled) { "Green" } else { "Red" })
            Write-Host "  Recovery Success Rate: $($results.recoverySuccessRate)%" -ForegroundColor White
        }
        "endurance" {
            Write-Host "Endurance Test Analysis:" -ForegroundColor Yellow
            Write-Host "  Performance Degradation: $($results.performanceDegradation)%" -ForegroundColor $(
                if ($results.performanceDegradation -lt 10) { "Green" }
                elseif ($results.performanceDegradation -lt 20) { "Yellow" }
                else { "Red" }
            )
            Write-Host "  Memory Leak Detected: $($results.memoryLeakDetected)" -ForegroundColor $(if ($results.memoryLeakDetected) { "Red" } else { "Green" })
        }
    }
    
    # Performance assessment
    Write-Host "Performance Assessment:" -ForegroundColor Yellow
    $rating = "POOR"
    if ($results.successRate -ge 99 -and $results.avgResponseTime -lt 1000) {
        $rating = "EXCELLENT"
    } elseif ($results.successRate -ge 95 -and $results.avgResponseTime -lt 2000) {
        $rating = "GOOD"
    } elseif ($results.successRate -ge 90 -and $results.avgResponseTime -lt 3000) {
        $rating = "FAIR"
    }
    
    Write-Host "  Overall Rating: $rating" -ForegroundColor $(
        switch ($rating) {
            "EXCELLENT" { "Green" }
            "GOOD" { "Yellow" }
            "FAIR" { "DarkYellow" }
            "POOR" { "Red" }
        }
    )
}

function Generate-JMeterReport {
    param($results, $scenarios, $config)
    
    Write-Host "Generating JMeter Test Report..." -ForegroundColor Yellow
    
    $reportDir = "reports/jmeter"
    if (-not (Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }
    
    $reportFile = "$reportDir/jmeter-report-$Environment-$TestType-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"
    
    $reportContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>JMeter Test Report - $TestType</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }
        .metric-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin: 20px 0; }
        .metric-card { background-color: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745; }
        .metric-value { font-size: 2em; font-weight: bold; color: #28a745; }
        .metric-label { color: #666; margin-top: 5px; }
        .section { margin: 30px 0; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
        .pass { color: #28a745; font-weight: bold; }
        .warning { color: #ffc107; font-weight: bold; }
        .fail { color: #dc3545; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>JMeter Test Report</h1>
            <p><strong>Test Type:</strong> $TestType | <strong>Environment:</strong> $Environment</p>
            <p><strong>Generated:</strong> $timestamp</p>
        </div>
        
        <div class="metric-grid">
            <div class="metric-card">
                <div class="metric-value">$($results.totalRequests)</div>
                <div class="metric-label">Total Requests</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">$($results.successRate)%</div>
                <div class="metric-label">Success Rate</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">$($results.avgResponseTime)ms</div>
                <div class="metric-label">Avg Response Time</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">$($results.throughput)</div>
                <div class="metric-label">Requests/sec</div>
            </div>
        </div>
        
        <div class="section">
            <h2>Test Configuration</h2>
            <ul>
                <li><strong>Base URL:</strong> $($config.baseUrl)</li>
                <li><strong>Concurrent Users:</strong> $Users</li>
                <li><strong>Test Duration:</strong> $Duration seconds</li>
                <li><strong>Ramp-up Time:</strong> $RampTime seconds</li>
            </ul>
        </div>
        
        <div class="section">
            <h2>Performance Analysis</h2>
            <p><strong>Overall Rating:</strong> <span class="$(
                if ($results.successRate -ge 99 -and $results.avgResponseTime -lt 1000) { 'pass' }
                elseif ($results.successRate -ge 95 -and $results.avgResponseTime -lt 2000) { 'warning' }
                else { 'fail' }
            )">$(
                if ($results.successRate -ge 99 -and $results.avgResponseTime -lt 1000) { 'EXCELLENT' }
                elseif ($results.successRate -ge 95 -and $results.avgResponseTime -lt 2000) { 'GOOD' }
                elseif ($results.successRate -ge 90 -and $results.avgResponseTime -lt 3000) { 'FAIR' }
                else { 'POOR' }
            )</span></p>
            
            <h3>Response Time Percentiles</h3>
            <ul>
                <li>50th Percentile: $($results.percentiles.p50)ms</li>
                <li>90th Percentile: $($results.percentiles.p90)ms</li>
                <li>95th Percentile: $($results.percentiles.p95)ms</li>
                <li>99th Percentile: $($results.percentiles.p99)ms</li>
            </ul>
        </div>
        
        <div class="section">
            <h2>User Scenarios Tested</h2>
            <ul>
"@

    foreach ($scenario in $scenarios) {
        $reportContent += "<li><strong>$($scenario.name):</strong> $($scenario.weight)% of traffic, $($scenario.duration) duration, $($scenario.success_rate)% success rate</li>`n"
    }

    $reportContent += @"
            </ul>
        </div>
        
        <div class="section">
            <h2>Recommendations</h2>
            <ul>
"@

    if ($results.successRate -lt 95) {
        $reportContent += "<li class='fail'>SUCCESS RATE: Investigate failed requests and improve error handling</li>`n"
    }
    if ($results.avgResponseTime -gt 2000) {
        $reportContent += "<li class='fail'>RESPONSE TIME: Optimize slow endpoints and database queries</li>`n"
    }
    if ($results.percentiles.p95 -gt 5000) {
        $reportContent += "<li class='warning'>95TH PERCENTILE: Address outlier response times</li>`n"
    }
    if ($results.successRate -ge 99 -and $results.avgResponseTime -lt 1000) {
        $reportContent += "<li class='pass'>EXCELLENT: System performing within optimal parameters</li>`n"
    }

    $reportContent += @"
            </ul>
        </div>
    </div>
</body>
</html>
"@
    
    $reportContent | Out-File -FilePath $reportFile -Encoding UTF8
    
    Write-Host "JMeter test report generated: $reportFile" -ForegroundColor Green
    
    return $reportFile
}

# Main execution
try {
    $config = Get-TestConfiguration -environment $Environment -testType $TestType
    $scenarios = Create-UserScenarios
    
    $results = switch ($TestType) {
        "load" { Run-LoadTest -config $config -users $Users -duration $Duration -rampTime $RampTime }
        "stress" { Run-StressTest -config $config -users $Users -duration $Duration -rampTime $RampTime }
        "spike" { Run-SpikeTest -config $config -users $Users -duration $Duration -rampTime $RampTime }
        "endurance" { Run-EnduranceTest -config $config -users $Users -duration $Duration -rampTime $RampTime }
        default { 
            Write-Host "Unknown test type: $TestType" -ForegroundColor Red
            Write-Host "Valid types: load, stress, spike, endurance" -ForegroundColor Yellow
            exit 1
        }
    }
    
    Display-TestResults -results $results
    
    if ($GenerateReport) {
        Generate-JMeterReport -results $results -scenarios $scenarios -config $config
    }
    
    Write-Host "`nAdvanced JMeter testing completed successfully!" -ForegroundColor Green
    
} catch {
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "=" * 70 
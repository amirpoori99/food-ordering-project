#!/usr/bin/env pwsh

# Food Ordering System - Load Testing & Stress Testing
# Phase 46: Performance Optimization & Load Testing

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("load", "stress", "spike", "endurance")]
    [string]$TestType = "load",
    
    [Parameter(Mandatory=$false)]
    [int]$ConcurrentUsers = 10,
    
    [Parameter(Mandatory=$false)]
    [int]$DurationMinutes = 5,
    
    [Parameter(Mandatory=$false)]
    [int]$RampUpSeconds = 30,
    
    [Parameter(Mandatory=$false)]
    [string]$BaseUrl = "http://localhost:8080",
    
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport
)

$ErrorActionPreference = "Stop"
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$TestResults = @{
    StartTime = Get-Date
    EndTime = $null
        TotalRequests = 0
        SuccessfulRequests = 0
        FailedRequests = 0
    AverageResponseTime = 0
    MaxResponseTime = 0
    MinResponseTime = 999999
    ResponseTimes = @()
    Errors = @()
    ThroughputRPS = 0
}

# Create directories
$directories = @("logs", "reports", "reports\load-testing")
foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -Path $dir -ItemType Directory -Force | Out-Null
    }
}

$LogFile = "logs\load-testing-$Environment-$TestType-$TimeStamp.log"
$ReportFile = "reports\load-testing\load-test-$Environment-$TestType-$TimeStamp.json"

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

# Test Configuration
$TestConfig = @{
    load = @{
        Description = "Normal load testing"
        Users = $ConcurrentUsers
        Duration = $DurationMinutes
        RampUp = $RampUpSeconds
    }
    stress = @{
        Description = "Stress testing with high load"
        Users = $ConcurrentUsers * 2
        Duration = $DurationMinutes
        RampUp = $RampUpSeconds / 2
    }
    spike = @{
        Description = "Spike testing with sudden load increase"
        Users = $ConcurrentUsers * 3
        Duration = $DurationMinutes / 2
        RampUp = 5
    }
    endurance = @{
        Description = "Endurance testing for extended period"
        Users = $ConcurrentUsers
        Duration = $DurationMinutes * 4
        RampUp = $RampUpSeconds * 2
    }
}

# API Endpoints to Test
$ApiEndpoints = @(
    @{
        Name = "Health Check"
        Method = "GET"
        Path = "/health"
        ExpectedStatus = 200
        Weight = 10
    },
    @{
        Name = "Login"
        Method = "POST"
        Path = "/api/auth/login"
        Body = @{
            username = "testuser"
            password = "testpass"
        }
        ExpectedStatus = 200
        Weight = 15
    },
    @{
        Name = "Get Restaurants"
        Method = "GET"
        Path = "/api/restaurants"
        ExpectedStatus = 200
        Weight = 20
    },
    @{
        Name = "Get Menu Items"
        Method = "GET"
        Path = "/api/menu/1"
        ExpectedStatus = 200
        Weight = 25
    },
    @{
        Name = "Get Orders"
        Method = "GET"
        Path = "/api/orders"
        ExpectedStatus = 200
        Weight = 15
    },
    @{
        Name = "Create Order"
        Method = "POST"
        Path = "/api/orders"
        Body = @{
            restaurantId = 1
            items = @(
                @{
                    itemId = 1
                    quantity = 2
                }
            )
        }
        ExpectedStatus = 201
        Weight = 15
    }
)

# HTTP Request Function
function Invoke-LoadTestRequest {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Body = $null,
        [int]$TimeoutSeconds = 30
    )
    
    try {
        $startTime = Get-Date
        
        $requestParams = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = $TimeoutSeconds
            ErrorAction = "Stop"
        }
        
        if ($Body) {
            $requestParams.Body = ($Body | ConvertTo-Json -Depth 3)
            $requestParams.ContentType = "application/json"
        }
        
        $response = Invoke-RestMethod @requestParams
        $endTime = Get-Date
        $responseTime = ($endTime - $startTime).TotalMilliseconds
        
        return @{
            Success = $true
            StatusCode = 200
            ResponseTime = $responseTime
            Error = $null
            Response = $response
        }
        
    } catch {
        $endTime = Get-Date
        $responseTime = ($endTime - $startTime).TotalMilliseconds
        
        return @{
            Success = $false
            StatusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { 0 }
            ResponseTime = $responseTime
            Error = $_.Exception.Message
            Response = $null
        }
    }
}

# Single User Load Test
function Start-SingleUserTest {
    param(
        [int]$UserId,
        [int]$DurationSeconds
    )
    
    $userResults = @{
        UserId = $UserId
        Requests = 0
        Successes = 0
        Failures = 0
        ResponseTimes = @()
        Errors = @()
    }
    
    $endTime = (Get-Date).AddSeconds($DurationSeconds)
    $requestCount = 0
    
    while ((Get-Date) -lt $endTime) {
        # Select random endpoint based on weight
        $totalWeight = ($ApiEndpoints | Measure-Object -Property Weight -Sum).Sum
        $randomValue = Get-Random -Minimum 1 -Maximum $totalWeight
        $cumulativeWeight = 0
        
        foreach ($endpoint in $ApiEndpoints) {
            $cumulativeWeight += $endpoint.Weight
            if ($randomValue -le $cumulativeWeight) {
                $selectedEndpoint = $endpoint
                break
            }
        }
        
        $requestCount++
        $url = $BaseUrl + $selectedEndpoint.Path
        
        $result = Invoke-LoadTestRequest -Method $selectedEndpoint.Method -Url $url -Body $selectedEndpoint.Body
        
        $userResults.Requests++
        $userResults.ResponseTimes += $result.ResponseTime
        
        if ($result.Success) {
            $userResults.Successes++
        } else {
            $userResults.Failures++
            $userResults.Errors += @{
                Endpoint = $selectedEndpoint.Name
                Error = $result.Error
                Time = Get-Date
            }
        }
        
        # Small delay to simulate real user behavior
        Start-Sleep -Milliseconds (Get-Random -Minimum 100 -Maximum 500)
    }
    
    return $userResults
}

# Multi-User Load Test
function Start-LoadTest {
    param(
        [int]$UserCount,
        [int]$DurationSeconds,
        [int]$RampUpSeconds
    )
    
    Write-Log "Starting load test with $UserCount users for $DurationSeconds seconds" "TEST"
    
    # Calculate ramp-up delay
    $rampUpDelay = if ($UserCount -gt 1) { $RampUpSeconds / $UserCount } else { 0 }
    
    # Start user jobs
    $jobs = @()
    for ($i = 1; $i -le $UserCount; $i++) {
        Write-Log "Starting user $i/$UserCount" "TEST"
        
        # Start user test as background job
        $job = Start-Job -ScriptBlock {
            param($UserId, $DurationSeconds, $BaseUrl, $ApiEndpoints)
            
            # Recreate functions in job scope
            function Invoke-LoadTestRequest {
                param(
                    [string]$Method,
                    [string]$Url,
                    [hashtable]$Body = $null,
                    [int]$TimeoutSeconds = 30
                )
                
                try {
                    $startTime = Get-Date
                    
                    $requestParams = @{
                        Uri = $Url
                        Method = $Method
                        TimeoutSec = $TimeoutSeconds
                        ErrorAction = "Stop"
                    }
                    
                    if ($Body) {
                        $requestParams.Body = ($Body | ConvertTo-Json -Depth 3)
                        $requestParams.ContentType = "application/json"
                    }
                    
                    $response = Invoke-RestMethod @requestParams
                    $endTime = Get-Date
                    $responseTime = ($endTime - $startTime).TotalMilliseconds
                    
                    return @{
                        Success = $true
                        StatusCode = 200
                        ResponseTime = $responseTime
                        Error = $null
                        Response = $response
                    }
                    
                } catch {
                    $endTime = Get-Date
                    $responseTime = ($endTime - $startTime).TotalMilliseconds
                    
                    return @{
                        Success = $false
                        StatusCode = if ($_.Exception.Response) { $_.Exception.Response.StatusCode } else { 0 }
                        ResponseTime = $responseTime
                        Error = $_.Exception.Message
                        Response = $null
                    }
                }
            }
            
            # Single user test logic
            $userResults = @{
                UserId = $UserId
                Requests = 0
                Successes = 0
                Failures = 0
                ResponseTimes = @()
                Errors = @()
            }
            
            $endTime = (Get-Date).AddSeconds($DurationSeconds)
            
            while ((Get-Date) -lt $endTime) {
                # Select random endpoint based on weight
                $totalWeight = ($ApiEndpoints | Measure-Object -Property Weight -Sum).Sum
                $randomValue = Get-Random -Minimum 1 -Maximum $totalWeight
                $cumulativeWeight = 0
                
                foreach ($endpoint in $ApiEndpoints) {
                    $cumulativeWeight += $endpoint.Weight
                    if ($randomValue -le $cumulativeWeight) {
                        $selectedEndpoint = $endpoint
                        break
                    }
                }
                
                $url = $BaseUrl + $selectedEndpoint.Path
                
                $result = Invoke-LoadTestRequest -Method $selectedEndpoint.Method -Url $url -Body $selectedEndpoint.Body
                
                $userResults.Requests++
                $userResults.ResponseTimes += $result.ResponseTime
                
                if ($result.Success) {
                    $userResults.Successes++
                } else {
                    $userResults.Failures++
                    $userResults.Errors += @{
                        Endpoint = $selectedEndpoint.Name
                        Error = $result.Error
                        Time = Get-Date
                    }
                }
                
                # Small delay to simulate real user behavior
                Start-Sleep -Milliseconds (Get-Random -Minimum 100 -Maximum 500)
            }
            
            return $userResults
            
        } -ArgumentList $i, $DurationSeconds, $BaseUrl, $ApiEndpoints
        
        $jobs += $job
        
        # Ramp-up delay
        if ($rampUpDelay -gt 0) {
            Start-Sleep -Seconds $rampUpDelay
        }
    }
    
    Write-Log "All users started. Waiting for test completion..." "TEST"
    
    # Wait for all jobs to complete
    $allResults = @()
    foreach ($job in $jobs) {
        $result = Receive-Job -Job $job -Wait
        $allResults += $result
        Remove-Job -Job $job
    }
    
    return $allResults
}

# Analyze Test Results
function Get-TestAnalysis {
    param($UserResults)
    
    $totalRequests = ($UserResults | Measure-Object -Property Requests -Sum).Sum
    $totalSuccesses = ($UserResults | Measure-Object -Property Successes -Sum).Sum
    $totalFailures = ($UserResults | Measure-Object -Property Failures -Sum).Sum
    
    $allResponseTimes = @()
    $allErrors = @()
    
    foreach ($user in $UserResults) {
        $allResponseTimes += $user.ResponseTimes
        $allErrors += $user.Errors
    }
    
    $TestResults.TotalRequests = $totalRequests
    $TestResults.SuccessfulRequests = $totalSuccesses
    $TestResults.FailedRequests = $totalFailures
    $TestResults.ResponseTimes = $allResponseTimes
    $TestResults.Errors = $allErrors
    
    if ($allResponseTimes.Count -gt 0) {
        $TestResults.AverageResponseTime = [math]::Round(($allResponseTimes | Measure-Object -Average).Average, 2)
        $TestResults.MaxResponseTime = [math]::Round(($allResponseTimes | Measure-Object -Maximum).Maximum, 2)
        $TestResults.MinResponseTime = [math]::Round(($allResponseTimes | Measure-Object -Minimum).Minimum, 2)
    }
    
    $TestResults.EndTime = Get-Date
    $testDurationSeconds = ($TestResults.EndTime - $TestResults.StartTime).TotalSeconds
    
    if ($testDurationSeconds -gt 0) {
        $TestResults.ThroughputRPS = [math]::Round($totalRequests / $testDurationSeconds, 2)
    }
    
    return $TestResults
}

# Generate Test Report
function New-LoadTestReport {
    param($TestResults, $TestConfig)
    
    $report = @{
        TestInfo = @{
            Environment = $Environment
            TestType = $TestType
            Configuration = $TestConfig
            StartTime = $TestResults.StartTime
            EndTime = $TestResults.EndTime
            Duration = ($TestResults.EndTime - $TestResults.StartTime).TotalMinutes
        }
        Results = $TestResults
        Summary = @{
            SuccessRate = if ($TestResults.TotalRequests -gt 0) { [math]::Round(($TestResults.SuccessfulRequests / $TestResults.TotalRequests) * 100, 2) } else { 0 }
            ErrorRate = if ($TestResults.TotalRequests -gt 0) { [math]::Round(($TestResults.FailedRequests / $TestResults.TotalRequests) * 100, 2) } else { 0 }
            AverageResponseTime = $TestResults.AverageResponseTime
            Throughput = $TestResults.ThroughputRPS
        }
        Recommendations = @()
    }
    
    # Add recommendations based on results
    if ($report.Summary.SuccessRate -lt 95) {
        $report.Recommendations += "Low success rate detected ($($report.Summary.SuccessRate)%). Investigate application stability."
    }
    
    if ($TestResults.AverageResponseTime -gt 2000) {
        $report.Recommendations += "High average response time ($($TestResults.AverageResponseTime)ms). Consider performance optimization."
    }
    
    if ($TestResults.ThroughputRPS -lt 10) {
        $report.Recommendations += "Low throughput ($($TestResults.ThroughputRPS) RPS). Review application capacity."
    }
    
    # Save report
    $report | ConvertTo-Json -Depth 5 | Out-File -FilePath $ReportFile -Encoding UTF8
    
    return $report
}

# Main Load Testing Process
function Start-LoadTestProcess {
    Write-Log "Starting load test process..." "INFO"
    
    # Get test configuration
    $config = $TestConfig[$TestType]
    
    Write-Log "Test Type: $TestType" "INFO"
    Write-Log "Description: $($config.Description)" "INFO"
    Write-Log "Users: $($config.Users)" "INFO"
    Write-Log "Duration: $($config.Duration) minutes" "INFO"
    Write-Log "Ramp-up: $($config.RampUp) seconds" "INFO"
    Write-Log "Base URL: $BaseUrl" "INFO"
    
    # Check if application is running
    try {
        $healthCheck = Invoke-LoadTestRequest -Method "GET" -Url "$BaseUrl/health" -TimeoutSeconds 5
        if (-not $healthCheck.Success) {
            Write-Log "Application health check failed. Starting basic connectivity test..." "WARNING"
            
            # Try a simple connection test
            try {
                $response = Invoke-WebRequest -Uri $BaseUrl -TimeoutSec 5 -ErrorAction Stop
                Write-Log "Application is reachable but health endpoint failed" "WARNING"
            } catch {
                Write-Log "Application is not reachable at $BaseUrl" "ERROR"
                Write-Log "Please ensure the application is running before load testing" "ERROR"
                return
            }
        } else {
            Write-Log "Application health check passed" "SUCCESS"
        }
    } catch {
        Write-Log "Health check failed: $($_.Exception.Message)" "WARNING"
        Write-Log "Proceeding with load test anyway..." "INFO"
    }
    
    # Start load test
    $durationSeconds = $config.Duration * 60
    $userResults = Start-LoadTest -UserCount $config.Users -DurationSeconds $durationSeconds -RampUpSeconds $config.RampUp
    
    # Analyze results
    $testResults = Get-TestAnalysis -UserResults $userResults
    
    # Display results
    Write-Log "Load test completed!" "SUCCESS"
    Write-Log "===================" "SUCCESS"
    Write-Log "Total Requests: $($testResults.TotalRequests)" "INFO"
    Write-Log "Successful Requests: $($testResults.SuccessfulRequests)" "INFO"
    Write-Log "Failed Requests: $($testResults.FailedRequests)" "INFO"
    Write-Log "Success Rate: $([math]::Round(($testResults.SuccessfulRequests / $testResults.TotalRequests) * 100, 2))%" "INFO"
    Write-Log "Average Response Time: $($testResults.AverageResponseTime)ms" "INFO"
    Write-Log "Max Response Time: $($testResults.MaxResponseTime)ms" "INFO"
    Write-Log "Min Response Time: $($testResults.MinResponseTime)ms" "INFO"
    Write-Log "Throughput: $($testResults.ThroughputRPS) requests/second" "INFO"
    
    if ($testResults.Errors.Count -gt 0) {
        Write-Log "Errors encountered: $($testResults.Errors.Count)" "WARNING"
        foreach ($error in $testResults.Errors | Select-Object -First 5) {
            Write-Log "  - $($error.Endpoint): $($error.Error)" "WARNING"
        }
    }
    
    # Generate report if requested
    if ($GenerateReport) {
        $report = New-LoadTestReport -TestResults $testResults -TestConfig $config
        Write-Log "Test report generated: $ReportFile" "SUCCESS"
        
        if ($report.Recommendations.Count -gt 0) {
            Write-Log "Recommendations:" "INFO"
            foreach ($rec in $report.Recommendations) {
                Write-Log "  - $rec" "INFO"
            }
        }
    }
    
    return $testResults
}

# Main execution
Write-Host "Food Ordering System - Load Testing" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

$results = Start-LoadTestProcess

Write-Log "Load testing completed successfully" "SUCCESS"
Write-Log "Log file: $LogFile" "INFO"

return $results

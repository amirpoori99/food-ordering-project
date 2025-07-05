# Food Ordering System - Vulnerability Scanner
# Phase 44: Security Hardening

Write-Host "Food Ordering Vulnerability Scanner" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan

$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"

# Counters
$CriticalIssues = 0
$HighIssues = 0
$MediumIssues = 0
$LowIssues = 0
$AllIssues = @()

function Add-Issue {
    param($Category, $Title, $Level, $File, $Fix)
    
    $issue = [PSCustomObject]@{
        Category = $Category
        Title = $Title
        Level = $Level
        File = $File
        Fix = $Fix
    }
    
    $script:AllIssues += $issue
    
    switch ($Level) {
        "CRITICAL" { $script:CriticalIssues++; $color = "Red" }
        "HIGH" { $script:HighIssues++; $color = "Red" }
        "MEDIUM" { $script:MediumIssues++; $color = "Yellow" }
        "LOW" { $script:LowIssues++; $color = "Gray" }
    }
    
    Write-Host "WARNING [$Level] $Title" -ForegroundColor $color
}

# Check Database Security
Write-Host "`nChecking Database..." -ForegroundColor Yellow

$dbFile = "$ProjectRoot\backend\food_ordering.db"
if (Test-Path $dbFile) {
    $dbBytes = [System.IO.File]::ReadAllBytes($dbFile)[0..15]
    $dbHeader = [System.Text.Encoding]::ASCII.GetString($dbBytes)
    
    if ($dbHeader.StartsWith("SQLite format")) {
        Add-Issue "Database" "Unencrypted Database" "HIGH" $dbFile "Implement database encryption"
    }
    
    Write-Host "Database checked" -ForegroundColor Green
} else {
    Add-Issue "Database" "Database Missing" "CRITICAL" $dbFile "Restore database file"
}

# Check Configuration Files  
Write-Host "`nChecking Configuration..." -ForegroundColor Yellow

$configs = @(
    "$ProjectRoot\backend\src\main\resources\hibernate.cfg.xml",
    "$ProjectRoot\backend\src\main\resources\hibernate-production.cfg.xml"
)

foreach ($config in $configs) {
    if (Test-Path $config) {
        $content = Get-Content $config -Raw
        
        if ($content -match 'password.*=.*"(.+)"') {
            Add-Issue "Config" "Hardcoded Password" "CRITICAL" $config "Use environment variables for passwords"
        }
        
        if ($config -like "*production*" -and $content -match "http://") {
            Add-Issue "Config" "HTTP in Production" "HIGH" $config "Use HTTPS for all production URLs"
        }
    }
}

Write-Host "Configuration checked" -ForegroundColor Green

# Check Java Source Code
Write-Host "`nChecking Java Code..." -ForegroundColor Yellow

$javaFiles = Get-ChildItem "$ProjectRoot\backend\src" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
$javaCount = 0

foreach ($javaFile in $javaFiles) {
    $javaCount++
    $lines = Get-Content $javaFile.FullName
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $lineNum = $i + 1
        
        # SQL Injection check
        if ($line -match "String.*=.*\+.*" -and $line -match "SELECT|INSERT|UPDATE|DELETE") {
            Add-Issue "Security" "SQL Injection Risk" "CRITICAL" "$($javaFile.FullName):$lineNum" "Use PreparedStatement instead of string concatenation"
        }
        
        # Hard-coded passwords
        if ($line -match 'password.*=.*"(.+)"') {
            Add-Issue "Security" "Hard-coded Password" "CRITICAL" "$($javaFile.FullName):$lineNum" "Use secure configuration for credentials"
        }
        
        # Debug code
        if ($line -match "System\.out\.print|printStackTrace") {
            Add-Issue "Code Quality" "Debug Code" "LOW" "$($javaFile.FullName):$lineNum" "Remove debug statements and use proper logging"
        }
        
        # Weak crypto
        if ($line -match "MD5|SHA1|DES") {
            Add-Issue "Crypto" "Weak Cryptography" "HIGH" "$($javaFile.FullName):$lineNum" "Use stronger algorithms like SHA-256 or AES-256"
        }
    }
}

Write-Host "$javaCount Java files scanned" -ForegroundColor Green

# Check PowerShell Scripts
Write-Host "`nChecking Scripts..." -ForegroundColor Yellow

$scriptFiles = Get-ChildItem "$ProjectRoot\scripts" -Filter "*.ps1" -ErrorAction SilentlyContinue
$scriptCount = 0

foreach ($scriptFile in $scriptFiles) {
    if ($scriptFile.Name -eq "vuln-scan.ps1") { continue }
    
    $scriptCount++
    $lines = Get-Content $scriptFile.FullName
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $lineNum = $i + 1
        
        # Execution policy bypass
        if ($line -match "-ExecutionPolicy.*Bypass") {
            Add-Issue "Script Security" "Execution Policy Bypass" "MEDIUM" "$($scriptFile.FullName):$lineNum" "Use proper execution policy instead of bypass"
        }
        
        # Command injection risk
        if ($line -match "Invoke-Expression|iex") {
            Add-Issue "Script Security" "Command Injection Risk" "HIGH" "$($scriptFile.FullName):$lineNum" "Avoid dynamic code execution"
        }
    }
}

Write-Host "$scriptCount PowerShell scripts scanned" -ForegroundColor Green

# Check for missing security features
Write-Host "`nChecking Security Features..." -ForegroundColor Yellow

# Check for authentication logging
$logConfig = "$ProjectRoot\backend\src\main\resources\logback.xml"
if (Test-Path $logConfig) {
    $logContent = Get-Content $logConfig -Raw
    
    if (-not ($logContent -match "authentication" -or $logContent -match "login")) {
        Add-Issue "Logging" "Missing Auth Logging" "MEDIUM" $logConfig "Add authentication event logging"
    }
} else {
    Add-Issue "Configuration" "Missing Log Config" "MEDIUM" $logConfig "Configure proper logging"
}

# Show Results
Write-Host "`n" + "="*50 -ForegroundColor Cyan
Write-Host "VULNERABILITY SCAN RESULTS" -ForegroundColor Cyan
Write-Host "="*50 -ForegroundColor Cyan

Write-Host "Total Issues Found: $($AllIssues.Count)" -ForegroundColor White
Write-Host "Critical: $CriticalIssues" -ForegroundColor Red
Write-Host "High: $HighIssues" -ForegroundColor Red
Write-Host "Medium: $MediumIssues" -ForegroundColor Yellow
Write-Host "Low: $LowIssues" -ForegroundColor Gray

$riskLevel = if ($CriticalIssues -gt 0) { "CRITICAL" }
             elseif ($HighIssues -gt 0) { "HIGH" }
             elseif ($MediumIssues -gt 0) { "MEDIUM" }
             else { "LOW" }

Write-Host "`nOverall Risk: $riskLevel" -ForegroundColor $(
    switch ($riskLevel) {
        "CRITICAL" { "Red" }
        "HIGH" { "Red" }
        "MEDIUM" { "Yellow" }
        "LOW" { "Green" }
    }
)

Write-Host "="*50 -ForegroundColor Cyan

# Generate Report
if (-not (Test-Path "logs")) {
    New-Item -Path "logs" -ItemType Directory -Force | Out-Null
}

$reportPath = "logs\vulnerability-scan-$TimeStamp.md"

$currentDate = Get-Date
$nextScanDate = Get-Date -Date $currentDate.AddDays(7) -Format "yyyy-MM-dd"

$report = "# Vulnerability Scan Report`n`n"
$report += "**Date**: $currentDate`n"
$report += "**Total Issues**: $($AllIssues.Count)`n"
$report += "**Risk Level**: $riskLevel`n`n"

$report += "## Summary`n"
$report += "- Critical: $CriticalIssues`n"
$report += "- High: $HighIssues`n"
$report += "- Medium: $MediumIssues`n"
$report += "- Low: $LowIssues`n`n"

$report += "## Issues Found`n"

foreach ($issue in $AllIssues) {
    $report += "`n### $($issue.Title) [$($issue.Level)]`n"
    $report += "**Category**: $($issue.Category)`n"
    $report += "**Location**: $($issue.File)`n"
    $report += "**Recommendation**: $($issue.Fix)`n"
}

$report += "`n## Priority Actions`n"

$criticalItems = $AllIssues | Where-Object { $_.Level -eq "CRITICAL" }
if ($criticalItems.Count -gt 0) {
    $report += "`n### Critical Issues`n"
    foreach ($item in $criticalItems) {
        $report += "- **$($item.Title)**: $($item.Fix)`n"
    }
}

$highItems = $AllIssues | Where-Object { $_.Level -eq "HIGH" }
if ($highItems.Count -gt 0) {
    $report += "`n### High Priority Issues`n"
    foreach ($item in $highItems) {
        $report += "- $($item.Title): $($item.Fix)`n"
    }
}

$report += "`n---`n"
$report += "**Next Scan Recommended**: $nextScanDate`n"

$report | Out-File $reportPath -Encoding UTF8

Write-Host "`nReport saved: $reportPath" -ForegroundColor Green

# Exit with appropriate code
if ($CriticalIssues -gt 0) { exit 4 }
elseif ($HighIssues -gt 0) { exit 3 }
elseif ($MediumIssues -gt 0) { exit 2 }
else { exit 0 } 
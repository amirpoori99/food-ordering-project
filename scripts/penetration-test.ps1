# Food Ordering System - Penetration Testing Script
# Phase 44: Security Hardening & Penetration Testing

Write-Host "Food Ordering System - Penetration Test" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan

$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"

# Test Results
$PenTestResults = @()
$CriticalFindings = 0
$HighFindings = 0
$MediumFindings = 0
$LowFindings = 0

function Add-PenTestResult {
    param($TestName, $Result, $Severity, $Details, $Recommendation)
    
    $finding = [PSCustomObject]@{
        TestName = $TestName
        Result = $Result
        Severity = $Severity
        Details = $Details
        Recommendation = $Recommendation
        Timestamp = Get-Date
    }
    
    $script:PenTestResults += $finding
    
    switch ($Severity) {
        "CRITICAL" { $script:CriticalFindings++; $color = "Red" }
        "HIGH" { $script:HighFindings++; $color = "Red" }
        "MEDIUM" { $script:MediumFindings++; $color = "Yellow" }
        "LOW" { $script:LowFindings++; $color = "Gray" }
        "INFO" { $color = "Green" }
    }
    
    Write-Host "[$Severity] $TestName - $Result" -ForegroundColor $color
    if ($Details) {
        Write-Host "  Details: $Details" -ForegroundColor Gray
    }
}

# Test 1: Authentication Security
Write-Host "`nTesting Authentication Security..." -ForegroundColor Yellow

$authFiles = Get-ChildItem "$ProjectRoot\backend\src" -Filter "*Auth*" -Recurse -ErrorAction SilentlyContinue
$weakAuthFound = $false

foreach ($authFile in $authFiles) {
    $content = Get-Content $authFile.FullName -Raw
    
    if ($content -match 'password.*=.*"(.+)"') {
        Add-PenTestResult "Authentication Bypass" "VULNERABLE" "CRITICAL" `
            "Hardcoded credentials found in $($authFile.Name)" `
            "Remove hardcoded credentials and use secure authentication"
        $weakAuthFound = $true
    }
    
    if ($content -match "password.*length.*[1-7]") {
        Add-PenTestResult "Weak Password Policy" "VULNERABLE" "HIGH" `
            "Minimum password length is too short in $($authFile.Name)" `
            "Enforce strong password policy (min 8 chars)"
        $weakAuthFound = $true
    }
}

if (-not $weakAuthFound) {
    Add-PenTestResult "Authentication Security" "SECURE" "INFO" `
        "No obvious authentication vulnerabilities detected" ""
}

# Test 2: SQL Injection Testing
Write-Host "`nTesting SQL Injection Vulnerabilities..." -ForegroundColor Yellow

$sqlInjectionFound = $false
$javaFiles = Get-ChildItem "$ProjectRoot\backend\src" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue

foreach ($javaFile in $javaFiles) {
    $lines = Get-Content $javaFile.FullName
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        
        if ($line -match "String.*sql.*=.*\+.*" -and $line -match "SELECT|INSERT|UPDATE|DELETE") {
            Add-PenTestResult "SQL Injection" "VULNERABLE" "CRITICAL" `
                "SQL injection vulnerability in $($javaFile.Name) at line $($i+1)" `
                "Use PreparedStatement to prevent SQL injection"
            $sqlInjectionFound = $true
        }
    }
}

if (-not $sqlInjectionFound) {
    Add-PenTestResult "SQL Injection Testing" "SECURE" "INFO" `
        "No SQL injection vulnerabilities detected" ""
}

# Test 3: Input Validation
Write-Host "`nTesting Input Validation..." -ForegroundColor Yellow

$inputVulnFound = $false

foreach ($javaFile in $javaFiles) {
    $content = Get-Content $javaFile.FullName -Raw
    
    if ($content -match "@RequestParam.*String" -and $content -notmatch "validate|sanitize|escape") {
        Add-PenTestResult "Input Validation" "VULNERABLE" "HIGH" `
            "User input not validated in $($javaFile.Name)" `
            "Implement input validation and sanitization"
        $inputVulnFound = $true
    }
    
    if ($content -match "innerHTML|document\.write" -and $content -notmatch "escape|encode") {
        Add-PenTestResult "XSS Vulnerability" "VULNERABLE" "MEDIUM" `
            "Potential XSS vulnerability in $($javaFile.Name)" `
            "Encode output and validate input"
        $inputVulnFound = $true
    }
}

if (-not $inputVulnFound) {
    Add-PenTestResult "Input Validation" "SECURE" "INFO" `
        "No obvious input validation issues detected" ""
}

# Test 4: Authorization Testing
Write-Host "`nTesting Authorization Controls..." -ForegroundColor Yellow

$authzVulnFound = $false

foreach ($javaFile in $javaFiles) {
    $content = Get-Content $javaFile.FullName -Raw
    
    if (($content -match "@RequestMapping") -or ($content -match "@PostMapping") -or ($content -match "@GetMapping")) {
        if (($content -notmatch "@PreAuthorize") -and ($content -notmatch "@Secured") -and ($content -notmatch "@RolesAllowed")) {
            Add-PenTestResult "Missing Authorization" "VULNERABLE" "HIGH" `
                "Endpoint without authorization checks in $($javaFile.Name)" `
                "Add proper authorization to all endpoints"
            $authzVulnFound = $true
        }
    }
    
    if ($content -match "setRole|updateRole|assignRole" -and $content -notmatch "hasRole.*ADMIN") {
        Add-PenTestResult "Privilege Escalation" "VULNERABLE" "CRITICAL" `
            "Potential privilege escalation in $($javaFile.Name)" `
            "Restrict role modification to administrators only"
        $authzVulnFound = $true
    }
}

if (-not $authzVulnFound) {
    Add-PenTestResult "Authorization Testing" "SECURE" "INFO" `
        "Authorization controls appear to be in place" ""
}

# Test 5: Database Security
Write-Host "`nTesting Database Security..." -ForegroundColor Yellow

$dbPath = "$ProjectRoot\backend\food_ordering.db"
if (Test-Path $dbPath) {
    $dbBytes = [System.IO.File]::ReadAllBytes($dbPath)[0..15]
    $dbHeader = [System.Text.Encoding]::ASCII.GetString($dbBytes)
    
    if ($dbHeader.StartsWith("SQLite format")) {
        Add-PenTestResult "Database Encryption" "VULNERABLE" "HIGH" `
            "Database is not encrypted" `
            "Implement database encryption using SQLCipher"
    }
    
    $dbAcl = Get-Acl $dbPath
    $publicAccess = $dbAcl.Access | Where-Object { $_.IdentityReference -eq "Everyone" }
    
    if ($publicAccess) {
        Add-PenTestResult "Database Permissions" "VULNERABLE" "HIGH" `
            "Database accessible by all users" `
            "Restrict database access to application only"
    }
} else {
    Add-PenTestResult "Database Availability" "VULNERABLE" "CRITICAL" `
        "Database file not found" `
        "Ensure database is properly configured and accessible"
}

# Test 6: Configuration Security
Write-Host "`nTesting Configuration Security..." -ForegroundColor Yellow

$configFiles = @(
    "$ProjectRoot\backend\src\main\resources\hibernate.cfg.xml",
    "$ProjectRoot\backend\src\main\resources\hibernate-production.cfg.xml"
)

$configVulnFound = $false

foreach ($configFile in $configFiles) {
    if (Test-Path $configFile) {
        $content = Get-Content $configFile -Raw
        
        if ($configFile -like "*production*") {
            if ($content -match "show_sql.*true|format_sql.*true") {
                Add-PenTestResult "Debug Info in Production" "VULNERABLE" "MEDIUM" `
                    "Debug information enabled in production config" `
                    "Disable debug features in production"
                $configVulnFound = $true
            }
            
            if ($content -match "http://") {
                Add-PenTestResult "HTTP in Production" "VULNERABLE" "HIGH" `
                    "HTTP URLs found in production configuration" `
                    "Use HTTPS for all production communications"
                $configVulnFound = $true
            }
        }
    }
}

if (-not $configVulnFound) {
    Add-PenTestResult "Configuration Security" "SECURE" "INFO" `
        "Configuration appears secure" ""
}

# Test 7: Cryptographic Implementation
Write-Host "`nTesting Cryptographic Implementation..." -ForegroundColor Yellow

$cryptoVulnFound = $false

foreach ($javaFile in $javaFiles) {
    $content = Get-Content $javaFile.FullName -Raw
    
    if (($content -match "MD5") -or ($content -match "SHA1") -or ($content -match "DES") -or ($content -match "RC4")) {
        Add-PenTestResult "Weak Cryptography" "VULNERABLE" "HIGH" `
            "Weak cryptographic algorithms in $($javaFile.Name)" `
            "Use strong algorithms like AES-256, SHA-256"
        $cryptoVulnFound = $true
    }
    
    if ($content -match 'key.*=.*"[A-Za-z0-9+/]{16,}"') {
        Add-PenTestResult "Hardcoded Crypto Key" "VULNERABLE" "CRITICAL" `
            "Hardcoded cryptographic key in $($javaFile.Name)" `
            "Use proper key management system"
        $cryptoVulnFound = $true
    }
}

if (-not $cryptoVulnFound) {
    Add-PenTestResult "Cryptographic Implementation" "SECURE" "INFO" `
        "No obvious cryptographic issues detected" ""
}

# Generate Summary
Write-Host "`n" + "="*60 -ForegroundColor Cyan
Write-Host "PENETRATION TEST SUMMARY" -ForegroundColor Cyan
Write-Host "="*60 -ForegroundColor Cyan

Write-Host "Total Tests Performed: $($PenTestResults.Count)" -ForegroundColor White
Write-Host "Critical Findings: $CriticalFindings" -ForegroundColor Red
Write-Host "High Findings: $HighFindings" -ForegroundColor Red
Write-Host "Medium Findings: $MediumFindings" -ForegroundColor Yellow
Write-Host "Low Findings: $LowFindings" -ForegroundColor Gray

$overallRisk = if ($CriticalFindings -gt 0) { "CRITICAL" }
               elseif ($HighFindings -gt 3) { "HIGH" }
               elseif ($HighFindings -gt 0) { "MEDIUM" }
               else { "LOW" }

Write-Host "`nOverall Security Rating: $overallRisk" -ForegroundColor $(
    switch ($overallRisk) {
        "CRITICAL" { "Red" }
        "HIGH" { "Red" }
        "MEDIUM" { "Yellow" }
        "LOW" { "Green" }
    }
)

Write-Host "="*60 -ForegroundColor Cyan

# Generate Report
if (-not (Test-Path "logs")) {
    New-Item -Path "logs" -ItemType Directory -Force | Out-Null
}

$reportPath = "logs\penetration-test-$TimeStamp.md"

$currentDate = Get-Date
$report = "# Penetration Test Report`n`n"
$report += "**Date**: $currentDate`n"
$report += "**Tests Performed**: $($PenTestResults.Count)`n"
$report += "**Overall Risk**: $overallRisk`n`n"

$report += "## Executive Summary`n"
$report += "- Critical: $CriticalFindings`n"
$report += "- High: $HighFindings`n"
$report += "- Medium: $MediumFindings`n"
$report += "- Low: $LowFindings`n`n"

$report += "## Test Results`n"

foreach ($result in $PenTestResults) {
    $report += "`n### $($result.TestName) [$($result.Severity)]`n"
    $report += "**Result**: $($result.Result)`n"
    if ($result.Details) {
        $report += "**Details**: $($result.Details)`n"
    }
    if ($result.Recommendation) {
        $report += "**Recommendation**: $($result.Recommendation)`n"
    }
}

$report += "`n## Priority Remediation`n"

$criticalResults = $PenTestResults | Where-Object { $_.Severity -eq "CRITICAL" }
if ($criticalResults.Count -gt 0) {
    $report += "`n### Critical Issues (Immediate Action Required)`n"
    foreach ($result in $criticalResults) {
        $report += "- **$($result.TestName)**: $($result.Recommendation)`n"
    }
}

$highResults = $PenTestResults | Where-Object { $_.Severity -eq "HIGH" }
if ($highResults.Count -gt 0) {
    $report += "`n### High Priority Issues`n"
    foreach ($result in $highResults) {
        $report += "- $($result.TestName): $($result.Recommendation)`n"
    }
}

$report += "`n---`n"
$nextTestDate = Get-Date -Date $currentDate.AddDays(30) -Format "yyyy-MM-dd"
$report += "**Next Test Recommended**: $nextTestDate`n"

$report | Out-File $reportPath -Encoding UTF8

Write-Host "`nPenetration test report saved: $reportPath" -ForegroundColor Green

if ($CriticalFindings -gt 0) { exit 4 }
elseif ($HighFindings -gt 0) { exit 3 }
elseif ($MediumFindings -gt 0) { exit 2 }
else { exit 0 }

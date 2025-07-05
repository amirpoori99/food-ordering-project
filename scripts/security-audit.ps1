# Food Ordering System - Security Audit Script
# Phase 44: Security Hardening

Write-Host "🔒 Food Ordering System - Security Audit" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan

$PROJECT_ROOT = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"

# Security counters
$CriticalCount = 0
$HighCount = 0
$MediumCount = 0
$LowCount = 0
$SecurityIssues = @()

# Function to add security finding
function Add-Finding {
    param($Category, $Title, $Severity, $File, $Recommendation)
    
    $issue = New-Object PSObject -Property @{
        Category = $Category
        Title = $Title
        Severity = $Severity
        File = $File
        Recommendation = $Recommendation
    }
    
    $script:SecurityIssues += $issue
    
    switch ($Severity) {
        "CRITICAL" { $script:CriticalCount++; $color = "Red" }
        "HIGH" { $script:HighCount++; $color = "Red" }
        "MEDIUM" { $script:MediumCount++; $color = "Yellow" }
        "LOW" { $script:LowCount++; $color = "Gray" }
    }
    
    Write-Host "🚨 [$Severity] $Title" -ForegroundColor $color
}

# Test Database Security
Write-Host "`n🗄️ Checking Database Security..." -ForegroundColor Cyan

$dbPath = "$PROJECT_ROOT\backend\food_ordering.db"

if (Test-Path $dbPath) {
    # Check encryption
    try {
        $header = [System.IO.File]::ReadAllBytes($dbPath)[0..15]
        $headerText = [System.Text.Encoding]::ASCII.GetString($header)
        
        if ($headerText.StartsWith("SQLite format")) {
            Add-Finding "Database" "Unencrypted Database" "HIGH" $dbPath "Implement database encryption"
        }
    }
    catch {
        Add-Finding "Database" "Database Check Failed" "LOW" $dbPath "Verify database integrity"
    }
    
    Write-Host "✅ Database file found and checked" -ForegroundColor Green
} else {
    Add-Finding "Database" "Database File Missing" "CRITICAL" $dbPath "Restore database file"
}

# Test Configuration Security
Write-Host "`n⚙️ Checking Configuration Security..." -ForegroundColor Cyan

$configs = @(
    "$PROJECT_ROOT\backend\src\main\resources\hibernate.cfg.xml",
    "$PROJECT_ROOT\backend\src\main\resources\hibernate-production.cfg.xml"
)

foreach ($config in $configs) {
    if (Test-Path $config) {
        $content = Get-Content $config -Raw
        
        if ($content -match 'password.*=.*"(.+)"') {
            Add-Finding "Configuration" "Hardcoded Password" "CRITICAL" $config "Use environment variables"
        }
        
        if ($config -like "*production*" -and $content -match "http://") {
            Add-Finding "Configuration" "HTTP in Production" "HIGH" $config "Use HTTPS only"
        }
    }
}

Write-Host "✅ Configuration files checked" -ForegroundColor Green

# Test Java Code Security
Write-Host "`n☕ Checking Java Code Security..." -ForegroundColor Cyan

$javaFiles = Get-ChildItem "$PROJECT_ROOT\backend\src" -Filter "*.java" -Recurse
$javaIssues = 0

foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        
        if ($line -match "String.*=.*\+.*" -and $line -match "(SELECT|INSERT|UPDATE|DELETE)") {
            Add-Finding "Code" "Potential SQL Injection" "HIGH" $file.FullName "Use PreparedStatement"
            $javaIssues++
        }
        
        if ($line -match 'password.*=.*"(.+)"') {
            Add-Finding "Code" "Hardcoded Password" "CRITICAL" $file.FullName "Use secure configuration"
            $javaIssues++
        }
        
        if ($line -match "System\.out\.print") {
            Add-Finding "Code" "Debug Code" "LOW" $file.FullName "Remove debug statements"
            $javaIssues++
        }
    }
}

Write-Host "✅ $($javaFiles.Count) Java files scanned, $javaIssues issues found" -ForegroundColor Green

# Test PowerShell Scripts
Write-Host "`n💻 Checking PowerShell Scripts..." -ForegroundColor Cyan

$scripts = Get-ChildItem "$PROJECT_ROOT\scripts" -Filter "*.ps1"
$scriptIssues = 0

foreach ($script in $scripts) {
    if ($script.Name -eq "security-audit.ps1") { continue }
    
    $lines = Get-Content $script.FullName
    
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        
        if ($line -match "-ExecutionPolicy\s+Bypass") {
            Add-Finding "Script" "Execution Policy Bypass" "MEDIUM" $script.FullName "Use proper execution policy"
            $scriptIssues++
        }
        
        if ($line -match "Invoke-Expression") {
            Add-Finding "Script" "Command Injection Risk" "HIGH" $script.FullName "Avoid dynamic execution"
            $scriptIssues++
        }
    }
}

Write-Host "✅ $($scripts.Count) PowerShell scripts scanned, $scriptIssues issues found" -ForegroundColor Green

# Show Summary
Write-Host "`n" + "="*50 -ForegroundColor Cyan
Write-Host "🔒 SECURITY AUDIT SUMMARY" -ForegroundColor Cyan
Write-Host "="*50 -ForegroundColor Cyan

Write-Host "Total Issues: $($SecurityIssues.Count)" -ForegroundColor White
Write-Host "Critical: $CriticalCount" -ForegroundColor Red
Write-Host "High: $HighCount" -ForegroundColor Red
Write-Host "Medium: $MediumCount" -ForegroundColor Yellow
Write-Host "Low: $LowCount" -ForegroundColor Gray

if ($CriticalCount -gt 0) {
    Write-Host "`n🚨 CRITICAL ISSUES FOUND!" -ForegroundColor Red
} elseif ($HighCount -gt 0) {
    Write-Host "`n⚠️ High priority issues found" -ForegroundColor Yellow
} else {
    Write-Host "`n✅ No critical issues found" -ForegroundColor Green
}

Write-Host "="*50 -ForegroundColor Cyan

# Generate Report
if (-not (Test-Path "$PROJECT_ROOT\logs")) {
    New-Item -Path "$PROJECT_ROOT\logs" -ItemType Directory -Force | Out-Null
}

$reportFile = "$PROJECT_ROOT\logs\security-audit-$TIMESTAMP.md"

$report = "# Security Audit Report`n"
$report += "**Date**: $(Get-Date)`n"
$report += "**Total Issues**: $($SecurityIssues.Count)`n`n"

$report += "## Summary`n"
$report += "- Critical: $CriticalCount`n"
$report += "- High: $HighCount`n"
$report += "- Medium: $MediumCount`n"
$report += "- Low: $LowCount`n`n"

$report += "## Issues Found`n"

foreach ($issue in $SecurityIssues) {
    $report += "`n### $($issue.Title) [$($issue.Severity)]`n"
    $report += "**Category**: $($issue.Category)`n"
    $report += "**File**: $($issue.File)`n"
    $report += "**Recommendation**: $($issue.Recommendation)`n"
}

$report | Out-File $reportFile -Encoding UTF8
Write-Host "`n📄 Report saved: $reportFile" -ForegroundColor Green

# Set exit code based on findings
if ($CriticalCount -gt 0) { exit 3 }
elseif ($HighCount -gt 0) { exit 2 }
elseif ($MediumCount -gt 0) { exit 1 }
else { exit 0 } 
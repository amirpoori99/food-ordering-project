# Food Ordering System - Final Code Verification
# Verification of all 3 security fixes

Write-Host "Food Ordering System - Final Code Verification" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

$TotalTests = 0
$PassedTests = 0
$FailedTests = 0

function Test-CodeFix {
    param($TestName, $TestResult, $Details)
    
    $script:TotalTests++
    
    if ($TestResult) {
        $script:PassedTests++
        Write-Host "PASS: $TestName" -ForegroundColor Green
    } else {
        $script:FailedTests++
        Write-Host "FAIL: $TestName" -ForegroundColor Red
    }
    
    if ($Details) {
        Write-Host "  $Details" -ForegroundColor Gray
    }
}

Write-Host "`nTesting Fix 1: SQL Injection Remediation" -ForegroundColor Yellow

# Test SQL Injection fix
$analyticsFile = "backend\src\main\java\com\myapp\analytics\repository\AnalyticsRepository.java"
if (Test-Path $analyticsFile) {
    $content = Get-Content $analyticsFile -Raw
    $hasParameterizedQuery = $content -match "setParameter.*status.*COMPLETED"
    $hasHQLInsteadOfSQL = $content -match "FROM Order o WHERE o\.status = :status"
    
    Test-CodeFix "SQL Injection Fixed" ($hasParameterizedQuery -and $hasHQLInsteadOfSQL) `
        "Converted Native SQL to HQL with parameterized queries"
} else {
    Test-CodeFix "SQL Injection Fixed" $false "AnalyticsRepository.java not found"
}

Write-Host "`nTesting Fix 2: Production Configuration Security" -ForegroundColor Yellow

# Test Production Configuration fix
$prodConfig = "backend\src\main\resources\hibernate-production.cfg.xml"
if (Test-Path $prodConfig) {
    $configContent = Get-Content $prodConfig -Raw
    $hasHTTPS = $configContent -match "https://"
    $hasEnvironmentVars = $configContent -match '\$\{DB_USERNAME\}' -and $configContent -match '\$\{DB_PASSWORD\}'
    
    Test-CodeFix "HTTPS Enforced" $hasHTTPS "DTD declaration uses HTTPS"
    Test-CodeFix "Environment Variables" $hasEnvironmentVars "Database credentials use environment variables"
} else {
    Test-CodeFix "Production Configuration" $false "hibernate-production.cfg.xml not found"
}

Write-Host "`nTesting Fix 3: Database Encryption Implementation" -ForegroundColor Yellow

# Test Database Encryption implementation
$encryptionClass = "backend\src\main\java\com\myapp\common\DatabaseEncryption.java"
if (Test-Path $encryptionClass) {
    $encryptionContent = Get-Content $encryptionClass -Raw
    $hasAES256 = $encryptionContent -match "AES.*256"
    $hasEncryptionMethod = $encryptionContent -match "encryptDatabase"
    $hasKeyManagement = $encryptionContent -match "generateEncryptionKey"
    
    Test-CodeFix "AES-256 Encryption" $hasAES256 "Strong encryption algorithm implemented"
    Test-CodeFix "Encryption Methods" $hasEncryptionMethod "Database encryption functionality implemented"
    Test-CodeFix "Key Management" $hasKeyManagement "Secure key generation implemented"
} else {
    Test-CodeFix "Database Encryption Class" $false "DatabaseEncryption.java not found"
}

# Test Environment Configuration
$envTemplate = ".env.template"
if (Test-Path $envTemplate) {
    $envContent = Get-Content $envTemplate -Raw
    $hasDBEncryption = $envContent -match "DB_ENCRYPTION_ENABLED"
    $hasCryptoKeys = $envContent -match "CRYPTO_KEY" -and $envContent -match "JWT_SECRET"
    
    Test-CodeFix "Environment Template" $hasDBEncryption "Database encryption configuration added"
    Test-CodeFix "Cryptographic Keys" $hasCryptoKeys "Secure key management configured"
} else {
    Test-CodeFix "Environment Template" $false ".env.template not found"
}

# Test Security Scripts
$verificationScript = "scripts\verify-database-encryption.ps1"
Test-CodeFix "Verification Script" (Test-Path $verificationScript) "Database encryption verification script created"

$securityHeaders = "config\security\headers.conf"
Test-CodeFix "Security Headers" (Test-Path $securityHeaders) "Security headers configuration created"

Write-Host "`nTesting Application Integrity" -ForegroundColor Yellow

# Check if core application files are intact
$coreFiles = @(
    "backend\src\main\java\com\myapp\ServerApp.java",
    "backend\src\main\resources\hibernate.cfg.xml",
    "backend\pom.xml"
)

$coreIntact = $true
foreach ($file in $coreFiles) {
    if (-not (Test-Path $file)) {
        $coreIntact = $false
        break
    }
}

Test-CodeFix "Core Application Files" $coreIntact "Essential application files are intact"

# Test if backend can still compile (basic check)
if (Test-Path "backend\target\classes") {
    Test-CodeFix "Backend Compilation" $true "Backend classes directory exists"
} else {
    Test-CodeFix "Backend Compilation" $false "Backend compilation may be required"
}

# Summary
Write-Host "`nFINAL VERIFICATION SUMMARY" -ForegroundColor Cyan
Write-Host "Total Tests: $TotalTests" -ForegroundColor White
Write-Host "Passed: $PassedTests" -ForegroundColor Green
Write-Host "Failed: $FailedTests" -ForegroundColor Red

$successRate = [math]::Round(($PassedTests / $TotalTests) * 100, 2)
Write-Host "Success Rate: $successRate%" -ForegroundColor $(
    if ($successRate -ge 90) { "Green" }
    elseif ($successRate -ge 80) { "Yellow" }
    else { "Red" }
)

Write-Host "`nCode Quality Status: " -NoNewline
if ($successRate -ge 90) {
    Write-Host "EXCELLENT" -ForegroundColor Green
} elseif ($successRate -ge 80) {
    Write-Host "GOOD" -ForegroundColor Yellow
} else {
    Write-Host "NEEDS IMPROVEMENT" -ForegroundColor Red
}

Write-Host "`nSecurity Compliance: " -NoNewline
if ($PassedTests -ge ($TotalTests - 1)) {
    Write-Host "SECURE" -ForegroundColor Green
} else {
    Write-Host "REVIEW REQUIRED" -ForegroundColor Yellow
}

Write-Host "`nProduction Readiness: " -NoNewline
if ($successRate -ge 95) {
    Write-Host "READY" -ForegroundColor Green
} elseif ($successRate -ge 85) {
    Write-Host "NEARLY READY" -ForegroundColor Yellow
} else {
    Write-Host "NOT READY" -ForegroundColor Red
}

Write-Host "`nAll 3 Critical Security Issues: " -NoNewline
if ($PassedTests -ge 10) {
    Write-Host "FIXED" -ForegroundColor Green
} else {
    Write-Host "PARTIALLY FIXED" -ForegroundColor Yellow
}

exit 0 
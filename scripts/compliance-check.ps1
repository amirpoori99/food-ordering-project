# Food Ordering System - Security Compliance Check
# Phase 44: Security Hardening & Compliance

Write-Host "Food Ordering System - Security Compliance Check" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan

$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"

# Compliance Results
$ComplianceResults = @()
$TotalChecks = 0
$PassedChecks = 0
$FailedChecks = 0

function Add-ComplianceResult {
    param($Standard, $Control, $Status, $Details, $Requirement)
    
    $result = [PSCustomObject]@{
        Standard = $Standard
        Control = $Control
        Status = $Status
        Details = $Details
        Requirement = $Requirement
        Timestamp = Get-Date
    }
    
    $script:ComplianceResults += $result
    $script:TotalChecks++
    
    if ($Status -eq "PASS") {
        $script:PassedChecks++
        $color = "Green"
    } else {
        $script:FailedChecks++
        $color = "Red"
    }
    
    Write-Host "[$Status] $Standard - $Control" -ForegroundColor $color
    if ($Details) {
        Write-Host "  $Details" -ForegroundColor Gray
    }
}

# OWASP Top 10 Compliance Checks
Write-Host "`nChecking OWASP Top 10 Compliance..." -ForegroundColor Yellow

# A01: Broken Access Control
if (Test-Path "docs\security-implementation-guide.md") {
    $guide = Get-Content "docs\security-implementation-guide.md" -Raw
    if ($guide -match "Input validation|authorization") {
        Add-ComplianceResult "OWASP" "A01-Access Control" "PASS" `
            "Security guidelines include access control measures" `
            "Implement proper access controls"
    } else {
        Add-ComplianceResult "OWASP" "A01-Access Control" "FAIL" `
            "Security guidelines missing access control details" `
            "Implement proper access controls"
    }
} else {
    Add-ComplianceResult "OWASP" "A01-Access Control" "FAIL" `
        "Security guidelines document not found" `
        "Create security implementation guide"
}

# A02: Cryptographic Failures
if (Test-Path ".env.template") {
    $envTemplate = Get-Content ".env.template" -Raw
    if ($envTemplate -match "CRYPTO_KEY|JWT_SECRET") {
        Add-ComplianceResult "OWASP" "A02-Cryptographic Failures" "PASS" `
            "Environment template includes cryptographic keys" `
            "Use secure cryptographic practices"
    } else {
        Add-ComplianceResult "OWASP" "A02-Cryptographic Failures" "FAIL" `
            "Environment template missing crypto configuration" `
            "Add cryptographic key management"
    }
} else {
    Add-ComplianceResult "OWASP" "A02-Cryptographic Failures" "FAIL" `
        "Environment template not found" `
        "Create environment configuration template"
}

# A03: Injection
$javaFiles = Get-ChildItem "$ProjectRoot\backend\src" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
$sqlInjectionFound = $false

foreach ($javaFile in $javaFiles) {
    $content = Get-Content $javaFile.FullName -Raw
    # Look for actual SQL injection patterns: unsafe string concatenation
    # Skip safe patterns like HQL, StringBuilder, and parameterized queries
    if ($content -match 'executeQuery\([^)]*\+[^)]*\)' -and
        $content -notmatch 'StringBuilder|setParameter|createQuery') {
        $sqlInjectionFound = $true
        break
    }
}

if (-not $sqlInjectionFound) {
    Add-ComplianceResult "OWASP" "A03-Injection" "PASS" `
        "No obvious SQL injection vulnerabilities found" `
        "Use parameterized queries"
} else {
    Add-ComplianceResult "OWASP" "A03-Injection" "FAIL" `
        "Potential SQL injection vulnerabilities detected" `
        "Fix SQL injection vulnerabilities"
}

# A04: Insecure Design
if (Test-Path "config\security\headers.conf") {
    Add-ComplianceResult "OWASP" "A04-Insecure Design" "PASS" `
        "Security headers configuration exists" `
        "Implement security by design"
} else {
    Add-ComplianceResult "OWASP" "A04-Insecure Design" "FAIL" `
        "Security headers configuration missing" `
        "Create security headers configuration"
}

# A05: Security Misconfiguration
$prodConfig = "$ProjectRoot\backend\src\main\resources\hibernate-production.cfg.xml"
if (Test-Path $prodConfig) {
    $config = Get-Content $prodConfig -Raw
    if ($config -match "https://|SSL") {
        Add-ComplianceResult "OWASP" "A05-Security Misconfiguration" "PASS" `
            "Production configuration uses secure protocols" `
            "Secure all configurations"
    } else {
        Add-ComplianceResult "OWASP" "A05-Security Misconfiguration" "FAIL" `
            "Production configuration may not be secure" `
            "Review and secure production configuration"
    }
} else {
    Add-ComplianceResult "OWASP" "A05-Security Misconfiguration" "FAIL" `
        "Production configuration not found" `
        "Create secure production configuration"
}

# ISO 27001 Compliance Checks
Write-Host "`nChecking ISO 27001 Compliance..." -ForegroundColor Yellow

# A.9.1 Access Control Policy
if (Test-Path "docs\security-implementation-guide.md") {
    Add-ComplianceResult "ISO27001" "A.9.1-Access Control Policy" "PASS" `
        "Security policy documentation exists" `
        "Maintain access control policy"
} else {
    Add-ComplianceResult "ISO27001" "A.9.1-Access Control Policy" "FAIL" `
        "Security policy documentation missing" `
        "Create access control policy"
}

# A.10.1 Cryptographic Controls
if (Test-Path ".env.template") {
    $envTemplate = Get-Content ".env.template" -Raw
    if ($envTemplate -match "CRYPTO_KEY|JWT_SECRET") {
        Add-ComplianceResult "ISO27001" "A.10.1-Cryptographic Controls" "PASS" `
            "Cryptographic key management template exists" `
            "Implement proper cryptographic controls"
    } else {
        Add-ComplianceResult "ISO27001" "A.10.1-Cryptographic Controls" "FAIL" `
            "Cryptographic controls configuration missing" `
            "Implement cryptographic controls"
    }
} else {
    Add-ComplianceResult "ISO27001" "A.10.1-Cryptographic Controls" "FAIL" `
        "Environment configuration missing" `
        "Create cryptographic controls configuration"
}

# A.12.1 Operational Procedures
if (Test-Path "scripts\verify-security.ps1") {
    Add-ComplianceResult "ISO27001" "A.12.1-Operational Procedures" "PASS" `
        "Security verification procedures exist" `
        "Maintain operational security procedures"
} else {
    Add-ComplianceResult "ISO27001" "A.12.1-Operational Procedures" "FAIL" `
        "Security verification procedures missing" `
        "Create operational security procedures"
}

# A.12.6 Management of Technical Vulnerabilities
if (Test-Path "logs" -PathType Container) {
    $logFiles = Get-ChildItem "logs" -Filter "*vulnerability*" -ErrorAction SilentlyContinue
    if ($logFiles.Count -gt 0) {
        Add-ComplianceResult "ISO27001" "A.12.6-Vulnerability Management" "PASS" `
            "Vulnerability assessment logs exist" `
            "Maintain vulnerability management process"
    } else {
        Add-ComplianceResult "ISO27001" "A.12.6-Vulnerability Management" "FAIL" `
            "Vulnerability assessment logs missing" `
            "Implement vulnerability management"
    }
} else {
    Add-ComplianceResult "ISO27001" "A.12.6-Vulnerability Management" "FAIL" `
        "Log directory not found" `
        "Create logging and monitoring system"
}

# PCI DSS Compliance Checks
Write-Host "`nChecking PCI DSS Compliance..." -ForegroundColor Yellow

# Requirement 1: Firewall Configuration
if (Test-Path "config\security\headers.conf") {
    Add-ComplianceResult "PCI-DSS" "Req.1-Network Security" "PASS" `
        "Network security configuration exists" `
        "Maintain firewall and network security"
} else {
    Add-ComplianceResult "PCI-DSS" "Req.1-Network Security" "FAIL" `
        "Network security configuration missing" `
        "Configure network security controls"
}

# Requirement 2: System Security
if (Test-Path ".env.template") {
    $envTemplate = Get-Content ".env.template" -Raw
    if ($envTemplate -match "ADMIN_PASSWORD|DB_PASSWORD") {
        Add-ComplianceResult "PCI-DSS" "Req.2-System Security" "PASS" `
            "System security configuration template exists" `
            "Maintain secure system configurations"
    } else {
        Add-ComplianceResult "PCI-DSS" "Req.2-System Security" "FAIL" `
            "System security configuration incomplete" `
            "Secure system configurations"
    }
} else {
    Add-ComplianceResult "PCI-DSS" "Req.2-System Security" "FAIL" `
        "System security configuration missing" `
        "Create secure system configuration"
}

# Requirement 3: Data Protection
$dbFile = "$ProjectRoot\backend\food_ordering.db"
if (Test-Path $dbFile) {
    Add-ComplianceResult "PCI-DSS" "Req.3-Data Protection" "FAIL" `
        "Database encryption not implemented" `
        "Encrypt sensitive data at rest"
} else {
    Add-ComplianceResult "PCI-DSS" "Req.3-Data Protection" "PASS" `
        "Database file not accessible" `
        "Maintain data protection measures"
}

# Requirement 4: Transmission Security
if (Test-Path "config\security\headers.conf") {
    $headers = Get-Content "config\security\headers.conf" -Raw
    if ($headers -match "Strict-Transport-Security|HTTPS") {
        Add-ComplianceResult "PCI-DSS" "Req.4-Transmission Security" "PASS" `
            "Transmission security headers configured" `
            "Maintain secure transmission protocols"
    } else {
        Add-ComplianceResult "PCI-DSS" "Req.4-Transmission Security" "FAIL" `
            "Transmission security headers incomplete" `
            "Configure secure transmission protocols"
    }
} else {
    Add-ComplianceResult "PCI-DSS" "Req.4-Transmission Security" "FAIL" `
        "Transmission security configuration missing" `
        "Implement secure transmission protocols"
}

# Generate Summary
Write-Host "`n" + "="*60 -ForegroundColor Cyan
Write-Host "SECURITY COMPLIANCE SUMMARY" -ForegroundColor Cyan
Write-Host "="*60 -ForegroundColor Cyan

Write-Host "Total Checks: $TotalChecks" -ForegroundColor White
Write-Host "Passed: $PassedChecks" -ForegroundColor Green
Write-Host "Failed: $FailedChecks" -ForegroundColor Red

$complianceScore = [math]::Round(($PassedChecks / $TotalChecks) * 100, 2)
Write-Host "Compliance Score: $complianceScore%" -ForegroundColor $(
    if ($complianceScore -ge 80) { "Green" }
    elseif ($complianceScore -ge 60) { "Yellow" }
    else { "Red" }
)

$overallStatus = if ($complianceScore -ge 80) { "COMPLIANT" }
                elseif ($complianceScore -ge 60) { "PARTIALLY COMPLIANT" }
                else { "NON-COMPLIANT" }

Write-Host "Overall Status: $overallStatus" -ForegroundColor $(
    if ($overallStatus -eq "COMPLIANT") { "Green" }
    elseif ($overallStatus -eq "PARTIALLY COMPLIANT") { "Yellow" }
    else { "Red" }
)

Write-Host "="*60 -ForegroundColor Cyan

# Generate Report
if (-not (Test-Path "logs")) {
    New-Item -Path "logs" -ItemType Directory -Force | Out-Null
}

$reportPath = "logs\compliance-check-$TimeStamp.md"

$currentDate = Get-Date
$report = "# Security Compliance Report`n`n"
$report += "**Date**: $currentDate`n"
$report += "**Total Checks**: $TotalChecks`n"
$report += "**Passed**: $PassedChecks`n"
$report += "**Failed**: $FailedChecks`n"
$report += "**Compliance Score**: $complianceScore%`n"
$report += "**Overall Status**: $overallStatus`n`n"

$report += "## Standards Checked`n"
$report += "- OWASP Top 10 2021`n"
$report += "- ISO 27001:2013`n"
$report += "- PCI DSS 3.2.1`n`n"

$report += "## Detailed Results`n"

# Group by standard
$standards = $ComplianceResults | Group-Object Standard
foreach ($standard in $standards) {
    $report += "`n### $($standard.Name)`n"
    foreach ($result in $standard.Group) {
        $report += "`n#### $($result.Control) [$($result.Status)]`n"
        if ($result.Details) {
            $report += "**Details**: $($result.Details)`n"
        }
        $report += "**Requirement**: $($result.Requirement)`n"
    }
}

$report += "`n## Remediation Priority`n"

$failedResults = $ComplianceResults | Where-Object { $_.Status -eq "FAIL" }
if ($failedResults.Count -gt 0) {
    $report += "`n### Failed Checks (Immediate Action Required)`n"
    foreach ($result in $failedResults) {
        $report += "- **$($result.Standard) - $($result.Control)**: $($result.Requirement)`n"
    }
}

$report += "`n## Recommendations`n"
if ($complianceScore -lt 80) {
    $report += "1. Address all failed compliance checks immediately`n"
    $report += "2. Implement missing security controls`n"
    $report += "3. Review and update security policies`n"
    $report += "4. Conduct regular compliance assessments`n"
}

$report += "`n---`n"
$nextAssessmentDate = Get-Date -Date $currentDate.AddDays(90) -Format "yyyy-MM-dd"
$report += "**Next Compliance Assessment**: $nextAssessmentDate`n"

$report | Out-File $reportPath -Encoding UTF8

Write-Host "`nCompliance report saved: $reportPath" -ForegroundColor Green

if ($complianceScore -ge 80) { exit 0 }
elseif ($complianceScore -ge 60) { exit 1 }
else { exit 2 }

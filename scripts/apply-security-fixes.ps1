# Food Ordering System - Apply Security Fixes
# Phase 44: Security Hardening

Write-Host "Food Ordering System - Applying Security Fixes" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$FixesApplied = 0

# Create backup directory
$BackupDir = "backups\security-fixes-$TimeStamp"
if (-not (Test-Path $BackupDir)) {
    New-Item -Path $BackupDir -ItemType Directory -Force | Out-Null
    Write-Host "Backup directory created: $BackupDir" -ForegroundColor Green
}

# 1. Create Environment Template
Write-Host "`nCreating Environment Variables Template..." -ForegroundColor Yellow

$envTemplate = @"
# Environment Variables for Security
# Copy to .env and set secure values

DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_32_chars_minimum
CRYPTO_KEY=your_encryption_key_here
ADMIN_EMAIL=admin@company.com
ADMIN_PASSWORD=secure_admin_password
"@

$envTemplate | Out-File ".env.template" -Encoding UTF8
Write-Host "Created .env.template file" -ForegroundColor Green
$FixesApplied++

# 2. Create Security Headers Config
Write-Host "`nCreating Security Headers Configuration..." -ForegroundColor Yellow

if (-not (Test-Path "config\security")) {
    New-Item -Path "config\security" -ItemType Directory -Force | Out-Null
}

$securityHeaders = @"
# Security Headers - Apply these to your web server

X-XSS-Protection: 1; mode=block
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Content-Security-Policy: default-src 'self'
Strict-Transport-Security: max-age=31536000
Referrer-Policy: strict-origin-when-cross-origin
"@

$securityHeaders | Out-File "config\security\headers.conf" -Encoding UTF8
Write-Host "Created security headers configuration" -ForegroundColor Green
$FixesApplied++

# 3. Create Security Guidelines
Write-Host "`nCreating Security Guidelines..." -ForegroundColor Yellow

$guidelines = @"
# Security Implementation Guide

## Critical Actions Required

### 1. Environment Variables
- Copy .env.template to .env
- Set all environment variables with secure values
- Never commit .env to version control

### 2. Database Security
- Implement database encryption
- Restrict database file permissions
- Use connection pooling

### 3. Code Changes Required
- Replace hardcoded credentials with environment variables
- Convert SQL string concatenation to PreparedStatement
- Upgrade weak cryptographic algorithms (MD5, SHA1, DES)
- Remove hardcoded encryption keys

### 4. Configuration Updates
- Replace HTTP with HTTPS in production
- Apply security headers from config/security/headers.conf
- Disable debug features in production

### 5. Input Validation
- Validate all user inputs
- Use parameterized queries
- Implement proper error handling

## Security Checklist

- [ ] Environment variables configured
- [ ] Database encrypted
- [ ] HTTPS enforced
- [ ] Security headers applied
- [ ] Input validation implemented
- [ ] SQL injection fixed
- [ ] Weak crypto replaced
- [ ] Debug code removed

## Testing
1. Set environment variables
2. Test application functionality
3. Run security scans
4. Verify all fixes work correctly

Next Security Review: $(Get-Date -Date (Get-Date).AddDays(30) -Format 'yyyy-MM-dd')
"@

$guidelines | Out-File "docs\security-implementation-guide.md" -Encoding UTF8
Write-Host "Created security implementation guide" -ForegroundColor Green
$FixesApplied++

# 4. Create Security Verification Script
Write-Host "`nCreating Security Verification Script..." -ForegroundColor Yellow

$verificationScript = @"
# Security Verification Script
Write-Host "Verifying Security Implementation..." -ForegroundColor Cyan

# Check environment variables
if (Test-Path ".env") {
    Write-Host "✓ Environment file exists" -ForegroundColor Green
} else {
    Write-Host "✗ Environment file missing" -ForegroundColor Red
}

# Check security headers
if (Test-Path "config\security\headers.conf") {
    Write-Host "✓ Security headers configured" -ForegroundColor Green
} else {
    Write-Host "✗ Security headers missing" -ForegroundColor Red
}

# Check for hardcoded credentials (basic check)
$javaFiles = Get-ChildItem "backend\src" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
$hardcodedFound = 0

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match 'password\s*=\s*"[^"]+"|secret\s*=\s*"[^"]+"') {
        $hardcodedFound++
    }
}

if ($hardcodedFound -eq 0) {
    Write-Host "✓ No obvious hardcoded credentials found" -ForegroundColor Green
} else {
    Write-Host "✗ Found $hardcodedFound files with potential hardcoded credentials" -ForegroundColor Red
}

Write-Host "`nSecurity verification complete." -ForegroundColor Cyan
"@

$verificationScript | Out-File "scripts\verify-security.ps1" -Encoding UTF8
Write-Host "Created security verification script" -ForegroundColor Green
$FixesApplied++

# Summary
Write-Host "`n" + "="*50 -ForegroundColor Cyan
Write-Host "SECURITY FIXES SUMMARY" -ForegroundColor Cyan
Write-Host "="*50 -ForegroundColor Cyan

Write-Host "Security fixes applied: $FixesApplied" -ForegroundColor Green
Write-Host "Backup location: $BackupDir" -ForegroundColor Cyan

Write-Host "`nFiles Created:" -ForegroundColor White
Write-Host "- .env.template (Environment variables)" -ForegroundColor Gray
Write-Host "- config\security\headers.conf (Security headers)" -ForegroundColor Gray  
Write-Host "- docs\security-implementation-guide.md (Guidelines)" -ForegroundColor Gray
Write-Host "- scripts\verify-security.ps1 (Verification)" -ForegroundColor Gray

Write-Host "`nNext Steps:" -ForegroundColor Yellow
Write-Host "1. Copy .env.template to .env and set values" -ForegroundColor Gray
Write-Host "2. Review security implementation guide" -ForegroundColor Gray
Write-Host "3. Apply code changes manually" -ForegroundColor Gray
Write-Host "4. Run verification script" -ForegroundColor Gray
Write-Host "5. Test application thoroughly" -ForegroundColor Gray

Write-Host "="*50 -ForegroundColor Cyan

# Generate report
$reportPath = "logs\security-fixes-$TimeStamp.md"

$report = "# Security Fixes Applied`n`n"
$report += "**Date**: $(Get-Date)`n"
$report += "**Fixes Applied**: $FixesApplied`n"
$report += "**Backup Location**: $BackupDir`n`n"

$report += "## Files Created`n"
$report += "- .env.template - Environment variables template`n"
$report += "- config\security\headers.conf - Security headers configuration`n"
$report += "- docs\security-implementation-guide.md - Implementation guidelines`n"
$report += "- scripts\verify-security.ps1 - Security verification script`n`n"

$report += "## Manual Actions Required`n"
$report += "1. Set environment variables from template`n"
$report += "2. Replace hardcoded credentials in Java code`n"
$report += "3. Fix SQL injection vulnerabilities`n"
$report += "4. Upgrade weak cryptographic algorithms`n"
$report += "5. Apply security headers to web server`n"
$report += "6. Test all functionality`n`n"

$report += "## Priority Order`n"
$report += "1. **CRITICAL**: Set environment variables`n"
$report += "2. **CRITICAL**: Fix hardcoded credentials`n"
$report += "3. **CRITICAL**: Fix SQL injection`n"
$report += "4. **HIGH**: Upgrade cryptography`n"
$report += "5. **HIGH**: Apply HTTPS configuration`n"
$report += "6. **MEDIUM**: Apply security headers`n"

$report | Out-File $reportPath -Encoding UTF8

Write-Host "`nReport saved: $reportPath" -ForegroundColor Green

exit 0 
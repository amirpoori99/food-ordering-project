# Security Verification Script
Write-Host "Verifying Security Implementation..." -ForegroundColor Cyan

# Check environment variables
if (Test-Path ".env") {
    Write-Host "âœ“ Environment file exists" -ForegroundColor Green
} else {
    Write-Host "âœ— Environment file missing" -ForegroundColor Red
}

# Check security headers
if (Test-Path "config\security\headers.conf") {
    Write-Host "âœ“ Security headers configured" -ForegroundColor Green
} else {
    Write-Host "âœ— Security headers missing" -ForegroundColor Red
}

# Check for hardcoded credentials (basic check)
 = Get-ChildItem "backend\src" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
 = 0

foreach ( in ) {
     = Get-Content .FullName -Raw
    if ( -match 'password\s*=\s*"[^"]+"|secret\s*=\s*"[^"]+"') {
        ++
    }
}

if ( -eq 0) {
    Write-Host "âœ“ No obvious hardcoded credentials found" -ForegroundColor Green
} else {
    Write-Host "âœ— Found  files with potential hardcoded credentials" -ForegroundColor Red
}

Write-Host "
Security verification complete." -ForegroundColor Cyan

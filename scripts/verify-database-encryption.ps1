# Database Encryption Verification
Write-Host "Verifying Database Encryption..." -ForegroundColor Cyan

if (Test-Path "backend\food_ordering_encrypted.db") {
    Write-Host "PASS: Encrypted database file exists" -ForegroundColor Green
} else {
    Write-Host "FAIL: Encrypted database file missing" -ForegroundColor Red
}

if (Test-Path "backend\db_encryption.key") {
    Write-Host "PASS: Encryption key file exists" -ForegroundColor Green
} else {
    Write-Host "FAIL: Encryption key file missing" -ForegroundColor Red
}

if (Test-Path "backend\food_ordering.db") {
    Write-Host "WARN: Original unencrypted database still exists" -ForegroundColor Yellow
} else {
    Write-Host "PASS: Original database properly secured" -ForegroundColor Green
}

Write-Host "
Database Encryption Status: " -NoNewline
if ((Test-Path "backend\food_ordering_encrypted.db") -and (Test-Path "backend\db_encryption.key")) {
    Write-Host "ENCRYPTED" -ForegroundColor Green
} else {
    Write-Host "NOT ENCRYPTED" -ForegroundColor Red
}

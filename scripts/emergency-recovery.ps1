# Food Ordering System - Emergency Recovery Script
# Phase 43: Disaster Recovery System

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("database", "config", "full", "application", "logs")]
    [string]$RecoveryType,
    
    [string]$BackupDate = "latest",
    [string]$BackupPath = "",
    [switch]$Force = $false,
    [switch]$TestMode = $false,
    [switch]$NoVerification = $false
)

# تنظیمات اصلی
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$RECOVERY_LOG = "$PROJECT_ROOT\logs\recovery-$TIMESTAMP.log"

# اطمینان از وجود دایرکتوری لاگ
if (-not (Test-Path "$PROJECT_ROOT\logs")) {
    New-Item -ItemType Directory -Path "$PROJECT_ROOT\logs" -Force | Out-Null
}

# توابع کمکی
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [$Level] $Message"
    Write-Host $logEntry
    Add-Content -Path $RECOVERY_LOG -Value $logEntry
}

function Write-Success { param([string]$Text) Write-Log "✅ $Text" "SUCCESS" }
function Write-Info { param([string]$Text) Write-Log "ℹ️ $Text" "INFO" }
function Write-Warning { param([string]$Text) Write-Log "⚠️ $Text" "WARNING" }
function Write-Error { param([string]$Text) Write-Log "❌ $Text" "ERROR" }

# تابع تأیید از کاربر
function Confirm-Action {
    param([string]$Message)
    
    if ($Force) {
        Write-Info "Force mode enabled - skipping confirmation"
        return $true
    }
    
    Write-Host "$Message (y/N): " -NoNewline -ForegroundColor Yellow
    $response = Read-Host
    return $response -match '^[Yy]$'
}

# تابع توقف سرویس‌ها
function Stop-ApplicationServices {
    Write-Info "Stopping application services..."
    
    $javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
    $javafxProcesses = Get-Process -Name "javafx" -ErrorAction SilentlyContinue
    
    if ($javaProcesses) {
        Write-Info "Stopping Java processes..."
        $javaProcesses | ForEach-Object {
            try {
                Stop-Process -Id $_.Id -Force
                Write-Success "Stopped Java process (PID: $($_.Id))"
            }
            catch {
                Write-Warning "Failed to stop Java process (PID: $($_.Id)): $($_.Exception.Message)"
            }
        }
    }
    
    if ($javafxProcesses) {
        Write-Info "Stopping JavaFX processes..."
        $javafxProcesses | ForEach-Object {
            try {
                Stop-Process -Id $_.Id -Force
                Write-Success "Stopped JavaFX process (PID: $($_.Id))"
            }
            catch {
                Write-Warning "Failed to stop JavaFX process (PID: $($_.Id)): $($_.Exception.Message)"
            }
        }
    }
    
    # انتظار برای اطمینان از توقف کامل
    Start-Sleep -Seconds 3
    Write-Success "Application services stopped"
}

# تابع یافتن آخرین پشتیبان
function Find-LatestBackup {
    param([string]$BackupType = "")
    
    Write-Info "Searching for latest backup..."
    
    if ($BackupPath -and (Test-Path $BackupPath)) {
        Write-Info "Using specified backup path: $BackupPath"
        return $BackupPath
    }
    
    $backupsDir = "$PROJECT_ROOT\backups"
    if (-not (Test-Path $backupsDir)) {
        Write-Error "Backups directory not found: $backupsDir"
        return $null
    }
    
    if ($BackupDate -eq "latest") {
        $latestBackup = Get-ChildItem $backupsDir -Directory | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($latestBackup) {
            Write-Success "Found latest backup: $($latestBackup.Name)"
            return $latestBackup.FullName
        }
    } else {
        $specificBackup = Get-ChildItem $backupsDir -Directory | Where-Object { $_.Name -like "*$BackupDate*" } | Select-Object -First 1
        if ($specificBackup) {
            Write-Success "Found specified backup: $($specificBackup.Name)"
            return $specificBackup.FullName
        }
    }
    
    Write-Error "No suitable backup found"
    return $null
}

# تابع بازیابی پایگاه داده
function Restore-Database {
    param([string]$BackupPath)
    
    Write-Info "Starting database recovery..."
    
    $dbBackupDir = "$BackupPath\database"
    $targetDbPath = "$PROJECT_ROOT\backend\food_ordering.db"
    
    if (-not (Test-Path $dbBackupDir)) {
        Write-Error "Database backup directory not found: $dbBackupDir"
        return $false
    }
    
    # یافتن فایل پشتیبان پایگاه داده
    $dbBackupFile = Get-ChildItem $dbBackupDir -File | Where-Object { $_.Extension -eq ".7z" -or $_.Extension -eq ".db" } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    
    if (-not $dbBackupFile) {
        Write-Error "No database backup file found in: $dbBackupDir"
        return $false
    }
    
    Write-Info "Found database backup: $($dbBackupFile.Name)"
    
    # ایجاد پشتیبان از پایگاه داده فعلی (اگر وجود دارد)
    if (Test-Path $targetDbPath) {
        $currentDbBackup = "$PROJECT_ROOT\backend\food_ordering_before_recovery_$TIMESTAMP.db"
        try {
            Copy-Item $targetDbPath $currentDbBackup -Force
            Write-Info "Current database backed up to: $currentDbBackup"
        }
        catch {
            Write-Warning "Failed to backup current database: $($_.Exception.Message)"
        }
    }
    
    try {
        if ($dbBackupFile.Extension -eq ".7z") {
            # استخراج فایل فشرده
            Write-Info "Extracting compressed database backup..."
            $tempDir = "$PROJECT_ROOT\temp\recovery_$TIMESTAMP"
            New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
            
            & 7z x $dbBackupFile.FullName -o"$tempDir" -y
            if ($LASTEXITCODE -ne 0) {
                Write-Error "Failed to extract database backup"
                return $false
            }
            
            # یافتن فایل استخراج شده
            $extractedDb = Get-ChildItem $tempDir -File -Filter "*.db" | Select-Object -First 1
            if (-not $extractedDb) {
                Write-Error "No database file found after extraction"
                return $false
            }
            
            # کپی فایل بازیابی شده
            Copy-Item $extractedDb.FullName $targetDbPath -Force
            Remove-Item $tempDir -Recurse -Force
        } else {
            # کپی مستقیم فایل
            Copy-Item $dbBackupFile.FullName $targetDbPath -Force
        }
        
        Write-Success "Database restored successfully"
        
        # بررسی یکپارچگی پایگاه داده بازیابی شده
        if (-not $NoVerification) {
            Write-Info "Verifying database integrity..."
            try {
                $integrityCheck = & sqlite3 $targetDbPath "PRAGMA integrity_check;"
                if ($integrityCheck -eq "ok") {
                    Write-Success "Database integrity check passed"
                } else {
                    Write-Error "Database integrity check failed: $integrityCheck"
                    return $false
                }
            }
            catch {
                Write-Error "Failed to verify database integrity: $($_.Exception.Message)"
                return $false
            }
        }
        
        return $true
    }
    catch {
        Write-Error "Failed to restore database: $($_.Exception.Message)"
        return $false
    }
}

# تابع بازیابی تنظیمات
function Restore-Configuration {
    param([string]$BackupPath)
    
    Write-Info "Starting configuration recovery..."
    
    $configBackupDir = "$BackupPath\config"
    
    if (-not (Test-Path $configBackupDir)) {
        Write-Error "Configuration backup directory not found: $configBackupDir"
        return $false
    }
    
    # یافتن فایل پشتیبان تنظیمات
    $configBackupFile = Get-ChildItem $configBackupDir -File | Where-Object { $_.Extension -eq ".7z" } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    
    if (-not $configBackupFile) {
        Write-Error "No configuration backup file found in: $configBackupDir"
        return $false
    }
    
    Write-Info "Found configuration backup: $($configBackupFile.Name)"
    
    try {
        # استخراج فایل تنظیمات
        Write-Info "Extracting configuration backup..."
        $tempDir = "$PROJECT_ROOT\temp\config_recovery_$TIMESTAMP"
        New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
        
        & 7z x $configBackupFile.FullName -o"$tempDir" -y
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to extract configuration backup"
            return $false
        }
        
        # بازگردانی فایل‌های تنظیمات
        $configMappings = @{
            "config_*" = "config"
            "backend_src_main_resources_*" = "backend\src\main\resources"
            "frontend-javafx_src_main_resources_*" = "frontend-javafx\src\main\resources"
        }
        
        foreach ($pattern in $configMappings.Keys) {
            $targetPath = "$PROJECT_ROOT\$($configMappings[$pattern])"
            $sourceDirs = Get-ChildItem $tempDir -Directory | Where-Object { $_.Name -like $pattern }
            
            foreach ($sourceDir in $sourceDirs) {
                Write-Info "Restoring configuration from: $($sourceDir.Name)"
                
                # ایجاد پشتیبان از تنظیمات فعلی
                if (Test-Path $targetPath) {
                    $currentConfigBackup = "$targetPath" + "_before_recovery_$TIMESTAMP"
                    try {
                        Copy-Item $targetPath $currentConfigBackup -Recurse -Force
                        Write-Info "Current configuration backed up"
                    }
                    catch {
                        Write-Warning "Failed to backup current configuration: $($_.Exception.Message)"
                    }
                }
                
                # بازگردانی تنظیمات
                Copy-Item "$($sourceDir.FullName)\*" $targetPath -Recurse -Force
                Write-Success "Configuration restored to: $targetPath"
            }
        }
        
        Remove-Item $tempDir -Recurse -Force
        Write-Success "Configuration recovery completed"
        return $true
    }
    catch {
        Write-Error "Failed to restore configuration: $($_.Exception.Message)"
        return $false
    }
}

# تابع بازیابی کدهای منبع
function Restore-SourceCode {
    param([string]$BackupPath)
    
    Write-Info "Starting source code recovery..."
    
    $sourceBackupDir = "$BackupPath\source"
    
    if (-not (Test-Path $sourceBackupDir)) {
        Write-Error "Source code backup directory not found: $sourceBackupDir"
        return $false
    }
    
    # یافتن فایل پشتیبان کدهای منبع
    $sourceBackupFile = Get-ChildItem $sourceBackupDir -File | Where-Object { $_.Extension -eq ".7z" } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    
    if (-not $sourceBackupFile) {
        Write-Error "No source code backup file found in: $sourceBackupDir"
        return $false
    }
    
    Write-Info "Found source code backup: $($sourceBackupFile.Name)"
    
    try {
        # استخراج کدهای منبع
        Write-Info "Extracting source code backup..."
        $tempDir = "$PROJECT_ROOT\temp\source_recovery_$TIMESTAMP"
        New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
        
        & 7z x $sourceBackupFile.FullName -o"$tempDir" -y
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to extract source code backup"
            return $false
        }
        
        # بازگردانی کدهای منبع
        $sourcePaths = @("backend\src", "frontend-javafx\src", "scripts", "docs")
        
        foreach ($sourcePath in $sourcePaths) {
            $targetPath = "$PROJECT_ROOT\$sourcePath"
            $sourcePath_temp = "$tempDir\$sourcePath"
            
            if (Test-Path $sourcePath_temp) {
                Write-Info "Restoring source code: $sourcePath"
                
                # ایجاد پشتیبان از کدهای فعلی
                if (Test-Path $targetPath) {
                    $currentSourceBackup = "$targetPath" + "_before_recovery_$TIMESTAMP"
                    try {
                        Copy-Item $targetPath $currentSourceBackup -Recurse -Force
                        Write-Info "Current source code backed up"
                    }
                    catch {
                        Write-Warning "Failed to backup current source code: $($_.Exception.Message)"
                    }
                }
                
                # بازگردانی کدهای منبع
                Copy-Item "$sourcePath_temp\*" $targetPath -Recurse -Force
                Write-Success "Source code restored: $sourcePath"
            }
        }
        
        Remove-Item $tempDir -Recurse -Force
        Write-Success "Source code recovery completed"
        return $true
    }
    catch {
        Write-Error "Failed to restore source code: $($_.Exception.Message)"
        return $false
    }
}

# تابع راه‌اندازی مجدد سرویس‌ها
function Start-ApplicationServices {
    Write-Info "Starting application services..."
    
    try {
        # بررسی وجود فایل‌های اجرایی
        $backendPom = "$PROJECT_ROOT\backend\pom.xml"
        $frontendPom = "$PROJECT_ROOT\frontend-javafx\pom.xml"
        
        if (Test-Path $backendPom) {
            Write-Info "Starting backend service..."
            Start-Process -FilePath "powershell.exe" -ArgumentList "-Command", "cd '$PROJECT_ROOT\backend'; mvn spring-boot:run" -WindowStyle Minimized
            Write-Success "Backend service started"
        }
        
        if (Test-Path $frontendPom) {
            Write-Info "Frontend is available for manual start"
            Write-Info "Run: cd frontend-javafx && mvn javafx:run"
        }
        
        # انتظار برای راه‌اندازی
        Write-Info "Waiting for services to start..."
        Start-Sleep -Seconds 10
        
        # تست اتصال به backend
        try {
            $response = Invoke-RestMethod -Uri "http://localhost:8080/api/health" -TimeoutSec 10
            Write-Success "Backend service is responding"
        }
        catch {
            Write-Warning "Backend service health check failed: $($_.Exception.Message)"
        }
        
        return $true
    }
    catch {
        Write-Error "Failed to start application services: $($_.Exception.Message)"
        return $false
    }
}

# تابع تست یکپارچگی سیستم
function Test-SystemIntegrity {
    Write-Info "Testing system integrity..."
    
    $tests = @()
    
    # تست پایگاه داده
    try {
        $dbPath = "$PROJECT_ROOT\backend\food_ordering.db"
        if (Test-Path $dbPath) {
            $tableCount = & sqlite3 $dbPath "SELECT COUNT(*) FROM sqlite_master WHERE type='table';"
            $tests += @{ Name = "Database Tables"; Status = if ($tableCount -gt 0) { "PASS" } else { "FAIL" }; Details = "$tableCount tables found" }
        } else {
            $tests += @{ Name = "Database File"; Status = "FAIL"; Details = "Database file not found" }
        }
    }
    catch {
        $tests += @{ Name = "Database Check"; Status = "FAIL"; Details = $_.Exception.Message }
    }
    
    # تست فایل‌های تنظیمات
    $configFiles = @(
        "config\hibernate.cfg.xml",
        "backend\src\main\resources\application.properties"
    )
    
    foreach ($configFile in $configFiles) {
        $fullPath = "$PROJECT_ROOT\$configFile"
        if (Test-Path $fullPath) {
            $tests += @{ Name = "Config: $configFile"; Status = "PASS"; Details = "File exists" }
        } else {
            $tests += @{ Name = "Config: $configFile"; Status = "FAIL"; Details = "File missing" }
        }
    }
    
    # نمایش نتایج تست
    Write-Info "System Integrity Test Results:"
    Write-Host "================================" -ForegroundColor Cyan
    
    $passCount = 0
    $failCount = 0
    
    foreach ($test in $tests) {
        $color = if ($test.Status -eq "PASS") { "Green"; $passCount++ } else { "Red"; $failCount++ }
        Write-Host "[$($test.Status)] $($test.Name): $($test.Details)" -ForegroundColor $color
    }
    
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "PASSED: $passCount | FAILED: $failCount" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
    
    return $failCount -eq 0
}

# تابع اصلی بازیابی
function Start-Recovery {
    $startTime = Get-Date
    Write-Info "Starting emergency recovery - Type: $RecoveryType"
    
    # یافتن پشتیبان مناسب
    $backupPath = Find-LatestBackup $RecoveryType
    if (-not $backupPath) {
        Write-Error "No suitable backup found for recovery"
        return $false
    }
    
    # تأیید از کاربر
    if (-not (Confirm-Action "This will restore the system from backup. Continue?")) {
        Write-Info "Recovery cancelled by user"
        return $false
    }
    
    # توقف سرویس‌ها
    Stop-ApplicationServices
    
    $success = $true
    
    # انجام بازیابی بر اساس نوع
    switch ($RecoveryType.ToLower()) {
        "database" {
            Write-Info "Performing database recovery..."
            $success = Restore-Database $backupPath
        }
        "config" {
            Write-Info "Performing configuration recovery..."
            $success = Restore-Configuration $backupPath
        }
        "full" {
            Write-Info "Performing full system recovery..."
            $success = $success -and (Restore-Database $backupPath)
            $success = $success -and (Restore-Configuration $backupPath)
            $success = $success -and (Restore-SourceCode $backupPath)
        }
        "application" {
            Write-Info "Performing application recovery..."
            $success = $success -and (Restore-Configuration $backupPath)
            $success = $success -and (Restore-SourceCode $backupPath)
        }
        default {
            Write-Error "Unknown recovery type: $RecoveryType"
            return $false
        }
    }
    
    if ($success) {
        Write-Success "Recovery completed successfully"
        
        # تست یکپارچگی سیستم
        if (-not $NoVerification) {
            $integrityTest = Test-SystemIntegrity
            if (-not $integrityTest) {
                Write-Warning "System integrity test failed"
            }
        }
        
        # راه‌اندازی مجدد سرویس‌ها
        if (Confirm-Action "Start application services now?") {
            Start-ApplicationServices
        }
        
        $endTime = Get-Date
        $duration = $endTime - $startTime
        Write-Success "Emergency recovery completed in $($duration.TotalMinutes) minutes"
        
        return $true
    } else {
        Write-Error "Recovery failed"
        return $false
    }
}

# اجرای اصلی
try {
    Write-Info "Food Ordering System - Emergency Recovery"
    Write-Info "=========================================="
    Write-Info "Recovery Type: $RecoveryType"
    Write-Info "Backup Date: $BackupDate"
    
    if ($TestMode) {
        Write-Info "Running in TEST MODE - No actual recovery will be performed"
        $backupPath = Find-LatestBackup $RecoveryType
        if ($backupPath) {
            Write-Success "Test completed - Backup found: $backupPath"
        } else {
            Write-Error "Test failed - No backup found"
        }
    } else {
        $result = Start-Recovery
        if ($result) {
            exit 0
        } else {
            exit 1
        }
    }
}
catch {
    Write-Error "Unexpected error: $($_.Exception.Message)"
    Write-Error "Stack trace: $($_.ScriptStackTrace)"
    exit 1
}

Write-Info "Emergency recovery script completed at: $(Get-Date)" 
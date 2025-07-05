# Food Ordering System - Automated Backup Script
# Phase 43: Disaster Recovery System

param(
    [string]$BackupType = "full",
    [string]$ConfigFile = "config/disaster-recovery/backup-config.yml",
    [switch]$TestMode = $false,
    [switch]$Force = $false
)

# تنظیمات اصلی
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$BACKUP_LOG = "$PROJECT_ROOT\logs\backup-$TIMESTAMP.log"

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
    Add-Content -Path $BACKUP_LOG -Value $logEntry
}

function Write-Success { param([string]$Text) Write-Log "✅ $Text" "SUCCESS" }
function Write-Info { param([string]$Text) Write-Log "ℹ️ $Text" "INFO" }
function Write-Warning { param([string]$Text) Write-Log "⚠️ $Text" "WARNING" }
function Write-Error { param([string]$Text) Write-Log "❌ $Text" "ERROR" }

# تابع بررسی وجود ابزارهای مورد نیاز
function Test-RequiredTools {
    Write-Info "Checking required tools..."
    
    $tools = @(
        @{ Name = "7z"; Command = "7z.exe"; Required = $true },
        @{ Name = "sqlite3"; Command = "sqlite3.exe"; Required = $true }
    )
    
    $allToolsAvailable = $true
    
    foreach ($tool in $tools) {
        try {
            $null = Get-Command $tool.Command -ErrorAction Stop
            Write-Success "$($tool.Name) is available"
        }
        catch {
            if ($tool.Required) {
                Write-Error "$($tool.Name) is required but not found"
                $allToolsAvailable = $false
            } else {
                Write-Warning "$($tool.Name) is not available (optional)"
            }
        }
    }
    
    return $allToolsAvailable
}

# تابع ایجاد ساختار دایرکتوری پشتیبان
function Initialize-BackupDirectories {
    Write-Info "Initializing backup directories..."
    
    $directories = @(
        "$PROJECT_ROOT\backups\database",
        "$PROJECT_ROOT\backups\config",
        "$PROJECT_ROOT\backups\source",
        "$PROJECT_ROOT\backups\logs",
        "$PROJECT_ROOT\temp\backup",
        "$PROJECT_ROOT\work\backup"
    )
    
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Success "Created directory: $dir"
        }
    }
}

# تابع پشتیبان‌گیری از پایگاه داده
function Backup-Database {
    param([string]$BackupPath)
    
    Write-Info "Starting database backup..."
    
    $dbPath = "$PROJECT_ROOT\backend\food_ordering.db"
    $dbBackupDir = "$BackupPath\database"
    
    if (-not (Test-Path $dbPath)) {
        Write-Warning "Database file not found: $dbPath"
        return $false
    }
    
    # بررسی یکپارچگی پایگاه داده قبل از پشتیبان‌گیری
    try {
        $integrityCheck = & sqlite3 $dbPath "PRAGMA integrity_check;"
        if ($integrityCheck -ne "ok") {
            Write-Error "Database integrity check failed: $integrityCheck"
            return $false
        }
        Write-Success "Database integrity check passed"
    }
    catch {
        Write-Error "Failed to check database integrity: $($_.Exception.Message)"
        return $false
    }
    
    # ایجاد دایرکتوری مقصد
    if (-not (Test-Path $dbBackupDir)) {
        New-Item -ItemType Directory -Path $dbBackupDir -Force | Out-Null
    }
    
    # کپی پایگاه داده
    $dbBackupFile = "$dbBackupDir\food_ordering_$TIMESTAMP.db"
    try {
        Copy-Item $dbPath $dbBackupFile -Force
        Write-Success "Database copied to: $dbBackupFile"
        
        # فشرده‌سازی پایگاه داده
        $compressedFile = "$dbBackupFile.7z"
        & 7z a $compressedFile $dbBackupFile -mx=9
        if ($LASTEXITCODE -eq 0) {
            Remove-Item $dbBackupFile -Force
            Write-Success "Database compressed: $compressedFile"
        }
        
        return $true
    }
    catch {
        Write-Error "Failed to backup database: $($_.Exception.Message)"
        return $false
    }
}

# تابع پشتیبان‌گیری از فایل‌های تنظیمات
function Backup-Configuration {
    param([string]$BackupPath)
    
    Write-Info "Starting configuration backup..."
    
    $configBackupDir = "$BackupPath\config"
    $configPaths = @(
        "config",
        "backend\src\main\resources",
        "frontend-javafx\src\main\resources"
    )
    
    # ایجاد دایرکتوری مقصد
    if (-not (Test-Path $configBackupDir)) {
        New-Item -ItemType Directory -Path $configBackupDir -Force | Out-Null
    }
    
    try {
        foreach ($configPath in $configPaths) {
            $fullPath = "$PROJECT_ROOT\$configPath"
            if (Test-Path $fullPath) {
                $relativePath = $configPath -replace "\\", "_"
                $destPath = "$configBackupDir\$relativePath" + "_$TIMESTAMP"
                Copy-Item $fullPath $destPath -Recurse -Force
                Write-Success "Configuration backed up: $configPath"
            }
        }
        
        # فشرده‌سازی تنظیمات
        $compressedFile = "$configBackupDir\config_$TIMESTAMP.7z"
        & 7z a $compressedFile "$configBackupDir\*_$TIMESTAMP" -mx=7
        if ($LASTEXITCODE -eq 0) {
            Get-ChildItem "$configBackupDir\*_$TIMESTAMP" | Remove-Item -Recurse -Force
            Write-Success "Configuration compressed: $compressedFile"
        }
        
        return $true
    }
    catch {
        Write-Error "Failed to backup configuration: $($_.Exception.Message)"
        return $false
    }
}

# تابع پشتیبان‌گیری از کدهای منبع
function Backup-SourceCode {
    param([string]$BackupPath)
    
    Write-Info "Starting source code backup..."
    
    $sourceBackupDir = "$BackupPath\source"
    $sourcePaths = @(
        "backend\src",
        "frontend-javafx\src",
        "scripts",
        "docs"
    )
    
    # ایجاد دایرکتوری مقصد
    if (-not (Test-Path $sourceBackupDir)) {
        New-Item -ItemType Directory -Path $sourceBackupDir -Force | Out-Null
    }
    
    try {
        $tempSourceDir = "$PROJECT_ROOT\temp\backup\source_$TIMESTAMP"
        New-Item -ItemType Directory -Path $tempSourceDir -Force | Out-Null
        
        foreach ($sourcePath in $sourcePaths) {
            $fullPath = "$PROJECT_ROOT\$sourcePath"
            if (Test-Path $fullPath) {
                $destPath = "$tempSourceDir\$sourcePath"
                $destParent = Split-Path $destPath -Parent
                if (-not (Test-Path $destParent)) {
                    New-Item -ItemType Directory -Path $destParent -Force | Out-Null
                }
                Copy-Item $fullPath $destPath -Recurse -Force
                Write-Success "Source backed up: $sourcePath"
            }
        }
        
        # فشرده‌سازی کدهای منبع
        $compressedFile = "$sourceBackupDir\source_$TIMESTAMP.7z"
        & 7z a $compressedFile "$tempSourceDir\*" -mx=5
        if ($LASTEXITCODE -eq 0) {
            Remove-Item $tempSourceDir -Recurse -Force
            Write-Success "Source code compressed: $compressedFile"
        }
        
        return $true
    }
    catch {
        Write-Error "Failed to backup source code: $($_.Exception.Message)"
        return $false
    }
}

# تابع پشتیبان‌گیری از لاگ‌ها
function Backup-Logs {
    param([string]$BackupPath)
    
    Write-Info "Starting logs backup..."
    
    $logsBackupDir = "$BackupPath\logs"
    $logPaths = @(
        "backend\logs",
        "logs",
        "frontend-javafx\logs"
    )
    
    # ایجاد دایرکتوری مقصد
    if (-not (Test-Path $logsBackupDir)) {
        New-Item -ItemType Directory -Path $logsBackupDir -Force | Out-Null
    }
    
    try {
        $tempLogsDir = "$PROJECT_ROOT\temp\backup\logs_$TIMESTAMP"
        New-Item -ItemType Directory -Path $tempLogsDir -Force | Out-Null
        
        foreach ($logPath in $logPaths) {
            $fullPath = "$PROJECT_ROOT\$logPath"
            if (Test-Path $fullPath) {
                $destPath = "$tempLogsDir\$($logPath -replace '\\', '_')"
                Copy-Item $fullPath $destPath -Recurse -Force
                Write-Success "Logs backed up: $logPath"
            }
        }
        
        # فشرده‌سازی لاگ‌ها
        $compressedFile = "$logsBackupDir\logs_$TIMESTAMP.7z"
        & 7z a $compressedFile "$tempLogsDir\*" -mx=9
        if ($LASTEXITCODE -eq 0) {
            Remove-Item $tempLogsDir -Recurse -Force
            Write-Success "Logs compressed: $compressedFile"
        }
        
        return $true
    }
    catch {
        Write-Error "Failed to backup logs: $($_.Exception.Message)"
        return $false
    }
}

# تابع ایجاد فایل مانیفست
function Create-BackupManifest {
    param([string]$BackupPath)
    
    Write-Info "Creating backup manifest..."
    
    $manifest = @{
        backup_id = [System.Guid]::NewGuid().ToString()
        timestamp = $TIMESTAMP
        backup_type = $BackupType
        system_info = @{
            hostname = $env:COMPUTERNAME
            username = $env:USERNAME
            os_version = (Get-WmiObject -Class Win32_OperatingSystem).Caption
            powershell_version = $PSVersionTable.PSVersion.ToString()
        }
        backup_size = 0
        files_count = 0
        duration_seconds = 0
        checksum = ""
        status = "completed"
    }
    
    # محاسبه حجم کل پشتیبان
    $backupFiles = Get-ChildItem $BackupPath -Recurse -File
    $manifest.backup_size = ($backupFiles | Measure-Object -Property Length -Sum).Sum
    $manifest.files_count = $backupFiles.Count
    
    # ایجاد فایل مانیفست
    $manifestFile = "$BackupPath\manifest.json"
    $manifest | ConvertTo-Json -Depth 3 | Out-File $manifestFile -Encoding UTF8
    
    Write-Success "Backup manifest created: $manifestFile"
    Write-Info "Backup ID: $($manifest.backup_id)"
    Write-Info "Files count: $($manifest.files_count)"
    Write-Info "Total size: $([math]::Round($manifest.backup_size / 1MB, 2)) MB"
}

# تابع پاک‌کردن پشتیبان‌های قدیمی
function Remove-OldBackups {
    param([int]$RetentionDays = 30)
    
    Write-Info "Removing old backups (older than $RetentionDays days)..."
    
    $cutoffDate = (Get-Date).AddDays(-$RetentionDays)
    $backupDirs = Get-ChildItem "$PROJECT_ROOT\backups" -Directory | Where-Object { $_.CreationTime -lt $cutoffDate }
    
    foreach ($dir in $backupDirs) {
        try {
            Remove-Item $dir.FullName -Recurse -Force
            Write-Success "Removed old backup: $($dir.Name)"
        }
        catch {
            Write-Warning "Failed to remove old backup: $($dir.Name) - $($_.Exception.Message)"
        }
    }
}

# تابع اصلی پشتیبان‌گیری
function Start-Backup {
    $startTime = Get-Date
    Write-Info "Starting backup process - Type: $BackupType"
    
    # بررسی ابزارهای مورد نیاز
    if (-not (Test-RequiredTools)) {
        Write-Error "Required tools are missing. Please install 7-Zip and SQLite3."
        return $false
    }
    
    # ایجاد ساختار دایرکتوری
    Initialize-BackupDirectories
    
    # ایجاد دایرکتوری پشتیبان برای این جلسه
    $sessionBackupPath = "$PROJECT_ROOT\backups\$TIMESTAMP"
    New-Item -ItemType Directory -Path $sessionBackupPath -Force | Out-Null
    
    $success = $true
    
    # انجام پشتیبان‌گیری بر اساس نوع
    switch ($BackupType.ToLower()) {
        "full" {
            Write-Info "Performing full backup..."
            $success = $success -and (Backup-Database $sessionBackupPath)
            $success = $success -and (Backup-Configuration $sessionBackupPath)
            $success = $success -and (Backup-SourceCode $sessionBackupPath)
            $success = $success -and (Backup-Logs $sessionBackupPath)
        }
        "database" {
            Write-Info "Performing database backup..."
            $success = (Backup-Database $sessionBackupPath)
        }
        "config" {
            Write-Info "Performing configuration backup..."
            $success = (Backup-Configuration $sessionBackupPath)
        }
        "source" {
            Write-Info "Performing source code backup..."
            $success = (Backup-SourceCode $sessionBackupPath)
        }
        "logs" {
            Write-Info "Performing logs backup..."
            $success = (Backup-Logs $sessionBackupPath)
        }
        default {
            Write-Error "Unknown backup type: $BackupType"
            return $false
        }
    }
    
    if ($success) {
        # ایجاد فایل مانیفست
        Create-BackupManifest $sessionBackupPath
        
        # پاک‌کردن پشتیبان‌های قدیمی
        Remove-OldBackups
        
        $endTime = Get-Date
        $duration = $endTime - $startTime
        Write-Success "Backup completed successfully in $($duration.TotalSeconds) seconds"
        Write-Info "Backup location: $sessionBackupPath"
        
        return $true
    } else {
        Write-Error "Backup failed"
        return $false
    }
}

# اجرای اصلی
try {
    Write-Info "Food Ordering System - Automated Backup"
    Write-Info "========================================="
    
    if ($TestMode) {
        Write-Info "Running in TEST MODE - No actual backup will be performed"
        # تست ابزارها و دایرکتوری‌ها
        Test-RequiredTools
        Initialize-BackupDirectories
        Write-Success "Test completed successfully"
    } else {
        $result = Start-Backup
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

Write-Info "Backup script completed at: $(Get-Date)" 
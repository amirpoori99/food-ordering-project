# Food Ordering System - Rollback System
# Phase 45: Rollback Strategy & Emergency Recovery

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("deployment", "database", "config", "full")]
    [string]$RollbackType = "deployment",
    
    [Parameter(Mandatory=$false)]
    [string]$TargetVersion,
    
    [Parameter(Mandatory=$false)]
    [switch]$ListVersions,
    
    [Parameter(Mandatory=$false)]
    [switch]$Emergency,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile = "logs\rollback-$Environment-$TimeStamp.log"

# Rollback Configuration
$RollbackConfig = @{
    deploymentsPath = "deployments\$Environment"
    backupsPath = "deployments\$Environment\.backups"
    databaseBackupsPath = "database\backups"
    configBackupsPath = "config\backups"
    maxBackups = 10
}

# Create logs directory
if (-not (Test-Path "logs")) {
    New-Item -Path "logs" -ItemType Directory -Force | Out-Null
}

# Logging Function
function Write-Log {
    param($Message, $Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [$Level] $Message"
    
    Write-Host $logEntry -ForegroundColor $(
        switch ($Level) {
            "ERROR" { "Red" }
            "WARNING" { "Yellow" }
            "SUCCESS" { "Green" }
            "INFO" { "Cyan" }
            "CRITICAL" { "Magenta" }
            default { "White" }
        }
    )
    
    Add-Content -Path $LogFile -Value $logEntry
}

# Get Available Versions
function Get-AvailableVersions {
    param($BackupPath)
    
    if (-not (Test-Path $BackupPath)) {
        Write-Log "Backup directory not found: $BackupPath" "WARNING"
        return @()
    }
    
    $versions = @()
    $backupDirs = Get-ChildItem -Path $BackupPath -Directory | Sort-Object CreationTime -Descending
    
    foreach ($dir in $backupDirs) {
        $deploymentInfo = "$($dir.FullName)\deployment-info.json"
        
        if (Test-Path $deploymentInfo) {
            try {
                $info = Get-Content $deploymentInfo | ConvertFrom-Json
                $versions += @{
                    Name = $dir.Name
                    Path = $dir.FullName
                    DeploymentId = $info.deploymentId
                    Version = $info.version
                    Timestamp = $info.timestamp
                    Environment = $info.environment
                    DeployedBy = $info.deployedBy
                    CreationTime = $dir.CreationTime
                }
            } catch {
                Write-Log "Invalid deployment info in $deploymentInfo" "WARNING"
            }
        } else {
            # Create basic version info if deployment-info.json is missing
            $versions += @{
                Name = $dir.Name
                Path = $dir.FullName
                DeploymentId = $dir.Name
                Version = "Unknown"
                Timestamp = $dir.CreationTime.ToString("yyyy-MM-ddTHH:mm:ssZ")
                Environment = $Environment
                DeployedBy = "Unknown"
                CreationTime = $dir.CreationTime
            }
        }
    }
    
    return $versions
}

# List Available Versions
function Show-AvailableVersions {
    Write-Log "Available versions for rollback in $Environment environment:" "INFO"
    Write-Log "" "INFO"
    
    $versions = Get-AvailableVersions -BackupPath $RollbackConfig.backupsPath
    
    if ($versions.Count -eq 0) {
        Write-Log "No backup versions found" "WARNING"
        return
    }
    
    Write-Log "Deployment Backups:" "INFO"
    Write-Log "==================" "INFO"
    
    for ($i = 0; $i -lt $versions.Count; $i++) {
        $version = $versions[$i]
        $status = if ($i -eq 0) { " (CURRENT)" } else { "" }
        
        Write-Log "[$($i + 1)] $($version.Name)$status" "SUCCESS"
        Write-Log "    Deployment ID: $($version.DeploymentId)" "INFO"
        Write-Log "    Version: $($version.Version)" "INFO"
        Write-Log "    Created: $($version.Timestamp)" "INFO"
        Write-Log "    Deployed By: $($version.DeployedBy)" "INFO"
        Write-Log "" "INFO"
    }
    
    # Database backups
    $dbBackups = Get-ChildItem -Path $RollbackConfig.databaseBackupsPath -Filter "backup_${Environment}_*.db" -ErrorAction SilentlyContinue | Sort-Object CreationTime -Descending
    
    if ($dbBackups.Count -gt 0) {
        Write-Log "Database Backups:" "INFO"
        Write-Log "=================" "INFO"
        
        foreach ($backup in $dbBackups) {
            Write-Log "  $($backup.Name) - $(($backup.CreationTime).ToString('yyyy-MM-dd HH:mm:ss'))" "INFO"
        }
        Write-Log "" "INFO"
    }
}

# Create Emergency Backup
function New-EmergencyBackup {
    Write-Log "Creating emergency backup before rollback..." "CRITICAL"
    
    try {
        $emergencyBackupName = "emergency-backup-$TimeStamp"
        $emergencyBackupPath = "$($RollbackConfig.backupsPath)\$emergencyBackupName"
        
        if (-not (Test-Path $RollbackConfig.backupsPath)) {
            New-Item -Path $RollbackConfig.backupsPath -ItemType Directory -Force | Out-Null
        }
        
        # Backup current deployment
        if (Test-Path $RollbackConfig.deploymentsPath) {
            Copy-Item -Path $RollbackConfig.deploymentsPath -Destination $emergencyBackupPath -Recurse -Force
            Write-Log "Emergency deployment backup created: $emergencyBackupPath" "SUCCESS"
        }
        
        # Backup current database
        $currentDbPath = "database\$Environment\food_ordering_$Environment.db"
        if (Test-Path $currentDbPath) {
            $dbBackupPath = "$($RollbackConfig.databaseBackupsPath)\emergency_backup_${Environment}_${TimeStamp}.db"
            
            if (-not (Test-Path $RollbackConfig.databaseBackupsPath)) {
                New-Item -Path $RollbackConfig.databaseBackupsPath -ItemType Directory -Force | Out-Null
            }
            
            Copy-Item -Path $currentDbPath -Destination $dbBackupPath -Force
            Write-Log "Emergency database backup created: $dbBackupPath" "SUCCESS"
        }
        
        return $emergencyBackupPath
        
    } catch {
        Write-Log "Failed to create emergency backup: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Rollback Deployment
function Invoke-DeploymentRollback {
    param($TargetVersion)
    
    Write-Log "Starting deployment rollback to version: $TargetVersion" "CRITICAL"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would rollback deployment to $TargetVersion" "WARNING"
        return $true
    }
    
    try {
        $versions = Get-AvailableVersions -BackupPath $RollbackConfig.backupsPath
        $targetBackup = $versions | Where-Object { $_.Name -eq $TargetVersion -or $_.DeploymentId -eq $TargetVersion }
        
        if (-not $targetBackup) {
            throw "Target version not found: $TargetVersion"
        }
        
        Write-Log "Found target backup: $($targetBackup.Path)" "INFO"
        
        # Create emergency backup
        if (-not $Emergency) {
            New-EmergencyBackup
        }
        
        # Stop services
        Write-Log "Stopping services..." "INFO"
        $stopScript = "$($RollbackConfig.deploymentsPath)\scripts\stop-$Environment.bat"
        if (Test-Path $stopScript) {
            & $stopScript
            Start-Sleep -Seconds 5
        }
        
        # Remove current deployment
        Write-Log "Removing current deployment..." "INFO"
        if (Test-Path $RollbackConfig.deploymentsPath) {
            Remove-Item -Path "$($RollbackConfig.deploymentsPath)\*" -Recurse -Force
        }
        
        # Restore target version
        Write-Log "Restoring target version..." "INFO"
        Copy-Item -Path "$($targetBackup.Path)\*" -Destination $RollbackConfig.deploymentsPath -Recurse -Force
        
        # Start services
        Write-Log "Starting services..." "INFO"
        $startScript = "$($RollbackConfig.deploymentsPath)\scripts\start-$Environment.bat"
        if (Test-Path $startScript) {
            Start-Process -FilePath $startScript -Wait -WindowStyle Hidden
        }
        
        Write-Log "Deployment rollback completed successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Deployment rollback failed: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Rollback Database
function Invoke-DatabaseRollback {
    param($TargetVersion)
    
    Write-Log "Starting database rollback to version: $TargetVersion" "CRITICAL"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would rollback database to $TargetVersion" "WARNING"
        return $true
    }
    
    try {
        $currentDbPath = "database\$Environment\food_ordering_$Environment.db"
        $targetBackupPath = "$($RollbackConfig.databaseBackupsPath)\$TargetVersion"
        
        if (-not (Test-Path $targetBackupPath)) {
            throw "Database backup not found: $targetBackupPath"
        }
        
        # Create emergency backup
        if (Test-Path $currentDbPath) {
            $emergencyDbBackup = "$($RollbackConfig.databaseBackupsPath)\emergency_before_rollback_${Environment}_${TimeStamp}.db"
            Copy-Item -Path $currentDbPath -Destination $emergencyDbBackup -Force
            Write-Log "Emergency database backup created: $emergencyDbBackup" "SUCCESS"
        }
        
        # Stop services that might be using the database
        Write-Log "Stopping database connections..." "INFO"
        
        # Restore database backup
        Write-Log "Restoring database backup..." "INFO"
        Copy-Item -Path $targetBackupPath -Destination $currentDbPath -Force
        
        Write-Log "Database rollback completed successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Database rollback failed: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Rollback Configuration
function Invoke-ConfigRollback {
    param($TargetVersion)
    
    Write-Log "Starting configuration rollback to version: $TargetVersion" "CRITICAL"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would rollback configuration to $TargetVersion" "WARNING"
        return $true
    }
    
    try {
        $configBackupPath = "$($RollbackConfig.configBackupsPath)\config_backup_${Environment}_${TargetVersion}"
        
        if (-not (Test-Path $configBackupPath)) {
            throw "Configuration backup not found: $configBackupPath"
        }
        
        # Backup current configuration
        $emergencyConfigBackup = "$($RollbackConfig.configBackupsPath)\emergency_config_${Environment}_${TimeStamp}"
        if (Test-Path "config") {
            Copy-Item -Path "config" -Destination $emergencyConfigBackup -Recurse -Force
            Write-Log "Emergency configuration backup created: $emergencyConfigBackup" "SUCCESS"
        }
        
        # Restore configuration backup
        Write-Log "Restoring configuration backup..." "INFO"
        if (Test-Path "config") {
            Remove-Item -Path "config\*" -Recurse -Force
        }
        Copy-Item -Path "$configBackupPath\*" -Destination "config" -Recurse -Force
        
        Write-Log "Configuration rollback completed successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Configuration rollback failed: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Full System Rollback
function Invoke-FullRollback {
    param($TargetVersion)
    
    Write-Log "Starting full system rollback to version: $TargetVersion" "CRITICAL"
    
    try {
        # Create comprehensive emergency backup
        if (-not $Emergency) {
            New-EmergencyBackup
        }
        
        # Rollback deployment
        Write-Log "Rolling back deployment..." "INFO"
        Invoke-DeploymentRollback -TargetVersion $TargetVersion
        
        # Rollback database
        Write-Log "Rolling back database..." "INFO"
        $dbBackupName = "backup_${Environment}_${TargetVersion}.db"
        if (Test-Path "$($RollbackConfig.databaseBackupsPath)\$dbBackupName") {
            Invoke-DatabaseRollback -TargetVersion $dbBackupName
        } else {
            Write-Log "Database backup not found for version $TargetVersion, skipping..." "WARNING"
        }
        
        # Rollback configuration
        Write-Log "Rolling back configuration..." "INFO"
        if (Test-Path "$($RollbackConfig.configBackupsPath)\config_backup_${Environment}_${TargetVersion}") {
            Invoke-ConfigRollback -TargetVersion $TargetVersion
        } else {
            Write-Log "Configuration backup not found for version $TargetVersion, skipping..." "WARNING"
        }
        
        Write-Log "Full system rollback completed successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Full system rollback failed: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Health Check After Rollback
function Test-RollbackHealth {
    Write-Log "Performing health check after rollback..." "INFO"
    
    try {
        # Check if deployment directory exists
        if (-not (Test-Path $RollbackConfig.deploymentsPath)) {
            Write-Log "Deployment directory missing" "ERROR"
            return $false
        }
        
        # Check if database exists
        $dbPath = "database\$Environment\food_ordering_$Environment.db"
        if (-not (Test-Path $dbPath)) {
            Write-Log "Database file missing" "ERROR"
            return $false
        }
        
        # Check if essential scripts exist
        $startScript = "$($RollbackConfig.deploymentsPath)\scripts\start-$Environment.bat"
        if (-not (Test-Path $startScript)) {
            Write-Log "Start script missing" "WARNING"
        }
        
        Write-Log "Rollback health check completed" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Health check failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Main Rollback Process
function Start-RollbackProcess {
    Write-Log "Starting rollback process..." "CRITICAL"
    Write-Log "Environment: $Environment" "INFO"
    Write-Log "Rollback Type: $RollbackType" "INFO"
    
    if ($Emergency) {
        Write-Log "EMERGENCY ROLLBACK MODE ACTIVATED" "CRITICAL"
    }
    
    try {
        # List versions if requested
        if ($ListVersions) {
            Show-AvailableVersions
            return 0
        }
        
        # Validate target version
        if (-not $TargetVersion) {
            Write-Log "Target version is required for rollback" "ERROR"
            Write-Log "Use -ListVersions to see available versions" "INFO"
            throw "Target version required"
        }
        
        # Confirm rollback in production
        if ($Environment -eq "production" -and -not $Force -and -not $Emergency) {
            Write-Log "Production rollback requires -Force parameter" "ERROR"
            throw "Production rollback requires confirmation"
        }
        
        # Execute rollback based on type
        switch ($RollbackType) {
            "deployment" {
                Invoke-DeploymentRollback -TargetVersion $TargetVersion
            }
            "database" {
                Invoke-DatabaseRollback -TargetVersion $TargetVersion
            }
            "config" {
                Invoke-ConfigRollback -TargetVersion $TargetVersion
            }
            "full" {
                Invoke-FullRollback -TargetVersion $TargetVersion
            }
        }
        
        # Health check
        if (-not $DryRun) {
            Start-Sleep -Seconds 10
            if (-not (Test-RollbackHealth)) {
                Write-Log "Rollback health check failed" "ERROR"
                return 1
            }
        }
        
        Write-Log "Rollback process completed successfully!" "SUCCESS"
        return 0
        
    } catch {
        Write-Log "Rollback process failed: $($_.Exception.Message)" "ERROR"
        return 1
    }
}

# Main execution
Write-Host "Food Ordering System - Rollback System" -ForegroundColor Magenta
Write-Host "=======================================" -ForegroundColor Magenta

if ($Emergency) {
    Write-Host "!!! EMERGENCY ROLLBACK MODE !!!" -ForegroundColor Red -BackgroundColor Yellow
}

$exitCode = Start-RollbackProcess
Write-Log "Rollback process completed with exit code: $exitCode" "INFO"
Write-Log "Log file: $LogFile" "INFO"

exit $exitCode 
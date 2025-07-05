# Simple Rollback Script for Food Ordering System

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [switch]$ListVersions,
    
    [Parameter(Mandatory=$false)]
    [string]$TargetVersion
)

Write-Host "Food Ordering System - Rollback" -ForegroundColor Magenta
Write-Host "Environment: $Environment" -ForegroundColor Yellow

$deploymentsPath = "deployments\$Environment"
$backupsPath = "deployments\$Environment\.backups"

# List available versions
if ($ListVersions) {
    Write-Host "Available backup versions:" -ForegroundColor Green
    
    if (Test-Path $backupsPath) {
        $backups = Get-ChildItem -Path $backupsPath -Directory | Sort-Object CreationTime -Descending
        
        if ($backups.Count -eq 0) {
            Write-Host "No backup versions found" -ForegroundColor Yellow
        } else {
            foreach ($backup in $backups) {
                $deploymentInfo = "$($backup.FullName)\deployment-info.json"
                
                if (Test-Path $deploymentInfo) {
                    $info = Get-Content $deploymentInfo | ConvertFrom-Json
                    Write-Host "  $($backup.Name)" -ForegroundColor Green
                    Write-Host "    Created: $($backup.CreationTime)" -ForegroundColor White
                    Write-Host "    Deployment ID: $($info.deploymentId)" -ForegroundColor White
                } else {
                    Write-Host "  $($backup.Name)" -ForegroundColor Green
                    Write-Host "    Created: $($backup.CreationTime)" -ForegroundColor White
                }
                Write-Host ""
            }
        }
    } else {
        Write-Host "No backup directory found" -ForegroundColor Yellow
    }
    
    return
}

# Perform rollback
if ($TargetVersion) {
    Write-Host "Rolling back to version: $TargetVersion" -ForegroundColor Yellow
    
    $targetPath = "$backupsPath\$TargetVersion"
    
    if (-not (Test-Path $targetPath)) {
        Write-Host "Target version not found: $TargetVersion" -ForegroundColor Red
        Write-Host "Use -ListVersions to see available versions" -ForegroundColor Yellow
        return
    }
    
    try {
        # Create emergency backup of current state
        $emergencyBackup = "$backupsPath\emergency-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
        if (Test-Path $deploymentsPath) {
            Copy-Item -Path $deploymentsPath -Destination $emergencyBackup -Recurse -Force
            Write-Host "Emergency backup created: $emergencyBackup" -ForegroundColor Green
        }
        
        # Remove current deployment
        if (Test-Path $deploymentsPath) {
            Remove-Item -Path "$deploymentsPath\*" -Recurse -Force
            Write-Host "Current deployment removed" -ForegroundColor Yellow
        }
        
        # Restore target version
        Copy-Item -Path "$targetPath\*" -Destination $deploymentsPath -Recurse -Force
        Write-Host "Rollback completed successfully!" -ForegroundColor Green
        
        # Show rollback info
        $deploymentInfo = "$deploymentsPath\deployment-info.json"
        if (Test-Path $deploymentInfo) {
            $info = Get-Content $deploymentInfo | ConvertFrom-Json
            Write-Host "Rolled back to:" -ForegroundColor Green
            Write-Host "  Deployment ID: $($info.deploymentId)" -ForegroundColor White
            Write-Host "  Environment: $($info.environment)" -ForegroundColor White
            Write-Host "  Timestamp: $($info.timestamp)" -ForegroundColor White
        }
        
    } catch {
        Write-Host "Rollback failed: $($_.Exception.Message)" -ForegroundColor Red
    }
    
} else {
    Write-Host "Please specify -TargetVersion or use -ListVersions" -ForegroundColor Yellow
    Write-Host "Example: .\rollback.ps1 -Environment development -TargetVersion backup-20250705-123456" -ForegroundColor Gray
} 
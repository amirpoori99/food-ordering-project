# Advanced Deployment Script
# Phase 45: Deployment Automation

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [string]$Version = "latest",
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$DeploymentId = "deploy-$Environment-$TimeStamp"
$LogFile = "logs\deployment-$DeploymentId.log"

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
            default { "White" }
        }
    )
    
    Add-Content -Path $LogFile -Value $logEntry
}

# Pre-deployment Checks
function Test-PreDeploymentChecks {
    Write-Log "Running pre-deployment checks..." "INFO"
    
    $requiredDirs = @("backend", "frontend-javafx", "config", "scripts")
    foreach ($dir in $requiredDirs) {
        if (-not (Test-Path $dir)) {
            Write-Log "Required directory missing: $dir" "ERROR"
            return $false
        }
    }
    
    Write-Log "Pre-deployment checks passed" "SUCCESS"
    return $true
}

# Build Application
function Build-Application {
    Write-Log "Building application..." "INFO"
    
    try {
        Set-Location "$ProjectRoot\backend"
        
        if ($SkipTests) {
            mvn clean compile -DskipTests
        } else {
            mvn clean compile test
        }
        
        if ($LASTEXITCODE -ne 0) {
            throw "Backend build failed"
        }
        
        Set-Location $ProjectRoot
        Write-Log "Application build completed successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Build failed: $($_.Exception.Message)" "ERROR"
        Set-Location $ProjectRoot
        return $false
    }
}

# Deploy Application
function Deploy-Application {
    Write-Log "Deploying application to $Environment environment..." "INFO"
    
    try {
        # Create deployment directory
        $deploymentDir = "deployments\$Environment"
        if (-not (Test-Path $deploymentDir)) {
            New-Item -Path $deploymentDir -ItemType Directory -Force | Out-Null
        }
        
        # Copy application files
        Write-Log "Copying application files..." "INFO"
        
        # Copy backend
        $backendTarget = "$deploymentDir\backend"
        if (Test-Path $backendTarget) {
            Remove-Item -Path $backendTarget -Recurse -Force
        }
        Copy-Item -Path "$ProjectRoot\backend" -Destination $backendTarget -Recurse
        
        # Copy frontend
        $frontendTarget = "$deploymentDir\frontend-javafx"
        if (Test-Path $frontendTarget) {
            Remove-Item -Path $frontendTarget -Recurse -Force
        }
        Copy-Item -Path "$ProjectRoot\frontend-javafx" -Destination $frontendTarget -Recurse
        
        # Create deployment info file
        $deploymentInfo = @{
            DeploymentId = $DeploymentId
            Environment = $Environment
            Version = $Version
            Timestamp = $TimeStamp
            DeployedBy = $env:USERNAME
        }
        
        $deploymentInfo | ConvertTo-Json -Depth 3 | Out-File "$deploymentDir\deployment-info.json"
        
        Write-Log "Application deployment completed successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Application deployment failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Main Deployment Process
function Start-Deployment {
    Write-Log "Starting deployment process..." "INFO"
    Write-Log "Environment: $Environment" "INFO"
    Write-Log "Version: $Version" "INFO"
    Write-Log "Deployment ID: $DeploymentId" "INFO"
    
    if ($DryRun) {
        Write-Log "DRY RUN MODE - No actual deployment will be performed" "WARNING"
    }
    
    try {
        # Pre-deployment checks
        if (-not (Test-PreDeploymentChecks)) {
            throw "Pre-deployment checks failed"
        }
        
        # Build application
        if (-not (Build-Application)) {
            throw "Application build failed"
        }
        
        # Deploy application
        if (-not $DryRun) {
            if (-not (Deploy-Application)) {
                throw "Application deployment failed"
            }
        }
        
        Write-Log "Deployment completed successfully!" "SUCCESS"
        Write-Log "Deployment ID: $DeploymentId" "SUCCESS"
        
        return 0
        
    } catch {
        Write-Log "Deployment failed: $($_.Exception.Message)" "ERROR"
        return 1
    }
}

# Main execution
Write-Host "Food Ordering System - Deployment Automation" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

$exitCode = Start-Deployment
Write-Log "Deployment process completed with exit code: $exitCode" "INFO"
Write-Log "Log file: $LogFile" "INFO"

exit $exitCode 
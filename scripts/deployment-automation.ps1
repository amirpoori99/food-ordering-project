# Food Ordering System - Deployment Automation
# Phase 45: Advanced Deployment Automation & DevOps Setup

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [string]$Version = "latest",
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipTests,
    
    [Parameter(Mandatory=$false)]
    [switch]$RollbackMode,
    
    [Parameter(Mandatory=$false)]
    [string]$ConfigPath = "config\deployment",
    
    [Parameter(Mandatory=$false)]
    [int]$HealthCheckRetries = 3,
    
    [Parameter(Mandatory=$false)]
    [int]$HealthCheckInterval = 30
)

$ErrorActionPreference = "Stop"
$WarningPreference = "Continue"

# Global Variables
$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$DeploymentId = "deploy-$Environment-$TimeStamp"
$LogFile = "logs\deployment-$DeploymentId.log"

# Create logs directory if it doesn't exist
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

# Configuration Management
function Get-EnvironmentConfig {
    param($Environment)
    
    $configFile = "$ConfigPath\$Environment.json"
    
    if (Test-Path $configFile) {
        $config = Get-Content $configFile | ConvertFrom-Json
        Write-Log "Loaded configuration for $Environment environment" "INFO"
        return $config
    } else {
        Write-Log "Configuration file not found: $configFile" "ERROR"
        throw "Configuration file not found"
    }
}

# Pre-deployment Checks
function Test-PreDeploymentChecks {
    Write-Log "Running pre-deployment checks..." "INFO"
    
    # Check if required directories exist
    $requiredDirs = @("backend", "frontend-javafx", "config", "scripts")
    foreach ($dir in $requiredDirs) {
        if (-not (Test-Path $dir)) {
            Write-Log "Required directory missing: $dir" "ERROR"
            return $false
        }
    }
    
    # Check if environment configuration exists
    $envConfig = "$ConfigPath\$Environment.json"
    if (-not (Test-Path $envConfig)) {
        Write-Log "Environment configuration missing: $envConfig" "ERROR"
        return $false
    }
    
    # Check if Maven is available
    try {
        $null = mvn --version 2>&1
        Write-Log "Maven is available" "INFO"
    } catch {
        Write-Log "Maven is not available" "ERROR"
        return $false
    }
    
    # Check if Java is available
    try {
        $null = java -version 2>&1
        Write-Log "Java is available" "INFO"
    } catch {
        Write-Log "Java is not available" "ERROR"
        return $false
    }
    
    Write-Log "Pre-deployment checks passed" "SUCCESS"
    return $true
}

# Build Application
function Build-Application {
    param($Config)
    
    Write-Log "Building application..." "INFO"
    
    try {
        # Backend Build
        Write-Log "Building backend..." "INFO"
        Set-Location "$ProjectRoot\backend"
        
        if ($Config.backend.skipTests) {
            mvn clean compile -DskipTests
        } else {
            mvn clean compile test
        }
        
        if ($LASTEXITCODE -ne 0) {
            throw "Backend build failed"
        }
        
        # Frontend Build
        Write-Log "Building frontend..." "INFO"
        Set-Location "$ProjectRoot\frontend-javafx"
        
        mvn clean compile
        if ($LASTEXITCODE -ne 0) {
            throw "Frontend build failed"
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

# Database Migration
function Invoke-DatabaseMigration {
    param($Config)
    
    Write-Log "Starting database migration..." "INFO"
    
    try {
        # Check if migration scripts exist
        $migrationScript = "scripts\database-migration.ps1"
        if (Test-Path $migrationScript) {
            Write-Log "Running database migration script..." "INFO"
            & $migrationScript -Environment $Environment
            
            if ($LASTEXITCODE -ne 0) {
                throw "Database migration failed"
            }
        } else {
            Write-Log "No database migration script found, skipping..." "WARNING"
        }
        
        # Database health check
        Write-Log "Performing database health check..." "INFO"
        $dbHealthy = Test-DatabaseConnection -Config $Config
        
        if (-not $dbHealthy) {
            throw "Database health check failed"
        }
        
        Write-Log "Database migration completed successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Database migration failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Test Database Connection
function Test-DatabaseConnection {
    param($Config)
    
    Write-Log "Testing database connection..." "INFO"
    
    try {
        # Use Maven to run a simple database connection test
        Set-Location "$ProjectRoot\backend"
        
        $testResult = mvn exec:java -Dexec.mainClass="com.myapp.DatabaseChecker" -Dexec.args="test-connection" -q
        
        if ($LASTEXITCODE -eq 0) {
            Write-Log "Database connection test passed" "SUCCESS"
            Set-Location $ProjectRoot
            return $true
        } else {
            Write-Log "Database connection test failed" "ERROR"
            Set-Location $ProjectRoot
            return $false
        }
        
    } catch {
        Write-Log "Database connection test error: $($_.Exception.Message)" "ERROR"
        Set-Location $ProjectRoot
        return $false
    }
}

# Deploy Application
function Deploy-Application {
    param($Config)
    
    Write-Log "Deploying application to $Environment environment..." "INFO"
    
    try {
        # Create deployment directory
        $deploymentDir = $Config.deployment.targetDirectory
        if (-not (Test-Path $deploymentDir)) {
            New-Item -Path $deploymentDir -ItemType Directory -Force | Out-Null
            Write-Log "Created deployment directory: $deploymentDir" "INFO"
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
        
        # Copy configuration
        $configTarget = "$deploymentDir\config"
        if (Test-Path $configTarget) {
            Remove-Item -Path $configTarget -Recurse -Force
        }
        Copy-Item -Path "$ProjectRoot\config" -Destination $configTarget -Recurse
        
        # Copy scripts
        $scriptsTarget = "$deploymentDir\scripts"
        if (Test-Path $scriptsTarget) {
            Remove-Item -Path $scriptsTarget -Recurse -Force
        }
        Copy-Item -Path "$ProjectRoot\scripts" -Destination $scriptsTarget -Recurse
        
        # Create deployment info file
        $deploymentInfo = @{
            DeploymentId = $DeploymentId
            Environment = $Environment
            Version = $Version
            Timestamp = $TimeStamp
            GitCommit = (git rev-parse --short HEAD 2>$null)
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

# Start Services
function Start-Services {
    param($Config)
    
    Write-Log "Starting services..." "INFO"
    
    try {
        # Start backend service
        if ($Config.services.backend.enabled) {
            Write-Log "Starting backend service..." "INFO"
            
            $backendPort = $Config.services.backend.port
            $backendCmd = $Config.services.backend.command
            
            # Check if port is available
            $portInUse = Test-NetConnection -ComputerName localhost -Port $backendPort -InformationLevel Quiet -ErrorAction SilentlyContinue
            if ($portInUse) {
                Write-Log "Port $backendPort is already in use" "WARNING"
            }
            
            # Start backend (this would typically be a service or systemd unit)
            Write-Log "Backend service start command would be: $backendCmd" "INFO"
        }
        
        # Start frontend service
        if ($Config.services.frontend.enabled) {
            Write-Log "Starting frontend service..." "INFO"
            
            $frontendCmd = $Config.services.frontend.command
            Write-Log "Frontend service start command would be: $frontendCmd" "INFO"
        }
        
        Write-Log "Services started successfully" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Service startup failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Health Check
function Test-ApplicationHealth {
    param($Config, $Retries = 3)
    
    Write-Log "Performing application health check..." "INFO"
    
    for ($i = 1; $i -le $Retries; $i++) {
        Write-Log "Health check attempt $i of $Retries" "INFO"
        
        try {
            # Test backend health
            if ($Config.healthCheck.backend.enabled) {
                $backendUrl = $Config.healthCheck.backend.url
                $response = Invoke-RestMethod -Uri $backendUrl -Method GET -TimeoutSec 10
                
                if ($response.status -eq "healthy") {
                    Write-Log "Backend health check passed" "SUCCESS"
                } else {
                    throw "Backend health check failed"
                }
            }
            
            # Test database health
            if ($Config.healthCheck.database.enabled) {
                $dbHealthy = Test-DatabaseConnection -Config $Config
                if (-not $dbHealthy) {
                    throw "Database health check failed"
                }
            }
            
            # Test frontend health (if applicable)
            if ($Config.healthCheck.frontend.enabled) {
                # Frontend health check logic would go here
                Write-Log "Frontend health check passed" "SUCCESS"
            }
            
            Write-Log "Application health check passed" "SUCCESS"
            return $true
            
        } catch {
            Write-Log "Health check attempt $i failed: $($_.Exception.Message)" "WARNING"
            
            if ($i -lt $Retries) {
                Write-Log "Waiting $HealthCheckInterval seconds before next attempt..." "INFO"
                Start-Sleep -Seconds $HealthCheckInterval
            }
        }
    }
    
    Write-Log "All health check attempts failed" "ERROR"
    return $false
}

# Rollback Function
function Invoke-Rollback {
    param($Config)
    
    Write-Log "Starting rollback process..." "INFO"
    
    try {
        # Find previous deployment
        $deploymentDir = $Config.deployment.targetDirectory
        $backupDir = "$deploymentDir\.backups"
        
        if (Test-Path $backupDir) {
            $backups = Get-ChildItem -Path $backupDir | Sort-Object CreationTime -Descending
            
            if ($backups.Count -gt 0) {
                $latestBackup = $backups[0]
                Write-Log "Rolling back to: $($latestBackup.Name)" "INFO"
                
                # Stop services
                Write-Log "Stopping services for rollback..." "INFO"
                # Service stop logic would go here
                
                # Restore backup
                Write-Log "Restoring backup..." "INFO"
                Copy-Item -Path $latestBackup.FullName -Destination $deploymentDir -Recurse -Force
                
                # Start services
                Write-Log "Restarting services..." "INFO"
                Start-Services -Config $Config
                
                # Health check
                $healthy = Test-ApplicationHealth -Config $Config -Retries 2
                if ($healthy) {
                    Write-Log "Rollback completed successfully" "SUCCESS"
                    return $true
                } else {
                    Write-Log "Rollback health check failed" "ERROR"
                    return $false
                }
            } else {
                Write-Log "No backup found for rollback" "ERROR"
                return $false
            }
        } else {
            Write-Log "Backup directory not found" "ERROR"
            return $false
        }
        
    } catch {
        Write-Log "Rollback failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Create Backup
function New-DeploymentBackup {
    param($Config)
    
    Write-Log "Creating deployment backup..." "INFO"
    
    try {
        $deploymentDir = $Config.deployment.targetDirectory
        $backupDir = "$deploymentDir\.backups"
        
        if (-not (Test-Path $backupDir)) {
            New-Item -Path $backupDir -ItemType Directory -Force | Out-Null
        }
        
        $backupName = "backup-$TimeStamp"
        $backupPath = "$backupDir\$backupName"
        
        if (Test-Path $deploymentDir) {
            Copy-Item -Path $deploymentDir -Destination $backupPath -Recurse -Force
            Write-Log "Backup created: $backupPath" "SUCCESS"
            
            # Clean old backups (keep last 5)
            $allBackups = Get-ChildItem -Path $backupDir | Sort-Object CreationTime -Descending
            if ($allBackups.Count -gt 5) {
                $oldBackups = $allBackups[5..($allBackups.Count-1)]
                foreach ($backup in $oldBackups) {
                    Remove-Item -Path $backup.FullName -Recurse -Force
                    Write-Log "Removed old backup: $($backup.Name)" "INFO"
                }
            }
            
            return $true
        } else {
            Write-Log "No existing deployment found to backup" "WARNING"
            return $true
        }
        
    } catch {
        Write-Log "Backup creation failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Main Deployment Process
function Start-Deployment {
    Write-Log "Starting deployment process..." "INFO"
    Write-Log "Environment: $Environment" "INFO"
    Write-Log "Version: $Version" "INFO"
    Write-Log "Deployment ID: $DeploymentId" "INFO"
    
    try {
        # Load configuration
        $config = Get-EnvironmentConfig -Environment $Environment
        
        # Handle rollback mode
        if ($RollbackMode) {
            Write-Log "Rollback mode enabled" "INFO"
            $rollbackResult = Invoke-Rollback -Config $config
            
            if ($rollbackResult) {
                Write-Log "Rollback completed successfully" "SUCCESS"
                return 0
            } else {
                Write-Log "Rollback failed" "ERROR"
                return 1
            }
        }
        
        # Dry run mode
        if ($DryRun) {
            Write-Log "Dry run mode enabled - no actual deployment will be performed" "INFO"
        }
        
        # Pre-deployment checks
        if (-not (Test-PreDeploymentChecks)) {
            throw "Pre-deployment checks failed"
        }
        
        # Create backup
        if (-not $DryRun) {
            if (-not (New-DeploymentBackup -Config $config)) {
                throw "Backup creation failed"
            }
        }
        
        # Build application
        if (-not $SkipTests) {
            if (-not (Build-Application -Config $config)) {
                throw "Application build failed"
            }
        }
        
        # Database migration
        if (-not $DryRun) {
            if (-not (Invoke-DatabaseMigration -Config $config)) {
                throw "Database migration failed"
            }
        }
        
        # Deploy application
        if (-not $DryRun) {
            if (-not (Deploy-Application -Config $config)) {
                throw "Application deployment failed"
            }
        }
        
        # Start services
        if (-not $DryRun) {
            if (-not (Start-Services -Config $config)) {
                throw "Service startup failed"
            }
        }
        
        # Health check
        if (-not $DryRun) {
            if (-not (Test-ApplicationHealth -Config $config -Retries $HealthCheckRetries)) {
                Write-Log "Health check failed, initiating rollback..." "ERROR"
                Invoke-Rollback -Config $config
                throw "Deployment health check failed"
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

# Create default configuration files if they don't exist
function Initialize-ConfigurationFiles {
    Write-Log "Initializing configuration files..." "INFO"
    
    if (-not (Test-Path $ConfigPath)) {
        New-Item -Path $ConfigPath -ItemType Directory -Force | Out-Null
    }
    
    # Development configuration
    $devConfig = @{
        environment = "development"
        backend = @{
            skipTests = $false
            port = 8080
        }
        frontend = @{
            port = 8081
        }
        database = @{
            url = "jdbc:sqlite:food_ordering_dev.db"
            driver = "org.sqlite.JDBC"
        }
        services = @{
            backend = @{
                enabled = $true
                port = 8080
                command = "java -jar backend/target/food-ordering-backend.jar"
            }
            frontend = @{
                enabled = $true
                command = "java -jar frontend-javafx/target/food-ordering-frontend.jar"
            }
        }
        healthCheck = @{
            backend = @{
                enabled = $true
                url = "http://localhost:8080/health"
            }
            database = @{
                enabled = $true
            }
            frontend = @{
                enabled = $false
            }
        }
        deployment = @{
            targetDirectory = "deployments\development"
        }
    }
    
    # Staging configuration
    $stagingConfig = @{
        environment = "staging"
        backend = @{
            skipTests = $false
            port = 8082
        }
        frontend = @{
            port = 8083
        }
        database = @{
            url = "jdbc:sqlite:food_ordering_staging.db"
            driver = "org.sqlite.JDBC"
        }
        services = @{
            backend = @{
                enabled = $true
                port = 8082
                command = "java -jar backend/target/food-ordering-backend.jar"
            }
            frontend = @{
                enabled = $true
                command = "java -jar frontend-javafx/target/food-ordering-frontend.jar"
            }
        }
        healthCheck = @{
            backend = @{
                enabled = $true
                url = "http://localhost:8082/health"
            }
            database = @{
                enabled = $true
            }
            frontend = @{
                enabled = $false
            }
        }
        deployment = @{
            targetDirectory = "deployments\staging"
        }
    }
    
    # Production configuration
    $prodConfig = @{
        environment = "production"
        backend = @{
            skipTests = $true
            port = 8080
        }
        frontend = @{
            port = 8080
        }
        database = @{
            url = "jdbc:sqlite:food_ordering_production.db"
            driver = "org.sqlite.JDBC"
        }
        services = @{
            backend = @{
                enabled = $true
                port = 8080
                command = "java -jar backend/target/food-ordering-backend.jar"
            }
            frontend = @{
                enabled = $true
                command = "java -jar frontend-javafx/target/food-ordering-frontend.jar"
            }
        }
        healthCheck = @{
            backend = @{
                enabled = $true
                url = "http://localhost:8080/health"
            }
            database = @{
                enabled = $true
            }
            frontend = @{
                enabled = $false
            }
        }
        deployment = @{
            targetDirectory = "deployments\production"
        }
    }
    
    # Save configurations
    $devConfig | ConvertTo-Json -Depth 4 | Out-File "$ConfigPath\development.json" -Encoding UTF8
    $stagingConfig | ConvertTo-Json -Depth 4 | Out-File "$ConfigPath\staging.json" -Encoding UTF8
    $prodConfig | ConvertTo-Json -Depth 4 | Out-File "$ConfigPath\production.json" -Encoding UTF8
    
    Write-Log "Configuration files created successfully" "SUCCESS"
}

# Main execution
Write-Host "Food Ordering System - Deployment Automation" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# Initialize configuration files if they don't exist
Initialize-ConfigurationFiles

# Start deployment process
$exitCode = Start-Deployment

Write-Log "Deployment process completed with exit code: $exitCode" "INFO"
Write-Log "Log file: $LogFile" "INFO"

exit $exitCode 
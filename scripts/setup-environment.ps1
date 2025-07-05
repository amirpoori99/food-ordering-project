# Food Ordering System - Environment Setup Script
# Phase 45: Environment Setup & Configuration

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipDependencies,
    
    [Parameter(Mandatory=$false)]
    [switch]$Force,
    
    [Parameter(Mandatory=$false)]
    [string]$ConfigPath = "config\deployment"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile = "logs\environment-setup-$Environment-$TimeStamp.log"

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

# Load Environment Configuration
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

# Check System Prerequisites
function Test-SystemPrerequisites {
    Write-Log "Checking system prerequisites..." "INFO"
    
    $prerequisites = @()
    
    # Check Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString() }
        Write-Log "Java found: $javaVersion" "SUCCESS"
    } catch {
        $prerequisites += "Java JDK 17 or higher"
        Write-Log "Java not found" "ERROR"
    }
    
    # Check Maven
    try {
        $mavenVersion = mvn --version | Select-String "Apache Maven" | ForEach-Object { $_.ToString() }
        Write-Log "Maven found: $mavenVersion" "SUCCESS"
    } catch {
        $prerequisites += "Apache Maven"
        Write-Log "Maven not found" "ERROR"
    }
    
    # Check Git
    try {
        $gitVersion = git --version 2>&1 | ForEach-Object { $_.ToString() }
        Write-Log "Git found: $gitVersion" "SUCCESS"
    } catch {
        $prerequisites += "Git"
        Write-Log "Git not found" "WARNING"
    }
    
    if ($prerequisites.Count -gt 0 -and -not $SkipDependencies) {
        Write-Log "Missing prerequisites: $($prerequisites -join ', ')" "ERROR"
        throw "Missing required prerequisites"
    }
    
    Write-Log "System prerequisites check completed" "SUCCESS"
    return $true
}

# Create Directory Structure
function New-DirectoryStructure {
    param($Config)
    
    Write-Log "Creating directory structure for $Environment environment..." "INFO"
    
    $directories = @(
        $Config.deployment.targetDirectory,
        "$($Config.deployment.targetDirectory)\logs",
        "$($Config.deployment.targetDirectory)\config",
        "$($Config.deployment.targetDirectory)\scripts",
        "$($Config.deployment.targetDirectory)\data",
        "$($Config.deployment.targetDirectory)\backups",
        "logs\$Environment",
        "database\$Environment"
    )
    
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -Path $dir -ItemType Directory -Force | Out-Null
            Write-Log "Created directory: $dir" "INFO"
        } else {
            Write-Log "Directory already exists: $dir" "INFO"
        }
    }
    
    Write-Log "Directory structure created successfully" "SUCCESS"
}

# Setup Database
function Initialize-Database {
    param($Config)
    
    Write-Log "Initializing database for $Environment environment..." "INFO"
    
    try {
        $dbPath = "database\$Environment"
        $dbFile = "$dbPath\food_ordering_$Environment.db"
        
        # Create database directory
        if (-not (Test-Path $dbPath)) {
            New-Item -Path $dbPath -ItemType Directory -Force | Out-Null
        }
        
        # Initialize database if it doesn't exist
        if (-not (Test-Path $dbFile) -or $Force) {
            Write-Log "Creating new database: $dbFile" "INFO"
            
            # Copy database schema if exists
            if (Test-Path "scripts\database-setup.sql") {
                Write-Log "Database schema file found, initializing..." "INFO"
                
                # Use Maven to run database initialization
                Set-Location "$ProjectRoot\backend"
                $env:DB_PATH = $dbFile
                mvn exec:java -Dexec.mainClass="com.myapp.DatabasePopulator" -Dexec.args="--environment=$Environment --init"
                Set-Location $ProjectRoot
                
                if ($LASTEXITCODE -eq 0) {
                    Write-Log "Database initialized successfully" "SUCCESS"
                } else {
                    Write-Log "Database initialization failed" "ERROR"
                    throw "Database initialization failed"
                }
            } else {
                Write-Log "No database schema file found, creating empty database" "WARNING"
                # Create empty SQLite database
                New-Item -Path $dbFile -ItemType File -Force | Out-Null
            }
        } else {
            Write-Log "Database already exists: $dbFile" "INFO"
        }
        
        return $true
        
    } catch {
        Write-Log "Database initialization failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Configure Application Properties
function Set-ApplicationConfiguration {
    param($Config)
    
    Write-Log "Configuring application properties for $Environment..." "INFO"
    
    try {
        $targetDir = $Config.deployment.targetDirectory
        
        # Create application properties file
        $appPropsPath = "$targetDir\config\application-$Environment.properties"
        
        $appProperties = @"
# Food Ordering System - $Environment Environment Configuration
# Generated on $(Get-Date)

# Server Configuration
server.port=$($Config.backend.port)
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=$($Config.database.url)
spring.datasource.driver-class-name=$($Config.database.driver)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=$($Environment -eq 'development')

# Logging Configuration
logging.level.com.myapp=$($Config.monitoring.loggingLevel)
logging.file.name=logs/$Environment/application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Monitoring Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true

# Environment Specific Settings
app.environment=$Environment
app.version=1.0.0
"@

        $appProperties | Out-File -FilePath $appPropsPath -Encoding UTF8
        Write-Log "Application configuration created: $appPropsPath" "SUCCESS"
        
        # Create JVM arguments file
        $jvmArgsPath = "$targetDir\config\jvm-args-$Environment.txt"
        $jvmArgs = $Config.backend.jvmArgs -join "`n"
        $jvmArgs | Out-File -FilePath $jvmArgsPath -Encoding UTF8
        Write-Log "JVM arguments file created: $jvmArgsPath" "SUCCESS"
        
        return $true
        
    } catch {
        Write-Log "Configuration setup failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Setup Monitoring
function Initialize-Monitoring {
    param($Config)
    
    if (-not $Config.monitoring.enabled) {
        Write-Log "Monitoring disabled for $Environment environment" "INFO"
        return $true
    }
    
    Write-Log "Setting up monitoring for $Environment environment..." "INFO"
    
    try {
        $monitoringDir = "$($Config.deployment.targetDirectory)\monitoring"
        if (-not (Test-Path $monitoringDir)) {
            New-Item -Path $monitoringDir -ItemType Directory -Force | Out-Null
        }
        
        # Create monitoring configuration
        $monitoringConfig = @{
            environment = $Environment
            metricsPort = $Config.monitoring.metricsPort
            loggingLevel = $Config.monitoring.loggingLevel
            alerts = @{
                enabled = $true
                endpoints = @("http://localhost:$($Config.backend.port)/actuator/health")
            }
        }
        
        $monitoringConfig | ConvertTo-Json -Depth 3 | Out-File "$monitoringDir\monitoring-config.json" -Encoding UTF8
        Write-Log "Monitoring configuration created" "SUCCESS"
        
        return $true
        
    } catch {
        Write-Log "Monitoring setup failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Create Service Scripts
function New-ServiceScripts {
    param($Config)
    
    Write-Log "Creating service scripts for $Environment environment..." "INFO"
    
    try {
        $scriptsDir = "$($Config.deployment.targetDirectory)\scripts"
        
        # Create start script
        $startScript = @"
@echo off
REM Food Ordering System - Start Script for $Environment
echo Starting Food Ordering System ($Environment environment)...

set JAVA_OPTS=$(($Config.backend.jvmArgs) -join ' ')
set SERVER_PORT=$($Config.backend.port)
set SPRING_PROFILES_ACTIVE=$Environment

cd /d "%~dp0.."
java %JAVA_OPTS% -jar backend\target\food-ordering-backend.jar

pause
"@
        
        $startScript | Out-File -FilePath "$scriptsDir\start-$Environment.bat" -Encoding UTF8
        Write-Log "Start script created: start-$Environment.bat" "SUCCESS"
        
        # Create stop script
        $stopScript = @"
@echo off
REM Food Ordering System - Stop Script for $Environment
echo Stopping Food Ordering System ($Environment environment)...

for /f "tokens=5" %%a in ('netstat -aon ^| find ":$($Config.backend.port)" ^| find "LISTENING"') do taskkill /f /pid %%a

echo Service stopped.
pause
"@
        
        $stopScript | Out-File -FilePath "$scriptsDir\stop-$Environment.bat" -Encoding UTF8
        Write-Log "Stop script created: stop-$Environment.bat" "SUCCESS"
        
        # Create health check script
        $healthScript = @"
@echo off
REM Food Ordering System - Health Check Script for $Environment
echo Checking health of Food Ordering System ($Environment environment)...

curl -f http://localhost:$($Config.backend.port)/actuator/health || echo Health check failed

pause
"@
        
        $healthScript | Out-File -FilePath "$scriptsDir\health-check-$Environment.bat" -Encoding UTF8
        Write-Log "Health check script created: health-check-$Environment.bat" "SUCCESS"
        
        return $true
        
    } catch {
        Write-Log "Service scripts creation failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Create Environment Info
function New-EnvironmentInfo {
    param($Config)
    
    Write-Log "Creating environment information file..." "INFO"
    
    try {
        $envInfo = @{
            environment = $Environment
            setupDate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            setupBy = $env:USERNAME
            configuration = $Config
            directories = @{
                target = $Config.deployment.targetDirectory
                logs = "logs\$Environment"
                database = "database\$Environment"
            }
            services = @{
                backendPort = $Config.backend.port
                frontendPort = $Config.frontend.port
                monitoringPort = $Config.monitoring.metricsPort
            }
            scripts = @{
                start = "scripts\start-$Environment.bat"
                stop = "scripts\stop-$Environment.bat"
                healthCheck = "scripts\health-check-$Environment.bat"
            }
        }
        
        $envInfoPath = "$($Config.deployment.targetDirectory)\environment-info.json"
        $envInfo | ConvertTo-Json -Depth 4 | Out-File $envInfoPath -Encoding UTF8
        Write-Log "Environment info created: $envInfoPath" "SUCCESS"
        
        return $true
        
    } catch {
        Write-Log "Environment info creation failed: $($_.Exception.Message)" "ERROR"
        return $false
    }
}

# Main Setup Process
function Start-EnvironmentSetup {
    Write-Log "Starting environment setup for $Environment..." "INFO"
    Write-Log "Setup ID: env-setup-$Environment-$TimeStamp" "INFO"
    
    try {
        # Load configuration
        $config = Get-EnvironmentConfig -Environment $Environment
        
        # Check prerequisites
        if (-not (Test-SystemPrerequisites)) {
            throw "System prerequisites check failed"
        }
        
        # Create directory structure
        New-DirectoryStructure -Config $config
        
        # Initialize database
        if (-not (Initialize-Database -Config $config)) {
            throw "Database initialization failed"
        }
        
        # Configure application
        if (-not (Set-ApplicationConfiguration -Config $config)) {
            throw "Application configuration failed"
        }
        
        # Setup monitoring
        if (-not (Initialize-Monitoring -Config $config)) {
            throw "Monitoring setup failed"
        }
        
        # Create service scripts
        if (-not (New-ServiceScripts -Config $config)) {
            throw "Service scripts creation failed"
        }
        
        # Create environment info
        if (-not (New-EnvironmentInfo -Config $config)) {
            throw "Environment info creation failed"
        }
        
        Write-Log "$Environment environment setup completed successfully!" "SUCCESS"
        Write-Log "Target Directory: $($config.deployment.targetDirectory)" "SUCCESS"
        Write-Log "Backend Port: $($config.backend.port)" "SUCCESS"
        Write-Log "Frontend Port: $($config.frontend.port)" "SUCCESS"
        
        return 0
        
    } catch {
        Write-Log "Environment setup failed: $($_.Exception.Message)" "ERROR"
        return 1
    }
}

# Main execution
Write-Host "Food Ordering System - Environment Setup" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$exitCode = Start-EnvironmentSetup
Write-Log "Environment setup completed with exit code: $exitCode" "INFO"
Write-Log "Log file: $LogFile" "INFO"

exit $exitCode 
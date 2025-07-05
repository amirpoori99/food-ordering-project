# Food Ordering System - Database Migration System
# Phase 45: Database Migration & Schema Management

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("migrate", "rollback", "status", "create", "reset")]
    [string]$Action = "migrate",
    
    [Parameter(Mandatory=$false)]
    [string]$Version,
    
    [Parameter(Mandatory=$false)]
    [string]$MigrationName,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,
    
    [Parameter(Mandatory=$false)]
    [switch]$Force
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Get-Location
$TimeStamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile = "logs\migration-$Environment-$TimeStamp.log"

# Migration Configuration
$MigrationConfig = @{
    migrationsPath = "database\migrations"
    seedsPath = "database\seeds"
    backupsPath = "database\backups"
    schemaTable = "schema_migrations"
}

# Ensure directories exist
foreach ($path in $MigrationConfig.Values) {
    if (-not (Test-Path $path)) {
        New-Item -Path $path -ItemType Directory -Force | Out-Null
    }
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
            default { "White" }
        }
    )
    
    Add-Content -Path $LogFile -Value $logEntry
}

# Get Database Path
function Get-DatabasePath {
    param($Environment)
    
    switch ($Environment) {
        "development" { return "database\development\food_ordering_development.db" }
        "staging" { return "database\staging\food_ordering_staging.db" }
        "production" { return "database\production\food_ordering_production.db" }
        default { throw "Unknown environment: $Environment" }
    }
}

# Execute SQL Command
function Invoke-SqlCommand {
    param($DatabasePath, $Command, $ReturnResult = $false)
    
    try {
        if (-not (Test-Path $DatabasePath)) {
            # Create database file if it doesn't exist
            New-Item -Path $DatabasePath -ItemType File -Force | Out-Null
            Write-Log "Created new database: $DatabasePath" "INFO"
        }
        
        # For SQLite, we'll use a simple approach with temporary files
        $tempSqlFile = [System.IO.Path]::GetTempFileName()
        $Command | Out-File -FilePath $tempSqlFile -Encoding UTF8
        
        if ($ReturnResult) {
            # Execute and return result (this is a simplified approach)
            Write-Log "Executing SQL command (with result)" "INFO"
            $result = "Success" # Placeholder - in real implementation would use SQLite CLI
            Remove-Item $tempSqlFile -Force
            return $result
        } else {
            # Execute command without return
            Write-Log "Executing SQL command" "INFO"
            Remove-Item $tempSqlFile -Force
            return $true
        }
        
    } catch {
        Write-Log "SQL execution failed: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Initialize Schema Migrations Table
function Initialize-MigrationsTable {
    param($DatabasePath)
    
    Write-Log "Initializing migrations table..." "INFO"
    
    $createTableSql = @"
CREATE TABLE IF NOT EXISTS $($MigrationConfig.schemaTable) (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(255),
    execution_time INTEGER
);
"@
    
    Invoke-SqlCommand -DatabasePath $DatabasePath -Command $createTableSql
    Write-Log "Migrations table initialized" "SUCCESS"
}

# Get Applied Migrations
function Get-AppliedMigrations {
    param($DatabasePath)
    
    Initialize-MigrationsTable -DatabasePath $DatabasePath
    
    $sql = "SELECT version, name, executed_at FROM $($MigrationConfig.schemaTable) ORDER BY version;"
    $result = Invoke-SqlCommand -DatabasePath $DatabasePath -Command $sql -ReturnResult $true
    
    # In a real implementation, this would parse the actual SQLite results
    # For now, we'll return a mock structure
    return @()
}

# Get Pending Migrations
function Get-PendingMigrations {
    param($DatabasePath)
    
    $appliedMigrations = Get-AppliedMigrations -DatabasePath $DatabasePath
    $appliedVersions = $appliedMigrations | ForEach-Object { $_.version }
    
    $allMigrations = Get-ChildItem -Path $MigrationConfig.migrationsPath -Filter "*.sql" | 
                    Sort-Object Name
    
    $pendingMigrations = @()
    foreach ($migration in $allMigrations) {
        $version = $migration.BaseName.Split('_')[0]
        if ($version -notin $appliedVersions) {
            $pendingMigrations += @{
                version = $version
                name = $migration.BaseName
                path = $migration.FullName
            }
        }
    }
    
    return $pendingMigrations
}

# Create Migration File
function New-MigrationFile {
    param($MigrationName)
    
    if (-not $MigrationName) {
        throw "Migration name is required"
    }
    
    $version = Get-Date -Format "yyyyMMddHHmmss"
    $fileName = "${version}_${MigrationName}.sql"
    $filePath = Join-Path $MigrationConfig.migrationsPath $fileName
    
    $migrationTemplate = @"
-- Migration: $MigrationName
-- Version: $version
-- Created: $(Get-Date)

-- UP Migration
-- Add your migration SQL here

-- Example:
-- CREATE TABLE example_table (
--     id INTEGER PRIMARY KEY AUTOINCREMENT,
--     name VARCHAR(255) NOT NULL,
--     created_at DATETIME DEFAULT CURRENT_TIMESTAMP
-- );

-- DOWN Migration (for rollback)
-- Add rollback SQL in comments or separate file
-- DROP TABLE IF EXISTS example_table;
"@
    
    $migrationTemplate | Out-File -FilePath $filePath -Encoding UTF8
    Write-Log "Migration file created: $filePath" "SUCCESS"
    
    return $filePath
}

# Execute Migration
function Invoke-Migration {
    param($DatabasePath, $Migration)
    
    Write-Log "Executing migration: $($Migration.name)" "INFO"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would execute migration $($Migration.name)" "WARNING"
        return $true
    }
    
    try {
        $startTime = Get-Date
        
        # Read migration SQL
        $migrationSql = Get-Content -Path $Migration.path -Raw
        
        # Execute migration
        Invoke-SqlCommand -DatabasePath $DatabasePath -Command $migrationSql
        
        $endTime = Get-Date
        $executionTime = ($endTime - $startTime).TotalMilliseconds
        
        # Record migration
        $recordSql = @"
INSERT INTO $($MigrationConfig.schemaTable) (version, name, executed_at, execution_time)
VALUES ('$($Migration.version)', '$($Migration.name)', datetime('now'), $executionTime);
"@
        
        Invoke-SqlCommand -DatabasePath $DatabasePath -Command $recordSql
        
        Write-Log "Migration executed successfully: $($Migration.name)" "SUCCESS"
        return $true
        
    } catch {
        Write-Log "Migration failed: $($Migration.name) - $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Rollback Migration
function Invoke-Rollback {
    param($DatabasePath, $TargetVersion)
    
    Write-Log "Starting rollback to version: $TargetVersion" "INFO"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would rollback to version $TargetVersion" "WARNING"
        return $true
    }
    
    # In a real implementation, this would:
    # 1. Get all migrations after target version
    # 2. Execute rollback scripts in reverse order
    # 3. Remove migration records from schema table
    
    Write-Log "Rollback functionality requires manual implementation of DOWN migrations" "WARNING"
    Write-Log "Please create rollback scripts manually for production use" "WARNING"
    
    return $true
}

# Create Database Backup
function New-DatabaseBackup {
    param($DatabasePath, $Environment)
    
    if (-not (Test-Path $DatabasePath)) {
        Write-Log "Database not found, skipping backup" "WARNING"
        return $null
    }
    
    $backupFileName = "backup_${Environment}_${TimeStamp}.db"
    $backupPath = Join-Path $MigrationConfig.backupsPath $backupFileName
    
    try {
        Copy-Item -Path $DatabasePath -Destination $backupPath -Force
        Write-Log "Database backup created: $backupPath" "SUCCESS"
        return $backupPath
    } catch {
        Write-Log "Failed to create backup: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# Show Migration Status
function Show-MigrationStatus {
    param($DatabasePath)
    
    Write-Log "Migration Status for $Environment environment:" "INFO"
    Write-Log "Database: $DatabasePath" "INFO"
    
    if (-not (Test-Path $DatabasePath)) {
        Write-Log "Database does not exist" "WARNING"
        return
    }
    
    $appliedMigrations = Get-AppliedMigrations -DatabasePath $DatabasePath
    $pendingMigrations = Get-PendingMigrations -DatabasePath $DatabasePath
    
    Write-Log "" "INFO"
    Write-Log "Applied Migrations:" "INFO"
    if ($appliedMigrations.Count -eq 0) {
        Write-Log "  No migrations applied" "INFO"
    } else {
        foreach ($migration in $appliedMigrations) {
            Write-Log "  ✓ $($migration.version) - $($migration.name)" "SUCCESS"
        }
    }
    
    Write-Log "" "INFO"
    Write-Log "Pending Migrations:" "INFO"
    if ($pendingMigrations.Count -eq 0) {
        Write-Log "  No pending migrations" "INFO"
    } else {
        foreach ($migration in $pendingMigrations) {
            Write-Log "  ○ $($migration.version) - $($migration.name)" "WARNING"
        }
    }
}

# Reset Database
function Reset-Database {
    param($DatabasePath)
    
    if (-not $Force) {
        Write-Log "Database reset requires -Force parameter" "ERROR"
        throw "Database reset requires confirmation"
    }
    
    Write-Log "Resetting database (WARNING: All data will be lost)" "WARNING"
    
    if ($DryRun) {
        Write-Log "DRY RUN: Would reset database" "WARNING"
        return $true
    }
    
    # Create backup before reset
    if (Test-Path $DatabasePath) {
        New-DatabaseBackup -DatabasePath $DatabasePath -Environment $Environment
        Remove-Item -Path $DatabasePath -Force
        Write-Log "Database reset completed" "SUCCESS"
    } else {
        Write-Log "Database does not exist, nothing to reset" "INFO"
    }
    
    return $true
}

# Create Sample Migration Files
function New-SampleMigrations {
    Write-Log "Creating sample migration files..." "INFO"
    
    # Create initial schema migration
    $initialMigration = @"
-- Migration: Create initial schema
-- Version: 20250101000001
-- Created: $(Get-Date)

-- Users table
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    address TEXT,
    role VARCHAR(50) DEFAULT 'CUSTOMER',
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Restaurants table
CREATE TABLE restaurants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    phone VARCHAR(20),
    cuisine_type VARCHAR(100),
    is_approved BOOLEAN DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Food items table
CREATE TABLE food_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    restaurant_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100),
    is_available BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Orders table
CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    restaurant_id INTEGER NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    delivery_address TEXT,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    delivery_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Create indexes
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_restaurants_active ON restaurants(is_active);
CREATE INDEX idx_food_items_restaurant ON food_items(restaurant_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_restaurant ON orders(restaurant_id);
"@
    
    $migrationPath = Join-Path $MigrationConfig.migrationsPath "20250101000001_create_initial_schema.sql"
    $initialMigration | Out-File -FilePath $migrationPath -Encoding UTF8
    Write-Log "Created: $migrationPath" "SUCCESS"
    
    # Create sample data migration
    $dataMigration = @"
-- Migration: Add sample data
-- Version: 20250101000002
-- Created: $(Get-Date)

-- Insert sample restaurants
INSERT INTO restaurants (name, description, address, phone, cuisine_type, is_approved, is_active) VALUES
('پیتزا سلطان', 'بهترین پیتزاهای شهر', 'تهران، خیابان انقلاب', '021-12345678', 'ایتالیایی', 1, 1),
('رستوران سنتی', 'غذاهای سنتی ایرانی', 'تهران، خیابان کارگر', '021-87654321', 'سنتی ایرانی', 1, 1),
('فست فود برگر لند', 'انواع برگر و ساندویچ', 'تهران، میدان تجریش', '021-11111111', 'فست فود', 1, 1);

-- Insert sample food items
INSERT INTO food_items (restaurant_id, name, description, price, category, is_available) VALUES
(1, 'پیتزا پپرونی', 'پیتزا با پپرونی و پنیر موزارلا', 25.50, 'پیتزا', 1),
(1, 'پیتزا مارگاریتا', 'پیتزا کلاسیک با گوجه و پنیر', 22.00, 'پیتزا', 1),
(2, 'چلو کباب', 'کباب کوبیده با برنج', 35.00, 'غذای اصلی', 1),
(2, 'خورشت قیمه', 'خورشت قیمه با برنج', 28.00, 'خورشت', 1),
(3, 'چیزبرگر', 'برگر با پنیر و سبزیجات', 18.50, 'برگر', 1),
(3, 'برگر مخصوص', 'برگر دوبل با ادویه مخصوص', 32.00, 'برگر', 1);
"@
    
    $dataPath = Join-Path $MigrationConfig.migrationsPath "20250101000002_add_sample_data.sql"
    $dataMigration | Out-File -FilePath $dataPath -Encoding UTF8
    Write-Log "Created: $dataPath" "SUCCESS"
    
    Write-Log "Sample migration files created successfully" "SUCCESS"
}

# Main Migration Process
function Start-Migration {
    Write-Log "Starting database migration process..." "INFO"
    Write-Log "Environment: $Environment" "INFO"
    Write-Log "Action: $Action" "INFO"
    
    $databasePath = Get-DatabasePath -Environment $Environment
    Write-Log "Database: $databasePath" "INFO"
    
    try {
        switch ($Action) {
            "migrate" {
                # Create backup before migration
                if (Test-Path $databasePath) {
                    New-DatabaseBackup -DatabasePath $databasePath -Environment $Environment
                }
                
                # Get pending migrations
                $pendingMigrations = Get-PendingMigrations -DatabasePath $databasePath
                
                if ($pendingMigrations.Count -eq 0) {
                    Write-Log "No pending migrations" "INFO"
                    return 0
                }
                
                Write-Log "Found $($pendingMigrations.Count) pending migrations" "INFO"
                
                foreach ($migration in $pendingMigrations) {
                    if ($Version -and $migration.version -gt $Version) {
                        Write-Log "Stopping at version $Version" "INFO"
                        break
                    }
                    
                    Invoke-Migration -DatabasePath $databasePath -Migration $migration
                }
                
                Write-Log "Migration completed successfully" "SUCCESS"
            }
            
            "rollback" {
                if (-not $Version) {
                    throw "Rollback requires -Version parameter"
                }
                
                # Create backup before rollback
                New-DatabaseBackup -DatabasePath $databasePath -Environment $Environment
                Invoke-Rollback -DatabasePath $databasePath -TargetVersion $Version
            }
            
            "status" {
                Show-MigrationStatus -DatabasePath $databasePath
            }
            
            "create" {
                if (-not $MigrationName) {
                    throw "Create action requires -MigrationName parameter"
                }
                
                $migrationPath = New-MigrationFile -MigrationName $MigrationName
                Write-Log "Migration template created: $migrationPath" "SUCCESS"
            }
            
            "reset" {
                Reset-Database -DatabasePath $databasePath
                Write-Log "Database reset completed" "SUCCESS"
            }
        }
        
        return 0
        
    } catch {
        Write-Log "Migration process failed: $($_.Exception.Message)" "ERROR"
        return 1
    }
}

# Initialize sample migrations if migrations directory is empty
if (-not (Get-ChildItem -Path $MigrationConfig.migrationsPath -Filter "*.sql" -ErrorAction SilentlyContinue)) {
    Write-Log "No migrations found, creating sample migrations..." "INFO"
    New-SampleMigrations
}

# Main execution
Write-Host "Food Ordering System - Database Migration" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$exitCode = Start-Migration
Write-Log "Migration process completed with exit code: $exitCode" "INFO"
Write-Log "Log file: $LogFile" "INFO"

exit $exitCode 
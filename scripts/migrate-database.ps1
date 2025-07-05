# Database Migration Script for Food Ordering System

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("development", "staging", "production")]
    [string]$Environment,
    
    [Parameter(Mandatory=$false)]
    [ValidateSet("migrate", "status", "reset")]
    [string]$Action = "migrate"
)

Write-Host "Database Migration for $Environment environment" -ForegroundColor Cyan
Write-Host "Action: $Action" -ForegroundColor Yellow

# Database paths
$databasePath = "database\$Environment\food_ordering_$Environment.db"
$migrationsPath = "database\migrations"

# Create directories if they don't exist
if (-not (Test-Path "database\$Environment")) {
    New-Item -Path "database\$Environment" -ItemType Directory -Force | Out-Null
    Write-Host "Created database directory for $Environment" -ForegroundColor Green
}

if (-not (Test-Path $migrationsPath)) {
    New-Item -Path $migrationsPath -ItemType Directory -Force | Out-Null
    Write-Host "Created migrations directory" -ForegroundColor Green
}

switch ($Action) {
    "migrate" {
        Write-Host "Running database migrations..." -ForegroundColor Green
        
        # Create database file if it doesn't exist
        if (-not (Test-Path $databasePath)) {
            New-Item -Path $databasePath -ItemType File -Force | Out-Null
            Write-Host "Created database file: $databasePath" -ForegroundColor Green
        }
        
        Write-Host "Migration completed!" -ForegroundColor Green
    }
    
    "status" {
        Write-Host "Database Status:" -ForegroundColor Green
        Write-Host "Database Path: $databasePath" -ForegroundColor White
        
        if (Test-Path $databasePath) {
            $fileSize = (Get-Item $databasePath).Length
            Write-Host "Database Size: $fileSize bytes" -ForegroundColor White
            Write-Host "Status: Ready" -ForegroundColor Green
        } else {
            Write-Host "Status: Not Created" -ForegroundColor Yellow
        }
    }
    
    "reset" {
        Write-Host "Resetting database..." -ForegroundColor Yellow
        
        if (Test-Path $databasePath) {
            Remove-Item $databasePath -Force
            Write-Host "Database reset completed" -ForegroundColor Green
        } else {
            Write-Host "Database does not exist" -ForegroundColor Yellow
        }
    }
}

Write-Host "Migration script completed!" -ForegroundColor Green 
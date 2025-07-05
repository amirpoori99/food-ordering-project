# Food Ordering System - Data Replication Script
# Phase 43: Disaster Recovery System

param(
    [string]$Action = "start",
    [string]$ReplicaType = "all",
    [switch]$TestMode = $false
)

# تنظیمات اصلی
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"

# توابع کمکی
function Write-Success { param([string]$Text) Write-Host "✅ $Text" -ForegroundColor Green }
function Write-Info { param([string]$Text) Write-Host "ℹ️ $Text" -ForegroundColor Cyan }
function Write-Warning { param([string]$Text) Write-Host "⚠️ $Text" -ForegroundColor Yellow }
function Write-Error { param([string]$Text) Write-Host "❌ $Text" -ForegroundColor Red }

# تابع ایجاد ساختار دایرکتوری رپلیکا
function Initialize-ReplicationDirectories {
    Write-Info "Initializing replication directories..."
    
    $directories = @(
        "$PROJECT_ROOT\replicas\database",
        "$PROJECT_ROOT\replicas\config",
        "$PROJECT_ROOT\replicas\logs",
        "$PROJECT_ROOT\work\replication"
    )
    
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Success "Created directory: $dir"
        }
    }
}

# تابع رپلیکیشن پایگاه داده
function Replicate-Database {
    Write-Info "Starting database replication..."
    
    $dbPath = "$PROJECT_ROOT\backend\food_ordering.db"
    $replica1Path = "$PROJECT_ROOT\replicas\database\replica_1.db"
    $replica2Path = "$PROJECT_ROOT\replicas\database\replica_2.db"
    
    if (-not (Test-Path $dbPath)) {
        Write-Warning "Master database not found: $dbPath"
        return $false
    }
    
    try {
        # کپی به رپلیکاهای محلی
        Copy-Item $dbPath $replica1Path -Force
        Copy-Item $dbPath $replica2Path -Force
        
        Write-Success "Database replicated successfully"
        return $true
    }
    catch {
        Write-Error "Failed to replicate database: $($_.Exception.Message)"
        return $false
    }
}

# تابع رپلیکیشن تنظیمات
function Replicate-Configuration {
    Write-Info "Starting configuration replication..."
    
    $configSource = "$PROJECT_ROOT\config"
    $configDest = "$PROJECT_ROOT\replicas\config"
    
    if (Test-Path $configSource) {
        try {
            Copy-Item $configSource $configDest -Recurse -Force
            Write-Success "Configuration replicated successfully"
            return $true
        }
        catch {
            Write-Error "Failed to replicate configuration: $($_.Exception.Message)"
            return $false
        }
    } else {
        Write-Warning "Configuration source not found"
        return $false
    }
}

# تابع اصلی رپلیکیشن
function Start-DataReplication {
    Write-Info "Starting data replication process..."
    
    Initialize-ReplicationDirectories
    
    $success = $true
    
    switch ($ReplicaType.ToLower()) {
        "database" {
            $success = Replicate-Database
        }
        "config" {
            $success = Replicate-Configuration
        }
        "all" {
            $success = $success -and (Replicate-Database)
            $success = $success -and (Replicate-Configuration)
        }
    }
    
    if ($success) {
        Write-Success "Data replication completed successfully"
        return $true
    } else {
        Write-Error "Data replication failed"
        return $false
    }
}

# اجرای اصلی
try {
    Write-Info "Food Ordering System - Data Replication"
    Write-Info "======================================="
    
    if ($TestMode) {
        Write-Info "Running in TEST MODE"
        Initialize-ReplicationDirectories
    } else {
        Start-DataReplication
    }
}
catch {
    Write-Error "Unexpected error: $($_.Exception.Message)"
}

Write-Info "Data replication script completed" 
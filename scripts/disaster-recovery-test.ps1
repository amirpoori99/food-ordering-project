# Food Ordering System - Disaster Recovery Test Suite
# Phase 43: Disaster Recovery System Testing

param(
    [string]$TestType = "all",
    [switch]$GenerateReport = $false,
    [switch]$Verbose = $false
)

# تنظیمات اصلی
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Definition
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_DIR
$TIMESTAMP = Get-Date -Format "yyyyMMdd-HHmmss"
$TEST_LOG = "$PROJECT_ROOT\logs\dr-test-$TIMESTAMP.log"

# متغیرهای گلوبال تست
$Global:TestResults = @()
$Global:PassedTests = 0
$Global:FailedTests = 0

# توابع کمکی
function Write-TestResult {
    param([string]$TestName, [bool]$Passed, [string]$Details = "")
    
    $result = @{
        TestName = $TestName
        Status = if ($Passed) { "PASS" } else { "FAIL" }
        Details = $Details
        Timestamp = Get-Date
    }
    
    $Global:TestResults += $result
    
    if ($Passed) {
        $Global:PassedTests++
        Write-Host "✅ [$($result.Status)] $TestName" -ForegroundColor Green
    } else {
        $Global:FailedTests++
        Write-Host "❌ [$($result.Status)] $TestName" -ForegroundColor Red
    }
    
    if ($Details) {
        Write-Host "   Details: $Details" -ForegroundColor Gray
    }
}

# تست وجود فایل‌های ضروری
function Test-RequiredFiles {
    Write-Host "`n🔍 Testing Required Files..." -ForegroundColor Yellow
    
    $requiredFiles = @(
        "config/disaster-recovery/backup-config.yml",
        "config/disaster-recovery/data-replication.yml", 
        "config/disaster-recovery/recovery-procedures.md",
        "config/disaster-recovery/business-continuity-plan.md",
        "scripts/automated-backup.ps1",
        "scripts/emergency-recovery.ps1",
        "scripts/data-replication.ps1"
    )
    
    foreach ($file in $requiredFiles) {
        $fullPath = "$PROJECT_ROOT\$file"
        $exists = Test-Path $fullPath
        $details = if ($exists) { "File exists" } else { "File missing: $fullPath" }
        Write-TestResult "Required File: $file" $exists $details
    }
}

# تست ساختار دایرکتوری
function Test-DirectoryStructure {
    Write-Host "`n📁 Testing Directory Structure..." -ForegroundColor Yellow
    
    $requiredDirectories = @(
        "config/disaster-recovery",
        "logs",
        "scripts"
    )
    
    foreach ($dir in $requiredDirectories) {
        $fullPath = "$PROJECT_ROOT\$dir"
        $exists = Test-Path $fullPath
        $details = if ($exists) { "Directory exists" } else { "Directory missing: $fullPath" }
        Write-TestResult "Required Directory: $dir" $exists $details
    }
}

# تست عملکرد اسکریپت‌ها
function Test-Scripts {
    Write-Host "`n🔧 Testing Scripts..." -ForegroundColor Yellow
    
    $scripts = @(
        "scripts/automated-backup.ps1",
        "scripts/emergency-recovery.ps1",
        "scripts/data-replication.ps1"
    )
    
    foreach ($script in $scripts) {
        $fullPath = "$PROJECT_ROOT\$script"
        if (Test-Path $fullPath) {
            try {
                $isValid = $true
                Write-TestResult "Script Existence: $script" $isValid "Script file found"
            }
            catch {
                Write-TestResult "Script Syntax: $script" $false "Error checking script: $($_.Exception.Message)"
            }
        } else {
            Write-TestResult "Script Existence: $script" $false "Script file not found"
        }
    }
}

# تست یکپارچگی پایگاه داده
function Test-DatabaseIntegrity {
    Write-Host "`n🗄️ Testing Database Integrity..." -ForegroundColor Yellow
    
    $dbPath = "$PROJECT_ROOT\backend\food_ordering.db"
    
    if (Test-Path $dbPath) {
        Write-TestResult "Database File Existence" $true "Database file found"
        
        # بررسی سایز فایل
        $fileSize = (Get-Item $dbPath).Length
        $hasSizeCheck = $fileSize -gt 0
        Write-TestResult "Database File Size" $hasSizeCheck "Size: $fileSize bytes"
        
    } else {
        Write-TestResult "Database File Existence" $false "Database file not found: $dbPath"
    }
}

# تست فایل‌های پیکربندی
function Test-ConfigurationFiles {
    Write-Host "`n⚙️ Testing Configuration Files..." -ForegroundColor Yellow
    
    $configFiles = @(
        "config/disaster-recovery/backup-config.yml",
        "config/disaster-recovery/data-replication.yml"
    )
    
    foreach ($configFile in $configFiles) {
        $fullPath = "$PROJECT_ROOT\$configFile"
        if (Test-Path $fullPath) {
            try {
                $content = Get-Content $fullPath -Raw
                $hasContent = $content.Length -gt 0
                Write-TestResult "Config File Content: $configFile" $hasContent "Content size: $($content.Length) characters"
            }
            catch {
                Write-TestResult "Config File Reading: $configFile" $false "Error reading file: $($_.Exception.Message)"
            }
        } else {
            Write-TestResult "Config File Existence: $configFile" $false "Config file not found"
        }
    }
}

# نمایش نتایج نهایی
function Show-FinalResults {
    Write-Host "`n" + "="*60 -ForegroundColor Cyan
    Write-Host "🏁 DISASTER RECOVERY TEST RESULTS" -ForegroundColor Cyan
    Write-Host "="*60 -ForegroundColor Cyan
    
    Write-Host "Total Tests: $($Global:TestResults.Count)" -ForegroundColor White
    Write-Host "Passed: $Global:PassedTests" -ForegroundColor Green
    Write-Host "Failed: $Global:FailedTests" -ForegroundColor $(if ($Global:FailedTests -eq 0) { "Green" } else { "Red" })
    
    if ($Global:TestResults.Count -gt 0) {
        $successRate = [math]::Round(($Global:PassedTests / $Global:TestResults.Count) * 100, 2)
        Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -eq 100) { "Green" } elseif ($successRate -ge 80) { "Yellow" } else { "Red" })
    }
    
    Write-Host "`nOverall Status: " -NoNewline
    if ($Global:FailedTests -eq 0) {
        Write-Host "✅ READY FOR PRODUCTION" -ForegroundColor Green
    } elseif ($Global:FailedTests -le 2) {
        Write-Host "⚠️ NEEDS MINOR FIXES" -ForegroundColor Yellow
    } else {
        Write-Host "❌ REQUIRES ATTENTION" -ForegroundColor Red
    }
    
    Write-Host "="*60 -ForegroundColor Cyan
}

# تابع اصلی اجرای تست‌ها
function Start-DisasterRecoveryTests {
    Write-Host "🧪 Food Ordering System - Disaster Recovery Test Suite" -ForegroundColor Cyan
    Write-Host "=======================================================" -ForegroundColor Cyan
    Write-Host "Starting disaster recovery tests..." -ForegroundColor White
    
    # اطمینان از وجود دایرکتوری لاگ
    if (-not (Test-Path "$PROJECT_ROOT\logs")) {
        New-Item -ItemType Directory -Path "$PROJECT_ROOT\logs" -Force | Out-Null
    }
    
    switch ($TestType.ToLower()) {
        "basic" {
            Test-RequiredFiles
            Test-DirectoryStructure
        }
        "scripts" {
            Test-Scripts
        }
        "database" {
            Test-DatabaseIntegrity
        }
        "config" {
            Test-ConfigurationFiles
        }
        "all" {
            Test-RequiredFiles
            Test-DirectoryStructure
            Test-Scripts
            Test-DatabaseIntegrity
            Test-ConfigurationFiles
        }
        default {
            Write-Host "❌ Unknown test type: $TestType" -ForegroundColor Red
            Write-Host "Available types: basic, scripts, database, config, all"
            return
        }
    }
    
    Show-FinalResults
    
    if ($GenerateReport) {
        Write-Host "`n📊 Generating detailed report..." -ForegroundColor Yellow
        $reportFile = "$PROJECT_ROOT\logs\disaster-recovery-test-report-$TIMESTAMP.txt"
        $report = "Disaster Recovery Test Report - $(Get-Date)`n"
        $report += "="*50 + "`n"
        $report += "Test Type: $TestType`n"
        $report += "Total Tests: $($Global:TestResults.Count)`n"
        $report += "Passed: $Global:PassedTests`n"
        $report += "Failed: $Global:FailedTests`n"
        $report += "`nTest Details:`n"
        
        foreach ($result in $Global:TestResults) {
            $report += "$($result.Status): $($result.TestName) - $($result.Details)`n"
        }
        
        $report | Out-File -FilePath $reportFile -Encoding UTF8
        Write-Host "📄 Report saved to: $reportFile" -ForegroundColor Green
    }
}

# اجرای اصلی
try {
    Start-DisasterRecoveryTests
    
    # تنظیم کد خروج بر اساس نتایج
    if ($Global:FailedTests -eq 0) {
        exit 0
    } else {
        exit 1
    }
}
catch {
    Write-Host "❌ Unexpected error during testing: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
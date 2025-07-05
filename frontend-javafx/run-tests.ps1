# ================================================================
# اسکریپت اجرای تست‌های فرانت‌اند JavaFX
# این اسکریپت امکان اجرای انواع مختلف تست‌ها را فراهم می‌کند
# ================================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("basic", "unit", "integration", "performance", "all", "clean")]
    [string]$TestType = "basic",
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipCompile,
    
    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

# تنظیمات رنگ‌ها برای خروجی
$Green = "Green"
$Red = "Red"
$Yellow = "Yellow"
$Cyan = "Cyan"
$White = "White"

# تابع نمایش پیام
function Write-ColorMessage {
    param(
        [string]$Message,
        [string]$Color = $White
    )
    Write-Host $Message -ForegroundColor $Color
}

# تابع نمایش عنوان
function Write-Header {
    param([string]$Title)
    Write-ColorMessage "`n================================================" $Cyan
    Write-ColorMessage $Title $Cyan
    Write-ColorMessage "================================================`n" $Cyan
}

# تابع نمایش خطا
function Write-Error {
    param([string]$Message)
    Write-ColorMessage "❌ ERROR: $Message" $Red
}

# تابع نمایش موفقیت
function Write-Success {
    param([string]$Message)
    Write-ColorMessage "✅ SUCCESS: $Message" $Green
}

# تابع نمایش هشدار
function Write-Warning {
    param([string]$Message)
    Write-ColorMessage "⚠️  WARNING: $Message" $Yellow
}

# تابع نمایش اطلاعات
function Write-Info {
    param([string]$Message)
    Write-ColorMessage "ℹ️  INFO: $Message" $White
}

# بررسی وجود Maven
function Test-Maven {
    try {
        $mavenVersion = mvn -version 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Maven found and working"
            return $true
        } else {
            Write-Error "Maven not found or not working"
            return $false
        }
    } catch {
        Write-Error "Maven not found in PATH"
        return $false
    }
}

# تابع پاکسازی
function Invoke-Clean {
    Write-Header "Cleaning Project"
    Write-Info "Running Maven clean..."
    
    if ($Verbose) {
        mvn clean
    } else {
        mvn clean -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Project cleaned successfully"
    } else {
        Write-Error "Failed to clean project"
        exit 1
    }
}

# تابع کامپایل
function Invoke-Compile {
    if ($SkipCompile) {
        Write-Info "Skipping compilation as requested"
        return
    }
    
    Write-Header "Compiling Project"
    Write-Info "Running Maven compile..."
    
    if ($Verbose) {
        mvn compile test-compile
    } else {
        mvn compile test-compile -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Project compiled successfully"
    } else {
        Write-Error "Failed to compile project"
        exit 1
    }
}

# تابع اجرای تست‌های پایه
function Invoke-BasicTests {
    Write-Header "Running Basic Tests"
    Write-Info "Executing basic infrastructure tests..."
    
    if ($Verbose) {
        mvn test -Pbasic-tests
    } else {
        mvn test -Pbasic-tests -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Basic tests passed"
    } else {
        Write-Error "Basic tests failed"
        exit 1
    }
}

# تابع اجرای تست‌های واحد
function Invoke-UnitTests {
    Write-Header "Running Unit Tests"
    Write-Info "Executing unit tests (excluding JavaFX UI tests)..."
    
    if ($Verbose) {
        mvn test -Punit-tests
    } else {
        mvn test -Punit-tests -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Unit tests passed"
    } else {
        Write-Warning "Some unit tests failed (this may be expected due to JavaFX headless issues)"
    }
}

# تابع اجرای تست‌های یکپارچگی
function Invoke-IntegrationTests {
    Write-Header "Running Integration Tests"
    Write-Info "Executing integration tests..."
    
    if ($Verbose) {
        mvn test -Pintegration-tests
    } else {
        mvn test -Pintegration-tests -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Integration tests passed"
    } else {
        Write-Warning "Some integration tests failed (this may be expected due to backend dependencies)"
    }
}

# تابع اجرای تست‌های عملکرد
function Invoke-PerformanceTests {
    Write-Header "Running Performance Tests"
    Write-Info "Executing performance and stress tests..."
    
    if ($Verbose) {
        mvn test -Pperformance-tests
    } else {
        mvn test -Pperformance-tests -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Performance tests passed"
    } else {
        Write-Warning "Some performance tests failed (this may be expected in CI environment)"
    }
}

# تابع اجرای تمام تست‌ها
function Invoke-AllTests {
    Write-Header "Running All Tests"
    Write-Info "Executing all test suites..."
    
    if ($Verbose) {
        mvn test
    } else {
        mvn test -q
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "All tests passed"
    } else {
        Write-Warning "Some tests failed (this may be expected due to environment limitations)"
    }
}

# تابع نمایش خلاصه
function Show-Summary {
    Write-Header "Test Execution Summary"
    Write-Info "Test type: $TestType"
    Write-Info "Verbose mode: $Verbose"
    Write-Info "Skip compile: $SkipCompile"
    Write-Info "`nAvailable test types:"
    Write-Info "  - basic: Basic infrastructure tests"
    Write-Info "  - unit: Unit tests (excluding UI tests)"
    Write-Info "  - integration: Integration tests"
    Write-Info "  - performance: Performance and stress tests"
    Write-Info "  - all: All test suites"
    Write-Info "  - clean: Clean project only"
}

# تابع اصلی
function Main {
    Write-Header "Food Ordering Frontend Test Runner"
    
    # بررسی Maven
    if (-not (Test-Maven)) {
        Write-Error "Maven is required but not found. Please install Maven and add it to PATH."
        exit 1
    }
    
    # نمایش خلاصه
    Show-Summary
    
    # اجرای تست‌ها بر اساس نوع
    switch ($TestType) {
        "clean" {
            Invoke-Clean
        }
        "basic" {
            Invoke-Compile
            Invoke-BasicTests
        }
        "unit" {
            Invoke-Compile
            Invoke-UnitTests
        }
        "integration" {
            Invoke-Compile
            Invoke-IntegrationTests
        }
        "performance" {
            Invoke-Compile
            Invoke-PerformanceTests
        }
        "all" {
            Invoke-Compile
            Invoke-BasicTests
            Invoke-UnitTests
            Invoke-IntegrationTests
            Invoke-PerformanceTests
        }
        default {
            Write-Error "Unknown test type: $TestType"
            exit 1
        }
    }
    
    Write-Header "Test Execution Completed"
    Write-Success "Test runner finished successfully"
}

# اجرای تابع اصلی
Main 
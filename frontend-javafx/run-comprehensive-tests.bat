@echo off
echo ================================================================================
echo 🎯 FOOD ORDERING SYSTEM - COMPREHENSIVE TEST EXECUTION
echo 📅 Date: %date% %time%
echo ================================================================================

echo.
echo 🔍 Checking Prerequisites...

:: Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven not found! Please install Maven first.
    echo 💡 Download from: https://maven.apache.org/download.cgi
    goto :error
)
echo ✅ Maven found

:: Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java not found! Please install Java 11+ first.
    goto :error
)
echo ✅ Java found

:: Check if backend project exists
if not exist "..\backend\pom.xml" (
    echo ❌ Backend project not found!
    echo 💡 Make sure backend folder exists in parent directory
    goto :error
)
echo ✅ Backend project found

echo.
echo 🚀 Starting Backend Server...
start "Backend Server" cmd /c "cd ..\backend && mvn spring-boot:run"
echo ⏳ Waiting for backend to start (30 seconds)...
timeout /t 30 /nobreak

echo.
echo 🧪 Running Comprehensive Test Suite...
echo.

:: Run all test categories with detailed reporting
echo 📊 Executing: Unit Tests
mvn test -Dtest="**/*SimpleTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Integration Tests  
mvn test -Dtest="**/*IntegrationTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Performance Tests
mvn test -Dtest="**/*PerformanceTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Security Tests
mvn test -Dtest="**/*SecurityTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Edge Case Tests
mvn test -Dtest="**/*EdgeCaseTest" -Dmaven.test.failure.ignore=true

echo.
echo 📊 Executing: Comprehensive Test Suite
mvn test -Dtest="ComprehensiveTestSuite" -Dmaven.test.failure.ignore=true

echo.
echo 📈 Generating Test Reports...
mvn surefire-report:report-only
mvn surefire-report:failsafe-report-only

echo.
echo 📋 Opening Test Results...
if exist "target\surefire-reports\index.html" (
    start "Test Results" "target\surefire-reports\index.html"
) else (
    echo ⚠️ Test report not found at expected location
)

echo.
echo ================================================================================
echo ✅ COMPREHENSIVE TEST EXECUTION COMPLETED
echo 📊 Check the opened browser window for detailed results
echo 📁 Test reports location: target\surefire-reports\
echo ================================================================================

echo.
echo 🛑 Stopping Backend Server...
taskkill /f /im java.exe /fi "WindowTitle eq Backend Server*" >nul 2>&1

goto :end

:error
echo.
echo ❌ Test execution failed due to missing prerequisites
echo 💡 Please resolve the issues above and try again
exit /b 1

:end
pause 
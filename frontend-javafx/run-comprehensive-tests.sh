#!/bin/bash

echo "================================================================================"
echo "🎯 FOOD ORDERING SYSTEM - COMPREHENSIVE TEST EXECUTION"
echo "📅 Date: $(date)"
echo "================================================================================"

echo ""
echo "🔍 Checking Prerequisites..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found! Please install Maven first."
    echo "💡 Install with: sudo apt-get install maven (Ubuntu/Debian)"
    echo "💡 Or download from: https://maven.apache.org/download.cgi"
    exit 1
fi
echo "✅ Maven found"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java not found! Please install Java 11+ first."
    echo "💡 Install with: sudo apt-get install openjdk-17-jdk"
    exit 1
fi
echo "✅ Java found"

# Check if backend project exists
if [ ! -f "../backend/pom.xml" ]; then
    echo "❌ Backend project not found!"
    echo "💡 Make sure backend folder exists in parent directory"
    exit 1
fi
echo "✅ Backend project found"

echo ""
echo "🚀 Starting Backend Server..."
cd ../backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ../frontend-javafx

echo "⏳ Waiting for backend to start (30 seconds)..."
sleep 30

# Check if backend is actually running
if kill -0 $BACKEND_PID 2>/dev/null; then
    echo "✅ Backend server started successfully"
else
    echo "⚠️ Backend server may not have started properly"
fi

echo ""
echo "🧪 Running Comprehensive Test Suite..."
echo ""

# Function to run tests with error handling
run_test_category() {
    local test_pattern=$1
    local category_name=$2
    
    echo "📊 Executing: $category_name"
    
    if mvn test -Dtest="$test_pattern" -Dmaven.test.failure.ignore=true; then
        echo "✅ $category_name completed"
    else
        echo "⚠️ $category_name had some failures (check reports)"
    fi
    echo ""
}

# Run all test categories
run_test_category "**/*SimpleTest" "Unit Tests"
run_test_category "**/*IntegrationTest" "Integration Tests"
run_test_category "**/*PerformanceTest" "Performance Tests"
run_test_category "**/*SecurityTest" "Security Tests"
run_test_category "**/*EdgeCaseTest" "Edge Case Tests"
run_test_category "ComprehensiveTestSuite" "Comprehensive Test Suite"

echo "📈 Generating Test Reports..."
mvn surefire-report:report-only
mvn surefire-report:failsafe-report-only

echo ""
echo "📋 Test Results Location:"
if [ -f "target/surefire-reports/index.html" ]; then
    echo "✅ HTML Report: $(pwd)/target/surefire-reports/index.html"
    echo "💡 Open in browser: file://$(pwd)/target/surefire-reports/index.html"
else
    echo "⚠️ Test report not found at expected location"
fi

if [ -f "target/site/jacoco/index.html" ]; then
    echo "✅ Coverage Report: $(pwd)/target/site/jacoco/index.html"
    echo "💡 Open in browser: file://$(pwd)/target/site/jacoco/index.html"
fi

echo ""
echo "================================================================================"
echo "✅ COMPREHENSIVE TEST EXECUTION COMPLETED"
echo "📊 Check the file paths above for detailed results"
echo "📁 Test reports location: target/surefire-reports/"
echo "📊 Coverage reports location: target/site/jacoco/"
echo "================================================================================"

echo ""
echo "🛑 Stopping Backend Server..."
if kill -0 $BACKEND_PID 2>/dev/null; then
    kill $BACKEND_PID
    echo "✅ Backend server stopped"
else
    echo "ℹ️ Backend server was not running"
fi

echo ""
echo "🎉 All done! Press Enter to exit..."
read -r 
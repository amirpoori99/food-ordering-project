@echo off
echo Starting Database Populator...
echo.

REM Compile the project first
echo Compiling project...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Copy dependencies
echo Copying dependencies...
call mvn dependency:copy-dependencies -DoutputDirectory=target/lib -q
if %errorlevel% neq 0 (
    echo Dependency copy failed!
    pause
    exit /b 1
)

REM Run DatabasePopulator
echo Running DatabasePopulator...
java -cp "target/classes;target/lib/*" com.myapp.DatabasePopulator

if %errorlevel% equ 0 (
    echo.
    echo Database population completed successfully!
    echo.
    echo Checking database contents...
    java -cp "target/classes;target/lib/*" com.myapp.DatabaseChecker
) else (
    echo.
    echo Database population failed!
)

echo.
pause 
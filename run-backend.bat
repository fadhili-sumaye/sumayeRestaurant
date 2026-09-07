@echo off
setlocal

REM Set JAVA_HOME if not already set (using Android Studio's JBR if present)
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Android\Android Studio\jbr" (
        set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
    )
)

REM Locate Maven executable
set "MVN_CMD=mvn"
if exist "C:\Tools\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=C:\Tools\apache-maven-3.9.9\bin\mvn.cmd"
)

echo ====================================================
echo Starting Sumaye Restaurant Spring Boot Backend...
echo JAVA_HOME: %JAVA_HOME%
echo Maven:     %MVN_CMD%
echo ====================================================

REM Keep Maven's dependency cache inside the project. This avoids broken or
REM inaccessible global Maven settings (for example, C:\.m2\repository).
set "MAVEN_REPO_LOCAL=%~dp0restaurant-management-api\.m2-cache"
if not exist "%MAVEN_REPO_LOCAL%" mkdir "%MAVEN_REPO_LOCAL%"

REM Check if port 8080 is already in use
set "EXISTING_PID="
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "EXISTING_PID=%%a"
)

if not defined EXISTING_PID goto :start_server

echo [WARNING] Port 8080 is already in use by process PID: %EXISTING_PID%!
echo The backend server appears to already be running.
echo.
set /p RESTART="Do you want to stop the existing process and restart? (Y/N, default N): "
if /i "%RESTART%"=="Y" (
    echo Stopping PID %EXISTING_PID%...
    taskkill /F /PID %EXISTING_PID%
    timeout /t 2 /nobreak >nul
    goto :start_server
)

echo Exiting without starting duplicate instance.
pause
exit /b 0

:start_server
cd /d "%~dp0restaurant-management-api"
"%MVN_CMD%" "-Dmaven.repo.local=%MAVEN_REPO_LOCAL%" spring-boot:run
pause

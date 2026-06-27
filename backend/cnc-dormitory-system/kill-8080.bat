@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   8080 Port Killer
echo ========================================
echo.

set "FOUND="
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr /i "LISTENING"') do (
    set "FOUND=1"
    echo [INFO] Stopping PID %%a ...
    taskkill /PID %%a /F >nul 2>&1
    if not errorlevel 1 echo [ OK ] PID %%a stopped
    if errorlevel 1 echo [FAIL] Failed to stop PID %%a (maybe insufficient privileges)
)

if not defined FOUND (k
    echo [INFO] No process listening on port 8080 was found
)

echo.
pause

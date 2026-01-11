@echo off
echo.
echo ========================================
echo   Enterprise AI Chatbot Platform
echo   Starting Application...
echo ========================================
echo.

REM Check if MySQL is running
echo [1/4] Checking MySQL...
mysql --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ MySQL not found! Please install MySQL 8.0
    pause
    exit /b 1
)
echo ✅ MySQL is available

REM Create database if it doesn't exist
echo.
echo [2/4] Setting up database...
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS chatbot_platform;" 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ Database ready
) else (
    echo ⚠️  Could not create database. It may already exist or credentials are different.
    echo    Please create manually: CREATE DATABASE chatbot_platform;
)

REM Start backend in new window
echo.
echo [3/4] Starting backend...
start "Enterprise Chatbot - Backend" cmd /k "cd backend && mvn spring-boot:run"
echo ✅ Backend starting in new window...
echo    Wait for: "Started ChatbotPlatformApplication"

REM Wait a bit for backend to start
timeout /t 5 /nobreak >nul

REM Start frontend in new window
echo.
echo [4/4] Starting frontend...
start "Enterprise Chatbot - Frontend" cmd /k "cd frontend && npm run dev"
echo ✅ Frontend starting in new window...

echo.
echo ========================================
echo   🎉 Application Starting!
echo ========================================
echo.
echo Backend:  http://localhost:8080/api
echo Frontend: http://localhost:5173
echo.
echo Wait for both servers to start, then open:
echo 👉 http://localhost:5173
echo.
echo Press any key to open browser...
pause >nul

start http://localhost:5173

echo.
echo ✅ Browser opened!
echo.
echo To stop the servers, close the terminal windows.
echo.
pause

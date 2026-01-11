@echo off
echo 🚀 Enterprise AI Chatbot Platform - Quick Setup
echo ================================================
echo.

echo Checking prerequisites...

REM Check Java
where java >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ Java found
) else (
    echo ❌ Java not found. Please install Java 17 or higher.
    exit /b 1
)

REM Check Node
where node >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ Node.js found
) else (
    echo ❌ Node.js not found. Please install Node.js 18 or higher.
    exit /b 1
)

REM Check MySQL
where mysql >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ MySQL found
) else (
    echo ⚠️  MySQL not found. Please ensure MySQL 8.0 is installed and running.
)

echo.
echo 📦 Setting up project...
echo.

REM Backend setup
echo 1️⃣ Setting up backend...
cd backend

if not exist .env (
    echo Creating .env file from template...
    copy .env.example .env
    echo ⚠️  Please edit backend\.env and add your GEMINI_API_KEY
)

echo Installing backend dependencies...
call mvn clean install -DskipTests

echo.
echo 2️⃣ Setting up frontend...
cd ..\frontend

echo Installing frontend dependencies...
call npm install

echo.
echo ✅ Setup complete!
echo.
echo 📝 Next steps:
echo 1. Get your Gemini API key from: https://makersuite.google.com/app/apikey
echo 2. Add it to backend\.env file
echo 3. Create MySQL database: CREATE DATABASE chatbot_platform;
echo 4. Start backend: cd backend ^&^& mvn spring-boot:run
echo 5. Start frontend: cd frontend ^&^& npm run dev
echo.
echo 🌐 Access the app at: http://localhost:5173
echo.

pause

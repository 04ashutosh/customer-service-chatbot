#!/bin/bash

echo "🚀 Enterprise AI Chatbot Platform - Quick Setup"
echo "================================================"
echo ""

# Check prerequisites
echo "Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "✅ Java found: $JAVA_VERSION"
else
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Node
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo "✅ Node.js found: $NODE_VERSION"
else
    echo "❌ Node.js not found. Please install Node.js 18 or higher."
    exit 1
fi

# Check MySQL
if command -v mysql &> /dev/null; then
    echo "✅ MySQL found"
else
    echo "⚠️  MySQL not found. Please ensure MySQL 8.0 is installed and running."
fi

echo ""
echo "📦 Setting up project..."
echo ""

# Backend setup
echo "1️⃣ Setting up backend..."
cd backend

if [ ! -f .env ]; then
    echo "Creating .env file from template..."
    cp .env.example .env
    echo "⚠️  Please edit backend/.env and add your GEMINI_API_KEY"
fi

echo "Installing backend dependencies..."
mvn clean install -DskipTests

echo ""
echo "2️⃣ Setting up frontend..."
cd ../frontend

echo "Installing frontend dependencies..."
npm install

echo ""
echo "✅ Setup complete!"
echo ""
echo "📝 Next steps:"
echo "1. Get your Gemini API key from: https://makersuite.google.com/app/apikey"
echo "2. Add it to backend/.env file"
echo "3. Create MySQL database: CREATE DATABASE chatbot_platform;"
echo "4. Start backend: cd backend && mvn spring-boot:run"
echo "5. Start frontend: cd frontend && npm run dev"
echo ""
echo "🌐 Access the app at: http://localhost:5173"
echo ""

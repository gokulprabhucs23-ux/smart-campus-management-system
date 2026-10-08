#!/usr/bin/env bash

# Smart Campus Management System Launch Script
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
export PATH="/opt/homebrew/opt/openjdk/bin:/opt/homebrew/bin:$PATH"

echo "==================================================="
echo "  AI-Enabled Smart Campus Management System"
echo "==================================================="

# 1. Check Java
if ! command -v java &>/dev/null; then
  echo "❌ Java not found. Please ensure OpenJDK is installed."
  exit 1
fi
echo "✔ Java: $(java -version 2>&1 | head -n 1)"

# 2. Check Maven
if ! command -v mvn &>/dev/null; then
  echo "❌ Maven not found. Please ensure Apache Maven is installed."
  exit 1
fi
echo "✔ Maven: $(mvn -version 2>&1 | head -n 1)"

# 3. Check MongoDB
echo "✔ Checking MongoDB..."
if ! nc -z localhost 27017 2>/dev/null; then
  echo "⚠ MongoDB not responding on 27017. Attempting to start service..."
  brew services start mongodb-community || true
  sleep 2
fi

# 4. Check & Build Backend if needed
JAR_FILE="$DIR/backend/target/smart-campus-backend-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
  echo "🔨 Compiling backend..."
  (cd "$DIR/backend" && mvn clean package -DskipTests)
fi

echo "🚀 Starting Spring Boot Backend (port 8080)..."
(cd "$DIR/backend" && java -jar "$JAR_FILE") &
BACKEND_PID=$!

echo "🚀 Starting React Frontend (port 5173)..."
(cd "$DIR/frontend" && npm run dev) &
FRONTEND_PID=$!

cleanup() {
  echo ""
  echo "Stopping services..."
  kill $BACKEND_PID 2>/dev/null || true
  kill $FRONTEND_PID 2>/dev/null || true
  exit 0
}

trap cleanup SIGINT SIGTERM

echo "==================================================="
echo "  Backend running on:  http://localhost:8080"
echo "  Frontend running on: http://localhost:5173"
echo "  Press Ctrl+C to stop all services."
echo "==================================================="

wait

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
else
  echo "✔ MongoDB is active on port 27017"
fi

# 4. Clean up any existing processes on port 8080 or 5173
OLD_BACKEND_PID=$(lsof -ti :8080 2>/dev/null || true)
if [ -n "$OLD_BACKEND_PID" ]; then
  echo "⚠ Freeing port 8080 (killing existing process $OLD_BACKEND_PID)..."
  kill -9 $OLD_BACKEND_PID 2>/dev/null || true
  sleep 1
fi

OLD_FRONTEND_PID=$(lsof -ti :5173 2>/dev/null || true)
if [ -n "$OLD_FRONTEND_PID" ]; then
  echo "⚠ Freeing port 5173 (killing existing process $OLD_FRONTEND_PID)..."
  kill -9 $OLD_FRONTEND_PID 2>/dev/null || true
  sleep 1
fi

# 5. Check & Build Backend if needed
JAR_FILE="$DIR/backend/target/smart-campus-backend-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
  echo "🔨 Compiling backend JAR..."
  (cd "$DIR/backend" && mvn clean package -DskipTests)
fi

echo "🚀 Starting Spring Boot Backend (port 8080)..."
(cd "$DIR/backend" && java -jar "$JAR_FILE") &
BACKEND_PID=$!

# Wait for backend to be ready
echo "⏳ Waiting for backend to initialize..."
for i in {1..30}; do
  if curl -s http://localhost:8080/api/auth/me >/dev/null 2>&1; then
    echo "✔ Backend initialized successfully!"
    break
  fi
  sleep 1
done

echo "🚀 Starting React Frontend (port 5173)..."
(cd "$DIR/frontend" && npm run dev) &
FRONTEND_PID=$!

cleanup() {
  echo ""
  echo "🛑 Stopping Smart Campus services..."
  kill $BACKEND_PID 2>/dev/null || true
  kill $FRONTEND_PID 2>/dev/null || true
  exit 0
}

trap cleanup SIGINT SIGTERM

echo "==================================================="
echo "  ✨ Smart Campus System is ONLINE!"
echo "  Frontend Application: http://localhost:5173"
echo "  Backend API Services: http://localhost:8080"
echo "  Press Ctrl+C to stop all services."
echo "==================================================="

wait

#!/bin/bash
export LANG=en_US.UTF-8

echo "✈️ : Moving to FastAPI directory..."
cd /home/t25101/v0.5/ai/BookCalendar-AI || {
  echo "❌ Failed to navigate to FastAPI directory"
  exit 1
}

# FastAPI 프로세스가 존재한다면 종료 (기존 uvicorn 프로세스를 종료)
echo "🧹 Step 1: Killing existing FastAPI process if running..."
PID=$(pgrep -f "uvicorn main:app")
if [ -n "$PID" ]; then
  kill -9 "$PID"
  echo "✅ Killed existing FastAPI process (PID: $PID)"
else
  echo "ℹ️ No running FastAPI process found."
fi

# FastAPI 서버 재시작
echo "🚀 Step 2: Restarting FastAPI server..."
nohup uvicorn main:app --host 0.0.0.0 --port 3004 --reload > fastapi.log 2>&1 &

if [ $? -eq 0 ]; then
  echo "✅ FastAPI successfully restarted."
else
  echo "❌ Failed to start FastAPI."
  exit 1
fi

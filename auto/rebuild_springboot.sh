#!/bin/bash
export LANG=en_US.UTF-8

# 📌 스크립트의 절대 경로 기준 루트로 이동
echo "✈️ : move to root dir "
cd "$(dirname "$0")/.."

# 자동화 스크립트에 down 넣기
echo "🧹 Step 0: Stopping existing containers..."
podman-compose -f podman-compose.yaml down || echo "⚠️ Containers may not have stopped cleanly."

# 📌 Step 1: Spring Boot JAR 파일 빌드
echo "🔨 Step 1: Moving to springboot directory and building jar..."
cd ./springboot || { echo "❌ Failed to enter springboot directory"; exit 1; }

# Gradle을 이용해 Spring Boot 애플리케이션을 JAR 파일로 빌드
./gradlew bootJar || { echo "❌ Gradle build failed"; exit 1; }

# 다시 루트 디렉토리(BookCalendar/)로 이동
cd .. || { echo "❌ Failed to return to root directory"; exit 1; }

# 📌 Step 2: Docker 이미지 재빌드
echo "🐳 Step 2: Rebuilding Docker image for springboot..."

# podman-compose.yaml 파일을 사용하여 'springboot' 서비스의 이미지 재빌드
podman-compose -f podman-compose.yaml build springboot || { echo "❌ Podman build failed"; exit 1; }
# docker일 경우
# docker compose -f podman-compose.yaml build springboot || { echo "❌ Docker build failed"; exit 1; }

# 📌 Step 3: 모든 컨테이너 다시 시작
echo "🚀 Step 3: Restarting all containers..."

# 모든 컨테이너를 다시 시작 (Spring Boot 포함)
podman-compose -f podman-compose.yaml up -d || { echo "❌ Podman up failed"; exit 1; }

# ✅ 완료 메시지 출력
echo "✅ Done! Spring Boot app has been rebuilt and redeployed."


#!/bin/bash
export LANG=ko_KR.UTF-8

echo "📦 1단계: 실행 중인 컨테이너 목록 확인 중..."
podman ps

sleep 3

echo "📜 2단계: redis-cache 로그 출력 중..."
podman logs redis-cache

sleep 3  

echo "📜 3단계: redis-exporter-cache 로그 출력 중..."
podman logs redis-exporter-cache

sleep 3  

echo "📜 4단계: redis-session 로그 출력 중..."
podman logs redis-session

sleep 3  

echo "📜 5단계: redis-exporter-session 로그 출력 중..."
podman logs redis-exporter-session

sleep 3  

echo "🧩 6단계: Spring Boot 로그 실시간 출력 중 (종료하려면 Ctrl+C)..."
podman logs -f springboot

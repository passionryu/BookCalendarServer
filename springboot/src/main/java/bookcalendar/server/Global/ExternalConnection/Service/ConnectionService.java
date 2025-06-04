package bookcalendar.server.global.ExternalConnection.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionService {

    @Qualifier("sessionRedisTemplate")
    private final RedisTemplate<String, String> sessionRedisTemplate;

    // Redis 키 상수
    private static final String REDISKEY = "FastAPI-Error";

    private static final String SCRIPT_PATH = "/home/t25101/v0.5/"; // 경로는 실제 경로로 바꿔주세요

    /**
     * Fast-API 커넥션 오류 발생 시 Redis에 오류 업로드 메서드
     *
     */
    public void uploadFastAPIConnectionErrorToSession(){

        // 에러 발생 시간을 에러키의 Value로 대입
        LocalDateTime errorTime = LocalDateTime.now();
        String errorTimeStr = errorTime.toString();
        // Redis에 FastAPI 오류 보고 (TTL: 30분)
        sessionRedisTemplate.opsForValue().set(REDISKEY, errorTimeStr, Duration.ofMinutes(30));
    }

    /**
     * Fast-API 커넥션 오류 최초 발생 시 Fast-API 재가동 스크립트 실행 메서드
     *
     */
    public void reRunFastAPI(){

        // fast-api 재시작 명령어 가동
        // 이 명령을 bash script로 만들어 놓고, Java에서 sh restart_fastapi.sh 실행하도록 해도 관리가 더 편리할 수 있을 것 같음.
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "/usr/bin/bash", "-c", "uvicorn main:app --host 0.0.0.0 --port 3004 --reload"
            );
            builder.directory(new File("/home/t25101/v0.5/ai/BookCalendar-AI")); // FastAPI가 위치한 디렉토리
            builder.start();
            log.info("FastAPI 재시작 명령 실행 완료");
        } catch (IOException e2) {
            log.info("FastAPI 재시작 실패", e2);
        }

    }

    /**
     * Fast-API 커넥션 오류 최초 발생 시 Fast-API 재가동 스크립트 실행 메서드
     *
     */
    public void rerunFastApiScript() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", SCRIPT_PATH);
            processBuilder.redirectErrorStream(true); // 에러도 출력에 함께 포함

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[rerun_fastapi.sh] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("✅ FastAPI 재시작 스크립트 성공");
            } else {
                log.error("❌ FastAPI 재시작 스크립트 실패. 종료 코드: {}", exitCode);
            }

        } catch (Exception e) {
            log.error("❌ FastAPI 재시작 중 오류 발생", e);
        }
    }
}

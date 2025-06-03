package bookcalendar.server.global.ExternalConnection.Client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class IntentClient {

    private final WebClient webClient;

    public IntentClient(@Qualifier("fastApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> predict(String text) {
        return webClient.post()
                .uri("/intent/predict_intent")
                .bodyValue(Map.of("text", text))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("응답 바디 없음")
                                .flatMap(errorBody -> {
                                    log.error("FastAPI 오류 응답: HTTP {} - {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("FastAPI 오류 발생: " + errorBody));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .map(response -> response.get("message")) // 여기서 "message" 값만 추출
                .doOnNext(intent -> log.info("의도 분석 결과: {}", intent))
                .doOnError(error -> log.error("의도 분석 요청 실패: {}", error.getMessage()));
    }
}

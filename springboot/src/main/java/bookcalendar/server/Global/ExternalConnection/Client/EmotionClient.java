package bookcalendar.server.global.ExternalConnection.Client;

import bookcalendar.server.global.ExternalConnection.DTO.TextInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class EmotionClient {

    private final WebClient webClient;

    public EmotionClient(@Qualifier("fastApiWebClient") WebClient webClient) {

        this.webClient = webClient;
    }

    public Mono<String> predict(String text) {

        return webClient.post()
                .uri("/emotion/predict_emotion")
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
                .map(response -> response.get("emotion"))
                .doOnNext(emotion -> log.info("감정 분석 결과: {}", emotion))
                .doOnError(error -> log.error("감정 분석 요청 실패: {}", error.getMessage()));
    }
}
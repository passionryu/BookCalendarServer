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
        log.info("요청 보낼 텍스트 - 위치(EmotionClient.class) : {}", text);

        /* 입력 검증 */
        if (text == null || text.trim().isEmpty()) {
            log.error("입력 텍스트가 null 또는 빈 문자열입니다.");
            return Mono.error(new IllegalArgumentException("텍스트가 필요합니다."));
        }

        TextInput input = new TextInput(text);
        try {
            String jsonBody = new ObjectMapper().writeValueAsString(input);
            log.info("직렬화된 JSON 본문 - {} ", jsonBody);
        } catch (Exception e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage());
        }

        return webClient.post()
                .uri("/emotion/predict_emotion")
                // .bodyValue(new TextInput(text))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.contentType(MediaType.APPLICATION_JSON)
                //.bodyValue(Map.of("text", text))
                .bodyValue(new TextInput(text))
                //.bodyValue(jsonBody);

                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class)
                                .map(errorBody -> {
                                    log.error("FastAPI 오류 응답  ", errorBody);
                                    return new RuntimeException("422 Error: " + errorBody);
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .map(response -> response.get("emotion"))
                .doOnNext(emotion -> log.info("감정 분석 결과: {}", emotion))
                .doOnError(error -> log.error("감정 분석 요청 실패: {}", error.getMessage()));
    }
}
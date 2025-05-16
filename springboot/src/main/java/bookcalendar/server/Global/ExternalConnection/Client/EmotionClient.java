package bookcalendar.server.global.ExternalConnection.Client;


import bookcalendar.server.global.ExternalConnection.DTO.TextInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
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

        log.info("요청 보낼 텍스트 - 위치(EmotionClient.class) :{}", text);
        Map<String, String> body = Map.of("요청 을 보내는 데이터 -> text", text);

        return webClient.post()
                .uri("/emotion/predict_emotion")  // ✅ 경로 정확히
                //.bodyValue(new TextInput(text))   // ✅ JSON 변환 확인
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .map(response -> response.get("emotion"))
                .doOnNext(emotion -> log.info("감정 분석 결과: {}", emotion))
                .doOnError(error -> log.error("감정 분석 요청 실패: {}", error.getMessage()));
    }


//        return webClient.post()
//                .uri("/emotion/predict_emotion")
//                .header("Content-Type", "application/json")
//                .bodyValue(Map.of("text", text))
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
//                .map(response -> response.get("emotion"))
//                .doOnNext(emotion -> log.info("감정 분석 결과: {}", emotion))
//                .doOnError(error -> log.error("감정 분석 요청 실패: {}", error.getMessage()));

}
package bookcalendar.server.global.ExternalConnection.Client;

import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberTwoThreeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class QuestionClient {

    private final WebClient webClient;

    public QuestionClient(@Qualifier("fastApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<QuestionNumberTwoThreeResponse> predict(String text) {
        return webClient.post()
                .uri("/predict_question")
                .bodyValue(Map.of("paragraph", text))  // 🚨 key 이름 'text' → 'paragraph' 수정 필요!
                .retrieve()
                .bodyToMono(QuestionNumberTwoThreeResponse.class)
                .doOnNext(response -> log.info("질문 생성 결과: {}", response))
                .doOnError(error -> log.error("질문 생성 실패: {}", error.getMessage()));
    }
}
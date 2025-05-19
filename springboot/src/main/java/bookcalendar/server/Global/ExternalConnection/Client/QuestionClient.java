package bookcalendar.server.global.ExternalConnection.Client;

import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberTwoThreeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
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

        /* 입력값 로그 */
        log.info("try input(입력값 로그) : " + text);
        /* 요청 보내기 직전의 로그 */
        Map<String, String> requestBody = Map.of("paragraph", text);
        log.info("Request Body(요청 보내기 직전의 로그): {}", requestBody);
        return webClient.post()
                .uri("/question/predict_question")
                .header("Content-Type", "application/json")  // 명시적 추가
                .bodyValue(BodyInserters.fromValue(requestBody))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("응답 바디 없음")
                                .flatMap(errorBody -> {
                                    log.error("FastAPI 오류 응답: HTTP {} - {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("FastAPI 서버 오류 발생: " + errorBody));
                                })
                )
                .bodyToMono(QuestionNumberTwoThreeResponse.class)
                .filter(response -> response != null)
                .switchIfEmpty(Mono.error(new IllegalStateException("FastAPI 응답이 null입니다.")))
                .doOnNext(response -> log.info("질문 생성 결과: {}", response))
                .doOnError(error -> log.error("질문 생성 실패: {}", error.getMessage()));
    }

}
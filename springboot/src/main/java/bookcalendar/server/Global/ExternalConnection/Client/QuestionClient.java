package bookcalendar.server.global.ExternalConnection.Client;

import bookcalendar.server.Domain.Review.DTO.Response.QuestionNumberTwoThreeResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class QuestionClient {

    private final WebClient webClient;

    public QuestionClient(@Qualifier("fastApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<QuestionNumberTwoThreeResponse> predict(String text) {
        return webClient.post()
                .uri("/predict_question")
                .bodyValue(Map.of("text", text))
                .retrieve()
                .bodyToMono(QuestionNumberTwoThreeResponse.class);
    }

}
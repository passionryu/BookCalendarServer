package bookcalendar.server.global.ExternalConnection.Client;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class EmotionClient {

    private final WebClient webClient;

    public EmotionClient(@Qualifier("fastApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> predict(String text) {
        return webClient.post()
                .uri("/emotion/predict")
                .bodyValue(Map.of("text", text))
                .retrieve()
                .bodyToMono(String.class);
    }
}
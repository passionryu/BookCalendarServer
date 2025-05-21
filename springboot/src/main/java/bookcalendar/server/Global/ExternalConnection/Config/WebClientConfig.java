package bookcalendar.server.global.ExternalConnection.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfig {

    // 현재 Host PC ip/ port: http://192.9.202.17:3004

    @Bean
    public WebClient fastApiWebClient() {
        return WebClient.builder()
                .baseUrl("http://192.9.202.17:3004") // 목표 서버 & 포트
//                .filter((request, next) -> {
//                    log.info("Request-webclientConfig: {} {}", request.method(), request.url());
//                    request.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
//                    return next.exchange(request);
//                })
                //.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}



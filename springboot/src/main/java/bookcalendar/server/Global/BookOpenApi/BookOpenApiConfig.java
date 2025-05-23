package bookcalendar.server.global.BookOpenApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BookOpenApiConfig {

    @Bean
    public RestTemplate restTemplate() {

        return new RestTemplate();
    }
}

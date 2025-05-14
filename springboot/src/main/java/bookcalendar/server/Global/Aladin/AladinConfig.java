package bookcalendar.server.global.Aladin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AladinConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /* 알라딘 OpneAPI키 입력 */
    @Bean
    public String aladinApiKey() {
        return "";
    }

    @Bean
    public String aladinSearchUrl() {
        return "";
    }
}

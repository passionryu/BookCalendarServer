package bookcalendar.server.global.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .version("v1.0.0") // 버전 기록
                        .title("Server") // API 명세서 제목
                        .description("Book Calendar")); // 상세

    }
}

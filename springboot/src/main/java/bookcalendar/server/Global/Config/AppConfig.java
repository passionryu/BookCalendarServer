package bookcalendar.server.global.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("bookcalendar.server.Domain.Community.Mapper")
public class AppConfig {
}

package bookcalendar.server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    /* 임시 방편의 Redis */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    // TODO : Redis 설정은 추후 따로 브랜치 추가하여 진행할 것

    // ======================= 학과 서버 Redis Config 로직 =========================

    // @Bean
    // public RedisConnectionFactory companyRedisConnectionFactory() {
    // LettuceConnectionFactory factory = new
    // LettuceConnectionFactory("company.redis.host", 6379);
    // factory.setDatabase(0);
    // return factory;
    // }
    //
    // @Bean
    // public RedisTemplate<String, String> companyRedisTemplate() {
    // RedisTemplate<String, String> template = new RedisTemplate<>();
    // template.setConnectionFactory(companyRedisConnectionFactory());
    // return template;
    // }

    // ======================= Podman Redis Config 로직 =========================

    // @Bean
    // public RedisConnectionFactory containerRedisConnectionFactory() {
    // LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost",
    // 6380); // 컨테이너 포트
    // factory.setDatabase(1);
    // return factory;
    // }
    //
    // @Bean
    // public RedisTemplate<String, String> containerRedisTemplate() {
    // RedisTemplate<String, String> template = new RedisTemplate<>();
    // template.setConnectionFactory(containerRedisConnectionFactory());
    // return template;
    // }
}

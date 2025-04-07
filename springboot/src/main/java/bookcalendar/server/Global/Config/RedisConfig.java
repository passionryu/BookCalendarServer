package bookcalendar.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RedisConfig {

    // 회사 서버 Redis 설정
    @Value("${spring.data.redis.company.host}")
    private String companyRedisHost;

    @Value("${spring.data.redis.company.port}")
    private int companyRedisPort;

    @Value("${spring.data.redis.company.password}")
    private String companyRedisPassword;

    // 컨테이너 Redis 설정
    @Value("${spring.data.redis.container.host}")
    private String containerRedisHost;

    @Value("${spring.data.redis.container.port}")
    private int containerRedisPort;

    @Value("${spring.data.redis.container.password}")
    private String containerRedisPassword;

    // 회사 서버 Redis Connection Factory
    @Bean(name = "companyRedisConnectionFactory")
    public RedisConnectionFactory companyRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(companyRedisHost);
        config.setPort(companyRedisPort);
        config.setPassword(companyRedisPassword);
        return new LettuceConnectionFactory(config);
    }

    // 컨테이너 Redis Connection Factory
    @Bean(name = "containerRedisConnectionFactory")
    @Primary
    public RedisConnectionFactory containerRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(containerRedisHost);
        config.setPort(containerRedisPort);
        config.setPassword(containerRedisPassword);
        return new LettuceConnectionFactory(config);
    }

    // 회사 서버 Redis Template (캐싱용)
    @Bean(name = "companyRedisTemplate")
    public RedisTemplate<String, String> companyRedisTemplate(
            @Qualifier("companyRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // 컨테이너 Redis Template (데이터 저장용)
    @Bean(name = "containerRedisTemplate")
    @Primary
    public RedisTemplate<String, String> containerRedisTemplate(
            @Qualifier("containerRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}

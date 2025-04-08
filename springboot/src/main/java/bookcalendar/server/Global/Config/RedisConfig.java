package bookcalendar.server.global.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

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

    // ======================= Redis Basic Configuration : 오류 방지용 =========================

    /**
     * 기본 이름의 redisTemplate
     *
     * description : RedisTemplate의 기본 이름이 필요한 외부 또는 내부 컴포넌트를 에러 없이 동작시키기 위해 사용
     *
     * @param connectionFactory
     * @return
     */
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate(
            @Qualifier("containerRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        return containerRedisTemplate(connectionFactory); // 재사용
    }

    // ======================= Company Server Local Redis - only Cache =========================

    /**
     * 회사 서버 Redis Connection Factory
     *
     * @return
     */
    @Bean(name = "companyRedisConnectionFactory")
    public RedisConnectionFactory companyRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(companyRedisHost);
        config.setPort(companyRedisPort);
        config.setPassword(companyRedisPassword);
        return new LettuceConnectionFactory(config);
    }

    /**
     * 캐싱용 CacheManager
     *
     * @param connectionFactory
     * @return
     */
    @Bean(name = "companyRedisCacheManager")
    public CacheManager companyRedisCacheManager(
            @Qualifier("companyRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 회사 서버 Redis Template - redis를 직접 다룰 때 사용
     *
     * @param connectionFactory
     * @return
     */
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

    // ======================= Podman Container Redis - For Session & ETC =========================

    /**
     * 컨테이너 Redis Connection Factory
     *
     * @return
     */
    @Bean(name = "containerRedisConnectionFactory")
    public RedisConnectionFactory containerRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(containerRedisHost);
        config.setPort(containerRedisPort);
        config.setPassword(containerRedisPassword);
        return new LettuceConnectionFactory(config);
    }

    /**
     * 컨테이너 Redis Template (데이터 저장용)
     *
     * @param connectionFactory
     * @return
     */
    @Bean(name = "containerRedisTemplate")
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

package bookcalendar.server.global.config;

import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // Cache - Redis 설정
    @Value("${spring.data.redis.cache.host}")
    private String cacheRedisHost;

    @Value("${spring.data.redis.cache.port}")
    private int cacheRedisPort;

    @Value("${spring.data.redis.cache.password}")
    private String cacheRedisPassword;

    // Session - Redis 설정
    @Value("${spring.data.redis.session.host}")
    private String sessionRedisHost;

    @Value("${spring.data.redis.session.port}")
    private int sessionRedisPort;

    @Value("${spring.data.redis.session.password}")
    private String sessionRedisPassword;

    // ======================= Redis Basic Config =========================

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
            @Qualifier("sessionRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        return sessionRedisTemplate(connectionFactory); // 재사용
    }

    // ======================= Cache Redis =========================

    /**
     * Cache Redis Connection Factory
     *
     * @return
     */
    @Bean(name = "cacheRedisConnectionFactory")
    public RedisConnectionFactory cacheRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(cacheRedisHost);
        config.setPort(cacheRedisPort);
        config.setPassword(cacheRedisPassword);
        return new LettuceConnectionFactory(config);
    }

    /**
     * 캐싱용 CacheManager
     *
     * @param connectionFactory
     * @return
     */
    @Bean(name = "cacheRedisCacheManager")
    public CacheManager cacheRedisCacheManager(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory connectionFactory) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 포맷 유지

//        objectMapper.activateDefaultTyping(
//                objectMapper.getPolymorphicTypeValidator(),
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.PROPERTY
//        );

        // BookResponse용 Jackson2JsonRedisSerializer 설정
        Jackson2JsonRedisSerializer<BookResponse> serializer = new Jackson2JsonRedisSerializer<>(BookResponse.class);
        serializer.setObjectMapper(objectMapper);

        // GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Cache Redis Template - redis를 직접 다룰 때 사용
     *
     * @param connectionFactory
     * @return
     */
    @Bean(name = "cacheRedisTemplate")
    public RedisTemplate<String, String> cacheRedisTemplate(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // ======================= Session Redis =========================

    /**
     * Session Redis Connection Factory
     *
     * @return
     */
    @Bean(name = "sessionRedisConnectionFactory")
    public RedisConnectionFactory sessionRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(sessionRedisHost);
        config.setPort(sessionRedisPort);
        config.setPassword(sessionRedisPassword);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Session Redis Template (데이터 저장용)
     *
     * @param connectionFactory
     * @return
     */
    @Bean(name = "sessionRedisTemplate")
    public RedisTemplate<String, String> sessionRedisTemplate(
            @Qualifier("sessionRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setHashValueSerializer(new StringRedisSerializer());
//        template.afterPropertiesSet();
        return template;
    }
}

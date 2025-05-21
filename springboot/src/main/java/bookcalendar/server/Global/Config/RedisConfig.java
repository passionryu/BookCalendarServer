package bookcalendar.server.global.config;

import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Community.DTO.Response.TopLikedPosts;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // ======================= Default =========================

        // Generic default (모든 캐시에 무난하게 적용될 기본값)
        GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // TTL 30분
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer));

        // ======================= Book :: 도서 정보 조회 - BookResponse =========================

        /* BookResponse 전용 Serializer 설정 */
        Jackson2JsonRedisSerializer<BookResponse> serializer = new Jackson2JsonRedisSerializer<>(BookResponse.class);
        serializer.setObjectMapper(objectMapper);

        /*  default cache config (BookResponse 전용)  */
        RedisCacheConfiguration BookResponseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // TTL 60분
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer));

        // ======================= Community :: TOP3 게시글 조회 - TopLikedPosts =========================

        /* TopLikedPosts 전용 Serializer 설정 */
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, TopLikedPosts.class);
        Jackson2JsonRedisSerializer<List<TopLikedPosts>> topLikedPostsListSerializer = new Jackson2JsonRedisSerializer<>(type);
        topLikedPostsListSerializer.setObjectMapper(objectMapper);

        /* top3Posts 캐시만 TTL과 Serializer를 따로 지정 */
        RedisCacheConfiguration top3Config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL 10분
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(topLikedPostsListSerializer));

        // =======================  캐시 이름별로 서로 다른 캐싱 정책을 적용 =========================
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("bookInfo", BookResponseConfig);
        cacheConfigurations.put("top3Posts", top3Config);

        // ======================= 최종 반환 =========================

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)  // 기본 설정
                .withInitialCacheConfigurations(cacheConfigurations) // 개별 캐시 설정 적용
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

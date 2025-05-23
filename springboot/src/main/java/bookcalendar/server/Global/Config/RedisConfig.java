package bookcalendar.server.global.config;

import bookcalendar.server.Domain.Book.DTO.Response.BookResponse;
import bookcalendar.server.Domain.Book.DTO.Response.PeriodResponse;
import bookcalendar.server.Domain.Community.DTO.Response.PostListResponse;
import bookcalendar.server.Domain.Community.DTO.Response.TopLikedPosts;
import bookcalendar.server.Domain.Mypage.DTO.Response.MyReviewList;
import bookcalendar.server.Domain.Mypage.DTO.Response.MyScrapListResponse;
import bookcalendar.server.Domain.Mypage.DTO.Response.StatisticResponse;
import bookcalendar.server.Domain.Mypage.Entity.Cart;
import bookcalendar.server.Domain.Review.DTO.Response.CalendarResponse;
import bookcalendar.server.Domain.Review.DTO.Response.MainPageResponse;
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

        Jackson2JsonRedisSerializer<BookResponse> serializer = new Jackson2JsonRedisSerializer<>(BookResponse.class);
        serializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration bookResponseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // TTL 60분
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // ======================= Book :: 월별 도서 리스트 - List<PeriodResponse> =========================

        Jackson2JsonRedisSerializer<List<PeriodResponse>> monthlyBookListSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructCollectionType(List.class, PeriodResponse.class));
        monthlyBookListSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration monthlyBookListConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(6)) // TTL 6시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(monthlyBookListSerializer));

        // ======================= Community :: TOP3 게시글 조회 - TopLikedPosts =========================

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, TopLikedPosts.class);
        Jackson2JsonRedisSerializer<List<TopLikedPosts>> topLikedPostsListSerializer = new Jackson2JsonRedisSerializer<>(type);
        topLikedPostsListSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration top3Config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL 10분
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(topLikedPostsListSerializer));

        // ======================= Community : 게시글 리스트 일괄 조회 - List<PostListResponse> =========================

        Jackson2JsonRedisSerializer<List<PostListResponse>> postListSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructCollectionType(List.class, PostListResponse.class));
        postListSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration PostListConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL 10분
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(postListSerializer));

        // ======================= Review :: 월별 독후감 리스트 - List<CalendarResponse> =========================

        Jackson2JsonRedisSerializer<List<CalendarResponse>> monthlyReviewListSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructCollectionType(List.class, CalendarResponse.class));
        monthlyReviewListSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration monthlyReviewListConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12)) // TTL 12시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(monthlyReviewListSerializer));

        // ======================= Review :: 메인 페이지 독후감 진행률 & 남은 독서일 정보 - MainPageResponse =========================

        Jackson2JsonRedisSerializer<MainPageResponse> mainPageResponseSerializer = new Jackson2JsonRedisSerializer<>(MainPageResponse.class);
        mainPageResponseSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration mainPageResponseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12)) // TTL 12시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(mainPageResponseSerializer));

        // ======================= Mypage :: 내 독후감 리스트 일괄 조회 - List<MyReviewList> =========================

        Jackson2JsonRedisSerializer<List<MyReviewList>> myReviewListSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructCollectionType(List.class, MyReviewList.class));
        myReviewListSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration myReviewListConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12)) // TTL 12시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(myReviewListSerializer));

        // ======================= Mypage :: 내 스크랩 리스트 일괄 조회 - List<MyScrapListResponse> =========================

        Jackson2JsonRedisSerializer<List<MyScrapListResponse>> myScrapListSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructCollectionType(List.class, MyScrapListResponse.class));
        myScrapListSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration myScrapListConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // TTL 1시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(myScrapListSerializer));

        // ======================= Mypage :: 내 장바구니 리스트 일괄 조회 List<Cart> =========================

        Jackson2JsonRedisSerializer<List<Cart>> myCartListSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructCollectionType(List.class, Cart.class));
        myCartListSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration myCartListConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // TTL 1시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(myCartListSerializer));

        // ======================= Mypage :: 독서 수 & 독후감 작성 수 조회- StatisticResponse =========================

        Jackson2JsonRedisSerializer<StatisticResponse> statisticResponseSerializer = new Jackson2JsonRedisSerializer<>(StatisticResponse.class);
        statisticResponseSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration statisticResponseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(12)) // TTL 12시간
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(statisticResponseSerializer));

        // =======================  캐시 이름별로 서로 다른 캐싱 정책을 적용 =========================
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("bookInfo", bookResponseConfig);
        cacheConfigurations.put("monthlyBookList", monthlyBookListConfig);
        cacheConfigurations.put("top3Posts", top3Config);
        cacheConfigurations.put("monthlyReviewList", monthlyReviewListConfig);
        cacheConfigurations.put("mainPageResponse", mainPageResponseConfig);
        cacheConfigurations.put("myReviewList", myReviewListConfig);
        cacheConfigurations.put("myScrapList", myScrapListConfig);
        cacheConfigurations.put("myCartList", myCartListConfig);
        cacheConfigurations.put("myStatistics", statisticResponseConfig);
        cacheConfigurations.put("postList", PostListConfig);

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

        return template;
    }
}

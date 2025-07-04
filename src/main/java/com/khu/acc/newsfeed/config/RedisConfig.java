package com.khu.acc.newsfeed.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (!redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        config.setDatabase(database);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // JSON serializer 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.registerModule(new JavaTimeModule());

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Key serializer
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // Value serializer
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 캐시별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // === 기존 캐시 설정 ===
        // 사용자 정보 캐시 (1시간)
        cacheConfigurations.put("users", config.entryTtl(Duration.ofHours(1)));

        // 포스트 캐시 (30분)
        cacheConfigurations.put("posts", config.entryTtl(Duration.ofMinutes(30)));

        // 포스트 댓글 캐시 (15분)
        cacheConfigurations.put("postComments", config.entryTtl(Duration.ofMinutes(15)));

        // 포스트 좋아요 캐시 (10분)
        cacheConfigurations.put("postLikes", config.entryTtl(Duration.ofMinutes(10)));

        // 트렌딩 포스트 캐시 (5분)
        cacheConfigurations.put("trendingPosts", config.entryTtl(Duration.ofMinutes(5)));

        // === 개인화 피드 관련 캐시 설정 ===
        // 뉴스피드 캐시 (30분) - 가장 중요한 캐시
        cacheConfigurations.put("newsFeed", config.entryTtl(Duration.ofMinutes(30)));

        // 사용자 개인화 피드 캐시 (30분)
        cacheConfigurations.put("userFeed", config.entryTtl(Duration.ofMinutes(30)));

        // 개인화 데이터 캐시 (1시간) - 사용자의 관심사, 상호작용 패턴 등
        cacheConfigurations.put("userPersonalizationData", config.entryTtl(Duration.ofHours(1)));

        // 피드 통계 캐시 (1시간)
        cacheConfigurations.put("userFeedStats", config.entryTtl(Duration.ofHours(1)));

        // 사용자 통계 캐시 (1시간)
        cacheConfigurations.put("userStats", config.entryTtl(Duration.ofHours(1)));

        // === 팔로우 관련 캐시 설정 ===
        // 팔로잉 목록 캐시 (1시간)
        cacheConfigurations.put("followings", config.entryTtl(Duration.ofHours(1)));

        // 팔로워 목록 캐시 (1시간)
        cacheConfigurations.put("followers", config.entryTtl(Duration.ofHours(1)));

        // 팔로우 상태 캐시 (2시간) - 자주 바뀌지 않음
        cacheConfigurations.put("followStatus", config.entryTtl(Duration.ofHours(2)));

        // 팔로우 통계 캐시 (1시간)
        cacheConfigurations.put("followStats", config.entryTtl(Duration.ofHours(1)));

        // 팔로잉 사용자 ID 목록 캐시 (1시간) - 피드 생성에 중요
        cacheConfigurations.put("followingUserIds", config.entryTtl(Duration.ofHours(1)));

        // 팔로워 사용자 ID 목록 캐시 (1시간)
        cacheConfigurations.put("followerUserIds", config.entryTtl(Duration.ofHours(1)));

        // 추천 사용자 캐시 (6시간) - 자주 바뀌지 않음
        cacheConfigurations.put("recommendedUsers", config.entryTtl(Duration.ofHours(6)));

        // === 전역 통계 캐시 설정 ===
        // 활성 사용자 수 (12시간)
        cacheConfigurations.put("activeUserCount", config.entryTtl(Duration.ofHours(12)));

        // === 단기 캐시 설정 ===
        // 실시간 알림 관련 (5분)
        cacheConfigurations.put("notifications", config.entryTtl(Duration.ofMinutes(5)));

        // 검색 결과 캐시 (15분)
        cacheConfigurations.put("searchResults", config.entryTtl(Duration.ofMinutes(15)));

        log.info("Redis cache manager configured with {} cache configurations", cacheConfigurations.size());

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
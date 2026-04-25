package com.internship.tool.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Tool-53 — Redis Cache Configuration (Day 6)
 *
 * Cache names and TTLs:
 * - "controls"      → 10 min  (GET /all paginated list)
 * - "control"       → 10 min  (GET /{id} single record)
 * - "stats"         → 10 min  (GET /stats dashboard KPIs)
 * - "ai-responses"  → 15 min  (AI microservice responses — AI Developer 2 wires on Day 8)
 *
 * SECURITY NOTES:
 * - Redis password loaded from ENV — never hardcoded
 * - Cache keys include user context where needed to prevent data leakage between users
 * - Null values not cached — prevents cache poisoning
 */
@Configuration
@EnableCaching
public class RedisConfig {

    // Cache name constants — used in @Cacheable / @CacheEvict annotations
    public static final String CACHE_CONTROLS = "controls";
    public static final String CACHE_CONTROL  = "control";
    public static final String CACHE_STATS    = "stats";
    public static final String CACHE_AI       = "ai-responses";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Default config — 10 min TTL for all caches
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()           // prevent cache poisoning
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // AI responses — 15 min TTL as per spec
        cacheConfigs.put(CACHE_AI, defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Stats — shorter TTL so dashboard stays fresh
        cacheConfigs.put(CACHE_STATS, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}

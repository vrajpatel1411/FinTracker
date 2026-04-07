package org.vrajpatel.personalexpense.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.vrajpatel.personalexpense.responseDto.PersonalExpenseDto;

import java.time.Duration;

@Configuration
public class RedisCacheManagerConfiguration {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig(Thread.currentThread().getContextClassLoader()).disableCachingNullValues();
    }

    @Bean
    public RedisTemplate<String, Object> RedisTemplateConfiguration(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        JacksonJsonRedisSerializer<PersonalExpenseDto> serializer = new JacksonJsonRedisSerializer<>(PersonalExpenseDto.class);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory,
                                          RedisCacheConfiguration redisCacheConfiguration) {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(redisCacheConfiguration)
                .withCacheConfiguration("personalExpenses",
                        redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("analytics",
                        redisCacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .build();
    }
}
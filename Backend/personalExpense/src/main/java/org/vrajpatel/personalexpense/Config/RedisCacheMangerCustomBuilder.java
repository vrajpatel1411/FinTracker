package org.vrajpatel.personalexpense.Config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
public class RedisCacheMangerCustomBuilder {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder)->{
            builder
                    .withCacheConfiguration("personalExpenses", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(1)));

        };
    }
}

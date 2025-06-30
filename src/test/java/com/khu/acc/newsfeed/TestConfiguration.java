package com.khu.acc.newsfeed;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
@EnableAutoConfiguration(exclude = {
        RedisAutoConfiguration.class
})
public class TestConfiguration {
    // Test configuration that excludes external dependencies
}
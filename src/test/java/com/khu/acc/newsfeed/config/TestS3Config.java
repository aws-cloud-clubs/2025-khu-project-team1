package com.khu.acc.newsfeed.config;

import com.khu.acc.newsfeed.post.image.ImageUploadService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestS3Config {
    
    @Bean
    @Primary
    public ImageUploadService imageUploadService() {
        return mock(ImageUploadService.class);
    }
}
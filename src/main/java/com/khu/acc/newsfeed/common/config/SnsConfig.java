package com.khu.acc.newsfeed.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * AWS SNS 설정
 */
@Configuration
@Slf4j
public class SnsConfig {
    
    @Value("${aws.region}")
    private String awsRegion;
    
    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
    
    @Bean
    public SnsTemplate snsTemplate(SnsClient snsClient) {
        return new SnsTemplate(snsClient);
    }
}
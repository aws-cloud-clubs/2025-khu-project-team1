package com.khu.acc.newsfeed.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.Mockito.mock;

/**
 * 테스트용 AWS SDK v2 설정
 * v1 DynamoDBMapper 대신 v2 DynamoDbEnhancedClient 사용
 */
@TestConfiguration
@Profile("test")
public class TestDynamoDBConfig {

    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient() {
        // Return a mock for testing
        return mock(DynamoDbClient.class);
    }

    @Bean
    @Primary
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        // Return a mock for testing
        return mock(DynamoDbEnhancedClient.class);
    }
    
    @Bean
    @Primary
    public S3Client s3Client() {
        // Return a mock for testing
        return mock(S3Client.class);
    }
}
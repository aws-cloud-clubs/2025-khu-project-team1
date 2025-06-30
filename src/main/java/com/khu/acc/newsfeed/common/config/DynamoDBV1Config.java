package com.khu.acc.newsfeed.common.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * AWS SDK v1 DynamoDBMapper 설정
 */
@Configuration
@Profile("!test")
public class DynamoDBV1Config {

    @Value("${aws.region:ap-northeast-2}")
    private String awsRegion;

    /**
     * 운영/개발 환경용 DynamoDB 클라이언트
     */
    @Bean
    @Primary
    @Profile("!local")
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(awsRegion)
                .build();
    }

    /**
     * 로컬 개발 환경용 DynamoDB 클라이언트
     */
    @Bean
    @Profile("local")
    public AmazonDynamoDB amazonDynamoDBLocal() {
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                    new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:8000", awsRegion))
                .build();
    }

    /**
     * DynamoDB Mapper
     */
    @Bean
    @Primary
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDBMapper(amazonDynamoDB, DynamoDBMapperConfig.DEFAULT);
    }
}